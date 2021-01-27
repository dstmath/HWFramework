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
import java.util.function.Consumer;

public class AiModelClientAgent extends DualProxy<DataServiceProxy, IAIServiceCall> {
    private static final String AI_SERVICE_ACTION = "com.huawei.nb.service.AIService.START";
    private static final String AI_SERVICE_NAME = "NaturalBase AI Service";
    private static final int DELETE_WAIT_TIMEOUT = 10;
    private static final String FAKE_PRIVATE_KEY_NAME = "fakePrivateKey";
    private static final String FAKE_PUBLIC_KEY_NAME = "fakePublicKey";
    private static final String TAG = "AiModelClientAgent";
    private volatile Map<String, Object> keyMap;
    private final Object lock = new Object();

    public AiModelClientAgent(Context context) {
        super(context, AI_SERVICE_NAME, AI_SERVICE_ACTION, new DataServiceProxy(context));
    }

    public void disconnect() {
        disconnectInner();
    }

    public void initKey() {
        generateKeysIfNeeded();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.nb.client.Proxy
    public IAIServiceCall asInterface(IBinder iBinder) {
        return IAIServiceCall.Stub.asInterface(iBinder);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.nb.client.Proxy
    public AILocalObservable newLocalObservable() {
        return new AILocalObservable();
    }

    private void generateKeysIfNeeded() {
        synchronized (this.lock) {
            if (this.keyMap == null) {
                this.keyMap = RSAEncryptUtils.generateKeyPair();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setPrivateKey(AiModelResponse aiModelResponse) {
        if (aiModelResponse != null) {
            aiModelResponse.setPrivateKey(RSAEncryptUtils.getPrivateKey(this.keyMap));
        }
    }

    /* access modifiers changed from: private */
    public void setPublicKey(AiModelRequest aiModelRequest) {
        if (aiModelRequest != null) {
            aiModelRequest.setPublicKey(RSAEncryptUtils.getPublicKey(this.keyMap));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a  */
    private boolean batchValidRequest(List<AiModelRequest> list) {
        for (AiModelRequest aiModelRequest : list) {
            if (aiModelRequest == null || !aiModelRequest.isValid()) {
                return false;
            }
            while (r2.hasNext()) {
            }
        }
        return true;
    }

    private boolean batchSetPrivateKey(List<AiModelResponse> list) {
        if (this.keyMap == null) {
            return false;
        }
        list.forEach(new Consumer() {
            /* class com.huawei.nb.client.ai.$$Lambda$AiModelClientAgent$jO3_2mR8__QDMQ5hIO9SK5CMc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModelClientAgent.m0lambda$jO3_2mR8__QDMQ5hIO9SK5CMc(AiModelClientAgent.this, (AiModelResponse) obj);
            }
        });
        return true;
    }

    private boolean batchSetFakePrivateKey(List<AiModelResponse> list) {
        for (AiModelResponse aiModelResponse : list) {
            aiModelResponse.setPrivateKey(FAKE_PRIVATE_KEY_NAME);
        }
        return true;
    }

    private boolean batchSetPublicKey(List<AiModelRequest> list) {
        generateKeysIfNeeded();
        if (this.keyMap == null) {
            return false;
        }
        list.forEach(new Consumer() {
            /* class com.huawei.nb.client.ai.$$Lambda$AiModelClientAgent$0IilKLtH8RC8zlrpOcHgTBDHRJs */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModelClientAgent.lambda$0IilKLtH8RC8zlrpOcHgTBDHRJs(AiModelClientAgent.this, (AiModelRequest) obj);
            }
        });
        return true;
    }

    private boolean batchSetFakePublicKey(List<AiModelRequest> list) {
        for (AiModelRequest aiModelRequest : list) {
            aiModelRequest.setPublicKey(FAKE_PUBLIC_KEY_NAME);
        }
        return true;
    }

    public AiModelResponse requestAiModel(AiModelRequest aiModelRequest) {
        List<AiModelResponse> requestAiModel = requestAiModel(Arrays.asList(aiModelRequest));
        if (requestAiModel == null || requestAiModel.isEmpty()) {
            return null;
        }
        return requestAiModel.get(0);
    }

    public AiModelResponse requestAiModelWithoutEncrypt(AiModelRequest aiModelRequest) {
        List<AiModelResponse> requestAiModelWithoutEncrypt = requestAiModelWithoutEncrypt(Arrays.asList(aiModelRequest));
        if (requestAiModelWithoutEncrypt == null || requestAiModelWithoutEncrypt.isEmpty()) {
            return null;
        }
        return requestAiModelWithoutEncrypt.get(0);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x006b: APUT  
      (r4v4 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x0067: INVOKE  (r3v6 java.lang.Integer) = (r3v5 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    public List<AiModelResponse> requestAiModel(List<AiModelRequest> list) {
        List<AiModelResponse> list2;
        int i;
        if (list == null || !batchValidRequest(list)) {
            DSLog.et(TAG, "Failed to get ai models, error: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "Failed to request Ai model, error: not connected to ai service.", new Object[0]);
            return null;
        } else if (!batchSetPublicKey(list)) {
            DSLog.et(TAG, "Failed to request Ai model, error: public key unavailable.", new Object[0]);
            return null;
        } else {
            ObjectContainer objectContainer = new ObjectContainer(AiModelRequest.class, list);
            if (this.callbackTimeout > 0) {
                list2 = requestAiModelAsync(objectContainer);
            } else {
                list2 = requestAiModel(objectContainer);
            }
            if (list2 == null || list2.size() != list.size()) {
                Object[] objArr = new Object[2];
                if (list2 == null) {
                    i = 0;
                } else {
                    i = list2.size();
                }
                objArr[0] = Integer.valueOf(i);
                objArr[1] = Integer.valueOf(list.size());
                DSLog.et(TAG, "Failed to get ai models, error: just get %s for %s requests.", objArr);
                return null;
            } else if (batchSetPrivateKey(list2)) {
                return list2;
            } else {
                DSLog.et(TAG, "Failed to request Ai model, error: private key unavailable.", new Object[0]);
                return null;
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x006b: APUT  
      (r4v4 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x0067: INVOKE  (r3v6 java.lang.Integer) = (r3v5 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    public List<AiModelResponse> requestAiModelWithoutEncrypt(List<AiModelRequest> list) {
        List<AiModelResponse> list2;
        int i;
        if (list == null || !batchValidRequest(list)) {
            DSLog.et(TAG, "Failed to get ai models, error: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "Failed to request Ai model, error: not connected to ai service.", new Object[0]);
            return null;
        } else if (!batchSetFakePublicKey(list)) {
            DSLog.et(TAG, "Failed to request Ai model, error: set fake public key failed.", new Object[0]);
            return null;
        } else {
            ObjectContainer objectContainer = new ObjectContainer(AiModelRequest.class, list);
            if (this.callbackTimeout > 0) {
                list2 = requestAiModelAsync(objectContainer);
            } else {
                list2 = requestAiModel(objectContainer);
            }
            if (list2 == null || list2.size() != list.size()) {
                Object[] objArr = new Object[2];
                if (list2 == null) {
                    i = 0;
                } else {
                    i = list2.size();
                }
                objArr[0] = Integer.valueOf(i);
                objArr[1] = Integer.valueOf(list.size());
                DSLog.et(TAG, "Failed to get ai models, error: just get %s for %s requests.", objArr);
                return null;
            } else if (batchSetFakePrivateKey(list2)) {
                return list2;
            } else {
                DSLog.et(TAG, "Failed to request Ai model, error: set fake private key failed.", new Object[0]);
                return null;
            }
        }
    }

    private List<AiModelResponse> requestAiModel(ObjectContainer objectContainer) {
        try {
            ObjectContainer requestAiModel = ((IAIServiceCall) this.remote).requestAiModel(objectContainer);
            if (requestAiModel == null) {
                return null;
            }
            return requestAiModel.get();
        } catch (RemoteException | RuntimeException unused) {
            return null;
        }
    }

    private List<AiModelResponse> requestAiModelAsync(ObjectContainer objectContainer) {
        AIFetchCallback aIFetchCallback = (AIFetchCallback) this.callbackManager.createCallBack($$Lambda$TzmhMF9wFAC_r8Vnaqj8F9zzWzQ.INSTANCE);
        try {
            return aIFetchCallback.await(((IAIServiceCall) this.remote).requestAiModelAsync(objectContainer, aIFetchCallback), this.callbackTimeout);
        } catch (RemoteException | RuntimeException unused) {
            return null;
        }
    }

    public ResourceInformation queryResInfo(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.et(TAG, "Failed to query model res info, error: invalid parameter.", new Object[0]);
            return null;
        }
        return (ResourceInformation) ((DataServiceProxy) this.secondary).executeSingleQuery(Query.select(ResourceInformation.class).equalTo("resid", str));
    }

    public boolean deleteResInfo(ResourceInformation resourceInformation) {
        if (!isAllowChangeInfo(resourceInformation)) {
            DSLog.et(TAG, "Fail to delete res info, error: not allow to delete.", new Object[0]);
            return false;
        }
        boolean[] zArr = {false};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        deleteResInfo(Arrays.asList(resourceInformation), new DeleteResInfoFromDbCallBack(zArr, countDownLatch));
        try {
            if (!countDownLatch.await(10, TimeUnit.SECONDS)) {
                zArr[0] = false;
                DSLog.dt(TAG, "Fail to delete res info,error: wait for callback timeout.", new Object[0]);
            }
        } catch (InterruptedException unused) {
            zArr[0] = false;
        }
        DSLog.dt(TAG, "res info is deleted, %s.", Boolean.valueOf(zArr[0]));
        return zArr[0];
    }

    public void deleteResInfo(List<ResourceInformation> list, DeleteResInfoCallBack deleteResInfoCallBack) {
        if (deleteResInfoCallBack == null) {
            DSLog.et(TAG, "Fail to delete res info, error: DeleteResInfoCallBack is null.", new Object[0]);
            return;
        }
        ObjectContainer objectContainer = new ObjectContainer(ResourceInformation.class, list, this.pkgName);
        if (this.remote == null) {
            DSLog.et(TAG, "Fail to delete res info, error: not connected to ai service.", new Object[0]);
            return;
        }
        try {
            ((IAIServiceCall) this.remote).deleteResInfoAgent(objectContainer, new DeleteUpdatedResInfoCallBack(deleteResInfoCallBack));
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Fail to delete res info, error: %s.", e.getMessage());
        }
    }

    private static class DeleteResInfoFromDbCallBack implements DeleteResInfoCallBack {
        private boolean[] isDeletes = null;
        private CountDownLatch latch = null;

        DeleteResInfoFromDbCallBack(boolean[] zArr, CountDownLatch countDownLatch) {
            this.isDeletes = zArr;
            this.latch = countDownLatch;
        }

        @Override // com.huawei.nb.client.callback.DeleteResInfoCallBack
        public void onSuccess() {
            this.isDeletes[0] = true;
            this.latch.countDown();
        }

        @Override // com.huawei.nb.client.callback.DeleteResInfoCallBack
        public void onFailure(int i, String str) {
            if (str == null) {
                str = "";
            }
            DSLog.dt(AiModelClientAgent.TAG, "Fail to delete res info,error: errorCode = " + i + "errorMessage = " + str, new Object[0]);
            this.isDeletes[0] = false;
            this.latch.countDown();
        }
    }

    public boolean updateResInfo(ResourceInformation resourceInformation) {
        if (!isAllowChangeInfo(resourceInformation)) {
            DSLog.et(TAG, "Fail to update res info, error: not allow to update.", new Object[0]);
            return false;
        }
        ResourceInformation resourceInformation2 = (ResourceInformation) ((DataServiceProxy) this.secondary).executeSingleQuery(Query.select(ResourceInformation.class).equalTo("resid", resourceInformation.getResid()));
        if (resourceInformation2 == null) {
            DSLog.et(TAG, "Fail to update res info, error: query resid failed.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "Failed to update res info, error: not connected to ai service.", new Object[0]);
            return false;
        } else {
            try {
                resourceInformation2.setAppVersion(resourceInformation.getAppVersion());
                boolean updateResInfoAgent = ((IAIServiceCall) this.remote).updateResInfoAgent(new ObjectContainer(ResourceInformation.class, Collections.singletonList(resourceInformation2), this.pkgName));
                DSLog.dt(TAG, "res info is updated, %s.", Boolean.valueOf(updateResInfoAgent));
                return updateResInfoAgent;
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
                ObjectContainer insertResInfoAgent = ((IAIServiceCall) this.remote).insertResInfoAgent(new ObjectContainer(ResourceInformation.class, Collections.singletonList(resourceInformation), this.pkgName));
                if (insertResInfoAgent != null) {
                    if (!insertResInfoAgent.get().isEmpty()) {
                        if (insertResInfoAgent.get().get(0) instanceof ResourceInformation) {
                            return (ResourceInformation) insertResInfoAgent.get().get(0);
                        }
                        DSLog.et(TAG, "Fail to insert res info, error: result not res info type.", new Object[0]);
                        return null;
                    }
                }
                DSLog.et(TAG, "Fail to insert res info, error: insert result is empty.", new Object[0]);
                return null;
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
        DSLog.et(TAG, "Failed to change model res info, error: invalid parameter. Please check the pkg is in res info.", new Object[0]);
        return false;
    }

    public CoordinatorSwitch querySwitch(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.et(TAG, "Failed to query switch, error: invalid parameter.", new Object[0]);
            return null;
        }
        CoordinatorSwitch coordinatorSwitch = (CoordinatorSwitch) ((DataServiceProxy) this.secondary).executeSingleQuery(Query.select(CoordinatorSwitch.class).equalTo("serviceName", str));
        if (coordinatorSwitch != null) {
            DSLog.dt(TAG, "switch is found, value is %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
        } else {
            DSLog.et(TAG, "switch is not found.", new Object[0]);
        }
        return coordinatorSwitch;
    }

    public boolean deleteSwitch(CoordinatorSwitch coordinatorSwitch) {
        if (!isAllowChangeSwitch(coordinatorSwitch)) {
            return false;
        }
        boolean executeDelete = ((DataServiceProxy) this.secondary).executeDelete((DataServiceProxy) coordinatorSwitch);
        DSLog.dt(TAG, "switch is deleted %s.", Boolean.valueOf(executeDelete));
        return executeDelete;
    }

    public boolean updateSwitch(CoordinatorSwitch coordinatorSwitch) {
        if (!isAllowChangeSwitch(coordinatorSwitch)) {
            return false;
        }
        if (((DataServiceProxy) this.secondary).executeUpdate((DataServiceProxy) coordinatorSwitch)) {
            DSLog.dt(TAG, "switch is update as %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
            return true;
        }
        DSLog.dt(TAG, "Failed to update switch as %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
        return false;
    }

    public CoordinatorSwitch insertSwitch(CoordinatorSwitch coordinatorSwitch) {
        CoordinatorSwitch coordinatorSwitch2;
        if (!isAllowChangeSwitch(coordinatorSwitch) || (coordinatorSwitch2 = (CoordinatorSwitch) ((DataServiceProxy) this.secondary).executeInsert((DataServiceProxy) coordinatorSwitch)) == null) {
            return null;
        }
        DSLog.dt(TAG, "switch inserted, value is %s.", Boolean.valueOf(coordinatorSwitch.getIsSwitchOn()));
        return coordinatorSwitch2;
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

    public boolean subscribe(ModelObserver modelObserver) {
        return subscribe(ObserverType.OBSERVER_MODEL, modelObserver);
    }

    public boolean subscribe(AiModelObserver aiModelObserver) {
        return subscribe(ObserverType.OBSERVER_RECORD, aiModelObserver);
    }

    private boolean subscribe(ObserverType observerType, ModelObserver modelObserver) {
        if (modelObserver == null) {
            DSLog.et(TAG, "Failed to subscribe model, error: null observer.", new Object[0]);
            return false;
        }
        ModelObserverInfo modelObserverInfo = new ModelObserverInfo(observerType, AiModel.class, this.pkgName);
        modelObserverInfo.setProxyId(Integer.valueOf(getId()));
        return ((AILocalObservable) this.localObservable).registerObserver(modelObserverInfo, modelObserver);
    }

    public boolean unSubscribe(ModelObserver modelObserver) {
        return unSubscribe(ObserverType.OBSERVER_MODEL, modelObserver);
    }

    public boolean unSubscribe(AiModelObserver aiModelObserver) {
        return unSubscribe(ObserverType.OBSERVER_RECORD, aiModelObserver);
    }

    private boolean unSubscribe(ObserverType observerType, ModelObserver modelObserver) {
        if (modelObserver == null) {
            DSLog.et(TAG, "Failed to unsubscribe model, error: null observer.", new Object[0]);
            return false;
        }
        ModelObserverInfo modelObserverInfo = new ModelObserverInfo(observerType, AiModel.class, this.pkgName);
        modelObserverInfo.setProxyId(Integer.valueOf(getId()));
        return ((AILocalObservable) this.localObservable).unregisterObserver(modelObserverInfo, modelObserver);
    }

    public DataServiceProxy getDataServiceProxy() {
        return (DataServiceProxy) this.secondary;
    }

    public void updatePackageCheck(List<ResourceInformation> list, final UpdatePackageCheckCallBack updatePackageCheckCallBack) {
        if (updatePackageCheckCallBack == null) {
            DSLog.et(TAG, "Fail to update package check, error: UpdatePackageCheckCallBack is null.", new Object[0]);
        } else if (this.remote == null) {
            DSLog.et(TAG, "Fail to update package check, error: not connected to ai service.", new Object[0]);
        } else {
            try {
                ((IAIServiceCall) this.remote).updatePackageCheckAgent(new ObjectContainer(ResourceInformation.class, list, this.pkgName), new UpdatePackageCheckCallBackAgent() {
                    /* class com.huawei.nb.client.ai.AiModelClientAgent.AnonymousClass1 */

                    @Override // com.huawei.nb.client.callback.UpdatePackageCheckCallBackAgent, com.huawei.nb.callback.IUpdatePackageCheckCallBack
                    public void onFinish(ObjectContainer objectContainer, int i) {
                        NetworkType transferNetworkType = AiModelClientAgent.this.transferNetworkType(i);
                        if (objectContainer == null || objectContainer.get() == null) {
                            DSLog.et(AiModelClientAgent.TAG, "Fail to update package check, error: Response is empty.", new Object[0]);
                            updatePackageCheckCallBack.onFinish(new ArrayList(0), transferNetworkType);
                            return;
                        }
                        updatePackageCheckCallBack.onFinish(objectContainer.get(), transferNetworkType);
                    }
                });
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "Fail to update package check, error: %s.", e.getMessage());
            }
        }
    }

    public void updatePackage(List<ResourceInformation> list, final UpdatePackageCallBack updatePackageCallBack, long j, long j2, boolean z) {
        if (updatePackageCallBack == null) {
            DSLog.et(TAG, "Fail to update package, error: UpdatePackageCallBack is null.", new Object[0]);
            return;
        }
        AnonymousClass2 r14 = new UpdatePackageCallBackAgent() {
            /* class com.huawei.nb.client.ai.AiModelClientAgent.AnonymousClass2 */

            @Override // com.huawei.nb.client.callback.UpdatePackageCallBackAgent, com.huawei.nb.callback.IUpdatePackageCallBack
            public int onRefresh(int i, long j, long j2, int i2, int i3, int i4, String str) {
                UpdateStatus transferUpdateStatus = AiModelClientAgent.this.transferUpdateStatus(i);
                UpdatePackageCallBack updatePackageCallBack = updatePackageCallBack;
                if (updatePackageCallBack instanceof UpdatePackageControllableCallBack) {
                    return ((UpdatePackageControllableCallBack) updatePackageCallBack).onControllableRefresh(transferUpdateStatus, j, j2, i2, i3, i4, str);
                }
                updatePackageCallBack.onRefresh(transferUpdateStatus, j, j2, i2, i3, i4, str);
                return 0;
            }
        };
        if (updatePackageCallBack instanceof UpdatePackageControllableCallBack) {
            ((UpdatePackageControllableCallBack) updatePackageCallBack).onControllableRefresh(UpdateStatus.BEGIN, 0, 0, 0, 0, 0, "");
        } else {
            updatePackageCallBack.onRefresh(UpdateStatus.BEGIN, 0, 0, 0, 0, 0, "");
        }
        ObjectContainer objectContainer = new ObjectContainer(ResourceInformation.class, list, this.pkgName);
        if (this.remote == null) {
            DSLog.et(TAG, "Fail to update package, error: not connected to ai service.", new Object[0]);
            return;
        }
        try {
            ((IAIServiceCall) this.remote).updatePackageAgent(objectContainer, r14, j, j2, z);
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Fail to update package, error: %s.", e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public static class DeleteUpdatedResInfoCallBack extends DeleteResInfoCallBackAgent {
        private DeleteResInfoCallBack mCb = null;

        DeleteUpdatedResInfoCallBack(DeleteResInfoCallBack deleteResInfoCallBack) {
            this.mCb = deleteResInfoCallBack;
        }

        @Override // com.huawei.nb.client.callback.DeleteResInfoCallBackAgent, com.huawei.nb.callback.IDeleteResInfoCallBack
        public void onSuccess() {
            this.mCb.onSuccess();
        }

        @Override // com.huawei.nb.client.callback.DeleteResInfoCallBackAgent, com.huawei.nb.callback.IDeleteResInfoCallBack
        public void onFailure(int i, String str) {
            if (str == null) {
                str = "";
            }
            this.mCb.onFailure(i, str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkType transferNetworkType(int i) {
        if (i == NetworkType.WIFI.ordinal()) {
            return NetworkType.WIFI;
        }
        if (i == NetworkType.CELLUAR.ordinal()) {
            return NetworkType.CELLUAR;
        }
        return NetworkType.NONE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UpdateStatus transferUpdateStatus(int i) {
        if (i == UpdateStatus.BEGIN.ordinal()) {
            return UpdateStatus.BEGIN;
        }
        if (i == UpdateStatus.SUCCESS.ordinal()) {
            return UpdateStatus.SUCCESS;
        }
        if (i == UpdateStatus.ONGOING.ordinal()) {
            return UpdateStatus.ONGOING;
        }
        return UpdateStatus.FAILURE;
    }

    private boolean isResInfoValid(ResourceInformation resourceInformation) {
        if (resourceInformation == null) {
            DSLog.et(TAG, "res info is invalid, error: resInfo is empty.", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(resourceInformation.getResid())) {
            DSLog.et(TAG, "res info is invalid, error: resid is empty.", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(resourceInformation.getXpu())) {
            DSLog.et(TAG, "res info is invalid, error: xpu is empty.", new Object[0]);
            return false;
        } else if (TextUtils.isEmpty(resourceInformation.getAbTest())) {
            DSLog.et(TAG, "res info is invalid, error: abTest is empty.", new Object[0]);
            return false;
        } else if (resourceInformation.getAppVersion() != null) {
            return true;
        } else {
            DSLog.et(TAG, "res info is invalid, error: appVersion is empty.", new Object[0]);
            return false;
        }
    }

    private boolean presetResInfo(ResourceInformation resourceInformation) {
        if (resourceInformation == null) {
            DSLog.et(TAG, "res info is invalid, error: resInfo is empty.", new Object[0]);
            return false;
        }
        resourceInformation.setIsPreset(0);
        resourceInformation.setIsExtended(1);
        resourceInformation.setFileSize(0L);
        resourceInformation.setVersionCode(0L);
        resourceInformation.setVersionName("");
        resourceInformation.setEmuiFamily("");
        resourceInformation.setProductFamily("");
        resourceInformation.setChipsetVendor("");
        resourceInformation.setChipset("");
        resourceInformation.setProduct("");
        resourceInformation.setProductModel("");
        resourceInformation.setDistrict("");
        resourceInformation.setSupportedAppVersion("");
        resourceInformation.setInterfaceVersion("");
        resourceInformation.setLatestTimestamp(0L);
        String packageName = this.context.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            resourceInformation.setPackageName(packageName);
            return true;
        }
        DSLog.et(TAG, "Fail to preset res info, error: package name is empty.", new Object[0]);
        return false;
    }
}
