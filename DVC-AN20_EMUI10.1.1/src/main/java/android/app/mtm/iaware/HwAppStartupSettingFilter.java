package android.app.mtm.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.rms.iaware.AwareLog;
import java.util.Arrays;

public class HwAppStartupSettingFilter implements Parcelable, Cloneable {
    public static final Parcelable.Creator<HwAppStartupSettingFilter> CREATOR = new Parcelable.Creator<HwAppStartupSettingFilter>() {
        /* class android.app.mtm.iaware.HwAppStartupSettingFilter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwAppStartupSettingFilter createFromParcel(Parcel source) {
            return new HwAppStartupSettingFilter(source);
        }

        @Override // android.os.Parcelable.Creator
        public HwAppStartupSettingFilter[] newArray(int size) {
            return new HwAppStartupSettingFilter[0];
        }
    };
    private static final String TAG = "HwAppStartupSettingFilter";
    public static final int VAL_ALL = -1;
    private int[] mModifier;
    private int[] mPolicy;
    private int[] mShow;

    public HwAppStartupSettingFilter() {
        this.mPolicy = new int[]{-1, -1, -1, -1};
        this.mShow = new int[]{-1, -1, -1, -1};
        this.mModifier = new int[]{-1, -1, -1, -1};
    }

    public int[] getPolicy() {
        return this.mPolicy;
    }

    public int[] getModifier() {
        return this.mModifier;
    }

    public int[] getShow() {
        return this.mShow;
    }

    public HwAppStartupSettingFilter setPolicy(int[] policy) {
        if (policy != null && policy.length <= this.mPolicy.length) {
            this.mPolicy = policy;
        }
        return this;
    }

    public HwAppStartupSettingFilter setModifier(int[] modifier) {
        if (modifier != null && modifier.length <= this.mModifier.length) {
            this.mModifier = modifier;
        }
        return this;
    }

    public HwAppStartupSettingFilter setShow(int[] show) {
        if (show != null && show.length <= this.mShow.length) {
            this.mShow = show;
        }
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.mPolicy);
        dest.writeIntArray(this.mShow);
        dest.writeIntArray(this.mModifier);
    }

    private HwAppStartupSettingFilter(Parcel source) {
        this.mPolicy = new int[]{-1, -1, -1, -1};
        this.mShow = new int[]{-1, -1, -1, -1};
        this.mModifier = new int[]{-1, -1, -1, -1};
        source.readIntArray(this.mPolicy);
        source.readIntArray(this.mShow);
        source.readIntArray(this.mModifier);
    }

    public String toString() {
        return " policy:" + Arrays.toString(this.mPolicy) + " modifier:" + Arrays.toString(this.mModifier) + " show:" + Arrays.toString(this.mShow);
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        try {
            return (HwAppStartupSettingFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            AwareLog.e(TAG, "clone catch exception!");
            return null;
        }
    }
}
