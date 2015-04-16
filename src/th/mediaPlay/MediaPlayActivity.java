package th.mediaPlay;

import java.io.File;

import th.common.SystemUiUtil;
import th.common.widget.PageHeader;
import th.pd.R;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * General media play activity.<br/>
 * Media includes video, audio and image.<br/>
 * This make a control of page header.<br/>
 *
 * @author tanghao
 */
public abstract class MediaPlayActivity extends Activity {

	static final String INTENT_EXTRA_LOGO = "intent.extra.LOGO";
	static final String INTENT_EXTRA_TITLE = Intent.EXTRA_TITLE;

	private PageHeader mPageHeader;

	private boolean mHasIntentTitle = false;

	private String getLogTag() {
		return this.getClass().getName();
	}

	private void hideSystemUi() {
		SystemUiUtil.hideSystemUi(mPageHeader.getView().getRootView());
	}

	@Override
	abstract protected void onCreate(Bundle savedInstanceState);

	protected void onCreate(Bundle savedInstanceState, int layoutRes) {
		super.onCreate(savedInstanceState);
		setContentView(layoutRes);
		setupPageHeader();
	}

	@Override
	protected void onPause() {
		mPageHeader.hideImmediately();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideSystemUi();
		mPageHeader.showWithAnim();
		mPageHeader.hideWithDelay();
	}

	private void setLogo() {
		Bitmap logo = getIntent().getParcelableExtra(INTENT_EXTRA_LOGO);
		if (logo != null) {
			mPageHeader.setLogo(new BitmapDrawable(getResources(), logo));
		}
	}

	protected void setSummary(CharSequence summary) {
		mPageHeader.setSummary(summary);
	}

	private void setTitle() {
		String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
		if (title != null) {
			mPageHeader.setTitle(title);
			mHasIntentTitle = true;
			return;
		}
		setTitleByUri(getIntent().getData());
	}

	private void setTitleByQuery(Uri contentUri) {
		AsyncQueryHandler handler =
				new AsyncQueryHandler(getContentResolver()) {
					@Override
					protected void onQueryComplete(int token, Object cookie,
							Cursor cursor) {
						try {
							if ((cursor != null) && cursor.moveToFirst()) {
								mPageHeader.setTitle(cursor.getString(0));
							}
						} finally {
							try {
								if (cursor != null) {
									cursor.close();
								}
							} catch (Throwable t) {
								Log.w(getLogTag(), "fail to close", t);
							}
						}
					}
				};
		handler.startQuery(0, null, contentUri,
				new String[] {
					OpenableColumns.DISPLAY_NAME
				}, null, null, null);
	}

	/**
	 * @return <code>true</code> iff title do be set by this method
	 */
	protected boolean setTitleByUri(Uri uri) {
		if (mHasIntentTitle || uri == null) {
			return false;
		}

		if (uri.isRelative()) {
			// try file scheme
			File f = new File(uri.toString());
			if (f.exists() && f.isFile()) {
				// same as file scheme
				mPageHeader.setTitle(f.getName());
				return true;
			}
			return false;
		}

		String origUriScheme = uri.getScheme();
		if (origUriScheme != null) {
			if (origUriScheme.equals("content")) {
				setTitleByQuery(uri);
				return true;
			} else if (origUriScheme.equals("file")) {
				mPageHeader.setTitle(new File(uri.toString()).getName());
				return true;
			}
		}
		return false;
	}

	private void setupPageHeader() {
		mPageHeader = new PageHeader(findViewById(R.id.pageHeader));
		mPageHeader.setBackButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MediaPlayActivity.this.onBackPressed();
			}
		});

		findViewById(R.id.btnToggleHeader).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mPageHeader.isFinallyVisible()) {
							mPageHeader.hideWithAnim();
						} else {
							mPageHeader.showWithAnim();
							mPageHeader.hideWithDelay();
						}
					}
				});

		setLogo();
		setTitle();
	}
}
