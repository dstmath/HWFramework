package android.inse;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.inse.IInSEService;

public class InSEImpl {
    private static final int HW_INSE_PLUGIN_ID = 14;
    private static final int RET_DEFAULT_ERROR_VALUE = -2001;
    private static final int RET_EXCEPTION_WHEN_POWERON_CALL = -2002;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "InSEImpl";
    private static IInSEService mInSEManager;
    private static IHwSecurityService mSecurityService;
    private static volatile InSEImpl mSelf = null;

    private InSEImpl() {
        mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
        if (mSecurityService == null) {
            Log.e(TAG, "error, securityserver was null");
        }
    }

    public static InSEImpl getInstance() {
        if (mSelf == null) {
            synchronized (InSEImpl.class) {
                if (mSelf == null) {
                    mSelf = new InSEImpl();
                }
            }
        }
        return mSelf;
    }

    private IInSEService getInSEManagerService() {
        if (mInSEManager != null) {
            return mInSEManager;
        }
        if (mSecurityService != null) {
            try {
                mInSEManager = IInSEService.Stub.asInterface(mSecurityService.querySecurityInterface(14));
                if (mInSEManager == null) {
                    Log.e(TAG, "error, IInSEService  is null");
                }
                return mInSEManager;
            } catch (RemoteException e) {
                Log.e(TAG, "Get getInSEManagerService failed!");
            }
        }
        return null;
    }

    public int inSE_PowerOnDelayed(int time, int id) {
        int ret = RET_DEFAULT_ERROR_VALUE;
        if (getInSEManagerService() != null) {
            try {
                ret = mInSEManager.inSE_PowerOnDelayed(time, id);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while poweron");
                return RET_EXCEPTION_WHEN_POWERON_CALL;
            }
        }
        return ret;
    }
}
