package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;

public class PackageInfo implements Parcelable {
    public static final Parcelable.Creator<PackageInfo> CREATOR = new Parcelable.Creator<PackageInfo>() {
        /* class android.content.pm.PackageInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PackageInfo createFromParcel(Parcel source) {
            return new PackageInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public PackageInfo[] newArray(int size) {
            return new PackageInfo[size];
        }
    };
    public static final int INSTALL_LOCATION_AUTO = 0;
    public static final int INSTALL_LOCATION_INTERNAL_ONLY = 1;
    public static final int INSTALL_LOCATION_PREFER_EXTERNAL = 2;
    @UnsupportedAppUsage
    public static final int INSTALL_LOCATION_UNSPECIFIED = -1;
    public static final int REQUESTED_PERMISSION_GRANTED = 2;
    public static final int REQUESTED_PERMISSION_REQUIRED = 1;
    public ActivityInfo[] activities;
    public ApplicationInfo applicationInfo;
    public int baseRevisionCode;
    public int compileSdkVersion;
    public String compileSdkVersionCodename;
    public ConfigurationInfo[] configPreferences;
    @UnsupportedAppUsage
    public boolean coreApp;
    public FeatureGroupInfo[] featureGroups;
    public long firstInstallTime;
    public int[] gids;
    public int installLocation;
    public InstrumentationInfo[] instrumentation;
    public boolean isApex;
    public boolean isStub;
    public long lastUpdateTime;
    boolean mOverlayIsStatic;
    public String overlayCategory;
    public int overlayPriority;
    @UnsupportedAppUsage
    public String overlayTarget;
    public String packageName;
    public PermissionInfo[] permissions;
    public ProviderInfo[] providers;
    public ActivityInfo[] receivers;
    public FeatureInfo[] reqFeatures;
    public String[] requestedPermissions;
    public int[] requestedPermissionsFlags;
    public String requiredAccountType;
    public boolean requiredForAllUsers;
    public String restrictedAccountType;
    public ServiceInfo[] services;
    public String sharedUserId;
    public int sharedUserLabel;
    @Deprecated
    public Signature[] signatures;
    public SigningInfo signingInfo;
    public String[] splitNames;
    public int[] splitRevisionCodes;
    public String targetOverlayableName;
    @Deprecated
    public int versionCode;
    public int versionCodeMajor;
    public String versionName;

    public long getLongVersionCode() {
        return composeLongVersionCode(this.versionCodeMajor, this.versionCode);
    }

    public void setLongVersionCode(long longVersionCode) {
        this.versionCodeMajor = (int) (longVersionCode >> 32);
        this.versionCode = (int) longVersionCode;
    }

    public static long composeLongVersionCode(int major, int minor) {
        return (((long) major) << 32) | (((long) minor) & 4294967295L);
    }

    public PackageInfo() {
        this.installLocation = 1;
    }

    public boolean isOverlayPackage() {
        return this.overlayTarget != null;
    }

    public boolean isStaticOverlayPackage() {
        return this.overlayTarget != null && this.mOverlayIsStatic;
    }

    public String toString() {
        return "PackageInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.packageName);
        dest.writeStringArray(this.splitNames);
        dest.writeInt(this.versionCode);
        dest.writeInt(this.versionCodeMajor);
        dest.writeString(this.versionName);
        dest.writeInt(this.baseRevisionCode);
        dest.writeIntArray(this.splitRevisionCodes);
        dest.writeString(this.sharedUserId);
        dest.writeInt(this.sharedUserLabel);
        if (this.applicationInfo != null) {
            dest.writeInt(1);
            this.applicationInfo.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
        dest.writeLong(this.firstInstallTime);
        dest.writeLong(this.lastUpdateTime);
        dest.writeIntArray(this.gids);
        dest.writeTypedArray(this.activities, parcelableFlags | 2);
        dest.writeTypedArray(this.receivers, parcelableFlags | 2);
        dest.writeTypedArray(this.services, parcelableFlags | 2);
        dest.writeTypedArray(this.providers, parcelableFlags | 2);
        dest.writeTypedArray(this.instrumentation, parcelableFlags);
        dest.writeTypedArray(this.permissions, parcelableFlags);
        dest.writeStringArray(this.requestedPermissions);
        dest.writeIntArray(this.requestedPermissionsFlags);
        dest.writeTypedArray(this.signatures, parcelableFlags);
        dest.writeTypedArray(this.configPreferences, parcelableFlags);
        dest.writeTypedArray(this.reqFeatures, parcelableFlags);
        dest.writeTypedArray(this.featureGroups, parcelableFlags);
        dest.writeInt(this.installLocation);
        dest.writeInt(this.isStub ? 1 : 0);
        dest.writeInt(this.coreApp ? 1 : 0);
        dest.writeInt(this.requiredForAllUsers ? 1 : 0);
        dest.writeString(this.restrictedAccountType);
        dest.writeString(this.requiredAccountType);
        dest.writeString(this.overlayTarget);
        dest.writeString(this.overlayCategory);
        dest.writeInt(this.overlayPriority);
        dest.writeBoolean(this.mOverlayIsStatic);
        dest.writeInt(this.compileSdkVersion);
        dest.writeString(this.compileSdkVersionCodename);
        if (this.signingInfo != null) {
            dest.writeInt(1);
            this.signingInfo.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
        dest.writeBoolean(this.isApex);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private PackageInfo(Parcel source) {
        boolean z = true;
        this.installLocation = 1;
        this.packageName = source.readString();
        this.splitNames = source.createStringArray();
        this.versionCode = source.readInt();
        this.versionCodeMajor = source.readInt();
        this.versionName = source.readString();
        this.baseRevisionCode = source.readInt();
        this.splitRevisionCodes = source.createIntArray();
        this.sharedUserId = source.readString();
        this.sharedUserLabel = source.readInt();
        if (source.readInt() != 0) {
            this.applicationInfo = ApplicationInfo.CREATOR.createFromParcel(source);
        }
        this.firstInstallTime = source.readLong();
        this.lastUpdateTime = source.readLong();
        this.gids = source.createIntArray();
        this.activities = (ActivityInfo[]) source.createTypedArray(ActivityInfo.CREATOR);
        this.receivers = (ActivityInfo[]) source.createTypedArray(ActivityInfo.CREATOR);
        this.services = (ServiceInfo[]) source.createTypedArray(ServiceInfo.CREATOR);
        this.providers = (ProviderInfo[]) source.createTypedArray(ProviderInfo.CREATOR);
        this.instrumentation = (InstrumentationInfo[]) source.createTypedArray(InstrumentationInfo.CREATOR);
        this.permissions = (PermissionInfo[]) source.createTypedArray(PermissionInfo.CREATOR);
        this.requestedPermissions = source.createStringArray();
        this.requestedPermissionsFlags = source.createIntArray();
        this.signatures = (Signature[]) source.createTypedArray(Signature.CREATOR);
        this.configPreferences = (ConfigurationInfo[]) source.createTypedArray(ConfigurationInfo.CREATOR);
        this.reqFeatures = (FeatureInfo[]) source.createTypedArray(FeatureInfo.CREATOR);
        this.featureGroups = (FeatureGroupInfo[]) source.createTypedArray(FeatureGroupInfo.CREATOR);
        this.installLocation = source.readInt();
        this.isStub = source.readInt() != 0;
        this.coreApp = source.readInt() != 0;
        this.requiredForAllUsers = source.readInt() == 0 ? false : z;
        this.restrictedAccountType = source.readString();
        this.requiredAccountType = source.readString();
        this.overlayTarget = source.readString();
        this.overlayCategory = source.readString();
        this.overlayPriority = source.readInt();
        this.mOverlayIsStatic = source.readBoolean();
        this.compileSdkVersion = source.readInt();
        this.compileSdkVersionCodename = source.readString();
        if (source.readInt() != 0) {
            this.signingInfo = SigningInfo.CREATOR.createFromParcel(source);
        }
        this.isApex = source.readBoolean();
        ApplicationInfo applicationInfo2 = this.applicationInfo;
        if (applicationInfo2 != null) {
            propagateApplicationInfo(applicationInfo2, this.activities);
            propagateApplicationInfo(this.applicationInfo, this.receivers);
            propagateApplicationInfo(this.applicationInfo, this.services);
            propagateApplicationInfo(this.applicationInfo, this.providers);
        }
    }

    private void propagateApplicationInfo(ApplicationInfo appInfo, ComponentInfo[] components) {
        if (components != null) {
            for (ComponentInfo ci : components) {
                ci.applicationInfo = appInfo;
            }
        }
    }
}
