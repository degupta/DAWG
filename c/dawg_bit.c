#include "dawg_bit.h"

#define min(a,b) (a < b ? a : b)

DawgBitArray_t createDawgFromFile(const char * fileName)
{
	char byte[6];
	FILE *fr = fopen (fileName, "rb");
	int val = 0, i = 0, size = 0;

	DawgBitArray_t dawgBitArray = (DawgBitArray_t)(malloc(sizeof(DawgBitArray)));

	// The first six bytes specify the header info.
	fread(byte, 1, 6, fr);
	for(int i = 0; i < 4; i++)
	{
		size = size | (int)((byte[i]) & 0xFF) << (8 * i);
	}

	dawgBitArray->bitsForNodePointers = byte[4];
	dawgBitArray->bitsPerChar = byte[5];
	dawgBitArray->bitsPerNode = dawgBitArray->bitsForNodePointers + 2 + dawgBitArray->bitsPerChar;

	printf("Size : %d, BitsForNodePointers : %d, BitsPerChar : %d, BitsPerNode : %d\n", size, dawgBitArray->bitsForNodePointers, dawgBitArray->bitsPerChar, dawgBitArray->bitsPerNode);

	// Create an array of that size
	dawgBitArray->dawgArray = (DawgArray)(malloc(size));
	fread(dawgBitArray->dawgArray, 1, size, fr);
	fclose(fr);
	return dawgBitArray;
}

int getBits(DawgBitArray_t dawgBitArray, int bitFrom, int numBits)
{
	int val = 0;
	int currentBit = bitFrom % 8;
	int currentBytePos = bitFrom / 8;
	int currentByte = dawgBitArray->dawgArray[currentBytePos] & 0xFF;
	int currentBitsInByte = 0;
	int bitsToAdd = 0;
	int i = 0;
	while(1)
	{
		bitsToAdd = min(8 - currentBit, numBits - i);
		// Apparently C doesn't support logical right shifts, who knew?
		currentBitsInByte = (((currentByte >> currentBit) & 0x7FFFFFFF) & ((1 << bitsToAdd) - 1));
		val |= (currentBitsInByte << i);
		i += bitsToAdd;
		if(i >= numBits)
			break;
		currentBytePos++;
		currentByte = dawgBitArray->dawgArray[currentBytePos] & 0xFF;
		currentBit = 0;
	}

	return val;
}

int wordExists(DawgBitArray_t dawgBitArray, int* word, int length)
{
	if(length == 0)
		return 0;

	int currentChar = 0;
	int currentNode = 0;
	int currentIndex = 0;
	int childPos = 0;
	int childArrPos = 0;
	int pos = 0;
	while(1)
	{
		pos = currentNode * dawgBitArray->bitsPerNode;
		// Get the index of the list of children
		childPos = getBits(dawgBitArray, pos, dawgBitArray->bitsForNodePointers);
		// If the index was 0 we have no children and we haven't reached the end of the word => word doesn't exist
		if(childPos == 0)
			return 0;
		// Go through the list of children and find the one with the character we are looking for
		while(1)
		{
			childArrPos = childPos * dawgBitArray->bitsPerNode;
			currentChar = getBits(dawgBitArray, childArrPos + dawgBitArray->bitsForNodePointers + 2, dawgBitArray->bitsPerChar);
			if(currentChar == word[currentIndex])
			{
				// We found the character move on
				currentIndex++;
				break;
			}
			// If we have reached end of list but still haven't found the character this word doesn't exist
			if(getBits(dawgBitArray, childArrPos + dawgBitArray->bitsForNodePointers, 1) > 0)
				return 0; // Is End Of List
			childPos++;
		}

		// Have we reached the last letter in the word. If yes check
		// whether the current node has its end of word flag set!
		if(currentIndex == length)
			return getBits(dawgBitArray, childArrPos + dawgBitArray->bitsForNodePointers + 1, 1) > 0; // Is Final Node

		// Recurse on child
		currentNode = childPos;
	}
}

void freeDawg(DawgBitArray_t dawgBitArray)
{
	free(dawgBitArray->dawgArray);
	free(dawgBitArray);
}
