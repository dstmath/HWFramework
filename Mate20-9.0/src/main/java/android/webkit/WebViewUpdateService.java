package android.webkit;

import android.annotation.SystemApi;
import android.os.RemoteException;

@SystemApi
public final class WebViewUpdateService {
    private WebViewUpdateService() {
    }

    public static WebViewProviderInfo[] getAllWebViewPackages() {
        IWebViewUpdateService service = getUpdateService();
        if (service == null) {
            return new WebViewProviderInfo[0];
        }
        try {
            return service.getAllWebViewPackages();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static WebViewProviderInfo[] getValidWebViewPackages() {
        IWebViewUpdateService service = getUpdateService();
        if (service == null) {
            return new WebViewProviderInfo[0];
        }
        try {
            return service.getValidWebViewPackages();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String getCurrentWebViewPackageName() {
        IWebViewUpdateService service = getUpdateService();
        if (service == null) {
            return null;
        }
        try {
            return service.getCurrentWebViewPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static IWebViewUpdateService getUpdateService() {
        return WebViewFactory.getUpdateService();
    }
}
