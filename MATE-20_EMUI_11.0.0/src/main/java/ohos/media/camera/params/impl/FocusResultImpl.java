package ohos.media.camera.params.impl;

import ohos.media.camera.params.FocusResult;

public final class FocusResultImpl implements FocusResult {
    private final int state;

    public FocusResultImpl(int i) {
        this.state = i;
    }

    @Override // ohos.media.camera.params.FocusResult
    public int getState() {
        return this.state;
    }
}
