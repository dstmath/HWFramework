package com.huawei.android.pushagent.utils.c;

import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import com.huawei.android.pushagent.utils.d.c;
import java.util.UUID;

public class b extends f {
    private int deviceIdType = 9;

    public String getDeviceId() {
        try {
            return super.getDeviceId();
        } catch (AndroidRuntimeException e) {
            c.sf("PushLog2951", "framework get udid exist exception");
        } catch (Exception e2) {
            c.sf("PushLog2951", "framework get udid exist uncatch exception, identify as udid");
        }
        return qw();
    }

    public String qv() {
        String deviceId = getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            return qw();
        }
        return deviceId;
    }

    public int getDeviceIdType() {
        return this.deviceIdType;
    }

    public String qw() {
        c.sh("PushLog2951", "get UUID as deviceID");
        this.deviceIdType = 6;
        StringBuilder stringBuilder = new StringBuilder("_" + UUID.randomUUID().toString().replace("-", ""));
        while (stringBuilder.length() < 64) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString().substring(0, 64);
    }
}
