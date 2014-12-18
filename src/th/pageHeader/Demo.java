package th.pageHeader;

import th.pd.SystemUiUtil;
import android.view.View;
import android.view.View.OnClickListener;

public class Demo {

	private static final int HEADER_DISPLAY_TIMEOUT = 3000;

	private PageHeader mHeader;

	public Demo(View headerView, View demoButton) {
		mHeader = new PageHeader(headerView);
		demoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHeader.isFinallyVisible()) {
					mHeader.hideWithAnim();
					showOptions();
				} else {
					mHeader.showWithAnim();
					mHeader.hideWithDelay(HEADER_DISPLAY_TIMEOUT);
				}
			}
		});
	}

	public void hideSystemUi() {
		SystemUiUtil.hideSystemUi(mHeader.getView().getRootView());
	}

	public void onResume() {
		hideSystemUi();
		mHeader.showWithAnim();
		mHeader.hideWithDelay(HEADER_DISPLAY_TIMEOUT);
	}

	public void setTitle(CharSequence title) {
		mHeader.setTitle(title);
	}

	private void showOptions() {
		mHeader.getView().showContextMenu();
	}
}
