package com.tutorialspoint.lucene;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * My attempt at a better analyzer.
 * 
 * @author amir
 *
 */
public class AdvancedAnalyzer  extends Analyzer {
	
	Set<String> stopList; 		// The stop-list
	
	/**
	 * Constructor.
	 * 
	 * @param stopList
	 */
	public AdvancedAnalyzer(Set<String> stopList) {
		
		this.stopList = stopList;
		
	}
	
	/**
	 * Create a chain of tokenizers and filters to create a token stream.
	 * 
	 */
	public TokenStream tokenStream(String fieldName, Reader reader) {

	    StandardTokenizer tokenStream = new StandardTokenizer(Version.LUCENE_36, reader);
	    
	    TokenStream result = new StandardFilter(Version.LUCENE_36, tokenStream);

	    result = new LowerCaseFilter(Version.LUCENE_36, result);

	    result = new StopFilter(Version.LUCENE_36, result, stopList);
	    
	    result = new PorterStemFilter(result);

	    return result;

	  }

}
