package ohos.global.resource;

import android.os.LocaleList;
import android.os.SystemProperties;
import java.util.Locale;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.configuration.LocaleProfile;

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
        LocaleList locales = configuration.getLocales();
        if (locales.isEmpty()) {
            configuration2.setLocaleProfile(new LocaleProfile(new Locale[]{Locale.getDefault()}));
            return;
        }
        Locale[] localeArr = new Locale[locales.size()];
        for (int i = 0; i < locales.size(); i++) {
            Object clone = locales.get(i).clone();
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
        String lowerCase = SystemProperties.get("ro.build.characteristics", "phone").toLowerCase();
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
            return Integer.parseInt(SystemProperties.get("hw.lcd.density", "160")) / DEFAULT_DENSITY;
        } catch (NumberFormatException unused) {
            return 1;
        }
    }
}
