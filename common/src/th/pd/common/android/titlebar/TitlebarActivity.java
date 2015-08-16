package th.pd.common.android.titlebar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import th.pd.common.android.R;

public abstract class TitlebarActivity extends Activity implements
        TitlebarController.ActionListener {

    private TitlebarController mTitlebarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.titlebar_activity);

        mTitlebarController = new TitlebarController(getWindow(), null,
                this, findViewById(R.id.titlebar));
    }

    @Override
    public void setContentView(int layoutResId) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.titlebar_activity_content);
        View.inflate(this, layoutResId, viewGroup);
    }

    public void setTitlebarDragMargin(int[] dragMargin) {
        mTitlebarController.setDragMargin(dragMargin);
    }
}
