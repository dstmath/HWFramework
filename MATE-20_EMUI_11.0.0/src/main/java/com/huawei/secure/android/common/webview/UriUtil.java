package com.huawei.secure.android.common.webview;

import android.annotation.TargetApi;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import com.huawei.secure.android.common.util.LogsUtil;
import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {
    private static final String TAG = "UriUtil";

    public static boolean isUrlHostInWhitelist(String url, String[] whiteListUrlOrHost) {
        if (whiteListUrlOrHost == null || whiteListUrlOrHost.length == 0) {
            LogsUtil.e(TAG, "whitelist is null");
            return false;
        }
        for (String item : whiteListUrlOrHost) {
            if (isUrlHostMatchWhitelist(url, item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUrlHostMatchWhitelist(String url, String whiteListUrlOrHost) {
        String urlHost = getHostByURI(url);
        if (TextUtils.isEmpty(urlHost) || TextUtils.isEmpty(whiteListUrlOrHost)) {
            LogsUtil.e(TAG, "url or whitelist is null");
            return false;
        }
        String safeHost = getWhiteListHost(whiteListUrlOrHost);
        if (TextUtils.isEmpty(safeHost)) {
            Log.e(TAG, "whitelist host is null");
            return false;
        } else if (safeHost.equals(urlHost)) {
            return true;
        } else {
            if (!urlHost.endsWith(safeHost)) {
                return false;
            }
            try {
                String hostPrefix = urlHost.substring(0, urlHost.length() - safeHost.length());
                if (!hostPrefix.endsWith(".")) {
                    return false;
                }
                return hostPrefix.matches("^[A-Za-z0-9.-]+$");
            } catch (IndexOutOfBoundsException e) {
                LogsUtil.e(TAG, "IndexOutOfBoundsException" + e.getMessage());
                return false;
            } catch (Exception e2) {
                LogsUtil.e(TAG, "Exception : " + e2.getMessage());
                return false;
            }
        }
    }

    @TargetApi(9)
    public static String getHostByURI(String url) {
        if (TextUtils.isEmpty(url)) {
            LogsUtil.i(TAG, "url is null");
            return url;
        }
        try {
            if (URLUtil.isNetworkUrl(url)) {
                return new URI(url).getHost();
            }
            LogsUtil.e(TAG, "url don't starts with http or https");
            return null;
        } catch (URISyntaxException e) {
            LogsUtil.e(TAG, "getHostByURI error : " + e.getMessage());
            return null;
        }
    }

    private static String getWhiteListHost(String whiteListUrl) {
        if (TextUtils.isEmpty(whiteListUrl)) {
            LogsUtil.i(TAG, "whiteListUrl is null");
            return null;
        } else if (!URLUtil.isNetworkUrl(whiteListUrl)) {
            return whiteListUrl;
        } else {
            return getHostByURI(whiteListUrl);
        }
    }

    public static boolean isUrlHostAndPathInWhitelist(String url, String[] whiteListUrl) {
        if (whiteListUrl == null || whiteListUrl.length == 0) {
            LogsUtil.e(TAG, "whitelist is null");
            return false;
        }
        for (String item : whiteListUrl) {
            if (isUrlHostAndPathMatchWhitelist(url, item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUrlHostAndPathMatchWhitelist(String url, String whiteListUrl) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(whiteListUrl)) {
            return false;
        }
        if (url.contains("..") || url.contains("@")) {
            Log.e(TAG, "url contains unsafe char");
            return false;
        }
        if (!whiteListUrl.equals(url)) {
            if (!url.startsWith(whiteListUrl + "?")) {
                if (!url.startsWith(whiteListUrl + "#")) {
                    if (!whiteListUrl.endsWith("/")) {
                        return false;
                    }
                    if (Uri.parse(url).getPathSegments().size() - Uri.parse(whiteListUrl).getPathSegments().size() != 1) {
                        return false;
                    }
                    return url.startsWith(whiteListUrl);
                }
            }
        }
        return true;
    }
}
