package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
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
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ICallLogDao;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.utils.f;
import tmsdk.common.utils.q;

public final class hz extends DataInterceptorBuilder<CallLogEntity> {
    private Context mContext;
    private b qX;
    private c qY;

    private static class a {
        static hz qZ = new hz();
    }

    public static final class b extends DataMonitor<CallLogEntity> {
        private static final boolean rc = Build.BRAND.contains("Xiaomi");
        private static CallLogEntity rd;
        private static long re = 0;
        private Context mContext;
        private ContentObserver ra;
        private BroadcastReceiver rb;
        private final long rf = 10000;
        private final ConcurrentLinkedQueue<String> rg = new ConcurrentLinkedQueue();
        private final ConcurrentLinkedQueue<String> rh = new ConcurrentLinkedQueue();
        private PhoneStateListener ri;

        public b(Context context) {
            this.mContext = context;
            register();
        }

        private void a(ContentObserver contentObserver, CallLogEntity callLogEntity, ConcurrentLinkedQueue<String> concurrentLinkedQueue) {
            f.f("MMM", "recoreds.size: " + concurrentLinkedQueue.size() + " lastcalllog.phonenum:" + callLogEntity.phonenum);
            if (!concurrentLinkedQueue.isEmpty() && concurrentLinkedQueue.contains(callLogEntity.phonenum)) {
                f.f("MMM", "match =" + callLogEntity.phonenum);
                long currentTimeMillis = System.currentTimeMillis();
                callLogEntity.phonenum = PhoneNumberUtils.stripSeparators(callLogEntity.phonenum);
                notifyDataReached(callLogEntity, Long.valueOf(currentTimeMillis));
                concurrentLinkedQueue.clear();
                f.f("MMM", "clear ");
            }
        }

        private void register() {
            this.rb = new if() {
                private String b(Intent intent) {
                    String stringExtra = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
                    return stringExtra == null ? getResultData() : stringExtra;
                }

                private String c(Intent intent) {
                    String stringExtra = intent.getStringExtra("incoming_number");
                    if (stringExtra == null) {
                        stringExtra = getResultData();
                    }
                    return PhoneNumberUtils.stripSeparators(stringExtra);
                }

                public void doOnRecv(Context context, Intent intent) {
                    String b;
                    if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                        b = b(intent);
                        b.this.rh.add(b != null ? b : "null");
                    } else if (it.a(context, intent) == 1 && !b.rc) {
                        b = c(intent);
                        b.this.rg.add(b != null ? b : "null");
                    }
                }
            };
            it.a(this.mContext, this.rb);
            IntentFilter intentFilter = new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
            intentFilter.setPriority(Integer.MAX_VALUE);
            intentFilter.addCategory("android.intent.category.DEFAULT");
            this.mContext.registerReceiver(this.rb, intentFilter);
            if (rc) {
                this.ri = new PhoneStateListener() {
                    public void onCallStateChanged(int i, String str) {
                        if (i == 1) {
                            Object str2;
                            ConcurrentLinkedQueue b = b.this.rg;
                            if (TextUtils.isEmpty(str2)) {
                                str2 = "null";
                            }
                            b.add(str2);
                        }
                    }
                };
                DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
                instance.listenPhonesState(0, this.ri, 32);
                instance.listenPhonesState(1, this.ri, 32);
            }
            final Handler handler = new Handler();
            this.ra = new ContentObserver(handler) {
                public synchronized void onChange(boolean z) {
                    super.onChange(z);
                    final AbsSysDao sysDao = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao();
                    final CallLogEntity lastCallLog = sysDao.getLastCallLog();
                    if (lastCallLog != null) {
                        handler.post(new Runnable() {
                            /* JADX WARNING: Missing block: B:16:0x00a3, code:
            if (tmsdkobf.hz.b.bB().phonenum.equals(r1.phonenum) != false) goto L_0x00a5;
     */
                            /* JADX WARNING: Missing block: B:19:0x00b3, code:
            if ("null".endsWith(r1.phonenum) == false) goto L_0x0014;
     */
                            /* Code decompiled incorrectly, please refer to instructions dump. */
                            public void run() {
                                Object obj = null;
                                if (lastCallLog.type != 2) {
                                    boolean z = false;
                                    long currentTimeMillis = System.currentTimeMillis();
                                    if (b.rd != null) {
                                        if (currentTimeMillis - b.re >= 10000) {
                                            obj = 1;
                                        }
                                        if (obj == null) {
                                            if (TextUtils.isEmpty(b.rd.phonenum)) {
                                            }
                                            z = true;
                                        }
                                    }
                                    f.f("SystemCallLogInterceptorBuilder", "needDel" + z);
                                    if (z) {
                                        sysDao.remove(lastCallLog);
                                        b.rd = null;
                                        b.re = 0;
                                        b.this.rg.clear();
                                    } else {
                                        b.this.a(b.this.ra, lastCallLog, b.this.rg);
                                    }
                                    b.this.rh.clear();
                                    return;
                                }
                                b.this.a(b.this.ra, lastCallLog, b.this.rh);
                                b.this.rg.clear();
                            }
                        });
                    }
                }
            };
            this.mContext.getContentResolver().registerContentObserver(CallLog.CONTENT_URI, true, this.ra);
        }

        private void unregister() {
            this.mContext.getContentResolver().unregisterContentObserver(this.ra);
            if (this.ri != null) {
                DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
                instance.listenPhonesState(0, this.ri, 0);
                instance.listenPhonesState(1, this.ri, 0);
            }
            this.ra = null;
            this.mContext.unregisterReceiver(this.rb);
            this.rb = null;
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }
    }

    private static final class c extends SystemCallLogFilter {
        private Context mContext;
        private hm qb;
        private AresEngineManager qc = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class));
        private IShortCallChecker ro;
        private boolean rp;

        public c(Context context) {
            this.mContext = context;
            this.rp = bD();
            this.qb = new hm();
            this.qb.a(512, 1, 2, 4, 8, 16, 32, 128, 64, 256);
            this.qb.a(512, new a() {
                boolean br() {
                    return (bn() == 0 || bn() == 1) ? q.cL(bm().phonenum) : false;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), bn() == 1, true);
                }
            });
            this.qb.a(1, new a() {
                boolean br() {
                    return bn() == 2 && c.this.qc.getAresEngineFactor().getPrivateListDao().contains(((CallLogEntity) bm()).phonenum, 0);
                }

                void bs() {
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    if (callLogEntity.type == 3) {
                        callLogEntity.duration = ((Long) bo()[0]).longValue() - callLogEntity.date;
                    }
                    c.this.a(this, c.this.qc.getAresEngineFactor().getPrivateCallLogDao(), true, false);
                }
            });
            this.qb.a(2, new a() {
                boolean br() {
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    return (bn() == 3 || callLogEntity.type == 2 || !c.this.qc.getAresEngineFactor().getWhiteListDao().contains(callLogEntity.phonenum, 0)) ? false : true;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), bn() == 1, true);
                }
            });
            this.qb.a(4, new a() {
                boolean br() {
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    return (bn() == 3 || callLogEntity.type == 2 || !c.this.qc.getAresEngineFactor().getBlackListDao().contains(callLogEntity.phonenum, 0)) ? false : true;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), bn() == 1, true);
                }
            });
            this.qb.a(8, new a() {
                boolean br() {
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    return (bn() == 3 || callLogEntity.type == 2 || !c.this.qc.getAresEngineFactor().getSysDao().contains(callLogEntity.phonenum)) ? false : true;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), bn() == 1, true);
                }
            });
            this.qb.a(16, new a() {
                boolean br() {
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    return (bn() == 3 || callLogEntity.type == 2 || !c.this.qc.getAresEngineFactor().getLastCallLogDao().contains(callLogEntity.phonenum)) ? false : true;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), bn() == 1, true);
                }
            });
            this.qb.a(32, new a() {
                boolean br() {
                    return (((CallLogEntity) bm()).type == 2 || bn() == 3) ? false : true;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), bn() == 1, true);
                }
            });
            this.qb.a(64, new a() {
                boolean br() {
                    int i = 1;
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    String str = callLogEntity.phonenum;
                    if (str == null || str.length() <= 2) {
                        return false;
                    }
                    int i2 = !c.this.rp ? callLogEntity.type != 1 ? 0 : 1 : 0;
                    if ((callLogEntity.duration > 5 ? 1 : 0) != 0) {
                        i = 0;
                    }
                    return i2 & i;
                }

                void bs() {
                    c.this.a(this, null, false, false);
                }
            });
            this.qb.a(128, new a() {
                private final int rr = 8000;

                boolean br() {
                    long longValue = ((Long) bo()[0]).longValue();
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    long j = longValue - callLogEntity.date;
                    if (c.this.ro != null) {
                        return c.this.ro.isShortCall(callLogEntity, j);
                    }
                    boolean z;
                    if (!c.this.rp && bn() == 2 && callLogEntity.type == 3) {
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

                void bs() {
                    CallLogEntity callLogEntity = (CallLogEntity) bm();
                    callLogEntity.duration = ((Long) bo()[0]).longValue() - callLogEntity.date;
                    AresEngineFactor aresEngineFactor = c.this.qc.getAresEngineFactor();
                    aresEngineFactor.getPhoneDeviceController().cancelMissCall();
                    c.this.a(this, aresEngineFactor.getCallLogDao(), true, false);
                }
            });
            this.qb.a(256, new a() {
                boolean br() {
                    return ((CallLogEntity) bm()).type != 2 && bn() == 2;
                }

                void bs() {
                    c.this.a(this, c.this.qc.getAresEngineFactor().getCallLogDao(), false, true);
                }
            });
        }

        private void a(a aVar, ICallLogDao<? extends CallLogEntity> iCallLogDao, boolean z, boolean z2) {
            FilterResult filterResult = new FilterResult();
            filterResult.mParams = aVar.bo();
            filterResult.mData = aVar.bm();
            filterResult.mFilterfiled = aVar.bp();
            filterResult.mState = aVar.bn();
            filterResult.isBlocked = z;
            aVar.a(filterResult);
            if (iCallLogDao != null && z) {
                CallLogEntity callLogEntity = (CallLogEntity) aVar.bm();
                if (z2) {
                    callLogEntity.type = 1;
                }
                AresEngineFactor aresEngineFactor = this.qc.getAresEngineFactor();
                IEntityConverter entityConverter = aresEngineFactor.getEntityConverter();
                if (iCallLogDao.insert(entityConverter == null ? callLogEntity : entityConverter.convert(callLogEntity), filterResult) != -1) {
                    aresEngineFactor.getSysDao().remove(callLogEntity);
                }
            }
        }

        private boolean bD() {
            return TMServiceFactory.getSystemInfoService().ai("com.htc.launcher");
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return this.qb.a(callLogEntity, getConfig(), objArr);
        }

        protected void a(CallLogEntity callLogEntity, FilterResult filterResult, Object... objArr) {
            super.a(callLogEntity, filterResult, new Object[0]);
            if (callLogEntity.type == 2) {
                this.qc.getAresEngineFactor().getLastCallLogDao().update(callLogEntity);
            }
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(512, 0);
            filterConfig.set(1, 2);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 3);
            filterConfig.set(128, 2);
            filterConfig.set(64, 2);
            filterConfig.set(256, 2);
            return filterConfig;
        }

        public void setShortCallChecker(IShortCallChecker iShortCallChecker) {
            this.ro = iShortCallChecker;
        }
    }

    private hz() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    public static hz bz() {
        return a.qZ;
    }

    public DataFilter<CallLogEntity> getDataFilter() {
        if (this.qY == null) {
            this.qY = new c(this.mContext);
        }
        return this.qY;
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<CallLogEntity> getDataMonitor() {
        if (this.qX == null) {
            this.qX = new b(this.mContext);
        }
        return this.qX;
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_SYSTEM_CALL;
    }
}
