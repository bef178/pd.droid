package t.typedef.droid.demo;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        showDensity();
        setupPasswordView(R.id.editPassword, R.id.actionShowPassword, -1);
    }

    private void showDensity() {
        TextView densityText = (TextView) findViewById(R.id.densityText);
        if (densityText != null) {
            float density = getResources().getDisplayMetrics().density;
            densityText.setText("density: " + density);
        }
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

    private void setEnabledVisiblePassword(boolean visible, EditText edit) {
        int selStart = edit.getSelectionStart();
        int selEnd = edit.getSelectionEnd();
        edit.setInputType(InputType.TYPE_CLASS_TEXT
                | (visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_TEXT_VARIATION_PASSWORD));
        edit.setSelection(selStart, selEnd);
    }
}
