package com.tutorialspoint.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneTester {

	String indexDir = "/Users/amir/Desktop/lucene/Index";
	String dataDir = "/Users/amir/Desktop/lucene/Data";
//	String docsPath = "/Users/amir/Desktop/lucene/Data/records.txt";
	String docsPath = "/Users/amir/Desktop/docs.txt";
	
	Indexer indexer;
	Searcher searcher;

	public static void main(String[] args) {
		
		LuceneTester tester;
		
		try {
			
			tester = new LuceneTester();
			tester.deleteIndex();
			tester.createIndex();
//			tester.search("Mohan");
//			tester.search("jan");
//			tester.search("1");
			tester.search("the crystalline lens in vertebrates, including humans.");
			
			System.out.println("DONE");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Searches the index for the given query.
	 * 
	 * @param searchQuery
	 * @throws IOException
	 * @throws ParseException
	 */
	private void search(String searchQuery) throws IOException, ParseException {
		
		System.out.println("Searching for \"" + searchQuery + "\"...");
		
		// Initialize the searcher
		searcher = new Searcher(indexDir);
	      
		// Execute the query
		TopDocs hits = searcher.search(searchQuery);
	   
		System.out.println(hits.totalHits + " documents found:");
		
		// Print the search results
		int rank = 1;
		for(ScoreDoc scoreDoc : hits.scoreDocs) {

			Document doc = searcher.getDocument(scoreDoc);
			System.out.print("\t+ Rank: " + rank);
			System.out.print(" | docID: " + doc.get(LuceneConstants.DOCID));
			System.out.println(" | Score: " + scoreDoc.score);
			rank++;
			
		}
		
		// Close the searcher
		searcher.close();
		
	}

	/**
	 * Creates a new index.
	 * 
	 * @throws IOException
	 */
	private void createIndex() throws IOException {

		int numIndexed;
		
		System.out.println("Starting index:");
		
		// Create the index
		indexer = new Indexer(indexDir);
		numIndexed = indexer.createIndex(docsPath);
		indexer.close();
		
		System.out.println(numIndexed + " files successfully indexed.");
		
   }
   
	/**
	 * Deletes the old index.
	 * 
	 */
   private void deleteIndex() {
	   
	   int numDeleted = 0;
	   
	   System.out.println("Deleting old index:"); 

	   // List all the old index files
	   File[] files = new File(indexDir).listFiles();
      
	   // Iterate over the files and delete them
	   for (File file : files) {
		   if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
			   System.out.println("\tDeleting: " + file.getAbsolutePath());
			   file.delete();
			   numDeleted++;
		   }
	   }
      
	   System.out.println(numDeleted + " files successfully deleted.");

   }
   
}