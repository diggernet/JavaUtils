# JavaUtils
JavaUtils is a collection of small utility classes.  It includes tools to
calculate checksums and CRCs using various algorithms, and to pause a thread
with finer control than the standard tools.


## CRC
CRC is a parameterized checksum and CRC calculation utility.  It can be
configured to compute a wide range of values, with the following
pre-configured:

* 8-bit checksum
* 16-bit checksum
* 32-bit checksum
* CRC-16
* CRC-16 Modbus
* CRC-16 CCITT
* CRC-16 CCITT (0x1D0F)
* CRC-16 CCITT (Kermit)
* CRC-16 CCITT (XModem)
* CRC-16 DNP
* CRC-32

### Usage
TL;DR: Give it a configuration and a message, get back a number.

* Can be called statically, for simple occasional use:

		String message = "...";
		long result = CRC.calculate(CRC.CRC16, message);

* For more intensive use, it's faster to create an instance and use the
table-driven method:

		CRC crc = new CRC(CRC.CRC16);
		String message = "...";
		long result = crc.calculate(message);

* You can also calculate the CRC incrementally:

		String message = "...";
		Long result = null;
		for (byte b : message.getBytes()) {
			result = CRC.update(CRC.CRC16, result, b);
		}

		CRC crc = new CRC(CRC.CRC16);
		String message = "...";
		Long result = null;
		for (byte b : message.getBytes()) {
			result = crc.update(result, b);
		}


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


## License
These utilities are provided under the terms of the GNU LGPLv3.
