package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hwcontrol.HwWidgetFactory;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.view.MotionEvent;
import huawei.android.os.HwGeneralManager;
import java.util.ArrayList;
import java.util.List;

public class NumberPicker extends android.widget.NumberPicker {
    private static final String HW_CHINESE_MEDIUM_TYPEFACE = "HwChinese-medium";
    private int FLING_BACKWARD;
    private int FLING_FOWARD;
    private int FLING_STOP;
    private boolean isVibrateImplemented;
    private Context mContext_Vibrate;
    private final Typeface mDefaultTypeface;
    private final int mEdgeOffset;
    private List<android.widget.NumberPicker> mFireList;
    private int mFlingDirection;
    private final Typeface mHwChineseMediumTypeface;
    private boolean mIsDarkHwTheme;
    private boolean mIsLongPress;
    private int mNormalTextColor;
    private float mNormalTextSize;
    private int mSelectorOffset;
    private int mSelectorTextColor;
    private float mSelectorTextSize;
    private int mSmallTextColor;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 16844068);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSelectorOffset = 0;
        this.mSelectorTextSize = 0.0f;
        this.mNormalTextSize = 0.0f;
        this.mSelectorTextColor = 0;
        this.mSmallTextColor = 0;
        this.mNormalTextColor = 0;
        this.mIsDarkHwTheme = false;
        this.mFireList = new ArrayList();
        this.isVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
        this.FLING_FOWARD = 0;
        this.FLING_BACKWARD = 1;
        this.FLING_STOP = 2;
        this.mFlingDirection = this.FLING_STOP;
        this.mIsLongPress = false;
        initialNumberPicker(context, attrs);
        getSelectorWheelPaint().setColor(this.mNormalTextColor);
        this.mContext_Vibrate = context;
        this.mSelectorWheelItemCount = 5;
        this.SELECTOR_MIDDLE_ITEM_INDEX = this.mSelectorWheelItemCount / 2;
        this.mSelectorIndices = new int[this.mSelectorWheelItemCount];
        this.mDefaultTypeface = Typeface.create((String) null, 0);
        this.mHwChineseMediumTypeface = Typeface.create(HW_CHINESE_MEDIUM_TYPEFACE, 0);
        this.mEdgeOffset = context.getResources().getDimensionPixelSize(34472050);
        getInputText().setTypeface(this.mHwChineseMediumTypeface);
        getInputText().setTextColor(this.mSelectorTextColor);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!getHasSelectorWheel() || (isEnabled() ^ 1) != 0) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                handleFireList();
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    protected void initializeFadingEdges() {
    }

    protected int getNormalTextColor(int color) {
        return this.mNormalTextColor;
    }

    protected void setSelectorColor(int i, int currentOffset, int initOffset, int index, int height, Paint paint) {
        int offset = currentOffset;
        offset = currentOffset + ((i - index) * height);
        if (offset <= initOffset - this.mSelectorOffset || offset >= this.mSelectorOffset + initOffset) {
            paint.setTextSize(this.mNormalTextSize);
            paint.setColor(this.mSmallTextColor);
            paint.setTypeface(this.mDefaultTypeface);
        } else {
            paint.setTextSize(this.mSelectorTextSize);
            paint.setColor(getAlphaGradient(initOffset, offset, this.mSelectorTextColor));
            paint.setTypeface(this.mHwChineseMediumTypeface);
        }
        if (i == 0 || this.mSelectorIndices.length - 1 == i) {
            paint.setAlpha(102);
        }
    }

    protected float adjustYPosition(int i, float y) {
        float ret = y;
        if (i == this.SELECTOR_MIDDLE_ITEM_INDEX) {
            return y - (this.mSelectorTextSize - this.mNormalTextSize);
        }
        return ret;
    }

    public void addFireList(android.widget.NumberPicker np) {
        this.mFireList.add(np);
    }

    protected int initializeSelectorElementHeight(int textSize, int selectorTextGapHeight) {
        return ((textSize * 5) / 3) + selectorTextGapHeight;
    }

    private void initialNumberPicker(Context context, AttributeSet attrs) {
        Resources res = context.getResources();
        this.mSelectorOffset = res.getDimensionPixelSize(34472047);
        this.mSelectorTextSize = (float) res.getDimensionPixelSize(34472048);
        this.mNormalTextSize = (float) res.getDimensionPixelSize(34472049);
        this.mSelectorTextColor = res.getColor(33882282);
        this.mIsDarkHwTheme = HwWidgetFactory.isHwDarkTheme(context);
        if (this.mIsDarkHwTheme) {
            this.mSmallTextColor = res.getColor(33882199);
            this.mNormalTextColor = res.getColor(33882200);
            return;
        }
        this.mSmallTextColor = res.getColor(33882194);
        this.mNormalTextColor = res.getColor(33882195);
    }

    private void handleFireList() {
        getInputText().setTextSize(0, this.mSelectorTextSize);
        int size = this.mFireList.size();
        for (int i = 0; i < size; i++) {
            android.widget.NumberPicker np = (android.widget.NumberPicker) this.mFireList.get(i);
            np.getInputText().setVisibility(0);
            np.invalidate();
        }
    }

    private int getAlphaGradient(int initOffset, int offset, int color) {
        float rate = 1.0f - (((float) Math.abs(initOffset - offset)) / ((float) this.mSelectorOffset));
        if (rate < 0.4f) {
            rate = 0.4f;
        }
        return (16777215 & color) | (((int) (((float) Color.alpha(color)) * rate)) << 24);
    }

    protected void playIvtEffect() {
        if (this.isVibrateImplemented && 1 == System.getInt(this.mContext_Vibrate.getContentResolver(), "touch_vibrate_mode", 1)) {
            HwGeneralManager.getInstance().playIvtEffect("NUMBERPICKER_ITEMSCROLL");
        }
    }

    protected void setLongPressState(boolean state) {
        this.mIsLongPress = state;
    }

    protected boolean needToPlayIvtEffectWhenScrolling(int scrollByY) {
        if (this.mIsLongPress || this.mScrollState != 1) {
            return false;
        }
        return Math.abs(scrollByY) > 10;
    }

    protected void playIvtEffectWhenFling(int previous, int current) {
        if (!this.mIsLongPress && this.mScrollState == 2) {
            if (this.mFlingDirection == this.FLING_FOWARD) {
                if (current > previous) {
                    playIvtEffect();
                } else {
                    this.mFlingDirection = this.FLING_STOP;
                }
            } else if (this.mFlingDirection != this.FLING_BACKWARD) {
            } else {
                if (current < previous) {
                    playIvtEffect();
                } else {
                    this.mFlingDirection = this.FLING_STOP;
                }
            }
        }
    }

    protected void setFlingDirection(int velocityY) {
        if (velocityY > 0) {
            this.mFlingDirection = this.FLING_BACKWARD;
        } else {
            this.mFlingDirection = this.FLING_FOWARD;
        }
    }

    protected float adjustYCoordinate(int i, float y) {
        if (i == 0) {
            return y + ((float) this.mEdgeOffset);
        }
        if (this.mSelectorIndices.length - 1 == i) {
            return y - ((float) this.mEdgeOffset);
        }
        return y;
    }
}
