package com.huawei.android.pushagent.model.c;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.a;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;

public class d implements e {
    public d(Context context) {
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.huawei.android.push.intent.HEARTBEAT_RANGE_CHANGE".equals(action) || "com.huawei.android.push.intent.HEARTBEAT_VALID_ARRIVED".equals(action)) {
            try {
                c.sg("PushLog2951", "when receive the heart beat range change or valid arrived,the file  HeartBeatCfg.xml need del");
                for (String action2 : a.hl(context).hs()) {
                    b.tx(context, action2);
                }
            } catch (Exception e) {
                c.sg("PushLog2951", "when receive the heart beat range change, files delete failed!");
            }
        }
    }
}
