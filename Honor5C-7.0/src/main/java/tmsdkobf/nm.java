package tmsdkobf;

import android.os.SystemClock;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.NativeBumblebee;
import tmsdk.common.module.intelli_sms.SmsCheckInput;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.IUpdateObserver;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.SpaceManager;

/* compiled from: Unknown */
public class nm {
    private NativeBumblebee CA;
    private long Cy;
    private HashMap<SmsCheckInput, Queue<b>> Cz;
    private IUpdateObserver vg;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.nm.1 */
    class AnonymousClass1 implements lg {
        final /* synthetic */ b CB;
        final /* synthetic */ nm CC;

        AnonymousClass1(nm nmVar, b bVar) {
            this.CC = nmVar;
            this.CB = bVar;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            d.e("BUMBLEBEE", "sendSmsToCloud onFinish");
            bt btVar = (bt) fsVar;
            d.e("BUMBLEBEE", "reqNO: " + i + " cmdId: " + i2 + " retCode: " + i3 + " result " + (fsVar != null ? fsVar.toString() : "null"));
            switch (i3) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    this.CC.a(this.CB, btVar);
                default:
            }
        }
    }

    /* compiled from: Unknown */
    private static class a implements nn {
        private SmsCheckResult CD;
        private CountDownLatch CE;

        public a(CountDownLatch countDownLatch) {
            this.CE = countDownLatch;
        }

        private SmsCheckResult fq() {
            return this.CD;
        }

        public int a(SmsCheckResult smsCheckResult) {
            d.e("BUMBLEBEE", "checkSmsCloudSync onResult " + (smsCheckResult == null ? "null" : smsCheckResult.toString()));
            this.CD = smsCheckResult;
            d.e("BUMBLEBEE", "checkSmsCloudSync onResult countDown");
            this.CE.countDown();
            d.e("BUMBLEBEE", "checkSmsCloudSync onResult countDown finish");
            return 0;
        }
    }

    /* compiled from: Unknown */
    private static class b {
        public nn CF;
        public SmsCheckInput CG;
        public bs CH;
        public ArrayList<a> CI;

        /* compiled from: Unknown */
        public static class a {
            public long CJ;
            public String name;
            public long time;
        }

        private b() {
            this.CI = new ArrayList();
        }
    }

    public nm() {
        this.vg = new IUpdateObserver() {
            final /* synthetic */ nm CC;

            {
                this.CC = r1;
            }

            public void onChanged(UpdateInfo updateInfo) {
                this.CC.reload();
            }
        };
        if (NativeBumblebee.isLoadNative()) {
            this.CA = new NativeBumblebee();
            int nativeInitSmsChecker_c = this.CA.nativeInitSmsChecker_c(0, fo());
            ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).addObserver(UpdateConfig.UPDATE_FLAG_POSEIDONV2, this.vg);
            d.e("BUMBLEBEE", "initSmsChecker " + nativeInitSmsChecker_c);
        }
        q(3000);
        this.Cz = new HashMap();
    }

    private void a(SmsCheckInput smsCheckInput, bs bsVar) {
        bsVar.sender = smsCheckInput.sender;
        bsVar.sms = smsCheckInput.sms;
        bsVar.uiCheckFlag = smsCheckInput.uiCheckFlag;
        bsVar.uiSmsInOut = smsCheckInput.uiSmsInOut;
        bsVar.uiSmsType = smsCheckInput.uiSmsType;
        bsVar.uiCheckType = smsCheckInput.uiCheckType;
        bsVar.dX = 0;
        bsVar.dY = null;
    }

    private void a(bt btVar, SmsCheckResult smsCheckResult) {
        smsCheckResult.uiFinalAction = btVar.uiFinalAction;
        smsCheckResult.uiContentType = btVar.uiContentType;
        smsCheckResult.uiMatchCnt = btVar.uiMatchCnt;
        smsCheckResult.fScore = btVar.fScore;
        smsCheckResult.uiActionReason = btVar.uiActionReason;
        if (btVar.stRuleTypeID != null) {
            Iterator it = btVar.stRuleTypeID.iterator();
            while (it.hasNext()) {
                bu buVar = (bu) it.next();
                smsCheckResult.addRuleTypeID(buVar.uiRuleType, buVar.uiRuleTypeId);
            }
        }
        smsCheckResult.sRule = btVar.sRule;
        smsCheckResult.uiShowRiskName = btVar.uiShowRiskName;
        smsCheckResult.sRiskClassify = btVar.sRiskClassify;
        smsCheckResult.sRiskUrl = btVar.sRiskUrl;
        smsCheckResult.sRiskName = btVar.sRiskName;
        smsCheckResult.sRiskReach = btVar.sRiskReach;
    }

    private void a(b bVar) {
        d.e("BUMBLEBEE", "sendSmsToCloud");
        a(bVar, "add");
        jq.cu().a(807, bVar.CH, new bt(), 0, new AnonymousClass1(this, bVar), 10000);
    }

    private void a(b bVar, String str) {
        a(bVar, str, 0);
    }

    private synchronized void a(b bVar, String str, long j) {
        a aVar = new a();
        aVar.name = str;
        aVar.time = SystemClock.elapsedRealtime();
        aVar.CJ = j;
        bVar.CI.add(aVar);
    }

    private void a(b bVar, bt btVar) {
        SmsCheckResult smsCheckResult = null;
        synchronized (this.Cz) {
            if (this.Cz.containsKey(bVar.CG)) {
                Queue<b> queue = (Queue) this.Cz.remove(bVar.CG);
                a(bVar, "cloud check finish", this.Cy);
                if (btVar != null) {
                    smsCheckResult = new SmsCheckResult();
                    a(btVar, smsCheckResult);
                }
                bVar.CF.a(smsCheckResult);
                a(bVar, "callback", 0);
                b(bVar);
                if (queue != null) {
                    for (b bVar2 : queue) {
                        bVar2.CF.a(smsCheckResult);
                    }
                }
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void b(b bVar) {
        d.e("BUMBLEBEE", "printLog ==================================================");
        d.e("BUMBLEBEE", "printLog |sender: " + bVar.CH.sender + " |sms: " + bVar.CH.sms + "|");
        long j = ((a) bVar.CI.get(0)).time;
        long j2 = -j;
        String str = "printLog %-20s[%d] duration[%06d] timeout[%b]";
        Iterator it = bVar.CI.iterator();
        while (true) {
            long j3 = j;
            if (it.hasNext()) {
                boolean z;
                a aVar = (a) it.next();
                Object[] objArr = new Object[4];
                objArr[0] = aVar.name;
                objArr[1] = Long.valueOf(aVar.time);
                objArr[2] = Long.valueOf(aVar.time - j3);
                if (aVar.CJ != 0) {
                    if ((aVar.time - j3 <= aVar.CJ ? 1 : null) == null) {
                        z = true;
                        objArr[3] = Boolean.valueOf(z);
                        d.e("BUMBLEBEE", String.format(str, objArr));
                        j = aVar.time;
                    }
                }
                z = false;
                objArr[3] = Boolean.valueOf(z);
                d.e("BUMBLEBEE", String.format(str, objArr));
                j = aVar.time;
            } else {
                d.e("BUMBLEBEE", "total[" + (((a) bVar.CI.get(bVar.CI.size() - 1)).time + j2) + "]");
            }
        }
    }

    private String fo() {
        String str = UpdateConfig.POSEIDONV2;
        ms.a(TMSDKContext.getApplicaionContext(), str, null);
        return ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).getFileSavePath() + File.separator + str;
    }

    public int a(SmsCheckInput smsCheckInput, SmsCheckResult smsCheckResult) {
        if (!NativeBumblebee.isLoadNative()) {
            return -4;
        }
        if (smsCheckInput == null || smsCheckResult == null) {
            return -5;
        }
        d.e("BUMBLEBEE", "checkSmsLocal IN Java: " + smsCheckInput.toString());
        int nativeCheckSms_c = this.CA.nativeCheckSms_c(smsCheckInput, smsCheckResult);
        d.e("BUMBLEBEE", "checkSmsLocal IN Java: ret = " + nativeCheckSms_c);
        return nativeCheckSms_c;
    }

    public SmsCheckResult a(SmsCheckInput smsCheckInput) {
        SmsCheckResult smsCheckResult = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        a aVar = new a(countDownLatch);
        a(smsCheckInput, (nn) aVar);
        try {
            d.e("BUMBLEBEE", "checkSmsCloudSync latch.await()");
            if (countDownLatch.await(this.Cy, TimeUnit.MILLISECONDS)) {
                d.e("BUMBLEBEE", "checkSmsCloudSync await true");
                smsCheckResult = aVar.fq();
            } else {
                d.e("BUMBLEBEE", "checkSmsCloudSync await false");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return smsCheckResult;
    }

    public void a(SmsCheckInput smsCheckInput, nn nnVar) {
        int i = 1;
        b bVar = new b();
        bVar.CF = nnVar;
        bVar.CG = smsCheckInput;
        bVar.CH = new bs();
        a(smsCheckInput, bVar.CH);
        if (NativeBumblebee.isLoadNative() && this.CA.nativeIsPrivateSms_c(smsCheckInput.sender, smsCheckInput.sms) == 1) {
            String nativeGetSmsInfo_c = this.CA.nativeGetSmsInfo_c(smsCheckInput.sender, smsCheckInput.sms);
            if (nativeGetSmsInfo_c == null) {
                nnVar.a(null);
                return;
            } else {
                bVar.CH.dX = 1;
                bVar.CH.sms = nativeGetSmsInfo_c;
            }
        }
        synchronized (this.Cz) {
            if (this.Cz.containsKey(bVar.CG)) {
                Queue queue = (Queue) this.Cz.get(bVar.CG);
                if (queue == null) {
                    queue = new LinkedList();
                }
                queue.add(bVar);
                this.Cz.put(bVar.CG, queue);
                i = 0;
            } else {
                this.Cz.put(bVar.CG, null);
            }
        }
        if (i != 0) {
            a(bVar);
        }
    }

    public void fp() {
        d.e("BUMBLEBEE", "Bumblebee stop()");
        if (NativeBumblebee.isLoadNative()) {
            ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(UpdateConfig.UPDATE_FLAG_POSEIDONV2);
            this.CA.nativeFinishSmsChecker_c();
        }
    }

    public int nativeCalcMap(int i) {
        return NativeBumblebee.isLoadNative() ? this.CA.nativeCalcMap_c(i) : 0;
    }

    public void q(long j) {
        if ((j >= 0 ? 1 : null) == null) {
            throw new IllegalArgumentException("CloudTimeout must >= 0 ");
        }
        this.Cy = j;
    }

    public void reload() {
        if (NativeBumblebee.isLoadNative()) {
            this.CA.nativeFinishSmsChecker_c();
            this.CA.nativeInitSmsChecker_c(0, fo());
        }
    }

    public SmsCheckResult u(String str, String str2) {
        SmsCheckInput smsCheckInput = new SmsCheckInput();
        smsCheckInput.sender = str;
        smsCheckInput.sms = str2;
        smsCheckInput.uiCheckType = 1;
        SmsCheckResult smsCheckResult = new SmsCheckResult();
        return (a(smsCheckInput, smsCheckResult) == 0 && smsCheckResult.uiFinalAction == 2 && smsCheckResult.uiContentType == SmsCheckResult.ESCT_PAY) ? smsCheckResult : null;
    }
}
