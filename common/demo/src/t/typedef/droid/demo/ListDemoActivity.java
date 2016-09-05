package t.typedef.droid.demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import t.typedef.droid.DataNetworkUtility;
import t.typedef.droid.OnScrollReachEndListener;

public class ListDemoActivity extends Activity {

    private class LoadItemsTask extends AsyncTask<Void, Void, Item> {

        private Mode mode = null;

        public LoadItemsTask(Mode mode) {
            this.mode = mode;
        }

        @Override
        protected Item doInBackground(Void... params) {
            // TODO read config
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // dummy
            }
            Item item = new Item();
            item.caption = "loaded";
            return item;
        }

        @Override
        protected void onPostExecute(Item result) {
            onLoadItemsEnd(mode, result);
        }
    }

    private static enum Mode {
        ADD, REPLACE;
    }

    private ListView mListView = null;
    private ItemContainerAdapter mListAdapter = null;

    private LoadItemsTask mCurrentTask = null;
    private boolean mRemoteHasMoreItems = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_demo);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnScrollListener(new OnScrollReachEndListener() {

            @Override
            public void onReachEnd() {
                ListDemoActivity.this.onLoadItemsStart(Mode.ADD);
            }
        });
        mListAdapter = new ItemContainerAdapter(this);
        mListView.setAdapter(mListAdapter);
    }

    private void onLoadItemsEnd(Mode mode, Item item) {
        if (mode != null) {
            switch (mode) {
                case ADD:
                    if (item == null) {
                        mRemoteHasMoreItems = false;
                    } else {
                        mListAdapter.addItems(item);
                        mListAdapter.notifyDataSetChanged();
                    }
                    break;
                case REPLACE:
                    if (item == null) {
                        mRemoteHasMoreItems = false;
                    } else {
                        mListAdapter.setItems(item);
                        mListAdapter.notifyDataSetChanged();
                        mRemoteHasMoreItems = true;
                    }
                default:
                    break;
            }
        }
        mCurrentTask = null;
    }

    private void onLoadItemsStart(Mode mode) {
        if (!DataNetworkUtility.isConnected(this)) {
            Toast.makeText(this, "bad network", Toast.LENGTH_SHORT).show();
        } else if (mode == null) {
            // dummy
        } else if (!mRemoteHasMoreItems) {
            // dummy
        } else if (mCurrentTask == null) {
            mCurrentTask = new LoadItemsTask(mode);
            mCurrentTask.execute();
        } else if (mode == Mode.REPLACE) {
            mCurrentTask.cancel(true);
            mCurrentTask = new LoadItemsTask(mode);
            mCurrentTask.execute();
        }
    }
}
