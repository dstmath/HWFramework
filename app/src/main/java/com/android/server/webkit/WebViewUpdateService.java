package com.android.server.webkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Process;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.util.Slog;
import android.webkit.IWebViewUpdateService.Stub;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewProviderResponse;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import java.io.FileDescriptor;
import java.util.Arrays;

public class WebViewUpdateService extends SystemService {
    static final int PACKAGE_ADDED = 1;
    static final int PACKAGE_ADDED_REPLACED = 2;
    static final int PACKAGE_CHANGED = 0;
    static final int PACKAGE_REMOVED = 3;
    private static final String TAG = "WebViewUpdateService";
    private WebViewUpdateServiceImpl mImpl;
    private BroadcastReceiver mWebViewUpdatedReceiver;

    private class BinderService extends Stub {
        private BinderService() {
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
            new WebViewUpdateServiceShellCommand(this).exec(this, in, out, err, args, resultReceiver);
        }

        public void notifyRelroCreationCompleted() {
            if (Binder.getCallingUid() == 1037 || Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    WebViewUpdateService.this.mImpl.notifyRelroCreationCompleted();
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public WebViewProviderResponse waitForAndGetProvider() {
            if (Binder.getCallingPid() != Process.myPid()) {
                return WebViewUpdateService.this.mImpl.waitForAndGetProvider();
            }
            throw new IllegalStateException("Cannot create a WebView from the SystemServer");
        }

        public String changeProviderAndSetting(String newProvider) {
            if (WebViewUpdateService.this.getContext().checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                String msg = "Permission Denial: changeProviderAndSetting() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.WRITE_SECURE_SETTINGS";
                Slog.w(WebViewUpdateService.TAG, msg);
                throw new SecurityException(msg);
            }
            long callingId = Binder.clearCallingIdentity();
            try {
                String changeProviderAndSetting = WebViewUpdateService.this.mImpl.changeProviderAndSetting(newProvider);
                return changeProviderAndSetting;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }

        public WebViewProviderInfo[] getValidWebViewPackages() {
            return WebViewUpdateService.this.mImpl.getValidWebViewPackages();
        }

        public WebViewProviderInfo[] getAllWebViewPackages() {
            return WebViewUpdateService.this.mImpl.getWebViewPackages();
        }

        public String getCurrentWebViewPackageName() {
            return WebViewUpdateService.this.mImpl.getCurrentWebViewPackageName();
        }

        public boolean isFallbackPackage(String packageName) {
            return WebViewUpdateService.this.mImpl.isFallbackPackage(packageName);
        }

        public void enableFallbackLogic(boolean enable) {
            if (WebViewUpdateService.this.getContext().checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                String msg = "Permission Denial: enableFallbackLogic() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.WRITE_SECURE_SETTINGS";
                Slog.w(WebViewUpdateService.TAG, msg);
                throw new SecurityException(msg);
            }
            long callingId = Binder.clearCallingIdentity();
            try {
                WebViewUpdateService.this.mImpl.enableFallbackLogic(enable);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public WebViewUpdateService(Context context) {
        super(context);
        this.mImpl = new WebViewUpdateServiceImpl(context, SystemImpl.getInstance());
    }

    public void onStart() {
        this.mWebViewUpdatedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                String action = intent.getAction();
                if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    if (!intent.getExtras().getBoolean("android.intent.extra.REPLACING")) {
                        WebViewUpdateService.this.mImpl.packageStateChanged(WebViewUpdateService.packageNameFromIntent(intent), WebViewUpdateService.PACKAGE_REMOVED, userId);
                    }
                } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    if (WebViewUpdateService.entirePackageChanged(intent)) {
                        WebViewUpdateService.this.mImpl.packageStateChanged(WebViewUpdateService.packageNameFromIntent(intent), WebViewUpdateService.PACKAGE_CHANGED, userId);
                    }
                } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                    WebViewUpdateService.this.mImpl.packageStateChanged(WebViewUpdateService.packageNameFromIntent(intent), intent.getExtras().getBoolean("android.intent.extra.REPLACING") ? WebViewUpdateService.PACKAGE_ADDED_REPLACED : WebViewUpdateService.PACKAGE_ADDED, userId);
                } else if (action.equals("android.intent.action.USER_ADDED")) {
                    WebViewUpdateService.this.mImpl.handleNewUser(userId);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        WebViewProviderInfo[] webViewPackages = this.mImpl.getWebViewPackages();
        int length = webViewPackages.length;
        for (int i = PACKAGE_CHANGED; i < length; i += PACKAGE_ADDED) {
            filter.addDataSchemeSpecificPart(webViewPackages[i].packageName, PACKAGE_CHANGED);
        }
        getContext().registerReceiverAsUser(this.mWebViewUpdatedReceiver, UserHandle.ALL, filter, null, null);
        IntentFilter userAddedFilter = new IntentFilter();
        userAddedFilter.addAction("android.intent.action.USER_ADDED");
        getContext().registerReceiverAsUser(this.mWebViewUpdatedReceiver, UserHandle.ALL, userAddedFilter, null, null);
        publishBinderService("webviewupdate", new BinderService(), true);
    }

    public void prepareWebViewInSystemServer() {
        this.mImpl.prepareWebViewInSystemServer();
    }

    private static String packageNameFromIntent(Intent intent) {
        return intent.getDataString().substring("package:".length());
    }

    public static boolean entirePackageChanged(Intent intent) {
        return Arrays.asList(intent.getStringArrayExtra("android.intent.extra.changed_component_name_list")).contains(intent.getDataString().substring("package:".length()));
    }
}
