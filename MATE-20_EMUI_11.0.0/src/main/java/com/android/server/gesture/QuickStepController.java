package com.android.server.gesture;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.graphics.MatrixEx;
import com.huawei.android.provider.SettingsEx;
import java.io.PrintWriter;

public class QuickStepController extends QuickStartupStub {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    public static final int HIT_TARGET_OVERVIEW = 3;
    private boolean mIsKeyguardShowing;
    private boolean mIsQuickStepStarted;
    private boolean mIsTalkBackOn;
    private Looper mLooper;
    private GestureNavView mNavView;
    private OverviewProxyService mOverviewEventSender;
    private SettingsObserver mSettingsObserver;
    private final Matrix mTransformGlobalMatrix = new Matrix();
    private final Matrix mTransformLocalMatrix = new Matrix();

    public QuickStepController(Context context, Looper looper) {
        super(context);
        this.mLooper = looper;
    }

    private IOverviewProxy getOverviewProxy() {
        OverviewProxyService overviewProxyService = this.mOverviewEventSender;
        if (overviewProxyService != null) {
            return overviewProxyService.getProxy();
        }
        return null;
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            QuickStepController.this.updateSettings();
        }
    }

    public boolean isSupportMultiTouch() {
        return this.mIsTalkBackOn;
    }

    public void updateKeyguardState(boolean isKeyguardShowing) {
        this.mIsKeyguardShowing = isKeyguardShowing;
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void updateSettings() {
        this.mContext.getContentResolver();
        boolean z = false;
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, -2) == 1) {
            z = true;
        }
        this.mIsTalkBackOn = z;
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QS, "mIsTalkBackOn : " + this.mIsTalkBackOn);
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onNavCreate(GestureNavView navView) {
        super.onNavCreate(navView);
        this.mNavView = navView;
        this.mOverviewEventSender = new OverviewProxyService(this.mContext, this.mLooper);
        this.mOverviewEventSender.notifyStart();
        this.mSettingsObserver = new SettingsObserver(new Handler(this.mLooper));
        ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), Settings.Secure.getUriFor(ACCESSIBILITY_SCREENREADER_ENABLED), false, this.mSettingsObserver, -1);
        updateSettings();
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onNavUpdate() {
        super.onNavUpdate();
        GestureNavView gestureNavView = this.mNavView;
        if (gestureNavView != null) {
            gestureNavView.invalidate();
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onNavDestroy() {
        super.onNavDestroy();
        if (this.mSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
            this.mSettingsObserver = null;
        }
        OverviewProxyService overviewProxyService = this.mOverviewEventSender;
        if (overviewProxyService != null) {
            overviewProxyService.notifyStop();
            this.mOverviewEventSender = null;
        }
        this.mNavView = null;
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture, Runnable runnable) {
        super.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, runnable);
        if (!this.mIsKeyguardShowing) {
            return;
        }
        if (isGoHomeAllRight(distance, durationTime) && runnable != null) {
            runnable.run();
        } else if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QS, "keyguard go home fail, distance=" + distance + ", durationTime=" + durationTime);
        }
    }

    @Override // com.android.server.gesture.QuickStartupStub
    public void handleTouchEvent(MotionEvent event) {
        if (getOverviewProxy() != null && !this.mIsKeyguardShowing) {
            int action = event.getActionMasked();
            if (action == 0) {
                this.mTransformGlobalMatrix.set(MatrixEx.IDENTITY_MATRIX);
                this.mTransformLocalMatrix.set(MatrixEx.IDENTITY_MATRIX);
                GestureNavView gestureNavView = this.mNavView;
                if (gestureNavView != null) {
                    gestureNavView.transformMatrixToGlobal(this.mTransformGlobalMatrix);
                    this.mNavView.transformMatrixToLocal(this.mTransformLocalMatrix);
                }
                this.mIsQuickStepStarted = false;
                this.mIsGestureReallyStarted = false;
            } else if (action != 1) {
                if (action != 2) {
                    if (action != 3) {
                    }
                } else if (!this.mIsQuickStepStarted && this.mIsGestureReallyStarted) {
                    startQuickStep(event);
                }
            }
            proxyMotionEvents(event);
        }
    }

    private void startQuickStep(MotionEvent event) {
        this.mIsQuickStepStarted = true;
        IOverviewProxy overviewProxy = getOverviewProxy();
        if (overviewProxy != null && isGesNavProxyEnable()) {
            event.transform(this.mTransformGlobalMatrix);
            try {
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QS, "Quick Step Start");
                }
                overviewProxy.onQuickStep(event);
            } catch (RemoteException e) {
                Log.e(GestureNavConst.TAG_GESTURE_QS, "Failed to send quick step started.");
            } catch (Throwable th) {
                event.transform(this.mTransformLocalMatrix);
                throw th;
            }
            event.transform(this.mTransformLocalMatrix);
        }
    }

    private boolean proxyMotionEvents(MotionEvent event) {
        IOverviewProxy overviewProxy = getOverviewProxy();
        if (overviewProxy == null || !isGesNavProxyEnable()) {
            return false;
        }
        event.transform(this.mTransformGlobalMatrix);
        try {
            if (event.getActionMasked() == 0) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_QS, "Send Motion Down Event");
                }
                overviewProxy.onPreMotionEvent(3);
            }
            if (GestureNavConst.DEBUG_ALL) {
                Log.d(GestureNavConst.TAG_GESTURE_QS, "Send MotionEvent: " + event.toString());
            }
            overviewProxy.onMotionEvent(event);
            return true;
        } catch (RemoteException e) {
            Log.e(GestureNavConst.TAG_GESTURE_QS, "Callback failed");
            return false;
        } finally {
            event.transform(this.mTransformLocalMatrix);
        }
    }

    private boolean isGoHomeAllRight(float distance, long durationTime) {
        if (distance <= 180.0f || durationTime >= 500) {
            return false;
        }
        return true;
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.println("mIsTalkBackOn=" + this.mIsTalkBackOn);
        OverviewProxyService overviewProxyService = this.mOverviewEventSender;
        if (overviewProxyService != null) {
            overviewProxyService.dump(prefix, pw, args);
        }
    }

    public boolean isGesNavProxyEnable() {
        OverviewProxyService overviewProxyService = this.mOverviewEventSender;
        if (overviewProxyService != null) {
            return overviewProxyService.isGesNavProxyEnable();
        }
        return true;
    }

    public void updateBottomVisible(boolean isBottomEnable) {
        IOverviewProxy overviewProxy = getOverviewProxy();
        if (overviewProxy != null && !isGesNavProxyEnable()) {
            try {
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QS, "updateBottomVisible isBottomEnable:" + isBottomEnable);
                }
                overviewProxy.onTip(0, isBottomEnable ? 1 : 0);
            } catch (RemoteException | NoSuchMethodError e) {
                Log.e(GestureNavConst.TAG_GESTURE_QS, "updateBottomVisible failed");
            }
        }
    }

    public void updateCheckTwiceSettings() {
        IOverviewProxy overviewProxy = getOverviewProxy();
        if (overviewProxy != null && !isGesNavProxyEnable() && !GestureNavConst.IS_SUPPORT_GAME_ASSIST) {
            boolean isCheckTwiceEnable = HwGestureNavWhiteConfig.getInstance().isEnable();
            try {
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QS, "updateCheckTwiceSettings isCheckTwiceEnable:" + isCheckTwiceEnable);
                }
                overviewProxy.onTip(1, isCheckTwiceEnable ? 1 : 0);
            } catch (RemoteException | NoSuchMethodError e) {
                Log.e(GestureNavConst.TAG_GESTURE_QS, "onCheckTwiceSettings failed");
            }
        }
    }
}
