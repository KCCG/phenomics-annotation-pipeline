//package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.remote;
//
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.DataSourceMetadata;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.IndexEntryAssembler;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.IndexEntryCreator;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.IndexProcessor;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.ta.TAPipeline;
//import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TimeUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.text.DecimalFormat;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by tudor on 18/01/17.
// */
//public class IndexManager {
//
//    private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);
//
//    private IndexProcessor indexProcessor;
//    private IndexEntryCreator indexEntryCreator;
//
//    public IndexManager(String outFolder, TAPipeline taPipeline) {
//        if (outFolder != null) {
//            if (!outFolder.endsWith("/")) {
//                outFolder += "/";
//            }
//        }
//
//        indexProcessor = new IndexProcessor(outFolder);
//        indexEntryCreator = new IndexEntryCreator(indexProcessor, taPipeline);
//
//        indexProcessor.initialize();
//    }
//
//    public void index(String dataSource, String version, List<RemoteIndexTerm> termsToIndex) {
//        logger.info("Started indexing ...");
//        double sTime = TimeUtil.start();
//
//        logger.info("Indexing classes ... [" + termsToIndex.size() + "]");
//
//        for (int i = 0; i < termsToIndex.size(); i++) {
//            RemoteIndexTerm term = termsToIndex.get(i);
//            logger.debug("Indexing: " + term.getUri());
//            indexEntryCreator.index(IndexEntryAssembler.fromRemoteTermDto(term));
//
//            if ((i % 1000) == 0) {
//                double percentage = ((double) i / termsToIndex.size()) * 100;
//                DecimalFormat df = new DecimalFormat("#.###");
//                logger.info("Progress: " + i + "/" + termsToIndex.size() + " (" + df.format(percentage) + "%)");
//            }
//
//        }
//
//        Map<String, String> metadataMap = new HashMap<>();
//        metadataMap.put("ACRONYM", dataSource);
//        metadataMap.put("TITLE", dataSource);
//        metadataMap.put("VERSION", version);
//        DataSourceMetadata metadata = new DataSourceMetadata(metadataMap);
//        indexEntryCreator.setMetadata(metadata);
//
//        logger.info("Indexing done ...");
////        indexEntryCreator.process();
//
//
//        logger.info("Finished indexing ...");
//        logger.info("Total indexing time: " + TimeUtil.end(sTime));
//    }
//
//    public void close() {
//        indexEntryCreator.close();
//        indexProcessor.closeIndex();
//    }
//}
