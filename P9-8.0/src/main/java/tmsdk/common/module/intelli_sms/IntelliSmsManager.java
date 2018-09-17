package tmsdk.common.module.intelli_sms;

import android.content.Context;
import tmsdk.common.SmsEntity;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.f;
import tmsdkobf.ic;
import tmsdkobf.kt;
import tmsdkobf.mm;

public class IntelliSmsManager extends BaseManagerC {
    public static final String TAG = "TMSDK_IntelliSmsManager";
    private mm Ae;

    public IntelliSmsCheckResult checkSms(SmsEntity smsEntity, Boolean bool) {
        int i = 1;
        f.f(TAG, "checkSms");
        if (this.Ae == null) {
            return null;
        }
        tmsdk.common.module.aresengine.SmsEntity smsEntity2 = new tmsdk.common.module.aresengine.SmsEntity();
        smsEntity2.phonenum = smsEntity.phonenum;
        smsEntity2.body = smsEntity.body;
        MMatchSysResult a = this.Ae.a(smsEntity2, bool);
        if (smsEntity2.protocolType != 1) {
            i = MMatchSysResult.getSuggestion(a);
        }
        return new IntelliSmsCheckResult(i, a);
    }

    public synchronized void destroy() {
        if (this.Ae != null) {
            this.Ae.eU();
            this.Ae = null;
        }
    }

    public synchronized void init() {
        if (!ic.bE()) {
            if (this.Ae == null) {
                this.Ae = mm.eV();
            }
            this.Ae.eT();
        }
    }

    public boolean isPaySms(SmsEntity smsEntity) {
        boolean z = false;
        f.f(TAG, "isPaySms");
        if (this.Ae == null) {
            return false;
        }
        tmsdk.common.module.aresengine.SmsEntity smsEntity2 = new tmsdk.common.module.aresengine.SmsEntity();
        smsEntity2.phonenum = smsEntity.phonenum;
        smsEntity2.body = smsEntity.body;
        if (this.Ae.t(smsEntity2.phonenum, smsEntity2.body) != null) {
            z = true;
        }
        return z;
    }

    public void onCreate(Context context) {
        this.Ae = null;
        kt.saveActionData(1320031);
    }
}
