package android.service.euicc;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.euicc.DownloadableSubscription;
import java.util.Arrays;
import java.util.List;

@SystemApi
public final class GetDefaultDownloadableSubscriptionListResult implements Parcelable {
    public static final Parcelable.Creator<GetDefaultDownloadableSubscriptionListResult> CREATOR = new Parcelable.Creator<GetDefaultDownloadableSubscriptionListResult>() {
        /* class android.service.euicc.GetDefaultDownloadableSubscriptionListResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GetDefaultDownloadableSubscriptionListResult createFromParcel(Parcel in) {
            return new GetDefaultDownloadableSubscriptionListResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public GetDefaultDownloadableSubscriptionListResult[] newArray(int size) {
            return new GetDefaultDownloadableSubscriptionListResult[size];
        }
    };
    private final DownloadableSubscription[] mSubscriptions;
    @UnsupportedAppUsage
    @Deprecated
    public final int result;

    public int getResult() {
        return this.result;
    }

    public List<DownloadableSubscription> getDownloadableSubscriptions() {
        DownloadableSubscription[] downloadableSubscriptionArr = this.mSubscriptions;
        if (downloadableSubscriptionArr == null) {
            return null;
        }
        return Arrays.asList(downloadableSubscriptionArr);
    }

    public GetDefaultDownloadableSubscriptionListResult(int result2, DownloadableSubscription[] subscriptions) {
        this.result = result2;
        if (this.result == 0) {
            this.mSubscriptions = subscriptions;
        } else if (subscriptions == null) {
            this.mSubscriptions = null;
        } else {
            throw new IllegalArgumentException("Error result with non-null subscriptions: " + result2);
        }
    }

    private GetDefaultDownloadableSubscriptionListResult(Parcel in) {
        this.result = in.readInt();
        this.mSubscriptions = (DownloadableSubscription[]) in.createTypedArray(DownloadableSubscription.CREATOR);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeTypedArray(this.mSubscriptions, flags);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
