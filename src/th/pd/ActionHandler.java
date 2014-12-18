package th.pd;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class ActionHandler {

	public static final int MSG_ACTION = 200;

	public static final int ACTION_CUSTOM_BUTTON = 200;

	private Activity mActivity;
	private Handler mHandler;

	public ActionHandler(Activity activity) {
		mActivity = activity;
		mHandler = new Handler(mActivity.getMainLooper()) {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case MSG_ACTION: {
					onAction(message.arg1);
					break;
				}
				default:
					throw new AssertionError(message.what);
				}
			}
		};
	}

	public void onAction(int action, int delay) {
		Message msg = Message.obtain();
		msg.what = MSG_ACTION;
		msg.arg1 = action;
		mHandler.sendMessageDelayed(msg, delay);
	}

	public boolean onAction(int action) {
		switch (action) {
		case android.R.id.home:
			mActivity.finish();
			return true;
		case R.id.action_share: {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("image/jpeg");
			// Uri uri = getIntent().getData();
			String asUri =
					"file:///storage/emulated/0/Email Attachments/1415255574.jpg";
			Uri uri = Uri.parse(asUri);
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			mActivity.startActivity(Intent.createChooser(
					intent, mActivity.getString(R.string.share_via)));
			return true;
		}
		case R.id.action_next:
		case R.id.action_prev:
			break;
		case ACTION_CUSTOM_BUTTON:

			break;
		}
		return false;
	}
}
