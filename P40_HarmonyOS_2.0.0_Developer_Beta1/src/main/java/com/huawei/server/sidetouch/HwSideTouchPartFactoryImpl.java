package com.huawei.server.sidetouch;

import android.content.Context;
import com.huawei.server.DefaultHwBasicPlatformPartFactory;

public class HwSideTouchPartFactoryImpl extends DefaultHwBasicPlatformPartFactory {
    public HwSideTouchPolicy getHwSideTouchPolicyInstance(Context context) {
        return HwSideTouchPolicy.getInstance(context);
    }

    public HwDisplaySideRegionConfig getHwDisplaySideRegionConfigInstance() {
        return HwDisplaySideRegionConfig.getInstance();
    }
}
