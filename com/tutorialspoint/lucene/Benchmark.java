package com.tutorialspoint.lucene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Benchmark {
	
	private Truth truth; 					// Golden standard
	private Map<String,myQuery> queries; 	// Dictionary of queryID -> myQuery objects
											// In each myQuery we save precision@k array and the average precision
	
	/**
	 * Constructor.
	 * 
	 * @param truthFile
	 * @throws IOException
	 */
	public Benchmark(String truthFile) throws IOException {
		
		// Initialize the truth from the truth file
		truth = new Truth(truthFile);
		
		// Initialize the query dictionary
		queries = new HashMap<String,myQuery>();
		
	}
	
	/**
	 * Analyzes the query:
	 * 	- Calculates Precision@K for every K from 1 to LuceneConstants.MAX_SEARCH
	 * 	- Calculates the Average Precision
	 * 
	 * @param queryID
	 * @param results
	 * @param searcher
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void analyzeQuery(String queryID, TopDocs results, Searcher searcher) throws CorruptIndexException, IOException {
		
		// No duplicate queryIDs allowed
		if (queries.containsKey(queryID)) {
			return;
		}
		
		// Initialize a new myQuery object and save it in the dictionary
		myQuery query = new myQuery();
		queries.put(queryID, query);
		
		// If no results were found, leave it as is (all zeros)
		if (results.totalHits == 0) {
			return;
		}
		
		int kCounter = 0; 			// Counts the total number of documents
		int relevantCounter = 0; 	// Counts the number of relevant documents
		for(ScoreDoc scoreDoc : results.scoreDocs) {

			// Always increment the documents counter
			kCounter++;
			
			// If the document is relevant, increment the relevant documents counter,
			// and also the average precision (it only sums on relevant documents)
			Document doc = searcher.getDocument(scoreDoc);
			String docID = doc.get(LuceneConstants.DOCID);
			if (truth.isRelevant(queryID, docID)) {
				relevantCounter++;
				query.averagePrecision += relevantCounter*1.0 / kCounter;
			}
			
			query.precisionAt[kCounter] = relevantCounter*1.0 / kCounter;
			
		}
		
		// Take the average
		if (relevantCounter != 0) {
			query.averagePrecision /= relevantCounter;
		}
		
	}
	
	/**
	 * Prints the query statistics (prec@5, prec@10).
	 * 
	 * @param queryID
	 */
	public void printQueryStatistics(String queryID) {
		
		// If no such query exists, quit
		if (!queries.containsKey(queryID)) {
			return;
		}
		
		// Print the stats
		myQuery query = queries.get(queryID);
		System.out.println("Prec@5 = "  + query.precisionAt[5]);
		System.out.println("Prec@10 = " + query.precisionAt[10]);
		System.out.println("AP = "           + query.averagePrecision);
		
	}
	
	/**
	 * Calculate the MAP (Mean Average Precision).
	 * 
	 * @return
	 */
	public double calculateMAP() {
		
		double map = 0;
		for (myQuery query : queries.values()) {
			map += query.averagePrecision;
		}
		map /= queries.size();
		
		return map;
	}

}
