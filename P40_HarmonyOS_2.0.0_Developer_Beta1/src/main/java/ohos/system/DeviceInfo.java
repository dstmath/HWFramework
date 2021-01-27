package ohos.system;

import java.util.Optional;
import ohos.annotation.SystemApi;
import ohos.system.controller.DeviceIdController;

public final class DeviceInfo {
    private static final String LOCALE = Parameters.get("ro.product.locale");
    private static final String LOCALE_LANGUAGE = Parameters.get("ro.product.locale.language");
    private static final String LOCALE_REGION = Parameters.get("ro.product.locale.region");
    private static final String MODEL = Parameters.get("ro.product.model");
    private static final String NAME = Parameters.get("ro.product.name");

    private DeviceInfo() {
    }

    public static String getModel() {
        return MODEL;
    }

    public static String getName() {
        return NAME;
    }

    public static String getLocale() {
        return LOCALE;
    }

    public static String getLocaleLanguage() {
        return LOCALE_LANGUAGE;
    }

    public static String getLocaleRegion() {
        return LOCALE_REGION;
    }

    @SystemApi
    public static Optional<String> getUdid() {
        return DeviceIdController.getUdid();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0035 A[RETURN] */
    public static String getDeviceType() {
        char c;
        String str = Parameters.get("ro.build.characteristics", "");
        int hashCode = str.hashCode();
        if (hashCode != 112903375) {
            if (hashCode == 297574343 && str.equals("fitnessWatch")) {
                c = 1;
                if (c != 0) {
                    return c != 1 ? str : "liteWearable";
                }
                return "wearable";
            }
        } else if (str.equals("watch")) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }
}
