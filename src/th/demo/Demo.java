package th.demo;

import th.demo.PageHeaderDemo;
import th.demo.ProgressArcDemo;
import th.intentSender.IntentSender;
import th.pd.R;
import th.common.widget.ProgressArc;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class Demo extends Activity {
    private static final String LOG_TAG = "Demo";

    private PageHeaderDemo mHeaderDemo;
    private IntentSender mIntentSender;

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIntentSender.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        mHeaderDemo.hideSystemUi();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);

        mHeaderDemo = new PageHeaderDemo(
                findViewById(R.id.pageHeader),
                findViewById(R.id.btnHeaderDemo));
        registerForContextMenu(findViewById(R.id.pageHeader));

        setHeaderTitleAsync();

        mIntentSender = new IntentSender(findViewById(R.id.intentSender),
                (TextView) findViewById(R.id.textLog));

        new ProgressArcDemo(
                (ProgressArc) findViewById(R.id.progressArc_demo)).start();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        menu.add(0, R.id.action_share, 0, "share");
        menu.add(0, R.id.action_next, 0, "next");
        menu.add(0, R.id.action_prev, 0, "prev");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHeaderDemo.onResume();
    }

    private void setHeaderTitleAsync() {
        Intent intent = getIntent();

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            mHeaderDemo.setTitle(title);
            return;
        }

        Uri uri = intent.getData();
        if (uri == null) {
            mHeaderDemo.setTitle(null);
            return;
        }

        AsyncQueryHandler aqh = new AsyncQueryHandler(
                getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        mHeaderDemo.setTitle(cursor.getString(0));
                    }
                } finally {
                    try {
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable t) {
                        Log.w(LOG_TAG, "fail to close", t);
                    }
                }
            }
        };
        aqh.startQuery(0, null, uri,
                new String[] {
                    OpenableColumns.DISPLAY_NAME
                }, null, null,
                null);
    }

}
