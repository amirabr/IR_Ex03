package com.tutorialspoint.lucene;

/**
 * Project constants.
 * 
 * @author amir
 *
 */
public class LuceneConstants {
	
	public static final String CONTENTS="contents"; 	// contents field, for the entire document (basic)
	public static final String TITLE="title"; 			// title field, for the document's title (advanced)
	public static final String BODY="body"; 			// body field, for the document's body (advanced)
	public static final String DOCID="docID";			// docID field (both)
	public static final int MAX_SEARCH = 1000;			// AP will be calculated on the first MAX_SEARCH results
	public static final int QUERY_PREVIEW = 30; 		// Show first 30 characters of query (for debugging purposes)
	
}