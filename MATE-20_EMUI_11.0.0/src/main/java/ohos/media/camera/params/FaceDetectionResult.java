package ohos.media.camera.params;

import java.util.Map;

public interface FaceDetectionResult {

    public @interface State {
        public static final int ERROR_UNKNOWN = -1;
        public static final int FACE_DETECTED = 1;
        public static final int FACE_SMILE_DETECTED = 2;
    }

    Face[] getFaces();

    Map<Integer, Integer> getSmiles();

    @State
    int getState();
}
