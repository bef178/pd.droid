package th.pd.glry.image;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import th.pd.common.android.SimpleAnimatorListener;

public class ImageSwitchingAgent {

    private static final float MIN_ALPHA = 0.4f;
    private static final float MAX_ALPHA = 1.0f;
    private static final float MIN_SCALE = 0.4f;
    private static final float MAX_SCALE = 1.0f;

    private static final ExpInterpolator f = new ExpInterpolator();

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
                            (int) (f.getInverseInterpolation(fx) * DURATION));
                }
            }
        });

        return a;
    }

    private View mView = null;
    private AnimatorSet mAnimSet = null;

    private ImageFrame1 src = new ImageFrame1();
    private ImageFrame1 dst = new ImageFrame1();
    private boolean isForward = true;
    private boolean isScrolling = false;

    public ImageSwitchingAgent(View view) {
        assert view != null;
        mView = view;
    }

    public ImageFrame1 getDstFrame() {
        return dst;
    }

    public void goScroll(float fx) {
        isScrolling = fx != 0f;
        updateFrames(fx);
    }

    public void goSwitch(float start, final float fallback,
            final Runnable callback) {
        mAnimSet = new AnimatorSet();
        mAnimSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator a) {
                if (callback != null) {
                    callback.run();
                }
                isScrolling = false;
                src.init(null);
                dst.init(null);
                isForward = true;
                mView.invalidate();
            }
        });

        ValueAnimator animator = getAnimator(start);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float fx1 = a.getAnimatedFraction();
                if (fx1 >= fallback) {
                    a.cancel();
                }
                updateFrames(fx1);
                mView.invalidate();
            }
        });

        if (fallback >= 1) {
            mAnimSet.playTogether(animator);
            mAnimSet.start();
            return;
        }

        ValueAnimator fallbackAnimator = getAnimator(1 - fallback);
        fallbackAnimator.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                ImageSwitchingAgent.this.init(dst.getBitmap(),
                        src.getBitmap(),
                        !ImageSwitchingAgent.this.isForward);
            }
        });
        fallbackAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float fx1 = a.getAnimatedFraction();
                updateFrames(fx1);
                mView.invalidate();
            }
        });

        mAnimSet.playSequentially(animator, fallbackAnimator);
        mAnimSet.start();
    }

    public void init(Bitmap from, Bitmap to, boolean isForward) {
        Util.init(src, from, mView.getWidth(), mView.getHeight());
        Util.init(dst, to, mView.getWidth(), mView.getHeight());
        this.isForward = isForward;
        if (isForward) {
            initAnimPullNext(src, dst);
        } else {
            initAnimPullPrev(src, dst);
        }
    }

    private void initAnimPullNext(ImageFrame1 src, ImageFrame1 dst) {
        src.initAnim()
                .setAnimTransRange(
                        mView.getWidth() / 2,
                        mView.getHeight() / 2,
                        mView.getWidth() / 2 - mView.getWidth(),
                        mView.getHeight() / 2);
        dst.initAnim()
                .setAnimAlphaRange(MIN_ALPHA, MAX_ALPHA)
                .setAnimScaleRange(MIN_SCALE, MAX_SCALE);
    }

    private void initAnimPullPrev(ImageFrame1 src, ImageFrame1 dst) {
        dst.initAnim()
                .setAnimTransRange(
                        mView.getWidth() / 2 - mView.getWidth(),
                        mView.getHeight() / 2,
                        mView.getWidth() / 2,
                        mView.getHeight() / 2);
        src.initAnim()
                .setAnimAlphaRange(MAX_ALPHA, MIN_ALPHA)
                .setAnimScaleRange(MAX_SCALE, MIN_SCALE);
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

    private void updateFrames(float fx) {
        src.onAnimUpdate(fx);
        dst.onAnimUpdate(fx);
    }
}
