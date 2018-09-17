package android.content.res;

import android.bluetooth.BluetoothAssignedNumbers;
import android.hardware.Camera.Parameters;
import android.hardware.usb.UsbManager;
import android.net.NetworkPolicyManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.tech.MifareClassic;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.text.TextUtils;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class Configuration implements Parcelable, Comparable<Configuration> {
    public static final Creator<Configuration> CREATOR = null;
    public static final int DENSITY_DPI_ANY = 65534;
    public static final int DENSITY_DPI_NONE = 65535;
    public static final int DENSITY_DPI_UNDEFINED = 0;
    public static final Configuration EMPTY = null;
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
    public static final int UI_MODE_TYPE_WATCH = 6;
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
    private static final String XML_ATTR_SCREEN_HEIGHT = "height";
    private static final String XML_ATTR_SCREEN_LAYOUT = "scrLay";
    private static final String XML_ATTR_SCREEN_WIDTH = "width";
    private static final String XML_ATTR_SMALLEST_WIDTH = "sw";
    private static final String XML_ATTR_TOUCHSCREEN = "touch";
    private static final String XML_ATTR_UI_MODE = "ui";
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
    public int orientation;
    public int screenHeightDp;
    public int screenLayout;
    public int screenWidthDp;
    public int seq;
    public int smallestScreenWidthDp;
    public int touchscreen;
    public int uiMode;
    public boolean userSetLocale;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.Configuration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.Configuration.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.Configuration.<clinit>():void");
    }

    public static int resetScreenLayout(int curLayout) {
        return (-268435520 & curLayout) | 36;
    }

    public static int reduceScreenLayout(int curLayout, int longSizeDp, int shortSizeDp) {
        int screenLayoutSize;
        boolean screenLayoutLong;
        boolean screenLayoutCompatNeeded;
        if (longSizeDp < 470) {
            screenLayoutSize = UI_MODE_TYPE_NORMAL;
            screenLayoutLong = false;
            screenLayoutCompatNeeded = false;
        } else {
            if (longSizeDp >= 960 && shortSizeDp >= 720) {
                screenLayoutSize = UI_MODE_TYPE_TELEVISION;
            } else if (longSizeDp < 640 || shortSizeDp < 480) {
                screenLayoutSize = UI_MODE_TYPE_DESK;
            } else {
                screenLayoutSize = UI_MODE_TYPE_CAR;
            }
            if (shortSizeDp > 321 || longSizeDp > 570) {
                screenLayoutCompatNeeded = true;
            } else {
                screenLayoutCompatNeeded = false;
            }
            if ((longSizeDp * UI_MODE_TYPE_CAR) / UI_MODE_TYPE_APPLIANCE >= shortSizeDp - 1) {
                screenLayoutLong = true;
            } else {
                screenLayoutLong = false;
            }
        }
        if (!screenLayoutLong) {
            curLayout = (curLayout & -49) | UI_MODE_NIGHT_NO;
        }
        if (screenLayoutCompatNeeded) {
            curLayout |= SCREENLAYOUT_COMPAT_NEEDED;
        }
        if (screenLayoutSize < (curLayout & UI_MODE_TYPE_MASK)) {
            return (curLayout & -16) | screenLayoutSize;
        }
        return curLayout;
    }

    public static String configurationDiffToString(int diff) {
        ArrayList<String> list = new ArrayList();
        if ((diff & UI_MODE_TYPE_NORMAL) != 0) {
            list.add("CONFIG_MCC");
        }
        if ((diff & UI_MODE_TYPE_DESK) != 0) {
            list.add("CONFIG_MNC");
        }
        if ((diff & UI_MODE_TYPE_TELEVISION) != 0) {
            list.add("CONFIG_LOCALE");
        }
        if ((diff & SCREENLAYOUT_ROUND_SHIFT) != 0) {
            list.add("CONFIG_TOUCHSCREEN");
        }
        if ((diff & UI_MODE_NIGHT_NO) != 0) {
            list.add("CONFIG_KEYBOARD");
        }
        if ((diff & UI_MODE_NIGHT_YES) != 0) {
            list.add("CONFIG_KEYBOARD_HIDDEN");
        }
        if ((diff & SCREENLAYOUT_LAYOUTDIR_LTR) != 0) {
            list.add("CONFIG_NAVIGATION");
        }
        if ((diff & SCREENLAYOUT_LAYOUTDIR_RTL) != 0) {
            list.add("CONFIG_ORIENTATION");
        }
        if ((diff & SCREENLAYOUT_ROUND_NO) != 0) {
            list.add("CONFIG_SCREEN_LAYOUT");
        }
        if ((diff & SCREENLAYOUT_ROUND_YES) != 0) {
            list.add("CONFIG_UI_MODE");
        }
        if ((diff & NATIVE_CONFIG_VERSION) != 0) {
            list.add("CONFIG_SCREEN_SIZE");
        }
        if ((diff & NATIVE_CONFIG_SCREEN_LAYOUT) != 0) {
            list.add("CONFIG_SMALLEST_SCREEN_SIZE");
        }
        if ((diff & NATIVE_CONFIG_SMALLEST_SCREEN_SIZE) != 0) {
            list.add("CONFIG_LAYOUT_DIRECTION");
        }
        if ((KeymasterDefs.KM_UINT_REP & diff) != 0) {
            list.add("CONFIG_FONT_SCALE");
        }
        StringBuilder builder = new StringBuilder("{");
        int n = list.size();
        for (int i = UI_MODE_TYPE_UNDEFINED; i < n; i += UI_MODE_TYPE_NORMAL) {
            builder.append((String) list.get(i));
            if (i != n - 1) {
                builder.append(", ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public boolean isLayoutSizeAtLeast(int size) {
        boolean z = false;
        int cur = this.screenLayout & UI_MODE_TYPE_MASK;
        if (cur == 0) {
            return false;
        }
        if (cur >= size) {
            z = true;
        }
        return z;
    }

    public Configuration() {
        this.extraConfig = HwConfiguration.initHwConfiguration();
        setToDefaults();
    }

    public Configuration(Configuration o) {
        this.extraConfig = HwConfiguration.initHwConfiguration();
        setTo(o);
    }

    private void fixUpLocaleList() {
        if ((this.locale == null && !this.mLocaleList.isEmpty()) || (this.locale != null && !this.locale.equals(this.mLocaleList.get(UI_MODE_TYPE_UNDEFINED)))) {
            LocaleList emptyLocaleList;
            if (this.locale == null) {
                emptyLocaleList = LocaleList.getEmptyLocaleList();
            } else {
                Locale[] localeArr = new Locale[UI_MODE_TYPE_NORMAL];
                localeArr[UI_MODE_TYPE_UNDEFINED] = this.locale;
                emptyLocaleList = new LocaleList(localeArr);
            }
            this.mLocaleList = emptyLocaleList;
        }
    }

    public void setTo(Configuration o) {
        Locale locale = null;
        this.fontScale = o.fontScale;
        this.mcc = o.mcc;
        this.mnc = o.mnc;
        if (o.locale != null) {
            locale = (Locale) o.locale.clone();
        }
        this.locale = locale;
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
        this.uiMode = o.uiMode;
        this.screenWidthDp = o.screenWidthDp;
        this.screenHeightDp = o.screenHeightDp;
        this.smallestScreenWidthDp = o.smallestScreenWidthDp;
        this.densityDpi = o.densityDpi;
        this.compatScreenWidthDp = o.compatScreenWidthDp;
        this.compatScreenHeightDp = o.compatScreenHeightDp;
        this.compatSmallestScreenWidthDp = o.compatSmallestScreenWidthDp;
        this.extraConfig.setTo(o.extraConfig);
        this.seq = o.seq;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(SCREENLAYOUT_LAYOUTDIR_RTL);
        sb.append("{");
        sb.append(this.fontScale);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
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
        if (this.mLocaleList.isEmpty()) {
            sb.append(" ?localeList");
        } else {
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.mLocaleList);
        }
        int layoutDir = this.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK;
        switch (layoutDir) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?layoutDir");
                break;
            case SCREENLAYOUT_LAYOUTDIR_LTR /*64*/:
                sb.append(" ldltr");
                break;
            case SCREENLAYOUT_LAYOUTDIR_RTL /*128*/:
                sb.append(" ldrtl");
                break;
            default:
                sb.append(" layoutDir=");
                sb.append(layoutDir >> UI_MODE_TYPE_WATCH);
                break;
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
        switch (this.screenLayout & UI_MODE_TYPE_MASK) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?lsize");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append(" smll");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append(" nrml");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                sb.append(" lrg");
                break;
            case UI_MODE_TYPE_TELEVISION /*4*/:
                sb.append(" xlrg");
                break;
            default:
                sb.append(" layoutSize=");
                sb.append(this.screenLayout & UI_MODE_TYPE_MASK);
                break;
        }
        switch (this.screenLayout & UI_MODE_NIGHT_MASK) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?long");
                break;
            case UI_MODE_NIGHT_NO /*16*/:
                break;
            case UI_MODE_NIGHT_YES /*32*/:
                sb.append(" long");
                break;
            default:
                sb.append(" layoutLong=");
                sb.append(this.screenLayout & UI_MODE_NIGHT_MASK);
                break;
        }
        switch (this.orientation) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?orien");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append(" port");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append(" land");
                break;
            default:
                sb.append(" orien=");
                sb.append(this.orientation);
                break;
        }
        switch (this.uiMode & UI_MODE_TYPE_MASK) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?uimode");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append(" desk");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                sb.append(" car");
                break;
            case UI_MODE_TYPE_TELEVISION /*4*/:
                sb.append(" television");
                break;
            case UI_MODE_TYPE_APPLIANCE /*5*/:
                sb.append(" appliance");
                break;
            case UI_MODE_TYPE_WATCH /*6*/:
                sb.append(" watch");
                break;
            default:
                sb.append(" uimode=");
                sb.append(this.uiMode & UI_MODE_TYPE_MASK);
                break;
        }
        switch (this.uiMode & UI_MODE_NIGHT_MASK) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?night");
                break;
            case UI_MODE_NIGHT_NO /*16*/:
                break;
            case UI_MODE_NIGHT_YES /*32*/:
                sb.append(" night");
                break;
            default:
                sb.append(" night=");
                sb.append(this.uiMode & UI_MODE_NIGHT_MASK);
                break;
        }
        switch (this.touchscreen) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?touch");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append(" -touch");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append(" stylus");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                sb.append(" finger");
                break;
            default:
                sb.append(" touch=");
                sb.append(this.touchscreen);
                break;
        }
        switch (this.keyboard) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?keyb");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append(" -keyb");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append(" qwerty");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                sb.append(" 12key");
                break;
            default:
                sb.append(" keys=");
                sb.append(this.keyboard);
                break;
        }
        switch (this.keyboardHidden) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append("/?");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append("/v");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append("/h");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                sb.append("/s");
                break;
            default:
                sb.append("/");
                sb.append(this.keyboardHidden);
                break;
        }
        switch (this.hardKeyboardHidden) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append("/?");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append("/v");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append("/h");
                break;
            default:
                sb.append("/");
                sb.append(this.hardKeyboardHidden);
                break;
        }
        switch (this.navigation) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append(" ?nav");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append(" -nav");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append(" dpad");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                sb.append(" tball");
                break;
            case UI_MODE_TYPE_TELEVISION /*4*/:
                sb.append(" wheel");
                break;
            default:
                sb.append(" nav=");
                sb.append(this.navigation);
                break;
        }
        switch (this.navigationHidden) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                sb.append("/?");
                break;
            case UI_MODE_TYPE_NORMAL /*1*/:
                sb.append("/v");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                sb.append("/h");
                break;
            default:
                sb.append("/");
                sb.append(this.navigationHidden);
                break;
        }
        sb.append(this.extraConfig.toString());
        if (this.seq != 0) {
            sb.append(" s.");
            sb.append(this.seq);
        }
        sb.append('}');
        return sb.toString();
    }

    public void setToDefaults() {
        this.fontScale = Engine.DEFAULT_VOLUME;
        this.mnc = UI_MODE_TYPE_UNDEFINED;
        this.mcc = UI_MODE_TYPE_UNDEFINED;
        this.mLocaleList = LocaleList.getEmptyLocaleList();
        this.locale = null;
        this.userSetLocale = false;
        this.touchscreen = UI_MODE_TYPE_UNDEFINED;
        this.keyboard = UI_MODE_TYPE_UNDEFINED;
        this.keyboardHidden = UI_MODE_TYPE_UNDEFINED;
        this.hardKeyboardHidden = UI_MODE_TYPE_UNDEFINED;
        this.navigation = UI_MODE_TYPE_UNDEFINED;
        this.navigationHidden = UI_MODE_TYPE_UNDEFINED;
        this.orientation = UI_MODE_TYPE_UNDEFINED;
        this.screenLayout = UI_MODE_TYPE_UNDEFINED;
        this.uiMode = UI_MODE_TYPE_UNDEFINED;
        this.compatScreenWidthDp = UI_MODE_TYPE_UNDEFINED;
        this.screenWidthDp = UI_MODE_TYPE_UNDEFINED;
        this.compatScreenHeightDp = UI_MODE_TYPE_UNDEFINED;
        this.screenHeightDp = UI_MODE_TYPE_UNDEFINED;
        this.compatSmallestScreenWidthDp = UI_MODE_TYPE_UNDEFINED;
        this.smallestScreenWidthDp = UI_MODE_TYPE_UNDEFINED;
        this.densityDpi = UI_MODE_TYPE_UNDEFINED;
        this.extraConfig.setToDefaults();
        this.seq = UI_MODE_TYPE_UNDEFINED;
    }

    @Deprecated
    public void makeDefault() {
        setToDefaults();
    }

    public int updateFrom(Configuration delta) {
        int changed = UI_MODE_TYPE_UNDEFINED;
        if (delta.fontScale > 0.0f && this.fontScale != delta.fontScale) {
            changed = KeymasterDefs.KM_UINT_REP;
            this.fontScale = delta.fontScale;
        }
        if (!(delta.mcc == 0 || this.mcc == delta.mcc)) {
            changed |= UI_MODE_TYPE_NORMAL;
            this.mcc = delta.mcc;
        }
        if (!(delta.mnc == 0 || this.mnc == delta.mnc)) {
            changed |= UI_MODE_TYPE_DESK;
            this.mnc = delta.mnc;
        }
        fixUpLocaleList();
        delta.fixUpLocaleList();
        if (!(delta.mLocaleList.isEmpty() || this.mLocaleList.equals(delta.mLocaleList))) {
            changed |= UI_MODE_TYPE_TELEVISION;
            this.mLocaleList = delta.mLocaleList;
            if (!delta.locale.equals(this.locale)) {
                this.locale = (Locale) delta.locale.clone();
                changed |= NATIVE_CONFIG_SMALLEST_SCREEN_SIZE;
                setLayoutDirection(this.locale);
            }
        }
        int deltaScreenLayoutDir = delta.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK;
        if (!(deltaScreenLayoutDir == 0 || deltaScreenLayoutDir == (this.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK))) {
            this.screenLayout = (this.screenLayout & -193) | deltaScreenLayoutDir;
            changed |= NATIVE_CONFIG_SMALLEST_SCREEN_SIZE;
        }
        if (delta.userSetLocale && !(this.userSetLocale && (changed & UI_MODE_TYPE_TELEVISION) == 0)) {
            changed |= UI_MODE_TYPE_TELEVISION;
            this.userSetLocale = true;
        }
        if (!(delta.touchscreen == 0 || this.touchscreen == delta.touchscreen)) {
            changed |= SCREENLAYOUT_ROUND_SHIFT;
            this.touchscreen = delta.touchscreen;
        }
        if (!(delta.keyboard == 0 || this.keyboard == delta.keyboard)) {
            changed |= UI_MODE_NIGHT_NO;
            this.keyboard = delta.keyboard;
        }
        if (!(delta.keyboardHidden == 0 || this.keyboardHidden == delta.keyboardHidden)) {
            changed |= UI_MODE_NIGHT_YES;
            this.keyboardHidden = delta.keyboardHidden;
        }
        if (!(delta.hardKeyboardHidden == 0 || this.hardKeyboardHidden == delta.hardKeyboardHidden)) {
            changed |= UI_MODE_NIGHT_YES;
            this.hardKeyboardHidden = delta.hardKeyboardHidden;
        }
        if (!(delta.navigation == 0 || this.navigation == delta.navigation)) {
            changed |= SCREENLAYOUT_LAYOUTDIR_LTR;
            this.navigation = delta.navigation;
        }
        if (!(delta.navigationHidden == 0 || this.navigationHidden == delta.navigationHidden)) {
            changed |= UI_MODE_NIGHT_YES;
            this.navigationHidden = delta.navigationHidden;
        }
        if (!(delta.orientation == 0 || this.orientation == delta.orientation)) {
            changed |= SCREENLAYOUT_LAYOUTDIR_RTL;
            this.orientation = delta.orientation;
        }
        if (!(getScreenLayoutNoDirection(delta.screenLayout) == 0 || getScreenLayoutNoDirection(this.screenLayout) == getScreenLayoutNoDirection(delta.screenLayout))) {
            changed |= SCREENLAYOUT_ROUND_NO;
            if ((delta.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) == 0) {
                this.screenLayout = (this.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) | delta.screenLayout;
            } else {
                this.screenLayout = delta.screenLayout;
            }
        }
        if (!(delta.uiMode == 0 || this.uiMode == delta.uiMode)) {
            changed |= SCREENLAYOUT_ROUND_YES;
            if ((delta.uiMode & UI_MODE_TYPE_MASK) != 0) {
                this.uiMode = (this.uiMode & -16) | (delta.uiMode & UI_MODE_TYPE_MASK);
            }
            if ((delta.uiMode & UI_MODE_NIGHT_MASK) != 0) {
                this.uiMode = (this.uiMode & -49) | (delta.uiMode & UI_MODE_NIGHT_MASK);
            }
        }
        if (!(delta.screenWidthDp == 0 || this.screenWidthDp == delta.screenWidthDp)) {
            changed |= NATIVE_CONFIG_VERSION;
            this.screenWidthDp = delta.screenWidthDp;
        }
        if (!(delta.screenHeightDp == 0 || this.screenHeightDp == delta.screenHeightDp)) {
            changed |= NATIVE_CONFIG_VERSION;
            this.screenHeightDp = delta.screenHeightDp;
        }
        if (!(delta.smallestScreenWidthDp == 0 || this.smallestScreenWidthDp == delta.smallestScreenWidthDp)) {
            changed |= NATIVE_CONFIG_SCREEN_LAYOUT;
            this.smallestScreenWidthDp = delta.smallestScreenWidthDp;
        }
        if (!(delta.densityDpi == 0 || this.densityDpi == delta.densityDpi)) {
            changed |= NATIVE_CONFIG_UI_MODE;
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
        changed |= this.extraConfig.updateFrom(delta.extraConfig);
        if (delta.seq != 0) {
            this.seq = delta.seq;
        }
        return changed;
    }

    public int diff(Configuration delta) {
        int changed = UI_MODE_TYPE_UNDEFINED;
        if (delta.fontScale > 0.0f && this.fontScale != delta.fontScale) {
            changed = KeymasterDefs.KM_UINT_REP;
        }
        if (!(delta.mcc == 0 || this.mcc == delta.mcc)) {
            changed |= UI_MODE_TYPE_NORMAL;
        }
        if (!(delta.mnc == 0 || this.mnc == delta.mnc)) {
            changed |= UI_MODE_TYPE_DESK;
        }
        changed |= this.extraConfig.diff(delta.extraConfig);
        fixUpLocaleList();
        delta.fixUpLocaleList();
        if (!(delta.mLocaleList.isEmpty() || this.mLocaleList.equals(delta.mLocaleList))) {
            changed = (changed | UI_MODE_TYPE_TELEVISION) | NATIVE_CONFIG_SMALLEST_SCREEN_SIZE;
        }
        int deltaScreenLayoutDir = delta.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK;
        if (!(deltaScreenLayoutDir == 0 || deltaScreenLayoutDir == (this.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK))) {
            changed |= NATIVE_CONFIG_SMALLEST_SCREEN_SIZE;
        }
        if (!(delta.touchscreen == 0 || this.touchscreen == delta.touchscreen)) {
            changed |= SCREENLAYOUT_ROUND_SHIFT;
        }
        if (!(delta.keyboard == 0 || this.keyboard == delta.keyboard)) {
            changed |= UI_MODE_NIGHT_NO;
        }
        if (!(delta.keyboardHidden == 0 || this.keyboardHidden == delta.keyboardHidden)) {
            changed |= UI_MODE_NIGHT_YES;
        }
        if (!(delta.hardKeyboardHidden == 0 || this.hardKeyboardHidden == delta.hardKeyboardHidden)) {
            changed |= UI_MODE_NIGHT_YES;
        }
        if (!(delta.navigation == 0 || this.navigation == delta.navigation)) {
            changed |= SCREENLAYOUT_LAYOUTDIR_LTR;
        }
        if (!(delta.navigationHidden == 0 || this.navigationHidden == delta.navigationHidden)) {
            changed |= UI_MODE_NIGHT_YES;
        }
        if (!(delta.orientation == 0 || this.orientation == delta.orientation)) {
            changed |= SCREENLAYOUT_LAYOUTDIR_RTL;
        }
        if (!(getScreenLayoutNoDirection(delta.screenLayout) == 0 || getScreenLayoutNoDirection(this.screenLayout) == getScreenLayoutNoDirection(delta.screenLayout))) {
            changed |= SCREENLAYOUT_ROUND_NO;
        }
        if (!(delta.uiMode == 0 || this.uiMode == delta.uiMode)) {
            changed |= SCREENLAYOUT_ROUND_YES;
        }
        if (!(delta.screenWidthDp == 0 || this.screenWidthDp == delta.screenWidthDp)) {
            changed |= NATIVE_CONFIG_VERSION;
        }
        if (!(delta.screenHeightDp == 0 || this.screenHeightDp == delta.screenHeightDp)) {
            changed |= NATIVE_CONFIG_VERSION;
        }
        if (!(delta.smallestScreenWidthDp == 0 || this.smallestScreenWidthDp == delta.smallestScreenWidthDp)) {
            changed |= NATIVE_CONFIG_SCREEN_LAYOUT;
        }
        if (delta.densityDpi == 0 || this.densityDpi == delta.densityDpi) {
            return changed;
        }
        return changed | NATIVE_CONFIG_UI_MODE;
    }

    public static boolean needNewResources(int configChanges, int interestingChanges) {
        if (((KeymasterDefs.KM_UINT_REP | interestingChanges) & configChanges) == 0 && (Document.FLAG_ARCHIVE & configChanges) == 0) {
            return ((interestingChanges | SCREENLAYOUT_ROUND_NO) & configChanges) != 0;
        } else {
            return true;
        }
    }

    public boolean isOtherSeqNewer(Configuration other) {
        boolean z = true;
        if (other == null) {
            return false;
        }
        if (other.seq == 0 || this.seq == 0) {
            return true;
        }
        int diff = other.seq - this.seq;
        if (diff > Root.FLAG_EMPTY) {
            return false;
        }
        if (diff <= 0) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return UI_MODE_TYPE_UNDEFINED;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.fontScale);
        dest.writeInt(this.mcc);
        dest.writeInt(this.mnc);
        fixUpLocaleList();
        int localeListSize = this.mLocaleList.size();
        dest.writeInt(localeListSize);
        for (int i = UI_MODE_TYPE_UNDEFINED; i < localeListSize; i += UI_MODE_TYPE_NORMAL) {
            dest.writeString(this.mLocaleList.get(i).toLanguageTag());
        }
        if (this.userSetLocale) {
            dest.writeInt(UI_MODE_TYPE_NORMAL);
        } else {
            dest.writeInt(UI_MODE_TYPE_UNDEFINED);
        }
        dest.writeInt(this.touchscreen);
        dest.writeInt(this.keyboard);
        dest.writeInt(this.keyboardHidden);
        dest.writeInt(this.hardKeyboardHidden);
        dest.writeInt(this.navigation);
        dest.writeInt(this.navigationHidden);
        dest.writeInt(this.orientation);
        dest.writeInt(this.screenLayout);
        dest.writeInt(this.uiMode);
        dest.writeInt(this.screenWidthDp);
        dest.writeInt(this.screenHeightDp);
        dest.writeInt(this.smallestScreenWidthDp);
        dest.writeInt(this.densityDpi);
        dest.writeInt(this.compatScreenWidthDp);
        dest.writeInt(this.compatScreenHeightDp);
        dest.writeInt(this.compatSmallestScreenWidthDp);
        this.extraConfig.writeToParcel(dest, flags);
        dest.writeInt(this.seq);
    }

    public void readFromParcel(Parcel source) {
        boolean z = true;
        this.fontScale = source.readFloat();
        this.mcc = source.readInt();
        this.mnc = source.readInt();
        int localeListSize = source.readInt();
        Locale[] localeArray = new Locale[localeListSize];
        for (int i = UI_MODE_TYPE_UNDEFINED; i < localeListSize; i += UI_MODE_TYPE_NORMAL) {
            localeArray[i] = Locale.forLanguageTag(source.readString());
        }
        this.mLocaleList = new LocaleList(localeArray);
        this.locale = this.mLocaleList.get(UI_MODE_TYPE_UNDEFINED);
        if (source.readInt() != UI_MODE_TYPE_NORMAL) {
            z = false;
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
        this.uiMode = source.readInt();
        this.screenWidthDp = source.readInt();
        this.screenHeightDp = source.readInt();
        this.smallestScreenWidthDp = source.readInt();
        this.densityDpi = source.readInt();
        this.compatScreenWidthDp = source.readInt();
        this.compatScreenHeightDp = source.readInt();
        this.compatSmallestScreenWidthDp = source.readInt();
        this.extraConfig.readFromParcel(source);
        this.seq = source.readInt();
    }

    private Configuration(Parcel source) {
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
            return UI_MODE_TYPE_NORMAL;
        }
        int n = this.mcc - that.mcc;
        if (n != 0) {
            return n;
        }
        n = this.mnc - that.mnc;
        if (n != 0) {
            return n;
        }
        fixUpLocaleList();
        that.fixUpLocaleList();
        if (this.mLocaleList.isEmpty()) {
            if (!that.mLocaleList.isEmpty()) {
                return UI_MODE_TYPE_NORMAL;
            }
        } else if (that.mLocaleList.isEmpty()) {
            return -1;
        } else {
            int minSize = Math.min(this.mLocaleList.size(), that.mLocaleList.size());
            for (int i = UI_MODE_TYPE_UNDEFINED; i < minSize; i += UI_MODE_TYPE_NORMAL) {
                Locale thisLocale = this.mLocaleList.get(i);
                Locale thatLocale = that.mLocaleList.get(i);
                n = thisLocale.getLanguage().compareTo(thatLocale.getLanguage());
                if (n != 0) {
                    return n;
                }
                n = thisLocale.getCountry().compareTo(thatLocale.getCountry());
                if (n != 0) {
                    return n;
                }
                n = thisLocale.getVariant().compareTo(thatLocale.getVariant());
                if (n != 0) {
                    return n;
                }
                n = thisLocale.toLanguageTag().compareTo(thatLocale.toLanguageTag());
                if (n != 0) {
                    return n;
                }
            }
            n = this.mLocaleList.size() - that.mLocaleList.size();
            if (n != 0) {
                return n;
            }
        }
        n = this.touchscreen - that.touchscreen;
        if (n != 0) {
            return n;
        }
        n = this.keyboard - that.keyboard;
        if (n != 0) {
            return n;
        }
        n = this.keyboardHidden - that.keyboardHidden;
        if (n != 0) {
            return n;
        }
        n = this.hardKeyboardHidden - that.hardKeyboardHidden;
        if (n != 0) {
            return n;
        }
        n = this.navigation - that.navigation;
        if (n != 0) {
            return n;
        }
        n = this.navigationHidden - that.navigationHidden;
        if (n != 0) {
            return n;
        }
        n = this.orientation - that.orientation;
        if (n != 0) {
            return n;
        }
        n = this.screenLayout - that.screenLayout;
        if (n != 0) {
            return n;
        }
        n = this.uiMode - that.uiMode;
        if (n != 0) {
            return n;
        }
        n = this.screenWidthDp - that.screenWidthDp;
        if (n != 0) {
            return n;
        }
        n = this.screenHeightDp - that.screenHeightDp;
        if (n != 0) {
            return n;
        }
        n = this.smallestScreenWidthDp - that.smallestScreenWidthDp;
        if (n != 0) {
            return n;
        }
        n = this.densityDpi - that.densityDpi;
        if (n != 0) {
            return n;
        }
        return this.extraConfig.compareTo(that.extraConfig);
    }

    public boolean equals(Configuration that) {
        boolean z = true;
        if (that == null) {
            return false;
        }
        if (that == this) {
            return true;
        }
        if (compareTo(that) != 0) {
            z = false;
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
        return ((((((((((((((((((((((((((((((((((Float.floatToIntBits(this.fontScale) + 527) * 31) + this.mcc) * 31) + this.mnc) * 31) + this.mLocaleList.hashCode()) * 31) + this.touchscreen) * 31) + this.keyboard) * 31) + this.keyboardHidden) * 31) + this.hardKeyboardHidden) * 31) + this.navigation) * 31) + this.navigationHidden) * 31) + this.orientation) * 31) + this.screenLayout) * 31) + this.uiMode) * 31) + this.screenWidthDp) * 31) + this.screenHeightDp) * 31) + this.smallestScreenWidthDp) * 31) + this.densityDpi) * 31) + this.extraConfig.hashCode();
    }

    public LocaleList getLocales() {
        fixUpLocaleList();
        return this.mLocaleList;
    }

    public void setLocales(LocaleList locales) {
        if (locales == null) {
            locales = LocaleList.getEmptyLocaleList();
        }
        this.mLocaleList = locales;
        this.locale = this.mLocaleList.get(UI_MODE_TYPE_UNDEFINED);
        setLayoutDirection(this.locale);
    }

    public void setLocale(Locale loc) {
        LocaleList emptyLocaleList;
        if (loc == null) {
            emptyLocaleList = LocaleList.getEmptyLocaleList();
        } else {
            Locale[] localeArr = new Locale[UI_MODE_TYPE_NORMAL];
            localeArr[UI_MODE_TYPE_UNDEFINED] = loc;
            emptyLocaleList = new LocaleList(localeArr);
        }
        setLocales(emptyLocaleList);
    }

    public void clearLocales() {
        this.mLocaleList = LocaleList.getEmptyLocaleList();
        this.locale = null;
    }

    public int getLayoutDirection() {
        return (this.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) == SCREENLAYOUT_LAYOUTDIR_RTL ? UI_MODE_TYPE_NORMAL : UI_MODE_TYPE_UNDEFINED;
    }

    public void setLayoutDirection(Locale loc) {
        this.screenLayout = (this.screenLayout & -193) | ((TextUtils.getLayoutDirectionFromLocale(loc) + UI_MODE_TYPE_NORMAL) << UI_MODE_TYPE_WATCH);
    }

    private static int getScreenLayoutNoDirection(int screenLayout) {
        return screenLayout & -193;
    }

    public boolean isScreenRound() {
        return (this.screenLayout & SCREENLAYOUT_ROUND_MASK) == SCREENLAYOUT_ROUND_YES;
    }

    public static String localesToResourceQualifier(LocaleList locs) {
        StringBuilder sb = new StringBuilder();
        for (int i = UI_MODE_TYPE_UNDEFINED; i < locs.size(); i += UI_MODE_TYPE_NORMAL) {
            Locale loc = locs.get(i);
            boolean l = loc.getLanguage().length() != 0;
            boolean c = loc.getCountry().length() != 0;
            boolean s = loc.getScript().length() != 0;
            boolean v = loc.getVariant().length() != 0;
            if (l) {
                if (sb.length() != 0) {
                    sb.append(",");
                }
                sb.append(loc.getLanguage());
                if (c) {
                    sb.append("-r").append(loc.getCountry());
                    if (s) {
                        sb.append("-s").append(loc.getScript());
                        if (v) {
                            sb.append("-v").append(loc.getVariant());
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String resourceQualifierString(Configuration config) {
        ArrayList<String> parts = new ArrayList();
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
        switch (config.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) {
            case SCREENLAYOUT_LAYOUTDIR_LTR /*64*/:
                parts.add("ldltr");
                break;
            case SCREENLAYOUT_LAYOUTDIR_RTL /*128*/:
                parts.add("ldrtl");
                break;
        }
        int srcDpi = SystemProperties.getInt("ro.sf.lcd_density", UI_MODE_TYPE_UNDEFINED);
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
            int smallestScreenWidthDp = (config.smallestScreenWidthDp * realDpi) / srcDpi;
            int screenWidthDp = (config.screenWidthDp * realDpi) / srcDpi;
            int screenHeightDp = (config.screenHeightDp * realDpi) / srcDpi;
            if (config.smallestScreenWidthDp != 0) {
                parts.add(XML_ATTR_SMALLEST_WIDTH + smallestScreenWidthDp + "dp");
            }
            if (screenWidthDp != 0) {
                parts.add("w" + screenWidthDp + "dp");
            }
            if (screenHeightDp != 0) {
                parts.add("h" + screenHeightDp + "dp");
            }
        }
        switch (config.screenLayout & UI_MODE_TYPE_MASK) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add("small");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("normal");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                parts.add("large");
                break;
            case UI_MODE_TYPE_TELEVISION /*4*/:
                parts.add("xlarge");
                break;
        }
        switch (config.screenLayout & UI_MODE_NIGHT_MASK) {
            case UI_MODE_NIGHT_NO /*16*/:
                parts.add("notlong");
                break;
            case UI_MODE_NIGHT_YES /*32*/:
                parts.add("long");
                break;
        }
        switch (config.screenLayout & SCREENLAYOUT_ROUND_MASK) {
            case SCREENLAYOUT_ROUND_NO /*256*/:
                parts.add("notround");
                break;
            case SCREENLAYOUT_ROUND_YES /*512*/:
                parts.add("round");
                break;
        }
        switch (config.orientation) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add(UsbManager.EXTRA_PORT);
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("land");
                break;
        }
        switch (config.uiMode & UI_MODE_TYPE_MASK) {
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("desk");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                parts.add("car");
                break;
            case UI_MODE_TYPE_TELEVISION /*4*/:
                parts.add("television");
                break;
            case UI_MODE_TYPE_APPLIANCE /*5*/:
                parts.add("appliance");
                break;
            case UI_MODE_TYPE_WATCH /*6*/:
                parts.add("watch");
                break;
        }
        switch (config.uiMode & UI_MODE_NIGHT_MASK) {
            case UI_MODE_NIGHT_NO /*16*/:
                parts.add("notnight");
                break;
            case UI_MODE_NIGHT_YES /*32*/:
                parts.add(Parameters.SCENE_MODE_NIGHT);
                break;
        }
        switch (srcDpi) {
            case UI_MODE_TYPE_UNDEFINED /*0*/:
                break;
            case BluetoothAssignedNumbers.NIKE /*120*/:
                parts.add("ldpi");
                break;
            case Const.CODE_G3_RANGE_START /*160*/:
                parts.add("mdpi");
                break;
            case BluetoothAssignedNumbers.AUSTCO_COMMUNICATION_SYSTEMS /*213*/:
                parts.add("tvdpi");
                break;
            case NetworkPolicyManager.MASK_ALL_NETWORKS /*240*/:
                parts.add("hdpi");
                break;
            case MifareClassic.SIZE_MINI /*320*/:
                parts.add("xhdpi");
                break;
            case 480:
                parts.add("xxhdpi");
                break;
            case 640:
                parts.add("xxxhdpi");
                break;
            case DENSITY_DPI_ANY /*65534*/:
                parts.add("anydpi");
                break;
            case MNC_ZERO /*65535*/:
                parts.add("nodpi");
                break;
        }
        parts.add(config.densityDpi + "dpi");
        switch (config.touchscreen) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add("notouch");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                parts.add("finger");
                break;
        }
        switch (config.keyboardHidden) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add("keysexposed");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("keyshidden");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                parts.add("keyssoft");
                break;
        }
        switch (config.keyboard) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add("nokeys");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("qwerty");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                parts.add("12key");
                break;
        }
        switch (config.navigationHidden) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add("navexposed");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("navhidden");
                break;
        }
        switch (config.navigation) {
            case UI_MODE_TYPE_NORMAL /*1*/:
                parts.add("nonav");
                break;
            case UI_MODE_TYPE_DESK /*2*/:
                parts.add("dpad");
                break;
            case UI_MODE_TYPE_CAR /*3*/:
                parts.add("trackball");
                break;
            case UI_MODE_TYPE_TELEVISION /*4*/:
                parts.add("wheel");
                break;
        }
        parts.add("v" + VERSION.RESOURCES_SDK_INT);
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
        if ((base.screenLayout & UI_MODE_TYPE_MASK) != (change.screenLayout & UI_MODE_TYPE_MASK)) {
            delta.screenLayout |= change.screenLayout & UI_MODE_TYPE_MASK;
        }
        if ((base.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) != (change.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK)) {
            delta.screenLayout |= change.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK;
        }
        if ((base.screenLayout & UI_MODE_NIGHT_MASK) != (change.screenLayout & UI_MODE_NIGHT_MASK)) {
            delta.screenLayout |= change.screenLayout & UI_MODE_NIGHT_MASK;
        }
        if ((base.screenLayout & SCREENLAYOUT_ROUND_MASK) != (change.screenLayout & SCREENLAYOUT_ROUND_MASK)) {
            delta.screenLayout |= change.screenLayout & SCREENLAYOUT_ROUND_MASK;
        }
        if ((base.uiMode & UI_MODE_TYPE_MASK) != (change.uiMode & UI_MODE_TYPE_MASK)) {
            delta.uiMode |= change.uiMode & UI_MODE_TYPE_MASK;
        }
        if ((base.uiMode & UI_MODE_NIGHT_MASK) != (change.uiMode & UI_MODE_NIGHT_MASK)) {
            delta.uiMode |= change.uiMode & UI_MODE_NIGHT_MASK;
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
        return delta;
    }

    public static void readXmlAttrs(XmlPullParser parser, Configuration configOut) throws XmlPullParserException, IOException {
        configOut.fontScale = Float.intBitsToFloat(XmlUtils.readIntAttribute(parser, XML_ATTR_FONT_SCALE, UI_MODE_TYPE_UNDEFINED));
        configOut.mcc = XmlUtils.readIntAttribute(parser, XML_ATTR_MCC, UI_MODE_TYPE_UNDEFINED);
        configOut.mnc = XmlUtils.readIntAttribute(parser, XML_ATTR_MNC, UI_MODE_TYPE_UNDEFINED);
        configOut.mLocaleList = LocaleList.forLanguageTags(XmlUtils.readStringAttribute(parser, XML_ATTR_LOCALES));
        configOut.locale = configOut.mLocaleList.get(UI_MODE_TYPE_UNDEFINED);
        configOut.touchscreen = XmlUtils.readIntAttribute(parser, XML_ATTR_TOUCHSCREEN, UI_MODE_TYPE_UNDEFINED);
        configOut.keyboard = XmlUtils.readIntAttribute(parser, XML_ATTR_KEYBOARD, UI_MODE_TYPE_UNDEFINED);
        configOut.keyboardHidden = XmlUtils.readIntAttribute(parser, XML_ATTR_KEYBOARD_HIDDEN, UI_MODE_TYPE_UNDEFINED);
        configOut.hardKeyboardHidden = XmlUtils.readIntAttribute(parser, XML_ATTR_HARD_KEYBOARD_HIDDEN, UI_MODE_TYPE_UNDEFINED);
        configOut.navigation = XmlUtils.readIntAttribute(parser, XML_ATTR_NAVIGATION, UI_MODE_TYPE_UNDEFINED);
        configOut.navigationHidden = XmlUtils.readIntAttribute(parser, XML_ATTR_NAVIGATION_HIDDEN, UI_MODE_TYPE_UNDEFINED);
        configOut.orientation = XmlUtils.readIntAttribute(parser, XML_ATTR_ORIENTATION, UI_MODE_TYPE_UNDEFINED);
        configOut.screenLayout = XmlUtils.readIntAttribute(parser, XML_ATTR_SCREEN_LAYOUT, UI_MODE_TYPE_UNDEFINED);
        configOut.uiMode = XmlUtils.readIntAttribute(parser, XML_ATTR_UI_MODE, UI_MODE_TYPE_UNDEFINED);
        configOut.screenWidthDp = XmlUtils.readIntAttribute(parser, XML_ATTR_SCREEN_WIDTH, UI_MODE_TYPE_UNDEFINED);
        configOut.screenHeightDp = XmlUtils.readIntAttribute(parser, XML_ATTR_SCREEN_HEIGHT, UI_MODE_TYPE_UNDEFINED);
        configOut.smallestScreenWidthDp = XmlUtils.readIntAttribute(parser, XML_ATTR_SMALLEST_WIDTH, UI_MODE_TYPE_UNDEFINED);
        configOut.densityDpi = XmlUtils.readIntAttribute(parser, XML_ATTR_DENSITY, UI_MODE_TYPE_UNDEFINED);
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
        if (config.uiMode != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_UI_MODE, config.uiMode);
        }
        if (config.screenWidthDp != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SCREEN_WIDTH, config.screenWidthDp);
        }
        if (config.screenHeightDp != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SCREEN_HEIGHT, config.screenHeightDp);
        }
        if (config.smallestScreenWidthDp != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_SMALLEST_WIDTH, config.smallestScreenWidthDp);
        }
        if (config.densityDpi != 0) {
            XmlUtils.writeIntAttribute(xml, XML_ATTR_DENSITY, config.densityDpi);
        }
    }
}
