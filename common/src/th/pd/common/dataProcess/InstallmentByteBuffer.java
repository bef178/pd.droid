package th.pd.common.dataProcess;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * some thing like smart array & queue & installment savings
 */
public class InstallmentByteBuffer extends OutputStream {

    private static final int INSTALLMENT_BITS = 10;
    private static final int INSTALLMENT_BYTES = 1 << INSTALLMENT_BITS;
    private static final int INSTALLMENT_MASK = INSTALLMENT_BYTES - 1;

    private ArrayList<byte[]> savings = new ArrayList<>();
    private int used = 0;

    private int reader = 0;

    private boolean readonly = false;

    public InstallmentByteBuffer() {
        this(INSTALLMENT_BYTES);
    }

    public InstallmentByteBuffer(int capacity) {
        setupCapacity(capacity);
    }

    public InstallmentByteBuffer append(int b) {
        if (!readonly()) {
            setupCapacity(used + 1);
            put(used++, (byte) (b & 0x3F));
        }
        return this;
    }

    public InstallmentByteBuffer append(byte[] a) {
        return append(a, 0, a.length);
    }

    public InstallmentByteBuffer append(byte[] a, int start, int length) {
        if (readonly()) {
            return this;
        }

        setupCapacity(used + length);

        int i = start;
        int j = start + length;

        if ((used & INSTALLMENT_MASK) != 0) {
            int n = INSTALLMENT_BYTES - (used & INSTALLMENT_MASK);
            if (n > length) {
                n = length;
            }
            System.arraycopy(a, i,
                    savings.get(used >> INSTALLMENT_BITS), used
                            & INSTALLMENT_MASK, n);
            i += n;
            used += n;
        }

        while (i + INSTALLMENT_BYTES < j) {
            System.arraycopy(a, i,
                    savings.get(used >> INSTALLMENT_BITS), 0,
                    INSTALLMENT_BYTES);
            i += INSTALLMENT_BYTES;
            used += INSTALLMENT_BYTES;
        }

        if (i < j) {
            System.arraycopy(a, i,
                    savings.get(used >> INSTALLMENT_BITS), 0, j - i);
            used += j - i;
        }

        return this;
    }

    /**
     * @return a copy of valid in bounds byte array
     */
    private byte[] array() {
        byte[] array = new byte[used];
        int i = 0;
        while (i + INSTALLMENT_BYTES <= used) {
            System.arraycopy(savings.get(i >> INSTALLMENT_BITS), 0, array,
                    i, INSTALLMENT_BYTES);
            i += INSTALLMENT_BYTES;
        }
        System.arraycopy(savings.get(i >> INSTALLMENT_BITS), 0, array, i,
                used - i);
        return array;
    }

    public int capacity() {
        return savings.size() << INSTALLMENT_BITS;
    }

    private int get(int pos) {
        return savings.get(pos >> INSTALLMENT_BITS)[pos & INSTALLMENT_MASK];
    }

    public boolean hasNext() {
        return reader >= 0 && reader < used;
    }

    public boolean isEmpty() {
        return used == 0;
    }

    public int next() {
        if (hasNext()) {
            return get(reader++) & 0xFF;
        } else {
            return -1;
        }
    }

    /**
     * For reading, get the cursor's offset.
     */
    public int offset() {
        return reader;
    }

    public void offset(int offset) {
        if (reader + offset >= 0 && reader + offset < used) {
            reader += offset;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * may throw IndexOutOfBoundsException
     */
    public int peek() {
        if (hasNext()) {
            return get(reader) & 0xFF;
        } else {
            return -1;
        }
    }

    private void put(int pos, byte b) {
        savings.get(pos >> INSTALLMENT_BITS)[pos & INSTALLMENT_MASK] = b;
    }

    public boolean readonly() {
        return readonly;
    }

    /**
     * Writable to read only is a one-way street.
     */
    public void readonly(boolean readonly) {
        if (this.readonly) {
            return;
        }
        this.readonly = readonly;
    }

    /**
     * For writing, simply reset size.<br/>
     */
    public void reset() {
        if (readonly) {
            return;
        }
        used = 0;
    }

    public void reset(int offset) {
        if (readonly) {
            return;
        }
        if (offset >= 0 && offset < used) {
            used -= offset;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Rewind for reading.
     */
    public void rewind() {
        reader = 0;
    }

    private void setupCapacity(int newLength) {
        if (savings == null) {
            savings = new ArrayList<>();
        }
        if (newLength > capacity()) {
            int n = newLength >> INSTALLMENT_BITS;
            if ((newLength & INSTALLMENT_MASK) != 0) {
                ++n;
            }
            for (int i = savings.size() + 1; i <= n; ++i) {
                savings.add(new byte[INSTALLMENT_BYTES]);
            }
        }
    }

    public int size() {
        return used;
    }

    public byte[] toByteArray() {
        return array();
    }

    @Override
    public String toString() {
        try {
            return new String(array(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // unlikely to happen
            throw new Error("Not support utf8");
        }
    }

    /**
     * For writing, erase every slot's content and reset size.<br/>
     */
    public void wipe() {
        if (readonly) {
            return;
        }
        for (byte[] a : savings) {
            Arrays.fill(a, (byte) 0);
        }
        used = 0;
    }

    @Override
    public void write(int oneByte) throws IOException {
        append(oneByte);
    }
}
