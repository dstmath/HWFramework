package com.huawei.server;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwPartMagicWindowServiceFactory {
    public static final String MW_PART_SVC_FACTORY_IMPL_NAME = "com.huawei.server.HwPartMagicWindowServiceFactoryImpl";
    private static final String TAG = "HWMW_HwPartMagicWindowServiceFactory";
    private static volatile HwPartMagicWindowServiceFactory sInstance = null;

    private HwPartMagicWindowServiceFactory() {
    }

    public static HwPartMagicWindowServiceFactory getInstance() {
        if (sInstance == null) {
            synchronized (HwPartMagicWindowServiceFactory.class) {
                if (sInstance == null) {
                    sInstance = new HwPartMagicWindowServiceFactory();
                }
            }
        }
        return sInstance;
    }

    public DefaultHwPartMagicWindowServiceFactoryImpl getHwPartMagicWindowServiceFactoryImpl() {
        DefaultHwPartMagicWindowServiceFactoryImpl factory = (DefaultHwPartMagicWindowServiceFactoryImpl) FactoryLoader.loadFactory(MW_PART_SVC_FACTORY_IMPL_NAME);
        if (factory == null) {
            return DefaultHwPartMagicWindowServiceFactoryImpl.getInstance();
        }
        return factory;
    }
}
