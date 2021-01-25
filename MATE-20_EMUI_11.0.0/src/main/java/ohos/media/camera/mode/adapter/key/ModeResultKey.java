package ohos.media.camera.mode.adapter.key;

import ohos.media.camera.params.ResultKey;

public final class ModeResultKey {
    public static final ResultKey.Key<Integer> EXPOSURE_HINT_RESULT = new ResultKey.Key<>("ohos.camera.exposureHintResult", Integer.class);
    public static final ResultKey.Key<Integer> SMART_CAPTURE_RESULT = new ResultKey.Key<>("ohos.camera.smartCaptureResult", Integer.class);

    private ModeResultKey() {
    }
}
