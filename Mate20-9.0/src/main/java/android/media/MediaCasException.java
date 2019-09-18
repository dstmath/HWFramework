package android.media;

public class MediaCasException extends Exception {

    public static final class DeniedByServerException extends MediaCasException {
        public DeniedByServerException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class NotProvisionedException extends MediaCasException {
        public NotProvisionedException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class ResourceBusyException extends MediaCasException {
        public ResourceBusyException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class UnsupportedCasException extends MediaCasException {
        public UnsupportedCasException(String detailMessage) {
            super(detailMessage);
        }
    }

    private MediaCasException(String detailMessage) {
        super(detailMessage);
    }

    static void throwExceptionIfNeeded(int error) throws MediaCasException {
        if (error != 0) {
            if (error == 7) {
                throw new NotProvisionedException(null);
            } else if (error == 8) {
                throw new ResourceBusyException(null);
            } else if (error != 11) {
                MediaCasStateException.throwExceptionIfNeeded(error);
            } else {
                throw new DeniedByServerException(null);
            }
        }
    }
}
