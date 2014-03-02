#include "dawg_bit.h"
#include <stdlib.h>
#include <string.h>

void translate(int *translatedWord, char *word, int* len)
{
	int i = 0;
	while(word[i] != '\0' && word[i] != '\n')
	{
		translatedWord[i] = (int)(word[i] - 'a' + 1);
		i++;
	}
	translatedWord[i] = '\0';
	*len = i;
}

void testDawg(const char * fileName, DawgBitArray_t dawgBitArray)
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
			printf("%s, %d\n", word, len);
		}
	}
	fclose(fr);
	printf("Num bad : %d\n", numBad);
}


int main(int argc, char *argv[])
{
	DawgBitArray_t dawgBitArray = createDawgBitFromFile("bin/dict_bit");
	testDawg("bin/words", dawgBitArray);
	freeDawg(dawgBitArray);
	return 0;
}
