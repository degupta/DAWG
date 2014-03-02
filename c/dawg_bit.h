#ifndef DAWG_BIT_ARRAY_H
#define DAWG_BIT_ARRAY_H

#include <stdlib.h>

typedef struct {
	DawgArray dawgArray;
	int bitsForNodePointers;
	int bitsPerChar;
	int bitsPerNode;
} DawgBitArray, *DawgBitArray_t;

int wordExists(DawgBitArray_t dawgBitArray, int* word, int wordLength);
void freeDawg(DawgBitArray_t dawgBitArray);
DawgBitArray_t createDawgFromFile(const char * fileName);

#endif
