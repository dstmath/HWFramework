package ohos.ai.asr.taskhandler;

import android.content.Intent;
import android.os.RemoteException;
import ohos.ai.asr.AsrIntent;
import ohos.ai.asr.AsrListenerBinder;
import ohos.ai.asr.service.AsrPluginService;
import ohos.ai.asr.util.AsrConstants;
import ohos.ai.engine.handler.TaskHandler;
import ohos.ai.engine.handler.TaskInfo;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.dispatcher.TaskDispatcher;

public class AsrTaskHandler extends TaskHandler {
    private static final int AUDIO_LENGTH_20_MS = 640;
    private static final int AUDIO_LENGTH_40_MS = 1280;
    private static final String TAG = AsrTaskHandler.class.getSimpleName();
    private AsrListenerBinder asrListener;
    private AsrPluginService asrService;
    private TaskDispatcher taskDispatcher;

    public AsrTaskHandler(TaskDispatcher taskDispatcher2, AsrPluginService asrPluginService, AsrListenerBinder asrListenerBinder) {
        this.taskDispatcher = taskDispatcher2;
        this.asrService = asrPluginService;
        this.asrListener = asrListenerBinder;
    }

    @Override // ohos.ai.engine.handler.TaskHandler
    public void start(TaskInfo taskInfo) {
        this.taskDispatcher.asyncDispatch(new Runnable(taskInfo) {
            /* class ohos.ai.asr.taskhandler.$$Lambda$AsrTaskHandler$WEfP9wkqkpCYinRju7QZ824q5A */
            private final /* synthetic */ TaskInfo f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AsrTaskHandler.this.lambda$start$0$AsrTaskHandler(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$start$0$AsrTaskHandler(TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        String str = TAG;
        HiAILog.info(str, "handletask, taskId: " + taskId);
        if (this.asrService == null) {
            HiAILog.error(TAG, "asrService is null");
        } else if (this.asrListener == null) {
            HiAILog.error(TAG, "asrListener is null");
        } else {
            handleTask(taskInfo);
        }
    }

    private void handleTask(TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        String str = TAG;
        HiAILog.info(str, "handleTask: " + taskId);
        if (taskId != 1) {
            if (taskId == 2) {
                handleStop();
            } else if (taskId != 3) {
                handleOtherTask(taskInfo);
            } else if (!(taskInfo.getObject() instanceof AsrIntent)) {
                this.asrListener.onError(10);
            } else {
                handleInit(createAndroidIntent((AsrIntent) taskInfo.getObject(), 3));
            }
        } else if (!(taskInfo.getObject() instanceof AsrIntent)) {
            this.asrListener.onError(10);
        } else {
            handleStartListening(createAndroidIntent((AsrIntent) taskInfo.getObject(), 1));
        }
    }

    private void handleOtherTask(TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        String str = TAG;
        HiAILog.info(str, "handleTask: " + taskId);
        if (taskId != 4) {
            if (taskId == 5) {
                handleCancel();
            } else if (taskId != 6) {
                if (taskId != 7) {
                    HiAILog.debug(TAG, "isMsgHandled false");
                } else {
                    handleDestroy();
                }
            } else if (!(taskInfo.getObject() instanceof AsrIntent)) {
                this.asrListener.onError(5);
            } else {
                handleUpdateLexicon(createAndroidIntent((AsrIntent) taskInfo.getObject(), 6));
            }
        } else if (!(taskInfo.getObject() instanceof byte[])) {
            this.asrListener.onError(10);
        } else {
            handleWritePcm((byte[]) taskInfo.getObject(), taskInfo.getArgOne());
        }
    }

    private void handleInit(Intent intent) {
        HiAILog.debug(TAG, "handleInit called");
        try {
            intent.putExtra(AsrConstants.SDK_VERSION_NAME, 2);
            this.asrService.init(intent, this.asrListener);
            HiAILog.info(TAG, "service init command succeeded");
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "init() failed");
            this.asrListener.onError(23);
        }
    }

    private void handleStartListening(Intent intent) {
        HiAILog.debug(TAG, "handleStartListening called");
        try {
            this.asrService.startListening(intent, this.asrListener);
            HiAILog.info(TAG, "service start listening command succeeded");
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "startListening() failed");
            this.asrListener.onError(5);
        }
    }

    private void handleStop() {
        HiAILog.debug(TAG, "handleStop called");
        try {
            this.asrService.stopListening(this.asrListener);
            HiAILog.info(TAG, "service stop listening command succeeded");
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "stopListening() failed");
            this.asrListener.onError(5);
        }
    }

    private void handleUpdateLexicon(Intent intent) {
        HiAILog.debug(TAG, "handleUpdateLexicon called");
        try {
            if (this.asrService.checkServerVersion(2)) {
                this.asrService.updateLexicon(intent, this.asrListener);
                HiAILog.debug(TAG, "service update lexicon command succeeded");
                return;
            }
            this.asrListener.onError(15);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "updateLexicon() failed");
            this.asrListener.onError(24);
        }
    }

    private void handleWritePcm(byte[] bArr, int i) {
        HiAILog.debug(TAG, "handleWritePcm called");
        if (bArr == null || bArr.length == 0) {
            HiAILog.error(TAG, "writePcm bytes error");
            this.asrListener.onError(10);
        } else if (i == 1280 || i == 640) {
            try {
                this.asrService.writePcm(bArr, i, this.asrListener);
                HiAILog.debug(TAG, "service write pcm command succeeded");
            } catch (RemoteException unused) {
                HiAILog.error(TAG, "writePcm() failed");
                this.asrListener.onError(5);
            }
        } else {
            HiAILog.error(TAG, "writePcm length error");
            this.asrListener.onError(10);
        }
    }

    private void handleCancel() {
        HiAILog.debug(TAG, "handleCancel called");
        try {
            this.asrService.cancel(this.asrListener);
            HiAILog.debug(TAG, "service cancel command succeeded");
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "cancel() failed");
            this.asrListener.onError(5);
        }
    }

    private void handleDestroy() {
        HiAILog.debug(TAG, "handleDestroy called");
        try {
            this.asrService.cancel(this.asrListener);
            this.asrService.destroy(this.asrListener);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "destroy() failed");
        }
    }

    private Intent createAndroidIntent(AsrIntent asrIntent, int i) {
        Intent intent = new Intent();
        if (asrIntent.getFilePath() != null && i == 1) {
            intent.putExtra(AsrConstants.ASR_SRC_FILE, asrIntent.getFilePath());
        }
        if (asrIntent.getVadEndWaitMs() > 0) {
            intent.putExtra(AsrConstants.ASR_VAD_END_WAIT_MS, asrIntent.getVadEndWaitMs());
        }
        if (asrIntent.getVadFrontWaitMs() > 0) {
            intent.putExtra(AsrConstants.ASR_VAD_FRONT_WAIT_MS, asrIntent.getVadFrontWaitMs());
        }
        if (asrIntent.getTimeoutThresholdMs() > 0) {
            intent.putExtra(AsrConstants.ASR_TIMEOUT_THRESHOLD_MS, asrIntent.getTimeoutThresholdMs());
        }
        if (i == 3) {
            intent.putExtra(AsrConstants.ASR_ENGINE_TYPE, asrIntent.getEngineType());
            intent.putExtra(AsrConstants.ASR_AUDIO_SRC_TYPE, asrIntent.getAudioSourceType());
        }
        return intent;
    }
}
