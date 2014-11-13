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


/*
 * Find term names that are the same across different ontologies 
 * and find term names that are also the same as synonyms of another term
 */
public class ReviewSameLabels {

	private static final List String = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<java.lang.String, List<java.lang.String>> lines = getTerms();
		//reviewSamePrefLabels();  //Can this be done with OQ since there is no notion of ontology only IDs?
		HashMap<String, String> itemsToCheckForDuplicates = reviewSamePrefLabelSynonym(lines);
		checkForDuplicateSynonyms(itemsToCheckForDuplicates);
	}

	/*
	 * Check for duplicate synonym values across different terms
	 */
	private static void checkForDuplicateSynonyms(
		HashMap<String, String> itemsToCheckForDuplicates) {
		//http://stackoverflow.com/questions/12710494/java-how-to-get-set-of-keys-having-same-value-in-hashmap
		//System.out.println("Original map: " + itemsToCheckForDuplicates);

		Multimap<String, String> multiMap = HashMultimap.create();
		for (Entry<String, String> entry : itemsToCheckForDuplicates.entrySet()) {
		  multiMap.put(entry.getValue(), entry.getKey());
		}
		System.out.println();

		for (Entry<String, Collection<String>> entry : multiMap.asMap().entrySet()) {
		  //System.out.println("Original value: " + entry.getKey() + " was mapped to keys: "
		  //    + entry.getValue());
		  if (entry.getValue().size() > 1) {
			  System.out.println("ISSUE: This synonym is mapped to multiple keys");
			  System.out.println("Original value: " + entry.getKey() + " was mapped to keys: "
				      + entry.getValue()+"\n");
		  }
		}	
	}


	private static HashMap<String, List<String>> getTerms() {
		// Get list of terms from somewhere (prefLabel, synonym(s), termId, and ontology
		
		HashMap<String, List<String>> resources = new HashMap();
		List<String> valueList = new ArrayList<String>();
		
		try {
			System.out.println("\n<-- Fetching Keys and corresponding Multiple Values -->");
			// Query DISCO user database
			//nt.term||'^^'||rtid||'^^'||rid as pk, nt.term, nt.tid, nt.synonyms
			resources = QueryOntoquest.queryOQ(); //Disable for testing w/o internet
			//System.out.println(resources);
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
	 * Given a list of terms, get all synonyms and check if any are repeated for any terms
	 */
	private static HashMap<String, String> reviewSamePrefLabelSynonym(HashMap<String, List<String>> lines) {
		Iterator it = lines.entrySet().iterator();
		HashMap <String, String> listToReview = new HashMap<String, String>();
		
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println("...");
	        //System.out.println("KEY: "+pairs.getKey() + " \nVALUE: " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	        String key = (String) pairs.getKey();
	        ArrayList values = (ArrayList) pairs.getValue();
	        
	        String syn = (String) values.get(2);
	        String[] synonyms = syn.split(",");
	        int synLength = synonyms.length;
	        for (int j= 0; j < synLength; j++) {
	        	//System.out.println("syn-values: "+synonyms[j]);
	        	listToReview.put(key, synonyms[j]);
	        }
	        //System.out.println("\n");
	    }
	    return listToReview;
	}
	
}
