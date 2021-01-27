package com.huawei.android.media;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioFocusChangeCallback;
import android.media.AudioFocusInfo;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.HwMediaMonitorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.android.util.HwPCUtilsEx;
import com.huawei.annotation.HwSystemApi;

public class AudioManagerEx {
    private static final int ENABLE_VOLUME_ADJUST_TOKEN = 1102;
    private static final int IS_ADJUST_VOLUME_ENABLE_TOKEN = 1101;
    @HwSystemApi
    public static final int STREAM_BLUETOOTH_SCO = 6;
    public static final int STREAM_FM;
    public static final int STREAM_INCALL_MUSIC = 3;
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_VOICE_HELPER = 11;
    private static final String TAG = "AudioManagerEx";
    private static final HwAudioServiceManager mHwAudioServiceManager = new HwAudioServiceManager();
    private static final IBinder sICallBack = new Binder();

    static {
        boolean supportFmStream = true;
        try {
            AudioSystem.class.getDeclaredField("STREAM_FM");
        } catch (NoSuchFieldException e) {
            supportFmStream = false;
        }
        if (supportFmStream) {
            STREAM_FM = 10;
        } else {
            STREAM_FM = 3;
        }
        Log.i(TAG, "STREAM_FM = " + STREAM_FM);
    }

    public static final String getVolumeChangedAction() {
        return "android.media.VOLUME_CHANGED_ACTION";
    }

    public static final String getExtraVolumeStreamType() {
        return "android.media.EXTRA_VOLUME_STREAM_TYPE";
    }

    public static final String getExtraVolumeStreamValue() {
        return "android.media.EXTRA_VOLUME_STREAM_VALUE";
    }

    @HwSystemApi
    public static final String getExtraPrevVolumeStreamValue() {
        return "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE";
    }

    public static void setSpeakermediaOn(Context context, boolean on) {
        Log.i(TAG, "setSpeakerphoneOn: " + on);
        if (context == null) {
            Log.e(TAG, "context is null");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (on) {
            data.writeInt(1);
        } else {
            data.writeInt(0);
        }
        try {
            ServiceManager.getService("audio").transact(101, data, reply, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on setSpeakermediaOn in exception....");
        }
        HwMediaMonitorManager.readyForWriteBigData(916600007, "SET_SPEAKERMEDIA_ON", "1: " + String.valueOf(on));
    }

    public static boolean isFMActive(AudioManager am) {
        boolean isFMActiveFlag = true;
        if (AudioSystem.getDeviceConnectionState(1048576, "") != 1) {
            isFMActiveFlag = false;
        }
        return isFMActiveFlag;
    }

    public static boolean isSourceActive(int source) {
        return AudioSystem.isSourceActive(source);
    }

    public static boolean isAdjuseVolumeEnable() {
        int ret = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            ServiceManager.getService("audio").transact(1101, data, reply, 0);
            reply.readException();
            ret = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "add-on isAdjuseVolumeEnable in exception....");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        if (ret > 0) {
            return true;
        }
        return false;
    }

    public static void enableVolumeAdjust(boolean enable) {
        Log.i(TAG, "enableVolumeAdjust " + enable);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeStrongBinder(new Binder());
        if (enable) {
            data.writeInt(1);
        } else {
            data.writeInt(0);
        }
        try {
            ServiceManager.getService("audio").transact(1102, data, reply, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on enableVolumeAdjust in exception....");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        HwMediaMonitorManager.readyForWriteBigData(916600008, "ENABLE_VOLUME_ADJUST", "4: " + String.valueOf(enable));
    }

    public static boolean isWiredHeadsetOnWithMicrophone() {
        if (AudioSystem.getDeviceConnectionState(4, "") == 1) {
            return true;
        }
        return false;
    }

    public static boolean isAudioOutputUSBDevieIn() {
        if (AudioSystem.getDeviceConnectionState(16384, "") == 1) {
            return true;
        }
        return false;
    }

    public static void disableHeadPhone(boolean disabled) {
        Log.i(TAG, "disableHeadPhone " + disabled);
        if (disabled) {
            IBinder binder = ServiceManager.getService("audio");
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken("android.media.IAudioService");
                if (binder != null) {
                    binder.transact(HwPCUtilsEx.FORCED_PC_DISPLAY_SIZE_OVERSCAN_MODE, _data, _reply, 0);
                }
                _reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "disableHeadPhone transact e: ");
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
            _reply.recycle();
            _data.recycle();
        }
        HwMediaMonitorManager.readyForWriteBigData(916600007, "DISABLE_HEAD_PHONE", "2: " + String.valueOf(disabled));
    }

    public static int getRingerModeInternal(AudioManager audioManager) {
        if (audioManager != null) {
            return audioManager.getRingerModeInternal();
        }
        Log.e(TAG, "audiomanager is null");
        return 0;
    }

    public static boolean isRemoteSubmixActive() {
        boolean isRemoteSubmixActiveFlag = true;
        if (AudioSystem.getDeviceConnectionState(32768, "") != 1) {
            isRemoteSubmixActiveFlag = false;
        }
        return isRemoteSubmixActiveFlag;
    }

    public static boolean isHDMIActive() {
        boolean isHDMIActiveFlag = true;
        if (AudioSystem.getDeviceConnectionState(1024, "") != 1) {
            isHDMIActiveFlag = false;
        }
        return isHDMIActiveFlag;
    }

    public static int getForceUse(int usage) {
        return AudioSystem.getForceUse(usage);
    }

    public static int getForDesktopMode() {
        return 9;
    }

    public static int getForceDesktopNoHdmi() {
        return 17;
    }

    public static int getForceDesktopHdmi() {
        return 16;
    }

    public static int getForceNone() {
        return 0;
    }

    public static int setForceUse(int usage, int config) {
        Log.i(TAG, "setForceUse: " + usage + " " + config);
        HwMediaMonitorManager.readyForWriteBigData(916600007, "SET_FORCE_USE", "3: " + String.valueOf(usage) + ", " + String.valueOf(config), usage);
        return AudioSystem.setForceUse(usage, config);
    }

    public static int getShowUiWarningsFlag() {
        return 1024;
    }

    public void registerAudioModeCallback(IAudioModeCallback cb, Handler handler) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("registerAudioModeCallback handler is null ? ");
        sb.append(handler == null);
        Log.v(TAG, sb.toString());
        if (cb != null) {
            mHwAudioServiceManager.registerAudioModeCallback(cb.getAudioModeCb(), handler);
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioModeCallback argument");
    }

    public void unregisterAudioModeCallback(IAudioModeCallback cb) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("unregisterAudioModeCallback cb is null ? ");
        sb.append(cb == null);
        Log.v(TAG, sb.toString());
        if (cb != null) {
            mHwAudioServiceManager.unregisterAudioModeCallback(cb.getAudioModeCb());
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioModeCallback argument");
    }

    public static boolean registerAudioDeviceSelectCallback(DeviceSelectCallback cb) throws IllegalArgumentException {
        if (cb != null && cb.getDeviceSelectCb() != null) {
            return mHwAudioServiceManager.registerAudioDeviceSelectCallback(cb.getDeviceSelectCb().asBinder());
        }
        throw new IllegalArgumentException("Illegal null AudioDeviceSelect argument");
    }

    public static boolean unregisterAudioDeviceSelectCallback(DeviceSelectCallback cb) throws IllegalArgumentException {
        if (cb != null && cb.getDeviceSelectCb() != null) {
            return mHwAudioServiceManager.unregisterAudioDeviceSelectCallback(cb.getDeviceSelectCb().asBinder());
        }
        throw new IllegalArgumentException("Illegal null AudioDeviceSelect argument");
    }

    public static boolean registerAudioFocusChangeCallback(IAudioFocusChangeCallback cb, Handler handler) throws IllegalArgumentException {
        if (cb != null) {
            return AudioFocusChangeCallback.registerAudioFocusChangeCallback(cb.getAudioFocusChangeCallback(), handler);
        }
        throw new IllegalArgumentException("Illegal null AudioFocusChangeCallback argument");
    }

    public static boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeCallback cb) throws IllegalArgumentException {
        if (cb != null) {
            return AudioFocusChangeCallback.unregisterAudioFocusChangeCallback(cb.getAudioFocusChangeCallback());
        }
        throw new IllegalArgumentException("Illegal null AudioFocusChangeCallback argument");
    }

    public static AudioFocusInfoEx getAudioFocusInfoEx() {
        String pkgName = "none";
        if (ActivityThread.currentApplication() != null) {
            pkgName = ActivityThread.currentApplication().getPackageName();
        }
        AudioFocusInfo af = mHwAudioServiceManager.getAudioFocusInfo(pkgName);
        if (af == null) {
            return null;
        }
        return new AudioFocusInfoEx(af.getAttributes(), af.getClientId(), af.getGainRequest());
    }

    public static boolean setFmDeviceAvailable(int state) {
        return mHwAudioServiceManager.setFmDeviceAvailable(state);
    }

    public static void setBtScoForRecord(boolean on) {
        Log.d(TAG, "setBtScoForRecord: " + on);
        mHwAudioServiceManager.setBtScoForRecord(on);
    }

    public static boolean registerVolumeChangeCallback(IVolumeChangeCallback cb, Handler handler) throws IllegalArgumentException {
        if (cb != null) {
            return VolumeChangeCallback.registerVolumeChangeCallback(cb.getVolumeChangeCallback(), handler);
        }
        throw new IllegalArgumentException("Illegal null IVolumeChangeCallback argument");
    }

    public static boolean unregisterVolumeChangeCallback(IVolumeChangeCallback cb) throws IllegalArgumentException {
        if (cb != null) {
            return VolumeChangeCallback.unregisterVolumeChangeCallback(cb.getVolumeChangeCallback());
        }
        throw new IllegalArgumentException("Illegal null IVolumeChangeCallback argument");
    }

    public static void setHistenNaturalMode(boolean on) {
        Log.d(TAG, "setHistenNaturalMode: " + on);
        mHwAudioServiceManager.setHistenNaturalMode(on, sICallBack);
    }

    public static void setMultiAudioRecordEnable(boolean enable) {
        Log.d(TAG, "setMultiAudioRecordEnable: " + enable);
        mHwAudioServiceManager.setMultiAudioRecordEnable(enable);
    }

    public static boolean isMultiAudioRecordEnable() {
        boolean enable = mHwAudioServiceManager.isMultiAudioRecordEnable();
        Log.d(TAG, "isMultiAudioRecordEnable: " + enable);
        return enable;
    }

    public static void setVoiceRecordingEnable(boolean enable) {
        Log.d(TAG, "setVoiceRecordingEnable: " + enable);
        mHwAudioServiceManager.setVoiceRecordingEnable(enable);
    }

    public static boolean isVoiceRecordingEnable() {
        boolean enable = mHwAudioServiceManager.isVoiceRecordingEnable();
        Log.d(TAG, "isVoiceRecordingEnable: " + enable);
        return enable;
    }

    public static boolean setVolumeByPidStream(int pid, int streamType, float volume) {
        Log.i(TAG, "setVolumeByPidStream: pid " + pid + " streamType " + streamType + " volume " + volume);
        return mHwAudioServiceManager.setVolumeByPidStream(pid, streamType, volume, sICallBack);
    }
}
