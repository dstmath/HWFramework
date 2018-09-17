package com.android.server.pm.auth.deviceid;

import java.util.ArrayList;
import java.util.List;

public class DeviceIdMeid implements DeviceId {
    public List<String> mMeids;

    public DeviceIdMeid() {
        this.mMeids = new ArrayList();
    }

    public void addDeviceId(String id) {
        this.mMeids.add(id);
    }

    public void append(StringBuffer sb) {
        sb.append(DeviceId.TAG_MEID);
        for (int i = 0; i < this.mMeids.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append((String) this.mMeids.get(i));
        }
    }

    public static boolean isType(String ids) {
        if (ids.startsWith(DeviceId.TAG_MEID)) {
            return true;
        }
        return false;
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
