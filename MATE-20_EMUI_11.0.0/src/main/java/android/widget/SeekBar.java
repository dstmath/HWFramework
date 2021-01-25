package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import huawei.android.widget.HwOnChangeProgressListener;
import java.util.Locale;

public class SeekBar extends AbsSeekBar {
    private static final String LANGUAGE_URDU = "ur";
    private static final int SEEKBAR_TIPS_DISMISS_TIME = 500;
    private HwGenericEventDetector mHwGenericEventDetector;
    private HwOnChangeProgressListener mOnGenericMotionProgressChangedListener;
    @UnsupportedAppUsage
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private Runnable mStopTrackRunable;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

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
        this.mHwGenericEventDetector = null;
        this.mOnGenericMotionProgressChangedListener = new HwOnChangeProgressListener() {
            /* class android.widget.SeekBar.AnonymousClass1 */

            @Override // huawei.android.widget.HwOnChangeProgressListener
            public boolean onChangeProgress(int deltaX, int deltaY, MotionEvent event) {
                if (Float.compare((float) deltaX, 0.0f) == 0) {
                    SeekBar.this.changeProgressWithGenericMotion(deltaY);
                    return true;
                } else if (!SeekBar.this.isLayoutRtl() || SeekBar.LANGUAGE_URDU.equals(Locale.getDefault().getLanguage())) {
                    SeekBar.this.changeProgressWithGenericMotion(deltaX);
                    return true;
                } else {
                    SeekBar.this.changeProgressWithGenericMotion(-deltaX);
                    return true;
                }
            }
        };
        this.mStopTrackRunable = new Runnable() {
            /* class android.widget.SeekBar.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                SeekBar.this.onHwStopTrackingTouch();
                SeekBar.this.removeCallbacks(this);
            }
        };
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.ProgressBar
    @UnsupportedAppUsage
    public void onProgressRefresh(float scale, boolean fromUser, int progress) {
        super.onProgressRefresh(scale, fromUser, progress);
        onProgressRefreshEx(scale, fromUser, progress);
        OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        this.mOnSeekBarChangeListener = l;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsSeekBar
    public void onStartTrackingTouch() {
        super.onStartTrackingTouch();
        OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onStartTrackingTouch(this);
        }
        onHwStartTrackingTouch();
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsSeekBar
    public void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onStopTrackingTouch(this);
        }
        onHwStopTrackingTouch();
    }

    @Override // android.widget.AbsSeekBar, android.widget.ProgressBar, android.view.View
    public CharSequence getAccessibilityClassName() {
        return SeekBar.class.getName();
    }

    @Override // android.widget.AbsSeekBar, android.widget.ProgressBar, android.view.View
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (canUserSetProgress()) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
        }
    }

    /* access modifiers changed from: protected */
    public void onHwStartTrackingTouch() {
    }

    /* access modifiers changed from: protected */
    public void onHwStopTrackingTouch() {
    }

    public void setTip(boolean setLabelling, int stepNum, boolean isBubbleTip) {
    }

    public void setTipText(String tipText) {
    }

    /* access modifiers changed from: protected */
    public void onProgressRefreshEx(float scale, boolean fromUser, int progress) {
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector == null || !hwGenericEventDetector.onGenericMotionEvent(event)) {
            return super.onGenericMotionEvent(event);
        }
        return true;
    }

    private void trackGenericEvent(int targetProgress) {
        if ((getWidth() - getPaddingLeft()) - getPaddingRight() <= 0) {
            setProgress(0);
        } else {
            setProgress(targetProgress);
        }
    }

    /* access modifiers changed from: protected */
    public void changeProgressWithGenericMotion(int delta) {
        removeCallbacks(this.mStopTrackRunable);
        onHwStartTrackingTouch();
        trackGenericEvent(getProgress() + delta);
        postDelayed(this.mStopTrackRunable, 500);
    }

    public void setExtendProgressEnabled(boolean isEnabled) {
        if (!isEnabled) {
            this.mHwGenericEventDetector = null;
            return;
        }
        if (this.mHwGenericEventDetector == null) {
            this.mHwGenericEventDetector = HwWidgetFactory.getGenericEventDetector(getContext());
        }
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector != null) {
            hwGenericEventDetector.setOnChangeProgressListener(this.mOnGenericMotionProgressChangedListener);
        }
    }

    public boolean isExtendProgressEnabled() {
        return this.mHwGenericEventDetector != null;
    }
}
