package com.tutorialspoint.lucene;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;

public class Main {

	public static void main(String[] args) {

		String queryFile 	= "/Users/amir/Desktop/lucene/Parameters/queries.txt";
		String docsFile 	= "/Users/amir/Desktop/lucene/Parameters/docs.txt";
		String truthFile 	= "/Users/amir/Desktop/lucene/Parameters/truth.txt";
		String outputFile 	= "/Users/amir/Desktop/lucene/Parameters/output.txt";
		String retrievalAlgorithm = "Basic";
		
		try {
			
			Experiment experiment = new Experiment(queryFile, docsFile, truthFile, outputFile, retrievalAlgorithm);
			experiment.run();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
