package ohos.ai.nlu.sdk;

import java.util.Optional;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.ai.engine.pluginservice.IPluginService;
import ohos.ai.engine.pluginservice.PluginServiceSkeleton;
import ohos.ai.engine.utils.HiAILog;
import ohos.ai.engine.utils.ServiceConnector;
import ohos.ai.nlu.OnResultListener;
import ohos.ai.nlu.ResponseResult;
import ohos.ai.nlu.sdk.INlpService;
import ohos.ai.nlu.sdk.INluCallback;
import ohos.ai.nlu.sdk.INluService;
import ohos.ai.nlu.utils.NluResultParser;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class NluApiLocalService {
    private static final String LOCAL_DEVICE_ID = "";
    private static final String TAG = NluApiLocalService.class.getSimpleName();
    private static volatile NluApiLocalService nluApiLocalService;
    private boolean isLoadModel = false;
    private OnResultListener<Integer> listener = null;
    private INlpService nlpService;
    private INluService nluService;
    private ServiceConnection serviceConnection;

    private NluApiLocalService() {
    }

    public static NluApiLocalService getInstance() {
        if (nluApiLocalService == null) {
            synchronized (NluApiLocalService.class) {
                if (nluApiLocalService == null) {
                    nluApiLocalService = new NluApiLocalService();
                }
            }
        }
        return nluApiLocalService;
    }

    private boolean isBind() {
        if (this.nlpService != null && this.nluService != null) {
            return true;
        }
        HiAILog.info(TAG, "NluApiLocalService is not binded.");
        return false;
    }

    public synchronized void bindService(Context context, OnResultListener<Integer> onResultListener, boolean z) {
        HiAILog.info(TAG, "begin to bind service");
        this.listener = onResultListener;
        if (context == null) {
            HiAILog.error(TAG, "context is null");
            if (this.listener != null) {
                this.listener.onResult(1);
            }
            return;
        }
        if (!isBind()) {
            HiAILog.info(TAG, "start binding nlu client");
            this.isLoadModel = z;
            this.serviceConnection = new ServiceConnection();
            if (!ServiceConnector.connectToService(context, "", this.serviceConnection)) {
                HiAILog.error(TAG, "will send error message to client");
                if (this.listener != null) {
                    this.listener.onResult(1);
                }
            }
        }
    }

    public synchronized void unBindService(Context context) {
        HiAILog.info(TAG, "begin to unBindService");
        if (!(context == null || this.serviceConnection == null)) {
            HiAILog.debug(TAG, "unBindService");
            ServiceConnector.unBindService(context, this.serviceConnection);
        }
        this.nluService = null;
        this.nlpService = null;
    }

    public synchronized Optional<String> analyzeAssistant(String str) {
        HiAILog.info(TAG, "begin to analyzeAssistant");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nlpService.analyzeAssistant(str));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "analyzeAssistant error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> analyzeShortText(String str) {
        HiAILog.info(TAG, "begin to analyzeShortText");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nlpService.analyzeShortText(str));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "analyzeShortText error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> analyzeLongText(String str) {
        HiAILog.info(TAG, "begin to analyzeLongText");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nlpService.analyzeLongText(str));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "analyzeLongText error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> getWordSegment(String str, OnResultListener<ResponseResult> onResultListener) {
        HiAILog.info(TAG, "begin to getWordSegment");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nluService.getWordSegment(str, createNluCallBack(onResultListener)));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getWordSegment error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> getEntity(String str, OnResultListener<ResponseResult> onResultListener) {
        HiAILog.info(TAG, "begin to getEntity");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nluService.getEntity(str, createNluCallBack(onResultListener)));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getEntity error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> getWordPos(String str, OnResultListener<ResponseResult> onResultListener) {
        HiAILog.info(TAG, "begin to getWordPos");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nluService.getWordPos(str, createNluCallBack(onResultListener)));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getWordPos error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> getChatIntention(String str, OnResultListener<ResponseResult> onResultListener) {
        HiAILog.info(TAG, "begin to getChatIntention");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nluService.getChatIntention(str, createNluCallBack(onResultListener)));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getChatIntention error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> getKeywords(String str, OnResultListener<ResponseResult> onResultListener) {
        HiAILog.info(TAG, "begin to getKeywords");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nluService.getKeywords(str, createNluCallBack(onResultListener)));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getKeywords error");
            return Optional.empty();
        }
    }

    public synchronized Optional<String> getAssistantIntention(String str, OnResultListener<ResponseResult> onResultListener) {
        HiAILog.info(TAG, "begin to getAssistantIntention");
        try {
            if (!isBind()) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.nluService.getAssistantIntention(str, createNluCallBack(onResultListener)));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getAssistantIntention error");
            return Optional.empty();
        }
    }

    private INluCallback createNluCallBack(final OnResultListener<ResponseResult> onResultListener) {
        if (onResultListener != null) {
            return new INluCallback.Stub() {
                /* class ohos.ai.nlu.sdk.NluApiLocalService.AnonymousClass1 */

                @Override // ohos.ai.nlu.sdk.INluCallback
                public void onNluResult(String str) throws RemoteException {
                    onResultListener.onResult(NluResultParser.parserResult(str));
                }
            };
        }
        return null;
    }

    public synchronized int systemInit() {
        HiAILog.info(TAG, "begin to systemInit");
        try {
            if (!isBind()) {
                HiAILog.error(TAG, "systemInit bindService error");
                return 1;
            }
            return this.nluService.systemInit();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "systemInit error");
            return 1;
        }
    }

    public class ServiceConnection implements IAbilityConnection {
        public ServiceConnection() {
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            NluApiLocalService.this.onAbilityConnect(elementName, iRemoteObject, i);
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            String str;
            String str2 = NluApiLocalService.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Service disconnect done, elementName: ");
            if (elementName == null) {
                str = null;
            } else {
                str = elementName.getURI();
            }
            sb.append(str);
            HiAILog.info(str2, sb.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void onAbilityConnect(ElementName elementName, IRemoteObject iRemoteObject, int i) {
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Service connect done, elementName:");
        sb.append(elementName == null ? null : elementName.getURI());
        HiAILog.info(str, sb.toString());
        synchronized (NluApiLocalService.class) {
            IPluginService orElse = PluginServiceSkeleton.asInterface(iRemoteObject).orElse(null);
            if (orElse == null) {
                if (this.listener != null) {
                    this.listener.onResult(1);
                }
                return;
            }
            try {
                IRemoteObject splitRemoteObject = orElse.getSplitRemoteObject(262144);
                IRemoteObject splitRemoteObject2 = orElse.getSplitRemoteObject(131072);
                this.nlpService = INlpService.Stub.asInterface(splitRemoteObject).orElse(null);
                this.nluService = INluService.Stub.asInterface(splitRemoteObject2).orElse(null);
                int i2 = 0;
                if (this.isLoadModel && systemInit() != 0) {
                    i2 = 1;
                }
                if (this.listener != null) {
                    this.listener.onResult(Integer.valueOf(i2));
                }
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "onServiceConnected RemoteException!");
                if (this.listener != null) {
                    this.listener.onResult(1);
                }
            }
        }
    }
}
