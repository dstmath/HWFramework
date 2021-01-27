package com.huawei.nb.coordinator.helper;

import android.content.Context;
import com.huawei.nb.client.DataServiceProxy;
import com.huawei.nb.client.ServiceConnectCallback;
import com.huawei.nb.coordinator.NetWorkStateUtil;
import com.huawei.nb.coordinator.common.CoordinatorSwitchParameter;
import com.huawei.nb.model.coordinator.CoordinatorAudit;
import com.huawei.nb.model.coordinator.CoordinatorSwitch;
import com.huawei.nb.query.Query;
import com.huawei.nb.utils.logger.DSLog;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class HelperDatabaseManager {
    private static final String TAG = "HelperDatabaseManager";
    private static final long WAIT_FOR_CONNECT = 100;

    private HelperDatabaseManager() {
    }

    public static CoordinatorAudit createCoordinatorAudit(Context context) {
        CoordinatorAudit coordinatorAudit = new CoordinatorAudit();
        try {
            coordinatorAudit.setAppPackageName(context.getPackageName());
            coordinatorAudit.setUrl(" ");
            coordinatorAudit.setNetWorkState("" + NetWorkStateUtil.getCurrentNetWorkType(context));
            long currentTimeMillis = System.currentTimeMillis();
            coordinatorAudit.setTimeStamp(Long.valueOf(currentTimeMillis));
            coordinatorAudit.setRequestDate(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Long.valueOf(currentTimeMillis)));
            coordinatorAudit.setSuccessTransferTime(0L);
            coordinatorAudit.setSuccessVerifyTime(Long.valueOf(currentTimeMillis));
            coordinatorAudit.setDataSize(0L);
            coordinatorAudit.setIsNeedRetry(0L);
        } catch (Throwable th) {
            DSLog.e("HelperDatabaseManager caught a throwable when create CooxxxxxtorAxxxt." + th.getMessage(), new Object[0]);
        }
        return coordinatorAudit;
    }

    public static void insertCoordinatorAudit(Context context, CoordinatorAudit coordinatorAudit) {
        new Thread(new Runnable(context, coordinatorAudit) {
            /* class com.huawei.nb.coordinator.helper.$$Lambda$HelperDatabaseManager$oyJtIrbigUYknoi383ZSBCPL9yU */
            private final /* synthetic */ Context f$0;
            private final /* synthetic */ CoordinatorAudit f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HelperDatabaseManager.lambda$insertCoordinatorAudit$0(this.f$0, this.f$1);
            }
        }).start();
    }

    static /* synthetic */ void lambda$insertCoordinatorAudit$0(Context context, CoordinatorAudit coordinatorAudit) {
        try {
            DataServiceProxy dataServiceProxy = new DataServiceProxy(context);
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            dataServiceProxy.connect(new ServiceConnectCallback() {
                /* class com.huawei.nb.coordinator.helper.HelperDatabaseManager.AnonymousClass1 */

                @Override // com.huawei.nb.client.ServiceConnectCallback
                public void onDisconnect() {
                }

                @Override // com.huawei.nb.client.ServiceConnectCallback
                public void onConnect() {
                    countDownLatch.countDown();
                }
            });
            try {
                if (countDownLatch.await(WAIT_FOR_CONNECT, TimeUnit.MILLISECONDS)) {
                    DSLog.d("HelperDatabaseManager Success to connect DataService.", new Object[0]);
                    if (dataServiceProxy.executeInsert((DataServiceProxy) coordinatorAudit) != null) {
                        DSLog.d("Success to insert to CooxxxxxtorAxxxt.", new Object[0]);
                    } else {
                        DSLog.e("Fail to insert to CooxxxxxtorAxxxt, error: insertedResInfo instanceof CooxxxxxtorAxxxt is false.", new Object[0]);
                    }
                    dataServiceProxy.disconnect();
                    return;
                }
                DSLog.e("HelperDatabaseManager Fail to connect DataService.", new Object[0]);
                dataServiceProxy.disconnect();
            } catch (InterruptedException e) {
                DSLog.e("Get Coordinator Service Flag InterruptedException:" + e.getMessage(), new Object[0]);
            }
        } catch (Throwable th) {
            DSLog.e("HelperDatabaseManager caught a throwable when insert CooxxxxxtorAxxxt." + th.getMessage(), new Object[0]);
        }
    }

    public static boolean getCoordinatorServiceFlag(Context context) {
        try {
            DataServiceProxy dataServiceProxy = new DataServiceProxy(context);
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            dataServiceProxy.connect(new ServiceConnectCallback() {
                /* class com.huawei.nb.coordinator.helper.HelperDatabaseManager.AnonymousClass2 */

                @Override // com.huawei.nb.client.ServiceConnectCallback
                public void onDisconnect() {
                }

                @Override // com.huawei.nb.client.ServiceConnectCallback
                public void onConnect() {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                DSLog.e("Get Coordinator Service Flag InterruptedException:" + e.getMessage(), new Object[0]);
            }
            List executeQuery = dataServiceProxy.executeQuery(Query.select(CoordinatorSwitch.class).equalTo("serviceName", CoordinatorSwitchParameter.TRAVELASSISTANT));
            dataServiceProxy.disconnect();
            if (executeQuery != null && executeQuery.size() > 0) {
                return ((CoordinatorSwitch) executeQuery.get(0)).getIsSwitchOn();
            }
        } catch (Throwable th) {
            DSLog.e("HelperDatabaseManager caught a throwable when get the CoordinatorServiceFlag." + th.getMessage(), new Object[0]);
        }
        return false;
    }
}
