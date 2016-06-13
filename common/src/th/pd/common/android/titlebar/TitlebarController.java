package th.pd.common.android.titlebar;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import th.pd.common.android.DoubleClickListener;
import th.pd.common.android.R;

public class TitlebarController {

    public interface ActionListener {

        void onClickClose(View btnClose);

        void onClickMaximize(View btnMaximize);

        void onClickMinimize(View btnMinimize);

        void onClickResize(View btnResize);

        void onDoubleClick(View titlebar);
    }

    private ActionListener mActionListener;
    private TitlebarDragListener mDragListener;

    private View mTitlebar;

    private ImageView mIcon;
    private TextView mTitle;
    private View mBtnResize;
    private View mBtnMinimize;
    private View mBtnMaximize;
    private View mBtnClose;

    public TitlebarController(Window window, int[] dragMargin,
            ActionListener actionListener, View view) {
        mDragListener = new TitlebarDragListener(window, dragMargin);
        mActionListener = actionListener;
        bindViews(view);
    }

    private void bindViews(View view) {
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

        mTitlebar.setOnTouchListener(new View.OnTouchListener() {

            private DoubleClickListener mDoubleClickListener =
                    new DoubleClickListener() {

                        @Override
                        public void onDoubleClick(View view) {
                            if (mActionListener != null) {
                                mActionListener.onDoubleClick(view);
                            }
                        }
                    };

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                boolean handled = false;
                handled = mDoubleClickListener.onTouch(view, event)
                        || handled;
                handled = mDragListener.onTouch(view, event)
                        || handled;
                return handled;
            }
        });

        mBtnResize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mActionListener != null) {
                    mActionListener.onClickResize(view);
                }
            }
        });

        mBtnMinimize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mActionListener != null) {
                    mActionListener.onClickMinimize(view);
                }
            }
        });

        mBtnMaximize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mActionListener != null) {
                    mActionListener.onClickMaximize(view);
                }
            }
        });

        mBtnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mActionListener != null) {
                    mActionListener.onClickClose(view);
                }
            }
        });
    }

    public void setEnableButtonMaximize(boolean enabled) {
        mBtnMaximize.setEnabled(enabled);
    }

    public void setEnableButtonMinimize(boolean enabled) {
        mBtnMinimize.setEnabled(enabled);
    }

    public void setDragMargin(int[] dragMargin) {
        mDragListener.setDragMargin(dragMargin);
    }

    public void setIcon(Drawable drawable) {
        if (mIcon != null) {
            mIcon.setImageDrawable(drawable);
        }
    }

    public void setTitle(CharSequence text) {
        if (mTitle != null) {
            mTitle.setText(text);
        }
    }
}
