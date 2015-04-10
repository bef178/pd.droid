package th.mock;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;

import th.pd.ViewStateController;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * We want to keep an "as-if-focused" state in both TouchMode/non-TouchMode.
 * This state would request appearance change and answer both
 * MotionEvent/KeyEvent. We also want a not-so-highlight state to indicate the
 * navigator is chosen but doesn't gain focus.<br/>
 * <br/>
 * But we have no suitable state to use from ListView's item. We cannot just use
 * "selected" state because (1) there's a little delay between the state is set
 * and it appears (2) ListView will set its all children's "selected" state.
 * Those causes color flickering and unpredictable final color.<br/>
 * <br/>
 * So, we take off the control from ListView.<br/>
 * Hope a better solution.
 *
 * @author tanghao
 */
public class StateListView extends ListView {
    private static final int FLAG_INDEX_AS_FOCUSED = 1;

    private static int[] generateState(int[] a, int[] b, boolean isAdd) {
        if (a == null) {
            if (b == null) {
                return new int[0];
            } else {
                return b;
            }
        } else if (b == null) {
            return a;
        }

        HashSet<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < a.length; ++i) {
            set.add(a[i]);
        }
        for (int i = 0; i < b.length; ++i) {
            if (isAdd) {
                if (set.contains(-b[i])) {
                    set.remove(-b[i]);
                }
                set.add(b[i]);
            } else {
                if (set.contains(b[i])) {
                    set.remove(b[i]);
                }
            }
        }
        Iterator<Integer> it = set.iterator();
        int[] c = new int[set.size()];
        int i = 0;
        while (it.hasNext()) {
            c[i++] = it.next();
        }
        return c;
    }

//    private static void updateViewAppearance(View view, int[] attrState,
//            boolean isAdd) {
//        Drawable bgDrawable = view.getBackground();
//        if (bgDrawable != null && bgDrawable.isStateful()) {
//            bgDrawable.setState(generateState(bgDrawable.getState(),
//                    attrState, isAdd));
//        }
//    }

    // set when lose-focus, clear when gain-focus
    private int mAsFocusedItemPosition = ListView.INVALID_POSITION;

    private OnFocusChangeListener mExternalFocusListener = null;

    private OnItemClickListener mExternalItemClickListener = null;


    ViewStateController ct = null;

    private BitSet mStateAppearance = new BitSet(16);

    private OnFocusChangeListener mStateFocusListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean isFocused) {
            // clear the item view "selected" state
            if (v == StateListView.this) {
                if (ct != null) {
                    ct.setStateFlags(ct.STATE_FLAG_FOCUSED);

                }


//                if (isFocused) {
//                    // gain focus
//                    View itemView = getItemView(mAsFocusedItemPosition);
//                    if (itemView != null) {
//                        // clear the item selector
//                        itemView.setBackground(null);
//                        StateListView.this
//                                .setSelection(mAsFocusedItemPosition);
//                        Log.w("th", itemView.getId() + " clear bg");
//                        Log.w("th", "set selection " + mAsFocusedItemPosition);
//                        Log.w("th", "current selection " + StateListView.this.getSelectedItemPosition());
//                        mAsFocusedItemPosition = ListView.INVALID_POSITION;
//                    }
//                } else {
//                    // lose focus
//                    mAsFocusedItemPosition = getSelectedItemPosition();
//                    View itemView = getItemView(mAsFocusedItemPosition);
//                    if (itemView != null) {
//                        itemView.setBackgroundResource(R.drawable.mock_item_selector);
//                        StateListView.this.setItemSelected(itemView, true);
//                    }
//                }
            }
//                    setStateAsFocused(isFocused);
//                    flushStateAppearance();
            if (mExternalFocusListener != null) {
                mExternalFocusListener.onFocusChange(v, isFocused);
            }
        }
    };

//    private OnItemClickListener mStateItemClickListener = new OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view,
//                int position, long id) {
//            if (mExternalItemClickListener != null) {
//                mExternalItemClickListener.onItemClick(parent, view,
//                        position, id);
//            }
//            requestFocus();
//            flushStateAppearance();
//            int selectedItemPosition = StateListView.this
//                    .getSelectedItemPosition();
//            if (selectedItemPosition != AdapterView.INVALID_POSITION) {
//                View itemView = getItemView(selectedItemPosition);
//                updateViewAppearance(itemView, new int[] {
//                        android.R.attr.state_focused
//                }, true);
//            }
//        }
//    };

    public StateListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnFocusChangeListener(mStateFocusListener);
//        super.setOnItemClickListener(mStateItemClickListener);
    }

//    public void flushStateAppearance() {
//        int selectedItemPosition = StateListView.this
//                .getSelectedItemPosition();
//        if (selectedItemPosition != AdapterView.INVALID_POSITION) {
//            View itemView = getItemView(selectedItemPosition);
//            updateViewAppearance(itemView, new int[] {
//                    android.R.attr.state_focused
//            }, getStateAsFocused());
//        }
//    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
            Rect previouslyFocusedRect) {
        // TODO Auto-generated method stub
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    public View getItemView(int itemPosition) {
        int[] visibleItemRange = new int[] {
                getFirstVisiblePosition(),
                getLastVisiblePosition()
        };
        if (itemPosition >= visibleItemRange[0]
                && itemPosition <= visibleItemRange[1]) {
            int itemViewPosition = itemPosition - visibleItemRange[0];
            return getChildAt(itemViewPosition);
        }
        return null;
    }

//    public boolean getStateAsFocused() {
//        return mStateAppearance.get(FLAG_INDEX_AS_FOCUSED);
//    }

    /**
     * We break selection/selected call chain. Rejoin via this api.
     *
     * @param itemPosition
     *            the position in data adapter
     * @param isSelected
     */
    public void setItemSelected(int itemPosition, boolean isSelected) {
        setItemSelected(getItemView(itemPosition), isSelected);
    }

    public void setItemSelected(View itemView, boolean isSelected) {
        if (itemView instanceof StateListItemLayout) {
            ((StateListItemLayout) itemView).setStateSelected(isSelected);
        }
    }

//    @Override
//    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
//        mFocusListener = listener;
//    }

    /**
     * Should be invoked explicitly when gain/loss "as focused" state. Better be
     * done in a activity-scope controller, e.g. the activity.
     *
     * @param isFocused
     */
//    public void setStateAsFocused(boolean isFocused) {
//        mStateAppearance.set(FLAG_INDEX_AS_FOCUSED, isFocused);
//    }
}
