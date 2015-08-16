package th.pd.mail.tidyface.compose;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import th.pd.common.android.DoubleClickListener;
import th.pd.common.android.titlebar.TitlebarController;
import th.pd.common.android.titlebar.TitlebarDragListener;
import th.pd.mail.R;

public class ComposeActivity extends Activity implements
        TitlebarController.Listener {

    private boolean isOutOfBounds(Context context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context)
                .getScaledWindowTouchSlop();
        final View decorView = getWindow().getDecorView();
        return (x < -slop) || (y < -slop)
                || (x > (decorView.getWidth() + slop))
                || (y > (decorView.getHeight() + slop));
    }

    @Override
    public void onClickClose(View btnClose) {
        finish();
    }

    @Override
    public void onClickMaximize(View btnMaximize) {
        Toast.makeText(this, "max", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickMinimize(View btnMinimize) {
        this.moveTaskToBack(true);
    }

    @Override
    public void onClickResize(View btnResize) {
        Toast.makeText(this, "resize", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        TitlebarController.newInstance(findViewById(android.R.id.content),
                this)
                .setTitlebarTouchListener(
                        new View.OnTouchListener() {

                            private DoubleClickListener mDoubleClickListener =
                                    new DoubleClickListener() {

                                        @Override
                                        public void onDoubleClick() {
                                            onClickMaximize(null);
                                        }
                                    };
                            private TitlebarDragListener mMoveListener = new TitlebarDragListener(
                                    ComposeActivity.this,
                                    new int[] {
                                            getResources()
                                                    .getDimensionPixelOffset(
                                                            R.dimen.compose_titlebar_move_margin_top),
                                            getResources()
                                                    .getDimensionPixelOffset(
                                                            R.dimen.compose_titlebar_move_margin_right),
                                            getResources()
                                                    .getDimensionPixelOffset(
                                                            R.dimen.compose_titlebar_move_margin_bottom),
                                            getResources()
                                                    .getDimensionPixelOffset(
                                                            R.dimen.compose_titlebar_move_margin_left)
                                    });

                            @Override
                            public boolean onTouch(View view,
                                    MotionEvent event) {
                                boolean handled = false;
                                handled = mDoubleClickListener.onTouch(
                                        view, event)
                                        | handled;
                                handled = mMoveListener
                                        .onTouch(view, event)
                                        | handled;
                                return handled;
                            }
                        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isOutOfBounds(this, event)) {
            moveTaskToBack(true);
            return true;
        }
        return super.onTouchEvent(event);
    }
}
