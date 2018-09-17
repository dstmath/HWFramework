package tmsdk.common.module.optimus;

import android.content.Context;
import tmsdk.common.SmsEntity;
import tmsdk.common.creator.BaseManagerC;
import tmsdkobf.jg;
import tmsdkobf.nw;

/* compiled from: Unknown */
public class OptimusManager extends BaseManagerC {
    private nw Dk;

    public SMSCheckerResult checkSms(SmsEntity smsEntity, boolean z) {
        return !jg.cl() ? this.Dk.checkSms(smsEntity, z) : null;
    }

    public long getFakeBSLastTime() {
        return !jg.cl() ? this.Dk.getFakeBSLastTime() : -1;
    }

    public final void onCreate(Context context) {
        this.Dk = new nw();
        this.Dk.onCreate(context);
    }

    public boolean start() {
        return !jg.cl() ? this.Dk.start() : false;
    }

    public void stop() {
        this.Dk.stop();
    }
}
