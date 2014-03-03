.PHONY: clean

clean:
	rm -rf bin/

dawg:
	ant clean dawg

dawg_test:
	ant clean dawg_test

dawg_bit:
	ant clean dawg_bit