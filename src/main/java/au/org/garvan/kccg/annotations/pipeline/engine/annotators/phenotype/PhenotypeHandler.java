package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.BaseLexiconHandler;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.LongestMatchSort;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.LongestSameMatchSort;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.beans.ConceptAnnotation;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.entities.dictionary.TASimpleDictionary;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.InputProcessorThread;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.ProcessedInput;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.concept.ConceptCandidate;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.generate.MatrixBasedCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.generate.NCCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.negation.NegationDetection;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.patterns.InputProcessorPatternsReader;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.process.PseudoTA;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.relations.FilterPhenotypes;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.relations.RelationDetection;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.spellcheck.CRSpellCheck;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.spellcheck.SpellCheckThread;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.search.IndexDataSource;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.search.IndexSearch;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.DataSourceMetadata;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.ConceptType;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry.DS_ConceptInfo;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.CRConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.IndexConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.StatsUtil;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.TAConstants;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.APPhenotype;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.mappers.APPhenotypeMapper;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.lexical.Annotation;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APDocument;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.apache.naming.ContextBindings.getClassLoader;

public class PhenotypeHandler {
    private static final Logger logger = LoggerFactory.getLogger(PhenotypeHandler.class);


    private boolean valid = false;

    private int maxThreads;
    private boolean processNC;
    private String resourcesPath;

    private InputProcessorPatternsReader patternsReader;
    private MatrixBasedCandidateGenerator matrixBasedGenerator;
    private NCCandidateGenerator phenoCandidateGenerator;

    private IndexSearch indexSearch;
    private CRSpellCheck spellCheck;

    private Map<String, Double> stats;

    private Map<String, DS_ConceptInfo> hpoToPhenotypeConcept;
    private Map<String, String> phenotypeLabelToHpo;


    public PhenotypeHandler() {
        //Fill configs using folder from resources.
        fillConfigs();
        this.stats = new LinkedHashMap<>();
        this.valid = initComponents(resourcesPath);

        logger.info("Initializing Index Search for Phenotype ...");
        indexSearch = new IndexSearch(resourcesPath + IndexConstants.FOLDER_DATASOURCES, maxThreads);

        logger.info("Initializing SpellCheck ...");
        spellCheck = new CRSpellCheck(resourcesPath);


        hpoToPhenotypeConcept = new HashMap<>();
        phenotypeLabelToHpo = new HashMap<>();
        IndexDataSource HpoDS = indexSearch.getIndexDataSource("HPO");
        for (DS_ConceptInfo concept : HpoDS.getConceptInfoMap().values()) {
            hpoToPhenotypeConcept.put(concept.getUri(), concept);
            List<String> names = concept.getAlternativeLabels().stream().map(x -> x.toLowerCase()).collect(Collectors.toList());
            names.add(concept.getPreferredLabel().toLowerCase());

            for (String name : names) {
                if (!Strings.isNullOrEmpty(name))
                    phenotypeLabelToHpo.put(name, concept.getUri());
            }
        }


    }

    private void fillConfigs() {
        this.maxThreads = CRConstants.MAX_THREADS_COUNT;
        this.processNC = CRConstants.PROCESS_NC;
        String path = CRConstants.RESOURCES_PATH;
        String resourcesRoot = System.getenv("PhenotypeResources");
        if(Strings.isNullOrEmpty(resourcesRoot)) {
            resourcesRoot = "/var/lib/";
        }
        resourcesPath = resourcesRoot + path;
        logger.info(String.format("Concept Recognizer resources path: %s", resourcesPath));
    }

    private boolean initComponents(String resourcesFolder) {
        File file = new File(resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE);
        if (!file.exists()) {
            logger.error("Failed to initialize Input Processor: Pattern file [" + resourcesFolder + TAConstants.INPUTPROC_PATTERN_FILE + "] not found.");
            return false;
        }

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

    private TASimpleDictionary getDictionary(String dictionary) {
        return PseudoTA.crResources.getSimpleDictionary(dictionary);
    }

    private ProcessedInput processInput(List<APSentence> sentenceList) {
        double sTime = System.currentTimeMillis();
        double eTime;
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

    public void processAndUpdateDocument(APDocument apDocument) {
        List<APSentence> sents = apDocument.getSentences();
        Map<DataSourceMetadata, List<ConceptAnnotation>> output = annotate(sents, true, true, true, true);

        for (DataSourceMetadata ds : output.keySet()) {
            //This should be a single execution loop as we are only looking for HPO
            if (ds.getMetadata().get(DataSourceMetadata.ACRONYM).equals("HPO")) {
                List<ConceptAnnotation> lstConcepts = output.get(ds);

                for (ConceptAnnotation conceptAnnotation : lstConcepts) {
                    //This loop should iterate over found annotations and put them in relevant sentences.
                    APSentence sentence = sents.stream().filter(s -> s.getDocOffset().x == conceptAnnotation.getSentOffSetBegin()).findFirst().orElse(null);
                    if (sentence != null) {
                        //Get tokens for annotations.
                        List<APToken> conceptTokens = sentence.getTokensInOffsetRange(conceptAnnotation.getStartOffset(), conceptAnnotation.getEndOffset());
                        sentence.putAnnoataion(convertConceptAnnotationToAnnotation(conceptAnnotation, conceptTokens, conceptAnnotation.isNegated(), ds.getMetadata().get(DataSourceMetadata.ACRONYM), ds.getMetadata().get(DataSourceMetadata.VERSION)));
                    }
                }
            }
        }


    }

    private Annotation convertConceptAnnotationToAnnotation(ConceptAnnotation conceptAnnotation, List<APToken> conceptTokens, Boolean isNegated,  String standard, String version) {
        //Create Annotation from ConceptAnnotation
        Annotation finalAnnotation = new Annotation();
        finalAnnotation.setEntity(new APPhenotype(conceptAnnotation.getConcept()));
        finalAnnotation.setTokenIDs(conceptTokens.stream().map(t -> t.getId()).collect(Collectors.toList()));
        finalAnnotation.setTokenOffsets(conceptTokens.stream().map(t -> t.getSentOffset()).collect(Collectors.toList()));
        finalAnnotation.setType(AnnotationType.PHENOTYPE);
        finalAnnotation.setStandard(standard);
        finalAnnotation.setVersion(version);
        finalAnnotation.setNegated(isNegated);
        return finalAnnotation;
    }

    private Map<DataSourceMetadata, List<ConceptAnnotation>> annotate(List<APSentence> sents, boolean longestMatch, boolean spellCheck, boolean detectNegation, boolean addRelations) {
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
            //CR:DONE  Adjusted offsets as APToken offset has a sentence reference instead of document.
            int startIndex = candidate.getStartOffset();// - candidate.getSentence().getDocOffset().x;
            int endIndex = candidate.getEndOffset();// - candidate.getSentence().getDocOffset().y;
            String target = candidate.getSentence().getOriginalText().substring(startIndex, endIndex);

            ConceptAnnotation annotation = new ConceptAnnotation(candidate.getStartOffset(), candidate.getEndOffset(), target);
            annotation.setConcept(conceptInfo);
            annotation.setNegated(candidate.isNegated());
            annotation.setLength(endIndex - startIndex);
            annotation.setSentOffSetBegin(candidate.getSentence().getDocOffset().x);

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


            //CR:DONE  Adjusted offsets as APToken offset has a sentence reference instead of document.
            int startIndex = candidate.getStartOffset();// - candidate.getSentence().getDocOffset().x;
            int endIndex = candidate.getEndOffset();// - candidate.getSentence().getDocOffset().x;
            String target = candidate.getSentence().getOriginalText().substring(startIndex, endIndex);

            ConceptAnnotation annotation = new ConceptAnnotation(candidate.getStartOffset(), candidate.getEndOffset(), target);
            annotation.setConcept(conceptInfo);
            annotation.setNegated(candidate.isNegated());
            annotation.setLength(endIndex - startIndex);
            annotation.setSentOffSetBegin(candidate.getSentence().getDocOffset().x);
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

    public List<APPhenotypeMapper> searchPhenotype(String infix){
         return phenotypeLabelToHpo.entrySet().stream().filter(x->x.getKey().contains(infix.toLowerCase())).map(p-> new APPhenotypeMapper(p.getValue(),p.getKey())).collect(Collectors.toList());
    }
    public String getPhenotypeLabelWithId(String id){
        if(hpoToPhenotypeConcept.containsKey(id)){
            return hpoToPhenotypeConcept.get(id).getPreferredLabel();
        }
        else return "";
    }
}
