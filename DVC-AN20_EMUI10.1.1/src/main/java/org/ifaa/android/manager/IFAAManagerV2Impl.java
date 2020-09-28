package org.ifaa.android.manager;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.hwpanpayservice.IHwIFAAService;
import com.huawei.hwpanpayservice.IHwIFAAServiceCallBack;

public class IFAAManagerV2Impl extends IFAAManagerV2 {
    private static final int IFAA_PLUGIN_ID = 3;
    private static final String LOG_TAG = "IFAAManagerImpl";
    private static final String SET_FP_CLASS = "com.android.settings.fingerprint.FingerprintSettingsActivity";
    private static final String SET_FP_PACKAGE = "com.android.settings";
    private static final int STATUS_BUSY = 1;
    private static final int STATUS_IDLE = 0;
    private final HwIFAAServiceCallBack mCallback = new HwIFAAServiceCallBack();
    private IHwIFAAService mHwIFAAService = null;
    private boolean mNotified;
    private byte[] mRecData;
    private int mRet;
    private final Object mSignal = new Object();
    private int mStatus = 0;
    private final Object mStatusLock = new Object();

    public IFAAManagerV2Impl(Context context) {
    }

    private IHwIFAAService getHwIFAAService(Context context) {
        ConnectRemoteServiceManager.initRemoteService(context);
        this.mHwIFAAService = ConnectRemoteServiceManager.getRemoteServiceInstance();
        if (this.mHwIFAAService != null) {
            Log.i(LOG_TAG, "getHwIFAAService successfully");
        } else {
            Log.e(LOG_TAG, "getHwIFAAService failed!!!!");
        }
        return this.mHwIFAAService;
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int getSupportBIOTypes(Context context) {
        Log.d(LOG_TAG, "getSupportBIOTypes");
        int type = 0;
        FingerprintManager mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
        if (mFingerprintManager != null && mFingerprintManager.isHardwareDetected()) {
            type = 0 | 1;
        }
        Log.d(LOG_TAG, "getSupportBIOTypes is " + type);
        return type;
    }

    @Override // org.ifaa.android.manager.IFAAManager
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

    @Override // org.ifaa.android.manager.IFAAManager
    public String getDeviceModel() {
        String deviceModel = SystemProperties.get("ro.product.fingerprintName");
        Log.d(LOG_TAG, "getDeviceModel: [" + deviceModel + "]");
        return deviceModel;
    }

    @Override // org.ifaa.android.manager.IFAAManager
    public int getVersion() {
        Log.d(LOG_TAG, "getVersion");
        return 2;
    }

    @Override // org.ifaa.android.manager.IFAAManagerV2
    public byte[] processCmdV2(Context context, byte[] param) {
        Log.d(LOG_TAG, "IFAA processCmdV2");
        byte[] retByte = new byte[0];
        if (!isOkProcessCmd()) {
            Log.e(LOG_TAG, "IFAA processCmdV2 failed because is busy!");
            return retByte;
        }
        IHwIFAAService hwIFAAService = getHwIFAAService(context);
        if (hwIFAAService != null) {
            try {
                hwIFAAService.processCmd(this.mCallback, param);
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
            Log.e(LOG_TAG, "IHwIFAAService get failed!!!");
        }
        endProcessCmd();
        return retByte;
    }

    private boolean isOkProcessCmd() {
        Log.d(LOG_TAG, "IFAA isOkProcessCmd enter !!!");
        synchronized (this.mStatusLock) {
            if (this.mStatus == 0) {
                this.mStatus = 1;
                synchronized (this.mSignal) {
                    this.mNotified = false;
                }
                Log.d(LOG_TAG, "IFAA isOkProcessCmd true !!!");
                return true;
            }
            Log.d(LOG_TAG, "IFAA isOkProcessCmd false !!!");
            return false;
        }
    }

    private void endProcessCmd() {
        Log.d(LOG_TAG, "IFAA endProcessCmd enter !!!");
        synchronized (this.mStatusLock) {
            this.mStatus = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyResult(int ret, byte[] param) {
        Log.d(LOG_TAG, "IFAA notifyResult enter !!!");
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

    static class HwIFAAServiceCallBack extends IHwIFAAServiceCallBack.Stub {
        private final IFAAManagerV2Impl mImpl;

        private HwIFAAServiceCallBack(IFAAManagerV2Impl impl) {
            this.mImpl = impl;
        }

        @Override // com.huawei.hwpanpayservice.IHwIFAAServiceCallBack
        public void processCmdResult(int ret, byte[] param) {
            if (this.mImpl != null) {
                Log.d(IFAAManagerV2Impl.LOG_TAG, "IFAA processCmdResult enter notify!!!");
                this.mImpl.notifyResult(ret, param);
            }
        }
    }
}
