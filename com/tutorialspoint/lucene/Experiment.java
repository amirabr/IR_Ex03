package com.tutorialspoint.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Experiment {
	
	private String queryFile;
	private String docsFile;
	private String indexDir;
	private String retrievalAlgorithm;
	
	private Indexer indexer;
	private Searcher searcher;
	private Truth truth;
    private PrintWriter outputStream;

	
	public Experiment(String queryFile, String docsFile, String truthFile, String outputFile, String retrievalAlgorithm) throws IOException {
		
		// Read configuration
		this.queryFile = queryFile;
		this.docsFile = docsFile;
		this.retrievalAlgorithm = retrievalAlgorithm;
		
		// Initialize output writer
		outputStream = new PrintWriter(new FileWriter(outputFile));
		
		// Calculate index directory (we'll put in the output file's folder)
		File file = new File(outputFile);
		indexDir = file.getParent() + "/_index";

		// initialize truth object if given the benchmark file
		if (!truthFile.equals("")) {
			truth = new Truth(truthFile);
		}
		
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
		numIndexed = indexer.createIndex(docsFile);
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
	
	/**
	 * Searches the index for the given query.
	 * 
	 * @param searchQuery
	 * @throws IOException
	 * @throws ParseException
	 */
	private void search(String id, String searchQuery) throws IOException, ParseException {
		
		System.out.print("Executing queryID #" + id + " \"");
		System.out.print(searchQuery.substring(0, Math.min(searchQuery.length(), LuceneConstants.QUERY_PREVIEW)));
		System.out.println("...\"");
		
		// Initialize the searcher
		searcher = new Searcher(indexDir);
	      
		// Execute the query
		TopDocs hits = searcher.search(searchQuery);
	   
		System.out.println(hits.totalHits + " documents found:");
		
		// If no hits were made, print 'dummy'
		if (hits.totalHits == 0) {
			
			System.out.println("\t+ dummy");
            outputStream.printf("q%s,dummy,1\n", id);
			
		} else {
		
			// Else, print the search results
			int rank = 1;
			for(ScoreDoc scoreDoc : hits.scoreDocs) {
	
				Document doc = searcher.getDocument(scoreDoc);
				String docID = doc.get(LuceneConstants.DOCID);
				System.out.print("\t+ Rank: " + rank);
				System.out.print(" | docID: " + docID);
				System.out.println(" | Score: " + scoreDoc.score);
				outputStream.printf("q%s,doc%s,%d\n", id, docID, rank);
				rank++;
				
			}
		
		}
		
		if (truth != null) {
			printPrecisionAtK(id, hits);
		}
		
		// Close the searcher
		searcher.close();
		
	}
	
	/**
	 * Reads the query file and executes the queries one-by-one.
	 *  
	 * @throws IOException
	 * @throws ParseException 
	 */
	private void readQueries() throws IOException, ParseException {
		
		BufferedReader inputStream = null;
		StringBuilder query;
		String id;
		
		try {
			
			// Initialize the input stream from the query file
			inputStream = new BufferedReader(new FileReader(queryFile));
			
			// Start reading the query file
			char space = ' ';
			String line = inputStream.readLine();
	        while (line != null) {
	        	
	    		// Extract the ID
	    		id = line.substring(3);
	        		
	    		// Read the next line, which is ".W"
	    		line = inputStream.readLine();
	    		
	    		// Read the data
	    		query = new StringBuilder();
	    		while ((line = inputStream.readLine()) != null && !line.startsWith(".I ")) {
	    			query.append(line);
	    			query.append(space);
	        	}
	    		
	    		// Search query
	    		search(id, query.toString());
	            
	        }
        
		} finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
		
	}
	
	private void printPrecisionAtK(String queryID, TopDocs hits) throws CorruptIndexException, IOException {

		int kCounter = 0;
		int relevantCounter = 0;
		for(ScoreDoc scoreDoc : hits.scoreDocs) {

			kCounter++;
			
			Document doc = searcher.getDocument(scoreDoc);
			String docID = doc.get(LuceneConstants.DOCID);
			if (truth.isRelevant(queryID, docID)) {
				relevantCounter++;
			}
			
			if (kCounter == 5 || kCounter == 10) {
				System.out.println("Prec@" + kCounter + " = " + relevantCounter*1.0 / kCounter);
			}
			
			if (kCounter == 10) {
				break;
			}
			
		}
		
	}
	
	/**
	 * Runs a single retrieval experiment.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public void run() throws IOException, ParseException {
		
		// Delete the old index
		deleteIndex();
		
		// Create the new index
		createIndex();
		
		// Execute the queries
		readQueries();
		
	}

}
