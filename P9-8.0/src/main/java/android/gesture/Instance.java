package android.gesture;

import android.hardware.camera2.params.TonemapCurve;

class Instance {
    private static final float[] ORIENTATIONS = new float[]{TonemapCurve.LEVEL_BLACK, 0.7853982f, 1.5707964f, 2.3561945f, 3.1415927f, TonemapCurve.LEVEL_BLACK, -0.7853982f, -1.5707964f, -2.3561945f, -3.1415927f};
    private static final int PATCH_SAMPLE_SIZE = 16;
    private static final int SEQUENCE_SAMPLE_SIZE = 16;
    final long id;
    final String label;
    final float[] vector;

    private Instance(long id, float[] sample, String sampleName) {
        this.id = id;
        this.vector = sample;
        this.label = sampleName;
    }

    private void normalize() {
        int i;
        float[] sample = this.vector;
        float sum = TonemapCurve.LEVEL_BLACK;
        int size = sample.length;
        for (i = 0; i < size; i++) {
            sum += sample[i] * sample[i];
        }
        float magnitude = (float) Math.sqrt((double) sum);
        for (i = 0; i < size; i++) {
            sample[i] = sample[i] / magnitude;
        }
    }

    static Instance createInstance(int sequenceType, int orientationType, Gesture gesture, String label) {
        if (sequenceType == 2) {
            Instance instance = new Instance(gesture.getID(), temporalSampler(orientationType, gesture), label);
            instance.normalize();
            return instance;
        }
        return new Instance(gesture.getID(), spatialSampler(gesture), label);
    }

    private static float[] spatialSampler(Gesture gesture) {
        return GestureUtils.spatialSampling(gesture, 16, false);
    }

    private static float[] temporalSampler(int orientationType, Gesture gesture) {
        float[] pts = GestureUtils.temporalSampling((GestureStroke) gesture.getStrokes().get(0), 16);
        float[] center = GestureUtils.computeCentroid(pts);
        float orientation = (float) Math.atan2((double) (pts[1] - center[1]), (double) (pts[0] - center[0]));
        float adjustment = -orientation;
        if (orientationType != 1) {
            for (float f : ORIENTATIONS) {
                float delta = f - orientation;
                if (Math.abs(delta) < Math.abs(adjustment)) {
                    adjustment = delta;
                }
            }
        }
        GestureUtils.translate(pts, -center[0], -center[1]);
        GestureUtils.rotate(pts, adjustment);
        return pts;
    }
}
