package com.huawei.server;

import android.content.Context;
import android.util.Log;
import com.huawei.server.fsm.DefaultHwInwardFoldPolicy;
import com.huawei.server.fsm.DefaultHwOutwardFoldPolicy;
import com.huawei.server.fsm.DefaultPostureStateMachine;

public class HwPartFsmFactory {
    private static final String FACTORY_NAME = "com.huawei.server.HwPartFsmFactoryImpl";
    private static final String TAG = "HwPartFsmFactory";
    private static HwPartFsmFactory sFactory = null;

    public static synchronized HwPartFsmFactory loadFactory() {
        synchronized (HwPartFsmFactory.class) {
            if (sFactory != null) {
                return sFactory;
            }
            Object object = FactoryLoader.loadFactory(FACTORY_NAME);
            if (object == null || !(object instanceof HwPartFsmFactory)) {
                sFactory = new HwPartFsmFactory();
            } else {
                sFactory = (HwPartFsmFactory) object;
            }
            if (sFactory != null) {
                Log.i(TAG, "add com.huawei.server.HwPartFsmFactoryImpl to memory.");
                return sFactory;
            }
            throw new RuntimeException("can't load fsm factory");
        }
    }

    public DefaultHwInwardFoldPolicy getHwInwardFoldPolicy(Context context) {
        return DefaultHwInwardFoldPolicy.getInstance(context);
    }

    public DefaultHwOutwardFoldPolicy getHwOutwardFoldPolicy(Context context) {
        return DefaultHwOutwardFoldPolicy.getInstance(context);
    }

    public DefaultPostureStateMachine getPostureStateMachine() {
        return DefaultPostureStateMachine.getInstance();
    }
}
