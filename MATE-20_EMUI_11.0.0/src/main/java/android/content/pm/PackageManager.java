package android.content.pm;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.AppDetailsActivity;
import android.app.PackageDeleteObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageParser;
import android.content.pm.dex.ArtManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import android.util.AndroidException;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import dalvik.system.VMRuntime;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class PackageManager {
    @SystemApi
    public static final String ACTION_REQUEST_PERMISSIONS = "android.content.pm.action.REQUEST_PERMISSIONS";
    public static final boolean APPLY_DEFAULT_TO_DEVICE_PROTECTED_STORAGE = true;
    public static final String APP_DETAILS_ACTIVITY_CLASS_NAME = AppDetailsActivity.class.getName();
    public static final int CERT_INPUT_RAW_X509 = 0;
    public static final int CERT_INPUT_SHA256 = 1;
    public static final int COMPONENT_ENABLED_STATE_DEFAULT = 0;
    public static final int COMPONENT_ENABLED_STATE_DISABLED = 2;
    public static final int COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED = 4;
    public static final int COMPONENT_ENABLED_STATE_DISABLED_USER = 3;
    public static final int COMPONENT_ENABLED_STATE_ENABLED = 1;
    public static final int DELETE_ALL_USERS = 2;
    public static final int DELETE_CHATTY = Integer.MIN_VALUE;
    public static final int DELETE_CONTRIBUTED_MEDIA = 16;
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
    @SystemApi
    public static final String EXTRA_REQUEST_PERMISSIONS_NAMES = "android.content.pm.extra.REQUEST_PERMISSIONS_NAMES";
    @SystemApi
    public static final String EXTRA_REQUEST_PERMISSIONS_RESULTS = "android.content.pm.extra.REQUEST_PERMISSIONS_RESULTS";
    public static final String EXTRA_VERIFICATION_ID = "android.content.pm.extra.VERIFICATION_ID";
    public static final String EXTRA_VERIFICATION_INSTALLER_PACKAGE = "android.content.pm.extra.VERIFICATION_INSTALLER_PACKAGE";
    public static final String EXTRA_VERIFICATION_INSTALLER_UID = "android.content.pm.extra.VERIFICATION_INSTALLER_UID";
    public static final String EXTRA_VERIFICATION_INSTALL_FLAGS = "android.content.pm.extra.VERIFICATION_INSTALL_FLAGS";
    public static final String EXTRA_VERIFICATION_LONG_VERSION_CODE = "android.content.pm.extra.VERIFICATION_LONG_VERSION_CODE";
    public static final String EXTRA_VERIFICATION_PACKAGE_NAME = "android.content.pm.extra.VERIFICATION_PACKAGE_NAME";
    public static final String EXTRA_VERIFICATION_RESULT = "android.content.pm.extra.VERIFICATION_RESULT";
    public static final String EXTRA_VERIFICATION_URI = "android.content.pm.extra.VERIFICATION_URI";
    @Deprecated
    public static final String EXTRA_VERIFICATION_VERSION_CODE = "android.content.pm.extra.VERIFICATION_VERSION_CODE";
    public static final String FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS = "android.software.activities_on_secondary_displays";
    public static final String FEATURE_ADOPTABLE_STORAGE = "android.software.adoptable_storage";
    public static final String FEATURE_APP_WIDGETS = "android.software.app_widgets";
    public static final String FEATURE_ASSIST_GESTURE = "android.hardware.sensor.assist";
    public static final String FEATURE_AUDIO_LOW_LATENCY = "android.hardware.audio.low_latency";
    public static final String FEATURE_AUDIO_OUTPUT = "android.hardware.audio.output";
    public static final String FEATURE_AUDIO_PRO = "android.hardware.audio.pro";
    public static final String FEATURE_AUTOFILL = "android.software.autofill";
    public static final String FEATURE_AUTOMOTIVE = "android.hardware.type.automotive";
    public static final String FEATURE_BACKUP = "android.software.backup";
    public static final String FEATURE_BLUETOOTH = "android.hardware.bluetooth";
    public static final String FEATURE_BLUETOOTH_LE = "android.hardware.bluetooth_le";
    @SystemApi
    public static final String FEATURE_BROADCAST_RADIO = "android.hardware.broadcastradio";
    public static final String FEATURE_CAMERA = "android.hardware.camera";
    public static final String FEATURE_CAMERA_ANY = "android.hardware.camera.any";
    public static final String FEATURE_CAMERA_AR = "android.hardware.camera.ar";
    public static final String FEATURE_CAMERA_AUTOFOCUS = "android.hardware.camera.autofocus";
    public static final String FEATURE_CAMERA_CAPABILITY_MANUAL_POST_PROCESSING = "android.hardware.camera.capability.manual_post_processing";
    public static final String FEATURE_CAMERA_CAPABILITY_MANUAL_SENSOR = "android.hardware.camera.capability.manual_sensor";
    public static final String FEATURE_CAMERA_CAPABILITY_RAW = "android.hardware.camera.capability.raw";
    public static final String FEATURE_CAMERA_EXTERNAL = "android.hardware.camera.external";
    public static final String FEATURE_CAMERA_FLASH = "android.hardware.camera.flash";
    public static final String FEATURE_CAMERA_FRONT = "android.hardware.camera.front";
    public static final String FEATURE_CAMERA_LEVEL_FULL = "android.hardware.camera.level.full";
    public static final String FEATURE_CANT_SAVE_STATE = "android.software.cant_save_state";
    public static final String FEATURE_COMPANION_DEVICE_SETUP = "android.software.companion_device_setup";
    public static final String FEATURE_CONNECTION_SERVICE = "android.software.connectionservice";
    public static final String FEATURE_CONSUMER_IR = "android.hardware.consumerir";
    public static final String FEATURE_CTS = "android.software.cts";
    public static final String FEATURE_DEVICE_ADMIN = "android.software.device_admin";
    public static final String FEATURE_DEVICE_ID_ATTESTATION = "android.software.device_id_attestation";
    public static final String FEATURE_EMBEDDED = "android.hardware.type.embedded";
    public static final String FEATURE_ETHERNET = "android.hardware.ethernet";
    public static final String FEATURE_FACE = "android.hardware.biometrics.face";
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
    public static final String FEATURE_IPSEC_TUNNELS = "android.software.ipsec_tunnels";
    public static final String FEATURE_IRIS = "android.hardware.biometrics.iris";
    public static final String FEATURE_LEANBACK = "android.software.leanback";
    public static final String FEATURE_LEANBACK_ONLY = "android.software.leanback_only";
    public static final String FEATURE_LIVE_TV = "android.software.live_tv";
    public static final String FEATURE_LIVE_WALLPAPER = "android.software.live_wallpaper";
    public static final String FEATURE_LOCATION = "android.hardware.location";
    public static final String FEATURE_LOCATION_GPS = "android.hardware.location.gps";
    public static final String FEATURE_LOCATION_NETWORK = "android.hardware.location.network";
    public static final String FEATURE_LOWPAN = "android.hardware.lowpan";
    public static final String FEATURE_MANAGED_PROFILES = "android.software.managed_users";
    public static final String FEATURE_MANAGED_USERS = "android.software.managed_users";
    public static final String FEATURE_MICROPHONE = "android.hardware.microphone";
    public static final String FEATURE_MIDI = "android.software.midi";
    public static final String FEATURE_NFC = "android.hardware.nfc";
    public static final String FEATURE_NFC_ANY = "android.hardware.nfc.any";
    public static final String FEATURE_NFC_BEAM = "android.sofware.nfc.beam";
    @Deprecated
    public static final String FEATURE_NFC_HCE = "android.hardware.nfc.hce";
    public static final String FEATURE_NFC_HOST_CARD_EMULATION = "android.hardware.nfc.hce";
    public static final String FEATURE_NFC_HOST_CARD_EMULATION_NFCF = "android.hardware.nfc.hcef";
    public static final String FEATURE_NFC_OFF_HOST_CARD_EMULATION_ESE = "android.hardware.nfc.ese";
    public static final String FEATURE_NFC_OFF_HOST_CARD_EMULATION_UICC = "android.hardware.nfc.uicc";
    public static final String FEATURE_OPENGLES_EXTENSION_PACK = "android.hardware.opengles.aep";
    public static final String FEATURE_PC = "android.hardware.type.pc";
    public static final String FEATURE_PICTURE_IN_PICTURE = "android.software.picture_in_picture";
    public static final String FEATURE_PRINTING = "android.software.print";
    public static final String FEATURE_RAM_LOW = "android.hardware.ram.low";
    public static final String FEATURE_RAM_NORMAL = "android.hardware.ram.normal";
    public static final String FEATURE_SCREEN_LANDSCAPE = "android.hardware.screen.landscape";
    public static final String FEATURE_SCREEN_PORTRAIT = "android.hardware.screen.portrait";
    public static final String FEATURE_SECURELY_REMOVES_USERS = "android.software.securely_removes_users";
    public static final String FEATURE_SECURE_LOCK_SCREEN = "android.software.secure_lock_screen";
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
    public static final String FEATURE_STRONGBOX_KEYSTORE = "android.hardware.strongbox_keystore";
    public static final String FEATURE_TELEPHONY = "android.hardware.telephony";
    @SystemApi
    public static final String FEATURE_TELEPHONY_CARRIERLOCK = "android.hardware.telephony.carrierlock";
    public static final String FEATURE_TELEPHONY_CDMA = "android.hardware.telephony.cdma";
    public static final String FEATURE_TELEPHONY_EUICC = "android.hardware.telephony.euicc";
    public static final String FEATURE_TELEPHONY_GSM = "android.hardware.telephony.gsm";
    public static final String FEATURE_TELEPHONY_IMS = "android.hardware.telephony.ims";
    public static final String FEATURE_TELEPHONY_MBMS = "android.hardware.telephony.mbms";
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
    @Deprecated
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
    public static final String FEATURE_WIFI_PASSPOINT = "android.hardware.wifi.passpoint";
    public static final String FEATURE_WIFI_RTT = "android.hardware.wifi.rtt";
    public static final int FLAGS_PERMISSION_RESTRICTION_ANY_EXEMPT = 14336;
    @SystemApi
    public static final int FLAG_PERMISSION_APPLY_RESTRICTION = 16384;
    @SystemApi
    public static final int FLAG_PERMISSION_GRANTED_BY_DEFAULT = 32;
    @SystemApi
    public static final int FLAG_PERMISSION_GRANTED_BY_ROLE = 32768;
    public static final int FLAG_PERMISSION_ONE_TIME = 65536;
    @SystemApi
    public static final int FLAG_PERMISSION_POLICY_FIXED = 4;
    @SystemApi
    public static final int FLAG_PERMISSION_RESTRICTION_INSTALLER_EXEMPT = 2048;
    @SystemApi
    public static final int FLAG_PERMISSION_RESTRICTION_SYSTEM_EXEMPT = 4096;
    @SystemApi
    public static final int FLAG_PERMISSION_RESTRICTION_UPGRADE_EXEMPT = 8192;
    @SystemApi
    public static final int FLAG_PERMISSION_REVIEW_REQUIRED = 64;
    @SystemApi
    public static final int FLAG_PERMISSION_REVOKE_ON_UPGRADE = 8;
    public static final int FLAG_PERMISSION_REVOKE_WHEN_REQUESTED = 128;
    @SystemApi
    public static final int FLAG_PERMISSION_SYSTEM_FIXED = 16;
    @SystemApi
    public static final int FLAG_PERMISSION_USER_FIXED = 2;
    @SystemApi
    public static final int FLAG_PERMISSION_USER_SENSITIVE_WHEN_DENIED = 512;
    @SystemApi
    public static final int FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED = 256;
    @SystemApi
    public static final int FLAG_PERMISSION_USER_SET = 1;
    public static final int FLAG_PERMISSION_WHITELIST_INSTALLER = 2;
    public static final int FLAG_PERMISSION_WHITELIST_SYSTEM = 1;
    public static final int FLAG_PERMISSION_WHITELIST_UPGRADE = 4;
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
    @Deprecated
    public static final int GET_SIGNATURES = 64;
    public static final int GET_SIGNING_CERTIFICATES = 134217728;
    @Deprecated
    public static final int GET_UNINSTALLED_PACKAGES = 8192;
    public static final int GET_URI_PERMISSION_PATTERNS = 2048;
    public static final String HW_LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    public static final int INSTALL_ALLOCATE_AGGRESSIVE = 32768;
    public static final int INSTALL_ALLOW_DOWNGRADE = 1048576;
    public static final int INSTALL_ALLOW_TEST = 4;
    public static final int INSTALL_ALL_USERS = 64;
    public static final int INSTALL_ALL_WHITELIST_RESTRICTED_PERMISSIONS = 4194304;
    public static final int INSTALL_APEX = 131072;
    public static final int INSTALL_DISABLE_VERIFICATION = 524288;
    public static final int INSTALL_DONT_KILL_APP = 4096;
    public static final int INSTALL_DRY_RUN = 8388608;
    public static final int INSTALL_ENABLE_ROLLBACK = 262144;
    public static final int INSTALL_FAILED_ABORTED = -115;
    @SystemApi
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;
    public static final int INSTALL_FAILED_BAD_DEX_METADATA = -117;
    public static final int INSTALL_FAILED_BAD_SIGNATURE = -118;
    @SystemApi
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;
    @SystemApi
    public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;
    @SystemApi
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;
    @SystemApi
    public static final int INSTALL_FAILED_DEXOPT = -11;
    @SystemApi
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;
    public static final int INSTALL_FAILED_DUPLICATE_PERMISSION = -112;
    public static final int INSTALL_FAILED_INSTANT_APP_INVALID = -116;
    @SystemApi
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;
    @SystemApi
    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
    @SystemApi
    public static final int INSTALL_FAILED_INVALID_APK = -2;
    @SystemApi
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;
    @SystemApi
    public static final int INSTALL_FAILED_INVALID_URI = -3;
    @SystemApi
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;
    @SystemApi
    public static final int INSTALL_FAILED_MISSING_FEATURE = -17;
    @SystemApi
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;
    public static final int INSTALL_FAILED_MISSING_SPLIT = -28;
    public static final int INSTALL_FAILED_MULTIPACKAGE_INCONSISTENCY = -120;
    @SystemApi
    public static final int INSTALL_FAILED_NEWER_SDK = -14;
    public static final int INSTALL_FAILED_NO_MATCHING_ABIS = -113;
    @SystemApi
    public static final int INSTALL_FAILED_NO_SHARED_USER = -6;
    @SystemApi
    public static final int INSTALL_FAILED_OLDER_SDK = -12;
    public static final int INSTALL_FAILED_OTHER_STAGED_SESSION_IN_PROGRESS = -119;
    @SystemApi
    public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;
    @SystemApi
    public static final int INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE = -26;
    @SystemApi
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;
    @SystemApi
    public static final int INSTALL_FAILED_SANDBOX_VERSION_DOWNGRADE = -27;
    @SystemApi
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;
    @SystemApi
    public static final int INSTALL_FAILED_TEST_ONLY = -15;
    public static final int INSTALL_FAILED_UID_CHANGED = -24;
    @SystemApi
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;
    public static final int INSTALL_FAILED_USER_RESTRICTED = -111;
    @SystemApi
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;
    @SystemApi
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;
    public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;
    public static final int INSTALL_FAILED_WRONG_INSTALLED_VERSION = -121;
    public static final int INSTALL_FORCE_PERMISSION_PROMPT = 1024;
    public static final int INSTALL_FORCE_VOLUME_UUID = 512;
    public static final int INSTALL_FROM_ADB = 32;
    public static final int INSTALL_FULL_APP = 16384;
    public static final int INSTALL_GRANT_RUNTIME_PERMISSIONS = 256;
    public static final int INSTALL_INSTANT_APP = 2048;
    public static final int INSTALL_INTERNAL = 16;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;
    @SystemApi
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;
    public static final int INSTALL_REASON_DEVICE_RESTORE = 2;
    public static final int INSTALL_REASON_DEVICE_SETUP = 3;
    public static final int INSTALL_REASON_POLICY = 1;
    public static final int INSTALL_REASON_UNKNOWN = 0;
    public static final int INSTALL_REASON_USER = 4;
    public static final int INSTALL_REASON_USER_MDM = 9;
    @UnsupportedAppUsage
    public static final int INSTALL_REPLACE_EXISTING = 2;
    public static final int INSTALL_REQUEST_DOWNGRADE = 128;
    public static final int INSTALL_STAGED = 2097152;
    @SystemApi
    public static final int INSTALL_SUCCEEDED = 1;
    public static final int INSTALL_UNKNOWN = 0;
    public static final int INSTALL_VIRTUAL_PRELOAD = 65536;
    @SystemApi
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS = 2;
    @SystemApi
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS_ASK = 4;
    @SystemApi
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ASK = 1;
    @SystemApi
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_NEVER = 3;
    @SystemApi
    public static final int INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_UNDEFINED = 0;
    @SystemApi
    public static final int INTENT_FILTER_VERIFICATION_FAILURE = -1;
    @SystemApi
    public static final int INTENT_FILTER_VERIFICATION_SUCCESS = 1;
    @SystemApi
    @Deprecated
    public static final int MASK_PERMISSION_FLAGS = 255;
    public static final int MASK_PERMISSION_FLAGS_ALL = 130047;
    public static final int MATCH_ALL = 131072;
    @SystemApi
    public static final int MATCH_ANY_USER = 4194304;
    public static final int MATCH_APEX = 1073741824;
    @Deprecated
    public static final int MATCH_DEBUG_TRIAGED_MISSING = 268435456;
    public static final int MATCH_DEFAULT_ONLY = 65536;
    public static final int MATCH_DIRECT_BOOT_AUTO = 268435456;
    public static final int MATCH_DIRECT_BOOT_AWARE = 524288;
    public static final int MATCH_DIRECT_BOOT_UNAWARE = 262144;
    public static final int MATCH_DISABLED_COMPONENTS = 512;
    public static final int MATCH_DISABLED_UNTIL_USED_COMPONENTS = 32768;
    public static final int MATCH_EXPLICITLY_VISIBLE_ONLY = 33554432;
    @SystemApi
    public static final int MATCH_FACTORY_ONLY = 2097152;
    public static final int MATCH_HIDDEN_UNTIL_INSTALLED_COMPONENTS = 536870912;
    @SystemApi
    public static final int MATCH_INSTANT = 8388608;
    public static final int MATCH_KNOWN_PACKAGES = 4202496;
    public static final int MATCH_STATIC_SHARED_LIBRARIES = 67108864;
    public static final int MATCH_SYSTEM_ONLY = 1048576;
    public static final int MATCH_UNINSTALLED_PACKAGES = 8192;
    public static final int MATCH_VISIBLE_TO_INSTANT_APP_ONLY = 16777216;
    public static final long MAXIMUM_VERIFICATION_TIMEOUT = 3600000;
    @UnsupportedAppUsage
    @Deprecated
    public static final int MOVE_EXTERNAL_MEDIA = 2;
    public static final int MOVE_FAILED_3RD_PARTY_NOT_ALLOWED_ON_INTERNAL = -9;
    public static final int MOVE_FAILED_DEVICE_ADMIN = -8;
    public static final int MOVE_FAILED_DOESNT_EXIST = -2;
    public static final int MOVE_FAILED_INSUFFICIENT_STORAGE = -1;
    public static final int MOVE_FAILED_INTERNAL_ERROR = -6;
    public static final int MOVE_FAILED_INVALID_LOCATION = -5;
    public static final int MOVE_FAILED_LOCKED_USER = -10;
    public static final int MOVE_FAILED_OPERATION_PENDING = -7;
    public static final int MOVE_FAILED_SYSTEM_PACKAGE = -3;
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    public static final int NO_NATIVE_LIBRARIES = -114;
    public static final int ONLY_IF_NO_MATCH_FOUND = 4;
    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_GRANTED = 0;
    @SystemApi
    public static final int RESTRICTION_HIDE_FROM_SUGGESTIONS = 1;
    @SystemApi
    public static final int RESTRICTION_HIDE_NOTIFICATIONS = 2;
    @SystemApi
    public static final int RESTRICTION_NONE = 0;
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface ApplicationInfoFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CertificateInputType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ComponentInfoFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DeleteFlags {
    }

    @SystemApi
    public static abstract class DexModuleRegisterCallback {
        public abstract void onDexModuleRegistered(String str, boolean z, String str2);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DistractionRestriction {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EnabledFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EnabledState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InstallFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InstallReason {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InstrumentationInfoFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ModuleInfoFlags {
    }

    @SystemApi
    public interface OnPermissionsChangedListener {
        void onPermissionsChanged(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PackageInfoFlags {
    }

    @SystemApi
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionGroupInfoFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionInfoFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionResult {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionWhitelistFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResolveInfoFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SignatureResult {
    }

    @UnsupportedAppUsage
    public abstract void addCrossProfileIntentFilter(IntentFilter intentFilter, int i, int i2, int i3);

    @SystemApi
    public abstract void addOnPermissionsChangeListener(OnPermissionsChangedListener onPermissionsChangedListener);

    @Deprecated
    public abstract void addPackageToPreferred(String str);

    public abstract boolean addPermission(PermissionInfo permissionInfo);

    public abstract boolean addPermissionAsync(PermissionInfo permissionInfo);

    @Deprecated
    public abstract void addPreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName);

    @SystemApi
    public abstract boolean arePermissionsIndividuallyControlled();

    public abstract boolean canRequestPackageInstalls();

    public abstract String[] canonicalToCurrentPackageNames(String[] strArr);

    public abstract int checkPermission(String str, String str2);

    public abstract int checkSignatures(int i, int i2);

    public abstract int checkSignatures(String str, String str2);

    @UnsupportedAppUsage
    public abstract void clearApplicationUserData(String str, IPackageDataObserver iPackageDataObserver);

    @UnsupportedAppUsage
    public abstract void clearCrossProfileIntentFilters(int i);

    public abstract void clearInstantAppCookie();

    @Deprecated
    public abstract void clearPackagePreferredActivities(String str);

    public abstract String[] currentToCanonicalPackageNames(String[] strArr);

    @UnsupportedAppUsage
    public abstract void deleteApplicationCacheFiles(String str, IPackageDataObserver iPackageDataObserver);

    @UnsupportedAppUsage
    public abstract void deleteApplicationCacheFilesAsUser(String str, int i, IPackageDataObserver iPackageDataObserver);

    @UnsupportedAppUsage
    public abstract void deletePackage(String str, IPackageDeleteObserver iPackageDeleteObserver, int i);

    @UnsupportedAppUsage
    public abstract void deletePackageAsUser(String str, IPackageDeleteObserver iPackageDeleteObserver, int i, int i2);

    public abstract void extendVerificationTimeout(int i, int i2, long j);

    @UnsupportedAppUsage
    public abstract void flushPackageRestrictionsAsUser(int i);

    @UnsupportedAppUsage
    public abstract void freeStorage(String str, long j, IntentSender intentSender);

    @UnsupportedAppUsage
    public abstract void freeStorageAndNotify(String str, long j, IPackageDataObserver iPackageDataObserver);

    public abstract Drawable getActivityBanner(ComponentName componentName) throws NameNotFoundException;

    public abstract Drawable getActivityBanner(Intent intent) throws NameNotFoundException;

    public abstract Drawable getActivityIcon(ComponentName componentName) throws NameNotFoundException;

    public abstract Drawable getActivityIcon(Intent intent) throws NameNotFoundException;

    public abstract ActivityInfo getActivityInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract Drawable getActivityLogo(ComponentName componentName) throws NameNotFoundException;

    public abstract Drawable getActivityLogo(Intent intent) throws NameNotFoundException;

    @SystemApi
    public abstract List<IntentFilter> getAllIntentFilters(String str);

    public abstract List<PermissionGroupInfo> getAllPermissionGroups(int i);

    public abstract Drawable getApplicationBanner(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationBanner(String str) throws NameNotFoundException;

    public abstract int getApplicationEnabledSetting(String str);

    @UnsupportedAppUsage
    public abstract boolean getApplicationHiddenSettingAsUser(String str, UserHandle userHandle);

    public abstract Drawable getApplicationIcon(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationIcon(String str) throws NameNotFoundException;

    public abstract ApplicationInfo getApplicationInfo(String str, int i) throws NameNotFoundException;

    @UnsupportedAppUsage
    public abstract ApplicationInfo getApplicationInfoAsUser(String str, int i, int i2) throws NameNotFoundException;

    public abstract CharSequence getApplicationLabel(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationLogo(ApplicationInfo applicationInfo);

    public abstract Drawable getApplicationLogo(String str) throws NameNotFoundException;

    public abstract Intent getCarLaunchIntentForPackage(String str);

    public abstract ChangedPackages getChangedPackages(int i);

    public abstract int getComponentEnabledSetting(ComponentName componentName);

    public abstract Drawable getDefaultActivityIcon();

    @SystemApi
    public abstract String getDefaultBrowserPackageNameAsUser(int i);

    public abstract Drawable getDrawable(String str, int i, ApplicationInfo applicationInfo);

    @UnsupportedAppUsage
    public abstract ComponentName getHomeActivities(List<ResolveInfo> list);

    public abstract int getInstallReason(String str, UserHandle userHandle);

    public abstract List<ApplicationInfo> getInstalledApplications(int i);

    public abstract List<ApplicationInfo> getInstalledApplicationsAsUser(int i, int i2);

    public abstract List<PackageInfo> getInstalledPackages(int i);

    @SystemApi
    public abstract List<PackageInfo> getInstalledPackagesAsUser(int i, int i2);

    public abstract String getInstallerPackageName(String str);

    public abstract String getInstantAppAndroidId(String str, UserHandle userHandle);

    public abstract byte[] getInstantAppCookie();

    public abstract int getInstantAppCookieMaxBytes();

    public abstract int getInstantAppCookieMaxSize();

    @SystemApi
    public abstract Drawable getInstantAppIcon(String str);

    @SystemApi
    public abstract ComponentName getInstantAppInstallerComponent();

    @SystemApi
    public abstract ComponentName getInstantAppResolverSettingsComponent();

    @SystemApi
    public abstract List<InstantAppInfo> getInstantApps();

    public abstract InstrumentationInfo getInstrumentationInfo(ComponentName componentName, int i) throws NameNotFoundException;

    @SystemApi
    public abstract List<IntentFilterVerificationInfo> getIntentFilterVerifications(String str);

    @SystemApi
    public abstract int getIntentVerificationStatusAsUser(String str, int i);

    @UnsupportedAppUsage
    public abstract KeySet getKeySetByAlias(String str, String str2);

    public abstract Intent getLaunchIntentForPackage(String str);

    public abstract Intent getLeanbackLaunchIntentForPackage(String str);

    @UnsupportedAppUsage
    public abstract int getMoveStatus(int i);

    public abstract String getNameForUid(int i);

    public abstract String[] getNamesForUids(int[] iArr);

    @UnsupportedAppUsage
    public abstract List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo applicationInfo);

    @UnsupportedAppUsage
    public abstract VolumeInfo getPackageCurrentVolume(ApplicationInfo applicationInfo);

    public abstract int[] getPackageGids(String str) throws NameNotFoundException;

    public abstract int[] getPackageGids(String str, int i) throws NameNotFoundException;

    public abstract PackageInfo getPackageInfo(VersionedPackage versionedPackage, int i) throws NameNotFoundException;

    public abstract PackageInfo getPackageInfo(String str, int i) throws NameNotFoundException;

    @UnsupportedAppUsage
    public abstract PackageInfo getPackageInfoAsUser(String str, int i, int i2) throws NameNotFoundException;

    public abstract PackageInstaller getPackageInstaller();

    @UnsupportedAppUsage
    @Deprecated
    public abstract void getPackageSizeInfoAsUser(String str, int i, IPackageStatsObserver iPackageStatsObserver);

    public abstract int getPackageUid(String str, int i) throws NameNotFoundException;

    @UnsupportedAppUsage
    public abstract int getPackageUidAsUser(String str, int i) throws NameNotFoundException;

    @UnsupportedAppUsage
    public abstract int getPackageUidAsUser(String str, int i, int i2) throws NameNotFoundException;

    public abstract String[] getPackagesForUid(int i);

    public abstract List<PackageInfo> getPackagesHoldingPermissions(String[] strArr, int i);

    public abstract String getPermissionControllerPackageName();

    @SystemApi
    public abstract int getPermissionFlags(String str, String str2, UserHandle userHandle);

    public abstract PermissionGroupInfo getPermissionGroupInfo(String str, int i) throws NameNotFoundException;

    public abstract PermissionInfo getPermissionInfo(String str, int i) throws NameNotFoundException;

    @Deprecated
    public abstract int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String str);

    @Deprecated
    public abstract List<PackageInfo> getPreferredPackages(int i);

    public abstract List<VolumeInfo> getPrimaryStorageCandidateVolumes();

    public abstract VolumeInfo getPrimaryStorageCurrentVolume();

    public abstract ProviderInfo getProviderInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract ActivityInfo getReceiverInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract Resources getResourcesForActivity(ComponentName componentName) throws NameNotFoundException;

    public abstract Resources getResourcesForApplication(ApplicationInfo applicationInfo) throws NameNotFoundException;

    public abstract Resources getResourcesForApplication(String str) throws NameNotFoundException;

    @UnsupportedAppUsage
    public abstract Resources getResourcesForApplicationAsUser(String str, int i) throws NameNotFoundException;

    public abstract ServiceInfo getServiceInfo(ComponentName componentName, int i) throws NameNotFoundException;

    public abstract String getServicesSystemSharedLibraryPackageName();

    public abstract List<SharedLibraryInfo> getSharedLibraries(int i);

    public abstract List<SharedLibraryInfo> getSharedLibrariesAsUser(int i, int i2);

    public abstract String getSharedSystemSharedLibraryPackageName();

    @UnsupportedAppUsage
    public abstract KeySet getSigningKeySet(String str);

    public abstract FeatureInfo[] getSystemAvailableFeatures();

    public abstract String[] getSystemSharedLibraryNames();

    public abstract CharSequence getText(String str, int i, ApplicationInfo applicationInfo);

    @UnsupportedAppUsage
    public abstract int getUidForSharedUser(String str) throws NameNotFoundException;

    @UnsupportedAppUsage
    public abstract Drawable getUserBadgeForDensity(UserHandle userHandle, int i);

    @UnsupportedAppUsage
    public abstract Drawable getUserBadgeForDensityNoBackground(UserHandle userHandle, int i);

    public abstract Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle userHandle, Rect rect, int i);

    public abstract Drawable getUserBadgedIcon(Drawable drawable, UserHandle userHandle);

    public abstract CharSequence getUserBadgedLabel(CharSequence charSequence, UserHandle userHandle);

    public abstract VerifierDeviceIdentity getVerifierDeviceIdentity();

    public abstract XmlResourceParser getXml(String str, int i, ApplicationInfo applicationInfo);

    @SystemApi
    public abstract void grantRuntimePermission(String str, String str2, UserHandle userHandle);

    public abstract boolean hasSystemFeature(String str);

    public abstract boolean hasSystemFeature(String str, int i);

    @SystemApi
    @Deprecated
    public abstract int installExistingPackage(String str) throws NameNotFoundException;

    @SystemApi
    @Deprecated
    public abstract int installExistingPackage(String str, int i) throws NameNotFoundException;

    @UnsupportedAppUsage
    @Deprecated
    public abstract int installExistingPackageAsUser(String str, int i) throws NameNotFoundException;

    public abstract boolean isInstantApp();

    public abstract boolean isInstantApp(String str);

    @UnsupportedAppUsage
    public abstract boolean isPackageAvailable(String str);

    @UnsupportedAppUsage
    public abstract boolean isPackageSuspendedForUser(String str, int i);

    public abstract boolean isPermissionRevokedByPolicy(String str, String str2);

    public abstract boolean isSafeMode();

    @UnsupportedAppUsage
    public abstract boolean isSignedBy(String str, KeySet keySet);

    @UnsupportedAppUsage
    public abstract boolean isSignedByExactly(String str, KeySet keySet);

    @UnsupportedAppUsage
    public abstract boolean isUpgrade();

    public abstract boolean isWirelessConsentModeEnabled();

    @UnsupportedAppUsage
    public abstract Drawable loadItemIcon(PackageItemInfo packageItemInfo, ApplicationInfo applicationInfo);

    @UnsupportedAppUsage
    public abstract Drawable loadUnbadgedItemIcon(PackageItemInfo packageItemInfo, ApplicationInfo applicationInfo);

    @UnsupportedAppUsage
    public abstract int movePackage(String str, VolumeInfo volumeInfo);

    public abstract int movePrimaryStorage(VolumeInfo volumeInfo);

    public abstract List<ResolveInfo> queryBroadcastReceivers(Intent intent, int i);

    @UnsupportedAppUsage
    public abstract List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int i, int i2);

    public abstract List<ProviderInfo> queryContentProviders(String str, int i, int i2);

    public abstract List<InstrumentationInfo> queryInstrumentation(String str, int i);

    public abstract List<ResolveInfo> queryIntentActivities(Intent intent, int i);

    @UnsupportedAppUsage
    public abstract List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int i, int i2);

    public abstract List<ResolveInfo> queryIntentActivityOptions(ComponentName componentName, Intent[] intentArr, Intent intent, int i);

    public abstract List<ResolveInfo> queryIntentContentProviders(Intent intent, int i);

    @UnsupportedAppUsage
    public abstract List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int i, int i2);

    public abstract List<ResolveInfo> queryIntentServices(Intent intent, int i);

    @UnsupportedAppUsage
    public abstract List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int i, int i2);

    public abstract List<PermissionInfo> queryPermissionsByGroup(String str, int i) throws NameNotFoundException;

    @SystemApi
    public abstract void registerDexModule(String str, DexModuleRegisterCallback dexModuleRegisterCallback);

    @UnsupportedAppUsage
    public abstract void registerMoveCallback(MoveCallback moveCallback, Handler handler);

    @SystemApi
    public abstract void removeOnPermissionsChangeListener(OnPermissionsChangedListener onPermissionsChangedListener);

    @Deprecated
    public abstract void removePackageFromPreferred(String str);

    public abstract void removePermission(String str);

    @UnsupportedAppUsage
    @Deprecated
    public abstract void replacePreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName);

    public abstract ResolveInfo resolveActivity(Intent intent, int i);

    @UnsupportedAppUsage
    public abstract ResolveInfo resolveActivityAsUser(Intent intent, int i, int i2);

    public abstract ProviderInfo resolveContentProvider(String str, int i);

    @UnsupportedAppUsage
    public abstract ProviderInfo resolveContentProviderAsUser(String str, int i, int i2);

    public abstract ResolveInfo resolveService(Intent intent, int i);

    public abstract ResolveInfo resolveServiceAsUser(Intent intent, int i, int i2);

    @SystemApi
    public abstract void revokeRuntimePermission(String str, String str2, UserHandle userHandle);

    public abstract void setApplicationCategoryHint(String str, int i);

    public abstract void setApplicationEnabledSetting(String str, int i, int i2);

    @UnsupportedAppUsage
    public abstract boolean setApplicationHiddenSettingAsUser(String str, boolean z, UserHandle userHandle);

    public abstract void setComponentEnabledSetting(ComponentName componentName, int i, int i2);

    @SystemApi
    public abstract boolean setDefaultBrowserPackageNameAsUser(String str, int i);

    public abstract void setInstallerPackageName(String str, String str2);

    public abstract boolean setInstantAppCookie(byte[] bArr);

    @SystemApi
    public abstract void setUpdateAvailable(String str, boolean z);

    @UnsupportedAppUsage
    public abstract boolean shouldShowRequestPermissionRationale(String str);

    @UnsupportedAppUsage
    public abstract void unregisterMoveCallback(MoveCallback moveCallback);

    public abstract void updateInstantAppCookie(byte[] bArr);

    @SystemApi
    public abstract boolean updateIntentVerificationStatusAsUser(String str, int i, int i2);

    @SystemApi
    public abstract void updatePermissionFlags(String str, String str2, int i, int i2, UserHandle userHandle);

    @SystemApi
    public abstract void verifyIntentFilter(int i, int i2, List<String> list);

    public abstract void verifyPendingInstall(int i, int i2);

    public static class NameNotFoundException extends AndroidException {
        public NameNotFoundException() {
        }

        public NameNotFoundException(String name) {
            super(name);
        }
    }

    public int getUserId() {
        return UserHandle.myUserId();
    }

    @SystemApi
    public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, UserHandle user) throws NameNotFoundException {
        return getApplicationInfoAsUser(packageName, flags, user.getIdentifier());
    }

    public ModuleInfo getModuleInfo(String packageName, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException("getModuleInfo not implemented in subclass");
    }

    public List<ModuleInfo> getInstalledModules(int flags) {
        throw new UnsupportedOperationException("getInstalledModules not implemented in subclass");
    }

    public Set<String> getWhitelistedRestrictedPermissions(String packageName, int whitelistFlag) {
        return Collections.emptySet();
    }

    public boolean addWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags) {
        return false;
    }

    public boolean removeWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags) {
        return false;
    }

    @UnsupportedAppUsage
    public Intent buildRequestPermissionsIntent(String[] permissions) {
        if (!ArrayUtils.isEmpty(permissions)) {
            Intent intent = new Intent(ACTION_REQUEST_PERMISSIONS);
            intent.putExtra(EXTRA_REQUEST_PERMISSIONS_NAMES, permissions);
            intent.setPackage(getPermissionControllerPackageName());
            return intent;
        }
        throw new IllegalArgumentException("permission cannot be null or empty");
    }

    @SystemApi
    public List<SharedLibraryInfo> getDeclaredSharedLibraries(String packageName, int flags) {
        throw new UnsupportedOperationException("getDeclaredSharedLibraries() not implemented in subclass");
    }

    @SystemApi
    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, UserHandle user) {
        return queryIntentActivitiesAsUser(intent, flags, user.getIdentifier());
    }

    @SystemApi
    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, UserHandle userHandle) {
        return queryBroadcastReceiversAsUser(intent, flags, userHandle.getIdentifier());
    }

    @UnsupportedAppUsage
    @Deprecated
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, int userId) {
        if (VMRuntime.getRuntime().getTargetSdkVersion() < 26) {
            Log.d(TAG, "Shame on you for calling the hidden API queryBroadcastReceivers(). Shame!");
            return queryBroadcastReceiversAsUser(intent, flags, userId);
        }
        throw new UnsupportedOperationException("Shame on you for calling the hidden API queryBroadcastReceivers(). Shame!");
    }

    @SystemApi
    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, UserHandle user) {
        return queryIntentServicesAsUser(intent, flags, user.getIdentifier());
    }

    @SystemApi
    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, UserHandle user) {
        return queryIntentContentProvidersAsUser(intent, flags, user.getIdentifier());
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, String metaDataKey) {
        return queryContentProviders(processName, uid, flags);
    }

    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        PackageParser parser = new PackageParser();
        parser.setCallback(new PackageParser.CallbackImpl(this));
        File apkFile = new File(archiveFilePath);
        if ((flags & 786432) == 0) {
            flags |= 786432;
        }
        try {
            PackageParser.Package pkg = parser.parseMonolithicPackage(apkFile, 0);
            if ((flags & 64) != 0) {
                PackageParser.collectCertificates(pkg, false);
            }
            return PackageParser.generatePackageInfo(pkg, null, flags, 0, 0, null, new PackageUserState());
        } catch (PackageParser.PackageParserException e) {
            return null;
        }
    }

    @UnsupportedAppUsage
    public void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {
        freeStorageAndNotify(null, freeStorageSize, observer);
    }

    @UnsupportedAppUsage
    public void freeStorage(long freeStorageSize, IntentSender pi) {
        freeStorage(null, freeStorageSize, pi);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void getPackageSizeInfo(String packageName, IPackageStatsObserver observer) {
        getPackageSizeInfoAsUser(packageName, getUserId(), observer);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void addPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    @SystemApi
    public void replacePreferredActivity(IntentFilter filter, int match, List<ComponentName> set, ComponentName activity) {
        replacePreferredActivity(filter, match, (ComponentName[]) set.toArray(new ComponentName[0]), activity);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void replacePreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    @SystemApi
    public void setSyntheticAppDetailsActivityEnabled(String packageName, boolean enabled) {
        throw new UnsupportedOperationException("setSyntheticAppDetailsActivityEnabled not implemented");
    }

    public boolean getSyntheticAppDetailsActivityEnabled(String packageName) {
        throw new UnsupportedOperationException("getSyntheticAppDetailsActivityEnabled not implemented");
    }

    @SystemApi
    public String[] setDistractingPackageRestrictions(String[] packages, int restrictionFlags) {
        throw new UnsupportedOperationException("setDistractingPackageRestrictions not implemented");
    }

    @SystemApi
    @Deprecated
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, String dialogMessage) {
        throw new UnsupportedOperationException("setPackagesSuspended not implemented");
    }

    @SystemApi
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, SuspendDialogInfo dialogInfo) {
        throw new UnsupportedOperationException("setPackagesSuspended not implemented");
    }

    @SystemApi
    public String[] getUnsuspendablePackages(String[] packageNames) {
        throw new UnsupportedOperationException("canSuspendPackages not implemented");
    }

    public boolean isPackageSuspended(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException("isPackageSuspended not implemented");
    }

    public boolean isPackageSuspended() {
        throw new UnsupportedOperationException("isPackageSuspended not implemented");
    }

    public Bundle getSuspendedPackageAppExtras() {
        throw new UnsupportedOperationException("getSuspendedPackageAppExtras not implemented");
    }

    public static boolean isMoveStatusFinished(int status) {
        return status < 0 || status > 100;
    }

    public static abstract class MoveCallback {
        public abstract void onStatusChanged(int i, int i2, long j);

        public void onCreated(int moveId, Bundle extras) {
        }
    }

    public boolean isDeviceUpgrading() {
        return false;
    }

    @UnsupportedAppUsage
    public static String installStatusToString(int status, String msg) {
        String str = installStatusToString(status);
        if (msg == null) {
            return str;
        }
        return str + ": " + msg;
    }

    @UnsupportedAppUsage
    public static String installStatusToString(int status) {
        if (status == -121) {
            return "INSTALL_FAILED_WRONG_INSTALLED_VERSION";
        }
        if (status == -115) {
            return "INSTALL_FAILED_ABORTED";
        }
        if (status == -28) {
            return "INSTALL_FAILED_MISSING_SPLIT";
        }
        if (status == 1) {
            return "INSTALL_SUCCEEDED";
        }
        if (status == -118) {
            return "INSTALL_FAILED_BAD_SIGNATURE";
        }
        if (status == -117) {
            return "INSTALL_FAILED_BAD_DEX_METADATA";
        }
        switch (status) {
            case -113:
                return "INSTALL_FAILED_NO_MATCHING_ABIS";
            case INSTALL_FAILED_DUPLICATE_PERMISSION /* -112 */:
                return "INSTALL_FAILED_DUPLICATE_PERMISSION";
            case INSTALL_FAILED_USER_RESTRICTED /* -111 */:
                return "INSTALL_FAILED_USER_RESTRICTED";
            case -110:
                return "INSTALL_FAILED_INTERNAL_ERROR";
            case INSTALL_PARSE_FAILED_MANIFEST_EMPTY /* -109 */:
                return "INSTALL_PARSE_FAILED_MANIFEST_EMPTY";
            case INSTALL_PARSE_FAILED_MANIFEST_MALFORMED /* -108 */:
                return "INSTALL_PARSE_FAILED_MANIFEST_MALFORMED";
            case INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID /* -107 */:
                return "INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID";
            case INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME /* -106 */:
                return "INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME";
            case INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING /* -105 */:
                return "INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING";
            case INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES /* -104 */:
                return "INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES";
            case INSTALL_PARSE_FAILED_NO_CERTIFICATES /* -103 */:
                return "INSTALL_PARSE_FAILED_NO_CERTIFICATES";
            case INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION /* -102 */:
                return "INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION";
            case -101:
                return "INSTALL_PARSE_FAILED_BAD_MANIFEST";
            case -100:
                return "INSTALL_PARSE_FAILED_NOT_APK";
            default:
                switch (status) {
                    case -25:
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
                    default:
                        return Integer.toString(status);
                }
        }
    }

    public static int installStatusToPublicStatus(int status) {
        if (status == -118 || status == -117) {
            return 4;
        }
        if (status == -115) {
            return 3;
        }
        if (status == -28) {
            return 7;
        }
        if (status == 1) {
            return 0;
        }
        switch (status) {
            case -113:
                return 7;
            case INSTALL_FAILED_DUPLICATE_PERMISSION /* -112 */:
                return 5;
            case INSTALL_FAILED_USER_RESTRICTED /* -111 */:
                return 7;
            case -110:
                return 1;
            case INSTALL_PARSE_FAILED_MANIFEST_EMPTY /* -109 */:
                return 4;
            case INSTALL_PARSE_FAILED_MANIFEST_MALFORMED /* -108 */:
                return 4;
            case INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID /* -107 */:
                return 4;
            case INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME /* -106 */:
                return 4;
            case INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING /* -105 */:
                return 4;
            case INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES /* -104 */:
                return 4;
            case INSTALL_PARSE_FAILED_NO_CERTIFICATES /* -103 */:
                return 4;
            case INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION /* -102 */:
                return 4;
            case -101:
                return 4;
            case -100:
                return 4;
            default:
                switch (status) {
                    case -26:
                        return 4;
                    case -25:
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
                    default:
                        return 1;
                }
        }
    }

    public static String deleteStatusToString(int status, String msg) {
        String str = deleteStatusToString(status);
        if (msg == null) {
            return str;
        }
        return str + ": " + msg;
    }

    @UnsupportedAppUsage
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
            case 0:
            default:
                return Integer.toString(status);
            case 1:
                return "DELETE_SUCCEEDED";
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
            case 0:
            default:
                return 1;
            case 1:
                return 0;
        }
    }

    public static String permissionFlagToString(int flag) {
        if (flag == 1) {
            return "USER_SET";
        }
        if (flag == 2) {
            return "USER_FIXED";
        }
        switch (flag) {
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
            case 128:
                return "REVOKE_WHEN_REQUESTED";
            case 256:
                return "USER_SENSITIVE_WHEN_GRANTED";
            case 512:
                return "USER_SENSITIVE_WHEN_DENIED";
            case 2048:
                return "RESTRICTION_INSTALLER_EXEMPT";
            case 4096:
                return "RESTRICTION_SYSTEM_EXEMPT";
            case 8192:
                return "RESTRICTION_UPGRADE_EXEMPT";
            case 16384:
                return "APPLY_RESTRICTION";
            case 32768:
                return "GRANTED_BY_ROLE";
            case 65536:
                return "ONE_TIME";
            default:
                return Integer.toString(flag);
        }
    }

    public static class LegacyPackageDeleteObserver extends PackageDeleteObserver {
        private final IPackageDeleteObserver mLegacy;

        public LegacyPackageDeleteObserver(IPackageDeleteObserver legacy) {
            this.mLegacy = legacy;
        }

        @Override // android.app.PackageDeleteObserver
        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            IPackageDeleteObserver iPackageDeleteObserver = this.mLegacy;
            if (iPackageDeleteObserver != null) {
                try {
                    iPackageDeleteObserver.packageDeleted(basePackageName, returnCode);
                } catch (RemoteException e) {
                }
            }
        }
    }

    @SystemApi
    public ArtManager getArtManager() {
        throw new UnsupportedOperationException("getArtManager not implemented in subclass");
    }

    @SystemApi
    public void setHarmfulAppWarning(String packageName, CharSequence warning) {
        throw new UnsupportedOperationException("setHarmfulAppWarning not implemented in subclass");
    }

    @SystemApi
    public CharSequence getHarmfulAppWarning(String packageName) {
        throw new UnsupportedOperationException("getHarmfulAppWarning not implemented in subclass");
    }

    public boolean hasSigningCertificate(String packageName, byte[] certificate, int type) {
        throw new UnsupportedOperationException("hasSigningCertificate not implemented in subclass");
    }

    public boolean hasSigningCertificate(int uid, byte[] certificate, int type) {
        throw new UnsupportedOperationException("hasSigningCertificate not implemented in subclass");
    }

    public String getSystemTextClassifierPackageName() {
        throw new UnsupportedOperationException("getSystemTextClassifierPackageName not implemented in subclass");
    }

    public String getAttentionServicePackageName() {
        throw new UnsupportedOperationException("getAttentionServicePackageName not implemented in subclass");
    }

    public String getWellbeingPackageName() {
        throw new UnsupportedOperationException("getWellbeingPackageName not implemented in subclass");
    }

    public String getAppPredictionServicePackageName() {
        throw new UnsupportedOperationException("getAppPredictionServicePackageName not implemented in subclass");
    }

    public String getSystemCaptionsServicePackageName() {
        throw new UnsupportedOperationException("getSystemCaptionsServicePackageName not implemented in subclass");
    }

    @SystemApi
    public String getIncidentReportApproverPackageName() {
        throw new UnsupportedOperationException("getIncidentReportApproverPackageName not implemented in subclass");
    }

    public boolean isPackageStateProtected(String packageName, int userId) {
        throw new UnsupportedOperationException("isPackageStateProtected not implemented in subclass");
    }

    @SystemApi
    public void sendDeviceCustomizationReadyBroadcast() {
        throw new UnsupportedOperationException("sendDeviceCustomizationReadyBroadcast not implemented in subclass");
    }
}
