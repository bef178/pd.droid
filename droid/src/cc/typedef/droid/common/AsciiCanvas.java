package cc.typedef.droid.common;

import android.util.SparseIntArray;

/**
 * a 2d coordinate system that supports ...<br/>
 * FIXME have forgotten what and why and how.<br/>
 * Recalling...<br/>
 *
 * @author tanghao
 *
 */
public class AsciiCanvas {

    public static final int NUM_COORDINATE_BITS = 9; // must LE 16
    public static final int MAX_SIDE_LENGTH = 1 << NUM_COORDINATE_BITS;

    private SparseIntArray matrix;

    int originX = 0;
    int originY = 0;

    public AsciiCanvas(int dx, int dy) {
        matrix = new SparseIntArray();
    }

    public void moveTo(int x, int y) {
        this.originX = x;
        this.originY = y;
    }

    public void offset(int dx, int dy) {
        this.originX += dx;
        this.originY += dy;
    }

    public byte get(int x, int y) {
        return (byte) matrix.get(getCoordinate(x, y));
    }

    public void set(int x, int y, byte c) {
        matrix.put(getCoordinate(x, y), c);
    }

    private int getCoordinate(int x, int y) {
        x += originX;
        y += originY;
        if (x >= 0 && x < MAX_SIDE_LENGTH && y >= 0 && y < MAX_SIDE_LENGTH) {
            return (x << NUM_COORDINATE_BITS) | y;
        } else {
            return -1;
        }
    }
}
