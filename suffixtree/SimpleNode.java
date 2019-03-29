package com.doccluster.suffixtree;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Node Class for the suffix tree
 * 
 */
public class SimpleNode extends SuffixNode implements TreeNode {

    int dfs = 0;

    int id = 0;

    public int suffixIndex;

    /**
     * Creates a root
     */
    public SimpleNode() {
        parent = null;
        suffixLink = null;
        labelStart = 0;
        labelEnd = 0;
        children = new HashMap<Character, SuffixNode>();
        additionalLabels = null;
        textNumber = 0;
        suffixIndex = 0;
    }

    /**
     * creates a leaf
     * 
     * @param parent
     *            the parent node
     * @param position
     *            the starting value of the suffix
     */
    public SimpleNode(SuffixNode parent, int position, int textNumber,
            int suffixIndex) {
        this();
        this.parent = parent;
        labelStart = position;
        labelEnd = A_LEAF;
        children = null;
        this.textNumber = textNumber;
        this.suffixIndex = suffixIndex;
    }

    /**
     * creates an internal node
     * 
     * @param parent
     *            the parent of this node
     * @param labelStart
     *            the starting point of the path label
     * @param labelStop
     *            the ending point of the path label
     */
    public SimpleNode(SuffixNode parent, int labelStart, int labelStop,
            int textNumber, int suffixIndex) {
        this();
        this.parent = parent;
        this.labelStart = labelStart;
        this.labelEnd = labelStop;
        this.textNumber = 0;
        this.suffixIndex = 0;
    }

    public boolean isTerminal() {
        return children == null;
    }

    public boolean hasChild(Character x) {
        return getChild(x) != null;
    }

    public SuffixNode getChild(Character x) {
        return (children == null) ? null : children.get(x);
    }

    public SuffixNode getParent() {
        return parent;
    }

    public int numChildren() {
        return children == null ? 0 : children.keySet().size();
    }

    public TreeNode getChild(int num) {
        Iterator<Character> iter = children.keySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            TreeNode node = (TreeNode) children.get(iter.next());
            if (i == num)
                return node;
            i++;
        }
        return null;
    }
}