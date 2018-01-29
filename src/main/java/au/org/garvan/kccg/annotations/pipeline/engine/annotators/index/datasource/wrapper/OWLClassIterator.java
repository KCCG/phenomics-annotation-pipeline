package au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.wrapper;

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.IParsedObjectIterator;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.ParsedObject;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.index.datasource.config.OntoDataSourceSpecChecker;

public class OWLClassIterator implements IParsedObjectIterator {

	private static final Logger logger = LoggerFactory.getLogger(OWLClassIterator.class);

	private OWLOntology ontology;
	private Iterator<OWLClass> classesIterator;
	private OntoDataSourceSpecChecker dataSourceSpecChecker;

	public OWLClassIterator(OWLOntology ontology, OntoDataSourceSpecChecker dataSourceSpecChecker) {
		logger.debug("Number of classes received: " + dataSourceSpecChecker.getNoClassesToIndex());
		this.ontology = ontology;
		this.classesIterator = dataSourceSpecChecker.getClasses().iterator();
		this.dataSourceSpecChecker = dataSourceSpecChecker;
	}

	@Override
	public boolean hasNext() {
		return classesIterator.hasNext();
	}

	@Override
	public synchronized ParsedObject next() {
		return createParsedObject(classesIterator.next());
	}
	
	private ParsedObject createParsedObject(OWLClass current) {
		ParsedObject parsedObject = new ParsedObjectFromAnnotations(
				ontology.getAnnotationAssertionAxioms(current.getIRI()), dataSourceSpecChecker)
				.getParsedObject();
		parsedObject.setUri(current.getIRI().toString());
		return parsedObject;
	}

	@Override
	public void remove() {
		classesIterator.remove();
	}
}
