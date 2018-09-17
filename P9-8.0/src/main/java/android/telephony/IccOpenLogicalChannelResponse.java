package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IccOpenLogicalChannelResponse implements Parcelable {
    public static final Creator<IccOpenLogicalChannelResponse> CREATOR = new Creator<IccOpenLogicalChannelResponse>() {
        public IccOpenLogicalChannelResponse createFromParcel(Parcel in) {
            return new IccOpenLogicalChannelResponse(in, null);
        }

        public IccOpenLogicalChannelResponse[] newArray(int size) {
            return new IccOpenLogicalChannelResponse[size];
        }
    };
    public static final int INVALID_CHANNEL = -1;
    public static final int STATUS_MISSING_RESOURCE = 2;
    public static final int STATUS_NO_ERROR = 1;
    public static final int STATUS_NO_SUCH_ELEMENT = 3;
    public static final int STATUS_UNKNOWN_ERROR = 4;
    private final int mChannel;
    private final byte[] mSelectResponse;
    private final int mStatus;

    /* synthetic */ IccOpenLogicalChannelResponse(Parcel in, IccOpenLogicalChannelResponse -this1) {
        this(in);
    }

    public IccOpenLogicalChannelResponse(int channel, int status, byte[] selectResponse) {
        this.mChannel = channel;
        this.mStatus = status;
        this.mSelectResponse = selectResponse;
    }

    private IccOpenLogicalChannelResponse(Parcel in) {
        this.mChannel = in.readInt();
        this.mStatus = in.readInt();
        int arrayLength = in.readInt();
        if (arrayLength > 0) {
            this.mSelectResponse = new byte[arrayLength];
            in.readByteArray(this.mSelectResponse);
            return;
        }
        this.mSelectResponse = null;
    }

    public int getChannel() {
        return this.mChannel;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public byte[] getSelectResponse() {
        return this.mSelectResponse;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mChannel);
        out.writeInt(this.mStatus);
        if (this.mSelectResponse == null || this.mSelectResponse.length <= 0) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.mSelectResponse.length);
        out.writeByteArray(this.mSelectResponse);
    }

    public String toString() {
        return "Channel: " + this.mChannel + " Status: " + this.mStatus;
    }
}
