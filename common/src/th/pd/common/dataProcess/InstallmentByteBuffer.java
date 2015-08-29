package th.pd.common.dataProcess;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * some thing like smart array & queue & installment savings
 */
public class InstallmentByteBuffer {

    private static final int INSTALLMENT_BITS = 10;
    private static final int INSTALLMENT_BYTES = 1 << INSTALLMENT_BITS;
    private static final int INSTALLMENT_MASK = INSTALLMENT_BYTES - 1;

    private byte[] savings;
    private int used = 0;

    private int reader = 0;

    private boolean readonly = false;

    public InstallmentByteBuffer() {
        this(1 << INSTALLMENT_BITS);
    }

    public InstallmentByteBuffer(int capacity) {
        setupCapacity(capacity);
    }

    public InstallmentByteBuffer(byte[] a, int used, boolean readonly) {
        assert a != null;
        this.savings = a;
        this.used = used;
        this.readonly = readonly;
    }

    public InstallmentByteBuffer append(byte b) {
		if (!readonly()) {
			setupCapacity(used + 1);
			savings[used++] = b;
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

        setupCapacity(used + a.length);
        System.arraycopy(a, start, savings, used, length);
        used += length;
        return this;
    }

    /**
     * @return a copy of valid in bounds byte array
     */
    private byte[] array() {
        return Arrays.copyOf(savings, used);
    }

    /**
     * @return the inner byte array
     */
    public byte[] bytes() {
        return savings;
    }

    public int capacity() {
        return savings.length;
    }

    public boolean hasNext() {
        return reader >= 0 && reader < used;
    }

    public boolean isEmpty() {
        return used == 0;
    }

    public byte next() {
        if (hasNext()) {
            return savings[reader++];
        } else {
            throw new IndexOutOfBoundsException();
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
    public byte peek() {
        if (hasNext()) {
            return savings[reader];
        } else {
            throw new IndexOutOfBoundsException();
        }
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

    public void reset(int n) {
        if (readonly) {
            return;
        }
        if (n >= 0 && n < used) {
            used -= n;
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
            savings = new byte[0];
        }
        if (newLength > savings.length) {
            int newCapacity = newLength & ~INSTALLMENT_MASK;
            if (newCapacity < newLength) {
                newCapacity += INSTALLMENT_BYTES;
            }
            savings = Arrays.copyOf(savings, newCapacity);
        }
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
        Arrays.fill(savings, (byte) 0);
        used = 0;
    }
}
