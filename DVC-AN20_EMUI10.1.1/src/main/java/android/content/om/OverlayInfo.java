package android.content.om;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@SystemApi
public final class OverlayInfo implements Parcelable {
    public static final String CATEGORY_THEME = "android.theme";
    public static final Parcelable.Creator<OverlayInfo> CREATOR = new Parcelable.Creator<OverlayInfo>() {
        /* class android.content.om.OverlayInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OverlayInfo createFromParcel(Parcel source) {
            return new OverlayInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public OverlayInfo[] newArray(int size) {
            return new OverlayInfo[size];
        }
    };
    public static final int STATE_DISABLED = 2;
    public static final int STATE_ENABLED = 3;
    public static final int STATE_ENABLED_STATIC = 6;
    public static final int STATE_MISSING_TARGET = 0;
    public static final int STATE_NO_IDMAP = 1;
    public static final int STATE_OVERLAY_IS_BEING_REPLACED = 5;
    @Deprecated
    public static final int STATE_TARGET_IS_BEING_REPLACED = 4;
    public static final int STATE_UNKNOWN = -1;
    public final String baseCodePath;
    public final String category;
    public final boolean isStatic;
    public final String packageName;
    public final int priority;
    @UnsupportedAppUsage
    public final int state;
    public final String targetOverlayableName;
    public final String targetPackageName;
    public final int userId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public OverlayInfo(OverlayInfo source, int state2) {
        this(source.packageName, source.targetPackageName, source.targetOverlayableName, source.category, source.baseCodePath, state2, source.userId, source.priority, source.isStatic);
    }

    public OverlayInfo(String packageName2, String targetPackageName2, String targetOverlayableName2, String category2, String baseCodePath2, int state2, int userId2, int priority2, boolean isStatic2) {
        this.packageName = packageName2;
        this.targetPackageName = targetPackageName2;
        this.targetOverlayableName = targetOverlayableName2;
        this.category = category2;
        this.baseCodePath = baseCodePath2;
        this.state = state2;
        this.userId = userId2;
        this.priority = priority2;
        this.isStatic = isStatic2;
        ensureValidState();
    }

    public OverlayInfo(Parcel source) {
        this.packageName = source.readString();
        this.targetPackageName = source.readString();
        this.targetOverlayableName = source.readString();
        this.category = source.readString();
        this.baseCodePath = source.readString();
        this.state = source.readInt();
        this.userId = source.readInt();
        this.priority = source.readInt();
        this.isStatic = source.readBoolean();
        ensureValidState();
    }

    @SystemApi
    public String getPackageName() {
        return this.packageName;
    }

    @SystemApi
    public String getTargetPackageName() {
        return this.targetPackageName;
    }

    @SystemApi
    public String getCategory() {
        return this.category;
    }

    @SystemApi
    public int getUserId() {
        return this.userId;
    }

    @SystemApi
    public String getTargetOverlayableName() {
        return this.targetOverlayableName;
    }

    private void ensureValidState() {
        if (this.packageName == null) {
            throw new IllegalArgumentException("packageName must not be null");
        } else if (this.targetPackageName == null) {
            throw new IllegalArgumentException("targetPackageName must not be null");
        } else if (this.baseCodePath != null) {
            switch (this.state) {
                case -1:
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    return;
                default:
                    throw new IllegalArgumentException("State " + this.state + " is not a valid state");
            }
        } else {
            throw new IllegalArgumentException("baseCodePath must not be null");
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.targetPackageName);
        dest.writeString(this.targetOverlayableName);
        dest.writeString(this.category);
        dest.writeString(this.baseCodePath);
        dest.writeInt(this.state);
        dest.writeInt(this.userId);
        dest.writeInt(this.priority);
        dest.writeBoolean(this.isStatic);
    }

    @SystemApi
    public boolean isEnabled() {
        int i = this.state;
        if (i == 3 || i == 6) {
            return true;
        }
        return false;
    }

    public static String stateToString(int state2) {
        switch (state2) {
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
            case 4:
                return "STATE_TARGET_IS_BEING_REPLACED";
            case 5:
                return "STATE_OVERLAY_IS_BEING_REPLACED";
            case 6:
                return "STATE_ENABLED_STATIC";
            default:
                return "<unknown state>";
        }
    }

    public int hashCode() {
        int i;
        int result = ((((1 * 31) + this.userId) * 31) + this.state) * 31;
        String str = this.packageName;
        int i2 = 0;
        int result2 = (result + (str == null ? 0 : str.hashCode())) * 31;
        String str2 = this.targetPackageName;
        int result3 = (result2 + (str2 == null ? 0 : str2.hashCode())) * 31;
        String str3 = this.targetOverlayableName;
        if (str3 == null) {
            i = 0;
        } else {
            i = str3.hashCode();
        }
        int result4 = (result3 + i) * 31;
        String str4 = this.category;
        int result5 = (result4 + (str4 == null ? 0 : str4.hashCode())) * 31;
        String str5 = this.baseCodePath;
        if (str5 != null) {
            i2 = str5.hashCode();
        }
        return result5 + i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OverlayInfo other = (OverlayInfo) obj;
        if (this.userId == other.userId && this.state == other.state && this.packageName.equals(other.packageName) && this.targetPackageName.equals(other.targetPackageName) && Objects.equals(this.targetOverlayableName, other.targetOverlayableName) && Objects.equals(this.category, other.category) && this.baseCodePath.equals(other.baseCodePath)) {
            return true;
        }
        return false;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("OverlayInfo { overlay=");
        sb.append(this.packageName);
        sb.append(", targetPackage=");
        sb.append(this.targetPackageName);
        if (this.targetOverlayableName == null) {
            str = "";
        } else {
            str = ", targetOverlayable=" + this.targetOverlayableName;
        }
        sb.append(str);
        sb.append(", state=");
        sb.append(this.state);
        sb.append(" (");
        sb.append(stateToString(this.state));
        sb.append("), userId=");
        sb.append(this.userId);
        sb.append(" }");
        return sb.toString();
    }
}
