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
    private static IBinder mAudioService = null;
    private static IHwMediaRecorder mHwMediaRecoder = new HwMediaRecorderImpl();

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
