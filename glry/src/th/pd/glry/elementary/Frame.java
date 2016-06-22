package th.pd.glry.elementary;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * **Used by <code>{@link FramedView}</code> for paint**<br/>
 * For an image, there're several rects:<br/>
 * <code>imageOrigRect</code> - the bitmap dimension<br/>
 * <code>hostRect</code> - the container dimension<br/>
 * <code>imageFitRect</code> - the best fit rect results from the above two rects
 * and thus calculated <code>fitScale</code><br/>
 *
 * @author tanghao
 */
class Frame {

    private static int[] findCentralizedOffset(int imageWidth,
            int imageHeight, int containerWidth, int containerHeight) {
        return new int[] {
                (containerWidth - imageWidth) / 2,
                (containerHeight - imageHeight) / 2
        };
    }

    /**
     * Principle: the container contains the scaled image
     */
    private static float findFitScale(int originalWidth,
            int originalHeight,
            int containerWidth, int containerHeight) {
        if (originalWidth <= 0 || originalHeight <= 0
                || containerWidth <= 0 || containerHeight <= 0) {
            return 0f;
        }

        if (containerWidth < originalWidth
                || containerHeight < originalHeight) {
            return Math.min(
                    1f * containerWidth / originalWidth,
                    1f * containerHeight / originalHeight);
        }
        return 1f;
    }

    Bitmap bitmap;

    // where the whole bitmap is drew into, while onDraw() starts at (0,0)
    Rect rect;

    // an instantaneous value that may change during animation, finalize to 0xFF
    private int alpha;

    private float fitScale;

    public Frame() {
        reset();
    }

    /**
     * accept [0f, 1f]
     */
    public void applyAlpha(float alpha) {
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
    public void applyAlpha(int alpha) {
        if (alpha > 0xFF) {
            alpha = 0xFF;
        }
        if (alpha < 0) {
            alpha = 0;
        }
        this.alpha = alpha;
    }

    public void applyOffset(int x, int y) {
        rect.offsetTo(x, y);
    }

    public void applyOffset(int[] coord) {
        applyOffset(coord[0], coord[1]);
    }

    /**
     * the pivot is (left,top)
     */
    public void applyScale(float scale) {
        if (scale <= 0f) {
            return;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        rect.right = rect.left + (int) (scale * w);
        rect.bottom = rect.top + (int) (scale * h);
    }

    public void centralize(int hostWidth, int hostHeight) {
        applyOffset(findCentralizedOffset(rect.width(), rect.height(),
                hostWidth, hostHeight));
    }

    public float findFloatAlpha() {
        return alpha / 255f;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public float getFitScale() {
        return this.fitScale;
    }

    /**
     * restore the image attributes and move the image to the origin
     */
    private void init(Bitmap bitmap) {
        if (bitmap == null) {
            reset();
            return;
        }
        this.bitmap = bitmap;
        this.rect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        this.alpha = 0xFF;
        this.fitScale = 1f;
    }

    public boolean isValid() {
        return bitmap != null;
    }

    /**
     * reset each attribute to its default value
     */
    public void reset() {
        this.bitmap = null;
        if (this.rect == null) {
            this.rect = new Rect();
        } else {
            this.rect.setEmpty();
        }
        this.alpha = 0xFF;
        this.fitScale = 1f;
    }

    public void resetAndFit(Bitmap bitmap, int hostWidth, int hostHeight) {
        init(bitmap);

        if (bitmap == null
                || hostWidth <= 0 || hostHeight <= 0) {
            return;
        }

        this.fitScale = findFitScale(
                bitmap.getWidth(), bitmap.getHeight(),
                hostWidth, hostHeight);
        applyScale(this.fitScale);
        applyOffset(findCentralizedOffset(
                this.rect.width(), this.rect.height(),
                hostWidth, hostHeight));
    }

    /**
     * just the setter for fit scale, no couple with any operation
     */
    public void updateFitScale(int hostWidth, int hostHeight) {
        this.fitScale = findFitScale(
                this.bitmap.getWidth(), this.bitmap.getHeight(),
                hostWidth, hostHeight);
    }
}
