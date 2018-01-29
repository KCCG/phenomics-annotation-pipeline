package au.org.garvan.kccg.annotations.pipeline.engine.annotators.cr.entities.dictionary;
import java.util.List;

public interface TADictionary {

	public boolean load(String dictFile, String dictName);
	
	public boolean close();
	
	public boolean hasKey(String key);

	public String getValue(String key);

	public List<String> getListValue(String key);

}
