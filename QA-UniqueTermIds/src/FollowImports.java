/**
 * FollowImports.java within QA-UniqueTermIds
 * 
 * Purpose: Iterate through NIFSTD import closure and all classes in these ontologies
 * to get the term IRI, IRI fragment, and ontology document IRI to identify IF an
 * IRI fragment is used more than once with a different ontology URL prefix. This is a
 * review step before globally updating all NIF ontology URL prefixes to http://uri.neuinfo.org/nif/nifstd/....
 * 
 * @author Trish Whetzel 
 * @date Tue Jul 15 21:20:20 PDT 2014 
 */


import java.io.File;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.coode.owlapi.examples.LoadOWLFile;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.SimpleIRIMapper;



public class FollowImports {
	public static void main(String[] args) throws IOException {
		try {
			// NOTE: Check that VM arguments (-DentityExpansionLimit=100000000 -Xmx2G) are added to Run Configuration
			System.out.println("Trying to load NIFSTD from Web location...");
			shouldUseIRIMappers();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}	
	}


	/** 
	 * Loads the ontology and follows "owl:import" statements 
	 * statements for ALL ontologies. 
	 * 
	 * @throws OWLOntologyCreationException 
	 * @throws IOException 
	 **/
	@Test
	public static void shouldUseIRIMappers() throws OWLOntologyCreationException, IOException {
		IRI NIFSTD_IRI = IRI.create("http://ontology.neuinfo.org/NIF/nif.owl");
		//IRI NIF_Resource = IRI.create("http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Resource.owl"); // Debug
		//IRI NIF_Investigation = IRI.create("http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Investigation.owl"); //Debug 
		
		// Create a manager to work with
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(NIFSTD_IRI);
		
		// Print out the ontology IRI and its imported ontology IRIs
		printOntologyAndImports(manager, ontology);
	}

	
	/**
	 * Print ontology URL and where it is loaded from
	 * Add term IRI fragment, term IRI, and document IRI to HashMap if unique, 
	 * otherwise writes message to file about duplicate values found
	 * 
	 * @param manager
	 * @param ontology
	 * @throws IOException 
	 */
	private static void printOntologyAndImports(OWLOntologyManager manager, OWLOntology ontology) throws IOException {
		System.out.println("\n** printOntologyAndImports method **");
		printOntology(manager, ontology);
		
		File file = new File("./data_files/duplicate-keys-nifstd.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
			
		Map<String,ArrayList<String>> mapAll = getAllClasses(manager, ontology);
		System.out.println("** Returned HashMap Size: "+mapAll.size());
		
		// List the imported ontologies, follows entire import chain for all ontologies
		for (OWLOntology importedOntology : ontology.getImportsClosure()) {  //previously used getImportsClosure() but timed out vs. getImports() 
			System.out.println("Imports: ");
			IRI documentIRI = manager.getOntologyDocumentIRI(importedOntology);
			bw.write("IMPORTS: "+importedOntology+"\nFROM: "+documentIRI.toQuotedString()+"\n");
			printOntology(manager, importedOntology);
			
			Map<String,ArrayList<String>> map = getAllClasses(manager, importedOntology); // map contains key=fragment and value=(term IRI, documentIRI)
			
			// Iterate through returned map 
			System.out.println("Analyzing returned map in ** printOntologyAndImports method **");
			for (Entry<String, ArrayList<String>> entry : map.entrySet()) {
				String key = entry.getKey();
			    ArrayList<String> value = entry.getValue();
			    //System.out.println("From new HashMap -> Key: "+key+" Value: "+value);
			    // If unique key, add to mapAll
			    if (!mapAll.containsKey(key)) {
			    	//System.out.println("Key is unique, adding to mapAll\n");
			    	mapAll.put(key, value);
			    }
			    else {
			    	// Since multiple ontologies can import the same ontology, this case can reasonably occur
			    	// Need to alert on the case below:
			    	// Key: birnlex_2 Value: http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBO-UBO.owl#birnlex_2
			    	// Duplicate Key birnlex_2 Value: http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Organism.owl#birnlex_2
			    	
			    	// Check if values are the same, if yes then there is no issue
			    	if (!mapAll.get(key).equals(value)) {
			    		System.out.println("Duplicate Found in existing HashMap -> Key: "+key+" Value: "+mapAll.get(key));
			    		System.out.println("From new HashMap -> Key: "+key+" Value: "+value);
			    		System.out.println("-- Values do not match\n");
			    		
			    		bw.write("\nDuplicate Found in existing HashMap\nFragment: "+key+" IRI: "+mapAll.get(key)+"\n");
				    	bw.write("Fragment: "+key+" IRI: "+value+"\n");
			    		bw.write("-- Values do not match\n\n"); 	    		
			    	}
			    }
			}
			System.out.println("** HashMapAll(Initial+Imports) Size: "+mapAll.size());
		}
		bw.close();
	}


	/** Prints the IRI of an ontology and its document IRI.
	 * 
	 * @param manager
	 * @param ontology  
	 **/
	private static void printOntology(OWLOntologyManager manager, OWLOntology ontology) {
		IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
		IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
		// Print ontology IRI and where it was loaded from (they will be the same)
		System.out.println("Loaded ontology: "+ontologyIRI == null ? "anonymous" : ontologyIRI
				.toQuotedString());
		System.out.println(" From: " + documentIRI.toQuotedString()+"\n");
	}
	
	
	/**
	 * Gets all classes in the ontology and 
	 * create a HashMap of the term Id (key) and URI, OntologyIRI (values) 
	 * @return 
	 */
	private static Map<String, ArrayList<String>> getAllClasses(OWLOntologyManager manager, OWLOntology ontology) {
		System.out.println("\n** getAllClasses method **"); //Debug  
		IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
		IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
		System.out.println("Gettting all classes for: "+ontologyIRI+" From: "+documentIRI);
	    
	 	Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
	 	// Test to confirm that existing Keys are found in HashMap
	 	/*ArrayList<String> seedStringValues = new ArrayList<String>();
	 	seedStringValues.add("http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBI-proxy.owl#birnlex_11012");
	 	seedStringValues.add("http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBI-proxy.owl");
	 	String seedIRIFragment = "birnlex_11012";
	 	map.put(seedIRIFragment, seedStringValues);
	 	*/
	 	
		//TODO Check that Bridge files are properly traversed to get all classes
	 	for (OWLClass c : ontology.getClassesInSignature()) {
	  		IRI iri = c.getIRI();
	  		String iriString = iri.toString(); // Value, e.g. http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Resource.owl#nlx_res_20090105 
	  		String fragment = c.getIRI().getFragment(); // Key, e.g. nlx_res_20090105
	  		//System.out.println("IRI: "+iriString+"\nFragment: "+fragment);
	  		
	  		// Add items to HashMap -> check if in NIF namespace (http://ontology.neuinfo.org/NIF/)
	  		String nifNamespace = "http://ontology.neuinfo.org/NIF/";
	  		if (iriString.contains(nifNamespace)) {
	  			//System.out.println("IRI is in NIFSTD namespace: "+iriString+"\n");
	  			// check if key exists in HashMap. This case should not exist within a single ontology since Protege prevents this
	  			if (!map.containsKey(fragment)) {
	  				ArrayList<String> iriValues = new ArrayList<String>();
	  				iriValues.add(iriString);
	  				iriValues.add(documentIRI.toString());
	  				map.put(fragment, iriValues);
	  			}
	  			else {
	  				System.err.println("Key exists: "+fragment);
	  				break;
	  			}
	  		}
		}
	 	System.out.println("** HashMap Size: "+map.size());
		return map;	
	}

	
}