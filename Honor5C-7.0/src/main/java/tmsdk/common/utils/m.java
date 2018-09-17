package tmsdk.common.utils;

import tmsdk.common.TMSDKContext;
import tmsdkobf.jq;
import tmsdkobf.mi;

/* compiled from: Unknown */
public class m {
    public static void wakeup() {
        if (jq.getTmsliteSwitch()) {
            mi.m(TMSDKContext.getApplicaionContext());
        } else {
            d.e("TmsliteUtil", "getTmsliteSwitch off");
        }
    }
}
