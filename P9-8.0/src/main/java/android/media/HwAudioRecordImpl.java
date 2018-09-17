package android.media;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;
import huawei.android.app.admin.ConstantValue;
import huawei.com.android.internal.widget.HwLockPatternUtils;

public class HwAudioRecordImpl implements IHwAudioRecord {
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

    public void checkRecordActive(int audio_source) {
        Log.i(TAG, "checkRecordActive");
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeInt(audio_source);
            if (b != null) {
                b.transact(ConstantValue.transaction_setWifiDisabled, _data, _reply, 0);
            }
            _reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "checkRecordActive transact e: " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    public boolean isAudioRecordAllowed() {
        boolean z = true;
        String packageName = ActivityThread.currentPackageName();
        if (this.mAppOps == null) {
            this.mAppOps = Stub.asInterface(ServiceManager.getService("appops"));
        }
        if (this.mAppOps == null) {
            return true;
        }
        try {
            if (this.mAppOps.noteOperation(27, Process.myUid(), packageName) != 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            throw new SecurityException("Unable to noteOperation", e);
        }
    }

    public void showDisableMicrophoneToast() {
        Log.i(TAG, "showDisableMicrophoneToast");
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            if (b != null) {
                b.transact(ConstantValue.transaction_isWifiDisabled, _data, _reply, 0);
            }
            _reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "showDisableMicrophoneToast transact e: " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }
}
