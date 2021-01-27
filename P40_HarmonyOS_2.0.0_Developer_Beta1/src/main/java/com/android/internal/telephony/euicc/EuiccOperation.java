package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.euicc.DownloadableSubscription;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class EuiccOperation implements Parcelable {
    @VisibleForTesting
    @Deprecated
    static final int ACTION_DOWNLOAD_CONFIRMATION_CODE = 8;
    @VisibleForTesting
    static final int ACTION_DOWNLOAD_DEACTIVATE_SIM = 2;
    @VisibleForTesting
    static final int ACTION_DOWNLOAD_NO_PRIVILEGES = 3;
    @VisibleForTesting
    static final int ACTION_DOWNLOAD_NO_PRIVILEGES_OR_DEACTIVATE_SIM_CHECK_METADATA = 9;
    @VisibleForTesting
    static final int ACTION_DOWNLOAD_RESOLVABLE_ERRORS = 7;
    @VisibleForTesting
    static final int ACTION_GET_DEFAULT_LIST_DEACTIVATE_SIM = 4;
    @VisibleForTesting
    static final int ACTION_GET_METADATA_DEACTIVATE_SIM = 1;
    @VisibleForTesting
    static final int ACTION_SWITCH_DEACTIVATE_SIM = 5;
    @VisibleForTesting
    static final int ACTION_SWITCH_NO_PRIVILEGES = 6;
    public static final Parcelable.Creator<EuiccOperation> CREATOR = new Parcelable.Creator<EuiccOperation>() {
        /* class com.android.internal.telephony.euicc.EuiccOperation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EuiccOperation createFromParcel(Parcel in) {
            return new EuiccOperation(in);
        }

        @Override // android.os.Parcelable.Creator
        public EuiccOperation[] newArray(int size) {
            return new EuiccOperation[size];
        }
    };
    private static final String TAG = "EuiccOperation";
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public final int mAction;
    private final String mCallingPackage;
    private final long mCallingToken;
    private final DownloadableSubscription mDownloadableSubscription;
    private final int mResolvableErrors;
    private final int mSubscriptionId;
    private final boolean mSwitchAfterDownload;

    @VisibleForTesting
    @Retention(RetentionPolicy.SOURCE)
    @interface Action {
    }

    static EuiccOperation forGetMetadataDeactivateSim(long callingToken, DownloadableSubscription subscription, String callingPackage) {
        return new EuiccOperation(1, callingToken, subscription, 0, false, callingPackage);
    }

    static EuiccOperation forDownloadDeactivateSim(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        return new EuiccOperation(2, callingToken, subscription, 0, switchAfterDownload, callingPackage);
    }

    static EuiccOperation forDownloadNoPrivileges(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        return new EuiccOperation(3, callingToken, subscription, 0, switchAfterDownload, callingPackage);
    }

    static EuiccOperation forDownloadNoPrivilegesOrDeactivateSimCheckMetadata(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        return new EuiccOperation(9, callingToken, subscription, 0, switchAfterDownload, callingPackage);
    }

    @Deprecated
    public static EuiccOperation forDownloadConfirmationCode(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        return new EuiccOperation(8, callingToken, subscription, 0, switchAfterDownload, callingPackage);
    }

    static EuiccOperation forDownloadResolvableErrors(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, int resolvableErrors) {
        return new EuiccOperation(7, callingToken, subscription, 0, switchAfterDownload, callingPackage, resolvableErrors);
    }

    static EuiccOperation forGetDefaultListDeactivateSim(long callingToken, String callingPackage) {
        return new EuiccOperation(4, callingToken, null, 0, false, callingPackage);
    }

    static EuiccOperation forSwitchDeactivateSim(long callingToken, int subscriptionId, String callingPackage) {
        return new EuiccOperation(5, callingToken, null, subscriptionId, false, callingPackage);
    }

    static EuiccOperation forSwitchNoPrivileges(long callingToken, int subscriptionId, String callingPackage) {
        return new EuiccOperation(6, callingToken, null, subscriptionId, false, callingPackage);
    }

    EuiccOperation(int action, long callingToken, DownloadableSubscription downloadableSubscription, int subscriptionId, boolean switchAfterDownload, String callingPackage, int resolvableErrors) {
        this.mAction = action;
        this.mCallingToken = callingToken;
        this.mDownloadableSubscription = downloadableSubscription;
        this.mSubscriptionId = subscriptionId;
        this.mSwitchAfterDownload = switchAfterDownload;
        this.mCallingPackage = callingPackage;
        this.mResolvableErrors = resolvableErrors;
    }

    EuiccOperation(int action, long callingToken, DownloadableSubscription downloadableSubscription, int subscriptionId, boolean switchAfterDownload, String callingPackage) {
        this.mAction = action;
        this.mCallingToken = callingToken;
        this.mDownloadableSubscription = downloadableSubscription;
        this.mSubscriptionId = subscriptionId;
        this.mSwitchAfterDownload = switchAfterDownload;
        this.mCallingPackage = callingPackage;
        this.mResolvableErrors = 0;
    }

    EuiccOperation(Parcel in) {
        this.mAction = in.readInt();
        this.mCallingToken = in.readLong();
        this.mDownloadableSubscription = (DownloadableSubscription) in.readTypedObject(DownloadableSubscription.CREATOR);
        this.mSubscriptionId = in.readInt();
        this.mSwitchAfterDownload = in.readBoolean();
        this.mCallingPackage = in.readString();
        this.mResolvableErrors = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAction);
        dest.writeLong(this.mCallingToken);
        dest.writeTypedObject(this.mDownloadableSubscription, flags);
        dest.writeInt(this.mSubscriptionId);
        dest.writeBoolean(this.mSwitchAfterDownload);
        dest.writeString(this.mCallingPackage);
        dest.writeInt(this.mResolvableErrors);
    }

    public void continueOperation(int cardId, Bundle resolutionExtras, PendingIntent callbackIntent) {
        Binder.restoreCallingIdentity(this.mCallingToken);
        switch (this.mAction) {
            case 1:
                resolvedGetMetadataDeactivateSim(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 2:
                resolvedDownloadDeactivateSim(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 3:
                resolvedDownloadNoPrivileges(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 4:
                resolvedGetDefaultListDeactivateSim(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 5:
                resolvedSwitchDeactivateSim(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 6:
                resolvedSwitchNoPrivileges(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 7:
                resolvedDownloadResolvableErrors(cardId, resolutionExtras, callbackIntent);
                return;
            case 8:
                resolvedDownloadConfirmationCode(cardId, resolutionExtras.getString("android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE"), callbackIntent);
                return;
            case 9:
                resolvedDownloadNoPrivilegesOrDeactivateSimCheckMetadata(cardId, resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            default:
                Log.wtf(TAG, "Unknown action: " + this.mAction);
                return;
        }
    }

    private void resolvedGetMetadataDeactivateSim(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().getDownloadableSubscriptionMetadata(cardId, this.mDownloadableSubscription, true, this.mCallingPackage, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedDownloadDeactivateSim(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().downloadSubscription(cardId, this.mDownloadableSubscription, this.mSwitchAfterDownload, this.mCallingPackage, true, null, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedDownloadNoPrivileges(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccController.get().downloadSubscriptionPrivileged(cardId, token, this.mDownloadableSubscription, this.mSwitchAfterDownload, true, this.mCallingPackage, null, callbackIntent);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedDownloadNoPrivilegesOrDeactivateSimCheckMetadata(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccController.get().downloadSubscriptionPrivilegedCheckMetadata(cardId, token, this.mDownloadableSubscription, this.mSwitchAfterDownload, true, this.mCallingPackage, null, callbackIntent);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            fail(callbackIntent);
        }
    }

    @Deprecated
    private void resolvedDownloadConfirmationCode(int cardId, String confirmationCode, PendingIntent callbackIntent) {
        if (TextUtils.isEmpty(confirmationCode)) {
            fail(callbackIntent);
            return;
        }
        this.mDownloadableSubscription.setConfirmationCode(confirmationCode);
        EuiccController.get().downloadSubscription(cardId, this.mDownloadableSubscription, this.mSwitchAfterDownload, this.mCallingPackage, true, null, callbackIntent);
    }

    private void resolvedDownloadResolvableErrors(int cardId, Bundle resolvedBundle, PendingIntent callbackIntent) {
        boolean pass = true;
        String confirmationCode = null;
        if ((this.mResolvableErrors & 2) != 0 && !resolvedBundle.getBoolean("android.service.euicc.extra.RESOLUTION_ALLOW_POLICY_RULES")) {
            pass = false;
        }
        if ((this.mResolvableErrors & 1) != 0) {
            confirmationCode = resolvedBundle.getString("android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE");
            if (TextUtils.isEmpty(confirmationCode)) {
                pass = false;
            }
        }
        if (!pass) {
            fail(callbackIntent);
            return;
        }
        this.mDownloadableSubscription.setConfirmationCode(confirmationCode);
        EuiccController.get().downloadSubscription(cardId, this.mDownloadableSubscription, this.mSwitchAfterDownload, this.mCallingPackage, true, resolvedBundle, callbackIntent);
    }

    private void resolvedGetDefaultListDeactivateSim(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().getDefaultDownloadableSubscriptionList(cardId, true, this.mCallingPackage, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedSwitchDeactivateSim(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().switchToSubscription(cardId, this.mSubscriptionId, true, this.mCallingPackage, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedSwitchNoPrivileges(int cardId, boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccController.get().switchToSubscriptionPrivileged(cardId, token, this.mSubscriptionId, true, this.mCallingPackage, callbackIntent);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            fail(callbackIntent);
        }
    }

    private static void fail(PendingIntent callbackIntent) {
        EuiccController.get().lambda$refreshSubscriptionsAndSendResult$0$EuiccController(callbackIntent, 2, null);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DownloadableSubscription getDownloadableSubscription() {
        return this.mDownloadableSubscription;
    }
}
