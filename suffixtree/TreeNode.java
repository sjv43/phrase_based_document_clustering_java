package com.doccluster.suffixtree;

public interface TreeNode {
    /**
     * return the number of children this node has
     * 
     * @return the number of children this node has
     */
    int numChildren();

    /**
     * return a selected child of this node.
     * 
     * @param num
     *            the number of child wanted, counting from 0
     * @return a child of this node
     */
    TreeNode getChild(int num /* from 0 */);
}