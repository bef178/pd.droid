package th.pd.glry;

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

import t.typedef.droid.OnActionCallback;
import t.typedef.droid.pageheader.PageheaderController;

/**
 * General media view activity.<br/>
 * Media includes video, audio and image.<br/>
 * This takes control of page header.<br/>
 *
 * @author tanghao
 */
public abstract class AbsMediaActivity extends Activity implements
        OnActionCallback {

    public static final String TAG = AbsMediaActivity.class
            .getPackage().getName();

    static final String INTENT_EXTRA_LOGO = "intent.extra.LOGO";
    static final String INTENT_EXTRA_TITLE = Intent.EXTRA_TITLE;

    private PageheaderController mPageheaderController;

    private boolean mHasIntentTitle = false;

    @Override
    public boolean onAction(int actionId, Object extra) {
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
        mPageheaderController.showWithAnim();
        mPageheaderController.hideWithDelay();
    }

    protected void setSummary(CharSequence summary) {
        mPageheaderController.setSummary(summary);
    }

    private void setTitleAsync(Uri contentUri) {
        AsyncQueryHandler handler =
                new AsyncQueryHandler(getContentResolver()) {

                    @Override
                    protected void onQueryComplete(int token,
                            Object cookie,
                            Cursor cursor) {
                        try {
                            if ((cursor != null) && cursor.moveToFirst()) {
                                mPageheaderController.setTitle(cursor
                                        .getString(0));
                            }
                        } finally {
                            try {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } catch (Throwable t) {
                                Log.w(TAG, "fail to close", t);
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
            if (f.isFile()) {
                mPageheaderController.setTitle(f.getName());
                return true;
            }
            return false;
        }

        String scheme = uri.getScheme();
        if (scheme != null) {
            if (scheme.equals("content")) {
                setTitleAsync(uri);
                return true;
            } else if (scheme.equals("file")) {
                mPageheaderController.setTitle(
                        new File(uri.toString()).getName());
                return true;
            }
        }
        return false;
    }

    private void setupPageHeaderController() {
        mPageheaderController = new PageheaderController(
                findViewById(R.id.pageheader),
                new PageheaderController.Callback() {

                    @Override
                    public void onActionBack() {
                        onBackPressed();
                    }
                });

        findViewById(R.id.btnToggleHeader).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mPageheaderController.isFinallyVisible()) {
                            mPageheaderController.hideWithAnim();
                        } else {
                            mPageheaderController.showWithAnim();
                            mPageheaderController.hideWithDelay();
                        }
                    }
                });

        Bitmap logo = getIntent().getParcelableExtra(INTENT_EXTRA_LOGO);
        if (logo != null) {
            mPageheaderController.setLogo(new BitmapDrawable(
                    getResources(), logo));
        }

        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            mPageheaderController.setTitle(title);
            mHasIntentTitle = true;
        } else {
            setTitleByUri(getIntent().getData());
        }
    }
}
