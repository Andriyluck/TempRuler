
package am.tempruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.ImageView;

public class ScaleView extends ImageView {

    private static Paint mCenterLinePaint;

    private static Paint m10LinePaint;

    private static Paint m5LinePaint;

    private static Paint m1LinePaint;

    private static Paint mTextPaint;

    private static Paint mBoldTextPaint;

    private static Paint mTitleTextPaint;

    private int SCALE_START = 10; // celcius

    private float mScale = 20;

    private int mScaleShift = 0;

    private Rect mBounds = new Rect();

    private Rect mTextBounds = new Rect();

    private float mTouchStartX = 0;

    private float mTouchStartShift = 0;

    private ScaleGestureDetector mGestureDetector;

    static {
        mCenterLinePaint = new Paint();
        mCenterLinePaint.setStyle(Style.STROKE);
        mCenterLinePaint.setStrokeWidth(14);

        m10LinePaint = new Paint();
        m10LinePaint.setStyle(Style.STROKE);
        m10LinePaint.setStrokeWidth(10);

        m5LinePaint = new Paint();
        m5LinePaint.setStyle(Style.STROKE);
        m5LinePaint.setStrokeWidth(6);

        m1LinePaint = new Paint();
        m1LinePaint.setStyle(Style.STROKE);
        m1LinePaint.setStrokeWidth(3);

        mTextPaint = new Paint();
        mTextPaint.setTextSize(40);

        mBoldTextPaint = new Paint();
        mBoldTextPaint.setTextSize(50);
        mBoldTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        mTitleTextPaint = new Paint();
        mTitleTextPaint.setTextSize(90);
        mTitleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new ScaleGestureDetector(getContext(), new OnScaleGestureListener() {
            float scaleValue = 1f;

            float scaleStart = 0;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleValue *= detector.getScaleFactor();
                mScale = (int)(scaleStart / scaleValue);
                if (mScale < 10)
                    mScale = 10;
                else if (mScale > 100)
                    mScale = 100;
                Log.d("AAA", "onScale " + scaleValue + " = " + mScale);
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                scaleStart = mScale;
                scaleValue = 1f;
                Log.d("AAA", "onScaleBegin " + detector.getScaleFactor() + " = " + mScale);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });
    }

    private void setScaleShift(int value) {
        mScaleShift = value;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat("mScale", mScale);
        bundle.putInt("mScaleShift", mScaleShift);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle)state;
            this.mScale = bundle.getFloat("mScale");
            this.mScaleShift = bundle.getInt("mScaleShift");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartShift = mScaleShift;
                mTouchStartX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                setScaleShift((int)(mTouchStartShift + ev.getX() - mTouchStartX));
                invalidate();
                break;
        }
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.getClipBounds(mBounds);
        int width = mBounds.width();
        int centerY = mBounds.centerY();

        float c_pxPerPoint = width / mScale;
        int c_pointsShift = (int)(mScaleShift / c_pxPerPoint); // how many C
                                                               // points shifted
        int c_startPoint = SCALE_START - c_pointsShift; // first visible C point
        int c_startShiftX = (int)(mScaleShift % c_pxPerPoint); // X shift to
                                                               // first visible
                                                               // C point

        // text
        int c_textEveryPoint = getTextFrequency(c_pxPerPoint);
        int f_textEveryPoint = c_textEveryPoint == 1 ? 2 : c_textEveryPoint;

        // celcius
        int celsius_value = c_startPoint;
        int x = 0;
        do {
            // get celcius X
            x = (int)(c_startShiftX + (celsius_value - c_startPoint) * c_pxPerPoint);

            // draw point line
            canvas.drawLine(x, centerY, x, centerY + getPointHeight(celsius_value),
                    getPaint(celsius_value));
            // draw point text
            if (celsius_value % c_textEveryPoint == 0) {
                String txt = String.format("%d\u00B0", celsius_value);

                mTextPaint.getTextBounds(txt, 0, txt.length(), mTextBounds);
                canvas.drawText(txt, x - mTextBounds.width() / 2, centerY + getTextY(celsius_value)
                        + mTextBounds.height(), getTextPaint(celsius_value));
            }

            celsius_value++;
        } while (x < width);

        // farenheit
        int f_value = (int)c2f(c_startPoint) - 1;
        do {
            float c_value = f2c((int)f_value);

            x = (int)(c_startShiftX + (c_value - c_startPoint) * c_pxPerPoint);

            // Log.d("AAA", f_value + "F = " + c_value + "C, x = " + x);

            // draw point line
            canvas.drawLine(x, centerY, x, centerY - getPointHeight(f_value),
                    getPaint((int)f_value));

            // draw point text
            if (f_value % f_textEveryPoint == 0) {
                String txt = String.format("%d\u00B0", f_value);
                mTextPaint.getTextBounds(txt, 0, txt.length(), mTextBounds);
                canvas.drawText(txt, x - mTextBounds.width() / 2, centerY - getTextY(f_value),
                        getTextPaint(f_value));
            }

            f_value++;
        } while (x < width);

        canvas.drawLine(0, centerY, width, centerY, mCenterLinePaint);

        canvas.drawText("\u00B0F", width / 10, mBounds.height() / 4, mTitleTextPaint);
        canvas.drawText("\u00B0C", width / 10, mBounds.height() * 0.75f, mTitleTextPaint);
    }

    private float c2f(float value) {
        return (9 * value) / 5 + 32;
    }

    private float f2c(float value) {
        return (value - 32) * 5 / 9;
    }

    private int getTextY(int value) {
        return value % 10 == 0 ? 80 : 60;
    }

    Paint getTextPaint(int value) {
        return value % 10 == 0 ? mBoldTextPaint : mTextPaint;
    }

    private int getTextFrequency(float pxPerPoint) {
        float txtw = mTextPaint.measureText("999C");
        int textEveryPoint = 1;
        if (txtw > pxPerPoint * 5)
            textEveryPoint = 20;
        else if (txtw > pxPerPoint * 3)
            textEveryPoint = 10;
        else if (txtw > pxPerPoint)
            textEveryPoint = 5;
        return textEveryPoint;
    }

    private Paint getPaint(int point) {
        if (point % 10 == 0) {
            return m10LinePaint;
        } else if (point % 5 == 0) {
            return m5LinePaint;
        }
        return m1LinePaint;
    }

    private int getPointHeight(int point) {
        return point % 10 == 0 ? 50 : 30;
    }
}
