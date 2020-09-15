package hw1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * K-anonymization algorithm
 * @author Natalia Stroupe
 *
 * Semi-implementation of DataFly algorithm. Distortion high, which is expected from this algorithm choice
 * To be used with adult.data file from https://archive.ics.uci.edu/ml/machine-learning-databases/adult/
 * I tried really hard :)
 */

public class Kanon {
	
	private static final int HIGH_PROTECTION_K = 10;
	private static final int LOW_PROTECTION_K = 5 ;
	
	public static void main(String[] args) throws IOException {
		ArrayList<String> highProtection = new ArrayList<String>();
		ArrayList<String> lowProtection = new ArrayList<String>();
		
	    //data file
		String filename = "adult.data";
		File dataFile = new File(filename);
		Scanner fileSc = new Scanner(dataFile);
		
		//add each row from file to list of rows
		while(fileSc.hasNextLine()) {
			String line = fileSc.nextLine();
			
			if(line.contains("<=50K")) {
				highProtection.add(line);
			}else if(line.contains(">50K")){
				lowProtection.add(line);
			}
		}
		fileSc.close();
		
		//Initial Suppression of unneeded attributes
		initialSuppression(highProtection);
		initialSuppression(lowProtection);
		
		//list of all tuples, still keep separate tuples for k = 5 and k = 10
		ArrayList<String> rowsList = new ArrayList<String>(lowProtection);
		rowsList.addAll(highProtection);
		
		// calculate initial k
		int k = minFrequencies(rowsList);
		int kGenLevel = 0;
		
		// Suppress all tuples until low protection k = 5 is met
		while(k<LOW_PROTECTION_K) {
			kGenLevel++;
			rowsList.clear();
			generalizeData(highProtection, kGenLevel);
			generalizeData(lowProtection, kGenLevel);
			
			rowsList.addAll(highProtection);
			rowsList.addAll(lowProtection);
			
			k = minFrequencies(rowsList);
		}
		
		//Print k and generalization level to use for k = 5 calculations later
		System.out.println("Low Protection Complete: k = " + k + ". Generalization level = " + kGenLevel);
		
		// Suppress only high Protection tuples until k = 10 is met
		while(k<HIGH_PROTECTION_K) {
			kGenLevel++;
			generalizeData(highProtection, kGenLevel);
			
			k = minFrequencies(highProtection);
		}
		
		// used for later calculations
		System.out.println("High Protection Complete: k = " + k + ". Generalization level = " + kGenLevel);
		
		// Output with separate protections in one array.
		rowsList.clear();
		rowsList.addAll(highProtection);
		rowsList.addAll(lowProtection);
		
		// Write rows to output file
		FileWriter writer = new FileWriter("k-anon-output.txt", false); 
		for(String str: rowsList) {
		  writer.write(str + System.lineSeparator());
		}
		writer.close();
		
		// let user know file has been written and process is complete
		System.out.println("Anonymization complete, please see output.txt.");
   }
	
	// METHODS
	
	/**
	 * Suppresses unnecessary values in original data file as directed by instructions
	 * 
	 * @param rowsList
	 */
	public static void initialSuppression(ArrayList<String> rowsList) {
		for(int i=0; i < rowsList.size(); i++) {
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
        Map<String, Integer> hm = new HashMap<String, Integer>(); 
  
        for (String i : temp) { 
            Integer j = hm.get(i); 
            hm.put(i, (j == null) ? 1 : j + 1); 
        } 
  
        for (Map.Entry<String, Integer> val : hm.entrySet()) { 
            if(val.getValue() < min) {
            	min = val.getValue();
            }
        } 
        
        return min;
    }  
	
	/**
	 * Generalizes data based off of Datafly algorithm. 
	 * Datafly: If k isn't met, generalize the attribute with most distinct values
	 * 
	 * @param arr
	 * @param level
	 */
	public static void generalizeData(ArrayList<String> arr, int level) {
		switch (level) {
			//Generalize Age 1 level: most number of distinct values
			case 1: 
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					int age = Integer.parseInt(thisRow[0]);
					if(age<16) {
						thisRow[0] = "1-15";
					} else if(age>15 && age<31){
						thisRow[0] = "16-30";
					} else if(age>30 && age<46){
						thisRow[0] = "31-46";
					} else if(age>45 && age<61){
						thisRow[0] = "46-60";
					} else if(age>60 && age<76){
						thisRow[0] = "61-75";
					} else if(age>75 && age<91){
						thisRow[0] = "76-90";
					} else if(age>90 && age<106){
						thisRow[0] = "91-105";
					} else if(age>105){
						thisRow[0] = "105+";
					}
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// suppress education level 1 level: most num of attributes after age suppression. Same for the rest of cases.
			case 2:
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String school = thisRow[3];
					if(school.equals("Preschool") || school.equals("1st-4th")) {
						thisRow[3] = "Preschool-4th";
					} else if(school.equals("5th-6th") || school.equals("7th-8th")){
						thisRow[3] = "5th-8th";
					} else if(school.equals("9th") || school.equals("10th")){
						thisRow[3] = "9th-10th";
					} else if(school.equals("11th") || school.equals("12th")){
						thisRow[3] = "11th-12th";
					} else if(school.equals("HS-grad") || school.equals("Some-college")){
						thisRow[3] = "HS Grad/No degree";
					} else if(school.equals("Assoc-acdm") || school.equals("Assoc-voc")){
						thisRow[3] = "Assoc";
					} else if(school.equals("Bachelors") || school.equals("Masters")){
						thisRow[3] = "Bs/Ms";
					} else if(school.equals("Doctorate") || school.equals("Prof-school")){
						thisRow[3] = "Prof-school/Doctorate";
					}
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// Age level 2
			case 3: 
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String age = thisRow[0];
					if(age.equals("1-15") || age.equals("16-30")) {
						thisRow[0] = "1-30";
					} else if(age.equals("31-45") || age.equals("46-60")){
						thisRow[0] = "31-60";
					} else if(age.equals("61-75") || age.equals("76-90")){
						thisRow[0] = "61-90";
					} else if(age.equals("91-105") || age.equals("105+")){
						thisRow[0] = "91+";
					}
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// Education level, level 2
			case 4:
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String school = thisRow[3];
					if(school.equals("Preschool-4th") || school.equals("5th-8th")) {
						thisRow[3] = "Preschool-8th";
					} else if(school.equals("9th-10th") || school.equals("11th-12th")){
						thisRow[3] = "9th-12th";
					} else if(school.equals("HS Grad/No degree") || school.equals("Assoc")){
						thisRow[3] = "<4 years college";
					} else if(school.equals("Bs/Ms") || school.equals("Prof-school/Doctorate")){
						thisRow[3] = "4+ years college";
					}
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// Marital Status level 1
			case 5:
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String maritalStatus = thisRow[5];
					if(maritalStatus.equals("Divorced") || maritalStatus.equals("Widowed") 
							|| maritalStatus.contentEquals("Separated")) {
						thisRow[5] = "Was Married";
					} else if(maritalStatus.equals("Married-civ-spouse") || maritalStatus.equals("Married-spouse-absent") 
							|| maritalStatus.equals("Married-AF-spouse")){
						thisRow[5] = "Married";
					} 
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// race level 1
			case 6: 
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String race = thisRow[8];
					if(race.equals("White") || race.equals("Black")){
						thisRow[8] = "White/Black";
					} else {
						thisRow[8] = "Asian-Pac-Islander/Amer-Indian-Eskimo/Other";
					}
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// age level 3
			case 7:
				for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String age = thisRow[0];
					if(age.equals("1-30") || age.equals("31-60")) {
						thisRow[0] = "<=60";
					} else {
						thisRow[0] = ">60";
					}
					arr.set(i, String.join(",", thisRow));
				}
			    break;
			// Education level, level 3
			case 8:
			    for(int i = 0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					String school = thisRow[3];
					if(school.equals("Preschool-8th") || school.equals("9th-12th")) {
						thisRow[3] = "Non-HS Grad";
					} else {
						thisRow[3] = "HS Grad or Higher";
					}
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// Marital Status level 2
			case 9: 
				for(int i =0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					thisRow[5] = "*";
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// age level 4
			case 10: 
				for(int i =0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					thisRow[0] = "*";
					arr.set(i, String.join(",", thisRow));
				}
				break;
			// education level 4
			case 11:
				for(int i =0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					thisRow[3] = "*";
					arr.set(i, String.join(",", thisRow));
				}
				break;
			//race level 2
			case 12: 
				for(int i =0; i < arr.size(); i++) {
					String row = arr.get(i);
					String[] thisRow = row.split(",");
					thisRow[1] = "*";
					arr.set(i, String.join(",", thisRow));
				}
				break;
		}
	}
}
