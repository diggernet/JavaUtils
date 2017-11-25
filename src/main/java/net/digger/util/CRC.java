package net.digger.util;

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
 * Calculates a variety of checksums and CRCs.
 * <p>
 * Based on Michael Barr's public domain CRC implementation found at:<br>
 * <a href="http://www.netrino.com/code/crc.zip">http://www.netrino.com/code/crc.zip</a><br>
 * which in turn is inspired by Ross Williams' "Parameterized Model" here:<br>
 * <a href="http://www.ross.net/crc/download/crc_v3.txt">http://www.ross.net/crc/download/crc_v3.txt</a>
 * <p>
 * Various additions and improvements include adding checksum calculation,
 * optional byte reflection (used by Kermit), and incremental crc calculation.
 * <p>
 * Additional CRC parameters from various places, including:<br>
 * <a href="https://stackoverflow.com/questions/4455257/crc16-checksum-hcs08-vs-kermit-vs-xmodem">https://stackoverflow.com/questions/4455257/crc16-checksum-hcs08-vs-kermit-vs-xmodem</a><br>
 * <a href="https://www.lammertbies.nl/comm/info/crc-calculation.html">https://www.lammertbies.nl/comm/info/crc-calculation.html</a>
 * <p>
 * This might be a good source for more CRC parameters:<br>
 * <a href="http://reveng.sourceforge.net/crc-catalogue/">http://reveng.sourceforge.net/crc-catalogue/</a>
 * 
 * @author walton
 */
public class CRC {
	// ##### Preconfigured Checksums and CRCs.
	
	public static final Config Checksum8 = new ChecksumConfig("8-bit Checksum", 8);
	public static final Config Checksum16 = new ChecksumConfig("16-bit Checksum", 16);
	public static final Config Checksum32 = new ChecksumConfig("32-bit Checksum", 32);
	public static final Config CRC16 = new CRCConfig("CRC-16", 16, 0x8005, 0x0000, 0x0000, true, true, false);
	public static final Config CRC16_Modbus = new CRCConfig("CRC-16 Modbus", 16, 0x8005, 0xFFFF, 0x0000, true, true, false);
	public static final Config CRC16_CCITT = new CRCConfig("CRC-CCITT", 16, 0x1021, 0xFFFF, 0x0000, false, false, false);
	public static final Config CRC16_CCITT_XModem = new CRCConfig("CRC-CCITT XModem", 16, 0x1021, 0x0000, 0x0000, false, false, false);
	public static final Config CRC16_CCITT_0x1D0F = new CRCConfig("CRC-CCITT 0x1D0F", 16, 0x1021, 0x1D0F, 0x0000, false, false, false);
	public static final Config CRC16_CCITT_Kermit = new CRCConfig("CRC-CCITT Kermit", 16, 0x1021, 0x0000, 0x0000, true, true, true);
	public static final Config CRC16_DNP = new CRCConfig("CRC-DNP", 16, 0x3D65, 0x0000, 0xFFFF, true, true, true);
	public static final Config CRC32 = new CRCConfig("CRC-32", 32, 0x04C11DB7L, 0xFFFFFFFFL, 0xFFFFFFFFL, true, true, false);
	
	
	// ##### Internal instance data
	
	private final Config config;
	private final long[] crcTable = new long[256];

	// ##### Instance constructor

	/**
	 * Create an instance with the given CRC config.
	 * <p>
	 * This is only required to use the faster table-driven method.
	 * 
	 * @param config CRC configuration to use.
	 */
	public CRC(Config config) {
		this.config = config;
		if (config.type == Config.Type.CRC) {
			crcInit((CRCConfig)config);
		}
	}


	// ##### Static API methods

	/**
	 * Compute the CRC of a given message using the slower computational method.
	 * <p>
	 * For Checksum configurations, there is actually no difference between fast and slow methods.
	 * 
	 * @param config CRC configuration to use.
	 * @param message Message to calculate CRC for.
	 * @return The CRC of the message.
	 */
	public static long calculate(Config config, String message) {
		return calculate(config, message.getBytes());
	}
	
	/**
	 * Compute the CRC of a given message using the slower computational method.
	 * <p>
	 * For Checksum configurations, there is actually no difference between fast and slow methods.
	 * 
	 * @param config CRC configuration to use.
	 * @param message Message to calculate CRC for.
	 * @return The CRC of the message.
	 */
	public static long calculate(Config config, byte[] message) {
		switch (config.type) {
			case Checksum:
				return checksum((ChecksumConfig)config, message);
			case CRC:
				return crcSlow((CRCConfig)config, message);
			default:
				throw new IllegalArgumentException("Unsupported configuration type: " + config.type);
		}
	}
	
	/**
	 * Update the given CRC with a new byte using the slower computational method.
	 * <p>
	 * For Checksum configurations, there is actually no difference between fast and slow methods.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the CRC.  Use {@code null} to start.
	 * @param b Next byte of the message to calculate CRC for.
	 * @return The CRC of the message so far.
	 */
	public static long update(Config config, Long crc, byte b) {
		switch (config.type) {
			case Checksum:
				return checksumUpdate((ChecksumConfig)config, crc, b);
			case CRC:
				return crcSlowUpdate((CRCConfig)config, crc, b);
			default:
				throw new IllegalArgumentException("Unsupported configuration type: " + config.type);
		}
	}
	

	// ##### Instance API methods

	/**
	 * Compute the CRC of a given message using the faster table-drive method.
	 * <p>
	 * For Checksum configurations, there is actually no difference between fast and slow methods.
	 * 
	 * @param message Message to calculate CRC for.
	 * @return The CRC of the message.
	 */
	public long calculate(String message) {
		return calculate(config, message.getBytes());
	}
	
	/**
	 * Compute the CRC of a given message using the faster table-drive method.
	 * <p>
	 * For Checksum configurations, there is actually no difference between fast and slow methods.
	 * 
	 * @param message Message to calculate CRC for.
	 * @return The CRC of the message.
	 */
	public long calculate(byte[] message) {
		switch (config.type) {
			case Checksum:
				return checksum((ChecksumConfig)config, message);
			case CRC:
				return crcFast((CRCConfig)config, message);
			default:
				throw new IllegalArgumentException("Unsupported configuration type: " + config.type);
		}
	}
	
	/**
	 * Update the given CRC with a new byte using the faster table-driven method.
	 * <p>
	 * For Checksum configurations, there is actually no difference between fast and slow methods.
	 * 
	 * @param crc Current value of the CRC.  Use {@code null} to start.
	 * @param b Next byte of the message to calculate CRC for.
	 * @return The CRC of the message so far.
	 */
	public long update(Long crc, byte b) {
		switch (config.type) {
			case Checksum:
				return checksumUpdate((ChecksumConfig)config, crc, b);
			case CRC:
				return crcFastUpdate((CRCConfig)config, crc, b);
			default:
				throw new IllegalArgumentException("Unsupported configuration type: " + config.type);
		}
	}
	
	
	// ##### Checksum calculations

	/**
	 * Update an incremental checksum with a new byte.
	 * <p>
	 * Used by {@link #update(Config, Long, byte)} and {@link #update(Long, byte)}.
	 * 
	 * @param config Checksum configuration to use.
	 * @param sum Current value of the checksum.  Use {@code null} to start.
	 * @param b Next byte of the message to calculate checksum for.
	 * @return The checksum of the message so far.
	 */
	private static long checksumUpdate(ChecksumConfig config, Long sum, byte b) {
		if (sum == null) {
			sum = config.initialValue;
		}
		sum += b;
		return sum & config.mask;
	}
	
	/**
	 * Compute the checksum of a given message.
	 * <p>
	 * Used by {@link #calculate(Config, byte[])}, {@link #calculate(Config, String)},
	 * {@link #calculate(byte[])}, and {@link #calculate(String)}, 
	 * 
	 * @param config Checksum configuration to use.
	 * @param message Message to calculate checksum for.
	 * @return The checksum of the message.
	 */
	private static long checksum(ChecksumConfig config, byte[] message) {
		long sum = config.initialValue;
		for (byte b : message) {
			sum += b;
		}
		return sum & config.mask;
	}


	// ##### Slower (computational) CRC calculations

	/**
	 * Update an incremental CRC with a new byte using the slower computational method.
	 * <p>
	 * Used by {@link #update(Config, Long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the CRC.  Use {@code null} to start.
	 * @param b Next byte of the message to calculate CRC for.
	 * @return The CRC of the message so far.
	 */
	private static long crcSlowUpdate(CRCConfig config, Long crc, byte b) {
		if (crc == null) {
			crc = config.initialValue;
		} else {
			crc = unfinalize(config, crc);
		}
		crc = crcSlowCore(config, crc, b);
		return finalize(config, crc);
	}
	
	/**
	 * Main part of the slower computational CRC method.
	 * <p>
	 * Used by {@link #crcSlow(CRCConfig, byte[])} and {@link #crcSlowUpdate(CRCConfig, Long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the CRC.
	 * @param b Next byte of the message to calculate CRC for.
	 * @return The CRC of the message so far.
	 */
	private static long crcSlowCore(CRCConfig config, long crc, byte b) {
		// Bring the next byte into the crc.
		long data = b;
		if (config.reflectInputBits) {
			data = reflectBits(data, 8);
		}
		crc ^= (data << (config.bits - 8));
		crc = crcInitCore(config, crc);
		return crc;
	}
	
	/**
	 * Compute the CRC of a given message using the slower computational method.
	 * <p>
	 * Used by {@link #calculate(Config, byte[])} and {@link #calculate(Config, String)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param message Message to calculate CRC for.
	 * @return The CRC of the message.
	 */
	private static long crcSlow(CRCConfig config, byte[] message) {
		long crc = config.initialValue;
		// Perform modulo-2 division, a byte at a time.
		for (byte b : message) {
			crc = crcSlowCore(config, crc, b);
		}
		// The final remainder is the CRC result.
		return finalize(config, crc);
	}


	// ##### CRC table initialization calculations

	/**
	 * Main part of the CRC lookup table population.
	 * <p>
	 * Used by {@link #crcInit(CRCConfig)} and {@link #crcSlowCore(CRCConfig, long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the partial CRC.
	 * @return The partial CRC to add to lookup table.
	 */
	private static long crcInitCore(CRCConfig config, long crc) {
		// Perform modulo-2 division, a bit at a time.
		for (char bit=8; bit>0; bit--) {
			// Try to divide the current data bit.
			if ((crc & config.topBit) > 0) {
				crc = (crc << 1) & config.mask;
				crc ^= config.polynomial;
			} else {
				crc <<= 1;
			}
		}
		return crc;
	}
	
	/**
	 * Populate the partial CRC lookup table.
	 * 
	 * @param config CRC configuration to use.
	 */
	private void crcInit(CRCConfig config) {
		long crc;
		// Compute the remainder of each possible dividend.
		for (char dividend=0; dividend<256; ++dividend) {
			// Start with the dividend followed by zeros.
			crc = (long)dividend << (config.bits - 8);
			crc = crcInitCore(config, crc);
			// Store the result into the table.
			crcTable[dividend] = crc;
		}
	}


	// ##### Faster (table-driven) CRC calculations

	/**
	 * Update an incremental CRC with a new byte using the faster table-driven method.
	 * <p>
	 * Used by {@link #update(Long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the CRC.  Use {@code null} to start.
	 * @param b Next byte of the message to calculate CRC for.
	 * @return The CRC of the message so far.
	 */
	private long crcFastUpdate(CRCConfig config, Long crc, byte b) {
		if (crc == null) {
			crc = config.initialValue;
		} else {
			crc = unfinalize(config, crc);
		}
		crc = crcFastCore(config, crc, b);
		return finalize(config, crc);
	}
	
	/**
	 * Main part of the faster table-driven CRC method.
	 * <p>
	 * Used by {@link #crcFast(CRCConfig, byte[])} and {@link #crcFastUpdate(CRCConfig, Long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the CRC.
	 * @param b Next byte of the message to calculate CRC for.
	 * @return The CRC of the message so far.
	 */
	private long crcFastCore(CRCConfig config, long crc, byte b) {
		// Bring the next byte into the crc.
		char data = (char)b;
		if (config.reflectInputBits) {
			data = (char)reflectBits(data, 8);
		}
		data ^= crc >>> (config.bits - 8);
		crc = (crc << 8) & config.mask;
		crc = crcTable[data] ^ crc;
		return crc;
	}
	
	/**
	 * Compute the CRC of a given message using the faster table-driven method.
	 * <p>
	 * Used by {@link #calculate(byte[])} and {@link #calculate(String)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param message Message to calculate CRC for.
	 * @return The CRC of the message.
	 */
	private long crcFast(CRCConfig config, byte[] message) {
		long crc = config.initialValue;
		// Divide the message by the polynomial, a byte at a time.
		for (byte b : message) {
			crc = crcFastCore(config, crc, b);
		}
		// The final remainder is the CRC.
		return finalize(config, crc);
	}


	// ##### Internal utility methods

	/**
	 * Reorder the bits of a binary sequence, by reflecting
	 * them about the middle position.
	 * <p>
	 * No checking is done that nBits <= 32.
	 * 
	 * @param data
	 * @param nBits
	 * @return The reflection of the original data.
	 */
	private static long reflectBits(long data, int nBits) {
		long reflection = 0x00000000;
		// Reflect the data about the center bit.
		for (char bit=0; bit<nBits; bit++) {
			// If the LSB bit is set, set the reflection of it.
			if ((data & 0x01) > 0) {
				reflection |= (1L << ((nBits - 1) - bit));
			}
			data >>>= 1;
		}
		return reflection;
	}
	
	/**
	 * Reorder the bytes of a binary sequence, by reflecting
	 * them about the middle position.
	 * <p>
	 * No checking is done that nBytes <= 4.
	 * 
	 * @param data
	 * @param nBytes
	 * @return The reflection of the original data.
	 */
	private static long reflectBytes(long data, int nBytes) {
		long reflection = 0x00000000;
		// Reflect the data about the center byte.
		for (char nByte=0; nByte<nBytes; nByte++) {
			// Set the reflection of the low byte.
			reflection |= ((data & 0xFF) << (((nBytes - 1) - nByte) * 8));
			data >>>= 8;
		}
		return reflection;
	}
	
	/**
	 * Perform the final steps of the CRC calculation.
	 * <p>
	 * Used by {@link #crcSlow(CRCConfig, byte[])}, {@link #crcSlowUpdate(CRCConfig, Long, byte)},
	 * {@link #crcFast(CRCConfig, byte[])}, and {@link #crcFastUpdate(CRCConfig, Long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Current value of the CRC.
	 * @return Final value of the CRC.
	 */
	private static long finalize(CRCConfig config, long crc) {
		if (config.reflectOutputBits) {
			crc = reflectBits(crc, config.bits);
		}
		crc ^= config.finalXORValue;
		if (config.reflectOutputBytes) {
			crc= reflectBytes(crc, config.bytes);
		}
		return crc;
	}
	
	/**
	 * Undo the final steps of the CRC calculation, for incremental update.
	 * <p>
	 * Used by {@link #crcSlowUpdate(CRCConfig, Long, byte)} and {@link #crcFastUpdate(CRCConfig, Long, byte)}.
	 * 
	 * @param config CRC configuration to use.
	 * @param crc Previous final value of the CRC.
	 * @return In progress value of the CRC.
	 */
	private static long unfinalize(CRCConfig config, long crc) {
		if (config.reflectOutputBytes) {
			crc = reflectBytes(crc, config.bytes);
		}
		crc ^= config.finalXORValue;
		if (config.reflectOutputBits) {
			crc = reflectBits(crc, config.bits);
		}
		return crc;
	}
	
	
	/**
	 * Base class for checksum and CRC configurations.
	 */
	public static abstract class Config {
		/**
		 * Options for calculation type.
		 */
		public enum Type { Checksum, CRC };
		/**
		 * Type of calculation to make.
		 */
		public final Type type;
		/**
		 * Name of this configuration.
		 */
		public final String name;
		/**
		 * Number of bits in final output.
		 */
		public final int bits;
		/**
		 * Number of bytes in final output.
		 */
		public final int bytes;
		/**
		 * Final output bitmask.
		 */
		public final long mask;
		/**
		 * Initial value for this configuration.
		 */
		public final long initialValue;

		/**
		 * Create a new configuration.
		 * 
		 * @param type Type of calculation to make.
		 * @param name Name of this configuration.
		 * @param bits Number of bits in final output.
		 * @param initialValue Initial value for this configuration.
		 */
		protected Config (Type type, String name, int bits, long initialValue) {
			this.type = type;
			this.name = name;
			this.bits = bits;
			this.bytes = bits / 8;
			this.mask = (long)Math.pow(2, bits) - 1;
			this.initialValue = initialValue;
		}
	}
	
	/**
	 * Checksum configuration class.
	 */
	public static class ChecksumConfig extends Config {
		/**
		 * Create a new checksum configuration.
		 * 
		 * @param name Name of this checksum configuration.
		 * @param bits Number of bits in final output.
		 */
		public ChecksumConfig(String name, int bits) {
			super(Type.Checksum, name, bits, 0);
		}
	}
	
	/**
	 * CRC configuration class.
	 */
	public static class CRCConfig extends Config {
		/**
		 * Polynomial for this CRC.
		 */
		public final long polynomial;
		/**
		 * XOR value to apply to the final CRC.
		 */
		public final long finalXORValue;
		/**
		 * Reverse the bits in the input bytes?
		 */
		public final boolean reflectInputBits;
		/**
		 * Reverse the bits in the output bytes?
		 */
		public final boolean reflectOutputBits;
		/**
		 * Reverse the output bytes?
		 */
		public final boolean reflectOutputBytes;
		/**
		 * Highest bit in the final CRC.
		 */
		public final long topBit;
		
		/**
		 * Create a new CRC configuration.
		 * 
		 * @param name Name of this CRC configuration.
		 * @param bits Number of bits in final output.
		 * @param polynomial Polynomial for this CRC.
		 * @param initialValue Initial value for this CRC configuration.
		 * @param finalXORValue XOR value to apply to the final CRC.
		 * @param reflectInputBits Reverse the bits in the input bytes?
		 * @param reflectOutputBits Reverse the bits in the output bytes?
		 * @param reflectOutputBytes Reverse the output bytes?
		 */
		public CRCConfig(String name, int bits, long polynomial, long initialValue, 
				long finalXORValue, boolean reflectInputBits, boolean reflectOutputBits, boolean reflectOutputBytes) {
			super(Type.CRC, name, bits, initialValue);
			this.polynomial = polynomial;
			this.finalXORValue = finalXORValue;
			this.reflectInputBits = reflectInputBits;
			this.reflectOutputBits = reflectOutputBits;
			this.reflectOutputBytes = reflectOutputBytes;
			this.topBit = 1 << (bits - 1);
		}
	}
}
