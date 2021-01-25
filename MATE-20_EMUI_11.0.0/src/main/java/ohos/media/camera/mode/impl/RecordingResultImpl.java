package ohos.media.camera.mode.impl;

import ohos.media.camera.mode.RecordingResult;

public final class RecordingResultImpl implements RecordingResult {
    private final int state;

    public RecordingResultImpl(int i) {
        this.state = i;
    }

    @Override // ohos.media.camera.mode.RecordingResult
    public int getState() {
        return this.state;
    }
}
