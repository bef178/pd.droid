package t.typedef.droid.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import t.typedef.droid.R;

public class CircularProgressbar extends View {

    private int mBgColor;
    private int mFgColor;

    private float mInnerRadius = 0;
    private float mOuterRadius = 0;
    private int mThickness = 0;

    private int mBorderColor = 0;
    private int mBorderWidth = 0;

    private float mStartDegree = -90;
    private float mSweptDegree = 270;

    /**
     * min value(included), total value, current value
     */
    private float mValueTo;
    private float mValue;

    private RectF mOval = new RectF();

    private Paint mPaint;
    private Paint mFillPaint;

    public CircularProgressbar(Context context, AttributeSet attrs) {
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

        mBgColor = typedArray.getColor(
                R.styleable.progressbar_bgColor, 0xFFFFFFFF);
        mFgColor = typedArray.getColor(
                R.styleable.progressbar_fgColor, 0xFF00FF00);

        mInnerRadius = typedArray.getDimensionPixelOffset(
                R.styleable.progressbar_innerRadius, 40);
        mOuterRadius = typedArray.getDimensionPixelOffset(
                R.styleable.progressbar_outerRadius, 42);
        mThickness = (int) (mOuterRadius - mInnerRadius);

        mBorderColor = typedArray.getColor(
                R.styleable.progressbar_borderColor, 0);
        mBorderWidth = typedArray.getDimensionPixelSize(
                R.styleable.progressbar_borderWidth, 0);

        mStartDegree = typedArray.getFloat(
                R.styleable.progressbar_startDegree, -90f);
        mSweptDegree = typedArray.getFloat(
                R.styleable.progressbar_sweptDegree, 360f);

        mValueTo = typedArray.getFloat(R.styleable.progressbar_valueTo, 1f);
        mValue = typedArray.getFloat(R.styleable.progressbar_value, 0f);

        typedArray.recycle();
    }

    public float getValue() {
        return mValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = getMeasuredWidth() / 2;
        int y = getMeasuredHeight() / 2;
        float radius = (mOuterRadius + mInnerRadius) / 2;

        mOval.set(x - radius, y - radius, x + radius, y + radius);

        // draw arc background
        if (Color.alpha(mBgColor) != 0x00) {
            mPaint.setColor(mBgColor);
            mPaint.setStrokeWidth(mThickness);
            canvas.drawArc(mOval, mStartDegree, mSweptDegree, false, mPaint);
        }

        // draw foreground
        if (Color.alpha(mFgColor) != 0x00) {
            mFillPaint.setColor(mFgColor);
            mFillPaint.setStrokeCap(Paint.Cap.BUTT);
            mFillPaint.setStrokeWidth(mThickness);
            canvas.drawArc(mOval, mStartDegree, mValue / mValueTo
                    * mSweptDegree, false, mFillPaint);
        }

        // draw border
        if (mBorderWidth > 0 && Color.alpha(mBorderColor) != 0x00) {
            mPaint.setStrokeWidth(mBorderWidth);
            mPaint.setColor(mBorderColor);
            // inner border
            radius = mInnerRadius - mBorderWidth / 2;
            mOval.set(x - radius, y - radius, x + radius, y + radius);
            canvas.drawArc(mOval, mStartDegree, mSweptDegree, false, mPaint);
            // outer border
            radius = mOuterRadius + mBorderWidth / 2;
            mOval.set(x - radius, y - radius, x + radius, y + radius);
            canvas.drawArc(mOval, mStartDegree, mSweptDegree, false, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void rewind() {
        setValue(0);
    }

    public void setValue(float value) {
        if (value < 0) {
            value = 0;
        }
        mValue = value;
        invalidate();
    }
}
