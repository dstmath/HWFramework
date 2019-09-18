package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.collect.Sets;
import java.util.HashSet;
import java.util.Iterator;

public class InterfaceConfiguration implements Parcelable {
    public static final Parcelable.Creator<InterfaceConfiguration> CREATOR = new Parcelable.Creator<InterfaceConfiguration>() {
        public InterfaceConfiguration createFromParcel(Parcel in) {
            InterfaceConfiguration info = new InterfaceConfiguration();
            String unused = info.mHwAddr = in.readString();
            if (in.readByte() == 1) {
                LinkAddress unused2 = info.mAddr = (LinkAddress) in.readParcelable(null);
            }
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                info.mFlags.add(in.readString());
            }
            return info;
        }

        public InterfaceConfiguration[] newArray(int size) {
            return new InterfaceConfiguration[size];
        }
    };
    private static final String FLAG_DOWN = "down";
    private static final String FLAG_UP = "up";
    /* access modifiers changed from: private */
    public LinkAddress mAddr;
    /* access modifiers changed from: private */
    public HashSet<String> mFlags = Sets.newHashSet();
    /* access modifiers changed from: private */
    public String mHwAddr;

    public String toString() {
        return "mHwAddr=" + this.mHwAddr + " mAddr=" + String.valueOf(this.mAddr) + " mFlags=" + getFlags();
    }

    public Iterable<String> getFlags() {
        return this.mFlags;
    }

    public boolean hasFlag(String flag) {
        validateFlag(flag);
        return this.mFlags.contains(flag);
    }

    public void clearFlag(String flag) {
        validateFlag(flag);
        this.mFlags.remove(flag);
    }

    public void setFlag(String flag) {
        validateFlag(flag);
        this.mFlags.add(flag);
    }

    public void setInterfaceUp() {
        this.mFlags.remove(FLAG_DOWN);
        this.mFlags.add(FLAG_UP);
    }

    public void setInterfaceDown() {
        this.mFlags.remove(FLAG_UP);
        this.mFlags.add(FLAG_DOWN);
    }

    public void ignoreInterfaceUpDownStatus() {
        this.mFlags.remove(FLAG_UP);
        this.mFlags.remove(FLAG_DOWN);
    }

    public LinkAddress getLinkAddress() {
        return this.mAddr;
    }

    public void setLinkAddress(LinkAddress addr) {
        this.mAddr = addr;
    }

    public String getHardwareAddress() {
        return this.mHwAddr;
    }

    public void setHardwareAddress(String hwAddr) {
        this.mHwAddr = hwAddr;
    }

    public boolean isActive() {
        try {
            if (isUp()) {
                for (byte b : this.mAddr.getAddress().getAddress()) {
                    if (b != 0) {
                        return true;
                    }
                }
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isUp() {
        return hasFlag(FLAG_UP);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mHwAddr);
        if (this.mAddr != null) {
            dest.writeByte((byte) 1);
            dest.writeParcelable(this.mAddr, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeInt(this.mFlags.size());
        Iterator<String> it = this.mFlags.iterator();
        while (it.hasNext()) {
            dest.writeString(it.next());
        }
    }

    private static void validateFlag(String flag) {
        if (flag.indexOf(32) >= 0) {
            throw new IllegalArgumentException("flag contains space: " + flag);
        }
    }
}
