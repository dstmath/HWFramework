package com.huawei.opcollect.odmf;

import android.content.Context;
import com.huawei.nb.client.DataServiceProxy;
import com.huawei.nb.client.ServiceConnectCallback;
import com.huawei.nb.client.kv.KvClient;
import com.huawei.nb.kv.KCompositeString;
import com.huawei.nb.kv.VJson;
import com.huawei.nb.model.meta.DataLifeCycle;
import com.huawei.nb.notification.ModelObserver;
import com.huawei.nb.notification.ObserverType;
import com.huawei.nb.query.Query;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OdmfHelper {
    public static final String ODMF_API_VERSION_2_11_2 = "2.11.2";
    public static final String ODMF_API_VERSION_2_11_3 = "2.11.3";
    public static final String ODMF_API_VERSION_2_11_6 = "2.11.6";
    public static final String ODMF_API_VERSION_2_11_7 = "2.11.7";
    private static final int RECONNECT_TIMES = 10;
    private static final String TAG = "OdmfHelper";
    /* access modifiers changed from: private */
    public int count = 0;
    /* access modifiers changed from: private */
    public volatile boolean hasConnected = false;
    /* access modifiers changed from: private */
    public CountDownLatch latch;
    private DataServiceProxy mDataServiceProxy = null;
    private KvClient<KCompositeString, VJson> mKvClient;
    protected ServiceConnectCallback odmfConnectCallback = new ServiceConnectCallback() {
        public void onConnect() {
            OPCollectLog.i(OdmfHelper.TAG, "Odmf service is connected");
            synchronized (OdmfHelper.this) {
                int unused = OdmfHelper.this.count = 0;
            }
            boolean unused2 = OdmfHelper.this.hasConnected = true;
            OdmfHelper.this.latch.countDown();
            OdmfCollectScheduler.getInstance().getCtrlHandler().sendEmptyMessage(OdmfCollectScheduler.MSG_ODMF_CONNECTED);
        }

        public void onDisconnect() {
            OPCollectLog.w(OdmfHelper.TAG, "Odmf service is disconnceted");
            boolean unused = OdmfHelper.this.hasConnected = false;
            OdmfCollectScheduler.getInstance().getCtrlHandler().sendEmptyMessage(OdmfCollectScheduler.MSG_ODMF_DISCONNECTED);
        }
    };

    public OdmfHelper(Context context) {
        OPCollectLog.r(TAG, TAG);
        this.latch = new CountDownLatch(1);
        this.hasConnected = false;
        this.mKvClient = new KvClient<>(context);
        if (OPCollectUtils.checkODMFApiVersion(context, ODMF_API_VERSION_2_11_7)) {
            this.mDataServiceProxy = this.mKvClient.getDataServiceProxy();
        } else {
            this.mDataServiceProxy = new DataServiceProxy(context);
        }
    }

    public boolean connectOdmfService() {
        if (this.hasConnected) {
            return true;
        }
        OPCollectLog.r(TAG, "connectOdmfService");
        try {
            this.mKvClient.connect(this.odmfConnectCallback);
            try {
                this.latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                OPCollectLog.e(TAG, "connectOdmfService " + e.getMessage());
            }
            if (this.hasConnected) {
                return true;
            }
            OPCollectLog.e(TAG, "connect failed");
            return false;
        } catch (RuntimeException e2) {
            OPCollectLog.e(TAG, "connectOdmfService " + e2.getMessage());
            return false;
        }
    }

    private void checkReConnectOdmfService() {
        synchronized (this) {
            this.count++;
            if (this.count > 10) {
                this.count = 0;
                connectOdmfService();
            }
        }
    }

    public AManagedObject insertManageObject(AManagedObject rawData) {
        AManagedObject rInfo = null;
        if (rawData != null) {
            if (!this.hasConnected) {
                checkReConnectOdmfService();
                OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            } else {
                rInfo = null;
                try {
                    rInfo = this.mDataServiceProxy.executeInsert(rawData);
                } catch (RuntimeException e) {
                    OPCollectLog.e(TAG, "insertManageObject " + e.getMessage());
                }
                if (rInfo != null) {
                    OPCollectLog.r(TAG, "insert " + rInfo.getEntityName() + " success ");
                } else {
                    OPCollectLog.r(TAG, "insert " + rawData.getEntityName() + " failed ");
                }
            }
        }
        return rInfo;
    }

    public List<AManagedObject> bulkInsertManageObject(List<AManagedObject> rawDatas) {
        List<AManagedObject> rInfo = null;
        if (rawDatas == null) {
            return null;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return null;
        }
        try {
            rInfo = this.mDataServiceProxy.executeInsert(rawDatas);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "bulkInsertManageObject " + e.getMessage());
        }
        if (rInfo != null) {
            if (rInfo.get(0) != null) {
                OPCollectLog.i(TAG, "insert " + rInfo.get(0).getDatabaseName() + " success ");
            }
        } else if (rawDatas.get(0) != null) {
            OPCollectLog.r(TAG, "insert " + rawDatas.get(0).getDatabaseName() + " failed ");
        }
        return rInfo;
    }

    public long queryManageObjectCount(Query query) {
        long count2 = -1;
        if (query == null) {
            return count2;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.e(TAG, "Odmf service is disconnceted, will reconnect later");
            return count2;
        }
        try {
            count2 = this.mDataServiceProxy.executeCountQuery(query);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "queryManageObjectCount " + e.getMessage());
        }
        return count2;
    }

    public List<AManagedObject> queryManageObject(Query query) {
        List<AManagedObject> objects = null;
        if (query == null) {
            return null;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.e(TAG, "Odmf service is disconnceted, will reconnect later");
            return null;
        }
        try {
            objects = this.mDataServiceProxy.executeQuery(query);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "queryManageObject " + e.getMessage());
        }
        return objects;
    }

    public AManagedObject querySingleManageObject(Query query) {
        AManagedObject aManagedObject = null;
        if (query == null) {
            return null;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.e(TAG, "Odmf service is disconnceted, will reconnect later");
            return null;
        }
        try {
            aManagedObject = this.mDataServiceProxy.executeSingleQuery(query);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "querySingleManageObject " + e.getMessage());
        }
        return aManagedObject;
    }

    public boolean updateManageObject(AManagedObject rawData) {
        boolean ret = false;
        if (rawData == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return ret;
        }
        try {
            ret = this.mDataServiceProxy.executeUpdate(rawData);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "updateManageObject " + e.getMessage());
        }
        return ret;
    }

    public boolean updateManageObjects(List<AManagedObject> rawDatas) {
        boolean ret = false;
        if (rawDatas == null || rawDatas.size() == 0) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return ret;
        }
        try {
            ret = this.mDataServiceProxy.executeUpdate(rawDatas);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "updateManageObjects " + e.getMessage());
        }
        return ret;
    }

    public boolean deleteManageObject(AManagedObject rawData) {
        boolean ret = false;
        if (rawData == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return ret;
        }
        try {
            ret = this.mDataServiceProxy.executeDelete(rawData);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "deleteManageObjects " + e.getMessage());
        }
        return ret;
    }

    public boolean deleteManageObjects(List<AManagedObject> rawDatas) {
        boolean ret = false;
        if (rawDatas == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return ret;
        }
        try {
            ret = this.mDataServiceProxy.executeDelete(rawDatas);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "deleteManageObjects " + e.getMessage());
        }
        return ret;
    }

    public boolean subscribeManagedObject(Class clazz, ObserverType type, ModelObserver observer) {
        boolean ret = false;
        if (observer == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return ret;
        }
        try {
            ret = this.mDataServiceProxy.subscribe(clazz, type, observer);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "subscribeManagedObject " + e.getMessage());
        }
        return ret;
    }

    public boolean unSubscribeManagedObject(Class clazz, ObserverType type, ModelObserver observer) {
        boolean ret = false;
        if (observer == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return ret;
        }
        try {
            ret = this.mDataServiceProxy.unSubscribe(clazz, type, observer);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "unSubscribeManagedObject " + e.getMessage());
        }
        return ret;
    }

    public int addDataLifeCycleConfig(String mDBName, String mTableName, String mFieldName, int mMode, int mCount) {
        int ret = 1;
        if (mDBName == null || mTableName == null || mFieldName == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return 2;
        }
        try {
            ret = this.mDataServiceProxy.addDataLifeCycleConfig(mDBName, mTableName, mFieldName, mMode, mCount);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "addDataLifeCycleConfig " + e.getMessage());
        }
        return ret;
    }

    public int removeDataLifeCycleConfig(String mDBName, String mTableName, String mFieldName, int mMode, int mCount) {
        int ret = 1;
        if (mDBName == null || mTableName == null || mFieldName == null) {
            return ret;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return 2;
        }
        try {
            ret = this.mDataServiceProxy.removeDataLifeCycleConfig(mDBName, mTableName, mFieldName, mMode, mCount);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "removeDataLifeCycleConfig " + e.getMessage());
        }
        return ret;
    }

    public List<DataLifeCycle> queryDataLifeCycleConfig(String dbName, String tableName) {
        List<DataLifeCycle> dlcs = null;
        if (dbName == null || tableName == null) {
            return null;
        }
        if (!this.hasConnected) {
            checkReConnectOdmfService();
            OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
            return null;
        }
        try {
            dlcs = this.mDataServiceProxy.queryDataLifeCycleConfig(dbName, tableName);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "queryDataLifeCycleConfig " + e.getMessage());
        }
        return dlcs;
    }

    public boolean put(KCompositeString key, VJson value) {
        if (this.hasConnected) {
            return this.mKvClient.put(key, value);
        }
        checkReConnectOdmfService();
        OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
        return false;
    }

    public VJson get(KCompositeString key) {
        if (this.hasConnected) {
            return this.mKvClient.get(key);
        }
        checkReConnectOdmfService();
        OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
        return null;
    }

    public boolean grant(KCompositeString key, String packageName, int authority) {
        if (this.hasConnected) {
            return this.mKvClient.grant(key, packageName, authority);
        }
        checkReConnectOdmfService();
        OPCollectLog.w(TAG, "Odmf service is disconnceted, will reconnect later");
        return false;
    }
}
