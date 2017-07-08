package tmsdkobf;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Telephony.MmsSms;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.OutgoingSmsFilter;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
public final class iz extends DataInterceptorBuilder<SmsEntity> {
    private Context mContext;

    /* compiled from: Unknown */
    private static final class a extends OutgoingSmsFilter {
        private iq sC;
        private AresEngineManager sD;

        public a(Context context) {
            this.sD = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
            this.sC = new iq();
            this.sC.b(1);
            this.sC.a(1, new a() {
                final /* synthetic */ a tj;

                /* compiled from: Unknown */
                /* renamed from: tmsdkobf.iz.a.1.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ FilterResult sT;
                    final /* synthetic */ AnonymousClass1 tk;

                    AnonymousClass1(AnonymousClass1 anonymousClass1, FilterResult filterResult) {
                        this.tk = anonymousClass1;
                        this.sT = filterResult;
                    }

                    public void run() {
                        SmsEntity smsEntity = (SmsEntity) this.sT.mData;
                        AresEngineFactor aresEngineFactor = this.tk.tj.sD.getAresEngineFactor();
                        aresEngineFactor.getSysDao().remove(smsEntity);
                        IEntityConverter entityConverter = aresEngineFactor.getEntityConverter();
                        if (entityConverter != null) {
                            smsEntity = entityConverter.convert(smsEntity);
                        }
                        aresEngineFactor.getPrivateSmsDao().insert(smsEntity, this.sT);
                    }
                }

                {
                    this.tj = r1;
                }

                boolean bY() {
                    if (bU() == 2) {
                        if (this.tj.sD.getAresEngineFactor().getPrivateListDao().contains(bT().phonenum, 1)) {
                            return true;
                        }
                    }
                    return false;
                }

                void bZ() {
                    FilterResult filterResult = new FilterResult();
                    filterResult.mFilterfiled = bW();
                    filterResult.mState = bU();
                    filterResult.mData = bT();
                    filterResult.mDotos.add(new AnonymousClass1(this, filterResult));
                    a(filterResult);
                }
            });
        }

        protected /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return b((SmsEntity) telephonyEntity, objArr);
        }

        protected FilterResult b(SmsEntity smsEntity, Object... objArr) {
            return this.sC.a(smsEntity, getConfig(), objArr);
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(1, 2);
            return filterConfig;
        }
    }

    /* compiled from: Unknown */
    private static final class b extends DataMonitor<SmsEntity> {
        private Context mContext;
        private ContentObserver tl;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.iz.b.1 */
        class AnonymousClass1 extends ContentObserver {
            final /* synthetic */ b tm;

            AnonymousClass1(b bVar, Handler handler) {
                this.tm = bVar;
                super(handler);
            }

            public void onChange(boolean z) {
                super.onChange(z);
                try {
                    TelephonyEntity lastSentSms = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao().getLastSentSms(10);
                    if (lastSentSms != null) {
                        ContentResolver contentResolver = this.tm.mContext.getContentResolver();
                        contentResolver.unregisterContentObserver(this);
                        this.tm.notifyDataReached(lastSentSms, new Object[0]);
                        contentResolver.registerContentObserver(MmsSms.CONTENT_CONVERSATIONS_URI, true, this);
                    }
                } catch (NullPointerException e) {
                }
            }
        }

        public b(Context context) {
            this.mContext = context;
            register();
        }

        private void register() {
            this.tl = new AnonymousClass1(this, new Handler());
            this.mContext.getContentResolver().registerContentObserver(MmsSms.CONTENT_CONVERSATIONS_URI, true, this.tl);
        }

        private void unregister() {
            if (this.tl != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.tl);
            }
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }
    }

    public iz() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    public DataFilter<SmsEntity> getDataFilter() {
        return new a(this.mContext);
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<SmsEntity> getDataMonitor() {
        return new b(this.mContext);
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_OUTGOING_SMS;
    }
}
