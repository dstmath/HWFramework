package tmsdk.bg.module.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.TMSDKContext;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.tcc.TrafficSmsParser;
import tmsdk.common.tcc.TrafficSmsParser.MatchRule;
import tmsdk.common.utils.f;
import tmsdk.common.utils.l;
import tmsdk.common.utils.q;
import tmsdk.common.utils.s;
import tmsdkobf.ay;
import tmsdkobf.az;
import tmsdkobf.ba;
import tmsdkobf.bb;
import tmsdkobf.bc;
import tmsdkobf.bd;
import tmsdkobf.be;
import tmsdkobf.bf;
import tmsdkobf.bg;
import tmsdkobf.bh;
import tmsdkobf.bj;
import tmsdkobf.bk;
import tmsdkobf.bl;
import tmsdkobf.bm;
import tmsdkobf.bn;
import tmsdkobf.bo;
import tmsdkobf.bq;
import tmsdkobf.eb;
import tmsdkobf.im;
import tmsdkobf.jk;
import tmsdkobf.jl;
import tmsdkobf.jn;
import tmsdkobf.jy;
import tmsdkobf.ka;
import tmsdkobf.kp;
import tmsdkobf.kq;
import tmsdkobf.lw;
import tmsdkobf.nl;
import tmsdkobf.oa;
import tmsdkobf.oh;

class i extends BaseManagerB {
    private Context mContext;
    private int mRetryCount = 0;
    public final int vO = 4097;
    public final int vP = 4098;
    public final int vQ = 4099;
    public final int vR = 4100;
    public final int vS = 4101;
    public final int vT = 4102;
    public final int vU = 4103;
    public final int vV = 4104;
    Handler vW;
    private oa vX;
    private ITrafficCorrectionListener vY = null;
    private int vZ = 2;
    private int wa = 3;
    ka wb = new ka() {
        public oh<Long, Integer, JceStruct> a(int -l_6_I, long -l_7_J, int -l_9_I, JceStruct -l_10_R) {
            f.d("TrafficCorrection", "【push listener收到push消息】--cmdId:[" + -l_9_I + "]pushId:[" + -l_7_J + "]seqNo:[" + -l_6_I + "]guid[" + i.this.vX.b() + "]");
            switch (-l_9_I) {
                case 11006:
                    if (-l_10_R != null) {
                        f.d("TrafficCorrection", "[流量push消息]--启动worker线程跑执行");
                        final int i = -l_9_I;
                        final int i2 = -l_6_I;
                        final long j = -l_7_J;
                        final JceStruct jceStruct = -l_10_R;
                        Thread thread = new Thread(new Runnable() {
                            public void run() {
                                kp kpVar = new kp();
                                kpVar.Y = i;
                                kpVar.ey = i2;
                                kpVar.ex = j;
                                kpVar.wL = jceStruct;
                                bh bhVar = (bh) kpVar.wL;
                                String str = bhVar.imsi;
                                int i = bhVar.cj;
                                f.d("TrafficCorrection", "[执行push消息]--个数:[" + bhVar.cD.size() + "]--cloudimsi:[" + str + "]卡槽:[" + i + "]seqNo:[" + kpVar.ey + "]pushId:[" + kpVar.ex + "]");
                                if (!(i == 0 || i == 1)) {
                                    i = 0;
                                }
                                boolean z = false;
                                String a = i.this.at(i);
                                if (str == null) {
                                    str = "";
                                }
                                if (a == null) {
                                    a = "";
                                }
                                if (!("".equals(str) || "".equals(a) || !a.equals(str))) {
                                    z = true;
                                }
                                if ("".equals(str) && "".equals(a)) {
                                    z = true;
                                }
                                f.d("TrafficCorrection", "isImsiOK:[" + z + "]");
                                JceStruct bcVar = new bc();
                                bcVar.cs = new ArrayList();
                                bcVar.imsi = a;
                                bcVar.cj = i;
                                Object obj = null;
                                for (int i2 = 0; i2 < bhVar.cD.size(); i2++) {
                                    boolean z2 = false;
                                    be beVar = (be) bhVar.cD.get(i2);
                                    f.d("TrafficCorrection", "[" + kpVar.ey + "]开始执行第[" + (i2 + 1) + "]条push指令:[" + beVar + "]");
                                    if (z && beVar != null) {
                                        z2 = i.this.a(i, beVar, str);
                                        if (!((beVar.cu != 14 && beVar.cu != 10 && beVar.cu != 13) || beVar.cw == null || beVar.cw.equals(""))) {
                                            obj = 1;
                                        }
                                    }
                                    f.d("TrafficCorrection", "]" + kpVar.ey + "]指令执行结果:[" + z2 + "]");
                                    bf bfVar = new bf();
                                    bfVar.cz = beVar;
                                    bfVar.cA = z2;
                                    bcVar.cs.add(bfVar);
                                }
                                f.d("TrafficCorrection", "【push消息处理完毕】全部指令执行结束--[upload]业务回包imsi:[" + bcVar.imsi + "]卡槽:[" + bcVar.cj + "]");
                                i.this.vX.b(kpVar.ey, kpVar.ex, 11006, bcVar);
                                if (obj != null && i.this.vY != null) {
                                    i.this.vW.sendMessage(i.this.vW.obtainMessage(4103, i, 0));
                                }
                            }
                        });
                        thread.setName("pushImpl");
                        thread.start();
                        break;
                    }
                    f.d("TrafficCorrection", "push == null结束");
                    return null;
            }
            return null;
        }
    };

    class a {
        int wt;
        int wu;
        int wv;
        int ww;

        a() {
        }
    }

    i() {
    }

    private int a(int i, List<MatchRule> list, String str, String str2, boolean z) {
        int i2 = 9;
        f.d("TrafficCorrection", "[开始模块匹配] body：[ " + str2 + "]isUsed:[" + z + "]matchRules:[" + list + "]");
        MatchRule matchRule = (MatchRule) list.get(0);
        MatchRule matchRule2 = new MatchRule(matchRule.unit, matchRule.type, matchRule.prefix, matchRule.postfix);
        if (list.size() > 1) {
            matchRule2.prefix += "&#" + matchRule2.unit + "&#" + matchRule.type;
        }
        for (int i3 = 1; i3 < list.size(); i3++) {
            matchRule2.prefix += ("&#" + ((MatchRule) list.get(i3)).prefix + "&#" + ((MatchRule) list.get(i3)).unit + "&#" + ((MatchRule) list.get(i3)).type);
            matchRule2.postfix += ("&#" + ((MatchRule) list.get(i3)).postfix);
        }
        f.d("TrafficCorrection", "prefix: " + matchRule2.prefix);
        f.d("TrafficCorrection", "postfix: " + matchRule2.postfix);
        Object obj = null;
        int i4 = 0;
        AtomicInteger atomicInteger = new AtomicInteger();
        if (TrafficSmsParser.getNumberEntrance(str, str2, matchRule2, atomicInteger) == 0) {
            obj = 1;
            i4 = atomicInteger.get() + 0;
        }
        if (obj == null) {
            f.d("TrafficCorrection", "[匹配不成功]");
        } else {
            f.d("TrafficCorrection", "[匹配成功]isUsed:[" + z + "]数据为：[" + i4 + "]");
            a aVar = new a();
            aVar.wt = i;
            aVar.wu = 1;
            aVar.ww = i4;
            if (z) {
                i2 = 6;
                aVar.wv = 258;
            } else {
                i2 = 7;
                aVar.wv = 257;
            }
            this.vW.sendMessage(this.vW.obtainMessage(4097, aVar));
        }
        return i2;
    }

    private String a(int i, JceStruct jceStruct) {
        switch (i) {
            case 1001:
                bd bdVar = (bd) jceStruct;
                return "\nsimcard:" + bdVar.cj + " imsi:" + bdVar.imsi + "\n sms:" + bdVar.sms + "\n startType:" + bdVar.cp + "\n time:" + bdVar.time + "\n code:" + bdVar.cm + "\n vecTraffic:" + bdVar.ck;
            case 1002:
                bb bbVar = (bb) jceStruct;
                return "\nsimcard:" + bbVar.cj + " imsi:" + bbVar.imsi + "\n method:" + bbVar.cn + "\n tplate:" + bbVar.co + "\n sms:" + bbVar.sms + "\n startType:" + bbVar.cp + "\n time:" + bbVar.time + "\n type:" + bbVar.type + "\n code:" + bbVar.cm + "\n vecTraffic:" + bbVar.ck;
            case 1003:
                az azVar = (az) jceStruct;
                return "\n simcard:" + azVar.cj + " imsi:" + azVar.imsi + "\n vecTraffic:" + azVar.ck;
            case 1004:
                ay ayVar = (ay) jceStruct;
                return "\n simcard:" + ayVar.cj + " imsi:" + ayVar.imsi + "\n authenResult:" + ayVar.ci + "\n skey:" + ayVar.ch;
            case 1007:
                ba baVar = (ba) jceStruct;
                return "\n simcard:" + baVar.cj + " imsi:" + baVar.imsi + "getType:" + baVar.aH;
            case 1008:
                bq bqVar = (bq) jceStruct;
                String str = "\nsimcard:" + bqVar.cj + "\n getParamType:" + bqVar.dj + " fixMethod:" + bqVar.de + "\n fixTimeLocal:" + bqVar.di + "\n fixTimes:" + bqVar.dd + "\n frequence:" + bqVar.dh + "\n imsi:" + bqVar.imsi + "\n status:" + bqVar.status + "\n timeOutNum:" + bqVar.df + "\n queryCode:" + bqVar.dg;
                return bqVar.dg != null ? str + "\n port:" + bqVar.dg.port + ", code:" + bqVar.dg.cC : str;
            default:
                return "";
        }
    }

    private void a(int i, int i2, int i3, int i4) {
        f.d("TrafficCorrection", "[开始短信校正]");
        if (as(i)) {
            this.vZ = i2;
            this.wa = i3;
            if (this.vY != null) {
                f.d("TrafficCorrection", "[通知使用者去发生查询短信]");
                this.vW.sendMessage(this.vW.obtainMessage(4098, i, 0));
            }
            return;
        }
        m(i, i4);
    }

    private void a(final int -l_10_I, int i, String str) {
        f.d("TrafficCorrection", "uploadLocalCorrectionState-simIndex:[" + -l_10_I + "]fixType:[" + i + "]smsBody:[" + str + "]");
        j jVar = new j(-l_10_I);
        ArrayList arrayList = new ArrayList();
        bm bmVar = new bm();
        bmVar.cU = arrayList;
        ArrayList arrayList2 = new ArrayList();
        bg bgVar = new bg();
        if (bgVar != null) {
            bgVar.cC = jVar.dm();
            bgVar.port = jVar.dn();
        }
        JceStruct bbVar = new bb();
        bbVar.imsi = at(-l_10_I);
        bbVar.cm = bgVar;
        bbVar.cn = this.wa;
        bbVar.sms = str;
        bbVar.cp = this.vZ;
        bbVar.co = bmVar;
        bbVar.type = i;
        bbVar.ck = arrayList2;
        bbVar.cj = -l_10_I;
        f.d("TrafficCorrection", "[upload]-[" + av(1002) + "],内容：[" + a(1002, bbVar) + "]");
        this.vX.a(1002, bbVar, null, 2, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                if (i3 != 0) {
                    i.this.o(-l_10_I, TrafficErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL);
                }
            }
        });
    }

    private void a(int i, String -l_5_R, String str) {
        int i2;
        boolean z = false;
        String str2 = "";
        eb iG = tmsdk.common.utils.i.iG();
        if (iG == eb.iL || iG == eb.iK) {
            z = true;
        }
        f.d("TrafficCorrection", "doValify--skey:[" + -l_5_R + "]url:[" + str + "]isGPRS:[" + z + "]");
        if (z) {
            try {
                lw bO = lw.bO(str);
                bO.eK();
                if (bO.getResponseCode() == SmsCheckResult.ESCT_200) {
                    i2 = 0;
                }
            } catch (NetWorkException e) {
                f.d("TrafficCorrection", "doValify--networkException:" + e.getMessage());
            }
            i2 = 2;
            -l_5_R = str2;
        } else {
            i2 = 1;
            -l_5_R = str2;
        }
        f.d("TrafficCorrection", "doValify--resultSkey:[" + -l_5_R + "]errorcode:[" + i2 + "]");
        if (-l_5_R == null) {
            -l_5_R = "";
        }
        JceStruct ayVar = new ay();
        ayVar.imsi = at(i);
        ayVar.ci = i2;
        ayVar.ch = -l_5_R;
        ayVar.cj = i;
        f.d("TrafficCorrection", "[upload]-[" + av(1004) + "]内容:[" + a(1004, ayVar) + "]");
        this.vX.a(1004, ayVar, null, 2, null);
    }

    private void a(int i, be beVar, String str, int i2) {
        if (this.vY != null) {
            a aVar = null;
            if (beVar.cv == 4) {
                aVar = new a();
                aVar.wv = 257;
            } else if (beVar.cv == 3) {
                aVar = new a();
                aVar.wv = 258;
            } else if (beVar.cv == 6) {
                aVar = new a();
                aVar.wv = 259;
            }
            if (!(aVar == null || this.vW == null)) {
                aVar.wt = i;
                aVar.wu = i2;
                aVar.ww = Integer.valueOf(str).intValue();
                this.vW.sendMessage(this.vW.obtainMessage(4097, aVar));
            }
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(int i, be beVar, String str) {
        boolean z = true;
        String str2 = beVar.cw;
        f.d("TrafficCorrection", "处理push卡槽:[" + i + "] order.orderType:[" + beVar.cu + "](" + aw(beVar.cu) + ") content:[" + str2 + "]");
        j jVar = new j(i);
        int i2;
        switch (beVar.cu) {
            case 1:
                if (!(str2 == null || "".equals(str2))) {
                    f.f("jiejieT", "simIndex is " + i);
                    f.f("jiejieT", "correctionType is " + str2);
                    jVar.bm(str2);
                    break;
                }
            case 2:
                if (!(str2 == null || "".equals(str2))) {
                    try {
                        jVar.aA(Integer.valueOf(str2).intValue());
                        break;
                    } catch (NumberFormatException e) {
                        f.e("TrafficCorrection", "[Error]EOrder.EO_ChangeFrequncy" + e.getMessage());
                    }
                }
            case 3:
                jVar.o(false);
                break;
            case 4:
                if (!(str2 == null || "".equals(str2))) {
                    jVar.bk(str2);
                    jVar.n(true);
                    f.f("New_PortCode", "EOrder.EO_DownCode, simIndex is " + i);
                    break;
                }
            case 5:
                jVar.o(true);
                break;
            case 6:
                if (!(str2 == null || "".equals(str2))) {
                    JceStruct bmVar = new bm();
                    if (a(str2.getBytes(), bmVar) && bmVar.cU != null) {
                        jVar.f(bmVar.cU);
                        break;
                    }
                }
            case 7:
                if (!(str2 == null || "".equals(str2))) {
                    jVar.bn(str2);
                    break;
                }
            case 8:
                if (!(str2 == null || "".equals(str2))) {
                    try {
                        jVar.az(Integer.valueOf(str2).intValue());
                        break;
                    } catch (NumberFormatException e2) {
                        f.e("TrafficCorrection", "[Error]EO_ChangeTimeOut: " + e2.getMessage());
                    }
                }
            case 9:
                if (!(str2 == null || "".equals(str2))) {
                    jVar.bl(str2);
                    jVar.m(true);
                    f.f("TrafficCorrection", "EOrder.EO_DownPort, simIndex is " + i);
                    break;
                }
            case 10:
                if (!(str2 == null || "".equals(str2))) {
                    a(i, beVar, str2, 1);
                    break;
                }
            case 11:
                a(i, 1, 0, 7);
                break;
            case 12:
                if (!(str2 == null || "".equals(str2))) {
                    final JceStruct kqVar = new kq();
                    if (a(str2.getBytes(), kqVar)) {
                        final int i3 = i;
                        im.bJ().addTask(new Runnable() {
                            public void run() {
                                i.this.a(i3, kqVar.ch, kqVar.url);
                            }
                        }, "AuthenticationInfo_Check");
                        break;
                    }
                }
            case 14:
                if (!(str2 == null || "".equals(str2))) {
                    a(i, beVar, str2, 2);
                    break;
                }
            case 16:
                if (!(str2 == null || "".equals(str2))) {
                    jVar.bo(str2);
                    break;
                }
            case 19:
                if (!(str2 == null || "".equals(str2))) {
                    a(i, beVar, str2, 3);
                    break;
                }
            case 20:
                if (!(beVar.cx == null || beVar.cx.length == 0)) {
                    if (this.vW != null) {
                        JceStruct bnVar = new bn();
                        if (a(beVar.cx, bnVar)) {
                            f.d("TrafficCorrection", "push详情信息timeNow:[" + bnVar.cW + "]");
                            i2 = 0;
                            if (bnVar.cX != null) {
                                ArrayList arrayList = new ArrayList();
                                Iterator it = bnVar.cX.iterator();
                                while (it.hasNext()) {
                                    bj bjVar;
                                    bk bkVar = (bk) it.next();
                                    DetailCategoryInfo detailCategoryInfo = new DetailCategoryInfo();
                                    if (bkVar.cK != null) {
                                        bjVar = bkVar.cK;
                                        f.d("TrafficCorrection", "[" + i2 + "]father.parDesc[" + j(bjVar.cF) + "]father.useNum[" + j(bjVar.cG) + "]father.usePer[" + bjVar.cH + "]");
                                        DetailItemInfo detailItemInfo = new DetailItemInfo();
                                        detailItemInfo.mDescription = j(bjVar.cF);
                                        detailItemInfo.mLeft = j(bjVar.cG);
                                        detailItemInfo.mUsed = bjVar.cH;
                                        detailCategoryInfo.mFatherInfo = detailItemInfo;
                                    }
                                    if (bkVar.cL != null) {
                                        int i4 = 0;
                                        Iterator it2 = bkVar.cL.iterator();
                                        while (it2.hasNext()) {
                                            bjVar = (bj) it2.next();
                                            f.d("TrafficCorrection", "[" + i2 + "][" + i4 + "]son.parDesc[" + j(bjVar.cF) + "]son.useNum[" + j(bjVar.cG) + "]son.usePer[" + bjVar.cH + "]");
                                            i4++;
                                            DetailItemInfo detailItemInfo2 = new DetailItemInfo();
                                            detailItemInfo2.mDescription = j(bjVar.cF);
                                            detailItemInfo2.mLeft = j(bjVar.cG);
                                            detailItemInfo2.mUsed = bjVar.cH;
                                            detailCategoryInfo.mSonInfoList.add(detailItemInfo2);
                                        }
                                    }
                                    i2++;
                                    arrayList.add(detailCategoryInfo);
                                }
                                this.vW.sendMessage(this.vW.obtainMessage(4104, arrayList));
                                break;
                            }
                        }
                    }
                }
                break;
            case 21:
                f.d("TrafficCorrection", "下发profile");
                if (!(beVar.cx == null || beVar.cx.length == 0)) {
                    JceStruct blVar = new bl();
                    if (a(beVar.cx, blVar)) {
                        int i5 = blVar.province;
                        i2 = blVar.city;
                        String au = au(blVar.cO);
                        int i6 = blVar.cP;
                        f.d("TrafficCorrection", "province:[" + i5 + "]city[" + i2 + "]carry[" + au + "]brand[" + i6 + "]payDay[" + blVar.cR + "]");
                        jVar.a(str, i5, i2, au, i6);
                        if (this.vW != null) {
                            ProfileInfo profileInfo = new ProfileInfo();
                            profileInfo.imsi = str;
                            profileInfo.province = i5;
                            profileInfo.city = i2;
                            profileInfo.carry = au;
                            profileInfo.brand = i6;
                            Message obtainMessage = this.vW.obtainMessage(4102, i, 0);
                            obtainMessage.obj = profileInfo;
                            this.vW.sendMessage(obtainMessage);
                            break;
                        }
                    }
                }
                break;
            case 49:
                f.d("TrafficCorrection", "校正失败");
                JceStruct boVar = new bo();
                if (a(beVar.cx, boVar)) {
                    int a = boVar.a();
                    String str3 = "";
                    if (a == 0) {
                        str3 = "短信校正失败";
                    } else if (a == 1 && !n(i, 0)) {
                        str3 = "不支持运营商接口，强制拉取profile回调";
                        return true;
                    } else {
                        str3 = a != 1 ? "服务器返回未知类型" : "运营商合作校正失败";
                    }
                    f.d("TrafficCorrection", "type of error is " + str3);
                }
                if (this.vW != null) {
                    this.vW.sendMessage(this.vW.obtainMessage(4103, i, -1));
                    break;
                }
                break;
        }
        return z;
        z = false;
        return z;
    }

    private boolean a(byte[] bArr, JceStruct jceStruct) {
        if (bArr == null || jceStruct == null) {
            return false;
        }
        boolean z;
        JceInputStream jceInputStream = new JceInputStream(bArr);
        jceInputStream.setServerEncoding("UTF-8");
        try {
            jceStruct.readFrom(jceInputStream);
            z = true;
        } catch (Exception e) {
            e.printStackTrace();
            z = false;
        }
        return z;
    }

    private int ap(int -l_22_I) {
        f.d("TrafficCorrection", "[profile上报][Beg]");
        j jVar = new j(-l_22_I);
        final String df = jVar.df();
        final String dg = jVar.dg();
        final String dh = jVar.dh();
        final String di = jVar.di();
        final String at = at(-l_22_I);
        final int dj = jVar.dj();
        try {
            int intValue = Integer.valueOf(df).intValue();
            int intValue2 = Integer.valueOf(dg).intValue();
            int intValue3 = Integer.valueOf(di).intValue();
            int be = be(dh);
            if (be != -1) {
                int i;
                int i2;
                int i3;
                int i4;
                int i5;
                int i6;
                final int i7 = -l_22_I;
                jn.cx().a(new jl() {
                    public void a(ArrayList<JceStruct> arrayList, int i) {
                        i.this.vW.sendMessage(i.this.vW.obtainMessage(4099, i7, 0, this));
                        Object obj = "";
                        if (i == 0) {
                            obj = i.this.vX.b() + "$" + at + "$" + df + dg + dh + di + "$" + dj;
                        }
                        i.this.vW.sendMessage(i.this.vW.obtainMessage(4100, i7, 0, obj));
                        f.d("TrafficCorrection", "profile上报结果[" + i + "]guid:[" + i.this.vX.b() + "]");
                    }
                });
                if (1 != -l_22_I) {
                    i = 2003;
                    i2 = 2002;
                    i3 = 2004;
                    i4 = 2005;
                    i5 = 2007;
                    i6 = 2008;
                } else {
                    i = 2011;
                    i2 = 2010;
                    i3 = 2012;
                    i4 = 2013;
                    i5 = 2015;
                    i6 = 2016;
                }
                jn.cx().l(i2, Integer.valueOf(intValue).intValue());
                jn.cx().l(i, Integer.valueOf(intValue2).intValue());
                jn.cx().l(i3, be);
                jn.cx().l(i4, Integer.valueOf(intValue3).intValue());
                jn.cx().l(i5, dj);
                jn.cx().a(i6, true);
                f.d("TrafficCorrection", "[profile上报][End]");
                return 0;
            }
            f.d("TrafficCorrection", "[error] upload profile Operator error");
            return TrafficErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
        } catch (NumberFormatException e) {
            f.d("TrafficCorrection", "[error] upload profile NumberFormatException:" + e.getMessage());
            return TrafficErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
        }
    }

    private boolean aq(int i) {
        j jVar = new j(i);
        String df = jVar.df();
        String dg = jVar.dg();
        String dh = jVar.dh();
        String di = jVar.di();
        f.d("TrafficCorrection", "[检查省、市、运营商、品牌代码]province:[" + df + "]city:[" + dg + "]carry:[" + dh + "]brand:[" + di + "]");
        if (!q.cK(df) && !q.cK(dg) && !q.cK(dh) && !q.cK(di)) {
            return true;
        }
        f.d("TrafficCorrection", "[error]省、市、运营商、品牌代码存在为空");
        return false;
    }

    private boolean ar(int i) {
        j jVar = new j(i);
        String str = this.vX.b() + "$" + at(i) + "$" + jVar.df() + jVar.dg() + jVar.dh() + jVar.di() + "$" + jVar.dj();
        String dk = jVar.dk();
        f.h("TrafficCorrection", "currentInfo:[" + str + "]lastSuccessInfo:[" + dk + "]");
        return str.compareTo(dk) != 0;
    }

    private boolean as(int i) {
        j jVar = new j(i);
        CharSequence dm = jVar.dm();
        CharSequence dn = jVar.dn();
        f.d("TrafficCorrection", "[检查查询码与端口号]queryCode:[" + dm + "]queryPort:[" + dn + "]");
        if (!TextUtils.isEmpty(dm) && !TextUtils.isEmpty(dn)) {
            return true;
        }
        f.d("TrafficCorrection", "[error]查询码或端口号不合法");
        return false;
    }

    private String at(int i) {
        String str = "";
        if (im.bO() != null) {
            str = im.bO().getIMSI(i);
        } else if (i == 0) {
            str = l.M(TMSDKContext.getApplicaionContext());
        }
        f.d("TrafficCorrection", "getIMSIBySimSlot:[" + i + "][" + str + "");
        return str;
    }

    private String au(int i) {
        String str = "";
        if (i == 2) {
            return "CMCC";
        }
        if (i != 1) {
            return i != 3 ? str : "TELECOM";
        } else {
            return "UNICOM";
        }
    }

    private String av(int i) {
        switch (i) {
            case 1001:
                return "通过查询码获取到流量短信处理";
            case 1002:
                return "本地校正后上报";
            case 1003:
                return "手动修改上报";
            case 1004:
                return "身份验证";
            case 1007:
                return "手动获取云端数据";
            case 1008:
                return "纠错上报";
            default:
                return "";
        }
    }

    private String aw(int i) {
        switch (i) {
            case 1:
                return "校正类型";
            case 2:
                return "调整校正频率：例如一天校正一次调整为3天校正一次";
            case 3:
                return "复活指令：关闭校正的用户复活。";
            case 4:
                return "直接替换终端当前使用的查询码：换查询码时使用";
            case 5:
                return "暂停校正";
            case 6:
                return "下发模板";
            case 7:
                return "调整校正时机:允许server校正的时间段调整";
            case 8:
                return "替换超时时间";
            case 9:
                return "更换监听运营商端口。";
            case 10:
                return "下发GPRS流量值";
            case 11:
                return "立即执行一次校正";
            case 12:
                return "下发身份认证信息(url+sky)，终端收到该信息后进行省份认证";
            case 13:
                return "下发TD流量值";
            case 14:
                return "下发闲时流量值";
            case 15:
                return "下发一串内容，这串内容需要终端展示给用户看；";
            case 16:
                return "调整校正时机:允许Local校正的时间段调整";
            case 17:
                return "下发推广链接";
            case 20:
                return "流量详情";
            case 21:
                return "下发profile";
            case 49:
                return "校正失败";
            default:
                return "";
        }
    }

    private void ax(int i) {
        f.f("TrafficCorrection", "setQueryInfo, simIndex is " + i);
        j jVar = new j(i);
        jVar.n(false);
        jVar.m(false);
    }

    /* JADX WARNING: Missing block: B:7:0x003a, code:
            if (9 != r13) goto L_0x003c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int b(int i, String str, String str2) {
        f.d("TrafficCorrection", "本地模板分析短信");
        if (TrafficSmsParser.getWrongSmsType(str, str2) == 0) {
            j jVar = new j(i);
            List aB = jVar.aB(2);
            List aB2 = jVar.aB(1);
            if (aB.isEmpty() && aB2.isEmpty()) {
                f.d("TrafficCorrection", "模板为空");
                o(i, TrafficErrorCode.ERR_CORRECTION_LOCAL_NO_TEMPLATE);
                a(i, 3, str2);
                return 0;
            }
            Object obj = null;
            int i2 = 9;
            if (aB2.isEmpty()) {
                f.d("TrafficCorrection", "剩余模板为空");
                obj = 1;
            } else {
                i2 = a(i, aB2, str, str2, false);
            }
            if (!(obj == null || aB.isEmpty())) {
                i2 = a(i, aB, str, str2, true);
            }
            if (i2 == 6 || i2 == 7) {
                f.d("TrafficCorrection", "匹配成功");
                o(i, 0);
            } else {
                o(i, TrafficErrorCode.ERR_CORRECTION_LOCAL_TEMPLATE_UNMATCH);
                a(i, i2, str2);
                f.d("TrafficCorrection", "匹配失败");
            }
            return 0;
        }
        f.d("TrafficCorrection", "[error]TrafficSmsParser.getWrongSmsType异常");
        o(i, TrafficErrorCode.ERR_CORRECTION_BAD_SMS);
        return TrafficErrorCode.ERR_CORRECTION_BAD_SMS;
    }

    private int be(String str) {
        if ("CMCC".equals(str)) {
            return 2;
        }
        if ("UNICOM".equals(str)) {
            return 1;
        }
        return !"TELECOM".equals(str) ? -1 : 3;
    }

    private synchronized void de() {
        f.d("TrafficCorrection", "[注册push listener]");
        this.vX.v(11006, 2);
        this.vX.a(11006, new bh(), 2, this.wb);
    }

    private String j(byte[] bArr) {
        return bArr != null ? new String(bArr) : "";
    }

    private void m(final int -l_6_I, int i) {
        int i2 = 0;
        f.d("TrafficCorrection", "[uploadParam]simIndex:[" + -l_6_I + "]");
        j jVar = new j(-l_6_I);
        bg bgVar = new bg();
        if (jVar.do() && jVar.dp()) {
            bgVar.cC = jVar.dm();
            bgVar.port = jVar.dn();
        } else {
            bgVar.cC = "";
            bgVar.port = "";
        }
        JceStruct bqVar = new bq();
        bqVar.imsi = at(-l_6_I);
        bqVar.de = jVar.dq();
        bqVar.di = jVar.dr();
        bqVar.dd = jVar.ds();
        bqVar.dh = jVar.dt();
        bqVar.dg = bgVar;
        if (jVar.du()) {
            i2 = 2;
        }
        bqVar.status = i2;
        bqVar.dj = i;
        bqVar.df = jVar.dl();
        bqVar.cj = -l_6_I;
        f.d("TrafficCorrection", "[upload]-[" + av(1008) + "],内容：[" + a(1008, bqVar) + "]");
        this.vX.a(1008, bqVar, null, 2, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                if (i3 != 0) {
                    i.this.o(-l_6_I, TrafficErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL);
                }
            }
        });
    }

    private void o(int i, int i2) {
        if (i2 != 0) {
            this.vW.sendMessage(this.vW.obtainMessage(4101, i, i2));
        }
        f.d("TrafficCorrection", "[本次校正流程结束]--重置状态");
    }

    public int a(final int i, String str, final String str2, final String -l_9_R, int i2) {
        f.d("TrafficCorrection", "[分析短信]analysisSMS--simIndex:[" + i + "]queryCode:[" + str + "]queryPort:" + str2 + "]smsBody:[" + -l_9_R + "]");
        if (!(i == 0 || i == 1) || q.cK(str) || q.cK(str2) || q.cK(-l_9_R)) {
            o(i, -6);
            f.d("TrafficCorrection", "参数错误");
            return -6;
        }
        bg bgVar = new bg();
        if (bgVar != null) {
            bgVar.cC = str;
            bgVar.port = str2;
        }
        if (!tmsdk.common.utils.i.hm()) {
            return b(i, str2, -l_9_R);
        }
        f.d("TrafficCorrection", "有网络，走云短信");
        JceStruct bdVar = new bd();
        bdVar.imsi = at(i);
        bdVar.cm = bgVar;
        bdVar.sms = -l_9_R;
        bdVar.cp = this.vZ;
        bdVar.time = i2;
        bdVar.ck = new ArrayList();
        bdVar.cj = i;
        f.d("TrafficCorrection", "[upload]-[" + av(1001) + "]内容:[" + a(1001, bdVar) + "]");
        this.vX.a(1001, bdVar, null, 2, new jy() {
            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                if (i3 != 0) {
                    f.d("TrafficCorrection", "有网络，上报短信流程失败，走本地短信分析流程");
                    i.this.b(i, str2, -l_9_R);
                }
            }
        });
        return 0;
    }

    public int getSingletonType() {
        return 1;
    }

    public boolean n(int i, int i2) {
        Object dq = new j(i).dq();
        if (TextUtils.isEmpty(dq)) {
            return false;
        }
        String[] split = dq.replace("||", "*").split("\\*");
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
        f.d("TrafficCorrection", "TrafficCorrectionManagerImpl-OnCreate-context:[" + context + "]");
        this.mContext = context;
        this.vX = im.bK();
        de();
        jk.cv().a(jn.cx());
        jk.cv().ah(jn.cx().cs());
        this.vW = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message message) {
                if (message.what == 4097) {
                    a aVar = (a) message.obj;
                    if (i.this.vY != null && aVar != null) {
                        f.d("TrafficCorrection", "onTrafficInfoNotify--simIndex:[" + aVar.wt + "]trafficClass:[" + aVar.wu + "]" + "]subClass:[" + aVar.wv + "]" + "]kBytes:[" + aVar.ww + "]");
                        i.this.vY.onTrafficInfoNotify(aVar.wt, aVar.wu, aVar.wv, aVar.ww);
                    }
                } else if (message.what != 4098) {
                    if (message.what == 4099) {
                        jn.cx().b((jl) message.obj);
                    } else if (message.what == 4100) {
                        if (TextUtils.isEmpty((String) message.obj)) {
                            f.d("TrafficCorrection", "onError--simIndex:[" + message.arg1 + "]ERR_CORRECTION_PROFILE_UPLOAD_FAIL");
                            if (i.this.vY != null) {
                                i.this.vY.onError(message.arg1, TrafficErrorCode.ERR_CORRECTION_PROFILE_UPLOAD_FAIL);
                            }
                        }
                        new j(message.arg1).bj((String) message.obj);
                    } else if (message.what != 4101) {
                        if (message.what != 4102) {
                            if (message.what != 4103) {
                                if (message.what == 4104) {
                                    ArrayList arrayList = (ArrayList) message.obj;
                                    if (i.this.vY != null && arrayList != null) {
                                        f.d("TrafficCorrection", "onDetailInfoNotify--");
                                        i.this.vY.onDetailInfoNotify(arrayList);
                                    }
                                }
                            } else if (i.this.vY != null) {
                                f.d("TrafficCorrection", "onCorrectionResult--simIndex:[" + message.arg1 + "]retCode:[" + message.arg2 + "]");
                                i.this.vY.onCorrectionResult(message.arg1, message.arg2);
                            }
                        } else if (i.this.vY != null) {
                            f.d("TrafficCorrection", "onProfileNotify--simIndex:[" + message.arg1 + "]ProfileInfo:[" + ((ProfileInfo) message.obj).toString() + "]");
                            i.this.vY.onProfileNotify(message.arg1, (ProfileInfo) message.obj);
                        }
                    } else if (i.this.vY != null) {
                        f.d("TrafficCorrection", "onError--simIndex:[" + message.arg1 + "]errorCode:[" + message.arg2 + "]");
                        i.this.vY.onError(message.arg1, message.arg2);
                    }
                } else if (i.this.vY != null) {
                    j jVar = new j(message.arg1);
                    String dm = jVar.dm();
                    String dn = jVar.dn();
                    f.d("TrafficCorrection", "onNeedSmsCorrection--simIndex:[" + message.arg1 + "]queryCode:[" + dm + "]queryPort:[" + dn + "]");
                    i.this.vY.onNeedSmsCorrection(message.arg1, dm, dn);
                }
            }
        };
    }

    public void onImsiChanged() {
        f.d("TrafficCorrection", "onImsiChanged");
        TMSDKContext.onImsiChanged();
        if (tmsdk.common.utils.i.hm()) {
            this.vX.gm();
        }
        if (aq(0) && ar(0)) {
            ax(0);
            ap(0);
        }
        if (aq(1) && ar(1)) {
            ax(1);
            ap(1);
        }
    }

    public int requestProfile(int i) {
        f.d("TrafficCorrection", "requestProfile--simIndex:[" + i + "]");
        ProfileInfo d = new j(i).d(i, at(i));
        if (d.province != -1) {
            Message obtainMessage = this.vW.obtainMessage(4102, i, 0);
            obtainMessage.obj = d;
            this.vW.sendMessage(obtainMessage);
        } else {
            f.d("TrafficCorrection", "本地没有profile信息");
            m(i, 5);
        }
        return 0;
    }

    public int setConfig(int i, String str, String str2, String str3, String str4, int i2) {
        f.d("TrafficCorrection", "[设置省、市、运营商、品牌代码]simIndex:[" + i + "]provinceId:[" + str + "]cityId:[" + str2 + "]carryId:[" + str3 + "]brandId:[" + str4 + "]closingDay:[" + i2 + "]");
        if (!(i == 0 || i == 1) || q.cK(str) || q.cK(str2) || q.cK(str3) || q.cK(str4)) {
            f.d("TrafficCorrection", "[error]设置信息有的为空");
            return -6;
        }
        j jVar = new j(i);
        jVar.bf(str);
        jVar.bg(str2);
        jVar.bh(str3);
        jVar.bi(str4);
        jVar.ay(i2);
        return 0;
    }

    public int setTrafficCorrectionListener(ITrafficCorrectionListener iTrafficCorrectionListener) {
        f.d("TrafficCorrection", "[设置流量校正监听]listener:[" + iTrafficCorrectionListener + "]");
        if (iTrafficCorrectionListener == null) {
            return -6;
        }
        this.vY = iTrafficCorrectionListener;
        return 0;
    }

    public int startCorrection(int i) {
        f.f("TrafficCorrection", "先检查vid");
        nl gl = this.vX.gl();
        if (gl.aH()) {
            String aJ = gl.aJ();
            if (aJ.equals("")) {
                f.f("TrafficCorrection", "支持vid, 没拿到vid：isSupportVid is " + gl.aH() + ", onGetVidFromPhone is " + aJ);
                if (this.mRetryCount < 10) {
                    this.mRetryCount++;
                    this.vX.gB();
                    return TrafficErrorCode.ERR_CORRECTION_NEED_RETRY;
                } else if (gl.aF().equals("")) {
                    this.mRetryCount = 0;
                    return TrafficErrorCode.ERR_CORRECTION_NEED_RETRY;
                }
            }
        }
        f.f("TrafficCorrection", "不支持vid");
        this.mRetryCount = 0;
        f.d("TrafficCorrection", "[开始校正]simIndex:[ " + i + "]");
        s.bW(128);
        if (at(i) != null) {
            if (tmsdk.common.utils.i.hm()) {
                this.vX.gm();
            }
            if (i != 0 && i != 1) {
                f.d("TrafficCorrection", "[error]simIndex 不合法");
                return -6;
            } else if (aq(i)) {
                j jVar = new j(i);
                if (ar(i)) {
                    f.d("TrafficCorrection", "[需要上报profile][上报profile触发后续校正流程]");
                    if (!tmsdk.common.utils.i.hm()) {
                        f.d("TrafficCorrection", "没有网络-[profile上报]结束");
                        return TrafficErrorCode.ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL;
                    } else if (ap(i) != 0) {
                        return TrafficErrorCode.ERR_CORRECTION_PROFILE_UPLOAD_FAIL;
                    }
                }
                boolean hm = tmsdk.common.utils.i.hm();
                if (hm) {
                    if (hm) {
                        if (ap(i) != 0) {
                            f.d("TrafficCorrection", "[无校正方式，上报profile]simIndex:[" + i + "]");
                        }
                        if (q.cK(jVar.dq())) {
                            f.d("TrafficCorrection", "[无校正方式，纠错上报]simIndex:[" + i + "]");
                            m(i, 5);
                            return TrafficErrorCode.ERR_CORRECTION_NEED_RETRY;
                        }
                        f.f("TrafficCorrection", "simIndex is " + i);
                        f.f("TrafficCorrection", "IsPortFreshed is " + jVar.do());
                        f.f("TrafficCorrection", "IsCodeFreshed is " + jVar.dp());
                        if (!(jVar.do() && jVar.dp())) {
                            f.d("TrafficCorrection", "查询码和端口号没有更新");
                            JceStruct baVar = new ba();
                            baVar.aH = 0;
                            baVar.cj = i;
                            baVar.imsi = at(i);
                            f.d("TrafficCorrection", "[upload]-[" + av(1007) + "]内容:[" + a(1007, baVar));
                            this.vX.a(1007, baVar, null, 2, null);
                            return 0;
                        }
                    }
                    if (n(i, 0)) {
                        f.d("TrafficCorrection", "[运营商云端合作校正]simIndex:[" + i + "]");
                        JceStruct baVar2 = new ba();
                        baVar2.aH = 0;
                        baVar2.cj = i;
                        baVar2.imsi = at(i);
                        f.d("TrafficCorrection", "[upload]-[" + av(1007) + "]内容:[" + a(1007, baVar2));
                        this.vX.a(1007, baVar2, null, 2, null);
                    } else if (n(i, 3)) {
                        f.d("TrafficCorrection", "[短信云端校正]simIndex:[" + i + "]");
                        a(i, 2, 3, 5);
                    }
                } else {
                    f.d("TrafficCorrection", "[本地模板匹配]simIndex:[" + i + "]");
                    a(i, 2, 2, 5);
                    return 0;
                }
                return 0;
            } else {
                f.d("TrafficCorrection", "[error]ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL");
                return TrafficErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL;
            }
        }
        f.d("TrafficCorrection", "imsi为null, 直接返回");
        return -7;
    }
}
