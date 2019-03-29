package com.doccluster.suffixtree;

import java.util.HashMap;

/**
 * Abstract superclass for tree nodes
 */

public abstract class SuffixNode {

    public static final int A_LEAF = -1;

    protected SuffixNode parent;

    protected SuffixNode suffixLink;

    protected int labelStart, labelEnd;

    protected HashMap<Character, SuffixNode> children;

    protected int[] additionalLabels;

    public int id;

    protected int textNumber;
    
    /**
     * Determine is this node is terminal (has no children).
     * <p>
     * Note that this only happens at the terminated node (if the sequences have
     * been terminated.
     * 
     * @return <code>true</code> if and only if this node has no children.
     */
    abstract public boolean isTerminal();

    /**
     * Determine if this node has a child corresponding to a given character
     * 
     * @param i
     *            the first <code>Character</code> of the edge coming down
     *            this node.
     * @return <code>true</code> if the node has a child going down from that
     *         character, false otherwise
     */
    abstract public boolean hasChild(Character i);

    /**
     * Gets the child of of a node that is coming down from that particular
     * node. It returns null if no child exists or if no child exists starting
     * on that particular character.
     * 
     * @param i
     *            the first <code>Character</code> of the edge coming down
     *            this node
     * @return the appropriate child <code>SuffixNode</code>, or null if it
     *         doesn't exists.
     */
    public abstract SuffixNode getChild(Character i);

    /**
     * Returns the parent of this node, null if it's the root.
     * 
     * @return the parent of this node, null if it's the root.
     */
    public abstract SuffixNode getParent();

    /**
     * @return Returns the children
     */
    public HashMap<Character, SuffixNode> getChildren() {
        return children;
    }

}