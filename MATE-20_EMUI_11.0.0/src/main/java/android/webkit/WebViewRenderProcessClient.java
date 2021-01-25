package android.webkit;

public abstract class WebViewRenderProcessClient {
    public abstract void onRenderProcessResponsive(WebView webView, WebViewRenderProcess webViewRenderProcess);

    public abstract void onRenderProcessUnresponsive(WebView webView, WebViewRenderProcess webViewRenderProcess);
}
