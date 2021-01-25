package ohos.media.camera.mode;

import ohos.media.camera.mode.BurstResult;
import ohos.media.camera.mode.PreviewResult;
import ohos.media.camera.mode.RecordingResult;
import ohos.media.camera.mode.TakePictureResult;
import ohos.media.camera.params.FaceDetectionResult;
import ohos.media.camera.params.FocusResult;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.SceneDetectionResult;

public abstract class ActionStateCallback {
    public void onBurst(Mode mode, @BurstResult.State int i, BurstResult burstResult) {
    }

    public void onFaceDetection(Mode mode, @FaceDetectionResult.State int i, FaceDetectionResult faceDetectionResult) {
    }

    public void onFocus(Mode mode, @FocusResult.State int i, FocusResult focusResult) {
    }

    public void onParameters(Mode mode, @ParametersResult.State int i, ParametersResult parametersResult) {
    }

    public void onPreview(Mode mode, @PreviewResult.State int i, PreviewResult previewResult) {
    }

    public void onRecording(Mode mode, @RecordingResult.State int i, RecordingResult recordingResult) {
    }

    public void onSceneDetection(Mode mode, @SceneDetectionResult.State int i, SceneDetectionResult sceneDetectionResult) {
    }

    public void onTakePicture(Mode mode, @TakePictureResult.State int i, TakePictureResult takePictureResult) {
    }
}
