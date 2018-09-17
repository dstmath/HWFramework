package com.android.server.soundtrigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger;
import android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.hardware.soundtrigger.SoundTrigger.ModuleProperties;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.hardware.soundtrigger.SoundTrigger.RecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.SoundModel;
import android.hardware.soundtrigger.SoundTrigger.SoundModelEvent;
import android.hardware.soundtrigger.SoundTrigger.StatusListener;
import android.hardware.soundtrigger.SoundTriggerModule;
import android.os.DeadObjectException;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

public class SoundTriggerHelper implements StatusListener {
    static final boolean DBG = true;
    private static final int INVALID_VALUE = Integer.MIN_VALUE;
    public static final int STATUS_ERROR = Integer.MIN_VALUE;
    public static final int STATUS_OK = 0;
    static final String TAG = "SoundTriggerHelper";
    private boolean mCallActive = false;
    private final Context mContext;
    private boolean mIsPowerSaveMode = false;
    private HashMap<Integer, UUID> mKeyphraseUuidMap;
    private final Object mLock = new Object();
    private final HashMap<UUID, ModelData> mModelDataMap;
    private SoundTriggerModule mModule;
    final ModuleProperties mModuleProperties;
    private final PhoneStateListener mPhoneStateListener;
    private final PowerManager mPowerManager;
    private PowerSaveModeListener mPowerSaveModeListener;
    private boolean mRecognitionRunning = false;
    private boolean mServiceDisabled = false;
    private final TelephonyManager mTelephonyManager;

    private static class ModelData {
        static final int MODEL_LOADED = 1;
        static final int MODEL_NOTLOADED = 0;
        static final int MODEL_STARTED = 2;
        private IRecognitionStatusCallback mCallback = null;
        private int mModelHandle = Integer.MIN_VALUE;
        private UUID mModelId;
        private int mModelState;
        private int mModelType = -1;
        private RecognitionConfig mRecognitionConfig = null;
        private boolean mRequested = false;
        private SoundModel mSoundModel = null;

        private ModelData(UUID modelId, int modelType) {
            this.mModelId = modelId;
            this.mModelType = modelType;
        }

        static ModelData createKeyphraseModelData(UUID modelId) {
            return new ModelData(modelId, 0);
        }

        static ModelData createGenericModelData(UUID modelId) {
            return new ModelData(modelId, 1);
        }

        static ModelData createModelDataOfUnknownType(UUID modelId) {
            return new ModelData(modelId, -1);
        }

        synchronized void setCallback(IRecognitionStatusCallback callback) {
            this.mCallback = callback;
        }

        synchronized IRecognitionStatusCallback getCallback() {
            return this.mCallback;
        }

        synchronized boolean isModelLoaded() {
            boolean z = true;
            synchronized (this) {
                if (!(this.mModelState == 1 || this.mModelState == 2)) {
                    z = false;
                }
            }
            return z;
        }

        synchronized boolean isModelNotLoaded() {
            boolean z = false;
            synchronized (this) {
                if (this.mModelState == 0) {
                    z = true;
                }
            }
            return z;
        }

        synchronized void setStarted() {
            this.mModelState = 2;
        }

        synchronized void setStopped() {
            this.mModelState = 1;
        }

        synchronized void setLoaded() {
            this.mModelState = 1;
        }

        synchronized boolean isModelStarted() {
            return this.mModelState == 2;
        }

        synchronized void clearState() {
            this.mModelState = 0;
            this.mModelHandle = Integer.MIN_VALUE;
            this.mRecognitionConfig = null;
            this.mRequested = false;
            this.mCallback = null;
        }

        synchronized void clearCallback() {
            this.mCallback = null;
        }

        synchronized void setHandle(int handle) {
            this.mModelHandle = handle;
        }

        synchronized void setRecognitionConfig(RecognitionConfig config) {
            this.mRecognitionConfig = config;
        }

        synchronized int getHandle() {
            return this.mModelHandle;
        }

        synchronized UUID getModelId() {
            return this.mModelId;
        }

        synchronized RecognitionConfig getRecognitionConfig() {
            return this.mRecognitionConfig;
        }

        synchronized boolean isRequested() {
            return this.mRequested;
        }

        synchronized void setRequested(boolean requested) {
            this.mRequested = requested;
        }

        synchronized void setSoundModel(SoundModel soundModel) {
            this.mSoundModel = soundModel;
        }

        synchronized SoundModel getSoundModel() {
            return this.mSoundModel;
        }

        synchronized int getModelType() {
            return this.mModelType;
        }

        synchronized boolean isKeyphraseModel() {
            boolean z = false;
            synchronized (this) {
                if (this.mModelType == 0) {
                    z = true;
                }
            }
            return z;
        }

        synchronized boolean isGenericModel() {
            boolean z = true;
            synchronized (this) {
                if (this.mModelType != 1) {
                    z = false;
                }
            }
            return z;
        }

        synchronized String stateToString() {
            switch (this.mModelState) {
                case 0:
                    return "NOT_LOADED";
                case 1:
                    return "LOADED";
                case 2:
                    return "STARTED";
                default:
                    return "Unknown state";
            }
        }

        synchronized String requestedToString() {
            return "Requested: " + (this.mRequested ? "Yes" : "No");
        }

        synchronized String callbackToString() {
            return "Callback: " + (this.mCallback != null ? this.mCallback.asBinder() : "null");
        }

        synchronized String uuidToString() {
            return "UUID: " + this.mModelId;
        }

        public synchronized String toString() {
            return "Handle: " + this.mModelHandle + "\n" + "ModelState: " + stateToString() + "\n" + requestedToString() + "\n" + callbackToString() + "\n" + uuidToString() + "\n" + modelTypeToString();
        }

        synchronized String modelTypeToString() {
            String type;
            type = null;
            switch (this.mModelType) {
                case -1:
                    type = "Unknown";
                    break;
                case 0:
                    type = "Keyphrase";
                    break;
                case 1:
                    type = "Generic";
                    break;
            }
            return "Model type: " + type + "\n";
        }
    }

    class MyCallStateListener extends PhoneStateListener {
        MyCallStateListener() {
        }

        public void onCallStateChanged(int state, String arg1) {
            boolean z = false;
            Slog.d(SoundTriggerHelper.TAG, "onCallStateChanged: " + state);
            synchronized (SoundTriggerHelper.this.mLock) {
                SoundTriggerHelper soundTriggerHelper = SoundTriggerHelper.this;
                if (state != 0) {
                    z = true;
                }
                soundTriggerHelper.onCallStateChangedLocked(z);
            }
        }
    }

    class PowerSaveModeListener extends BroadcastReceiver {
        PowerSaveModeListener() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(intent.getAction())) {
                boolean active = SoundTriggerHelper.this.mPowerManager.getPowerSaveState(8).batterySaverEnabled;
                Slog.d(SoundTriggerHelper.TAG, "onPowerSaveModeChanged: " + active);
                synchronized (SoundTriggerHelper.this.mLock) {
                    SoundTriggerHelper.this.onPowerSaveModeChangedLocked(active);
                }
            }
        }
    }

    SoundTriggerHelper(Context context) {
        ArrayList<ModuleProperties> modules = new ArrayList();
        int status = SoundTrigger.listModules(modules);
        this.mContext = context;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mModelDataMap = new HashMap();
        this.mKeyphraseUuidMap = new HashMap();
        this.mPhoneStateListener = new MyCallStateListener();
        if (status != 0 || modules.size() == 0) {
            Slog.w(TAG, "listModules status=" + status + ", # of modules=" + modules.size());
            this.mModuleProperties = null;
            this.mModule = null;
            return;
        }
        this.mModuleProperties = (ModuleProperties) modules.get(0);
    }

    int startGenericRecognition(UUID modelId, GenericSoundModel soundModel, IRecognitionStatusCallback callback, RecognitionConfig recognitionConfig) {
        MetricsLogger.count(this.mContext, "sth_start_recognition", 1);
        if (modelId == null || soundModel == null || callback == null || recognitionConfig == null) {
            Slog.w(TAG, "Passed in bad data to startGenericRecognition().");
            return Integer.MIN_VALUE;
        }
        synchronized (this.mLock) {
            ModelData modelData = getOrCreateGenericModelDataLocked(modelId);
            if (modelData == null) {
                Slog.w(TAG, "Irrecoverable error occurred, check UUID / sound model data.");
                return Integer.MIN_VALUE;
            }
            int startRecognition = startRecognition(soundModel, modelData, callback, recognitionConfig, Integer.MIN_VALUE);
            return startRecognition;
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0013, code:
            return Integer.MIN_VALUE;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int startKeyphraseRecognition(int keyphraseId, KeyphraseSoundModel soundModel, IRecognitionStatusCallback callback, RecognitionConfig recognitionConfig) {
        synchronized (this.mLock) {
            MetricsLogger.count(this.mContext, "sth_start_recognition", 1);
            if (soundModel == null || callback == null || recognitionConfig == null) {
            } else {
                Slog.d(TAG, "startKeyphraseRecognition for keyphraseId=" + keyphraseId + " soundModel=" + soundModel + ", callback=" + callback.asBinder() + ", recognitionConfig=" + recognitionConfig);
                Slog.d(TAG, "moduleProperties=" + this.mModuleProperties);
                dumpModelStateLocked();
                ModelData model = getKeyphraseModelDataLocked(keyphraseId);
                if (model == null || (model.isKeyphraseModel() ^ 1) == 0) {
                    if (model != null) {
                        if ((model.getModelId().equals(soundModel.uuid) ^ 1) != 0) {
                            int status = cleanUpExistingKeyphraseModelLocked(model);
                            if (status != 0) {
                                return status;
                            }
                            removeKeyphraseModelLocked(keyphraseId);
                            model = null;
                        }
                    }
                    if (model == null) {
                        model = createKeyphraseModelDataLocked(soundModel.uuid, keyphraseId);
                    }
                    int startRecognition = startRecognition(soundModel, model, callback, recognitionConfig, keyphraseId);
                    return startRecognition;
                }
                Slog.e(TAG, "Generic model with same UUID exists.");
                return Integer.MIN_VALUE;
            }
        }
    }

    private int cleanUpExistingKeyphraseModelLocked(ModelData modelData) {
        int status = tryStopAndUnloadLocked(modelData, true, true);
        if (status != 0) {
            Slog.w(TAG, "Unable to stop or unload previous model: " + modelData.toString());
        }
        return status;
    }

    int startRecognition(SoundModel soundModel, ModelData modelData, IRecognitionStatusCallback callback, RecognitionConfig recognitionConfig, int keyphraseId) {
        synchronized (this.mLock) {
            if (this.mModuleProperties == null) {
                Slog.w(TAG, "Attempting startRecognition without the capability");
                return Integer.MIN_VALUE;
            }
            int status;
            if (this.mModule == null) {
                this.mModule = SoundTrigger.attachModule(this.mModuleProperties.id, this, null);
                if (this.mModule == null) {
                    Slog.w(TAG, "startRecognition cannot attach to sound trigger module");
                    return Integer.MIN_VALUE;
                }
            }
            if (!this.mRecognitionRunning) {
                initializeTelephonyAndPowerStateListeners();
            }
            if (modelData.getSoundModel() != null) {
                boolean stopModel = false;
                boolean unloadModel = false;
                if (modelData.getSoundModel().equals(soundModel) && modelData.isModelStarted()) {
                    stopModel = true;
                    unloadModel = false;
                } else if (!modelData.getSoundModel().equals(soundModel)) {
                    stopModel = modelData.isModelStarted();
                    unloadModel = modelData.isModelLoaded();
                }
                if (stopModel || unloadModel) {
                    status = tryStopAndUnloadLocked(modelData, stopModel, unloadModel);
                    if (status != 0) {
                        Slog.w(TAG, "Unable to stop or unload previous model: " + modelData.toString());
                        return status;
                    }
                }
            }
            IRecognitionStatusCallback oldCallback = modelData.getCallback();
            if (!(oldCallback == null || oldCallback.asBinder() == callback.asBinder())) {
                Slog.w(TAG, "Canceling previous recognition for model id: " + modelData.getModelId());
                try {
                    oldCallback.onError(Integer.MIN_VALUE);
                } catch (RemoteException e) {
                    Slog.w(TAG, "RemoteException in onDetectionStopped", e);
                }
                modelData.clearCallback();
            }
            if (!modelData.isModelLoaded()) {
                int[] handle = new int[]{Integer.MIN_VALUE};
                status = this.mModule.loadSoundModel(soundModel, handle);
                if (status != 0) {
                    Slog.w(TAG, "loadSoundModel call failed with " + status);
                    return status;
                } else if (handle[0] == Integer.MIN_VALUE) {
                    Slog.w(TAG, "loadSoundModel call returned invalid sound model handle");
                    return Integer.MIN_VALUE;
                } else {
                    modelData.setHandle(handle[0]);
                    modelData.setLoaded();
                    Slog.d(TAG, "Sound model loaded with handle:" + handle[0]);
                }
            }
            modelData.setCallback(callback);
            modelData.setRequested(true);
            modelData.setRecognitionConfig(recognitionConfig);
            modelData.setSoundModel(soundModel);
            int startRecognitionLocked = startRecognitionLocked(modelData, false);
            return startRecognitionLocked;
        }
    }

    /* JADX WARNING: Missing block: B:22:0x007d, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int stopGenericRecognition(UUID modelId, IRecognitionStatusCallback callback) {
        synchronized (this.mLock) {
            MetricsLogger.count(this.mContext, "sth_stop_recognition", 1);
            if (callback == null || modelId == null) {
                Slog.e(TAG, "Null callbackreceived for stopGenericRecognition() for modelid:" + modelId);
                return Integer.MIN_VALUE;
            }
            ModelData modelData = (ModelData) this.mModelDataMap.get(modelId);
            if (modelData == null || (modelData.isGenericModel() ^ 1) != 0) {
                Slog.w(TAG, "Attempting stopRecognition on invalid model with id:" + modelId);
                return Integer.MIN_VALUE;
            }
            int status = stopRecognition(modelData, callback);
            if (status != 0) {
                Slog.w(TAG, "stopGenericRecognition failed: " + status);
            }
        }
    }

    int stopKeyphraseRecognition(int keyphraseId, IRecognitionStatusCallback callback) {
        synchronized (this.mLock) {
            MetricsLogger.count(this.mContext, "sth_stop_recognition", 1);
            if (callback == null) {
                Slog.e(TAG, "Null callback received for stopKeyphraseRecognition() for keyphraseId:" + keyphraseId);
                return Integer.MIN_VALUE;
            }
            ModelData modelData = getKeyphraseModelDataLocked(keyphraseId);
            if (modelData == null || (modelData.isKeyphraseModel() ^ 1) != 0) {
                Slog.e(TAG, "No model exists for given keyphrase Id " + keyphraseId);
                return Integer.MIN_VALUE;
            }
            Object obj;
            Slog.d(TAG, "stopRecognition for keyphraseId=" + keyphraseId + ", callback =" + callback.asBinder());
            String str = TAG;
            StringBuilder append = new StringBuilder().append("current callback=");
            if (modelData == null) {
                obj = "null";
            } else {
                obj = modelData.getCallback().asBinder();
            }
            Slog.d(str, append.append(obj).toString());
            int status = stopRecognition(modelData, callback);
            if (status != 0) {
                return status;
            }
            return status;
        }
    }

    /* JADX WARNING: Missing block: B:41:0x0077, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int stopRecognition(ModelData modelData, IRecognitionStatusCallback callback) {
        synchronized (this.mLock) {
            if (callback == null) {
                return Integer.MIN_VALUE;
            } else if (this.mModuleProperties == null || this.mModule == null) {
                Slog.w(TAG, "Attempting stopRecognition without the capability");
                return Integer.MIN_VALUE;
            } else {
                IRecognitionStatusCallback currentCallback = modelData.getCallback();
                if (!(modelData == null || currentCallback == null)) {
                    if (modelData.isRequested() || (modelData.isModelStarted() ^ 1) == 0) {
                        if (currentCallback.asBinder() != callback.asBinder()) {
                            Slog.w(TAG, "Attempting stopRecognition for another recognition");
                            return Integer.MIN_VALUE;
                        }
                        modelData.setRequested(false);
                        int status = updateRecognitionLocked(modelData, isRecognitionAllowed(), false);
                        if (status != 0) {
                            return status;
                        }
                        modelData.setLoaded();
                        modelData.clearCallback();
                        modelData.setRecognitionConfig(null);
                        if (!computeRecognitionRunningLocked()) {
                            internalClearGlobalStateLocked();
                        }
                    }
                }
                Slog.w(TAG, "Attempting stopRecognition without a successful startRecognition");
                return Integer.MIN_VALUE;
            }
        }
    }

    private int tryStopAndUnloadLocked(ModelData modelData, boolean stopModel, boolean unloadModel) {
        int status = 0;
        if (modelData.isModelNotLoaded()) {
            return 0;
        }
        if (stopModel && modelData.isModelStarted()) {
            status = stopRecognitionLocked(modelData, false);
            if (status != 0) {
                Slog.w(TAG, "stopRecognition failed: " + status);
                return status;
            }
        }
        if (unloadModel && modelData.isModelLoaded()) {
            Slog.d(TAG, "Unloading previously loaded stale model.");
            status = this.mModule.unloadSoundModel(modelData.getHandle());
            MetricsLogger.count(this.mContext, "sth_unloading_stale_model", 1);
            if (status != 0) {
                Slog.w(TAG, "unloadSoundModel call failed with " + status);
            } else {
                modelData.clearState();
            }
        }
        return status;
    }

    public ModuleProperties getModuleProperties() {
        return this.mModuleProperties;
    }

    /* JADX WARNING: Missing block: B:7:0x0019, code:
            return Integer.MIN_VALUE;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int unloadKeyphraseSoundModel(int keyphraseId) {
        synchronized (this.mLock) {
            MetricsLogger.count(this.mContext, "sth_unload_keyphrase_sound_model", 1);
            ModelData modelData = getKeyphraseModelDataLocked(keyphraseId);
            if (!(this.mModule == null || modelData == null)) {
                if (modelData.getHandle() != Integer.MIN_VALUE && (modelData.isKeyphraseModel() ^ 1) == 0) {
                    modelData.setRequested(false);
                    int status = updateRecognitionLocked(modelData, isRecognitionAllowed(), false);
                    if (status != 0) {
                        Slog.w(TAG, "Stop recognition failed for keyphrase ID:" + status);
                    }
                    status = this.mModule.unloadSoundModel(modelData.getHandle());
                    if (status != 0) {
                        Slog.w(TAG, "unloadKeyphraseSoundModel call failed with " + status);
                    }
                    removeKeyphraseModelLocked(keyphraseId);
                    return status;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0016, code:
            return Integer.MIN_VALUE;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int unloadGenericSoundModel(UUID modelId) {
        synchronized (this.mLock) {
            MetricsLogger.count(this.mContext, "sth_unload_generic_sound_model", 1);
            if (modelId == null || this.mModule == null) {
            } else {
                ModelData modelData = (ModelData) this.mModelDataMap.get(modelId);
                if (modelData == null || (modelData.isGenericModel() ^ 1) != 0) {
                    Slog.w(TAG, "Unload error: Attempting unload invalid generic model with id:" + modelId);
                    return Integer.MIN_VALUE;
                } else if (modelData.isModelLoaded()) {
                    int status;
                    if (modelData.isModelStarted()) {
                        status = stopRecognitionLocked(modelData, false);
                        if (status != 0) {
                            Slog.w(TAG, "stopGenericRecognition failed: " + status);
                        }
                    }
                    status = this.mModule.unloadSoundModel(modelData.getHandle());
                    if (status != 0) {
                        Slog.w(TAG, "unloadGenericSoundModel() call failed with " + status);
                        Slog.w(TAG, "unloadGenericSoundModel() force-marking model as unloaded.");
                    }
                    this.mModelDataMap.remove(modelId);
                    dumpModelStateLocked();
                    return status;
                } else {
                    Slog.i(TAG, "Unload: Given generic model is not loaded:" + modelId);
                    return 0;
                }
            }
        }
    }

    public void onRecognition(RecognitionEvent event) {
        if (event == null) {
            Slog.w(TAG, "Null recognition event!");
        } else if ((event instanceof KeyphraseRecognitionEvent) || ((event instanceof GenericRecognitionEvent) ^ 1) == 0) {
            Slog.d(TAG, "onRecognition: " + event);
            synchronized (this.mLock) {
                switch (event.status) {
                    case 0:
                        if (!isKeyphraseRecognitionEvent(event)) {
                            onGenericRecognitionSuccessLocked((GenericRecognitionEvent) event);
                            break;
                        } else {
                            onKeyphraseRecognitionSuccessLocked((KeyphraseRecognitionEvent) event);
                            break;
                        }
                    case 1:
                        onRecognitionAbortLocked(event);
                        break;
                    case 2:
                        onRecognitionFailureLocked();
                        break;
                }
            }
        } else {
            Slog.w(TAG, "Invalid recognition event type (not one of generic or keyphrase)!");
        }
    }

    private boolean isKeyphraseRecognitionEvent(RecognitionEvent event) {
        return event instanceof KeyphraseRecognitionEvent;
    }

    private void onGenericRecognitionSuccessLocked(GenericRecognitionEvent event) {
        MetricsLogger.count(this.mContext, "sth_generic_recognition_event", 1);
        if (event.status == 0) {
            ModelData model = getModelDataForLocked(event.soundModelHandle);
            if (model == null || (model.isGenericModel() ^ 1) != 0) {
                Slog.w(TAG, "Generic recognition event: Model does not exist for handle: " + event.soundModelHandle);
                return;
            }
            IRecognitionStatusCallback callback = model.getCallback();
            if (callback == null) {
                Slog.w(TAG, "Generic recognition event: Null callback for model handle: " + event.soundModelHandle);
                return;
            }
            model.setStopped();
            try {
                callback.onGenericSoundTriggerDetected(event);
            } catch (DeadObjectException e) {
                forceStopAndUnloadModelLocked(model, e);
                return;
            } catch (RemoteException e2) {
                Slog.w(TAG, "RemoteException in onGenericSoundTriggerDetected", e2);
            }
            RecognitionConfig config = model.getRecognitionConfig();
            if (config == null) {
                Slog.w(TAG, "Generic recognition event: Null RecognitionConfig for model handle: " + event.soundModelHandle);
                return;
            }
            model.setRequested(config.allowMultipleTriggers);
            if (model.isRequested()) {
                updateRecognitionLocked(model, isRecognitionAllowed(), true);
            }
        }
    }

    public void onSoundModelUpdate(SoundModelEvent event) {
        if (event == null) {
            Slog.w(TAG, "Invalid sound model event!");
            return;
        }
        Slog.d(TAG, "onSoundModelUpdate: " + event);
        synchronized (this.mLock) {
            MetricsLogger.count(this.mContext, "sth_sound_model_updated", 1);
            onSoundModelUpdatedLocked(event);
        }
    }

    public void onServiceStateChange(int state) {
        boolean z = true;
        Slog.d(TAG, "onServiceStateChange, state: " + state);
        synchronized (this.mLock) {
            if (1 != state) {
                z = false;
            }
            onServiceStateChangedLocked(z);
        }
    }

    public void onServiceDied() {
        Slog.e(TAG, "onServiceDied!!");
        MetricsLogger.count(this.mContext, "sth_service_died", 1);
        synchronized (this.mLock) {
            onServiceDiedLocked();
        }
    }

    private void onCallStateChangedLocked(boolean callActive) {
        if (this.mCallActive != callActive) {
            this.mCallActive = callActive;
            updateAllRecognitionsLocked(true);
        }
    }

    private void onPowerSaveModeChangedLocked(boolean isPowerSaveMode) {
        if (this.mIsPowerSaveMode != isPowerSaveMode) {
            this.mIsPowerSaveMode = isPowerSaveMode;
            updateAllRecognitionsLocked(true);
        }
    }

    private void onSoundModelUpdatedLocked(SoundModelEvent event) {
    }

    private void onServiceStateChangedLocked(boolean disabled) {
        if (disabled != this.mServiceDisabled) {
            this.mServiceDisabled = disabled;
            updateAllRecognitionsLocked(true);
        }
    }

    private void onRecognitionAbortLocked(RecognitionEvent event) {
        Slog.w(TAG, "Recognition aborted");
        MetricsLogger.count(this.mContext, "sth_recognition_aborted", 1);
        ModelData modelData = getModelDataForLocked(event.soundModelHandle);
        if (modelData != null && modelData.isModelStarted()) {
            modelData.setStopped();
            try {
                modelData.getCallback().onRecognitionPaused();
            } catch (DeadObjectException e) {
                forceStopAndUnloadModelLocked(modelData, e);
            } catch (RemoteException e2) {
                Slog.w(TAG, "RemoteException in onRecognitionPaused", e2);
            }
        }
    }

    private void onRecognitionFailureLocked() {
        Slog.w(TAG, "Recognition failure");
        MetricsLogger.count(this.mContext, "sth_recognition_failure_event", 1);
        try {
            sendErrorCallbacksToAllLocked(Integer.MIN_VALUE);
        } finally {
            internalClearModelStateLocked();
            internalClearGlobalStateLocked();
        }
    }

    private int getKeyphraseIdFromEvent(KeyphraseRecognitionEvent event) {
        if (event == null) {
            Slog.w(TAG, "Null RecognitionEvent received.");
            return Integer.MIN_VALUE;
        }
        KeyphraseRecognitionExtra[] keyphraseExtras = event.keyphraseExtras;
        if (keyphraseExtras != null && keyphraseExtras.length != 0) {
            return keyphraseExtras[0].id;
        }
        Slog.w(TAG, "Invalid keyphrase recognition event!");
        return Integer.MIN_VALUE;
    }

    private void onKeyphraseRecognitionSuccessLocked(KeyphraseRecognitionEvent event) {
        Slog.i(TAG, "Recognition success");
        MetricsLogger.count(this.mContext, "sth_keyphrase_recognition_event", 1);
        int keyphraseId = getKeyphraseIdFromEvent(event);
        ModelData modelData = getKeyphraseModelDataLocked(keyphraseId);
        if (modelData == null || (modelData.isKeyphraseModel() ^ 1) != 0) {
            Slog.e(TAG, "Keyphase model data does not exist for ID:" + keyphraseId);
        } else if (modelData.getCallback() == null) {
            Slog.w(TAG, "Received onRecognition event without callback for keyphrase model.");
        } else {
            modelData.setStopped();
            try {
                modelData.getCallback().onKeyphraseDetected(event);
            } catch (DeadObjectException e) {
                forceStopAndUnloadModelLocked(modelData, e);
                return;
            } catch (RemoteException e2) {
                Slog.w(TAG, "RemoteException in onKeyphraseDetected", e2);
            }
            RecognitionConfig config = modelData.getRecognitionConfig();
            if (config != null) {
                modelData.setRequested(config.allowMultipleTriggers);
            }
            if (modelData.isRequested()) {
                updateRecognitionLocked(modelData, isRecognitionAllowed(), true);
            }
        }
    }

    private void updateAllRecognitionsLocked(boolean notify) {
        boolean isAllowed = isRecognitionAllowed();
        for (ModelData modelData : new ArrayList(this.mModelDataMap.values())) {
            updateRecognitionLocked(modelData, isAllowed, notify);
        }
    }

    private int updateRecognitionLocked(ModelData model, boolean isAllowed, boolean notify) {
        boolean start = model.isRequested() ? isAllowed : false;
        if (start == model.isModelStarted()) {
            return 0;
        }
        if (start) {
            return startRecognitionLocked(model, notify);
        }
        return stopRecognitionLocked(model, notify);
    }

    private void onServiceDiedLocked() {
        try {
            MetricsLogger.count(this.mContext, "sth_service_died", 1);
            sendErrorCallbacksToAllLocked(SoundTrigger.STATUS_DEAD_OBJECT);
        } finally {
            internalClearModelStateLocked();
            internalClearGlobalStateLocked();
            if (this.mModule != null) {
                this.mModule.detach();
                this.mModule = null;
            }
        }
    }

    private void internalClearGlobalStateLocked() {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        if (this.mPowerSaveModeListener != null) {
            this.mContext.unregisterReceiver(this.mPowerSaveModeListener);
            this.mPowerSaveModeListener = null;
        }
    }

    private void internalClearModelStateLocked() {
        for (ModelData modelData : this.mModelDataMap.values()) {
            modelData.clearState();
        }
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            pw.print("  module properties=");
            pw.println(this.mModuleProperties == null ? "null" : this.mModuleProperties);
            pw.print("  call active=");
            pw.println(this.mCallActive);
            pw.print("  power save mode active=");
            pw.println(this.mIsPowerSaveMode);
            pw.print("  service disabled=");
            pw.println(this.mServiceDisabled);
        }
    }

    private void initializeTelephonyAndPowerStateListeners() {
        boolean z = false;
        if (this.mTelephonyManager.getCallState() != 0) {
            z = true;
        }
        this.mCallActive = z;
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
        if (this.mPowerSaveModeListener == null) {
            this.mPowerSaveModeListener = new PowerSaveModeListener();
            this.mContext.registerReceiver(this.mPowerSaveModeListener, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGED"));
        }
        this.mIsPowerSaveMode = this.mPowerManager.getPowerSaveState(8).batterySaverEnabled;
    }

    private void sendErrorCallbacksToAllLocked(int errorCode) {
        for (ModelData modelData : this.mModelDataMap.values()) {
            IRecognitionStatusCallback callback = modelData.getCallback();
            if (callback != null) {
                try {
                    callback.onError(errorCode);
                } catch (RemoteException e) {
                    Slog.w(TAG, "RemoteException sendErrorCallbacksToAllLocked for model handle " + modelData.getHandle(), e);
                }
            }
        }
    }

    private void forceStopAndUnloadModelLocked(ModelData modelData, Exception exception) {
        if (exception != null) {
            Slog.e(TAG, "forceStopAndUnloadModel", exception);
        }
        if (modelData.isModelStarted()) {
            Slog.d(TAG, "Stopping previously started dangling model " + modelData.getHandle());
            if (this.mModule.stopRecognition(modelData.getHandle()) != 0) {
                modelData.setStopped();
                modelData.setRequested(false);
            } else {
                Slog.e(TAG, "Failed to stop model " + modelData.getHandle());
            }
        }
        if (modelData.isModelLoaded()) {
            Slog.d(TAG, "Unloading previously loaded dangling model " + modelData.getHandle());
            if (this.mModule.unloadSoundModel(modelData.getHandle()) == 0) {
                this.mModelDataMap.remove(modelData.getModelId());
                Iterator it = this.mKeyphraseUuidMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (((Entry) it.next()).getValue().equals(modelData.getModelId())) {
                        it.remove();
                    }
                }
                modelData.clearState();
                return;
            }
            Slog.e(TAG, "Failed to unload model " + modelData.getHandle());
        }
    }

    private ModelData getOrCreateGenericModelDataLocked(UUID modelId) {
        ModelData modelData = (ModelData) this.mModelDataMap.get(modelId);
        if (modelData == null) {
            modelData = ModelData.createGenericModelData(modelId);
            this.mModelDataMap.put(modelId, modelData);
        } else if (!modelData.isGenericModel()) {
            Slog.e(TAG, "UUID already used for non-generic model.");
            return null;
        }
        return modelData;
    }

    private void removeKeyphraseModelLocked(int keyphraseId) {
        UUID uuid = (UUID) this.mKeyphraseUuidMap.get(Integer.valueOf(keyphraseId));
        if (uuid != null) {
            this.mModelDataMap.remove(uuid);
            this.mKeyphraseUuidMap.remove(Integer.valueOf(keyphraseId));
        }
    }

    private ModelData getKeyphraseModelDataLocked(int keyphraseId) {
        UUID uuid = (UUID) this.mKeyphraseUuidMap.get(Integer.valueOf(keyphraseId));
        if (uuid == null) {
            return null;
        }
        return (ModelData) this.mModelDataMap.get(uuid);
    }

    private ModelData createKeyphraseModelDataLocked(UUID modelId, int keyphraseId) {
        this.mKeyphraseUuidMap.remove(Integer.valueOf(keyphraseId));
        this.mModelDataMap.remove(modelId);
        this.mKeyphraseUuidMap.put(Integer.valueOf(keyphraseId), modelId);
        ModelData modelData = ModelData.createKeyphraseModelData(modelId);
        this.mModelDataMap.put(modelId, modelData);
        return modelData;
    }

    private ModelData getModelDataForLocked(int modelHandle) {
        for (ModelData model : this.mModelDataMap.values()) {
            if (model.getHandle() == modelHandle) {
                return model;
            }
        }
        return null;
    }

    private boolean isRecognitionAllowed() {
        return (this.mCallActive || (this.mServiceDisabled ^ 1) == 0) ? false : this.mIsPowerSaveMode ^ 1;
    }

    private int startRecognitionLocked(ModelData modelData, boolean notify) {
        IRecognitionStatusCallback callback = modelData.getCallback();
        int handle = modelData.getHandle();
        RecognitionConfig config = modelData.getRecognitionConfig();
        if (callback == null || handle == Integer.MIN_VALUE || config == null) {
            Slog.w(TAG, "startRecognition: Bad data passed in.");
            MetricsLogger.count(this.mContext, "sth_start_recognition_error", 1);
            return Integer.MIN_VALUE;
        } else if (isRecognitionAllowed()) {
            int status = this.mModule.startRecognition(handle, config);
            if (status != 0) {
                Slog.w(TAG, "startRecognition failed with " + status);
                MetricsLogger.count(this.mContext, "sth_start_recognition_error", 1);
                if (notify) {
                    try {
                        callback.onError(status);
                    } catch (DeadObjectException e) {
                        forceStopAndUnloadModelLocked(modelData, e);
                    } catch (RemoteException e2) {
                        Slog.w(TAG, "RemoteException in onError", e2);
                    }
                }
            } else {
                Slog.i(TAG, "startRecognition successful.");
                MetricsLogger.count(this.mContext, "sth_start_recognition_success", 1);
                modelData.setStarted();
                if (notify) {
                    try {
                        callback.onRecognitionResumed();
                    } catch (DeadObjectException e3) {
                        forceStopAndUnloadModelLocked(modelData, e3);
                    } catch (RemoteException e22) {
                        Slog.w(TAG, "RemoteException in onRecognitionResumed", e22);
                    }
                }
            }
            Slog.d(TAG, "Model being started :" + modelData.toString());
            return status;
        } else {
            Slog.w(TAG, "startRecognition requested but not allowed.");
            MetricsLogger.count(this.mContext, "sth_start_recognition_not_allowed", 1);
            return 0;
        }
    }

    private int stopRecognitionLocked(ModelData modelData, boolean notify) {
        IRecognitionStatusCallback callback = modelData.getCallback();
        int status = this.mModule.stopRecognition(modelData.getHandle());
        if (status != 0) {
            Slog.w(TAG, "stopRecognition call failed with " + status);
            MetricsLogger.count(this.mContext, "sth_stop_recognition_error", 1);
            if (notify) {
                try {
                    callback.onError(status);
                } catch (DeadObjectException e) {
                    forceStopAndUnloadModelLocked(modelData, e);
                } catch (RemoteException e2) {
                    Slog.w(TAG, "RemoteException in onError", e2);
                }
            }
        } else {
            modelData.setStopped();
            MetricsLogger.count(this.mContext, "sth_stop_recognition_success", 1);
            if (notify) {
                try {
                    callback.onRecognitionPaused();
                } catch (DeadObjectException e3) {
                    forceStopAndUnloadModelLocked(modelData, e3);
                } catch (RemoteException e22) {
                    Slog.w(TAG, "RemoteException in onRecognitionPaused", e22);
                }
            }
        }
        Slog.d(TAG, "Model being stopped :" + modelData.toString());
        return status;
    }

    private void dumpModelStateLocked() {
        for (UUID modelId : this.mModelDataMap.keySet()) {
            Slog.i(TAG, "Model :" + ((ModelData) this.mModelDataMap.get(modelId)).toString());
        }
    }

    private boolean computeRecognitionRunningLocked() {
        if (this.mModuleProperties == null || this.mModule == null) {
            this.mRecognitionRunning = false;
            return this.mRecognitionRunning;
        }
        for (ModelData modelData : this.mModelDataMap.values()) {
            if (modelData.isModelStarted()) {
                this.mRecognitionRunning = true;
                return this.mRecognitionRunning;
            }
        }
        this.mRecognitionRunning = false;
        return this.mRecognitionRunning;
    }
}
