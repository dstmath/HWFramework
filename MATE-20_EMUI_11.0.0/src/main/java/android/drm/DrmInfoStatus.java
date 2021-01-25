package android.drm;

public class DrmInfoStatus {
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_OK = 1;
    public final ProcessedData data;
    public final int infoType;
    public final String mimeType;
    public final int statusCode;

    public DrmInfoStatus(int statusCode2, int infoType2, ProcessedData data2, String mimeType2) {
        if (!DrmInfoRequest.isValidType(infoType2)) {
            throw new IllegalArgumentException("infoType: " + infoType2);
        } else if (!isValidStatusCode(statusCode2)) {
            throw new IllegalArgumentException("Unsupported status code: " + statusCode2);
        } else if (mimeType2 == null || mimeType2 == "") {
            throw new IllegalArgumentException("mimeType is null or an empty string");
        } else {
            this.statusCode = statusCode2;
            this.infoType = infoType2;
            this.data = data2;
            this.mimeType = mimeType2;
        }
    }

    private boolean isValidStatusCode(int statusCode2) {
        return statusCode2 == 1 || statusCode2 == 2;
    }
}
