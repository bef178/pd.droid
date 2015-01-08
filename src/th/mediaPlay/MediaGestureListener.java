package th.mediaPlay;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class MediaGestureListener extends
		GestureDetector.SimpleOnGestureListener {

	static interface Callback {
		void flingBy(int offset);
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
		if (mCallback == null) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		float dx = e2.getX() - e1.getX();
		float dy = e2.getY() - e2.getY();
		switch (getMoveTrend(dx, dy, velocityX, velocityY)) {
			case 6:
				mCallback.flingBy(1);
				return true;
			case 4:
				mCallback.flingBy(-1);
				return true;
			default:
				break;
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}
}
