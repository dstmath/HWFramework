package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Input implements Parcelable {
    public static final Creator<Input> CREATOR = new Creator<Input>() {
        public Input createFromParcel(Parcel in) {
            return new Input(in, null);
        }

        public Input[] newArray(int size) {
            return new Input[size];
        }
    };
    public String defaultText;
    public boolean digitOnly;
    public Duration duration;
    public boolean echo;
    public boolean helpAvailable;
    public Bitmap icon;
    public boolean iconSelfExplanatory;
    public int maxLen;
    public int minLen;
    public boolean packed;
    public String text;
    public boolean ucs2;
    public boolean yesNo;

    /* synthetic */ Input(Parcel in, Input -this1) {
        this(in);
    }

    Input() {
        this.text = "";
        this.defaultText = null;
        this.icon = null;
        this.minLen = 0;
        this.maxLen = 1;
        this.ucs2 = false;
        this.packed = false;
        this.digitOnly = false;
        this.echo = false;
        this.yesNo = false;
        this.helpAvailable = false;
        this.duration = null;
        this.iconSelfExplanatory = false;
    }

    private Input(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.text = in.readString();
        this.defaultText = in.readString();
        this.icon = (Bitmap) in.readParcelable(null);
        this.minLen = in.readInt();
        this.maxLen = in.readInt();
        this.ucs2 = in.readInt() == 1;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.packed = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.digitOnly = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.echo = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.yesNo = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.helpAvailable = z;
        this.duration = (Duration) in.readParcelable(null);
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.iconSelfExplanatory = z2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.text);
        dest.writeString(this.defaultText);
        dest.writeParcelable(this.icon, 0);
        dest.writeInt(this.minLen);
        dest.writeInt(this.maxLen);
        dest.writeInt(this.ucs2 ? 1 : 0);
        if (this.packed) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.digitOnly) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.echo) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.yesNo) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.helpAvailable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeParcelable(this.duration, 0);
        if (!this.iconSelfExplanatory) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    boolean setIcon(Bitmap Icon) {
        return true;
    }
}
