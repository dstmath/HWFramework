package android.media;

import android.os.ServiceSpecificException;

public class MediaCasException extends Exception {
    public static final int DRM_ERROR_BASE = -2000;
    public static final int ERROR_DRM_CANNOT_HANDLE = -2006;
    public static final int ERROR_DRM_DECRYPT = -2005;
    public static final int ERROR_DRM_DECRYPT_UNIT_NOT_INITIALIZED = -2004;
    public static final int ERROR_DRM_DEVICE_REVOKED = -2009;
    public static final int ERROR_DRM_INSUFFICIENT_OUTPUT_PROTECTION = -2011;
    public static final int ERROR_DRM_LAST_USED_ERRORCODE = -2011;
    public static final int ERROR_DRM_LICENSE_EXPIRED = -2002;
    public static final int ERROR_DRM_NOT_PROVISIONED = -2008;
    public static final int ERROR_DRM_NO_LICENSE = -2001;
    public static final int ERROR_DRM_RESOURCE_BUSY = -2010;
    public static final int ERROR_DRM_SESSION_NOT_OPENED = -2003;
    public static final int ERROR_DRM_TAMPER_DETECTED = -2007;
    public static final int ERROR_DRM_UNKNOWN = -2000;
    public static final int ERROR_DRM_VENDOR_MAX = -2500;
    public static final int ERROR_DRM_VENDOR_MIN = -2999;

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

    public MediaCasException(String detailMessage) {
        super(detailMessage);
    }

    static void throwExceptions(ServiceSpecificException e) throws MediaCasException {
        if (e.errorCode == ERROR_DRM_NOT_PROVISIONED) {
            throw new NotProvisionedException(e.getMessage());
        } else if (e.errorCode == ERROR_DRM_RESOURCE_BUSY) {
            throw new ResourceBusyException(e.getMessage());
        } else if (e.errorCode == ERROR_DRM_DEVICE_REVOKED) {
            throw new DeniedByServerException(e.getMessage());
        } else {
            MediaCasStateException.throwExceptions(e);
        }
    }
}
