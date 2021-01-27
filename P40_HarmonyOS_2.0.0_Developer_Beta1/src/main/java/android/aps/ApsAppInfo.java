package android.aps;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.logging.nano.MetricsProto;

public class ApsAppInfo implements Parcelable {
    public static final int BRIGHTNESS = 8;
    public static final Parcelable.Creator<ApsAppInfo> CREATOR = new Parcelable.Creator<ApsAppInfo>() {
        /* class android.aps.ApsAppInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApsAppInfo createFromParcel(Parcel in) {
            return new ApsAppInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public ApsAppInfo[] newArray(int size) {
            return new ApsAppInfo[size];
        }
    };
    public static final int DEFAULT_VALUE_BRIGHTNESS = 100;
    public static final boolean DEFAULT_VALUE_FB_SKIP = false;
    public static final int DEFAULT_VALUE_FRAMERATE = 60;
    public static final boolean DEFAULT_VALUE_HIGHP_TO_LOWP = false;
    public static final int DEFAULT_VALUE_MIPMAP = 0;
    public static final float DEFAULT_VALUE_RESOLUTION = 1.0f;
    public static final int DEFAULT_VALUE_SHADOWMAP = 0;
    public static final int DEFAULT_VALUE_TEXTURE = 100;
    public static final int FB_SKIP = 16;
    public static final int FRAMERATE = 2;
    public static final int HIGHP_TO_LOWP = 32;
    public static final int MIPMAP = 128;
    public static final int RESOLUTION = 1;
    public static final int SHADOWMAP = 64;
    private static final String TAG = "ApsAppInfo";
    public static final int TEXTURE = 4;
    private String mBasePackageName;
    private int mBrightnessPercent;
    private boolean mFbSkipSwitch;
    private boolean mHighpToLowpSwitch;
    private int mMaxFrameRate;
    private int mMinFrameRate;
    private int mMipMapSwitch;
    private float mResolutionRatio;
    private int mShadowMapSwitch;
    private boolean mSwitchable;
    private int mTexturePercent;

    private ApsAppInfo() {
        this.mBasePackageName = "";
        this.mResolutionRatio = 1.0f;
        this.mMinFrameRate = 60;
        this.mMaxFrameRate = 60;
        this.mTexturePercent = 100;
        this.mBrightnessPercent = 100;
        this.mSwitchable = false;
        this.mFbSkipSwitch = false;
        this.mHighpToLowpSwitch = false;
        this.mShadowMapSwitch = 0;
        this.mMipMapSwitch = 0;
    }

    public ApsAppInfo(Parcel parcel) {
        this.mBasePackageName = "";
        this.mResolutionRatio = 1.0f;
        this.mMinFrameRate = 60;
        this.mMaxFrameRate = 60;
        this.mTexturePercent = 100;
        this.mBrightnessPercent = 100;
        this.mSwitchable = false;
        this.mFbSkipSwitch = false;
        this.mHighpToLowpSwitch = false;
        this.mShadowMapSwitch = 0;
        this.mMipMapSwitch = 0;
        readFromParcel(parcel);
    }

    public ApsAppInfo(ApsAppInfo info) {
        this.mBasePackageName = "";
        this.mResolutionRatio = 1.0f;
        this.mMinFrameRate = 60;
        this.mMaxFrameRate = 60;
        this.mTexturePercent = 100;
        this.mBrightnessPercent = 100;
        this.mSwitchable = false;
        this.mFbSkipSwitch = false;
        this.mHighpToLowpSwitch = false;
        this.mShadowMapSwitch = 0;
        this.mMipMapSwitch = 0;
        this.mBasePackageName = info.mBasePackageName;
        this.mResolutionRatio = info.mResolutionRatio;
        this.mMinFrameRate = info.mMinFrameRate;
        this.mMaxFrameRate = info.mMaxFrameRate;
        this.mTexturePercent = info.mTexturePercent;
        this.mBrightnessPercent = info.mBrightnessPercent;
        this.mSwitchable = info.mSwitchable;
    }

    public ApsAppInfo(String pkgName, float resolutionRatio, int frameRatio, int texturePercent, int brightnessPercent, boolean isSwitchable) {
        this(pkgName, resolutionRatio, frameRatio, 60, texturePercent, brightnessPercent, isSwitchable);
    }

    public ApsAppInfo(String pkgName, float resolutionRatio, int minFrameRatio, int maxFrameRatio, int texturePercent, int brightnessPercent, boolean isSwitchable) {
        this.mBasePackageName = "";
        this.mResolutionRatio = 1.0f;
        this.mMinFrameRate = 60;
        this.mMaxFrameRate = 60;
        this.mTexturePercent = 100;
        this.mBrightnessPercent = 100;
        this.mSwitchable = false;
        this.mFbSkipSwitch = false;
        this.mHighpToLowpSwitch = false;
        this.mShadowMapSwitch = 0;
        this.mMipMapSwitch = 0;
        this.mBasePackageName = pkgName;
        this.mResolutionRatio = resolutionRatio;
        this.mMinFrameRate = minFrameRatio;
        this.mMaxFrameRate = maxFrameRatio;
        this.mTexturePercent = texturePercent;
        this.mBrightnessPercent = brightnessPercent;
        this.mSwitchable = isSwitchable;
    }

    public ApsAppInfo(String pkgName, float resolutionRatio, int frameRatio, int texturePercent, int brightnessPercent, boolean isSwitchable, boolean isEnableFbSkipSwitch, boolean isEnableHighpToLowpSwitch, int shadowMapSwitch, int mipMapSwitch) {
        this.mBasePackageName = "";
        this.mResolutionRatio = 1.0f;
        this.mMinFrameRate = 60;
        this.mMaxFrameRate = 60;
        this.mTexturePercent = 100;
        this.mBrightnessPercent = 100;
        this.mSwitchable = false;
        this.mFbSkipSwitch = false;
        this.mHighpToLowpSwitch = false;
        this.mShadowMapSwitch = 0;
        this.mMipMapSwitch = 0;
        this.mBasePackageName = pkgName;
        this.mResolutionRatio = resolutionRatio;
        this.mMinFrameRate = frameRatio;
        this.mTexturePercent = texturePercent;
        this.mBrightnessPercent = brightnessPercent;
        this.mSwitchable = isSwitchable;
        this.mFbSkipSwitch = isEnableFbSkipSwitch;
        this.mHighpToLowpSwitch = isEnableHighpToLowpSwitch;
        this.mShadowMapSwitch = shadowMapSwitch;
        this.mMipMapSwitch = mipMapSwitch;
    }

    public void setBasePackageName(String pkgName) {
        this.mBasePackageName = pkgName;
    }

    public void setResolutionRatio(float resolutionRatio, boolean isEnable) {
        this.mResolutionRatio = resolutionRatio;
        this.mSwitchable = isEnable;
    }

    public void setFps(int frameRate) {
        this.mMinFrameRate = frameRate;
    }

    public void setMaxFps(int frameRate) {
        this.mMaxFrameRate = frameRate;
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
        return this.mMinFrameRate;
    }

    public int getMaxFrameRatio() {
        return this.mMaxFrameRate;
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

    public boolean getFbSkipSwitch() {
        return this.mFbSkipSwitch;
    }

    public void setFbSkipSwitch(boolean isEnable) {
        this.mFbSkipSwitch = isEnable;
    }

    public boolean getHighpToLowpSwitch() {
        return this.mHighpToLowpSwitch;
    }

    public void setHighpToLowpSwitch(boolean isEnable) {
        this.mHighpToLowpSwitch = isEnable;
    }

    public int getShadowMapSwitch() {
        return this.mShadowMapSwitch;
    }

    public void setShadowMapSwitch(int shadowMapSwitch) {
        this.mShadowMapSwitch = shadowMapSwitch;
    }

    public int getMipMapSwitch() {
        return this.mMipMapSwitch;
    }

    public void setMipMapSwitch(int mipMapSwitch) {
        this.mMipMapSwitch = mipMapSwitch;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.mBasePackageName);
        dest.writeFloat(this.mResolutionRatio);
        dest.writeInt(this.mMinFrameRate);
        dest.writeInt(this.mMaxFrameRate);
        dest.writeInt(this.mTexturePercent);
        dest.writeInt(this.mBrightnessPercent);
        dest.writeByte(this.mSwitchable ? (byte) 1 : 0);
    }

    private void readFromParcel(Parcel in) {
        this.mBasePackageName = in.readString();
        this.mResolutionRatio = in.readFloat();
        this.mMinFrameRate = in.readInt();
        this.mMaxFrameRate = in.readInt();
        this.mTexturePercent = in.readInt();
        this.mBrightnessPercent = in.readInt();
        this.mSwitchable = in.readByte() != 0;
    }

    public String toString() {
        return "ApsAppInfo [mBasePackageName=" + this.mBasePackageName + ", mResolutionRatio=" + this.mResolutionRatio + ", mMinFrameRate=" + this.mMinFrameRate + ", mMaxFrameRate=" + this.mMaxFrameRate + ", mTexturePercent=" + this.mTexturePercent + ", mBrightnessPercent=" + this.mBrightnessPercent + ", mSwitchable=" + this.mSwitchable + ", mFbSkipSwitch=" + this.mFbSkipSwitch + ", mHighpToLowpSwitch=" + this.mHighpToLowpSwitch + ", mShadowMapSwitch=" + this.mShadowMapSwitch + ", mMipMapSwitch=" + this.mMipMapSwitch + "]";
    }

    public int hashCode() {
        int i = 1 * 31;
        String str = this.mBasePackageName;
        int result = (((i + (str == null ? 0 : str.hashCode())) * 31) + this.mBrightnessPercent) * 31;
        boolean z = this.mFbSkipSwitch;
        int i2 = MetricsProto.MetricsEvent.AUTOFILL_SERVICE_DISABLED_APP;
        int result2 = (((((((((((((result + (z ? 1231 : 1237)) * 31) + this.mMinFrameRate) * 31) + this.mMaxFrameRate) * 31) + (this.mHighpToLowpSwitch ? 1231 : 1237)) * 31) + this.mMipMapSwitch) * 31) + Float.floatToIntBits(this.mResolutionRatio)) * 31) + this.mShadowMapSwitch) * 31;
        if (!this.mSwitchable) {
            i2 = 1237;
        }
        return ((result2 + i2) * 31) + this.mTexturePercent;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ApsAppInfo other = (ApsAppInfo) obj;
        String str = this.mBasePackageName;
        if (str == null) {
            if (other.mBasePackageName != null) {
                return false;
            }
        } else if (!str.equals(other.mBasePackageName)) {
            return false;
        }
        if (this.mBrightnessPercent == other.mBrightnessPercent && this.mFbSkipSwitch == other.mFbSkipSwitch && this.mMinFrameRate == other.mMinFrameRate && this.mMaxFrameRate == other.mMaxFrameRate && this.mHighpToLowpSwitch == other.mHighpToLowpSwitch && this.mMipMapSwitch == other.mMipMapSwitch && Float.floatToIntBits(this.mResolutionRatio) == Float.floatToIntBits(other.mResolutionRatio) && this.mShadowMapSwitch == other.mShadowMapSwitch && this.mSwitchable == other.mSwitchable && this.mTexturePercent == other.mTexturePercent) {
            return true;
        }
        return false;
    }
}
