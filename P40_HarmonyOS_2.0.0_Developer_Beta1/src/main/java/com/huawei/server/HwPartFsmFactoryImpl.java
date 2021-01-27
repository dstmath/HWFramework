package com.huawei.server;

import android.content.Context;
import com.huawei.server.fsm.HwInwardFoldPolicy;
import com.huawei.server.fsm.HwOutwardFoldPolicy;
import com.huawei.server.fsm.PostureStateMachine;

public class HwPartFsmFactoryImpl extends HwPartFsmFactory {
    public HwInwardFoldPolicy getHwInwardFoldPolicy(Context context) {
        return HwInwardFoldPolicy.getInstance(context);
    }

    public HwOutwardFoldPolicy getHwOutwardFoldPolicy(Context context) {
        return HwOutwardFoldPolicy.getInstance(context);
    }

    public PostureStateMachine getPostureStateMachine() {
        return PostureStateMachine.getInstance();
    }
}
