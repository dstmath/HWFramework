package com.huawei.android.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings.SettingNotFoundException;

public final class SettingsEx {
    public static final String AUTHORITY = "settings";

    public static final class Global {
        public static final String SINGLE_HAND_MODE = "single_hand_mode";
    }

    public static final class Secure {
        public static final String WIFI_AP_IGNOREBROADCASTSSID = "wifi_ap_ignorebroadcastssid";
        public static final String WIFI_P2P_ON = "wifi_p2p_on";
    }

    public static final class System {
        public static final String AUTO_ANSWER_TIMEOUT = "auto_answer";
        public static final String AUTO_CONNECT_ATT = "auto_connect_att";
        public static final String AUTO_HIDE_NAVIGATIONBAR = "auto_hide_navigationbar_enable";
        public static final int AUTO_HIDE_NAVIGATIONBAR_DEFAULT = 0;
        public static final String AUTO_HIDE_NAVIGATIONBAR_TIMEOUT = "auto_hide_navigationbar_timeout";
        public static final int AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT = 2000;
        public static final String ENABLE_EXPAND_ON_HUAWEI_UNLOCK = "enable_expand_on_huawei_unlock";
        public static final String FIRST_DAY_OF_WEEK = "first_day_of_week";
        public static final String HIDE_VIRTUAL_KEY = "hide_virtual_key";
        public static final String HUAWEI_FORCEMINNAVIGATIONBAR = "forceMinNavigationBar";
        public static final String HUAWEI_MINNAVIGATIONBAR = "minNavigationBar";
        public static final String HUAWEI_NAVIGATIONBAR_STATUSCHANGE = "com.huawei.navigationbar.statuschange";
        public static final Uri HUAWEI_RINGTONE2_URI = null;
        public static final String MULTI_SIM_VOICE_CALL_SUBSCRIPTION = "multi_sim_voice_call";
        public static final String NAVIGATIONBAR_HEIGHT_MIN = "navigationbar_height_min";
        public static final int NAVIGATIONBAR_HEIGHT_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
        public static final int NAVIGATIONBAR_IS_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_MIN_PROMPT = "navigationbar_min_prompt";
        public static final int NAVIGATIONBAR_MIN_PROMPT_DEFAULT = 0;
        public static final String NAVIGATIONBAR_WIDTH_MIN = "navigationbar_width_min";
        public static final int NAVIGATIONBAR_WIDTH_MIN_DEFAULT = 0;
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
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.provider.SettingsEx.System.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.provider.SettingsEx.System.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.provider.SettingsEx.System.<clinit>():void");
        }
    }

    public static final class Systemex {
        public static final String ATTWIFI_HOTSPOT = "attwifi_hotspot";
        public static final Uri CONTENT_URI = null;
        public static final String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
        public static final String SHOW_BROADCAST_SSID_CONFIG = "show_broadcast_ssid_config";
        public static final String SYS_PROP_SETTINGEX_VERSION = "sys.settings_system_version";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.provider.SettingsEx.Systemex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.provider.SettingsEx.Systemex.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.provider.SettingsEx.Systemex.<clinit>():void");
        }

        public Systemex() {
        }

        public static synchronized String getString(ContentResolver resolver, String name) {
            String string;
            synchronized (Systemex.class) {
                string = huawei.android.provider.HwSettings.Systemex.getString(resolver, name);
            }
            return string;
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return huawei.android.provider.HwSettings.Systemex.putString(resolver, name, value);
        }

        public static Uri getUriFor(String name) {
            return huawei.android.provider.HwSettings.Systemex.getUriFor(name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            return huawei.android.provider.HwSettings.Systemex.getInt(cr, name, def);
        }

        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
            return huawei.android.provider.HwSettings.Systemex.getInt(cr, name);
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return huawei.android.provider.HwSettings.Systemex.putInt(cr, name, value);
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            return huawei.android.provider.HwSettings.Systemex.getLong(cr, name, def);
        }

        public static long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
            return huawei.android.provider.HwSettings.Systemex.getLong(cr, name);
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return huawei.android.provider.HwSettings.Systemex.putLong(cr, name, value);
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            return huawei.android.provider.HwSettings.Systemex.getFloat(cr, name, def);
        }

        public static float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
            return huawei.android.provider.HwSettings.Systemex.getFloat(cr, name);
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return huawei.android.provider.HwSettings.Systemex.putFloat(cr, name, value);
        }
    }

    public SettingsEx() {
    }
}
