package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.GeneralUtil;

public class SubClassRetriever {
	
	private static final String OWL_NOTHING = "http://www.w3.org/2002/07/owl#Nothing";

	private OWLReasoner reasoner;
	private List<OWLClass> subClasses;
	private List<String> superClasses;
	private Map<String, String> visitedSubclasses;
	private Map<String, String> visitedSuperclasses;
	
	public SubClassRetriever(OWLClass targetClass, OWLReasoner reasoner) {
		this.reasoner = reasoner;
		subClasses = new ArrayList<OWLClass>();
		superClasses = new ArrayList<String>();
		visitedSubclasses = new LinkedHashMap<String, String>();
		visitedSuperclasses = new LinkedHashMap<String, String>();
		
		retrieveSubClasses(targetClass);
		retrieveSuperClasses(targetClass);
	}

	private void retrieveSuperClasses(OWLClass targetClass) {
		String uri = GeneralUtil.stripURI(targetClass.getIRI().toString());
		if (!visitedSuperclasses.containsKey(uri)) {
			visitedSuperclasses.put(uri, "");
			NodeSet<OWLClass> superClassesSet = reasoner.getSuperClasses(targetClass, true);
			for (OWLClass superClass : superClassesSet.getFlattened()) {
				if (!superClass.getIRI().toString().equalsIgnoreCase(OWL_NOTHING)) {
					String superURI = GeneralUtil.stripURI(superClass.getIRI().toString());
					superClasses.add(superURI);
					retrieveSuperClasses(superClass.asOWLClass());
				}
			}
		}
	}

	private void retrieveSubClasses(OWLClass targetClass) {
		String uri = GeneralUtil.stripURI(targetClass.getIRI().toString());
		if (!visitedSubclasses.containsKey(uri)) {
			visitedSubclasses.put(uri, "");
			NodeSet<OWLClass> subClassesSet = reasoner.getSubClasses(targetClass, true);
			for (OWLClass subClass : subClassesSet.getFlattened()) {
				if (!subClass.getIRI().toString().equalsIgnoreCase(OWL_NOTHING)) {
					subClasses.add(subClass);
					retrieveSubClasses(subClass.asOWLClass());
				}
			}
		}
	}

	public List<OWLClass> getSubClasses() {
		return subClasses;
	}

	public List<String> getSuperClasses() {
		return superClasses;
	}
}
