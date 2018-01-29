package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource;

public interface IDataSourceWrapper {

	public boolean initialize();

	public void index();

	public void close();

}
