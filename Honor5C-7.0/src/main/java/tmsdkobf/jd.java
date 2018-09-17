package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.IShortCallChecker;
import tmsdk.bg.module.aresengine.SystemCallLogFilter;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ICallLogDao;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public final class jd extends DataInterceptorBuilder<CallLogEntity> {
    private Context mContext;
    private b ty;
    private c tz;

    /* compiled from: Unknown */
    private static class a {
        static jd tA;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jd.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jd.a.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jd.a.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    public static final class b extends DataMonitor<CallLogEntity> {
        private static final boolean tD = false;
        private static CallLogEntity tE;
        private static long tF;
        private Context mContext;
        private ContentObserver tB;
        private BroadcastReceiver tC;
        private final long tG;
        private final ConcurrentLinkedQueue<String> tH;
        private final ConcurrentLinkedQueue<String> tI;
        private PhoneStateListener tJ;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.b.1 */
        class AnonymousClass1 extends jj {
            final /* synthetic */ b tK;

            AnonymousClass1(b bVar) {
                this.tK = bVar;
            }

            private String c(Intent intent) {
                String stringExtra = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
                return stringExtra == null ? getResultData() : stringExtra;
            }

            private String d(Intent intent) {
                String stringExtra = intent.getStringExtra("incoming_number");
                if (stringExtra == null) {
                    stringExtra = getResultData();
                }
                return PhoneNumberUtils.stripSeparators(stringExtra);
            }

            public void doOnRecv(Context context, Intent intent) {
                Object c;
                ConcurrentLinkedQueue a;
                if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                    c = c(intent);
                    a = this.tK.tI;
                    if (c == null) {
                        c = "null";
                    }
                    a.add(c);
                } else if (jy.a(context, intent) == 1 && !b.tD) {
                    c = d(intent);
                    a = this.tK.tH;
                    if (c == null) {
                        c = "null";
                    }
                    a.add(c);
                }
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.b.2 */
        class AnonymousClass2 extends PhoneStateListener {
            final /* synthetic */ b tK;

            AnonymousClass2(b bVar) {
                this.tK = bVar;
            }

            public void onCallStateChanged(int i, String str) {
                if (i == 1) {
                    Object obj;
                    ConcurrentLinkedQueue b = this.tK.tH;
                    if (TextUtils.isEmpty(str)) {
                        obj = "null";
                    }
                    b.add(obj);
                }
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.b.3 */
        class AnonymousClass3 extends ContentObserver {
            final /* synthetic */ b tK;
            final /* synthetic */ Handler tL;

            /* compiled from: Unknown */
            /* renamed from: tmsdkobf.jd.b.3.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ CallLogEntity tM;
                final /* synthetic */ AbsSysDao tN;
                final /* synthetic */ AnonymousClass3 tO;

                AnonymousClass1(AnonymousClass3 anonymousClass3, CallLogEntity callLogEntity, AbsSysDao absSysDao) {
                    this.tO = anonymousClass3;
                    this.tM = callLogEntity;
                    this.tN = absSysDao;
                }

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    boolean z = false;
                    if (this.tM.type != 2) {
                        long currentTimeMillis = System.currentTimeMillis();
                        if (b.tE != null) {
                            if (!(currentTimeMillis - b.tF >= 10000)) {
                                if (TextUtils.isEmpty(b.tE.phonenum)) {
                                    if (!"null".endsWith(this.tM.phonenum)) {
                                    }
                                }
                                z = true;
                            }
                        }
                        d.d("SystemCallLogInterceptorBuilder", "needDel" + z);
                        if (z) {
                            this.tN.remove(this.tM);
                            b.tE = null;
                            b.tF = 0;
                            this.tO.tK.tH.clear();
                        } else {
                            this.tO.tK.a(this.tO.tK.tB, this.tM, this.tO.tK.tH);
                        }
                        this.tO.tK.tI.clear();
                        return;
                    }
                    this.tO.tK.a(this.tO.tK.tB, this.tM, this.tO.tK.tI);
                    this.tO.tK.tH.clear();
                }
            }

            AnonymousClass3(b bVar, Handler handler, Handler handler2) {
                this.tK = bVar;
                this.tL = handler2;
                super(handler);
            }

            public synchronized void onChange(boolean z) {
                super.onChange(z);
                AbsSysDao sysDao = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao();
                CallLogEntity lastCallLog = sysDao.getLastCallLog();
                if (lastCallLog != null) {
                    this.tL.post(new AnonymousClass1(this, lastCallLog, sysDao));
                }
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jd.b.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jd.b.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jd.b.<clinit>():void");
        }

        public b(Context context) {
            this.tG = 10000;
            this.tH = new ConcurrentLinkedQueue();
            this.tI = new ConcurrentLinkedQueue();
            this.mContext = context;
            register();
        }

        private void a(ContentObserver contentObserver, CallLogEntity callLogEntity, ConcurrentLinkedQueue<String> concurrentLinkedQueue) {
            d.d("MMM", "recoreds.size: " + concurrentLinkedQueue.size() + " lastcalllog.phonenum:" + callLogEntity.phonenum);
            if (!concurrentLinkedQueue.isEmpty() && concurrentLinkedQueue.contains(callLogEntity.phonenum)) {
                d.d("MMM", "match =" + callLogEntity.phonenum);
                long currentTimeMillis = System.currentTimeMillis();
                callLogEntity.phonenum = PhoneNumberUtils.stripSeparators(callLogEntity.phonenum);
                notifyDataReached(callLogEntity, Long.valueOf(currentTimeMillis));
                concurrentLinkedQueue.clear();
                d.d("MMM", "clear ");
            }
        }

        private void register() {
            this.tC = new AnonymousClass1(this);
            jy.a(this.mContext, this.tC);
            IntentFilter intentFilter = new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
            intentFilter.setPriority(UrlCheckType.UNKNOWN);
            intentFilter.addCategory("android.intent.category.DEFAULT");
            this.mContext.registerReceiver(this.tC, intentFilter);
            if (tD) {
                this.tJ = new AnonymousClass2(this);
                DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
                instance.listenPhonesState(0, this.tJ, 32);
                instance.listenPhonesState(1, this.tJ, 32);
            }
            Handler handler = new Handler();
            this.tB = new AnonymousClass3(this, handler, handler);
            this.mContext.getContentResolver().registerContentObserver(CallLog.CONTENT_URI, true, this.tB);
        }

        private void unregister() {
            this.mContext.getContentResolver().unregisterContentObserver(this.tB);
            if (this.tJ != null) {
                DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
                instance.listenPhonesState(0, this.tJ, 0);
                instance.listenPhonesState(1, this.tJ, 0);
            }
            this.tB = null;
            this.mContext.unregisterReceiver(this.tC);
            this.tC = null;
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }
    }

    /* compiled from: Unknown */
    private static final class c extends SystemCallLogFilter {
        private Context mContext;
        private iq sC;
        private AresEngineManager sD;
        private IShortCallChecker tP;
        private boolean tQ;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.10 */
        class AnonymousClass10 extends a {
            final /* synthetic */ c tR;
            private final int tS;

            AnonymousClass10(c cVar) {
                this.tR = cVar;
                this.tS = 8000;
            }

            boolean bY() {
                long longValue = ((Long) bV()[0]).longValue();
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                long j = longValue - callLogEntity.date;
                if (this.tR.tP != null) {
                    return this.tR.tP.isShortCall(callLogEntity, j);
                }
                boolean z;
                if (!this.tR.tQ && bU() == 2 && callLogEntity.type == 3) {
                    if ((callLogEntity.duration > 8000 ? 1 : 0) == 0) {
                        if ((longValue - callLogEntity.date > 8000 ? 1 : 0) == 0) {
                            z = true;
                            return z;
                        }
                    }
                }
                z = false;
                return z;
            }

            void bZ() {
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                callLogEntity.duration = ((Long) bV()[0]).longValue() - callLogEntity.date;
                AresEngineFactor aresEngineFactor = this.tR.sD.getAresEngineFactor();
                aresEngineFactor.getPhoneDeviceController().cancelMissCall();
                this.tR.a(this, aresEngineFactor.getCallLogDao(), true, false);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.1 */
        class AnonymousClass1 extends a {
            final /* synthetic */ c tR;

            AnonymousClass1(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                return (bU() == 0 || bU() == 1) ? l.dn(bT().phonenum) : false;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.2 */
        class AnonymousClass2 extends a {
            final /* synthetic */ c tR;

            AnonymousClass2(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                return ((CallLogEntity) bT()).type != 2 && bU() == 2;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), false, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.3 */
        class AnonymousClass3 extends a {
            final /* synthetic */ c tR;

            AnonymousClass3(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                return bU() == 2 && this.tR.sD.getAresEngineFactor().getPrivateListDao().contains(((CallLogEntity) bT()).phonenum, 0);
            }

            void bZ() {
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                if (callLogEntity.type == 3) {
                    callLogEntity.duration = ((Long) bV()[0]).longValue() - callLogEntity.date;
                }
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getPrivateCallLogDao(), true, false);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.4 */
        class AnonymousClass4 extends a {
            final /* synthetic */ c tR;

            AnonymousClass4(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getWhiteListDao().contains(callLogEntity.phonenum, 0)) ? false : true;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.5 */
        class AnonymousClass5 extends a {
            final /* synthetic */ c tR;

            AnonymousClass5(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getBlackListDao().contains(callLogEntity.phonenum, 0)) ? false : true;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.6 */
        class AnonymousClass6 extends a {
            final /* synthetic */ c tR;

            AnonymousClass6(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getSysDao().contains(callLogEntity.phonenum)) ? false : true;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.7 */
        class AnonymousClass7 extends a {
            final /* synthetic */ c tR;

            AnonymousClass7(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getLastCallLogDao().contains(callLogEntity.phonenum)) ? false : true;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.8 */
        class AnonymousClass8 extends a {
            final /* synthetic */ c tR;

            AnonymousClass8(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                return (((CallLogEntity) bT()).type == 2 || bU() == 3) ? false : true;
            }

            void bZ() {
                this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.jd.c.9 */
        class AnonymousClass9 extends a {
            final /* synthetic */ c tR;

            AnonymousClass9(c cVar) {
                this.tR = cVar;
            }

            boolean bY() {
                int i = 1;
                CallLogEntity callLogEntity = (CallLogEntity) bT();
                String str = callLogEntity.phonenum;
                if (str == null || str.length() <= 2) {
                    return false;
                }
                int i2 = (this.tR.tQ || callLogEntity.type != 1) ? 0 : 1;
                if (callLogEntity.duration > 5) {
                    int i3 = 1;
                } else {
                    boolean z = false;
                }
                if (i3 != 0) {
                    i = 0;
                }
                return i2 & i;
            }

            void bZ() {
                this.tR.a(this, null, false, false);
            }
        }

        public c(Context context) {
            this.sD = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
            this.mContext = context;
            this.tQ = ck();
            this.sC = new iq();
            this.sC.b(SystemCallLogFilterConsts.ANONYMOUS_CALL, 1, 2, 4, 8, 16, 32, SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, 64, WifiDetectManager.SECURITY_NONE);
            this.sC.a(SystemCallLogFilterConsts.ANONYMOUS_CALL, new AnonymousClass1(this));
            this.sC.a(1, new AnonymousClass3(this));
            this.sC.a(2, new AnonymousClass4(this));
            this.sC.a(4, new AnonymousClass5(this));
            this.sC.a(8, new AnonymousClass6(this));
            this.sC.a(16, new AnonymousClass7(this));
            this.sC.a(32, new AnonymousClass8(this));
            this.sC.a(64, new AnonymousClass9(this));
            this.sC.a(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, new AnonymousClass10(this));
            this.sC.a(WifiDetectManager.SECURITY_NONE, new AnonymousClass2(this));
        }

        private void a(a aVar, ICallLogDao<? extends CallLogEntity> iCallLogDao, boolean z, boolean z2) {
            FilterResult filterResult = new FilterResult();
            filterResult.mParams = aVar.bV();
            filterResult.mData = aVar.bT();
            filterResult.mFilterfiled = aVar.bW();
            filterResult.mState = aVar.bU();
            filterResult.isBlocked = z;
            aVar.a(filterResult);
            if (iCallLogDao != null && z) {
                CallLogEntity callLogEntity = (CallLogEntity) aVar.bT();
                if (z2) {
                    callLogEntity.type = 1;
                }
                AresEngineFactor aresEngineFactor = this.sD.getAresEngineFactor();
                IEntityConverter entityConverter = aresEngineFactor.getEntityConverter();
                if (iCallLogDao.insert(entityConverter == null ? callLogEntity : entityConverter.convert(callLogEntity), filterResult) != -1) {
                    aresEngineFactor.getSysDao().remove(callLogEntity);
                }
            }
        }

        private boolean ck() {
            return TMServiceFactory.getSystemInfoService().aC("com.htc.launcher");
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return this.sC.a(callLogEntity, getConfig(), objArr);
        }

        protected /* bridge */ /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return a((CallLogEntity) telephonyEntity, objArr);
        }

        protected void a(CallLogEntity callLogEntity, FilterResult filterResult, Object... objArr) {
            super.a(callLogEntity, filterResult, new Object[0]);
            if (callLogEntity.type == 2) {
                this.sD.getAresEngineFactor().getLastCallLogDao().update(callLogEntity);
            }
        }

        protected /* bridge */ /* synthetic */ void a(TelephonyEntity telephonyEntity, FilterResult filterResult, Object[] objArr) {
            a((CallLogEntity) telephonyEntity, filterResult, objArr);
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(SystemCallLogFilterConsts.ANONYMOUS_CALL, 0);
            filterConfig.set(1, 2);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 3);
            filterConfig.set(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, 2);
            filterConfig.set(64, 2);
            filterConfig.set(WifiDetectManager.SECURITY_NONE, 2);
            return filterConfig;
        }

        public void setShortCallChecker(IShortCallChecker iShortCallChecker) {
            this.tP = iShortCallChecker;
        }
    }

    private jd() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    /* synthetic */ jd(AnonymousClass1 anonymousClass1) {
        this();
    }

    public static jd cg() {
        return a.tA;
    }

    public DataFilter<CallLogEntity> getDataFilter() {
        if (this.tz == null) {
            this.tz = new c(this.mContext);
        }
        return this.tz;
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<CallLogEntity> getDataMonitor() {
        if (this.ty == null) {
            this.ty = new b(this.mContext);
        }
        return this.ty;
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_SYSTEM_CALL;
    }
}
