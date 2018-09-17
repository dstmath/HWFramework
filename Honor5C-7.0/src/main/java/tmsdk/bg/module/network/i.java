package tmsdk.bg.module.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.tcc.TrafficSmsParser;
import tmsdk.common.tcc.TrafficSmsParser.MatchRule;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.h;
import tmsdk.common.utils.l;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.ar;
import tmsdkobf.as;
import tmsdkobf.at;
import tmsdkobf.au;
import tmsdkobf.av;
import tmsdkobf.aw;
import tmsdkobf.ax;
import tmsdkobf.ay;
import tmsdkobf.az;
import tmsdkobf.ba;
import tmsdkobf.bc;
import tmsdkobf.bd;
import tmsdkobf.be;
import tmsdkobf.bf;
import tmsdkobf.bg;
import tmsdkobf.bi;
import tmsdkobf.cz;
import tmsdkobf.fq;
import tmsdkobf.fs;
import tmsdkobf.jq;
import tmsdkobf.ks;
import tmsdkobf.ku;
import tmsdkobf.lg;
import tmsdkobf.li;
import tmsdkobf.lw;
import tmsdkobf.lx;
import tmsdkobf.mu;
import tmsdkobf.pf;
import tmsdkobf.pl;

/* compiled from: Unknown */
class i extends BaseManagerB {
    private Context mContext;
    li oy;
    public final int yI;
    public final int yJ;
    public final int yK;
    public final int yL;
    public final int yM;
    public final int yN;
    Handler yO;
    private pf yP;
    private ITrafficCorrectionListener yQ;
    private int yR;
    private int yS;

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.network.i.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ i yT;

        AnonymousClass1(i iVar, Looper looper) {
            this.yT = iVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 4097) {
                a aVar = (a) message.obj;
                if (this.yT.yQ != null && aVar != null) {
                    d.e("TrafficCorrection", "onTrafficInfoNotify--simIndex:[" + aVar.zk + "]trafficClass:[" + aVar.zl + "]" + "]subClass:[" + aVar.zm + "]" + "]kBytes:[" + aVar.zn + "]");
                    this.yT.yQ.onTrafficInfoNotify(aVar.zk, aVar.zl, aVar.zm, aVar.zn);
                }
            } else if (message.what != 4098) {
                if (message.what == 4099) {
                    ku.dq().b((ks) message.obj);
                } else if (message.what == 4100) {
                    if (TextUtils.isEmpty((String) message.obj)) {
                        d.e("TrafficCorrection", "onError--simIndex:[" + message.arg1 + "]ERR_CORRECTION_PROFILE_UPLOAD_FAIL");
                        if (this.yT.yQ != null) {
                            this.yT.yQ.onError(message.arg1, ErrorCode.ERR_CORRECTION_PROFILE_UPLOAD_FAIL);
                        }
                    }
                    new j(message.arg1).ch((String) message.obj);
                } else if (message.what != 4101) {
                    if (message.what == 4102 && this.yT.yQ != null) {
                        d.e("TrafficCorrection", "onProfileNotify--simIndex:[" + message.arg1 + "]ProfileInfo:[" + ((ProfileInfo) message.obj).toString() + "]");
                        this.yT.yQ.onProfileNotify(message.arg1, (ProfileInfo) message.obj);
                    }
                } else if (this.yT.yQ != null) {
                    d.e("TrafficCorrection", "onError--simIndex:[" + message.arg1 + "]errorCode:[" + message.arg2 + "]");
                    this.yT.yQ.onError(message.arg1, message.arg2);
                }
            } else if (this.yT.yQ != null) {
                j jVar = new j(message.arg1);
                String ed = jVar.ed();
                String ee = jVar.ee();
                d.e("TrafficCorrection", "onNeedSmsCorrection--simIndex:[" + message.arg1 + "]queryCode:[" + ed + "]queryPort:[" + ee + "]");
                this.yT.yQ.onNeedSmsCorrection(message.arg1, ed, ee);
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.network.i.3 */
    class AnonymousClass3 implements lg {
        final /* synthetic */ i yT;
        final /* synthetic */ int yZ;
        final /* synthetic */ String za;
        final /* synthetic */ String zb;

        AnonymousClass3(i iVar, int i, String str, String str2) {
            this.yT = iVar;
            this.yZ = i;
            this.za = str;
            this.zb = str2;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            if (i3 != 0) {
                d.e("TrafficCorrection", "\u6709\u7f51\u7edc\uff0c\u4e0a\u62a5\u77ed\u4fe1\u6d41\u7a0b\u5931\u8d25\uff0c\u8d70\u672c\u5730\u77ed\u4fe1\u5206\u6790\u6d41\u7a0b");
                this.yT.b(this.yZ, this.za, this.zb);
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.network.i.4 */
    class AnonymousClass4 implements ks {
        final /* synthetic */ i yT;
        final /* synthetic */ int zc;
        final /* synthetic */ String zd;
        final /* synthetic */ String ze;
        final /* synthetic */ String zf;
        final /* synthetic */ String zg;
        final /* synthetic */ String zh;
        final /* synthetic */ int zi;

        AnonymousClass4(i iVar, int i, String str, String str2, String str3, String str4, String str5, int i2) {
            this.yT = iVar;
            this.zc = i;
            this.zd = str;
            this.ze = str2;
            this.zf = str3;
            this.zg = str4;
            this.zh = str5;
            this.zi = i2;
        }

        public void a(ArrayList<fs> arrayList, int i) {
            this.yT.yO.sendMessage(this.yT.yO.obtainMessage(4099, this.zc, 0, this));
            Object obj = "";
            if (i == 0) {
                obj = this.yT.yP.c() + "$" + this.zd + "$" + this.ze + this.zf + this.zg + this.zh + "$" + this.zi;
            }
            this.yT.yO.sendMessage(this.yT.yO.obtainMessage(4100, this.zc, 0, obj));
            d.e("TrafficCorrection", "profile\u4e0a\u62a5\u7ed3\u679c[" + i + "]guid:[" + this.yT.yP.c() + "]");
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.network.i.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ i yT;
        final /* synthetic */ int yZ;
        final /* synthetic */ lx zj;

        AnonymousClass5(i iVar, int i, lx lxVar) {
            this.yT = iVar;
            this.yZ = i;
            this.zj = lxVar;
        }

        public void run() {
            this.yT.a(this.yZ, this.zj.bA, this.zj.url);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.network.i.6 */
    class AnonymousClass6 implements lg {
        final /* synthetic */ i yT;
        final /* synthetic */ int zc;

        AnonymousClass6(i iVar, int i) {
            this.yT = iVar;
            this.zc = i;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            if (i3 != 0) {
                this.yT.l(this.zc, ErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL);
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.network.i.7 */
    class AnonymousClass7 implements lg {
        final /* synthetic */ i yT;
        final /* synthetic */ int zc;

        AnonymousClass7(i iVar, int i) {
            this.yT = iVar;
            this.zc = i;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            if (i3 != 0) {
                this.yT.l(this.zc, ErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL);
            }
        }
    }

    /* compiled from: Unknown */
    class a {
        final /* synthetic */ i yT;
        int zk;
        int zl;
        int zm;
        int zn;

        a(i iVar) {
            this.yT = iVar;
        }
    }

    i() {
        this.yI = 4097;
        this.yJ = 4098;
        this.yK = 4099;
        this.yL = 4100;
        this.yM = 4101;
        this.yN = 4102;
        this.yQ = null;
        this.yR = 2;
        this.yS = 3;
        this.oy = new li() {
            final /* synthetic */ i yT;

            /* compiled from: Unknown */
            /* renamed from: tmsdk.bg.module.network.i.2.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ int yU;
                final /* synthetic */ int yV;
                final /* synthetic */ long yW;
                final /* synthetic */ fs yX;
                final /* synthetic */ AnonymousClass2 yY;

                AnonymousClass1(AnonymousClass2 anonymousClass2, int i, int i2, long j, fs fsVar) {
                    this.yY = anonymousClass2;
                    this.yU = i;
                    this.yV = i2;
                    this.yW = j;
                    this.yX = fsVar;
                }

                public void run() {
                    lw lwVar = new lw();
                    lwVar.H = this.yU;
                    lwVar.dG = this.yV;
                    lwVar.dF = this.yW;
                    lwVar.ol = this.yX;
                    ba baVar = (ba) lwVar.ol;
                    String str = baVar.imsi;
                    int i = baVar.bC;
                    d.e("TrafficCorrection", "[\u6267\u884cpush\u6d88\u606f]--\u4e2a\u6570:[" + baVar.bW.size() + "]--cloudimsi:[" + str + "]\u5361\u69fd:[" + i + "]seqNo:[" + lwVar.dG + "]pushId:[" + lwVar.dF + "]");
                    int i2 = (i == 0 || i == 1) ? i : 0;
                    String a = this.yY.yT.bn(i2);
                    if (str == null) {
                        str = "";
                    }
                    String str2 = str;
                    if (a == null) {
                        a = "";
                    }
                    boolean z = ("".equals(str2) || "".equals(a) || !a.equals(str2)) ? false : true;
                    if ("".equals(str2) && "".equals(a)) {
                        z = true;
                    }
                    d.e("TrafficCorrection", "isImsiOK:[" + z + "]");
                    fs avVar = new av();
                    avVar.bL = new ArrayList();
                    avVar.imsi = a;
                    avVar.bC = i2;
                    for (int i3 = 0; i3 < baVar.bW.size(); i3++) {
                        ax axVar = (ax) baVar.bW.get(i3);
                        d.e("TrafficCorrection", "[" + lwVar.dG + "]\u5f00\u59cb\u6267\u884c\u7b2c[" + (i3 + 1) + "]\u6761push\u6307\u4ee4:[" + axVar + "]");
                        boolean a2 = (z && axVar != null) ? this.yY.yT.a(i2, axVar, str2) : false;
                        d.e("TrafficCorrection", "]" + lwVar.dG + "]\u6307\u4ee4\u6267\u884c\u7ed3\u679c:[" + a2 + "]");
                        ay ayVar = new ay();
                        ayVar.bS = axVar;
                        ayVar.bT = a2;
                        avVar.bL.add(ayVar);
                    }
                    d.e("TrafficCorrection", "\u3010push\u6d88\u606f\u5904\u7406\u5b8c\u6bd5\u3011\u5168\u90e8\u6307\u4ee4\u6267\u884c\u7ed3\u675f--[upload]\u4e1a\u52a1\u56de\u5305imsi:[" + avVar.imsi + "]\u5361\u69fd:[" + avVar.bC + "]");
                    this.yY.yT.yP.b(lwVar.dG, lwVar.dF, 11006, avVar);
                }
            }

            {
                this.yT = r1;
            }

            public pl<Long, Integer, fs> a(int i, long j, int i2, fs fsVar) {
                d.e("TrafficCorrection", "\u3010push listener\u6536\u5230push\u6d88\u606f\u3011--cmdId:[" + i2 + "]pushId:[" + j + "]seqNo:[" + i + "]guid[" + this.yT.yP.c() + "]");
                switch (i2) {
                    case 11006:
                        if (fsVar != null) {
                            d.e("TrafficCorrection", "[\u6d41\u91cfpush\u6d88\u606f]--\u542f\u52a8worker\u7ebf\u7a0b\u8dd1\u6267\u884c");
                            Thread thread = new Thread(new AnonymousClass1(this, i2, i, j, fsVar));
                            thread.setName("pushImpl");
                            thread.start();
                            break;
                        }
                        d.e("TrafficCorrection", "push == null\u7ed3\u675f");
                        return null;
                }
                return null;
            }
        };
    }

    private int a(int i, List<MatchRule> list, String str, String str2, boolean z) {
        int i2;
        int i3 = 0;
        d.e("TrafficCorrection", "[\u5f00\u59cb\u6a21\u5757\u5339\u914d] body\uff1a[ " + str2 + "]isUsed:[" + z + "]matchRules:[" + list + "]");
        MatchRule matchRule = (MatchRule) list.get(0);
        MatchRule matchRule2 = new MatchRule(matchRule.unit, matchRule.type, matchRule.prefix, matchRule.postfix);
        if (list.size() > 1) {
            matchRule2.prefix += "&#" + matchRule2.unit + "&#" + matchRule.type;
        }
        for (int i4 = 1; i4 < list.size(); i4++) {
            matchRule2.prefix += ("&#" + ((MatchRule) list.get(i4)).prefix + "&#" + ((MatchRule) list.get(i4)).unit + "&#" + ((MatchRule) list.get(i4)).type);
            matchRule2.postfix += ("&#" + ((MatchRule) list.get(i4)).postfix);
        }
        d.e("TrafficCorrection", "prefix: " + matchRule2.prefix);
        d.e("TrafficCorrection", "postfix: " + matchRule2.postfix);
        AtomicInteger atomicInteger = new AtomicInteger();
        if (TrafficSmsParser.getNumberEntrance(str, str2, matchRule2, atomicInteger) != 0) {
            i2 = 0;
        } else {
            i2 = atomicInteger.get() + 0;
            i3 = 1;
        }
        if (i3 == 0) {
            d.e("TrafficCorrection", "[\u5339\u914d\u4e0d\u6210\u529f]");
            return 9;
        }
        d.e("TrafficCorrection", "[\u5339\u914d\u6210\u529f]isUsed:[" + z + "]\u6570\u636e\u4e3a\uff1a[" + i2 + "]");
        a aVar = new a(this);
        aVar.zk = i;
        aVar.zl = 1;
        aVar.zn = i2;
        if (z) {
            i2 = 6;
            aVar.zm = WifiDetectManager.SECURITY_PSK;
        } else {
            i2 = 7;
            aVar.zm = WifiDetectManager.SECURITY_WEP;
        }
        this.yO.sendMessage(this.yO.obtainMessage(4097, aVar));
        return i2;
    }

    private String a(int i, fs fsVar) {
        switch (i) {
            case 1001:
                aw awVar = (aw) fsVar;
                return "\nsimcard:" + awVar.bC + " imsi:" + awVar.imsi + "\n sms:" + awVar.sms + "\n startType:" + awVar.bI + "\n time:" + awVar.time + "\n code:" + awVar.bF + "\n vecTraffic:" + awVar.bD;
            case 1002:
                au auVar = (au) fsVar;
                return "\nsimcard:" + auVar.bC + " imsi:" + auVar.imsi + "\n method:" + auVar.bG + "\n tplate:" + auVar.bH + "\n sms:" + auVar.sms + "\n startType:" + auVar.bI + "\n time:" + auVar.time + "\n type:" + auVar.type + "\n code:" + auVar.bF + "\n vecTraffic:" + auVar.bD;
            case 1003:
                as asVar = (as) fsVar;
                return "\n simcard:" + asVar.bC + " imsi:" + asVar.imsi + "\n vecTraffic:" + asVar.bD;
            case 1004:
                ar arVar = (ar) fsVar;
                return "\n simcard:" + arVar.bC + " imsi:" + arVar.imsi + "\n authenResult:" + arVar.bB + "\n skey:" + arVar.bA;
            case 1007:
                at atVar = (at) fsVar;
                return "\n simcard:" + atVar.bC + " imsi:" + atVar.imsi + "getType:" + atVar.ap;
            case 1008:
                bi biVar = (bi) fsVar;
                String str = "\nsimcard:" + biVar.bC + "\n getParamType:" + biVar.cA + " fixMethod:" + biVar.cv + "\n fixTimeLocal:" + biVar.cz + "\n fixTimes:" + biVar.cu + "\n frequence:" + biVar.cy + "\n imsi:" + biVar.imsi + "\n status:" + biVar.status + "\n timeOutNum:" + biVar.cw + "\n queryCode:" + biVar.cx;
                return biVar.cx != null ? str + "\n port:" + biVar.cx.port + ", code:" + biVar.cx.bV : str;
            default:
                return "";
        }
    }

    private void a(int i, int i2, int i3, int i4) {
        d.e("TrafficCorrection", "[\u5f00\u59cb\u77ed\u4fe1\u6821\u6b63]");
        if (bm(i)) {
            this.yR = i2;
            this.yS = i3;
            if (this.yQ != null) {
                d.e("TrafficCorrection", "[\u901a\u77e5\u4f7f\u7528\u8005\u53bb\u53d1\u751f\u67e5\u8be2\u77ed\u4fe1]");
                this.yO.sendMessage(this.yO.obtainMessage(4098, i, 0));
            }
            return;
        }
        j(i, i4);
    }

    private void a(int i, String str, String str2) {
        int i2 = 1;
        int i3 = 0;
        String str3 = "";
        cz iw = f.iw();
        boolean z = iw == cz.gF || iw == cz.gE;
        d.e("TrafficCorrection", "doValify--skey:[" + str + "]url:[" + str2 + "]isGPRS:[" + z + "]");
        if (z) {
            try {
                mu cA = mu.cA(str2);
                cA.fc();
                if (cA.getResponseCode() != SmsCheckResult.ESCT_200) {
                    str = str3;
                    i3 = 2;
                }
                str3 = str;
                i2 = i3;
            } catch (NetWorkException e) {
                d.e("TrafficCorrection", "doValify--networkException:" + e.getMessage());
                i2 = 2;
            }
        }
        d.e("TrafficCorrection", "doValify--resultSkey:[" + str3 + "]errorcode:[" + i2 + "]");
        if (str3 == null) {
            str3 = "";
        }
        fs arVar = new ar();
        arVar.imsi = bn(i);
        arVar.bB = i2;
        arVar.bA = str3;
        arVar.bC = i;
        d.e("TrafficCorrection", "[upload]-[" + bp(1004) + "]\u5185\u5bb9:[" + a(1004, arVar) + "]");
        this.yP.a(1004, arVar, null, 2, null);
    }

    private void a(int i, ax axVar, String str, int i2) {
        a aVar = null;
        if (this.yQ != null) {
            if (axVar.bO == 4) {
                aVar = new a(this);
                aVar.zm = WifiDetectManager.SECURITY_WEP;
            } else if (axVar.bO == 3) {
                aVar = new a(this);
                aVar.zm = WifiDetectManager.SECURITY_PSK;
            } else if (axVar.bO == 6) {
                aVar = new a(this);
                aVar.zm = WifiDetectManager.SECURITY_EAP;
            }
            if (aVar != null) {
                aVar.zk = i;
                aVar.zl = i2;
                aVar.zn = Integer.valueOf(str).intValue();
                this.yO.sendMessage(this.yO.obtainMessage(4097, aVar));
            }
        }
    }

    private boolean a(int i, ax axVar, String str) {
        String str2 = axVar.bP;
        d.e("TrafficCorrection", "\u5904\u7406push\u5361\u69fd:[" + i + "] order.orderType:[" + axVar.bN + "](" + bq(axVar.bN) + ") content:[" + str2 + "]");
        j jVar = new j(i);
        fs lxVar;
        int i2;
        switch (axVar.bN) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                if (str2 != null && !"".equals(str2)) {
                    jVar.ck(str2);
                    break;
                }
                return false;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                if (str2 == null || "".equals(str2)) {
                    return false;
                }
                try {
                    jVar.bt(Integer.valueOf(str2).intValue());
                    break;
                } catch (NumberFormatException e) {
                    d.c("TrafficCorrection", "[Error]EOrder.EO_ChangeFrequncy" + e.getMessage());
                    return false;
                }
                break;
            case FileInfo.TYPE_BIGFILE /*3*/:
                jVar.A(false);
                break;
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                if (str2 != null && !"".equals(str2)) {
                    jVar.ci(str2);
                    break;
                }
                return false;
            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                jVar.A(true);
                break;
            case UrlCheckType.TIPS_CHEAT /*6*/:
                if (str2 != null && !"".equals(str2)) {
                    fs bfVar = new bf();
                    if (a(str2.getBytes(), bfVar) && bfVar.cn != null) {
                        jVar.o(bfVar.cn);
                        break;
                    }
                }
                return false;
                break;
            case UrlCheckType.TIPS_DEFAULT /*7*/:
                if (str2 != null && !"".equals(str2)) {
                    jVar.cl(str2);
                    break;
                }
                return false;
            case RubbishType.SCAN_FLAG_APK /*8*/:
                if (str2 == null || "".equals(str2)) {
                    return false;
                }
                try {
                    jVar.bs(Integer.valueOf(str2).intValue());
                    break;
                } catch (NumberFormatException e2) {
                    d.c("TrafficCorrection", "[Error]EO_ChangeTimeOut: " + e2.getMessage());
                    return false;
                }
                break;
            case UrlCheckType.MAKE_MONEY /*9*/:
                if (str2 != null && !"".equals(str2)) {
                    jVar.cj(str2);
                    break;
                }
                return false;
            case UrlCheckType.SEX /*10*/:
                if (str2 != null && !"".equals(str2)) {
                    a(i, axVar, str2, 1);
                    break;
                }
                return false;
                break;
            case UrlCheckType.PRIVATE_SERVER /*11*/:
                a(i, 1, 0, 7);
                break;
            case UrlCheckType.MSG_REACTIONARY /*12*/:
                if (str2 == null || "".equals(str2)) {
                    return false;
                }
                lxVar = new lx();
                if (a(str2.getBytes(), lxVar)) {
                    jq.ct().a(new AnonymousClass5(this, i, lxVar), "AuthenticationInfo_Check");
                    break;
                }
                return false;
                break;
            case QScanConstants.TYPE_AD_CHABO /*14*/:
                if (str2 != null && !"".equals(str2)) {
                    a(i, axVar, str2, 2);
                    break;
                }
                return false;
            case NumQueryRet.USED_FOR_Common /*16*/:
                if (str2 != null && !"".equals(str2)) {
                    jVar.cm(str2);
                    break;
                }
                return false;
            case UrlCheckType.MSG_BLOG /*19*/:
                if (str2 != null && !"".equals(str2)) {
                    a(i, axVar, str2, 3);
                    break;
                }
                return false;
            case 20:
                if (axVar.bQ != null && !"".equals(axVar.bQ)) {
                    lxVar = new bg();
                    if (a(axVar.bQ, lxVar)) {
                        d.e("TrafficCorrection", "push\u8be6\u60c5\u4fe1\u606ftimeNow:[" + lxVar.cp + "]");
                        if (lxVar.cq != null) {
                            Iterator it = lxVar.cq.iterator();
                            int i3 = 0;
                            while (it.hasNext()) {
                                bd bdVar = (bd) it.next();
                                if (bdVar.cd != null) {
                                    bc bcVar = bdVar.cd;
                                    d.e("TrafficCorrection", "[" + i3 + "]father.parDesc[" + j(bcVar.bY) + "]father.useNum[" + j(bcVar.bZ) + "]father.usePer[" + bcVar.ca + "]");
                                }
                                if (bdVar.ce != null) {
                                    Iterator it2 = bdVar.ce.iterator();
                                    i2 = 0;
                                    while (it2.hasNext()) {
                                        bc bcVar2 = (bc) it2.next();
                                        d.e("TrafficCorrection", "[" + i3 + "][" + i2 + "]son.parDesc[" + j(bcVar2.bY) + "]son.useNum[" + j(bcVar2.bZ) + "]son.usePer[" + bcVar2.ca + "]");
                                        i2++;
                                    }
                                }
                                i3++;
                            }
                            break;
                        }
                    }
                }
                return false;
                break;
            case 21:
                d.e("TrafficCorrection", "\u4e0b\u53d1profile");
                if (axVar.bQ == null || "".equals(axVar.bQ)) {
                    return false;
                }
                fs beVar = new be();
                if (a(axVar.bQ, beVar)) {
                    i2 = beVar.province;
                    int i4 = beVar.city;
                    String bo = bo(beVar.ch);
                    int i5 = beVar.ci;
                    d.e("TrafficCorrection", "province:[" + i2 + "]city[" + i4 + "]carry[" + bo + "]brand[" + i5 + "]payDay[" + beVar.ck + "]");
                    jVar.a(str, i2, i4, bo, i5);
                    ProfileInfo profileInfo = new ProfileInfo();
                    profileInfo.imsi = str;
                    profileInfo.province = i2;
                    profileInfo.city = i4;
                    profileInfo.carry = bo;
                    profileInfo.brand = i5;
                    Message obtainMessage = this.yO.obtainMessage(4102, i, 0);
                    obtainMessage.obj = profileInfo;
                    this.yO.sendMessage(obtainMessage);
                    break;
                }
                return false;
                break;
        }
        return true;
    }

    private boolean a(byte[] bArr, fs fsVar) {
        boolean z = false;
        if (bArr == null || fsVar == null) {
            return false;
        }
        fq fqVar = new fq(bArr);
        fqVar.ae("UTF-8");
        try {
            fsVar.readFrom(fqVar);
            z = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return z;
    }

    private synchronized void aK() {
        d.e("TrafficCorrection", "[\u6ce8\u518cpush listener]");
        this.yP.v(11006, 2);
        this.yP.a(11006, new ba(), 2, this.oy);
    }

    private int b(int i, String str, String str2) {
        d.e("TrafficCorrection", "\u672c\u5730\u6a21\u677f\u5206\u6790\u77ed\u4fe1");
        if (TrafficSmsParser.getWrongSmsType(str, str2) == 0) {
            j jVar = new j(i);
            List bu = jVar.bu(2);
            List bu2 = jVar.bu(1);
            if (bu.isEmpty() && bu2.isEmpty()) {
                d.e("TrafficCorrection", "\u6a21\u677f\u4e3a\u7a7a");
                l(i, ErrorCode.ERR_CORRECTION_LOCAL_NO_TEMPLATE);
                b(i, 3, str2);
                return 0;
            }
            int i2;
            boolean z;
            if (bu2.isEmpty()) {
                d.e("TrafficCorrection", "\u5269\u4f59\u6a21\u677f\u4e3a\u7a7a");
                i2 = 9;
                z = true;
            } else {
                i2 = a(i, bu2, str, str2, false);
                z = 9 == i2;
            }
            if (z && !bu.isEmpty()) {
                i2 = a(i, bu, str, str2, true);
            }
            if (i2 == 6 || i2 == 7) {
                d.e("TrafficCorrection", "\u5339\u914d\u6210\u529f");
                l(i, 0);
            } else {
                l(i, ErrorCode.ERR_CORRECTION_LOCAL_TEMPLATE_UNMATCH);
                b(i, i2, str2);
                d.e("TrafficCorrection", "\u5339\u914d\u5931\u8d25");
            }
            return 0;
        }
        d.e("TrafficCorrection", "[error]TrafficSmsParser.getWrongSmsType\u5f02\u5e38");
        l(i, ErrorCode.ERR_CORRECTION_BAD_SMS);
        return ErrorCode.ERR_CORRECTION_BAD_SMS;
    }

    private void b(int i, int i2, String str) {
        d.e("TrafficCorrection", "uploadLocalCorrectionState-simIndex:[" + i + "]fixType:[" + i2 + "]smsBody:[" + str + "]");
        j jVar = new j(i);
        ArrayList arrayList = new ArrayList();
        bf bfVar = new bf();
        bfVar.cn = arrayList;
        ArrayList arrayList2 = new ArrayList();
        az azVar = new az();
        if (azVar != null) {
            azVar.bV = jVar.ed();
            azVar.port = jVar.ee();
        }
        fs auVar = new au();
        auVar.imsi = bn(i);
        auVar.bF = azVar;
        auVar.bG = this.yS;
        auVar.sms = str;
        auVar.bI = this.yR;
        auVar.bH = bfVar;
        auVar.type = i2;
        auVar.bD = arrayList2;
        auVar.bC = i;
        d.e("TrafficCorrection", "[upload]-[" + bp(1002) + "],\u5185\u5bb9\uff1a[" + a(1002, auVar) + "]");
        this.yP.a(1002, auVar, null, 2, new AnonymousClass7(this, i));
    }

    private int bj(int i) {
        d.e("TrafficCorrection", "[profile\u4e0a\u62a5][Beg]");
        j jVar = new j(i);
        String dW = jVar.dW();
        String dX = jVar.dX();
        String dY = jVar.dY();
        String dZ = jVar.dZ();
        String bn = bn(i);
        int ea = jVar.ea();
        try {
            int intValue = Integer.valueOf(dW).intValue();
            int intValue2 = Integer.valueOf(dX).intValue();
            int intValue3 = Integer.valueOf(dZ).intValue();
            int cc = cc(dY);
            if (cc != -1) {
                int i2;
                int i3;
                int i4;
                int i5;
                int i6;
                int i7;
                ku.dq().a(new AnonymousClass4(this, i, bn, dW, dX, dY, dZ, ea));
                if (1 != i) {
                    i2 = 2003;
                    i3 = 2002;
                    i4 = 2004;
                    i5 = 2005;
                    i6 = 2007;
                    i7 = 2008;
                } else {
                    i2 = 2011;
                    i3 = 2010;
                    i4 = 2012;
                    i5 = 2013;
                    i6 = 2015;
                    i7 = 2016;
                }
                ku.dq().i(i3, Integer.valueOf(intValue).intValue());
                ku.dq().i(i2, Integer.valueOf(intValue2).intValue());
                ku.dq().i(i4, cc);
                ku.dq().i(i5, Integer.valueOf(intValue3).intValue());
                ku.dq().i(i6, ea);
                ku.dq().c(i7, true);
                d.e("TrafficCorrection", "[profile\u4e0a\u62a5][End]");
                return 0;
            }
            d.e("TrafficCorrection", "[error] upload profile Operator error");
            return ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
        } catch (NumberFormatException e) {
            d.e("TrafficCorrection", "[error] upload profile NumberFormatException:" + e.getMessage());
            return ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
        }
    }

    private boolean bk(int i) {
        j jVar = new j(i);
        String dW = jVar.dW();
        String dX = jVar.dX();
        String dY = jVar.dY();
        String dZ = jVar.dZ();
        d.e("TrafficCorrection", "[\u68c0\u67e5\u7701\u3001\u5e02\u3001\u8fd0\u8425\u5546\u3001\u54c1\u724c\u4ee3\u7801]province:[" + dW + "]city:[" + dX + "]carry:[" + dY + "]brand:[" + dZ + "]");
        if (!l.dm(dW) && !l.dm(dX) && !l.dm(dY) && !l.dm(dZ)) {
            return true;
        }
        d.e("TrafficCorrection", "[error]\u7701\u3001\u5e02\u3001\u8fd0\u8425\u5546\u3001\u54c1\u724c\u4ee3\u7801\u5b58\u5728\u4e3a\u7a7a");
        return false;
    }

    private boolean bl(int i) {
        j jVar = new j(i);
        String str = this.yP.c() + "$" + bn(i) + "$" + jVar.dW() + jVar.dX() + jVar.dY() + jVar.dZ() + "$" + jVar.ea();
        String eb = jVar.eb();
        d.g("TrafficCorrection", "currentInfo:[" + str + "]lastSuccessInfo:[" + eb + "]");
        return str.compareTo(eb) != 0;
    }

    private boolean bm(int i) {
        j jVar = new j(i);
        CharSequence ed = jVar.ed();
        CharSequence ee = jVar.ee();
        d.e("TrafficCorrection", "[\u68c0\u67e5\u67e5\u8be2\u7801\u4e0e\u7aef\u53e3\u53f7]queryCode:[" + ed + "]queryPort:[" + ee + "]");
        if (!TextUtils.isEmpty(ed) && !TextUtils.isEmpty(ee)) {
            return true;
        }
        d.e("TrafficCorrection", "[error]\u67e5\u8be2\u7801\u6216\u7aef\u53e3\u53f7\u4e0d\u5408\u6cd5");
        return false;
    }

    private String bn(int i) {
        String str = "";
        if (jq.cx() != null) {
            str = jq.cx().getIMSI(i);
        } else if (i == 0) {
            str = h.D(TMSDKContext.getApplicaionContext());
        }
        d.e("TrafficCorrection", "getIMSIBySimSlot:[" + i + "][" + str + "");
        return str;
    }

    private String bo(int i) {
        return i != 2 ? i != 1 ? i != 3 ? "" : "TELECOM" : "UNICOM" : "CMCC";
    }

    private String bp(int i) {
        switch (i) {
            case 1001:
                return "\u901a\u8fc7\u67e5\u8be2\u7801\u83b7\u53d6\u5230\u6d41\u91cf\u77ed\u4fe1\u5904\u7406";
            case 1002:
                return "\u672c\u5730\u6821\u6b63\u540e\u4e0a\u62a5";
            case 1003:
                return "\u624b\u52a8\u4fee\u6539\u4e0a\u62a5";
            case 1004:
                return "\u8eab\u4efd\u9a8c\u8bc1";
            case 1007:
                return "\u624b\u52a8\u83b7\u53d6\u4e91\u7aef\u6570\u636e";
            case 1008:
                return "\u7ea0\u9519\u4e0a\u62a5";
            default:
                return "";
        }
    }

    private String bq(int i) {
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                return "\u6821\u6b63\u7c7b\u578b";
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return "\u8c03\u6574\u6821\u6b63\u9891\u7387\uff1a\u4f8b\u5982\u4e00\u5929\u6821\u6b63\u4e00\u6b21\u8c03\u6574\u4e3a3\u5929\u6821\u6b63\u4e00\u6b21";
            case FileInfo.TYPE_BIGFILE /*3*/:
                return "\u590d\u6d3b\u6307\u4ee4\uff1a\u5173\u95ed\u6821\u6b63\u7684\u7528\u6237\u590d\u6d3b\u3002";
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                return "\u76f4\u63a5\u66ff\u6362\u7ec8\u7aef\u5f53\u524d\u4f7f\u7528\u7684\u67e5\u8be2\u7801\uff1a\u6362\u67e5\u8be2\u7801\u65f6\u4f7f\u7528";
            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                return "\u6682\u505c\u6821\u6b63";
            case UrlCheckType.TIPS_CHEAT /*6*/:
                return "\u4e0b\u53d1\u6a21\u677f";
            case UrlCheckType.TIPS_DEFAULT /*7*/:
                return "\u8c03\u6574\u6821\u6b63\u65f6\u673a:\u5141\u8bb8server\u6821\u6b63\u7684\u65f6\u95f4\u6bb5\u8c03\u6574";
            case RubbishType.SCAN_FLAG_APK /*8*/:
                return "\u66ff\u6362\u8d85\u65f6\u65f6\u95f4";
            case UrlCheckType.MAKE_MONEY /*9*/:
                return "\u66f4\u6362\u76d1\u542c\u8fd0\u8425\u5546\u7aef\u53e3\u3002";
            case UrlCheckType.SEX /*10*/:
                return "\u4e0b\u53d1GPRS\u6d41\u91cf\u503c";
            case UrlCheckType.PRIVATE_SERVER /*11*/:
                return "\u7acb\u5373\u6267\u884c\u4e00\u6b21\u6821\u6b63";
            case UrlCheckType.MSG_REACTIONARY /*12*/:
                return "\u4e0b\u53d1\u8eab\u4efd\u8ba4\u8bc1\u4fe1\u606f(url+sky)\uff0c\u7ec8\u7aef\u6536\u5230\u8be5\u4fe1\u606f\u540e\u8fdb\u884c\u7701\u4efd\u8ba4\u8bc1";
            case QScanConstants.TYPE_AD_BANNER /*13*/:
                return "\u4e0b\u53d1TD\u6d41\u91cf\u503c";
            case QScanConstants.TYPE_AD_CHABO /*14*/:
                return "\u4e0b\u53d1\u95f2\u65f6\u6d41\u91cf\u503c";
            case RubbishType.SCAN_FLAG_ALL /*15*/:
                return "\u4e0b\u53d1\u4e00\u4e32\u5185\u5bb9\uff0c\u8fd9\u4e32\u5185\u5bb9\u9700\u8981\u7ec8\u7aef\u5c55\u793a\u7ed9\u7528\u6237\u770b\uff1b";
            case NumQueryRet.USED_FOR_Common /*16*/:
                return "\u8c03\u6574\u6821\u6b63\u65f6\u673a:\u5141\u8bb8Local\u6821\u6b63\u7684\u65f6\u95f4\u6bb5\u8c03\u6574";
            case NumQueryRet.USED_FOR_Calling /*17*/:
                return "\u4e0b\u53d1\u63a8\u5e7f\u94fe\u63a5";
            case 20:
                return "\u6d41\u91cf\u8be6\u60c5";
            case 21:
                return "\u4e0b\u53d1profile";
            default:
                return "";
        }
    }

    private int cc(String str) {
        return !"CMCC".equals(str) ? !"UNICOM".equals(str) ? !"TELECOM".equals(str) ? -1 : 3 : 1 : 2;
    }

    private String j(byte[] bArr) {
        return bArr != null ? new String(bArr) : "";
    }

    private void j(int i, int i2) {
        int i3 = 0;
        d.e("TrafficCorrection", "[uploadParam]simIndex:[" + i + "]");
        j jVar = new j(i);
        az azVar = new az();
        azVar.bV = jVar.ed();
        azVar.port = jVar.ee();
        fs biVar = new bi();
        biVar.imsi = bn(i);
        biVar.cv = jVar.ef();
        biVar.cz = jVar.eg();
        biVar.cu = jVar.eh();
        biVar.cy = jVar.ei();
        biVar.cx = azVar;
        if (jVar.ej()) {
            i3 = 2;
        }
        biVar.status = i3;
        biVar.cA = i2;
        biVar.cw = jVar.ec();
        biVar.bC = i;
        d.e("TrafficCorrection", "[upload]-[" + bp(1008) + "],\u5185\u5bb9\uff1a[" + a(1008, biVar) + "]");
        this.yP.a(1008, biVar, null, 2, new AnonymousClass6(this, i));
    }

    private void l(int i, int i2) {
        if (i2 != 0) {
            this.yO.sendMessage(this.yO.obtainMessage(4101, i, i2));
        }
        d.e("TrafficCorrection", "[\u672c\u6b21\u6821\u6b63\u6d41\u7a0b\u7ed3\u675f]--\u91cd\u7f6e\u72b6\u6001");
    }

    public int a(int i, String str, String str2, String str3, int i2) {
        d.e("TrafficCorrection", "[\u5206\u6790\u77ed\u4fe1]analysisSMS--simIndex:[" + i + "]queryCode:[" + str + "]queryPort:" + str2 + "]smsBody:[" + str3 + "]");
        if (i == 0 || i == 1) {
            if (!(l.dm(str) || l.dm(str2) || l.dm(str3))) {
                az azVar = new az();
                if (azVar != null) {
                    azVar.bV = str;
                    azVar.port = str2;
                }
                if (!f.hv()) {
                    return b(i, str2, str3);
                }
                d.e("TrafficCorrection", "\u6709\u7f51\u7edc\uff0c\u8d70\u4e91\u77ed\u4fe1");
                fs awVar = new aw();
                awVar.imsi = bn(i);
                awVar.bF = azVar;
                awVar.sms = str3;
                awVar.bI = this.yR;
                awVar.time = i2;
                awVar.bD = new ArrayList();
                awVar.bC = i;
                d.e("TrafficCorrection", "[upload]-[" + bp(1001) + "]\u5185\u5bb9:[" + a(1001, awVar) + "]");
                this.yP.a(1001, awVar, null, 2, new AnonymousClass3(this, i, str2, str3));
                return 0;
            }
        }
        l(i, -6);
        d.e("TrafficCorrection", "\u53c2\u6570\u9519\u8bef");
        return -6;
    }

    public int getSingletonType() {
        return 1;
    }

    public boolean k(int i, int i2) {
        Object ef = new j(i).ef();
        if (TextUtils.isEmpty(ef)) {
            return false;
        }
        String[] split = ef.replace("||", "*").split("\\*");
        int i3 = 0;
        while (i3 < split.length) {
            try {
                if (Integer.valueOf(split[i3]).intValue() == i2) {
                    return true;
                }
                i3++;
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }

    public void onCreate(Context context) {
        d.e("TrafficCorrection", "TrafficCorrectionManagerImpl-OnCreate-context:[" + context + "]");
        this.mContext = context;
        this.yP = jq.cu();
        aK();
        this.yO = new AnonymousClass1(this, Looper.getMainLooper());
    }

    public int requestProfile(int i) {
        d.e("TrafficCorrection", "requestProfile--simIndex:[" + i + "]");
        ProfileInfo c = new j(i).c(i, bn(i));
        if (c.province != -1) {
            Message obtainMessage = this.yO.obtainMessage(4102, i, 0);
            obtainMessage.obj = c;
            this.yO.sendMessage(obtainMessage);
        } else {
            d.e("TrafficCorrection", "\u672c\u5730\u6ca1\u6709profile\u4fe1\u606f");
            j(i, 5);
        }
        return 0;
    }

    public int setConfig(int i, String str, String str2, String str3, String str4, int i2) {
        d.e("TrafficCorrection", "[\u8bbe\u7f6e\u7701\u3001\u5e02\u3001\u8fd0\u8425\u5546\u3001\u54c1\u724c\u4ee3\u7801]simIndex:[" + i + "]provinceId:[" + str + "]cityId:[" + str2 + "]carryId:[" + str3 + "]brandId:[" + str4 + "]closingDay:[" + i2 + "]");
        if (i == 0 || i == 1) {
            if (!(l.dm(str) || l.dm(str2) || l.dm(str3) || l.dm(str4))) {
                j jVar = new j(i);
                jVar.cd(str);
                jVar.ce(str2);
                jVar.cf(str3);
                jVar.cg(str4);
                jVar.br(i2);
                return 0;
            }
        }
        d.e("TrafficCorrection", "[error]\u8bbe\u7f6e\u4fe1\u606f\u6709\u7684\u4e3a\u7a7a");
        return -6;
    }

    public int setTrafficCorrectionListener(ITrafficCorrectionListener iTrafficCorrectionListener) {
        d.e("TrafficCorrection", "[\u8bbe\u7f6e\u6d41\u91cf\u6821\u6b63\u76d1\u542c]listener:[" + iTrafficCorrectionListener + "]");
        if (iTrafficCorrectionListener == null) {
            return -6;
        }
        this.yQ = iTrafficCorrectionListener;
        return 0;
    }

    public int startCorrection(int i) {
        d.e("TrafficCorrection", "[\u5f00\u59cb\u6821\u6b63]simIndex:[ " + i + "]");
        if (f.hv()) {
            this.yP.gl();
        }
        if (i != 0 && i != 1) {
            d.e("TrafficCorrection", "[error]simIndex \u4e0d\u5408\u6cd5");
            return -6;
        } else if (bk(i)) {
            j jVar = new j(i);
            if (bl(i)) {
                d.e("TrafficCorrection", "[\u9700\u8981\u4e0a\u62a5profile][\u4e0a\u62a5profile\u89e6\u53d1\u540e\u7eed\u6821\u6b63\u6d41\u7a0b]");
                if (!f.hv()) {
                    d.e("TrafficCorrection", "\u6ca1\u6709\u7f51\u7edc-[profile\u4e0a\u62a5]\u7ed3\u675f");
                    return ErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL;
                } else if (bj(i) != 0) {
                    return ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
                }
            }
            boolean hv = f.hv();
            if (!hv) {
                d.e("TrafficCorrection", "[\u672c\u5730\u6a21\u677f\u5339\u914d]simIndex:[" + i + "]");
                a(i, 2, 2, 5);
                return 0;
            } else if (hv && l.dm(jVar.ef())) {
                d.e("TrafficCorrection", "[\u65e0\u6821\u6b63\u65b9\u5f0f\uff0c\u7ea0\u9519\u4e0a\u62a5]simIndex:[" + i + "]");
                j(i, 5);
                return 0;
            } else if (k(i, 0)) {
                d.e("TrafficCorrection", "[\u8fd0\u8425\u5546\u4e91\u7aef\u5408\u4f5c\u6821\u6b63]simIndex:[" + i + "]");
                fs atVar = new at();
                atVar.ap = 0;
                atVar.bC = i;
                atVar.imsi = bn(i);
                d.e("TrafficCorrection", "[upload]-[" + bp(1007) + "]\u5185\u5bb9:[" + a(1007, atVar));
                this.yP.a(1007, atVar, null, 2, null);
            } else if (k(i, 3)) {
                d.e("TrafficCorrection", "[\u77ed\u4fe1\u4e91\u7aef\u6821\u6b63]simIndex:[" + i + "]");
                a(i, 2, 3, 5);
            }
            return 0;
        } else {
            d.e("TrafficCorrection", "[error]ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL");
            return ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
        }
    }
}
