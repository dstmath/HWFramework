package ohos.nfc;

public class NfcErrCodes {
    public static final int ERROR_INVALID_PARAM = -8;
    public static final int ERROR_IO = -1;
    public static final int SUCCESS = 0;

    public static boolean isError(int i) {
        return i < 0;
    }
}
