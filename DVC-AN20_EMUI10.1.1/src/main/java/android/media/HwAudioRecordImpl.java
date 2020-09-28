package android.media;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.os.ServiceManagerEx;

public class HwAudioRecordImpl extends DefaultHwAudioRecord {
    private static final int CONSTANT_NUM = 1004;
    private static final int CONSTANT_VALUE = 1002;
    private static final String TAG = "HwAudioRecordImpl";
    private static IBinder sAudioService = null;
    private static HwAudioRecordImpl sHwAudioRecoder = new HwAudioRecordImpl();
    private AppOpsManager mAppOps;

    private HwAudioRecordImpl() {
        Log.i(TAG, TAG);
    }

    public static HwAudioRecordImpl getDefault() {
        return sHwAudioRecoder;
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

    private static IBinder getAudioService() {
        IBinder iBinder = sAudioService;
        if (iBinder != null) {
            return iBinder;
        }
        sAudioService = ServiceManagerEx.getService("audio");
        return sAudioService;
    }

    public boolean isAudioRecordAllowed() {
        String packageName = ActivityThreadEx.currentPackageName();
        Context context = ActivityThreadEx.currentApplication().getApplicationContext();
        if (this.mAppOps == null && context != null && (context.getSystemService("appops") instanceof AppOpsManager)) {
            this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        }
        AppOpsManager appOpsManager = this.mAppOps;
        if (appOpsManager == null || AppOpsManagerEx.checkOp(appOpsManager, AppOpsManagerEx.getOp(1), Process.myUid(), packageName) == 0) {
            return true;
        }
        return false;
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
