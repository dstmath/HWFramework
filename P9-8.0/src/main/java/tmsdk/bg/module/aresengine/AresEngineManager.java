package tmsdk.bg.module.aresengine;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.ErrorCode;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdkobf.hh;
import tmsdkobf.ic;
import tmsdkobf.kt;

public final class AresEngineManager extends BaseManagerB {
    private hh tP;
    private Map<String, DataInterceptor<? extends TelephonyEntity>> tQ;
    private b tR;

    public void addInterceptor(DataInterceptorBuilder<? extends TelephonyEntity> dataInterceptorBuilder) throws RuntimeException {
        if (!ic.bE()) {
            this.tP.addInterceptor(dataInterceptorBuilder);
        }
    }

    public DataInterceptor<? extends TelephonyEntity> findInterceptor(String str) {
        if (!ic.bE()) {
            return this.tP.findInterceptor(str);
        }
        if (this.tQ == null) {
            this.tQ = new HashMap();
            String[] strArr = new String[]{DataInterceptorBuilder.TYPE_INCOMING_CALL, DataInterceptorBuilder.TYPE_INCOMING_SMS, DataInterceptorBuilder.TYPE_OUTGOING_SMS, DataInterceptorBuilder.TYPE_SYSTEM_CALL};
            String[] strArr2 = strArr;
            int length = strArr.length;
            for (int i = 0; i < length; i++) {
                String str2 = strArr2[i];
                this.tQ.put(str2, new a(str2));
            }
        }
        return (DataInterceptor) this.tQ.get(str);
    }

    public AresEngineFactor getAresEngineFactor() {
        return this.tP.getAresEngineFactor();
    }

    public IntelliSmsChecker getIntelligentSmsChecker() {
        if (ic.bE()) {
            if (this.tR == null) {
                this.tR = new b();
            }
            return this.tR;
        }
        kt.saveActionData(29947);
        return this.tP.bh();
    }

    public List<DataInterceptor<? extends TelephonyEntity>> interceptors() {
        return !ic.bE() ? this.tP.interceptors() : new ArrayList();
    }

    public void onCreate(Context context) {
        this.tP = new hh();
        this.tP.onCreate(context);
        a(this.tP);
    }

    public void reportRecoverSms(LinkedHashMap<SmsEntity, Integer> linkedHashMap, ISmsReportCallBack iSmsReportCallBack) {
        if (iSmsReportCallBack == null) {
            return;
        }
        if (ic.bE()) {
            iSmsReportCallBack.onReprotFinish(ErrorCode.ERR_LICENSE_EXPIRED);
        } else if (linkedHashMap != null && linkedHashMap.size() > 0) {
            this.tP.reportRecoverSms(linkedHashMap, iSmsReportCallBack);
        } else {
            iSmsReportCallBack.onReprotFinish(-6);
        }
    }

    public final boolean reportSms(List<SmsEntity> list) {
        if (ic.bE()) {
            return false;
        }
        kt.saveActionData(29946);
        return this.tP.reportSms(list);
    }

    public void setAresEngineFactor(AresEngineFactor aresEngineFactor) {
        this.tP.setAresEngineFactor(aresEngineFactor);
    }
}
