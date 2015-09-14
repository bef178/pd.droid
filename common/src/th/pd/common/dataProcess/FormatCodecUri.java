package th.pd.common.dataProcess;

import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

/**
 * rfc3986
 */
public class FormatCodecUri {

    private static final BitSet ENCODE_ALPHABET_EXCLUDED =
            new BitSet(128);

    static {
        final int[] RESERVED = {
                '!', '*', '(', ')', ';', ':', '@', '&', '=',
                '+', '$', ',', '/', '?', '#', '[', ']', '\''
        };
        final int[] SPECIAL = {
                '-', '_', '.', '~'
        };

        for (int i = 'A'; i <= 'Z'; ++i) {
            ENCODE_ALPHABET_EXCLUDED.set(i);
        }
        for (int i = 'a'; i <= 'z'; ++i) {
            ENCODE_ALPHABET_EXCLUDED.set(i);
        }
        for (int i = '0'; i <= '9'; ++i) {
            ENCODE_ALPHABET_EXCLUDED.set(i);
        }
        for (int i = 0; i < RESERVED.length; ++i) {
            ENCODE_ALPHABET_EXCLUDED.set(RESERVED[i]);
        }
        for (int i = 0; i < SPECIAL.length; ++i) {
            ENCODE_ALPHABET_EXCLUDED.set(SPECIAL[i]);
        }
    }

    public static boolean shouldEncode(int ch) {
        return !ENCODE_ALPHABET_EXCLUDED.get(ch);
    }

    public static OutputStream encodeAndPut(byte b, OutputStream ostream)
            throws IOException {
        ostream.write('%');
        ostream.write(FormatCodecUnicode.toHexBytes(b));
        return ostream;
    }
}
