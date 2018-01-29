package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.LabelObject;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IndexDocumentEntryThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(IndexDocumentEntryThread.class);

	private int conceptHash;
	private LabelObject taLabelObject;
	private Map<Integer, String> symbolMap;
	private NavigableSet<Object[]> documentMap;
	private NavigableSet<Object[]> documentSizeMap;
	
	public IndexDocumentEntryThread(int conceptHash,
			LabelObject taLabelObject, Map<Integer, String> symbolMap,
			NavigableSet<Object[]> documentMap, NavigableSet<Object[]> documentSizeMap) {
		this.conceptHash = conceptHash;
		this.taLabelObject = taLabelObject;
		this.symbolMap = symbolMap;
		this.documentMap = documentMap;
		this.documentSizeMap = documentSizeMap;
	}

	@Override
	public void run() {
		List<APToken> tokenList = taLabelObject.getTokenList();
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for (APToken token : tokenList) {
			String symbolID = symbolMap.get(token.getOriginalText().hashCode());
			int count = countMap.containsKey(symbolID) ? countMap.get(symbolID) : 0;
			count++;
			countMap.put(symbolID, count);
		}
		
		List<String> symbolList = new ArrayList<String>();
		for (String symbol : countMap.keySet()) {
			int count = countMap.get(symbol);
			documentMap.add(new Object[]{ symbol, conceptHash, taLabelObject.getLabel().hashCode(), count });
			documentSizeMap.add(new Object[]{ symbol, conceptHash, taLabelObject.getLabel().hashCode(), tokenList.size() });
			symbolList.add(symbol);
		}
		
		logger.debug(conceptHash + " => " + taLabelObject.getLabel() + " => " + symbolList);
	}

}
