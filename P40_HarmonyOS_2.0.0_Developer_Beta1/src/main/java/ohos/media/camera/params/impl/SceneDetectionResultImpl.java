package ohos.media.camera.params.impl;

import java.util.Map;
import ohos.media.camera.params.SceneDetectionResult;

public final class SceneDetectionResultImpl implements SceneDetectionResult {
    private final Map<Integer, Float> scenes;
    private final int state;

    public SceneDetectionResultImpl(int i, Map<Integer, Float> map) {
        this.state = i;
        this.scenes = map;
    }

    @Override // ohos.media.camera.params.SceneDetectionResult
    public int getState() {
        return this.state;
    }

    @Override // ohos.media.camera.params.SceneDetectionResult
    public Map<Integer, Float> getScenes() {
        return this.scenes;
    }
}
