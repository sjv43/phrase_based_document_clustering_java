package com.doccluster.algs;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.doccluster.suffixtree.WordSuffixTree;

public class STCAlgorithm {
	private int Kvalue; //the top K clusters
	private final int maxIterations = 10000;
	private WordSuffixTree wst;
	public List<Set<Integer>> clusters;
	
	public STCAlgorithm (WordSuffixTree wst) {
		//the default Kvalue is the total number of nodes
		this(wst, wst.nodeDocumentListScores.size());
	}
	
	public STCAlgorithm (WordSuffixTree wst, int Kvalue) {
		this.Kvalue = Kvalue;	
		this.wst = wst;
		this.clusters = new ArrayList<Set<Integer>>();
		
		getBaseClusters();
		int totIterations = getFinalClusters();
		
		System.out.println("K-value = " + this.Kvalue + 
					", Total iterations = " + totIterations);
	}
	
	//Finds the base clusters
	private void getBaseClusters () {
		//Finding the top K clusters
		List<Integer> topKClusters = new ArrayList<Integer>();
		if (this.Kvalue == wst.nodeDocumentListScores.size()) {
			for(int i = 0; i < this.Kvalue; i++)
				topKClusters.add(i);
		} else {
			Map<Float, List<Integer>> clusterScores = 
				new TreeMap<Float, List<Integer>>(Collections.reverseOrder());
			for(int i = 0; i < this.wst.nodeDocumentListScores.size(); i++) {
				List<Integer> ind = clusterScores.get(this.wst.nodeDocumentListScores.get(i));
			    if(ind == null){
			        ind = new ArrayList<Integer>();
			        clusterScores.put(this.wst.nodeDocumentListScores.get(i), ind);
			    }
			    ind.add(i);
			}

			for(List<Integer> arr : clusterScores.values()) {
				topKClusters.addAll(arr);
			}
		}

		Set<Integer> newCluster;
		for (int i = 0; i < this.Kvalue; i++) {
			newCluster = new HashSet<Integer>();
			newCluster.addAll(this.wst.nodeDocumentList.get(topKClusters.get(i)).keySet());
			this.clusters.add(newCluster);
		}
	}
	
	//Finds the final clustsers
	private int getFinalClusters () {
		int iterations = 0, diff;
		System.out.println("Iteration #0" + ", cluster size = " + this.clusters.size());
		
        do {
        	iterations++;
            diff = step();
            System.out.println("Iteration #" + iterations + ", cluster size = " + this.clusters.size());
        } while (diff > 0 && iterations < this.maxIterations);
        
        
        return iterations;
	}
	
	//Method to computer one iteration
	private int step() {	
		//Adjacency matrix for finding connected clusters
		List<List<Integer>> adjMatrix = new ArrayList<List<Integer>>();
		List<Integer> currCluster;
		for (int i = 0; i < this.clusters.size(); i++) {
			currCluster = new ArrayList<Integer>();
			for (int j = 0; j < this.clusters.size(); j++) {
				if (isConnected(i, j))
					currCluster.add(j);
			}
			adjMatrix.add(currCluster);
		}
		
		//marked array indicates if the each cluster has already been taken
		boolean[] marked = new boolean[this.clusters.size()];
		for (int i = 0; i < marked.length; i++)
			marked[i] = false;
		
		//Finding all connected clusters
		List<Set<Integer>> newClusterList = new ArrayList<Set<Integer>>();
		for (int i = 0; i < this.clusters.size(); i++) {
			if (!marked[i])
				newClusterList.add(findConnectedClusters(i, adjMatrix, marked));
		}
		
		//Reduction in cluster size
		int diff = this.clusters.size() - newClusterList.size();
        this.clusters = newClusterList;
        
		return diff;
	}
	
	//Recursive function to find all connected clusters
	private Set<Integer> findConnectedClusters(int clusterId, List<List<Integer>> adjMatrix, boolean[] marked){
		marked[clusterId] = true;
		List<Integer> currClusterConnections = adjMatrix.get(clusterId);
		for (int i = 0; i < this.clusters.size(); i++) {
			if (currClusterConnections.contains(i) && !marked[i]) {
				Set<Integer> newCluster = new HashSet<Integer>(this.clusters.get(clusterId));
				newCluster.addAll(findConnectedClusters(i, adjMatrix, marked));
				return newCluster;
			}
		}
		return this.clusters.get(clusterId);
	}
	
	//Function finds out if two clusters are connected
	private boolean isConnected (int cluster_i, int cluster_j) {
		Set<Integer> intersect = new HashSet<Integer>(this.clusters.get(cluster_i));
		intersect.retainAll(this.clusters.get(cluster_j));
		
		Set<Integer> union = new HashSet<Integer>(this.clusters.get(cluster_i));
		union.addAll(this.clusters.get(cluster_j));
		
		double JaccardCoeff = (double)intersect.size()/union.size();
		
		return (JaccardCoeff > 0.5);
	}
	
	//Outputting the clusters
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
