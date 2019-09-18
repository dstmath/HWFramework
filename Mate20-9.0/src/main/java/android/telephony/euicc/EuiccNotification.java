package android.telephony.euicc;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

@SystemApi
public final class EuiccNotification implements Parcelable {
    public static final int ALL_EVENTS = 15;
    public static final Parcelable.Creator<EuiccNotification> CREATOR = new Parcelable.Creator<EuiccNotification>() {
        public EuiccNotification createFromParcel(Parcel source) {
            return new EuiccNotification(source);
        }

        public EuiccNotification[] newArray(int size) {
            return new EuiccNotification[size];
        }
    };
    public static final int EVENT_DELETE = 8;
    public static final int EVENT_DISABLE = 4;
    public static final int EVENT_ENABLE = 2;
    public static final int EVENT_INSTALL = 1;
    private final byte[] mData;
    private final int mEvent;
    private final int mSeq;
    private final String mTargetAddr;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Event {
    }

    public EuiccNotification(int seq, String targetAddr, int event, byte[] data) {
        this.mSeq = seq;
        this.mTargetAddr = targetAddr;
        this.mEvent = event;
        this.mData = data;
    }

    public int getSeq() {
        return this.mSeq;
    }

    public String getTargetAddr() {
        return this.mTargetAddr;
    }

    public int getEvent() {
        return this.mEvent;
    }

    public byte[] getData() {
        return this.mData;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EuiccNotification that = (EuiccNotification) obj;
        if (this.mSeq != that.mSeq || !Objects.equals(this.mTargetAddr, that.mTargetAddr) || this.mEvent != that.mEvent || !Arrays.equals(this.mData, that.mData)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * 1) + this.mSeq)) + Objects.hashCode(this.mTargetAddr))) + this.mEvent)) + Arrays.hashCode(this.mData);
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("EuiccNotification (seq=");
        sb.append(this.mSeq);
        sb.append(", targetAddr=");
        sb.append(this.mTargetAddr);
        sb.append(", event=");
        sb.append(this.mEvent);
        sb.append(", data=");
        if (this.mData == null) {
            str = "null";
        } else {
            str = "byte[" + this.mData.length + "]";
        }
        sb.append(str);
        sb.append(")");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSeq);
        dest.writeString(this.mTargetAddr);
        dest.writeInt(this.mEvent);
        dest.writeByteArray(this.mData);
    }

    private EuiccNotification(Parcel source) {
        this.mSeq = source.readInt();
        this.mTargetAddr = source.readString();
        this.mEvent = source.readInt();
        this.mData = source.createByteArray();
    }
}
