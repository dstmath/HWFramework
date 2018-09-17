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
import tmsdk.common.utils.f;

public final class ho extends DataInterceptorBuilder<CallLogEntity> {
    public static long pZ = 0;
    private Context mContext;

    private static class a {
        static ho qa = new ho();
    }

    private static final class b extends IncomingCallFilter {
        private hm qb = new hm();
        private AresEngineManager qc = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class));

        b(Context context) {
            this.qb.a(64, 1, 2, 4, 8, 16, 32);
            this.qb.a(64, ac(64));
            this.qb.a(1, ac(1));
            this.qb.a(2, ac(2));
            this.qb.a(4, ac(4));
            this.qb.a(8, ac(8));
            this.qb.a(16, ac(16));
            this.qb.a(32, ac(32));
        }

        private a ac(final int i) {
            return new a() {
                boolean br() {
                    if (bn() != 0 && bn() != 1) {
                        return false;
                    }
                    IContactDao iContactDao = null;
                    switch (i) {
                        case 1:
                            iContactDao = b.this.qc.getAresEngineFactor().getPrivateListDao();
                            break;
                        case 2:
                            iContactDao = b.this.qc.getAresEngineFactor().getWhiteListDao();
                            break;
                        case 4:
                            iContactDao = b.this.qc.getAresEngineFactor().getBlackListDao();
                            break;
                        case 8:
                            iContactDao = b.this.qc.getAresEngineFactor().getSysDao();
                            break;
                        case 16:
                            iContactDao = b.this.qc.getAresEngineFactor().getLastCallLogDao();
                            break;
                        case 32:
                            iContactDao = null;
                            break;
                    }
                    if (i == 64) {
                        return TextUtils.isEmpty(bm().phonenum);
                    }
                    if (i == 32) {
                        return true;
                    }
                    if (iContactDao instanceof IContactDao) {
                        return iContactDao.contains(bm().phonenum, 0);
                    }
                    if (iContactDao instanceof ILastCallLogDao) {
                        return ((ILastCallLogDao) iContactDao).contains(bm().phonenum);
                    }
                    return !(iContactDao instanceof AbsSysDao) ? false : ((AbsSysDao) iContactDao).contains(bm().phonenum);
                }

                void bs() {
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bm();
                    filterResult.mParams = bo();
                    filterResult.mState = bn();
                    filterResult.mFilterfiled = bp();
                    if (bn() != 0 && bn() == 1) {
                        filterResult.isBlocked = true;
                        CallLogEntity callLogEntity = (CallLogEntity) filterResult.mData;
                        qc qcVar = im.rE;
                        ITelephony iTelephony = null;
                        if (qcVar != null) {
                            if (callLogEntity.fromCard == null || callLogEntity.fromCard.equals(qcVar.bT(0))) {
                                iTelephony = DualSimTelephonyManager.getDefaultTelephony();
                            } else if (callLogEntity.fromCard.equals(qcVar.bT(1))) {
                                iTelephony = DualSimTelephonyManager.getSecondTelephony();
                            }
                        }
                        boolean z = false;
                        if (iTelephony != null) {
                            try {
                                z = iTelephony.endCall();
                            } catch (Throwable e) {
                                f.b("IncomingCallInterceptorBuilder", "endCall", e);
                            }
                        }
                        f.f("IncomingCallInterceptorBuilder", "endCall1 " + z);
                        if (!z) {
                            z = b.this.qc.getAresEngineFactor().getPhoneDeviceController().hangup();
                            f.f("IncomingCallInterceptorBuilder", "endCall2 " + z);
                        }
                        if (!z) {
                            long currentTimeMillis = System.currentTimeMillis();
                            f.f("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - ho.pZ));
                            f.f("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                            f.f("IncomingCallInterceptorBuilder", "lastCallEndTime" + ho.pZ);
                            if (!(ho.pZ <= 0) && currentTimeMillis > ho.pZ) {
                                int i = 0;
                            }
                        }
                    }
                    a(filterResult);
                }
            };
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return this.qb.a(callLogEntity, getConfig(), objArr);
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

    private static final class c extends DataMonitor<CallLogEntity> {
        private tmsdkobf.hw.b qf = new tmsdkobf.hw.b() {
            public void aA(String str) {
                ho.pZ = System.currentTimeMillis();
            }

            public void aB(String str) {
                ho.pZ = 0;
            }

            public void az(String str) {
            }

            public void g(String str, String str2) {
                ho.pZ = 0;
                TelephonyEntity callLogEntity = new CallLogEntity();
                callLogEntity.phonenum = str;
                callLogEntity.type = 1;
                callLogEntity.date = System.currentTimeMillis();
                callLogEntity.fromCard = str2;
                c.this.notifyDataReached(callLogEntity, new Object[0]);
            }
        };

        public c(Context context) {
            hw.bx().a(this.qf);
        }

        protected void finalize() throws Throwable {
            hw.bx().b(this.qf);
            super.finalize();
        }
    }

    private ho() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    public static ho bu() {
        return a.qa;
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
