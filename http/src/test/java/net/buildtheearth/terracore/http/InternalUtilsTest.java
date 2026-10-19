package net.buildtheearth.terracore.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InternalUtilsTest {

    @Test
    void canComputeHexString() {
        // Empty input
        assertEquals("", InternalUtils.hexString(new byte[0]));

        // All zeros and all max values
        assertEquals("00000000", InternalUtils.hexString(new byte[] { 0x00, 0x00, 0x00, 0x00 }));
        assertEquals("ffffffff", InternalUtils.hexString(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }));

        // Single byte
        assertEquals("00", InternalUtils.hexString(new byte[] { 0x00 }));
        assertEquals("ff", InternalUtils.hexString(new byte[] { (byte) 0xff }));
        assertEquals("0f", InternalUtils.hexString(new byte[] { 0x0f }));
        assertEquals("f0", InternalUtils.hexString(new byte[] { (byte) 0xf0 }));

        // All hex digits: 0-9, a-f
        assertEquals("0123456789abcdef", InternalUtils.hexString(new byte[] {
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef
        }));

        // deadbeef
        assertEquals("deadbeef", InternalUtils.hexString(new byte[] {
                (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef
        }));

        // Odd-length array
        assertEquals("ab0c", InternalUtils.hexString(new byte[] { (byte) 0xab, (byte) 0x0c }));

        // Negative byte values (Java bytes are signed)
        assertEquals("80", InternalUtils.hexString(new byte[] { (byte) 0x80 }));
        assertEquals("7f", InternalUtils.hexString(new byte[] { 0x7f }));
    }
}
