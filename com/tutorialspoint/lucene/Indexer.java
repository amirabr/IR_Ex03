package com.tutorialspoint.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {

	private IndexWriter writer;
	
	/**
	 * Constructor.
	 * 
	 * @param indexDirectoryPath
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public Indexer(String indexDirectoryPath) throws IOException {

		// Open the directory where the index is saved
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));

		// Initialize the index writer
		writer = new IndexWriter(indexDirectory,
								 new StandardAnalyzer(Version.LUCENE_36),
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

	private Document createDocument(String docID, StringReader contents) {
      
		Document document;
		Field docIDField;
		Field contentField;
		
		// Initialize the document object
		document = new Document();

		// Define the 'content' field
		contentField = new Field(LuceneConstants.CONTENTS,
								 contents);

		// Define the 'docID' field
		docIDField = new Field(LuceneConstants.DOCID,
							   docID,
							   Field.Store.YES,
							   Field.Index.NOT_ANALYZED);

		// Add the fields to the document
		document.add(contentField);
		document.add(docIDField);

		// Return it
		return document;
		
	}   

	private void indexDocument(String docID, StringReader contents) throws CorruptIndexException, IOException {
		
		System.out.println("Indexing docID #" + docID);
		Document document = createDocument(docID, contents);
		writer.addDocument(document);
		
	}

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
	    		indexDocument(id, new StringReader(contents.toString()));
	            
	        }
        
		} finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

		return writer.numDocs();
		
	}
	
}