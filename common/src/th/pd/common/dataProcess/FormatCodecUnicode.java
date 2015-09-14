package th.pd.common.dataProcess;

import java.util.Arrays;

public class FormatCodecUnicode {

    static final byte[] HEX_DIGIT = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

    private static final int NUM_HEX_BYTES_FROM_INT = Integer.SIZE
            / Byte.SIZE * 2;

    public static int fromUtf8(byte[] utf8) {
        assert utf8 != null && utf8.length > 0;

        int n = 0;
        int ch = utf8[0];
        if ((ch & 0x80) == 0) {
            return ch;
        } else if ((ch & 0x40) == 0) {
            return -1;
        } else if ((ch & 0x20) == 0) {
            n = 2;
            ch = ch & 0x1F;
        } else if ((ch & 0x10) == 0) {
            n = 3;
            ch = ch & 0x0F;
        } else if ((ch & 0x08) == 0) {
            n = 4;
            ch = ch & 0x07;
        } else if ((ch & 0x04) == 0) {
            n = 5;
            ch = ch & 0x03;
        } else if ((ch & 0x02) == 0) {
            n = 6;
            ch = ch & 0x01;
        } else {
            return -1;
        }

        for (int i = 1; i < n; ++i) {
            ch = utf8[i] & 0x3F | (ch << 6);
        }
        return ch;
    }

    /**
     * '0' => 0, 'A' => 10, etc<br/>
     * @return -1 if given an invalid hex
     */
    public static int hexByte2HexInt(int hexByte) {
        assert hexByte >= 0;
        switch (hexByte) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return hexByte - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return hexByte - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return hexByte - 'a' + 10;
            default:
                return -1;
        }
    }

    /**
     * 0x00 => '0', 0x0A => 'A', etc<br/>
     * <br/>
     * may throw IndexOutOfBoundsException<br/>
     */
    public static byte hexInt2HexByte(int i) {
        return HEX_DIGIT[i];
    }

    /**
     * [0-9A-Fa-f]
     */
    public static boolean isHexByte(int ch) {
        return hexByte2HexInt(ch) >= 0;
    }

    /**
     * for a byte, this implementation is better than {@link #toHexBytes(int)}
     */
    public static byte[] toHexBytes(byte b) {
        return new byte[] {
                HEX_DIGIT[0x0F & (b >> 4)],
                HEX_DIGIT[0x0F & (b >> 0)]
        };
    }

    /**
     * the functionality is very similar to
     * <code>Integer.toHexString(ch).toUpperCase().getBytes("UTF-8");</code><br/>
     * usually, it is invoked with escape purpose, so defaults to best fit
     *
     * @param ch the code point
     * @return character-like presentation of the code point, in bytes
     */
    public static byte[] toHexBytes(int ch) {
        return toHexBytes(ch, 0);
    }

    /**
     * @param ch a code point
     * @param preferredLength expects shortest array if 0
     * @return an even-length (at least 2) byte array with char-presenting of
     *         ch;
     */
    public static byte[] toHexBytes(int ch, int preferredLength) {
        assert ch >= 0;
        assert preferredLength >= 0
                && preferredLength <= NUM_HEX_BYTES_FROM_INT;

        int pos = NUM_HEX_BYTES_FROM_INT;
        byte[] bytes = new byte[pos];
        while (ch != 0 && pos-- > 0) {
            bytes[pos] = hexInt2HexByte(ch & 0x0F);
            ch >>>= 4;
        }

        int startIndex = NUM_HEX_BYTES_FROM_INT - preferredLength;
        while (pos > startIndex
                || (pos & 0x01) == 1
                || pos == NUM_HEX_BYTES_FROM_INT) {
            bytes[--pos] = '0';
        }
        if (pos == 0) {
            return bytes;
        } else {
            return Arrays.copyOfRange(bytes, pos, NUM_HEX_BYTES_FROM_INT);
        }
    }

    public static byte[] toUcs4(int ch) {
        assert ch >= 0;
        byte[] ucs4 = new byte[] {
                0, 0, 0, 0
        };
        int pos = 4;
        while (ch != 0) {
            ucs4[--pos] = (byte) (0xFF & ch);
            ch >>>= 8;
        }
        return ucs4;
    }

    /**
     * a fast encoder without checking content<br/>
     * if checking required, use
     * <code>new String(Character.toChars(ch)).getBytes("UTF-8")</code>
     *
     * @return standard utf8 byte sequence
     */
    public static byte[] toUtf8(int ch) {
        assert ch >= 0;

        int n = utf8Length(ch);

        if (n == 1) {
            // ASCII
            byte[] utf8 = new byte[1];
            utf8[0] = (byte) ch;
            return utf8;
        }

        byte[] utf8 = new byte[n];
        int pos = n;
        while (ch != 0) {
            // set data
            int b = ch & 0x3F | 0x80;
            utf8[--pos] = (byte) b;
            ch >>>= 6;
        }

        // set leading bits
        utf8[0] |= 0xFF >> (8 - n) << (8 - n);

        return utf8;
    }

    // UTF-8 uses 6b * 5 + 1b = 31b, the highest bit is not used
    private static int utf8Length(int ch) {
        assert ch >= 0;
        int bytes = 0;
        if (ch <= 0x7F) {
            bytes = 1;
        } else if (ch <= 0x7FF) {
            // LE 11-bit: 0000 0111 1111 1111 => 1101 1111 1011 1111
            //                  ~~~ ~~~~ ~~~~ =>    ~ ~~~~   ~~ ~~~~
            bytes = 2;
        } else if (ch <= 0xFFFF) {
            // LE 16b
            bytes = 3;
        } else if (ch <= 0x1FFFFF) {
            // LE 21b
            bytes = 4;
        } else if (ch <= 0x3FFFFFF) {
            // LE 26b
            bytes = 5;
        } else if (ch <= 0x7FFFFFFF) {
            // LE 31b
            bytes = 6;
        }
        return bytes;
    }

    private FormatCodecUnicode() {
        // private dummy
    }
}
