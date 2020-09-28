package android.media;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.ServiceManagerEx;

public class HwMediaRecorderImpl extends DefaultHwMediaRecorder {
    private static final int CONSTANT_NUM = 1004;
    private static final int CONSTANT_VALUE = 1002;
    private static final String TAG = "HwMediaRecorderImpl";
    private static IBinder sAudioService = null;
    private static HwMediaRecorderImpl sHwMediaRecoder = new HwMediaRecorderImpl();

    private HwMediaRecorderImpl() {
        Log.i(TAG, TAG);
    }

    private static IBinder getAudioService() {
        IBinder iBinder = sAudioService;
        if (iBinder != null) {
            return iBinder;
        }
        sAudioService = ServiceManagerEx.getService("audio");
        return sAudioService;
    }

    public static HwMediaRecorderImpl getDefault() {
        return sHwMediaRecoder;
    }

    public void sendStateChangedIntent(int state) {
        Log.i(TAG, "sendStateChangedIntent, state=" + state);
        IBinder binder = getAudioService();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            data.writeString(MediaRecorder.class.getSimpleName());
            data.writeInt(state);
            data.writeInt(Process.myPid());
            data.writeString(ActivityThreadEx.currentPackageName());
            if (binder != null) {
                binder.transact(1003, data, reply, 0);
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
        IBinder binder = getAudioService();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            if (binder != null) {
                binder.transact(1005, data, reply, 0);
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
