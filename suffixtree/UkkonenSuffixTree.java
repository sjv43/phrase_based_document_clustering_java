package com.doccluster.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UkkonenSuffixTree {

    public static final char DEFAULT_TERM_CHAR = '$';
    private char terminationChar;
    SuffixNode root;
    public static final int TO_A_LEAF = -1;
    private int e;
    private CharSequence sequences;
    
    //A list to store the frequency of documents traversing
    //each internal node
    public List<Map<Integer, Short>> nodeDocumentList;
    
    //A list to store the cluster score of each internal node
    public List<Float> nodeDocumentListScores;
    
    /**
     * Describes the rule that needs to be applied after walking down a tree.
     * Put as a class variable because it can only return a single object (and I
     * don't want to extend Node any further. rule 1: ended up at a leaf. rule
     * 2: need to extend an internalNode. rule 3: would split an edge. rule 4:
     * ended up in the middle of an edge. rule 5: ended up at an InternalNode
     * 
     * Production 5 counts as rule 4 when adding a sequence, but the rules are
     * also used to when searching the tree.
     */
    private int rule;

    /**
     * Initializes a new <code>UkkonenSuffixTree</code> instance.
     */
    public UkkonenSuffixTree() {
        terminationChar = DEFAULT_TERM_CHAR;
        root = new SimpleNode();
        e = 0;
        sequences = "";
        nodeDocumentList = new ArrayList<Map<Integer, Short>>();
        nodeDocumentListScores = new ArrayList<Float>();
    }

    /**
     * Initializes a new <code>UkkonenSuffixTree</code> instance with a text.
     * 
     * @param seqs
     *            The text to add
     */
    public UkkonenSuffixTree(String seqs) {
        this();
        addSequence(seqs, 1, false);
    }

    /**
     * Add a sequence into the tree. If there are more sequences, they should be
     * separated by a terminationChar ($ by default). If none exist, it is
     * assumed that there is only 1 continuous sequence to be added.
     * 
     * @param seq
     *            the sequence/sequences to be added into the tree.
     * @param number
     *            The text number, for generalized trees
     * @param doNotTerminate
     *            whether we should terminate the sequence if it's
     *            non-terminated.
     */
    public void addSequence(String seq, int number, boolean doNotTerminate) {
        int i;
        int start, end;
        ArrayList<String> toBeAdded = new ArrayList<String>();
        Iterator<String> iterator;
        String subseq;

        if (seq == null || seq.length() == 0)
            return;

        // terminate the String if it's not terminated.
        if (!doNotTerminate && seq.charAt(seq.length() - 1) != terminationChar)
            seq = seq + terminationChar;

        // count how many termination Chars in in.
        start = 0;
        for (i = 0; seq.indexOf(terminationChar, i) != -1; i = seq.indexOf(
                terminationChar, i) + 1) {
            end = seq.indexOf(terminationChar, i);
            toBeAdded.add(seq.substring(start, end + 1));
        }

        iterator = toBeAdded.iterator();
        i = 0;
        while (iterator.hasNext()) {
            subseq = (String) iterator.next();
            addPreppedSequence(subseq, number);
            i++;
        }
    }

    /**
     * Add a single sequence into the tree.
     * 
     * @param seq
     *            a <code>String</code> value
     * @param number
     */
    private void addPreppedSequence(CharSequence seq, int number) {
        int i, gammaStart;
        int j = 0;
        SuffixNode oldNode = null, newNode;
        SuffixNode currentNode;
        boolean canLinkJump = false;

        // Puts i at the end of the previous sequences
        i = sequences.length();
        int k = i;
        j = i;

        sequences = sequences.toString() + seq.toString();

        currentNode = root;

        // phase i
        for (; i < sequences.length(); i++) {
            // System.out.println("Phase "+i);

            e += 1;
            // extension j;
            for (; j <= i; j++) {
                // System.out.println("extension "+j);

                // reset a couple of things...
                newNode = null;

                // find first node v at or above s[j-1,i] that is root or has a
                // suffixLink
                while (currentNode != root && currentNode.suffixLink == null
                        && canLinkJump)
                    currentNode = currentNode.parent;

                if (root == currentNode) {
                    currentNode = jumpTo(root, sequences, j, i + 1);
                } else {
                    if (canLinkJump)
                        currentNode = currentNode.suffixLink;
                    gammaStart = j + getPathLength(currentNode);

                    currentNode = jumpTo(currentNode, sequences, gammaStart,
                            i + 1);
                }

                if (rule == 1)
                    addPositionToLeaf(j, currentNode);
                if (rule == 2)
                    doRule2(currentNode, i, j, number, i - k);
                if (rule == 3) {
                    newNode = doRule3(currentNode, i, j, number, i - k);
                    currentNode = newNode;

                }

                if (rule == 1 || rule == 4 || rule == 5)
                    currentNode = currentNode.parent;

                if (oldNode != null) {
                    if (currentNode.isTerminal())
                        currentNode = currentNode.parent;

                    oldNode.suffixLink = currentNode;

                }
                oldNode = newNode;
                newNode = null;

                if (rule == 1 || rule == 4 || rule == 5) {
                    oldNode = null;
                    canLinkJump = false;
                    break;
                } else
                    canLinkJump = true;
            }// for phase i
        }// for extension j
        finishAddition();
    }

    /**
     * This method is used to walk down the tree, from a given node. The
     * <code>rule</code> variable can be used to check where the walk stopped.
     * Note that rule 3 means that the string used to walk down the tree does
     * not match (which is a bit different from the construction where rule 3
     * implies that only the last character doesn't match.
     * <p>
     * The String is encoded as a substring of a given source. This is done to
     * avoid replicating the string. To send walk down the string <code>x</code>
     * from the root, one would call walkTo(root,x,0,x.length()).
     * 
     * @param starting
     *            the root of the subtree we're walking down form.
     * @param source
     *            a superstring that contains the string we're using to walking
     *            down. source.subtring(from,to) should give the string we're
     *            walking down from.
     * @param from
     *            the start position (inclusive) of the target string in the
     *            source.
     * @param to
     *            the end position (exclusive) of the target string in the node.
     * @return a <code>SuffixNode</code> that the walk stopped at. If the walk
     *         stopped inside an edge. (check the rule variable to see where it
     *         stopped).
     */
    public SuffixNode walkTo(SuffixNode starting, String source, int from,
            int to) {
        SuffixNode currentNode;
        SuffixNode arrivedAt;
        CharSequence edgeLabel;

        currentNode = starting;
        arrivedAt = starting;
        while (from < to) {
            arrivedAt = (SuffixNode) currentNode.getChildren().get(
                    new Character(source.charAt(from)));
            if (arrivedAt == null) {
                from = to;
                arrivedAt = currentNode;
                rule = 2;
                break;
            }

            edgeLabel = getEdgeLabel(arrivedAt);
            if (edgeLabel.length() >= to - from) {
                if (edgeLabel.equals(source.substring(from, to))) {
                    // rule 1 or 5,
                    if (arrivedAt.isTerminal())
                        rule = 1;
                    else
                        rule = 5;
                }
                if (edgeLabel.subSequence(0, to - from).equals(
                        source.substring(from, to)))
                    rule = 4;
                else
                    rule = 3;
                from = to;
            } else if (source.subSequence(from, from + edgeLabel.length())
                    .equals(edgeLabel)) {
                from += edgeLabel.length();
                currentNode = arrivedAt;
            }

            else {
                rule = 3;
                from = to;
            }
        }

        return arrivedAt;

    }

    /**
     * Just like walkTo, but faster when used during tree construction, as it
     * assumes that a mismatch can only occurs with the last character of the
     * target string.
     * 
     * @param starting
     *            the root of the subtree we're walking down form.
     * @param source
     *            a superstring that contains the string we're using to walking
     *            down. source.subtring(from,to) should give the string we're
     *            walking down from.
     * @param from
     *            the start position (inclusive) of the target string in the
     *            source.
     * @param to
     *            the end position (exclusive) of the target string in the node.
     * @return a <code>SuffixNode</code> that the walk stopped at. If the walk
     *         stopped inside an edge. (check the rule variable to see where it
     *         stopped).
     */
    public SuffixNode jumpTo(SuffixNode starting, CharSequence source,
            int from, int to) {
        SuffixNode currentNode;
        SuffixNode arrivedAt;
        boolean canGoDown = true;
        int edgeLength;
        int original = from;
        SuffixNode originalNode = starting;
        // int i = 0;

        currentNode = starting;
        arrivedAt = starting;

        rule = 0;

        if (from == to) {
            rule = 5;
            return starting;
        }

        while (canGoDown) {
            // if (source.substring(from, to).equals("CAGCG"))
            // System.out.println(to+" here to "+source.substring(from, to)+"
            // "+(i++));

            if (currentNode.isTerminal()) {
                System.out.println("ARRGH! at "
                        + source.subSequence(original, to) + "(" + from + ","
                        + original + "," + to + ") from "
                        + getLabel(originalNode));
                // Something truly awful happened if this line is ever reached.
                // This bug should be dead, but it it came back from the dead a
                // couple
                // of times already.
            }

            arrivedAt = (SuffixNode) currentNode.getChildren().get(
                    new Character(source.charAt(from)));
            if (arrivedAt == null) {
                canGoDown = false;
                arrivedAt = currentNode;
                rule = 2;
                break;
            }

            edgeLength = getEdgeLength(arrivedAt);
            if (edgeLength >= to - from) {
                // int before = currentNode.labelEnd + to - from + 1;
                int after = getPathEnd(arrivedAt) - getEdgeLength(arrivedAt)
                        + to - from - 1;
                if (sequences.charAt(after) == source.charAt(to - 1)) {
                    if (getEdgeLength(arrivedAt) == to - from) {
                        if (arrivedAt.isTerminal())
                            rule = 1;
                        else
                            rule = 5;
                    } else
                        rule = 4;
                } else
                    rule = 3;
                canGoDown = false;
                break;
            }
            from += edgeLength;
            currentNode = arrivedAt;

        }// while canGoDOwn

        return arrivedAt;
    }

    /***************************************************************************
     * Tree navigation methods
     **************************************************************************/

    public int getEdgeLength(SuffixNode child) {
        int parentLength, childLength;
        SuffixNode parent;
        if (child == root)
            return 0;
        parent = child.parent;
        parentLength = getPathLength(parent);
        childLength = getPathLength(child);
        if (childLength - parentLength <= 0) {

            System.out.println("negative length "
                    + (childLength - parentLength));

            System.out.println(getLabel(child) + "," + getLabel(parent));
        }

        return childLength - parentLength;
    }

    public CharSequence getEdgeLabel(SuffixNode child) {
        return sequences.subSequence(child.labelStart
                + (getPathLength(child) - getEdgeLength(child)),
                (child.labelEnd == TO_A_LEAF) ? e : child.labelEnd);
    }

    public int getPathLength(SuffixNode node) {
        return getPathEnd(node) - node.labelStart;
    }

    public int getPathEnd(SuffixNode node) {
        return node.labelEnd == TO_A_LEAF ? e : node.labelEnd;
    }

    public CharSequence getLabel(SuffixNode node) {
        if (node == root)
            return "root";
        else
            return sequences.subSequence(node.labelStart,
                    (node.labelEnd == TO_A_LEAF) ? e : node.labelEnd)
                    .toString();
    }

    public ArrayList<SuffixNode> getAllNodes(SuffixNode root,
            ArrayList<SuffixNode> list, boolean leavesOnly) {
        Iterator<SuffixNode> iterator;
        if (list == null)
            list = new ArrayList<SuffixNode>();
        if (!leavesOnly || (leavesOnly && root.isTerminal()))
            list.add(root);
        if (!root.isTerminal()) {
            iterator = root.getChildren().values().iterator();
            while (iterator.hasNext())
                list = getAllNodes((SuffixNode) iterator.next(), list,
                        leavesOnly);
        }

        return list;
    }

    public void printTree() {
        ArrayList<SuffixNode> allNodes = getAllNodes(root, null, false);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allNodes.size(); i++) {
            SuffixNode node = (SuffixNode) allNodes.get(i);
            if (node == root)
                System.out.println("root");
            else {
                CharSequence thisLabel = getLabel(node);
                CharSequence parentLabel = getLabel(node.parent);
                builder.append("node " + i + " label: \t" + thisLabel
                        + " attached to: \t" + parentLabel + "\n"); //+ " docList: " + node.documentList.toString() + "\n");
            }
        }
        System.out.println(builder.toString());
    }
    
    //The following function populates the nodeDocumentList and 
    //nodeDocumentListScores lists
    public void constructNodeDocumentList () {
    	String[] fileData = sequences.toString().split("\\$");
    	ArrayList<SuffixNode> allNodes = getAllNodes(root, null, false);
        Map<Integer, Short> currDocList;
 
        for (int i = 0; i < allNodes.size(); i++) {
            SuffixNode node = (SuffixNode) allNodes.get(i);
            if (node != root && !node.isTerminal()) {
            	currDocList = new HashMap<Integer, Short>();
            	String nodeLabel = sequences.subSequence(node.labelStart,
                        (node.labelEnd == TO_A_LEAF) ? e : node.labelEnd).toString();
            	
            	int countSum = 0;
                for (int j = 0; j < fileData.length; j++) {
                	short count = calcTokenCount(fileData[j] + "$", nodeLabel);
                	if (count != 0)
                		currDocList.put(j, count);
                	countSum += count;
                }
                
                if (currDocList.keySet().size() >= 2) {
                	this.nodeDocumentList.add(currDocList);
                	this.nodeDocumentListScores.add((float) (countSum * calcWeightedWordCount(nodeLabel.length())));
                }
            }
        }
    }
    
    //Utility function to calculate the frequency of "str" in "token"
    private short calcTokenCount (final String str, final String token) {
        short count = 0;
        int idx = 0;

        while ((idx = str.indexOf(token, idx)) != -1) {
           idx++;
           count++;
        }

        return count;
    }
    
    //Function calculate the weighted word count
    private double calcWeightedWordCount (int wordCount) {
    	final double singleWordPenalty = 0.5;
    	final double moreThanSixWordsScore = 10;
    	
    	if (wordCount == 1)
    		return singleWordPenalty;
    	else if (wordCount <= 6)
    		return wordCount;
    	else
    		return moreThanSixWordsScore;    		
    }

    public SuffixNode getRoot() {
        return root;
    }

    /***************************************************************************
     * End Tree Navigation Methods
     **************************************************************************/

    /***************************************************************************
     * Tree modification methods
     **************************************************************************/
    private void addPositionToLeaf(int pos, SuffixNode leaf) {
        int[] moreLabels;
        if (leaf.additionalLabels == null)
            leaf.additionalLabels = new int[] { pos };
        else {
            moreLabels = new int[leaf.additionalLabels.length + 1];
            System.arraycopy(leaf.additionalLabels, 0, moreLabels, 0,
                    leaf.additionalLabels.length);
            moreLabels[moreLabels.length - 1] = pos;
            leaf.additionalLabels = moreLabels;
        }

    }

    private void doRule2(SuffixNode parent, int splittingPos, int suffixStart,
            int number, int suffixIndex) {
        SuffixNode leaf = new SimpleNode(parent, suffixStart, number,
                suffixIndex);

        parent.getChildren().put(new Character(sequences.charAt(splittingPos)),
                leaf);
    }

    private SuffixNode doRule3(SuffixNode child, int splittingPos,
            int suffixStart, int number, int suffixIndex) {
        SuffixNode parent = child.parent;
        SuffixNode middle = new SimpleNode(parent, suffixStart, splittingPos,
                number, suffixIndex);
        Character x = new Character(sequences.charAt(child.labelStart
                + getPathLength(child) - getEdgeLength(child)));

        Character y = new Character(sequences.charAt(child.labelStart
                + getPathLength(child) - getEdgeLength(child)
                + getEdgeLength(middle)));

        parent.getChildren().remove(x);
        parent.getChildren().put(x, middle);

        middle.getChildren().put(y, child);
        child.parent = middle;
        doRule2(middle, splittingPos, suffixStart, number, suffixIndex);
        return middle;
    }

    private void finishAddition() {
        SuffixNode leaf;
        ArrayList<SuffixNode> leaves = getAllNodes(root, null, true);
        for (int i = 0; i < leaves.size(); i++) {
            leaf = (SuffixNode) leaves.get(i);
            if (leaf.labelEnd == TO_A_LEAF)
                leaf.labelEnd = e;
        }

    }

    /***************************************************************************
     * end Tree modification methods
     **************************************************************************/

    public boolean subStringExists(String str) {
        walkTo(root, str, 0, str.length());
        return (rule == 1 || rule == 4 || rule == 5);
    }
}
