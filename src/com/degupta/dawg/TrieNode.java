package com.degupta.dawg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.io.*;
import java.util.Set;

public class TrieNode {
	char c;
	HashMap<Character, TrieNode> children;
	boolean isFinal = false;
	int childPositionInFile = 0;
	public static char NULL_CHAR = (char) 0;

	public TrieNode(char _c) {
		c = _c;
		children = new HashMap<Character, TrieNode>();
	}

	public void addChild(TrieNode child) {
		children.put(child.c, child);
	}

	public boolean hasLetter(char _c) {
		return children.get(_c) != null;
	}

	public TrieNode getChildNode(char _c) {
		return children.get(_c);
	}

	public boolean isFinal() {
		return this.isFinal;
	}

	public void addWord(String word) {
		char _c = word.charAt(0);
		TrieNode node = getChildNode(_c);
		if (node == null) {
			node = new TrieNode(_c);
			this.addChild(node);
		}

		if (word.length() > 1)
			node.addWord(word.substring(1));
		else
			node.isFinal = true;
	}

	public Set<Character> getEdges() {
		return this.children.keySet();
	}

	public boolean wordExists(String word) {
		if (word.length() == 0)
			return this.isFinal();
		char _c = word.charAt(0);
		TrieNode node = getChildNode(_c);
		return node == null ? false : node.wordExists(word.substring(1));
	}

	public String toString() {
		return this.c + ":" + this.hashCode();
	}

	public int edgeCount() {
		int count = this.children.size();
		for (TrieNode node : this.children.values())
			count += node.edgeCount();
		return count;
	}

	public int nodeCount() {
		int count = 0;
		for (TrieNode node : this.children.values())
			count += node.nodeCount();
		return count + 1;
	}

	public static int[] nodeAndEdgeCountDawg(TrieNode root) {
		int count = 0;
		int countEdge = 0;
		Stack<TrieNode> stack = new Stack<TrieNode>();
		HashSet<TrieNode> set = new HashSet<TrieNode>();
		stack.push(root);
		while (!stack.empty()) {
			TrieNode n = stack.pop();
			if (!set.contains(n)) {
				count++;
				countEdge += n.children.size();
			}
			set.add(n);
			for (TrieNode child : n.children.values()) {
				stack.push(child);
			}
		}
		return new int[] { count, countEdge };
	}

	/**
	 * Two TrieNodes are equal (for a dawg) if they have the same character,
	 * both are either end of word or not end of word, and they have *exactly*
	 * the same children (as in the java objects in the children hashset are the
	 * equal using ==)
	 */
	public boolean equals(Object o) {
		return o instanceof TrieNode && compare((TrieNode) o);
	}

	/**
	 * Two TrieNodes are equal (for a dawg) if they have the same character,
	 * both are either end of word or not end of word, and they have *exactly*
	 * the same children (as in the java objects in the children hashset are the
	 * equal using ==)
	 */
	private boolean compare(TrieNode node) {
		if (node.isFinal != this.isFinal)
			return false;
		Set<Character> thisEdges = this.getEdges();
		Set<Character> otherEdges = node.getEdges();
		if (thisEdges.size() != otherEdges.size())
			return false;
		for (Character c : thisEdges) {
			if (this.children.get(c) != node.children.get(c))
				return false;
		}
		return true;
	}

	/**
	 * Returns hash Code of a string constructed as letter + (isFinal ? 0 : 1) +
	 * list of all children in sorted order. This makes sure if
	 * node1.equals(node2) == true then node1.hashCode() == node2.hashCode()
	 * (otherwise none of the HashSets and HashMaps will work)
	 */
	public int hashCode() {
		String s = this.c + "" + (this.isFinal ? 0 : 1);
		Object[] edges = (Object[]) this.getEdges().toArray();
		Arrays.sort(edges);
		for (int i = 0; i < edges.length; i++)
			s += edges[i];
		return s.hashCode();
	}

	public void printTrie(int tabs) {
		for (int i = 0; i < tabs; i++)
			System.out.print("\t");
		System.out.print(this);
		System.out.println();
		Set<Character> edges = this.getEdges();
		for (Character edge : edges)
			this.children.get(edge).printTrie(tabs + 1);
	}

	/**
	 * Read in a file and return the words in it as an HashMap<Character,
	 * ArrayList<String>> where the key is the starting letter of the word.
	 * Effectively separated out the words by starting letter. If perLetter is
	 * false then just uses the NULL_CHAR as the key.
	 */
	public static HashMap<Character, ArrayList<String>> getWords(
			String wordListPath, boolean perLetter) {
		HashMap<Character, ArrayList<String>> words = new HashMap<Character, ArrayList<String>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					wordListPath));
			String word = reader.readLine();
			while (word != null) {
				word = word.trim().toLowerCase();
				char c = perLetter ? word.charAt(0) : (char) 0;
				if (!words.containsKey(c))
					words.put(c, new ArrayList<String>());
				words.get(c).add(word.toLowerCase());
				word = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return words;
	}

	/**
	 * Create the dawg from the list of words
	 * 
	 */
	public static TrieNode createDawg(ArrayList<String> words) {
		// The Root has NULL_CHAR
		TrieNode root = new TrieNode(NULL_CHAR);
		// The words need to be added in alphabetical order to the dawg
		// otherwise my algorithm won't work.
		Collections.sort(words);
		String previousWord = "";
		int commonPrefix = 0, minLen = 0, len = 0;

		// These are the nodes that can be potentially removed from trie
		ArrayList<TrieNodePair> uncheckedNodes = new ArrayList<TrieNodePair>();
		// These are the nodes that we have already seen. This is a unique set
		HashMap<TrieNode, TrieNode> minimizedNodes = new HashMap<TrieNode, TrieNode>();

		TrieNode node, nextNode;

		for (String word : words) {
			commonPrefix = 0;
			len = word.length();
			minLen = Math.min(len, previousWord.length());
			// Find the common prefix length. Nodes created by
			// letters in front of the common prefix will be
			// added to uncheckedNodes to minimized later on.
			for (int i = 0; i < minLen; i++) {
				if (word.charAt(i) != previousWord.charAt(i))
					break;
				commonPrefix += 1;
			}

			// Minimize the nodes infront of the commonPrefix (which
			// is actually the depth in the current branch after which
			// we starting checking nodes for minimization)
			minimize(commonPrefix, uncheckedNodes, minimizedNodes);

			// Get the last known unminimized node
			if (uncheckedNodes.size() == 0)
				node = root;
			else
				node = uncheckedNodes.get(uncheckedNodes.size() - 1).child;

			// Starting from currentWord[commonPrefix] add nodes to the Trie
			// for the unsimilar part. Put them in uncheckedNodes
			// so that they can be minimized later on
			for (int i = commonPrefix; i < len; i++) {
				nextNode = new TrieNode(word.charAt(i));
				node.addChild(nextNode);
				uncheckedNodes.add(new TrieNodePair(node, nextNode));
				node = nextNode;
			}

			// We are at the node representing the last letter. Hence
			// node.isFinal = true
			node.isFinal = true;
			previousWord = word;
		}

		// Minimize starting from the root
		minimize(0, uncheckedNodes, minimizedNodes);

		return root;
	}

	/**
	 * For the last 'downTo' nodes in uncheckedNodes see if we have already seen
	 * the same node before (in minimizedNodes). If we have then use the node in
	 * minimized nodes, discard this one. If we haven't the use this one and add
	 * it to minimizedNodes since this is the first time we are seeing such a
	 * node.
	 */
	private static void minimize(int downTo,
			ArrayList<TrieNodePair> uncheckedNodes,
			HashMap<TrieNode, TrieNode> minimizedNodes) {
		for (int i = uncheckedNodes.size() - 1; i >= downTo; i--) {
			TrieNodePair pair = uncheckedNodes.get(i);
			TrieNode n = minimizedNodes.get(pair.child);
			if (n != null) {
				pair.parent.addChild(n);
			} else {
				minimizedNodes.put(pair.child, pair.child);
			}

			uncheckedNodes.remove(uncheckedNodes.size() - 1);
		}
	}

	private static byte getByte(int num, int bitFrom) {
		return (byte) (num >> bitFrom);
	}

	public static int writeDawgToBitFile(TrieNode root, String fileName,
			int numEdges, Languages.ILanguageMapping mapping) {
		int numNodesToWrite = numEdges + 1;
		int alphabetSize = mapping.getAlphabetSize() + 1; // for NULL CHAR
		int bitsForNodePointers = Math.max(1,
				32 - Integer.numberOfLeadingZeros(numNodesToWrite));
		int bitsForChar = Math.max(1,
				32 - Integer.numberOfLeadingZeros(alphabetSize));
		int fileSize = (int) Math.ceil((bitsForNodePointers + 2 + bitsForChar)
				* numNodesToWrite / 8.0);

		BitOutputStream bos = new BitOutputStream();

		int currentChildPos = 1;
		int currentNodeCount = 0;
		// Used to store the nodes as a they are added in Breadth First Manner.
		LinkedList<TrieNode> queue = new LinkedList<TrieNode>();
		// The key is the node and the value is the index where the node's
		// children have been written (or will be written) to the file.
		HashMap<TrieNode, Integer> set = new HashMap<TrieNode, Integer>();
		// Stores which positions should be set the End-Of-List Flag
		HashSet<Integer> isEndOfList = new HashSet<Integer>();

		// Add the root to queue to start things off
		root.isFinal = false;
		queue.add(root);

		try {

			// The file size (except header info). They will be the
			// first 4 bytes of the file
			bos.write(fileSize, 32);

			// The next byte is bitsForNodePointers and the next one is
			// bitsForChar
			bos.write(bitsForNodePointers, 8);
			bos.write(bitsForChar, 8);

			// Breadth First Addition of child nodes
			while (queue.peek() != null) {
				TrieNode n = queue.poll();

				int childPos = 0;
				// Have been to this node?
				boolean visited = set.containsKey(n);
				if (visited) {
					// Yes we have. Use the old position where the list of
					// children have been written
					childPos = set.get(n);
				} else {
					// No we haven't. Remember where the list of children is
					// being written too. 0 signifies no children.
					childPos = n.children.size() > 0 ? currentChildPos : 0;
					set.put(n, childPos);
				}

				// If we haven't already visited this node then queue
				// up its children to be added
				if (!visited) {
					for (TrieNode child : n.children.values()) {
						currentChildPos++;
						queue.add(child);
					}

					// The last child node should have the End-of-List flag
					if (n.children.size() > 0)
						isEndOfList.add(currentChildPos - 1);
				}

				// As specified the first 22 bits store the child pos
				// the 23rd bit stores whether end-of-list, the 24th
				// bit stores whether end-of-word and lastly the last
				// 8 bits (the last byte) stores the character
				bos.write(childPos, bitsForNodePointers);
				// Did my parent set me to the end-of-list of his children?
				bos.write(isEndOfList.contains(currentNodeCount) ? 1 : 0, 1);
				bos.write(n.isFinal ? 1 : 0, 1);
				bos.write(mapping.map(n), bitsForChar);

				// Increment the current node we have written
				currentNodeCount++;
			}

			// Write out the whole dawg to the file
			bos.outputToFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileSize + 6;
	}

	/**
	 * <pre>
	 * Writes the dawg to file.
	 * 
	 * ------------------
	 * FILE DESCRIPTION
	 * ------------------
	 * The size of file is driven by the edges in the dawg data structure.
	 * Thanks to dawg words with similar prefixes and suffixes have been
	 * "compressed" making the number of edges pretty small.
	 * 
	 * The size of the file will be (numEdges + 1) * 4 + 4 bytes.
	 * The number of nodes we have to write out will be (numEdges + 1) always.
	 * Each node will require 4 bytes hence (numEdges + 1) * 4.
	 * Lastly, we store the size of file (excluding the first 4 bytes) in the first four bytes of the file
	 * to help during lookup.
	 * 
	 * The data contained in the four bytes is as follows:
	 * The first 22 bits : Index where the list of children of node reside
	 * The 23rd bit : Signifies if the list of children is at an end
	 * The 24th bit : Signifies if a words at this node
	 * The last 8 bits : Store the character at this node
	 * 
	 * For example if we have WORDS = {CITIES, CITY, PITIES, PITY}
	 * 
	 * The DAWG will be [(0) => root, $ = End of Word] 
	 * 
	 * 
	 * (0)---(C)-----(I)----(T)----(I)----(E)----(S$)
	 * |             /        \
	 * |            /         (Y$)
	 * |           /
	 * |-----(P)--/
	 * 
	 * (see how PITY and PITIES have been redirected to CITY and CITIES)
	 * 
	 * There are 9 edges in this graph hence the size of the file shall be = (9 + 1) * 4 + 4 = 44 bytes
	 * 
	 * The dawg file shall be like this (every line represents 4 bytes and of course it is stored binary 
	 * in the format specified above)
	 * 
	 * 40 => The number of bytes to follow
	 * 01  EOL, EOW  NULL_CHAR => THIS IS THE ROOT, always at index 0. I have a list of children starting at index 1.
	 * 03 !EOL,!EOW  a => My character is 'a' and I have a list of children starting at index 3.
	 * 04  EOL,!EOW  p => My character is 'p' and I have a list of children starting at index 4. Also end-of-list of children.
	 * 05  EOL,!EOW  i => My character is 'i' and I have a list of children starting at index 5. Also end-of-list of children.
	 * 05  EOL,!EOW  i => My character is 'i' and I have a list of children starting at index 5. Also end-of-list of children.
	 * 06  EOL,!EOW  t => My character is 't' and I have a list of children starting at index 6. Also end-of-list of children.
	 * 08 !EOL,!EOW  i => My character is 'i' and I have a list of children starting at index 8.
	 * 00  EOL, EOW  y => My character is 'y' and I have NO CHILDREN. Also end-of-list of children and end-of-word.
	 * 09  EOL,!EOW  e => My character is 'e' and I have a list of children starting at index 9. Also end-of-list of children.
	 * 00  EOL, EOW  s => My character is 's' and I have NO CHILDREN. Also end-of-list of children and end-of-word.
	 * 
	 * Note that 'i' is stored twice (the first two i's), once as a child of 'a' and once as a child of 'p'. However, they point to the same
	 * list of nodes as their children.
	 * </pre>
	 */
	private static void writeDawgToFile(TrieNode root, String fileName,
			int numEdges) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int currentChildPos = 1;
		int currentNodeCount = 0;
		// Used to store the nodes as a they are added in Breadth First Manner.
		LinkedList<TrieNode> queue = new LinkedList<TrieNode>();
		// The key is the node and the value is the index where the node's
		// children have been written (or will be written) to the file.
		HashMap<TrieNode, Integer> set = new HashMap<TrieNode, Integer>();
		// Stores which positions should be set the End-Of-List Flag
		HashSet<Integer> isEndOfList = new HashSet<Integer>();

		// Add the root to queue to start things off
		queue.add(root);
		byte[] bytes = new byte[4];
		root.isFinal = false;

		// The number of bytes to follow. They will be the
		// first 4 bytes of the file
		int size = (numEdges + 1) * 4;
		bytes[0] = (byte) ((size & 0xFF000000) >> 24);
		bytes[1] = (byte) ((size & 0x00FF0000) >> 16);
		bytes[2] = (byte) ((size & 0x0000FF00) >> 8);
		bytes[3] = (byte) (size & 0x000000FF);

		try {
			// The size of the file to follow
			baos.write(bytes);

			// Breadth First Addition of child nodes
			while (queue.peek() != null) {
				TrieNode n = queue.poll();

				int childPos = 0;
				// Have been to this node?
				boolean visited = set.containsKey(n);
				if (visited) {
					// Yes we have. Use the old position where the list of
					// children have been written
					childPos = set.get(n);
				} else {
					// No we haven't. Remember where the list of children is
					// being written too. 0 signifies no children.
					childPos = n.children.size() > 0 ? currentChildPos : 0;
					set.put(n, childPos);
				}

				// If we haven't already visited this node then queue
				// up its children to be added
				if (!visited) {
					for (TrieNode child : n.children.values()) {
						currentChildPos++;
						queue.add(child);
					}

					// The last child node should have the End-of-List flag
					if (n.children.size() > 0)
						isEndOfList.add(currentChildPos - 1);
				}

				// As specified the first 22 bits store the child pos
				// the 23rd bit stores whether end-of-list, the 24th
				// bit stores whether end-of-word and lastly the last
				// 8 bits (the last byte) stores the character
				bytes[0] = getByte(childPos, 14);
				bytes[1] = getByte(childPos, 6);
				bytes[2] = (byte) (getByte(childPos, 0) << 2);
				// Did my parent set me to the end-of-list of his children?
				bytes[2] |= (byte) (isEndOfList.contains(currentNodeCount) ? 2
						: 0);
				bytes[2] |= (byte) (n.isFinal ? 1 : 0);
				bytes[3] = (byte) (n.c);

				// Write out the bytes
				baos.write(bytes);

				// Increment the current node we have written
				currentNodeCount++;
			}

			// Write out the whole byte array output stream to the file
			FileOutputStream output = new FileOutputStream(new File(fileName));
			baos.writeTo(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * For debugging purposes.
	 */
	public static void readFile(String fileName) {
		File f = new File(fileName);
		try {
			FileInputStream in = new FileInputStream(f);
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			int c;
			while ((c = in.read()) != -1)
				byteArrayOut.write(c);

			byte[] bytes = byteArrayOut.toByteArray();

			for (int i = 0; i < bytes.length; i += 4) {
				int childPos = (unsignedToBytes(bytes[i]) << 16)
						| (unsignedToBytes(bytes[i + 1]) << 8)
						| (unsignedToBytes(bytes[i + 2]));
				childPos = childPos >> 2;
				int flags = unsignedToBytes(bytes[i + 2]);
				char letter = (char) bytes[i + 3];
				System.out.println("Letter : " + letter + "(" + (i / 4)
						+ ") Child Pos : " + childPos + ", flags : " + flags
						+ " Letter : " + letter);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int unsignedToBytes(byte b) {
		return b & 0xFF;
	}

	/**
	 * Create a normal trie from the words
	 */
	public static TrieNode createTrie(ArrayList<String> words) {
		TrieNode root = new TrieNode(NULL_CHAR);
		for (String s : words)
			root.addWord(s);
		return root;
	}

	/**
	 * Create a dawg from the list of words and write to a file
	 */
	public static int createDawgForWords(ArrayList<String> words,
			String outputFileName, boolean bitWise) {
		TrieNode root;
		long start = System.currentTimeMillis();
		root = createDawg(words);
		System.out.println("Dawg created in "
				+ (System.currentTimeMillis() - start) / 1000.0 + " secs");

		System.out.println("Testing dawg created online... ");
		int numBadWords = 0;
		for (String word : words) {
			if (!root.wordExists(word))
				numBadWords++;
		}
		System.out
				.println("Finished testing dawg created online. No. of bad words "
						+ numBadWords + " out of " + words.size());

		System.out.println("Counting Nodes and Edges... ");
		int[] arr = nodeAndEdgeCountDawg(root);
		System.out.println("Nodes : " + arr[0]);
		System.out.println("Edges : " + arr[1]);
		System.out.println("Writing File... ");

		int fileSize = 0;
		if (bitWise) {
			fileSize = writeDawgToBitFile(root, outputFileName, arr[1],
					new Languages.EnglishMapping());
		} else {
			fileSize = (arr[1] + 2) * 4;
			writeDawgToFile(root, outputFileName, arr[1]);
		}

		System.out.println("File written");

		return fileSize;
	}

	/**
	 * Read in a file representing a dawg and test it to make sure the words
	 * that should be there are there!
	 * 
	 */
	public static int createDawgFromFileForArrayLookupAndTestIt(
			ArrayList<String> words, InputStream in, boolean bitWise) {
		System.out.println("Creating Dawg From File for Array Lookup... ");
		long start2 = System.currentTimeMillis();

		DawgArray dawgArray = null;
		DawgBitArray dawgBitArray = null;
		if (bitWise) {
			dawgBitArray = new DawgBitArray(in,
					new Languages.EnglishMapping());
		} else {
			dawgArray = new DawgArray(in);
		}

		System.out.println("Dawg created from file  for Array Lookup in "
				+ (System.currentTimeMillis() - start2) / 1000.0 + " secs");

		System.out
				.println("Testing dawg created from file for Array Lookup... ");
		start2 = System.currentTimeMillis();
		int numBadWords = 0;
		for (String word : words) {
			if((dawgArray != null && !dawgArray.wordExists(word)) || (dawgBitArray != null && !dawgBitArray.wordExists(word))) {
				numBadWords++;
			}
		}
		System.out
				.println("Finished testing dawg created from file for Array Lookup. No. of bad words "
						+ numBadWords
						+ " out of "
						+ words.size()
						+ ". It only took "
						+ (System.currentTimeMillis() - start2)
						/ 1000.0
						+ " secs.");

		return numBadWords;
	}
}
