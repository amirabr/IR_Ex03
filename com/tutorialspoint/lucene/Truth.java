package com.tutorialspoint.lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Truth {

	private Map<String,ArrayList<String>> multiMap;
	
	public Truth(String truthPath) throws IOException {
		
		// Initialize the dictionary
		multiMap = new HashMap<String,ArrayList<String>>();
		
		// Load the truth file into the dictionary
		load(truthPath);
		
	}
	
	private void load(String truthPath) throws IOException {
		
		BufferedReader inputStream = null;
		
		try {
			
			System.out.println("Reading the truth:");
			
            inputStream = new BufferedReader(new FileReader(truthPath));

            String line;
            while ((line = inputStream.readLine()) != null) {
            	
            	// Extract queryID and docID from line
            	String[] parts = line.split("\\s");
            	String queryID = parts[0];
            	String docID = parts[2];
            	
            	System.out.printf("\t- docID %s is relevant to queryID %s\n", docID, queryID);
            	
            	// Add it to the dictionary
            	if (multiMap.containsKey(queryID)) {
            		
            		// queryID already exists in the dictionary, just append to it
            		ArrayList<String> list = multiMap.get(queryID);
            		list.add(docID);
            		
            	} else {
            		
            		// queryID is not in the dictionary, create a new list and add it
            		ArrayList<String> list = new ArrayList<String>();
            		list.add(docID);
            		multiMap.put(queryID, list);
            		
            	}
                
            }
            
            System.out.println("The truth has been read.");
            
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
		
	}
	
	/**
	 * Receives a queryID and a postings list from the experiment,
	 * and intersects it with the true postings list from the truth file.
	 * (the list is changed in-place)
	 *  
	 * @param queryID
	 * @param postings
	 * @return
	 */
	public boolean IntersectWithTruth(String queryID, ArrayList<String> postings) {
		
		// If no such queryID exists, exit
		if (!multiMap.containsKey(queryID)) {
			return false;
		}
		
		// Get the true postings list and intersect it
		ArrayList<String> TrueQueryRelevantPostings = multiMap.get(queryID);
		return postings.retainAll(TrueQueryRelevantPostings);
	}
	
	/**
	 * Checks if a given docID is truly relevant to a given queryID.
	 * 
	 * @param queryID
	 * @param docID
	 * @return
	 */
	public boolean isRelevant(String queryID, String docID) {
		
		// If no such queryID exists, exit
		if (!multiMap.containsKey(queryID)) {
			return false;
		} 
		
		// Get the true postings list and check it
		ArrayList<String> TrueQueryRelevantPostings = multiMap.get(queryID);
		return TrueQueryRelevantPostings.contains(docID);
		
	}
	
}
