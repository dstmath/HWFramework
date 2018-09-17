package huawei.com.android.server.policy.fingersense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.GestureUtils;
import android.gesture.OrientedBoundingBox;
import android.gesture.Prediction;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.util.ArrayList;

public class CustomGestureDetector {
    private static final boolean DEBUG_LETTER = false;
    private static final boolean DEBUG_LINE = false;
    private static final boolean DEBUG_REGION = false;
    private static final float DEFAULT_LINE_GESTURE_STROKE_LANDSCAPE_ANGLE = 0.0f;
    private static final float DEFAULT_LINE_GESTURE_STROKE_PORTRAIT_ANGLE = 0.0f;
    private static final float DEFAULT_LINE_GESTURE_STROKE_STRAIGTHNESS = 4.0f;
    private static final float DEFAULT_MAX_LINE_GESTURE_STROKE_ANGLE_DEVIATION = 90.0f;
    private static final float DEFAULT_MIN_GESTURE_STROKE_LENGTH = 200.0f;
    private static final float DEFAULT_MIN_LETTER_GESTURE_STROKE_SQUARENESS = 0.15f;
    private static final float DEFAULT_MIN_LINE_GESTURE_STROKE_LENGTH = 500.0f;
    private static final float DEFAULT_MIN_PREDICTION_SCORE = 2.0f;
    private static final float DEFAULT_NON_ENCLOSED_REGION_DIAGONAL_THRESHOLD = 0.6f;
    private static final float DEFAULT_REGION_DIAGONAL_THRESHOLD = 0.25f;
    private static final float MAX_LINE_GESTURE_STROKE_SQUARENESS = 0.1f;
    private static final String TAG = "CustomGestureDetector";
    public static final float TOUCH_TOLERANCE = 3.0f;
    private boolean asyncNotifications;
    private Context mContext;
    private Gesture mCurrentGesture;
    private float mCurrentX;
    private float mCurrentY;
    private GestureLibrary mGestureLibraryMultipleOrientations;
    private GestureLibrary mGestureLibrarySingleOrientation;
    private final GesturePerformedRunnable mGesturePerformed;
    private final Handler mHandler;
    private boolean mIsGesturing;
    private boolean mIsListeningForGestures;
    private boolean mLetterGestureStarted;
    private float mLineGestureStrokeLandscapeAngle;
    private float mLineGestureStrokePortraitAngle;
    private float mLineGestureStrokeStraightness;
    private float mMaxLineGestureStrokeAngleDeviation;
    private float mMinGestureStrokeLength;
    private float mMinLetterGestureStrokeSquareness;
    private float mMinLineGestureStrokeLength;
    private float mMinPredictionScore;
    private float mNonEnclosedRegionDiagonalThreshold;
    private final OnGesturePerformedListener mOnGesturePerformedListener;
    private OrientationFix[] mOrientationFixes;
    private float mRegionDiagonalThreshold;
    private final ArrayList<GesturePoint> mStrokeBuffer;
    private float mTotalLength;

    private class GesturePerformedRunnable implements Runnable {
        boolean letterGestureStarted;

        /* synthetic */ GesturePerformedRunnable(CustomGestureDetector this$0, GesturePerformedRunnable -this1) {
            this();
        }

        private GesturePerformedRunnable() {
            this.letterGestureStarted = false;
        }

        public void run() {
            CustomGestureDetector.this.fireOnGesturePerformed(this);
            CustomGestureDetector.this.mCurrentGesture = null;
        }
    }

    public interface OnGesturePerformedListener {
        void onLetterGesture(String str, Gesture gesture, double d);

        void onLineGesture(String str, Gesture gesture, double d);

        void onRegionGesture(String str, Gesture gesture, double d);
    }

    public static class OrientationFix {
        public String multipleOrientationsGestureName;
        public String replaceGestureWith;
        public String singleOrientationGestureName;

        public OrientationFix(String gestureName, String singleOrientationGestureName, String replaceGestureWith) {
            this.multipleOrientationsGestureName = gestureName;
            this.singleOrientationGestureName = singleOrientationGestureName;
            this.replaceGestureWith = replaceGestureWith;
        }
    }

    public CustomGestureDetector(Context context, GestureLibrary multipleOrientationLib, GestureLibrary singleOrientationLib, OnGesturePerformedListener listener) {
        this.mMinPredictionScore = DEFAULT_MIN_PREDICTION_SCORE;
        this.mMinGestureStrokeLength = DEFAULT_MIN_GESTURE_STROKE_LENGTH;
        this.mMinLetterGestureStrokeSquareness = DEFAULT_MIN_LETTER_GESTURE_STROKE_SQUARENESS;
        this.mRegionDiagonalThreshold = DEFAULT_REGION_DIAGONAL_THRESHOLD;
        this.mNonEnclosedRegionDiagonalThreshold = 0.6f;
        this.mLineGestureStrokeStraightness = DEFAULT_LINE_GESTURE_STROKE_STRAIGTHNESS;
        this.mMaxLineGestureStrokeAngleDeviation = DEFAULT_MAX_LINE_GESTURE_STROKE_ANGLE_DEVIATION;
        this.mMinLineGestureStrokeLength = DEFAULT_MIN_LINE_GESTURE_STROKE_LENGTH;
        this.mLineGestureStrokePortraitAngle = 0.0f;
        this.mLineGestureStrokeLandscapeAngle = 0.0f;
        this.mIsGesturing = false;
        this.mLetterGestureStarted = false;
        this.mStrokeBuffer = new ArrayList(100);
        this.mGesturePerformed = new GesturePerformedRunnable(this, null);
        this.mOrientationFixes = null;
        this.asyncNotifications = true;
        this.mContext = context;
        this.mOnGesturePerformedListener = listener;
        this.mHandler = new Handler();
        if (multipleOrientationLib == null || singleOrientationLib == null) {
            throw new IllegalArgumentException("The multipleOrientationLib and singleOrientationLib libraries must not be null");
        }
        this.mGestureLibraryMultipleOrientations = multipleOrientationLib;
        this.mGestureLibraryMultipleOrientations.setOrientationStyle(2);
        this.mGestureLibraryMultipleOrientations.setSequenceType(2);
        this.mGestureLibraryMultipleOrientations.load();
        this.mGestureLibrarySingleOrientation = singleOrientationLib;
        this.mGestureLibrarySingleOrientation.setOrientationStyle(1);
        this.mGestureLibrarySingleOrientation.setSequenceType(1);
        this.mGestureLibrarySingleOrientation.load();
    }

    public CustomGestureDetector(Context context, int gestureLibraryResourceID, OnGesturePerformedListener listener) {
        this(context, GestureLibraries.fromRawResource(context, gestureLibraryResourceID), GestureLibraries.fromRawResource(context, gestureLibraryResourceID), listener);
    }

    public CustomGestureDetector(Context context, String gestureLibraryFilePath, OnGesturePerformedListener listener) {
        this(context, GestureLibraries.fromFile(gestureLibraryFilePath), GestureLibraries.fromFile(gestureLibraryFilePath), listener);
    }

    public void setMinLineGestureStrokeLength(int length) {
        this.mMinLineGestureStrokeLength = (float) length;
    }

    public void setMinGestureStrokeLength(float length) {
        this.mMinGestureStrokeLength = length;
    }

    public void setMinLetterGestureStrokeSquareness(float squareness) {
        this.mMinLetterGestureStrokeSquareness = squareness;
    }

    public void setLineGestureStrokeStraightness(float straightness) {
        if (straightness < 0.0f || straightness > 9.0f) {
            throw new IllegalArgumentException("straightness must be between 1 and 10");
        }
        this.mLineGestureStrokeStraightness = straightness;
    }

    public void setMaxLineGestureStrokeAngleDeviation(float angleDeviation) {
        if (angleDeviation < 0.0f || angleDeviation > DEFAULT_MAX_LINE_GESTURE_STROKE_ANGLE_DEVIATION) {
            throw new IllegalArgumentException("angleDeviation must be between 0 and 90");
        }
        this.mMaxLineGestureStrokeAngleDeviation = angleDeviation;
    }

    public void setLineGestureStrokePortraitAngle(float angle) {
        if (angle < 0.0f || angle > DEFAULT_MAX_LINE_GESTURE_STROKE_ANGLE_DEVIATION) {
            throw new IllegalArgumentException("angle must be between 0 and 90");
        }
        this.mLineGestureStrokePortraitAngle = angle;
    }

    public void setLineGestureStrokeLandscapeAngle(float angle) {
        if (angle < 0.0f || angle > DEFAULT_MAX_LINE_GESTURE_STROKE_ANGLE_DEVIATION) {
            throw new IllegalArgumentException("angle must be between 0 and 90");
        }
        this.mLineGestureStrokeLandscapeAngle = angle;
    }

    public void setRegionDiagonalThreshold(float threshold) {
        this.mRegionDiagonalThreshold = threshold;
    }

    public void setNonEnclosedRegionDiagonalThreshold(float threshold) {
        this.mNonEnclosedRegionDiagonalThreshold = threshold;
    }

    public void setMinPredictionScore(float minScore) {
        this.mMinPredictionScore = minScore;
    }

    public void setOrientationFixes(OrientationFix[] orientationFixes) {
        this.mOrientationFixes = orientationFixes;
    }

    public void setAsyncNotifications(boolean async) {
        this.asyncNotifications = async;
    }

    public void clear() {
        clear(false);
    }

    private void clear(boolean fireActionPerformed) {
        if (fireActionPerformed) {
            this.mGesturePerformed.letterGestureStarted = this.mLetterGestureStarted;
            if (this.asyncNotifications) {
                this.mHandler.post(this.mGesturePerformed);
                return;
            } else {
                fireOnGesturePerformed(this.mGesturePerformed);
                return;
            }
        }
        Log.w(TAG, "Knuckle Gesture performed but not recognized as Region, Letter, or Line Gesture.");
        this.mCurrentGesture = null;
    }

    public void cancelGesture() {
        this.mIsListeningForGestures = false;
        this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
        clear();
        this.mIsGesturing = false;
        this.mLetterGestureStarted = false;
        this.mStrokeBuffer.clear();
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                touchDown(event);
                return true;
            case 1:
                if (this.mIsListeningForGestures) {
                    touchUp(event, false);
                    return true;
                }
                break;
            case 2:
                if (this.mIsListeningForGestures) {
                    touchMove(event);
                    return true;
                }
                break;
            case 3:
                if (this.mIsListeningForGestures) {
                    touchUp(event, true);
                    return true;
                }
                break;
        }
        return false;
    }

    private void touchDown(MotionEvent event) {
        this.mIsListeningForGestures = true;
        float x = event.getX();
        float y = event.getY();
        this.mCurrentX = x;
        this.mCurrentY = y;
        this.mTotalLength = 0.0f;
        this.mIsGesturing = false;
        this.mLetterGestureStarted = false;
        this.mCurrentGesture = new Gesture();
        this.mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));
    }

    private void touchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float previousX = this.mCurrentX;
        float previousY = this.mCurrentY;
        float dx = Math.abs(x - previousX);
        float dy = Math.abs(y - previousY);
        if (dx >= 3.0f || dy >= 3.0f) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            this.mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));
            if (!this.mIsGesturing) {
                this.mTotalLength += (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
                if (this.mTotalLength > this.mMinGestureStrokeLength) {
                    this.mIsGesturing = true;
                }
            }
            if (this.mIsGesturing && (this.mLetterGestureStarted ^ 1) != 0 && GestureUtils.computeOrientedBoundingBox(this.mStrokeBuffer).squareness > this.mMinLetterGestureStrokeSquareness) {
                this.mLetterGestureStarted = true;
            }
        }
    }

    private void touchUp(MotionEvent event, boolean cancel) {
        this.mIsListeningForGestures = false;
        if (this.mCurrentGesture != null) {
            this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
            if (cancel) {
                cancelGesture(event);
            } else {
                clear(this.mIsGesturing);
            }
        } else {
            cancelGesture(event);
        }
        this.mStrokeBuffer.clear();
        this.mIsGesturing = false;
        this.mLetterGestureStarted = false;
    }

    private void cancelGesture(MotionEvent event) {
        clear(false);
    }

    private float calculateDistanceBetweenFirstAndLastPoint() {
        float[] points = ((GestureStroke) this.mCurrentGesture.getStrokes().get(0)).points;
        if (points.length < 4) {
            return 0.0f;
        }
        float firstX = points[0];
        float firstY = points[1];
        float deltaX = firstX - points[points.length - 2];
        float deltaY = firstY - points[points.length - 1];
        return (float) Math.sqrt((double) ((deltaX * deltaX) + (deltaY * deltaY)));
    }

    private boolean notifyIfLineGesture() {
        OrientedBoundingBox box = ((GestureStroke) this.mCurrentGesture.getStrokes().get(0)).computeOrientedBoundingBox();
        if (box.squareness > 0.1f * ((10.0f - this.mLineGestureStrokeStraightness) / 10.0f)) {
            return false;
        }
        if (Math.abs((this.mContext.getResources().getConfiguration().orientation == 2 ? this.mLineGestureStrokeLandscapeAngle : this.mLineGestureStrokePortraitAngle) - Math.abs(box.orientation)) > this.mMaxLineGestureStrokeAngleDeviation) {
            return false;
        }
        float distance = calculateDistanceBetweenFirstAndLastPoint();
        if (distance < this.mMinLineGestureStrokeLength || distance < box.width * 0.9f) {
            return false;
        }
        notifyLineGesture("line", (double) this.mMinPredictionScore);
        return true;
    }

    private boolean notifyIfRegionGesture(float boundingBoxDiagonalThreshold) {
        float distance = calculateDistanceBetweenFirstAndLastPoint();
        RectF boundingBox = this.mCurrentGesture.getBoundingBox();
        float height = boundingBox.height();
        float width = boundingBox.width();
        if (distance >= ((float) Math.sqrt((double) ((height * height) + (width * width)))) * boundingBoxDiagonalThreshold) {
            return false;
        }
        notifyRegionGesture(StylusGestureSettings.STYLUS_GESTURE_REGION_SUFFIX, (double) this.mMinPredictionScore);
        return true;
    }

    @SuppressLint({"PreferForInArrayList"})
    private boolean notifyIfLetterGesture() {
        try {
            ArrayList<Prediction> predMultipleOrient = this.mGestureLibraryMultipleOrientations.recognize(this.mCurrentGesture);
            ArrayList<Prediction> predSingleOrient = this.mGestureLibrarySingleOrientation.recognize(this.mCurrentGesture);
            if (predMultipleOrient == null || predSingleOrient == null) {
                Log.e(TAG, "The gesture library returned a null list of predictions. Ignoring gesture.");
                return false;
            } else if (predMultipleOrient.isEmpty()) {
                return false;
            } else {
                Prediction bestPredMultOrient = (Prediction) predMultipleOrient.get(0);
                if (bestPredMultOrient.name.equals("reject") || bestPredMultOrient.score < ((double) this.mMinPredictionScore)) {
                    return false;
                }
                if (predSingleOrient.isEmpty()) {
                    notifyLetterGesture(bestPredMultOrient.name, bestPredMultOrient.score);
                    return true;
                }
                Prediction bestPredSingleOrient = (Prediction) predSingleOrient.get(0);
                if (bestPredSingleOrient.name.equals("reject")) {
                    return false;
                }
                if (bestPredMultOrient.name.equals(bestPredSingleOrient.name)) {
                    notifyLetterGesture(bestPredMultOrient.name, bestPredMultOrient.score);
                    return true;
                }
                if (this.mOrientationFixes != null) {
                    OrientationFix[] orientationFixArr = this.mOrientationFixes;
                    int length = orientationFixArr.length;
                    int i = 0;
                    while (i < length) {
                        OrientationFix fix = orientationFixArr[i];
                        if (!bestPredMultOrient.name.equals(fix.multipleOrientationsGestureName) || !bestPredSingleOrient.name.equals(fix.singleOrientationGestureName)) {
                            i++;
                        } else if (fix.replaceGestureWith == null) {
                            return false;
                        } else {
                            notifyLetterGesture(fix.replaceGestureWith, bestPredMultOrient.score);
                            return true;
                        }
                    }
                }
                if (predSingleOrient.size() > 1) {
                    if (bestPredMultOrient.name.equals(((Prediction) predSingleOrient.get(1)).name)) {
                        notifyLetterGesture(bestPredMultOrient.name, bestPredMultOrient.score);
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception excep) {
            Log.e(TAG, "There was an error recognizing the gesture. Ignoring gesture.");
            excep.printStackTrace();
            return false;
        }
    }

    private void notifyLetterGesture(String gestureName, double predictionScore) {
        if (this.mOnGesturePerformedListener != null) {
            this.mOnGesturePerformedListener.onLetterGesture(gestureName, this.mCurrentGesture, predictionScore);
        }
    }

    private void notifyRegionGesture(String gestureName, double predictionScore) {
        if (this.mOnGesturePerformedListener != null) {
            this.mOnGesturePerformedListener.onRegionGesture(gestureName, this.mCurrentGesture, predictionScore);
        }
    }

    private void notifyLineGesture(String gestureName, double predictionScore) {
        if (this.mOnGesturePerformedListener != null) {
            this.mOnGesturePerformedListener.onLineGesture(gestureName, this.mCurrentGesture, predictionScore);
        }
    }

    private void fireOnGesturePerformed(GesturePerformedRunnable gesturePerformed) {
        if (this.mCurrentGesture == null || this.mCurrentGesture.getStrokesCount() <= 0) {
            Log.w(TAG, "Ignoring empty gesture.");
        } else if (notifyIfLineGesture()) {
            Log.w(TAG, "Recognized Line Gesture.");
        } else if (notifyIfRegionGesture(this.mRegionDiagonalThreshold)) {
            Log.w(TAG, "Recognized Region Gesture.");
        } else if (gesturePerformed.letterGestureStarted && notifyIfLetterGesture()) {
            Log.w(TAG, "Recognized Letter Gesture.");
        } else if (notifyIfRegionGesture(this.mNonEnclosedRegionDiagonalThreshold)) {
            Log.w(TAG, "Recognized Region Gesture.");
        }
    }
}
