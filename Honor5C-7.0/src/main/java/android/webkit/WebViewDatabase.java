package android.webkit;

import android.content.Context;

public abstract class WebViewDatabase {
    protected static final String LOGTAG = "webviewdatabase";

    public abstract void clearFormData();

    public abstract void clearHttpAuthUsernamePassword();

    @Deprecated
    public abstract void clearUsernamePassword();

    public abstract boolean hasFormData();

    public abstract boolean hasHttpAuthUsernamePassword();

    @Deprecated
    public abstract boolean hasUsernamePassword();

    public static WebViewDatabase getInstance(Context context) {
        return WebViewFactory.getProvider().getWebViewDatabase(context);
    }
}
