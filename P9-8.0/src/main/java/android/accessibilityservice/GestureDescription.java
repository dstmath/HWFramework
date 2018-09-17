package android.accessibilityservice;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

public final class GestureDescription {
    private static final long MAX_GESTURE_DURATION_MS = 60000;
    private static final int MAX_STROKE_COUNT = 10;
    private final List<StrokeDescription> mStrokes;
    private final float[] mTempPos;

    public static class Builder {
        private final List<StrokeDescription> mStrokes = new ArrayList();

        public Builder addStroke(StrokeDescription strokeDescription) {
            if (this.mStrokes.size() >= 10) {
                throw new IllegalStateException("Attempting to add too many strokes to a gesture");
            }
            this.mStrokes.add(strokeDescription);
            if (GestureDescription.getTotalDuration(this.mStrokes) <= GestureDescription.MAX_GESTURE_DURATION_MS) {
                return this;
            }
            this.mStrokes.remove(strokeDescription);
            throw new IllegalStateException("Gesture would exceed maximum duration with new stroke");
        }

        public GestureDescription build() {
            if (this.mStrokes.size() != 0) {
                return new GestureDescription(this.mStrokes, null);
            }
            throw new IllegalStateException("Gestures must have at least one stroke");
        }
    }

    public static class GestureStep implements Parcelable {
        public static final Creator<GestureStep> CREATOR = new Creator<GestureStep>() {
            public GestureStep createFromParcel(Parcel in) {
                return new GestureStep(in);
            }

            public GestureStep[] newArray(int size) {
                return new GestureStep[size];
            }
        };
        public int numTouchPoints;
        public long timeSinceGestureStart;
        public TouchPoint[] touchPoints;

        public GestureStep(long timeSinceGestureStart, int numTouchPoints, TouchPoint[] touchPointsToCopy) {
            this.timeSinceGestureStart = timeSinceGestureStart;
            this.numTouchPoints = numTouchPoints;
            this.touchPoints = new TouchPoint[numTouchPoints];
            for (int i = 0; i < numTouchPoints; i++) {
                this.touchPoints[i] = new TouchPoint(touchPointsToCopy[i]);
            }
        }

        public GestureStep(Parcel parcel) {
            this.timeSinceGestureStart = parcel.readLong();
            Parcelable[] parcelables = parcel.readParcelableArray(TouchPoint.class.getClassLoader());
            this.numTouchPoints = parcelables == null ? 0 : parcelables.length;
            this.touchPoints = new TouchPoint[this.numTouchPoints];
            for (int i = 0; i < this.numTouchPoints; i++) {
                this.touchPoints[i] = (TouchPoint) parcelables[i];
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.timeSinceGestureStart);
            dest.writeParcelableArray(this.touchPoints, flags);
        }
    }

    public static class MotionEventGenerator {
        private static TouchPoint[] sCurrentTouchPoints;

        public static List<GestureStep> getGestureStepsFromGestureDescription(GestureDescription description, int sampleTimeMs) {
            List<GestureStep> gestureSteps = new ArrayList();
            TouchPoint[] currentTouchPoints = getCurrentTouchPoints(description.getStrokeCount());
            int currentTouchPointSize = 0;
            long timeSinceGestureStart = 0;
            long nextKeyPointTime = description.getNextKeyPointAtLeast(0);
            while (nextKeyPointTime >= 0) {
                if (currentTouchPointSize == 0) {
                    timeSinceGestureStart = nextKeyPointTime;
                } else {
                    timeSinceGestureStart = Math.min(nextKeyPointTime, ((long) sampleTimeMs) + timeSinceGestureStart);
                }
                currentTouchPointSize = description.getPointsForTime(timeSinceGestureStart, currentTouchPoints);
                gestureSteps.add(new GestureStep(timeSinceGestureStart, currentTouchPointSize, currentTouchPoints));
                nextKeyPointTime = description.getNextKeyPointAtLeast(1 + timeSinceGestureStart);
            }
            return gestureSteps;
        }

        private static TouchPoint[] getCurrentTouchPoints(int requiredCapacity) {
            if (sCurrentTouchPoints == null || sCurrentTouchPoints.length < requiredCapacity) {
                sCurrentTouchPoints = new TouchPoint[requiredCapacity];
                for (int i = 0; i < requiredCapacity; i++) {
                    sCurrentTouchPoints[i] = new TouchPoint();
                }
            }
            return sCurrentTouchPoints;
        }
    }

    public static class StrokeDescription {
        private static final int INVALID_STROKE_ID = -1;
        static int sIdCounter;
        boolean mContinued;
        int mContinuedStrokeId;
        long mEndTime;
        int mId;
        Path mPath;
        private PathMeasure mPathMeasure;
        long mStartTime;
        float[] mTapLocation;
        private float mTimeToLengthConversion;

        public StrokeDescription(Path path, long startTime, long duration) {
            this(path, startTime, duration, false);
        }

        public StrokeDescription(Path path, long startTime, long duration, boolean willContinue) {
            this.mContinuedStrokeId = -1;
            this.mContinued = willContinue;
            Preconditions.checkArgument(duration > 0, "Duration must be positive");
            Preconditions.checkArgument(startTime >= 0, "Start time must not be negative");
            Preconditions.checkArgument(path.isEmpty() ^ 1, "Path is empty");
            RectF bounds = new RectF();
            path.computeBounds(bounds, false);
            boolean z = (bounds.bottom < TonemapCurve.LEVEL_BLACK || bounds.top < TonemapCurve.LEVEL_BLACK || bounds.right < TonemapCurve.LEVEL_BLACK) ? false : bounds.left >= TonemapCurve.LEVEL_BLACK;
            Preconditions.checkArgument(z, "Path bounds must not be negative");
            this.mPath = new Path(path);
            this.mPathMeasure = new PathMeasure(path, false);
            if (this.mPathMeasure.getLength() == TonemapCurve.LEVEL_BLACK) {
                Path tempPath = new Path(path);
                tempPath.lineTo(-1.0f, -1.0f);
                this.mTapLocation = new float[2];
                new PathMeasure(tempPath, false).getPosTan(TonemapCurve.LEVEL_BLACK, this.mTapLocation, null);
            }
            if (this.mPathMeasure.nextContour()) {
                throw new IllegalArgumentException("Path has more than one contour");
            }
            this.mPathMeasure.setPath(this.mPath, false);
            this.mStartTime = startTime;
            this.mEndTime = startTime + duration;
            this.mTimeToLengthConversion = getLength() / ((float) duration);
            int i = sIdCounter;
            sIdCounter = i + 1;
            this.mId = i;
        }

        public Path getPath() {
            return new Path(this.mPath);
        }

        public long getStartTime() {
            return this.mStartTime;
        }

        public long getDuration() {
            return this.mEndTime - this.mStartTime;
        }

        public int getId() {
            return this.mId;
        }

        public StrokeDescription continueStroke(Path path, long startTime, long duration, boolean willContinue) {
            if (this.mContinued) {
                StrokeDescription strokeDescription = new StrokeDescription(path, startTime, duration, willContinue);
                strokeDescription.mContinuedStrokeId = this.mId;
                return strokeDescription;
            }
            throw new IllegalStateException("Only strokes marked willContinue can be continued");
        }

        public boolean willContinue() {
            return this.mContinued;
        }

        public int getContinuedStrokeId() {
            return this.mContinuedStrokeId;
        }

        float getLength() {
            return this.mPathMeasure.getLength();
        }

        boolean getPosForTime(long time, float[] pos) {
            if (this.mTapLocation != null) {
                pos[0] = this.mTapLocation[0];
                pos[1] = this.mTapLocation[1];
                return true;
            } else if (time == this.mEndTime) {
                return this.mPathMeasure.getPosTan(getLength(), pos, null);
            } else {
                return this.mPathMeasure.getPosTan(this.mTimeToLengthConversion * ((float) (time - this.mStartTime)), pos, null);
            }
        }

        boolean hasPointForTime(long time) {
            return time >= this.mStartTime && time <= this.mEndTime;
        }
    }

    public static class TouchPoint implements Parcelable {
        public static final Creator<TouchPoint> CREATOR = new Creator<TouchPoint>() {
            public TouchPoint createFromParcel(Parcel in) {
                return new TouchPoint(in);
            }

            public TouchPoint[] newArray(int size) {
                return new TouchPoint[size];
            }
        };
        private static final int FLAG_IS_END_OF_PATH = 2;
        private static final int FLAG_IS_START_OF_PATH = 1;
        public int mContinuedStrokeId;
        public boolean mIsEndOfPath;
        public boolean mIsStartOfPath;
        public int mStrokeId;
        public float mX;
        public float mY;

        public TouchPoint(TouchPoint pointToCopy) {
            copyFrom(pointToCopy);
        }

        public TouchPoint(Parcel parcel) {
            boolean z;
            boolean z2 = true;
            this.mStrokeId = parcel.readInt();
            this.mContinuedStrokeId = parcel.readInt();
            int startEnd = parcel.readInt();
            if ((startEnd & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsStartOfPath = z;
            if ((startEnd & 2) == 0) {
                z2 = false;
            }
            this.mIsEndOfPath = z2;
            this.mX = parcel.readFloat();
            this.mY = parcel.readFloat();
        }

        public void copyFrom(TouchPoint other) {
            this.mStrokeId = other.mStrokeId;
            this.mContinuedStrokeId = other.mContinuedStrokeId;
            this.mIsStartOfPath = other.mIsStartOfPath;
            this.mIsEndOfPath = other.mIsEndOfPath;
            this.mX = other.mX;
            this.mY = other.mY;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mStrokeId);
            dest.writeInt(this.mContinuedStrokeId);
            dest.writeInt((this.mIsStartOfPath ? 1 : 0) | (this.mIsEndOfPath ? 2 : 0));
            dest.writeFloat(this.mX);
            dest.writeFloat(this.mY);
        }
    }

    /* synthetic */ GestureDescription(List strokes, GestureDescription -this1) {
        this(strokes);
    }

    public static int getMaxStrokeCount() {
        return 10;
    }

    public static long getMaxGestureDuration() {
        return MAX_GESTURE_DURATION_MS;
    }

    private GestureDescription() {
        this.mStrokes = new ArrayList();
        this.mTempPos = new float[2];
    }

    private GestureDescription(List<StrokeDescription> strokes) {
        this.mStrokes = new ArrayList();
        this.mTempPos = new float[2];
        this.mStrokes.addAll(strokes);
    }

    public int getStrokeCount() {
        return this.mStrokes.size();
    }

    public StrokeDescription getStroke(int index) {
        return (StrokeDescription) this.mStrokes.get(index);
    }

    private long getNextKeyPointAtLeast(long offset) {
        long nextKeyPoint = Long.MAX_VALUE;
        for (int i = 0; i < this.mStrokes.size(); i++) {
            long thisStartTime = ((StrokeDescription) this.mStrokes.get(i)).mStartTime;
            if (thisStartTime < nextKeyPoint && thisStartTime >= offset) {
                nextKeyPoint = thisStartTime;
            }
            long thisEndTime = ((StrokeDescription) this.mStrokes.get(i)).mEndTime;
            if (thisEndTime < nextKeyPoint && thisEndTime >= offset) {
                nextKeyPoint = thisEndTime;
            }
        }
        return nextKeyPoint == Long.MAX_VALUE ? -1 : nextKeyPoint;
    }

    private int getPointsForTime(long time, TouchPoint[] touchPoints) {
        int numPointsFound = 0;
        for (int i = 0; i < this.mStrokes.size(); i++) {
            StrokeDescription strokeDescription = (StrokeDescription) this.mStrokes.get(i);
            if (strokeDescription.hasPointForTime(time)) {
                touchPoints[numPointsFound].mStrokeId = strokeDescription.getId();
                touchPoints[numPointsFound].mContinuedStrokeId = strokeDescription.getContinuedStrokeId();
                TouchPoint touchPoint = touchPoints[numPointsFound];
                boolean z = strokeDescription.getContinuedStrokeId() < 0 ? time == strokeDescription.mStartTime : false;
                touchPoint.mIsStartOfPath = z;
                touchPoint = touchPoints[numPointsFound];
                z = !strokeDescription.willContinue() ? time == strokeDescription.mEndTime : false;
                touchPoint.mIsEndOfPath = z;
                strokeDescription.getPosForTime(time, this.mTempPos);
                touchPoints[numPointsFound].mX = (float) Math.round(this.mTempPos[0]);
                touchPoints[numPointsFound].mY = (float) Math.round(this.mTempPos[1]);
                numPointsFound++;
            }
        }
        return numPointsFound;
    }

    private static long getTotalDuration(List<StrokeDescription> paths) {
        long latestEnd = Long.MIN_VALUE;
        for (int i = 0; i < paths.size(); i++) {
            latestEnd = Math.max(latestEnd, ((StrokeDescription) paths.get(i)).mEndTime);
        }
        return Math.max(latestEnd, 0);
    }
}
