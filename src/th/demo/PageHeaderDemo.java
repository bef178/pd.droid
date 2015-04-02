package th.demo;

import th.common.SystemUiUtil;
import th.common.widget.PageHeader;

import android.view.View;
import android.view.View.OnClickListener;

public class PageHeaderDemo {

	private PageHeader mHeader;

	public PageHeaderDemo(View headerView, View demoButton) {
		mHeader = new PageHeader(headerView);
		demoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHeader.isFinallyVisible()) {
					mHeader.hideWithAnim();
					showOptions();
				} else {
					mHeader.showWithAnim();
					mHeader.hideWithDelay();
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
		mHeader.hideWithDelay();
	}

	public void setTitle(CharSequence title) {
		mHeader.setTitle(title);
	}

	private void showOptions() {
		mHeader.getView().showContextMenu();
	}
}
