package com.android.commands.applist;

import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import java.util.List;

public final class AppList {
    private static final String TAG = "AppList";

    public static void main(String[] args) {
        try {
            new AppList().run();
        } catch (Exception e) {
            System.out.print("Error : " + e);
        }
    }

    public void run() throws RemoteException {
        IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
        if (pm != null) {
            runListPackages(pm);
        } else {
            System.out.print("Can't get PackageManager interface");
        }
    }

    private void runListPackages(IPackageManager pm) {
        try {
            System.out.println("sdkVersion:" + SystemProperties.getInt("ro.build.version.sdk", 0));
            System.out.println("PackageName VersionName");
            List<PackageInfo> packages = getInstalledPackages(pm, 0, 0);
            int count = packages.size();
            for (int p = 0; p < count; p++) {
                printInfo((PackageInfo) packages.get(p));
            }
            System.out.println("OKAY");
        } catch (RemoteException e) {
            System.err.println(e.toString());
        }
    }

    private List<PackageInfo> getInstalledPackages(IPackageManager pm, int flags, int userId) throws RemoteException {
        return pm.getInstalledPackages(flags, userId).getList();
    }

    private void printInfo(PackageInfo info) {
        StringBuffer lineBuffer = new StringBuffer();
        String packageName = info.packageName;
        if (packageName == null) {
            packageName = "NA";
        }
        lineBuffer.append(packageName);
        lineBuffer.append(" ");
        String versionName = info.versionName;
        if (versionName == null) {
            versionName = "NA";
        }
        lineBuffer.append(versionName);
        System.out.println(lineBuffer.toString());
    }
}
