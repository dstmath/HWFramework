package android.app.mtm.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.rms.iaware.AwareLog;
import java.util.Arrays;

public class HwAppStartupSettingFilter implements Parcelable, Cloneable {
    public static final Creator<HwAppStartupSettingFilter> CREATOR = new Creator<HwAppStartupSettingFilter>() {
        public HwAppStartupSettingFilter createFromParcel(Parcel source) {
            return new HwAppStartupSettingFilter(source, null);
        }

        public HwAppStartupSettingFilter[] newArray(int size) {
            return new HwAppStartupSettingFilter[0];
        }
    };
    private static final String TAG = "HwAppStartupSettingFilter";
    public static final int VAL_ALL = -1;
    private int[] mModifier;
    private int[] mPolicy;
    private int[] mShow;

    /* synthetic */ HwAppStartupSettingFilter(Parcel source, HwAppStartupSettingFilter -this1) {
        this(source);
    }

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
        StringBuilder sb = new StringBuilder();
        sb.append(" policy:").append(Arrays.toString(this.mPolicy));
        sb.append(" modifier:").append(Arrays.toString(this.mModifier));
        sb.append(" show:").append(Arrays.toString(this.mShow));
        return sb.toString();
    }

    protected Object clone() throws CloneNotSupportedException {
        HwAppStartupSettingFilter obj = null;
        try {
            return (HwAppStartupSettingFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            AwareLog.e(TAG, "clone catch exception!");
            return obj;
        }
    }
}
