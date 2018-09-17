package android.content.pm;

import android.app.PackageDeleteObserver;
import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageParser.CallbackImpl;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import android.util.AndroidException;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import dalvik.system.VMRuntime;
import java.io.File;
import java.util.List;

public abstract class PackageManager {
    public static final String ACTION_CLEAN_EXTERNAL_STORAGE = "android.content.pm.CLEAN_EXTERNAL_STORAGE";
    public static final String ACTION_REQUEST_PERMISSIONS = "android.content.pm.action.REQUEST_PERMISSIONS";
    public static final String ACTION_UNINSTALL_UPDATE = "android.intent.action.UNINSTALL_UPDATE";
    public static final boolean APPLY_DEFAULT_TO_DEVICE_PROTECTED_STORAGE = true;
    public static final int COMPONENT_ENABLED_STATE_DEFAULT = 0;
    public static final int COMPONENT_ENABLED_STATE_DISABLED = 2;
    public static final int COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED = 4;
    public static final int COMPONENT_ENABLED_STATE_DISABLED_USER = 3;
    public static final int COMPONENT_ENABLED_STATE_ENABLED = 1;
    public static final int DELETE_ALL_USERS = 2;
    public static final int DELETE_DONT_KILL_APP = 8;
    public static final int DELETE_FAILED_ABORTED = -5;
    public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -2;
    public static final int DELETE_FAILED_INTERNAL_ERROR = -1;
    public static final int DELETE_FAILED_OWNER_BLOCKED = -4;
    public static final int DELETE_FAILED_USED_SHARED_LIBRARY = -6;
    public static final int DELETE_FAILED_USER_RESTRICTED = -3;
    public static final int DELETE_KEEP_DATA = 1;
    public static final int DELETE_SUCCEEDED = 1;
    public static final int DELETE_SYSTEM_APP = 4;
    public static final int DONT_KILL_APP = 1;
    public static final String EXTRA_FAILURE_EXISTING_PACKAGE = "android.content.pm.extra.FAILURE_EXISTING_PACKAGE";
    public static final String EXTRA_FAILURE_EXISTING_PERMISSION = "android.content.pm.extra.FAILURE_EXISTING_PERMISSION";
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_HOSTS = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_HOSTS";
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_ID = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_ID";
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_PACKAGE_NAME = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_PACKAGE_NAME";
    public static final String EXTRA_INTENT_FILTER_VERIFICATION_URI_SCHEME = "android.content.pm.extra.INTENT_FILTER_VERIFICATION_URI_SCHEME";
    public static final String EXTRA_MOVE_ID = "android.content.pm.extra.MOVE_ID";
    public static final String EXTRA_REQUEST_PERMISSIONS_NAMES = "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES";
    public static final String EXTRA_REQUEST_PERMISSIONS_RESULTS = "android.content.pm.extra.REQUEST_PERMISSIONS_RESULTS";
    public static final String EXTRA_VERIFICATION_ID = "android.content.pm.extra.VERIFICATION_ID";
    public static final String EXTRA_VERIFICATION_INSTALLER_PACKAGE = "android.content.pm.extra.VERIFICATION_INSTALLER_PACKAGE";
    public static final String EXTRA_VERIFICATION_INSTALLER_UID = "android.content.pm.extra.VERIFICATION_INSTALLER_UID";
    public static final String EXTRA_VERIFICATION_INSTALL_FLAGS = "android.content.pm.extra.VERIFICATION_INSTALL_FLAGS";
    public static final String EXTRA_VERIFICATION_PACKAGE_NAME = "android.content.pm.extra.VERIFICATION_PACKAGE_NAME";
    public static final String EXTRA_VERIFICATION_RESULT = "android.content.pm.extra.VERIFICATION_RESULT";
    public static final String EXTRA_VERIFICATION_URI = "android.content.pm.extra.VERIFICATION_URI";
    public static final String EXTRA_VERIFICATION_VERSION_CODE = "android.content.pm.extra.VERIFICATION_VERSION_CODE";
    public static final String FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS = "android.software.activities_on_secondary_displays";
    public static final String FEATURE_APP_WIDGETS = "android.software.app_widgets";
    public static final String FEATURE_AUDIO_LOW_LATENCY = "android.hardware.audio.low_latency";
    public static final String FEATURE_AUDIO_OUTPUT = "android.hardware.audio.output";
    public static final String FEATURE_AUDIO_PRO = "android.hardware.audio.pro";
    public static final String FEATURE_AUTOFILL = "android.software.autofill";
    public static final String FEATURE_AUTOMOTIVE = "android.hardware.type.automotive";
    public static final String FEATURE_BACKUP = "android.software.backup";
    public static final String FEATURE_BLUETOOTH = "android.hardware.bluetooth";
    public static final String FEATURE_BLUETOOTH_LE = "android.hardware.bluetooth_le";
    public static final String FEATURE_CAMERA = "android.hardware.camera";
    public static final String FEATURE_CAMERA_ANY = "android.hardware.camera.any";
    public static final String FEATURE_CAMERA_AUTOFOCUS = "android.hardware.camera.autofocus";
    public static final String FEATURE_CAMERA_CAPABILITY_MANUAL_POST_PROCESSING = "android.hardware.camera.capability.manual_post_processing";
    public static final String FEATURE_CAMERA_CAPABILITY_MANUAL_SENSOR = "android.hardware.camera.capability.manual_sensor";
    public static final String FEATURE_CAMERA_CAPABILITY_RAW = "android.hardware.camera.capability.raw";
    public static final String FEATURE_CAMERA_EXTERNAL = "android.hardware.camera.external";
    public static final String FEATURE_CAMERA_FLASH = "android.hardware.camera.flash";
    public static final String FEATURE_CAMERA_FRONT = "android.hardware.camera.front";
    public static final String FEATURE_CAMERA_LEVEL_FULL = "android.hardware.camera.level.full";
    public static final String FEATURE_COMPANION_DEVICE_SETUP = "android.software.companion_device_setup";
    public static final String FEATURE_CONNECTION_SERVICE = "android.software.connectionservice";
    public static final String FEATURE_CONSUMER_IR = "android.hardware.consumerir";
    public static final String FEATURE_CTS = "android.software.cts";
    public static final String FEATURE_DEVICE_ADMIN = "android.software.device_admin";
    public static final String FEATURE_EMBEDDED = "android.hardware.type.embedded";
    public static final String FEATURE_ETHERNET = "android.hardware.ethernet";
    public static final String FEATURE_FAKETOUCH = "android.hardware.faketouch";
    public static final String FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT = "android.hardware.faketouch.multitouch.distinct";
    public static final String FEATURE_FAKETOUCH_MULTITOUCH_JAZZHAND = "android.hardware.faketouch.multitouch.jazzhand";
    public static final String FEATURE_FILE_BASED_ENCRYPTION = "android.software.file_based_encryption";
    public static final String FEATURE_FINGERPRINT = "android.hardware.fingerprint";
    public static final String FEATURE_FREEFORM_WINDOW_MANAGEMENT = "android.software.freeform_window_management";
    public static final String FEATURE_GAMEPAD = "android.hardware.gamepad";
    public static final String FEATURE_HDMI_CEC = "android.hardware.hdmi.cec";
    public static final String FEATURE_HIFI_SENSORS = "android.hardware.sensor.hifi_sensors";
    public static final String FEATURE_HOME_SCREEN = "android.software.home_screen";
    public static final String FEATURE_INPUT_METHODS = "android.software.input_methods";
    public static final String FEATURE_LEANBACK = "android.software.leanback";
    public static final String FEATURE_LEANBACK_ONLY = "android.software.leanback_only";
    public static final String FEATURE_LIVE_TV = "android.software.live_tv";
    public static final String FEATURE_LIVE_WALLPAPER = "android.software.live_wallpaper";
    public static final String FEATURE_LOCATION = "android.hardware.location";
    public static final String FEATURE_LOCATION_GPS = "android.hardware.location.gps";
    public static final String FEATURE_LOCATION_NETWORK = "android.hardware.location.network";
    public static final String FEATURE_MANAGED_PROFILES = "android.software.managed_users";
    public static final String FEATURE_MANAGED_USERS = "android.software.managed_users";
    public static final String FEATURE_MICROPHONE = "android.hardware.microphone";
    public static final String FEATURE_MIDI = "android.software.midi";
    public static final String FEATURE_NFC = "android.hardware.nfc";
    public static final String FEATURE_NFC_ANY = "android.hardware.nfc.any";
    @Deprecated
    public static final String FEATURE_NFC_HCE = "android.hardware.nfc.hce";
    public static final String FEATURE_NFC_HOST_CARD_EMULATION = "android.hardware.nfc.hce";
    public static final String FEATURE_NFC_HOST_CARD_EMULATION_NFCF = "android.hardware.nfc.hcef";
    public static final String FEATURE_OPENGLES_EXTENSION_PACK = "android.hardware.opengles.aep";
    public static final String FEATURE_PICTURE_IN_PICTURE = "android.software.picture_in_picture";
    public static final String FEATURE_PRINTING = "android.software.print";
    public static final String FEATURE_SCREEN_LANDSCAPE = "android.hardware.screen.landscape";
    public static final String FEATURE_SCREEN_PORTRAIT = "android.hardware.screen.portrait";
    public static final String FEATURE_SECURELY_REMOVES_USERS = "android.software.securely_removes_users";
    public static final String FEATURE_SENSOR_ACCELEROMETER = "android.hardware.sensor.accelerometer";
    public static final String FEATURE_SENSOR_AMBIENT_TEMPERATURE = "android.hardware.sensor.ambient_temperature";
    public static final String FEATURE_SENSOR_BAROMETER = "android.hardware.sensor.barometer";
    public static final String FEATURE_SENSOR_COMPASS = "android.hardware.sensor.compass";
    public static final String FEATURE_SENSOR_GYROSCOPE = "android.hardware.sensor.gyroscope";
    public static final String FEATURE_SENSOR_HEART_RATE = "android.hardware.sensor.heartrate";
    public static final String FEATURE_SENSOR_HEART_RATE_ECG = "android.hardware.sensor.heartrate.ecg";
    public static final String FEATURE_SENSOR_LIGHT = "android.hardware.sensor.light";
    public static final String FEATURE_SENSOR_PROXIMITY = "android.hardware.sensor.proximity";
    public static final String FEATURE_SENSOR_RELATIVE_HUMIDITY = "android.hardware.sensor.relative_humidity";
    public static final String FEATURE_SENSOR_STEP_COUNTER = "android.hardware.sensor.stepcounter";
    public static final String FEATURE_SENSOR_STEP_DETECTOR = "android.hardware.sensor.stepdetector";
    public static final String FEATURE_SIP = "android.software.sip";
    public static final String FEATURE_SIP_VOIP = "android.software.sip.voip";
    public static final String FEATURE_TELEPHONY = "android.hardware.telephony";
    public static final String FEATURE_TELEPHONY_CARRIERLOCK = "android.hardware.telephony.carrierlock";
    public static final String FEATURE_TELEPHONY_CDMA = "android.hardware.telephony.cdma";
    public static final String FEATURE_TELEPHONY_GSM = "android.hardware.telephony.gsm";
    @Deprecated
    public static final String FEATURE_TELEVISION = "android.hardware.type.television";
    public static final String FEATURE_TOUCHSCREEN = "android.hardware.touchscreen";
    public static final String FEATURE_TOUCHSCREEN_MULTITOUCH = "android.hardware.touchscreen.multitouch";
    public static final String FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT = "android.hardware.touchscreen.multitouch.distinct";
    public static final String FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND = "android.hardware.touchscreen.multitouch.jazzhand";
    public static final String FEATURE_USB_ACCESSORY = "android.hardware.usb.accessory";
    public static final String FEATURE_USB_HOST = "android.hardware.usb.host";
    public static final String FEATURE_VERIFIED_BOOT = "android.software.verified_boot";
    public static final String FEATURE_VOICE_RECOGNIZERS = "android.software.voice_recognizers";
    public static final String FEATURE_VR_HEADTRACKING = "android.hardware.vr.headtracking";
    public static final String FEATURE_VR_MODE = "android.software.vr.mode";
    public static final String FEATURE_VR_MODE_HIGH_PERFORMANCE = "android.hardware.vr.high_performance";
    public static final String FEATURE_VULKAN_HARDWARE_COMPUTE = "android.hardware.vulkan.compute";
    public static final String FEATURE_VULKAN_HARDWARE_LEVEL = "android.hardware.vulkan.level";
    public static final String FEATURE_VULKAN_HARDWARE_VERSION = "android.hardware.vulkan.version";
    public static final String FEATURE_WATCH = "android.hardware.type.watch";
    public static final String FEATURE_WEBVIEW = "android.software.webview";
    public static final String FEATURE_WIFI = "android.hardware.wifi";
    public static final String FEATURE_WIFI_AWARE = "android.hardware.wifi.aware";
    public static final String FEATURE_WIFI_DIRECT = "android.hardware.wifi.direct";
    public static final int FLAG_PERMISSION_GRANTED_BY_DEFAULT = 32;
    public static final int FLAG_PERMISSION_POLICY_FIXED = 4;
    public static final int FLAG_PERMISSION_REVIEW_REQUIRED = 64;
    public static final int FLAG_PERMISSION_REVOKE_ON_UPGRADE = 8;
    public static final int FLAG_PERMISSION_SYSTEM_FIXED = 16;
    public static final int FLAG_PERMISSION_USER_FIXED = 2;
    public static final int FLAG_PERMISSION_USER_SET = 1;
    public static final int GET_ACTIVITIES = 1;
    public static final int GET_CONFIGURATIONS = 16384;
    @Deprecated
    public static final int GET_DISABLED_COMPONENTS = 512;
    @Deprecated
    public static final int GET_DISABLED_UNTIL_USED_COMPONENTS = 32768;
    public static final int GET_GIDS = 256;
    public static final int GET_INSTRUMENTATION = 16;
    public static final int GET_INTENT_FILTERS = 32;
    public static final int GET_META_DATA = 128;
    public static final int GET_PERMISSIONS = 4096;
    public static final int GET_PROVIDERS = 8;
    public static final int GET_RECEIVERS = 2;
    public static final int GET_RESOLVED_FILTER = 64;
    public static final int GET_SERVICES = 4;
    public static final int GET_SHARED_LIBRARY_FILES = 1024;
    public static final int GET_SIGNATURES = 64;
    @Deprecated
    public static final int GET_UNINSTALLED_PACKAGES = 8192;
    public static final int GET_URI_PERMISSION_PATTERNS = 2048;
    public static final int INSTALL_ALLOCATE_AGGRESSIVE = 32768;
    public static final int INSTALL_ALLOW_DOWNGRADE = 128;
    public static final int INSTALL_ALLOW_TEST = 4;
    public static final int INSTALL_ALL_USERS = 64;
    public static final int INSTALL_DONT_KILL_APP = 4096;
    @Deprecated
    public static final int INSTALL_EXTERNAL = 8;
    public static final int INSTALL_FAILED_ABORTED = -115;
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;
    public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;
    public static final int INSTALL_FAILED_DEXOPT = -11;
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;
    public static final int INSTALL_FAILED_DUPLICATE_PERMISSION = -112;
    public static final int INSTALL_FAILED_INSTANT_APP_INVALID = -116;
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;
    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
    public static final int INSTALL_FAILED_INVALID_APK = -2;
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;
    public static final int INSTALL_FAILED_INVALID_URI = -3;
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;
    public static final int INSTALL_FAILED_MISSING_FEATURE = -17;
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;
    public static final int INSTALL_FAILED_NEWER_SDK = -14;
    public static final int INSTALL_FAILED_NO_MATCHING_ABIS = -113;
    public static final int INSTALL_FAILED_NO_SHARED_USER = -6;
    public static final int INSTALL_FAILED_OLDER_SDK = -12;
    public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;
    public static final int INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE = -26;
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;
    public static final int INSTALL_FAILED_SANDBOX_VERSION_DOWNGRADE = -27;
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;
    public static final int INSTALL_FAILED_TEST_ONLY = -15;
    public static final int INSTALL_FAILED_UID_CHANGED = -24;
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;
    public static final int INSTALL_FAILED_USER_RESTRICTED = -111;
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;
    public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;
    public static final int INSTALL_FORCE_PERMISSION_PROMPT = 1024;
    public static final int INSTALL_FORCE_SDK = 8192;
    public static final int INSTALL_FORCE_VOLUME_UUID = 512;
    @Deprecated
    public static final int INSTALL_FORWARD_LOCK = 1;
    public static final int INSTALL_FROM_ADB = 32;
    public static final int INSTALL_FULL_APP = 16384;
    public static final int INSTALL_GRANT_RUNTIME_PERMISSIONS = 256;
    public static final int INSTALL_INSTANT_APP = 2048;
    public static final int INSTALL_INTERNAL = 16;
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;
    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;
    public static final int INSTALL_REASON_DEVICE_RESTORE = 2;
    public static final int INSTALL_REASON_DEVICE_SETUP = 3;
    public static final int INSTALL_REASON_POLICY = 1;
    public static final int INSTALL_REASON_UNKNOWN = 0;
    public static final int INSTALL_REASON_USER = 4;
    public static final int INSTALL_REPLACE_EXISTING = 2;
    public static final int INSTALL_SUCCEEDED = 1;
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS = 2;
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS_ASK = 4;
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ASK = 1;
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_NEVER = 3;
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_UNDEFINED = 0;
    public static final int INTENT_FILTER_VERIFICATION_FAILURE = -1;
    public static final int INTENT_FILTER_VERIFICATION_SUCCESS = 1;
    public static final int MASK_PERMISSION_FLAGS = 255;
    public static final int MATCH_ALL = 131072;
    public static final int MATCH_ANY_USER = 4194304;
    public static final int MATCH_DEBUG_TRIAGED_MISSING = 268435456;
    public static final int MATCH_DEFAULT_ONLY = 65536;
    public static final int MATCH_DIRECT_BOOT_AWARE = 524288;
    public static final int MATCH_DIRECT_BOOT_UNAWARE = 262144;
    public static final int MATCH_DISABLED_COMPONENTS = 512;
    public static final int MATCH_DISABLED_UNTIL_USED_COMPONENTS = 32768;
    public static final int MATCH_EXPLICITLY_VISIBLE_ONLY = 33554432;
    public static final int MATCH_FACTORY_ONLY = 2097152;
    public static final int MATCH_INSTANT = 8388608;
    public static final int MATCH_KNOWN_PACKAGES = 4202496;
    public static final int MATCH_STATIC_SHARED_LIBRARIES = 67108864;
    public static final int MATCH_SYSTEM_ONLY = 1048576;
    public static final int MATCH_UNINSTALLED_PACKAGES = 8192;
    public static final int MATCH_VISIBLE_TO_INSTANT_APP_ONLY = 16777216;
    public static final long MAXIMUM_VERIFICATION_TIMEOUT = 3600000;
    @Deprecated
    public static final int MOVE_EXTERNAL_MEDIA = 2;
    public static final int MOVE_FAILED_3RD_PARTY_NOT_ALLOWED_ON_INTERNAL = -9;
    public static final int MOVE_FAILED_DEVICE_ADMIN = -8;
    public static final int MOVE_FAILED_DOESNT_EXIST = -2;
    public static final int MOVE_FAILED_FORWARD_LOCKED = -4;
    public static final int MOVE_FAILED_INSUFFICIENT_STORAGE = -1;
    public static final int MOVE_FAILED_INTERNAL_ERROR = -6;
    public static final int MOVE_FAILED_INVALID_LOCATION = -5;
    public static final int MOVE_FAILED_OPERATION_PENDING = -7;
    public static final int MOVE_FAILED_SYSTEM_PACKAGE = -3;
    @Deprecated
    public static final int MOVE_INTERNAL = 1;
    public static final int MOVE_SUCCEEDED = -100;
    public static final int NOTIFY_PACKAGE_USE_ACTIVITY = 0;
    public static final int NOTIFY_PACKAGE_USE_BACKUP = 5;
    public static final int NOTIFY_PACKAGE_USE_BROADCAST_RECEIVER = 3;
    public static final int NOTIFY_PACKAGE_USE_CONTENT_PROVIDER = 4;
    public static final int NOTIFY_PACKAGE_USE_CROSS_PACKAGE = 6;
    public static final int NOTIFY_PACKAGE_USE_FOREGROUND_SERVICE = 2;
    public static final int NOTIFY_PACKAGE_USE_INSTRUMENTATION = 7;
    public static final int NOTIFY_PACKAGE_USE_REASONS_COUNT = 8;
    public static final int NOTIFY_PACKAGE_USE_SERVICE = 1;
    public static final int NO_NATIVE_LIBRARIES = -114;
    public static final int ONLY_IF_NO_MATCH_FOUND = 4;
    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_GRANTED = 0;
    public static final int SIGNATURE_FIRST_NOT_SIGNED = -1;
    public static final int SIGNATURE_MATCH = 0;
    public static final int SIGNATURE_NEITHER_SIGNED = 1;
    public static final int SIGNATURE_NO_MATCH = -3;
    public static final int SIGNATURE_SECOND_NOT_SIGNED = -2;
    public static final int SIGNATURE_UNKNOWN_PACKAGE = -4;
    public static final int SKIP_CURRENT_PROFILE = 2;
    public static final String SYSTEM_SHARED_LIBRARY_SERVICES = "android.ext.services";
    public static final String SYSTEM_SHARED_LIBRARY_SHARED = "android.ext.shared";
    private static final String TAG = "PackageManager";
    public static final int VERIFICATION_ALLOW = 1;
    public static final int VERIFICATION_ALLOW_WITHOUT_SUFFICIENT = 2;
    public static final int VERIFICATION_REJECT = -1;
    public static final int VERSION_CODE_HIGHEST = -1;

    public static class LegacyPackageDeleteObserver extends PackageDeleteObserver {
        private final IPackageDeleteObserver mLegacy;

        public LegacyPackageDeleteObserver(IPackageDeleteObserver legacy) {
            this.mLegacy = legacy;
        }

        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            if (this.mLegacy != null) {
                try {
                    this.mLegacy.packageDeleted(basePackageName, returnCode);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public static class LegacyPackageInstallObserver extends PackageInstallObserver {
        private final IPackageInstallObserver mLegacy;

        public LegacyPackageInstallObserver(IPackageInstallObserver legacy) {
            this.mLegacy = legacy;
        }

        public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
            if (this.mLegacy != null) {
                try {
                    this.mLegacy.packageInstalled(basePackageName, returnCode);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public static abstract class MoveCallback {
        public abstract void onStatusChanged(int i, int i2, long j);

        public void onCreated(int moveId, Bundle extras) {
        }
    }

    public static class NameNotFoundException extends AndroidException {
        public NameNotFoundException(String name) {
            super(name);
        }
    }

    public interface OnPermissionsChangedListener {
        void onPermissionsChanged(int i);
    }

    public abstract void addCrossProfileIntentFilter(IntentFilter intentFilter, int i, int i2, int i3);

    public abstract void addOnPermissionsChangeListener(OnPermissionsChangedListener onPermissionsChangedListener);

    @Deprecated
    public abstract void addPackageToPreferred(String str);

    public abstract boolean addPermission(PermissionInfo permissionInfo);

    public abstract boolean addPermissionAsync(PermissionInfo permissionInfo);

    @Deprecated
    public abstract void addPreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName);

    public abstract boolean canRequestPackageInstalls();

    public abstract String[] canonicalToCurrentPackageNames(String[] strArr);

    public abstract int checkPermission(String str, String str2);

    public abstract int checkSignatures(int i, int i2);

    public abstract int checkSignatures(String str, String str2);

    public abstract void clearApplicationUserData(String str, IPackageDataObserver iPackageDataObserver);

    public abstract void clearCrossProfileIntentFilters(int i);

    public abstract void clearInstantAppCookie();

    public abstract void clearPackagePreferredActivities(String str);

    public abstract String[] currentToCanonicalPackageNames(String[] strArr);

    public abstract void deleteApplicationCacheFiles(String str, IPackageDataObserver iPackageDataObserver);

    public abstract void deleteApplicationCacheFilesAsUser(String str, int i, IPackageDataObserver iPackageDataObserver);

    public abstract void deletePackage(String str, IPackageDeleteObserver iPackageDeleteObserver, int i);

    public abstract void deletePackageAsUser(String str, IPackageDeleteObserver iPackageDeleteObserver, int i, int i2);

    public abstract void extendVerificationTimeout(int i, int i2, long j);

    public abstract void flushPackageRestrictionsAsUser(int i);

    public abstract void freeStorage(String str, long j, IntentSender intentSender);

    public abstract void freeStorageAndNotify(String str, long j, IPackageDataObserver iPackageDataObserver);

    public abstract Drawable getActivityBanner(ComponentName componentName) throws NameNotFoundException;

    public abstract Drawable getActivityBanner(Intent intent) throws NameNotFoundException;

    public abstract Drawable getActivityIcon(ComponentName componentName) throws NameNotFoundException;

    public abstract Drawable getActivityIcon(Intent intent) throws NameNotFoundException;

    public abstract ActivityInfo getActivityInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract Drawable getActivityLogo(ComponentName componentName) throws NameNotFoundException;

    public abstract Drawable getActivityLogo(Intent intent) throws NameNotFoundException;

    public abstract List<IntentFilter> getAllIntentFilters(String str);

    public abstract List<PermissionGroupInfo> getAllPermissionGroups(int i);

    public abstract Drawable getApplicationBanner(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationBanner(String str) throws NameNotFoundException;

    public abstract int getApplicationEnabledSetting(String str);

    public abstract boolean getApplicationHiddenSettingAsUser(String str, UserHandle userHandle);

    public abstract Drawable getApplicationIcon(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationIcon(String str) throws NameNotFoundException;

    public abstract ApplicationInfo getApplicationInfo(String str, int i) throws NameNotFoundException;

    public abstract ApplicationInfo getApplicationInfoAsUser(String str, int i, int i2) throws NameNotFoundException;

    public abstract CharSequence getApplicationLabel(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationLogo(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationLogo(String str) throws NameNotFoundException;

    public abstract ChangedPackages getChangedPackages(int i);

    public abstract int getComponentEnabledSetting(ComponentName componentName);

    public abstract Drawable getDefaultActivityIcon();

    public abstract String getDefaultBrowserPackageNameAsUser(int i);

    public abstract Drawable getDrawable(String str, int i, ApplicationInfo applicationInfo);

    public abstract ComponentName getHomeActivities(List<ResolveInfo> list);

    public abstract int getInstallReason(String str, UserHandle userHandle);

    public abstract List<ApplicationInfo> getInstalledApplications(int i);

    public abstract List<ApplicationInfo> getInstalledApplicationsAsUser(int i, int i2);

    public abstract List<PackageInfo> getInstalledPackages(int i);

    public abstract List<PackageInfo> getInstalledPackagesAsUser(int i, int i2);

    public abstract String getInstallerPackageName(String str);

    public abstract String getInstantAppAndroidId(String str, UserHandle userHandle);

    public abstract byte[] getInstantAppCookie();

    public abstract int getInstantAppCookieMaxBytes();

    public abstract int getInstantAppCookieMaxSize();

    public abstract Drawable getInstantAppIcon(String str);

    public abstract ComponentName getInstantAppInstallerComponent();

    public abstract ComponentName getInstantAppResolverSettingsComponent();

    public abstract List<InstantAppInfo> getInstantApps();

    public abstract InstrumentationInfo getInstrumentationInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract List<IntentFilterVerificationInfo> getIntentFilterVerifications(String str);

    public abstract int getIntentVerificationStatusAsUser(String str, int i);

    public abstract KeySet getKeySetByAlias(String str, String str2);

    public abstract Intent getLaunchIntentForPackage(String str);

    public abstract Intent getLeanbackLaunchIntentForPackage(String str);

    public abstract int getMoveStatus(int i);

    public abstract String getNameForUid(int i);

    public abstract List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo applicationInfo);

    public abstract VolumeInfo getPackageCurrentVolume(ApplicationInfo applicationInfo);

    public abstract int[] getPackageGids(String str) throws NameNotFoundException;

    public abstract int[] getPackageGids(String str, int i) throws NameNotFoundException;

    public abstract PackageInfo getPackageInfo(VersionedPackage versionedPackage, int i) throws NameNotFoundException;

    public abstract PackageInfo getPackageInfo(String str, int i) throws NameNotFoundException;

    public abstract PackageInfo getPackageInfoAsUser(String str, int i, int i2) throws NameNotFoundException;

    public abstract PackageInstaller getPackageInstaller();

    @Deprecated
    public abstract void getPackageSizeInfoAsUser(String str, int i, IPackageStatsObserver iPackageStatsObserver);

    public abstract int getPackageUid(String str, int i) throws NameNotFoundException;

    public abstract int getPackageUidAsUser(String str, int i) throws NameNotFoundException;

    public abstract int getPackageUidAsUser(String str, int i, int i2) throws NameNotFoundException;

    public abstract String[] getPackagesForUid(int i);

    public abstract List<PackageInfo> getPackagesHoldingPermissions(String[] strArr, int i);

    public abstract String getPermissionControllerPackageName();

    public abstract int getPermissionFlags(String str, String str2, UserHandle userHandle);

    public abstract PermissionGroupInfo getPermissionGroupInfo(String str, int i) throws NameNotFoundException;

    public abstract PermissionInfo getPermissionInfo(String str, int i) throws NameNotFoundException;

    public abstract int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String str);

    public abstract List<PackageInfo> getPreferredPackages(int i);

    public abstract List<VolumeInfo> getPrimaryStorageCandidateVolumes();

    public abstract VolumeInfo getPrimaryStorageCurrentVolume();

    public abstract ProviderInfo getProviderInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract ActivityInfo getReceiverInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract Resources getResourcesForActivity(ComponentName componentName) throws NameNotFoundException;

    public abstract Resources getResourcesForApplication(ApplicationInfo applicationInfo) throws NameNotFoundException;

    public abstract Resources getResourcesForApplication(String str) throws NameNotFoundException;

    public abstract Resources getResourcesForApplicationAsUser(String str, int i) throws NameNotFoundException;

    public abstract ServiceInfo getServiceInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract String getServicesSystemSharedLibraryPackageName();

    public abstract List<SharedLibraryInfo> getSharedLibraries(int i);

    public abstract List<SharedLibraryInfo> getSharedLibrariesAsUser(int i, int i2);

    public abstract String getSharedSystemSharedLibraryPackageName();

    public abstract KeySet getSigningKeySet(String str);

    public abstract FeatureInfo[] getSystemAvailableFeatures();

    public abstract String[] getSystemSharedLibraryNames();

    public abstract CharSequence getText(String str, int i, ApplicationInfo applicationInfo);

    public abstract int getUidForSharedUser(String str) throws NameNotFoundException;

    public abstract Drawable getUserBadgeForDensity(UserHandle userHandle, int i);

    public abstract Drawable getUserBadgeForDensityNoBackground(UserHandle userHandle, int i);

    public abstract Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle userHandle, Rect rect, int i);

    public abstract Drawable getUserBadgedIcon(Drawable drawable, UserHandle userHandle);

    public abstract CharSequence getUserBadgedLabel(CharSequence charSequence, UserHandle userHandle);

    public abstract VerifierDeviceIdentity getVerifierDeviceIdentity();

    public abstract XmlResourceParser getXml(String str, int i, ApplicationInfo applicationInfo);

    public abstract void grantRuntimePermission(String str, String str2, UserHandle userHandle);

    public abstract boolean hasSystemFeature(String str);

    public abstract boolean hasSystemFeature(String str, int i);

    public abstract int installExistingPackage(String str) throws NameNotFoundException;

    public abstract int installExistingPackage(String str, int i) throws NameNotFoundException;

    public abstract int installExistingPackageAsUser(String str, int i) throws NameNotFoundException;

    @Deprecated
    public abstract void installPackage(Uri uri, PackageInstallObserver packageInstallObserver, int i, String str);

    @Deprecated
    public abstract void installPackage(Uri uri, IPackageInstallObserver iPackageInstallObserver, int i, String str);

    public abstract boolean isInstantApp();

    public abstract boolean isInstantApp(String str);

    public abstract boolean isPackageAvailable(String str);

    public abstract boolean isPackageSuspendedForUser(String str, int i);

    public abstract boolean isPermissionReviewModeEnabled();

    public abstract boolean isPermissionRevokedByPolicy(String str, String str2);

    public abstract boolean isSafeMode();

    public abstract boolean isSignedBy(String str, KeySet keySet);

    public abstract boolean isSignedByExactly(String str, KeySet keySet);

    public abstract boolean isUpgrade();

    public abstract Drawable loadItemIcon(PackageItemInfo packageItemInfo, ApplicationInfo applicationInfo);

    public abstract Drawable loadUnbadgedItemIcon(PackageItemInfo packageItemInfo, ApplicationInfo applicationInfo);

    public abstract int movePackage(String str, VolumeInfo volumeInfo);

    public abstract int movePrimaryStorage(VolumeInfo volumeInfo);

    public abstract List<ResolveInfo> queryBroadcastReceivers(Intent intent, int i);

    public abstract List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int i, int i2);

    public abstract List<ProviderInfo> queryContentProviders(String str, int i, int i2);

    public abstract List<InstrumentationInfo> queryInstrumentation(String str, int i);

    public abstract List<ResolveInfo> queryIntentActivities(Intent intent, int i);

    public abstract List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int i, int i2);

    public abstract List<ResolveInfo> queryIntentActivityOptions(ComponentName componentName, Intent[] intentArr, Intent intent, int i);

    public abstract List<ResolveInfo> queryIntentContentProviders(Intent intent, int i);

    public abstract List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int i, int i2);

    public abstract List<ResolveInfo> queryIntentServices(Intent intent, int i);

    public abstract List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int i, int i2);

    public abstract List<PermissionInfo> queryPermissionsByGroup(String str, int i) throws NameNotFoundException;

    public abstract void registerMoveCallback(MoveCallback moveCallback, Handler handler);

    public abstract void removeOnPermissionsChangeListener(OnPermissionsChangedListener onPermissionsChangedListener);

    @Deprecated
    public abstract void removePackageFromPreferred(String str);

    public abstract void removePermission(String str);

    @Deprecated
    public abstract void replacePreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName);

    public abstract ResolveInfo resolveActivity(Intent intent, int i);

    public abstract ResolveInfo resolveActivityAsUser(Intent intent, int i, int i2);

    public abstract ProviderInfo resolveContentProvider(String str, int i);

    public abstract ProviderInfo resolveContentProviderAsUser(String str, int i, int i2);

    public abstract ResolveInfo resolveService(Intent intent, int i);

    public abstract void revokeRuntimePermission(String str, String str2, UserHandle userHandle);

    public abstract void setApplicationCategoryHint(String str, int i);

    public abstract void setApplicationEnabledSetting(String str, int i, int i2);

    public abstract boolean setApplicationHiddenSettingAsUser(String str, boolean z, UserHandle userHandle);

    public abstract void setComponentEnabledSetting(ComponentName componentName, int i, int i2);

    public abstract boolean setDefaultBrowserPackageNameAsUser(String str, int i);

    public abstract void setInstallerPackageName(String str, String str2);

    public abstract boolean setInstantAppCookie(byte[] bArr);

    public abstract String[] setPackagesSuspendedAsUser(String[] strArr, boolean z, int i);

    public abstract void setUpdateAvailable(String str, boolean z);

    public abstract boolean shouldShowRequestPermissionRationale(String str);

    public abstract void unregisterMoveCallback(MoveCallback moveCallback);

    public abstract void updateInstantAppCookie(byte[] bArr);

    public abstract boolean updateIntentVerificationStatusAsUser(String str, int i, int i2);

    public abstract void updatePermissionFlags(String str, String str2, int i, int i2, UserHandle userHandle);

    public abstract void verifyIntentFilter(int i, int i2, List<String> list);

    public abstract void verifyPendingInstall(int i, int i2);

    public Intent buildRequestPermissionsIntent(String[] permissions) {
        if (ArrayUtils.isEmpty(permissions)) {
            throw new IllegalArgumentException("permission cannot be null or empty");
        }
        Intent intent = new Intent(ACTION_REQUEST_PERMISSIONS);
        intent.putExtra(EXTRA_REQUEST_PERMISSIONS_NAMES, permissions);
        intent.setPackage(getPermissionControllerPackageName());
        return intent;
    }

    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, UserHandle userHandle) {
        return queryBroadcastReceiversAsUser(intent, flags, userHandle.getIdentifier());
    }

    @Deprecated
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, int userId) {
        String msg = "Shame on you for calling the hidden API queryBroadcastReceivers(). Shame!";
        if (VMRuntime.getRuntime().getTargetSdkVersion() >= 26) {
            throw new UnsupportedOperationException("Shame on you for calling the hidden API queryBroadcastReceivers(). Shame!");
        }
        Log.d(TAG, "Shame on you for calling the hidden API queryBroadcastReceivers(). Shame!");
        return queryBroadcastReceiversAsUser(intent, flags, userId);
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, String metaDataKey) {
        return queryContentProviders(processName, uid, flags);
    }

    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        PackageParser parser = new PackageParser();
        parser.setCallback(new CallbackImpl(this));
        File apkFile = new File(archiveFilePath);
        if ((flags & 786432) == 0) {
            flags |= 786432;
        }
        try {
            Package pkg = parser.parseMonolithicPackage(apkFile, 0);
            if ((flags & 64) != 0) {
                PackageParser.collectCertificates(pkg, 0);
            }
            return PackageParser.generatePackageInfo(pkg, null, flags, 0, 0, null, new PackageUserState());
        } catch (PackageParserException e) {
            return null;
        }
    }

    public void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {
        freeStorageAndNotify(null, freeStorageSize, observer);
    }

    public void freeStorage(long freeStorageSize, IntentSender pi) {
        freeStorage(null, freeStorageSize, pi);
    }

    @Deprecated
    public void getPackageSizeInfo(String packageName, IPackageStatsObserver observer) {
        getPackageSizeInfoAsUser(packageName, UserHandle.myUserId(), observer);
    }

    public void addPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    @Deprecated
    public void replacePreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public static boolean isMoveStatusFinished(int status) {
        return status < 0 || status > 100;
    }

    public static String installStatusToString(int status, String msg) {
        String str = installStatusToString(status);
        if (msg != null) {
            return str + ": " + msg;
        }
        return str;
    }

    public static String installStatusToString(int status) {
        switch (status) {
            case INSTALL_FAILED_ABORTED /*-115*/:
                return "INSTALL_FAILED_ABORTED";
            case INSTALL_FAILED_NO_MATCHING_ABIS /*-113*/:
                return "INSTALL_FAILED_NO_MATCHING_ABIS";
            case INSTALL_FAILED_DUPLICATE_PERMISSION /*-112*/:
                return "INSTALL_FAILED_DUPLICATE_PERMISSION";
            case INSTALL_FAILED_USER_RESTRICTED /*-111*/:
                return "INSTALL_FAILED_USER_RESTRICTED";
            case -110:
                return "INSTALL_FAILED_INTERNAL_ERROR";
            case INSTALL_PARSE_FAILED_MANIFEST_EMPTY /*-109*/:
                return "INSTALL_PARSE_FAILED_MANIFEST_EMPTY";
            case INSTALL_PARSE_FAILED_MANIFEST_MALFORMED /*-108*/:
                return "INSTALL_PARSE_FAILED_MANIFEST_MALFORMED";
            case INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID /*-107*/:
                return "INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID";
            case INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME /*-106*/:
                return "INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME";
            case INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING /*-105*/:
                return "INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING";
            case INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES /*-104*/:
                return "INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES";
            case INSTALL_PARSE_FAILED_NO_CERTIFICATES /*-103*/:
                return "INSTALL_PARSE_FAILED_NO_CERTIFICATES";
            case INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION /*-102*/:
                return "INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION";
            case INSTALL_PARSE_FAILED_BAD_MANIFEST /*-101*/:
                return "INSTALL_PARSE_FAILED_BAD_MANIFEST";
            case -100:
                return "INSTALL_PARSE_FAILED_NOT_APK";
            case INSTALL_FAILED_VERSION_DOWNGRADE /*-25*/:
                return "INSTALL_FAILED_VERSION_DOWNGRADE";
            case -24:
                return "INSTALL_FAILED_UID_CHANGED";
            case -23:
                return "INSTALL_FAILED_PACKAGE_CHANGED";
            case -22:
                return "INSTALL_FAILED_VERIFICATION_FAILURE";
            case -21:
                return "INSTALL_FAILED_VERIFICATION_TIMEOUT";
            case -20:
                return "INSTALL_FAILED_MEDIA_UNAVAILABLE";
            case -19:
                return "INSTALL_FAILED_INVALID_INSTALL_LOCATION";
            case -18:
                return "INSTALL_FAILED_CONTAINER_ERROR";
            case -17:
                return "INSTALL_FAILED_MISSING_FEATURE";
            case -16:
                return "INSTALL_FAILED_CPU_ABI_INCOMPATIBLE";
            case -15:
                return "INSTALL_FAILED_TEST_ONLY";
            case -14:
                return "INSTALL_FAILED_NEWER_SDK";
            case -13:
                return "INSTALL_FAILED_CONFLICTING_PROVIDER";
            case -12:
                return "INSTALL_FAILED_OLDER_SDK";
            case -11:
                return "INSTALL_FAILED_DEXOPT";
            case -10:
                return "INSTALL_FAILED_REPLACE_COULDNT_DELETE";
            case -9:
                return "INSTALL_FAILED_MISSING_SHARED_LIBRARY";
            case -8:
                return "INSTALL_FAILED_SHARED_USER_INCOMPATIBLE";
            case -7:
                return "INSTALL_FAILED_UPDATE_INCOMPATIBLE";
            case -6:
                return "INSTALL_FAILED_NO_SHARED_USER";
            case -5:
                return "INSTALL_FAILED_DUPLICATE_PACKAGE";
            case -4:
                return "INSTALL_FAILED_INSUFFICIENT_STORAGE";
            case -3:
                return "INSTALL_FAILED_INVALID_URI";
            case -2:
                return "INSTALL_FAILED_INVALID_APK";
            case -1:
                return "INSTALL_FAILED_ALREADY_EXISTS";
            case 1:
                return "INSTALL_SUCCEEDED";
            default:
                return Integer.toString(status);
        }
    }

    public static int installStatusToPublicStatus(int status) {
        switch (status) {
            case INSTALL_FAILED_ABORTED /*-115*/:
                return 3;
            case INSTALL_FAILED_NO_MATCHING_ABIS /*-113*/:
                return 7;
            case INSTALL_FAILED_DUPLICATE_PERMISSION /*-112*/:
                return 5;
            case INSTALL_FAILED_USER_RESTRICTED /*-111*/:
                return 7;
            case -110:
                return 1;
            case INSTALL_PARSE_FAILED_MANIFEST_EMPTY /*-109*/:
                return 4;
            case INSTALL_PARSE_FAILED_MANIFEST_MALFORMED /*-108*/:
                return 4;
            case INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID /*-107*/:
                return 4;
            case INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME /*-106*/:
                return 4;
            case INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING /*-105*/:
                return 4;
            case INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES /*-104*/:
                return 4;
            case INSTALL_PARSE_FAILED_NO_CERTIFICATES /*-103*/:
                return 4;
            case INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION /*-102*/:
                return 4;
            case INSTALL_PARSE_FAILED_BAD_MANIFEST /*-101*/:
                return 4;
            case -100:
                return 4;
            case INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE /*-26*/:
                return 4;
            case INSTALL_FAILED_VERSION_DOWNGRADE /*-25*/:
                return 4;
            case -24:
                return 4;
            case -23:
                return 4;
            case -22:
                return 3;
            case -21:
                return 3;
            case -20:
                return 6;
            case -19:
                return 6;
            case -18:
                return 6;
            case -17:
                return 7;
            case -16:
                return 7;
            case -15:
                return 4;
            case -14:
                return 7;
            case -13:
                return 5;
            case -12:
                return 7;
            case -11:
                return 4;
            case -10:
                return 5;
            case -9:
                return 7;
            case -8:
                return 5;
            case -7:
                return 5;
            case -6:
                return 5;
            case -5:
                return 5;
            case -4:
                return 6;
            case -3:
                return 4;
            case -2:
                return 4;
            case -1:
                return 5;
            case 1:
                return 0;
            default:
                return 1;
        }
    }

    public static String deleteStatusToString(int status, String msg) {
        String str = deleteStatusToString(status);
        if (msg != null) {
            return str + ": " + msg;
        }
        return str;
    }

    public static String deleteStatusToString(int status) {
        switch (status) {
            case -6:
                return "DELETE_FAILED_USED_SHARED_LIBRARY";
            case -5:
                return "DELETE_FAILED_ABORTED";
            case -4:
                return "DELETE_FAILED_OWNER_BLOCKED";
            case -3:
                return "DELETE_FAILED_USER_RESTRICTED";
            case -2:
                return "DELETE_FAILED_DEVICE_POLICY_MANAGER";
            case -1:
                return "DELETE_FAILED_INTERNAL_ERROR";
            case 1:
                return "DELETE_SUCCEEDED";
            default:
                return Integer.toString(status);
        }
    }

    public static int deleteStatusToPublicStatus(int status) {
        switch (status) {
            case -6:
                return 5;
            case -5:
                return 3;
            case -4:
                return 2;
            case -3:
                return 2;
            case -2:
                return 2;
            case -1:
                return 1;
            case 1:
                return 0;
            default:
                return 1;
        }
    }

    public static String permissionFlagToString(int flag) {
        switch (flag) {
            case 1:
                return "USER_SET";
            case 2:
                return "USER_FIXED";
            case 4:
                return "POLICY_FIXED";
            case 8:
                return "REVOKE_ON_UPGRADE";
            case 16:
                return "SYSTEM_FIXED";
            case 32:
                return "GRANTED_BY_DEFAULT";
            case 64:
                return "REVIEW_REQUIRED";
            default:
                return Integer.toString(flag);
        }
    }
}
