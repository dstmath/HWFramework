package com.android.server.security;

import android.content.Context;
import com.android.server.SystemService;
import com.huawei.server.security.DefaultHwSecurityServiceProxy;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;

public class HwSecurityService extends SystemService {
    private DefaultHwSecurityServiceProxy proxy;

    public HwSecurityService(Context context) {
        super(context);
        this.proxy = HwServiceSecurityPartsFactoryEx.getInstance().getHwSecurityServiceProxy(context);
    }

    public void onStart() {
        this.proxy.onStart();
    }

    public void onBootPhase(int phase) {
        this.proxy.onBootPhase(phase);
    }
}
