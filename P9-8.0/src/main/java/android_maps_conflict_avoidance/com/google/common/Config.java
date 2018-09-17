package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.graphics.ImageFactory;
import android_maps_conflict_avoidance.com.google.common.io.HttpConnectionFactory;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import java.io.IOException;
import java.io.InputStream;

public abstract class Config {
    private static String ADS_CLIENT;
    protected static boolean ALT_ARROWS_ENABLED = false;
    protected static int ALT_DOWN = -9999;
    protected static int ALT_LEFT = -9999;
    private static int[] ALT_NUMBER_KEYS;
    protected static int ALT_RIGHT = -9999;
    protected static int ALT_UP = -9999;
    private static String CARRIER;
    private static String DISTRIBUTION_CHANNEL;
    public static int KEY_BACK = -9999;
    public static int KEY_CLEAR = -9999;
    public static int KEY_MENU = -9999;
    public static int KEY_OK = -9999;
    public static int KEY_POUND = 35;
    public static int KEY_SIDE_DOWN = -9999;
    public static int KEY_SIDE_SELECT = -9999;
    public static int KEY_SIDE_UP = -9999;
    public static int KEY_SOFT_LEFT = -9999;
    public static int KEY_SOFT_MIDDLE = -9999;
    public static int KEY_SOFT_RIGHT = -9999;
    public static int KEY_STAR = 42;
    public static int KEY_TALK = -9999;
    public static int KEY_VOICE_SEARCH = -9999;
    public static boolean QWERTY_KEYBOARD;
    public static boolean REVERSE_SOFTKEYS;
    public static boolean SOFTKEYS_ON_SIDE_IN_LANDSCAPE;
    public static int SOFTKEY_HEIGHT;
    public static boolean USE_NATIVE_COMMANDS;
    public static boolean USE_NATIVE_MENUS;
    private static Config instance;
    private final long applicationStartTime = this.clock.currentTimeMillis();
    private final Clock clock = new GenericClock();
    private String countryCode = "";
    private I18n i18n;

    public abstract String getAppProperty(String str);

    public abstract HttpConnectionFactory getConnectionFactory();

    public abstract ImageFactory getImageFactory();

    public abstract InputStream getInflaterInputStream(InputStream inputStream) throws IOException;

    public abstract PersistentStore getPersistentStore();

    public abstract int getPixelsPerInch();

    protected abstract void setupGzipper();

    public static void setConfig(Config config) {
        instance = config;
    }

    public static Config getInstance() {
        return instance;
    }

    protected void init() {
        if (this.i18n == null) {
            this.i18n = I18n.init(getAppProperty("DownloadLocale"));
        }
        String platform = System.getProperty("microedition.platform");
        platform = platform != null ? platform.toLowerCase() : "";
        DISTRIBUTION_CHANNEL = getDistributionChannelInternal();
        if (DISTRIBUTION_CHANNEL == null) {
            DISTRIBUTION_CHANNEL = "unknown";
        }
        ADS_CLIENT = getAdsClientInternal();
        if (ADS_CLIENT == null) {
            ADS_CLIENT = "unknown";
        }
        CARRIER = getAppProperty("Carrier");
        if (CARRIER == null) {
            CARRIER = "unknown";
        }
        KEY_BACK = getIntProperty("BackKey", KEY_BACK);
        KEY_SOFT_LEFT = getIntProperty("LeftSoftKey", KEY_SOFT_LEFT);
        KEY_SOFT_MIDDLE = getIntProperty("MiddleSoftKey", KEY_SOFT_MIDDLE);
        KEY_SOFT_RIGHT = getIntProperty("RightSoftKey", KEY_SOFT_RIGHT);
        REVERSE_SOFTKEYS = getBooleanProperty("ReverseSoftkeys", platform.startsWith("nokia"));
        SOFTKEYS_ON_SIDE_IN_LANDSCAPE = getBooleanProperty("SoftkeysOnSideInLandscape", false);
        KEY_SIDE_UP = getIntProperty("SideUpKey", KEY_SIDE_UP);
        KEY_SIDE_DOWN = getIntProperty("SideDownKey", KEY_SIDE_DOWN);
        KEY_SIDE_SELECT = getIntProperty("SideSelectKey", KEY_SIDE_SELECT);
        QWERTY_KEYBOARD = getBooleanProperty("QwertyKeyboard", false);
        if (REVERSE_SOFTKEYS) {
            int temp = KEY_SOFT_LEFT;
            KEY_SOFT_LEFT = KEY_SOFT_RIGHT;
            KEY_SOFT_RIGHT = temp;
        }
        KEY_MENU = getIntProperty("MenuKey", KEY_MENU);
        KEY_OK = getIntProperty("SelectKey", KEY_OK);
        KEY_TALK = getIntProperty("TalkKey", KEY_TALK);
        KEY_VOICE_SEARCH = getIntProperty("VoiceSearchKey", KEY_VOICE_SEARCH);
        KEY_CLEAR = getIntProperty("ClearKey", KEY_CLEAR);
        String str = "UseNativeCommands";
        boolean z = (platform.startsWith("nokia") || platform.startsWith("sony")) ? false : true;
        USE_NATIVE_COMMANDS = getBooleanProperty(str, z);
        USE_NATIVE_MENUS = getBooleanProperty("UseNativeMenus", false);
        SOFTKEY_HEIGHT = getIntProperty("SoftkeyHeight", 0);
        parseAltNumberKeys(getAppProperty("AltNumberKeys"));
        parseAltArrowKeys(getAppProperty("AltArrowKeys"));
        if (USE_NATIVE_MENUS && !USE_NATIVE_COMMANDS) {
            USE_NATIVE_COMMANDS = true;
        }
        setupGzipper();
    }

    protected String getDistributionChannelInternal() {
        return getAppProperty("DistributionChannel");
    }

    protected String getAdsClientInternal() {
        return getAppProperty("AdsClient");
    }

    private static void parseAltArrowKeys(String altArrowKeys) {
        int[] altKeys = parseAltKeys(4, altArrowKeys);
        if (altKeys != null) {
            ALT_ARROWS_ENABLED = true;
            ALT_UP = altKeys[0];
            ALT_LEFT = altKeys[1];
            ALT_DOWN = altKeys[2];
            ALT_RIGHT = altKeys[3];
        }
    }

    private static void parseAltNumberKeys(String altNumberKeys) {
        ALT_NUMBER_KEYS = parseAltKeys(12, altNumberKeys);
    }

    private static int[] parseAltKeys(int numberOfKeys, String altKeys) {
        if (altKeys == null || altKeys.length() == 0) {
            return null;
        }
        try {
            int index;
            int[] keyCodes = new int[numberOfKeys];
            int lastComma = 0;
            int index2 = 0;
            while (true) {
                int nextComma = altKeys.indexOf(",", lastComma);
                if (nextComma == -1) {
                    break;
                }
                index = index2 + 1;
                keyCodes[index2] = Integer.parseInt(altKeys.substring(lastComma, nextComma));
                lastComma = nextComma + 1;
                index2 = index;
            }
            index = index2 + 1;
            keyCodes[index2] = Integer.parseInt(altKeys.substring(lastComma));
            if (index == numberOfKeys) {
                return keyCodes;
            }
            return null;
        } catch (NumberFormatException e) {
            Log.logThrowable("CONFIG", e);
            return null;
        } catch (ArrayIndexOutOfBoundsException e2) {
            Log.logThrowable("CONFIG", e2);
            return null;
        }
    }

    public static synchronized String getLocale() {
        String uiLocale;
        synchronized (Config.class) {
            uiLocale = instance.i18n.getUiLocale();
        }
        return uiLocale;
    }

    protected I18n getI18n() {
        return this.i18n;
    }

    public static boolean isChinaVersion() {
        return false;
    }

    public int getIntProperty(String property, int defaultValue) {
        String value = getAppProperty(property);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    public boolean getBooleanProperty(String property, boolean defaultValue) {
        String value = getAppProperty(property);
        if (value != null) {
            if ("true".equals(value)) {
                return true;
            }
            if ("false".equals(value)) {
                return false;
            }
        }
        return defaultValue;
    }

    public Clock getClock() {
        return this.clock;
    }
}
