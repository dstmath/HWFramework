package huawei.android.ukey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.hsm.permission.StubController;
import com.huawei.hwpanpayservice.IHwSEService;

public class UKeyManagerImpl {
    private static final int FAILED = -1;
    private static final int SDK_VERSION = 1;
    private static final Object SERVICE_SYNC = new Object();
    private static final String TAG = "UKeyManagerImpl";
    public static final int UNSUPPORT_UKEY = 0;
    private static volatile UKeyManagerImpl sSelf = null;
    private IHwSEService mHwSEService = null;

    private UKeyManagerImpl() {
    }

    public static UKeyManagerImpl getInstance() {
        if (sSelf == null) {
            synchronized (UKeyManagerImpl.class) {
                if (sSelf == null) {
                    sSelf = new UKeyManagerImpl();
                }
            }
        }
        return sSelf;
    }

    private IHwSEService getHwSEService() {
        IHwSEService iHwSEService;
        synchronized (SERVICE_SYNC) {
            HwSEServiceManager.initRemoteService(ActivityThreadEx.currentApplication().getApplicationContext());
            this.mHwSEService = HwSEServiceManager.getRemoteServiceInstance();
            if (this.mHwSEService != null) {
                Log.d(TAG, "getHwPanPayService successfully");
            } else {
                Log.e(TAG, "getHwPanPayService failed!!!!");
            }
            iHwSEService = this.mHwSEService;
        }
        return iHwSEService;
    }

    public int getSDKVersion() {
        return 1;
    }

    public int getUKeyVersion() {
        Log.i(TAG, "getUKeyStatus.");
        IHwSEService hwSEService = getHwSEService();
        if (hwSEService == null) {
            return 0;
        }
        try {
            return hwSEService.isSwitchFeatureOn();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException while getting ukey version.");
            return 0;
        }
    }

    public int getUKeyStatus(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        Log.i(TAG, "getUKeyStatus : " + packageName);
        IHwSEService hwSEService = getHwSEService();
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        }
        if (hwSEService != null) {
            try {
                return hwSEService.isUKeySwitchDisabled(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey switch status.");
            }
        }
        return -1;
    }

    public void requestUKeyPermission(Context context, int requestCode) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.ukey.UKeyPermissionActivity");
            intent.putExtra(StubController.TABLE_COLUM_PACKAGE_NAME, context.getApplicationInfo().packageName);
            intent.putExtra("appName", context.getApplicationInfo().loadLabel(context.getPackageManager()));
            Log.d(TAG, "PackageName = " + context.getApplicationInfo().packageName + "AppName = " + ((Object) context.getApplicationInfo().loadLabel(context.getPackageManager())));
            try {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                Log.e(TAG, "Exception when requestUKeyPermission is invoked.");
            }
        }
    }

    public int createUKey(String spID, String ssdAid, String sign, String timeStamp) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(ssdAid) || TextUtils.isEmpty(sign) || TextUtils.isEmpty(timeStamp)) {
            return -1;
        }
        Log.i(TAG, "CreateUKey.");
        IHwSEService hwSEService = getHwSEService();
        if (hwSEService != null) {
            try {
                return hwSEService.createSSD(spID, sign, timeStamp, ssdAid);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when createUKey is invoked.");
            }
        }
        return -1;
    }

    public int deleteUKey(String spID, String ssdAid, String sign, String timeStamp) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(ssdAid) || TextUtils.isEmpty(sign) || TextUtils.isEmpty(timeStamp)) {
            return -1;
        }
        Log.i(TAG, "DeleteUKey.");
        IHwSEService hwSEService = getHwSEService();
        if (hwSEService != null) {
            try {
                return hwSEService.deleteSSD(spID, sign, timeStamp, ssdAid);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when deleteUKey is invoked.");
            }
        }
        return -1;
    }

    public String getUKeyID() {
        Log.i(TAG, "GetUKeyID.");
        IHwSEService hwSEService = getHwSEService();
        if (hwSEService == null) {
            return null;
        }
        try {
            return hwSEService.getCplc();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when getUKeyID is invoked.");
            return null;
        }
    }

    public int syncUKey(String spID, String sign, String timeStamp) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(sign) || TextUtils.isEmpty(timeStamp)) {
            return -1;
        }
        Log.i(TAG, "SyncUKey.");
        IHwSEService hwSEService = getHwSEService();
        if (hwSEService != null) {
            try {
                return hwSEService.initEse(spID, sign, timeStamp);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when syncUKey is invoked.");
            }
        }
        return -1;
    }
}
