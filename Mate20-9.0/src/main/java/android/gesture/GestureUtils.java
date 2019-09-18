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
        int count;
        float targetPatchSize;
        float[] pts;
        float preDx;
        int i = bitmapSize;
        float targetPatchSize2 = (float) (i - 1);
        float[] sample = new float[(i * i)];
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
        float preDy = -rect.centerY();
        float postDx = targetPatchSize2 / 2.0f;
        float postDy = targetPatchSize2 / 2.0f;
        ArrayList<GestureStroke> strokes = gesture.getStrokes();
        int i2 = strokes.size();
        int index = 0;
        while (true) {
            int index2 = index;
            if (index2 < i2) {
                RectF rect2 = rect;
                GestureStroke stroke = strokes.get(index2);
                float gestureWidth2 = gestureWidth;
                float[] strokepoints = stroke.points;
                GestureStroke gestureStroke = stroke;
                int size = strokepoints.length;
                float gestureHeight2 = gestureHeight;
                float[] pts2 = new float[size];
                int i3 = 0;
                while (true) {
                    count = i2;
                    int count2 = i3;
                    if (count2 >= size) {
                        break;
                    }
                    pts2[count2] = ((strokepoints[count2] + preDx2) * sx) + postDx;
                    pts2[count2 + 1] = ((strokepoints[count2 + 1] + preDy) * sy) + postDy;
                    i3 = count2 + 2;
                    i2 = count;
                }
                float[] fArr = strokepoints;
                float sx2 = sx;
                float segmentEndX = -1.0f;
                int i4 = 0;
                float segmentEndY = -1.0f;
                while (i4 < size) {
                    float segmentStartX = pts2[i4] < 0.0f ? 0.0f : pts2[i4];
                    float segmentStartY = pts2[i4 + 1] < 0.0f ? 0.0f : pts2[i4 + 1];
                    if (segmentStartX > targetPatchSize2) {
                        segmentStartX = targetPatchSize2;
                    }
                    int size2 = size;
                    float segmentStartX2 = segmentStartX;
                    if (segmentStartY > targetPatchSize2) {
                        float segmentStartY2 = targetPatchSize2;
                        targetPatchSize = targetPatchSize2;
                    } else {
                        targetPatchSize = targetPatchSize2;
                        targetPatchSize2 = segmentStartY;
                    }
                    plot(segmentStartX2, targetPatchSize2, sample, i);
                    if (segmentEndX != -1.0f) {
                        if (segmentEndX > segmentStartX2) {
                            preDx = preDx2;
                            float xpos = (float) Math.ceil((double) segmentStartX2);
                            float slope = (segmentEndY - targetPatchSize2) / (segmentEndX - segmentStartX2);
                            while (xpos < segmentEndX) {
                                plot(xpos, ((xpos - segmentStartX2) * slope) + targetPatchSize2, sample, i);
                                xpos += 1.0f;
                                pts2 = pts2;
                            }
                            pts = pts2;
                        } else {
                            pts = pts2;
                            preDx = preDx2;
                            if (segmentEndX < segmentStartX2) {
                                float slope2 = (segmentEndY - targetPatchSize2) / (segmentEndX - segmentStartX2);
                                for (float xpos2 = (float) Math.ceil((double) segmentEndX); xpos2 < segmentStartX2; xpos2 += 1.0f) {
                                    plot(xpos2, ((xpos2 - segmentStartX2) * slope2) + targetPatchSize2, sample, i);
                                }
                            }
                        }
                        if (segmentEndY > targetPatchSize2) {
                            float invertSlope = (segmentEndX - segmentStartX2) / (segmentEndY - targetPatchSize2);
                            for (float ypos = (float) Math.ceil((double) targetPatchSize2); ypos < segmentEndY; ypos += 1.0f) {
                                plot(((ypos - targetPatchSize2) * invertSlope) + segmentStartX2, ypos, sample, i);
                            }
                        } else if (segmentEndY < targetPatchSize2) {
                            float invertSlope2 = (segmentEndX - segmentStartX2) / (segmentEndY - targetPatchSize2);
                            for (float ypos2 = (float) Math.ceil((double) segmentEndY); ypos2 < targetPatchSize2; ypos2 += 1.0f) {
                                plot(((ypos2 - targetPatchSize2) * invertSlope2) + segmentStartX2, ypos2, sample, i);
                            }
                        }
                    } else {
                        pts = pts2;
                        preDx = preDx2;
                    }
                    segmentEndX = segmentStartX2;
                    segmentEndY = targetPatchSize2;
                    i4 += 2;
                    size = size2;
                    targetPatchSize2 = targetPatchSize;
                    preDx2 = preDx;
                    pts2 = pts;
                }
                int i5 = size;
                float f = preDx2;
                index = index2 + 1;
                rect = rect2;
                gestureWidth = gestureWidth2;
                gestureHeight = gestureHeight2;
                i2 = count;
                sx = sx2;
            } else {
                RectF rectF = rect;
                float f2 = gestureWidth;
                float f3 = gestureHeight;
                float f4 = sx;
                int i6 = i2;
                float f5 = preDx2;
                return sample;
            }
        }
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
            float f = y2;
            float f2 = x2;
            int i = xFloor;
            int i2 = xCeiling;
            return;
        }
        double xFloorSq = Math.pow((double) (((float) xFloor) - x2), 2.0d);
        double yFloorSq = Math.pow((double) (((float) yFloor) - y2), 2.0d);
        double xCeilingSq = Math.pow((double) (((float) xCeiling) - x2), 2.0d);
        float f3 = y2;
        float f4 = x2;
        double yCeilingSq = Math.pow((double) (((float) yCeiling) - y2), 2.0d);
        float topLeft = (float) Math.sqrt(xFloorSq + yFloorSq);
        int xFloor2 = xFloor;
        int xCeiling2 = xCeiling;
        float topRight = (float) Math.sqrt(xCeilingSq + yFloorSq);
        double d = yFloorSq;
        float btmLeft = (float) Math.sqrt(xFloorSq + yCeilingSq);
        float btmRight = (float) Math.sqrt(xCeilingSq + yCeilingSq);
        float sum = topLeft + topRight + btmLeft + btmRight;
        float value = topLeft / sum;
        int index2 = (yFloor * sampleSize) + xFloor2;
        if (value > sample[index2]) {
            sample[index2] = value;
        }
        float value2 = topRight / sum;
        int index3 = (yFloor * sampleSize) + xCeiling2;
        if (value2 > sample[index3]) {
            sample[index3] = value2;
        }
        float value3 = btmLeft / sum;
        int index4 = (yCeiling * sampleSize) + xFloor2;
        if (value3 > sample[index4]) {
            sample[index4] = value3;
        }
        float value4 = btmRight / sum;
        int index5 = (yCeiling * sampleSize) + xCeiling2;
        if (value4 > sample[index5]) {
            sample[index5] = value4;
        }
    }

    public static float[] temporalSampling(GestureStroke stroke, int numPoints) {
        int i;
        GestureStroke gestureStroke = stroke;
        float increment = gestureStroke.length / ((float) (numPoints - 1));
        int vectorLength = numPoints * 2;
        float[] vector = new float[vectorLength];
        float distanceSoFar = 0.0f;
        float[] pts = gestureStroke.points;
        float lstPointX = pts[0];
        int i2 = 1;
        float lstPointY = pts[1];
        float currentPointX = Float.MIN_VALUE;
        float currentPointY = Float.MIN_VALUE;
        vector[0] = lstPointX;
        int index = 0 + 1;
        vector[index] = lstPointY;
        int index2 = index + 1;
        int i3 = 0;
        int count = pts.length / 2;
        while (true) {
            if (i3 >= count) {
                break;
            }
            if (currentPointX == Float.MIN_VALUE) {
                i3++;
                if (i3 >= count) {
                    int i4 = count;
                    break;
                }
                currentPointX = pts[i3 * 2];
                currentPointY = pts[(i3 * 2) + i2];
            }
            float deltaX = currentPointX - lstPointX;
            float deltaY = currentPointY - lstPointY;
            int i5 = i3;
            int count2 = count;
            float distance = (float) Math.hypot((double) deltaX, (double) deltaY);
            if (distanceSoFar + distance >= increment) {
                float ratio = (increment - distanceSoFar) / distance;
                float nx = (ratio * deltaX) + lstPointX;
                float ny = (ratio * deltaY) + lstPointY;
                vector[index2] = nx;
                int index3 = index2 + 1;
                vector[index3] = ny;
                i = 1;
                index2 = index3 + 1;
                lstPointX = nx;
                lstPointY = ny;
                distanceSoFar = 0.0f;
            } else {
                i = 1;
                float lstPointX2 = currentPointX;
                float lstPointX3 = currentPointY;
                distanceSoFar += distance;
                currentPointY = Float.MIN_VALUE;
                currentPointX = Float.MIN_VALUE;
                lstPointY = lstPointX3;
                lstPointX = lstPointX2;
            }
            i2 = i;
            count = count2;
            i3 = i5;
            GestureStroke gestureStroke2 = stroke;
        }
        for (int i6 = index2; i6 < vectorLength; i6 += 2) {
            vector[i6] = lstPointX;
            vector[i6 + 1] = lstPointY;
        }
        return vector;
    }

    static float[] computeCentroid(float[] points) {
        int count = points.length;
        float centerY = 0.0f;
        float centerX = 0.0f;
        int i = 0;
        while (i < count) {
            centerX += points[i];
            int i2 = i + 1;
            centerY += points[i2];
            i = i2 + 1;
        }
        return new float[]{(2.0f * centerX) / ((float) count), (2.0f * centerY) / ((float) count)};
    }

    private static float[][] computeCoVariance(float[] points) {
        float[][] array = (float[][]) Array.newInstance(float.class, new int[]{2, 2});
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
        float[] fArr = vector1;
        int i = numOrientations;
        int len = fArr.length;
        float a = 0.0f;
        float b = 0.0f;
        for (int i2 = 0; i2 < len; i2 += 2) {
            a += (fArr[i2] * vector2[i2]) + (fArr[i2 + 1] * vector2[i2 + 1]);
            b += (fArr[i2] * vector2[i2 + 1]) - (fArr[i2 + 1] * vector2[i2]);
        }
        if (a == 0.0f) {
            return 1.5707964f;
        }
        float tan = b / a;
        double angle = Math.atan((double) tan);
        if (i > 2 && Math.abs(angle) >= 3.141592653589793d / ((double) i)) {
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
        float[] fArr = points;
        translate(fArr, -centroid[0], -centroid[1]);
        float[] targetVector = computeOrientation(computeCoVariance(points));
        if (targetVector[0] == 0.0f && targetVector[1] == 0.0f) {
            angle = -1.5707964f;
        } else {
            angle = (float) Math.atan2((double) targetVector[1], (double) targetVector[0]);
            rotate(fArr, -angle);
        }
        float maxx = Float.MIN_VALUE;
        float maxy = Float.MIN_VALUE;
        int count = fArr.length;
        float miny = Float.MAX_VALUE;
        float minx = Float.MAX_VALUE;
        int i = 0;
        while (i < count) {
            if (fArr[i] < minx) {
                minx = fArr[i];
            }
            if (fArr[i] > maxx) {
                maxx = fArr[i];
            }
            int i2 = i + 1;
            if (fArr[i2] < miny) {
                miny = fArr[i2];
            }
            if (fArr[i2] > maxy) {
                maxy = fArr[i2];
            }
            i = i2 + 1;
        }
        OrientedBoundingBox orientedBoundingBox = new OrientedBoundingBox((float) (((double) (180.0f * angle)) / 3.141592653589793d), centroid[0], centroid[1], maxx - minx, maxy - miny);
        return orientedBoundingBox;
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
