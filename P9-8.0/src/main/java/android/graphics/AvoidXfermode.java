package android.graphics;

@Deprecated
public class AvoidXfermode extends Xfermode {

    public enum Mode {
        AVOID(0),
        TARGET(1);
        
        final int nativeInt;

        private Mode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public AvoidXfermode(int opColor, int tolerance, Mode mode) {
        if (tolerance < 0 || tolerance > 255) {
            throw new IllegalArgumentException("tolerance must be 0..255");
        }
    }
}
