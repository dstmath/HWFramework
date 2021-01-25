package android.rms;

import android.rms.resource.PidsResource;
import com.huawei.dfr.DefaultRMSFrameworkFactory;
import com.huawei.dfr.rms.DefaultHwSysResource;

public class RMSFrameworkFactory extends DefaultRMSFrameworkFactory {
    private static final String TAG = "RMSFrameworkFactory";

    public HwSysResource getHwSysResource(int typeId) {
        return HwSysResImpl.getResource(typeId);
    }

    public DefaultHwSysResource getPidsResource() {
        return PidsResource.getInstance();
    }
}
