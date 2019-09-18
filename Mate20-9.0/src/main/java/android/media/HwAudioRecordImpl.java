package android.media;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.app.IAppOpsService;

public class HwAudioRecordImpl implements IHwAudioRecord {
    private static final int CONSTANT_NUM = 1004;
    private static final int CONSTANT_VALUE = 1002;
    private static final String TAG = "HwAudioRecordImpl";
    private static IBinder mAudioService = null;
    private static IHwAudioRecord mHwAudioRecoder = new HwAudioRecordImpl();
    private IAppOpsService mAppOps;

    private HwAudioRecordImpl() {
        Log.i(TAG, TAG);
    }

    public static IHwAudioRecord getDefault() {
        return mHwAudioRecoder;
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

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    public boolean isAudioRecordAllowed() {
        String packageName = ActivityThread.currentPackageName();
        if (this.mAppOps == null) {
            this.mAppOps = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        }
        boolean z = true;
        if (this.mAppOps == null) {
            return true;
        }
        try {
            if (this.mAppOps.noteOperation(27, Process.myUid(), packageName) != 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            throw new SecurityException("Unable to noteOperation error");
        }
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
