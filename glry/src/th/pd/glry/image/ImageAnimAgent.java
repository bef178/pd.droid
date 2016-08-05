package th.pd.glry.image;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import t.typedef.droid.InvertibleInterpolator;
import t.typedef.droid.SimpleAnimatorListener;

public class ImageAnimAgent {

    private static final float ANIM_MIN_ALPHA = 0.4f;
    private static final float ANIM_MAX_ALPHA = 1.0f;
    private static final float ANIM_MIN_SCALE = 0.4f;
    private static final float ANIM_MAX_SCALE = 1.0f;

    private static final InvertibleInterpolator f = new ExpInterpolator();

    private static ValueAnimator getAnimator(final float fx) {

        final int DURATION = 300;

        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(DURATION);
        a.setInterpolator(f);

        a.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                if (animator instanceof ValueAnimator) {
                    ValueAnimator a = (ValueAnimator) animator;
                    a.setCurrentPlayTime(
                            (int) (f.getInvertedInterpolation(fx) * DURATION));
                }
            }
        });

        return a;
    }

    /**
     * upper:leave
     */
    private static void initAnimMoveFromCenterToLeft(
            ImageFrame1 frame, Bitmap bitmap,
            int hostWidth, int hostHeight) {
        Util.init(frame, bitmap, hostWidth, hostHeight);
        frame.initAnim();
        Rect start = new Rect(frame.getRect());
        Rect end = new Rect(frame.getRect());
        end.left = -end.width();
        frame.setAnimRects(start, end);
    }

    /**
     * upper:enter
     */
    private static void initAnimMoveFromLeftToCenter(
            ImageFrame1 frame, Bitmap bitmap,
            int hostWidth, int hostHeight) {
        Util.init(frame, bitmap, hostWidth, hostHeight);
        frame.initAnim();
        Rect start = new Rect(frame.getRect());
        start.left = -start.width();
        Rect end = new Rect(frame.getRect());
        frame.setAnimRects(start, end);
    }

    /**
     * lower:enter
     */
    private static void initAnimZoomInAndFadeIn(
            ImageFrame1 frame, Bitmap bitmap,
            int hostWidth, int hostHeight) {
        Util.init(frame, bitmap, hostWidth, hostHeight);
        frame.initAnim();
        frame.setAnimAlpha(ANIM_MIN_ALPHA, ANIM_MAX_ALPHA);
        Rect start = new Rect(frame.getRect());
        start.right = (int) (start.width() * ANIM_MIN_SCALE + start.left);
        start.bottom = (int) (start.height() * ANIM_MIN_SCALE + start.top);
        start.offset((frame.getRect().right - start.right) / 2,
                (frame.getRect().bottom - start.bottom) / 2);
        Rect end = frame.getRect();
        end.right = (int) (end.width() * ANIM_MAX_SCALE + end.left);
        end.bottom = (int) (end.height() * ANIM_MAX_SCALE + end.top);
        end.offset((frame.getRect().right - end.right) / 2,
                (frame.getRect().bottom - end.bottom) / 2);
        frame.setAnimRects(start, end);
    }

    /**
     *  lower:leave
     */
    private static void initAnimZoomOutAndFadeOut(
            ImageFrame1 frame, Bitmap bitmap,
            int hostWidth, int hostHeight) {
        Util.init(frame, bitmap, hostWidth, hostHeight);
        frame.initAnim();
        frame.setAnimAlpha(ANIM_MAX_ALPHA, ANIM_MIN_ALPHA);
        Rect start = new Rect(frame.getRect());
        start.right = (int) (start.width() * ANIM_MAX_SCALE + start.left);
        start.bottom = (int) (start.height() * ANIM_MAX_SCALE + start.top);
        start.offset((frame.getRect().right - start.right) / 2,
                (frame.getRect().bottom - start.bottom) / 2);
        Rect end = new Rect(frame.getRect());
        end.right = (int) (end.width() * ANIM_MIN_SCALE + end.left);
        end.bottom = (int) (end.height() * ANIM_MIN_SCALE + end.top);
        end.offset((frame.getRect().right - end.right) / 2,
                (frame.getRect().bottom - end.bottom) / 2);
        frame.setAnimRects(start, end);
    }

    private ImageDisplay mView = null;
    private AnimatorSet mAnimSet = null;

    private ImageFrame1 src = new ImageFrame1();
    private ImageFrame1 dst = new ImageFrame1();
    private boolean isForward = true;

    private boolean isScrolling = false;

    public ImageAnimAgent(ImageDisplay view) {
        assert view != null;
        mView = view;
    }

    public ImageFrame1 getDstFrame() {
        return dst;
    }

    public boolean startScrolling(Bitmap from, Bitmap to, boolean isForward,
            float startPoint) {
        if (isSwitching()) {
            return false;
        }
        initAnim(from, to, isForward);

        isScrolling = startPoint != 0f;
        updateFrames(startPoint);

        mView.invalidate();
        return true;
    }

    public boolean startSwitching(Bitmap from, Bitmap to, boolean isForward,
            float startPoint, final float fallbackPoint,
            final Runnable onAnimEnd) {
        if (isSwitching()) {
            return false;
        }
        initAnim(from, to, isForward);

        mAnimSet = new AnimatorSet();
        mAnimSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator a) {
                if (onAnimEnd != null) {
                    onAnimEnd.run();
                }
                isScrolling = false;
                src.init(null);
                dst.init(null);
                ImageAnimAgent.this.isForward = true;
                mView.invalidate();
            }
        });

        ValueAnimator animator = getAnimator(startPoint);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float y = a.getAnimatedFraction();
                if (y >= fallbackPoint) {
                    a.cancel();
                }
                updateFrames(y);
                mView.invalidate();
            }
        });

        if (fallbackPoint >= 1) {
            mAnimSet.playTogether(animator);
            mAnimSet.start();
            return true;
        }

        ValueAnimator fallbackAnimator = getAnimator(1 - fallbackPoint);
        fallbackAnimator.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                ImageAnimAgent.this.initAnim(dst.getBitmap(),
                        src.getBitmap(),
                        !ImageAnimAgent.this.isForward);
            }
        });
        fallbackAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float y = a.getAnimatedFraction();
                updateFrames(y);
                mView.invalidate();
            }
        });

        mAnimSet.playSequentially(animator, fallbackAnimator);
        mAnimSet.start();
        return true;
    }

    public void initAnim(Bitmap from, Bitmap to, boolean isForward) {
        this.isForward = isForward;
        if (isForward) {
            initAnimMoveFromCenterToLeft(src, from,
                    mView.getWidth(), mView.getHeight());
            initAnimZoomInAndFadeIn(dst, to,
                    mView.getWidth(), mView.getHeight());
        } else {
            initAnimZoomOutAndFadeOut(src, from,
                    mView.getWidth(), mView.getHeight());
            initAnimMoveFromLeftToCenter(dst, to,
                    mView.getWidth(), mView.getHeight());
        }
    }

    public boolean interceptOnDraw(Canvas canvas, Paint paint) {
        if (isSwitching() || isScrolling) {
            if (isForward) {
                dst.draw(canvas, paint);
                src.draw(canvas, paint);
            } else {
                src.draw(canvas, paint);
                dst.draw(canvas, paint);
            }
            return true;
        }
        return false;
    }

    public boolean isSwitching() {
        return mAnimSet != null && mAnimSet.isRunning();
    }

    private void updateFrames(float y) {
        src.onAnimUpdate(y);
        dst.onAnimUpdate(y);
    }
}
