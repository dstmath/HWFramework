package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;

public class HwPFWStartupSetting implements Parcelable, Cloneable {
    public static final Parcelable.Creator<HwPFWStartupSetting> CREATOR = new Parcelable.Creator<HwPFWStartupSetting>() {
        public HwPFWStartupSetting createFromParcel(Parcel source) {
            return new HwPFWStartupSetting(source);
        }

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
        if (this.mType == 0 || 1 == this.mType) {
            return this.mAllow == 0 || 1 == this.mAllow || 2 == this.mAllow;
        }
        return false;
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
    public Object clone() throws CloneNotSupportedException {
        try {
            return (HwPFWStartupSetting) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
