package th.pd.common;

public final class Blob {

    public byte[] a = null;

    public int i = 0;

    public Blob() {
        // dummy
    }

    public Blob(byte[] a, int i) {
        this.a = a;
        this.i = i;
    }

    public byte next() {
        return a[i++];
    }
}
