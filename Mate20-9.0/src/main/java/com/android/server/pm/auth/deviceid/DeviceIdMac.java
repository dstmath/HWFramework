package com.android.server.pm.auth.deviceid;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdMac implements DeviceId {
    private List<String> mMacs = new ArrayList();

    public static boolean isType(String ids) {
        if (ids.startsWith("WIFIMAC/")) {
            return true;
        }
        return false;
    }

    public void addDeviceId(String id) {
        this.mMacs.add(id);
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    public void append(StringBuffer sb) {
        sb.append("WIFIMAC/");
        for (int i = 0; i < this.mMacs.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(this.mMacs.get(i));
        }
    }

    public boolean contain(String devId) {
        for (String id : this.mMacs) {
            if (id.equals(devId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.mMacs.isEmpty();
    }
}
