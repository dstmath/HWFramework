package com.android.server.pm.auth.deviceid;

import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.ArrayList;
import java.util.List;

public class DeviceIdList implements DeviceId {
    private List<String> mDeviceIds = new ArrayList();

    public static boolean isType(String deviceId) {
        return !TextUtils.isEmpty(deviceId) && deviceId.startsWith("IMEI/") && deviceId.indexOf(AwarenessInnerConstants.DASH_KEY) < 0;
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public void addDeviceId(String deviceId) {
        if (!TextUtils.isEmpty(deviceId)) {
            this.mDeviceIds.add(deviceId);
        }
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public void append(StringBuffer strBuf) {
        if (strBuf != null) {
            strBuf.append("IMEI/");
            int size = this.mDeviceIds.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    strBuf.append(",");
                }
                strBuf.append(this.mDeviceIds.get(i));
            }
        }
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public boolean contain(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            return false;
        }
        int size = this.mDeviceIds.size();
        for (int i = 0; i < size; i++) {
            if (this.mDeviceIds.get(i).equals(deviceId)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.pm.auth.deviceid.DeviceId
    public boolean isEmpty() {
        return this.mDeviceIds.isEmpty();
    }
}
