package th.pd;

import th.intentSender.IntentSender;
import th.progressArc.ProgressArc;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

public class Main extends Activity {
	private static final String LOG_TAG = "MainActivity";

	private th.pageHeader.Demo mHeaderDemo;
	private IntentSender mIntentSender;

	private boolean onAction(int itemId) {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mIntentSender.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (onAction(item.getItemId())) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		super.onContextMenuClosed(menu);
		mHeaderDemo.hideSystemUi();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mHeaderDemo = new th.pageHeader.Demo(
				findViewById(R.id.pageHeader), findViewById(R.id.btnHeaderDemo));
		registerForContextMenu(findViewById(R.id.pageHeader));

		setHeaderTitleAsync();

		mIntentSender = new IntentSender(findViewById(R.id.intentSender),
				(TextView) findViewById(R.id.textLog));

		new th.progressArc.Demo(
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
	public boolean onOptionsItemSelected(MenuItem item) {
		return onAction(item.getItemId())
				|| super.onOptionsItemSelected(item);
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
				new String[] { OpenableColumns.DISPLAY_NAME }, null, null,
				null);
	}

}
