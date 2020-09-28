package android.content.pm;

import android.Manifest;
import android.annotation.UnsupportedAppUsage;
import android.apex.ApexInfo;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.ResourcesManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParserCacheHelper;
import android.content.pm.PackageParserEx;
import android.content.pm.split.DefaultSplitAssetLoader;
import android.content.pm.split.SplitAssetDependencyLoader;
import android.content.pm.split.SplitAssetLoader;
import android.content.pm.split.SplitDependencyLoader;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.hardware.display.HwFoldScreenState;
import android.hwcontrol.HwWidgetFactory;
import android.hwtheme.HwThemeManager;
import android.media.TtmlUtils;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.permission.PermissionManager;
import android.provider.SettingsStringUtil;
import android.security.keystore.KeyProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.ByteStringUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.PackageUtils;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.apk.ApkSignatureVerifier;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.DumpHeapActivity;
import com.android.internal.os.ClassLoaderFactory;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.huawei.android.permission.ZosPermissionAdapter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedRef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PackageParser {
    public static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
    private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";
    public static final String APK_FILE_EXTENSION = ".apk";
    private static final Set<String> CHILD_PACKAGE_TAGS = new ArraySet();
    private static final int CURRENT_EMUI_SDK_VERSION = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
    private static final boolean DEBUG_BACKUP = false;
    private static final boolean DEBUG_JAR = false;
    private static final boolean DEBUG_PARSER = false;
    private static final int DEFAULT_MIN_SDK_VERSION = 1;
    private static final float DEFAULT_PRE_O_MAX_ASPECT_RATIO = 1.86f;
    private static final float DEFAULT_PRE_Q_MIN_ASPECT_RATIO = 1.333f;
    private static final float DEFAULT_PRE_Q_MIN_ASPECT_RATIO_WATCH = 1.0f;
    private static final int DEFAULT_TARGET_SDK_VERSION = 0;
    private static final boolean LOG_PARSE_TIMINGS = Build.IS_DEBUGGABLE;
    private static final int LOG_PARSE_TIMINGS_THRESHOLD_MS = 100;
    private static final boolean LOG_UNSAFE_BROADCASTS = false;
    private static final String METADATA_GESTURE_NAV_OPTIONS = "hw.gesture_nav_options";
    private static final String METADATA_MAX_ASPECT_RATIO = "android.max_aspect";
    private static final String METADATA_MIN_ASPECT_RATIO = "android.min_aspect";
    private static final String METADATA_NOTCH_SUPPORT = "android.notch_support";
    private static final String MNT_EXPAND = "/mnt/expand/";
    private static final boolean MULTI_PACKAGE_APK_ENABLED = (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROPERTY_CHILD_PACKAGES_ENABLED, false));
    @UnsupportedAppUsage
    public static final NewPermissionInfo[] NEW_PERMISSIONS = {new NewPermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, 4, 0), new NewPermissionInfo(Manifest.permission.READ_PHONE_STATE, 4, 0)};
    public static final int PARSE_CHATTY = Integer.MIN_VALUE;
    public static final int PARSE_COLLECT_CERTIFICATES = 32;
    private static final int PARSE_DEFAULT_INSTALL_LOCATION = -1;
    private static final int PARSE_DEFAULT_TARGET_SANDBOX = 1;
    public static final int PARSE_ENFORCE_CODE = 64;
    public static final int PARSE_EXTERNAL_STORAGE = 8;
    public static final int PARSE_IGNORE_PROCESSES = 2;
    public static final int PARSE_IS_SYSTEM_DIR = 16;
    public static final int PARSE_MUST_BE_APK = 1;
    private static final String PROPERTY_CHILD_PACKAGES_ENABLED = "persist.sys.child_packages_enabled";
    private static final int RECREATE_ON_CONFIG_CHANGES_MASK = 3;
    private static final boolean RIGID_PARSER = false;
    private static final Set<String> SAFE_BROADCASTS = new ArraySet();
    private static final String[] SDK_CODENAMES = Build.VERSION.ACTIVE_CODENAMES;
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
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
    private static final String TAG_PACKAGE_VERIFIER = "package-verifier";
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
    private static final String TAG_USES_SPLIT = "uses-split";
    private static float mDefaultMaxAspectRatio = DEFAULT_PRE_O_MAX_ASPECT_RATIO;
    private static float mExclusionNavBar = 0.0f;
    private static boolean mFristAddView = true;
    private static boolean mFullScreenDisplay = false;
    private static IHwPackageParser mHwPackageParser = null;
    private static float mScreenAspectRatio = 0.0f;
    public static final AtomicInteger sCachedPackageReadCount = new AtomicInteger();
    private static boolean sCompatibilityModeEnabled = true;
    private static int sCurrentEmuiSysImgVersion = 0;
    private static final Comparator<String> sSplitNameComparator = new SplitNameComparator();
    private static boolean sUseRoundIcon = false;
    @Deprecated
    private String mArchiveSourcePath;
    private File mCacheDir;
    @UnsupportedAppUsage
    private Callback mCallback;
    private DisplayMetrics mMetrics = new DisplayMetrics();
    private boolean mOnlyCoreApps;
    private int mParseError = 1;
    private ParsePackageItemArgs mParseInstrumentationArgs;
    private String[] mSeparateProcesses;

    public interface Callback {
        String[] getOverlayApks(String str);

        String[] getOverlayPaths(String str, String str2);

        boolean hasFeature(String str);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ParseFlags {
    }

    static {
        CHILD_PACKAGE_TAGS.add(TAG_APPLICATION);
        CHILD_PACKAGE_TAGS.add(TAG_USES_PERMISSION);
        CHILD_PACKAGE_TAGS.add(TAG_USES_PERMISSION_SDK_M);
        CHILD_PACKAGE_TAGS.add(TAG_USES_PERMISSION_SDK_23);
        CHILD_PACKAGE_TAGS.add(TAG_USES_CONFIGURATION);
        CHILD_PACKAGE_TAGS.add(TAG_USES_FEATURE);
        CHILD_PACKAGE_TAGS.add(TAG_FEATURE_GROUP);
        CHILD_PACKAGE_TAGS.add(TAG_USES_SDK);
        CHILD_PACKAGE_TAGS.add(TAG_SUPPORT_SCREENS);
        CHILD_PACKAGE_TAGS.add(TAG_INSTRUMENTATION);
        CHILD_PACKAGE_TAGS.add(TAG_USES_GL_TEXTURE);
        CHILD_PACKAGE_TAGS.add(TAG_COMPATIBLE_SCREENS);
        CHILD_PACKAGE_TAGS.add(TAG_SUPPORTS_INPUT);
        CHILD_PACKAGE_TAGS.add(TAG_EAT_COMMENT);
        SAFE_BROADCASTS.add(Intent.ACTION_BOOT_COMPLETED);
    }

    public static void setCurrentEmuiSysImgVersion(int version) {
        sCurrentEmuiSysImgVersion = version;
        SystemProperties.set("persist.sys.version", String.valueOf(sCurrentEmuiSysImgVersion));
        Slog.d(TAG, "setCurrentEmuiSysImgVersion version:" + version);
    }

    private static void initFullScreenData() {
        if (mFristAddView) {
            mDefaultMaxAspectRatio = HwFrameworkFactory.getHwPackageParser().getDefaultNonFullMaxRatio();
            mScreenAspectRatio = HwFrameworkFactory.getHwPackageParser().getDeviceMaxRatio();
            mExclusionNavBar = HwFrameworkFactory.getHwPackageParser().getExclusionNavBarMaxRatio();
            mFullScreenDisplay = HwFrameworkFactory.getHwPackageParser().isFullScreenDevice();
            mFristAddView = false;
        }
    }

    public static class NewPermissionInfo {
        public final int fileVersion;
        @UnsupportedAppUsage
        public final String name;
        @UnsupportedAppUsage
        public final int sdkVersion;

        public NewPermissionInfo(String name2, int sdkVersion2, int fileVersion2) {
            this.name = name2;
            this.sdkVersion = sdkVersion2;
            this.fileVersion = fileVersion2;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ParsePackageItemArgs {
        final int bannerRes;
        final int iconRes;
        final int labelRes;
        final int logoRes;
        final int nameRes;
        final String[] outError;
        final Package owner;
        final int roundIconRes;
        TypedArray sa;
        String tag;

        ParsePackageItemArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _roundIconRes, int _logoRes, int _bannerRes) {
            this.owner = _owner;
            this.outError = _outError;
            this.nameRes = _nameRes;
            this.labelRes = _labelRes;
            this.iconRes = _iconRes;
            this.logoRes = _logoRes;
            this.bannerRes = _bannerRes;
            this.roundIconRes = _roundIconRes;
        }
    }

    @VisibleForTesting
    public static class ParseComponentArgs extends ParsePackageItemArgs {
        final int descriptionRes;
        final int enabledRes;
        int flags;
        final int processRes;
        final String[] sepProcesses;

        public ParseComponentArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _roundIconRes, int _logoRes, int _bannerRes, String[] _sepProcesses, int _processRes, int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _roundIconRes, _logoRes, _bannerRes);
            this.sepProcesses = _sepProcesses;
            this.processRes = _processRes;
            this.descriptionRes = _descriptionRes;
            this.enabledRes = _enabledRes;
        }
    }

    public static class PackageLite {
        public final String baseCodePath;
        public final int baseRevisionCode;
        public final String codePath;
        public final String[] configForSplit;
        public final boolean coreApp;
        public final boolean debuggable;
        public final boolean extractNativeLibs;
        public final boolean hasPlugin;
        @UnsupportedAppUsage
        public final int installLocation;
        public final boolean[] isFeatureSplits;
        public final boolean isolatedSplits;
        public final boolean multiArch;
        @UnsupportedAppUsage
        public final String packageName;
        public final String[] splitCodePaths;
        public final String[] splitNames;
        public final int[] splitPrivateFlags;
        public final int[] splitRevisionCodes;
        public final int[] splitVersionCodes;
        public final boolean use32bitAbi;
        public final String[] usesSplitNames;
        public final VerifierInfo[] verifiers;
        public final int versionCode;
        public final int versionCodeMajor;

        public PackageLite(String codePath2, ApkLite baseApk, String[] splitNames2, boolean[] isFeatureSplits2, String[] usesSplitNames2, String[] configForSplit2, String[] splitCodePaths2, int[] splitVersionCodes2, int[] splitRevisionCodes2, int[] splitPrivateFlags2, boolean hasPlugin2) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.versionCodeMajor = baseApk.versionCodeMajor;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames2;
            this.isFeatureSplits = isFeatureSplits2;
            this.usesSplitNames = usesSplitNames2;
            this.configForSplit = configForSplit2;
            this.codePath = codePath2;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths2;
            this.baseRevisionCode = baseApk.revisionCode;
            this.splitVersionCodes = splitVersionCodes2;
            this.splitRevisionCodes = splitRevisionCodes2;
            this.coreApp = baseApk.coreApp;
            this.debuggable = baseApk.debuggable;
            this.multiArch = baseApk.multiArch;
            this.use32bitAbi = baseApk.use32bitAbi;
            this.extractNativeLibs = baseApk.extractNativeLibs;
            this.isolatedSplits = baseApk.isolatedSplits;
            this.splitPrivateFlags = splitPrivateFlags2;
            this.hasPlugin = hasPlugin2;
        }

        public PackageLite(String codePath2, ApkLite baseApk, String[] splitNames2, boolean[] isFeatureSplits2, String[] usesSplitNames2, String[] configForSplit2, String[] splitCodePaths2, int[] splitRevisionCodes2) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.versionCodeMajor = baseApk.versionCodeMajor;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames2;
            this.isFeatureSplits = isFeatureSplits2;
            this.usesSplitNames = usesSplitNames2;
            this.configForSplit = configForSplit2;
            this.codePath = codePath2;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths2;
            this.baseRevisionCode = baseApk.revisionCode;
            this.splitRevisionCodes = splitRevisionCodes2;
            this.coreApp = baseApk.coreApp;
            this.debuggable = baseApk.debuggable;
            this.multiArch = baseApk.multiArch;
            this.use32bitAbi = baseApk.use32bitAbi;
            this.extractNativeLibs = baseApk.extractNativeLibs;
            this.isolatedSplits = baseApk.isolatedSplits;
            this.splitVersionCodes = new int[(splitNames2 != null ? splitNames2.length : 0)];
            this.splitPrivateFlags = new int[(splitNames2 != null ? splitNames2.length : 0)];
            this.hasPlugin = false;
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList<>();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }
    }

    public static class ApkLite {
        public final String codePath;
        public final String configForSplit;
        public final boolean coreApp;
        public final boolean debuggable;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public boolean isFeatureSplit;
        public final boolean isPlugin;
        public final boolean isSplitRequired;
        public final boolean isolatedSplits;
        public final int minSdkVersion;
        public final boolean multiArch;
        public final String packageName;
        public final int revisionCode;
        public final SigningDetails signingDetails;
        public final String splitName;
        public final int targetSdkVersion;
        public final boolean use32bitAbi;
        public final boolean useEmbeddedDex;
        public final String usesSplitName;
        public final VerifierInfo[] verifiers;
        public final int versionCode;
        public final int versionCodeMajor;

        public ApkLite(String codePath2, String packageName2, String splitName2, boolean isFeatureSplit2, String configForSplit2, String usesSplitName2, boolean isSplitRequired2, int versionCode2, int versionCodeMajor2, int revisionCode2, int installLocation2, List<VerifierInfo> verifiers2, SigningDetails signingDetails2, boolean coreApp2, boolean debuggable2, boolean multiArch2, boolean use32bitAbi2, boolean preferCodeIntegrity, boolean extractNativeLibs2, boolean isolatedSplits2, int minSdkVersion2, int targetSdkVersion2, boolean isPlugin2) {
            this.codePath = codePath2;
            this.packageName = packageName2;
            this.splitName = splitName2;
            this.isFeatureSplit = isFeatureSplit2;
            this.configForSplit = configForSplit2;
            this.usesSplitName = usesSplitName2;
            this.versionCode = versionCode2;
            this.versionCodeMajor = versionCodeMajor2;
            this.revisionCode = revisionCode2;
            this.installLocation = installLocation2;
            this.signingDetails = signingDetails2;
            this.verifiers = (VerifierInfo[]) verifiers2.toArray(new VerifierInfo[verifiers2.size()]);
            this.coreApp = coreApp2;
            this.debuggable = debuggable2;
            this.multiArch = multiArch2;
            this.use32bitAbi = use32bitAbi2;
            this.useEmbeddedDex = preferCodeIntegrity;
            this.extractNativeLibs = extractNativeLibs2;
            this.isolatedSplits = isolatedSplits2;
            this.isSplitRequired = isSplitRequired2;
            this.minSdkVersion = minSdkVersion2;
            this.targetSdkVersion = targetSdkVersion2;
            this.isPlugin = isPlugin2;
        }

        public ApkLite(String codePath2, String packageName2, String splitName2, boolean isFeatureSplit2, String configForSplit2, String usesSplitName2, boolean isSplitRequired2, int versionCode2, int versionCodeMajor2, int revisionCode2, int installLocation2, List<VerifierInfo> verifiers2, SigningDetails signingDetails2, boolean coreApp2, boolean debuggable2, boolean multiArch2, boolean use32bitAbi2, boolean useEmbeddedDex2, boolean extractNativeLibs2, boolean isolatedSplits2, int minSdkVersion2, int targetSdkVersion2) {
            this.codePath = codePath2;
            this.packageName = packageName2;
            this.splitName = splitName2;
            this.isFeatureSplit = isFeatureSplit2;
            this.configForSplit = configForSplit2;
            this.usesSplitName = usesSplitName2;
            this.versionCode = versionCode2;
            this.versionCodeMajor = versionCodeMajor2;
            this.revisionCode = revisionCode2;
            this.installLocation = installLocation2;
            this.signingDetails = signingDetails2;
            this.verifiers = (VerifierInfo[]) verifiers2.toArray(new VerifierInfo[verifiers2.size()]);
            this.coreApp = coreApp2;
            this.debuggable = debuggable2;
            this.multiArch = multiArch2;
            this.use32bitAbi = use32bitAbi2;
            this.useEmbeddedDex = useEmbeddedDex2;
            this.extractNativeLibs = extractNativeLibs2;
            this.isolatedSplits = isolatedSplits2;
            this.isSplitRequired = isSplitRequired2;
            this.minSdkVersion = minSdkVersion2;
            this.targetSdkVersion = targetSdkVersion2;
            this.isPlugin = false;
        }

        public long getLongVersionCode() {
            return PackageInfo.composeLongVersionCode(this.versionCodeMajor, this.versionCode);
        }
    }

    /* access modifiers changed from: private */
    public static class CachedComponentArgs {
        ParseComponentArgs mActivityAliasArgs;
        ParseComponentArgs mActivityArgs;
        ParseComponentArgs mProviderArgs;
        ParseComponentArgs mServiceArgs;

        private CachedComponentArgs() {
        }
    }

    @UnsupportedAppUsage
    public PackageParser() {
        this.mMetrics.setToDefaults();
        mHwPackageParser = HwFrameworkFactory.getHwPackageParser();
        initFullScreenData();
    }

    @UnsupportedAppUsage
    public void setSeparateProcesses(String[] procs) {
        this.mSeparateProcesses = procs;
    }

    public void setOnlyCoreApps(boolean onlyCoreApps) {
        this.mOnlyCoreApps = onlyCoreApps;
    }

    public void setDisplayMetrics(DisplayMetrics metrics) {
        this.mMetrics = metrics;
    }

    public void setCacheDir(File cacheDir) {
        this.mCacheDir = cacheDir;
    }

    public static final class CallbackImpl implements Callback {
        private final PackageManager mPm;

        public CallbackImpl(PackageManager pm) {
            this.mPm = pm;
        }

        @Override // android.content.pm.PackageParser.Callback
        public boolean hasFeature(String feature) {
            return this.mPm.hasSystemFeature(feature);
        }

        @Override // android.content.pm.PackageParser.Callback
        public String[] getOverlayPaths(String targetPackageName, String targetPath) {
            return null;
        }

        @Override // android.content.pm.PackageParser.Callback
        public String[] getOverlayApks(String targetPackageName) {
            return null;
        }
    }

    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    public static final boolean isApkFile(File file) {
        return isApkPath(file.getName());
    }

    public static boolean isApkPath(String path) {
        return path.endsWith(APK_FILE_EXTENSION);
    }

    @UnsupportedAppUsage
    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions, state, UserHandle.getCallingUserId());
    }

    private static boolean checkUseInstalledOrHidden(int flags, PackageUserState state, ApplicationInfo appInfo) {
        if ((flags & 536870912) == 0 && !state.installed && appInfo != null && appInfo.hiddenUntilInstalled) {
            return false;
        }
        if (!state.isAvailable(flags)) {
            if (appInfo == null || !appInfo.isSystemApp()) {
                return false;
            }
            if ((4202496 & flags) == 0 && (536870912 & flags) == 0) {
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean isAvailable(PackageUserState state) {
        return checkUseInstalledOrHidden(0, state, null);
    }

    @UnsupportedAppUsage
    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state, int userId) {
        int N;
        int N2;
        int N3;
        int N4;
        if (!checkUseInstalledOrHidden(flags, state, p.applicationInfo)) {
            return null;
        }
        if (!p.isMatch(flags)) {
            return null;
        }
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.splitNames = p.splitNames;
        pi.versionCode = p.mVersionCode;
        pi.versionCodeMajor = p.mVersionCodeMajor;
        pi.baseRevisionCode = p.baseRevisionCode;
        pi.splitRevisionCodes = p.splitRevisionCodes;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.installLocation = p.installLocation;
        pi.isStub = p.isStub;
        pi.coreApp = p.coreApp;
        if (!((pi.applicationInfo.flags & 1) == 0 && (pi.applicationInfo.flags & 128) == 0)) {
            pi.requiredForAllUsers = p.mRequiredForAllUsers;
        }
        pi.restrictedAccountType = p.mRestrictedAccountType;
        pi.requiredAccountType = p.mRequiredAccountType;
        pi.overlayTarget = p.mOverlayTarget;
        pi.targetOverlayableName = p.mOverlayTargetName;
        pi.overlayCategory = p.mOverlayCategory;
        pi.overlayPriority = p.mOverlayPriority;
        pi.mOverlayIsStatic = p.mOverlayIsStatic;
        pi.compileSdkVersion = p.mCompileSdkVersion;
        pi.compileSdkVersionCodename = p.mCompileSdkVersionCodename;
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if ((flags & 256) != 0) {
            pi.gids = gids;
        }
        if ((flags & 16384) != 0) {
            int N5 = p.configPreferences != null ? p.configPreferences.size() : 0;
            if (N5 > 0) {
                pi.configPreferences = new ConfigurationInfo[N5];
                p.configPreferences.toArray(pi.configPreferences);
            }
            int N6 = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N6 > 0) {
                pi.reqFeatures = new FeatureInfo[N6];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
            int N7 = p.featureGroups != null ? p.featureGroups.size() : 0;
            if (N7 > 0) {
                pi.featureGroups = new FeatureGroupInfo[N7];
                p.featureGroups.toArray(pi.featureGroups);
            }
        }
        if ((flags & 1) != 0) {
            int N8 = p.activities.size();
            if (N8 > 0) {
                ActivityInfo[] res = new ActivityInfo[N8];
                int num = 0;
                int i = 0;
                while (i < N8) {
                    Activity a = p.activities.get(i);
                    if (state.isMatch(a.info, flags) && !PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME.equals(a.className)) {
                        res[num] = generateActivityInfo(a, flags, state, userId);
                        num++;
                    }
                    i++;
                    N8 = N8;
                }
                pi.activities = (ActivityInfo[]) ArrayUtils.trimToSize(res, num);
            }
        }
        if ((flags & 2) != 0 && (N4 = p.receivers.size()) > 0) {
            int num2 = 0;
            ActivityInfo[] res2 = new ActivityInfo[N4];
            for (int i2 = 0; i2 < N4; i2++) {
                Activity a2 = p.receivers.get(i2);
                if (state.isMatch(a2.info, flags)) {
                    res2[num2] = generateActivityInfo(a2, flags, state, userId);
                    num2++;
                }
            }
            pi.receivers = (ActivityInfo[]) ArrayUtils.trimToSize(res2, num2);
        }
        if ((flags & 4) != 0 && (N3 = p.services.size()) > 0) {
            int num3 = 0;
            ServiceInfo[] res3 = new ServiceInfo[N3];
            for (int i3 = 0; i3 < N3; i3++) {
                Service s = p.services.get(i3);
                if (state.isMatch(s.info, flags)) {
                    res3[num3] = generateServiceInfo(s, flags, state, userId);
                    num3++;
                }
            }
            pi.services = (ServiceInfo[]) ArrayUtils.trimToSize(res3, num3);
        }
        if ((flags & 8) != 0 && (N2 = p.providers.size()) > 0) {
            int num4 = 0;
            ProviderInfo[] res4 = new ProviderInfo[N2];
            for (int i4 = 0; i4 < N2; i4++) {
                Provider pr = p.providers.get(i4);
                if (state.isMatch(pr.info, flags)) {
                    res4[num4] = generateProviderInfo(pr, flags, state, userId);
                    num4++;
                }
            }
            pi.providers = (ProviderInfo[]) ArrayUtils.trimToSize(res4, num4);
        }
        if ((flags & 16) != 0 && (N = p.instrumentation.size()) > 0) {
            pi.instrumentation = new InstrumentationInfo[N];
            for (int i5 = 0; i5 < N; i5++) {
                pi.instrumentation[i5] = generateInstrumentationInfo(p.instrumentation.get(i5), flags);
            }
        }
        if ((flags & 4096) != 0) {
            int N9 = p.permissions.size();
            if (N9 > 0) {
                pi.permissions = new PermissionInfo[N9];
                for (int i6 = 0; i6 < N9; i6++) {
                    pi.permissions[i6] = generatePermissionInfo(p.permissions.get(i6), flags);
                }
            }
            int N10 = p.requestedPermissions.size();
            if (N10 > 0) {
                pi.requestedPermissions = new String[N10];
                pi.requestedPermissionsFlags = new int[N10];
                for (int i7 = 0; i7 < N10; i7++) {
                    String perm = p.requestedPermissions.get(i7);
                    pi.requestedPermissions[i7] = perm;
                    int[] iArr = pi.requestedPermissionsFlags;
                    iArr[i7] = iArr[i7] | 1;
                    if (grantedPermissions != null && grantedPermissions.contains(perm)) {
                        int[] iArr2 = pi.requestedPermissionsFlags;
                        iArr2[i7] = iArr2[i7] | 2;
                    }
                }
            }
        }
        if ((flags & 64) != 0) {
            if (p.mSigningDetails.hasPastSigningCertificates()) {
                pi.signatures = new Signature[1];
                pi.signatures[0] = p.mSigningDetails.pastSigningCertificates[0];
            } else if (p.mRealSigningDetails.hasSignatures()) {
                int numberOfSigs = p.mRealSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs];
                System.arraycopy(p.mRealSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs);
            } else if (p.mSigningDetails.hasSignatures()) {
                int numberOfSigs2 = p.mSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs2];
                System.arraycopy(p.mSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs2);
            }
        }
        if ((134217728 & flags) != 0) {
            if (p.mSigningDetails.hasPastSigningCertificates()) {
                pi.signatures = new Signature[1];
                pi.signatures[0] = p.mSigningDetails.pastSigningCertificates[0];
            } else if (p.mRealSigningDetails.hasSignatures()) {
                int numberOfSigs3 = p.mRealSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs3];
                System.arraycopy(p.mRealSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs3);
            } else if (p.mSigningDetails.hasSignatures()) {
                int numberOfSigs4 = p.mSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs4];
                System.arraycopy(p.mSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs4);
            }
            if (p.mRealSigningDetails.hasSignatures()) {
                pi.signingInfo = new SigningInfo(p.mRealSigningDetails);
            } else if (p.mSigningDetails != SigningDetails.UNKNOWN) {
                pi.signingInfo = new SigningInfo(p.mSigningDetails);
            } else {
                pi.signingInfo = null;
            }
        }
        return pi;
    }

    private static class SplitNameComparator implements Comparator<String> {
        private SplitNameComparator() {
        }

        public int compare(String lhs, String rhs) {
            if (lhs == null) {
                return -1;
            }
            if (rhs == null) {
                return 1;
            }
            return lhs.compareTo(rhs);
        }
    }

    @UnsupportedAppUsage
    public static PackageLite parsePackageLite(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackageLite(packageFile, flags);
        }
        return parseMonolithicPackageLite(packageFile, flags);
    }

    private static PackageLite parseMonolithicPackageLite(File packageFile, int flags) throws PackageParserException {
        Trace.traceBegin(262144, "parseApkLite");
        ApkLite baseApk = parseApkLite(packageFile, flags);
        String packagePath = packageFile.getAbsolutePath();
        Trace.traceEnd(262144);
        return new PackageLite(packagePath, baseApk, null, null, null, null, null, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x01e5  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01e8  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01f0  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01f6  */
    static PackageLite parseClusterPackageLite(File packageDir, int flags) throws PackageParserException {
        int size;
        File[] files = packageDir.listFiles();
        if (!ArrayUtils.isEmpty(files)) {
            int versionCode = 0;
            int pluginVersionCode = 0;
            Trace.traceBegin(262144, "parseApkLite");
            ArrayMap<String, ApkLite> apks = new ArrayMap<>();
            String packageName = null;
            for (File file : files) {
                if (isApkFile(file)) {
                    ApkLite lite = parseApkLite(file, flags);
                    if (!HwFrameworkFactory.getHwPackageParser().needStopApp(lite.packageName, file)) {
                        if (!lite.isPlugin && versionCode == 0) {
                            versionCode = lite.versionCode;
                        } else if (lite.isPlugin && pluginVersionCode == 0) {
                            pluginVersionCode = lite.versionCode;
                        }
                        if (packageName == null) {
                            packageName = lite.packageName;
                        } else if (!packageName.equals(lite.packageName)) {
                            throw new PackageParserException(-101, "Inconsistent package " + lite.packageName + " in " + file + "; expected " + packageName);
                        } else if (versionCode != lite.versionCode && !lite.isPlugin) {
                            throw new PackageParserException(-101, "Inconsistent version " + lite.versionCode + " in " + file + "; expected " + versionCode);
                        } else if (lite.isPlugin && IHwPluginManager.compareAPIMajorVersion(lite.versionCode, pluginVersionCode) != 0) {
                            throw new PackageParserException(-101, "Incompatible plugin version " + lite.versionCode + " in " + file + " with " + pluginVersionCode);
                        }
                        if (apks.put(lite.splitName, lite) != null) {
                            throw new PackageParserException(-101, "Split name " + lite.splitName + " defined more than once; most recent was " + file);
                        }
                    } else {
                        throw new PackageParserException(-2, "need to stop install app: " + lite.packageName + " in " + file);
                    }
                }
            }
            Trace.traceEnd(262144);
            if (versionCode == 0 || pluginVersionCode == 0 || IHwPluginManager.compareAPIMajorVersion(versionCode, pluginVersionCode) == 0) {
                ApkLite baseApk = apks.remove(null);
                if (baseApk != null) {
                    int size2 = apks.size();
                    String[] splitNames = null;
                    boolean[] isFeatureSplits = null;
                    String[] usesSplitNames = null;
                    String[] configForSplits = null;
                    String[] splitCodePaths = null;
                    int[] splitVersionCodes = null;
                    int[] splitRevisionCodes = null;
                    int[] splitPrivateFlags = null;
                    if (size2 > 0) {
                        isFeatureSplits = new boolean[size2];
                        usesSplitNames = new String[size2];
                        configForSplits = new String[size2];
                        splitCodePaths = new String[size2];
                        splitVersionCodes = new int[size2];
                        splitRevisionCodes = new int[size2];
                        splitPrivateFlags = new int[size2];
                        String[] splitNames2 = (String[]) apks.keySet().toArray(new String[size2]);
                        Arrays.sort(splitNames2, sSplitNameComparator);
                        int i = 0;
                        while (i < size2) {
                            ApkLite apk = apks.get(splitNames2[i]);
                            usesSplitNames[i] = apk.usesSplitName;
                            isFeatureSplits[i] = apk.isFeatureSplit;
                            configForSplits[i] = apk.configForSplit;
                            try {
                                size = size2;
                                try {
                                    splitCodePaths[i] = new File(apk.codePath).getCanonicalPath();
                                } catch (IOException e) {
                                }
                            } catch (IOException e2) {
                                size = size2;
                                splitCodePaths[i] = apk.codePath;
                                splitVersionCodes[i] = apk.versionCode;
                                splitRevisionCodes[i] = apk.revisionCode;
                                splitPrivateFlags[i] = !apk.isFeatureSplit ? 1073741824 : 536870912;
                                splitPrivateFlags[i] = !apk.isPlugin ? splitPrivateFlags[i] | Integer.MIN_VALUE : splitPrivateFlags[i];
                                i++;
                                splitNames2 = splitNames2;
                                size2 = size;
                            }
                            splitVersionCodes[i] = apk.versionCode;
                            splitRevisionCodes[i] = apk.revisionCode;
                            splitPrivateFlags[i] = !apk.isFeatureSplit ? 1073741824 : 536870912;
                            splitPrivateFlags[i] = !apk.isPlugin ? splitPrivateFlags[i] | Integer.MIN_VALUE : splitPrivateFlags[i];
                            i++;
                            splitNames2 = splitNames2;
                            size2 = size;
                        }
                        splitNames = splitNames2;
                    }
                    return new PackageLite(packageDir.getAbsolutePath(), baseApk, splitNames, isFeatureSplits, usesSplitNames, configForSplits, splitCodePaths, splitVersionCodes, splitRevisionCodes, splitPrivateFlags, pluginVersionCode != 0);
                }
                throw new PackageParserException(-101, "Missing base APK in " + packageDir);
            }
            throw new PackageParserException(-101, "Incompatible plugin version " + pluginVersionCode + " with base " + versionCode);
        }
        throw new PackageParserException(-100, "No packages found in split");
    }

    @UnsupportedAppUsage
    public Package parsePackage(File packageFile, int flags, boolean useCaches, int hwFlags) throws PackageParserException {
        Package parsed;
        Package parsed2 = useCaches ? getCachedResult(packageFile, flags) : null;
        if (parsed2 != null) {
            return parsed2;
        }
        long cacheTime = 0;
        long parseTime = LOG_PARSE_TIMINGS ? SystemClock.uptimeMillis() : 0;
        if (packageFile.isDirectory()) {
            parsed = parseClusterPackage(packageFile, flags, hwFlags);
        } else {
            parsed = parseMonolithicPackage(packageFile, flags, hwFlags);
        }
        if (parsed.mAppMetaData != null && parsed.mAppMetaData.getBoolean("permZA", false)) {
            Slog.w(TAG, "meta-data permZA is true.");
            ZosPermissionAdapter.getInstance().translatePermissionName(parsed);
            parsed.applicationInfo.hwFlags |= 1048576;
        }
        if (LOG_PARSE_TIMINGS) {
            cacheTime = SystemClock.uptimeMillis();
        }
        cacheResult(packageFile, flags, parsed);
        if (LOG_PARSE_TIMINGS) {
            long parseTime2 = cacheTime - parseTime;
            long cacheTime2 = SystemClock.uptimeMillis() - cacheTime;
            if (parseTime2 + cacheTime2 > 100) {
                Slog.i(TAG, "Parse times for '" + packageFile + "': parse=" + parseTime2 + "ms, update_cache=" + cacheTime2 + " ms");
            }
        }
        return parsed;
    }

    @UnsupportedAppUsage
    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        return parsePackage(packageFile, flags, false, 0);
    }

    public Package parsePackage(File packageFile, int flags, boolean useCaches) throws PackageParserException {
        return parsePackage(packageFile, flags, useCaches, 0);
    }

    private String getCacheKey(File packageFile, int flags) {
        return packageFile.getName() + '-' + flags;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Package fromCacheEntry(byte[] bytes) {
        return fromCacheEntryStatic(bytes);
    }

    @VisibleForTesting
    public static Package fromCacheEntryStatic(byte[] bytes) {
        Parcel p = Parcel.obtain();
        p.unmarshall(bytes, 0, bytes.length);
        p.setDataPosition(0);
        new PackageParserCacheHelper.ReadHelper(p).startAndInstall();
        Package pkg = new Package(p);
        p.recycle();
        sCachedPackageReadCount.incrementAndGet();
        return pkg;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public byte[] toCacheEntry(Package pkg) {
        return toCacheEntryStatic(pkg);
    }

    @VisibleForTesting
    public static byte[] toCacheEntryStatic(Package pkg) {
        Parcel p = Parcel.obtain();
        PackageParserCacheHelper.WriteHelper helper = new PackageParserCacheHelper.WriteHelper(p);
        pkg.writeToParcel(p, 0);
        helper.finishAndUninstall();
        byte[] serialized = p.marshall();
        p.recycle();
        return serialized;
    }

    private static boolean isCacheUpToDate(File packageFile, File cacheFile) {
        try {
            if (Os.stat(packageFile.getAbsolutePath()).st_mtime < Os.stat(cacheFile.getAbsolutePath()).st_mtime) {
                return true;
            }
            return false;
        } catch (ErrnoException ee) {
            if (ee.errno != OsConstants.ENOENT) {
                Slog.w("Error while stating package cache : ", ee);
            }
            return false;
        }
    }

    private Package getCachedResult(File packageFile, int flags) {
        String[] overlayApks;
        if (this.mCacheDir == null) {
            return null;
        }
        File cacheFile = new File(this.mCacheDir, getCacheKey(packageFile, flags));
        try {
            if (!isCacheUpToDate(packageFile, cacheFile)) {
                return null;
            }
            Package p = fromCacheEntry(IoUtils.readFileAsByteArray(cacheFile.getAbsolutePath()));
            if (!(this.mCallback == null || (overlayApks = this.mCallback.getOverlayApks(p.packageName)) == null || overlayApks.length <= 0)) {
                for (String overlayApk : overlayApks) {
                    if (!isCacheUpToDate(new File(overlayApk), cacheFile)) {
                        return null;
                    }
                }
            }
            return p;
        } catch (Throwable e) {
            Slog.w(TAG, "Error reading package cache: ", e);
            String fileName = cacheFile.getName();
            HwFrameworkFactory.getHwFrameworkMonitor();
            Bundle data = new Bundle();
            data.putString(HwFrameworkMonitor.FILE_NAME, fileName);
            data.putString(HwFrameworkMonitor.EXCEPTION_NAME, e.getClass().toString());
            HwFrameworkMonitor hwFwkMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
            if (hwFwkMonitor == null || !hwFwkMonitor.monitor(HwFrameworkMonitor.SCENE_PMS_PARSE_FILE_EXCEPTION, data)) {
                Slog.i(TAG, "upload bigdata fail for cached file: " + fileName);
            } else {
                Slog.i(TAG, "upload bigdata success for cached file: " + fileName);
            }
            cacheFile.delete();
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0050, code lost:
        throw r6;
     */
    private void cacheResult(File packageFile, int flags, Package parsed) {
        if (this.mCacheDir != null) {
            try {
                File cacheFile = new File(this.mCacheDir, getCacheKey(packageFile, flags));
                if (cacheFile.exists() && !cacheFile.delete()) {
                    Slog.e(TAG, "Unable to delete cache file: " + cacheFile);
                }
                byte[] cacheEntry = toCacheEntry(parsed);
                if (cacheEntry != null) {
                    try {
                        FileOutputStream fos = new FileOutputStream(cacheFile);
                        fos.write(cacheEntry);
                        fos.close();
                    } catch (IOException ioe) {
                        Slog.w(TAG, "Error writing cache entry.", ioe);
                        cacheFile.delete();
                    }
                }
            } catch (Throwable e) {
                Slog.w(TAG, "Error saving package cache.", e);
            }
        }
    }

    private Package parseClusterPackage(File packageDir, int flags) throws PackageParserException {
        return parseClusterPackage(packageDir, flags, 0);
    }

    private Package parseClusterPackage(File packageDir, int flags, int hwFlags) throws PackageParserException {
        SplitAssetLoader assetLoader;
        PackageLite lite = parseClusterPackageLite(packageDir, 0);
        if (!this.mOnlyCoreApps || lite.coreApp) {
            SparseArray<int[]> splitDependencies = null;
            if (!lite.isolatedSplits || ArrayUtils.isEmpty(lite.splitNames)) {
                assetLoader = new DefaultSplitAssetLoader(lite, flags);
            } else {
                try {
                    splitDependencies = SplitAssetDependencyLoader.createDependenciesFromPackage(lite);
                    assetLoader = new SplitAssetDependencyLoader(lite, splitDependencies, flags);
                } catch (SplitDependencyLoader.IllegalDependencyException e) {
                    throw new PackageParserException(-101, e.getMessage());
                }
            }
            try {
                AssetManager assets = assetLoader.getBaseAssetManager();
                File baseApk = new File(lite.baseCodePath);
                Package pkg = parseBaseApk(baseApk, assets, flags, hwFlags);
                if (pkg != null) {
                    if (!ArrayUtils.isEmpty(lite.splitNames)) {
                        int num = lite.splitNames.length;
                        pkg.splitNames = lite.splitNames;
                        pkg.splitCodePaths = lite.splitCodePaths;
                        pkg.splitVersionCodes = lite.splitVersionCodes;
                        pkg.splitRevisionCodes = lite.splitRevisionCodes;
                        pkg.splitFlags = new int[num];
                        pkg.splitPrivateFlags = lite.splitPrivateFlags;
                        pkg.applicationInfo.hwSplitFlags = pkg.splitPrivateFlags;
                        pkg.applicationInfo.splitVersionCodes = pkg.splitVersionCodes;
                        pkg.applicationInfo.splitNames = pkg.splitNames;
                        pkg.applicationInfo.splitDependencies = splitDependencies;
                        pkg.applicationInfo.splitClassLoaderNames = new String[num];
                        pkg.hasPlugin = lite.hasPlugin;
                        if (pkg.hasPlugin) {
                            pkg.applicationInfo.hw_extra_flags |= 1;
                        }
                        for (int i = 0; i < num; i++) {
                            parseSplitApk(pkg, i, assetLoader.getSplitAssetManager(i), flags);
                        }
                    }
                    pkg.setCodePath(packageDir.getCanonicalPath());
                    pkg.setUse32bitAbi(lite.use32bitAbi);
                    IoUtils.closeQuietly(assetLoader);
                    return pkg;
                }
                throw new PackageParserException(-100, "Failed to parse base APK: " + baseApk);
            } catch (IOException e2) {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to get path: " + lite.baseCodePath, e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(assetLoader);
                throw th;
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + packageDir);
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        return parseMonolithicPackage(apkFile, flags, 0);
    }

    public Package parseMonolithicPackage(File apkFile, int flags, int hwFlags) throws PackageParserException {
        PackageLite lite = parseMonolithicPackageLite(apkFile, flags);
        if (this.mOnlyCoreApps) {
            if (!lite.coreApp) {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + apkFile);
            } else if (HwFrameworkFactory.getHwPackageParser().needStopApp(lite.packageName, apkFile)) {
                throw new PackageParserException(-2, "need to stop install app: " + lite.packageName + " in " + apkFile);
            }
        }
        SplitAssetLoader assetLoader = new DefaultSplitAssetLoader(lite, flags);
        try {
            Package pkg = parseBaseApk(apkFile, assetLoader.getBaseAssetManager(), flags, hwFlags);
            if (!HwFrameworkFactory.getHwPackageParser().needStopApp(pkg.packageName, apkFile)) {
                pkg.setCodePath(apkFile.getCanonicalPath());
                pkg.setUse32bitAbi(lite.use32bitAbi);
                IoUtils.closeQuietly(assetLoader);
                return pkg;
            }
            throw new PackageParserException(-2, "need to stop install app: " + pkg.packageName + " in " + apkFile);
        } catch (IOException e) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to get path: " + apkFile, e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(assetLoader);
            throw th;
        }
    }

    private Package parseBaseApk(File apkFile, AssetManager assets, int flags) throws PackageParserException {
        return parseBaseApk(apkFile, assets, flags, 0);
    }

    private Package parseBaseApk(File apkFile, AssetManager assets, int flags, int hwFlags) throws PackageParserException {
        String volumeUuid;
        XmlResourceParser parser;
        String apkPath = apkFile.getAbsolutePath();
        if (apkPath.startsWith(MNT_EXPAND)) {
            volumeUuid = apkPath.substring(MNT_EXPAND.length(), apkPath.indexOf(47, MNT_EXPAND.length()));
        } else {
            volumeUuid = null;
        }
        this.mParseError = 1;
        this.mArchiveSourcePath = apkFile.getAbsolutePath();
        XmlResourceParser parser2 = null;
        try {
            int cookie = assets.findCookieForPath(apkPath);
            if (cookie != 0) {
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                String[] outError = new String[1];
                Package pkg = parseBaseApk(apkPath, new Resources(assets, this.mMetrics, null), parser, flags, outError, hwFlags);
                if (pkg != null) {
                    pkg.setVolumeUuid(volumeUuid);
                    pkg.setApplicationVolumeUuid(volumeUuid);
                    pkg.setBaseCodePath(apkPath);
                    pkg.setSigningDetails(SigningDetails.UNKNOWN);
                    IoUtils.closeQuietly(parser);
                    return pkg;
                }
                throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
            }
            throw new PackageParserException(-101, "Failed adding asset path: " + apkPath);
        } catch (PackageParserException e) {
            throw e;
        } catch (Exception e2) {
            e = e2;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (PackageParserException e3) {
            throw e3;
        } catch (Exception e4) {
            e = e4;
            parser2 = parser;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (Throwable th) {
            e = th;
            parser = parser2;
            IoUtils.closeQuietly(parser);
            throw e;
        }
    }

    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags) throws PackageParserException {
        XmlResourceParser parser;
        String apkPath = pkg.splitCodePaths[splitIndex];
        this.mParseError = 1;
        this.mArchiveSourcePath = apkPath;
        XmlResourceParser parser2 = null;
        try {
            int cookie = assets.findCookieForPath(apkPath);
            if (cookie != 0) {
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                try {
                    String[] outError = new String[1];
                    if (parseSplitApk(pkg, new Resources(assets, this.mMetrics, null), parser, flags, splitIndex, outError) != null) {
                        IoUtils.closeQuietly(parser);
                        return;
                    }
                    int i = this.mParseError;
                    throw new PackageParserException(i, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
                } catch (PackageParserException e) {
                    throw e;
                } catch (Exception e2) {
                    e = e2;
                    parser2 = parser;
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
                } catch (Throwable th) {
                    e = th;
                    parser2 = parser;
                    IoUtils.closeQuietly(parser2);
                    throw e;
                }
            } else {
                throw new PackageParserException(-101, "Failed adding asset path: " + apkPath);
            }
        } catch (PackageParserException e3) {
            throw e3;
        } catch (Exception e4) {
            e = e4;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (PackageParserException e5) {
            throw e5;
        } catch (Exception e6) {
            e = e6;
            parser2 = parser;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (Throwable th2) {
            e = th2;
            IoUtils.closeQuietly(parser2);
            throw e;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0079  */
    private Package parseSplitApk(Package pkg, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException, PackageParserException {
        parsePackageSplitNames(parser, parser);
        this.mParseInstrumentationArgs = null;
        boolean foundApp = false;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!foundApp) {
                    outError[0] = "<manifest> does not contain an <application>";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
                }
            } else if (!(type == 3 || type == 4)) {
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
        if (!foundApp) {
        }
        return pkg;
    }

    public static ArraySet<PublicKey> toSigningKeys(Signature[] signatures) throws CertificateException {
        ArraySet<PublicKey> keys = new ArraySet<>(signatures.length);
        for (Signature signature : signatures) {
            keys.add(signature.getPublicKey());
        }
        return keys;
    }

    @UnsupportedAppUsage
    public static void collectCertificates(Package pkg, boolean skipVerify) throws PackageParserException {
        collectCertificatesInternal(pkg, skipVerify);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            pkg.childPackages.get(i).mSigningDetails = pkg.mSigningDetails;
        }
    }

    private static void collectCertificatesInternal(Package pkg, boolean skipVerify) throws PackageParserException {
        pkg.mSigningDetails = SigningDetails.UNKNOWN;
        Trace.traceBegin(262144, "collectCertificates");
        try {
            collectCertificates(pkg, new File(pkg.baseCodePath), skipVerify);
            if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
                for (int i = 0; i < pkg.splitCodePaths.length; i++) {
                    collectCertificates(pkg, new File(pkg.splitCodePaths[i]), skipVerify);
                }
            }
        } finally {
            Trace.traceEnd(262144);
        }
    }

    @UnsupportedAppUsage
    private static void collectCertificates(Package pkg, File apkFile, boolean skipVerify) throws PackageParserException {
        SigningDetails verified;
        String apkPath = apkFile.getAbsolutePath();
        int minSignatureScheme = 1;
        if (pkg.applicationInfo.isStaticSharedLibrary()) {
            minSignatureScheme = 2;
        }
        if (skipVerify) {
            verified = ApkSignatureVerifier.unsafeGetCertsWithoutVerification(apkPath, minSignatureScheme);
        } else {
            verified = ApkSignatureVerifier.verify(apkPath, minSignatureScheme);
        }
        if (pkg.mSigningDetails == SigningDetails.UNKNOWN) {
            pkg.mSigningDetails = verified;
        } else if (!Signature.areExactMatch(pkg.mSigningDetails.signatures, verified.signatures)) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, apkPath + " has mismatched certificates");
        }
    }

    private static AssetManager newConfiguredAssetManager() {
        AssetManager assetManager = new AssetManager();
        assetManager.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
        return assetManager;
    }

    public static ApkLite parseApkLite(File apkFile, int flags) throws PackageParserException {
        return parseApkLiteInner(apkFile, null, null, flags);
    }

    public static ApkLite parseApkLite(FileDescriptor fd, String debugPathName, int flags) throws PackageParserException {
        return parseApkLiteInner(null, fd, debugPathName, flags);
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        android.util.Slog.w(android.content.pm.PackageParser.TAG, "Failed to parse " + r0, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a1, code lost:
        throw new android.content.pm.PackageParser.PackageParserException(android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + r0, r4);
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005d A[ExcHandler: RuntimeException | XmlPullParserException (r4v1 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:9:0x001c] */
    private static ApkLite parseApkLiteInner(File apkFile, FileDescriptor fd, String debugPathName, int flags) throws PackageParserException {
        ApkAssets apkAssets;
        SigningDetails signingDetails;
        String apkPath = fd != null ? debugPathName : apkFile.getAbsolutePath();
        boolean skipVerify = false;
        if (fd != null) {
            try {
                apkAssets = ApkAssets.loadFromFd(fd, debugPathName, false, false);
            } catch (IOException e) {
                throw new PackageParserException(-100, "Failed to parse " + apkPath);
            }
        } else {
            apkAssets = ApkAssets.loadFromPath(apkPath);
        }
        try {
            XmlResourceParser parser = apkAssets.openXml(ANDROID_MANIFEST_FILENAME);
            if ((flags & 32) != 0) {
                Package tempPkg = new Package((String) null);
                if ((flags & 16) != 0) {
                    skipVerify = true;
                }
                Trace.traceBegin(262144, "collectCertificates");
                try {
                    collectCertificates(tempPkg, apkFile, skipVerify);
                    Trace.traceEnd(262144);
                    signingDetails = tempPkg.mSigningDetails;
                } catch (Throwable th) {
                    Trace.traceEnd(262144);
                    throw th;
                }
            } else {
                signingDetails = SigningDetails.UNKNOWN;
            }
            ApkLite parseApkLite = parseApkLite(apkPath, parser, parser, signingDetails);
            IoUtils.closeQuietly(parser);
            try {
                apkAssets.close();
            } catch (Throwable th2) {
            }
            return parseApkLite;
        } catch (RuntimeException | XmlPullParserException e2) {
        } catch (Throwable th3) {
        }
        throw th;
    }

    private static String validateName(String name, boolean requireSeparator, boolean requireFilename) {
        int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        for (int i = 0; i < N; i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
            } else if (front || ((c < '0' || c > '9') && c != '_')) {
                if (c == '.') {
                    hasSep = true;
                    front = true;
                } else {
                    return "bad character '" + c + "'";
                }
            }
        }
        if (requireFilename && !FileUtils.isValidExtFilename(name)) {
            return "Invalid filename";
        }
        if (hasSep || !requireSeparator) {
            return null;
        }
        return "must have at least one '.' separator";
    }

    private static Pair<String, String> parsePackageSplitNames(XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException, PackageParserException {
        int type;
        String error;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No start tag found");
        } else if (parser.getName().equals(TAG_MANIFEST)) {
            String packageName = attrs.getAttributeValue(null, "package");
            if ("android".equals(packageName) || "androidhwext".equals(packageName) || (error = validateName(packageName, true, true)) == null) {
                String splitName = attrs.getAttributeValue(null, "split");
                if (splitName != null) {
                    if (splitName.length() == 0) {
                        splitName = null;
                    } else {
                        String error2 = validateName(splitName, false, false);
                        if (error2 != null) {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest split: " + error2);
                        }
                    }
                }
                return Pair.create(packageName.intern(), splitName != null ? splitName.intern() : splitName);
            }
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest package: " + error);
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No <manifest> tag");
        }
    }

    private static ApkLite parseApkLite(String codePath, XmlPullParser parser, AttributeSet attrs, SigningDetails signingDetails) throws IOException, XmlPullParserException, PackageParserException {
        int searchDepth;
        Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs);
        int targetSdkVersion = 0;
        int minSdkVersion = 1;
        String configForSplit = null;
        String usesSplitName = null;
        boolean isSplitRequired = false;
        boolean isPlugin = false;
        boolean isFeatureSplit = false;
        boolean isolatedSplits = false;
        boolean coreApp = false;
        int revisionCode = 0;
        int versionCodeMajor = 0;
        int versionCode = 0;
        int installLocation = -1;
        int i = 0;
        while (i < attrs.getAttributeCount()) {
            String attr = attrs.getAttributeName(i);
            if (attr.equals("installLocation")) {
                installLocation = attrs.getAttributeIntValue(i, -1);
            } else if (attr.equals(HwFrameworkMonitor.KEY_VERSION_CODE)) {
                versionCode = attrs.getAttributeIntValue(i, 0);
            } else if (attr.equals("versionCodeMajor")) {
                versionCodeMajor = attrs.getAttributeIntValue(i, 0);
            } else if (attr.equals("revisionCode")) {
                revisionCode = attrs.getAttributeIntValue(i, 0);
            } else if (attr.equals("coreApp")) {
                coreApp = attrs.getAttributeBooleanValue(i, false);
            } else if (attr.equals("isolatedSplits")) {
                isolatedSplits = attrs.getAttributeBooleanValue(i, false);
            } else if (attr.equals("configForSplit")) {
                configForSplit = attrs.getAttributeValue(i);
            } else if (attr.equals("isFeatureSplit")) {
                isFeatureSplit = attrs.getAttributeBooleanValue(i, false);
            } else if (attr.equals("isSplitRequired")) {
                isSplitRequired = attrs.getAttributeBooleanValue(i, false);
            } else if (attr.equals("isPlugin") && packageSplit.second != null) {
                isPlugin = attrs.getAttributeBooleanValue(i, false);
            }
            i++;
            targetSdkVersion = targetSdkVersion;
            minSdkVersion = minSdkVersion;
        }
        int type = 1;
        int i2 = parser.getDepth() + 1;
        List<VerifierInfo> verifiers = new ArrayList<>();
        boolean extractNativeLibs = true;
        boolean useEmbeddedDex = false;
        boolean multiArch = false;
        boolean use32bitAbi = false;
        int minSdkVersion2 = minSdkVersion;
        boolean debuggable = false;
        int targetSdkVersion2 = targetSdkVersion;
        while (true) {
            int type2 = parser.next();
            if (type2 == type) {
                break;
            }
            int type3 = type2;
            if (type3 == 3 && parser.getDepth() < i2) {
                break;
            }
            if (type3 == 3) {
                searchDepth = i2;
            } else if (type3 == 4) {
                searchDepth = i2;
            } else if (parser.getDepth() != i2) {
                searchDepth = i2;
            } else {
                searchDepth = i2;
                if (TAG_PACKAGE_VERIFIER.equals(parser.getName())) {
                    VerifierInfo verifier = parseVerifier(attrs);
                    if (verifier != null) {
                        verifiers.add(verifier);
                    }
                } else if (TAG_APPLICATION.equals(parser.getName())) {
                    int i3 = 0;
                    while (i3 < attrs.getAttributeCount()) {
                        String attr2 = attrs.getAttributeName(i3);
                        if ("debuggable".equals(attr2)) {
                            debuggable = attrs.getAttributeBooleanValue(i3, false);
                        }
                        if ("multiArch".equals(attr2)) {
                            multiArch = attrs.getAttributeBooleanValue(i3, false);
                        }
                        if ("use32bitAbi".equals(attr2)) {
                            use32bitAbi = attrs.getAttributeBooleanValue(i3, false);
                        }
                        if ("extractNativeLibs".equals(attr2)) {
                            extractNativeLibs = attrs.getAttributeBooleanValue(i3, true);
                        }
                        if ("useEmbeddedDex".equals(attr2)) {
                            useEmbeddedDex = attrs.getAttributeBooleanValue(i3, false);
                        }
                        i3++;
                        type3 = type3;
                    }
                    targetSdkVersion2 = targetSdkVersion2;
                    i2 = searchDepth;
                    type = 1;
                } else if (TAG_USES_SPLIT.equals(parser.getName())) {
                    if (usesSplitName != null) {
                        Slog.w(TAG, "Only one <uses-split> permitted. Ignoring others.");
                    } else {
                        usesSplitName = attrs.getAttributeValue(ANDROID_RESOURCES, "name");
                        if (usesSplitName != null) {
                            targetSdkVersion2 = targetSdkVersion2;
                            i2 = searchDepth;
                            type = 1;
                        } else {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "<uses-split> tag requires 'android:name' attribute");
                        }
                    }
                } else if (TAG_USES_SDK.equals(parser.getName())) {
                    int i4 = 0;
                    int targetSdkVersion3 = targetSdkVersion2;
                    while (i4 < attrs.getAttributeCount()) {
                        String attr3 = attrs.getAttributeName(i4);
                        int targetSdkVersion4 = targetSdkVersion3;
                        if ("targetSdkVersion".equals(attr3)) {
                            targetSdkVersion4 = attrs.getAttributeIntValue(i4, 0);
                        }
                        if ("minSdkVersion".equals(attr3)) {
                            minSdkVersion2 = attrs.getAttributeIntValue(i4, 1);
                        }
                        i4++;
                        targetSdkVersion3 = targetSdkVersion4;
                    }
                    type = 1;
                    targetSdkVersion2 = targetSdkVersion3;
                    i2 = searchDepth;
                }
            }
            type = 1;
            targetSdkVersion2 = targetSdkVersion2;
            i2 = searchDepth;
        }
        return new ApkLite(codePath, packageSplit.first, packageSplit.second, isFeatureSplit, configForSplit, usesSplitName, isSplitRequired, versionCode, versionCodeMajor, revisionCode, installLocation, verifiers, signingDetails, coreApp, debuggable, multiArch, use32bitAbi, useEmbeddedDex, extractNativeLibs, isolatedSplits, minSdkVersion2, targetSdkVersion2, isPlugin);
    }

    private boolean parseBaseApkChild(Package parentPkg, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        String childPackageName = parser.getAttributeValue(null, "package");
        if (validateName(childPackageName, true, false) != null) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return false;
        } else if (childPackageName.equals(parentPkg.packageName)) {
            String message = "Child package name cannot be equal to parent package name: " + parentPkg.packageName;
            Slog.w(TAG, message);
            outError[0] = message;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        } else if (parentPkg.hasChildPackage(childPackageName)) {
            String message2 = "Duplicate child package:" + childPackageName;
            Slog.w(TAG, message2);
            outError[0] = message2;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        } else {
            Package childPkg = new Package(childPackageName);
            childPkg.mVersionCode = parentPkg.mVersionCode;
            childPkg.baseRevisionCode = parentPkg.baseRevisionCode;
            childPkg.mVersionName = parentPkg.mVersionName;
            childPkg.applicationInfo.targetSdkVersion = parentPkg.applicationInfo.targetSdkVersion;
            childPkg.applicationInfo.minSdkVersion = parentPkg.applicationInfo.minSdkVersion;
            Package childPkg2 = parseBaseApkCommon(childPkg, CHILD_PACKAGE_TAGS, res, parser, flags, outError);
            if (childPkg2 == null) {
                return false;
            }
            if (parentPkg.childPackages == null) {
                parentPkg.childPackages = new ArrayList<>();
            }
            parentPkg.childPackages.add(childPkg2);
            childPkg2.parentPackage = parentPkg;
            return true;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private Package parseBaseApk(String apkPath, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        return parseBaseApk(apkPath, res, parser, flags, outError, 0);
    }

    private Package parseBaseApk(String apkPath, Resources res, XmlResourceParser parser, int flags, String[] outError, int hwFlags) throws XmlPullParserException, IOException {
        try {
            Pair<String, String> packageSplit = parsePackageSplitNames(parser, parser);
            String pkgName = packageSplit.first;
            String splitName = packageSplit.second;
            if (!TextUtils.isEmpty(splitName)) {
                outError[0] = "Expected base APK, but found split " + splitName;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
                return null;
            }
            Callback callback = this.mCallback;
            if (callback != null) {
                String[] overlayPaths = callback.getOverlayPaths(pkgName, apkPath);
                if (overlayPaths != null && overlayPaths.length > 0) {
                    for (String overlayPath : overlayPaths) {
                        res.getAssets().addOverlayPath(overlayPath);
                    }
                }
            }
            Package pkg = new Package(pkgName);
            pkg.applicationInfo.hwFlags = hwFlags;
            TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifest);
            pkg.mVersionCode = sa.getInteger(1, 0);
            pkg.mVersionCodeMajor = sa.getInteger(11, 0);
            pkg.applicationInfo.setVersionCode(pkg.getLongVersionCode());
            pkg.baseRevisionCode = sa.getInteger(5, 0);
            pkg.mVersionName = sa.getNonConfigurationString(2, 0);
            if (pkg.mVersionName != null) {
                pkg.mVersionName = pkg.mVersionName.intern();
            }
            pkg.coreApp = parser.getAttributeBooleanValue(null, "coreApp", false);
            pkg.mCompileSdkVersion = sa.getInteger(9, 0);
            pkg.applicationInfo.compileSdkVersion = pkg.mCompileSdkVersion;
            pkg.mCompileSdkVersionCodename = sa.getNonConfigurationString(10, 0);
            if (pkg.mCompileSdkVersionCodename != null) {
                pkg.mCompileSdkVersionCodename = pkg.mCompileSdkVersionCodename.intern();
            }
            pkg.applicationInfo.compileSdkVersionCodename = pkg.mCompileSdkVersionCodename;
            sa.recycle();
            return parseBaseApkCommon(pkg, null, res, parser, flags, outError);
        } catch (PackageParserException e) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }
    }

    /* JADX INFO: Multiple debug info for r23v13 'supportsXLargeScreens'  int: [D('resizeable' int), D('supportsXLargeScreens' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0302, code lost:
        return null;
     */
    private Package parseBaseApkCommon(Package pkg, Set<String> acceptedTags, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        int supportsNormalScreens;
        int supportsXLargeScreens;
        int supportsXLargeScreens2;
        int anyDensity;
        int type;
        int anyDensity2;
        String str;
        int targetSandboxVersion;
        int i;
        int anyDensity3;
        int resizeable;
        int supportsXLargeScreens3;
        int supportsXLargeScreens4;
        int supportsLargeScreens;
        int outerDepth;
        int anyDensity4;
        int supportsNormalScreens2;
        int supportsLargeScreens2;
        int resizeable2;
        int anyDensity5;
        int innerDepth;
        Set<String> set = acceptedTags;
        this.mParseInstrumentationArgs = null;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifest);
        String str2 = sa.getNonConfigurationString(0, 0);
        int i2 = 1;
        if (str2 != null && str2.length() > 0) {
            String nameError = validateName(str2, true, true);
            if (nameError == null || "android".equals(pkg.packageName) || "androidhwext".equals(pkg.packageName)) {
                pkg.mSharedUserId = str2.intern();
                pkg.mSharedUserLabel = sa.getResourceId(3, 0);
            } else {
                outError[0] = "<manifest> specifies bad sharedUserId name \"" + str2 + "\": " + nameError;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                return null;
            }
        }
        pkg.installLocation = sa.getInteger(4, -1);
        pkg.applicationInfo.installLocation = pkg.installLocation;
        int targetSandboxVersion2 = sa.getInteger(7, 1);
        pkg.applicationInfo.targetSandboxVersion = targetSandboxVersion2;
        if ((flags & 8) != 0) {
            pkg.applicationInfo.flags |= 262144;
        }
        if (sa.getBoolean(6, false)) {
            pkg.applicationInfo.privateFlags |= 32768;
        }
        int supportsSmallScreens = 1;
        int outerDepth2 = parser.getDepth();
        int supportsXLargeScreens5 = 1;
        int resizeable3 = 1;
        int anyDensity6 = 1;
        boolean foundApp = false;
        int supportsLargeScreens3 = 1;
        int supportsNormalScreens3 = 1;
        while (true) {
            int type2 = parser.next();
            if (type2 == i2) {
                supportsNormalScreens = supportsNormalScreens3;
                supportsXLargeScreens = supportsXLargeScreens5;
                supportsXLargeScreens2 = resizeable3;
                anyDensity = anyDensity6;
                type = supportsLargeScreens3;
                anyDensity2 = supportsSmallScreens;
                break;
            }
            if (type2 == 3 && parser.getDepth() <= outerDepth2) {
                type = supportsLargeScreens3;
                supportsNormalScreens = supportsNormalScreens3;
                supportsXLargeScreens = supportsXLargeScreens5;
                supportsXLargeScreens2 = resizeable3;
                anyDensity = anyDensity6;
                anyDensity2 = supportsSmallScreens;
                break;
            }
            if (type2 == 3) {
                supportsLargeScreens = supportsLargeScreens3;
                supportsNormalScreens2 = supportsNormalScreens3;
                targetSandboxVersion = targetSandboxVersion2;
                outerDepth = outerDepth2;
                str = str2;
                supportsXLargeScreens4 = supportsXLargeScreens5;
                supportsXLargeScreens3 = resizeable3;
                anyDensity4 = anyDensity6;
                i = 1;
                anyDensity3 = supportsSmallScreens;
                resizeable = 3;
            } else if (type2 == 4) {
                supportsLargeScreens = supportsLargeScreens3;
                supportsNormalScreens2 = supportsNormalScreens3;
                targetSandboxVersion = targetSandboxVersion2;
                outerDepth = outerDepth2;
                str = str2;
                supportsXLargeScreens4 = supportsXLargeScreens5;
                supportsXLargeScreens3 = resizeable3;
                anyDensity4 = anyDensity6;
                resizeable = 3;
                i = 1;
                anyDensity3 = supportsSmallScreens;
            } else {
                outerDepth = outerDepth2;
                String tagName = parser.getName();
                if (set == null || set.contains(tagName)) {
                    if (!tagName.equals(TAG_APPLICATION)) {
                        supportsLargeScreens2 = supportsLargeScreens3;
                        supportsNormalScreens2 = supportsNormalScreens3;
                        targetSandboxVersion = targetSandboxVersion2;
                        str = str2;
                        if (tagName.equals("overlay")) {
                            TypedArray sa2 = res.obtainAttributes(parser, R.styleable.AndroidManifestResourceOverlay);
                            pkg.mOverlayTarget = sa2.getString(1);
                            pkg.mOverlayTargetName = sa2.getString(3);
                            pkg.mOverlayCategory = sa2.getString(2);
                            pkg.mOverlayPriority = sa2.getInt(0, 0);
                            pkg.mOverlayIsStatic = sa2.getBoolean(4, false);
                            String propName = sa2.getString(5);
                            String propValue = sa2.getString(6);
                            sa2.recycle();
                            if (pkg.mOverlayTarget == null) {
                                outError[0] = "<overlay> does not specify a target package";
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return null;
                            } else if (pkg.mOverlayPriority < 0 || pkg.mOverlayPriority > 9999) {
                                outError[0] = "<overlay> priority must be between 0 and 9999";
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            } else if (!checkOverlayRequiredSystemProperty(propName, propValue)) {
                                Slog.i(TAG, "Skipping target and overlay pair " + pkg.mOverlayTarget + " and " + pkg.baseCodePath + ": overlay ignored due to required system property: " + propName + " with value: " + propValue);
                                return null;
                            } else {
                                pkg.applicationInfo.privateFlags |= 268435456;
                                XmlUtils.skipCurrentTag(parser);
                                i = 1;
                                supportsNormalScreens3 = supportsNormalScreens2;
                                resizeable2 = resizeable3;
                                supportsSmallScreens = supportsSmallScreens;
                                resizeable = 3;
                            }
                        } else {
                            if (tagName.equals(TAG_KEY_SETS)) {
                                if (!parseKeySets(pkg, res, parser, outError)) {
                                    return null;
                                }
                                i = 1;
                                supportsXLargeScreens4 = supportsXLargeScreens5;
                                supportsXLargeScreens3 = resizeable3;
                                anyDensity4 = anyDensity6;
                                anyDensity5 = supportsSmallScreens;
                                supportsLargeScreens = supportsLargeScreens2;
                                resizeable = 3;
                            } else if (!tagName.equals(TAG_PERMISSION_GROUP)) {
                                int i3 = 1;
                                if (tagName.equals("permission")) {
                                    if (!parsePermission(pkg, res, parser, outError)) {
                                        return null;
                                    }
                                    i = 1;
                                    supportsXLargeScreens4 = supportsXLargeScreens5;
                                    supportsXLargeScreens3 = resizeable3;
                                    anyDensity4 = anyDensity6;
                                    anyDensity5 = supportsSmallScreens;
                                    supportsLargeScreens = supportsLargeScreens2;
                                    resizeable = 3;
                                } else if (tagName.equals(TAG_PERMISSION_TREE)) {
                                    if (!parsePermissionTree(pkg, res, parser, outError)) {
                                        return null;
                                    }
                                    i = 1;
                                    supportsXLargeScreens4 = supportsXLargeScreens5;
                                    supportsXLargeScreens3 = resizeable3;
                                    anyDensity4 = anyDensity6;
                                    anyDensity5 = supportsSmallScreens;
                                    supportsLargeScreens = supportsLargeScreens2;
                                    resizeable = 3;
                                } else if (!tagName.equals(TAG_USES_PERMISSION)) {
                                    if (tagName.equals(TAG_USES_PERMISSION_SDK_M)) {
                                        i = 1;
                                        supportsXLargeScreens4 = supportsXLargeScreens5;
                                        supportsXLargeScreens3 = resizeable3;
                                        anyDensity4 = anyDensity6;
                                        anyDensity5 = supportsSmallScreens;
                                        supportsLargeScreens = supportsLargeScreens2;
                                        resizeable = 3;
                                    } else if (tagName.equals(TAG_USES_PERMISSION_SDK_23)) {
                                        i = 1;
                                        supportsXLargeScreens4 = supportsXLargeScreens5;
                                        supportsXLargeScreens3 = resizeable3;
                                        anyDensity4 = anyDensity6;
                                        anyDensity5 = supportsSmallScreens;
                                        supportsLargeScreens = supportsLargeScreens2;
                                        resizeable = 3;
                                    } else if (tagName.equals(TAG_USES_CONFIGURATION)) {
                                        ConfigurationInfo cPref = new ConfigurationInfo();
                                        TypedArray sa3 = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesConfiguration);
                                        cPref.reqTouchScreen = sa3.getInt(0, 0);
                                        cPref.reqKeyboardType = sa3.getInt(1, 0);
                                        if (sa3.getBoolean(2, false)) {
                                            cPref.reqInputFeatures |= 1;
                                        }
                                        cPref.reqNavigation = sa3.getInt(3, 0);
                                        if (sa3.getBoolean(4, false)) {
                                            cPref.reqInputFeatures = 2 | cPref.reqInputFeatures;
                                        }
                                        sa3.recycle();
                                        pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);
                                        XmlUtils.skipCurrentTag(parser);
                                        i = 1;
                                        supportsNormalScreens3 = supportsNormalScreens2;
                                        resizeable2 = resizeable3;
                                        supportsSmallScreens = supportsSmallScreens;
                                        resizeable = 3;
                                    } else if (tagName.equals(TAG_USES_FEATURE)) {
                                        FeatureInfo fi = parseUsesFeature(res, parser);
                                        pkg.reqFeatures = ArrayUtils.add(pkg.reqFeatures, fi);
                                        if (fi.name == null) {
                                            ConfigurationInfo cPref2 = new ConfigurationInfo();
                                            cPref2.reqGlEsVersion = fi.reqGlEsVersion;
                                            pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref2);
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                        i = 1;
                                        supportsXLargeScreens4 = supportsXLargeScreens5;
                                        supportsXLargeScreens3 = resizeable3;
                                        anyDensity4 = anyDensity6;
                                        anyDensity5 = supportsSmallScreens;
                                        supportsLargeScreens = supportsLargeScreens2;
                                        resizeable = 3;
                                    } else if (tagName.equals(TAG_FEATURE_GROUP)) {
                                        FeatureGroupInfo group = new FeatureGroupInfo();
                                        ArrayList<FeatureInfo> features = null;
                                        int innerDepth2 = parser.getDepth();
                                        while (true) {
                                            int type3 = parser.next();
                                            if (type3 == i3) {
                                                break;
                                            } else if (type3 == 3 && parser.getDepth() <= innerDepth2) {
                                                break;
                                            } else if (type3 == 3 || type3 == 4) {
                                                innerDepth2 = innerDepth2;
                                                i3 = 1;
                                            } else {
                                                String innerTagName = parser.getName();
                                                if (innerTagName.equals(TAG_USES_FEATURE)) {
                                                    FeatureInfo featureInfo = parseUsesFeature(res, parser);
                                                    innerDepth = innerDepth2;
                                                    featureInfo.flags |= 1;
                                                    features = ArrayUtils.add(features, featureInfo);
                                                } else {
                                                    innerDepth = innerDepth2;
                                                    Slog.w(TAG, "Unknown element under <feature-group>: " + innerTagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                }
                                                XmlUtils.skipCurrentTag(parser);
                                                innerDepth2 = innerDepth;
                                                i3 = 1;
                                            }
                                        }
                                        if (features != null) {
                                            group.features = new FeatureInfo[features.size()];
                                            group.features = (FeatureInfo[]) features.toArray(group.features);
                                        }
                                        pkg.featureGroups = ArrayUtils.add(pkg.featureGroups, group);
                                        supportsNormalScreens3 = supportsNormalScreens2;
                                        resizeable2 = resizeable3;
                                        supportsSmallScreens = supportsSmallScreens;
                                        resizeable = 3;
                                        i = 1;
                                    } else if (tagName.equals(TAG_USES_SDK)) {
                                        if (SDK_VERSION > 0) {
                                            TypedArray sa4 = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesSdk);
                                            int minVers = 1;
                                            String minCode = null;
                                            int targetVers = 0;
                                            String targetCode = null;
                                            TypedValue val = sa4.peekValue(0);
                                            if (val != null) {
                                                if (val.type != 3 || val.string == null) {
                                                    minVers = val.data;
                                                } else {
                                                    minCode = val.string.toString();
                                                }
                                            }
                                            TypedValue val2 = sa4.peekValue(1);
                                            if (val2 == null) {
                                                targetVers = minVers;
                                                targetCode = minCode;
                                            } else if (val2.type != 3 || val2.string == null) {
                                                targetVers = val2.data;
                                            } else {
                                                targetCode = val2.string.toString();
                                                if (minCode == null) {
                                                    minCode = targetCode;
                                                }
                                            }
                                            sa4.recycle();
                                            int minSdkVersion = computeMinSdkVersion(minVers, minCode, SDK_VERSION, SDK_CODENAMES, outError);
                                            if (minSdkVersion < 0) {
                                                this.mParseError = -12;
                                                return null;
                                            }
                                            int targetSdkVersion = computeTargetSdkVersion(targetVers, targetCode, SDK_CODENAMES, outError);
                                            if (targetSdkVersion < 0) {
                                                this.mParseError = -12;
                                                return null;
                                            }
                                            pkg.applicationInfo.minSdkVersion = minSdkVersion;
                                            pkg.applicationInfo.targetSdkVersion = targetSdkVersion;
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                        supportsNormalScreens3 = supportsNormalScreens2;
                                        resizeable2 = resizeable3;
                                        supportsSmallScreens = supportsSmallScreens;
                                        resizeable = 3;
                                        i = 1;
                                    } else if (tagName.equals(TAG_SUPPORT_SCREENS)) {
                                        TypedArray sa5 = res.obtainAttributes(parser, R.styleable.AndroidManifestSupportsScreens);
                                        pkg.applicationInfo.requiresSmallestWidthDp = sa5.getInteger(6, 0);
                                        pkg.applicationInfo.compatibleWidthLimitDp = sa5.getInteger(7, 0);
                                        pkg.applicationInfo.largestWidthLimitDp = sa5.getInteger(8, 0);
                                        int supportsSmallScreens2 = sa5.getInteger(1, supportsSmallScreens);
                                        int supportsNormalScreens4 = sa5.getInteger(2, supportsNormalScreens2);
                                        int supportsLargeScreens4 = sa5.getInteger(3, supportsLargeScreens2);
                                        int supportsXLargeScreens6 = sa5.getInteger(5, supportsXLargeScreens5);
                                        resizeable2 = sa5.getInteger(4, resizeable3);
                                        int anyDensity7 = sa5.getInteger(0, anyDensity6);
                                        sa5.recycle();
                                        XmlUtils.skipCurrentTag(parser);
                                        supportsXLargeScreens5 = supportsXLargeScreens6;
                                        supportsNormalScreens3 = supportsNormalScreens4;
                                        supportsLargeScreens2 = supportsLargeScreens4;
                                        resizeable = 3;
                                        anyDensity6 = anyDensity7;
                                        i = 1;
                                        supportsSmallScreens = supportsSmallScreens2;
                                    } else {
                                        supportsXLargeScreens4 = supportsXLargeScreens5;
                                        supportsXLargeScreens3 = resizeable3;
                                        anyDensity4 = anyDensity6;
                                        supportsLargeScreens = supportsLargeScreens2;
                                        resizeable = 3;
                                        if (tagName.equals(TAG_PROTECTED_BROADCAST)) {
                                            TypedArray sa6 = res.obtainAttributes(parser, R.styleable.AndroidManifestProtectedBroadcast);
                                            String name = sa6.getNonResourceString(0);
                                            sa6.recycle();
                                            if (name != null) {
                                                if (pkg.protectedBroadcasts == null) {
                                                    pkg.protectedBroadcasts = new ArrayList<>();
                                                }
                                                if (!pkg.protectedBroadcasts.contains(name)) {
                                                    pkg.protectedBroadcasts.add(name.intern());
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            supportsSmallScreens = supportsSmallScreens;
                                            supportsNormalScreens3 = supportsNormalScreens2;
                                            anyDensity6 = anyDensity4;
                                            supportsLargeScreens2 = supportsLargeScreens;
                                            resizeable2 = supportsXLargeScreens3;
                                            i = 1;
                                            supportsXLargeScreens5 = supportsXLargeScreens4;
                                        } else if (tagName.equals(TAG_INSTRUMENTATION)) {
                                            if (parseInstrumentation(pkg, res, parser, outError) == null) {
                                                return null;
                                            }
                                            anyDensity5 = supportsSmallScreens;
                                            i = 1;
                                        } else if (tagName.equals(TAG_ORIGINAL_PACKAGE)) {
                                            TypedArray sa7 = res.obtainAttributes(parser, R.styleable.AndroidManifestOriginalPackage);
                                            String orig = sa7.getNonConfigurationString(0, 0);
                                            if (!pkg.packageName.equals(orig)) {
                                                if (pkg.mOriginalPackages == null) {
                                                    pkg.mOriginalPackages = new ArrayList<>();
                                                    pkg.mRealPackage = pkg.packageName;
                                                }
                                                pkg.mOriginalPackages.add(orig);
                                            }
                                            sa7.recycle();
                                            XmlUtils.skipCurrentTag(parser);
                                            supportsSmallScreens = supportsSmallScreens;
                                            supportsNormalScreens3 = supportsNormalScreens2;
                                            anyDensity6 = anyDensity4;
                                            supportsLargeScreens2 = supportsLargeScreens;
                                            resizeable2 = supportsXLargeScreens3;
                                            i = 1;
                                            supportsXLargeScreens5 = supportsXLargeScreens4;
                                        } else if (tagName.equals(TAG_ADOPT_PERMISSIONS)) {
                                            TypedArray sa8 = res.obtainAttributes(parser, R.styleable.AndroidManifestOriginalPackage);
                                            String name2 = sa8.getNonConfigurationString(0, 0);
                                            sa8.recycle();
                                            if (name2 != null) {
                                                if (pkg.mAdoptPermissions == null) {
                                                    pkg.mAdoptPermissions = new ArrayList<>();
                                                }
                                                pkg.mAdoptPermissions.add(name2);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            supportsSmallScreens = supportsSmallScreens;
                                            supportsNormalScreens3 = supportsNormalScreens2;
                                            anyDensity6 = anyDensity4;
                                            supportsLargeScreens2 = supportsLargeScreens;
                                            resizeable2 = supportsXLargeScreens3;
                                            i = 1;
                                            supportsXLargeScreens5 = supportsXLargeScreens4;
                                        } else if (tagName.equals(TAG_USES_GL_TEXTURE)) {
                                            XmlUtils.skipCurrentTag(parser);
                                            anyDensity3 = supportsSmallScreens;
                                            i = 1;
                                        } else if (tagName.equals(TAG_COMPATIBLE_SCREENS)) {
                                            XmlUtils.skipCurrentTag(parser);
                                            anyDensity3 = supportsSmallScreens;
                                            i = 1;
                                        } else if (tagName.equals(TAG_SUPPORTS_INPUT)) {
                                            XmlUtils.skipCurrentTag(parser);
                                            anyDensity3 = supportsSmallScreens;
                                            i = 1;
                                        } else if (tagName.equals(TAG_EAT_COMMENT)) {
                                            XmlUtils.skipCurrentTag(parser);
                                            anyDensity3 = supportsSmallScreens;
                                            i = 1;
                                        } else if (!tagName.equals("package")) {
                                            anyDensity3 = supportsSmallScreens;
                                            i = 1;
                                            if (tagName.equals(TAG_RESTRICT_UPDATE)) {
                                                if ((flags & 16) != 0) {
                                                    TypedArray sa9 = res.obtainAttributes(parser, R.styleable.AndroidManifestRestrictUpdate);
                                                    String hash = sa9.getNonConfigurationString(0, 0);
                                                    sa9.recycle();
                                                    pkg.restrictUpdateHash = null;
                                                    if (hash != null) {
                                                        int hashLength = hash.length();
                                                        byte[] hashBytes = new byte[(hashLength / 2)];
                                                        int i4 = 0;
                                                        while (i4 < hashLength) {
                                                            hashBytes[i4 / 2] = (byte) ((Character.digit(hash.charAt(i4), 16) << 4) + Character.digit(hash.charAt(i4 + 1), 16));
                                                            i4 += 2;
                                                            sa9 = sa9;
                                                            hashLength = hashLength;
                                                        }
                                                        pkg.restrictUpdateHash = hashBytes;
                                                    }
                                                }
                                                XmlUtils.skipCurrentTag(parser);
                                                supportsNormalScreens3 = supportsNormalScreens2;
                                                supportsLargeScreens2 = supportsLargeScreens;
                                                resizeable2 = supportsXLargeScreens3;
                                                supportsSmallScreens = anyDensity3;
                                                anyDensity6 = anyDensity4;
                                                supportsXLargeScreens5 = supportsXLargeScreens4;
                                            } else {
                                                Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                XmlUtils.skipCurrentTag(parser);
                                            }
                                        } else if (!MULTI_PACKAGE_APK_ENABLED) {
                                            XmlUtils.skipCurrentTag(parser);
                                            anyDensity3 = supportsSmallScreens;
                                            i = 1;
                                        } else {
                                            anyDensity5 = supportsSmallScreens;
                                            i = 1;
                                            if (!parseBaseApkChild(pkg, res, parser, flags, outError)) {
                                                return null;
                                            }
                                        }
                                    }
                                    if (!parseUsesPermission(pkg, res, parser)) {
                                        return null;
                                    }
                                } else if (!parseUsesPermission(pkg, res, parser)) {
                                    return null;
                                } else {
                                    i = 1;
                                    supportsXLargeScreens4 = supportsXLargeScreens5;
                                    supportsXLargeScreens3 = resizeable3;
                                    anyDensity4 = anyDensity6;
                                    anyDensity5 = supportsSmallScreens;
                                    supportsLargeScreens = supportsLargeScreens2;
                                    resizeable = 3;
                                }
                            } else if (!parsePermissionGroup(pkg, flags, res, parser, outError)) {
                                return null;
                            } else {
                                i = 1;
                                supportsXLargeScreens4 = supportsXLargeScreens5;
                                supportsXLargeScreens3 = resizeable3;
                                anyDensity4 = anyDensity6;
                                anyDensity5 = supportsSmallScreens;
                                supportsLargeScreens = supportsLargeScreens2;
                                resizeable = 3;
                            }
                            supportsNormalScreens3 = supportsNormalScreens2;
                            supportsLargeScreens2 = supportsLargeScreens;
                            resizeable2 = supportsXLargeScreens3;
                            supportsSmallScreens = anyDensity5;
                            anyDensity6 = anyDensity4;
                            supportsXLargeScreens5 = supportsXLargeScreens4;
                        }
                    } else if (foundApp) {
                        Slog.w(TAG, "<manifest> has more than one <application>");
                        XmlUtils.skipCurrentTag(parser);
                        supportsXLargeScreens4 = supportsXLargeScreens5;
                        supportsXLargeScreens3 = resizeable3;
                        supportsLargeScreens = supportsLargeScreens3;
                        supportsNormalScreens2 = supportsNormalScreens3;
                        targetSandboxVersion = targetSandboxVersion2;
                        resizeable = 3;
                        i = 1;
                        str = str2;
                        anyDensity4 = anyDensity6;
                        anyDensity3 = supportsSmallScreens;
                    } else {
                        foundApp = true;
                        str = str2;
                        targetSandboxVersion = targetSandboxVersion2;
                        supportsLargeScreens2 = supportsLargeScreens3;
                        if (!parseBaseApplication(pkg, res, parser, flags, outError)) {
                            return null;
                        }
                        int i5 = pkg.applicationInfo.minEmuiSdkVersion;
                        int i6 = CURRENT_EMUI_SDK_VERSION;
                        if (i5 <= i6 || i6 == 0) {
                            int i7 = sCurrentEmuiSysImgVersion;
                            if (i7 == 0) {
                                i7 = SystemProperties.getInt("persist.sys.version", 0);
                            }
                            sCurrentEmuiSysImgVersion = i7;
                            if (pkg.applicationInfo.minEmuiSysImgVersion > sCurrentEmuiSysImgVersion) {
                                Slog.e(TAG, pkg.applicationInfo.packageName + " requires min system img version = " + pkg.applicationInfo.minEmuiSysImgVersion + ", current system img version = " + sCurrentEmuiSysImgVersion);
                                this.mParseError = -12;
                                return null;
                            }
                            supportsNormalScreens3 = supportsNormalScreens3;
                            resizeable2 = resizeable3;
                            supportsSmallScreens = supportsSmallScreens;
                            resizeable = 3;
                            i = 1;
                        } else {
                            Slog.e(TAG, "package requires min EMUI sdk level=" + pkg.applicationInfo.minEmuiSdkVersion + ", current EMUI sdk level=" + CURRENT_EMUI_SDK_VERSION);
                            this.mParseError = -12;
                            return null;
                        }
                    }
                    set = acceptedTags;
                    outerDepth2 = outerDepth;
                    i2 = i;
                    targetSandboxVersion2 = targetSandboxVersion;
                    str2 = str;
                    supportsLargeScreens3 = supportsLargeScreens2;
                    resizeable3 = resizeable2;
                } else {
                    Slog.w(TAG, "Skipping unsupported element under <manifest>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                    supportsXLargeScreens4 = supportsXLargeScreens5;
                    supportsXLargeScreens3 = resizeable3;
                    supportsLargeScreens = supportsLargeScreens3;
                    supportsNormalScreens2 = supportsNormalScreens3;
                    targetSandboxVersion = targetSandboxVersion2;
                    resizeable = 3;
                    i = 1;
                    str = str2;
                    anyDensity4 = anyDensity6;
                    anyDensity3 = supportsSmallScreens;
                }
            }
            supportsNormalScreens3 = supportsNormalScreens2;
            outerDepth2 = outerDepth;
            supportsLargeScreens3 = supportsLargeScreens;
            supportsSmallScreens = anyDensity3;
            i2 = i;
            targetSandboxVersion2 = targetSandboxVersion;
            set = acceptedTags;
            anyDensity6 = anyDensity4;
            resizeable3 = supportsXLargeScreens3;
            str2 = str;
            supportsXLargeScreens5 = supportsXLargeScreens4;
        }
        if (!foundApp && pkg.instrumentation.size() == 0) {
            outError[0] = "<manifest> does not contain an <application> or <instrumentation>";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
        }
        int NP = NEW_PERMISSIONS.length;
        StringBuilder newPermsMsg = null;
        for (int ip = 0; ip < NP; ip++) {
            NewPermissionInfo npi = NEW_PERMISSIONS[ip];
            if (pkg.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
                break;
            }
            if (!pkg.requestedPermissions.contains(npi.name)) {
                if (newPermsMsg == null) {
                    newPermsMsg = new StringBuilder(128);
                    newPermsMsg.append(pkg.packageName);
                    newPermsMsg.append(": compat added ");
                } else {
                    newPermsMsg.append(' ');
                }
                newPermsMsg.append(npi.name);
                pkg.requestedPermissions.add(npi.name);
                pkg.implicitPermissions.add(npi.name);
            }
        }
        if (newPermsMsg != null) {
            Slog.i(TAG, newPermsMsg.toString());
        }
        int NS = PermissionManager.SPLIT_PERMISSIONS.size();
        int is = 0;
        while (is < NS) {
            PermissionManager.SplitPermissionInfo spi = PermissionManager.SPLIT_PERMISSIONS.get(is);
            if (pkg.applicationInfo.targetSdkVersion < spi.getTargetSdk() && pkg.requestedPermissions.contains(spi.getSplitPermission())) {
                List<String> newPerms = spi.getNewPermissions();
                int in = 0;
                while (in < newPerms.size()) {
                    String perm = newPerms.get(in);
                    if (!pkg.requestedPermissions.contains(perm)) {
                        pkg.requestedPermissions.add(perm);
                        pkg.implicitPermissions.add(perm);
                    }
                    in++;
                    NP = NP;
                }
            }
            is++;
            NP = NP;
        }
        if (anyDensity2 < 0 || (anyDensity2 > 0 && pkg.applicationInfo.targetSdkVersion >= 4)) {
            pkg.applicationInfo.flags |= 512;
        }
        if (supportsNormalScreens != 0) {
            pkg.applicationInfo.flags |= 1024;
        }
        if (type < 0 || (type > 0 && pkg.applicationInfo.targetSdkVersion >= 4)) {
            pkg.applicationInfo.flags |= 2048;
        }
        if (supportsXLargeScreens < 0 || (supportsXLargeScreens > 0 && pkg.applicationInfo.targetSdkVersion >= 9)) {
            pkg.applicationInfo.flags |= 524288;
        }
        if (supportsXLargeScreens2 < 0 || (supportsXLargeScreens2 > 0 && pkg.applicationInfo.targetSdkVersion >= 4)) {
            pkg.applicationInfo.flags |= 4096;
        }
        if (anyDensity < 0 || (anyDensity > 0 && pkg.applicationInfo.targetSdkVersion >= 4)) {
            pkg.applicationInfo.flags |= 8192;
        }
        if (pkg.applicationInfo.usesCompatibilityMode()) {
            adjustPackageToBeUnresizeableAndUnpipable(pkg);
        }
        return pkg;
    }

    private boolean checkOverlayRequiredSystemProperty(String propName, String propValue) {
        if (!TextUtils.isEmpty(propName) && !TextUtils.isEmpty(propValue)) {
            String currValue = SystemProperties.get(propName);
            return currValue != null && currValue.equals(propValue);
        } else if (TextUtils.isEmpty(propName) && TextUtils.isEmpty(propValue)) {
            return true;
        } else {
            Slog.w(TAG, "Disabling overlay - incomplete property :'" + propName + "=" + propValue + "' - require both requiredSystemPropertyName AND requiredSystemPropertyValue to be specified.");
            return false;
        }
    }

    private void adjustPackageToBeUnresizeableAndUnpipable(Package pkg) {
        Iterator<Activity> it = pkg.activities.iterator();
        while (it.hasNext()) {
            Activity a = it.next();
            a.info.resizeMode = 0;
            a.info.flags &= -4194305;
        }
    }

    private static boolean matchTargetCode(String[] codeNames, String targetCode) {
        String targetCodeName;
        int targetCodeIdx = targetCode.indexOf(46);
        if (targetCodeIdx == -1) {
            targetCodeName = targetCode;
        } else {
            targetCodeName = targetCode.substring(0, targetCodeIdx);
        }
        return ArrayUtils.contains(codeNames, targetCodeName);
    }

    public static int computeTargetSdkVersion(int targetVers, String targetCode, String[] platformSdkCodenames, String[] outError) {
        if (targetCode == null) {
            return targetVers;
        }
        if (matchTargetCode(platformSdkCodenames, targetCode)) {
            return 10000;
        }
        if (platformSdkCodenames.length > 0) {
            outError[0] = "Requires development platform " + targetCode + " (current platform is any of " + Arrays.toString(platformSdkCodenames) + ")";
            return -1;
        }
        outError[0] = "Requires development platform " + targetCode + " but this is a release platform.";
        return -1;
    }

    public static int computeMinSdkVersion(int minVers, String minCode, int platformSdkVersion, String[] platformSdkCodenames, String[] outError) {
        if (minCode == null) {
            if (minVers <= platformSdkVersion) {
                return minVers;
            }
            outError[0] = "Requires newer sdk version #" + minVers + " (current version is #" + platformSdkVersion + ")";
            return -1;
        } else if (matchTargetCode(platformSdkCodenames, minCode)) {
            return 10000;
        } else {
            if (platformSdkCodenames.length > 0) {
                outError[0] = "Requires development platform " + minCode + " (current platform is any of " + Arrays.toString(platformSdkCodenames) + ")";
            } else {
                outError[0] = "Requires development platform " + minCode + " but this is a release platform.";
            }
            return -1;
        }
    }

    private FeatureInfo parseUsesFeature(Resources res, AttributeSet attrs) {
        FeatureInfo fi = new FeatureInfo();
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesFeature);
        fi.name = sa.getNonResourceString(0);
        fi.version = sa.getInt(3, 0);
        if (fi.name == null) {
            fi.reqGlEsVersion = sa.getInt(1, 0);
        }
        if (sa.getBoolean(2, true)) {
            fi.flags |= 1;
        }
        sa.recycle();
        return fi;
    }

    private boolean parseUsesStaticLibrary(Package pkg, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesStaticLibrary);
        String lname = sa.getNonResourceString(0);
        int version = sa.getInt(1, -1);
        String certSha256Digest = sa.getNonResourceString(2);
        sa.recycle();
        if (lname == null || version < 0 || certSha256Digest == null) {
            outError[0] = "Bad uses-static-library declaration name: " + lname + " version: " + version + " certDigest" + certSha256Digest;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            XmlUtils.skipCurrentTag(parser);
            return false;
        } else if (pkg.usesStaticLibraries == null || !pkg.usesStaticLibraries.contains(lname)) {
            String lname2 = lname.intern();
            String certSha256Digest2 = certSha256Digest.replace(SettingsStringUtil.DELIMITER, "").toLowerCase();
            String[] additionalCertSha256Digests = EmptyArray.STRING;
            if (pkg.applicationInfo.targetSdkVersion >= 27) {
                additionalCertSha256Digests = parseAdditionalCertificates(res, parser, outError);
                if (additionalCertSha256Digests == null) {
                    return false;
                }
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
            String[] certSha256Digests = new String[(additionalCertSha256Digests.length + 1)];
            certSha256Digests[0] = certSha256Digest2;
            System.arraycopy(additionalCertSha256Digests, 0, certSha256Digests, 1, additionalCertSha256Digests.length);
            pkg.usesStaticLibraries = ArrayUtils.add(pkg.usesStaticLibraries, lname2);
            pkg.usesStaticLibrariesVersions = ArrayUtils.appendLong(pkg.usesStaticLibrariesVersions, (long) version, true);
            pkg.usesStaticLibrariesCertDigests = (String[][]) ArrayUtils.appendElement(String[].class, pkg.usesStaticLibrariesCertDigests, certSha256Digests, true);
            return true;
        } else {
            outError[0] = "Depending on multiple versions of static library " + lname;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            XmlUtils.skipCurrentTag(parser);
            return false;
        }
    }

    private String[] parseAdditionalCertificates(Resources resources, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        String[] certSha256Digests = EmptyArray.STRING;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return certSha256Digests;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("additional-certificate")) {
                    TypedArray sa = resources.obtainAttributes(parser, R.styleable.AndroidManifestAdditionalCertificate);
                    String certSha256Digest = sa.getNonResourceString(0);
                    sa.recycle();
                    if (TextUtils.isEmpty(certSha256Digest)) {
                        outError[0] = "Bad additional-certificate declaration with empty certDigest:" + certSha256Digest;
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        XmlUtils.skipCurrentTag(parser);
                        sa.recycle();
                        return null;
                    }
                    certSha256Digests = (String[]) ArrayUtils.appendElement(String.class, certSha256Digests, certSha256Digest.replace(SettingsStringUtil.DELIMITER, "").toLowerCase());
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return certSha256Digests;
    }

    private boolean parseUsesPermission(Package pkg, Resources res, XmlResourceParser parser) throws XmlPullParserException, IOException {
        Callback callback;
        Callback callback2;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesPermission);
        String name = sa.getNonResourceString(0);
        int maxSdkVersion = 0;
        TypedValue val = sa.peekValue(1);
        if (val != null && val.type >= 16 && val.type <= 31) {
            maxSdkVersion = val.data;
        }
        String requiredFeature = sa.getNonConfigurationString(2, 0);
        String requiredNotfeature = sa.getNonConfigurationString(3, 0);
        sa.recycle();
        XmlUtils.skipCurrentTag(parser);
        if (name == null) {
            return true;
        }
        if (maxSdkVersion != 0 && maxSdkVersion < Build.VERSION.RESOURCES_SDK_INT) {
            return true;
        }
        if (requiredFeature != null && (callback2 = this.mCallback) != null && !callback2.hasFeature(requiredFeature)) {
            return true;
        }
        if (requiredNotfeature != null && (callback = this.mCallback) != null && callback.hasFeature(requiredNotfeature)) {
            return true;
        }
        if (pkg.requestedPermissions.indexOf(name) == -1) {
            pkg.requestedPermissions.add(name.intern());
        } else {
            Slog.w(TAG, "Ignoring duplicate uses-permissions/uses-permissions-sdk-m: " + name + " in package: " + pkg.packageName + " at: " + parser.getPositionDescription());
        }
        return true;
    }

    private static String buildClassName(String pkg, CharSequence clsSeq, String[] outError) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            outError[0] = "Empty class name in package " + pkg;
            return null;
        }
        String cls = clsSeq.toString();
        if (cls.charAt(0) == '.') {
            return pkg + cls;
        } else if (cls.indexOf(46) >= 0) {
            return cls;
        } else {
            return pkg + '.' + cls;
        }
    }

    private static String buildCompoundName(String pkg, CharSequence procSeq, String type, String[] outError) {
        String proc = procSeq.toString();
        char c = proc.charAt(0);
        if (pkg == null || c != ':') {
            String nameError = validateName(proc, true, false);
            if (nameError == null || "system".equals(proc)) {
                return proc;
            }
            outError[0] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError;
            return null;
        } else if (proc.length() < 2) {
            outError[0] = "Bad " + type + " name " + proc + " in package " + pkg + ": must be at least two characters";
            return null;
        } else {
            String nameError2 = validateName(proc.substring(1), false, false);
            if (nameError2 != null) {
                outError[0] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError2;
                return null;
            }
            return pkg + proc;
        }
    }

    /* access modifiers changed from: private */
    public static String buildProcessName(String pkg, String defProc, CharSequence procSeq, int flags, String[] separateProcesses, String[] outError) {
        if (!((flags & 2) == 0 || "system".equals(procSeq))) {
            return defProc != null ? defProc : pkg;
        }
        if (separateProcesses != null) {
            for (int i = separateProcesses.length - 1; i >= 0; i--) {
                String sp = separateProcesses[i];
                if (sp.equals(pkg) || sp.equals(defProc) || sp.equals(procSeq)) {
                    return pkg;
                }
            }
        }
        if (procSeq == null || procSeq.length() <= 0) {
            return defProc;
        }
        return TextUtils.safeIntern(buildCompoundName(pkg, procSeq, DumpHeapActivity.KEY_PROCESS, outError));
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

    private boolean parseKeySets(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        int currentKeySetDepth;
        int outerDepth;
        int outerDepth2 = parser.getDepth();
        int currentKeySetDepth2 = -1;
        String currentKeySet = null;
        ArrayMap<String, PublicKey> publicKeys = new ArrayMap<>();
        ArraySet<String> upgradeKeySets = new ArraySet<>();
        ArrayMap<String, ArraySet<String>> definedKeySets = new ArrayMap<>();
        ArraySet<String> improperKeySets = new ArraySet<>();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                break;
            }
            if (type == 3 && parser.getDepth() <= outerDepth2) {
                break;
            }
            if (type != 3) {
                String tagName = parser.getName();
                if (!tagName.equals("key-set")) {
                    if (!tagName.equals("public-key")) {
                        outerDepth = outerDepth2;
                        currentKeySetDepth = currentKeySetDepth2;
                        if (tagName.equals("upgrade-key-set")) {
                            TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUpgradeKeySet);
                            upgradeKeySets.add(sa.getNonResourceString(0));
                            sa.recycle();
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <key-sets>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } else if (currentKeySet == null) {
                        outError[0] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    } else {
                        TypedArray sa2 = res.obtainAttributes(parser, R.styleable.AndroidManifestPublicKey);
                        outerDepth = outerDepth2;
                        String publicKeyName = sa2.getNonResourceString(0);
                        String encodedKey = sa2.getNonResourceString(1);
                        if (encodedKey == null && publicKeys.get(publicKeyName) == null) {
                            outError[0] = "'public-key' " + publicKeyName + " must define a public-key value on first use at " + parser.getPositionDescription();
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            sa2.recycle();
                            return false;
                        }
                        currentKeySetDepth = currentKeySetDepth2;
                        if (encodedKey != null) {
                            PublicKey currentKey = parsePublicKey(encodedKey);
                            if (currentKey == null) {
                                Slog.w(TAG, "No recognized valid key in 'public-key' tag at " + parser.getPositionDescription() + " key-set " + currentKeySet + " will not be added to the package's defined key-sets.");
                                sa2.recycle();
                                improperKeySets.add(currentKeySet);
                                XmlUtils.skipCurrentTag(parser);
                            } else if (publicKeys.get(publicKeyName) == null || publicKeys.get(publicKeyName).equals(currentKey)) {
                                publicKeys.put(publicKeyName, currentKey);
                            } else {
                                outError[0] = "Value of 'public-key' " + publicKeyName + " conflicts with previously defined value at " + parser.getPositionDescription();
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                sa2.recycle();
                                return false;
                            }
                        }
                        definedKeySets.get(currentKeySet).add(publicKeyName);
                        sa2.recycle();
                        XmlUtils.skipCurrentTag(parser);
                    }
                    currentKeySetDepth2 = currentKeySetDepth;
                } else if (currentKeySet != null) {
                    outError[0] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                } else {
                    TypedArray sa3 = res.obtainAttributes(parser, R.styleable.AndroidManifestKeySet);
                    String keysetName = sa3.getNonResourceString(0);
                    definedKeySets.put(keysetName, new ArraySet<>());
                    currentKeySet = keysetName;
                    currentKeySetDepth2 = parser.getDepth();
                    sa3.recycle();
                    outerDepth = outerDepth2;
                }
                outerDepth2 = outerDepth;
            } else if (parser.getDepth() == currentKeySetDepth2) {
                currentKeySet = null;
                currentKeySetDepth2 = -1;
            } else {
                outerDepth = outerDepth2;
                currentKeySetDepth = currentKeySetDepth2;
            }
            outerDepth2 = outerDepth;
            currentKeySetDepth2 = currentKeySetDepth;
        }
        if (publicKeys.keySet().removeAll(definedKeySets.keySet())) {
            outError[0] = "Package" + owner.packageName + " AndroidManifext.xml 'key-set' and 'public-key' names must be distinct.";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        owner.mKeySetMapping = new ArrayMap<>();
        for (Map.Entry<String, ArraySet<String>> e : definedKeySets.entrySet()) {
            String keySetName = e.getKey();
            if (e.getValue().size() == 0) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml 'key-set' " + keySetName + " has no valid associated 'public-key'. Not including in package's defined key-sets.");
            } else if (improperKeySets.contains(keySetName)) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml 'key-set' " + keySetName + " contained improper 'public-key' tags. Not including in package's defined key-sets.");
            } else {
                owner.mKeySetMapping.put(keySetName, new ArraySet<>());
                for (Iterator<String> it = e.getValue().iterator(); it.hasNext(); it = it) {
                    owner.mKeySetMapping.get(keySetName).add(publicKeys.get(it.next()));
                }
            }
        }
        if (owner.mKeySetMapping.keySet().containsAll(upgradeKeySets)) {
            owner.mUpgradeKeySets = upgradeKeySets;
            return true;
        }
        outError[0] = "Package" + owner.packageName + " AndroidManifext.xml does not define all 'upgrade-key-set's .";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    private boolean parsePermissionGroup(Package owner, int flags, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermissionGroup);
        PermissionGroup perm = new PermissionGroup(owner, sa.getResourceId(12, 0), sa.getResourceId(9, 0), sa.getResourceId(10, 0));
        if (!parsePackageItemInfo(owner, perm.info, outError, "<permission-group>", sa, true, 2, 0, 1, 8, 5, 7)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        perm.info.descriptionRes = sa.getResourceId(4, 0);
        perm.info.requestRes = sa.getResourceId(11, 0);
        perm.info.flags = sa.getInt(6, 0);
        perm.info.priority = sa.getInt(3, 0);
        sa.recycle();
        if (!parseAllMetaData(res, parser, "<permission-group>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        owner.permissionGroups.add(perm);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x006c  */
    private boolean parsePermission(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        String backgroundPermission;
        Permission perm;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermission);
        if (sa.hasValue(10)) {
            if ("android".equals(owner.packageName)) {
                backgroundPermission = sa.getNonResourceString(10);
                perm = new Permission(owner, backgroundPermission);
                if (parsePackageItemInfo(owner, perm.info, outError, "<permission>", sa, true, 2, 0, 1, 9, 6, 8)) {
                    sa.recycle();
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                perm.info.group = sa.getNonResourceString(4);
                if (perm.info.group != null) {
                    perm.info.group = perm.info.group.intern();
                }
                perm.info.descriptionRes = sa.getResourceId(5, 0);
                perm.info.requestRes = sa.getResourceId(11, 0);
                perm.info.protectionLevel = sa.getInt(3, 0);
                perm.info.flags = sa.getInt(7, 0);
                if (!perm.info.isRuntime() || !"android".equals(perm.info.packageName)) {
                    perm.info.flags &= -5;
                    perm.info.flags &= -9;
                } else if (!((perm.info.flags & 4) == 0 || (perm.info.flags & 8) == 0)) {
                    throw new IllegalStateException("Permission cannot be both soft and hard restricted: " + perm.info.name);
                }
                sa.recycle();
                if (perm.info.protectionLevel == -1) {
                    outError[0] = "<permission> does not specify protectionLevel";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                }
                perm.info.protectionLevel = PermissionInfo.fixProtectionLevel(perm.info.protectionLevel);
                if (perm.info.getProtectionFlags() != 0 && (perm.info.protectionLevel & 4096) == 0 && (perm.info.protectionLevel & 8192) == 0 && (perm.info.protectionLevel & 15) != 2) {
                    outError[0] = "<permission>  protectionLevel specifies a non-instant flag but is not based on signature type";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                } else if (!parseAllMetaData(res, parser, "<permission>", perm, outError)) {
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                    return false;
                } else {
                    owner.permissions.add(perm);
                    return true;
                }
            } else {
                Slog.w(TAG, owner.packageName + " defines a background permission. Only the 'android' package can do that.");
            }
        }
        backgroundPermission = null;
        perm = new Permission(owner, backgroundPermission);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission>", sa, true, 2, 0, 1, 9, 6, 8)) {
        }
    }

    private boolean parsePermissionTree(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        int index;
        Permission perm = new Permission(owner, (String) null);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermissionTree);
        if (!parsePackageItemInfo(owner, perm.info, outError, "<permission-tree>", sa, true, 2, 0, 1, 5, 3, 4)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        sa.recycle();
        int index2 = perm.info.name.indexOf(46);
        if (index2 > 0) {
            index = perm.info.name.indexOf(46, index2 + 1);
        } else {
            index = index2;
        }
        if (index < 0) {
            outError[0] = "<permission-tree> name has less than three segments: " + perm.info.name;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        perm.info.descriptionRes = 0;
        perm.info.requestRes = 0;
        perm.info.protectionLevel = 0;
        perm.tree = true;
        if (!parseAllMetaData(res, parser, "<permission-tree>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        owner.permissions.add(perm);
        return true;
    }

    private Instrumentation parseInstrumentation(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestInstrumentation);
        if (this.mParseInstrumentationArgs == null) {
            this.mParseInstrumentationArgs = new ParsePackageItemArgs(owner, outError, 2, 0, 1, 8, 6, 7);
            this.mParseInstrumentationArgs.tag = "<instrumentation>";
        }
        ParsePackageItemArgs parsePackageItemArgs = this.mParseInstrumentationArgs;
        parsePackageItemArgs.sa = sa;
        Instrumentation a = new Instrumentation(parsePackageItemArgs, new InstrumentationInfo());
        if (outError[0] != null) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        String str = sa.getNonResourceString(3);
        a.info.targetPackage = str != null ? str.intern() : null;
        String str2 = sa.getNonResourceString(9);
        a.info.targetProcesses = str2 != null ? str2.intern() : null;
        a.info.handleProfiling = sa.getBoolean(4, false);
        a.info.functionalTest = sa.getBoolean(5, false);
        sa.recycle();
        if (a.info.targetPackage == null) {
            outError[0] = "<instrumentation> does not specify targetPackage";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        } else if (!parseAllMetaData(res, parser, "<instrumentation>", a, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        } else {
            owner.instrumentation.add(a);
            return a;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r12v8, resolved type: android.os.Bundle */
    /* JADX DEBUG: Multi-variable search result rejected for r4v10, resolved type: android.os.Bundle */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v68 */
    /* JADX WARN: Type inference failed for: r1v69, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r1v88 */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x0804, code lost:
        if (android.text.TextUtils.isEmpty(r14.staticSharedLibName) == false) goto L_0x0814;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x0806, code lost:
        r14.activities.add(r0.generateAppDetailsHiddenActivity(r14, r41, r9, r14.baseHardwareAccelerated));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x0816, code lost:
        if (r26 == 0) goto L_0x081f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:321:0x0818, code lost:
        java.util.Collections.sort(r14.activities, android.content.pm.$$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4.INSTANCE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:322:0x081f, code lost:
        if (r27 == false) goto L_0x0828;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x0821, code lost:
        java.util.Collections.sort(r14.receivers, android.content.pm.$$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw.INSTANCE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x0828, code lost:
        if (r28 == false) goto L_0x0831;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:325:0x082a, code lost:
        java.util.Collections.sort(r14.services, android.content.pm.$$Lambda$PackageParser$M9fHqS_eEp1oYkuKJhRHOGUxf8.INSTANCE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:326:0x0831, code lost:
        setMaxAspectRatio(r38);
        setMinAspectRatio(r38);
        r2 = false;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x083c, code lost:
        if (r14.mAppMetaData == null) goto L_0x084c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x0844, code lost:
        if (r14.mAppMetaData.containsKey(android.content.pm.PackageParser.METADATA_NOTCH_SUPPORT) == false) goto L_0x084c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x0846, code lost:
        r2 = r14.mAppMetaData.getBoolean(android.content.pm.PackageParser.METADATA_NOTCH_SUPPORT, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x084c, code lost:
        r4 = 0;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x0851, code lost:
        if (r14.mAppMetaData == null) goto L_0x0861;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x0859, code lost:
        if (r14.mAppMetaData.containsKey(android.content.pm.PackageParser.METADATA_GESTURE_NAV_OPTIONS) == false) goto L_0x0861;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:0x085b, code lost:
        r4 = r14.mAppMetaData.getInt(android.content.pm.PackageParser.METADATA_GESTURE_NAV_OPTIONS, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x0861, code lost:
        r12 = 0;
        r15 = r14.activities.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x0868, code lost:
        if (r12 >= r15) goto L_0x08b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x086a, code lost:
        r1 = r14.activities.get(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x0872, code lost:
        if (r2 != false) goto L_0x0888;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:0x0876, code lost:
        if (r1.metaData == null) goto L_0x0884;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:342:0x0878, code lost:
        r23 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:343:0x0881, code lost:
        if (r1.metaData.getBoolean(android.content.pm.PackageParser.METADATA_NOTCH_SUPPORT, false) == false) goto L_0x0890;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x0884, code lost:
        r23 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:345:0x0888, code lost:
        r23 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:346:0x088b, code lost:
        r1.info.hwNotchSupport = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:348:0x0892, code lost:
        if (r1.metaData == null) goto L_0x08a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:350:0x089a, code lost:
        if (r1.metaData.containsKey(android.content.pm.PackageParser.METADATA_GESTURE_NAV_OPTIONS) == false) goto L_0x08a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:351:0x089c, code lost:
        r1.info.hwGestureNavOptions = r1.metaData.getInt(android.content.pm.PackageParser.METADATA_GESTURE_NAV_OPTIONS, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:352:0x08a7, code lost:
        if (r4 == 0) goto L_0x08ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:353:0x08a9, code lost:
        r1.info.hwGestureNavOptions = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:354:0x08ad, code lost:
        r12 = r12 + 1;
        r2 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:356:0x08bb, code lost:
        if (hasDomainURLs(r38) == false) goto L_0x08c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:0x08bd, code lost:
        r14.applicationInfo.privateFlags |= 16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x08c6, code lost:
        r14.applicationInfo.privateFlags &= -17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:379:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:?, code lost:
        return true;
     */
    @UnsupportedAppUsage
    private boolean parseBaseApplication(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        String pkgName;
        String str;
        int i;
        String pkgName2;
        String[] strArr;
        PackageParser packageParser;
        ?? r1;
        int innerDepth;
        String restrictedAccountType;
        String restrictedAccountType2;
        String[] strArr2;
        XmlResourceParser xmlResourceParser;
        ApplicationInfo ai;
        PackageParser packageParser2;
        String pkgName3;
        int i2;
        String lname;
        int version;
        CharSequence pname;
        String requiredFeature;
        PackageParser packageParser3 = this;
        Package r14 = owner;
        XmlResourceParser xmlResourceParser2 = parser;
        ApplicationInfo ai2 = r14.applicationInfo;
        String pkgName4 = r14.applicationInfo.packageName;
        TypedArray sa = res.obtainAttributes(xmlResourceParser2, R.styleable.AndroidManifestApplication);
        ai2.iconRes = sa.getResourceId(2, 0);
        ai2.roundIconRes = sa.getResourceId(42, 0);
        String[] strArr3 = outError;
        if (!parsePackageItemInfo(owner, ai2, outError, "<application>", sa, false, 3, 1, 2, 42, 22, 30)) {
            sa.recycle();
            packageParser3.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        ApplicationInfo ai3 = ai2;
        if (ai3.name != null) {
            ai3.className = ai3.name;
        }
        String manageSpaceActivity = sa.getNonConfigurationString(4, 1024);
        if (manageSpaceActivity != null) {
            pkgName = pkgName4;
            ai3.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity, strArr3);
        } else {
            pkgName = pkgName4;
        }
        if (sa.getBoolean(17, true)) {
            ai3.flags |= 32768;
            String backupAgent = sa.getNonConfigurationString(16, 1024);
            if (backupAgent != null) {
                ai3.backupAgentName = buildClassName(pkgName, backupAgent, strArr3);
                if (sa.getBoolean(18, true)) {
                    ai3.flags |= 65536;
                }
                if (sa.getBoolean(21, false)) {
                    ai3.flags |= 131072;
                }
                if (sa.getBoolean(32, false)) {
                    ai3.flags |= 67108864;
                }
                if (sa.getBoolean(40, false)) {
                    ai3.privateFlags |= 8192;
                }
            }
            TypedValue v = sa.peekValue(35);
            if (v != null) {
                int i3 = v.resourceId;
                ai3.fullBackupContent = i3;
                if (i3 == 0) {
                    ai3.fullBackupContent = v.data == 0 ? -1 : 0;
                }
            }
        }
        ai3.theme = sa.getResourceId(0, 0);
        ai3.descriptionRes = sa.getResourceId(13, 0);
        r14.mPersistentApp = sa.getBoolean(8, false);
        if (r14.mPersistentApp && ((requiredFeature = sa.getNonResourceString(45)) == null || packageParser3.mCallback.hasFeature(requiredFeature))) {
            ai3.flags |= 8;
        }
        if (sa.getBoolean(27, false)) {
            r14.mRequiredForAllUsers = true;
        }
        String pkgName5 = sa.getString(28);
        if (pkgName5 != null && pkgName5.length() > 0) {
            r14.mRestrictedAccountType = pkgName5;
        }
        String requiredAccountType = sa.getString(29);
        if (requiredAccountType != null && requiredAccountType.length() > 0) {
            r14.mRequiredAccountType = requiredAccountType;
        }
        if (sa.getBoolean(10, false)) {
            ai3.flags |= 2;
            ai3.privateFlags |= 8388608;
        }
        if (sa.getBoolean(20, false)) {
            ai3.flags |= 16384;
        }
        r14.baseHardwareAccelerated = sa.getBoolean(23, r14.applicationInfo.targetSdkVersion >= 14);
        if (r14.baseHardwareAccelerated) {
            ai3.flags |= 536870912;
        }
        if (sa.getBoolean(7, true)) {
            ai3.flags |= 4;
        }
        if (sa.getBoolean(14, false)) {
            ai3.flags |= 32;
        }
        if (sa.getBoolean(5, true)) {
            ai3.flags |= 64;
        }
        if (r14.parentPackage == null && sa.getBoolean(15, false)) {
            ai3.flags |= 256;
        }
        if (sa.getBoolean(24, false)) {
            ai3.flags |= 1048576;
        }
        if (sa.getBoolean(36, r14.applicationInfo.targetSdkVersion < 28)) {
            ai3.flags |= 134217728;
        }
        if (sa.getBoolean(26, false)) {
            ai3.flags |= 4194304;
        }
        if (sa.getBoolean(33, false)) {
            ai3.flags |= Integer.MIN_VALUE;
        }
        if (sa.getBoolean(34, true)) {
            ai3.flags |= 268435456;
        }
        if (sa.getBoolean(53, false)) {
            ai3.privateFlags |= 33554432;
        }
        if (sa.getBoolean(38, false)) {
            ai3.privateFlags |= 32;
        }
        if (sa.getBoolean(39, false)) {
            ai3.privateFlags |= 64;
        }
        if (sa.hasValueOrEmpty(37)) {
            if (sa.getBoolean(37, true)) {
                ai3.privateFlags |= 1024;
            } else {
                ai3.privateFlags |= 2048;
            }
        } else if (r14.applicationInfo.targetSdkVersion >= 24) {
            ai3.privateFlags |= 4096;
        }
        if (sa.getBoolean(54, true)) {
            ai3.privateFlags |= 67108864;
        }
        if (sa.getBoolean(55, r14.applicationInfo.targetSdkVersion >= 29)) {
            ai3.privateFlags |= 134217728;
        }
        if (sa.getBoolean(56, r14.applicationInfo.targetSdkVersion < 29)) {
            ai3.privateFlags |= 536870912;
        }
        ai3.maxAspectRatio = sa.getFloat(44, 0.0f);
        ai3.minAspectRatio = sa.getFloat(51, 0.0f);
        ai3.networkSecurityConfigRes = sa.getResourceId(41, 0);
        ai3.category = sa.getInt(43, -1);
        String str2 = sa.getNonConfigurationString(6, 0);
        ai3.permission = (str2 == null || str2.length() <= 0) ? null : str2.intern();
        if (r14.applicationInfo.targetSdkVersion >= 8) {
            str = sa.getNonConfigurationString(12, 1024);
        } else {
            str = sa.getNonResourceString(12);
        }
        ai3.taskAffinity = buildTaskAffinityName(ai3.packageName, ai3.packageName, str, strArr3);
        String factory = sa.getNonResourceString(48);
        if (factory != null) {
            ai3.appComponentFactory = buildClassName(ai3.packageName, factory, strArr3);
        }
        if (sa.getBoolean(49, false)) {
            ai3.privateFlags |= 4194304;
        }
        if (sa.getBoolean(50, false)) {
            ai3.privateFlags |= 16777216;
        }
        if (strArr3[0] == null) {
            if (r14.applicationInfo.targetSdkVersion >= 8) {
                pname = sa.getNonConfigurationString(11, 1024);
            } else {
                pname = sa.getNonResourceString(11);
            }
            i = 1;
            i = 1;
            i = 1;
            i = 1;
            pkgName2 = pkgName;
            ai3.processName = buildProcessName(ai3.packageName, null, pname, flags, packageParser3.mSeparateProcesses, outError);
            ai3.enabled = sa.getBoolean(9, true);
            if (sa.getBoolean(31, false)) {
                ai3.flags |= 33554432;
            }
            if (sa.getBoolean(47, false)) {
                ai3.privateFlags |= 2;
                if (ai3.processName != null && !ai3.processName.equals(ai3.packageName)) {
                    strArr3[0] = "cantSaveState applications can not use custom processes";
                }
            }
        } else {
            i = 1;
            pkgName2 = pkgName;
        }
        ai3.uiOptions = sa.getInt(25, 0);
        ai3.classLoaderName = sa.getString(46);
        if (ai3.classLoaderName != null && !ClassLoaderFactory.isValidClassLoaderName(ai3.classLoaderName)) {
            strArr3[0] = "Invalid class loader name: " + ai3.classLoaderName;
        }
        ai3.zygotePreloadName = sa.getString(52);
        sa.recycle();
        if (strArr3[0] != null) {
            packageParser3.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        int innerDepth2 = parser.getDepth();
        CachedComponentArgs cachedArgs = new CachedComponentArgs();
        int i4 = 0;
        boolean hasReceiverOrder = false;
        boolean hasServiceOrder = false;
        while (true) {
            int type = parser.next();
            if (type != i) {
                if (type == 3 && parser.getDepth() <= innerDepth2) {
                    strArr = strArr3;
                    packageParser = packageParser3;
                    r1 = 0;
                    break;
                }
                if (type == 3) {
                    innerDepth = innerDepth2;
                    ai = ai3;
                    restrictedAccountType = pkgName5;
                    strArr2 = strArr3;
                    packageParser2 = packageParser3;
                    restrictedAccountType2 = pkgName2;
                    xmlResourceParser = xmlResourceParser2;
                } else if (type == 4) {
                    innerDepth = innerDepth2;
                    ai = ai3;
                    restrictedAccountType = pkgName5;
                    strArr2 = strArr3;
                    packageParser2 = packageParser3;
                    restrictedAccountType2 = pkgName2;
                    xmlResourceParser = xmlResourceParser2;
                } else {
                    String tagName = parser.getName();
                    if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                        innerDepth = innerDepth2;
                        Activity a = parseActivity(owner, res, parser, flags, outError, cachedArgs, false, r14.baseHardwareAccelerated);
                        if (a == null) {
                            packageParser3.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return false;
                        }
                        int i5 = a.order != 0 ? i : 0;
                        r14.activities.add(a);
                        i4 |= i5;
                        ai = ai3;
                        restrictedAccountType = pkgName5;
                        xmlResourceParser = xmlResourceParser2;
                        strArr2 = strArr3;
                        packageParser2 = packageParser3;
                        restrictedAccountType2 = pkgName2;
                    } else {
                        innerDepth = innerDepth2;
                        if (tagName.equals("receiver")) {
                            ai = ai3;
                            restrictedAccountType = pkgName5;
                            packageParser2 = packageParser3;
                            Activity a2 = parseActivity(owner, res, parser, flags, outError, cachedArgs, true, false);
                            if (a2 == null) {
                                packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return false;
                            }
                            boolean z = a2.order != 0;
                            r14 = owner;
                            r14.receivers.add(a2);
                            xmlResourceParser = parser;
                            strArr2 = outError;
                            hasReceiverOrder |= z;
                            restrictedAccountType2 = pkgName2;
                        } else {
                            ai = ai3;
                            restrictedAccountType = pkgName5;
                            packageParser2 = packageParser3;
                            if (tagName.equals("service")) {
                                Service s = parseService(owner, res, parser, flags, outError, cachedArgs);
                                if (s == null) {
                                    packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                boolean z2 = s.order != 0;
                                r14.services.add(s);
                                xmlResourceParser = parser;
                                strArr2 = outError;
                                hasServiceOrder |= z2;
                                restrictedAccountType2 = pkgName2;
                            } else if (tagName.equals("provider")) {
                                Provider p = parseProvider(owner, res, parser, flags, outError, cachedArgs);
                                if (p == null) {
                                    packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                r14.providers.add(p);
                                xmlResourceParser = parser;
                                strArr2 = outError;
                                restrictedAccountType2 = pkgName2;
                            } else if (tagName.equals("activity-alias")) {
                                Activity a3 = parseActivityAlias(owner, res, parser, flags, outError, cachedArgs);
                                if (a3 == null) {
                                    packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                int i6 = a3.order != 0 ? 1 : 0;
                                r14.activities.add(a3);
                                xmlResourceParser = parser;
                                strArr2 = outError;
                                i4 |= i6;
                                restrictedAccountType2 = pkgName2;
                            } else if (parser.getName().equals("meta-data")) {
                                xmlResourceParser = parser;
                                strArr2 = outError;
                                Bundle parseMetaData = packageParser2.parseMetaData(res, xmlResourceParser, r14.mAppMetaData, strArr2);
                                r14.mAppMetaData = parseMetaData;
                                if (parseMetaData == null) {
                                    packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                int themeId = HwWidgetFactory.getThemeId(r14.mAppMetaData, res);
                                if (themeId != 0) {
                                    ai.theme = themeId;
                                }
                                ai.minEmuiSdkVersion = r14.mAppMetaData.getInt("huawei.emui_minSdk");
                                ai.targetEmuiSdkVersion = r14.mAppMetaData.getInt("huawei.emui_targetSdk");
                                ai.hwThemeType = r14.mAppMetaData.getInt("hw.theme_type");
                                ai.minEmuiSysImgVersion = r14.mAppMetaData.getInt("huawei.emui_minSysImgVersion", -1);
                                ai.gestnav_extra_flags = r14.mAppMetaData.getInt("huawei.gestnav_extra_flags");
                                ai.owns = r14.mAppMetaData.getInt("android.owns", -1);
                                restrictedAccountType2 = pkgName2;
                            } else {
                                xmlResourceParser = parser;
                                strArr2 = outError;
                                if (tagName.equals("static-library")) {
                                    TypedArray sa2 = res.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestStaticLibrary);
                                    lname = sa2.getNonResourceString(0);
                                    version = sa2.getInt(1, -1);
                                    int versionMajor = sa2.getInt(2, 0);
                                    sa2.recycle();
                                    if (lname == null) {
                                        break;
                                    } else if (version < 0) {
                                        break;
                                    } else if (r14.mSharedUserId != null) {
                                        strArr2[0] = "sharedUserId not allowed in static shared library";
                                        packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                                        XmlUtils.skipCurrentTag(parser);
                                        return false;
                                    } else if (r14.staticSharedLibName != null) {
                                        strArr2[0] = "Multiple static-shared libs for package " + pkgName2;
                                        packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        XmlUtils.skipCurrentTag(parser);
                                        return false;
                                    } else {
                                        restrictedAccountType2 = pkgName2;
                                        r14.staticSharedLibName = lname.intern();
                                        if (version >= 0) {
                                            r14.staticSharedLibVersion = PackageInfo.composeLongVersionCode(versionMajor, version);
                                        } else {
                                            r14.staticSharedLibVersion = (long) version;
                                        }
                                        ai.privateFlags |= 16384;
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                } else {
                                    restrictedAccountType2 = pkgName2;
                                    if (tagName.equals("library")) {
                                        TypedArray sa3 = res.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestLibrary);
                                        String lname2 = sa3.getNonResourceString(0);
                                        sa3.recycle();
                                        if (lname2 != null) {
                                            String lname3 = lname2.intern();
                                            if (!ArrayUtils.contains(r14.libraryNames, lname3)) {
                                                r14.libraryNames = ArrayUtils.add(r14.libraryNames, lname3);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                    } else if (tagName.equals("uses-static-library")) {
                                        if (!packageParser2.parseUsesStaticLibrary(r14, res, xmlResourceParser, strArr2)) {
                                            return false;
                                        }
                                    } else if (tagName.equals("uses-library")) {
                                        TypedArray sa4 = res.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestUsesLibrary);
                                        String lname4 = sa4.getNonResourceString(0);
                                        boolean req = sa4.getBoolean(1, true);
                                        sa4.recycle();
                                        if (lname4 != null) {
                                            String lname5 = lname4.intern();
                                            if (req) {
                                                r14.usesLibraries = ArrayUtils.add(r14.usesLibraries, lname5);
                                            } else {
                                                r14.usesOptionalLibraries = ArrayUtils.add(r14.usesOptionalLibraries, lname5);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                    } else if (tagName.equals("uses-package")) {
                                        XmlUtils.skipCurrentTag(parser);
                                    } else if (tagName.equals("profileable")) {
                                        TypedArray sa5 = res.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestProfileable);
                                        if (sa5.getBoolean(0, false)) {
                                            ai.privateFlags |= 8388608;
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                        sa5.recycle();
                                    } else {
                                        Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + packageParser2.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                    }
                    packageParser3 = packageParser2;
                    xmlResourceParser2 = xmlResourceParser;
                    strArr3 = strArr2;
                    pkgName3 = restrictedAccountType2;
                    pkgName5 = restrictedAccountType;
                    innerDepth2 = innerDepth;
                    i2 = 1;
                    ai3 = ai;
                }
                packageParser3 = packageParser2;
                xmlResourceParser2 = xmlResourceParser;
                strArr3 = strArr2;
                pkgName3 = restrictedAccountType2;
                pkgName5 = restrictedAccountType;
                innerDepth2 = innerDepth;
                i2 = 1;
                ai3 = ai;
            } else {
                strArr = strArr3;
                packageParser = packageParser3;
                r1 = 0;
                break;
            }
        }
        strArr2[0] = "Bad static-library declaration name: " + lname + " version: " + version;
        packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        XmlUtils.skipCurrentTag(parser);
        return false;
    }

    private static boolean hasDomainURLs(Package pkg) {
        if (pkg == null || pkg.activities == null) {
            return false;
        }
        ArrayList<Activity> activities = pkg.activities;
        int countActivities = activities.size();
        for (int n = 0; n < countActivities; n++) {
            ArrayList<ActivityIntentInfo> filters = activities.get(n).intents;
            if (filters != null) {
                int countFilters = filters.size();
                for (int m = 0; m < countFilters; m++) {
                    ActivityIntentInfo aii = filters.get(m);
                    if (aii.hasAction("android.intent.action.VIEW") && aii.hasAction("android.intent.action.VIEW") && (aii.hasDataScheme(IntentFilter.SCHEME_HTTP) || aii.hasDataScheme(IntentFilter.SCHEME_HTTPS))) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r3v23 android.content.pm.ComponentInfo: [D('a' android.content.pm.PackageParser$Activity), D('parsedComponent' android.content.pm.ComponentInfo)] */
    /* JADX INFO: Multiple debug info for r3v26 android.content.pm.ComponentInfo: [D('p' android.content.pm.PackageParser$Provider), D('parsedComponent' android.content.pm.ComponentInfo)] */
    /* JADX INFO: Multiple debug info for r3v29 android.content.pm.ComponentInfo: [D('s' android.content.pm.PackageParser$Service), D('parsedComponent' android.content.pm.ComponentInfo)] */
    /* JADX INFO: Multiple debug info for r3v33 android.content.pm.ComponentInfo: [D('a' android.content.pm.PackageParser$Activity), D('parsedComponent' android.content.pm.ComponentInfo)] */
    /* JADX INFO: Multiple debug info for r0v19 android.content.pm.ComponentInfo: [D('a' android.content.pm.PackageParser$Activity), D('parsedComponent' android.content.pm.ComponentInfo)] */
    private boolean parseSplitApplication(Package owner, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException {
        String classLoaderName;
        int innerDepth;
        int i;
        XmlResourceParser xmlResourceParser;
        Resources resources;
        String[] strArr;
        int i2;
        int innerDepth2;
        int i3;
        PackageParser packageParser;
        ComponentInfo parsedComponent;
        PackageParser packageParser2 = this;
        Package r14 = owner;
        Resources resources2 = res;
        XmlResourceParser xmlResourceParser2 = parser;
        int i4 = splitIndex;
        String[] strArr2 = outError;
        TypedArray sa = resources2.obtainAttributes(xmlResourceParser2, R.styleable.AndroidManifestApplication);
        int i5 = 1;
        int i6 = 4;
        if (sa.getBoolean(7, true)) {
            int[] iArr = r14.splitFlags;
            iArr[i4] = iArr[i4] | 4;
        }
        String classLoaderName2 = sa.getString(46);
        int i7 = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        int i8 = 0;
        if (classLoaderName2 == null || ClassLoaderFactory.isValidClassLoaderName(classLoaderName2)) {
            r14.applicationInfo.splitClassLoaderNames[i4] = classLoaderName2;
            int innerDepth3 = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == i5) {
                    return true;
                }
                if (type == 3 && parser.getDepth() <= innerDepth3) {
                    return true;
                }
                if (type == 3) {
                    innerDepth = innerDepth3;
                    innerDepth2 = i8;
                    i3 = i7;
                    classLoaderName = classLoaderName2;
                    i = i6;
                    strArr = strArr2;
                    i2 = i4;
                    xmlResourceParser = xmlResourceParser2;
                    resources = resources2;
                    packageParser = packageParser2;
                } else if (type == i6) {
                    innerDepth = innerDepth3;
                    innerDepth2 = i8;
                    i3 = i7;
                    classLoaderName = classLoaderName2;
                    i = i6;
                    strArr = strArr2;
                    i2 = i4;
                    xmlResourceParser = xmlResourceParser2;
                    resources = resources2;
                    packageParser = packageParser2;
                } else {
                    CachedComponentArgs cachedArgs = new CachedComponentArgs();
                    String tagName = parser.getName();
                    if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                        innerDepth = innerDepth3;
                        classLoaderName = classLoaderName2;
                        i = i6;
                        Activity a = parseActivity(owner, res, parser, flags, outError, cachedArgs, false, r14.baseHardwareAccelerated);
                        if (a == null) {
                            packageParser2.mParseError = i7;
                            return false;
                        }
                        r14.activities.add(a);
                        resources = res;
                        parsedComponent = a.info;
                        innerDepth2 = 0;
                        strArr = strArr2;
                        i2 = i4;
                        xmlResourceParser = xmlResourceParser2;
                        i3 = i7;
                        packageParser = packageParser2;
                    } else {
                        innerDepth = innerDepth3;
                        classLoaderName = classLoaderName2;
                        i = i6;
                        if (tagName.equals("receiver")) {
                            strArr = strArr2;
                            i2 = i4;
                            i3 = i7;
                            packageParser = packageParser2;
                            Activity a2 = parseActivity(owner, res, parser, flags, outError, cachedArgs, true, false);
                            if (a2 == null) {
                                packageParser.mParseError = i3;
                                return false;
                            }
                            r14 = owner;
                            r14.receivers.add(a2);
                            resources = res;
                            xmlResourceParser = parser;
                            parsedComponent = a2.info;
                            innerDepth2 = 0;
                        } else {
                            strArr = strArr2;
                            i2 = i4;
                            i3 = i7;
                            packageParser = packageParser2;
                            if (tagName.equals("service")) {
                                Service s = parseService(owner, res, parser, flags, outError, cachedArgs);
                                if (s == null) {
                                    packageParser.mParseError = i3;
                                    return false;
                                }
                                r14.services.add(s);
                                resources = res;
                                xmlResourceParser = parser;
                                parsedComponent = s.info;
                                innerDepth2 = 0;
                            } else if (tagName.equals("provider")) {
                                Provider p = parseProvider(owner, res, parser, flags, outError, cachedArgs);
                                if (p == null) {
                                    packageParser.mParseError = i3;
                                    return false;
                                }
                                IHwPackageParser iHwPackageParser = mHwPackageParser;
                                if (iHwPackageParser != null) {
                                    iHwPackageParser.updateBaseProvider(new PackageParserEx.PackageEx(r14), p.info, i2);
                                }
                                r14.providers.add(p);
                                resources = res;
                                xmlResourceParser = parser;
                                parsedComponent = p.info;
                                innerDepth2 = 0;
                            } else if (tagName.equals("activity-alias")) {
                                Activity a3 = parseActivityAlias(owner, res, parser, flags, outError, cachedArgs);
                                if (a3 == null) {
                                    packageParser.mParseError = i3;
                                    return false;
                                }
                                r14.activities.add(a3);
                                resources = res;
                                xmlResourceParser = parser;
                                parsedComponent = a3.info;
                                innerDepth2 = 0;
                            } else {
                                if (parser.getName().equals("meta-data")) {
                                    resources = res;
                                    xmlResourceParser = parser;
                                    Bundle parseMetaData = packageParser.parseMetaData(resources, xmlResourceParser, r14.mAppMetaData, strArr);
                                    r14.mAppMetaData = parseMetaData;
                                    if (parseMetaData == null) {
                                        packageParser.mParseError = i3;
                                        return false;
                                    }
                                    innerDepth2 = 0;
                                } else {
                                    resources = res;
                                    xmlResourceParser = parser;
                                    innerDepth2 = 0;
                                    if (tagName.equals("uses-static-library")) {
                                        if (!packageParser.parseUsesStaticLibrary(r14, resources, xmlResourceParser, strArr)) {
                                            return false;
                                        }
                                    } else if (tagName.equals("uses-library")) {
                                        TypedArray sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestUsesLibrary);
                                        String lname = sa2.getNonResourceString(0);
                                        boolean req = sa2.getBoolean(1, true);
                                        sa2.recycle();
                                        if (lname != null) {
                                            String lname2 = lname.intern();
                                            if (req) {
                                                r14.usesLibraries = ArrayUtils.add(r14.usesLibraries, lname2);
                                                r14.usesOptionalLibraries = ArrayUtils.remove(r14.usesOptionalLibraries, lname2);
                                            } else if (!ArrayUtils.contains(r14.usesLibraries, lname2)) {
                                                r14.usesOptionalLibraries = ArrayUtils.add(r14.usesOptionalLibraries, lname2);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                        parsedComponent = null;
                                    } else if (tagName.equals("uses-package")) {
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + packageParser.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                                parsedComponent = null;
                            }
                        }
                    }
                    if (parsedComponent != null && parsedComponent.splitName == null) {
                        parsedComponent.splitName = r14.splitNames[i2];
                    }
                    packageParser2 = packageParser;
                    i4 = i2;
                    strArr2 = strArr;
                    resources2 = resources;
                    xmlResourceParser2 = xmlResourceParser;
                    i6 = i;
                    classLoaderName2 = classLoaderName;
                    i5 = 1;
                    i7 = i3;
                    i8 = innerDepth2;
                    innerDepth3 = innerDepth;
                }
                packageParser2 = packageParser;
                i4 = i2;
                strArr2 = strArr;
                resources2 = resources;
                xmlResourceParser2 = xmlResourceParser;
                i6 = i;
                classLoaderName2 = classLoaderName;
                i5 = 1;
                i7 = i3;
                i8 = innerDepth2;
                innerDepth3 = innerDepth;
            }
        } else {
            strArr2[0] = "Invalid class loader name: " + classLoaderName2;
            packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static boolean parsePackageItemInfo(Package owner, PackageItemInfo outInfo, String[] outError, String tag, TypedArray sa, boolean nameRequired, int nameRes, int labelRes, int iconRes, int roundIconRes, int logoRes, int bannerRes) {
        if (sa == null) {
            outError[0] = tag + " does not contain any attributes";
            return false;
        }
        String name = sa.getNonConfigurationString(nameRes, 0);
        if (name != null) {
            String outInfoName = buildClassName(owner.applicationInfo.packageName, name, outError);
            if (PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME.equals(outInfoName)) {
                outError[0] = tag + " invalid android:name";
                return false;
            }
            outInfo.name = outInfoName;
            if (outInfoName == null) {
                return false;
            }
        } else if (nameRequired) {
            outError[0] = tag + " does not specify android:name";
            return false;
        }
        int roundIconVal = sUseRoundIcon ? sa.getResourceId(roundIconRes, 0) : 0;
        if (roundIconVal != 0) {
            outInfo.icon = roundIconVal;
            outInfo.nonLocalizedLabel = null;
        } else {
            int iconVal = sa.getResourceId(iconRes, 0);
            if (iconVal != 0) {
                outInfo.icon = iconVal;
                outInfo.nonLocalizedLabel = null;
            }
        }
        int logoVal = sa.getResourceId(logoRes, 0);
        if (logoVal != 0) {
            outInfo.logo = logoVal;
        }
        int bannerVal = sa.getResourceId(bannerRes, 0);
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

    private Activity generateAppDetailsHiddenActivity(Package owner, int flags, String[] outError, boolean hardwareAccelerated) {
        Activity a = new Activity(owner, PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME, new ActivityInfo());
        a.owner = owner;
        a.setPackageName(owner.packageName);
        a.info.theme = 16973909;
        a.info.exported = true;
        a.info.name = PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME;
        a.info.processName = owner.applicationInfo.processName;
        a.info.uiOptions = a.info.applicationInfo.uiOptions;
        a.info.taskAffinity = buildTaskAffinityName(owner.packageName, owner.packageName, ":app_details", outError);
        a.info.enabled = true;
        a.info.launchMode = 0;
        a.info.documentLaunchMode = 0;
        a.info.maxRecents = ActivityTaskManager.getDefaultAppRecentsLimitStatic();
        a.info.configChanges = getActivityConfigChanges(0, 0);
        a.info.softInputMode = 0;
        a.info.persistableMode = 1;
        a.info.screenOrientation = -1;
        a.info.resizeMode = 4;
        a.info.lockTaskLaunchMode = 0;
        ActivityInfo activityInfo = a.info;
        a.info.directBootAware = false;
        activityInfo.encryptionAware = false;
        a.info.rotationAnimation = -1;
        a.info.colorMode = 0;
        if (hardwareAccelerated) {
            a.info.flags |= 512;
        }
        return a;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.content.res.TypedArray */
    /* JADX DEBUG: Multi-variable search result rejected for r17v0, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: android.content.res.TypedArray */
    /* JADX DEBUG: Multi-variable search result rejected for r3v4, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r3v5, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r3v6, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r1v3, resolved type: android.content.res.TypedArray */
    /* JADX DEBUG: Multi-variable search result rejected for r17v1, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r17v2, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r17v3, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r17v4, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v51, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r2v66 */
    /* JADX WARN: Type inference failed for: r2v68 */
    /* JADX WARN: Type inference failed for: r2v70 */
    private Activity parseActivity(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs, boolean receiver, boolean hardwareAccelerated) throws XmlPullParserException, IOException {
        int i;
        int i2;
        ActivityIntentInfo intent;
        int visibility;
        char c;
        ActivityIntentInfo intent2;
        int visibility2;
        ?? r2;
        Package r6 = owner;
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        String[] strArr = outError;
        TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestActivity);
        if (cachedArgs.mActivityArgs == null) {
            cachedArgs.mActivityArgs = new ParseComponentArgs(owner, outError, 3, 1, 2, 44, 23, 30, this.mSeparateProcesses, 7, 17, 5);
        }
        cachedArgs.mActivityArgs.tag = receiver ? "<receiver>" : "<activity>";
        cachedArgs.mActivityArgs.sa = sa;
        cachedArgs.mActivityArgs.flags = flags;
        Activity a = new Activity(cachedArgs.mActivityArgs, new ActivityInfo());
        int i3 = 0;
        i3 = 0;
        if (strArr[0] != null) {
            sa.recycle();
            return null;
        }
        boolean setExported = sa.hasValue(6);
        if (setExported) {
            a.info.exported = sa.getBoolean(6, false);
        }
        a.info.theme = sa.getResourceId(0, 0);
        a.info.uiOptions = sa.getInt(26, a.info.applicationInfo.uiOptions);
        String parentName = sa.getNonConfigurationString(27, 1024);
        if (parentName != null) {
            String parentClassName = buildClassName(a.info.packageName, parentName, strArr);
            if (strArr[0] == null) {
                a.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity " + a.info.name + " specified invalid parentActivityName " + parentName);
                i3 = 0;
                strArr[0] = null;
            }
        }
        String str = sa.getNonConfigurationString(4, i3);
        if (str == null) {
            a.info.permission = r6.applicationInfo.permission;
        } else {
            a.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        a.info.taskAffinity = buildTaskAffinityName(r6.applicationInfo.packageName, r6.applicationInfo.taskAffinity, sa.getNonConfigurationString(8, 1024), strArr);
        a.info.splitName = sa.getNonConfigurationString(48, 0);
        a.info.flags = 0;
        if (sa.getBoolean(9, false)) {
            a.info.flags |= 1;
        }
        if (sa.getBoolean(10, false)) {
            a.info.flags |= 2;
        }
        if (sa.getBoolean(11, false)) {
            a.info.flags |= 4;
        }
        if (sa.getBoolean(21, false)) {
            a.info.flags |= 128;
        }
        if (sa.getBoolean(18, false)) {
            a.info.flags |= 8;
        }
        if (sa.getBoolean(12, false)) {
            a.info.flags |= 16;
        }
        if (sa.getBoolean(13, false)) {
            a.info.flags |= 32;
        }
        if (sa.getBoolean(19, (r6.applicationInfo.flags & 32) != 0)) {
            a.info.flags |= 64;
        }
        if (sa.getBoolean(22, false)) {
            a.info.flags |= 256;
        }
        if (sa.getBoolean(29, false) || sa.getBoolean(39, false)) {
            a.info.flags |= 1024;
        }
        if (sa.getBoolean(24, false)) {
            a.info.flags |= 2048;
        }
        if (sa.getBoolean(56, false)) {
            a.info.flags |= 536870912;
        }
        if (!receiver) {
            if (sa.getBoolean(25, hardwareAccelerated)) {
                a.info.flags |= 512;
            }
            a.info.launchMode = sa.getInt(14, 0);
            a.info.documentLaunchMode = sa.getInt(33, 0);
            a.info.maxRecents = sa.getInt(34, ActivityTaskManager.getDefaultAppRecentsLimitStatic());
            a.info.configChanges = getActivityConfigChanges(sa.getInt(16, 0), sa.getInt(47, 0));
            a.info.softInputMode = sa.getInt(20, 0);
            a.info.persistableMode = sa.getInteger(32, 0);
            if (sa.getBoolean(31, false)) {
                a.info.flags |= Integer.MIN_VALUE;
            }
            if (sa.getBoolean(35, false)) {
                a.info.flags |= 8192;
            }
            if (sa.getBoolean(36, false)) {
                a.info.flags |= 4096;
            }
            if (sa.getBoolean(37, false)) {
                a.info.flags |= 16384;
            }
            a.info.screenOrientation = sa.getInt(15, -1);
            setActivityResizeMode(a.info, sa, r6);
            if (sa.getBoolean(41, false)) {
                a.info.flags |= 4194304;
            }
            if (sa.getBoolean(55, false)) {
                a.info.flags |= 262144;
            }
            if (sa.hasValue(50) && sa.getType(50) == 4) {
                a.setMaxAspectRatio(sa.getFloat(50, 0.0f));
            }
            if (!sa.hasValue(53)) {
                r2 = 0;
            } else if (sa.getType(53) == 4) {
                r2 = 0;
                a.setMinAspectRatio(sa.getFloat(53, 0.0f), false);
            } else {
                r2 = 0;
            }
            a.info.lockTaskLaunchMode = sa.getInt(38, r2);
            ActivityInfo activityInfo = a.info;
            ActivityInfo activityInfo2 = a.info;
            boolean z = sa.getBoolean(42, r2);
            activityInfo2.directBootAware = z;
            activityInfo.encryptionAware = z;
            a.info.requestedVrComponent = sa.getString(43);
            a.info.rotationAnimation = sa.getInt(46, -1);
            a.info.colorMode = sa.getInt(49, 0);
            if (sa.getBoolean(51, false)) {
                a.info.flags |= 8388608;
            }
            if (sa.getBoolean(52, false)) {
                a.info.flags |= 16777216;
            }
            if (sa.getBoolean(54, false)) {
                a.info.privateFlags |= 1;
            }
        } else {
            a.info.launchMode = 0;
            a.info.configChanges = 0;
            if (sa.getBoolean(28, false)) {
                a.info.flags |= 1073741824;
            }
            ActivityInfo activityInfo3 = a.info;
            ActivityInfo activityInfo4 = a.info;
            boolean z2 = sa.getBoolean(42, false);
            activityInfo4.directBootAware = z2;
            activityInfo3.encryptionAware = z2;
        }
        if (a.info.directBootAware) {
            r6.applicationInfo.privateFlags |= 256;
        }
        boolean visibleToEphemeral = sa.getBoolean(45, false);
        if (visibleToEphemeral) {
            a.info.flags |= 1048576;
            r6.visibleToInstantApps = true;
        }
        sa.recycle();
        if (receiver) {
            i = 2;
            i = 2;
            i = 2;
            if ((r6.applicationInfo.privateFlags & 2) == 0) {
                i2 = 0;
            } else if (a.info.processName == r6.packageName) {
                i2 = 0;
                strArr[0] = "Heavy-weight applications can not have receivers in main process";
            } else {
                i2 = 0;
            }
        } else {
            i2 = 0;
            i = 2;
        }
        if (strArr[i2] != null) {
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1) {
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    break;
                } else if (type == 3) {
                    sa = sa;
                    xmlResourceParser = xmlResourceParser;
                    outerDepth = outerDepth;
                } else if (type != 4) {
                    if (parser.getName().equals("intent-filter")) {
                        ActivityIntentInfo intent3 = new ActivityIntentInfo(a);
                        if (!parseIntent(res, parser, true, true, intent3, outError)) {
                            return null;
                        }
                        if (intent3.countActions() == 0) {
                            Slog.w(TAG, "No actions in intent filter at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            intent2 = intent3;
                        } else {
                            a.order = Math.max(intent3.getOrder(), a.order);
                            intent2 = intent3;
                            a.intents.add(intent2);
                        }
                        if (visibleToEphemeral) {
                            visibility2 = 1;
                        } else if (receiver || !isImplicitlyExposedIntent(intent2)) {
                            visibility2 = i2;
                        } else {
                            visibility2 = i;
                        }
                        intent2.setVisibilityToInstantApp(visibility2);
                        if (intent2.isVisibleToInstantApp()) {
                            a.info.flags |= 1048576;
                        }
                        if (intent2.isImplicitlyVisibleToInstantApp()) {
                            a.info.flags |= 2097152;
                        }
                        resources = res;
                        xmlResourceParser = parser;
                        strArr = strArr;
                        r6 = r6;
                        sa = sa;
                        outerDepth = outerDepth;
                    } else if (!receiver && parser.getName().equals("preferred")) {
                        ActivityIntentInfo intent4 = new ActivityIntentInfo(a);
                        if (!parseIntent(res, parser, false, false, intent4, outError)) {
                            return null;
                        }
                        if (intent4.countActions() == 0) {
                            Slog.w(TAG, "No actions in preferred at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            intent = intent4;
                        } else {
                            if (r6.preferredActivityFilters == null) {
                                r6.preferredActivityFilters = new ArrayList<>();
                            }
                            intent = intent4;
                            r6.preferredActivityFilters.add(intent);
                        }
                        if (visibleToEphemeral) {
                            visibility = 1;
                        } else if (receiver || !isImplicitlyExposedIntent(intent)) {
                            visibility = i2;
                        } else {
                            visibility = i;
                        }
                        intent.setVisibilityToInstantApp(visibility);
                        if (intent.isVisibleToInstantApp()) {
                            c = 0;
                            a.info.flags |= 1048576;
                        } else {
                            c = 0;
                        }
                        if (intent.isImplicitlyVisibleToInstantApp()) {
                            a.info.flags |= 2097152;
                        }
                        resources = res;
                        strArr = strArr;
                        r6 = r6;
                        sa = sa;
                        outerDepth = outerDepth;
                        xmlResourceParser = parser;
                    } else if (parser.getName().equals("meta-data")) {
                        Bundle parseMetaData = parseMetaData(res, parser, a.metaData, strArr);
                        a.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return null;
                        }
                        HwFrameworkFactory.getHwPackageParser().initMetaData(new PackageParserEx.ActivityEx(a));
                        int themeId = HwWidgetFactory.getThemeId(a.metaData, res);
                        if (themeId != 0) {
                            a.info.theme = themeId;
                        }
                        HwThemeManager.addSimpleUIConfig(new PackageParserEx.ActivityEx(a));
                        resources = res;
                        strArr = strArr;
                        r6 = r6;
                        sa = sa;
                        xmlResourceParser = parser;
                        outerDepth = outerDepth;
                    } else if (receiver || !parser.getName().equals(TtmlUtils.TAG_LAYOUT)) {
                        Slog.w(TAG, "Problem in package " + this.mArchiveSourcePath + SettingsStringUtil.DELIMITER);
                        if (receiver) {
                            Slog.w(TAG, "Unknown element under <receiver>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        } else {
                            Slog.w(TAG, "Unknown element under <activity>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        }
                        XmlUtils.skipCurrentTag(parser);
                        resources = res;
                        strArr = strArr;
                        r6 = r6;
                        sa = sa;
                        xmlResourceParser = parser;
                        outerDepth = outerDepth;
                    } else {
                        parseLayout(res, parser, a);
                        resources = res;
                        strArr = strArr;
                        r6 = r6;
                        sa = sa;
                        xmlResourceParser = parser;
                        outerDepth = outerDepth;
                    }
                }
            } else {
                break;
            }
        }
        if (!setExported) {
            a.info.exported = a.intents.size() > 0 ? true : i2;
        }
        return a;
    }

    private void setActivityResizeMode(ActivityInfo aInfo, TypedArray sa, Package owner) {
        boolean appResizeable = true;
        boolean appExplicitDefault = (owner.applicationInfo.privateFlags & 3072) != 0;
        if (sa.hasValue(40) || appExplicitDefault) {
            if ((owner.applicationInfo.privateFlags & 1024) == 0) {
                appResizeable = false;
            }
            if (sa.getBoolean(40, appResizeable)) {
                aInfo.resizeMode = 2;
            } else {
                aInfo.resizeMode = 0;
            }
        } else if ((owner.applicationInfo.privateFlags & 4096) != 0) {
            aInfo.resizeMode = 1;
        } else if (aInfo.isFixedOrientationPortrait()) {
            aInfo.resizeMode = 6;
        } else if (aInfo.isFixedOrientationLandscape()) {
            aInfo.resizeMode = 5;
        } else if (aInfo.isFixedOrientation()) {
            aInfo.resizeMode = 7;
        } else {
            aInfo.resizeMode = 4;
        }
    }

    private void setMaxAspectRatio(Package owner) {
        float activityAspectRatio;
        float maxAspectRatio = owner.applicationInfo.targetSdkVersion < 26 ? mDefaultMaxAspectRatio : 0.0f;
        if (owner.applicationInfo.maxAspectRatio != 0.0f) {
            maxAspectRatio = owner.applicationInfo.maxAspectRatio;
        } else if (owner.mAppMetaData != null && owner.mAppMetaData.containsKey(METADATA_MAX_ASPECT_RATIO)) {
            maxAspectRatio = owner.mAppMetaData.getFloat(METADATA_MAX_ASPECT_RATIO, maxAspectRatio);
        }
        Iterator<Activity> it = owner.activities.iterator();
        while (it.hasNext()) {
            Activity activity = it.next();
            if (HwFrameworkFactory.getHwPackageParser().isDefaultFullScreen(activity.info.packageName)) {
                activity.info.maxAspectRatio = 0.0f;
            } else if (!activity.hasMaxAspectRatio()) {
                if (activity.metaData != null) {
                    activityAspectRatio = activity.metaData.getFloat(METADATA_MAX_ASPECT_RATIO, maxAspectRatio);
                } else {
                    activityAspectRatio = maxAspectRatio;
                }
                activity.setMaxAspectRatio(activityAspectRatio);
            }
        }
    }

    private void setMinAspectRatio(Package owner) {
        float minAspectRatio;
        float f = 0.0f;
        if (HwFoldScreenState.isFoldScreenDevice()) {
            boolean isConfigedInWhitelist = false;
            if (owner.mAppMetaData == null || !owner.mAppMetaData.containsKey(METADATA_MIN_ASPECT_RATIO)) {
                Float defaultAspectInXml = HwFrameworkFactory.getHwPackageParser().getDefaultAspect(owner.applicationInfo.packageName, owner.mVersionCode);
                if (defaultAspectInXml != null) {
                    owner.applicationInfo.minAspectRatio = defaultAspectInXml.floatValue();
                    isConfigedInWhitelist = true;
                } else if (owner.applicationInfo.targetSdkVersion < 28) {
                    owner.applicationInfo.minAspectRatio = HwFoldScreenState.getScreenFoldFullRatio();
                    Iterator<Activity> it = owner.activities.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Activity activity = it.next();
                        if (!PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME.equals(activity.className) && !activity.isResizeable()) {
                            owner.applicationInfo.minAspectRatio = 1.3333334f;
                            break;
                        }
                    }
                } else {
                    owner.applicationInfo.minAspectRatio = 0.0f;
                }
                owner.applicationInfo.hw_extra_flags |= 1024;
            } else {
                owner.applicationInfo.minAspectRatio = Math.max(owner.mAppMetaData.getFloat(METADATA_MIN_ASPECT_RATIO, 0.0f), HwFoldScreenState.getScreenFoldFullRatio());
                owner.applicationInfo.hw_extra_flags &= -1025;
            }
            boolean isAllResizable = true;
            Iterator<Activity> it2 = owner.activities.iterator();
            while (it2.hasNext()) {
                Activity activity2 = it2.next();
                if (activity2.hasMinAspectRatio()) {
                    isAllResizable = false;
                } else if (!PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME.equals(activity2.className)) {
                    if (isAllResizable && !activity2.isResizeable()) {
                        isAllResizable = false;
                    }
                    if (((double) Math.abs(owner.applicationInfo.minAspectRatio - HwFoldScreenState.getScreenFoldFullRatio())) > 1.0E-8d) {
                        activity2.setMinAspectRatio(owner.applicationInfo.minAspectRatio, isConfigedInWhitelist);
                    }
                }
            }
            if (isAllResizable && !isConfigedInWhitelist) {
                owner.applicationInfo.minAspectRatio = 0.0f;
                return;
            }
            return;
        }
        if (owner.applicationInfo.minAspectRatio != 0.0f) {
            minAspectRatio = owner.applicationInfo.minAspectRatio;
        } else {
            if (owner.applicationInfo.targetSdkVersion < 29) {
                Callback callback = this.mCallback;
                if (callback == null || !callback.hasFeature(PackageManager.FEATURE_WATCH)) {
                    f = DEFAULT_PRE_Q_MIN_ASPECT_RATIO;
                } else {
                    f = 1.0f;
                }
            }
            minAspectRatio = f;
        }
        Iterator<Activity> it3 = owner.activities.iterator();
        while (it3.hasNext()) {
            Activity activity3 = it3.next();
            if (!activity3.hasMinAspectRatio()) {
                activity3.setMinAspectRatio(minAspectRatio, false);
            }
        }
    }

    public static int getActivityConfigChanges(int configChanges, int recreateOnConfigChanges) {
        return ((~recreateOnConfigChanges) & 3) | configChanges;
    }

    private void parseLayout(Resources res, AttributeSet attrs, Activity a) {
        TypedArray sw = res.obtainAttributes(attrs, R.styleable.AndroidManifestLayout);
        int width = -1;
        float widthFraction = -1.0f;
        int height = -1;
        float heightFraction = -1.0f;
        int widthType = sw.getType(3);
        if (widthType == 6) {
            widthFraction = sw.getFraction(3, 1, 1, -1.0f);
        } else if (widthType == 5) {
            width = sw.getDimensionPixelSize(3, -1);
        }
        int heightType = sw.getType(4);
        if (heightType == 6) {
            heightFraction = sw.getFraction(4, 1, 1, -1.0f);
        } else if (heightType == 5) {
            height = sw.getDimensionPixelSize(4, -1);
        }
        int gravity = sw.getInt(0, 17);
        int minWidth = sw.getDimensionPixelSize(1, -1);
        int minHeight = sw.getDimensionPixelSize(2, -1);
        sw.recycle();
        a.info.windowLayout = new ActivityInfo.WindowLayout(width, widthFraction, height, heightFraction, gravity, minWidth, minHeight);
    }

    private Activity parseActivityAlias(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs) throws XmlPullParserException, IOException {
        String targetActivity;
        Activity target;
        String targetActivity2;
        boolean z;
        String str;
        ActivityIntentInfo intent;
        int visibility;
        char c;
        Resources resources = res;
        String[] strArr = outError;
        TypedArray sa = resources.obtainAttributes(parser, R.styleable.AndroidManifestActivityAlias);
        String targetActivity3 = sa.getNonConfigurationString(7, 1024);
        if (targetActivity3 == null) {
            strArr[0] = "<activity-alias> does not specify android:targetActivity";
            sa.recycle();
            return null;
        }
        String targetActivity4 = buildClassName(owner.applicationInfo.packageName, targetActivity3, strArr);
        if (targetActivity4 == null) {
            sa.recycle();
            return null;
        }
        if (cachedArgs.mActivityAliasArgs == null) {
            targetActivity = targetActivity4;
            cachedArgs.mActivityAliasArgs = new ParseComponentArgs(owner, outError, 2, 0, 1, 11, 8, 10, this.mSeparateProcesses, 0, 6, 4);
            cachedArgs.mActivityAliasArgs.tag = "<activity-alias>";
        } else {
            targetActivity = targetActivity4;
        }
        cachedArgs.mActivityAliasArgs.sa = sa;
        cachedArgs.mActivityAliasArgs.flags = flags;
        int NA = owner.activities.size();
        int i = 0;
        while (true) {
            if (i >= NA) {
                target = null;
                break;
            }
            Activity t = owner.activities.get(i);
            if (targetActivity.equals(t.info.name)) {
                target = t;
                break;
            }
            i++;
        }
        if (target == null) {
            strArr[0] = "<activity-alias> target activity " + targetActivity + " not found in manifest";
            sa.recycle();
            return null;
        }
        ActivityInfo info = new ActivityInfo();
        info.targetActivity = targetActivity;
        info.configChanges = target.info.configChanges;
        info.flags = target.info.flags;
        info.privateFlags = target.info.privateFlags;
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
        info.maxAspectRatio = target.info.maxAspectRatio;
        info.minAspectRatio = target.info.minAspectRatio;
        info.requestedVrComponent = target.info.requestedVrComponent;
        boolean z2 = target.info.directBootAware;
        info.directBootAware = z2;
        info.encryptionAware = z2;
        Activity a = new Activity(cachedArgs.mActivityAliasArgs, info);
        if (strArr[0] != null) {
            sa.recycle();
            return null;
        }
        boolean setExported = sa.hasValue(5);
        if (setExported) {
            a.info.exported = sa.getBoolean(5, false);
        }
        String str2 = sa.getNonConfigurationString(3, 0);
        if (str2 != null) {
            a.info.permission = str2.length() > 0 ? str2.toString().intern() : null;
        }
        String parentName = sa.getNonConfigurationString(9, 1024);
        String str3 = TAG;
        if (parentName != null) {
            String parentClassName = buildClassName(a.info.packageName, parentName, strArr);
            if (strArr[0] == null) {
                a.info.parentActivityName = parentClassName;
                targetActivity2 = targetActivity;
            } else {
                StringBuilder sb = new StringBuilder();
                targetActivity2 = targetActivity;
                sb.append("Activity alias ");
                sb.append(a.info.name);
                sb.append(" specified invalid parentActivityName ");
                sb.append(parentName);
                Log.e(str3, sb.toString());
                strArr[0] = null;
            }
        } else {
            targetActivity2 = targetActivity;
        }
        boolean z3 = true;
        boolean visibleToEphemeral = (a.info.flags & 1048576) != 0;
        sa.recycle();
        if (strArr[0] != null) {
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != z3) {
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    z = true;
                    break;
                } else if (type == 3) {
                    targetActivity2 = targetActivity2;
                    z3 = true;
                    outerDepth = outerDepth;
                    resources = resources;
                    strArr = strArr;
                } else if (type == 4) {
                    z3 = true;
                } else if (parser.getName().equals("intent-filter")) {
                    ActivityIntentInfo intent2 = new ActivityIntentInfo(a);
                    if (!parseIntent(res, parser, true, true, intent2, outError)) {
                        return null;
                    }
                    if (intent2.countActions() == 0) {
                        str = str3;
                        Slog.w(str, "No actions in intent filter at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        intent = intent2;
                    } else {
                        str = str3;
                        a.order = Math.max(intent2.getOrder(), a.order);
                        intent = intent2;
                        a.intents.add(intent);
                    }
                    if (visibleToEphemeral) {
                        visibility = 1;
                    } else if (isImplicitlyExposedIntent(intent)) {
                        visibility = 2;
                    } else {
                        visibility = 0;
                    }
                    intent.setVisibilityToInstantApp(visibility);
                    if (intent.isVisibleToInstantApp()) {
                        c = 0;
                        a.info.flags |= 1048576;
                    } else {
                        c = 0;
                    }
                    if (intent.isImplicitlyVisibleToInstantApp()) {
                        a.info.flags |= 2097152;
                    }
                    str3 = str;
                    strArr = strArr;
                    targetActivity2 = targetActivity2;
                    z3 = true;
                    sa = sa;
                    outerDepth = outerDepth;
                    resources = res;
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, a.metaData, strArr);
                    a.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return null;
                    }
                    HwThemeManager.addSimpleUIConfig(new PackageParserEx.ActivityEx(a));
                    str3 = str3;
                    targetActivity2 = targetActivity2;
                    z3 = true;
                    sa = sa;
                    outerDepth = outerDepth;
                    resources = res;
                    strArr = strArr;
                } else {
                    Slog.w(str3, "Unknown element under <activity-alias>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                    str3 = str3;
                    targetActivity2 = targetActivity2;
                    z3 = true;
                    sa = sa;
                    outerDepth = outerDepth;
                    resources = res;
                    strArr = strArr;
                }
            } else {
                z = z3;
                break;
            }
        }
        if (!setExported) {
            a.info.exported = a.intents.size() > 0 ? z : false;
        }
        return a;
    }

    private Provider parseProvider(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs) throws XmlPullParserException, IOException {
        TypedArray sa;
        boolean providerExportedDefault;
        String str;
        TypedArray sa2 = res.obtainAttributes(parser, R.styleable.AndroidManifestProvider);
        if (cachedArgs.mProviderArgs == null) {
            sa = sa2;
            cachedArgs.mProviderArgs = new ParseComponentArgs(owner, outError, 2, 0, 1, 19, 15, 17, this.mSeparateProcesses, 8, 14, 6);
            cachedArgs.mProviderArgs.tag = "<provider>";
        } else {
            sa = sa2;
        }
        cachedArgs.mProviderArgs.sa = sa;
        cachedArgs.mProviderArgs.flags = flags;
        Provider p = new Provider(cachedArgs.mProviderArgs, new ProviderInfo());
        if (outError[0] != null) {
            sa.recycle();
            return null;
        }
        if (owner.applicationInfo.targetSdkVersion < 17) {
            providerExportedDefault = true;
        } else {
            providerExportedDefault = false;
        }
        p.info.exported = sa.getBoolean(7, providerExportedDefault);
        String cpname = sa.getNonConfigurationString(10, 0);
        p.info.isSyncable = sa.getBoolean(11, false);
        String permission = sa.getNonConfigurationString(3, 0);
        String str2 = sa.getNonConfigurationString(4, 0);
        if (str2 == null) {
            str2 = permission;
        }
        if (str2 == null) {
            p.info.readPermission = owner.applicationInfo.permission;
        } else {
            p.info.readPermission = str2.length() > 0 ? str2.toString().intern() : null;
        }
        String str3 = sa.getNonConfigurationString(5, 0);
        if (str3 == null) {
            str = permission;
        } else {
            str = str3;
        }
        if (str == null) {
            p.info.writePermission = owner.applicationInfo.permission;
        } else {
            p.info.writePermission = str.length() > 0 ? str.toString().intern() : null;
        }
        p.info.grantUriPermissions = sa.getBoolean(13, false);
        p.info.forceUriPermissions = sa.getBoolean(22, false);
        p.info.multiprocess = sa.getBoolean(9, false);
        p.info.initOrder = sa.getInt(12, 0);
        p.info.splitName = sa.getNonConfigurationString(21, 0);
        p.info.flags = 0;
        if (sa.getBoolean(16, false)) {
            p.info.flags |= 1073741824;
        }
        ProviderInfo providerInfo = p.info;
        ProviderInfo providerInfo2 = p.info;
        boolean z = sa.getBoolean(18, false);
        providerInfo2.directBootAware = z;
        providerInfo.encryptionAware = z;
        if (p.info.directBootAware) {
            owner.applicationInfo.privateFlags |= 256;
        }
        boolean visibleToEphemeral = sa.getBoolean(20, false);
        if (visibleToEphemeral) {
            p.info.flags |= 1048576;
            owner.visibleToInstantApps = true;
        }
        sa.recycle();
        if ((owner.applicationInfo.privateFlags & 2) != 0 && p.info.processName == owner.packageName) {
            outError[0] = "Heavy-weight applications can not have providers in main process";
            return null;
        } else if (cpname == null) {
            outError[0] = "<provider> does not include authorities attribute";
            return null;
        } else if (cpname.length() <= 0) {
            outError[0] = "<provider> has empty authorities attribute";
            return null;
        } else {
            p.info.authority = cpname.intern();
            if (!parseProviderTags(res, parser, visibleToEphemeral, p, outError)) {
                return null;
            }
            return p;
        }
    }

    private boolean parseProviderTags(Resources res, XmlResourceParser parser, boolean visibleToEphemeral, Provider outInfo, String[] outError) throws XmlPullParserException, IOException {
        String readPermission;
        String readPermission2;
        String writePermission;
        PathPermission pa;
        PathPermission pa2;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return true;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("intent-filter")) {
                    ProviderIntentInfo intent = new ProviderIntentInfo(outInfo);
                    if (!parseIntent(res, parser, true, false, intent, outError)) {
                        return false;
                    }
                    if (visibleToEphemeral) {
                        intent.setVisibilityToInstantApp(1);
                        outInfo.info.flags |= 1048576;
                    }
                    outInfo.order = Math.max(intent.getOrder(), outInfo.order);
                    outInfo.intents.add(intent);
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, outInfo.metaData, outError);
                    outInfo.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return false;
                    }
                } else if (parser.getName().equals("grant-uri-permission")) {
                    TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestGrantUriPermission);
                    PatternMatcher pa3 = null;
                    String str = sa.getNonConfigurationString(0, 0);
                    if (str != null) {
                        pa3 = new PatternMatcher(str, 0);
                    }
                    String str2 = sa.getNonConfigurationString(1, 0);
                    if (str2 != null) {
                        pa3 = new PatternMatcher(str2, 1);
                    }
                    String str3 = sa.getNonConfigurationString(2, 0);
                    if (str3 != null) {
                        pa3 = new PatternMatcher(str3, 2);
                    }
                    sa.recycle();
                    if (pa3 != null) {
                        if (outInfo.info.uriPermissionPatterns == null) {
                            outInfo.info.uriPermissionPatterns = new PatternMatcher[1];
                            outInfo.info.uriPermissionPatterns[0] = pa3;
                        } else {
                            int N = outInfo.info.uriPermissionPatterns.length;
                            PatternMatcher[] newp = new PatternMatcher[(N + 1)];
                            System.arraycopy(outInfo.info.uriPermissionPatterns, 0, newp, 0, N);
                            newp[N] = pa3;
                            outInfo.info.uriPermissionPatterns = newp;
                        }
                        outInfo.info.grantUriPermissions = true;
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        Slog.w(TAG, "Unknown element under <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                } else if (parser.getName().equals("path-permission")) {
                    TypedArray sa2 = res.obtainAttributes(parser, R.styleable.AndroidManifestPathPermission);
                    String permission = sa2.getNonConfigurationString(0, 0);
                    String readPermission3 = sa2.getNonConfigurationString(1, 0);
                    if (readPermission3 == null) {
                        readPermission = permission;
                    } else {
                        readPermission = readPermission3;
                    }
                    String writePermission2 = sa2.getNonConfigurationString(2, 0);
                    if (writePermission2 == null) {
                        writePermission2 = permission;
                    }
                    boolean havePerm = false;
                    if (readPermission != null) {
                        havePerm = true;
                        readPermission2 = readPermission.intern();
                    } else {
                        readPermission2 = readPermission;
                    }
                    if (writePermission2 != null) {
                        havePerm = true;
                        writePermission = writePermission2.intern();
                    } else {
                        writePermission = writePermission2;
                    }
                    if (!havePerm) {
                        Slog.w(TAG, "No readPermission or writePermssion for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        String path = sa2.getNonConfigurationString(3, 0);
                        if (path != null) {
                            pa = new PathPermission(path, 0, readPermission2, writePermission);
                        } else {
                            pa = null;
                        }
                        String path2 = sa2.getNonConfigurationString(4, 0);
                        if (path2 != null) {
                            pa2 = new PathPermission(path2, 1, readPermission2, writePermission);
                        } else {
                            pa2 = pa;
                        }
                        String path3 = sa2.getNonConfigurationString(5, 0);
                        if (path3 != null) {
                            pa2 = new PathPermission(path3, 2, readPermission2, writePermission);
                        }
                        PathPermission pa4 = pa2;
                        String path4 = sa2.getNonConfigurationString(6, 0);
                        if (path4 != null) {
                            pa4 = new PathPermission(path4, 3, readPermission2, writePermission);
                        }
                        sa2.recycle();
                        if (pa4 != null) {
                            if (outInfo.info.pathPermissions == null) {
                                outInfo.info.pathPermissions = new PathPermission[1];
                                outInfo.info.pathPermissions[0] = pa4;
                            } else {
                                int N2 = outInfo.info.pathPermissions.length;
                                PathPermission[] newp2 = new PathPermission[(N2 + 1)];
                                System.arraycopy(outInfo.info.pathPermissions, 0, newp2, 0, N2);
                                newp2[N2] = pa4;
                                outInfo.info.pathPermissions = newp2;
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "No path, pathPrefix, or pathPattern for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                } else {
                    Slog.w(TAG, "Unknown element under <provider>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private Service parseService(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs) throws XmlPullParserException, IOException {
        boolean z;
        char c;
        boolean z2;
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        String[] strArr = outError;
        TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestService);
        if (cachedArgs.mServiceArgs == null) {
            cachedArgs.mServiceArgs = new ParseComponentArgs(owner, outError, 2, 0, 1, 15, 8, 12, this.mSeparateProcesses, 6, 7, 4);
            cachedArgs.mServiceArgs.tag = "<service>";
        }
        cachedArgs.mServiceArgs.sa = sa;
        cachedArgs.mServiceArgs.flags = flags;
        Service s = new Service(cachedArgs.mServiceArgs, new ServiceInfo());
        if (strArr[0] != null) {
            sa.recycle();
            return null;
        }
        boolean setExported = sa.hasValue(5);
        if (setExported) {
            s.info.exported = sa.getBoolean(5, false);
        }
        String str = sa.getNonConfigurationString(3, 0);
        if (str == null) {
            s.info.permission = owner.applicationInfo.permission;
        } else {
            s.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        s.info.splitName = sa.getNonConfigurationString(17, 0);
        s.info.mForegroundServiceType = sa.getInt(19, 0);
        s.info.flags = 0;
        boolean z3 = true;
        if (sa.getBoolean(9, false)) {
            s.info.flags |= 1;
        }
        if (sa.getBoolean(10, false)) {
            s.info.flags |= 2;
        }
        if (sa.getBoolean(14, false)) {
            s.info.flags |= 4;
        }
        if (sa.getBoolean(18, false)) {
            s.info.flags |= 8;
        }
        if (sa.getBoolean(11, false)) {
            s.info.flags |= 1073741824;
        }
        ServiceInfo serviceInfo = s.info;
        ServiceInfo serviceInfo2 = s.info;
        boolean z4 = sa.getBoolean(13, false);
        serviceInfo2.directBootAware = z4;
        serviceInfo.encryptionAware = z4;
        if (s.info.directBootAware) {
            owner.applicationInfo.privateFlags |= 256;
        }
        boolean visibleToEphemeral = sa.getBoolean(16, false);
        if (visibleToEphemeral) {
            s.info.flags |= 1048576;
            owner.visibleToInstantApps = true;
        }
        sa.recycle();
        if ((owner.applicationInfo.privateFlags & 2) != 0) {
            if (s.info.processName == owner.packageName) {
                strArr[0] = "Heavy-weight applications can not have services in main process";
                return null;
            }
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != z3) {
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    z = true;
                    break;
                } else if (type == 3) {
                    strArr = strArr;
                    sa = sa;
                    z3 = true;
                    outerDepth = outerDepth;
                } else if (type == 4) {
                    z3 = true;
                } else if (parser.getName().equals("intent-filter")) {
                    ServiceIntentInfo intent = new ServiceIntentInfo(s);
                    if (!parseIntent(res, parser, true, false, intent, outError)) {
                        return null;
                    }
                    if (visibleToEphemeral) {
                        z2 = true;
                        intent.setVisibilityToInstantApp(1);
                        c = 0;
                        s.info.flags |= 1048576;
                    } else {
                        z2 = true;
                        c = 0;
                    }
                    s.order = Math.max(intent.getOrder(), s.order);
                    s.intents.add(intent);
                    resources = res;
                    strArr = strArr;
                    xmlResourceParser = xmlResourceParser;
                    sa = sa;
                    z3 = z2;
                    outerDepth = outerDepth;
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, xmlResourceParser, s.metaData, strArr);
                    s.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return null;
                    }
                    resources = res;
                    strArr = strArr;
                    xmlResourceParser = xmlResourceParser;
                    sa = sa;
                    z3 = true;
                    outerDepth = outerDepth;
                } else {
                    Slog.w(TAG, "Unknown element under <service>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                    resources = res;
                    strArr = strArr;
                    xmlResourceParser = xmlResourceParser;
                    sa = sa;
                    z3 = true;
                    outerDepth = outerDepth;
                }
            } else {
                z = z3;
                break;
            }
        }
        if (!setExported) {
            ServiceInfo serviceInfo3 = s.info;
            if (s.intents.size() <= 0) {
                z = false;
            }
            serviceInfo3.exported = z;
        }
        return s;
    }

    private boolean isImplicitlyExposedIntent(IntentInfo intent) {
        return intent.hasCategory(Intent.CATEGORY_BROWSABLE) || intent.hasAction(Intent.ACTION_SEND) || intent.hasAction(Intent.ACTION_SENDTO) || intent.hasAction(Intent.ACTION_SEND_MULTIPLE);
    }

    private boolean parseAllMetaData(Resources res, XmlResourceParser parser, String tag, Component<?> outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, outInfo.metaData, outError);
                    outInfo.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return false;
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
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestMetaData);
        if (data == null) {
            data = new Bundle();
        }
        boolean z = false;
        String name = sa.getNonConfigurationString(0, 0);
        String str = null;
        if (name == null) {
            outError[0] = "<meta-data> requires an android:name attribute";
            sa.recycle();
            return null;
        }
        String name2 = name.intern();
        TypedValue v = sa.peekValue(2);
        if (v == null || v.resourceId == 0) {
            TypedValue v2 = sa.peekValue(1);
            if (v2 == null) {
                outError[0] = "<meta-data> requires an android:value or android:resource attribute";
                data = null;
            } else if (v2.type == 3) {
                CharSequence cs = v2.coerceToString();
                if (cs != null) {
                    str = cs.toString();
                }
                data.putString(name2, str);
            } else if (v2.type == 18) {
                if (v2.data != 0) {
                    z = true;
                }
                data.putBoolean(name2, z);
            } else if (v2.type >= 16 && v2.type <= 31) {
                data.putInt(name2, v2.data);
            } else if (v2.type == 4) {
                data.putFloat(name2, v2.getFloat());
            } else {
                Slog.w(TAG, "<meta-data> only supports string, integer, float, color, boolean, and resource reference types: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
            }
        } else {
            data.putInt(name2, v.resourceId);
        }
        sa.recycle();
        XmlUtils.skipCurrentTag(parser);
        return data;
    }

    private static VerifierInfo parseVerifier(AttributeSet attrs) {
        String packageName = null;
        String encodedPublicKey = null;
        int attrCount = attrs.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            int attrResId = attrs.getAttributeNameResource(i);
            if (attrResId == 16842755) {
                packageName = attrs.getAttributeValue(i);
            } else if (attrResId == 16843686) {
                encodedPublicKey = attrs.getAttributeValue(i);
            }
        }
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
            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(encodedPublicKey, 0));
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

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b0, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00cf, code lost:
        return false;
     */
    private boolean parseIntent(Resources res, XmlResourceParser parser, boolean allowGlobs, boolean allowAutoVerify, IntentInfo outInfo, String[] outError) throws XmlPullParserException, IOException {
        int roundIconVal;
        int i;
        int i2;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestIntentFilter);
        outInfo.setPriority(sa.getInt(2, 0));
        int i3 = 3;
        outInfo.setOrder(sa.getInt(3, 0));
        TypedValue v = sa.peekValue(0);
        if (v != null) {
            int i4 = v.resourceId;
            outInfo.labelRes = i4;
            if (i4 == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        if (sUseRoundIcon) {
            roundIconVal = sa.getResourceId(7, 0);
        } else {
            roundIconVal = 0;
        }
        int i5 = 1;
        if (roundIconVal != 0) {
            outInfo.icon = roundIconVal;
        } else {
            outInfo.icon = sa.getResourceId(1, 0);
        }
        int i6 = 4;
        outInfo.logo = sa.getResourceId(4, 0);
        outInfo.banner = sa.getResourceId(5, 0);
        if (allowAutoVerify) {
            outInfo.setAutoVerify(sa.getBoolean(6, false));
        }
        sa.recycle();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != i5) {
                if (type == i3 && parser.getDepth() <= outerDepth) {
                    break;
                } else if (type == i3 || type == i6) {
                    i3 = i3;
                    i5 = 1;
                    i6 = 4;
                } else {
                    String nodeName = parser.getName();
                    if (nodeName.equals("action")) {
                        String value = parser.getAttributeValue(ANDROID_RESOURCES, "name");
                        if (value == null || value == "") {
                            outError[0] = "No value supplied for <android:name>";
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                            outInfo.addAction(value);
                            i = 3;
                        }
                    } else if (nodeName.equals("category")) {
                        String value2 = parser.getAttributeValue(ANDROID_RESOURCES, "name");
                        if (value2 == null || value2 == "") {
                            outError[0] = "No value supplied for <android:name>";
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                            outInfo.addCategory(value2);
                            i = 3;
                        }
                    } else if (nodeName.equals("data")) {
                        TypedArray sa2 = res.obtainAttributes(parser, R.styleable.AndroidManifestData);
                        String str = sa2.getNonConfigurationString(0, 0);
                        if (str != null) {
                            try {
                                outInfo.addDataType(str);
                            } catch (IntentFilter.MalformedMimeTypeException e) {
                                outError[0] = e.toString();
                                sa2.recycle();
                                return false;
                            }
                        }
                        String str2 = sa2.getNonConfigurationString(1, 0);
                        if (str2 != null) {
                            outInfo.addDataScheme(str2);
                        }
                        String str3 = sa2.getNonConfigurationString(7, 0);
                        if (str3 != null) {
                            outInfo.addDataSchemeSpecificPart(str3, 0);
                        }
                        String str4 = sa2.getNonConfigurationString(8, 0);
                        if (str4 != null) {
                            outInfo.addDataSchemeSpecificPart(str4, 1);
                        }
                        String str5 = sa2.getNonConfigurationString(9, 0);
                        if (str5 == null) {
                            i2 = 2;
                        } else if (!allowGlobs) {
                            outError[0] = "sspPattern not allowed here; ssp must be literal";
                            return false;
                        } else {
                            i2 = 2;
                            outInfo.addDataSchemeSpecificPart(str5, 2);
                        }
                        String host = sa2.getNonConfigurationString(i2, 0);
                        String port = sa2.getNonConfigurationString(3, 0);
                        if (host != null) {
                            outInfo.addDataAuthority(host, port);
                        }
                        String str6 = sa2.getNonConfigurationString(4, 0);
                        if (str6 != null) {
                            outInfo.addDataPath(str6, 0);
                        }
                        String str7 = sa2.getNonConfigurationString(5, 0);
                        if (str7 != null) {
                            outInfo.addDataPath(str7, 1);
                        }
                        String str8 = sa2.getNonConfigurationString(6, 0);
                        if (str8 != null) {
                            if (!allowGlobs) {
                                outError[0] = "pathPattern not allowed here; path must be literal";
                                return false;
                            }
                            outInfo.addDataPath(str8, 2);
                        }
                        String str9 = sa2.getNonConfigurationString(10, 0);
                        if (str9 == null) {
                            i = 3;
                        } else if (!allowGlobs) {
                            outError[0] = "pathAdvancedPattern not allowed here; path must be literal";
                            return false;
                        } else {
                            i = 3;
                            outInfo.addDataPath(str9, 3);
                        }
                        sa2.recycle();
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        i = 3;
                        if (nodeName.equals("state")) {
                            HwFrameworkFactory.getHwPackageParser().parseIntentFilterState(parser, ANDROID_RESOURCES, outInfo);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <intent-filter>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                    i3 = i;
                    i5 = 1;
                    i6 = 4;
                }
            } else {
                break;
            }
        }
        outInfo.hasDefault = outInfo.hasCategory(Intent.CATEGORY_DEFAULT);
        return true;
    }

    public static final class SigningDetails implements Parcelable {
        public static final Parcelable.Creator<SigningDetails> CREATOR = new Parcelable.Creator<SigningDetails>() {
            /* class android.content.pm.PackageParser.SigningDetails.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SigningDetails createFromParcel(Parcel source) {
                if (source.readBoolean()) {
                    return SigningDetails.UNKNOWN;
                }
                return new SigningDetails(source);
            }

            @Override // android.os.Parcelable.Creator
            public SigningDetails[] newArray(int size) {
                return new SigningDetails[size];
            }
        };
        private static final int PAST_CERT_EXISTS = 0;
        public static final SigningDetails UNKNOWN = new SigningDetails(null, 0, null, null);
        public final Signature[] pastSigningCertificates;
        public final ArraySet<PublicKey> publicKeys;
        @SignatureSchemeVersion
        public final int signatureSchemeVersion;
        @UnsupportedAppUsage
        public final Signature[] signatures;

        public @interface CertCapabilities {
            public static final int AUTH = 16;
            public static final int INSTALLED_DATA = 1;
            public static final int PERMISSION = 4;
            public static final int ROLLBACK = 8;
            public static final int SHARED_USER_ID = 2;
        }

        public @interface SignatureSchemeVersion {
            public static final int JAR = 1;
            public static final int SIGNING_BLOCK_V2 = 2;
            public static final int SIGNING_BLOCK_V3 = 3;
            public static final int UNKNOWN = 0;
        }

        @VisibleForTesting
        public SigningDetails(Signature[] signatures2, @SignatureSchemeVersion int signatureSchemeVersion2, ArraySet<PublicKey> keys, Signature[] pastSigningCertificates2) {
            this.signatures = signatures2;
            this.signatureSchemeVersion = signatureSchemeVersion2;
            this.publicKeys = keys;
            this.pastSigningCertificates = pastSigningCertificates2;
        }

        public SigningDetails(Signature[] signatures2, @SignatureSchemeVersion int signatureSchemeVersion2, Signature[] pastSigningCertificates2) throws CertificateException {
            this(signatures2, signatureSchemeVersion2, PackageParser.toSigningKeys(signatures2), pastSigningCertificates2);
        }

        public SigningDetails(Signature[] signatures2, @SignatureSchemeVersion int signatureSchemeVersion2) throws CertificateException {
            this(signatures2, signatureSchemeVersion2, null);
        }

        public SigningDetails(SigningDetails orig) {
            if (orig != null) {
                Signature[] signatureArr = orig.signatures;
                if (signatureArr != null) {
                    this.signatures = (Signature[]) signatureArr.clone();
                } else {
                    this.signatures = null;
                }
                this.signatureSchemeVersion = orig.signatureSchemeVersion;
                this.publicKeys = new ArraySet<>(orig.publicKeys);
                Signature[] signatureArr2 = orig.pastSigningCertificates;
                if (signatureArr2 != null) {
                    this.pastSigningCertificates = (Signature[]) signatureArr2.clone();
                } else {
                    this.pastSigningCertificates = null;
                }
            } else {
                this.signatures = null;
                this.signatureSchemeVersion = 0;
                this.publicKeys = null;
                this.pastSigningCertificates = null;
            }
        }

        public boolean hasSignatures() {
            Signature[] signatureArr = this.signatures;
            return signatureArr != null && signatureArr.length > 0;
        }

        public boolean hasPastSigningCertificates() {
            Signature[] signatureArr = this.pastSigningCertificates;
            return signatureArr != null && signatureArr.length > 0;
        }

        public boolean hasAncestorOrSelf(SigningDetails oldDetails) {
            SigningDetails signingDetails = UNKNOWN;
            if (this == signingDetails || oldDetails == signingDetails) {
                return false;
            }
            Signature[] signatureArr = oldDetails.signatures;
            if (signatureArr.length > 1) {
                return signaturesMatchExactly(oldDetails);
            }
            return hasCertificate(signatureArr[0]);
        }

        public boolean hasAncestor(SigningDetails oldDetails) {
            SigningDetails signingDetails = UNKNOWN;
            if (this != signingDetails && oldDetails != signingDetails && hasPastSigningCertificates() && oldDetails.signatures.length == 1) {
                int i = 0;
                while (true) {
                    Signature[] signatureArr = this.pastSigningCertificates;
                    if (i >= signatureArr.length - 1) {
                        break;
                    } else if (signatureArr[i].equals(oldDetails.signatures[i])) {
                        return true;
                    } else {
                        i++;
                    }
                }
            }
            return false;
        }

        public boolean checkCapability(SigningDetails oldDetails, @CertCapabilities int flags) {
            SigningDetails signingDetails = UNKNOWN;
            if (this == signingDetails || oldDetails == signingDetails) {
                return false;
            }
            Signature[] signatureArr = oldDetails.signatures;
            if (signatureArr.length > 1) {
                return signaturesMatchExactly(oldDetails);
            }
            return hasCertificate(signatureArr[0], flags);
        }

        public boolean checkCapabilityRecover(SigningDetails oldDetails, @CertCapabilities int flags) throws CertificateException {
            SigningDetails signingDetails = UNKNOWN;
            if (oldDetails == signingDetails || this == signingDetails) {
                return false;
            }
            if (!hasPastSigningCertificates() || oldDetails.signatures.length != 1) {
                return Signature.areEffectiveMatch(oldDetails.signatures, this.signatures);
            }
            int i = 0;
            while (true) {
                Signature[] signatureArr = this.pastSigningCertificates;
                if (i >= signatureArr.length) {
                    return false;
                }
                if (Signature.areEffectiveMatch(oldDetails.signatures[0], signatureArr[i]) && this.pastSigningCertificates[i].getFlags() == flags) {
                    return true;
                }
                i++;
            }
        }

        public boolean hasCertificate(Signature signature) {
            return hasCertificateInternal(signature, 0);
        }

        public boolean hasCertificate(Signature signature, @CertCapabilities int flags) {
            return hasCertificateInternal(signature, flags);
        }

        public boolean hasCertificate(byte[] certificate) {
            return hasCertificate(new Signature(certificate));
        }

        private boolean hasCertificateInternal(Signature signature, int flags) {
            if (this == UNKNOWN) {
                return false;
            }
            if (hasPastSigningCertificates()) {
                int i = 0;
                while (true) {
                    Signature[] signatureArr = this.pastSigningCertificates;
                    if (i >= signatureArr.length - 1) {
                        break;
                    } else if (!signatureArr[i].equals(signature) || !(flags == 0 || (this.pastSigningCertificates[i].getFlags() & flags) == flags)) {
                        i++;
                    }
                }
                return true;
            }
            Signature[] signatureArr2 = this.signatures;
            if (signatureArr2.length != 1 || !signatureArr2[0].equals(signature)) {
                return false;
            }
            return true;
        }

        public boolean checkCapability(String sha256String, @CertCapabilities int flags) {
            if (this == UNKNOWN) {
                return false;
            }
            if (hasSha256Certificate(ByteStringUtils.fromHexToByteArray(sha256String), flags)) {
                return true;
            }
            return PackageUtils.computeSignaturesSha256Digest(PackageUtils.computeSignaturesSha256Digests(this.signatures)).equals(sha256String);
        }

        public boolean hasSha256Certificate(byte[] sha256Certificate) {
            return hasSha256CertificateInternal(sha256Certificate, 0);
        }

        public boolean hasSha256Certificate(byte[] sha256Certificate, @CertCapabilities int flags) {
            return hasSha256CertificateInternal(sha256Certificate, flags);
        }

        private boolean hasSha256CertificateInternal(byte[] sha256Certificate, int flags) {
            if (this == UNKNOWN) {
                return false;
            }
            if (hasPastSigningCertificates()) {
                int i = 0;
                while (true) {
                    Signature[] signatureArr = this.pastSigningCertificates;
                    if (i >= signatureArr.length - 1) {
                        break;
                    } else if (!Arrays.equals(sha256Certificate, PackageUtils.computeSha256DigestBytes(signatureArr[i].toByteArray())) || !(flags == 0 || (this.pastSigningCertificates[i].getFlags() & flags) == flags)) {
                        i++;
                    }
                }
                return true;
            }
            Signature[] signatureArr2 = this.signatures;
            if (signatureArr2.length == 1) {
                return Arrays.equals(sha256Certificate, PackageUtils.computeSha256DigestBytes(signatureArr2[0].toByteArray()));
            }
            return false;
        }

        public boolean signaturesMatchExactly(SigningDetails other) {
            return Signature.areExactMatch(this.signatures, other.signatures);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            boolean isUnknown = UNKNOWN == this;
            dest.writeBoolean(isUnknown);
            if (!isUnknown) {
                dest.writeTypedArray(this.signatures, flags);
                dest.writeInt(this.signatureSchemeVersion);
                dest.writeArraySet(this.publicKeys);
                dest.writeTypedArray(this.pastSigningCertificates, flags);
            }
        }

        /* JADX DEBUG: Type inference failed for r1v4. Raw type applied. Possible types: android.util.ArraySet<? extends java.lang.Object>, android.util.ArraySet<java.security.PublicKey> */
        protected SigningDetails(Parcel in) {
            ClassLoader boot = Object.class.getClassLoader();
            this.signatures = (Signature[]) in.createTypedArray(Signature.CREATOR);
            this.signatureSchemeVersion = in.readInt();
            this.publicKeys = in.readArraySet(boot);
            this.pastSigningCertificates = (Signature[]) in.createTypedArray(Signature.CREATOR);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SigningDetails)) {
                return false;
            }
            SigningDetails that = (SigningDetails) o;
            if (this.signatureSchemeVersion != that.signatureSchemeVersion || !Signature.areExactMatch(this.signatures, that.signatures)) {
                return false;
            }
            ArraySet<PublicKey> arraySet = this.publicKeys;
            if (arraySet != null) {
                if (!arraySet.equals(that.publicKeys)) {
                    return false;
                }
            } else if (that.publicKeys != null) {
                return false;
            }
            if (!Arrays.equals(this.pastSigningCertificates, that.pastSigningCertificates)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int result = ((Arrays.hashCode(this.signatures) * 31) + this.signatureSchemeVersion) * 31;
            ArraySet<PublicKey> arraySet = this.publicKeys;
            return ((result + (arraySet != null ? arraySet.hashCode() : 0)) * 31) + Arrays.hashCode(this.pastSigningCertificates);
        }

        public static class Builder {
            private Signature[] mPastSigningCertificates;
            private int mSignatureSchemeVersion = 0;
            private Signature[] mSignatures;

            @UnsupportedAppUsage
            public Builder setSignatures(Signature[] signatures) {
                this.mSignatures = signatures;
                return this;
            }

            @UnsupportedAppUsage
            public Builder setSignatureSchemeVersion(int signatureSchemeVersion) {
                this.mSignatureSchemeVersion = signatureSchemeVersion;
                return this;
            }

            @UnsupportedAppUsage
            public Builder setPastSigningCertificates(Signature[] pastSigningCertificates) {
                this.mPastSigningCertificates = pastSigningCertificates;
                return this;
            }

            private void checkInvariants() {
                if (this.mSignatures == null) {
                    throw new IllegalStateException("SigningDetails requires the current signing certificates.");
                }
            }

            @UnsupportedAppUsage
            public SigningDetails build() throws CertificateException {
                checkInvariants();
                return new SigningDetails(this.mSignatures, this.mSignatureSchemeVersion, this.mPastSigningCertificates);
            }
        }
    }

    public static final class Package implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Package>() {
            /* class android.content.pm.PackageParser.Package.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Package createFromParcel(Parcel in) {
                return new Package(in);
            }

            @Override // android.os.Parcelable.Creator
            public Package[] newArray(int size) {
                return new Package[size];
            }
        };
        @UnsupportedAppUsage
        public final ArrayList<Activity> activities;
        @UnsupportedAppUsage
        public ApplicationInfo applicationInfo;
        public String baseCodePath;
        public boolean baseHardwareAccelerated;
        public int baseRevisionCode;
        public ArrayList<Package> childPackages;
        public String codePath;
        @UnsupportedAppUsage
        public ArrayList<ConfigurationInfo> configPreferences;
        public boolean coreApp;
        public String cpuAbiOverride;
        public ArrayList<FeatureGroupInfo> featureGroups;
        public boolean hasPlugin;
        public final ArrayList<String> implicitPermissions;
        @UnsupportedAppUsage
        public int installLocation;
        @UnsupportedAppUsage
        public final ArrayList<Instrumentation> instrumentation;
        public boolean isStub;
        public ArrayList<String> libraryNames;
        public ArrayList<String> mAdoptPermissions;
        @UnsupportedAppUsage
        public Bundle mAppMetaData;
        public int mCompileSdkVersion;
        public String mCompileSdkVersionCodename;
        @UnsupportedAppUsage
        public Object mExtras;
        @UnsupportedAppUsage
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;
        public long[] mLastPackageUsageTimeInMills;
        public ArrayList<String> mOriginalPackages;
        public String mOverlayCategory;
        public boolean mOverlayIsStatic;
        public int mOverlayPriority;
        public String mOverlayTarget;
        public String mOverlayTargetName;
        public boolean mPersistentApp;
        @UnsupportedAppUsage
        public int mPreferredOrder;
        public String mRealPackage;
        public SigningDetails mRealSigningDetails;
        public String mRequiredAccountType;
        public boolean mRequiredForAllUsers;
        public String mRestrictedAccountType;
        @UnsupportedAppUsage
        public String mSharedUserId;
        @UnsupportedAppUsage
        public int mSharedUserLabel;
        @UnsupportedAppUsage
        public SigningDetails mSigningDetails;
        @UnsupportedAppUsage
        public ArraySet<String> mUpgradeKeySets;
        @UnsupportedAppUsage
        public int mVersionCode;
        public int mVersionCodeMajor;
        @UnsupportedAppUsage
        public String mVersionName;
        public String manifestPackageName;
        @UnsupportedAppUsage
        public String packageName;
        public Package parentPackage;
        @UnsupportedAppUsage
        public final ArrayList<PermissionGroup> permissionGroups;
        @UnsupportedAppUsage
        public final ArrayList<Permission> permissions;
        public ArrayList<ActivityIntentInfo> preferredActivityFilters;
        @UnsupportedAppUsage
        public ArrayList<String> protectedBroadcasts;
        @UnsupportedAppUsage
        public final ArrayList<Provider> providers;
        @UnsupportedAppUsage
        public final ArrayList<Activity> receivers;
        @UnsupportedAppUsage
        public ArrayList<FeatureInfo> reqFeatures;
        @UnsupportedAppUsage
        public final ArrayList<String> requestedPermissions;
        public byte[] restrictUpdateHash;
        @UnsupportedAppUsage
        public final ArrayList<Service> services;
        public String[] splitCodePaths;
        public int[] splitFlags;
        public String[] splitNames;
        public int[] splitPrivateFlags;
        public int[] splitRevisionCodes;
        public int[] splitVersionCodes;
        public String staticSharedLibName;
        public long staticSharedLibVersion;
        public boolean use32bitAbi;
        @UnsupportedAppUsage
        public ArrayList<String> usesLibraries;
        @UnsupportedAppUsage
        public String[] usesLibraryFiles;
        public ArrayList<SharedLibraryInfo> usesLibraryInfos;
        @UnsupportedAppUsage
        public ArrayList<String> usesOptionalLibraries;
        public ArrayList<String> usesStaticLibraries;
        public String[][] usesStaticLibrariesCertDigests;
        public long[] usesStaticLibrariesVersions;
        public boolean visibleToInstantApps;
        public String volumeUuid;

        public long getLongVersionCode() {
            return PackageInfo.composeLongVersionCode(this.mVersionCodeMajor, this.mVersionCode);
        }

        @UnsupportedAppUsage
        public Package(String packageName2) {
            this.applicationInfo = new ApplicationInfo();
            this.permissions = new ArrayList<>(0);
            this.permissionGroups = new ArrayList<>(0);
            this.activities = new ArrayList<>(0);
            this.receivers = new ArrayList<>(0);
            this.providers = new ArrayList<>(0);
            this.services = new ArrayList<>(0);
            this.instrumentation = new ArrayList<>(0);
            this.requestedPermissions = new ArrayList<>();
            this.implicitPermissions = new ArrayList<>();
            this.staticSharedLibName = null;
            this.staticSharedLibVersion = 0;
            this.libraryNames = null;
            this.usesLibraries = null;
            this.usesStaticLibraries = null;
            this.usesStaticLibrariesVersions = null;
            this.usesStaticLibrariesCertDigests = null;
            this.usesOptionalLibraries = null;
            this.usesLibraryFiles = null;
            this.usesLibraryInfos = null;
            this.preferredActivityFilters = null;
            this.mOriginalPackages = null;
            this.mRealPackage = null;
            this.mAdoptPermissions = null;
            this.mAppMetaData = null;
            this.mSigningDetails = SigningDetails.UNKNOWN;
            this.mRealSigningDetails = SigningDetails.UNKNOWN;
            this.mPreferredOrder = 0;
            this.mLastPackageUsageTimeInMills = new long[8];
            this.configPreferences = null;
            this.reqFeatures = null;
            this.featureGroups = null;
            this.mPersistentApp = false;
            this.packageName = packageName2;
            this.manifestPackageName = packageName2;
            ApplicationInfo applicationInfo2 = this.applicationInfo;
            applicationInfo2.packageName = packageName2;
            applicationInfo2.uid = -1;
        }

        public void setApplicationVolumeUuid(String volumeUuid2) {
            UUID storageUuid = StorageManager.convert(volumeUuid2);
            ApplicationInfo applicationInfo2 = this.applicationInfo;
            applicationInfo2.volumeUuid = volumeUuid2;
            applicationInfo2.storageUuid = storageUuid;
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.volumeUuid = volumeUuid2;
                    this.childPackages.get(i).applicationInfo.storageUuid = storageUuid;
                }
            }
        }

        public void setApplicationInfoCodePath(String codePath2) {
            this.applicationInfo.setCodePath(codePath2);
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setCodePath(codePath2);
                }
            }
        }

        @Deprecated
        public void setApplicationInfoResourcePath(String resourcePath) {
            this.applicationInfo.setResourcePath(resourcePath);
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setResourcePath(resourcePath);
                }
            }
        }

        @Deprecated
        public void setApplicationInfoBaseResourcePath(String resourcePath) {
            this.applicationInfo.setBaseResourcePath(resourcePath);
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setBaseResourcePath(resourcePath);
                }
            }
        }

        public void setApplicationInfoBaseCodePath(String baseCodePath2) {
            this.applicationInfo.setBaseCodePath(baseCodePath2);
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setBaseCodePath(baseCodePath2);
                }
            }
        }

        public List<String> getChildPackageNames() {
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList == null) {
                return null;
            }
            int childCount = arrayList.size();
            List<String> childPackageNames = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                childPackageNames.add(this.childPackages.get(i).packageName);
            }
            return childPackageNames;
        }

        public boolean hasChildPackage(String packageName2) {
            ArrayList<Package> arrayList = this.childPackages;
            int childCount = arrayList != null ? arrayList.size() : 0;
            for (int i = 0; i < childCount; i++) {
                if (this.childPackages.get(i).packageName.equals(packageName2)) {
                    return true;
                }
            }
            return false;
        }

        public void setApplicationInfoSplitCodePaths(String[] splitCodePaths2) {
            this.applicationInfo.setSplitCodePaths(splitCodePaths2);
        }

        @Deprecated
        public void setApplicationInfoSplitResourcePaths(String[] resroucePaths) {
            this.applicationInfo.setSplitResourcePaths(resroucePaths);
        }

        public void setSplitCodePaths(String[] codePaths) {
            this.splitCodePaths = codePaths;
        }

        public void setCodePath(String codePath2) {
            this.codePath = codePath2;
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).codePath = codePath2;
                }
            }
        }

        public void setBaseCodePath(String baseCodePath2) {
            this.baseCodePath = baseCodePath2;
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).baseCodePath = baseCodePath2;
                }
            }
        }

        public void setSigningDetails(SigningDetails signingDetails) {
            this.mSigningDetails = signingDetails;
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).mSigningDetails = signingDetails;
                }
            }
        }

        public void setVolumeUuid(String volumeUuid2) {
            this.volumeUuid = volumeUuid2;
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).volumeUuid = volumeUuid2;
                }
            }
        }

        public void setApplicationInfoFlags(int mask, int flags) {
            ApplicationInfo applicationInfo2 = this.applicationInfo;
            applicationInfo2.flags = (applicationInfo2.flags & (~mask)) | (mask & flags);
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.flags = (this.applicationInfo.flags & (~mask)) | (mask & flags);
                }
            }
        }

        public void setUse32bitAbi(boolean use32bitAbi2) {
            this.use32bitAbi = use32bitAbi2;
            ArrayList<Package> arrayList = this.childPackages;
            if (arrayList != null) {
                int packageCount = arrayList.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).use32bitAbi = use32bitAbi2;
                }
            }
        }

        public boolean isLibrary() {
            return this.staticSharedLibName != null || !ArrayUtils.isEmpty(this.libraryNames);
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList<>();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }

        public List<String> getAllCodePathsExcludingResourceOnly() {
            ArrayList<String> paths = new ArrayList<>();
            if ((this.applicationInfo.flags & 4) != 0) {
                paths.add(this.baseCodePath);
            }
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                int i = 0;
                while (true) {
                    String[] strArr = this.splitCodePaths;
                    if (i >= strArr.length) {
                        break;
                    }
                    if ((this.splitFlags[i] & 4) != 0) {
                        paths.add(strArr[i]);
                    }
                    i++;
                }
            }
            return paths;
        }

        @UnsupportedAppUsage
        public void setPackageName(String newName) {
            this.packageName = newName;
            this.applicationInfo.packageName = newName;
            for (int i = this.permissions.size() - 1; i >= 0; i--) {
                this.permissions.get(i).setPackageName(newName);
            }
            for (int i2 = this.permissionGroups.size() - 1; i2 >= 0; i2--) {
                this.permissionGroups.get(i2).setPackageName(newName);
            }
            for (int i3 = this.activities.size() - 1; i3 >= 0; i3--) {
                this.activities.get(i3).setPackageName(newName);
            }
            for (int i4 = this.receivers.size() - 1; i4 >= 0; i4--) {
                this.receivers.get(i4).setPackageName(newName);
            }
            for (int i5 = this.providers.size() - 1; i5 >= 0; i5--) {
                this.providers.get(i5).setPackageName(newName);
            }
            for (int i6 = this.services.size() - 1; i6 >= 0; i6--) {
                this.services.get(i6).setPackageName(newName);
            }
            for (int i7 = this.instrumentation.size() - 1; i7 >= 0; i7--) {
                this.instrumentation.get(i7).setPackageName(newName);
            }
        }

        public boolean hasComponentClassName(String name) {
            for (int i = this.activities.size() - 1; i >= 0; i--) {
                if (name.equals(this.activities.get(i).className)) {
                    return true;
                }
            }
            for (int i2 = this.receivers.size() - 1; i2 >= 0; i2--) {
                if (name.equals(this.receivers.get(i2).className)) {
                    return true;
                }
            }
            for (int i3 = this.providers.size() - 1; i3 >= 0; i3--) {
                if (name.equals(this.providers.get(i3).className)) {
                    return true;
                }
            }
            for (int i4 = this.services.size() - 1; i4 >= 0; i4--) {
                if (name.equals(this.services.get(i4).className)) {
                    return true;
                }
            }
            for (int i5 = this.instrumentation.size() - 1; i5 >= 0; i5--) {
                if (name.equals(this.instrumentation.get(i5).className)) {
                    return true;
                }
            }
            return false;
        }

        public void forceResizeableAllActivity() {
            for (int i = 0; i < this.activities.size(); i++) {
                if (2 != this.activities.get(i).info.resizeMode) {
                    this.activities.get(i).info.resizeMode = 4;
                }
            }
        }

        public boolean isExternal() {
            return this.applicationInfo.isExternal();
        }

        public boolean isForwardLocked() {
            return false;
        }

        public boolean isOem() {
            return this.applicationInfo.isOem();
        }

        public boolean isVendor() {
            return this.applicationInfo.isVendor();
        }

        public boolean isProduct() {
            return this.applicationInfo.isProduct();
        }

        public boolean isProductServices() {
            return this.applicationInfo.isProductServices();
        }

        public boolean isOdm() {
            return this.applicationInfo.isOdm();
        }

        public boolean isPrivileged() {
            return this.applicationInfo.isPrivilegedApp();
        }

        public boolean isSystem() {
            return this.applicationInfo.isSystemApp();
        }

        public boolean isUpdatedSystemApp() {
            return this.applicationInfo.isUpdatedSystemApp();
        }

        public boolean canHaveOatDir() {
            return !isSystem() || isUpdatedSystemApp();
        }

        public boolean isMatch(int flags) {
            if ((1048576 & flags) != 0) {
                return isSystem();
            }
            return true;
        }

        public long getLatestPackageUseTimeInMills() {
            long latestUse = 0;
            for (long use : this.mLastPackageUsageTimeInMills) {
                latestUse = Math.max(latestUse, use);
            }
            return latestUse;
        }

        public long getLatestForegroundPackageUseTimeInMills() {
            long latestUse = 0;
            for (int reason : new int[]{0, 2}) {
                latestUse = Math.max(latestUse, this.mLastPackageUsageTimeInMills[reason]);
            }
            return latestUse;
        }

        public String toString() {
            return "Package{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        /* JADX DEBUG: Type inference failed for r0v26. Raw type applied. Possible types: android.util.ArraySet<? extends java.lang.Object>, android.util.ArraySet<java.lang.String> */
        public Package(Parcel dest) {
            this.applicationInfo = new ApplicationInfo();
            boolean z = false;
            this.permissions = new ArrayList<>(0);
            this.permissionGroups = new ArrayList<>(0);
            this.activities = new ArrayList<>(0);
            this.receivers = new ArrayList<>(0);
            this.providers = new ArrayList<>(0);
            this.services = new ArrayList<>(0);
            this.instrumentation = new ArrayList<>(0);
            this.requestedPermissions = new ArrayList<>();
            this.implicitPermissions = new ArrayList<>();
            this.staticSharedLibName = null;
            this.staticSharedLibVersion = 0;
            this.libraryNames = null;
            this.usesLibraries = null;
            this.usesStaticLibraries = null;
            this.usesStaticLibrariesVersions = null;
            this.usesStaticLibrariesCertDigests = null;
            this.usesOptionalLibraries = null;
            this.usesLibraryFiles = null;
            this.usesLibraryInfos = null;
            this.preferredActivityFilters = null;
            this.mOriginalPackages = null;
            this.mRealPackage = null;
            this.mAdoptPermissions = null;
            this.mAppMetaData = null;
            this.mSigningDetails = SigningDetails.UNKNOWN;
            this.mRealSigningDetails = SigningDetails.UNKNOWN;
            this.mPreferredOrder = 0;
            this.mLastPackageUsageTimeInMills = new long[8];
            this.configPreferences = null;
            this.reqFeatures = null;
            this.featureGroups = null;
            this.mPersistentApp = false;
            ClassLoader boot = Object.class.getClassLoader();
            this.packageName = dest.readString().intern();
            this.manifestPackageName = dest.readString();
            this.splitNames = dest.readStringArray();
            this.volumeUuid = dest.readString();
            this.codePath = dest.readString();
            this.baseCodePath = dest.readString();
            this.splitCodePaths = dest.readStringArray();
            this.baseRevisionCode = dest.readInt();
            this.splitRevisionCodes = dest.createIntArray();
            this.splitFlags = dest.createIntArray();
            this.splitPrivateFlags = dest.createIntArray();
            this.baseHardwareAccelerated = dest.readInt() == 1;
            this.applicationInfo = (ApplicationInfo) dest.readParcelable(boot);
            if (this.applicationInfo.permission != null) {
                ApplicationInfo applicationInfo2 = this.applicationInfo;
                applicationInfo2.permission = applicationInfo2.permission.intern();
            }
            dest.readParcelableList(this.permissions, boot);
            fixupOwner(this.permissions);
            dest.readParcelableList(this.permissionGroups, boot);
            fixupOwner(this.permissionGroups);
            dest.readParcelableList(this.activities, boot);
            fixupOwner(this.activities);
            dest.readParcelableList(this.receivers, boot);
            fixupOwner(this.receivers);
            dest.readParcelableList(this.providers, boot);
            fixupOwner(this.providers);
            dest.readParcelableList(this.services, boot);
            fixupOwner(this.services);
            dest.readParcelableList(this.instrumentation, boot);
            fixupOwner(this.instrumentation);
            dest.readStringList(this.requestedPermissions);
            internStringArrayList(this.requestedPermissions);
            dest.readStringList(this.implicitPermissions);
            internStringArrayList(this.implicitPermissions);
            this.protectedBroadcasts = dest.createStringArrayList();
            internStringArrayList(this.protectedBroadcasts);
            this.parentPackage = (Package) dest.readParcelable(boot);
            this.childPackages = new ArrayList<>();
            dest.readParcelableList(this.childPackages, boot);
            if (this.childPackages.size() == 0) {
                this.childPackages = null;
            }
            this.staticSharedLibName = dest.readString();
            String str = this.staticSharedLibName;
            if (str != null) {
                this.staticSharedLibName = str.intern();
            }
            this.staticSharedLibVersion = dest.readLong();
            this.libraryNames = dest.createStringArrayList();
            internStringArrayList(this.libraryNames);
            this.usesLibraries = dest.createStringArrayList();
            internStringArrayList(this.usesLibraries);
            this.usesOptionalLibraries = dest.createStringArrayList();
            internStringArrayList(this.usesOptionalLibraries);
            this.usesLibraryFiles = dest.readStringArray();
            this.usesLibraryInfos = dest.createTypedArrayList(SharedLibraryInfo.CREATOR);
            int libCount = dest.readInt();
            if (libCount > 0) {
                this.usesStaticLibraries = new ArrayList<>(libCount);
                dest.readStringList(this.usesStaticLibraries);
                internStringArrayList(this.usesStaticLibraries);
                this.usesStaticLibrariesVersions = new long[libCount];
                dest.readLongArray(this.usesStaticLibrariesVersions);
                this.usesStaticLibrariesCertDigests = new String[libCount][];
                for (int i = 0; i < libCount; i++) {
                    this.usesStaticLibrariesCertDigests[i] = dest.createStringArray();
                }
            }
            this.preferredActivityFilters = new ArrayList<>();
            dest.readParcelableList(this.preferredActivityFilters, boot);
            if (this.preferredActivityFilters.size() == 0) {
                this.preferredActivityFilters = null;
            }
            this.mOriginalPackages = dest.createStringArrayList();
            this.mRealPackage = dest.readString();
            this.mAdoptPermissions = dest.createStringArrayList();
            this.mAppMetaData = dest.readBundle();
            this.mVersionCode = dest.readInt();
            this.mVersionCodeMajor = dest.readInt();
            this.mVersionName = dest.readString();
            String str2 = this.mVersionName;
            if (str2 != null) {
                this.mVersionName = str2.intern();
            }
            this.mSharedUserId = dest.readString();
            String str3 = this.mSharedUserId;
            if (str3 != null) {
                this.mSharedUserId = str3.intern();
            }
            this.mSharedUserLabel = dest.readInt();
            this.mSigningDetails = (SigningDetails) dest.readParcelable(boot);
            this.mPreferredOrder = dest.readInt();
            this.configPreferences = new ArrayList<>();
            dest.readParcelableList(this.configPreferences, boot);
            if (this.configPreferences.size() == 0) {
                this.configPreferences = null;
            }
            this.reqFeatures = new ArrayList<>();
            dest.readParcelableList(this.reqFeatures, boot);
            if (this.reqFeatures.size() == 0) {
                this.reqFeatures = null;
            }
            this.featureGroups = new ArrayList<>();
            dest.readParcelableList(this.featureGroups, boot);
            if (this.featureGroups.size() == 0) {
                this.featureGroups = null;
            }
            this.installLocation = dest.readInt();
            this.coreApp = dest.readInt() == 1;
            this.mRequiredForAllUsers = dest.readInt() == 1;
            this.mRestrictedAccountType = dest.readString();
            this.mRequiredAccountType = dest.readString();
            this.mOverlayTarget = dest.readString();
            this.mOverlayTargetName = dest.readString();
            this.mOverlayCategory = dest.readString();
            this.mOverlayPriority = dest.readInt();
            this.mOverlayIsStatic = dest.readInt() == 1;
            this.mCompileSdkVersion = dest.readInt();
            this.mCompileSdkVersionCodename = dest.readString();
            this.mUpgradeKeySets = dest.readArraySet(boot);
            this.mKeySetMapping = readKeySetMapping(dest);
            this.cpuAbiOverride = dest.readString();
            this.use32bitAbi = dest.readInt() == 1;
            this.restrictUpdateHash = dest.createByteArray();
            this.visibleToInstantApps = dest.readInt() == 1 ? true : z;
            this.mPersistentApp = dest.readBoolean();
            this.hasPlugin = dest.readBoolean();
            this.splitVersionCodes = dest.createIntArray();
        }

        private static void internStringArrayList(List<String> list) {
            if (list != null) {
                int N = list.size();
                for (int i = 0; i < N; i++) {
                    list.set(i, list.get(i).intern());
                }
            }
        }

        private void fixupOwner(List<? extends Component<?>> list) {
            if (list != null) {
                for (Component<?> c : list) {
                    c.owner = this;
                    if (c instanceof Activity) {
                        ((Activity) c).info.applicationInfo = this.applicationInfo;
                        if (PackageParser.mFullScreenDisplay && ((Activity) c).info.maxAspectRatio > PackageParser.mExclusionNavBar && ((Activity) c).info.maxAspectRatio < PackageParser.mScreenAspectRatio) {
                            ((Activity) c).info.maxAspectRatio = PackageParser.mExclusionNavBar;
                        }
                    } else if (c instanceof Service) {
                        ((Service) c).info.applicationInfo = this.applicationInfo;
                    } else if (c instanceof Provider) {
                        ((Provider) c).info.applicationInfo = this.applicationInfo;
                    }
                }
            }
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.packageName);
            dest.writeString(this.manifestPackageName);
            dest.writeStringArray(this.splitNames);
            dest.writeString(this.volumeUuid);
            dest.writeString(this.codePath);
            dest.writeString(this.baseCodePath);
            dest.writeStringArray(this.splitCodePaths);
            dest.writeInt(this.baseRevisionCode);
            dest.writeIntArray(this.splitRevisionCodes);
            dest.writeIntArray(this.splitFlags);
            dest.writeIntArray(this.splitPrivateFlags);
            dest.writeInt(this.baseHardwareAccelerated ? 1 : 0);
            dest.writeParcelable(this.applicationInfo, flags);
            dest.writeParcelableList(this.permissions, flags);
            dest.writeParcelableList(this.permissionGroups, flags);
            dest.writeParcelableList(this.activities, flags);
            dest.writeParcelableList(this.receivers, flags);
            dest.writeParcelableList(this.providers, flags);
            dest.writeParcelableList(this.services, flags);
            dest.writeParcelableList(this.instrumentation, flags);
            dest.writeStringList(this.requestedPermissions);
            dest.writeStringList(this.implicitPermissions);
            dest.writeStringList(this.protectedBroadcasts);
            dest.writeParcelable(this.parentPackage, flags);
            dest.writeParcelableList(this.childPackages, flags);
            dest.writeString(this.staticSharedLibName);
            dest.writeLong(this.staticSharedLibVersion);
            dest.writeStringList(this.libraryNames);
            dest.writeStringList(this.usesLibraries);
            dest.writeStringList(this.usesOptionalLibraries);
            dest.writeStringArray(this.usesLibraryFiles);
            dest.writeTypedList(this.usesLibraryInfos);
            if (ArrayUtils.isEmpty(this.usesStaticLibraries)) {
                dest.writeInt(-1);
            } else {
                dest.writeInt(this.usesStaticLibraries.size());
                dest.writeStringList(this.usesStaticLibraries);
                dest.writeLongArray(this.usesStaticLibrariesVersions);
                for (String[] usesStaticLibrariesCertDigest : this.usesStaticLibrariesCertDigests) {
                    dest.writeStringArray(usesStaticLibrariesCertDigest);
                }
            }
            dest.writeParcelableList(this.preferredActivityFilters, flags);
            dest.writeStringList(this.mOriginalPackages);
            dest.writeString(this.mRealPackage);
            dest.writeStringList(this.mAdoptPermissions);
            dest.writeBundle(this.mAppMetaData);
            dest.writeInt(this.mVersionCode);
            dest.writeInt(this.mVersionCodeMajor);
            dest.writeString(this.mVersionName);
            dest.writeString(this.mSharedUserId);
            dest.writeInt(this.mSharedUserLabel);
            dest.writeParcelable(this.mSigningDetails, flags);
            dest.writeInt(this.mPreferredOrder);
            dest.writeParcelableList(this.configPreferences, flags);
            dest.writeParcelableList(this.reqFeatures, flags);
            dest.writeParcelableList(this.featureGroups, flags);
            dest.writeInt(this.installLocation);
            dest.writeInt(this.coreApp ? 1 : 0);
            dest.writeInt(this.mRequiredForAllUsers ? 1 : 0);
            dest.writeString(this.mRestrictedAccountType);
            dest.writeString(this.mRequiredAccountType);
            dest.writeString(this.mOverlayTarget);
            dest.writeString(this.mOverlayTargetName);
            dest.writeString(this.mOverlayCategory);
            dest.writeInt(this.mOverlayPriority);
            dest.writeInt(this.mOverlayIsStatic ? 1 : 0);
            dest.writeInt(this.mCompileSdkVersion);
            dest.writeString(this.mCompileSdkVersionCodename);
            dest.writeArraySet(this.mUpgradeKeySets);
            writeKeySetMapping(dest, this.mKeySetMapping);
            dest.writeString(this.cpuAbiOverride);
            dest.writeInt(this.use32bitAbi ? 1 : 0);
            dest.writeByteArray(this.restrictUpdateHash);
            dest.writeInt(this.visibleToInstantApps ? 1 : 0);
            dest.writeBoolean(this.mPersistentApp);
            dest.writeBoolean(this.hasPlugin);
            dest.writeIntArray(this.splitVersionCodes);
        }

        private static void writeKeySetMapping(Parcel dest, ArrayMap<String, ArraySet<PublicKey>> keySetMapping) {
            if (keySetMapping == null) {
                dest.writeInt(-1);
                return;
            }
            int N = keySetMapping.size();
            dest.writeInt(N);
            for (int i = 0; i < N; i++) {
                dest.writeString(keySetMapping.keyAt(i));
                ArraySet<PublicKey> keys = keySetMapping.valueAt(i);
                if (keys == null) {
                    dest.writeInt(-1);
                } else {
                    int M = keys.size();
                    dest.writeInt(M);
                    for (int j = 0; j < M; j++) {
                        dest.writeSerializable(keys.valueAt(j));
                    }
                }
            }
        }

        private static ArrayMap<String, ArraySet<PublicKey>> readKeySetMapping(Parcel in) {
            int N = in.readInt();
            if (N == -1) {
                return null;
            }
            ArrayMap<String, ArraySet<PublicKey>> keySetMapping = new ArrayMap<>();
            for (int i = 0; i < N; i++) {
                String key = in.readString();
                int M = in.readInt();
                if (M == -1) {
                    keySetMapping.put(key, null);
                } else {
                    ArraySet<PublicKey> keys = new ArraySet<>(M);
                    for (int j = 0; j < M; j++) {
                        keys.add((PublicKey) in.readSerializable());
                    }
                    keySetMapping.put(key, keys);
                }
            }
            return keySetMapping;
        }
    }

    public static abstract class Component<II extends IntentInfo> {
        @UnsupportedAppUsage
        public final String className;
        ComponentName componentName;
        String componentShortName;
        @UnsupportedAppUsage
        public final ArrayList<II> intents;
        @UnsupportedAppUsage
        public Bundle metaData;
        public int order;
        @UnsupportedAppUsage
        public Package owner;

        public Component(Package owner2, ArrayList<II> intents2, String className2) {
            this.owner = owner2;
            this.intents = intents2;
            this.className = className2;
        }

        public Component(Package owner2) {
            this.owner = owner2;
            this.intents = null;
            this.className = null;
        }

        public Component(ParsePackageItemArgs args, PackageItemInfo outInfo) {
            this.owner = args.owner;
            this.intents = new ArrayList<>(0);
            if (PackageParser.parsePackageItemInfo(args.owner, outInfo, args.outError, args.tag, args.sa, true, args.nameRes, args.labelRes, args.iconRes, args.roundIconRes, args.logoRes, args.bannerRes)) {
                this.className = outInfo.name;
            } else {
                this.className = null;
            }
        }

        public Component(ParseComponentArgs args, ComponentInfo outInfo) {
            this((ParsePackageItemArgs) args, (PackageItemInfo) outInfo);
            CharSequence pname;
            if (args.outError[0] == null) {
                if (args.processRes != 0) {
                    if (this.owner.applicationInfo.targetSdkVersion >= 8) {
                        pname = args.sa.getNonConfigurationString(args.processRes, 1024);
                    } else {
                        pname = args.sa.getNonResourceString(args.processRes);
                    }
                    outInfo.processName = PackageParser.buildProcessName(this.owner.applicationInfo.packageName, this.owner.applicationInfo.processName, pname, args.flags, args.sepProcesses, args.outError);
                }
                if (args.descriptionRes != 0) {
                    outInfo.descriptionRes = args.sa.getResourceId(args.descriptionRes, 0);
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

        @UnsupportedAppUsage
        public ComponentName getComponentName() {
            ComponentName componentName2 = this.componentName;
            if (componentName2 != null) {
                return componentName2;
            }
            if (this.className != null) {
                this.componentName = new ComponentName(this.owner.applicationInfo.packageName, this.className);
            }
            return this.componentName;
        }

        protected Component(Parcel in) {
            this.className = in.readString();
            this.metaData = in.readBundle();
            this.intents = createIntentsList(in);
            this.owner = null;
        }

        /* access modifiers changed from: protected */
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.className);
            dest.writeBundle(this.metaData);
            writeIntentsList(this.intents, dest, flags);
        }

        private static void writeIntentsList(ArrayList<? extends IntentInfo> list, Parcel out, int flags) {
            if (list == null) {
                out.writeInt(-1);
                return;
            }
            int N = list.size();
            out.writeInt(N);
            if (N > 0) {
                out.writeString(((IntentInfo) list.get(0)).getClass().getName());
                for (int i = 0; i < N; i++) {
                    ((IntentInfo) list.get(i)).writeIntentInfoToParcel(out, flags);
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r6v1, resolved type: java.util.ArrayList<T extends android.content.pm.PackageParser$IntentInfo> */
        /* JADX DEBUG: Multi-variable search result rejected for r8v2, resolved type: android.content.pm.PackageParser$IntentInfo */
        /* JADX WARN: Multi-variable type inference failed */
        private static <T extends IntentInfo> ArrayList<T> createIntentsList(Parcel in) {
            int N = in.readInt();
            if (N == -1) {
                return null;
            }
            if (N == 0) {
                return new ArrayList<>(0);
            }
            String componentName2 = in.readString();
            try {
                Constructor<?> constructor = Class.forName(componentName2).getConstructor(Parcel.class);
                ArrayList<T> intentsList = (ArrayList<T>) new ArrayList(N);
                for (int i = 0; i < N; i++) {
                    intentsList.add((IntentInfo) constructor.newInstance(in));
                }
                return intentsList;
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unable to construct intent list for: " + componentName2);
            }
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

    public static final class Permission extends Component<IntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Permission>() {
            /* class android.content.pm.PackageParser.Permission.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Permission createFromParcel(Parcel in) {
                return new Permission(in);
            }

            @Override // android.os.Parcelable.Creator
            public Permission[] newArray(int size) {
                return new Permission[size];
            }
        };
        @UnsupportedAppUsage
        public PermissionGroup group;
        @UnsupportedAppUsage
        public final PermissionInfo info;
        @UnsupportedAppUsage
        public boolean tree;

        public Permission(Package owner, String backgroundPermission) {
            super(owner);
            this.info = new PermissionInfo(backgroundPermission);
        }

        @UnsupportedAppUsage
        public Permission(Package _owner, PermissionInfo _info) {
            super(_owner);
            this.info = _info;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "Permission{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.info.name + "}";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.content.pm.PackageParser.Component, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags);
            dest.writeInt(this.tree ? 1 : 0);
            dest.writeParcelable(this.group, flags);
        }

        public boolean isAppOp() {
            return this.info.isAppOp();
        }

        private Permission(Parcel in) {
            super(in);
            ClassLoader boot = Object.class.getClassLoader();
            this.info = (PermissionInfo) in.readParcelable(boot);
            if (this.info.group != null) {
                PermissionInfo permissionInfo = this.info;
                permissionInfo.group = permissionInfo.group.intern();
            }
            this.tree = in.readInt() != 1 ? false : true;
            this.group = (PermissionGroup) in.readParcelable(boot);
        }
    }

    public static final class PermissionGroup extends Component<IntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<PermissionGroup>() {
            /* class android.content.pm.PackageParser.PermissionGroup.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public PermissionGroup createFromParcel(Parcel in) {
                return new PermissionGroup(in);
            }

            @Override // android.os.Parcelable.Creator
            public PermissionGroup[] newArray(int size) {
                return new PermissionGroup[size];
            }
        };
        @UnsupportedAppUsage
        public final PermissionGroupInfo info;

        public PermissionGroup(Package owner, int requestDetailResourceId, int backgroundRequestResourceId, int backgroundRequestDetailResourceId) {
            super(owner);
            this.info = new PermissionGroupInfo(requestDetailResourceId, backgroundRequestResourceId, backgroundRequestDetailResourceId);
        }

        public PermissionGroup(Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            this.info = _info;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.info.name + "}";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.content.pm.PackageParser.Component, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags);
        }

        private PermissionGroup(Parcel in) {
            super(in);
            this.info = (PermissionGroupInfo) in.readParcelable(Object.class.getClassLoader());
        }
    }

    private static boolean copyNeeded(int flags, Package p, PackageUserState state, Bundle metaData, int userId) {
        if (userId != 0) {
            return true;
        }
        if (state.enabled != 0) {
            if (p.applicationInfo.enabled != (state.enabled == 1)) {
                return true;
            }
        }
        if (state.suspended != ((p.applicationInfo.flags & 1073741824) != 0) || !state.installed || state.hidden || state.stopped || state.instantApp != p.applicationInfo.isInstantApp()) {
            return true;
        }
        if ((flags & 128) != 0 && (metaData != null || p.mAppMetaData != null)) {
            return true;
        }
        if ((flags & 1024) != 0 && p.usesLibraryFiles != null) {
            return true;
        }
        if (((flags & 1024) == 0 || p.usesLibraryInfos == null) && p.staticSharedLibName == null) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state) {
        return generateApplicationInfo(p, flags, state, UserHandle.getCallingUserId());
    }

    private static void updateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state) {
        if (!sCompatibilityModeEnabled) {
            ai.disableCompatibilityMode();
        }
        if (state.installed) {
            ai.flags |= 8388608;
        } else {
            ai.flags &= -8388609;
        }
        if (state.suspended) {
            ai.flags |= 1073741824;
        } else {
            ai.flags &= -1073741825;
        }
        if (state.instantApp) {
            ai.privateFlags |= 128;
        } else {
            ai.privateFlags &= -129;
        }
        if (state.virtualPreload) {
            ai.privateFlags |= 65536;
        } else {
            ai.privateFlags &= -65537;
        }
        boolean z = true;
        if (state.hidden) {
            ai.privateFlags |= 1;
        } else {
            ai.privateFlags &= -2;
        }
        if (state.enabled == 1) {
            ai.enabled = true;
        } else if (state.enabled == 4) {
            if ((32768 & flags) == 0) {
                z = false;
            }
            ai.enabled = z;
        } else if (state.enabled == 2 || state.enabled == 3) {
            ai.enabled = false;
        }
        ai.enabledSetting = state.enabled;
        if (ai.category == -1) {
            ai.category = state.categoryHint;
        }
        if (ai.category == -1) {
            ai.category = FallbackCategoryProvider.getFallbackCategory(ai.packageName);
        }
        ai.seInfoUser = SELinuxUtil.assignSeinfoUser(state);
        ai.resourceDirs = state.overlayPaths;
        ai.icon = (!sUseRoundIcon || ai.roundIconRes == 0) ? ai.iconRes : ai.roundIconRes;
    }

    @UnsupportedAppUsage
    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state, p.applicationInfo) || !p.isMatch(flags)) {
            return null;
        }
        if (copyNeeded(flags, p, state, null, userId) || ((32768 & flags) != 0 && state.enabled == 4)) {
            ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
            ai.initForUser(userId);
            if ((flags & 128) != 0) {
                ai.metaData = p.mAppMetaData;
            }
            if ((flags & 1024) != 0) {
                ai.sharedLibraryFiles = p.usesLibraryFiles;
                ai.sharedLibraryInfos = p.usesLibraryInfos;
            }
            if (state.stopped) {
                ai.flags |= 2097152;
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
        if (ai == null || !checkUseInstalledOrHidden(flags, state, ai)) {
            return null;
        }
        ApplicationInfo ai2 = new ApplicationInfo(ai);
        ai2.initForUser(userId);
        if (state.stopped) {
            ai2.flags |= 2097152;
        } else {
            ai2.flags &= -2097153;
        }
        updateApplicationInfo(ai2, flags, state);
        return ai2;
    }

    @UnsupportedAppUsage
    public static final PermissionInfo generatePermissionInfo(Permission p, int flags) {
        if (p == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return p.info;
        }
        PermissionInfo pi = new PermissionInfo(p.info);
        pi.metaData = p.metaData;
        return pi;
    }

    @UnsupportedAppUsage
    public static final PermissionGroupInfo generatePermissionGroupInfo(PermissionGroup pg, int flags) {
        if (pg == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return pg.info;
        }
        PermissionGroupInfo pgi = new PermissionGroupInfo(pg.info);
        pgi.metaData = pg.metaData;
        return pgi;
    }

    public static final class Activity extends Component<ActivityIntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Activity>() {
            /* class android.content.pm.PackageParser.Activity.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Activity createFromParcel(Parcel in) {
                return new Activity(in);
            }

            @Override // android.os.Parcelable.Creator
            public Activity[] newArray(int size) {
                return new Activity[size];
            }
        };
        @UnsupportedAppUsage
        public final ActivityInfo info;
        private boolean mHasMaxAspectRatio;
        private boolean mHasMinAspectRatio;

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasMaxAspectRatio() {
            return this.mHasMaxAspectRatio;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasMinAspectRatio() {
            return this.mHasMinAspectRatio;
        }

        Activity(Package owner, String className, ActivityInfo info2) {
            super(owner, new ArrayList(0), className);
            this.info = info2;
            this.info.applicationInfo = owner.applicationInfo;
        }

        public Activity(ParseComponentArgs args, ActivityInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setMaxAspectRatio(float maxAspectRatio) {
            if (this.info.resizeMode == 2 || this.info.resizeMode == 1) {
                this.info.maxAspectRatio = 0.0f;
            } else if (PackageParser.mFullScreenDisplay && this.info.applicationInfo.maxAspectRatio >= PackageParser.mScreenAspectRatio) {
                this.info.maxAspectRatio = 0.0f;
            } else if (maxAspectRatio < 1.0f) {
                this.info.maxAspectRatio = 0.0f;
            } else {
                if (PackageParser.mFullScreenDisplay && !PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME.equals(this.info.name) && maxAspectRatio < PackageParser.mScreenAspectRatio) {
                    this.owner.applicationInfo.hasDefaultNoFullScreen = 1;
                    if (maxAspectRatio > PackageParser.mExclusionNavBar) {
                        this.info.maxAspectRatio = PackageParser.mExclusionNavBar;
                        this.mHasMaxAspectRatio = true;
                        return;
                    }
                }
                this.info.maxAspectRatio = maxAspectRatio;
                this.mHasMaxAspectRatio = true;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setMinAspectRatio(float minAspectRatio, boolean isConfigedInWhitelist) {
            if (isResizeable() && !isConfigedInWhitelist) {
                return;
            }
            if (minAspectRatio >= 1.0f || minAspectRatio == 0.0f) {
                this.info.minAspectRatio = minAspectRatio;
                this.mHasMinAspectRatio = true;
            }
        }

        public boolean isResizeable() {
            if (this.info.resizeMode == 2 || this.info.resizeMode == 1) {
                return true;
            }
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Activity{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.content.pm.PackageParser.Component, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags | 2);
            dest.writeBoolean(this.mHasMaxAspectRatio);
            dest.writeBoolean(this.mHasMinAspectRatio);
        }

        private Activity(Parcel in) {
            super(in);
            this.info = (ActivityInfo) in.readParcelable(Object.class.getClassLoader());
            this.mHasMaxAspectRatio = in.readBoolean();
            this.mHasMinAspectRatio = in.readBoolean();
            Iterator it = this.intents.iterator();
            while (it.hasNext()) {
                ActivityIntentInfo aii = (ActivityIntentInfo) it.next();
                aii.activity = this;
                this.order = Math.max(aii.getOrder(), this.order);
            }
            if (this.info.permission != null) {
                ActivityInfo activityInfo = this.info;
                activityInfo.permission = activityInfo.permission.intern();
            }
        }
    }

    @UnsupportedAppUsage
    public static final ActivityInfo generateActivityInfo(Activity a, int flags, PackageUserState state, int userId) {
        if (a == null || !checkUseInstalledOrHidden(flags, state, a.owner.applicationInfo)) {
            return null;
        }
        if (!copyNeeded(flags, a.owner, state, a.metaData, userId)) {
            updateApplicationInfo(a.info.applicationInfo, flags, state);
            return a.info;
        }
        ActivityInfo ai = new ActivityInfo(a.info);
        ai.metaData = a.metaData;
        ai.applicationInfo = generateApplicationInfo(a.owner, flags, state, userId);
        return ai;
    }

    public static final ActivityInfo generateActivityInfo(ActivityInfo ai, int flags, PackageUserState state, int userId) {
        if (ai == null || !checkUseInstalledOrHidden(flags, state, ai.applicationInfo)) {
            return null;
        }
        ActivityInfo ai2 = new ActivityInfo(ai);
        ai2.applicationInfo = generateApplicationInfo(ai2.applicationInfo, flags, state, userId);
        return ai2;
    }

    public static final class Service extends Component<ServiceIntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Service>() {
            /* class android.content.pm.PackageParser.Service.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Service createFromParcel(Parcel in) {
                return new Service(in);
            }

            @Override // android.os.Parcelable.Creator
            public Service[] newArray(int size) {
                return new Service[size];
            }
        };
        @UnsupportedAppUsage
        public final ServiceInfo info;

        public Service(ParseComponentArgs args, ServiceInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.content.pm.PackageParser.Component, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags | 2);
        }

        private Service(Parcel in) {
            super(in);
            this.info = (ServiceInfo) in.readParcelable(Object.class.getClassLoader());
            Iterator it = this.intents.iterator();
            while (it.hasNext()) {
                ServiceIntentInfo aii = (ServiceIntentInfo) it.next();
                aii.service = this;
                this.order = Math.max(aii.getOrder(), this.order);
            }
            if (this.info.permission != null) {
                ServiceInfo serviceInfo = this.info;
                serviceInfo.permission = serviceInfo.permission.intern();
            }
        }
    }

    @UnsupportedAppUsage
    public static final ServiceInfo generateServiceInfo(Service s, int flags, PackageUserState state, int userId) {
        if (s == null || !checkUseInstalledOrHidden(flags, state, s.owner.applicationInfo)) {
            return null;
        }
        if (!copyNeeded(flags, s.owner, state, s.metaData, userId)) {
            updateApplicationInfo(s.info.applicationInfo, flags, state);
            return s.info;
        }
        ServiceInfo si = new ServiceInfo(s.info);
        si.metaData = s.metaData;
        si.applicationInfo = generateApplicationInfo(s.owner, flags, state, userId);
        return si;
    }

    public static final class Provider extends Component<ProviderIntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Provider>() {
            /* class android.content.pm.PackageParser.Provider.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Provider createFromParcel(Parcel in) {
                return new Provider(in);
            }

            @Override // android.os.Parcelable.Creator
            public Provider[] newArray(int size) {
                return new Provider[size];
            }
        };
        @UnsupportedAppUsage
        public final ProviderInfo info;
        @UnsupportedAppUsage
        public boolean syncable;

        public Provider(ParseComponentArgs args, ProviderInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
            this.syncable = false;
        }

        @UnsupportedAppUsage
        public Provider(Provider existingProvider) {
            super(existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Provider{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.content.pm.PackageParser.Component, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags | 2);
            dest.writeInt(this.syncable ? 1 : 0);
        }

        private Provider(Parcel in) {
            super(in);
            this.info = (ProviderInfo) in.readParcelable(Object.class.getClassLoader());
            this.syncable = in.readInt() != 1 ? false : true;
            Iterator it = this.intents.iterator();
            while (it.hasNext()) {
                ((ProviderIntentInfo) it.next()).provider = this;
            }
            if (this.info.readPermission != null) {
                ProviderInfo providerInfo = this.info;
                providerInfo.readPermission = providerInfo.readPermission.intern();
            }
            if (this.info.writePermission != null) {
                ProviderInfo providerInfo2 = this.info;
                providerInfo2.writePermission = providerInfo2.writePermission.intern();
            }
            if (this.info.authority != null) {
                ProviderInfo providerInfo3 = this.info;
                providerInfo3.authority = providerInfo3.authority.intern();
            }
        }
    }

    @UnsupportedAppUsage
    public static final ProviderInfo generateProviderInfo(Provider p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state, p.owner.applicationInfo)) {
            return null;
        }
        if (copyNeeded(flags, p.owner, state, p.metaData, userId) || ((flags & 2048) == 0 && p.info.uriPermissionPatterns != null)) {
            ProviderInfo pi = new ProviderInfo(p.info);
            pi.metaData = p.metaData;
            if ((flags & 2048) == 0) {
                pi.uriPermissionPatterns = null;
            }
            pi.applicationInfo = generateApplicationInfo(p.owner, flags, state, userId);
            return pi;
        }
        updateApplicationInfo(p.info.applicationInfo, flags, state);
        return p.info;
    }

    public static final class Instrumentation extends Component<IntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Instrumentation>() {
            /* class android.content.pm.PackageParser.Instrumentation.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Instrumentation createFromParcel(Parcel in) {
                return new Instrumentation(in);
            }

            @Override // android.os.Parcelable.Creator
            public Instrumentation[] newArray(int size) {
                return new Instrumentation[size];
            }
        };
        @UnsupportedAppUsage
        public final InstrumentationInfo info;

        public Instrumentation(ParsePackageItemArgs args, InstrumentationInfo _info) {
            super(args, _info);
            this.info = _info;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Instrumentation{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.content.pm.PackageParser.Component, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags);
        }

        private Instrumentation(Parcel in) {
            super(in);
            this.info = (InstrumentationInfo) in.readParcelable(Object.class.getClassLoader());
            if (this.info.targetPackage != null) {
                InstrumentationInfo instrumentationInfo = this.info;
                instrumentationInfo.targetPackage = instrumentationInfo.targetPackage.intern();
            }
            if (this.info.targetProcesses != null) {
                InstrumentationInfo instrumentationInfo2 = this.info;
                instrumentationInfo2.targetProcesses = instrumentationInfo2.targetProcesses.intern();
            }
        }
    }

    @UnsupportedAppUsage
    public static final InstrumentationInfo generateInstrumentationInfo(Instrumentation i, int flags) {
        if (i == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return i.info;
        }
        InstrumentationInfo ii = new InstrumentationInfo(i.info);
        ii.metaData = i.metaData;
        return ii;
    }

    public static abstract class IntentInfo extends IntentFilter {
        @UnsupportedAppUsage
        public int banner;
        @UnsupportedAppUsage
        public boolean hasDefault;
        @UnsupportedAppUsage
        public int icon;
        @UnsupportedAppUsage
        public int labelRes;
        @UnsupportedAppUsage
        public int logo;
        @UnsupportedAppUsage
        public CharSequence nonLocalizedLabel;
        public int preferred;

        @UnsupportedAppUsage
        protected IntentInfo() {
        }

        protected IntentInfo(Parcel dest) {
            super(dest);
            this.hasDefault = dest.readInt() != 1 ? false : true;
            this.labelRes = dest.readInt();
            this.nonLocalizedLabel = dest.readCharSequence();
            this.icon = dest.readInt();
            this.logo = dest.readInt();
            this.banner = dest.readInt();
            this.preferred = dest.readInt();
        }

        public void writeIntentInfoToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.hasDefault ? 1 : 0);
            dest.writeInt(this.labelRes);
            dest.writeCharSequence(this.nonLocalizedLabel);
            dest.writeInt(this.icon);
            dest.writeInt(this.logo);
            dest.writeInt(this.banner);
            dest.writeInt(this.preferred);
        }
    }

    public static final class ActivityIntentInfo extends IntentInfo {
        @UnsupportedAppUsage
        public Activity activity;

        public ActivityIntentInfo(Activity _activity) {
            this.activity = _activity;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ActivityIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.activity.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        public ActivityIntentInfo(Parcel in) {
            super(in);
        }
    }

    public static final class ServiceIntentInfo extends IntentInfo {
        @UnsupportedAppUsage
        @RCUnownedRef
        public Service service;

        public ServiceIntentInfo(Service _service) {
            this.service = _service;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ServiceIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.service.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        public ServiceIntentInfo(Parcel in) {
            super(in);
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        @UnsupportedAppUsage
        @RCUnownedRef
        public Provider provider;

        public ProviderIntentInfo(Provider provider2) {
            this.provider = provider2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ProviderIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.provider.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        public ProviderIntentInfo(Parcel in) {
            super(in);
        }
    }

    @UnsupportedAppUsage
    public static void setCompatibilityModeEnabled(boolean compatibilityModeEnabled) {
        sCompatibilityModeEnabled = compatibilityModeEnabled;
    }

    public static void readConfigUseRoundIcon(Resources r) {
        if (r != null) {
            sUseRoundIcon = r.getBoolean(R.bool.config_useRoundIcon);
            return;
        }
        try {
            ApplicationInfo androidAppInfo = ActivityThread.getPackageManager().getApplicationInfo("android", 0, UserHandle.myUserId());
            Resources systemResources = Resources.getSystem();
            sUseRoundIcon = ResourcesManager.getInstance().getResources(null, null, null, androidAppInfo.resourceDirs, androidAppInfo.sharedLibraryFiles, 0, null, systemResources.getCompatibilityInfo(), systemResources.getClassLoader()).getBoolean(R.bool.config_useRoundIcon);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static class PackageParserException extends Exception {
        public final int error;

        public PackageParserException(int error2, String detailMessage) {
            super(detailMessage);
            this.error = error2;
        }

        public PackageParserException(int error2, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.error = error2;
        }
    }

    public static PackageInfo generatePackageInfoFromApex(ApexInfo apexInfo, int flags) throws PackageParserException {
        PackageParser pp = new PackageParser();
        File apexFile = new File(apexInfo.packagePath);
        Package p = pp.parsePackage(apexFile, flags, false);
        PackageInfo pi = generatePackageInfo(p, EmptyArray.INT, flags, 0, 0, Collections.emptySet(), new PackageUserState());
        pi.applicationInfo.sourceDir = apexFile.getPath();
        pi.applicationInfo.publicSourceDir = apexFile.getPath();
        if (apexInfo.isFactory) {
            pi.applicationInfo.flags |= 1;
        } else {
            pi.applicationInfo.flags &= -2;
        }
        if (apexInfo.isActive) {
            pi.applicationInfo.flags |= 8388608;
        } else {
            pi.applicationInfo.flags &= -8388609;
        }
        pi.isApex = true;
        if ((134217728 & flags) != 0) {
            collectCertificates(p, apexFile, false);
            if (p.mSigningDetails.hasPastSigningCertificates()) {
                pi.signatures = new Signature[1];
                pi.signatures[0] = p.mSigningDetails.pastSigningCertificates[0];
            } else if (p.mSigningDetails.hasSignatures()) {
                int numberOfSigs = p.mSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs];
                System.arraycopy(p.mSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs);
            }
            if (p.mSigningDetails != SigningDetails.UNKNOWN) {
                pi.signingInfo = new SigningInfo(p.mSigningDetails);
            } else {
                pi.signingInfo = null;
            }
        }
        return pi;
    }
}
