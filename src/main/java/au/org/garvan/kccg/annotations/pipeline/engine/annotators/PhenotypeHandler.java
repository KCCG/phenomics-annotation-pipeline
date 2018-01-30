package au.org.garvan.kccg.annotations.pipeline.engine.annotators;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.LongestMatchSort;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.LongestSameMatchSort;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.beans.ConceptAnnotation;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary.CRResources;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary.TASimpleDictionary;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.InputProcessorThread;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.ProcessedInput;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate.MatrixBasedCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate.NCCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.negation.NegationDetection;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.patterns.InputProcessorPatternsReader;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.process.PseudoTA;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.relations.FilterPhenotypes;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.relations.RelationDetection;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.spellcheck.CRSpellCheck;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.spellcheck.SpellCheckThread;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.search.IndexSearch;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.ConceptType;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.DS_ConceptInfo;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.IndexConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.StatsUtil;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhenotypeHandler {
    private static final Logger logger = LoggerFactory.getLogger(PhenotypeHandler.class);


    private boolean valid = false;

    private int maxThreads;
    private boolean processNC;

    private InputProcessorPatternsReader patternsReader;
    private MatrixBasedCandidateGenerator matrixBasedGenerator;
    private NCCandidateGenerator phenoCandidateGenerator;

    private IndexSearch indexSearch;
    private CRSpellCheck spellCheck;
//
//    @Getter
//    private static CRResources crResources;

    private Map<String, Double> stats;
    private ProcessedInput pInput;


    public  PhenotypeHandler(String resourcesFolder, int maxThreads, boolean processNC) {
        this.maxThreads = maxThreads;
        this.processNC = processNC;
        this.stats = new LinkedHashMap<String, Double>();
        this.valid = initComponents(resourcesFolder);


        logger.info("Initializing Input Processor ...");
        indexSearch = new IndexSearch(resourcesFolder + IndexConstants.FOLDER_DATASOURCES, maxThreads);

        logger.info("Initializing SpellCheck ...");
        spellCheck = new CRSpellCheck(resourcesFolder);


    }
    private boolean initComponents(String resourcesFolder) {
        File file = new File(resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE);
        if (!file.exists()) {
            logger.error("Failed to initialize Input Processor: Pattern file [" + resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE + "] not found.");
            return false;
        }
        //CR: Hack
//        crResources = new CRResources(new Properties());
        PseudoTA.init(resourcesFolder);
        patternsReader = new InputProcessorPatternsReader(resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE);
        if (patternsReader.isValid()) {
            matrixBasedGenerator = new MatrixBasedCandidateGenerator(maxThreads, patternsReader.getGeneralPatterns());
            phenoCandidateGenerator = new NCCandidateGenerator(patternsReader.getNcPatterns());
            logger.info("Input Processor initialized. Using " + maxThreads + " threads.");
            return true;
        } else {
            return false;
        }
    }


    public void close() {
        patternsReader.close();
    }

    public TASimpleDictionary getDictionary(String dictionary) {
        return PseudoTA.crResources.getSimpleDictionary(dictionary);
    }

    public ProcessedInput processInput(List<APSentence> sentenceList) {
        double sTime = System.currentTimeMillis();
        double eTime ;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        ProcessedInput processedInput = new ProcessedInput();
        for (APSentence sent : sentenceList) {
            executor.execute(new InputProcessorThread(sent, processedInput, matrixBasedGenerator, processNC, phenoCandidateGenerator));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        eTime = System.currentTimeMillis();
        this.stats.put(StatsUtil.INPUT_PROC_TIME, (eTime - sTime));

        return processedInput;
    }

    public Map<DataSourceMetadata, List<ConceptAnnotation>> annotate(List<APSentence> sents, boolean longestMatch, boolean spellCheck, boolean detectNegation, boolean addRelations) {
        ProcessedInput pInput = processInput(sents);

        double sTime = System.currentTimeMillis();
        Map<String, List<String>> vocabulary = doSpellCheck(pInput, spellCheck);
        double eTime = System.currentTimeMillis();
        this.stats.put(StatsUtil.VOCAB_PROC_TIME, (eTime - sTime));

        Map<DataSourceMetadata, Map<ConceptCandidate, DS_ConceptInfo>> dsConceptCandidates = indexSearch.search(vocabulary, getDictionary(TAConstants.DICT_LEXVAR), pInput.getConceptCandidates());
        this.stats.putAll(indexSearch.getStats());

        sTime = System.currentTimeMillis();
        Map<DataSourceMetadata, List<ConceptAnnotation>> conceptAnnotationResults = new HashMap<DataSourceMetadata, List<ConceptAnnotation>>();
        for (DataSourceMetadata ds : dsConceptCandidates.keySet()) {
            List<ConceptAnnotation> results = process(dsConceptCandidates.get(ds), longestMatch, spellCheck, detectNegation, addRelations);
            conceptAnnotationResults.put(ds, results);
        }
        eTime = System.currentTimeMillis();
        this.stats.put(StatsUtil.POST_PROC_TIME, (eTime - sTime));
        return conceptAnnotationResults;
    }

    private List<ConceptAnnotation> process(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates, boolean longestMatch, boolean spellCheck,
                                            boolean detectNegation, boolean addRelations) {
        if (detectNegation) {
            conceptCandidates = new NegationDetection(conceptCandidates).getCandidates();
        }
        List<ConceptAnnotation> annotations = new ArrayList<ConceptAnnotation>();
        if (addRelations) {
            RelationDetection relDetect = new RelationDetection(conceptCandidates);
            annotations = relDetect.getMapping().isEmpty() ?
                    transform(relDetect.getCandidates()) :
                    transformRelations(relDetect.getCandidates(), relDetect.getDsCandidates(), relDetect.getMapping());
        } else {
            annotations = transform(new FilterPhenotypes(conceptCandidates).getCandidates());
        }
        return longestMatch ? new LongestMatchSort(annotations).getAnnotations() : new LongestSameMatchSort(annotations).getAnnotations();
    }

    private List<ConceptAnnotation> transformRelations(
            Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates,
            Map<ConceptCandidate, DS_ConceptInfo> dsCandidates,
            Map<ConceptCandidate, ConceptCandidate> mapping) {
        List<ConceptAnnotation> list = new ArrayList<ConceptAnnotation>();
        for (ConceptCandidate candidate : conceptCandidates.keySet()) {
            DS_ConceptInfo conceptInfo = conceptCandidates.get(candidate);

            int startIndex = candidate.getStartOffset() - candidate.getSentence().getDocOffset().x;
            int endIndex = candidate.getEndOffset() - candidate.getSentence().getDocOffset().y;
            String target = candidate.getSentence().getOriginalText().substring(startIndex, endIndex);

            ConceptAnnotation annotation = new ConceptAnnotation(candidate.getStartOffset(), candidate.getEndOffset(), target);
            annotation.setConcept(conceptInfo);
            annotation.setNegated(candidate.isNegated());
            annotation.setLength(endIndex - startIndex);

            if (mapping.containsKey(candidate)) {
                annotation.addRelation(ConceptType.DEGREE_OF_SEVERITY, mapping.get(candidate), dsCandidates.get(mapping.get(candidate)));
            }

            list.add(annotation);
        }
        return list;
    }

    private List<ConceptAnnotation> transform(Map<ConceptCandidate, DS_ConceptInfo> conceptCandidates) {
        List<ConceptAnnotation> list = new ArrayList<ConceptAnnotation>();
        for (ConceptCandidate candidate : conceptCandidates.keySet()) {
            DS_ConceptInfo conceptInfo = conceptCandidates.get(candidate);

            int startIndex = candidate.getStartOffset();// - candidate.getSentence().getDocOffset().x;
            int endIndex = candidate.getEndOffset();// - candidate.getSentence().getDocOffset().x;
            String target = candidate.getSentence().getOriginalText().substring(startIndex, endIndex);
//CR:Fix this
            ConceptAnnotation annotation = new ConceptAnnotation(candidate.getStartOffset(), candidate.getEndOffset(), target);
            annotation.setConcept(conceptInfo);
            annotation.setNegated(candidate.isNegated());
            annotation.setLength(endIndex - startIndex);
            list.add(annotation);
        }
        return list;
    }

    private Map<String, List<String>> doSpellCheck(ProcessedInput pInput, boolean doSpellCheck) {
        Map<String, List<String>> spellCheckedMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (String key : pInput.getVocabulary().keySet()) {
            String value = pInput.getVocabulary().get(key);
            executor.execute(new SpellCheckThread(key, value, spellCheck, spellCheckedMap, doSpellCheck));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        logger.debug("Spell checked map: " + spellCheckedMap);

        return spellCheckedMap;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public boolean isValid() {
        return valid;
    }
}
