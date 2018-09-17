package com.android.server.pm.auth.deviceid;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdMac implements DeviceId {
    public List<String> mMacs = new ArrayList();

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
            sb.append((String) this.mMacs.get(i));
        }
    }

    public static boolean isType(String ids) {
        if (ids.startsWith("WIFIMAC/")) {
            return true;
        }
        return false;
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
