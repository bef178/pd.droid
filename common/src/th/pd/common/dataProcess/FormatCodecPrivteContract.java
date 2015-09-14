package th.pd.common.dataProcess;

import th.pd.common.Blob;


public class FormatCodecPrivteContract {

    /**
     * @param ch a code point
     */
    public static byte[] encode(int ch) {
        byte[] utf8 = FormatCodecUnicode.toUtf8(ch);
        byte[] result = new byte[3 + utf8.length * 2];
        result[0] = '\\';
        result[1] = 'x';
        result[2] = FormatCodecUnicode.hexInt2HexByte(utf8.length);
        for (int i = 0; i < utf8.length; ++i) {
            byte[] a = FormatCodecUnicode.toHexBytes(utf8[i]);
            result[3 + i++] = a[0];
            result[3 + i] = a[1];
        }
        return result;
    }

    public static int decode(byte[] a) {
        return decode(new Blob(a, 0));
    }

    public static int decode(Blob blob) {
        expect('\\', blob.next());
        int ch = blob.next();
        switch (ch) {
            case 'a':
                return 0x07;
            case 't':
                return 0x09;
            case 'n':
                return 0x0A;
            case 'v':
                return 0x0B;
            case 'f':
                return 0x0C;
            case 'r':
                return 0x0D;
            case 'x':
                int n = FormatCodecUnicode.hexByte2HexInt(blob.next());
                assert n >= 1 && n <= 6;
                // get utf8bytes
                byte[] utf8 = new byte[n];
                for (int i = 0; i < n; ++i) {
                    int hi = FormatCodecUnicode.hexByte2HexInt(blob.next());
                    int lo = FormatCodecUnicode.hexByte2HexInt(blob.next());
                    utf8[i] = (byte) ((hi << 4) | lo);
                }
                return FormatCodecUnicode.fromUtf8(utf8);
            default:
                return ch;
        }
    }

    private static void expect(int ideal, int actual) {
        if (ideal != actual) {
            throw new IllegalArgumentException();
        }
    }
}
