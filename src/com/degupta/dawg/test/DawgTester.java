package com.degupta.dawg.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

import com.degupta.dawg.DawgArray;

public class DawgTester {
	DawgArray dawgArray;
	HashSet<String> words;

	ArrayList<String> errors = new ArrayList<String>();

	public static void main(String[] args) {
		new DawgTester(args[0], args[1]);
	}

	public DawgTester(String dawgFile, String wordsFileName) {
		try {
			dawgArray = new DawgArray(new FileInputStream(new File(dawgFile)));
			BufferedReader reader = new BufferedReader(new FileReader(
					wordsFileName));
			String word = reader.readLine();
			words = new HashSet<String>();
			while (word != null) {
				words.add(word.toLowerCase());
				word = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			error("Problem while initializing DawgTester", e);
		}

		generateWordsAndTestIt();
		testSmallStrings();
		testSmallerAndLargerStrings();
		testMyName();

		if (errors.size() != 0) {
			System.out.println("There were " + errors.size() + " failure(s)!");
			for (String error : errors) {
				System.out.println(error);
			}
			System.exit(1);
		} else {
			System.out.println("All is good with the big DAWG");
			System.exit(0);
		}

	}

	public void generateWordsAndTestIt() {
		try {
			HashSet<String> genWords = dawgArray.getAllWords();

			if (words.size() != genWords.size()) {
				error("Generated words was not the same as input words!");
			}

			for (String genWord : genWords) {
				if (!words.contains(genWord)) {
					error("Generated words was not the same as input words!");
				}
			}
		} catch (Exception e) {
			error("Exception in checking generated words", e);
		}
	}

	public void testSmallStrings() {
		if (dawgArray.wordExists("")) {
			error("Empty string is a valid word!");
		}
		for (char i = 0; i < 26; i++) {
			String letter = "" + (char) ('a' + i);
			if (dawgArray.wordExists(letter)) {
				error("The letter '" + letter + "' is a valid word!");
			}
		}
	}

	public void testSmallerAndLargerStrings() {
		String smaller, larger;
		for (String word : words) {
			smaller = word.substring(0, word.length() - 1);
			if (words.contains(smaller) != dawgArray.wordExists(smaller)) {
				error("The word " + smaller
						+ " doesn't match in the words list and dawg!");
			}
			larger = word + ("" + ((int) Math.floor(Math.random() * 26) + 'a'));
			if (words.contains(larger) != dawgArray.wordExists(larger)) {
				error("The word " + larger
						+ " doesn't match in the words list and dawg!");
			}
		}
	}

	public void testMyName() {
		if (dawgArray.wordExists("devansh")) {
			error("Sadly, devansh is not a legal word.");
		}
	}

	public void error(String error) {
		error(error, null);
	}

	public void error(String error, Exception e) {
		if (e != null) {
			error += ":" + e.getMessage();
		}
		errors.add(error);
	}
}