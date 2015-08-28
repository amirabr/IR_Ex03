package com.tutorialspoint.lucene;

public class myQuery {

	public double[] precisionAt;
	public double averagePrecision;
	
	public myQuery() {
		
		// Note: This array starts from 1 and goes up to LuceneConstants.MAX_SEARCH 
		precisionAt = new double[LuceneConstants.MAX_SEARCH + 1];
		averagePrecision = 0;
	}
}
