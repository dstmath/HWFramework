package com.huawei.secure.android.common.webview;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.huawei.secure.android.common.util.LogsUtil;
import com.huawei.secure.android.common.webview.WebViewLoadCallBack;
import java.util.Arrays;
import java.util.Map;

public class SafeWebView extends WebView {
    private static final String TAG = "SafeWebView";
    private String defaultErrorPage;
    private WebViewLoadCallBack webViewLoadCallBack;
    private String[] whitelist;
    private String[] whitelistWithPath;

    public SafeWebView(Context context) {
        super(context);
        initWebviewSettings();
    }

    public SafeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebviewSettings();
    }

    public SafeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebviewSettings();
    }

    @TargetApi(21)
    public SafeWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWebviewSettings();
    }

    @TargetApi(9)
    public String[] getWhitelist() {
        String[] strArr = this.whitelist;
        if (strArr == null) {
            return null;
        }
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    @TargetApi(9)
    public void setWhitelist(String[] whitelist2) {
        this.whitelist = whitelist2 == null ? null : (String[]) Arrays.copyOf(whitelist2, whitelist2.length);
    }

    @TargetApi(9)
    public String[] getWhitelistWithPath() {
        String[] strArr = this.whitelistWithPath;
        if (strArr == null) {
            return null;
        }
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    @TargetApi(9)
    public void setWhitelistWithPath(String[] whitelistWithPath2) {
        this.whitelistWithPath = whitelistWithPath2 == null ? null : (String[]) Arrays.copyOf(whitelistWithPath2, whitelistWithPath2.length);
    }

    public String getDefaultErrorPage() {
        return this.defaultErrorPage;
    }

    public void setDefaultErrorPage(String url) {
        this.defaultErrorPage = url;
    }

    @Override // android.webkit.WebView
    public void loadUrl(String url) {
        if (isHttpUrl(url)) {
            Log.e(TAG, "loadUrl: http url , not safe");
            if (!TextUtils.isEmpty(this.defaultErrorPage)) {
                super.loadUrl(this.defaultErrorPage);
            } else if (getWebViewLoadCallBack() != null) {
                Log.e(TAG, "WebViewLoadCallBack");
                getWebViewLoadCallBack().onCheckError(url, WebViewLoadCallBack.ErrorCode.HTTP_URL);
            }
        } else {
            super.loadUrl(url);
        }
    }

    @Override // android.webkit.WebView
    public void postUrl(String url, byte[] postData) {
        if (isHttpUrl(url)) {
            Log.e(TAG, "postUrl: http url , not safe");
            if (!TextUtils.isEmpty(this.defaultErrorPage)) {
                super.postUrl(this.defaultErrorPage, postData);
            } else if (getWebViewLoadCallBack() != null) {
                Log.e(TAG, "WebViewLoadCallBack");
                getWebViewLoadCallBack().onCheckError(url, WebViewLoadCallBack.ErrorCode.HTTP_URL);
            }
        } else {
            super.postUrl(url, postData);
        }
    }

    @Override // android.webkit.WebView
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (isHttpUrl(url)) {
            Log.e(TAG, "loadUrl: http url , not safe");
            if (!TextUtils.isEmpty(this.defaultErrorPage)) {
                super.loadUrl(this.defaultErrorPage, additionalHttpHeaders);
            } else if (getWebViewLoadCallBack() != null) {
                Log.e(TAG, "WebViewLoadCallBack");
                getWebViewLoadCallBack().onCheckError(url, WebViewLoadCallBack.ErrorCode.HTTP_URL);
            }
        } else {
            super.loadUrl(url, additionalHttpHeaders);
        }
    }

    @Override // android.webkit.WebView
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        if (isHttpUrl(baseUrl)) {
            Log.e(TAG, "loadDataWithBaseURL: http url , not safe");
            if (!TextUtils.isEmpty(this.defaultErrorPage)) {
                super.loadDataWithBaseURL(this.defaultErrorPage, data, mimeType, encoding, historyUrl);
            } else if (getWebViewLoadCallBack() != null) {
                Log.e(TAG, "WebViewLoadCallBack");
                getWebViewLoadCallBack().onCheckError(baseUrl, WebViewLoadCallBack.ErrorCode.HTTP_URL);
            }
        } else {
            super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
        }
    }

    private boolean isHttpUrl(String url) {
        return URLUtil.isHttpUrl(url);
    }

    @Override // android.webkit.WebView
    public void setWebViewClient(WebViewClient client) {
        super.setWebViewClient(new SafeWebviewClientWrapper(client, true));
    }

    public void setWebViewClient(WebViewClient client, boolean useDefaultSafeWebViewClient) {
        super.setWebViewClient(new SafeWebviewClientWrapper(client, useDefaultSafeWebViewClient));
    }

    private void initWebviewSettings() {
        SafeWebSettings.initWebviewAndSettings(this);
        setWebViewClient(null);
    }

    @TargetApi(9)
    public boolean isWhiteListUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            LogsUtil.e(TAG, "url is null");
            return false;
        } else if (!URLUtil.isNetworkUrl(url)) {
            return true;
        } else {
            String[] whiteWithPath = getWhitelistWithPath();
            String[] white = getWhitelist();
            if (whiteWithPath == null || whiteWithPath.length == 0) {
                return UriUtil.isUrlHostInWhitelist(url, white);
            }
            return UriUtil.isUrlHostAndPathInWhitelist(url, whiteWithPath);
        }
    }

    public final void onCheckError(WebView view, String url) {
        LogsUtil.e(TAG, "onCheckError url is not in white list ", url);
        view.stopLoading();
        String defaultUrl = getDefaultErrorPage();
        if (!TextUtils.isEmpty(defaultUrl)) {
            view.loadUrl(defaultUrl);
        } else if (getWebViewLoadCallBack() != null) {
            Log.e(TAG, "onPageStarted WebViewLoadCallBack");
            getWebViewLoadCallBack().onCheckError(url, WebViewLoadCallBack.ErrorCode.URL_NOT_IN_WHITE_LIST);
        }
    }

    /* access modifiers changed from: protected */
    public final void showNoticeWhenSSLErrorOccurred(@Nullable String title, @NonNull String tips, @NonNull String posiviceButton, @NonNull String negaticeButton, @NonNull SslErrorHandler handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        DialogInterface.OnClickListener positiveListener = new PositiveOnClickListener(handler);
        DialogInterface.OnClickListener negativeListener = new NegativeOnClickListener(handler);
        builder.setMessage(tips);
        builder.setPositiveButton(posiviceButton, positiveListener);
        builder.setNegativeButton(negaticeButton, negativeListener);
        builder.create().show();
    }

    public WebViewLoadCallBack getWebViewLoadCallBack() {
        return this.webViewLoadCallBack;
    }

    public void setWebViewLoadCallBack(WebViewLoadCallBack webViewLoadCallBack2) {
        this.webViewLoadCallBack = webViewLoadCallBack2;
    }

    private static class PositiveOnClickListener implements DialogInterface.OnClickListener {
        private final SslErrorHandler handler;

        PositiveOnClickListener(SslErrorHandler handler2) {
            this.handler = handler2;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            this.handler.proceed();
        }
    }

    private static class NegativeOnClickListener implements DialogInterface.OnClickListener {
        private final SslErrorHandler handler;

        NegativeOnClickListener(SslErrorHandler handler2) {
            this.handler = handler2;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            this.handler.cancel();
        }
    }

    /* access modifiers changed from: private */
    public final class SafeWebviewClientWrapper extends WebViewClient {
        private WebViewClient mWebviewClient;
        private boolean useDefaultSafeWebViewClient;

        private SafeWebviewClientWrapper(WebViewClient webViewClient, boolean useDefaultSafeWebViewClient2) {
            this.mWebviewClient = webViewClient;
            this.useDefaultSafeWebViewClient = useDefaultSafeWebViewClient2;
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(21)
        @Nullable
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                return webViewClient.shouldInterceptRequest(view, request);
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(11)
        @Nullable
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                return webViewClient.shouldInterceptRequest(view, url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                return webViewClient.shouldOverrideUrlLoading(view, url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(24)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                return webViewClient.shouldOverrideUrlLoading(view, request);
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override // android.webkit.WebViewClient
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null && !this.useDefaultSafeWebViewClient) {
                webViewClient.onPageStarted(view, url, favicon);
            } else if (!SafeWebView.this.isWhiteListUrl(url)) {
                SafeWebView.this.onCheckError(view, url);
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView view, String url) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onPageFinished(view, url);
            } else {
                super.onPageFinished(view, url);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onLoadResource(WebView view, String url) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onLoadResource(view, url);
            } else {
                super.onLoadResource(view, url);
            }
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(23)
        public void onPageCommitVisible(WebView view, String url) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onPageCommitVisible(view, url);
            } else {
                super.onPageCommitVisible(view, url);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
            } else {
                super.onTooManyRedirects(view, cancelMsg, continueMsg);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedError(view, errorCode, description, failingUrl);
            } else {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(23)
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedError(view, request, error);
            } else {
                super.onReceivedError(view, request, error);
            }
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(23)
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedHttpError(view, request, errorResponse);
            } else {
                super.onReceivedHttpError(view, request, errorResponse);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onFormResubmission(view, dontResend, resend);
            } else {
                super.onFormResubmission(view, dontResend, resend);
            }
        }

        @Override // android.webkit.WebViewClient
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.doUpdateVisitedHistory(view, url, isReload);
            } else {
                super.doUpdateVisitedHistory(view, url, isReload);
            }
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(21)
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedClientCertRequest(view, request);
            } else {
                super.onReceivedClientCertRequest(view, request);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
            } else {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
            }
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                return webViewClient.shouldOverrideKeyEvent(view, event);
            }
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override // android.webkit.WebViewClient
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onUnhandledKeyEvent(view, event);
            } else {
                super.onUnhandledKeyEvent(view, event);
            }
        }

        @Override // android.webkit.WebViewClient
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onScaleChanged(view, oldScale, newScale);
            } else {
                super.onScaleChanged(view, oldScale, newScale);
            }
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(12)
        public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedLoginRequest(view, realm, account, args);
            } else {
                super.onReceivedLoginRequest(view, realm, account, args);
            }
        }

        @Override // android.webkit.WebViewClient
        @TargetApi(8)
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            WebViewClient webViewClient = this.mWebviewClient;
            if (webViewClient != null) {
                webViewClient.onReceivedSslError(view, handler, error);
            } else {
                super.onReceivedSslError(view, handler, error);
            }
        }
    }
}
