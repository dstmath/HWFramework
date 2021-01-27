package com.android.server.pm.auth.deviceid;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdMeid implements DeviceId {
    private List<String> mMeids = new ArrayList();

    public static boolean isType(String deviceId) {
        return !TextUtils.isEmpty(deviceId) && deviceId.startsWith("MEID/");
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public void addDeviceId(String deviceId) {
        if (!TextUtils.isEmpty(deviceId)) {
            this.mMeids.add(deviceId);
        }
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public void append(StringBuffer strBuf) {
        if (strBuf != null) {
            strBuf.append("MEID/");
            int size = this.mMeids.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    strBuf.append(",");
                }
                strBuf.append(this.mMeids.get(i));
            }
        }
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public boolean contain(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return false;
        }
        int size = this.mMeids.size();
        for (int i = 0; i < size; i++) {
            if (this.mMeids.get(i).equalsIgnoreCase(deviceId)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public boolean isEmpty() {
        return this.mMeids.isEmpty();
    }
}
