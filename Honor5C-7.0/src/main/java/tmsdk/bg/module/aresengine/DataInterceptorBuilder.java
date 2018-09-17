package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdkobf.io;
import tmsdkobf.ip;
import tmsdkobf.is;
import tmsdkobf.iu;
import tmsdkobf.iz;
import tmsdkobf.jd;

/* compiled from: Unknown */
public abstract class DataInterceptorBuilder<T extends TelephonyEntity> extends io<T> {
    public static final String TYPE_INCOMING_CALL = "incoming_call";
    public static final String TYPE_INCOMING_SMS = "incoming_sms";
    public static final String TYPE_OUTGOING_SMS = "outing_sms";
    public static final String TYPE_SYSTEM_CALL = "system_call";
    private DataMonitor<T> sq;
    private DataFilter<T> sr;
    private DataHandler ss;

    public static final DataInterceptorBuilder<CallLogEntity> createInComingCallInterceptorBuilder() {
        return is.cb();
    }

    public static final DataInterceptorBuilder<SmsEntity> createInComingSmsInterceptorBuilder() {
        return new iu();
    }

    public static final DataInterceptorBuilder<SmsEntity> createOutgoingSmsInterceptorBuilder() {
        return new iz();
    }

    public static final DataInterceptorBuilder<CallLogEntity> createSystemCallLogInterceptorBuilder() {
        return jd.cg();
    }

    protected DataInterceptor<T> bS() {
        this.sq = this.sq != null ? this.sq : getDataMonitor();
        this.sr = this.sr != null ? this.sr : getDataFilter();
        this.ss = this.ss != null ? this.ss : getDataHandler();
        if (this.sq == null || this.sr == null || this.ss == null) {
            throw new NullPointerException();
        }
        this.sq.bind(this.sr);
        this.sr.a(this.ss);
        DataInterceptor ipVar = new ip(this.sq, this.sr, this.ss);
        this.sr = null;
        this.sq = null;
        this.ss = null;
        return ipVar;
    }

    public abstract DataFilter<T> getDataFilter();

    public abstract DataHandler getDataHandler();

    public abstract DataMonitor<T> getDataMonitor();

    public abstract String getName();

    public void setDataHandler(DataHandler dataHandler) {
        this.ss = dataHandler;
    }

    public void setDataMonitor(DataMonitor<T> dataMonitor) {
        this.sq = dataMonitor;
    }
}
