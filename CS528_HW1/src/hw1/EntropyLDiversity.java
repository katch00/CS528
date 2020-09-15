package hw1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
	
	private static String[] races = new String[] {"White", "Black", "Amer-Indian-Eskimo", "Asian-Pac-Islander", "Other"};
	private static String[] marital = new String[] {"Divorced", "Married-civ-spouse", "Never-married", "Separated", "Widowed", "Married-spouse-absent", "Married-AF-spouse"};
	private static String[] occupation = new String[] {"Tech-support", "Craft-repair", "Other-service", "Sales", "Exec-managerial", 
			"Prof-specialty", "Handlers-cleaners", "Machine-op-inspct", "Adm-clerical", "Farming-fishing", "Transport-moving", 
			"Priv-house-serv", "Protective-serv", "Armed-Forces"};
	
	public static void main(String[] args) throws IOException {	
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
		
		HelperMethods.initialSuppression(dataSet);
		
		// create map of key: education, value: array of tuples. Can be seen as 16 tables sorted by education level
		Map<String, ArrayList<String>> eduTableMap = HelperMethods.educationTables(dataSet);
		
		// create map of maps: Key: education value: age map, key: age, value: array of tuples
		// do this to have "tables" with relatively similar QIIDs
		Map<String, Map<String, ArrayList<String>>> ageTableMap = HelperMethods.ageTables(eduTableMap);
		
		// Check diversity for each education/age table (diversifies if not)
		for (Map.Entry<String, Map<String, ArrayList<String>>> val : ageTableMap.entrySet()) {
			Map<String, ArrayList<String>> eduTuples = val.getValue();
			
			for(Map.Entry<String, ArrayList<String>> val2 : eduTuples.entrySet()) { 
				 ArrayList<String> ageTuples = val2.getValue();
				 
				 boolean diverse = diversityCheck(ageTuples);
				 
				 
				 while(!diverse) {
					diversify(ageTuples, val2.getKey(), val.getKey());
					
					diverse = diversityCheck(ageTuples);
				 }
			}
		}
		
        // get all tables/arrays/tuples into one single array/table
		dataSet.clear();
		for (Map.Entry<String, Map<String, ArrayList<String>>> val : ageTableMap.entrySet()) {
			Map<String, ArrayList<String>> eduTuples = val.getValue();
			
			for(Map.Entry<String, ArrayList<String>> val2: eduTuples.entrySet()) {
				dataSet.addAll(val2.getValue());
			}
		}
		
		int k = HelperMethods.minFrequencies(dataSet);
		int kGenLevel = 1;
		while(k<K_VAL) {
			kGenLevel++;
			Kanon.generalizeData(dataSet, kGenLevel);
			
			k = HelperMethods.minFrequencies(dataSet);
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
	 * Method used to diversify the data
	 * 
	 * @param list
	 * @param age
	 * @param education
	 */
	public static void diversify(ArrayList<String> list, String age, String education) {
		Random rndm = new Random();
		for(int i = 0; i < 5; i++) {
			list.add(age + ",*,*," + education + ",*," + marital[rndm.nextInt(marital.length)] + ","+ occupation[rndm.nextInt(marital.length)]
					+ ",*," + races[rndm.nextInt(races.length)] + ",*,*,*,*,*,*");
		}
	}
	
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
}
