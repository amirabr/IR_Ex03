package com.tutorialspoint.lucene;

import java.util.Comparator;
import java.util.Map;

/**
 * Compares hashmap entries based on value, not key.
 * 
 * @author amir
 *
 */
@SuppressWarnings("rawtypes")
public class ValueComparator implements Comparator{

	Map<String, Integer> base;
	
	/**
	 * Constructor.
	 * 
	 * @param base
	 */
	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}
	
	/**
	 * Comapres two entries.
	 * 
	 */
	public int compare(Object a, Object b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}

}
