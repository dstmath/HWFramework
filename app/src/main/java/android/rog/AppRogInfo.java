package android.rog;

import android.content.res.Configuration;
import android.net.ProxyInfo;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;
import android.util.DisplayMetrics;

public class AppRogInfo implements Parcelable {
    public static final Creator<AppRogInfo> CREATOR = null;
    public static final int ROG_FEATURE_OFF = 0;
    public static final int ROG_POLICY_NORMAL = 2;
    public static final int ROG_POLICY_PERFORMANCE = 1;
    public static final int ROG_RESOLUTION_FHD = 3;
    public static final int ROG_RESOLUTION_HD = 4;
    public static final int ROG_RESOLUTION_QHD = 2;
    public static final int ROG_RESOLUTION_UHD = 1;
    private static final String TAG = "AppRogInfo";
    public String mPackageName;
    public int mRogMode;
    public float mRogScale;
    public boolean mSupportHotSwitch;

    public static final class UpdateRog {
        public String packageName;
        public boolean rogEnable;
        public AppRogInfo rogInfo;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rog.AppRogInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rog.AppRogInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.rog.AppRogInfo.<clinit>():void");
    }

    public AppRogInfo() {
        this.mPackageName = ProxyInfo.LOCAL_EXCL_LIST;
    }

    public AppRogInfo(Parcel parcel) {
        this.mPackageName = ProxyInfo.LOCAL_EXCL_LIST;
        readFromParcel(parcel);
    }

    public AppRogInfo(AppRogInfo another) {
        this.mPackageName = ProxyInfo.LOCAL_EXCL_LIST;
        this.mPackageName = another.mPackageName;
        this.mRogMode = another.mRogMode;
        this.mRogScale = another.mRogScale;
        this.mSupportHotSwitch = another.mSupportHotSwitch;
    }

    public int describeContents() {
        return ROG_FEATURE_OFF;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mRogMode);
        dest.writeFloat(this.mRogScale);
        dest.writeInt(this.mSupportHotSwitch ? ROG_RESOLUTION_UHD : ROG_FEATURE_OFF);
    }

    public void readFromParcel(Parcel in) {
        boolean z = false;
        this.mPackageName = in.readString();
        this.mRogMode = in.readInt();
        this.mRogScale = in.readFloat();
        if (in.readInt() > 0) {
            z = true;
        }
        this.mSupportHotSwitch = z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name:").append(this.mPackageName).append(",mode:").append(this.mRogMode).append(", scale:").append(this.mRogScale).append(", SupportHotSwitch:").append(this.mSupportHotSwitch);
        return sb.toString();
    }

    public float getRogAppSclae() {
        return this.mRogScale;
    }

    public boolean isRogEnable() {
        return this.mRogMode != 0;
    }

    public boolean isSupportHotSwitch() {
        return this.mSupportHotSwitch;
    }

    public boolean equals(Object another) {
        boolean z = false;
        if (!(another instanceof AppRogInfo)) {
            return false;
        }
        AppRogInfo anotherCopy = (AppRogInfo) another;
        if (anotherCopy.mPackageName.equalsIgnoreCase(this.mPackageName) && anotherCopy.mRogMode == this.mRogMode && Float.compare(anotherCopy.mRogScale, this.mRogScale) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return ((((this.mRogMode + 527) * 31) + Float.floatToIntBits(this.mRogScale)) * 31) + this.mPackageName.hashCode();
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm, boolean rogEnable) {
        if (Float.compare(this.mRogScale, Engine.DEFAULT_VOLUME) != 0) {
            if (rogEnable) {
                inoutDm.densityDpi = (int) ((((float) inoutDm.noncompatDensityDpi) / this.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
                inoutDm.density = ((float) inoutDm.densityDpi) * 0.00625f;
                inoutDm.scaledDensity = inoutDm.noncompatScaledDensity / this.mRogScale;
            } else {
                inoutDm.densityDpi = inoutDm.noncompatDensityDpi;
                inoutDm.density = ((float) inoutDm.densityDpi) * 0.00625f;
                inoutDm.scaledDensity = inoutDm.noncompatScaledDensity;
            }
        }
    }

    public void applyToConfiguration(DisplayMetrics inoutDm, Configuration inoutConfig) {
        inoutConfig.screenLayout = (inoutConfig.screenLayout & -16) | ROG_RESOLUTION_QHD;
        inoutConfig.densityDpi = inoutDm.densityDpi;
    }

    public void getRealSizeDisplayMetrics(DisplayMetrics inoutDm, boolean rogEnable) {
        if (Float.compare(this.mRogScale, Engine.DEFAULT_VOLUME) != 0) {
            if (rogEnable) {
                inoutDm.xdpi = inoutDm.noncompatXdpi / this.mRogScale;
                inoutDm.ydpi = inoutDm.noncompatYdpi / this.mRogScale;
                inoutDm.widthPixels = (int) ((((float) inoutDm.noncompatWidthPixels) / this.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
                inoutDm.heightPixels = (int) ((((float) inoutDm.noncompatHeightPixels) / this.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            } else {
                inoutDm.xdpi = inoutDm.noncompatXdpi;
                inoutDm.ydpi = inoutDm.noncompatYdpi;
                inoutDm.widthPixels = inoutDm.noncompatWidthPixels;
                inoutDm.heightPixels = inoutDm.noncompatHeightPixels;
            }
        }
    }
}
