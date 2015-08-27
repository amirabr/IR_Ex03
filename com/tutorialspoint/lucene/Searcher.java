package com.tutorialspoint.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

	IndexSearcher indexSearcher; 	// Implements search over an index
	QueryParser queryParser; 		// Parses a user-given query 
	Query query; 					// The parsed query, to be used by the index searcher

	/**
	 * Constructor.
	 * 
	 * @param indexDirectoryPath
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public Searcher(String indexDirectoryPath) throws IOException {
		
		// Open the directory where the index is saved
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));
		
		// Initialize the index searcher
		indexSearcher = new IndexSearcher(indexDirectory);
		
		// Initialize the query parser
		queryParser = new QueryParser(Version.LUCENE_36,
									  LuceneConstants.CONTENTS,
									  new StandardAnalyzer(Version.LUCENE_36));
		
	}

	/**
	 * Searches the index with the given query, returning the top search results.
	 * 
	 * @param searchQuery
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public TopDocs search(String searchQuery) throws IOException, ParseException {
      
		// Parse the query
		query = queryParser.parse(searchQuery);
		
		// Search the index
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
		
	}

	/**
	 * Retrieve a document from the search results.
	 * 
	 * @param scoreDoc
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		
		return indexSearcher.doc(scoreDoc.doc);
		
	}

	/**
	 * Closes the index searcher.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		
		// Close the index searcher
		indexSearcher.close();
		
	}
	
}