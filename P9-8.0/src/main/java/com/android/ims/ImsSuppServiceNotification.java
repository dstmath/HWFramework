package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import java.util.Arrays;

public class ImsSuppServiceNotification implements Parcelable {
    public static final Creator<ImsSuppServiceNotification> CREATOR = new Creator<ImsSuppServiceNotification>() {
        public ImsSuppServiceNotification createFromParcel(Parcel in) {
            return new ImsSuppServiceNotification(in);
        }

        public ImsSuppServiceNotification[] newArray(int size) {
            return new ImsSuppServiceNotification[size];
        }
    };
    private static final String TAG = "ImsSuppServiceNotification";
    public int code;
    public String[] history;
    public int index;
    public int notificationType;
    public String number;
    public int type;

    public ImsSuppServiceNotification(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        return "{ notificationType=" + this.notificationType + ", code=" + this.code + ", index=" + this.index + ", type=" + this.type + ", number=" + PhoneNumberUtils.toLogSafePhoneNumber(this.number) + ", history=" + PhoneNumberUtils.toLogSafePhoneNumber(Arrays.toString(this.history)) + " }";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.notificationType);
        out.writeInt(this.code);
        out.writeInt(this.index);
        out.writeInt(this.type);
        out.writeString(this.number);
        out.writeStringArray(this.history);
    }

    private void readFromParcel(Parcel in) {
        this.notificationType = in.readInt();
        this.code = in.readInt();
        this.index = in.readInt();
        this.type = in.readInt();
        this.number = in.readString();
        this.history = in.createStringArray();
    }
}
