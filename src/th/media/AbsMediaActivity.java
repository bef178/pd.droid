package th.media;

import java.io.File;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import th.pd.R;
import th.pd.common.android.PageHeaderController;
import th.pd.common.android.SystemUiUtil;

/**
 * General media view activity.<br/>
 * Media includes video, audio and image.<br/>
 * This takes control of page header.<br/>
 *
 * @author tanghao
 */
public abstract class AbsMediaActivity extends Activity {

    static final String INTENT_EXTRA_LOGO = "intent.extra.LOGO";
    static final String INTENT_EXTRA_TITLE = Intent.EXTRA_TITLE;

    private PageHeaderController mPageHeaderController;

    private boolean mHasIntentTitle = false;

    private String getLogTag() {
        return this.getClass().getName();
    }

    protected boolean onAction(int actionId) {
        switch (actionId) {
            case R.id.actionPageHeaderBack:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    abstract protected void onCreate(Bundle savedInstanceState);

    protected void onCreate(Bundle savedInstanceState, int layoutRes) {
        super.onCreate(savedInstanceState);
        setContentView(layoutRes);
        setupPageHeaderController();
    }

    @Override
    protected void onPause() {
        mPageHeaderController.hideImmediately();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return onKeyEvent(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    protected boolean onKeyEvent(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemUiUtil.hideSystemUi(mPageHeaderController.getView()
                .getRootView());
        mPageHeaderController.showWithAnim();
        mPageHeaderController.hideWithDelay();
    }

    private void setLogo() {
        Bitmap logo = getIntent().getParcelableExtra(INTENT_EXTRA_LOGO);
        if (logo != null) {
            mPageHeaderController.setLogo(new BitmapDrawable(
                    getResources(), logo));
        }
    }

    protected void setSummary(CharSequence summary) {
        mPageHeaderController.setSummary(summary);
    }

    private void setTitle() {
        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            mPageHeaderController.setTitle(title);
            mHasIntentTitle = true;
            return;
        }
        setTitleByUri(getIntent().getData());
    }

    private void setTitleByQuery(Uri contentUri) {
        AsyncQueryHandler handler =
                new AsyncQueryHandler(getContentResolver()) {

                    @Override
                    protected void onQueryComplete(int token,
                            Object cookie,
                            Cursor cursor) {
                        try {
                            if ((cursor != null) && cursor.moveToFirst()) {
                                mPageHeaderController.setTitle(cursor
                                        .getString(0));
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
                mPageHeaderController.setTitle(f.getName());
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
                mPageHeaderController.setTitle(new File(uri.toString())
                        .getName());
                return true;
            }
        }
        return false;
    }

    private void setupPageHeaderController() {
        mPageHeaderController = new PageHeaderController(
                findViewById(R.id.pageHeader));
        mPageHeaderController
                .setCallback(new PageHeaderController.Callback() {

                    @Override
                    public boolean onAction(int actionId) {
                        return AbsMediaActivity.this.onAction(actionId);
                    }
                });

        findViewById(R.id.btnToggleHeader).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mPageHeaderController.isFinallyVisible()) {
                            mPageHeaderController.hideWithAnim();
                        } else {
                            mPageHeaderController.showWithAnim();
                            mPageHeaderController.hideWithDelay();
                        }
                    }
                });

        setLogo();
        setTitle();
    }
}
