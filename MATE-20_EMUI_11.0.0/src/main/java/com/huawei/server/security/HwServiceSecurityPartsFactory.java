package com.huawei.server.security;

import android.util.Log;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.huawei.server.security.securityprofile.DefaultSecurityProfileControllerImpl;
import com.huawei.server.security.trustspace.DefaultTrustSpaceControllerImpl;

public class HwServiceSecurityPartsFactory {
    private static final String HW_SERVICE_SECURITY_PARTS_FACTORY_IMPL_NAME = "com.huawei.server.security.HwServiceSecurityPartsFactoryImpl";
    private static final Object LOCK = new Object();
    private static final String TAG = "HwServiceSecurityPartsFactory";
    private static volatile HwServiceSecurityPartsFactory instance;

    public static HwServiceSecurityPartsFactory getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = loadSecurityPartsFactory();
                }
            }
        }
        return instance;
    }

    private static HwServiceSecurityPartsFactory loadSecurityPartsFactory() {
        Object object = null;
        try {
            object = Class.forName(HW_SERVICE_SECURITY_PARTS_FACTORY_IMPL_NAME).newInstance();
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "loadFactory ClassNotFoundException");
        } catch (InstantiationException e2) {
            Log.e(TAG, "loadFactory InstantiationException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "loadFactory IllegalAccessException");
        } catch (Exception e4) {
            Log.e(TAG, "loadFactory exception");
        }
        if (object instanceof HwServiceSecurityPartsFactory) {
            return (HwServiceSecurityPartsFactory) object;
        }
        return new HwServiceSecurityPartsFactory();
    }

    public ITrustSpaceController getTrustSpaceController() {
        return new DefaultTrustSpaceControllerImpl();
    }

    public ISecurityProfileController getSecurityProfileController() {
        return new DefaultSecurityProfileControllerImpl();
    }
}
