package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.wrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.index.datasource.config.URIPatternDefinition;
import au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util.GeneralUtil;

public class NamespaceDefinition {

	private static final Logger logger = LoggerFactory.getLogger(NamespaceDefinition.class);

	private String namespace;

	private Map<String, String> includes = new HashMap<String, String>();
	
	private Map<String, String> excludesIndividuals = new HashMap<String, String>();

	private Map<String, String> excludesPatterns = new HashMap<String, String>();

	public NamespaceDefinition(URIPatternDefinition patternDefintion,
			OWLOntology ontology, OWLReasoner reasoner) {
		this.namespace = patternDefintion.getNamespace();
		processExcludes(patternDefintion.getExclude(), ontology, reasoner);
		logger.info("Excludes: " + excludesIndividuals.size());
		logger.info("Excludes patterns: " + excludesPatterns);
		processIncludes(patternDefintion.getInclude(), ontology, reasoner);
		logger.info("Includes: " + includes.size());
	}

	private void processIncludes(List<String> include, OWLOntology ontology, OWLReasoner reasoner) {
		if (include.isEmpty()) {
			for (OWLClass cls : ontology.getClassesInSignature()) {
				String[] split = GeneralUtil.splitURI(cls.getIRI().toString());
				String nSpace = split[0];
				if (!nSpace.equalsIgnoreCase(namespace)) {
					continue;
				}
				if (!excludesIndividuals.containsKey(split[1]) && !prefixExcluded(split[1])) {
					includes.put(split[1], "");
				}
			}
		} else {
			for (String i : include) {
				if (i.startsWith("^")) {
					i = i.substring(1);
					if (!excludesIndividuals.containsKey(i) && !prefixExcluded(i)) {
						includes.put(i, "");
					}
					addAllSubclassesOfToIncludes(i, ontology, reasoner);
				} else {
					if (!excludesIndividuals.containsKey(i) && !prefixExcluded(i)) {
						includes.put(i, "");
					}
				}
			}
		}
	}

	private boolean prefixExcluded(String uri) {
		for (String prefix : excludesPatterns.keySet()) {
			if (uri.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private void processExcludes(List<String> exclude, OWLOntology ontology, OWLReasoner reasoner) {
		for (String e : exclude) {
			if (e.startsWith("^")) {
				addAllSubclassesOfToExcludesIndividuals(e.substring(1), ontology, reasoner);
			} else {
				if (e.endsWith("*")) {
					excludesPatterns.put(e.substring(0, e.length() - 1), "");
				} else {
					excludesIndividuals.put(e, "");
				}
			}
		}
	}

	private void addAllSubclassesOfToExcludesIndividuals(String uri, OWLOntology ontology, OWLReasoner reasoner) {
		for (OWLClass cls : ontology.getClassesInSignature()) {
			String[] split = GeneralUtil.splitURI(cls.getIRI().toString());
			String nSpace = split[0];
			if (!nSpace.equalsIgnoreCase(namespace)) {
				continue;
			}
			
			if (split[1].equalsIgnoreCase(uri)) {
				NodeSet<OWLClass> subClasses = reasoner.getSubClasses(cls, false);
				for (Node<OWLClass> subClsNode : subClasses) {
					Iterator<OWLClass> iter = subClsNode.iterator();
					while (iter.hasNext()) {
						OWLClass subCls = iter.next();
						String shortURI = GeneralUtil.stripURI(subCls.getIRI().toString());
						excludesIndividuals.put(shortURI, "");
					}
				}
				break;
			}
		}
	}

	private void addAllSubclassesOfToIncludes(String uri, OWLOntology ontology,
			OWLReasoner reasoner) {
		for (OWLClass cls : ontology.getClassesInSignature()) {
			String[] split = GeneralUtil.splitURI(cls.getIRI().toString());
			String nSpace = split[0];
			if (!nSpace.equalsIgnoreCase(namespace)) {
				continue;
			}
			
			if (split[1].equalsIgnoreCase(uri)) {
				NodeSet<OWLClass> subClasses = reasoner.getSubClasses(cls, false);
				for (Node<OWLClass> subClsNode : subClasses) {
					Iterator<OWLClass> iter = subClsNode.iterator();
					while (iter.hasNext()) {
						OWLClass subCls = iter.next();
						String shortURI = GeneralUtil.stripURI(subCls.getIRI().toString());
						if (!excludesIndividuals.containsKey(shortURI) && !prefixExcluded(shortURI)) {
							includes.put(shortURI, "");
						}
					}
				}
				break;
			}
		}
	}

	public boolean accepts(String uri) {
		if (!excludesIndividuals.containsKey(uri) && !prefixExcluded(uri)) {
			return includes.containsKey(uri);
		}
		return false;
	}
}
