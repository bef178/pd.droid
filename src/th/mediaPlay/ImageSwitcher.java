package th.mediaPlay;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ViewAnimator;

public class ImageSwitcher extends ViewAnimator {

	private static final String LOG_TAG = ImageSwitcher.class.getName();

	public ImageSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

//	private GestureDetector mGestureDetector;
//
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		if (mGestureDetector != null) {
//			return mGestureDetector.onTouchEvent(ev);
//		}
//		return super.onInterceptTouchEvent(ev);
//	}
//
//	public void setGestureDetector(GestureDetector gestureDetector) {
//		mGestureDetector = gestureDetector;
//	}

	/**
	 * @param pos
	 * @return <code>true</code> if the pos is set as result
	 */
	public boolean switchTo(View nextView, Animation animEnter, Animation animLeave) {
		setInAnimation(animEnter);
		setOutAnimation(animLeave);

		switch (getChildCount()) {
			case 1:
				if (getChildAt(0) != nextView && nextView != null) {
					addView(nextView);
					setDisplayedChild(1);
					removeViewAt(0);
				}
				break;
			case 0:
				addView(nextView);
				setDisplayedChild(0);
				break;
			default:
				Log.e(LOG_TAG, "too many child views: " + getChildCount());
				setInAnimation(null);
				setOutAnimation(null);
				removeAllViews();
				addView(nextView);
				setDisplayedChild(0);
				break;
		}
		return true;
	}

}
