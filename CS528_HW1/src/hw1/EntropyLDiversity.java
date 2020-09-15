package hw1;

import hw1.Kanon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Entropy l-diversity algorithm
 * @author Natalia Stroupe
 * 
 * PROCESS:
 * get data in arrays, suppress initial values
 * Create "tables" based on education level
 * For each education level, make tables based on age
 * Map of Maps: Key: education level, Value: Map of Age Tables
 * Map of Ages: Key: age (suppressed one level for simplicity), Value: arrayList of tuples
 * 
 * Find Diversity of table:
 * Every Array In map:
 * 	- count total number of values in array
 *  - make Map of frequencies of SA
 *  - add sum of (Freq/total)log(freq/total) (negate sum)
 *  - must be larger than log(L)
 *  - if not l-diverse, diversify
 * Combine all tables back into one table
 * K-anonymize using kanon.java
 * print results to output.txt
 */

public class EntropyLDiversity {
	
	private static final int K_VAL = 5;
	private static final int L_VAL = 3;
	
	public static void main(String[] args) throws IOException {
		System.out.println("Hello World!");
		
		ArrayList<String> dataSet = new ArrayList<String>();
		
	    //data file
		String filename = "adult.data";
		File dataFile = new File(filename);
		Scanner fileSc = new Scanner(dataFile);
		
		//add each row from file to list of rows
		while(fileSc.hasNextLine()) {
			String line = fileSc.nextLine();
			
			dataSet.add(line);
		}
		fileSc.close();
		
		initialSuppression(dataSet);
		
		// create map of key: education, value: array of tuples. Can be seen as 16 tables sorted by education level
		Map<String, ArrayList<String>> eduTableMap = educationTables(dataSet);
		
		// create map of maps: Key: education value: age map, key: age, value: array of tuples
		// do this to have "tables" with relatively similar QIIDs
		Map<String, Map<String, ArrayList<String>>> ageTableMap = ageTables(eduTableMap);
		
		// sanity check
		int count = 0;
		
		// Check diversity for each education/age table (diversifies if not)
		for (Map.Entry<String, Map<String, ArrayList<String>>> val : ageTableMap.entrySet()) {
			Map<String, ArrayList<String>> eduTuples = val.getValue();
			
			for(Map.Entry<String, ArrayList<String>> val2 : eduTuples.entrySet()) { 
				 ArrayList<String> ageTuples = val2.getValue();
				 
				 boolean diverse = diversityCheck(ageTuples);
				 
				 // Print out if they are diverse or not (for my sanity, maybe yours too)
				 // only one was false, if you care to know
				 System.out.println(diverse);
				 count++;
			}
		}
		
		// ensure there are a decent amount of diversity checks
		System.out.println(count);
		
        // get all tables/arrays/tuples into one single array/table
		dataSet.clear();
		for (Map.Entry<String, Map<String, ArrayList<String>>> val : ageTableMap.entrySet()) {
			Map<String, ArrayList<String>> eduTuples = val.getValue();
			
			for(Map.Entry<String, ArrayList<String>> val2: eduTuples.entrySet()) {
				dataSet.addAll(val2.getValue());
			}
		}
		
		int k = Kanon.minFrequencies(dataSet);
		int kGenLevel = 1;
		while(k<K_VAL) {
			kGenLevel++;
			Kanon.generalizeData(dataSet, kGenLevel);
			
			
			k = Kanon.minFrequencies(dataSet);
		}
        
		// Write rows to output file
		FileWriter writer = new FileWriter("entropy-output.txt", false); 
		for(String str: dataSet) {
			writer.write(str + System.lineSeparator());
		}
		writer.close();
		System.out.println("Anonymization complete, please see entropy-output.txt. K: " + k + ". Gen-level: " + kGenLevel);
		
	}
	
	// METHODS
	
	/**
	 * Checks l-diversity of a table/array of tuples. If not l-diverse, diversifies the tuple using diversify()
	 * returns a boolean used for human checking
	 * 
	 * @param list
	 * @return
	 */
	public static boolean diversityCheck(ArrayList<String> list) {
		boolean isDiverse = false;
		
		// total number of values in "table"
		int numberOfTuples = list.size();
		
		// Map of frequencies
        Map<String, Integer> frequencies = new HashMap<String, Integer>(); 
        for (String i : list) { 
            Integer j = frequencies.get(i); 
            frequencies.put(i, (j == null) ? 1 : j + 1); 
        } 
        
        double sum = 0;
        
        // calculate entropy for table
        for (Map.Entry<String, Integer> val : frequencies.entrySet()) { 
            double fraction = val.getValue()/numberOfTuples;
            double logFrac = Math.log(fraction);
            double product = fraction*logFrac;
            
            sum += product;
        } 
        sum = sum*-1;
		
        // check if l-diversity satisfied
        if(sum<Math.log(L_VAL)) {
        	isDiverse = false;
        }else {
        	isDiverse = true;
        }
		
		return isDiverse;
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
}
