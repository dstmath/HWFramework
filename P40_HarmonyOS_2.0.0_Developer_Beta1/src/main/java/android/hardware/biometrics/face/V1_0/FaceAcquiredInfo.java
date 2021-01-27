package android.hardware.biometrics.face.V1_0;

import java.util.ArrayList;

public final class FaceAcquiredInfo {
    public static final int FACE_OBSCURED = 19;
    public static final int FACE_TOO_HIGH = 6;
    public static final int FACE_TOO_LEFT = 9;
    public static final int FACE_TOO_LOW = 7;
    public static final int FACE_TOO_RIGHT = 8;
    public static final int GOOD = 0;
    public static final int INSUFFICIENT = 1;
    public static final int NOT_DETECTED = 11;
    public static final int PAN_TOO_EXTREME = 16;
    public static final int POOR_GAZE = 10;
    public static final int RECALIBRATE = 13;
    public static final int ROLL_TOO_EXTREME = 18;
    public static final int SENSOR_DIRTY = 21;
    public static final int START = 20;
    public static final int TILT_TOO_EXTREME = 17;
    public static final int TOO_BRIGHT = 2;
    public static final int TOO_CLOSE = 4;
    public static final int TOO_DARK = 3;
    public static final int TOO_DIFFERENT = 14;
    public static final int TOO_FAR = 5;
    public static final int TOO_MUCH_MOTION = 12;
    public static final int TOO_SIMILAR = 15;
    public static final int VENDOR = 22;

    public static final String toString(int o) {
        if (o == 0) {
            return "GOOD";
        }
        if (o == 1) {
            return "INSUFFICIENT";
        }
        if (o == 2) {
            return "TOO_BRIGHT";
        }
        if (o == 3) {
            return "TOO_DARK";
        }
        if (o == 4) {
            return "TOO_CLOSE";
        }
        if (o == 5) {
            return "TOO_FAR";
        }
        if (o == 6) {
            return "FACE_TOO_HIGH";
        }
        if (o == 7) {
            return "FACE_TOO_LOW";
        }
        if (o == 8) {
            return "FACE_TOO_RIGHT";
        }
        if (o == 9) {
            return "FACE_TOO_LEFT";
        }
        if (o == 10) {
            return "POOR_GAZE";
        }
        if (o == 11) {
            return "NOT_DETECTED";
        }
        if (o == 12) {
            return "TOO_MUCH_MOTION";
        }
        if (o == 13) {
            return "RECALIBRATE";
        }
        if (o == 14) {
            return "TOO_DIFFERENT";
        }
        if (o == 15) {
            return "TOO_SIMILAR";
        }
        if (o == 16) {
            return "PAN_TOO_EXTREME";
        }
        if (o == 17) {
            return "TILT_TOO_EXTREME";
        }
        if (o == 18) {
            return "ROLL_TOO_EXTREME";
        }
        if (o == 19) {
            return "FACE_OBSCURED";
        }
        if (o == 20) {
            return "START";
        }
        if (o == 21) {
            return "SENSOR_DIRTY";
        }
        if (o == 22) {
            return "VENDOR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("GOOD");
        if ((o & 1) == 1) {
            list.add("INSUFFICIENT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TOO_BRIGHT");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TOO_DARK");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TOO_CLOSE");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TOO_FAR");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("FACE_TOO_HIGH");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("FACE_TOO_LOW");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("FACE_TOO_RIGHT");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("FACE_TOO_LEFT");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("POOR_GAZE");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("NOT_DETECTED");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("TOO_MUCH_MOTION");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("RECALIBRATE");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("TOO_DIFFERENT");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("TOO_SIMILAR");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("PAN_TOO_EXTREME");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("TILT_TOO_EXTREME");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("ROLL_TOO_EXTREME");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("FACE_OBSCURED");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("START");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("SENSOR_DIRTY");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("VENDOR");
            flipped |= 22;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
