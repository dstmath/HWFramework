package com.android.server.pm;

import java.util.HashSet;
import java.util.Map;

public interface IHwPackageManagerServiceExInner {
    boolean containDelPath(String str);

    HwCustPackageManagerService getCust();

    IHwPackageManagerInner getIPmsInner();

    Map<String, String> getUninstalledMap();

    void installAPKforInstallList(HashSet<String> hashSet, int i, int i2, long j, int i3);

    boolean isPlatformSignatureApp(String str);

    boolean scanInstallApk(String str, String str2, int i);
}
