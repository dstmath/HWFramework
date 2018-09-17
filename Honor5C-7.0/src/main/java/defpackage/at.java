package defpackage;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.ChannelMgr;

/* renamed from: at */
public class at extends o {
    public at(Context context) {
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.huawei.android.push.intent.HEARTBEAT_RANGE_CHANGE".equals(action) || "com.huawei.android.push.intent.HEARTBEAT_VALID_ARRIVED".equals(action)) {
            try {
                aw.d("PushLog2828", "when receive the heart beat range change or valid arrived,the file  HeartBeatCfg.xml need del");
                for (String action2 : ChannelMgr.g(context).aS()) {
                    au.n(context, action2);
                }
            } catch (Exception e) {
                aw.d("PushLog2828", "when receive the heart beat range change, files delete failed!");
            }
        }
    }
}
