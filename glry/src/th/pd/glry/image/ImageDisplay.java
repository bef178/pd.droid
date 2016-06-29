package th.pd.glry.image;

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
 * As MVP but the presenter is flatten into the view.<br/>
 * <br/>
 * Also note the terms.<br/>
 * It always animates from "src" to the "dst", which is in chronological.
 * While, the "dst" may be the "next" or the "prev", i.e. the neighborhood in
 * location.<br/>
 * <br/>
 * There's a mapping for time to location during switching, y = f(x), where x
 * stands for the elapsed time(fraction), y stands for the swept
 * length(animatedFraction) and f is seldom linear. So there
 * would be a distinguishable difference between x and y.<br/>
 * <br/>
 * All update requests are applied to {@link ImageFrame} for {@link #onDraw(Canvas)}
 * reading.<br/>
 * <br/>
 */
public class ImageDisplay extends View {

    private static final int FLAG_ENTER = 0x1;
    private static final int FLAG_SCALE = 0x10;
    private static final int FLAG_ALPHA = 0x20;
    private static final int FLAG_TRANS = 0x40;
    private static final int FLAG_TRANS_TO_BOTTOM = 0x200;
    private static final int FLAG_TRANS_TO_LEFT = 0x400;
    private static final int FLAG_TRANS_TO_RIGHT = 0x600;
    private static final int FLAG_TRANS_TO_TOP = 0x800;

    private static final float SCALE_ALLOWANCE = 0.025f;

    private static ExpInterpolator interpolator = new ExpInterpolator();

    /**
     * Principle: the container contains the scaled image
     */
    private static float findFitScale(Bitmap bitmap,
            int hostWidth, int hostHeight) {
        if (bitmap == null || hostWidth <= 0 || hostHeight <= 0) {
            return 0f;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w > hostWidth || h > hostHeight) {
            return Math.min(1f * hostWidth / w, 1f * hostHeight / h);
        }
        return 1f;
    }

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

    private static int getFlagsForAnim(boolean isEnter, boolean isLower) {
        if (isEnter) {
            return isLower
                    ? FLAG_ENTER | FLAG_SCALE | FLAG_ALPHA
                    : FLAG_ENTER | FLAG_TRANS | FLAG_TRANS_TO_RIGHT;
        } else {
            return isLower
                    ? FLAG_TRANS | FLAG_TRANS_TO_LEFT
                    : FLAG_SCALE | FLAG_ALPHA;
        }
    }

    private static void updateFrame(ImageFrame frame, int flags,
            float animatedFraction, int hostWidth, int hostHeight) {
        if (!frame.isValid() || hostWidth == 0 || hostHeight == 0) {
            return;
        }

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
                    int totalX = frame.rect.right;
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
                    int totalX = hostWidth - frame.rect.left;
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
            float fitScale = findFitScale(frame.bitmap,
                    hostWidth, hostHeight);
            float scaleStart = 0.4f * fitScale;
            float scaleFinal = 1.0f * fitScale;
            frame.applyScale(isEnter
                    ? (scaleFinal - scaleStart) * animatedFraction
                            + scaleStart
                    : (scaleStart - scaleFinal) * animatedFraction
                            + scaleFinal);
            frame.moveToCenter(hostWidth, hostHeight);
        }
    }

    private AnimatorSet mAnimatorSet = null;

    private ImageFrame mSrcFrame;
    private ImageFrame mDstFrame;

    // mainly for onDraw() to keep consistent animation
    private boolean mDstIsNext = true;

    private Paint mPaint;

    // to fix the time sequence of data load and view load
    private Runnable mFirstLoadRunnable;

    public ImageDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSrcFrame = new ImageFrame();
        mDstFrame = new ImageFrame();
        mPaint = new Paint();
    }

    private void applyScale(float scale) {
        if (!mSrcFrame.isValid()) {
            return;
        }

        final float MAX_SCALE = 64f;
        final float MIN_SCALE = 0.1f;

        if (scale > MAX_SCALE) {
            scale = MAX_SCALE;
        } else if (scale < MIN_SCALE) {
            scale = MIN_SCALE;
        } else {
            float fitScale = findFitScale(mSrcFrame.bitmap,
                    getWidth(), getHeight());
            if (scale > (1f - SCALE_ALLOWANCE) * fitScale
                    && scale < (1f + SCALE_ALLOWANCE) * fitScale) {
                scale = fitScale;
            }
        }

        mSrcFrame.applyScale(scale);
    }

    public void doneSwitching() {
        if (isSwitching()) {
            mAnimatorSet.end();
        }
    }

    public void doOffset(int dx, int dy) {
        mSrcFrame.move(dx, dy);
        invalidate();
    }

    public void doScale(float scale) {
        // focus on the center: the special case avoids accumulated error
        applyScale(scale * mSrcFrame.findScale());
        mSrcFrame.moveToCenter(getWidth(), getHeight());
        invalidate();
    }

    /**
     * (anchorX, anchorY) is stable on the screen
     */
    public void doScale(float scale, int anchorX, int anchorY) {
        // focus on the anchor
        anchorX -= mSrcFrame.rect.left; // relative to (left, top)
        anchorY -= mSrcFrame.rect.top;
        float fractionX = 1f * anchorX / mSrcFrame.rect.width();
        float fractionY = 1f * anchorY / mSrcFrame.rect.height();
        applyScale(scale * mSrcFrame.findScale());
        float offsetX = fractionX * mSrcFrame.rect.width() - anchorX;
        float offsetY = fractionY * mSrcFrame.rect.height() - anchorY;
        mSrcFrame.moveTo(mSrcFrame.rect.left - (int) offsetX,
                mSrcFrame.rect.top - (int) offsetY);
        invalidate();
    }

    public void doScroll(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext, float animatedFraction) {
        if (isSwitching()) {
            return;
        }

        int hostWidth = getWidth();
        int hostHeight = getHeight();
        initAndFit(mSrcFrame, bitmap, hostWidth, hostHeight);
        initAndFit(mDstFrame, comingBitmap, hostWidth, hostHeight);
        mDstIsNext = asNext;

        updateFrame(mDstFrame,
                getFlagsForAnim(true, mDstIsNext),
                animatedFraction,
                hostWidth, hostHeight);
        updateFrame(mSrcFrame,
                getFlagsForAnim(false, mDstIsNext),
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
        initAndFit(mSrcFrame, bitmap, hostWidth, hostHeight);
        initAndFit(mDstFrame, comingBitmap, hostWidth, hostHeight);
        mDstIsNext = asNext;

        ValueAnimator animator = getAnimator(startAnimatedFraction);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator
                        .getAnimatedFraction();
                if (animatedFraction >= endAnimatedFraction) {
                    mAnimatorSet.cancel();
                }
                updateFrame(mDstFrame,
                        getFlagsForAnim(true, mDstIsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                updateFrame(mSrcFrame,
                        getFlagsForAnim(false, mDstIsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                invalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animator) {
                initAndFit(mSrcFrame, mDstFrame.bitmap,
                        hostWidth, hostHeight);
                mDstFrame.init(null);
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
        initAndFit(mSrcFrame, bitmap, hostWidth, hostHeight);
        initAndFit(mDstFrame, comingBitmap, hostWidth, hostHeight);
        mDstIsNext = asNext;

        ValueAnimator animator = getAnimator(0f);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator
                        .getAnimatedFraction();
                if (animatedFraction >= turnPointAnimatedFraction) {
                    valueAnimator.cancel();
                }
                updateFrame(mDstFrame,
                        getFlagsForAnim(true, mDstIsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                updateFrame(mSrcFrame,
                        getFlagsForAnim(false, mDstIsNext),
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
                updateFrame(mSrcFrame,
                        getFlagsForAnim(true, !mDstIsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                updateFrame(mDstFrame,
                        getFlagsForAnim(false, !mDstIsNext),
                        animatedFraction,
                        hostWidth, hostHeight);
                invalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animator, fallbackAnimator);
        mAnimatorSet.start();
    }

    private void drawFrame(Canvas canvas, Paint paint, ImageFrame frame) {
        if (frame.isValid()) {
            paint.setAlpha(frame.getAlpha());
            canvas.drawBitmap(frame.bitmap, null, frame.rect, paint);
        }
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
            initAndFit(mSrcFrame, bitmap, getWidth(), getHeight());
            invalidate();
        }
    }

    public Rect getFrameRect() {
        return new Rect(mSrcFrame.rect);
    }

    public void initAndFit(ImageFrame frame, Bitmap bitmap,
            int hostWidth, int hostHeight) {
        frame.init(bitmap);
        if (bitmap == null || hostWidth <= 0 || hostHeight <= 0) {
            return;
        }
        float fitScale = findFitScale(frame.bitmap, hostWidth, hostHeight);
        frame.applyScale(fitScale);
        frame.moveToCenter(hostWidth, hostHeight);
    }

    public boolean isScaled() {
        float scale = mSrcFrame.findScale();
        float fitScale = findFitScale(mSrcFrame.bitmap,
                getWidth(), getHeight());
        return mDstFrame == null && (scale < (1f - SCALE_ALLOWANCE) * fitScale
                || scale > (1f + SCALE_ALLOWANCE) * fitScale);
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
        if (mDstIsNext) {
            drawFrame(canvas, mPaint, mDstFrame);
            drawFrame(canvas, mPaint, mSrcFrame);
        } else {
            drawFrame(canvas, mPaint, mSrcFrame);
            drawFrame(canvas, mPaint, mDstFrame);
        }
        super.onDraw(canvas);
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
        initAndFit(mSrcFrame, mSrcFrame.bitmap, getWidth(), getHeight());
        invalidate();
    }
}
