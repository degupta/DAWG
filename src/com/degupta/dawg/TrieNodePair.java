package com.degupta.dawg;
public class TrieNodePair {
	TrieNode parent;
	TrieNode child;

	public TrieNodePair(TrieNode _parent, TrieNode _child) {
		parent = _parent;
		child = _child;
	}

	public String toString() {
		return "(" + parent.toString() + ", " + child.toString() + ")";
	}
}
