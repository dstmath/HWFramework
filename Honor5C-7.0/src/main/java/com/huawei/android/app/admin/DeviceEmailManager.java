package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceEmailManager {
    private static final String TAG = "DeviceEmailManager";
    private final HwDevicePolicyManagerEx mDpm;

    public DeviceEmailManager() {
        this.mDpm = new HwDevicePolicyManagerEx();
    }

    public void configExchangeMailProvider(ComponentName admin, HwMailProvider para) {
        if (para == null) {
            Log.w(TAG, "configExchangeMailProvider para is null");
            return;
        }
        Bundle paraex = new Bundle();
        paraex.putString("id", para.getId());
        paraex.putString("label", para.getLabel());
        paraex.putString("domain", para.getDomain());
        paraex.putString("incominguri", para.getIncominguri());
        paraex.putString("incomingusername", para.getIncomingusername());
        paraex.putString("incomingfield", para.getIncomingfield());
        paraex.putString("outgoinguri", para.getOutgoinguri());
        paraex.putString("outgoingusername", para.getOutgoingusername());
        this.mDpm.configExchangeMailProvider(admin, paraex);
    }

    public HwMailProvider getMailProviderForDomain(ComponentName admin, String domain) {
        Bundle paraex = this.mDpm.getMailProviderForDomain(admin, domain);
        if (paraex != null) {
            return new HwMailProvider(paraex.getString("id"), paraex.getString("label"), paraex.getString("domain"), paraex.getString("incominguri"), paraex.getString("incomingusername"), paraex.getString("incomingfield"), paraex.getString("outgoinguri"), paraex.getString("outgoingusername"));
        }
        return null;
    }
}
