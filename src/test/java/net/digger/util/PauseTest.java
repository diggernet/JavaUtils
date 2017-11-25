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
		long duration = Timer.time(() -> {
			Pause.second(5);
		});
		System.out.printf("Second duration: %f\n", duration / 1000000000.0);
		assertThat(duration, allOf(greaterThan(4990000000L), lessThan(5010000000L)));
	}

	@Test
	public void testPauseMillis() {
		long duration = Timer.time(() -> {
			Pause.milli(5);
			Pause.milli(5);
			Pause.milli(5);
			Pause.milli(5);
			Pause.milli(5);
		});
		duration /= 5;
		System.out.printf("Milli duration: %f\n", duration / 1000000.0);
		assertThat(duration, allOf(greaterThan(4990000L), lessThan(5010000L)));
	}

	@Test
	public void testPauseMicros() {
		// Timer.time() adds too much overhead for timing microseconds.
		long start = System.nanoTime();
		Pause.micro(50);
		Pause.micro(50);
		Pause.micro(50);
		Pause.micro(50);
		Pause.micro(50);
		long duration = (System.nanoTime() - start) / 5;
		System.out.printf("Micro duration: %f\n", duration / 1000.0);
		assertThat(duration, allOf(greaterThan(49000L), lessThan(52000L)));
	}

	@Test
	public void testPauseNanos() {
		// Timer.time() adds too much overhead for timing nanoseconds.
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
