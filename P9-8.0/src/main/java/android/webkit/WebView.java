package android.webkit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.StrictMode;
import android.print.PrintDocumentAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.HierarchyHandler;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.ViewHierarchyEncoder;
import android.view.ViewStructure;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.textclassifier.TextClassifier;
import android.widget.AbsoluteLayout;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Map;

public class WebView extends AbsoluteLayout implements OnGlobalFocusChangeListener, OnHierarchyChangeListener, HierarchyHandler {
    public static final String DATA_REDUCTION_PROXY_SETTING_CHANGED = "android.webkit.DATA_REDUCTION_PROXY_SETTING_CHANGED";
    private static final String LOGTAG = "WebView";
    public static final int RENDERER_PRIORITY_BOUND = 1;
    public static final int RENDERER_PRIORITY_IMPORTANT = 2;
    public static final int RENDERER_PRIORITY_WAIVED = 0;
    public static final String SCHEME_GEO = "geo:0,0?q=";
    public static final String SCHEME_MAILTO = "mailto:";
    public static final String SCHEME_TEL = "tel:";
    private static volatile boolean sEnforceThreadChecking = false;
    private FindListenerDistributor mFindListener;
    private WebViewProvider mProvider;
    private final Looper mWebViewThread;

    public interface FindListener {
        void onFindResultReceived(int i, int i2, boolean z);
    }

    private class FindListenerDistributor implements FindListener {
        private FindListener mFindDialogFindListener;
        private FindListener mUserFindListener;

        /* synthetic */ FindListenerDistributor(WebView this$0, FindListenerDistributor -this1) {
            this();
        }

        private FindListenerDistributor() {
        }

        public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
            if (this.mFindDialogFindListener != null) {
                this.mFindDialogFindListener.onFindResultReceived(activeMatchOrdinal, numberOfMatches, isDoneCounting);
            }
            if (this.mUserFindListener != null) {
                this.mUserFindListener.onFindResultReceived(activeMatchOrdinal, numberOfMatches, isDoneCounting);
            }
        }
    }

    public static class HitTestResult {
        @Deprecated
        public static final int ANCHOR_TYPE = 1;
        public static final int EDIT_TEXT_TYPE = 9;
        public static final int EMAIL_TYPE = 4;
        public static final int GEO_TYPE = 3;
        @Deprecated
        public static final int IMAGE_ANCHOR_TYPE = 6;
        public static final int IMAGE_TYPE = 5;
        public static final int PHONE_TYPE = 2;
        public static final int SRC_ANCHOR_TYPE = 7;
        public static final int SRC_IMAGE_ANCHOR_TYPE = 8;
        public static final int UNKNOWN_TYPE = 0;
        private String mExtra;
        private int mType = 0;

        public void setType(int type) {
            this.mType = type;
        }

        public void setExtra(String extra) {
            this.mExtra = extra;
        }

        public int getType() {
            return this.mType;
        }

        public String getExtra() {
            return this.mExtra;
        }
    }

    @Deprecated
    public interface PictureListener {
        @Deprecated
        void onNewPicture(WebView webView, Picture picture);
    }

    public class PrivateAccess {
        public int super_getScrollBarStyle() {
            return super.-wrap14();
        }

        public void super_scrollTo(int scrollX, int scrollY) {
            super.-wrap17(scrollX, scrollY);
        }

        public void super_computeScroll() {
            super.-wrap15();
        }

        public boolean super_onHoverEvent(MotionEvent event) {
            return super.-wrap9(event);
        }

        public boolean super_performAccessibilityAction(int action, Bundle arguments) {
            return super.-wrap10(action, arguments);
        }

        public boolean super_performLongClick() {
            return super.-wrap11();
        }

        public boolean super_setFrame(int left, int top, int right, int bottom) {
            return super.-wrap13(left, top, right, bottom);
        }

        public boolean super_dispatchKeyEvent(KeyEvent event) {
            return super.-wrap7(event);
        }

        public boolean super_onGenericMotionEvent(MotionEvent event) {
            return super.-wrap8(event);
        }

        public boolean super_requestFocus(int direction, Rect previouslyFocusedRect) {
            return super.-wrap12(direction, previouslyFocusedRect);
        }

        public void super_setLayoutParams(LayoutParams params) {
            super.-wrap18(params);
        }

        public void super_startActivityForResult(Intent intent, int requestCode) {
            super.-wrap19(intent, requestCode);
        }

        public void overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
            WebView.this.-wrap2(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }

        public void awakenScrollBars(int duration) {
            WebView.this.-wrap1(duration);
        }

        public void awakenScrollBars(int duration, boolean invalidate) {
            WebView.this.-wrap0(duration, invalidate);
        }

        public float getVerticalScrollFactor() {
            return WebView.this.-wrap4();
        }

        public float getHorizontalScrollFactor() {
            return WebView.this.-wrap3();
        }

        public void setMeasuredDimension(int measuredWidth, int measuredHeight) {
            WebView.this.-wrap6(measuredWidth, measuredHeight);
        }

        public void onScrollChanged(int l, int t, int oldl, int oldt) {
            WebView.this.onScrollChanged(l, t, oldl, oldt);
        }

        public int getHorizontalScrollbarHeight() {
            return WebView.this.-wrap5();
        }

        public void super_onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
            super.-wrap16(canvas, scrollBar, l, t, r, b);
        }

        public void setScrollXRaw(int scrollX) {
            WebView.this.mScrollX = scrollX;
        }

        public void setScrollYRaw(int scrollY) {
            WebView.this.mScrollY = scrollY;
        }
    }

    public static abstract class VisualStateCallback {
        public abstract void onComplete(long j);
    }

    public class WebViewTransport {
        private WebView mWebview;

        public synchronized void setWebView(WebView webview) {
            this.mWebview = webview;
        }

        public synchronized WebView getWebView() {
            return this.mWebview;
        }
    }

    public WebView(Context context) {
        this(context, null);
    }

    public WebView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.webViewStyle);
    }

    public WebView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr, defStyleRes, null, false);
    }

    @Deprecated
    public WebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        this(context, attrs, defStyleAttr, 0, null, privateBrowsing);
    }

    protected WebView(Context context, AttributeSet attrs, int defStyleAttr, Map<String, Object> javaScriptInterfaces, boolean privateBrowsing) {
        this(context, attrs, defStyleAttr, 0, javaScriptInterfaces, privateBrowsing);
    }

    protected WebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, Map<String, Object> javaScriptInterfaces, boolean privateBrowsing) {
        boolean z = true;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mWebViewThread = Looper.myLooper();
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(1);
        }
        if (context == null) {
            throw new IllegalArgumentException("Invalid context argument");
        }
        if (context.getApplicationInfo().targetSdkVersion < 18) {
            z = false;
        }
        sEnforceThreadChecking = z;
        checkThread();
        ensureProviderCreated();
        this.mProvider.init(javaScriptInterfaces, privateBrowsing);
        CookieSyncManager.setGetInstanceIsAllowed();
    }

    @Deprecated
    public void setHorizontalScrollbarOverlay(boolean overlay) {
    }

    @Deprecated
    public void setVerticalScrollbarOverlay(boolean overlay) {
    }

    @Deprecated
    public boolean overlayHorizontalScrollbar() {
        return true;
    }

    @Deprecated
    public boolean overlayVerticalScrollbar() {
        return false;
    }

    @Deprecated
    public int getVisibleTitleHeight() {
        checkThread();
        return this.mProvider.getVisibleTitleHeight();
    }

    public SslCertificate getCertificate() {
        checkThread();
        return this.mProvider.getCertificate();
    }

    @Deprecated
    public void setCertificate(SslCertificate certificate) {
        checkThread();
        this.mProvider.setCertificate(certificate);
    }

    @Deprecated
    public void savePassword(String host, String username, String password) {
        checkThread();
        this.mProvider.savePassword(host, username, password);
    }

    @Deprecated
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        checkThread();
        this.mProvider.setHttpAuthUsernamePassword(host, realm, username, password);
    }

    @Deprecated
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        checkThread();
        return this.mProvider.getHttpAuthUsernamePassword(host, realm);
    }

    public void destroy() {
        checkThread();
        this.mProvider.destroy();
    }

    @Deprecated
    public static void enablePlatformNotifications() {
    }

    @Deprecated
    public static void disablePlatformNotifications() {
    }

    public static void freeMemoryForTests() {
        getFactory().getStatics().freeMemoryForTests();
    }

    public void setNetworkAvailable(boolean networkUp) {
        checkThread();
        this.mProvider.setNetworkAvailable(networkUp);
    }

    public WebBackForwardList saveState(Bundle outState) {
        checkThread();
        return this.mProvider.saveState(outState);
    }

    @Deprecated
    public boolean savePicture(Bundle b, File dest) {
        checkThread();
        return this.mProvider.savePicture(b, dest);
    }

    @Deprecated
    public boolean restorePicture(Bundle b, File src) {
        checkThread();
        return this.mProvider.restorePicture(b, src);
    }

    public WebBackForwardList restoreState(Bundle inState) {
        checkThread();
        return this.mProvider.restoreState(inState);
    }

    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        checkThread();
        this.mProvider.loadUrl(url, additionalHttpHeaders);
    }

    public void loadUrl(String url) {
        checkThread();
        this.mProvider.loadUrl(url);
    }

    public void postUrl(String url, byte[] postData) {
        checkThread();
        if (URLUtil.isNetworkUrl(url)) {
            this.mProvider.postUrl(url, postData);
        } else {
            this.mProvider.loadUrl(url);
        }
    }

    public void loadData(String data, String mimeType, String encoding) {
        checkThread();
        this.mProvider.loadData(data, mimeType, encoding);
    }

    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        checkThread();
        this.mProvider.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        checkThread();
        this.mProvider.evaluateJavaScript(script, resultCallback);
    }

    public void saveWebArchive(String filename) {
        checkThread();
        this.mProvider.saveWebArchive(filename);
    }

    public void saveWebArchive(String basename, boolean autoname, ValueCallback<String> callback) {
        checkThread();
        this.mProvider.saveWebArchive(basename, autoname, callback);
    }

    public void stopLoading() {
        checkThread();
        this.mProvider.stopLoading();
    }

    public void reload() {
        checkThread();
        this.mProvider.reload();
    }

    public boolean canGoBack() {
        checkThread();
        return this.mProvider.canGoBack();
    }

    public void goBack() {
        checkThread();
        this.mProvider.goBack();
    }

    public boolean canGoForward() {
        checkThread();
        return this.mProvider.canGoForward();
    }

    public void goForward() {
        checkThread();
        this.mProvider.goForward();
    }

    public boolean canGoBackOrForward(int steps) {
        checkThread();
        return this.mProvider.canGoBackOrForward(steps);
    }

    public void goBackOrForward(int steps) {
        checkThread();
        this.mProvider.goBackOrForward(steps);
    }

    public boolean isPrivateBrowsingEnabled() {
        checkThread();
        return this.mProvider.isPrivateBrowsingEnabled();
    }

    public boolean pageUp(boolean top) {
        checkThread();
        return this.mProvider.pageUp(top);
    }

    public boolean pageDown(boolean bottom) {
        checkThread();
        return this.mProvider.pageDown(bottom);
    }

    public void postVisualStateCallback(long requestId, VisualStateCallback callback) {
        checkThread();
        this.mProvider.insertVisualStateCallback(requestId, callback);
    }

    @Deprecated
    public void clearView() {
        checkThread();
        this.mProvider.clearView();
    }

    @Deprecated
    public Picture capturePicture() {
        checkThread();
        return this.mProvider.capturePicture();
    }

    @Deprecated
    public PrintDocumentAdapter createPrintDocumentAdapter() {
        checkThread();
        return this.mProvider.createPrintDocumentAdapter(PhoneConstants.APN_TYPE_DEFAULT);
    }

    public PrintDocumentAdapter createPrintDocumentAdapter(String documentName) {
        checkThread();
        return this.mProvider.createPrintDocumentAdapter(documentName);
    }

    @ExportedProperty(category = "webview")
    @Deprecated
    public float getScale() {
        checkThread();
        return this.mProvider.getScale();
    }

    public void setInitialScale(int scaleInPercent) {
        checkThread();
        this.mProvider.setInitialScale(scaleInPercent);
    }

    public void invokeZoomPicker() {
        checkThread();
        this.mProvider.invokeZoomPicker();
    }

    public HitTestResult getHitTestResult() {
        checkThread();
        return this.mProvider.getHitTestResult();
    }

    public void requestFocusNodeHref(Message hrefMsg) {
        checkThread();
        this.mProvider.requestFocusNodeHref(hrefMsg);
    }

    public void requestImageRef(Message msg) {
        checkThread();
        this.mProvider.requestImageRef(msg);
    }

    @ExportedProperty(category = "webview")
    public String getUrl() {
        checkThread();
        return this.mProvider.getUrl();
    }

    @ExportedProperty(category = "webview")
    public String getOriginalUrl() {
        checkThread();
        return this.mProvider.getOriginalUrl();
    }

    @ExportedProperty(category = "webview")
    public String getTitle() {
        checkThread();
        return this.mProvider.getTitle();
    }

    public Bitmap getFavicon() {
        checkThread();
        return this.mProvider.getFavicon();
    }

    public String getTouchIconUrl() {
        return this.mProvider.getTouchIconUrl();
    }

    public int getProgress() {
        checkThread();
        return this.mProvider.getProgress();
    }

    @ExportedProperty(category = "webview")
    public int getContentHeight() {
        checkThread();
        return this.mProvider.getContentHeight();
    }

    @ExportedProperty(category = "webview")
    public int getContentWidth() {
        return this.mProvider.getContentWidth();
    }

    public void pauseTimers() {
        checkThread();
        this.mProvider.pauseTimers();
    }

    public void resumeTimers() {
        checkThread();
        this.mProvider.resumeTimers();
    }

    public void onPause() {
        checkThread();
        this.mProvider.onPause();
    }

    public void onResume() {
        checkThread();
        this.mProvider.onResume();
    }

    public boolean isPaused() {
        return this.mProvider.isPaused();
    }

    @Deprecated
    public void freeMemory() {
        checkThread();
        this.mProvider.freeMemory();
    }

    public void clearCache(boolean includeDiskFiles) {
        checkThread();
        this.mProvider.clearCache(includeDiskFiles);
    }

    public void clearFormData() {
        checkThread();
        this.mProvider.clearFormData();
    }

    public void clearHistory() {
        checkThread();
        this.mProvider.clearHistory();
    }

    public void clearSslPreferences() {
        checkThread();
        this.mProvider.clearSslPreferences();
    }

    public static void clearClientCertPreferences(Runnable onCleared) {
        getFactory().getStatics().clearClientCertPreferences(onCleared);
    }

    public WebBackForwardList copyBackForwardList() {
        checkThread();
        return this.mProvider.copyBackForwardList();
    }

    public void setFindListener(FindListener listener) {
        checkThread();
        setupFindListenerIfNeeded();
        this.mFindListener.mUserFindListener = listener;
    }

    public void findNext(boolean forward) {
        checkThread();
        this.mProvider.findNext(forward);
    }

    @Deprecated
    public int findAll(String find) {
        checkThread();
        StrictMode.noteSlowCall("findAll blocks UI: prefer findAllAsync");
        return this.mProvider.findAll(find);
    }

    public void findAllAsync(String find) {
        checkThread();
        this.mProvider.findAllAsync(find);
    }

    @Deprecated
    public boolean showFindDialog(String text, boolean showIme) {
        checkThread();
        return this.mProvider.showFindDialog(text, showIme);
    }

    public static String findAddress(String addr) {
        return getFactory().getStatics().findAddress(addr);
    }

    public static void enableSlowWholeDocumentDraw() {
        getFactory().getStatics().enableSlowWholeDocumentDraw();
    }

    public void clearMatches() {
        checkThread();
        this.mProvider.clearMatches();
    }

    public void documentHasImages(Message response) {
        checkThread();
        this.mProvider.documentHasImages(response);
    }

    public void setWebViewClient(WebViewClient client) {
        checkThread();
        this.mProvider.setWebViewClient(client);
    }

    public WebViewClient getWebViewClient() {
        checkThread();
        return this.mProvider.getWebViewClient();
    }

    public void setDownloadListener(DownloadListener listener) {
        checkThread();
        this.mProvider.setDownloadListener(listener);
    }

    public void setWebChromeClient(WebChromeClient client) {
        checkThread();
        this.mProvider.setWebChromeClient(client);
    }

    public WebChromeClient getWebChromeClient() {
        checkThread();
        return this.mProvider.getWebChromeClient();
    }

    @Deprecated
    public void setPictureListener(PictureListener listener) {
        checkThread();
        this.mProvider.setPictureListener(listener);
    }

    public void addJavascriptInterface(Object object, String name) {
        checkThread();
        this.mProvider.addJavascriptInterface(object, name);
    }

    public void removeJavascriptInterface(String name) {
        checkThread();
        this.mProvider.removeJavascriptInterface(name);
    }

    public WebMessagePort[] createWebMessageChannel() {
        checkThread();
        return this.mProvider.createWebMessageChannel();
    }

    public void postWebMessage(WebMessage message, Uri targetOrigin) {
        checkThread();
        this.mProvider.postMessageToMainFrame(message, targetOrigin);
    }

    public WebSettings getSettings() {
        checkThread();
        return this.mProvider.getSettings();
    }

    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        getFactory().getStatics().setWebContentsDebuggingEnabled(enabled);
    }

    @Deprecated
    public static synchronized PluginList getPluginList() {
        PluginList pluginList;
        synchronized (WebView.class) {
            pluginList = new PluginList();
        }
        return pluginList;
    }

    @Deprecated
    public void refreshPlugins(boolean reloadOpenPages) {
        checkThread();
    }

    @Deprecated
    public void emulateShiftHeld() {
        checkThread();
    }

    @Deprecated
    public void onChildViewAdded(View parent, View child) {
    }

    @Deprecated
    public void onChildViewRemoved(View p, View child) {
    }

    @Deprecated
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
    }

    @Deprecated
    public void setMapTrackballToArrowKeys(boolean setMap) {
        checkThread();
        this.mProvider.setMapTrackballToArrowKeys(setMap);
    }

    public void flingScroll(int vx, int vy) {
        checkThread();
        this.mProvider.flingScroll(vx, vy);
    }

    @Deprecated
    public View getZoomControls() {
        checkThread();
        return this.mProvider.getZoomControls();
    }

    @Deprecated
    public boolean canZoomIn() {
        checkThread();
        return this.mProvider.canZoomIn();
    }

    @Deprecated
    public boolean canZoomOut() {
        checkThread();
        return this.mProvider.canZoomOut();
    }

    public void zoomBy(float zoomFactor) {
        checkThread();
        if (((double) zoomFactor) < 0.01d) {
            throw new IllegalArgumentException("zoomFactor must be greater than 0.01.");
        } else if (((double) zoomFactor) > 100.0d) {
            throw new IllegalArgumentException("zoomFactor must be less than 100.");
        } else {
            this.mProvider.zoomBy(zoomFactor);
        }
    }

    public boolean zoomIn() {
        checkThread();
        return this.mProvider.zoomIn();
    }

    public boolean zoomOut() {
        checkThread();
        return this.mProvider.zoomOut();
    }

    @Deprecated
    public void debugDump() {
        checkThread();
    }

    public void dumpViewHierarchyWithProperties(BufferedWriter out, int level) {
        this.mProvider.dumpViewHierarchyWithProperties(out, level);
    }

    public View findHierarchyView(String className, int hashCode) {
        return this.mProvider.findHierarchyView(className, hashCode);
    }

    public void setRendererPriorityPolicy(int rendererRequestedPriority, boolean waivedWhenNotVisible) {
        this.mProvider.setRendererPriorityPolicy(rendererRequestedPriority, waivedWhenNotVisible);
    }

    public int getRendererRequestedPriority() {
        return this.mProvider.getRendererRequestedPriority();
    }

    public boolean getRendererPriorityWaivedWhenNotVisible() {
        return this.mProvider.getRendererPriorityWaivedWhenNotVisible();
    }

    public void setTextClassifier(TextClassifier textClassifier) {
        this.mProvider.setTextClassifier(textClassifier);
    }

    public TextClassifier getTextClassifier() {
        return this.mProvider.getTextClassifier();
    }

    public WebViewProvider getWebViewProvider() {
        return this.mProvider;
    }

    void setFindDialogFindListener(FindListener listener) {
        checkThread();
        setupFindListenerIfNeeded();
        this.mFindListener.mFindDialogFindListener = listener;
    }

    void notifyFindDialogDismissed() {
        checkThread();
        this.mProvider.notifyFindDialogDismissed();
    }

    private void setupFindListenerIfNeeded() {
        if (this.mFindListener == null) {
            this.mFindListener = new FindListenerDistributor(this, null);
            this.mProvider.setFindListener(this.mFindListener);
        }
    }

    private void ensureProviderCreated() {
        checkThread();
        if (this.mProvider == null) {
            this.mProvider = getFactory().createWebView(this, new PrivateAccess());
        }
    }

    private static WebViewFactoryProvider getFactory() {
        return WebViewFactory.getProvider();
    }

    private void checkThread() {
        if (this.mWebViewThread != null && Looper.myLooper() != this.mWebViewThread) {
            Throwable throwable = new Throwable("A WebView method was called on thread '" + Thread.currentThread().getName() + "'. " + "All WebView methods must be called on the same thread. " + "(Expected Looper " + this.mWebViewThread + " called on " + Looper.myLooper() + ", FYI main Looper is " + Looper.getMainLooper() + ")");
            Log.w(LOGTAG, Log.getStackTraceString(throwable));
            StrictMode.onWebViewMethodCalledOnWrongThread(throwable);
            if (sEnforceThreadChecking) {
                throw new RuntimeException(throwable);
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mProvider.getViewDelegate().onAttachedToWindow();
    }

    protected void onDetachedFromWindowInternal() {
        this.mProvider.getViewDelegate().onDetachedFromWindow();
        super.onDetachedFromWindowInternal();
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
        this.mProvider.getViewDelegate().onMovedToDisplay(displayId, config);
    }

    public void setLayoutParams(LayoutParams params) {
        this.mProvider.getViewDelegate().setLayoutParams(params);
    }

    public void setOverScrollMode(int mode) {
        super.setOverScrollMode(mode);
        ensureProviderCreated();
        this.mProvider.getViewDelegate().setOverScrollMode(mode);
    }

    public void setScrollBarStyle(int style) {
        this.mProvider.getViewDelegate().setScrollBarStyle(style);
        super.setScrollBarStyle(style);
    }

    protected int computeHorizontalScrollRange() {
        return this.mProvider.getScrollDelegate().computeHorizontalScrollRange();
    }

    protected int computeHorizontalScrollOffset() {
        return this.mProvider.getScrollDelegate().computeHorizontalScrollOffset();
    }

    protected int computeVerticalScrollRange() {
        return this.mProvider.getScrollDelegate().computeVerticalScrollRange();
    }

    protected int computeVerticalScrollOffset() {
        return this.mProvider.getScrollDelegate().computeVerticalScrollOffset();
    }

    protected int computeVerticalScrollExtent() {
        return this.mProvider.getScrollDelegate().computeVerticalScrollExtent();
    }

    public void computeScroll() {
        this.mProvider.getScrollDelegate().computeScroll();
    }

    public boolean onHoverEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onHoverEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onTouchEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onGenericMotionEvent(event);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return this.mProvider.getViewDelegate().onTrackballEvent(event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mProvider.getViewDelegate().onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mProvider.getViewDelegate().onKeyUp(keyCode, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return this.mProvider.getViewDelegate().onKeyMultiple(keyCode, repeatCount, event);
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        AccessibilityNodeProvider provider = this.mProvider.getViewDelegate().getAccessibilityNodeProvider();
        return provider == null ? super.getAccessibilityNodeProvider() : provider;
    }

    @Deprecated
    public boolean shouldDelayChildPressedState() {
        return this.mProvider.getViewDelegate().shouldDelayChildPressedState();
    }

    public CharSequence getAccessibilityClassName() {
        return WebView.class.getName();
    }

    public void onProvideVirtualStructure(ViewStructure structure) {
        this.mProvider.getViewDelegate().onProvideVirtualStructure(structure);
    }

    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        this.mProvider.getViewDelegate().onProvideAutofillVirtualStructure(structure, flags);
    }

    public void autofill(SparseArray<AutofillValue> values) {
        this.mProvider.getViewDelegate().autofill(values);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        this.mProvider.getViewDelegate().onInitializeAccessibilityNodeInfo(info);
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        this.mProvider.getViewDelegate().onInitializeAccessibilityEvent(event);
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        return this.mProvider.getViewDelegate().performAccessibilityAction(action, arguments);
    }

    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        this.mProvider.getViewDelegate().onDrawVerticalScrollBar(canvas, scrollBar, l, t, r, b);
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        this.mProvider.getViewDelegate().onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mProvider.getViewDelegate().onWindowVisibilityChanged(visibility);
    }

    protected void onDraw(Canvas canvas) {
        this.mProvider.getViewDelegate().onDraw(canvas);
    }

    public boolean performLongClick() {
        return this.mProvider.getViewDelegate().performLongClick();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mProvider.getViewDelegate().onConfigurationChanged(newConfig);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return this.mProvider.getViewDelegate().onCreateInputConnection(outAttrs);
    }

    public boolean onDragEvent(DragEvent event) {
        return this.mProvider.getViewDelegate().onDragEvent(event);
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        ensureProviderCreated();
        this.mProvider.getViewDelegate().onVisibilityChanged(changedView, visibility);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        this.mProvider.getViewDelegate().onWindowFocusChanged(hasWindowFocus);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        this.mProvider.getViewDelegate().onFocusChanged(focused, direction, previouslyFocusedRect);
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        return this.mProvider.getViewDelegate().setFrame(left, top, right, bottom);
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        this.mProvider.getViewDelegate().onSizeChanged(w, h, ow, oh);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        this.mProvider.getViewDelegate().onScrollChanged(l, t, oldl, oldt);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return this.mProvider.getViewDelegate().dispatchKeyEvent(event);
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return this.mProvider.getViewDelegate().requestFocus(direction, previouslyFocusedRect);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mProvider.getViewDelegate().onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        return this.mProvider.getViewDelegate().requestChildRectangleOnScreen(child, rect, immediate);
    }

    public void setBackgroundColor(int color) {
        this.mProvider.getViewDelegate().setBackgroundColor(color);
    }

    public void setLayerType(int layerType, Paint paint) {
        super.setLayerType(layerType, paint);
        this.mProvider.getViewDelegate().setLayerType(layerType, paint);
    }

    protected void dispatchDraw(Canvas canvas) {
        this.mProvider.getViewDelegate().preDispatchDraw(canvas);
        super.dispatchDraw(canvas);
    }

    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        this.mProvider.getViewDelegate().onStartTemporaryDetach();
    }

    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        this.mProvider.getViewDelegate().onFinishTemporaryDetach();
    }

    public Handler getHandler() {
        return this.mProvider.getViewDelegate().getHandler(super.getHandler());
    }

    public View findFocus() {
        return this.mProvider.getViewDelegate().findFocus(super.findFocus());
    }

    public static PackageInfo getCurrentWebViewPackage() {
        PackageInfo webviewPackage = WebViewFactory.getLoadedPackageInfo();
        if (webviewPackage != null) {
            return webviewPackage;
        }
        try {
            return WebViewFactory.getUpdateService().getCurrentWebViewPackage();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mProvider.getViewDelegate().onActivityResult(requestCode, resultCode, data);
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        checkThread();
        encoder.addProperty("webview:contentHeight", this.mProvider.getContentHeight());
        encoder.addProperty("webview:contentWidth", this.mProvider.getContentWidth());
        encoder.addProperty("webview:scale", this.mProvider.getScale());
        encoder.addProperty("webview:title", this.mProvider.getTitle());
        encoder.addProperty("webview:url", this.mProvider.getUrl());
        encoder.addProperty("webview:originalUrl", this.mProvider.getOriginalUrl());
    }
}
