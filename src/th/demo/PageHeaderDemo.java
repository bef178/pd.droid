package th.demo;

import android.view.View;
import android.view.View.OnClickListener;

import th.pd.common.android.PageHeaderController;
import th.pd.common.android.SystemUiUtil;

public class PageHeaderDemo {

    private PageHeaderController mPageHeaderController;

    public PageHeaderDemo(View headerView, View demoButton) {
        mPageHeaderController = new PageHeaderController(headerView, null);
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
        SystemUiUtil.hideSystemUi(mPageHeaderController.getView()
                .getRootView());
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
