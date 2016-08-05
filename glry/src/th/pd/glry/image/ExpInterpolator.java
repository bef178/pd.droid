package th.pd.glry.image;

import t.typedef.droid.InvertibleInterpolator;

public class ExpInterpolator implements InvertibleInterpolator {

    private static double getInterpolated(float x, double exp) {
        return Math.pow(x, exp);
    }

    private final double exp;

    private final double invExp;

    public ExpInterpolator() {
        this(0.5);
    }

    public ExpInterpolator(double e) {
        this.exp = e;
        this.invExp = 1 / exp;
    }

    @Override
    public float getInterpolation(float x) {
        return (float) getInterpolated(x, exp);
    }

    @Override
    public float getInvertedInterpolation(float y) {
        return (float) (Math.pow(y, invExp));
    }
}
