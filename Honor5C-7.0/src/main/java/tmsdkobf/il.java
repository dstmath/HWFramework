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
import tmsdk.common.module.aresengine.MmsTransactionHelper;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;
import tmsdk.common.module.intelli_sms.MMatchSysResult;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public final class il extends BaseManagerB {
    private final HashMap<String, DataInterceptor<? extends TelephonyEntity>> rZ;
    private b sa;
    private AresEngineFactor sb;
    private MmsTransactionHelper sc;
    private a sd;

    /* compiled from: Unknown */
    final class a implements jm {
        final /* synthetic */ il se;

        a(il ilVar) {
            this.se = ilVar;
        }

        private void a(DataEntity dataEntity, DataEntity dataEntity2) {
            boolean z = false;
            TelephonyEntity unmarshall = SmsEntity.unmarshall(dataEntity.bundle().getByteArray("sms"));
            DataInterceptor findInterceptor = this.se.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
            in dataFilter = findInterceptor == null ? null : findInterceptor.dataFilter();
            if (findInterceptor != null && (dataFilter instanceof IncomingSmsFilter)) {
                DataHandler dataHandler = findInterceptor.dataHandler();
                dataFilter.unbind();
                FilterResult filter = dataFilter.filter(unmarshall, Integer.valueOf(1), null);
                dataFilter.a(dataHandler);
                if (filter != null) {
                    z = filter.isBlocked;
                    it itVar = new it();
                    itVar.mPkg = TMSDKContext.getApplicaionContext().getPackageName();
                    itVar.sI = filter.mFilterfiled;
                    itVar.mState = filter.mState;
                    itVar.sJ = filter.isBlocked;
                    dataEntity2.bundle().putString("information", it.a(itVar));
                }
            }
            dataEntity2.bundle().putBoolean("blocked", z);
        }

        private void b(DataEntity dataEntity, DataEntity dataEntity2) {
            Bundle bundle = dataEntity.bundle();
            TelephonyEntity unmarshall = SmsEntity.unmarshall(bundle.getByteArray("sms"));
            ArrayList bB = it.bB(bundle.getString("informations"));
            DataInterceptor findInterceptor = this.se.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
            String string = bundle.getString("event_sender");
            if (findInterceptor != null) {
                DataMonitor dataMonitor = findInterceptor.dataMonitor();
                if (dataMonitor instanceof a) {
                    ((a) dataMonitor).a(unmarshall, Integer.valueOf(2), string, bB);
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
                    im.bO().bv(string2);
                } else {
                    im.bO().bw(string2);
                }
            }
        }

        private void d(DataEntity dataEntity, DataEntity dataEntity2) {
            dataEntity2.bundle().putBoolean("support_this_phone", ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao().supportThisPhone());
            dataEntity2.bundle().putString("pkg", TMSDKContext.getApplicaionContext().getPackageName());
        }

        public boolean isMatch(int i) {
            if (i >= 1) {
                if (i <= 4) {
                    return true;
                }
            }
            return false;
        }

        public DataEntity onProcessing(DataEntity dataEntity) {
            int what = dataEntity.what();
            DataEntity dataEntity2 = new DataEntity(what);
            switch (what) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    b(dataEntity, dataEntity2);
                    break;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    a(dataEntity, dataEntity2);
                    break;
                case FileInfo.TYPE_BIGFILE /*3*/:
                    c(dataEntity, dataEntity2);
                    break;
                case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                    d(dataEntity, dataEntity2);
                    break;
                default:
                    dataEntity2.bundle().putBoolean("has_exceprtion", true);
                    break;
            }
            return dataEntity2;
        }
    }

    /* compiled from: Unknown */
    static final class b extends IntelliSmsChecker {
        private nl sf;

        public b() {
            this.sf = null;
        }

        private MMatchSysResult a(SmsEntity smsEntity, Boolean bool) {
            synchronized (b.class) {
                if (this.sf == null) {
                    this.sf = nl.fn();
                    this.sf.fl();
                }
            }
            if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
                smsEntity.protocolType = 0;
            }
            SmsCheckResult b = this.sf.b(smsEntity, bool);
            if (b == null) {
                return new MMatchSysResult(1, 1, 0, 0, 1, null);
            }
            MMatchSysResult mMatchSysResult = new MMatchSysResult(b);
            mMatchSysResult.contentType = this.sf.bL(mMatchSysResult.contentType);
            return mMatchSysResult;
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
            IntelliSmsCheckResult intelliSmsCheckResult = new IntelliSmsCheckResult(i, a);
            if (bool.booleanValue()) {
                ma.bx(1320004);
            }
            return intelliSmsCheckResult;
        }

        public boolean isChargingSms(SmsEntity smsEntity) {
            synchronized (b.class) {
                if (this.sf == null) {
                    this.sf = nl.fn();
                    this.sf.fl();
                }
            }
            return this.sf.t(smsEntity.phonenum, smsEntity.body) != null;
        }
    }

    public il() {
        this.rZ = new HashMap();
    }

    public void addInterceptor(DataInterceptorBuilder<? extends TelephonyEntity> dataInterceptorBuilder) {
        if (this.rZ.containsKey(dataInterceptorBuilder.getName())) {
            throw new RuntimeException("the interceptor named " + dataInterceptorBuilder.getName() + " had exist");
        }
        this.rZ.put(dataInterceptorBuilder.getName(), dataInterceptorBuilder.bS());
    }

    public IntelliSmsChecker bN() {
        return this.sa;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (this.sc != null) {
            this.sc.stop();
        }
        d.g("QQPimSecure", "AresEngineManagerImpl finalize()");
    }

    public DataInterceptor<? extends TelephonyEntity> findInterceptor(String str) {
        return (DataInterceptor) this.rZ.get(str);
    }

    public AresEngineFactor getAresEngineFactor() {
        if (this.sb != null) {
            return this.sb;
        }
        throw new NullPointerException("The AresEngineManager's Factor can not be null.");
    }

    public int getSingletonType() {
        return 1;
    }

    public List<DataInterceptor<? extends TelephonyEntity>> interceptors() {
        return new ArrayList(this.rZ.values());
    }

    public void onCreate(Context context) {
        this.sa = new b();
        this.sd = new a(this);
        jo.a(this.sd);
        jz.cM();
    }

    public void reportRecoverSms(LinkedHashMap<SmsEntity, Integer> linkedHashMap, ISmsReportCallBack iSmsReportCallBack) {
        jc.reportRecoverSms(linkedHashMap, iSmsReportCallBack);
    }

    public final boolean reportSms(List<SmsEntity> list) {
        List arrayList = new ArrayList();
        IntelliSmsChecker bN = bN();
        for (SmsEntity smsEntity : list) {
            MMatchSysResult mMatchSysResult = (MMatchSysResult) bN.check(smsEntity).getSysResult();
            ed edVar = new ed();
            edVar.setComment(null);
            edVar.C((int) (System.currentTimeMillis() / 1000));
            edVar.O(smsEntity.phonenum);
            edVar.P(smsEntity.body);
            if (smsEntity.protocolType < 0 || smsEntity.protocolType > 2) {
                smsEntity.protocolType = 0;
            }
            edVar.H(nl.Cv[smsEntity.protocolType][0]);
            if (mMatchSysResult != null) {
                edVar.D(mMatchSysResult.finalAction);
                edVar.E(mMatchSysResult.actionReason);
                edVar.G(mMatchSysResult.minusMark);
                edVar.F(mMatchSysResult.contentType);
                edVar.et = new ArrayList();
                if (mMatchSysResult.ruleTypeID != null) {
                    for (no noVar : mMatchSysResult.ruleTypeID) {
                        edVar.et.add(new dv(noVar.el, noVar.em));
                    }
                }
            }
            arrayList.add(edVar);
        }
        return ((qt) ManagerCreatorC.getManager(qt.class)).x(arrayList) == 0;
    }

    public void setAresEngineFactor(AresEngineFactor aresEngineFactor) {
        this.sb = aresEngineFactor;
    }
}
