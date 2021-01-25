package ohos.media.camera.mode.impl;

import ohos.media.camera.mode.PreviewResult;

public final class PreviewResultImpl implements PreviewResult {
    private final int state;

    public PreviewResultImpl(int i) {
        this.state = i;
    }

    @Override // ohos.media.camera.mode.PreviewResult
    public int getState() {
        return this.state;
    }
}
