package th.pd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import th.demo.Demo;
import th.mock.MockFileManager;

public class Main extends Activity {

    private boolean onAction(int actionId) {
        switch (actionId) {
            case R.id.action_startDemo: {
                Intent intent = new Intent();
                intent.setClass(Main.this, Demo.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_startMockFileManager: {
                Intent intent = new Intent();
                intent.setClass(Main.this, MockFileManager.class);
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return onAction(item.getItemId())
                || super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.action_startDemo:
                    case R.id.action_startMockFileManager:
                        onAction(v.getId());
                        break;
                }
            }
        };

        findViewById(R.id.action_startDemo).setOnClickListener(
                clickListener);
        findViewById(R.id.action_startMockFileManager).setOnClickListener(
                clickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onAction(item.getItemId())
                || super.onOptionsItemSelected(item);
    }
}
