package th.pd.common.android.pageheader;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import th.pd.common.android.R;

public class PageheaderController {

    public interface Callback {

        public void onActionBack();
    }

    private static final int DEFAULT_DISPLAY_TIMEOUT = 2000;

    private static final int MSG_HIDE_WITH_ANIM = 7749;

    // same value as that in ActionBarImpl.
    private static final int ANIM_DURATION = 250;

    // a little faster to catch up with the system UI
    private static final int ANIM_SHOW_DURATION = 250;

    private Context mContext;

    private View mHeader;
    private ImageView mHeaderLogo;
    private TextView mHeaderTitle;
    private TextView mHeaderSummary;
    private ViewGroup mHeaderOptionContainer;

    // for the final state of the last animation
    private boolean mFinallyVisible = false;

    private Animator mOnGoingAnimation;

    private Callback mCallback;

    private Handler mHandler;

    private View.OnClickListener mOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onActionBack();
                    }
                }
            };

    private final AnimatorListener mAnimHideListener =
            new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mOnGoingAnimation = null;
                    mFinallyVisible = false;
                    if (mHeader != null) {
                        mHeader.setTranslationY(0);
                        mHeader.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    if (mOnGoingAnimation != null) {
                        mOnGoingAnimation.end();
                    }
                    mOnGoingAnimation = animation;
                    mFinallyVisible = false;
                };
            };

    private final AnimatorListener mAnimShowListener =
            new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mOnGoingAnimation = null;
                    mFinallyVisible = true;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    if (mOnGoingAnimation != null) {
                        mOnGoingAnimation.end();
                    }
                    mOnGoingAnimation = animation;
                    mFinallyVisible = true;
                };
            };

    private final ValueAnimator.AnimatorUpdateListener mAnimUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mHeader.requestLayout();
                }
            };

    public PageheaderController(View headerView, Callback callback) {
        mContext = headerView.getContext();
        findViews(headerView);
        setCallback(callback);

        mHandler = new Handler(mContext.getMainLooper()) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIDE_WITH_ANIM: {
                        hideWithAnim();
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };

        hideImmediately();
    }

    private void findViews(View headerView) {
        mHeader = headerView;
        mHeader.findViewById(R.id.actionBack)
                .setOnClickListener(mOnClickListener);
        mHeaderOptionContainer = (ViewGroup) headerView
                .findViewById(R.id.pageheader_option_container);
        mHeaderLogo = (ImageView) headerView
                .findViewById(android.R.id.icon);
        mHeaderTitle = (TextView) headerView
                .findViewById(android.R.id.title);
        mHeaderSummary = (TextView) headerView
                .findViewById(android.R.id.summary);
    }

    public ViewGroup getOptionContainer() {
        return mHeaderOptionContainer;
    }

    public View getView() {
        return mHeader;
    }

    public void hideImmediately() {
        if (mHeader == null) {
            return;
        }

        if (mOnGoingAnimation != null) {
            mOnGoingAnimation.end();
        }

        if (isFinallyVisible()) {
            mHeader.setVisibility(View.GONE);
            mFinallyVisible = false;
        }
    }

    public void hideWithAnim() {
        hideWithAnim(ANIM_DURATION);
    }

    /**
     * remove any pending posts of hide message and do hide
     */
    public void hideWithAnim(int animDuration) {
        if (mHeader == null) {
            return;
        }

        if (animDuration <= 0) {
            hideImmediately();
            return;
        }

        if (!isFinallyVisible()) {
            return;
        }

        float endingY = -mHeader.getHeight();
        int topLeft[] = { 0, 0 };
        mHeader.getLocationInWindow(topLeft);
        endingY -= topLeft[1];

        ObjectAnimator a = ObjectAnimator.ofFloat(
                mHeader, View.TRANSLATION_Y, endingY);
        a.addUpdateListener(mAnimUpdateListener);

        a.setInterpolator(new AccelerateInterpolator(1.5f));
        a.setDuration(animDuration);
        a.addListener(mAnimHideListener);
        a.start();
    }

    public void hideWithDelay() {
        hideWithDelay(DEFAULT_DISPLAY_TIMEOUT);
    }

    /**
     * refresh timeout of hide message
     */
    public void hideWithDelay(int delay) {
        mHandler.removeMessages(MSG_HIDE_WITH_ANIM);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_WITH_ANIM, delay);
    }

    /**
     * @return the final state of header
     */
    public boolean isFinallyVisible() {
        return mHeader != null && mFinallyVisible;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setLogo(Drawable logo) {
        mHeaderLogo.setImageDrawable(logo);
        if (logo != null) {
            mHeaderLogo.setVisibility(View.VISIBLE);
        } else {
            mHeaderLogo.setVisibility(View.GONE);
        }
    }

    public void setSummary(CharSequence text) {
        mHeaderSummary.setText(text);
    }

    public void setTitle(CharSequence text) {
        mHeaderTitle.setText(text);
    }

    public void showImmediately() {
        if (mHeader == null) {
            return;
        }

        if (mOnGoingAnimation != null) {
            mOnGoingAnimation.end();
        }

        if (!isFinallyVisible()) {
            mHeader.setVisibility(View.VISIBLE);
            mFinallyVisible = true;
        }
    }

    public void showWithAnim() {
        showWithAnim(ANIM_SHOW_DURATION);
    }

    /**
     * dummy if show animation wip;<br/>
     * end hide animation if hide animation wip to restore stable state<br/>
     */
    private void showWithAnim(int animDuration) {
        if (mHeader == null) {
            return;
        }

        if (animDuration <= 0) {
            showImmediately();
            return;
        }

        if (isFinallyVisible()) {
            return;
        }

        mHeader.setVisibility(View.VISIBLE);

        float startingY = -mHeader.getHeight();
        int[] topLeft = { 0, 0 };
        mHeader.getLocationInWindow(topLeft);
        startingY -= topLeft[1];
        mHeader.setTranslationY(startingY);

        ObjectAnimator a = ObjectAnimator.ofFloat(
                mHeader, View.TRANSLATION_Y, 0);
        a.addUpdateListener(mAnimUpdateListener);

        a.setInterpolator(new DecelerateInterpolator(1.5f));
        a.setDuration(animDuration);
        a.addListener(mAnimShowListener);
        a.start();
    }
}
