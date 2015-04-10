package th.melody;

import th.pd.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onAction(item.getItemId())
                || super.onOptionsItemSelected(item);
    }

}
