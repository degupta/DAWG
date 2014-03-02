package com.degupta.dawg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainClass {
	/**
	 * <pre>
	 * Main Function
	 * 
	 * @param args[0] outFileName
	 *                The file in which the dawg will be written too
	 * @param args[1] fileName
	 *                The file containing the list of words
	 * @param args[2] createDawgFirst
	 *                Set to false only if outFileName has already been
	 *                generated and nothing has changed since then
	 * @param args[3] perLetter
	 *                Set to true if you want to separate out the dawgs per letter 
	 *                (as in one file will store all words starting with 'a', the next 
	 *                 with 'b' and so on). They will be stored as <outFileName>_<startingLetter>
	 * @param args
	 *            [4] createFromFileForArrayLookup
	 *            Set to true if you want to read in the file created and test it
	 * 
	 * @param args[5] bitWise
	 *                Set to true if you want to write the file on bit boundaries
	 *                rather than byte boundaries. Byte Bounderies will only work for
	 *                Englisg
	 * </pre>
	 */
	public static void main(String args[]) {
		String outFileName = "bin/dict";
		String fileName = "words";
		boolean createDawgFirst = true;
		boolean perLetter = false;
		boolean createFromFileForArrayLookup = true;
		boolean bitWise = false;
		if (args.length != 0) {
			outFileName = args[0];
			fileName = args[1];
			createDawgFirst = args[2].equalsIgnoreCase("true");
			perLetter = args[3].equalsIgnoreCase("true");
			createFromFileForArrayLookup = args[4].equalsIgnoreCase("true");
			bitWise = args[5].equalsIgnoreCase("true");
		}

		HashMap<Character, ArrayList<String>> allWords = TrieNode.getWords(
				fileName, perLetter);
		HashMap<Character, int[]> results = new HashMap<Character, int[]>();
		Character[] letters = new Character[allWords.keySet().size()];
		allWords.keySet().toArray(letters);
		Arrays.sort(letters);
		for (Character c : letters) {
			ArrayList<String> words = allWords.get(c);
			int[] outputArr = new int[2];
			if (createDawgFirst) {
				System.out.println("Creating Dawg for letter "
						+ (char) (c - 32) + "... ");
				outputArr[0] = TrieNode.createDawgForWords(words, outFileName
						+ (perLetter ? "_" + c : ""), bitWise);
			}

			if (createFromFileForArrayLookup) {
				try {
					outputArr[1] = TrieNode
							.createDawgFromFileForArrayLookupAndTestIt(words,
									new FileInputStream(new File(outFileName
											+ (perLetter ? "_" + c : ""))),
									bitWise);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			results.put(c, outputArr);

			System.out.println();
		}

		int total = 0;
		int totalBadWords = 0;
		for (Character c : letters) {
			total += results.get(c)[0];
			totalBadWords += results.get(c)[1];
			System.out.println((char) (c - 32) + " : "
					+ (results.get(c)[0] / 1024.0) + " KB");
		}

		System.out.println("TOTAL : " + total / 1024.0 + " KB, Bad Words : "
				+ totalBadWords);
	}
}
