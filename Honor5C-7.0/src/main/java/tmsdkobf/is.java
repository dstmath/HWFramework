package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import com.android.internal.telephony.ITelephony;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.IncomingCallFilter;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.IContactDao;
import tmsdk.common.module.aresengine.ILastCallLogDao;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public final class is extends DataInterceptorBuilder<CallLogEntity> {
    public static Long sA;
    public static long sy;
    public static String sz;
    private Context mContext;

    /* compiled from: Unknown */
    private static class a {
        static is sB;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.is.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.is.a.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.is.a.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    private static final class b extends IncomingCallFilter {
        private iq sC;
        private AresEngineManager sD;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.is.b.1 */
        class AnonymousClass1 extends a {
            final /* synthetic */ int sE;
            final /* synthetic */ b sF;

            AnonymousClass1(b bVar, int i) {
                this.sF = bVar;
                this.sE = i;
            }

            boolean bY() {
                IContactDao iContactDao = null;
                if (bU() != 0 && bU() != 1) {
                    return false;
                }
                switch (this.sE) {
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        iContactDao = this.sF.sD.getAresEngineFactor().getPrivateListDao();
                        break;
                    case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                        iContactDao = this.sF.sD.getAresEngineFactor().getWhiteListDao();
                        break;
                    case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                        iContactDao = this.sF.sD.getAresEngineFactor().getBlackListDao();
                        break;
                    case RubbishType.SCAN_FLAG_APK /*8*/:
                        iContactDao = this.sF.sD.getAresEngineFactor().getSysDao();
                        break;
                    case NumQueryRet.USED_FOR_Common /*16*/:
                        iContactDao = this.sF.sD.getAresEngineFactor().getLastCallLogDao();
                        break;
                }
                return this.sE != 64 ? this.sE != 32 ? !(iContactDao instanceof IContactDao) ? !(iContactDao instanceof ILastCallLogDao) ? !(iContactDao instanceof AbsSysDao) ? false : ((AbsSysDao) iContactDao).contains(bT().phonenum) : ((ILastCallLogDao) iContactDao).contains(bT().phonenum) : iContactDao.contains(bT().phonenum, 0) : true : TextUtils.isEmpty(bT().phonenum);
            }

            void bZ() {
                int i = 0;
                FilterResult filterResult = new FilterResult();
                filterResult.mData = bT();
                filterResult.mParams = bV();
                filterResult.mState = bU();
                filterResult.mFilterfiled = bW();
                if (bU() != 0 && bU() == 1) {
                    ITelephony defaultTelephony;
                    boolean endCall;
                    long currentTimeMillis;
                    filterResult.isBlocked = true;
                    CallLogEntity callLogEntity = (CallLogEntity) filterResult.mData;
                    qz qzVar = jq.uh;
                    if (qzVar != null) {
                        if (callLogEntity.fromCard == null || callLogEntity.fromCard.equals(qzVar.cA(0))) {
                            defaultTelephony = DualSimTelephonyManager.getDefaultTelephony();
                            if (defaultTelephony != null) {
                                endCall = defaultTelephony.endCall();
                                d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                                if (!endCall) {
                                    endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                    d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                                }
                                if (!endCall) {
                                    currentTimeMillis = System.currentTimeMillis();
                                    d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                    d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                    d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                    if (is.sy > 0) {
                                    }
                                    if (!(is.sy > 0)) {
                                        if (currentTimeMillis <= is.sy) {
                                            i = 1;
                                        }
                                        if (i == 0) {
                                        }
                                    }
                                }
                            }
                            endCall = false;
                            d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                            if (endCall) {
                                endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                            }
                            if (endCall) {
                                currentTimeMillis = System.currentTimeMillis();
                                d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                if (is.sy > 0) {
                                }
                                if (is.sy > 0) {
                                    if (currentTimeMillis <= is.sy) {
                                        i = 1;
                                    }
                                    if (i == 0) {
                                    }
                                }
                            }
                        } else if (callLogEntity.fromCard.equals(qzVar.cA(1))) {
                            defaultTelephony = DualSimTelephonyManager.getSecondTelephony();
                            if (defaultTelephony != null) {
                                try {
                                    endCall = defaultTelephony.endCall();
                                } catch (Throwable e) {
                                    d.a("IncomingCallInterceptorBuilder", "endCall", e);
                                }
                                d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                                if (endCall) {
                                    endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                    d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                                }
                                if (endCall) {
                                    currentTimeMillis = System.currentTimeMillis();
                                    d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                    d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                    d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                    if (is.sy > 0) {
                                        if (currentTimeMillis <= is.sy) {
                                            i = 1;
                                        }
                                        if (i == 0) {
                                        }
                                    }
                                }
                            }
                            endCall = false;
                            d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                            if (endCall) {
                                endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                            }
                            if (endCall) {
                                currentTimeMillis = System.currentTimeMillis();
                                d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                if (is.sy > 0) {
                                }
                                if (is.sy > 0) {
                                    if (currentTimeMillis <= is.sy) {
                                        i = 1;
                                    }
                                    if (i == 0) {
                                    }
                                }
                            }
                        }
                    }
                    defaultTelephony = null;
                    if (defaultTelephony != null) {
                        endCall = defaultTelephony.endCall();
                        d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                        if (endCall) {
                            endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                            d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                        }
                        if (endCall) {
                            currentTimeMillis = System.currentTimeMillis();
                            d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                            d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                            d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                            if (is.sy > 0) {
                            }
                            if (is.sy > 0) {
                                if (currentTimeMillis <= is.sy) {
                                    i = 1;
                                }
                                if (i == 0) {
                                }
                            }
                        }
                    }
                    endCall = false;
                    d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                    if (endCall) {
                        endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                        d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                    }
                    if (endCall) {
                        currentTimeMillis = System.currentTimeMillis();
                        d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                        d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                        d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                        if (is.sy > 0) {
                        }
                        if (is.sy > 0) {
                            if (currentTimeMillis <= is.sy) {
                                i = 1;
                            }
                            if (i == 0) {
                            }
                        }
                    }
                }
                a(filterResult);
            }
        }

        b(Context context) {
            this.sD = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
            this.sC = new iq();
            this.sC.b(64, 1, 2, 4, 8, 16, 32);
            this.sC.a(64, aV(64));
            this.sC.a(1, aV(1));
            this.sC.a(2, aV(2));
            this.sC.a(4, aV(4));
            this.sC.a(8, aV(8));
            this.sC.a(16, aV(16));
            this.sC.a(32, aV(32));
        }

        private a aV(int i) {
            return new AnonymousClass1(this, i);
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return this.sC.a(callLogEntity, getConfig(), objArr);
        }

        protected /* bridge */ /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return a((CallLogEntity) telephonyEntity, objArr);
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(1, 0);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 0);
            filterConfig.set(64, 0);
            return filterConfig;
        }
    }

    /* compiled from: Unknown */
    private static final class c extends DataMonitor<CallLogEntity> {
        private tmsdkobf.ja.b sG;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.is.c.1 */
        class AnonymousClass1 implements tmsdkobf.ja.b {
            final /* synthetic */ c sH;

            AnonymousClass1(c cVar) {
                this.sH = cVar;
            }

            public void bx(String str) {
            }

            public void by(String str) {
                is.sy = System.currentTimeMillis();
            }

            public void bz(String str) {
                is.sy = 0;
            }

            public void i(String str, String str2) {
                is.sy = 0;
                TelephonyEntity callLogEntity = new CallLogEntity();
                callLogEntity.phonenum = str;
                callLogEntity.type = 1;
                callLogEntity.date = System.currentTimeMillis();
                callLogEntity.fromCard = str2;
                this.sH.notifyDataReached(callLogEntity, new Object[0]);
            }
        }

        public c(Context context) {
            this.sG = new AnonymousClass1(this);
            ja.ce().a(this.sG);
        }

        protected void finalize() throws Throwable {
            ja.ce().b(this.sG);
            super.finalize();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.is.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.is.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.is.<clinit>():void");
    }

    private is() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    /* synthetic */ is(AnonymousClass1 anonymousClass1) {
        this();
    }

    public static is cb() {
        return a.sB;
    }

    public DataFilter<CallLogEntity> getDataFilter() {
        return new b(this.mContext);
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<CallLogEntity> getDataMonitor() {
        return new c(this.mContext);
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_INCOMING_CALL;
    }
}
