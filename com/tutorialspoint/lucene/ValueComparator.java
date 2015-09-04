package com.tutorialspoint.lucene;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator{

	Map<String, Integer> base;
	
	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}
	
	public int compare(Object a, Object b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		}
	}

}
