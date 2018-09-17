package tmsdkobf;

import android.os.SystemClock;
import com.qq.taf.jce.JceStruct;
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
import tmsdk.common.utils.f;

public class mn {
    private long Al;
    private HashMap<SmsCheckInput, Queue<b>> Am;
    private NativeBumblebee An;
    private IUpdateObserver vD = new IUpdateObserver() {
        public void onChanged(UpdateInfo updateInfo) {
            mn.this.reload();
        }
    };

    private static class a implements mo {
        private SmsCheckResult Aq;
        private CountDownLatch Ar;

        public a(CountDownLatch countDownLatch) {
            this.Ar = countDownLatch;
        }

        private SmsCheckResult eY() {
            return this.Aq;
        }

        public int a(SmsCheckResult smsCheckResult) {
            f.d("BUMBLEBEE", "checkSmsCloudSync onResult " + (smsCheckResult == null ? "null" : smsCheckResult.toString()));
            this.Aq = smsCheckResult;
            f.d("BUMBLEBEE", "checkSmsCloudSync onResult countDown");
            this.Ar.countDown();
            f.d("BUMBLEBEE", "checkSmsCloudSync onResult countDown finish");
            return 0;
        }
    }

    private static class b {
        public mo As;
        public SmsCheckInput At;
        public cg Au;
        public ArrayList<a> Av;

        public static class a {
            public long Aw;
            public String name;
            public long time;
        }

        private b() {
            this.Av = new ArrayList();
        }

        /* synthetic */ b(AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    public mn() {
        if (NativeBumblebee.isLoadNative()) {
            this.An = new NativeBumblebee();
            int nativeInitSmsChecker_c = this.An.nativeInitSmsChecker_c(0, eW());
            ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).addObserver(UpdateConfig.UPDATE_FLAG_POSEIDONV2, this.vD);
            f.d("BUMBLEBEE", "initSmsChecker " + nativeInitSmsChecker_c);
        }
        u(3000);
        this.Am = new HashMap();
    }

    private void a(SmsCheckInput smsCheckInput, cg cgVar) {
        cgVar.sender = smsCheckInput.sender;
        cgVar.sms = smsCheckInput.sms;
        cgVar.uiCheckFlag = smsCheckInput.uiCheckFlag;
        cgVar.uiSmsInOut = smsCheckInput.uiSmsInOut;
        cgVar.uiSmsType = smsCheckInput.uiSmsType;
        cgVar.uiCheckType = smsCheckInput.uiCheckType;
        cgVar.eS = 0;
        cgVar.eT = null;
    }

    private void a(ch chVar, SmsCheckResult smsCheckResult) {
        smsCheckResult.uiFinalAction = chVar.uiFinalAction;
        smsCheckResult.uiContentType = chVar.uiContentType;
        smsCheckResult.uiMatchCnt = chVar.uiMatchCnt;
        smsCheckResult.fScore = chVar.fScore;
        smsCheckResult.uiActionReason = chVar.uiActionReason;
        if (chVar.stRuleTypeID != null) {
            Iterator it = chVar.stRuleTypeID.iterator();
            while (it.hasNext()) {
                ci ciVar = (ci) it.next();
                smsCheckResult.addRuleTypeID(ciVar.uiRuleType, ciVar.uiRuleTypeId);
            }
        }
        smsCheckResult.sRule = chVar.sRule;
        smsCheckResult.uiShowRiskName = chVar.uiShowRiskName;
        smsCheckResult.sRiskClassify = chVar.sRiskClassify;
        smsCheckResult.sRiskUrl = chVar.sRiskUrl;
        smsCheckResult.sRiskName = chVar.sRiskName;
        smsCheckResult.sRiskReach = chVar.sRiskReach;
    }

    private void a(final b bVar) {
        f.d("BUMBLEBEE", "sendSmsToCloud");
        a(bVar, "add");
        im.bK().a(807, bVar.Au, new ch(), 0, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                f.d("BUMBLEBEE", "sendSmsToCloud onFinish");
                ch chVar = (ch) jceStruct;
                f.d("BUMBLEBEE", "reqNO: " + i + " cmdId: " + i2 + " retCode: " + i3 + " result " + (jceStruct != null ? jceStruct.toString() : "null"));
                switch (i3) {
                    case 0:
                        mn.this.a(bVar, chVar);
                        return;
                    default:
                        return;
                }
            }
        }, 10000);
    }

    private void a(b bVar, String str) {
        a(bVar, str, 0);
    }

    private synchronized void a(b bVar, String str, long j) {
        a aVar = new a();
        aVar.name = str;
        aVar.time = SystemClock.elapsedRealtime();
        aVar.Aw = j;
        bVar.Av.add(aVar);
    }

    /* JADX WARNING: Missing block: B:9:0x001a, code:
            a(r9, "cloud check finish", r8.Al);
            r1 = null;
     */
    /* JADX WARNING: Missing block: B:10:0x0023, code:
            if (r10 != null) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:11:0x0025, code:
            r9.As.a(r1);
            a(r9, "callback", 0);
            b(r9);
     */
    /* JADX WARNING: Missing block: B:12:0x0035, code:
            if (r0 != null) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:13:0x0037, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x003b, code:
            r1 = new tmsdk.common.module.intelli_sms.SmsCheckResult();
            a(r10, r1);
     */
    /* JADX WARNING: Missing block: B:19:0x0044, code:
            r2 = r0.iterator();
     */
    /* JADX WARNING: Missing block: B:21:0x004c, code:
            if (r2.hasNext() == false) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:22:0x004e, code:
            ((tmsdkobf.mn.b) r2.next()).As.a(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(b bVar, ch chVar) {
        synchronized (this.Am) {
            if (this.Am.containsKey(bVar.At)) {
                Queue queue = (Queue) this.Am.remove(bVar.At);
            }
        }
    }

    private synchronized void b(b bVar) {
        f.d("BUMBLEBEE", "printLog ==================================================");
        f.d("BUMBLEBEE", "printLog |sender: " + bVar.Au.sender + " |sms: " + bVar.Au.sms + "|");
        long j = ((a) bVar.Av.get(0)).time;
        long j2 = -j;
        String str = "printLog %-20s[%d] duration[%06d] timeout[%b]";
        Iterator it = bVar.Av.iterator();
        while (it.hasNext()) {
            boolean z;
            a aVar = (a) it.next();
            Object[] objArr = new Object[4];
            objArr[0] = aVar.name;
            objArr[1] = Long.valueOf(aVar.time);
            objArr[2] = Long.valueOf(aVar.time - j);
            if (aVar.Aw != 0) {
                if ((aVar.time - j <= aVar.Aw ? 1 : null) == null) {
                    z = true;
                    objArr[3] = Boolean.valueOf(z);
                    f.d("BUMBLEBEE", String.format(str, objArr));
                    j = aVar.time;
                }
            }
            z = false;
            objArr[3] = Boolean.valueOf(z);
            f.d("BUMBLEBEE", String.format(str, objArr));
            j = aVar.time;
        }
        f.d("BUMBLEBEE", "total[" + (j2 + ((a) bVar.Av.get(bVar.Av.size() - 1)).time) + "]");
    }

    private String eW() {
        String str = UpdateConfig.POSEIDONV2;
        lu.b(TMSDKContext.getApplicaionContext(), str, null);
        return ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).getFileSavePath() + File.separator + str;
    }

    public int a(SmsCheckInput smsCheckInput, SmsCheckResult smsCheckResult) {
        if (!NativeBumblebee.isLoadNative()) {
            return -4;
        }
        if (smsCheckInput == null || smsCheckResult == null) {
            return -5;
        }
        f.d("BUMBLEBEE", "checkSmsLocal IN Java: " + smsCheckInput.toString());
        int nativeCheckSms_c = this.An.nativeCheckSms_c(smsCheckInput, smsCheckResult);
        f.d("BUMBLEBEE", "checkSmsLocal IN Java: ret = " + nativeCheckSms_c);
        return nativeCheckSms_c;
    }

    public SmsCheckResult a(SmsCheckInput smsCheckInput) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        a aVar = new a(countDownLatch);
        a(smsCheckInput, (mo) aVar);
        try {
            f.d("BUMBLEBEE", "checkSmsCloudSync latch.await()");
            if (countDownLatch.await(this.Al, TimeUnit.MILLISECONDS)) {
                f.d("BUMBLEBEE", "checkSmsCloudSync await true");
                return aVar.eY();
            }
            f.d("BUMBLEBEE", "checkSmsCloudSync await false");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void a(SmsCheckInput smsCheckInput, mo moVar) {
        Object obj;
        b bVar = new b();
        bVar.As = moVar;
        bVar.At = smsCheckInput;
        bVar.Au = new cg();
        a(smsCheckInput, bVar.Au);
        if (NativeBumblebee.isLoadNative() && this.An.nativeIsPrivateSms_c(smsCheckInput.sender, smsCheckInput.sms) == 1) {
            String nativeGetSmsInfo_c = this.An.nativeGetSmsInfo_c(smsCheckInput.sender, smsCheckInput.sms);
            if (nativeGetSmsInfo_c == null) {
                moVar.a(null);
                return;
            } else {
                bVar.Au.eS = 1;
                bVar.Au.sms = nativeGetSmsInfo_c;
            }
        }
        synchronized (this.Am) {
            if (this.Am.containsKey(bVar.At)) {
                Queue queue = (Queue) this.Am.get(bVar.At);
                if (queue == null) {
                    queue = new LinkedList();
                }
                queue.add(bVar);
                this.Am.put(bVar.At, queue);
                obj = null;
            } else {
                this.Am.put(bVar.At, null);
                obj = 1;
            }
        }
        if (obj != null) {
            a(bVar);
        }
    }

    public void eX() {
        f.d("BUMBLEBEE", "Bumblebee stop()");
        if (NativeBumblebee.isLoadNative()) {
            ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(UpdateConfig.UPDATE_FLAG_POSEIDONV2);
            this.An.nativeFinishSmsChecker_c();
        }
    }

    public int nativeCalcMap(int i) {
        return NativeBumblebee.isLoadNative() ? this.An.nativeCalcMap_c(i) : 0;
    }

    public void reload() {
        if (NativeBumblebee.isLoadNative()) {
            this.An.nativeFinishSmsChecker_c();
            this.An.nativeInitSmsChecker_c(0, eW());
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

    public void u(long j) {
        if ((j >= 0 ? 1 : null) == null) {
            throw new IllegalArgumentException("CloudTimeout must >= 0 ");
        }
        this.Al = j;
    }
}
