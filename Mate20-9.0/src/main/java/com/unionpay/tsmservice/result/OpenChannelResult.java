package com.unionpay.tsmservice.result;

import android.os.Parcel;
import android.os.Parcelable;

public class OpenChannelResult implements Parcelable {
    public static final Parcelable.Creator<OpenChannelResult> CREATOR = new Parcelable.Creator<OpenChannelResult>() {
        public final OpenChannelResult createFromParcel(Parcel parcel) {
            return new OpenChannelResult(parcel);
        }

        public final OpenChannelResult[] newArray(int i) {
            return new OpenChannelResult[i];
        }
    };
    private String channel;
    private String outHexApdu;

    public OpenChannelResult() {
    }

    public OpenChannelResult(Parcel parcel) {
        this.outHexApdu = parcel.readString();
        this.channel = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getOutHexApdu() {
        return this.outHexApdu;
    }

    public void setChannel(String str) {
        this.channel = str;
    }

    public void setOutHexApdu(String str) {
        this.outHexApdu = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.outHexApdu);
        parcel.writeString(this.channel);
    }
}
