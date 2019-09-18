package android.media;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class HwMediaRecorderImpl implements IHwMediaRecorder {
    private static final int CONSTANT_NUM = 1004;
    private static final int CONSTANT_VALUE = 1002;
    private static final String TAG = "HwMediaRecorderImpl";
    private static IBinder mAudioService = null;
    private static IHwMediaRecorder mHwMediaRecoder = new HwMediaRecorderImpl();

    private HwMediaRecorderImpl() {
        Log.i(TAG, TAG);
    }

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    public static IHwMediaRecorder getDefault() {
        return mHwMediaRecoder;
    }

    public void sendStateChangedIntent(int state) {
        Log.i(TAG, "sendStateChangedIntent, state=" + state);
        IBinder b = getAudioService();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            data.writeString(MediaRecorder.class.getSimpleName());
            data.writeInt(state);
            data.writeInt(Process.myPid());
            data.writeString(ActivityThread.currentPackageName());
            if (b != null) {
                b.transact(1003, data, reply, 0);
            }
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "sendStateChangedIntent transact error");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public void showDisableMicrophoneToast() {
        Log.i(TAG, "showDisableMicrophoneToast");
        IBinder b = getAudioService();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            if (b != null) {
                b.transact(1005, data, reply, 0);
            }
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "showDisableMicrophoneToast transact error");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }
}
