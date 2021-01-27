package ohos.net;

import android.net.Uri;
import ohos.miscservices.httpaccess.HttpConstant;

public class UriConverter {
    private UriConverter() {
    }

    public static Uri convertToAndroidContentUri(ohos.utils.net.Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null");
        } else if ("dataability".equals(uri.getScheme())) {
            String encodedPath = uri.getEncodedPath();
            if (encodedPath == null || !encodedPath.startsWith("/")) {
                throw new IllegalArgumentException("uri is illegal");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("content://");
            sb.append(encodedPath.substring(1));
            if (uri.getEncodedQuery() != null) {
                sb.append(HttpConstant.URL_PARAM_SEPARATOR);
                sb.append(uri.getEncodedQuery());
            }
            if (uri.getEncodedFragment() != null) {
                sb.append("#");
                sb.append(uri.getEncodedFragment());
            }
            return Uri.parse(sb.toString());
        } else {
            throw new IllegalArgumentException("scheme is not dataability");
        }
    }

    public static ohos.utils.net.Uri convertToZidaneContentUri(Uri uri, String str) {
        if (uri == null || str == null) {
            throw new NullPointerException("uri is null");
        } else if ("content".equals(uri.getScheme())) {
            StringBuilder sb = new StringBuilder();
            sb.append("dataability://");
            sb.append(str);
            if (uri.getEncodedSchemeSpecificPart() != null && uri.getEncodedSchemeSpecificPart().startsWith("//")) {
                sb.append(uri.getEncodedSchemeSpecificPart().substring(1));
            }
            if (uri.getEncodedFragment() != null) {
                sb.append("#");
                sb.append(uri.getEncodedFragment());
            }
            return ohos.utils.net.Uri.parse(sb.toString());
        } else {
            throw new IllegalArgumentException("scheme is not content");
        }
    }

    public static Uri convertToAndroidUri(ohos.utils.net.Uri uri) {
        if (uri != null) {
            return Uri.parse(uri.toString());
        }
        throw new NullPointerException("uri is null");
    }

    public static ohos.utils.net.Uri convertToZidaneUri(Uri uri) {
        if (uri != null) {
            return ohos.utils.net.Uri.parse(uri.toString());
        }
        throw new NullPointerException("uri is null");
    }
}
