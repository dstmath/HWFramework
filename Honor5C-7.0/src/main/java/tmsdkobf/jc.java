package tmsdkobf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import tmsdk.bg.module.aresengine.ISmsReportCallBack;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class jc {
    private static cb a(SmsEntity smsEntity) {
        cb cbVar = new cb();
        cbVar.ew = null;
        cbVar.ep = (int) (System.currentTimeMillis() / 1000);
        cbVar.sender = l.dk(smsEntity.getAddress());
        cbVar.sms = l.dk(smsEntity.getBody());
        cbVar.ex = smsEntity.protocolType;
        cbVar.er = -1;
        cbVar.es = -1;
        cbVar.eq = -1;
        cbVar.eu = -1;
        cbVar.ev = new ArrayList();
        cbVar.et = new ArrayList();
        cbVar.ey = 0;
        cbVar.ez = null;
        return cbVar;
    }

    private static void a(ArrayList<cb> arrayList, ISmsReportCallBack iSmsReportCallBack) {
        if (arrayList.size() > 0 && f.hv()) {
            fs bwVar = new bw();
            bwVar.ee = arrayList;
            jq.cu().a(801, bwVar, null, 0, iSmsReportCallBack, 180000);
            return;
        }
        d.g("SmsReport", "not connected!");
        iSmsReportCallBack.onReprotFinish(-52);
    }

    public static void reportRecoverSms(LinkedHashMap<SmsEntity, Integer> linkedHashMap, ISmsReportCallBack iSmsReportCallBack) {
        ArrayList arrayList = new ArrayList();
        for (SmsEntity smsEntity : linkedHashMap.keySet()) {
            cb a = a(smsEntity);
            ce ceVar = new ce();
            if (((Integer) linkedHashMap.get(smsEntity)).intValue() != 0) {
                ceVar.eV = 12;
            } else {
                ceVar.eV = 24;
            }
            ceVar.time = (int) (System.currentTimeMillis() / 1000);
            arrayList.add(a);
        }
        a(arrayList, iSmsReportCallBack);
    }
}
