.PHONY: clean

clean:
	rm -rf bin/

dawg:
	ant clean dawg

dawg_bit:
	ant clean dawg_bit