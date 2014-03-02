.PHONY: dawg dawg_bit

dawg_bit:
	rm -rf bin/
	mkdir bin
	cp ../words bin/words
	cp ../bin/dict_bit bin/dict_bit
	gcc common.h dawg_bit.h dawg_bit.c dawg_bit_main.c -o bin/dawg_bit

dawg:
	rm -rf bin/
	mkdir bin
	cp ../words bin/words
	cp ../bin/dict bin/dict
	gcc common.h dawg.h dawg.c dawg_main.c -o bin/dawg