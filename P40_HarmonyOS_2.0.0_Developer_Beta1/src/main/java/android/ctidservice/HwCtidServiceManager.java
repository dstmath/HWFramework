package android.ctidservice;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.security.IHwEidPlugin;
import huawei.android.security.IHwSecurityService;
import java.util.Optional;

public class HwCtidServiceManager {
    private static final int HW_EID_PLUGIN_ID = 15;
    private static final int RET_DEFAULT_ERROR_VALUE = -2001;
    private static final int RET_EXCEPTION_WHEN_CTID_GET_SEC_IMAGE_CALL = -2003;
    private static final int RET_EXCEPTION_WHEN_CTID_SET_SEC_MODE_CALL = -2002;
    private static final int RET_EXCEPTION_WHEN_GET_CTID_VERSION_CALL = -2004;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwCtidServiceManager";
    private static IHwEidPlugin hwEidPlugin;
    private static volatile HwCtidServiceManager instance = null;
    private IHwSecurityService hwSecurityService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));

    private HwCtidServiceManager() {
        if (this.hwSecurityService == null) {
            Log.e(TAG, "Error, securityservice was null");
        }
    }

    public static HwCtidServiceManager getInstance() {
        if (instance == null) {
            synchronized (HwCtidServiceManager.class) {
                if (instance == null) {
                    instance = new HwCtidServiceManager();
                }
            }
        }
        return instance;
    }

    private Optional<IHwEidPlugin> getHwEidPlugin() {
        IHwEidPlugin iHwEidPlugin = hwEidPlugin;
        if (iHwEidPlugin != null) {
            return Optional.of(iHwEidPlugin);
        }
        IHwSecurityService iHwSecurityService = this.hwSecurityService;
        if (iHwSecurityService != null) {
            try {
                hwEidPlugin = IHwEidPlugin.Stub.asInterface(iHwSecurityService.querySecurityInterface(15));
                if (hwEidPlugin == null) {
                    Log.e(TAG, "Error, IHwEidPlugin is null");
                }
                return Optional.of(hwEidPlugin);
            } catch (RemoteException e) {
                Log.e(TAG, "Get getHwEidPlugin failed");
            }
        }
        return Optional.empty();
    }

    public int ctidSetCameraSecMode() {
        if (!getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return hwEidPlugin.ctidSetSecMode();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid init jnis is invoked");
            return RET_EXCEPTION_WHEN_CTID_SET_SEC_MODE_CALL;
        }
    }

    public int ctidSendSecImage2TA() {
        if (!getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return hwEidPlugin.ctidGetSecImage();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when eid finish jni is invoked");
            return RET_EXCEPTION_WHEN_CTID_GET_SEC_IMAGE_CALL;
        }
    }

    public int ctidGetServiceVerionInfo(byte[] uuid, int uuidLen, String taPath, int[] cmdList, int cmdCount) {
        if (uuid == null || taPath == null || taPath.isEmpty() || cmdList == null || uuidLen < 0 || cmdCount < 0 || !getHwEidPlugin().isPresent()) {
            return RET_DEFAULT_ERROR_VALUE;
        }
        try {
            return hwEidPlugin.ctidGetServiceVerionInfo(uuid, uuidLen, taPath, cmdList, cmdCount);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when ctid_get_service_verion_info jni is invoked");
            return RET_EXCEPTION_WHEN_GET_CTID_VERSION_CALL;
        }
    }
}
