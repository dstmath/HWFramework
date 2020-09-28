package com.huawei.dmsdpsdk2.common;

import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdp.devicevirtualization.Capability;
import com.huawei.dmsdpsdk2.DMSDPConfig;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static <T extends Enum<T>> String getEnumSetString(EnumSet<T> enumSet) {
        if (enumSet == null) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        Iterator it = enumSet.iterator();
        while (it.hasNext()) {
            sb.append((Enum) it.next());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public static <T> String getListString(List<T> list) {
        if (list == null) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (T t : list) {
            sb.append((Object) t);
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public static Capability convertServiceTypeToCapability(int serviceType) {
        if (serviceType == 1) {
            return Capability.CAMERA;
        }
        if (serviceType == 2) {
            return Capability.MIC;
        }
        if (serviceType == 4) {
            return Capability.SPEAKER;
        }
        if (serviceType == 8) {
            return Capability.DISPLAY;
        }
        if (serviceType == 2048) {
            return Capability.SENSOR;
        }
        if (serviceType == 4096) {
            return Capability.VIBRATE;
        }
        if (serviceType != 8192) {
            return null;
        }
        return Capability.NOTIFICATION;
    }

    public static int convertCapabilityToServiceType(Capability cap) {
        switch (cap) {
            case CAMERA:
                return 1;
            case MIC:
                return 2;
            case SPEAKER:
                return 4;
            case DISPLAY:
                return 8;
            case VIBRATE:
                return DMSDPConfig.DEVICE_SERVICE_TYPE_VIBRATE;
            case SENSOR:
                return DMSDPConfig.DEVICE_SERVICE_TYPE_SENSOR;
            case NOTIFICATION:
                return DMSDPConfig.DEVICE_SERVICE_TYPE_NOTIFICATION;
            default:
                return -1;
        }
    }
}
