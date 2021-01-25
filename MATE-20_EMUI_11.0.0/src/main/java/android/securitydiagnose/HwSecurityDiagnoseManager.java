package android.securitydiagnose;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.hwpartsecurity.BuildConfig;
import huawei.android.security.IHwAntiMalPlugin;
import huawei.android.security.IHwSecurityDiagnoseCallback;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import huawei.android.security.IHwSecurityService;

public class HwSecurityDiagnoseManager {
    private static final String ANTIMAL_KEY_PROTECT_TYPE = "protect_type";
    public static final String ANTIMAL_MALDATA_INSTALL_BEGIN_TIME = "begintime";
    public static final String ANTIMAL_MALDATA_INSTALL_END_TIME = "endtime";
    public static final String ANTIMAL_MALDATA_PACKAGE_NAME = "pkg";
    public static final String ANTIMAL_MALDATA_PACKAGE_NAME_LIST = "pkglist";
    public static final String ANTIMAL_MALDATA_SOURCE_APP = "src";
    public static final String ANTIMAL_PROTECTION_PACKAGE_NAME = "pkg";
    public static final String ANTIMAL_PROTECTION_SRC = "src";
    public static final String ANTIMAL_PROTECTION_UID = "uid";
    public static final int ANTIMAL_PROTECT_POLICY_ALLOW = 0;
    public static final int ANTIMAL_PROTECT_POLICY_DENY = 1;
    public static final int ANTIMAL_PROTECT_POLICY_ERROR = -1;
    public static final int ANTIMAL_PROTECT_POLICY_LOGIN_HUAWEI_ACCOUNT = 2;
    private static final int ANTIMAL_PROTECT_TYPE_DEFAULT = 0;
    private static final int ANTIMAL_PROTECT_TYPE_LAUNCHER_OLD = 2;
    private static final long CALL_ENOUGH_TIME_FOR_ANTIMAL = 900;
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int DEVICE_USAGE_PLUGIN_ID = 1;
    private static final int GET_ROOT_STATUS_ERR = -1;
    private static final int HW_ANTIMAL_PLUGIN_ID = 16;
    private static final long SCREENON_ENOUGH_TIME_FOR_ANTIMAL = 360000;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final int STP_ITEM_CREDIBLE = 1;
    private static final int STP_ITEM_REFERENCE = 0;
    private static final int STP_ITEM_RISK = 1;
    private static final int STP_ITEM_SAFE = 0;
    private static final int STP_STATUS_BUFF_LEN = 4096;
    private static final String TAG = "HwSecurityDiagnoseManager";
    private static final int USER_CLIENT_DEFAULT_ERROR = -2000;
    private static volatile HwSecurityDiagnoseManager sInstance;
    private IHwAntiMalPlugin mHwAntiMalPlugin = null;
    private IHwSecurityService mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));

    public enum AntiMalDataSrcType {
        BEGIN,
        PMS,
        HISUITE,
        CLONE,
        ACCESSIBILITY,
        END
    }

    public enum AntiMalProtectLauncherType {
        BEGIN,
        PMS,
        HW_DPM,
        SETTINGS,
        END
    }

    public enum AntiMalProtectType {
        BEGIN,
        LAUNCHER,
        DEVICE_MANAGER,
        ACCESSIBILITY,
        ADB,
        END
    }

    private HwSecurityDiagnoseManager() {
        if (this.mSecurityService == null) {
            Log.e(TAG, "error, securityservice was null");
        }
    }

    public static HwSecurityDiagnoseManager getInstance() {
        if (sInstance == null) {
            synchronized (HwSecurityDiagnoseManager.class) {
                if (sInstance == null) {
                    sInstance = new HwSecurityDiagnoseManager();
                }
            }
        }
        return sInstance;
    }

    private IHwSecurityDiagnosePlugin getHwSecurityDiagnosePlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    IHwSecurityDiagnosePlugin securityDiagnoseService = IHwSecurityDiagnosePlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(2));
                    if (securityDiagnoseService == null) {
                        Log.e(TAG, "error, HwSecurityDiagnosePlugin is null");
                    }
                    return securityDiagnoseService;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwSecurityDiagnosePlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    private IHwAntiMalPlugin getHwAntiMalPlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                if (this.mHwAntiMalPlugin != null) {
                    return this.mHwAntiMalPlugin;
                }
                try {
                    IBinder sPlugin = this.mSecurityService.querySecurityInterface(16);
                    this.mHwAntiMalPlugin = IHwAntiMalPlugin.Stub.asInterface(sPlugin);
                    if (sPlugin != null) {
                        sPlugin.linkToDeath(new IBinder.DeathRecipient() {
                            /* class android.securitydiagnose.HwSecurityDiagnoseManager.AnonymousClass1 */

                            @Override // android.os.IBinder.DeathRecipient
                            public void binderDied() {
                                HwSecurityDiagnoseManager.this.mHwAntiMalPlugin = null;
                                Log.e(HwSecurityDiagnoseManager.TAG, "getHwAntiMalPlugin sPlugin is died.");
                            }
                        }, 0);
                    } else {
                        Log.i(TAG, "mHwAntiMalPlugin is null");
                    }
                    return this.mHwAntiMalPlugin;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwAntiMalPlugin invoked");
                } catch (Exception e2) {
                    Log.e(TAG, "Exception when getHwAntiMalPlugin invoked");
                } catch (Error e3) {
                    Log.e(TAG, "Error when getHwAntiMalPlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    public void getRootStatus(IHwSecurityDiagnoseCallback callback) {
        if (callback != null) {
            IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
            if (plugin != null) {
                try {
                    plugin.getRootStatus(callback);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getRootStatus is invoked");
                    try {
                        callback.onRootStatus(-1);
                    } catch (RemoteException ex) {
                        Log.e(TAG, "RemoteException when onRootStatus is invoked, " + ex.getMessage());
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an get root status callback");
        }
    }

    public boolean isThirdPartyLauncherProtectionOn() {
        return false;
    }

    @Deprecated
    public boolean isAntiMalProtectionOn(Context context, Bundle params) {
        return false;
    }

    public int getAntimalProtectionPolicy(int type, Bundle params) {
        if (getHwAntiMalPlugin() == null) {
            return 0;
        }
        try {
            return this.mHwAntiMalPlugin.getAntimalProtectionPolicy(type, params);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when getAntimalProtectionPolicy is invoked");
            return 0;
        } catch (Exception e2) {
            Log.e(TAG, "Exception when getAntimalProtectionPolicy is invoked");
            return 0;
        }
    }

    public boolean setMalData(int type, Bundle features) {
        if (getHwAntiMalPlugin() == null) {
            return false;
        }
        try {
            return this.mHwAntiMalPlugin.setMalData(type, features);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when setMalData is invoked");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "Exception when setMalData is invoked");
            return false;
        }
    }

    public int getRootStatusSync() {
        IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
        if (plugin == null) {
            return USER_CLIENT_DEFAULT_ERROR;
        }
        try {
            return plugin.getRootStatusSync();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException occurs when get root status synchronized");
            return USER_CLIENT_DEFAULT_ERROR;
        }
    }

    public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition_info) {
        if ((status == 0 || status == 1) && (credible == 0 || credible == 1)) {
            IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
            if (plugin == null) {
                return USER_CLIENT_DEFAULT_ERROR;
            }
            try {
                return plugin.sendThreatenInfo(id, status, credible, version, name, addition_info);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occurs when sending threaten info to stp hidl deamon");
                return USER_CLIENT_DEFAULT_ERROR;
            }
        } else {
            Log.e(TAG, "Invalid stp item input arguments , status : " + ((int) status) + " ,credible: " + ((int) credible));
            return USER_CLIENT_DEFAULT_ERROR;
        }
    }

    public static final class StpExtraStatusInfo {
        private StringBuffer stpExtraStatusBuffer = new StringBuffer(BuildConfig.FLAVOR);
        private int stpExtraStatusBufferLen = 0;

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setStpExtraStatusBuffer(String buffer) {
            this.stpExtraStatusBuffer.append(buffer);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setStpExtraStatusBufferLen(int len) {
            this.stpExtraStatusBufferLen = len;
        }

        public StringBuffer getStpExtraStatusBuffer() {
            return this.stpExtraStatusBuffer;
        }

        public int getStpExtraStatusBufferLen() {
            return this.stpExtraStatusBufferLen;
        }
    }

    public int getStpStatusByCategory(int category, boolean inDetail, boolean withHistory, StpExtraStatusInfo stpExtraStatusInfo) {
        int ret = USER_CLIENT_DEFAULT_ERROR;
        if (stpExtraStatusInfo == null) {
            Log.e(TAG, "stpExtraStatusInfo is null");
            return USER_CLIENT_DEFAULT_ERROR;
        }
        IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
        if (plugin == null) {
            return USER_CLIENT_DEFAULT_ERROR;
        }
        try {
            char[] outBuff = new char[4096];
            int[] outBuffLen = new int[1];
            ret = plugin.getStpStatusByCategory(category, inDetail, withHistory, outBuff, outBuffLen);
            stpExtraStatusInfo.setStpExtraStatusBuffer(new String(outBuff));
            stpExtraStatusInfo.setStpExtraStatusBufferLen(outBuffLen[0]);
            return ret;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException occurs when geting status by category to stp hidl deamon");
            return ret;
        } catch (Exception e2) {
            Log.e(TAG, "Other exception occurs when geting status by category");
            return ret;
        }
    }

    public int startKernelDetection(int uid) {
        IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
        if (plugin != null) {
            try {
                return plugin.startKernelDetection(uid);
            } catch (RemoteException e) {
                Log.e(TAG, "startKernelDetection: RemoteException");
                return USER_CLIENT_DEFAULT_ERROR;
            }
        } else {
            Log.e(TAG, "startKernelDetection: plugin is null");
            return USER_CLIENT_DEFAULT_ERROR;
        }
    }

    public int updateKernelDetectionConfig(int[] conf) {
        IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
        if (plugin != null) {
            try {
                return plugin.updateKernelDetectionConfig(conf);
            } catch (RemoteException e) {
                Log.e(TAG, "updateKernelDetectionConfig: RemoteException");
                return USER_CLIENT_DEFAULT_ERROR;
            }
        } else {
            Log.e(TAG, "updateKernelDetectionConfig: plugin is null");
            return USER_CLIENT_DEFAULT_ERROR;
        }
    }

    public int stopKernelDetection(int uid) {
        IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
        if (plugin != null) {
            try {
                return plugin.stopKernelDetection(uid);
            } catch (RemoteException e) {
                Log.e(TAG, "stopKernelDetection: RemoteException");
                return USER_CLIENT_DEFAULT_ERROR;
            }
        } else {
            Log.e(TAG, "stopKernelDetection: plugin is null");
            return USER_CLIENT_DEFAULT_ERROR;
        }
    }
}
