
package th.common.widget;

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
import android.view.animation.Interpolator;
import android.view.View;

import th.common.R;

/**
 * Pay attention to the terms:<br/>
 * 'next' vs 'coming', 'fraction' vs 'animatedFraction'
 */
public class ImageSwitcher extends View {
    private static class SquareInterpolator implements Interpolator {

        public static float getInterpolated(float x) {
            return x * x;
        }

        public static float getInversed(float y) {
            return (float) (Math.pow(y, 0.5));
        }

        @Override
        public float getInterpolation(float x) {
            return getInterpolated(x);
        }
    }

    private class ImageStatus {

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

    private static final int FLAG_ALPHA = 0x20;
    private static final int FLAG_ENTER = 0x1;
    private static final int FLAG_SCALE = 0x10;
    private static final int FLAG_TRANS = 0x40;
    private static final int FLAG_TRANS_TO_BOTTOM = 0x200;
    private static final int FLAG_TRANS_TO_LEFT = 0x400;
    private static final int FLAG_TRANS_TO_RIGHT = 0x600;
    private static final int FLAG_TRANS_TO_TOP = 0x800;

    private static SquareInterpolator interpolator = new SquareInterpolator();

    private static ValueAnimator getAnimator(float interpolated) {
        final int DURATION = 500;

        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(DURATION);
        a.setInterpolator(interpolator);

        final int playedTime = (int) (SquareInterpolator
                .getInversed(interpolated) * DURATION);
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

    private static void updateImageStatus(ImageStatus imageStatus, int flags,
            float animatedFraction, int hostWidth, int hostHeight) {

        if (!imageStatus.isValid()) {
            return;
        }

        boolean isEnter = (flags & FLAG_ENTER) != 0;

        if ((flags & FLAG_ALPHA) != 0) {
            final float ALPHA_START = 0.4f;
            final float ALPHA_FINAL = 1.0f;
            imageStatus.alpha(isEnter
                    ? (ALPHA_FINAL - ALPHA_START) * animatedFraction + ALPHA_START
                    : (ALPHA_START - ALPHA_FINAL) * animatedFraction + ALPHA_FINAL);
        } else {
            imageStatus.alpha(1.0f);
        }

        if ((flags & FLAG_TRANS) != 0) {
            int offsetX = 0;
            int offsetY = 0;
            switch (flags & 0xF00) {
                case FLAG_TRANS_TO_LEFT: {
                    int wBitmap = imageStatus.bitmap.getWidth();
                    if (isEnter) {
                        offsetX = (int) (wBitmap * (1f - animatedFraction));
                    } else {
                        offsetX = (int) (wBitmap * -animatedFraction);
                    }
                    break;
                }
                case FLAG_TRANS_TO_RIGHT: {
                    int wTotal = imageStatus.bitmap.getWidth();
                    if (isEnter) {
                        offsetX = (int) (wTotal * (animatedFraction - 1f));
                    } else {
                        offsetX = (int) (wTotal * animatedFraction);
                    }
                    break;
                }
                case FLAG_TRANS_TO_TOP:
                    offsetY = (int) (imageStatus.bitmap.getHeight() * animatedFraction);
                    break;
                case FLAG_TRANS_TO_BOTTOM:
                    offsetY = -(int) (imageStatus.bitmap.getHeight() * animatedFraction);
                    break;
            }
            imageStatus.offset(offsetX, offsetY);
        }

        if ((flags & FLAG_SCALE) != 0) {
            final float SCALE_START = 0.4f;
            final float SCALE_FINAL = 1.0f;
            imageStatus.scale(isEnter
                    ? (SCALE_FINAL - SCALE_START) * animatedFraction + SCALE_START
                    : (SCALE_START - SCALE_FINAL) * animatedFraction + SCALE_FINAL);

            int w = imageStatus.rect.width();
            int h = imageStatus.rect.height();
            imageStatus.offset((hostWidth - w) / 2, (hostHeight - h) / 2);
        }
    }

    private AnimatorSet mAnimatorSet = null;

    private ImageStatus mImage;
    private ImageStatus mComingImage;
    private boolean mComingAsNext = true;

    private Paint mPaint;

    public ImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mImage = new ImageStatus();
        mComingImage = new ImageStatus();
        mPaint = new Paint();
    }

    public void doneSwitching() {
        if (isSwitching()) {
            mAnimatorSet.end();
        }
    }

    public void doScroll(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext, float animatedFraction) {
        if (isSwitching()) {
            return;
        }

        mImage.initialize(bitmap);
        mComingImage.initialize(comingBitmap);
        mComingAsNext = asNext;

        updateImageStatus(mComingImage,
                getFlagsForAnim(true, mComingAsNext),
                animatedFraction,
                getWidth(), getHeight());
        updateImageStatus(mImage,
                getFlagsForAnim(false, mComingAsNext),
                animatedFraction,
                getWidth(), getHeight());

        invalidate();
    }

    public void doSwitch(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext,
            float startAnimatedFraction) {
        doSwitch(bitmap, comingBitmap, asNext,
                startAnimatedFraction, 7749f);
    }

    private void doSwitch(Bitmap bitmap, Bitmap comingBitmap,
            boolean asNext, float startAnimatedFraction,
            final float endAnimatedFraction) {

        if (isSwitching()) {
            return;
        }

        mImage.initialize(bitmap);
        mComingImage.initialize(comingBitmap);
        mComingAsNext = asNext;

        ValueAnimator animator = getAnimator(startAnimatedFraction);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                if (animatedFraction >= endAnimatedFraction) {
                    mAnimatorSet.cancel();
                }
                updateImageStatus(mComingImage,
                        getFlagsForAnim(true, mComingAsNext),
                        animatedFraction,
                        getWidth(), getHeight());
                updateImageStatus(mImage,
                        getFlagsForAnim(false, mComingAsNext),
                        animatedFraction,
                        getWidth(), getHeight());
                invalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mImage.initialize(mComingImage.bitmap);
                mComingImage.clear();
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

        mImage.initialize(bitmap);
        mComingImage.initialize(comingBitmap);
        mComingAsNext = asNext;

        ValueAnimator animator = getAnimator(0f);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                if (animatedFraction >= turnPointAnimatedFraction) {
                    valueAnimator.cancel();
                }
                updateImageStatus(mComingImage,
                        getFlagsForAnim(true, mComingAsNext),
                        animatedFraction,
                        getWidth(), getHeight());
                updateImageStatus(mImage,
                        getFlagsForAnim(false, mComingAsNext),
                        animatedFraction,
                        getWidth(), getHeight());
                invalidate();
            }
        });

        ValueAnimator fallbackAnimator = getAnimator(1 - turnPointAnimatedFraction);
        fallbackAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                updateImageStatus(mComingImage,
                        getFlagsForAnim(false, !mComingAsNext),
                        animatedFraction,
                        getWidth(), getHeight());
                updateImageStatus(mImage,
                        getFlagsForAnim(true, !mComingAsNext),
                        animatedFraction,
                        getWidth(), getHeight());
                invalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animator, fallbackAnimator);
        mAnimatorSet.start();
    }

    public boolean isSwitching() {
        return mAnimatorSet != null && mAnimatorSet.isRunning();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.dev_gray9));
        if (mComingAsNext) {
            onDrawImage(canvas, mPaint, mComingImage);
            onDrawImage(canvas, mPaint, mImage);
        } else {
            onDrawImage(canvas, mPaint, mImage);
            onDrawImage(canvas, mPaint, mComingImage);
        }
        super.onDraw(canvas);
    }

    private void onDrawImage(Canvas canvas, Paint paint, ImageStatus image) {
        if (image.isValid()) {
            paint.setAlpha(image.alpha);
            canvas.drawBitmap(image.bitmap, null, image.rect, paint);
        }
    }

    public void reset() {
        mImage.restore();
        mComingImage.clear();
        mComingAsNext = true;
        invalidate();
    }
}
