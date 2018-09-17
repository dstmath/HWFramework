package tmsdkobf;

import android.content.Context;
import com.qq.taf.jce.JceStruct;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.optimus.BsFakeType;
import tmsdk.common.module.optimus.IFakeBaseStationListener;
import tmsdk.common.module.optimus.Optimus;
import tmsdk.common.module.optimus.SMSCheckerResult;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;

public class mq implements tmsdkobf.mu.a {
    private static mq AM = null;
    private String AN;
    public Optimus AO = new Optimus();
    private mu AP;
    private mt AQ = new mt();
    private IFakeBaseStationListener AR;
    private Context mContext;

    public abstract class a {
        public BsCloudResult AU;
        protected CountDownLatch AV;

        public a(CountDownLatch countDownLatch) {
            this.AV = countDownLatch;
        }

        public abstract void a(BsCloudResult bsCloudResult);
    }

    class b {
        BsInput AW;
        int AX = 0;

        b() {
        }
    }

    private mq(Context context) {
        this.mContext = context.getApplicationContext();
        this.AN = lu.b(context, "fake_bs.dat", null);
    }

    private BsCloudResult a(b bVar) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        a anonymousClass1 = new a(countDownLatch) {
            public void a(BsCloudResult bsCloudResult) {
                f.d("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync has result =" + bsCloudResult);
                this.AU = bsCloudResult;
                this.AV.countDown();
            }
        };
        f.d("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync start");
        a(bVar.AW, anonymousClass1);
        try {
            countDownLatch.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        f.d("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync timeout or notifyed");
        return anonymousClass1.AU;
    }

    private void a(BsInput bsInput, final a aVar) {
        JceStruct mxVar = new mx();
        mxVar.Bw = bsInput.sms;
        mxVar.Bv = bsInput.sender;
        mxVar.Bu = mw.l(this.AO.getBsInfos(bsInput));
        im.bK().a(812, mxVar, new nc(), 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                try {
                    nc ncVar = (nc) jceStruct;
                    if (ncVar == null || ncVar.BF == null) {
                        aVar.a(null);
                        return;
                    }
                    BsCloudResult a = mw.a(ncVar.BF);
                    mq.this.AO.setBlackWhiteItems(mw.m(ncVar.BG), mw.m(ncVar.BH));
                    aVar.a(a);
                } catch (Throwable th) {
                    aVar.a(null);
                }
            }
        }, 3000);
    }

    public static mq t(Context context) {
        if (AM == null) {
            Class cls = mq.class;
            synchronized (mq.class) {
                if (AM == null) {
                    AM = new mq(context);
                }
            }
        }
        return AM;
    }

    public void a(BsInput bsInput) {
        this.AQ.bZ("基站信息发生了变化");
        BsResult bsResult = new BsResult();
        this.AO.check(bsInput, bsResult);
        if (BsFakeType.FAKE == bsResult.fakeType) {
            f.f("Optimus", "type is fake");
            mv.fj().w(System.currentTimeMillis());
            this.AQ.a("", "", "", bsInput.neighbors == null ? "" : bsInput.neighbors.toString(), this.AO.getUploadInfo(), true, false);
            if (this.AR != null) {
                f.f("Optimus", "onFakeNotify");
                this.AR.onFakeNotify(bsResult.fakeType);
            }
        } else if (BsFakeType.RIST == bsResult.fakeType) {
            f.f("Optimus", "type is risk");
            if (this.AR != null) {
                f.f("Optimus", "onFakeNotify");
                this.AR.onFakeNotify(bsResult.fakeType);
            }
        }
    }

    public SMSCheckerResult b(SmsEntity smsEntity, boolean z) {
        SMSCheckerResult sMSCheckerResult = new SMSCheckerResult();
        if (smsEntity == null) {
            return sMSCheckerResult;
        }
        BsInput bsInput = this.AP == null ? new BsInput() : this.AP.fi();
        bsInput.sender = smsEntity.phonenum;
        bsInput.sms = smsEntity.body;
        BsResult bsResult = new BsResult();
        f.f("Optimus", "check local");
        this.AO.check(bsInput, bsResult);
        sMSCheckerResult.mType = bsResult.fakeType;
        if (z && i.iE() && !i.iF()) {
            f.f("Optimus", "check cloud");
            b bVar = new b();
            bVar.AW = bsInput;
            this.AQ.fe();
            BsCloudResult a = a(bVar);
            if (a != null) {
                this.AO.checkWithCloud(bsInput, a, bsResult);
                sMSCheckerResult.isCloudCheck = true;
            }
        }
        f.f("Optimus", "final result is " + bsResult.toString());
        this.AQ.bZ("|最终的检测结果=" + bsResult.toString());
        if (BsFakeType.FAKE == bsResult.fakeType) {
            mv.fj().v(System.currentTimeMillis());
            this.AQ.a("", smsEntity.phonenum, smsEntity.body, bsInput.neighbors == null ? "" : bsInput.neighbors.toString(), this.AO.getUploadInfo(), false, sMSCheckerResult.isCloudCheck);
        }
        sMSCheckerResult.mType = bsResult.fakeType;
        return sMSCheckerResult;
    }

    public void setFakeBsListener(IFakeBaseStationListener iFakeBaseStationListener) {
        this.AR = iFakeBaseStationListener;
    }

    public boolean start() {
        if (!this.AO.init(this.AN, null)) {
            return false;
        }
        this.AQ.init();
        this.AP = new mu(this.AQ);
        this.AP.a((tmsdkobf.mu.a) this);
        this.AP.u(this.mContext);
        return true;
    }

    public void stop() {
        this.AO.finish();
        if (this.AP != null) {
            this.AP.v(this.mContext);
            this.AP.a(null);
        }
        if (this.AQ != null) {
            this.AQ.destroy();
        }
        Class cls = mq.class;
        synchronized (mq.class) {
            AM = null;
        }
    }
}
