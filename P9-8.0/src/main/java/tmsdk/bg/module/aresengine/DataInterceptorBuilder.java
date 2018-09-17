package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdkobf.hk;
import tmsdkobf.hl;
import tmsdkobf.ho;
import tmsdkobf.hq;
import tmsdkobf.hv;
import tmsdkobf.hz;

public abstract class DataInterceptorBuilder<T extends TelephonyEntity> extends hk<T> {
    public static final String TYPE_INCOMING_CALL = "incoming_call";
    public static final String TYPE_INCOMING_SMS = "incoming_sms";
    public static final String TYPE_OUTGOING_SMS = "outing_sms";
    public static final String TYPE_SYSTEM_CALL = "system_call";
    private DataMonitor<T> pR;
    private DataFilter<T> pS;
    private DataHandler pT;

    public static final DataInterceptorBuilder<CallLogEntity> createInComingCallInterceptorBuilder() {
        return ho.bu();
    }

    public static final DataInterceptorBuilder<SmsEntity> createInComingSmsInterceptorBuilder() {
        return new hq();
    }

    public static final DataInterceptorBuilder<SmsEntity> createOutgoingSmsInterceptorBuilder() {
        return new hv();
    }

    public static final DataInterceptorBuilder<CallLogEntity> createSystemCallLogInterceptorBuilder() {
        return hz.bz();
    }

    public DataInterceptor<T> create() {
        this.pR = this.pR != null ? this.pR : getDataMonitor();
        this.pS = this.pS != null ? this.pS : getDataFilter();
        this.pT = this.pT != null ? this.pT : getDataHandler();
        if (this.pR == null || this.pS == null || this.pT == null) {
            throw new NullPointerException();
        }
        this.pR.bind(this.pS);
        this.pS.a(this.pT);
        DataInterceptor hlVar = new hl(this.pR, this.pS, this.pT);
        this.pS = null;
        this.pR = null;
        this.pT = null;
        return hlVar;
    }

    public abstract DataFilter<T> getDataFilter();

    public abstract DataHandler getDataHandler();

    public abstract DataMonitor<T> getDataMonitor();

    public abstract String getName();

    public void setDataHandler(DataHandler dataHandler) {
        this.pT = dataHandler;
    }

    public void setDataMonitor(DataMonitor<T> dataMonitor) {
        this.pR = dataMonitor;
    }
}
