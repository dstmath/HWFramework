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
    static final int ACTION_DOWNLOAD_CONFIRMATION_CODE = 7;
    @VisibleForTesting
    static final int ACTION_DOWNLOAD_DEACTIVATE_SIM = 2;
    @VisibleForTesting
    static final int ACTION_DOWNLOAD_NO_PRIVILEGES = 3;
    @VisibleForTesting
    static final int ACTION_GET_DEFAULT_LIST_DEACTIVATE_SIM = 4;
    @VisibleForTesting
    static final int ACTION_GET_METADATA_DEACTIVATE_SIM = 1;
    @VisibleForTesting
    static final int ACTION_SWITCH_DEACTIVATE_SIM = 5;
    @VisibleForTesting
    static final int ACTION_SWITCH_NO_PRIVILEGES = 6;
    public static final Parcelable.Creator<EuiccOperation> CREATOR = new Parcelable.Creator<EuiccOperation>() {
        public EuiccOperation createFromParcel(Parcel in) {
            return new EuiccOperation(in);
        }

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
    private final int mSubscriptionId;
    private final boolean mSwitchAfterDownload;

    @VisibleForTesting
    @Retention(RetentionPolicy.SOURCE)
    @interface Action {
    }

    public static EuiccOperation forGetMetadataDeactivateSim(long callingToken, DownloadableSubscription subscription, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(1, callingToken, subscription, 0, false, callingPackage);
        return euiccOperation;
    }

    public static EuiccOperation forDownloadDeactivateSim(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(2, callingToken, subscription, 0, switchAfterDownload, callingPackage);
        return euiccOperation;
    }

    public static EuiccOperation forDownloadNoPrivileges(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(3, callingToken, subscription, 0, switchAfterDownload, callingPackage);
        return euiccOperation;
    }

    public static EuiccOperation forDownloadConfirmationCode(long callingToken, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(7, callingToken, subscription, 0, switchAfterDownload, callingPackage);
        return euiccOperation;
    }

    static EuiccOperation forGetDefaultListDeactivateSim(long callingToken, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(4, callingToken, null, 0, false, callingPackage);
        return euiccOperation;
    }

    static EuiccOperation forSwitchDeactivateSim(long callingToken, int subscriptionId, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(5, callingToken, null, subscriptionId, false, callingPackage);
        return euiccOperation;
    }

    static EuiccOperation forSwitchNoPrivileges(long callingToken, int subscriptionId, String callingPackage) {
        EuiccOperation euiccOperation = new EuiccOperation(6, callingToken, null, subscriptionId, false, callingPackage);
        return euiccOperation;
    }

    EuiccOperation(int action, long callingToken, DownloadableSubscription downloadableSubscription, int subscriptionId, boolean switchAfterDownload, String callingPackage) {
        this.mAction = action;
        this.mCallingToken = callingToken;
        this.mDownloadableSubscription = downloadableSubscription;
        this.mSubscriptionId = subscriptionId;
        this.mSwitchAfterDownload = switchAfterDownload;
        this.mCallingPackage = callingPackage;
    }

    EuiccOperation(Parcel in) {
        this.mAction = in.readInt();
        this.mCallingToken = in.readLong();
        this.mDownloadableSubscription = (DownloadableSubscription) in.readTypedObject(DownloadableSubscription.CREATOR);
        this.mSubscriptionId = in.readInt();
        this.mSwitchAfterDownload = in.readBoolean();
        this.mCallingPackage = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAction);
        dest.writeLong(this.mCallingToken);
        dest.writeTypedObject(this.mDownloadableSubscription, flags);
        dest.writeInt(this.mSubscriptionId);
        dest.writeBoolean(this.mSwitchAfterDownload);
        dest.writeString(this.mCallingPackage);
    }

    public void continueOperation(Bundle resolutionExtras, PendingIntent callbackIntent) {
        Binder.restoreCallingIdentity(this.mCallingToken);
        switch (this.mAction) {
            case 1:
                resolvedGetMetadataDeactivateSim(resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 2:
                resolvedDownloadDeactivateSim(resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 3:
                resolvedDownloadNoPrivileges(resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 4:
                resolvedGetDefaultListDeactivateSim(resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 5:
                resolvedSwitchDeactivateSim(resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 6:
                resolvedSwitchNoPrivileges(resolutionExtras.getBoolean("android.service.euicc.extra.RESOLUTION_CONSENT"), callbackIntent);
                return;
            case 7:
                resolvedDownloadConfirmationCode(resolutionExtras.getString("android.service.euicc.extra.RESOLUTION_CONFIRMATION_CODE"), callbackIntent);
                return;
            default:
                Log.wtf(TAG, "Unknown action: " + this.mAction);
                return;
        }
    }

    private void resolvedGetMetadataDeactivateSim(boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().getDownloadableSubscriptionMetadata(this.mDownloadableSubscription, true, this.mCallingPackage, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedDownloadDeactivateSim(boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().downloadSubscription(this.mDownloadableSubscription, this.mSwitchAfterDownload, this.mCallingPackage, true, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedDownloadNoPrivileges(boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccController.get().downloadSubscriptionPrivileged(token, this.mDownloadableSubscription, this.mSwitchAfterDownload, true, this.mCallingPackage, callbackIntent);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedDownloadConfirmationCode(String confirmationCode, PendingIntent callbackIntent) {
        if (TextUtils.isEmpty(confirmationCode)) {
            fail(callbackIntent);
            return;
        }
        this.mDownloadableSubscription.setConfirmationCode(confirmationCode);
        EuiccController.get().downloadSubscription(this.mDownloadableSubscription, this.mSwitchAfterDownload, this.mCallingPackage, true, callbackIntent);
    }

    private void resolvedGetDefaultListDeactivateSim(boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().getDefaultDownloadableSubscriptionList(true, this.mCallingPackage, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedSwitchDeactivateSim(boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            EuiccController.get().switchToSubscription(this.mSubscriptionId, true, this.mCallingPackage, callbackIntent);
        } else {
            fail(callbackIntent);
        }
    }

    private void resolvedSwitchNoPrivileges(boolean consent, PendingIntent callbackIntent) {
        if (consent) {
            long token = Binder.clearCallingIdentity();
            try {
                EuiccController.get().switchToSubscriptionPrivileged(token, this.mSubscriptionId, true, this.mCallingPackage, callbackIntent);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            fail(callbackIntent);
        }
    }

    private static void fail(PendingIntent callbackIntent) {
        EuiccController.get().sendResult(callbackIntent, 2, null);
    }

    public int describeContents() {
        return 0;
    }
}
