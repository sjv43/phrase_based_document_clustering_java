package com.doccluster.cleaner;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class DataCleaner {
	private String inputFolderLocation;
	private String outputFolderLocation;
	private String stopWordsFileLocation;
	private String documentKeyword;
	
	//character separating each file sequence
	private final char fileSeparatorCharacter = '$';
	
	//a word replacement for integer value of the fileSeparatorCharacter
	private final String fileSeparatorReplacement = "00null00";
	
	//a word to indicate that the document is empty
	private final String emptyDocumentIndicator = "00DocumentIsEmpty00";
	
	private List<String> stopWords;
	public List<String> allWords;
	
	public DataCleaner(String inputFolderLocation, String outputFolderLocation, 
			String stopWordsFileLocation, String documentKeyword) {
		this.stopWordsFileLocation = stopWordsFileLocation;
		this.inputFolderLocation = inputFolderLocation;
		this.outputFolderLocation = outputFolderLocation;
		this.documentKeyword = documentKeyword;
		
		stopWords = new ArrayList<String>();
		allWords = new ArrayList<String>();
		
		getStopWords();
	}
	
	//Reading the stop words list
	private void getStopWords() {
      	FileInputStream fstream = null;
      	DataInputStream  inStream = null;
        BufferedReader br = null;
        
        String allStopWords = "";
		try {
      		// Open the file that is the first 
      		// command line parameter
      		fstream = new FileInputStream(stopWordsFileLocation);
      		
            // Get the object of DataInputStream
        	inStream = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(inStream));
        
        	String strLine;
        	//Read File Line By Line
        	while ((strLine = br.readLine()) != null)   {
          		// Print the content on the console
        		allStopWords = allStopWords + " " + strLine;
        	}
        	allStopWords = allStopWords.trim();
        	       	
    	    //Close the input stream
    	    inStream.close();
    	        	    
        } catch (Exception e) { //Catch exception if any
          System.err.println("Error: " + e.getMessage());
        } 
        
        StringTokenizer wordTokenizer = new StringTokenizer(allStopWords);
	    while (wordTokenizer.hasMoreTokens()) {
	    	this.stopWords.add(wordTokenizer.nextToken());
	    }  	
        
	}
	
	public void doCleaning() {
		File inputFolder = new File(inputFolderLocation);
	    File[] listOfFolders = inputFolder.listFiles();
	    	    
	    for (int i = 0; i < listOfFolders.length; i++) {
		    File inputFolder2 = new File(listOfFolders[i].getPath());
		    File[] listOfFiles = inputFolder2.listFiles();
		    
		    for (int j = 0; j < listOfFiles.length; j++) {
		      	FileInputStream fstream = null;
		      	DataInputStream  inStream = null;
		        BufferedReader br = null;
		        
		        String fileData = "";
				try {
		      		// Open the file that is the first 
		      		// command line parameter
		      		fstream = new FileInputStream(listOfFiles[j]);
		      		
		            // Get the object of DataInputStream
		        	inStream = new DataInputStream(fstream);
		            br = new BufferedReader(new InputStreamReader(inStream));
		        
		            String strLine;
		            boolean startFlag = (this.documentKeyword != "") ? false : true; 
		        	//Read File Line By Line
		        	while ((strLine = br.readLine()) != null)   {
		          		// Print the content on the console
		        		if (!startFlag) {
		        			if (strLine.startsWith(this.documentKeyword)) 
			        			startFlag = true;
		        		}
		        		else 
		        			fileData = fileData + " " + strLine;
		        	}
		        	fileData = fileData.trim();
		        	
		    	    //Close the input stream
		    	    inStream.close();
		    	    
		        } catch (Exception e) { //Catch exception if any
		          System.err.println("Error: " + e.getMessage());
		        }
		                
		        fileData = initialCleaning(fileData);
		        fileData = removeNonWords(fileData);
		        fileData = removeStopWords(fileData);
		        fileData = applyPortersAlg(fileData);
		        addWords(fileData);
		        outputCleanedData(fileData, inputFolder2.getName(), listOfFiles[j].getName());
		    }
	    }
  
	    Collections.sort(allWords);
	    outputCodedData();
	}
	
	//Only letters, numbers and space characters are retained
	private String initialCleaning (String fileData) {
		String cleanedData;
		cleanedData = fileData.replaceAll("[^a-zA-Z0-9 ]", "");
		cleanedData = cleanedData.toLowerCase();
		return cleanedData;		
	}
	
	//Checking for non words
	private String removeNonWords (String fileData) {
		String cleanedData = "";
        StringTokenizer wordTokenizer = new StringTokenizer(fileData);
	    while (wordTokenizer.hasMoreTokens()) {
	    	String nextWord = wordTokenizer.nextToken();
	    	if(isWordValid(nextWord)) {
	    		cleanedData = cleanedData + " " + nextWord;
	    	}
	    }
	    cleanedData = cleanedData.trim();
		return cleanedData; 
	}
	
	//if the string has non-letters in it, its considered not a word
	private boolean isWordValid(String str) {
		char[] str2 = str.toCharArray();
		
		for (int i = 0; i < str2.length; i++) {
			if (!Character.isLetter(str2[i]))
				return false;
		}
		return true;
	}
	
	//Removing stop words
	private String removeStopWords (String fileData) {
		String cleanedData = "";
        StringTokenizer wordTokenizer = new StringTokenizer(fileData);
	    while (wordTokenizer.hasMoreTokens()) {
	    	String nextWord = wordTokenizer.nextToken();
	    	if(!stopWords.contains(nextWord)) {
	    		cleanedData = cleanedData + " " + nextWord;
	    	}
	    }
	    cleanedData = cleanedData.trim();
		return cleanedData; 
	}
	
	//Applying porters stemming algorithm
	private String applyPortersAlg (String fileData) {
		PorterStemmer pStemmer = new PorterStemmer();
		String cleanedData = "";
        StringTokenizer wordTokenizer = new StringTokenizer(fileData);
	    while (wordTokenizer.hasMoreTokens()) {
	    	cleanedData = cleanedData + " " + pStemmer.stem(wordTokenizer.nextToken());
	    }  	
	    cleanedData = cleanedData.trim();
		return cleanedData; 
	}
	
	//Populating the allWords list
	private void addWords (String fileData) {
        StringTokenizer wordTokenizer = new StringTokenizer(fileData);
	    while (wordTokenizer.hasMoreTokens()) {
	    	String nextWord = wordTokenizer.nextToken();
	    	if (!allWords.contains(nextWord))
	    		allWords.add(nextWord);
	    } 
	    
	    //Replacing the "integer value of the fileSeparatorCharacter" word entry with
	    //the fileSeparatorReplacement string
	    int index = (int)this.fileSeparatorCharacter;
	    String currFileSeparatorWord = allWords.get(index);
	    allWords.set(index, this.fileSeparatorReplacement);
	    allWords.add(currFileSeparatorWord);
	    
	    //Adding the emptyDocumentIndicator word entry
	    allWords.add(this.emptyDocumentIndicator);
	}
	
	//Outpputting the cleaned data
	private void outputCleanedData(String fileData, String folderName, String fileName) {
		if (fileData == "")
			fileData = this.emptyDocumentIndicator;
		
  	    fileData = addLineBreakers(fileData);
		FileOutputStream fOut = null;
	    try {
		    String newOutputFolderName = this.outputFolderLocation + "\\CleanedData";
		  	File newOutputFolder = new File(newOutputFolderName);
		  	if (!newOutputFolder.exists()) newOutputFolder.mkdir();
		  	
		    newOutputFolderName = this.outputFolderLocation + "\\CleanedData\\" + folderName ;
		  	newOutputFolder = new File(newOutputFolderName);
		  	if (!newOutputFolder.exists()) newOutputFolder.mkdir();
		  	
	  		// Open the input streams for the remote file 
		    String outputFile = this.outputFolderLocation + "\\CleanedData\\" + folderName + "\\" + fileName;
		    fOut = new FileOutputStream(outputFile);
		
		    // Open the output streams for saving this file on disk
	   		char[] data = fileData.toCharArray();
	   		for (int i = 0; i < data.length; i++) {
	   			fOut.write(data[i]);
	   		}
		    
	    } catch (Exception e) { //Catch exception if any
          System.err.println("Error: " + e.getMessage());
        } finally {
         	try{
           		fOut.flush(); 
           		fOut.close();      
         	} catch(Exception e){e.printStackTrace();}
        }
	}
	
	//Outputting the coded data
	private void outputCodedData() {
		File inputFolder = new File(this.outputFolderLocation + "\\CleanedData\\");
	    File[] listOfFolders = inputFolder.listFiles();
	    
	    for (int i = 0; i < listOfFolders.length; i++) {
		    File inputFolder2 = new File(listOfFolders[i].getPath());
		    File[] listOfFiles = inputFolder2.listFiles();
		    
		    for (int j = 0; j < listOfFiles.length; j++) {
		      	FileInputStream fstream = null;
		      	DataInputStream  inStream = null;
		        BufferedReader br = null;
		        
		        String fileData = "";
				try {
		      		// Open the file that is the first 
		      		// command line parameter
		      		fstream = new FileInputStream(listOfFiles[j]);
		      		
		            // Get the object of DataInputStream
		        	inStream = new DataInputStream(fstream);
		            br = new BufferedReader(new InputStreamReader(inStream));
		        
		            String strLine = "";
		        	//Read File Line By Line
		        	while ((strLine = br.readLine()) != null)   {
		          		// Print the content on the console
		        		fileData = fileData + " " + strLine;
		        	}
		        	fileData = fileData.trim();
		        	
		    	    //Close the input stream
		    	    inStream.close();
		    	    
		        } catch (Exception e) { //Catch exception if any
		          System.err.println("Error: " + e.getMessage());
		        }
		        
		        String codedData = "";
		        StringTokenizer wordTokenizer = new StringTokenizer(fileData);
			    while (wordTokenizer.hasMoreTokens()) {
			    	codedData = codedData + " " + allWords.indexOf((wordTokenizer.nextToken()));
			    } 
			    codedData = codedData.trim();
			    codedData = addLineBreakers(codedData);
			    			    
		  	    FileOutputStream fOut = null;
			    try {
				    String newOutputFolderName = this.outputFolderLocation + "\\CodedData";
				  	File newOutputFolder = new File(newOutputFolderName);
				  	if (!newOutputFolder.exists()) newOutputFolder.mkdir();
				  	
				    newOutputFolderName = this.outputFolderLocation + "\\CodedData\\" + inputFolder2.getName() ;
				  	newOutputFolder = new File(newOutputFolderName);
				  	if (!newOutputFolder.exists()) newOutputFolder.mkdir();
				  	
			  		// Open the input streams for the remote file 
				    String outputFile = this.outputFolderLocation + "\\CodedData\\" + inputFolder2.getName() +
				    			"\\" + listOfFiles[j].getName();
				    fOut = new FileOutputStream(outputFile);
				
				    // Open the output streams for saving this file on disk
			   		char[] data = codedData.toCharArray();
			   		for (int k = 0; k < data.length; k++) {
			   			fOut.write(data[k]);
			   		}
				    
			    } catch (Exception e) { //Catch exception if any
		          System.err.println("Error: " + e.getMessage());
		        } finally {
		         	try{
		           		fOut.flush(); 
		           		fOut.close();      
		         	} catch(Exception e){e.printStackTrace();}
		        }
		    }
	    }
	}
	
	//Utility function to add line breakers after 20 words
	private String addLineBreakers(String str) {
		final int wordsPerLine = 20;
		String newStr = "";
        int wordCount = 0;
        
        StringTokenizer wordTokenizer = new StringTokenizer(str);
	    while (wordTokenizer.hasMoreTokens()) {
	    	newStr += wordTokenizer.nextToken() + " ";
	    	wordCount++;
	    	if (wordCount % wordsPerLine == 0)
	    		newStr += "\n";
	    }  	
		return newStr.trim(); 
	}
}


