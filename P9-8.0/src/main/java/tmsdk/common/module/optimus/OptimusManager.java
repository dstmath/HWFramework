package tmsdk.common.module.optimus;

import android.content.Context;
import tmsdk.common.SmsEntity;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.f;
import tmsdkobf.ic;
import tmsdkobf.kt;
import tmsdkobf.ms;

public class OptimusManager extends BaseManagerC {
    public static final String TAG = "TMSDK_OptimusManager";
    private ms AL;

    public SMSCheckerResult checkSms(SmsEntity smsEntity, boolean z) {
        f.f(TAG, "checkSms");
        return this.AL.checkSms(smsEntity, z);
    }

    public long getFakeBSLastTime() {
        return this.AL.getFakeBSLastTime();
    }

    public final void onCreate(Context context) {
        this.AL = new ms();
        this.AL.onCreate(context);
        kt.saveActionData(1320013);
    }

    public void setFakeBsListener(IFakeBaseStationListener iFakeBaseStationListener) {
        this.AL.setFakeBsListener(iFakeBaseStationListener);
    }

    public boolean start() {
        f.f(TAG, "start");
        return !ic.bE() ? this.AL.start() : false;
    }

    public void stop() {
        this.AL.stop();
    }
}
