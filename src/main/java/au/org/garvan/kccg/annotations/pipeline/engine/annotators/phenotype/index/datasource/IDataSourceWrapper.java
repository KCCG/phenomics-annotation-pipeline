package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource;

public interface IDataSourceWrapper {

	public boolean initialize();

	public void index();

	public void close();

}
