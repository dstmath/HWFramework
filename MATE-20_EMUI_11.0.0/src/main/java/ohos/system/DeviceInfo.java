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

    public static String getDeviceType() {
        return Parameters.get("ro.build.characteristics", "");
    }
}
