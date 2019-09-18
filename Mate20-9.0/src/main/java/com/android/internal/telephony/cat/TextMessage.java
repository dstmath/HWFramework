package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class TextMessage implements Parcelable {
    public static final Parcelable.Creator<TextMessage> CREATOR = new Parcelable.Creator<TextMessage>() {
        public TextMessage createFromParcel(Parcel in) {
            return new TextMessage(in);
        }

        public TextMessage[] newArray(int size) {
            return new TextMessage[size];
        }
    };
    public Duration duration;
    public Bitmap icon;
    public boolean iconSelfExplanatory;
    public boolean isHighPriority;
    public boolean responseNeeded;
    public String text;
    public String title;
    public boolean userClear;

    TextMessage() {
        this.title = "";
        this.text = null;
        this.icon = null;
        this.iconSelfExplanatory = false;
        this.isHighPriority = false;
        this.responseNeeded = true;
        this.userClear = false;
        this.duration = null;
    }

    private TextMessage(Parcel in) {
        this.title = "";
        this.text = null;
        this.icon = null;
        boolean z = false;
        this.iconSelfExplanatory = false;
        this.isHighPriority = false;
        this.responseNeeded = true;
        this.userClear = false;
        this.duration = null;
        this.title = in.readString();
        this.text = in.readString();
        this.icon = (Bitmap) in.readParcelable(null);
        this.iconSelfExplanatory = in.readInt() == 1;
        this.isHighPriority = in.readInt() == 1;
        this.responseNeeded = in.readInt() == 1;
        this.userClear = in.readInt() == 1 ? true : z;
        this.duration = (Duration) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.text);
        dest.writeParcelable(this.icon, 0);
        dest.writeInt(this.iconSelfExplanatory ? 1 : 0);
        dest.writeInt(this.isHighPriority ? 1 : 0);
        dest.writeInt(this.responseNeeded ? 1 : 0);
        dest.writeInt(this.userClear ? 1 : 0);
        dest.writeParcelable(this.duration, 0);
    }

    public String toString() {
        return "title=" + this.title + " text=" + this.text + " icon=" + this.icon + " iconSelfExplanatory=" + this.iconSelfExplanatory + " isHighPriority=" + this.isHighPriority + " responseNeeded=" + this.responseNeeded + " userClear=" + this.userClear + " duration=" + this.duration;
    }
}
