package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DistortionPrecision {
	
	private static final int NUM_QIIDS = 4;
	private static final int MAX_AGE_LEVEL= 4;
	private static final int MAX_EDU_LEVEL = 4;
	private static final int MAX_RACE_LEVEL = 2;
	private static final int MAX_MARITAL_LEVEL = 2;
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("First we will calculate distortion!");
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter current level of generalization for attribute age:");
		double ageLevel = sc.nextDouble();
		System.out.println("Enter current level of generalization for attribute education:");
		double eduLevel = sc.nextDouble();
		System.out.println("Enter current level of generalization for attribute race:");
		double raceLevel = sc.nextDouble();
		System.out.println("Enter current level of generalization for attribute marital status:");
		double maritalLevel = sc.nextDouble();
		
		double sum = (ageLevel/MAX_AGE_LEVEL) + (eduLevel/MAX_EDU_LEVEL) + (raceLevel/MAX_RACE_LEVEL) + (maritalLevel/MAX_MARITAL_LEVEL);
		double distortion = sum/NUM_QIIDS;
		
		System.err.println("Distortion = " + distortion);
		sc.close();
		
		String filename = "adult.data";
		File dataFile = new File(filename);
		Scanner fileSc = new Scanner(dataFile);
		
		int valueCount = 0; // |PT|
		while(fileSc.hasNextLine()) {
			fileSc.nextLine();
			valueCount++;
		}
		fileSc.close();
		
		System.err.println("Precision: " + (1-distortion));
		
		
		
	}

}
