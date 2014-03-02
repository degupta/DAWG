package com.degupta.dawg;

import java.io.InputStream;

public class DawgBitArray {

	byte[] dawgArray = null;
	int bitsForNodePointers = 0;
	int bitsPerChar = 0;
	int bitsPerNode = 0;
	Languages.ILanguageMapping mapping;

	public DawgBitArray(InputStream in, Languages.ILanguageMapping mapping) {
		this.mapping = mapping;
		createDawgFromFileForArrayLookup(in);
	}

	public void createDawgFromFileForArrayLookup(InputStream in) {
		try {
			int val = 0, i = 0;

			// The first four bytes will specify how many bytes are there in the
			// file (minus the first four bytes)
			int size = in.read() | (in.read() << 8) | (in.read() << 16)
					| (in.read() << 24);

			// Create an array of that size
			dawgArray = new byte[size];

			// Read the bitsForNodePointers and bisPerChar
			this.bitsForNodePointers = in.read();
			this.bitsPerChar = in.read();

			this.bitsPerNode = this.bitsForNodePointers + 2 + this.bitsPerChar;

			// Read in array byte by byte
			while ((val = in.read()) != -1) {
				dawgArray[i++] = (byte) (val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int unsignedToBytes(byte b) {
		return b & 0xFF;
	}

	/**
	 * Fills the int with all bytes [bitFrom, bitFrom + numBits) with bitFrom
	 * being the LSB.
	 * 
	 * @param byteFrom
	 * @param byteTo
	 * @return
	 */
	public int get(int bitFrom, int numBits) {
		int val = 0;
		int currentBit = bitFrom % 8;
		int currentBytePos = bitFrom / 8;
		int currentByte = unsignedToBytes(this.dawgArray[currentBytePos]);
		int currentBitsInByte = 0;
		int bitsToAdd = 0;
		int i = 0;
		while (true) {
			bitsToAdd = Math.min(8 - currentBit, numBits - i);
			currentBitsInByte = ((currentByte >>> currentBit) & ((1 << bitsToAdd) - 1));
			val |= (currentBitsInByte << i);
			i += bitsToAdd;
			if (i >= numBits)
				break;
			currentBytePos++;
			currentByte = this.dawgArray[currentBytePos] & 0xFF;
			currentBit = 0;
		}

		return val;
	}

	public boolean wordExists(String word) {
		int length = word.length();
		if (length == 0)
			return false;

		char currentChar = (char) 0;
		int currentNode = 0;
		int currentIndex = 0;
		int childPos = 0;
		int childArrPos = 0;
		int pos = 0;
		while (true) {
			pos = currentNode * this.bitsPerNode;
			// Get the index of the list of children
			childPos = this.get(pos, this.bitsForNodePointers);
			// If the index was 0 we have no children and we haven't reached the
			// end of the word => word doesn't exist
			if (childPos == 0)
				return false;
			// Go through the list of children and find the one with the
			// character we are looking for
			while (true) {
				childArrPos = childPos * this.bitsPerNode;
				currentChar = this.mapping.reverseMap(this.get(childArrPos
						+ this.bitsForNodePointers + 2, this.bitsPerChar));
				if (currentChar == word.charAt(currentIndex)) {
					// We found the character move on
					currentIndex++;
					break;
				}
				// If we have reached end of list but still haven't found the
				// character this word doesn't exist
				if (this.get(childArrPos + this.bitsForNodePointers, 1) > 0)
					return false; // Is End Of List
				childPos++;
			}

			// Have we reached the last letter in the word. If yes check
			// whether the current node has its end of word flag set!
			if (currentIndex == length) {
				// Is Final Node
				return this.get(childArrPos + this.bitsForNodePointers + 1, 1) > 0;
			}
			// Recurse on child
			currentNode = childPos;
		}
	}
}
