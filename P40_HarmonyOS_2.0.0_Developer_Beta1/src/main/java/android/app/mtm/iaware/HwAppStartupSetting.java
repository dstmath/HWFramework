package android.app.mtm.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.huawei.annotation.HwSystemApi;
import java.util.Arrays;

@HwSystemApi
public class HwAppStartupSetting implements Parcelable, Cloneable {
    public static final int AS_MAX_NUM = 4;
    public static final int AS_MODIFIER_CUST = 2;
    public static final int AS_MODIFIER_DEF = 0;
    public static final int AS_MODIFIER_USER = 1;
    public static final int AS_OFF = 0;
    public static final int AS_ON = 1;
    public static final int AS_TP_ALV = 3;
    public static final int AS_TP_ASS = 2;
    public static final int AS_TP_MDF = 0;
    public static final int AS_TP_SHW = 0;
    public static final int AS_TP_SLF = 1;
    public static final int AS_TP_SMT = 0;
    public static final int AS_UNSET = -1;
    public static final Parcelable.Creator<HwAppStartupSetting> CREATOR = new Parcelable.Creator<HwAppStartupSetting>() {
        /* class android.app.mtm.iaware.HwAppStartupSetting.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwAppStartupSetting createFromParcel(Parcel source) {
            return new HwAppStartupSetting(source);
        }

        @Override // android.os.Parcelable.Creator
        public HwAppStartupSetting[] newArray(int size) {
            return new HwAppStartupSetting[size];
        }
    };
    private static final String TAG = "HwAppStartupSetting";
    private int[] mModifier;
    private String mPackageName;
    private int[] mPolicy;
    private int[] mShow;

    public HwAppStartupSetting(String packageName, int[] policy, int[] modifier, int[] show) {
        this.mPolicy = new int[]{-1, -1, -1, -1};
        this.mShow = new int[]{-1, -1, -1, -1};
        this.mModifier = new int[]{-1, -1, -1, -1};
        this.mPackageName = packageName;
        if (policy != null && policy.length == 4) {
            System.arraycopy(policy, 0, this.mPolicy, 0, 4);
        }
        if (show != null && show.length == 4) {
            System.arraycopy(show, 0, this.mShow, 0, 4);
        }
        if (modifier != null && modifier.length == 4) {
            System.arraycopy(modifier, 0, this.mModifier, 0, 4);
        }
    }

    public HwAppStartupSetting(HwAppStartupSetting setting) {
        this.mPolicy = new int[]{-1, -1, -1, -1};
        this.mShow = new int[]{-1, -1, -1, -1};
        this.mModifier = new int[]{-1, -1, -1, -1};
        if (setting != null) {
            this.mPackageName = setting.mPackageName;
            int[] iArr = setting.mPolicy;
            if (iArr.length == 4) {
                System.arraycopy(iArr, 0, this.mPolicy, 0, 4);
            }
            int[] iArr2 = setting.mShow;
            if (iArr2.length == 4) {
                System.arraycopy(iArr2, 0, this.mShow, 0, 4);
            }
            int[] iArr3 = setting.mModifier;
            if (iArr3.length == 4) {
                System.arraycopy(iArr3, 0, this.mModifier, 0, 4);
            }
        }
    }

    public HwAppStartupSetting copyValidInfo(HwAppStartupSetting setting) {
        if (setting != null && setting.isValid(true)) {
            this.mPackageName = setting.mPackageName;
            for (int i = 0; i < 4; i++) {
                if (setting.getPolicy(i) != -1) {
                    this.mPolicy[i] = setting.getPolicy(i);
                }
                if (setting.getShow(i) != -1) {
                    this.mShow[i] = setting.getShow(i);
                }
                if (setting.getModifier(i) != -1) {
                    this.mModifier[i] = setting.getModifier(i);
                }
            }
        }
        return this;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getPolicy(int type) {
        if (type < 0) {
            return -1;
        }
        int[] iArr = this.mPolicy;
        if (type >= iArr.length) {
            return -1;
        }
        return iArr[type];
    }

    public int getModifier(int type) {
        if (type < 0) {
            return -1;
        }
        int[] iArr = this.mModifier;
        if (type >= iArr.length) {
            return -1;
        }
        return iArr[type];
    }

    public int getShow(int type) {
        if (type < 0) {
            return -1;
        }
        int[] iArr = this.mShow;
        if (type >= iArr.length) {
            return -1;
        }
        return iArr[type];
    }

    public boolean valid() {
        return isValid(false);
    }

    private boolean isValid(boolean checkUnset) {
        if (!isParaValid()) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            int[] iArr = this.mPolicy;
            if (!(iArr[i] == 0 || iArr[i] == 1 || (checkUnset && iArr[i] == -1))) {
                return false;
            }
        }
        for (int i2 = 0; i2 < 4; i2++) {
            int[] iArr2 = this.mShow;
            if (!(iArr2[i2] == 0 || iArr2[i2] == 1 || (checkUnset && iArr2[i2] == -1))) {
                return false;
            }
        }
        for (int i3 = 0; i3 < 4; i3++) {
            int[] iArr3 = this.mModifier;
            if (!(iArr3[i3] == 0 || iArr3[i3] == 1 || iArr3[i3] == 2 || (checkUnset && iArr3[i3] == -1))) {
                return false;
            }
        }
        return true;
    }

    private boolean isParaValid() {
        int[] iArr;
        int[] iArr2;
        if (!TextUtils.isEmpty(this.mPackageName) && (iArr = this.mPolicy) != null && iArr.length == 4 && (iArr2 = this.mShow) != null && iArr2.length == 4) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeIntArray(this.mPolicy);
        dest.writeIntArray(this.mShow);
        dest.writeIntArray(this.mModifier);
    }

    private HwAppStartupSetting(Parcel source) {
        this.mPolicy = new int[]{-1, -1, -1, -1};
        this.mShow = new int[]{-1, -1, -1, -1};
        this.mModifier = new int[]{-1, -1, -1, -1};
        this.mPackageName = source.readString();
        source.readIntArray(this.mPolicy);
        source.readIntArray(this.mShow);
        source.readIntArray(this.mModifier);
    }

    @Override // java.lang.Object
    public String toString() {
        return "{" + this.mPackageName + " policy:" + Arrays.toString(this.mPolicy) + " modifier:" + Arrays.toString(this.mModifier) + " show:" + Arrays.toString(this.mShow) + "}";
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        try {
            Object tempObj = super.clone();
            if (tempObj instanceof HwAppStartupSetting) {
                return (HwAppStartupSetting) tempObj;
            }
            return null;
        } catch (CloneNotSupportedException e) {
            AwareLog.e(TAG, "clone catch exception!");
            return null;
        }
    }
}
