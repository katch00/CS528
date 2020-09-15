package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class RecursiveLDiversity {
	
	public static void main(String[] args) throws FileNotFoundException {
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
		
		Kanon.initialSuppression(dataSet);
	}

}
