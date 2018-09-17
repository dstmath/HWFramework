package android.graphics;

public class PorterDuff {

    public enum Mode {
        CLEAR(0),
        SRC(1),
        DST(2),
        SRC_OVER(3),
        DST_OVER(4),
        SRC_IN(5),
        DST_IN(6),
        SRC_OUT(7),
        DST_OUT(8),
        SRC_ATOP(9),
        DST_ATOP(10),
        XOR(11),
        DARKEN(16),
        LIGHTEN(17),
        MULTIPLY(13),
        SCREEN(14),
        ADD(12),
        OVERLAY(15);
        
        public final int nativeInt;

        private Mode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public static int modeToInt(Mode mode) {
        return mode.nativeInt;
    }

    public static Mode intToMode(int val) {
        switch (val) {
            case 1:
                return Mode.SRC;
            case 2:
                return Mode.DST;
            case 3:
                return Mode.SRC_OVER;
            case 4:
                return Mode.DST_OVER;
            case 5:
                return Mode.SRC_IN;
            case 6:
                return Mode.DST_IN;
            case 7:
                return Mode.SRC_OUT;
            case 8:
                return Mode.DST_OUT;
            case 9:
                return Mode.SRC_ATOP;
            case 10:
                return Mode.DST_ATOP;
            case 11:
                return Mode.XOR;
            case 12:
                return Mode.ADD;
            case 13:
                return Mode.MULTIPLY;
            case 14:
                return Mode.SCREEN;
            case 15:
                return Mode.OVERLAY;
            case 16:
                return Mode.DARKEN;
            case 17:
                return Mode.LIGHTEN;
            default:
                return Mode.CLEAR;
        }
    }
}
