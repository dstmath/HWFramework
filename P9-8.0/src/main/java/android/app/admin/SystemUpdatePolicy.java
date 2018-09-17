package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class SystemUpdatePolicy implements Parcelable {
    public static final Creator<SystemUpdatePolicy> CREATOR = new Creator<SystemUpdatePolicy>() {
        public SystemUpdatePolicy createFromParcel(Parcel source) {
            SystemUpdatePolicy policy = new SystemUpdatePolicy();
            policy.mPolicyType = source.readInt();
            policy.mMaintenanceWindowStart = source.readInt();
            policy.mMaintenanceWindowEnd = source.readInt();
            return policy;
        }

        public SystemUpdatePolicy[] newArray(int size) {
            return new SystemUpdatePolicy[size];
        }
    };
    private static final String KEY_INSTALL_WINDOW_END = "install_window_end";
    private static final String KEY_INSTALL_WINDOW_START = "install_window_start";
    private static final String KEY_POLICY_TYPE = "policy_type";
    public static final int TYPE_INSTALL_AUTOMATIC = 1;
    public static final int TYPE_INSTALL_WINDOWED = 2;
    public static final int TYPE_POSTPONE = 3;
    private static final int TYPE_UNKNOWN = -1;
    private static final int WINDOW_BOUNDARY = 1440;
    private int mMaintenanceWindowEnd;
    private int mMaintenanceWindowStart;
    private int mPolicyType;

    /* synthetic */ SystemUpdatePolicy(SystemUpdatePolicy -this0) {
        this();
    }

    private SystemUpdatePolicy() {
        this.mPolicyType = -1;
    }

    public static SystemUpdatePolicy createAutomaticInstallPolicy() {
        SystemUpdatePolicy policy = new SystemUpdatePolicy();
        policy.mPolicyType = 1;
        return policy;
    }

    public static SystemUpdatePolicy createWindowedInstallPolicy(int startTime, int endTime) {
        if (startTime < 0 || startTime >= WINDOW_BOUNDARY || endTime < 0 || endTime >= WINDOW_BOUNDARY) {
            throw new IllegalArgumentException("startTime and endTime must be inside [0, 1440)");
        }
        SystemUpdatePolicy policy = new SystemUpdatePolicy();
        policy.mPolicyType = 2;
        policy.mMaintenanceWindowStart = startTime;
        policy.mMaintenanceWindowEnd = endTime;
        return policy;
    }

    public static SystemUpdatePolicy createPostponeInstallPolicy() {
        SystemUpdatePolicy policy = new SystemUpdatePolicy();
        policy.mPolicyType = 3;
        return policy;
    }

    public int getPolicyType() {
        return this.mPolicyType;
    }

    public int getInstallWindowStart() {
        if (this.mPolicyType == 2) {
            return this.mMaintenanceWindowStart;
        }
        return -1;
    }

    public int getInstallWindowEnd() {
        if (this.mPolicyType == 2) {
            return this.mMaintenanceWindowEnd;
        }
        return -1;
    }

    public boolean isValid() {
        boolean z = true;
        if (this.mPolicyType == 1 || this.mPolicyType == 3) {
            return true;
        }
        if (this.mPolicyType != 2) {
            return false;
        }
        if (this.mMaintenanceWindowStart < 0 || this.mMaintenanceWindowStart >= WINDOW_BOUNDARY || this.mMaintenanceWindowEnd < 0) {
            z = false;
        } else if (this.mMaintenanceWindowEnd >= WINDOW_BOUNDARY) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return String.format("SystemUpdatePolicy (type: %d, windowStart: %d, windowEnd: %d)", new Object[]{Integer.valueOf(this.mPolicyType), Integer.valueOf(this.mMaintenanceWindowStart), Integer.valueOf(this.mMaintenanceWindowEnd)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPolicyType);
        dest.writeInt(this.mMaintenanceWindowStart);
        dest.writeInt(this.mMaintenanceWindowEnd);
    }

    public static SystemUpdatePolicy restoreFromXml(XmlPullParser parser) {
        try {
            SystemUpdatePolicy policy = new SystemUpdatePolicy();
            String value = parser.getAttributeValue(null, KEY_POLICY_TYPE);
            if (value != null) {
                policy.mPolicyType = Integer.parseInt(value);
                value = parser.getAttributeValue(null, KEY_INSTALL_WINDOW_START);
                if (value != null) {
                    policy.mMaintenanceWindowStart = Integer.parseInt(value);
                }
                value = parser.getAttributeValue(null, KEY_INSTALL_WINDOW_END);
                if (value != null) {
                    policy.mMaintenanceWindowEnd = Integer.parseInt(value);
                }
                return policy;
            }
        } catch (NumberFormatException e) {
        }
        return null;
    }

    public void saveToXml(XmlSerializer out) throws IOException {
        out.attribute(null, KEY_POLICY_TYPE, Integer.toString(this.mPolicyType));
        out.attribute(null, KEY_INSTALL_WINDOW_START, Integer.toString(this.mMaintenanceWindowStart));
        out.attribute(null, KEY_INSTALL_WINDOW_END, Integer.toString(this.mMaintenanceWindowEnd));
    }
}
