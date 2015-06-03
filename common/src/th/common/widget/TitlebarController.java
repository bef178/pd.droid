package th.common.widget;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import th.common.R;

public class TitlebarController {
	public interface Listener {
		void onClickClose(View btnClose);
		void onClickMaximize(View btnMaximize);
		void onClickMinimize(View btnMinimize);
		void onClickResize(View btnResize);
	}

	public static TitlebarController newInstance(View view,
			Listener listener) {
		TitlebarController instance = new TitlebarController();
		instance.setupHolders(view);
		instance.setListener(listener);
		return instance;
	}

	private Listener mListener;

	private View mTitlebar;
	private ImageView mIcon;
	private TextView mTitle;
	private View mBtnResize;
	private View mBtnMinimize;
	private View mBtnMaximize;
	private View mBtnClose;

	public void setEnableButtonMaximize(boolean enabled) {
		mBtnMaximize.setEnabled(enabled);
	}

	public void setEnableButtonMinimize(boolean enabled) {
		mBtnMinimize.setEnabled(enabled);
	}

	public void setIcon(Drawable drawable) {
		if (mIcon != null) {
			mIcon.setImageDrawable(drawable);
		}
	}

	private void setListener(Listener listener) {
		mListener = listener;
	}

	public void setTitle(CharSequence text) {
		if (mTitle != null) {
			mTitle.setText(text);
		}
	}

	public void setTitlebarTouchListener(View.OnTouchListener touchListener) {
		if (mTitlebar != null) {
			mTitlebar.setOnTouchListener(touchListener);
		}
	}

	private void setupHolders(View view) {
		mTitlebar = (view.getId() == R.id.titlebar)
				? view
				: view.findViewById(R.id.titlebar);

		mIcon = (ImageView) view.findViewById(R.id.icon);
		mTitle = (TextView) view.findViewById(R.id.title);

		// these buttons shouldn't be null
		mBtnResize = view.findViewById(R.id.btnResize);
		mBtnMinimize = view.findViewById(R.id.btnMinimize);
		mBtnMaximize = view.findViewById(R.id.btnMaximize);
		mBtnClose = view.findViewById(R.id.btnClose);

		mBtnResize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mListener != null) {
					mListener.onClickResize(view);
				}
			}
		});

		mBtnMinimize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mListener != null) {
					mListener.onClickMinimize(view);
				}
			}
		});

		mBtnMaximize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mListener != null) {
					mListener.onClickMaximize(view);
				}
			}
		});

		mBtnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mListener != null) {
					mListener.onClickClose(view);
				}
			}
		});
	}
}
