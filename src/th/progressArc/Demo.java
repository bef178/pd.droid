package th.progressArc;

import th.progressArc.ProgressArc.ProgressChangeListener;
import android.os.Handler;

public class Demo {
	private static final int MSG_INCREASE = 0;

	private ProgressArc mProgArc;

	private Handler mHandler;

	public Demo(ProgressArc progArc) {
		mProgArc = progArc;
		mProgArc.setListener(
				new ProgressChangeListener() {
					@Override
					public void onChange(float value) {
					}

					@Override
					public void onComplete(float value) {
						mProgArc.setValue(0);
					}
				});

		mHandler = new Handler(progArc.getContext().getMainLooper()) {
			@Override
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case MSG_INCREASE:
					autoIncrease();
					start();
					break;
				}
			};
		};
	}

	private void autoIncrease() {
		mProgArc.setValue(mProgArc.getValue() + 0.02f);
	}

	public void start() {
		mHandler.sendEmptyMessageDelayed(MSG_INCREASE, 100);
	}
}
