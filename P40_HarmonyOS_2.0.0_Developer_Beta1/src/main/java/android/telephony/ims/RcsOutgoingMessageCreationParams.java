package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.ims.RcsMessageCreationParams;

public final class RcsOutgoingMessageCreationParams extends RcsMessageCreationParams implements Parcelable {
    public static final Parcelable.Creator<RcsOutgoingMessageCreationParams> CREATOR = new Parcelable.Creator<RcsOutgoingMessageCreationParams>() {
        /* class android.telephony.ims.RcsOutgoingMessageCreationParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsOutgoingMessageCreationParams createFromParcel(Parcel in) {
            return new RcsOutgoingMessageCreationParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsOutgoingMessageCreationParams[] newArray(int size) {
            return new RcsOutgoingMessageCreationParams[size];
        }
    };

    public static class Builder extends RcsMessageCreationParams.Builder {
        public Builder(long originationTimestamp, int subscriptionId) {
            super(originationTimestamp, subscriptionId);
        }

        @Override // android.telephony.ims.RcsMessageCreationParams.Builder
        public RcsOutgoingMessageCreationParams build() {
            return new RcsOutgoingMessageCreationParams(this);
        }
    }

    private RcsOutgoingMessageCreationParams(Builder builder) {
        super(builder);
    }

    private RcsOutgoingMessageCreationParams(Parcel in) {
        super(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest);
    }
}
