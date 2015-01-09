package th.mediaPlay;

import th.mediaPlay.MediaGestureListener.Callback;
import android.content.Context;
import android.view.GestureDetector;

public class MediaGestureDetector extends GestureDetector {

	public MediaGestureDetector(Context context, Callback callback) {
		super(context, new MediaGestureListener(callback));
	}
}
