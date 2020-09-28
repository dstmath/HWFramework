package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class HwPFWStartupSetting implements Parcelable, Cloneable {
    public static final Parcelable.Creator<HwPFWStartupSetting> CREATOR = new Parcelable.Creator<HwPFWStartupSetting>() {
        /* class huawei.android.pfw.HwPFWStartupSetting.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwPFWStartupSetting createFromParcel(Parcel source) {
            return new HwPFWStartupSetting(source);
        }

        @Override // android.os.Parcelable.Creator
        public HwPFWStartupSetting[] newArray(int size) {
            return new HwPFWStartupSetting[0];
        }
    };
    public static final int STARTUP_STATUS_ALLOW = 1;
    public static final int STARTUP_STATUS_FORBID = 0;
    public static final int STARTUP_STATUS_UNKNOWN = 2;
    public static final int STARTUP_TYPE_NUM = 2;
    public static final int STARTUP_TYPE_RECEIVER = 0;
    public static final int STARTUP_TYPE_SERVICE_PROVIDER = 1;
    private static final String TAG = "HwPFWStartupSetting";
    private int mAllow;
    private String mPackageName;
    private int mType;

    public HwPFWStartupSetting(String packageName, int type, int allow) {
        this.mPackageName = packageName;
        this.mType = type;
        this.mAllow = allow;
    }

    public HwPFWStartupSetting(String packageName, int type) {
        this.mPackageName = packageName;
        this.mType = type;
        this.mAllow = 2;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getTypeValue() {
        return this.mType;
    }

    public int getAllowValue() {
        return this.mAllow;
    }

    public boolean valid() {
        int i = this.mType;
        if (i != 0 && 1 != i) {
            return false;
        }
        int i2 = this.mAllow;
        return i2 == 0 || 1 == i2 || 2 == i2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mType);
        dest.writeInt(this.mAllow);
    }

    private HwPFWStartupSetting(Parcel source) {
        this.mPackageName = source.readString();
        this.mType = source.readInt();
        this.mAllow = source.readInt();
    }

    public String toString() {
        return "HwPFWStartupSetting {" + this.mPackageName + ", type: " + this.mType + ", allow: " + this.mAllow + "}";
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        try {
            return (HwPFWStartupSetting) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "CloneNotSupportedException");
            return null;
        }
    }
}
