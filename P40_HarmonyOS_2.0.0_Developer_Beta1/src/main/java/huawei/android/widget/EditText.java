package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidhwext.R;
import com.huawei.anim.dynamicanimation.DynamicAnimation;
import huawei.android.widget.plume.HwPlumeManager;

public class EditText extends android.widget.EditText {
    private static final int ALT_SHIFT_STATUS = 4;
    private static final String CURSOR_STATE = "CursorState";
    private static final int EDIT_STATUS = 2;
    private static final int FOCUSED_STATUS = 1;
    private static final String INSTANCE_STATE = "InstanceState";
    private static final int KEYCODE_INPUTMETHOD_ACTION = 746;
    private static final String TAG = "EditText";
    private static final int TOUCH_STATUS = 3;
    private static final int UNFOCUSED_STATUS = 0;
    private static final String VIEW_STATE = "ViewState";
    private Drawable mFocusDrawable;
    private boolean mIsOldViewGainFocus;
    private boolean mIsOldWindowGainFocus;
    private boolean mIsViewFocused;
    private int mStatus;

    public EditText(Context context) {
        this(context, null);
    }

    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, 16842862);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mStatus = 0;
        this.mIsViewFocused = false;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EditText);
        this.mFocusDrawable = array.getDrawable(0);
        array.recycle();
        setValueFromPlume();
    }

    private void setValueFromPlume() {
        if (!HwPlumeManager.isPlumeUsed(this.mContext)) {
            setExtendedEditEnabled(true);
        } else {
            setExtendedEditEnabled(HwPlumeManager.getInstance(this.mContext).getDefault(this, "insertEnabled", true));
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIsOldViewGainFocus = hasFocus();
        this.mIsOldWindowGainFocus = hasWindowFocus();
        this.mIsViewFocused = this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onFocusChanged(boolean hasGainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(hasGainFocus, direction, previouslyFocusedRect);
        if (isFocusChanged(hasGainFocus, this.mIsOldWindowGainFocus)) {
            this.mIsViewFocused = hasGainFocus;
        }
        this.mIsOldViewGainFocus = hasGainFocus;
        if (hasGainFocus && this.mStatus == 0) {
            if (!isInTouchMode()) {
                this.mStatus = 1;
                setCursorVisible(false);
            } else {
                this.mStatus = 3;
                setCursorVisible(true);
            }
        }
        if (!hasGainFocus) {
            if (this.mStatus != 3) {
                hideSoftInput();
            }
            this.mStatus = 0;
        }
    }

    @Override // android.widget.TextView, android.view.View
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (isFocusChanged(this.mIsOldViewGainFocus, hasWindowFocus)) {
            this.mIsViewFocused = hasWindowFocus;
        }
        this.mIsOldWindowGainFocus = hasWindowFocus;
    }

    private boolean isFocusChanged(boolean isNewViewGainFocus, boolean isNewWindowGainFocus) {
        return (this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus) != (isNewViewGainFocus && isNewWindowGainFocus);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (event.getAction() == 0) {
            setCursorVisible(true);
            this.mStatus = 3;
        }
        return super.onTouchEvent(event);
    }

    private void drawFocusLayer(Canvas canvas) {
        if (this.mFocusDrawable != null) {
            Rect rect = new Rect();
            rect.set(0, 0, getWidth(), getHeight());
            getDrawingRect(rect);
            this.mFocusDrawable.setBounds(rect);
            this.mFocusDrawable.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mStatus == 1 && this.mIsViewFocused) {
            drawFocusLayer(canvas);
        }
    }

    private boolean isConfirmKey(int keyCode) {
        if (keyCode == 23 || keyCode == 62 || keyCode == 66 || keyCode == 160) {
            return true;
        }
        return false;
    }

    private boolean isDirectionalNavigationKey(int keyCode) {
        switch (keyCode) {
            case 19:
            case 20:
            case DynamicAnimation.ANDROID_LOLLIPOP /* 21 */:
            case 22:
                return true;
            default:
                return false;
        }
    }

    private boolean isAltKey(int keyCode) {
        return keyCode == 57 || keyCode == 58;
    }

    private boolean isShiftKey(int keyCode) {
        return keyCode == 59 || keyCode == 60;
    }

    private void handleAltShiftKeyEvent(int keyCode, KeyEvent event) {
        if (this.mStatus == 4 && event.getAction() == 1 && isAltKey(keyCode)) {
            this.mStatus = 3;
        } else if (isShiftKey(keyCode) && (event.getMetaState() & 2) != 0) {
            this.mStatus = 4;
        }
    }

    private void handleTabKeyEvent(KeyEvent event) {
        View view = null;
        boolean isShiftOn = (event.getMetaState() & 1) != 0;
        FocusFinder focusFinder = FocusFinder.getInstance();
        View rootView = getRootView();
        if (rootView instanceof ViewGroup) {
            if (isShiftOn) {
                view = focusFinder.findNextFocus((ViewGroup) rootView, this, 1);
            } else {
                view = focusFinder.findNextFocus((ViewGroup) rootView, this, 2);
            }
        }
        if (view != null) {
            view.requestFocus();
        }
    }

    private void hideSoftInput() {
        Object object = getContext().getSystemService("input_method");
        if (object instanceof InputMethodManager) {
            InputMethodManager manager = (InputMethodManager) object;
            if (manager.isActive()) {
                manager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event == null) {
            return false;
        }
        if (keyCode == 4 || keyCode == 3 || keyCode == 124) {
            return super.onKeyPreIme(keyCode, event);
        }
        if (this.mStatus == 3 && keyCode != KEYCODE_INPUTMETHOD_ACTION) {
            this.mStatus = 2;
            hideSoftInput();
        }
        if (this.mStatus != 1) {
            handleAltShiftKeyEvent(keyCode, event);
            if (this.mStatus != 2 || keyCode != 111) {
                return super.onKeyPreIme(keyCode, event);
            }
            this.mStatus = 1;
            setCursorVisible(false);
            return true;
        } else if (isDirectionalNavigationKey(keyCode)) {
            return false;
        } else {
            if (keyCode == 61 && event.getAction() == 0) {
                handleTabKeyEvent(event);
            }
            if (isConfirmKey(keyCode) && event.getAction() == 1) {
                this.mStatus = 2;
                setCursorVisible(true);
            }
            return true;
        }
    }

    @Override // android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) {
            return false;
        }
        if (this.mStatus != 1 || !isDirectionalNavigationKey(event.getKeyCode())) {
            return super.dispatchKeyEvent(event);
        }
        return false;
    }

    @Override // android.widget.TextView, android.view.View
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        try {
            bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
            bundle.putInt(VIEW_STATE, this.mStatus);
            bundle.putBoolean(CURSOR_STATE, isCursorVisible());
        } catch (BadParcelableException e) {
            Log.e(TAG, "Parcelable, onSaveInstanceState error");
        }
        return bundle;
    }

    @Override // android.widget.TextView, android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            try {
                this.mStatus = bundle.getInt(VIEW_STATE);
                setCursorVisible(bundle.getBoolean(CURSOR_STATE));
                Parcelable parcelableRestoreState = bundle.getParcelable(INSTANCE_STATE);
                if (parcelableRestoreState != null) {
                    super.onRestoreInstanceState(parcelableRestoreState);
                }
            } catch (BadParcelableException e) {
                Log.e(TAG, "Parcelable, onRestoreInstanceState error");
            }
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
