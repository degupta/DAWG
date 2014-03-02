package com.degupta.dawg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;

public class SWFSolver {
	public enum Direction {
		BAD, START, TOP_LEFT, TOP, TOP_RIGHT, LEFT, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

		public static Direction getDirection(int dR, int dC) {
			switch (dR) {
			case -1:
				switch (dC) {
				case -1:
					return TOP_LEFT;
				case 0:
					return TOP;
				case 1:
					return TOP_RIGHT;
				}
				break;

			case 0:
				switch (dC) {
				case -1:
					return LEFT;
				case 0:
					return START;
				case 1:
					return RIGHT;
				}
				break;

			case 1:
				switch (dC) {
				case -1:
					return BOTTOM_LEFT;
				case 0:
					return BOTTOM;
				case 1:
					return BOTTOM_RIGHT;
				}
				break;
			}

			return BAD;
		}
	}

	public static final int BOARD_SIZE = 4;

	public DawgArray dawgArray = null;

	public SWFSolver(InputStream in) {
		dawgArray = new DawgArray(in);
	}

	public SWFSolver(DawgArray _dawgArray) {
		dawgArray = _dawgArray;
	}

	public SWFSolverContext getAllWordsNoDirections(char[][] board) {
		SWFSolverContext scc = new SWFSolverContext(false);
		scc.board = board;
		// 0 => root in dawg array
		HashMap<Character, Integer> rootChildren = dawgArray.getChildren(0);
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				// Get all words starting from this position
				if (rootChildren.containsKey(scc.board[row][col]))
					getAllWordsFromNoDirections(scc, row, col,
							rootChildren.get(scc.board[row][col]));
			}
		}

		return scc;
	}

	private void getAllWordsFromNoDirections(SWFSolverContext scc, int row,
			int col, int currentNode) {
		char currentChar = scc.board[row][col];
		// Add the current letter at this position, and mark it visited so we
		// don't come here again
		scc.currentWord = scc.currentWord.append(currentChar);
		scc.visited[row][col] = true;

		if (dawgArray.isEndOFWord(currentNode))
			scc.uniqueWords.add(sbToString(scc.currentWord));

		HashMap<Character, Integer> children = dawgArray
				.getChildren(currentNode);

		// Recurse on all neighbors
		for (int nextR = row - 1; nextR <= row + 1; nextR++) {
			for (int nextC = col - 1; nextC <= col + 1; nextC++) {
				// Are we going to a legal board position && we are not on the
				// current square && we have not visited this place already &&
				// going to this node is not a complete waste of time
				if (nextR >= 0 && nextR < BOARD_SIZE && nextC >= 0
						&& nextC < BOARD_SIZE && (nextR != row || nextC != col)
						&& !scc.visited[nextR][nextC]
						&& children.containsKey(scc.board[nextR][nextC])) {
					getAllWordsFromNoDirections(scc, nextR, nextC,
							children.get(scc.board[nextR][nextC]));
				}
			}
		}

		// Backtrack! Remove the last letter we added above and remove the fact
		// that we visited this node
		scc.currentWord = scc.currentWord
				.deleteCharAt(scc.currentWord.length() - 1);
		scc.visited[row][col] = false;
	}

	private String sbToString(StringBuilder currentWord) {
		String str = "";
		int len = currentWord.length();
		for (int i = 0; i < len; i++)
			str += currentWord.charAt(i);
		return str;
	}

	public SWFSolverContext getAllWords(char[][] board) {
		SWFSolverContext scc = new SWFSolverContext(true);
		scc.board = board;
		// 0 => root in dawg array
		HashMap<Character, Integer> rootChildren = dawgArray.getChildren(0);
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				scc.startRow = row;
				scc.startCol = col;
				// Get all words starting from this position
				if (rootChildren.containsKey(scc.board[row][col]))
					getAllWordsFrom(scc, row, col,
							rootChildren.get(scc.board[row][col]),
							Direction.START);
			}
		}

		return scc;
	}

	private void getAllWordsFrom(SWFSolverContext scc, int row, int col,
			int currentNode, Direction comingDirection) {
		char currentChar = scc.board[row][col];
		// Add the current letter at this position, and mark it visited so we
		// don't come here again
		scc.currentWord = scc.currentWord.append(currentChar);
		scc.visited[row][col] = true;
		scc.curDirections.add(comingDirection);

		if (dawgArray.isEndOFWord(currentNode)) {
			// Does the current string of letters form a legal word?
			String curWord = sbToString(scc.currentWord);
			ArrayList<Direction> directions = new ArrayList<Direction>(
					scc.curDirections.size());
			for (Direction d : scc.curDirections)
				directions.add(d);
			int i = 0;
			while (scc.words.containsKey(curWord + ":" + i))
				i++;
			scc.words.put(curWord + ":" + i, directions);
			scc.startPositions.put(curWord + ":" + i, scc.startRow * BOARD_SIZE
					+ scc.startCol);
		}

		HashMap<Character, Integer> children = dawgArray
				.getChildren(currentNode);

		// Recurse on all neighbors
		for (int nextR = row - 1; nextR <= row + 1; nextR++) {
			for (int nextC = col - 1; nextC <= col + 1; nextC++) {
				// Are we going to a legal board position && we are not on the
				// current square && we have not visited this place already &&
				// going to this node is not a complete waste of time
				if (nextR >= 0 && nextR < BOARD_SIZE && nextC >= 0
						&& nextC < BOARD_SIZE && (nextR != row || nextC != col)
						&& !scc.visited[nextR][nextC]
						&& children.containsKey(scc.board[nextR][nextC])) {
					getAllWordsFrom(scc, nextR, nextC,
							children.get(scc.board[nextR][nextC]),
							Direction.getDirection(nextR - row, nextC - col));
				}
			}
		}

		// Backtrack! Remove the last letter we added above and remove the fact
		// that we visited this node
		scc.currentWord = scc.currentWord
				.deleteCharAt(scc.currentWord.length() - 1);
		scc.visited[row][col] = false;
		scc.curDirections.remove(scc.curDirections.size() - 1);
	}

	public static char[][] testBoard = new char[][] { { 'f', 'l', 's', 'n' },
			{ 'h', 'i', 't', 'e' }, { 'n', 'r', 'a', 't' },
			{ 's', 'b', 'n', 'r' } };

	public static int[] scores = { 1, 4, 4, 2, 1, 4, 3, 3, 1, 10, 5, 2, 4, 2,
			1, 4, 10, 1, 1, 1, 2, 5, 4, 8, 3, 10 };

	public static void main(String[] args) {
		boolean noDirections = true;

		System.out.println("Reading in Dawg...");
		long start = System.currentTimeMillis();
		SWFSolver solver = null;
		try {
			solver = new SWFSolver(new FileInputStream(new File("bin/dict")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Read in Dawg. It took "
				+ (System.currentTimeMillis() - start) / 1000.0 + " secs.");

		if (noDirections) {
			System.out.println("Finding all possible solutions...");
			start = System.currentTimeMillis();
			SWFSolverContext solution = solver
					.getAllWordsNoDirections(testBoard);
			System.out.println("Found all possible solutions. It only took "
					+ (System.currentTimeMillis() - start) / 1000.0 + " secs.");

			for (String word : solution.uniqueWords)
				System.out.println(word);
		} else {
			System.out.println("Finding all possible solutions...");
			start = System.currentTimeMillis();
			SWFSolverContext solution = solver.getAllWords(testBoard);
			System.out.println("Found all possible solutions. It only took "
					+ (System.currentTimeMillis() - start) / 1000.0 + " secs.");

			System.out.println("Sorting solutions...");
			start = System.currentTimeMillis();
			String[] wordKeys = new String[solution.words.keySet().size()];
			solution.words.keySet().toArray(wordKeys);
			Arrays.sort(wordKeys, new Comparator<String>() {
				public int compare(String string1, String string2) {
					string1 = string1.substring(0, string1.indexOf(":"));
					string2 = string2.substring(0, string2.indexOf(":"));
					int score = 0, str1Len = string1.length(), str2Len = string2
							.length(), i = 0;
					for (i = 0; i < str1Len; i++)
						score -= scores[string1.charAt(i) - 'a'];
					for (i = 0; i < str2Len; i++)
						score += scores[string2.charAt(i) - 'a'];
					return score == 0 ? str1Len - str2Len : score;
				}

			});
			System.out.println("Sorted solutions. It took "
					+ (System.currentTimeMillis() - start) / 1000.0 + " secs.");

			for (String word : wordKeys)
				System.out.println(word.substring(0, word.indexOf(":")) + " ("
						+ solution.startPositions.get(word) + ")  => "
						+ solution.words.get(word).toString());
		}
	}
}
