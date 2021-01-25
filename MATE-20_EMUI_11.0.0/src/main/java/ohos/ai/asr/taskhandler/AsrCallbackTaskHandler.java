package ohos.ai.asr.taskhandler;

import ohos.ai.asr.AsrListener;
import ohos.ai.engine.handler.TaskHandler;
import ohos.ai.engine.handler.TaskInfo;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.utils.PacMap;

public class AsrCallbackTaskHandler extends TaskHandler {
    private static final String TAG = AsrCallbackTaskHandler.class.getSimpleName();
    private AsrListener asrListener;
    private TaskDispatcher taskDispatcher;

    public AsrCallbackTaskHandler(TaskDispatcher taskDispatcher2, AsrListener asrListener2) {
        this.taskDispatcher = taskDispatcher2;
        this.asrListener = asrListener2;
    }

    @Override // ohos.ai.engine.handler.TaskHandler
    public void start(TaskInfo taskInfo) {
        TaskDispatcher taskDispatcher2 = this.taskDispatcher;
        if (taskDispatcher2 == null) {
            HiAILog.error(TAG, "taskDispatcher is null");
        } else {
            taskDispatcher2.asyncDispatch(new Runnable(taskInfo) {
                /* class ohos.ai.asr.taskhandler.$$Lambda$AsrCallbackTaskHandler$7m1GZaRRRTx4pbQzc72jcanMlc */
                private final /* synthetic */ TaskInfo f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AsrCallbackTaskHandler.this.lambda$start$0$AsrCallbackTaskHandler(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$start$0$AsrCallbackTaskHandler(TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        String str = TAG;
        HiAILog.info(str, "handletask, taskId: " + taskId);
        if (this.asrListener == null) {
            HiAILog.error(TAG, "asrlistener is null");
        } else {
            handleTask(taskInfo);
        }
    }

    private void handleTask(TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        if (taskId != 1) {
            if (taskId == 3) {
                this.asrListener.onBeginningOfSpeech();
            } else if (taskId != 5) {
                if (taskId == 6) {
                    this.asrListener.onEndOfSpeech();
                } else if (taskId == 8) {
                    this.asrListener.onError(taskInfo.getArgOne());
                } else if (taskId != 9) {
                    handleOtherTask(taskInfo);
                } else if (taskInfo.getObject() instanceof PacMap) {
                    this.asrListener.onResults((PacMap) taskInfo.getObject());
                } else {
                    this.asrListener.onResults(null);
                }
            } else if (taskInfo.getObject() instanceof byte[]) {
                this.asrListener.onBufferReceived((byte[]) taskInfo.getObject());
            } else {
                this.asrListener.onBufferReceived(new byte[0]);
            }
        } else if (taskInfo.getObject() instanceof PacMap) {
            this.asrListener.onInit((PacMap) taskInfo.getObject());
        } else {
            this.asrListener.onInit(null);
        }
    }

    private void handleOtherTask(TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        if (taskId == 2) {
            this.asrListener.onAudioStart();
        } else if (taskId != 4) {
            if (taskId != 7) {
                switch (taskId) {
                    case 10:
                        if (taskInfo.getObject() instanceof PacMap) {
                            this.asrListener.onIntermediateResults((PacMap) taskInfo.getObject());
                            return;
                        } else {
                            this.asrListener.onIntermediateResults(null);
                            return;
                        }
                    case 11:
                        this.asrListener.onEnd();
                        return;
                    case 12:
                        if (taskInfo.getObject() instanceof PacMap) {
                            this.asrListener.onEvent(taskInfo.getArgOne(), (PacMap) taskInfo.getObject());
                            return;
                        } else {
                            this.asrListener.onEvent(taskInfo.getArgOne(), null);
                            return;
                        }
                    default:
                        return;
                }
            } else {
                this.asrListener.onAudioEnd();
            }
        } else if (taskInfo.getObject() instanceof Float) {
            this.asrListener.onRmsChanged(((Float) taskInfo.getObject()).floatValue());
        }
    }
}
