package com.huawei.android.gameassist;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;
import android.util.Slog;
import com.huawei.android.gameassist.IHiNetworkManager;
import java.util.Arrays;

public class HwHiNetworkManager {
    private static final Singleton<IHiNetworkManager> IHwHiNetworkManagerSingleton = new Singleton<IHiNetworkManager>() {
        /* class com.huawei.android.gameassist.HwHiNetworkManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHiNetworkManager create() {
            Slog.e(HwHiNetworkManager.TAG, Arrays.toString(ServiceManager.listServices()));
            if (ServiceManager.checkService("hinetwork") != null) {
                return IHiNetworkManager.Stub.asInterface(ServiceManager.getService("hinetwork"));
            }
            Slog.e(HwHiNetworkManager.TAG, "can not find  hinetwork service");
            return null;
        }
    };
    private static final String TAG = "HwHiNetworkManager";

    public static IHiNetworkManager getService() {
        Slog.e(TAG, "enter getService.");
        return IHwHiNetworkManagerSingleton.get();
    }

    public static int onOpenAccelerateResult(String acceletrateResult) {
        IHiNetworkManager hiNetworkService = getService();
        if (hiNetworkService != null) {
            try {
                Slog.e(TAG, "onOpenAccelerateResult.");
                return hiNetworkService.onOpenAccelerateResult(acceletrateResult);
            } catch (RemoteException e) {
                Slog.e(TAG, "onOpenAccelerateResult failed: catch RemoteException!");
                return -1;
            }
        } else {
            Slog.e(TAG, "IHiNetworkManager is null.");
            return -1;
        }
    }

    public static int onDetectTimeDelayResult(String timeDelayResult) {
        IHiNetworkManager hiNetworkService = getService();
        if (hiNetworkService != null) {
            try {
                Slog.e(TAG, "onOpenAccelerateResult.");
                return hiNetworkService.onDetectTimeDelayResult(timeDelayResult);
            } catch (RemoteException e) {
                Slog.e(TAG, "DetectTimeDelayResult failed: catch RemoteException!");
                return -1;
            }
        } else {
            Slog.e(TAG, "IHiNetworkManager is null.");
            return -1;
        }
    }
}
