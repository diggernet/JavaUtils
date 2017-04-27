package net.digger.util;

import java.util.concurrent.TimeUnit;

/**
 * net.digger.util.Delay
 * Copyright © 2017  David Walton
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility for pausing a program briefly.
 * @author walton
 */
public class Delay {
	public static final int NANO_PRECISION = setNanoPrecision();

	/**
	 * Pauses the given number of seconds.
	 * @param sec
	 */
	public static void second(int sec) {
		try {
			TimeUnit.SECONDS.sleep(sec);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
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
					// sleep for no time (which still takes a bit of time)
					TimeUnit.MILLISECONDS.sleep(0);
				}
				us = (int)((end - System.nanoTime()) / 1000);
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Pauses the given number of nanoseconds.
	 * This is probably not worth calling for values less than
	 * about double the value of NANO_PRECISION.
	 * @param ns
	 */
	public static void nano(int ns) {
		try {
			long end = System.nanoTime() + ns;
			while (ns > 0) {
				if (ns > TimeUnit.MILLISECONDS.toNanos(2)) {
					// if more than 2ms remaining...
					// sleep for 1ms (or so)
					TimeUnit.MILLISECONDS.sleep(1);
				} else if (ns > TimeUnit.MICROSECONDS.toNanos(2)) {
					// if more than 2us remaining...
					// sleep for no time (which still takes a bit of time)
					TimeUnit.MILLISECONDS.sleep(0);
				} else {
					// if 1us or less remaining...
					// can't be efficient about it, just busy-wait until time is up
				}
				ns = (int)(end - System.nanoTime());
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	// This checks the time between System.nanoTime() updates.
	// It varies, but I've personally never seen it go below 500ns.
	private static int setNanoPrecision() {
		long start = System.nanoTime();
		long end = start;
		while (end == start) {
			end = System.nanoTime();
		}
		int nano = (int)(end - start);
//		System.out.println("NANO_PRECISION = (" + end + "-" + start + " = " + (end - start) + ") " + nano + " nanoseconds");
		return nano;
	}
}
