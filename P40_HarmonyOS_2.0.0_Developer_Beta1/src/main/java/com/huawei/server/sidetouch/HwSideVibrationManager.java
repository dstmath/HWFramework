package com.huawei.server.sidetouch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.KeyEvent;
import com.huawei.android.media.AudioManagerEx;
import com.huawei.android.os.HwVibrator;
import com.huawei.android.telecom.TelecomManagerEx;
import com.huawei.utils.HwPartResourceUtils;

public class HwSideVibrationManager {
    private static final int DEFAULT_MAX_VOLUME = 15;
    private static final int DEFAULT_MIN_VOLUME = 0;
    private static final int DEFAULT_PRIVATE_MIN_VOLUME = 1;
    private static final int DEFAULT_VOLUME = 7;
    private static final String HW_VIBRATOR_VOLUME_CHANGE = "haptic.volume.change";
    private static final String HW_VIBRATOR_VOLUME_MAX = "haptic.volume.max";
    private static final String HW_VIBRATOR_VOLUME_MIN = "haptic.volume.min";
    private static final String HW_VIBRATOR_VOLUME_TRIGGER = "haptic.volume.trigger";
    private static final String TAG = "HwSideVibrationManager";
    private static final int UNKNOWN_STREAM = -1;
    private static final int VIBRATE_TIME_LIMIT_SCREENOFF = 1000;
    private static final int VIBRATE_TIME_LIMIT_SCREENON = 300;
    private static final int VIBRATE_TRIGGER_INTERVAL = 50;
    private static final int VIBRATOR_MAX = 4;
    private static final int VIBRATOR_MIN = 3;
    private static final int VIBRATOR_NORMAL = 1;
    private static final int VIBRATOR_TRIGGER = 2;
    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static HwSideVibrationManager sInstance = null;
    private AudioManager mAudioManager = null;
    private Context mContext;
    private int[] mEffictiveStreamTypes = {DEFAULT_MIN_VOLUME, 2, 3, VIBRATOR_MAX, 6, 9};
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsHandleVolumeKeysInWm;
    private boolean mIsRegistered = false;
    private boolean mIsVolumePanelVisiable;
    private long mLastTriggerVibrateTime = 0;
    private long mLastVibrateTime = 0;
    private boolean mShouldVibrateWhenVolumeChanged;
    private HwSideStatusManager mSideStatusManager = null;
    private final BroadcastReceiver mVolumeChangedReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.sidetouch.HwSideVibrationManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action == null) {
                    Log.e(HwSideVibrationManager.TAG, "mVolumeChangedReceiver onReceive action is null");
                    return;
                }
                char c = 65535;
                if (action.hashCode() == -1940635523 && action.equals(HwSideVibrationManager.VOLUME_CHANGED_ACTION)) {
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
        this.mSideStatusManager = HwSideStatusManager.getInstance(this.mContext);
        initHandleThread();
        if (this.mContext.getResources() != null) {
            this.mIsHandleVolumeKeysInWm = this.mContext.getResources().getBoolean(HwPartResourceUtils.getResourceId("config_handleVolumeKeysInWindowManager"));
        }
    }

    private void initHandleThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.huawei.server.sidetouch.HwSideVibrationManager.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_CHANGE);
                } else if (i == 2) {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_TRIGGER);
                } else if (i == 3) {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_MIN);
                } else if (i == HwSideVibrationManager.VIBRATOR_MAX) {
                    HwSideVibrationManager.this.doHwVibrate(HwSideVibrationManager.HW_VIBRATOR_VOLUME_MAX);
                }
            }
        };
    }

    public static synchronized HwSideVibrationManager getInstance(Context context) {
        HwSideVibrationManager hwSideVibrationManager;
        synchronized (HwSideVibrationManager.class) {
            if (sInstance == null) {
                sInstance = new HwSideVibrationManager(context);
            }
            hwSideVibrationManager = sInstance;
        }
        return hwSideVibrationManager;
    }

    public void onVolumePanelVisibleChanged(boolean isVisiable) {
        this.mIsVolumePanelVisiable = isVisiable;
        if (isVisiable) {
            registerVolumeChangedListener();
        } else {
            unregisterVolumeChangedListener();
        }
    }

    private void registerVolumeChangedListener() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
        } else if (!this.mIsRegistered) {
            this.mIsRegistered = true;
            Log.d(TAG, "registerVolumeChangedListener");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(VOLUME_CHANGED_ACTION);
            this.mContext.registerReceiver(this.mVolumeChangedReceiver, intentFilter);
        }
    }

    private void unregisterVolumeChangedListener() {
        if (this.mContext == null) {
            Log.w(TAG, "mContext is null");
        } else if (this.mIsRegistered) {
            this.mIsRegistered = false;
            Log.d(TAG, "unregisterVolumeChangedListener");
            this.mContext.unregisterReceiver(this.mVolumeChangedReceiver);
        }
    }

    private void scheduleVibrate(int type) {
        this.mHandler.sendEmptyMessage(type);
    }

    public void notifyThpEvent(int event) {
        if (SideTouchConst.DEBUG) {
            Log.i(TAG, "notifyThpEvent:" + event);
        }
        boolean isVolumeEvent = false;
        if ((event & 3072) != 0 ? true : DEFAULT_MIN_VOLUME) {
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - this.mLastTriggerVibrateTime) >= 300) {
                this.mLastTriggerVibrateTime = currentTime;
                return;
            }
            return;
        }
        boolean isTouchEvent = (196608 & event) != 0 ? true : DEFAULT_MIN_VOLUME;
        if ((event & 12288) != 0) {
            isVolumeEvent = true;
        }
        if (!this.mIsVolumePanelVisiable) {
            return;
        }
        if (isTouchEvent || isVolumeEvent) {
            this.mShouldVibrateWhenVolumeChanged = true;
            if (isTouchEvent) {
                scheduleVibrate(2);
            }
        }
    }

    private boolean shouldVibrateFromVolumeKey(KeyEvent event, boolean isScreenOn) {
        HwSideStatusManager hwSideStatusManager = this.mSideStatusManager;
        if (hwSideStatusManager == null) {
            Log.d(TAG, "mSideStatusManager is null, return");
            return false;
        } else if (this.mIsRegistered) {
            Log.d(TAG, "mIsRegistered is true, return");
            return false;
        } else if (!hwSideStatusManager.isSideTouchEvent(event)) {
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

    private void vibrateForSideVolumeEvent(KeyEvent event, boolean isAudioPlaying, boolean isScreenOn) {
        if (shouldVibrateFromVolumeKey(event, isScreenOn)) {
            if (isScreenOn || isAudioPlaying) {
                Log.d(TAG, "vibrateFromVolumeKey");
                if ((event.getFlags() & 4096) != 0) {
                    this.mShouldVibrateWhenVolumeChanged = true;
                    scheduleVibrate(2);
                    return;
                }
                int streamType = this.mSideStatusManager.getActiveStreamType();
                int streamType2 = streamType == UNKNOWN_STREAM ? 3 : streamType;
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
                    if (currentlevel == minVolumeLevel) {
                        processVibrate(event, false, 3, curTime);
                    } else if (currentlevel == maxVolumelevel) {
                        processVibrate(event, true, VIBRATOR_MAX, curTime);
                    } else {
                        scheduleVibrate(1);
                    }
                }
            } else {
                Log.w(TAG, "screen off and not audio playing, do not vibrate");
            }
        }
    }

    public void onSideVolumeEvent(KeyEvent event, boolean isAudioPlaying, boolean isScreenOn) {
        vibrateForSideVolumeEvent(event, isAudioPlaying, isScreenOn);
    }

    public void onNormalVolumeEvent(KeyEvent event, boolean isInjected, boolean isScreenOn) {
        Log.i(TAG, "normal volume key arrived, isInjected:" + isInjected + ", isScreenOn:" + isScreenOn);
        if (!isInjected && isScreenOn) {
            this.mShouldVibrateWhenVolumeChanged = true;
        }
    }

    private boolean isRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager == null || this.mIsHandleVolumeKeysInWm || !TelecomManagerEx.isRinging(telecomManager)) {
            return false;
        }
        return true;
    }

    private void processVibrate(KeyEvent event, boolean isMaxVolumeLevel, int type, long currentTime) {
        if (isMaxVolumeLevel) {
            if (event.getKeyCode() == 25) {
                Log.d(TAG, "already max volume level, return");
                scheduleVibrate(1);
                this.mLastVibrateTime = 0;
                return;
            }
        } else if (event.getKeyCode() == 24) {
            Log.d(TAG, "already max volume level, return");
            scheduleVibrate(1);
            this.mLastVibrateTime = 0;
            return;
        }
        if (Math.abs(currentTime - this.mLastVibrateTime) < 1000) {
            Log.d(TAG, "last vibrate time is too short");
            return;
        }
        scheduleVibrate(type);
        this.mLastVibrateTime = currentTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHwVibrate(String type) {
        if (HwVibrator.isSupportHwVibrator(type)) {
            HwVibrator.setHwVibrator(Process.myUid(), this.mContext.getPackageName(), type);
        }
    }

    private boolean isEffctiveStreamType(int streamType) {
        int[] iArr = this.mEffictiveStreamTypes;
        int length = iArr.length;
        for (int i = DEFAULT_MIN_VOLUME; i < length; i++) {
            if (streamType == iArr[i]) {
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
            return DEFAULT_VOLUME;
        }
    }

    private int getDefaultMaxVolume(int streamType) {
        try {
            return this.mAudioManager.getStreamMaxVolume(streamType);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "AudioManager exception ");
            return DEFAULT_MAX_VOLUME;
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
            return DEFAULT_MIN_VOLUME;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void vibrateWhenVolumeChanged(Intent intent) {
        if (this.mShouldVibrateWhenVolumeChanged) {
            if (this.mAudioManager == null) {
                Log.w(TAG, "AudioManager is null");
                return;
            }
            int streamType = intent.getIntExtra(AudioManagerEx.getExtraVolumeStreamType(), UNKNOWN_STREAM);
            if (!isEffctiveStreamType(streamType)) {
                Log.d(TAG, "not a effctive stream type, return: " + streamType);
                return;
            }
            int currentlevel = intent.getIntExtra(AudioManagerEx.getExtraVolumeStreamValue(), UNKNOWN_STREAM);
            if (currentlevel == intent.getIntExtra(AudioManagerEx.getExtraPrevVolumeStreamValue(), UNKNOWN_STREAM)) {
                Log.d(TAG, "currentlevel == oldLevel");
                return;
            }
            int maxVolumelevel = getDefaultMaxVolume(streamType);
            int minVolumeLevel = getDefaultMinVolume(streamType);
            Log.d(TAG, "VOLUME_CHANGED_ACTION stream = " + streamType + " maxVolumelevel= " + maxVolumelevel + " minVolumeLevel= " + minVolumeLevel + " currentlevel= " + currentlevel);
            if (currentlevel == minVolumeLevel) {
                scheduleVibrate(3);
            } else if (currentlevel == maxVolumelevel) {
                scheduleVibrate(VIBRATOR_MAX);
            } else {
                scheduleVibrate(1);
            }
        }
    }
}
