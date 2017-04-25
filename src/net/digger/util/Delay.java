package net.digger.util;

import java.util.concurrent.TimeUnit;

public class Delay {
	private static int NANO_PRECISION = setNanoPrecision();

	/**
	 * Pauses the given number of milliseconds.
	 * @param ms
	 */
	public static void milli(int ms) {
		try {
			TimeUnit.MILLISECONDS.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Pauses the given number of microseconds.
	 * @param us
	 */
	public static void micro(int us) {
		try {
			long end = System.nanoTime() + (us * 1000);
			while (us > 0) {
				// if more than 2ms remaining...
				if (us > TimeUnit.MILLISECONDS.toMicros(2)) {
					// sleep for 1ms (or so)
					TimeUnit.MILLISECONDS.sleep(1);
				} else {
					TimeUnit.MILLISECONDS.sleep(0);
				}
				us = (int)((end - System.nanoTime()) / 1000);
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private static int setNanoPrecision() {
		long start = System.nanoTime();
		long end = start;
		while (end == start) {
			end = System.nanoTime();
		}
		int nano = (int)Math.ceil((end - start) / 1000.0) * 1000;
		System.out.println("NANO_PRECISION = (" + end + "-" + start + " = " + (end - start) + ") " + nano + " nanoseconds");
		return nano;
	}
}
