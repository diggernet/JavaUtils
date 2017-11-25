package net.digger.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import net.digger.util.CRC.Config;

import org.junit.Test;

public class CRCTest {
	private static final String TEST1 = "123456789";
	private static final String TEST2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private void testCRC(Config config, String str, long crcValue) {
//		System.out.printf("%s: %s -> 0x%s\n", config.name, str, Long.toHexString(crcValue));
		long crc1 = CRC.calculate(config, str);
//		System.out.println(Long.toHexString(crc1));
		assertThat(crc1, equalTo(crcValue));

		CRC crc = new CRC(config);
		long crc2 = crc.calculate(str);
//		System.out.println(Long.toHexString(crc2));
		assertThat(crc2, equalTo(crcValue));

		Long crc3 = null;
		for (byte b : str.getBytes()) {
			crc3 = CRC.update(config, crc3, b);
		}
//		System.out.println(Long.toHexString(crc3));
		assertThat(crc3, equalTo(crcValue));

		Long crc4 = null;
		for (byte b : str.getBytes()) {
			crc4 = crc.update(crc4, b);
		}
//		System.out.println(Long.toHexString(crc4));
		assertThat(crc4, equalTo(crcValue));
	}

	@Test
	public void testChecksum8() {
		testCRC(CRC.Checksum8, TEST1, 0xDD);
		testCRC(CRC.Checksum8, TEST2, 0xDF);
	}

	@Test
	public void testChecksum16() {
		testCRC(CRC.Checksum16, TEST1, 0x01DD);
		testCRC(CRC.Checksum16, TEST2, 0x07DF);
	}

	@Test
	public void testChecksum32() {
		testCRC(CRC.Checksum32, TEST1, 0x01DD);
		testCRC(CRC.Checksum32, TEST2, 0x07DF);
	}
	
	@Test
	public void testCRC16() {
		testCRC(CRC.CRC16, TEST1, 0xBB3D);
		testCRC(CRC.CRC16, TEST2, 0x18E7);
	}

	@Test
	public void testCRC16_Modbus() {
		testCRC(CRC.CRC16_Modbus, TEST1, 0x4B37);
		testCRC(CRC.CRC16_Modbus, TEST2, 0xFE85);
	}

	@Test
	public void testCRC16_CCITT_XModem() {
		testCRC(CRC.CRC16_CCITT_XModem, TEST1, 0x31C3);
		testCRC(CRC.CRC16_CCITT_XModem, TEST2, 0xE8AF);
	}

	@Test
	public void testCRC16_CCITT() {
		testCRC(CRC.CRC16_CCITT, TEST1, 0x29B1);
		testCRC(CRC.CRC16_CCITT, TEST2, 0xD8E1);
	}

	@Test
	public void testCRC16_CCITT_0x1D0F() {
		testCRC(CRC.CRC16_CCITT_0x1D0F, TEST1, 0xE5CC);
		testCRC(CRC.CRC16_CCITT_0x1D0F, TEST2, 0x4430);
	}

	@Test
	public void testCRC16_CCITT_Kermit() {
		testCRC(CRC.CRC16_CCITT_Kermit, TEST1, 0x8921);
		testCRC(CRC.CRC16_CCITT_Kermit, TEST2, 0x5EB6);
	}

	@Test
	public void testCRC16_DNP() {
		testCRC(CRC.CRC16_DNP, TEST1, 0x82EA);
		testCRC(CRC.CRC16_DNP, TEST2, 0x6CE7);
	}

	@Test
	public void testCRC32() {
		testCRC(CRC.CRC32, TEST1, 0xCBF43926L);
		testCRC(CRC.CRC32, TEST2, 0xABF77822L);
	}
}
