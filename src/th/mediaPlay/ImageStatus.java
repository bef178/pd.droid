package th.mediaPlay;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class ImageStatus {

    int alpha = 0xFF;

    Bitmap bitmap = null;

    Rect rect = new Rect();

    public void alpha(float alpha) {
        if (alpha > 1f) {
            alpha = 1f;
        } else if (alpha < 0f) {
            alpha = 0f;
        }
        this.alpha = (int) (alpha * 0xFF);
    }

    /**
     * reset each attribute to its default value
     */
    public void clear() {
        this.alpha = 0xFF;

        this.bitmap = null;

        this.rect.left = 0;
        this.rect.top = 0;
        this.rect.right = 0;
        this.rect.bottom = 0;
    }

    public void initialize(Bitmap bitmap) {
        clear();
        this.bitmap = bitmap;
        if (this.bitmap != null) {
            this.rect.right = bitmap.getWidth();
            this.rect.bottom = bitmap.getHeight();
        }
    }

    public boolean isValid() {
        return bitmap != null;
    }

    public void offset(int dx, int dy) {
        rect.offsetTo(dx, dy);
    }

    /**
     * reset but keep the image itself
     */
    public void restore() {
        initialize(this.bitmap);
    }

    public void scale(float scale) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        rect.right = rect.left + (int) (scale * w);
        rect.bottom = rect.top + (int) (scale * h);
    }
}
