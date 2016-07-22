package th.pd.common.android;

import android.graphics.Rect;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SystemUiUtil {

    public static int[] findScreenResolution(WindowManager m, int[] result) {
        Display defDisplay = m.getDefaultDisplay();
        Rect r = new Rect();
        defDisplay.getRectSize(r);
        if (result == null) {
            result = new int[2];
        }
        result[0] = r.width();
        result[1] = r.height();
        return result;
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
