package android.gesture;

class Instance {
    private static final float[] ORIENTATIONS = {0.0f, 0.7853982f, 1.5707964f, 2.3561945f, 3.1415927f, 0.0f, -0.7853982f, -1.5707964f, -2.3561945f, -3.1415927f};
    private static final int PATCH_SAMPLE_SIZE = 16;
    private static final int SEQUENCE_SAMPLE_SIZE = 16;
    final long id;
    final String label;
    final float[] vector;

    private Instance(long id2, float[] sample, String sampleName) {
        this.id = id2;
        this.vector = sample;
        this.label = sampleName;
    }

    private void normalize() {
        float[] sample = this.vector;
        int size = sample.length;
        float sum = 0.0f;
        for (int i = 0; i < size; i++) {
            sum += sample[i] * sample[i];
        }
        float magnitude = (float) Math.sqrt((double) sum);
        for (int i2 = 0; i2 < size; i2++) {
            sample[i2] = sample[i2] / magnitude;
        }
    }

    static Instance createInstance(int sequenceType, int orientationType, Gesture gesture, String label2) {
        if (sequenceType == 2) {
            Instance instance = new Instance(gesture.getID(), temporalSampler(orientationType, gesture), label2);
            instance.normalize();
            return instance;
        }
        return new Instance(gesture.getID(), spatialSampler(gesture), label2);
    }

    private static float[] spatialSampler(Gesture gesture) {
        return GestureUtils.spatialSampling(gesture, 16, false);
    }

    private static float[] temporalSampler(int orientationType, Gesture gesture) {
        float[] pts = GestureUtils.temporalSampling(gesture.getStrokes().get(0), 16);
        float[] center = GestureUtils.computeCentroid(pts);
        float orientation = (float) Math.atan2((double) (pts[1] - center[1]), (double) (pts[0] - center[0]));
        float adjustment = -orientation;
        if (orientationType != 1) {
            float adjustment2 = adjustment;
            for (float f : ORIENTATIONS) {
                float delta = f - orientation;
                if (Math.abs(delta) < Math.abs(adjustment2)) {
                    adjustment2 = delta;
                }
            }
            adjustment = adjustment2;
        }
        GestureUtils.translate(pts, -center[0], -center[1]);
        GestureUtils.rotate(pts, adjustment);
        return pts;
    }
}
