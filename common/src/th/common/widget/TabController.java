package th.common.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * provide basic add/remove/get operation against tabContainer for UI<br/>
 * TODO support tabSeprator
 */
public class TabController {
	public interface Listener {
		View onTabCreate();
	}

	private ViewGroup mTabContainer;
	private Listener mListener;

	public TabController(ViewGroup tabContainer, Listener listener) {
		tabContainer.removeAllViews();
		mTabContainer = tabContainer;
		mListener = listener;
	}

	public int addTab() {
		View tabView = mListener.onTabCreate();
		if (tabView == null) {
			return -1;
		}
		mTabContainer.addView(tabView);
		return mTabContainer.getChildCount() - 1;
	}

	public int getTabCount() {
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

	public void hideContainer() {
		mTabContainer.setVisibility(View.GONE);
	}

	public View removeTab(int tabIndex) {
		if (tabIndex < 0 || tabIndex >= getTabCount()) {
			return null;
		}
		View tabView = getTabView(tabIndex);
		mTabContainer.removeViewAt(tabIndex);
		return tabView;
	}

	public void showContainer() {
		mTabContainer.setVisibility(View.VISIBLE);
	}
}
