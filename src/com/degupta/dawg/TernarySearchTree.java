package com.degupta.dawg;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class TernarySearchTree {
	public static long NUM_NODES = 0;

	public class TSTNode {
		char c;
		TSTNode leftChild = null, rightChild = null, equalChild = null;
		boolean isEndofWord = false;

		public TSTNode() {
			NUM_NODES++;
		}

		public String toString() {
			return c + "";
		}
	}

	TSTNode root = null;

	public boolean wordExists(String word) {
		TSTNode current = root;
		char currentChar = word.charAt(0);
		int currentIndex = 0;
		int length = word.length();
		while (current != null) {
			if (current.c < currentChar)
				current = current.rightChild;
			else if (current.c > currentChar)
				current = current.leftChild;
			else {
				currentIndex++;
				if (currentIndex == length)
					return current.isEndofWord;
				current = current.equalChild;
				currentChar = word.charAt(currentIndex);
			}
		}
		return false;
	}

	public void insert(final String word) {
		root = insert(root, word, 0);
	}

	private TSTNode insert(TSTNode node, final String word, int currentIndex) {
		char currentChar = word.charAt(currentIndex);
		if (node == null) {
			node = new TSTNode();
			node.c = currentChar;
		}
		if (node.c < currentChar)
			node.rightChild = insert(node.rightChild, word, currentIndex);
		else if (node.c > currentChar)
			node.leftChild = insert(node.leftChild, word, currentIndex);
		else {
			currentIndex++;
			if (currentIndex == word.length()) {
				node.isEndofWord = true;
			} else {
				node.equalChild = insert(node.equalChild, word, currentIndex);
			}
		}

		return node;
	}

	public static void insertWords(TernarySearchTree tree,
			ArrayList<String> words, int lo, int hi) {
		if (lo > hi)
			return;
		int mid = (lo + hi) / 2;
		tree.insert(words.get(mid));
		insertWords(tree, words, lo, mid - 1);
		insertWords(tree, words, mid + 1, hi);
	}

	public static void insertWords(TernarySearchTree tree,
			ArrayList<String> words) {
		insertWords(tree, words, 0, words.size() - 1);
	}

	public static long createTSTForWords(ArrayList<String> words,
			String outputFileName) {
		TernarySearchTree.NUM_NODES = 0;
		TernarySearchTree tree = new TernarySearchTree();
		long start = System.currentTimeMillis();
		insertWords(tree, words);
		System.out.println("Ternary Search Tree created in "
				+ (System.currentTimeMillis() - start) / 1000.0 + " secs");

		System.out.println("Testing Ternary Search Tree created online... ");
		int numBadWords = 0;
		for (String word : words) {
			if (!tree.wordExists(word))
				numBadWords++;
		}
		System.out
				.println("Finished testing Testing Ternary Search Tree created online. No. of bad words "
						+ numBadWords + " out of " + words.size());
		System.out.println("Number of nodes required : "
				+ TernarySearchTree.NUM_NODES);

		System.out.println("Writing File... ");
		writeTSTTOFile(tree, outputFileName, TernarySearchTree.NUM_NODES);
		System.out.println("File written");

		return TernarySearchTree.NUM_NODES;
	}

	public static void writeTSTTOFile(TernarySearchTree tree, String fileName,
			long numNodes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// The number of bytes to follow. They will be the
		// first 4 bytes of the file
		byte[] bytes = new byte[4];
		long size = numNodes;
		bytes[0] = (byte) ((size & 0xFF000000) >> 24);
		bytes[1] = (byte) ((size & 0x00FF0000) >> 16);
		bytes[2] = (byte) ((size & 0x0000FF00) >> 8);
		bytes[3] = (byte) (size & 0x000000FF);

		// Used to store the nodes as a they are added in Breadth First Manner.
		LinkedList<TSTNode> queue = new LinkedList<TSTNode>();
		queue.add(tree.root);
		bytes = new byte[1];
		try {
			// The size of the file to follow
			baos.write(bytes);

			while (!queue.isEmpty()) {
				TSTNode node = queue.poll();
				bytes[0] = 0;
				if (node != null) {
					bytes[0] = (byte) ((node.c - 'a') | (node.isEndofWord ? 1 << 7
							: 0));
					queue.add(node.leftChild);
					queue.add(node.rightChild);
					queue.add(node.equalChild);
					baos.write(bytes[0]);
				}
			}

			// Write out the whole byte array output stream to the file
			FileOutputStream output = new FileOutputStream(new File(fileName));
			baos.writeTo(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		String outFileName = "bin/dict_tst";
		String fileName = "bin/words";
		boolean createTSTFirst = true;
		boolean perLetter = false;

		HashMap<Character, ArrayList<String>> allWords = TrieNode.getWords(
				fileName, perLetter);
		HashMap<Character, long[]> results = new HashMap<Character, long[]>();
		Character[] letters = new Character[allWords.keySet().size()];
		allWords.keySet().toArray(letters);
		Arrays.sort(letters);
		for (Character c : letters) {
			ArrayList<String> words = allWords.get(c);
			long[] outputArr = new long[1];
			if (createTSTFirst) {
				System.out.println("Creating Ternary Search Tree for letter "
						+ (char) (c - 32) + "... ");
				outputArr[0] = createTSTForWords(words, outFileName
						+ (perLetter ? "_" + c : ""));
			}

			results.put(c, outputArr);

			System.out.println();
		}
	}
}
