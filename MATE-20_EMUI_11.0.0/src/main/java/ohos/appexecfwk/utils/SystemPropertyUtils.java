package ohos.appexecfwk.utils;

import java.lang.reflect.InvocationTargetException;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.hiviewdfx.HiLogLabel;

public class SystemPropertyUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "SystemPropertyUtils");
    private static final String SYSTEM_PARAMETER_KEY_DEVICE_TYPE = "ro.build.characteristics";
    private static final String SYSTEM_PROPERTIES_CLASS_NAME = "android.os.SystemProperties";

    public enum DeviceType {
        DEVICE_TYPE_PHONE("phone"),
        DEVICE_TYPE_TABLET("tablet"),
        DEVICE_TYPE_TV("tv"),
        DEVICE_TYPE_WEARABLE("wearable"),
        DEVICE_TYPE_LITEWEARABLE("liteWearable"),
        DEVICE_TYPE_AR("ar"),
        DEVICE_TYPE_VR("vr"),
        DEVICE_TYPE_CAR("car"),
        DEVICE_TYPE_EARPHONES("earphones"),
        DEVICE_TYPE_PC("pc"),
        DEVICE_TYPE_SPEAKER("speaker"),
        DEVICE_TYPE_SMARTVISION("smartVision"),
        DEVICE_TYPE_LINKIOT("linkIoT"),
        DEVICE_TYPE_ROUTER("router"),
        DEVICE_TYPE_DEFAULT("default");
        
        private String deviceType;

        private DeviceType(String str) {
            this.deviceType = str;
        }

        public String getDeviceType() {
            return this.deviceType;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static DeviceType getDeviceType() {
        char c;
        String realDeviceType = getRealDeviceType(getSystemProperty(SYSTEM_PARAMETER_KEY_DEVICE_TYPE));
        switch (realDeviceType.hashCode()) {
            case -2008522753:
                if (realDeviceType.equals("speaker")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -925132983:
                if (realDeviceType.equals("router")) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -881377690:
                if (realDeviceType.equals("tablet")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -730112679:
                if (realDeviceType.equals("wearable")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -414801135:
                if (realDeviceType.equals("smartVision")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 3121:
                if (realDeviceType.equals("ar")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 3571:
                if (realDeviceType.equals("pc")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 3714:
                if (realDeviceType.equals("tv")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3772:
                if (realDeviceType.equals("vr")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 98260:
                if (realDeviceType.equals("car")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 106642798:
                if (realDeviceType.equals("phone")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 177059220:
                if (realDeviceType.equals("linkIoT")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 786735003:
                if (realDeviceType.equals("earphones")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1499194407:
                if (realDeviceType.equals("liteWearable")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1544803905:
                if (realDeviceType.equals("default")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return DeviceType.DEVICE_TYPE_PHONE;
            case 1:
                return DeviceType.DEVICE_TYPE_TABLET;
            case 2:
                return DeviceType.DEVICE_TYPE_TV;
            case 3:
                return DeviceType.DEVICE_TYPE_WEARABLE;
            case 4:
                return DeviceType.DEVICE_TYPE_LITEWEARABLE;
            case 5:
                return DeviceType.DEVICE_TYPE_AR;
            case 6:
                return DeviceType.DEVICE_TYPE_VR;
            case 7:
                return DeviceType.DEVICE_TYPE_CAR;
            case '\b':
                return DeviceType.DEVICE_TYPE_EARPHONES;
            case '\t':
                return DeviceType.DEVICE_TYPE_PC;
            case '\n':
                return DeviceType.DEVICE_TYPE_SPEAKER;
            case 11:
                return DeviceType.DEVICE_TYPE_SMARTVISION;
            case '\f':
                return DeviceType.DEVICE_TYPE_LINKIOT;
            case '\r':
                return DeviceType.DEVICE_TYPE_ROUTER;
            case 14:
                return DeviceType.DEVICE_TYPE_DEFAULT;
            default:
                AppLog.w(LABEL, "unknow device type", new Object[0]);
                return DeviceType.DEVICE_TYPE_DEFAULT;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032  */
    private static String getRealDeviceType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != 112903375) {
            if (hashCode == 297574343 && str.equals("fitnessWatch")) {
                c = 1;
                if (c == 0) {
                    return DeviceType.DEVICE_TYPE_WEARABLE.getDeviceType();
                }
                if (c != 1) {
                    return str;
                }
                return DeviceType.DEVICE_TYPE_LITEWEARABLE.getDeviceType();
            }
        } else if (str.equals("watch")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    private static String getSystemProperty(String str) {
        try {
            Object invoke = Class.forName(SYSTEM_PROPERTIES_CLASS_NAME).getMethod("get", String.class).invoke(null, str);
            if (invoke instanceof String) {
                return (String) invoke;
            }
            return "";
        } catch (ClassNotFoundException unused) {
            AppLog.w(LABEL, "getSystemProperty error ClassNotFoundException", new Object[0]);
            return "";
        } catch (NoSuchMethodException unused2) {
            AppLog.w(LABEL, "getSystemProperty error NoSuchMethodException", new Object[0]);
            return "";
        } catch (InvocationTargetException unused3) {
            AppLog.w(LABEL, "getSystemProperty error InvocationTargetException", new Object[0]);
            return "";
        } catch (IllegalAccessException unused4) {
            AppLog.w(LABEL, "getSystemProperty error IllegalAccessException", new Object[0]);
            return "";
        }
    }
}
