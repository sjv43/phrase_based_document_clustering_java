package com.doccluster.suffixtree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class WordSuffixTree extends UkkonenSuffixTree {

	public String inputFolderLocation;
	public List<String> wordList;
	public int numOfDocs;
	
    public WordSuffixTree(String inputFolderLocation, List<String> wordList) {
        this.inputFolderLocation = inputFolderLocation + "\\CodedData";
        this.wordList = wordList;
        
        if (wordList.size() > 65535) {
            throw new UnsupportedOperationException(
                    "Current implementation does not allow for Texts \n" +
                    "with a number of unique words above 65535.");
        }
        else 
        	constructTree();
    }
    
    private void constructTree() {
    	File inputFolder = new File(this.inputFolderLocation);
	    File[] listOfFolders = inputFolder.listFiles();
	    
	    int documentCount = 0;
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
		        	        
		        StringBuilder builder = new StringBuilder();
		        StringTokenizer wordCodeTokenizer = new StringTokenizer(fileData);
			    while (wordCodeTokenizer.hasMoreTokens()) {
			    	String nextWordCode = wordCodeTokenizer.nextToken();
			    	char codeChar = (char)Integer.parseInt(nextWordCode);
			    	builder.append(codeChar);
			    }  	
		       
			    documentCount++;
		        addSequence(builder.toString(), documentCount, false);
		    }
	    }
	    this.numOfDocs = documentCount;
	    constructNodeDocumentList();
	    
	    System.out.println("Document Count = " + this.numOfDocs);
	    System.out.println("Total Unique Word Count = " + this.wordList.size());
		System.out.println("Number of nodes used for analysis = " + this.nodeDocumentList.size());
    }
    
    // these are for dot output
    private int count = 1;
    
    private String translate(CharSequence label, boolean cut) {
    	   	
        String res = "";
        int orig = label.length();
        if (!label.equals("root")) {
            if (cut)
                label = label.toString().substring(0, Math.min(10, label.length()));
            char[] l = label.toString().toCharArray(); 
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < l.length; j++) {
            	int wordCode = (int) l[j];
                builder.append(wordList.get(wordCode) + " ");
            }
            res = builder.toString().trim();
        }
        if (cut && orig > 10)
            res += " [...]";
        return res;
    }
    
    /**
     * Minimal run of WordSuffixTree. For further tests see
     * {@link TestWordSuffixTree}.
     * 
     * @param args
     *            Not used
     */
    
    @Override
    public CharSequence getEdgeLabel(SuffixNode child) {
        return translate(super.getEdgeLabel(child), true);
    }

    @Override
    public CharSequence getLabel(SuffixNode node) {
        return translate(super.getLabel(node), true);
    }

    /**
     * Writes the tree as a dot text file to disk
     * 
     * <p/> TODO get this out of here, and into a class DotUtils, using a thin
     * interface like SuffixNode
     * 
     * @param root
     *            The root {@link SuffixNode} to export
     * @param dest
     *            The location in the file system to write to (eg. "out.dot")
     */
    public void exportDot(String dest) {
        try {
            String string = dest;
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(string)));
            fileWriter.write("Suffix Tree Representation\n");
            printDotBody(root, null, false, fileWriter, 0);
            fileWriter.write("}");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<SuffixNode> printDotBody(SuffixNode root,
            ArrayList<SuffixNode> list, boolean leavesOnly, BufferedWriter writer,
            int depth) throws IOException {
        tab(writer, depth);
        root.id = count;
        if (root.parent != null)
            writer.write(root.parent.id + "->");
        writer.write("" + count);
        writer.write("[label=\"" + getEdgeLabel(root).toString().trim()
                + ", Text: " + root.textNumber + ", Suffix: "
                + ((SimpleNode) root).suffixIndex + "\"];\n");
        Iterator<SuffixNode> iterator;
        if (list == null) {
            list = new ArrayList<SuffixNode>();
            count = 1;
        }
        if (!leavesOnly || (leavesOnly && root.isTerminal()))
            list.add(root);
        if (!root.isTerminal()) {
            iterator = root.getChildren().values().iterator();
            // writer.write("\n");
            depth = depth + 1;
//            last = count;
            while (iterator.hasNext()) {
                SuffixNode next = (SuffixNode) iterator.next();
                count++;
                list = printDotBody(next, list, leavesOnly, writer, depth);
            }
        }
        return list;
    }

    /**
     * @param writer
     *            The writer to write tabs to
     * @param depth
     *            The current depth in the tree
     * @throws IOException
     *             If writing goes wrong
     */
    private void tab(BufferedWriter writer, int depth) throws IOException {
        for (int i = 0; i <= depth; i++) {
            writer.write("\t");
        }
    }
}
