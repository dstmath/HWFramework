package android.hardware.hdmi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class HdmiPortInfo implements Parcelable {
    public static final Creator<HdmiPortInfo> CREATOR = new Creator<HdmiPortInfo>() {
        public HdmiPortInfo createFromParcel(Parcel source) {
            return new HdmiPortInfo(source.readInt(), source.readInt(), source.readInt(), source.readInt() == 1, source.readInt() == 1, source.readInt() == 1);
        }

        public HdmiPortInfo[] newArray(int size) {
            return new HdmiPortInfo[size];
        }
    };
    public static final int PORT_INPUT = 0;
    public static final int PORT_OUTPUT = 1;
    private final int mAddress;
    private final boolean mArcSupported;
    private final boolean mCecSupported;
    private final int mId;
    private final boolean mMhlSupported;
    private final int mType;

    public HdmiPortInfo(int id, int type, int address, boolean cec, boolean mhl, boolean arc) {
        this.mId = id;
        this.mType = type;
        this.mAddress = address;
        this.mCecSupported = cec;
        this.mArcSupported = arc;
        this.mMhlSupported = mhl;
    }

    public int getId() {
        return this.mId;
    }

    public int getType() {
        return this.mType;
    }

    public int getAddress() {
        return this.mAddress;
    }

    public boolean isCecSupported() {
        return this.mCecSupported;
    }

    public boolean isMhlSupported() {
        return this.mMhlSupported;
    }

    public boolean isArcSupported() {
        return this.mArcSupported;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mId);
        dest.writeInt(this.mType);
        dest.writeInt(this.mAddress);
        if (this.mCecSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mArcSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mMhlSupported) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("port_id: ").append(this.mId).append(", ");
        s.append("address: ").append(String.format("0x%04x", new Object[]{Integer.valueOf(this.mAddress)})).append(", ");
        s.append("cec: ").append(this.mCecSupported).append(", ");
        s.append("arc: ").append(this.mArcSupported).append(", ");
        s.append("mhl: ").append(this.mMhlSupported);
        return s.toString();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof HdmiPortInfo)) {
            return false;
        }
        HdmiPortInfo other = (HdmiPortInfo) o;
        if (this.mId == other.mId && this.mType == other.mType && this.mAddress == other.mAddress && this.mCecSupported == other.mCecSupported && this.mArcSupported == other.mArcSupported && this.mMhlSupported == other.mMhlSupported) {
            z = true;
        }
        return z;
    }
}
