package com.huawei.server;

public class HwPCFactory {
    private static final Object LOCK = new Object();
    private static final String TAG = "HwPCFactory";
    private static volatile HwPCFactory obj = null;

    private HwPCFactory() {
    }

    public static HwPCFactory getHwPCFactory() {
        HwPCFactory hwPCFactory;
        synchronized (LOCK) {
            if (obj == null) {
                obj = new HwPCFactory();
            }
            hwPCFactory = obj;
        }
        return hwPCFactory;
    }

    public DefaultHwPCFactoryImpl getHwPCFactoryImpl() {
        DefaultHwPCFactoryImpl factory = (DefaultHwPCFactoryImpl) FactoryLoader.loadFactory("com.huawei.server.HwPCFactoryImpl");
        if (factory == null) {
            return DefaultHwPCFactoryImpl.getInstance();
        }
        return factory;
    }
}
