package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.input.spellcheck;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TAConstants;

public class CRSpellCheck {

	private static final Logger logger = LoggerFactory.getLogger(CRSpellCheck.class);

	private boolean valid;
	
	private String location;

	private SpellChecker spellChecker;

	public CRSpellCheck(String location) {
		this.location = location;
		initialize();
	}

	public void initialize() {
		try {
			String spellCheckIndexFolder = location + TAConstants.FOLDER_SPELLCHECK;
			File folder = new File(spellCheckIndexFolder);
			if (!folder.exists()) {
				logger.error("Unable to initialize Spell Check. Folder [" + folder + "] does not exist.");
				valid = false;
				return;
			}
			
			logger.info("Initializing Spell Check from: " + folder.getAbsolutePath());
			Path dir = Paths.get(folder.getAbsolutePath());
			spellChecker = new SpellChecker(FSDirectory.open(dir));
			spellChecker.setAccuracy(0.9f);
			valid = true;
		} catch (IOException e) {
			logger.error("Unable to initialize Spell Check: " + e.getMessage(), e);
			valid = false;
		}
	}
	
	public boolean hasEntry(String input) {
		boolean exists = true;
		try {
			exists = spellChecker.exist(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return exists;
	}

	public List<String> suggest(String input) {
		List<String> sugestions = new ArrayList<String>();
		try {
			String[] suggest = spellChecker.suggestSimilar(input, 5);
			if (suggest != null) {
				for (String word : suggest) {
					sugestions.add(word);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sugestions;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void close() {
		try {
			if (valid) {
				spellChecker.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
