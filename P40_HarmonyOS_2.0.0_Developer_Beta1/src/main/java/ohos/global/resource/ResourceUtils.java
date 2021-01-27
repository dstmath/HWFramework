package ohos.global.resource;

import android.os.LocaleList;
import java.util.Locale;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.configuration.LocaleProfile;
import ohos.system.Parameters;

public class ResourceUtils {
    private static final int DEFAULT_DENSITY = 160;

    public static Configuration convert(android.content.res.Configuration configuration) {
        Configuration configuration2 = new Configuration();
        covertLocale(configuration, configuration2);
        int i = configuration.orientation;
        if (i == 1) {
            configuration2.direction = 0;
        } else if (i != 2) {
            configuration2.direction = -1;
        } else {
            configuration2.direction = 1;
        }
        configuration2.fontRatio = configuration.fontScale;
        configuration2.isLayoutRTL = isLayoutRTL(configuration);
        int i2 = configuration.uiMode & 48;
        if (i2 == 16) {
            configuration2.sysColorMode = 1;
        } else if (i2 == 32) {
            configuration2.sysColorMode = 0;
        } else {
            configuration2.sysColorMode = -1;
        }
        return configuration2;
    }

    public static DeviceCapability convertToDeviceCapability(android.content.res.Configuration configuration) {
        DeviceCapability deviceCapability = new DeviceCapability();
        deviceCapability.deviceType = getDeviceType();
        deviceCapability.screenDensity = configuration.densityDpi;
        deviceCapability.isRound = configuration.isScreenRound();
        deviceCapability.width = configuration.screenWidthDp;
        deviceCapability.height = configuration.screenHeightDp;
        return deviceCapability;
    }

    private static void covertLocale(android.content.res.Configuration configuration, Configuration configuration2) {
        LocaleList localeList = LocaleList.getDefault();
        if (localeList.isEmpty()) {
            configuration2.setLocaleProfile(new LocaleProfile(new Locale[]{Locale.getDefault()}));
            return;
        }
        Locale[] localeArr = new Locale[localeList.size()];
        for (int i = 0; i < localeList.size(); i++) {
            Object clone = localeList.get(i).clone();
            localeArr[i] = clone instanceof Locale ? (Locale) clone : null;
        }
        configuration2.setLocaleProfile(new LocaleProfile(localeArr));
    }

    private static boolean isLayoutRTL(android.content.res.Configuration configuration) {
        return configuration.getLayoutDirection() == 1;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getDeviceType() {
        char c;
        String lowerCase = Parameters.get("ro.build.characteristics", "phone").toLowerCase();
        switch (lowerCase.hashCode()) {
            case -881377690:
                if (lowerCase.equals("tablet")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3714:
                if (lowerCase.equals("tv")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 98260:
                if (lowerCase.equals("car")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 106642798:
                if (lowerCase.equals("phone")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 112903375:
                if (lowerCase.equals("watch")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return 6;
        }
        if (c == 1) {
            return 4;
        }
        if (c != 2) {
            return (c == 3 || c != 4) ? 0 : 2;
        }
        return 1;
    }

    public static int getDensity() {
        try {
            return Integer.parseInt(Parameters.get("hw.lcd.density", "160")) / DEFAULT_DENSITY;
        } catch (NumberFormatException unused) {
            return 1;
        }
    }
}
