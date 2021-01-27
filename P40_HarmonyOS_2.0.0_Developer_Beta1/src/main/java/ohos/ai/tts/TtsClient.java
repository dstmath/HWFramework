package ohos.ai.tts;

import com.huawei.ohos.interwork.AndroidUtils;
import java.util.Optional;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.ai.engine.pluginservice.IPluginService;
import ohos.ai.engine.pluginservice.PluginServiceSkeleton;
import ohos.ai.engine.utils.HiAILog;
import ohos.ai.engine.utils.ServiceConnector;
import ohos.ai.tts.TtsPluginService;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.media.audio.AudioManager;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class TtsClient {
    private static final int DEVICE_ID_MAX_LENGTH = 64;
    private static final int FAIL_GET_MAX_SUPPORT_LENGTH = -1;
    private static final String LOCAL_DEVICE_ID = "";
    public static final int METHOD_CREATE = 1000;
    public static final int METHOD_DESTROY = 1003;
    public static final int METHOD_RELEASE = 1005;
    public static final int METHOD_SET_IS_SAVE_TTS_DATA = 1007;
    public static final int METHOD_SPEAK_LONG_TEXT = 1006;
    public static final int METHOD_SPEAK_TEXT = 1001;
    public static final int METHOD_STOP_SPEAK = 1004;
    public static final String PAC_MAP_KEY_METHOD_ID = "methodId";
    private static final String TAG = TtsClient.class.getSimpleName();
    private static volatile TtsClient ttsClient;
    private TtsCallback ttsCallback = new TtsCallback();
    private TtsServiceConnection ttsConnection;
    private Context ttsContext;
    private TtsPluginService ttsService;

    private TtsClient() {
    }

    public static TtsClient getInstance() {
        if (ttsClient == null) {
            synchronized (TtsClient.class) {
                if (ttsClient == null) {
                    ttsClient = new TtsClient();
                }
            }
        }
        return ttsClient;
    }

    public synchronized void create(Context context, TtsListener ttsListener) {
        HiAILog.info(TAG, "create");
        if (!isCreated()) {
            HiAILog.info(TAG, "Start creating tts client");
            if (ttsListener != null) {
                this.ttsCallback.setTtsListener(ttsListener);
            }
            if (context == null) {
                HiAILog.error(TAG, "Context is null and create tts client failed");
                sendCallback(1, 1000);
                this.ttsCallback.releaseTtsListener();
                return;
            }
            this.ttsContext = context;
            if (this.ttsConnection == null) {
                HiAILog.info(TAG, "ttsConnection is null");
                this.ttsConnection = new TtsServiceConnection();
            }
            if (!ServiceConnector.connectToService(context, "", this.ttsConnection)) {
                HiAILog.error(TAG, "Create tts client failed");
                sendCallback(1, 1000);
                this.ttsCallback.releaseTtsListener();
            }
        } else {
            HiAILog.warn(TAG, "Tts client has been created and cannot be created repeatedly");
        }
    }

    public synchronized void release() {
        HiAILog.info(TAG, "release");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, release failed");
            return;
        }
        try {
            this.ttsService.release(this.ttsCallback);
            sendCallback(100, 1005);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "release RemoteException");
            sendCallback(101, 1005);
        }
    }

    public synchronized boolean init(TtsParams ttsParams) {
        HiAILog.info(TAG, "init");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created,initialization failed");
            return false;
        }
        try {
            Optional<InitParams> convertToTtsParams = convertToTtsParams(ttsParams);
            if (!convertToTtsParams.isPresent()) {
                HiAILog.error(TAG, "ttsParams is null");
                return false;
            }
            return this.ttsService.init(convertToTtsParams.get(), this.ttsCallback);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "init RemoteException");
            return false;
        }
    }

    public synchronized void setIsSaveTtsData(boolean z) {
        HiAILog.info(TAG, "setIsSaveTtsData");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, setIsSaveTtsData failed");
            return;
        }
        try {
            this.ttsService.setIsSaveTtsData(z, this.ttsCallback);
            sendCallback(100, 1007);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "setIsSaveTtsData RemoteException");
            sendCallback(101, 1007);
        }
    }

    public synchronized boolean setParams(TtsParams ttsParams) {
        HiAILog.info(TAG, "setParams");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, setParams failed");
            return false;
        }
        try {
            Optional<InitParams> convertToTtsParams = convertToTtsParams(ttsParams);
            if (!convertToTtsParams.isPresent()) {
                return false;
            }
            return this.ttsService.setParams(convertToTtsParams.get(), this.ttsCallback);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "setParams RemoteException");
            return false;
        }
    }

    public synchronized boolean setAudioType(AudioManager.AudioVolumeType audioVolumeType) {
        HiAILog.info(TAG, "setAudioType");
        boolean z = false;
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, setAudioType failed");
            return false;
        }
        if (audioVolumeType != null) {
            try {
                z = this.ttsService.setAudioType(audioVolumeType.getValue(), this.ttsCallback);
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "setAudioType RemoteException");
                return false;
            }
        }
        return z;
    }

    public synchronized void speakText(String str, String str2) {
        String str3;
        HiAILog.info(TAG, "speakText");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, speakText failed");
            return;
        }
        if (str2 != null) {
            try {
                str3 = str2.trim();
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "speakText RemoteException");
                sendCallback(101, 1001);
            }
        } else {
            str3 = null;
        }
        this.ttsService.speakText(str, str3, this.ttsCallback);
        sendCallback(100, 1001);
    }

    public synchronized long getSupportMaxLength() {
        HiAILog.info(TAG, "getSupportMaxLength");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, getSupportMaxLength failed");
            return -1;
        }
        try {
            return this.ttsService.getSupportMaxLength(this.ttsCallback);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getSupportMaxLength RemoteException");
            return -1;
        }
    }

    public synchronized void speakLongText(String str, String str2) {
        String str3;
        HiAILog.info(TAG, "speakLongText");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, speakLongText failed");
            return;
        }
        if (str2 != null) {
            try {
                str3 = str2.trim();
            } catch (RemoteException unused) {
                sendCallback(101, 1006);
            }
        } else {
            str3 = null;
        }
        this.ttsService.speakLongText(str, str3, this.ttsCallback);
        sendCallback(100, 1006);
    }

    public synchronized boolean isSpeaking() {
        HiAILog.info(TAG, "isSpeaking");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, isSpeaking failed");
            return false;
        }
        try {
            return this.ttsService.isSpeaking(this.ttsCallback);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "isSpeaking RemoteException");
            return false;
        }
    }

    public synchronized void stopSpeak() {
        HiAILog.info(TAG, "stopSpeak");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, stopSpeak failed");
            return;
        }
        try {
            this.ttsService.stopSpeak(this.ttsCallback);
            sendCallback(100, 1004);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "stopSpeak RemoteException");
            sendCallback(101, 1004);
        }
    }

    public synchronized Optional<String> getVersion() {
        HiAILog.info(TAG, "getVersion");
        if (!isCreated()) {
            HiAILog.error(TAG, "Tts client was not created, getVersion failed");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(this.ttsService.getVersion(this.ttsCallback));
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getVersion RemoteException");
            return Optional.empty();
        }
    }

    public synchronized void destroy() {
        HiAILog.info(TAG, "destroy");
        if (this.ttsService != null) {
            if (this.ttsContext != null) {
                HiAILog.info(TAG, "context is not null");
                AndroidUtils.unbindService(this.ttsContext, this.ttsConnection);
            }
            this.ttsService = null;
            sendCallback(2, 1003);
            this.ttsCallback.releaseTtsListener();
        } else {
            HiAILog.warn(TAG, "Tts client wasn't created successfully and cannot be destroyed.");
            sendCallback(3, 1003);
        }
    }

    private class TtsServiceConnection implements IAbilityConnection {
        private TtsServiceConnection() {
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            HiAILog.info(TtsClient.TAG, "TtsService connect done");
            synchronized (TtsClient.class) {
                IPluginService orElse = PluginServiceSkeleton.asInterface(iRemoteObject).orElse(null);
                if (orElse == null) {
                    HiAILog.error(TtsClient.TAG, "PluginService is null");
                    TtsClient.this.sendCallback(1, 1000);
                    TtsClient.this.ttsCallback.releaseTtsListener();
                    return;
                }
                try {
                    IRemoteObject splitRemoteObject = orElse.getSplitRemoteObject(524288);
                    if (splitRemoteObject == null) {
                        HiAILog.error(TtsClient.TAG, "SplitRemoteObject is null");
                        TtsClient.this.sendCallback(1, 1000);
                        TtsClient.this.ttsCallback.releaseTtsListener();
                        return;
                    }
                    TtsClient.this.ttsService = TtsPluginService.Stub.asInterface(splitRemoteObject).orElse(null);
                    if (TtsClient.this.ttsService != null) {
                        HiAILog.info(TtsClient.TAG, "TtsService is not null, initOnAppStart");
                        if (TtsClient.this.ttsService.initOnAppStart(TtsClient.this.ttsCallback)) {
                            TtsClient.this.sendCallback(0, 1000);
                            return;
                        }
                        TtsClient.this.sendCallback(1, 1000);
                        TtsClient.this.ttsCallback.releaseTtsListener();
                        return;
                    }
                    TtsClient.this.sendCallback(1, 1000);
                    TtsClient.this.ttsCallback.releaseTtsListener();
                } catch (RemoteException unused) {
                    HiAILog.error(TtsClient.TAG, "onServiceConnected RemoteException");
                    TtsClient.this.sendCallback(1, 1000);
                    TtsClient.this.ttsCallback.releaseTtsListener();
                }
            }
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            String str = TtsClient.TAG;
            HiAILog.info(str, "TtsService disconnect done, resultCode is: " + i);
        }
    }

    private boolean isCreated() {
        return this.ttsService != null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCallback(int i, int i2) {
        if (this.ttsCallback != null) {
            PacMap pacMap = new PacMap();
            pacMap.putIntValue(PAC_MAP_KEY_METHOD_ID, i2);
            this.ttsCallback.onEvent(i, pacMap);
            return;
        }
        HiAILog.warn(TAG, "sendResult call back failed, ttsCallback is null");
    }

    private Optional<InitParams> convertToTtsParams(TtsParams ttsParams) {
        HiAILog.info(TAG, "convertToTtsParams");
        InitParams initParams = new InitParams();
        if (ttsParams == null) {
            HiAILog.error(TAG, "Outer params pass null");
            return Optional.empty();
        }
        String deviceId = ttsParams.getDeviceId();
        if (deviceId == null || deviceId.length() > 64) {
            initParams.setDeviceId(null);
        } else {
            initParams.setDeviceId(deviceId.trim());
        }
        initParams.setDeviceType(ttsParams.getDeviceType());
        initParams.setPitch(ttsParams.getPitch());
        initParams.setSpeaker(ttsParams.getSpeaker());
        initParams.setSpeed(ttsParams.getSpeed());
        initParams.setVolume(ttsParams.getVolume());
        return Optional.of(initParams);
    }
}
