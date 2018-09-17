package android.webkit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView.PrivateAccess;

public interface WebViewFactoryProvider {

    public interface Statics {
        void clearClientCertPreferences(Runnable runnable);

        void enableSlowWholeDocumentDraw();

        String findAddress(String str);

        void freeMemoryForTests();

        String getDefaultUserAgent(Context context);

        Uri[] parseFileChooserResult(int i, Intent intent);

        void setWebContentsDebuggingEnabled(boolean z);
    }

    WebViewProvider createWebView(WebView webView, PrivateAccess privateAccess);

    CookieManager getCookieManager();

    GeolocationPermissions getGeolocationPermissions();

    ServiceWorkerController getServiceWorkerController();

    Statics getStatics();

    TokenBindingService getTokenBindingService();

    WebIconDatabase getWebIconDatabase();

    WebStorage getWebStorage();

    WebViewDatabase getWebViewDatabase(Context context);
}
