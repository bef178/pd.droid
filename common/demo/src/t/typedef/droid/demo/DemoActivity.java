package t.typedef.droid.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        showDensity((TextView) findViewById(R.id.densityText));
        showNetworkInterfaceStat((TextView) findViewById(R.id.networkInterfaceStatText));
        setupPasswordView(R.id.editPassword, R.id.actionShowPassword, -1);
    }

    private void setEnabledVisiblePassword(boolean visible, EditText edit) {
        int selStart = edit.getSelectionStart();
        int selEnd = edit.getSelectionEnd();
        edit.setInputType(InputType.TYPE_CLASS_TEXT
                | (visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_TEXT_VARIATION_PASSWORD));
        edit.setSelection(selStart, selEnd);
    }

    private void setupPasswordView(int resEdit, int resCheckable,
            final int action) {
        final EditText edit = (EditText) findViewById(resEdit);
        edit.setTypeface(Typeface.MONOSPACE);
        edit.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                            KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (action >= 0) {
                                View target = findViewById(action);
                                if (target != null) {
                                    target.performClick();
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });

        findViewById(resCheckable).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Checkable checkable = (Checkable) v;
                        setEnabledVisiblePassword(!checkable.isChecked(),
                                edit);
                        checkable.setChecked(!checkable.isChecked());
                    }
                });
    }

    private void showDensity(TextView densityText) {
        if (densityText != null) {
            float density = getResources().getDisplayMetrics().density;
            densityText.setText("density: " + density);
        }
    }

    private void showNetworkInterfaceStat(TextView connText) {
        if (connText != null) {
            connText.setTypeface(Typeface.MONOSPACE);
            try {
                connText.setText("networkInterfaceStat: "
                        + sumUpNetworkInterfaceStat(this).toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject sumUpNetworkInterfaceStat(Context context)
            throws JSONException {
        ConnectivityManager connManger = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        JSONObject stat = new JSONObject();

        JSONObject active = toJson(connManger
                .getActiveNetworkInfo());
        active.put("metered", connManger.isActiveNetworkMetered());
        stat.put("active", active);

        Network[] networks = connManger.getAllNetworks();
        if (networks != null) {
            JSONArray list = new JSONArray();
            for (Network network : networks) {
                NetworkInfo networkInfo = connManger.getNetworkInfo(network);
                list.put(toJson(networkInfo));
            }
            stat.put("all", list);
        }

        JSONArray typeList = new JSONArray();
        for (int type : new Integer[] {
                ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI,
                ConnectivityManager.TYPE_ETHERNET
        }) {
            NetworkInfo networkInfo = connManger.getNetworkInfo(type);
            if (networkInfo != null) {
                if (networkInfo.isAvailable()) {
                    typeList.put(toJson(networkInfo));
                }
            }
        }
        return stat;
    }

    private JSONObject toJson(NetworkInfo networkInfo)
            throws JSONException {
        JSONObject stat = new JSONObject();
        if (networkInfo != null) {
            stat.put("state", networkInfo.getState().name());
            stat.put("type", networkInfo.getTypeName());
            stat.put("subtype", networkInfo.getSubtypeName());
            stat.put("available", networkInfo.isAvailable());
        }
        return stat;
    }
}
