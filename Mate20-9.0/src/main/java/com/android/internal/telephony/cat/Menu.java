package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Menu implements Parcelable {
    public static final Parcelable.Creator<Menu> CREATOR = new Parcelable.Creator<Menu>() {
        public Menu createFromParcel(Parcel in) {
            return new Menu(in);
        }

        public Menu[] newArray(int size) {
            return new Menu[size];
        }
    };
    public int defaultItem;
    public boolean helpAvailable;
    public List<Item> items;
    public boolean itemsIconSelfExplanatory;
    public PresentationType presentationType;
    public boolean softKeyPreferred;
    public String title;
    public List<TextAttribute> titleAttrs;
    public Bitmap titleIcon;
    public boolean titleIconSelfExplanatory;

    public Menu() {
        this.items = new ArrayList();
        this.title = null;
        this.titleAttrs = null;
        this.defaultItem = 0;
        this.softKeyPreferred = false;
        this.helpAvailable = false;
        this.titleIconSelfExplanatory = false;
        this.itemsIconSelfExplanatory = false;
        this.titleIcon = null;
        this.presentationType = PresentationType.NAVIGATION_OPTIONS;
    }

    private Menu(Parcel in) {
        this.title = in.readString();
        this.titleIcon = (Bitmap) in.readParcelable(null);
        this.items = new ArrayList();
        int size = in.readInt();
        boolean z = false;
        for (int i = 0; i < size; i++) {
            this.items.add((Item) in.readParcelable(null));
        }
        this.defaultItem = in.readInt();
        this.softKeyPreferred = in.readInt() == 1;
        this.helpAvailable = in.readInt() == 1;
        this.titleIconSelfExplanatory = in.readInt() == 1;
        this.itemsIconSelfExplanatory = in.readInt() == 1 ? true : z;
        this.presentationType = PresentationType.values()[in.readInt()];
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeParcelable(this.titleIcon, flags);
        int size = this.items.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeParcelable(this.items.get(i), flags);
        }
        dest.writeInt(this.defaultItem);
        dest.writeInt(this.softKeyPreferred ? 1 : 0);
        dest.writeInt(this.helpAvailable ? 1 : 0);
        dest.writeInt(this.titleIconSelfExplanatory ? 1 : 0);
        dest.writeInt(this.itemsIconSelfExplanatory ? 1 : 0);
        dest.writeInt(this.presentationType.ordinal());
    }
}
