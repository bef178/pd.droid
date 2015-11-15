package th.pd.mail.tidyface.compose;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import th.pd.common.android.QueryUtil;
import th.pd.mail.R;
import th.pd.mail.dao.FastSyncAccess;
import th.pd.mail.dao.SmtpSyncable;
import th.pd.mail.fastsync.Const;
import th.pd.mail.fastsync.MailServerAuth;

public class Hedwig extends Fragment implements ComposeController.Listener {

    private static final int REQUEST_CODE_PICK_FILE = 11;

    private ComposeController mComposeController;
    private View mBtnSend;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBtnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onClickSend();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_FILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Uri contentUri = data.getData();
                        final ContentResolver resolver = getActivity()
                                .getContentResolver();
                        String mimeType = resolver.getType(contentUri);
                        String displayName = QueryUtil.queryDisplayName(
                                contentUri, resolver);
                        mComposeController.addAttachment(contentUri,
                                mimeType, displayName, getActivity());
                        break;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCleanExit() {
        this.getActivity().finish();
    }

    private void onClickSend() {
        SmtpSyncable syncMessage = new SmtpSyncable();
        syncMessage.setMessage(
                mComposeController.getUpdatedModel().getMessage());
        int errorCode = processSendMessage(syncMessage, true, true);
        switch (errorCode) {
            case -2:
                Toast.makeText(getActivity(), "no sender",
                        Toast.LENGTH_SHORT).show();
                break;
            case -1:
                Toast.makeText(getActivity(), "no recipient",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        relayout();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // make sure the default window padding is cleared
        getActivity().getWindow().setBackgroundDrawable(null);

        View view = inflater.inflate(R.layout.hedwig, container, false);
        mComposeController = ComposeController.newInstance(view, this,
                FastSyncAccess.getMailboxSequence(getActivity())
                        .getCurrent().getAddr());
        mBtnSend = view.findViewById(R.id.btnSend);

        return view;
    }

    @Override
    public void onPickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent wrapperIntent = Intent.createChooser(intent,
                "Select a File to Upload");

        try {
            startActivityForResult(wrapperIntent, REQUEST_CODE_PICK_FILE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(),
                    R.string.cannot_find_app_to_pick_file,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @return an error code to tell which is incorrect
     */
    private int processSendMessage(final SmtpSyncable syncMessage,
            final boolean willCheckEmptySubject,
            final boolean willCheckEmptyContent) {
        if (!syncMessage.hasSender()) {
            // no sender
            return -2;
        }

        if (!syncMessage.hasRecipient()) {
            // no recipient
            return -1;
        }

        if (willCheckEmptySubject && !syncMessage.hasSubject()) {
            DialogInterface.OnClickListener dialogButtonListener =
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                                processSendMessage(syncMessage, false,
                                        willCheckEmptyContent);
                            }
                            dialog.dismiss();
                        }
                    };
            new AlertDialog.Builder(getActivity())
                    .setMessage("empty SUBJECT: send anyway?")
                    .setPositiveButton("send", dialogButtonListener)
                    .setNegativeButton("cancel", dialogButtonListener)
                    .create().show();
            return 0;
        }

        if (willCheckEmptyContent && !syncMessage.hasContent()) {
            DialogInterface.OnClickListener dialogButtonListener =
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                                processSendMessage(syncMessage,
                                        willCheckEmptySubject, false);
                            }
                            dialog.dismiss();
                        }
                    };
            new AlertDialog.Builder(getActivity())
                    .setMessage("empty CONTENT: send anyway?")
                    .setPositiveButton("send", dialogButtonListener)
                    .setNegativeButton("cancel", dialogButtonListener)
                    .create().show();
            return 0;
        }

        // TODO add serverAuth according to sender address
        // TODO ... and other necessary stuff
        MailServerAuth serverAuth = FastSyncAccess.findServerAuth(
                getActivity(), syncMessage.getMessage().getSender(),
                Const.PROTOCOL_SMTP);
        if (serverAuth == null) {
            // TODO make error number const
            return -3;
        }

        syncMessage.setServerAuth(serverAuth);

        if (ActivityManager.isUserAMonkey()) {
            // monkey user
            return -9;
        }
        FastSyncAccess.enqueueMessage(syncMessage);

        // successfully enqueued
        return 1;
    }

    /**
     * the screen anchor's coordinate will be changed if orientation changes,
     * thus everything based on "offset" will be invalid.
     * the screen anchor may be as:<br/>
     * <code>(width/2, (height-statusbarHeight-navigationbarHeight)/2+statusbarHeight)</code><br/>
     * but shouldn't assume anything. just put the window back to screen center.
     */
    private void relayout() {
        View decorView = getActivity().getWindow().getDecorView();
        WindowManager.LayoutParams layoutParams =
                (WindowManager.LayoutParams) decorView.getLayoutParams();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.flags |=
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        getActivity().getWindowManager().updateViewLayout(
                decorView, layoutParams);
    }
}
