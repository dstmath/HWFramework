package com.huawei.server.security.antimal;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.securitycenter.IHwSecService;

public class AntiMalDataPipeline {
    private static final Object LOCK = new Object();
    private static final String SERVICE_NAME = "com.huawei.securitycenter.mainservice.HwSecService";
    private static final String TAG = AntiMalDataPipeline.class.getSimpleName();
    private static volatile AntiMalDataPipeline sAntiMalDataPipeline;
    private IHwSecService mHwSecService;

    private AntiMalDataPipeline() {
    }

    public static AntiMalDataPipeline getInstance() {
        if (sAntiMalDataPipeline == null) {
            synchronized (LOCK) {
                if (sAntiMalDataPipeline == null) {
                    sAntiMalDataPipeline = new AntiMalDataPipeline();
                }
            }
        }
        return sAntiMalDataPipeline;
    }

    @Nullable
    private IHwSecService getHwSecService() {
        IHwSecService iHwSecService = this.mHwSecService;
        if (iHwSecService != null) {
            return iHwSecService;
        }
        try {
            IBinder binder = ServiceManagerEx.getService(SERVICE_NAME);
            if (binder != null) {
                this.mHwSecService = IHwSecService.Stub.asInterface(binder);
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.huawei.server.security.antimal.AntiMalDataPipeline.AnonymousClass1 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        Log.e(AntiMalDataPipeline.TAG, "HwSecService client died.");
                        AntiMalDataPipeline.this.mHwSecService = null;
                    }
                }, 0);
            } else {
                Log.e(TAG, "getHwSecService: error, bind is null.");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getHwSecService occurs RemoteException");
        } catch (Exception e2) {
            Log.e(TAG, "getHwSecService occurs Exception");
        } catch (Error e3) {
            Log.e(TAG, "getHwSecService occurs Error");
        }
        return this.mHwSecService;
    }

    @Nullable
    public Bundle transferMalInformation(String module, Bundle bundle) {
        try {
            if (getHwSecService() != null) {
                return this.mHwSecService.call(module, bundle);
            }
            Log.e(TAG, "transferMalInformation: mHwSecService is null.");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "transferMalInformation occurs RemoteException.");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "transferMalInformation occurs Exception.");
            return null;
        }
    }
}
