package com.tutorialspoint.lucene;

/**
 * Represents a single query, for statistic purposes.
 * Holds a prec@k array and the AP for the query.
 * 
 * @author amir
 *
 */
public class myQuery {

	public double[] precisionAt; 		// prec@k array
	public double averagePrecision; 	// AP
	
	public myQuery() {
		
		// Note: This array starts from 1 and goes up to LuceneConstants.MAX_SEARCH 
		precisionAt = new double[LuceneConstants.MAX_SEARCH + 1];
		averagePrecision = 0;
		
	}
	
}
