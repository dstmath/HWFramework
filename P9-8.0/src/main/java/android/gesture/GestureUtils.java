package android.gesture;

import android.graphics.RectF;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public final class GestureUtils {
    private static final float NONUNIFORM_SCALE = ((float) Math.sqrt(2.0d));
    private static final float SCALING_THRESHOLD = 0.26f;

    private GestureUtils() {
    }

    static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(GestureConstants.LOG_TAG, "Could not close stream", e);
            }
        }
    }

    public static float[] spatialSampling(Gesture gesture, int bitmapSize) {
        return spatialSampling(gesture, bitmapSize, false);
    }

    public static float[] spatialSampling(Gesture gesture, int bitmapSize, boolean keepAspectRatio) {
        float targetPatchSize = (float) (bitmapSize - 1);
        float[] sample = new float[(bitmapSize * bitmapSize)];
        Arrays.fill(sample, 0.0f);
        RectF rect = gesture.getBoundingBox();
        float gestureWidth = rect.width();
        float gestureHeight = rect.height();
        float sx = targetPatchSize / gestureWidth;
        float sy = targetPatchSize / gestureHeight;
        float scale;
        if (keepAspectRatio) {
            scale = sx < sy ? sx : sy;
            sx = scale;
            sy = scale;
        } else {
            float aspectRatio = gestureWidth / gestureHeight;
            if (aspectRatio > 1.0f) {
                aspectRatio = 1.0f / aspectRatio;
            }
            if (aspectRatio < SCALING_THRESHOLD) {
                scale = sx < sy ? sx : sy;
                sx = scale;
                sy = scale;
            } else if (sx > sy) {
                scale = sy * NONUNIFORM_SCALE;
                if (scale < sx) {
                    sx = scale;
                }
            } else {
                scale = sx * NONUNIFORM_SCALE;
                if (scale < sy) {
                    sy = scale;
                }
            }
        }
        float preDx = -rect.centerX();
        float preDy = -rect.centerY();
        float postDx = targetPatchSize / 2.0f;
        float postDy = targetPatchSize / 2.0f;
        ArrayList<GestureStroke> strokes = gesture.getStrokes();
        int count = strokes.size();
        for (int index = 0; index < count; index++) {
            int i;
            float[] strokepoints = ((GestureStroke) strokes.get(index)).points;
            int size = strokepoints.length;
            float[] pts = new float[size];
            for (i = 0; i < size; i += 2) {
                pts[i] = ((strokepoints[i] + preDx) * sx) + postDx;
                pts[i + 1] = ((strokepoints[i + 1] + preDy) * sy) + postDy;
            }
            float segmentEndX = -1.0f;
            float segmentEndY = -1.0f;
            i = 0;
            while (i < size) {
                float segmentStartX = pts[i] < 0.0f ? 0.0f : pts[i];
                float segmentStartY = pts[i + 1] < 0.0f ? 0.0f : pts[i + 1];
                if (segmentStartX > targetPatchSize) {
                    segmentStartX = targetPatchSize;
                }
                if (segmentStartY > targetPatchSize) {
                    segmentStartY = targetPatchSize;
                }
                plot(segmentStartX, segmentStartY, sample, bitmapSize);
                if (segmentEndX != -1.0f) {
                    float slope;
                    float xpos;
                    if (segmentEndX > segmentStartX) {
                        slope = (segmentEndY - segmentStartY) / (segmentEndX - segmentStartX);
                        for (xpos = (float) Math.ceil((double) segmentStartX); xpos < segmentEndX; xpos += 1.0f) {
                            plot(xpos, ((xpos - segmentStartX) * slope) + segmentStartY, sample, bitmapSize);
                        }
                    } else if (segmentEndX < segmentStartX) {
                        slope = (segmentEndY - segmentStartY) / (segmentEndX - segmentStartX);
                        for (xpos = (float) Math.ceil((double) segmentEndX); xpos < segmentStartX; xpos += 1.0f) {
                            plot(xpos, ((xpos - segmentStartX) * slope) + segmentStartY, sample, bitmapSize);
                        }
                    }
                    float invertSlope;
                    float ypos;
                    if (segmentEndY > segmentStartY) {
                        invertSlope = (segmentEndX - segmentStartX) / (segmentEndY - segmentStartY);
                        for (ypos = (float) Math.ceil((double) segmentStartY); ypos < segmentEndY; ypos += 1.0f) {
                            plot(((ypos - segmentStartY) * invertSlope) + segmentStartX, ypos, sample, bitmapSize);
                        }
                    } else if (segmentEndY < segmentStartY) {
                        invertSlope = (segmentEndX - segmentStartX) / (segmentEndY - segmentStartY);
                        for (ypos = (float) Math.ceil((double) segmentEndY); ypos < segmentStartY; ypos += 1.0f) {
                            plot(((ypos - segmentStartY) * invertSlope) + segmentStartX, ypos, sample, bitmapSize);
                        }
                    }
                }
                segmentEndX = segmentStartX;
                segmentEndY = segmentStartY;
                i += 2;
            }
        }
        return sample;
    }

    private static void plot(float x, float y, float[] sample, int sampleSize) {
        if (x < 0.0f) {
            x = 0.0f;
        }
        if (y < 0.0f) {
            y = 0.0f;
        }
        int xFloor = (int) Math.floor((double) x);
        int xCeiling = (int) Math.ceil((double) x);
        int yFloor = (int) Math.floor((double) y);
        int yCeiling = (int) Math.ceil((double) y);
        int index;
        if (x == ((float) xFloor) && y == ((float) yFloor)) {
            index = (yCeiling * sampleSize) + xCeiling;
            if (sample[index] < 1.0f) {
                sample[index] = 1.0f;
                return;
            }
            return;
        }
        double xFloorSq = Math.pow((double) (((float) xFloor) - x), 2.0d);
        double yFloorSq = Math.pow((double) (((float) yFloor) - y), 2.0d);
        double xCeilingSq = Math.pow((double) (((float) xCeiling) - x), 2.0d);
        double yCeilingSq = Math.pow((double) (((float) yCeiling) - y), 2.0d);
        float topLeft = (float) Math.sqrt(xFloorSq + yFloorSq);
        float topRight = (float) Math.sqrt(xCeilingSq + yFloorSq);
        float btmLeft = (float) Math.sqrt(xFloorSq + yCeilingSq);
        float btmRight = (float) Math.sqrt(xCeilingSq + yCeilingSq);
        float sum = ((topLeft + topRight) + btmLeft) + btmRight;
        float value = topLeft / sum;
        index = (yFloor * sampleSize) + xFloor;
        if (value > sample[index]) {
            sample[index] = value;
        }
        value = topRight / sum;
        index = (yFloor * sampleSize) + xCeiling;
        if (value > sample[index]) {
            sample[index] = value;
        }
        value = btmLeft / sum;
        index = (yCeiling * sampleSize) + xFloor;
        if (value > sample[index]) {
            sample[index] = value;
        }
        value = btmRight / sum;
        index = (yCeiling * sampleSize) + xCeiling;
        if (value > sample[index]) {
            sample[index] = value;
        }
    }

    public static float[] temporalSampling(GestureStroke stroke, int numPoints) {
        float increment = stroke.length / ((float) (numPoints - 1));
        int vectorLength = numPoints * 2;
        float[] vector = new float[vectorLength];
        float distanceSoFar = 0.0f;
        float[] pts = stroke.points;
        float lstPointX = pts[0];
        float lstPointY = pts[1];
        float currentPointX = Float.MIN_VALUE;
        float currentPointY = Float.MIN_VALUE;
        vector[0] = lstPointX;
        vector[1] = lstPointY;
        int index = 1 + 1;
        int i = 0;
        int count = pts.length / 2;
        while (i < count) {
            if (currentPointX == Float.MIN_VALUE) {
                i++;
                if (i >= count) {
                    break;
                }
                currentPointX = pts[i * 2];
                currentPointY = pts[(i * 2) + 1];
            }
            float deltaX = currentPointX - lstPointX;
            float deltaY = currentPointY - lstPointY;
            float distance = (float) Math.hypot((double) deltaX, (double) deltaY);
            if (distanceSoFar + distance >= increment) {
                float ratio = (increment - distanceSoFar) / distance;
                float nx = lstPointX + (ratio * deltaX);
                float ny = lstPointY + (ratio * deltaY);
                vector[index] = nx;
                index++;
                vector[index] = ny;
                index++;
                lstPointX = nx;
                lstPointY = ny;
                distanceSoFar = 0.0f;
            } else {
                lstPointX = currentPointX;
                lstPointY = currentPointY;
                currentPointX = Float.MIN_VALUE;
                currentPointY = Float.MIN_VALUE;
                distanceSoFar += distance;
            }
        }
        for (i = index; i < vectorLength; i += 2) {
            vector[i] = lstPointX;
            vector[i + 1] = lstPointY;
        }
        return vector;
    }

    static float[] computeCentroid(float[] points) {
        float centerX = 0.0f;
        float centerY = 0.0f;
        int i = 0;
        while (i < points.length) {
            centerX += points[i];
            i++;
            centerY += points[i];
            i++;
        }
        return new float[]{(2.0f * centerX) / ((float) points.length), (2.0f * centerY) / ((float) points.length)};
    }

    private static float[][] computeCoVariance(float[] points) {
        float[] fArr;
        float[][] array = (float[][]) Array.newInstance(Float.TYPE, new int[]{2, 2});
        array[0][0] = 0.0f;
        array[0][1] = 0.0f;
        array[1][0] = 0.0f;
        array[1][1] = 0.0f;
        int count = points.length;
        int i = 0;
        while (i < count) {
            float x = points[i];
            i++;
            float y = points[i];
            fArr = array[0];
            fArr[0] = fArr[0] + (x * x);
            fArr = array[0];
            fArr[1] = fArr[1] + (x * y);
            array[1][0] = array[0][1];
            fArr = array[1];
            fArr[1] = fArr[1] + (y * y);
            i++;
        }
        fArr = array[0];
        fArr[0] = fArr[0] / ((float) (count / 2));
        fArr = array[0];
        fArr[1] = fArr[1] / ((float) (count / 2));
        fArr = array[1];
        fArr[0] = fArr[0] / ((float) (count / 2));
        fArr = array[1];
        fArr[1] = fArr[1] / ((float) (count / 2));
        return array;
    }

    static float computeTotalLength(float[] points) {
        float sum = 0.0f;
        for (int i = 0; i < points.length - 4; i += 2) {
            sum = (float) (((double) sum) + Math.hypot((double) (points[i + 2] - points[i]), (double) (points[i + 3] - points[i + 1])));
        }
        return sum;
    }

    static float computeStraightness(float[] points) {
        return ((float) Math.hypot((double) (points[2] - points[0]), (double) (points[3] - points[1]))) / computeTotalLength(points);
    }

    static float computeStraightness(float[] points, float totalLen) {
        return ((float) Math.hypot((double) (points[2] - points[0]), (double) (points[3] - points[1]))) / totalLen;
    }

    static float squaredEuclideanDistance(float[] vector1, float[] vector2) {
        float squaredDistance = 0.0f;
        int size = vector1.length;
        for (int i = 0; i < size; i++) {
            float difference = vector1[i] - vector2[i];
            squaredDistance += difference * difference;
        }
        return squaredDistance / ((float) size);
    }

    static float cosineDistance(float[] vector1, float[] vector2) {
        float sum = 0.0f;
        for (int i = 0; i < vector1.length; i++) {
            sum += vector1[i] * vector2[i];
        }
        return (float) Math.acos((double) sum);
    }

    static float minimumCosineDistance(float[] vector1, float[] vector2, int numOrientations) {
        float a = 0.0f;
        float b = 0.0f;
        for (int i = 0; i < vector1.length; i += 2) {
            a += (vector1[i] * vector2[i]) + (vector1[i + 1] * vector2[i + 1]);
            b += (vector1[i] * vector2[i + 1]) - (vector1[i + 1] * vector2[i]);
        }
        if (a == 0.0f) {
            return 1.5707964f;
        }
        float tan = b / a;
        double angle = Math.atan((double) tan);
        if (numOrientations > 2 && Math.abs(angle) >= 3.141592653589793d / ((double) numOrientations)) {
            return (float) Math.acos((double) a);
        }
        double cosine = Math.cos(angle);
        return (float) Math.acos((((double) a) * cosine) + (((double) b) * (cosine * ((double) tan))));
    }

    public static OrientedBoundingBox computeOrientedBoundingBox(ArrayList<GesturePoint> originalPoints) {
        int count = originalPoints.size();
        float[] points = new float[(count * 2)];
        for (int i = 0; i < count; i++) {
            GesturePoint point = (GesturePoint) originalPoints.get(i);
            int index = i * 2;
            points[index] = point.x;
            points[index + 1] = point.y;
        }
        return computeOrientedBoundingBox(points, computeCentroid(points));
    }

    public static OrientedBoundingBox computeOrientedBoundingBox(float[] originalPoints) {
        int size = originalPoints.length;
        float[] points = new float[size];
        for (int i = 0; i < size; i++) {
            points[i] = originalPoints[i];
        }
        return computeOrientedBoundingBox(points, computeCentroid(points));
    }

    private static OrientedBoundingBox computeOrientedBoundingBox(float[] points, float[] centroid) {
        float angle;
        translate(points, -centroid[0], -centroid[1]);
        float[] targetVector = computeOrientation(computeCoVariance(points));
        if (targetVector[0] == 0.0f && targetVector[1] == 0.0f) {
            angle = -1.5707964f;
        } else {
            angle = (float) Math.atan2((double) targetVector[1], (double) targetVector[0]);
            rotate(points, -angle);
        }
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxx = Float.MIN_VALUE;
        float maxy = Float.MIN_VALUE;
        int count = points.length;
        int i = 0;
        while (i < count) {
            if (points[i] < minx) {
                minx = points[i];
            }
            if (points[i] > maxx) {
                maxx = points[i];
            }
            i++;
            if (points[i] < miny) {
                miny = points[i];
            }
            if (points[i] > maxy) {
                maxy = points[i];
            }
            i++;
        }
        return new OrientedBoundingBox((float) (((double) (180.0f * angle)) / 3.141592653589793d), centroid[0], centroid[1], maxx - minx, maxy - miny);
    }

    private static float[] computeOrientation(float[][] covarianceMatrix) {
        float[] targetVector = new float[2];
        if (covarianceMatrix[0][1] == 0.0f || covarianceMatrix[1][0] == 0.0f) {
            targetVector[0] = 1.0f;
            targetVector[1] = 0.0f;
        }
        float value = ((-covarianceMatrix[0][0]) - covarianceMatrix[1][1]) / 2.0f;
        float rightside = (float) Math.sqrt(Math.pow((double) value, 2.0d) - ((double) ((covarianceMatrix[0][0] * covarianceMatrix[1][1]) - (covarianceMatrix[0][1] * covarianceMatrix[1][0]))));
        float lambda1 = (-value) + rightside;
        float lambda2 = (-value) - rightside;
        if (lambda1 == lambda2) {
            targetVector[0] = 0.0f;
            targetVector[1] = 0.0f;
        } else {
            float lambda = lambda1 > lambda2 ? lambda1 : lambda2;
            targetVector[0] = 1.0f;
            targetVector[1] = (lambda - covarianceMatrix[0][0]) / covarianceMatrix[0][1];
        }
        return targetVector;
    }

    static float[] rotate(float[] points, float angle) {
        float cos = (float) Math.cos((double) angle);
        float sin = (float) Math.sin((double) angle);
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            float y = (points[i] * sin) + (points[i + 1] * cos);
            points[i] = (points[i] * cos) - (points[i + 1] * sin);
            points[i + 1] = y;
        }
        return points;
    }

    static float[] translate(float[] points, float dx, float dy) {
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            points[i] = points[i] + dx;
            int i2 = i + 1;
            points[i2] = points[i2] + dy;
        }
        return points;
    }

    static float[] scale(float[] points, float sx, float sy) {
        int size = points.length;
        for (int i = 0; i < size; i += 2) {
            points[i] = points[i] * sx;
            int i2 = i + 1;
            points[i2] = points[i2] * sy;
        }
        return points;
    }
}
