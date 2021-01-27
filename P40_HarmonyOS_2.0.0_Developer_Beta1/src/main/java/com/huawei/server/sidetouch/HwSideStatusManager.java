package com.huawei.server.sidetouch;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import com.huawei.android.media.AudioSystemEx;
import com.huawei.android.view.InputDeviceEx;
import com.huawei.sidetouch.IHwSideStatusManager;

public class HwSideStatusManager implements IHwSideStatusManager {
    public static final String AUDIO_STATE_MUSIC = "1";
    public static final String AUDIO_STATE_NONE = "0";
    public static final String AUDIO_STATE_RING = "2";
    public static final String CONFIG_SIDE_FEATURE_DISABLE = "1,0";
    public static final String CONFIG_SIDE_FEATURE_ENABLE = "1,1";
    public static final int FLAG_AUDIO_FEATURE = 3;
    public static final int FLAG_SIDE_FEATURE = 8;
    private static final int FLAG_VOLUME_TRIGGER = 4096;
    private static final int INVALID_STREAM_TYPE = -1;
    private static final String SIDE_DEVICE_NAME = "huawei,ts_extra_key";
    private static final String TAG = "HwSideStatusManager";
    private static HwSideStatusManager sInstance = null;
    private AudioManager mAudioManager;
    private int[] mAudioStreamTypes = {0, 2, 3, 4, 6, 8, 9, 10};
    private Context mContext;
    private boolean mIsVolumeTrigger = false;
    private int[] mRingModes = {2, 3, 1};
    private final Object mTriggerEventMutex = new Object();

    private HwSideStatusManager(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
    }

    public static synchronized HwSideStatusManager getInstance(Context context) {
        HwSideStatusManager hwSideStatusManager;
        synchronized (HwSideStatusManager.class) {
            if (sInstance == null) {
                sInstance = new HwSideStatusManager(context);
            }
            hwSideStatusManager = sInstance;
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
            if (AudioSystemEx.isStreamActive(streamType, 0)) {
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
            return INVALID_STREAM_TYPE;
        }
        int[] iArr = this.mAudioStreamTypes;
        for (int streamType : iArr) {
            if (AudioSystemEx.isStreamActive(streamType, 0)) {
                Log.i(TAG, "updateAudioStatus streamType is active: " + streamType);
                return streamType;
            }
        }
        if (this.mAudioManager.getMode() != 2) {
            return INVALID_STREAM_TYPE;
        }
        Log.i(TAG, "updateAudioStatus MODE_IN_CALL");
        return 0;
    }

    public boolean isSideTouchEvent(KeyEvent event) {
        InputDevice device = event.getDevice();
        if (device == null || InputDeviceEx.isExternal(device) || device.isVirtual() || !SIDE_DEVICE_NAME.equals(device.getName())) {
            return false;
        }
        return true;
    }

    @Override // com.huawei.sidetouch.IHwSideStatusManager
    public void updateVolumeTriggerStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int flag = event.getFlags();
        resetVolumeTriggerStatus();
        if (isSideTouchEvent(event) && keyCode == 25 && (flag & FLAG_VOLUME_TRIGGER) != 0) {
            synchronized (this.mTriggerEventMutex) {
                Log.d(TAG, "updateVolumeTriggerStatus set VolumeTrigger true ");
                this.mIsVolumeTrigger = true;
            }
        }
    }

    @Override // com.huawei.sidetouch.IHwSideStatusManager
    public boolean isVolumeTriggered() {
        boolean z;
        synchronized (this.mTriggerEventMutex) {
            z = this.mIsVolumeTrigger;
        }
        return z;
    }

    @Override // com.huawei.sidetouch.IHwSideStatusManager
    public void resetVolumeTriggerStatus() {
        synchronized (this.mTriggerEventMutex) {
            Log.d(TAG, "resetVolumeTriggerStatus");
            this.mIsVolumeTrigger = false;
        }
    }

    public boolean checkVolumeTriggerStatusAndReset() {
        boolean isTriggered;
        synchronized (this.mTriggerEventMutex) {
            isTriggered = this.mIsVolumeTrigger;
            if (this.mIsVolumeTrigger) {
                Log.d(TAG, "resetVolumeTriggerStatus");
                this.mIsVolumeTrigger = false;
            }
        }
        return isTriggered;
    }

    public String getAudioStatus(boolean isAudioPlaying) {
        AudioManager audioManager = this.mAudioManager;
        if (audioManager == null || !isAudioPlaying) {
            return AUDIO_STATE_NONE;
        }
        int audioMode = audioManager.getMode();
        for (int mode : this.mRingModes) {
            if (mode == audioMode) {
                return AUDIO_STATE_RING;
            }
        }
        return AUDIO_STATE_MUSIC;
    }
}
