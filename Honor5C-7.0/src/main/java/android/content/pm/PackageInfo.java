package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PackageInfo implements Parcelable {
    public static final Creator<PackageInfo> CREATOR = null;
    public static final int INSTALL_LOCATION_AUTO = 0;
    public static final int INSTALL_LOCATION_INTERNAL_ONLY = 1;
    public static final int INSTALL_LOCATION_PREFER_EXTERNAL = 2;
    public static final int INSTALL_LOCATION_UNSPECIFIED = -1;
    public static final int REQUESTED_PERMISSION_GRANTED = 2;
    public static final int REQUESTED_PERMISSION_REQUIRED = 1;
    public ActivityInfo[] activities;
    public ApplicationInfo applicationInfo;
    public int baseRevisionCode;
    public ConfigurationInfo[] configPreferences;
    public boolean coreApp;
    public FeatureGroupInfo[] featureGroups;
    public long firstInstallTime;
    public int[] gids;
    public int installLocation;
    public InstrumentationInfo[] instrumentation;
    public long lastUpdateTime;
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
    public Signature[] signatures;
    public String[] splitNames;
    public int[] splitRevisionCodes;
    public int versionCode;
    public String versionName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PackageInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageInfo.<clinit>():void");
    }

    public PackageInfo() {
        this.installLocation = REQUESTED_PERMISSION_REQUIRED;
    }

    public String toString() {
        return "PackageInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    public int describeContents() {
        return INSTALL_LOCATION_AUTO;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i = REQUESTED_PERMISSION_REQUIRED;
        dest.writeString(this.packageName);
        dest.writeStringArray(this.splitNames);
        dest.writeInt(this.versionCode);
        dest.writeString(this.versionName);
        dest.writeInt(this.baseRevisionCode);
        dest.writeIntArray(this.splitRevisionCodes);
        dest.writeString(this.sharedUserId);
        dest.writeInt(this.sharedUserLabel);
        if (this.applicationInfo != null) {
            dest.writeInt(REQUESTED_PERMISSION_REQUIRED);
            this.applicationInfo.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(INSTALL_LOCATION_AUTO);
        }
        dest.writeLong(this.firstInstallTime);
        dest.writeLong(this.lastUpdateTime);
        dest.writeIntArray(this.gids);
        dest.writeTypedArray(this.activities, parcelableFlags | REQUESTED_PERMISSION_GRANTED);
        dest.writeTypedArray(this.receivers, parcelableFlags | REQUESTED_PERMISSION_GRANTED);
        dest.writeTypedArray(this.services, parcelableFlags | REQUESTED_PERMISSION_GRANTED);
        dest.writeTypedArray(this.providers, parcelableFlags | REQUESTED_PERMISSION_GRANTED);
        dest.writeTypedArray(this.instrumentation, parcelableFlags);
        dest.writeTypedArray(this.permissions, parcelableFlags);
        dest.writeStringArray(this.requestedPermissions);
        dest.writeIntArray(this.requestedPermissionsFlags);
        dest.writeTypedArray(this.signatures, parcelableFlags);
        dest.writeTypedArray(this.configPreferences, parcelableFlags);
        dest.writeTypedArray(this.reqFeatures, parcelableFlags);
        dest.writeTypedArray(this.featureGroups, parcelableFlags);
        dest.writeInt(this.installLocation);
        dest.writeInt(this.coreApp ? REQUESTED_PERMISSION_REQUIRED : INSTALL_LOCATION_AUTO);
        if (!this.requiredForAllUsers) {
            i = INSTALL_LOCATION_AUTO;
        }
        dest.writeInt(i);
        dest.writeString(this.restrictedAccountType);
        dest.writeString(this.requiredAccountType);
        dest.writeString(this.overlayTarget);
    }

    private PackageInfo(Parcel source) {
        boolean z = true;
        this.installLocation = REQUESTED_PERMISSION_REQUIRED;
        this.packageName = source.readString();
        this.splitNames = source.createStringArray();
        this.versionCode = source.readInt();
        this.versionName = source.readString();
        this.baseRevisionCode = source.readInt();
        this.splitRevisionCodes = source.createIntArray();
        this.sharedUserId = source.readString();
        this.sharedUserLabel = source.readInt();
        if (source.readInt() != 0) {
            this.applicationInfo = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(source);
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
        this.coreApp = source.readInt() != 0;
        if (source.readInt() == 0) {
            z = false;
        }
        this.requiredForAllUsers = z;
        this.restrictedAccountType = source.readString();
        this.requiredAccountType = source.readString();
        this.overlayTarget = source.readString();
        if (this.applicationInfo != null) {
            propagateApplicationInfo(this.applicationInfo, this.activities);
            propagateApplicationInfo(this.applicationInfo, this.receivers);
            propagateApplicationInfo(this.applicationInfo, this.services);
            propagateApplicationInfo(this.applicationInfo, this.providers);
        }
    }

    private void propagateApplicationInfo(ApplicationInfo appInfo, ComponentInfo[] components) {
        if (components != null) {
            int length = components.length;
            for (int i = INSTALL_LOCATION_AUTO; i < length; i += REQUESTED_PERMISSION_REQUIRED) {
                components[i].applicationInfo = appInfo;
            }
        }
    }
}
