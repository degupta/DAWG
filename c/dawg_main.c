#include "dawg.h"
#include <stdlib.h>
#include <string.h>
#include <string.h>
#include <stdio.h>

void testDawg(const char * fileName, DawgArray dawgArray)
{
	char word[50];
	FILE *fr = fopen (fileName, "rt");
	int numBad = 0;
	while(fgets(word, 50, fr) != 0)
	{
		int len = strlen(word);
		if (word[len - 1] == '\n')
		{
			word[len - 1] = '\0';
		}
		if(wordExists(dawgArray, word) == 0)
		{
			numBad++;
			printf("%s\n", word);
		}
	}
	fclose(fr);
	printf("Num bad : %d\n", numBad);
}


int main(int argc, char *argv[])
{
	if (argc < 3)
	{
		printf("Requires Dawg File and List of Words\n");
		return 0;
	}
	DawgArray dawgArray = createDawgFromFile(argv[1]);
	testDawg(argv[2], dawgArray);
	free(dawgArray);
	return 0;
}
