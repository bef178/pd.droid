package th.mediaPlay;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class ImageStatus {
    Bitmap bitmap;
    int alpha;
    Rect rect;
    int dx;
    int dy;

    public ImageStatus() {
        this.rect = new Rect();
        reset(null);
    }

    public void alpha(float alpha) {
        if (alpha > 1f) {
            alpha = 1f;
        }
        if (alpha < 0f) {
            alpha = 0f;
        }
        this.alpha = (int) (alpha * 0xFF);
    }

    public boolean isValid() {
        return this.bitmap != null;
    }

    public void offset(int dx, int dy) {
        rect.offset(dx - this.dx, dy - this.dy);
        this.dx = dx;
        this.dy = dy;
    }

    public void reset() {
        this.alpha = 0xFF;

        rect.left = 0;
        rect.top = 0;
        if (bitmap != null) {
            rect.right = bitmap.getWidth();
            rect.bottom = bitmap.getHeight();
        } else {
            rect.right = 0;
            rect.bottom = 0;
        }
        dx = 0;
        dy = 0;
    }

    public void reset(Bitmap bitmap) {
        this.bitmap = bitmap;
        reset();
    }

    public void scale(float scale) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        rect.right = rect.left + (int) (scale * w);
        rect.bottom = rect.top + (int) (scale * h);
    }
}
