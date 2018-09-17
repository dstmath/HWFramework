package tmsdkobf;

import android.content.Context;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptor;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.ISmsReportCallBack;
import tmsdk.bg.module.aresengine.IncomingSmsFilter;
import tmsdk.bg.module.aresengine.IntelliSmsChecker;
import tmsdk.common.DataEntity;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;
import tmsdk.common.module.intelli_sms.MMatchSysResult;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;

public final class hh extends BaseManagerB {
    private final HashMap<String, DataInterceptor<? extends TelephonyEntity>> pB = new HashMap();
    private b pC;
    private AresEngineFactor pD;
    private a pE;

    final class a implements ii {
        a() {
        }

        private void a(DataEntity dataEntity, DataEntity dataEntity2) {
            boolean z = false;
            TelephonyEntity unmarshall = SmsEntity.unmarshall(dataEntity.bundle().getByteArray("sms"));
            DataInterceptor findInterceptor = hh.this.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
            hj dataFilter = findInterceptor == null ? null : findInterceptor.dataFilter();
            if (findInterceptor != null && (dataFilter instanceof IncomingSmsFilter)) {
                DataHandler dataHandler = findInterceptor.dataHandler();
                dataFilter.unbind();
                FilterResult filter = dataFilter.filter(unmarshall, Integer.valueOf(1), null);
                dataFilter.a(dataHandler);
                if (filter != null) {
                    z = filter.isBlocked;
                    hp hpVar = new hp();
                    hpVar.mPkg = TMSDKContext.getApplicaionContext().getPackageName();
                    hpVar.qh = filter.mFilterfiled;
                    hpVar.mState = filter.mState;
                    hpVar.qi = filter.isBlocked;
                    dataEntity2.bundle().putString("information", hp.a(hpVar));
                }
            }
            dataEntity2.bundle().putBoolean("blocked", z);
        }

        private void b(DataEntity dataEntity, DataEntity dataEntity2) {
            Bundle bundle = dataEntity.bundle();
            TelephonyEntity unmarshall = SmsEntity.unmarshall(bundle.getByteArray("sms"));
            ArrayList aD = hp.aD(bundle.getString("informations"));
            DataInterceptor findInterceptor = hh.this.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
            String string = bundle.getString("event_sender");
            if (findInterceptor != null) {
                DataMonitor dataMonitor = findInterceptor.dataMonitor();
                if (dataMonitor instanceof a) {
                    ((a) dataMonitor).a(unmarshall, Integer.valueOf(2), string, aD);
                    return;
                }
                dataMonitor.notifyDataReached(unmarshall, new Object[0]);
            }
        }

        private void c(DataEntity dataEntity, DataEntity dataEntity2) {
            Bundle bundle = dataEntity.bundle();
            String string = bundle.getString("command");
            String string2 = bundle.getString("data");
            if (string != null && string2 != null) {
                if (string.equals("add")) {
                    hi.bi().ax(string2);
                } else {
                    hi.bi().ay(string2);
                }
            }
        }

        private void d(DataEntity dataEntity, DataEntity dataEntity2) {
            dataEntity2.bundle().putBoolean("support_this_phone", ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao().supportThisPhone());
            dataEntity2.bundle().putString("pkg", TMSDKContext.getApplicaionContext().getPackageName());
        }

        public boolean isMatch(int i) {
            return i >= 1 && i <= 4;
        }

        public DataEntity onProcessing(DataEntity dataEntity) {
            int what = dataEntity.what();
            DataEntity dataEntity2 = new DataEntity(what);
            switch (what) {
                case 1:
                    b(dataEntity, dataEntity2);
                    break;
                case 2:
                    a(dataEntity, dataEntity2);
                    break;
                case 3:
                    c(dataEntity, dataEntity2);
                    break;
                case 4:
                    d(dataEntity, dataEntity2);
                    break;
                default:
                    dataEntity2.bundle().putBoolean("has_exceprtion", true);
                    break;
            }
            return dataEntity2;
        }
    }

    static final class b extends IntelliSmsChecker {
        private mm pG = null;

        private MMatchSysResult a(SmsEntity smsEntity, Boolean bool) {
            Class cls = b.class;
            synchronized (b.class) {
                if (this.pG == null) {
                    this.pG = mm.eV();
                    this.pG.eT();
                }
                if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
                    smsEntity.protocolType = 0;
                }
                SmsCheckResult b = this.pG.b(smsEntity, bool);
                if (b == null) {
                    return new MMatchSysResult(1, 1, 0, 0, 1, null);
                }
                MMatchSysResult mMatchSysResult = new MMatchSysResult(b);
                mMatchSysResult.contentType = this.pG.aU(mMatchSysResult.contentType);
                return mMatchSysResult;
            }
        }

        public IntelliSmsCheckResult check(SmsEntity smsEntity) {
            return check(smsEntity, Boolean.valueOf(false));
        }

        public IntelliSmsCheckResult check(SmsEntity smsEntity, Boolean bool) {
            int i = 1;
            MMatchSysResult a = a(smsEntity, bool);
            if (smsEntity.protocolType != 1) {
                i = MMatchSysResult.getSuggestion(a);
            }
            return new IntelliSmsCheckResult(i, a);
        }

        public boolean isChargingSms(SmsEntity smsEntity) {
            Class cls = b.class;
            synchronized (b.class) {
                if (this.pG == null) {
                    this.pG = mm.eV();
                    this.pG.eT();
                }
                return this.pG.t(smsEntity.phonenum, smsEntity.body) != null;
            }
        }
    }

    public void addInterceptor(DataInterceptorBuilder<? extends TelephonyEntity> dataInterceptorBuilder) {
        if (this.pB.containsKey(dataInterceptorBuilder.getName())) {
            throw new RuntimeException("the interceptor named " + dataInterceptorBuilder.getName() + " had exist");
        }
        this.pB.put(dataInterceptorBuilder.getName(), dataInterceptorBuilder.create());
    }

    public IntelliSmsChecker bh() {
        return this.pC;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        f.h("QQPimSecure", "AresEngineManagerImpl finalize()");
    }

    public DataInterceptor<? extends TelephonyEntity> findInterceptor(String str) {
        return (DataInterceptor) this.pB.get(str);
    }

    public AresEngineFactor getAresEngineFactor() {
        if (this.pD != null) {
            return this.pD;
        }
        throw new NullPointerException("The AresEngineManager's Factor can not be null.");
    }

    public int getSingletonType() {
        return 1;
    }

    public List<DataInterceptor<? extends TelephonyEntity>> interceptors() {
        return new ArrayList(this.pB.values());
    }

    public void onCreate(Context context) {
        this.pC = new b();
        this.pE = new a();
        ik.a(this.pE);
        iu.bY();
    }

    public void reportRecoverSms(LinkedHashMap<SmsEntity, Integer> linkedHashMap, ISmsReportCallBack iSmsReportCallBack) {
        hy.reportRecoverSms(linkedHashMap, iSmsReportCallBack);
    }

    public final boolean reportSms(List<SmsEntity> list) {
        List arrayList = new ArrayList();
        IntelliSmsChecker bh = bh();
        for (SmsEntity smsEntity : list) {
            MMatchSysResult mMatchSysResult = (MMatchSysResult) bh.check(smsEntity).getSysResult();
            es esVar = new es();
            esVar.setComment(null);
            esVar.q((int) (System.currentTimeMillis() / 1000));
            esVar.t(smsEntity.phonenum);
            esVar.u(smsEntity.body);
            if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
                smsEntity.protocolType = 0;
            }
            esVar.v(mm.Ai[smsEntity.protocolType][0]);
            if (mMatchSysResult != null) {
                esVar.r(mMatchSysResult.finalAction);
                esVar.s(mMatchSysResult.actionReason);
                esVar.u(mMatchSysResult.minusMark);
                esVar.t(mMatchSysResult.contentType);
                esVar.fo = new ArrayList();
                if (mMatchSysResult.ruleTypeID != null) {
                    for (mp mpVar : mMatchSysResult.ruleTypeID) {
                        esVar.fo.add(new en(mpVar.fg, mpVar.fh));
                    }
                }
            }
            arrayList.add(esVar);
        }
        return ((pq) ManagerCreatorC.getManager(pq.class)).u(arrayList) == 0;
    }

    public void setAresEngineFactor(AresEngineFactor aresEngineFactor) {
        this.pD = aresEngineFactor;
    }
}
