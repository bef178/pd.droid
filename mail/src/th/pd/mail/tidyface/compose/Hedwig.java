package th.pd.mail.tidyface.compose;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import th.common.widget.TitlebarController;
import th.pd.mail.R;

public class Hedwig extends Fragment implements
		TitlebarController.Listener, ComposeController.Listener {

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
					res.getDimensionPixelOffset(R.dimen.compose_window_move_margin_top),
					res.getDimensionPixelOffset(R.dimen.compose_window_move_margin_right),
					res.getDimensionPixelOffset(R.dimen.compose_window_move_margin_bottom),
					res.getDimensionPixelOffset(R.dimen.compose_window_move_margin_left)
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
		private void findAndSetWindowAnchorPoint(int[] anchor) {
			mDecorView.getLocationOnScreen(anchor);
			anchor[0] += mDecorView.getWidth() / 2;
			anchor[1] += mDecorView.getHeight() / 2;
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

			// the offset after adjust
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

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final int rawX = (int) event.getRawX();
			final int rawY = (int) event.getRawY();
			event.getX();

			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					findAndSetWindowAnchorPoint(mAnchor);
					findAndSetAcceptableAnchorRect(v);
					mTouchPointFromAnchor[0] = rawX - mAnchor[0];
					mTouchPointFromAnchor[1] = rawY - mAnchor[1];
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_MOVE:
					onMove(rawX, rawY);
					break;
			}
			return true;
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
		getView().findViewById(R.id.titlebar).setOnTouchListener(
				new MoveListener());
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
		mComposeController.addTab();
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
		TitlebarController.newInstance(view, this);
		mComposeController = ComposeController.newInstance(view, this);
		mBtnSend = view.findViewById(R.id.btnSend);

		return view;
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
		ComposeModel model = mComposeController.removeCurrentTab();
		// TODO
	}
}
