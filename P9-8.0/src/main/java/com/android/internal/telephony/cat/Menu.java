package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class Menu implements Parcelable {
    public static final Creator<Menu> CREATOR = new Creator<Menu>() {
        public Menu createFromParcel(Parcel in) {
            return new Menu(in, null);
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

    /* synthetic */ Menu(Parcel in, Menu -this1) {
        this(in);
    }

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
        boolean z;
        boolean z2 = true;
        this.title = in.readString();
        this.titleIcon = (Bitmap) in.readParcelable(null);
        this.items = new ArrayList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            this.items.add((Item) in.readParcelable(null));
        }
        this.defaultItem = in.readInt();
        this.softKeyPreferred = in.readInt() == 1;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.helpAvailable = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.titleIconSelfExplanatory = z;
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.itemsIconSelfExplanatory = z2;
        this.presentationType = PresentationType.values()[in.readInt()];
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.title);
        dest.writeParcelable(this.titleIcon, flags);
        int size = this.items.size();
        dest.writeInt(size);
        for (int i3 = 0; i3 < size; i3++) {
            dest.writeParcelable((Parcelable) this.items.get(i3), flags);
        }
        dest.writeInt(this.defaultItem);
        dest.writeInt(this.softKeyPreferred ? 1 : 0);
        if (this.helpAvailable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.titleIconSelfExplanatory) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.itemsIconSelfExplanatory) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.presentationType.ordinal());
    }
}
