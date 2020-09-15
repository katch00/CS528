package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * (c,l)-Diversity Algorithm
 * @author nstro
 * 
 * Similar process to EntropyLDiversity.java, except diversityCheck() fits (c,l)-diversity needs
 * 
 * To find if (c,l)-Diverse:
 * 	Get Table
 * 	Put all sensitive attributes from table into an arrayList
 *  Find attribute frequency using Map
 *  Put all frequency values into arraylist, sort in descending order
 *  Find if r1 < c(r3+ .... +rm) (r3 for l=3 diversity)
 */
public class RecursiveLDiversity {
	
	private static final int K_VAL = 5;
	
	private static String[] races = new String[] {"White", "Black", "Amer-Indian-Eskimo", "Asian-Pac-Islander", "Other"};
	private static String[] marital = new String[] {"Divorced", "Married-civ-spouse", "Never-married", "Separated", 
			"Widowed", "Married-spouse-absent", "Married-AF-spouse"};
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
		
		System.out.println("What is your c value?");
		Scanner sc = new Scanner(System.in);
		double cVal = sc.nextDouble();
		sc.close();
		
		// Check diversity for each education/age table (diversifies if not)
		for (Map.Entry<String, Map<String, ArrayList<String>>> val : ageTableMap.entrySet()) {
			Map<String, ArrayList<String>> eduTuples = val.getValue();
					
			for(Map.Entry<String, ArrayList<String>> val2 : eduTuples.entrySet()) { 
				ArrayList<String> ageTuples = val2.getValue();
						 
				boolean diverse = diversityCheck(ageTuples, cVal);
				//System.out.println(diverse);
						 
				while(!diverse) {
					diversify(ageTuples, val2.getKey(), val.getKey());
					diverse = diversityCheck(ageTuples, cVal);
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
		FileWriter writer = new FileWriter("(" + cVal + ", 3)-recursive-output.txt", false); 
		for(String str: dataSet) {
			writer.write(str + System.lineSeparator());
		}
		writer.close();
		System.out.println("Anonymization complete, please see " + cVal+ ",3-recursive-output.txt. K: " + k + ". Gen-level: " + kGenLevel);
	}
	
	// MEHTODS
	
	/**
	 * Checks (c,l)-diversity of a table/array of tuples.
	 * 
	 * @param list
	 * @return
	 */
	public static boolean diversityCheck(ArrayList<String> list, double cVal) {
		boolean isDiverse = false;
		
		ArrayList<String> sensitiveAttributes = new ArrayList<String>();
        for(int i=0; i < list.size(); i++) {
			String row = list.get(i);
			String[] thisRow = row.split(",");
			//6 occupation, sensitive attribute
			sensitiveAttributes.add(thisRow[6]);
		}
        
       // get frequencies into descending order array
       Map<String, Double> frequencies = new HashMap<String, Double>(); 
       for (String i : sensitiveAttributes) { 
           Double j = frequencies.get(i); 
           frequencies.put(i, (j == null) ? 1 : j + 1); 
       } 
       ArrayList<Double> freqArr = new ArrayList<Double>();
       for(Map.Entry<String, Double> val : frequencies.entrySet()) {
    	   freqArr.add(val.getValue());
       }
       Collections.sort(freqArr, Collections.reverseOrder());
       
       double r1 = freqArr.get(0);
       double sum = 0;
       
       for(int i = 2; i < freqArr.size()-2; i++)
       {
    	   sum += freqArr.get(i);
       }
       
       if(r1 < cVal*sum) {
    	   isDiverse=true;
       }
       
       return isDiverse;
	}
	
	/**
	 * Method used to diversify the data
	 * 
	 * @param list
	 * @param age
	 * @param education
	 */
	public static void diversify(ArrayList<String> list, String age, String education) {
		Random rndm = new Random();
		for(int i = 0; i < 10; i++) {
			list.add(age + ",*,*," + education + ",*," + marital[rndm.nextInt(marital.length)] + ","+ occupation[rndm.nextInt(marital.length)] 
					+ ",*,"+ races[rndm.nextInt(races.length)] + ",*,*,*,*,*,*");
		}
	}
}
