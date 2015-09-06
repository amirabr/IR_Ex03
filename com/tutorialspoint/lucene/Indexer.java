package com.tutorialspoint.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Creates and manages the index.
 * 
 * @author amir
 *
 */
public class Indexer {

	private Directory indexDirectory; 		// The index directory
	private IndexWriter writer; 			// Creates and maintains an index
	private boolean isBasic; 				// Basic/Advanced mode
	
	/**
	 * Constructor.
	 * 
	 * @param indexDirectoryPath
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public Indexer(String indexDirectoryPath, Analyzer analyzer, boolean isBasic) throws IOException {

		this.isBasic = isBasic;
		
		// Open the directory where the index is saved
		indexDirectory = FSDirectory.open(new File(indexDirectoryPath));

		// Initialize the index writer
		writer = new IndexWriter(indexDirectory,
								 analyzer,
								 true,
								 IndexWriter.MaxFieldLength.UNLIMITED);
		
	}

	/**
	 * Closes the index writer.
	 * 
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void close() throws CorruptIndexException, IOException {
		
		// Close the index writer
		writer.close();
		
	}

	/**
	 * Creates a new document from the given parameters and returns it.
	 * Basic configuration indexes the entire document in the "contents" field,
	 * Advanced configuration indexes the title and the body separately.
	 * 
	 * @param docID
	 * @param contents
	 * @return
	 */
	private Document createDocument(String docID, String contents) {
		
		// Initialize the document object
		Document document = new Document();

		if (isBasic) {
		
			// Define the 'contents' field
			Field contentsField = new Field(LuceneConstants.CONTENTS,
									        new StringReader(contents));
			
			// Add the fields to the document
			document.add(contentsField);

		} else {
			
			int firstDot = contents.indexOf('.');
			String title = contents.substring(0, firstDot);
			String body = contents.substring(firstDot+1);
			
			// Define the 'title' field
			Field titleField = new Field(LuceneConstants.TITLE,
								         new StringReader(title));
			
			// Define the 'body' field
			Field bodyField = new Field(LuceneConstants.BODY,
								        new StringReader(body));
			
			// Boost the title field
			titleField.setBoost(0.5f);
						
			// Add the fields to the document
			document.add(titleField);
			document.add(bodyField);
			
		}
		
		// Define the 'docID' field
		Field docIDField = new Field(LuceneConstants.DOCID,
							   docID,
							   Field.Store.YES,
							   Field.Index.NOT_ANALYZED);

		// Add the field to the document
		document.add(docIDField);

		// Return it
		return document;
		
	}   

	/**
	 * Indexes the document.
	 * It first calls createDocument() with the given parameters to create the document,
	 * and then adds it to the index using the index writer.
	 * 
	 * @param docID
	 * @param contents
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	private void indexDocument(String docID, String contents) throws CorruptIndexException, IOException {
		
//		System.out.println("\tIndexing docID #" + docID + "...");
		Document document = createDocument(docID, contents);
		writer.addDocument(document);
		
	}

	/**
	 * Creates the search engine's index, from the given docs file.
	 * Currently, it only indexes 2 fields: the docID and the contents of the document.
	 * 
	 * @param docsPath
	 * @return
	 * @throws IOException
	 */
	public int createIndex(String docsPath) throws IOException {

		BufferedReader inputStream = null;
		StringBuilder contents;
		String id;
		
		try {
			
			// Initialize the input stream from the docs file
			inputStream = new BufferedReader(new FileReader(docsPath));
			
			// Start reading the docs file
			char space = ' ';
			String line = inputStream.readLine();
	        while (line != null) {
	        	
	    		// Extract the ID
	    		id = line.substring(3);
	        		
	    		// Read the next line, which is ".W"
	    		line = inputStream.readLine();
	    		
	    		// Read the data
	    		contents = new StringBuilder();
	    		while ((line = inputStream.readLine()) != null && !line.startsWith(".I ")) {
	    			contents.append(line);
	    			contents.append(space);
	        	}
	    		
	    		// Create the document
	    		indexDocument(id, contents.toString());
	            
	        }
        
		} finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

		return writer.numDocs();
		
	}
	
	/**
	 * Returns the top 20 terms from the collection.
	 * 
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public Set<String> getTop20Terms() throws CorruptIndexException, IOException {
		
		IndexReader reader;
		Map<String, Integer> unsortedFrequencyMap;
		Map<String, Integer> sortedFrequencyMap;
		Set<String> top20;
		int termFrequency;
		String termText;
		
		unsortedFrequencyMap = new HashMap<String, Integer>();
		reader = IndexReader.open(indexDirectory);
		termFrequency = 0;
		termText = "";
		
		// Iterate over all the terms in the collection
		TermEnum terms = reader.terms();
		while (terms.next()) {
			
			// Grab the term
			Term term = terms.term();
			termText = term.text();
			
			// Iterate over all <document, frequency> pairs for that term and sum up the frequencies
			TermDocs td = reader.termDocs(term);
			while (td.next()) {
				termFrequency += td.freq();
			}
			td.close();
			
			// Insert the <term, frequency> pair to the unsorted dictionary
			unsortedFrequencyMap.put(termText, termFrequency);
			
			// Reset counter
			termFrequency = 0;

		}
		terms.close();
		reader.close();
		
		// Sort the frequency dictionary
		sortedFrequencyMap = sortByComparator(unsortedFrequencyMap);

		// Add the top 20 entries to the return set
		top20 = new HashSet<String>();
		int counter = 0;
		System.out.println("Top 20 terms are:");
		for (Map.Entry<String, Integer> entry : sortedFrequencyMap.entrySet()) {
			System.out.println("\t" + entry.getKey() + " - " + entry.getValue());
			top20.add(entry.getKey());
			if(++counter == 20) {
				break;
			}
		}
		
		return top20;
		
	}
	
	/**
	 * Helper method for getTop20Terms().
	 * Sorts the map by values, instead of keys, by descending order.
	 * Taken from: http://www.mkyong.com/java/how-to-sort-a-map-in-java/
	 * 
	 * @param unsortMap
	 * @return
	 */
	private Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		return sortedMap;
		
	}
	
}