package android.ctidservice;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwEidPlugin;
import huawei.android.security.IHwSecurityService;

public class HwCtidServiceManager {
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int HW_EID_PLUGIN_ID = 15;
    private static final int RET_DEFAULT_ERROR_VALUE = -2001;
    private static final int RET_EXCEPTION_WHEN_CTID_GETSECIMAGE_CALL = -2003;
    private static final int RET_EXCEPTION_WHEN_CTID_SETSECMODE_CALL = -2002;
    private static final int RET_EXCEPTION_WHEN_GET_CTID_VERSION_CALL = -2004;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwCtidServiceManager";
    private static IHwEidPlugin mIHwEidPlugin;
    private static volatile HwCtidServiceManager sInstance = null;
    private IHwSecurityService mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));

    private HwCtidServiceManager() {
        if (this.mSecurityService == null) {
            Log.e(TAG, "error, securityservice was null");
        }
    }

    public static HwCtidServiceManager getInstance() {
        if (sInstance == null) {
            synchronized (HwCtidServiceManager.class) {
                if (sInstance == null) {
                    sInstance = new HwCtidServiceManager();
                }
            }
        }
        return sInstance;
    }

    private IHwEidPlugin getHwEidPlugin() {
        if (mIHwEidPlugin != null) {
            return mIHwEidPlugin;
        }
        if (this.mSecurityService != null) {
            try {
                mIHwEidPlugin = IHwEidPlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(15));
                if (mIHwEidPlugin == null) {
                    Log.e(TAG, "error, IHwEidPlugin is null");
                }
                return mIHwEidPlugin;
            } catch (RemoteException e) {
                Log.e(TAG, "Get getHwEidPlugin failed!");
            }
        }
        return null;
    }

    public int ctidSetCameraSecMode() {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (getHwEidPlugin() != null) {
            try {
                ret = mIHwEidPlugin.ctid_set_sec_mode();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid init jnis is invoked");
                return RET_EXCEPTION_WHEN_CTID_SETSECMODE_CALL;
            }
        }
        return ret;
    }

    public int ctidSendSecImage2TA() {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (getHwEidPlugin() != null) {
            try {
                ret = mIHwEidPlugin.ctid_get_sec_image();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when eid finish jni is invoked");
                return RET_EXCEPTION_WHEN_CTID_GETSECIMAGE_CALL;
            }
        }
        return ret;
    }

    public int ctidGetServiceVerionInfo(byte[] uuid, int uuidLen, String taPath, int[] cmdList, int cmdCount) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (getHwEidPlugin() != null) {
            try {
                ret = mIHwEidPlugin.ctid_get_service_verion_info(uuid, uuidLen, taPath, cmdList, cmdCount);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when ctid_get_service_verion_info jni is invoked");
                return RET_EXCEPTION_WHEN_GET_CTID_VERSION_CALL;
            }
        }
        return ret;
    }
}
