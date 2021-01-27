package android.webkit;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@Deprecated
public class WebViewFragment extends Fragment {
    private boolean mIsWebViewAvailable;
    private WebView mWebView;

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WebView webView = this.mWebView;
        if (webView != null) {
            webView.destroy();
        }
        this.mWebView = new WebView(getContext());
        this.mIsWebViewAvailable = true;
        return this.mWebView;
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mWebView.onPause();
    }

    @Override // android.app.Fragment
    public void onResume() {
        this.mWebView.onResume();
        super.onResume();
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        this.mIsWebViewAvailable = false;
        super.onDestroyView();
    }

    @Override // android.app.Fragment
    public void onDestroy() {
        WebView webView = this.mWebView;
        if (webView != null) {
            webView.destroy();
            this.mWebView = null;
        }
        super.onDestroy();
    }

    public WebView getWebView() {
        if (this.mIsWebViewAvailable) {
            return this.mWebView;
        }
        return null;
    }
}
