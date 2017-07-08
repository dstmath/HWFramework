package com.android.server.pm.auth.deviceid;

import java.util.ArrayList;
import java.util.List;

public class DeviceIdList implements DeviceId {
    private List<String> mIds;

    public DeviceIdList() {
        this.mIds = new ArrayList();
    }

    public void addDeviceId(String id) {
        this.mIds.add(id);
    }

    public void append(StringBuffer sb) {
        sb.append(DeviceId.TAG_IMEI);
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
        if (!ids.startsWith(DeviceId.TAG_IMEI) || ids.indexOf("-") >= 0) {
            return false;
        }
        return true;
    }
}
