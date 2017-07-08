package android.content.pm;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.Printer;
import com.android.internal.util.ArrayUtils;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class ApplicationInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<ApplicationInfo> CREATOR = null;
    public static final int FLAG_ALLOW_BACKUP = 32768;
    public static final int FLAG_ALLOW_CLEAR_USER_DATA = 64;
    public static final int FLAG_ALLOW_TASK_REPARENTING = 32;
    public static final int FLAG_DEBUGGABLE = 2;
    public static final int FLAG_EXTERNAL_STORAGE = 262144;
    public static final int FLAG_EXTRACT_NATIVE_LIBS = 268435456;
    public static final int FLAG_FACTORY_TEST = 16;
    public static final int FLAG_FULL_BACKUP_ONLY = 67108864;
    public static final int FLAG_HARDWARE_ACCELERATED = 536870912;
    public static final int FLAG_HAS_CODE = 4;
    public static final int FLAG_INSTALLED = 8388608;
    public static final int FLAG_IS_DATA_ONLY = 16777216;
    public static final int FLAG_IS_GAME = 33554432;
    public static final int FLAG_KILL_AFTER_RESTORE = 65536;
    public static final int FLAG_LARGE_HEAP = 1048576;
    public static final int FLAG_MULTIARCH = Integer.MIN_VALUE;
    public static final int FLAG_PERSISTENT = 8;
    public static final int FLAG_RESIZEABLE_FOR_SCREENS = 4096;
    public static final int FLAG_RESTORE_ANY_VERSION = 131072;
    public static final int FLAG_STOPPED = 2097152;
    public static final int FLAG_SUPPORTS_LARGE_SCREENS = 2048;
    public static final int FLAG_SUPPORTS_NORMAL_SCREENS = 1024;
    public static final int FLAG_SUPPORTS_RTL = 4194304;
    public static final int FLAG_SUPPORTS_SCREEN_DENSITIES = 8192;
    public static final int FLAG_SUPPORTS_SMALL_SCREENS = 512;
    public static final int FLAG_SUPPORTS_XLARGE_SCREENS = 524288;
    public static final int FLAG_SUSPENDED = 1073741824;
    public static final int FLAG_SYSTEM = 1;
    public static final int FLAG_TEST_ONLY = 256;
    public static final int FLAG_UPDATED_SYSTEM_APP = 128;
    public static final int FLAG_USES_CLEARTEXT_TRAFFIC = 134217728;
    public static final int FLAG_VM_SAFE_MODE = 16384;
    public static final int PRIVATE_FLAG_AUTOPLAY = 128;
    public static final int PRIVATE_FLAG_BACKUP_IN_FOREGROUND = 4096;
    public static final int PRIVATE_FLAG_CANT_SAVE_STATE = 2;
    public static final int PRIVATE_FLAG_DEFAULT_TO_DEVICE_PROTECTED_STORAGE = 32;
    public static final int PRIVATE_FLAG_DIRECT_BOOT_AWARE = 64;
    public static final int PRIVATE_FLAG_EPHEMERAL = 512;
    public static final int PRIVATE_FLAG_FORWARD_LOCK = 4;
    public static final int PRIVATE_FLAG_HAS_DOMAIN_URLS = 16;
    public static final int PRIVATE_FLAG_HIDDEN = 1;
    public static final int PRIVATE_FLAG_PARTIALLY_DIRECT_BOOT_AWARE = 256;
    public static final int PRIVATE_FLAG_PRIVILEGED = 8;
    public static final int PRIVATE_FLAG_REQUIRED_FOR_SYSTEM_USER = 1024;
    public static final int PRIVATE_FLAG_RESIZEABLE_ACTIVITIES = 2048;
    public String backupAgentName;
    public String className;
    public int compatibleWidthLimitDp;
    @Deprecated
    public String credentialEncryptedDataDir;
    public String credentialProtectedDataDir;
    public String dataDir;
    public int descriptionRes;
    @Deprecated
    public String deviceEncryptedDataDir;
    public String deviceProtectedDataDir;
    public boolean enabled;
    public int enabledSetting;
    public int euid;
    public int flags;
    public int fullBackupContent;
    public int hwFlags;
    public int installLocation;
    public int largestWidthLimitDp;
    public String manageSpaceActivityName;
    public int minSdkVersion;
    public String nativeLibraryDir;
    public String nativeLibraryRootDir;
    public boolean nativeLibraryRootRequiresIsa;
    public int networkSecurityConfigRes;
    public String permission;
    public String primaryCpuAbi;
    public int privateFlags;
    public String processName;
    public String publicSourceDir;
    public int requiresSmallestWidthDp;
    public String[] resourceDirs;
    public String scanPublicSourceDir;
    public String scanSourceDir;
    public String secondaryCpuAbi;
    public String secondaryNativeLibraryDir;
    public String seinfo;
    public String[] sharedLibraryFiles;
    public String sourceDir;
    public String[] splitPublicSourceDirs;
    public String[] splitSourceDirs;
    public int targetSdkVersion;
    public String taskAffinity;
    public int theme;
    public int uiOptions;
    public int uid;
    public int versionCode;
    public String volumeUuid;

    public static class DisplayNameComparator implements Comparator<ApplicationInfo> {
        private PackageManager mPM;
        private final Collator sCollator;

        public DisplayNameComparator(PackageManager pm) {
            this.sCollator = Collator.getInstance();
            this.mPM = pm;
        }

        public final int compare(ApplicationInfo aa, ApplicationInfo ab) {
            CharSequence sa = this.mPM.getApplicationLabel(aa);
            if (sa == null) {
                sa = aa.packageName;
            }
            CharSequence sb = this.mPM.getApplicationLabel(ab);
            if (sb == null) {
                sb = ab.packageName;
            }
            return this.sCollator.compare(sa.toString(), sb.toString());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.ApplicationInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.ApplicationInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ApplicationInfo.<clinit>():void");
    }

    public void dump(Printer pw, String prefix) {
        dump(pw, prefix, 3);
    }

    public void dump(Printer pw, String prefix, int flags) {
        super.dumpFront(pw, prefix);
        if (!((flags & PRIVATE_FLAG_HIDDEN) == 0 || this.className == null)) {
            pw.println(prefix + "className=" + this.className);
        }
        if (this.permission != null) {
            pw.println(prefix + "permission=" + this.permission);
        }
        pw.println(prefix + "processName=" + this.processName);
        if ((flags & PRIVATE_FLAG_HIDDEN) != 0) {
            pw.println(prefix + "taskAffinity=" + this.taskAffinity);
        }
        pw.println(prefix + "uid=" + this.uid + " flags=0x" + Integer.toHexString(flags) + " privateFlags=0x" + Integer.toHexString(this.privateFlags) + " theme=0x" + Integer.toHexString(this.theme));
        if ((flags & PRIVATE_FLAG_HIDDEN) != 0) {
            pw.println(prefix + "requiresSmallestWidthDp=" + this.requiresSmallestWidthDp + " compatibleWidthLimitDp=" + this.compatibleWidthLimitDp + " largestWidthLimitDp=" + this.largestWidthLimitDp);
        }
        pw.println(prefix + "sourceDir=" + this.sourceDir);
        if (!Objects.equals(this.sourceDir, this.publicSourceDir)) {
            pw.println(prefix + "publicSourceDir=" + this.publicSourceDir);
        }
        if (!ArrayUtils.isEmpty(this.splitSourceDirs)) {
            pw.println(prefix + "splitSourceDirs=" + Arrays.toString(this.splitSourceDirs));
        }
        if (!(ArrayUtils.isEmpty(this.splitPublicSourceDirs) || Arrays.equals(this.splitSourceDirs, this.splitPublicSourceDirs))) {
            pw.println(prefix + "splitPublicSourceDirs=" + Arrays.toString(this.splitPublicSourceDirs));
        }
        if (this.resourceDirs != null) {
            pw.println(prefix + "resourceDirs=" + Arrays.toString(this.resourceDirs));
        }
        if (!((flags & PRIVATE_FLAG_HIDDEN) == 0 || this.seinfo == null)) {
            pw.println(prefix + "seinfo=" + this.seinfo);
        }
        pw.println(prefix + "dataDir=" + this.dataDir);
        if ((flags & PRIVATE_FLAG_HIDDEN) != 0) {
            pw.println(prefix + "deviceProtectedDataDir=" + this.deviceProtectedDataDir);
            pw.println(prefix + "credentialProtectedDataDir=" + this.credentialProtectedDataDir);
            if (this.sharedLibraryFiles != null) {
                pw.println(prefix + "sharedLibraryFiles=" + Arrays.toString(this.sharedLibraryFiles));
            }
        }
        pw.println(prefix + "enabled=" + this.enabled + " minSdkVersion=" + this.minSdkVersion + " targetSdkVersion=" + this.targetSdkVersion + " versionCode=" + this.versionCode);
        if ((flags & PRIVATE_FLAG_HIDDEN) != 0) {
            if (this.manageSpaceActivityName != null) {
                pw.println(prefix + "manageSpaceActivityName=" + this.manageSpaceActivityName);
            }
            if (this.descriptionRes != 0) {
                pw.println(prefix + "description=0x" + Integer.toHexString(this.descriptionRes));
            }
            if (this.uiOptions != 0) {
                pw.println(prefix + "uiOptions=0x" + Integer.toHexString(this.uiOptions));
            }
            pw.println(prefix + "supportsRtl=" + (hasRtlSupport() ? "true" : "false"));
            pw.println(prefix + " hwFlags=0x" + Integer.toHexString(this.hwFlags));
            pw.println(prefix + " euid=" + this.euid);
            if (this.fullBackupContent > 0) {
                pw.println(prefix + "fullBackupContent=@xml/" + this.fullBackupContent);
            } else {
                pw.println(prefix + "fullBackupContent=" + (this.fullBackupContent < 0 ? "false" : "true"));
            }
            if (this.networkSecurityConfigRes != 0) {
                pw.println(prefix + "networkSecurityConfigRes=0x" + Integer.toHexString(this.networkSecurityConfigRes));
            }
        }
        super.dumpBack(pw, prefix);
    }

    public boolean hasRtlSupport() {
        return (this.flags & FLAG_SUPPORTS_RTL) == FLAG_SUPPORTS_RTL;
    }

    public boolean hasCode() {
        return (this.flags & PRIVATE_FLAG_FORWARD_LOCK) != 0;
    }

    public ApplicationInfo() {
        this.fullBackupContent = 0;
        this.uiOptions = 0;
        this.flags = 0;
        this.hwFlags = 0;
        this.euid = 0;
        this.requiresSmallestWidthDp = 0;
        this.compatibleWidthLimitDp = 0;
        this.largestWidthLimitDp = 0;
        this.seinfo = "default";
        this.enabled = true;
        this.enabledSetting = 0;
        this.installLocation = -1;
    }

    public ApplicationInfo(ApplicationInfo orig) {
        super((PackageItemInfo) orig);
        this.fullBackupContent = 0;
        this.uiOptions = 0;
        this.flags = 0;
        this.hwFlags = 0;
        this.euid = 0;
        this.requiresSmallestWidthDp = 0;
        this.compatibleWidthLimitDp = 0;
        this.largestWidthLimitDp = 0;
        this.seinfo = "default";
        this.enabled = true;
        this.enabledSetting = 0;
        this.installLocation = -1;
        this.taskAffinity = orig.taskAffinity;
        this.permission = orig.permission;
        this.processName = orig.processName;
        this.className = orig.className;
        this.theme = orig.theme;
        this.flags = orig.flags;
        this.privateFlags = orig.privateFlags;
        this.hwFlags = orig.hwFlags;
        this.euid = orig.euid;
        this.requiresSmallestWidthDp = orig.requiresSmallestWidthDp;
        this.compatibleWidthLimitDp = orig.compatibleWidthLimitDp;
        this.largestWidthLimitDp = orig.largestWidthLimitDp;
        this.volumeUuid = orig.volumeUuid;
        this.scanSourceDir = orig.scanSourceDir;
        this.scanPublicSourceDir = orig.scanPublicSourceDir;
        this.sourceDir = orig.sourceDir;
        this.publicSourceDir = orig.publicSourceDir;
        this.splitSourceDirs = orig.splitSourceDirs;
        this.splitPublicSourceDirs = orig.splitPublicSourceDirs;
        this.nativeLibraryDir = orig.nativeLibraryDir;
        this.secondaryNativeLibraryDir = orig.secondaryNativeLibraryDir;
        this.nativeLibraryRootDir = orig.nativeLibraryRootDir;
        this.nativeLibraryRootRequiresIsa = orig.nativeLibraryRootRequiresIsa;
        this.primaryCpuAbi = orig.primaryCpuAbi;
        this.secondaryCpuAbi = orig.secondaryCpuAbi;
        this.resourceDirs = orig.resourceDirs;
        this.seinfo = orig.seinfo;
        this.sharedLibraryFiles = orig.sharedLibraryFiles;
        this.dataDir = orig.dataDir;
        String str = orig.deviceProtectedDataDir;
        this.deviceProtectedDataDir = str;
        this.deviceEncryptedDataDir = str;
        str = orig.credentialProtectedDataDir;
        this.credentialProtectedDataDir = str;
        this.credentialEncryptedDataDir = str;
        this.uid = orig.uid;
        this.minSdkVersion = orig.minSdkVersion;
        this.targetSdkVersion = orig.targetSdkVersion;
        this.versionCode = orig.versionCode;
        this.enabled = orig.enabled;
        this.enabledSetting = orig.enabledSetting;
        this.installLocation = orig.installLocation;
        this.manageSpaceActivityName = orig.manageSpaceActivityName;
        this.descriptionRes = orig.descriptionRes;
        this.uiOptions = orig.uiOptions;
        this.backupAgentName = orig.backupAgentName;
        this.fullBackupContent = orig.fullBackupContent;
        this.networkSecurityConfigRes = orig.networkSecurityConfigRes;
    }

    public String toString() {
        return "ApplicationInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i = PRIVATE_FLAG_HIDDEN;
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.taskAffinity);
        dest.writeString(this.permission);
        dest.writeString(this.processName);
        dest.writeString(this.className);
        dest.writeInt(this.theme);
        dest.writeInt(this.flags);
        dest.writeInt(this.privateFlags);
        dest.writeInt(this.hwFlags);
        dest.writeInt(this.euid);
        dest.writeInt(this.requiresSmallestWidthDp);
        dest.writeInt(this.compatibleWidthLimitDp);
        dest.writeInt(this.largestWidthLimitDp);
        dest.writeString(this.volumeUuid);
        dest.writeString(this.scanSourceDir);
        dest.writeString(this.scanPublicSourceDir);
        dest.writeString(this.sourceDir);
        dest.writeString(this.publicSourceDir);
        dest.writeStringArray(this.splitSourceDirs);
        dest.writeStringArray(this.splitPublicSourceDirs);
        dest.writeString(this.nativeLibraryDir);
        dest.writeString(this.secondaryNativeLibraryDir);
        dest.writeString(this.nativeLibraryRootDir);
        dest.writeInt(this.nativeLibraryRootRequiresIsa ? PRIVATE_FLAG_HIDDEN : 0);
        dest.writeString(this.primaryCpuAbi);
        dest.writeString(this.secondaryCpuAbi);
        dest.writeStringArray(this.resourceDirs);
        dest.writeString(this.seinfo);
        dest.writeStringArray(this.sharedLibraryFiles);
        dest.writeString(this.dataDir);
        dest.writeString(this.deviceProtectedDataDir);
        dest.writeString(this.credentialProtectedDataDir);
        dest.writeInt(this.uid);
        dest.writeInt(this.minSdkVersion);
        dest.writeInt(this.targetSdkVersion);
        dest.writeInt(this.versionCode);
        if (!this.enabled) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.enabledSetting);
        dest.writeInt(this.installLocation);
        dest.writeString(this.manageSpaceActivityName);
        dest.writeString(this.backupAgentName);
        dest.writeInt(this.descriptionRes);
        dest.writeInt(this.uiOptions);
        dest.writeInt(this.fullBackupContent);
        dest.writeInt(this.networkSecurityConfigRes);
    }

    private ApplicationInfo(Parcel source) {
        boolean z = true;
        super(source);
        this.fullBackupContent = 0;
        this.uiOptions = 0;
        this.flags = 0;
        this.hwFlags = 0;
        this.euid = 0;
        this.requiresSmallestWidthDp = 0;
        this.compatibleWidthLimitDp = 0;
        this.largestWidthLimitDp = 0;
        this.seinfo = "default";
        this.enabled = true;
        this.enabledSetting = 0;
        this.installLocation = -1;
        this.taskAffinity = source.readString();
        this.permission = source.readString();
        this.processName = source.readString();
        this.className = source.readString();
        this.theme = source.readInt();
        this.flags = source.readInt();
        this.privateFlags = source.readInt();
        this.hwFlags = source.readInt();
        this.euid = source.readInt();
        this.requiresSmallestWidthDp = source.readInt();
        this.compatibleWidthLimitDp = source.readInt();
        this.largestWidthLimitDp = source.readInt();
        this.volumeUuid = source.readString();
        this.scanSourceDir = source.readString();
        this.scanPublicSourceDir = source.readString();
        this.sourceDir = source.readString();
        this.publicSourceDir = source.readString();
        this.splitSourceDirs = source.readStringArray();
        this.splitPublicSourceDirs = source.readStringArray();
        this.nativeLibraryDir = source.readString();
        this.secondaryNativeLibraryDir = source.readString();
        this.nativeLibraryRootDir = source.readString();
        this.nativeLibraryRootRequiresIsa = source.readInt() != 0;
        this.primaryCpuAbi = source.readString();
        this.secondaryCpuAbi = source.readString();
        this.resourceDirs = source.readStringArray();
        this.seinfo = source.readString();
        this.sharedLibraryFiles = source.readStringArray();
        this.dataDir = source.readString();
        String readString = source.readString();
        this.deviceProtectedDataDir = readString;
        this.deviceEncryptedDataDir = readString;
        readString = source.readString();
        this.credentialProtectedDataDir = readString;
        this.credentialEncryptedDataDir = readString;
        this.uid = source.readInt();
        this.minSdkVersion = source.readInt();
        this.targetSdkVersion = source.readInt();
        this.versionCode = source.readInt();
        if (source.readInt() == 0) {
            z = false;
        }
        this.enabled = z;
        this.enabledSetting = source.readInt();
        this.installLocation = source.readInt();
        this.manageSpaceActivityName = source.readString();
        this.backupAgentName = source.readString();
        this.descriptionRes = source.readInt();
        this.uiOptions = source.readInt();
        this.fullBackupContent = source.readInt();
        this.networkSecurityConfigRes = source.readInt();
    }

    public CharSequence loadDescription(PackageManager pm) {
        if (this.descriptionRes != 0) {
            CharSequence label = pm.getText(this.packageName, this.descriptionRes, this);
            if (label != null) {
                return label;
            }
        }
        return null;
    }

    public void disableCompatibilityMode() {
        this.flags |= 540160;
    }

    public void initForUser(int userId) {
        this.uid = UserHandle.getUid(userId, UserHandle.getAppId(this.uid));
        if (ZenModeConfig.SYSTEM_AUTHORITY.equals(this.packageName)) {
            this.dataDir = Environment.getDataSystemDirectory().getAbsolutePath();
            return;
        }
        String absolutePath = Environment.getDataUserDePackageDirectory(this.volumeUuid, userId, this.packageName).getAbsolutePath();
        this.deviceProtectedDataDir = absolutePath;
        this.deviceEncryptedDataDir = absolutePath;
        absolutePath = Environment.getDataUserCePackageDirectory(this.volumeUuid, userId, this.packageName).getAbsolutePath();
        this.credentialProtectedDataDir = absolutePath;
        this.credentialEncryptedDataDir = absolutePath;
        if ((this.privateFlags & PRIVATE_FLAG_DEFAULT_TO_DEVICE_PROTECTED_STORAGE) != 0) {
            this.dataDir = this.deviceProtectedDataDir;
        } else {
            this.dataDir = this.credentialProtectedDataDir;
        }
    }

    public Drawable loadDefaultIcon(PackageManager pm) {
        if ((this.flags & FLAG_EXTERNAL_STORAGE) == 0 || !isPackageUnavailable(pm)) {
            return pm.getDefaultActivityIcon();
        }
        return Resources.getSystem().getDrawable(17303323);
    }

    private boolean isPackageUnavailable(PackageManager pm) {
        boolean z = true;
        try {
            if (pm.getPackageInfo(this.packageName, 0) != null) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            return true;
        }
    }

    public boolean isForwardLocked() {
        return (this.privateFlags & PRIVATE_FLAG_FORWARD_LOCK) != 0;
    }

    public boolean isSystemApp() {
        return (this.flags & PRIVATE_FLAG_HIDDEN) != 0;
    }

    public boolean isPrivilegedApp() {
        return (this.privateFlags & PRIVATE_FLAG_PRIVILEGED) != 0;
    }

    public boolean isUpdatedSystemApp() {
        return (this.flags & PRIVATE_FLAG_AUTOPLAY) != 0;
    }

    public boolean isInternal() {
        return (this.flags & FLAG_EXTERNAL_STORAGE) == 0;
    }

    public boolean isExternalAsec() {
        if (!TextUtils.isEmpty(this.volumeUuid) || (this.flags & FLAG_EXTERNAL_STORAGE) == 0) {
            return false;
        }
        return true;
    }

    public boolean isDefaultToDeviceProtectedStorage() {
        return (this.privateFlags & PRIVATE_FLAG_DEFAULT_TO_DEVICE_PROTECTED_STORAGE) != 0;
    }

    public boolean isDirectBootAware() {
        return (this.privateFlags & PRIVATE_FLAG_DIRECT_BOOT_AWARE) != 0;
    }

    public boolean isPartiallyDirectBootAware() {
        return (this.privateFlags & PRIVATE_FLAG_PARTIALLY_DIRECT_BOOT_AWARE) != 0;
    }

    public boolean isAutoPlayApp() {
        return (this.privateFlags & PRIVATE_FLAG_AUTOPLAY) != 0;
    }

    public boolean isEphemeralApp() {
        return (this.privateFlags & PRIVATE_FLAG_EPHEMERAL) != 0;
    }

    public boolean isRequiredForSystemUser() {
        return (this.privateFlags & PRIVATE_FLAG_REQUIRED_FOR_SYSTEM_USER) != 0;
    }

    protected ApplicationInfo getApplicationInfo() {
        return this;
    }

    public void setCodePath(String codePath) {
        this.scanSourceDir = codePath;
    }

    public void setBaseCodePath(String baseCodePath) {
        this.sourceDir = baseCodePath;
    }

    public void setSplitCodePaths(String[] splitCodePaths) {
        this.splitSourceDirs = splitCodePaths;
    }

    public void setResourcePath(String resourcePath) {
        this.scanPublicSourceDir = resourcePath;
    }

    public void setBaseResourcePath(String baseResourcePath) {
        this.publicSourceDir = baseResourcePath;
    }

    public void setSplitResourcePaths(String[] splitResourcePaths) {
        this.splitPublicSourceDirs = splitResourcePaths;
    }

    public String getCodePath() {
        return this.scanSourceDir;
    }

    public String getBaseCodePath() {
        return this.sourceDir;
    }

    public String[] getSplitCodePaths() {
        return this.splitSourceDirs;
    }

    public String getResourcePath() {
        return this.scanPublicSourceDir;
    }

    public String getBaseResourcePath() {
        return this.publicSourceDir;
    }

    public String[] getSplitResourcePaths() {
        return this.splitSourceDirs;
    }
}
