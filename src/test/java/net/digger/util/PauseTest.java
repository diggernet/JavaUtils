package net.digger.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.BeforeClass;
import org.junit.Test;

public class PauseTest {
	@BeforeClass
	public static void setup() {
		// make sure all objects are set up, to get startup time out of the way
		Pause.second(0);
		Pause.milli(1);
		Pause.micro(1);
		Pause.nano(1);
	}
	
	@Test
	public void testPauseSeconds() {
		long start = System.nanoTime();
		Pause.second(5);
		long duration = System.nanoTime() - start;
		System.out.printf("Second duration: %f\n", duration / 1000000000.0);
		assertThat(duration, allOf(greaterThan(4990000000L), lessThan(5010000000L)));
	}

	@Test
	public void testPauseMillis() {
		long start = System.nanoTime();
		Pause.milli(5);
		Pause.milli(5);
		Pause.milli(5);
		Pause.milli(5);
		Pause.milli(5);
		long duration = (System.nanoTime() - start) / 5;
		System.out.printf("Milli duration: %f\n", duration / 1000000.0);
		assertThat(duration, allOf(greaterThan(4990000L), lessThan(5010000L)));
	}

	@Test
	public void testPauseMicros() {
		long start = System.nanoTime();
		Pause.micro(50);
		Pause.micro(50);
		Pause.micro(50);
		Pause.micro(50);
		Pause.micro(50);
		long duration = (System.nanoTime() - start) / 5;
		System.out.printf("Micro duration: %f\n", duration / 1000.0);
		assertThat(duration, allOf(greaterThan(49000L), lessThan(51000L)));
	}

	@Test
	public void testPauseNanos() {
		long start = System.nanoTime();
		Pause.nano(5000);
		Pause.nano(5000);
		Pause.nano(5000);
		Pause.nano(5000);
		Pause.nano(5000);
		long duration = (System.nanoTime() - start) / 5;
		System.out.printf("Nano duration: %d\n", duration);
		assertThat(duration, allOf(greaterThan(5000L), lessThan(6000L)));
	}
}
