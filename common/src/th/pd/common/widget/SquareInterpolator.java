package th.pd.common.widget;

import android.view.animation.Interpolator;

public class SquareInterpolator implements Interpolator {

    public static float getInterpolated(float x) {
        return x * x;
    }

    public static float getInversed(float y) {
        return (float) (Math.pow(y, 0.5));
    }

    @Override
    public float getInterpolation(float x) {
        return getInterpolated(x);
    }
}
