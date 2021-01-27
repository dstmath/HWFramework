package com.android.server.timezone;

import android.content.Intent;
import android.content.pm.PackageManager;

interface PackageManagerHelper {
    boolean contentProviderRegistered(String str, String str2);

    long getInstalledPackageVersion(String str) throws PackageManager.NameNotFoundException;

    boolean isPrivilegedApp(String str) throws PackageManager.NameNotFoundException;

    boolean receiverRegistered(Intent intent, String str) throws PackageManager.NameNotFoundException;

    boolean usesPermission(String str, String str2) throws PackageManager.NameNotFoundException;
}
