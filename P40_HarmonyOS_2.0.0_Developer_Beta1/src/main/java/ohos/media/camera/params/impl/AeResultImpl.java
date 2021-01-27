package ohos.media.camera.params.impl;

import ohos.media.camera.params.AeResult;

public class AeResultImpl implements AeResult {
    private static final int INVALID_AE_STATE = -1;
    private final Integer aeState;

    public AeResultImpl(Integer num) {
        this.aeState = num;
    }

    public static AeResultImpl getDefault() {
        return new AeResultImpl(-1);
    }

    @Override // ohos.media.camera.params.AeResult
    public int getState() {
        Integer num = this.aeState;
        if (num == null) {
            return -1;
        }
        return num.intValue();
    }
}
