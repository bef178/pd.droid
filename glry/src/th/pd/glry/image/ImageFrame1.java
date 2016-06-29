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

    public ImageFrame1 setAnimAlphaRange(float start, float end) {
        assert start >= 0f && start <= 1f;
        assert end >= 0f && end <= 1f;
        animAlphaStart = (int) (start * 0xFF);
        animAlphaEnd = (int) (end * 0xFF);
        return this;
    }

    /**
     * anchor to center
     */
    public ImageFrame1 setAnimScaleRange(float start, float end) {
        Rect r = getRect();

        animRectStart.set(r);
        animRectStart.right = (int) (r.width() * start + r.left);
        animRectStart.bottom = (int) (r.height() * start + r.top);
        animRectStart.offset((r.right - animRectStart.right) / 2,
                (r.bottom - animRectStart.bottom) / 2);

        animRectEnd.set(r);
        animRectEnd.right = (int) (r.width() * end + r.left);
        animRectEnd.bottom = (int) (r.height() * end + r.top);
        animRectEnd.offset((r.right - animRectEnd.right) / 2,
                (r.bottom - animRectEnd.bottom) / 2);

        return this;
    }

    public void setAnimTransRange(int startX, int startY, int endX, int endY) {
        animRectStart.offsetTo(startX - animRectStart.width() / 2,
                startY - animRectStart.height() / 2);
        animRectEnd.offsetTo(endX - animRectEnd.width() / 2,
                endY - animRectEnd.height() / 2);
    }
}
