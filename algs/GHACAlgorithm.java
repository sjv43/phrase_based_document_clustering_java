package com.doccluster.algs;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GHACAlgorithm {
	private int numOfDocs;
	private float[][] simMatrix;
	private final int maxIterations = 10000;
	public List<Set<Integer>> clusters; //computed cluster structure
	
	public GHACAlgorithm (float[][] simMatrix) {
		this.simMatrix = simMatrix;
		this.numOfDocs = simMatrix.length;
		
		//Initializing the clusters
		this.clusters = new ArrayList<Set<Integer>>();
		Set<Integer> initCluster;
		for (int i = 0; i < this.numOfDocs; i++) {
			initCluster = new HashSet<Integer>();
			initCluster.add(i);
			this.clusters.add(initCluster);
		}
		int totIterations = runGHACAlgorithm();
		System.out.println("Total iterations = " + totIterations);
	}
	
	private int runGHACAlgorithm() {
		int iterations = 0, diff;
		
        do {
        	iterations++;
            diff = step();
            //System.out.println("Iteration #" + iterations + ", changes = " + diff);
        } while (diff > 0 && iterations < this.maxIterations);
        
        return iterations;
	}
	
	//Method does one iteration
    private int step() {
        double similarity = 0;
        double currSim;

        Set<Integer> m1 = null, m2 = null;

        for (Set<Integer> c1 : this.clusters) {
            for (Set<Integer> c2 : this.clusters) {
                if (c1.equals(c2)) continue;
                currSim = getSimilarity(c1, c2);
                if (currSim > similarity) {
                	similarity = currSim;
                    m1 = c1;
                    m2 = c2;
                }
            }
        }
        
        if (m1 != null && m2 != null) {
        	Set<Integer> merge = new HashSet<Integer>();
        	merge.addAll(m1);
        	merge.addAll(m2);
        	
        	clusters.remove(m1);
        	clusters.remove(m2);
        	clusters.add(merge);

            return 1;
        } else {
            return 0;
        }
    }
    
    private double getSimilarity(Set<Integer> c1, Set<Integer> c2) {
        double simTotal = 0;

        for (Integer doc1 : c1) {
            for (Integer doc2 : c2) {
            	int i = doc1.intValue(), j = doc2.intValue();
            	double currSimValue = (j <= i) ? this.simMatrix[i][j] : this.simMatrix[j][i];
            	simTotal += currSimValue;
            }
        }
        double avgSimilarity = simTotal / (c1.size() * c2.size());
        
        return avgSimilarity;
    }
    
    public void outputClusters (String outputFile) {
 	    FileOutputStream fOut = null;
	    try {		  	
	  		// Open the input streams for the remote file 
		    fOut = new FileOutputStream(outputFile);
		
		    // Open the output streams for saving this file on disk
		    for (int i = 0; i < this.clusters.size(); i++) {
		    	
		    	char[] data = this.clusters.get(i).toString().toCharArray();
		    	for (int k = 0; k < data.length; k++) {
		   			fOut.write(data[k]);
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
