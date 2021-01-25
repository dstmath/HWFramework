package ohos.media.camera.mode.impl;

import ohos.eventhandler.EventHandler;
import ohos.media.camera.mode.ActionStateCallback;
import ohos.media.camera.mode.BurstResult;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.PreviewResult;
import ohos.media.camera.mode.RecordingResult;
import ohos.media.camera.mode.TakePictureResult;
import ohos.media.camera.params.FaceDetectionResult;
import ohos.media.camera.params.FocusResult;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.SceneDetectionResult;

public class ActionStateCallbackImpl extends ActionStateCallback {
    private final ActionStateCallback callback;
    private final EventHandler handler;
    private final Mode mode;

    ActionStateCallbackImpl(Mode mode2, ActionStateCallback actionStateCallback, EventHandler eventHandler) {
        this.mode = mode2;
        this.callback = actionStateCallback;
        this.handler = eventHandler;
    }

    public static ActionStateCallbackImpl obtain(Mode mode2, ActionStateCallback actionStateCallback, EventHandler eventHandler) {
        return new ActionStateCallbackImpl(mode2, actionStateCallback, eventHandler);
    }

    public void onPreview(int i, PreviewResult previewResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, previewResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$NX3pi3VjbQh5yHSc0QtPKynhNGE */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ PreviewResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onPreview$0$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onPreview$0$ActionStateCallbackImpl(int i, PreviewResult previewResult) {
        this.callback.onPreview(this.mode, i, previewResult);
    }

    public void onTakePicture(int i, TakePictureResult takePictureResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, takePictureResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$Sk2PPmAYqtehy2fWxEVpANgT6o */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ TakePictureResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onTakePicture$1$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onTakePicture$1$ActionStateCallbackImpl(int i, TakePictureResult takePictureResult) {
        this.callback.onTakePicture(this.mode, i, takePictureResult);
    }

    public void onBurst(int i, BurstResult burstResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, burstResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$IkPpEUvCUW9oUjmd55bueN0i4yM */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ BurstResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onBurst$2$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onBurst$2$ActionStateCallbackImpl(int i, BurstResult burstResult) {
        this.callback.onBurst(this.mode, i, burstResult);
    }

    public void onRecording(int i, RecordingResult recordingResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, recordingResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$CLIoNWY38m7c3AUtJn4cHnosBG0 */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ RecordingResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onRecording$3$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onRecording$3$ActionStateCallbackImpl(int i, RecordingResult recordingResult) {
        this.callback.onRecording(this.mode, i, recordingResult);
    }

    public void onFocus(int i, FocusResult focusResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, focusResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$NOtdqBo0MqPhpLQR3HW2l1Bxnj0 */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ FocusResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onFocus$4$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onFocus$4$ActionStateCallbackImpl(int i, FocusResult focusResult) {
        this.callback.onFocus(this.mode, i, focusResult);
    }

    public void onFaceDetection(@FaceDetectionResult.State int i, FaceDetectionResult faceDetectionResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, faceDetectionResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$uwCp7AWxZteGY3ETvRArC_1GRTQ */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ FaceDetectionResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onFaceDetection$5$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onFaceDetection$5$ActionStateCallbackImpl(int i, FaceDetectionResult faceDetectionResult) {
        this.callback.onFaceDetection(this.mode, i, faceDetectionResult);
    }

    public void onSceneDetection(int i, SceneDetectionResult sceneDetectionResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, sceneDetectionResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$A948XlSn3ov7j6xInB0PlIn6N3I */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ SceneDetectionResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onSceneDetection$6$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onSceneDetection$6$ActionStateCallbackImpl(int i, SceneDetectionResult sceneDetectionResult) {
        this.callback.onSceneDetection(this.mode, i, sceneDetectionResult);
    }

    public void onParameters(int i, ParametersResult parametersResult) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, parametersResult) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionStateCallbackImpl$X0CL4gl29bJqVRUoz3JPzOqCmSo */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ ParametersResult f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionStateCallbackImpl.this.lambda$onParameters$7$ActionStateCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onParameters$7$ActionStateCallbackImpl(int i, ParametersResult parametersResult) {
        this.callback.onParameters(this.mode, i, parametersResult);
    }
}
