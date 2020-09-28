package huawei.android.security;

import android.common.HwFrameworkSecurityPartsFactory;
import android.hsm.HwSystemManager;
import com.huawei.hsm.HsmInterfaceImpl;
import com.huawei.security.dpermission.DistributedPermissionManagerImpl;
import com.huawei.security.dpermission.IDPermissionManager;

public class HwFrameworkSecurityPartsFactoryImpl extends HwFrameworkSecurityPartsFactory {
    public IHwBehaviorCollectManager getInnerHwBehaviorCollectManager() {
        return HwBehaviorCollectManagerImpl.getDefault();
    }

    public HwSystemManager.HsmInterface getHwSystemManager() {
        return HsmInterfaceImpl.getDefault();
    }

    public IDPermissionManager getDPermissionManager() {
        return DistributedPermissionManagerImpl.getInnerServiceImpl();
    }
}
