package th.pd.glry.elementary;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import th.pd.common.android.SimpleAnimatorListener;

/**
 * Note the terms.<br/>
 * It always animates from "src" to the "dst", which is in chronological.
 * While, the "dst" may be the "next" or the "prev", i.e. the neighborhood in
 * location.<br/>
 * <br/>
 * There's a mapping for time to location during switching, y = f(x), where x
 * stands for the elapsed time(fraction), y stands for the swept
 * length(animatedFraction) and f is seldom linear. So there
 * would be a distinguishable difference between x and y.<br/>
 * <br/>
 * All update requests are applied to {@link Frame} for {@link #onDraw(Canvas)}
 * reading.<br/>
 * <br/>
 */
public class FramedView extends View {

    private static final int FLAG_ENTER = 0x1;
    private static final int FLAG_SCALE = 0x10;
    private static final int FLAG_ALPHA = 0x20;
    private static final int FLAG_TRANS = 0x40;
    private static final int FLAG_TRANS_TO_BOTTOM = 0x200;
    private static final int FLAG_TRANS_TO_LEFT = 0x400;
    private static final int FLAG_TRANS_TO_RIGHT = 0x600;
    private static final int FLAG_TRANS_TO_TOP = 0x800;

    private static ExpInterpolator interpolator = new ExpInterpolator();

    private static ValueAnimator getAnimator(float interpolated) {

        final int DURATION = 300;

        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(DURATION);
        a.setInterpolator(interpolator);

        final int playedTime = (int) (interpolator
                .getInverseInterpolation(interpolated) * DURATION);

        a.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                if (animator instanceof ValueAnimator) {
                    ValueAnimator a = (ValueAnimator) animator;
                    a.setCurrentPlayTime(playedTime);
                }
            }
        });

        return a;
    }

    private static int getFlagsForAnim(boolean isEnter, boolean asNext) {
        if (isEnter) {
            return asNext
                    ? FLAG_ENTER | FLAG_SCALE | FLAG_ALPHA
                    : FLAG_ENTER | FLAG_TRANS | FLAG_TRANS_TO_RIGHT;
        } else {
            return asNext
                    ? FLAG_TRANS | FLAG_TRANS_TO_LEFT
                    : FLAG_SCALE | FLAG_ALPHA;
        }
    }

    private static void updateFrame(Frame frame, int flags,
            float animatedFraction, int hostWidth, int hostHeight) {
        if (!frame.isValid() || hostWidth == 0 || hostHeight == 0) {
            return;
        }

        frame.updateFitScale(hostWidth, hostHeight);

        boolean isEnter = (flags & FLAG_ENTER) != 0;

        if ((flags & FLAG_ALPHA) != 0) {
            float alphaStart = 0.4f;
            float alphaFinal = 1.0f;
            frame.setAlpha(isEnter
                    ? (alphaFinal - alphaStart) * animatedFraction
                            + alphaStart
                    : (alphaStart - alphaFinal) * animatedFraction
                            + alphaFinal);
        } else {
            frame.setAlpha(1.0f);
        }

        if ((flags & FLAG_TRANS) != 0) {
            int offsetX = 0;
            int offsetY = 0;
            switch (flags & 0xF00) {
                case FLAG_TRANS_TO_LEFT: {
                    int totalX = (frame.rect.width() + hostWidth) / 2;
                    offsetY = frame.rect.top;
                    if (isEnter) {
                        // not been here yet, so no test
                    } else {
                        int endX = -frame.rect.width();
                        offsetX = (int) (totalX * (1f - animatedFraction))
                                + endX;
                    }
                    break;
                }
                case FLAG_TRANS_TO_RIGHT: {
                    int totalX = (frame.rect.width() + hostWidth) / 2;
                    offsetY = frame.rect.top;
                    if (isEnter) {
                        int startX = -frame.rect.width();
                        offsetX = (int) (totalX * animatedFraction)
                                + startX;
                    } else {
                        // not been here yet, so no test
                    }
                    break;
                }
                case FLAG_TRANS_TO_TOP:
                    // not been here yet, so no test
                    break;
                case FLAG_TRANS_TO_BOTTOM:
                    // not been here yet, so no test
                    break;
            }
            frame.moveTo(offsetX, offsetY);
        }

        if ((flags & FLAG_SCALE) != 0) {
            float scaleStart = 0.4f * frame.getFitScale();
            float scaleFinal = 1.0f * frame.getFitScale();
            frame.applyScale(isEnter
                    ? (scaleFinal - scaleStart) * animatedFraction
                            + scaleStart
                    : (scaleStart - scaleFinal) * animatedFraction
                            + scaleFinal);
            frame.moveToCenter(hostWidth, hostHeight);
        }
    }

    private AnimatorSet mAnimatorSet = null;

    private Frame mFrame;
    private Frame mComingFrame;

    // mainly for onDraw() to keep consistent animation
    private boolean mComingAsNext = true;

    // as a trigger to tell which mode we are in
    private float mScale = 1f;

    private Paint mPaint;

    // to fix the time sequence of data load and view load
    private Runnable mFirstLoadRunnable;

    public FramedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFrame = new Frame();
        mComingFrame = new Frame();
        mPaint = new Paint();
    }

    private void applyScale(float scale) {
        if (!mFrame.isValid()) {
            return;
        }

        float maxScale = 64f;
        float minScale = Math.min(
                1f * getWidth() / mFrame.bitmap.getWidth(),
                1f * getHeight() / mFrame.bitmap.getHeight());
        minScale = Math.min(0.1f, minScale);

        if (scale > maxScale) {
            scale = maxScale;
        } else if (scale < minScale) {
            scale = minScale;
        }

        if (scale >= 0.98f * mFrame.getFitScale()
                && scale <= 1.02f * mFrame.getFitScale()) {
            scale = mFrame.getFitScale();
        }

        mFrame.applyScale(scale);
        mScale = scale;
    }

    public void doneSwitching() {
        if (isSwitching()) {
            mAnimatorSet.end();
        }
    }

    public void doOffset(int dx, int dy) {
        mFrame.move(dx, dy);
        invalidate();
    }

    public void doScale(float scale) {
        // focus on the center: the sepcial case avoids accumulated error
        applyScale(scale * mScale);
        mFrame.moveToCenter(getWidth(), getHeight());
        invalidate();
    }

    /**
     * (anchorX, anchorY) is stable on the screen
     */
    public void doScale(float scale, int anchorX, int anchorY) {
        // focus on the anchor
        anchorX -= mFrame.rect.left; // relative to (left, top)
        anchorY -= mFrame.rect.top;
        float fractionX = 1f * anchorX / mFrame.rect.width();
        float fractionY = 1f * anchorY / mFrame.rect.height();
        applyScale(scale * mScale);
        float offsetX = fractionX * mFrame.rect.width() - anchorX;
        float offsetY = fractionY * mFrame.rect.height() - anchorY;
        mFrame.moveTo(mFrame.rect.left - (int) offsetX,
                mFrame.rect.top - (int) offsetY);
        invalidate();
    }

    public void doScroll(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext, float animatedFraction) {
        if (isSwitching()) {
            return;
        }

        int hostWidth = getWidth();
        int hostHeight = getHeight();
        mFrame.resetAndFit(bitmap, hostWidth, hostHeight);
        mComingFrame.resetAndFit(comingBitmap, hostWidth, hostHeight);
        mComingAsNext = asNext;

        updateFrame(mComingFrame,
                getFlagsForAnim(true, mComingAsNext),
                animatedFraction,
                hostWidth, hostHeight);
        updateFrame(mFrame,
                getFlagsForAnim(false, mComingAsNext),
                animatedFraction,
                hostWidth, hostHeight);

        invalidate();
    }

    public void doSwitch(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext,
            float startAnimatedFraction) {
        doSwitch(bitmap, comingBitmap, asNext,
                startAnimatedFraction, 7749f);
    }

    private void doSwitch(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext,
            float startAnimatedFraction, final float endAnimatedFraction) {

        if (isSwitching()) {
            return;
        }

        final int hostWidth = getWidth();
        final int hostHeight = getHeight();
        mFrame.resetAndFit(bitmap, hostWidth, hostHeight);
        mComingFrame.resetAndFit(comingBitmap, hostWidth, hostHeight);
        mComingAsNext = asNext;

        ValueAnimator animator = getAnimator(startAnimatedFraction);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator
                        .getAnimatedFraction();
                if (animatedFraction >= endAnimatedFraction) {
                    mAnimatorSet.cancel();
                }
                updateFrame(mComingFrame,
                        getFlagsForAnim(true, mComingAsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                updateFrame(mFrame,
                        getFlagsForAnim(false, mComingAsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                invalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animator) {
                mFrame.resetAndFit(mComingFrame.bitmap, hostWidth,
                        hostHeight);
                mComingFrame.init(null);
                mScale = mFrame.getFitScale();
                invalidate();
            }
        });

        mAnimatorSet.playTogether(animator);
        mAnimatorSet.start();
    }

    public void doSwitchAndFallback(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext, final float turnPointAnimatedFraction) {
        if (isSwitching()) {
            return;
        }

        final int hostWidth = getWidth();
        final int hostHeight = getHeight();
        mFrame.resetAndFit(bitmap, hostWidth, hostHeight);
        mComingFrame.resetAndFit(comingBitmap, hostWidth, hostHeight);
        mComingAsNext = asNext;

        ValueAnimator animator = getAnimator(0f);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator
                        .getAnimatedFraction();
                if (animatedFraction >= turnPointAnimatedFraction) {
                    valueAnimator.cancel();
                }
                updateFrame(mComingFrame,
                        getFlagsForAnim(true, mComingAsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                updateFrame(mFrame,
                        getFlagsForAnim(false, mComingAsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                invalidate();
            }
        });

        ValueAnimator fallbackAnimator = getAnimator(1 - turnPointAnimatedFraction);
        fallbackAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator
                        .getAnimatedFraction();
                updateFrame(mFrame,
                        getFlagsForAnim(true, !mComingAsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                updateFrame(mComingFrame,
                        getFlagsForAnim(false, !mComingAsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                invalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animator, fallbackAnimator);
        mAnimatorSet.start();
    }

    public void firstLoad(final Bitmap bitmap) {
        if (getWidth() == 0 || getHeight() == 0) {
            mFirstLoadRunnable = new Runnable() {

                @Override
                public void run() {
                    firstLoad(bitmap);
                }
            };
        } else {
            mFrame.resetAndFit(bitmap, getWidth(), getHeight());
            mScale = mFrame.getFitScale();
            invalidate();
        }
    }

    public Rect getFrameRect() {
        return new Rect(mFrame.rect);
    }

    public boolean isScaled() {
        return mScale != mFrame.getFitScale();
    }

    public boolean isSwitching() {
        return mAnimatorSet != null && mAnimatorSet.isRunning();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFirstLoadRunnable != null) {
            mFirstLoadRunnable.run();
            mFirstLoadRunnable = null;
            return;
        }
        if (mComingAsNext) {
            drawFrame(canvas, mPaint, mComingFrame);
            drawFrame(canvas, mPaint, mFrame);
        } else {
            drawFrame(canvas, mPaint, mFrame);
            drawFrame(canvas, mPaint, mComingFrame);
        }
        super.onDraw(canvas);
    }

    private void drawFrame(Canvas canvas, Paint paint, Frame frame) {
        if (frame.isValid()) {
            paint.setAlpha(frame.getAlpha());
            canvas.drawBitmap(frame.bitmap, null, frame.rect, paint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            resetImage();
        }
    }

    public void resetImage() {
        mFrame.resetAndFit(mFrame.bitmap, getWidth(), getHeight());
        mScale = mFrame.getFitScale();
        invalidate();
    }
}
