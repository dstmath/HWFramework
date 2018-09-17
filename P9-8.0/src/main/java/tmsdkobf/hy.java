package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import tmsdk.bg.module.aresengine.ISmsReportCallBack;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdk.common.utils.q;

public class hy {
    private static cp a(SmsEntity smsEntity) {
        cp cpVar = new cp();
        cpVar.fr = null;
        cpVar.fk = (int) (System.currentTimeMillis() / 1000);
        cpVar.sender = q.cI(smsEntity.getAddress());
        cpVar.sms = q.cI(smsEntity.getBody());
        cpVar.fs = smsEntity.protocolType;
        cpVar.fm = -1;
        cpVar.fn = -1;
        cpVar.fl = -1;
        cpVar.fp = -1;
        cpVar.fq = new ArrayList();
        cpVar.fo = new ArrayList();
        cpVar.ft = 0;
        cpVar.fu = null;
        return cpVar;
    }

    private static void a(ArrayList<cp> arrayList, ISmsReportCallBack iSmsReportCallBack) {
        if (arrayList.size() > 0 && i.hm()) {
            JceStruct ckVar = new ck();
            ckVar.eZ = arrayList;
            im.bK().a(801, ckVar, null, 0, iSmsReportCallBack, 180000);
            return;
        }
        f.h("SmsReport", "not connected!");
        iSmsReportCallBack.onReprotFinish(-52);
    }

    public static void reportRecoverSms(LinkedHashMap<SmsEntity, Integer> linkedHashMap, ISmsReportCallBack iSmsReportCallBack) {
        ArrayList arrayList = new ArrayList();
        for (SmsEntity smsEntity : linkedHashMap.keySet()) {
            cp a = a(smsEntity);
            cs csVar = new cs();
            if (((Integer) linkedHashMap.get(smsEntity)).intValue() != 0) {
                csVar.fP = 12;
            } else {
                csVar.fP = 24;
            }
            csVar.time = (int) (System.currentTimeMillis() / 1000);
            arrayList.add(a);
        }
        a(arrayList, iSmsReportCallBack);
    }
}
