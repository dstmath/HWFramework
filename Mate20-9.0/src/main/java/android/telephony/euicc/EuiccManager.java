package android.telephony.euicc;

import android.annotation.SystemApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.telephony.euicc.IEuiccController;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EuiccManager {
    public static final String ACTION_MANAGE_EMBEDDED_SUBSCRIPTIONS = "android.telephony.euicc.action.MANAGE_EMBEDDED_SUBSCRIPTIONS";
    public static final String ACTION_NOTIFY_CARRIER_SETUP_INCOMPLETE = "android.telephony.euicc.action.NOTIFY_CARRIER_SETUP_INCOMPLETE";
    @SystemApi
    public static final String ACTION_OTA_STATUS_CHANGED = "android.telephony.euicc.action.OTA_STATUS_CHANGED";
    @SystemApi
    public static final String ACTION_PROVISION_EMBEDDED_SUBSCRIPTION = "android.telephony.euicc.action.PROVISION_EMBEDDED_SUBSCRIPTION";
    public static final String ACTION_RESOLVE_ERROR = "android.telephony.euicc.action.RESOLVE_ERROR";
    public static final int EMBEDDED_SUBSCRIPTION_RESULT_ERROR = 2;
    public static final int EMBEDDED_SUBSCRIPTION_RESULT_OK = 0;
    public static final int EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR = 1;
    @SystemApi
    public static final int EUICC_OTA_FAILED = 2;
    @SystemApi
    public static final int EUICC_OTA_IN_PROGRESS = 1;
    @SystemApi
    public static final int EUICC_OTA_NOT_NEEDED = 4;
    @SystemApi
    public static final int EUICC_OTA_STATUS_UNAVAILABLE = 5;
    @SystemApi
    public static final int EUICC_OTA_SUCCEEDED = 3;
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION";
    @SystemApi
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTIONS = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTIONS";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_ACTION = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_ACTION";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT";
    public static final String EXTRA_FORCE_PROVISION = "android.telephony.euicc.extra.FORCE_PROVISION";
    public static final String META_DATA_CARRIER_ICON = "android.telephony.euicc.carriericon";
    private final Context mContext;

    @SystemApi
    @Retention(RetentionPolicy.SOURCE)
    public @interface OtaStatus {
    }

    public EuiccManager(Context context) {
        this.mContext = context;
    }

    public boolean isEnabled() {
        return getIEuiccController() != null;
    }

    public String getEid() {
        if (!isEnabled()) {
            return null;
        }
        try {
            return getIEuiccController().getEid();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getOtaStatus() {
        if (!isEnabled()) {
            return 5;
        }
        try {
            return getIEuiccController().getOtaStatus();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void downloadSubscription(DownloadableSubscription subscription, boolean switchAfterDownload, PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().downloadSubscription(subscription, switchAfterDownload, this.mContext.getOpPackageName(), callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startResolutionActivity(Activity activity, int requestCode, Intent resultIntent, PendingIntent callbackIntent) throws IntentSender.SendIntentException {
        PendingIntent resolutionIntent = (PendingIntent) resultIntent.getParcelableExtra(EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT);
        if (resolutionIntent != null) {
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT, callbackIntent);
            activity.startIntentSenderForResult(resolutionIntent.getIntentSender(), requestCode, fillInIntent, 0, 0, 0);
            return;
        }
        throw new IllegalArgumentException("Invalid result intent");
    }

    @SystemApi
    public void continueOperation(Intent resolutionIntent, Bundle resolutionExtras) {
        if (!isEnabled()) {
            PendingIntent callbackIntent = (PendingIntent) resolutionIntent.getParcelableExtra(EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT);
            if (callbackIntent != null) {
                sendUnavailableError(callbackIntent);
            }
            return;
        }
        try {
            getIEuiccController().continueOperation(resolutionIntent, resolutionExtras);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void getDownloadableSubscriptionMetadata(DownloadableSubscription subscription, PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().getDownloadableSubscriptionMetadata(subscription, this.mContext.getOpPackageName(), callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void getDefaultDownloadableSubscriptionList(PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().getDefaultDownloadableSubscriptionList(this.mContext.getOpPackageName(), callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public EuiccInfo getEuiccInfo() {
        if (!isEnabled()) {
            return null;
        }
        try {
            return getIEuiccController().getEuiccInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteSubscription(int subscriptionId, PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().deleteSubscription(subscriptionId, this.mContext.getOpPackageName(), callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void switchToSubscription(int subscriptionId, PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().switchToSubscription(subscriptionId, this.mContext.getOpPackageName(), callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateSubscriptionNickname(int subscriptionId, String nickname, PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().updateSubscriptionNickname(subscriptionId, nickname, callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void eraseSubscriptions(PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().eraseSubscriptions(callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void retainSubscriptionsForFactoryReset(PendingIntent callbackIntent) {
        if (!isEnabled()) {
            sendUnavailableError(callbackIntent);
            return;
        }
        try {
            getIEuiccController().retainSubscriptionsForFactoryReset(callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static void sendUnavailableError(PendingIntent callbackIntent) {
        try {
            callbackIntent.send(2);
        } catch (PendingIntent.CanceledException e) {
        }
    }

    private static IEuiccController getIEuiccController() {
        return IEuiccController.Stub.asInterface(ServiceManager.getService("econtroller"));
    }
}
