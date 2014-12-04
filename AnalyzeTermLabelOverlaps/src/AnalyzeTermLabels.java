import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class AnalyzeTermLabels {

	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		/* TODO Loop through entire import closure and get term IRIs and 
		labels for all terms to put in HashMap<String, String>. Then review
		HashMap for any labels that overlap
		*/
		
		System.out.println("Trying to load ontology...");
		OWLOntology ontology = loadOntologyFile();
		//System.out.println(ontology);
		HashMap<String, String> allTermLabelAndIRIMap = getTermLabelsAndIRIs(ontology);
		System.out.println("Size of All Class Labels/IRIs: "+allTermLabelAndIRIMap.size());
		checkForDuplicateTermLabels(allTermLabelAndIRIMap);
		
	}
	
	/** 
	 * Load Ontology file (the one to be removed from the import)
	 * @return 
	 * 
	 * @throws OWLOntologyCreationException 
	 **/
	@Test
	public static OWLOntology loadOntologyFile() throws OWLOntologyCreationException {
		// Load ontology from web
		//IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl"); // IRIs not updated, use with NIF-GO-CC-Bridge.owl only!
		IRI ONTOLOGY = IRI.create("http://ontology.neuinfo.org/NIF/nif.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(ONTOLOGY); 
		
		System.out.println("Loaded ontology: " + ontology);
		return ontology;
	}
	
	
	/**
	 * Get all Classes and Labels from each ontology in the import closure
	 * @param ontology
	 * @return 
	 */
	private static HashMap<String, String> getTermLabelsAndIRIs(OWLOntology ontology) {
		HashMap<String, String> termLabelIRIMap = new HashMap<String, String>();
		
		/*
		 * Get all terms and IRIs from ontology
		 */
		Set<OWLClass> allClasses = ontology.getClassesInSignature();
		int size = allClasses.size();
		System.out.println("Total number of classes: "+size+"\n from ontology: "+ontology+"\n");
		
		// Prepare to get term label for each class 
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLDataFactory ontologyDF = ontologyManager.getOWLDataFactory();
		OWLAnnotationProperty ontologyTermLabel = ontologyDF
                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		
		for (OWLClass term : allClasses) {
			term.getAnnotations(ontology, ontologyTermLabel);
			String termLabel = term.getAnnotations(ontology, ontologyTermLabel).toString();
			String termIRI = term.getIRI().toString();
			
			//System.out.println("Term Label: "+termLabel+" Term IRI: "+termIRI);
			// Add term IRI as key and label as value to HashMap
			termLabelIRIMap.put(termIRI, termLabel);
		}
		
		
		 /*
		  * Get all terms and IRIs from ontology import closure
		  */
	    for (OWLOntology importedOntology : ontology.getImports()) {  //previously used getImportsClosure() but timed out
	        System.out.println("Imports: "+importedOntology.getOntologyID().getOntologyIRI());
	        
	        // Add classes from imported ontology to variable to store all classes, might not be needed with hashmap now
	        Set<OWLClass> importAllClasses = importedOntology.getClassesInSignature();
			/*allClasses.addAll(importedOntology.getClassesInSignature());
	        size = allClasses.size();
			System.out.println("Imported ontology class size ("+importAllClasses.size()+")"
					+"\n Total number of classes: "+size
					+"\n from ontology: "+importedOntology+"\n");
			*/
			// Prepare to get term label for each class 
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	        OWLDataFactory df = man.getOWLDataFactory();
			OWLAnnotationProperty label = df
	                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			
			for (OWLClass term : importAllClasses) {
				term.getAnnotations(importedOntology, label);
				String importedTermLabel = term.getAnnotations(importedOntology, label).toString();
				String importedTermIRI = term.getIRI().toString();
				
				//System.out.println("Term label from Imported Ontology: "+importedTermLabel+" IRI: "+importedTermIRI);
				// Add term IRI as key and label as value to HashMap
				termLabelIRIMap.put(importedTermIRI, importedTermLabel);
				}	 
			System.out.println();
	    }
		return termLabelIRIMap;
	}
	
	/**
	 * Check for duplicate term labels
	 * @throws IOException 
	 */
	private static void checkForDuplicateTermLabels(
			HashMap<String, String> allTermLabelAndIRIMap) throws IOException {
		File file = new File("./sameTermLabels_12032014.txt");
		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		
		//http://stackoverflow.com/questions/12710494/java-how-to-get-set-of-keys-having-same-value-in-hashmap
		Multimap<String, String> multiMap = HashMultimap.create();
		for (Entry<String, String> entry : allTermLabelAndIRIMap.entrySet()) {
			//Label is now the key and all term IRIs with that term label are the value in multiMap
			multiMap.put(entry.getValue(), entry.getKey());
		}
		
		int count = 0;
		for (Entry<String, Collection<String>> entry : multiMap.asMap().entrySet()) {
			//System.out.println("Original synonym value: " + entry.getKey() + " was mapped to keys: " + entry.getValue()); //Prints all items in multiMap 
			if (entry.getValue().size() > 1) {
				count++;
				System.out.println("Duplicate Labels ("+entry.getKey()+") for IRIs: "+entry.getValue()
						+"\n count: "+count); 
				bw.write("Duplicate Labels for IRIs:\t"+entry.getKey()+"\t"+entry.getValue()+"\n");
			}
		}
			System.out.println("Total Count of duplicate term labels: "+count);
			bw.close();
	}
	

}
