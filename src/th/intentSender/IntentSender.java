package th.intentSender;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import th.pd.R;

/**
 * a tester to send kinds of intents, may with data
 *
 * @author tanghao
 */
public class IntentSender {

    private static void appendLogEntry(CharSequence tag, CharSequence s,
            StringBuilder sb) {
        sb.append('\t').append(tag).append(':').append(s).append(";\n");
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException e1) {
                throw e1;
            } catch (Exception e2) {
                // dummy
            }
        }
    }

    private static byte[] getByteArray(InputStream stream)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int byteCount = 0;
        while ((byteCount = stream.read(buffer)) != -1) {
            bytes.write(buffer, 0, byteCount);
        }
        return bytes.toByteArray();
    }

    private static final String LOG_TAG = IntentSender.class
            .getSimpleName();

    private TextView mTextLog;

    private View mView;

    final int REQUEST_CODE = 42;

    public IntentSender(View intentSenderView, TextView textLog) {
        mTextLog = textLog;
        mView = intentSenderView;

        initSpinnerIntentAction();
        initSpinnerIntentType();

        mView.findViewById(R.id.btnSendIntent).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        sendIntent();
                    }
                });

        mView.findViewById(R.id.btnSendNotification).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        sendNotification();
                    }
                });
    }

    private void initSpinnerIntentAction() {
        Spinner spnIntentAction = (Spinner) mView
                .findViewById(R.id.spnIntentAction);

        final IntentActionAdapter adapter = new IntentActionAdapter(
                mView.getContext());
        spnIntentAction.setAdapter(adapter);

        spnIntentAction.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                            View view, int position, long id) {
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    private void initSpinnerIntentType() {
        Spinner spnIntentType = (Spinner) mView
                .findViewById(R.id.spnIntentType);

        final IntentTypeAdapter adapter = new IntentTypeAdapter(
                mView.getContext());
        spnIntentType.setAdapter(adapter);

        spnIntentType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                            View view, int position, long id) {
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    private void logIntent(Intent intent) {
        println(LOG_TAG + ":\n\taction:" + intent.getAction()
                + ";\n\ttype:"
                + intent.getType() + ";");
    }

    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        StringBuilder sb = new StringBuilder();

        appendLogEntry("requestCode", Integer.toString(requestCode), sb);
        appendLogEntry("resultCode", Integer.toString(resultCode), sb);

        if (data == null) {
            appendLogEntry("data", "(null)", sb);
        } else {
            appendLogEntry("data", data.toString(), sb);

            Uri uri = data.getData();

            Context context = mView.getContext();

            try {
                context.getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                sb.append("failed to take ").append(e).append(";\n");
            }

            InputStream stream = null;
            try {
                stream = context.getContentResolver().openInputStream(uri);
                final int length = getByteArray(stream).length;
                appendLogEntry("readLength", Integer.toString(length), sb);
            } catch (Exception e) {
                sb.append("failed to read ").append(uri).append(";\n")
                        .append(e).append('\n');
            } finally {
                closeQuietly(stream);
            }
        }

        println(sb.toString());
    }

    private void println(CharSequence cs) {
        StringBuilder sb = new StringBuilder();
        sb.append(mTextLog.getText()).append(cs);
        mTextLog.setText(sb.toString());
    }

    private void sendIntent() {
        String action = ((IntentActionItem) ((Spinner) mView
                .findViewById(R.id.spnIntentAction)).getSelectedItem()).action;
        String type = ((IntentTypeItem) ((Spinner) mView
                .findViewById(R.id.spnIntentType)).getSelectedItem()).type;

        Intent intent = new Intent(action).setType(type);
        if (action.equals(Intent.ACTION_CREATE_DOCUMENT)) {
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "pd.txt");
        } else if (action.equals(IntentActionAdapter.ACTION_MANAGE_ROOT)) {
            Uri uri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority("com.android.externalstorage.documents")
                    .appendPath("root")
                    .appendPath("primary").build();
            intent.setData(uri);
        }

        CheckBox checkExtraAllowMultiple = (CheckBox) mView
                .findViewById(R.id.checkExtraAllowMultiple);
        CheckBox checkExtraLocalOnly = (CheckBox) mView
                .findViewById(R.id.checkExtraLocalOnly);
        CheckBox checkExtraHighlight = (CheckBox) mView
                .findViewById(R.id.checkExtraHighlight);

        if (checkExtraAllowMultiple.isChecked()) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        if (checkExtraLocalOnly.isChecked()) {
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }

        if (checkExtraHighlight.isChecked()) {
            intent.putExtra("android.content.extra.EXTRA_HIGHLIGHT_PATH",
                    "file:///storage/emulated/0/Documents/a.pdf");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        logIntent(intent);

        ((Activity) mView.getContext()).startActivityForResult(
                Intent.createChooser(intent, "chooser title"),
                REQUEST_CODE);
    }

    private void sendNotification() {
        int id = 71823;
        Context context = mView.getContext();

        Notification notification = new Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setTicker("click to open Settings")
                .setSmallIcon(R.drawable.mime_generic_file)
                .setContentTitle("contentTitle")
                .setContentText("contentText")
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent("android.settings.SETTINGS"),
                        Intent.FLAG_ACTIVITY_NEW_TASK))
                .build();

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
        mNotificationManager.notify(id, notification);
    }
}
