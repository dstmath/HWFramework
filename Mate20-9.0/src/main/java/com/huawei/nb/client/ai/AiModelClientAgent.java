package com.huawei.nb.client.ai;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.nb.ai.AiModelRequest;
import com.huawei.nb.ai.AiModelResponse;
import com.huawei.nb.client.DataServiceProxy;
import com.huawei.nb.client.DualProxy;
import com.huawei.nb.client.callback.AIFetchCallback;
import com.huawei.nb.client.callback.DeleteResInfoCallBack;
import com.huawei.nb.client.callback.DeleteResInfoCallBackAgent;
import com.huawei.nb.client.callback.UpdatePackageCallBack;
import com.huawei.nb.client.callback.UpdatePackageCallBackAgent;
import com.huawei.nb.client.callback.UpdatePackageCheckCallBack;
import com.huawei.nb.client.callback.UpdatePackageCheckCallBackAgent;
import com.huawei.nb.client.callback.UpdatePackageControllableCallBack;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.model.aimodel.AiModel;
import com.huawei.nb.model.coordinator.CoordinatorSwitch;
import com.huawei.nb.model.coordinator.ResourceInformation;
import com.huawei.nb.notification.AILocalObservable;
import com.huawei.nb.notification.ModelObserver;
import com.huawei.nb.notification.ModelObserverInfo;
import com.huawei.nb.notification.ObserverType;
import com.huawei.nb.query.Query;
import com.huawei.nb.security.RSAEncryptUtils;
import com.huawei.nb.service.IAIServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AiModelClientAgent extends DualProxy<DataServiceProxy, IAIServiceCall> {
    private static final String AI_SERVICE_ACTION = "com.huawei.nb.service.AIService.START";
    private static final String AI_SERVICE_NAME = "NaturalBase AI Service";
    private static final int DELETE_WAIT_TIMEOUT = 10;
    private static final String TAG = "AiModelClientAgent";
    private volatile Map<String, Object> keyMap;
    private final Object lock = new Object();

    private static class DeleteResInfoFromDbCallBack implements DeleteResInfoCallBack {
        private boolean[] isDelete = null;
        private CountDownLatch latch = null;

        DeleteResInfoFromDbCallBack(boolean[] isDelete2, CountDownLatch latch2) {
            this.isDelete = isDelete2;
            this.latch = latch2;
        }

        public void onSuccess() {
            this.isDelete[0] = true;
            this.latch.countDown();
        }

        public void onFailure(int errorCode, String errorMessage) {
            if (errorMessage == null) {
                errorMessage = "";
            }
            DSLog.dt(AiModelClientAgent.TAG, "Fail to delete res info,error: errorCode = " + errorCode + "errorMessage = " + errorMessage, new Object[0]);
            this.isDelete[0] = false;
            this.latch.countDown();
        }
    }

    private static class DeleteUpdatedResInfoCallBack extends DeleteResInfoCallBackAgent {
        private DeleteResInfoCallBack mCb = null;

        DeleteUpdatedResInfoCallBack(DeleteResInfoCallBack cb) {
            this.mCb = cb;
        }

        public void onSuccess() {
            this.mCb.onSuccess();
        }

        public void onFailure(int errorCode, String errorMessage) {
            if (errorMessage == null) {
                errorMessage = "";
            }
            this.mCb.onFailure(errorCode, errorMessage);
        }
    }

    public AiModelClientAgent(Context context) {
        super(context, AI_SERVICE_NAME, AI_SERVICE_ACTION, new DataServiceProxy(context));
        generateKeysIfNeeded();
    }

    public void disconnect() {
        disconnectInner();
    }

    /* access modifiers changed from: protected */
    public IAIServiceCall asInterface(IBinder binder) {
        return IAIServiceCall.Stub.asInterface(binder);
    }

    /* access modifiers changed from: protected */
    public AILocalObservable newLocalObservable() {
        return new AILocalObservable(this.callbackManager);
    }

    private void generateKeysIfNeeded() {
        synchronized (this.lock) {
            if (this.keyMap == null) {
                this.keyMap = RSAEncryptUtils.generateKeyPair();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: setPrivateKey */
    public void bridge$lambda$0$AiModelClientAgent(AiModelResponse response) {
        if (response != null) {
            response.setPrivateKey(RSAEncryptUtils.getPrivateKey(this.keyMap));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: setPublicKey */
    public void bridge$lambda$1$AiModelClientAgent(AiModelRequest request) {
        if (request != null) {
            request.setPublicKey(RSAEncryptUtils.getPublicKey(this.keyMap));
        }
    }

    private boolean batchValidRequest(List<AiModelRequest> requestList) {
        for (AiModelRequest request : requestList) {
            if (request != null) {
                if (!request.isValid()) {
                }
            }
            return false;
        }
        return true;
    }

    private boolean batchSetPrivateKey(List<AiModelResponse> responseList) {
        if (this.keyMap == null) {
            return false;
        }
        responseList.forEach(new AiModelClientAgent$$Lambda$0(this));
        return true;
    }

    private boolean batchSetPublicKey(List<AiModelRequest> requestList) {
        generateKeysIfNeeded();
        if (this.keyMap == null) {
            return false;
        }
        requestList.forEach(new AiModelClientAgent$$Lambda$1(this));
        return true;
    }

    public AiModelResponse requestAiModel(AiModelRequest request) {
        List<AiModelResponse> responseList = requestAiModel((List<AiModelRequest>) Arrays.asList(new AiModelRequest[]{request}));
        if (responseList == null || responseList.isEmpty()) {
            return null;
        }
        return responseList.get(0);
    }

    public List<AiModelResponse> requestAiModel(List<AiModelRequest> requestList) {
        List<AiModelResponse> responseList;
        int size;
        if (requestList == null || !batchValidRequest(requestList)) {
            DSLog.et(TAG, "Failed to get ai models, error: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "Failed to request Ai model, error: not connected to ai service.", new Object[0]);
            return null;
        } else if (!batchSetPublicKey(requestList)) {
            DSLog.et(TAG, "Failed to request Ai model, error: public key unavailable.", new Object[0]);
            return null;
        } else {
            ObjectContainer<AiModelRequest> requestContainer = new ObjectContainer<>(AiModelRequest.class, requestList);
            if (this.callbackTimeout > 0) {
                responseList = requestAiModelAsync(requestContainer);
            } else {
                responseList = requestAiModel((ObjectContainer) requestContainer);
            }
            if (responseList == null || responseList.size() != requestList.size()) {
                Object[] objArr = new Object[2];
                if (responseList == null) {
                    size = 0;
                } else {
                    size = responseList.size();
                }
                objArr[0] = Integer.valueOf(size);
                objArr[1] = Integer.valueOf(requestList.size());
                DSLog.et(TAG, "Failed to get ai models, error: just get %s for %s requests.", objArr);
                return null;
            } else if (batchSetPrivateKey(responseList)) {
                return responseList;
            } else {
                DSLog.et(TAG, "Failed to request Ai model, error: private key unavailable.", new Object[0]);
                return null;
            }
        }
    }

    private List<AiModelResponse> requestAiModel(ObjectContainer requestContainer) {
        try {
            ObjectContainer responseContainer = ((IAIServiceCall) this.remote).requestAiModel(requestContainer);
            if (responseContainer == null) {
                return null;
            }
            return responseContainer.get();
        } catch (RemoteException | RuntimeException e) {
            return null;
        }
    }

    private List<AiModelResponse> requestAiModelAsync(ObjectContainer requestContainer) {
        AIFetchCallback callback = (AIFetchCallback) this.callbackManager.createCallBack(AiModelClientAgent$$Lambda$2.$instance);
        try {
            return callback.await(((IAIServiceCall) this.remote).requestAiModelAsync(requestContainer, callback), this.callbackTimeout);
        } catch (RemoteException | RuntimeException e) {
            return null;
        }
    }

    public ResourceInformation queryResInfo(String resid) {
        if (TextUtils.isEmpty(resid)) {
            DSLog.et(TAG, "Failed to query model res info, error: invalid parameter.", new Object[0]);
            return null;
        }
        return (ResourceInformation) ((DataServiceProxy) this.secondary).executeSingleQuery(Query.select(ResourceInformation.class).equalTo("resid", resid));
    }

    public boolean deleteResInfo(ResourceInformation resourceInformation) {
        if (!isAllowChangeInfo(resourceInformation)) {
            DSLog.et(TAG, "Fail to delete res info, error: not allow to delete.", new Object[0]);
            return false;
        }
        boolean[] isDelete = {false};
        CountDownLatch latch = new CountDownLatch(1);
        deleteResInfo(Arrays.asList(new ResourceInformation[]{resourceInformation}), new DeleteResInfoFromDbCallBack(isDelete, latch));
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                isDelete[0] = false;
                DSLog.dt(TAG, "Fail to delete res info,error: wait for callback timeout.", new Object[0]);
            }
        } catch (InterruptedException e) {
            isDelete[0] = false;
        }
        DSLog.dt(TAG, "res info is deleted, %s.", Boolean.valueOf(isDelete[0]));
        return isDelete[0];
    }

    public boolean updateResInfo(ResourceInformation resourceInformation) {
        if (!isAllowChangeInfo(resourceInformation)) {
            DSLog.et(TAG, "Fail to update res info, error: not allow to update.", new Object[0]);
            return false;
        }
        ResourceInformation resInfo = (ResourceInformation) ((DataServiceProxy) this.secondary).executeSingleQuery(Query.select(ResourceInformation.class).equalTo("resid", resourceInformation.getResid()));
        if (resInfo == null) {
            DSLog.et(TAG, "Fail to update res info, error: query resid failed.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "Failed to update res info, error: not connected to ai service.", new Object[0]);
            return false;
        } else {
            try {
                resInfo.setAppVersion(resourceInformation.getAppVersion());
                boolean isUpdate = ((IAIServiceCall) this.remote).updateResInfoAgent(new ObjectContainer(ResourceInformation.class, Collections.singletonList(resInfo), this.pkgName));
                DSLog.dt(TAG, "res info is updated, %s.", Boolean.valueOf(isUpdate));
                return isUpdate;
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "Fail to update res info, error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public ResourceInformation insertResInfo(ResourceInformation resourceInformation) {
        if (!isResInfoValid(resourceInformation)) {
            DSLog.et(TAG, "Fail to insert res info, error: resource information is invalid.", new Object[0]);
            return null;
        } else if (!presetResInfo(resourceInformation)) {
            DSLog.et(TAG, "Fail to insert res info, error: can not preset invalid information.", new Object[0]);
            return null;
        } else if (!isAllowChangeInfo(resourceInformation)) {
            DSLog.et(TAG, "Fail to insert res info, error: not allow to insert.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "Failed to insert res info, error: not connected to ai service.", new Object[0]);
            return null;
        } else {
            try {
                ObjectContainer insertResult = ((IAIServiceCall) this.remote).insertResInfoAgent(new ObjectContainer(ResourceInformation.class, Collections.singletonList(resourceInformation), this.pkgName));
                if (insertResult == null || insertResult.get().isEmpty()) {
                    DSLog.et(TAG, "Fail to insert res info, error: insert result is empty.", new Object[0]);
                    return null;
                } else if (insertResult.get().get(0) instanceof ResourceInformation) {
                    return (ResourceInformation) insertResult.get().get(0);
                } else {
                    DSLog.et(TAG, "Fail to insert res info, error: result not res info type.", new Object[0]);
                    return null;
                }
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "Fail to insert res info, error: %s.", e.getMessage());
                return null;
            }
        }
    }

    private boolean isAllowChangeInfo(ResourceInformation resourceInformation) {
        if (this.context.getPackageName() != null && resourceInformation != null && resourceInformation.getPackageName() != null) {
            return true;
        }
        DSLog.et(TAG, "Failed to change model res info, error: invalid parameter. Context package name is" + this.context.getPackageName(), new Object[0]);
        return false;
    }

    public CoordinatorSwitch querySwitch(String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            DSLog.et(TAG, "Failed to query switch, error: invalid parameter.", new Object[0]);
            return null;
        }
        CoordinatorSwitch coordinatorSwitch = (CoordinatorSwitch) ((DataServiceProxy) this.secondary).executeSingleQuery(Query.select(CoordinatorSwitch.class).equalTo("serviceName", serviceName));
        if (coordinatorSwitch != null) {
            DSLog.dt(TAG, "switch is found, value is %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
            return coordinatorSwitch;
        }
        DSLog.et(TAG, "switch is not found.", new Object[0]);
        return coordinatorSwitch;
    }

    public boolean deleteSwitch(CoordinatorSwitch coordinatorSwitch) {
        if (!isAllowChangeSwitch(coordinatorSwitch)) {
            return false;
        }
        boolean isDelete = ((DataServiceProxy) this.secondary).executeDelete(coordinatorSwitch);
        DSLog.dt(TAG, "switch is deleted %s.", Boolean.valueOf(isDelete));
        return isDelete;
    }

    public boolean updateSwitch(CoordinatorSwitch coordinatorSwitch) {
        if (!isAllowChangeSwitch(coordinatorSwitch)) {
            return false;
        }
        if (((DataServiceProxy) this.secondary).executeUpdate(coordinatorSwitch)) {
            DSLog.dt(TAG, "switch is update as %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
            return true;
        }
        DSLog.dt(TAG, "Failed to update switch as %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
        return false;
    }

    public CoordinatorSwitch insertSwitch(CoordinatorSwitch coordinatorSwitch) {
        if (!isAllowChangeSwitch(coordinatorSwitch)) {
            return null;
        }
        CoordinatorSwitch insertedSwitch = (CoordinatorSwitch) ((DataServiceProxy) this.secondary).executeInsert(coordinatorSwitch);
        if (insertedSwitch == null) {
            return null;
        }
        DSLog.dt(TAG, "switch inserted, value is %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
        return insertedSwitch;
    }

    private boolean isAllowChangeSwitch(CoordinatorSwitch coordinatorSwitch) {
        if (this.context.getPackageName() == null || coordinatorSwitch == null || coordinatorSwitch.getPackageName() == null) {
            DSLog.et(TAG, "Failed to change switch, error: invalid parameter.", new Object[0]);
            return false;
        } else if (this.context.getPackageName().equals(coordinatorSwitch.getPackageName())) {
            return true;
        } else {
            DSLog.et(TAG, "Failed to change switch, error: permission denied.", new Object[0]);
            return false;
        }
    }

    public boolean subscribe(ModelObserver observer) {
        return subscribe(ObserverType.OBSERVER_MODEL, observer);
    }

    public boolean unSubscribe(ModelObserver observer) {
        return unSubscribe(ObserverType.OBSERVER_MODEL, observer);
    }

    public boolean subscribe(AiModelObserver observer) {
        return subscribe(ObserverType.OBSERVER_RECORD, observer);
    }

    public boolean unSubscribe(AiModelObserver observer) {
        return unSubscribe(ObserverType.OBSERVER_RECORD, observer);
    }

    public DataServiceProxy getDataServiceProxy() {
        return (DataServiceProxy) this.secondary;
    }

    public void updatePackageCheck(List<ResourceInformation> resources, final UpdatePackageCheckCallBack cb) {
        if (cb == null) {
            DSLog.et(TAG, "Fail to update package check, error: UpdatePackageCheckCallBack is null.", new Object[0]);
        } else if (this.remote == null) {
            DSLog.et(TAG, "Fail to update package check, error: not connected to ai service.", new Object[0]);
        } else {
            UpdatePackageCheckCallBackAgent agentCallBack = new UpdatePackageCheckCallBackAgent() {
                public void onFinish(ObjectContainer oc, int type) {
                    NetworkType networkType = AiModelClientAgent.this.transferNetworkType(type);
                    if (oc == null || oc.get() == null) {
                        DSLog.et(AiModelClientAgent.TAG, "Fail to update package check, error: Response is empty.", new Object[0]);
                        cb.onFinish(new ArrayList(), networkType);
                        return;
                    }
                    cb.onFinish(oc.get(), networkType);
                }
            };
            try {
                ((IAIServiceCall) this.remote).updatePackageCheckAgent(new ObjectContainer(ResourceInformation.class, resources, this.pkgName), agentCallBack);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "Fail to update package check, error: %s.", e.getMessage());
            }
        }
    }

    public void updatePackage(List<ResourceInformation> resources, UpdatePackageCallBack cb, long refreshInterval, long refreshBucketSize, boolean wifiOnly) {
        if (cb == null) {
            DSLog.et(TAG, "Fail to update package, error: UpdatePackageCallBack is null.", new Object[0]);
            return;
        }
        final UpdatePackageCallBack updatePackageCallBack = cb;
        AnonymousClass2 r14 = new UpdatePackageCallBackAgent() {
            public int onRefresh(int status, long totalSize, long downloadedSize, int totalPackages, int downloadedPackages, int errorCode, String errorMessage) {
                UpdateStatus updateStatus = AiModelClientAgent.this.transferUpdateStatus(status);
                if (updatePackageCallBack instanceof UpdatePackageControllableCallBack) {
                    return ((UpdatePackageControllableCallBack) updatePackageCallBack).onControllableRefresh(updateStatus, totalSize, downloadedSize, totalPackages, downloadedPackages, errorCode, errorMessage);
                }
                updatePackageCallBack.onRefresh(updateStatus, totalSize, downloadedSize, totalPackages, downloadedPackages, errorCode, errorMessage);
                return 0;
            }
        };
        if (cb instanceof UpdatePackageControllableCallBack) {
            ((UpdatePackageControllableCallBack) cb).onControllableRefresh(UpdateStatus.BEGIN, 0, 0, 0, 0, 0, "");
        } else {
            cb.onRefresh(UpdateStatus.BEGIN, 0, 0, 0, 0, 0, "");
        }
        ObjectContainer oc = new ObjectContainer(ResourceInformation.class, resources, this.pkgName);
        if (this.remote == null) {
            DSLog.et(TAG, "Fail to update package, error: not connected to ai service.", new Object[0]);
            return;
        }
        try {
            ((IAIServiceCall) this.remote).updatePackageAgent(oc, r14, refreshInterval, refreshBucketSize, wifiOnly);
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Fail to update package, error: %s.", e.getMessage());
        }
    }

    public void deleteResInfo(List<ResourceInformation> resources, DeleteResInfoCallBack cb) {
        if (cb == null) {
            DSLog.et(TAG, "Fail to delete res info, error: DeleteResInfoCallBack is null.", new Object[0]);
            return;
        }
        ObjectContainer oc = new ObjectContainer(ResourceInformation.class, resources, this.pkgName);
        if (this.remote == null) {
            DSLog.et(TAG, "Fail to delete res info, error: not connected to ai service.", new Object[0]);
            return;
        }
        try {
            ((IAIServiceCall) this.remote).deleteResInfoAgent(oc, new DeleteUpdatedResInfoCallBack(cb));
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Fail to delete res info, error: %s.", e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public NetworkType transferNetworkType(int type) {
        if (type == NetworkType.WIFI.ordinal()) {
            return NetworkType.WIFI;
        }
        if (type == NetworkType.CELLUAR.ordinal()) {
            return NetworkType.CELLUAR;
        }
        return NetworkType.NONE;
    }

    /* access modifiers changed from: private */
    public UpdateStatus transferUpdateStatus(int status) {
        if (status == UpdateStatus.BEGIN.ordinal()) {
            return UpdateStatus.BEGIN;
        }
        if (status == UpdateStatus.SUCCESS.ordinal()) {
            return UpdateStatus.SUCCESS;
        }
        if (status == UpdateStatus.ONGOING.ordinal()) {
            return UpdateStatus.ONGOING;
        }
        return UpdateStatus.FAILURE;
    }

    private boolean isResInfoValid(ResourceInformation resInfo) {
        if (resInfo == null) {
            DSLog.et(TAG, "res info is invalid, error: resInfo is empty.", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(resInfo.getResid())) {
            DSLog.et(TAG, "res info is invalid, error: resid is empty.", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(resInfo.getXpu())) {
            DSLog.et(TAG, "res info is invalid, error: xpu is empty.", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(resInfo.getAbTest())) {
            DSLog.et(TAG, "res info is invalid, error: abTest is empty.", new Object[0]);
            return false;
        } else if (resInfo.getAppVersion() != null) {
            return true;
        } else {
            DSLog.et(TAG, "res info is invalid, error: appVersion is empty.", new Object[0]);
            return false;
        }
    }

    private boolean presetResInfo(ResourceInformation resInfo) {
        if (resInfo == null) {
            DSLog.et(TAG, "res info is invalid, error: resInfo is empty.", new Object[0]);
            return false;
        }
        resInfo.setIsPreset(0);
        resInfo.setIsExtended(1);
        resInfo.setFileSize(0L);
        resInfo.setVersionCode(0L);
        resInfo.setVersionName("");
        resInfo.setEmuiFamily("");
        resInfo.setProductFamily("");
        resInfo.setChipsetVendor("");
        resInfo.setChipset("");
        resInfo.setProduct("");
        resInfo.setProductModel("");
        resInfo.setDistrict("");
        resInfo.setSupportedAppVersion("");
        resInfo.setInterfaceVersion("");
        resInfo.setLatestTimestamp(0L);
        String packageName = this.context.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            resInfo.setPackageName(packageName);
            return true;
        }
        DSLog.et(TAG, "Fail to preset res info, error: package name is empty.", new Object[0]);
        return false;
    }

    private boolean subscribe(ObserverType type, ModelObserver observer) {
        if (observer == null) {
            DSLog.et(TAG, "Failed to subscribe model, error: null observer.", new Object[0]);
            return false;
        }
        ModelObserverInfo observerInfo = new ModelObserverInfo(type, AiModel.class, this.pkgName);
        observerInfo.setProxyId(Integer.valueOf(getId()));
        return ((AILocalObservable) this.localObservable).registerObserver(observerInfo, observer);
    }

    private boolean unSubscribe(ObserverType type, ModelObserver observer) {
        if (observer == null) {
            DSLog.et(TAG, "Failed to unsubscribe model, error: null observer.", new Object[0]);
            return false;
        }
        ModelObserverInfo observerInfo = new ModelObserverInfo(type, AiModel.class, this.pkgName);
        observerInfo.setProxyId(Integer.valueOf(getId()));
        return ((AILocalObservable) this.localObservable).unregisterObserver(observerInfo, observer);
    }
}
