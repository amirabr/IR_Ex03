package com.tutorialspoint.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

/**
 * Runs a single experiment on the given query set.
 * 
 * @author amir
 *
 */
public class Experiment {
	
	private String queryFile; 				// queryFile from the parameterFile
	private String docsFile; 				// docsFile from the parameterFile
	private String indexDir; 				// Where to store the index
	private boolean isBasic; 				// Basic/Advanced configuration
	
	private Indexer indexer; 				// Creates and manages the index
	private Searcher searcher; 				// Searches the index
	private Benchmark benchmark; 			// Calculates statistics
    private PrintWriter outputStream; 		// Prints log

	/**
	 * Constructor.
	 * 
	 * @param queryFile
	 * @param docsFile
	 * @param truthFile
	 * @param outputFile
	 * @param retrievalAlgorithm
	 * @throws IOException
	 */
	public Experiment(String queryFile, String docsFile, String truthFile, String outputFile, String retrievalAlgorithm) throws IOException {
		
		// Read configuration
		this.queryFile = queryFile;
		this.docsFile = docsFile;
		indexDir = "_index";
		isBasic = retrievalAlgorithm.equalsIgnoreCase("basic") ? true : false;
		
		// Initialize output writer
		outputStream = new PrintWriter(new FileWriter(outputFile));
		
		// initialize benchmark object if given the truth file
		if (!truthFile.equals("")) {
			benchmark = new Benchmark(truthFile);
		}
		
	}
	
	/**
	 * Creates a new index.
	 * 
	 * @throws IOException
	 */
	private void createIndex(Analyzer analyzer) throws IOException {

		int numIndexed;
		
		System.out.println("Starting index...");
		
		// Create the index
		indexer = new Indexer(indexDir, analyzer, isBasic);
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
	   
	   // If the old index doesn't exist, return
	   File index = new File(indexDir);
	   if (!index.exists() || !index.isDirectory()) {
		   return;
	   }
	   
	   System.out.println("Deleting old index..."); 

	   // List all the old index files
	   File[] files = index.listFiles();
      
	   // Iterate over the files and delete them
	   for (File file : files) {
		   if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
//			   System.out.println("\tDeleting: " + file.getAbsolutePath());
			   file.delete();
			   numDeleted++;
		   }
	   }
      
	   System.out.println(numDeleted + " files successfully deleted.");

   }
	
	/**
	 * Searches the index for the given query,
	 * using the given analyzer.
	 * 
	 * @param id
	 * @param searchQuery
	 * @param analyzer
	 * @throws IOException
	 * @throws ParseException
	 */
	private void search(String id, String searchQuery, Analyzer analyzer) throws IOException, ParseException {
		
		System.out.print("\nExecuting queryID #" + id + " \"");
		System.out.print(searchQuery.substring(0, Math.min(searchQuery.length(), LuceneConstants.QUERY_PREVIEW)));
		System.out.println("...\"");
		
		// Initialize the searcher
		searcher = new Searcher(indexDir, analyzer, isBasic);
	      
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
//				System.out.print("\t+ Rank: " + rank);
//				System.out.print(" | docID: " + docID);
//				System.out.println(" | Score: " + scoreDoc.score);
				outputStream.printf("q%s,doc%s,%d\n", id, docID, rank);
				rank++;
				
			}
		
		}
		
        // If benchmarking was enabled, calculate the AP
		if (benchmark != null) {
			benchmark.analyzeQuery(id, hits, searcher);
			benchmark.printQueryStatistics(id);
		}
		
		// Close the searcher
		searcher.close();
		
	}
	
	/**
	 * Reads the query file and executes the queries one-by-one,
	 * using the given analyzer.
	 * 
	 * @param analyzer
	 * @throws IOException
	 * @throws ParseException
	 */
	private void readQueries(Analyzer analyzer) throws IOException, ParseException {
		
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
	    		search(id, query.toString(), analyzer);
	            
	        }
	        
	        // If benchmarking was enabled, calculate the MAP
	        if (benchmark != null) {
	        	System.out.println("\nMAP = " + benchmark.calculateMAP());
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
	
	/**
	 * Runs a single retrieval experiment.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public void run() throws IOException, ParseException {
		
		// Delete the old index
		deleteIndex();
		
		// Create a new index with a SimpleAnalyzer
		createIndex(new SimpleAnalyzer(Version.LUCENE_36));		
		
		// Fetch the top 20 repeating terms in the collection
		Set<String> top20terms = indexer.getTop20Terms();
		
		// Delete the old index, again :(
		deleteIndex();
		
		// Let the user choose the analyzer type (basic/advanced)
		Analyzer analyzer;
		if (isBasic) {
			analyzer = new StopAnalyzer(Version.LUCENE_36, top20terms);
			System.out.println("\n*** Using Basic Analyzer *** \n");
		} else {
//			analyzer = new StopAnalyzer(Version.LUCENE_36, top20terms);
//			analyzer = new StandardAnalyzer(Version.LUCENE_36, top20terms);
			analyzer = new AdvancedAnalyzer(top20terms);
			System.out.println("\n*** Using Advanced Analyzer *** \n");
		}
		
		// Create a new index with a basic/advanced analyzer
		createIndex(analyzer);
		
		// Execute the queries
		readQueries(analyzer);
		
	}

}
