package th.pd.mail.tidyface.compose;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import th.pd.mail.R;

import java.util.ArrayList;
import java.util.List;

/**
 * an agent for mail-compose header &amp; content
 */
class ComposeController {
	interface Listener {
		void onCleanExit();
		void onPickFile();
	}

	private static final int MAX_NUM_MODELS = 4;

	public static ComposeController newInstance(View containerView,
			Listener clickListener) {
		ComposeController instance = new ComposeController();
		instance.setupHolders(containerView);
		instance.setListener(clickListener);
		instance.addTab();
		return instance;
	}

	private ArrayList<ComposeModel> mModelList =
			new ArrayList<>(MAX_NUM_MODELS);
	private int mCurrentModelIndex = -1;

	private Listener mListener;

	private ViewGroup mTabContainer;
	private TextView mLabelSubject;
	private EditText mEditSubject;
	private View mBtnAttach0;
	private View mAttachmentRow;
	// TODO attachment
	private TextView mLabelRecipient;
	private EditText mEditRecipient;
	private View mCcRow;
	private EditText mEditCc;
	private View mBccRow;
	private EditText mEditBcc;
	private EditText mEditMailContent;

	public void addAttachment(Uri contentUri) {
		ComposeModel model = getCurrentModel();
		if (true) {
			model.addAttachment(contentUri);
			updateAttachmentRow(model);
		}
	}

	public boolean addTab() {
		if (mModelList.size() == MAX_NUM_MODELS) {
			return false;
		}

		ComposeModel model = new ComposeModel();
		mModelList.add(model);
		int tabIndex = mModelList.size() - 1; // focus on newly added tab
		getTabView(tabIndex).setVisibility(View.VISIBLE);
		switchTab(tabIndex, mCurrentModelIndex);
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

	private int getTabIndex(View view) {
		int tabIndex = mTabContainer.indexOfChild(view);
		if (tabIndex > 0) {
			return --tabIndex;
		}
		return -1;
	}

	private View getTabView(int tabIndex) {
		return mTabContainer.getChildAt(tabIndex + 1);
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

		View tabView = removeTabView(tabIndex);
		tabView.setVisibility(View.GONE);
		mTabContainer.addView(tabView);

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

	private View removeTabView(int tabIndex) {
		View tabView = getTabView(tabIndex);
		mTabContainer.removeViewAt(tabIndex + 1);
		return tabView;
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	private void setupHolders(View view) {
		mTabContainer = (ViewGroup) view.findViewById(R.id.tabContainer);
		mLabelSubject = (TextView) view.findViewById(R.id.labelSubject);
		mEditSubject = (EditText) view.findViewById(R.id.subject);
		mBtnAttach0 = view.findViewById(R.id.btnAttach0);
		mAttachmentRow = view.findViewById(R.id.attachmentRow);
		mLabelRecipient = (TextView) view.findViewById(R.id.labelRecipient);
		mEditRecipient = (EditText) view.findViewById(R.id.recipient);
		mCcRow = view.findViewById(R.id.ccRow);
		mEditCc = (EditText) view.findViewById(R.id.cc);
		mBccRow = view.findViewById(R.id.bccRow);
		mEditBcc = (EditText) view.findViewById(R.id.bcc);
		mEditMailContent = (EditText) view.findViewById(R.id.mailContent);

		setupTabContainer();

		mLabelSubject.setOnClickListener(new View.OnClickListener() {
			// toggle attachment row
			@Override
			public void onClick(View view) {
				ComposeModel model = getCurrentModel();
				updateToModel(model);
				boolean isShown = model.isAttachmentRowVisible();
				boolean toShow = model.toggleShowAttachmentRow();
				if (toShow != isShown) {
					updateTabLabelAndContent(model);
				}
			}
		});

		mBtnAttach0.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onPickFile();
				}
			}
		});

		mLabelRecipient.setOnClickListener(new View.OnClickListener() {
			// toggle cc and bcc row
			@Override
			public void onClick(View view) {
				ComposeModel model = getCurrentModel();
				updateToModel(model);
				boolean isShown = model.isCcBccRowVisible();
				boolean toShow = model.toggleShowCcBccRow();
				if (toShow != isShown) {
					updateTabLabelAndContent(model);
				}
			}
		});
	}

	private void setupTabContainer() {
		mTabContainer.removeAllViews();
		View.inflate(mTabContainer.getContext(), R.layout.line_vertical,
				mTabContainer);
		View.OnClickListener tabClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				switchTab(getTabIndex(view), mCurrentModelIndex);
			}
		};
		View.OnClickListener tabCloseListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ViewParent parent = view.getParent();
				if (parent instanceof View) {
					removeTab(getTabIndex((View) parent));
				}
			}
		};
		int tabHeight = mTabContainer.getResources().getDimensionPixelSize(
				R.dimen.compose_tab_height);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				0, tabHeight, 1f);
		for (int i = 0; i < MAX_NUM_MODELS; ++i) {
			View tabView = View.inflate(mTabContainer.getContext(),
					R.layout.hedwig_tab, null);
			tabView.setVisibility(View.GONE);
			tabView.setOnClickListener(tabClickListener);
			tabView.findViewById(R.id.btnTabClose).setOnClickListener(
					tabCloseListener);
			mTabContainer.addView(tabView, layoutParams);
		}
	}

	/**
	 * will trigger a forced refresh
	 */
	public void switchTab(int targetTabIndex, int sourceTabIndex) {
		if (targetTabIndex < 0) {
			return;
		}

		// push the swapped out tab
		if (sourceTabIndex >= 0) {
			ComposeModel sourceModel = getModel(sourceTabIndex);
			updateToModel(sourceModel);
			updateTabLabel(sourceModel);
		}

		// pull the swapped in tab
		mCurrentModelIndex = targetTabIndex;
		updateTabContainer();
		updateTabLabelAndContent(getCurrentModel());
	}

	private void updateTabContainer() {
		if (mModelList.isEmpty()) {
			return;
		}

		if (mModelList.size() == 1) {
			mTabContainer.setVisibility(View.GONE);
			return;
		}

		mTabContainer.setVisibility(View.VISIBLE);
		int textColor = mTabContainer.getResources().getColor(
				R.color.compose_tab_text);
		int textColorCurrent = mTabContainer.getResources().getColor(
				R.color.compose_tab_text_current);
		for (int i = 0; i < mModelList.size(); ++i) {
			View tabView = getTabView(i);
			TextView labelTabTitle = (TextView) tabView
					.findViewById(R.id.labelTabTitle);
			if (i == mCurrentModelIndex) {
				tabView.setBackgroundResource(R.color.compose_tab_bg_current);
				labelTabTitle.setTextColor(textColorCurrent);
			} else {
				tabView.setBackgroundResource(R.color.compose_tab_bg);
				labelTabTitle.setTextColor(textColor);
			}
		}
	}

	private void updateAttachmentRow(ComposeModel model) {
		if (model == null) {
			return;
		}
		if (model.shouldShowAttachmentRow()) {
			mLabelSubject.setText(R.string.subject);
			mBtnAttach0.setVisibility(View.GONE);
			mAttachmentRow.setVisibility(View.VISIBLE);
		} else {
			mLabelSubject.setText(R.string.subject0);
			mBtnAttach0.setVisibility(View.VISIBLE);
			mAttachmentRow.setVisibility(View.GONE);
		}
		// TODO show attachment
	}

	private void updateTabLabel(ComposeModel model) {
		if (model == null) {
			return;
		}
		TextView labelView = (TextView) getTabView(mCurrentModelIndex)
				.findViewById(R.id.labelTabTitle);
		String subject = model.getSubject();
		if (subject == null || subject.isEmpty()) {
			labelView.setText(R.string.compose);
		} else {
			labelView.setText(subject);
		}
	}

	private void updateTabLabelAndContent(ComposeModel model) {
		updateTabLabel(model);

		updateAttachmentRow(model);

		if (model.shouldShowCcBccRow()) {
			mLabelRecipient.setText(R.string.recipient);
			mCcRow.setVisibility(View.VISIBLE);
			mBccRow.setVisibility(View.VISIBLE);
		} else {
			mLabelRecipient.setText(R.string.recipient0);
			mCcRow.setVisibility(View.GONE);
			mBccRow.setVisibility(View.GONE);
		}

		// string content
		mEditSubject.setText(model.getSubject());
		mEditRecipient.setText(model.getRecipient());
		mEditCc.setText(model.getCc());
		mEditBcc.setText(model.getBcc());
		mEditMailContent.setText(model.getMailContent());
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
