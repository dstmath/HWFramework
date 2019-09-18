package android.content.res;

import android.app.WindowConfiguration;
import android.app.slice.Slice;
import android.content.ConfigurationProto;
import android.content.ResourcesConfigurationProto;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class Configuration implements Parcelable, Comparable<Configuration> {
    public static final int ASSETS_SEQ_UNDEFINED = 0;
    public static final int COLOR_MODE_HDR_MASK = 12;
    public static final int COLOR_MODE_HDR_NO = 4;
    public static final int COLOR_MODE_HDR_SHIFT = 2;
    public static final int COLOR_MODE_HDR_UNDEFINED = 0;
    public static final int COLOR_MODE_HDR_YES = 8;
    public static final int COLOR_MODE_UNDEFINED = 0;
    public static final int COLOR_MODE_WIDE_COLOR_GAMUT_MASK = 3;
    public static final int COLOR_MODE_WIDE_COLOR_GAMUT_NO = 1;
    public static final int COLOR_MODE_WIDE_COLOR_GAMUT_UNDEFINED = 0;
    public static final int COLOR_MODE_WIDE_COLOR_GAMUT_YES = 2;
    public static final Parcelable.Creator<Configuration> CREATOR = new Parcelable.Creator<Configuration>() {
        public Configuration createFromParcel(Parcel source) {
            return new Configuration(source);
        }

        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };
    public static final int DENSITY_DPI_ANY = 65534;
    public static final int DENSITY_DPI_NONE = 65535;
    public static final int DENSITY_DPI_UNDEFINED = 0;
    public static final Configuration EMPTY = new Configuration();
    public static final int HARDKEYBOARDHIDDEN_NO = 1;
    public static final int HARDKEYBOARDHIDDEN_UNDEFINED = 0;
    public static final int HARDKEYBOARDHIDDEN_YES = 2;
    public static final int KEYBOARDHIDDEN_NO = 1;
    public static final int KEYBOARDHIDDEN_SOFT = 3;
    public static final int KEYBOARDHIDDEN_UNDEFINED = 0;
    public static final int KEYBOARDHIDDEN_YES = 2;
    public static final int KEYBOARD_12KEY = 3;
    public static final int KEYBOARD_NOKEYS = 1;
    public static final int KEYBOARD_QWERTY = 2;
    public static final int KEYBOARD_UNDEFINED = 0;
    public static final int MNC_ZERO = 65535;
    public static final int NATIVE_CONFIG_COLOR_MODE = 65536;
    public static final int NATIVE_CONFIG_DENSITY = 256;
    public static final int NATIVE_CONFIG_KEYBOARD = 16;
    public static final int NATIVE_CONFIG_KEYBOARD_HIDDEN = 32;
    public static final int NATIVE_CONFIG_LAYOUTDIR = 16384;
    public static final int NATIVE_CONFIG_LOCALE = 4;
    public static final int NATIVE_CONFIG_MCC = 1;
    public static final int NATIVE_CONFIG_MNC = 2;
    public static final int NATIVE_CONFIG_NAVIGATION = 64;
    public static final int NATIVE_CONFIG_ORIENTATION = 128;
    public static final int NATIVE_CONFIG_SCREEN_LAYOUT = 2048;
    public static final int NATIVE_CONFIG_SCREEN_SIZE = 512;
    public static final int NATIVE_CONFIG_SMALLEST_SCREEN_SIZE = 8192;
    public static final int NATIVE_CONFIG_TOUCHSCREEN = 8;
    public static final int NATIVE_CONFIG_UI_MODE = 4096;
    public static final int NATIVE_CONFIG_VERSION = 1024;
    public static final int NAVIGATIONHIDDEN_NO = 1;
    public static final int NAVIGATIONHIDDEN_UNDEFINED = 0;
    public static final int NAVIGATIONHIDDEN_YES = 2;
    public static final int NAVIGATION_DPAD = 2;
    public static final int NAVIGATION_NONAV = 1;
    public static final int NAVIGATION_TRACKBALL = 3;
    public static final int NAVIGATION_UNDEFINED = 0;
    public static final int NAVIGATION_WHEEL = 4;
    public static final int ORIENTATION_LANDSCAPE = 2;
    public static final int ORIENTATION_PORTRAIT = 1;
    @Deprecated
    public static final int ORIENTATION_SQUARE = 3;
    public static final int ORIENTATION_UNDEFINED = 0;
    public static final int SCREENLAYOUT_COMPAT_NEEDED = 268435456;
    public static final int SCREENLAYOUT_LAYOUTDIR_LTR = 64;
    public static final int SCREENLAYOUT_LAYOUTDIR_MASK = 192;
    public static final int SCREENLAYOUT_LAYOUTDIR_RTL = 128;
    public static final int SCREENLAYOUT_LAYOUTDIR_SHIFT = 6;
    public static final int SCREENLAYOUT_LAYOUTDIR_UNDEFINED = 0;
    public static final int SCREENLAYOUT_LONG_MASK = 48;
    public static final int SCREENLAYOUT_LONG_NO = 16;
    public static final int SCREENLAYOUT_LONG_UNDEFINED = 0;
    public static final int SCREENLAYOUT_LONG_YES = 32;
    public static final int SCREENLAYOUT_ROUND_MASK = 768;
    public static final int SCREENLAYOUT_ROUND_NO = 256;
    public static final int SCREENLAYOUT_ROUND_SHIFT = 8;
    public static final int SCREENLAYOUT_ROUND_UNDEFINED = 0;
    public static final int SCREENLAYOUT_ROUND_YES = 512;
    public static final int SCREENLAYOUT_SIZE_LARGE = 3;
    public static final int SCREENLAYOUT_SIZE_MASK = 15;
    public static final int SCREENLAYOUT_SIZE_NORMAL = 2;
    public static final int SCREENLAYOUT_SIZE_SMALL = 1;
    public static final int SCREENLAYOUT_SIZE_UNDEFINED = 0;
    public static final int SCREENLAYOUT_SIZE_XLARGE = 4;
    public static final int SCREENLAYOUT_UNDEFINED = 0;
    public static final int SCREEN_HEIGHT_DP_UNDEFINED = 0;
    public static final int SCREEN_WIDTH_DP_UNDEFINED = 0;
    public static final int SMALLEST_SCREEN_WIDTH_DP_UNDEFINED = 0;
    public static final int TOUCHSCREEN_FINGER = 3;
    public static final int TOUCHSCREEN_NOTOUCH = 1;
    @Deprecated
    public static final int TOUCHSCREEN_STYLUS = 2;
    public static final int TOUCHSCREEN_UNDEFINED = 0;
    public static final int UI_MODE_NIGHT_MASK = 48;
    public static final int UI_MODE_NIGHT_NO = 16;
    public static final int UI_MODE_NIGHT_UNDEFINED = 0;
    public static final int UI_MODE_NIGHT_YES = 32;
    public static final int UI_MODE_TYPE_APPLIANCE = 5;
    public static final int UI_MODE_TYPE_CAR = 3;
    public static final int UI_MODE_TYPE_DESK = 2;
    public static final int UI_MODE_TYPE_MASK = 15;
    public static final int UI_MODE_TYPE_NORMAL = 1;
    public static final int UI_MODE_TYPE_TELEVISION = 4;
    public static final int UI_MODE_TYPE_UNDEFINED = 0;
    public static final int UI_MODE_TYPE_VR_HEADSET = 7;
    public static final int UI_MODE_TYPE_WATCH = 6;
    private static final String XML_ATTR_APP_BOUNDS = "app_bounds";
    private static final String XML_ATTR_COLOR_MODE = "clrMod";
    private static final String XML_ATTR_DENSITY = "density";
    private static final String XML_ATTR_FONT_SCALE = "fs";
    private static final String XML_ATTR_HARD_KEYBOARD_HIDDEN = "hardKeyHid";
    private static final String XML_ATTR_KEYBOARD = "key";
    private static final String XML_ATTR_KEYBOARD_HIDDEN = "keyHid";
    private static final String XML_ATTR_LOCALES = "locales";
    private static final String XML_ATTR_MCC = "mcc";
    private static final String XML_ATTR_MNC = "mnc";
    private static final String XML_ATTR_NAVIGATION = "nav";
    private static final String XML_ATTR_NAVIGATION_HIDDEN = "navHid";
    private static final String XML_ATTR_ORIENTATION = "ori";
    private static final String XML_ATTR_ROTATION = "rot";
    private static final String XML_ATTR_SCREEN_HEIGHT = "height";
    private static final String XML_ATTR_SCREEN_LAYOUT = "scrLay";
    private static final String XML_ATTR_SCREEN_WIDTH = "width";
    private static final String XML_ATTR_SMALLEST_WIDTH = "sw";
    private static final String XML_ATTR_TOUCHSCREEN = "touch";
    private static final String XML_ATTR_UI_MODE = "ui";
    public int assetsSeq;
    public int colorMode;
    public int compatScreenHeightDp;
    public int compatScreenWidthDp;
    public int compatSmallestScreenWidthDp;
    public int densityDpi;
    public IHwConfiguration extraConfig;
    public float fontScale;
    public int hardKeyboardHidden;
    public int keyboard;
    public int keyboardHidden;
    @Deprecated
    public Locale locale;
    private LocaleList mLocaleList;
    public int mcc;
    public int mnc;
    public int navigation;
    public int navigationHidden;
    public int nonFullScreen;
    public int orientation;
    public int screenHeightDp;
    public int screenLayout;
    public int screenWidthDp;
    public int seq;
    public int smallestScreenWidthDp;
    public int touchscreen;
    public int uiMode;
    public boolean userSetLocale;
    public final WindowConfiguration windowConfiguration;

    @Retention(RetentionPolicy.SOURCE)
    public @interface NativeConfig {
    }

    public static int resetScreenLayout(int curLayout) {
        return (-268435520 & curLayout) | 36;
    }

    public static int reduceScreenLayout(int curLayout, int longSizeDp, int shortSizeDp) {
        boolean screenLayoutCompatNeeded;
        int screenLayoutSize;
        int screenLayoutSize2 = 0;
        if (longSizeDp < 470) {
            screenLayoutCompatNeeded = false;
            screenLayoutSize = 1;
            screenLayoutSize2 = 0;
        } else {
            if (longSizeDp >= 960 && shortSizeDp >= 720) {
                screenLayoutSize = 4;
            } else if (longSizeDp < 640 || shortSizeDp < 480) {
                screenLayoutSize = 2;
            } else {
                screenLayoutSize = 3;
            }
            if (shortSizeDp > 321 || longSizeDp > 570) {
                screenLayoutCompatNeeded = true;
            } else {
                screenLayoutCompatNeeded = false;
            }
            if ((longSizeDp * 3) / 5 >= shortSizeDp - 1) {
                screenLayoutSize2 = 1;
            }
        }
        if (screenLayoutSize2 == 0) {
            curLayout = (curLayout & -49) | 16;
        }
        if (screenLayoutCompatNeeded) {
            curLayout |= 268435456;
        }
        if (screenLayoutSize < (curLayout & 15)) {
            return (curLayout & -16) | screenLayoutSize;
        }
        return curLayout;
    }

    public static String configurationDiffToString(int diff) {
        ArrayList<String> list = new ArrayList<>();
        if ((diff & 1) != 0) {
            list.add("CONFIG_MCC");
        }
        if ((diff & 2) != 0) {
            list.add("CONFIG_MNC");
        }
        if ((diff & 4) != 0) {
            list.add("CONFIG_LOCALE");
        }
        if ((diff & 8) != 0) {
            list.add("CONFIG_TOUCHSCREEN");
        }
        if ((diff & 16) != 0) {
            list.add("CONFIG_KEYBOARD");
        }
        if ((diff & 32) != 0) {
            list.add("CONFIG_KEYBOARD_HIDDEN");
        }
        if ((diff & 64) != 0) {
            list.add("CONFIG_NAVIGATION");
        }
        if ((diff & 128) != 0) {
            list.add("CONFIG_ORIENTATION");
        }
        if ((diff & 256) != 0) {
            list.add("CONFIG_SCREEN_LAYOUT");
        }
        if ((diff & 16384) != 0) {
            list.add("CONFIG_COLOR_MODE");
        }
        if ((diff & 512) != 0) {
            list.add("CONFIG_UI_MODE");
        }
        if ((diff & 1024) != 0) {
            list.add("CONFIG_SCREEN_SIZE");
        }
        if ((diff & 2048) != 0) {
            list.add("CONFIG_SMALLEST_SCREEN_SIZE");
        }
        if ((diff & 8192) != 0) {
            list.add("CONFIG_LAYOUT_DIRECTION");
        }
        if ((1073741824 & diff) != 0) {
            list.add("CONFIG_FONT_SCALE");
        }
        if ((Integer.MIN_VALUE & diff) != 0) {
            list.add("CONFIG_ASSETS_PATHS");
        }
        StringBuilder builder = new StringBuilder("{");
        int n = list.size();
        for (int i = 0; i < n; i++) {
            builder.append(list.get(i));
            if (i != n - 1) {
                builder.append(", ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public boolean isLayoutSizeAtLeast(int size) {
        int cur = this.screenLayout & 15;
        boolean z = false;
        if (cur == 0) {
            return false;
        }
        if (cur >= size) {
            z = true;
        }
        return z;
    }

    public Configuration() {
        this.windowConfiguration = new WindowConfiguration();
        this.extraConfig = HwConfiguration.initHwConfiguration();
        unset();
    }

    public Configuration(Configuration o) {
        this.windowConfiguration = new WindowConfiguration();
        this.extraConfig = HwConfiguration.initHwConfiguration();
        setTo(o);
    }

    private void fixUpLocaleList() {
        LocaleList localeList;
        if ((this.locale == null && !this.mLocaleList.isEmpty()) || (this.locale != null && !this.locale.equals(this.mLocaleList.get(0)))) {
            if (this.locale == null) {
                localeList = LocaleList.getEmptyLocaleList();
            } else {
                localeList = new LocaleList(new Locale[]{this.locale});
            }
            this.mLocaleList = localeList;
        }
    }

    public void setTo(Configuration o) {
        this.fontScale = o.fontScale;
        this.mcc = o.mcc;
        this.mnc = o.mnc;
        this.locale = o.locale == null ? null : (Locale) o.locale.clone();
        o.fixUpLocaleList();
        this.mLocaleList = o.mLocaleList;
        this.userSetLocale = o.userSetLocale;
        this.touchscreen = o.touchscreen;
        this.keyboard = o.keyboard;
        this.keyboardHidden = o.keyboardHidden;
        this.hardKeyboardHidden = o.hardKeyboardHidden;
        this.navigation = o.navigation;
        this.navigationHidden = o.navigationHidden;
        this.orientation = o.orientation;
        this.screenLayout = o.screenLayout;
        this.colorMode = o.colorMode;
        this.uiMode = o.uiMode;
        this.screenWidthDp = o.screenWidthDp;
        this.screenHeightDp = o.screenHeightDp;
        this.smallestScreenWidthDp = o.smallestScreenWidthDp;
        this.densityDpi = o.densityDpi;
        this.compatScreenWidthDp = o.compatScreenWidthDp;
        this.compatScreenHeightDp = o.compatScreenHeightDp;
        this.compatSmallestScreenWidthDp = o.compatSmallestScreenWidthDp;
        this.assetsSeq = o.assetsSeq;
        this.extraConfig.setTo(o.extraConfig);
        this.nonFullScreen = o.nonFullScreen;
        this.seq = o.seq;
        this.windowConfiguration.setTo(o.windowConfiguration);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        sb.append(this.fontScale);
        sb.append(" ");
        if (this.mcc != 0) {
            sb.append(this.mcc);
            sb.append(XML_ATTR_MCC);
        } else {
            sb.append("?mcc");
        }
        if (this.mnc != 0) {
            sb.append(this.mnc);
            sb.append(XML_ATTR_MNC);
        } else {
            sb.append("?mnc");
        }
        fixUpLocaleList();
        if (!this.mLocaleList.isEmpty()) {
            sb.append(" ");
            sb.append(this.mLocaleList);
        } else {
            sb.append(" ?localeList");
        }
        int layoutDir = this.screenLayout & 192;
        if (layoutDir == 0) {
            sb.append(" ?layoutDir");
        } else if (layoutDir == 64) {
            sb.append(" ldltr");
        } else if (layoutDir != 128) {
            sb.append(" layoutDir=");
            sb.append(layoutDir >> 6);
        } else {
            sb.append(" ldrtl");
        }
        if (this.smallestScreenWidthDp != 0) {
            sb.append(" sw");
            sb.append(this.smallestScreenWidthDp);
            sb.append("dp");
        } else {
            sb.append(" ?swdp");
        }
        if (this.screenWidthDp != 0) {
            sb.append(" w");
            sb.append(this.screenWidthDp);
            sb.append("dp");
        } else {
            sb.append(" ?wdp");
        }
        if (this.screenHeightDp != 0) {
            sb.append(" h");
            sb.append(this.screenHeightDp);
            sb.append("dp");
        } else {
            sb.append(" ?hdp");
        }
        if (this.densityDpi != 0) {
            sb.append(" ");
            sb.append(this.densityDpi);
            sb.append("dpi");
        } else {
            sb.append(" ?density");
        }
        switch (this.screenLayout & 15) {
            case 0:
                sb.append(" ?lsize");
                break;
            case 1:
                sb.append(" smll");
                break;
            case 2:
                sb.append(" nrml");
                break;
            case 3:
                sb.append(" lrg");
                break;
            case 4:
                sb.append(" xlrg");
                break;
            default:
                sb.append(" layoutSize=");
                sb.append(this.screenLayout & 15);
                break;
        }
        int i = this.screenLayout & 48;
        if (i == 0) {
            sb.append(" ?long");
        } else if (i != 16) {
            if (i != 32) {
                sb.append(" layoutLong=");
                sb.append(this.screenLayout & 48);
            } else {
                sb.append(" long");
            }
        }
        int i2 = this.colorMode & 12;
        if (i2 == 0) {
            sb.append(" ?ldr");
        } else if (i2 != 4) {
            if (i2 != 8) {
                sb.append(" dynamicRange=");
                sb.append(this.colorMode & 12);
            } else {
                sb.append(" hdr");
            }
        }
        switch (this.colorMode & 3) {
            case 0:
                sb.append(" ?wideColorGamut");
                break;
            case 1:
                break;
            case 2:
                sb.append(" widecg");
                break;
            default:
                sb.append(" wideColorGamut=");
                sb.append(this.colorMode & 3);
                break;
        }
        switch (this.orientation) {
            case 0:
                sb.append(" ?orien");
                break;
            case 1:
                sb.append(" port");
                break;
            case 2:
                sb.append(" land");
                break;
            default:
                sb.append(" orien=");
                sb.append(this.orientation);
                break;
        }
        switch (this.uiMode & 15) {
            case 0:
                sb.append(" ?uimode");
                break;
            case 1:
                break;
            case 2:
                sb.append(" desk");
                break;
            case 3:
                sb.append(" car");
                break;
            case 4:
                sb.append(" television");
                break;
            case 5:
                sb.append(" appliance");
                break;
            case 6:
                sb.append(" watch");
                break;
            case 7:
                sb.append(" vrheadset");
                break;
            default:
                sb.append(" uimode=");
                sb.append(this.uiMode & 15);
                break;
        }
        int i3 = this.uiMode & 48;
        if (i3 == 0) {
            sb.append(" ?night");
        } else if (i3 != 16) {
            if (i3 != 32) {
                sb.append(" night=");
                sb.append(this.uiMode & 48);
            } else {
                sb.append(" night");
            }
        }
        switch (this.touchscreen) {
            case 0:
                sb.append(" ?touch");
                break;
            case 1:
                sb.append(" -touch");
                break;
            case 2:
                sb.append(" stylus");
                break;
            case 3:
                sb.append(" finger");
                break;
            default:
                sb.append(" touch=");
                sb.append(this.touchscreen);
                break;
        }
        switch (this.keyboard) {
            case 0:
                sb.append(" ?keyb");
                break;
            case 1:
                sb.append(" -keyb");
                break;
            case 2:
                sb.append(" qwerty");
                break;
            case 3:
                sb.append(" 12key");
                break;
            default:
                sb.append(" keys=");
                sb.append(this.keyboard);
                break;
        }
        switch (this.keyboardHidden) {
            case 0:
                sb.append("/?");
                break;
            case 1:
                sb.append("/v");
                break;
            case 2:
                sb.append("/h");
                break;
            case 3:
                sb.append("/s");
                break;
            default:
                sb.append("/");
                sb.append(this.keyboardHidden);
                break;
        }
        switch (this.hardKeyboardHidden) {
            case 0:
                sb.append("/?");
                break;
            case 1:
                sb.append("/v");
                break;
            case 2:
                sb.append("/h");
                break;
            default:
                sb.append("/");
                sb.append(this.hardKeyboardHidden);
                break;
        }
        switch (this.navigation) {
            case 0:
                sb.append(" ?nav");
                break;
            case 1:
                sb.append(" -nav");
                break;
            case 2:
                sb.append(" dpad");
                break;
            case 3:
                sb.append(" tball");
                break;
            case 4:
                sb.append(" wheel");
                break;
            default:
                sb.append(" nav=");
                sb.append(this.navigation);
                break;
        }
        switch (this.navigationHidden) {
            case 0:
                sb.append("/?");
                break;
            case 1:
                sb.append("/v");
                break;
            case 2:
                sb.append("/h");
                break;
            default:
                sb.append("/");
                sb.append(this.navigationHidden);
                break;
        }
        sb.append(" winConfig=");
        sb.append(this.windowConfiguration);
        sb.append(" nonFullScreen=");
        sb.append(this.nonFullScreen);
        if (this.assetsSeq != 0) {
            sb.append(" as.");
            sb.append(this.assetsSeq);
        }
        sb.append(this.extraConfig.toString());
        if (this.seq != 0) {
            sb.append(" s.");
            sb.append(this.seq);
        }
        sb.append('}');
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        long token = protoOutputStream.start(fieldId);
        protoOutputStream.write(ConfigurationProto.FONT_SCALE, this.fontScale);
        protoOutputStream.write(1155346202626L, this.mcc);
        protoOutputStream.write(1155346202627L, this.mnc);
        this.mLocaleList.writeToProto(protoOutputStream, 2246267895812L);
        protoOutputStream.write(ConfigurationProto.SCREEN_LAYOUT, this.screenLayout);
        protoOutputStream.write(1155346202630L, this.colorMode);
        protoOutputStream.write(ConfigurationProto.TOUCHSCREEN, this.touchscreen);
        protoOutputStream.write(1155346202632L, this.keyboard);
        protoOutputStream.write(ConfigurationProto.KEYBOARD_HIDDEN, this.keyboardHidden);
        protoOutputStream.write(ConfigurationProto.HARD_KEYBOARD_HIDDEN, this.hardKeyboardHidden);
        protoOutputStream.write(ConfigurationProto.NAVIGATION, this.navigation);
        protoOutputStream.write(ConfigurationProto.NAVIGATION_HIDDEN, this.navigationHidden);
        protoOutputStream.write(ConfigurationProto.ORIENTATION, this.orientation);
        protoOutputStream.write(ConfigurationProto.UI_MODE, this.uiMode);
        protoOutputStream.write(ConfigurationProto.SCREEN_WIDTH_DP, this.screenWidthDp);
        protoOutputStream.write(ConfigurationProto.SCREEN_HEIGHT_DP, this.screenHeightDp);
        protoOutputStream.write(ConfigurationProto.SMALLEST_SCREEN_WIDTH_DP, this.smallestScreenWidthDp);
        protoOutputStream.write(ConfigurationProto.DENSITY_DPI, this.densityDpi);
        this.windowConfiguration.writeToProto(protoOutputStream, ConfigurationProto.WINDOW_CONFIGURATION);
        protoOutputStream.end(token);
    }

    public void writeResConfigToProto(ProtoOutputStream protoOutputStream, long fieldId, DisplayMetrics metrics) {
        int height;
        int width;
        if (metrics.widthPixels >= metrics.heightPixels) {
            width = metrics.widthPixels;
            height = metrics.heightPixels;
        } else {
            width = metrics.heightPixels;
            height = metrics.widthPixels;
        }
        long token = protoOutputStream.start(fieldId);
        writeToProto(protoOutputStream, 1146756268033L);
        protoOutputStream.write(1155346202626L, Build.VERSION.RESOURCES_SDK_INT);
        protoOutputStream.write(1155346202627L, width);
        protoOutputStream.write(ResourcesConfigurationProto.SCREEN_HEIGHT_PX, height);
        protoOutputStream.end(token);
    }

    public static String uiModeToString(int uiMode2) {
        switch (uiMode2) {
            case 0:
                return "UI_MODE_TYPE_UNDEFINED";
            case 1:
                return "UI_MODE_TYPE_NORMAL";
            case 2:
                return "UI_MODE_TYPE_DESK";
            case 3:
                return "UI_MODE_TYPE_CAR";
            case 4:
                return "UI_MODE_TYPE_TELEVISION";
            case 5:
                return "UI_MODE_TYPE_APPLIANCE";
            case 6:
                return "UI_MODE_TYPE_WATCH";
            case 7:
                return "UI_MODE_TYPE_VR_HEADSET";
            default:
                return Integer.toString(uiMode2);
        }
    }

    public void setToDefaults() {
        this.fontScale = 1.0f;
        this.mnc = 0;
        this.mcc = 0;
        this.mLocaleList = LocaleList.getEmptyLocaleList();
        this.locale = null;
        this.userSetLocale = false;
        this.touchscreen = 0;
        this.keyboard = 0;
        this.keyboardHidden = 0;
        this.hardKeyboardHidden = 0;
        this.navigation = 0;
        this.navigationHidden = 0;
        this.orientation = 0;
        this.screenLayout = 0;
        this.colorMode = 0;
        this.uiMode = 0;
        this.compatScreenWidthDp = 0;
        this.screenWidthDp = 0;
        this.compatScreenHeightDp = 0;
        this.screenHeightDp = 0;
        this.compatSmallestScreenWidthDp = 0;
        this.smallestScreenWidthDp = 0;
        this.densityDpi = 0;
        this.assetsSeq = 0;
        this.extraConfig.setToDefaults();
        this.nonFullScreen = 0;
        this.seq = 0;
        this.windowConfiguration.setToDefaults();
    }

    public void unset() {
        setToDefaults();
        this.fontScale = 0.0f;
    }

    @Deprecated
    public void makeDefault() {
        setToDefaults();
    }

    public int updateFrom(Configuration delta) {
        int changed = 0;
        if (delta.fontScale > 0.0f && this.fontScale != delta.fontScale) {
            changed = 0 | 1073741824;
            this.fontScale = delta.fontScale;
        }
        if (!(delta.mcc == 0 || this.mcc == delta.mcc)) {
            changed |= 1;
            this.mcc = delta.mcc;
        }
        if (!(delta.mnc == 0 || this.mnc == delta.mnc)) {
            changed |= 2;
            this.mnc = delta.mnc;
        }
        fixUpLocaleList();
        delta.fixUpLocaleList();
        if (!delta.mLocaleList.isEmpty() && !this.mLocaleList.equals(delta.mLocaleList)) {
            changed |= 4;
            this.mLocaleList = delta.mLocaleList;
            if (!delta.locale.equals(this.locale)) {
                this.locale = (Locale) delta.locale.clone();
                changed |= 8192;
                setLayoutDirection(this.locale);
            }
        }
        int deltaScreenLayoutDir = delta.screenLayout & 192;
        if (!(deltaScreenLayoutDir == 0 || deltaScreenLayoutDir == (this.screenLayout & 192))) {
            this.screenLayout = (this.screenLayout & -193) | deltaScreenLayoutDir;
            changed |= 8192;
        }
        if (delta.userSetLocale && (!this.userSetLocale || (changed & 4) != 0)) {
            changed |= 4;
            this.userSetLocale = true;
        }
        if (!(delta.touchscreen == 0 || this.touchscreen == delta.touchscreen)) {
            changed |= 8;
            this.touchscreen = delta.touchscreen;
        }
        if (!(delta.keyboard == 0 || this.keyboard == delta.keyboard)) {
            changed |= 16;
            this.keyboard = delta.keyboard;
        }
        if (!(delta.keyboardHidden == 0 || this.keyboardHidden == delta.keyboardHidden)) {
            changed |= 32;
            this.keyboardHidden = delta.keyboardHidden;
        }
        if (!(delta.hardKeyboardHidden == 0 || this.hardKeyboardHidden == delta.hardKeyboardHidden)) {
            changed |= 32;
            this.hardKeyboardHidden = delta.hardKeyboardHidden;
        }
        if (!(delta.navigation == 0 || this.navigation == delta.navigation)) {
            changed |= 64;
            this.navigation = delta.navigation;
        }
        if (!(delta.navigationHidden == 0 || this.navigationHidden == delta.navigationHidden)) {
            changed |= 32;
            this.navigationHidden = delta.navigationHidden;
        }
        if (!(delta.orientation == 0 || this.orientation == delta.orientation)) {
            changed |= 128;
            this.orientation = delta.orientation;
        }
        if (!((delta.screenLayout & 15) == 0 || (delta.screenLayout & 15) == (this.screenLayout & 15))) {
            changed |= 256;
            this.screenLayout = (this.screenLayout & -16) | (delta.screenLayout & 15);
        }
        if (!((delta.screenLayout & 48) == 0 || (delta.screenLayout & 48) == (this.screenLayout & 48))) {
            changed |= 256;
            this.screenLayout = (this.screenLayout & -49) | (delta.screenLayout & 48);
        }
        if (!((delta.screenLayout & 768) == 0 || (delta.screenLayout & 768) == (this.screenLayout & 768))) {
            changed |= 256;
            this.screenLayout = (this.screenLayout & -769) | (delta.screenLayout & 768);
        }
        if (!((delta.screenLayout & 268435456) == (this.screenLayout & 268435456) || delta.screenLayout == 0)) {
            changed |= 256;
            this.screenLayout = (this.screenLayout & -268435457) | (268435456 & delta.screenLayout);
        }
        if (!((delta.colorMode & 3) == 0 || (delta.colorMode & 3) == (this.colorMode & 3))) {
            changed |= 16384;
            this.colorMode = (this.colorMode & -4) | (delta.colorMode & 3);
        }
        if (!((delta.colorMode & 12) == 0 || (delta.colorMode & 12) == (this.colorMode & 12))) {
            changed |= 16384;
            this.colorMode = (this.colorMode & -13) | (delta.colorMode & 12);
        }
        if (!(delta.uiMode == 0 || this.uiMode == delta.uiMode)) {
            changed |= 512;
            if ((delta.uiMode & 15) != 0) {
                this.uiMode = (this.uiMode & -16) | (delta.uiMode & 15);
            }
            if ((delta.uiMode & 48) != 0) {
                this.uiMode = (this.uiMode & -49) | (delta.uiMode & 48);
            }
        }
        if (!(delta.screenWidthDp == 0 || this.screenWidthDp == delta.screenWidthDp)) {
            changed |= 1024;
            this.screenWidthDp = delta.screenWidthDp;
        }
        if (!(delta.screenHeightDp == 0 || this.screenHeightDp == delta.screenHeightDp)) {
            changed |= 1024;
            this.screenHeightDp = delta.screenHeightDp;
        }
        if (!(delta.smallestScreenWidthDp == 0 || this.smallestScreenWidthDp == delta.smallestScreenWidthDp)) {
            changed |= 2048;
            this.smallestScreenWidthDp = delta.smallestScreenWidthDp;
        }
        if (!(delta.densityDpi == 0 || this.densityDpi == delta.densityDpi)) {
            changed |= 4096;
            this.densityDpi = delta.densityDpi;
        }
        if (delta.compatScreenWidthDp != 0) {
            this.compatScreenWidthDp = delta.compatScreenWidthDp;
        }
        if (delta.compatScreenHeightDp != 0) {
            this.compatScreenHeightDp = delta.compatScreenHeightDp;
        }
        if (delta.compatSmallestScreenWidthDp != 0) {
            this.compatSmallestScreenWidthDp = delta.compatSmallestScreenWidthDp;
        }
        if (!(delta.assetsSeq == 0 || delta.assetsSeq == this.assetsSeq)) {
            changed |= Integer.MIN_VALUE;
            this.assetsSeq = delta.assetsSeq;
        }
        int changed2 = changed | this.extraConfig.updateFrom(delta.extraConfig);
        if (delta.nonFullScreen != 0) {
            this.nonFullScreen = delta.nonFullScreen;
        }
        if (delta.seq != 0) {
            this.seq = delta.seq;
        }
        if (this.windowConfiguration.updateFrom(delta.windowConfiguration) != 0) {
            return changed2 | 536870912;
        }
        return changed2;
    }

    public int diff(Configuration delta) {
        return diff(delta, false, false);
    }

    public int diffPublicOnly(Configuration delta) {
        return diff(delta, false, true);
    }

    public int diff(Configuration delta, boolean compareUndefined, boolean publicOnly) {
        int changed = 0;
        if ((compareUndefined || delta.fontScale > 0.0f) && this.fontScale != delta.fontScale) {
            changed = 0 | 1073741824;
        }
        if ((compareUndefined || delta.mcc != 0) && this.mcc != delta.mcc) {
            changed |= 1;
        }
        if ((compareUndefined || delta.mnc != 0) && this.mnc != delta.mnc) {
            changed |= 2;
        }
        int changed2 = changed | this.extraConfig.diff(delta.extraConfig);
        fixUpLocaleList();
        delta.fixUpLocaleList();
        if ((compareUndefined || !delta.mLocaleList.isEmpty()) && !this.mLocaleList.equals(delta.mLocaleList)) {
            changed2 = changed2 | 4 | 8192;
        }
        int deltaScreenLayoutDir = delta.screenLayout & 192;
        if ((compareUndefined || deltaScreenLayoutDir != 0) && deltaScreenLayoutDir != (this.screenLayout & 192)) {
            changed2 |= 8192;
        }
        if ((compareUndefined || delta.touchscreen != 0) && this.touchscreen != delta.touchscreen) {
            changed2 |= 8;
        }
        if ((compareUndefined || delta.keyboard != 0) && this.keyboard != delta.keyboard) {
            changed2 |= 16;
        }
        if ((compareUndefined || delta.keyboardHidden != 0) && this.keyboardHidden != delta.keyboardHidden) {
            changed2 |= 32;
        }
        if ((compareUndefined || delta.hardKeyboardHidden != 0) && this.hardKeyboardHidden != delta.hardKeyboardHidden) {
            changed2 |= 32;
        }
        if ((compareUndefined || delta.navigation != 0) && this.navigation != delta.navigation) {
            changed2 |= 64;
        }
        if ((compareUndefined || delta.navigationHidden != 0) && this.navigationHidden != delta.navigationHidden) {
            changed2 |= 32;
        }
        if ((compareUndefined || delta.orientation != 0) && this.orientation != delta.orientation) {
            changed2 |= 128;
        }
        if ((compareUndefined || getScreenLayoutNoDirection(delta.screenLayout) != 0) && getScreenLayoutNoDirection(this.screenLayout) != getScreenLayoutNoDirection(delta.screenLayout)) {
            changed2 |= 256;
        }
        if ((compareUndefined || (delta.colorMode & 12) != 0) && (this.colorMode & 12) != (delta.colorMode & 12)) {
            changed2 |= 16384;
        }
        if ((compareUndefined || (delta.colorMode & 3) != 0) && (this.colorMode & 3) != (delta.colorMode & 3)) {
            changed2 |= 16384;
        }
        if ((compareUndefined || delta.uiMode != 0) && this.uiMode != delta.uiMode) {
            changed2 |= 512;
        }
        if ((compareUndefined || delta.screenWidthDp != 0) && this.screenWidthDp != delta.screenWidthDp) {
            changed2 |= 1024;
        }
        if ((compareUndefined || delta.screenHeightDp != 0) && this.screenHeightDp != delta.screenHeightDp) {
            changed2 |= 1024;
        }
        if ((compareUndefined || delta.smallestScreenWidthDp != 0) && this.smallestScreenWidthDp != delta.smallestScreenWidthDp) {
            changed2 |= 2048;
        }
        if ((compareUndefined || delta.densityDpi != 0) && this.densityDpi != delta.densityDpi) {
            changed2 |= 4096;
        }
        if ((compareUndefined || delta.assetsSeq != 0) && this.assetsSeq != delta.assetsSeq) {
            changed2 |= Integer.MIN_VALUE;
        }
        if (publicOnly || this.windowConfiguration.diff(delta.windowConfiguration, compareUndefined) == 0) {
            return changed2;
        }
        return changed2 | 536870912;
    }

    public static boolean needNewResources(int configChanges, int interestingChanges) {
        return (configChanges & ((((Integer.MIN_VALUE | interestingChanges) | 1073741824) | 32768) | 256)) != 0;
    }

    public boolean isOtherSeqNewer(Configuration other) {
        boolean z = false;
        if (other == null) {
            return false;
        }
        if (other.seq == 0 || this.seq == 0) {
            return true;
        }
        int diff = other.seq - this.seq;
        if (diff > 65536) {
            return false;
        }
        if (diff > 0) {
            z = true;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.fontScale);
        dest.writeInt(this.mcc);
        dest.writeInt(this.mnc);
        fixUpLocaleList();
        dest.writeParcelable(this.mLocaleList, flags);
        if (this.userSetLocale) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.touchscreen);
        dest.writeInt(this.keyboard);
        dest.writeInt(this.keyboardHidden);
        dest.writeInt(this.hardKeyboardHidden);
        dest.writeInt(this.navigation);
        dest.writeInt(this.navigationHidden);
        dest.writeInt(this.orientation);
        dest.writeInt(this.screenLayout);
        dest.writeInt(this.colorMode);
        dest.writeInt(this.uiMode);
        dest.writeInt(this.screenWidthDp);
        dest.writeInt(this.screenHeightDp);
        dest.writeInt(this.smallestScreenWidthDp);
        dest.writeInt(this.densityDpi);
        dest.writeInt(this.compatScreenWidthDp);
        dest.writeInt(this.compatScreenHeightDp);
        dest.writeInt(this.compatSmallestScreenWidthDp);
        dest.writeValue(this.windowConfiguration);
        dest.writeInt(this.assetsSeq);
        this.extraConfig.writeToParcel(dest, flags);
        dest.writeInt(this.nonFullScreen);
        dest.writeInt(this.seq);
    }

    public void readFromParcel(Parcel source) {
        this.fontScale = source.readFloat();
        this.mcc = source.readInt();
        this.mnc = source.readInt();
        this.mLocaleList = (LocaleList) source.readParcelable(LocaleList.class.getClassLoader());
        boolean z = false;
        this.locale = this.mLocaleList.get(0);
        if (source.readInt() == 1) {
            z = true;
        }
        this.userSetLocale = z;
        this.touchscreen = source.readInt();
        this.keyboard = source.readInt();
        this.keyboardHidden = source.readInt();
        this.hardKeyboardHidden = source.readInt();
        this.navigation = source.readInt();
        this.navigationHidden = source.readInt();
        this.orientation = source.readInt();
        this.screenLayout = source.readInt();
        this.colorMode = source.readInt();
        this.uiMode = source.readInt();
        this.screenWidthDp = source.readInt();
        this.screenHeightDp = source.readInt();
        this.smallestScreenWidthDp = source.readInt();
        this.densityDpi = source.readInt();
        this.compatScreenWidthDp = source.readInt();
        this.compatScreenHeightDp = source.readInt();
        this.compatSmallestScreenWidthDp = source.readInt();
        this.windowConfiguration.setTo((WindowConfiguration) source.readValue(null));
        this.assetsSeq = source.readInt();
        this.extraConfig.readFromParcel(source);
        this.nonFullScreen = source.readInt();
        this.seq = source.readInt();
    }

    private Configuration(Parcel source) {
        this.windowConfiguration = new WindowConfiguration();
        this.extraConfig = HwConfiguration.initHwConfiguration();
        readFromParcel(source);
    }

    public int compareTo(Configuration that) {
        float a = this.fontScale;
        float b = that.fontScale;
        if (a < b) {
            return -1;
        }
        if (a > b) {
            return 1;
        }
        int n = this.mcc - that.mcc;
        if (n != 0) {
            return n;
        }
        int n2 = this.mnc - that.mnc;
        if (n2 != 0) {
            return n2;
        }
        fixUpLocaleList();
        that.fixUpLocaleList();
        if (this.mLocaleList.isEmpty()) {
            if (!that.mLocaleList.isEmpty()) {
                return 1;
            }
        } else if (that.mLocaleList.isEmpty()) {
            return -1;
        } else {
            int minSize = Math.min(this.mLocaleList.size(), that.mLocaleList.size());
            for (int i = 0; i < minSize; i++) {
                Locale thisLocale = this.mLocaleList.get(i);
                Locale thatLocale = that.mLocaleList.get(i);
                int n3 = thisLocale.getLanguage().compareTo(thatLocale.getLanguage());
                if (n3 != 0) {
                    return n3;
                }
                int n4 = thisLocale.getCountry().compareTo(thatLocale.getCountry());
                if (n4 != 0) {
                    return n4;
                }
                int n5 = thisLocale.getVariant().compareTo(thatLocale.getVariant());
                if (n5 != 0) {
                    return n5;
                }
                int n6 = thisLocale.toLanguageTag().compareTo(thatLocale.toLanguageTag());
                if (n6 != 0) {
                    return n6;
                }
            }
            int n7 = this.mLocaleList.size() - that.mLocaleList.size();
            if (n7 != 0) {
                return n7;
            }
        }
        int n8 = this.touchscreen - that.touchscreen;
        if (n8 != 0) {
            return n8;
        }
        int n9 = this.keyboard - that.keyboard;
        if (n9 != 0) {
            return n9;
        }
        int n10 = this.keyboardHidden - that.keyboardHidden;
        if (n10 != 0) {
            return n10;
        }
        int n11 = this.hardKeyboardHidden - that.hardKeyboardHidden;
        if (n11 != 0) {
            return n11;
        }
        int n12 = this.navigation - that.navigation;
        if (n12 != 0) {
            return n12;
        }
        int n13 = this.navigationHidden - that.navigationHidden;
        if (n13 != 0) {
            return n13;
        }
        int n14 = this.orientation - that.orientation;
        if (n14 != 0) {
            return n14;
        }
        int n15 = this.colorMode - that.colorMode;
        if (n15 != 0) {
            return n15;
        }
        int n16 = this.screenLayout - that.screenLayout;
        if (n16 != 0) {
            return n16;
        }
        int n17 = this.uiMode - that.uiMode;
        if (n17 != 0) {
            return n17;
        }
        int n18 = this.screenWidthDp - that.screenWidthDp;
        if (n18 != 0) {
            return n18;
        }
        int n19 = this.screenHeightDp - that.screenHeightDp;
        if (n19 != 0) {
            return n19;
        }
        int n20 = this.smallestScreenWidthDp - that.smallestScreenWidthDp;
        if (n20 != 0) {
            return n20;
        }
        int n21 = this.densityDpi - that.densityDpi;
        if (n21 != 0) {
            return n21;
        }
        int n22 = this.assetsSeq - that.assetsSeq;
        if (n22 != 0) {
            return n22;
        }
        int n23 = this.windowConfiguration.compareTo(that.windowConfiguration);
        if (n23 != 0) {
            return n23;
        }
        return this.extraConfig.compareTo(that.extraConfig);
    }

    public boolean equals(Configuration that) {
        boolean z = false;
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if (compareTo(that) == 0) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object that) {
        try {
            return equals((Configuration) that);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * 17) + Float.floatToIntBits(this.fontScale))) + this.mcc)) + this.mnc)) + this.mLocaleList.hashCode())) + this.touchscreen)) + this.keyboard)) + this.keyboardHidden)) + this.hardKeyboardHidden)) + this.navigation)) + this.navigationHidden)) + this.orientation)) + this.screenLayout)) + this.colorMode)) + this.uiMode)) + this.screenWidthDp)) + this.screenHeightDp)) + this.smallestScreenWidthDp)) + this.densityDpi)) + this.assetsSeq)) + this.extraConfig.hashCode();
    }

    public LocaleList getLocales() {
        fixUpLocaleList();
        return this.mLocaleList;
    }

    public void setLocales(LocaleList locales) {
        this.mLocaleList = locales == null ? LocaleList.getEmptyLocaleList() : locales;
        this.locale = this.mLocaleList.get(0);
        setLayoutDirection(this.locale);
    }

    public void setLocale(Locale loc) {
        LocaleList localeList;
        if (loc == null) {
            localeList = LocaleList.getEmptyLocaleList();
        } else {
            localeList = new LocaleList(new Locale[]{loc});
        }
        setLocales(localeList);
    }

    public void clearLocales() {
        this.mLocaleList = LocaleList.getEmptyLocaleList();
        this.locale = null;
    }

    public int getLayoutDirection() {
        return (this.screenLayout & 192) == 128 ? 1 : 0;
    }

    public void setLayoutDirection(Locale loc) {
        this.screenLayout = (this.screenLayout & -193) | ((1 + TextUtils.getLayoutDirectionFromLocale(loc)) << 6);
    }

    private static int getScreenLayoutNoDirection(int screenLayout2) {
        return screenLayout2 & -193;
    }

    public boolean isScreenRound() {
        return (this.screenLayout & 768) == 512;
    }

    public boolean isScreenWideColorGamut() {
        return (this.colorMode & 3) == 2;
    }

    public boolean isScreenHdr() {
        return (this.colorMode & 12) == 8;
    }

    public static String localesToResourceQualifier(LocaleList locs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locs.size(); i++) {
            Locale loc = locs.get(i);
            int l = loc.getLanguage().length();
            if (l != 0) {
                int s = loc.getScript().length();
                int c = loc.getCountry().length();
                int v = loc.getVariant().length();
                if (sb.length() != 0) {
                    sb.append(",");
                }
                if (l == 2 && s == 0 && ((c == 0 || c == 2) && v == 0)) {
                    sb.append(loc.getLanguage());
                    if (c == 2) {
                        sb.append("-r");
                        sb.append(loc.getCountry());
                    }
                } else {
                    sb.append("b+");
                    sb.append(loc.getLanguage());
                    if (s != 0) {
                        sb.append("+");
                        sb.append(loc.getScript());
                    }
                    if (c != 0) {
                        sb.append("+");
                        sb.append(loc.getCountry());
                    }
                    if (v != 0) {
                        sb.append("+");
                        sb.append(loc.getVariant());
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String resourceQualifierString(Configuration config) {
        return resourceQualifierString(config, null);
    }

    public static String resourceQualifierString(Configuration config, DisplayMetrics metrics) {
        int height;
        int width;
        ArrayList<String> parts = new ArrayList<>();
        if (config.mcc != 0) {
            parts.add(XML_ATTR_MCC + config.mcc);
            if (config.mnc != 0) {
                parts.add(XML_ATTR_MNC + config.mnc);
            }
        }
        if (!config.mLocaleList.isEmpty()) {
            String resourceQualifier = localesToResourceQualifier(config.mLocaleList);
            if (!resourceQualifier.isEmpty()) {
                parts.add(resourceQualifier);
            }
        }
        int i = config.screenLayout & 192;
        if (i == 64) {
            parts.add("ldltr");
        } else if (i == 128) {
            parts.add("ldrtl");
        }
        int srcDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        int realDpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
        if (realDpi == srcDpi) {
            if (config.smallestScreenWidthDp != 0) {
                parts.add(XML_ATTR_SMALLEST_WIDTH + config.smallestScreenWidthDp + "dp");
            }
            if (config.screenWidthDp != 0) {
                parts.add("w" + config.screenWidthDp + "dp");
            }
            if (config.screenHeightDp != 0) {
                parts.add("h" + config.screenHeightDp + "dp");
            }
        } else {
            int smallestScreenWidthDp2 = (config.smallestScreenWidthDp * realDpi) / srcDpi;
            int screenWidthDp2 = (config.screenWidthDp * realDpi) / srcDpi;
            int screenHeightDp2 = (config.screenHeightDp * realDpi) / srcDpi;
            if (config.smallestScreenWidthDp != 0) {
                parts.add(XML_ATTR_SMALLEST_WIDTH + smallestScreenWidthDp2 + "dp");
            }
            if (screenWidthDp2 != 0) {
                parts.add("w" + screenWidthDp2 + "dp");
            }
            if (screenHeightDp2 != 0) {
                parts.add("h" + screenHeightDp2 + "dp");
            }
        }
        switch (config.screenLayout & 15) {
            case 1:
                parts.add("small");
                break;
            case 2:
                parts.add("normal");
                break;
            case 3:
                parts.add(Slice.HINT_LARGE);
                break;
            case 4:
                parts.add("xlarge");
                break;
        }
        int i2 = config.screenLayout & 48;
        if (i2 == 16) {
            parts.add("notlong");
        } else if (i2 == 32) {
            parts.add("long");
        }
        int i3 = config.screenLayout & 768;
        if (i3 == 256) {
            parts.add("notround");
        } else if (i3 == 512) {
            parts.add("round");
        }
        int i4 = config.colorMode & 12;
        if (i4 == 4) {
            parts.add("lowdr");
        } else if (i4 == 8) {
            parts.add("highdr");
        }
        switch (config.colorMode & 3) {
            case 1:
                parts.add("nowidecg");
                break;
            case 2:
                parts.add("widecg");
                break;
        }
        switch (config.orientation) {
            case 1:
                parts.add(UsbManager.EXTRA_PORT);
                break;
            case 2:
                parts.add("land");
                break;
        }
        switch (config.uiMode & 15) {
            case 2:
                parts.add("desk");
                break;
            case 3:
                parts.add("car");
                break;
            case 4:
                parts.add("television");
                break;
            case 5:
                parts.add("appliance");
                break;
            case 6:
                parts.add("watch");
                break;
            case 7:
                parts.add("vrheadset");
                break;
        }
        int i5 = config.uiMode & 48;
        if (i5 == 16) {
            parts.add("notnight");
        } else if (i5 == 32) {
            parts.add(Camera.Parameters.SCENE_MODE_NIGHT);
        }
        int i6 = config.densityDpi;
        if (i6 != 0) {
            if (i6 == 120) {
                parts.add("ldpi");
            } else if (i6 == 160) {
                parts.add("mdpi");
            } else if (i6 == 213) {
                parts.add("tvdpi");
            } else if (i6 == 240) {
                parts.add("hdpi");
            } else if (i6 == 320) {
                parts.add("xhdpi");
            } else if (i6 == 480) {
                parts.add("xxhdpi");
            } else if (i6 != 640) {
                switch (i6) {
                    case DENSITY_DPI_ANY /*65534*/:
                        parts.add("anydpi");
                        break;
                    case 65535:
                        parts.add("nodpi");
                        break;
                    default:
                        parts.add(config.densityDpi + "dpi");
                        break;
                }
            } else {
                parts.add("xxxhdpi");
            }
        }
        int i7 = config.touchscreen;
        if (i7 == 1) {
            parts.add("notouch");
        } else if (i7 == 3) {
            parts.add("finger");
        }
        switch (config.keyboardHidden) {
            case 1:
                parts.add("keysexposed");
                break;
            case 2:
                parts.add("keyshidden");
                break;
            case 3:
                parts.add("keyssoft");
                break;
        }
        switch (config.keyboard) {
            case 1:
                parts.add("nokeys");
                break;
            case 2:
                parts.add("qwerty");
                break;
            case 3:
                parts.add("12key");
                break;
        }
        switch (config.navigationHidden) {
            case 1:
                parts.add("navexposed");
                break;
            case 2:
                parts.add("navhidden");
                break;
        }
        switch (config.navigation) {
            case 1:
                parts.add("nonav");
                break;
            case 2:
                parts.add("dpad");
                break;
            case 3:
                parts.add("trackball");
                break;
            case 4:
                parts.add("wheel");
                break;
        }
        if (metrics != null) {
            if (metrics.widthPixels >= metrics.heightPixels) {
                width = metrics.widthPixels;
                height = metrics.heightPixels;
            } else {
                width = metrics.heightPixels;
                height = metrics.widthPixels;
            }
            parts.add(width + "x" + height);
        }
        parts.add("v" + Build.VERSION.RESOURCES_SDK_INT);
        return TextUtils.join("-", parts);
    }

    public static Configuration generateDelta(Configuration base, Configuration change) {
        Configuration delta = new Configuration();
        if (base.fontScale != change.fontScale) {
            delta.fontScale = change.fontScale;
        }
        if (base.mcc != change.mcc) {
            delta.mcc = change.mcc;
        }
        if (base.mnc != change.mnc) {
            delta.mnc = change.mnc;
        }
        base.fixUpLocaleList();
        change.fixUpLocaleList();
        if (!base.mLocaleList.equals(change.mLocaleList)) {
            delta.mLocaleList = change.mLocaleList;
            delta.locale = change.locale;
        }
        if (base.touchscreen != change.touchscreen) {
            delta.touchscreen = change.touchscreen;
        }
        if (base.keyboard != change.keyboard) {
            delta.keyboard = change.keyboard;
        }
        if (base.keyboardHidden != change.keyboardHidden) {
            delta.keyboardHidden = change.keyboardHidden;
        }
        if (base.navigation != change.navigation) {
            delta.navigation = change.navigation;
        }
        if (base.navigationHidden != change.navigationHidden) {
            delta.navigationHidden = change.navigationHidden;
        }
        if (base.orientation != change.orientation) {
            delta.orientation = change.orientation;
        }
        if ((base.screenLayout & 15) != (change.screenLayout & 15)) {
            delta.screenLayout |= change.screenLayout & 15;
        }
        if ((base.screenLayout & 192) != (change.screenLayout & 192)) {
            delta.screenLayout |= change.screenLayout & 192;
        }
        if ((base.screenLayout & 48) != (change.screenLayout & 48)) {
            delta.screenLayout |= change.screenLayout & 48;
        }
        if ((base.screenLayout & 768) != (change.screenLayout & 768)) {
            delta.screenLayout |= change.screenLayout & 768;
        }
        if ((base.colorMode & 3) != (change.colorMode & 3)) {
            delta.colorMode |= change.colorMode & 3;
        }
        if ((base.colorMode & 12) != (change.colorMode & 12)) {
            delta.colorMode |= change.colorMode & 12;
        }
        if ((base.uiMode & 15) != (change.uiMode & 15)) {
            delta.uiMode |= change.uiMode & 15;
        }
        if ((base.uiMode & 48) != (change.uiMode & 48)) {
            delta.uiMode |= change.uiMode & 48;
        }
        if (base.screenWidthDp != change.screenWidthDp) {
            delta.screenWidthDp = change.screenWidthDp;
        }
        if (base.screenHeightDp != change.screenHeightDp) {
            delta.screenHeightDp = change.screenHeightDp;
        }
        if (base.smallestScreenWidthDp != change.smallestScreenWidthDp) {
            delta.smallestScreenWidthDp = change.smallestScreenWidthDp;
        }
        if (base.densityDpi != change.densityDpi) {
            delta.densityDpi = change.densityDpi;
        }
        if (base.assetsSeq != change.assetsSeq) {
            delta.assetsSeq = change.assetsSeq;
        }
        if (!base.windowConfiguration.equals(change.windowConfiguration)) {
            delta.windowConfiguration.setTo(change.windowConfiguration);
        }
        return delta;
    }

    public static void readXmlAttrs(XmlPullParser parser, Configuration configOut) throws XmlPullParserException, IOException {
        configOut.fontScale = Float.intBitsToFloat(XmlUtils.readIntAttribute(parser, XML_ATTR_FONT_SCALE, 0));
        configOut.mcc = XmlUtils.readIntAttribute(parser, XML_ATTR_MCC, 0);
        configOut.mnc = XmlUtils.readIntAttribute(parser, XML_ATTR_MNC, 0);
        configOut.mLocaleList = LocaleList.forLanguageTags(XmlUtils.readStringAttribute(parser, XML_ATTR_LOCALES));
        configOut.locale = configOut.mLocaleList.get(0);
        configOut.touchscreen = XmlUtils.readIntAttribute(parser, XML_ATTR_TOUCHSCREEN, 0);
        configOut.keyboard = XmlUtils.readIntAttribute(parser, XML_ATTR_KEYBOARD, 0);
        configOut.keyboardHidden = XmlUtils.readIntAttribute(parser, XML_ATTR_KEYBOARD_HIDDEN, 0);
        configOut.hardKeyboardHidden = XmlUtils.readIntAttribute(parser, XML_ATTR_HARD_KEYBOARD_HIDDEN, 0);
        configOut.navigation = XmlUtils.readIntAttribute(parser, XML_ATTR_NAVIGATION, 0);
        configOut.navigationHidden = XmlUtils.readIntAttribute(parser, XML_ATTR_NAVIGATION_HIDDEN, 0);
        configOut.orientation = XmlUtils.readIntAttribute(parser, XML_ATTR_ORIENTATION, 0);
        configOut.screenLayout = XmlUtils.readIntAttribute(parser, XML_ATTR_SCREEN_LAYOUT, 0);
        configOut.colorMode = XmlUtils.readIntAttribute(parser, XML_ATTR_COLOR_MODE, 0);
        configOut.uiMode = XmlUtils.readIntAttribute(parser, XML_ATTR_UI_MODE, 0);
        configOut.screenWidthDp = XmlUtils.readIntAttribute(parser, "width", 0);
        configOut.screenHeightDp = XmlUtils.readIntAttribute(parser, "height", 0);
        configOut.smallestScreenWidthDp = XmlUtils.readIntAttribute(parser, XML_ATTR_SMALLEST_WIDTH, 0);
        configOut.densityDpi = XmlUtils.readIntAttribute(parser, XML_ATTR_DENSITY, 0);
    }

    public static void writeXmlAttrs(XmlSerializer xml, Configuration config) throws IOException {
        XmlUtils.writeIntAttribute(xml, XML_ATTR_FONT_SCALE, Float.floatToIntBits(config.fontScale));
        if (config.mcc != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_MCC, config.mcc);
        }
        if (config.mnc != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_MNC, config.mnc);
        }
        config.fixUpLocaleList();
        if (!config.mLocaleList.isEmpty()) {
            XmlUtils.writeStringAttribute(xml, XML_ATTR_LOCALES, config.mLocaleList.toLanguageTags());
        }
        if (config.touchscreen != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_TOUCHSCREEN, config.touchscreen);
        }
        if (config.keyboard != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_KEYBOARD, config.keyboard);
        }
        if (config.keyboardHidden != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_KEYBOARD_HIDDEN, config.keyboardHidden);
        }
        if (config.hardKeyboardHidden != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_HARD_KEYBOARD_HIDDEN, config.hardKeyboardHidden);
        }
        if (config.navigation != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_NAVIGATION, config.navigation);
        }
        if (config.navigationHidden != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_NAVIGATION_HIDDEN, config.navigationHidden);
        }
        if (config.orientation != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_ORIENTATION, config.orientation);
        }
        if (config.screenLayout != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SCREEN_LAYOUT, config.screenLayout);
        }
        if (config.colorMode != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_COLOR_MODE, config.colorMode);
        }
        if (config.uiMode != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_UI_MODE, config.uiMode);
        }
        if (config.screenWidthDp != 0) {
            XmlUtils.writeIntAttribute(xml, "width", config.screenWidthDp);
        }
        if (config.screenHeightDp != 0) {
            XmlUtils.writeIntAttribute(xml, "height", config.screenHeightDp);
        }
        if (config.smallestScreenWidthDp != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SMALLEST_WIDTH, config.smallestScreenWidthDp);
        }
        if (config.densityDpi != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_DENSITY, config.densityDpi);
        }
    }
}
