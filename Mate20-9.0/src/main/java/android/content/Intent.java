package android.content;

import android.annotation.SystemApi;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.SearchManager;
import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteAuditLog;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.os.StrictMode;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Intent implements Parcelable, Cloneable {
    public static final String ACTION_ADVANCED_SETTINGS_CHANGED = "android.intent.action.ADVANCED_SETTINGS";
    public static final String ACTION_AIRPLANE_MODE_CHANGED = "android.intent.action.AIRPLANE_MODE";
    public static final String ACTION_ALARM_CHANGED = "android.intent.action.ALARM_CHANGED";
    public static final String ACTION_ALL_APPS = "android.intent.action.ALL_APPS";
    public static final String ACTION_ANSWER = "android.intent.action.ANSWER";
    public static final String ACTION_APPLICATION_PREFERENCES = "android.intent.action.APPLICATION_PREFERENCES";
    public static final String ACTION_APPLICATION_RESTRICTIONS_CHANGED = "android.intent.action.APPLICATION_RESTRICTIONS_CHANGED";
    public static final String ACTION_APP_ERROR = "android.intent.action.APP_ERROR";
    public static final String ACTION_ASSIST = "android.intent.action.ASSIST";
    public static final String ACTION_ATTACH_DATA = "android.intent.action.ATTACH_DATA";
    public static final String ACTION_BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
    @SystemApi
    public static final String ACTION_BATTERY_LEVEL_CHANGED = "android.intent.action.BATTERY_LEVEL_CHANGED";
    public static final String ACTION_BATTERY_LOW = "android.intent.action.BATTERY_LOW";
    public static final String ACTION_BATTERY_OKAY = "android.intent.action.BATTERY_OKAY";
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_BUG_REPORT = "android.intent.action.BUG_REPORT";
    public static final String ACTION_CALL = "android.intent.action.CALL";
    public static final String ACTION_CALL_BUTTON = "android.intent.action.CALL_BUTTON";
    @SystemApi
    public static final String ACTION_CALL_EMERGENCY = "android.intent.action.CALL_EMERGENCY";
    @SystemApi
    public static final String ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
    public static final String ACTION_CAMERA_BUTTON = "android.intent.action.CAMERA_BUTTON";
    public static final String ACTION_CARRIER_SETUP = "android.intent.action.CARRIER_SETUP";
    public static final String ACTION_CHOOSER = "android.intent.action.CHOOSER";
    public static final String ACTION_CLEAR_DNS_CACHE = "android.intent.action.CLEAR_DNS_CACHE";
    public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
    public static final String ACTION_CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    public static final String ACTION_CREATE_DOCUMENT = "android.intent.action.CREATE_DOCUMENT";
    public static final String ACTION_CREATE_SHORTCUT = "android.intent.action.CREATE_SHORTCUT";
    public static final String ACTION_DATE_CHANGED = "android.intent.action.DATE_CHANGED";
    public static final String ACTION_DEFAULT = "android.intent.action.VIEW";
    public static final String ACTION_DELETE = "android.intent.action.DELETE";
    @SystemApi
    @Deprecated
    public static final String ACTION_DEVICE_INITIALIZATION_WIZARD = "android.intent.action.DEVICE_INITIALIZATION_WIZARD";
    public static final String ACTION_DEVICE_LOCKED_CHANGED = "android.intent.action.DEVICE_LOCKED_CHANGED";
    @Deprecated
    public static final String ACTION_DEVICE_STORAGE_FULL = "android.intent.action.DEVICE_STORAGE_FULL";
    @Deprecated
    public static final String ACTION_DEVICE_STORAGE_LOW = "android.intent.action.DEVICE_STORAGE_LOW";
    @Deprecated
    public static final String ACTION_DEVICE_STORAGE_NOT_FULL = "android.intent.action.DEVICE_STORAGE_NOT_FULL";
    @Deprecated
    public static final String ACTION_DEVICE_STORAGE_OK = "android.intent.action.DEVICE_STORAGE_OK";
    public static final String ACTION_DIAL = "android.intent.action.DIAL";
    public static final String ACTION_DISMISS_KEYBOARD_SHORTCUTS = "com.android.intent.action.DISMISS_KEYBOARD_SHORTCUTS";
    public static final String ACTION_DOCK_ACTIVE = "android.intent.action.DOCK_ACTIVE";
    public static final String ACTION_DOCK_EVENT = "android.intent.action.DOCK_EVENT";
    public static final String ACTION_DOCK_IDLE = "android.intent.action.DOCK_IDLE";
    public static final String ACTION_DREAMING_STARTED = "android.intent.action.DREAMING_STARTED";
    public static final String ACTION_DREAMING_STOPPED = "android.intent.action.DREAMING_STOPPED";
    public static final String ACTION_DYNAMIC_SENSOR_CHANGED = "android.intent.action.DYNAMIC_SENSOR_CHANGED";
    public static final String ACTION_EDIT = "android.intent.action.EDIT";
    public static final String ACTION_EXTERNAL_APPLICATIONS_AVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
    public static final String ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
    @SystemApi
    public static final String ACTION_FACTORY_RESET = "android.intent.action.FACTORY_RESET";
    public static final String ACTION_FACTORY_TEST = "android.intent.action.FACTORY_TEST";
    public static final String ACTION_FM = "android.intent.action.FM";
    public static final String ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT";
    public static final String ACTION_GET_RESTRICTION_ENTRIES = "android.intent.action.GET_RESTRICTION_ENTRIES";
    @SystemApi
    public static final String ACTION_GLOBAL_BUTTON = "android.intent.action.GLOBAL_BUTTON";
    public static final String ACTION_GTALK_SERVICE_CONNECTED = "android.intent.action.GTALK_CONNECTED";
    public static final String ACTION_GTALK_SERVICE_DISCONNECTED = "android.intent.action.GTALK_DISCONNECTED";
    public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
    public static final String ACTION_IDLE_MAINTENANCE_END = "android.intent.action.ACTION_IDLE_MAINTENANCE_END";
    public static final String ACTION_IDLE_MAINTENANCE_START = "android.intent.action.ACTION_IDLE_MAINTENANCE_START";
    public static final String ACTION_INPUT_METHOD_CHANGED = "android.intent.action.INPUT_METHOD_CHANGED";
    public static final String ACTION_INSERT = "android.intent.action.INSERT";
    public static final String ACTION_INSERT_OR_EDIT = "android.intent.action.INSERT_OR_EDIT";
    public static final String ACTION_INSTALL_FAILURE = "android.intent.action.INSTALL_FAILURE";
    @SystemApi
    public static final String ACTION_INSTALL_INSTANT_APP_PACKAGE = "android.intent.action.INSTALL_INSTANT_APP_PACKAGE";
    public static final String ACTION_INSTALL_PACKAGE = "android.intent.action.INSTALL_PACKAGE";
    @SystemApi
    public static final String ACTION_INSTANT_APP_RESOLVER_SETTINGS = "android.intent.action.INSTANT_APP_RESOLVER_SETTINGS";
    @SystemApi
    public static final String ACTION_INTENT_FILTER_NEEDS_VERIFICATION = "android.intent.action.INTENT_FILTER_NEEDS_VERIFICATION";
    public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
    public static final String ACTION_LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String ACTION_MANAGED_PROFILE_ADDED = "android.intent.action.MANAGED_PROFILE_ADDED";
    public static final String ACTION_MANAGED_PROFILE_AVAILABLE = "android.intent.action.MANAGED_PROFILE_AVAILABLE";
    public static final String ACTION_MANAGED_PROFILE_REMOVED = "android.intent.action.MANAGED_PROFILE_REMOVED";
    public static final String ACTION_MANAGED_PROFILE_UNAVAILABLE = "android.intent.action.MANAGED_PROFILE_UNAVAILABLE";
    public static final String ACTION_MANAGED_PROFILE_UNLOCKED = "android.intent.action.MANAGED_PROFILE_UNLOCKED";
    @SystemApi
    public static final String ACTION_MANAGE_APP_PERMISSIONS = "android.intent.action.MANAGE_APP_PERMISSIONS";
    public static final String ACTION_MANAGE_NETWORK_USAGE = "android.intent.action.MANAGE_NETWORK_USAGE";
    public static final String ACTION_MANAGE_PACKAGE_STORAGE = "android.intent.action.MANAGE_PACKAGE_STORAGE";
    @SystemApi
    public static final String ACTION_MANAGE_PERMISSIONS = "android.intent.action.MANAGE_PERMISSIONS";
    @SystemApi
    public static final String ACTION_MANAGE_PERMISSION_APPS = "android.intent.action.MANAGE_PERMISSION_APPS";
    @SystemApi
    @Deprecated
    public static final String ACTION_MASTER_CLEAR = "android.intent.action.MASTER_CLEAR";
    @SystemApi
    public static final String ACTION_MASTER_CLEAR_NOTIFICATION = "android.intent.action.MASTER_CLEAR_NOTIFICATION";
    public static final String ACTION_MEDIA_ABNORMAL_SD = "com.huawei.storage.MEDIA_ABNORMAL_SD";
    public static final String ACTION_MEDIA_BAD_REMOVAL = "android.intent.action.MEDIA_BAD_REMOVAL";
    public static final String ACTION_MEDIA_BUTTON = "android.intent.action.MEDIA_BUTTON";
    public static final String ACTION_MEDIA_CHECKING = "android.intent.action.MEDIA_CHECKING";
    public static final String ACTION_MEDIA_EJECT = "android.intent.action.MEDIA_EJECT";
    public static final String ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
    public static final String ACTION_MEDIA_MOUNTED_RO = "com.huawei.android.MEDIA_MOUNTED_RO";
    public static final String ACTION_MEDIA_NOFS = "android.intent.action.MEDIA_NOFS";
    public static final String ACTION_MEDIA_REMOVED = "android.intent.action.MEDIA_REMOVED";
    public static final String ACTION_MEDIA_RESOURCE_GRANTED = "android.intent.action.MEDIA_RESOURCE_GRANTED";
    public static final String ACTION_MEDIA_SCANNER_FINISHED = "android.intent.action.MEDIA_SCANNER_FINISHED";
    public static final String ACTION_MEDIA_SCANNER_SCAN_FILE = "android.intent.action.MEDIA_SCANNER_SCAN_FILE";
    public static final String ACTION_MEDIA_SCANNER_STARTED = "android.intent.action.MEDIA_SCANNER_STARTED";
    public static final String ACTION_MEDIA_SHARED = "android.intent.action.MEDIA_SHARED";
    public static final String ACTION_MEDIA_UNMOUNTABLE = "android.intent.action.MEDIA_UNMOUNTABLE";
    public static final String ACTION_MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED";
    public static final String ACTION_MEDIA_UNSHARED = "android.intent.action.MEDIA_UNSHARED";
    public static final String ACTION_MY_PACKAGE_REPLACED = "android.intent.action.MY_PACKAGE_REPLACED";
    public static final String ACTION_MY_PACKAGE_SUSPENDED = "android.intent.action.MY_PACKAGE_SUSPENDED";
    public static final String ACTION_MY_PACKAGE_UNSUSPENDED = "android.intent.action.MY_PACKAGE_UNSUSPENDED";
    public static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    public static final String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
    public static final String ACTION_OPEN_DOCUMENT_TREE = "android.intent.action.OPEN_DOCUMENT_TREE";
    public static final String ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED";
    public static final String ACTION_PACKAGES_SUSPENDED = "android.intent.action.PACKAGES_SUSPENDED";
    public static final String ACTION_PACKAGES_UNSUSPENDED = "android.intent.action.PACKAGES_UNSUSPENDED";
    public static final String ACTION_PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED";
    public static final String ACTION_PACKAGE_DATA_CLEARED = "android.intent.action.PACKAGE_DATA_CLEARED";
    public static final String ACTION_PACKAGE_FIRST_LAUNCH = "android.intent.action.PACKAGE_FIRST_LAUNCH";
    public static final String ACTION_PACKAGE_FULLY_REMOVED = "android.intent.action.PACKAGE_FULLY_REMOVED";
    @Deprecated
    public static final String ACTION_PACKAGE_INSTALL = "android.intent.action.PACKAGE_INSTALL";
    public static final String ACTION_PACKAGE_NEEDS_VERIFICATION = "android.intent.action.PACKAGE_NEEDS_VERIFICATION";
    public static final String ACTION_PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    public static final String ACTION_PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
    public static final String ACTION_PACKAGE_RESTARTED = "android.intent.action.PACKAGE_RESTARTED";
    public static final String ACTION_PACKAGE_VERIFIED = "android.intent.action.PACKAGE_VERIFIED";
    public static final String ACTION_PASTE = "android.intent.action.PASTE";
    public static final String ACTION_PICK = "android.intent.action.PICK";
    public static final String ACTION_PICK_ACTIVITY = "android.intent.action.PICK_ACTIVITY";
    public static final String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
    public static final String ACTION_POWER_DISCONNECTED = "android.intent.action.ACTION_POWER_DISCONNECTED";
    public static final String ACTION_POWER_USAGE_SUMMARY = "android.intent.action.POWER_USAGE_SUMMARY";
    public static final String ACTION_PREFERRED_ACTIVITY_CHANGED = "android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED";
    @SystemApi
    public static final String ACTION_PRE_BOOT_COMPLETED = "android.intent.action.PRE_BOOT_COMPLETED";
    public static final String ACTION_PRIMARYSD_MOUNTED_RO = "com.huawei.android.PRIMARYSD_MOUNTED_RO";
    public static final String ACTION_PROCESS_TEXT = "android.intent.action.PROCESS_TEXT";
    public static final String ACTION_PROVIDER_CHANGED = "android.intent.action.PROVIDER_CHANGED";
    @SystemApi
    public static final String ACTION_QUERY_PACKAGE_RESTART = "android.intent.action.QUERY_PACKAGE_RESTART";
    public static final String ACTION_QUICK_CLOCK = "android.intent.action.QUICK_CLOCK";
    public static final String ACTION_QUICK_VIEW = "android.intent.action.QUICK_VIEW";
    public static final String ACTION_REBOOT = "android.intent.action.REBOOT";
    public static final String ACTION_REMOTE_INTENT = "com.google.android.c2dm.intent.RECEIVE";
    public static final String ACTION_REQUEST_SHUTDOWN = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
    @SystemApi
    public static final String ACTION_RESOLVE_INSTANT_APP_PACKAGE = "android.intent.action.RESOLVE_INSTANT_APP_PACKAGE";
    @SystemApi
    public static final String ACTION_REVIEW_PERMISSIONS = "android.intent.action.REVIEW_PERMISSIONS";
    public static final String ACTION_RUN = "android.intent.action.RUN";
    public static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    public static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    public static final String ACTION_SEARCH = "android.intent.action.SEARCH";
    public static final String ACTION_SEARCH_LONG_PRESS = "android.intent.action.SEARCH_LONG_PRESS";
    public static final String ACTION_SEND = "android.intent.action.SEND";
    public static final String ACTION_SENDTO = "android.intent.action.SENDTO";
    public static final String ACTION_SEND_MULTIPLE = "android.intent.action.SEND_MULTIPLE";
    @SystemApi
    @Deprecated
    public static final String ACTION_SERVICE_STATE = "android.intent.action.SERVICE_STATE";
    public static final String ACTION_SETTING_RESTORED = "android.os.action.SETTING_RESTORED";
    public static final String ACTION_SET_WALLPAPER = "android.intent.action.SET_WALLPAPER";
    public static final String ACTION_SHOW_APP_INFO = "android.intent.action.SHOW_APP_INFO";
    public static final String ACTION_SHOW_BRIGHTNESS_DIALOG = "com.android.intent.action.SHOW_BRIGHTNESS_DIALOG";
    public static final String ACTION_SHOW_KEYBOARD_SHORTCUTS = "com.android.intent.action.SHOW_KEYBOARD_SHORTCUTS";
    @SystemApi
    public static final String ACTION_SHOW_SUSPENDED_APP_DETAILS = "android.intent.action.SHOW_SUSPENDED_APP_DETAILS";
    public static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    @SystemApi
    @Deprecated
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    @SystemApi
    public static final String ACTION_SPLIT_CONFIGURATION_CHANGED = "android.intent.action.SPLIT_CONFIGURATION_CHANGED";
    public static final String ACTION_SYNC = "android.intent.action.SYNC";
    public static final String ACTION_SYSTEM_TUTORIAL = "android.intent.action.SYSTEM_TUTORIAL";
    public static final String ACTION_THERMAL_EVENT = "android.intent.action.THERMAL_EVENT";
    public static final String ACTION_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED";
    public static final String ACTION_TIME_CHANGED = "android.intent.action.TIME_SET";
    public static final String ACTION_TIME_TICK = "android.intent.action.TIME_TICK";
    public static final String ACTION_UID_REMOVED = "android.intent.action.UID_REMOVED";
    @Deprecated
    public static final String ACTION_UMS_CONNECTED = "android.intent.action.UMS_CONNECTED";
    @Deprecated
    public static final String ACTION_UMS_DISCONNECTED = "android.intent.action.UMS_DISCONNECTED";
    public static final String ACTION_UNINSTALL_PACKAGE = "android.intent.action.UNINSTALL_PACKAGE";
    @SystemApi
    public static final String ACTION_UPGRADE_SETUP = "android.intent.action.UPGRADE_SETUP";
    public static final String ACTION_USER_ADDED = "android.intent.action.USER_ADDED";
    public static final String ACTION_USER_BACKGROUND = "android.intent.action.USER_BACKGROUND";
    public static final String ACTION_USER_FOREGROUND = "android.intent.action.USER_FOREGROUND";
    public static final String ACTION_USER_INFO_CHANGED = "android.intent.action.USER_INFO_CHANGED";
    public static final String ACTION_USER_INITIALIZE = "android.intent.action.USER_INITIALIZE";
    public static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
    @SystemApi
    public static final String ACTION_USER_REMOVED = "android.intent.action.USER_REMOVED";
    public static final String ACTION_USER_STARTED = "android.intent.action.USER_STARTED";
    public static final String ACTION_USER_STARTING = "android.intent.action.USER_STARTING";
    public static final String ACTION_USER_STOPPED = "android.intent.action.USER_STOPPED";
    public static final String ACTION_USER_STOPPING = "android.intent.action.USER_STOPPING";
    public static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    public static final String ACTION_USER_UNLOCKED = "android.intent.action.USER_UNLOCKED";
    public static final String ACTION_VIEW = "android.intent.action.VIEW";
    @SystemApi
    public static final String ACTION_VOICE_ASSIST = "android.intent.action.VOICE_ASSIST";
    public static final String ACTION_VOICE_COMMAND = "android.intent.action.VOICE_COMMAND";
    @Deprecated
    public static final String ACTION_WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED";
    public static final String ACTION_WEB_SEARCH = "android.intent.action.WEB_SEARCH";
    private static final String ATTR_ACTION = "action";
    private static final String ATTR_CATEGORY = "category";
    private static final String ATTR_COMPONENT = "component";
    private static final String ATTR_DATA = "data";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_HWFLAGS = "hwFlags";
    private static final String ATTR_TYPE = "type";
    public static final String CATEGORY_ALTERNATIVE = "android.intent.category.ALTERNATIVE";
    public static final String CATEGORY_APP_BROWSER = "android.intent.category.APP_BROWSER";
    public static final String CATEGORY_APP_CALCULATOR = "android.intent.category.APP_CALCULATOR";
    public static final String CATEGORY_APP_CALENDAR = "android.intent.category.APP_CALENDAR";
    public static final String CATEGORY_APP_CONTACTS = "android.intent.category.APP_CONTACTS";
    public static final String CATEGORY_APP_EMAIL = "android.intent.category.APP_EMAIL";
    public static final String CATEGORY_APP_GALLERY = "android.intent.category.APP_GALLERY";
    public static final String CATEGORY_APP_MAPS = "android.intent.category.APP_MAPS";
    public static final String CATEGORY_APP_MARKET = "android.intent.category.APP_MARKET";
    public static final String CATEGORY_APP_MESSAGING = "android.intent.category.APP_MESSAGING";
    public static final String CATEGORY_APP_MUSIC = "android.intent.category.APP_MUSIC";
    public static final String CATEGORY_BROWSABLE = "android.intent.category.BROWSABLE";
    public static final String CATEGORY_CAR_DOCK = "android.intent.category.CAR_DOCK";
    public static final String CATEGORY_CAR_LAUNCHER = "android.intent.category.CAR_LAUNCHER";
    public static final String CATEGORY_CAR_MODE = "android.intent.category.CAR_MODE";
    public static final String CATEGORY_DEFAULT = "android.intent.category.DEFAULT";
    public static final String CATEGORY_DESK_DOCK = "android.intent.category.DESK_DOCK";
    public static final String CATEGORY_DEVELOPMENT_PREFERENCE = "android.intent.category.DEVELOPMENT_PREFERENCE";
    public static final String CATEGORY_EMBED = "android.intent.category.EMBED";
    public static final String CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST = "android.intent.category.FRAMEWORK_INSTRUMENTATION_TEST";
    public static final String CATEGORY_HE_DESK_DOCK = "android.intent.category.HE_DESK_DOCK";
    public static final String CATEGORY_HOME = "android.intent.category.HOME";
    public static final String CATEGORY_HOME_MAIN = "android.intent.category.HOME_MAIN";
    public static final String CATEGORY_INFO = "android.intent.category.INFO";
    public static final String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";
    public static final String CATEGORY_LAUNCHER_APP = "android.intent.category.LAUNCHER_APP";
    public static final String CATEGORY_LEANBACK_LAUNCHER = "android.intent.category.LEANBACK_LAUNCHER";
    @SystemApi
    public static final String CATEGORY_LEANBACK_SETTINGS = "android.intent.category.LEANBACK_SETTINGS";
    public static final String CATEGORY_LE_DESK_DOCK = "android.intent.category.LE_DESK_DOCK";
    public static final String CATEGORY_MONKEY = "android.intent.category.MONKEY";
    public static final String CATEGORY_OPENABLE = "android.intent.category.OPENABLE";
    public static final String CATEGORY_PREFERENCE = "android.intent.category.PREFERENCE";
    public static final String CATEGORY_SAMPLE_CODE = "android.intent.category.SAMPLE_CODE";
    public static final String CATEGORY_SELECTED_ALTERNATIVE = "android.intent.category.SELECTED_ALTERNATIVE";
    public static final String CATEGORY_SETUP_WIZARD = "android.intent.category.SETUP_WIZARD";
    public static final String CATEGORY_TAB = "android.intent.category.TAB";
    public static final String CATEGORY_TEST = "android.intent.category.TEST";
    public static final String CATEGORY_TYPED_OPENABLE = "android.intent.category.TYPED_OPENABLE";
    public static final String CATEGORY_UNIT_TEST = "android.intent.category.UNIT_TEST";
    public static final String CATEGORY_VOICE = "android.intent.category.VOICE";
    public static final String CATEGORY_VR_HOME = "android.intent.category.VR_HOME";
    private static final int COPY_MODE_ALL = 0;
    private static final int COPY_MODE_FILTER = 1;
    private static final int COPY_MODE_HISTORY = 2;
    public static final Parcelable.Creator<Intent> CREATOR = new Parcelable.Creator<Intent>() {
        public Intent createFromParcel(Parcel in) {
            return new Intent(in);
        }

        public Intent[] newArray(int size) {
            return new Intent[size];
        }
    };
    public static final String EXTRA_ALARM_COUNT = "android.intent.extra.ALARM_COUNT";
    public static final String EXTRA_ALLOW_MULTIPLE = "android.intent.extra.ALLOW_MULTIPLE";
    @Deprecated
    public static final String EXTRA_ALLOW_REPLACE = "android.intent.extra.ALLOW_REPLACE";
    public static final String EXTRA_ALTERNATE_INTENTS = "android.intent.extra.ALTERNATE_INTENTS";
    public static final String EXTRA_ASSIST_CONTEXT = "android.intent.extra.ASSIST_CONTEXT";
    public static final String EXTRA_ASSIST_INPUT_DEVICE_ID = "android.intent.extra.ASSIST_INPUT_DEVICE_ID";
    public static final String EXTRA_ASSIST_INPUT_HINT_KEYBOARD = "android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD";
    public static final String EXTRA_ASSIST_PACKAGE = "android.intent.extra.ASSIST_PACKAGE";
    public static final String EXTRA_ASSIST_UID = "android.intent.extra.ASSIST_UID";
    public static final String EXTRA_AUTO_LAUNCH_SINGLE_CHOICE = "android.intent.extra.AUTO_LAUNCH_SINGLE_CHOICE";
    public static final String EXTRA_BCC = "android.intent.extra.BCC";
    public static final String EXTRA_BUG_REPORT = "android.intent.extra.BUG_REPORT";
    @SystemApi
    public static final String EXTRA_CALLING_PACKAGE = "android.intent.extra.CALLING_PACKAGE";
    public static final String EXTRA_CC = "android.intent.extra.CC";
    @SystemApi
    @Deprecated
    public static final String EXTRA_CDMA_DEFAULT_ROAMING_INDICATOR = "cdmaDefaultRoamingIndicator";
    @SystemApi
    @Deprecated
    public static final String EXTRA_CDMA_ROAMING_INDICATOR = "cdmaRoamingIndicator";
    @Deprecated
    public static final String EXTRA_CHANGED_COMPONENT_NAME = "android.intent.extra.changed_component_name";
    public static final String EXTRA_CHANGED_COMPONENT_NAME_LIST = "android.intent.extra.changed_component_name_list";
    public static final String EXTRA_CHANGED_PACKAGE_LIST = "android.intent.extra.changed_package_list";
    public static final String EXTRA_CHANGED_UID_LIST = "android.intent.extra.changed_uid_list";
    public static final String EXTRA_CHOOSER_REFINEMENT_INTENT_SENDER = "android.intent.extra.CHOOSER_REFINEMENT_INTENT_SENDER";
    public static final String EXTRA_CHOOSER_TARGETS = "android.intent.extra.CHOOSER_TARGETS";
    public static final String EXTRA_CHOSEN_COMPONENT = "android.intent.extra.CHOSEN_COMPONENT";
    public static final String EXTRA_CHOSEN_COMPONENT_INTENT_SENDER = "android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER";
    public static final String EXTRA_CLIENT_INTENT = "android.intent.extra.client_intent";
    public static final String EXTRA_CLIENT_LABEL = "android.intent.extra.client_label";
    public static final String EXTRA_COMPONENT_NAME = "android.intent.extra.COMPONENT_NAME";
    public static final String EXTRA_CONTENT_ANNOTATIONS = "android.intent.extra.CONTENT_ANNOTATIONS";
    @SystemApi
    @Deprecated
    public static final String EXTRA_CSS_INDICATOR = "cssIndicator";
    @SystemApi
    @Deprecated
    public static final String EXTRA_DATA_OPERATOR_ALPHA_LONG = "data-operator-alpha-long";
    @SystemApi
    @Deprecated
    public static final String EXTRA_DATA_OPERATOR_ALPHA_SHORT = "data-operator-alpha-short";
    @SystemApi
    @Deprecated
    public static final String EXTRA_DATA_OPERATOR_NUMERIC = "data-operator-numeric";
    @SystemApi
    @Deprecated
    public static final String EXTRA_DATA_RADIO_TECH = "dataRadioTechnology";
    @SystemApi
    @Deprecated
    public static final String EXTRA_DATA_REG_STATE = "dataRegState";
    public static final String EXTRA_DATA_REMOVED = "android.intent.extra.DATA_REMOVED";
    @SystemApi
    @Deprecated
    public static final String EXTRA_DATA_ROAMING_TYPE = "dataRoamingType";
    public static final String EXTRA_DOCK_STATE = "android.intent.extra.DOCK_STATE";
    public static final int EXTRA_DOCK_STATE_CAR = 2;
    public static final int EXTRA_DOCK_STATE_DESK = 1;
    public static final int EXTRA_DOCK_STATE_HE_DESK = 4;
    public static final int EXTRA_DOCK_STATE_LE_DESK = 3;
    public static final int EXTRA_DOCK_STATE_UNDOCKED = 0;
    public static final String EXTRA_DONT_KILL_APP = "android.intent.extra.DONT_KILL_APP";
    public static final String EXTRA_EMAIL = "android.intent.extra.EMAIL";
    @SystemApi
    @Deprecated
    public static final String EXTRA_EMERGENCY_ONLY = "emergencyOnly";
    @Deprecated
    public static final String EXTRA_EPHEMERAL_FAILURE = "android.intent.extra.EPHEMERAL_FAILURE";
    @Deprecated
    public static final String EXTRA_EPHEMERAL_HOSTNAME = "android.intent.extra.EPHEMERAL_HOSTNAME";
    @Deprecated
    public static final String EXTRA_EPHEMERAL_SUCCESS = "android.intent.extra.EPHEMERAL_SUCCESS";
    @Deprecated
    public static final String EXTRA_EPHEMERAL_TOKEN = "android.intent.extra.EPHEMERAL_TOKEN";
    public static final String EXTRA_EXCLUDE_COMPONENTS = "android.intent.extra.EXCLUDE_COMPONENTS";
    @SystemApi
    public static final String EXTRA_FORCE_FACTORY_RESET = "android.intent.extra.FORCE_FACTORY_RESET";
    @Deprecated
    public static final String EXTRA_FORCE_MASTER_CLEAR = "android.intent.extra.FORCE_MASTER_CLEAR";
    public static final String EXTRA_FROM_STORAGE = "android.intent.extra.FROM_STORAGE";
    public static final String EXTRA_HTML_TEXT = "android.intent.extra.HTML_TEXT";
    public static final String EXTRA_HW_SPLIT_REGION = "huawei.intent.extra.SPLIT_REGION";
    public static final String EXTRA_HW_SPLIT_SECSTAGE = "huawei.intent.extra.SPLIT_SECSTAGE";
    public static final String EXTRA_INDEX = "android.intent.extra.INDEX";
    public static final String EXTRA_INITIAL_INTENTS = "android.intent.extra.INITIAL_INTENTS";
    public static final String EXTRA_INSTALLER_PACKAGE_NAME = "android.intent.extra.INSTALLER_PACKAGE_NAME";
    public static final String EXTRA_INSTALL_RESULT = "android.intent.extra.INSTALL_RESULT";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_ACTION = "android.intent.extra.INSTANT_APP_ACTION";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_BUNDLES = "android.intent.extra.INSTANT_APP_BUNDLES";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_EXTRAS = "android.intent.extra.INSTANT_APP_EXTRAS";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_FAILURE = "android.intent.extra.INSTANT_APP_FAILURE";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_HOSTNAME = "android.intent.extra.INSTANT_APP_HOSTNAME";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_SUCCESS = "android.intent.extra.INSTANT_APP_SUCCESS";
    @SystemApi
    public static final String EXTRA_INSTANT_APP_TOKEN = "android.intent.extra.INSTANT_APP_TOKEN";
    public static final String EXTRA_INTENT = "android.intent.extra.INTENT";
    @SystemApi
    @Deprecated
    public static final String EXTRA_IS_DATA_ROAMING_FROM_REGISTRATION = "isDataRoamingFromRegistration";
    @SystemApi
    @Deprecated
    public static final String EXTRA_IS_USING_CARRIER_AGGREGATION = "isUsingCarrierAggregation";
    public static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
    public static final String EXTRA_KEY_EVENT = "android.intent.extra.KEY_EVENT";
    public static final String EXTRA_LAUNCHER_EXTRAS = "android.intent.extra.LAUNCHER_EXTRAS";
    public static final String EXTRA_LOCAL_ONLY = "android.intent.extra.LOCAL_ONLY";
    @SystemApi
    public static final String EXTRA_LONG_VERSION_CODE = "android.intent.extra.LONG_VERSION_CODE";
    @SystemApi
    @Deprecated
    public static final String EXTRA_LTE_EARFCN_RSRP_BOOST = "LteEarfcnRsrpBoost";
    @SystemApi
    @Deprecated
    public static final String EXTRA_MANUAL = "manual";
    public static final String EXTRA_MEDIA_RESOURCE_TYPE = "android.intent.extra.MEDIA_RESOURCE_TYPE";
    public static final int EXTRA_MEDIA_RESOURCE_TYPE_AUDIO_CODEC = 1;
    public static final int EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC = 0;
    public static final String EXTRA_MIME_TYPES = "android.intent.extra.MIME_TYPES";
    @SystemApi
    @Deprecated
    public static final String EXTRA_NETWORK_ID = "networkId";
    public static final String EXTRA_NOT_UNKNOWN_SOURCE = "android.intent.extra.NOT_UNKNOWN_SOURCE";
    @SystemApi
    @Deprecated
    public static final String EXTRA_OPERATOR_ALPHA_LONG = "operator-alpha-long";
    @SystemApi
    @Deprecated
    public static final String EXTRA_OPERATOR_ALPHA_SHORT = "operator-alpha-short";
    @SystemApi
    @Deprecated
    public static final String EXTRA_OPERATOR_NUMERIC = "operator-numeric";
    @SystemApi
    public static final String EXTRA_ORIGINATING_UID = "android.intent.extra.ORIGINATING_UID";
    public static final String EXTRA_ORIGINATING_URI = "android.intent.extra.ORIGINATING_URI";
    @SystemApi
    public static final String EXTRA_PACKAGES = "android.intent.extra.PACKAGES";
    public static final String EXTRA_PACKAGE_NAME = "android.intent.extra.PACKAGE_NAME";
    @SystemApi
    public static final String EXTRA_PERMISSION_NAME = "android.intent.extra.PERMISSION_NAME";
    public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";
    public static final String EXTRA_PROCESS_TEXT = "android.intent.extra.PROCESS_TEXT";
    public static final String EXTRA_PROCESS_TEXT_READONLY = "android.intent.extra.PROCESS_TEXT_READONLY";
    @Deprecated
    public static final String EXTRA_QUICK_VIEW_ADVANCED = "android.intent.extra.QUICK_VIEW_ADVANCED";
    public static final String EXTRA_QUICK_VIEW_FEATURES = "android.intent.extra.QUICK_VIEW_FEATURES";
    public static final String EXTRA_QUIET_MODE = "android.intent.extra.QUIET_MODE";
    @SystemApi
    public static final String EXTRA_REASON = "android.intent.extra.REASON";
    public static final String EXTRA_REFERRER = "android.intent.extra.REFERRER";
    public static final String EXTRA_REFERRER_NAME = "android.intent.extra.REFERRER_NAME";
    @SystemApi
    public static final String EXTRA_REMOTE_CALLBACK = "android.intent.extra.REMOTE_CALLBACK";
    public static final String EXTRA_REMOTE_INTENT_TOKEN = "android.intent.extra.remote_intent_token";
    public static final String EXTRA_REMOVED_FOR_ALL_USERS = "android.intent.extra.REMOVED_FOR_ALL_USERS";
    public static final String EXTRA_REPLACEMENT_EXTRAS = "android.intent.extra.REPLACEMENT_EXTRAS";
    public static final String EXTRA_REPLACING = "android.intent.extra.REPLACING";
    public static final String EXTRA_RESTRICTIONS_BUNDLE = "android.intent.extra.restrictions_bundle";
    public static final String EXTRA_RESTRICTIONS_INTENT = "android.intent.extra.restrictions_intent";
    public static final String EXTRA_RESTRICTIONS_LIST = "android.intent.extra.restrictions_list";
    @SystemApi
    public static final String EXTRA_RESULT_NEEDED = "android.intent.extra.RESULT_NEEDED";
    public static final String EXTRA_RESULT_RECEIVER = "android.intent.extra.RESULT_RECEIVER";
    public static final String EXTRA_RETURN_RESULT = "android.intent.extra.RETURN_RESULT";
    public static final String EXTRA_SETTING_NAME = "setting_name";
    public static final String EXTRA_SETTING_NEW_VALUE = "new_value";
    public static final String EXTRA_SETTING_PREVIOUS_VALUE = "previous_value";
    public static final String EXTRA_SETTING_RESTORED_FROM_SDK_INT = "restored_from_sdk_int";
    @Deprecated
    public static final String EXTRA_SHORTCUT_ICON = "android.intent.extra.shortcut.ICON";
    @Deprecated
    public static final String EXTRA_SHORTCUT_ICON_RESOURCE = "android.intent.extra.shortcut.ICON_RESOURCE";
    @Deprecated
    public static final String EXTRA_SHORTCUT_INTENT = "android.intent.extra.shortcut.INTENT";
    @Deprecated
    public static final String EXTRA_SHORTCUT_NAME = "android.intent.extra.shortcut.NAME";
    public static final String EXTRA_SHUTDOWN_USERSPACE_ONLY = "android.intent.extra.SHUTDOWN_USERSPACE_ONLY";
    public static final String EXTRA_SIM_ACTIVATION_RESPONSE = "android.intent.extra.SIM_ACTIVATION_RESPONSE";
    public static final String EXTRA_SPLIT_NAME = "android.intent.extra.SPLIT_NAME";
    public static final String EXTRA_STREAM = "android.intent.extra.STREAM";
    public static final String EXTRA_SUBJECT = "android.intent.extra.SUBJECT";
    public static final String EXTRA_SUSPENDED_PACKAGE_EXTRAS = "android.intent.extra.SUSPENDED_PACKAGE_EXTRAS";
    @SystemApi
    @Deprecated
    public static final String EXTRA_SYSTEM_ID = "systemId";
    public static final String EXTRA_TASK_ID = "android.intent.extra.TASK_ID";
    public static final String EXTRA_TEMPLATE = "android.intent.extra.TEMPLATE";
    public static final String EXTRA_TEXT = "android.intent.extra.TEXT";
    public static final String EXTRA_THERMAL_STATE = "android.intent.extra.THERMAL_STATE";
    public static final int EXTRA_THERMAL_STATE_EXCEEDED = 2;
    public static final int EXTRA_THERMAL_STATE_NORMAL = 0;
    public static final int EXTRA_THERMAL_STATE_WARNING = 1;
    public static final String EXTRA_TIME_PREF_24_HOUR_FORMAT = "android.intent.extra.TIME_PREF_24_HOUR_FORMAT";
    public static final int EXTRA_TIME_PREF_VALUE_USE_12_HOUR = 0;
    public static final int EXTRA_TIME_PREF_VALUE_USE_24_HOUR = 1;
    public static final int EXTRA_TIME_PREF_VALUE_USE_LOCALE_DEFAULT = 2;
    public static final String EXTRA_TITLE = "android.intent.extra.TITLE";
    public static final String EXTRA_UID = "android.intent.extra.UID";
    public static final String EXTRA_UNINSTALL_ALL_USERS = "android.intent.extra.UNINSTALL_ALL_USERS";
    @SystemApi
    public static final String EXTRA_UNKNOWN_INSTANT_APP = "android.intent.extra.UNKNOWN_INSTANT_APP";
    public static final String EXTRA_USER = "android.intent.extra.USER";
    public static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
    public static final String EXTRA_USER_ID = "android.intent.extra.USER_ID";
    public static final String EXTRA_USER_REQUESTED_SHUTDOWN = "android.intent.extra.USER_REQUESTED_SHUTDOWN";
    @SystemApi
    public static final String EXTRA_VERIFICATION_BUNDLE = "android.intent.extra.VERIFICATION_BUNDLE";
    @Deprecated
    public static final String EXTRA_VERSION_CODE = "android.intent.extra.VERSION_CODE";
    @SystemApi
    @Deprecated
    public static final String EXTRA_VOICE_RADIO_TECH = "radioTechnology";
    @SystemApi
    @Deprecated
    public static final String EXTRA_VOICE_REG_STATE = "voiceRegState";
    @SystemApi
    @Deprecated
    public static final String EXTRA_VOICE_ROAMING_TYPE = "voiceRoamingType";
    public static final String EXTRA_WIPE_ESIMS = "com.android.internal.intent.extra.WIPE_ESIMS";
    public static final String EXTRA_WIPE_EXTERNAL_STORAGE = "android.intent.extra.WIPE_EXTERNAL_STORAGE";
    public static final int FILL_IN_ACTION = 1;
    public static final int FILL_IN_CATEGORIES = 4;
    public static final int FILL_IN_CLIP_DATA = 128;
    public static final int FILL_IN_COMPONENT = 8;
    public static final int FILL_IN_DATA = 2;
    public static final int FILL_IN_PACKAGE = 16;
    public static final int FILL_IN_SELECTOR = 64;
    public static final int FILL_IN_SOURCE_BOUNDS = 32;
    public static final int FLAG_ACTIVITY_BROUGHT_TO_FRONT = 4194304;
    public static final int FLAG_ACTIVITY_CLEAR_TASK = 32768;
    public static final int FLAG_ACTIVITY_CLEAR_TOP = 67108864;
    @Deprecated
    public static final int FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET = 524288;
    public static final int FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS = 8388608;
    public static final int FLAG_ACTIVITY_FORWARD_RESULT = 33554432;
    public static final int FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY = 1048576;
    public static final int FLAG_ACTIVITY_LAUNCH_ADJACENT = 4096;
    public static final int FLAG_ACTIVITY_MATCH_EXTERNAL = 2048;
    public static final int FLAG_ACTIVITY_MULTIPLE_TASK = 134217728;
    public static final int FLAG_ACTIVITY_NEW_DOCUMENT = 524288;
    public static final int FLAG_ACTIVITY_NEW_TASK = 268435456;
    public static final int FLAG_ACTIVITY_NO_ANIMATION = 65536;
    public static final int FLAG_ACTIVITY_NO_HISTORY = 1073741824;
    public static final int FLAG_ACTIVITY_NO_USER_ACTION = 262144;
    public static final int FLAG_ACTIVITY_PREVIOUS_IS_TOP = 16777216;
    public static final int FLAG_ACTIVITY_REORDER_TO_FRONT = 131072;
    public static final int FLAG_ACTIVITY_RESET_TASK_IF_NEEDED = 2097152;
    public static final int FLAG_ACTIVITY_RETAIN_IN_RECENTS = 8192;
    public static final int FLAG_ACTIVITY_SINGLE_TOP = 536870912;
    public static final int FLAG_ACTIVITY_TASK_ON_HOME = 16384;
    public static final int FLAG_DEBUG_LOG_RESOLUTION = 8;
    public static final int FLAG_DEBUG_TRIAGED_MISSING = 256;
    public static final int FLAG_EXCLUDE_STOPPED_PACKAGES = 16;
    public static final int FLAG_FROM_BACKGROUND = 4;
    public static final int FLAG_GRANT_PERSISTABLE_URI_PERMISSION = 64;
    public static final int FLAG_GRANT_PREFIX_URI_PERMISSION = 128;
    public static final int FLAG_GRANT_READ_URI_PERMISSION = 1;
    public static final int FLAG_GRANT_WRITE_URI_PERMISSION = 2;
    public static final int FLAG_HW_ACTIVITY_CHECK_DUAL_CHOOSER = 1024;
    public static final int FLAG_HW_ACTIVITY_EXCLUDE_TRIM_TASK = 16384;
    @Deprecated
    public static final int FLAG_HW_ACTIVITY_FOR_CLONE_PROCESS = 1;
    public static final int FLAG_HW_ACTIVITY_FOR_DUAL_CHOOSER = 2;
    public static final int FLAG_HW_ACTIVITY_STARTING_FROM_FINISHING = 256;
    public static final int FLAG_HW_ACTIVITY_START_FROM_HOME = 4096;
    public static final int FLAG_HW_ALARMDELIVER_INTENT = 2048;
    public static final int FLAG_HW_ALARM_INTENT = 256;
    public static final int FLAG_HW_CANCEL_SPLIT = 8;
    public static final int FLAG_HW_CLEAR_SNAPSHOT = 8192;
    public static final int FLAG_HW_HOME_INTENT_FROM_SYSTEM = 512;
    public static final int FLAG_HW_INTENT_TO_STRING_SAFELY = 16;
    public static final int FLAG_HW_JOB_INTENT = 32;
    public static final int FLAG_HW_LOCATION_INTENT = 512;
    public static final int FLAG_HW_SPLIT_ACTIVITY = 4;
    public static final int FLAG_HW_SYNC_INTENT = 64;
    public static final int FLAG_HW_SYNC_USER = 128;
    public static final int FLAG_IGNORE_EPHEMERAL = 512;
    public static final int FLAG_INCLUDE_STOPPED_PACKAGES = 32;
    public static final int FLAG_RECEIVER_BOOT_UPGRADE = 33554432;
    public static final int FLAG_RECEIVER_EXCLUDE_BACKGROUND = 8388608;
    public static final int FLAG_RECEIVER_FOREGROUND = 268435456;
    public static final int FLAG_RECEIVER_FROM_SHELL = 4194304;
    public static final int FLAG_RECEIVER_INCLUDE_BACKGROUND = 16777216;
    public static final int FLAG_RECEIVER_KEY_QUEUE = 4096;
    public static final int FLAG_RECEIVER_NO_ABORT = 134217728;
    public static final int FLAG_RECEIVER_REGISTERED_ONLY = 1073741824;
    public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 67108864;
    public static final int FLAG_RECEIVER_REPLACE_PENDING = 536870912;
    public static final int FLAG_RECEIVER_THIRDAPP_QUEUE = 8192;
    public static final int FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS = 2097152;
    public static final int IMMUTABLE_FLAGS = 195;
    public static final String METADATA_DOCK_HOME = "android.dock_home";
    public static final String METADATA_SETUP_VERSION = "android.SETUP_VERSION";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_EXTRA = "extra";
    public static final int URI_ALLOW_UNSAFE = 4;
    public static final int URI_ANDROID_APP_SCHEME = 2;
    public static final int URI_INTENT_SCHEME = 1;
    private int callingUid;
    private int hwFlags;
    private String mAction;
    private ArraySet<String> mCategories;
    private ClipData mClipData;
    private ComponentName mComponent;
    private int mContentUserHint;
    private Uri mData;
    private Bundle mExtras;
    private int mFlags;
    private String mLaunchToken;
    private String mPackage;
    private Intent mSelector;
    private Rect mSourceBounds;
    private String mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AccessUriMode {
    }

    public interface CommandOptionHandler {
        boolean handleOption(String str, ShellCommand shellCommand);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CopyMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FillInFlags {
    }

    public static final class FilterComparison {
        private final int mHashCode;
        private final Intent mIntent;

        public FilterComparison(Intent intent) {
            this.mIntent = intent;
            this.mHashCode = intent.filterHashCode();
        }

        public Intent getIntent() {
            return this.mIntent;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof FilterComparison)) {
                return false;
            }
            return this.mIntent.filterEquals(((FilterComparison) obj).mIntent);
        }

        public int hashCode() {
            return this.mHashCode;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface GrantUriMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MutableFlags {
    }

    public static class ShortcutIconResource implements Parcelable {
        public static final Parcelable.Creator<ShortcutIconResource> CREATOR = new Parcelable.Creator<ShortcutIconResource>() {
            public ShortcutIconResource createFromParcel(Parcel source) {
                ShortcutIconResource icon = new ShortcutIconResource();
                icon.packageName = source.readString();
                icon.resourceName = source.readString();
                return icon;
            }

            public ShortcutIconResource[] newArray(int size) {
                return new ShortcutIconResource[size];
            }
        };
        public String packageName;
        public String resourceName;

        public static ShortcutIconResource fromContext(Context context, int resourceId) {
            ShortcutIconResource icon = new ShortcutIconResource();
            icon.packageName = context.getPackageName();
            icon.resourceName = context.getResources().getResourceName(resourceId);
            return icon;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.packageName);
            dest.writeString(this.resourceName);
        }

        public String toString() {
            return this.resourceName;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UriFlags {
    }

    public static Intent createChooser(Intent target, CharSequence title) {
        return createChooser(target, title, null);
    }

    public static Intent createChooser(Intent target, CharSequence title, IntentSender sender) {
        Intent intent = HwFrameworkFactory.getHuaweiChooserIntent();
        if (intent == null || title == null) {
            intent = new Intent(ACTION_CHOOSER);
        }
        intent.putExtra(EXTRA_INTENT, (Parcelable) target);
        if (title != null) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        if (sender != null) {
            intent.putExtra(EXTRA_CHOSEN_COMPONENT_INTENT_SENDER, (Parcelable) sender);
        }
        int permFlags = target.getFlags() & 195;
        if (permFlags != 0) {
            ClipData targetClipData = target.getClipData();
            if (targetClipData == null && target.getData() != null) {
                targetClipData = new ClipData(null, target.getType() != null ? new String[]{target.getType()} : new String[0], new ClipData.Item(target.getData()));
            }
            if (targetClipData != null) {
                intent.setClipData(targetClipData);
                intent.addFlags(permFlags);
            }
        }
        return intent;
    }

    public static boolean isAccessUriMode(int modeFlags) {
        return (modeFlags & 3) != 0;
    }

    public Intent() {
        this.mContentUserHint = -2;
    }

    public Intent(Intent o) {
        this(o, 0);
    }

    private Intent(Intent o, int copyMode) {
        this.mContentUserHint = -2;
        this.mAction = o.mAction;
        this.mData = o.mData;
        this.mType = o.mType;
        this.mPackage = o.mPackage;
        this.mComponent = o.mComponent;
        if (o.mCategories != null) {
            this.mCategories = new ArraySet<>(o.mCategories);
        }
        this.hwFlags = o.hwFlags;
        this.callingUid = o.callingUid;
        if (copyMode != 1) {
            this.mFlags = o.mFlags;
            this.mContentUserHint = o.mContentUserHint;
            this.mLaunchToken = o.mLaunchToken;
            if (o.mSourceBounds != null) {
                this.mSourceBounds = new Rect(o.mSourceBounds);
            }
            if (o.mSelector != null) {
                this.mSelector = new Intent(o.mSelector);
            }
            if (copyMode != 2) {
                if (o.mExtras != null) {
                    this.mExtras = new Bundle(o.mExtras);
                }
                if (o.mClipData != null) {
                    this.mClipData = new ClipData(o.mClipData);
                }
            } else if (o.mExtras != null && !o.mExtras.maybeIsEmpty()) {
                this.mExtras = Bundle.STRIPPED;
            }
        }
    }

    public Object clone() {
        return new Intent(this);
    }

    public Intent cloneFilter() {
        return new Intent(this, 1);
    }

    public Intent(String action) {
        this.mContentUserHint = -2;
        setAction(action);
    }

    public Intent(String action, Uri uri) {
        this.mContentUserHint = -2;
        setAction(action);
        this.mData = uri;
    }

    public Intent(Context packageContext, Class<?> cls) {
        this.mContentUserHint = -2;
        this.mComponent = new ComponentName(packageContext, cls);
    }

    public Intent(String action, Uri uri, Context packageContext, Class<?> cls) {
        this.mContentUserHint = -2;
        setAction(action);
        this.mData = uri;
        this.mComponent = new ComponentName(packageContext, cls);
    }

    public static Intent makeMainActivity(ComponentName mainActivity) {
        Intent intent = new Intent(ACTION_MAIN);
        intent.setComponent(mainActivity);
        intent.addCategory(CATEGORY_LAUNCHER);
        return intent;
    }

    public static Intent makeMainSelectorActivity(String selectorAction, String selectorCategory) {
        Intent intent = new Intent(ACTION_MAIN);
        intent.addCategory(CATEGORY_LAUNCHER);
        Intent selector = new Intent();
        selector.setAction(selectorAction);
        selector.addCategory(selectorCategory);
        intent.setSelector(selector);
        return intent;
    }

    public static Intent makeRestartActivityTask(ComponentName mainActivity) {
        Intent intent = makeMainActivity(mainActivity);
        intent.addFlags(268468224);
        return intent;
    }

    @Deprecated
    public static Intent getIntent(String uri) throws URISyntaxException {
        return parseUri(uri, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:170:0x0332 A[SYNTHETIC, Splitter:B:170:0x0332] */
    public static Intent parseUri(String uri, int flags) throws URISyntaxException {
        String data;
        String data2;
        String data3;
        String data4;
        String scheme;
        String str = uri;
        int i = 0;
        try {
            boolean androidApp = str.startsWith("android-app:");
            if ((flags & 3) == 0 || str.startsWith("intent:") || androidApp) {
                i = str.lastIndexOf("#");
                if (i == -1) {
                    if (!androidApp) {
                        return new Intent("android.intent.action.VIEW", Uri.parse(uri));
                    }
                } else if (!str.startsWith("#Intent;", i)) {
                    if (!androidApp) {
                        return getIntentOld(uri, flags);
                    }
                    i = -1;
                }
                Intent intent = new Intent("android.intent.action.VIEW");
                Intent baseIntent = intent;
                boolean explicitAction = false;
                boolean inSelector = false;
                String scheme2 = null;
                if (i >= 0) {
                    data = str.substring(0, i);
                    i += 8;
                } else {
                    data = str;
                }
                while (i >= 0 && !str.startsWith(TtmlUtils.ATTR_END, i)) {
                    int eq = str.indexOf(61, i);
                    if (eq < 0) {
                        eq = i - 1;
                    }
                    int semi = str.indexOf(59, i);
                    String value = eq < semi ? Uri.decode(str.substring(eq + 1, semi)) : "";
                    if (str.startsWith("action=", i)) {
                        intent.setAction(value);
                        if (!inSelector) {
                            explicitAction = true;
                        }
                        scheme = scheme2;
                        data4 = data;
                        scheme2 = scheme;
                        i = semi + 1;
                        data = data4;
                    } else {
                        if (str.startsWith("category=", i)) {
                            intent.addCategory(value);
                        } else if (str.startsWith("type=", i)) {
                            intent.mType = value;
                        } else if (str.startsWith("launchFlags=", i)) {
                            intent.mFlags = Integer.decode(value).intValue();
                            if ((flags & 4) == 0) {
                                intent.mFlags &= -196;
                            }
                        } else if (str.startsWith("launchHwFlags=", i)) {
                            intent.hwFlags = Integer.decode(value).intValue();
                        } else if (str.startsWith("package=", i)) {
                            intent.mPackage = value;
                        } else if (str.startsWith("component=", i)) {
                            intent.mComponent = ComponentName.unflattenFromString(value);
                        } else if (str.startsWith("scheme=", i)) {
                            if (inSelector) {
                                intent.mData = Uri.parse(value + ":");
                            } else {
                                scheme2 = value;
                            }
                        } else if (str.startsWith("sourceBounds=", i)) {
                            intent.mSourceBounds = Rect.unflattenFromString(value);
                        } else if (semi != i + 3 || !str.startsWith("SEL", i)) {
                            String key = Uri.decode(str.substring(i + 2, eq));
                            if (intent.mExtras == null) {
                                intent.mExtras = new Bundle();
                            }
                            Bundle b = intent.mExtras;
                            if (str.startsWith("S.", i)) {
                                b.putString(key, value);
                            } else if (str.startsWith("B.", i)) {
                                b.putBoolean(key, Boolean.parseBoolean(value));
                            } else if (str.startsWith("b.", i)) {
                                b.putByte(key, Byte.parseByte(value));
                            } else if (str.startsWith("c.", i)) {
                                b.putChar(key, value.charAt(0));
                            } else {
                                if (str.startsWith("d.", i)) {
                                    try {
                                        b.putDouble(key, Double.parseDouble(value));
                                        scheme = scheme2;
                                        data4 = data;
                                        str = uri;
                                    } catch (IndexOutOfBoundsException e) {
                                        str = uri;
                                        throw new URISyntaxException(str, "illegal Intent URI format", i);
                                    }
                                } else {
                                    str = uri;
                                    if (str.startsWith("f.", i)) {
                                        b.putFloat(key, Float.parseFloat(value));
                                    } else if (str.startsWith("i.", i)) {
                                        b.putInt(key, Integer.parseInt(value));
                                    } else if (str.startsWith("l.", i)) {
                                        scheme = scheme2;
                                        data4 = data;
                                        b.putLong(key, Long.parseLong(value));
                                    } else {
                                        scheme = scheme2;
                                        data4 = data;
                                        if (str.startsWith("s.", i)) {
                                            b.putShort(key, Short.parseShort(value));
                                        } else {
                                            throw new URISyntaxException(str, "unknown EXTRA type", i);
                                        }
                                    }
                                }
                                scheme2 = scheme;
                                i = semi + 1;
                                data = data4;
                            }
                        } else {
                            intent = new Intent();
                            inSelector = true;
                        }
                        scheme = scheme2;
                        data4 = data;
                        scheme2 = scheme;
                        i = semi + 1;
                        data = data4;
                    }
                    data4 = data;
                    i = semi + 1;
                    data = data4;
                }
                String scheme3 = scheme2;
                String data5 = data;
                if (inSelector) {
                    if (baseIntent.mPackage == null) {
                        baseIntent.setSelector(intent);
                    }
                    intent = baseIntent;
                }
                if (data5 != null) {
                    String data6 = data5;
                    if (data6.startsWith("intent:")) {
                        String data7 = data6.substring(7);
                        if (scheme3 != null) {
                            data3 = scheme3 + ':' + data7;
                        } else {
                            data2 = data7;
                            if (data2.length() > 0) {
                                intent.mData = Uri.parse(data2);
                            }
                            String str2 = data2;
                        }
                    } else {
                        String scheme4 = scheme3;
                        if (!data6.startsWith("android-app:")) {
                            data2 = data6;
                            if (data2.length() > 0) {
                            }
                            String str22 = data2;
                        } else if (data6.charAt(12) == '/' && data6.charAt(13) == '/') {
                            int end = data6.indexOf(47, 14);
                            if (end < 0) {
                                intent.mPackage = data6.substring(14);
                                if (!explicitAction) {
                                    intent.setAction(ACTION_MAIN);
                                }
                                data3 = "";
                            } else {
                                String authority = null;
                                intent.mPackage = data6.substring(14, end);
                                if (end + 1 < data6.length()) {
                                    int indexOf = data6.indexOf(47, end + 1);
                                    int newEnd = indexOf;
                                    if (indexOf >= 0) {
                                        scheme4 = data6.substring(end + 1, newEnd);
                                        end = newEnd;
                                        if (end < data6.length()) {
                                            int indexOf2 = data6.indexOf(47, end + 1);
                                            int newEnd2 = indexOf2;
                                            if (indexOf2 >= 0) {
                                                authority = data6.substring(end + 1, newEnd2);
                                                end = newEnd2;
                                            }
                                        }
                                    } else {
                                        scheme4 = data6.substring(end + 1);
                                    }
                                }
                                if (scheme4 == null) {
                                    if (!explicitAction) {
                                        intent.setAction(ACTION_MAIN);
                                    }
                                    data3 = "";
                                } else if (authority == null) {
                                    data3 = scheme4 + ":";
                                } else {
                                    data3 = scheme4 + "://" + authority + data6.substring(end);
                                }
                            }
                        } else {
                            data3 = "";
                        }
                    }
                    data2 = data3;
                    if (data2.length() > 0) {
                    }
                    String str222 = data2;
                } else {
                    String str3 = data5;
                    String str4 = scheme3;
                }
                return intent;
            }
            Intent intent2 = new Intent("android.intent.action.VIEW");
            intent2.setData(Uri.parse(uri));
            return intent2;
        } catch (IllegalArgumentException e2) {
            throw new URISyntaxException(str, e2.getMessage());
        } catch (IllegalArgumentException e3) {
            throw new URISyntaxException(str, e3.getMessage());
        } catch (IndexOutOfBoundsException e4) {
            throw new URISyntaxException(str, "illegal Intent URI format", i);
        }
    }

    public static Intent getIntentOld(String uri) throws URISyntaxException {
        return getIntentOld(uri, 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01e3, code lost:
        throw new java.net.URISyntaxException(r1, "EXTRA missing '='", r0);
     */
    private static Intent getIntentOld(String uri, int flags) throws URISyntaxException {
        Intent intent;
        int i;
        String str = uri;
        int i2 = str.lastIndexOf(35);
        if (i2 >= 0) {
            String action = null;
            int intentFragmentStart = i2;
            boolean isIntentFragment = false;
            int i3 = i2 + 1;
            if (str.regionMatches(i3, "action(", 0, 7)) {
                isIntentFragment = true;
                int i4 = i3 + 7;
                int j = str.indexOf(41, i4);
                action = str.substring(i4, j);
                i3 = j + 1;
            }
            intent = new Intent(action);
            int i5 = 33;
            if (str.regionMatches(i3, "categories(", 0, 11)) {
                isIntentFragment = true;
                int i6 = i3 + 11;
                int j2 = str.indexOf(41, i6);
                while (i6 < j2) {
                    int sep = str.indexOf(33, i6);
                    if (sep < 0 || sep > j2) {
                        sep = j2;
                    }
                    if (i6 < sep) {
                        intent.addCategory(str.substring(i6, sep));
                    }
                    i6 = sep + 1;
                }
                i3 = j2 + 1;
            }
            if (str.regionMatches(i3, "type(", 0, 5)) {
                isIntentFragment = true;
                int i7 = i3 + 5;
                int j3 = str.indexOf(41, i7);
                intent.mType = str.substring(i7, j3);
                i3 = j3 + 1;
            }
            if (str.regionMatches(i3, "launchFlags(", 0, 12)) {
                isIntentFragment = true;
                int i8 = i3 + 12;
                int j4 = str.indexOf(41, i8);
                intent.mFlags = Integer.decode(str.substring(i8, j4)).intValue();
                if ((flags & 4) == 0) {
                    intent.mFlags &= -196;
                }
                i3 = j4 + 1;
            }
            if (str.regionMatches(i3, "component(", 0, 10)) {
                isIntentFragment = true;
                int i9 = i3 + 10;
                int j5 = str.indexOf(41, i9);
                int sep2 = str.indexOf(33, i9);
                if (sep2 >= 0 && sep2 < j5) {
                    intent.mComponent = new ComponentName(str.substring(i9, sep2), str.substring(sep2 + 1, j5));
                }
                i3 = j5 + 1;
            }
            if (str.regionMatches(i3, "extras(", 0, 7)) {
                isIntentFragment = true;
                int i10 = i3 + 7;
                int closeParen = str.indexOf(41, i10);
                int i11 = -1;
                if (closeParen != -1) {
                    while (true) {
                        if (i10 < closeParen) {
                            int j6 = str.indexOf(61, i10);
                            if (j6 > i10 + 1 && i10 < closeParen) {
                                char type = str.charAt(i10);
                                String key = str.substring(i10 + 1, j6);
                                int i12 = j6 + 1;
                                int j7 = str.indexOf(i5, i12);
                                if (j7 == i11 || j7 >= closeParen) {
                                    j7 = closeParen;
                                }
                                if (i12 < j7) {
                                    String value = str.substring(i12, j7);
                                    int i13 = j7;
                                    if (intent.mExtras == null) {
                                        intent.mExtras = new Bundle();
                                    }
                                    if (type == 'B') {
                                        i = i13;
                                        intent.mExtras.putBoolean(key, Boolean.parseBoolean(value));
                                    } else if (type == 'S') {
                                        i = i13;
                                        intent.mExtras.putString(key, Uri.decode(value));
                                    } else if (type == 'f') {
                                        i = i13;
                                        intent.mExtras.putFloat(key, Float.parseFloat(value));
                                    } else if (type == 'i') {
                                        i = i13;
                                        intent.mExtras.putInt(key, Integer.parseInt(value));
                                    } else if (type == 'l') {
                                        i = i13;
                                        intent.mExtras.putLong(key, Long.parseLong(value));
                                    } else if (type != 's') {
                                        switch (type) {
                                            case 'b':
                                                i = i13;
                                                intent.mExtras.putByte(key, Byte.parseByte(value));
                                                break;
                                            case 'c':
                                                i = i13;
                                                intent.mExtras.putChar(key, Uri.decode(value).charAt(0));
                                                break;
                                            case 'd':
                                                i = i13;
                                                try {
                                                    intent.mExtras.putDouble(key, Double.parseDouble(value));
                                                    break;
                                                } catch (NumberFormatException e) {
                                                    throw new URISyntaxException(str, "EXTRA value can't be parsed", i);
                                                }
                                            default:
                                                try {
                                                    int i14 = i13;
                                                    try {
                                                        throw new URISyntaxException(str, "EXTRA has unknown type", i14);
                                                    } catch (NumberFormatException e2) {
                                                        i = i14;
                                                        throw new URISyntaxException(str, "EXTRA value can't be parsed", i);
                                                    }
                                                } catch (NumberFormatException e3) {
                                                    i = i13;
                                                    throw new URISyntaxException(str, "EXTRA value can't be parsed", i);
                                                }
                                        }
                                    } else {
                                        i = i13;
                                        intent.mExtras.putShort(key, Short.parseShort(value));
                                    }
                                    char ch = str.charAt(i);
                                    if (ch == ')') {
                                        int i15 = i;
                                    } else {
                                        i5 = 33;
                                        if (ch == '!') {
                                            i10 = i + 1;
                                            i11 = -1;
                                        } else {
                                            throw new URISyntaxException(str, "EXTRA missing '!'", i);
                                        }
                                    }
                                } else {
                                    throw new URISyntaxException(str, "EXTRA missing '!'", i12);
                                }
                            }
                        }
                    }
                } else {
                    throw new URISyntaxException(str, "EXTRA missing trailing ')'", i10);
                }
            }
            if (isIntentFragment) {
                intent.mData = Uri.parse(str.substring(0, intentFragmentStart));
            } else {
                intent.mData = Uri.parse(uri);
            }
            if (intent.mAction == null) {
                intent.mAction = "android.intent.action.VIEW";
            }
        } else {
            intent = new Intent("android.intent.action.VIEW", Uri.parse(uri));
        }
        return intent;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01c3, code lost:
        if (r8.equals("--ei") != false) goto L_0x0291;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x036f, code lost:
        r5 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x049f, code lost:
        r5 = r18;
        r6 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x0600, code lost:
        r0 = r7;
     */
    public static Intent parseCommandArgs(ShellCommand cmd, CommandOptionHandler optionHandler) throws URISyntaxException {
        String opt;
        boolean hasIntentInfo;
        CommandOptionHandler commandOptionHandler = optionHandler;
        Intent intent = new Intent();
        Intent baseIntent = intent;
        Uri data = null;
        boolean hasIntentInfo2 = false;
        Intent intent2 = intent;
        String type = null;
        while (true) {
            String type2 = type;
            String type3 = cmd.getNextOption();
            opt = type3;
            char c = 7;
            if (type3 != null) {
                int hashCode = opt.hashCode();
                switch (hashCode) {
                    case 1494:
                        if (opt.equals("-c")) {
                            c = 3;
                            break;
                        }
                    case 1495:
                        if (opt.equals("-d")) {
                            c = 1;
                            break;
                        }
                    case 1496:
                        if (opt.equals("-e")) {
                            c = 4;
                            break;
                        }
                    case 1497:
                        if (opt.equals("-f")) {
                            c = 23;
                            break;
                        }
                    default:
                        switch (hashCode) {
                            case -2147394086:
                                if (opt.equals("--grant-prefix-uri-permission")) {
                                    c = 27;
                                    break;
                                }
                            case -2118172637:
                                if (opt.equals("--activity-task-on-home")) {
                                    c = '-';
                                    break;
                                }
                            case -1630559130:
                                if (opt.equals("--activity-no-history")) {
                                    c = '&';
                                    break;
                                }
                            case -1252939549:
                                if (opt.equals("--activity-clear-task")) {
                                    c = ',';
                                    break;
                                }
                            case -1069446353:
                                if (opt.equals("--debug-log-resolution")) {
                                    c = 30;
                                    break;
                                }
                            case -848214457:
                                if (opt.equals("--activity-reorder-to-front")) {
                                    c = ')';
                                    break;
                                }
                            case -833172539:
                                if (opt.equals("--activity-brought-to-front")) {
                                    c = 31;
                                    break;
                                }
                            case -792169302:
                                if (opt.equals("--activity-previous-is-top")) {
                                    c = '(';
                                    break;
                                }
                            case -780160399:
                                if (opt.equals("--receiver-include-background")) {
                                    c = '3';
                                    break;
                                }
                            case 1492:
                                if (opt.equals("-a")) {
                                    c = 0;
                                    break;
                                }
                            case 1505:
                                if (opt.equals("-n")) {
                                    c = 21;
                                    break;
                                }
                            case 1507:
                                if (opt.equals("-p")) {
                                    c = 22;
                                    break;
                                }
                            case 1511:
                                if (opt.equals("-t")) {
                                    c = 2;
                                    break;
                                }
                            case 1387073:
                                if (opt.equals("--ef")) {
                                    c = 15;
                                    break;
                                }
                            case 1387076:
                                break;
                            case 1387079:
                                if (opt.equals("--el")) {
                                    c = 12;
                                    break;
                                }
                            case 1387086:
                                if (opt.equals("--es")) {
                                    c = 5;
                                    break;
                                }
                            case 1387088:
                                if (opt.equals("--eu")) {
                                    c = 8;
                                    break;
                                }
                            case 1387093:
                                if (opt.equals("--ez")) {
                                    c = 20;
                                    break;
                                }
                            case 42999280:
                                if (opt.equals("--ecn")) {
                                    c = 9;
                                    break;
                                }
                            case 42999360:
                                if (opt.equals("--efa")) {
                                    c = 16;
                                    break;
                                }
                            case 42999453:
                                if (opt.equals("--eia")) {
                                    c = 10;
                                    break;
                                }
                            case 42999546:
                                if (opt.equals("--ela")) {
                                    c = 13;
                                    break;
                                }
                            case 42999763:
                                if (opt.equals("--esa")) {
                                    c = 18;
                                    break;
                                }
                            case 42999776:
                                if (opt.equals("--esn")) {
                                    c = 6;
                                    break;
                                }
                            case 69120454:
                                if (opt.equals("--activity-exclude-from-recents")) {
                                    c = '\"';
                                    break;
                                }
                            case 88747734:
                                if (opt.equals("--activity-no-animation")) {
                                    c = '%';
                                    break;
                                }
                            case 190913209:
                                if (opt.equals("--activity-reset-task-if-needed")) {
                                    c = '*';
                                    break;
                                }
                            case 236677687:
                                if (opt.equals("--activity-clear-top")) {
                                    c = ' ';
                                    break;
                                }
                            case 429439306:
                                if (opt.equals("--activity-no-user-action")) {
                                    c = '\'';
                                    break;
                                }
                            case 436286937:
                                if (opt.equals("--receiver-registered-only")) {
                                    c = '/';
                                    break;
                                }
                            case 438531630:
                                if (opt.equals("--activity-single-top")) {
                                    c = '+';
                                    break;
                                }
                            case 527014976:
                                if (opt.equals("--grant-persistable-uri-permission")) {
                                    c = 26;
                                    break;
                                }
                            case 580418080:
                                if (opt.equals("--exclude-stopped-packages")) {
                                    c = 28;
                                    break;
                                }
                            case 749648146:
                                if (opt.equals("--include-stopped-packages")) {
                                    c = 29;
                                    break;
                                }
                            case 775126336:
                                if (opt.equals("--receiver-replace-pending")) {
                                    c = '0';
                                    break;
                                }
                            case 1110195121:
                                if (opt.equals("--activity-match-external")) {
                                    c = '.';
                                    break;
                                }
                            case 1207327103:
                                if (opt.equals("--selector")) {
                                    c = '4';
                                    break;
                                }
                            case 1332980268:
                                if (opt.equals("--efal")) {
                                    c = 17;
                                    break;
                                }
                            case 1332983151:
                                if (opt.equals("--eial")) {
                                    c = 11;
                                    break;
                                }
                            case 1332986034:
                                if (opt.equals("--elal")) {
                                    c = 14;
                                    break;
                                }
                            case 1332992761:
                                if (opt.equals("--esal")) {
                                    c = 19;
                                    break;
                                }
                            case 1353919836:
                                if (opt.equals("--activity-clear-when-task-reset")) {
                                    c = '!';
                                    break;
                                }
                            case 1398403374:
                                if (opt.equals("--activity-launched-from-history")) {
                                    c = '#';
                                    break;
                                }
                            case 1453225122:
                                if (opt.equals("--receiver-no-abort")) {
                                    c = '2';
                                    break;
                                }
                            case 1652786753:
                                if (opt.equals("--receiver-foreground")) {
                                    c = '1';
                                    break;
                                }
                            case 1742380566:
                                if (opt.equals("--grant-read-uri-permission")) {
                                    c = 24;
                                    break;
                                }
                            case 1765369476:
                                if (opt.equals("--activity-multiple-task")) {
                                    c = '$';
                                    break;
                                }
                            case 1816558127:
                                if (opt.equals("--grant-write-uri-permission")) {
                                    c = 25;
                                    break;
                                }
                        }
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        intent2.setAction(cmd.getNextArgRequired());
                        if (intent2 == baseIntent) {
                            hasIntentInfo = true;
                            break;
                        }
                        break;
                    case 1:
                        Uri parse = Uri.parse(cmd.getNextArgRequired());
                        if (intent2 == baseIntent) {
                            ShellCommand shellCommand = cmd;
                            hasIntentInfo2 = true;
                        } else {
                            ShellCommand shellCommand2 = cmd;
                        }
                        data = parse;
                        break;
                    case 2:
                        type = cmd.getNextArgRequired();
                        if (intent2 == baseIntent) {
                            hasIntentInfo2 = true;
                        }
                        ShellCommand shellCommand3 = cmd;
                        break;
                    case 3:
                        intent2.addCategory(cmd.getNextArgRequired());
                        if (intent2 == baseIntent) {
                            hasIntentInfo = true;
                            break;
                        }
                        break;
                    case 4:
                    case 5:
                        intent2.putExtra(cmd.getNextArgRequired(), cmd.getNextArgRequired());
                        break;
                    case 6:
                        intent2.putExtra(cmd.getNextArgRequired(), (String) null);
                        break;
                    case 7:
                        intent2.putExtra(cmd.getNextArgRequired(), (Serializable) Integer.decode(cmd.getNextArgRequired()));
                        break;
                    case 8:
                        intent2.putExtra(cmd.getNextArgRequired(), (Parcelable) Uri.parse(cmd.getNextArgRequired()));
                        break;
                    case 9:
                        String key = cmd.getNextArgRequired();
                        ComponentName cn = ComponentName.unflattenFromString(cmd.getNextArgRequired());
                        if (cn != null) {
                            intent2.putExtra(key, (Parcelable) cn);
                            break;
                        } else {
                            throw new IllegalArgumentException("Bad component name: " + value);
                        }
                    case 10:
                        String key2 = cmd.getNextArgRequired();
                        String[] strings = cmd.getNextArgRequired().split(",");
                        int[] list = new int[strings.length];
                        int i = 0;
                        while (true) {
                            int i2 = i;
                            if (i2 >= strings.length) {
                                intent2.putExtra(key2, list);
                                break;
                            } else {
                                list[i2] = Integer.decode(strings[i2]).intValue();
                                i = i2 + 1;
                            }
                        }
                    case 11:
                        String key3 = cmd.getNextArgRequired();
                        String[] strings2 = cmd.getNextArgRequired().split(",");
                        ArrayList<Integer> list2 = new ArrayList<>(strings2.length);
                        int i3 = 0;
                        while (true) {
                            int i4 = i3;
                            if (i4 >= strings2.length) {
                                intent2.putExtra(key3, (Serializable) list2);
                                break;
                            } else {
                                list2.add(Integer.decode(strings2[i4]));
                                i3 = i4 + 1;
                            }
                        }
                    case 12:
                        intent2.putExtra(cmd.getNextArgRequired(), (Serializable) Long.valueOf(cmd.getNextArgRequired()));
                        break;
                    case 13:
                        String key4 = cmd.getNextArgRequired();
                        String[] strings3 = cmd.getNextArgRequired().split(",");
                        long[] list3 = new long[strings3.length];
                        int i5 = 0;
                        while (true) {
                            int i6 = i5;
                            if (i6 >= strings3.length) {
                                intent2.putExtra(key4, list3);
                                hasIntentInfo = true;
                                break;
                            } else {
                                list3[i6] = Long.valueOf(strings3[i6]).longValue();
                                i5 = i6 + 1;
                            }
                        }
                    case 14:
                        String key5 = cmd.getNextArgRequired();
                        String[] strings4 = cmd.getNextArgRequired().split(",");
                        ArrayList<Long> list4 = new ArrayList<>(strings4.length);
                        int i7 = 0;
                        while (true) {
                            int i8 = i7;
                            if (i8 >= strings4.length) {
                                intent2.putExtra(key5, (Serializable) list4);
                                hasIntentInfo = true;
                                break;
                            } else {
                                list4.add(Long.valueOf(strings4[i8]));
                                i7 = i8 + 1;
                            }
                        }
                    case 15:
                        intent2.putExtra(cmd.getNextArgRequired(), (Serializable) Float.valueOf(cmd.getNextArgRequired()));
                        hasIntentInfo = true;
                        break;
                    case 16:
                        String key6 = cmd.getNextArgRequired();
                        String[] strings5 = cmd.getNextArgRequired().split(",");
                        float[] list5 = new float[strings5.length];
                        int i9 = 0;
                        while (true) {
                            int i10 = i9;
                            if (i10 >= strings5.length) {
                                intent2.putExtra(key6, list5);
                                hasIntentInfo = true;
                                break;
                            } else {
                                list5[i10] = Float.valueOf(strings5[i10]).floatValue();
                                i9 = i10 + 1;
                            }
                        }
                    case 17:
                        String key7 = cmd.getNextArgRequired();
                        String[] strings6 = cmd.getNextArgRequired().split(",");
                        ArrayList<Float> list6 = new ArrayList<>(strings6.length);
                        int i11 = 0;
                        while (true) {
                            int i12 = i11;
                            if (i12 >= strings6.length) {
                                intent2.putExtra(key7, (Serializable) list6);
                                hasIntentInfo = true;
                                break;
                            } else {
                                list6.add(Float.valueOf(strings6[i12]));
                                i11 = i12 + 1;
                            }
                        }
                    case 18:
                        intent2.putExtra(cmd.getNextArgRequired(), cmd.getNextArgRequired().split("(?<!\\\\),"));
                        hasIntentInfo = true;
                        break;
                    case 19:
                        String key8 = cmd.getNextArgRequired();
                        String[] strings7 = cmd.getNextArgRequired().split("(?<!\\\\),");
                        ArrayList<String> list7 = new ArrayList<>(strings7.length);
                        int i13 = 0;
                        while (true) {
                            int i14 = i13;
                            if (i14 >= strings7.length) {
                                intent2.putExtra(key8, (Serializable) list7);
                                hasIntentInfo = true;
                                break;
                            } else {
                                list7.add(strings7[i14]);
                                i13 = i14 + 1;
                            }
                        }
                    case 20:
                        boolean arg = true;
                        String key9 = cmd.getNextArgRequired();
                        String value = cmd.getNextArgRequired().toLowerCase();
                        if ("true".equals(value) || "t".equals(value)) {
                            arg = true;
                        } else if ("false".equals(value) || FullBackup.FILES_TREE_TOKEN.equals(value)) {
                            arg = false;
                        } else {
                            try {
                                if (Integer.decode(value).intValue() == 0) {
                                    arg = false;
                                }
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid boolean value: " + value);
                            }
                        }
                        intent2.putExtra(key9, arg);
                        break;
                    case 21:
                        ComponentName cn2 = ComponentName.unflattenFromString(cmd.getNextArgRequired());
                        if (cn2 != null) {
                            intent2.setComponent(cn2);
                            if (intent2 == baseIntent) {
                                hasIntentInfo2 = true;
                                break;
                            }
                        } else {
                            throw new IllegalArgumentException("Bad component name: " + str);
                        }
                        break;
                    case 22:
                        intent2.setPackage(cmd.getNextArgRequired());
                        if (intent2 == baseIntent) {
                            hasIntentInfo2 = true;
                            break;
                        }
                        break;
                    case 23:
                        intent2.setFlags(Integer.decode(cmd.getNextArgRequired()).intValue());
                        break;
                    case 24:
                        intent2.addFlags(1);
                        break;
                    case 25:
                        intent2.addFlags(2);
                        break;
                    case 26:
                        intent2.addFlags(64);
                        break;
                    case 27:
                        intent2.addFlags(128);
                        break;
                    case 28:
                        intent2.addFlags(16);
                        break;
                    case 29:
                        intent2.addFlags(32);
                        break;
                    case 30:
                        intent2.addFlags(8);
                        break;
                    case 31:
                        intent2.addFlags(4194304);
                        break;
                    case ' ':
                        intent2.addFlags(67108864);
                        break;
                    case '!':
                        intent2.addFlags(524288);
                        break;
                    case '\"':
                        intent2.addFlags(8388608);
                        break;
                    case '#':
                        intent2.addFlags(1048576);
                        break;
                    case '$':
                        intent2.addFlags(134217728);
                        break;
                    case '%':
                        intent2.addFlags(65536);
                        break;
                    case '&':
                        intent2.addFlags(1073741824);
                        break;
                    case '\'':
                        intent2.addFlags(262144);
                        break;
                    case '(':
                        intent2.addFlags(16777216);
                        break;
                    case ')':
                        intent2.addFlags(131072);
                        break;
                    case '*':
                        intent2.addFlags(2097152);
                        break;
                    case '+':
                        intent2.addFlags(536870912);
                        break;
                    case ',':
                        intent2.addFlags(32768);
                        break;
                    case '-':
                        intent2.addFlags(16384);
                        break;
                    case '.':
                        intent2.addFlags(2048);
                        break;
                    case '/':
                        intent2.addFlags(1073741824);
                        break;
                    case '0':
                        intent2.addFlags(536870912);
                        break;
                    case '1':
                        intent2.addFlags(268435456);
                        break;
                    case '2':
                        intent2.addFlags(134217728);
                        break;
                    case '3':
                        intent2.addFlags(16777216);
                        break;
                    case '4':
                        intent2.setDataAndType(data, type2);
                        ShellCommand shellCommand4 = cmd;
                        intent2 = new Intent();
                        break;
                    default:
                        if (commandOptionHandler != null) {
                            if (!commandOptionHandler.handleOption(opt, cmd)) {
                                break;
                            }
                        } else {
                            ShellCommand shellCommand5 = cmd;
                            break;
                        }
                        break;
                }
            } else {
                ShellCommand shellCommand6 = cmd;
                boolean hasSelector = true;
                intent2.setDataAndType(data, type2);
                if (intent2 == baseIntent) {
                    hasSelector = false;
                }
                if (hasSelector) {
                    baseIntent.setSelector(intent2);
                    intent2 = baseIntent;
                }
                String arg2 = cmd.getNextArg();
                Intent baseIntent2 = null;
                if (arg2 == null) {
                    if (hasSelector) {
                        baseIntent2 = new Intent(ACTION_MAIN);
                        baseIntent2.addCategory(CATEGORY_LAUNCHER);
                    }
                } else if (arg2.indexOf(58) >= 0) {
                    baseIntent2 = parseUri(arg2, 7);
                } else if (arg2.indexOf(47) >= 0) {
                    baseIntent2 = new Intent(ACTION_MAIN);
                    baseIntent2.addCategory(CATEGORY_LAUNCHER);
                    baseIntent2.setComponent(ComponentName.unflattenFromString(arg2));
                } else {
                    baseIntent2 = new Intent(ACTION_MAIN);
                    baseIntent2.addCategory(CATEGORY_LAUNCHER);
                    baseIntent2.setPackage(arg2);
                }
                if (baseIntent2 != null) {
                    Bundle extras = intent2.getExtras();
                    Bundle bundle = null;
                    intent2.replaceExtras(bundle);
                    Bundle uriExtras = baseIntent2.getExtras();
                    baseIntent2.replaceExtras(bundle);
                    if (!(intent2.getAction() == null || baseIntent2.getCategories() == null)) {
                        Iterator<String> it = new HashSet<>(baseIntent2.getCategories()).iterator();
                        while (it.hasNext()) {
                            baseIntent2.removeCategory(it.next());
                        }
                    }
                    intent2.fillIn(baseIntent2, 72);
                    if (extras == null) {
                        extras = uriExtras;
                    } else if (uriExtras != null) {
                        uriExtras.putAll(extras);
                        extras = uriExtras;
                    }
                    intent2.replaceExtras(extras);
                    hasIntentInfo2 = true;
                }
                if (hasIntentInfo2) {
                    return intent2;
                }
                throw new IllegalArgumentException("No intent supplied");
            }
        }
        throw new IllegalArgumentException("Unknown option: " + opt);
    }

    public static void printIntentArgsHelp(PrintWriter pw, String prefix) {
        for (String line : new String[]{"<INTENT> specifications include these flags and arguments:", "    [-a <ACTION>] [-d <DATA_URI>] [-t <MIME_TYPE>]", "    [-c <CATEGORY> [-c <CATEGORY>] ...]", "    [-n <COMPONENT_NAME>]", "    [-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]", "    [--esn <EXTRA_KEY> ...]", "    [--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]", "    [--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]", "    [--el <EXTRA_KEY> <EXTRA_LONG_VALUE> ...]", "    [--ef <EXTRA_KEY> <EXTRA_FLOAT_VALUE> ...]", "    [--eu <EXTRA_KEY> <EXTRA_URI_VALUE> ...]", "    [--ecn <EXTRA_KEY> <EXTRA_COMPONENT_NAME_VALUE>]", "    [--eia <EXTRA_KEY> <EXTRA_INT_VALUE>[,<EXTRA_INT_VALUE...]]", "        (mutiple extras passed as Integer[])", "    [--eial <EXTRA_KEY> <EXTRA_INT_VALUE>[,<EXTRA_INT_VALUE...]]", "        (mutiple extras passed as List<Integer>)", "    [--ela <EXTRA_KEY> <EXTRA_LONG_VALUE>[,<EXTRA_LONG_VALUE...]]", "        (mutiple extras passed as Long[])", "    [--elal <EXTRA_KEY> <EXTRA_LONG_VALUE>[,<EXTRA_LONG_VALUE...]]", "        (mutiple extras passed as List<Long>)", "    [--efa <EXTRA_KEY> <EXTRA_FLOAT_VALUE>[,<EXTRA_FLOAT_VALUE...]]", "        (mutiple extras passed as Float[])", "    [--efal <EXTRA_KEY> <EXTRA_FLOAT_VALUE>[,<EXTRA_FLOAT_VALUE...]]", "        (mutiple extras passed as List<Float>)", "    [--esa <EXTRA_KEY> <EXTRA_STRING_VALUE>[,<EXTRA_STRING_VALUE...]]", "        (mutiple extras passed as String[]; to embed a comma into a string,", "         escape it using \"\\,\")", "    [--esal <EXTRA_KEY> <EXTRA_STRING_VALUE>[,<EXTRA_STRING_VALUE...]]", "        (mutiple extras passed as List<String>; to embed a comma into a string,", "         escape it using \"\\,\")", "    [-f <FLAG>]", "    [--grant-read-uri-permission] [--grant-write-uri-permission]", "    [--grant-persistable-uri-permission] [--grant-prefix-uri-permission]", "    [--debug-log-resolution] [--exclude-stopped-packages]", "    [--include-stopped-packages]", "    [--activity-brought-to-front] [--activity-clear-top]", "    [--activity-clear-when-task-reset] [--activity-exclude-from-recents]", "    [--activity-launched-from-history] [--activity-multiple-task]", "    [--activity-no-animation] [--activity-no-history]", "    [--activity-no-user-action] [--activity-previous-is-top]", "    [--activity-reorder-to-front] [--activity-reset-task-if-needed]", "    [--activity-single-top] [--activity-clear-task]", "    [--activity-task-on-home] [--activity-match-external]", "    [--receiver-registered-only] [--receiver-replace-pending]", "    [--receiver-foreground] [--receiver-no-abort]", "    [--receiver-include-background]", "    [--selector]", "    [<URI> | <PACKAGE> | <COMPONENT>]"}) {
            pw.print(prefix);
            pw.println(line);
        }
        PrintWriter printWriter = pw;
    }

    public String getAction() {
        return this.mAction;
    }

    public Uri getData() {
        return this.mData;
    }

    public String getDataString() {
        if (this.mData != null) {
            return this.mData.toString();
        }
        return null;
    }

    public String getScheme() {
        if (this.mData != null) {
            return this.mData.getScheme();
        }
        return null;
    }

    public String getType() {
        return this.mType;
    }

    public String resolveType(Context context) {
        return resolveType(context.getContentResolver());
    }

    public String resolveType(ContentResolver resolver) {
        if (this.mType != null) {
            return this.mType;
        }
        if (this.mData == null || !"content".equals(this.mData.getScheme())) {
            return null;
        }
        return resolver.getType(this.mData);
    }

    public String resolveTypeIfNeeded(ContentResolver resolver) {
        if (this.mComponent != null) {
            return this.mType;
        }
        return resolveType(resolver);
    }

    public boolean hasCategory(String category) {
        return this.mCategories != null && this.mCategories.contains(category);
    }

    public Set<String> getCategories() {
        return this.mCategories;
    }

    public Intent getSelector() {
        return this.mSelector;
    }

    public ClipData getClipData() {
        return this.mClipData;
    }

    public int getContentUserHint() {
        return this.mContentUserHint;
    }

    public String getLaunchToken() {
        return this.mLaunchToken;
    }

    public void setLaunchToken(String launchToken) {
        this.mLaunchToken = launchToken;
    }

    public void setExtrasClassLoader(ClassLoader loader) {
        if (this.mExtras != null) {
            this.mExtras.setClassLoader(loader);
        }
    }

    public boolean hasExtra(String name) {
        return this.mExtras != null && this.mExtras.containsKey(name);
    }

    public boolean hasFileDescriptors() {
        return this.mExtras != null && this.mExtras.hasFileDescriptors();
    }

    public void setAllowFds(boolean allowFds) {
        if (this.mExtras != null) {
            this.mExtras.setAllowFds(allowFds);
        }
    }

    public void setDefusable(boolean defusable) {
        if (this.mExtras != null) {
            this.mExtras.setDefusable(defusable);
        }
    }

    @Deprecated
    public Object getExtra(String name) {
        return getExtra(name, null);
    }

    public boolean getBooleanExtra(String name, boolean defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getBoolean(name, defaultValue);
    }

    public byte getByteExtra(String name, byte defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getByte(name, defaultValue).byteValue();
    }

    public short getShortExtra(String name, short defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getShort(name, defaultValue);
    }

    public char getCharExtra(String name, char defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getChar(name, defaultValue);
    }

    public int getIntExtra(String name, int defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getInt(name, defaultValue);
    }

    public long getLongExtra(String name, long defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getLong(name, defaultValue);
    }

    public float getFloatExtra(String name, float defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getFloat(name, defaultValue);
    }

    public double getDoubleExtra(String name, double defaultValue) {
        if (this.mExtras == null) {
            return defaultValue;
        }
        return this.mExtras.getDouble(name, defaultValue);
    }

    public String getStringExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getString(name);
    }

    public CharSequence getCharSequenceExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getCharSequence(name);
    }

    public <T extends Parcelable> T getParcelableExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getParcelable(name);
    }

    public Parcelable[] getParcelableArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getParcelableArray(name);
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getParcelableArrayList(name);
    }

    public Serializable getSerializableExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getSerializable(name);
    }

    public ArrayList<Integer> getIntegerArrayListExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getIntegerArrayList(name);
    }

    public ArrayList<String> getStringArrayListExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getStringArrayList(name);
    }

    public ArrayList<CharSequence> getCharSequenceArrayListExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getCharSequenceArrayList(name);
    }

    public boolean[] getBooleanArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getBooleanArray(name);
    }

    public byte[] getByteArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getByteArray(name);
    }

    public short[] getShortArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getShortArray(name);
    }

    public char[] getCharArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getCharArray(name);
    }

    public int[] getIntArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getIntArray(name);
    }

    public long[] getLongArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getLongArray(name);
    }

    public float[] getFloatArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getFloatArray(name);
    }

    public double[] getDoubleArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getDoubleArray(name);
    }

    public String[] getStringArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getStringArray(name);
    }

    public CharSequence[] getCharSequenceArrayExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getCharSequenceArray(name);
    }

    public Bundle getBundleExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getBundle(name);
    }

    @Deprecated
    public IBinder getIBinderExtra(String name) {
        if (this.mExtras == null) {
            return null;
        }
        return this.mExtras.getIBinder(name);
    }

    @Deprecated
    public Object getExtra(String name, Object defaultValue) {
        Object result = defaultValue;
        if (this.mExtras == null) {
            return result;
        }
        Object result2 = this.mExtras.get(name);
        if (result2 != null) {
            return result2;
        }
        return result;
    }

    public Bundle getExtras() {
        if (this.mExtras != null) {
            return new Bundle(this.mExtras);
        }
        return null;
    }

    public void removeUnsafeExtras() {
        if (this.mExtras != null) {
            this.mExtras = this.mExtras.filterValues();
        }
    }

    public boolean canStripForHistory() {
        return (this.mExtras != null && this.mExtras.isParcelled()) || this.mClipData != null;
    }

    public Intent maybeStripForHistory() {
        if (!canStripForHistory()) {
            return this;
        }
        return new Intent(this, 2);
    }

    public int getFlags() {
        return this.mFlags;
    }

    public int getHwFlags() {
        return this.hwFlags;
    }

    public int getCallingUid() {
        return this.callingUid;
    }

    public boolean isExcludingStopped() {
        return (this.mFlags & 48) == 16;
    }

    public String getPackage() {
        return this.mPackage;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    public Rect getSourceBounds() {
        return this.mSourceBounds;
    }

    public ComponentName resolveActivity(PackageManager pm) {
        if (this.mComponent != null) {
            return this.mComponent;
        }
        ResolveInfo info = pm.resolveActivity(this, 65536);
        if (info != null) {
            return new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        }
        return null;
    }

    public ActivityInfo resolveActivityInfo(PackageManager pm, int flags) {
        if (this.mComponent != null) {
            try {
                return pm.getActivityInfo(this.mComponent, flags);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        } else {
            ResolveInfo info = pm.resolveActivity(this, 65536 | flags);
            if (info != null) {
                return info.activityInfo;
            }
            return null;
        }
    }

    public ComponentName resolveSystemService(PackageManager pm, int flags) {
        if (this.mComponent != null) {
            return this.mComponent;
        }
        List<ResolveInfo> results = pm.queryIntentServices(this, flags);
        if (results == null) {
            return null;
        }
        ComponentName comp = null;
        for (int i = 0; i < results.size(); i++) {
            ResolveInfo ri = results.get(i);
            if ((ri.serviceInfo.applicationInfo.flags & 1) != 0) {
                ComponentName foundComp = new ComponentName(ri.serviceInfo.applicationInfo.packageName, ri.serviceInfo.name);
                if (comp == null) {
                    comp = foundComp;
                } else {
                    throw new IllegalStateException("Multiple system services handle " + this + ": " + comp + ", " + foundComp);
                }
            }
        }
        return comp;
    }

    public Intent setAction(String action) {
        this.mAction = action != null ? action.intern() : null;
        return this;
    }

    public Intent setData(Uri data) {
        this.mData = data;
        this.mType = null;
        return this;
    }

    public Intent setDataAndNormalize(Uri data) {
        return setData(data.normalizeScheme());
    }

    public Intent setType(String type) {
        this.mData = null;
        this.mType = type;
        return this;
    }

    public Intent setTypeAndNormalize(String type) {
        return setType(normalizeMimeType(type));
    }

    public Intent setDataAndType(Uri data, String type) {
        this.mData = data;
        this.mType = type;
        return this;
    }

    public Intent setDataAndTypeAndNormalize(Uri data, String type) {
        return setDataAndType(data.normalizeScheme(), normalizeMimeType(type));
    }

    public Intent addCategory(String category) {
        if (this.mCategories == null) {
            this.mCategories = new ArraySet<>();
        }
        this.mCategories.add(category.intern());
        return this;
    }

    public void removeCategory(String category) {
        if (this.mCategories != null) {
            this.mCategories.remove(category);
            if (this.mCategories.size() == 0) {
                this.mCategories = null;
            }
        }
    }

    public void setSelector(Intent selector) {
        if (selector == this) {
            throw new IllegalArgumentException("Intent being set as a selector of itself");
        } else if (selector == null || this.mPackage == null) {
            this.mSelector = selector;
        } else {
            throw new IllegalArgumentException("Can't set selector when package name is already set");
        }
    }

    public void setClipData(ClipData clip) {
        this.mClipData = clip;
    }

    public void prepareToLeaveUser(int userId) {
        if (this.mContentUserHint == -2) {
            this.mContentUserHint = userId;
        }
    }

    public Intent putExtra(String name, boolean value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putBoolean(name, value);
        return this;
    }

    public Intent putExtra(String name, byte value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putByte(name, value);
        return this;
    }

    public Intent putExtra(String name, char value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putChar(name, value);
        return this;
    }

    public Intent putExtra(String name, short value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putShort(name, value);
        return this;
    }

    public Intent putExtra(String name, int value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putInt(name, value);
        return this;
    }

    public Intent putExtra(String name, long value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putLong(name, value);
        return this;
    }

    public Intent putExtra(String name, float value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putFloat(name, value);
        return this;
    }

    public Intent putExtra(String name, double value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putDouble(name, value);
        return this;
    }

    public Intent putExtra(String name, String value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putString(name, value);
        return this;
    }

    public Intent putExtra(String name, CharSequence value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putCharSequence(name, value);
        return this;
    }

    public Intent putExtra(String name, Parcelable value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putParcelable(name, value);
        return this;
    }

    public Intent putExtra(String name, Parcelable[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putParcelableArray(name, value);
        return this;
    }

    public Intent putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putParcelableArrayList(name, value);
        return this;
    }

    public Intent putIntegerArrayListExtra(String name, ArrayList<Integer> value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putIntegerArrayList(name, value);
        return this;
    }

    public Intent putStringArrayListExtra(String name, ArrayList<String> value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putStringArrayList(name, value);
        return this;
    }

    public Intent putCharSequenceArrayListExtra(String name, ArrayList<CharSequence> value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putCharSequenceArrayList(name, value);
        return this;
    }

    public Intent putExtra(String name, Serializable value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putSerializable(name, value);
        return this;
    }

    public Intent putExtra(String name, boolean[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putBooleanArray(name, value);
        return this;
    }

    public Intent putExtra(String name, byte[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putByteArray(name, value);
        return this;
    }

    public Intent putExtra(String name, short[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putShortArray(name, value);
        return this;
    }

    public Intent putExtra(String name, char[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putCharArray(name, value);
        return this;
    }

    public Intent putExtra(String name, int[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putIntArray(name, value);
        return this;
    }

    public Intent putExtra(String name, long[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putLongArray(name, value);
        return this;
    }

    public Intent putExtra(String name, float[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putFloatArray(name, value);
        return this;
    }

    public Intent putExtra(String name, double[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putDoubleArray(name, value);
        return this;
    }

    public Intent putExtra(String name, String[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putStringArray(name, value);
        return this;
    }

    public Intent putExtra(String name, CharSequence[] value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putCharSequenceArray(name, value);
        return this;
    }

    public Intent putExtra(String name, Bundle value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putBundle(name, value);
        return this;
    }

    @Deprecated
    public Intent putExtra(String name, IBinder value) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putIBinder(name, value);
        return this;
    }

    public Intent putExtras(Intent src) {
        if (src.mExtras != null) {
            if (this.mExtras == null) {
                this.mExtras = new Bundle(src.mExtras);
            } else {
                this.mExtras.putAll(src.mExtras);
            }
        }
        return this;
    }

    public Intent putExtras(Bundle extras) {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        this.mExtras.putAll(extras);
        return this;
    }

    public Intent replaceExtras(Intent src) {
        this.mExtras = src.mExtras != null ? new Bundle(src.mExtras) : null;
        return this;
    }

    public Intent replaceExtras(Bundle extras) {
        this.mExtras = extras != null ? new Bundle(extras) : null;
        return this;
    }

    public void removeExtra(String name) {
        if (this.mExtras != null) {
            this.mExtras.remove(name);
            if (this.mExtras.size() == 0) {
                this.mExtras = null;
            }
        }
    }

    public Intent setFlags(int flags) {
        this.mFlags = flags;
        return this;
    }

    public Intent setHwFlags(int flags) {
        this.hwFlags = flags;
        return this;
    }

    public Intent setCallingUid(int callingUid2) {
        this.callingUid = callingUid2;
        return this;
    }

    public Intent addFlags(int flags) {
        this.mFlags |= flags;
        return this;
    }

    public Intent addHwFlags(int flags) {
        this.hwFlags |= flags;
        return this;
    }

    public void removeFlags(int flags) {
        this.mFlags &= ~flags;
    }

    public Intent setPackage(String packageName) {
        if (packageName == null || this.mSelector == null) {
            this.mPackage = packageName;
            return this;
        }
        throw new IllegalArgumentException("Can't set package name when selector is already set");
    }

    public Intent setComponent(ComponentName component) {
        this.mComponent = component;
        return this;
    }

    public Intent setClassName(Context packageContext, String className) {
        this.mComponent = new ComponentName(packageContext, className);
        return this;
    }

    public Intent setClassName(String packageName, String className) {
        this.mComponent = new ComponentName(packageName, className);
        return this;
    }

    public Intent setClass(Context packageContext, Class<?> cls) {
        this.mComponent = new ComponentName(packageContext, cls);
        return this;
    }

    public void setSourceBounds(Rect r) {
        if (r != null) {
            this.mSourceBounds = new Rect(r);
        } else {
            this.mSourceBounds = null;
        }
    }

    public int fillIn(Intent other, int flags) {
        int changes = 0;
        boolean mayHaveCopiedUris = false;
        if (other.mAction != null && (this.mAction == null || (flags & 1) != 0)) {
            this.mAction = other.mAction;
            changes = 0 | 1;
        }
        if (!(other.mData == null && other.mType == null) && ((this.mData == null && this.mType == null) || (flags & 2) != 0)) {
            this.mData = other.mData;
            this.mType = other.mType;
            changes |= 2;
            mayHaveCopiedUris = true;
        }
        if (other.mCategories != null && (this.mCategories == null || (flags & 4) != 0)) {
            if (other.mCategories != null) {
                this.mCategories = new ArraySet<>(other.mCategories);
            }
            changes |= 4;
        }
        if (other.mPackage != null && ((this.mPackage == null || (flags & 16) != 0) && this.mSelector == null)) {
            this.mPackage = other.mPackage;
            changes |= 16;
        }
        if (!(other.mSelector == null || (flags & 64) == 0 || this.mPackage != null)) {
            this.mSelector = new Intent(other.mSelector);
            this.mPackage = null;
            changes |= 64;
        }
        if (other.mClipData != null && (this.mClipData == null || (flags & 128) != 0)) {
            this.mClipData = other.mClipData;
            changes |= 128;
            mayHaveCopiedUris = true;
        }
        if (!(other.mComponent == null || (flags & 8) == 0)) {
            this.mComponent = other.mComponent;
            changes |= 8;
        }
        this.mFlags |= other.mFlags;
        this.hwFlags |= other.hwFlags;
        this.callingUid = other.callingUid;
        if (other.mSourceBounds != null && (this.mSourceBounds == null || (flags & 32) != 0)) {
            this.mSourceBounds = new Rect(other.mSourceBounds);
            changes |= 32;
        }
        if (this.mExtras == null) {
            if (other.mExtras != null) {
                this.mExtras = new Bundle(other.mExtras);
                mayHaveCopiedUris = true;
            }
        } else if (other.mExtras != null) {
            try {
                Bundle newb = new Bundle(other.mExtras);
                newb.putAll(this.mExtras);
                this.mExtras = newb;
                mayHaveCopiedUris = true;
            } catch (RuntimeException e) {
                Log.w("Intent", "Failure filling in extras", e);
            }
        }
        if (mayHaveCopiedUris && this.mContentUserHint == -2 && other.mContentUserHint != -2) {
            this.mContentUserHint = other.mContentUserHint;
        }
        return changes;
    }

    public boolean filterEquals(Intent other) {
        if (other != null && Objects.equals(this.mAction, other.mAction) && Objects.equals(this.mData, other.mData) && Objects.equals(this.mType, other.mType) && Objects.equals(this.mPackage, other.mPackage) && Objects.equals(this.mComponent, other.mComponent) && Objects.equals(this.mCategories, other.mCategories)) {
            return true;
        }
        return false;
    }

    public int filterHashCode() {
        int code = 0;
        if (this.mAction != null) {
            code = 0 + this.mAction.hashCode();
        }
        if (this.mData != null) {
            code += this.mData.hashCode();
        }
        if (this.mType != null) {
            code += this.mType.hashCode();
        }
        if (this.mPackage != null) {
            code += this.mPackage.hashCode();
        }
        if (this.mComponent != null) {
            code += this.mComponent.hashCode();
        }
        if (this.mCategories != null) {
            return code + this.mCategories.hashCode();
        }
        return code;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(128);
        b.append("Intent { ");
        toShortString(b, true, true, true, false);
        b.append(" }");
        return b.toString();
    }

    public String toInsecureString() {
        StringBuilder b = new StringBuilder(128);
        b.append("Intent { ");
        toShortString(b, false, true, true, false);
        b.append(" }");
        return b.toString();
    }

    public String toInsecureStringWithClip() {
        StringBuilder b = new StringBuilder(128);
        b.append("Intent { ");
        toShortString(b, false, true, true, true);
        b.append(" }");
        return b.toString();
    }

    public String toShortString(boolean secure, boolean comp, boolean extras, boolean clip) {
        StringBuilder b = new StringBuilder(128);
        toShortString(b, secure, comp, extras, clip);
        return b.toString();
    }

    public void toShortString(StringBuilder b, boolean secure, boolean comp, boolean extras, boolean clip) {
        boolean first;
        boolean first2 = true;
        if (this.mAction != null) {
            b.append("act=");
            b.append(this.mAction);
            first2 = false;
        }
        if (this.mCategories != null) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("cat=[");
            for (int i = 0; i < this.mCategories.size(); i++) {
                if (i > 0) {
                    b.append(',');
                }
                b.append(this.mCategories.valueAt(i));
            }
            b.append("]");
        }
        if (this.mData != null && (this.hwFlags & 16) == 0) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("dat=");
            if (secure) {
                b.append(this.mData.toSafeString());
            } else {
                b.append(this.mData);
            }
        }
        if (this.mType != null) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("typ=");
            b.append(this.mType);
        }
        if (this.mFlags != 0) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("flg=0x");
            b.append(Integer.toHexString(this.mFlags));
        }
        if (this.hwFlags != 0) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("hwFlg=0x");
            b.append(Integer.toHexString(this.hwFlags));
        }
        if (this.mPackage != null) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("pkg=");
            b.append(this.mPackage);
        }
        if (comp && this.mComponent != null) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("cmp=");
            b.append(this.mComponent.flattenToShortString());
        }
        if (this.mSourceBounds != null) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("bnds=");
            b.append(this.mSourceBounds.toShortString());
        }
        if (this.mClipData != null && (this.hwFlags & 16) == 0) {
            if (!first2) {
                b.append(' ');
            }
            b.append("clip={");
            if (clip) {
                this.mClipData.toShortString(b);
            } else {
                if (this.mClipData.getDescription() != null) {
                    first = !this.mClipData.getDescription().toShortStringTypesOnly(b);
                } else {
                    first = true;
                }
                this.mClipData.toShortStringShortItems(b, first);
            }
            first2 = false;
            b.append('}');
        }
        if (extras && this.mExtras != null) {
            if (!first2) {
                b.append(' ');
            }
            first2 = false;
            b.append("(has extras)");
        }
        if (this.mContentUserHint != -2) {
            if (!first2) {
                b.append(' ');
            }
            b.append("u=");
            b.append(this.mContentUserHint);
        }
        if (this.mSelector != null) {
            b.append(" sel=");
            this.mSelector.toShortString(b, secure, comp, extras, clip);
            b.append("}");
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        writeToProto(proto, fieldId, true, true, true, false);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean secure, boolean comp, boolean extras, boolean clip) {
        long token = proto.start(fieldId);
        if (this.mAction != null) {
            proto.write(1138166333441L, this.mAction);
        }
        if (this.mCategories != null) {
            Iterator<String> it = this.mCategories.iterator();
            while (it.hasNext()) {
                proto.write(2237677961218L, it.next());
            }
        }
        if (this.mData != null) {
            proto.write(1138166333443L, secure ? this.mData.toSafeString() : this.mData.toString());
        }
        if (this.mType != null) {
            proto.write(1138166333444L, this.mType);
        }
        if (this.mFlags != 0) {
            proto.write(1138166333445L, "0x" + Integer.toHexString(this.mFlags));
        }
        if (this.mPackage != null) {
            proto.write(1138166333446L, this.mPackage);
        }
        if (comp && this.mComponent != null) {
            this.mComponent.writeToProto(proto, IntentProto.COMPONENT);
        }
        if (this.mSourceBounds != null) {
            proto.write(1138166333448L, this.mSourceBounds.toShortString());
        }
        if (this.mClipData != null) {
            StringBuilder b = new StringBuilder();
            if (clip) {
                this.mClipData.toShortString(b);
            } else {
                this.mClipData.toShortStringShortItems(b, false);
            }
            proto.write(1138166333449L, b.toString());
        }
        if (extras && this.mExtras != null) {
            proto.write(IntentProto.EXTRAS, this.mExtras.toShortString());
        }
        if (this.mContentUserHint != 0) {
            proto.write(1120986464267L, this.mContentUserHint);
        }
        if (this.mSelector != null) {
            proto.write(IntentProto.SELECTOR, this.mSelector.toShortString(secure, comp, extras, clip));
        }
        proto.end(token);
    }

    public String toShortStringWithoutClip(boolean secure, boolean comp, boolean extras) {
        this.hwFlags |= 16;
        String result = toShortString(secure, comp, extras, false);
        this.hwFlags &= -17;
        return result;
    }

    @Deprecated
    public String toURI() {
        return toUri(0);
    }

    public String toUri(int flags) {
        StringBuilder uri = new StringBuilder(128);
        if ((flags & 2) == 0) {
            String scheme = null;
            if (this.mData != null) {
                String data = this.mData.toString();
                if ((flags & 1) != 0) {
                    int N = data.length();
                    int i = 0;
                    while (true) {
                        if (i >= N) {
                            break;
                        }
                        char c = data.charAt(i);
                        if ((c >= 'a' && c <= 'z') || ((c >= 'A' && c <= 'Z') || ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+'))) {
                            i++;
                        } else if (c == ':' && i > 0) {
                            scheme = data.substring(0, i);
                            uri.append("intent:");
                            data = data.substring(i + 1);
                        }
                    }
                }
                uri.append(data);
            } else if ((flags & 1) != 0) {
                uri.append("intent:");
            }
            toUriFragment(uri, scheme, "android.intent.action.VIEW", null, flags);
            return uri.toString();
        } else if (this.mPackage != null) {
            uri.append("android-app://");
            uri.append(this.mPackage);
            String scheme2 = null;
            if (this.mData != null) {
                scheme2 = this.mData.getScheme();
                if (scheme2 != null) {
                    uri.append('/');
                    uri.append(scheme2);
                    String authority = this.mData.getEncodedAuthority();
                    if (authority != null) {
                        uri.append('/');
                        uri.append(authority);
                        String path = this.mData.getEncodedPath();
                        if (path != null) {
                            uri.append(path);
                        }
                        String queryParams = this.mData.getEncodedQuery();
                        if (queryParams != null) {
                            uri.append('?');
                            uri.append(queryParams);
                        }
                        String fragment = this.mData.getEncodedFragment();
                        if (fragment != null) {
                            uri.append('#');
                            uri.append(fragment);
                        }
                    }
                }
            }
            toUriFragment(uri, null, scheme2 == null ? ACTION_MAIN : "android.intent.action.VIEW", this.mPackage, flags);
            return uri.toString();
        } else {
            throw new IllegalArgumentException("Intent must include an explicit package name to build an android-app: " + this);
        }
    }

    private void toUriFragment(StringBuilder uri, String scheme, String defAction, String defPackage, int flags) {
        StringBuilder frag = new StringBuilder(128);
        toUriInner(frag, scheme, defAction, defPackage, flags);
        if (this.mSelector != null) {
            frag.append("SEL;");
            this.mSelector.toUriInner(frag, this.mSelector.mData != null ? this.mSelector.mData.getScheme() : null, null, null, flags);
        }
        if (frag.length() > 0) {
            uri.append("#Intent;");
            uri.append(frag);
            uri.append(TtmlUtils.ATTR_END);
        }
    }

    private void toUriInner(StringBuilder uri, String scheme, String defAction, String defPackage, int flags) {
        char entryType;
        if (scheme != null) {
            uri.append("scheme=");
            uri.append(scheme);
            uri.append(';');
        }
        if (this.mAction != null && !this.mAction.equals(defAction)) {
            uri.append("action=");
            uri.append(Uri.encode(this.mAction));
            uri.append(';');
        }
        if (this.mCategories != null) {
            for (int i = 0; i < this.mCategories.size(); i++) {
                uri.append("category=");
                uri.append(Uri.encode(this.mCategories.valueAt(i)));
                uri.append(';');
            }
        }
        if (this.mType != null) {
            uri.append("type=");
            uri.append(Uri.encode(this.mType, "/"));
            uri.append(';');
        }
        if (this.mFlags != 0) {
            uri.append("launchFlags=0x");
            uri.append(Integer.toHexString(this.mFlags));
            uri.append(';');
        }
        if (this.hwFlags != 0) {
            uri.append("launchHwFlags=0x");
            uri.append(Integer.toHexString(this.hwFlags));
            uri.append(';');
        }
        if (this.mPackage != null && !this.mPackage.equals(defPackage)) {
            uri.append("package=");
            uri.append(Uri.encode(this.mPackage));
            uri.append(';');
        }
        if (this.mComponent != null) {
            uri.append("component=");
            uri.append(Uri.encode(this.mComponent.flattenToShortString(), "/"));
            uri.append(';');
        }
        if (this.mSourceBounds != null) {
            uri.append("sourceBounds=");
            uri.append(Uri.encode(this.mSourceBounds.flattenToString()));
            uri.append(';');
        }
        if (this.mExtras != null) {
            for (String key : this.mExtras.keySet()) {
                Object value = this.mExtras.get(key);
                if (value instanceof String) {
                    entryType = 'S';
                } else if (value instanceof Boolean) {
                    entryType = 'B';
                } else if (value instanceof Byte) {
                    entryType = 'b';
                } else if (value instanceof Character) {
                    entryType = 'c';
                } else if (value instanceof Double) {
                    entryType = 'd';
                } else if (value instanceof Float) {
                    entryType = 'f';
                } else if (value instanceof Integer) {
                    entryType = 'i';
                } else if (value instanceof Long) {
                    entryType = 'l';
                } else if (value instanceof Short) {
                    entryType = SearchManager.MENU_KEY;
                } else {
                    entryType = 0;
                }
                if (entryType != 0) {
                    uri.append(entryType);
                    uri.append('.');
                    uri.append(Uri.encode(key));
                    uri.append('=');
                    uri.append(Uri.encode(value.toString()));
                    uri.append(';');
                }
            }
        }
    }

    public int describeContents() {
        if (this.mExtras != null) {
            return this.mExtras.describeContents();
        }
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mAction);
        Uri.writeToParcel(out, this.mData);
        out.writeString(this.mType);
        out.writeInt(this.mFlags);
        out.writeInt(this.hwFlags);
        out.writeInt(this.callingUid);
        out.writeString(this.mPackage);
        ComponentName.writeToParcel(this.mComponent, out);
        if (this.mSourceBounds != null) {
            out.writeInt(1);
            this.mSourceBounds.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        if (this.mCategories != null) {
            int N = this.mCategories.size();
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeString(this.mCategories.valueAt(i));
            }
        } else {
            out.writeInt(0);
        }
        if (this.mSelector != null) {
            out.writeInt(1);
            this.mSelector.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        if (this.mClipData != null) {
            out.writeInt(1);
            this.mClipData.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.mContentUserHint);
        out.writeBundle(this.mExtras);
    }

    protected Intent(Parcel in) {
        this.mContentUserHint = -2;
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        setAction(in.readString());
        this.mData = Uri.CREATOR.createFromParcel(in);
        this.mType = in.readString();
        this.mFlags = in.readInt();
        this.hwFlags = in.readInt();
        this.callingUid = in.readInt();
        this.mPackage = in.readString();
        this.mComponent = ComponentName.readFromParcel(in);
        if (in.readInt() != 0) {
            this.mSourceBounds = Rect.CREATOR.createFromParcel(in);
        }
        int N = in.readInt();
        if (N > 0) {
            this.mCategories = new ArraySet<>();
            for (int i = 0; i < N; i++) {
                this.mCategories.add(in.readString().intern());
            }
        } else {
            this.mCategories = null;
        }
        if (in.readInt() != 0) {
            this.mSelector = new Intent(in);
        }
        if (in.readInt() != 0) {
            this.mClipData = new ClipData(in);
        }
        this.mContentUserHint = in.readInt();
        this.mExtras = in.readBundle();
    }

    public static Intent parseIntent(Resources resources, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        Resources resources2 = resources;
        AttributeSet attributeSet = attrs;
        Intent intent = new Intent();
        TypedArray sa = resources2.obtainAttributes(attributeSet, R.styleable.Intent);
        intent.setAction(sa.getString(2));
        int i = 3;
        String data = sa.getString(3);
        intent.setDataAndType(data != null ? Uri.parse(data) : null, sa.getString(1));
        String packageName = sa.getString(0);
        String className = sa.getString(4);
        if (!(packageName == null || className == null)) {
            intent.setComponent(new ComponentName(packageName, className));
        }
        sa.recycle();
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == i && parser.getDepth() <= outerDepth)) {
                return intent;
            }
            if (!(type == i || type == 4)) {
                String nodeName = parser.getName();
                if (nodeName.equals(TAG_CATEGORIES)) {
                    TypedArray sa2 = resources2.obtainAttributes(attributeSet, R.styleable.IntentCategory);
                    String cat = sa2.getString(0);
                    sa2.recycle();
                    if (cat != null) {
                        intent.addCategory(cat);
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else if (nodeName.equals("extra")) {
                    if (intent.mExtras == null) {
                        intent.mExtras = new Bundle();
                    }
                    resources2.parseBundleExtra("extra", attributeSet, intent.mExtras);
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
            i = 3;
        }
        return intent;
    }

    public void saveToXml(XmlSerializer out) throws IOException {
        if (this.mAction != null) {
            out.attribute(null, "action", this.mAction);
        }
        if (this.mData != null) {
            out.attribute(null, "data", this.mData.toString());
        }
        if (this.mType != null) {
            out.attribute(null, "type", this.mType);
        }
        if (this.mComponent != null) {
            out.attribute(null, "component", this.mComponent.flattenToShortString());
        }
        out.attribute(null, ATTR_FLAGS, Integer.toHexString(getFlags()));
        out.attribute(null, ATTR_HWFLAGS, Integer.toHexString(getHwFlags()));
        if (this.mCategories != null) {
            out.startTag(null, TAG_CATEGORIES);
            for (int categoryNdx = this.mCategories.size() - 1; categoryNdx >= 0; categoryNdx--) {
                out.attribute(null, ATTR_CATEGORY, this.mCategories.valueAt(categoryNdx));
            }
            out.endTag(null, TAG_CATEGORIES);
        }
    }

    public static Intent restoreFromXml(XmlPullParser in) throws IOException, XmlPullParserException {
        Intent intent = new Intent();
        int outerDepth = in.getDepth();
        for (int attrNdx = in.getAttributeCount() - 1; attrNdx >= 0; attrNdx--) {
            String attrName = in.getAttributeName(attrNdx);
            String attrValue = in.getAttributeValue(attrNdx);
            if ("action".equals(attrName)) {
                intent.setAction(attrValue);
            } else if ("data".equals(attrName)) {
                intent.setData(Uri.parse(attrValue));
            } else if ("type".equals(attrName)) {
                intent.setType(attrValue);
            } else if ("component".equals(attrName)) {
                intent.setComponent(ComponentName.unflattenFromString(attrValue));
            } else if (ATTR_FLAGS.equals(attrName)) {
                intent.setFlags(Integer.valueOf(attrValue, 16).intValue());
            } else if (ATTR_HWFLAGS.equals(attrName)) {
                intent.setHwFlags(Integer.valueOf(attrValue, 16).intValue());
            } else {
                Log.e("Intent", "restoreFromXml: unknown attribute=" + attrName);
            }
        }
        while (true) {
            int attrNdx2 = in.next();
            int event = attrNdx2;
            if (attrNdx2 == 1 || (event == 3 && in.getDepth() >= outerDepth)) {
                return intent;
            }
            if (event == 2) {
                if (TAG_CATEGORIES.equals(in.getName())) {
                    for (int attrNdx3 = in.getAttributeCount() - 1; attrNdx3 >= 0; attrNdx3--) {
                        intent.addCategory(in.getAttributeValue(attrNdx3));
                    }
                } else {
                    Log.w("Intent", "restoreFromXml: unknown name=" + name);
                    XmlUtils.skipCurrentTag(in);
                }
            }
        }
        return intent;
    }

    public static String normalizeMimeType(String type) {
        if (type == null) {
            return null;
        }
        String type2 = type.trim().toLowerCase(Locale.ROOT);
        int semicolonIndex = type2.indexOf(59);
        if (semicolonIndex != -1) {
            type2 = type2.substring(0, semicolonIndex);
        }
        return type2;
    }

    public void prepareToLeaveProcess(Context context) {
        prepareToLeaveProcess(this.mComponent == null || !Objects.equals(this.mComponent.getPackageName(), context.getPackageName()));
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x012c, code lost:
        if (r1.equals(ACTION_PROVIDER_CHANGED) == false) goto L_0x0139;
     */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A[RETURN, SYNTHETIC] */
    public void prepareToLeaveProcess(boolean leavingPackage) {
        char c;
        boolean z = false;
        setAllowFds(false);
        if (this.mSelector != null) {
            this.mSelector.prepareToLeaveProcess(leavingPackage);
        }
        if (this.mClipData != null) {
            this.mClipData.prepareToLeaveProcess(leavingPackage, getFlags());
        }
        if (this.mExtras != null && !this.mExtras.isParcelled()) {
            Object intent = this.mExtras.get(EXTRA_INTENT);
            if (intent instanceof Intent) {
                ((Intent) intent).prepareToLeaveProcess(leavingPackage);
            }
        }
        if (this.mAction != null && this.mData != null && StrictMode.vmFileUriExposureEnabled() && leavingPackage) {
            String str = this.mAction;
            switch (str.hashCode()) {
                case -1823790459:
                    if (str.equals(ACTION_MEDIA_SHARED)) {
                        c = 5;
                        break;
                    }
                case -1665311200:
                    if (str.equals(ACTION_MEDIA_REMOVED)) {
                        c = 0;
                        break;
                    }
                case -1514214344:
                    if (str.equals(ACTION_MEDIA_MOUNTED)) {
                        c = 4;
                        break;
                    }
                case -1142424621:
                    if (str.equals(ACTION_MEDIA_SCANNER_FINISHED)) {
                        c = 11;
                        break;
                    }
                case -963871873:
                    if (str.equals(ACTION_MEDIA_UNMOUNTED)) {
                        c = 1;
                        break;
                    }
                case -625887599:
                    if (str.equals(ACTION_MEDIA_EJECT)) {
                        c = 9;
                        break;
                    }
                case 257177710:
                    if (str.equals(ACTION_MEDIA_NOFS)) {
                        c = 3;
                        break;
                    }
                case 410719838:
                    if (str.equals(ACTION_MEDIA_UNSHARED)) {
                        c = 6;
                        break;
                    }
                case 582421979:
                    if (str.equals(ACTION_PACKAGE_NEEDS_VERIFICATION)) {
                        c = 13;
                        break;
                    }
                case 852070077:
                    if (str.equals(ACTION_MEDIA_SCANNER_SCAN_FILE)) {
                        c = 12;
                        break;
                    }
                case 1412829408:
                    if (str.equals(ACTION_MEDIA_SCANNER_STARTED)) {
                        c = 10;
                        break;
                    }
                case 1431947322:
                    if (str.equals(ACTION_MEDIA_UNMOUNTABLE)) {
                        c = 8;
                        break;
                    }
                case 1920444806:
                    if (str.equals(ACTION_PACKAGE_VERIFIED)) {
                        c = 14;
                        break;
                    }
                case 1964681210:
                    if (str.equals(ACTION_MEDIA_CHECKING)) {
                        c = 2;
                        break;
                    }
                case 2045140818:
                    if (str.equals(ACTION_MEDIA_BAD_REMOVAL)) {
                        c = 7;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    break;
                default:
                    this.mData.checkFileUriExposed("Intent.getData()");
                    break;
            }
        }
        if (this.mAction != null && this.mData != null && StrictMode.vmContentUriWithoutPermissionEnabled() && leavingPackage) {
            String str2 = this.mAction;
            int hashCode = str2.hashCode();
            if (hashCode != -577088908) {
                if (hashCode == 1662413067) {
                }
            } else if (str2.equals("android.provider.action.QUICK_CONTACT")) {
                z = true;
                switch (z) {
                    case false:
                    case true:
                        return;
                    default:
                        this.mData.checkContentUriWithoutPermission("Intent.getData()", getFlags());
                        return;
                }
            }
            z = true;
            switch (z) {
                case false:
                case true:
                    break;
            }
        }
    }

    public void prepareToEnterProcess() {
        setDefusable(true);
        if (this.mSelector != null) {
            this.mSelector.prepareToEnterProcess();
        }
        if (this.mClipData != null) {
            this.mClipData.prepareToEnterProcess();
        }
        if (this.mContentUserHint != -2 && UserHandle.getAppId(Process.myUid()) != 1000) {
            fixUris(this.mContentUserHint);
            this.mContentUserHint = -2;
        }
    }

    public boolean hasWebURI() {
        boolean z = false;
        if (getData() == null) {
            return false;
        }
        String scheme = getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return false;
        }
        if (scheme.equals(IntentFilter.SCHEME_HTTP) || scheme.equals(IntentFilter.SCHEME_HTTPS)) {
            z = true;
        }
        return z;
    }

    public boolean isWebIntent() {
        return "android.intent.action.VIEW".equals(this.mAction) && hasWebURI();
    }

    public void fixUris(int contentUserHint) {
        Uri data = getData();
        if (data != null) {
            this.mData = ContentProvider.maybeAddUserId(data, contentUserHint);
        }
        if (this.mClipData != null) {
            this.mClipData.fixUris(contentUserHint);
        }
        String action = getAction();
        if (ACTION_SEND.equals(action)) {
            Uri stream = (Uri) getParcelableExtra(EXTRA_STREAM);
            if (stream != null) {
                putExtra(EXTRA_STREAM, (Parcelable) ContentProvider.maybeAddUserId(stream, contentUserHint));
            }
        } else if (ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> streams = getParcelableArrayListExtra(EXTRA_STREAM);
            if (streams != null) {
                ArrayList<Uri> newStreams = new ArrayList<>();
                for (int i = 0; i < streams.size(); i++) {
                    newStreams.add(ContentProvider.maybeAddUserId(streams.get(i), contentUserHint));
                }
                putParcelableArrayListExtra(EXTRA_STREAM, newStreams);
            }
        } else if ("android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action) || "android.media.action.VIDEO_CAPTURE".equals(action)) {
            Uri output = (Uri) getParcelableExtra("output");
            if (output != null) {
                putExtra("output", (Parcelable) ContentProvider.maybeAddUserId(output, contentUserHint));
            }
        }
    }

    public boolean migrateExtraStreamToClipData() {
        if ((this.mExtras != null && this.mExtras.isParcelled()) || getClipData() != null) {
            return false;
        }
        String action = getAction();
        if (ACTION_CHOOSER.equals(action) || "com.huawei.intent.action.hwCHOOSER".equals(action)) {
            boolean migrated = false;
            try {
                Intent intent = (Intent) getParcelableExtra(EXTRA_INTENT);
                if (intent != null) {
                    migrated = false | intent.migrateExtraStreamToClipData();
                    if (migrated && "com.huawei.intent.action.hwCHOOSER".equals(action)) {
                        ClipData clipData = intent.getClipData();
                        try {
                            int itemCount = clipData.getItemCount();
                            for (int i = 0; i < itemCount; i++) {
                                try {
                                    ActivityManagerNative.getDefault().grantUriPermission(ActivityThread.currentActivityThread().getApplicationThread(), "com.huawei.android.internal.app", clipData.getItemAt(i).getUri(), 1, UserHandle.myUserId());
                                } catch (RemoteException e) {
                                    Log.w("Intent", "Failure when grantUriPermission", e);
                                }
                            }
                        } catch (Exception e2) {
                            Log.w("Intent", "Failure when grantUriPermission", e2);
                        }
                    }
                }
            } catch (ClassCastException e3) {
            }
            try {
                Parcelable[] intents = getParcelableArrayExtra(EXTRA_INITIAL_INTENTS);
                if (intents != null) {
                    for (Parcelable parcelable : intents) {
                        Intent intent2 = (Intent) parcelable;
                        if (intent2 != null) {
                            migrated |= intent2.migrateExtraStreamToClipData();
                        }
                    }
                }
            } catch (ClassCastException e4) {
            }
            return migrated;
        }
        if (ACTION_SEND.equals(action)) {
            try {
                Uri stream = (Uri) getParcelableExtra(EXTRA_STREAM);
                CharSequence text = getCharSequenceExtra(EXTRA_TEXT);
                String htmlText = getStringExtra(EXTRA_HTML_TEXT);
                if (!(stream == null && text == null && htmlText == null)) {
                    setClipData(new ClipData(null, new String[]{getType()}, new ClipData.Item(text, htmlText, null, stream)));
                    addFlags(1);
                    return true;
                }
            } catch (ClassCastException e5) {
            }
        } else if (ACTION_SEND_MULTIPLE.equals(action)) {
            try {
                ArrayList<Uri> streams = getParcelableArrayListExtra(EXTRA_STREAM);
                ArrayList<CharSequence> texts = getCharSequenceArrayListExtra(EXTRA_TEXT);
                ArrayList<String> htmlTexts = getStringArrayListExtra(EXTRA_HTML_TEXT);
                int num = -1;
                if (streams != null) {
                    num = streams.size();
                }
                if (texts != null) {
                    if (num >= 0 && num != texts.size()) {
                        return false;
                    }
                    num = texts.size();
                }
                if (htmlTexts != null) {
                    if (num >= 0 && num != htmlTexts.size()) {
                        return false;
                    }
                    num = htmlTexts.size();
                }
                if (num > 0) {
                    ClipData clipData2 = new ClipData(null, new String[]{getType()}, makeClipItem(streams, texts, htmlTexts, 0));
                    for (int i2 = 1; i2 < num; i2++) {
                        clipData2.addItem(makeClipItem(streams, texts, htmlTexts, i2));
                    }
                    setClipData(clipData2);
                    addFlags(1);
                    return true;
                }
            } catch (ClassCastException e6) {
            }
        } else if ("android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action) || "android.media.action.VIDEO_CAPTURE".equals(action)) {
            try {
                Uri output = (Uri) getParcelableExtra("output");
                if (output != null) {
                    setClipData(ClipData.newRawUri("", output));
                    addFlags(3);
                    return true;
                }
            } catch (ClassCastException e7) {
                return false;
            }
        }
        return false;
    }

    public static String dockStateToString(int dock) {
        switch (dock) {
            case 0:
                return "EXTRA_DOCK_STATE_UNDOCKED";
            case 1:
                return "EXTRA_DOCK_STATE_DESK";
            case 2:
                return "EXTRA_DOCK_STATE_CAR";
            case 3:
                return "EXTRA_DOCK_STATE_LE_DESK";
            case 4:
                return "EXTRA_DOCK_STATE_HE_DESK";
            default:
                return Integer.toString(dock);
        }
    }

    private static ClipData.Item makeClipItem(ArrayList<Uri> streams, ArrayList<CharSequence> texts, ArrayList<String> htmlTexts, int which) {
        return new ClipData.Item(texts != null ? texts.get(which) : null, htmlTexts != null ? htmlTexts.get(which) : null, null, streams != null ? streams.get(which) : null);
    }

    public boolean isDocument() {
        return (this.mFlags & 524288) == 524288;
    }

    public static String toPkgClsString(Intent intent) {
        return toPkgClsString(intent, "");
    }

    public static String toPkgClsString(Intent intent, String prefix) {
        StringBuilder sb = new StringBuilder(128);
        if (intent != null) {
            if (intent.getComponent() != null) {
                sb.append(toPkgClsString(intent.getComponent(), prefix));
            } else {
                sb.append(SQLiteAuditLog.AuditLog.PID);
                sb.append("=");
                sb.append(Process.myPid());
                if (intent.getAction() != null) {
                    ActivityInfo aInfo = null;
                    try {
                        long start = System.nanoTime();
                        ResolveInfo rInfo = AppGlobals.getPackageManager().resolveIntent(intent, null, 66560, 0);
                        sb.append("&");
                        sb.append("tm=");
                        sb.append(System.nanoTime() - start);
                        if (rInfo != null) {
                            aInfo = rInfo.activityInfo;
                        }
                    } catch (RemoteException e) {
                    }
                    if (!(aInfo == null || aInfo.applicationInfo == null)) {
                        sb.append("&");
                        sb.append(prefix);
                        sb.append("pkg=");
                        sb.append(aInfo.applicationInfo.packageName);
                        sb.append("&");
                        sb.append(prefix);
                        sb.append("cls=");
                        sb.append(aInfo.name);
                    }
                }
            }
        }
        if (sb.length() == 0) {
            sb.append(SQLiteAuditLog.AuditLog.PID);
            sb.append(Process.myPid());
        }
        return sb.toString();
    }

    public static String toPkgClsString(ComponentName componentName) {
        return toPkgClsString(componentName, "");
    }

    public static String toPkgClsString(ComponentName componentName, String prefix) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(prefix);
        sb.append("pid=");
        sb.append(Process.myPid());
        if (componentName != null) {
            if (componentName.getPackageName() != null) {
                sb.append("&");
                sb.append(prefix);
                sb.append("pkg=");
                sb.append(componentName.getPackageName());
            }
            if (componentName.getClassName() != null) {
                sb.append("&");
                sb.append(prefix);
                sb.append("cls=");
                sb.append(componentName.getClassName());
            }
        }
        return sb.toString();
    }
}
