package com.huawei.android.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.android.util.HwPCUtilsEx;
import com.huawei.systemmanager.power.HwHistoryItem;

public class AudioManagerEx {
    private static final int ENABLE_VOLUME_ADJUST_TOKEN = 1102;
    private static final int IS_ADJUST_VOLUME_ENABLE_TOKEN = 1101;
    public static final int STREAM_FM;
    public static final int STREAM_INCALL_MUSIC = 3;
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_VOICE_HELPER = 11;
    private static final String TAG = "AudioManagerEx";
    private static final HwAudioServiceManager mHwAudioServiceManager = new HwAudioServiceManager();

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

    public static void setSpeakermediaOn(Context context, boolean on) {
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
    }

    public static boolean isFMActive(AudioManager am) {
        boolean isFMActiveFlag = true;
        if (1 != AudioSystem.getDeviceConnectionState(HwHistoryItem.STATE_SCREEN_ON_FLAG, "")) {
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
            Log.e(TAG, "add-on isAdjuseVolumeEnable in exception....", e);
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
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeStrongBinder(new Binder());
        if (enable) {
            data.writeInt(1);
        } else {
            data.writeInt(0);
        }
        try {
            ServiceManager.getService("audio").transact(ENABLE_VOLUME_ADJUST_TOKEN, data, reply, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on enableVolumeAdjust in exception....");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
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
        if (disabled) {
            IBinder b = ServiceManager.getService("audio");
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken("android.media.IAudioService");
                if (b != null) {
                    b.transact(HwPCUtilsEx.FORCED_PC_DISPLAY_SIZE_OVERSCAN_MODE, _data, _reply, 0);
                }
                _reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "disableHeadPhone transact e: " + e);
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
            _reply.recycle();
            _data.recycle();
        }
    }

    public static int getRingerModeInternal(AudioManager audioManager) {
        return audioManager.getRingerModeInternal();
    }

    public static boolean isRemoteSubmixActive() {
        boolean isRemoteSubmixActiveFlag = true;
        if (1 != AudioSystem.getDeviceConnectionState(32768, "")) {
            isRemoteSubmixActiveFlag = false;
        }
        return isRemoteSubmixActiveFlag;
    }

    public static boolean isHDMIActive() {
        boolean isHDMIActiveFlag = true;
        if (1 != AudioSystem.getDeviceConnectionState(AppOpsManagerEx.TYPE_CAMERA, "")) {
            isHDMIActiveFlag = false;
        }
        return isHDMIActiveFlag;
    }

    public static int getForceUse(int usage) {
        return AudioSystem.getForceUse(usage);
    }

    public static int getForDesktopMode() {
        return 8;
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
        return AudioSystem.setForceUse(usage, config);
    }

    public static int getShowUiWarningsFlag() {
        return AppOpsManagerEx.TYPE_CAMERA;
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
}
