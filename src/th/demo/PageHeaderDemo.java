package th.demo;

import th.common.SystemUiUtil;
import th.common.widget.PageHeaderController;

import android.view.View;
import android.view.View.OnClickListener;

public class PageHeaderDemo {

	private PageHeaderController mPageHeaderController;

	public PageHeaderDemo(View headerView, View demoButton) {
		mPageHeaderController = new PageHeaderController(headerView);
		demoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPageHeaderController.isFinallyVisible()) {
					mPageHeaderController.hideWithAnim();
					showOptions();
				} else {
					mPageHeaderController.showWithAnim();
					mPageHeaderController.hideWithDelay();
				}
			}
		});
	}

	public void hideSystemUi() {
		SystemUiUtil.hideSystemUi(mPageHeaderController.getView().getRootView());
	}

	public void onResume() {
		hideSystemUi();
		mPageHeaderController.showWithAnim();
		mPageHeaderController.hideWithDelay();
	}

	public void setTitle(CharSequence title) {
		mPageHeaderController.setTitle(title);
	}

	private void showOptions() {
		mPageHeaderController.getView().showContextMenu();
	}
}
