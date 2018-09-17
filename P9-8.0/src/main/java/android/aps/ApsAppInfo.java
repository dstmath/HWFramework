package android.aps;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ApsAppInfo implements Parcelable {
    public static final int BRIGHTNESS = 8;
    public static final Creator<ApsAppInfo> CREATOR = new Creator<ApsAppInfo>() {
        public ApsAppInfo createFromParcel(Parcel in) {
            return new ApsAppInfo(in);
        }

        public ApsAppInfo[] newArray(int size) {
            return new ApsAppInfo[size];
        }
    };
    public static final int FRAMERATE = 2;
    public static final int RESOLUTION = 1;
    private static final String TAG = "ApsAppInfo";
    public static final int TEXTURE = 4;
    private String mBasePackageName = ProxyInfo.LOCAL_EXCL_LIST;
    private int mBrightnessPercent;
    private int mFrameRate;
    private float mResolutionRatio;
    private boolean mSwitchable;
    private int mTexturePercent;

    private ApsAppInfo() {
    }

    public ApsAppInfo(Parcel parcel) {
        readFromParcel(parcel);
    }

    public ApsAppInfo(ApsAppInfo info) {
        this.mBasePackageName = info.mBasePackageName;
        this.mResolutionRatio = info.mResolutionRatio;
        this.mFrameRate = info.mFrameRate;
        this.mTexturePercent = info.mTexturePercent;
        this.mBrightnessPercent = info.mBrightnessPercent;
        this.mSwitchable = info.mSwitchable;
    }

    public ApsAppInfo(String pkgName, float resolutionRatio, int frameRatio, int texturePercent, int brightnessPercent, boolean switchable) {
        this.mBasePackageName = pkgName;
        this.mResolutionRatio = resolutionRatio;
        this.mFrameRate = frameRatio;
        this.mTexturePercent = texturePercent;
        this.mBrightnessPercent = brightnessPercent;
        this.mSwitchable = switchable;
    }

    public void setBasePackageName(String pkgName) {
        this.mBasePackageName = pkgName;
    }

    public void setResolutionRatio(float resolutionRatio, boolean switchable) {
        this.mResolutionRatio = resolutionRatio;
        this.mSwitchable = switchable;
    }

    public void setFps(int frameRate) {
        this.mFrameRate = frameRate;
    }

    public void setTexturePercent(int texturePercent) {
        this.mTexturePercent = texturePercent;
    }

    public void setBrightnessPercent(int brightnessPercent) {
        this.mBrightnessPercent = brightnessPercent;
    }

    public String getBasePackageName() {
        return this.mBasePackageName;
    }

    public float getResolutionRatio() {
        return this.mResolutionRatio;
    }

    public int getFrameRatio() {
        return this.mFrameRate;
    }

    public int getTexturePercent() {
        return this.mTexturePercent;
    }

    public int getBrightnessPercent() {
        return this.mBrightnessPercent;
    }

    public boolean getSwitchable() {
        return this.mSwitchable;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.mBasePackageName);
        dest.writeFloat(this.mResolutionRatio);
        dest.writeInt(this.mFrameRate);
        dest.writeInt(this.mTexturePercent);
        dest.writeInt(this.mBrightnessPercent);
        dest.writeByte((byte) (this.mSwitchable ? 1 : 0));
    }

    private void readFromParcel(Parcel in) {
        boolean z = false;
        this.mBasePackageName = in.readString();
        this.mResolutionRatio = in.readFloat();
        this.mFrameRate = in.readInt();
        this.mTexturePercent = in.readInt();
        this.mBrightnessPercent = in.readInt();
        if (in.readByte() != (byte) 0) {
            z = true;
        }
        this.mSwitchable = z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("pkgName:").append(this.mBasePackageName).append(", resolutionRatio:").append(this.mResolutionRatio).append(", frameRate:").append(this.mFrameRate).append(", texturePercent:").append(this.mTexturePercent).append(", brightnessPercent:").append(this.mBrightnessPercent).append(", switchable: ").append(this.mSwitchable);
        return sb.toString();
    }

    public int hashCode() {
        return (((((((((((this.mBasePackageName == null ? 0 : this.mBasePackageName.hashCode()) + 31) * 31) + this.mBrightnessPercent) * 31) + this.mFrameRate) * 31) + Float.floatToIntBits(this.mResolutionRatio)) * 31) + (this.mSwitchable ? 1231 : 1237)) * 31) + this.mTexturePercent;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ApsAppInfo other = (ApsAppInfo) obj;
        if (this.mBasePackageName == null) {
            if (other.mBasePackageName != null) {
                return false;
            }
        } else if (!this.mBasePackageName.equals(other.mBasePackageName)) {
            return false;
        }
        return this.mBrightnessPercent == other.mBrightnessPercent && this.mFrameRate == other.mFrameRate && Float.floatToIntBits(this.mResolutionRatio) == Float.floatToIntBits(other.mResolutionRatio) && this.mSwitchable == other.mSwitchable && this.mTexturePercent == other.mTexturePercent;
    }
}
