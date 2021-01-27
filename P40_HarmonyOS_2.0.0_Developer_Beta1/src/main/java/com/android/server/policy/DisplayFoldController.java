package com.android.server.policy;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.IDisplayFoldListener;
import com.android.server.DisplayThread;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerInternal;

/* access modifiers changed from: package-private */
public class DisplayFoldController {
    private static final String TAG = "DisplayFoldController";
    private final int mDisplayId;
    private final DisplayManagerInternal mDisplayManagerInternal;
    private final DisplayFoldDurationLogger mDurationLogger = new DisplayFoldDurationLogger();
    private String mFocusedApp;
    private Boolean mFolded;
    private final Rect mFoldedArea;
    private final Handler mHandler;
    private final RemoteCallbackList<IDisplayFoldListener> mListeners = new RemoteCallbackList<>();
    private final DisplayInfo mNonOverrideDisplayInfo = new DisplayInfo();
    private Rect mOverrideFoldedArea = new Rect();
    private final WindowManagerInternal mWindowManagerInternal;

    DisplayFoldController(WindowManagerInternal windowManagerInternal, DisplayManagerInternal displayManagerInternal, int displayId, Rect foldedArea, Handler handler) {
        this.mWindowManagerInternal = windowManagerInternal;
        this.mDisplayManagerInternal = displayManagerInternal;
        this.mDisplayId = displayId;
        this.mFoldedArea = new Rect(foldedArea);
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public void finishedGoingToSleep() {
        this.mDurationLogger.onFinishedGoingToSleep();
    }

    /* access modifiers changed from: package-private */
    public void finishedWakingUp() {
        this.mDurationLogger.onFinishedWakingUp(this.mFolded);
    }

    /* access modifiers changed from: package-private */
    public void requestDeviceFolded(boolean folded) {
        this.mHandler.post(new Runnable(folded) {
            /* class com.android.server.policy.$$Lambda$DisplayFoldController$NTSuhIo_Cno_Oi2ZijeIvJCcvfc */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                DisplayFoldController.this.lambda$requestDeviceFolded$0$DisplayFoldController(this.f$1);
            }
        });
    }

    /* access modifiers changed from: package-private */
    /* renamed from: setDeviceFolded */
    public void lambda$requestDeviceFolded$0$DisplayFoldController(boolean folded) {
        Rect foldedArea;
        Boolean bool = this.mFolded;
        if (bool == null || bool.booleanValue() != folded) {
            if (folded) {
                if (!this.mOverrideFoldedArea.isEmpty()) {
                    foldedArea = this.mOverrideFoldedArea;
                } else if (!this.mFoldedArea.isEmpty()) {
                    foldedArea = this.mFoldedArea;
                } else {
                    return;
                }
                this.mDisplayManagerInternal.getNonOverrideDisplayInfo(this.mDisplayId, this.mNonOverrideDisplayInfo);
                int dx = ((this.mNonOverrideDisplayInfo.logicalWidth - foldedArea.width()) / 2) - foldedArea.left;
                int dy = ((this.mNonOverrideDisplayInfo.logicalHeight - foldedArea.height()) / 2) - foldedArea.top;
                this.mDisplayManagerInternal.setDisplayScalingDisabled(this.mDisplayId, true);
                this.mWindowManagerInternal.setForcedDisplaySize(this.mDisplayId, foldedArea.width(), foldedArea.height());
                this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplayId, -dx, -dy);
            } else {
                this.mDisplayManagerInternal.setDisplayScalingDisabled(this.mDisplayId, false);
                this.mWindowManagerInternal.clearForcedDisplaySize(this.mDisplayId);
                this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplayId, 0, 0);
            }
            this.mDurationLogger.setDeviceFolded(folded);
            this.mDurationLogger.logFocusedAppWithFoldState(folded, this.mFocusedApp);
            this.mFolded = Boolean.valueOf(folded);
            int n = this.mListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onDisplayFoldChanged(this.mDisplayId, folded);
                } catch (RemoteException e) {
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: package-private */
    public void registerDisplayFoldListener(IDisplayFoldListener listener) {
        this.mListeners.register(listener);
        if (this.mFolded != null) {
            this.mHandler.post(new Runnable(listener) {
                /* class com.android.server.policy.$$Lambda$DisplayFoldController$aUVA2gXih47E319JuwXnHTqEGHI */
                private final /* synthetic */ IDisplayFoldListener f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    DisplayFoldController.this.lambda$registerDisplayFoldListener$1$DisplayFoldController(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$registerDisplayFoldListener$1$DisplayFoldController(IDisplayFoldListener listener) {
        try {
            listener.onDisplayFoldChanged(this.mDisplayId, this.mFolded.booleanValue());
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterDisplayFoldListener(IDisplayFoldListener listener) {
        this.mListeners.unregister(listener);
    }

    /* access modifiers changed from: package-private */
    public void setOverrideFoldedArea(Rect area) {
        this.mOverrideFoldedArea.set(area);
    }

    /* access modifiers changed from: package-private */
    public Rect getFoldedArea() {
        if (!this.mOverrideFoldedArea.isEmpty()) {
            return this.mOverrideFoldedArea;
        }
        return this.mFoldedArea;
    }

    static DisplayFoldController createWithProxSensor(Context context, int displayId) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SensorManager.class);
        Sensor proxSensor = sensorManager.getDefaultSensor(8);
        if (proxSensor == null) {
            return null;
        }
        DisplayFoldController result = create(context, displayId);
        sensorManager.registerListener(new SensorEventListener() {
            /* class com.android.server.policy.DisplayFoldController.AnonymousClass1 */

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent event) {
                DisplayFoldController displayFoldController = DisplayFoldController.this;
                boolean z = false;
                if (event.values[0] < 1.0f) {
                    z = true;
                }
                displayFoldController.requestDeviceFolded(z);
            }

            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, proxSensor, 3);
        return result;
    }

    /* access modifiers changed from: package-private */
    public void onDefaultDisplayFocusChanged(String pkg) {
        this.mFocusedApp = pkg;
    }

    static DisplayFoldController create(Context context, int displayId) {
        Rect foldedArea;
        DisplayManagerInternal displayService = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        String configFoldedArea = context.getResources().getString(17039842);
        if (configFoldedArea == null || configFoldedArea.isEmpty()) {
            foldedArea = new Rect();
        } else {
            foldedArea = Rect.unflattenFromString(configFoldedArea);
        }
        return new DisplayFoldController((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class), displayService, displayId, foldedArea, DisplayThread.getHandler());
    }
}
