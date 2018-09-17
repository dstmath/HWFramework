package com.android.server.webkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.webkit.UserPackage;
import android.webkit.WebViewProviderInfo;
import java.util.List;

public interface SystemInterface {
    void enableFallbackLogic(boolean z);

    void enablePackageForAllUsers(Context context, String str, boolean z);

    void enablePackageForUser(String str, boolean z, int i);

    int getFactoryPackageVersion(String str) throws NameNotFoundException;

    int getMultiProcessSetting(Context context);

    PackageInfo getPackageInfoForProvider(WebViewProviderInfo webViewProviderInfo) throws NameNotFoundException;

    List<UserPackage> getPackageInfoForProviderAllUsers(Context context, WebViewProviderInfo webViewProviderInfo);

    String getUserChosenWebViewProvider(Context context);

    WebViewProviderInfo[] getWebViewPackages();

    boolean isFallbackLogicEnabled();

    boolean isMultiProcessDefaultEnabled();

    void killPackageDependents(String str);

    void notifyZygote(boolean z);

    int onWebViewProviderChanged(PackageInfo packageInfo);

    void setMultiProcessSetting(Context context, int i);

    boolean systemIsDebuggable();

    void uninstallAndDisablePackageForAllUsers(Context context, String str);

    void updateUserSetting(Context context, String str);
}
