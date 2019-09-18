package huawei.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RoundProgressBar extends View {
    private int max;
    private Paint paint;
    private int progress;
    private int roundColor;
    private int roundProgressColor;
    private int roundWidth;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.paint = new Paint();
        this.roundColor = Color.parseColor("#7fFFFFFF");
        this.roundProgressColor = Color.parseColor("#ff27c0c6");
        this.roundWidth = 6;
        this.max = 100;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centreH = getHeight() / 2;
        int centreW = getWidth() / 2;
        int radiusH = centreH - (this.roundWidth / 2);
        int radiusW = centreW - (this.roundWidth / 2);
        this.paint.setColor(this.roundColor);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth((float) this.roundWidth);
        this.paint.setAntiAlias(true);
        RectF oval = new RectF((float) (centreW - radiusW), (float) (centreH - radiusH), (float) (centreW + radiusW), (float) (centreH + radiusH));
        Canvas canvas2 = canvas;
        canvas2.drawOval(oval, this.paint);
        this.paint.setStrokeWidth((float) this.roundWidth);
        this.paint.setColor(this.roundProgressColor);
        this.paint.setStyle(Paint.Style.STROKE);
        canvas2.drawArc(oval, -90.0f, ((float) (360 * this.progress)) / ((float) this.max), false, this.paint);
    }

    public void setProgress(int progress2) {
        if (progress2 > this.max) {
            progress2 = this.max;
        }
        if (progress2 <= this.max) {
            this.progress = progress2;
            postInvalidate();
        }
    }
}
