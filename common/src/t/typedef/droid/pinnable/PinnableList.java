package t.typedef.droid.pinnable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PinnableList extends ListView {

    public static interface Callback {

        // TODO may not work
        public boolean onDispatchTouchEventToPinned(View mPinned,
                MotionEvent ev);
    }

    private PinnableListAdapter mAdapter;
    private Callback mCallback;

    private View mPinned;
    private float mPinnedOffsetY = 0;
    private int mMeasureModeForWidth;

    public PinnableList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinnableList(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                if (mAdapter == null) {
                    return;
                }

                int lastPinnableItemPosition = view
                        .getFirstVisiblePosition();
                while (lastPinnableItemPosition >= 0) {
                    Pinnable item = mAdapter
                            .getItem(lastPinnableItemPosition);
                    if (item.isPinnable()) {
                        break;
                    }
                    --lastPinnableItemPosition;
                }
                mPinned = mAdapter.getView(lastPinnableItemPosition,
                        mPinned, PinnableList.this);
                if (mPinned == null) {
                    mPinnedOffsetY = 0;
                    invalidate();
                    return;
                }

                settlePinnedView(mPinned, mMeasureModeForWidth);
                mPinnedOffsetY = 0;
                for (int i = 0; i < visibleItemCount; ++i) {
                    if (mAdapter.getItem(firstVisibleItem + i).isPinnable()) {
                        View pinnable = getChildAt(i);
                        float pinnableTop = pinnable.getTop();
                        float pinnedHeight = mPinned.getMeasuredHeight();
                        pinnable.setVisibility(VISIBLE);
                        if (pinnableTop >= 0 && pinnableTop <= pinnedHeight) {
                            mPinnedOffsetY = pinnableTop - pinnedHeight;
                        }
                    }
                }

                invalidate();
            }

            @Override
            public void onScrollStateChanged(AbsListView view,
                    int scrollState) {
                // dummy
            }
        });
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mAdapter == null || mPinned == null) {
            return;
        }
        int saved = canvas.save();
        canvas.translate(0, mPinnedOffsetY);
        canvas.clipRect(0, 0, getWidth(), mPinned.getMeasuredHeight());
        mPinned.draw(canvas);
        canvas.restoreToCount(saved);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mPinned != null) {
            Rect localVisible = new Rect();
            mPinned.getLocalVisibleRect(localVisible);
            if (localVisible.contains((int) ev.getX(), (int) ev.getY())) {
                if (mCallback != null) {
                    return mCallback.onDispatchTouchEventToPinned(mPinned,
                            ev);
                } else {
                    return mPinned.dispatchTouchEvent(ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureModeForWidth = MeasureSpec.getMode(widthMeasureSpec);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof PinnableListAdapter) {
            mAdapter = (PinnableListAdapter) adapter;
            super.setAdapter(adapter);
            return;
        }
        throw new IllegalArgumentException(
                PinnableListAdapter.class.getSimpleName()
                        + " required");
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    private void settlePinnedView(View pinned, int measureModeForWidth) {
        if (pinned != null && pinned.isLayoutRequested()) {
            int specWidth = MeasureSpec.makeMeasureSpec(
                    getMeasuredWidth(), measureModeForWidth);

            ViewGroup.LayoutParams lp = pinned.getLayoutParams();
            int specHeight = (lp != null && lp.height > 0)
                    ? MeasureSpec.makeMeasureSpec(
                            lp.height, MeasureSpec.EXACTLY)
                    : MeasureSpec.makeMeasureSpec(
                            0, MeasureSpec.UNSPECIFIED);
            pinned.measure(specWidth, specHeight);
            pinned.layout(0, 0, pinned.getMeasuredWidth(),
                    pinned.getMeasuredHeight());
        }
    }
}
