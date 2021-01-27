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

    /* JADX INFO: Multiple debug info for r4v2 float[]: [D('strokepoints' float[]), D('rect' android.graphics.RectF)] */
    /* JADX INFO: Multiple debug info for r3v3 int: [D('stroke' android.gesture.GestureStroke), D('size' int)] */
    /* JADX INFO: Multiple debug info for r5v2 float[]: [D('gestureWidth' float), D('pts' float[])] */
    /* JADX INFO: Multiple debug info for r5v19 float: [D('ypos' float), D('pts' float[])] */
    public static float[] spatialSampling(Gesture gesture, int bitmapSize, boolean keepAspectRatio) {
        int size;
        float segmentStartX;
        float targetPatchSize;
        float[] pts;
        float preDy;
        float preDx;
        float targetPatchSize2 = (float) (bitmapSize - 1);
        float[] sample = new float[(bitmapSize * bitmapSize)];
        Arrays.fill(sample, 0.0f);
        RectF rect = gesture.getBoundingBox();
        float gestureWidth = rect.width();
        float gestureHeight = rect.height();
        float sx = targetPatchSize2 / gestureWidth;
        float sy = targetPatchSize2 / gestureHeight;
        if (keepAspectRatio) {
            float scale = sx < sy ? sx : sy;
            sx = scale;
            sy = scale;
        } else {
            float aspectRatio = gestureWidth / gestureHeight;
            if (aspectRatio > 1.0f) {
                aspectRatio = 1.0f / aspectRatio;
            }
            if (aspectRatio < SCALING_THRESHOLD) {
                float scale2 = sx < sy ? sx : sy;
                sx = scale2;
                sy = scale2;
            } else if (sx > sy) {
                float scale3 = NONUNIFORM_SCALE * sy;
                if (scale3 < sx) {
                    sx = scale3;
                }
            } else {
                float scale4 = NONUNIFORM_SCALE * sx;
                if (scale4 < sy) {
                    sy = scale4;
                }
            }
        }
        float preDx2 = -rect.centerX();
        float preDy2 = -rect.centerY();
        float postDx = targetPatchSize2 / 2.0f;
        float postDy = targetPatchSize2 / 2.0f;
        ArrayList<GestureStroke> strokes = gesture.getStrokes();
        int count = strokes.size();
        int index = 0;
        while (index < count) {
            float[] strokepoints = strokes.get(index).points;
            int size2 = strokepoints.length;
            float[] pts2 = new float[size2];
            for (int i = 0; i < size2; i += 2) {
                pts2[i] = ((strokepoints[i] + preDx2) * sx) + postDx;
                pts2[i + 1] = ((strokepoints[i + 1] + preDy2) * sy) + postDy;
            }
            float segmentEndX = -1.0f;
            int i2 = 0;
            float segmentEndY = -1.0f;
            while (i2 < size2) {
                float segmentStartX2 = pts2[i2] < 0.0f ? 0.0f : pts2[i2];
                float segmentStartY = pts2[i2 + 1] < 0.0f ? 0.0f : pts2[i2 + 1];
                if (segmentStartX2 > targetPatchSize2) {
                    size = size2;
                    segmentStartX = targetPatchSize2;
                } else {
                    size = size2;
                    segmentStartX = segmentStartX2;
                }
                if (segmentStartY > targetPatchSize2) {
                    targetPatchSize = targetPatchSize2;
                } else {
                    targetPatchSize = targetPatchSize2;
                    targetPatchSize2 = segmentStartY;
                }
                plot(segmentStartX, targetPatchSize2, sample, bitmapSize);
                if (segmentEndX != -1.0f) {
                    if (segmentEndX > segmentStartX) {
                        preDx = preDx2;
                        preDy = preDy2;
                        float xpos = (float) Math.ceil((double) segmentStartX);
                        float slope = (segmentEndY - targetPatchSize2) / (segmentEndX - segmentStartX);
                        while (xpos < segmentEndX) {
                            plot(xpos, ((xpos - segmentStartX) * slope) + targetPatchSize2, sample, bitmapSize);
                            xpos += 1.0f;
                            pts2 = pts2;
                        }
                        pts = pts2;
                    } else {
                        pts = pts2;
                        preDx = preDx2;
                        preDy = preDy2;
                        if (segmentEndX < segmentStartX) {
                            float slope2 = (segmentEndY - targetPatchSize2) / (segmentEndX - segmentStartX);
                            for (float xpos2 = (float) Math.ceil((double) segmentEndX); xpos2 < segmentStartX; xpos2 += 1.0f) {
                                plot(xpos2, ((xpos2 - segmentStartX) * slope2) + targetPatchSize2, sample, bitmapSize);
                            }
                        }
                    }
                    if (segmentEndY > targetPatchSize2) {
                        float invertSlope = (segmentEndX - segmentStartX) / (segmentEndY - targetPatchSize2);
                        for (float ypos = (float) Math.ceil((double) targetPatchSize2); ypos < segmentEndY; ypos += 1.0f) {
                            plot(((ypos - targetPatchSize2) * invertSlope) + segmentStartX, ypos, sample, bitmapSize);
                        }
                    } else if (segmentEndY < targetPatchSize2) {
                        float invertSlope2 = (segmentEndX - segmentStartX) / (segmentEndY - targetPatchSize2);
                        for (float ypos2 = (float) Math.ceil((double) segmentEndY); ypos2 < targetPatchSize2; ypos2 += 1.0f) {
                            plot(((ypos2 - targetPatchSize2) * invertSlope2) + segmentStartX, ypos2, sample, bitmapSize);
                        }
                    }
                } else {
                    pts = pts2;
                    preDx = preDx2;
                    preDy = preDy2;
                }
                segmentEndX = segmentStartX;
                segmentEndY = targetPatchSize2;
                i2 += 2;
                targetPatchSize2 = targetPatchSize;
                preDx2 = preDx;
                size2 = size;
                preDy2 = preDy;
                pts2 = pts;
            }
            index++;
            rect = rect;
            gestureWidth = gestureWidth;
            gestureHeight = gestureHeight;
            sx = sx;
        }
        return sample;
    }

    private static void plot(float x, float y, float[] sample, int sampleSize) {
        float y2 = 0.0f;
        float x2 = x < 0.0f ? 0.0f : x;
        if (y >= 0.0f) {
            y2 = y;
        }
        int xFloor = (int) Math.floor((double) x2);
        int xCeiling = (int) Math.ceil((double) x2);
        int yFloor = (int) Math.floor((double) y2);
        int yCeiling = (int) Math.ceil((double) y2);
        if (x2 == ((float) xFloor) && y2 == ((float) yFloor)) {
            int index = (yCeiling * sampleSize) + xCeiling;
            if (sample[index] < 1.0f) {
                sample[index] = 1.0f;
            }
            return;
        }
        double xFloorSq = Math.pow((double) (((float) xFloor) - x2), 2.0d);
        double yFloorSq = Math.pow((double) (((float) yFloor) - y2), 2.0d);
        double xCeilingSq = Math.pow((double) (((float) xCeiling) - x2), 2.0d);
        double yCeilingSq = Math.pow((double) (((float) yCeiling) - y2), 2.0d);
        float topLeft = (float) Math.sqrt(xFloorSq + yFloorSq);
        float topRight = (float) Math.sqrt(xCeilingSq + yFloorSq);
        float btmLeft = (float) Math.sqrt(xFloorSq + yCeilingSq);
        float btmRight = (float) Math.sqrt(xCeilingSq + yCeilingSq);
        float sum = topLeft + topRight + btmLeft + btmRight;
        float value = topLeft / sum;
        int index2 = (yFloor * sampleSize) + xFloor;
        if (value > sample[index2]) {
            sample[index2] = value;
        }
        float value2 = topRight / sum;
        int index3 = (yFloor * sampleSize) + xCeiling;
        if (value2 > sample[index3]) {
            sample[index3] = value2;
        }
        float value3 = btmLeft / sum;
        int index4 = (yCeiling * sampleSize) + xFloor;
        if (value3 > sample[index4]) {
            sample[index4] = value3;
        }
        float value4 = btmRight / sum;
        int index5 = (yCeiling * sampleSize) + xCeiling;
        if (value4 > sample[index5]) {
            sample[index5] = value4;
        }
    }

    public static float[] temporalSampling(GestureStroke stroke, int numPoints) {
        float lstPointY;
        int i;
        float increment = stroke.length / ((float) (numPoints - 1));
        int vectorLength = numPoints * 2;
        float[] vector = new float[vectorLength];
        float distanceSoFar = 0.0f;
        float[] pts = stroke.points;
        float lstPointX = pts[0];
        int i2 = 1;
        float ratio = pts[1];
        float currentPointX = Float.MIN_VALUE;
        float currentPointY = Float.MIN_VALUE;
        vector[0] = lstPointX;
        int index = 0 + 1;
        vector[index] = ratio;
        int index2 = index + 1;
        int i3 = 0;
        int count = pts.length / 2;
        while (true) {
            if (i3 >= count) {
                lstPointY = ratio;
                break;
            }
            if (currentPointX == Float.MIN_VALUE) {
                i3++;
                if (i3 >= count) {
                    lstPointY = ratio;
                    break;
                }
                currentPointX = pts[i3 * 2];
                currentPointY = pts[(i3 * 2) + i2];
            }
            float deltaX = currentPointX - lstPointX;
            float deltaY = currentPointY - ratio;
            float distance = (float) Math.hypot((double) deltaX, (double) deltaY);
            if (distanceSoFar + distance >= increment) {
                float ratio2 = (increment - distanceSoFar) / distance;
                float nx = (ratio2 * deltaX) + lstPointX;
                float ny = ratio + (ratio2 * deltaY);
                vector[index2] = nx;
                int index3 = index2 + 1;
                vector[index3] = ny;
                i = 1;
                index2 = index3 + 1;
                lstPointX = nx;
                distanceSoFar = 0.0f;
                ratio = ny;
            } else {
                i = 1;
                lstPointX = currentPointX;
                ratio = currentPointY;
                currentPointX = Float.MIN_VALUE;
                currentPointY = Float.MIN_VALUE;
                distanceSoFar += distance;
            }
            i2 = i;
            count = count;
            i3 = i3;
        }
        for (int i4 = index2; i4 < vectorLength; i4 += 2) {
            vector[i4] = lstPointX;
            vector[i4 + 1] = lstPointY;
        }
        return vector;
    }

    static float[] computeCentroid(float[] points) {
        float centerX = 0.0f;
        float centerY = 0.0f;
        int count = points.length;
        int i = 0;
        while (i < count) {
            centerX += points[i];
            int i2 = i + 1;
            centerY += points[i2];
            i = i2 + 1;
        }
        return new float[]{(centerX * 2.0f) / ((float) count), (2.0f * centerY) / ((float) count)};
    }

    private static float[][] computeCoVariance(float[] points) {
        float[][] array = (float[][]) Array.newInstance(float.class, 2, 2);
        array[0][0] = 0.0f;
        array[0][1] = 0.0f;
        array[1][0] = 0.0f;
        array[1][1] = 0.0f;
        int count = points.length;
        int i = 0;
        while (i < count) {
            float x = points[i];
            int i2 = i + 1;
            float y = points[i2];
            float[] fArr = array[0];
            fArr[0] = fArr[0] + (x * x);
            float[] fArr2 = array[0];
            fArr2[1] = fArr2[1] + (x * y);
            array[1][0] = array[0][1];
            float[] fArr3 = array[1];
            fArr3[1] = fArr3[1] + (y * y);
            i = i2 + 1;
        }
        float[] fArr4 = array[0];
        fArr4[0] = fArr4[0] / ((float) (count / 2));
        float[] fArr5 = array[0];
        fArr5[1] = fArr5[1] / ((float) (count / 2));
        float[] fArr6 = array[1];
        fArr6[0] = fArr6[0] / ((float) (count / 2));
        float[] fArr7 = array[1];
        fArr7[1] = fArr7[1] / ((float) (count / 2));
        return array;
    }

    static float computeTotalLength(float[] points) {
        float sum = 0.0f;
        int count = points.length - 4;
        for (int i = 0; i < count; i += 2) {
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
        int len = vector1.length;
        for (int i = 0; i < len; i++) {
            sum += vector1[i] * vector2[i];
        }
        return (float) Math.acos((double) sum);
    }

    static float minimumCosineDistance(float[] vector1, float[] vector2, int numOrientations) {
        int len = vector1.length;
        float a = 0.0f;
        float b = 0.0f;
        for (int i = 0; i < len; i += 2) {
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
        return (float) Math.acos((((double) a) * cosine) + (((double) b) * ((double) tan) * cosine));
    }

    public static OrientedBoundingBox computeOrientedBoundingBox(ArrayList<GesturePoint> originalPoints) {
        int count = originalPoints.size();
        float[] points = new float[(count * 2)];
        for (int i = 0; i < count; i++) {
            GesturePoint point = originalPoints.get(i);
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
            int i2 = i + 1;
            if (points[i2] < miny) {
                miny = points[i2];
            }
            if (points[i2] > maxy) {
                maxy = points[i2];
            }
            i = i2 + 1;
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
            float x = (points[i] * cos) - (points[i + 1] * sin);
            float y = (points[i] * sin) + (points[i + 1] * cos);
            points[i] = x;
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
