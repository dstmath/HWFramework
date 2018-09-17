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
import tmsdkobf.il;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
public final class AresEngineManager extends BaseManagerB {
    private il wL;
    private Map<String, DataInterceptor<? extends TelephonyEntity>> wM;
    private b wN;

    public void addInterceptor(DataInterceptorBuilder<? extends TelephonyEntity> dataInterceptorBuilder) throws RuntimeException {
        if (!jg.cl()) {
            this.wL.addInterceptor(dataInterceptorBuilder);
        }
    }

    public DataInterceptor<? extends TelephonyEntity> findInterceptor(String str) {
        int i = 0;
        if (!jg.cl()) {
            return this.wL.findInterceptor(str);
        }
        if (this.wM == null) {
            this.wM = new HashMap();
            String[] strArr = new String[]{DataInterceptorBuilder.TYPE_INCOMING_CALL, DataInterceptorBuilder.TYPE_INCOMING_SMS, DataInterceptorBuilder.TYPE_OUTGOING_SMS, DataInterceptorBuilder.TYPE_SYSTEM_CALL};
            int length = strArr.length;
            while (i < length) {
                String str2 = strArr[i];
                this.wM.put(str2, new a(str2));
                i++;
            }
        }
        return (DataInterceptor) this.wM.get(str);
    }

    public AresEngineFactor getAresEngineFactor() {
        return this.wL.getAresEngineFactor();
    }

    public IntelliSmsChecker getIntelligentSmsChecker() {
        if (jg.cl()) {
            if (this.wN == null) {
                this.wN = new b();
            }
            return this.wN;
        }
        ma.bx(29947);
        return this.wL.bN();
    }

    public List<DataInterceptor<? extends TelephonyEntity>> interceptors() {
        return !jg.cl() ? this.wL.interceptors() : new ArrayList();
    }

    public void onCreate(Context context) {
        this.wL = new il();
        this.wL.onCreate(context);
        a(this.wL);
    }

    public void reportRecoverSms(LinkedHashMap<SmsEntity, Integer> linkedHashMap, ISmsReportCallBack iSmsReportCallBack) {
        if (jg.cl()) {
            iSmsReportCallBack.onReprotFinish(ErrorCode.ERR_LICENSE_EXPIRED);
        } else if (linkedHashMap == null || iSmsReportCallBack == null || linkedHashMap.size() <= 0) {
            iSmsReportCallBack.onReprotFinish(-6);
        } else {
            this.wL.reportRecoverSms(linkedHashMap, iSmsReportCallBack);
        }
    }

    public final boolean reportSms(List<SmsEntity> list) {
        if (jg.cl()) {
            return false;
        }
        ma.bx(29946);
        return this.wL.reportSms(list);
    }

    public void setAresEngineFactor(AresEngineFactor aresEngineFactor) {
        this.wL.setAresEngineFactor(aresEngineFactor);
    }
}
