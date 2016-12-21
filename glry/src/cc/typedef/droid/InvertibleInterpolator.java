package cc.typedef.droid;

import android.view.animation.Interpolator;

public interface InvertibleInterpolator extends Interpolator {

    public float getInvertedInterpolation(float y);
}
