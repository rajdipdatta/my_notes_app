package com.rd.mynotes;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.appcompat.widget.AppCompatEditText;

public class LinedEditText extends AppCompatEditText {
    private Rect mRect;
    private Paint mPaint;
    private Paint mMarginPaint;

    // Configuration values
    private int mLineColor = 0x800000FF; // Default light blue
    private float mLineThickness = 2.0f; // Default 2px
    private int mMarginColor = 0x80FF0000; // Default light red margin

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRect = new Rect();
        mPaint = new Paint();
        mMarginPaint = new Paint();

        // 1. Fetch custom attributes from XML layout
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinedEditText, 0, 0);
            try {
                mLineColor = a.getColor(R.styleable.LinedEditText_lineColor, mLineColor);
                mLineThickness = a.getDimension(R.styleable.LinedEditText_lineThickness, mLineThickness);
                mMarginColor = a.getColor(R.styleable.LinedEditText_marginLineColor, mMarginColor);
            } finally {
                a.recycle();
            }
        }

        // 2. Adjust colors for Dark Mode automatically if no runtime changes have occurred yet
        if (isDarkMode()) {
            mLineColor = 0x40FFFFFF;   // Subtle semi-transparent white lines
            mMarginColor = 0x40FF5555; // Darker, subtle red margin to protect contrast
        }

        // 3. Initialize horizontal line paint setup
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(mLineThickness);

        // 4. Initialize vertical margin line paint setup
        mMarginPaint.setStyle(Paint.Style.STROKE);
        mMarginPaint.setColor(mMarginColor);
        mMarginPaint.setStrokeWidth(mLineThickness);
    }

    private boolean isDarkMode() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    /* =========================================================================
       NEW: Runtime Setter Methods
       ========================================================================= */

    /**
     * Set the horizontal page line color dynamically.
     * @param color The hex color value (e.g., 0xFFFFFFFF or Color.WHITE)
     */
    public void setLineColor(int color) {
        this.mLineColor = color;
        mPaint.setColor(mLineColor);
        invalidate(); // Force the view to redraw horizontal lines immediately
    }

    /**
     * Set the vertical margin line color dynamically.
     * @param color The hex color value
     */
    public void setMarginLineColor(int color) {
        this.mMarginColor = color;
        mMarginPaint.setColor(mMarginColor);
        invalidate(); // Force the view to redraw the margin line immediately
    }

    /**
     * Set the text size dynamically in SP (Scale-independent Pixels).
     * Inherited helper overloads standard setTextSize to keep interface simple.
     * @param sizeInSp Size value in SP
     */
    public void setPaperTextSize(float sizeInSp) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp);
    }

    /**
     * Set the text color dynamically.
     * Overloads standard setTextColor with forced updates if text lines need layout shifts.
     * @param color The hex color value
     */
    public void setPaperTextColor(int color) {
        setTextColor(color);
    }

    /* =========================================================================
       Drawing Routine
       ========================================================================= */

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        Rect r = mRect;
        Paint paint = mPaint;

        // Draw horizontal lines for all existing text rows
        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
        }

        // Fill the remaining bottom space with empty horizontal lines
        int height = getHeight();
        int lineHeight = getLineHeight();
        int maxLines = height / lineHeight;

        if (count < maxLines) {
            for (int i = count; i < maxLines; i++) {
                int baseline = r.bottom + lineHeight;
                r.bottom = baseline;
                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }
        }

        // Draw a vertical margin line (Left notebook margin line)
        float marginX = 40 * getContext().getResources().getDisplayMetrics().density;
        canvas.drawLine(marginX, 0, marginX, height, mMarginPaint);

        super.onDraw(canvas);
    }
}

