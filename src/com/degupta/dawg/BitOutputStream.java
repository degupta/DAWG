package com.degupta.dawg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitOutputStream {
	int currentBit = 0;
	byte[] currentByte = new byte[] { 0 };
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public void outputToFile(String fileName) {
		if (currentBit != 0) {
			// If we have some pending bits to write,
			// write them (the rest should be 0)
			write(0, 8 - currentBit);
		}

		// Write out the whole byte array output stream to the file
		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(fileName));
			baos.writeTo(output);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Writes the first bits of num
	 * 
	 * @param num
	 * @param bitsToWrite
	 */
	public void write(int num, int bits) {
		for (int i = 0; i < bits; i++) {
			writeBit(num >> i);
		}
	}

	/**
	 * Writes the lowest bit.
	 * 
	 * @param bit
	 */
	public void writeBit(int bit) {
		bit &= 0x1; // Zero out everything else
		currentByte[0] |= (bit << currentBit);
		currentBit += 1;
		if (currentBit == 8) {
			try {
				baos.write(currentByte);
			} catch (IOException e) {
				e.printStackTrace();
			}
			currentBit = 0;
			currentByte[0] = 0;
		}
	}
}