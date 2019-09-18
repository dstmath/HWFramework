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
import huawei.android.security.IIFAAPluginCallBack;
import java.lang.reflect.InvocationTargetException;
import org.ukey.android.manager.IUKeyManager;

public class IFAAManagerV2Impl extends IFAAManagerV2 {
    private static final int CH_ALG_VERSION = SystemProperties.getInt(CH_ALG_VERSION_PRO, 0);
    private static final String CH_ALG_VERSION_PRO = "ro.config.hw_ch_alg";
    private static final int IFAA_PLUGIN_ID = 3;
    private static final String LOG_TAG = "IFAAManagerImpl";
    private static final String SET_FP_CLASS = "com.android.settings.fingerprint.FingerprintSettingsActivity";
    private static final String SET_FP_PACKAGE = "com.android.settings";
    private static final int STATUS_BUSY = 1;
    private static final int STATUS_IDLE = 0;
    private final IFAACallBack mCallback = new IFAACallBack();
    private IIFAAPlugin mIFAAPlugin;
    private boolean mNotified;
    private byte[] mRecData;
    private int mRet;
    private final Object mSignal = new Object();
    private int mStatus = 0;
    private final Object mStatusLock = new Object();

    static class IFAACallBack extends IIFAAPluginCallBack.Stub {
        private final IFAAManagerV2Impl mImpl;

        private IFAACallBack(IFAAManagerV2Impl impl) {
            this.mImpl = impl;
        }

        public void processCmdResult(int ret, byte[] param) {
            if (this.mImpl != null) {
                Log.d(IFAAManagerV2Impl.LOG_TAG, "IFAA processCmdResult enter notify!!!");
                this.mImpl.notifyResult(ret, param);
            }
        }
    }

    public IFAAManagerV2Impl(Context context) {
    }

    /* JADX WARNING: type inference failed for: r3v1, types: [org.ifaa.android.manager.IFAAManagerV2Impl$IFAACallBack, android.os.IBinder] */
    private IIFAAPlugin getIFAAPlugin() {
        if (this.mIFAAPlugin == null) {
            IBinder binder = ServiceManager.getService("securityserver");
            if (binder != null) {
                Log.d(LOG_TAG, "getHwSecurityService");
                try {
                    IBinder ifaaPluginBinder = IHwSecurityService.Stub.asInterface(binder).bind(3, this.mCallback);
                    if (ifaaPluginBinder != null) {
                        this.mIFAAPlugin = IIFAAPlugin.Stub.asInterface(ifaaPluginBinder);
                    }
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "Error in get IFAA Plugin ");
                }
            } else {
                Log.e(LOG_TAG, "getHwSecurityService failed!!!!");
            }
        }
        return this.mIFAAPlugin;
    }

    private static IUKeyManager getUkeyManager() {
        try {
            return (IUKeyManager) Class.forName("org.ukey.android.manager.UKeyManager").getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | LinkageError | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            Log.e(LOG_TAG, "getUkeyManager failed!");
            return null;
        }
    }

    public int getSupportBIOTypes(Context context) {
        Log.d(LOG_TAG, "getSupportBIOTypes");
        int type = 0;
        FingerprintManager mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
        if (mFingerprintManager != null && mFingerprintManager.isHardwareDetected()) {
            type = 0 | 1;
        }
        IUKeyManager manager = getUkeyManager();
        int ukeyVersion = manager == null ? 0 : manager.getUKeyVersion();
        if (ukeyVersion >= 2 && CH_ALG_VERSION >= 2) {
            type |= 8;
        }
        Log.d(LOG_TAG, "getSupportBIOTypes is " + type + "and ch alg version is" + CH_ALG_VERSION + "manager version is " + ukeyVersion);
        return type;
    }

    public int startBIOManager(Context context, int authType) {
        Log.d(LOG_TAG, "startBIOManager");
        if (authType != 1) {
            return -1;
        }
        Intent intent = new Intent("android.settings.SETTINGS");
        intent.setClassName(SET_FP_PACKAGE, SET_FP_CLASS);
        intent.setFlags(268435456);
        context.startActivity(intent);
        return 0;
    }

    public String getDeviceModel() {
        String deviceModel = SystemProperties.get("ro.product.fingerprintName");
        Log.d(LOG_TAG, "getDeviceModel: [" + deviceModel + "]");
        return deviceModel;
    }

    public int getVersion() {
        Log.d(LOG_TAG, "getVersion");
        return 2;
    }

    public byte[] processCmdV2(Context context, byte[] param) {
        Log.d(LOG_TAG, "IFAA processCmdV2");
        byte[] retByte = new byte[0];
        if (!isOkProcessCmd()) {
            Log.e(LOG_TAG, "IFAA processCmdV2 failed because is busy!");
            return retByte;
        }
        IIFAAPlugin ifaaPlugin = getIFAAPlugin();
        if (ifaaPlugin != null) {
            try {
                ifaaPlugin.processCmd(this.mCallback, param);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "processCmd failed: " + e);
            }
            try {
                synchronized (this.mSignal) {
                    Log.d(LOG_TAG, "IFAA processCmdV2 enter wait!!!");
                    if (!this.mNotified) {
                        this.mSignal.wait();
                    }
                    this.mNotified = false;
                    Log.d(LOG_TAG, "IFAA processCmdV2 enter waited ok!!!");
                }
            } catch (InterruptedException e2) {
                Log.d(LOG_TAG, "IFAA processCmdV2 interrupted!!!");
            }
            if (this.mRet == 0) {
                retByte = this.mRecData;
            }
        } else {
            Log.e(LOG_TAG, "IIFAAPlugin get failed!!!");
        }
        endProcessCmd();
        return retByte;
    }

    private boolean isOkProcessCmd() {
        synchronized (this.mStatusLock) {
            if (this.mStatus != 0) {
                return false;
            }
            this.mStatus = 1;
            synchronized (this.mSignal) {
                this.mNotified = false;
            }
            return true;
        }
    }

    private void endProcessCmd() {
        synchronized (this.mStatusLock) {
            this.mStatus = 0;
        }
    }

    /* access modifiers changed from: private */
    public void notifyResult(int ret, byte[] param) {
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
