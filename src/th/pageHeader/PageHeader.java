package th.pageHeader;

import th.pd.R;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class PageHeader {

	/**
	 * it has animations so gives a chance to notify
	 */
	public static abstract class VisibilityListener {

		public void onHideStart() {
			// dummy by default
		}
	}

	private static final int MSG_HIDE_WITH_ANIM = 7749;

	// same value as that in ActionBarImpl.
	private static final int ANIM_DURATION = 250;

	// a little faster to catch up with the system UI
	private static final int ANIM_SHOW_DURATION = 250;

	private Context mContext;

	private View mHeader;
	private View mHeaderBackButton;
	private ImageView mHeaderLogo;
	private TextView mHeaderTitle;
	private TextView mHeaderSummary;
	private ViewGroup mHeaderOptionContainer;

	// for the final state of the last animation
	private boolean mFinallyVisible = false;

	private Animator mOnGoingAnimation;

	private VisibilityListener mExtVisibilityListener;

	private Handler mHandler;

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

					if (mExtVisibilityListener != null) {
						mExtVisibilityListener.onHideStart();
					}
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

	public PageHeader(View headerView) {
		mContext = headerView.getContext();
		findViews(headerView);

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
		mHeaderBackButton = mHeader.findViewById(R.id.pageheader_back_button);
		mHeaderBackButton.setClickable(true);
		mHeaderOptionContainer = (ViewGroup) mHeader
				.findViewById(R.id.pageheader_option_container);
		mHeaderLogo = (ImageView) mHeader.findViewById(R.id.pageheader_logo);
		mHeaderTitle = (TextView) mHeader.findViewById(R.id.pageheader_title);
		mHeaderSummary = (TextView) mHeader
				.findViewById(R.id.pageheader_summary);
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
			if (mExtVisibilityListener != null) {
				mExtVisibilityListener.onHideStart();
			}
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

		a.setInterpolator(AnimationUtils.loadInterpolator(mContext,
				R.interpolator.accelerate_cubic));
		a.setDuration(animDuration);
		a.addListener(mAnimHideListener);
		a.start();
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

	public void setBackButtonClickListener(OnClickListener clickListener) {
		mHeaderBackButton.setOnClickListener(clickListener);
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

	public void setVisibilityListener(VisibilityListener listener) {
		mExtVisibilityListener = listener;
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

		a.setInterpolator(AnimationUtils.loadInterpolator(mContext,
				R.interpolator.decelerate_cubic));
		a.setDuration(animDuration);
		a.addListener(mAnimShowListener);
		a.start();
	}
}
