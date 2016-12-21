package cc.typedef.droid.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import cc.typedef.droid.common.R;

public class AnnularProgressBar extends View {

    private int mBgColor;
    private int mFgColor;

    private float mRadius = 0;
    private int mStrokeWidth = 0;

    private int mBorderStrokeWidth = 0;
    private int mBorderColor = 0;

    private float mStartDegree = -90;
    private float mSweptDegree = 270;

    private float mValue;

    private RectF mOval = new RectF();

    private Paint mPaint;

    public AnnularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        fetchAttributes(attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void fetchAttributes(AttributeSet attrs) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(
                attrs, R.styleable.progressbar);

        mBgColor = typedArray.getColor(
                R.styleable.progressbar_bgColor, 0xFFFFFFFF);
        mFgColor = typedArray.getColor(
                R.styleable.progressbar_fgColor, 0xFF00FF00);

        mRadius = typedArray.getDimensionPixelSize(
                R.styleable.progressbar_radius, 20);
        mStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.progressbar_strokeWidth, 2);

        mBorderColor = typedArray.getColor(
                R.styleable.progressbar_borderColor, 0);
        mBorderStrokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.progressbar_borderStrokeWidth, 0);

        mStartDegree = typedArray.getFloat(
                R.styleable.progressbar_startDegree, -90f);
        mSweptDegree = typedArray.getFloat(
                R.styleable.progressbar_sweptDegree, 360f);

        mValue = typedArray.getFloat(R.styleable.progressbar_value, 0f);

        boolean isCounterClockwise = typedArray.getBoolean(
                R.styleable.progressbar_isCounterClockwise, false);
        if (isCounterClockwise) {
            mSweptDegree *= -1;
        }

        typedArray.recycle();
    }

    public float getValue() {
        return mValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = getMeasuredWidth() / 2f;
        float y = getMeasuredHeight() / 2f;

        float averageRadius = mRadius - mStrokeWidth / 2f;
        mOval.set(x, y, x, y);
        mOval.inset(-averageRadius, -averageRadius);

        // draw arc background
        if (Color.alpha(mBgColor) != 0) {
            mPaint.setColor(mBgColor);
            mPaint.setStrokeWidth(mStrokeWidth);
            canvas.drawArc(mOval, mStartDegree + mValue * mSweptDegree,
                    (1 - mValue) * mSweptDegree,
                    false, mPaint);
        }

        // draw foreground
        if (Color.alpha(mFgColor) != 0) {
            mPaint.setColor(mFgColor);
            mPaint.setStrokeWidth(mStrokeWidth);
            canvas.drawArc(mOval, mStartDegree,
                    mValue * mSweptDegree,
                    false, mPaint);
        }

        // draw border
        if (mBorderStrokeWidth > 0 && Color.alpha(mBorderColor) != 0) {
            mPaint.setStrokeWidth(mBorderStrokeWidth);
            mPaint.setColor(mBorderColor);
            // inner border
            float innerBorderRadius = mRadius - mStrokeWidth
                    + mBorderStrokeWidth / 2f;
            mOval.set(x, y, x, y);
            mOval.inset(-innerBorderRadius, -innerBorderRadius);
            canvas.drawArc(mOval, mStartDegree, mSweptDegree, false, mPaint);
            // outer border
            float outerBoarderRadius = mRadius - mBorderStrokeWidth / 2f;
            mOval.set(x, y, x, y);
            mOval.inset(-outerBoarderRadius, -outerBoarderRadius);
            canvas.drawArc(mOval, mStartDegree, mSweptDegree, false, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setValue(float value) {
        if (value < 0f) {
            value = 0f;
        } else if (value > 1f) {
            value = 1f;
        }
        mValue = value;
        invalidate();
    }
}
