package ohos.media.camera.mode.impl;

import ohos.media.camera.mode.BurstResult;

public final class BurstResultImpl implements BurstResult {
    private final int state;

    public BurstResultImpl(int i) {
        this.state = i;
    }

    @Override // ohos.media.camera.mode.BurstResult
    public int getState() {
        return this.state;
    }
}
