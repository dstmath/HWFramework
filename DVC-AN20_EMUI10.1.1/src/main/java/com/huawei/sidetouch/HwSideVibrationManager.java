package com.huawei.sidetouch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import com.huawei.android.os.HwVibrator;

public class HwSideVibrationManager {
    private static final int DEFAULT_MAX_VOLUME = 15;
    private static final int DEFAULT_MIN_VOLUME = 0;
    private static final int DEFAULT_PRIVATE_MIN_VOLUME = 1;
    private static final int DEFAULT_VOLUME = 7;
    private static final String HW_VIBRATOR_VOLUME_CHANGE = "haptic.volume.change";
    private static final String HW_VIBRATOR_VOLUME_MAXMIN = "haptic.volume.maxmin";
    private static final String HW_VIBRATOR_VOLUME_TRIGGER = "haptic.volume.trigger";
    private static final String TAG = "HwSideVibrationManager";
    private static final long[] VIBRATE_PATTERN_MINMAX = {0, 75};
    private static final int VIBRATE_TIME_LIMIT_SCREENOFF = 1000;
    private static final int VIBRATE_TIME_LIMIT_SCREENON = 300;
    private static final int VIBRATE_TRIGGER_INTERVAL = 50;
    private static final int VIBRATOR_MINMAX = 1;
    private static final int VIBRATOR_NORMAL = 0;
    private static final int VIBRATOR_TRIGGER = 2;
    private static HwSideVibrationManager mInstance = null;
    private AudioManager mAudioManager = null;
    private Context mContext;
    private int[] mEffictiveStreamTypes = {0, 2, 3, 4, 6, 9};
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsHandleVolumeKeysInWm;
    private boolean mIsRegistered = false;
    private long mLastTriggerVibrateTime = 0;
    private long mLastVibrateTime = 0;
    private HwSideStatusManager mSideStatusManager = null;
    private Vibrator mVibrator = null;
    private final BroadcastReceiver mVolumeChangedReceiver = new BroadcastReceiver() {
        /* class com.huawei.sidetouch.HwSideVibrationManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action == null) {
                    Log.e(HwSideVibrationManager.TAG, "mVolumeChangedReceiver onReceive action is null");
                    return;
                }
                char c = 65535;
                if (action.hashCode() == -1940635523 && action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                    c = 0;
                }
                if (c == 0) {
                    HwSideVibrationManager.this.vibrateWhenVolumeChanged(intent);
                }
            }
        }
    };

    private HwSideVibrationManager(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mSideStatusManager = HwSideStatusManager.getInstance(this.mContext);
        initHandleThread();
        if (this.mContext.getResources() != null) {
            this.mIsHandleVolumeKeysInWm = this.mContext.getResources().getBoolean(17891464);
        }
    }

    private void initHandleThread() {
        this.mHandlerThread = new HandlerThread("handler-thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.huawei.sidetouch.HwSideVibrationManager.AnonymousClass2 */

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_MAXMIN);
                } else if (msg.what == 0) {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_CHANGE);
                } else {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_TRIGGER);
                }
            }
        };
    }

    public static synchronized HwSideVibrationManager getInstance(Context context) {
        HwSideVibrationManager hwSideVibrationManager;
        synchronized (HwSideVibrationManager.class) {
            if (mInstance == null) {
                mInstance = new HwSideVibrationManager(context);
            }
            hwSideVibrationManager = mInstance;
        }
        return hwSideVibrationManager;
    }

    public void registerVolumeChangedListener() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
        } else if (!this.mIsRegistered) {
            this.mIsRegistered = true;
            Log.d(TAG, "registerVolumeChangedListener");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
            this.mContext.registerReceiver(this.mVolumeChangedReceiver, intentFilter);
        }
    }

    public void unregisterVolumeChangedListener() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
        } else if (this.mIsRegistered) {
            this.mIsRegistered = false;
            Log.d(TAG, "unregisterVolumeChangedListener");
            this.mContext.unregisterReceiver(this.mVolumeChangedReceiver);
        }
    }

    public void doVibrate(int type) {
        if (this.mVibrator == null) {
            Log.w(TAG, "getVibrator is null");
        } else if (type == 0) {
            postVibrateHandler(HW_VIBRATOR_VOLUME_CHANGE);
        } else if (type == 1) {
            postVibrateHandler(HW_VIBRATOR_VOLUME_MAXMIN);
        } else if (type == 2) {
            postVibrateHandler(HW_VIBRATOR_VOLUME_TRIGGER);
        }
    }

    public void notifyTriggerTimer() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - this.mLastTriggerVibrateTime) >= 300) {
            Log.d(TAG, "record the trigger time! " + currentTime);
            this.mLastTriggerVibrateTime = currentTime;
        }
    }

    private boolean shouldVibrateFromVolumeKey(KeyEvent event, boolean isScreenOn) {
        if (this.mSideStatusManager == null) {
            Log.d(TAG, "mSideStatusManager is null, return");
            return false;
        } else if (this.mIsRegistered) {
            Log.d(TAG, "mIsRegistered is true, return");
            return false;
        } else if (!isSideTouchVolumeKey(event)) {
            Log.d(TAG, "not sidetouch volume key, return");
            return false;
        } else if (isRinging()) {
            Log.d(TAG, "ringing not vibrate ");
            return false;
        } else {
            if (isScreenOn) {
                long currentTime = System.currentTimeMillis();
                if (Math.abs(currentTime - this.mLastTriggerVibrateTime) < 50) {
                    return true;
                }
                long j = this.mLastTriggerVibrateTime;
                if (j <= 0 || Math.abs(currentTime - j) >= 300) {
                    this.mLastTriggerVibrateTime = 0;
                } else {
                    Log.d(TAG, "current scrren status is " + isScreenOn + " and last vibrate is too short, ignore this");
                    return false;
                }
            }
            return true;
        }
    }

    public void vibrateFromVolumeKey(KeyEvent event, boolean isAudioPlaying, boolean isScreenOn, boolean isKeyguardShowing) {
        if (shouldVibrateFromVolumeKey(event, isScreenOn)) {
            if (isScreenOn || isAudioPlaying) {
                Log.d(TAG, "vibrateFromVolumeKey");
                if ((event.getFlags() & 4096) != 0) {
                    doVibrate(2);
                    return;
                }
                int streamType = this.mSideStatusManager.getActiveStreamType();
                int streamType2 = streamType == -1 ? 3 : streamType;
                if (streamType2 < 0) {
                    Log.w(TAG, "not a active streamType:" + streamType2);
                } else if (this.mAudioManager == null) {
                    Log.w(TAG, "AudioManager is null");
                } else {
                    int maxVolumelevel = getDefaultMaxVolume(streamType2);
                    int minVolumeLevel = getDefaultMinVolume(streamType2);
                    int currentlevel = getCurrentVolume(streamType2);
                    Log.d(TAG, "vibrateFromVolumeKey stream= " + streamType2 + " level= " + currentlevel + " maxVolumelevel= " + maxVolumelevel + " minVolumeLevel= " + minVolumeLevel);
                    long curTime = System.currentTimeMillis();
                    if (currentlevel == maxVolumelevel) {
                        processVibrate(event, true, curTime);
                    } else if (currentlevel == minVolumeLevel) {
                        processVibrate(event, false, curTime);
                    } else {
                        doVibrate(0);
                    }
                }
            } else {
                Log.w(TAG, "screen off and not audio playing, do not vibrate");
            }
        }
    }

    private boolean isRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager == null || this.mIsHandleVolumeKeysInWm || !telecomManager.isRinging()) {
            return false;
        }
        return true;
    }

    private boolean isSideTouchVolumeKey(KeyEvent event) {
        InputDevice device = InputDevice.getDevice(event.getDeviceId());
        if (device == null) {
            return true;
        }
        if (device.isExternal() || device.isVirtual()) {
            return false;
        }
        return true;
    }

    private void processVibrate(KeyEvent event, boolean isMaxVolumeLevel, long currentTime) {
        if (isMaxVolumeLevel) {
            if (event.getKeyCode() == 25) {
                Log.d(TAG, "already max volume level, return");
                doVibrate(0);
                this.mLastVibrateTime = 0;
                return;
            }
        } else if (event.getKeyCode() == 24) {
            Log.d(TAG, "already max volume level, return");
            doVibrate(0);
            this.mLastVibrateTime = 0;
            return;
        }
        if (Math.abs(currentTime - this.mLastVibrateTime) < 1000) {
            Log.d(TAG, "last vibrate time is too short");
            return;
        }
        doVibrate(1);
        this.mLastVibrateTime = currentTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHwVibrate(String type) {
        if (this.mContext != null && HwVibrator.isSupportHwVibrator(type)) {
            HwVibrator.setHwVibrator(Process.myUid(), this.mContext.getPackageName(), type);
        }
    }

    private void postVibrateHandler(String vibrateType) {
        if (this.mHandler == null) {
            Log.w(TAG, "mHandler is null, return");
            return;
        }
        Log.d(TAG, "vibrate now: " + vibrateType);
        if (HW_VIBRATOR_VOLUME_MAXMIN.equals(vibrateType)) {
            this.mHandler.obtainMessage(1);
            this.mHandler.sendEmptyMessage(1);
        } else if (HW_VIBRATOR_VOLUME_CHANGE.equals(vibrateType)) {
            this.mHandler.obtainMessage(0);
            this.mHandler.sendEmptyMessage(0);
        } else {
            this.mHandler.obtainMessage(2);
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private boolean isEffctiveStreamType(int streamType) {
        for (int audioStreamType : this.mEffictiveStreamTypes) {
            if (streamType == audioStreamType) {
                return true;
            }
        }
        return false;
    }

    private int getCurrentVolume(int streamType) {
        try {
            return this.mAudioManager.getStreamVolume(streamType);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "AudioManager exception ");
            return 7;
        }
    }

    private int getDefaultMaxVolume(int streamType) {
        try {
            return this.mAudioManager.getStreamMaxVolume(streamType);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "AudioManager exception ");
            return 15;
        }
    }

    private int getDefaultMinVolume(int streamType) {
        if (streamType == 6) {
            return 1;
        }
        try {
            return this.mAudioManager.getStreamMinVolume(streamType);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "AudioManager exception ");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void vibrateWhenVolumeChanged(Intent intent) {
        if (this.mAudioManager == null) {
            Log.w(TAG, "AudioManager is null");
            return;
        }
        int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
        if (!isEffctiveStreamType(streamType)) {
            Log.d(TAG, "not a effctive stream type, return: " + streamType);
            return;
        }
        int currentlevel = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
        if (currentlevel == intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1)) {
            Log.d(TAG, "currentlevel == oldLevel");
            return;
        }
        int maxVolumelevel = getDefaultMaxVolume(streamType);
        int minVolumeLevel = getDefaultMinVolume(streamType);
        Log.d(TAG, "VOLUME_CHANGED_ACTION stream = " + streamType + " maxVolumelevel= " + maxVolumelevel + " minVolumeLevel= " + minVolumeLevel + " currentlevel= " + currentlevel);
        if (currentlevel == maxVolumelevel || currentlevel == minVolumeLevel) {
            doVibrate(1);
        } else {
            doVibrate(0);
        }
    }
}
