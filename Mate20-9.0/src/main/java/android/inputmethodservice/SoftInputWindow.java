package android.inputmethodservice;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

public class SoftInputWindow extends Dialog {
    private final Rect mBounds = new Rect();
    final Callback mCallback;
    final KeyEvent.DispatcherState mDispatcherState;
    final int mGravity;
    final KeyEvent.Callback mKeyEventCallback;
    final String mName;
    final boolean mTakesFocus;
    final int mWindowType;

    public interface Callback {
        void onBackPressed();
    }

    public void setToken(IBinder token) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.token = token;
        getWindow().setAttributes(lp);
    }

    public SoftInputWindow(Context context, String name, int theme, Callback callback, KeyEvent.Callback keyEventCallback, KeyEvent.DispatcherState dispatcherState, int windowType, int gravity, boolean takesFocus) {
        super(context, theme);
        this.mName = name;
        this.mCallback = callback;
        this.mKeyEventCallback = keyEventCallback;
        this.mDispatcherState = dispatcherState;
        this.mWindowType = windowType;
        this.mGravity = gravity;
        this.mTakesFocus = takesFocus;
        initDockWindow();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.mDispatcherState.reset();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        getWindow().getDecorView().getHitRect(this.mBounds);
        if (ev.isWithinBoundsNoHistory((float) this.mBounds.left, (float) this.mBounds.top, (float) (this.mBounds.right - 1), (float) (this.mBounds.bottom - 1))) {
            return super.dispatchTouchEvent(ev);
        }
        MotionEvent temp = ev.clampNoHistory((float) this.mBounds.left, (float) this.mBounds.top, (float) (this.mBounds.right - 1), (float) (this.mBounds.bottom - 1));
        boolean handled = super.dispatchTouchEvent(temp);
        temp.recycle();
        return handled;
    }

    public void setGravity(int gravity) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = gravity;
        updateWidthHeight(lp);
        getWindow().setAttributes(lp);
    }

    public int getGravity() {
        return getWindow().getAttributes().gravity;
    }

    private void updateWidthHeight(WindowManager.LayoutParams lp) {
        if (lp.gravity == 48 || lp.gravity == 80) {
            lp.width = -1;
            lp.height = -2;
            return;
        }
        lp.width = -2;
        lp.height = -1;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mKeyEventCallback == null || !this.mKeyEventCallback.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (this.mKeyEventCallback == null || !this.mKeyEventCallback.onKeyLongPress(keyCode, event)) {
            return super.onKeyLongPress(keyCode, event);
        }
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mKeyEventCallback == null || !this.mKeyEventCallback.onKeyUp(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        if (this.mKeyEventCallback == null || !this.mKeyEventCallback.onKeyMultiple(keyCode, count, event)) {
            return super.onKeyMultiple(keyCode, count, event);
        }
        return true;
    }

    public void onBackPressed() {
        if (this.mCallback != null) {
            this.mCallback.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    private void initDockWindow() {
        int windowSetFlags;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.type = this.mWindowType;
        lp.setTitle(this.mName);
        lp.gravity = this.mGravity;
        updateWidthHeight(lp);
        getWindow().setAttributes(lp);
        int windowModFlags = 266;
        if (!this.mTakesFocus) {
            windowSetFlags = 256 | 8;
        } else {
            windowSetFlags = 256 | 32;
            windowModFlags = 266 | 32;
        }
        getWindow().setFlags(windowSetFlags, windowModFlags);
    }
}
