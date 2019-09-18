package android.hardware.camera2.impl;

import android.os.Parcel;
import android.os.Parcelable;

public class CaptureResultExtras implements Parcelable {
    public static final Parcelable.Creator<CaptureResultExtras> CREATOR = new Parcelable.Creator<CaptureResultExtras>() {
        public CaptureResultExtras createFromParcel(Parcel in) {
            return new CaptureResultExtras(in);
        }

        public CaptureResultExtras[] newArray(int size) {
            return new CaptureResultExtras[size];
        }
    };
    private int afTriggerId;
    private int errorStreamId;
    private long frameNumber;
    private int partialResultCount;
    private int precaptureTriggerId;
    private int requestId;
    private int subsequenceId;

    private CaptureResultExtras(Parcel in) {
        readFromParcel(in);
    }

    public CaptureResultExtras(int requestId2, int subsequenceId2, int afTriggerId2, int precaptureTriggerId2, long frameNumber2, int partialResultCount2, int errorStreamId2) {
        this.requestId = requestId2;
        this.subsequenceId = subsequenceId2;
        this.afTriggerId = afTriggerId2;
        this.precaptureTriggerId = precaptureTriggerId2;
        this.frameNumber = frameNumber2;
        this.partialResultCount = partialResultCount2;
        this.errorStreamId = errorStreamId2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestId);
        dest.writeInt(this.subsequenceId);
        dest.writeInt(this.afTriggerId);
        dest.writeInt(this.precaptureTriggerId);
        dest.writeLong(this.frameNumber);
        dest.writeInt(this.partialResultCount);
        dest.writeInt(this.errorStreamId);
    }

    public void readFromParcel(Parcel in) {
        this.requestId = in.readInt();
        this.subsequenceId = in.readInt();
        this.afTriggerId = in.readInt();
        this.precaptureTriggerId = in.readInt();
        this.frameNumber = in.readLong();
        this.partialResultCount = in.readInt();
        this.errorStreamId = in.readInt();
    }

    public int getRequestId() {
        return this.requestId;
    }

    public int getSubsequenceId() {
        return this.subsequenceId;
    }

    public int getAfTriggerId() {
        return this.afTriggerId;
    }

    public int getPrecaptureTriggerId() {
        return this.precaptureTriggerId;
    }

    public long getFrameNumber() {
        return this.frameNumber;
    }

    public int getPartialResultCount() {
        return this.partialResultCount;
    }

    public int getErrorStreamId() {
        return this.errorStreamId;
    }
}
