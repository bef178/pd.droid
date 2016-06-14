package th.pd.common.android;

import android.app.Activity;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;

public class SystemUiUtil {

    public static void findScreenResolution(Activity a, int[] result) {
        Display defDisplay = a.getWindowManager().getDefaultDisplay();
        Rect r = new Rect();
        defDisplay.getRectSize(r);
        result[0] = r.width();
        result[1] = r.height();
    }

    private static int flagsHideAllSystemUi() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    public static void hideSystemUi(View view) {
        int flags = view.getSystemUiVisibility()
                | flagsHideAllSystemUi();
        view.setSystemUiVisibility(flags);
    }

    public static void showSystemUi(View view) {
        int flags = view.getSystemUiVisibility()
                & ~flagsHideAllSystemUi();
        view.setSystemUiVisibility(flags);
    }
}
