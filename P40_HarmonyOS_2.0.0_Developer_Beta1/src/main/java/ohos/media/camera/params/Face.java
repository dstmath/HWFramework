package ohos.media.camera.params;

import java.util.List;
import java.util.Objects;
import ohos.agp.utils.Rect;

public final class Face {
    private static final FaceLandmark[] EMPTY_ARRAY = new FaceLandmark[0];
    public static final int FACE_PROBABILITY_UNSUPPORTED = -1;
    private final Rect faceRect;
    private final int id;
    private final List<FaceLandmark> landmarks;
    private final float probability;

    public Face(int i, Rect rect, float f, List<FaceLandmark> list) {
        this.id = i;
        this.probability = f;
        this.faceRect = rect;
        this.landmarks = list;
    }

    public Face(int i, Rect rect, float f) {
        this.id = i;
        this.probability = f;
        this.faceRect = rect;
        this.landmarks = null;
    }

    public int getId() {
        return this.id;
    }

    public float getProbability() {
        return this.probability;
    }

    public Rect getFaceRect() {
        return this.faceRect;
    }

    public FaceLandmark[] getLandmarks() {
        List<FaceLandmark> list = this.landmarks;
        if (list == null) {
            return EMPTY_ARRAY;
        }
        return (FaceLandmark[]) list.toArray(new FaceLandmark[0]);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Face)) {
            return false;
        }
        Face face = (Face) obj;
        return this.id == face.id && Float.compare(face.probability, this.probability) == 0 && Objects.equals(this.faceRect, face.faceRect) && Objects.equals(this.landmarks, face.landmarks);
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.id), Float.valueOf(this.probability), this.faceRect, this.landmarks);
    }
}
