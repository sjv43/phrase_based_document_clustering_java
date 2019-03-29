package com.doccluster.algs;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class KNNAlgorithm {
	private int kValue; //number of nearest neighbors
	private int numOfDocs; 
	private float[][] simMatrix;
	private final int maxIterations = 10000;
	public List<Set<Integer>> clusters; //computed cluster structure
	
	public KNNAlgorithm (float[][] simMatrix) {
		//if not Kvalue is specified the default value of 10 is used
		this(10, simMatrix);
	}

	public KNNAlgorithm (int kValue, float[][] simMatrix) {
		this.kValue = kValue;
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
		
		int totIterations = runKNNAlgorithm();
		System.out.println("K-value = " + this.kValue + 
					", Total iterations = " + totIterations);
	}
	
	private int runKNNAlgorithm() {
		int iterations = 0, diff;
		
        do {
        	iterations++;
            diff = step();
            //System.out.println("Iteration #" + iterations + ", changes = " + diff);
        } while (diff > 0 && iterations < this.maxIterations);
        
        //Removing all the empty clusters
        List<Set<Integer>> updatedClusterList = new ArrayList<Set<Integer>>();
        for (int i = 0; i < this.clusters.size(); i++) {
        	if (!this.clusters.get(i).isEmpty())
        		updatedClusterList.add(this.clusters.get(i));
        		
        }
        this.clusters = updatedClusterList;
        
        return iterations;
	}
	
	//Method calculates one iteration
	private int step() {
		int diff = 0, newdocCluster, olddocCluster;
		for (int i = 0; i < this.numOfDocs; i++) {
			olddocCluster = getCurrClusterForDoc(i);
			newdocCluster = findNewClusterForDoc(i);
			
			if (newdocCluster != olddocCluster) {
				this.clusters.get(olddocCluster).remove(i);
				this.clusters.get(newdocCluster).add(i);
				diff++;
			}
		}
		return diff;
	}
	
	//This method finds the new cluster for each document.
	//First, the K nearest neighbors are found and then the document is
	//put in that cluster which is most common among the K nearest neighbors
	private int findNewClusterForDoc (int doc) {
		//Finding the K nearest neighbors
		Map<Double, List<Integer>> docSimValues = 
			new TreeMap<Double, List<Integer>>(Collections.reverseOrder());
		for(int i = 0; i < this.numOfDocs; i++) {
			if (i != doc) {
				double currSimValue = (i <= doc) ? this.simMatrix[doc][i] : this.simMatrix[i][doc];
				List<Integer> ind = docSimValues.get(currSimValue);
			    if(ind == null){
			        ind = new ArrayList<Integer>();
			        docSimValues.put(currSimValue, ind);
			    }
			    ind.add(i);
			}	   
		}

		List<Integer> closestDocs = new ArrayList<Integer>();
		for(List<Integer> arr : docSimValues.values()) {
			closestDocs.addAll(arr);
		}

		//Calculating the frequency of clusters in K nearest neighbors
		Map<Integer, Integer> kNearestClusters = new HashMap<Integer, Integer>();
		for (int i = 0; i < this.kValue; i++) {
			int currDocCluster = getCurrClusterForDoc(closestDocs.get(i));
			if (kNearestClusters.containsKey(currDocCluster)) 
				kNearestClusters.put(currDocCluster, kNearestClusters.get(currDocCluster) + 1);
			else
				kNearestClusters.put(currDocCluster, 1);
		}
		
		//Finding the cluster with maximum votes 
		int maxVote = Collections.max(kNearestClusters.values());
		int maxVoteCluster = -1;
		for (Map.Entry<Integer, Integer> entry : kNearestClusters.entrySet()) {
			if (entry.getValue() == maxVote) {
				maxVoteCluster = entry.getKey();
				break;
			}	
		}
				
		return maxVoteCluster;	
	}
	
	//Utility method to find the current cluster for each document
	private int getCurrClusterForDoc  (int doc) {
		for (int i = 0; i < this.clusters.size(); i++) {
			if (this.clusters.get(i).contains(doc))
				return i;
		}
		return -1;
	}
	
	//Method outputs the cluster
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
