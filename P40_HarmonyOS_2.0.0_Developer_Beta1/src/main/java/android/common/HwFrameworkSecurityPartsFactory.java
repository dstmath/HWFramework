package android.common;

import android.hsm.HwSystemManager;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import com.huawei.security.dpermission.DefaultDPermissionManagerImpl;
import com.huawei.security.dpermission.IDPermissionManager;
import huawei.android.security.DefaultHwInnerBehaviorCollectManager;
import huawei.android.security.HsmDefaultImpl;
import huawei.android.security.IHwBehaviorCollectManager;

@HwSystemApi
public class HwFrameworkSecurityPartsFactory {
    private static final String HW_FRAMEWORK_SECURITY_PARTS_FACTORY_IMPL_NAME = "huawei.android.security.HwFrameworkSecurityPartsFactoryImpl";
    private static final String TAG = "HwFrameworkSecurityPartsFactory";
    private static volatile HwFrameworkSecurityPartsFactory mFactory;

    @HwSystemApi
    public static HwFrameworkSecurityPartsFactory getInstance() {
        if (mFactory == null) {
            synchronized (HwFrameworkSecurityPartsFactory.class) {
                if (mFactory == null) {
                    mFactory = loadSecurityPartsFactory();
                    Log.i(TAG, "add HwFrameworkSecurityPartsFactory to memory.");
                }
            }
        }
        return mFactory;
    }

    private static HwFrameworkSecurityPartsFactory loadSecurityPartsFactory() {
        Object object = FactoryLoader.loadFactory(HW_FRAMEWORK_SECURITY_PARTS_FACTORY_IMPL_NAME);
        if (object != null && (object instanceof HwFrameworkSecurityPartsFactory)) {
            return (HwFrameworkSecurityPartsFactory) object;
        }
        Log.i(TAG, "HwSystemManagerFactoryImpl is null, need create HwFrameworkSecurityPartsFactory");
        return new HwFrameworkSecurityPartsFactory();
    }

    protected HwFrameworkSecurityPartsFactory() {
        Log.d(TAG, "HwFrameworkSecurityPartsFactory in.");
    }

    @HwSystemApi
    public IHwBehaviorCollectManager getInnerHwBehaviorCollectManager() {
        Log.d(TAG, "get DefaultHwInnerBehaviorCollectManager");
        return new DefaultHwInnerBehaviorCollectManager();
    }

    public HwSystemManager.HsmInterface getHwSystemManager() {
        return new HsmDefaultImpl();
    }

    public IDPermissionManager getDPermissionManager() {
        return new DefaultDPermissionManagerImpl();
    }
}
