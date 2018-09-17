package android.app.admin;

import java.util.ArrayList;
import java.util.List;

public class HwManifest {
    public static final List<String> PERMIISONS_LIST = new ArrayList();
    public static final String PERMISSION_ACCESS_UDID = "com.huawei.permission.sec.ACCESS_UDID";
    public static final String PERMISSION_MDM = "com.huawei.permission.sec.MDM";
    public static final String PERMISSION_MDM_APN = "com.huawei.permission.sec.MDM_APN";
    public static final String PERMISSION_MDM_APP_MANAGEMENT = "com.huawei.permission.sec.MDM_APP_MANAGEMENT";
    public static final String PERMISSION_MDM_BLUETOOTH = "com.huawei.permission.sec.MDM_BLUETOOTH";
    public static final String PERMISSION_MDM_CAMERA = "com.huawei.permission.sec.MDM_CAMERA";
    public static final String PERMISSION_MDM_CAPTURE_SCREEN = "com.huawei.permission.sec.MDM_CAPTURE_SCREEN";
    public static final String PERMISSION_MDM_CLIPBOARD = "com.huawei.permission.sec.MDM_CLIPBOARD";
    public static final String PERMISSION_MDM_CONNECTIVITY = "com.huawei.permission.sec.MDM_CONNECTIVITY";
    public static final String PERMISSION_MDM_DEVICE_MANAGER = "com.huawei.permission.sec.MDM_DEVICE_MANAGER";
    public static final String PERMISSION_MDM_EMAIL = "com.huawei.permission.sec.MDM_EMAIL";
    public static final String PERMISSION_MDM_FINGERPRINT = "com.huawei.permission.sec.MDM_FINGERPRINT";
    public static final String PERMISSION_MDM_GOOGLE_ACCOUNT = "com.huawei.permission.sec.MDM_GOOGLE_ACCOUNT";
    public static final String PERMISSION_MDM_KEYGUARD = "com.huawei.permission.sec.MDM_KEYGUARD";
    public static final String PERMISSION_MDM_LOCATION = "com.huawei.permission.sec.MDM_LOCATION";
    public static final String PERMISSION_MDM_MMS = "com.huawei.permission.sec.MDM_MMS";
    public static final String PERMISSION_MDM_NETWORK_MANAGER = "com.huawei.permission.sec.MDM_NETWORK_MANAGER";
    public static final String PERMISSION_MDM_NFC = "com.huawei.permission.sec.MDM_NFC";
    public static final String PERMISSION_MDM_PHONE = "com.huawei.permission.sec.MDM_PHONE";
    public static final String PERMISSION_MDM_PHONE_MANAGER = "com.huawei.permission.sec.MDM_PHONE_MANAGER";
    public static final String PERMISSION_MDM_PREFIX = "com.huawei.permission.sec.MDM_";
    public static final String PERMISSION_MDM_SDCARD = "com.huawei.permission.sec.MDM_SDCARD";
    public static final String PERMISSION_MDM_UPDATESTATE_MANAGER = "com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER";
    public static final String PERMISSION_MDM_USB = "com.huawei.permission.sec.MDM_USB";
    public static final String PERMISSION_MDM_V2 = "com.huawei.permission.sec.MDM.v2";
    public static final String PERMISSION_MDM_WIFI = "com.huawei.permission.sec.MDM_WIFI";
    public static final String PERMISSION_SDK_LAUNCHER = "com.huawei.permission.sec.SDK_LAUNCHER";
    public static final String PERMISSION_SDK_PREFIX = "com.huawei.permission.sec.SDK_";

    static {
        PERMIISONS_LIST.add(PERMISSION_MDM_APP_MANAGEMENT);
        PERMIISONS_LIST.add(PERMISSION_MDM_CAMERA);
        PERMIISONS_LIST.add(PERMISSION_MDM_BLUETOOTH);
        PERMIISONS_LIST.add(PERMISSION_MDM_FINGERPRINT);
        PERMIISONS_LIST.add(PERMISSION_MDM_PHONE_MANAGER);
        PERMIISONS_LIST.add(PERMISSION_MDM_WIFI);
        PERMIISONS_LIST.add(PERMISSION_MDM_EMAIL);
        PERMIISONS_LIST.add(PERMISSION_MDM_USB);
        PERMIISONS_LIST.add(PERMISSION_MDM_SDCARD);
        PERMIISONS_LIST.add(PERMISSION_MDM_PHONE);
        PERMIISONS_LIST.add(PERMISSION_MDM_MMS);
        PERMIISONS_LIST.add(PERMISSION_MDM_DEVICE_MANAGER);
        PERMIISONS_LIST.add(PERMISSION_MDM_NFC);
        PERMIISONS_LIST.add(PERMISSION_MDM_CONNECTIVITY);
        PERMIISONS_LIST.add(PERMISSION_MDM_KEYGUARD);
        PERMIISONS_LIST.add(PERMISSION_MDM_APN);
        PERMIISONS_LIST.add(PERMISSION_MDM_LOCATION);
        PERMIISONS_LIST.add(PERMISSION_MDM_CAPTURE_SCREEN);
        PERMIISONS_LIST.add(PERMISSION_MDM_NETWORK_MANAGER);
        PERMIISONS_LIST.add(PERMISSION_SDK_LAUNCHER);
        PERMIISONS_LIST.add(PERMISSION_MDM_CLIPBOARD);
        PERMIISONS_LIST.add(PERMISSION_MDM_GOOGLE_ACCOUNT);
    }
}
