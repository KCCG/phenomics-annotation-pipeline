package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.ParsedObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.OntoDataSourceSpecChecker;

public class ParsedObjectFromAnnotations {

	private static final Logger logger = LoggerFactory.getLogger(ParsedObjectFromAnnotations.class);

	private ParsedObject parsedObject;
	private OntoDataSourceSpecChecker dataSourceSpecChecker;
	private MyOWLObjectVisitor objectVisitor;

	public ParsedObjectFromAnnotations(Set<OWLAnnotationAssertionAxiom> set, OntoDataSourceSpecChecker dataSourceSpecChecker) {
		this.dataSourceSpecChecker = dataSourceSpecChecker;
		objectVisitor = new MyOWLObjectVisitor();

		parsedObject = new ParsedObject(dataSourceSpecChecker);
		processAnnotations(set);
	}

	private void processAnnotations(Set<OWLAnnotationAssertionAxiom> annotationAxioms) {
		for (OWLAnnotationAssertionAxiom annotationAxiom : annotationAxioms) {
			OWLAnnotationProperty annotationProperty = annotationAxiom.getProperty();
			OWLAnnotationValue annotationValue = annotationAxiom.getValue();
			annotationValue.accept(objectVisitor);
			String propertyURI = annotationProperty.getIRI().toString();
			String value = objectVisitor.getValue();

			if (dataSourceSpecChecker.getPreferredLabel().equalsIgnoreCase(propertyURI)) {
				parsedObject.setPreferredLabel(value);
			} else {
				if (dataSourceSpecChecker.isValidIndexProperty(propertyURI, annotationAxiom)) {
					parsedObject.addSynonym(value);
					logger.debug("LABEL: " + value);
				}
			}
		}
	}
	
	public ParsedObject getParsedObject() {
		return parsedObject;
	}
}
