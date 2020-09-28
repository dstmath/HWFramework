package com.huawei.sidetouch;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import com.huawei.android.view.HwExtDisplaySizeUtil;

public class HwSideStatusManager implements IHwSideStatusManager {
    public static final String AUDIO_STATE_MUSIC = "1";
    public static final String AUDIO_STATE_NONE = "0";
    public static final String AUDIO_STATE_RING = "2";
    public static final int FLAG_FEATURE = 3;
    private static final int FLAG_VOLUME_TRIGGER = 4096;
    private static final String SIDE_DEVICE_NAME = "huawei,ts_extra_key";
    private static final String TAG = "HwSideStatusManager";
    private static HwSideStatusManager mInstance = null;
    private AudioManager mAudioManager;
    private int[] mAudioStreamTypes = {0, 2, 3, 4, 6, 8, 9, 10};
    private Context mContext;
    private boolean mIsSupportSideScreen = false;
    private boolean mIsVolumeTrigger = false;
    private int[] mRingModes = {2, 3, 1};
    private final Object triggerEventMutex = new Object();

    private HwSideStatusManager(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        HwExtDisplaySizeUtil displaySizeUtil = HwExtDisplaySizeUtil.getInstance();
        if (displaySizeUtil != null) {
            this.mIsSupportSideScreen = displaySizeUtil.hasSideInScreen();
        }
    }

    public static synchronized HwSideStatusManager getInstance(Context context) {
        HwSideStatusManager hwSideStatusManager;
        synchronized (HwSideStatusManager.class) {
            if (mInstance == null) {
                mInstance = new HwSideStatusManager(context);
            }
            hwSideStatusManager = mInstance;
        }
        return hwSideStatusManager;
    }

    public void registerAudioPlaybackListener(AudioManager.AudioPlaybackCallback callback, Handler handler) {
        if (this.mAudioManager != null && callback != null) {
            Log.i(TAG, "registerAudioPlaybackListener");
            this.mAudioManager.registerAudioPlaybackCallback(callback, handler);
        }
    }

    public void unregisterAudioPlaybackListener(AudioManager.AudioPlaybackCallback callback) {
        if (this.mAudioManager != null && callback != null) {
            Log.i(TAG, "unregisterAudioPlaybackListener");
            this.mAudioManager.unregisterAudioPlaybackCallback(callback);
        }
    }

    public boolean isAudioPlaybackActive() {
        if (this.mAudioManager == null) {
            return false;
        }
        int[] iArr = this.mAudioStreamTypes;
        for (int streamType : iArr) {
            if (AudioSystem.isStreamActive(streamType, 0)) {
                Log.i(TAG, "updateAudioStatus streamType is active: " + streamType);
                return true;
            }
        }
        int audioMode = this.mAudioManager.getMode();
        for (int mode : this.mRingModes) {
            if (mode == audioMode) {
                Log.i(TAG, "updateAudioStatus MODE_IN_CALL");
                return true;
            }
        }
        return false;
    }

    public int getActiveStreamType() {
        if (this.mAudioManager == null) {
            return -1;
        }
        int[] iArr = this.mAudioStreamTypes;
        for (int streamType : iArr) {
            if (AudioSystem.isStreamActive(streamType, 0)) {
                Log.i(TAG, "updateAudioStatus streamType is active: " + streamType);
                return streamType;
            }
        }
        if (this.mAudioManager.getMode() != 2) {
            return -1;
        }
        Log.i(TAG, "updateAudioStatus MODE_IN_CALL");
        return 0;
    }

    public boolean isSideTouchEvent(KeyEvent event) {
        InputDevice device = InputDevice.getDevice(event.getDeviceId());
        if (device == null || device.isExternal() || device.isVirtual()) {
            return false;
        }
        String deviceName = device.getName();
        Log.d(TAG, "isSideTouchEvent, device name " + deviceName);
        if (deviceName == null || !SIDE_DEVICE_NAME.equals(deviceName)) {
            return false;
        }
        return true;
    }

    public void updateVolumeTriggerStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int flag = event.getFlags();
        resetVolumeTriggerStatus();
        if (isSideTouchEvent(event) && keyCode == 25 && (flag & 4096) != 0) {
            synchronized (this.triggerEventMutex) {
                Log.d(TAG, "updateVolumeTriggerStatus set VolumeTrigger true ");
                this.mIsVolumeTrigger = true;
            }
        }
    }

    public boolean isVolumeTriggered() {
        boolean z;
        if (!this.mIsSupportSideScreen) {
            return false;
        }
        synchronized (this.triggerEventMutex) {
            z = this.mIsVolumeTrigger;
        }
        return z;
    }

    public void resetVolumeTriggerStatus() {
        if (this.mIsSupportSideScreen) {
            synchronized (this.triggerEventMutex) {
                Log.d(TAG, "resetVolumeTriggerStatus");
                this.mIsVolumeTrigger = false;
            }
        }
    }

    public String getAudioStatus(boolean isAudioPlaying) {
        AudioManager audioManager = this.mAudioManager;
        if (audioManager == null || !isAudioPlaying) {
            return "0";
        }
        int audioMode = audioManager.getMode();
        for (int mode : this.mRingModes) {
            if (mode == audioMode) {
                return AUDIO_STATE_RING;
            }
        }
        return "1";
    }
}
