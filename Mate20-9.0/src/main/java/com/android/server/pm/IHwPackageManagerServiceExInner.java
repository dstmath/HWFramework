package com.android.server.pm;

import android.content.Context;
import java.util.HashSet;
import java.util.Map;

public interface IHwPackageManagerServiceExInner {
    boolean containDelPath(String str);

    Context getContextInner();

    HwCustPackageManagerService getCust();

    IHwPackageManagerInner getIPmsInner();

    Map<String, String> getUninstalledMap();

    void installAPKforInstallList(HashSet<String> hashSet, int i, int i2, long j, int i3);

    boolean isPlatformSignatureApp(String str);

    void reportEventStream(int i, String str);

    boolean scanInstallApk(String str, String str2, int i);
}
