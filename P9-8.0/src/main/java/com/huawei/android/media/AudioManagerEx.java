package com.huawei.android.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.facerecognition.FaceCamera;

public class AudioManagerEx {
    private static final int ENABLE_VOLUME_ADJUST_TOKEN = 1102;
    private static final int IS_ADJUST_VOLUME_ENABLE_TOKEN = 1101;
    public static final int STREAM_FM;
    public static final int STREAM_INCALL_MUSIC = 3;
    public static final int STREAM_VOICE_HELPER = 11;
    private static final String TAG = "AudioManagerEx";

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
        return 1 == AudioSystem.getDeviceConnectionState(1048576, "");
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
        } finally {
            reply.recycle();
            data.recycle();
        }
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
        } finally {
            reply.recycle();
            data.recycle();
        }
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
                    b.transact(FaceCamera.RET_REPEAT_REQUEST_FAILED, _data, _reply, 0);
                }
                _reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "disableHeadPhone transact e: " + e);
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
    }

    public static int getRingerModeInternal(AudioManager audioManager) {
        return audioManager.getRingerModeInternal();
    }
}
