package th.pd.glry.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * As MVP but the presenter is flatten into the view.<br/>
 * <br/>
 * Note the terms.<br/>
 * It always animates from "src" to the "dst", which is in chronological.
 * While, the "dst" may be the "next" or the "prev", i.e. the neighborhood in
 * location.<br/>
 * <br/>
 * Two functionalities provided by this class:<br/>
 * &emsp;- display a single image: zoom, move, restore<br/>
 * &emsp;- anim from one image to another: switch, scroll, fall-back<br/>
 * <br/>
 * For anim, there's a mapping: y = f(x), where x stands for the elapsed
 * time(fraction), y stands for the swept length(animatedFraction). Seldom f
 * is linear, so there would be distinguishable difference between x and y.<br/>
 * <br/>
 * All update requests are applied to {@link ImageFrame1} for
 * {@link #onDraw(Canvas)} reading.<br/>
 * <br/>
 */
public class ImageDisplay extends View {

    private static final float SCALE_ALLOWANCE = 0.025f;
    private static final float MAX_SCALE = 64f;
    private static final float MIN_SCALE = 0.1f;

    private ImageFrame mFrame;
    private ImageSwitchingAgent mAgent = null;
    private Paint mPaint;

    // to fix the time sequence of data load and view load
    private Runnable mFirstLoadRunnable;

    public ImageDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFrame = new ImageFrame();
        mPaint = new Paint();
        mAgent = new ImageSwitchingAgent(this);
    }

    public Rect copyFrameRect() {
        return new Rect(mFrame.getRect());
    }

    public void doFallback(Bitmap from, Bitmap to, boolean isForward,
            float startPoint, final Runnable onAnimEnd) {
        doSwitch(to, from, !isForward, 1 - startPoint, onAnimEnd);
    }

    public void doScroll(Bitmap from, Bitmap to, boolean isForward,
            float startPoint) {
        mAgent.startScrolling(from, to, isForward, startPoint);
    }

    public void doSwitch(Bitmap from, Bitmap to, boolean isForward,
            float startPoint, float fallbackPoint, final Runnable onAnimEnd) {
        mAgent.startSwitching(from, to, isForward, startPoint, fallbackPoint,
                new Runnable() {

                    @Override
                    public void run() {
                        Util.init(mFrame,
                                mAgent.getDstFrame().getBitmap(),
                                ImageDisplay.this.getWidth(),
                                ImageDisplay.this.getHeight());
                        if (onAnimEnd != null) {
                            onAnimEnd.run();
                        }
                    }
                });
    }

    public void doSwitch(Bitmap from, Bitmap to, boolean isForward,
            float startPoint, final Runnable onAnimEnd) {
        doSwitch(from, to, isForward, startPoint, 2f, onAnimEnd);
    }

    public void firstLoad(final Bitmap bitmap) {
        if (getWidth() > 0 && getHeight() > 0) {
            Util.init(mFrame, bitmap, getWidth(), getHeight());
            invalidate();
        } else {
            mFirstLoadRunnable = new Runnable() {

                @Override
                public void run() {
                    firstLoad(bitmap);
                }
            };
        }
    }

    public boolean isScaled() {
        float scale = mFrame.getScale();
        float fitScale = Util.findBaseScale(mFrame.getBitmap(),
                getWidth(), getHeight());
        return scale < (1f - SCALE_ALLOWANCE) * fitScale
                || scale > (1f + SCALE_ALLOWANCE) * fitScale;
    }

    public boolean isSwitching() {
        return mAgent.isSwitching();
    }

    public void move(int dx, int dy) {
        mFrame.move(dx, dy);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFirstLoadRunnable != null) {
            mFirstLoadRunnable.run();
            mFirstLoadRunnable = null;
            return;
        }

        if (!mAgent.interceptOnDraw(canvas, mPaint)) {
            mFrame.draw(canvas, mPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            restore();
        }
    }

    public void restore() {
        Util.init(mFrame, mFrame.getBitmap(), getWidth(), getHeight());
        invalidate();
    }

    public void scale(float scale) {
        // focus on the center: the special case avoids accumulated error
        setScale(scale * mFrame.getScale());
        Util.moveToCenter(mFrame, getWidth(), getHeight());
        invalidate();
    }

    /**
     * (anchorX, anchorY) is stable on the screen
     */
    public void scale(float scale, int anchorX, int anchorY) {
        // focus on the anchor
        anchorX -= mFrame.getRect().left; // relative to (left, top)
        anchorY -= mFrame.getRect().top;
        float fractionX = 1f * anchorX / mFrame.getRect().width();
        float fractionY = 1f * anchorY / mFrame.getRect().height();
        setScale(scale * mFrame.getScale());
        float offsetX = fractionX * mFrame.getRect().width() - anchorX;
        float offsetY = fractionY * mFrame.getRect().height() - anchorY;
        mFrame.moveTo(mFrame.getRect().left - (int) offsetX,
                mFrame.getRect().top - (int) offsetY);
        invalidate();
    }

    public void scaleToDotForDot() {
        setScale(1f);
        Util.moveToCenter(mFrame, getWidth(), getHeight());
        invalidate();
    }

    private void setScale(float scale) {
        if (scale > MAX_SCALE) {
            scale = MAX_SCALE;
        } else if (scale < MIN_SCALE) {
            scale = MIN_SCALE;
        } else {
            float baseScale = Util.findBaseScale(mFrame.getBitmap(),
                    getWidth(), getHeight());
            if (scale > (1f - SCALE_ALLOWANCE) * baseScale
                    && scale < (1f + SCALE_ALLOWANCE) * baseScale) {
                scale = baseScale;
            }
        }
        mFrame.setScale(scale);
    }
}
