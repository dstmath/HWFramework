package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import android.webkit.WebView;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.util.Map;

class WebViewHook {
    private static final String TAG = WebViewHook.class.getSimpleName();

    WebViewHook() {
    }

    @HookMethod(name = "loadUrl", params = {String.class, Map.class}, targetClass = WebView.class)
    static void loadUrlHook(Object obj, String url, Map<String, String> additionalHttpHeaders) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.WEBVIEW_LOADURL.getValue());
        Log.i(TAG, "Call System Hook Method: WebView loadurlHook(String, Map).");
        loadUrlBackup(obj, url, additionalHttpHeaders);
    }

    @BackupMethod(name = "loadUrl", params = {String.class, Map.class}, targetClass = WebView.class)
    static void loadUrlBackup(Object obj, String url, Map<String, String> map) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: WebView loadurlHook(String, Map).");
    }

    @HookMethod(name = "loadUrl", params = {String.class}, targetClass = WebView.class)
    static void loadUrlHook(Object obj, String url) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.WEBVIEW_LOADURL.getValue());
        Log.i(TAG, "Call System Hook Method:WebView loadurlHook(String).");
        loadUrlBackup(obj, url);
    }

    @BackupMethod(name = "loadUrl", params = {String.class}, targetClass = WebView.class)
    static void loadUrlBackup(Object obj, String url) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: WebView loadurlBackup(String url).");
    }

    @HookMethod(name = "postUrl", params = {String.class, byte[].class}, targetClass = WebView.class)
    static void postUrlHook(Object obj, String url, byte[] postData) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.WEBVIEW_POSTURL.getValue());
        Log.i(TAG, "Call System Hook Method: WebView postUrlHook().");
        postUrlBackup(obj, url, postData);
    }

    @BackupMethod(name = "postUrl", params = {String.class, byte[].class}, targetClass = WebView.class)
    static void postUrlBackup(Object obj, String url, byte[] postData) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: WebView postUrlBackup().");
    }

    @HookMethod(name = "loadDataWithBaseURL", params = {String.class, String.class, String.class, String.class, String.class}, targetClass = WebView.class)
    static void loadDataWithBaseURLHook(Object obj, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.WEBVIEW_LOADDATAWITHBASEURL.getValue());
        Log.i(TAG, "Call System Hook Method: WebView loadDataWithBaseURLHook().");
        loadDataWithBaseURLBackup(obj, baseUrl, data, mimeType, encoding, historyUrl);
    }

    @BackupMethod(name = "loadDataWithBaseURL", params = {String.class, String.class, String.class, String.class, String.class}, targetClass = WebView.class)
    static void loadDataWithBaseURLBackup(Object obj, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: WebView loadDataWithBaseURLBackup().");
    }
}
