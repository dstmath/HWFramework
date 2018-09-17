package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.cover.CoverManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import huawei.android.hwutil.IconBitmapUtils;

public class SeekBar extends android.widget.SeekBar {
    private static final int LABELLING_TEXT_PADDING = 2;
    private static final int LABELLING_TEXT_SIZE = 9;
    private static final int OFFSET_HEIGHT_TIP = 6;
    private static final int OFFSET_LABELLING = 4;
    private static final int TIP_TEXT_SIZE = 13;
    private static final int TIP_TEXT_TOP_PADDING = 8;
    private int mBottomForLabelling;
    private int mBubbleTipBgId;
    private Drawable mCircleDr;
    private Context mContext;
    private boolean mIsBubbleTip;
    private boolean mIsFirst;
    private boolean mIsShowPopWindow;
    private int mProgress;
    private Resources mRes;
    private boolean mSetLabelling;
    private boolean mSetTip;
    private int mSingleTipBgId;
    private int mStepNum;
    private int mStepTextColor;
    private int mStepValue;
    private int mThumbHeight;
    private int mThumbWidth;
    private int mTipBgId;
    private String mTipText;
    private int mTipTextColor;
    private TextView mTv;
    private int mTvHeight;
    private int mTvWidth;
    private int mXmlProgress;
    private PopupWindow mpw;

    public SeekBar(Context context) {
        this(context, null);
    }

    public SeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSetTip = false;
        this.mSetLabelling = false;
        this.mIsBubbleTip = false;
        this.mIsShowPopWindow = false;
        this.mIsFirst = true;
        this.mTipText = null;
        this.mProgress = 0;
        this.mContext = context;
        TypedArray progresssTypedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        this.mXmlProgress = progresssTypedArray.getInt(3, this.mProgress);
        progresssTypedArray.recycle();
        TypedArray showTextTypedArray = context.obtainStyledAttributes(attrs, R.styleable.Switch, defStyleAttr, defStyleRes);
        this.mIsShowPopWindow = showTextTypedArray.getBoolean(11, false);
        showTextTypedArray.recycle();
        if (HwWidgetFactory.isHwLightTheme(context)) {
            this.mBubbleTipBgId = 33751330;
            this.mSingleTipBgId = 33751333;
            this.mTipTextColor = -1;
            this.mStepTextColor = CoverManager.DEFAULT_COLOR;
        } else if (HwWidgetFactory.isHwDarkTheme(context)) {
            this.mBubbleTipBgId = 33751331;
            this.mSingleTipBgId = 33751334;
            this.mTipTextColor = CoverManager.DEFAULT_COLOR;
            this.mStepTextColor = -1;
        } else if (HwWidgetFactory.isHwEmphasizeTheme(context)) {
            this.mBubbleTipBgId = 33751332;
            this.mSingleTipBgId = 33751335;
            this.mTipTextColor = -1;
            this.mStepTextColor = -1;
        }
        if (this.mIsShowPopWindow) {
            this.mRes = this.mContext.getResources();
            this.mThumbWidth = getThumb().getIntrinsicWidth();
            this.mThumbHeight = getThumb().getIntrinsicHeight();
            this.mTipBgId = this.mSingleTipBgId;
            Drawable tipBackground = this.mRes.getDrawable(this.mTipBgId);
            if (tipBackground != null) {
                this.mPaddingTop += tipBackground.getIntrinsicHeight() + dip2px(6);
            }
            initTip();
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null) {
            progressDrawable.setAlpha(isEnabled() ? 255 : 76);
        }
        if (this.mCircleDr != null) {
            this.mCircleDr.setState(getDrawableState());
        }
    }

    public void setTip(boolean setLabelling, int stepNum, boolean isBubbleTip) {
        if (stepNum != 0) {
            this.mRes = this.mContext.getResources();
            this.mStepNum = stepNum;
            this.mSetTip = true;
            this.mStepValue = getMax() / this.mStepNum;
            this.mThumbWidth = getThumb().getIntrinsicWidth();
            this.mThumbHeight = getThumb().getIntrinsicHeight();
            this.mTipBgId = this.mSingleTipBgId;
            this.mIsBubbleTip = isBubbleTip;
            Drawable tipBgDra = this.mRes.getDrawable(this.mTipBgId);
            this.mCircleDr = this.mRes.getDrawable(33751329);
            if (tipBgDra != null) {
                this.mPaddingTop += tipBgDra.getIntrinsicHeight() + dip2px(6);
            }
            if (this.mIsBubbleTip) {
                this.mTipBgId = this.mBubbleTipBgId;
            }
            if (this.mSetLabelling != setLabelling) {
                this.mSetLabelling = setLabelling;
            }
            if (this.mSetLabelling) {
                int labellingHeight = getTextHeight("0", 9) + dip2px(2);
                int circleHeight = this.mCircleDr.getIntrinsicHeight();
                int trackHeight = circleHeight;
                this.mBottomForLabelling = (((dip2px(4) + labellingHeight) + circleHeight) + (circleHeight / 2)) - (this.mThumbHeight / 2);
            }
            initTip();
            setProgress(this.mXmlProgress);
        }
    }

    public void setTipText(String tipText) {
        if (this.mIsBubbleTip && tipText != null) {
            this.mTipText = tipText;
            this.mTv.setText(this.mTipText);
        }
    }

    private void initTip() {
        this.mTv = new TextView(this.mContext);
        this.mTv.setTextColor(this.mTipTextColor);
        this.mTv.setTextSize(13.0f);
        if (this.mTipBgId == this.mSingleTipBgId) {
            Drawable tipBgDra = this.mRes.getDrawable(this.mTipBgId);
            this.mTv.setWidth(tipBgDra.getIntrinsicWidth());
            this.mTv.setHeight(tipBgDra.getIntrinsicHeight());
            this.mTv.setGravity(17);
        } else {
            this.mTv.setLayoutParams(new LayoutParams(-2, -2));
            this.mTv.setGravity(17);
        }
        this.mTv.setSingleLine(true);
        this.mpw = new PopupWindow(this.mTv, -2, -2);
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mSetLabelling && this.mIsFirst) {
            this.mPaddingBottom += this.mBottomForLabelling;
            this.mIsFirst = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void drawTrack(Canvas canvas) {
        super.drawTrack(canvas);
        if (this.mSetTip) {
            drawCircles(canvas);
        }
    }

    protected void onHwStartTrackingTouch() {
        if (this.mIsShowPopWindow) {
            this.mTv.setBackgroundResource(this.mTipBgId);
            this.mpw.showAsDropDown(this);
            onProgressRefresh(getScale(), true, getProgress());
        }
    }

    protected void onHwStopTrackingTouch() {
        if (this.mIsShowPopWindow) {
            this.mpw.dismiss();
        }
    }

    protected void onProgressRefresh(float scale, boolean fromUser, int progress) {
        if (this.mSetTip) {
            super.onProgressRefresh(getScale(), fromUser, this.mStepValue * ((int) (((((float) progress) + 0.0f) / ((float) this.mStepValue)) + 0.5f)));
            progress = this.mStepValue * ((int) (((((float) progress) + 0.0f) / ((float) this.mStepValue)) + 0.5f));
            setProgress(progress);
        } else {
            super.onProgressRefresh(scale, fromUser, progress);
        }
        if (this.mIsShowPopWindow) {
            if (this.mTipBgId == this.mSingleTipBgId) {
                this.mTv.setText(progress + "");
            }
            updateTip();
        }
    }

    public synchronized void setProgress(int progress) {
        if (this.mSetTip) {
            progress = this.mStepValue * ((int) (((((float) progress) + 0.0f) / ((float) this.mStepValue)) + 0.5f));
        }
        this.mProgress = progress;
        super.setProgress(this.mProgress);
    }

    private void updateTip() {
        updatePopWidth();
        this.mpw.update(this, (((this.mThumbLeft + this.mPaddingLeft) - (this.mTvWidth / 2)) - getThumbOffset()) + (this.mThumbWidth / 2), (((-this.mThumbHeight) - this.mTvHeight) - dip2px(6)) - this.mBottomForLabelling, this.mTvWidth, this.mTvHeight);
    }

    private void updatePopWidth() {
        this.mTv.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        this.mTvWidth = this.mTv.getMeasuredWidth();
        this.mTvHeight = this.mTv.getMeasuredHeight();
    }

    private void drawCircles(Canvas canvas) {
        Paint paint = new Paint();
        Paint paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setColor(this.mStepTextColor);
        paintText.setAlpha(76);
        paintText.setTextSize((float) dip2px(9));
        if (this.mCircleDr != null) {
            Bitmap bmp = IconBitmapUtils.drawableToBitmap(this.mCircleDr);
            int circleWidth = this.mCircleDr.getIntrinsicWidth();
            int circleHeight = this.mCircleDr.getIntrinsicHeight();
            int circleTop = (this.mPaddingTop + (this.mThumbHeight / 2)) - (circleHeight / 2);
            int circleLeft = (this.mPaddingLeft - (circleWidth / 2)) + ((this.mThumbWidth / 2) - getThumbOffset());
            int availableLen = ((getWidth() - this.mPaddingRight) - this.mPaddingLeft) - (this.mThumbWidth - (getThumbOffset() * 2));
            if (this.mStepNum <= 1) {
                return;
            }
            int i;
            if (this.mSetLabelling) {
                for (i = 0; i < this.mStepNum + 1; i++) {
                    int stepText;
                    int strHeight = getTextHeight((this.mStepValue * i) + "", 9);
                    int strWidth = getTextWidth((this.mStepValue * i) + "", 9);
                    Log.e("TAG", "strHeight===========" + strHeight);
                    Log.e("TAG", "strWidth===========" + strWidth);
                    if (isRtlLocale()) {
                        stepText = this.mStepValue * (this.mStepNum - i);
                    } else {
                        stepText = this.mStepValue * i;
                    }
                    canvas.drawText(stepText + "", ((float) ((((availableLen / this.mStepNum) * i) + circleLeft) - (strWidth / 2))) + 0.0f, (float) ((((circleHeight * 2) + circleTop) + dip2px(4)) + strHeight), paintText);
                }
                for (i = 1; i < this.mStepNum; i++) {
                    canvas.drawBitmap(bmp, (float) (((availableLen / this.mStepNum) * i) + circleLeft), (float) (circleTop + circleHeight), paint);
                }
                return;
            }
            for (i = 1; i < this.mStepNum; i++) {
                canvas.drawBitmap(bmp, (float) (((availableLen / this.mStepNum) * i) + circleLeft), (float) circleTop, paint);
            }
        }
    }

    private int dip2px(int dpValue) {
        return (int) ((((float) dpValue) * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int getTextWidth(String str, int dps) {
        if (str == null || dps <= 0) {
            return -1;
        }
        Paint paint = new Paint();
        paint.setTextSize((float) dip2px(dps));
        char[] arr = str.toCharArray();
        Rect rect = new Rect();
        int len = 0;
        for (char c : arr) {
            paint.getTextBounds(c + "", 0, 1, rect);
            len += rect.width();
        }
        return len;
    }

    private int getTextHeight(String str, int dps) {
        if (str == null || dps <= 0) {
            return -1;
        }
        Paint paint = new Paint();
        paint.setTextSize((float) dip2px(dps));
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, 1, rect);
        return rect.height();
    }
}
