package com.huawei.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class HwDragBarPopupBubbleLayout extends RelativeLayout {
    private static final int BET_DIS = 6;
    private static final int BUBBLE_LEG_HEIGHT = 8;
    private static final int CAL_VAL = 2;
    private static final float CORNER_RADIUS = 12.0f;
    private static final int LR_DIS = 4;
    private static final int PADDING_TB = 12;
    private static final int ROUND_DIS = 8;
    private static final int ROUND_VAL = 3;
    protected static final float SCALE = 2.0f;
    private static final float SHADOW_BLUR = 8.0f;
    private static final String SHADOW_COLOR = "#33000000";
    private static final float SHADOW_X = 0.0f;
    private static final float SHADOW_Y = 2.0f;
    private static final float STROKE_WIDTH = 2.0f;
    private static final String TAG = "HwDragBarPopupBubbleLayout";
    private static final int TB_DIS = 3;
    private float mBubbleHeight;
    private final Path mBubbleLegPrototype = new Path();
    private float mBubbleWidth;
    private float mCaptionWidth;
    private Context mContext;
    private Paint mFillPaint = null;
    private float mHeightSize;
    private float mHeightTmpSize;
    protected float mPaddingRL;
    private final Path mPath = new Path();
    private float mRatioValue;
    private float mWidthSize;
    protected float mWidthTmpSize;

    public HwDragBarPopupBubbleLayout(Context context) {
        super(context);
        init(context);
    }

    public HwDragBarPopupBubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HwDragBarPopupBubbleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mFillPaint = new Paint();
        this.mFillPaint.setColor(this.mContext.getColor(33882612));
        this.mFillPaint.setShadowLayer((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 16.0f), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 0.0f), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 4.0f), Color.parseColor(SHADOW_COLOR));
        this.mFillPaint.setAntiAlias(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void renderBubbleLegPrototype(float bubbleWidth) {
        this.mBubbleLegPrototype.reset();
        this.mBubbleLegPrototype.moveTo(bubbleWidth / 2.0f, (float) (AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f) - AbsHwMultiWindowCaptionView.dip2px(this.mContext, 16.0f)));
        this.mBubbleLegPrototype.lineTo((bubbleWidth / 2.0f) + ((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 16.0f)), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f));
        this.mBubbleLegPrototype.lineTo((bubbleWidth / 2.0f) - ((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 16.0f)), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f));
        this.mBubbleLegPrototype.close();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            setPadding((int) this.mPaddingRL, AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f), (int) this.mPaddingRL, AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f));
            this.mBubbleWidth = (float) canvas.getWidth();
            this.mBubbleHeight = (float) canvas.getHeight();
            renderBubbleLegPrototype(this.mBubbleWidth);
            this.mPath.rewind();
            this.mPath.addRoundRect(new RectF(this.mPaddingRL, (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f), this.mBubbleWidth - this.mPaddingRL, this.mBubbleHeight - ((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f))), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f), (float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 24.0f), Path.Direction.CW);
            this.mPath.addPath(this.mBubbleLegPrototype);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
            canvas.drawPath(this.mPath, this.mFillPaint);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.RelativeLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mWidthSize = this.mWidthTmpSize + (this.mPaddingRL * 2.0f);
        setContentHeight();
        this.mHeightSize = this.mHeightTmpSize + ((float) AbsHwMultiWindowCaptionView.dip2px(this.mContext, 48.0f));
        setMeasuredDimension(Math.round(this.mWidthSize), Math.round(this.mHeightSize));
    }

    public void setContentWidth(int captionWidth) {
        this.mWidthTmpSize = (((float) captionWidth) - (this.mContext.getResources().getDimension(34472308) * 2.0f)) - (2.0f * this.mContext.getResources().getDimension(34472307));
        this.mCaptionWidth = (float) captionWidth;
    }

    private void setContentHeight() {
        this.mHeightTmpSize = this.mRatioValue * 14.0f;
    }

    public float getRatioValue() {
        this.mRatioValue = this.mWidthTmpSize / 44.0f;
        return this.mRatioValue;
    }

    public void setContentWidthForAppLock() {
        this.mWidthTmpSize = this.mRatioValue * 20.0f;
    }

    public void updateRightAndLeftPadding() {
        this.mPaddingRL = (this.mCaptionWidth - this.mWidthTmpSize) / 2.0f;
    }
}
