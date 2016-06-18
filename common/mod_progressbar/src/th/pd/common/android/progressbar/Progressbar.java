package th.pd.common.android.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import th.pd.common.android.R;

public class Progressbar extends View {

    public interface ProgressChangeListener {

        public void onChange(float value);

        public void onComplete(float value);
    }

    /**
     * foreground color, background color
     */
    private int[] mColors;

    private float mArcInnerRadius = 0;
    private float mArcOuterRadius = 0;
    private int mArcWidth = 0;

    private int mBorderColor = 0;
    private int mBorderWidth = 0;

    /**
     * start degree(included), total degrees
     */
    private float[] mDegrees;

    /**
     * min value(included), total value, current value
     */
    private float[] mValues;

    private RectF mOval = new RectF();

    private Paint mPaint;
    private Paint mFillPaint;

    private ProgressChangeListener mProgressChangeListener = null;

    public Progressbar(Context context, AttributeSet attrs) {
        super(context, attrs);

        fetchAttributes(attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.STROKE);
        mFillPaint.setDither(true);
        mFillPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    private void fetchAttributes(AttributeSet attrs) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(
                attrs, R.styleable.progressbar);

        mColors = new int[2];
        mColors[0] = typedArray.getColor(
                R.styleable.progressbar_foreground, 0xFF00FF00);
        mColors[1] = typedArray.getColor(
                R.styleable.progressbar_background, 0xFFFFFFFF);

        mArcInnerRadius = typedArray.getDimensionPixelOffset(
                R.styleable.progressbar_innerRadius, 40);
        mArcOuterRadius = typedArray.getDimensionPixelOffset(
                R.styleable.progressbar_outerRadius, 42);
        mArcWidth = (int) (mArcOuterRadius - mArcInnerRadius);

        mBorderColor = typedArray.getColor(
                R.styleable.progressbar_borderColor, 0);
        mBorderWidth = typedArray.getDimensionPixelSize(
                R.styleable.progressbar_borderWidth, 0);

        mDegrees = new float[2];
        mDegrees[0] = typedArray.getFloat(
                R.styleable.progressbar_degreeFrom, -90f);
        mDegrees[1] = typedArray.getFloat(
                R.styleable.progressbar_degreeTo, 360f);

        mValues = new float[3];
        mValues[0] = typedArray.getFloat(
                R.styleable.progressbar_valueFrom, 0f);
        mValues[1] = typedArray.getFloat(
                R.styleable.progressbar_valueTo, 1f);
        mValues[2] = typedArray.getFloat(
                R.styleable.progressbar_value, 0f);

        typedArray.recycle();
    }

    public float getValue() {
        return mValues[2];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = getMeasuredWidth() / 2;
        int y = getMeasuredHeight() / 2;
        float radius = (mArcOuterRadius + mArcInnerRadius) / 2;

        mOval.set(x - radius, y - radius, x + radius, y + radius);

        // draw arc background
        if (Color.alpha(mColors[0]) != 0x00) {
            mPaint.setColor(mColors[1]);
            mPaint.setStrokeWidth(mArcWidth);
            canvas.drawArc(mOval, mDegrees[0], mDegrees[1] - mDegrees[0],
                    false, mPaint);
        }

        // draw foreground
        if (Color.alpha(mColors[0]) != 0x00) {
            mFillPaint.setColor(mColors[0]);
            mFillPaint.setStrokeCap(Paint.Cap.BUTT);
            mFillPaint.setStrokeWidth(mArcWidth);
            canvas.drawArc(
                    mOval,
                    mDegrees[0],
                    ((mValues[2] - mValues[0]) / mValues[1]) * mDegrees[1],
                    false, mFillPaint);
        }

        // draw border
        if (mBorderWidth > 0 && Color.alpha(mBorderColor) != 0x00) {
            mPaint.setStrokeWidth(mBorderWidth);
            mPaint.setColor(mBorderColor);
            // inner border
            radius = mArcInnerRadius - mBorderWidth / 2;
            mOval.set(x - radius, y - radius, x + radius, y + radius);
            canvas.drawArc(mOval, mDegrees[0], mDegrees[1], false, mPaint);
            // outer border
            radius = mArcOuterRadius + mBorderWidth / 2;
            mOval.set(x - radius, y - radius, x + radius, y + radius);
            canvas.drawArc(mOval, mDegrees[0], mDegrees[1], false, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void rewind() {
        mValues[2] = mValues[0];
        invalidate();
    }

    public void setListener(ProgressChangeListener listener) {
        mProgressChangeListener = listener;
    }

    public void setValue(float value) {
        if (value < mValues[0]) {
            value = mValues[0];
        }
        mValues[2] = value;
        invalidate();
        if (mProgressChangeListener != null) {
            if (mValues[2] >= mValues[1]) {
                mProgressChangeListener.onComplete(value);
            } else {
                mProgressChangeListener.onChange(value);
            }
        }
    }
}
