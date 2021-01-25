package com.huawei.dfr;

import android.os.IBlockMonitor;
import android.zrhung.IAppEyeUiProbe;
import android.zrhung.IZrHung;
import com.huawei.dfr.zrhung.DefaultAppEyeFwkBlock;
import com.huawei.dfr.zrhung.DefaultAppEyeUiProbe;
import com.huawei.dfr.zrhung.DefaultBlockMonitor;
import com.huawei.dfr.zrhung.DefaultZrHungImpl;

public class DefaultZrHungFrameworkFactory {
    private static DefaultZrHungFrameworkFactory factory = null;

    public static synchronized DefaultZrHungFrameworkFactory getFactory() {
        DefaultZrHungFrameworkFactory defaultZrHungFrameworkFactory;
        synchronized (DefaultZrHungFrameworkFactory.class) {
            if (factory == null) {
                factory = new DefaultZrHungFrameworkFactory();
            }
            defaultZrHungFrameworkFactory = factory;
        }
        return defaultZrHungFrameworkFactory;
    }

    public IAppEyeUiProbe getAppEyeUiProbe() {
        return DefaultAppEyeUiProbe.getDefault();
    }

    public IBlockMonitor getBlockMonitor() {
        return DefaultBlockMonitor.getBlockMonitor();
    }

    public IZrHung getIZrHung(String wpName) {
        return DefaultZrHungImpl.getZrHung(wpName);
    }

    public DefaultZrHungImpl getAppEyeFwkBlock() {
        return DefaultAppEyeFwkBlock.getAppEyeFwkBlock();
    }
}
