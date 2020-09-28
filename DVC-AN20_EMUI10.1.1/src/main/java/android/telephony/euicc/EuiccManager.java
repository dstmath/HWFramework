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
import android.telephony.TelephonyManager;
import com.android.internal.telephony.euicc.IEuiccController;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EuiccManager {
    @SystemApi
    public static final String ACTION_DELETE_SUBSCRIPTION_PRIVILEGED = "android.telephony.euicc.action.DELETE_SUBSCRIPTION_PRIVILEGED";
    public static final String ACTION_MANAGE_EMBEDDED_SUBSCRIPTIONS = "android.telephony.euicc.action.MANAGE_EMBEDDED_SUBSCRIPTIONS";
    public static final String ACTION_NOTIFY_CARRIER_SETUP_INCOMPLETE = "android.telephony.euicc.action.NOTIFY_CARRIER_SETUP_INCOMPLETE";
    @SystemApi
    public static final String ACTION_OTA_STATUS_CHANGED = "android.telephony.euicc.action.OTA_STATUS_CHANGED";
    @SystemApi
    public static final String ACTION_PROVISION_EMBEDDED_SUBSCRIPTION = "android.telephony.euicc.action.PROVISION_EMBEDDED_SUBSCRIPTION";
    @SystemApi
    public static final String ACTION_RENAME_SUBSCRIPTION_PRIVILEGED = "android.telephony.euicc.action.RENAME_SUBSCRIPTION_PRIVILEGED";
    public static final String ACTION_RESOLVE_ERROR = "android.telephony.euicc.action.RESOLVE_ERROR";
    @SystemApi
    public static final String ACTION_TOGGLE_SUBSCRIPTION_PRIVILEGED = "android.telephony.euicc.action.TOGGLE_SUBSCRIPTION_PRIVILEGED";
    public static final int EMBEDDED_SUBSCRIPTION_RESULT_ERROR = 2;
    public static final int EMBEDDED_SUBSCRIPTION_RESULT_OK = 0;
    public static final int EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR = 1;
    @SystemApi
    public static final int EUICC_ACTIVATION_TYPE_ACCOUNT_REQUIRED = 4;
    @SystemApi
    public static final int EUICC_ACTIVATION_TYPE_BACKUP = 2;
    @SystemApi
    public static final int EUICC_ACTIVATION_TYPE_DEFAULT = 1;
    @SystemApi
    public static final int EUICC_ACTIVATION_TYPE_TRANSFER = 3;
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
    @SystemApi
    public static final String EXTRA_ACTIVATION_TYPE = "android.telephony.euicc.extra.ACTIVATION_TYPE";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DETAILED_CODE";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTION";
    @SystemApi
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTIONS = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_DOWNLOADABLE_SUBSCRIPTIONS";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_ACTION = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_ACTION";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_CALLBACK_INTENT";
    public static final String EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT = "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT";
    @SystemApi
    public static final String EXTRA_ENABLE_SUBSCRIPTION = "android.telephony.euicc.extra.ENABLE_SUBSCRIPTION";
    @SystemApi
    public static final String EXTRA_FORCE_PROVISION = "android.telephony.euicc.extra.FORCE_PROVISION";
    @SystemApi
    public static final String EXTRA_FROM_SUBSCRIPTION_ID = "android.telephony.euicc.extra.FROM_SUBSCRIPTION_ID";
    public static final String EXTRA_PHYSICAL_SLOT_ID = "android.telephony.euicc.extra.PHYSICAL_SLOT_ID";
    @SystemApi
    public static final String EXTRA_SUBSCRIPTION_ID = "android.telephony.euicc.extra.SUBSCRIPTION_ID";
    @SystemApi
    public static final String EXTRA_SUBSCRIPTION_NICKNAME = "android.telephony.euicc.extra.SUBSCRIPTION_NICKNAME";
    public static final String META_DATA_CARRIER_ICON = "android.telephony.euicc.carriericon";
    private int mCardId;
    private final Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface EuiccActivationType {
    }

    @SystemApi
    @Retention(RetentionPolicy.SOURCE)
    public @interface OtaStatus {
    }

    public EuiccManager(Context context) {
        this.mContext = context;
        this.mCardId = ((TelephonyManager) context.getSystemService("phone")).getCardIdForDefaultEuicc();
    }

    private EuiccManager(Context context, int cardId) {
        this.mContext = context;
        this.mCardId = cardId;
    }

    public EuiccManager createForCardId(int cardId) {
        return new EuiccManager(this.mContext, cardId);
    }

    public boolean isEnabled() {
        return getIEuiccController() != null && refreshCardIdIfUninitialized();
    }

    public String getEid() {
        if (!isEnabled()) {
            return null;
        }
        try {
            return getIEuiccController().getEid(this.mCardId, this.mContext.getOpPackageName());
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
            return getIEuiccController().getOtaStatus(this.mCardId);
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
            getIEuiccController().downloadSubscription(this.mCardId, subscription, switchAfterDownload, this.mContext.getOpPackageName(), null, callbackIntent);
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
                return;
            }
            return;
        }
        try {
            getIEuiccController().continueOperation(this.mCardId, resolutionIntent, resolutionExtras);
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
            getIEuiccController().getDownloadableSubscriptionMetadata(this.mCardId, subscription, this.mContext.getOpPackageName(), callbackIntent);
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
            getIEuiccController().getDefaultDownloadableSubscriptionList(this.mCardId, this.mContext.getOpPackageName(), callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public EuiccInfo getEuiccInfo() {
        if (!isEnabled()) {
            return null;
        }
        try {
            return getIEuiccController().getEuiccInfo(this.mCardId);
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
            getIEuiccController().deleteSubscription(this.mCardId, subscriptionId, this.mContext.getOpPackageName(), callbackIntent);
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
            getIEuiccController().switchToSubscription(this.mCardId, subscriptionId, this.mContext.getOpPackageName(), callbackIntent);
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
            getIEuiccController().updateSubscriptionNickname(this.mCardId, subscriptionId, nickname, this.mContext.getOpPackageName(), callbackIntent);
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
            getIEuiccController().eraseSubscriptions(this.mCardId, callbackIntent);
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
            getIEuiccController().retainSubscriptionsForFactoryReset(this.mCardId, callbackIntent);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean refreshCardIdIfUninitialized() {
        if (this.mCardId == -2) {
            this.mCardId = ((TelephonyManager) this.mContext.getSystemService("phone")).getCardIdForDefaultEuicc();
        }
        if (this.mCardId == -2) {
            return false;
        }
        return true;
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
