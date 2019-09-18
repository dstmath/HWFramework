package com.android.server.pm.auth.deviceid;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdMeid implements DeviceId {
    private List<String> mMeids = new ArrayList();

    public static boolean isType(String ids) {
        if (ids.startsWith("MEID/")) {
            return true;
        }
        return false;
    }

    public void addDeviceId(String id) {
        this.mMeids.add(id);
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    public void append(StringBuffer sb) {
        sb.append("MEID/");
        for (int i = 0; i < this.mMeids.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(this.mMeids.get(i));
        }
    }

    public boolean contain(String devId) {
        for (String id : this.mMeids) {
            if (id.equalsIgnoreCase(devId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.mMeids.isEmpty();
    }
}
