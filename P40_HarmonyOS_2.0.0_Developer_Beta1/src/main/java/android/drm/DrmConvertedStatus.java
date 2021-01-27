package android.drm;

public class DrmConvertedStatus {
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_INPUTDATA_ERROR = 2;
    public static final int STATUS_OK = 1;
    public final byte[] convertedData;
    public final int offset;
    public final int statusCode;

    public DrmConvertedStatus(int statusCode2, byte[] convertedData2, int offset2) {
        if (isValidStatusCode(statusCode2)) {
            this.statusCode = statusCode2;
            this.convertedData = convertedData2;
            this.offset = offset2;
            return;
        }
        throw new IllegalArgumentException("Unsupported status code: " + statusCode2);
    }

    private boolean isValidStatusCode(int statusCode2) {
        return statusCode2 == 1 || statusCode2 == 2 || statusCode2 == 3;
    }
}
