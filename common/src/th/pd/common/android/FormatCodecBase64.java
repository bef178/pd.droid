package th.pd.common.android;

import java.io.IOException;
import java.io.OutputStream;

import android.annotation.SuppressLint;

import th.pd.common.dataProcess.InstallmentByteBuffer;

@SuppressLint("Assert")
class FormatCodecBase64 {

    private static final char[] ENCODE_ALPHABET = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/'
    };

    byte[] encode(byte[] a, int i, int n, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix)
            throws IOException {
        InstallmentByteBuffer o = new InstallmentByteBuffer();
        encode(a, i, n, bytesPerLine, firstOffset, prefix, suffix, o);
        return o.toByteArray();
    }

    /**
     * it is user who should put the prefix/suffix before/after invoke this method if necessary
     */
    OutputStream encode(byte[] a, int i, int n, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix, OutputStream o)
            throws IOException {
        assert a != null;
        assert i >= 0 && i < a.length;
        assert n >= 0 && i + n <= a.length;
        assert bytesPerLine > 0;
        assert firstOffset >= 0;
        if (firstOffset > 0) {
            assert bytesPerLine >= prefix.length + suffix.length
                    + firstOffset;
        } else {
            assert bytesPerLine > prefix.length + suffix.length;
        }

        InstallmentByteBuffer ibb = encode(a, i, n);
        ibb.rewind();
        int m = ibb.size();

        // the first line
        int rest = bytesPerLine - firstOffset - suffix.length;
        if (rest > m) {
            rest = m;
        }
        for (int j = 0; j < rest; ++i) {
            o.write(ibb.next());
        }
        m -= rest;
        if (m > 0) {
            o.write(suffix);
        }

        // middle lines
        rest = bytesPerLine - prefix.length - suffix.length;
        while (m >= rest) {
            o.write(prefix);
            while (rest-- > 0) {
                o.write(ibb.next());
            }
            o.write(suffix);
            m -= rest;
        }

        if (m > 0) {
            rest = m;
            o.write(prefix);
            while (rest-- > 0) {
                o.write(ibb.next());
            }
        }
        return o;
    }

    private InstallmentByteBuffer encode(byte[] a, int i, int n) {
        // encode
        InstallmentByteBuffer ibb = new InstallmentByteBuffer();
        int j = i + n;
        while (i + 3 <= j) {
            ibb.append(encode3(a, i));
            i += 3;
        }
        switch (j - i) {
            case 2:
                ibb.append(encode3(new byte[] {
                        a[i], a[i + 1], 0
                }, i));
                ibb.append('=');
                break;
            case 1:
                ibb.append(encode3(new byte[] {
                        a[i], 0, 0
                }, i));
                ibb.append('=');
                ibb.append('=');
                break;
        }
        return ibb;
    }

    private byte[] encode3(byte[] a, int i) {
        return encode3(a, i, new byte[4], 0);
    }

    private byte[] encode3(byte[] a, int i, byte[] result, int offset) {
        assert a != null;
        assert i >= 0 && i + 3 < a.length;
        assert result != null;
        assert offset >= 0 && offset + 4 < result.length;
        result[0] = (byte) ENCODE_ALPHABET[a[i] >>> 2];
        result[1] = (byte) ENCODE_ALPHABET[((0x03 & a[i]) << 4)
                | (a[i + 1] >>> 4)];
        result[2] = (byte) ENCODE_ALPHABET[((0x0F & a[i + 1]) << 2)
                | (a[i + 2] >>> 6)];
        result[3] = (byte) ENCODE_ALPHABET[0x3F & a[i + 2]];
        return result;
    }
}
