package com.android.server.security.fileprotect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInstalld;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.util.Slog;
import com.android.server.security.core.IHwSecurityPlugin;
import huawei.android.security.IHwSfpService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwSfpService extends IHwSfpService.Stub implements IHwSecurityPlugin {
    private static final String ACTION_LOCK_SCREEN = "lockScreen";
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            return new HwSfpService(context);
        }

        public String getPluginPermission() {
            return null;
        }
    };
    private static final String INTENT_SLIDE_UNLOCK = "slideUnlock";
    private static final String INTENT_SMART_UNLOCK = "smartUnlock";
    private static final String INTENT_USER_ID = "userId";
    private static final int INVALID_PRELOAD_STATUS = -1;
    private static final String LOCK_PERMISSION = "com.isec.lockScreenBroadcast";
    private static final String POLICY_CONFIG_FILE = "sfpconfig.json";
    private static final int PRELOAD_STATUS = 1;
    private static final String TAG = "HwSfpService";
    private static final String[] multipleUserPath = {"/data/user/0/", "/storage/emulated/0/"};
    private Context mContext;
    private IInstalld mInstalld;
    private List<PackStoragePolicy> mPackagePolicies = new ArrayList();
    private IStorageManager mStorageManager;

    class LockScreenReceiver extends BroadcastReceiver {
        LockScreenReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (HwSfpService.ACTION_LOCK_SCREEN.equals(intent.getAction())) {
                boolean smartUnlock = intent.getBooleanExtra(HwSfpService.INTENT_SMART_UNLOCK, false);
                boolean slideUnlock = intent.getBooleanExtra(HwSfpService.INTENT_SLIDE_UNLOCK, false);
                if (smartUnlock || slideUnlock) {
                    Slog.d(HwSfpService.TAG, "skip the broadcast: smartUnlock=" + smartUnlock + " and slideUnlock=" + slideUnlock);
                    return;
                }
                int userId = intent.getIntExtra(HwSfpService.INTENT_USER_ID, -1);
                if (userId == -1) {
                    Slog.d(HwSfpService.TAG, "skip the broadcast: userId=null");
                    return;
                }
                UserInfo userInfo = HwSfpService.this.getUserInfo(userId);
                if (userInfo != null) {
                    new PolicyTask(userId, userInfo.serialNumber).start();
                }
            }
        }
    }

    private class PolicyTask extends Thread {
        private int serialNumber;
        private int userId;

        public PolicyTask(int userId2, int serialNumber2) {
            this.userId = userId2;
            this.serialNumber = serialNumber2;
        }

        public void run() {
            HwSfpService.this.execPolicies(this.userId, this.serialNumber);
            HwSfpService.this.deleteKey(this.userId, this.serialNumber);
        }
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.fileprotect.HwSfpService, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    public HwSfpService(Context context) {
        this.mContext = context;
    }

    public void onStart() {
        Slog.d(TAG, "onStart");
        this.mPackagePolicies = PackStoragePolicy.parse(this.mContext, POLICY_CONFIG_FILE);
        Slog.d(TAG, "policy size: " + this.mPackagePolicies.size());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOCK_SCREEN);
        this.mContext.registerReceiverAsUser(new LockScreenReceiver(), UserHandle.ALL, filter, LOCK_PERMISSION, null);
    }

    public void onStop() {
    }

    public String getKeyDesc(int userId, int storageType) {
        String result = null;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = getUserInfo(userId);
            if (userInfo != null) {
                result = getKeyDesc(userId, userInfo.serialNumber, storageType);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public List<String> getSensitiveDataPolicyList() {
        List<String> result = new ArrayList<>();
        for (PackStoragePolicy packPolicy : this.mPackagePolicies) {
            result.add(packPolicy.packageName);
        }
        return result;
    }

    /* access modifiers changed from: private */
    public UserInfo getUserInfo(int userId) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager == null) {
            Slog.e(TAG, "UserManager is not found");
            return null;
        }
        UserInfo userInfo = userManager.getUserInfo(userId);
        if (userInfo == null) {
            Slog.e(TAG, "cannot get the UserInfo: userId=" + userId);
        }
        return userInfo;
    }

    /* access modifiers changed from: private */
    public void deleteKey(int userId, int serialNumber) {
        try {
            getStorageManager().lockUserScreenISec(userId, serialNumber);
        } catch (RemoteException e) {
            Slog.e(TAG, "fialed to delete key: " + e.getMessage());
        }
    }

    private synchronized IStorageManager getStorageManager() {
        if (this.mStorageManager == null) {
            try {
                this.mStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            } catch (Exception e) {
                Slog.v(TAG, e.getMessage());
            }
        }
        return this.mStorageManager;
    }

    private synchronized IInstalld getInstalld() {
        if (this.mInstalld == null) {
            this.mInstalld = IInstalld.Stub.asInterface(ServiceManager.getService("installd"));
        }
        return this.mInstalld;
    }

    private String convertPathToUser(String path, int userId) {
        for (String subPath : multipleUserPath) {
            if (path.startsWith(subPath)) {
                return path.replaceFirst("0", String.valueOf(userId));
            }
        }
        return null;
    }

    public void execPolicies(int userId, int serialNumber) {
        int i = userId;
        int i2 = serialNumber;
        Map<Integer, String> keyDescMap = new HashMap<>();
        String eceKeyDesc = getKeyDesc(i, i2, 2);
        String seceKeyDesc = getKeyDesc(i, i2, 3);
        if (eceKeyDesc == null || seceKeyDesc == null) {
            Slog.d(TAG, "cannot get the eceKeyDesc or seceKeyDesc: userId=" + i);
            return;
        }
        keyDescMap.put(2, eceKeyDesc);
        keyDescMap.put(3, seceKeyDesc);
        IStorageManager storageManager = getStorageManager();
        if (storageManager != null) {
            for (PackStoragePolicy packagePolicy : this.mPackagePolicies) {
                int i3 = -1;
                try {
                    if (storageManager.getPreLoadPolicyFlag(i, i2) == 1) {
                        if (!isInstalledApp(packagePolicy.packageName, i)) {
                            Slog.d(TAG, "package is not found: " + packagePolicy.packageName);
                        } else {
                            for (PathPolicy pathPolicy : packagePolicy.policies) {
                                if (pathPolicy.encryptionType != i3) {
                                    String path = convertPathToUser(pathPolicy.path, i);
                                    if (path != null) {
                                        setFileXattr(path, keyDescMap.get(Integer.valueOf(pathPolicy.encryptionType)), pathPolicy.encryptionType, pathPolicy.fileType);
                                        i3 = -1;
                                    }
                                }
                            }
                        }
                    } else {
                        return;
                    }
                } catch (RemoteException e) {
                    RemoteException remoteException = e;
                    Slog.e(TAG, e.getMessage());
                    return;
                }
            }
        }
    }

    private boolean isInstalledApp(String packageName, int userId) {
        try {
            this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, userId);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String getKeyDesc(int userId, int serialNumber, int storageType) {
        IStorageManager storageManager = getStorageManager();
        if (storageManager == null) {
            Slog.e(TAG, "StorageManager is not found");
            return null;
        }
        try {
            String origin = storageManager.getKeyDesc(userId, serialNumber, storageType);
            if (origin == null || !origin.startsWith("V1[keydesc_ERR:")) {
                return origin;
            }
            return null;
        } catch (RemoteException | RuntimeException e) {
            Slog.e(TAG, "Failed to getKeyDesc: " + e.getMessage());
            return null;
        }
    }

    private void setFileXattr(String path, String keyDesc, int storageType, int fileType) {
        IInstalld installd = getInstalld();
        if (installd == null) {
            Slog.e(TAG, "IInstalld is not found");
            return;
        }
        try {
            installd.setFileXattr(path, keyDesc, storageType, fileType);
        } catch (RemoteException e) {
            String errorMsg = e.getMessage();
            Slog.e(TAG, "RemoteException" + errorMsg);
        } catch (Exception e2) {
            String error = e2.getMessage();
            Slog.e(TAG, "Exception" + error);
        }
    }
}
