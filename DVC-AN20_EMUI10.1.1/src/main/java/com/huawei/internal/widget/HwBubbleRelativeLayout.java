package com.huawei.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class HwBubbleRelativeLayout extends RelativeLayout {
    private static final int BUBBLE_LEG_HEIGHT = 8;
    private static final int BUBBLE_LEG_LENGTH = 10;
    private static final float CORNER_RADIUS = 8.0f;
    private static final int DIVIDE_BY_TWO = 2;
    private static final int PADDING = 30;
    private static final int ROUND_CORNER = 16;
    private static final float STROKE_WIDTH = 2.0f;
    private static final String TAG = "BubbleRelativeLayout";
    private final Path mBubbleLegPrototype = new Path();
    private Context mContext;
    private Paint mFillPaint = null;
    private final Paint mPaint = new Paint(4);
    private final Path mPath = new Path();

    public HwBubbleRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public HwBubbleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwBubbleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        Log.d(TAG, "init");
        int shadowColor = context.getResources().getColor(33882969);
        this.mPaint.setColor(shadowColor);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setStrokeCap(Paint.Cap.BUTT);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(2.0f);
        this.mPaint.setStrokeJoin(Paint.Join.MITER);
        this.mPaint.setPathEffect(new CornerPathEffect(CORNER_RADIUS));
        setLayerType(1, this.mPaint);
        this.mFillPaint = new Paint(this.mPaint);
        this.mFillPaint.setColor(shadowColor);
        setLayerType(1, this.mFillPaint);
        setPadding(30, 30, 30, 30);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void renderBubbleLegPrototype(float width) {
        this.mBubbleLegPrototype.reset();
        this.mBubbleLegPrototype.moveTo(width / 2.0f, (float) (30 - AbsHwMultiWindowCaptionView.dip2px(this.mContext, CORNER_RADIUS)));
        this.mBubbleLegPrototype.lineTo((width / 2.0f) + ((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, CORNER_RADIUS)), 40.0f);
        this.mBubbleLegPrototype.lineTo((width / 2.0f) - ((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, CORNER_RADIUS)), 40.0f);
        this.mBubbleLegPrototype.close();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float width = (float) canvas.getWidth();
        float height = (float) canvas.getHeight();
        Log.d(TAG, "onDraw width:" + width + " height:" + height + " padding:" + 30);
        renderBubbleLegPrototype(width);
        this.mPath.rewind();
        this.mPath.addRoundRect(new RectF(30.0f, 30.0f, width - 30.0f, height - 30.0f), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 16.0f), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 16.0f), Path.Direction.CW);
        this.mPath.addPath(this.mBubbleLegPrototype);
        canvas.drawPath(this.mPath, this.mPaint);
        canvas.drawPath(this.mPath, this.mFillPaint);
    }
}
