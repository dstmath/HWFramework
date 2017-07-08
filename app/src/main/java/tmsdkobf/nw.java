package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.optimus.BsFakeType;
import tmsdk.common.module.optimus.SMSCheckerResult;

/* compiled from: Unknown */
public class nw extends BaseManagerC {
    private nu Dx;
    private volatile boolean pk;
    private nl sf;

    public nw() {
        this.pk = false;
        this.Dx = null;
        this.sf = null;
    }

    private SMSCheckerResult c(SmsEntity smsEntity, Boolean bool) {
        if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
            smsEntity.protocolType = 0;
        }
        SmsCheckResult b = this.sf.b(smsEntity, bool);
        return (b != null && b.uiContentType == SmsCheckResult.ESCT_326) ? new SMSCheckerResult(BsFakeType.FAKE, b.sIsCloudResult) : null;
    }

    public SMSCheckerResult checkSms(tmsdk.common.SmsEntity smsEntity, boolean z) {
        if (!this.pk) {
            return null;
        }
        if (z) {
            ma.bx(1320001);
        } else {
            ma.bx(1320013);
        }
        SmsEntity smsEntity2 = new SmsEntity();
        smsEntity2.phonenum = smsEntity.phonenum;
        smsEntity2.body = smsEntity.body;
        SMSCheckerResult c = c(smsEntity2, Boolean.valueOf(z));
        return c == null ? this.Dx.b(smsEntity2, z) : c;
    }

    public long getFakeBSLastTime() {
        if (!this.pk) {
            return -1;
        }
        ma.bx(1320002);
        return nz.fD().fE();
    }

    public void onCreate(Context context) {
    }

    public synchronized boolean start() {
        if (this.pk) {
            stop();
        }
        if (this.Dx == null) {
            this.Dx = nu.q(TMSDKContext.getApplicaionContext());
            this.Dx.start();
        }
        if (this.sf == null) {
            this.sf = nl.fn();
            this.sf.fl();
        }
        this.pk = true;
        return true;
    }

    public synchronized void stop() {
        if (this.pk) {
            if (this.Dx != null) {
                this.Dx.stop();
                this.Dx = null;
            }
            if (this.sf != null) {
                this.sf.fm();
                this.sf = null;
            }
        }
        nz.stop();
        this.pk = false;
    }
}
