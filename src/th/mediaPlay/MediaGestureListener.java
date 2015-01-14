package th.mediaPlay;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class MediaGestureListener extends
		GestureDetector.SimpleOnGestureListener {

	static interface Callback {

		boolean onFlingTo(int trend);

		boolean onScrollBy(int dx, int dy);
	}

	private Callback mCallback;

	public MediaGestureListener(Callback callback) {
		mCallback = callback;
	}

	private float abs(float f) {
		return f >= 0f ? f : -f;
	}

	private int getMoveTrend(float dx, float dy, float vx, float vy) {
		final int THRESHOLD_DISTANCE = 100;
		final int THRESHOLD_VELOCITY = 100;
		if (abs(dx) >= abs(dy)) {
			if (abs(dx) > THRESHOLD_DISTANCE
					&& abs(vx) > THRESHOLD_VELOCITY) {
				if (dx > 0) {
					return 6;
				} else {
					return 4;
				}
			}
		} else {
			if (abs(dy) > THRESHOLD_DISTANCE
					&& abs(vy) > THRESHOLD_VELOCITY) {
				if (dy > 0) {
					return 8;
				} else {
					return 2;
				}
			}
		}
		return 5;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2,
			float velocityX, float velocityY) {
		if (mCallback != null) {
			float dx = e2.getX() - e1.getX();
			float dy = e2.getY() - e1.getY();
			if (mCallback.onFlingTo(
					getMoveTrend(dx, dy, velocityX, velocityY))) {
				return true;
			}
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (mCallback != null) {
			if (mCallback.onScrollBy(
					(int) (e2.getRawX() - e1.getRawX()),
					(int) (e2.getRawY() - e1.getRawY()))) {
				return true;
			}
		}
		return super.onScroll(e1, e2, distanceX, distanceY);
	}
}
