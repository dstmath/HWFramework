package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.ISpecialSmsChecker;
import tmsdk.bg.module.aresengine.IncomingSmsFilter;
import tmsdk.bg.module.aresengine.IntelliSmsChecker;
import tmsdk.bg.module.aresengine.IntelligentSmsHandler;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.DataEntity;
import tmsdk.common.ITMSPlugin;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public final class iu extends DataInterceptorBuilder<SmsEntity> {
    private Context mContext;

    /* compiled from: Unknown */
    static final class a extends DataMonitor<SmsEntity> {
        private ix sK;
        private c sL;

        public a() {
            this.sL = new c();
            register();
        }

        private void register() {
            this.sK = ix.e(TMSDKContext.getApplicaionContext());
            this.sK.a(new iy() {
                final /* synthetic */ a sM;

                {
                    this.sM = r1;
                }

                void a(SmsEntity smsEntity, BroadcastReceiver broadcastReceiver) {
                    ITMSPlugin tMSPlugin = TMServiceFactory.getTMSPlugin();
                    if (tMSPlugin != null) {
                        tMSPlugin.onReceiveMsg();
                    }
                    if (smsEntity != null) {
                        if (this.sM.sL.c(smsEntity, broadcastReceiver)) {
                            this.sM.notifyDataReached(smsEntity, Integer.valueOf(0), broadcastReceiver);
                        }
                        ma.bx(29945);
                        mi.bA(5);
                    }
                }
            });
            this.sK.a(null);
        }

        private void unregister() {
            this.sK.unregister();
        }

        void a(SmsEntity smsEntity, Object... objArr) {
            if (smsEntity != null) {
                notifyDataReached(smsEntity, objArr);
            }
        }

        protected void a(boolean z, SmsEntity smsEntity, Object... objArr) {
            super.a(z, smsEntity, objArr);
            if (z && objArr != null && objArr.length >= 2 && (objArr[1] instanceof BroadcastReceiver)) {
                try {
                    ((BroadcastReceiver) objArr[1]).abortBroadcast();
                } catch (Throwable th) {
                    d.c("abortBroadcast", th);
                }
            }
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }

        public void setRegisterState(boolean z) {
            if (z != this.sK.cd()) {
                if (z) {
                    register();
                } else {
                    unregister();
                }
            }
        }
    }

    /* compiled from: Unknown */
    private static final class b extends IncomingSmsFilter {
        private iq sC;
        private AresEngineManager sD;
        private IntelligentSmsHandler sN;
        private ISpecialSmsChecker sO;
        private IntelliSmsChecker sP;
        private nl sf;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.iu.b.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ b sQ;
            final /* synthetic */ ISmsDao sR;
            final /* synthetic */ SmsEntity sS;
            final /* synthetic */ FilterResult sT;

            AnonymousClass3(b bVar, ISmsDao iSmsDao, SmsEntity smsEntity, FilterResult filterResult) {
                this.sQ = bVar;
                this.sR = iSmsDao;
                this.sS = smsEntity;
                this.sT = filterResult;
            }

            public void run() {
                IEntityConverter entityConverter = this.sQ.sD.getAresEngineFactor().getEntityConverter();
                long insert = this.sR.insert(entityConverter != null ? entityConverter.convert(this.sS) : this.sS, this.sT);
                if ((insert <= 0 ? 1 : null) == null) {
                    this.sS.id = (int) insert;
                }
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.iu.b.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ b sQ;
            final /* synthetic */ SmsEntity sU;
            final /* synthetic */ Object[] sV;

            AnonymousClass4(b bVar, SmsEntity smsEntity, Object[] objArr) {
                this.sQ = bVar;
                this.sU = smsEntity;
                this.sV = objArr;
            }

            public void run() {
                this.sQ.sD.getAresEngineFactor().getPhoneDeviceController().unBlockSms(this.sU, this.sV);
            }
        }

        b(Context context) {
            this.sf = null;
            this.sD = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
            this.sf = nl.fn();
            this.sf.fl();
            this.sP = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getIntelligentSmsChecker();
            this.sC = new iq();
            this.sC.b(WifiDetectManager.SECURITY_NONE, 1, 2, 4, 8, 16, 32, 64, IncomingSmsFilterConsts.PAY_SMS, SystemCallLogFilterConsts.NOTIFY_SHORT_CALL);
            this.sC.a(WifiDetectManager.SECURITY_NONE, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                boolean bY() {
                    return (bU() == 2 && this.sQ.sO != null) ? this.sQ.sO.isMatch((SmsEntity) bT()) : false;
                }

                void bZ() {
                    SmsEntity smsEntity = (SmsEntity) bT();
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bT();
                    filterResult.mFilterfiled = bW();
                    filterResult.mState = bU();
                    filterResult.mParams = new Object[]{this.sQ.a(bV()), Boolean.valueOf(this.sQ.sO.isBlocked(smsEntity))};
                    AresEngineFactor aresEngineFactor = this.sQ.sD.getAresEngineFactor();
                    if (((Boolean) filterResult.mParams[1]).booleanValue()) {
                        filterResult.isBlocked = true;
                        aresEngineFactor.getPhoneDeviceController().blockSms(bV());
                        filterResult.mDotos.add(this.sQ.a(smsEntity, aresEngineFactor.getSmsDao(), filterResult));
                    } else {
                        aresEngineFactor.getPhoneDeviceController().unBlockSms(smsEntity, bV());
                    }
                    a(filterResult);
                }
            });
            this.sC.a(1, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    SmsEntity smsEntity = (SmsEntity) bT();
                    return bU() == 2 && smsEntity.protocolType != 2 && this.sQ.sD.getAresEngineFactor().getPrivateListDao().contains(smsEntity.phonenum, 1);
                }

                public void bZ() {
                    SmsEntity smsEntity = (SmsEntity) bT();
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bT();
                    filterResult.mFilterfiled = bW();
                    filterResult.mState = bU();
                    filterResult.mParams = new Object[]{this.sQ.a(bV())};
                    AresEngineFactor aresEngineFactor = this.sQ.sD.getAresEngineFactor();
                    filterResult.isBlocked = true;
                    aresEngineFactor.getPhoneDeviceController().blockSms(bV());
                    filterResult.mDotos.add(this.sQ.a(smsEntity, aresEngineFactor.getPrivateSmsDao(), filterResult));
                    a(filterResult);
                }
            });
            this.sC.a(2, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    return bU() != 2 && this.sQ.sD.getAresEngineFactor().getWhiteListDao().contains(((SmsEntity) bT()).phonenum, 1);
                }

                public void bZ() {
                    this.sQ.b((a) this);
                }
            });
            this.sC.a(4, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    return bU() != 2 && this.sQ.sD.getAresEngineFactor().getBlackListDao().contains(((SmsEntity) bT()).phonenum, 1);
                }

                public void bZ() {
                    this.sQ.b((a) this);
                }
            });
            this.sC.a(8, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    return bU() != 2 && this.sQ.sD.getAresEngineFactor().getSysDao().contains(((SmsEntity) bT()).phonenum);
                }

                public void bZ() {
                    this.sQ.b((a) this);
                }
            });
            this.sC.a(16, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    return bU() != 2 && this.sQ.sD.getAresEngineFactor().getLastCallLogDao().contains(((SmsEntity) bT()).phonenum);
                }

                public void bZ() {
                    this.sQ.b((a) this);
                }
            });
            this.sC.a(32, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    return bU() != 2 && this.sQ.sD.getAresEngineFactor().getKeyWordDao().contains(((SmsEntity) bT()).body);
                }

                public void bZ() {
                    this.sQ.b((a) this);
                }
            });
            this.sC.a(64, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    IntelliSmsCheckResult check = this.sQ.sP.check((SmsEntity) bT());
                    boolean z = bU() == 2 && check.suggestion != 4;
                    if (z) {
                        a((Object) check);
                    }
                    return z;
                }

                public void bZ() {
                    Object obj;
                    boolean z;
                    boolean z2;
                    ISmsDao iSmsDao = null;
                    FilterResult filterResult = new FilterResult();
                    SmsEntity smsEntity = (SmsEntity) bT();
                    IntelliSmsCheckResult intelliSmsCheckResult = (IntelliSmsCheckResult) bX();
                    filterResult.mData = bT();
                    filterResult.mFilterfiled = bW();
                    filterResult.mState = bU();
                    AresEngineFactor aresEngineFactor = this.sQ.sD.getAresEngineFactor();
                    if (IntelliSmsCheckResult.shouldBeBlockedOrNot(intelliSmsCheckResult)) {
                        obj = null;
                        z = false;
                        z2 = true;
                    } else if (intelliSmsCheckResult.suggestion != 1) {
                        obj = null;
                        z = false;
                        z2 = false;
                    } else {
                        ISmsDao paySmsDao = aresEngineFactor.getPaySmsDao();
                        SmsCheckResult t = paySmsDao == null ? null : this.sQ.sf.t(smsEntity.getAddress(), smsEntity.getBody());
                        ISmsDao iSmsDao2;
                        if (t == null) {
                            z = false;
                            z2 = false;
                            iSmsDao2 = paySmsDao;
                            obj = null;
                            iSmsDao = iSmsDao2;
                        } else {
                            String str = t.sRule;
                            z = true;
                            z2 = true;
                            iSmsDao2 = paySmsDao;
                            String str2 = str;
                            iSmsDao = iSmsDao2;
                        }
                    }
                    if (this.sQ.sN != null) {
                        z2 = this.sQ.sN.handleCheckResult(smsEntity, intelliSmsCheckResult, z2);
                    }
                    if (z2) {
                        filterResult.isBlocked = true;
                        aresEngineFactor.getPhoneDeviceController().blockSms(bV());
                        ArrayList arrayList = filterResult.mDotos;
                        b bVar = this.sQ;
                        smsEntity = (SmsEntity) bT();
                        if (!z) {
                            iSmsDao = aresEngineFactor.getSmsDao();
                        }
                        arrayList.add(bVar.a(smsEntity, iSmsDao, filterResult));
                    } else {
                        aresEngineFactor.getPhoneDeviceController().unBlockSms(smsEntity, bV());
                    }
                    filterResult.mParams = new Object[]{this.sQ.a(bV()), intelliSmsCheckResult, Boolean.valueOf(z2), Boolean.valueOf(z), obj};
                    a(filterResult);
                }
            });
            this.sC.a(IncomingSmsFilterConsts.PAY_SMS, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    if (this.sQ.sD.getAresEngineFactor().getPaySmsDao() != null) {
                        SmsEntity smsEntity = (SmsEntity) bT();
                        SmsCheckResult t = this.sQ.sf.t(smsEntity.getAddress(), smsEntity.getBody());
                        if (t != null) {
                            a((Object) t);
                            return true;
                        }
                    }
                    return false;
                }

                public void bZ() {
                    SmsEntity smsEntity = (SmsEntity) bT();
                    SmsCheckResult smsCheckResult = (SmsCheckResult) bX();
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bT();
                    filterResult.mFilterfiled = bW();
                    filterResult.mState = bU();
                    String str = smsCheckResult == null ? null : smsCheckResult.sRule;
                    filterResult.mParams = new Object[]{this.sQ.a(bV()), str};
                    AresEngineFactor aresEngineFactor = this.sQ.sD.getAresEngineFactor();
                    filterResult.isBlocked = true;
                    aresEngineFactor.getPhoneDeviceController().blockSms(bV());
                    filterResult.mDotos.add(this.sQ.a(smsEntity, aresEngineFactor.getPaySmsDao(), filterResult));
                    a(filterResult);
                }
            });
            this.sC.a(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, new a() {
                final /* synthetic */ b sQ;

                {
                    this.sQ = r1;
                }

                public boolean bY() {
                    return true;
                }

                public void bZ() {
                    this.sQ.b((a) this);
                }
            });
        }

        private final Runnable a(SmsEntity smsEntity, ISmsDao<? extends SmsEntity> iSmsDao, FilterResult filterResult) {
            return new AnonymousClass3(this, iSmsDao, smsEntity, filterResult);
        }

        private ArrayList<it> a(Object... objArr) {
            return (objArr != null && objArr.length > 2 && ((Integer) objArr[0]).intValue() == 2) ? (ArrayList) objArr[2] : null;
        }

        private final void b(a aVar) {
            FilterResult filterResult = new FilterResult();
            SmsEntity smsEntity = (SmsEntity) aVar.bT();
            filterResult.mData = aVar.bT();
            filterResult.mFilterfiled = aVar.bW();
            filterResult.mState = aVar.bU();
            filterResult.mParams = new Object[]{a(aVar.bV())};
            AresEngineFactor aresEngineFactor = this.sD.getAresEngineFactor();
            if (aVar.bU() == 0) {
                aresEngineFactor.getPhoneDeviceController().unBlockSms(smsEntity, aVar.bV());
            } else if (aVar.bU() == 1) {
                filterResult.isBlocked = true;
                aresEngineFactor.getPhoneDeviceController().blockSms(aVar.bV());
                if (aresEngineFactor.getSmsDao() != null) {
                    filterResult.mDotos.add(a((SmsEntity) aVar.bT(), aresEngineFactor.getSmsDao(), filterResult));
                }
            }
            aVar.a(filterResult);
        }

        protected /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return b((SmsEntity) telephonyEntity, objArr);
        }

        protected FilterResult b(SmsEntity smsEntity, Object... objArr) {
            Object insert;
            Throwable th;
            qz qzVar;
            StringBuffer d;
            String a;
            String ij;
            FilterResult filterResult;
            int intValue;
            Runnable anonymousClass4;
            FilterResult a2 = this.sC.a(smsEntity, getConfig(), objArr);
            if (ix.f(TMSDKContext.getApplicaionContext())) {
                if (a2 == null || !a2.isBlocked) {
                    AbsSysDao sysDao = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao();
                    try {
                        insert = sysDao.insert(smsEntity);
                        try {
                            smsEntity.id = (int) ContentUris.parseId(insert);
                        } catch (Throwable th2) {
                            th = th2;
                            qzVar = jq.uh;
                            if (qzVar != null) {
                                d.c("IncomingSmsInterceptorBuilder", th);
                            } else {
                                d = iu.c(TMSDKContext.getApplicaionContext());
                                d.append(" url=").append(insert).append(" protocolType=").append(smsEntity.protocolType);
                                d.append(" addfiled=").append(qzVar.ii());
                                if (!TextUtils.isEmpty(smsEntity.fromCard)) {
                                    try {
                                        a = qzVar.a(TMSDKContext.getApplicaionContext(), Integer.parseInt(smsEntity.fromCard));
                                        ij = qzVar.ij();
                                        if (!(ij == null || a == null)) {
                                            d.append(" secondAddFiled=").append(ij);
                                            d.append(" secondAddFiledValue=").append(a);
                                        }
                                    } catch (Throwable th3) {
                                        d.c("IncomingSmsInterceptorBuilder", th3);
                                    }
                                }
                                nd.a(new Thread(), th, d.toString(), null);
                                d.c("IncomingSmsInterceptorBuilder", th + d.toString());
                            }
                            try {
                                smsEntity.id = (int) ContentUris.parseId(sysDao.insert(smsEntity, true));
                            } catch (Throwable th4) {
                                d.c("IncomingSmsInterceptorBuilder", th4);
                                smsEntity.id = -1;
                            }
                            a2 = new FilterResult();
                            a2.mData = smsEntity;
                            a2.mFilterfiled = SystemCallLogFilterConsts.ANONYMOUS_CALL;
                            a2.mState = 0;
                            if (a2 == null) {
                                return a2;
                            }
                            filterResult = new FilterResult();
                            filterResult.mData = smsEntity;
                            filterResult.mFilterfiled = -1;
                            filterResult.mState = 0;
                            intValue = ((Integer) objArr[0]).intValue();
                            anonymousClass4 = new AnonymousClass4(this, smsEntity, objArr);
                            if (intValue != 0) {
                                anonymousClass4.run();
                            } else {
                                filterResult.mDotos.add(anonymousClass4);
                            }
                            filterResult.mParams = new Object[]{a(objArr)};
                            return filterResult;
                        }
                    } catch (Throwable th5) {
                        th4 = th5;
                        insert = null;
                        qzVar = jq.uh;
                        if (qzVar != null) {
                            d = iu.c(TMSDKContext.getApplicaionContext());
                            d.append(" url=").append(insert).append(" protocolType=").append(smsEntity.protocolType);
                            d.append(" addfiled=").append(qzVar.ii());
                            if (TextUtils.isEmpty(smsEntity.fromCard)) {
                                a = qzVar.a(TMSDKContext.getApplicaionContext(), Integer.parseInt(smsEntity.fromCard));
                                ij = qzVar.ij();
                                d.append(" secondAddFiled=").append(ij);
                                d.append(" secondAddFiledValue=").append(a);
                            }
                            nd.a(new Thread(), th4, d.toString(), null);
                            d.c("IncomingSmsInterceptorBuilder", th4 + d.toString());
                        } else {
                            d.c("IncomingSmsInterceptorBuilder", th4);
                        }
                        smsEntity.id = (int) ContentUris.parseId(sysDao.insert(smsEntity, true));
                        a2 = new FilterResult();
                        a2.mData = smsEntity;
                        a2.mFilterfiled = SystemCallLogFilterConsts.ANONYMOUS_CALL;
                        a2.mState = 0;
                        if (a2 == null) {
                            return a2;
                        }
                        filterResult = new FilterResult();
                        filterResult.mData = smsEntity;
                        filterResult.mFilterfiled = -1;
                        filterResult.mState = 0;
                        intValue = ((Integer) objArr[0]).intValue();
                        anonymousClass4 = new AnonymousClass4(this, smsEntity, objArr);
                        if (intValue != 0) {
                            filterResult.mDotos.add(anonymousClass4);
                        } else {
                            anonymousClass4.run();
                        }
                        filterResult.mParams = new Object[]{a(objArr)};
                        return filterResult;
                    }
                    a2 = new FilterResult();
                    a2.mData = smsEntity;
                    a2.mFilterfiled = SystemCallLogFilterConsts.ANONYMOUS_CALL;
                    a2.mState = 0;
                }
            }
            if (a2 == null) {
                return a2;
            }
            filterResult = new FilterResult();
            filterResult.mData = smsEntity;
            filterResult.mFilterfiled = -1;
            filterResult.mState = 0;
            intValue = ((Integer) objArr[0]).intValue();
            if (!(objArr == null || objArr.length < 2 || intValue == 1)) {
                anonymousClass4 = new AnonymousClass4(this, smsEntity, objArr);
                if (intValue != 0) {
                    filterResult.mDotos.add(anonymousClass4);
                } else {
                    anonymousClass4.run();
                }
            }
            filterResult.mParams = new Object[]{a(objArr)};
            return filterResult;
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(WifiDetectManager.SECURITY_NONE, 3);
            filterConfig.set(1, 2);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 1);
            filterConfig.set(64, 2);
            filterConfig.set(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, 1);
            return filterConfig;
        }

        protected void finalize() throws Throwable {
            if (this.sf != null) {
                this.sf.fm();
            }
            super.finalize();
        }

        public void setIntelligentSmsHandler(IntelligentSmsHandler intelligentSmsHandler) {
            this.sN = intelligentSmsHandler;
        }

        public void setSpecialSmsChecker(ISpecialSmsChecker iSpecialSmsChecker) {
            this.sO = iSpecialSmsChecker;
        }
    }

    /* compiled from: Unknown */
    private static final class c {
        private im sW;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.iu.c.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ SmsEntity sS;
            final /* synthetic */ c sX;

            AnonymousClass1(c cVar, SmsEntity smsEntity) {
                this.sX = cVar;
                this.sS = smsEntity;
            }

            public void run() {
                List arrayList = new ArrayList();
                this.sX.a(this.sX.a(this.sS, arrayList), this.sS, (ArrayList) arrayList);
            }
        }

        private c() {
            this.sW = im.bO();
        }

        private ArrayList<jl> a(SmsEntity smsEntity, List<it> list) {
            ArrayList<jl> bP = this.sW.bP();
            ArrayList<jl> arrayList = new ArrayList();
            DataEntity dataEntity = new DataEntity(2);
            dataEntity.bundle().putByteArray("sms", SmsEntity.marshall(smsEntity));
            int size = bP.size() - 1;
            while (size >= 0) {
                try {
                    jl jlVar = (jl) bP.get(size);
                    DataEntity sendMessage = jlVar.sendMessage(dataEntity);
                    if (sendMessage == null) {
                        bP.remove(jlVar);
                    } else {
                        boolean z = sendMessage.bundle().getBoolean("blocked");
                        it bA = it.bA(sendMessage.bundle().getString("information"));
                        if (bA != null) {
                            list.add(bA);
                        }
                        if (z) {
                            arrayList.add(jlVar);
                        }
                    }
                    size--;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return arrayList.size() != 0 ? arrayList : bP;
        }

        private void a(BroadcastReceiver broadcastReceiver) {
            if (broadcastReceiver != null) {
                try {
                    broadcastReceiver.abortBroadcast();
                } catch (Throwable th) {
                    d.c("abortBroadcast", th);
                }
            }
        }

        private void a(List<jl> list, SmsEntity smsEntity, ArrayList<it> arrayList) {
            DataEntity dataEntity = new DataEntity(1);
            Bundle bundle = dataEntity.bundle();
            bundle.putByteArray("sms", SmsEntity.marshall(smsEntity));
            bundle.putString("event_sender", j(list));
            bundle.putString("informations", it.i(arrayList));
            try {
                for (jl sendMessage : list) {
                    sendMessage.sendMessage(dataEntity);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private String j(List<jl> list) {
            String packageName = TMSDKContext.getApplicaionContext().getPackageName();
            DataEntity dataEntity = new DataEntity(4);
            try {
                for (jl sendMessage : list) {
                    DataEntity sendMessage2 = sendMessage.sendMessage(dataEntity);
                    if (sendMessage2 != null && sendMessage2.bundle().getBoolean("support_this_phone")) {
                        return sendMessage2.bundle().getString("pkg");
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return packageName;
        }

        public boolean c(SmsEntity smsEntity, Object... objArr) {
            BroadcastReceiver broadcastReceiver = (BroadcastReceiver) objArr[0];
            if (this.sW.bQ() < 2) {
                return true;
            }
            a(broadcastReceiver);
            jq.ct().b(new AnonymousClass1(this, smsEntity), "onCallingNotifyDataReachedThread");
            return false;
        }
    }

    public iu() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    private static StringBuffer c(Context context) {
        Object e;
        Throwable th;
        StringBuffer stringBuffer = new StringBuffer();
        Cursor query;
        try {
            query = context.getContentResolver().query(Uri.parse("content://sms"), null, null, null, "_id limit 0,1");
            if (query != null) {
                try {
                    int columnCount = query.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        stringBuffer.append(i).append("=").append(query.getColumnName(i)).append(",");
                    }
                } catch (Exception e2) {
                    e = e2;
                    try {
                        d.c("getColumnInfo", e);
                        if (query != null) {
                            try {
                                query.close();
                            } catch (Exception e3) {
                                d.c("getColumnInfo", e3);
                            }
                        }
                        return stringBuffer;
                    } catch (Throwable th2) {
                        th = th2;
                        if (query != null) {
                            try {
                                query.close();
                            } catch (Exception e4) {
                                d.c("getColumnInfo", e4);
                            }
                        }
                        throw th;
                    }
                }
            }
            if (query != null) {
                try {
                    query.close();
                } catch (Exception e32) {
                    d.c("getColumnInfo", e32);
                }
            }
        } catch (Exception e5) {
            e = e5;
            query = null;
            d.c("getColumnInfo", e);
            if (query != null) {
                query.close();
            }
            return stringBuffer;
        } catch (Throwable th3) {
            th = th3;
            query = null;
            if (query != null) {
                query.close();
            }
            throw th;
        }
        return stringBuffer;
    }

    public DataFilter<SmsEntity> getDataFilter() {
        return new b(this.mContext);
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<SmsEntity> getDataMonitor() {
        return new a();
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_INCOMING_SMS;
    }
}
