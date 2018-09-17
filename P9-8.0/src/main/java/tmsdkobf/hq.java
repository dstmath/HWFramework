package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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
import tmsdk.common.DataEntity;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;

public final class hq extends DataInterceptorBuilder<SmsEntity> {
    private Context mContext = TMSDKContext.getApplicaionContext();

    static final class a extends DataMonitor<SmsEntity> {
        private ht qj;
        private c qk = new c();

        public a() {
            register();
        }

        private void register() {
            this.qj = ht.h(TMSDKContext.getApplicaionContext());
            kt.saveActionData(29945);
            this.qj.a(new hu() {
                void a(SmsEntity smsEntity, BroadcastReceiver broadcastReceiver) {
                    if (smsEntity != null) {
                        if (a.this.qk.c(smsEntity, broadcastReceiver)) {
                            a.this.notifyDataReached(smsEntity, Integer.valueOf(0), broadcastReceiver);
                        }
                        ll.aM(5);
                    }
                }
            });
            this.qj.a(null);
        }

        private void unregister() {
            this.qj.unregister();
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
                    f.e("abortBroadcast", th);
                }
            }
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }

        public void setRegisterState(boolean z) {
            if (z != this.qj.bw()) {
                if (z) {
                    register();
                } else {
                    unregister();
                }
            }
        }
    }

    private static final class b extends IncomingSmsFilter {
        private mm pG;
        private hm qb;
        private AresEngineManager qc;
        private IntelligentSmsHandler qm;
        private ISpecialSmsChecker qn;
        private IntelliSmsChecker qo;

        b(Context context) {
            this.pG = null;
            this.qc = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
            this.pG = mm.eV();
            this.pG.eT();
            this.qo = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getIntelligentSmsChecker();
            this.qb = new hm();
            this.qb.a(256, 1, 2, 4, 8, 16, 32, 64, IncomingSmsFilterConsts.PAY_SMS, 128);
            this.qb.a(256, new a() {
                boolean br() {
                    return (bn() == 2 && b.this.qn != null) ? b.this.qn.isMatch((SmsEntity) bm()) : false;
                }

                void bs() {
                    SmsEntity smsEntity = (SmsEntity) bm();
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bm();
                    filterResult.mFilterfiled = bp();
                    filterResult.mState = bn();
                    filterResult.mParams = new Object[]{b.this.a(bo()), Boolean.valueOf(b.this.qn.isBlocked(smsEntity))};
                    AresEngineFactor aresEngineFactor = b.this.qc.getAresEngineFactor();
                    if (((Boolean) filterResult.mParams[1]).booleanValue()) {
                        filterResult.isBlocked = true;
                        aresEngineFactor.getPhoneDeviceController().blockSms(bo());
                        filterResult.mDotos.add(b.this.a(smsEntity, aresEngineFactor.getSmsDao(), filterResult));
                    } else {
                        aresEngineFactor.getPhoneDeviceController().unBlockSms(smsEntity, bo());
                    }
                    a(filterResult);
                }
            });
            this.qb.a(1, new a() {
                public boolean br() {
                    SmsEntity smsEntity = (SmsEntity) bm();
                    return bn() == 2 && smsEntity.protocolType != 2 && b.this.qc.getAresEngineFactor().getPrivateListDao().contains(smsEntity.phonenum, 1);
                }

                public void bs() {
                    SmsEntity smsEntity = (SmsEntity) bm();
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bm();
                    filterResult.mFilterfiled = bp();
                    filterResult.mState = bn();
                    filterResult.mParams = new Object[]{b.this.a(bo())};
                    AresEngineFactor aresEngineFactor = b.this.qc.getAresEngineFactor();
                    filterResult.isBlocked = true;
                    aresEngineFactor.getPhoneDeviceController().blockSms(bo());
                    filterResult.mDotos.add(b.this.a(smsEntity, aresEngineFactor.getPrivateSmsDao(), filterResult));
                    a(filterResult);
                }
            });
            this.qb.a(2, new a() {
                public boolean br() {
                    return bn() != 2 && b.this.qc.getAresEngineFactor().getWhiteListDao().contains(((SmsEntity) bm()).phonenum, 1);
                }

                public void bs() {
                    b.this.b((a) this);
                }
            });
            this.qb.a(4, new a() {
                public boolean br() {
                    return bn() != 2 && b.this.qc.getAresEngineFactor().getBlackListDao().contains(((SmsEntity) bm()).phonenum, 1);
                }

                public void bs() {
                    b.this.b((a) this);
                }
            });
            this.qb.a(8, new a() {
                public boolean br() {
                    return bn() != 2 && b.this.qc.getAresEngineFactor().getSysDao().contains(((SmsEntity) bm()).phonenum);
                }

                public void bs() {
                    b.this.b((a) this);
                }
            });
            this.qb.a(16, new a() {
                public boolean br() {
                    return bn() != 2 && b.this.qc.getAresEngineFactor().getLastCallLogDao().contains(((SmsEntity) bm()).phonenum);
                }

                public void bs() {
                    b.this.b((a) this);
                }
            });
            this.qb.a(32, new a() {
                public boolean br() {
                    return bn() != 2 && b.this.qc.getAresEngineFactor().getKeyWordDao().contains(((SmsEntity) bm()).body);
                }

                public void bs() {
                    b.this.b((a) this);
                }
            });
            this.qb.a(64, new a() {
                public boolean br() {
                    boolean z = false;
                    IntelliSmsCheckResult check = b.this.qo.check((SmsEntity) bm());
                    if (bn() == 2 && check.suggestion != 4) {
                        z = true;
                    }
                    if (z) {
                        a((Object) check);
                    }
                    return z;
                }

                public void bs() {
                    FilterResult filterResult = new FilterResult();
                    SmsEntity smsEntity = (SmsEntity) bm();
                    IntelliSmsCheckResult intelliSmsCheckResult = (IntelliSmsCheckResult) bq();
                    filterResult.mData = bm();
                    filterResult.mFilterfiled = bp();
                    filterResult.mState = bn();
                    boolean z = false;
                    boolean z2 = false;
                    String str = null;
                    ISmsDao iSmsDao = null;
                    AresEngineFactor aresEngineFactor = b.this.qc.getAresEngineFactor();
                    if (IntelliSmsCheckResult.shouldBeBlockedOrNot(intelliSmsCheckResult)) {
                        z = true;
                    } else if (intelliSmsCheckResult.suggestion == 1) {
                        SmsCheckResult smsCheckResult = null;
                        iSmsDao = aresEngineFactor.getPaySmsDao();
                        if (iSmsDao != null) {
                            smsCheckResult = b.this.pG.t(smsEntity.getAddress(), smsEntity.getBody());
                        }
                        if (smsCheckResult == null) {
                            z = false;
                        } else {
                            z2 = true;
                            z = true;
                            str = smsCheckResult.sRule;
                        }
                    }
                    if (b.this.qm != null) {
                        z = b.this.qm.handleCheckResult(smsEntity, intelliSmsCheckResult, z);
                    }
                    if (z) {
                        filterResult.isBlocked = true;
                        aresEngineFactor.getPhoneDeviceController().blockSms(bo());
                        ArrayList arrayList = filterResult.mDotos;
                        b bVar = b.this;
                        SmsEntity smsEntity2 = (SmsEntity) bm();
                        if (!z2) {
                            iSmsDao = aresEngineFactor.getSmsDao();
                        }
                        arrayList.add(bVar.a(smsEntity2, iSmsDao, filterResult));
                    } else {
                        aresEngineFactor.getPhoneDeviceController().unBlockSms(smsEntity, bo());
                    }
                    filterResult.mParams = new Object[]{b.this.a(bo()), intelliSmsCheckResult, Boolean.valueOf(z), Boolean.valueOf(z2), str};
                    a(filterResult);
                }
            });
            this.qb.a(IncomingSmsFilterConsts.PAY_SMS, new a() {
                public boolean br() {
                    if (b.this.qc.getAresEngineFactor().getPaySmsDao() == null) {
                        return false;
                    }
                    SmsEntity smsEntity = (SmsEntity) bm();
                    SmsCheckResult t = b.this.pG.t(smsEntity.getAddress(), smsEntity.getBody());
                    if (t == null) {
                        return false;
                    }
                    a((Object) t);
                    return true;
                }

                public void bs() {
                    SmsEntity smsEntity = (SmsEntity) bm();
                    SmsCheckResult smsCheckResult = (SmsCheckResult) bq();
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bm();
                    filterResult.mFilterfiled = bp();
                    filterResult.mState = bn();
                    String str = null;
                    if (smsCheckResult != null) {
                        str = smsCheckResult.sRule;
                    }
                    filterResult.mParams = new Object[]{b.this.a(bo()), str};
                    AresEngineFactor aresEngineFactor = b.this.qc.getAresEngineFactor();
                    filterResult.isBlocked = true;
                    aresEngineFactor.getPhoneDeviceController().blockSms(bo());
                    filterResult.mDotos.add(b.this.a(smsEntity, aresEngineFactor.getPaySmsDao(), filterResult));
                    a(filterResult);
                }
            });
            this.qb.a(128, new a() {
                public boolean br() {
                    return true;
                }

                public void bs() {
                    b.this.b((a) this);
                }
            });
        }

        private final Runnable a(final SmsEntity smsEntity, final ISmsDao<? extends SmsEntity> iSmsDao, final FilterResult filterResult) {
            return new Runnable() {
                public void run() {
                    IEntityConverter entityConverter = b.this.qc.getAresEngineFactor().getEntityConverter();
                    long insert = iSmsDao.insert(entityConverter != null ? entityConverter.convert(smsEntity) : smsEntity, filterResult);
                    if ((insert <= 0 ? 1 : null) == null) {
                        smsEntity.id = (int) insert;
                    }
                }
            };
        }

        private ArrayList<hp> a(Object... objArr) {
            return (objArr != null && objArr.length > 2 && ((Integer) objArr[0]).intValue() == 2) ? (ArrayList) objArr[2] : null;
        }

        private final void b(a aVar) {
            FilterResult filterResult = new FilterResult();
            SmsEntity smsEntity = (SmsEntity) aVar.bm();
            filterResult.mData = aVar.bm();
            filterResult.mFilterfiled = aVar.bp();
            filterResult.mState = aVar.bn();
            filterResult.mParams = new Object[]{a(aVar.bo())};
            AresEngineFactor aresEngineFactor = this.qc.getAresEngineFactor();
            if (aVar.bn() == 0) {
                aresEngineFactor.getPhoneDeviceController().unBlockSms(smsEntity, aVar.bo());
            } else if (aVar.bn() == 1) {
                filterResult.isBlocked = true;
                aresEngineFactor.getPhoneDeviceController().blockSms(aVar.bo());
                if (aresEngineFactor.getSmsDao() != null) {
                    filterResult.mDotos.add(a((SmsEntity) aVar.bm(), aresEngineFactor.getSmsDao(), filterResult));
                }
            }
            aVar.a(filterResult);
        }

        /* JADX WARNING: Removed duplicated region for block: B:30:0x0125  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        /* renamed from: b */
        protected FilterResult a(SmsEntity smsEntity, Object... objArr) {
            AbsSysDao sysDao;
            StringBuffer g;
            FilterResult a = this.qb.a(smsEntity, getConfig(), objArr);
            if (ht.i(TMSDKContext.getApplicaionContext()) && (a == null || !a.isBlocked)) {
                sysDao = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao();
                Object obj = null;
                try {
                    obj = sysDao.insert(smsEntity);
                    smsEntity.id = (int) ContentUris.parseId(obj);
                } catch (Throwable th) {
                    f.e("IncomingSmsInterceptorBuilder", th);
                }
                a = new FilterResult();
                a.mData = smsEntity;
                a.mFilterfiled = 512;
                a.mState = 0;
            }
            if (a == null) {
                a = new FilterResult();
                a.mData = smsEntity;
                a.mFilterfiled = -1;
                a.mState = 0;
                if (objArr != null && objArr.length >= 2) {
                    int intValue = ((Integer) objArr[0]).intValue();
                    if (intValue != 1) {
                        final SmsEntity smsEntity2 = smsEntity;
                        final Object[] objArr2 = objArr;
                        Runnable anonymousClass4 = new Runnable() {
                            public void run() {
                                b.this.qc.getAresEngineFactor().getPhoneDeviceController().unBlockSms(smsEntity2, objArr2);
                            }
                        };
                        if (intValue != 0) {
                            a.mDotos.add(anonymousClass4);
                        } else {
                            anonymousClass4.run();
                        }
                    }
                }
                a.mParams = new Object[]{a(objArr)};
            }
            return a;
            me.a(new Thread(), th, g.toString(), null);
            f.e("IncomingSmsInterceptorBuilder", th + g.toString());
            smsEntity.id = (int) ContentUris.parseId(sysDao.insert(smsEntity, true));
            a = new FilterResult();
            a.mData = smsEntity;
            a.mFilterfiled = 512;
            a.mState = 0;
            if (a == null) {
            }
            return a;
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(256, 3);
            filterConfig.set(1, 2);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 1);
            filterConfig.set(64, 2);
            filterConfig.set(128, 1);
            return filterConfig;
        }

        protected void finalize() throws Throwable {
            if (this.pG != null) {
                this.pG.eU();
            }
            super.finalize();
        }

        public void setIntelligentSmsHandler(IntelligentSmsHandler intelligentSmsHandler) {
            this.qm = intelligentSmsHandler;
        }

        public void setSpecialSmsChecker(ISpecialSmsChecker iSpecialSmsChecker) {
            this.qn = iSpecialSmsChecker;
        }
    }

    private static final class c {
        private hi qv;

        private c() {
            this.qv = hi.bi();
        }

        private ArrayList<ih> a(SmsEntity smsEntity, List<hp> list) {
            ArrayList<ih> bj = this.qv.bj();
            ArrayList<ih> arrayList = new ArrayList();
            DataEntity dataEntity = new DataEntity(2);
            dataEntity.bundle().putByteArray("sms", SmsEntity.marshall(smsEntity));
            try {
                for (int size = bj.size() - 1; size >= 0; size--) {
                    ih ihVar = (ih) bj.get(size);
                    DataEntity sendMessage = ihVar.sendMessage(dataEntity);
                    if (sendMessage == null) {
                        bj.remove(ihVar);
                    } else {
                        boolean z = sendMessage.bundle().getBoolean("blocked");
                        hp aC = hp.aC(sendMessage.bundle().getString("information"));
                        if (aC != null) {
                            list.add(aC);
                        }
                        if (z) {
                            arrayList.add(ihVar);
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return arrayList.size() != 0 ? arrayList : bj;
        }

        private void a(BroadcastReceiver broadcastReceiver) {
            if (broadcastReceiver != null) {
                try {
                    broadcastReceiver.abortBroadcast();
                } catch (Throwable th) {
                    f.e("abortBroadcast", th);
                }
            }
        }

        private void a(List<ih> list, SmsEntity smsEntity, ArrayList<hp> arrayList) {
            DataEntity dataEntity = new DataEntity(1);
            Bundle bundle = dataEntity.bundle();
            bundle.putByteArray("sms", SmsEntity.marshall(smsEntity));
            bundle.putString("event_sender", b(list));
            bundle.putString("informations", hp.a((List) arrayList));
            try {
                for (ih sendMessage : list) {
                    sendMessage.sendMessage(dataEntity);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private String b(List<ih> list) {
            String packageName = TMSDKContext.getApplicaionContext().getPackageName();
            DataEntity dataEntity = new DataEntity(4);
            try {
                for (ih sendMessage : list) {
                    DataEntity sendMessage2 = sendMessage.sendMessage(dataEntity);
                    if (sendMessage2 != null && sendMessage2.bundle().getBoolean("support_this_phone")) {
                        return sendMessage2.bundle().getString("pkg");
                    }
                }
                return packageName;
            } catch (RemoteException e) {
                e.printStackTrace();
                return packageName;
            }
        }

        public boolean c(final SmsEntity smsEntity, Object... objArr) {
            BroadcastReceiver broadcastReceiver = (BroadcastReceiver) objArr[0];
            if (this.qv.bk() < 2) {
                return true;
            }
            a(broadcastReceiver);
            im.bJ().a(new Runnable() {
                public void run() {
                    List arrayList = new ArrayList();
                    c.this.a(c.this.a(smsEntity, arrayList), smsEntity, (ArrayList) arrayList);
                }
            }, "onCallingNotifyDataReachedThread");
            return false;
        }
    }

    private static StringBuffer f(Context context) {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://sms"), null, null, null, "_id limit 0,1");
            if (cursor != null) {
                int columnCount = cursor.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    stringBuffer.append(i).append("=").append(cursor.getColumnName(i)).append(",");
                }
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    f.e("getColumnInfo", e);
                }
            }
        } catch (Exception e2) {
            f.e("getColumnInfo", e2);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e22) {
                    f.e("getColumnInfo", e22);
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e3) {
                    f.e("getColumnInfo", e3);
                }
            }
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
