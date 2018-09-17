package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class BearerDescription implements Parcelable {
    public static final Creator<BearerDescription> CREATOR = new Creator<BearerDescription>() {
        public BearerDescription createFromParcel(Parcel in) {
            return new BearerDescription(in, null);
        }

        public BearerDescription[] newArray(int size) {
            return new BearerDescription[size];
        }
    };
    public byte[] parameters;
    public BearerType type;

    public enum BearerType {
        MOBILE_CSD(1),
        MOBILE_PS(2),
        DEFAULT_BEARER(3),
        LOCAL_LINK(4),
        BLUETOOTH(5),
        IRDA(6),
        RS232(7),
        MOBILE_PS_EXTENDED_QOS(9),
        I_WLAN(10),
        E_UTRAN(11),
        USB(16);
        
        private int mValue;

        private BearerType(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }
    }

    public BearerDescription(BearerType type, byte[] parameters) {
        this.parameters = new byte[0];
        this.type = type;
        this.parameters = (byte[]) parameters.clone();
    }

    private BearerDescription(Parcel in) {
        this.parameters = new byte[0];
        this.type = BearerType.values()[in.readInt()];
        int len = in.readInt();
        if (len > 0) {
            this.parameters = new byte[len];
            in.readByteArray(this.parameters);
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type.ordinal());
        dest.writeInt(this.parameters.length);
        if (this.parameters.length > 0) {
            dest.writeByteArray(this.parameters);
        }
    }

    public int describeContents() {
        return 0;
    }
}
