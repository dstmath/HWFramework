package ohos.data.searchimpl.model;

import android.os.Parcel;
import android.os.Parcelable;

public class InnerSearchableEntity implements Parcelable {
    public static final Parcelable.Creator<InnerSearchableEntity> CREATOR = new Parcelable.Creator<InnerSearchableEntity>() {
        /* class ohos.data.searchimpl.model.InnerSearchableEntity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InnerSearchableEntity[] newArray(int i) {
            return new InnerSearchableEntity[0];
        }

        @Override // android.os.Parcelable.Creator
        public InnerSearchableEntity createFromParcel(Parcel parcel) {
            return new InnerSearchableEntity(parcel);
        }
    };
    private String appId;
    private String bundleName;
    private String componentName;
    private String intentAction;
    private boolean isAllowGlobalSearch;
    private String permission;
    private int versionCode;
    private String versionName;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public InnerSearchableEntity(String str, String str2, String str3, String str4, String str5, boolean z, int i, String str6) {
        this.bundleName = str;
        this.appId = str2;
        this.permission = str3;
        this.intentAction = str4;
        this.componentName = str5;
        this.isAllowGlobalSearch = z;
        this.versionCode = i;
        this.versionName = str6;
    }

    private InnerSearchableEntity(Parcel parcel) {
        this.bundleName = parcel.readString();
        this.appId = parcel.readString();
        this.permission = parcel.readString();
        this.intentAction = parcel.readString();
        this.componentName = parcel.readString();
        this.isAllowGlobalSearch = parcel.readInt() != 0;
        this.versionCode = parcel.readInt();
        this.versionName = parcel.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.bundleName);
        parcel.writeString(this.appId);
        parcel.writeString(this.permission);
        parcel.writeString(this.intentAction);
        parcel.writeString(this.componentName);
        parcel.writeInt(this.isAllowGlobalSearch ? (byte) 1 : 0);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.versionName);
    }

    @Override // java.lang.Object
    public String toString() {
        return "InnerSearchableEntity{" + this.bundleName + "," + this.appId + "," + this.permission + "," + this.intentAction + "," + this.componentName + "," + this.isAllowGlobalSearch + "," + this.versionCode + "," + this.versionName + "}";
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getAppId() {
        return this.appId;
    }

    public String getPermission() {
        return this.permission;
    }

    public String getIntentAction() {
        return this.intentAction;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public boolean isAllowGlobalSearch() {
        return this.isAllowGlobalSearch;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public String getVersionName() {
        return this.versionName;
    }
}
