package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.R;

public class CheckedTextView extends TextView implements Checkable {
    private static final int[] CHECKED_STATE_SET = {16842912};
    private int mBasePadding;
    private Drawable mCheckMarkDrawable;
    private int mCheckMarkGravity;
    private int mCheckMarkResource;
    private ColorStateList mCheckMarkTintList;
    private PorterDuff.Mode mCheckMarkTintMode;
    private int mCheckMarkWidth;
    private boolean mChecked;
    private boolean mHasCheckMarkTint;
    private boolean mHasCheckMarkTintMode;
    private boolean mNeedRequestlayout;

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.checked = ((Boolean) in.readValue(null)).booleanValue();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(Boolean.valueOf(this.checked));
        }

        public String toString() {
            return "CheckedTextView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " checked=" + this.checked + "}";
        }
    }

    public CheckedTextView(Context context) {
        this(context, null);
    }

    public CheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843720);
    }

    public CheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCheckMarkTintList = null;
        this.mCheckMarkTintMode = null;
        this.mHasCheckMarkTint = false;
        this.mHasCheckMarkTintMode = false;
        this.mCheckMarkGravity = Gravity.END;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckedTextView, defStyleAttr, defStyleRes);
        Drawable d = HwWidgetFactory.getDesireCheckMarkDrawable(context, a, 1);
        if (d != null) {
            setCheckMarkDrawable(d);
        }
        if (a.hasValue(3)) {
            this.mCheckMarkTintMode = Drawable.parseTintMode(a.getInt(3, -1), this.mCheckMarkTintMode);
            this.mHasCheckMarkTintMode = true;
        }
        if (a.hasValue(2)) {
            this.mCheckMarkTintList = a.getColorStateList(2);
            this.mHasCheckMarkTint = true;
        }
        this.mCheckMarkGravity = a.getInt(4, Gravity.END);
        setChecked(a.getBoolean(0, false));
        a.recycle();
        applyCheckMarkTint();
    }

    public void toggle() {
        setChecked(!this.mChecked);
    }

    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return this.mChecked;
    }

    public void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            refreshDrawableState();
            notifyViewAccessibilityStateChangedIfNeeded(0);
        }
    }

    public void setCheckMarkDrawable(int resId) {
        if (resId == 0 || resId != this.mCheckMarkResource) {
            setCheckMarkDrawableInternal(resId != 0 ? getContext().getDrawable(resId) : null, resId);
        }
    }

    public void setCheckMarkDrawable(Drawable d) {
        setCheckMarkDrawableInternal(d, 0);
    }

    private void setCheckMarkDrawableInternal(Drawable d, int resId) {
        if (this.mCheckMarkDrawable != null) {
            this.mCheckMarkDrawable.setCallback(null);
            unscheduleDrawable(this.mCheckMarkDrawable);
        }
        boolean z = true;
        this.mNeedRequestlayout = d != this.mCheckMarkDrawable;
        if (d != null) {
            d.setCallback(this);
            if (getVisibility() != 0) {
                z = false;
            }
            d.setVisible(z, false);
            d.setState(CHECKED_STATE_SET);
            setMinHeight(d.getIntrinsicHeight());
            this.mCheckMarkWidth = d.getIntrinsicWidth();
            d.setState(getDrawableState());
        } else {
            this.mCheckMarkWidth = 0;
        }
        this.mCheckMarkDrawable = d;
        this.mCheckMarkResource = resId;
        applyCheckMarkTint();
        resolvePadding();
    }

    public void setCheckMarkTintList(ColorStateList tint) {
        this.mCheckMarkTintList = tint;
        this.mHasCheckMarkTint = true;
        applyCheckMarkTint();
    }

    public ColorStateList getCheckMarkTintList() {
        return this.mCheckMarkTintList;
    }

    public void setCheckMarkTintMode(PorterDuff.Mode tintMode) {
        this.mCheckMarkTintMode = tintMode;
        this.mHasCheckMarkTintMode = true;
        applyCheckMarkTint();
    }

    public PorterDuff.Mode getCheckMarkTintMode() {
        return this.mCheckMarkTintMode;
    }

    private void applyCheckMarkTint() {
        if (this.mCheckMarkDrawable == null) {
            return;
        }
        if (this.mHasCheckMarkTint || this.mHasCheckMarkTintMode) {
            this.mCheckMarkDrawable = this.mCheckMarkDrawable.mutate();
            if (this.mHasCheckMarkTint) {
                this.mCheckMarkDrawable.setTintList(this.mCheckMarkTintList);
            }
            if (this.mHasCheckMarkTintMode) {
                this.mCheckMarkDrawable.setTintMode(this.mCheckMarkTintMode);
            }
            if (this.mCheckMarkDrawable.isStateful()) {
                this.mCheckMarkDrawable.setState(getDrawableState());
            }
        }
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mCheckMarkDrawable != null) {
            this.mCheckMarkDrawable.setVisible(visibility == 0, false);
        }
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mCheckMarkDrawable != null) {
            this.mCheckMarkDrawable.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable who) {
        return who == this.mCheckMarkDrawable || super.verifyDrawable(who);
    }

    public Drawable getCheckMarkDrawable() {
        return this.mCheckMarkDrawable;
    }

    /* access modifiers changed from: protected */
    public void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        setBasePadding(isCheckMarkAtStart());
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updatePadding();
    }

    private void updatePadding() {
        resetPaddingToInitialValues();
        int newPadding = this.mCheckMarkDrawable != null ? this.mCheckMarkWidth + this.mBasePadding : this.mBasePadding;
        boolean z = true;
        if (isCheckMarkAtStart()) {
            boolean z2 = this.mNeedRequestlayout;
            if (this.mPaddingLeft == newPadding) {
                z = false;
            }
            this.mNeedRequestlayout = z2 | z;
            this.mPaddingLeft = newPadding;
        } else {
            boolean z3 = this.mNeedRequestlayout;
            if (this.mPaddingRight == newPadding) {
                z = false;
            }
            this.mNeedRequestlayout = z3 | z;
            this.mPaddingRight = newPadding;
        }
        if (this.mNeedRequestlayout) {
            requestLayout();
            this.mNeedRequestlayout = false;
        }
    }

    private void setBasePadding(boolean checkmarkAtStart) {
        if (checkmarkAtStart) {
            this.mBasePadding = this.mPaddingLeft;
        } else {
            this.mBasePadding = this.mPaddingRight;
        }
    }

    public boolean isCheckMarkAtStartEx() {
        return isCheckMarkAtStart();
    }

    private boolean isCheckMarkAtStart() {
        return (Gravity.getAbsoluteGravity(this.mCheckMarkGravity, getLayoutDirection()) & 7) == 3;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int right;
        int left;
        super.onDraw(canvas);
        Drawable checkMarkDrawable = this.mCheckMarkDrawable;
        if (checkMarkDrawable != null) {
            int verticalGravity = getGravity() & 112;
            int height = checkMarkDrawable.getIntrinsicHeight();
            int y = 0;
            if (verticalGravity == 16) {
                y = (getHeight() - height) / 2;
            } else if (verticalGravity == 80) {
                y = getHeight() - height;
            }
            boolean checkMarkAtStart = isCheckMarkAtStart();
            int width = getWidth();
            int top = y;
            int bottom = top + height;
            if (checkMarkAtStart) {
                left = this.mBasePadding;
                right = this.mCheckMarkWidth + left;
            } else {
                right = width - this.mBasePadding;
                left = right - this.mCheckMarkWidth;
            }
            checkMarkDrawable.setBounds(this.mScrollX + left, top, this.mScrollX + right, bottom);
            checkMarkDrawable.draw(canvas);
            Drawable background = getBackground();
            if (background != null) {
                background.setHotspotBounds(this.mScrollX + left, top, this.mScrollX + right, bottom);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable checkMarkDrawable = this.mCheckMarkDrawable;
        if (checkMarkDrawable != null && checkMarkDrawable.isStateful() && checkMarkDrawable.setState(getDrawableState())) {
            invalidateDrawable(checkMarkDrawable);
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mCheckMarkDrawable != null) {
            this.mCheckMarkDrawable.setHotspot(x, y);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return CheckedTextView.class.getName();
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.checked = isChecked();
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setChecked(this.mChecked);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setCheckable(true);
        info.setChecked(this.mChecked);
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        stream.addProperty("text:checked", isChecked());
    }
}
