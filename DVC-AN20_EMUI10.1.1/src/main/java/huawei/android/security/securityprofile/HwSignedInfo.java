package huawei.android.security.securityprofile;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class HwSignedInfo implements Parcelable {
    public static final Parcelable.Creator<HwSignedInfo> CREATOR = new Parcelable.Creator<HwSignedInfo>() {
        /* class huawei.android.security.securityprofile.HwSignedInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwSignedInfo createFromParcel(Parcel in) {
            return new HwSignedInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwSignedInfo[] newArray(int size) {
            return new HwSignedInfo[size];
        }
    };
    private static final int DEFAULT_PACKAGE_LABEL_CAPACITY = 4;
    public static final int FLAG_PERMISSION_REASON_AUDIT_PERMITTED = 1;
    public static final int FLAG_PERMISSION_REASON_FORCED_RESTRICTED = 2;
    public static final int FLAG_POLICY_APK_DIGEST = 1;
    public static final int FLAG_POLICY_LABELS = 2;
    public static final int FLAG_POLICY_PERMISSION_FLAGS = 4;
    public ApkDigest apkDigest;
    public List<String> labelsList;
    public String packageName;
    public int permissionFlags;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PolicyFlags {
    }

    public HwSignedInfo(String inPackageName) {
        this.packageName = inPackageName;
    }

    private HwSignedInfo(Parcel in) {
        this.packageName = in.readString();
        this.apkDigest = (ApkDigest) in.readTypedObject(ApkDigest.CREATOR);
        if (this.labelsList == null) {
            this.labelsList = new ArrayList(4);
        }
        in.readStringList(this.labelsList);
        this.permissionFlags = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.packageName);
        out.writeTypedObject(this.apkDigest, flags);
        out.writeStringList(this.labelsList);
        out.writeInt(this.permissionFlags);
    }
}
