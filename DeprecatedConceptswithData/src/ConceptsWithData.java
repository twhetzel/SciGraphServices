import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;


public class ConceptsWithData {

	/**
	 * Confirm that concepts that are deprecated/retired 
	 * do not return results from Federation queries. 
	 * 
	 * @param args Ontology location currently hard coded. 
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Trying to load ontology...");
			shouldUseIRIMappers();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}	
	}


	/** 
	 * Load the ontology  
	 * 
	 * @throws OWLOntologyCreationException 
	 **/
	@Test
	public static void shouldUseIRIMappers() throws OWLOntologyCreationException {
		// Load ontology from web
		//TODO Pass ontology location as a commandline argument 
		IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ONTOLOGY); 

		// Load ontology from local file
		/**File file = new File("/Users/whetzel/Desktop/NIF-Subcellular.owl");
        OWLOntology localOntology = manager.loadOntologyFromOntologyDocument(file);
		 */

		System.out.println("Loaded ontology: " + ontology);

		// Find retired/deprecated classes using two methods
		// Get all properties - annotation (interested in owl:deprecated) and then get axioms owl:deprecated is attached to
		getAllAnnotationProperties(manager, ontology); 
		// Get all subclasses of the _retired_class
		getAllDeprecatedClasses (manager, ontology);
		
		System.out.println("** Program Complete **");
	}


	/**
	 * Gets the OWL_DEPRECATED annotation property 
	 * and all axioms using this property
	 * 
	 * @param manager
	 * @param ontology
	 */
	private static void getAllAnnotationProperties (OWLOntologyManager manager, OWLOntology ontology) {
		for (OWLAnnotationProperty annProp : ontology.getAnnotationPropertiesInSignature()) {
			IRI dep = OWLRDFVocabulary.OWL_DEPRECATED.getIRI();
			if (annProp.getIRI().equals(dep)) {
				//System.out.println("DepProp: "+annProp.getIRI());

				// Get class property is attached to
				Set<OWLAxiom> depAxioms = annProp.getReferencingAxioms(ontology);
				//System.out.println("DepClass: "+depAxioms);
				printCollection(depAxioms);
			}
			else {
				//System.out.println("Not DepProp: "+annProp.getIRI());
			}
		}
	}


	/**
	 * Iterate through ontology and get all subclasses of "_retired_class"
	 * 
	 * @param manager
	 * @param ontology
	 * 
	 */
	private static void getAllDeprecatedClasses (OWLOntologyManager manager, OWLOntology ontology) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI iri = IRI.create("http://ontology.neuinfo.org/NIF/Backend/BIRNLex_annotation_properties.owl#_birnlex_retired_class");
		OWLClass retiredClass = factory.getOWLClass(iri);
		//System.out.println("RetiredClass: "+retiredClass);

		//gets all subclasses of a certain class
		Set<OWLClassExpression> allRetiredClasses = retiredClass.getSubClasses(ontology); 
		Iterator<OWLClassExpression> it = allRetiredClasses.iterator();
		while ( it.hasNext() ){
			OWLClassExpression axiom = it.next();
			//System.out.println("DeprecatedClasses: "+axiom);
		}		
	}


/**
 * Original method to print all deprecated Classes 
 * 
 * @param depAxioms
 */
	public static void printCollection(Set<OWLAxiom> depAxioms){
		Iterator<OWLAxiom> it = depAxioms.iterator();
		while ( it.hasNext() ){
			OWLAxiom axiom = it.next();
			if (axiom instanceof OWLAnnotationAssertionAxiom) {
				OWLAnnotationSubject subject = ((OWLAnnotationAssertionAxiom) axiom).getSubject();
				//System.out.println("DeprecatedClasses: "+axiom);

				// This assumes the URI has a hash
				String iri = ((IRI) subject).getFragment();
				//System.out.println("--Fragment: "+iri);

				// Add fragments to Array
				ArrayList<String> iriFragments = new ArrayList<String>();
				iriFragments.add(iri);
				
				// Call NIF Federation with term fragment or term label
				checkForData(iriFragments);
			}
		}
	}


	private static void checkForData(ArrayList<String> iriFragments) {
		System.out.println("FRAGMENTS: "+iriFragments);
		try {
			SearchDataFederation.runFederationSearch(iriFragments);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
