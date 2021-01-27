package com.huawei.server.security;

import android.content.Context;
import com.huawei.server.FactoryLoader;
import com.huawei.server.security.behaviorcollect.DefaultBehaviorCollector;
import com.huawei.server.security.fileprotect.DefaultHwAppAuthManager;
import com.huawei.server.security.fileprotect.DefaultHwSfpService;
import com.huawei.server.security.hsm.DefaultHwAddViewHelper;
import com.huawei.server.security.hsm.DefaultHwSystemManagerPluginEx;

public class HwServiceSecurityPartsFactoryEx {
    private static final String HW_SERVICE_SECURITY_PARTS_FACTORYEX_IMPL_NAME = "com.huawei.server.security.HwServiceSecurityPartsFactoryExImpl";
    private static final Object LOCK = new Object();
    private static volatile HwServiceSecurityPartsFactoryEx instance;

    public static HwServiceSecurityPartsFactoryEx getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = loadSecurityPartsFactory();
                }
            }
        }
        return instance;
    }

    private static HwServiceSecurityPartsFactoryEx loadSecurityPartsFactory() {
        Object object = FactoryLoader.loadFactory(HW_SERVICE_SECURITY_PARTS_FACTORYEX_IMPL_NAME);
        if (object instanceof HwServiceSecurityPartsFactoryEx) {
            return (HwServiceSecurityPartsFactoryEx) object;
        }
        return new HwServiceSecurityPartsFactoryEx();
    }

    public DefaultHwSecurityServiceProxy getHwSecurityServiceProxy(Context context) {
        return new DefaultHwSecurityServiceProxy(context);
    }

    public DefaultBehaviorCollector getBehaviorCollector() {
        return DefaultBehaviorCollector.getInstance();
    }

    public DefaultHwAddViewHelper getHwAddViewHelper(Context context) {
        return DefaultHwAddViewHelper.getInstance(context);
    }

    public DefaultHwSystemManagerPluginEx getHwSystemManagerPluginEx(Context context) {
        return DefaultHwSystemManagerPluginEx.getInstance(context);
    }

    public DefaultHwAppAuthManager getHwAppAuthManager() {
        return DefaultHwAppAuthManager.getInstance();
    }

    public DefaultHwSfpService getHwSfpService() {
        return DefaultHwSfpService.getInstance();
    }
}
