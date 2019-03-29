package com.doccluster.algs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.doccluster.suffixtree.WordSuffixTree;

public class SimilarityMatrix {
	private int numOfDocs;
	private int numOfWords;
	private String calcMethod;
	private String similarityType;
	public float[][] matrix; //the array stores the similarity matrix
	
	//Constructor for the class
	//argument "method" tells whether it is phraseBased or wordBased calculation
	public SimilarityMatrix (WordSuffixTree wst, String method, String similarityType) {
		this.calcMethod = method;
		this.similarityType = similarityType;
		this.numOfDocs = wst.numOfDocs;
		this.numOfWords = wst.wordList.size();
		
		matrix = new float[this.numOfDocs][];
		for (int i = 0; i < this.numOfDocs; i++)
			matrix[i] = new float[i+1];
		
		if (method.equals("phraseBased"))
			this.SimilarityMatrixPhraseBased(wst);
		else if (method.equals("wordBased"))
			this.SimilarityMatrixWordBased(wst);
		else
			throw new UnsupportedOperationException("Unknown method: " + method + "\n");
	}
	
	//Similarity matrix with phrase based calculations
	private void SimilarityMatrixPhraseBased (WordSuffixTree wst) {	
		//Calculating the weight vector for each document
		float[][] docWts = new float[this.numOfDocs][wst.nodeDocumentList.size()];
		for (int i = 0; i < this.numOfDocs; i++) {
			for (int j = 0; j < wst.nodeDocumentList.size(); j++) {
				Short tf = wst.nodeDocumentList.get(j).get(i);
				if (tf == null)
					docWts[i][j] = 0;
				else {
					short df = (short) wst.nodeDocumentList.get(j).keySet().size();
					docWts[i][j] = calcWeight(tf, df);
				}
			}
		}
			
	    int percentageDone = 0;
		System.out.print("%Done: " + percentageDone + "..");
		percentageDone += 10;
		
		//Computing the cosine similarity measures
		for (int i = 0; i < this.numOfDocs; i++) {			
			for (int j = 0; j <= i; j++) {
				if (i == j)
					matrix[i][j] = 1;
				else {								
					float cosSim = calcCosineSimilarity (docWts[i], docWts[j]);
					matrix[i][j] = cosSim;
				}
			}
			
			if (percentageDone <= (int)Math.pow(10.0 * i/this.numOfDocs, 2)) {
				System.out.print(percentageDone + "..");
				percentageDone += 10;
			}
		}		
		System.out.println("100");
	}
	
	//Similarity matrix with word based calculations
	private void SimilarityMatrixWordBased (WordSuffixTree wst) {	
		//wordFreqList stores the word frequency in each documents
		List<Map<Integer, Short>> wordFreqList = new ArrayList<Map<Integer, Short>>();
		for (int i = 0; i < this.numOfDocs; i++)
			wordFreqList.add(new HashMap<Integer, Short>());
		
		//docFreqList has the document frequency of each word
		//for later calculations
		List<Set<Integer>> docFreqList = new ArrayList<Set<Integer>>();
		for (int i = 0; i < this.numOfWords; i++)
			docFreqList.add(new HashSet<Integer>());
				
		File inputFolder = new File(wst.inputFolderLocation);
	    File[] listOfFolders = inputFolder.listFiles();
	    
	    int docId = 0;
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
		        	//Read File Line By Line
		        	while ((strLine = br.readLine()) != null)   {
		          		// Print the content on the console
		        		fileData = fileData + " " + strLine.trim();
		        	}
		        	fileData = fileData.trim();
		        	
		    	    //Close the input stream
		    	    inStream.close();
		    	    
		        } catch (Exception e) { //Catch exception if any
		          System.err.println("Error: " + e.getMessage());
		        }
		                
		        StringTokenizer wordCodeTokenizer = new StringTokenizer(fileData);
			    while (wordCodeTokenizer.hasMoreTokens()) {
			    	int nextWordCode = Integer.parseInt(wordCodeTokenizer.nextToken());
			    	
			    	Short value = wordFreqList.get(docId).get(nextWordCode);
			    	value = (short) ((value == null) ? 1 : (value + 1));
			    	wordFreqList.get(docId).put(nextWordCode, value);
			    	
			    	docFreqList.get(nextWordCode).add(docId);
			    }
			    
			    docId++;
		    }
	    }
	    
	    //Calculating the weight vectors for each document
	    float[][] docWts = new float[this.numOfDocs][this.numOfWords];
		for (int i = 0; i < this.numOfDocs; i++) {
			for (int j = 0; j < this.numOfWords; j++)
				docWts[i][j] = 0;
				
			for (Map.Entry<Integer, Short> entry : wordFreqList.get(i).entrySet()){
					int wordId = entry.getKey();
					Short tf = entry.getValue();
					Short df = (short) docFreqList.get(wordId).size();
					docWts[i][wordId] = calcWeight(tf, df);
			}				
		}
				
	    int percentageDone = 0;
		System.out.print("%Done: " + percentageDone + "..");
		percentageDone += 10;
		
		//Populating the similarity matrix
		for (int i = 0; i < this.numOfDocs; i++) {			
			for (int j = 0; j <= i; j++) {
				matrix[i][j] = (i == j) ? 1 : calcSimilarity (docWts[i], docWts[j]);
			}
			
			if (percentageDone <= (int)Math.pow(10.0 * i/this.numOfDocs, 2)) {
				System.out.print(percentageDone + "..");
				percentageDone += 10;
			}
		}		
		System.out.println("100");
	}
	
	//tf-idf method for calculating the weights
	private float calcWeight (short tf, short df) {
		float weight = (float) ((1 + Math.log(tf))*Math.log(1 + (float)this.numOfDocs/df));
		return weight;
	}
	
	//Similarity Calculations
	private float calcSimilarity (float[] d1, float[] d2) {		
		if (this.similarityType.equals("cosine"))
			return calcCosineSimilarity (d1, d2);
		else if (this.similarityType.equals("euclidean"))
			return calcEuclideanSimilarity (d1, d2);
		else if (this.similarityType.equals("correlation"))
			return calcCorrelationSimilarity (d1, d2);
		else
			throw new UnsupportedOperationException("Unknown similarity type: " + 
								this.similarityType + "\n");
	}
	
	//cosine similarity calculations
	private float calcCosineSimilarity (float[] d1, float[] d2) {
		float X, Y, sumXY = 0, sumX2 = 0, sumY2 = 0;
		
		for (int i = 0; i < d1.length; i++) {
			X = d1[i];
			Y = d2[i];
			sumXY += (X * Y);
			sumX2 += (X * X);
			sumY2 += (Y * Y);
		}

		if (sumX2 == 0 || sumY2 == 0)
			return 0;
		else
			return (float)(sumXY/Math.sqrt(sumX2 * sumY2));
	}
	
	//Euclidean similarity calculations
	private float calcEuclideanSimilarity (float[] d1, float[] d2) {
		float sum = 0;
		
		for (int i = 0; i < d1.length; i++) 
			sum = sum + (d1[i] - d2[i])*(d1[i] - d2[i]);

		return (float)(1/(1 + Math.sqrt(sum)));
	}
	
	//Correlation similarity calculations
	private float calcCorrelationSimilarity (float[] d1, float[] d2) {
		float X, Y, sumX = 0, sumY = 0, meanX, meanY, sumXY = 0, sumX2 = 0, sumY2 = 0;
		
		for (int i = 0; i < d1.length; i++) {
			sumX += d1[i];
			sumY += d2[i];
		}
		meanX = sumX/d1.length;
		meanY = sumY/d2.length;
		
		for (int i = 0; i < d1.length; i++) {
			X = d1[i];
			Y = d2[i];       
			sumXY += (X - meanX)*(Y - meanY);
			sumX2 += (X - meanX)*(X - meanX);
			sumY2 += (Y - meanY)*(Y - meanY);
		}
		
		if (sumX2 == 0 || sumY2 == 0)
			return Float.NaN;
		else 
			return (float)(1 - sumXY/Math.sqrt(sumX2*sumY2));
	}
	
	//Output the similarity matrix
	//Default precision is four decimal points
	public void outputSimilarityMatrix (String outputFolderLocation) {
		this.outputSimilarityMatrix(outputFolderLocation, 4);
	}
	
	public void outputSimilarityMatrix (String outputFolderLocation, int decimalPoints) {	
		String format = "0.";
		for (int i = 0; i < decimalPoints; i++)
			format += "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat(format);
		
		
  	    FileOutputStream fOut = null;
	    try {
		    String newOutputFolderName = outputFolderLocation + "\\OtherOutputFiles";
		  	File newOutputFolder = new File(newOutputFolderName);
		  	if (!newOutputFolder.exists()) newOutputFolder.mkdir();
		  	
		  	
	  		// Open the input streams for the remote file 
		    String outputFile = outputFolderLocation + "\\OtherOutputFiles\\" + this.calcMethod + "SimMatrix";
		    fOut = new FileOutputStream(outputFile);
		
		    // Open the output streams for saving this file on disk
		    for (int i = 0; i < this.numOfDocs; i++) {
		    	for (int j = 0; j < this.numOfDocs; j++) {
			   		
		    		float currSimValue = (j <= i) ? matrix[i][j] : matrix[j][i];
		    		char[] data = df.format(currSimValue).toCharArray();
			   		for (int k = 0; k < data.length; k++) {
			   			fOut.write(data[k]);
			   		}
			   		fOut.write('\t');
		    	}
		    	fOut.write('\n');
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
