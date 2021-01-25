package ohos.sysappcomponents.settings;

import android.content.ContentResolver;
import android.provider.Settings;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class SystemSettingsProxy {
    private static final String GLOBAL = "global";
    private static final String SECURE = "secure";
    private static final String SYSTEM = "system";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110211, "SystemSettingsProxy");

    private SystemSettingsProxy() {
    }

    public static Uri getUri(String str) {
        String settingsSource = SystemSettingsUtils.getSettingsSource(str);
        if (settingsSource.isEmpty()) {
            return System.getUri(str);
        }
        char c = 65535;
        int hashCode = settingsSource.hashCode();
        if (hashCode != -1243020381) {
            if (hashCode != -906273929) {
                if (hashCode == -887328209 && settingsSource.equals(SYSTEM)) {
                    c = 0;
                }
            } else if (settingsSource.equals(SECURE)) {
                c = 1;
            }
        } else if (settingsSource.equals(GLOBAL)) {
            c = 2;
        }
        if (c == 0) {
            return System.getUri(str);
        }
        if (c == 1) {
            return Secure.getUri(str);
        }
        if (c != 2) {
            return null;
        }
        return Global.getUri(str);
    }

    public static String getValue(DataAbilityHelper dataAbilityHelper, String str) {
        String settingsSource = SystemSettingsUtils.getSettingsSource(str);
        if (settingsSource.isEmpty()) {
            return System.getStringItem(dataAbilityHelper, str);
        }
        char c = 65535;
        int hashCode = settingsSource.hashCode();
        if (hashCode != -1243020381) {
            if (hashCode != -906273929) {
                if (hashCode == -887328209 && settingsSource.equals(SYSTEM)) {
                    c = 0;
                }
            } else if (settingsSource.equals(SECURE)) {
                c = 1;
            }
        } else if (settingsSource.equals(GLOBAL)) {
            c = 2;
        }
        if (c == 0) {
            return System.getStringItem(dataAbilityHelper, str);
        }
        if (c == 1) {
            return Secure.getStringItem(dataAbilityHelper, str);
        }
        if (c != 2) {
            return null;
        }
        return Global.getStringItem(dataAbilityHelper, str);
    }

    public static boolean setValue(DataAbilityHelper dataAbilityHelper, String str, String str2) {
        String settingsSource = SystemSettingsUtils.getSettingsSource(str);
        if (settingsSource.isEmpty()) {
            return System.setStringItem(dataAbilityHelper, str, str2);
        }
        char c = 65535;
        int hashCode = settingsSource.hashCode();
        if (hashCode != -1243020381) {
            if (hashCode != -906273929) {
                if (hashCode == -887328209 && settingsSource.equals(SYSTEM)) {
                    c = 0;
                }
            } else if (settingsSource.equals(SECURE)) {
                c = 1;
            }
        } else if (settingsSource.equals(GLOBAL)) {
            c = 2;
        }
        if (c == 0) {
            return System.setStringItem(dataAbilityHelper, str, str2);
        }
        if (c == 1) {
            return Secure.setStringItem(dataAbilityHelper, str, str2);
        }
        if (c != 2) {
            return false;
        }
        return Global.setStringItem(dataAbilityHelper, str, str2);
    }

    public static boolean checkSetPermission(Context context) {
        if (context == null) {
            HiLog.error(TAG, "Context is null!", new Object[0]);
            return false;
        }
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            return Settings.System.canWrite((android.content.Context) hostContext);
        }
        HiLog.error(TAG, "Illegal Context!", new Object[0]);
        return false;
    }

    public static boolean canShowFloating(Context context) {
        if (context == null) {
            HiLog.error(TAG, "Context is null!", new Object[0]);
            return false;
        }
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            return Settings.canDrawOverlays((android.content.Context) hostContext);
        }
        HiLog.error(TAG, "Illegal Context!", new Object[0]);
        return false;
    }

    public static class System {
        private static final HiLogLabel TAG = new HiLogLabel(3, 218110211, "SystemSettingsProxy.System");

        private System() {
        }

        public static String getStringItem(DataAbilityHelper dataAbilityHelper, String str) {
            ContentResolver creatFromDataAbilityHelper = SystemSettingsUtils.creatFromDataAbilityHelper(dataAbilityHelper);
            if (creatFromDataAbilityHelper != null) {
                return Settings.System.getString(creatFromDataAbilityHelper, SystemSettingsUtils.getMappedName(str));
            }
            HiLog.error(TAG, "Illegal dataAbilityHelper!", new Object[0]);
            return null;
        }

        public static boolean setStringItem(DataAbilityHelper dataAbilityHelper, String str, String str2) {
            ContentResolver creatFromDataAbilityHelper = SystemSettingsUtils.creatFromDataAbilityHelper(dataAbilityHelper);
            if (creatFromDataAbilityHelper != null) {
                return Settings.System.putString(creatFromDataAbilityHelper, SystemSettingsUtils.getMappedName(str), str2);
            }
            HiLog.error(TAG, "Illegal dataAbilityHelper!", new Object[0]);
            return false;
        }

        public static Uri getUri(String str) {
            return UriConverter.convertToZidaneContentUri(Settings.System.getUriFor(SystemSettingsUtils.getMappedName(str)), "");
        }
    }

    public static class Secure {
        private static final HiLogLabel TAG = new HiLogLabel(3, 218110211, "SystemSettingsProxy.Secure");

        private Secure() {
        }

        public static String getStringItem(DataAbilityHelper dataAbilityHelper, String str) {
            ContentResolver creatFromDataAbilityHelper = SystemSettingsUtils.creatFromDataAbilityHelper(dataAbilityHelper);
            if (creatFromDataAbilityHelper != null) {
                return Settings.Secure.getString(creatFromDataAbilityHelper, SystemSettingsUtils.getMappedName(str));
            }
            HiLog.error(TAG, "Illegal dataAbilityHelper!", new Object[0]);
            return null;
        }

        public static boolean setStringItem(DataAbilityHelper dataAbilityHelper, String str, String str2) {
            ContentResolver creatFromDataAbilityHelper = SystemSettingsUtils.creatFromDataAbilityHelper(dataAbilityHelper);
            if (creatFromDataAbilityHelper != null) {
                return Settings.Secure.putString(creatFromDataAbilityHelper, SystemSettingsUtils.getMappedName(str), str2);
            }
            HiLog.error(TAG, "Illegal dataAbilityHelper!", new Object[0]);
            return false;
        }

        public static Uri getUri(String str) {
            return UriConverter.convertToZidaneContentUri(Settings.Secure.getUriFor(SystemSettingsUtils.getMappedName(str)), "");
        }
    }

    public static class Global {
        private static final HiLogLabel TAG = new HiLogLabel(3, 218110211, "SystemSettingsProxy.Global");

        private Global() {
        }

        public static String getStringItem(DataAbilityHelper dataAbilityHelper, String str) {
            ContentResolver creatFromDataAbilityHelper = SystemSettingsUtils.creatFromDataAbilityHelper(dataAbilityHelper);
            if (creatFromDataAbilityHelper != null) {
                return Settings.Global.getString(creatFromDataAbilityHelper, SystemSettingsUtils.getMappedName(str));
            }
            HiLog.error(TAG, "Illegal dataAbilityHelper!", new Object[0]);
            return null;
        }

        public static boolean setStringItem(DataAbilityHelper dataAbilityHelper, String str, String str2) {
            ContentResolver creatFromDataAbilityHelper = SystemSettingsUtils.creatFromDataAbilityHelper(dataAbilityHelper);
            if (creatFromDataAbilityHelper != null) {
                return Settings.Global.putString(creatFromDataAbilityHelper, SystemSettingsUtils.getMappedName(str), str2);
            }
            HiLog.error(TAG, "Illegal dataAbilityHelper!", new Object[0]);
            return false;
        }

        public static Uri getUri(String str) {
            return UriConverter.convertToZidaneContentUri(Settings.Global.getUriFor(SystemSettingsUtils.getMappedName(str)), "");
        }
    }
}
