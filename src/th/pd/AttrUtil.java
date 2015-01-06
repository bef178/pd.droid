package th.pd;

public class AttrUtil {

    /**
     * usage<br/>
     * <code><pre>
     * String arcValues = typedArray.getString(
                R.styleable.ProgressArc_arcDegrees);
        float[] values = new float[] { 0f, 100f };
        parseFloats(arcDegrees, values);
        mValueStart = (int) values[0];
        mValueTotal = (int) values[1];
        </pre></code>
     */
    public boolean parseFloats(String attrs, float[] defaultsAlsoOutput) {
        if (attrs == null) {
            return false;
        }

        boolean fine = true;
        String[] args = attrs.split(",");
        for (int i = 0; i < defaultsAlsoOutput.length; ++i) {
            try {
                defaultsAlsoOutput[i] = Float.valueOf(args[i]);
            } catch (NumberFormatException e1) {
                fine = false;
            } catch (IndexOutOfBoundsException e2) {
                fine = false;
            } catch (Exception e3) {
                e3.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
        return fine;
    }
}
