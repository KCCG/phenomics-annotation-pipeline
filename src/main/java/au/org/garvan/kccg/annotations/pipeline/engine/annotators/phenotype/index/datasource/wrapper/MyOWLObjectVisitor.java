package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;


public class MyOWLObjectVisitor implements OWLAnnotationValueVisitorEx<Object> {

	private String value;
	
	public MyOWLObjectVisitor() {
	}

	@Override
	public Object visit(IRI iri) {
		return iri;
	}

	@Override
	public Object visit(OWLAnonymousIndividual individual) {
		return individual;
	}

	@Override
	public Object visit(OWLLiteral literal) {
		value = literal.getLiteral();
		return literal;
	}

	public String getValue() {
		return value;
	}
}
