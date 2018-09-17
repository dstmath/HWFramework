package android.hardware.camera2.params;

import android.graphics.Point;
import android.graphics.Rect;

public final class Face {
    public static final int ID_UNSUPPORTED = -1;
    public static final int SCORE_MAX = 100;
    public static final int SCORE_MIN = 1;
    private final Rect mBounds;
    private final int mId;
    private final Point mLeftEye;
    private final Point mMouth;
    private final Point mRightEye;
    private final int mScore;

    public Face(Rect bounds, int score, int id, Point leftEyePosition, Point rightEyePosition, Point mouthPosition) {
        checkNotNull("bounds", bounds);
        if (score < SCORE_MIN || score > SCORE_MAX) {
            throw new IllegalArgumentException("Confidence out of range");
        } else if (id >= 0 || id == ID_UNSUPPORTED) {
            if (id == ID_UNSUPPORTED) {
                checkNull("leftEyePosition", leftEyePosition);
                checkNull("rightEyePosition", rightEyePosition);
                checkNull("mouthPosition", mouthPosition);
            }
            this.mBounds = bounds;
            this.mScore = score;
            this.mId = id;
            this.mLeftEye = leftEyePosition;
            this.mRightEye = rightEyePosition;
            this.mMouth = mouthPosition;
        } else {
            throw new IllegalArgumentException("Id out of range");
        }
    }

    public Face(Rect bounds, int score) {
        this(bounds, score, ID_UNSUPPORTED, null, null, null);
    }

    public Rect getBounds() {
        return this.mBounds;
    }

    public int getScore() {
        return this.mScore;
    }

    public int getId() {
        return this.mId;
    }

    public Point getLeftEyePosition() {
        return this.mLeftEye;
    }

    public Point getRightEyePosition() {
        return this.mRightEye;
    }

    public Point getMouthPosition() {
        return this.mMouth;
    }

    public String toString() {
        return String.format("{ bounds: %s, score: %s, id: %d, leftEyePosition: %s, rightEyePosition: %s, mouthPosition: %s }", new Object[]{this.mBounds, Integer.valueOf(this.mScore), Integer.valueOf(this.mId), this.mLeftEye, this.mRightEye, this.mMouth});
    }

    private static void checkNotNull(String name, Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " was required, but it was null");
        }
    }

    private static void checkNull(String name, Object obj) {
        if (obj != null) {
            throw new IllegalArgumentException(name + " was required to be null, but it wasn't");
        }
    }
}
