package com.doccluster.algs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusterDiagnostics {
	private List<Set<Integer>> calculatedClusters; //computed cluster structure
	private List<Set<Integer>> correctClusters; //correct cluster structure
	private int calculatedClustersSize;
	private int correctClustersSize;
	private int N;
	public double Fmeasure;
	public double Purity;
	public double Entropy;
	
	public ClusterDiagnostics() {
	}
	
	public void doClusterDiagnostics 
		(List<Set<Integer>> calculatedClusters, List<Set<Integer>> correctClusters) {
		
		this.calculatedClusters = calculatedClusters;
		this.correctClusters = correctClusters;
		this.calculatedClustersSize = this.calculatedClusters.size();
		this.correctClustersSize = this.correctClusters.size();
		
		this.N = 0;
		for (int i = 0; i < this.correctClustersSize; i++) {
			this.N += this.correctClusters.get(i).size();
		}
		
		calculateFmeasure();
		calculatePurity();
		calculateEntropy();
	}
	
	//Fmeasure computation
	public void calculateFmeasure() {
		this.Fmeasure = 0;
		for (int i = 0; i < this.correctClustersSize; i++) {
			double FMeasureMax_i = 0;
			for (int j = 0; j < this.calculatedClustersSize; j++) {
				
				Set<Integer> intersectionSet = new HashSet<Integer>(this.calculatedClusters.get(j));
				intersectionSet.retainAll(this.correctClusters.get(i));
				
				double rec_ij = (double)intersectionSet.size()/this.correctClusters.get(i).size();
				double prec_ij = (double)intersectionSet.size()/this.calculatedClusters.get(j).size();

				if (rec_ij != 0 || prec_ij != 0) {
					double currFmeasure = (2 * rec_ij * prec_ij)/(rec_ij + prec_ij);			
					if (currFmeasure > FMeasureMax_i)
						FMeasureMax_i = currFmeasure;
				}
			}
			this.Fmeasure += (double)this.correctClusters.get(i).size()/this.N * FMeasureMax_i;
		}				
	}
	
	//Purity computation
	public void calculatePurity() {
		this.Purity = 0;
		for (int j = 0; j < this.calculatedClustersSize; j++) {
			double PrecisionMax_j = 0;
			for (int i = 0; i < this.correctClustersSize; i++) {
				
				Set<Integer> intersectionSet = new HashSet<Integer>(this.calculatedClusters.get(j));
				intersectionSet.retainAll(this.correctClusters.get(i));

				double currPrecision_ij = (double)intersectionSet.size()/this.calculatedClusters.get(j).size();

				if (currPrecision_ij > PrecisionMax_j)
					PrecisionMax_j = currPrecision_ij;
			}
			this.Purity += (double)this.calculatedClusters.get(j).size()/this.N * PrecisionMax_j;
		}	
	}
	
	//Entropy computation
	private void calculateEntropy() {
		if (this.calculatedClustersSize > 1) {
			this.Entropy = 0;
			for (int j = 0; j < this.calculatedClustersSize; j++) {
				double plogpSum = 0;
				for (int i = 0; i < this.correctClustersSize; i++) {
					
					Set<Integer> intersectionSet = new HashSet<Integer>(this.calculatedClusters.get(j));
					intersectionSet.retainAll(this.correctClusters.get(i));
					
					double p_ij = (double)intersectionSet.size()/this.calculatedClusters.get(j).size();
					if (p_ij > 0)
						plogpSum += p_ij * Math.log(p_ij);
				}
				this.Entropy += (double)this.calculatedClusters.get(j).size()/this.N * plogpSum;
			}
			this.Entropy = -1.0/Math.log(this.calculatedClustersSize) * this.Entropy;
		}
		else
			this.Entropy = Double.NaN;
	}	
	
	//Outputting the diagnostic measures
	public String toString() {
		java.text.DecimalFormat df = new java.text.DecimalFormat("0.0000");
		String str = "[Fmeasure = " + df.format(Math.abs(this.Fmeasure)) +
		             ", Purity = " + df.format(Math.abs(this.Purity)) +
		             ", Entropy = " + df.format(Math.abs(this.Entropy)) + "]";
		return str;
	}
}
