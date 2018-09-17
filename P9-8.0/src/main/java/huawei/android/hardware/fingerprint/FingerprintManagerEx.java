package huawei.android.hardware.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintService.Stub;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class FingerprintManagerEx {
    private static final int CODE_GET_TOKEN_LEN = 1103;
    private static final int CODE_IS_FP_NEED_CALIBRATE = 1101;
    private static final int CODE_SET_CALIBRATE_MODE = 1102;
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final String TAG = "FingerprintManagerEx";
    private IFingerprintService mService = Stub.asInterface(ServiceManager.getService("fingerprint"));

    public FingerprintManagerEx(Context context) {
    }

    public int getRemainingNum() {
        if (this.mService != null) {
            try {
                return this.mService.getRemainingNum();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in getRemainingNum: ", e);
            }
        }
        return -1;
    }

    public long getRemainingTime() {
        if (this.mService != null) {
            try {
                return this.mService.getRemainingTime();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in getRemainingTime: ", e);
            }
        }
        return 0;
    }

    public static boolean isFpNeedCalibrate() {
        boolean z;
        boolean z2 = true;
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
            z = FRONT_FINGERPRINT_NAVIGATION;
        } else {
            z = false;
        }
        if (!z) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int result = -1;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(1101, _data, _reply, 0);
                _reply.readException();
                result = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "isFpNeedCalibrate result: " + result);
        if (result != 1) {
            z2 = false;
        }
        return z2;
    }

    public static void setCalibrateMode(int mode) {
        Log.d(TAG, "setCalibrateMode: " + mode);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(mode);
                b.transact(CODE_SET_CALIBRATE_MODE, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static int getTokenLen() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int len = -1;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_GET_TOKEN_LEN, _data, _reply, 0);
                _reply.readException();
                len = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "getTokenLen len: " + len);
        return len;
    }
}
