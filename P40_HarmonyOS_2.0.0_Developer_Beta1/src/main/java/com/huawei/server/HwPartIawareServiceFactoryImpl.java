package com.huawei.server;

import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.BroadcastQueueEx;
import com.android.server.am.HwMtmBroadcastResourceManager;

public class HwPartIawareServiceFactoryImpl extends HwPartIawareServiceFactory {
    public AbsHwMtmBroadcastResourceManager getHwMtmBroadcastResourceManagerImpl(BroadcastQueueEx queue) {
        return new HwMtmBroadcastResourceManager(queue);
    }
}
