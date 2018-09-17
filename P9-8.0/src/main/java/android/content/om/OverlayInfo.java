package android.content.om;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class OverlayInfo implements Parcelable {
    public static final Creator<OverlayInfo> CREATOR = new Creator<OverlayInfo>() {
        public OverlayInfo createFromParcel(Parcel source) {
            return new OverlayInfo(source);
        }

        public OverlayInfo[] newArray(int size) {
            return new OverlayInfo[size];
        }
    };
    public static final int STATE_DISABLED = 2;
    public static final int STATE_ENABLED = 3;
    public static final int STATE_MISSING_TARGET = 0;
    public static final int STATE_NO_IDMAP = 1;
    public static final int STATE_UNKNOWN = -1;
    public final String baseCodePath;
    public final String packageName;
    public final int state;
    public final String targetPackageName;
    public final int userId;

    public OverlayInfo(OverlayInfo source, int state) {
        this(source.packageName, source.targetPackageName, source.baseCodePath, state, source.userId);
    }

    public OverlayInfo(String packageName, String targetPackageName, String baseCodePath, int state, int userId) {
        this.packageName = packageName;
        this.targetPackageName = targetPackageName;
        this.baseCodePath = baseCodePath;
        this.state = state;
        this.userId = userId;
        ensureValidState();
    }

    public OverlayInfo(Parcel source) {
        this.packageName = source.readString();
        this.targetPackageName = source.readString();
        this.baseCodePath = source.readString();
        this.state = source.readInt();
        this.userId = source.readInt();
        ensureValidState();
    }

    private void ensureValidState() {
        if (this.packageName == null) {
            throw new IllegalArgumentException("packageName must not be null");
        } else if (this.targetPackageName == null) {
            throw new IllegalArgumentException("targetPackageName must not be null");
        } else if (this.baseCodePath == null) {
            throw new IllegalArgumentException("baseCodePath must not be null");
        } else {
            switch (this.state) {
                case -1:
                case 0:
                case 1:
                case 2:
                case 3:
                    return;
                default:
                    throw new IllegalArgumentException("State " + this.state + " is not a valid state");
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.targetPackageName);
        dest.writeString(this.baseCodePath);
        dest.writeInt(this.state);
        dest.writeInt(this.userId);
    }

    public boolean isEnabled() {
        switch (this.state) {
            case 3:
                return true;
            default:
                return false;
        }
    }

    public static String stateToString(int state) {
        switch (state) {
            case -1:
                return "STATE_UNKNOWN";
            case 0:
                return "STATE_MISSING_TARGET";
            case 1:
                return "STATE_NO_IDMAP";
            case 2:
                return "STATE_DISABLED";
            case 3:
                return "STATE_ENABLED";
            default:
                return "<unknown state>";
        }
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((this.userId + 31) * 31) + this.state) * 31) + (this.packageName == null ? 0 : this.packageName.hashCode())) * 31) + (this.targetPackageName == null ? 0 : this.targetPackageName.hashCode())) * 31;
        if (this.baseCodePath != null) {
            i = this.baseCodePath.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OverlayInfo other = (OverlayInfo) obj;
        return this.userId == other.userId && this.state == other.state && this.packageName.equals(other.packageName) && this.targetPackageName.equals(other.targetPackageName) && this.baseCodePath.equals(other.baseCodePath);
    }

    public String toString() {
        return "OverlayInfo { overlay=" + this.packageName + ", target=" + this.targetPackageName + ", state=" + this.state + " (" + stateToString(this.state) + "), userId=" + this.userId + " }";
    }
}
