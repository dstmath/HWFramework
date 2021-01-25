package ohos.media.camera.params.impl;

import ohos.media.camera.params.AfResult;

public class AfResultImpl implements AfResult {
    private static final int INVALID_AF_STATE = -1;
    private final Integer afState;

    public AfResultImpl(Integer num) {
        this.afState = num;
    }

    public static AfResultImpl getDefault() {
        return new AfResultImpl(-1);
    }

    @Override // ohos.media.camera.params.AfResult
    public int getState() {
        Integer num = this.afState;
        if (num == null) {
            return -1;
        }
        return num.intValue();
    }
}
