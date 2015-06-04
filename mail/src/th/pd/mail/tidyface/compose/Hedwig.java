package th.pd.mail.tidyface.compose;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import th.common.widget.TitlebarController;
import th.pd.mail.R;
import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.dao.MessageForSend;

public class Hedwig extends Fragment implements
		TitlebarController.Listener, ComposeController.Listener {

	private class DoubleClickListener implements View.OnTouchListener {
		private static final int ACCEPTABLE_OFFSET = 10;
		private static final int ACCEPTABLE_TIMEOUT = 250;

		private long mLastActionTimestamp = -1;
		private Point mFirstTouchPoint = new Point();
		private int mDoubleClickStep = 0;

		private boolean allowsPosition(int rawX, int rawY) {
			return rawX - mFirstTouchPoint.x <= ACCEPTABLE_OFFSET
					&& rawY - mFirstTouchPoint.y <= ACCEPTABLE_OFFSET;
		}

		private boolean allowsTimestamp() {
			return SystemClock.elapsedRealtime() - mLastActionTimestamp <= ACCEPTABLE_TIMEOUT;
		}

		private void onTouchDown(MotionEvent event) {
			if (mDoubleClickStep == 0) {
				mFirstTouchPoint.x = (int) event.getRawX();
				mFirstTouchPoint.y = (int) event.getRawY();
				mDoubleClickStep = 1;
				return;
			}

			if (!allowsTimestamp()) {
				mDoubleClickStep = 1;
				return;
			}

			if (mDoubleClickStep == 2
					&& allowsPosition((int) event.getRawX(),
							(int) event.getRawY())) {
				mDoubleClickStep = 3;
			} else {
				mDoubleClickStep = 1;
			}
		}

		private void onTouchUp(MotionEvent event) {
			if (!allowsTimestamp()
					|| !allowsPosition((int) event.getRawX(),
							(int) event.getRawY())) {
				mDoubleClickStep = 0;
				return;
			}

			++mDoubleClickStep;
			if (mDoubleClickStep == 4) {
				mDoubleClickStep = 0;

				// trigger double click
				onClickMaximize(null);
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					onTouchDown(event);
					mLastActionTimestamp = SystemClock.elapsedRealtime();
					return true;
				case MotionEvent.ACTION_UP:
					onTouchUp(event);
					mLastActionTimestamp = SystemClock.elapsedRealtime();
					return true;
			}
			mLastActionTimestamp = SystemClock.elapsedRealtime();
			return false;
		}
	}

	/**
	 * the finger offset maps to the anchor coordinate<br/>
	 * the anchor coordinate is restricted then maps to the layout offset<br/>
	 */
	private class MoveListener implements View.OnTouchListener {
		private final int[] MARGIN;
		private final View mDecorView;
		private WindowManager.LayoutParams mLayoutParams;

		// i don't believe any resize triggered during moving
		private Rect mAcceptableAnchorRect = new Rect();

		// the center point of the window
		private int[] mAnchor = new int[2];

		private int[] mTouchPointFromAnchor = new int[2];

		public MoveListener() {
			mDecorView = getActivity().getWindow().getDecorView();
			Resources res = getActivity().getResources();
			MARGIN = new int[] {
					res.getDimensionPixelOffset(R.dimen.compose_titlebar_move_margin_top),
					res.getDimensionPixelOffset(R.dimen.compose_titlebar_move_margin_right),
					res.getDimensionPixelOffset(R.dimen.compose_titlebar_move_margin_bottom),
					res.getDimensionPixelOffset(R.dimen.compose_titlebar_move_margin_left)
			};
		}

		private void findAndSetAcceptableAnchorRect(View handleView) {
			Display defDisplay = getActivity().getWindowManager()
					.getDefaultDisplay();
			Point screenSize = new Point();
			defDisplay.getSize(screenSize);

			// the boundary for the anchor point;
			// for determine whether the window goes beyoud the boundary
			mAcceptableAnchorRect.top = -handleView.getTop() + MARGIN[0];
			mAcceptableAnchorRect.right =
					screenSize.x - handleView.getRight() - MARGIN[1];
			mAcceptableAnchorRect.bottom =
					screenSize.y - handleView.getBottom() - MARGIN[2];
			mAcceptableAnchorRect.left = -handleView.getLeft() + MARGIN[3];
			mAcceptableAnchorRect.offset(mDecorView.getWidth() / 2,
					mDecorView.getHeight() / 2);
		}

		/**
		 * take the center point as anchor point
		 */
		private void findAndSetWindowAnchorPoint(View decorView, int[] anchor) {
			decorView.getLocationOnScreen(anchor);
			anchor[0] += decorView.getWidth() / 2;
			anchor[1] += decorView.getHeight() / 2;
		}

		private void onMove(int rawX, int rawY) {
			int anchorX = rawX - mTouchPointFromAnchor[0];
			int anchorY = rawY - mTouchPointFromAnchor[1];

			if (anchorX < mAcceptableAnchorRect.left) {
				anchorX = mAcceptableAnchorRect.left;
			} else if (anchorX > mAcceptableAnchorRect.right) {
				anchorX = mAcceptableAnchorRect.right;
			}

			if (anchorY < mAcceptableAnchorRect.top) {
				anchorY = mAcceptableAnchorRect.top;
			} else if (anchorY > mAcceptableAnchorRect.bottom) {
				anchorY = mAcceptableAnchorRect.bottom;
			}

			// the offset after restrict
			int dx = anchorX - mAnchor[0];
			int dy = anchorY - mAnchor[1];

			mLayoutParams = (WindowManager.LayoutParams) mDecorView
					.getLayoutParams();
			mLayoutParams.x += dx;
			mLayoutParams.y += dy;
			mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
			getActivity().getWindowManager().updateViewLayout(
					mDecorView, mLayoutParams);

			mAnchor[0] += dx;
			mAnchor[1] += dy;
		}

		private void onMoveBegin(View view, int rawX, int rawY) {
			findAndSetWindowAnchorPoint(mDecorView, mAnchor);
			findAndSetAcceptableAnchorRect(view);
			mTouchPointFromAnchor[0] = rawX - mAnchor[0];
			mTouchPointFromAnchor[1] = rawY - mAnchor[1];
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final int rawX = (int) event.getRawX();
			final int rawY = (int) event.getRawY();

			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					onMoveBegin(v, rawX, rawY);
					return true;
				case MotionEvent.ACTION_MOVE:
					onMove(rawX, rawY);
					return true;
			}
			return false;
		}
	}

	private static final int REQUEST_CODE_PICK_FILE = 11;

	private ComposeController mComposeController;
	private View mBtnSend;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBtnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSendMail();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_PICK_FILE:
				switch (resultCode) {
					case Activity.RESULT_OK:
						Uri contentUri = data.getData();
						mComposeController.addAttachment(contentUri);
						break;
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCleanExit() {
		this.getActivity().finish();
	}

	@Override
	public void onClickClose(View btnClose) {
		onCleanExit();
	}

	@Override
	public void onClickMaximize(View btnMaximize) {
		Toast.makeText(getActivity(), "max", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClickMinimize(View btnMinimize) {
		Toast.makeText(getActivity(), "min", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClickResize(View btnResize) {
		Toast.makeText(getActivity(), "resize", Toast.LENGTH_SHORT).show();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// make sure the default window padding is cleared
		getActivity().getWindow().setBackgroundDrawable(null);

		View view = inflater.inflate(R.layout.hedwig, container, false);
		TitlebarController.newInstance(view, this).setTitlebarTouchListener(
				new View.OnTouchListener() {
					private DoubleClickListener mDoubleClickListener =
							new DoubleClickListener();
					private MoveListener mMoveListener = new MoveListener();

					@Override
					public boolean onTouch(View view, MotionEvent event) {
						boolean handled = false;
						handled = mDoubleClickListener.onTouch(view, event)
								| handled;
						handled = mMoveListener.onTouch(view, event)
								| handled;
						return handled;
					}
				});
		mComposeController = ComposeController.newInstance(view, this);
		mBtnSend = view.findViewById(R.id.btnSend);

		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		relayout();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onPickFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					REQUEST_CODE_PICK_FILE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getActivity(),
					R.string.cannot_find_app_to_pick_file, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void onSendMail() {
		MessageForSend syncMessage = new MessageForSend();
		syncMessage.setMessage(
				mComposeController.getUpdatedModel().getMessage());
		processSendMail(syncMessage, true, true);
	}

	/**
	 * @return an error code to tell which is incorrect
	 */
	private int processSendMail(final MessageForSend syncMessage,
			final boolean checksEmptySubject,
			final boolean checksEmptyContent) {
		if (!syncMessage.hasRecipient()) {
			// no recipient
			return -1;
		}

		if (checksEmptySubject && !syncMessage.hasSubject()) {
			DialogInterface.OnClickListener dialogButtonListener =
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							if (whichButton == DialogInterface.BUTTON_POSITIVE) {
								processSendMail(syncMessage, false,
										checksEmptyContent);
							}
							dialog.dismiss();
						}
					};
			new AlertDialog.Builder(getActivity())
					.setMessage("empty SUBJECT: send anyway?")
					.setPositiveButton("send", dialogButtonListener)
					.setNegativeButton("cancel", dialogButtonListener)
					.create().show();
			return 0;
		}

		if (checksEmptyContent && !syncMessage.hasContent()) {
			DialogInterface.OnClickListener dialogButtonListener =
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							if (whichButton == DialogInterface.BUTTON_POSITIVE) {
								processSendMail(syncMessage,
										checksEmptySubject, false);
							}
							dialog.dismiss();
						}
					};
			new AlertDialog.Builder(getActivity())
					.setMessage("empty CONTENT: send anyway?")
					.setPositiveButton("send", dialogButtonListener)
					.setNegativeButton("cancel", dialogButtonListener)
					.create().show();
			return 0;
		}

		syncMessage.setServerAuth(FastSyncAccess
				.getCurrentServerAuthForSend());
		// TODO ... and other necessary stuff

		if (ActivityManager.isUserAMonkey()) {
			// monkey user
			return -9;
		}
		FastSyncAccess.addMessageForSend(syncMessage);
		FastSyncAccess.awakeWorkerIfNecessary();

		// successfully enqueued
		return 1;
	}

	/**
	 * the screen anchor coord changes after orientation changes,<br/>
	 * thus everything based on "offset" will be invalid.<br/>
	 * in this case, the screen anchor is (width/2,
	 * (height-statusbarHeight-navigationbarHeight)/2+statusbarHeight) and with
	 * error +1/-1<br/>
	 * but we shouldn't assume anything.<br/>
	 * so just put the window back to screen center.
	 */
	private void relayout() {
		View decorView = getActivity().getWindow().getDecorView();
		WindowManager.LayoutParams layoutParams =
				(WindowManager.LayoutParams) decorView.getLayoutParams();
		layoutParams.x = 0;
		layoutParams.y = 0;
		layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		getActivity().getWindowManager().updateViewLayout(
				decorView, layoutParams);
	}
}
