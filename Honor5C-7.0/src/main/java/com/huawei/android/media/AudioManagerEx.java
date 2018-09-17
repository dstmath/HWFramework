package com.huawei.android.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.MSimTelephonyConstants;
import android.util.Log;
import com.huawei.motiondetection.MotionTypeApps;

public class AudioManagerEx {
    private static final int ENABLE_VOLUME_ADJUST_TOKEN = 1102;
    private static final int IS_ADJUST_VOLUME_ENABLE_TOKEN = 1101;
    public static final int STREAM_FM = 0;
    public static final int STREAM_INCALL_MUSIC = 3;
    public static final int STREAM_VOICE_HELPER = 11;
    private static final String TAG = "AudioManagerEx";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.media.AudioManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.media.AudioManagerEx.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.media.AudioManagerEx.<clinit>():void");
    }

    public static void setSpeakermediaOn(Context context, boolean on) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (on) {
            data.writeInt(1);
        } else {
            data.writeInt(STREAM_FM);
        }
        try {
            ServiceManager.getService("audio").transact(MotionTypeApps.TYPE_PICKUP_REDUCE_CALL, data, reply, STREAM_FM);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on setSpeakermediaOn in exception....");
        }
    }

    public static boolean isFMActive(AudioManager am) {
        if (1 == AudioSystem.getDeviceConnectionState(1048576, MSimTelephonyConstants.MY_RADIO_PLATFORM)) {
            return true;
        }
        return false;
    }

    public static boolean isAdjuseVolumeEnable() {
        int ret = STREAM_FM;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            ServiceManager.getService("audio").transact(IS_ADJUST_VOLUME_ENABLE_TOKEN, data, reply, STREAM_FM);
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
            data.writeInt(STREAM_FM);
        }
        try {
            ServiceManager.getService("audio").transact(ENABLE_VOLUME_ADJUST_TOKEN, data, reply, STREAM_FM);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on enableVolumeAdjust in exception....");
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
