package com.android.server.webkit;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.webkit.UserPackage;
import android.webkit.WebViewFactory;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewZygote;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class SystemImpl implements SystemInterface {
    private static final int PACKAGE_FLAGS = 272629952;
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
        private static final SystemImpl INSTANCE = new SystemImpl();

        private LazyHolder() {
        }
    }

    /* synthetic */ SystemImpl(SystemImpl -this0) {
        this();
    }

    public static SystemImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0056 A:{Splitter: B:1:0x000c, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'e' java.lang.Exception), PHI: r11 } */
    /* JADX WARNING: Missing block: B:17:0x0056, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x0070, code:
            throw new android.util.AndroidRuntimeException("Error when parsing WebView config " + r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private SystemImpl() {
        int numFallbackPackages = 0;
        int numAvailableByDefaultPackages = 0;
        int numAvByDefaultAndNotFallback = 0;
        XmlResourceParser parser = null;
        List<WebViewProviderInfo> webViewProviders = new ArrayList();
        try {
            parser = AppGlobals.getInitialApplication().getResources().getXml(18284548);
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
                    } else if (numAvByDefaultAndNotFallback == 0) {
                        throw new AndroidRuntimeException("There must be at least one WebView package that is available by default and not a fallback");
                    } else {
                        this.mWebViewProviderPackages = (WebViewProviderInfo[]) webViewProviders.toArray(new WebViewProviderInfo[webViewProviders.size()]);
                        return;
                    }
                } else if (element.equals(TAG_WEBVIEW_PROVIDER)) {
                    String packageName = parser.getAttributeValue(null, TAG_PACKAGE_NAME);
                    if (packageName == null) {
                        throw new AndroidRuntimeException("WebView provider in framework resources missing package name");
                    }
                    String description = parser.getAttributeValue(null, TAG_DESCRIPTION);
                    if (description == null) {
                        throw new AndroidRuntimeException("WebView provider in framework resources missing description");
                    }
                    WebViewProviderInfo currentProvider = new WebViewProviderInfo(packageName, description, "true".equals(parser.getAttributeValue(null, TAG_AVAILABILITY)), "true".equals(parser.getAttributeValue(null, TAG_FALLBACK)), readSignatures(parser));
                    if (currentProvider.isFallback) {
                        numFallbackPackages++;
                        if (!currentProvider.availableByDefault) {
                            throw new AndroidRuntimeException("Each WebView fallback package must be available by default.");
                        } else if (numFallbackPackages > 1) {
                            throw new AndroidRuntimeException("There can be at most one WebView fallback package.");
                        }
                    }
                    if (currentProvider.availableByDefault) {
                        numAvailableByDefaultPackages++;
                        if (!currentProvider.isFallback) {
                            numAvByDefaultAndNotFallback++;
                        }
                    }
                    webViewProviders.add(currentProvider);
                } else {
                    Log.e(TAG, "Found an element that is not a WebView provider");
                }
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public WebViewProviderInfo[] getWebViewPackages() {
        return this.mWebViewProviderPackages;
    }

    public int getFactoryPackageVersion(String packageName) throws NameNotFoundException {
        return AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, DumpState.DUMP_COMPILER_STATS).versionCode;
    }

    private static String[] readSignatures(XmlResourceParser parser) throws IOException, XmlPullParserException {
        List<String> signatures = new ArrayList();
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
        return Global.getString(context.getContentResolver(), "webview_provider");
    }

    public void updateUserSetting(Context context, String newProviderName) {
        ContentResolver contentResolver = context.getContentResolver();
        String str = "webview_provider";
        if (newProviderName == null) {
            newProviderName = "";
        }
        Global.putString(contentResolver, str, newProviderName);
    }

    public void killPackageDependents(String packageName) {
        try {
            ActivityManager.getService().killPackageDependents(packageName, -1);
        } catch (RemoteException e) {
        }
    }

    public boolean isFallbackLogicEnabled() {
        return Global.getInt(AppGlobals.getInitialApplication().getContentResolver(), "webview_fallback_logic_enabled", 1) == 1;
    }

    public void enableFallbackLogic(boolean enable) {
        Global.putInt(AppGlobals.getInitialApplication().getContentResolver(), "webview_fallback_logic_enabled", enable ? 1 : 0);
    }

    public void uninstallAndDisablePackageForAllUsers(final Context context, String packageName) {
        enablePackageForAllUsers(context, packageName, false);
        try {
            PackageManager pm = AppGlobals.getInitialApplication().getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            if (applicationInfo != null && applicationInfo.isUpdatedSystemApp()) {
                pm.deletePackage(packageName, new Stub() {
                    public void packageDeleted(String packageName, int returnCode) {
                        SystemImpl.this.enablePackageForAllUsers(context, packageName, false);
                    }
                }, 6);
            }
        } catch (NameNotFoundException e) {
        }
    }

    public void enablePackageForAllUsers(Context context, String packageName, boolean enable) {
        for (UserInfo userInfo : ((UserManager) context.getSystemService("user")).getUsers()) {
            enablePackageForUser(packageName, enable, userInfo.id);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0011 A:{Splitter: B:1:0x0001, ExcHandler: android.os.RemoteException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:6:0x0011, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x0012, code:
            r1 = TAG;
            r2 = new java.lang.StringBuilder().append("Tried to ");
     */
    /* JADX WARNING: Missing block: B:8:0x0020, code:
            if (r9 != false) goto L_0x0022;
     */
    /* JADX WARNING: Missing block: B:9:0x0022, code:
            r0 = "enable ";
     */
    /* JADX WARNING: Missing block: B:10:0x0025, code:
            android.util.Log.w(r1, r2.append(r0).append(r8).append(" for user ").append(r10).append(": ").append(r6).toString());
     */
    /* JADX WARNING: Missing block: B:11:0x004b, code:
            r0 = "disable ";
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void enablePackageForUser(String packageName, boolean enable, int userId) {
        int i = 0;
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (!enable) {
                i = 3;
            }
            packageManager.setApplicationEnabledSetting(packageName, i, 0, userId, null);
        } catch (Exception e) {
        }
    }

    public boolean systemIsDebuggable() {
        return Build.IS_DEBUGGABLE;
    }

    public PackageInfo getPackageInfoForProvider(WebViewProviderInfo configInfo) throws NameNotFoundException {
        return AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(configInfo.packageName, PACKAGE_FLAGS);
    }

    public List<UserPackage> getPackageInfoForProviderAllUsers(Context context, WebViewProviderInfo configInfo) {
        return UserPackage.getPackageInfosAllUsers(context, configInfo.packageName, PACKAGE_FLAGS);
    }

    public int getMultiProcessSetting(Context context) {
        return Global.getInt(context.getContentResolver(), "webview_multiprocess", 0);
    }

    public void setMultiProcessSetting(Context context, int value) {
        Global.putInt(context.getContentResolver(), "webview_multiprocess", value);
    }

    public void notifyZygote(boolean enableMultiProcess) {
        WebViewZygote.setMultiprocessEnabled(enableMultiProcess);
    }

    public boolean isMultiProcessDefaultEnabled() {
        return true;
    }
}
