package hw2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

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
	 * Query the average of results above 25
	 * Returns an arraylist of 1000 results with laplace noise
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
	
	public static ArrayList<Double> queryResultsRemoveOldest(ArrayList<String> dataSet, double e){
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
		// loop through array and get results above 25
		int count2 = 0;
		int sum = 0;
		ArrayList<String> newDataSet = new ArrayList<String>();
		for(int i = 0; i < dataSet.size()-1; i++ ) {
			String row = dataSet.get(i);
			String[] thisRow = row.split(", ");
			int age = Integer.parseInt(thisRow[0]);
			if(age > 25 && age != maxAge ) {
				count++;
				sum = sum + age;
				newDataSet.add(thisRow[0]);
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
		ArrayList<Double> queryNoMax = queryResultsRemoveOldest(dataSet, e1);
		FileWriter writer = new FileWriter("laplace.txt", false); 
		for(Double str: query25) {
		  writer.write(str + System.lineSeparator());
		}
		for(Double str: queryNoMax) {
			  writer.write(str + System.lineSeparator());
			}
		writer.close();
		
	}
}
