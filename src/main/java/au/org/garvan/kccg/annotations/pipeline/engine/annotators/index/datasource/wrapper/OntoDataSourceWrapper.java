package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.wrapper;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.IDataSourceWrapper;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.IParsedObjectIterator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.ParsedObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.*;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.IndexEntryAssembler;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.entry.IndexEntryCreator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.util.TimeUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class OntoDataSourceWrapper implements IDataSourceWrapper {

	private static final Logger logger = LoggerFactory.getLogger(OntoDataSourceWrapper.class);

	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private IndexEntryCreator indexEntryCreator;
	private DataSourceConfig dataSourceConfig;
	
	private String preferredLabel;
	private Map<String, String> indexProperties;
	
	public OntoDataSourceWrapper(DataSourceConfig dataSourceConfig, IndexEntryCreator indexEntryCreator) {
		this.dataSourceConfig = dataSourceConfig;
		this.indexEntryCreator = indexEntryCreator;
		this.manager = OWLManager.createOWLOntologyManager();
		
		parseProperties(dataSourceConfig);
	}

	private void parseProperties(DataSourceConfig dataSourceConfig) {
		this.preferredLabel = dataSourceConfig.getPreferred_label_property();
		this.indexProperties = new HashMap<String, String>();
		for (DataSourceIndexProperty property : dataSourceConfig.getIndex_properties()) {
			String subProperty = property.getSubproperty() == null ? "" : property.getSubproperty();
			this.indexProperties.put(property.getProperty(), subProperty);
		}
	}

	@Override
	public boolean initialize() {
		logger.info("Loading ontology ...");
		IRI iri = IRI.create(new File(dataSourceConfig.getLocation()));
		try {
			double sTime = TimeUtil.start();
			ontology = manager.loadOntologyFromOntologyDocument(iri);
			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			reasoner = reasonerFactory.createReasoner(ontology);
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			logger.info("Ontology loaded [" + TimeUtil.end(sTime) + "] ...");
			return true;
		} catch (OWLOntologyCreationException e) {
			logger.error("Ontology parsing error ...");
			logger.error(e.getMessage());
			return false;
		}
	}

	@Override
	public void index() {
		int noClasses = ontology.getClassesInSignature().size();
		logger.info("Total number of classes ... [" + noClasses + "]");
		
		for (DataSourceSpec dataSourceSpec : dataSourceConfig.getSpec()) {
			OntoDataSourceSpecChecker dataSourceSpecChecker = new OntoDataSourceSpecChecker(dataSourceSpec, 
				preferredLabel, indexProperties, ontology, reasoner);

			IParsedObjectIterator iterator = new OWLClassIterator(ontology, dataSourceSpecChecker);
			noClasses = dataSourceSpecChecker.getNoClassesToIndex();
			
			logger.info("Indexing classes ... [" + noClasses + "]");
			for (int i = 0; i < noClasses; i++) {
				ParsedObject parsedObject = iterator.next();
				if (parsedObject != null) {
					logger.debug("Indexing: " + parsedObject.getUri());
			
					if ((i % 1000) == 0) {
						double percentage = ((double) i / noClasses) * 100;
						DecimalFormat df = new DecimalFormat("#.###");
						logger.info("Progress: " + i + "/" + noClasses + " (" + df.format(percentage) + "%)");
					}

					indexEntryCreator.index(IndexEntryAssembler.fromParsedObject(parsedObject));
				}
			}
		}

		DataSourceMetadata metadata = new DataSourceMetadata(dataSourceConfig);
		indexEntryCreator.setMetadata(metadata);

		logger.info("Indexing done ...");
		indexEntryCreator.process();
	}

	@Override
	public void close() {
		manager.removeOntology(ontology);
		manager = null;
	}
}
