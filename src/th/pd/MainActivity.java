package th.pd;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final int HEADER_TIMEOUT = 3500;

	private PageHeader mHeader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aaa);

		View button = findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView tv = (TextView) findViewById(R.id.textView);
				tv.setText(tv.getText() + ",.");
				if (mHeader.isFinallyVisible()) {
					mHeader.hideImmediately();
				} else {
					mHeader.showWithAnim();
					mHeader.hideWithDelay(HEADER_TIMEOUT);
				}
			}
		});

		final View decor = getWindow().getDecorView();

		mHeader = new PageHeader(findViewById(R.id.pageHeader));
		mHeader.setVisibilityListener(new PageHeader.VisibilityListener() {

			@Override
			public void onHideStart() {
				hideSystemUi(decor);
			}
		});

		mHeader.setBackButtonClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		hideSystemUi(decor);
	}

	private void hideSystemUi(View view) {
		int flags = view.getSystemUiVisibility()
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		view.setSystemUiVisibility(flags);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHeader.showImmediately();
		mHeader.hideWithDelay(HEADER_TIMEOUT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
