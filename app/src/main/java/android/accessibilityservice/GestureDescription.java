package android.accessibilityservice;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.net.LinkQualityInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import java.util.ArrayList;
import java.util.List;

public final class GestureDescription {
    private static final long MAX_GESTURE_DURATION_MS = 60000;
    private static final int MAX_STROKE_COUNT = 10;
    private final List<StrokeDescription> mStrokes;
    private final float[] mTempPos;

    public static class Builder {
        private final List<StrokeDescription> mStrokes;

        public Builder() {
            this.mStrokes = new ArrayList();
        }

        public Builder addStroke(StrokeDescription strokeDescription) {
            if (this.mStrokes.size() >= GestureDescription.MAX_STROKE_COUNT) {
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
        public static final Creator<GestureStep> CREATOR = null;
        public int numTouchPoints;
        public long timeSinceGestureStart;
        public TouchPoint[] touchPoints;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.accessibilityservice.GestureDescription.GestureStep.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.accessibilityservice.GestureDescription.GestureStep.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accessibilityservice.GestureDescription.GestureStep.<clinit>():void");
        }

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
        private static final int EVENT_BUTTON_STATE = 0;
        private static final int EVENT_DEVICE_ID = 0;
        private static final int EVENT_EDGE_FLAGS = 0;
        private static final int EVENT_FLAGS = 0;
        private static final int EVENT_META_STATE = 0;
        private static final int EVENT_SOURCE = 4098;
        private static final float EVENT_X_PRECISION = 1.0f;
        private static final float EVENT_Y_PRECISION = 1.0f;
        private static TouchPoint[] sCurrentTouchPoints;
        private static TouchPoint[] sLastTouchPoints;
        private static PointerCoords[] sPointerCoords;
        private static PointerProperties[] sPointerProps;

        public MotionEventGenerator() {
        }

        static List<GestureStep> getGestureStepsFromGestureDescription(GestureDescription description, int sampleTimeMs) {
            List<GestureStep> gestureSteps = new ArrayList();
            TouchPoint[] currentTouchPoints = getCurrentTouchPoints(description.getStrokeCount());
            int currentTouchPointSize = EVENT_META_STATE;
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

        public static List<MotionEvent> getMotionEventsFromGestureSteps(List<GestureStep> steps) {
            List<MotionEvent> motionEvents = new ArrayList();
            int lastTouchPointSize = EVENT_META_STATE;
            for (int i = EVENT_META_STATE; i < steps.size(); i++) {
                GestureStep step = (GestureStep) steps.get(i);
                int currentTouchPointSize = step.numTouchPoints;
                TouchPoint[] lastTouchPoints = getLastTouchPoints(Math.max(lastTouchPointSize, currentTouchPointSize));
                appendMoveEventIfNeeded(motionEvents, lastTouchPoints, lastTouchPointSize, step.touchPoints, currentTouchPointSize, step.timeSinceGestureStart);
                lastTouchPointSize = appendDownEvents(motionEvents, lastTouchPoints, appendUpEvents(motionEvents, lastTouchPoints, lastTouchPointSize, step.touchPoints, currentTouchPointSize, step.timeSinceGestureStart), step.touchPoints, currentTouchPointSize, step.timeSinceGestureStart);
            }
            return motionEvents;
        }

        private static TouchPoint[] getCurrentTouchPoints(int requiredCapacity) {
            if (sCurrentTouchPoints == null || sCurrentTouchPoints.length < requiredCapacity) {
                sCurrentTouchPoints = new TouchPoint[requiredCapacity];
                for (int i = EVENT_META_STATE; i < requiredCapacity; i++) {
                    sCurrentTouchPoints[i] = new TouchPoint();
                }
            }
            return sCurrentTouchPoints;
        }

        private static TouchPoint[] getLastTouchPoints(int requiredCapacity) {
            if (sLastTouchPoints == null || sLastTouchPoints.length < requiredCapacity) {
                sLastTouchPoints = new TouchPoint[requiredCapacity];
                for (int i = EVENT_META_STATE; i < requiredCapacity; i++) {
                    sLastTouchPoints[i] = new TouchPoint();
                }
            }
            return sLastTouchPoints;
        }

        private static PointerCoords[] getPointerCoords(int requiredCapacity) {
            if (sPointerCoords == null || sPointerCoords.length < requiredCapacity) {
                sPointerCoords = new PointerCoords[requiredCapacity];
                for (int i = EVENT_META_STATE; i < requiredCapacity; i++) {
                    sPointerCoords[i] = new PointerCoords();
                }
            }
            return sPointerCoords;
        }

        private static PointerProperties[] getPointerProps(int requiredCapacity) {
            if (sPointerProps == null || sPointerProps.length < requiredCapacity) {
                sPointerProps = new PointerProperties[requiredCapacity];
                for (int i = EVENT_META_STATE; i < requiredCapacity; i++) {
                    sPointerProps[i] = new PointerProperties();
                }
            }
            return sPointerProps;
        }

        private static void appendMoveEventIfNeeded(List<MotionEvent> motionEvents, TouchPoint[] lastTouchPoints, int lastTouchPointsSize, TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
            int moveFound = EVENT_META_STATE;
            for (int i = EVENT_META_STATE; i < currentTouchPointsSize; i++) {
                int lastPointsIndex = findPointByPathIndex(lastTouchPoints, lastTouchPointsSize, currentTouchPoints[i].mPathIndex);
                if (lastPointsIndex >= 0) {
                    int i2 = lastTouchPoints[lastPointsIndex].mX == currentTouchPoints[i].mX ? lastTouchPoints[lastPointsIndex].mY != currentTouchPoints[i].mY ? 1 : EVENT_META_STATE : 1;
                    moveFound |= i2;
                    lastTouchPoints[lastPointsIndex].copyFrom(currentTouchPoints[i]);
                }
            }
            if (moveFound != 0) {
                motionEvents.add(obtainMotionEvent(((MotionEvent) motionEvents.get(motionEvents.size() - 1)).getDownTime(), currentTime, 2, lastTouchPoints, lastTouchPointsSize));
            }
        }

        private static int appendUpEvents(List<MotionEvent> motionEvents, TouchPoint[] lastTouchPoints, int lastTouchPointsSize, TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
            for (int i = EVENT_META_STATE; i < currentTouchPointsSize; i++) {
                if (currentTouchPoints[i].mIsEndOfPath) {
                    int indexOfUpEvent = findPointByPathIndex(lastTouchPoints, lastTouchPointsSize, currentTouchPoints[i].mPathIndex);
                    if (indexOfUpEvent >= 0) {
                        int action;
                        long downTime = ((MotionEvent) motionEvents.get(motionEvents.size() - 1)).getDownTime();
                        if (lastTouchPointsSize == 1) {
                            action = 1;
                        } else {
                            action = 6;
                        }
                        motionEvents.add(obtainMotionEvent(downTime, currentTime, action | (indexOfUpEvent << 8), lastTouchPoints, lastTouchPointsSize));
                        for (int j = indexOfUpEvent; j < lastTouchPointsSize - 1; j++) {
                            lastTouchPoints[j].copyFrom(lastTouchPoints[j + 1]);
                        }
                        lastTouchPointsSize--;
                    }
                }
            }
            return lastTouchPointsSize;
        }

        private static int appendDownEvents(List<MotionEvent> motionEvents, TouchPoint[] lastTouchPoints, int lastTouchPointsSize, TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
            int i = EVENT_META_STATE;
            int lastTouchPointsSize2 = lastTouchPointsSize;
            while (i < currentTouchPointsSize) {
                if (currentTouchPoints[i].mIsStartOfPath) {
                    int action;
                    long downTime;
                    lastTouchPointsSize = lastTouchPointsSize2 + 1;
                    lastTouchPoints[lastTouchPointsSize2].copyFrom(currentTouchPoints[i]);
                    if (lastTouchPointsSize == 1) {
                        action = EVENT_META_STATE;
                    } else {
                        action = 5;
                    }
                    if (action == 0) {
                        downTime = currentTime;
                    } else {
                        downTime = ((MotionEvent) motionEvents.get(motionEvents.size() - 1)).getDownTime();
                    }
                    motionEvents.add(obtainMotionEvent(downTime, currentTime, action | (i << 8), lastTouchPoints, lastTouchPointsSize));
                } else {
                    lastTouchPointsSize = lastTouchPointsSize2;
                }
                i++;
                lastTouchPointsSize2 = lastTouchPointsSize;
            }
            return lastTouchPointsSize2;
        }

        private static MotionEvent obtainMotionEvent(long downTime, long eventTime, int action, TouchPoint[] touchPoints, int touchPointsSize) {
            PointerCoords[] pointerCoords = getPointerCoords(touchPointsSize);
            PointerProperties[] pointerProperties = getPointerProps(touchPointsSize);
            for (int i = EVENT_META_STATE; i < touchPointsSize; i++) {
                pointerProperties[i].id = touchPoints[i].mPathIndex;
                pointerProperties[i].toolType = EVENT_META_STATE;
                pointerCoords[i].clear();
                pointerCoords[i].pressure = EVENT_Y_PRECISION;
                pointerCoords[i].size = EVENT_Y_PRECISION;
                pointerCoords[i].x = touchPoints[i].mX;
                pointerCoords[i].y = touchPoints[i].mY;
            }
            return MotionEvent.obtain(downTime, eventTime, action, touchPointsSize, pointerProperties, pointerCoords, EVENT_META_STATE, EVENT_META_STATE, EVENT_Y_PRECISION, EVENT_Y_PRECISION, EVENT_META_STATE, EVENT_META_STATE, EVENT_SOURCE, EVENT_META_STATE);
        }

        private static int findPointByPathIndex(TouchPoint[] touchPoints, int touchPointsSize, int pathIndex) {
            for (int i = EVENT_META_STATE; i < touchPointsSize; i++) {
                if (touchPoints[i].mPathIndex == pathIndex) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static class StrokeDescription {
        long mEndTime;
        Path mPath;
        private PathMeasure mPathMeasure;
        long mStartTime;
        float[] mTapLocation;
        private float mTimeToLengthConversion;

        public StrokeDescription(Path path, long startTime, long duration) {
            if (duration <= 0) {
                throw new IllegalArgumentException("Duration must be positive");
            } else if (startTime < 0) {
                throw new IllegalArgumentException("Start time must not be negative");
            } else {
                RectF bounds = new RectF();
                path.computeBounds(bounds, false);
                if (bounds.bottom < 0.0f || bounds.top < 0.0f || bounds.right < 0.0f || bounds.left < 0.0f) {
                    throw new IllegalArgumentException("Path bounds must not be negative");
                } else if (path.isEmpty()) {
                    throw new IllegalArgumentException("Path is empty");
                } else {
                    this.mPath = new Path(path);
                    this.mPathMeasure = new PathMeasure(path, false);
                    if (this.mPathMeasure.getLength() == 0.0f) {
                        Path tempPath = new Path(path);
                        tempPath.lineTo(ScaledLayoutParams.SCALE_UNSPECIFIED, ScaledLayoutParams.SCALE_UNSPECIFIED);
                        this.mTapLocation = new float[2];
                        new PathMeasure(tempPath, false).getPosTan(0.0f, this.mTapLocation, null);
                    }
                    if (this.mPathMeasure.nextContour()) {
                        throw new IllegalArgumentException("Path has more than one contour");
                    }
                    this.mPathMeasure.setPath(this.mPath, false);
                    this.mStartTime = startTime;
                    this.mEndTime = startTime + duration;
                    this.mTimeToLengthConversion = getLength() / ((float) duration);
                }
            }
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
        public static final Creator<TouchPoint> CREATOR = null;
        private static final int FLAG_IS_END_OF_PATH = 2;
        private static final int FLAG_IS_START_OF_PATH = 1;
        boolean mIsEndOfPath;
        boolean mIsStartOfPath;
        int mPathIndex;
        float mX;
        float mY;

        /* renamed from: android.accessibilityservice.GestureDescription.TouchPoint.1 */
        static class AnonymousClass1 implements Creator<TouchPoint> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m4createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public TouchPoint createFromParcel(Parcel in) {
                return new TouchPoint(in);
            }

            public /* bridge */ /* synthetic */ Object[] m5newArray(int size) {
                return newArray(size);
            }

            public TouchPoint[] newArray(int size) {
                return new TouchPoint[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.accessibilityservice.GestureDescription.TouchPoint.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.accessibilityservice.GestureDescription.TouchPoint.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accessibilityservice.GestureDescription.TouchPoint.<clinit>():void");
        }

        public TouchPoint() {
        }

        public TouchPoint(TouchPoint pointToCopy) {
            copyFrom(pointToCopy);
        }

        public TouchPoint(Parcel parcel) {
            boolean z;
            boolean z2 = true;
            this.mPathIndex = parcel.readInt();
            int startEnd = parcel.readInt();
            if ((startEnd & FLAG_IS_START_OF_PATH) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsStartOfPath = z;
            if ((startEnd & FLAG_IS_END_OF_PATH) == 0) {
                z2 = false;
            }
            this.mIsEndOfPath = z2;
            this.mX = parcel.readFloat();
            this.mY = parcel.readFloat();
        }

        void copyFrom(TouchPoint other) {
            this.mPathIndex = other.mPathIndex;
            this.mIsStartOfPath = other.mIsStartOfPath;
            this.mIsEndOfPath = other.mIsEndOfPath;
            this.mX = other.mX;
            this.mY = other.mY;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mPathIndex);
            dest.writeInt((this.mIsStartOfPath ? FLAG_IS_START_OF_PATH : 0) | (this.mIsEndOfPath ? FLAG_IS_END_OF_PATH : 0));
            dest.writeFloat(this.mX);
            dest.writeFloat(this.mY);
        }
    }

    /* synthetic */ GestureDescription(List strokes, GestureDescription gestureDescription) {
        this(strokes);
    }

    public static int getMaxStrokeCount() {
        return MAX_STROKE_COUNT;
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
        long nextKeyPoint = LinkQualityInfo.UNKNOWN_LONG;
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
        return nextKeyPoint == LinkQualityInfo.UNKNOWN_LONG ? -1 : nextKeyPoint;
    }

    private int getPointsForTime(long time, TouchPoint[] touchPoints) {
        int numPointsFound = 0;
        for (int i = 0; i < this.mStrokes.size(); i++) {
            StrokeDescription strokeDescription = (StrokeDescription) this.mStrokes.get(i);
            if (strokeDescription.hasPointForTime(time)) {
                boolean z;
                touchPoints[numPointsFound].mPathIndex = i;
                TouchPoint touchPoint = touchPoints[numPointsFound];
                if (time == strokeDescription.mStartTime) {
                    z = true;
                } else {
                    z = false;
                }
                touchPoint.mIsStartOfPath = z;
                touchPoint = touchPoints[numPointsFound];
                if (time == strokeDescription.mEndTime) {
                    z = true;
                } else {
                    z = false;
                }
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
