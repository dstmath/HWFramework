package android.webkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebStorage.QuotaUpdater;

public class WebChromeClient {

    public interface CustomViewCallback {
        void onCustomViewHidden();
    }

    public static abstract class FileChooserParams {
        public static final int MODE_OPEN = 0;
        public static final int MODE_OPEN_FOLDER = 2;
        public static final int MODE_OPEN_MULTIPLE = 1;
        public static final int MODE_SAVE = 3;

        public abstract Intent createIntent();

        public abstract String[] getAcceptTypes();

        public abstract String getFilenameHint();

        public abstract int getMode();

        public abstract CharSequence getTitle();

        public abstract boolean isCaptureEnabled();

        public static Uri[] parseResult(int resultCode, Intent data) {
            return WebViewFactory.getProvider().getStatics().parseFileChooserResult(resultCode, data);
        }
    }

    public void onProgressChanged(WebView view, int newProgress) {
    }

    public void onReceivedTitle(WebView view, String title) {
    }

    public void onReceivedIcon(WebView view, Bitmap icon) {
    }

    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
    }

    public void onShowCustomView(View view, CustomViewCallback callback) {
    }

    @Deprecated
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
    }

    public void onHideCustomView() {
    }

    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return false;
    }

    public void onRequestFocus(WebView view) {
    }

    public void onCloseWindow(WebView window) {
    }

    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return false;
    }

    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return false;
    }

    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return false;
    }

    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return false;
    }

    @Deprecated
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(quota);
    }

    @Deprecated
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(quota);
    }

    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
    }

    public void onGeolocationPermissionsHidePrompt() {
    }

    public void onPermissionRequest(PermissionRequest request) {
        request.deny();
    }

    public void onPermissionRequestCanceled(PermissionRequest request) {
    }

    public boolean onJsTimeout() {
        return true;
    }

    @Deprecated
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
    }

    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId());
        return false;
    }

    public Bitmap getDefaultVideoPoster() {
        return null;
    }

    public View getVideoLoadingProgressView() {
        return null;
    }

    public void getVisitedHistory(ValueCallback<String[]> valueCallback) {
    }

    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
        return false;
    }

    @Deprecated
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        uploadFile.onReceiveValue(null);
    }

    public void setupAutoFill(Message msg) {
    }
}
