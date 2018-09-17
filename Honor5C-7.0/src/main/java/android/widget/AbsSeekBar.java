package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Debug;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import huawei.cust.HwCfgFilePolicy;
import java.util.Locale;

public abstract class AbsSeekBar extends ProgressBar {
    private static final boolean DEBUG = false;
    private static final int NO_ALPHA = 255;
    private static final String TAG = "AbsSeekBar";
    private float mDisabledAlpha;
    private boolean mHasThumbTint;
    private boolean mHasThumbTintMode;
    private boolean mHasTickMarkTint;
    private boolean mHasTickMarkTintMode;
    private boolean mIsDragging;
    boolean mIsUserSeekable;
    private int mKeyProgressIncrement;
    private int mScaledTouchSlop;
    private boolean mSplitTrack;
    private final Rect mTempRect;
    private Drawable mThumb;
    protected int mThumbBottom;
    protected int mThumbLeft;
    private int mThumbOffset;
    protected int mThumbRight;
    private ColorStateList mThumbTintList;
    private Mode mThumbTintMode;
    protected int mThumbTop;
    private Drawable mTickMark;
    private ColorStateList mTickMarkTintList;
    private Mode mTickMarkTintMode;
    private float mTouchDownX;
    float mTouchProgressOffset;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.AbsSeekBar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.AbsSeekBar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsSeekBar.<clinit>():void");
    }

    public AbsSeekBar(Context context) {
        super(context);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = DEBUG;
        this.mHasThumbTintMode = DEBUG;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = DEBUG;
        this.mHasTickMarkTintMode = DEBUG;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
    }

    public AbsSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = DEBUG;
        this.mHasThumbTintMode = DEBUG;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = DEBUG;
        this.mHasTickMarkTintMode = DEBUG;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
    }

    public AbsSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = DEBUG;
        this.mHasThumbTintMode = DEBUG;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = DEBUG;
        this.mHasTickMarkTintMode = DEBUG;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBar, defStyleAttr, defStyleRes);
        setThumb(a.getDrawable(0));
        if (a.hasValue(4)) {
            this.mThumbTintMode = Drawable.parseTintMode(a.getInt(4, -1), this.mThumbTintMode);
            this.mHasThumbTintMode = true;
        }
        if (a.hasValue(3)) {
            this.mThumbTintList = a.getColorStateList(3);
            this.mHasThumbTint = true;
        }
        setTickMark(a.getDrawable(5));
        if (a.hasValue(7)) {
            this.mTickMarkTintMode = Drawable.parseTintMode(a.getInt(7, -1), this.mTickMarkTintMode);
            this.mHasTickMarkTintMode = true;
        }
        if (a.hasValue(6)) {
            this.mTickMarkTintList = a.getColorStateList(6);
            this.mHasTickMarkTint = true;
        }
        this.mSplitTrack = a.getBoolean(2, DEBUG);
        setThumbOffset(a.getDimensionPixelOffset(1, getThumbOffset()));
        boolean useDisabledAlpha = a.getBoolean(8, true);
        a.recycle();
        if (useDisabledAlpha) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Theme, 0, 0);
            this.mDisabledAlpha = ta.getFloat(3, 0.5f);
            ta.recycle();
        } else {
            this.mDisabledAlpha = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        }
        applyThumbTint();
        applyTickMarkTint();
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setThumb(Drawable thumb) {
        boolean needUpdate;
        if (this.mThumb == null || thumb == this.mThumb) {
            needUpdate = DEBUG;
        } else {
            this.mThumb.setCallback(null);
            needUpdate = true;
        }
        if (thumb != null) {
            thumb.setCallback(this);
            if (canResolveLayoutDirection()) {
                if ("ur".equals(Locale.getDefault().getLanguage())) {
                    thumb.setLayoutDirection(0);
                } else {
                    thumb.setLayoutDirection(getLayoutDirection());
                }
            }
            this.mThumbOffset = thumb.getIntrinsicWidth() / 2;
            if (needUpdate && !(thumb.getIntrinsicWidth() == this.mThumb.getIntrinsicWidth() && thumb.getIntrinsicHeight() == this.mThumb.getIntrinsicHeight())) {
                requestLayout();
            }
        }
        this.mThumb = thumb;
        applyThumbTint();
        invalidate();
        if (needUpdate) {
            updateThumbAndTrackPos(getWidth(), getHeight());
            if (thumb != null && thumb.isStateful()) {
                thumb.setState(getDrawableState());
            }
        }
    }

    public Drawable getThumb() {
        return this.mThumb;
    }

    public void setThumbTintList(ColorStateList tint) {
        this.mThumbTintList = tint;
        this.mHasThumbTint = true;
        applyThumbTint();
    }

    public ColorStateList getThumbTintList() {
        return this.mThumbTintList;
    }

    public void setThumbTintMode(Mode tintMode) {
        this.mThumbTintMode = tintMode;
        this.mHasThumbTintMode = true;
        applyThumbTint();
    }

    public Mode getThumbTintMode() {
        return this.mThumbTintMode;
    }

    private void applyThumbTint() {
        if (this.mThumb == null) {
            return;
        }
        if (this.mHasThumbTint || this.mHasThumbTintMode) {
            this.mThumb = this.mThumb.mutate();
            if (this.mHasThumbTint) {
                this.mThumb.setTintList(this.mThumbTintList);
            }
            if (this.mHasThumbTintMode) {
                this.mThumb.setTintMode(this.mThumbTintMode);
            }
            if (this.mThumb.isStateful()) {
                this.mThumb.setState(getDrawableState());
            }
        }
    }

    public int getThumbOffset() {
        return this.mThumbOffset;
    }

    public void setThumbOffset(int thumbOffset) {
        this.mThumbOffset = thumbOffset;
        invalidate();
    }

    public void setSplitTrack(boolean splitTrack) {
        this.mSplitTrack = splitTrack;
        invalidate();
    }

    public boolean getSplitTrack() {
        return this.mSplitTrack;
    }

    public void setTickMark(Drawable tickMark) {
        if (this.mTickMark != null) {
            this.mTickMark.setCallback(null);
        }
        this.mTickMark = tickMark;
        if (tickMark != null) {
            tickMark.setCallback(this);
            tickMark.setLayoutDirection(getLayoutDirection());
            if (tickMark.isStateful()) {
                tickMark.setState(getDrawableState());
            }
            applyTickMarkTint();
        }
        invalidate();
    }

    public Drawable getTickMark() {
        return this.mTickMark;
    }

    public void setTickMarkTintList(ColorStateList tint) {
        this.mTickMarkTintList = tint;
        this.mHasTickMarkTint = true;
        applyTickMarkTint();
    }

    public ColorStateList getTickMarkTintList() {
        return this.mTickMarkTintList;
    }

    public void setTickMarkTintMode(Mode tintMode) {
        this.mTickMarkTintMode = tintMode;
        this.mHasTickMarkTintMode = true;
        applyTickMarkTint();
    }

    public Mode getTickMarkTintMode() {
        return this.mTickMarkTintMode;
    }

    private void applyTickMarkTint() {
        if (this.mTickMark == null) {
            return;
        }
        if (this.mHasTickMarkTint || this.mHasTickMarkTintMode) {
            this.mTickMark = this.mTickMark.mutate();
            if (this.mHasTickMarkTint) {
                this.mTickMark.setTintList(this.mTickMarkTintList);
            }
            if (this.mHasTickMarkTintMode) {
                this.mTickMark.setTintMode(this.mTickMarkTintMode);
            }
            if (this.mTickMark.isStateful()) {
                this.mTickMark.setState(getDrawableState());
            }
        }
    }

    public void setKeyProgressIncrement(int increment) {
        if (increment < 0) {
            increment = -increment;
        }
        this.mKeyProgressIncrement = increment;
    }

    public int getKeyProgressIncrement() {
        return this.mKeyProgressIncrement;
    }

    public synchronized void setMax(int max) {
        super.setMax(max);
        if (this.mKeyProgressIncrement == 0 || getMax() / this.mKeyProgressIncrement > 20) {
            setKeyProgressIncrement(Math.max(1, Math.round(((float) getMax()) / 20.0f)));
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return (who == this.mThumb || who == this.mTickMark) ? true : super.verifyDrawable(who);
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mThumb != null) {
            this.mThumb.jumpToCurrentState();
        }
        if (this.mTickMark != null) {
            this.mTickMark.jumpToCurrentState();
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null && this.mDisabledAlpha < LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            progressDrawable.setAlpha(isEnabled() ? NO_ALPHA : (int) (this.mDisabledAlpha * 255.0f));
        }
        Drawable thumb = this.mThumb;
        if (thumb != null && thumb.isStateful() && thumb.setState(getDrawableState())) {
            invalidateDrawable(thumb);
        }
        Drawable tickMark = this.mTickMark;
        if (tickMark != null && tickMark.isStateful() && tickMark.setState(getDrawableState())) {
            invalidateDrawable(tickMark);
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mThumb != null) {
            this.mThumb.setHotspot(x, y);
        }
    }

    void onVisualProgressChanged(int id, float scale) {
        super.onVisualProgressChanged(id, scale);
        if (DEBUG) {
            Log.d(TAG, "xProgress = " + (((float) ((getWidth() - this.mPaddingLeft) - this.mPaddingRight)) * scale) + "\n onVisualProgressChanged: Callers=" + Debug.getCallers(5));
        }
        if (id == R.id.progress) {
            Drawable thumb = this.mThumb;
            if (thumb != null) {
                setThumbPos(getWidth(), thumb, scale, RtlSpacingHelper.UNDEFINED);
                invalidate();
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateThumbAndTrackPos(w, h);
    }

    private void updateThumbAndTrackPos(int w, int h) {
        int trackOffset;
        int thumbOffset;
        int paddedHeight = (h - this.mPaddingTop) - this.mPaddingBottom;
        Drawable track = getCurrentDrawable();
        Drawable thumb = this.mThumb;
        int trackHeight = Math.min(this.mMaxHeight, paddedHeight);
        int thumbHeight = thumb == null ? 0 : thumb.getIntrinsicHeight();
        int offsetHeight;
        if (thumbHeight > trackHeight) {
            offsetHeight = (paddedHeight - thumbHeight) / 2;
            trackOffset = offsetHeight + ((thumbHeight - trackHeight) / 2);
            thumbOffset = offsetHeight;
        } else {
            offsetHeight = (paddedHeight - trackHeight) / 2;
            trackOffset = offsetHeight;
            thumbOffset = offsetHeight + ((trackHeight - thumbHeight) / 2);
        }
        if (track != null) {
            track.setBounds(0, trackOffset, (w - this.mPaddingRight) - this.mPaddingLeft, trackOffset + trackHeight);
        }
        if (thumb != null) {
            setThumbPos(w, thumb, getScale(), thumbOffset);
        }
    }

    protected float getScale() {
        int max = getMax();
        return max > 0 ? ((float) getProgress()) / ((float) max) : 0.0f;
    }

    private void setThumbPos(int w, Drawable thumb, float scale, int offset) {
        int top;
        int bottom;
        int available = (w - this.mPaddingLeft) - this.mPaddingRight;
        int thumbWidth = thumb.getIntrinsicWidth();
        int thumbHeight = thumb.getIntrinsicHeight();
        available = (available - thumbWidth) + (this.mThumbOffset * 2);
        int thumbPos = (int) ((((float) available) * scale) + 0.5f);
        if (offset == Integer.MIN_VALUE) {
            Rect oldBounds = thumb.getBounds();
            top = oldBounds.top;
            bottom = oldBounds.bottom;
        } else {
            top = offset;
            bottom = offset + thumbHeight;
        }
        int left = (isLayoutRtl() && this.mMirrorForRtl && !"ur".equals(Locale.getDefault().getLanguage())) ? available - thumbPos : thumbPos;
        int right = left + thumbWidth;
        Drawable background = getBackground();
        if (background != null) {
            int offsetX = this.mPaddingLeft - this.mThumbOffset;
            int offsetY = this.mPaddingTop;
            background.setHotspotBounds(left + offsetX, top + offsetY, right + offsetX, bottom + offsetY);
        }
        this.mThumbLeft = left;
        this.mThumbTop = top;
        this.mThumbRight = right;
        this.mThumbBottom = bottom;
        thumb.setBounds(left, top, right, bottom);
        if (DEBUG) {
            Log.d(TAG, "mThumbLeft = " + left + ";mThumbRight = " + right + " \n setThumbPos: Callers=" + Debug.getCallers(5));
        }
    }

    public void onResolveDrawables(int layoutDirection) {
        super.onResolveDrawables(layoutDirection);
        if (this.mThumb == null) {
            return;
        }
        if ("ur".equals(Locale.getDefault().getLanguage())) {
            this.mThumb.setLayoutDirection(0);
        } else {
            this.mThumb.setLayoutDirection(layoutDirection);
        }
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawThumb(canvas);
    }

    protected void drawTrack(Canvas canvas) {
        Drawable thumbDrawable = this.mThumb;
        if (thumbDrawable == null || !this.mSplitTrack) {
            super.drawTrack(canvas);
            drawTickMarks(canvas);
            return;
        }
        Insets insets = thumbDrawable.getOpticalInsets();
        Rect tempRect = this.mTempRect;
        thumbDrawable.copyBounds(tempRect);
        tempRect.offset(this.mPaddingLeft - this.mThumbOffset, this.mPaddingTop);
        tempRect.left += insets.left;
        tempRect.right -= insets.right;
        int saveCount = canvas.save();
        canvas.clipRect(tempRect, Op.DIFFERENCE);
        super.drawTrack(canvas);
        drawTickMarks(canvas);
        canvas.restoreToCount(saveCount);
    }

    void drawTickMarks(Canvas canvas) {
        if (this.mTickMark != null) {
            int count = getMax();
            if (count > 1) {
                int w = this.mTickMark.getIntrinsicWidth();
                int h = this.mTickMark.getIntrinsicHeight();
                int halfW = w >= 0 ? w / 2 : 1;
                int halfH = h >= 0 ? h / 2 : 1;
                this.mTickMark.setBounds(-halfW, -halfH, halfW, halfH);
                float spacing = ((float) ((getWidth() - this.mPaddingLeft) - this.mPaddingRight)) / ((float) count);
                int saveCount = canvas.save();
                canvas.translate((float) this.mPaddingLeft, (float) (getHeight() / 2));
                for (int i = 0; i <= count; i++) {
                    this.mTickMark.draw(canvas);
                    canvas.translate(spacing, 0.0f);
                }
                canvas.restoreToCount(saveCount);
            }
        }
    }

    void drawThumb(Canvas canvas) {
        if (this.mThumb != null) {
            int saveCount = canvas.save();
            canvas.translate((float) (this.mPaddingLeft - this.mThumbOffset), (float) this.mPaddingTop);
            this.mThumb.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getCurrentDrawable();
        int thumbHeight = this.mThumb == null ? 0 : this.mThumb.getIntrinsicHeight();
        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(thumbHeight, Math.max(this.mMinHeight, Math.min(this.mMaxHeight, d.getIntrinsicHeight())));
        }
        setMeasuredDimension(View.resolveSizeAndState(dw + (this.mPaddingLeft + this.mPaddingRight), widthMeasureSpec, 0), View.resolveSizeAndState(dh + (this.mPaddingTop + this.mPaddingBottom), heightMeasureSpec, 0));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mIsUserSeekable || !isEnabled()) {
            return DEBUG;
        }
        switch (event.getAction()) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                if (!isInScrollingContainer()) {
                    startDrag(event);
                    break;
                }
                this.mTouchDownX = event.getX();
                break;
            case HwCfgFilePolicy.EMUI /*1*/:
                if (this.mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(DEBUG);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                invalidate();
                break;
            case HwCfgFilePolicy.PC /*2*/:
                if (!this.mIsDragging) {
                    if (Math.abs(event.getX() - this.mTouchDownX) > ((float) this.mScaledTouchSlop)) {
                        startDrag(event);
                        break;
                    }
                }
                trackTouchEvent(event);
                break;
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                if (this.mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(DEBUG);
                }
                invalidate();
                break;
        }
        return true;
    }

    private void startDrag(MotionEvent event) {
        setPressed(true);
        if (this.mThumb != null) {
            invalidate(this.mThumb.getBounds());
        }
        onStartTrackingTouch();
        trackTouchEvent(event);
        attemptClaimDrag();
    }

    private void setHotspot(float x, float y) {
        Drawable bg = getBackground();
        if (bg != null) {
            bg.setHotspot(x, y);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        float scale;
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());
        int width = getWidth();
        int availableWidth = (width - this.mPaddingLeft) - this.mPaddingRight;
        float progress = 0.0f;
        if (isLayoutRtl() && this.mMirrorForRtl && !"ur".equals(Locale.getDefault().getLanguage())) {
            if (x > width - this.mPaddingRight) {
                scale = 0.0f;
            } else if (x < this.mPaddingLeft) {
                scale = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            } else {
                scale = ((float) ((availableWidth - x) + this.mPaddingLeft)) / ((float) availableWidth);
                progress = this.mTouchProgressOffset;
            }
        } else if (x < this.mPaddingLeft) {
            scale = 0.0f;
        } else if (x > width - this.mPaddingRight) {
            scale = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        } else {
            scale = ((float) (x - this.mPaddingLeft)) / ((float) availableWidth);
            progress = this.mTouchProgressOffset;
        }
        progress += ((float) getMax()) * scale;
        setHotspot((float) x, (float) y);
        setProgressInternal(Math.round(progress), true, DEBUG);
    }

    private void attemptClaimDrag() {
        if (this.mParent != null) {
            this.mParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    void onStartTrackingTouch() {
        this.mIsDragging = true;
    }

    void onStopTrackingTouch() {
        this.mIsDragging = DEBUG;
    }

    void onKeyChange() {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isEnabled()) {
            int increment = this.mKeyProgressIncrement;
            switch (keyCode) {
                case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                case RILConstants.RIL_REQUEST_STK_SEND_ENVELOPE_COMMAND /*69*/:
                    increment = -increment;
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                case StatisticalConstant.TYPE_NAVIGATIONBAR_END /*70*/:
                case StatisticalConstant.TYPE_TOUCH_FORCE_CALL_NAVIGATION /*81*/:
                    break;
            }
            if (isLayoutRtl()) {
                increment = -increment;
            }
            if (setProgressInternal(getProgress() + increment, true, true)) {
                onKeyChange();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public CharSequence getAccessibilityClassName() {
        return AbsSeekBar.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            int progress = getProgress();
            if (progress > 0) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
            }
            if (progress < getMax()) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
            }
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (!isEnabled()) {
            return DEBUG;
        }
        switch (action) {
            case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
                if (!canUserSetProgress()) {
                    return DEBUG;
                }
                int increment = Math.max(1, Math.round(((float) getMax()) / 20.0f));
                if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
                    increment = -increment;
                }
                if (!setProgressInternal(getProgress() + increment, true, true)) {
                    return DEBUG;
                }
                onKeyChange();
                return true;
            case R.id.accessibilityActionSetProgress /*16908349*/:
                if (canUserSetProgress() && arguments != null && arguments.containsKey(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE)) {
                    return setProgressInternal((int) arguments.getFloat(AccessibilityNodeInfo.ACTION_ARGUMENT_PROGRESS_VALUE), true, true);
                }
                return DEBUG;
            default:
                return DEBUG;
        }
    }

    boolean canUserSetProgress() {
        return !isIndeterminate() ? isEnabled() : DEBUG;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        if ("ur".equals(Locale.getDefault().getLanguage()) && layoutDirection == 1) {
            layoutDirection = 0;
        }
        super.onRtlPropertiesChanged(layoutDirection);
        Drawable thumb = this.mThumb;
        if (thumb != null) {
            setThumbPos(getWidth(), thumb, getScale(), RtlSpacingHelper.UNDEFINED);
            invalidate();
        }
    }
}
