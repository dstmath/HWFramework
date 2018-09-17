package android.media;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.app.admin.ConstantValue;
import huawei.com.android.internal.widget.HwLockPatternUtils;

public class HwMediaRecorderImpl implements IHwMediaRecorder {
    private static final String TAG = "HwMediaRecorderImpl";
    private static IBinder mAudioService;
    private static IHwMediaRecorder mHwMediaRecoder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwMediaRecorderImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwMediaRecorderImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwMediaRecorderImpl.<clinit>():void");
    }

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    private HwMediaRecorderImpl() {
        Log.i(TAG, TAG);
    }

    public static IHwMediaRecorder getDefault() {
        return mHwMediaRecoder;
    }

    public void sendStateChangedIntent(int state) {
        Log.i(TAG, "sendStateChangedIntent, state=" + state);
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeString(MediaRecorder.class.getSimpleName());
            _data.writeInt(state);
            _data.writeInt(Process.myPid());
            _data.writeString(ActivityThread.currentPackageName());
            if (b != null) {
                b.transact(HwLockPatternUtils.transaction_setActiveVisitorPasswordState, _data, _reply, 0);
            }
            _reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "sendStateChangedIntent transact e: " + e);
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public void checkRecordActive() {
        Log.i(TAG, "checkRecordActive");
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            if (b != null) {
                b.transact(ConstantValue.transaction_setWifiDisabled, _data, _reply, 0);
            }
            _reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "checkRecordActive transact e: " + e);
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }
}
