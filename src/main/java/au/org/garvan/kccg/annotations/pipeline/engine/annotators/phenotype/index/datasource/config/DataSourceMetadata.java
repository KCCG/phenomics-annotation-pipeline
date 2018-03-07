package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DataSourceMetadata {

	public static final String ACRONYM = "ACRONYM";

	public static final String TITLE = "TITLE";

	public static final String VERSION = "VERSION";

	private Map<String, String> metadata;
	
	public DataSourceMetadata(Map<String, String> metadata) {
		this.metadata = new HashMap<String, String>();
		this.metadata.putAll(metadata);
	}

	public DataSourceMetadata(DataSourceConfig dataSourceConfig) {
		this.metadata = new HashMap<String, String>();
		this.metadata.put(ACRONYM, dataSourceConfig.getAcronym());
		this.metadata.put(TITLE, dataSourceConfig.getTitle());
		this.metadata.put(VERSION, dataSourceConfig.getVersion());
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DataSourceMetadata that = (DataSourceMetadata) o;

        return new EqualsBuilder()
                .append(metadata, that.metadata)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(metadata)
                .toHashCode();
    }

}
