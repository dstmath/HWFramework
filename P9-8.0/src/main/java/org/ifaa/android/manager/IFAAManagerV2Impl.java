package org.ifaa.android.manager;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IIFAAPlugin;
import huawei.android.security.IIFAAPluginCallBack.Stub;
import org.ukey.android.manager.IUKeyManager;

public class IFAAManagerV2Impl extends IFAAManagerV2 {
    private static final int CH_ALG_VERSION = SystemProperties.getInt(CH_ALG_VERSION_PRO, 0);
    private static final String CH_ALG_VERSION_PRO = "ro.config.hw_ch_alg";
    private static final boolean HW_DEBUG;
    public static final int IFAA_AUTH_FINGERPRINT = 1;
    public static final int IFAA_AUTH_IRIS = 2;
    public static final int IFAA_AUTH_NONE = 0;
    public static final int IFAA_AUTH_PIN = 8;
    public static final int IFAA_FAIL = -1;
    public static final int IFAA_OK = 0;
    private static final int IFAA_PLUGIN_ID = 3;
    public static final int IFAA_VERSION = 2;
    private static final String SETTINGS_FP_CLASS = "com.android.settings.fingerprint.FingerprintSettingsActivity";
    private static final String SETTINGS_FP_PACKAGE = "com.android.settings";
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_IDLE = 0;
    private static final String TAG = "IFAAManagerImpl";
    private IFAACallBack mCallback = new IFAACallBack(this);
    FingerprintManager mFingerprintManager;
    private IIFAAPlugin mIFAAPlugin;
    private boolean mNotified;
    byte[] mRecData;
    int mRet;
    private Object mSignal = new Object();
    int mStatus = 0;
    private Object mStatusLock = new Object();

    static class IFAACallBack extends Stub {
        private IFAAManagerV2Impl mImpl;

        public IFAACallBack(IFAAManagerV2Impl impl) {
            this.mImpl = impl;
        }

        public void processCmdResult(int ret, byte[] param) {
            if (this.mImpl != null) {
                if (IFAAManagerV2Impl.HW_DEBUG) {
                    Log.d(IFAAManagerV2Impl.TAG, "IFAA processCmdResult enter notify!!!");
                }
                this.mImpl.notifyResult(ret, param);
            }
        }
    }

    static {
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : HW_DEBUG;
        }
        HW_DEBUG = z;
    }

    private IIFAAPlugin getIFAAPlugin() {
        if (this.mIFAAPlugin == null) {
            IBinder b = ServiceManager.getService("securityserver");
            if (b != null) {
                if (HW_DEBUG) {
                    Log.d(TAG, "getHwSecurityService");
                }
                try {
                    IBinder ifaaPluginBinder = IHwSecurityService.Stub.asInterface(b).bind(IFAA_PLUGIN_ID, this.mCallback);
                    if (ifaaPluginBinder != null) {
                        this.mIFAAPlugin = IIFAAPlugin.Stub.asInterface(ifaaPluginBinder);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error in get IFAA Plugin ");
                }
            } else {
                Log.e(TAG, "getHwSecurityService failed!!!!");
            }
        }
        return this.mIFAAPlugin;
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0029 A:{ExcHandler: java.lang.LinkageError (r0_0 'ex' java.lang.Throwable), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0029 A:{ExcHandler: java.lang.LinkageError (r0_0 'ex' java.lang.Throwable), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0029 A:{ExcHandler: java.lang.LinkageError (r0_0 'ex' java.lang.Throwable), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0029 A:{ExcHandler: java.lang.LinkageError (r0_0 'ex' java.lang.Throwable), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0029 A:{ExcHandler: java.lang.LinkageError (r0_0 'ex' java.lang.Throwable), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0029 A:{ExcHandler: java.lang.LinkageError (r0_0 'ex' java.lang.Throwable), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:8:0x0029, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x002a, code:
            android.util.Log.e(TAG, r0.getMessage(), r0);
     */
    /* JADX WARNING: Missing block: B:10:0x0034, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IUKeyManager getUkeyManager() {
        try {
            Class klass = Class.forName("org.ukey.android.manager.UKeyManager");
            if (klass != null) {
                return (IUKeyManager) klass.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            }
            Log.e(TAG, "ukeyManager is NULL");
            return null;
        } catch (Throwable ex) {
        }
    }

    public int getSupportBIOTypes(Context context) {
        if (HW_DEBUG) {
            Log.d(TAG, "getSupportBIOTypes");
        }
        int type = 0;
        this.mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
        if (this.mFingerprintManager != null && this.mFingerprintManager.isHardwareDetected()) {
            type = 1;
        }
        IUKeyManager ukeyManager = getUkeyManager();
        if (ukeyManager != null && ukeyManager.getUKeyVersion() >= 2 && CH_ALG_VERSION >= 2) {
            type |= 8;
        }
        if (ukeyManager == null) {
            Log.e(TAG, "ukeyManager is NULL");
        } else if (HW_DEBUG) {
            Log.d(TAG, "ukeyManager is version is " + ukeyManager.getUKeyVersion());
        }
        if (HW_DEBUG) {
            Log.d(TAG, "getSupportBIOTypes is " + type + "and ch alg version is" + CH_ALG_VERSION);
        }
        return type;
    }

    public int startBIOManager(Context context, int authType) {
        if (HW_DEBUG) {
            Log.d(TAG, "startBIOManager");
        }
        if (authType != 1) {
            return -1;
        }
        Intent i = new Intent("android.settings.SETTINGS");
        i.setClassName(SETTINGS_FP_PACKAGE, SETTINGS_FP_CLASS);
        i.setFlags(268435456);
        context.startActivity(i);
        return 0;
    }

    public String getDeviceModel() {
        String deviceModel = SystemProperties.get("ro.product.fingerprintName");
        if (HW_DEBUG) {
            Log.d(TAG, "getDeviceModel: [" + deviceModel + "]");
        }
        return deviceModel;
    }

    public int getVersion() {
        if (HW_DEBUG) {
            Log.d(TAG, "getVersion");
        }
        return 2;
    }

    public byte[] processCmdV2(Context context, byte[] param) {
        if (HW_DEBUG) {
            Log.d(TAG, "IFAA processCmdV2");
        }
        byte[] retByte = new byte[0];
        if (preProcessCmd()) {
            IIFAAPlugin ifaaPlugin = getIFAAPlugin();
            if (ifaaPlugin != null) {
                try {
                    ifaaPlugin.processCmd(this.mCallback, param);
                } catch (RemoteException e) {
                    Log.e(TAG, "processCmd failed: " + e);
                }
                try {
                    synchronized (this.mSignal) {
                        if (HW_DEBUG) {
                            Log.d(TAG, "IFAA processCmdV2 enter wait!!!");
                        }
                        if (!this.mNotified) {
                            this.mSignal.wait();
                        }
                        this.mNotified = HW_DEBUG;
                        if (HW_DEBUG) {
                            Log.d(TAG, "IFAA processCmdV2 enter waited ok!!!");
                        }
                    }
                } catch (InterruptedException e2) {
                    if (HW_DEBUG) {
                        Log.d(TAG, "IFAA processCmdV2 interrupted!!!");
                    }
                }
                if (this.mRet == 0) {
                    retByte = this.mRecData;
                }
            } else {
                Log.e(TAG, "IIFAAPlugin get failed!!!");
            }
            endProcessCmd();
            return retByte;
        }
        Log.e(TAG, "IFAA processCmdV2 failed because is busy!");
        return retByte;
    }

    private boolean preProcessCmd() {
        synchronized (this.mStatusLock) {
            if (this.mStatus == 0) {
                this.mStatus = 1;
                this.mNotified = HW_DEBUG;
                return true;
            }
            return HW_DEBUG;
        }
    }

    private void endProcessCmd() {
        synchronized (this.mStatusLock) {
            this.mStatus = 0;
        }
    }

    private void notifyResult(int ret, byte[] param) {
        synchronized (this.mStatusLock) {
            if (this.mStatus == 1) {
                this.mRet = ret;
                this.mRecData = param;
                synchronized (this.mSignal) {
                    this.mSignal.notifyAll();
                    this.mNotified = true;
                }
            }
        }
    }
}
