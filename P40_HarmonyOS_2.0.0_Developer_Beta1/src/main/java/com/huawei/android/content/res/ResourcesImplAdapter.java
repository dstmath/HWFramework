package com.huawei.android.content.res;

import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ResourcesImplAdapter {
    private ResourcesImplAdapter() {
    }

    public static ResourcesImplAdapter getResourcesImplEx(Resources res) {
        if (res.getImpl() != null) {
            return new ResourcesImplAdapter();
        }
        return null;
    }

    public static AbsResourcesImpl getHwResourcesImpl(Resources res) {
        return res.getImpl().getHwResourcesImpl();
    }

    public static boolean isResourcesImplPreloading(Resources res) {
        return res.getImpl().mPreloading;
    }
}
