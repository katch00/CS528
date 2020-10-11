package hw2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Class used to create 8,000 results for a search query on the adult.data dataset with added laplace noise
 * Four different methods are used to cater to the four result types
 * Each method is used for e = .5 and e = 1
 * Results are printed to laplace.txt
 * 
 * @author nstro
 *
 */
public class Laplace_Mechanism {
	
	/** 
	 * scale should be sensitivity/epsilon.
	 * Returns noise to be added to the average
	 * Formula derived from link below:
	 * https://www.johndcook.com/blog/2018/03/13/generating-laplace-random-variables/
	 * 
	 * @param scale
	 * @return
	 */
	public static double laplace_noise(double scale) {
		double e1 = -scale * Math.log(Math.random());
		double e2 = -scale * Math.log(Math.random());
		
		return e1 - e2;
	}
	
	/**
	 * returns the sensitive value (in this case it is maximum age over count)
	 * 
	 * @param dataSet
	 * @return
	 */
	public static double getSensitive(ArrayList<String> dataSet) {
		double maxAge = 0, count = 0;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			double age = Integer.parseInt(thisRow[0]);
			if(age > 25) {
				count++;
				if(Integer.parseInt(thisRow[0]) > maxAge) {
					maxAge = age;
				}
			}
		}
		return maxAge/count;
	}
	
	/**
	 * Takes dataset and filters out records with ages under 25
	 * Then applies Laplace noise to create 1000 results for the query
	 * 
	 * @param dataSet
	 * @param e
	 * @return
	 */
	public static ArrayList<Double> queryResultsAbove25(ArrayList<String> dataSet, double e){
		// loop through array and get results above 25
		int count = 0;
		int sum = 0;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			if(Integer.parseInt(thisRow[0]) > 25) {
				count++;
				sum = sum + Integer.parseInt(thisRow[0]);
			}
		}
		// get average
		double average = sum / count;
		ArrayList<Double> results = new ArrayList<Double>();
		double sensitiveVal = getSensitive(dataSet);
		// add averages + noise, 1000 results
		for(int i = 0; i < 1000; i++) {
			double result = average + laplace_noise(sensitiveVal/e); 
			//two decimal places
			BigDecimal bd = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP);
	        double roundedResult = bd.doubleValue();
			results.add(roundedResult);
		}
		return results;
	}
	
	/**
	 * Takes dataset and filters out records with ages under 25, then removes one record with the max age
	 * Then applies Laplace noise to create 1000 results for the query
	 * 
	 * @param dataSet
	 * @param e
	 * @return
	 */
	public static ArrayList<Double> queryResultsRemoveOldest(ArrayList<String> dataSet, double e){
		double maxAge = 0, count = 0;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			double age = Integer.parseInt(thisRow[0]);
			if(age > 25) {
				if(Integer.parseInt(thisRow[0]) > maxAge) {
					maxAge = age;
				}
			}
		}
		// loop through array and get results above 25
		int sum = 0;
		ArrayList<String> newDataSet = new ArrayList<String>();
		boolean removedOldest = false;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			int age = Integer.parseInt(thisRow[0]);
			// if record is not max age, use it
			if((age > 25) && age != maxAge ) {
				count++;
				sum = sum + age;
				newDataSet.add(thisRow[0]);
			}
			//check if single oldest has been removed
			if(age == maxAge && removedOldest == true) {
				count++;
				sum = sum + age;
				newDataSet.add(thisRow[0]);
			}
			else {
				removedOldest = true;
			}
		}
		
		// get average
		double average = sum / count;
		ArrayList<Double> results = new ArrayList<Double>();
		double sensitiveVal = getSensitive(newDataSet);
		// add averages + noise, 1000 results
		for(int i = 0; i < 1000; i++) {
			double result = average + laplace_noise(sensitiveVal/e); 
			//two decimal places
			BigDecimal bd = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP);
	        double roundedResult = bd.doubleValue();
			results.add(roundedResult);
		}
		return results;
	}
	
	
	/**
	 * Takes dataset and filters out records with ages under 25 and records with age = 26
	 * Then applies Laplace noise to create 1000 results for the query
	 * 
	 * @param dataSet
	 * @param e
	 * @return
	 */
	public static ArrayList<Double> queryResultsRemove26(ArrayList<String> dataSet, double e){
		// loop through array and get results above 25
		int count = 0;
		int sum = 0;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			int age = Integer.parseInt(thisRow[0]);
			if((age > 25) && age != 26 ) {
				count++;
				sum = sum + age;
			}
		}
		
		// get average
		double average = sum / count;
		ArrayList<Double> results = new ArrayList<Double>();
		double sensitiveVal = getSensitive(dataSet);
		// add averages + noise, 1000 results
		for(int i = 0; i < 1000; i++) {
			double result = average + laplace_noise(sensitiveVal/e); 
			//two decimal places
			BigDecimal bd = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP);
	        double roundedResult = bd.doubleValue();
			results.add(roundedResult);
		}
		return results;
	}
	
	/**
	 * I don't see how removing the youngest changes anything, which means I'm doing everything wrong probably
	 * Either way, this method technically checks for the youngest age and doesn't include it
	 * But if we're averaging the records over 25 that won't come into play, idk
	 * 
	 * @param dataSet
	 * @param e
	 * @return
	 */
	public static ArrayList<Double> queryResultsRemoveYoungest(ArrayList<String> dataSet, double e){
		double minAge = 1000, count = 0;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			double age = Integer.parseInt(thisRow[0]);
			if(age < minAge) {
				minAge = age;
			}
		}
		// loop through array and get results above 25
		int sum = 0;
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			int age = Integer.parseInt(thisRow[0]);
			// if record is not max age, use it
			if((age > 25) && age != minAge ) {
				count++;
				sum = sum + age;
			}
		}
		// get average
		double average = sum / count;
		ArrayList<Double> results = new ArrayList<Double>();
		double sensitiveVal = getSensitive(dataSet);
		// add averages + noise, 1000 results
		for(int i = 0; i < 1000; i++) {
			double result = average + laplace_noise(sensitiveVal/e); 
			//two decimal places
			BigDecimal bd = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP);
	        double roundedResult = bd.doubleValue();
			results.add(roundedResult);
		}
		return results;
	}
	
	/**
	 * Takes two ArrayLists of query results. Sorts values into buckets (20).
	 * Then finds probability quotient of prob1/prob2 and prob2/prob1
	 * If both are smaller than e^epsilon, then this proves epsilon-indistinguishable
	 * 
	 * @param results1
	 * @param results2
	 * @param epsilon
	 */
	public static void isIndistinguishable(ArrayList<Double> results1, ArrayList<Double> results2, double epsilon) {
		double max = Math.max(Collections.max(results1), Collections.max(results2));
		double min = Math.min(Collections.min(results1), Collections.min(results2));
		
		int[] counts1 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		for(int i = 0; i < results1.size(); i++) {
			int index = (int) Math.floor((results1.get(i)-min+1)/((max-min+1)/20))-1;
			counts1[index] = counts1[index] +1 ;
		}
		
		Map<String, Double> probs1 = getProbabilities(counts1);
		
		int[] counts2 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		for(int i = 0; i < results2.size(); i++) {
			int index = (int) Math.floor((results2.get(i)-min+1)/((max-min+1)/20))-1;
			counts2[index] = counts2[index] +1 ;
		}
		Map<String, Double> probs2 = getProbabilities(counts2);
		
		double check1 = 0;
		for(Map.Entry<String, Double> val : probs1.entrySet()) {
			String key = val.getKey();
			if(probs1.get(key) != 0 && probs2.get(key) !=0 ) {
				check1 = check1 + (probs1.get(key) / probs2.get(key));
			}
		}
		double check2 = 0;
		for(Map.Entry<String, Double> val : probs1.entrySet()) {
			String key = val.getKey();
			if(probs1.get(key) != 0 && probs2.get(key) !=0 ) {
				check2 = check2 + (probs2.get(key) / probs1.get(key));
			}
		}
		
		System.out.println("Res1: " + check1/20 + " Res2: " + check2/20 + " Exp^Epsilon: " + Math.exp(epsilon));
		
		
	}
	
	/**
	 * Creates a Map of frequencies given an array of counts
	 * Key: position in array/bucket number, value: count of how many objects in this bucket
	 * 
	 * @param counts
	 * @return
	 */
	public static Map<String, Integer> getFrequencies(int[] counts) {
		Map<String, Integer> frequencies = new HashMap<String, Integer>(); 
        for (int i = 0; i < counts.length; i++) { 
            Integer j = frequencies.get(Integer.toString(i));
            frequencies.put(Integer.toString(i), (j == null) ? 1 : j + 1);
        }  
		
		return frequencies;
	}
	
	/**
	 * Takes array of counts and outputs a Map of probabilities
	 * Key: position in array/bucket number, value: probability of landing in this bucket
	 * 
	 * @param counts
	 * @return
	 */
	public static Map<String, Double> getProbabilities(int[] counts) {
		Map<String, Integer> frequencies = getFrequencies(counts);
		double length = counts.length;
        Map<String, Double> probabilities = new HashMap<String, Double>(); 
        for (Map.Entry<String, Integer> val : frequencies.entrySet()) { 
            double probability = (val.getValue()) / length;
            probabilities.put(val.getKey(), probability);
        } 
		
		return probabilities;
	}
	

	/**
	 * Main method for creating query results output.
	 * Also calculates values to determine if e-indistinguishable is met
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		ArrayList<String> dataSet = new ArrayList<String>();
		
		String filename = "adult.data";
		File dataFile = new File(filename);
		Scanner fileSc = new Scanner(dataFile);
		
		//add each row from file to list of rows
		while(fileSc.hasNextLine()) {
			String line = fileSc.nextLine();
			dataSet.add(line);	
		}
		fileSc.close();
		
		// for e = .5
		double e1 = .5;
		ArrayList<Double> query25 = queryResultsAbove25(dataSet, e1); 
		ArrayList<Double> queryRemoveMax = queryResultsRemoveOldest(dataSet, e1);
		ArrayList<Double> queryNo26 = queryResultsRemove26(dataSet, e1);
		ArrayList<Double> queryNoYoungest = queryResultsRemoveYoungest(dataSet, e1);
		// prints probability quotients to compare to exp^epsilon
		isIndistinguishable(query25, queryRemoveMax, e1);	
		isIndistinguishable(query25, queryNo26, e1);	
		isIndistinguishable(query25, queryNoYoungest, e1);
		
		
		// for e = 1.0
		double e2 = 1.0;
		ArrayList<Double> e2query25 = queryResultsAbove25(dataSet, e2); 
		ArrayList<Double> e2queryRemoveMax = queryResultsRemoveOldest(dataSet, e2);
		ArrayList<Double> e2queryNo26 = queryResultsRemove26(dataSet, e2);
		ArrayList<Double> e2queryNoYoungest = queryResultsRemoveYoungest(dataSet, e2);
		// prints probability quotients to compare to exp^epsilon
		isIndistinguishable(e2query25, e2queryRemoveMax, e2);	
		isIndistinguishable(e2query25, e2queryNo26, e2);	
		isIndistinguishable(e2query25, e2queryNoYoungest, e2);
		
		// writes all results to an output file.
		FileWriter writer = new FileWriter("laplace.txt", false); 
		for(Double str: query25) {
			writer.write(str + System.lineSeparator());
		}
		for(Double str: queryRemoveMax) {
			writer.write(str + System.lineSeparator());
		}
		for(Double str: queryNo26) {
			writer.write(str + System.lineSeparator());
		}
		for(Double str: queryNoYoungest) {
			writer.write(str + System.lineSeparator());
		}
		
		for(Double str: e2query25) {
			writer.write(str + System.lineSeparator());
		}
		for(Double str: e2queryRemoveMax) {
			writer.write(str + System.lineSeparator());
		}
		for(Double str: e2queryNo26) {
			writer.write(str + System.lineSeparator());
		}
		for(Double str: e2queryNoYoungest) {
			writer.write(str + System.lineSeparator());
		}
		writer.close();
		
	}
}
