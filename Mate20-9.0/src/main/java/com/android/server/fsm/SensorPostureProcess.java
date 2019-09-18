package com.android.server.fsm;

public class SensorPostureProcess {
    private static final float EXPAND_LOWER_THRESHOLD = 145.0f;
    private static final float FOLDED_UPPER_THRESHOLD = 35.0f;
    private static final float G = 9.8f;
    private static final float HALF_FOLDED_LOWER_THRESHOLD = 20.0f;
    private static final float HALF_FOLDED_UPPER_THRESHOLD = 160.0f;
    private static final float PORTAIT_THRESHOLD = 9.5f;
    private static final String TAG = "Fsm_SensorPostureProcess";

    public static boolean isPortraitState(float[] data) {
        float gravityMainY = data[1];
        float gravitySubY = data[4];
        if ((gravityMainY < PORTAIT_THRESHOLD || gravitySubY < PORTAIT_THRESHOLD) && (gravityMainY > -9.5f || gravitySubY > -9.5f)) {
            return false;
        }
        return true;
    }

    public static int handlePostureSensor(float[] data, int currentPosture) {
        float angle = data[6];
        if (angle >= HALF_FOLDED_UPPER_THRESHOLD) {
            return 109;
        }
        if (angle <= HALF_FOLDED_LOWER_THRESHOLD) {
            return getFoldedPosture(data);
        }
        if (angle >= FOLDED_UPPER_THRESHOLD && angle <= EXPAND_LOWER_THRESHOLD) {
            return 106;
        }
        if (angle <= EXPAND_LOWER_THRESHOLD || angle >= HALF_FOLDED_UPPER_THRESHOLD) {
            if (currentPosture == 109 || currentPosture == 106) {
                return 106;
            }
            return getFoldedPosture(data);
        } else if (currentPosture == 109) {
            return 109;
        } else {
            return 106;
        }
    }

    private static int getFoldedPosture(float[] data) {
        float gravityMainZ = data[2];
        float gravitySubZ = data[5];
        if (isProbablyEqual(gravityMainZ, G) && isProbablyEqual(gravitySubZ, -9.8f)) {
            return 101;
        }
        if (!isProbablyEqual(gravityMainZ, -9.8f) || !isProbablyEqual(gravitySubZ, G)) {
            return 103;
        }
        return 102;
    }

    private static boolean isProbablyEqual(float data, float standardData) {
        return ((double) Math.abs(data - standardData)) < 0.1d;
    }

    public static int getFoldableState(float angle, int currentState) {
        int i = currentState;
        if (angle <= HALF_FOLDED_LOWER_THRESHOLD) {
            return 2;
        }
        if (angle >= HALF_FOLDED_UPPER_THRESHOLD) {
            return 1;
        }
        if (angle >= FOLDED_UPPER_THRESHOLD && angle <= EXPAND_LOWER_THRESHOLD) {
            return 3;
        }
        if (angle <= HALF_FOLDED_LOWER_THRESHOLD || angle >= FOLDED_UPPER_THRESHOLD) {
            if (currentState == 1) {
                return 1;
            }
            return 3;
        } else if (currentState == 2) {
            return 2;
        } else {
            return 3;
        }
    }
}
