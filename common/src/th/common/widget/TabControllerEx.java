package th.common.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * provide basic add/remove/get operation against tabView for UI<br/>
 * tabView is the hand hold for the tab content<br/>
 * support tab separator<br/>
 * support only 1 active tab<br/>
 * <br/>
 * after think over, i place tab sp change logic here
 */
public class TabControllerEx extends TabController {
	public interface CallbackEx {
		View onTabCreate(int viewType);
		int onTabGetBgRes(int viewType);
	}

	private static final int VIEW_TYPE_NA = 0x0;
	public static final int VIEW_TYPE_BG = 0x1;
	public static final int VIEW_TYPE_FG = 0x2;
	public static final int VIEW_TYPE_SP = 0x4;

	public static final int VIEW_TYPE_SP_BG_BG =
			(VIEW_TYPE_BG << 4) | (VIEW_TYPE_BG << 2);
	public static final int VIEW_TYPE_SP_BG_FG =
			(VIEW_TYPE_BG << 4) | (VIEW_TYPE_FG << 2);
	public static final int VIEW_TYPE_SP_BG_NA =
			(VIEW_TYPE_BG << 4) | (VIEW_TYPE_NA << 2);
	public static final int VIEW_TYPE_SP_FG_BG =
			(VIEW_TYPE_FG << 4) | (VIEW_TYPE_BG << 2);
	public static final int VIEW_TYPE_SP_FG_NA =
			(VIEW_TYPE_FG << 4) | (VIEW_TYPE_NA << 2);
	public static final int VIEW_TYPE_SP_NA_BG =
			(VIEW_TYPE_NA << 4) | (VIEW_TYPE_BG << 2);
	public static final int VIEW_TYPE_SP_NA_FG =
			(VIEW_TYPE_NA << 4) | (VIEW_TYPE_FG << 2);

	// for switch-case the static finals cannot take advantage of makeViewTypeSp()
	static int makeViewTypeSp(int l, int r) {
		return (l << 4) | (r << 2);
	}

	private static int toInternalIndex(int tabIndex) {
		return tabIndex * 2 + 1;
	}

	private CallbackEx mCallbackEx;

	public TabControllerEx(ViewGroup tabContainer, CallbackEx callbackEx) {
		super(tabContainer, null);
		mCallbackEx = callbackEx;
	}

	@Override
	public int addTab() {
		View tabView = mCallbackEx.onTabCreate(VIEW_TYPE_BG);
		if (tabView == null) {
			return -1;
		}
		View tabPrevSpView = mCallbackEx.onTabCreate(VIEW_TYPE_SP);
		if (tabPrevSpView == null) {
			return -1;
		}
		int count = getTabCount();
		if (count == 0) {
			View tabNextSpView = mCallbackEx.onTabCreate(VIEW_TYPE_SP);
			if (tabNextSpView == null) {
				return -1;
			}
			mTabContainer.addView(tabPrevSpView);
			mTabContainer.addView(tabView);
			mTabContainer.addView(tabNextSpView);
		} else {
			int index = toInternalIndex(count - 1) + 1;
			mTabContainer.addView(tabPrevSpView, index);
			mTabContainer.addView(tabView, index + 1);
		}

		return getTabCount() - 1;
	}

	@Override
	public int getTabCount() {
		return (mTabContainer.getChildCount() - 1) / 2;
	}

	@Override
	public int getTabIndex(View tabView) {
		return (mTabContainer.indexOfChild(tabView) - 1) / 2;
	}

	@Override
	public View getTabView(int tabIndex) {
		if (!isValidTabIndex(tabIndex)) {
			return null;
		}
		return mTabContainer.getChildAt(toInternalIndex(tabIndex));
	}

	private boolean isValidTabIndex(int tabIndex) {
		return tabIndex >= 0 || tabIndex < getTabCount();
	}

	/**
	 * remove the tab and the prev tab sp, if plural
	 */
	@Override
	public void removeTab(int tabIndex) {
		if (!isValidTabIndex(tabIndex)) {
			return;
		}

		int count = getTabCount();
		switch (count) {
			case 0:
				break;
			case 1:
				mTabContainer.removeAllViews();
				break;
			default:
				mTabContainer.removeViews(toInternalIndex(tabIndex) - 1, 2);
				break;
		}
	}

	public void setActiveTab(int tabIndex) {
		if (!isValidTabIndex(tabIndex)) {
			return;
		}

		int size = mTabContainer.getChildCount();

		// should not involve R at this common controller, get via callback
		mTabContainer.getChildAt(0).setBackgroundResource(mCallbackEx
				.onTabGetBgRes(VIEW_TYPE_SP_NA_BG));
		mTabContainer.getChildAt(size - 1).setBackgroundResource(mCallbackEx
				.onTabGetBgRes(VIEW_TYPE_SP_BG_NA));
		for (int i = 1; i < size - 1; ++i) {
			View view = mTabContainer.getChildAt(i);
			if ((i & 0x01) == 0) {
				// tab sp
				view.setBackgroundResource(mCallbackEx
						.onTabGetBgRes(VIEW_TYPE_SP_BG_BG));
			} else {
				view.setBackgroundResource(mCallbackEx
						.onTabGetBgRes(VIEW_TYPE_BG));
			}
		}

		int index = toInternalIndex(tabIndex);
		mTabContainer.getChildAt(index).setBackgroundResource(
				mCallbackEx.onTabGetBgRes(VIEW_TYPE_FG));
		mTabContainer.getChildAt(index - 1).setBackgroundResource(
				mCallbackEx.onTabGetBgRes((index - 1 == 0)
						? VIEW_TYPE_SP_NA_FG
						: VIEW_TYPE_SP_BG_FG));
		mTabContainer.getChildAt(index + 1).setBackgroundResource(
				mCallbackEx.onTabGetBgRes((index + 1 == size - 1)
						? VIEW_TYPE_SP_FG_NA
						: VIEW_TYPE_SP_FG_BG));
	}
}
