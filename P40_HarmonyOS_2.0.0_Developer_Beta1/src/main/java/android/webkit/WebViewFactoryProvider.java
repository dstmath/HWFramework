package android.webkit;

import android.annotation.SystemApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import java.util.List;

@SystemApi
public interface WebViewFactoryProvider {

    public interface Statics {
        void clearClientCertPreferences(Runnable runnable);

        void enableSlowWholeDocumentDraw();

        String findAddress(String str);

        void freeMemoryForTests();

        String getDefaultUserAgent(Context context);

        Uri getSafeBrowsingPrivacyPolicyUrl();

        void initSafeBrowsing(Context context, ValueCallback<Boolean> valueCallback);

        Uri[] parseFileChooserResult(int i, Intent intent);

        void setSafeBrowsingWhitelist(List<String> list, ValueCallback<Boolean> valueCallback);

        void setWebContentsDebuggingEnabled(boolean z);
    }

    WebViewProvider createWebView(WebView webView, WebView.PrivateAccess privateAccess);

    CookieManager getCookieManager();

    GeolocationPermissions getGeolocationPermissions();

    ServiceWorkerController getServiceWorkerController();

    Statics getStatics();

    TokenBindingService getTokenBindingService();

    TracingController getTracingController();

    WebIconDatabase getWebIconDatabase();

    WebStorage getWebStorage();

    ClassLoader getWebViewClassLoader();

    WebViewDatabase getWebViewDatabase(Context context);
}
