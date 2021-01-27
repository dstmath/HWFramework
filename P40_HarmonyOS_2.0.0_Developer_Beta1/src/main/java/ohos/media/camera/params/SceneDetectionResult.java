package ohos.media.camera.params;

import java.util.Map;

public interface SceneDetectionResult {

    public @interface State {
        public static final int ERROR_UNKNOWN = -1;
        public static final int SCENE_DETECTED = 1;
    }

    Map<Integer, Float> getScenes();

    @State
    int getState();
}
