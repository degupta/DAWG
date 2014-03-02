.PHONY: dawg dawg_bit

dawg_bit:
	rm -rf bin/
	mkdir bin
	cp ../words bin/words
	cp ../words_es bin/words_es
	cp ../bin/dict_bit bin/dict_bit
	cp ../bin/dict_bit_es bin/dict_bit_es
	gcc common.h dawg_bit.h dawg_bit.c dawg_bit_main.c -o bin/dawg_bit
	bin/dawg_bit bin/dict_bit bin/words
	bin/dawg_bit bin/dict_bit_es bin/words_es

dawg:
	rm -rf bin/
	mkdir bin
	cp ../words bin/words
	cp ../bin/dict bin/dict
	gcc common.h dawg.h dawg.c dawg_main.c -o bin/dawg
	bin/dawg bin/dict bin/words