package th.pd.mail.tidyface.compose;

import android.content.res.Resources;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import th.common.widget.TabControllerEx;
import th.pd.mail.R;

import java.util.ArrayList;
import java.util.List;

/**
 * an agent for mail-compose header &amp; content<br/>
 * models store data and status and no logic<br/>
 * views are honest to models<br/>
 */
class ComposeController implements TabControllerEx.CallbackEx {
	interface Listener {
		void onCleanExit();

		void onPickFile();
	}

	private static final int MAX_NUM_MODELS = 4;

	public static ComposeController newInstance(View containerView,
			Listener listener) {
		ComposeController instance = new ComposeController();
		instance.setupHolder(containerView);
		instance.setListener(listener);
		instance.addTab();
		return instance;
	}

	private ArrayList<ComposeModel> mModelList =
			new ArrayList<>(MAX_NUM_MODELS);
	private int mCurrentModelIndex = -1;

	private Resources mRes;

	private Listener mListener;
	private TabControllerEx mTabControllerEx;

	private View mTabContent;
	private TextView mLabelSubject;
	private EditText mEditSubject;
	private TextView mLabelRecipient;
	private EditText mEditRecipient;
	private View mCcRow;
	private EditText mEditCc;
	private View mBccRow;
	private EditText mEditBcc;
	private EditText mEditMailContent;

	private View mBtnAttach;
	// TODO attachment

	private final View.OnClickListener mTabClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			int tabIndex = mTabControllerEx.getTabIndex(view);
			if (tabIndex >= 0) {
				switchTab(tabIndex, mCurrentModelIndex);
			}
		}
	};

	private final View.OnClickListener mTabCloseListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			ViewParent parent = view.getParent();
			if (parent instanceof View) {
				int tabIndex = mTabControllerEx
						.getTabIndex((View) parent);
				if (tabIndex >= 0) {
					removeTab(tabIndex);
				}
			}
		}
	};

	public void addAttachment(Uri contentUri) {
		ComposeModel model = getCurrentModel();
		if (true) {
			model.addAttachment(contentUri);
			updateAttachmentRow(model);
		}
	}

	public boolean addTab() {
		if (mModelList.size() >= MAX_NUM_MODELS) {
			// TODO prompt
			return false;
		}
		int tabIndex = mTabControllerEx.addTab();
		if (tabIndex < 0) {
			return false;
		}
		ComposeModel model = new ComposeModel();
		mModelList.add(model);
		switchTab(tabIndex, mCurrentModelIndex); // focus on the added tab
		return true;
	}

	public ComposeModel getCurrentModel() {
		return getModel(mCurrentModelIndex);
	}

	public List<ComposeModel> getData() {
		return mModelList;
	}

	public ComposeModel getModel(int index) {
		if (index < 0 || index >= mModelList.size()) {
			return null;
		}
		return mModelList.get(index);
	}

	private void hideTabs() {
		mTabControllerEx.hideTabContainer();
		mTabContent.setBackground(null);
	}

	@Override
	public View onTabCreate(int viewType) {
		int tabHeight = mRes.getDimensionPixelSize(
				R.dimen.compose_tab_height);
		switch (viewType) {
			case TabControllerEx.VIEW_TYPE_BG:
				View tabView = View.inflate(mLabelSubject.getContext(),
						R.layout.classic_tab, null);
				tabView.setOnClickListener(mTabClickListener);
				tabView.findViewById(R.id.btnTabClose).setOnClickListener(
						mTabCloseListener);
				tabView.setLayoutParams(new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, tabHeight));
				return tabView;
			case TabControllerEx.VIEW_TYPE_SP:
				ImageView tabSpView = (ImageView) View.inflate(
						mLabelSubject.getContext(),
						R.layout.classic_tab_sp, null);
				tabSpView.setLayoutParams(new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, tabHeight));
				return tabSpView;
		}
		return null;
	}

	@Override
	public int onTabGetBgRes(int viewType) {
		switch (viewType) {
			case TabControllerEx.VIEW_TYPE_BG:
				return R.drawable.classic_tab_bg;
			case TabControllerEx.VIEW_TYPE_FG:
				return R.drawable.classic_tab_fg;
			case TabControllerEx.VIEW_TYPE_SP_BG_BG:
				return R.drawable.classic_tab_sp_bg_bg;
			case TabControllerEx.VIEW_TYPE_SP_BG_FG:
				return R.drawable.classic_tab_sp_bg_fg;
			case TabControllerEx.VIEW_TYPE_SP_BG_NA:
				return R.drawable.classic_tab_sp_bg_na;
			case TabControllerEx.VIEW_TYPE_SP_FG_BG:
				return R.drawable.classic_tab_sp_fg_bg;
			case TabControllerEx.VIEW_TYPE_SP_FG_NA:
				return R.drawable.classic_tab_sp_fg_na;
			case TabControllerEx.VIEW_TYPE_SP_NA_BG:
				return R.drawable.classic_tab_sp_na_bg;
			case TabControllerEx.VIEW_TYPE_SP_NA_FG:
				return R.drawable.classic_tab_sp_na_fg;
			default:
				return 0;
		}
	}

	public ComposeModel removeCurrentTab() {
		return removeTab(mCurrentModelIndex);
	}

	public ComposeModel removeTab(int tabIndex) {
		if (tabIndex < 0 || tabIndex >= mModelList.size()) {
			return null;
		}

		// TODO confirm dialog

		if (tabIndex == mCurrentModelIndex) {
			if (mModelList.size() == 1) {
				mCurrentModelIndex = -1;
			} else {
				int futureTabIndex = (tabIndex == 0) ? 0 : tabIndex - 1;
				switchTab(futureTabIndex, tabIndex);
			}
		} else if (tabIndex < mCurrentModelIndex) {
			--mCurrentModelIndex;
		}

		ComposeModel model = mModelList.remove(tabIndex);

		mTabControllerEx.removeTab(tabIndex);

		updateTabContainer();

		if (mModelList.isEmpty()) {
			if (mListener != null) {
				mListener.onCleanExit();
			} else {
				addTab();
			}
		}

		return model;
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	private void setupHolder(View view) {
		mRes = view.getResources();

		mTabControllerEx = new TabControllerEx(
				(ViewGroup) view.findViewById(R.id.tabContainer), this);

		mTabContent = view.findViewById(R.id.tabContent);
		mLabelSubject = (TextView) view.findViewById(R.id.labelSubject);
		mEditSubject = (EditText) view.findViewById(R.id.subject);
		mLabelRecipient = (TextView) view.findViewById(R.id.labelRecipient);
		mEditRecipient = (EditText) view.findViewById(R.id.recipient);
		mCcRow = view.findViewById(R.id.ccRow);
		mEditCc = (EditText) view.findViewById(R.id.cc);
		mBccRow = view.findViewById(R.id.bccRow);
		mEditBcc = (EditText) view.findViewById(R.id.bcc);
		mEditMailContent = (EditText) view.findViewById(R.id.mailContent);
		mBtnAttach = view.findViewById(R.id.btnAttach);

		mLabelRecipient.setOnClickListener(new View.OnClickListener() {
			// toggle cc and bcc row
			@Override
			public void onClick(View view) {
				ComposeModel model = getCurrentModel();
				updateToModel(model);
				if (model.hasCcOrBcc()) {
					// do not hide with cc/bcc
					return;
				}
				model.toggleShowCcBccRow();
				updateTabCaption(model,
						mTabControllerEx.getTabView(mCurrentModelIndex));
				updateTabContent(model);
			}
		});

		mBtnAttach.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onPickFile();
				}
			}
		});
	}

	private void showTabs() {
		mTabControllerEx.showTabContainer();
		mTabContent.setBackgroundResource(
				R.drawable.classic_tab_content_border);
	}

	/**
	 * will trigger a forced refresh
	 */
	public void switchTab(int targetTabIndex, int sourceTabIndex) {
		if (targetTabIndex < 0) {
			return;
		}

		if (sourceTabIndex >= 0) {
			ComposeModel sourceModel = getModel(sourceTabIndex);
			updateToModel(sourceModel);
		}

		mCurrentModelIndex = targetTabIndex;
		updateTabContainer();
		updateTabContent(getCurrentModel());
	}

	private void updateAttachmentRow(ComposeModel model) {
		if (model == null) {
			return;
		}
		// TODO show attachment
	}

	private TextView updateTabCaption(ComposeModel model, View tabView) {
		if (model == null) {
			return null;
		}
		TextView txtCaption = (TextView) tabView
				.findViewById(R.id.txtTabCaption);
		String subject = model.getSubject();
		if (subject == null || subject.isEmpty()) {
			txtCaption.setText(R.string.compose);
		} else {
			txtCaption.setText(subject);
		}
		return txtCaption;
	}

	private void updateTabContainer() {
		if (mModelList.isEmpty()) {
			return;
		}

		if (mModelList.size() == 1) {
			hideTabs();
			return;
		}

		showTabs();
		int textColor = mRes.getColor(R.color.compose_tab_text);
		int textColorCurrent = mRes
				.getColor(R.color.compose_tab_text_current);
		for (int i = 0; i < mModelList.size(); ++i) {
			ComposeModel model = mModelList.get(i);
			View tabView = mTabControllerEx.getTabView(i);

			TextView txtCaption = updateTabCaption(model, tabView);
			if (i == mCurrentModelIndex) {
				txtCaption.setTextColor(textColorCurrent);
			} else {
				txtCaption.setTextColor(textColor);
			}
		}
		mTabControllerEx.setActiveTab(mCurrentModelIndex);
	}

	private void updateTabContent(ComposeModel model) {
		// cc/bcc visibility
		if (model.queryStatusIsCcBccRowShown()) {
			mLabelRecipient.setText(R.string.recipient);
			mCcRow.setVisibility(View.VISIBLE);
			mBccRow.setVisibility(View.VISIBLE);
		} else {
			mLabelRecipient.setText(R.string.recipient0);
			mCcRow.setVisibility(View.GONE);
			mBccRow.setVisibility(View.GONE);
		}

		// content texts
		mEditSubject.setText(model.getSubject());
		mEditRecipient.setText(model.getRecipient());
		mEditCc.setText(model.getCc());
		mEditBcc.setText(model.getBcc());
		mEditMailContent.setText(model.getMailContent());

		updateAttachmentRow(model);
	}

	private void updateToModel(ComposeModel model) {
		if (model == null) {
			return;
		}
		model.setSubject(mEditSubject.getText().toString());
		// TODO attachment
		model.setRecipient(mEditRecipient.getText().toString());
		model.setCc(mEditCc.getText().toString());
		model.setBcc(mEditBcc.getText().toString());
		model.setMailContent(mEditMailContent.getText().toString());
	}
}
