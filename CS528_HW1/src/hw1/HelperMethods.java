package hw1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Methods file, has methods used in more than one file to maintain code health.
 * Does not include generalizeData()
 * 
 * @author nstro 
 *
 */
public class HelperMethods {
	
	/**
	 * Suppresses unnecessary values in original data file as directed by instructions
	 * 
	 * @param rowsList
	 */
	public static void initialSuppression(ArrayList<String> rowsList) {
		for(int i=0; i < rowsList.size()-1; i++) {
			String row = rowsList.get(i);
			String[] thisRow = row.split(", ");
			
			String supp = "*";
			//Go through each attribute and suppress non QUIds
			//0 age: QIID
			if(thisRow[0].contains("?")) {
				thisRow[0] = supp;
			}
			//1 work class: suppress
			thisRow[1] = supp;
			//2 fnlwgt: suppress
			thisRow[2] = supp;
			//3 education: QIID
			if(thisRow[3].contains("?")) {
				thisRow[3] = supp;
			}
			//4 education-num: suppress
			thisRow[4] = supp;
			//5 marital-status: QIID
			if(thisRow[5].contains("?")) {
				thisRow[5] = supp;
			}
			//6 occupation: sensitive attribute, remains as ?
			//7 relationship: suppress
			thisRow[7] = supp;
			//8 race: QIID
			if(thisRow[8].contains("?")) {
				thisRow[8] = supp;
			}
			//9 sex: suppress
			thisRow[9] = supp;
			//10 capital-gain:
			thisRow[10] = supp;
			//11 capital-loss:
			thisRow[11] = supp;
			//12 hours-per-week: 
			thisRow[12] = supp;
			//13 native-country: 
			thisRow[13] = supp;
			//14 salary: prof said can be excluded, so suppressed
			thisRow[14] = supp;
			rowsList.set(i, String.join(",", thisRow));
		}
	}
	
	/**
	 * Separates Education level tables into tables sorted by age (suppression level 1).
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, Map<String, ArrayList<String>>> ageTables(Map<String, ArrayList<String>> map) {
		
		Map<String, Map<String, ArrayList<String>>> ageTuples = new HashMap<String, Map<String, ArrayList<String>>>();
		
		for (Map.Entry<String,  ArrayList<String>> val : map.entrySet()) {
        	ArrayList<String> eduTuples = val.getValue();
        	Kanon.generalizeData(eduTuples, 1);
        	String key = val.getKey();
        	
        	Map<String, ArrayList<String>> sortedArrays = new HashMap<String, ArrayList<String>>();
            for(int i = 0; i < eduTuples.size()-1; i++) {
            	String row = eduTuples.get(i);
    			String[] thisRow = row.split(","); // array of values in tuple
            	String ageKey = thisRow[0]; // key for hashmap
            	
            	ArrayList<String> tuples = new ArrayList<String>();
            	if(sortedArrays.containsKey(ageKey)) {
            		tuples.addAll(sortedArrays.get(ageKey));
            	}
            	
            	tuples.add(row);
            	
            	sortedArrays.put(ageKey, tuples);
            }
            
            ageTuples.put(key, sortedArrays);
        }
		
		return ageTuples;
	}

	/**
	 * Separates dataset into a Map, key:education level value: arraylist of tuples with that education level
	 * 
	 * @param list
	 * @return
	 */
	public static Map<String, ArrayList<String>> educationTables(ArrayList<String> list) {   
		
		Map<String, ArrayList<String>> sortedArrays = new HashMap<String, ArrayList<String>>();
        for(int i = 0; i < list.size()-1; i++) {
        	String row = list.get(i);
			String[] thisRow = row.split(","); // array of values in tuple
        	String key = thisRow[3]; // key for hashmap
        	
        	ArrayList<String> tuples = new ArrayList<String>();
        	if(sortedArrays.containsKey(key)) {
        		tuples.addAll(sortedArrays.get(key));
        	}
        	
        	tuples.add(row);
        	
        	
        	sortedArrays.put(key, tuples);
        }
        
        return sortedArrays;
    }
	
	/**
	 * Calculates how many times a tuple appears in a given table/list (used to check if k is met)
	 * 
	 * @param list
	 * @return
	 */
	public static int minFrequencies(ArrayList<String> list) 
    { 
        int min = 10000000;
        // we use a temp array to exclude the sensitive attribute from the frequency count
        ArrayList<String> temp = new ArrayList<String>(list);
        for(int i=0; i < temp.size(); i++) {
			String row = temp.get(i);
			String[] thisRow = row.split(",");
			//6 occupation: suppress this to calculate frequencies since it doesn't count against k
			thisRow[6] = "occupation";
			temp.set(i, String.join(",", thisRow));
		}
        
		// hashmap to store the frequency of element 
        Map<String, Integer> frequencies = new HashMap<String, Integer>(); 
  
        for (String i : temp) { 
            Integer j = frequencies.get(i); 
            frequencies.put(i, (j == null) ? 1 : j + 1); 
        } 
  
        for (Map.Entry<String, Integer> val : frequencies.entrySet()) { 
            if(val.getValue() < min) {
            	min = val.getValue();
            }
        } 
        
        return min;
    }

}
