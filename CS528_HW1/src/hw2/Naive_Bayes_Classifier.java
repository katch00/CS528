package hw2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Naive_Bayes_Classifier {

	public static double calculateSD(double numArray[]) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }
	
	public static void getStatistics(ArrayList<String> data) {
		
	}
	
	
	public static void main(String [] args) throws FileNotFoundException {
		// ArrayLists for testing data and training data
		ArrayList<String> testData = new ArrayList<String>();
		ArrayList<String> trainingData = new ArrayList<String>();
		
	    //data file
		String filename = "iris.data";
		File dataFile = new File(filename);
		Scanner fileSc = new Scanner(dataFile);
		
		// get data from files and sort into respective arraylists
		int lineNum = 1;
		while(fileSc.hasNextLine()) {
			String line = fileSc.nextLine();
			//consider records (1-10, 51-60, 101-110) as testing data for prediction, and the remaining 120 records as training data.
			if((lineNum >=1 && lineNum <=10) || (lineNum >=51 && lineNum <=60) || (lineNum >=101 && lineNum <=110) ) {
				testData.add(line);
				lineNum++;
			} else {
				trainingData.add(line);
				lineNum++;
			}
		}
		fileSc.close();
		
		
		
	}
}
