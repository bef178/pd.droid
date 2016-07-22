package th.pd.android;

import android.view.animation.Interpolator;

public interface InvertibleInterpolator extends Interpolator {

    public float getInvertedInterpolation(float y);
}
