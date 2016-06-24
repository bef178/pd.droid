package th.pd.glry.elementary;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * **Used by <code>{@link FramedView}</code> for painting**<br/>
 * For an image, there're several rects:<br/>
 * <code>origRect</code> - the bitmap dimension<br/>
 * <code>hostRect</code> - the container dimension<br/>
 * <code>imageFitRect</code> - the best fit rect results from the above two
 * rects and thus calculated <code>fitScale</code><br/>
 *
 * @author tanghao
 */
class Frame {

    Bitmap bitmap;

    // where the whole bitmap is drew into, while onDraw() starts at (0,0)
    Rect rect;

    // an instantaneous value that may change during animation, finalize to 0xFF
    private int alpha;

    public Frame() {
        rect = new Rect();
        init(null);
    }

    public void applyOffset(int[] coord) {
        moveTo(coord[0], coord[1]);
    }

    /**
     * anchor to (left,top)
     */
    public void applyScale(float scale) {
        if (scale > 0f) {
            rect.right = rect.left + (int) (scale * bitmap.getWidth());
            rect.bottom = rect.top + (int) (scale * bitmap.getHeight());
        }
    }

    public float findScale() {
        if (bitmap != null && !rect.isEmpty()) {
            return 1f * rect.width() / bitmap.getWidth();
        }
        return 0f;
    }

    public int getAlpha() {
        return this.alpha;
    }

    /**
     * restore the image attributes and move the image to the origin point
     */
    public void init(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.alpha = 0xFF;
        if (bitmap == null) {
            this.rect.setEmpty();
        } else {
            this.rect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
    }

    public boolean isValid() {
        return bitmap != null && !rect.isEmpty();
    }

    public void move(int dx, int dy) {
        rect.offset(dx, dy);
    }

    public void moveTo(int x, int y) {
        rect.offsetTo(x, y);
    }

    public void moveToCenter(int hostWidth, int hostHeight) {
        moveTo((hostWidth - rect.width()) / 2,
                (hostHeight - rect.height()) / 2);
    }

    /**
     * accept [0f, 1f]
     */
    public void setAlpha(float alpha) {
        if (alpha > 1f) {
            alpha = 1f;
        } else if (alpha < 0f) {
            alpha = 0f;
        }
        this.alpha = (int) (alpha * 0xFF);
    }

    /**
     * accept [0, 0xFF]
     */
    public void setAlpha(int alpha) {
        if (alpha > 0xFF) {
            alpha = 0xFF;
        }
        if (alpha < 0) {
            alpha = 0;
        }
        this.alpha = alpha;
    }
}
