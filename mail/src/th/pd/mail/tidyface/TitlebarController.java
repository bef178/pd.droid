package th.pd.mail.tidyface;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import th.pd.mail.R;

public class TitlebarController {
	public interface Listener {
		void onClickClose(View btnClose);

		void onClickMaximize(View btnMaximize);

		void onClickMinimize(View btnMinimize);
	}

	public static TitlebarController newInstance(View view,
			Listener listener) {
		TitlebarController instance = new TitlebarController();
		instance.setupHolders(view);
		instance.setListener(listener);
		return instance;
	}

	private Listener mListener;

	private ImageView mIcon;
	private TextView mTitle;
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

	public void setListener(Listener listener) {
		mListener = listener;
	}

	public void setTitle(CharSequence text) {
		if (mTitle != null) {
			mTitle.setText(text);
		}
	}

	private void setupHolders(View view) {
		mIcon = (ImageView) view.findViewById(R.id.icon);
		mTitle = (TextView) view.findViewById(R.id.title);

		// these 3 buttons shouldn't be null
		mBtnMinimize = view.findViewById(R.id.btnMinimize);
		mBtnMaximize = view.findViewById(R.id.btnMaximize);
		mBtnClose = view.findViewById(R.id.btnClose);

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
