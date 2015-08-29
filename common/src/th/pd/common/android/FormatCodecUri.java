package th.pd.common.android;

import java.util.BitSet;

/**
 * rfc3986
 */
class FormatCodecUri {

    public static final byte[] DIGIT = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

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

    public static byte getHi(int ch) {
        return DIGIT[0x0F & (ch >> 4)];
    }

    public static byte getLo(int ch) {
        return DIGIT[0x0F & ch];
    }
}
