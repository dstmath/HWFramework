package android.os;

import android.os.Parcelable.Creator;

public class JankAppInfo implements Parcelable {
    public static final Creator<JankAppInfo> CREATOR = new Creator<JankAppInfo>() {
        public JankAppInfo createFromParcel(Parcel in) {
            return new JankAppInfo(in, null);
        }

        public JankAppInfo[] newArray(int size) {
            return new JankAppInfo[size];
        }
    };
    public boolean coreApp;
    public int flags;
    public String packageName;
    public boolean systemApp;
    public int versionCode;
    public String versionName;

    /* synthetic */ JankAppInfo(Parcel in, JankAppInfo -this1) {
        this(in);
    }

    private JankAppInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.packageName = in.readString();
        this.versionCode = in.readInt();
        this.versionName = in.readString();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.coreApp = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.systemApp = z2;
        this.flags = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        int i;
        int i2 = 1;
        dest.writeString(this.packageName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.versionName);
        if (this.coreApp) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.systemApp) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.flags);
    }
}
