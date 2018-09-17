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

public final class hv extends DataInterceptorBuilder<SmsEntity> {
    private Context mContext = TMSDKContext.getApplicaionContext();

    private static final class a extends OutgoingSmsFilter {
        private hm qb = new hm();
        private AresEngineManager qc = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class));

        public a(Context context) {
            this.qb.a(1);
            this.qb.a(1, new a() {
                boolean br() {
                    return bn() == 2 && a.this.qc.getAresEngineFactor().getPrivateListDao().contains(bm().phonenum, 1);
                }

                void bs() {
                    final FilterResult filterResult = new FilterResult();
                    filterResult.mFilterfiled = bp();
                    filterResult.mState = bn();
                    filterResult.mData = bm();
                    filterResult.mDotos.add(new Runnable() {
                        public void run() {
                            SmsEntity smsEntity = (SmsEntity) filterResult.mData;
                            AresEngineFactor aresEngineFactor = a.this.qc.getAresEngineFactor();
                            aresEngineFactor.getSysDao().remove(smsEntity);
                            IEntityConverter entityConverter = aresEngineFactor.getEntityConverter();
                            if (entityConverter != null) {
                                smsEntity = entityConverter.convert(smsEntity);
                            }
                            aresEngineFactor.getPrivateSmsDao().insert(smsEntity, filterResult);
                        }
                    });
                    a(filterResult);
                }
            });
        }

        /* renamed from: b */
        protected FilterResult a(SmsEntity smsEntity, Object... objArr) {
            return this.qb.a(smsEntity, getConfig(), objArr);
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(1, 2);
            return filterConfig;
        }
    }

    private static final class b extends DataMonitor<SmsEntity> {
        private Context mContext;
        private ContentObserver qK;

        public b(Context context) {
            this.mContext = context;
            register();
        }

        private void register() {
            this.qK = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    super.onChange(z);
                    try {
                        TelephonyEntity lastSentSms = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao().getLastSentSms(10);
                        if (lastSentSms != null) {
                            ContentResolver contentResolver = b.this.mContext.getContentResolver();
                            contentResolver.unregisterContentObserver(this);
                            b.this.notifyDataReached(lastSentSms, new Object[0]);
                            contentResolver.registerContentObserver(MmsSms.CONTENT_CONVERSATIONS_URI, true, this);
                        }
                    } catch (NullPointerException e) {
                    }
                }
            };
            this.mContext.getContentResolver().registerContentObserver(MmsSms.CONTENT_CONVERSATIONS_URI, true, this.qK);
        }

        private void unregister() {
            if (this.qK != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.qK);
            }
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }
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
