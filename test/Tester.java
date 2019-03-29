package com.doccluster.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.doccluster.algs.*;
import com.doccluster.cleaner.DataCleaner;
import com.doccluster.suffixtree.WordSuffixTree;

public class Tester {

	public static void main(String[] args) {
		
		//Specifying the input-output folder locations
		//and the location of the stop words file
		
		/*
		final String   inputFolder = "C:\\DataFiles\\mini_newsgroups",
		              outputFolder = "C:\\DataFiles\\OutputData",
		             stopWordsFile = "C:\\DataFiles\\stopwords-final.txt",
		            similarityType = "correlation",
		            	docKeyWord = "Lines: "; 
		final int numOfClusters = 5;
		final int numOfDocsPerCluster = 100;
		*/
		
		final String inputFolder = args[0].trim();
		final String outputFolder = args[1].trim();
		final String stopWordsFile = args[2].trim();
		final String similarityType = args[3].trim();
		final int numOfClusters = Integer.parseInt(args[4].trim()); 
		final int numOfDocsPerCluster = Integer.parseInt(args[5].trim());
		final String docKeyWord = (args.length >= 7) ? args[6] : "";
		
		
		System.out.println("\nProgram started...");
		int heapMaxSize = (int) (Runtime.getRuntime().maxMemory()/1000000);
		System.out.println("Maximum available memory heap size = " + heapMaxSize + "mb\n");
		
		//Pre-processing the data and outputting the coded data
		double startTime, endTime;
		System.out.println("Started cleaning data...");
		startTime = System.currentTimeMillis();
		DataCleaner dc = new DataCleaner(inputFolder, outputFolder, stopWordsFile, docKeyWord);
		dc.doCleaning();
		endTime = System.currentTimeMillis();
		System.out.println("Finished cleaning data... (" + (endTime - startTime) + " ms)\n");

		//Constructing the suffix tree
		System.out.println("Started suffix tree construction...");
		startTime = System.currentTimeMillis();
		WordSuffixTree wordST = new WordSuffixTree(outputFolder, dc.allWords);
		endTime = System.currentTimeMillis();
		System.out.println("Finished suffix tree construction... (" + (endTime - startTime) + " ms)\n");
		
		//Constructing the phrase-based similarity matrix
		System.out.println("Started phrase-based similarity matrix construction...");
		startTime = System.currentTimeMillis();
		SimilarityMatrix phraseSimMatrix = new SimilarityMatrix(wordST, "phraseBased", similarityType);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting similarity matrix...");
		phraseSimMatrix.outputSimilarityMatrix(outputFolder, 4);
		System.out.println("Finished phrase-based similarity matrix construction... (" + (endTime - startTime) + " ms)\n");
		
		//Constructing the word-based similarity matrix
		System.out.println("Started word-based similarity matrix construction...");
		startTime = System.currentTimeMillis();
		SimilarityMatrix wordSimMatrix = new SimilarityMatrix(wordST, "wordBased", similarityType);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting similarity matrix...");
		wordSimMatrix.outputSimilarityMatrix(outputFolder, 4);
		System.out.println("Finished word-based similarity matrix construction... (" + (endTime - startTime) + " ms)\n");
		
		//Document clustering using the STC algorithm
		System.out.println("Started STC algorithm...");
		startTime = System.currentTimeMillis();
		STCAlgorithm stcAlg = new STCAlgorithm(wordST);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting clusters...");
		stcAlg.outputClusters(outputFolder + "\\OtherOutputFiles\\stcOutput");
		System.out.println("Finished STC algorithm... (" + (endTime - startTime) + " ms)\n");
		
		//Document clustering using the STC algorithm with the top 10 clusters
		System.out.println("Started STC-10 algorithm...");
		startTime = System.currentTimeMillis();
		STCAlgorithm stc10Alg = new STCAlgorithm(wordST, 10);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting clusters...");
		stc10Alg.outputClusters(outputFolder + "\\OtherOutputFiles\\stc10Output");
		System.out.println("Finished STC-10 algorithm... (" + (endTime - startTime) + " ms)\n");
		
		//Document clustering using the GHAC algorithm with phrased-based similarity
		System.out.println("Started GHAC with phrased-based similarity...");
		startTime = System.currentTimeMillis();
		GHACAlgorithm ghacAlgPhrase = new GHACAlgorithm(phraseSimMatrix.matrix);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting clusters...");
		ghacAlgPhrase.outputClusters(outputFolder + "\\OtherOutputFiles\\ghacOutputPhrase");
		System.out.println("Finished GHAC with phrased-based similarity... (" + (endTime - startTime) + " ms)\n");
		
		//Document clustering using the GHAC algorithm with word-based similarity
		System.out.println("Started GHAC with word-based similarity...");
		startTime = System.currentTimeMillis();
		GHACAlgorithm ghacAlgWord = new GHACAlgorithm(wordSimMatrix.matrix);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting clusters...");
		ghacAlgWord.outputClusters(outputFolder + "\\OtherOutputFiles\\ghacOutputWord");
		System.out.println("Finished GHAC with word-based similarity... (" + (endTime - startTime) + " ms)\n");
		
		//Document clustering using the KNN algorithm with phrase-based similarity
		System.out.println("Started KNN with phrase-based similarity...");
		startTime = System.currentTimeMillis();
		final int K = 10;
		KNNAlgorithm knnAlg = new KNNAlgorithm(K, phraseSimMatrix.matrix);
		endTime = System.currentTimeMillis();
		System.out.println("Outputting clusters...");
		knnAlg.outputClusters(outputFolder + "\\OtherOutputFiles\\knnOutput");
		System.out.println("Finished KNN with phrase-based similarity... (" + (endTime - startTime) + " ms)\n");
		
		//Constructing the "correct" cluster structure
		//for diagnostics calculations
		List<Set<Integer>> correctClusters = new ArrayList<Set<Integer>>();
		Set<Integer> tempCluster;
		int docId = 0;
		for (int i = 0; i < numOfClusters; i++) {
			tempCluster = new HashSet<Integer>();
			for (int j = 0; j < numOfDocsPerCluster; j++) {
				tempCluster.add(docId);
				docId++;
			}
			correctClusters.add(tempCluster);
		}
		
		//Calculating the clustering diagnostics
		ClusterDiagnostics clusterDiag = new ClusterDiagnostics();
		System.out.println("Cluster diagnostics:");
		clusterDiag.doClusterDiagnostics(stcAlg.clusters, correctClusters);
		System.out.println("STC:\t\t" + clusterDiag.toString());
		clusterDiag.doClusterDiagnostics(stc10Alg.clusters, correctClusters);
		System.out.println("STC-10:\t\t" + clusterDiag.toString());
		clusterDiag.doClusterDiagnostics(ghacAlgPhrase.clusters, correctClusters);
		System.out.println("GHAC (phrase):\t" + clusterDiag.toString());
		clusterDiag.doClusterDiagnostics(ghacAlgWord.clusters, correctClusters);
		System.out.println("GHAC (word):\t" + clusterDiag.toString());
		clusterDiag.doClusterDiagnostics(knnAlg.clusters, correctClusters);
		System.out.println("KNN (phrase):\t" + clusterDiag.toString() + "\n");
		
		System.out.println("Done.");
	}

}
