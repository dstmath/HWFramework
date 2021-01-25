package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationInfo implements Parcelable {
    public static final Parcelable.Creator<ConfigurationInfo> CREATOR = new Parcelable.Creator<ConfigurationInfo>() {
        /* class android.content.pm.ConfigurationInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConfigurationInfo createFromParcel(Parcel source) {
            return new ConfigurationInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ConfigurationInfo[] newArray(int size) {
            return new ConfigurationInfo[size];
        }
    };
    public static final int GL_ES_VERSION_UNDEFINED = 0;
    public static final int INPUT_FEATURE_FIVE_WAY_NAV = 2;
    public static final int INPUT_FEATURE_HARD_KEYBOARD = 1;
    public int reqGlEsVersion;
    public int reqInputFeatures;
    public int reqKeyboardType;
    public int reqNavigation;
    public int reqTouchScreen;

    public ConfigurationInfo() {
        this.reqInputFeatures = 0;
    }

    public ConfigurationInfo(ConfigurationInfo orig) {
        this.reqInputFeatures = 0;
        this.reqTouchScreen = orig.reqTouchScreen;
        this.reqKeyboardType = orig.reqKeyboardType;
        this.reqNavigation = orig.reqNavigation;
        this.reqInputFeatures = orig.reqInputFeatures;
        this.reqGlEsVersion = orig.reqGlEsVersion;
    }

    public String toString() {
        return "ConfigurationInfo{" + Integer.toHexString(System.identityHashCode(this)) + " touchscreen = " + this.reqTouchScreen + " inputMethod = " + this.reqKeyboardType + " navigation = " + this.reqNavigation + " reqInputFeatures = " + this.reqInputFeatures + " reqGlEsVersion = " + this.reqGlEsVersion + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.reqTouchScreen);
        dest.writeInt(this.reqKeyboardType);
        dest.writeInt(this.reqNavigation);
        dest.writeInt(this.reqInputFeatures);
        dest.writeInt(this.reqGlEsVersion);
    }

    private ConfigurationInfo(Parcel source) {
        this.reqInputFeatures = 0;
        this.reqTouchScreen = source.readInt();
        this.reqKeyboardType = source.readInt();
        this.reqNavigation = source.readInt();
        this.reqInputFeatures = source.readInt();
        this.reqGlEsVersion = source.readInt();
    }

    public String getGlEsVersion() {
        int i = this.reqGlEsVersion;
        return String.valueOf((-65536 & i) >> 16) + "." + String.valueOf(i & 65535);
    }
}
