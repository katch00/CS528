package hw2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Exponential_Mechanism {
	
	/**
	 * Returns the new frequency used in the exponential mechanism.
	 * @param freq
	 * @return
	 */
	public static double exponential(double freq) {
		return -freq * Math.log(Math.random());
	}

	/**
	 * Returns map of the frequencies of each education in the given Arraylist
	 * Key: education type, value: count
	 * 
	 * @param dataSet
	 * @return
	 */
	public static Map<String, Integer> getFrequencies(ArrayList<String> dataSet) {
		Map<String, Integer> frequencies = new HashMap<String, Integer>(); 
        for (String i : dataSet) { 
            Integer j = frequencies.get(i); 
            frequencies.put(i, (j == null) ? 1 : j + 1);
        }  
		
		return frequencies;
	}
	
	/**
	 * Returns map of the probabilities of each education in the given Arraylist
	 * This version is used without the exponential mechanism addition
	 * Key: education type, value: probability
	 * 
	 * @param dataSet
	 * @return
	 */
	public static Map<String, Double> getProbabilities(ArrayList<String> dataSet) {
		Map<String, Integer> frequencies = getFrequencies(dataSet);
		double length = dataSet.size();
        Map<String, Double> probabilities = new HashMap<String, Double>(); 
        for (Map.Entry<String, Integer> val : frequencies.entrySet()) { 
            double probability = (val.getValue()) / length;
            probabilities.put(val.getKey(), probability);
        } 
		
		return probabilities;
	}
	
	/**
	 * Returns map of the probabilities of each education in the given Arraylist
	 * This version is used with the exponential mechanism addition
	 * Key: education type, value: probability
	 * 
	 * @param dataSet
	 * @return
	 */
	public static Map<String, Double> getProbabilities(ArrayList<String> dataSet, double e) {
		Map<String, Integer> frequencies = getFrequencies(dataSet);
		double length = dataSet.size();
        Map<String, Double> probabilities = new HashMap<String, Double>(); 
        for (Map.Entry<String, Integer> val : frequencies.entrySet()) { 
            double probability = (val.getValue()) / length;
            probability = exponential((e*probability) / 2);
            probabilities.put(val.getKey(), probability*100);
        } 
		
		return probabilities;
	}
	
	/**
	 * Takes Map of probabilities and returns a weighted array
	 * Array used to get random query result while taking probability in mind
	 * 
	 * @param probs
	 * @return
	 */
	public static ArrayList<String> getWeightedArray(Map<String, Double> probs) {
		ArrayList<String> weightedArr = new ArrayList<String>();
		for(Map.Entry<String, Double> edu : probs.entrySet()) {
			int num = (int) Math.floor(edu.getValue());
			String education = edu.getKey();
			for(int i = 0; i < num; i++) {
				weightedArr.add(education);
			}
		}
		
		// for extra randomization's sake
		Collections.shuffle(weightedArr);
		return weightedArr;
	}
	
	/**
	 * Generates 1,000 results for the most frequent "Education" over the original dataset
	 * ensures-differential privacy
	 * 
	 * @param data
	 * @param epsilon
	 * @return
	 */
	public static ArrayList<String> queryResultMostFrequent(ArrayList<String> data, double epsilon) {
		Map<String, Double> probs = getProbabilities(data, epsilon);
		
		ArrayList<String> weightedArray = getWeightedArray(probs);
		ArrayList<String> results = new ArrayList<String>();
		for(int i = 0; i < 1000; i++) {
			results.add(weightedArray.get(new Random().nextInt(weightedArray.size())));
		}
		
		return results;
	}
	
	/**
	 * Generates 1,000 results for the most frequent "Education" over dataset with one less record of
	 * most frequent education type.
	 * 
	 * ensures-differential privacy
	 * 
	 * @param data
	 * @param epsilon
	 * @return
	 */
	public static ArrayList<String> queryResultMinusMostFrequent(ArrayList<String> data, double epsilon) {
		// find most frequent education
		Map<String, Integer> freqs = getFrequencies(data);
		int maxCount = 0;
		String maxFreq = "";
		for (Map.Entry<String, Integer> val : freqs.entrySet()) { 
            if(val.getValue() > maxCount) {
            	maxFreq = val.getKey();
            }
        }
		
		boolean oneRemoved = false;
		ArrayList<String> newData = new ArrayList<String>();
		for(String edu : data) {
			if(!edu.equals(maxFreq)) {
				newData.add(edu);
			}
			else if (!oneRemoved) {
				newData.add(edu);
			}
		}
		
		Map<String, Double> probs = getProbabilities(newData, epsilon);
		
		ArrayList<String> weightedArray = getWeightedArray(probs);
		ArrayList<String> results = new ArrayList<String>();
		for(int i = 0; i < 1000; i++) {
			results.add(weightedArray.get(new Random().nextInt(weightedArray.size())));
		}
		
		return results;
	}
	
	/**
	 * Generates 1,000 results for the most frequent "Education" over the dataset with the second most
	 * frequent education type removed
	 * 
	 * ensures-differential privacy
	 * 
	 * @param data
	 * @param epsilon
	 * @return
	 */
	public static ArrayList<String> queryResultNoSecondFreq(ArrayList<String> data, double epsilon) {
		// find most frequent education
		Map<String, Integer> freqs = getFrequencies(data);
		int maxFreq = 0;
		int secFreq = 0;
		String maxEdu = "";
		String secEdu = "";
		
		for (Map.Entry<String, Integer> val : freqs.entrySet()) { 
			String currentChar = val.getKey();
            int frequency = val.getValue();
 
            if (frequency > maxFreq) {
                secFreq = maxFreq;
                secEdu = maxEdu;
                maxFreq = frequency;
                maxEdu = currentChar;
            } else if (frequency > secFreq) {
                secFreq = frequency;
                secEdu = currentChar;
            }
        }
		
		ArrayList<String> newData = new ArrayList<String>();
		for(String edu : data) {
			if(!edu.equals(secEdu)) {
				newData.add(edu);
			}
		}
		
		Map<String, Double> probs = getProbabilities(newData, epsilon);
		
		ArrayList<String> weightedArray = getWeightedArray(probs);
		ArrayList<String> results = new ArrayList<String>();
		for(int i = 0; i < 1000; i++) {
			results.add(weightedArray.get(new Random().nextInt(weightedArray.size())));
		}
		
		return results;
	}
	
	/**
	 * Generates 1,000 results for the most frequent "Education" over the dataset with the least frequent
	 * education type removed.
	 * 
	 * ensures-differential privacy
	 * 
	 * @param data
	 * @param epsilon
	 * @return
	 */
	public static ArrayList<String> queryResultNoLeastFrequent(ArrayList<String> data, double epsilon) {
		// find most frequent education
		Map<String, Integer> freqs = getFrequencies(data);
		int minCount = 100000;
		String minFreq = "";
		for (Map.Entry<String, Integer> val : freqs.entrySet()) { 
            if(val.getValue() < minCount) {
            	minFreq = val.getKey();
            }
        }
		
		ArrayList<String> newData = new ArrayList<String>();
		for(String edu : data) {
			if(!edu.equals(minFreq)) {
				newData.add(edu);
			}
		}
		
		Map<String, Double> probs = getProbabilities(newData, epsilon);
		
		ArrayList<String> weightedArray = getWeightedArray(probs);
		ArrayList<String> results = new ArrayList<String>();
		for(int i = 0; i < 1000; i++) {
			results.add(weightedArray.get(new Random().nextInt(weightedArray.size())));
		}
		
		return results;
	}
	
	/**
	 * 
	 * Takes two ArrayLists of query results. 
	 * Then finds probability quotient.
	 * If both are smaller than e^epsilon, then this proves epsilon-indistinguishable
	 * 
	 * Indistinguishability can be proved by showing the probability quotient of two adjacent tables is smaller than exp^epsilon
	 * 
	 * @param results1
	 * @param results2
	 * @param epsilon
	 */
	public static void isIndistinguishable(ArrayList<String> results1, ArrayList<String> results2, double epsilon) {
		Map<String, Double> probs1 = getProbabilities(results1);
		Map<String, Double> probs2 = getProbabilities(results2);
		
		double sum1 = 0, count1 = 0;
		
		for(Map.Entry<String, Double> val : probs1.entrySet()) {
			String key = val.getKey();
			if(probs2.containsKey(key)) {
				sum1 = sum1 + (probs1.get(key) / probs2.get(key));
				count1++;
			}
		}
		
		double check1 = sum1/count1;
		double sum2 = 0, count2 = 0;
		for(Map.Entry<String, Double> val : probs2.entrySet()) {
			String key = val.getKey();
			if(probs1.containsKey(key)) {
				sum2 = sum2 + (probs2.get(key) / probs1.get(key));
				count2++;
			}
		}
		double check2 = sum2/count2;
		
		System.out.println("Res1: " + check1 + " Res2: " + check2 + " Exp^Ep: " + Math.exp(epsilon));
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
		ArrayList<String> educations = new ArrayList<String>();
		for(int i = 0; i < dataSet.size()-1; i++) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			educations.add(thisRow[3]);
		}
		
		// epsilon = .5
		ArrayList<String> mostFrequent = queryResultMostFrequent(educations, .5);
		ArrayList<String> mostFrequentMinusOne = queryResultMinusMostFrequent(educations, .5);
		ArrayList<String> noSecMostFreq = queryResultNoSecondFreq(educations, .5);
		ArrayList<String> noLeastFreq = queryResultNoLeastFrequent(educations, .5);
		isIndistinguishable(mostFrequent, mostFrequentMinusOne, .5);
		isIndistinguishable(mostFrequent, noSecMostFreq, .5);
		isIndistinguishable(mostFrequent, noLeastFreq, .5);
		
		// epsilon = 1
		ArrayList<String> e2mostFrequent = queryResultMostFrequent(educations, 1);
		ArrayList<String> e2mostFrequentMinusOne = queryResultMinusMostFrequent(educations, 1);
		ArrayList<String> e2noSecMostFreq = queryResultNoSecondFreq(educations, 1);
		ArrayList<String> e2noLeastFreq = queryResultNoLeastFrequent(educations, 1);
		isIndistinguishable(e2mostFrequent, e2mostFrequentMinusOne, 1);
		isIndistinguishable(e2mostFrequent, e2noSecMostFreq, 1);
		isIndistinguishable(e2mostFrequent, e2noLeastFreq, 1);
		
		FileWriter writer = new FileWriter("exponential.txt", false); 
		
		// print 8000 results
		for(String str: mostFrequent) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: mostFrequentMinusOne) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: noSecMostFreq) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: noLeastFreq) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: e2mostFrequent) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: e2mostFrequentMinusOne) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: e2noSecMostFreq) {
			writer.write(str + System.lineSeparator());
		}
		for(String str: e2noLeastFreq) {
			writer.write(str + System.lineSeparator());
		}
		writer.close();
	}
}
