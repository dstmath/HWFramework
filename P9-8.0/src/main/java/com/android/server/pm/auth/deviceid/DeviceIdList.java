package com.android.server.pm.auth.deviceid;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdList implements DeviceId {
    private List<String> mIds = new ArrayList();

    public void addDeviceId(String id) {
        this.mIds.add(id);
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    public void append(StringBuffer sb) {
        sb.append("IMEI/");
        for (int i = 0; i < this.mIds.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append((String) this.mIds.get(i));
        }
    }

    public boolean contain(String devId) {
        for (String id : this.mIds) {
            if (id.equals(devId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.mIds.isEmpty();
    }

    public static boolean isType(String ids) {
        if (!ids.startsWith("IMEI/") || ids.indexOf("-") >= 0) {
            return false;
        }
        return true;
    }
}
