package com.huawei.dfr.rms.resource;

import android.util.Log;
import com.huawei.dfr.rms.DefaultHwSysResource;

public class DefaultPidsResource extends DefaultHwSysResource {
    private static final String TAG = "DefaultPidsResource";
    private static volatile DefaultPidsResource resource;

    public static synchronized DefaultPidsResource getPidsResource() {
        DefaultPidsResource defaultPidsResource;
        synchronized (DefaultPidsResource.class) {
            if (resource == null) {
                resource = new DefaultPidsResource();
            }
            defaultPidsResource = resource;
        }
        return defaultPidsResource;
    }

    @Override // com.huawei.dfr.rms.DefaultHwSysResource
    public void init(String[] args) {
        Log.i(TAG, "DefaultPidsResource init");
    }

    @Override // com.huawei.dfr.rms.DefaultHwSysResource
    public int acquire(int tgid, String pkg, int processTpye) {
        Log.i(TAG, "DefaultPidsResource acquire");
        return -1;
    }
}
