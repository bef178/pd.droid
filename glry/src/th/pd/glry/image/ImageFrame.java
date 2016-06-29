package th.pd.glry.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * **Used by <code>{@link ImageDisplay}</code> for painting**<br/>
 */
class ImageFrame {

    private Bitmap bitmap;
    private int alpha;
    private Rect rect = new Rect();

    public ImageFrame() {
        init(null);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null && alpha > 0 && !rect.isEmpty()) {
            paint.setAlpha(alpha);
            canvas.drawBitmap(bitmap, null, rect, paint);
        }
    }

    public int getAlpha() {
        return alpha;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Rect getRect() {
        return rect;
    }

    /**
     * scale based on bitmap's dimen<br/>
     */
    public float getScale() {
        assert bitmap != null;
        return 1f * rect.width() / bitmap.getWidth();
    }

    public ImageFrame init(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (bitmap == null) {
            this.rect.setEmpty();
        } else {
            this.rect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
        this.alpha = 0xFF;
        return this;
    }

    public void move(int dx, int dy) {
        rect.offset(dx, dy);
    }

    public void moveTo(int x, int y) {
        rect.offsetTo(x, y);
    }

    /**
     * accept [0f,1f]
     */
    public void setAlpha(float alpha) {
        assert alpha >= 0f && alpha <= 1f;
        alpha = (int) (alpha * 0xFF);
    }

    /**
     * scale based on bitmap's dimen<br/>
     * anchor to (left,top)<br/>
     */
    public void setScale(float scale) {
        assert bitmap != null;
        assert scale > 0;
        rect.right = rect.left + (int) (scale * bitmap.getWidth());
        rect.bottom = rect.top + (int) (scale * bitmap.getHeight());
    }
}
