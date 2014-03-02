package com.degupta.dawg;

public class Languages {
	public static interface ILanguageMapping {
		public int map(TrieNode node);

		public int getAlphabetSize();

		public char reverseMap(int num);
	}

	public static class EnglishMapping implements ILanguageMapping {
		public int map(TrieNode node) {
			return (int) (node.c - 'a' + 1);
		}

		public int getAlphabetSize() {
			return 26;
		}

		public char reverseMap(int num) {
			return (char) (num + 'a' - 1);
		}
	}
}
