package th.pd.mail.tidyface.compose;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import th.common.widget.TitlebarController;
import th.pd.mail.R;

public class Hedwig extends Fragment implements
		TitlebarController.Listener, ComposeController.Listener {

	private static final int REQUEST_CODE_PICK_FILE = 11;

	private ComposeController mComposeController;
	private View mBtnSend;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBtnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSendMail();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_PICK_FILE:
				switch (resultCode) {
					case Activity.RESULT_OK:
						Uri contentUri = data.getData();
						mComposeController.addAttachment(contentUri);
						break;
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClickClose(View btnClose) {
		onCleanExit();
	}

	@Override
	public void onClickMaximize(View btnMaximize) {
		mComposeController.addTab();
	}

	@Override
	public void onClickMinimize(View btnMinimize) {
		Toast.makeText(getActivity(), "min", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClickResize(View btnResize) {
		Toast.makeText(getActivity(), "resize", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCleanExit() {
		this.getActivity().finish();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hedwig, container, false);
		TitlebarController.newInstance(view, this);
		mComposeController = ComposeController.newInstance(view, this);
		mBtnSend = view.findViewById(R.id.btnSend);
		return view;
	}

	private void onSendMail() {
		ComposeModel model = mComposeController.removeCurrentTab();
		// TODO
	}

	@Override
	public void onPickFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					REQUEST_CODE_PICK_FILE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getActivity(),
					R.string.cannot_find_app_to_pick_file, Toast.LENGTH_SHORT)
					.show();
		}
	}
}
