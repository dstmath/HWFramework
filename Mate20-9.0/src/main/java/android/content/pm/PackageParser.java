package android.content.pm;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.Notification;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParserCacheHelper;
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
import android.media.midi.MidiDeviceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.StorageManager;
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
import android.util.Flog;
import android.util.Log;
import android.util.PackageUtils;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.apk.ApkSignatureVerifier;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.ClassLoaderFactory;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
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
    public static final int APK_SIGNING_UNKNOWN = 0;
    public static final int APK_SIGNING_V1 = 1;
    public static final int APK_SIGNING_V2 = 2;
    private static final Set<String> CHILD_PACKAGE_TAGS = new ArraySet();
    private static final int CURRENT_EMUI_SDK_VERSION = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
    private static final boolean DEBUG_BACKUP = false;
    private static final boolean DEBUG_JAR = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean LOG_PARSE_TIMINGS = Build.IS_DEBUGGABLE;
    private static final int LOG_PARSE_TIMINGS_THRESHOLD_MS = 100;
    private static final boolean LOG_UNSAFE_BROADCASTS = false;
    private static final int MAX_PACKAGES_PER_APK = 5;
    private static final String METADATA_GESTURE_NAV_OPTIONS = "hw.gesture_nav_options";
    private static final String METADATA_MAX_ASPECT_RATIO = "android.max_aspect";
    private static final String METADATA_MIN_ASPECT_RATIO = "android.min_aspect";
    private static final String METADATA_NOTCH_SUPPORT = "android.notch_support";
    private static final String MNT_EXPAND = "/mnt/expand/";
    private static final boolean MULTI_PACKAGE_APK_ENABLED = (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROPERTY_CHILD_PACKAGES_ENABLED, false));
    public static final NewPermissionInfo[] NEW_PERMISSIONS = {new NewPermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, 4, 0), new NewPermissionInfo(Manifest.permission.READ_PHONE_STATE, 4, 0)};
    public static final int PARSE_CHATTY = Integer.MIN_VALUE;
    public static final int PARSE_COLLECT_CERTIFICATES = 32;
    private static final int PARSE_DEFAULT_INSTALL_LOCATION = -1;
    private static final int PARSE_DEFAULT_TARGET_SANDBOX = 1;
    public static final int PARSE_ENFORCE_CODE = 64;
    public static final int PARSE_EXTERNAL_STORAGE = 8;
    public static final int PARSE_FORCE_SDK = 128;
    @Deprecated
    public static final int PARSE_FORWARD_LOCK = 4;
    public static final int PARSE_IGNORE_PROCESSES = 2;
    public static final int PARSE_IS_SYSTEM_DIR = 16;
    public static final int PARSE_MUST_BE_APK = 1;
    private static final String PROPERTY_CHILD_PACKAGES_ENABLED = "persist.sys.child_packages_enabled";
    private static final int RECREATE_ON_CONFIG_CHANGES_MASK = 3;
    private static final boolean RIGID_PARSER = false;
    private static final Set<String> SAFE_BROADCASTS = new ArraySet();
    private static final String[] SDK_CODENAMES = Build.VERSION.ACTIVE_CODENAMES;
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
    public static final SplitPermissionInfo[] SPLIT_PERMISSIONS = {new SplitPermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10001), new SplitPermissionInfo(Manifest.permission.READ_CONTACTS, new String[]{Manifest.permission.READ_CALL_LOG}, 16), new SplitPermissionInfo(Manifest.permission.WRITE_CONTACTS, new String[]{Manifest.permission.WRITE_CALL_LOG}, 16)};
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
    private static float mDefaultMaxAspectRatio = 1.86f;
    /* access modifiers changed from: private */
    public static float mExclusionNavBar = 0.0f;
    private static boolean mFristAddView = true;
    /* access modifiers changed from: private */
    public static boolean mFullScreenDisplay = false;
    /* access modifiers changed from: private */
    public static float mScreenAspectRatio = 0.0f;
    public static final AtomicInteger sCachedPackageReadCount = new AtomicInteger();
    private static boolean sCompatibilityModeEnabled = true;
    private static int sCurrentEmuiSysImgVersion = 0;
    private static final Comparator<String> sSplitNameComparator = new SplitNameComparator();
    @Deprecated
    private String mArchiveSourcePath;
    private File mCacheDir;
    private Callback mCallback;
    private DisplayMetrics mMetrics = new DisplayMetrics();
    private boolean mOnlyCoreApps;
    private int mParseError = 1;
    private ParsePackageItemArgs mParseInstrumentationArgs;
    private String[] mSeparateProcesses;

    public static final class Activity extends Component<ActivityIntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Activity>() {
            public Activity createFromParcel(Parcel in) {
                return new Activity(in);
            }

            public Activity[] newArray(int size) {
                return new Activity[size];
            }
        };
        public final ActivityInfo info;
        private boolean mHasMaxAspectRatio;

        /* access modifiers changed from: private */
        public boolean hasMaxAspectRatio() {
            return this.mHasMaxAspectRatio;
        }

        public Activity(ParseComponentArgs args, ActivityInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        /* access modifiers changed from: private */
        public void setMaxAspectRatio(float maxAspectRatio) {
            if (this.info.resizeMode == 2 || this.info.resizeMode == 1) {
                setOriginMaxRatio(PackageParser.mScreenAspectRatio);
                this.info.maxAspectRatio = 0.0f;
                return;
            }
            if (HwFoldScreenState.isFoldScreenDevice() && !this.owner.applicationInfo.canChangeAspectRatio(AbsApplicationInfo.MIN_ASPECT_RATIO) && (this.owner.mAppMetaData == null || !this.owner.mAppMetaData.containsKey(PackageParser.METADATA_MIN_ASPECT_RATIO))) {
                this.owner.applicationInfo.hw_extra_flags |= 1024;
            }
            if (PackageParser.mFullScreenDisplay && this.info.applicationInfo.maxAspectRatio >= PackageParser.mScreenAspectRatio) {
                setOriginMaxRatio(PackageParser.mScreenAspectRatio);
                this.info.maxAspectRatio = 0.0f;
            } else if (maxAspectRatio < 1.0f) {
                setOriginMaxRatio(PackageParser.mScreenAspectRatio);
                this.info.maxAspectRatio = 0.0f;
            } else {
                if (PackageParser.mFullScreenDisplay && maxAspectRatio < PackageParser.mScreenAspectRatio) {
                    this.owner.applicationInfo.hasDefaultNoFullScreen = 1;
                    if (maxAspectRatio > PackageParser.mExclusionNavBar) {
                        this.info.originMaxAspectRatio = PackageParser.mExclusionNavBar;
                        this.info.maxAspectRatio = PackageParser.mExclusionNavBar;
                        this.mHasMaxAspectRatio = true;
                        return;
                    }
                }
                setOriginMaxRatio(maxAspectRatio);
                this.info.originMaxAspectRatio = maxAspectRatio;
                this.info.maxAspectRatio = maxAspectRatio;
                this.mHasMaxAspectRatio = true;
            }
        }

        private void setOriginMaxRatio(float maxRatio) {
            this.info.originMaxAspectRatio = maxRatio;
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

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags | 2);
            dest.writeBoolean(this.mHasMaxAspectRatio);
        }

        private Activity(Parcel in) {
            super(in);
            this.info = (ActivityInfo) in.readParcelable(Object.class.getClassLoader());
            this.mHasMaxAspectRatio = in.readBoolean();
            Iterator it = this.intents.iterator();
            while (it.hasNext()) {
                ActivityIntentInfo aii = (ActivityIntentInfo) it.next();
                aii.activity = this;
                this.order = Math.max(aii.getOrder(), this.order);
            }
            if (this.info.permission != null) {
                this.info.permission = this.info.permission.intern();
            }
        }
    }

    public static final class ActivityIntentInfo extends IntentInfo {
        @RCUnownedRef
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

    public static class ApkLite {
        public final String codePath;
        public final String configForSplit;
        public final boolean coreApp;
        public final boolean debuggable;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public boolean isFeatureSplit;
        public final boolean isPlugin;
        public final boolean isolatedSplits;
        public final boolean multiArch;
        public final String packageName;
        public final int revisionCode;
        public final SigningDetails signingDetails;
        public final String splitName;
        public final boolean use32bitAbi;
        public final String usesSplitName;
        public final VerifierInfo[] verifiers;
        public final int versionCode;
        public final int versionCodeMajor;

        public ApkLite(String codePath2, String packageName2, String splitName2, boolean isFeatureSplit2, String configForSplit2, String usesSplitName2, int versionCode2, int versionCodeMajor2, int revisionCode2, int installLocation2, List<VerifierInfo> verifiers2, SigningDetails signingDetails2, boolean coreApp2, boolean debuggable2, boolean multiArch2, boolean use32bitAbi2, boolean extractNativeLibs2, boolean isolatedSplits2, boolean isPlugin2) {
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
            this.extractNativeLibs = extractNativeLibs2;
            this.isolatedSplits = isolatedSplits2;
            this.isPlugin = isPlugin2;
        }

        public ApkLite(String codePath2, String packageName2, String splitName2, boolean isFeatureSplit2, String configForSplit2, String usesSplitName2, int versionCode2, int versionCodeMajor2, int revisionCode2, int installLocation2, List<VerifierInfo> verifiers2, SigningDetails signingDetails2, boolean coreApp2, boolean debuggable2, boolean multiArch2, boolean use32bitAbi2, boolean extractNativeLibs2, boolean isolatedSplits2) {
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
            this.extractNativeLibs = extractNativeLibs2;
            this.isolatedSplits = isolatedSplits2;
            this.isPlugin = false;
        }

        public long getLongVersionCode() {
            return PackageInfo.composeLongVersionCode(this.versionCodeMajor, this.versionCode);
        }
    }

    private static class CachedComponentArgs {
        ParseComponentArgs mActivityAliasArgs;
        ParseComponentArgs mActivityArgs;
        ParseComponentArgs mProviderArgs;
        ParseComponentArgs mServiceArgs;

        private CachedComponentArgs() {
        }
    }

    public interface Callback {
        String[] getOverlayApks(String str);

        String[] getOverlayPaths(String str, String str2);

        boolean hasFeature(String str);
    }

    public static final class CallbackImpl implements Callback {
        private final PackageManager mPm;

        public CallbackImpl(PackageManager pm) {
            this.mPm = pm;
        }

        public boolean hasFeature(String feature) {
            return this.mPm.hasSystemFeature(feature);
        }

        public String[] getOverlayPaths(String targetPackageName, String targetPath) {
            return null;
        }

        public String[] getOverlayApks(String targetPackageName) {
            return null;
        }
    }

    public static abstract class Component<II extends IntentInfo> {
        public final String className;
        ComponentName componentName;
        String componentShortName;
        public final ArrayList<II> intents;
        public Bundle metaData;
        public int order;
        @RCUnownedRef
        public Package owner;

        public Component(Package _owner) {
            this.owner = _owner;
            this.intents = null;
            this.className = null;
        }

        public Component(ParsePackageItemArgs args, PackageItemInfo outInfo) {
            ParsePackageItemArgs parsePackageItemArgs = args;
            this.owner = parsePackageItemArgs.owner;
            this.intents = new ArrayList<>(0);
            if (PackageParser.parsePackageItemInfo(parsePackageItemArgs.owner, outInfo, parsePackageItemArgs.outError, parsePackageItemArgs.tag, parsePackageItemArgs.sa, true, parsePackageItemArgs.nameRes, parsePackageItemArgs.labelRes, parsePackageItemArgs.iconRes, parsePackageItemArgs.roundIconRes, parsePackageItemArgs.logoRes, parsePackageItemArgs.bannerRes)) {
                this.className = outInfo.name;
                return;
            }
            PackageItemInfo packageItemInfo = outInfo;
            this.className = null;
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

        public ComponentName getComponentName() {
            if (this.componentName != null) {
                return this.componentName;
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

        private static <T extends IntentInfo> ArrayList<T> createIntentsList(Parcel in) {
            int N = in.readInt();
            if (N == -1) {
                return null;
            }
            if (N == 0) {
                return new ArrayList<>(0);
            }
            try {
                Constructor<?> constructor = Class.forName(in.readString()).getConstructor(new Class[]{Parcel.class});
                ArrayList<T> intentsList = new ArrayList<>(N);
                for (int i = 0; i < N; i++) {
                    intentsList.add((IntentInfo) constructor.newInstance(new Object[]{in}));
                }
                return intentsList;
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unable to construct intent list for: " + componentName);
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

    public static final class Instrumentation extends Component<IntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Instrumentation>() {
            public Instrumentation createFromParcel(Parcel in) {
                return new Instrumentation(in);
            }

            public Instrumentation[] newArray(int size) {
                return new Instrumentation[size];
            }
        };
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
            StringBuilder sb = new StringBuilder(128);
            sb.append("Instrumentation{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags);
        }

        private Instrumentation(Parcel in) {
            super(in);
            this.info = (InstrumentationInfo) in.readParcelable(Object.class.getClassLoader());
            if (this.info.targetPackage != null) {
                this.info.targetPackage = this.info.targetPackage.intern();
            }
            if (this.info.targetProcesses != null) {
                this.info.targetProcesses = this.info.targetProcesses.intern();
            }
        }
    }

    public static abstract class IntentInfo extends IntentFilter {
        public int banner;
        public boolean hasDefault;
        public int icon;
        public int labelRes;
        public int logo;
        public CharSequence nonLocalizedLabel;
        public int preferred;

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

    public static class NewPermissionInfo {
        public final int fileVersion;
        public final String name;
        public final int sdkVersion;

        public NewPermissionInfo(String name2, int sdkVersion2, int fileVersion2) {
            this.name = name2;
            this.sdkVersion = sdkVersion2;
            this.fileVersion = fileVersion2;
        }
    }

    public static final class Package implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Package>() {
            public Package createFromParcel(Parcel in) {
                return new Package(in);
            }

            public Package[] newArray(int size) {
                return new Package[size];
            }
        };
        public final ArrayList<Activity> activities;
        public ApplicationInfo applicationInfo;
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
        public boolean isPlugin;
        public boolean isStub;
        public ArrayList<String> libraryNames;
        public ArrayList<String> mAdoptPermissions;
        public Bundle mAppMetaData;
        public int mCompileSdkVersion;
        public String mCompileSdkVersionCodename;
        public Object mExtras;
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;
        public long[] mLastPackageUsageTimeInMills;
        public ArrayList<String> mOriginalPackages;
        public String mOverlayCategory;
        public boolean mOverlayIsStatic;
        public int mOverlayPriority;
        public String mOverlayTarget;
        public boolean mPersistentApp;
        public int mPreferredOrder;
        public String mRealPackage;
        public SigningDetails mRealSigningDetails;
        public String mRequiredAccountType;
        public boolean mRequiredForAllUsers;
        public String mRestrictedAccountType;
        public String mSharedUserId;
        public int mSharedUserLabel;
        public SigningDetails mSigningDetails;
        public ArraySet<String> mUpgradeKeySets;
        public int mVersionCode;
        public int mVersionCodeMajor;
        public String mVersionName;
        public String manifestPackageName;
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
        public int[] splitVersionCodes;
        public String staticSharedLibName;
        public long staticSharedLibVersion;
        public boolean use32bitAbi;
        public ArrayList<String> usesLibraries;
        public String[] usesLibraryFiles;
        public ArrayList<String> usesOptionalLibraries;
        public ArrayList<String> usesStaticLibraries;
        public String[][] usesStaticLibrariesCertDigests;
        public long[] usesStaticLibrariesVersions;
        public boolean visibleToInstantApps;
        public String volumeUuid;

        public long getLongVersionCode() {
            return PackageInfo.composeLongVersionCode(this.mVersionCodeMajor, this.mVersionCode);
        }

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
            this.staticSharedLibName = null;
            this.staticSharedLibVersion = 0;
            this.libraryNames = null;
            this.usesLibraries = null;
            this.usesStaticLibraries = null;
            this.usesStaticLibrariesVersions = null;
            this.usesStaticLibrariesCertDigests = null;
            this.usesOptionalLibraries = null;
            this.usesLibraryFiles = null;
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
            this.applicationInfo.packageName = packageName2;
            this.applicationInfo.uid = -1;
        }

        public void setApplicationVolumeUuid(String volumeUuid2) {
            UUID storageUuid = StorageManager.convert(volumeUuid2);
            this.applicationInfo.volumeUuid = volumeUuid2;
            this.applicationInfo.storageUuid = storageUuid;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.volumeUuid = volumeUuid2;
                    this.childPackages.get(i).applicationInfo.storageUuid = storageUuid;
                }
            }
        }

        public void setApplicationInfoCodePath(String codePath2) {
            this.applicationInfo.setCodePath(codePath2);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setCodePath(codePath2);
                }
            }
        }

        @Deprecated
        public void setApplicationInfoResourcePath(String resourcePath) {
            this.applicationInfo.setResourcePath(resourcePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setResourcePath(resourcePath);
                }
            }
        }

        @Deprecated
        public void setApplicationInfoBaseResourcePath(String resourcePath) {
            this.applicationInfo.setBaseResourcePath(resourcePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setBaseResourcePath(resourcePath);
                }
            }
        }

        public void setApplicationInfoBaseCodePath(String baseCodePath2) {
            this.applicationInfo.setBaseCodePath(baseCodePath2);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.setBaseCodePath(baseCodePath2);
                }
            }
        }

        public List<String> getChildPackageNames() {
            if (this.childPackages == null) {
                return null;
            }
            int childCount = this.childPackages.size();
            List<String> childPackageNames = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                childPackageNames.add(this.childPackages.get(i).packageName);
            }
            return childPackageNames;
        }

        public boolean hasChildPackage(String packageName2) {
            int childCount = this.childPackages != null ? this.childPackages.size() : 0;
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
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).codePath = codePath2;
                }
            }
        }

        public void setBaseCodePath(String baseCodePath2) {
            this.baseCodePath = baseCodePath2;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).baseCodePath = baseCodePath2;
                }
            }
        }

        public void setSigningDetails(SigningDetails signingDetails) {
            this.mSigningDetails = signingDetails;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).mSigningDetails = signingDetails;
                }
            }
        }

        public void setVolumeUuid(String volumeUuid2) {
            this.volumeUuid = volumeUuid2;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).volumeUuid = volumeUuid2;
                }
            }
        }

        public void setApplicationInfoFlags(int mask, int flags) {
            this.applicationInfo.flags = (this.applicationInfo.flags & (~mask)) | (mask & flags);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    this.childPackages.get(i).applicationInfo.flags = (this.applicationInfo.flags & (~mask)) | (mask & flags);
                }
            }
        }

        public void setUse32bitAbi(boolean use32bitAbi2) {
            this.use32bitAbi = use32bitAbi2;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
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
                for (int i = 0; i < this.splitCodePaths.length; i++) {
                    if ((this.splitFlags[i] & 4) != 0) {
                        paths.add(this.splitCodePaths[i]);
                    }
                }
            }
            return paths;
        }

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

        public void clearResizeableAllActivity() {
            int i = 0;
            while (i < this.activities.size() && this.activities.get(i) != null && this.activities.get(i).info != null) {
                boolean isResizeEnable = true;
                if (!(2 == this.activities.get(i).info.resizeMode || 4 == this.activities.get(i).info.resizeMode || 1 == this.activities.get(i).info.resizeMode)) {
                    isResizeEnable = false;
                }
                if (isResizeEnable) {
                    this.activities.get(i).info.resizeMode = 0;
                }
                i++;
            }
        }

        public boolean isExternal() {
            return this.applicationInfo.isExternal();
        }

        public boolean isForwardLocked() {
            return this.applicationInfo.isForwardLocked();
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
            return (!isSystem() || isUpdatedSystemApp()) && !isForwardLocked() && !this.applicationInfo.isExternalAsec();
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
            return "Package{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.packageName + "}";
        }

        public int describeContents() {
            return 0;
        }

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
            this.staticSharedLibName = null;
            this.staticSharedLibVersion = 0;
            this.libraryNames = null;
            this.usesLibraries = null;
            this.usesStaticLibraries = null;
            this.usesStaticLibrariesVersions = null;
            this.usesStaticLibrariesCertDigests = null;
            this.usesOptionalLibraries = null;
            this.usesLibraryFiles = null;
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
                this.applicationInfo.permission = this.applicationInfo.permission.intern();
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
            this.protectedBroadcasts = dest.createStringArrayList();
            internStringArrayList(this.protectedBroadcasts);
            this.parentPackage = (Package) dest.readParcelable(boot);
            this.childPackages = new ArrayList<>();
            dest.readParcelableList(this.childPackages, boot);
            if (this.childPackages.size() == 0) {
                this.childPackages = null;
            }
            this.staticSharedLibName = dest.readString();
            if (this.staticSharedLibName != null) {
                this.staticSharedLibName = this.staticSharedLibName.intern();
            }
            this.staticSharedLibVersion = dest.readLong();
            this.libraryNames = dest.createStringArrayList();
            internStringArrayList(this.libraryNames);
            this.usesLibraries = dest.createStringArrayList();
            internStringArrayList(this.usesLibraries);
            this.usesOptionalLibraries = dest.createStringArrayList();
            internStringArrayList(this.usesOptionalLibraries);
            this.usesLibraryFiles = dest.readStringArray();
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
            if (this.mVersionName != null) {
                this.mVersionName = this.mVersionName.intern();
            }
            this.mSharedUserId = dest.readString();
            if (this.mSharedUserId != null) {
                this.mSharedUserId = this.mSharedUserId.intern();
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
            this.isPlugin = dest.readBoolean();
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
            dest.writeStringList(this.protectedBroadcasts);
            dest.writeParcelable(this.parentPackage, flags);
            dest.writeParcelableList(this.childPackages, flags);
            dest.writeString(this.staticSharedLibName);
            dest.writeLong(this.staticSharedLibVersion);
            dest.writeStringList(this.libraryNames);
            dest.writeStringList(this.usesLibraries);
            dest.writeStringList(this.usesOptionalLibraries);
            dest.writeStringArray(this.usesLibraryFiles);
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
            dest.writeBoolean(this.isPlugin);
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

    public static class PackageLite {
        public final String baseCodePath;
        public final int baseRevisionCode;
        public final String codePath;
        public final String[] configForSplit;
        public final boolean coreApp;
        public final boolean debuggable;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public final boolean[] isFeatureSplits;
        public final boolean isPlugin;
        public final boolean isolatedSplits;
        public final boolean multiArch;
        public final String packageName;
        public final String[] splitCodePaths;
        public final String[] splitNames;
        public final int[] splitRevisionCodes;
        public final int[] splitVersionCodes;
        public final boolean use32bitAbi;
        public final String[] usesSplitNames;
        public final VerifierInfo[] verifiers;
        public final int versionCode;
        public final int versionCodeMajor;

        public PackageLite(String codePath2, ApkLite baseApk, String[] splitNames2, boolean[] isFeatureSplits2, String[] usesSplitNames2, String[] configForSplit2, String[] splitCodePaths2, int[] splitVersionCodes2, int[] splitRevisionCodes2, boolean isPlugin2) {
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
            this.isPlugin = isPlugin2;
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
            this.isPlugin = false;
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface ParseFlags {
    }

    static class ParsePackageItemArgs {
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

    public static final class Permission extends Component<IntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Permission>() {
            public Permission createFromParcel(Parcel in) {
                return new Permission(in);
            }

            public Permission[] newArray(int size) {
                return new Permission[size];
            }
        };
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
            return "Permission{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.info.name + "}";
        }

        public int describeContents() {
            return 0;
        }

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
                this.info.group = this.info.group.intern();
            }
            this.tree = in.readInt() != 1 ? false : true;
            this.group = (PermissionGroup) in.readParcelable(boot);
        }
    }

    public static final class PermissionGroup extends Component<IntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<PermissionGroup>() {
            public PermissionGroup createFromParcel(Parcel in) {
                return new PermissionGroup(in);
            }

            public PermissionGroup[] newArray(int size) {
                return new PermissionGroup[size];
            }
        };
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
            return "PermissionGroup{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.info.name + "}";
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.info, flags);
        }

        private PermissionGroup(Parcel in) {
            super(in);
            this.info = (PermissionGroupInfo) in.readParcelable(Object.class.getClassLoader());
        }
    }

    public static final class Provider extends Component<ProviderIntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Provider>() {
            public Provider createFromParcel(Parcel in) {
                return new Provider(in);
            }

            public Provider[] newArray(int size) {
                return new Provider[size];
            }
        };
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(ParseComponentArgs args, ProviderInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
            this.syncable = false;
        }

        public Provider(Provider existingProvider) {
            super(existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

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

        public int describeContents() {
            return 0;
        }

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
                this.info.readPermission = this.info.readPermission.intern();
            }
            if (this.info.writePermission != null) {
                this.info.writePermission = this.info.writePermission.intern();
            }
            if (this.info.authority != null) {
                this.info.authority = this.info.authority.intern();
            }
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
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

    public static final class Service extends Component<ServiceIntentInfo> implements Parcelable {
        public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Service>() {
            public Service createFromParcel(Parcel in) {
                return new Service(in);
            }

            public Service[] newArray(int size) {
                return new Service[size];
            }
        };
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
            StringBuilder sb = new StringBuilder(128);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }

        public int describeContents() {
            return 0;
        }

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
                this.info.permission = this.info.permission.intern();
            }
        }
    }

    public static final class ServiceIntentInfo extends IntentInfo {
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

    public static final class SigningDetails implements Parcelable {
        public static final Parcelable.Creator<SigningDetails> CREATOR = new Parcelable.Creator<SigningDetails>() {
            public SigningDetails createFromParcel(Parcel source) {
                if (source.readBoolean()) {
                    return SigningDetails.UNKNOWN;
                }
                return new SigningDetails(source);
            }

            public SigningDetails[] newArray(int size) {
                return new SigningDetails[size];
            }
        };
        private static final int PAST_CERT_EXISTS = 0;
        public static final SigningDetails UNKNOWN;
        public final Signature[] pastSigningCertificates;
        public final int[] pastSigningCertificatesFlags;
        public final ArraySet<PublicKey> publicKeys;
        @SignatureSchemeVersion
        public final int signatureSchemeVersion;
        public final Signature[] signatures;

        public static class Builder {
            private Signature[] mPastSigningCertificates;
            private int[] mPastSigningCertificatesFlags;
            private int mSignatureSchemeVersion = 0;
            private Signature[] mSignatures;

            public Builder setSignatures(Signature[] signatures) {
                this.mSignatures = signatures;
                return this;
            }

            public Builder setSignatureSchemeVersion(int signatureSchemeVersion) {
                this.mSignatureSchemeVersion = signatureSchemeVersion;
                return this;
            }

            public Builder setPastSigningCertificates(Signature[] pastSigningCertificates) {
                this.mPastSigningCertificates = pastSigningCertificates;
                return this;
            }

            public Builder setPastSigningCertificatesFlags(int[] pastSigningCertificatesFlags) {
                this.mPastSigningCertificatesFlags = pastSigningCertificatesFlags;
                return this;
            }

            private void checkInvariants() {
                if (this.mSignatures != null) {
                    boolean pastMismatch = false;
                    if (this.mPastSigningCertificates == null || this.mPastSigningCertificatesFlags == null) {
                        if (!(this.mPastSigningCertificates == null && this.mPastSigningCertificatesFlags == null)) {
                            pastMismatch = true;
                        }
                    } else if (this.mPastSigningCertificates.length != this.mPastSigningCertificatesFlags.length) {
                        pastMismatch = true;
                    }
                    if (pastMismatch) {
                        throw new IllegalStateException("SigningDetails must have a one to one mapping between pastSigningCertificates and pastSigningCertificatesFlags");
                    }
                    return;
                }
                throw new IllegalStateException("SigningDetails requires the current signing certificates.");
            }

            public SigningDetails build() throws CertificateException {
                checkInvariants();
                return new SigningDetails(this.mSignatures, this.mSignatureSchemeVersion, this.mPastSigningCertificates, this.mPastSigningCertificatesFlags);
            }
        }

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

        static {
            SigningDetails signingDetails = new SigningDetails(null, 0, null, null, null);
            UNKNOWN = signingDetails;
        }

        @VisibleForTesting
        public SigningDetails(Signature[] signatures2, @SignatureSchemeVersion int signatureSchemeVersion2, ArraySet<PublicKey> keys, Signature[] pastSigningCertificates2, int[] pastSigningCertificatesFlags2) {
            this.signatures = signatures2;
            this.signatureSchemeVersion = signatureSchemeVersion2;
            this.publicKeys = keys;
            this.pastSigningCertificates = pastSigningCertificates2;
            this.pastSigningCertificatesFlags = pastSigningCertificatesFlags2;
        }

        public SigningDetails(Signature[] signatures2, @SignatureSchemeVersion int signatureSchemeVersion2, Signature[] pastSigningCertificates2, int[] pastSigningCertificatesFlags2) throws CertificateException {
            this(signatures2, signatureSchemeVersion2, PackageParser.toSigningKeys(signatures2), pastSigningCertificates2, pastSigningCertificatesFlags2);
        }

        public SigningDetails(Signature[] signatures2, @SignatureSchemeVersion int signatureSchemeVersion2) throws CertificateException {
            this(signatures2, signatureSchemeVersion2, null, null);
        }

        public SigningDetails(SigningDetails orig) {
            if (orig != null) {
                if (orig.signatures != null) {
                    this.signatures = (Signature[]) orig.signatures.clone();
                } else {
                    this.signatures = null;
                }
                this.signatureSchemeVersion = orig.signatureSchemeVersion;
                this.publicKeys = new ArraySet<>(orig.publicKeys);
                if (orig.pastSigningCertificates != null) {
                    this.pastSigningCertificates = (Signature[]) orig.pastSigningCertificates.clone();
                    this.pastSigningCertificatesFlags = (int[]) orig.pastSigningCertificatesFlags.clone();
                    return;
                }
                this.pastSigningCertificates = null;
                this.pastSigningCertificatesFlags = null;
                return;
            }
            this.signatures = null;
            this.signatureSchemeVersion = 0;
            this.publicKeys = null;
            this.pastSigningCertificates = null;
            this.pastSigningCertificatesFlags = null;
        }

        public boolean hasSignatures() {
            return this.signatures != null && this.signatures.length > 0;
        }

        public boolean hasPastSigningCertificates() {
            return this.pastSigningCertificates != null && this.pastSigningCertificates.length > 0;
        }

        public boolean hasAncestorOrSelf(SigningDetails oldDetails) {
            if (this == UNKNOWN || oldDetails == UNKNOWN) {
                return false;
            }
            if (oldDetails.signatures.length > 1) {
                return signaturesMatchExactly(oldDetails);
            }
            return hasCertificate(oldDetails.signatures[0]);
        }

        public boolean hasAncestor(SigningDetails oldDetails) {
            if (this != UNKNOWN && oldDetails != UNKNOWN && hasPastSigningCertificates() && oldDetails.signatures.length == 1) {
                for (int i = 0; i < this.pastSigningCertificates.length - 1; i++) {
                    if (this.pastSigningCertificates[i].equals(oldDetails.signatures[i])) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean checkCapability(SigningDetails oldDetails, @CertCapabilities int flags) {
            if (this == UNKNOWN || oldDetails == UNKNOWN) {
                return false;
            }
            if (oldDetails.signatures.length > 1) {
                return signaturesMatchExactly(oldDetails);
            }
            return hasCertificate(oldDetails.signatures[0], flags);
        }

        public boolean checkCapabilityRecover(SigningDetails oldDetails, @CertCapabilities int flags) throws CertificateException {
            if (oldDetails == UNKNOWN || this == UNKNOWN) {
                return false;
            }
            if (!hasPastSigningCertificates() || oldDetails.signatures.length != 1) {
                return Signature.areEffectiveMatch(oldDetails.signatures, this.signatures);
            }
            for (int i = 0; i < this.pastSigningCertificates.length; i++) {
                if (Signature.areEffectiveMatch(oldDetails.signatures[0], this.pastSigningCertificates[i]) && this.pastSigningCertificatesFlags[i] == flags) {
                    return true;
                }
            }
            return false;
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
            boolean z = false;
            if (this == UNKNOWN) {
                return false;
            }
            if (hasPastSigningCertificates()) {
                for (int i = 0; i < this.pastSigningCertificates.length - 1; i++) {
                    if (this.pastSigningCertificates[i].equals(signature) && (flags == 0 || (this.pastSigningCertificatesFlags[i] & flags) == flags)) {
                        return true;
                    }
                }
            }
            if (this.signatures.length == 1 && this.signatures[0].equals(signature)) {
                z = true;
            }
            return z;
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
                for (int i = 0; i < this.pastSigningCertificates.length - 1; i++) {
                    if (Arrays.equals(sha256Certificate, PackageUtils.computeSha256DigestBytes(this.pastSigningCertificates[i].toByteArray())) && (flags == 0 || (this.pastSigningCertificatesFlags[i] & flags) == flags)) {
                        return true;
                    }
                }
            }
            if (this.signatures.length == 1) {
                return Arrays.equals(sha256Certificate, PackageUtils.computeSha256DigestBytes(this.signatures[0].toByteArray()));
            }
            return false;
        }

        public boolean signaturesMatchExactly(SigningDetails other) {
            return Signature.areExactMatch(this.signatures, other.signatures);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            boolean isUnknown = UNKNOWN == this;
            dest.writeBoolean(isUnknown);
            if (!isUnknown) {
                dest.writeTypedArray(this.signatures, flags);
                dest.writeInt(this.signatureSchemeVersion);
                dest.writeArraySet(this.publicKeys);
                dest.writeTypedArray(this.pastSigningCertificates, flags);
                dest.writeIntArray(this.pastSigningCertificatesFlags);
            }
        }

        protected SigningDetails(Parcel in) {
            ClassLoader boot = Object.class.getClassLoader();
            this.signatures = (Signature[]) in.createTypedArray(Signature.CREATOR);
            this.signatureSchemeVersion = in.readInt();
            this.publicKeys = in.readArraySet(boot);
            this.pastSigningCertificates = (Signature[]) in.createTypedArray(Signature.CREATOR);
            this.pastSigningCertificatesFlags = in.createIntArray();
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
            if (this.publicKeys != null) {
                if (!this.publicKeys.equals(that.publicKeys)) {
                    return false;
                }
            } else if (that.publicKeys != null) {
                return false;
            }
            if (Arrays.equals(this.pastSigningCertificates, that.pastSigningCertificates) && Arrays.equals(this.pastSigningCertificatesFlags, that.pastSigningCertificatesFlags)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * ((31 * Arrays.hashCode(this.signatures)) + this.signatureSchemeVersion)) + (this.publicKeys != null ? this.publicKeys.hashCode() : 0))) + Arrays.hashCode(this.pastSigningCertificates))) + Arrays.hashCode(this.pastSigningCertificatesFlags);
        }
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

    public static class SplitPermissionInfo {
        public final String[] newPerms;
        public final String rootPerm;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm2, String[] newPerms2, int targetSdk2) {
            this.rootPerm = rootPerm2;
            this.newPerms = newPerms2;
            this.targetSdk = targetSdk2;
        }
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

    public PackageParser() {
        this.mMetrics.setToDefaults();
        initFullScreenData();
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

    public void setCacheDir(File cacheDir) {
        this.mCacheDir = cacheDir;
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

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions, state, UserHandle.getCallingUserId());
    }

    private static boolean checkUseInstalledOrHidden(int flags, PackageUserState state, ApplicationInfo appInfo) {
        return state.isAvailable(flags) || !(appInfo == null || !appInfo.isSystemApp() || (4202496 & flags) == 0);
    }

    public static boolean isAvailable(PackageUserState state) {
        return checkUseInstalledOrHidden(0, state, null);
    }

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state, int userId) {
        Package packageR = p;
        int i = flags;
        Set<String> set = grantedPermissions;
        PackageUserState packageUserState = state;
        int i2 = userId;
        if (!checkUseInstalledOrHidden(i, packageUserState, packageR.applicationInfo) || !packageR.isMatch(i)) {
            int[] iArr = gids;
            long j = firstInstallTime;
            long j2 = lastUpdateTime;
            return null;
        }
        PackageInfo pi = new PackageInfo();
        pi.packageName = packageR.packageName;
        pi.splitNames = packageR.splitNames;
        pi.versionCode = packageR.mVersionCode;
        pi.versionCodeMajor = packageR.mVersionCodeMajor;
        pi.baseRevisionCode = packageR.baseRevisionCode;
        pi.splitRevisionCodes = packageR.splitRevisionCodes;
        pi.versionName = packageR.mVersionName;
        pi.sharedUserId = packageR.mSharedUserId;
        pi.sharedUserLabel = packageR.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(packageR, i, packageUserState, i2);
        pi.installLocation = packageR.installLocation;
        pi.isStub = packageR.isStub;
        pi.coreApp = packageR.coreApp;
        if (!((pi.applicationInfo.flags & 1) == 0 && (pi.applicationInfo.flags & 128) == 0)) {
            pi.requiredForAllUsers = packageR.mRequiredForAllUsers;
        }
        pi.restrictedAccountType = packageR.mRestrictedAccountType;
        pi.requiredAccountType = packageR.mRequiredAccountType;
        pi.overlayTarget = packageR.mOverlayTarget;
        pi.overlayCategory = packageR.mOverlayCategory;
        pi.overlayPriority = packageR.mOverlayPriority;
        pi.mOverlayIsStatic = packageR.mOverlayIsStatic;
        pi.compileSdkVersion = packageR.mCompileSdkVersion;
        pi.compileSdkVersionCodename = packageR.mCompileSdkVersionCodename;
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if ((i & 256) != 0) {
            pi.gids = gids;
        } else {
            int[] iArr2 = gids;
        }
        if ((i & 16384) != 0) {
            int N = packageR.configPreferences != null ? packageR.configPreferences.size() : 0;
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                packageR.configPreferences.toArray(pi.configPreferences);
            }
            int N2 = packageR.reqFeatures != null ? packageR.reqFeatures.size() : 0;
            if (N2 > 0) {
                pi.reqFeatures = new FeatureInfo[N2];
                packageR.reqFeatures.toArray(pi.reqFeatures);
            }
            int N3 = packageR.featureGroups != null ? packageR.featureGroups.size() : 0;
            if (N3 > 0) {
                pi.featureGroups = new FeatureGroupInfo[N3];
                packageR.featureGroups.toArray(pi.featureGroups);
            }
        }
        if ((i & 1) != 0) {
            int N4 = packageR.activities.size();
            if (N4 > 0) {
                ActivityInfo[] res = new ActivityInfo[N4];
                int num = 0;
                int i3 = 0;
                while (i3 < N4) {
                    Activity a = packageR.activities.get(i3);
                    int N5 = N4;
                    if (packageUserState.isMatch(a.info, i) != 0) {
                        res[num] = generateActivityInfo(a, i, packageUserState, i2);
                        num++;
                    }
                    i3++;
                    N4 = N5;
                }
                pi.activities = (ActivityInfo[]) ArrayUtils.trimToSize(res, num);
            }
        }
        if ((i & 2) != 0) {
            int N6 = packageR.receivers.size();
            if (N6 > 0) {
                ActivityInfo[] res2 = new ActivityInfo[N6];
                int num2 = 0;
                int i4 = 0;
                while (i4 < N6) {
                    Activity a2 = packageR.receivers.get(i4);
                    int N7 = N6;
                    if (packageUserState.isMatch(a2.info, i) != 0) {
                        res2[num2] = generateActivityInfo(a2, i, packageUserState, i2);
                        num2++;
                    }
                    i4++;
                    N6 = N7;
                }
                pi.receivers = (ActivityInfo[]) ArrayUtils.trimToSize(res2, num2);
            }
        }
        if ((i & 4) != 0) {
            int N8 = packageR.services.size();
            if (N8 > 0) {
                ServiceInfo[] res3 = new ServiceInfo[N8];
                int num3 = 0;
                int i5 = 0;
                while (i5 < N8) {
                    Service s = packageR.services.get(i5);
                    int N9 = N8;
                    if (packageUserState.isMatch(s.info, i) != 0) {
                        res3[num3] = generateServiceInfo(s, i, packageUserState, i2);
                        num3++;
                    }
                    i5++;
                    N8 = N9;
                }
                pi.services = (ServiceInfo[]) ArrayUtils.trimToSize(res3, num3);
            }
        }
        if ((i & 8) != 0) {
            int N10 = packageR.providers.size();
            if (N10 > 0) {
                ProviderInfo[] res4 = new ProviderInfo[N10];
                int num4 = 0;
                int i6 = 0;
                while (i6 < N10) {
                    Provider pr = packageR.providers.get(i6);
                    int N11 = N10;
                    if (packageUserState.isMatch(pr.info, i) != 0) {
                        res4[num4] = generateProviderInfo(pr, i, packageUserState, i2);
                        num4++;
                    }
                    i6++;
                    N10 = N11;
                }
                pi.providers = (ProviderInfo[]) ArrayUtils.trimToSize(res4, num4);
            }
        }
        if ((i & 16) != 0) {
            int N12 = packageR.instrumentation.size();
            if (N12 > 0) {
                pi.instrumentation = new InstrumentationInfo[N12];
                for (int i7 = 0; i7 < N12; i7++) {
                    pi.instrumentation[i7] = generateInstrumentationInfo(packageR.instrumentation.get(i7), i);
                }
            }
        }
        if ((i & 4096) != 0) {
            int N13 = packageR.permissions.size();
            if (N13 > 0) {
                pi.permissions = new PermissionInfo[N13];
                for (int i8 = 0; i8 < N13; i8++) {
                    pi.permissions[i8] = generatePermissionInfo(packageR.permissions.get(i8), i);
                }
            }
            int N14 = packageR.requestedPermissions.size();
            if (N14 > 0) {
                pi.requestedPermissions = new String[N14];
                pi.requestedPermissionsFlags = new int[N14];
                for (int i9 = 0; i9 < N14; i9++) {
                    String perm = packageR.requestedPermissions.get(i9);
                    pi.requestedPermissions[i9] = perm;
                    int[] iArr3 = pi.requestedPermissionsFlags;
                    iArr3[i9] = iArr3[i9] | 1;
                    if (set != null && set.contains(perm)) {
                        int[] iArr4 = pi.requestedPermissionsFlags;
                        iArr4[i9] = iArr4[i9] | 2;
                    }
                }
            }
        }
        if ((i & 64) != 0) {
            if (packageR.mSigningDetails.hasPastSigningCertificates()) {
                pi.signatures = new Signature[1];
                pi.signatures[0] = packageR.mSigningDetails.pastSigningCertificates[0];
            } else if (packageR.mRealSigningDetails.hasSignatures()) {
                int numberOfSigs = packageR.mRealSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs];
                System.arraycopy(packageR.mRealSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs);
            } else if (packageR.mSigningDetails.hasSignatures()) {
                int numberOfSigs2 = packageR.mSigningDetails.signatures.length;
                pi.signatures = new Signature[numberOfSigs2];
                System.arraycopy(packageR.mSigningDetails.signatures, 0, pi.signatures, 0, numberOfSigs2);
            }
        }
        if ((134217728 & i) != 0) {
            if (packageR.mRealSigningDetails.hasSignatures()) {
                pi.signingInfo = new SigningInfo(packageR.mRealSigningDetails);
            } else if (packageR.mSigningDetails != SigningDetails.UNKNOWN) {
                pi.signingInfo = new SigningInfo(packageR.mSigningDetails);
            } else {
                pi.signingInfo = null;
            }
        }
        return pi;
    }

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
        PackageLite packageLite = new PackageLite(packagePath, baseApk, null, null, null, null, null, null);
        return packageLite;
    }

    /* JADX WARNING: type inference failed for: r15v3, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    static PackageLite parseClusterPackageLite(File packageDir, int flags) throws PackageParserException {
        File[] files = packageDir.listFiles();
        if (!ArrayUtils.isEmpty(files)) {
            Trace.traceBegin(262144, "parseApkLite");
            ArrayMap<String, ApkLite> apks = new ArrayMap<>();
            int i = 0;
            int versionCode = 0;
            String packageName = null;
            for (File file : files) {
                if (isApkFile(file)) {
                    ApkLite lite = parseApkLite(file, flags);
                    HwFrameworkFactory.getHwPackageParser().needStopApp(lite.packageName, file);
                    if (packageName == null) {
                        packageName = lite.packageName;
                        versionCode = lite.versionCode;
                    } else if (!packageName.equals(lite.packageName)) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Inconsistent package " + lite.packageName + " in " + file + "; expected " + packageName);
                    } else if (versionCode != lite.versionCode && !lite.isPlugin) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Inconsistent version " + lite.versionCode + " in " + file + "; expected " + versionCode);
                    }
                    if (apks.put(lite.splitName, lite) != null) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Split name " + lite.splitName + " defined more than once; most recent was " + file);
                    }
                } else {
                    int i2 = flags;
                }
            }
            int i3 = flags;
            Trace.traceEnd(262144);
            ApkLite baseApk = apks.remove(null);
            if (baseApk != null) {
                int size = apks.size();
                String[] splitNames = null;
                boolean[] isFeatureSplits = null;
                String[] usesSplitNames = null;
                String[] configForSplits = null;
                String[] splitCodePaths = null;
                int[] splitVersionCodes = null;
                int[] splitRevisionCodes = null;
                if (size > 0) {
                    isFeatureSplits = new boolean[size];
                    usesSplitNames = new String[size];
                    configForSplits = new String[size];
                    splitCodePaths = new String[size];
                    splitVersionCodes = new int[size];
                    splitRevisionCodes = new int[size];
                    splitNames = apks.keySet().toArray(new String[size]);
                    Arrays.sort(splitNames, sSplitNameComparator);
                    while (i < size) {
                        ApkLite apk = apks.get(splitNames[i]);
                        usesSplitNames[i] = apk.usesSplitName;
                        isFeatureSplits[i] = apk.isFeatureSplit;
                        configForSplits[i] = apk.configForSplit;
                        splitCodePaths[i] = apk.codePath;
                        splitVersionCodes[i] = apk.versionCode;
                        splitRevisionCodes[i] = apk.revisionCode;
                        i++;
                        files = files;
                    }
                }
                PackageLite packageLite = new PackageLite(packageDir.getAbsolutePath(), baseApk, splitNames, isFeatureSplits, usesSplitNames, configForSplits, splitCodePaths, splitVersionCodes, splitRevisionCodes, baseApk.isPlugin);
                return packageLite;
            }
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Missing base APK in " + packageDir);
        }
        File file2 = packageDir;
        int i4 = flags;
        File[] fileArr = files;
        throw new PackageParserException(-100, "No packages found in split");
    }

    public Package parsePackage(File packageFile, int flags, boolean useCaches, int hwFlags) throws PackageParserException {
        Package parsed;
        Package parsed2 = useCaches ? getCachedResult(packageFile, flags) : null;
        if (parsed2 != null) {
            return parsed2;
        }
        long j = 0;
        long parseTime = LOG_PARSE_TIMINGS ? SystemClock.uptimeMillis() : 0;
        if (packageFile.isDirectory()) {
            parsed = parseClusterPackage(packageFile, flags, hwFlags);
        } else {
            parsed = parseMonolithicPackage(packageFile, flags, hwFlags);
        }
        if (LOG_PARSE_TIMINGS) {
            j = SystemClock.uptimeMillis();
        }
        long cacheTime = j;
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
        boolean z = false;
        try {
            if (Os.stat(packageFile.getAbsolutePath()).st_mtime < Os.stat(cacheFile.getAbsolutePath()).st_mtime) {
                z = true;
            }
            return z;
        } catch (ErrnoException ee) {
            if (ee.errno != OsConstants.ENOENT) {
                Slog.w("Error while stating package cache : ", ee);
            }
            return false;
        }
    }

    private Package getCachedResult(File packageFile, int flags) {
        if (this.mCacheDir == null) {
            return null;
        }
        File cacheFile = new File(this.mCacheDir, getCacheKey(packageFile, flags));
        try {
            if (!isCacheUpToDate(packageFile, cacheFile)) {
                return null;
            }
            Package p = fromCacheEntry(IoUtils.readFileAsByteArray(cacheFile.getAbsolutePath()));
            if (this.mCallback != null) {
                String[] overlayApks = this.mCallback.getOverlayApks(p.packageName);
                if (overlayApks != null && overlayApks.length > 0) {
                    for (String overlayApk : overlayApks) {
                        if (!isCacheUpToDate(new File(overlayApk), cacheFile)) {
                            return null;
                        }
                    }
                }
            }
            return p;
        } catch (Throwable e) {
            Slog.w(TAG, "Error reading package cache: ", e);
            cacheFile.delete();
            return null;
        }
    }

    private void cacheResult(File packageFile, int flags, Package parsed) {
        FileOutputStream fos;
        if (this.mCacheDir != null) {
            try {
                File cacheFile = new File(this.mCacheDir, getCacheKey(packageFile, flags));
                if (cacheFile.exists() && !cacheFile.delete()) {
                    Slog.e(TAG, "Unable to delete cache file: " + cacheFile);
                }
                byte[] cacheEntry = toCacheEntry(parsed);
                if (cacheEntry != null) {
                    try {
                        fos = new FileOutputStream(cacheFile);
                        fos.write(cacheEntry);
                        fos.close();
                    } catch (IOException ioe) {
                        Slog.w(TAG, "Error writing cache entry.", ioe);
                        cacheFile.delete();
                    } catch (Throwable th) {
                        r4.addSuppressed(th);
                    }
                    return;
                }
                return;
            } catch (Throwable e) {
                Slog.w(TAG, "Error saving package cache.", e);
            }
        } else {
            return;
        }
        throw th;
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
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, e.getMessage());
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
                        pkg.splitPrivateFlags = new int[num];
                        pkg.applicationInfo.splitNames = pkg.splitNames;
                        pkg.applicationInfo.splitDependencies = splitDependencies;
                        pkg.applicationInfo.splitClassLoaderNames = new String[num];
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

    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        return parseMonolithicPackage(apkFile, flags, 0);
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
        SplitAssetLoader assetLoader = new DefaultSplitAssetLoader(lite, flags);
        try {
            Package pkg = parseBaseApk(apkFile, assetLoader.getBaseAssetManager(), flags, hwFlags);
            HwFrameworkFactory.getHwPackageParser().needStopApp(pkg.packageName, apkFile);
            pkg.setCodePath(apkFile.getCanonicalPath());
            pkg.setUse32bitAbi(lite.use32bitAbi);
            IoUtils.closeQuietly(assetLoader);
            return pkg;
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
        XmlResourceParser parser;
        AssetManager assetManager = assets;
        String apkPath = apkFile.getAbsolutePath();
        String volumeUuid = null;
        if (apkPath.startsWith(MNT_EXPAND)) {
            volumeUuid = apkPath.substring(MNT_EXPAND.length(), apkPath.indexOf(47, MNT_EXPAND.length()));
        }
        String volumeUuid2 = volumeUuid;
        this.mParseError = 1;
        this.mArchiveSourcePath = apkFile.getAbsolutePath();
        XmlResourceParser parser2 = null;
        try {
            int cookie = assetManager.findCookieForPath(apkPath);
            if (cookie != 0) {
                parser = assetManager.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                try {
                    Package pkg = parseBaseApk(apkPath, new Resources(assetManager, this.mMetrics, null), parser, flags, new String[1], hwFlags);
                    if (pkg != null) {
                        pkg.setVolumeUuid(volumeUuid2);
                        pkg.setApplicationVolumeUuid(volumeUuid2);
                        pkg.setBaseCodePath(apkPath);
                        pkg.setSigningDetails(SigningDetails.UNKNOWN);
                        IoUtils.closeQuietly(parser);
                        return pkg;
                    }
                    throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
                } catch (PackageParserException e) {
                    e = e;
                    XmlResourceParser xmlResourceParser = parser;
                    throw e;
                } catch (Exception e2) {
                    e = e2;
                    parser2 = parser;
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
                } catch (Throwable th) {
                    e = th;
                    IoUtils.closeQuietly(parser);
                    throw e;
                }
            } else {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Failed adding asset path: " + apkPath);
            }
        } catch (PackageParserException e3) {
            e = e3;
            throw e;
        } catch (Exception e4) {
            e = e4;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (Throwable th2) {
            e = th2;
            parser = parser2;
            IoUtils.closeQuietly(parser);
            throw e;
        }
    }

    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags) throws PackageParserException {
        AssetManager assetManager = assets;
        Package packageR = pkg;
        String apkPath = packageR.splitCodePaths[splitIndex];
        this.mParseError = 1;
        this.mArchiveSourcePath = apkPath;
        XmlResourceParser parser = null;
        try {
            int cookie = assetManager.findCookieForPath(apkPath);
            if (cookie != 0) {
                XmlResourceParser parser2 = assetManager.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                try {
                    Package pkg2 = parseSplitApk(packageR, new Resources(assetManager, this.mMetrics, null), parser2, flags, splitIndex, new String[1]);
                    if (pkg2 != null) {
                        IoUtils.closeQuietly(parser2);
                        return;
                    }
                    try {
                        throw new PackageParserException(this.mParseError, apkPath + " (at " + parser2.getPositionDescription() + "): " + outError[0]);
                    } catch (PackageParserException e) {
                        e = e;
                        Package packageR2 = pkg2;
                        throw e;
                    } catch (Exception e2) {
                        e = e2;
                        Package packageR3 = pkg2;
                        parser = parser2;
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
                    } catch (Throwable th) {
                        e = th;
                        Package packageR4 = pkg2;
                        parser = parser2;
                        IoUtils.closeQuietly(parser);
                        throw e;
                    }
                } catch (PackageParserException e3) {
                    e = e3;
                    throw e;
                } catch (Exception e4) {
                    e = e4;
                    parser = parser2;
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
                } catch (Throwable th2) {
                    e = th2;
                    parser = parser2;
                    IoUtils.closeQuietly(parser);
                    throw e;
                }
            } else {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Failed adding asset path: " + apkPath);
            }
        } catch (PackageParserException e5) {
            e = e5;
            throw e;
        } catch (Exception e6) {
            e = e6;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (Throwable th3) {
            e = th3;
            IoUtils.closeQuietly(parser);
            throw e;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x007b  */
    private Package parseSplitApk(Package pkg, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException, PackageParserException {
        parsePackageSplitNames(parser, parser);
        this.mParseInstrumentationArgs = null;
        boolean foundApp = false;
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!foundApp) {
                    outError[0] = "<manifest> does not contain an <application>";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
                }
            } else if (!(type == 3 || type == 4)) {
                if (!parser.getName().equals(TAG_APPLICATION)) {
                    Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
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
        for (Signature publicKey : signatures) {
            keys.add(publicKey.getPublicKey());
        }
        return keys;
    }

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
                for (String file : pkg.splitCodePaths) {
                    collectCertificates(pkg, new File(file), skipVerify);
                }
            }
        } finally {
            Trace.traceEnd(262144);
        }
    }

    private static void collectCertificates(Package pkg, File apkFile, boolean skipVerify) throws PackageParserException {
        SigningDetails verified;
        String apkPath = apkFile.getAbsolutePath();
        int minSignatureScheme = 1;
        if (pkg.applicationInfo.isStaticSharedLibrary()) {
            minSignatureScheme = 2;
        }
        if (skipVerify) {
            verified = ApkSignatureVerifier.plsCertsNoVerifyOnlyCerts(apkPath, minSignatureScheme);
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

    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        android.util.Slog.w(TAG, "Failed to parse " + r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a4, code lost:
        throw new android.content.pm.PackageParser.PackageParserException(android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0015 A[Catch:{ all -> 0x004b, IOException -> 0x0017, RuntimeException | XmlPullParserException -> 0x0015, RuntimeException | XmlPullParserException -> 0x0015, RuntimeException | XmlPullParserException -> 0x0015, all -> 0x0012 }, ExcHandler: RuntimeException | XmlPullParserException (r1v6 'e' java.lang.Exception A[CUSTOM_DECLARE, Catch:{  }]), PHI: r2 
      PHI: (r2v2 'parser' android.content.res.XmlResourceParser) = (r2v0 'parser' android.content.res.XmlResourceParser), (r2v1 'parser' android.content.res.XmlResourceParser), (r2v1 'parser' android.content.res.XmlResourceParser), (r2v0 'parser' android.content.res.XmlResourceParser) binds: [B:15:0x0021, B:22:0x0040, B:24:0x0043, B:33:0x005c] A[DONT_GENERATE, DONT_INLINE], Splitter:B:15:0x0021] */
    private static ApkLite parseApkLiteInner(File apkFile, FileDescriptor fd, String debugPathName, int flags) throws PackageParserException {
        ApkAssets apkAssets;
        SigningDetails signingDetails;
        String apkPath = fd != null ? debugPathName : apkFile.getAbsolutePath();
        XmlResourceParser parser = null;
        boolean skipVerify = false;
        if (fd != null) {
            try {
                apkAssets = ApkAssets.loadFromFd(fd, debugPathName, false, false);
            } catch (IOException e) {
                throw new PackageParserException(-100, "Failed to parse " + apkPath);
            } catch (RuntimeException | XmlPullParserException e2) {
            } catch (Throwable th) {
                IoUtils.closeQuietly(parser);
                throw th;
            }
        } else {
            apkAssets = ApkAssets.loadFromPath(apkPath);
        }
        parser = apkAssets.openXml(ANDROID_MANIFEST_FILENAME);
        if ((flags & 32) != 0) {
            Package tempPkg = new Package((String) null);
            if ((flags & 16) != 0) {
                skipVerify = true;
            }
            Trace.traceBegin(262144, "collectCertificates");
            collectCertificates(tempPkg, apkFile, skipVerify);
            Trace.traceEnd(262144);
            signingDetails = tempPkg.mSigningDetails;
        } else {
            signingDetails = SigningDetails.UNKNOWN;
        }
        ApkLite parseApkLite = parseApkLite(apkPath, parser, parser, signingDetails);
        IoUtils.closeQuietly(parser);
        return parseApkLite;
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
        return (hasSep || !requireSeparator) ? null : "must have at least one '.' separator";
    }

    private static Pair<String, String> parsePackageSplitNames(XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException, PackageParserException {
        int type;
        String str;
        do {
            int next = parser.next();
            type = next;
            if (next == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No start tag found");
        } else if (parser.getName().equals(TAG_MANIFEST)) {
            String packageName = attrs.getAttributeValue(null, "package");
            if (!"android".equals(packageName) && !"androidhwext".equals(packageName) && !"featurelayerwidget".equals(packageName)) {
                String error = validateName(packageName, true, true);
                if (error != null) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest package: " + error);
                }
            }
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
            String error3 = packageName.intern();
            if (splitName != null) {
                str = splitName.intern();
            } else {
                str = splitName;
            }
            return Pair.create(error3, str);
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No <manifest> tag");
        }
    }

    private static ApkLite parseApkLite(String codePath, XmlPullParser parser, AttributeSet attrs, SigningDetails signingDetails) throws IOException, XmlPullParserException, PackageParserException {
        boolean isolatedSplits;
        int searchDepth;
        AttributeSet attributeSet = attrs;
        Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs);
        boolean debuggable = false;
        boolean multiArch = false;
        boolean use32bitAbi = false;
        boolean extractNativeLibs = true;
        String usesSplitName = null;
        boolean isFeatureSplit = false;
        String configForSplit = null;
        boolean isPlugin = false;
        boolean coreApp = false;
        boolean isolatedSplits2 = false;
        int versionCodeMajor = 0;
        int revisionCode = 0;
        int installLocation = -1;
        int versionCode = 0;
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attr = attributeSet.getAttributeName(i);
            if (attr.equals("installLocation")) {
                installLocation = attributeSet.getAttributeIntValue(i, -1);
            } else if (attr.equals(HwFrameworkMonitor.KEY_VERSION_CODE)) {
                versionCode = attributeSet.getAttributeIntValue(i, 0);
            } else if (attr.equals("versionCodeMajor")) {
                versionCodeMajor = attributeSet.getAttributeIntValue(i, 0);
            } else if (attr.equals("revisionCode")) {
                revisionCode = attributeSet.getAttributeIntValue(i, 0);
            } else if (attr.equals("coreApp")) {
                coreApp = attributeSet.getAttributeBooleanValue(i, false);
            } else if (attr.equals("isolatedSplits")) {
                isolatedSplits2 = attributeSet.getAttributeBooleanValue(i, false);
            } else if (attr.equals("configForSplit")) {
                configForSplit = attributeSet.getAttributeValue(i);
            } else if (attr.equals("isFeatureSplit")) {
                isFeatureSplit = attributeSet.getAttributeBooleanValue(i, false);
            } else if (attr.equals("isPlugin")) {
                isPlugin = attributeSet.getAttributeBooleanValue(i, false);
            }
        }
        int type = 1;
        int searchDepth2 = parser.getDepth() + 1;
        List<VerifierInfo> verifiers = new ArrayList<>();
        while (true) {
            isolatedSplits = isolatedSplits2;
            int next = parser.next();
            int type2 = next;
            if (next == type) {
                int i2 = type2;
                break;
            }
            int type3 = type2;
            if (type3 == 3 && parser.getDepth() < searchDepth2) {
                int i3 = searchDepth2;
                int i4 = type3;
                break;
            }
            if (type3 != 3) {
                if (type3 != 4 && parser.getDepth() == searchDepth2) {
                    searchDepth = searchDepth2;
                    if (TAG_PACKAGE_VERIFIER.equals(parser.getName()) != 0) {
                        VerifierInfo verifier = parseVerifier(attrs);
                        if (verifier != null) {
                            verifiers.add(verifier);
                        }
                    } else if (TAG_APPLICATION.equals(parser.getName())) {
                        int i5 = 0;
                        while (i5 < attrs.getAttributeCount()) {
                            String attr2 = attributeSet.getAttributeName(i5);
                            int type4 = type3;
                            if ("debuggable".equals(attr2) != 0) {
                                debuggable = attributeSet.getAttributeBooleanValue(i5, false);
                            }
                            if ("multiArch".equals(attr2)) {
                                multiArch = attributeSet.getAttributeBooleanValue(i5, false);
                            }
                            if ("use32bitAbi".equals(attr2)) {
                                use32bitAbi = attributeSet.getAttributeBooleanValue(i5, false);
                            }
                            if ("extractNativeLibs".equals(attr2)) {
                                extractNativeLibs = attributeSet.getAttributeBooleanValue(i5, true);
                            }
                            i5++;
                            type3 = type4;
                        }
                        isolatedSplits2 = isolatedSplits;
                        searchDepth2 = searchDepth;
                        type = 1;
                    } else {
                        type = 1;
                        if (TAG_USES_SPLIT.equals(parser.getName())) {
                            if (usesSplitName != null) {
                                Slog.w(TAG, "Only one <uses-split> permitted. Ignoring others.");
                            } else {
                                usesSplitName = attributeSet.getAttributeValue(ANDROID_RESOURCES, MidiDeviceInfo.PROPERTY_NAME);
                                if (usesSplitName == null) {
                                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "<uses-split> tag requires 'android:name' attribute");
                                }
                            }
                        }
                    }
                } else {
                    searchDepth = searchDepth2;
                }
                type = 1;
            } else {
                searchDepth = searchDepth2;
                type = 1;
            }
            isolatedSplits2 = isolatedSplits;
            searchDepth2 = searchDepth;
        }
        ApkLite apkLite = new ApkLite(codePath, (String) packageSplit.first, (String) packageSplit.second, isFeatureSplit, configForSplit, usesSplitName, versionCode, versionCodeMajor, revisionCode, installLocation, verifiers, signingDetails, coreApp, debuggable, multiArch, use32bitAbi, extractNativeLibs, isolatedSplits, isPlugin);
        return apkLite;
    }

    private boolean parseBaseApkChild(Package parentPkg, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        Package packageR = parentPkg;
        XmlResourceParser xmlResourceParser = parser;
        String childPackageName = xmlResourceParser.getAttributeValue(null, "package");
        if (validateName(childPackageName, true, false) != null) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return false;
        } else if (childPackageName.equals(packageR.packageName)) {
            String message = "Child package name cannot be equal to parent package name: " + packageR.packageName;
            Slog.w(TAG, message);
            outError[0] = message;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        } else if (packageR.hasChildPackage(childPackageName)) {
            String message2 = "Duplicate child package:" + childPackageName;
            Slog.w(TAG, message2);
            outError[0] = message2;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        } else {
            Package childPkg = new Package(childPackageName);
            childPkg.mVersionCode = packageR.mVersionCode;
            childPkg.baseRevisionCode = packageR.baseRevisionCode;
            childPkg.mVersionName = packageR.mVersionName;
            childPkg.applicationInfo.targetSdkVersion = packageR.applicationInfo.targetSdkVersion;
            childPkg.applicationInfo.minSdkVersion = packageR.applicationInfo.minSdkVersion;
            Package childPkg2 = parseBaseApkCommon(childPkg, CHILD_PACKAGE_TAGS, res, xmlResourceParser, flags, outError);
            if (childPkg2 == null) {
                return false;
            }
            if (packageR.childPackages == null) {
                packageR.childPackages = new ArrayList<>();
            }
            packageR.childPackages.add(childPkg2);
            childPkg2.parentPackage = packageR;
            return true;
        }
    }

    private Package parseBaseApk(String apkPath, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        return parseBaseApk(apkPath, res, parser, flags, outError, 0);
    }

    private Package parseBaseApk(String apkPath, Resources res, XmlResourceParser parser, int flags, String[] outError, int hwFlags) throws XmlPullParserException, IOException {
        XmlResourceParser xmlResourceParser = parser;
        try {
            Pair<String, String> packageSplit = parsePackageSplitNames(xmlResourceParser, xmlResourceParser);
            String pkgName = (String) packageSplit.first;
            if (!TextUtils.isEmpty((String) packageSplit.second)) {
                outError[0] = "Expected base APK, but found split " + splitName;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
                return null;
            }
            if (this.mCallback != null) {
                String[] overlayPaths = this.mCallback.getOverlayPaths(pkgName, apkPath);
                if (overlayPaths != null && overlayPaths.length > 0) {
                    for (String overlayPath : overlayPaths) {
                        res.getAssets().addOverlayPath(overlayPath);
                    }
                }
            } else {
                String str = apkPath;
            }
            Package pkg = new Package(pkgName);
            pkg.applicationInfo.hwFlags = hwFlags;
            Resources resources = res;
            TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifest);
            pkg.mVersionCode = sa.getInteger(1, 0);
            pkg.mVersionCodeMajor = sa.getInteger(11, 0);
            pkg.applicationInfo.setVersionCode(pkg.getLongVersionCode());
            pkg.baseRevisionCode = sa.getInteger(5, 0);
            pkg.mVersionName = sa.getNonConfigurationString(2, 0);
            if (pkg.mVersionName != null) {
                pkg.mVersionName = pkg.mVersionName.intern();
            }
            pkg.coreApp = xmlResourceParser.getAttributeBooleanValue(null, "coreApp", false);
            pkg.mCompileSdkVersion = sa.getInteger(9, 0);
            pkg.applicationInfo.compileSdkVersion = pkg.mCompileSdkVersion;
            pkg.mCompileSdkVersionCodename = sa.getNonConfigurationString(10, 0);
            if (pkg.mCompileSdkVersionCodename != null) {
                pkg.mCompileSdkVersionCodename = pkg.mCompileSdkVersionCodename.intern();
            }
            pkg.applicationInfo.compileSdkVersionCodename = pkg.mCompileSdkVersionCodename;
            sa.recycle();
            pkg.isPlugin = xmlResourceParser.getAttributeBooleanValue(null, "isPlugin", false);
            if (pkg.isPlugin) {
                ApplicationInfo applicationInfo = pkg.applicationInfo;
                applicationInfo.hw_extra_flags = 1 | applicationInfo.hw_extra_flags;
            }
            TypedArray typedArray = sa;
            return parseBaseApkCommon(pkg, null, resources, xmlResourceParser, flags, outError);
        } catch (PackageParserException e) {
            String str2 = apkPath;
            Resources resources2 = res;
            int i = hwFlags;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:85:0x02e0, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x0583  */
    /* JADX WARNING: Removed duplicated region for block: B:381:0x057d A[SYNTHETIC] */
    private Package parseBaseApkCommon(Package pkg, Set<String> acceptedTags, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        int supportsXLargeScreens;
        int supportsLargeScreens;
        int type;
        int resizeable;
        int anyDensity;
        int supportsNormalScreens;
        int i;
        int outerDepth;
        int anyDensity2;
        String str;
        int targetSandboxVersion;
        int supportsLargeScreens2;
        int resizeable2;
        int i2;
        int outerDepth2;
        int supportsXLargeScreens2;
        int supportsNormalScreens2;
        int type2;
        int supportsXLargeScreens3;
        int supportsLargeScreens3;
        int supportsXLargeScreens4;
        int supportsLargeScreens4;
        int anyDensity3;
        int supportsLargeScreens5;
        int i3;
        int anyDensity4;
        int supportsLargeScreens6;
        int i4;
        int resizeable3;
        int supportsXLargeScreens5;
        TypedArray sa;
        TypedArray sa2;
        int supportsLargeScreens7;
        int supportsSmallScreens;
        int supportsNormalScreens3;
        TypedArray sa3;
        int minVers;
        String minCode;
        int minSdkVersion;
        String minCode2;
        int type3;
        int innerDepth;
        int i5;
        Package packageR = pkg;
        Set<String> set = acceptedTags;
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        int supportsLargeScreens8 = flags;
        String[] strArr = outError;
        this.mParseInstrumentationArgs = null;
        TypedArray sa4 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifest);
        String str2 = sa4.getNonConfigurationString(0, 0);
        int i6 = 3;
        if (str2 != null && str2.length() > 0) {
            if (validateName(str2, true, false) == null || "android".equals(packageR.packageName) || "androidhwext".equals(packageR.packageName) || "featurelayerwidget".equals(packageR.packageName)) {
                packageR.mSharedUserId = str2.intern();
                packageR.mSharedUserLabel = sa4.getResourceId(3, 0);
            } else {
                strArr[0] = "<manifest> specifies bad sharedUserId name \"" + str2 + "\": " + nameError;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                return null;
            }
        }
        packageR.installLocation = sa4.getInteger(4, -1);
        packageR.applicationInfo.installLocation = packageR.installLocation;
        int targetSandboxVersion2 = sa4.getInteger(7, 1);
        packageR.applicationInfo.targetSandboxVersion = targetSandboxVersion2;
        if ((supportsLargeScreens8 & 4) != 0) {
            packageR.applicationInfo.privateFlags |= 4;
        }
        if ((supportsLargeScreens8 & 8) != 0) {
            packageR.applicationInfo.flags |= 262144;
        }
        if (sa4.getBoolean(6, false)) {
            packageR.applicationInfo.privateFlags |= 32768;
        }
        int outerDepth3 = parser.getDepth();
        int resizeable4 = 1;
        int anyDensity5 = 1;
        boolean foundApp = false;
        int supportsXLargeScreens6 = 1;
        TypedArray sa5 = sa4;
        int supportsLargeScreens9 = 1;
        int supportsSmallScreens2 = 1;
        int supportsNormalScreens4 = 1;
        while (true) {
            int outerDepth4 = outerDepth3;
            int next = parser.next();
            int type4 = next;
            if (next == 1) {
                supportsXLargeScreens = supportsXLargeScreens6;
                supportsLargeScreens = supportsLargeScreens9;
                type = supportsSmallScreens2;
                int i7 = targetSandboxVersion2;
                String str3 = str2;
                resizeable = resizeable4;
                anyDensity = anyDensity5;
                int i8 = outerDepth4;
                int i9 = type4;
                supportsNormalScreens = supportsNormalScreens4;
                break;
            }
            int type5 = type4;
            if (type5 == i6) {
                outerDepth = outerDepth4;
                if (parser.getDepth() <= outerDepth) {
                    supportsXLargeScreens = supportsXLargeScreens6;
                    supportsLargeScreens = supportsLargeScreens9;
                    int i10 = outerDepth;
                    int i11 = targetSandboxVersion2;
                    int i12 = type5;
                    String str4 = str2;
                    resizeable = resizeable4;
                    anyDensity = anyDensity5;
                    supportsNormalScreens = supportsNormalScreens4;
                    type = supportsSmallScreens2;
                    break;
                }
            } else {
                outerDepth = outerDepth4;
            }
            if (type5 != 3) {
                if (type5 == 4) {
                    supportsXLargeScreens2 = supportsXLargeScreens6;
                    supportsLargeScreens2 = supportsLargeScreens9;
                    type2 = supportsSmallScreens2;
                    outerDepth2 = outerDepth;
                    targetSandboxVersion = targetSandboxVersion2;
                    str = str2;
                    resizeable2 = resizeable4;
                    anyDensity2 = anyDensity5;
                } else {
                    targetSandboxVersion = targetSandboxVersion2;
                    String tagName = parser.getName();
                    if (set == null || set.contains(tagName)) {
                        supportsXLargeScreens3 = supportsXLargeScreens6;
                        supportsLargeScreens3 = supportsLargeScreens9;
                        if (!tagName.equals(TAG_APPLICATION)) {
                            outerDepth2 = outerDepth;
                            int i13 = type5;
                            str = str2;
                            supportsXLargeScreens4 = supportsXLargeScreens3;
                            supportsLargeScreens4 = supportsLargeScreens3;
                            supportsNormalScreens2 = supportsNormalScreens4;
                            type2 = supportsSmallScreens2;
                            if (tagName.equals("overlay")) {
                                TypedArray sa6 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestResourceOverlay);
                                packageR.mOverlayTarget = sa6.getString(1);
                                packageR.mOverlayCategory = sa6.getString(2);
                                packageR.mOverlayPriority = sa6.getInt(0, 0);
                                packageR.mOverlayIsStatic = sa6.getBoolean(3, false);
                                String propName = sa6.getString(4);
                                String propValue = sa6.getString(5);
                                sa6.recycle();
                                if (packageR.mOverlayTarget == null) {
                                    strArr[0] = "<overlay> does not specify a target package";
                                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return null;
                                } else if (packageR.mOverlayPriority < 0 || packageR.mOverlayPriority > 9999) {
                                    strArr[0] = "<overlay> priority must be between 0 and 9999";
                                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                } else if (!checkOverlayRequiredSystemProperty(propName, propValue)) {
                                    Slog.i(TAG, "Skipping target and overlay pair " + packageR.mOverlayTarget + " and " + packageR.baseCodePath + ": overlay ignored due to required system property: " + propName + " with value: " + propValue);
                                    return null;
                                } else {
                                    XmlUtils.skipCurrentTag(parser);
                                    sa5 = sa6;
                                    i5 = 3;
                                }
                            } else {
                                if (!tagName.equals(TAG_KEY_SETS)) {
                                    if (tagName.equals(TAG_PERMISSION_GROUP)) {
                                        i3 = 3;
                                        if (!parsePermissionGroup(packageR, supportsLargeScreens8, resources, xmlResourceParser, strArr)) {
                                            return null;
                                        }
                                    } else {
                                        i3 = 3;
                                        if (tagName.equals("permission")) {
                                            if (!parsePermission(packageR, resources, xmlResourceParser, strArr)) {
                                                return null;
                                            }
                                        } else if (tagName.equals(TAG_PERMISSION_TREE)) {
                                            if (!parsePermissionTree(packageR, resources, xmlResourceParser, strArr)) {
                                                return null;
                                            }
                                        } else if (!tagName.equals(TAG_USES_PERMISSION)) {
                                            if (tagName.equals(TAG_USES_PERMISSION_SDK_M)) {
                                                i4 = 3;
                                                resizeable3 = resizeable4;
                                                anyDensity4 = anyDensity5;
                                                supportsXLargeScreens5 = supportsXLargeScreens4;
                                                supportsLargeScreens6 = supportsLargeScreens4;
                                            } else if (tagName.equals(TAG_USES_PERMISSION_SDK_23)) {
                                                i4 = 3;
                                                resizeable3 = resizeable4;
                                                anyDensity4 = anyDensity5;
                                                supportsXLargeScreens5 = supportsXLargeScreens4;
                                                supportsLargeScreens6 = supportsLargeScreens4;
                                            } else if (tagName.equals(TAG_USES_CONFIGURATION)) {
                                                ConfigurationInfo cPref = new ConfigurationInfo();
                                                TypedArray sa7 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestUsesConfiguration);
                                                cPref.reqTouchScreen = sa7.getInt(0, 0);
                                                cPref.reqKeyboardType = sa7.getInt(1, 0);
                                                if (sa7.getBoolean(2, false)) {
                                                    cPref.reqInputFeatures |= 1;
                                                }
                                                cPref.reqNavigation = sa7.getInt(3, 0);
                                                if (sa7.getBoolean(4, false)) {
                                                    cPref.reqInputFeatures = 2 | cPref.reqInputFeatures;
                                                }
                                                sa7.recycle();
                                                packageR.configPreferences = ArrayUtils.add(packageR.configPreferences, cPref);
                                                XmlUtils.skipCurrentTag(parser);
                                                sa5 = sa7;
                                                i5 = 3;
                                            } else if (tagName.equals(TAG_USES_FEATURE)) {
                                                FeatureInfo fi = parseUsesFeature(resources, xmlResourceParser);
                                                packageR.reqFeatures = ArrayUtils.add(packageR.reqFeatures, fi);
                                                if (fi.name == null) {
                                                    ConfigurationInfo cPref2 = new ConfigurationInfo();
                                                    cPref2.reqGlEsVersion = fi.reqGlEsVersion;
                                                    packageR.configPreferences = ArrayUtils.add(packageR.configPreferences, cPref2);
                                                }
                                                XmlUtils.skipCurrentTag(parser);
                                            } else if (tagName.equals(TAG_FEATURE_GROUP)) {
                                                FeatureGroupInfo group = new FeatureGroupInfo();
                                                ArrayList<FeatureInfo> features = null;
                                                int innerDepth2 = parser.getDepth();
                                                while (true) {
                                                    int next2 = parser.next();
                                                    type3 = next2;
                                                    if (next2 != 1) {
                                                        if (type3 == i3 && parser.getDepth() <= innerDepth2) {
                                                            int i14 = innerDepth2;
                                                            break;
                                                        }
                                                        if (type3 == i3) {
                                                            innerDepth = innerDepth2;
                                                        } else if (type3 == 4) {
                                                            innerDepth = innerDepth2;
                                                        } else {
                                                            String innerTagName = parser.getName();
                                                            if (innerTagName.equals(TAG_USES_FEATURE)) {
                                                                FeatureInfo featureInfo = parseUsesFeature(resources, xmlResourceParser);
                                                                featureInfo.flags |= 1;
                                                                features = ArrayUtils.add(features, featureInfo);
                                                                innerDepth = innerDepth2;
                                                            } else {
                                                                StringBuilder sb = new StringBuilder();
                                                                innerDepth = innerDepth2;
                                                                sb.append("Unknown element under <feature-group>: ");
                                                                sb.append(innerTagName);
                                                                sb.append(" at ");
                                                                sb.append(this.mArchiveSourcePath);
                                                                sb.append(" ");
                                                                sb.append(parser.getPositionDescription());
                                                                Slog.w(TAG, sb.toString());
                                                            }
                                                            XmlUtils.skipCurrentTag(parser);
                                                        }
                                                        innerDepth2 = innerDepth;
                                                        i3 = 3;
                                                    } else {
                                                        int i15 = innerDepth2;
                                                        break;
                                                    }
                                                }
                                                if (features != null) {
                                                    group.features = new FeatureInfo[features.size()];
                                                    group.features = (FeatureInfo[]) features.toArray(group.features);
                                                }
                                                packageR.featureGroups = ArrayUtils.add(packageR.featureGroups, group);
                                                int i16 = type3;
                                                supportsSmallScreens2 = type2;
                                                supportsNormalScreens4 = supportsNormalScreens2;
                                            } else if (tagName.equals(TAG_USES_SDK)) {
                                                if (SDK_VERSION > 0) {
                                                    TypedArray sa8 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestUsesSdk);
                                                    String minCode3 = null;
                                                    int targetVers = 0;
                                                    String targetCode = null;
                                                    TypedValue val = sa8.peekValue(0);
                                                    if (val == null) {
                                                        minVers = 1;
                                                    } else if (val.type != 3 || val.string == null) {
                                                        int i17 = val.data;
                                                        minVers = i17;
                                                        targetVers = i17;
                                                    } else {
                                                        String charSequence = val.string.toString();
                                                        minCode3 = charSequence;
                                                        targetCode = charSequence;
                                                        minVers = 1;
                                                    }
                                                    TypedValue val2 = sa8.peekValue(1);
                                                    if (val2 != null) {
                                                        int targetVers2 = targetVers;
                                                        if (val2.type != 3 || val2.string == null) {
                                                            targetVers = val2.data;
                                                        } else {
                                                            targetCode = val2.string.toString();
                                                            if (minCode3 == null) {
                                                                minCode3 = targetCode;
                                                            }
                                                            targetVers = targetVers2;
                                                        }
                                                    } else {
                                                        int i18 = targetVers;
                                                    }
                                                    sa8.recycle();
                                                    if (packageR.packageName != null) {
                                                        sa3 = sa8;
                                                        if ((packageR.packageName.contains(".cts") || packageR.packageName.contains(".gts")) && (minVers > SDK_VERSION || targetVers > SDK_VERSION)) {
                                                            StringBuilder sb2 = new StringBuilder();
                                                            String str5 = minCode3;
                                                            sb2.append("cts pkg ");
                                                            sb2.append(packageR.packageName);
                                                            sb2.append(" minVers is :");
                                                            sb2.append(minVers);
                                                            sb2.append(",change to SDK_VERSION:");
                                                            sb2.append(SDK_VERSION);
                                                            Slog.w(TAG, sb2.toString());
                                                            targetCode = null;
                                                            minCode = null;
                                                            int i19 = SDK_VERSION;
                                                            targetVers = i19;
                                                            minVers = i19;
                                                            minSdkVersion = computeMinSdkVersion(minVers, minCode, SDK_VERSION, SDK_CODENAMES, strArr);
                                                            if (minSdkVersion >= 0) {
                                                                this.mParseError = -12;
                                                                return null;
                                                            }
                                                            boolean defaultToCurrentDevBranch = (supportsLargeScreens8 & 128) != 0;
                                                            String str6 = minCode;
                                                            int targetSdkVersion = computeTargetSdkVersion(targetVers, targetCode, SDK_CODENAMES, strArr, defaultToCurrentDevBranch);
                                                            if (targetSdkVersion < 0) {
                                                                boolean z = defaultToCurrentDevBranch;
                                                                this.mParseError = -12;
                                                                return null;
                                                            }
                                                            packageR.applicationInfo.minSdkVersion = minSdkVersion;
                                                            packageR.applicationInfo.targetSdkVersion = targetSdkVersion;
                                                        } else {
                                                            minCode2 = minCode3;
                                                        }
                                                    } else {
                                                        sa3 = sa8;
                                                        minCode2 = minCode3;
                                                    }
                                                    minCode = minCode2;
                                                    minSdkVersion = computeMinSdkVersion(minVers, minCode, SDK_VERSION, SDK_CODENAMES, strArr);
                                                    if (minSdkVersion >= 0) {
                                                    }
                                                } else {
                                                    sa3 = sa5;
                                                }
                                                XmlUtils.skipCurrentTag(parser);
                                                supportsSmallScreens2 = type2;
                                                supportsNormalScreens4 = supportsNormalScreens2;
                                                supportsLargeScreens9 = supportsLargeScreens4;
                                                sa5 = sa3;
                                                i2 = 3;
                                                outerDepth3 = outerDepth2;
                                                i6 = i2;
                                                targetSandboxVersion2 = targetSandboxVersion;
                                                str2 = str;
                                                supportsXLargeScreens6 = supportsXLargeScreens4;
                                                set = acceptedTags;
                                            } else {
                                                if (tagName.equals(TAG_SUPPORT_SCREENS)) {
                                                    TypedArray sa9 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestSupportsScreens);
                                                    packageR.applicationInfo.requiresSmallestWidthDp = sa9.getInteger(6, 0);
                                                    packageR.applicationInfo.compatibleWidthLimitDp = sa9.getInteger(7, 0);
                                                    packageR.applicationInfo.largestWidthLimitDp = sa9.getInteger(8, 0);
                                                    supportsSmallScreens = sa9.getInteger(1, type2);
                                                    supportsNormalScreens3 = sa9.getInteger(2, supportsNormalScreens2);
                                                    int supportsLargeScreens10 = sa9.getInteger(3, supportsLargeScreens4);
                                                    int supportsXLargeScreens7 = sa9.getInteger(5, supportsXLargeScreens4);
                                                    int resizeable5 = sa9.getInteger(4, resizeable4);
                                                    int anyDensity6 = sa9.getInteger(0, anyDensity5);
                                                    sa9.recycle();
                                                    XmlUtils.skipCurrentTag(parser);
                                                    sa5 = sa9;
                                                    supportsXLargeScreens4 = supportsXLargeScreens7;
                                                    supportsLargeScreens7 = supportsLargeScreens10;
                                                    anyDensity5 = anyDensity6;
                                                    i2 = 3;
                                                    resizeable4 = resizeable5;
                                                } else {
                                                    resizeable2 = resizeable4;
                                                    int anyDensity7 = anyDensity5;
                                                    supportsXLargeScreens2 = supportsXLargeScreens4;
                                                    int supportsLargeScreens11 = supportsLargeScreens4;
                                                    i2 = 3;
                                                    if (tagName.equals(TAG_PROTECTED_BROADCAST)) {
                                                        sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestProtectedBroadcast);
                                                        String name = sa2.getNonResourceString(0);
                                                        sa2.recycle();
                                                        if (name != null) {
                                                            if (packageR.protectedBroadcasts == null) {
                                                                packageR.protectedBroadcasts = new ArrayList<>();
                                                            }
                                                            if (!packageR.protectedBroadcasts.contains(name)) {
                                                                packageR.protectedBroadcasts.add(name.intern());
                                                            }
                                                        }
                                                        XmlUtils.skipCurrentTag(parser);
                                                    } else if (tagName.equals(TAG_INSTRUMENTATION)) {
                                                        if (parseInstrumentation(packageR, resources, xmlResourceParser, strArr) == null) {
                                                            return null;
                                                        }
                                                        supportsLargeScreens5 = supportsLargeScreens11;
                                                        anyDensity3 = anyDensity7;
                                                        supportsSmallScreens2 = type2;
                                                        supportsNormalScreens4 = supportsNormalScreens2;
                                                        supportsXLargeScreens4 = supportsXLargeScreens2;
                                                        resizeable4 = resizeable2;
                                                        supportsLargeScreens9 = supportsLargeScreens5;
                                                        anyDensity5 = anyDensity3;
                                                        outerDepth3 = outerDepth2;
                                                        i6 = i2;
                                                        targetSandboxVersion2 = targetSandboxVersion;
                                                        str2 = str;
                                                        supportsXLargeScreens6 = supportsXLargeScreens4;
                                                        set = acceptedTags;
                                                    } else if (tagName.equals(TAG_ORIGINAL_PACKAGE)) {
                                                        sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestOriginalPackage);
                                                        String orig = sa2.getNonConfigurationString(0, 0);
                                                        if (!packageR.packageName.equals(orig)) {
                                                            if (packageR.mOriginalPackages == null) {
                                                                packageR.mOriginalPackages = new ArrayList<>();
                                                                packageR.mRealPackage = packageR.packageName;
                                                            }
                                                            packageR.mOriginalPackages.add(orig);
                                                        }
                                                        sa2.recycle();
                                                        XmlUtils.skipCurrentTag(parser);
                                                    } else if (tagName.equals(TAG_ADOPT_PERMISSIONS)) {
                                                        sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestOriginalPackage);
                                                        String name2 = sa2.getNonConfigurationString(0, 0);
                                                        sa2.recycle();
                                                        if (name2 != null) {
                                                            if (packageR.mAdoptPermissions == null) {
                                                                packageR.mAdoptPermissions = new ArrayList<>();
                                                            }
                                                            packageR.mAdoptPermissions.add(name2);
                                                        }
                                                        XmlUtils.skipCurrentTag(parser);
                                                    } else {
                                                        if (tagName.equals(TAG_USES_GL_TEXTURE)) {
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else if (tagName.equals(TAG_COMPATIBLE_SCREENS)) {
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else if (tagName.equals(TAG_SUPPORTS_INPUT)) {
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else if (tagName.equals(TAG_EAT_COMMENT)) {
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else if (!tagName.equals("package")) {
                                                            supportsLargeScreens2 = supportsLargeScreens11;
                                                            anyDensity2 = anyDensity7;
                                                            if (tagName.equals(TAG_RESTRICT_UPDATE)) {
                                                                if ((supportsLargeScreens8 & 16) != 0) {
                                                                    TypedArray sa10 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestRestrictUpdate);
                                                                    String hash = sa10.getNonConfigurationString(0, 0);
                                                                    sa10.recycle();
                                                                    packageR.restrictUpdateHash = null;
                                                                    if (hash != null) {
                                                                        int hashLength = hash.length();
                                                                        byte[] hashBytes = new byte[(hashLength / 2)];
                                                                        int i20 = 0;
                                                                        while (i20 < hashLength) {
                                                                            hashBytes[i20 / 2] = (byte) ((Character.digit(hash.charAt(i20), 16) << 4) + Character.digit(hash.charAt(i20 + 1), 16));
                                                                            i20 += 2;
                                                                            sa10 = sa10;
                                                                            hashLength = hashLength;
                                                                        }
                                                                        sa = sa10;
                                                                        int i21 = hashLength;
                                                                        packageR.restrictUpdateHash = hashBytes;
                                                                    } else {
                                                                        sa = sa10;
                                                                    }
                                                                } else {
                                                                    sa = sa5;
                                                                }
                                                                XmlUtils.skipCurrentTag(parser);
                                                                supportsSmallScreens2 = type2;
                                                                supportsNormalScreens4 = supportsNormalScreens2;
                                                                supportsXLargeScreens4 = supportsXLargeScreens2;
                                                                resizeable4 = resizeable2;
                                                                supportsLargeScreens9 = supportsLargeScreens2;
                                                                anyDensity5 = anyDensity2;
                                                                sa5 = sa;
                                                                outerDepth3 = outerDepth2;
                                                                i6 = i2;
                                                                targetSandboxVersion2 = targetSandboxVersion;
                                                                str2 = str;
                                                                supportsXLargeScreens6 = supportsXLargeScreens4;
                                                                set = acceptedTags;
                                                            } else {
                                                                Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                                                XmlUtils.skipCurrentTag(parser);
                                                            }
                                                        } else if (!MULTI_PACKAGE_APK_ENABLED) {
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else {
                                                            supportsLargeScreens6 = supportsLargeScreens11;
                                                            anyDensity4 = anyDensity7;
                                                            if (!parseBaseApkChild(packageR, resources, xmlResourceParser, supportsLargeScreens8, strArr)) {
                                                                return null;
                                                            }
                                                            supportsSmallScreens2 = type2;
                                                            supportsNormalScreens4 = supportsNormalScreens2;
                                                            supportsXLargeScreens4 = supportsXLargeScreens2;
                                                            resizeable4 = resizeable2;
                                                            supportsLargeScreens9 = supportsLargeScreens5;
                                                            anyDensity5 = anyDensity3;
                                                            outerDepth3 = outerDepth2;
                                                            i6 = i2;
                                                            targetSandboxVersion2 = targetSandboxVersion;
                                                            str2 = str;
                                                            supportsXLargeScreens6 = supportsXLargeScreens4;
                                                            set = acceptedTags;
                                                        }
                                                        supportsLargeScreens2 = supportsLargeScreens11;
                                                        anyDensity2 = anyDensity7;
                                                    }
                                                    sa5 = sa2;
                                                    supportsLargeScreens7 = supportsLargeScreens11;
                                                    anyDensity5 = anyDensity7;
                                                    supportsSmallScreens = type2;
                                                    supportsNormalScreens3 = supportsNormalScreens2;
                                                    supportsXLargeScreens4 = supportsXLargeScreens2;
                                                    resizeable4 = resizeable2;
                                                }
                                                outerDepth3 = outerDepth2;
                                                i6 = i2;
                                                targetSandboxVersion2 = targetSandboxVersion;
                                                str2 = str;
                                                supportsXLargeScreens6 = supportsXLargeScreens4;
                                                set = acceptedTags;
                                            }
                                            if (!parseUsesPermission(packageR, resources, xmlResourceParser)) {
                                                return null;
                                            }
                                            supportsSmallScreens2 = type2;
                                            supportsNormalScreens4 = supportsNormalScreens2;
                                            supportsXLargeScreens4 = supportsXLargeScreens2;
                                            resizeable4 = resizeable2;
                                            supportsLargeScreens9 = supportsLargeScreens5;
                                            anyDensity5 = anyDensity3;
                                            outerDepth3 = outerDepth2;
                                            i6 = i2;
                                            targetSandboxVersion2 = targetSandboxVersion;
                                            str2 = str;
                                            supportsXLargeScreens6 = supportsXLargeScreens4;
                                            set = acceptedTags;
                                        } else if (!parseUsesPermission(packageR, resources, xmlResourceParser)) {
                                            return null;
                                        }
                                        i2 = 3;
                                    }
                                    i2 = i3;
                                    resizeable2 = resizeable4;
                                    anyDensity3 = anyDensity5;
                                    supportsXLargeScreens2 = supportsXLargeScreens4;
                                    supportsLargeScreens5 = supportsLargeScreens4;
                                    supportsSmallScreens2 = type2;
                                    supportsNormalScreens4 = supportsNormalScreens2;
                                    supportsXLargeScreens4 = supportsXLargeScreens2;
                                    resizeable4 = resizeable2;
                                    supportsLargeScreens9 = supportsLargeScreens5;
                                    anyDensity5 = anyDensity3;
                                    outerDepth3 = outerDepth2;
                                    i6 = i2;
                                    targetSandboxVersion2 = targetSandboxVersion;
                                    str2 = str;
                                    supportsXLargeScreens6 = supportsXLargeScreens4;
                                    set = acceptedTags;
                                } else if (!parseKeySets(packageR, resources, xmlResourceParser, strArr)) {
                                    return null;
                                } else {
                                    i2 = 3;
                                }
                                resizeable2 = resizeable4;
                                anyDensity3 = anyDensity5;
                                supportsXLargeScreens2 = supportsXLargeScreens4;
                                supportsLargeScreens5 = supportsLargeScreens4;
                                supportsSmallScreens2 = type2;
                                supportsNormalScreens4 = supportsNormalScreens2;
                                supportsXLargeScreens4 = supportsXLargeScreens2;
                                resizeable4 = resizeable2;
                                supportsLargeScreens9 = supportsLargeScreens5;
                                anyDensity5 = anyDensity3;
                                outerDepth3 = outerDepth2;
                                i6 = i2;
                                targetSandboxVersion2 = targetSandboxVersion;
                                str2 = str;
                                supportsXLargeScreens6 = supportsXLargeScreens4;
                                set = acceptedTags;
                            }
                            supportsSmallScreens = type2;
                            supportsNormalScreens3 = supportsNormalScreens2;
                            supportsLargeScreens7 = supportsLargeScreens4;
                            outerDepth3 = outerDepth2;
                            i6 = i2;
                            targetSandboxVersion2 = targetSandboxVersion;
                            str2 = str;
                            supportsXLargeScreens6 = supportsXLargeScreens4;
                            set = acceptedTags;
                        } else if (foundApp) {
                            Slog.w(TAG, "<manifest> has more than one <application>");
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            int i22 = type5;
                            str = str2;
                            int supportsNormalScreens5 = supportsNormalScreens4;
                            supportsLargeScreens4 = supportsLargeScreens3;
                            supportsXLargeScreens4 = supportsXLargeScreens3;
                            int supportsSmallScreens3 = supportsSmallScreens2;
                            outerDepth2 = outerDepth;
                            if (!parseBaseApplication(packageR, resources, xmlResourceParser, supportsLargeScreens8, strArr)) {
                                return null;
                            }
                            if (packageR.applicationInfo.minEmuiSdkVersion > CURRENT_EMUI_SDK_VERSION && CURRENT_EMUI_SDK_VERSION != 0) {
                                Slog.e(TAG, "package requires min EMUI sdk level=" + packageR.applicationInfo.minEmuiSdkVersion + ", current EMUI sdk level=" + CURRENT_EMUI_SDK_VERSION);
                                this.mParseError = -12;
                                return null;
                            } else if (packageR.applicationInfo.minEmuiSysImgVersion > sCurrentEmuiSysImgVersion) {
                                Slog.e(TAG, packageR.applicationInfo.packageName + " requires min system img version = " + packageR.applicationInfo.minEmuiSysImgVersion + ", current system img version = " + sCurrentEmuiSysImgVersion);
                                this.mParseError = -12;
                                return null;
                            } else {
                                supportsSmallScreens2 = supportsSmallScreens3;
                                supportsNormalScreens4 = supportsNormalScreens5;
                                foundApp = true;
                            }
                        }
                        supportsLargeScreens9 = supportsLargeScreens4;
                        i2 = 3;
                        outerDepth3 = outerDepth2;
                        i6 = i2;
                        targetSandboxVersion2 = targetSandboxVersion;
                        str2 = str;
                        supportsXLargeScreens6 = supportsXLargeScreens4;
                        set = acceptedTags;
                    } else {
                        supportsXLargeScreens3 = supportsXLargeScreens6;
                        StringBuilder sb3 = new StringBuilder();
                        supportsLargeScreens3 = supportsLargeScreens9;
                        sb3.append("Skipping unsupported element under <manifest>: ");
                        sb3.append(tagName);
                        sb3.append(" at ");
                        sb3.append(this.mArchiveSourcePath);
                        sb3.append(" ");
                        sb3.append(parser.getPositionDescription());
                        Slog.w(TAG, sb3.toString());
                        XmlUtils.skipCurrentTag(parser);
                    }
                    type2 = supportsSmallScreens2;
                    outerDepth2 = outerDepth;
                    str = str2;
                    resizeable2 = resizeable4;
                    anyDensity2 = anyDensity5;
                    supportsXLargeScreens2 = supportsXLargeScreens3;
                    supportsLargeScreens2 = supportsLargeScreens3;
                }
                i2 = 3;
                supportsNormalScreens2 = supportsNormalScreens4;
            } else {
                supportsXLargeScreens2 = supportsXLargeScreens6;
                supportsLargeScreens2 = supportsLargeScreens9;
                type2 = supportsSmallScreens2;
                i2 = 3;
                outerDepth2 = outerDepth;
                targetSandboxVersion = targetSandboxVersion2;
                str = str2;
                resizeable2 = resizeable4;
                anyDensity2 = anyDensity5;
                supportsNormalScreens2 = supportsNormalScreens4;
            }
            supportsSmallScreens2 = type2;
            supportsNormalScreens4 = supportsNormalScreens2;
            supportsXLargeScreens6 = supportsXLargeScreens2;
            outerDepth3 = outerDepth2;
            i6 = i2;
            resizeable4 = resizeable2;
            supportsLargeScreens9 = supportsLargeScreens2;
            targetSandboxVersion2 = targetSandboxVersion;
            str2 = str;
            anyDensity5 = anyDensity2;
            set = acceptedTags;
        }
        if (foundApp || packageR.instrumentation.size() != 0) {
            i = 0;
        } else {
            i = 0;
            strArr[0] = "<manifest> does not contain an <application> or <instrumentation>";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
        }
        int NP = NEW_PERMISSIONS.length;
        StringBuilder implicitPerms = null;
        for (int ip = i; ip < NP; ip++) {
            NewPermissionInfo npi = NEW_PERMISSIONS[ip];
            if (packageR.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
                break;
            }
            if (!packageR.requestedPermissions.contains(npi.name)) {
                if (implicitPerms == null) {
                    implicitPerms = new StringBuilder(128);
                    implicitPerms.append(packageR.packageName);
                    implicitPerms.append(": compat added ");
                } else {
                    implicitPerms.append(' ');
                }
                implicitPerms.append(npi.name);
                packageR.requestedPermissions.add(npi.name);
            }
        }
        if (implicitPerms != null) {
            Slog.i(TAG, implicitPerms.toString());
        }
        int NS = SPLIT_PERMISSIONS.length;
        int is = i;
        while (is < NS) {
            SplitPermissionInfo spi = SPLIT_PERMISSIONS[is];
            if (packageR.applicationInfo.targetSdkVersion < spi.targetSdk && packageR.requestedPermissions.contains(spi.rootPerm)) {
                for (int in = i; in < spi.newPerms.length; in++) {
                    String perm = spi.newPerms[in];
                    if (!packageR.requestedPermissions.contains(perm)) {
                        packageR.requestedPermissions.add(perm);
                    }
                }
            }
            is++;
            i = 0;
        }
        if (type < 0 || (type > 0 && packageR.applicationInfo.targetSdkVersion >= 4)) {
            packageR.applicationInfo.flags |= 512;
        }
        if (supportsNormalScreens != 0) {
            packageR.applicationInfo.flags |= 1024;
        }
        if (supportsLargeScreens < 0 || (supportsLargeScreens > 0 && packageR.applicationInfo.targetSdkVersion >= 4)) {
            packageR.applicationInfo.flags |= 2048;
        }
        if (supportsXLargeScreens < 0 || (supportsXLargeScreens > 0 && packageR.applicationInfo.targetSdkVersion >= 9)) {
            packageR.applicationInfo.flags |= 524288;
        }
        if (resizeable < 0 || (resizeable > 0 && packageR.applicationInfo.targetSdkVersion >= 4)) {
            packageR.applicationInfo.flags |= 4096;
        }
        if (anyDensity < 0 || (anyDensity > 0 && packageR.applicationInfo.targetSdkVersion >= 4)) {
            packageR.applicationInfo.flags |= 8192;
        }
        if (packageR.applicationInfo.usesCompatibilityMode()) {
            adjustPackageToBeUnresizeableAndUnpipable(pkg);
        }
        return packageR;
    }

    private boolean checkOverlayRequiredSystemProperty(String propName, String propValue) {
        boolean z = false;
        if (!TextUtils.isEmpty(propName) && !TextUtils.isEmpty(propValue)) {
            String currValue = SystemProperties.get(propName);
            if (currValue != null && currValue.equals(propValue)) {
                z = true;
            }
            return z;
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

    public static int computeTargetSdkVersion(int targetVers, String targetCode, String[] platformSdkCodenames, String[] outError, boolean forceCurrentDev) {
        if (targetCode == null) {
            return targetVers;
        }
        if (ArrayUtils.contains(platformSdkCodenames, targetCode) || forceCurrentDev) {
            return 10000;
        }
        if (platformSdkCodenames.length > 0) {
            outError[0] = "Requires development platform " + targetCode + " (current platform is any of " + Arrays.toString(platformSdkCodenames) + ")";
        } else {
            outError[0] = "Requires development platform " + targetCode + " but this is a release platform.";
        }
        return -1;
    }

    public static int computeMinSdkVersion(int minVers, String minCode, int platformSdkVersion, String[] platformSdkCodenames, String[] outError) {
        if (minCode == null) {
            if (minVers <= platformSdkVersion) {
                return minVers;
            }
            outError[0] = "Requires newer sdk version #" + minVers + " (current version is #" + platformSdkVersion + ")";
            return -1;
        } else if (ArrayUtils.contains(platformSdkCodenames, minCode)) {
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
            String certSha256Digest2 = certSha256Digest.replace(":", "").toLowerCase();
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

    /* JADX WARNING: type inference failed for: r6v2, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    private String[] parseAdditionalCertificates(Resources resources, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        String[] certSha256Digests = EmptyArray.STRING;
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
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
                    certSha256Digests = ArrayUtils.appendElement(String.class, certSha256Digests, certSha256Digest.replace(":", "").toLowerCase());
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return certSha256Digests;
    }

    private boolean parseUsesPermission(Package pkg, Resources res, XmlResourceParser parser) throws XmlPullParserException, IOException {
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
        if (requiredFeature != null && this.mCallback != null && !this.mCallback.hasFeature(requiredFeature)) {
            return true;
        }
        if (requiredNotfeature != null && this.mCallback != null && this.mCallback.hasFeature(requiredNotfeature)) {
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
            if (nameError == null || HwThemeManager.HWT_USER_SYSTEM.equals(proc)) {
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
        if ((flags & 2) == 0 || HwThemeManager.HWT_USER_SYSTEM.equals(procSeq)) {
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
            return TextUtils.safeIntern(buildCompoundName(pkg, procSeq, "process", outError));
        }
        return defProc != null ? defProc : pkg;
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
        Set<String> publicKeyNames;
        int currentKeySetDepth;
        int outerDepth;
        Package packageR = owner;
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        int outerDepth2 = parser.getDepth();
        int currentKeySetDepth2 = -1;
        String currentKeySet = null;
        ArrayMap<String, PublicKey> publicKeys = new ArrayMap<>();
        ArraySet<String> upgradeKeySets = new ArraySet<>();
        ArrayMap<String, ArraySet<String>> definedKeySets = new ArrayMap<>();
        ArraySet<String> improperKeySets = new ArraySet<>();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next != 1) {
                if (type == 3 && parser.getDepth() <= outerDepth2) {
                    int i = outerDepth2;
                    int i2 = currentKeySetDepth2;
                    int i3 = type;
                    break;
                }
                if (type != 3) {
                    String tagName = parser.getName();
                    if (!tagName.equals("key-set")) {
                        outerDepth = outerDepth2;
                        if (!tagName.equals("public-key")) {
                            currentKeySetDepth = currentKeySetDepth2;
                            int i4 = type;
                            if (tagName.equals("upgrade-key-set")) {
                                TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestUpgradeKeySet);
                                upgradeKeySets.add(sa.getNonResourceString(0));
                                sa.recycle();
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                Slog.w(TAG, "Unknown element under <key-sets>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        } else if (currentKeySet == null) {
                            outError[0] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return false;
                        } else {
                            TypedArray sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestPublicKey);
                            String publicKeyName = sa2.getNonResourceString(0);
                            String encodedKey = sa2.getNonResourceString(1);
                            if (encodedKey == null && publicKeys.get(publicKeyName) == null) {
                                int i5 = currentKeySetDepth2;
                                StringBuilder sb = new StringBuilder();
                                int i6 = type;
                                sb.append("'public-key' ");
                                sb.append(publicKeyName);
                                sb.append(" must define a public-key value on first use at ");
                                sb.append(parser.getPositionDescription());
                                outError[0] = sb.toString();
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                sa2.recycle();
                                return false;
                            }
                            currentKeySetDepth = currentKeySetDepth2;
                            int i7 = type;
                            if (encodedKey != null) {
                                PublicKey currentKey = parsePublicKey(encodedKey);
                                if (currentKey == null) {
                                    String str = encodedKey;
                                    Slog.w(TAG, "No recognized valid key in 'public-key' tag at " + parser.getPositionDescription() + " key-set " + currentKeySet + " will not be added to the package's defined key-sets.");
                                    sa2.recycle();
                                    improperKeySets.add(currentKeySet);
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    if (publicKeys.get(publicKeyName) == null || publicKeys.get(publicKeyName).equals(currentKey)) {
                                        publicKeys.put(publicKeyName, currentKey);
                                    } else {
                                        outError[0] = "Value of 'public-key' " + publicKeyName + " conflicts with previously defined value at " + parser.getPositionDescription();
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        sa2.recycle();
                                        return false;
                                    }
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
                        TypedArray sa3 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestKeySet);
                        String keysetName = sa3.getNonResourceString(0);
                        outerDepth = outerDepth2;
                        definedKeySets.put(keysetName, new ArraySet());
                        currentKeySetDepth2 = parser.getDepth();
                        sa3.recycle();
                        currentKeySet = keysetName;
                        int i8 = type;
                    }
                    outerDepth2 = outerDepth;
                    Package packageR2 = owner;
                } else if (parser.getDepth() == currentKeySetDepth2) {
                    currentKeySet = null;
                    currentKeySetDepth2 = -1;
                } else {
                    outerDepth = outerDepth2;
                    currentKeySetDepth = currentKeySetDepth2;
                }
                outerDepth2 = outerDepth;
                currentKeySetDepth2 = currentKeySetDepth;
                Package packageR3 = owner;
            } else {
                int i9 = currentKeySetDepth2;
                int i10 = type;
                break;
            }
        }
        Set<String> publicKeyNames2 = publicKeys.keySet();
        if (publicKeyNames2.removeAll(definedKeySets.keySet())) {
            outError[0] = "Package" + owner.packageName + " AndroidManifext.xml 'key-set' and 'public-key' names must be distinct.";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        Package packageR4 = owner;
        packageR4.mKeySetMapping = new ArrayMap<>();
        for (Map.Entry<String, ArraySet<String>> e : definedKeySets.entrySet()) {
            String keySetName = e.getKey();
            if (e.getValue().size() == 0) {
                StringBuilder sb2 = new StringBuilder();
                publicKeyNames = publicKeyNames2;
                sb2.append("Package");
                sb2.append(packageR4.packageName);
                sb2.append(" AndroidManifext.xml 'key-set' ");
                sb2.append(keySetName);
                sb2.append(" has no valid associated 'public-key'. Not including in package's defined key-sets.");
                Slog.w(TAG, sb2.toString());
            } else {
                publicKeyNames = publicKeyNames2;
                if (improperKeySets.contains(keySetName)) {
                    Slog.w(TAG, "Package" + packageR4.packageName + " AndroidManifext.xml 'key-set' " + keySetName + " contained improper 'public-key' tags. Not including in package's defined key-sets.");
                } else {
                    packageR4.mKeySetMapping.put(keySetName, new ArraySet());
                    for (Iterator it = e.getValue().iterator(); it.hasNext(); it = it) {
                        packageR4.mKeySetMapping.get(keySetName).add(publicKeys.get((String) it.next()));
                    }
                }
            }
            publicKeyNames2 = publicKeyNames;
        }
        if (packageR4.mKeySetMapping.keySet().containsAll(upgradeKeySets)) {
            packageR4.mUpgradeKeySets = upgradeKeySets;
            return true;
        }
        outError[0] = "Package" + packageR4.packageName + " AndroidManifext.xml does not define all 'upgrade-key-set's .";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    private boolean parsePermissionGroup(Package owner, int flags, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Package packageR = owner;
        PermissionGroup perm = new PermissionGroup(packageR);
        Resources resources = res;
        TypedArray sa = resources.obtainAttributes(parser, R.styleable.AndroidManifestPermissionGroup);
        if (!parsePackageItemInfo(packageR, perm.info, outError, "<permission-group>", sa, true, 2, 0, 1, 8, 5, 7)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        perm.info.descriptionRes = sa.getResourceId(4, 0);
        perm.info.requestRes = sa.getResourceId(9, 0);
        perm.info.flags = sa.getInt(6, 0);
        perm.info.priority = sa.getInt(3, 0);
        sa.recycle();
        TypedArray typedArray = sa;
        PermissionGroup perm2 = perm;
        Package packageR2 = packageR;
        if (!parseAllMetaData(resources, parser, "<permission-group>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        packageR2.permissionGroups.add(perm2);
        return true;
    }

    private boolean parsePermission(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Package packageR = owner;
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestPermission);
        Permission perm = new Permission(packageR);
        if (!parsePackageItemInfo(packageR, perm.info, outError, "<permission>", sa, true, 2, 0, 1, 9, 6, 8)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        perm.info.group = sa.getNonResourceString(4);
        if (perm.info.group != null) {
            perm.info.group = perm.info.group.intern();
        }
        perm.info.descriptionRes = sa.getResourceId(5, 0);
        perm.info.requestRes = sa.getResourceId(10, 0);
        perm.info.protectionLevel = sa.getInt(3, 0);
        perm.info.flags = sa.getInt(7, 0);
        sa.recycle();
        if (perm.info.protectionLevel == -1) {
            outError[0] = "<permission> does not specify protectionLevel";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        perm.info.protectionLevel = PermissionInfo.fixProtectionLevel(perm.info.protectionLevel);
        if (perm.info.getProtectionFlags() == 0 || (perm.info.protectionLevel & 4096) != 0 || (perm.info.protectionLevel & 8192) != 0 || (perm.info.protectionLevel & 15) == 2) {
            Permission perm2 = perm;
            TypedArray typedArray = sa;
            Package packageR2 = packageR;
            if (!parseAllMetaData(resources, xmlResourceParser, "<permission>", perm2, outError)) {
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            }
            packageR2.permissions.add(perm2);
            return true;
        }
        outError[0] = "<permission>  protectionLevel specifies a non-instant flag but is not based on signature type";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    private boolean parsePermissionTree(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Package packageR = owner;
        Permission perm = new Permission(packageR);
        Resources resources = res;
        TypedArray sa = resources.obtainAttributes(parser, R.styleable.AndroidManifestPermissionTree);
        if (!parsePackageItemInfo(packageR, perm.info, outError, "<permission-tree>", sa, true, 2, 0, 1, 5, 3, 4)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        sa.recycle();
        int index = perm.info.name.indexOf(46);
        if (index > 0) {
            index = perm.info.name.indexOf(46, index + 1);
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
        TypedArray typedArray = sa;
        Permission perm2 = perm;
        Package packageR2 = packageR;
        if (!parseAllMetaData(resources, parser, "<permission-tree>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        packageR2.permissions.add(perm2);
        return true;
    }

    private Instrumentation parseInstrumentation(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestInstrumentation);
        if (this.mParseInstrumentationArgs == null) {
            ParsePackageItemArgs parsePackageItemArgs = new ParsePackageItemArgs(owner, outError, 2, 0, 1, 8, 6, 7);
            this.mParseInstrumentationArgs = parsePackageItemArgs;
            this.mParseInstrumentationArgs.tag = "<instrumentation>";
        }
        this.mParseInstrumentationArgs.sa = sa;
        Instrumentation a = new Instrumentation(this.mParseInstrumentationArgs, new InstrumentationInfo());
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
        }
        TypedArray typedArray = sa;
        if (!parseAllMetaData(resources, xmlResourceParser, "<instrumentation>", a, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        owner.instrumentation.add(a);
        return a;
    }

    /* JADX WARNING: type inference failed for: r1v22, types: [boolean] */
    /* JADX WARNING: type inference failed for: r1v50 */
    /* JADX WARNING: type inference failed for: r1v53 */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x0608, code lost:
        r9[0] = "Bad static-library declaration name: " + r10 + " version: " + r13;
        r0.mParseError = android.content.pm.PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        com.android.internal.util.XmlUtils.skipCurrentTag(r37);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x062b, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x078a, code lost:
        if (r10.metaData.getBoolean(METADATA_NOTCH_SUPPORT, false) != false) goto L_0x0790;
     */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x07b2  */
    /* JADX WARNING: Removed duplicated region for block: B:358:0x07b6 A[SYNTHETIC] */
    private boolean parseBaseApplication(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        String pkgName;
        String str;
        String pkgName2;
        boolean z;
        int i;
        ? r1;
        boolean hasServiceOrder;
        int innerDepth;
        String str2;
        String restrictedAccountType;
        String restrictedAccountType2;
        String[] strArr;
        Resources resources;
        ApplicationInfo ai;
        PackageParser packageParser;
        TypedArray sa;
        String[] strArr2;
        CharSequence pname;
        PackageParser packageParser2 = this;
        Package packageR = owner;
        Resources resources2 = res;
        String[] strArr3 = outError;
        ApplicationInfo ai2 = packageR.applicationInfo;
        String pkgName3 = packageR.applicationInfo.packageName;
        TypedArray sa2 = resources2.obtainAttributes(parser, R.styleable.AndroidManifestApplication);
        TypedArray sa3 = sa2;
        String pkgName4 = pkgName3;
        ApplicationInfo ai3 = ai2;
        String[] strArr4 = strArr3;
        if (!parsePackageItemInfo(packageR, ai2, strArr3, "<application>", sa2, false, 3, 1, 2, 42, 22, 30)) {
            sa3.recycle();
            packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        TypedArray sa4 = sa3;
        ApplicationInfo ai4 = ai3;
        if (ai4.name != null) {
            ai4.className = ai4.name;
        }
        String manageSpaceActivity = sa4.getNonConfigurationString(4, 1024);
        if (manageSpaceActivity != null) {
            pkgName = pkgName4;
            ai4.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity, strArr4);
        } else {
            pkgName = pkgName4;
        }
        if (sa4.getBoolean(17, true)) {
            ai4.flags |= 32768;
            String backupAgent = sa4.getNonConfigurationString(16, 1024);
            if (backupAgent != null) {
                ai4.backupAgentName = buildClassName(pkgName, backupAgent, strArr4);
                if (sa4.getBoolean(18, true)) {
                    ai4.flags |= 65536;
                }
                if (sa4.getBoolean(21, false)) {
                    ai4.flags |= 131072;
                }
                if (sa4.getBoolean(32, false)) {
                    ai4.flags |= 67108864;
                }
                if (sa4.getBoolean(40, false)) {
                    ai4.privateFlags |= 8192;
                }
            }
            TypedValue v = sa4.peekValue(35);
            if (v != null) {
                int i2 = v.resourceId;
                ai4.fullBackupContent = i2;
                if (i2 == 0) {
                    ai4.fullBackupContent = v.data == 0 ? -1 : 0;
                }
            }
        }
        ai4.theme = sa4.getResourceId(0, 0);
        if ("com.google.android.packageinstaller".equals(pkgName)) {
            ai4.theme = 33951745;
            Flog.i(206, "parseBaseApplication, packageinstaller new themeName = " + resources2.getResourceName(ai4.theme));
        }
        ai4.descriptionRes = sa4.getResourceId(13, 0);
        packageR.mPersistentApp = sa4.getBoolean(8, false);
        if (packageR.mPersistentApp) {
            String requiredFeature = sa4.getNonResourceString(45);
            if (requiredFeature == null || packageParser2.mCallback.hasFeature(requiredFeature)) {
                ai4.flags |= 8;
            }
        }
        if (sa4.getBoolean(27, false)) {
            packageR.mRequiredForAllUsers = true;
        }
        String restrictedAccountType3 = sa4.getString(28);
        if (restrictedAccountType3 != null && restrictedAccountType3.length() > 0) {
            packageR.mRestrictedAccountType = restrictedAccountType3;
        }
        String requiredAccountType = sa4.getString(29);
        if (requiredAccountType != null && requiredAccountType.length() > 0) {
            packageR.mRequiredAccountType = requiredAccountType;
        }
        if (sa4.getBoolean(10, false)) {
            ai4.flags |= 2;
        }
        if (sa4.getBoolean(20, false)) {
            ai4.flags |= 16384;
        }
        packageR.baseHardwareAccelerated = sa4.getBoolean(23, packageR.applicationInfo.targetSdkVersion >= 14);
        if (packageR.baseHardwareAccelerated) {
            ai4.flags |= 536870912;
        }
        if (sa4.getBoolean(7, true)) {
            ai4.flags |= 4;
        }
        if (sa4.getBoolean(14, false)) {
            ai4.flags |= 32;
        }
        if (sa4.getBoolean(5, true)) {
            ai4.flags |= 64;
        }
        if (packageR.parentPackage == null && sa4.getBoolean(15, false)) {
            ai4.flags |= 256;
        }
        if (sa4.getBoolean(24, false)) {
            ai4.flags |= 1048576;
        }
        if (sa4.getBoolean(36, packageR.applicationInfo.targetSdkVersion < 28)) {
            ai4.flags |= 134217728;
        }
        if (sa4.getBoolean(26, false)) {
            ai4.flags |= 4194304;
        }
        if (sa4.getBoolean(33, false)) {
            ai4.flags |= Integer.MIN_VALUE;
        }
        if (sa4.getBoolean(34, true)) {
            ai4.flags |= 268435456;
        }
        if (sa4.getBoolean(38, false)) {
            ai4.privateFlags |= 32;
        }
        if (sa4.getBoolean(39, false)) {
            ai4.privateFlags |= 64;
        }
        if (sa4.hasValueOrEmpty(37)) {
            if (sa4.getBoolean(37, true)) {
                ai4.privateFlags |= 1024;
            } else {
                ai4.privateFlags |= 2048;
            }
        } else if (packageR.applicationInfo.targetSdkVersion >= 24) {
            ai4.privateFlags |= 4096;
        }
        ai4.maxAspectRatio = sa4.getFloat(44, 0.0f);
        ai4.networkSecurityConfigRes = sa4.getResourceId(41, 0);
        ai4.category = sa4.getInt(43, -1);
        String str3 = sa4.getNonConfigurationString(6, 0);
        ai4.permission = (str3 == null || str3.length() <= 0) ? null : str3.intern();
        if (packageR.applicationInfo.targetSdkVersion >= 8) {
            str = sa4.getNonConfigurationString(12, 1024);
        } else {
            str = sa4.getNonResourceString(12);
        }
        String str4 = str;
        ai4.taskAffinity = buildTaskAffinityName(ai4.packageName, ai4.packageName, str4, strArr4);
        String factory = sa4.getNonResourceString(48);
        if (factory != null) {
            ai4.appComponentFactory = buildClassName(ai4.packageName, factory, strArr4);
        }
        if (strArr4[0] == null) {
            if (packageR.applicationInfo.targetSdkVersion >= 8) {
                pname = sa4.getNonConfigurationString(11, 1024);
            } else {
                pname = sa4.getNonResourceString(11);
            }
            String str5 = requiredAccountType;
            String str6 = factory;
            z = true;
            pkgName2 = pkgName;
            ai4.processName = buildProcessName(ai4.packageName, null, pname, flags, packageParser2.mSeparateProcesses, strArr4);
            ai4.enabled = sa4.getBoolean(9, true);
            if (sa4.getBoolean(31, false)) {
                ai4.flags |= 33554432;
            }
            if (sa4.getBoolean(47, false)) {
                ai4.privateFlags |= 2;
                if (ai4.processName == null || ai4.processName.equals(ai4.packageName)) {
                    i = 0;
                } else {
                    i = 0;
                    strArr4[0] = "cantSaveState applications can not use custom processes";
                }
            } else {
                i = 0;
            }
        } else {
            String str7 = factory;
            pkgName2 = pkgName;
            i = 0;
            z = true;
        }
        ai4.uiOptions = sa4.getInt(25, i);
        ai4.classLoaderName = sa4.getString(46);
        if (ai4.classLoaderName == null || ClassLoaderFactory.isValidClassLoaderName(ai4.classLoaderName)) {
            r1 = 0;
        } else {
            r1 = 0;
            strArr4[0] = "Invalid class loader name: " + ai4.classLoaderName;
        }
        sa4.recycle();
        if (strArr4[r1] != null) {
            packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return r1;
        }
        int innerDepth2 = parser.getDepth();
        String str8 = manageSpaceActivity;
        CachedComponentArgs cachedArgs = new CachedComponentArgs();
        boolean hasActivityOrder = false;
        boolean hasReceiverOrder = false;
        TypedArray typedArray = sa4;
        boolean hasServiceOrder2 = false;
        while (true) {
            hasServiceOrder = hasServiceOrder2;
            int next = parser.next();
            int type = next;
            if (next != z) {
                if (type == 3 && parser.getDepth() <= innerDepth2) {
                    int i3 = innerDepth2;
                    int i4 = type;
                    String str9 = str4;
                    ApplicationInfo applicationInfo = ai4;
                    String str10 = restrictedAccountType3;
                    String[] strArr5 = strArr4;
                    Resources resources3 = resources2;
                    PackageParser packageParser3 = packageParser2;
                    String restrictedAccountType4 = pkgName2;
                    XmlResourceParser xmlResourceParser = parser;
                    break;
                }
                if (type == 3) {
                    innerDepth = innerDepth2;
                    str2 = str4;
                    ai = ai4;
                    restrictedAccountType = restrictedAccountType3;
                    strArr = strArr4;
                    resources = resources2;
                    packageParser = packageParser2;
                    restrictedAccountType2 = pkgName2;
                    XmlResourceParser xmlResourceParser2 = parser;
                } else if (type == 4) {
                    innerDepth = innerDepth2;
                    str2 = str4;
                    ai = ai4;
                    restrictedAccountType = restrictedAccountType3;
                    strArr = strArr4;
                    resources = resources2;
                    packageParser = packageParser2;
                    restrictedAccountType2 = pkgName2;
                    XmlResourceParser xmlResourceParser3 = parser;
                } else {
                    String tagName = parser.getName();
                    if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                        String str11 = tagName;
                        innerDepth = innerDepth2;
                        int i5 = type;
                        str2 = str4;
                        Activity a = packageParser2.parseActivity(packageR, resources2, parser, flags, strArr4, cachedArgs, false, packageR.baseHardwareAccelerated);
                        if (a == null) {
                            packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return false;
                        }
                        boolean z2 = a.order != 0 ? z : false;
                        packageR.activities.add(a);
                        XmlResourceParser xmlResourceParser4 = parser;
                        hasActivityOrder |= z2;
                        ai = ai4;
                        restrictedAccountType = restrictedAccountType3;
                        strArr2 = strArr4;
                        resources = resources2;
                        packageParser = packageParser2;
                    } else {
                        innerDepth = innerDepth2;
                        int i6 = type;
                        str2 = str4;
                        String tagName2 = tagName;
                        if (tagName2.equals(HwFrameworkMonitor.KEY_RECEIVER)) {
                            ai = ai4;
                            boolean z3 = z;
                            restrictedAccountType = restrictedAccountType3;
                            packageParser = packageParser2;
                            Activity a2 = packageParser2.parseActivity(packageR, resources2, parser, flags, outError, cachedArgs, true, false);
                            if (a2 == null) {
                                packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return false;
                            }
                            boolean z4 = a2.order != 0;
                            packageR = owner;
                            packageR.receivers.add(a2);
                            XmlResourceParser xmlResourceParser5 = parser;
                            strArr = outError;
                            hasReceiverOrder |= z4;
                        } else {
                            ai = ai4;
                            restrictedAccountType = restrictedAccountType3;
                            packageParser = packageParser2;
                            if (tagName2.equals(Notification.CATEGORY_SERVICE)) {
                                Service s = packageParser.parseService(packageR, res, parser, flags, outError, cachedArgs);
                                if (s == null) {
                                    packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                boolean z5 = s.order != 0;
                                packageR.services.add(s);
                                XmlResourceParser xmlResourceParser6 = parser;
                                strArr = outError;
                                hasServiceOrder |= z5;
                            } else if (tagName2.equals("provider")) {
                                Provider p = packageParser.parseProvider(packageR, res, parser, flags, outError, cachedArgs);
                                if (p == null) {
                                    packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                packageR.providers.add(p);
                                resources = res;
                                XmlResourceParser xmlResourceParser7 = parser;
                                strArr2 = outError;
                            } else if (tagName2.equals("activity-alias")) {
                                Activity a3 = packageParser.parseActivityAlias(packageR, res, parser, flags, outError, cachedArgs);
                                if (a3 == null) {
                                    packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                boolean z6 = a3.order != 0;
                                packageR.activities.add(a3);
                                XmlResourceParser xmlResourceParser8 = parser;
                                strArr = outError;
                                hasActivityOrder |= z6;
                            } else if (parser.getName().equals("meta-data")) {
                                resources = res;
                                strArr2 = outError;
                                Bundle parseMetaData = packageParser.parseMetaData(resources, parser, packageR.mAppMetaData, strArr2);
                                packageR.mAppMetaData = parseMetaData;
                                if (parseMetaData == null) {
                                    packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                int themeId = HwWidgetFactory.getThemeId(packageR.mAppMetaData, resources);
                                if (themeId != 0) {
                                    ai.theme = themeId;
                                }
                                ai.minEmuiSdkVersion = packageR.mAppMetaData.getInt("huawei.emui_minSdk");
                                ai.targetEmuiSdkVersion = packageR.mAppMetaData.getInt("huawei.emui_targetSdk");
                                ai.hwThemeType = packageR.mAppMetaData.getInt("hw.theme_type");
                                ai.minEmuiSysImgVersion = packageR.mAppMetaData.getInt("huawei.emui_minSysImgVersion", -1);
                                ai.gestnav_extra_flags = packageR.mAppMetaData.getInt("huawei.gestnav_extra_flags");
                            } else {
                                resources = res;
                                XmlResourceParser xmlResourceParser9 = parser;
                                strArr = outError;
                                if (tagName2.equals("static-library")) {
                                    TypedArray sa5 = resources.obtainAttributes(xmlResourceParser9, R.styleable.AndroidManifestStaticLibrary);
                                    String lname = sa5.getNonResourceString(0);
                                    int version = sa5.getInt(1, -1);
                                    int versionMajor = sa5.getInt(2, 0);
                                    sa5.recycle();
                                    if (lname == null) {
                                        break;
                                    } else if (version < 0) {
                                        String str12 = pkgName2;
                                        break;
                                    } else if (packageR.mSharedUserId != null) {
                                        strArr[0] = "sharedUserId not allowed in static shared library";
                                        packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                                        XmlUtils.skipCurrentTag(parser);
                                        return false;
                                    } else if (packageR.staticSharedLibName != null) {
                                        strArr[0] = "Multiple static-shared libs for package " + pkgName2;
                                        packageParser.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        XmlUtils.skipCurrentTag(parser);
                                        return false;
                                    } else {
                                        restrictedAccountType2 = pkgName2;
                                        packageR.staticSharedLibName = lname.intern();
                                        if (version >= 0) {
                                            packageR.staticSharedLibVersion = PackageInfo.composeLongVersionCode(versionMajor, version);
                                        } else {
                                            packageR.staticSharedLibVersion = (long) version;
                                        }
                                        ai.privateFlags |= 16384;
                                        XmlUtils.skipCurrentTag(parser);
                                        TypedArray typedArray2 = sa5;
                                    }
                                } else {
                                    restrictedAccountType2 = pkgName2;
                                    if (tagName2.equals("library")) {
                                        sa = resources.obtainAttributes(xmlResourceParser9, R.styleable.AndroidManifestLibrary);
                                        String lname2 = sa.getNonResourceString(0);
                                        sa.recycle();
                                        if (lname2 != null) {
                                            String lname3 = lname2.intern();
                                            if (!ArrayUtils.contains(packageR.libraryNames, lname3)) {
                                                packageR.libraryNames = ArrayUtils.add(packageR.libraryNames, lname3);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                    } else if (tagName2.equals("uses-static-library")) {
                                        if (!packageParser.parseUsesStaticLibrary(packageR, resources, xmlResourceParser9, strArr)) {
                                            return false;
                                        }
                                    } else if (tagName2.equals("uses-library")) {
                                        sa = resources.obtainAttributes(xmlResourceParser9, R.styleable.AndroidManifestUsesLibrary);
                                        String lname4 = sa.getNonResourceString(0);
                                        boolean req = sa.getBoolean(1, true);
                                        sa.recycle();
                                        if (lname4 != null) {
                                            String lname5 = lname4.intern();
                                            if (req) {
                                                packageR.usesLibraries = ArrayUtils.add(packageR.usesLibraries, lname5);
                                            } else {
                                                packageR.usesOptionalLibraries = ArrayUtils.add(packageR.usesOptionalLibraries, lname5);
                                            }
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                    } else if (tagName2.equals("uses-package")) {
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        Slog.w(TAG, "Unknown element under <application>: " + tagName2 + " at " + packageParser.mArchiveSourcePath + " " + parser.getPositionDescription());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                        restrictedAccountType2 = pkgName2;
                        resources = res;
                    }
                    restrictedAccountType2 = pkgName2;
                }
                packageParser2 = packageParser;
                resources2 = resources;
                strArr4 = strArr;
                pkgName2 = restrictedAccountType2;
                restrictedAccountType3 = restrictedAccountType;
                hasServiceOrder2 = hasServiceOrder;
                str4 = str2;
                innerDepth2 = innerDepth;
                z = true;
                ai4 = ai;
            } else {
                int i7 = innerDepth2;
                int i8 = type;
                String str13 = str4;
                ApplicationInfo applicationInfo2 = ai4;
                String str14 = restrictedAccountType3;
                String[] strArr6 = strArr4;
                Resources resources4 = resources2;
                PackageParser packageParser4 = packageParser2;
                String restrictedAccountType5 = pkgName2;
                XmlResourceParser xmlResourceParser10 = parser;
                break;
            }
        }
        if (hasActivityOrder) {
            Collections.sort(packageR.activities, $$Lambda$PackageParser$0aobsT7Zf7WVZCqMZ5z2clAuQf4.INSTANCE);
        }
        if (hasReceiverOrder) {
            Collections.sort(packageR.receivers, $$Lambda$PackageParser$0DZRgzfgaIMpCOhJqjw6PUiU5vw.INSTANCE);
        }
        if (hasServiceOrder) {
            Collections.sort(packageR.services, $$Lambda$PackageParser$M9fHqS_eEp1oYkuKJhRHOGUxf8.INSTANCE);
        }
        setMaxAspectRatio(owner);
        boolean hwNotchSupport = false;
        if (packageR.mAppMetaData != null && packageR.mAppMetaData.containsKey(METADATA_NOTCH_SUPPORT)) {
            hwNotchSupport = packageR.mAppMetaData.getBoolean(METADATA_NOTCH_SUPPORT, false);
        }
        int gestureNavOptions = 0;
        if (packageR.mAppMetaData != null && packageR.mAppMetaData.containsKey(METADATA_GESTURE_NAV_OPTIONS)) {
            gestureNavOptions = packageR.mAppMetaData.getInt(METADATA_GESTURE_NAV_OPTIONS, 0);
        }
        int size = packageR.activities.size();
        for (int i9 = 0; i9 < size; i9++) {
            Activity t = packageR.activities.get(i9);
            if (!hwNotchSupport) {
                if (t.metaData != null) {
                }
                if (t.metaData == null && t.metaData.containsKey(METADATA_GESTURE_NAV_OPTIONS)) {
                    t.info.hwGestureNavOptions = t.metaData.getInt(METADATA_GESTURE_NAV_OPTIONS, gestureNavOptions);
                } else if (gestureNavOptions == 0) {
                    t.info.hwGestureNavOptions = gestureNavOptions;
                }
            }
            t.info.hwNotchSupport = true;
            if (t.metaData == null) {
            }
            if (gestureNavOptions == 0) {
            }
        }
        PackageBackwardCompatibility.modifySharedLibraries(owner);
        if (hasDomainURLs(owner)) {
            packageR.applicationInfo.privateFlags |= 16;
        } else {
            packageR.applicationInfo.privateFlags &= -17;
        }
        return true;
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

    private boolean parseSplitApplication(Package owner, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException {
        int innerDepth;
        String classLoaderName;
        String[] strArr;
        XmlResourceParser xmlResourceParser;
        Resources resources;
        int i;
        Package packageR;
        boolean z;
        PackageParser packageParser;
        ComponentInfo parsedComponent;
        PackageParser packageParser2 = this;
        Package packageR2 = owner;
        Resources resources2 = res;
        XmlResourceParser xmlResourceParser2 = parser;
        String[] strArr2 = outError;
        TypedArray sa = resources2.obtainAttributes(xmlResourceParser2, R.styleable.AndroidManifestApplication);
        int i2 = 1;
        int i3 = 4;
        if (sa.getBoolean(7, true)) {
            int[] iArr = packageR2.splitFlags;
            iArr[splitIndex] = iArr[splitIndex] | 4;
        }
        String classLoaderName2 = sa.getString(46);
        int i4 = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        boolean z2 = false;
        if (classLoaderName2 == null || ClassLoaderFactory.isValidClassLoaderName(classLoaderName2)) {
            packageR2.applicationInfo.splitClassLoaderNames[splitIndex] = classLoaderName2;
            int innerDepth2 = parser.getDepth();
            TypedArray typedArray = sa;
            while (true) {
                int innerDepth3 = innerDepth2;
                int next = parser.next();
                int type = next;
                if (next != i2) {
                    if (type == 3 && parser.getDepth() <= innerDepth3) {
                        int i5 = type;
                        int i6 = innerDepth3;
                        String str = classLoaderName2;
                        String[] strArr3 = strArr2;
                        XmlResourceParser xmlResourceParser3 = xmlResourceParser2;
                        Resources resources3 = resources2;
                        Package packageR3 = packageR2;
                        PackageParser packageParser3 = packageParser2;
                        break;
                    }
                    if (type == 3) {
                        innerDepth = innerDepth3;
                        z = z2;
                        i = i4;
                        classLoaderName = classLoaderName2;
                        strArr = strArr2;
                        xmlResourceParser = xmlResourceParser2;
                        resources = resources2;
                        packageR = packageR2;
                        packageParser = packageParser2;
                    } else if (type == i3) {
                        innerDepth = innerDepth3;
                        z = z2;
                        i = i4;
                        classLoaderName = classLoaderName2;
                        strArr = strArr2;
                        xmlResourceParser = xmlResourceParser2;
                        resources = resources2;
                        packageR = packageR2;
                        packageParser = packageParser2;
                    } else {
                        CachedComponentArgs cachedArgs = new CachedComponentArgs();
                        String tagName = parser.getName();
                        if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                            String tagName2 = tagName;
                            int i7 = type;
                            innerDepth = innerDepth3;
                            boolean z3 = z2;
                            int i8 = i4;
                            classLoaderName = classLoaderName2;
                            Activity a = packageParser2.parseActivity(packageR2, resources2, xmlResourceParser2, flags, strArr2, cachedArgs, false, packageR2.baseHardwareAccelerated);
                            if (a == null) {
                                packageParser2.mParseError = i8;
                                return false;
                            }
                            z = false;
                            packageR2.activities.add(a);
                            parsedComponent = a.info;
                            i = i8;
                            strArr = strArr2;
                            xmlResourceParser = xmlResourceParser2;
                            resources = resources2;
                            packageR = packageR2;
                            packageParser = packageParser2;
                            String str2 = tagName2;
                        } else {
                            int i9 = type;
                            innerDepth = innerDepth3;
                            z = z2;
                            int i10 = i4;
                            classLoaderName = classLoaderName2;
                            if (tagName.equals(HwFrameworkMonitor.KEY_RECEIVER)) {
                                int i11 = i3;
                                i = i10;
                                packageR = packageR2;
                                packageParser = packageParser2;
                                Activity a2 = packageParser2.parseActivity(packageR2, resources2, xmlResourceParser2, flags, outError, cachedArgs, true, false);
                                if (a2 == null) {
                                    packageParser.mParseError = i;
                                    return z;
                                }
                                packageR.receivers.add(a2);
                                parsedComponent = a2.info;
                            } else {
                                i = i10;
                                packageR = packageR2;
                                packageParser = packageParser2;
                                if (tagName.equals(Notification.CATEGORY_SERVICE)) {
                                    Service s = packageParser.parseService(packageR, res, parser, flags, outError, cachedArgs);
                                    if (s == null) {
                                        packageParser.mParseError = i;
                                        return z;
                                    }
                                    packageR.services.add(s);
                                    parsedComponent = s.info;
                                } else if (tagName.equals("provider")) {
                                    Provider p = packageParser.parseProvider(packageR, res, parser, flags, outError, cachedArgs);
                                    if (p == null) {
                                        packageParser.mParseError = i;
                                        return z;
                                    }
                                    packageR.providers.add(p);
                                    parsedComponent = p.info;
                                } else if (tagName.equals("activity-alias")) {
                                    Activity a3 = packageParser.parseActivityAlias(packageR, res, parser, flags, outError, cachedArgs);
                                    if (a3 == null) {
                                        packageParser.mParseError = i;
                                        return z;
                                    }
                                    packageR.activities.add(a3);
                                    parsedComponent = a3.info;
                                } else {
                                    if (parser.getName().equals("meta-data")) {
                                        resources = res;
                                        xmlResourceParser = parser;
                                        strArr = outError;
                                        Bundle parseMetaData = packageParser.parseMetaData(resources, xmlResourceParser, packageR.mAppMetaData, strArr);
                                        packageR.mAppMetaData = parseMetaData;
                                        if (parseMetaData == null) {
                                            packageParser.mParseError = i;
                                            return z;
                                        }
                                    } else {
                                        resources = res;
                                        xmlResourceParser = parser;
                                        strArr = outError;
                                        if (tagName.equals("uses-static-library")) {
                                            if (!packageParser.parseUsesStaticLibrary(packageR, resources, xmlResourceParser, strArr)) {
                                                return z;
                                            }
                                        } else if (tagName.equals("uses-library")) {
                                            TypedArray sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestUsesLibrary);
                                            String lname = sa2.getNonResourceString(z ? 1 : 0);
                                            boolean req = sa2.getBoolean(1, true);
                                            sa2.recycle();
                                            if (lname != null) {
                                                String lname2 = lname.intern();
                                                if (req) {
                                                    packageR.usesLibraries = ArrayUtils.add(packageR.usesLibraries, lname2);
                                                    packageR.usesOptionalLibraries = ArrayUtils.remove(packageR.usesOptionalLibraries, lname2);
                                                } else if (!ArrayUtils.contains(packageR.usesLibraries, lname2)) {
                                                    packageR.usesOptionalLibraries = ArrayUtils.add(packageR.usesOptionalLibraries, lname2);
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            TypedArray typedArray2 = sa2;
                                        } else if (tagName.equals("uses-package")) {
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + packageParser.mArchiveSourcePath + " " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                        }
                                    }
                                    parsedComponent = null;
                                }
                            }
                            resources = res;
                            xmlResourceParser = parser;
                            strArr = outError;
                        }
                        if (parsedComponent != null && parsedComponent.splitName == null) {
                            parsedComponent.splitName = packageR.splitNames[splitIndex];
                        }
                    }
                    packageParser2 = packageParser;
                    packageR2 = packageR;
                    resources2 = resources;
                    xmlResourceParser2 = xmlResourceParser;
                    strArr2 = strArr;
                    classLoaderName2 = classLoaderName;
                    i3 = 4;
                    i2 = 1;
                    i4 = i;
                    z2 = z;
                    innerDepth2 = innerDepth;
                } else {
                    int i12 = type;
                    int i13 = innerDepth3;
                    String str3 = classLoaderName2;
                    String[] strArr4 = strArr2;
                    XmlResourceParser xmlResourceParser4 = xmlResourceParser2;
                    Resources resources4 = resources2;
                    Package packageR4 = packageR2;
                    PackageParser packageParser4 = packageParser2;
                    break;
                }
            }
            return true;
        }
        strArr2[0] = "Invalid class loader name: " + classLoaderName2;
        packageParser2.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean parsePackageItemInfo(Package owner, PackageItemInfo outInfo, String[] outError, String tag, TypedArray sa, boolean nameRequired, int nameRes, int labelRes, int iconRes, int roundIconRes, int logoRes, int bannerRes) {
        int roundIconVal;
        Package packageR = owner;
        PackageItemInfo packageItemInfo = outInfo;
        String[] strArr = outError;
        String str = tag;
        TypedArray typedArray = sa;
        if (typedArray == null) {
            strArr[0] = str + " does not contain any attributes";
            return false;
        }
        String name = typedArray.getNonConfigurationString(nameRes, 0);
        if (name != null) {
            packageItemInfo.name = buildClassName(packageR.applicationInfo.packageName, name, strArr);
            if (packageItemInfo.name == null) {
                return false;
            }
        } else if (nameRequired) {
            strArr[0] = str + " does not specify android:name";
            return false;
        }
        if (Resources.getSystem().getBoolean(R.bool.config_useRoundIcon)) {
            roundIconVal = typedArray.getResourceId(roundIconRes, 0);
        } else {
            int i = roundIconRes;
            roundIconVal = 0;
        }
        if (roundIconVal != 0) {
            packageItemInfo.icon = roundIconVal;
            packageItemInfo.nonLocalizedLabel = null;
            int i2 = iconRes;
        } else {
            int iconVal = typedArray.getResourceId(iconRes, 0);
            if (iconVal != 0) {
                packageItemInfo.icon = iconVal;
                packageItemInfo.nonLocalizedLabel = null;
            }
        }
        int logoVal = typedArray.getResourceId(logoRes, 0);
        if (logoVal != 0) {
            packageItemInfo.logo = logoVal;
        }
        int bannerVal = typedArray.getResourceId(bannerRes, 0);
        if (bannerVal != 0) {
            packageItemInfo.banner = bannerVal;
        }
        TypedValue v = typedArray.peekValue(labelRes);
        if (v != null) {
            int i3 = v.resourceId;
            packageItemInfo.labelRes = i3;
            if (i3 == 0) {
                packageItemInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        packageItemInfo.packageName = packageR.packageName;
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:126:0x0404  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0437  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x044a  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0459  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0472  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x047a  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x047c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0190  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01ee  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01fe  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0210  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0221  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0235  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0247  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0257  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0259  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0260  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0271  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x029c  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x02ad  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02b8  */
    private Activity parseActivity(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs, boolean receiver, boolean hardwareAccelerated) throws XmlPullParserException, IOException {
        int i;
        String str;
        String str2;
        boolean visibleToEphemeral;
        int i2;
        int i3;
        String parentName;
        int outerDepth;
        TypedArray sa;
        String str3;
        Package packageR;
        String[] strArr;
        char c;
        XmlResourceParser xmlResourceParser;
        Resources resources;
        Package packageR2 = owner;
        Resources resources2 = res;
        XmlResourceParser xmlResourceParser2 = parser;
        String[] strArr2 = outError;
        CachedComponentArgs cachedComponentArgs = cachedArgs;
        TypedArray sa2 = resources2.obtainAttributes(xmlResourceParser2, R.styleable.AndroidManifestActivity);
        if (cachedComponentArgs.mActivityArgs == null) {
            ParseComponentArgs parseComponentArgs = new ParseComponentArgs(packageR2, strArr2, 3, 1, 2, 44, 23, 30, this.mSeparateProcesses, 7, 17, 5);
            cachedComponentArgs.mActivityArgs = parseComponentArgs;
        }
        cachedComponentArgs.mActivityArgs.tag = receiver ? "<receiver>" : "<activity>";
        cachedComponentArgs.mActivityArgs.sa = sa2;
        cachedComponentArgs.mActivityArgs.flags = flags;
        Activity a = new Activity(cachedComponentArgs.mActivityArgs, new ActivityInfo());
        if (strArr2[0] != null) {
            sa2.recycle();
            return null;
        }
        boolean setExported = sa2.hasValue(6);
        if (setExported) {
            a.info.exported = sa2.getBoolean(6, false);
        }
        a.info.theme = sa2.getResourceId(0, 0);
        if ("com.google.android.packageinstaller".equals(packageR2.packageName)) {
            if (a.info.theme != 0) {
                String themeName = resources2.getResourceName(a.info.theme);
                if (themeName.endsWith("AlertDialogActivity")) {
                    a.info.theme = 33951747;
                } else if (themeName.endsWith("GrantPermissions")) {
                    a.info.theme = 33951753;
                } else if (themeName.endsWith("Settings")) {
                    a.info.theme = 33951748;
                } else if (themeName.endsWith("Theme.DeviceDefault.Light.Dialog.NoActionBar")) {
                    a.info.theme = 33951753;
                } else if (themeName.endsWith("Settings.NoActionBar")) {
                    a.info.theme = 33951749;
                } else {
                    a.info.theme = 33951746;
                }
                Flog.i(206, "parseActivity, packageinstaller themeName changes from [" + themeName + "] to [" + resources2.getResourceName(a.info.theme) + "]");
            } else {
                a.info.theme = 33951746;
                Flog.i(206, "parseActivity, packageinstaller no themeName change to [" + resources2.getResourceName(a.info.theme) + "]");
            }
        }
        a.info.uiOptions = sa2.getInt(26, a.info.applicationInfo.uiOptions);
        String parentName2 = sa2.getNonConfigurationString(27, 1024);
        if (parentName2 != null) {
            String parentClassName = buildClassName(a.info.packageName, parentName2, strArr2);
            if (strArr2[0] == null) {
                a.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity " + a.info.name + " specified invalid parentActivityName " + parentName2);
                i = 0;
                strArr2[0] = null;
                str = sa2.getNonConfigurationString(4, i);
                if (str != null) {
                    a.info.permission = packageR2.applicationInfo.permission;
                } else {
                    a.info.permission = str.length() > 0 ? str.toString().intern() : null;
                }
                String str4 = sa2.getNonConfigurationString(8, 1024);
                a.info.taskAffinity = buildTaskAffinityName(packageR2.applicationInfo.packageName, packageR2.applicationInfo.taskAffinity, str4, strArr2);
                a.info.splitName = sa2.getNonConfigurationString(48, 0);
                a.info.flags = 0;
                if (sa2.getBoolean(9, false)) {
                    a.info.flags |= 1;
                }
                if (sa2.getBoolean(10, false)) {
                    a.info.flags |= 2;
                }
                if (sa2.getBoolean(11, false)) {
                    a.info.flags |= 4;
                }
                if (sa2.getBoolean(21, false)) {
                    a.info.flags |= 128;
                }
                if (sa2.getBoolean(18, false)) {
                    a.info.flags |= 8;
                }
                if (sa2.getBoolean(12, false)) {
                    a.info.flags |= 16;
                }
                if (sa2.getBoolean(13, false)) {
                    a.info.flags |= 32;
                }
                if (sa2.getBoolean(19, (packageR2.applicationInfo.flags & 32) == 0)) {
                    a.info.flags |= 64;
                }
                if (sa2.getBoolean(22, false)) {
                    a.info.flags |= 256;
                }
                if (sa2.getBoolean(29, false) || sa2.getBoolean(39, false)) {
                    a.info.flags |= 1024;
                }
                if (sa2.getBoolean(24, false)) {
                    a.info.flags |= 2048;
                }
                if (sa2.getBoolean(54, false)) {
                    a.info.flags |= 536870912;
                }
                if (receiver) {
                    if (sa2.getBoolean(25, hardwareAccelerated)) {
                        a.info.flags |= 512;
                    }
                    str2 = str4;
                    a.info.launchMode = sa2.getInt(14, 0);
                    a.info.documentLaunchMode = sa2.getInt(33, 0);
                    a.info.maxRecents = sa2.getInt(34, ActivityManager.getDefaultAppRecentsLimitStatic());
                    a.info.configChanges = getActivityConfigChanges(sa2.getInt(16, 0), sa2.getInt(47, 0));
                    a.info.softInputMode = sa2.getInt(20, 0);
                    a.info.persistableMode = sa2.getInteger(32, 0);
                    if (sa2.getBoolean(31, false)) {
                        a.info.flags |= Integer.MIN_VALUE;
                    }
                    if (sa2.getBoolean(35, false)) {
                        a.info.flags |= 8192;
                    }
                    if (sa2.getBoolean(36, false)) {
                        a.info.flags |= 4096;
                    }
                    if (sa2.getBoolean(37, false)) {
                        a.info.flags |= 16384;
                    }
                    a.info.screenOrientation = sa2.getInt(15, -1);
                    setActivityResizeMode(a.info, sa2, packageR2);
                    if (sa2.getBoolean(41, false)) {
                        a.info.flags |= 4194304;
                    }
                    if (sa2.getBoolean(53, false)) {
                        a.info.flags |= 262144;
                    }
                    if (sa2.hasValue(50) && sa2.getType(50) == 4) {
                        a.setMaxAspectRatio(sa2.getFloat(50, 0.0f));
                    }
                    a.info.lockTaskLaunchMode = sa2.getInt(38, 0);
                    ActivityInfo activityInfo = a.info;
                    ActivityInfo activityInfo2 = a.info;
                    boolean z = sa2.getBoolean(42, false);
                    activityInfo2.directBootAware = z;
                    activityInfo.encryptionAware = z;
                    a.info.requestedVrComponent = sa2.getString(43);
                    a.info.rotationAnimation = sa2.getInt(46, -1);
                    a.info.colorMode = sa2.getInt(49, 0);
                    if (sa2.getBoolean(51, false)) {
                        a.info.flags |= 8388608;
                    }
                    if (sa2.getBoolean(52, false)) {
                        a.info.flags |= 16777216;
                    }
                } else {
                    boolean z2 = hardwareAccelerated;
                    str2 = str4;
                    a.info.launchMode = 0;
                    a.info.configChanges = 0;
                    if (sa2.getBoolean(28, false)) {
                        a.info.flags |= 1073741824;
                    }
                    ActivityInfo activityInfo3 = a.info;
                    ActivityInfo activityInfo4 = a.info;
                    boolean z3 = sa2.getBoolean(42, false);
                    activityInfo4.directBootAware = z3;
                    activityInfo3.encryptionAware = z3;
                }
                if (a.info.directBootAware) {
                    packageR2.applicationInfo.privateFlags |= 256;
                }
                visibleToEphemeral = sa2.getBoolean(45, false);
                if (visibleToEphemeral) {
                    a.info.flags |= 1048576;
                    packageR2.visibleToInstantApps = true;
                }
                sa2.recycle();
                if (!receiver) {
                    i3 = 2;
                    if ((packageR2.applicationInfo.privateFlags & 2) != 0 && a.info.processName == packageR2.packageName) {
                        i2 = 0;
                        strArr2[0] = "Heavy-weight applications can not have receivers in main process";
                        if (strArr2[i2] != null) {
                            return null;
                        }
                        int outerDepth2 = parser.getDepth();
                        while (true) {
                            int outerDepth3 = outerDepth2;
                            int outerDepth4 = parser.next();
                            int type = outerDepth4;
                            if (outerDepth4 == 1) {
                                int i4 = outerDepth3;
                                XmlResourceParser xmlResourceParser3 = xmlResourceParser2;
                                Resources resources3 = resources2;
                                Package packageR3 = packageR2;
                                String str5 = parentName2;
                                String str6 = str2;
                                int i5 = type;
                                String[] strArr3 = strArr2;
                                break;
                            }
                            int type2 = type;
                            if (type2 == 3 && parser.getDepth() <= outerDepth3) {
                                TypedArray typedArray = sa2;
                                int i6 = outerDepth3;
                                XmlResourceParser xmlResourceParser4 = xmlResourceParser2;
                                Resources resources4 = resources2;
                                Package packageR4 = packageR2;
                                String str7 = parentName2;
                                String str8 = str2;
                                String[] strArr4 = strArr2;
                                break;
                            }
                            if (type2 != 3) {
                                if (type2 == 4) {
                                    sa = sa2;
                                    outerDepth = outerDepth3;
                                    xmlResourceParser = xmlResourceParser2;
                                    resources = resources2;
                                    packageR = packageR2;
                                    parentName = parentName2;
                                    str3 = str2;
                                    strArr = strArr2;
                                } else {
                                    TypedArray sa3 = sa2;
                                    if (parser.getName().equals("intent-filter")) {
                                        str3 = str2;
                                        ActivityIntentInfo intent = new ActivityIntentInfo(a);
                                        sa = sa3;
                                        outerDepth = outerDepth3;
                                        packageR = packageR2;
                                        if (!parseIntent(resources2, xmlResourceParser2, true, true, intent, outError)) {
                                            return null;
                                        }
                                        ActivityIntentInfo intent2 = intent;
                                        if (intent2.countActions() == 0) {
                                            Slog.w(TAG, "No actions in intent filter at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                        } else {
                                            a.order = Math.max(intent2.getOrder(), a.order);
                                            a.intents.add(intent2);
                                        }
                                        intent2.setVisibilityToInstantApp(visibleToEphemeral ? 1 : (receiver || !isImplicitlyExposedIntent(intent2)) ? i2 : i3);
                                        if (intent2.isVisibleToInstantApp()) {
                                            a.info.flags |= 1048576;
                                        }
                                        if (intent2.isImplicitlyVisibleToInstantApp()) {
                                            a.info.flags |= 2097152;
                                        }
                                        resources = res;
                                        xmlResourceParser = parser;
                                        strArr = outError;
                                        parentName = parentName2;
                                    } else {
                                        outerDepth = outerDepth3;
                                        packageR = packageR2;
                                        str3 = str2;
                                        sa = sa3;
                                        if (receiver || !parser.getName().equals("preferred")) {
                                            parentName = parentName2;
                                            c = 0;
                                            if (parser.getName().equals("meta-data")) {
                                                resources = res;
                                                xmlResourceParser = parser;
                                                strArr = outError;
                                                Bundle parseMetaData = parseMetaData(resources, xmlResourceParser, a.metaData, strArr);
                                                a.metaData = parseMetaData;
                                                if (parseMetaData == null) {
                                                    return null;
                                                }
                                                HwFrameworkFactory.getHwPackageParser().initMetaData(a);
                                                int themeId = HwWidgetFactory.getThemeId(a.metaData, resources);
                                                if (themeId != 0) {
                                                    a.info.theme = themeId;
                                                }
                                                HwThemeManager.addSimpleUIConfig(a);
                                            } else {
                                                resources = res;
                                                xmlResourceParser = parser;
                                                strArr = outError;
                                                if (receiver || !parser.getName().equals("layout")) {
                                                    Slog.w(TAG, "Problem in package " + this.mArchiveSourcePath + ":");
                                                    if (receiver) {
                                                        Slog.w(TAG, "Unknown element under <receiver>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                                    } else {
                                                        Slog.w(TAG, "Unknown element under <activity>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                                    }
                                                    XmlUtils.skipCurrentTag(parser);
                                                } else {
                                                    parseLayout(resources, xmlResourceParser, a);
                                                }
                                            }
                                        } else {
                                            ActivityIntentInfo intent3 = new ActivityIntentInfo(a);
                                            parentName = parentName2;
                                            ActivityIntentInfo intent4 = intent3;
                                            if (!parseIntent(res, parser, false, false, intent3, outError)) {
                                                return null;
                                            }
                                            if (intent4.countActions() == 0) {
                                                Slog.w(TAG, "No actions in preferred at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                            } else {
                                                if (packageR.preferredActivityFilters == null) {
                                                    packageR.preferredActivityFilters = new ArrayList<>();
                                                }
                                                packageR.preferredActivityFilters.add(intent4);
                                            }
                                            intent4.setVisibilityToInstantApp(visibleToEphemeral ? 1 : (receiver || !isImplicitlyExposedIntent(intent4)) ? i2 : i3);
                                            if (intent4.isVisibleToInstantApp()) {
                                                c = 0;
                                                a.info.flags |= 1048576;
                                            } else {
                                                c = 0;
                                            }
                                            if (intent4.isImplicitlyVisibleToInstantApp()) {
                                                a.info.flags |= 2097152;
                                            }
                                            resources = res;
                                            xmlResourceParser = parser;
                                            strArr = outError;
                                        }
                                    }
                                }
                                c = 0;
                            } else {
                                sa = sa2;
                                outerDepth = outerDepth3;
                                xmlResourceParser = xmlResourceParser2;
                                resources = resources2;
                                packageR = packageR2;
                                parentName = parentName2;
                                str3 = str2;
                                strArr = strArr2;
                                c = 0;
                            }
                            resources2 = resources;
                            char c2 = c;
                            strArr2 = strArr;
                            packageR2 = packageR;
                            str2 = str3;
                            sa2 = sa;
                            outerDepth2 = outerDepth;
                            parentName2 = parentName;
                            int i7 = flags;
                            xmlResourceParser2 = xmlResourceParser;
                        }
                        if (!setExported) {
                            a.info.exported = a.intents.size() > 0 ? true : i2;
                        }
                        return a;
                    }
                } else {
                    i3 = 2;
                }
                i2 = 0;
                if (strArr2[i2] != null) {
                }
            }
        }
        i = 0;
        str = sa2.getNonConfigurationString(4, i);
        if (str != null) {
        }
        String str42 = sa2.getNonConfigurationString(8, 1024);
        a.info.taskAffinity = buildTaskAffinityName(packageR2.applicationInfo.packageName, packageR2.applicationInfo.taskAffinity, str42, strArr2);
        a.info.splitName = sa2.getNonConfigurationString(48, 0);
        a.info.flags = 0;
        if (sa2.getBoolean(9, false)) {
        }
        if (sa2.getBoolean(10, false)) {
        }
        if (sa2.getBoolean(11, false)) {
        }
        if (sa2.getBoolean(21, false)) {
        }
        if (sa2.getBoolean(18, false)) {
        }
        if (sa2.getBoolean(12, false)) {
        }
        if (sa2.getBoolean(13, false)) {
        }
        if (sa2.getBoolean(19, (packageR2.applicationInfo.flags & 32) == 0)) {
        }
        if (sa2.getBoolean(22, false)) {
        }
        a.info.flags |= 1024;
        if (sa2.getBoolean(24, false)) {
        }
        if (sa2.getBoolean(54, false)) {
        }
        if (receiver) {
        }
        if (a.info.directBootAware) {
        }
        visibleToEphemeral = sa2.getBoolean(45, false);
        if (visibleToEphemeral) {
        }
        sa2.recycle();
        if (!receiver) {
        }
        i2 = 0;
        if (strArr2[i2] != null) {
        }
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
        } else {
            if (aInfo.isFixedOrientationPortrait()) {
                aInfo.resizeMode = 6;
            } else if (aInfo.isFixedOrientationLandscape()) {
                aInfo.resizeMode = 5;
            } else if (aInfo.isFixedOrientation()) {
                aInfo.resizeMode = 7;
            } else {
                aInfo.resizeMode = 4;
            }
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
        if (HwFoldScreenState.isFoldScreenDevice()) {
            if (owner.mAppMetaData == null || !owner.mAppMetaData.containsKey(METADATA_MIN_ASPECT_RATIO)) {
                Float defaultAspectInXml = HwFrameworkFactory.getHwPackageParser().getDefaultAspect(owner.applicationInfo.packageName);
                if (defaultAspectInXml != null) {
                    owner.applicationInfo.minAspectRatio = defaultAspectInXml.floatValue();
                } else {
                    owner.applicationInfo.minAspectRatio = 1.3333334f;
                }
            } else {
                owner.applicationInfo.minAspectRatio = Math.max(owner.mAppMetaData.getFloat(METADATA_MIN_ASPECT_RATIO, 0.0f), HwFoldScreenState.getScreenFoldFullRatio());
                owner.applicationInfo.hw_extra_flags &= -1025;
            }
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
        ActivityInfo activityInfo = a.info;
        int i = heightType;
        ActivityInfo.WindowLayout windowLayout = new ActivityInfo.WindowLayout(width, widthFraction, height, heightFraction, gravity, minWidth, minHeight);
        activityInfo.windowLayout = windowLayout;
    }

    private Activity parseActivityAlias(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs) throws XmlPullParserException, IOException {
        String targetActivity;
        String targetActivity2;
        Activity target;
        String str;
        boolean z;
        int outerDepth;
        TypedArray sa;
        String str2;
        boolean z2;
        XmlResourceParser xmlResourceParser;
        Resources resources;
        int visibility;
        Package packageR = owner;
        Resources resources2 = res;
        XmlResourceParser xmlResourceParser2 = parser;
        String[] strArr = outError;
        CachedComponentArgs cachedComponentArgs = cachedArgs;
        TypedArray sa2 = resources2.obtainAttributes(xmlResourceParser2, R.styleable.AndroidManifestActivityAlias);
        String targetActivity3 = sa2.getNonConfigurationString(7, 1024);
        if (targetActivity3 == null) {
            strArr[0] = "<activity-alias> does not specify android:targetActivity";
            sa2.recycle();
            return null;
        }
        String targetActivity4 = buildClassName(packageR.applicationInfo.packageName, targetActivity3, strArr);
        if (targetActivity4 == null) {
            sa2.recycle();
            return null;
        }
        if (cachedComponentArgs.mActivityAliasArgs == null) {
            ParseComponentArgs parseComponentArgs = r8;
            targetActivity = targetActivity4;
            ParseComponentArgs parseComponentArgs2 = new ParseComponentArgs(packageR, strArr, 2, 0, 1, 11, 8, 10, this.mSeparateProcesses, 0, 6, 4);
            cachedComponentArgs.mActivityAliasArgs = parseComponentArgs;
            cachedComponentArgs.mActivityAliasArgs.tag = "<activity-alias>";
        } else {
            targetActivity = targetActivity4;
        }
        cachedComponentArgs.mActivityAliasArgs.sa = sa2;
        cachedComponentArgs.mActivityAliasArgs.flags = flags;
        int NA = packageR.activities.size();
        int i = 0;
        while (true) {
            if (i >= NA) {
                targetActivity2 = targetActivity;
                target = null;
                break;
            }
            Activity t = packageR.activities.get(i);
            targetActivity2 = targetActivity;
            if (targetActivity2.equals(t.info.name)) {
                target = t;
                break;
            }
            i++;
            targetActivity = targetActivity2;
        }
        if (target == null) {
            strArr[0] = "<activity-alias> target activity " + targetActivity2 + " not found in manifest";
            sa2.recycle();
            return null;
        }
        ActivityInfo info = new ActivityInfo();
        info.targetActivity = targetActivity2;
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
        info.maxAspectRatio = target.info.maxAspectRatio;
        info.requestedVrComponent = target.info.requestedVrComponent;
        boolean z3 = target.info.directBootAware;
        info.directBootAware = z3;
        info.encryptionAware = z3;
        Activity a = new Activity(cachedComponentArgs.mActivityAliasArgs, info);
        if (strArr[0] != null) {
            sa2.recycle();
            return null;
        }
        boolean setExported = sa2.hasValue(5);
        if (setExported) {
            a.info.exported = sa2.getBoolean(5, false);
        }
        String str3 = sa2.getNonConfigurationString(3, 0);
        if (str3 != null) {
            a.info.permission = str3.length() > 0 ? str3.toString().intern() : null;
        }
        String parentName = sa2.getNonConfigurationString(9, 1024);
        if (parentName != null) {
            String parentClassName = buildClassName(a.info.packageName, parentName, strArr);
            if (strArr[0] == null) {
                str = str3;
                a.info.parentActivityName = parentClassName;
            } else {
                str = str3;
                String str4 = parentClassName;
                Log.e(TAG, "Activity alias " + a.info.name + " specified invalid parentActivityName " + parentName);
                strArr[0] = null;
            }
        } else {
            str = str3;
        }
        boolean z4 = true;
        boolean visibleToEphemeral = (a.info.flags & 1048576) != 0;
        sa2.recycle();
        if (strArr[0] != null) {
            return null;
        }
        int outerDepth2 = parser.getDepth();
        while (true) {
            int outerDepth3 = outerDepth2;
            int outerDepth4 = parser.next();
            int type = outerDepth4;
            if (outerDepth4 == z4) {
                int i2 = outerDepth3;
                XmlResourceParser xmlResourceParser3 = xmlResourceParser2;
                Resources resources3 = resources2;
                z = z4;
                String str5 = str;
                int i3 = type;
                break;
            }
            int type2 = type;
            if (type2 == 3 && parser.getDepth() <= outerDepth3) {
                int i4 = type2;
                TypedArray typedArray = sa2;
                int i5 = outerDepth3;
                XmlResourceParser xmlResourceParser4 = xmlResourceParser2;
                Resources resources4 = resources2;
                String str6 = str;
                z = true;
                break;
            }
            if (type2 == 3) {
                sa = sa2;
                outerDepth = outerDepth3;
                xmlResourceParser = xmlResourceParser2;
                resources = resources2;
                str2 = str;
                z2 = true;
            } else if (type2 == 4) {
                sa = sa2;
                outerDepth = outerDepth3;
                xmlResourceParser = xmlResourceParser2;
                resources = resources2;
                str2 = str;
                z2 = true;
            } else {
                int type3 = type2;
                if (parser.getName().equals("intent-filter") != 0) {
                    ActivityIntentInfo intent = new ActivityIntentInfo(a);
                    str2 = str;
                    int i6 = type3;
                    sa = sa2;
                    outerDepth = outerDepth3;
                    ActivityIntentInfo intent2 = intent;
                    z2 = true;
                    if (!parseIntent(resources2, xmlResourceParser2, true, true, intent, outError)) {
                        return null;
                    }
                    if (intent2.countActions() == 0) {
                        Slog.w(TAG, "No actions in intent filter at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                    } else {
                        a.order = Math.max(intent2.getOrder(), a.order);
                        a.intents.add(intent2);
                    }
                    if (visibleToEphemeral) {
                        visibility = 1;
                    } else {
                        visibility = isImplicitlyExposedIntent(intent2) ? 2 : 0;
                    }
                    intent2.setVisibilityToInstantApp(visibility);
                    if (intent2.isVisibleToInstantApp()) {
                        a.info.flags |= 1048576;
                    }
                    if (intent2.isImplicitlyVisibleToInstantApp()) {
                        a.info.flags |= 2097152;
                    }
                    xmlResourceParser = parser;
                    String[] strArr2 = outError;
                    resources = res;
                } else {
                    sa = sa2;
                    outerDepth = outerDepth3;
                    str2 = str;
                    int i7 = type3;
                    z2 = true;
                    if (parser.getName().equals("meta-data")) {
                        xmlResourceParser = parser;
                        resources = res;
                        Bundle parseMetaData = parseMetaData(resources, xmlResourceParser, a.metaData, outError);
                        a.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return null;
                        }
                        HwThemeManager.addSimpleUIConfig(a);
                    } else {
                        xmlResourceParser = parser;
                        String[] strArr3 = outError;
                        resources = res;
                        Slog.w(TAG, "Unknown element under <activity-alias>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            resources2 = resources;
            xmlResourceParser2 = xmlResourceParser;
            z4 = z2;
            str = str2;
            sa2 = sa;
            outerDepth2 = outerDepth;
            int i8 = flags;
        }
        if (!setExported) {
            a.info.exported = a.intents.size() > 0 ? z : false;
        }
        return a;
    }

    private Provider parseProvider(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs) throws XmlPullParserException, IOException {
        TypedArray sa;
        Package packageR = owner;
        CachedComponentArgs cachedComponentArgs = cachedArgs;
        TypedArray sa2 = res.obtainAttributes(parser, R.styleable.AndroidManifestProvider);
        if (cachedComponentArgs.mProviderArgs == null) {
            ParseComponentArgs parseComponentArgs = r0;
            sa = sa2;
            ParseComponentArgs parseComponentArgs2 = new ParseComponentArgs(packageR, outError, 2, 0, 1, 19, 15, 17, this.mSeparateProcesses, 8, 14, 6);
            cachedComponentArgs.mProviderArgs = parseComponentArgs;
            cachedComponentArgs.mProviderArgs.tag = "<provider>";
        } else {
            sa = sa2;
        }
        TypedArray sa3 = sa;
        cachedComponentArgs.mProviderArgs.sa = sa3;
        cachedComponentArgs.mProviderArgs.flags = flags;
        Provider p = new Provider(cachedComponentArgs.mProviderArgs, new ProviderInfo());
        if (outError[0] != null) {
            sa3.recycle();
            return null;
        }
        boolean providerExportedDefault = false;
        if (packageR.applicationInfo.targetSdkVersion < 17) {
            providerExportedDefault = true;
        }
        p.info.exported = sa3.getBoolean(7, providerExportedDefault);
        String cpname = sa3.getNonConfigurationString(10, 0);
        p.info.isSyncable = sa3.getBoolean(11, false);
        String permission = sa3.getNonConfigurationString(3, 0);
        String str = sa3.getNonConfigurationString(4, 0);
        if (str == null) {
            str = permission;
        }
        if (str == null) {
            p.info.readPermission = packageR.applicationInfo.permission;
        } else {
            p.info.readPermission = str.length() > 0 ? str.toString().intern() : null;
        }
        String str2 = sa3.getNonConfigurationString(5, 0);
        if (str2 == null) {
            str2 = permission;
        }
        String str3 = str2;
        if (str3 == null) {
            p.info.writePermission = packageR.applicationInfo.permission;
        } else {
            p.info.writePermission = str3.length() > 0 ? str3.toString().intern() : null;
        }
        p.info.grantUriPermissions = sa3.getBoolean(13, false);
        p.info.multiprocess = sa3.getBoolean(9, false);
        p.info.initOrder = sa3.getInt(12, 0);
        p.info.splitName = sa3.getNonConfigurationString(21, 0);
        p.info.flags = 0;
        if (sa3.getBoolean(16, false)) {
            p.info.flags |= 1073741824;
        }
        ProviderInfo providerInfo = p.info;
        ProviderInfo providerInfo2 = p.info;
        boolean z = sa3.getBoolean(18, false);
        providerInfo2.directBootAware = z;
        providerInfo.encryptionAware = z;
        if (p.info.directBootAware) {
            packageR.applicationInfo.privateFlags |= 256;
        }
        boolean visibleToEphemeral = sa3.getBoolean(20, false);
        if (visibleToEphemeral) {
            p.info.flags |= 1048576;
            packageR.visibleToInstantApps = true;
        }
        sa3.recycle();
        if ((packageR.applicationInfo.privateFlags & 2) != 0 && p.info.processName == packageR.packageName) {
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
            String str4 = str3;
            if (!parseProviderTags(res, parser, visibleToEphemeral, p, outError)) {
                return null;
            }
            return p;
        }
    }

    private boolean parseProviderTags(Resources res, XmlResourceParser parser, boolean visibleToEphemeral, Provider outInfo, String[] outError) throws XmlPullParserException, IOException {
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        Provider provider = outInfo;
        int outerDepth = parser.getDepth();
        while (true) {
            int outerDepth2 = outerDepth;
            int outerDepth3 = parser.next();
            int type = outerDepth3;
            if (outerDepth3 == 1 || (type == 3 && parser.getDepth() <= outerDepth2)) {
                String[] strArr = outError;
            } else {
                if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals("intent-filter")) {
                        ProviderIntentInfo intent = new ProviderIntentInfo(provider);
                        if (!parseIntent(resources, xmlResourceParser, true, false, intent, outError)) {
                            return false;
                        }
                        if (visibleToEphemeral) {
                            intent.setVisibilityToInstantApp(1);
                            provider.info.flags |= 1048576;
                        }
                        provider.order = Math.max(intent.getOrder(), provider.order);
                        provider.intents.add(intent);
                    } else {
                        if (parser.getName().equals("meta-data")) {
                            Bundle parseMetaData = parseMetaData(resources, xmlResourceParser, provider.metaData, outError);
                            provider.metaData = parseMetaData;
                            if (parseMetaData == null) {
                                return false;
                            }
                        } else {
                            String[] strArr2 = outError;
                            if (parser.getName().equals("grant-uri-permission")) {
                                TypedArray sa = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestGrantUriPermission);
                                PatternMatcher pa = null;
                                String str = sa.getNonConfigurationString(0, 0);
                                if (str != null) {
                                    pa = new PatternMatcher(str, 0);
                                }
                                String str2 = sa.getNonConfigurationString(1, 0);
                                if (str2 != null) {
                                    pa = new PatternMatcher(str2, 1);
                                }
                                String str3 = sa.getNonConfigurationString(2, 0);
                                if (str3 != null) {
                                    pa = new PatternMatcher(str3, 2);
                                }
                                sa.recycle();
                                if (pa != null) {
                                    if (provider.info.uriPermissionPatterns == null) {
                                        provider.info.uriPermissionPatterns = new PatternMatcher[1];
                                        provider.info.uriPermissionPatterns[0] = pa;
                                    } else {
                                        int N = provider.info.uriPermissionPatterns.length;
                                        PatternMatcher[] newp = new PatternMatcher[(N + 1)];
                                        System.arraycopy(provider.info.uriPermissionPatterns, 0, newp, 0, N);
                                        newp[N] = pa;
                                        provider.info.uriPermissionPatterns = newp;
                                    }
                                    provider.info.grantUriPermissions = true;
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    Slog.w(TAG, "Unknown element under <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            } else if (parser.getName().equals("path-permission")) {
                                TypedArray sa2 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestPathPermission);
                                PathPermission pa2 = null;
                                String permission = sa2.getNonConfigurationString(0, 0);
                                String readPermission = sa2.getNonConfigurationString(1, 0);
                                if (readPermission == null) {
                                    readPermission = permission;
                                }
                                String readPermission2 = readPermission;
                                String writePermission = sa2.getNonConfigurationString(2, 0);
                                if (writePermission == null) {
                                    writePermission = permission;
                                }
                                String writePermission2 = writePermission;
                                boolean havePerm = false;
                                if (readPermission2 != null) {
                                    readPermission2 = readPermission2.intern();
                                    havePerm = true;
                                }
                                if (writePermission2 != null) {
                                    writePermission2 = writePermission2.intern();
                                    havePerm = true;
                                }
                                if (!havePerm) {
                                    Slog.w(TAG, "No readPermission or writePermssion for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    String path = sa2.getNonConfigurationString(3, 0);
                                    if (path != null) {
                                        pa2 = new PathPermission(path, 0, readPermission2, writePermission2);
                                    }
                                    String path2 = sa2.getNonConfigurationString(4, 0);
                                    if (path2 != null) {
                                        pa2 = new PathPermission(path2, 1, readPermission2, writePermission2);
                                    }
                                    String path3 = sa2.getNonConfigurationString(5, 0);
                                    if (path3 != null) {
                                        pa2 = new PathPermission(path3, 2, readPermission2, writePermission2);
                                    }
                                    String path4 = sa2.getNonConfigurationString(6, 0);
                                    if (path4 != null) {
                                        pa2 = new PathPermission(path4, 3, readPermission2, writePermission2);
                                    }
                                    sa2.recycle();
                                    if (pa2 != null) {
                                        if (provider.info.pathPermissions == null) {
                                            provider.info.pathPermissions = new PathPermission[1];
                                            provider.info.pathPermissions[0] = pa2;
                                            String str4 = path4;
                                        } else {
                                            int N2 = provider.info.pathPermissions.length;
                                            PathPermission[] newp2 = new PathPermission[(N2 + 1)];
                                            String str5 = path4;
                                            System.arraycopy(provider.info.pathPermissions, 0, newp2, 0, N2);
                                            newp2[N2] = pa2;
                                            provider.info.pathPermissions = newp2;
                                        }
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        String str6 = path4;
                                        Slog.w(TAG, "No path, pathPrefix, or pathPattern for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            } else {
                                Slog.w(TAG, "Unknown element under <provider>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                        outerDepth = outerDepth2;
                    }
                }
                String[] strArr3 = outError;
                outerDepth = outerDepth2;
            }
        }
        String[] strArr4 = outError;
        return true;
    }

    private Service parseService(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, CachedComponentArgs cachedArgs) throws XmlPullParserException, IOException {
        boolean z;
        TypedArray sa;
        int outerDepth;
        XmlResourceParser xmlResourceParser;
        String[] strArr;
        char c;
        Resources resources;
        boolean z2;
        Package packageR = owner;
        Resources resources2 = res;
        XmlResourceParser xmlResourceParser2 = parser;
        String[] strArr2 = outError;
        CachedComponentArgs cachedComponentArgs = cachedArgs;
        TypedArray sa2 = resources2.obtainAttributes(xmlResourceParser2, R.styleable.AndroidManifestService);
        if (cachedComponentArgs.mServiceArgs == null) {
            ParseComponentArgs parseComponentArgs = new ParseComponentArgs(packageR, strArr2, 2, 0, 1, 15, 8, 12, this.mSeparateProcesses, 6, 7, 4);
            cachedComponentArgs.mServiceArgs = parseComponentArgs;
            cachedComponentArgs.mServiceArgs.tag = "<service>";
        }
        cachedComponentArgs.mServiceArgs.sa = sa2;
        cachedComponentArgs.mServiceArgs.flags = flags;
        Service s = new Service(cachedComponentArgs.mServiceArgs, new ServiceInfo());
        if (strArr2[0] != null) {
            sa2.recycle();
            return null;
        }
        boolean setExported = sa2.hasValue(5);
        if (setExported) {
            s.info.exported = sa2.getBoolean(5, false);
        }
        String str = sa2.getNonConfigurationString(3, 0);
        if (str == null) {
            s.info.permission = packageR.applicationInfo.permission;
        } else {
            s.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        s.info.splitName = sa2.getNonConfigurationString(17, 0);
        s.info.flags = 0;
        boolean z3 = true;
        if (sa2.getBoolean(9, false)) {
            s.info.flags |= 1;
        }
        if (sa2.getBoolean(10, false)) {
            s.info.flags |= 2;
        }
        if (sa2.getBoolean(14, false)) {
            s.info.flags |= 4;
        }
        if (sa2.getBoolean(11, false)) {
            s.info.flags |= 1073741824;
        }
        ServiceInfo serviceInfo = s.info;
        ServiceInfo serviceInfo2 = s.info;
        boolean z4 = sa2.getBoolean(13, false);
        serviceInfo2.directBootAware = z4;
        serviceInfo.encryptionAware = z4;
        if (s.info.directBootAware) {
            packageR.applicationInfo.privateFlags |= 256;
        }
        boolean visibleToEphemeral = sa2.getBoolean(16, false);
        if (visibleToEphemeral) {
            s.info.flags |= 1048576;
            packageR.visibleToInstantApps = true;
        }
        sa2.recycle();
        if ((packageR.applicationInfo.privateFlags & 2) == 0 || s.info.processName != packageR.packageName) {
            int outerDepth2 = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == z3) {
                    TypedArray typedArray = sa2;
                    Resources resources3 = resources2;
                    z = z3;
                    int i = type;
                    XmlResourceParser xmlResourceParser3 = xmlResourceParser2;
                    String[] strArr3 = strArr2;
                    break;
                }
                int type2 = type;
                if (type2 == 3 && parser.getDepth() <= outerDepth2) {
                    int i2 = outerDepth2;
                    TypedArray typedArray2 = sa2;
                    XmlResourceParser xmlResourceParser4 = xmlResourceParser2;
                    Resources resources4 = resources2;
                    z = true;
                    String[] strArr4 = strArr2;
                    break;
                }
                if (type2 == 3) {
                    outerDepth = outerDepth2;
                    sa = sa2;
                    xmlResourceParser = xmlResourceParser2;
                    resources = resources2;
                    z2 = true;
                    strArr = strArr2;
                    c = 0;
                } else if (type2 == 4) {
                    outerDepth = outerDepth2;
                    sa = sa2;
                    xmlResourceParser = xmlResourceParser2;
                    resources = resources2;
                    z2 = true;
                    strArr = strArr2;
                    c = 0;
                } else if (parser.getName().equals("intent-filter")) {
                    ServiceIntentInfo intent = new ServiceIntentInfo(s);
                    outerDepth = outerDepth2;
                    sa = sa2;
                    xmlResourceParser = xmlResourceParser2;
                    if (!parseIntent(resources2, xmlResourceParser2, true, false, intent, outError)) {
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
                    strArr = outError;
                    resources = res;
                } else {
                    outerDepth = outerDepth2;
                    sa = sa2;
                    xmlResourceParser = xmlResourceParser2;
                    z2 = true;
                    c = 0;
                    if (parser.getName().equals("meta-data")) {
                        strArr = outError;
                        resources = res;
                        Bundle parseMetaData = parseMetaData(resources, xmlResourceParser, s.metaData, strArr);
                        s.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return null;
                        }
                    } else {
                        strArr = outError;
                        resources = res;
                        Slog.w(TAG, "Unknown element under <service>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
                Package packageR2 = owner;
                resources2 = resources;
                char c2 = c;
                strArr2 = strArr;
                xmlResourceParser2 = xmlResourceParser;
                sa2 = sa;
                CachedComponentArgs cachedComponentArgs2 = cachedArgs;
                z3 = z2;
                outerDepth2 = outerDepth;
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
        strArr2[0] = "Heavy-weight applications can not have services in main process";
        return null;
    }

    private boolean isImplicitlyExposedIntent(IntentInfo intent) {
        return intent.hasCategory(Intent.CATEGORY_BROWSABLE) || intent.hasAction(Intent.ACTION_SEND) || intent.hasAction(Intent.ACTION_SENDTO) || intent.hasAction(Intent.ACTION_SEND_MULTIPLE);
    }

    private boolean parseAllMetaData(Resources res, XmlResourceParser parser, String tag, Component<?> outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
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
                    Slog.w(TAG, "Unknown element under " + tag + ": " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
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
                Slog.w(TAG, "<meta-data> only supports string, integer, float, color, boolean, and resource reference types: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
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
                return KeyFactory.getInstance("RSA").generatePublic(keySpec);
            } catch (NoSuchAlgorithmException e) {
                Slog.wtf(TAG, "Could not parse public key: RSA KeyFactory not included in build");
                try {
                    return KeyFactory.getInstance("EC").generatePublic(keySpec);
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
                return KeyFactory.getInstance("EC").generatePublic(keySpec);
            }
        } catch (IllegalArgumentException e7) {
            Slog.w(TAG, "Could not parse verifier public key; invalid Base64");
            return null;
        }
    }

    /* JADX WARNING: type inference failed for: r2v9, types: [boolean, int] */
    /* JADX WARNING: type inference failed for: r2v10 */
    /* JADX WARNING: type inference failed for: r2v12 */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00cc, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f5, code lost:
        return false;
     */
    private boolean parseIntent(Resources res, XmlResourceParser parser, boolean allowGlobs, boolean allowAutoVerify, IntentInfo outInfo, String[] outError) throws XmlPullParserException, IOException {
        int roundIconVal;
        int outerDepth;
        int priority;
        TypedArray sa;
        boolean z;
        int priority2;
        int outerDepth2;
        boolean z2;
        TypedArray sa2;
        boolean z3;
        ? r2;
        int i;
        Resources resources = res;
        XmlResourceParser xmlResourceParser = parser;
        IntentInfo intentInfo = outInfo;
        TypedArray sa3 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestIntentFilter);
        int priority3 = sa3.getInt(2, 0);
        intentInfo.setPriority(priority3);
        intentInfo.setOrder(sa3.getInt(3, 0));
        TypedValue v = sa3.peekValue(0);
        if (v != null) {
            int i2 = v.resourceId;
            intentInfo.labelRes = i2;
            if (i2 == 0) {
                intentInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        if (Resources.getSystem().getBoolean(R.bool.config_useRoundIcon)) {
            roundIconVal = sa3.getResourceId(7, 0);
        } else {
            roundIconVal = 0;
        }
        int i3 = 1;
        if (roundIconVal != 0) {
            intentInfo.icon = roundIconVal;
        } else {
            intentInfo.icon = sa3.getResourceId(1, 0);
        }
        intentInfo.logo = sa3.getResourceId(4, 0);
        intentInfo.banner = sa3.getResourceId(5, 0);
        if (allowAutoVerify) {
            intentInfo.setAutoVerify(sa3.getBoolean(6, false));
        }
        sa3.recycle();
        int outerDepth3 = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == i3) {
                TypedArray typedArray = sa3;
                int i4 = priority3;
                int i5 = outerDepth3;
                int i6 = type;
                break;
            }
            int type2 = type;
            if (type2 == 3 && parser.getDepth() <= outerDepth3) {
                TypedArray typedArray2 = sa3;
                int i7 = priority3;
                int i8 = outerDepth3;
                break;
            }
            if (type2 == 3) {
                sa = sa3;
                priority2 = priority3;
                outerDepth2 = outerDepth3;
                z = false;
            } else if (type2 == 4) {
                sa = sa3;
                priority2 = priority3;
                outerDepth2 = outerDepth3;
                z = false;
            } else {
                String nodeName = parser.getName();
                if (nodeName.equals("action")) {
                    sa2 = sa3;
                    String value = xmlResourceParser.getAttributeValue(ANDROID_RESOURCES, MidiDeviceInfo.PROPERTY_NAME);
                    if (value == null || value == "") {
                        outError[0] = "No value supplied for <android:name>";
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                        intentInfo.addAction(value);
                    }
                } else {
                    sa2 = sa3;
                    if (nodeName.equals("category")) {
                        String value2 = xmlResourceParser.getAttributeValue(ANDROID_RESOURCES, MidiDeviceInfo.PROPERTY_NAME);
                        if (value2 == null || value2 == "") {
                            int i9 = priority3;
                            outError[0] = "No value supplied for <android:name>";
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                            intentInfo.addCategory(value2);
                        }
                    } else {
                        priority = priority3;
                        if (nodeName.equals(ActivityManagerInternal.ASSIST_KEY_DATA)) {
                            TypedArray sa4 = resources.obtainAttributes(xmlResourceParser, R.styleable.AndroidManifestData);
                            String str = sa4.getNonConfigurationString(0, 0);
                            if (str != null) {
                                try {
                                    intentInfo.addDataType(str);
                                    String str2 = str;
                                    r2 = 0;
                                } catch (IntentFilter.MalformedMimeTypeException e) {
                                    IntentFilter.MalformedMimeTypeException malformedMimeTypeException = e;
                                    String str3 = str;
                                    outError[0] = e.toString();
                                    sa4.recycle();
                                    return false;
                                }
                            } else {
                                r2 = 0;
                            }
                            String str4 = sa4.getNonConfigurationString(1, r2);
                            if (str4 != null) {
                                intentInfo.addDataScheme(str4);
                            }
                            String str5 = sa4.getNonConfigurationString(7, r2);
                            if (str5 != null) {
                                intentInfo.addDataSchemeSpecificPart(str5, r2);
                            }
                            String str6 = sa4.getNonConfigurationString(8, r2);
                            if (str6 != null) {
                                intentInfo.addDataSchemeSpecificPart(str6, 1);
                            }
                            String str7 = sa4.getNonConfigurationString(9, r2);
                            if (str7 == null) {
                                i = 2;
                            } else if (!allowGlobs) {
                                outError[r2] = "sspPattern not allowed here; ssp must be literal";
                                return r2;
                            } else {
                                i = 2;
                                intentInfo.addDataSchemeSpecificPart(str7, 2);
                            }
                            String str8 = str7;
                            String str9 = sa4.getNonConfigurationString(i, r2);
                            outerDepth = outerDepth3;
                            String port = sa4.getNonConfigurationString(3, r2);
                            if (str9 != null) {
                                intentInfo.addDataAuthority(str9, port);
                            }
                            String str10 = str9;
                            String str11 = sa4.getNonConfigurationString(4, r2);
                            if (str11 != null) {
                                intentInfo.addDataPath(str11, r2);
                            }
                            String str12 = sa4.getNonConfigurationString(5, r2);
                            if (str12 != null) {
                                intentInfo.addDataPath(str12, 1);
                            }
                            String str13 = sa4.getNonConfigurationString(6, r2);
                            if (str13 != null) {
                                if (!allowGlobs) {
                                    outError[r2] = "pathPattern not allowed here; path must be literal";
                                    return r2;
                                }
                                intentInfo.addDataPath(str13, 2);
                            }
                            String str14 = sa4.getNonConfigurationString(10, r2);
                            if (str14 != null) {
                                if (!allowGlobs) {
                                    outError[r2] = "pathAdvancedPattern not allowed here; path must be literal";
                                    return r2;
                                }
                                intentInfo.addDataPath(str14, 3);
                            }
                            sa4.recycle();
                            XmlUtils.skipCurrentTag(parser);
                            sa3 = sa4;
                            z2 = r2;
                            boolean z4 = z2;
                            priority3 = priority;
                            outerDepth3 = outerDepth;
                            resources = res;
                            i3 = 1;
                        } else {
                            boolean z5 = false;
                            outerDepth = outerDepth3;
                            if (nodeName.equals("state")) {
                                parseIntentFilterState(xmlResourceParser, ANDROID_RESOURCES, intentInfo);
                                XmlUtils.skipCurrentTag(parser);
                                z3 = z5;
                            } else {
                                Slog.w(TAG, "Unknown element under <intent-filter>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                                z3 = z5;
                            }
                            sa3 = sa2;
                            z2 = z3;
                            boolean z42 = z2;
                            priority3 = priority;
                            outerDepth3 = outerDepth;
                            resources = res;
                            i3 = 1;
                        }
                    }
                }
                priority = priority3;
                outerDepth = outerDepth3;
                z3 = false;
                sa3 = sa2;
                z2 = z3;
                boolean z422 = z2;
                priority3 = priority;
                outerDepth3 = outerDepth;
                resources = res;
                i3 = 1;
            }
            boolean z6 = z;
            sa3 = sa;
            priority3 = priority;
            outerDepth3 = outerDepth;
            resources = res;
            i3 = 1;
        }
        intentInfo.hasDefault = intentInfo.hasCategory(Intent.CATEGORY_DEFAULT);
        return true;
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
        if (((flags & 1024) == 0 || p.usesLibraryFiles == null) && p.staticSharedLibName == null) {
            return false;
        }
        return true;
    }

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
    }

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

    public static void setCompatibilityModeEnabled(boolean compatibilityModeEnabled) {
        sCompatibilityModeEnabled = compatibilityModeEnabled;
    }

    private void parseIntentFilterState(XmlResourceParser parser, String android_resources, IntentInfo outInfo) {
        XmlResourceParser xmlResourceParser = parser;
        String str = android_resources;
        String name = xmlResourceParser.getAttributeValue(str, MidiDeviceInfo.PROPERTY_NAME);
        if (name == null) {
            Log.w(TAG, "No value supplied for <android:name>");
            return;
        }
        String value = xmlResourceParser.getAttributeValue(str, Slice.SUBTYPE_VALUE);
        if (value == null) {
            Log.w(TAG, "No value supplied for <android:value>");
            return;
        }
        String[] items = name.split("@");
        if (items.length != 2) {
            Log.w(TAG, "state name error");
            return;
        }
        char c = 0;
        String action = items[0];
        if (!items[1].equals("ImplicitBroadcastExpand")) {
            Log.w(TAG, "state flag error");
            return;
        }
        String[] filters = value.split("\\|");
        int i = 0;
        while (i < filters.length) {
            String[] state = filters[i].split("=");
            if (state.length != 2) {
                Log.w(TAG, "value format error");
                return;
            }
            outInfo.addActionFilter(action, state[c], state[1]);
            i++;
            c = 0;
        }
        IntentInfo intentInfo = outInfo;
    }
}
