package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.entry;

import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APToken;

import java.util.List;
import java.util.Map;


public class IndexEntryLabelThread implements Runnable {

	private List<APToken> tokenList;
	
	// ORIGINAL TOKEN HASH -> ORIGINAL TOKEN
	private Map<Integer, String> symbolSet;

	public IndexEntryLabelThread(List<APToken> tokenList, Map<Integer, String> symbolSet) {
		this.symbolSet = symbolSet;
		this.tokenList = tokenList;
	}

	@Override
	public void run() {
		for (APToken token : tokenList) {
			int tokenHash = token.getOriginalText().hashCode();
			symbolSet.put(tokenHash, token.getNormalizedText());
		}
	}
}
