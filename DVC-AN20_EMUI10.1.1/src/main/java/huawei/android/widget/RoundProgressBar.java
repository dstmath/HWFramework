package huawei.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RoundProgressBar extends View {
    private static final int DICHOTOMY_SIZE = 2;
    private static final int MAX_ANGLE = 360;
    private static final int MAX_PROGRESS = 100;
    private static final int ROUND_WIDTH = 6;
    private static final float START_ANGLE = -90.0f;
    private int mMax;
    private Paint mPaint;
    private int mProgress;
    private int mRoundColor;
    private int mRoundProgressColor;
    private int mRoundWidth;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRoundWidth = 6;
        this.mMax = 100;
        this.mPaint = new Paint();
        this.mRoundColor = Color.parseColor("#7fFFFFFF");
        this.mRoundProgressColor = Color.parseColor("#ff27c0c6");
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerHeight = getHeight() / 2;
        int centerWidth = getWidth() / 2;
        int i = this.mRoundWidth;
        int radiusHeight = centerHeight - (i / 2);
        int radiusWidth = centerWidth - (i / 2);
        this.mPaint.setColor(this.mRoundColor);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeWidth((float) this.mRoundWidth);
        this.mPaint.setAntiAlias(true);
        RectF oval = new RectF((float) (centerWidth - radiusWidth), (float) (centerHeight - radiusHeight), (float) (centerWidth + radiusWidth), (float) (centerHeight + radiusHeight));
        canvas.drawOval(oval, this.mPaint);
        this.mPaint.setStrokeWidth((float) this.mRoundWidth);
        this.mPaint.setColor(this.mRoundProgressColor);
        this.mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval, START_ANGLE, ((float) (this.mProgress * MAX_ANGLE)) / ((float) this.mMax), false, this.mPaint);
    }

    public void setProgress(int progress) {
        int i = this.mMax;
        if (progress <= i) {
            i = progress;
        }
        this.mProgress = i;
        postInvalidate();
    }
}
