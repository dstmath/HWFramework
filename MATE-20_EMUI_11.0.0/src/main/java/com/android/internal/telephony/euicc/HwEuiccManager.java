package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import com.android.internal.telephony.euicc.IEuiccController;
import com.android.internal.telephony.euicc.IHwEuiccManager;

public class HwEuiccManager {
    private static final String TAG = "HwEuiccManager";

    private static IEuiccController getIEuiccControllerOrThrow() {
        IEuiccController euiccController = IEuiccController.Stub.asInterface(ServiceManager.getService("econtroller"));
        if (euiccController != null) {
            return euiccController;
        }
        throw new UnsupportedOperationException("Euicc is not supported");
    }

    public static IHwEuiccManager getService() {
        try {
            return IHwEuiccManager.Stub.asInterface(getIEuiccControllerOrThrow().getHwInnerService());
        } catch (RemoteException e) {
            Rlog.e(TAG, "RemoteException error");
            return null;
        } catch (RuntimeException e2) {
            Rlog.e(TAG, "RuntimeException error");
            return null;
        }
    }

    private static boolean isEnabled() {
        return getIEuiccController() != null;
    }

    private static IEuiccController getIEuiccController() {
        return IEuiccController.Stub.asInterface(ServiceManager.getService("econtroller"));
    }

    private static void sendUnavailableError(PendingIntent callbackIntent) {
        if (callbackIntent != null) {
            try {
                callbackIntent.send(2);
            } catch (PendingIntent.CanceledException e) {
                Rlog.e(TAG, "CanceledException error");
            }
        }
    }

    public static void requestDefaultSmdpAddress(String cardId, PendingIntent callbackIntent) {
        IHwEuiccManager service = getService();
        if (service == null || !isEnabled() || TextUtils.isEmpty(cardId)) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            service.requestDefaultSmdpAddress(cardId, callbackIntent);
        } catch (RemoteException e) {
            Rlog.e(TAG, "requestDefaultSmdpAddress failed: catch RemoteException!");
        }
    }

    public static void resetMemory(String cardId, int options, PendingIntent callbackIntent) {
        IHwEuiccManager service = getService();
        if (service == null || !isEnabled() || TextUtils.isEmpty(cardId)) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            service.resetMemory(cardId, options, callbackIntent);
        } catch (RemoteException e) {
            Rlog.e(TAG, "resetMemory failed: catch RemoteException!");
        }
    }

    public static void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, PendingIntent callbackIntent) {
        IHwEuiccManager service = getService();
        if (service == null || !isEnabled() || TextUtils.isEmpty(cardId) || TextUtils.isEmpty(defaultSmdpAddress)) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            service.setDefaultSmdpAddress(cardId, defaultSmdpAddress, callbackIntent);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setDefaultSmdpAddress failed: catch RemoteException!");
        }
    }

    public static void cancelSession() {
        IHwEuiccManager service = getService();
        if (service != null && isEnabled()) {
            try {
                service.cancelSession();
            } catch (RemoteException e) {
                Rlog.e(TAG, "cancelSession failed: catch RemoteException!");
            }
        }
    }

    public static void updateSubscriptionNickname(EuiccManager euiccManager, int subscriptionId, String nickname, PendingIntent callbackIntent) {
        if (euiccManager != null) {
            euiccManager.updateSubscriptionNickname(subscriptionId, nickname, callbackIntent);
        } else {
            sendUnavailableError(callbackIntent);
        }
    }

    public static void continueOperation(EuiccManager euiccManager, Intent resolutionIntent, Bundle resolutionExtras) {
        if (euiccManager != null && resolutionIntent != null) {
            euiccManager.continueOperation(resolutionIntent, resolutionExtras);
        }
    }

    public static void getDownloadableSubscriptionMetadata(EuiccManager euiccManager, DownloadableSubscription subscription, PendingIntent callbackIntent) {
        if (euiccManager != null) {
            euiccManager.getDownloadableSubscriptionMetadata(subscription, callbackIntent);
        }
    }

    public static void getDefaultDownloadableSubscriptionList(EuiccManager euiccManager, PendingIntent callbackIntent) {
        if (euiccManager != null) {
            euiccManager.getDefaultDownloadableSubscriptionList(callbackIntent);
        }
    }

    public static int getOtaStatus(EuiccManager euiccManager) {
        if (euiccManager != null) {
            return euiccManager.getOtaStatus();
        }
        return 5;
    }

    public static void startOtaUpdating(int cardId, int otaReason) {
        IHwEuiccManager service = getService();
        if (service != null && isEnabled()) {
            try {
                service.startOtaUpdating(cardId, otaReason);
            } catch (RemoteException e) {
                Rlog.e(TAG, "startOtaUpdating failed: catch RemoteException!");
            }
        }
    }
}
