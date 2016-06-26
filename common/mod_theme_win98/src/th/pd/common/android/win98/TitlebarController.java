package th.pd.common.android.win98;

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

    public interface Callback {

        void onClose();

        void onMaximize();

        void onMinimize();

        void onNew();

        void onRestore();
    }

    public static final int CLOSE = 0;

    private Callback mCallback;
    private TitlebarDragListener mDragListener;

    private View mTitlebar;
    private View mMaximize;
    private View mRestore;

    public TitlebarController(Window window, int[] dragMargin,
            Callback callback, View view) {
        mDragListener = new TitlebarDragListener(window, dragMargin);
        mCallback = callback;
        setupViews(view);
    }

    public void setDragMargin(int[] dragMargin) {
        mDragListener.setDragMargin(dragMargin);
    }

    public void setIcon(Drawable drawable) {
        ImageView icon = (ImageView) mTitlebar
                .findViewById(android.R.id.icon);
        if (icon != null) {
            icon.setImageDrawable(drawable);
        }
    }

    private void setState(View view, boolean enabled, boolean visible) {
        view.setEnabled(enabled);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setStateForClose(boolean enabled, boolean visible) {
        setState(mTitlebar.findViewById(R.id.actionClose),
                enabled, visible);
    }

    public void setStateForMaximize(boolean enabled, boolean visible) {
        setState(mMaximize, enabled, visible);
    }

    public void setStateForMinimize(boolean enabled, boolean visible) {
        setState(mTitlebar.findViewById(R.id.actionMinimize),
                enabled, visible);
    }

    public void setStateForNew(boolean enabled, boolean visible) {
        setState(mTitlebar.findViewById(R.id.actionNew),
                enabled, visible);
    }

    public void setStateForRestore(boolean enabled, boolean visible) {
        setState(mRestore, enabled, visible);
    }

    public void setTitle(CharSequence text) {
        TextView title = (TextView) mTitlebar
                .findViewById(android.R.id.title);
        if (title != null) {
            title.setText(text);
        }
    }

    private void setupViews(View view) {
        mTitlebar = (view.getId() == R.id.titlebar)
                ? view
                : view.findViewById(R.id.titlebar);
        mTitlebar.setOnTouchListener(new View.OnTouchListener() {

            private DoubleClickListener mDoubleClickListener =
                    new DoubleClickListener() {

                        @Override
                        public void onDoubleClick(View view) {
                            if (mCallback != null) {
                                mCallback.onMaximize();
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

        mTitlebar.findViewById(R.id.actionMinimize).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mCallback != null) {
                            mCallback.onMinimize();
                        }
                    }
                });

        mMaximize = mTitlebar.findViewById(R.id.actionMaximize);
        mMaximize.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mCallback != null) {
                            mCallback.onMaximize();
                        }
                    }
                });

        mRestore = mTitlebar.findViewById(R.id.actionRestore);
        mRestore.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mCallback != null) {
                            mCallback.onRestore();
                        }
                    }
                });

        mTitlebar.findViewById(R.id.actionClose).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mCallback != null) {
                            mCallback.onClose();
                        }
                    }
                });
    }
}
