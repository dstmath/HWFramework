package com.android.server.gesture;

import android.content.ContentResolver;
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
import java.io.PrintWriter;

public class QuickStepController extends QuickStartupStub {
    public static final int HIT_TARGET_OVERVIEW = 3;
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private boolean mKeyguardShowing;
    private Looper mLooper;
    private GestureNavView mNavView;
    private OverviewProxyService mOverviewEventSender;
    private boolean mQuickStepStarted;
    private SettingsObserver mSettingsObserver;
    private boolean mTalkBackOn;
    private final Matrix mTransformGlobalMatrix = new Matrix();
    private final Matrix mTransformLocalMatrix = new Matrix();

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            QuickStepController.this.updateSettings();
        }
    }

    public QuickStepController(Context context, Looper looper) {
        super(context);
        this.mLooper = looper;
    }

    private IOverviewProxy getOverviewProxy() {
        if (this.mOverviewEventSender != null) {
            return this.mOverviewEventSender.getProxy();
        }
        return null;
    }

    public boolean isSupportMultiTouch() {
        return this.mTalkBackOn;
    }

    public void updateKeyguardState(boolean keyguardShowing) {
        this.mKeyguardShowing = keyguardShowing;
    }

    public void updateSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean z = false;
        boolean accessibilityEnabled = Settings.Secure.getIntForUser(resolver, "accessibility_enabled", 0, -2) == 1;
        String enabledSerices = Settings.Secure.getStringForUser(resolver, "enabled_accessibility_services", -2);
        boolean isContainsTalkBackService = enabledSerices != null && enabledSerices.contains(TALKBACK_COMPONENT_NAME);
        if (accessibilityEnabled && isContainsTalkBackService) {
            z = true;
        }
        this.mTalkBackOn = z;
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QS, "accessEnabled:" + accessibilityEnabled + ", hasTalkback:" + isContainsTalkBackService);
        }
    }

    public void onNavCreate(GestureNavView navView) {
        super.onNavCreate(navView);
        this.mNavView = navView;
        this.mOverviewEventSender = new OverviewProxyService(this.mContext, this.mLooper);
        this.mOverviewEventSender.notifyStart();
        this.mSettingsObserver = new SettingsObserver(new Handler(this.mLooper));
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Secure.getUriFor("accessibility_enabled"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("enabled_accessibility_services"), false, this.mSettingsObserver, -1);
        updateSettings();
    }

    public void onNavUpdate() {
        super.onNavUpdate();
        if (this.mNavView != null) {
            this.mNavView.invalidate();
        }
    }

    public void onNavDestroy() {
        super.onNavDestroy();
        if (this.mSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
            this.mSettingsObserver = null;
        }
        if (this.mOverviewEventSender != null) {
            this.mOverviewEventSender.notifyStop();
            this.mOverviewEventSender = null;
        }
        this.mNavView = null;
    }

    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture, Runnable runnable) {
        super.onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, runnable);
        if (!this.mKeyguardShowing) {
            return;
        }
        if (isGoHomeAllRight(distance, durationTime) && runnable != null) {
            runnable.run();
        } else if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QS, "keyguard go home fail, distance=" + distance + ", durationTime=" + durationTime);
        }
    }

    public void handleTouchEvent(MotionEvent event) {
        if (!GestureNavConst.USE_ANIM_LEGACY && getOverviewProxy() != null && !this.mKeyguardShowing) {
            int action = event.getActionMasked();
            if (action == 0) {
                this.mTransformGlobalMatrix.set(Matrix.IDENTITY_MATRIX);
                this.mTransformLocalMatrix.set(Matrix.IDENTITY_MATRIX);
                if (this.mNavView != null) {
                    this.mNavView.transformMatrixToGlobal(this.mTransformGlobalMatrix);
                    this.mNavView.transformMatrixToLocal(this.mTransformLocalMatrix);
                }
                this.mQuickStepStarted = false;
                this.mGestureReallyStarted = false;
            } else if (action == 2 && !this.mQuickStepStarted && this.mGestureReallyStarted) {
                startQuickStep(event);
            }
            proxyMotionEvents(event);
        }
    }

    private void startQuickStep(MotionEvent event) {
        this.mQuickStepStarted = true;
        IOverviewProxy overviewProxy = getOverviewProxy();
        if (overviewProxy != null) {
            event.transform(this.mTransformGlobalMatrix);
            try {
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QS, "Quick Step Start");
                }
                overviewProxy.onQuickStep(event);
            } catch (RemoteException e) {
                Log.e(GestureNavConst.TAG_GESTURE_QS, "Failed to send quick step started.", e);
            } catch (Throwable th) {
                event.transform(this.mTransformLocalMatrix);
                throw th;
            }
            event.transform(this.mTransformLocalMatrix);
        }
    }

    private boolean proxyMotionEvents(MotionEvent event) {
        IOverviewProxy overviewProxy = getOverviewProxy();
        if (overviewProxy == null) {
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
            Log.e(GestureNavConst.TAG_GESTURE_QS, "Callback failed", e);
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
        pw.println("mTalkBackOn=" + this.mTalkBackOn);
        if (this.mOverviewEventSender != null) {
            this.mOverviewEventSender.dump(prefix, pw, args);
        }
    }
}
