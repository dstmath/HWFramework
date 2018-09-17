package android.webkit;

import android.net.WebAddress;

public abstract class CookieManager {
    public abstract boolean acceptCookie();

    public abstract boolean acceptThirdPartyCookies(WebView webView);

    protected abstract boolean allowFileSchemeCookiesImpl();

    public abstract void flush();

    public abstract String getCookie(String str);

    public abstract String getCookie(String str, boolean z);

    public abstract boolean hasCookies();

    public abstract boolean hasCookies(boolean z);

    @Deprecated
    public abstract void removeAllCookie();

    public abstract void removeAllCookies(ValueCallback<Boolean> valueCallback);

    @Deprecated
    public abstract void removeExpiredCookie();

    public abstract void removeSessionCookie();

    public abstract void removeSessionCookies(ValueCallback<Boolean> valueCallback);

    public abstract void setAcceptCookie(boolean z);

    protected abstract void setAcceptFileSchemeCookiesImpl(boolean z);

    public abstract void setAcceptThirdPartyCookies(WebView webView, boolean z);

    public abstract void setCookie(String str, String str2);

    public abstract void setCookie(String str, String str2, ValueCallback<Boolean> valueCallback);

    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("doesn't implement Cloneable");
    }

    public static synchronized CookieManager getInstance() {
        CookieManager cookieManager;
        synchronized (CookieManager.class) {
            cookieManager = WebViewFactory.getProvider().getCookieManager();
        }
        return cookieManager;
    }

    public synchronized String getCookie(WebAddress uri) {
        return getCookie(uri.toString());
    }

    public static boolean allowFileSchemeCookies() {
        return getInstance().allowFileSchemeCookiesImpl();
    }

    public static void setAcceptFileSchemeCookies(boolean accept) {
        getInstance().setAcceptFileSchemeCookiesImpl(accept);
    }
}
