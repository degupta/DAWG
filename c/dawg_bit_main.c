#include "dawg_bit.h"
#include <stdlib.h>
#include <string.h>

void translate(int *translatedWord, char *word, int* len)
{
	int i = 0;
	while(word[i] != '\0' && word[i] != '\n')
	{
		if (word[i] < 'a')
		{
			translatedWord[i] = (int)(word[i] - 'a' + 1 + 32);
		}
		else
		{
			translatedWord[i] = (int)(word[i] - 'a' + 1);
		}
		i++;
	}
	translatedWord[i] = '\0';
	*len = i;
}

int testDawg(const char * fileName, DawgBitArray_t dawgBitArray)
{
	char word[50];
	int translatedWord[50];
	FILE *fr = fopen (fileName, "rt");
	int numBad = 0;
	while(fgets(word, 50, fr) != 0)
	{
		int len = strlen(word);
		translate(translatedWord, word, &len);
		if(wordExists(dawgBitArray, translatedWord, len) == 0)
		{
			numBad++;
			printf("Bad word: %s, %d\n", word, len);
		}
	}
	fclose(fr);
	return numBad;
}


int main(int argc, char *argv[])
{
	if (argc < 3)
	{
		printf("Requires Dawg File and List of Words\n");
		return 0;
	}
	DawgBitArray_t dawgBitArray = createDawgBitFromFile(argv[1]);
	int numBad = testDawg(argv[2], dawgBitArray);
	printf("Num bad : %d\n", numBad);
	freeDawg(dawgBitArray);
	return numBad;
}
