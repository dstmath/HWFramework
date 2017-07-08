package tmsdkobf;

import android.content.Context;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.optimus.BsFakeType;
import tmsdk.common.module.optimus.Optimus;
import tmsdk.common.module.optimus.SMSCheckerResult;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsResult;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;

/* compiled from: Unknown */
public class nu implements tmsdkobf.ny.a {
    private static nu Dl;
    private String Dm;
    public Optimus Dn;
    private ny Do;
    private nx Dp;
    private Context mContext;

    /* compiled from: Unknown */
    public abstract class a {
        final /* synthetic */ nu Dq;
        public BsCloudResult Ds;
        protected CountDownLatch Dt;

        public a(nu nuVar, CountDownLatch countDownLatch) {
            this.Dq = nuVar;
            this.Dt = countDownLatch;
        }

        public abstract void a(BsCloudResult bsCloudResult);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.nu.1 */
    class AnonymousClass1 extends a {
        final /* synthetic */ nu Dq;

        AnonymousClass1(nu nuVar, CountDownLatch countDownLatch) {
            this.Dq = nuVar;
            super(nuVar, countDownLatch);
        }

        public void a(BsCloudResult bsCloudResult) {
            d.e("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync has result =" + bsCloudResult);
            this.Ds = bsCloudResult;
            this.Dt.countDown();
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.nu.2 */
    class AnonymousClass2 implements lg {
        final /* synthetic */ nu Dq;
        final /* synthetic */ a Dr;

        AnonymousClass2(nu nuVar, a aVar) {
            this.Dq = nuVar;
            this.Dr = aVar;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            this.Dq.Dp.cL("\u4e91\u7aef\u68c0\u6d4b\u7ed3\u679c:retCode=" + i3 + ",dataRetCode=" + i4 + ",resp=" + (fsVar != null ? fsVar.toString() : "null"));
            try {
                og ogVar = (og) fsVar;
                if (ogVar != null) {
                    if (ogVar.Ed != null) {
                        BsCloudResult a = oa.a(ogVar.Ed);
                        this.Dq.Dn.setBlackWhiteItems(oa.u(ogVar.Ee), oa.u(ogVar.Ef));
                        this.Dr.a(a);
                        return;
                    }
                }
                this.Dr.a(null);
            } catch (Throwable th) {
                this.Dr.a(null);
            }
        }
    }

    /* compiled from: Unknown */
    class b {
        final /* synthetic */ nu Dq;
        BsInput Du;
        int Dv;

        b(nu nuVar) {
            this.Dq = nuVar;
            this.Dv = 0;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.nu.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.nu.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.nu.<clinit>():void");
    }

    private nu(Context context) {
        this.mContext = context.getApplicationContext();
        this.Dp = new nx();
        this.Dn = new Optimus();
        this.Dm = ms.a(context, "fake_bs.dat", null);
    }

    private BsCloudResult a(b bVar) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        a anonymousClass1 = new AnonymousClass1(this, countDownLatch);
        d.e("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync start");
        a(bVar.Du, anonymousClass1);
        try {
            countDownLatch.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        d.e("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync timeout or notifyed");
        return anonymousClass1.Ds;
    }

    private void a(BsInput bsInput, a aVar) {
        fs obVar = new ob();
        obVar.DU = bsInput.sms;
        obVar.DT = bsInput.sender;
        obVar.DS = oa.t(this.Dn.getBsInfos(bsInput));
        jq.cu().a(812, obVar, new og(), 0, new AnonymousClass2(this, aVar), 3000);
    }

    public static nu q(Context context) {
        if (Dl == null) {
            synchronized (nu.class) {
                if (Dl == null) {
                    Dl = new nu(context);
                }
            }
        }
        return Dl;
    }

    public void a(BsInput bsInput) {
        BsResult bsResult = new BsResult();
        this.Dn.check(bsInput, bsResult);
        if (BsFakeType.FAKE == bsResult.fakeType) {
            nz.fD().s(System.currentTimeMillis());
            this.Dp.a("", "", "", bsInput.neighbors == null ? "" : bsInput.neighbors.toString(), this.Dn.getUploadInfo(), true, false);
        }
    }

    public SMSCheckerResult b(SmsEntity smsEntity, boolean z) {
        SMSCheckerResult sMSCheckerResult = new SMSCheckerResult();
        if (smsEntity == null) {
            return sMSCheckerResult;
        }
        BsInput bsInput = this.Do == null ? new BsInput() : this.Do.fC();
        bsInput.sender = smsEntity.phonenum;
        bsInput.sms = smsEntity.body;
        BsResult bsResult = new BsResult();
        this.Dn.check(bsInput, bsResult);
        this.Dp.cL("|\u672c\u5730\u68c0\u6d4b\u7ed3\u679c=" + bsResult.toString());
        sMSCheckerResult.mType = bsResult.fakeType;
        if (z && f.iu() && !f.iv()) {
            b bVar = new b(this);
            bVar.Du = bsInput;
            this.Dp.fy();
            BsCloudResult a = a(bVar);
            if (a != null) {
                this.Dn.checkWithCloud(bsInput, a, bsResult);
                sMSCheckerResult.isCloudCheck = true;
            }
        }
        this.Dp.cL("|\u6700\u7ec8\u7684\u68c0\u6d4b\u7ed3\u679c=" + bsResult.toString());
        if (BsFakeType.FAKE == bsResult.fakeType) {
            nz.fD().r(System.currentTimeMillis());
            this.Dp.a("", smsEntity.phonenum, smsEntity.body, bsInput.neighbors == null ? "" : bsInput.neighbors.toString(), this.Dn.getUploadInfo(), false, sMSCheckerResult.isCloudCheck);
        }
        sMSCheckerResult.mType = bsResult.fakeType;
        return sMSCheckerResult;
    }

    public boolean start() {
        if (!this.Dn.init(this.Dm, null)) {
            return false;
        }
        this.Dp.init();
        this.Do = new ny(this.Dp);
        this.Do.a((tmsdkobf.ny.a) this);
        this.Do.r(this.mContext);
        return true;
    }

    public void stop() {
        this.Dn.finish();
        if (this.Do != null) {
            this.Do.s(this.mContext);
            this.Do.a(null);
        }
        if (this.Dp != null) {
            this.Dp.destroy();
        }
        synchronized (nu.class) {
            Dl = null;
        }
    }
}
