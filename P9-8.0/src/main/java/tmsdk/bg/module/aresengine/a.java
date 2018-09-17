package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;

final class a implements DataInterceptor<TelephonyEntity> {
    static final FilterConfig uh = new FilterConfig();
    static final FilterResult ui = new FilterResult();
    a uj = new a();
    DataFilter<? extends TelephonyEntity> uk;
    DataHandler ul = new DataHandler();

    static final class a extends DataMonitor<TelephonyEntity> {
        a() {
        }
    }

    static final class b extends IncomingCallFilter {
        b() {
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return a.ui;
        }

        public FilterConfig defalutFilterConfig() {
            return a.uh;
        }
    }

    static final class c extends IncomingSmsFilter {
        c() {
        }

        /* renamed from: b */
        protected FilterResult a(SmsEntity smsEntity, Object... objArr) {
            return a.ui;
        }

        public FilterConfig defalutFilterConfig() {
            return a.uh;
        }

        public void setIntelligentSmsHandler(IntelligentSmsHandler intelligentSmsHandler) {
        }

        public void setSpecialSmsChecker(ISpecialSmsChecker iSpecialSmsChecker) {
        }
    }

    static final class d extends OutgoingSmsFilter {
        d() {
        }

        /* renamed from: b */
        protected FilterResult a(SmsEntity smsEntity, Object... objArr) {
            return a.ui;
        }

        public FilterConfig defalutFilterConfig() {
            return a.uh;
        }
    }

    static final class e extends SystemCallLogFilter {
        e() {
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return a.ui;
        }

        public FilterConfig defalutFilterConfig() {
            return a.uh;
        }

        public void setShortCallChecker(IShortCallChecker iShortCallChecker) {
        }
    }

    public a(String str) {
        DataFilter bVar;
        if (str.equals(DataInterceptorBuilder.TYPE_INCOMING_CALL)) {
            bVar = new b();
        } else if (str.equals(DataInterceptorBuilder.TYPE_INCOMING_SMS)) {
            bVar = new c();
        } else if (str.equals(DataInterceptorBuilder.TYPE_OUTGOING_SMS)) {
            bVar = new d();
        } else if (str.equals(DataInterceptorBuilder.TYPE_SYSTEM_CALL)) {
            bVar = new e();
        } else {
            return;
        }
        this.uk = bVar;
    }

    public DataFilter<TelephonyEntity> dataFilter() {
        return this.uk;
    }

    public DataHandler dataHandler() {
        return this.ul;
    }

    public DataMonitor<TelephonyEntity> dataMonitor() {
        return this.uj;
    }
}
