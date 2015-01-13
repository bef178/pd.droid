package th.mediaPlay;

import th.pd.R;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

public class ImageSwitcher extends View {

    static class ImageAttribute {
        Bitmap bitmap;
        int alpha;
        Rect rect;
        int dx;
        int dy;

        public ImageAttribute() {
            this.rect = new Rect();
            reset(null);
        }

        private void alpha(float alpha) {
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

        public void reset(Bitmap bitmap) {
            this.bitmap = bitmap;
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

        public void scale(float scale) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            rect.right = rect.left + (int) (scale * w);
            rect.bottom = rect.top + (int) (scale * h);
        }
    }

    static ValueAnimator getAnimator(Context context, int flags) {
        final int DURATION = 2500;

        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(DURATION);
        a.setInterpolator((flags & FLAG_ENTER) != 0
                ? new AccelerateDecelerateInterpolator()
                : new AccelerateInterpolator(0.5f));
        return a;
    }

    private static final int FLAG_ENTER = 0x1;
    private static final int FLAG_SCALE = 0x10;
    private static final int FLAG_ALPHA = 0x20;
    private static final int FLAG_TRANS = 0x40;

    static final int FLAG_TRANS_TO_LEFT = 0x400;
    static final int FLAG_TRANS_TO_RIGHT = 0x600;
    static final int FLAG_TRANS_TO_TOP = 0x800;
    static final int FLAG_TRANS_TO_BOTTOM = 0x200;

    public static final int ANIM_ENTER_SCALE_FROM_CENTER =
            FLAG_ENTER | FLAG_SCALE | FLAG_ALPHA;
    public static final int ANIM_LEAVE_SCALE_TO_CENTER =
            FLAG_SCALE | FLAG_ALPHA;

    public static final int ANIM_ENTER_TRANS_FROM_LEFT =
            FLAG_ENTER | FLAG_TRANS | FLAG_TRANS_TO_RIGHT;
    public static final int ANIM_LEAVE_TRANS_TO_LEFT =
            FLAG_TRANS | FLAG_TRANS_TO_LEFT;

    private AnimatorSet mAnimatorSet = null;

    private ImageAttribute mThisImage;
    private ImageAttribute mNextImage;

    private Paint mPaint;

    private boolean mIsForward = true;

    public ImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mThisImage = new ImageAttribute();
        mNextImage = new ImageAttribute();
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.dev_gray9));
        if (mIsForward) {
            onDrawImage(canvas, mPaint, mNextImage);
            onDrawImage(canvas, mPaint, mThisImage);
        } else {
            onDrawImage(canvas, mPaint, mThisImage);
            onDrawImage(canvas, mPaint, mNextImage);
        }
        super.onDraw(canvas);
    }

    private void onDrawImage(Canvas canvas, Paint paint, ImageAttribute attr) {
        if (attr.isValid()) {
            paint.setAlpha(attr.alpha);
            canvas.drawBitmap(attr.bitmap, null, attr.rect, paint);
        }
    }

    public void switchTo(Bitmap nextBitmap, boolean isForward) {
        this.mIsForward = isForward;
        final int flagsForEnter = isForward
                ? ANIM_ENTER_SCALE_FROM_CENTER
                : ANIM_ENTER_TRANS_FROM_LEFT;
        final int flagsForLeave = isForward
                ? ANIM_LEAVE_TRANS_TO_LEFT
                : ANIM_LEAVE_SCALE_TO_CENTER;

        ValueAnimator enterAnimator = getAnimator(getContext(), flagsForEnter);
        enterAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                updateImageAttribute(mNextImage, flagsForEnter, fraction);
                invalidate();
            }
        });

        ValueAnimator leaveAnimator = getAnimator(getContext(), flagsForLeave);
        leaveAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                updateImageAttribute(mThisImage, flagsForLeave, fraction);
                // we call invalidate() in enterAnimator
            }
        });

        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mThisImage.reset(mNextImage.bitmap);
                mNextImage.reset(null);
                invalidate();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }
        });

        mNextImage.reset(nextBitmap);

        mAnimatorSet.playTogether(enterAnimator, leaveAnimator);
        mAnimatorSet.start();
    }

    private void updateImageAttribute(ImageAttribute imageAttr, int flags,
            float fraction) {

        if (!imageAttr.isValid()) {
            return;
        }

        boolean isEnter = (flags & FLAG_ENTER) != 0;

        if ((flags & FLAG_ALPHA) != 0) {
            final float ALPHA_START = 0.0f;
            final float ALPHA_FINAL = 1.0f;
            imageAttr.alpha(isEnter
                    ? (ALPHA_FINAL - ALPHA_START) * fraction + ALPHA_START
                    : (ALPHA_START - ALPHA_FINAL) * fraction + ALPHA_FINAL);
        } else {
            imageAttr.alpha(1.0f);
        }

        if ((flags & FLAG_TRANS) != 0) {
            int offsetX = 0;
            int offsetY = 0;
            switch (flags & 0xF00) {
                case FLAG_TRANS_TO_LEFT: {
                    int wTotal = imageAttr.bitmap.getWidth();
                    if (isEnter) {
                        offsetX = (int) (wTotal * (1 - fraction));
                    } else {
                        offsetX = (int) (wTotal * -fraction);
                    }
                    break;
                }
                case FLAG_TRANS_TO_RIGHT: {
                    int wTotal = imageAttr.bitmap.getWidth();
                    if (isEnter) {
                        offsetX = (int) (wTotal * (fraction - 1));
                    } else {
                        offsetX = (int) (wTotal * fraction);
                    }
                    break;
                }
                case FLAG_TRANS_TO_TOP:
                    offsetY = (int) (imageAttr.bitmap.getHeight() * fraction);
                    break;
                case FLAG_TRANS_TO_BOTTOM:
                    offsetY = -(int) (imageAttr.bitmap.getHeight() * fraction);
                    break;
            }
            imageAttr.offset(offsetX, offsetY);
        }

        if ((flags & FLAG_SCALE) != 0) {
            final float SCALE_START = 0.4f;
            final float SCALE_FINAL = 1.0f;
            imageAttr.scale(isEnter
                    ? (SCALE_FINAL - SCALE_START) * fraction + SCALE_START
                    : (SCALE_START - SCALE_FINAL) * fraction + SCALE_FINAL);

            int wTotal = getWidth();
            int hTotal = getHeight();
            int w = imageAttr.rect.width();
            int h = imageAttr.rect.height();
            imageAttr.offset((wTotal - w) / 2, (hTotal - h) / 2);
        }
    }
}
