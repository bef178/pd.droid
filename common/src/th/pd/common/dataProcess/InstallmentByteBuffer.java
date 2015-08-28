package th.pd.common.dataProcess;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * some thing like smart array & queue & installment savings
 */
public class InstallmentByteBuffer {

    private static final int INSTALLMENT_BITS = 10;

    private byte[] bytes;
    private int used = 0;

    private int reader = 0;

    private boolean ro = false;

    public InstallmentByteBuffer() {
        this(1 << INSTALLMENT_BITS);
    }

    public InstallmentByteBuffer(int capacity) {
        setupCapacity(capacity);
    }

    public InstallmentByteBuffer(byte[] ba, int used, boolean readonly) {
        assert ba != null;
        this.bytes = ba;
        this.used = used;
        this.ro = readonly;
    }

    public void append(byte b) {
        pushByte(b);
    }

    public void append(byte[] ba) {
        pushBytes(ba, 0, ba.length);
    }

    public void append(byte[] ba, int start, int length) {
        pushBytes(ba, start, length);
    }

    /**
     * @return a copy of valid in bounds byte array
     */
    private byte[] array() {
        return Arrays.copyOf(bytes, used);
    }

    /**
     * @return the inner byte array
     */
    public byte[] bytes() {
        return bytes;
    }

    public int capacity() {
        return bytes.length;
    }

    public boolean hasNext() {
        return reader >= 0 && reader < used;
    }

    public boolean isEmpty() {
        return used == 0;
    }

    public byte next() {
        if (hasNext()) {
            return bytes[reader++];
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
            return bytes[reader];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    private void pushByte(byte b) {
        if (readonly()) {
            return;
        }
        setupCapacity(used + 1);
        bytes[used++] = b;
    }

    private void pushBytes(byte[] ba, int start, int length) {
        if (readonly()) {
            return;
        }
        setupCapacity(used + ba.length);
        System.arraycopy(ba, start, bytes, used, length);
        used += length;
    }

    public boolean readonly() {
        return ro;
    }

    /**
     * Writable to read only is a one-way street.
     */
    public void readonly(boolean readonly) {
        if (this.ro) {
            return;
        }
        this.ro = readonly;
    }

    /**
     * For writing, simply reset size.<br/>
     */
    public void reset() {
        if (ro) {
            return;
        }
        used = 0;
    }

    public void reset(int n) {
        if (ro) {
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
        if (bytes == null) {
            bytes = new byte[0];
        }
        if (newLength > bytes.length) {
            int installment = 1 << INSTALLMENT_BITS;
            int newCapacity = newLength & ~(installment - 1);
            if (newCapacity < newLength) {
                newCapacity += installment;
            }
            bytes = Arrays.copyOf(bytes, newCapacity);
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
        if (ro) {
            return;
        }
        Arrays.fill(bytes, (byte) 0);
        used = 0;
    }
}
