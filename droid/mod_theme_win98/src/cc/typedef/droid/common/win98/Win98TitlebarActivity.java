package cc.typedef.droid.common.win98;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import cc.typedef.droid.common.R;

public abstract class Win98TitlebarActivity extends Activity implements
        TitlebarController.Callback {

    private TitlebarController mTitlebarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.win98_titlebar_activity);

        mTitlebarController = new TitlebarController(getWindow(), null,
                this, findViewById(R.id.titlebar));
    }

    @Override
    public void setContentView(int layoutResId) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.placeholder);
        View.inflate(this, layoutResId, viewGroup);
    }

    public void setTitlebarDragMargin(int[] dragMargin) {
        mTitlebarController.setDragMargin(dragMargin);
    }
}
