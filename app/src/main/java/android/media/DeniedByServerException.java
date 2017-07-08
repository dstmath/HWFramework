package android.media;

public final class DeniedByServerException extends MediaDrmException {
    public DeniedByServerException(String detailMessage) {
        super(detailMessage);
    }
}
