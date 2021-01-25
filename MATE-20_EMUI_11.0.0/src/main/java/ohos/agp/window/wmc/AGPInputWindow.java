package ohos.agp.window.wmc;

import android.graphics.Rect;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.WindowManager;
import ohos.aafwk.utils.log.LogDomain;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AGPInputWindow extends AGPCommonDialogWindow {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPInputWindow");
    private final Rect mBounds = new Rect();
    final Callback mCallback;
    final boolean mTakesFocus;
    private AGPInputWindowState mWindowState = AGPInputWindowState.TOKEN_PENDING;

    private enum AGPInputWindowState {
        TOKEN_PENDING,
        TOKEN_SET,
        SHOWN_AT_LEAST_ONCE,
        REJECTED_AT_LEAST_ONCE,
        DESTROYED
    }

    public interface Callback {
        void onBackPressed();
    }

    /* renamed from: ohos.agp.window.wmc.AGPInputWindow$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState = new int[AGPInputWindowState.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[AGPInputWindowState.TOKEN_PENDING.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[AGPInputWindowState.TOKEN_SET.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[AGPInputWindowState.SHOWN_AT_LEAST_ONCE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[AGPInputWindowState.REJECTED_AT_LEAST_ONCE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[AGPInputWindowState.DESTROYED.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public void setToken(IBinder iBinder) {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[this.mWindowState.ordinal()];
        if (i == 1) {
            this.mAndroidParam.token = iBinder;
            this.mAndroidWindow.setAttributes(this.mAndroidParam);
            this.mWindowState = AGPInputWindowState.TOKEN_SET;
        } else if (i == 2 || i == 3 || i == 4) {
            throw new IllegalStateException("setToken can be called only once");
        } else if (i == 5) {
            HiLog.debug(LABEL, "Ignoring setToken() because window is already destroyed.", new Object[0]);
        } else {
            throw new IllegalStateException("Unexpected state=" + this.mWindowState);
        }
    }

    public AGPInputWindow(Context context, String str, Callback callback, int i, boolean z) {
        super(context, 8);
        this.mAndroidWindow.setType(i);
        this.mAndroidWindow.setTitle(str);
        this.mAndroidWindow.setGravity(AGPWindowManager.getAndroidGravity(128));
        this.mCallback = callback;
        this.mTakesFocus = z;
        this.mAndroidParam = this.mAndroidWindow.getAttributes();
        updateWidthHeight();
        setFlags();
    }

    public void setGravity(int i) {
        this.mAndroidParam.gravity = AGPWindowManager.getAndroidGravity(i);
        updateWidthHeight();
        this.mAndroidWindow.setAttributes(this.mAndroidParam);
    }

    public int getGravity() {
        return AGPWindowManager.getZidaneTextAlignment(this.mAndroidParam.gravity);
    }

    private void setFlags() {
        int i;
        int i2;
        if (this.mTakesFocus) {
            i2 = 288;
            i = 298;
        } else {
            i2 = 264;
            i = 266;
        }
        this.mAndroidWindow.setFlags(i2, i);
    }

    private void updateWidthHeight() {
        if (this.mAndroidParam == null) {
            HiLog.debug(LABEL, "updateWidthHeight mAndroidParam is null", new Object[0]);
        } else if (this.mAndroidParam.gravity == AGPWindowManager.getAndroidGravity(128) || this.mAndroidParam.gravity == AGPWindowManager.getAndroidGravity(32)) {
            this.mAndroidParam.height = -2;
            this.mAndroidParam.width = -1;
        } else {
            this.mAndroidParam.height = -1;
            this.mAndroidParam.width = -2;
        }
    }

    @Override // ohos.agp.window.wmc.AGPCommonDialogWindow, ohos.agp.window.wmc.AGPWindow
    public void show() {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[this.mWindowState.ordinal()];
        if (i == 1) {
            throw new IllegalStateException("Window token is not set yet.");
        } else if (i == 2 || i == 3) {
            try {
                super.show();
                this.mWindowState = AGPInputWindowState.SHOWN_AT_LEAST_ONCE;
            } catch (WindowManager.BadTokenException unused) {
                HiLog.debug(LABEL, "Window token is already invalidated. show() does nothing.", new Object[0]);
                this.mWindowState = AGPInputWindowState.REJECTED_AT_LEAST_ONCE;
            }
        } else if (i == 4) {
            HiLog.debug(LABEL, "Not trying to call show() because it was already rejected once.", new Object[0]);
        } else if (i == 5) {
            HiLog.debug(LABEL, "Ignoring show() because the window is already destroyed.", new Object[0]);
        } else {
            throw new IllegalStateException("Unexpected state=" + this.mWindowState);
        }
    }

    @Override // ohos.agp.window.wmc.AGPCommonDialogWindow, ohos.agp.window.wmc.AGPWindow
    public void destroy() {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$window$wmc$AGPInputWindow$AGPInputWindowState[this.mWindowState.ordinal()];
        if (i == 1 || i == 2) {
            this.mWindowState = AGPInputWindowState.DESTROYED;
        } else if (i == 3) {
            try {
                this.mAndroidWindow.setWindowAnimations(0);
                super.destroy();
            } catch (WindowManager.BadTokenException unused) {
                HiLog.debug(LABEL, "Window token is already invalidated. No need to destroy it.", new Object[0]);
            }
            this.mWindowState = AGPInputWindowState.DESTROYED;
        } else if (i == 4) {
            HiLog.debug(LABEL, "Not trying to dismiss the window because it is most likely unnecessary.", new Object[0]);
            this.mWindowState = AGPInputWindowState.DESTROYED;
        } else if (i != 5) {
            throw new IllegalStateException("Unexpected state=" + this.mWindowState);
        } else {
            throw new IllegalStateException("destroy can be called only once");
        }
    }

    public void hide() {
        if (this.mDecor == null) {
            HiLog.error(LABEL, "hide mDecor is null", new Object[0]);
        } else {
            this.mDecor.setVisibility(8);
        }
    }

    @Override // ohos.agp.window.wmc.AGPCommonDialogWindow, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            HiLog.error(LABEL, "dispatchTouchEvent event is null", new Object[0]);
            return false;
        } else if (this.mDecor == null) {
            HiLog.error(LABEL, "dispatchTouchEvent mDecor is null", new Object[0]);
            return false;
        } else {
            this.mDecor.getHitRect(this.mBounds);
            if (motionEvent.isWithinBoundsNoHistory((float) this.mBounds.left, (float) this.mBounds.top, (float) (this.mBounds.right - 1), (float) (this.mBounds.bottom - 1))) {
                return super.dispatchTouchEvent(motionEvent);
            }
            MotionEvent clampNoHistory = motionEvent.clampNoHistory((float) this.mBounds.left, (float) this.mBounds.top, (float) (this.mBounds.right - 1), (float) (this.mBounds.bottom - 1));
            boolean dispatchTouchEvent = super.dispatchTouchEvent(clampNoHistory);
            clampNoHistory.recycle();
            return dispatchTouchEvent;
        }
    }

    public void onBackPressed() {
        Callback callback = this.mCallback;
        if (callback == null) {
            HiLog.error(LABEL, "mCallback is null.", new Object[0]);
        } else {
            callback.onBackPressed();
        }
    }
}
