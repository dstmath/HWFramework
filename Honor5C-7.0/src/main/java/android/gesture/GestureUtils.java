package android.gesture;

import android.graphics.RectF;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public final class GestureUtils {
    private static final float NONUNIFORM_SCALE = 0.0f;
    private static final float SCALING_THRESHOLD = 0.26f;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.gesture.GestureUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.gesture.GestureUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.gesture.GestureUtils.<clinit>():void");
    }

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
        Arrays.fill(sample, NONUNIFORM_SCALE);
        RectF rect = gesture.getBoundingBox();
        float gestureWidth = rect.width();
        float gestureHeight = rect.height();
        float sx = targetPatchSize / gestureWidth;
        float sy = targetPatchSize / gestureHeight;
        float scale;
        if (keepAspectRatio) {
            if (sx < sy) {
                scale = sx;
            } else {
                scale = sy;
            }
            sx = scale;
            sy = scale;
        } else {
            float aspectRatio = gestureWidth / gestureHeight;
            if (aspectRatio > Engine.DEFAULT_VOLUME) {
                aspectRatio = Engine.DEFAULT_VOLUME / aspectRatio;
            }
            if (aspectRatio < SCALING_THRESHOLD) {
                if (sx < sy) {
                    scale = sx;
                } else {
                    scale = sy;
                }
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
            float segmentEndX = ScaledLayoutParams.SCALE_UNSPECIFIED;
            float segmentEndY = ScaledLayoutParams.SCALE_UNSPECIFIED;
            i = 0;
            while (i < size) {
                float segmentStartX = pts[i] < NONUNIFORM_SCALE ? NONUNIFORM_SCALE : pts[i];
                float segmentStartY = pts[i + 1] < NONUNIFORM_SCALE ? NONUNIFORM_SCALE : pts[i + 1];
                if (segmentStartX > targetPatchSize) {
                    segmentStartX = targetPatchSize;
                }
                if (segmentStartY > targetPatchSize) {
                    segmentStartY = targetPatchSize;
                }
                plot(segmentStartX, segmentStartY, sample, bitmapSize);
                if (segmentEndX != ScaledLayoutParams.SCALE_UNSPECIFIED) {
                    float slope;
                    float xpos;
                    if (segmentEndX > segmentStartX) {
                        slope = (segmentEndY - segmentStartY) / (segmentEndX - segmentStartX);
                        for (xpos = (float) Math.ceil((double) segmentStartX); xpos < segmentEndX; xpos += Engine.DEFAULT_VOLUME) {
                            plot(xpos, ((xpos - segmentStartX) * slope) + segmentStartY, sample, bitmapSize);
                        }
                    } else if (segmentEndX < segmentStartX) {
                        slope = (segmentEndY - segmentStartY) / (segmentEndX - segmentStartX);
                        for (xpos = (float) Math.ceil((double) segmentEndX); xpos < segmentStartX; xpos += Engine.DEFAULT_VOLUME) {
                            plot(xpos, ((xpos - segmentStartX) * slope) + segmentStartY, sample, bitmapSize);
                        }
                    }
                    float invertSlope;
                    float ypos;
                    if (segmentEndY > segmentStartY) {
                        invertSlope = (segmentEndX - segmentStartX) / (segmentEndY - segmentStartY);
                        for (ypos = (float) Math.ceil((double) segmentStartY); ypos < segmentEndY; ypos += Engine.DEFAULT_VOLUME) {
                            plot(((ypos - segmentStartY) * invertSlope) + segmentStartX, ypos, sample, bitmapSize);
                        }
                    } else if (segmentEndY < segmentStartY) {
                        invertSlope = (segmentEndX - segmentStartX) / (segmentEndY - segmentStartY);
                        for (ypos = (float) Math.ceil((double) segmentEndY); ypos < segmentStartY; ypos += Engine.DEFAULT_VOLUME) {
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
        int index;
        if (x < NONUNIFORM_SCALE) {
            x = NONUNIFORM_SCALE;
        }
        if (y < NONUNIFORM_SCALE) {
            y = NONUNIFORM_SCALE;
        }
        int xFloor = (int) Math.floor((double) x);
        int xCeiling = (int) Math.ceil((double) x);
        int yFloor = (int) Math.floor((double) y);
        int yCeiling = (int) Math.ceil((double) y);
        if (x == ((float) xFloor)) {
            if (y == ((float) yFloor)) {
                index = (yCeiling * sampleSize) + xCeiling;
                if (sample[index] < Engine.DEFAULT_VOLUME) {
                    sample[index] = Engine.DEFAULT_VOLUME;
                    return;
                }
                return;
            }
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
        float distanceSoFar = NONUNIFORM_SCALE;
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
                distanceSoFar = NONUNIFORM_SCALE;
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
        float centerX = NONUNIFORM_SCALE;
        float centerY = NONUNIFORM_SCALE;
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
        float sum = NONUNIFORM_SCALE;
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
        float squaredDistance = NONUNIFORM_SCALE;
        int size = vector1.length;
        for (int i = 0; i < size; i++) {
            float difference = vector1[i] - vector2[i];
            squaredDistance += difference * difference;
        }
        return squaredDistance / ((float) size);
    }

    static float cosineDistance(float[] vector1, float[] vector2) {
        float sum = NONUNIFORM_SCALE;
        for (int i = 0; i < vector1.length; i++) {
            sum += vector1[i] * vector2[i];
        }
        return (float) Math.acos((double) sum);
    }

    static float minimumCosineDistance(float[] vector1, float[] vector2, int numOrientations) {
        int len = vector1.length;
        float a = NONUNIFORM_SCALE;
        float b = NONUNIFORM_SCALE;
        for (int i = 0; i < len; i += 2) {
            a += (vector1[i] * vector2[i]) + (vector1[i + 1] * vector2[i + 1]);
            b += (vector1[i] * vector2[i + 1]) - (vector1[i + 1] * vector2[i]);
        }
        if (a == NONUNIFORM_SCALE) {
            return 1.5707964f;
        }
        float tan = b / a;
        double angle = Math.atan((double) tan);
        if (numOrientations > 2) {
            if (Math.abs(angle) >= 3.141592653589793d / ((double) numOrientations)) {
                return (float) Math.acos((double) a);
            }
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
        if (targetVector[0] == NONUNIFORM_SCALE && targetVector[1] == NONUNIFORM_SCALE) {
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
        if (covarianceMatrix[0][1] == NONUNIFORM_SCALE || covarianceMatrix[1][0] == NONUNIFORM_SCALE) {
            targetVector[0] = Engine.DEFAULT_VOLUME;
            targetVector[1] = NONUNIFORM_SCALE;
        }
        float value = ((-covarianceMatrix[0][0]) - covarianceMatrix[1][1]) / 2.0f;
        float rightside = (float) Math.sqrt(Math.pow((double) value, 2.0d) - ((double) ((covarianceMatrix[0][0] * covarianceMatrix[1][1]) - (covarianceMatrix[0][1] * covarianceMatrix[1][0]))));
        float lambda1 = (-value) + rightside;
        float lambda2 = (-value) - rightside;
        if (lambda1 == lambda2) {
            targetVector[0] = NONUNIFORM_SCALE;
            targetVector[1] = NONUNIFORM_SCALE;
        } else {
            float lambda = lambda1 > lambda2 ? lambda1 : lambda2;
            targetVector[0] = Engine.DEFAULT_VOLUME;
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
