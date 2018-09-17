package android.webkit;

import android.content.Context;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.content.PackageHelper;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;

class LegacyErrorStrings {
    private static final String LOGTAG = "Http";

    private LegacyErrorStrings() {
    }

    static String getString(int errorCode, Context context) {
        return context.getText(getResource(errorCode)).toString();
    }

    private static int getResource(int errorCode) {
        switch (errorCode) {
            case WebViewClient.ERROR_TOO_MANY_REQUESTS /*-15*/:
                return R.string.httpErrorTooManyRequests;
            case WebViewClient.ERROR_FILE_NOT_FOUND /*-14*/:
                return R.string.httpErrorFileNotFound;
            case WebViewClient.ERROR_FILE /*-13*/:
                return R.string.httpErrorFile;
            case WebViewClient.ERROR_BAD_URL /*-12*/:
                return androidhwext.R.string.httpErrorBadUrl_Toast;
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE /*-11*/:
                return R.string.httpErrorFailedSslHandshake;
            case WebViewClient.ERROR_UNSUPPORTED_SCHEME /*-10*/:
                return R.string.httpErrorUnsupportedScheme;
            case WebViewClient.ERROR_REDIRECT_LOOP /*-9*/:
                return R.string.httpErrorRedirectLoop;
            case WebViewClient.ERROR_TIMEOUT /*-8*/:
                return R.string.httpErrorTimeout;
            case PackageHelper.RECOMMEND_FAILED_VERSION_DOWNGRADE /*-7*/:
                return R.string.httpErrorIO;
            case PackageHelper.RECOMMEND_FAILED_INVALID_URI /*-6*/:
                return R.string.httpErrorConnect;
            case PackageHelper.RECOMMEND_MEDIA_UNAVAILABLE /*-5*/:
                return R.string.httpErrorProxyAuth;
            case PackageHelper.RECOMMEND_FAILED_ALREADY_EXISTS /*-4*/:
                return R.string.httpErrorAuth;
            case HwPerformance.REQUEST_INPUT_INVALID /*-3*/:
                return R.string.httpErrorUnsupportedAuthScheme;
            case HwPerformance.REQUEST_PLATFORM_NOTSUPPORT /*-2*/:
                return R.string.httpErrorLookup;
            case PGSdk.TYPE_UNKNOW /*-1*/:
                return R.string.httpError;
            case HwCfgFilePolicy.GLOBAL /*0*/:
                return R.string.httpErrorOk;
            default:
                Log.w(LOGTAG, "Using generic message for unknown error code: " + errorCode);
                return R.string.httpError;
        }
    }
}
