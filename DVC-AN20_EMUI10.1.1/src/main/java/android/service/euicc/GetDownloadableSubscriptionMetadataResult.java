package android.service.euicc;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.euicc.DownloadableSubscription;

@SystemApi
public final class GetDownloadableSubscriptionMetadataResult implements Parcelable {
    public static final Parcelable.Creator<GetDownloadableSubscriptionMetadataResult> CREATOR = new Parcelable.Creator<GetDownloadableSubscriptionMetadataResult>() {
        /* class android.service.euicc.GetDownloadableSubscriptionMetadataResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GetDownloadableSubscriptionMetadataResult createFromParcel(Parcel in) {
            return new GetDownloadableSubscriptionMetadataResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public GetDownloadableSubscriptionMetadataResult[] newArray(int size) {
            return new GetDownloadableSubscriptionMetadataResult[size];
        }
    };
    private boolean mPprFlag;
    private int mPprType;
    private int mResolvableErrors;
    private final DownloadableSubscription mSubscription;
    @UnsupportedAppUsage
    @Deprecated
    public final int result;

    public GetDownloadableSubscriptionMetadataResult(int result2, DownloadableSubscription subscription, int resolvableErrors, int pprType, boolean pprFlag) {
        this.mResolvableErrors = 0;
        this.mPprType = 0;
        this.mPprFlag = false;
        this.result = result2;
        int i = this.result;
        if (i == 0) {
            this.mSubscription = subscription;
        } else if (i == -2) {
            this.mSubscription = subscription;
            this.mResolvableErrors = resolvableErrors;
            this.mPprType = pprType;
            this.mPprFlag = pprFlag;
        } else if (subscription == null) {
            this.mSubscription = null;
        } else {
            throw new IllegalArgumentException("Error result with non-null subscription: " + result2);
        }
    }

    public int getResult() {
        return this.result;
    }

    public DownloadableSubscription getDownloadableSubscription() {
        return this.mSubscription;
    }

    public GetDownloadableSubscriptionMetadataResult(int result2, DownloadableSubscription subscription) {
        this.mResolvableErrors = 0;
        this.mPprType = 0;
        this.mPprFlag = false;
        this.result = result2;
        if (this.result == 0) {
            this.mSubscription = subscription;
        } else if (subscription == null) {
            this.mSubscription = null;
        } else {
            throw new IllegalArgumentException("Error result with non-null subscription: " + result2);
        }
    }

    private GetDownloadableSubscriptionMetadataResult(Parcel in) {
        this.mResolvableErrors = 0;
        this.mPprType = 0;
        this.mPprFlag = false;
        this.result = in.readInt();
        this.mSubscription = (DownloadableSubscription) in.readTypedObject(DownloadableSubscription.CREATOR);
        this.mPprType = in.readInt();
        this.mPprFlag = in.readBoolean();
        this.mResolvableErrors = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeTypedObject(this.mSubscription, flags);
        dest.writeInt(this.mPprType);
        dest.writeBoolean(this.mPprFlag);
        dest.writeInt(this.mResolvableErrors);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getResolvableErrors() {
        return this.mResolvableErrors;
    }

    public int getPprType() {
        return this.mPprType;
    }

    public boolean getPprFlag() {
        return this.mPprFlag;
    }
}
