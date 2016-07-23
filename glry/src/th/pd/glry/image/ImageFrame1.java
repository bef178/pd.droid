package th.pd.glry.image;

import android.graphics.Rect;

class ImageFrame1 extends ImageFrame {

    private int animAlphaStart;
    private int animAlphaEnd;
    private Rect animRectStart = new Rect();
    private Rect animRectEnd = new Rect();

    public ImageFrame1 initAnim() {
        animAlphaStart = 0xFF;
        animAlphaEnd = 0xFF;
        animRectStart.set(getRect());
        animRectEnd.set(getRect());
        return this;
    }

    public void onAnimUpdate(float fx) {
        setAlpha((animAlphaEnd - animAlphaStart) * fx + animAlphaStart);
        getRect().set(
                (int) ((animRectEnd.left - animRectStart.left)
                        * fx + animRectStart.left),
                (int) ((animRectEnd.top - animRectStart.top)
                        * fx + animRectStart.top),
                (int) ((animRectEnd.right - animRectStart.right)
                        * fx + animRectStart.right),
                (int) ((animRectEnd.bottom - animRectStart.bottom)
                        * fx + animRectStart.bottom));
    }

    public ImageFrame1 setAnimAlpha(float start, float end) {
        assert start >= 0f && start <= 1f;
        assert end >= 0f && end <= 1f;
        animAlphaStart = (int) (start * 0xFF);
        animAlphaEnd = (int) (end * 0xFF);
        return this;
    }

    public void setAnimRects(Rect start, Rect end) {
        assert start != null;
        assert end != null;
        animRectStart.set(start);
        animRectEnd.set(end);
    }
}
