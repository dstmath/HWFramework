package ohos.media.camera.device;

import ohos.media.camera.device.FrameResult;

public abstract class FrameStateCallback {
    public void onCaptureTriggerFinished(Camera camera, int i, long j) {
    }

    public void onCaptureTriggerInterrupted(Camera camera, int i) {
    }

    public void onCaptureTriggerStarted(Camera camera, int i, long j) {
    }

    public void onFrameError(Camera camera, FrameConfig frameConfig, @FrameResult.State int i, FrameResult frameResult) {
    }

    public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
    }

    public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
    }

    public void onFrameStarted(Camera camera, FrameConfig frameConfig, long j, long j2) {
    }
}
