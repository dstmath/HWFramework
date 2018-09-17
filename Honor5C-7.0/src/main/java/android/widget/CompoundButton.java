package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.R;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.log.LogPower;
import com.nxp.nfc.gsma.internal.NxpNfcController;

public abstract class CompoundButton extends Button implements Checkable {
    private static final int[] CHECKED_STATE_SET = null;
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private ColorStateList mButtonTintList;
    private Mode mButtonTintMode;
    private boolean mChecked;
    private boolean mHasButtonTint;
    private boolean mHasButtonTintMode;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
    private int mUserButtonDrwableHeight;
    private int mUserButtonDrwableWidth;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CompoundButton compoundButton, boolean z);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        boolean checked;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.CompoundButton.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.CompoundButton.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.CompoundButton.SavedState.<clinit>():void");
        }

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
            return "CompoundButton.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " checked=" + this.checked + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.CompoundButton.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.CompoundButton.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.CompoundButton.<clinit>():void");
    }

    public CompoundButton(Context context) {
        this(context, null);
    }

    public CompoundButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CompoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mButtonTintList = null;
        this.mButtonTintMode = null;
        this.mHasButtonTint = false;
        this.mHasButtonTintMode = false;
        this.mUserButtonDrwableHeight = 0;
        this.mUserButtonDrwableWidth = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompoundButton, defStyleAttr, defStyleRes);
        Drawable d = a.getDrawable(1);
        if (d != null) {
            setButtonDrawable(d);
        }
        if (a.hasValue(3)) {
            this.mButtonTintMode = Drawable.parseTintMode(a.getInt(3, -1), this.mButtonTintMode);
            this.mHasButtonTintMode = true;
        }
        if (a.hasValue(2)) {
            this.mButtonTintList = a.getColorStateList(2);
            this.mHasButtonTint = true;
        }
        setChecked(a.getBoolean(0, false));
        a.recycle();
        applyButtonTint();
    }

    public void toggle() {
        setChecked(!this.mChecked);
    }

    public boolean performClick() {
        toggle();
        boolean handled = super.performClick();
        if (!handled) {
            playSoundEffect(0);
        }
        return handled;
    }

    @ExportedProperty
    public boolean isChecked() {
        return this.mChecked;
    }

    public void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            refreshDrawableState();
            notifyViewAccessibilityStateChangedIfNeeded(0);
            if (!this.mBroadcasting) {
                this.mBroadcasting = true;
                if (this.mOnCheckedChangeListener != null) {
                    this.mOnCheckedChangeListener.onCheckedChanged(this, this.mChecked);
                }
                if (this.mOnCheckedChangeWidgetListener != null) {
                    this.mOnCheckedChangeWidgetListener.onCheckedChanged(this, this.mChecked);
                }
                this.mBroadcasting = false;
            }
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeWidgetListener = listener;
    }

    public void setButtonDrawable(int resId) {
        Drawable drawable;
        if (resId != 0) {
            drawable = getContext().getDrawable(resId);
        } else {
            drawable = null;
        }
        setButtonDrawable(drawable);
    }

    public void setButtonDrawable(Drawable drawable) {
        if (this.mButtonDrawable != drawable) {
            if (this.mButtonDrawable != null) {
                this.mButtonDrawable.setCallback(null);
                unscheduleDrawable(this.mButtonDrawable);
            }
            this.mButtonDrawable = drawable;
            if (drawable != null) {
                drawable.setCallback(this);
                drawable.setLayoutDirection(getLayoutDirection());
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                drawable.setVisible(getVisibility() == 0, false);
                setMinHeight(drawable.getIntrinsicHeight());
                applyButtonTint();
            }
        }
    }

    public void onResolveDrawables(int layoutDirection) {
        super.onResolveDrawables(layoutDirection);
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.setLayoutDirection(layoutDirection);
        }
    }

    public Drawable getButtonDrawable() {
        return this.mButtonDrawable;
    }

    public void setButtonTintList(ColorStateList tint) {
        this.mButtonTintList = tint;
        this.mHasButtonTint = true;
        applyButtonTint();
    }

    public ColorStateList getButtonTintList() {
        return this.mButtonTintList;
    }

    public void setButtonTintMode(Mode tintMode) {
        this.mButtonTintMode = tintMode;
        this.mHasButtonTintMode = true;
        applyButtonTint();
    }

    public Mode getButtonTintMode() {
        return this.mButtonTintMode;
    }

    private void applyButtonTint() {
        if (this.mButtonDrawable == null) {
            return;
        }
        if (this.mHasButtonTint || this.mHasButtonTintMode) {
            this.mButtonDrawable = this.mButtonDrawable.mutate();
            if (this.mHasButtonTint) {
                this.mButtonDrawable.setTintList(this.mButtonTintList);
            }
            if (this.mHasButtonTintMode) {
                this.mButtonDrawable.setTintMode(this.mButtonTintMode);
            }
            if (this.mButtonDrawable.isStateful()) {
                this.mButtonDrawable.setState(getDrawableState());
            }
        }
    }

    public CharSequence getAccessibilityClassName() {
        return CompoundButton.class.getName();
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

    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (isRtlLocale()) {
            return padding;
        }
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            return padding + buttonDrawable.getIntrinsicWidth();
        }
        return padding;
    }

    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        if (!isRtlLocale()) {
            return padding;
        }
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            return padding + buttonDrawable.getIntrinsicWidth();
        }
        return padding;
    }

    public int getHorizontalOffsetForDrawables() {
        Drawable buttonDrawable = this.mButtonDrawable;
        return buttonDrawable != null ? buttonDrawable.getIntrinsicWidth() : 0;
    }

    protected void onDraw(Canvas canvas) {
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null) {
            int top;
            int right;
            int verticalGravity = getGravity() & LogPower.APP_PROCESS_EXIT;
            int drawableHeight = this.mUserButtonDrwableHeight == 0 ? buttonDrawable.getIntrinsicHeight() : this.mUserButtonDrwableHeight;
            int drawableWidth = this.mUserButtonDrwableWidth == 0 ? buttonDrawable.getIntrinsicWidth() : this.mUserButtonDrwableWidth;
            switch (verticalGravity) {
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    top = (getHeight() - drawableHeight) / 2;
                    break;
                case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                    top = getHeight() - drawableHeight;
                    break;
                default:
                    top = 0;
                    break;
            }
            int bottom = top + drawableHeight;
            int left = isRtlLocale() ? getWidth() - drawableWidth : 0;
            if (isRtlLocale()) {
                right = getWidth();
            } else {
                right = drawableWidth;
            }
            buttonDrawable.setBounds(left, top, right, bottom);
            Drawable background = getBackground();
            if (background != null) {
                background.setHotspotBounds(left, top, right, bottom);
            }
        }
        super.onDraw(canvas);
        if (buttonDrawable != null) {
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            if (scrollX == 0 && scrollY == 0) {
                buttonDrawable.draw(canvas);
                return;
            }
            canvas.translate((float) scrollX, (float) scrollY);
            buttonDrawable.draw(canvas);
            canvas.translate((float) (-scrollX), (float) (-scrollY));
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable buttonDrawable = this.mButtonDrawable;
        if (buttonDrawable != null && buttonDrawable.isStateful() && buttonDrawable.setState(getDrawableState())) {
            invalidateDrawable(buttonDrawable);
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.setHotspot(x, y);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mButtonDrawable;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mButtonDrawable != null) {
            this.mButtonDrawable.jumpToCurrentState();
        }
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

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        stream.addProperty("checked", isChecked());
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return this.mOnCheckedChangeListener;
    }

    protected void setButtonDrawableHeight(int height) {
        this.mUserButtonDrwableHeight = height;
    }

    protected void setButtonDrawableWidth(int width) {
        this.mUserButtonDrwableWidth = width;
    }
}
