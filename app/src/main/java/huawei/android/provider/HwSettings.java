package huawei.android.provider;

import android.content.ContentResolver;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.NameValueTable;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import java.util.HashMap;

public final class HwSettings {
    public static final String AUTHORITY = "settings";
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "Settings";

    public static final class Global {
        public static final String SINGLE_HAND_MODE = "single_hand_mode";
        public static final String WIFI_REPEATER_ON = "wifi_repeater_on";
    }

    private static class NameValueCache {
        private static final String NAME_EQ_PLACEHOLDER = "name=?";
        private static final String[] SELECT_VALUE = null;
        private final String mCallCommand;
        private IContentProvider mContentProvider;
        private final Uri mUri;
        private final HashMap<String, String> mValues;
        private long mValuesVersion;
        private final String mVersionSystemProperty;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.provider.HwSettings.NameValueCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.provider.HwSettings.NameValueCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.provider.HwSettings.NameValueCache.<clinit>():void");
        }

        public NameValueCache(String versionSystemProperty, Uri uri, String callCommand) {
            this.mValues = new HashMap();
            this.mValuesVersion = 0;
            this.mContentProvider = null;
            this.mVersionSystemProperty = versionSystemProperty;
            this.mUri = uri;
            this.mCallCommand = callCommand;
        }

        public String getString(ContentResolver cr, String name) {
            long newValuesVersion = SystemProperties.getLong(this.mVersionSystemProperty, 0);
            synchronized (this) {
                if (this.mValuesVersion != newValuesVersion) {
                    this.mValues.clear();
                    this.mValuesVersion = newValuesVersion;
                }
                if (this.mValues.containsKey(name)) {
                    String str = (String) this.mValues.get(name);
                    return str;
                }
                String value;
                synchronized (this) {
                    IContentProvider cp = this.mContentProvider;
                    if (cp == null) {
                        IContentProvider cp2 = cr.acquireProvider(this.mUri.getAuthority());
                        this.mContentProvider = cp2;
                        cp = cp2;
                    }
                }
                if (!(this.mCallCommand == null || cp == null)) {
                    try {
                        Bundle b = cp.call(null, this.mCallCommand, name, null);
                        if (b != null) {
                            value = b.getPairValue();
                            synchronized (this) {
                                this.mValues.put(name, value);
                                return value;
                            }
                        }
                    } catch (RemoteException e) {
                    }
                }
                Cursor cursor = null;
                if (cp != null) {
                    try {
                        cursor = cp.query(null, this.mUri, SELECT_VALUE, NAME_EQ_PLACEHOLDER, new String[]{name}, null, null);
                    } catch (RemoteException e2) {
                        try {
                            Log.w(HwSettings.TAG, "Can't get key " + name + " from " + this.mUri, e2);
                            return null;
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                }
                if (cursor == null) {
                    Log.w(HwSettings.TAG, "Can't get key " + name + " from " + this.mUri);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
                value = cursor.moveToNext() ? cursor.getString(0) : null;
                synchronized (this) {
                    this.mValues.put(name, value);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return value;
            }
        }
    }

    public static final class Secure {
        public static final String WIFI_AP_IGNOREBROADCASTSSID = "wifi_ap_ignorebroadcastssid";

        public Secure() {
        }
    }

    public static final class System {
        public static final String AUTO_CONNECT_ATT = "auto_connect_att";
        public static final String AUTO_HIDE_NAVIGATIONBAR = "auto_hide_navigationbar_enable";
        public static final int AUTO_HIDE_NAVIGATIONBAR_DEFAULT = 0;
        public static final String AUTO_HIDE_NAVIGATIONBAR_TIMEOUT = "auto_hide_navigationbar_timeout";
        public static final int AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT = 2000;
        public static final String EASYFINGER_LETTER_SETTING_PREFIX = "persist.sys.fingerjoint.";
        public static final String EASYWAKE_ENABLE_FLAG = "persist.sys.easyflag";
        public static final int EASYWAKE_ENABLE_FLAG_MASK = 8192;
        public static final String EASYWAKE_LETTER_SETTING_PREFIX = "persist.sys.easywakeup.";
        public static final String ENABLE_EXPAND_ON_HUAWEI_UNLOCK = "enable_expand_on_huawei_unlock";
        public static final int FINGERSENSE_DEFAULT_GLOW_COLOR = 0;
        public static final int FINGERSENSE_DEFAULT_PIXIE_COLOR = 0;
        public static final int FINGERSENSE_DEFAULT_STROKE_COLOR = 0;
        public static final String FINGERSENSE_DOUBLE_KNUCKLE_ENABLED = "fingersense_double_knuckle_enabled";
        public static final String FINGERSENSE_DOUBLE_KNUCKLE_GESTURE_KNOCK = "fingersense_knuckle_gesture_double_knock";
        public static final String FINGERSENSE_DOUBLE_KNUCKLE_GESTURE_KNOCK_SUFFIX = "double_knock";
        public static final String FINGERSENSE_ENABLED = "fingersense_enabled";
        public static final int FINGERSENSE_ENABLED_NO = 0;
        public static final int FINGERSENSE_ENABLED_YES = 1;
        public static final String FINGERSENSE_GLOW_COLOR = "fingersense_glow_color";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_C_SUFFIX = "c";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX = "e";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_KNOCK = "fingersense_knuckle_gesture_knock";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_KNOCK_SUFFIX = "knock";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_LINE_SUFFIX = "line";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_M_SUFFIX = "m";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_NONE = "Nothing";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_OFF = "0";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_PREFIX = "fingersense_knuckle_gesture_";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_REGION = "fingersense_knuckle_gesture_region";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_REGION_SUFFIX = "region";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_REJECT_SUFFIX = "reject";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_S_SUFFIX = "s";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_V_SUFFIX = "v";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_W_SUFFIX = "w";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_Z_SUFFIX = "z";
        public static final String FINGERSENSE_LETTERS_ENABLED = "fingersense_letters_enabled";
        public static final String FINGERSENSE_LINE_GESTURE_ENABLED = "fingersense_multiwindow_enabled";
        public static final String FINGERSENSE_PIXIE_COLOR = "fingersense_pixie_color";
        public static final String FINGERSENSE_SMARTSHOT_ENABLED = "fingersense_smartshot_enabled";
        public static final String FINGERSENSE_STROKE_COLOR = "fingersense_stroke_color";
        public static final String FIRST_DAY_OF_WEEK = "first_day_of_week";
        public static final String HIDE_VIRTUAL_KEY = "hide_virtual_key";
        public static final String HUAWEI_FORCEMINNAVIGATIONBAR = "forceMinNavigationBar";
        public static final String HUAWEI_MINNAVIGATIONBAR = "minNavigationBar";
        public static final String HUAWEI_NAVIGATIONBAR_STATUSCHANGE = "com.huawei.navigationbar.statuschange";
        public static final Uri HUAWEI_RINGTONE2_URI = null;
        public static final String NAVIGATIONBAR_HEIGHT_MIN = "navigationbar_height_min";
        public static final int NAVIGATIONBAR_HEIGHT_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
        public static final int NAVIGATIONBAR_IS_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_MIN_PROMPT = "navigationbar_min_prompt";
        public static final int NAVIGATIONBAR_MIN_PROMPT_DEFAULT = 0;
        public static final String NAVIGATIONBAR_WIDTH_MIN = "navigationbar_width_min";
        public static final int NAVIGATIONBAR_WIDTH_MIN_DEFAULT = 0;
        public static final String NAVIGATION_BAR_ENABLE = "enable_navbar";
        public static final int NAVI_BAR_DISABLE = 0;
        public static final int NAVI_BAR_ENABLE = 1;
        public static final String RINGTONE2 = "ringtone2";
        public static final String RTSP_MAX_PORT = "rtsp_max_udp_port";
        public static final String RTSP_MIN_PORT = "rtsp_min_udp_port";
        public static final String RTSP_PROXY_HOST = "rtsp_proxy_host";
        public static final String RTSP_PROXY_PORT = "rtsp_proxy_port";
        public static final String SHOW_HWLOCK_FIRST = "show_hwlock_first";
        public static final String SHOW_NAVIGATIONBAR_CHECKBOK = "show_navigationbar_checkbox";
        public static final int SHOW_NAVIGATIONBAR_CHECKBOK_DEFAULT = 0;
        public static final String SIMPLEUI_MODE = "simpleui_mode";
        public static final String SINGLE_HAND_MODE = "single_hand_mode";
        public static final int SINGLE_HAND_MODE_LEFT = 1;
        public static final int SINGLE_HAND_MODE_RIGHT = 2;
        public static final String SINGLE_HAND_SWITCH = "single_hand_switch";
        public static final int SINGLE_HAND_SWITCH_OFF = 0;
        public static final int SINGLE_HAND_SWITCH_ON = 1;
        public static final String VOLUME_FM = "volume_fm";
        public static final String WEEKEND = "weekend";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.provider.HwSettings.System.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.provider.HwSettings.System.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.provider.HwSettings.System.<clinit>():void");
        }

        public System() {
        }
    }

    public static final class Systemex extends NameValueTable {
        public static final String ATTWIFI_HOTSPOT = "attwifi_hotspot";
        public static final Uri CONTENT_URI = null;
        public static final String FAST_POWER_ON = "fast_power_on";
        public static final String NEED_RESTORE_PHONE_STATE = "need_restore_phone_state";
        public static final String SHOW_BROADCAST_SSID_CONFIG = "show_broadcast_ssid_config";
        public static final String SYS_PROP_SETTINGEX_VERSION = "sys.settings_system_version";
        public static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
        public static final String USER_DEFAULT_SUBSCRIPTION = "user_default_sub";
        public static final String USER_SET_AIRPLANE = "user_set_airplane";
        private static volatile NameValueCache mNameValueCache;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.provider.HwSettings.Systemex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.provider.HwSettings.Systemex.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.provider.HwSettings.Systemex.<clinit>():void");
        }

        public Systemex() {
        }

        public static synchronized String getString(ContentResolver resolver, String name) {
            String string;
            synchronized (Systemex.class) {
                if (mNameValueCache == null) {
                    mNameValueCache = new NameValueCache(SYS_PROP_SETTINGEX_VERSION, CONTENT_URI, null);
                }
                string = mNameValueCache.getString(resolver, name);
            }
            return string;
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putString(resolver, CONTENT_URI, name, value);
        }

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            if (v != null) {
                try {
                    def = Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
            try {
                return Integer.parseInt(getString(cr, name));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            String valString = getString(cr, name);
            if (valString == null) {
                return def;
            }
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
            try {
                return Long.parseLong(getString(cr, name));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            if (v != null) {
                try {
                    def = Float.parseFloat(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
            String v = getString(cr, name);
            if (v == null) {
                return Float.parseFloat("");
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }
    }

    public HwSettings() {
    }
}
