package ohos.media.camera.params;

import java.util.Objects;
import ohos.agp.utils.Point;

public final class FaceLandmark {
    private final Point position;
    private final int type;

    public @interface FaceLandmarkType {
        public static final int LEFT_EYE = 0;
        public static final int MOUTH_CENTER = 2;
        public static final int RIGHT_EYE = 1;
        public static final int TYPE_UNKNOWN = -1;
    }

    public FaceLandmark(@FaceLandmarkType int i, Point point) {
        this.type = i;
        this.position = point;
    }

    @FaceLandmarkType
    public int getType() {
        return this.type;
    }

    public Point getPosition() {
        return this.position;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FaceLandmark)) {
            return false;
        }
        FaceLandmark faceLandmark = (FaceLandmark) obj;
        return this.type == faceLandmark.type && Objects.equals(this.position, faceLandmark.position);
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.type), this.position);
    }
}
