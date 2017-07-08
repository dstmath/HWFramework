package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
final class a implements DataInterceptor<TelephonyEntity> {
    static final FilterConfig xd = null;
    static final FilterResult xe = null;
    a xf;
    DataFilter<? extends TelephonyEntity> xg;
    DataHandler xh;

    /* compiled from: Unknown */
    static final class a extends DataMonitor<TelephonyEntity> {
        a() {
        }
    }

    /* compiled from: Unknown */
    static final class b extends IncomingCallFilter {
        b() {
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }
    }

    /* compiled from: Unknown */
    static final class c extends IncomingSmsFilter {
        c() {
        }

        protected /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return b((SmsEntity) telephonyEntity, objArr);
        }

        protected FilterResult b(SmsEntity smsEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }

        public void setIntelligentSmsHandler(IntelligentSmsHandler intelligentSmsHandler) {
        }

        public void setSpecialSmsChecker(ISpecialSmsChecker iSpecialSmsChecker) {
        }
    }

    /* compiled from: Unknown */
    static final class d extends OutgoingSmsFilter {
        d() {
        }

        protected /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return b((SmsEntity) telephonyEntity, objArr);
        }

        protected FilterResult b(SmsEntity smsEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }
    }

    /* compiled from: Unknown */
    static final class e extends SystemCallLogFilter {
        e() {
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }

        public void setShortCallChecker(IShortCallChecker iShortCallChecker) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.module.aresengine.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.module.aresengine.a.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.module.aresengine.a.<clinit>():void");
    }

    public a(String str) {
        DataFilter bVar;
        this.xf = new a();
        this.xh = new DataHandler();
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
        this.xg = bVar;
    }

    public DataFilter<TelephonyEntity> dataFilter() {
        return this.xg;
    }

    public DataHandler dataHandler() {
        return this.xh;
    }

    public DataMonitor<TelephonyEntity> dataMonitor() {
        return this.xf;
    }
}
