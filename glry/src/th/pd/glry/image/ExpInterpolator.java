package th.pd.glry.image;

import android.view.animation.Interpolator;

public class ExpInterpolator implements Interpolator {

    private final double exp;
    private final double invExp;

    public ExpInterpolator() {
        this(0.5);
    }

    public ExpInterpolator(double e) {
        this.exp = e;
        this.invExp = 1 / exp;
    }

    private static double getInterpolated(float x, double exp) {
        return Math.pow(x, exp);
    }

    public float getInversed(float y) {
        return (float) (Math.pow(y, invExp));
    }

    @Override
    public float getInterpolation(float x) {
        return (float) getInterpolated(x, exp);
    }
}
