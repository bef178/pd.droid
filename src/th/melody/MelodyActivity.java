package th.melody;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import th.pd.R;

public class MelodyActivity extends Activity {

    private boolean onAction(int itemId) {
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (onAction(item.getItemId())) {
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.melody_main);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onAction(item.getItemId())
                || super.onOptionsItemSelected(item);
    }

}
