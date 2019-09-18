package com.android.server.webkit;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.webkit.UserPackage;
import android.webkit.WebViewFactory;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewZygote;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.DumpState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class SystemImpl implements SystemInterface {
    private static final boolean GMS_BUILTIN = (!TextUtils.isEmpty(GMS_VERSION));
    private static final String GMS_VERSION = SystemProperties.get("ro.com.google.gmsversion", null);
    private static final String HUAWEI_WEBVIEW = "com.huawei.webview";
    private static final int PACKAGE_FLAGS = 272630976;
    private static final String TAG = SystemImpl.class.getSimpleName();
    private static final String TAG_AVAILABILITY = "availableByDefault";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_FALLBACK = "isFallback";
    private static final String TAG_PACKAGE_NAME = "packageName";
    private static final String TAG_SIGNATURE = "signature";
    private static final String TAG_START = "webviewproviders";
    private static final String TAG_WEBVIEW_PROVIDER = "webviewprovider";
    private final WebViewProviderInfo[] mWebViewProviderPackages;

    private static class LazyHolder {
        /* access modifiers changed from: private */
        public static final SystemImpl INSTANCE = new SystemImpl();

        private LazyHolder() {
        }
    }

    public static SystemImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    private SystemImpl() {
        int numFallbackPackages = 0;
        int numAvailableByDefaultPackages = 0;
        int numAvByDefaultAndNotFallback = 0;
        XmlResourceParser parser = null;
        List<WebViewProviderInfo> webViewProviders = new ArrayList<>();
        try {
            parser = AppGlobals.getInitialApplication().getResources().getXml(18284550);
            XmlUtils.beginDocument(parser, TAG_START);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) {
                    if (parser != null) {
                        parser.close();
                    }
                    if (numAvailableByDefaultPackages == 0) {
                        throw new AndroidRuntimeException("There must be at least one WebView package that is available by default");
                    } else if (numAvByDefaultAndNotFallback != 0) {
                        this.mWebViewProviderPackages = (WebViewProviderInfo[]) webViewProviders.toArray(new WebViewProviderInfo[webViewProviders.size()]);
                        return;
                    } else {
                        throw new AndroidRuntimeException("There must be at least one WebView package that is available by default and not a fallback");
                    }
                } else if (element.equals(TAG_WEBVIEW_PROVIDER)) {
                    String packageName = parser.getAttributeValue(null, "packageName");
                    if (packageName != null) {
                        String description = parser.getAttributeValue(null, TAG_DESCRIPTION);
                        if (description != null) {
                            WebViewProviderInfo webViewProviderInfo = new WebViewProviderInfo(packageName, description, "true".equals(parser.getAttributeValue(null, TAG_AVAILABILITY)), "true".equals(parser.getAttributeValue(null, TAG_FALLBACK)), readSignatures(parser));
                            if (webViewProviderInfo.isFallback) {
                                numFallbackPackages++;
                                if (!webViewProviderInfo.availableByDefault) {
                                    throw new AndroidRuntimeException("Each WebView fallback package must be available by default.");
                                } else if (numFallbackPackages > 1) {
                                    throw new AndroidRuntimeException("There can be at most one WebView fallback package.");
                                }
                            }
                            if (webViewProviderInfo.availableByDefault) {
                                numAvailableByDefaultPackages++;
                                if (!webViewProviderInfo.isFallback) {
                                    numAvByDefaultAndNotFallback++;
                                }
                            }
                            webViewProviders.add(webViewProviderInfo);
                        } else {
                            throw new AndroidRuntimeException("WebView provider in framework resources missing description");
                        }
                    } else {
                        throw new AndroidRuntimeException("WebView provider in framework resources missing package name");
                    }
                } else {
                    Log.e(TAG, "Found an element that is not a WebView provider");
                }
            }
        } catch (IOException | XmlPullParserException e) {
            throw new AndroidRuntimeException("Error when parsing WebView config " + e);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    public WebViewProviderInfo[] getWebViewPackages() {
        return filterWebViewPackages(this.mWebViewProviderPackages);
    }

    private WebViewProviderInfo[] filterWebViewPackages(WebViewProviderInfo[] infos) {
        if (!GMS_BUILTIN || infos == null || infos.length <= 1) {
            return infos;
        }
        List<WebViewProviderInfo> providers = new ArrayList<>();
        for (WebViewProviderInfo info : infos) {
            if (HUAWEI_WEBVIEW.equals(info.packageName)) {
                PackageInfo packageInfo = null;
                try {
                    packageInfo = AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(info.packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "get package info fail");
                }
                if (packageInfo == null) {
                    Log.v(TAG, "continue : packageInfo == null");
                } else {
                    Log.v(TAG, info.packageName);
                }
            }
            providers.add(info);
        }
        return (WebViewProviderInfo[]) providers.toArray(new WebViewProviderInfo[providers.size()]);
    }

    public long getFactoryPackageVersion(String packageName) throws PackageManager.NameNotFoundException {
        return AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, DumpState.DUMP_COMPILER_STATS).getLongVersionCode();
    }

    private static String[] readSignatures(XmlResourceParser parser) throws IOException, XmlPullParserException {
        List<String> signatures = new ArrayList<>();
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals(TAG_SIGNATURE)) {
                signatures.add(parser.nextText());
            } else {
                Log.e(TAG, "Found an element in a webview provider that is not a signature");
            }
        }
        return (String[]) signatures.toArray(new String[signatures.size()]);
    }

    public int onWebViewProviderChanged(PackageInfo packageInfo) {
        return WebViewFactory.onWebViewProviderChanged(packageInfo);
    }

    public String getUserChosenWebViewProvider(Context context) {
        return Settings.Global.getString(context.getContentResolver(), "webview_provider");
    }

    public void updateUserSetting(Context context, String newProviderName) {
        String str;
        ContentResolver contentResolver = context.getContentResolver();
        if (newProviderName == null) {
            str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        } else {
            str = newProviderName;
        }
        Settings.Global.putString(contentResolver, "webview_provider", str);
    }

    public void killPackageDependents(String packageName) {
        try {
            ActivityManager.getService().killPackageDependents(packageName, -1);
        } catch (RemoteException e) {
        }
    }

    public boolean isFallbackLogicEnabled() {
        return Settings.Global.getInt(AppGlobals.getInitialApplication().getContentResolver(), "webview_fallback_logic_enabled", 1) == 1;
    }

    public void enableFallbackLogic(boolean enable) {
        Settings.Global.putInt(AppGlobals.getInitialApplication().getContentResolver(), "webview_fallback_logic_enabled", enable);
    }

    public void uninstallAndDisablePackageForAllUsers(final Context context, String packageName) {
        enablePackageForAllUsers(context, packageName, false);
        try {
            PackageManager pm = AppGlobals.getInitialApplication().getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            if (applicationInfo != null && applicationInfo.isUpdatedSystemApp()) {
                pm.deletePackage(packageName, new IPackageDeleteObserver.Stub() {
                    public void packageDeleted(String packageName, int returnCode) {
                        SystemImpl.this.enablePackageForAllUsers(context, packageName, false);
                    }
                }, 6);
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public void enablePackageForAllUsers(Context context, String packageName, boolean enable) {
        for (UserInfo userInfo : ((UserManager) context.getSystemService("user")).getUsers()) {
            enablePackageForUser(packageName, enable, userInfo.id);
        }
    }

    public void enablePackageForUser(String packageName, boolean enable, int userId) {
        int i;
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (enable) {
                i = 0;
            } else {
                i = 3;
            }
            packageManager.setApplicationEnabledSetting(packageName, i, 0, userId, null);
        } catch (RemoteException | IllegalArgumentException e) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Tried to ");
            sb.append(enable ? "enable " : "disable ");
            sb.append(packageName);
            sb.append(" for user ");
            sb.append(userId);
            sb.append(": ");
            sb.append(e);
            Log.w(str, sb.toString());
        }
    }

    public boolean systemIsDebuggable() {
        return Build.IS_DEBUGGABLE;
    }

    public PackageInfo getPackageInfoForProvider(WebViewProviderInfo configInfo) throws PackageManager.NameNotFoundException {
        return AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(configInfo.packageName, PACKAGE_FLAGS);
    }

    public List<UserPackage> getPackageInfoForProviderAllUsers(Context context, WebViewProviderInfo configInfo) {
        return UserPackage.getPackageInfosAllUsers(context, configInfo.packageName, PACKAGE_FLAGS);
    }

    public int getMultiProcessSetting(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "webview_multiprocess", 0);
    }

    public void setMultiProcessSetting(Context context, int value) {
        Settings.Global.putInt(context.getContentResolver(), "webview_multiprocess", value);
    }

    public void notifyZygote(boolean enableMultiProcess) {
        WebViewZygote.setMultiprocessEnabled(enableMultiProcess);
    }

    public boolean isMultiProcessDefaultEnabled() {
        return Build.SUPPORTED_64_BIT_ABIS.length > 0 || !ActivityManager.isLowRamDeviceStatic();
    }
}
