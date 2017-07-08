package android.content.pm;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAssignedNumbers;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo.WindowLayout;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.hwcontrol.HwWidgetFactory;
import android.hwtheme.HwThemeManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats.HistoryItem;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.PatternMatcher;
import android.os.Process;
import android.os.StrictMode;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.MediaStore.Video.VideoColumns;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.rms.iaware.Events;
import android.security.KeyChain;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties;
import android.service.notification.ZenModeConfig;
import android.service.quicksettings.TileService;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.TypedValue;
import android.util.apk.ApkSignatureSchemeV2Verifier;
import android.util.apk.ApkSignatureSchemeV2Verifier.SignatureNotFoundException;
import android.util.jar.StrictJarFile;
import com.android.internal.R;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PackageParser {
    private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
    private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";
    public static final int APK_SIGNING_UNKNOWN = 0;
    public static final int APK_SIGNING_V1 = 1;
    public static final int APK_SIGNING_V2 = 2;
    private static final Set<String> CHILD_PACKAGE_TAGS = null;
    private static final boolean DEBUG_BACKUP = false;
    private static final boolean DEBUG_JAR = false;
    private static final boolean DEBUG_PARSER = false;
    private static final int MAX_PACKAGES_PER_APK = 5;
    private static final String MNT_EXPAND = "/mnt/expand/";
    private static final boolean MULTI_PACKAGE_APK_ENABLED = false;
    public static final NewPermissionInfo[] NEW_PERMISSIONS = null;
    public static final int PARSE_CHATTY = 2;
    public static final int PARSE_COLLECT_CERTIFICATES = 256;
    private static final int PARSE_DEFAULT_INSTALL_LOCATION = -1;
    public static final int PARSE_ENFORCE_CODE = 1024;
    public static final int PARSE_EXTERNAL_STORAGE = 32;
    public static final int PARSE_FORCE_SDK = 4096;
    public static final int PARSE_FORWARD_LOCK = 16;
    public static final int PARSE_IGNORE_PROCESSES = 8;
    public static final int PARSE_IS_EPHEMERAL = 2048;
    public static final int PARSE_IS_PRIVILEGED = 128;
    public static final int PARSE_IS_SYSTEM = 1;
    public static final int PARSE_IS_SYSTEM_DIR = 64;
    public static final int PARSE_MUST_BE_APK = 4;
    public static final int PARSE_TRUSTED_OVERLAY = 512;
    private static final boolean RIGID_PARSER = false;
    private static final String[] SDK_CODENAMES = null;
    private static final int SDK_VERSION = 0;
    public static final SplitPermissionInfo[] SPLIT_PERMISSIONS = null;
    private static final String TAG = "PackageParser";
    private static final String TAG_ADOPT_PERMISSIONS = "adopt-permissions";
    private static final String TAG_APPLICATION = "application";
    private static final String TAG_COMPATIBLE_SCREENS = "compatible-screens";
    private static final String TAG_EAT_COMMENT = "eat-comment";
    private static final String TAG_FEATURE_GROUP = "feature-group";
    private static final String TAG_INSTRUMENTATION = "instrumentation";
    private static final String TAG_KEY_SETS = "key-sets";
    private static final String TAG_MANIFEST = "manifest";
    private static final String TAG_ORIGINAL_PACKAGE = "original-package";
    private static final String TAG_OVERLAY = "overlay";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PERMISSION = "permission";
    private static final String TAG_PERMISSION_GROUP = "permission-group";
    private static final String TAG_PERMISSION_TREE = "permission-tree";
    private static final String TAG_PROTECTED_BROADCAST = "protected-broadcast";
    private static final String TAG_RESTRICT_UPDATE = "restrict-update";
    private static final String TAG_SUPPORTS_INPUT = "supports-input";
    private static final String TAG_SUPPORT_SCREENS = "supports-screens";
    private static final String TAG_USES_CONFIGURATION = "uses-configuration";
    private static final String TAG_USES_FEATURE = "uses-feature";
    private static final String TAG_USES_GL_TEXTURE = "uses-gl-texture";
    private static final String TAG_USES_PERMISSION = "uses-permission";
    private static final String TAG_USES_PERMISSION_SDK_23 = "uses-permission-sdk-23";
    private static final String TAG_USES_PERMISSION_SDK_M = "uses-permission-sdk-m";
    private static final String TAG_USES_SDK = "uses-sdk";
    private static AtomicReference<byte[]> sBuffer;
    private static boolean sCompatibilityModeEnabled;
    private static final Comparator<String> sSplitNameComparator = null;
    @Deprecated
    private String mArchiveSourcePath;
    private DisplayMetrics mMetrics;
    private boolean mOnlyCoreApps;
    private ParseComponentArgs mParseActivityAliasArgs;
    private ParseComponentArgs mParseActivityArgs;
    private int mParseError;
    private ParsePackageItemArgs mParseInstrumentationArgs;
    private ParseComponentArgs mParseProviderArgs;
    private ParseComponentArgs mParseServiceArgs;
    private String[] mSeparateProcesses;

    public static class Component<II extends IntentInfo> {
        public final String className;
        ComponentName componentName;
        String componentShortName;
        public final ArrayList<II> intents;
        public Bundle metaData;
        public final Package owner;

        public Component(Package _owner) {
            this.owner = _owner;
            this.intents = null;
            this.className = null;
        }

        public Component(ParsePackageItemArgs args, PackageItemInfo outInfo) {
            this.owner = args.owner;
            this.intents = new ArrayList(PackageParser.SDK_VERSION);
            String name = args.sa.getNonConfigurationString(args.nameRes, PackageParser.SDK_VERSION);
            if (name == null) {
                this.className = null;
                args.outError[PackageParser.SDK_VERSION] = args.tag + " does not specify android:name";
                return;
            }
            outInfo.name = PackageParser.buildClassName(this.owner.applicationInfo.packageName, name, args.outError);
            if (outInfo.name == null) {
                this.className = null;
                args.outError[PackageParser.SDK_VERSION] = args.tag + " does not have valid android:name";
                return;
            }
            this.className = outInfo.name;
            int iconVal = args.sa.getResourceId(args.iconRes, PackageParser.SDK_VERSION);
            if (iconVal != 0) {
                outInfo.icon = iconVal;
                outInfo.nonLocalizedLabel = null;
            }
            int logoVal = args.sa.getResourceId(args.logoRes, PackageParser.SDK_VERSION);
            if (logoVal != 0) {
                outInfo.logo = logoVal;
            }
            int bannerVal = args.sa.getResourceId(args.bannerRes, PackageParser.SDK_VERSION);
            if (bannerVal != 0) {
                outInfo.banner = bannerVal;
            }
            TypedValue v = args.sa.peekValue(args.labelRes);
            if (v != null) {
                int i = v.resourceId;
                outInfo.labelRes = i;
                if (i == 0) {
                    outInfo.nonLocalizedLabel = v.coerceToString();
                }
            }
            outInfo.packageName = this.owner.packageName;
        }

        public Component(ParseComponentArgs args, ComponentInfo outInfo) {
            this((ParsePackageItemArgs) args, (PackageItemInfo) outInfo);
            if (args.outError[PackageParser.SDK_VERSION] == null) {
                if (args.processRes != 0) {
                    CharSequence pname;
                    if (this.owner.applicationInfo.targetSdkVersion >= PackageParser.PARSE_IGNORE_PROCESSES) {
                        pname = args.sa.getNonConfigurationString(args.processRes, PackageParser.PARSE_ENFORCE_CODE);
                    } else {
                        pname = args.sa.getNonResourceString(args.processRes);
                    }
                    outInfo.processName = PackageParser.buildProcessName(this.owner.applicationInfo.packageName, this.owner.applicationInfo.processName, pname, args.flags, args.sepProcesses, args.outError);
                }
                if (args.descriptionRes != 0) {
                    outInfo.descriptionRes = args.sa.getResourceId(args.descriptionRes, PackageParser.SDK_VERSION);
                }
                outInfo.enabled = args.sa.getBoolean(args.enabledRes, true);
            }
        }

        public Component(Component<II> clone) {
            this.owner = clone.owner;
            this.intents = clone.intents;
            this.className = clone.className;
            this.componentName = clone.componentName;
            this.componentShortName = clone.componentShortName;
        }

        public ComponentName getComponentName() {
            if (this.componentName != null) {
                return this.componentName;
            }
            if (this.className != null) {
                this.componentName = new ComponentName(this.owner.applicationInfo.packageName, this.className);
            }
            return this.componentName;
        }

        public void appendComponentShortName(StringBuilder sb) {
            ComponentName.appendShortString(sb, this.owner.applicationInfo.packageName, this.className);
        }

        public void printComponentShortName(PrintWriter pw) {
            ComponentName.printShortString(pw, this.owner.applicationInfo.packageName, this.className);
        }

        public void setPackageName(String packageName) {
            this.componentName = null;
            this.componentShortName = null;
        }
    }

    public static final class Activity extends Component<ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(ParseComponentArgs args, ActivityInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("Activity{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class IntentInfo extends IntentFilter {
        public int banner;
        public boolean hasDefault;
        public int icon;
        public int labelRes;
        public int logo;
        public CharSequence nonLocalizedLabel;
        public int preferred;
    }

    public static final class ActivityIntentInfo extends IntentInfo {
        public final Activity activity;

        public ActivityIntentInfo(Activity _activity) {
            this.activity = _activity;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("ActivityIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.activity.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class ApkLite {
        public final Certificate[][] certificates;
        public final String codePath;
        public final boolean coreApp;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public final boolean multiArch;
        public final String packageName;
        public final int revisionCode;
        public final Signature[] signatures;
        public final String splitName;
        public final boolean use32bitAbi;
        public final VerifierInfo[] verifiers;
        public final int versionCode;

        public ApkLite(String codePath, String packageName, String splitName, int versionCode, int revisionCode, int installLocation, List<VerifierInfo> verifiers, Signature[] signatures, Certificate[][] certificates, boolean coreApp, boolean multiArch, boolean use32bitAbi, boolean extractNativeLibs) {
            this.codePath = codePath;
            this.packageName = packageName;
            this.splitName = splitName;
            this.versionCode = versionCode;
            this.revisionCode = revisionCode;
            this.installLocation = installLocation;
            this.verifiers = (VerifierInfo[]) verifiers.toArray(new VerifierInfo[verifiers.size()]);
            this.signatures = signatures;
            this.certificates = certificates;
            this.coreApp = coreApp;
            this.multiArch = multiArch;
            this.use32bitAbi = use32bitAbi;
            this.extractNativeLibs = extractNativeLibs;
        }
    }

    public static final class Instrumentation extends Component<IntentInfo> {
        public final InstrumentationInfo info;

        public Instrumentation(ParsePackageItemArgs args, InstrumentationInfo _info) {
            super(args, (PackageItemInfo) _info);
            this.info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("Instrumentation{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class NewPermissionInfo {
        public final int fileVersion;
        public final String name;
        public final int sdkVersion;

        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            this.name = name;
            this.sdkVersion = sdkVersion;
            this.fileVersion = fileVersion;
        }
    }

    public static final class Package {
        public final ArrayList<Activity> activities;
        public final ApplicationInfo applicationInfo;
        public String baseCodePath;
        public boolean baseHardwareAccelerated;
        public int baseRevisionCode;
        public ArrayList<Package> childPackages;
        public String codePath;
        public ArrayList<ConfigurationInfo> configPreferences;
        public boolean coreApp;
        public String cpuAbiOverride;
        public ArrayList<FeatureGroupInfo> featureGroups;
        public int installLocation;
        public final ArrayList<Instrumentation> instrumentation;
        public ArrayList<String> libraryNames;
        public ArrayList<String> mAdoptPermissions;
        public Bundle mAppMetaData;
        public Certificate[][] mCertificates;
        public Object mExtras;
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;
        public long[] mLastPackageUsageTimeInMills;
        public ArrayList<String> mOriginalPackages;
        public int mOverlayPriority;
        public String mOverlayTarget;
        public int mPreferredOrder;
        public String mRealPackage;
        public Signature[] mRealSignatures;
        public String mRequiredAccountType;
        public boolean mRequiredForAllUsers;
        public String mRestrictedAccountType;
        public String mSharedUserId;
        public int mSharedUserLabel;
        public Signature[] mSignatures;
        public ArraySet<PublicKey> mSigningKeys;
        public boolean mTrustedOverlay;
        public ArraySet<String> mUpgradeKeySets;
        public int mVersionCode;
        public String mVersionName;
        public String packageName;
        public Package parentPackage;
        public final ArrayList<PermissionGroup> permissionGroups;
        public final ArrayList<Permission> permissions;
        public ArrayList<ActivityIntentInfo> preferredActivityFilters;
        public ArrayList<String> protectedBroadcasts;
        public final ArrayList<Provider> providers;
        public final ArrayList<Activity> receivers;
        public ArrayList<FeatureInfo> reqFeatures;
        public final ArrayList<String> requestedPermissions;
        public byte[] restrictUpdateHash;
        public final ArrayList<Service> services;
        public String[] splitCodePaths;
        public int[] splitFlags;
        public String[] splitNames;
        public int[] splitPrivateFlags;
        public int[] splitRevisionCodes;
        public boolean use32bitAbi;
        public ArrayList<String> usesLibraries;
        public String[] usesLibraryFiles;
        public ArrayList<String> usesOptionalLibraries;
        public String volumeUuid;

        public void setApplicationInfoFlags(int r1, int r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageParser.Package.setApplicationInfoFlags(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.Package.setApplicationInfoFlags(int, int):void");
        }

        public Package(String packageName) {
            this.applicationInfo = new ApplicationInfo();
            this.permissions = new ArrayList(PackageParser.SDK_VERSION);
            this.permissionGroups = new ArrayList(PackageParser.SDK_VERSION);
            this.activities = new ArrayList(PackageParser.SDK_VERSION);
            this.receivers = new ArrayList(PackageParser.SDK_VERSION);
            this.providers = new ArrayList(PackageParser.SDK_VERSION);
            this.services = new ArrayList(PackageParser.SDK_VERSION);
            this.instrumentation = new ArrayList(PackageParser.SDK_VERSION);
            this.requestedPermissions = new ArrayList();
            this.libraryNames = null;
            this.usesLibraries = null;
            this.usesOptionalLibraries = null;
            this.usesLibraryFiles = null;
            this.preferredActivityFilters = null;
            this.mOriginalPackages = null;
            this.mRealPackage = null;
            this.mAdoptPermissions = null;
            this.mAppMetaData = null;
            this.mRealSignatures = new Signature[PackageParser.SDK_VERSION];
            this.mPreferredOrder = PackageParser.SDK_VERSION;
            this.mLastPackageUsageTimeInMills = new long[PackageParser.PARSE_IGNORE_PROCESSES];
            this.configPreferences = null;
            this.reqFeatures = null;
            this.featureGroups = null;
            this.packageName = packageName;
            this.applicationInfo.packageName = packageName;
            this.applicationInfo.uid = PackageParser.PARSE_DEFAULT_INSTALL_LOCATION;
        }

        public void setApplicationVolumeUuid(String volumeUuid) {
            this.applicationInfo.volumeUuid = volumeUuid;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).applicationInfo.volumeUuid = volumeUuid;
                }
            }
        }

        public void setApplicationInfoCodePath(String codePath) {
            this.applicationInfo.setCodePath(codePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setCodePath(codePath);
                }
            }
        }

        public void setApplicationInfoResourcePath(String resourcePath) {
            this.applicationInfo.setResourcePath(resourcePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setResourcePath(resourcePath);
                }
            }
        }

        public void setApplicationInfoBaseResourcePath(String resourcePath) {
            this.applicationInfo.setBaseResourcePath(resourcePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setBaseResourcePath(resourcePath);
                }
            }
        }

        public void setApplicationInfoBaseCodePath(String baseCodePath) {
            this.applicationInfo.setBaseCodePath(baseCodePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setBaseCodePath(baseCodePath);
                }
            }
        }

        public boolean hasChildPackage(String packageName) {
            int childCount = this.childPackages != null ? this.childPackages.size() : PackageParser.SDK_VERSION;
            for (int i = PackageParser.SDK_VERSION; i < childCount; i += PackageParser.PARSE_IS_SYSTEM) {
                if (((Package) this.childPackages.get(i)).packageName.equals(packageName)) {
                    return true;
                }
            }
            return PackageParser.RIGID_PARSER;
        }

        public void setApplicationInfoSplitCodePaths(String[] splitCodePaths) {
            this.applicationInfo.setSplitCodePaths(splitCodePaths);
        }

        public void setApplicationInfoSplitResourcePaths(String[] resroucePaths) {
            this.applicationInfo.setSplitResourcePaths(resroucePaths);
        }

        public void setSplitCodePaths(String[] codePaths) {
            this.splitCodePaths = codePaths;
        }

        public void setCodePath(String codePath) {
            this.codePath = codePath;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).codePath = codePath;
                }
            }
        }

        public void setBaseCodePath(String baseCodePath) {
            this.baseCodePath = baseCodePath;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).baseCodePath = baseCodePath;
                }
            }
        }

        public void setSignatures(Signature[] signatures) {
            this.mSignatures = signatures;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).mSignatures = signatures;
                }
            }
        }

        public void setVolumeUuid(String volumeUuid) {
            this.volumeUuid = volumeUuid;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).volumeUuid = volumeUuid;
                }
            }
        }

        public void setUse32bitAbi(boolean use32bitAbi) {
            this.use32bitAbi = use32bitAbi;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = PackageParser.SDK_VERSION; i < packageCount; i += PackageParser.PARSE_IS_SYSTEM) {
                    ((Package) this.childPackages.get(i)).use32bitAbi = use32bitAbi;
                }
            }
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }

        public List<String> getAllCodePathsExcludingResourceOnly() {
            ArrayList<String> paths = new ArrayList();
            if ((this.applicationInfo.flags & PackageParser.PARSE_MUST_BE_APK) != 0) {
                paths.add(this.baseCodePath);
            }
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                for (int i = PackageParser.SDK_VERSION; i < this.splitCodePaths.length; i += PackageParser.PARSE_IS_SYSTEM) {
                    if ((this.splitFlags[i] & PackageParser.PARSE_MUST_BE_APK) != 0) {
                        paths.add(this.splitCodePaths[i]);
                    }
                }
            }
            return paths;
        }

        public void setPackageName(String newName) {
            int i;
            this.packageName = newName;
            this.applicationInfo.packageName = newName;
            for (i = this.permissions.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((Permission) this.permissions.get(i)).setPackageName(newName);
            }
            for (i = this.permissionGroups.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((PermissionGroup) this.permissionGroups.get(i)).setPackageName(newName);
            }
            for (i = this.activities.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((Activity) this.activities.get(i)).setPackageName(newName);
            }
            for (i = this.receivers.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((Activity) this.receivers.get(i)).setPackageName(newName);
            }
            for (i = this.providers.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((Provider) this.providers.get(i)).setPackageName(newName);
            }
            for (i = this.services.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((Service) this.services.get(i)).setPackageName(newName);
            }
            for (i = this.instrumentation.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                ((Instrumentation) this.instrumentation.get(i)).setPackageName(newName);
            }
        }

        public boolean hasComponentClassName(String name) {
            int i;
            for (i = this.activities.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                if (name.equals(((Activity) this.activities.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.receivers.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                if (name.equals(((Activity) this.receivers.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.providers.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                if (name.equals(((Provider) this.providers.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.services.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                if (name.equals(((Service) this.services.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.instrumentation.size() + PackageParser.PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PackageParser.PARSE_DEFAULT_INSTALL_LOCATION) {
                if (name.equals(((Instrumentation) this.instrumentation.get(i)).className)) {
                    return true;
                }
            }
            return PackageParser.RIGID_PARSER;
        }

        public void forceResizeableAllActivity() {
            for (int i = PackageParser.SDK_VERSION; i < this.activities.size(); i += PackageParser.PARSE_IS_SYSTEM) {
                if (((Activity) this.activities.get(i)).info.resizeMode == 0) {
                    ((Activity) this.activities.get(i)).info.resizeMode = PackageParser.PARSE_MUST_BE_APK;
                }
            }
        }

        public boolean isForwardLocked() {
            return this.applicationInfo.isForwardLocked();
        }

        public boolean isSystemApp() {
            return this.applicationInfo.isSystemApp();
        }

        public boolean isPrivilegedApp() {
            return this.applicationInfo.isPrivilegedApp();
        }

        public boolean isUpdatedSystemApp() {
            return this.applicationInfo.isUpdatedSystemApp();
        }

        public boolean canHaveOatDir() {
            if ((isSystemApp() && !isUpdatedSystemApp()) || isForwardLocked() || this.applicationInfo.isExternalAsec()) {
                return PackageParser.RIGID_PARSER;
            }
            return true;
        }

        public boolean isMatch(int flags) {
            if ((Root.FLAG_REMOVABLE_USB & flags) != 0) {
                return isSystemApp();
            }
            return true;
        }

        public long getLatestPackageUseTimeInMills() {
            long latestUse = 0;
            long[] jArr = this.mLastPackageUsageTimeInMills;
            int length = jArr.length;
            for (int i = PackageParser.SDK_VERSION; i < length; i += PackageParser.PARSE_IS_SYSTEM) {
                latestUse = Math.max(latestUse, jArr[i]);
            }
            return latestUse;
        }

        public long getLatestForegroundPackageUseTimeInMills() {
            int i = PackageParser.SDK_VERSION;
            int[] foregroundReasons = new int[]{PackageParser.SDK_VERSION, PackageParser.PARSE_CHATTY};
            long latestUse = 0;
            int length = foregroundReasons.length;
            while (i < length) {
                latestUse = Math.max(latestUse, this.mLastPackageUsageTimeInMills[foregroundReasons[i]]);
                i += PackageParser.PARSE_IS_SYSTEM;
            }
            return latestUse;
        }

        public String toString() {
            return "Package{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
        }
    }

    public static class PackageLite {
        public final String baseCodePath;
        public final int baseRevisionCode;
        public final String codePath;
        public final boolean coreApp;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public final boolean multiArch;
        public final String packageName;
        public final String[] splitCodePaths;
        public final String[] splitNames;
        public final int[] splitRevisionCodes;
        public final boolean use32bitAbi;
        public final VerifierInfo[] verifiers;
        public final int versionCode;

        public PackageLite(String codePath, ApkLite baseApk, String[] splitNames, String[] splitCodePaths, int[] splitRevisionCodes) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames;
            this.codePath = codePath;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths;
            this.baseRevisionCode = baseApk.revisionCode;
            this.splitRevisionCodes = splitRevisionCodes;
            this.coreApp = baseApk.coreApp;
            this.multiArch = baseApk.multiArch;
            this.use32bitAbi = baseApk.use32bitAbi;
            this.extractNativeLibs = baseApk.extractNativeLibs;
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }
    }

    public static class PackageParserException extends Exception {
        public final int error;

        public PackageParserException(int error, String detailMessage) {
            super(detailMessage);
            this.error = error;
        }

        public PackageParserException(int error, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.error = error;
        }
    }

    static class ParsePackageItemArgs {
        final int bannerRes;
        final int iconRes;
        final int labelRes;
        final int logoRes;
        final int nameRes;
        final String[] outError;
        final Package owner;
        TypedArray sa;
        String tag;

        ParsePackageItemArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _logoRes, int _bannerRes) {
            this.owner = _owner;
            this.outError = _outError;
            this.nameRes = _nameRes;
            this.labelRes = _labelRes;
            this.iconRes = _iconRes;
            this.logoRes = _logoRes;
            this.bannerRes = _bannerRes;
        }
    }

    static class ParseComponentArgs extends ParsePackageItemArgs {
        final int descriptionRes;
        final int enabledRes;
        int flags;
        final int processRes;
        final String[] sepProcesses;

        ParseComponentArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _logoRes, int _bannerRes, String[] _sepProcesses, int _processRes, int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _logoRes, _bannerRes);
            this.sepProcesses = _sepProcesses;
            this.processRes = _processRes;
            this.descriptionRes = _descriptionRes;
            this.enabledRes = _enabledRes;
        }
    }

    public static final class Permission extends Component<IntentInfo> {
        public PermissionGroup group;
        public final PermissionInfo info;
        public boolean tree;

        public Permission(Package _owner) {
            super(_owner);
            this.info = new PermissionInfo();
        }

        public Permission(Package _owner, PermissionInfo _info) {
            super(_owner);
            this.info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "Permission{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.info.name + "}";
        }
    }

    public static final class PermissionGroup extends Component<IntentInfo> {
        public final PermissionGroupInfo info;

        public PermissionGroup(Package _owner) {
            super(_owner);
            this.info = new PermissionGroupInfo();
        }

        public PermissionGroup(Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            this.info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.info.name + "}";
        }
    }

    public static final class Provider extends Component<ProviderIntentInfo> {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(ParseComponentArgs args, ProviderInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
            this.syncable = PackageParser.RIGID_PARSER;
        }

        public Provider(Provider existingProvider) {
            super((Component) existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("Provider{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        public final Provider provider;

        public ProviderIntentInfo(Provider provider) {
            this.provider = provider;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("ProviderIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.provider.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(ParseComponentArgs args, ServiceInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ServiceIntentInfo extends IntentInfo {
        public final Service service;

        public ServiceIntentInfo(Service _service) {
            this.service = _service;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(PackageParser.PARSE_IS_PRIVILEGED);
            sb.append("ServiceIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.service.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class SplitNameComparator implements Comparator<String> {
        private SplitNameComparator() {
        }

        public int compare(String lhs, String rhs) {
            if (lhs == null) {
                return PackageParser.PARSE_DEFAULT_INSTALL_LOCATION;
            }
            if (rhs == null) {
                return PackageParser.PARSE_IS_SYSTEM;
            }
            return lhs.compareTo(rhs);
        }
    }

    public static class SplitPermissionInfo {
        public final String[] newPerms;
        public final String rootPerm;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            this.rootPerm = rootPerm;
            this.newPerms = newPerms;
            this.targetSdk = targetSdk;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PackageParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.<clinit>():void");
    }

    public PackageParser() {
        this.mParseError = PARSE_IS_SYSTEM;
        this.mMetrics = new DisplayMetrics();
        this.mMetrics.setToDefaults();
    }

    public void setSeparateProcesses(String[] procs) {
        this.mSeparateProcesses = procs;
    }

    public void setOnlyCoreApps(boolean onlyCoreApps) {
        this.mOnlyCoreApps = onlyCoreApps;
    }

    public void setDisplayMetrics(DisplayMetrics metrics) {
        this.mMetrics = metrics;
    }

    public static final boolean isApkFile(File file) {
        return isApkPath(file.getName());
    }

    private static boolean isApkPath(String path) {
        return path.endsWith(".apk");
    }

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions, state, UserHandle.getCallingUserId());
    }

    private static boolean checkUseInstalledOrHidden(int flags, PackageUserState state) {
        return ((!state.installed || state.hidden) && (flags & Process.PROC_OUT_LONG) == 0) ? RIGID_PARSER : true;
    }

    public static boolean isAvailable(PackageUserState state) {
        return checkUseInstalledOrHidden(SDK_VERSION, state);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state, int userId) {
        if (!checkUseInstalledOrHidden(flags, state) || !p.isMatch(flags)) {
            return null;
        }
        int N;
        ActivityInfo[] res;
        int i;
        int num;
        Activity a;
        int num2;
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.splitNames = p.splitNames;
        pi.versionCode = p.mVersionCode;
        pi.baseRevisionCode = p.baseRevisionCode;
        pi.splitRevisionCodes = p.splitRevisionCodes;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.installLocation = p.installLocation;
        pi.coreApp = p.coreApp;
        if ((pi.applicationInfo.flags & PARSE_IS_SYSTEM) == 0) {
        }
        pi.requiredForAllUsers = p.mRequiredForAllUsers;
        pi.restrictedAccountType = p.mRestrictedAccountType;
        pi.requiredAccountType = p.mRequiredAccountType;
        pi.overlayTarget = p.mOverlayTarget;
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if ((flags & PARSE_COLLECT_CERTIFICATES) != 0) {
            pi.gids = gids;
        }
        if ((flags & Process.PROC_OUT_FLOAT) != 0) {
            if (p.configPreferences != null) {
                N = p.configPreferences.size();
            } else {
                N = SDK_VERSION;
            }
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                p.configPreferences.toArray(pi.configPreferences);
            }
            if (p.reqFeatures != null) {
                N = p.reqFeatures.size();
            } else {
                N = SDK_VERSION;
            }
            if (N > 0) {
                pi.reqFeatures = new FeatureInfo[N];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
            if (p.featureGroups != null) {
                N = p.featureGroups.size();
            } else {
                N = SDK_VERSION;
            }
            if (N > 0) {
                pi.featureGroups = new FeatureGroupInfo[N];
                p.featureGroups.toArray(pi.featureGroups);
            }
        }
        if ((flags & PARSE_IS_SYSTEM) != 0) {
            N = p.activities.size();
            if (N > 0) {
                res = new ActivityInfo[N];
                i = SDK_VERSION;
                num = SDK_VERSION;
                while (i < N) {
                    a = (Activity) p.activities.get(i);
                    if (state.isMatch(a.info, flags)) {
                        num2 = num + PARSE_IS_SYSTEM;
                        res[num] = generateActivityInfo(a, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i += PARSE_IS_SYSTEM;
                    num = num2;
                }
                pi.activities = (ActivityInfo[]) ArrayUtils.trimToSize(res, num);
            }
        }
        if ((flags & PARSE_CHATTY) != 0) {
            N = p.receivers.size();
            if (N > 0) {
                res = new ActivityInfo[N];
                i = SDK_VERSION;
                num = SDK_VERSION;
                while (i < N) {
                    a = (Activity) p.receivers.get(i);
                    if (state.isMatch(a.info, flags)) {
                        num2 = num + PARSE_IS_SYSTEM;
                        res[num] = generateActivityInfo(a, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i += PARSE_IS_SYSTEM;
                    num = num2;
                }
                pi.receivers = (ActivityInfo[]) ArrayUtils.trimToSize(res, num);
            }
        }
        if ((flags & PARSE_MUST_BE_APK) != 0) {
            N = p.services.size();
            if (N > 0) {
                ServiceInfo[] res2 = new ServiceInfo[N];
                i = SDK_VERSION;
                num = SDK_VERSION;
                while (i < N) {
                    Service s = (Service) p.services.get(i);
                    if (state.isMatch(s.info, flags)) {
                        num2 = num + PARSE_IS_SYSTEM;
                        res2[num] = generateServiceInfo(s, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i += PARSE_IS_SYSTEM;
                    num = num2;
                }
                pi.services = (ServiceInfo[]) ArrayUtils.trimToSize(res2, num);
            }
        }
        if ((flags & PARSE_IGNORE_PROCESSES) != 0) {
            N = p.providers.size();
            if (N > 0) {
                ProviderInfo[] res3 = new ProviderInfo[N];
                i = SDK_VERSION;
                num = SDK_VERSION;
                while (i < N) {
                    Provider pr = (Provider) p.providers.get(i);
                    if (state.isMatch(pr.info, flags)) {
                        num2 = num + PARSE_IS_SYSTEM;
                        res3[num] = generateProviderInfo(pr, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i += PARSE_IS_SYSTEM;
                    num = num2;
                }
                pi.providers = (ProviderInfo[]) ArrayUtils.trimToSize(res3, num);
            }
        }
        if ((flags & PARSE_FORWARD_LOCK) != 0) {
            N = p.instrumentation.size();
            if (N > 0) {
                pi.instrumentation = new InstrumentationInfo[N];
                for (i = SDK_VERSION; i < N; i += PARSE_IS_SYSTEM) {
                    pi.instrumentation[i] = generateInstrumentationInfo((Instrumentation) p.instrumentation.get(i), flags);
                }
            }
        }
        if ((flags & PARSE_FORCE_SDK) != 0) {
            N = p.permissions.size();
            if (N > 0) {
                pi.permissions = new PermissionInfo[N];
                for (i = SDK_VERSION; i < N; i += PARSE_IS_SYSTEM) {
                    pi.permissions[i] = generatePermissionInfo((Permission) p.permissions.get(i), flags);
                }
            }
            N = p.requestedPermissions.size();
            if (N > 0) {
                pi.requestedPermissions = new String[N];
                pi.requestedPermissionsFlags = new int[N];
                for (i = SDK_VERSION; i < N; i += PARSE_IS_SYSTEM) {
                    String perm = (String) p.requestedPermissions.get(i);
                    pi.requestedPermissions[i] = perm;
                    int[] iArr = pi.requestedPermissionsFlags;
                    iArr[i] = iArr[i] | PARSE_IS_SYSTEM;
                    if (grantedPermissions != null && grantedPermissions.contains(perm)) {
                        iArr = pi.requestedPermissionsFlags;
                        iArr[i] = iArr[i] | PARSE_CHATTY;
                    }
                }
            }
        }
        if ((flags & PARSE_IS_SYSTEM_DIR) != 0) {
            if (p.mRealSignatures != null) {
                if (p.mRealSignatures.length > 0) {
                    N = p.mRealSignatures.length;
                    pi.signatures = new Signature[N];
                    System.arraycopy(p.mRealSignatures, SDK_VERSION, pi.signatures, SDK_VERSION, N);
                }
            }
            if (p.mSignatures != null) {
                N = p.mSignatures.length;
            } else {
                N = SDK_VERSION;
            }
            if (N > 0) {
                pi.signatures = new Signature[N];
                System.arraycopy(p.mSignatures, SDK_VERSION, pi.signatures, SDK_VERSION, N);
            }
        }
        return pi;
    }

    private static Certificate[][] loadCertificates(StrictJarFile jarFile, ZipEntry entry) throws PackageParserException {
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = jarFile.getInputStream(entry);
            readFullyIgnoringContents(autoCloseable);
            Certificate[][] certificateChains = jarFile.getCertificateChains(entry);
            IoUtils.closeQuietly(autoCloseable);
            return certificateChains;
        } catch (Exception e) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed reading " + entry.getName() + " in " + jarFile, e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public static PackageLite parsePackageLite(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackageLite(packageFile, flags);
        }
        return parseMonolithicPackageLite(packageFile, flags);
    }

    private static PackageLite parseMonolithicPackageLite(File packageFile, int flags) throws PackageParserException {
        return new PackageLite(packageFile.getAbsolutePath(), parseApkLite(packageFile, flags), null, null, null);
    }

    private static PackageLite parseClusterPackageLite(File packageDir, int flags) throws PackageParserException {
        File[] files = packageDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            throw new PackageParserException(-100, "No packages found in split");
        }
        String packageName = null;
        int versionCode = SDK_VERSION;
        ArrayMap<String, ApkLite> apks = new ArrayMap();
        int length = files.length;
        for (int i = SDK_VERSION; i < length; i += PARSE_IS_SYSTEM) {
            File file = files[i];
            if (isApkFile(file)) {
                ApkLite lite = parseApkLite(file, flags);
                HwFrameworkFactory.getHwPackageParser().needStopApp(lite.packageName, file);
                if (packageName == null) {
                    packageName = lite.packageName;
                    versionCode = lite.versionCode;
                } else {
                    if (packageName.equals(lite.packageName)) {
                        int i2 = lite.versionCode;
                        if (versionCode != r0) {
                            throw new PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Inconsistent version " + lite.versionCode + " in " + file + "; expected " + versionCode);
                        }
                    }
                    throw new PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Inconsistent package " + lite.packageName + " in " + file + "; expected " + packageName);
                }
                if (apks.put(lite.splitName, lite) != null) {
                    throw new PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Split name " + lite.splitName + " defined more than once; most recent was " + file);
                }
            }
        }
        ApkLite baseApk = (ApkLite) apks.remove(null);
        if (baseApk == null) {
            throw new PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Missing base APK in " + packageDir);
        }
        int size = apks.size();
        String[] strArr = null;
        String[] strArr2 = null;
        int[] iArr = null;
        if (size > 0) {
            strArr2 = new String[size];
            iArr = new int[size];
            strArr = (String[]) apks.keySet().toArray(new String[size]);
            Arrays.sort(strArr, sSplitNameComparator);
            for (int i3 = SDK_VERSION; i3 < size; i3 += PARSE_IS_SYSTEM) {
                strArr2[i3] = ((ApkLite) apks.get(strArr[i3])).codePath;
                iArr[i3] = ((ApkLite) apks.get(strArr[i3])).revisionCode;
            }
        }
        return new PackageLite(packageDir.getAbsolutePath(), baseApk, strArr, strArr2, iArr);
    }

    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        return parsePackage(packageFile, flags, SDK_VERSION);
    }

    public Package parsePackage(File packageFile, int flags, int hwFlags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackage(packageFile, flags, hwFlags);
        }
        return parseMonolithicPackage(packageFile, flags, hwFlags);
    }

    private Package parseClusterPackage(File packageDir, int flags) throws PackageParserException {
        return parseClusterPackage(packageDir, flags, SDK_VERSION);
    }

    private Package parseClusterPackage(File packageDir, int flags, int hwFlags) throws PackageParserException {
        int i = SDK_VERSION;
        PackageLite lite = parseClusterPackageLite(packageDir, SDK_VERSION);
        if (!this.mOnlyCoreApps || lite.coreApp) {
            AssetManager assets = new AssetManager();
            try {
                loadApkIntoAssetManager(assets, lite.baseCodePath, flags);
                if (!ArrayUtils.isEmpty(lite.splitCodePaths)) {
                    String[] strArr = lite.splitCodePaths;
                    int length = strArr.length;
                    while (i < length) {
                        loadApkIntoAssetManager(assets, strArr[i], flags);
                        i += PARSE_IS_SYSTEM;
                    }
                }
                File baseApk = new File(lite.baseCodePath);
                Package pkg = parseBaseApk(baseApk, assets, flags, hwFlags);
                if (pkg == null) {
                    throw new PackageParserException(-100, "Failed to parse base APK: " + baseApk);
                }
                if (!ArrayUtils.isEmpty(lite.splitNames)) {
                    int num = lite.splitNames.length;
                    pkg.splitNames = lite.splitNames;
                    pkg.splitCodePaths = lite.splitCodePaths;
                    pkg.splitRevisionCodes = lite.splitRevisionCodes;
                    pkg.splitFlags = new int[num];
                    pkg.splitPrivateFlags = new int[num];
                    for (int i2 = SDK_VERSION; i2 < num; i2 += PARSE_IS_SYSTEM) {
                        parseSplitApk(pkg, i2, assets, flags);
                    }
                }
                pkg.setCodePath(packageDir.getAbsolutePath());
                pkg.setUse32bitAbi(lite.use32bitAbi);
                return pkg;
            } finally {
                IoUtils.closeQuietly(assets);
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + packageDir);
        }
    }

    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        return parseMonolithicPackage(apkFile, flags, SDK_VERSION);
    }

    public Package parseMonolithicPackage(File apkFile, int flags, int hwFlags) throws PackageParserException {
        PackageLite lite = parseMonolithicPackageLite(apkFile, flags);
        if (this.mOnlyCoreApps) {
            if (lite.coreApp) {
                HwFrameworkFactory.getHwPackageParser().needStopApp(lite.packageName, apkFile);
            } else {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + apkFile);
            }
        }
        AssetManager assets = new AssetManager();
        try {
            Package pkg = parseBaseApk(apkFile, assets, flags, hwFlags);
            HwFrameworkFactory.getHwPackageParser().needStopApp(pkg.packageName, apkFile);
            pkg.setCodePath(apkFile.getAbsolutePath());
            pkg.setUse32bitAbi(lite.use32bitAbi);
            return pkg;
        } finally {
            IoUtils.closeQuietly(assets);
        }
    }

    private static int loadApkIntoAssetManager(AssetManager assets, String apkPath, int flags) throws PackageParserException {
        if ((flags & PARSE_MUST_BE_APK) == 0 || isApkPath(apkPath)) {
            int cookie = assets.addAssetPath(apkPath);
            if (cookie != 0) {
                return cookie;
            }
            throw new PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Failed adding asset path: " + apkPath);
        }
        throw new PackageParserException(-100, "Invalid package file: " + apkPath);
    }

    private Package parseBaseApk(File apkFile, AssetManager assets, int flags) throws PackageParserException {
        return parseBaseApk(apkFile, assets, flags, (int) SDK_VERSION);
    }

    private Package parseBaseApk(File apkFile, AssetManager assets, int flags, int hwFlags) throws PackageParserException {
        PackageParserException e;
        Resources resources;
        Throwable th;
        Throwable e2;
        String apkPath = apkFile.getAbsolutePath();
        String volumeUuid = null;
        if (apkPath.startsWith(MNT_EXPAND)) {
            int end = apkPath.indexOf(47, MNT_EXPAND.length());
            volumeUuid = apkPath.substring(MNT_EXPAND.length(), end);
        }
        this.mParseError = PARSE_IS_SYSTEM;
        this.mArchiveSourcePath = apkFile.getAbsolutePath();
        int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        XmlResourceParser parser;
        try {
            Resources resources2 = new Resources(assets, this.mMetrics, null);
            try {
                assets.setConfiguration(SDK_VERSION, SDK_VERSION, null, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, VERSION.RESOURCES_SDK_INT);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                try {
                    String[] outError = new String[PARSE_IS_SYSTEM];
                    Package pkg = parseBaseApk(resources2, parser, flags, outError, hwFlags);
                    if (pkg == null) {
                        throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[SDK_VERSION]);
                    }
                    pkg.setVolumeUuid(volumeUuid);
                    pkg.setApplicationVolumeUuid(volumeUuid);
                    pkg.setBaseCodePath(apkPath);
                    pkg.setSignatures(null);
                    IoUtils.closeQuietly(parser);
                    return pkg;
                } catch (PackageParserException e3) {
                    e = e3;
                    resources = resources2;
                    try {
                        throw e;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Exception e4) {
                    e2 = e4;
                    resources = resources2;
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
                } catch (Throwable th3) {
                    th = th3;
                    resources = resources2;
                    IoUtils.closeQuietly(parser);
                    throw th;
                }
            } catch (PackageParserException e5) {
                e = e5;
                parser = null;
                resources = resources2;
                throw e;
            } catch (Exception e6) {
                e2 = e6;
                parser = null;
                resources = resources2;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
            } catch (Throwable th4) {
                th = th4;
                parser = null;
                resources = resources2;
                IoUtils.closeQuietly(parser);
                throw th;
            }
        } catch (PackageParserException e7) {
            e = e7;
            parser = null;
            throw e;
        } catch (Exception e8) {
            e2 = e8;
            parser = null;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
        } catch (Throwable th5) {
            th = th5;
            parser = null;
            IoUtils.closeQuietly(parser);
            throw th;
        }
    }

    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags) throws PackageParserException {
        XmlResourceParser parser;
        PackageParserException e;
        Resources res;
        Throwable th;
        Throwable e2;
        String apkPath = pkg.splitCodePaths[splitIndex];
        this.mParseError = PARSE_IS_SYSTEM;
        this.mArchiveSourcePath = apkPath;
        int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        try {
            Resources resources = new Resources(assets, this.mMetrics, null);
            try {
                assets.setConfiguration(SDK_VERSION, SDK_VERSION, null, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, VERSION.RESOURCES_SDK_INT);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
            } catch (PackageParserException e3) {
                e = e3;
                parser = null;
                res = resources;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e4) {
                e2 = e4;
                parser = null;
                res = resources;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
            } catch (Throwable th3) {
                th = th3;
                parser = null;
                res = resources;
                IoUtils.closeQuietly(parser);
                throw th;
            }
            try {
                String[] outError = new String[PARSE_IS_SYSTEM];
                if (parseSplitApk(pkg, resources, parser, flags, splitIndex, outError) == null) {
                    throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[SDK_VERSION]);
                }
                IoUtils.closeQuietly(parser);
            } catch (PackageParserException e5) {
                e = e5;
                res = resources;
                throw e;
            } catch (Exception e6) {
                e2 = e6;
                res = resources;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
            } catch (Throwable th4) {
                th = th4;
                res = resources;
                IoUtils.closeQuietly(parser);
                throw th;
            }
        } catch (PackageParserException e7) {
            e = e7;
            parser = null;
            throw e;
        } catch (Exception e8) {
            e2 = e8;
            parser = null;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
        } catch (Throwable th5) {
            th = th5;
            parser = null;
            IoUtils.closeQuietly(parser);
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Package parseSplitApk(Package pkg, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException, PackageParserException {
        XmlResourceParser attrs = parser;
        parsePackageSplitNames(parser, parser);
        this.mParseInstrumentationArgs = null;
        this.mParseActivityArgs = null;
        this.mParseServiceArgs = null;
        this.mParseProviderArgs = null;
        boolean foundApp = RIGID_PARSER;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!foundApp) {
                    outError[SDK_VERSION] = "<manifest> does not contain an <application>";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
                }
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                if (!parser.getName().equals(TAG_APPLICATION)) {
                    Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                } else if (foundApp) {
                    Slog.w(TAG, "<manifest> has more than one <application>");
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    foundApp = true;
                    if (!parseSplitApplication(pkg, res, parser, flags, splitIndex, outError)) {
                        return null;
                    }
                }
            }
        }
        if (foundApp) {
            outError[SDK_VERSION] = "<manifest> does not contain an <application>";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
        }
        return pkg;
    }

    public static int getApkSigningVersion(Package pkg) {
        try {
            if (ApkSignatureSchemeV2Verifier.hasSignature(pkg.baseCodePath)) {
                return PARSE_CHATTY;
            }
            return PARSE_IS_SYSTEM;
        } catch (IOException e) {
            return SDK_VERSION;
        }
    }

    public static void populateCertificates(Package pkg, Certificate[][] certificates) throws PackageParserException {
        pkg.mCertificates = null;
        pkg.mSignatures = null;
        pkg.mSigningKeys = null;
        pkg.mCertificates = certificates;
        try {
            int i;
            pkg.mSignatures = convertToSignatures(certificates);
            pkg.mSigningKeys = new ArraySet(certificates.length);
            for (i = SDK_VERSION; i < certificates.length; i += PARSE_IS_SYSTEM) {
                pkg.mSigningKeys.add(certificates[i][SDK_VERSION].getPublicKey());
            }
            int childCount = pkg.childPackages != null ? pkg.childPackages.size() : SDK_VERSION;
            for (i = SDK_VERSION; i < childCount; i += PARSE_IS_SYSTEM) {
                Package childPkg = (Package) pkg.childPackages.get(i);
                childPkg.mCertificates = pkg.mCertificates;
                childPkg.mSignatures = pkg.mSignatures;
                childPkg.mSigningKeys = pkg.mSigningKeys;
            }
        } catch (CertificateEncodingException e) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + pkg.baseCodePath, e);
        }
    }

    public static void collectCertificates(Package pkg, int parseFlags) throws PackageParserException {
        collectCertificatesInternal(pkg, parseFlags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : SDK_VERSION;
        for (int i = SDK_VERSION; i < childCount; i += PARSE_IS_SYSTEM) {
            Package childPkg = (Package) pkg.childPackages.get(i);
            childPkg.mCertificates = pkg.mCertificates;
            childPkg.mSignatures = pkg.mSignatures;
            childPkg.mSigningKeys = pkg.mSigningKeys;
        }
    }

    private static void collectCertificatesInternal(Package pkg, int parseFlags) throws PackageParserException {
        pkg.mCertificates = null;
        pkg.mSignatures = null;
        pkg.mSigningKeys = null;
        Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "collectCertificates");
        try {
            collectCertificates(pkg, new File(pkg.baseCodePath), parseFlags);
            if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
                for (int i = SDK_VERSION; i < pkg.splitCodePaths.length; i += PARSE_IS_SYSTEM) {
                    collectCertificates(pkg, new File(pkg.splitCodePaths[i]), parseFlags);
                }
            }
            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
        } catch (Throwable th) {
            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
        }
    }

    private static void collectCertificates(Package pkg, File apkFile, int parseFlags) throws PackageParserException {
        Exception e;
        int i;
        GeneralSecurityException e2;
        Throwable th;
        String apkPath = apkFile.getAbsolutePath();
        boolean verified = RIGID_PARSER;
        Certificate[][] certificateArr = null;
        Signature[] signatureArr = null;
        try {
            Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "verifyV2");
            certificateArr = ApkSignatureSchemeV2Verifier.verify(apkPath);
            signatureArr = convertToSignatures(certificateArr);
            verified = true;
            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
        } catch (SignatureNotFoundException e3) {
            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
        } catch (Exception e4) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e4);
        } catch (Throwable th2) {
            Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
        }
        if (verified) {
            if (pkg.mCertificates == null) {
                pkg.mCertificates = certificateArr;
                pkg.mSignatures = signatureArr;
                pkg.mSigningKeys = new ArraySet(certificateArr.length);
                i = SDK_VERSION;
                while (true) {
                    int length = certificateArr.length;
                    if (i >= r0) {
                        break;
                    }
                    pkg.mSigningKeys.add(certificateArr[i][SDK_VERSION].getPublicKey());
                    i += PARSE_IS_SYSTEM;
                }
            } else {
                if (!Signature.areExactMatch(pkg.mSignatures, signatureArr)) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, apkPath + " has mismatched certificates");
                }
            }
        }
        StrictJarFile strictJarFile = null;
        try {
            Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "strictJarFileCtor");
            StrictJarFile strictJarFile2 = new StrictJarFile(apkPath, verified ? RIGID_PARSER : true, (parseFlags & PARSE_IS_SYSTEM_DIR) == 0 ? true : RIGID_PARSER);
            try {
                Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                ZipEntry manifestEntry = strictJarFile2.findEntry(ANDROID_MANIFEST_FILENAME);
                if (manifestEntry == null) {
                    throw new PackageParserException(KeymasterDefs.KM_ERROR_VERSION_MISMATCH, "Package " + apkPath + " has no manifest");
                } else if (verified) {
                    closeQuietly(strictJarFile2);
                } else {
                    ZipEntry entry;
                    Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "verifyV1");
                    List<ZipEntry> toVerify = new ArrayList();
                    toVerify.add(manifestEntry);
                    if ((parseFlags & PARSE_IS_SYSTEM_DIR) == 0) {
                        Iterator<ZipEntry> i2 = strictJarFile2.iterator();
                        while (i2.hasNext()) {
                            entry = (ZipEntry) i2.next();
                            if (!entry.isDirectory()) {
                                String entryName = entry.getName();
                                if (!entryName.startsWith("META-INF/")) {
                                    if (!entryName.equals(ANDROID_MANIFEST_FILENAME)) {
                                        toVerify.add(entry);
                                    }
                                }
                            }
                        }
                    }
                    for (ZipEntry entry2 : toVerify) {
                        Certificate[][] entryCerts = loadCertificates(strictJarFile2, entry2);
                        if (ArrayUtils.isEmpty(entryCerts)) {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + entry2.getName());
                        }
                        Signature[] entrySignatures = convertToSignatures(entryCerts);
                        if (pkg.mCertificates == null) {
                            pkg.mCertificates = entryCerts;
                            pkg.mSignatures = entrySignatures;
                            pkg.mSigningKeys = new ArraySet();
                            i = SDK_VERSION;
                            while (true) {
                                length = entryCerts.length;
                                if (i >= r0) {
                                    break;
                                }
                                pkg.mSigningKeys.add(entryCerts[i][SDK_VERSION].getPublicKey());
                                i += PARSE_IS_SYSTEM;
                            }
                        } else {
                            if (!Signature.areExactMatch(pkg.mSignatures, entrySignatures)) {
                                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, "Package " + apkPath + " has mismatched certificates at entry " + entry2.getName());
                            }
                        }
                    }
                    Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                    closeQuietly(strictJarFile2);
                }
            } catch (GeneralSecurityException e5) {
                e2 = e5;
                strictJarFile = strictJarFile2;
                try {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e2);
                } catch (Throwable th3) {
                    th = th3;
                    closeQuietly(strictJarFile);
                    throw th;
                }
            } catch (IOException e6) {
                e4 = e6;
                strictJarFile = strictJarFile2;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e4);
            } catch (Throwable th4) {
                th = th4;
                strictJarFile = strictJarFile2;
                closeQuietly(strictJarFile);
                throw th;
            }
        } catch (GeneralSecurityException e7) {
            e2 = e7;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e2);
        } catch (IOException e8) {
            e4 = e8;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e4);
        }
    }

    private static Signature[] convertToSignatures(Certificate[][] certs) throws CertificateEncodingException {
        Signature[] res = new Signature[certs.length];
        for (int i = SDK_VERSION; i < certs.length; i += PARSE_IS_SYSTEM) {
            res[i] = new Signature(certs[i]);
        }
        return res;
    }

    public static ApkLite parseApkLite(File apkFile, int flags) throws PackageParserException {
        AssetManager assets;
        XmlResourceParser parser;
        Throwable e;
        Throwable th;
        String apkPath = apkFile.getAbsolutePath();
        try {
            assets = new AssetManager();
            try {
                assets.setConfiguration(SDK_VERSION, SDK_VERSION, null, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, SDK_VERSION, VERSION.RESOURCES_SDK_INT);
                int cookie = assets.addAssetPath(apkPath);
                if (cookie == 0) {
                    throw new PackageParserException(-100, "Failed to parse " + apkPath);
                }
                Signature[] signatures;
                Certificate[][] certificateArr;
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.setToDefaults();
                Resources res = new Resources(assets, metrics, null);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                if ((flags & PARSE_COLLECT_CERTIFICATES) != 0) {
                    try {
                        Package packageR = new Package(null);
                        Trace.traceBegin(Trace.TRACE_TAG_PACKAGE_MANAGER, "collectCertificates");
                        collectCertificates(packageR, apkFile, SDK_VERSION);
                        Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                        signatures = packageR.mSignatures;
                        certificateArr = packageR.mCertificates;
                    } catch (XmlPullParserException e2) {
                        e = e2;
                        try {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e);
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(parser);
                            IoUtils.closeQuietly(assets);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        Trace.traceEnd(Trace.TRACE_TAG_PACKAGE_MANAGER);
                    }
                }
                signatures = null;
                certificateArr = null;
                XmlResourceParser attrs = parser;
                ApkLite parseApkLite = parseApkLite(apkPath, res, parser, parser, flags, signatures, certificateArr);
                IoUtils.closeQuietly(parser);
                IoUtils.closeQuietly(assets);
                return parseApkLite;
            } catch (XmlPullParserException e3) {
                e = e3;
                parser = null;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e);
            } catch (Throwable th4) {
                th = th4;
                parser = null;
                IoUtils.closeQuietly(parser);
                IoUtils.closeQuietly(assets);
                throw th;
            }
        } catch (XmlPullParserException e4) {
            e = e4;
            parser = null;
            assets = null;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e);
        } catch (Throwable th5) {
            th = th5;
            parser = null;
            assets = null;
            IoUtils.closeQuietly(parser);
            IoUtils.closeQuietly(assets);
            throw th;
        }
    }

    private static String validateName(String name, boolean requireSeparator, boolean requireFilename) {
        int N = name.length();
        boolean hasSep = RIGID_PARSER;
        boolean front = true;
        for (int i = SDK_VERSION; i < N; i += PARSE_IS_SYSTEM) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = RIGID_PARSER;
            } else if (front || ((c < '0' || c > '9') && c != '_')) {
                if (c != '.') {
                    return "bad character '" + c + "'";
                }
                hasSep = true;
                front = true;
            }
        }
        if (requireFilename && !FileUtils.isValidExtFilename(name)) {
            return "Invalid filename";
        }
        String str = (hasSep || !requireSeparator) ? null : "must have at least one '.' separator";
        return str;
    }

    private static Pair<String, String> parsePackageSplitNames(XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException, PackageParserException {
        int type;
        do {
            type = parser.next();
            if (type == PARSE_CHATTY) {
                break;
            }
        } while (type != PARSE_IS_SYSTEM);
        if (type != PARSE_CHATTY) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No start tag found");
        } else if (parser.getName().equals(TAG_MANIFEST)) {
            String error;
            String packageName = attrs.getAttributeValue(null, TAG_PACKAGE);
            if (!(ZenModeConfig.SYSTEM_AUTHORITY.equals(packageName) || "androidhwext".equals(packageName))) {
                error = validateName(packageName, true, true);
                if (error != null) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest package: " + error);
                }
            }
            Object splitName = attrs.getAttributeValue(null, "split");
            if (splitName != null) {
                if (splitName.length() == 0) {
                    splitName = null;
                } else {
                    error = validateName(splitName, RIGID_PARSER, RIGID_PARSER);
                    if (error != null) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest split: " + error);
                    }
                }
            }
            String intern = packageName.intern();
            if (splitName != null) {
                splitName = splitName.intern();
            }
            return Pair.create(intern, splitName);
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No <manifest> tag");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ApkLite parseApkLite(String codePath, Resources res, XmlPullParser parser, AttributeSet attrs, int flags, Signature[] signatures, Certificate[][] certificates) throws IOException, XmlPullParserException, PackageParserException {
        int i;
        Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs);
        int installLocation = PARSE_DEFAULT_INSTALL_LOCATION;
        int versionCode = SDK_VERSION;
        int revisionCode = SDK_VERSION;
        boolean coreApp = RIGID_PARSER;
        boolean multiArch = RIGID_PARSER;
        boolean use32bitAbi = RIGID_PARSER;
        boolean extractNativeLibs = true;
        for (i = SDK_VERSION; i < attrs.getAttributeCount(); i += PARSE_IS_SYSTEM) {
            String attr = attrs.getAttributeName(i);
            if (attr.equals("installLocation")) {
                installLocation = attrs.getAttributeIntValue(i, PARSE_DEFAULT_INSTALL_LOCATION);
            } else {
                if (attr.equals(HwFrameworkMonitor.KEY_VERSION_CODE)) {
                    versionCode = attrs.getAttributeIntValue(i, SDK_VERSION);
                } else {
                    if (attr.equals("revisionCode")) {
                        revisionCode = attrs.getAttributeIntValue(i, SDK_VERSION);
                    } else {
                        if (attr.equals("coreApp")) {
                            coreApp = attrs.getAttributeBooleanValue(i, RIGID_PARSER);
                        }
                    }
                }
            }
        }
        int searchDepth = parser.getDepth() + PARSE_IS_SYSTEM;
        List<VerifierInfo> verifiers = new ArrayList();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() < searchDepth)) {
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                if (parser.getDepth() == searchDepth && "package-verifier".equals(parser.getName())) {
                    VerifierInfo verifier = parseVerifier(res, parser, attrs, flags);
                    if (verifier != null) {
                        verifiers.add(verifier);
                    }
                }
                if (parser.getDepth() == searchDepth && TAG_APPLICATION.equals(parser.getName())) {
                    for (i = SDK_VERSION; i < attrs.getAttributeCount(); i += PARSE_IS_SYSTEM) {
                        attr = attrs.getAttributeName(i);
                        if ("multiArch".equals(attr)) {
                            multiArch = attrs.getAttributeBooleanValue(i, RIGID_PARSER);
                        }
                        if ("use32bitAbi".equals(attr)) {
                            use32bitAbi = attrs.getAttributeBooleanValue(i, RIGID_PARSER);
                        }
                        if ("extractNativeLibs".equals(attr)) {
                            extractNativeLibs = attrs.getAttributeBooleanValue(i, true);
                        }
                    }
                }
            }
        }
        return new ApkLite(codePath, (String) packageSplit.first, (String) packageSplit.second, versionCode, revisionCode, installLocation, verifiers, signatures, certificates, coreApp, multiArch, use32bitAbi, extractNativeLibs);
    }

    public static Signature stringToSignature(String str) {
        int N = str.length();
        byte[] sig = new byte[N];
        for (int i = SDK_VERSION; i < N; i += PARSE_IS_SYSTEM) {
            sig[i] = (byte) str.charAt(i);
        }
        return new Signature(sig);
    }

    private boolean parseBaseApkChild(Package parentPkg, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        if (parentPkg.childPackages == null || parentPkg.childPackages.size() + PARSE_CHATTY <= MAX_PACKAGES_PER_APK) {
            String childPackageName = parser.getAttributeValue(null, TAG_PACKAGE);
            if (validateName(childPackageName, true, RIGID_PARSER) != null) {
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
                return RIGID_PARSER;
            } else if (childPackageName.equals(parentPkg.packageName)) {
                message = "Child package name cannot be equal to parent package name: " + parentPkg.packageName;
                Slog.w(TAG, message);
                outError[SDK_VERSION] = message;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return RIGID_PARSER;
            } else if (parentPkg.hasChildPackage(childPackageName)) {
                message = "Duplicate child package:" + childPackageName;
                Slog.w(TAG, message);
                outError[SDK_VERSION] = message;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return RIGID_PARSER;
            } else {
                Package childPkg = new Package(childPackageName);
                childPkg.mVersionCode = parentPkg.mVersionCode;
                childPkg.baseRevisionCode = parentPkg.baseRevisionCode;
                childPkg.mVersionName = parentPkg.mVersionName;
                childPkg.applicationInfo.targetSdkVersion = parentPkg.applicationInfo.targetSdkVersion;
                childPkg.applicationInfo.minSdkVersion = parentPkg.applicationInfo.minSdkVersion;
                childPkg = parseBaseApkCommon(childPkg, CHILD_PACKAGE_TAGS, res, parser, flags, outError);
                if (childPkg == null) {
                    return RIGID_PARSER;
                }
                if (parentPkg.childPackages == null) {
                    parentPkg.childPackages = new ArrayList();
                }
                parentPkg.childPackages.add(childPkg);
                childPkg.parentPackage = parentPkg;
                return true;
            }
        }
        outError[SDK_VERSION] = "Maximum number of packages per APK is: 5";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return RIGID_PARSER;
    }

    private Package parseBaseApk(Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        return parseBaseApk(res, parser, flags, outError, SDK_VERSION);
    }

    private Package parseBaseApk(Resources res, XmlResourceParser parser, int flags, String[] outError, int hwFlags) throws XmlPullParserException, IOException {
        try {
            Pair<String, String> packageSplit = parsePackageSplitNames(parser, parser);
            String pkgName = packageSplit.first;
            String splitName = packageSplit.second;
            if (TextUtils.isEmpty(splitName)) {
                Package pkg = new Package(pkgName);
                pkg.applicationInfo.hwFlags = hwFlags;
                TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifest);
                int integer = sa.getInteger(PARSE_IS_SYSTEM, SDK_VERSION);
                pkg.applicationInfo.versionCode = integer;
                pkg.mVersionCode = integer;
                pkg.baseRevisionCode = sa.getInteger(MAX_PACKAGES_PER_APK, SDK_VERSION);
                pkg.mVersionName = sa.getNonConfigurationString(PARSE_CHATTY, SDK_VERSION);
                if (pkg.mVersionName != null) {
                    pkg.mVersionName = pkg.mVersionName.intern();
                }
                pkg.coreApp = parser.getAttributeBooleanValue(null, "coreApp", RIGID_PARSER);
                sa.recycle();
                return parseBaseApkCommon(pkg, null, res, parser, flags, outError);
            }
            outError[SDK_VERSION] = "Expected base APK, but found split " + splitName;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        } catch (PackageParserException e) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Package parseBaseApkCommon(Package pkg, Set<String> acceptedTags, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        this.mParseInstrumentationArgs = null;
        this.mParseActivityArgs = null;
        this.mParseServiceArgs = null;
        this.mParseProviderArgs = null;
        boolean foundApp = RIGID_PARSER;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifest);
        String str = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
        if (str != null && str.length() > 0) {
            String nameError = validateName(str, true, RIGID_PARSER);
            if (nameError == null || ZenModeConfig.SYSTEM_AUTHORITY.equals(pkg.packageName) || "androidhwext".equals(pkg.packageName)) {
                pkg.mSharedUserId = str.intern();
                pkg.mSharedUserLabel = sa.getResourceId(3, SDK_VERSION);
            } else {
                outError[SDK_VERSION] = "<manifest> specifies bad sharedUserId name \"" + str + "\": " + nameError;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                return null;
            }
        }
        pkg.installLocation = sa.getInteger(PARSE_MUST_BE_APK, PARSE_DEFAULT_INSTALL_LOCATION);
        pkg.applicationInfo.installLocation = pkg.installLocation;
        if ((flags & PARSE_FORWARD_LOCK) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= PARSE_MUST_BE_APK;
        }
        if ((flags & PARSE_EXTERNAL_STORAGE) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.flags |= Root.FLAG_HAS_SETTINGS;
        }
        if ((flags & PARSE_IS_EPHEMERAL) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= PARSE_TRUSTED_OVERLAY;
        }
        int supportsSmallScreens = PARSE_IS_SYSTEM;
        int supportsNormalScreens = PARSE_IS_SYSTEM;
        int supportsLargeScreens = PARSE_IS_SYSTEM;
        int supportsXLargeScreens = PARSE_IS_SYSTEM;
        int resizeable = PARSE_IS_SYSTEM;
        int anyDensity = PARSE_IS_SYSTEM;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!foundApp && pkg.instrumentation.size() == 0) {
                    outError[SDK_VERSION] = "<manifest> does not contain an <application> or <instrumentation>";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
                }
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                String tagName = parser.getName();
                if (acceptedTags == null || acceptedTags.contains(tagName)) {
                    if (!tagName.equals(TAG_APPLICATION)) {
                        if (tagName.equals(TAG_OVERLAY)) {
                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestResourceOverlay);
                            pkg.mOverlayTarget = sa.getString(PARSE_IS_SYSTEM);
                            pkg.mOverlayPriority = sa.getInt(SDK_VERSION, PARSE_DEFAULT_INSTALL_LOCATION);
                            sa.recycle();
                            if (pkg.mOverlayTarget == null) {
                                outError[SDK_VERSION] = "<overlay> does not specify a target package";
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return null;
                            } else if (pkg.mOverlayPriority < 0 || pkg.mOverlayPriority > 9999) {
                                outError[SDK_VERSION] = "<overlay> priority must be between 0 and 9999";
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            } else {
                                XmlUtils.skipCurrentTag(parser);
                            }
                        } else {
                            if (!tagName.equals(TAG_KEY_SETS)) {
                                if (!tagName.equals(TAG_PERMISSION_GROUP)) {
                                    if (!tagName.equals(TAG_PERMISSION)) {
                                        if (!tagName.equals(TAG_PERMISSION_TREE)) {
                                            if (!tagName.equals(TAG_USES_PERMISSION)) {
                                                if (!tagName.equals(TAG_USES_PERMISSION_SDK_M)) {
                                                    if (!tagName.equals(TAG_USES_PERMISSION_SDK_23)) {
                                                        ConfigurationInfo cPref;
                                                        if (tagName.equals(TAG_USES_CONFIGURATION)) {
                                                            cPref = new ConfigurationInfo();
                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesConfiguration);
                                                            cPref.reqTouchScreen = sa.getInt(SDK_VERSION, SDK_VERSION);
                                                            cPref.reqKeyboardType = sa.getInt(PARSE_IS_SYSTEM, SDK_VERSION);
                                                            if (sa.getBoolean(PARSE_CHATTY, RIGID_PARSER)) {
                                                                cPref.reqInputFeatures |= PARSE_IS_SYSTEM;
                                                            }
                                                            cPref.reqNavigation = sa.getInt(3, SDK_VERSION);
                                                            if (sa.getBoolean(PARSE_MUST_BE_APK, RIGID_PARSER)) {
                                                                cPref.reqInputFeatures |= PARSE_CHATTY;
                                                            }
                                                            sa.recycle();
                                                            pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else {
                                                            if (tagName.equals(TAG_USES_FEATURE)) {
                                                                FeatureInfo fi = parseUsesFeature(res, parser);
                                                                pkg.reqFeatures = ArrayUtils.add(pkg.reqFeatures, fi);
                                                                if (fi.name == null) {
                                                                    cPref = new ConfigurationInfo();
                                                                    cPref.reqGlEsVersion = fi.reqGlEsVersion;
                                                                    pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);
                                                                }
                                                                XmlUtils.skipCurrentTag(parser);
                                                            } else {
                                                                if (tagName.equals(TAG_FEATURE_GROUP)) {
                                                                    FeatureGroupInfo group = new FeatureGroupInfo();
                                                                    ArrayList features = null;
                                                                    int innerDepth = parser.getDepth();
                                                                    while (true) {
                                                                        type = parser.next();
                                                                        if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= innerDepth)) {
                                                                            if (features != null) {
                                                                                group.features = new FeatureInfo[features.size()];
                                                                                group.features = (FeatureInfo[]) features.toArray(group.features);
                                                                            }
                                                                        } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                                                                            String innerTagName = parser.getName();
                                                                            if (innerTagName.equals(TAG_USES_FEATURE)) {
                                                                                FeatureInfo featureInfo = parseUsesFeature(res, parser);
                                                                                featureInfo.flags |= PARSE_IS_SYSTEM;
                                                                                features = ArrayUtils.add(features, featureInfo);
                                                                            } else {
                                                                                Slog.w(TAG, "Unknown element under <feature-group>: " + innerTagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                                            }
                                                                            XmlUtils.skipCurrentTag(parser);
                                                                        }
                                                                    }
                                                                    if (features != null) {
                                                                        group.features = new FeatureInfo[features.size()];
                                                                        group.features = (FeatureInfo[]) features.toArray(group.features);
                                                                    }
                                                                    pkg.featureGroups = ArrayUtils.add(pkg.featureGroups, group);
                                                                } else {
                                                                    if (tagName.equals(TAG_USES_SDK)) {
                                                                        if (SDK_VERSION > 0) {
                                                                            boolean allowedCodename;
                                                                            String[] strArr;
                                                                            int length;
                                                                            int i;
                                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesSdk);
                                                                            int minVers = PARSE_IS_SYSTEM;
                                                                            String str2 = null;
                                                                            int targetVers = SDK_VERSION;
                                                                            String targetCode = null;
                                                                            TypedValue val = sa.peekValue(SDK_VERSION);
                                                                            if (val != null) {
                                                                                if (val.type != 3 || val.string == null) {
                                                                                    minVers = val.data;
                                                                                    targetVers = minVers;
                                                                                } else {
                                                                                    str2 = val.string.toString();
                                                                                    targetCode = str2;
                                                                                }
                                                                            }
                                                                            val = sa.peekValue(PARSE_IS_SYSTEM);
                                                                            if (val != null) {
                                                                                if (val.type != 3 || val.string == null) {
                                                                                    targetVers = val.data;
                                                                                } else {
                                                                                    targetCode = val.string.toString();
                                                                                    if (str2 == null) {
                                                                                        str2 = targetCode;
                                                                                    }
                                                                                }
                                                                            }
                                                                            sa.recycle();
                                                                            if ("N".equals(targetCode) || "N".equals(str2)) {
                                                                                Slog.w(TAG, "pkg is :" + pkg.packageName + "targetCode is :" + targetCode + "minCode is :" + str2);
                                                                                str2 = null;
                                                                                targetCode = null;
                                                                                minVers = 24;
                                                                                targetVers = 24;
                                                                            }
                                                                            if (str2 != null) {
                                                                                allowedCodename = RIGID_PARSER;
                                                                                strArr = SDK_CODENAMES;
                                                                                length = strArr.length;
                                                                                for (i = SDK_VERSION; i < length; i += PARSE_IS_SYSTEM) {
                                                                                    if (str2.equals(strArr[i])) {
                                                                                        allowedCodename = true;
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                if (!allowedCodename) {
                                                                                    break;
                                                                                }
                                                                                pkg.applicationInfo.minSdkVersion = Events.EVENT_FLAG_START;
                                                                            } else if (minVers > SDK_VERSION) {
                                                                                outError[SDK_VERSION] = "Requires newer sdk version #" + minVers + " (current version is #" + SDK_VERSION + ")";
                                                                                this.mParseError = -12;
                                                                                return null;
                                                                            } else {
                                                                                pkg.applicationInfo.minSdkVersion = minVers;
                                                                            }
                                                                            if (targetCode != null) {
                                                                                allowedCodename = RIGID_PARSER;
                                                                                strArr = SDK_CODENAMES;
                                                                                length = strArr.length;
                                                                                for (i = SDK_VERSION; i < length; i += PARSE_IS_SYSTEM) {
                                                                                    if (targetCode.equals(strArr[i])) {
                                                                                        allowedCodename = true;
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                if (!allowedCodename) {
                                                                                    break;
                                                                                }
                                                                                pkg.applicationInfo.targetSdkVersion = Events.EVENT_FLAG_START;
                                                                            } else {
                                                                                pkg.applicationInfo.targetSdkVersion = targetVers;
                                                                            }
                                                                        }
                                                                        XmlUtils.skipCurrentTag(parser);
                                                                    } else {
                                                                        if (tagName.equals(TAG_SUPPORT_SCREENS)) {
                                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestSupportsScreens);
                                                                            pkg.applicationInfo.requiresSmallestWidthDp = sa.getInteger(6, SDK_VERSION);
                                                                            pkg.applicationInfo.compatibleWidthLimitDp = sa.getInteger(7, SDK_VERSION);
                                                                            pkg.applicationInfo.largestWidthLimitDp = sa.getInteger(PARSE_IGNORE_PROCESSES, SDK_VERSION);
                                                                            supportsSmallScreens = sa.getInteger(PARSE_IS_SYSTEM, supportsSmallScreens);
                                                                            supportsNormalScreens = sa.getInteger(PARSE_CHATTY, supportsNormalScreens);
                                                                            supportsLargeScreens = sa.getInteger(3, supportsLargeScreens);
                                                                            supportsXLargeScreens = sa.getInteger(MAX_PACKAGES_PER_APK, supportsXLargeScreens);
                                                                            resizeable = sa.getInteger(PARSE_MUST_BE_APK, resizeable);
                                                                            anyDensity = sa.getInteger(SDK_VERSION, anyDensity);
                                                                            sa.recycle();
                                                                            XmlUtils.skipCurrentTag(parser);
                                                                        } else {
                                                                            String name;
                                                                            if (tagName.equals(TAG_PROTECTED_BROADCAST)) {
                                                                                sa = res.obtainAttributes(parser, R.styleable.AndroidManifestProtectedBroadcast);
                                                                                name = sa.getNonResourceString(SDK_VERSION);
                                                                                sa.recycle();
                                                                                if (!(name == null || (flags & PARSE_IS_SYSTEM) == 0)) {
                                                                                    if (pkg.protectedBroadcasts == null) {
                                                                                        pkg.protectedBroadcasts = new ArrayList();
                                                                                    }
                                                                                    if (!pkg.protectedBroadcasts.contains(name)) {
                                                                                        pkg.protectedBroadcasts.add(name.intern());
                                                                                    }
                                                                                }
                                                                                XmlUtils.skipCurrentTag(parser);
                                                                            } else {
                                                                                if (!tagName.equals(TAG_INSTRUMENTATION)) {
                                                                                    if (tagName.equals(TAG_ORIGINAL_PACKAGE)) {
                                                                                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestOriginalPackage);
                                                                                        String orig = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
                                                                                        if (!pkg.packageName.equals(orig)) {
                                                                                            if (pkg.mOriginalPackages == null) {
                                                                                                pkg.mOriginalPackages = new ArrayList();
                                                                                                pkg.mRealPackage = pkg.packageName;
                                                                                            }
                                                                                            pkg.mOriginalPackages.add(orig);
                                                                                        }
                                                                                        sa.recycle();
                                                                                        XmlUtils.skipCurrentTag(parser);
                                                                                    } else {
                                                                                        if (tagName.equals(TAG_ADOPT_PERMISSIONS)) {
                                                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestOriginalPackage);
                                                                                            name = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
                                                                                            sa.recycle();
                                                                                            if (name != null) {
                                                                                                if (pkg.mAdoptPermissions == null) {
                                                                                                    pkg.mAdoptPermissions = new ArrayList();
                                                                                                }
                                                                                                pkg.mAdoptPermissions.add(name);
                                                                                            }
                                                                                            XmlUtils.skipCurrentTag(parser);
                                                                                        } else {
                                                                                            if (tagName.equals(TAG_USES_GL_TEXTURE)) {
                                                                                                XmlUtils.skipCurrentTag(parser);
                                                                                            } else {
                                                                                                if (tagName.equals(TAG_COMPATIBLE_SCREENS)) {
                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                } else {
                                                                                                    if (tagName.equals(TAG_SUPPORTS_INPUT)) {
                                                                                                        XmlUtils.skipCurrentTag(parser);
                                                                                                    } else {
                                                                                                        if (tagName.equals(TAG_EAT_COMMENT)) {
                                                                                                            XmlUtils.skipCurrentTag(parser);
                                                                                                        } else {
                                                                                                            if (tagName.equals(TAG_PACKAGE)) {
                                                                                                                XmlUtils.skipCurrentTag(parser);
                                                                                                            } else {
                                                                                                                if (tagName.equals(TAG_RESTRICT_UPDATE)) {
                                                                                                                    if ((flags & PARSE_IS_SYSTEM_DIR) != 0) {
                                                                                                                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestRestrictUpdate);
                                                                                                                        String hash = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
                                                                                                                        sa.recycle();
                                                                                                                        pkg.restrictUpdateHash = null;
                                                                                                                        if (hash != null) {
                                                                                                                            int hashLength = hash.length();
                                                                                                                            byte[] hashBytes = new byte[(hashLength / PARSE_CHATTY)];
                                                                                                                            for (int i2 = SDK_VERSION; i2 < hashLength; i2 += PARSE_CHATTY) {
                                                                                                                                hashBytes[i2 / PARSE_CHATTY] = (byte) ((Character.digit(hash.charAt(i2), PARSE_FORWARD_LOCK) << PARSE_MUST_BE_APK) + Character.digit(hash.charAt(i2 + PARSE_IS_SYSTEM), PARSE_FORWARD_LOCK));
                                                                                                                            }
                                                                                                                            pkg.restrictUpdateHash = hashBytes;
                                                                                                                        }
                                                                                                                    }
                                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                                } else {
                                                                                                                    Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                } else if (parseInstrumentation(pkg, res, parser, outError) == null) {
                                                                                    return null;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!parseUsesPermission(pkg, res, parser)) {
                                                    return null;
                                                }
                                            } else if (!parseUsesPermission(pkg, res, parser)) {
                                                return null;
                                            }
                                        } else if (parsePermissionTree(pkg, res, parser, outError) == null) {
                                            return null;
                                        }
                                    } else if (parsePermission(pkg, res, parser, outError) == null) {
                                        return null;
                                    }
                                } else if (parsePermissionGroup(pkg, flags, res, parser, outError) == null) {
                                    return null;
                                }
                            } else if (!parseKeySets(pkg, res, parser, outError)) {
                                return null;
                            }
                        }
                    } else if (foundApp) {
                        Slog.w(TAG, "<manifest> has more than one <application>");
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        foundApp = true;
                        if (!parseBaseApplication(pkg, res, parser, flags, outError)) {
                            return null;
                        }
                    }
                } else {
                    Slog.w(TAG, "Skipping unsupported element under <manifest>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        outError[SDK_VERSION] = "<overlay> priority must be between 0 and 9999";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private FeatureInfo parseUsesFeature(Resources res, AttributeSet attrs) {
        FeatureInfo fi = new FeatureInfo();
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesFeature);
        fi.name = sa.getNonResourceString(SDK_VERSION);
        fi.version = sa.getInt(3, SDK_VERSION);
        if (fi.name == null) {
            fi.reqGlEsVersion = sa.getInt(PARSE_IS_SYSTEM, SDK_VERSION);
        }
        if (sa.getBoolean(PARSE_CHATTY, true)) {
            fi.flags |= PARSE_IS_SYSTEM;
        }
        sa.recycle();
        return fi;
    }

    private boolean parseUsesPermission(Package pkg, Resources res, XmlResourceParser parser) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesPermission);
        String name = sa.getNonResourceString(SDK_VERSION);
        int maxSdkVersion = SDK_VERSION;
        TypedValue val = sa.peekValue(PARSE_IS_SYSTEM);
        if (val != null && val.type >= PARSE_FORWARD_LOCK && val.type <= 31) {
            maxSdkVersion = val.data;
        }
        sa.recycle();
        if ((maxSdkVersion == 0 || maxSdkVersion >= VERSION.RESOURCES_SDK_INT) && name != null) {
            if (pkg.requestedPermissions.indexOf(name) == PARSE_DEFAULT_INSTALL_LOCATION) {
                pkg.requestedPermissions.add(name.intern());
            } else {
                Slog.w(TAG, "Ignoring duplicate uses-permissions/uses-permissions-sdk-m: " + name + " in package: " + pkg.packageName + " at: " + parser.getPositionDescription());
            }
        }
        XmlUtils.skipCurrentTag(parser);
        return true;
    }

    private static String buildClassName(String pkg, CharSequence clsSeq, String[] outError) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            outError[SDK_VERSION] = "Empty class name in package " + pkg;
            return null;
        }
        String cls = clsSeq.toString();
        char c = cls.charAt(SDK_VERSION);
        if (c == '.') {
            return (pkg + cls).intern();
        }
        if (cls.indexOf(46) < 0) {
            StringBuilder b = new StringBuilder(pkg);
            b.append('.');
            b.append(cls);
            return b.toString().intern();
        } else if (c >= 'a' && c <= 'z') {
            return cls.intern();
        } else {
            outError[SDK_VERSION] = "Bad class name " + cls + " in package " + pkg;
            return null;
        }
    }

    private static String buildCompoundName(String pkg, CharSequence procSeq, String type, String[] outError) {
        String proc = procSeq.toString();
        char c = proc.charAt(SDK_VERSION);
        String nameError;
        if (pkg == null || c != ':') {
            nameError = validateName(proc, true, RIGID_PARSER);
            if (nameError == null || HwThemeManager.HWT_USER_SYSTEM.equals(proc)) {
                return proc.intern();
            }
            outError[SDK_VERSION] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError;
            return null;
        } else if (proc.length() < PARSE_CHATTY) {
            outError[SDK_VERSION] = "Bad " + type + " name " + proc + " in package " + pkg + ": must be at least two characters";
            return null;
        } else {
            nameError = validateName(proc.substring(PARSE_IS_SYSTEM), RIGID_PARSER, RIGID_PARSER);
            if (nameError == null) {
                return (pkg + proc).intern();
            }
            outError[SDK_VERSION] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError;
            return null;
        }
    }

    private static String buildProcessName(String pkg, String defProc, CharSequence procSeq, int flags, String[] separateProcesses, String[] outError) {
        if ((flags & PARSE_IGNORE_PROCESSES) == 0 || HwThemeManager.HWT_USER_SYSTEM.equals(procSeq)) {
            if (separateProcesses != null) {
                for (int i = separateProcesses.length + PARSE_DEFAULT_INSTALL_LOCATION; i >= 0; i += PARSE_DEFAULT_INSTALL_LOCATION) {
                    String sp = separateProcesses[i];
                    if (sp.equals(pkg) || sp.equals(defProc) || sp.equals(procSeq)) {
                        return pkg;
                    }
                }
            }
            if (procSeq == null || procSeq.length() <= 0) {
                return defProc;
            }
            return buildCompoundName(pkg, procSeq, "process", outError);
        }
        if (defProc == null) {
            defProc = pkg;
        }
        return defProc;
    }

    private static String buildTaskAffinityName(String pkg, String defProc, CharSequence procSeq, String[] outError) {
        if (procSeq == null) {
            return defProc;
        }
        if (procSeq.length() <= 0) {
            return null;
        }
        return buildCompoundName(pkg, procSeq, "taskAffinity", outError);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseKeySets(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int currentKeySetDepth = PARSE_DEFAULT_INSTALL_LOCATION;
        String currentKeySet = null;
        ArrayMap<String, PublicKey> publicKeys = new ArrayMap();
        ArraySet<String> upgradeKeySets = new ArraySet();
        ArrayMap<String, ArraySet<String>> definedKeySets = new ArrayMap();
        ArraySet<String> improperKeySets = new ArraySet();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
            } else if (type != 3) {
                String tagName = parser.getName();
                TypedArray sa;
                if (!tagName.equals("key-set")) {
                    if (!tagName.equals("public-key")) {
                        if (tagName.equals("upgrade-key-set")) {
                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUpgradeKeySet);
                            upgradeKeySets.add(sa.getNonResourceString(SDK_VERSION));
                            sa.recycle();
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <key-sets>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } else if (currentKeySet == null) {
                        outError[SDK_VERSION] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return RIGID_PARSER;
                    } else {
                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPublicKey);
                        String publicKeyName = sa.getNonResourceString(SDK_VERSION);
                        String encodedKey = sa.getNonResourceString(PARSE_IS_SYSTEM);
                        if (encodedKey == null && publicKeys.get(publicKeyName) == null) {
                            outError[SDK_VERSION] = "'public-key' " + publicKeyName + " must define a public-key value" + " on first use at " + parser.getPositionDescription();
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            sa.recycle();
                            return RIGID_PARSER;
                        }
                        if (encodedKey != null) {
                            PublicKey currentKey = parsePublicKey(encodedKey);
                            if (currentKey == null) {
                                Slog.w(TAG, "No recognized valid key in 'public-key' tag at " + parser.getPositionDescription() + " key-set " + currentKeySet + " will not be added to the package's defined key-sets.");
                                sa.recycle();
                                improperKeySets.add(currentKeySet);
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                if (publicKeys.get(publicKeyName) != null) {
                                    if (!((PublicKey) publicKeys.get(publicKeyName)).equals(currentKey)) {
                                        outError[SDK_VERSION] = "Value of 'public-key' " + publicKeyName + " conflicts with previously defined value at " + parser.getPositionDescription();
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        sa.recycle();
                                        return RIGID_PARSER;
                                    }
                                }
                                publicKeys.put(publicKeyName, currentKey);
                            }
                        }
                        ((ArraySet) definedKeySets.get(currentKeySet)).add(publicKeyName);
                        sa.recycle();
                        XmlUtils.skipCurrentTag(parser);
                    }
                } else if (currentKeySet != null) {
                    outError[SDK_VERSION] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return RIGID_PARSER;
                } else {
                    sa = res.obtainAttributes(parser, R.styleable.AndroidManifestKeySet);
                    String keysetName = sa.getNonResourceString(SDK_VERSION);
                    definedKeySets.put(keysetName, new ArraySet());
                    currentKeySet = keysetName;
                    currentKeySetDepth = parser.getDepth();
                    sa.recycle();
                }
            } else if (parser.getDepth() == currentKeySetDepth) {
                currentKeySet = null;
                currentKeySetDepth = PARSE_DEFAULT_INSTALL_LOCATION;
            }
        }
        if (publicKeys.keySet().removeAll(definedKeySets.keySet())) {
            outError[SDK_VERSION] = "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' and 'public-key' names must be distinct.";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return RIGID_PARSER;
        }
        owner.mKeySetMapping = new ArrayMap();
        for (Entry<String, ArraySet<String>> e : definedKeySets.entrySet()) {
            String keySetName = (String) e.getKey();
            if (((ArraySet) e.getValue()).size() == 0) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' " + keySetName + " has no valid associated 'public-key'." + " Not including in package's defined key-sets.");
            } else if (improperKeySets.contains(keySetName)) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' " + keySetName + " contained improper 'public-key'" + " tags. Not including in package's defined key-sets.");
            } else {
                owner.mKeySetMapping.put(keySetName, new ArraySet());
                for (String s : (ArraySet) e.getValue()) {
                    ((ArraySet) owner.mKeySetMapping.get(keySetName)).add((PublicKey) publicKeys.get(s));
                }
            }
        }
        if (owner.mKeySetMapping.keySet().containsAll(upgradeKeySets)) {
            owner.mUpgradeKeySets = upgradeKeySets;
            return true;
        }
        outError[SDK_VERSION] = "Package" + owner.packageName + " AndroidManifext.xml " + "does not define all 'upgrade-key-set's .";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return RIGID_PARSER;
    }

    private PermissionGroup parsePermissionGroup(Package owner, int flags, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Component perm = new PermissionGroup(owner);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermissionGroup);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission-group>", sa, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, MAX_PACKAGES_PER_APK, 7)) {
            perm.info.descriptionRes = sa.getResourceId(PARSE_MUST_BE_APK, SDK_VERSION);
            perm.info.flags = sa.getInt(6, SDK_VERSION);
            perm.info.priority = sa.getInt(3, SDK_VERSION);
            if (perm.info.priority > 0 && (flags & PARSE_IS_SYSTEM) == 0) {
                perm.info.priority = SDK_VERSION;
            }
            sa.recycle();
            if (parseAllMetaData(res, parser, "<permission-group>", perm, outError)) {
                owner.permissionGroups.add(perm);
                return perm;
            }
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private Permission parsePermission(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Component perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermission);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission>", sa, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, 6, PARSE_IGNORE_PROCESSES)) {
            perm.info.group = sa.getNonResourceString(PARSE_MUST_BE_APK);
            if (perm.info.group != null) {
                perm.info.group = perm.info.group.intern();
            }
            perm.info.descriptionRes = sa.getResourceId(MAX_PACKAGES_PER_APK, SDK_VERSION);
            perm.info.protectionLevel = sa.getInt(3, SDK_VERSION);
            perm.info.flags = sa.getInt(7, SDK_VERSION);
            sa.recycle();
            if (perm.info.protectionLevel == PARSE_DEFAULT_INSTALL_LOCATION) {
                outError[SDK_VERSION] = "<permission> does not specify protectionLevel";
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
            perm.info.protectionLevel = PermissionInfo.fixProtectionLevel(perm.info.protectionLevel);
            if ((perm.info.protectionLevel & PermissionInfo.PROTECTION_MASK_FLAGS) == 0 || (perm.info.protectionLevel & 15) == PARSE_CHATTY) {
                if (parseAllMetaData(res, parser, "<permission>", perm, outError)) {
                    owner.permissions.add(perm);
                    return perm;
                }
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
            outError[SDK_VERSION] = "<permission>  protectionLevel specifies a flag but is not based on signature type";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private Permission parsePermissionTree(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Component perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermissionTree);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission-tree>", sa, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, 3, PARSE_MUST_BE_APK)) {
            sa.recycle();
            int index = perm.info.name.indexOf(46);
            if (index > 0) {
                index = perm.info.name.indexOf(46, index + PARSE_IS_SYSTEM);
            }
            if (index < 0) {
                outError[SDK_VERSION] = "<permission-tree> name has less than three segments: " + perm.info.name;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
            perm.info.descriptionRes = SDK_VERSION;
            perm.info.protectionLevel = SDK_VERSION;
            perm.tree = true;
            if (parseAllMetaData(res, parser, "<permission-tree>", perm, outError)) {
                owner.permissions.add(perm);
                return perm;
            }
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private Instrumentation parseInstrumentation(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestInstrumentation);
        if (this.mParseInstrumentationArgs == null) {
            this.mParseInstrumentationArgs = new ParsePackageItemArgs(owner, outError, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, 6, 7);
            this.mParseInstrumentationArgs.tag = "<instrumentation>";
        }
        this.mParseInstrumentationArgs.sa = sa;
        Instrumentation a = new Instrumentation(this.mParseInstrumentationArgs, new InstrumentationInfo());
        if (outError[SDK_VERSION] != null) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        String str = sa.getNonResourceString(3);
        a.info.targetPackage = str != null ? str.intern() : null;
        a.info.handleProfiling = sa.getBoolean(PARSE_MUST_BE_APK, RIGID_PARSER);
        a.info.functionalTest = sa.getBoolean(MAX_PACKAGES_PER_APK, RIGID_PARSER);
        sa.recycle();
        if (a.info.targetPackage == null) {
            outError[SDK_VERSION] = "<instrumentation> does not specify targetPackage";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        if (parseAllMetaData(res, parser, "<instrumentation>", a, outError)) {
            owner.instrumentation.add(a);
            return a;
        }
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseBaseApplication(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedValue v;
        ApplicationInfo ai = owner.applicationInfo;
        String pkgName = owner.applicationInfo.packageName;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestApplication);
        String name = sa.getNonConfigurationString(3, SDK_VERSION);
        if (name != null) {
            ai.className = buildClassName(pkgName, name, outError);
            if (ai.className == null) {
                sa.recycle();
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return RIGID_PARSER;
            }
        }
        String manageSpaceActivity = sa.getNonConfigurationString(PARSE_MUST_BE_APK, PARSE_ENFORCE_CODE);
        if (manageSpaceActivity != null) {
            ai.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity, outError);
        }
        if (sa.getBoolean(17, true)) {
            ai.flags |= Document.FLAG_ARCHIVE;
            String backupAgent = sa.getNonConfigurationString(PARSE_FORWARD_LOCK, PARSE_ENFORCE_CODE);
            if (backupAgent != null) {
                ai.backupAgentName = buildClassName(pkgName, backupAgent, outError);
                if (sa.getBoolean(18, true)) {
                    ai.flags |= Root.FLAG_EMPTY;
                }
                if (sa.getBoolean(21, RIGID_PARSER)) {
                    ai.flags |= Root.FLAG_ADVANCED;
                }
                if (sa.getBoolean(PARSE_EXTERNAL_STORAGE, RIGID_PARSER)) {
                    ai.flags |= StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE;
                }
                if (sa.getBoolean(40, RIGID_PARSER)) {
                    ai.privateFlags |= PARSE_FORCE_SDK;
                }
            }
            v = sa.peekValue(35);
            if (v != null) {
                int i;
                i = v.resourceId;
                ai.fullBackupContent = i;
                if (i == 0) {
                    ai.fullBackupContent = v.data == 0 ? PARSE_DEFAULT_INSTALL_LOCATION : SDK_VERSION;
                }
            }
        }
        v = sa.peekValue(PARSE_IS_SYSTEM);
        if (v != null) {
            i = v.resourceId;
            ai.labelRes = i;
            if (i == 0) {
                ai.nonLocalizedLabel = v.coerceToString();
            }
        }
        ai.icon = sa.getResourceId(PARSE_CHATTY, SDK_VERSION);
        ai.logo = sa.getResourceId(22, SDK_VERSION);
        ai.banner = sa.getResourceId(30, SDK_VERSION);
        ai.theme = sa.getResourceId(SDK_VERSION, SDK_VERSION);
        if ("com.google.android.packageinstaller".equals(pkgName)) {
            ai.theme = 33947865;
            Flog.i(BluetoothAssignedNumbers.ELGATO_SYSTEMS, "parseBaseApplication, packageinstaller new themeName = " + res.getResourceName(ai.theme));
        }
        ai.descriptionRes = sa.getResourceId(13, SDK_VERSION);
        if ((flags & PARSE_IS_SYSTEM) != 0 && sa.getBoolean(PARSE_IGNORE_PROCESSES, RIGID_PARSER)) {
            ai.flags |= PARSE_IGNORE_PROCESSES;
        }
        if (sa.getBoolean(27, RIGID_PARSER)) {
            owner.mRequiredForAllUsers = true;
        }
        String restrictedAccountType = sa.getString(28);
        if (restrictedAccountType != null && restrictedAccountType.length() > 0) {
            owner.mRestrictedAccountType = restrictedAccountType;
        }
        String requiredAccountType = sa.getString(29);
        if (requiredAccountType != null && requiredAccountType.length() > 0) {
            owner.mRequiredAccountType = requiredAccountType;
        }
        if (sa.getBoolean(10, RIGID_PARSER)) {
            ai.flags |= PARSE_CHATTY;
        }
        if (sa.getBoolean(20, RIGID_PARSER)) {
            ai.flags |= Process.PROC_OUT_FLOAT;
        }
        owner.baseHardwareAccelerated = sa.getBoolean(23, owner.applicationInfo.targetSdkVersion >= 14 ? true : RIGID_PARSER);
        if (owner.baseHardwareAccelerated) {
            ai.flags |= KeymasterDefs.KM_ENUM_REP;
        }
        if (sa.getBoolean(7, true)) {
            ai.flags |= PARSE_MUST_BE_APK;
        }
        if (sa.getBoolean(14, RIGID_PARSER)) {
            ai.flags |= PARSE_EXTERNAL_STORAGE;
        }
        if (sa.getBoolean(MAX_PACKAGES_PER_APK, true)) {
            ai.flags |= PARSE_IS_SYSTEM_DIR;
        }
        if (owner.parentPackage == null && sa.getBoolean(15, RIGID_PARSER)) {
            ai.flags |= PARSE_COLLECT_CERTIFICATES;
        }
        if (sa.getBoolean(24, RIGID_PARSER)) {
            ai.flags |= Root.FLAG_REMOVABLE_USB;
        }
        if (sa.getBoolean(36, true)) {
            ai.flags |= HistoryItem.STATE_WIFI_SCAN_FLAG;
        }
        if (sa.getBoolean(26, RIGID_PARSER)) {
            ai.flags |= StrictMode.PENALTY_GATHER;
        }
        if (sa.getBoolean(33, RIGID_PARSER)) {
            ai.flags |= KeymasterDefs.KM_BIGNUM;
        }
        if (sa.getBoolean(34, true)) {
            ai.flags |= KeymasterDefs.KM_ENUM;
        }
        if (sa.getBoolean(38, RIGID_PARSER)) {
            ai.privateFlags |= PARSE_EXTERNAL_STORAGE;
        }
        if (sa.getBoolean(39, RIGID_PARSER)) {
            ai.privateFlags |= PARSE_IS_SYSTEM_DIR;
        }
        if (sa.getBoolean(37, owner.applicationInfo.targetSdkVersion >= 24 ? true : RIGID_PARSER)) {
            ai.privateFlags |= PARSE_IS_EPHEMERAL;
        }
        ai.networkSecurityConfigRes = sa.getResourceId(41, SDK_VERSION);
        String str = sa.getNonConfigurationString(6, SDK_VERSION);
        String intern = (str == null || str.length() <= 0) ? null : str.intern();
        ai.permission = intern;
        if (owner.applicationInfo.targetSdkVersion >= PARSE_IGNORE_PROCESSES) {
            str = sa.getNonConfigurationString(12, PARSE_ENFORCE_CODE);
        } else {
            str = sa.getNonResourceString(12);
        }
        ai.taskAffinity = buildTaskAffinityName(ai.packageName, ai.packageName, str, outError);
        if (outError[SDK_VERSION] == null) {
            CharSequence pname;
            if (owner.applicationInfo.targetSdkVersion >= PARSE_IGNORE_PROCESSES) {
                pname = sa.getNonConfigurationString(11, PARSE_ENFORCE_CODE);
            } else {
                pname = sa.getNonResourceString(11);
            }
            ai.processName = buildProcessName(ai.packageName, null, pname, flags, this.mSeparateProcesses, outError);
            ai.enabled = sa.getBoolean(9, true);
            if (sa.getBoolean(31, RIGID_PARSER)) {
                ai.flags |= StrictMode.PENALTY_DEATH_ON_CLEARTEXT_NETWORK;
            }
        }
        ai.uiOptions = sa.getInt(25, SDK_VERSION);
        sa.recycle();
        if (outError[SDK_VERSION] != null) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return RIGID_PARSER;
        }
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= innerDepth)) {
                modifySharedLibrariesForBackwardCompatibility(owner);
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                String tagName = parser.getName();
                Activity a;
                if (tagName.equals(LaunchMode.ACTIVITY)) {
                    a = parseActivity(owner, res, parser, flags, outError, RIGID_PARSER, owner.baseHardwareAccelerated);
                    if (a == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return RIGID_PARSER;
                    }
                    owner.activities.add(a);
                } else {
                    if (tagName.equals(HwFrameworkMonitor.KEY_RECEIVER)) {
                        a = parseActivity(owner, res, parser, flags, outError, true, RIGID_PARSER);
                        if (a == null) {
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return RIGID_PARSER;
                        }
                        owner.receivers.add(a);
                    } else {
                        if (tagName.equals(TileService.EXTRA_SERVICE)) {
                            Service s = parseService(owner, res, parser, flags, outError);
                            if (s == null) {
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return RIGID_PARSER;
                            }
                            owner.services.add(s);
                        } else {
                            if (tagName.equals(LaunchMode.PROVIDER)) {
                                Provider p = parseProvider(owner, res, parser, flags, outError);
                                if (p == null) {
                                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return RIGID_PARSER;
                                }
                                owner.providers.add(p);
                            } else {
                                if (tagName.equals("activity-alias")) {
                                    a = parseActivityAlias(owner, res, parser, flags, outError);
                                    if (a == null) {
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        return RIGID_PARSER;
                                    }
                                    owner.activities.add(a);
                                } else if (parser.getName().equals("meta-data")) {
                                    Bundle parseMetaData = parseMetaData(res, parser, owner.mAppMetaData, outError);
                                    owner.mAppMetaData = parseMetaData;
                                    if (parseMetaData == null) {
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        return RIGID_PARSER;
                                    }
                                    int themeId = HwWidgetFactory.getThemeId(owner.mAppMetaData, res);
                                    if (themeId != 0) {
                                        ai.theme = themeId;
                                    }
                                } else {
                                    String lname;
                                    if (tagName.equals("library")) {
                                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestLibrary);
                                        lname = sa.getNonResourceString(SDK_VERSION);
                                        sa.recycle();
                                        if (lname != null) {
                                            lname = lname.intern();
                                            if (!ArrayUtils.contains(owner.libraryNames, lname)) {
                                                owner.libraryNames = ArrayUtils.add(owner.libraryNames, lname);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        if (tagName.equals("uses-library")) {
                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesLibrary);
                                            lname = sa.getNonResourceString(SDK_VERSION);
                                            boolean req = sa.getBoolean(PARSE_IS_SYSTEM, true);
                                            sa.recycle();
                                            if (lname != null) {
                                                lname = lname.intern();
                                                if (req) {
                                                    owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname);
                                                } else {
                                                    owner.usesOptionalLibraries = ArrayUtils.add(owner.usesOptionalLibraries, lname);
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            if (tagName.equals("uses-package")) {
                                                XmlUtils.skipCurrentTag(parser);
                                            } else {
                                                Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                XmlUtils.skipCurrentTag(parser);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        modifySharedLibrariesForBackwardCompatibility(owner);
        ApplicationInfo applicationInfo;
        if (hasDomainURLs(owner)) {
            applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= PARSE_FORWARD_LOCK;
        } else {
            applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags &= -17;
        }
        return true;
    }

    private static void modifySharedLibrariesForBackwardCompatibility(Package owner) {
        owner.usesLibraries = ArrayUtils.remove(owner.usesLibraries, "org.apache.http.legacy");
        owner.usesOptionalLibraries = ArrayUtils.remove(owner.usesOptionalLibraries, "org.apache.http.legacy");
    }

    private static boolean hasDomainURLs(Package pkg) {
        if (pkg == null || pkg.activities == null) {
            return RIGID_PARSER;
        }
        ArrayList<Activity> activities = pkg.activities;
        int countActivities = activities.size();
        for (int n = SDK_VERSION; n < countActivities; n += PARSE_IS_SYSTEM) {
            ArrayList<ActivityIntentInfo> filters = ((Activity) activities.get(n)).intents;
            if (filters != null) {
                int countFilters = filters.size();
                for (int m = SDK_VERSION; m < countFilters; m += PARSE_IS_SYSTEM) {
                    ActivityIntentInfo aii = (ActivityIntentInfo) filters.get(m);
                    if (aii.hasAction(Intent.ACTION_VIEW) && aii.hasAction(Intent.ACTION_VIEW) && (aii.hasDataScheme(IntentFilter.SCHEME_HTTP) || aii.hasDataScheme(IntentFilter.SCHEME_HTTPS))) {
                        return true;
                    }
                }
                continue;
            }
        }
        return RIGID_PARSER;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseSplitApplication(Package owner, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException {
        if (res.obtainAttributes(parser, R.styleable.AndroidManifestApplication).getBoolean(7, true)) {
            int[] iArr = owner.splitFlags;
            iArr[splitIndex] = iArr[splitIndex] | PARSE_MUST_BE_APK;
        }
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != PARSE_IS_SYSTEM && (type != 3 || parser.getDepth() > innerDepth)) {
                if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                    String tagName = parser.getName();
                    Activity a;
                    if (tagName.equals(LaunchMode.ACTIVITY)) {
                        a = parseActivity(owner, res, parser, flags, outError, RIGID_PARSER, owner.baseHardwareAccelerated);
                        if (a == null) {
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return RIGID_PARSER;
                        }
                        owner.activities.add(a);
                    } else {
                        if (tagName.equals(HwFrameworkMonitor.KEY_RECEIVER)) {
                            a = parseActivity(owner, res, parser, flags, outError, true, RIGID_PARSER);
                            if (a == null) {
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return RIGID_PARSER;
                            }
                            owner.receivers.add(a);
                        } else {
                            if (tagName.equals(TileService.EXTRA_SERVICE)) {
                                Service s = parseService(owner, res, parser, flags, outError);
                                if (s == null) {
                                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return RIGID_PARSER;
                                }
                                owner.services.add(s);
                            } else {
                                if (tagName.equals(LaunchMode.PROVIDER)) {
                                    Provider p = parseProvider(owner, res, parser, flags, outError);
                                    if (p == null) {
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        return RIGID_PARSER;
                                    }
                                    owner.providers.add(p);
                                } else {
                                    if (tagName.equals("activity-alias")) {
                                        a = parseActivityAlias(owner, res, parser, flags, outError);
                                        if (a == null) {
                                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                            return RIGID_PARSER;
                                        }
                                        owner.activities.add(a);
                                    } else if (parser.getName().equals("meta-data")) {
                                        Bundle parseMetaData = parseMetaData(res, parser, owner.mAppMetaData, outError);
                                        owner.mAppMetaData = parseMetaData;
                                        if (parseMetaData == null) {
                                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                            return RIGID_PARSER;
                                        }
                                    } else {
                                        if (tagName.equals("uses-library")) {
                                            TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesLibrary);
                                            String lname = sa.getNonResourceString(SDK_VERSION);
                                            boolean req = sa.getBoolean(PARSE_IS_SYSTEM, true);
                                            sa.recycle();
                                            if (lname != null) {
                                                lname = lname.intern();
                                                if (req) {
                                                    owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname);
                                                    owner.usesOptionalLibraries = ArrayUtils.remove(owner.usesOptionalLibraries, lname);
                                                } else if (!ArrayUtils.contains(owner.usesLibraries, lname)) {
                                                    owner.usesOptionalLibraries = ArrayUtils.add(owner.usesOptionalLibraries, lname);
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            if (tagName.equals("uses-package")) {
                                                XmlUtils.skipCurrentTag(parser);
                                            } else {
                                                Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                XmlUtils.skipCurrentTag(parser);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean parsePackageItemInfo(Package owner, PackageItemInfo outInfo, String[] outError, String tag, TypedArray sa, int nameRes, int labelRes, int iconRes, int logoRes, int bannerRes) {
        String name = sa.getNonConfigurationString(nameRes, SDK_VERSION);
        if (name == null) {
            outError[SDK_VERSION] = tag + " does not specify android:name";
            return RIGID_PARSER;
        }
        outInfo.name = buildClassName(owner.applicationInfo.packageName, name, outError);
        if (outInfo.name == null) {
            return RIGID_PARSER;
        }
        int iconVal = sa.getResourceId(iconRes, SDK_VERSION);
        if (iconVal != 0) {
            outInfo.icon = iconVal;
            outInfo.nonLocalizedLabel = null;
        }
        int logoVal = sa.getResourceId(logoRes, SDK_VERSION);
        if (logoVal != 0) {
            outInfo.logo = logoVal;
        }
        int bannerVal = sa.getResourceId(bannerRes, SDK_VERSION);
        if (bannerVal != 0) {
            outInfo.banner = bannerVal;
        }
        TypedValue v = sa.peekValue(labelRes);
        if (v != null) {
            int i = v.resourceId;
            outInfo.labelRes = i;
            if (i == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        outInfo.packageName = owner.packageName;
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Activity parseActivity(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, boolean receiver, boolean hardwareAccelerated) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestActivity);
        if (this.mParseActivityArgs == null) {
            this.mParseActivityArgs = new ParseComponentArgs(owner, outError, 3, PARSE_IS_SYSTEM, PARSE_CHATTY, 23, 30, this.mSeparateProcesses, 7, 17, MAX_PACKAGES_PER_APK);
        }
        this.mParseActivityArgs.tag = receiver ? "<receiver>" : "<activity>";
        this.mParseActivityArgs.sa = sa;
        this.mParseActivityArgs.flags = flags;
        Activity activity = new Activity(this.mParseActivityArgs, new ActivityInfo());
        if (outError[SDK_VERSION] != null) {
            sa.recycle();
            return null;
        }
        boolean hasValue = sa.hasValue(6);
        if (hasValue) {
            activity.info.exported = sa.getBoolean(6, RIGID_PARSER);
        }
        activity.info.theme = sa.getResourceId(SDK_VERSION, SDK_VERSION);
        if ("com.google.android.packageinstaller".equals(owner.packageName)) {
            if (activity.info.theme != 0) {
                String themeName = res.getResourceName(activity.info.theme);
                if (themeName.endsWith("Theme.AlertDialogActivity")) {
                    activity.info.theme = 33947867;
                } else {
                    if (themeName.endsWith("GrantPermissions")) {
                        activity.info.theme = 33947873;
                    } else {
                        if (themeName.endsWith("Settings")) {
                            activity.info.theme = 33947868;
                        } else {
                            if (themeName.endsWith("Theme.DeviceDefault.Light.Dialog.NoActionBar")) {
                                activity.info.theme = 33947873;
                            } else {
                                if (themeName.endsWith("Settings.NoActionBar")) {
                                    activity.info.theme = 33947869;
                                } else {
                                    activity.info.theme = 33947866;
                                }
                            }
                        }
                    }
                }
                Flog.i(BluetoothAssignedNumbers.ELGATO_SYSTEMS, "parseActivity, packageinstaller themeName changes from [" + themeName + "] to [" + res.getResourceName(activity.info.theme) + "]");
            } else {
                activity.info.theme = 33947866;
                Flog.i(BluetoothAssignedNumbers.ELGATO_SYSTEMS, "parseActivity, packageinstaller no themeName change to [" + res.getResourceName(activity.info.theme) + "]");
            }
        }
        activity.info.uiOptions = sa.getInt(26, activity.info.applicationInfo.uiOptions);
        String parentName = sa.getNonConfigurationString(27, PARSE_ENFORCE_CODE);
        if (parentName != null) {
            String parentClassName = buildClassName(activity.info.packageName, parentName, outError);
            if (outError[SDK_VERSION] == null) {
                activity.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity " + activity.info.name + " specified invalid parentActivityName " + parentName);
                outError[SDK_VERSION] = null;
            }
        }
        String str = sa.getNonConfigurationString(PARSE_MUST_BE_APK, SDK_VERSION);
        if (str == null) {
            activity.info.permission = owner.applicationInfo.permission;
        } else {
            activity.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        activity.info.taskAffinity = buildTaskAffinityName(owner.applicationInfo.packageName, owner.applicationInfo.taskAffinity, sa.getNonConfigurationString(PARSE_IGNORE_PROCESSES, PARSE_ENFORCE_CODE), outError);
        activity.info.flags = SDK_VERSION;
        if (sa.getBoolean(9, RIGID_PARSER)) {
            ActivityInfo activityInfo = activity.info;
            activityInfo.flags |= PARSE_IS_SYSTEM;
        }
        if (sa.getBoolean(10, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_CHATTY;
        }
        if (sa.getBoolean(11, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_MUST_BE_APK;
        }
        if (sa.getBoolean(21, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_IS_PRIVILEGED;
        }
        if (sa.getBoolean(18, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_IGNORE_PROCESSES;
        }
        if (sa.getBoolean(12, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_FORWARD_LOCK;
        }
        if (sa.getBoolean(13, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_EXTERNAL_STORAGE;
        }
        if (sa.getBoolean(19, (owner.applicationInfo.flags & PARSE_EXTERNAL_STORAGE) != 0 ? true : RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_IS_SYSTEM_DIR;
        }
        if (sa.getBoolean(22, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_COLLECT_CERTIFICATES;
        }
        if (sa.getBoolean(29, RIGID_PARSER) || sa.getBoolean(39, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_ENFORCE_CODE;
        }
        if (sa.getBoolean(24, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= PARSE_IS_EPHEMERAL;
        }
        if (sa.getBoolean(44, RIGID_PARSER)) {
            activityInfo = activity.info;
            activityInfo.flags |= KeymasterDefs.KM_ENUM_REP;
        }
        boolean z;
        if (receiver) {
            activity.info.launchMode = SDK_VERSION;
            activity.info.configChanges = SDK_VERSION;
            if (sa.getBoolean(28, RIGID_PARSER)) {
                activityInfo = activity.info;
                activityInfo.flags |= KeymasterDefs.KM_UINT_REP;
                if (activity.info.exported && (flags & PARSE_IS_PRIVILEGED) == 0) {
                    Slog.w(TAG, "Activity exported request ignored due to singleUser: " + activity.className + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    activity.info.exported = RIGID_PARSER;
                    hasValue = true;
                }
            }
            activityInfo = activity.info;
            z = sa.getBoolean(42, RIGID_PARSER);
            activity.info.directBootAware = z;
            activityInfo.encryptionAware = z;
        } else {
            if (sa.getBoolean(25, hardwareAccelerated)) {
                activityInfo = activity.info;
                activityInfo.flags |= PARSE_TRUSTED_OVERLAY;
            }
            activity.info.launchMode = sa.getInt(14, SDK_VERSION);
            activity.info.documentLaunchMode = sa.getInt(33, SDK_VERSION);
            activity.info.maxRecents = sa.getInt(34, ActivityManager.getDefaultAppRecentsLimitStatic());
            activity.info.configChanges = sa.getInt(PARSE_FORWARD_LOCK, SDK_VERSION);
            activity.info.softInputMode = sa.getInt(20, SDK_VERSION);
            activity.info.persistableMode = sa.getInteger(PARSE_EXTERNAL_STORAGE, SDK_VERSION);
            if (sa.getBoolean(31, RIGID_PARSER)) {
                activityInfo = activity.info;
                activityInfo.flags |= KeymasterDefs.KM_BIGNUM;
            }
            if (sa.getBoolean(35, RIGID_PARSER)) {
                activityInfo = activity.info;
                activityInfo.flags |= Process.PROC_OUT_LONG;
            }
            if (sa.getBoolean(36, RIGID_PARSER)) {
                activityInfo = activity.info;
                activityInfo.flags |= PARSE_FORCE_SDK;
            }
            if (sa.getBoolean(37, RIGID_PARSER)) {
                activityInfo = activity.info;
                activityInfo.flags |= Process.PROC_OUT_FLOAT;
            }
            activity.info.screenOrientation = sa.getInt(15, PARSE_DEFAULT_INSTALL_LOCATION);
            activity.info.resizeMode = SDK_VERSION;
            boolean appDefault = (owner.applicationInfo.privateFlags & PARSE_IS_EPHEMERAL) != 0 ? true : RIGID_PARSER;
            boolean resizeableSetExplicitly = sa.hasValue(40);
            if (sa.getBoolean(40, appDefault)) {
                if (sa.getBoolean(41, RIGID_PARSER)) {
                    activity.info.resizeMode = 3;
                } else {
                    activity.info.resizeMode = PARSE_CHATTY;
                }
            } else if (owner.applicationInfo.targetSdkVersion >= 24 || resizeableSetExplicitly) {
                activity.info.resizeMode = SDK_VERSION;
            } else if (!activity.info.isFixedOrientation() && (activity.info.flags & PARSE_IS_EPHEMERAL) == 0) {
                activity.info.resizeMode = PARSE_MUST_BE_APK;
            }
            if (sa.getBoolean(45, RIGID_PARSER)) {
                activityInfo = activity.info;
                activityInfo.flags |= Root.FLAG_HAS_SETTINGS;
            }
            activity.info.lockTaskLaunchMode = sa.getInt(38, SDK_VERSION);
            activityInfo = activity.info;
            z = sa.getBoolean(42, RIGID_PARSER);
            activity.info.directBootAware = z;
            activityInfo.encryptionAware = z;
            activity.info.requestedVrComponent = sa.getString(43);
        }
        if (activity.info.directBootAware) {
            ApplicationInfo applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= PARSE_COLLECT_CERTIFICATES;
        }
        sa.recycle();
        if (receiver && (owner.applicationInfo.privateFlags & PARSE_CHATTY) != 0 && activity.info.processName == owner.packageName) {
            outError[SDK_VERSION] = "Heavy-weight applications can not have receivers in main process";
        }
        if (outError[SDK_VERSION] != null) {
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!hasValue) {
                    activity.info.exported = activity.intents.size() <= 0 ? true : RIGID_PARSER;
                }
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                ActivityIntentInfo intent;
                if (parser.getName().equals("intent-filter")) {
                    intent = new ActivityIntentInfo(activity);
                    if (!parseIntent(res, parser, true, true, intent, outError)) {
                        return null;
                    }
                    if (intent.countActions() == 0) {
                        Slog.w(TAG, "No actions in intent filter at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    } else {
                        activity.intents.add(intent);
                    }
                } else if (!receiver && parser.getName().equals("preferred")) {
                    intent = new ActivityIntentInfo(activity);
                    if (!parseIntent(res, parser, RIGID_PARSER, RIGID_PARSER, intent, outError)) {
                        return null;
                    }
                    if (intent.countActions() == 0) {
                        Slog.w(TAG, "No actions in preferred at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    } else {
                        if (owner.preferredActivityFilters == null) {
                            owner.preferredActivityFilters = new ArrayList();
                        }
                        owner.preferredActivityFilters.add(intent);
                    }
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, activity.metaData, outError);
                    activity.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return null;
                    }
                    HwFrameworkFactory.getHwPackageParser().initMetaData(activity);
                    int themeId = HwWidgetFactory.getThemeId(activity.metaData, res);
                    if (themeId != 0) {
                        activity.info.theme = themeId;
                    }
                    HwThemeManager.addSimpleUIConfig(activity);
                } else if (receiver || !parser.getName().equals(TtmlUtils.TAG_LAYOUT)) {
                    Slog.w(TAG, "Problem in package " + this.mArchiveSourcePath + ":");
                    if (receiver) {
                        Slog.w(TAG, "Unknown element under <receiver>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    } else {
                        Slog.w(TAG, "Unknown element under <activity>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    parseLayout(res, parser, activity);
                }
            }
        }
        if (hasValue) {
            if (activity.intents.size() <= 0) {
            }
            activity.info.exported = activity.intents.size() <= 0 ? true : RIGID_PARSER;
        }
        return activity;
    }

    private void parseLayout(Resources res, AttributeSet attrs, Activity a) {
        TypedArray sw = res.obtainAttributes(attrs, R.styleable.AndroidManifestLayout);
        int width = PARSE_DEFAULT_INSTALL_LOCATION;
        float widthFraction = ScaledLayoutParams.SCALE_UNSPECIFIED;
        int height = PARSE_DEFAULT_INSTALL_LOCATION;
        float heightFraction = ScaledLayoutParams.SCALE_UNSPECIFIED;
        int widthType = sw.getType(3);
        if (widthType == 6) {
            widthFraction = sw.getFraction(3, PARSE_IS_SYSTEM, PARSE_IS_SYSTEM, ScaledLayoutParams.SCALE_UNSPECIFIED);
        } else if (widthType == MAX_PACKAGES_PER_APK) {
            width = sw.getDimensionPixelSize(3, PARSE_DEFAULT_INSTALL_LOCATION);
        }
        int heightType = sw.getType(PARSE_MUST_BE_APK);
        if (heightType == 6) {
            heightFraction = sw.getFraction(PARSE_MUST_BE_APK, PARSE_IS_SYSTEM, PARSE_IS_SYSTEM, ScaledLayoutParams.SCALE_UNSPECIFIED);
        } else if (heightType == MAX_PACKAGES_PER_APK) {
            height = sw.getDimensionPixelSize(PARSE_MUST_BE_APK, PARSE_DEFAULT_INSTALL_LOCATION);
        }
        int gravity = sw.getInt(SDK_VERSION, 17);
        int minWidth = sw.getDimensionPixelSize(PARSE_IS_SYSTEM, PARSE_DEFAULT_INSTALL_LOCATION);
        int minHeight = sw.getDimensionPixelSize(PARSE_CHATTY, PARSE_DEFAULT_INSTALL_LOCATION);
        sw.recycle();
        a.info.windowLayout = new WindowLayout(width, widthFraction, height, heightFraction, gravity, minWidth, minHeight);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Activity parseActivityAlias(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestActivityAlias);
        String targetActivity = sa.getNonConfigurationString(7, PARSE_ENFORCE_CODE);
        if (targetActivity == null) {
            outError[SDK_VERSION] = "<activity-alias> does not specify android:targetActivity";
            sa.recycle();
            return null;
        }
        targetActivity = buildClassName(owner.applicationInfo.packageName, targetActivity, outError);
        if (targetActivity == null) {
            sa.recycle();
            return null;
        }
        if (this.mParseActivityAliasArgs == null) {
            this.mParseActivityAliasArgs = new ParseComponentArgs(owner, outError, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, PARSE_IGNORE_PROCESSES, 10, this.mSeparateProcesses, SDK_VERSION, 6, PARSE_MUST_BE_APK);
            this.mParseActivityAliasArgs.tag = "<activity-alias>";
        }
        this.mParseActivityAliasArgs.sa = sa;
        this.mParseActivityAliasArgs.flags = flags;
        Activity target = null;
        int NA = owner.activities.size();
        for (int i = SDK_VERSION; i < NA; i += PARSE_IS_SYSTEM) {
            Activity t = (Activity) owner.activities.get(i);
            if (targetActivity.equals(t.info.name)) {
                target = t;
                break;
            }
        }
        if (target == null) {
            outError[SDK_VERSION] = "<activity-alias> target activity " + targetActivity + " not found in manifest";
            sa.recycle();
            return null;
        }
        ActivityInfo info = new ActivityInfo();
        info.targetActivity = targetActivity;
        info.configChanges = target.info.configChanges;
        info.flags = target.info.flags;
        info.icon = target.info.icon;
        info.logo = target.info.logo;
        info.banner = target.info.banner;
        info.labelRes = target.info.labelRes;
        info.nonLocalizedLabel = target.info.nonLocalizedLabel;
        info.launchMode = target.info.launchMode;
        info.lockTaskLaunchMode = target.info.lockTaskLaunchMode;
        info.processName = target.info.processName;
        if (info.descriptionRes == 0) {
            info.descriptionRes = target.info.descriptionRes;
        }
        info.screenOrientation = target.info.screenOrientation;
        info.taskAffinity = target.info.taskAffinity;
        info.theme = target.info.theme;
        info.softInputMode = target.info.softInputMode;
        info.uiOptions = target.info.uiOptions;
        info.parentActivityName = target.info.parentActivityName;
        info.maxRecents = target.info.maxRecents;
        info.windowLayout = target.info.windowLayout;
        info.resizeMode = target.info.resizeMode;
        boolean z = target.info.directBootAware;
        info.directBootAware = z;
        info.encryptionAware = z;
        Activity activity = new Activity(this.mParseActivityAliasArgs, info);
        if (outError[SDK_VERSION] != null) {
            sa.recycle();
            return null;
        }
        boolean setExported = sa.hasValue(MAX_PACKAGES_PER_APK);
        if (setExported) {
            activity.info.exported = sa.getBoolean(MAX_PACKAGES_PER_APK, RIGID_PARSER);
        }
        String str = sa.getNonConfigurationString(3, SDK_VERSION);
        if (str != null) {
            activity.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        String parentName = sa.getNonConfigurationString(9, PARSE_ENFORCE_CODE);
        if (parentName != null) {
            String parentClassName = buildClassName(activity.info.packageName, parentName, outError);
            if (outError[SDK_VERSION] == null) {
                activity.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity alias " + activity.info.name + " specified invalid parentActivityName " + parentName);
                outError[SDK_VERSION] = null;
            }
        }
        sa.recycle();
        if (outError[SDK_VERSION] != null) {
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!setExported) {
                    activity.info.exported = activity.intents.size() <= 0 ? true : RIGID_PARSER;
                }
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                if (parser.getName().equals("intent-filter")) {
                    ActivityIntentInfo intent = new ActivityIntentInfo(activity);
                    if (!parseIntent(res, parser, true, true, intent, outError)) {
                        return null;
                    }
                    if (intent.countActions() == 0) {
                        Slog.w(TAG, "No actions in intent filter at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    } else {
                        activity.intents.add(intent);
                    }
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, activity.metaData, outError);
                    activity.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return null;
                    }
                    HwThemeManager.addSimpleUIConfig(activity);
                } else {
                    Slog.w(TAG, "Unknown element under <activity-alias>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        if (setExported) {
            if (activity.intents.size() <= 0) {
            }
            activity.info.exported = activity.intents.size() <= 0 ? true : RIGID_PARSER;
        }
        return activity;
    }

    private Provider parseProvider(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestProvider);
        if (this.mParseProviderArgs == null) {
            this.mParseProviderArgs = new ParseComponentArgs(owner, outError, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, 15, 17, this.mSeparateProcesses, PARSE_IGNORE_PROCESSES, 14, 6);
            this.mParseProviderArgs.tag = "<provider>";
        }
        this.mParseProviderArgs.sa = sa;
        this.mParseProviderArgs.flags = flags;
        Provider provider = new Provider(this.mParseProviderArgs, new ProviderInfo());
        if (outError[SDK_VERSION] != null) {
            sa.recycle();
            return null;
        }
        ProviderInfo providerInfo;
        boolean providerExportedDefault = RIGID_PARSER;
        if (owner.applicationInfo.targetSdkVersion < 17) {
            providerExportedDefault = true;
        }
        provider.info.exported = sa.getBoolean(7, providerExportedDefault);
        String cpname = sa.getNonConfigurationString(10, SDK_VERSION);
        provider.info.isSyncable = sa.getBoolean(11, RIGID_PARSER);
        String permission = sa.getNonConfigurationString(3, SDK_VERSION);
        String str = sa.getNonConfigurationString(PARSE_MUST_BE_APK, SDK_VERSION);
        if (str == null) {
            str = permission;
        }
        if (str == null) {
            provider.info.readPermission = owner.applicationInfo.permission;
        } else {
            provider.info.readPermission = str.length() > 0 ? str.toString().intern() : null;
        }
        str = sa.getNonConfigurationString(MAX_PACKAGES_PER_APK, SDK_VERSION);
        if (str == null) {
            str = permission;
        }
        if (str == null) {
            provider.info.writePermission = owner.applicationInfo.permission;
        } else {
            provider.info.writePermission = str.length() > 0 ? str.toString().intern() : null;
        }
        provider.info.grantUriPermissions = sa.getBoolean(13, RIGID_PARSER);
        provider.info.multiprocess = sa.getBoolean(9, RIGID_PARSER);
        provider.info.initOrder = sa.getInt(12, SDK_VERSION);
        provider.info.flags = SDK_VERSION;
        if (sa.getBoolean(PARSE_FORWARD_LOCK, RIGID_PARSER)) {
            providerInfo = provider.info;
            providerInfo.flags |= KeymasterDefs.KM_UINT_REP;
            if (provider.info.exported && (flags & PARSE_IS_PRIVILEGED) == 0) {
                Slog.w(TAG, "Provider exported request ignored due to singleUser: " + provider.className + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                provider.info.exported = RIGID_PARSER;
            }
        }
        providerInfo = provider.info;
        boolean z = sa.getBoolean(18, RIGID_PARSER);
        provider.info.directBootAware = z;
        providerInfo.encryptionAware = z;
        if (provider.info.directBootAware) {
            ApplicationInfo applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= PARSE_COLLECT_CERTIFICATES;
        }
        sa.recycle();
        if ((owner.applicationInfo.privateFlags & PARSE_CHATTY) != 0 && provider.info.processName == owner.packageName) {
            outError[SDK_VERSION] = "Heavy-weight applications can not have providers in main process";
            return null;
        } else if (cpname == null) {
            outError[SDK_VERSION] = "<provider> does not include authorities attribute";
            return null;
        } else if (cpname.length() <= 0) {
            outError[SDK_VERSION] = "<provider> has empty authorities attribute";
            return null;
        } else {
            provider.info.authority = cpname.intern();
            if (parseProviderTags(res, parser, provider, outError)) {
                return provider;
            }
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseProviderTags(Resources res, XmlResourceParser parser, Provider outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != PARSE_IS_SYSTEM && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                    if (parser.getName().equals("intent-filter")) {
                        ProviderIntentInfo intent = new ProviderIntentInfo(outInfo);
                        if (!parseIntent(res, parser, true, RIGID_PARSER, intent, outError)) {
                            return RIGID_PARSER;
                        }
                        outInfo.intents.add(intent);
                    } else if (parser.getName().equals("meta-data")) {
                        Bundle parseMetaData = parseMetaData(res, parser, outInfo.metaData, outError);
                        outInfo.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return RIGID_PARSER;
                        }
                    } else if (parser.getName().equals("grant-uri-permission")) {
                        PatternMatcher patternMatcher;
                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestGrantUriPermission);
                        PatternMatcher patternMatcher2 = null;
                        String str = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
                        if (str != null) {
                            patternMatcher = new PatternMatcher(str, SDK_VERSION);
                        }
                        str = sa.getNonConfigurationString(PARSE_IS_SYSTEM, SDK_VERSION);
                        if (str != null) {
                            patternMatcher = new PatternMatcher(str, PARSE_IS_SYSTEM);
                        }
                        str = sa.getNonConfigurationString(PARSE_CHATTY, SDK_VERSION);
                        if (str != null) {
                            patternMatcher = new PatternMatcher(str, PARSE_CHATTY);
                        }
                        sa.recycle();
                        if (patternMatcher2 != null) {
                            if (outInfo.info.uriPermissionPatterns == null) {
                                outInfo.info.uriPermissionPatterns = new PatternMatcher[PARSE_IS_SYSTEM];
                                outInfo.info.uriPermissionPatterns[SDK_VERSION] = patternMatcher2;
                            } else {
                                N = outInfo.info.uriPermissionPatterns.length;
                                PatternMatcher[] newp = new PatternMatcher[(N + PARSE_IS_SYSTEM)];
                                System.arraycopy(outInfo.info.uriPermissionPatterns, SDK_VERSION, newp, SDK_VERSION, N);
                                newp[N] = patternMatcher2;
                                outInfo.info.uriPermissionPatterns = newp;
                            }
                            outInfo.info.grantUriPermissions = true;
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } else if (parser.getName().equals("path-permission")) {
                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPathPermission);
                        PathPermission pathPermission = null;
                        String permission = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
                        String readPermission = sa.getNonConfigurationString(PARSE_IS_SYSTEM, SDK_VERSION);
                        if (readPermission == null) {
                            readPermission = permission;
                        }
                        String writePermission = sa.getNonConfigurationString(PARSE_CHATTY, SDK_VERSION);
                        if (writePermission == null) {
                            writePermission = permission;
                        }
                        boolean havePerm = RIGID_PARSER;
                        if (readPermission != null) {
                            readPermission = readPermission.intern();
                            havePerm = true;
                        }
                        if (writePermission != null) {
                            writePermission = writePermission.intern();
                            havePerm = true;
                        }
                        if (havePerm) {
                            PathPermission pathPermission2;
                            String path = sa.getNonConfigurationString(3, SDK_VERSION);
                            if (path != null) {
                                pathPermission2 = new PathPermission(path, SDK_VERSION, readPermission, writePermission);
                            }
                            path = sa.getNonConfigurationString(PARSE_MUST_BE_APK, SDK_VERSION);
                            if (path != null) {
                                pathPermission2 = new PathPermission(path, PARSE_IS_SYSTEM, readPermission, writePermission);
                            }
                            path = sa.getNonConfigurationString(MAX_PACKAGES_PER_APK, SDK_VERSION);
                            if (path != null) {
                                pathPermission2 = new PathPermission(path, PARSE_CHATTY, readPermission, writePermission);
                            }
                            sa.recycle();
                            if (pathPermission != null) {
                                if (outInfo.info.pathPermissions == null) {
                                    outInfo.info.pathPermissions = new PathPermission[PARSE_IS_SYSTEM];
                                    outInfo.info.pathPermissions[SDK_VERSION] = pathPermission;
                                } else {
                                    N = outInfo.info.pathPermissions.length;
                                    PathPermission[] newp2 = new PathPermission[(N + PARSE_IS_SYSTEM)];
                                    System.arraycopy(outInfo.info.pathPermissions, SDK_VERSION, newp2, SDK_VERSION, N);
                                    newp2[N] = pathPermission;
                                    outInfo.info.pathPermissions = newp2;
                                }
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                Slog.w(TAG, "No path, pathPrefix, or pathPattern for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        } else {
                            Slog.w(TAG, "No readPermission or writePermssion for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } else {
                        Slog.w(TAG, "Unknown element under <provider>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Service parseService(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestService);
        if (this.mParseServiceArgs == null) {
            this.mParseServiceArgs = new ParseComponentArgs(owner, outError, PARSE_CHATTY, SDK_VERSION, PARSE_IS_SYSTEM, PARSE_IGNORE_PROCESSES, 12, this.mSeparateProcesses, 6, 7, PARSE_MUST_BE_APK);
            this.mParseServiceArgs.tag = "<service>";
        }
        this.mParseServiceArgs.sa = sa;
        this.mParseServiceArgs.flags = flags;
        Service service = new Service(this.mParseServiceArgs, new ServiceInfo());
        if (outError[SDK_VERSION] != null) {
            sa.recycle();
            return null;
        }
        ServiceInfo serviceInfo;
        boolean setExported = sa.hasValue(MAX_PACKAGES_PER_APK);
        if (setExported) {
            service.info.exported = sa.getBoolean(MAX_PACKAGES_PER_APK, RIGID_PARSER);
        }
        String str = sa.getNonConfigurationString(3, SDK_VERSION);
        if (str == null) {
            service.info.permission = owner.applicationInfo.permission;
        } else {
            service.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        service.info.flags = SDK_VERSION;
        if (sa.getBoolean(9, RIGID_PARSER)) {
            serviceInfo = service.info;
            serviceInfo.flags |= PARSE_IS_SYSTEM;
        }
        if (sa.getBoolean(10, RIGID_PARSER)) {
            serviceInfo = service.info;
            serviceInfo.flags |= PARSE_CHATTY;
        }
        if (sa.getBoolean(14, RIGID_PARSER)) {
            serviceInfo = service.info;
            serviceInfo.flags |= PARSE_MUST_BE_APK;
        }
        if (sa.getBoolean(11, RIGID_PARSER)) {
            serviceInfo = service.info;
            serviceInfo.flags |= KeymasterDefs.KM_UINT_REP;
            if (service.info.exported && (flags & PARSE_IS_PRIVILEGED) == 0) {
                Slog.w(TAG, "Service exported request ignored due to singleUser: " + service.className + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                service.info.exported = RIGID_PARSER;
                setExported = true;
            }
        }
        serviceInfo = service.info;
        boolean z = sa.getBoolean(13, RIGID_PARSER);
        service.info.directBootAware = z;
        serviceInfo.encryptionAware = z;
        if (service.info.directBootAware) {
            ApplicationInfo applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= PARSE_COLLECT_CERTIFICATES;
        }
        sa.recycle();
        if ((owner.applicationInfo.privateFlags & PARSE_CHATTY) == 0 || service.info.processName != owner.packageName) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                    if (!setExported) {
                        service.info.exported = service.intents.size() <= 0 ? true : RIGID_PARSER;
                    }
                } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                    if (parser.getName().equals("intent-filter")) {
                        ServiceIntentInfo intent = new ServiceIntentInfo(service);
                        if (!parseIntent(res, parser, true, RIGID_PARSER, intent, outError)) {
                            return null;
                        }
                        service.intents.add(intent);
                    } else if (parser.getName().equals("meta-data")) {
                        Bundle parseMetaData = parseMetaData(res, parser, service.metaData, outError);
                        service.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return null;
                        }
                    } else {
                        Slog.w(TAG, "Unknown element under <service>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            if (setExported) {
                if (service.intents.size() <= 0) {
                }
                service.info.exported = service.intents.size() <= 0 ? true : RIGID_PARSER;
            }
            return service;
        }
        outError[SDK_VERSION] = "Heavy-weight applications can not have services in main process";
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseAllMetaData(Resources res, XmlResourceParser parser, String tag, Component<?> outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                return true;
            }
            if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, outInfo.metaData, outError);
                    outInfo.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return RIGID_PARSER;
                    }
                } else {
                    Slog.w(TAG, "Unknown element under " + tag + ": " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return true;
    }

    private Bundle parseMetaData(Resources res, XmlResourceParser parser, Bundle data, String[] outError) throws XmlPullParserException, IOException {
        String str = null;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestMetaData);
        if (data == null) {
            data = new Bundle();
        }
        String name = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
        if (name == null) {
            outError[SDK_VERSION] = "<meta-data> requires an android:name attribute";
            sa.recycle();
            return null;
        }
        name = name.intern();
        TypedValue v = sa.peekValue(PARSE_CHATTY);
        if (v == null || v.resourceId == 0) {
            v = sa.peekValue(PARSE_IS_SYSTEM);
            if (v == null) {
                outError[SDK_VERSION] = "<meta-data> requires an android:value or android:resource attribute";
                data = null;
            } else if (v.type == 3) {
                CharSequence cs = v.coerceToString();
                if (cs != null) {
                    str = cs.toString().intern();
                }
                data.putString(name, str);
            } else if (v.type == 18) {
                data.putBoolean(name, v.data != 0 ? true : RIGID_PARSER);
            } else if (v.type >= PARSE_FORWARD_LOCK && v.type <= 31) {
                data.putInt(name, v.data);
            } else if (v.type == PARSE_MUST_BE_APK) {
                data.putFloat(name, v.getFloat());
            } else {
                Slog.w(TAG, "<meta-data> only supports string, integer, float, color, boolean, and resource reference types: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
            }
        } else {
            data.putInt(name, v.resourceId);
        }
        sa.recycle();
        XmlUtils.skipCurrentTag(parser);
        return data;
    }

    private static VerifierInfo parseVerifier(Resources res, XmlPullParser parser, AttributeSet attrs, int flags) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestPackageVerifier);
        String packageName = sa.getNonResourceString(SDK_VERSION);
        String encodedPublicKey = sa.getNonResourceString(PARSE_IS_SYSTEM);
        sa.recycle();
        if (packageName == null || packageName.length() == 0) {
            Slog.i(TAG, "verifier package name was null; skipping");
            return null;
        }
        PublicKey publicKey = parsePublicKey(encodedPublicKey);
        if (publicKey != null) {
            return new VerifierInfo(packageName, publicKey);
        }
        Slog.i(TAG, "Unable to parse verifier public key for " + packageName);
        return null;
    }

    public static final PublicKey parsePublicKey(String encodedPublicKey) {
        if (encodedPublicKey == null) {
            Slog.w(TAG, "Could not parse null public key");
            return null;
        }
        try {
            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(encodedPublicKey, SDK_VERSION));
            try {
                return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA).generatePublic(keySpec);
            } catch (NoSuchAlgorithmException e) {
                Slog.wtf(TAG, "Could not parse public key: RSA KeyFactory not included in build");
                try {
                    return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC).generatePublic(keySpec);
                } catch (NoSuchAlgorithmException e2) {
                    Slog.wtf(TAG, "Could not parse public key: EC KeyFactory not included in build");
                    try {
                        return KeyFactory.getInstance("DSA").generatePublic(keySpec);
                    } catch (NoSuchAlgorithmException e3) {
                        Slog.wtf(TAG, "Could not parse public key: DSA KeyFactory not included in build");
                        return null;
                    } catch (InvalidKeySpecException e4) {
                        return null;
                    }
                } catch (InvalidKeySpecException e5) {
                    return KeyFactory.getInstance("DSA").generatePublic(keySpec);
                }
            } catch (InvalidKeySpecException e6) {
                return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC).generatePublic(keySpec);
            }
        } catch (IllegalArgumentException e7) {
            Slog.w(TAG, "Could not parse verifier public key; invalid Base64");
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseIntent(Resources res, XmlResourceParser parser, boolean allowGlobs, boolean allowAutoVerify, IntentInfo outInfo, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestIntentFilter);
        outInfo.setPriority(sa.getInt(PARSE_CHATTY, SDK_VERSION));
        TypedValue v = sa.peekValue(SDK_VERSION);
        if (v != null) {
            int i = v.resourceId;
            outInfo.labelRes = i;
            if (i == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        outInfo.icon = sa.getResourceId(PARSE_IS_SYSTEM, SDK_VERSION);
        outInfo.logo = sa.getResourceId(3, SDK_VERSION);
        outInfo.banner = sa.getResourceId(PARSE_MUST_BE_APK, SDK_VERSION);
        if (allowAutoVerify) {
            outInfo.setAutoVerify(sa.getBoolean(MAX_PACKAGES_PER_APK, RIGID_PARSER));
        }
        sa.recycle();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == PARSE_IS_SYSTEM || (type == 3 && parser.getDepth() <= outerDepth)) {
                outInfo.hasDefault = outInfo.hasCategory(Intent.CATEGORY_DEFAULT);
            } else if (!(type == 3 || type == PARSE_MUST_BE_APK)) {
                String nodeName = parser.getName();
                String value;
                if (nodeName.equals(InputProperty.ACTION)) {
                    value = parser.getAttributeValue(ANDROID_RESOURCES, KeyChain.EXTRA_NAME);
                    if (value == null || value == ProxyInfo.LOCAL_EXCL_LIST) {
                        outError[SDK_VERSION] = "No value supplied for <android:name>";
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                        outInfo.addAction(value);
                    }
                } else if (nodeName.equals(VideoColumns.CATEGORY)) {
                    value = parser.getAttributeValue(ANDROID_RESOURCES, KeyChain.EXTRA_NAME);
                    if (value == null || value == ProxyInfo.LOCAL_EXCL_LIST) {
                        outError[SDK_VERSION] = "No value supplied for <android:name>";
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                        outInfo.addCategory(value);
                    }
                } else if (nodeName.equals(VoiceInteractionSession.KEY_DATA)) {
                    sa = res.obtainAttributes(parser, R.styleable.AndroidManifestData);
                    String str = sa.getNonConfigurationString(SDK_VERSION, SDK_VERSION);
                    if (str != null) {
                        try {
                            outInfo.addDataType(str);
                        } catch (MalformedMimeTypeException e) {
                            outError[SDK_VERSION] = e.toString();
                            sa.recycle();
                            return RIGID_PARSER;
                        }
                    }
                    str = sa.getNonConfigurationString(PARSE_IS_SYSTEM, SDK_VERSION);
                    if (str != null) {
                        outInfo.addDataScheme(str);
                    }
                    str = sa.getNonConfigurationString(7, SDK_VERSION);
                    if (str != null) {
                        outInfo.addDataSchemeSpecificPart(str, SDK_VERSION);
                    }
                    str = sa.getNonConfigurationString(PARSE_IGNORE_PROCESSES, SDK_VERSION);
                    if (str != null) {
                        outInfo.addDataSchemeSpecificPart(str, PARSE_IS_SYSTEM);
                    }
                    str = sa.getNonConfigurationString(9, SDK_VERSION);
                    if (str != null) {
                        if (allowGlobs) {
                            outInfo.addDataSchemeSpecificPart(str, PARSE_CHATTY);
                        } else {
                            outError[SDK_VERSION] = "sspPattern not allowed here; ssp must be literal";
                            return RIGID_PARSER;
                        }
                    }
                    String host = sa.getNonConfigurationString(PARSE_CHATTY, SDK_VERSION);
                    String port = sa.getNonConfigurationString(3, SDK_VERSION);
                    if (host != null) {
                        outInfo.addDataAuthority(host, port);
                    }
                    str = sa.getNonConfigurationString(PARSE_MUST_BE_APK, SDK_VERSION);
                    if (str != null) {
                        outInfo.addDataPath(str, SDK_VERSION);
                    }
                    str = sa.getNonConfigurationString(MAX_PACKAGES_PER_APK, SDK_VERSION);
                    if (str != null) {
                        outInfo.addDataPath(str, PARSE_IS_SYSTEM);
                    }
                    str = sa.getNonConfigurationString(6, SDK_VERSION);
                    if (str != null) {
                        if (allowGlobs) {
                            outInfo.addDataPath(str, PARSE_CHATTY);
                        } else {
                            outError[SDK_VERSION] = "pathPattern not allowed here; path must be literal";
                            return RIGID_PARSER;
                        }
                    }
                    sa.recycle();
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <intent-filter>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        outInfo.hasDefault = outInfo.hasCategory(Intent.CATEGORY_DEFAULT);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean copyNeeded(int flags, Package p, PackageUserState state, Bundle metaData, int userId) {
        if (userId != 0) {
            return true;
        }
        if (state.enabled != 0) {
            if (p.applicationInfo.enabled != (state.enabled == PARSE_IS_SYSTEM ? true : RIGID_PARSER)) {
                return true;
            }
        }
        if (state.suspended != ((p.applicationInfo.flags & KeymasterDefs.KM_UINT_REP) != 0 ? true : RIGID_PARSER) || !state.installed || state.hidden || state.stopped) {
            return true;
        }
        if ((flags & PARSE_IS_PRIVILEGED) == 0 || (metaData == null && p.mAppMetaData == null)) {
            return (((flags & PARSE_ENFORCE_CODE) == 0 || p.usesLibraryFiles == null) && (StrictMode.PENALTY_GATHER & flags) == 0) ? RIGID_PARSER : true;
        } else {
            return true;
        }
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state) {
        return generateApplicationInfo(p, flags, state, UserHandle.getCallingUserId());
    }

    private static void updateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state) {
        boolean z = true;
        if (!sCompatibilityModeEnabled) {
            ai.disableCompatibilityMode();
        }
        if (state.installed) {
            ai.flags |= HistoryItem.STATE_SENSOR_ON_FLAG;
        } else {
            ai.flags &= -8388609;
        }
        if (state.suspended) {
            ai.flags |= KeymasterDefs.KM_UINT_REP;
        } else {
            ai.flags &= -1073741825;
        }
        if (state.hidden) {
            ai.privateFlags |= PARSE_IS_SYSTEM;
        } else {
            ai.privateFlags &= -2;
        }
        if (state.enabled == PARSE_IS_SYSTEM) {
            ai.enabled = true;
        } else if (state.enabled == PARSE_MUST_BE_APK) {
            if ((Document.FLAG_ARCHIVE & flags) == 0) {
                z = RIGID_PARSER;
            }
            ai.enabled = z;
        } else if (state.enabled == PARSE_CHATTY || state.enabled == 3) {
            ai.enabled = RIGID_PARSER;
        }
        ai.enabledSetting = state.enabled;
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state) || !p.isMatch(flags)) {
            return null;
        }
        if (copyNeeded(flags, p, state, null, userId) || ((Document.FLAG_ARCHIVE & flags) != 0 && state.enabled == PARSE_MUST_BE_APK)) {
            ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
            ai.initForUser(userId);
            HwFrameworkFactory.getHwPackageParser().changeApplicationEuidIfNeeded(ai, flags);
            if ((flags & PARSE_IS_PRIVILEGED) != 0) {
                ai.metaData = p.mAppMetaData;
            }
            if ((flags & PARSE_ENFORCE_CODE) != 0) {
                ai.sharedLibraryFiles = p.usesLibraryFiles;
            }
            if (state.stopped) {
                ai.flags |= StrictMode.PENALTY_DROPBOX;
            } else {
                ai.flags &= -2097153;
            }
            updateApplicationInfo(ai, flags, state);
            return ai;
        }
        updateApplicationInfo(p.applicationInfo, flags, state);
        return p.applicationInfo;
    }

    public static ApplicationInfo generateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state, int userId) {
        if (ai == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        ApplicationInfo ai2 = new ApplicationInfo(ai);
        ai2.initForUser(userId);
        HwFrameworkFactory.getHwPackageParser().changeApplicationEuidIfNeeded(ai2, flags);
        if (state.stopped) {
            ai2.flags |= StrictMode.PENALTY_DROPBOX;
        } else {
            ai2.flags &= -2097153;
        }
        updateApplicationInfo(ai2, flags, state);
        return ai2;
    }

    public static final PermissionInfo generatePermissionInfo(Permission p, int flags) {
        if (p == null) {
            return null;
        }
        if ((flags & PARSE_IS_PRIVILEGED) == 0) {
            return p.info;
        }
        PermissionInfo pi = new PermissionInfo(p.info);
        pi.metaData = p.metaData;
        return pi;
    }

    public static final PermissionGroupInfo generatePermissionGroupInfo(PermissionGroup pg, int flags) {
        if (pg == null) {
            return null;
        }
        if ((flags & PARSE_IS_PRIVILEGED) == 0) {
            return pg.info;
        }
        PermissionGroupInfo pgi = new PermissionGroupInfo(pg.info);
        pgi.metaData = pg.metaData;
        return pgi;
    }

    public static final ActivityInfo generateActivityInfo(Activity a, int flags, PackageUserState state, int userId) {
        if (a == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, a.owner, state, a.metaData, userId)) {
            return a.info;
        }
        ActivityInfo ai = new ActivityInfo(a.info);
        ai.metaData = a.metaData;
        ai.applicationInfo = generateApplicationInfo(a.owner, flags, state, userId);
        return ai;
    }

    public static final ActivityInfo generateActivityInfo(ActivityInfo ai, int flags, PackageUserState state, int userId) {
        if (ai == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        ActivityInfo ai2 = new ActivityInfo(ai);
        ai2.applicationInfo = generateApplicationInfo(ai2.applicationInfo, flags, state, userId);
        return ai2;
    }

    public static final ServiceInfo generateServiceInfo(Service s, int flags, PackageUserState state, int userId) {
        if (s == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, s.owner, state, s.metaData, userId)) {
            return s.info;
        }
        ServiceInfo si = new ServiceInfo(s.info);
        si.metaData = s.metaData;
        si.applicationInfo = generateApplicationInfo(s.owner, flags, state, userId);
        return si;
    }

    public static final ProviderInfo generateProviderInfo(Provider p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, p.owner, state, p.metaData, userId) && ((flags & PARSE_IS_EPHEMERAL) != 0 || p.info.uriPermissionPatterns == null)) {
            return p.info;
        }
        ProviderInfo pi = new ProviderInfo(p.info);
        pi.metaData = p.metaData;
        if ((flags & PARSE_IS_EPHEMERAL) == 0) {
            pi.uriPermissionPatterns = null;
        }
        pi.applicationInfo = generateApplicationInfo(p.owner, flags, state, userId);
        return pi;
    }

    public static final InstrumentationInfo generateInstrumentationInfo(Instrumentation i, int flags) {
        if (i == null) {
            return null;
        }
        if ((flags & PARSE_IS_PRIVILEGED) == 0) {
            return i.info;
        }
        InstrumentationInfo ii = new InstrumentationInfo(i.info);
        ii.metaData = i.metaData;
        return ii;
    }

    public static void setCompatibilityModeEnabled(boolean compatibilityModeEnabled) {
        sCompatibilityModeEnabled = compatibilityModeEnabled;
    }

    public static long readFullyIgnoringContents(InputStream in) throws IOException {
        byte[] buffer = (byte[]) sBuffer.getAndSet(null);
        if (buffer == null) {
            buffer = new byte[PARSE_FORCE_SDK];
        }
        int count = SDK_VERSION;
        while (true) {
            int n = in.read(buffer, SDK_VERSION, buffer.length);
            if (n != PARSE_DEFAULT_INSTALL_LOCATION) {
                count += n;
            } else {
                sBuffer.set(buffer);
                return (long) count;
            }
        }
    }

    public static void closeQuietly(StrictJarFile jarFile) {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (Exception e) {
            }
        }
    }
}
