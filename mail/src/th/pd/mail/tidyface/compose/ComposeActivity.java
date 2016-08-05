package th.pd.mail.tidyface.compose;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import t.typedef.droid.win98.Win98TitlebarActivity;
import th.pd.mail.R;

public class ComposeActivity extends Win98TitlebarActivity {

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
    public void onClose() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        setTitlebarDragMargin(new int[] {
                getResources().getDimensionPixelOffset(
                        R.dimen.compose_titlebar_move_margin_top),
                getResources().getDimensionPixelOffset(
                        R.dimen.compose_titlebar_move_margin_right),
                getResources().getDimensionPixelOffset(
                        R.dimen.compose_titlebar_move_margin_bottom),
                getResources().getDimensionPixelOffset(
                        R.dimen.compose_titlebar_move_margin_left),
        });
    }

    @Override
    public void onMaximize() {
        Toast.makeText(this, "max", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMinimize() {
        moveTaskToBack(true);
    }

    @Override
    public void onNew() {
        Toast.makeText(this, "new", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onRestore() {
        Toast.makeText(this, "restore", Toast.LENGTH_SHORT).show();
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
