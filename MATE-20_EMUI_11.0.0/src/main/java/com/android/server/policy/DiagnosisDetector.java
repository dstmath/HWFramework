package com.android.server.policy;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;

public class DiagnosisDetector {
    private static final long ACTIVITY_DELAY_MILLIS = 6000;
    private static final int RECEIVER_DEFAULT_VALUE = -1;
    private static final String TAG = "DiagnosisDetector";
    private ComponentName mComponentName;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsPowKeyDown;
    private boolean mIsUsbReceiverRegister;
    private boolean mIsVolumeDownKeyDown;
    private boolean mIsVolumeUpKeyDown;
    private final Runnable mRunnable = new Runnable() {
        /* class com.android.server.policy.DiagnosisDetector.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            DiagnosisDetector.this.startLockDetectionActivity();
        }
    };
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.DiagnosisDetector.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                return;
            }
            if (intent.getIntExtra("plugged", -1) == 2) {
                Log.i(DiagnosisDetector.TAG, "usb connected");
                DiagnosisDetector.this.startTimer();
                return;
            }
            Log.i(DiagnosisDetector.TAG, "usb disconnected");
            DiagnosisDetector.this.cancelTimer();
        }
    };

    public DiagnosisDetector(Context context, Handler handler, ComponentName componentName) {
        this.mContext = context;
        this.mHandler = handler;
        this.mComponentName = componentName;
        Log.i(TAG, "DiagnosisDetector is created");
    }

    public void updateState(int keyCode, boolean isDown) {
        switch (keyCode) {
            case 24:
                if (!isDown) {
                    this.mIsVolumeUpKeyDown = false;
                    checkState();
                    return;
                } else if (!this.mIsVolumeUpKeyDown) {
                    this.mIsVolumeUpKeyDown = true;
                    checkState();
                    return;
                } else {
                    return;
                }
            case 25:
                if (!isDown) {
                    this.mIsVolumeDownKeyDown = false;
                    checkState();
                    return;
                } else if (!this.mIsVolumeDownKeyDown) {
                    this.mIsVolumeDownKeyDown = true;
                    checkState();
                    return;
                } else {
                    return;
                }
            case 26:
                if (!isDown) {
                    this.mIsPowKeyDown = false;
                    checkState();
                    return;
                } else if (!this.mIsPowKeyDown) {
                    this.mIsPowKeyDown = true;
                    checkState();
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private void checkState() {
        if (!this.mIsPowKeyDown || !this.mIsVolumeUpKeyDown || this.mIsVolumeDownKeyDown) {
            unregisterUsbReceiver(this.mContext);
            cancelTimer();
            return;
        }
        registerUsbReceiver(this.mContext);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startTimer() {
        if (this.mHandler.hasCallbacks(this.mRunnable)) {
            this.mHandler.removeCallbacks(this.mRunnable);
        }
        this.mHandler.postDelayed(this.mRunnable, ACTIVITY_DELAY_MILLIS);
        Log.i(TAG, "start the timer");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelTimer() {
        this.mHandler.removeCallbacks(this.mRunnable);
        Log.i(TAG, "stop timing");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startLockDetectionActivity() {
        Intent intent = new Intent();
        intent.setComponent(this.mComponentName);
        intent.setFlags(268435456);
        Log.i(TAG, "startLockDetectionActivity(): " + this.mComponentName);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "startLockDetectionActivity() failed message : " + ex.getMessage());
        }
    }

    private void registerUsbReceiver(Context context) {
        if (!this.mIsUsbReceiverRegister) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
            context.registerReceiver(this.mUsbReceiver, intentFilter);
            this.mIsUsbReceiverRegister = true;
            Log.i(TAG, "register usb receiver");
        }
    }

    private void unregisterUsbReceiver(Context context) {
        if (this.mIsUsbReceiverRegister) {
            try {
                context.unregisterReceiver(this.mUsbReceiver);
                this.mIsUsbReceiverRegister = false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "unregister usb receiver exception");
            }
            Log.i(TAG, "unregister usb receiver");
        }
    }
}
