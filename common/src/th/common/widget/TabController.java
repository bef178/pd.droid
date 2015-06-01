package th.common.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * provide basic add/remove/get operation against tabView for UI<br/>
 * tabView is the hand hold for the tab content
 */
public class TabController {
	public interface Callback {
		View onTabCreate();
	}

	protected ViewGroup mTabContainer;
	private Callback mCallback;

	public TabController(ViewGroup tabContainer, Callback callback) {
		tabContainer.removeAllViews();
		mTabContainer = tabContainer;
		mCallback = callback;
	}

	public int addTab() {
		View tabView = mCallback.onTabCreate();
		if (tabView == null) {
			return -1;
		}
		mTabContainer.addView(tabView);
		return mTabContainer.getChildCount() - 1;
	}

	private int getTabCount() {
		return mTabContainer.getChildCount();
	}

	public int getTabIndex(View tabView) {
		return mTabContainer.indexOfChild(tabView);
	}

	public View getTabView(int tabIndex) {
		if (tabIndex < 0 || tabIndex >= getTabCount()) {
			return null;
		}
		return mTabContainer.getChildAt(tabIndex);
	}

	public void hideTabContainer() {
		mTabContainer.setVisibility(View.GONE);
	}

	public void removeTab(int tabIndex) {
		if (tabIndex < 0 || tabIndex >= getTabCount()) {
			return;
		}
		mTabContainer.removeViewAt(tabIndex);
	}

	public void showTabContainer() {
		mTabContainer.setVisibility(View.VISIBLE);
	}
}
