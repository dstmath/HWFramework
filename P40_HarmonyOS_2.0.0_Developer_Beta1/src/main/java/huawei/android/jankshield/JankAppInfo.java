package huawei.android.jankshield;

import android.os.Parcel;
import android.os.Parcelable;

public class JankAppInfo implements Parcelable {
    public static final Parcelable.Creator<JankAppInfo> CREATOR = new Parcelable.Creator<JankAppInfo>() {
        /* class huawei.android.jankshield.JankAppInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public JankAppInfo createFromParcel(Parcel in) {
            return new JankAppInfo(in);
        }

        @Override // android.os.Parcelable.Creator
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

    public JankAppInfo() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.packageName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.versionName);
        dest.writeInt(this.coreApp ? 1 : 0);
        dest.writeInt(this.systemApp ? 1 : 0);
        dest.writeInt(this.flags);
    }

    private JankAppInfo(Parcel in) {
        this.packageName = in.readString();
        this.versionCode = in.readInt();
        this.versionName = in.readString();
        boolean z = true;
        this.coreApp = in.readInt() != 0;
        this.systemApp = in.readInt() == 0 ? false : z;
        this.flags = in.readInt();
    }
}
