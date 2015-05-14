package th.pd.mail.tidyface.compose;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import th.pd.mail.R;
import th.pd.mail.tidyface.TitlebarController;

public class Hedwig extends Fragment implements
		TitlebarController.Listener, ComposeController.Listener {

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
	public boolean onCleanExit() {
		this.getActivity().finish();
		return true;
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
}
