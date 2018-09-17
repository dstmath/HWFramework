package android.media;

import android.net.ProxyInfo;
import android.os.ServiceSpecificException;

public class MediaCasStateException extends IllegalStateException {
    private final String mDiagnosticInfo;
    private final int mErrorCode;

    public MediaCasStateException(int err, String msg, String diagnosticInfo) {
        super(msg);
        this.mErrorCode = err;
        this.mDiagnosticInfo = diagnosticInfo;
    }

    static void throwExceptions(ServiceSpecificException e) {
        String diagnosticInfo = ProxyInfo.LOCAL_EXCL_LIST;
        switch (e.errorCode) {
            case MediaCasException.ERROR_DRM_TAMPER_DETECTED /*-2007*/:
                diagnosticInfo = "Tamper detected";
                break;
            case MediaCasException.ERROR_DRM_CANNOT_HANDLE /*-2006*/:
                diagnosticInfo = "Unsupported scheme or data format";
                break;
            case MediaCasException.ERROR_DRM_DECRYPT /*-2005*/:
                diagnosticInfo = "Decrypt error";
                break;
            case MediaCasException.ERROR_DRM_DECRYPT_UNIT_NOT_INITIALIZED /*-2004*/:
                diagnosticInfo = "Not initialized";
                break;
            case -2003:
                diagnosticInfo = "Session not opened";
                break;
            case -2002:
                diagnosticInfo = "License expired";
                break;
            case -2001:
                diagnosticInfo = "No license";
                break;
            case -2000:
                diagnosticInfo = "General CAS error";
                break;
            default:
                diagnosticInfo = "Unknown CAS state exception";
                break;
        }
        throw new MediaCasStateException(e.errorCode, e.getMessage(), String.format("%s (err=%d)", new Object[]{diagnosticInfo, Integer.valueOf(e.errorCode)}));
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public String getDiagnosticInfo() {
        return this.mDiagnosticInfo;
    }
}
