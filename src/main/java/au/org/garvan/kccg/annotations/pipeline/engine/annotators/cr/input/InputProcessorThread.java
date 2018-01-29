package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.concept.ICandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate.MatrixBasedCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.generate.NCCandidateGenerator;
import au.org.garvan.kccg.annotations.pipeline.engine.entities.linguistic.APSentence;

public class InputProcessorThread implements Runnable {

	private APSentence taSentence;
	private ProcessedInput processedInput;
	private MatrixBasedCandidateGenerator matrixBasedGenerator;
	private boolean processNC;
	private NCCandidateGenerator phenoCandidateGenerator;
	
	public InputProcessorThread(APSentence taSentence, ProcessedInput processedInput, MatrixBasedCandidateGenerator matrixBasedGenerator, boolean processNC, NCCandidateGenerator phenoCandidateGenerator) {
		this.taSentence = taSentence;
		this.processedInput = processedInput;
		this.matrixBasedGenerator = matrixBasedGenerator;
		this.processNC = processNC;
		this.phenoCandidateGenerator = phenoCandidateGenerator;
	}
	
	@Override
	public void run() {
		ICandidateGenerator candidateGenerator = matrixBasedGenerator.generate(taSentence, processedInput);
		candidateGenerator.generate();
		
		if (processNC) {
			candidateGenerator = phenoCandidateGenerator.generate(taSentence, processedInput.getVocabulary(), 
					processedInput.getConceptCandidates());
			candidateGenerator.generate();
		}
	}
}
