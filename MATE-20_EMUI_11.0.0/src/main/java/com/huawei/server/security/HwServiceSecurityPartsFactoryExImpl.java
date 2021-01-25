package com.huawei.server.security;

import android.content.Context;
import com.huawei.server.security.behaviorcollect.BehaviorCollector;
import com.huawei.server.security.behaviorcollect.DefaultBehaviorCollector;
import com.huawei.server.security.fileprotect.DefaultHwAppAuthManager;
import com.huawei.server.security.fileprotect.DefaultHwSfpService;
import com.huawei.server.security.fileprotect.HwAppAuthManager;
import com.huawei.server.security.fileprotect.HwSfpServiceAdapter;
import com.huawei.server.security.hsm.DefaultHwAddViewHelper;
import com.huawei.server.security.hsm.DefaultHwSystemManagerPluginEx;
import com.huawei.server.security.hsm.HwAddViewHelper;
import com.huawei.server.security.hsm.HwSystemManagerPluginEx;

public class HwServiceSecurityPartsFactoryExImpl extends HwServiceSecurityPartsFactoryEx {
    public DefaultHwSecurityServiceProxy getHwSecurityServiceProxy(Context context) {
        return new HwSecurityServiceProxy(context);
    }

    public DefaultBehaviorCollector getBehaviorCollector() {
        return BehaviorCollector.getInstance();
    }

    public DefaultHwAppAuthManager getHwAppAuthManager() {
        return HwAppAuthManager.getInstance();
    }

    public DefaultHwAddViewHelper getHwAddViewHelper(Context context) {
        return HwAddViewHelper.getInstance(context);
    }

    public DefaultHwSystemManagerPluginEx getHwSystemManagerPluginEx(Context context) {
        return HwSystemManagerPluginEx.getInstance(context);
    }

    public DefaultHwSfpService getHwSfpService() {
        return HwSfpServiceAdapter.getInstance();
    }
}
