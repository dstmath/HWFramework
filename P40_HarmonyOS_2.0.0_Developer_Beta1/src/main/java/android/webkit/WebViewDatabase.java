package android.webkit;

import android.content.Context;

public abstract class WebViewDatabase {
    protected static final String LOGTAG = "webviewdatabase";

    @Deprecated
    public abstract void clearFormData();

    public abstract void clearHttpAuthUsernamePassword();

    @Deprecated
    public abstract void clearUsernamePassword();

    public abstract String[] getHttpAuthUsernamePassword(String str, String str2);

    @Deprecated
    public abstract boolean hasFormData();

    public abstract boolean hasHttpAuthUsernamePassword();

    @Deprecated
    public abstract boolean hasUsernamePassword();

    public abstract void setHttpAuthUsernamePassword(String str, String str2, String str3, String str4);

    public static WebViewDatabase getInstance(Context context) {
        return WebViewFactory.getProvider().getWebViewDatabase(context);
    }
}
