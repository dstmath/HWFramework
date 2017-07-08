package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;
import tmsdk.common.utils.i;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;

/* compiled from: Unknown */
public class hb {
    private static boolean qp;
    gu qi;
    List<gm> qj;
    gm qk;
    List<gm> ql;
    List<gm> qm;
    Map<String, List<Integer>> qn;
    private boolean qo;
    String[] qq;

    public hb() {
        this.qj = new ArrayList();
        this.qn = new HashMap();
        this.qo = new ge().aG();
    }

    private boolean a(gt gtVar) {
        if (gtVar == null || this.qn == null) {
            return false;
        }
        List list = (List) this.qn.get(gtVar.ba().getPackageName());
        if (list == null) {
            list = new ArrayList();
            list.add(Integer.valueOf(gtVar.ba().hB()));
            this.qn.put(gtVar.ba().getPackageName(), list);
            return false;
        } else if (list.indexOf(Integer.valueOf(gtVar.ba().hB())) != -1) {
            gtVar.an(12);
            return true;
        } else {
            list.add(Integer.valueOf(gtVar.ba().hB()));
            return false;
        }
    }

    private List<gv> aV(String str) {
        if (str == null) {
            return null;
        }
        try {
            byte[] aE = gs.aW().aE(str);
            if (aE == null) {
                return null;
            }
            fq fqVar = new fq(aE);
            fqVar.ae("UTF-8");
            ah ahVar = new ah();
            ahVar.readFrom(fqVar);
            aE = ahVar.aW;
            if (aE == null) {
                return null;
            }
            fqVar = new fq(TccCryptor.decrypt(aE, null));
            fqVar.ae("UTF-8");
            ag agVar = new ag();
            agVar.readFrom(fqVar);
            List<gv> arrayList = new ArrayList();
            Iterator it = agVar.aT.iterator();
            while (it.hasNext()) {
                Map map = (Map) it.next();
                gv gvVar;
                if (map.get(Integer.valueOf(3)) != null) {
                    gvVar = new gv();
                    gvVar.om = str;
                    gvVar.pw = Integer.valueOf((String) map.get(Integer.valueOf(9))).intValue();
                    gvVar.op = new ArrayList();
                    gvVar.op.add(((String) map.get(Integer.valueOf(3))).toLowerCase());
                    gvVar.mDescription = !qp ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                    if (qp && TextUtils.isEmpty((CharSequence) map.get(Integer.valueOf(18)))) {
                        gvVar.mDescription = "Data Cache";
                    }
                    String str2 = (String) map.get(Integer.valueOf(10));
                    if (!(str2 == null || str2.equals(""))) {
                        int intValue = Integer.valueOf((String) map.get(Integer.valueOf(10))).intValue();
                        if (intValue > 0) {
                            if (qp) {
                                gvVar.mDescription += "(" + String.format(i.dh("eng_in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                            } else {
                                gvVar.mDescription += "(" + String.format(i.dh("cn_in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                            }
                            gvVar.pe = "0," + intValue;
                            gv gvVar2 = new gv();
                            gvVar2.om = gvVar.om;
                            gvVar2.pw = 1;
                            gvVar2.op = new ArrayList();
                            gvVar2.op.addAll(gvVar.op);
                            gvVar2.mDescription = !qp ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                            if (qp && TextUtils.isEmpty((CharSequence) map.get(Integer.valueOf(18)))) {
                                gvVar2.mDescription = "Data Cache";
                            }
                            if (qp) {
                                gvVar.mDescription += "(" + String.format(i.dh("eng_days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                            } else {
                                gvVar.mDescription += "(" + String.format(i.dh("cn_days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                            }
                            arrayList.add(gvVar2);
                        }
                    }
                    arrayList.add(gvVar);
                } else if (map.get(Integer.valueOf(4)) != null) {
                    gvVar = new gv();
                    gvVar.om = str;
                    gvVar.pw = Integer.valueOf((String) map.get(Integer.valueOf(9))).intValue();
                    gvVar.op = new ArrayList();
                    gvVar.op.add(((String) map.get(Integer.valueOf(4))).toLowerCase());
                    gvVar.mDescription = !qp ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                    if (qp && TextUtils.isEmpty((CharSequence) map.get(Integer.valueOf(18)))) {
                        gvVar.mDescription = "Data Cache";
                    }
                    gvVar.mFileName = (String) map.get(Integer.valueOf(11));
                    gvVar.pb = (String) map.get(Integer.valueOf(12));
                    gvVar.pc = (String) map.get(Integer.valueOf(13));
                    gvVar.pd = (String) map.get(Integer.valueOf(14));
                    gvVar.pe = (String) map.get(Integer.valueOf(15));
                    arrayList.add(gvVar);
                }
            }
            return arrayList;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean bo() {
        return qp;
    }

    private boolean bp() {
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.intToString(40248) + ".dat", UpdateConfig.intToString(40248), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            return false;
        }
        HashMap aV = gr.aV();
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            gm gmVar = new gm();
            gmVar.pa = cwVar.gp;
            if (qp) {
                gmVar.mDescription = (String) aV.get(cwVar.gq);
                gmVar.pg = !TextUtils.isEmpty(cwVar.gr) ? (String) aV.get(cwVar.gr) : null;
            }
            if (gmVar.mDescription == null) {
                gmVar.mDescription = cwVar.gq;
            }
            if (gmVar.pg == null) {
                gmVar.pg = !TextUtils.isEmpty(cwVar.gr) ? cwVar.gr : null;
            }
            gmVar.ph = !"1".equals(cwVar.gs);
            gmVar.pf = !TextUtils.isEmpty(cwVar.gu) ? cwVar.gu : "0";
            if (!TextUtils.isEmpty(cwVar.gt)) {
                String[] split = cwVar.gt.split("&");
                if (split != null) {
                    for (String str : split) {
                        String str2;
                        if (str2.length() > 2) {
                            char charAt = str2.charAt(0);
                            str2 = str2.substring(2);
                            switch (charAt) {
                                case SmsCheckResult.ESC_TEL_95013 /*49*/:
                                    gmVar.mFileName = str2;
                                    break;
                                case SmsCheckResult.ESC_TEL_OTHER /*50*/:
                                    gmVar.pb = str2;
                                    break;
                                case '3':
                                    gmVar.pc = str2;
                                    break;
                                case '4':
                                    gmVar.pd = str2;
                                    break;
                                case '5':
                                    gmVar.pe = str2;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
            if ("1".equals(cwVar.go)) {
                if (this.qj == null) {
                    this.qj = new ArrayList();
                }
                this.qj.add(gmVar);
            } else if ("2".equals(cwVar.go)) {
                if (this.ql == null) {
                    this.ql = new ArrayList();
                }
                this.ql.add(gmVar);
            } else if ("3".equals(cwVar.go)) {
                if (this.qm == null) {
                    this.qm = new ArrayList();
                }
                this.qm.add(gmVar);
            }
        }
        return true;
    }

    public static void s(boolean z) {
        qp = z;
    }

    public void a(Context context) {
        if (this.qo) {
            d.c("deepclean", "cloud list V2");
            this.qi = new gu();
            this.qi.pu = gs.aW().aX();
        } else {
            d.c("deepclean", "local list");
            this.qi = gr.a(context, qp);
        }
        if (this.qi != null) {
            if (this.qi.pt == null) {
                this.qi.pt = new HashSet();
            }
            if (this.qq != null) {
                for (String toLowerCase : this.qq) {
                    this.qi.pt.add(toLowerCase.toLowerCase());
                    d.d("whitepath", "truePath");
                }
            }
        } else {
            d.c("deepclean", "rull is null");
        }
        bp();
        this.qk = new gm();
        this.qk.mFileName = ".apk";
        this.qk.pf = "0";
        this.qj.add(this.qk);
        if (this.ql != null && this.ql.size() > 0) {
            this.qk.pf = ((gm) this.ql.get(0)).pf;
        }
    }

    public void a(String[] strArr) {
        this.qq = strArr;
    }

    public gw aT(String str) {
        if (str == null || this.qi == null || this.qi.pu == null) {
            return null;
        }
        String toLowerCase = str.toLowerCase();
        if (!this.qo) {
            return (gw) this.qi.pu.get(toLowerCase);
        }
        gw gwVar = (gw) this.qi.pu.get(toLowerCase);
        if (gwVar == null) {
            gwVar = new gw();
            gwVar.om = str;
            if (this.qo) {
                gwVar.op = aV(toLowerCase);
            }
            gwVar.pA = gs.aW().aQ(toLowerCase);
            if (gwVar.op == null || gwVar.pA == null || gwVar.pA.size() == 0) {
                return null;
            }
            this.qi.pu.put(toLowerCase, gwVar);
        }
        return gwVar;
    }

    public gm aU(String str) {
        for (gm gmVar : this.qj) {
            if (str.equals(gmVar.oZ)) {
                return gmVar;
            }
        }
        return null;
    }

    public gt aW(String str) {
        if (str == null) {
            return null;
        }
        fy aJ = go.aJ(str);
        if (aJ == null) {
            return null;
        }
        gt gtVar = new gt();
        gtVar.aS(aJ.aZ());
        gtVar.b(aJ);
        gtVar.an(aJ.al());
        gtVar.setSize(aJ.getSize());
        if (aJ.al() != 6 && a(gtVar)) {
            a(gtVar);
        }
        switch (gtVar.al()) {
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
            case UrlCheckType.MAKE_MONEY /*9*/:
            case UrlCheckType.MSG_REACTIONARY /*12*/:
                gtVar.setStatus(1);
                break;
        }
        return gtVar;
    }

    public gm bq() {
        return this.qk;
    }

    public List<gm> br() {
        return this.ql;
    }

    public List<gm> bs() {
        return this.qj;
    }

    public List<gm> bt() {
        return this.qm;
    }

    public String[] bu() {
        if (this.qi == null || this.qi.pu == null) {
            return null;
        }
        return (String[]) this.qi.pu.keySet().toArray(new String[0]);
    }

    public String[] bv() {
        if (this.qi == null || this.qi.pt == null) {
            return null;
        }
        return (String[]) this.qi.pt.toArray(new String[0]);
    }
}
