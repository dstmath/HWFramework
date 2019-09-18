package com.android.server.policy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import java.io.PrintWriter;

public abstract class WakeGestureListener {
    private static final String TAG = "WakeGestureListener";
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final TriggerEventListener mListener = new TriggerEventListener() {
        public void onTrigger(TriggerEvent event) {
            synchronized (WakeGestureListener.this.mLock) {
                boolean unused = WakeGestureListener.this.mTriggerRequested = false;
                WakeGestureListener.this.mHandler.post(WakeGestureListener.this.mWakeUpRunnable);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private Sensor mSensor;
    private final SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public boolean mTriggerRequested;
    /* access modifiers changed from: private */
    public final Runnable mWakeUpRunnable = new Runnable() {
        public void run() {
            WakeGestureListener.this.onWakeUp();
        }
    };

    public abstract void onWakeUp();

    public WakeGestureListener(Context context, Handler handler) {
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mHandler = handler;
        this.mSensor = this.mSensorManager.getDefaultSensor(23);
    }

    public boolean isSupported() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSensor != null;
        }
        return z;
    }

    public void requestWakeUpTrigger() {
        synchronized (this.mLock) {
            if (this.mSensor != null && !this.mTriggerRequested) {
                this.mTriggerRequested = true;
                this.mSensorManager.requestTriggerSensor(this.mListener, this.mSensor);
            }
        }
    }

    public void cancelWakeUpTrigger() {
        synchronized (this.mLock) {
            if (this.mSensor != null && this.mTriggerRequested) {
                this.mTriggerRequested = false;
                this.mSensorManager.cancelTriggerSensor(this.mListener, this.mSensor);
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.println(prefix + TAG);
            String prefix2 = prefix + "  ";
            pw.println(prefix2 + "mTriggerRequested=" + this.mTriggerRequested);
            pw.println(prefix2 + "mSensor=" + this.mSensor);
        }
    }
}
