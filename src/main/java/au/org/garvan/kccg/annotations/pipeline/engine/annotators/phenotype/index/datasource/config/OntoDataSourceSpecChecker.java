package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper.MyOWLObjectVisitor;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper.NamespaceDefinition;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.GeneralUtil;

public class OntoDataSourceSpecChecker {

	private static final Logger logger = LoggerFactory.getLogger(OntoDataSourceSpecChecker.class);

	private DataSourceSpec dataSourceSpec;
	
	private Map<String, NamespaceDefinition> namespaces;
	
	private Map<String, String> indexProperties;
	
	private String preferredLabel;
	
	private List<OWLClass> classesToIndex;

	private MyOWLObjectVisitor objectVisitor;

	public OntoDataSourceSpecChecker(DataSourceSpec dataSourceSpec, String preferredLabel, Map<String, String> indexProperties, 
			OWLOntology ontology, OWLReasoner reasoner) {
		this.dataSourceSpec = dataSourceSpec;
		this.preferredLabel = preferredLabel;
		this.indexProperties = indexProperties;
		logger.debug("Properties to index: " + indexProperties);
		
		this.namespaces = new HashMap<String, NamespaceDefinition>();
		this.classesToIndex = new ArrayList<OWLClass>();
		this.objectVisitor = new MyOWLObjectVisitor();
		
		logger.info("Processing URI patterns ...");
		namespaces.put(dataSourceSpec.getUri_pattern().getNamespace(), new NamespaceDefinition(dataSourceSpec.getUri_pattern(), ontology, reasoner));

		logger.info("Compiling classes targeted for indexing ...");
		processClasses(ontology);
	}

	private void processClasses(OWLOntology ontology) {
		for (OWLClass cls : ontology.getClassesInSignature()) {
			String[] split = GeneralUtil.splitURI(cls.getIRI().toString());
			String namespace = split[0];
			String uri = split[1];
			
			if (namespaces.containsKey(namespace)) {
				if (namespaces.get(namespace).accepts(uri)) {
					classesToIndex.add(cls);
				}
			}
		}
	}

	public boolean isValidIndexProperty(String propertyURI, OWLAnnotationAssertionAxiom annotationAxiom) {
		if (indexProperties.containsKey(propertyURI)) {
			String subProperty = indexProperties.get(propertyURI);
			if (subProperty.equalsIgnoreCase("")) {
				return true;
			}
			
			Set<OWLAnnotation> annotations = annotationAxiom.getAnnotations();
			for (OWLAnnotation annotation : annotations) {
				annotation.getValue().accept(objectVisitor);
				String annotPropertyURI = annotation.getProperty().getIRI().toString();
				String annotValue = objectVisitor.getValue();

				if (annotPropertyURI.equalsIgnoreCase(propertyURI) && subProperty.equalsIgnoreCase(annotValue)) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	public int getNoClassesToIndex() {
		return classesToIndex.size();
	}

	public List<OWLClass> getClasses() {
		return classesToIndex;
	}

	public DataSourceSpec getDataSourceSpec() {
		return dataSourceSpec;
	}

	public String getPreferredLabel() {
		return preferredLabel;
	}
}
