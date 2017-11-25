package net.digger.util;

import java.util.concurrent.TimeUnit;

/**
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
 * Utility for pausing the current thread briefly.
 * <p>
 * Does its best to handle sub-millisecond pauses.
 * As of Java8 at least, TimeUnit.*.sleep() calls Thread.sleep(millis, nanos),
 * which simply rounds up to millis + 1.
 * 
 * @author walton
 */
public class Pause {
	public static final int NANO_PRECISION = setNanoPrecision();

	/**
	 * Pauses the given number of seconds.
	 * 
	 * @param sec Number of seconds to pause.
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
	 * 
	 * @param ms Number of milliseconds to pause.
	 */
	public static void milli(int ms) {
		// Since TimeUnit.MILLISECONDS.sleep() tends to sleep rather longer
		// than it should, we'll use the more accurate micro().
		micro(ms * 1000);
	}
	
	/**
	 * Pauses the given number of microseconds.
	 * 
	 * @param us Number of microseconds to pause.
	 */
	public static void micro(int us) {
		try {
			long end = System.nanoTime() + (us * 1000);
			long ms = TimeUnit.MICROSECONDS.toMillis(us);
			// if more than 1ms requested...
			if (ms > 1) {
				// sleep for 1ms less than that (or so)
				TimeUnit.MILLISECONDS.sleep(ms - 1);
				us = (int)((end - System.nanoTime()) / 1000);
			}
			// while any us remaining...
			while (us > 0) {
				// sleep for no time (which still takes a bit of time)
				TimeUnit.MILLISECONDS.sleep(0);
				us = (int)((end - System.nanoTime()) / 1000);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Pauses the given number of nanoseconds.
	 * This is probably not worth calling for values less than
	 * several times the value of NANO_PRECISION.
	 * 
	 * @param ns Number of nanoseconds to pause.
	 */
	public static void nano(int ns) {
		try {
			long end = System.nanoTime() + ns;
			long ms = TimeUnit.NANOSECONDS.toMillis(ns);
			// if more than 1ms requested...
			if (ms > 1) {
				// sleep for 1ms less than that (or so)
				TimeUnit.MILLISECONDS.sleep(ms - 1);
				ns = (int)(end - System.nanoTime());
			}
			// while any ns remaining...
			while (ns > 0) {
				if (ns > TimeUnit.MICROSECONDS.toNanos(1)) {
					// if more than 1us remaining...
					// sleep for no time (which still takes a bit of time)
					TimeUnit.MILLISECONDS.sleep(0);
				} else {
					// if less than 2us remaining...
					// can't be efficient about it, just busy-wait until time is up
				}
				ns = (int)(end - System.nanoTime());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	// This checks the approximate time between System.nanoTime() updates.
	// The value varies, but I've personally never yet seen it go below 500ns.
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
