#ifndef DAWG_BIT_ARRAY_H
#define DAWG_BIT_ARRAY_H

#include <stdlib.h>
#include <stdio.h>
#include "common.h"

typedef struct {
	DawgArray dawgArray;
	int bitsForNodePointers;
	int bitsPerChar;
	int bitsPerNode;
} DawgBitArray, *DawgBitArray_t;

DawgBitArray_t createDawgBitFromFile(const char * fileName);
int wordExists(DawgBitArray_t dawgBitArray, int* word, int wordLength);
void freeDawg(DawgBitArray_t dawgBitArray);

#endif
