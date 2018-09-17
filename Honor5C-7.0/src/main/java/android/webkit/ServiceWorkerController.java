package android.webkit;

public abstract class ServiceWorkerController {
    public abstract ServiceWorkerWebSettings getServiceWorkerWebSettings();

    public abstract void setServiceWorkerClient(ServiceWorkerClient serviceWorkerClient);

    public static ServiceWorkerController getInstance() {
        return WebViewFactory.getProvider().getServiceWorkerController();
    }
}
