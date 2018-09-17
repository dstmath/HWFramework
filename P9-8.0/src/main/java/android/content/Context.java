package android.content;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.ViewDebug.ExportedProperty;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class Context {
    public static final String ACCESSIBILITY_SERVICE = "accessibility";
    public static final String ACCOUNT_SERVICE = "account";
    public static final String ACTIVITY_SERVICE = "activity";
    public static final String ALARM_SERVICE = "alarm";
    public static final String APPWIDGET_SERVICE = "appwidget";
    public static final String APP_OPS_SERVICE = "appops";
    public static final String AUDIO_SERVICE = "audio";
    public static final String AUTOFILL_MANAGER_SERVICE = "autofill";
    public static final String BACKUP_SERVICE = "backup";
    public static final String BATTERY_SERVICE = "batterymanager";
    public static final int BIND_ABOVE_CLIENT = 8;
    public static final int BIND_ADJUST_WITH_ACTIVITY = 128;
    public static final int BIND_ALLOW_OOM_MANAGEMENT = 16;
    public static final int BIND_ALLOW_WHITELIST_MANAGEMENT = 16777216;
    public static final int BIND_AUTO_CREATE = 1;
    public static final int BIND_DEBUG_UNBIND = 2;
    public static final int BIND_EXTERNAL_SERVICE = Integer.MIN_VALUE;
    public static final int BIND_FOREGROUND_SERVICE = 67108864;
    public static final int BIND_FOREGROUND_SERVICE_WHILE_AWAKE = 33554432;
    public static final int BIND_IMPORTANT = 64;
    public static final int BIND_IMPORTANT_BACKGROUND = 8388608;
    public static final int BIND_NOT_FOREGROUND = 4;
    public static final int BIND_NOT_VISIBLE = 1073741824;
    public static final int BIND_SHOWING_UI = 536870912;
    public static final int BIND_TREAT_LIKE_ACTIVITY = 134217728;
    public static final int BIND_VISIBLE = 268435456;
    public static final int BIND_WAIVE_PRIORITY = 32;
    public static final String BLUETOOTH_SERVICE = "bluetooth";
    public static final String CAMERA_SERVICE = "camera";
    public static final String CAPTIONING_SERVICE = "captioning";
    public static final String CARRIER_CONFIG_SERVICE = "carrier_config";
    public static final String CLIPBOARD_SERVICE = "clipboard";
    public static final String COMPANION_DEVICE_SERVICE = "companiondevice";
    public static final String CONNECTIVITY_SERVICE = "connectivity";
    public static final String CONSUMER_IR_SERVICE = "consumer_ir";
    public static final String CONTEXTHUB_SERVICE = "contexthub";
    public static final int CONTEXT_CREDENTIAL_PROTECTED_STORAGE = 16;
    public static final int CONTEXT_DEVICE_PROTECTED_STORAGE = 8;
    public static final int CONTEXT_IGNORE_SECURITY = 2;
    public static final int CONTEXT_INCLUDE_CODE = 1;
    public static final int CONTEXT_REGISTER_PACKAGE = 1073741824;
    public static final int CONTEXT_RESTRICTED = 4;
    public static final String COUNTRY_DETECTOR = "country_detector";
    public static final String DEVICE_IDENTIFIERS_SERVICE = "device_identifiers";
    public static final String DEVICE_IDLE_CONTROLLER = "deviceidle";
    public static final String DEVICE_POLICY_SERVICE = "device_policy";
    public static final String DISPLAY_SERVICE = "display";
    public static final String DOWNLOAD_SERVICE = "download";
    public static final String DROPBOX_SERVICE = "dropbox";
    public static final String DTV_SERVICE = "dtvservice";
    public static final String ETHERNET_SERVICE = "ethernet";
    public static final String FINGERPRINT_SERVICE = "fingerprint";
    public static final String GATEKEEPER_SERVICE = "android.service.gatekeeper.IGateKeeperService";
    public static final String HARDWARE_PROPERTIES_SERVICE = "hardware_properties";
    public static final String HDMI_CONTROL_SERVICE = "hdmi_control";
    public static final String INCIDENT_SERVICE = "incident";
    public static final String INPUT_METHOD_SERVICE = "input_method";
    public static final String INPUT_SERVICE = "input";
    public static final String IPSEC_SERVICE = "ipsec";
    public static final String JOB_SCHEDULER_SERVICE = "jobscheduler";
    public static final String KEYGUARD_SERVICE = "keyguard";
    public static final String LAUNCHER_APPS_SERVICE = "launcherapps";
    public static final String LAYOUT_INFLATER_SERVICE = "layout_inflater";
    public static final String LOCATION_SERVICE = "location";
    public static final String MEDIA_PROJECTION_SERVICE = "media_projection";
    public static final String MEDIA_ROUTER_SERVICE = "media_router";
    public static final String MEDIA_SESSION_SERVICE = "media_session";
    public static final String MIDI_SERVICE = "midi";
    public static final int MODE_APPEND = 32768;
    public static final int MODE_ENABLE_WRITE_AHEAD_LOGGING = 8;
    @Deprecated
    public static final int MODE_MULTI_PROCESS = 4;
    public static final int MODE_NO_LOCALIZED_COLLATORS = 16;
    public static final int MODE_PRIVATE = 0;
    @Deprecated
    public static final int MODE_WORLD_READABLE = 1;
    @Deprecated
    public static final int MODE_WORLD_WRITEABLE = 2;
    public static final String NETWORKMANAGEMENT_SERVICE = "network_management";
    public static final String NETWORK_POLICY_SERVICE = "netpolicy";
    public static final String NETWORK_SCORE_SERVICE = "network_score";
    public static final String NETWORK_STATS_SERVICE = "netstats";
    public static final String NFC_SERVICE = "nfc";
    public static final String NOTIFICATION_SERVICE = "notification";
    public static final String NSD_SERVICE = "servicediscovery";
    public static final String OEM_LOCK_SERVICE = "oem_lock";
    public static final String OVERLAY_SERVICE = "overlay";
    public static final String PERSISTENT_DATA_BLOCK_SERVICE = "persistent_data_block";
    public static final String POWER_SERVICE = "power";
    public static final String PRINT_SERVICE = "print";
    public static final String RADIO_SERVICE = "radio";
    private static final int REALVNC_MIRRORLINKTOGO_PATCH_VERSION = 1;
    public static final int RECEIVER_VISIBLE_TO_INSTANT_APPS = 1;
    public static final String RECOVERY_SERVICE = "recovery";
    public static final String RESTRICTIONS_SERVICE = "restrictions";
    public static final String SEARCH_SERVICE = "search";
    public static final String SENSOR_SERVICE = "sensor";
    public static final String SERIAL_SERVICE = "serial";
    public static final String SHORTCUT_SERVICE = "shortcut";
    public static final String SIP_SERVICE = "sip";
    public static final String SOUND_TRIGGER_SERVICE = "soundtrigger";
    public static final String STATUS_BAR_SERVICE = "statusbar";
    public static final String STORAGE_SERVICE = "storage";
    public static final String STORAGE_STATS_SERVICE = "storagestats";
    public static final String SYSTEM_HEALTH_SERVICE = "systemhealth";
    public static final String TELECOM_SERVICE = "telecom";
    public static final String TELEPHONY_SERVICE = "phone";
    public static final String TELEPHONY_SUBSCRIPTION_SERVICE = "telephony_subscription_service";
    public static final String TEXT_CLASSIFICATION_SERVICE = "textclassification";
    public static final String TEXT_SERVICES_MANAGER_SERVICE = "textservices";
    public static final String TRUST_SERVICE = "trust";
    public static final String TV_INPUT_SERVICE = "tv_input";
    public static final String UI_MODE_SERVICE = "uimode";
    public static final String UPDATE_LOCK_SERVICE = "updatelock";
    public static final String USAGE_STATS_SERVICE = "usagestats";
    public static final String USB_SERVICE = "usb";
    public static final String USER_SERVICE = "user";
    public static final String VIBRATOR_SERVICE = "vibrator";
    public static final String VOICE_INTERACTION_MANAGER_SERVICE = "voiceinteraction";
    public static final String VR_SERVICE = "vrmanager";
    public static final String WALLPAPER_SERVICE = "wallpaper";
    public static final String WIFI_AWARE_SERVICE = "wifiaware";
    public static final String WIFI_P2P_SERVICE = "wifip2p";
    public static final String WIFI_RTT_SERVICE = "rttmanager";
    public static final String WIFI_SCANNING_SERVICE = "wifiscanner";
    public static final String WIFI_SERVICE = "wifi";
    public static final String WINDOW_SERVICE = "window";
    private static int sLastAutofillId = -1;

    public abstract boolean bindService(Intent intent, ServiceConnection serviceConnection, int i);

    public abstract boolean canLoadUnsafeResources();

    public abstract int checkCallingOrSelfPermission(String str);

    public abstract int checkCallingOrSelfUriPermission(Uri uri, int i);

    public abstract int checkCallingPermission(String str);

    public abstract int checkCallingUriPermission(Uri uri, int i);

    public abstract int checkPermission(String str, int i, int i2);

    public abstract int checkPermission(String str, int i, int i2, IBinder iBinder);

    public abstract int checkSelfPermission(String str);

    public abstract int checkUriPermission(Uri uri, int i, int i2, int i3);

    public abstract int checkUriPermission(Uri uri, int i, int i2, int i3, IBinder iBinder);

    public abstract int checkUriPermission(Uri uri, String str, String str2, int i, int i2, int i3);

    @Deprecated
    public abstract void clearWallpaper() throws IOException;

    public abstract Context createApplicationContext(ApplicationInfo applicationInfo, int i) throws NameNotFoundException;

    public abstract Context createConfigurationContext(Configuration configuration);

    public abstract Context createContextForSplit(String str) throws NameNotFoundException;

    public abstract Context createCredentialProtectedStorageContext();

    public abstract Context createDeviceProtectedStorageContext();

    public abstract Context createDisplayContext(Display display);

    public abstract Context createPackageContext(String str, int i) throws NameNotFoundException;

    public abstract Context createPackageContextAsUser(String str, int i, UserHandle userHandle) throws NameNotFoundException;

    public abstract String[] databaseList();

    public abstract boolean deleteDatabase(String str);

    public abstract boolean deleteFile(String str);

    public abstract boolean deleteSharedPreferences(String str);

    public abstract void enforceCallingOrSelfPermission(String str, String str2);

    public abstract void enforceCallingOrSelfUriPermission(Uri uri, int i, String str);

    public abstract void enforceCallingPermission(String str, String str2);

    public abstract void enforceCallingUriPermission(Uri uri, int i, String str);

    public abstract void enforcePermission(String str, int i, int i2, String str2);

    public abstract void enforceUriPermission(Uri uri, int i, int i2, int i3, String str);

    public abstract void enforceUriPermission(Uri uri, String str, String str2, int i, int i2, int i3, String str3);

    public abstract String[] fileList();

    public abstract Context getApplicationContext();

    public abstract ApplicationInfo getApplicationInfo();

    public abstract AssetManager getAssets();

    public abstract String getBasePackageName();

    public abstract File getCacheDir();

    public abstract ClassLoader getClassLoader();

    public abstract File getCodeCacheDir();

    public abstract ContentResolver getContentResolver();

    public abstract File getDataDir();

    public abstract File getDatabasePath(String str);

    public abstract File getDir(String str, int i);

    public abstract Display getDisplay();

    public abstract DisplayAdjustments getDisplayAdjustments(int i);

    public abstract File getExternalCacheDir();

    public abstract File[] getExternalCacheDirs();

    public abstract File getExternalFilesDir(String str);

    public abstract File[] getExternalFilesDirs(String str);

    public abstract File[] getExternalMediaDirs();

    public abstract File getFileStreamPath(String str);

    public abstract File getFilesDir();

    public abstract Looper getMainLooper();

    public abstract File getNoBackupFilesDir();

    public abstract File getObbDir();

    public abstract File[] getObbDirs();

    public abstract String getOpPackageName();

    public abstract String getPackageCodePath();

    public abstract PackageManager getPackageManager();

    public abstract String getPackageName();

    public abstract String getPackageResourcePath();

    public abstract File getPreloadsFileCache();

    public abstract Resources getResources();

    public abstract SharedPreferences getSharedPreferences(File file, int i);

    public abstract SharedPreferences getSharedPreferences(String str, int i);

    public abstract File getSharedPreferencesPath(String str);

    public abstract Object getSystemService(String str);

    public abstract String getSystemServiceName(Class<?> cls);

    @ExportedProperty(deepExport = true)
    public abstract Theme getTheme();

    public abstract int getUserId();

    @Deprecated
    public abstract Drawable getWallpaper();

    @Deprecated
    public abstract int getWallpaperDesiredMinimumHeight();

    @Deprecated
    public abstract int getWallpaperDesiredMinimumWidth();

    public abstract void grantUriPermission(String str, Uri uri, int i);

    public abstract boolean isCredentialProtectedStorage();

    public abstract boolean isDeviceProtectedStorage();

    public abstract boolean moveDatabaseFrom(Context context, String str);

    public abstract boolean moveSharedPreferencesFrom(Context context, String str);

    public abstract FileInputStream openFileInput(String str) throws FileNotFoundException;

    public abstract FileOutputStream openFileOutput(String str, int i) throws FileNotFoundException;

    public abstract SQLiteDatabase openOrCreateDatabase(String str, int i, CursorFactory cursorFactory);

    public abstract SQLiteDatabase openOrCreateDatabase(String str, int i, CursorFactory cursorFactory, DatabaseErrorHandler databaseErrorHandler);

    @Deprecated
    public abstract Drawable peekWallpaper();

    public abstract Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter);

    public abstract Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, int i);

    public abstract Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, String str, Handler handler);

    public abstract Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, String str, Handler handler, int i);

    public abstract Intent registerReceiverAsUser(BroadcastReceiver broadcastReceiver, UserHandle userHandle, IntentFilter intentFilter, String str, Handler handler);

    @Deprecated
    public abstract void removeStickyBroadcast(Intent intent);

    @Deprecated
    public abstract void removeStickyBroadcastAsUser(Intent intent, UserHandle userHandle);

    public abstract void revokeUriPermission(Uri uri, int i);

    public abstract void revokeUriPermission(String str, Uri uri, int i);

    public abstract void sendBroadcast(Intent intent);

    public abstract void sendBroadcast(Intent intent, String str);

    public abstract void sendBroadcast(Intent intent, String str, int i);

    public abstract void sendBroadcast(Intent intent, String str, Bundle bundle);

    public abstract void sendBroadcastAsUser(Intent intent, UserHandle userHandle);

    public abstract void sendBroadcastAsUser(Intent intent, UserHandle userHandle, String str);

    public abstract void sendBroadcastAsUser(Intent intent, UserHandle userHandle, String str, int i);

    public abstract void sendBroadcastAsUser(Intent intent, UserHandle userHandle, String str, Bundle bundle);

    public abstract void sendBroadcastMultiplePermissions(Intent intent, String[] strArr);

    public abstract void sendOrderedBroadcast(Intent intent, String str);

    public abstract void sendOrderedBroadcast(Intent intent, String str, int i, BroadcastReceiver broadcastReceiver, Handler handler, int i2, String str2, Bundle bundle);

    public abstract void sendOrderedBroadcast(Intent intent, String str, BroadcastReceiver broadcastReceiver, Handler handler, int i, String str2, Bundle bundle);

    public abstract void sendOrderedBroadcast(Intent intent, String str, Bundle bundle, BroadcastReceiver broadcastReceiver, Handler handler, int i, String str2, Bundle bundle2);

    public abstract void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, String str, int i, BroadcastReceiver broadcastReceiver, Handler handler, int i2, String str2, Bundle bundle);

    public abstract void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, String str, int i, Bundle bundle, BroadcastReceiver broadcastReceiver, Handler handler, int i2, String str2, Bundle bundle2);

    public abstract void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, String str, BroadcastReceiver broadcastReceiver, Handler handler, int i, String str2, Bundle bundle);

    @Deprecated
    public abstract void sendStickyBroadcast(Intent intent);

    @Deprecated
    public abstract void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle);

    @Deprecated
    public abstract void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle, Bundle bundle);

    @Deprecated
    public abstract void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver broadcastReceiver, Handler handler, int i, String str, Bundle bundle);

    @Deprecated
    public abstract void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, BroadcastReceiver broadcastReceiver, Handler handler, int i, String str, Bundle bundle);

    public abstract void setTheme(int i);

    @Deprecated
    public abstract void setWallpaper(Bitmap bitmap) throws IOException;

    @Deprecated
    public abstract void setWallpaper(InputStream inputStream) throws IOException;

    public abstract void startActivities(Intent[] intentArr);

    public abstract void startActivities(Intent[] intentArr, Bundle bundle);

    public abstract void startActivity(Intent intent);

    public abstract void startActivity(Intent intent, Bundle bundle);

    public abstract ComponentName startForegroundService(Intent intent);

    public abstract ComponentName startForegroundServiceAsUser(Intent intent, UserHandle userHandle);

    public abstract boolean startInstrumentation(ComponentName componentName, String str, Bundle bundle);

    public abstract void startIntentSender(IntentSender intentSender, Intent intent, int i, int i2, int i3) throws SendIntentException;

    public abstract void startIntentSender(IntentSender intentSender, Intent intent, int i, int i2, int i3, Bundle bundle) throws SendIntentException;

    public abstract ComponentName startService(Intent intent);

    public abstract ComponentName startServiceAsUser(Intent intent, UserHandle userHandle);

    public abstract boolean stopService(Intent intent);

    public abstract boolean stopServiceAsUser(Intent intent, UserHandle userHandle);

    public abstract void unbindService(ServiceConnection serviceConnection);

    public abstract void unregisterReceiver(BroadcastReceiver broadcastReceiver);

    public abstract void updateDisplay(int i);

    public int getNextAutofillId() {
        if (sLastAutofillId == 1073741822) {
            sLastAutofillId = -1;
        }
        sLastAutofillId++;
        return sLastAutofillId;
    }

    public void registerComponentCallbacks(ComponentCallbacks callback) {
        getApplicationContext().registerComponentCallbacks(callback);
    }

    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        getApplicationContext().unregisterComponentCallbacks(callback);
    }

    public final CharSequence getText(int resId) {
        return getResources().getText(resId);
    }

    public final String getString(int resId) {
        return getResources().getString(resId);
    }

    public final String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    public final int getColor(int id) {
        return getResources().getColor(id, getTheme());
    }

    public final Drawable getDrawable(int id) {
        return getResources().getDrawable(id, getTheme());
    }

    public final ColorStateList getColorStateList(int id) {
        return getResources().getColorStateList(id, getTheme());
    }

    public int getThemeResId() {
        return 0;
    }

    public final TypedArray obtainStyledAttributes(int[] attrs) {
        return getTheme().obtainStyledAttributes(attrs);
    }

    public final TypedArray obtainStyledAttributes(int resid, int[] attrs) throws NotFoundException {
        return getTheme().obtainStyledAttributes(resid, attrs);
    }

    public final TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs) {
        return getTheme().obtainStyledAttributes(set, attrs, 0, 0);
    }

    public final TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
        return getTheme().obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
    }

    @Deprecated
    public File getSharedPrefsFile(String name) {
        return getSharedPreferencesPath(name);
    }

    public void startActivityAsUser(Intent intent, UserHandle user) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle userId) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public void startActivityForResult(String who, Intent intent, int requestCode, Bundle options) {
        throw new RuntimeException("This method is only implemented for Activity-based Contexts. Check canStartActivityForResult() before calling.");
    }

    public boolean canStartActivityForResult() {
        return false;
    }

    public void startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, UserHandle user) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags, Handler handler, UserHandle user) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public final <T> T getSystemService(Class<T> serviceClass) {
        String serviceName = getSystemServiceName(serviceClass);
        if (serviceName != null) {
            return getSystemService(serviceName);
        }
        return null;
    }

    public boolean isRestricted() {
        return false;
    }

    public IBinder getActivityToken() {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public IServiceConnection getServiceDispatcher(ServiceConnection conn, Handler handler, int flags) {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public IApplicationThread getIApplicationThread() {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public Handler getMainThreadHandler() {
        throw new RuntimeException("Not implemented. Must override in a subclass.");
    }

    public void assertRuntimeOverlayThemable() {
        if (getResources() == Resources.getSystem()) {
            throw new IllegalArgumentException("Non-UI context used to display UI; get a UI context from ActivityThread#getSystemUiContext()");
        }
    }
}
