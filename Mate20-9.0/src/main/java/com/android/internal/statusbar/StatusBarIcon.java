package com.android.internal.statusbar;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.text.TextUtils;

public class StatusBarIcon implements Parcelable {
    public static final Parcelable.Creator<StatusBarIcon> CREATOR = new Parcelable.Creator<StatusBarIcon>() {
        public StatusBarIcon createFromParcel(Parcel parcel) {
            return new StatusBarIcon(parcel);
        }

        public StatusBarIcon[] newArray(int size) {
            return new StatusBarIcon[size];
        }
    };
    public CharSequence contentDescription;
    public Icon icon;
    public int iconLevel;
    public int number;
    public String pkg;
    public UserHandle user;
    public boolean visible;

    public StatusBarIcon(UserHandle user2, String resPackage, Icon icon2, int iconLevel2, int number2, CharSequence contentDescription2) {
        this.visible = true;
        if (icon2.getType() == 2 && TextUtils.isEmpty(icon2.getResPackage())) {
            icon2 = Icon.createWithResource(resPackage, icon2.getResId());
        }
        this.pkg = resPackage;
        this.user = user2;
        this.icon = icon2;
        this.iconLevel = iconLevel2;
        this.number = number2;
        this.contentDescription = contentDescription2;
    }

    public StatusBarIcon(String iconPackage, UserHandle user2, int iconId, int iconLevel2, int number2, CharSequence contentDescription2) {
        this(user2, iconPackage, Icon.createWithResource(iconPackage, iconId), iconLevel2, number2, contentDescription2);
    }

    public String toString() {
        String str;
        String str2;
        StringBuilder sb = new StringBuilder();
        sb.append("StatusBarIcon(icon=");
        sb.append(this.icon);
        if (this.iconLevel != 0) {
            str = " level=" + this.iconLevel;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(this.visible ? " visible" : "");
        sb.append(" user=");
        sb.append(this.user.getIdentifier());
        if (this.number != 0) {
            str2 = " num=" + this.number;
        } else {
            str2 = "";
        }
        sb.append(str2);
        sb.append(" )");
        return sb.toString();
    }

    public StatusBarIcon clone() {
        StatusBarIcon that = new StatusBarIcon(this.user, this.pkg, this.icon, this.iconLevel, this.number, this.contentDescription);
        that.visible = this.visible;
        return that;
    }

    public StatusBarIcon(Parcel in) {
        this.visible = true;
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.icon = (Icon) in.readParcelable(null);
        this.pkg = in.readString();
        this.user = (UserHandle) in.readParcelable(null);
        this.iconLevel = in.readInt();
        this.visible = in.readInt() != 0;
        this.number = in.readInt();
        this.contentDescription = in.readCharSequence();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.icon, 0);
        out.writeString(this.pkg);
        out.writeParcelable(this.user, 0);
        out.writeInt(this.iconLevel);
        out.writeInt(this.visible ? 1 : 0);
        out.writeInt(this.number);
        out.writeCharSequence(this.contentDescription);
    }

    public int describeContents() {
        return 0;
    }
}
