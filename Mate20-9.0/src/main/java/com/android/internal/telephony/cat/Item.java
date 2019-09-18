package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    public Bitmap icon;
    public int id;
    public String text;

    public Item(int id2, String text2) {
        this(id2, text2, null);
    }

    public Item(int id2, String text2, Bitmap icon2) {
        this.id = id2;
        this.text = text2;
        this.icon = icon2;
    }

    public Item(Parcel in) {
        this.id = in.readInt();
        this.text = in.readString();
        this.icon = (Bitmap) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.text);
        dest.writeParcelable(this.icon, flags);
    }

    public String toString() {
        return this.text;
    }
}
