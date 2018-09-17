package com.huawei.deliver.info;

import android.os.SystemProperties;
import com.huawei.android.smcs.SmartTrimProcessEvent;

public class HwDeliverInfo {
    public static final int DELIVER_BRAND_ASCEND = 0;
    public static final int DELIVER_BRAND_HONNER = 1;
    private static final int DELIVER_BRAND_START = 0;
    public static final int DELIVER_CHANNEL_OPERATOR_CUSTOMIZE = 1;
    public static final int DELIVER_CHANNEL_PUBLIC_RELEASE = 0;
    private static final int DELIVER_CHANNEL_START = 0;
    public static final int DELIVER_CHANNEL_WEB_RELEASE = 2;

    public static final int getBrand() {
        return getDeliverInfo(0);
    }

    public static final int getReleaseChannel() {
        return getDeliverInfo(1);
    }

    public static final int getOperatorMcc() {
        return getDeliverInfo(2);
    }

    public static final int getOperatorMnc() {
        return getDeliverInfo(3);
    }

    public static final boolean isIOTVersion() {
        return 1 == getDeliverInfo(4);
    }

    private static final int getDeliverInfo(int index) {
        String[] infos = SystemProperties.get("ro.config.hw_channel_info", "0,0,460,1,0").split(SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN);
        if (infos.length >= index + 1 && infos[index] != null) {
            try {
                return Integer.parseInt(infos[index]);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }
}
