package android.telephony.data;

import android.app.Service;
import android.content.Intent;
import android.net.LinkProperties;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.data.IDataService;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public abstract class DataService extends Service {
    private static final int DATA_SERVICE_CREATE_DATA_SERVICE_PROVIDER = 1;
    public static final String DATA_SERVICE_EXTRA_SLOT_ID = "android.telephony.data.extra.SLOT_ID";
    private static final int DATA_SERVICE_INDICATION_DATA_CALL_LIST_CHANGED = 11;
    public static final String DATA_SERVICE_INTERFACE = "android.telephony.data.DataService";
    private static final int DATA_SERVICE_REMOVE_ALL_DATA_SERVICE_PROVIDERS = 3;
    private static final int DATA_SERVICE_REMOVE_DATA_SERVICE_PROVIDER = 2;
    private static final int DATA_SERVICE_REQUEST_DEACTIVATE_DATA_CALL = 5;
    private static final int DATA_SERVICE_REQUEST_GET_DATA_CALL_LIST = 8;
    private static final int DATA_SERVICE_REQUEST_REGISTER_DATA_CALL_LIST_CHANGED = 9;
    private static final int DATA_SERVICE_REQUEST_SETUP_DATA_CALL = 4;
    private static final int DATA_SERVICE_REQUEST_SET_DATA_PROFILE = 7;
    private static final int DATA_SERVICE_REQUEST_SET_INITIAL_ATTACH_APN = 6;
    private static final int DATA_SERVICE_REQUEST_UNREGISTER_DATA_CALL_LIST_CHANGED = 10;
    public static final int REQUEST_REASON_HANDOVER = 3;
    public static final int REQUEST_REASON_NORMAL = 1;
    public static final int REQUEST_REASON_SHUTDOWN = 2;
    private static final String TAG = DataService.class.getSimpleName();
    @VisibleForTesting
    public final IDataServiceWrapper mBinder = new IDataServiceWrapper();
    /* access modifiers changed from: private */
    public final DataServiceHandler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread(TAG);
    /* access modifiers changed from: private */
    public final SparseArray<DataServiceProvider> mServiceMap = new SparseArray<>();

    private static final class DataCallListChangedIndication {
        public final IDataServiceCallback callback;
        public final List<DataCallResponse> dataCallList;

        DataCallListChangedIndication(List<DataCallResponse> dataCallList2, IDataServiceCallback callback2) {
            this.dataCallList = dataCallList2;
            this.callback = callback2;
        }
    }

    private class DataServiceHandler extends Handler {
        DataServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int slotId = message.arg1;
            DataServiceProvider serviceProvider = (DataServiceProvider) DataService.this.mServiceMap.get(slotId);
            DataServiceCallback dataServiceCallback = null;
            switch (message.what) {
                case 1:
                    DataServiceProvider serviceProvider2 = DataService.this.createDataServiceProvider(message.arg1);
                    if (serviceProvider2 != null) {
                        DataService.this.mServiceMap.put(slotId, serviceProvider2);
                        return;
                    }
                    return;
                case 2:
                    if (serviceProvider != null) {
                        serviceProvider.onDestroy();
                        DataService.this.mServiceMap.remove(slotId);
                        return;
                    }
                    return;
                case 3:
                    for (int i = 0; i < DataService.this.mServiceMap.size(); i++) {
                        DataServiceProvider serviceProvider3 = (DataServiceProvider) DataService.this.mServiceMap.get(i);
                        if (serviceProvider3 != null) {
                            serviceProvider3.onDestroy();
                        }
                    }
                    DataService.this.mServiceMap.clear();
                    return;
                case 4:
                    if (serviceProvider != null) {
                        SetupDataCallRequest setupDataCallRequest = (SetupDataCallRequest) message.obj;
                        serviceProvider.setupDataCall(setupDataCallRequest.accessNetworkType, setupDataCallRequest.dataProfile, setupDataCallRequest.isRoaming, setupDataCallRequest.allowRoaming, setupDataCallRequest.reason, setupDataCallRequest.linkProperties, setupDataCallRequest.callback != null ? new DataServiceCallback(setupDataCallRequest.callback) : null);
                        return;
                    }
                    return;
                case 5:
                    if (serviceProvider != null) {
                        DeactivateDataCallRequest deactivateDataCallRequest = (DeactivateDataCallRequest) message.obj;
                        int i2 = deactivateDataCallRequest.cid;
                        int i3 = deactivateDataCallRequest.reason;
                        if (deactivateDataCallRequest.callback != null) {
                            dataServiceCallback = new DataServiceCallback(deactivateDataCallRequest.callback);
                        }
                        serviceProvider.deactivateDataCall(i2, i3, dataServiceCallback);
                        return;
                    }
                    return;
                case 6:
                    if (serviceProvider != null) {
                        SetInitialAttachApnRequest setInitialAttachApnRequest = (SetInitialAttachApnRequest) message.obj;
                        DataProfile dataProfile = setInitialAttachApnRequest.dataProfile;
                        boolean z = setInitialAttachApnRequest.isRoaming;
                        if (setInitialAttachApnRequest.callback != null) {
                            dataServiceCallback = new DataServiceCallback(setInitialAttachApnRequest.callback);
                        }
                        serviceProvider.setInitialAttachApn(dataProfile, z, dataServiceCallback);
                        return;
                    }
                    return;
                case 7:
                    if (serviceProvider != null) {
                        SetDataProfileRequest setDataProfileRequest = (SetDataProfileRequest) message.obj;
                        List<DataProfile> list = setDataProfileRequest.dps;
                        boolean z2 = setDataProfileRequest.isRoaming;
                        if (setDataProfileRequest.callback != null) {
                            dataServiceCallback = new DataServiceCallback(setDataProfileRequest.callback);
                        }
                        serviceProvider.setDataProfile(list, z2, dataServiceCallback);
                        return;
                    }
                    return;
                case 8:
                    if (serviceProvider != null) {
                        serviceProvider.getDataCallList(new DataServiceCallback((IDataServiceCallback) message.obj));
                        return;
                    }
                    return;
                case 9:
                    if (serviceProvider != null) {
                        serviceProvider.registerForDataCallListChanged((IDataServiceCallback) message.obj);
                        return;
                    }
                    return;
                case 10:
                    if (serviceProvider != null) {
                        serviceProvider.unregisterForDataCallListChanged((IDataServiceCallback) message.obj);
                        return;
                    }
                    return;
                case 11:
                    if (serviceProvider != null) {
                        DataCallListChangedIndication indication = (DataCallListChangedIndication) message.obj;
                        try {
                            indication.callback.onDataCallListChanged(indication.dataCallList);
                            return;
                        } catch (RemoteException e) {
                            DataService dataService = DataService.this;
                            dataService.loge("Failed to call onDataCallListChanged. " + e);
                            return;
                        }
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public class DataServiceProvider {
        private final List<IDataServiceCallback> mDataCallListChangedCallbacks = new ArrayList();
        private final int mSlotId;

        public DataServiceProvider(int slotId) {
            this.mSlotId = slotId;
        }

        public final int getSlotId() {
            return this.mSlotId;
        }

        public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, DataServiceCallback callback) {
            callback.onSetupDataCallComplete(1, null);
        }

        public void deactivateDataCall(int cid, int reason, DataServiceCallback callback) {
            callback.onDeactivateDataCallComplete(1);
        }

        public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, DataServiceCallback callback) {
            callback.onSetInitialAttachApnComplete(1);
        }

        public void setDataProfile(List<DataProfile> list, boolean isRoaming, DataServiceCallback callback) {
            callback.onSetDataProfileComplete(1);
        }

        public void getDataCallList(DataServiceCallback callback) {
            callback.onGetDataCallListComplete(1, null);
        }

        /* access modifiers changed from: private */
        public void registerForDataCallListChanged(IDataServiceCallback callback) {
            synchronized (this.mDataCallListChangedCallbacks) {
                this.mDataCallListChangedCallbacks.add(callback);
            }
        }

        /* access modifiers changed from: private */
        public void unregisterForDataCallListChanged(IDataServiceCallback callback) {
            synchronized (this.mDataCallListChangedCallbacks) {
                this.mDataCallListChangedCallbacks.remove(callback);
            }
        }

        public final void notifyDataCallListChanged(List<DataCallResponse> dataCallList) {
            synchronized (this.mDataCallListChangedCallbacks) {
                for (IDataServiceCallback callback : this.mDataCallListChangedCallbacks) {
                    DataService.this.mHandler.obtainMessage(11, this.mSlotId, 0, new DataCallListChangedIndication(dataCallList, callback)).sendToTarget();
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onDestroy() {
            this.mDataCallListChangedCallbacks.clear();
        }
    }

    private static final class DeactivateDataCallRequest {
        public final IDataServiceCallback callback;
        public final int cid;
        public final int reason;

        DeactivateDataCallRequest(int cid2, int reason2, IDataServiceCallback callback2) {
            this.cid = cid2;
            this.reason = reason2;
            this.callback = callback2;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DeactivateDataReason {
    }

    private class IDataServiceWrapper extends IDataService.Stub {
        private IDataServiceWrapper() {
        }

        public void createDataServiceProvider(int slotId) {
            DataService.this.mHandler.obtainMessage(1, slotId, 0).sendToTarget();
        }

        public void removeDataServiceProvider(int slotId) {
            DataService.this.mHandler.obtainMessage(2, slotId, 0).sendToTarget();
        }

        public void setupDataCall(int slotId, int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, IDataServiceCallback callback) {
            DataServiceHandler access$100 = DataService.this.mHandler;
            SetupDataCallRequest setupDataCallRequest = new SetupDataCallRequest(accessNetworkType, dataProfile, isRoaming, allowRoaming, reason, linkProperties, callback);
            access$100.obtainMessage(4, slotId, 0, setupDataCallRequest).sendToTarget();
        }

        public void deactivateDataCall(int slotId, int cid, int reason, IDataServiceCallback callback) {
            DataService.this.mHandler.obtainMessage(5, slotId, 0, new DeactivateDataCallRequest(cid, reason, callback)).sendToTarget();
        }

        public void setInitialAttachApn(int slotId, DataProfile dataProfile, boolean isRoaming, IDataServiceCallback callback) {
            DataService.this.mHandler.obtainMessage(6, slotId, 0, new SetInitialAttachApnRequest(dataProfile, isRoaming, callback)).sendToTarget();
        }

        public void setDataProfile(int slotId, List<DataProfile> dps, boolean isRoaming, IDataServiceCallback callback) {
            DataService.this.mHandler.obtainMessage(7, slotId, 0, new SetDataProfileRequest(dps, isRoaming, callback)).sendToTarget();
        }

        public void getDataCallList(int slotId, IDataServiceCallback callback) {
            if (callback == null) {
                DataService.this.loge("getDataCallList: callback is null");
            } else {
                DataService.this.mHandler.obtainMessage(8, slotId, 0, callback).sendToTarget();
            }
        }

        public void registerForDataCallListChanged(int slotId, IDataServiceCallback callback) {
            if (callback == null) {
                DataService.this.loge("registerForDataCallListChanged: callback is null");
            } else {
                DataService.this.mHandler.obtainMessage(9, slotId, 0, callback).sendToTarget();
            }
        }

        public void unregisterForDataCallListChanged(int slotId, IDataServiceCallback callback) {
            if (callback == null) {
                DataService.this.loge("unregisterForDataCallListChanged: callback is null");
            } else {
                DataService.this.mHandler.obtainMessage(10, slotId, 0, callback).sendToTarget();
            }
        }
    }

    private static final class SetDataProfileRequest {
        public final IDataServiceCallback callback;
        public final List<DataProfile> dps;
        public final boolean isRoaming;

        SetDataProfileRequest(List<DataProfile> dps2, boolean isRoaming2, IDataServiceCallback callback2) {
            this.dps = dps2;
            this.isRoaming = isRoaming2;
            this.callback = callback2;
        }
    }

    private static final class SetInitialAttachApnRequest {
        public final IDataServiceCallback callback;
        public final DataProfile dataProfile;
        public final boolean isRoaming;

        SetInitialAttachApnRequest(DataProfile dataProfile2, boolean isRoaming2, IDataServiceCallback callback2) {
            this.dataProfile = dataProfile2;
            this.isRoaming = isRoaming2;
            this.callback = callback2;
        }
    }

    private static final class SetupDataCallRequest {
        public final int accessNetworkType;
        public final boolean allowRoaming;
        public final IDataServiceCallback callback;
        public final DataProfile dataProfile;
        public final boolean isRoaming;
        public final LinkProperties linkProperties;
        public final int reason;

        SetupDataCallRequest(int accessNetworkType2, DataProfile dataProfile2, boolean isRoaming2, boolean allowRoaming2, int reason2, LinkProperties linkProperties2, IDataServiceCallback callback2) {
            this.accessNetworkType = accessNetworkType2;
            this.dataProfile = dataProfile2;
            this.isRoaming = isRoaming2;
            this.allowRoaming = allowRoaming2;
            this.linkProperties = linkProperties2;
            this.reason = reason2;
            this.callback = callback2;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SetupDataReason {
    }

    public abstract DataServiceProvider createDataServiceProvider(int i);

    public DataService() {
        this.mHandlerThread.start();
        this.mHandler = new DataServiceHandler(this.mHandlerThread.getLooper());
        log("Data service created");
    }

    public IBinder onBind(Intent intent) {
        if (intent != null && DATA_SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        loge("Unexpected intent " + intent);
        return null;
    }

    public boolean onUnbind(Intent intent) {
        this.mHandler.obtainMessage(3).sendToTarget();
        return false;
    }

    public void onDestroy() {
        this.mHandlerThread.quit();
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Rlog.e(TAG, s);
    }
}
