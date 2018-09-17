package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.optimus.BsFakeType;
import tmsdk.common.module.optimus.IFakeBaseStationListener;
import tmsdk.common.module.optimus.SMSCheckerResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.s;

public class ms extends BaseManagerC {
    private mq AZ = null;
    private volatile boolean Ac = false;
    private mm pG = null;

    private SMSCheckerResult c(SmsEntity smsEntity, Boolean bool) {
        if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
            smsEntity.protocolType = 0;
        }
        SmsCheckResult b = this.pG.b(smsEntity, bool);
        return (b != null && b.uiContentType == SmsCheckResult.ESCT_326) ? new SMSCheckerResult(BsFakeType.FAKE, b.sIsCloudResult) : null;
    }

    public SMSCheckerResult checkSms(tmsdk.common.SmsEntity smsEntity, boolean z) {
        if (this.Ac) {
            if (z) {
                kt.aE(1320001);
            }
            SmsEntity smsEntity2 = new SmsEntity();
            smsEntity2.phonenum = smsEntity.phonenum;
            smsEntity2.body = smsEntity.body;
            SMSCheckerResult c = c(smsEntity2, Boolean.valueOf(z));
            if (c == null) {
                return this.AZ.b(smsEntity2, z);
            }
            f.f("Optimus", "intelli_fake, return");
            return c;
        }
        f.f("Optimus", "not inited");
        return null;
    }

    public long getFakeBSLastTime() {
        if (!this.Ac) {
            return -1;
        }
        kt.saveActionData(1320002);
        return mv.fj().fk();
    }

    public void onCreate(Context context) {
    }

    public void setFakeBsListener(IFakeBaseStationListener iFakeBaseStationListener) {
        if (this.AZ != null) {
            this.AZ.setFakeBsListener(iFakeBaseStationListener);
        }
    }

    public synchronized boolean start() {
        s.bW(IncomingSmsFilterConsts.PAY_SMS);
        if (this.Ac) {
            stop();
        }
        if (this.AZ == null) {
            this.AZ = mq.t(TMSDKContext.getApplicaionContext());
            this.AZ.start();
            f.d("jiejie-optimus", "isInited 2 is " + this.Ac);
        }
        if (this.pG == null) {
            this.pG = mm.eV();
            this.pG.eT();
        }
        this.Ac = true;
        return true;
    }

    public synchronized void stop() {
        if (this.Ac) {
            if (this.AZ != null) {
                this.AZ.stop();
                this.AZ = null;
            }
            if (this.pG != null) {
                this.pG.eU();
                this.pG = null;
            }
        }
        mv.stop();
        this.Ac = false;
    }
}
