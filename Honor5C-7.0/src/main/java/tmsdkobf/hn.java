package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.urlcheck.UrlCheckType;

/* compiled from: Unknown */
public class hn {
    private static hg a(cw cwVar) {
        hg hgVar = new hg();
        hgVar.pa = cwVar.gp;
        if (!TextUtils.isEmpty(cwVar.gq)) {
            String[] split = cwVar.gq.split("&");
            if (split != null) {
                for (String str : split) {
                    String str2;
                    if (str2.length() > 2) {
                        char charAt = str2.charAt(0);
                        str2 = str2.substring(2);
                        switch (charAt) {
                            case SmsCheckResult.ESC_TEL_95013 /*49*/:
                                hgVar.mFileName = str2;
                                break;
                            case SmsCheckResult.ESC_TEL_OTHER /*50*/:
                                hgVar.pb = str2;
                                break;
                            case '3':
                                hgVar.pc = str2;
                                break;
                            case '4':
                                hgVar.pd = str2;
                                break;
                            case '5':
                                hgVar.pe = str2;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(cwVar.gr)) {
            hgVar.mPlayers = cwVar.gr.split("&");
        }
        if (!TextUtils.isEmpty(cwVar.gs)) {
            hgVar.mAdapter = cwVar.gs;
        }
        return hgVar;
    }

    private static void a(List<hg> list, hg hgVar) {
        if (TextUtils.isEmpty(hgVar.pa)) {
            list.add(hgVar);
            return;
        }
        int i = 0;
        while (i < list.size() && !TextUtils.isEmpty(((hg) list.get(i)).pa)) {
            i++;
        }
        list.add(i, hgVar);
    }

    public static List<hg> bx() {
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.PROCESSMANAGER_WHITE_LIST_NAME, UpdateConfig.intToString(40006), new cx(), "UTF-8");
        List<hg> arrayList = new ArrayList();
        if (cxVar == null || cxVar.gw == null) {
            return arrayList;
        }
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if (cwVar.go != null) {
                try {
                    switch (Integer.valueOf(cwVar.go).intValue()) {
                        case UrlCheckType.STEAL_ACCOUNT /*5*/:
                            hg a = a(cwVar);
                            if (a == null) {
                                break;
                            }
                            a(arrayList, a);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return arrayList;
    }
}
