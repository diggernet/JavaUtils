# JavaUtils
JavaUtils is a collection of small utility classes.  It includes tools to
pause a thread with finer control than the standard tools, and to measure
elapsed time.


## Pause
Pause is a tool to pause a thread for a period of time, with finer control
than the standard tools.  For example, you could call

		TimeUnit.MICROSECONDS.sleep(micros);

or

		TimeUnit.NANOSECONDS.sleep(nanos);

but currently both of those internally just call

		Thread.sleep(millis);

so you don't have any control below 1 millisecond.

Pause gives you control well below that threshold, down to microseconds
and makes a good effort at nanoseconds.  Precision at that level depends
on your machine and your JVM.  I get good results down to around a few
tens of microseconds.  Your results may vary.

### Usage

		Pause.second(seconds);

		Pause.milli(milliseconds);

		Pause.micro(microseconds);

		Pause.nano(nanoseconds);


## Timer
A simple little tool to quickly check the elapsed time for a block of code.

### Usage

		long elapsed = Timer.time(() -> {
			// your code here
		});

## License
These utilities are provided under the terms of the GNU LGPLv3.
