package th.pd.glry.image;

import android.graphics.Bitmap;

public class Util {

    /**
     * @return the max scale that LE 1f and thus display the *entire* bitmap
     */
    public static float findBaseScale(Bitmap bitmap,
            int hostWidth, int hostHeight) {
        assert bitmap != null;
        assert hostWidth > 0 && hostHeight > 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > hostWidth || height > hostHeight) {
            return Math.min(1f * hostWidth / width,
                    1f * hostHeight / height);
        } else {
            return 1f;
        }
    }

    public static void init(ImageFrame frame, Bitmap bitmap,
            int hostWidth, int hostHeight) {
        assert hostWidth > 0 && hostHeight > 0;
        frame.init(bitmap);
        if (bitmap != null) {
            frame.setScale(Util.findBaseScale(bitmap, hostWidth, hostHeight));
            Util.moveToCenter(frame, hostWidth, hostHeight);
        }
    }

    public static void moveToCenter(ImageFrame frame,
            int hostWidth, int hostHeight) {
        frame.moveTo((hostWidth - frame.getRect().width()) / 2,
                (hostHeight - frame.getRect().height()) / 2);
    }
}
