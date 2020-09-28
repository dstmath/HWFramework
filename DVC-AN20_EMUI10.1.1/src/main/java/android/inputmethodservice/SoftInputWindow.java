package android.inputmethodservice;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SoftInputWindow extends Dialog {
    private static final boolean DEBUG = false;
    private static final String TAG = "SoftInputWindow";
    private final Rect mBounds = new Rect();
    final Callback mCallback;
    final KeyEvent.DispatcherState mDispatcherState;
    final int mGravity;
    final KeyEvent.Callback mKeyEventCallback;
    final String mName;
    final boolean mTakesFocus;
    private int mWindowState = 0;
    final int mWindowType;

    public interface Callback {
        void onBackPressed();
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface SoftInputWindowState {
        public static final int DESTROYED = 4;
        public static final int REJECTED_AT_LEAST_ONCE = 3;
        public static final int SHOWN_AT_LEAST_ONCE = 2;
        public static final int TOKEN_PENDING = 0;
        public static final int TOKEN_SET = 1;
    }

    public void setToken(IBinder token) {
        int i = this.mWindowState;
        if (i == 0) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.token = token;
            getWindow().setAttributes(lp);
            updateWindowState(1);
        } else if (i == 1 || i == 2 || i == 3) {
            throw new IllegalStateException("setToken can be called only once");
        } else if (i == 4) {
            Log.i(TAG, "Ignoring setToken() because window is already destroyed.");
        } else {
            throw new IllegalStateException("Unexpected state=" + this.mWindowState);
        }
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

    @Override // android.app.Dialog, android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.mDispatcherState.reset();
    }

    @Override // android.app.Dialog, android.view.Window.Callback
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

    @Override // android.view.KeyEvent.Callback, android.app.Dialog
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        KeyEvent.Callback callback = this.mKeyEventCallback;
        if (callback == null || !callback.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.app.Dialog
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        KeyEvent.Callback callback = this.mKeyEventCallback;
        if (callback == null || !callback.onKeyLongPress(keyCode, event)) {
            return super.onKeyLongPress(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.app.Dialog
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        KeyEvent.Callback callback = this.mKeyEventCallback;
        if (callback == null || !callback.onKeyUp(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.app.Dialog
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        KeyEvent.Callback callback = this.mKeyEventCallback;
        if (callback == null || !callback.onKeyMultiple(keyCode, count, event)) {
            return super.onKeyMultiple(keyCode, count, event);
        }
        return true;
    }

    @Override // android.app.Dialog
    public void onBackPressed() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onBackPressed();
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

    @Override // android.app.Dialog
    public final void show() {
        int i = this.mWindowState;
        if (i == 0) {
            throw new IllegalStateException("Window token is not set yet.");
        } else if (i == 1 || i == 2) {
            try {
                super.show();
                updateWindowState(2);
            } catch (WindowManager.BadTokenException e) {
                Log.i(TAG, "Probably the IME window token is already invalidated. show() does nothing.");
                updateWindowState(3);
            }
        } else if (i == 3) {
            Log.i(TAG, "Not trying to call show() because it was already rejected once.");
        } else if (i == 4) {
            Log.i(TAG, "Ignoring show() because the window is already destroyed.");
        } else {
            throw new IllegalStateException("Unexpected state=" + this.mWindowState);
        }
    }

    /* access modifiers changed from: package-private */
    public final void dismissForDestroyIfNecessary() {
        int i = this.mWindowState;
        if (i == 0 || i == 1) {
            updateWindowState(4);
        } else if (i == 2) {
            try {
                getWindow().setWindowAnimations(0);
                dismiss();
            } catch (WindowManager.BadTokenException e) {
                Log.i(TAG, "Probably the IME window token is already invalidated. No need to dismiss it.");
            }
            updateWindowState(4);
        } else if (i == 3) {
            Log.i(TAG, "Not trying to dismiss the window because it is most likely unnecessary.");
            updateWindowState(4);
        } else if (i != 4) {
            throw new IllegalStateException("Unexpected state=" + this.mWindowState);
        } else {
            throw new IllegalStateException("dismissForDestroyIfNecessary can be called only once");
        }
    }

    private void updateWindowState(int newState) {
        this.mWindowState = newState;
    }

    private static String stateToString(int state) {
        if (state == 0) {
            return "TOKEN_PENDING";
        }
        if (state == 1) {
            return "TOKEN_SET";
        }
        if (state == 2) {
            return "SHOWN_AT_LEAST_ONCE";
        }
        if (state == 3) {
            return "REJECTED_AT_LEAST_ONCE";
        }
        if (state == 4) {
            return "DESTROYED";
        }
        throw new IllegalStateException("Unknown state=" + state);
    }
}
