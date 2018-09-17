package tmsdk.common.module.intelli_sms;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdkobf.jg;
import tmsdkobf.nl;

/* compiled from: Unknown */
public class IntelliSmsManager extends BaseManagerC {
    private nl Cr;

    private MMatchSysResult a(SmsEntity smsEntity, Boolean bool) {
        if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
            smsEntity.protocolType = 0;
        }
        SmsCheckResult b = this.Cr.b(smsEntity, bool);
        if (b == null) {
            return new MMatchSysResult(1, 1, 0, 0, 1, null);
        }
        MMatchSysResult mMatchSysResult = new MMatchSysResult(b);
        mMatchSysResult.contentType = this.Cr.bL(mMatchSysResult.contentType);
        return mMatchSysResult;
    }

    public IntelliSmsCheckResult checkSms(tmsdk.common.SmsEntity smsEntity, Boolean bool) {
        int i = 1;
        if (jg.cl() || this.Cr == null) {
            return null;
        }
        SmsEntity smsEntity2 = new SmsEntity();
        smsEntity2.phonenum = smsEntity.phonenum;
        smsEntity2.body = smsEntity.body;
        MMatchSysResult a = a(smsEntity2, bool);
        if (smsEntity2.protocolType != 1) {
            i = MMatchSysResult.getSuggestion(a);
        }
        return new IntelliSmsCheckResult(i, a);
    }

    public synchronized void destroy() {
        if (!jg.cl()) {
            if (this.Cr != null) {
                this.Cr.fm();
                this.Cr = null;
            }
        }
    }

    public synchronized void init() {
        if (!jg.cl()) {
            if (this.Cr == null) {
                this.Cr = nl.fn();
            }
            this.Cr.fl();
        }
    }

    public boolean isPaySms(tmsdk.common.SmsEntity smsEntity) {
        boolean z = false;
        if (jg.cl() || this.Cr == null) {
            return false;
        }
        SmsEntity smsEntity2 = new SmsEntity();
        smsEntity2.phonenum = smsEntity.phonenum;
        smsEntity2.body = smsEntity.body;
        if (this.Cr.t(smsEntity2.phonenum, smsEntity2.body) != null) {
            z = true;
        }
        return z;
    }

    public void onCreate(Context context) {
        this.Cr = null;
    }
}
