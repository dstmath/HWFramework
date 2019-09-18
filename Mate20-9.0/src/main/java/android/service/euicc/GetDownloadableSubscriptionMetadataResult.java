package android.service.euicc;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.euicc.DownloadableSubscription;

@SystemApi
public final class GetDownloadableSubscriptionMetadataResult implements Parcelable {
    public static final Parcelable.Creator<GetDownloadableSubscriptionMetadataResult> CREATOR = new Parcelable.Creator<GetDownloadableSubscriptionMetadataResult>() {
        public GetDownloadableSubscriptionMetadataResult createFromParcel(Parcel in) {
            return new GetDownloadableSubscriptionMetadataResult(in);
        }

        public GetDownloadableSubscriptionMetadataResult[] newArray(int size) {
            return new GetDownloadableSubscriptionMetadataResult[size];
        }
    };
    private final DownloadableSubscription mSubscription;
    @Deprecated
    public final int result;

    public int getResult() {
        return this.result;
    }

    public DownloadableSubscription getDownloadableSubscription() {
        return this.mSubscription;
    }

    public GetDownloadableSubscriptionMetadataResult(int result2, DownloadableSubscription subscription) {
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
        this.result = in.readInt();
        this.mSubscription = (DownloadableSubscription) in.readTypedObject(DownloadableSubscription.CREATOR);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeTypedObject(this.mSubscription, flags);
    }

    public int describeContents() {
        return 0;
    }
}
