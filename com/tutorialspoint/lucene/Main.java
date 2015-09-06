package com.tutorialspoint.lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;

/**
 * The main class. Everything starts here.
 * 
 * @author amir
 *
 */
public class Main {

	public static void main(String[] args) {
		
		String queryFile 			= "";
		String docsFile 			= "";
		String truthFile 			= "";
		String outputFile 			= "";
		String retrievalAlgorithm 	= "";
		
		// No parameterFile - use debug parameters
		if (args.length == 0) {
			
			System.out.println("Using debug parameters...");
			
			queryFile 	= "/Users/amir/Desktop/lucene/Parameters/queries.txt";
			docsFile 	= "/Users/amir/Desktop/lucene/Parameters/docs.txt";
			truthFile 	= "/Users/amir/Desktop/lucene/Parameters/truth.txt";
			outputFile 	= "/Users/amir/Desktop/lucene/Parameters/output.txt";
			retrievalAlgorithm = "Basic";
//			String retrievalAlgorithm = "Advanced";
			
		} else {
			
			BufferedReader inputStream = null;
			String parameterFile = args[0];

			try {
				
				System.out.println("Reading parameterFile '" + parameterFile + "'...");
				
	            inputStream = new BufferedReader(new FileReader(parameterFile));
	
	            // Read parameters from file (assume given order)
	            queryFile = getVal(inputStream);
	            System.out.println("\t- queryFile = " + queryFile);
	            docsFile = getVal(inputStream);
	            System.out.println("\t- docsFile = " + docsFile);
	            outputFile = getVal(inputStream);
	            System.out.println("\t- outputFile = " + outputFile);
	            retrievalAlgorithm = getVal(inputStream);
	            System.out.println("\t- retrievalAlgorithm = " + retrievalAlgorithm);
	            
	            System.out.println("The parameters have been read.");
	            
			} catch (IOException e) {
				System.out.println("Error: Reading parameterFile.txt");
			} finally {
	            if (inputStream != null) {
	                try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	            }
	        }
			
		}
		

		try {
			
			Experiment experiment = new Experiment(queryFile, docsFile, truthFile, outputFile, retrievalAlgorithm);
			experiment.run();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Reads a single parameter from the file and return its value.
	 * 
	 * @param br
	 * @return
	 * @throws IOException
	 */
	private static String getVal(BufferedReader br) throws IOException {
        
        String line = br.readLine();
    	String[] parts = line.split("=");
    	return parts[1];
		
	}

}
