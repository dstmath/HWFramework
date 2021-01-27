package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import huawei.android.widget.plume.HwPlumeManager;
import java.util.Locale;

public class SeekBar extends android.widget.SeekBar {
    private static final int DEFAULT_FACTOR = 1;
    private static final int DICHOTOMY_SIZE = 2;
    private static final int OFFSET_LABELLING = 16;
    private static final int OFFSET_TRACK = 4;
    private static final int SEEKBAR_HEIGHT_LABELLING = 48;
    private static final int STEPTEXT_DEFAULT_COLOR = 8421504;
    private static final int TIPTEXT_DEFAULT_COLOR = 16777215;
    private int mBubbleTipBgId;
    private Drawable mCircleDr;
    private Context mContext;
    private boolean mIsSetLabelling;
    private boolean mIsSetTip;
    private boolean mIsShowPopWindow;
    private Paint mPaintCircle;
    private Paint mPaintText;
    private PopupWindow mPopupWindow;
    private int mProgress;
    private Resources mRes;
    private ResLoader mResLoader;
    private int mScaleLineDrawableId;
    private int mSingleTipBgId;
    private int mStepNum;
    private int mStepTextColor;
    private int mStepTextSize;
    private float mStepValue;
    private Rect mTempRect;
    private TextView mTextView;
    private int mTextViewHeight;
    private int mTextViewWidth;
    private int mTipBgId;
    private String mTipText;
    private int mTipTextColor;
    private int mTipTextSize;
    private int mXmlProgress;

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
        this.mIsSetTip = false;
        this.mIsSetLabelling = false;
        this.mIsShowPopWindow = false;
        this.mTipText = null;
        this.mProgress = 0;
        this.mTempRect = new Rect();
        this.mContext = context;
        TypedArray progresssTypedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        this.mXmlProgress = progresssTypedArray.getInt(3, this.mProgress);
        progresssTypedArray.recycle();
        TypedArray showTextTypedArray = context.obtainStyledAttributes(attrs, R.styleable.Switch, defStyleAttr, defStyleRes);
        this.mIsShowPopWindow = showTextTypedArray.getBoolean(11, false);
        showTextTypedArray.recycle();
        this.mRes = ResLoaderUtil.getResources(context);
        this.mStepTextSize = ResLoaderUtil.getDimensionPixelSize(context, "hwseekbar_master_caption_2");
        this.mTipTextSize = ResLoaderUtil.getDimensionPixelSize(context, "emui_text_size_body2");
        this.mResLoader = ResLoader.getInstance();
        if (this.mResLoader.getTheme(context) != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.SeekBar, defStyleAttr, defStyleRes);
            this.mBubbleTipBgId = array.getResourceId(0, 0);
            this.mSingleTipBgId = array.getResourceId(1, 0);
            this.mScaleLineDrawableId = array.getResourceId(2, 0);
            this.mTipTextColor = array.getColor(3, TIPTEXT_DEFAULT_COLOR);
            this.mStepTextColor = array.getColor(4, STEPTEXT_DEFAULT_COLOR);
            array.recycle();
        }
        setDefaultFocusHighlightEnabled(false);
        if (this.mIsShowPopWindow) {
            initTip();
            this.mTipBgId = this.mBubbleTipBgId;
        }
        setValueFromPlume();
    }

    private void setValueFromPlume() {
        if (!HwPlumeManager.isPlumeUsed(this.mContext)) {
            setExtendProgressEnabled(true);
        } else {
            setExtendProgressEnabled(HwPlumeManager.getInstance(this.mContext).getDefault(this, "seekBarScrollEnabled", true));
        }
    }

    private static int dip2px(int dp) {
        return (int) TypedValue.applyDimension(1, (float) dp, Resources.getSystem().getDisplayMetrics());
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsSeekBar, android.widget.ProgressBar, android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null) {
            progressDrawable.setAlpha(isEnabled() ? 255 : 76);
        }
        Drawable drawable = this.mCircleDr;
        if (drawable != null) {
            drawable.setState(getDrawableState());
        }
    }

    public void setTip(boolean isSetLabelling, int stepNum, boolean isBubbleTip) {
        if (stepNum != 0) {
            this.mIsSetTip = true;
            this.mIsSetLabelling = isSetLabelling;
            this.mStepNum = stepNum;
            this.mStepValue = (((float) (getMax() - getMin())) + 0.0f) / ((float) this.mStepNum);
            this.mTipBgId = isBubbleTip ? this.mBubbleTipBgId : this.mSingleTipBgId;
            this.mCircleDr = this.mRes.getDrawable(this.mScaleLineDrawableId);
            initTip();
            this.mPaintCircle = new Paint();
            this.mPaintCircle.setAntiAlias(true);
            this.mPaintText = new Paint();
            this.mPaintText.setAntiAlias(true);
            this.mPaintText.setColor(this.mStepTextColor);
            this.mPaintText.setTextSize((float) this.mStepTextSize);
            this.mPaintText.setTypeface(Typeface.create("HwChinese-medium", 0));
            if (this.mIsSetLabelling) {
                getLayoutParams().height = dip2px(SEEKBAR_HEIGHT_LABELLING);
            }
            setProgress(this.mXmlProgress);
            invalidate();
        }
    }

    public void setTipText(String tipText) {
        if (this.mTipBgId == this.mBubbleTipBgId && this.mIsShowPopWindow && tipText != null) {
            this.mTipText = tipText;
            this.mTextView.setText(this.mTipText);
            updateTip();
        }
    }

    private void initTip() {
        this.mTextView = new TextView(this.mContext);
        this.mTextView.setTextColor(this.mTipTextColor);
        this.mTextView.setTextSize(0, (float) this.mTipTextSize);
        this.mTextView.setTypeface(Typeface.create(ResLoaderUtil.getString(this.mContext, "emui_text_font_family_medium"), 0));
        int i = this.mTipBgId;
        if (i == this.mSingleTipBgId) {
            Drawable tipBgDra = null;
            try {
                tipBgDra = this.mRes.getDrawable(i);
            } catch (Resources.NotFoundException e) {
                Log.e("SeekBar", "Throws NotFoundException if the mTipBgId ID does not exist.");
            }
            if (tipBgDra != null) {
                this.mTextView.setLayoutParams(new ViewGroup.LayoutParams(tipBgDra.getIntrinsicWidth(), tipBgDra.getIntrinsicHeight()));
            } else {
                this.mTextView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            }
            this.mTextView.setGravity(17);
        } else {
            this.mTextView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.mTextView.setGravity(17);
        }
        this.mTextView.setSingleLine(true);
        this.mPopupWindow = new PopupWindow((View) this.mTextView, -2, -2, false);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsSeekBar, android.widget.ProgressBar, android.view.View
    public synchronized void onDraw(Canvas canvas) {
        if (!this.mIsSetTip || !this.mIsSetLabelling) {
            super.onDraw(canvas);
        } else {
            int saveCount = canvas.save();
            canvas.translate(0.0f, (float) (0 - dip2px(4)));
            super.onDraw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    /* access modifiers changed from: protected */
    public void drawTrackEx(Canvas canvas) {
        if (this.mIsSetTip) {
            drawCircles(canvas);
        }
        super.drawTrackEx(canvas);
    }

    /* access modifiers changed from: protected */
    public void onHwStartTrackingTouch() {
        if (this.mIsShowPopWindow) {
            this.mTextView.setBackgroundResource(this.mTipBgId);
            this.mPopupWindow.showAsDropDown(this);
            onProgressRefreshEx(getScaleEx(), true, getProgress());
        }
    }

    /* access modifiers changed from: protected */
    public void onHwStopTrackingTouch() {
        if (this.mIsShowPopWindow) {
            this.mPopupWindow.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    public void onProgressRefreshEx(float scale, boolean isFromUser, int progress) {
        int currentProgress = progress;
        if (this.mIsSetTip) {
            float f = this.mStepValue;
            currentProgress = Math.round(f * ((float) Math.round(((float) progress) / f)));
            setProgress(currentProgress);
        }
        if (this.mIsShowPopWindow) {
            this.mTextView.setText(String.valueOf(currentProgress));
            updateTip();
        }
    }

    @Override // android.widget.ProgressBar
    public synchronized void setProgress(int progress) {
        this.mProgress = this.mIsSetTip ? Math.round(this.mStepValue * ((float) Math.round(((float) progress) / this.mStepValue))) : progress;
        super.setProgress(this.mProgress);
        if (this.mIsShowPopWindow) {
            updateTip();
        }
    }

    /* access modifiers changed from: protected */
    public void changeProgressWithGenericMotion(int delta) {
        super.changeProgressWithGenericMotion(delta * (this.mIsSetTip ? Math.round(this.mStepValue) : 1));
    }

    private void updateTip() {
        updatePopWidth();
        int offsetX = (getPaddingLeft() + ((int) Math.round(((double) ((getWidth() - this.mPaddingLeft) - this.mPaddingRight)) * ((double) ((!isLayoutRtl() || "ur".equals(Locale.getDefault().getLanguage())) ? getScaleEx() : 1.0f - getScaleEx()))))) - (this.mTextViewWidth / 2);
        int offsetY = (0 - getMeasuredHeight()) - this.mTextViewHeight;
        if (this.mPopupWindow.isShowing()) {
            this.mPopupWindow.update(this, offsetX, offsetY, this.mTextViewWidth, this.mTextViewHeight);
        }
    }

    private void updatePopWidth() {
        this.mTextView.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
        this.mTextViewWidth = this.mTextView.getMeasuredWidth();
        this.mTextViewHeight = this.mTextView.getMeasuredHeight();
    }

    private void drawCircles(Canvas canvas) {
        int stepText;
        Drawable drawable = this.mCircleDr;
        if (drawable != null) {
            Bitmap bitmap = drawableToBitmap(drawable);
            int circleHeight = this.mCircleDr.getIntrinsicHeight();
            int circleWidth = this.mCircleDr.getIntrinsicWidth();
            int width = getWidth();
            float trackLength = (float) (((width - getPaddingLeft()) - getPaddingRight()) - circleWidth);
            int stepNum = this.mStepNum;
            int circleTop = (((getHeight() - this.mPaddingTop) - this.mPaddingBottom) / 2) - (circleHeight / 2);
            int circleLeft = getPaddingLeft() + (circleWidth / 2);
            if (stepNum > 1) {
                float stepLength = trackLength / ((float) stepNum);
                if (this.mIsSetLabelling) {
                    int i = 0;
                    while (i <= stepNum) {
                        if (!isLayoutRtl() || "ur".equals(Locale.getDefault().getLanguage())) {
                            stepText = Math.round((this.mStepValue * ((float) i)) + ((float) getMin()));
                        } else {
                            stepText = Math.round((this.mStepValue * ((float) (stepNum - i))) + ((float) getMin()));
                        }
                        int strHeight = getTextHeight(String.valueOf(stepText));
                        canvas.drawText(String.valueOf(stepText), (((float) circleLeft) + (((float) i) * stepLength)) - ((float) (getTextWidth(String.valueOf(stepText)) / 2)), (float) (circleTop + circleHeight + dip2px(16) + strHeight), this.mPaintText);
                        i++;
                        width = width;
                        circleHeight = circleHeight;
                        trackLength = trackLength;
                    }
                    return;
                }
                for (int i2 = 0; i2 <= stepNum; i2++) {
                    canvas.drawBitmap(bitmap, (((float) circleLeft) + (((float) i2) * stepLength)) - ((float) (circleWidth / 2)), (float) circleTop, this.mPaintCircle);
                }
            }
        }
    }

    private int getTextWidth(String str) {
        if (TextUtils.isEmpty(str)) {
            return -1;
        }
        this.mPaintText.getTextBounds(str, 0, str.length(), this.mTempRect);
        return this.mTempRect.width();
    }

    private int getTextHeight(String str) {
        if (TextUtils.isEmpty(str)) {
            return -1;
        }
        this.mPaintText.getTextBounds(str, 0, str.length(), this.mTempRect);
        return this.mTempRect.height();
    }
}
