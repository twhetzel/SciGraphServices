package com.neuinfo.same;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.neuinfo.same.QueryOntoquest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/*
 * Find term names that are the same across different ontologies 
 * and find term names that are also the same as synonyms of another term
 */
public class ReviewSameLabels {

	private static final List String = null;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HashMap<java.lang.String, List<java.lang.String>> lines = getTerms();
		//reviewSamePrefLabels();  //Can this be done with OQ since there is no notion of ontology only ID?
		HashMap<String, String> itemsToCheckForDuplicates = reviewSamePrefLabelSynonym(lines);
		checkForDuplicateSynonyms(itemsToCheckForDuplicates);
	}

	
	/*
	 * Query Ontoquest to get all rows that have synonyms 
	 */
	private static HashMap<String, List<String>> getTerms() {
		// Get list of terms from Ontoquest
		HashMap<String, List<String>> resources = new HashMap();
		
		try {
			System.out.println("\n<-- Querying Ontoquest -->");
			// Query DISCO user database for these columns, 
			// nt.term||'^^'||rtid||'^^'||rid as pk, nt.term, nt.tid, nt.synonyms
			resources = QueryOntoquest.queryOQ(); 
		} 
			catch (SQLException e) {
			e.printStackTrace();
		}
			return resources;
	}

	
	private static void reviewSamePrefLabels() {
		// Given list of terms, check if any labels are the same across 2 or more concepts		
	}


	/*
	 * Given data from OQ, get all synonyms and unique identifier to check if any synonyms 
	 * are repeated for different unique identifiers/terms(term||'^^'||rtid||'^^'||rid||'^^'||tid as pk)  
	 */
	private static HashMap<String, String> reviewSamePrefLabelSynonym(HashMap<String, List<String>> lines) {
		Iterator it = lines.entrySet().iterator();
		HashMap <String, String> listToReview = new HashMap<String, String>();
		
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.print("...");
	        //System.out.println("KEY: "+pairs.getKey() + " \nVALUE: " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	        //Order of data: nt.term||'^^'||rtid||'^^'||rid as pk, nt.term, nt.tid, nt.synonyms
	        String key = (String) pairs.getKey(); //Primary key combination (nt.term||'^^'||rtid||'^^'||rid) 
	        ArrayList values = (ArrayList) pairs.getValue(); //All other columns (nt.term, nt.tid, nt.synonyms)  
	        
	        String syn = (String) values.get(2); //Based on query, synonyms are index=2  
	        String[] synonyms = syn.split(","); //Each row may contain >1 synonym in a comma-separated list
	        int synLength = synonyms.length;
	        for (int j= 0; j < synLength; j++) {
	        	//System.out.println("syn-values: "+synonyms[j]);
	        	//TODO Might need to append synonym to key to make key unique for HashMap and not overwrite values 
	        	listToReview.put(key, synonyms[j]); //Populate HashMap with key and synonym values to review, 1 synonym per key 
	        }
	    }
	    System.out.println("\n");
	    return listToReview;
	}
	
	
	
	/*
	 * Check for duplicate synonym values across different terms
	 */
	private static void checkForDuplicateSynonyms(HashMap<String, String> itemsToCheckForDuplicates) throws IOException {
		//System.out.println("Original map: " + itemsToCheckForDuplicates);
		System.out.println("Size of itemsToCheckForDuplicates: "+itemsToCheckForDuplicates.size());
		
		File file = new File("/Users/whetzel/git/OntologyQA/QA-SameLabelsSynonyms/sameSynonyms_11172014.txt");
		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("SYNONYM\tKEY\n"); //Write file column headers 
		
		//http://stackoverflow.com/questions/12710494/java-how-to-get-set-of-keys-having-same-value-in-hashmap
		// Data: term||'^^'||rtid||'^^'||rid||'^^'||tid as db KEY, 1 synonym as VALUE
		Multimap<String, String> multiMap = HashMultimap.create();
		for (Entry<String, String> entry : itemsToCheckForDuplicates.entrySet()) {
			//Synonym value is now the key and all primary key combinations with that synonym are the value in multiMap
			multiMap.put(entry.getValue(), entry.getKey());
		}
		
		for (Entry<String, Collection<String>> entry : multiMap.asMap().entrySet()) {
		  //System.out.println("Original value: " + entry.getKey() + " was mapped to keys: " + entry.getValue()); //Prints all items in multiMap 
		  if (entry.getValue().size() > 1) { //Print lines that have >1 value (db KEY) mapped to the same synonym 
			  //System.out.println("ISSUE: This synonym is mapped to multiple keys");
			  System.out.println("Original value: \'" + entry.getKey() + "\' was mapped to keys: "
				      + entry.getValue()+"\n");		  
			  bw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		  }
		}
		bw.close();
	}
	
}
