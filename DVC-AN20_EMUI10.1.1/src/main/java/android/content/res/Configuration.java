package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.app.WindowConfiguration;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.content.ConfigurationProto;
import android.content.ResourcesConfigurationProto;
import android.hardware.Camera;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.proto.ProtoInputStream;
import android.util.proto.ProtoOutputStream;
import android.util.proto.WireTypeMismatchException;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.List;
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
        /* class android.content.res.Configuration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Configuration createFromParcel(Parcel source) {
            return new Configuration(source);
        }

        @Override // android.os.Parcelable.Creator
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
    private static final String TAG = "Configuration";
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
    @UnsupportedAppUsage
    public int seq;
    public int smallestScreenWidthDp;
    public int touchscreen;
    public int uiMode;
    @UnsupportedAppUsage
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
        boolean screenLayoutLong;
        int screenLayoutSize;
        if (longSizeDp < 470) {
            screenLayoutSize = 1;
            screenLayoutLong = false;
            screenLayoutCompatNeeded = false;
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
                screenLayoutLong = true;
            } else {
                screenLayoutLong = false;
            }
        }
        if (!screenLayoutLong) {
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
        if (cur != 0 && cur >= size) {
            return true;
        }
        return false;
    }

    public Configuration() {
        this.windowConfiguration = new WindowConfiguration();
        this.extraConfig = HwFrameworkFactory.getHwConfiguration();
        unset();
    }

    public Configuration(Configuration o) {
        this.windowConfiguration = new WindowConfiguration();
        this.extraConfig = HwFrameworkFactory.getHwConfiguration();
        setTo(o);
    }

    private void fixUpLocaleList() {
        Locale locale2;
        if ((this.locale == null && !this.mLocaleList.isEmpty()) || ((locale2 = this.locale) != null && !locale2.equals(this.mLocaleList.get(0)))) {
            Locale locale3 = this.locale;
            this.mLocaleList = locale3 == null ? LocaleList.getEmptyLocaleList() : new LocaleList(locale3);
        }
    }

    public void setTo(Configuration o) {
        this.fontScale = o.fontScale;
        this.mcc = o.mcc;
        this.mnc = o.mnc;
        Locale locale2 = o.locale;
        this.locale = locale2 == null ? null : (Locale) locale2.clone();
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
        this.seq = o.seq;
        this.windowConfiguration.setTo(o.windowConfiguration);
        this.extraConfig.setTo(o.extraConfig);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        sb.append(this.fontScale);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        int i = this.mcc;
        if (i != 0) {
            sb.append(i);
            sb.append("mcc");
        } else {
            sb.append("?mcc");
        }
        int i2 = this.mnc;
        if (i2 != 0) {
            sb.append(i2);
            sb.append("mnc");
        } else {
            sb.append("?mnc");
        }
        fixUpLocaleList();
        if (!this.mLocaleList.isEmpty()) {
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
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
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.densityDpi);
            sb.append("dpi");
        } else {
            sb.append(" ?density");
        }
        int i3 = this.screenLayout & 15;
        if (i3 == 0) {
            sb.append(" ?lsize");
        } else if (i3 == 1) {
            sb.append(" smll");
        } else if (i3 == 2) {
            sb.append(" nrml");
        } else if (i3 == 3) {
            sb.append(" lrg");
        } else if (i3 != 4) {
            sb.append(" layoutSize=");
            sb.append(this.screenLayout & 15);
        } else {
            sb.append(" xlrg");
        }
        int i4 = this.screenLayout & 48;
        if (i4 == 0) {
            sb.append(" ?long");
        } else if (i4 != 16) {
            if (i4 != 32) {
                sb.append(" layoutLong=");
                sb.append(this.screenLayout & 48);
            } else {
                sb.append(" long");
            }
        }
        int i5 = this.colorMode & 12;
        if (i5 == 0) {
            sb.append(" ?ldr");
        } else if (i5 != 4) {
            if (i5 != 8) {
                sb.append(" dynamicRange=");
                sb.append(this.colorMode & 12);
            } else {
                sb.append(" hdr");
            }
        }
        int i6 = this.colorMode & 3;
        if (i6 == 0) {
            sb.append(" ?wideColorGamut");
        } else if (i6 != 1) {
            if (i6 != 2) {
                sb.append(" wideColorGamut=");
                sb.append(this.colorMode & 3);
            } else {
                sb.append(" widecg");
            }
        }
        int i7 = this.orientation;
        if (i7 == 0) {
            sb.append(" ?orien");
        } else if (i7 == 1) {
            sb.append(" port");
        } else if (i7 != 2) {
            sb.append(" orien=");
            sb.append(this.orientation);
        } else {
            sb.append(" land");
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
        int i8 = this.uiMode & 48;
        if (i8 == 0) {
            sb.append(" ?night");
        } else if (i8 != 16) {
            if (i8 != 32) {
                sb.append(" night=");
                sb.append(this.uiMode & 48);
            } else {
                sb.append(" night");
            }
        }
        int i9 = this.touchscreen;
        if (i9 == 0) {
            sb.append(" ?touch");
        } else if (i9 == 1) {
            sb.append(" -touch");
        } else if (i9 == 2) {
            sb.append(" stylus");
        } else if (i9 != 3) {
            sb.append(" touch=");
            sb.append(this.touchscreen);
        } else {
            sb.append(" finger");
        }
        int i10 = this.keyboard;
        if (i10 == 0) {
            sb.append(" ?keyb");
        } else if (i10 == 1) {
            sb.append(" -keyb");
        } else if (i10 == 2) {
            sb.append(" qwerty");
        } else if (i10 != 3) {
            sb.append(" keys=");
            sb.append(this.keyboard);
        } else {
            sb.append(" 12key");
        }
        int i11 = this.keyboardHidden;
        if (i11 == 0) {
            sb.append("/?");
        } else if (i11 == 1) {
            sb.append("/v");
        } else if (i11 == 2) {
            sb.append("/h");
        } else if (i11 != 3) {
            sb.append("/");
            sb.append(this.keyboardHidden);
        } else {
            sb.append("/s");
        }
        int i12 = this.hardKeyboardHidden;
        if (i12 == 0) {
            sb.append("/?");
        } else if (i12 == 1) {
            sb.append("/v");
        } else if (i12 != 2) {
            sb.append("/");
            sb.append(this.hardKeyboardHidden);
        } else {
            sb.append("/h");
        }
        int i13 = this.navigation;
        if (i13 == 0) {
            sb.append(" ?nav");
        } else if (i13 == 1) {
            sb.append(" -nav");
        } else if (i13 == 2) {
            sb.append(" dpad");
        } else if (i13 == 3) {
            sb.append(" tball");
        } else if (i13 != 4) {
            sb.append(" nav=");
            sb.append(this.navigation);
        } else {
            sb.append(" wheel");
        }
        int i14 = this.navigationHidden;
        if (i14 == 0) {
            sb.append("/?");
        } else if (i14 == 1) {
            sb.append("/v");
        } else if (i14 != 2) {
            sb.append("/");
            sb.append(this.navigationHidden);
        } else {
            sb.append("/h");
        }
        sb.append(" winConfig=");
        sb.append(this.windowConfiguration);
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

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId, boolean persisted, boolean critical) {
        WindowConfiguration windowConfiguration2;
        long token = protoOutputStream.start(fieldId);
        if (!critical) {
            protoOutputStream.write(1108101562369L, this.fontScale);
            protoOutputStream.write(1155346202626L, this.mcc);
            protoOutputStream.write(1155346202627L, this.mnc);
            LocaleList localeList = this.mLocaleList;
            if (localeList != null) {
                localeList.writeToProto(protoOutputStream, 2246267895812L);
            }
            protoOutputStream.write(1155346202629L, this.screenLayout);
            protoOutputStream.write(1155346202630L, this.colorMode);
            protoOutputStream.write(ConfigurationProto.TOUCHSCREEN, this.touchscreen);
            protoOutputStream.write(1155346202632L, this.keyboard);
            protoOutputStream.write(ConfigurationProto.KEYBOARD_HIDDEN, this.keyboardHidden);
            protoOutputStream.write(ConfigurationProto.HARD_KEYBOARD_HIDDEN, this.hardKeyboardHidden);
            protoOutputStream.write(ConfigurationProto.NAVIGATION, this.navigation);
            protoOutputStream.write(ConfigurationProto.NAVIGATION_HIDDEN, this.navigationHidden);
            protoOutputStream.write(ConfigurationProto.UI_MODE, this.uiMode);
            protoOutputStream.write(ConfigurationProto.SMALLEST_SCREEN_WIDTH_DP, this.smallestScreenWidthDp);
            protoOutputStream.write(ConfigurationProto.DENSITY_DPI, this.densityDpi);
            if (!persisted && (windowConfiguration2 = this.windowConfiguration) != null) {
                windowConfiguration2.writeToProto(protoOutputStream, 1146756268051L);
            }
        }
        protoOutputStream.write(ConfigurationProto.ORIENTATION, this.orientation);
        protoOutputStream.write(ConfigurationProto.SCREEN_WIDTH_DP, this.screenWidthDp);
        protoOutputStream.write(ConfigurationProto.SCREEN_HEIGHT_DP, this.screenHeightDp);
        protoOutputStream.end(token);
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        writeToProto(protoOutputStream, fieldId, false, false);
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId, boolean critical) {
        writeToProto(protoOutputStream, fieldId, false, critical);
    }

    /* JADX INFO: Multiple debug info for r8v1 java.lang.String: [D('variant' java.lang.String), D('country' java.lang.String), D('language' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x02e6  */
    public void readFromProto(ProtoInputStream protoInputStream, long fieldId) throws IOException {
        long token;
        long token2;
        String str;
        WireTypeMismatchException wireTypeMismatchException;
        String script;
        String variant;
        String country;
        String script2;
        String variant2;
        String country2;
        long token3;
        String str2;
        String language = "";
        long token4 = protoInputStream.start(fieldId);
        List<Locale> list = new ArrayList<>();
        while (true) {
            try {
                if (protoInputStream.nextField() != -1) {
                    try {
                        switch (protoInputStream.getFieldNumber()) {
                            case 1:
                                str = language;
                                token2 = token4;
                                this.fontScale = protoInputStream.readFloat(1108101562369L);
                                break;
                            case 2:
                                str = language;
                                token2 = token4;
                                this.mcc = protoInputStream.readInt(1155346202626L);
                                break;
                            case 3:
                                str = language;
                                token2 = token4;
                                this.mnc = protoInputStream.readInt(1155346202627L);
                                break;
                            case 4:
                                long localeToken = protoInputStream.start(2246267895812L);
                                String country3 = language;
                                String variant3 = language;
                                String script3 = language;
                                String language2 = language;
                                for (int i = -1; protoInputStream.nextField() != i; i = -1) {
                                    try {
                                        try {
                                            int fieldNumber = protoInputStream.getFieldNumber();
                                            if (fieldNumber == 1) {
                                                str2 = language;
                                                token3 = token4;
                                                language2 = protoInputStream.readString(1138166333441L);
                                            } else if (fieldNumber == 2) {
                                                str2 = language;
                                                token3 = token4;
                                                country3 = protoInputStream.readString(1138166333442L);
                                            } else if (fieldNumber == 3) {
                                                str2 = language;
                                                token3 = token4;
                                                variant3 = protoInputStream.readString(1138166333443L);
                                            } else if (fieldNumber != 4) {
                                                str2 = language;
                                                token3 = token4;
                                            } else {
                                                str2 = language;
                                                token3 = token4;
                                                try {
                                                    script3 = protoInputStream.readString(1138166333444L);
                                                } catch (WireTypeMismatchException e) {
                                                    wtme = e;
                                                    country = country3;
                                                    variant = variant3;
                                                    script = script3;
                                                    try {
                                                        throw wtme;
                                                    } catch (Throwable wtme) {
                                                        wireTypeMismatchException = wtme;
                                                    }
                                                } catch (Throwable th) {
                                                    wireTypeMismatchException = th;
                                                    country = country3;
                                                    variant = variant3;
                                                    script = script3;
                                                    protoInputStream.end(localeToken);
                                                    try {
                                                        list.add(new Locale.Builder().setLanguage(language2).setRegion(country).setVariant(variant).setScript(script).build());
                                                    } catch (IllformedLocaleException e2) {
                                                        Slog.e(TAG, "readFromProto error building locale with: language-" + language2 + ";country-" + country + ";variant-" + variant + ";script-" + script);
                                                    }
                                                    throw wireTypeMismatchException;
                                                }
                                            }
                                            language = str2;
                                            token4 = token3;
                                        } catch (WireTypeMismatchException e3) {
                                            wtme = e3;
                                            country = country3;
                                            variant = variant3;
                                            script = script3;
                                            throw wtme;
                                        } catch (Throwable th2) {
                                            wireTypeMismatchException = th2;
                                            country = country3;
                                            variant = variant3;
                                            script = script3;
                                            protoInputStream.end(localeToken);
                                            list.add(new Locale.Builder().setLanguage(language2).setRegion(country).setVariant(variant).setScript(script).build());
                                            throw wireTypeMismatchException;
                                        }
                                    } catch (WireTypeMismatchException e4) {
                                        wtme = e4;
                                        country = country3;
                                        variant = variant3;
                                        script = script3;
                                        throw wtme;
                                    } catch (Throwable th3) {
                                        country = country3;
                                        variant = variant3;
                                        script = script3;
                                        wireTypeMismatchException = th3;
                                        protoInputStream.end(localeToken);
                                        list.add(new Locale.Builder().setLanguage(language2).setRegion(country).setVariant(variant).setScript(script).build());
                                        throw wireTypeMismatchException;
                                    }
                                }
                                str = language;
                                token2 = token4;
                                try {
                                    protoInputStream.end(localeToken);
                                    try {
                                        country2 = country3;
                                        try {
                                            variant2 = variant3;
                                            try {
                                                script2 = script3;
                                                try {
                                                    list.add(new Locale.Builder().setLanguage(language2).setRegion(country2).setVariant(variant2).setScript(script2).build());
                                                } catch (IllformedLocaleException e5) {
                                                    e = e5;
                                                }
                                            } catch (IllformedLocaleException e6) {
                                                e = e6;
                                                script2 = script3;
                                                Slog.e(TAG, "readFromProto error building locale with: language-" + language2 + ";country-" + country2 + ";variant-" + variant2 + ";script-" + script2);
                                                language = str;
                                                token4 = token2;
                                            }
                                        } catch (IllformedLocaleException e7) {
                                            e = e7;
                                            variant2 = variant3;
                                            script2 = script3;
                                            Slog.e(TAG, "readFromProto error building locale with: language-" + language2 + ";country-" + country2 + ";variant-" + variant2 + ";script-" + script2);
                                            language = str;
                                            token4 = token2;
                                        }
                                    } catch (IllformedLocaleException e8) {
                                        e = e8;
                                        country2 = country3;
                                        variant2 = variant3;
                                        script2 = script3;
                                        Slog.e(TAG, "readFromProto error building locale with: language-" + language2 + ";country-" + country2 + ";variant-" + variant2 + ";script-" + script2);
                                        language = str;
                                        token4 = token2;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    token = token2;
                                    if (list.size() > 0) {
                                        setLocales(new LocaleList((Locale[]) list.toArray(new Locale[list.size()])));
                                    }
                                    protoInputStream.end(token);
                                    throw th;
                                }
                            case 5:
                                this.screenLayout = protoInputStream.readInt(1155346202629L);
                                str = language;
                                token2 = token4;
                                break;
                            case 6:
                                this.colorMode = protoInputStream.readInt(1155346202630L);
                                str = language;
                                token2 = token4;
                                break;
                            case 7:
                                this.touchscreen = protoInputStream.readInt(ConfigurationProto.TOUCHSCREEN);
                                str = language;
                                token2 = token4;
                                break;
                            case 8:
                                this.keyboard = protoInputStream.readInt(1155346202632L);
                                str = language;
                                token2 = token4;
                                break;
                            case 9:
                                this.keyboardHidden = protoInputStream.readInt(ConfigurationProto.KEYBOARD_HIDDEN);
                                str = language;
                                token2 = token4;
                                break;
                            case 10:
                                this.hardKeyboardHidden = protoInputStream.readInt(ConfigurationProto.HARD_KEYBOARD_HIDDEN);
                                str = language;
                                token2 = token4;
                                break;
                            case 11:
                                this.navigation = protoInputStream.readInt(ConfigurationProto.NAVIGATION);
                                str = language;
                                token2 = token4;
                                break;
                            case 12:
                                this.navigationHidden = protoInputStream.readInt(ConfigurationProto.NAVIGATION_HIDDEN);
                                str = language;
                                token2 = token4;
                                break;
                            case 13:
                                this.orientation = protoInputStream.readInt(ConfigurationProto.ORIENTATION);
                                str = language;
                                token2 = token4;
                                break;
                            case 14:
                                this.uiMode = protoInputStream.readInt(ConfigurationProto.UI_MODE);
                                str = language;
                                token2 = token4;
                                break;
                            case 15:
                                this.screenWidthDp = protoInputStream.readInt(ConfigurationProto.SCREEN_WIDTH_DP);
                                str = language;
                                token2 = token4;
                                break;
                            case 16:
                                this.screenHeightDp = protoInputStream.readInt(ConfigurationProto.SCREEN_HEIGHT_DP);
                                str = language;
                                token2 = token4;
                                break;
                            case 17:
                                this.smallestScreenWidthDp = protoInputStream.readInt(ConfigurationProto.SMALLEST_SCREEN_WIDTH_DP);
                                str = language;
                                token2 = token4;
                                break;
                            case 18:
                                this.densityDpi = protoInputStream.readInt(ConfigurationProto.DENSITY_DPI);
                                str = language;
                                token2 = token4;
                                break;
                            case 19:
                                try {
                                    this.windowConfiguration.readFromProto(protoInputStream, 1146756268051L);
                                    str = language;
                                    token2 = token4;
                                    break;
                                } catch (Throwable th5) {
                                    th = th5;
                                    token = token4;
                                    if (list.size() > 0) {
                                    }
                                    protoInputStream.end(token);
                                    throw th;
                                }
                            default:
                                str = language;
                                token2 = token4;
                                break;
                        }
                        language = str;
                        token4 = token2;
                    } catch (Throwable th6) {
                        th = th6;
                        token = token4;
                        if (list.size() > 0) {
                        }
                        protoInputStream.end(token);
                        throw th;
                    }
                } else {
                    if (list.size() > 0) {
                        setLocales(new LocaleList((Locale[]) list.toArray(new Locale[list.size()])));
                    }
                    protoInputStream.end(token4);
                    return;
                }
            } catch (Throwable th7) {
                th = th7;
                token = token4;
                if (list.size() > 0) {
                }
                protoInputStream.end(token);
                throw th;
            }
        }
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
        this.seq = 0;
        this.windowConfiguration.setToDefaults();
    }

    public void unset() {
        setToDefaults();
        this.fontScale = 0.0f;
    }

    @UnsupportedAppUsage
    @Deprecated
    public void makeDefault() {
        setToDefaults();
    }

    public int updateFrom(Configuration delta) {
        int i;
        int changed = 0;
        float f = delta.fontScale;
        if (f > 0.0f && this.fontScale != f) {
            changed = 0 | 1073741824;
            this.fontScale = f;
        }
        int i2 = delta.mcc;
        if (!(i2 == 0 || this.mcc == i2)) {
            changed |= 1;
            this.mcc = i2;
        }
        int i3 = delta.mnc;
        if (!(i3 == 0 || this.mnc == i3)) {
            changed |= 2;
            this.mnc = i3;
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
        if (deltaScreenLayoutDir != 0) {
            int i4 = this.screenLayout;
            if (deltaScreenLayoutDir != (i4 & 192)) {
                this.screenLayout = (i4 & -193) | deltaScreenLayoutDir;
                changed |= 8192;
            }
        }
        if (delta.userSetLocale && (!this.userSetLocale || (changed & 4) != 0)) {
            changed |= 4;
            this.userSetLocale = true;
        }
        int i5 = delta.touchscreen;
        if (!(i5 == 0 || this.touchscreen == i5)) {
            changed |= 8;
            this.touchscreen = i5;
        }
        int i6 = delta.keyboard;
        if (!(i6 == 0 || this.keyboard == i6)) {
            changed |= 16;
            this.keyboard = i6;
        }
        int i7 = delta.keyboardHidden;
        if (!(i7 == 0 || this.keyboardHidden == i7)) {
            changed |= 32;
            this.keyboardHidden = i7;
        }
        int i8 = delta.hardKeyboardHidden;
        if (!(i8 == 0 || this.hardKeyboardHidden == i8)) {
            changed |= 32;
            this.hardKeyboardHidden = i8;
        }
        int i9 = delta.navigation;
        if (!(i9 == 0 || this.navigation == i9)) {
            changed |= 64;
            this.navigation = i9;
        }
        int i10 = delta.navigationHidden;
        if (!(i10 == 0 || this.navigationHidden == i10)) {
            changed |= 32;
            this.navigationHidden = i10;
        }
        int i11 = delta.orientation;
        if (!(i11 == 0 || this.orientation == i11)) {
            changed |= 128;
            this.orientation = i11;
        }
        int i12 = delta.screenLayout;
        if ((i12 & 15) != 0) {
            int i13 = i12 & 15;
            int i14 = this.screenLayout;
            if (i13 != (i14 & 15)) {
                changed |= 256;
                this.screenLayout = (i12 & 15) | (i14 & -16);
            }
        }
        int i15 = delta.screenLayout;
        if ((i15 & 48) != 0) {
            int i16 = i15 & 48;
            int i17 = this.screenLayout;
            if (i16 != (i17 & 48)) {
                changed |= 256;
                this.screenLayout = (i15 & 48) | (i17 & -49);
            }
        }
        int i18 = delta.screenLayout;
        if ((i18 & 768) != 0) {
            int i19 = i18 & 768;
            int i20 = this.screenLayout;
            if (i19 != (i20 & 768)) {
                changed |= 256;
                this.screenLayout = (i18 & 768) | (i20 & -769);
            }
        }
        int i21 = delta.screenLayout;
        int i22 = i21 & 268435456;
        int i23 = this.screenLayout;
        if (!(i22 == (i23 & 268435456) || i21 == 0)) {
            changed |= 256;
            this.screenLayout = (i21 & 268435456) | (-268435457 & i23);
        }
        int i24 = delta.colorMode;
        if ((i24 & 3) != 0) {
            int i25 = i24 & 3;
            int i26 = this.colorMode;
            if (i25 != (i26 & 3)) {
                changed |= 16384;
                this.colorMode = (i24 & 3) | (i26 & -4);
            }
        }
        int i27 = delta.colorMode;
        if ((i27 & 12) != 0) {
            int i28 = i27 & 12;
            int i29 = this.colorMode;
            if (i28 != (i29 & 12)) {
                changed |= 16384;
                this.colorMode = (i27 & 12) | (i29 & -13);
            }
        }
        int i30 = delta.uiMode;
        if (!(i30 == 0 || (i = this.uiMode) == i30)) {
            changed |= 512;
            if ((i30 & 15) != 0) {
                this.uiMode = (i30 & 15) | (i & -16);
            }
            int i31 = delta.uiMode;
            if ((i31 & 48) != 0) {
                this.uiMode = (i31 & 48) | (this.uiMode & -49);
            }
        }
        int i32 = delta.screenWidthDp;
        if (!(i32 == 0 || this.screenWidthDp == i32)) {
            changed |= 1024;
            this.screenWidthDp = i32;
        }
        int i33 = delta.screenHeightDp;
        if (!(i33 == 0 || this.screenHeightDp == i33)) {
            changed |= 1024;
            this.screenHeightDp = i33;
        }
        int i34 = delta.smallestScreenWidthDp;
        if (!(i34 == 0 || this.smallestScreenWidthDp == i34)) {
            changed |= 2048;
            this.smallestScreenWidthDp = i34;
        }
        int i35 = delta.densityDpi;
        if (!(i35 == 0 || this.densityDpi == i35)) {
            changed |= 4096;
            this.densityDpi = i35;
        }
        int i36 = delta.compatScreenWidthDp;
        if (i36 != 0) {
            this.compatScreenWidthDp = i36;
        }
        int i37 = delta.compatScreenHeightDp;
        if (i37 != 0) {
            this.compatScreenHeightDp = i37;
        }
        int i38 = delta.compatSmallestScreenWidthDp;
        if (i38 != 0) {
            this.compatSmallestScreenWidthDp = i38;
        }
        int i39 = delta.assetsSeq;
        if (!(i39 == 0 || i39 == this.assetsSeq)) {
            changed |= Integer.MIN_VALUE;
            this.assetsSeq = i39;
        }
        int changed2 = changed | this.extraConfig.updateFrom(delta.extraConfig);
        int i40 = delta.seq;
        if (i40 != 0) {
            this.seq = i40;
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
        return (configChanges & (((Integer.MIN_VALUE | interestingChanges) | 1073741824) | 32768)) != 0;
    }

    public boolean isOtherSeqNewer(Configuration other) {
        int i;
        if (other == null) {
            return false;
        }
        int i2 = other.seq;
        if (i2 == 0 || (i = this.seq) == 0) {
            return true;
        }
        int diff = i2 - i;
        if (diff <= 65536 && diff > 0) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
        this.seq = source.readInt();
    }

    private Configuration(Parcel source) {
        this.windowConfiguration = new WindowConfiguration();
        this.extraConfig = HwFrameworkFactory.getHwConfiguration();
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
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if (compareTo(that) == 0) {
            return true;
        }
        return false;
    }

    public boolean equals(Object that) {
        try {
            return equals((Configuration) that);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return (((((((((((((((((((((((((((((((((((((((17 * 31) + Float.floatToIntBits(this.fontScale)) * 31) + this.mcc) * 31) + this.mnc) * 31) + this.mLocaleList.hashCode()) * 31) + this.touchscreen) * 31) + this.keyboard) * 31) + this.keyboardHidden) * 31) + this.hardKeyboardHidden) * 31) + this.navigation) * 31) + this.navigationHidden) * 31) + this.orientation) * 31) + this.screenLayout) * 31) + this.colorMode) * 31) + this.uiMode) * 31) + this.screenWidthDp) * 31) + this.screenHeightDp) * 31) + this.smallestScreenWidthDp) * 31) + this.densityDpi) * 31) + this.assetsSeq) * 31) + this.extraConfig.hashCode();
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
            localeList = new LocaleList(loc);
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
        this.screenLayout = (this.screenLayout & -193) | ((TextUtils.getLayoutDirectionFromLocale(loc) + 1) << 6);
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
                    sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
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
                        sb.append(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
                        sb.append(loc.getScript());
                    }
                    if (c != 0) {
                        sb.append(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
                        sb.append(loc.getCountry());
                    }
                    if (v != 0) {
                        sb.append(HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
                        sb.append(loc.getVariant());
                    }
                }
            }
        }
        return sb.toString();
    }

    @UnsupportedAppUsage
    public static String resourceQualifierString(Configuration config) {
        return resourceQualifierString(config, null);
    }

    public static String resourceQualifierString(Configuration config, DisplayMetrics metrics) {
        int height;
        int width;
        ArrayList<String> parts = new ArrayList<>();
        if (config.mcc != 0) {
            parts.add("mcc" + config.mcc);
            if (config.mnc != 0) {
                parts.add("mnc" + config.mnc);
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
        if (config.smallestScreenWidthDp != 0) {
            parts.add(XML_ATTR_SMALLEST_WIDTH + config.smallestScreenWidthDp + "dp");
        }
        if (config.screenWidthDp != 0) {
            parts.add("w" + config.screenWidthDp + "dp");
        }
        if (config.screenHeightDp != 0) {
            parts.add("h" + config.screenHeightDp + "dp");
        }
        int i2 = config.screenLayout & 15;
        if (i2 == 1) {
            parts.add("small");
        } else if (i2 == 2) {
            parts.add("normal");
        } else if (i2 == 3) {
            parts.add(Slice.HINT_LARGE);
        } else if (i2 == 4) {
            parts.add("xlarge");
        }
        int i3 = config.screenLayout & 48;
        if (i3 == 16) {
            parts.add("notlong");
        } else if (i3 == 32) {
            parts.add("long");
        }
        int i4 = config.screenLayout & 768;
        if (i4 == 256) {
            parts.add("notround");
        } else if (i4 == 512) {
            parts.add("round");
        }
        int i5 = config.colorMode & 3;
        if (i5 == 1) {
            parts.add("nowidecg");
        } else if (i5 == 2) {
            parts.add("widecg");
        }
        int i6 = config.colorMode & 12;
        if (i6 == 4) {
            parts.add("lowdr");
        } else if (i6 == 8) {
            parts.add("highdr");
        }
        int i7 = config.orientation;
        if (i7 == 1) {
            parts.add("port");
        } else if (i7 == 2) {
            parts.add("land");
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
        int i8 = config.uiMode & 48;
        if (i8 == 16) {
            parts.add("notnight");
        } else if (i8 == 32) {
            parts.add(Camera.Parameters.SCENE_MODE_NIGHT);
        }
        int i9 = config.densityDpi;
        if (i9 != 0) {
            if (i9 == 120) {
                parts.add("ldpi");
            } else if (i9 == 160) {
                parts.add("mdpi");
            } else if (i9 == 213) {
                parts.add("tvdpi");
            } else if (i9 == 240) {
                parts.add("hdpi");
            } else if (i9 == 320) {
                parts.add("xhdpi");
            } else if (i9 == 480) {
                parts.add("xxhdpi");
            } else if (i9 != 640) {
                switch (i9) {
                    case DENSITY_DPI_ANY /*{ENCODED_INT: 65534}*/:
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
        int i10 = config.touchscreen;
        if (i10 == 1) {
            parts.add("notouch");
        } else if (i10 == 3) {
            parts.add("finger");
        }
        int i11 = config.keyboardHidden;
        if (i11 == 1) {
            parts.add("keysexposed");
        } else if (i11 == 2) {
            parts.add("keyshidden");
        } else if (i11 == 3) {
            parts.add("keyssoft");
        }
        int i12 = config.keyboard;
        if (i12 == 1) {
            parts.add("nokeys");
        } else if (i12 == 2) {
            parts.add("qwerty");
        } else if (i12 == 3) {
            parts.add("12key");
        }
        int i13 = config.navigationHidden;
        if (i13 == 1) {
            parts.add("navexposed");
        } else if (i13 == 2) {
            parts.add("navhidden");
        }
        int i14 = config.navigation;
        if (i14 == 1) {
            parts.add("nonav");
        } else if (i14 == 2) {
            parts.add("dpad");
        } else if (i14 == 3) {
            parts.add("trackball");
        } else if (i14 == 4) {
            parts.add("wheel");
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
        parts.add(Telephony.BaseMmsColumns.MMS_VERSION + Build.VERSION.RESOURCES_SDK_INT);
        return TextUtils.join(NativeLibraryHelper.CLEAR_ABI_OVERRIDE, parts);
    }

    @UnsupportedAppUsage
    public static Configuration generateDelta(Configuration base, Configuration change) {
        Configuration delta = new Configuration();
        float f = base.fontScale;
        float f2 = change.fontScale;
        if (f != f2) {
            delta.fontScale = f2;
        }
        int i = base.mcc;
        int i2 = change.mcc;
        if (i != i2) {
            delta.mcc = i2;
        }
        int i3 = base.mnc;
        int i4 = change.mnc;
        if (i3 != i4) {
            delta.mnc = i4;
        }
        base.fixUpLocaleList();
        change.fixUpLocaleList();
        if (!base.mLocaleList.equals(change.mLocaleList)) {
            delta.mLocaleList = change.mLocaleList;
            delta.locale = change.locale;
        }
        int i5 = base.touchscreen;
        int i6 = change.touchscreen;
        if (i5 != i6) {
            delta.touchscreen = i6;
        }
        int i7 = base.keyboard;
        int i8 = change.keyboard;
        if (i7 != i8) {
            delta.keyboard = i8;
        }
        int i9 = base.keyboardHidden;
        int i10 = change.keyboardHidden;
        if (i9 != i10) {
            delta.keyboardHidden = i10;
        }
        int i11 = base.navigation;
        int i12 = change.navigation;
        if (i11 != i12) {
            delta.navigation = i12;
        }
        int i13 = base.navigationHidden;
        int i14 = change.navigationHidden;
        if (i13 != i14) {
            delta.navigationHidden = i14;
        }
        int i15 = base.orientation;
        int i16 = change.orientation;
        if (i15 != i16) {
            delta.orientation = i16;
        }
        int i17 = base.screenLayout & 15;
        int i18 = change.screenLayout;
        if (i17 != (i18 & 15)) {
            delta.screenLayout |= i18 & 15;
        }
        int i19 = base.screenLayout & 192;
        int i20 = change.screenLayout;
        if (i19 != (i20 & 192)) {
            delta.screenLayout |= i20 & 192;
        }
        int i21 = base.screenLayout & 48;
        int i22 = change.screenLayout;
        if (i21 != (i22 & 48)) {
            delta.screenLayout |= i22 & 48;
        }
        int i23 = base.screenLayout & 768;
        int i24 = change.screenLayout;
        if (i23 != (i24 & 768)) {
            delta.screenLayout |= i24 & 768;
        }
        int i25 = base.colorMode & 3;
        int i26 = change.colorMode;
        if (i25 != (i26 & 3)) {
            delta.colorMode |= i26 & 3;
        }
        int i27 = base.colorMode & 12;
        int i28 = change.colorMode;
        if (i27 != (i28 & 12)) {
            delta.colorMode |= i28 & 12;
        }
        int i29 = base.uiMode & 15;
        int i30 = change.uiMode;
        if (i29 != (i30 & 15)) {
            delta.uiMode |= i30 & 15;
        }
        int i31 = base.uiMode & 48;
        int i32 = change.uiMode;
        if (i31 != (i32 & 48)) {
            delta.uiMode |= i32 & 48;
        }
        int i33 = base.screenWidthDp;
        int i34 = change.screenWidthDp;
        if (i33 != i34) {
            delta.screenWidthDp = i34;
        }
        int i35 = base.screenHeightDp;
        int i36 = change.screenHeightDp;
        if (i35 != i36) {
            delta.screenHeightDp = i36;
        }
        int i37 = base.smallestScreenWidthDp;
        int i38 = change.smallestScreenWidthDp;
        if (i37 != i38) {
            delta.smallestScreenWidthDp = i38;
        }
        int i39 = base.densityDpi;
        int i40 = change.densityDpi;
        if (i39 != i40) {
            delta.densityDpi = i40;
        }
        int i41 = base.assetsSeq;
        int i42 = change.assetsSeq;
        if (i41 != i42) {
            delta.assetsSeq = i42;
        }
        if (!base.windowConfiguration.equals(change.windowConfiguration)) {
            delta.windowConfiguration.setTo(change.windowConfiguration);
        }
        return delta;
    }

    public static void readXmlAttrs(XmlPullParser parser, Configuration configOut) throws XmlPullParserException, IOException {
        configOut.fontScale = Float.intBitsToFloat(XmlUtils.readIntAttribute(parser, XML_ATTR_FONT_SCALE, 0));
        configOut.mcc = XmlUtils.readIntAttribute(parser, "mcc", 0);
        configOut.mnc = XmlUtils.readIntAttribute(parser, "mnc", 0);
        configOut.mLocaleList = LocaleList.forLanguageTags(XmlUtils.readStringAttribute(parser, XML_ATTR_LOCALES));
        configOut.locale = configOut.mLocaleList.get(0);
        configOut.touchscreen = XmlUtils.readIntAttribute(parser, XML_ATTR_TOUCHSCREEN, 0);
        configOut.keyboard = XmlUtils.readIntAttribute(parser, "key", 0);
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
        int i = config.mcc;
        if (i != 0) {
            XmlUtils.writeIntAttribute(xml, "mcc", i);
        }
        int i2 = config.mnc;
        if (i2 != 0) {
            XmlUtils.writeIntAttribute(xml, "mnc", i2);
        }
        config.fixUpLocaleList();
        if (!config.mLocaleList.isEmpty()) {
            XmlUtils.writeStringAttribute(xml, XML_ATTR_LOCALES, config.mLocaleList.toLanguageTags());
        }
        int i3 = config.touchscreen;
        if (i3 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_TOUCHSCREEN, i3);
        }
        int i4 = config.keyboard;
        if (i4 != 0) {
            XmlUtils.writeIntAttribute(xml, "key", i4);
        }
        int i5 = config.keyboardHidden;
        if (i5 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_KEYBOARD_HIDDEN, i5);
        }
        int i6 = config.hardKeyboardHidden;
        if (i6 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_HARD_KEYBOARD_HIDDEN, i6);
        }
        int i7 = config.navigation;
        if (i7 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_NAVIGATION, i7);
        }
        int i8 = config.navigationHidden;
        if (i8 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_NAVIGATION_HIDDEN, i8);
        }
        int i9 = config.orientation;
        if (i9 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_ORIENTATION, i9);
        }
        int i10 = config.screenLayout;
        if (i10 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SCREEN_LAYOUT, i10);
        }
        int i11 = config.colorMode;
        if (i11 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_COLOR_MODE, i11);
        }
        int i12 = config.uiMode;
        if (i12 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_UI_MODE, i12);
        }
        int i13 = config.screenWidthDp;
        if (i13 != 0) {
            XmlUtils.writeIntAttribute(xml, "width", i13);
        }
        int i14 = config.screenHeightDp;
        if (i14 != 0) {
            XmlUtils.writeIntAttribute(xml, "height", i14);
        }
        int i15 = config.smallestScreenWidthDp;
        if (i15 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SMALLEST_WIDTH, i15);
        }
        int i16 = config.densityDpi;
        if (i16 != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_DENSITY, i16);
        }
    }
}
