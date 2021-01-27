package com.android.server.pm.permission;

import android.content.pm.PackageParser;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PermissionSettings {
    @GuardedBy({"mLock"})
    final ArrayMap<String, ArraySet<String>> mAppOpPermissionPackages = new ArrayMap<>();
    private final Object mLock;
    @GuardedBy({"mLock"})
    final ArrayMap<String, PackageParser.PermissionGroup> mPermissionGroups = new ArrayMap<>();
    @GuardedBy({"mLock"})
    final ArrayMap<String, BasePermission> mPermissionTrees = new ArrayMap<>();
    @GuardedBy({"mLock"})
    final ArrayMap<String, BasePermission> mPermissions = new ArrayMap<>();

    PermissionSettings(Object lock) {
        this.mLock = lock;
    }

    public BasePermission getPermission(String permName) {
        BasePermission permissionLocked;
        synchronized (this.mLock) {
            permissionLocked = getPermissionLocked(permName);
        }
        return permissionLocked;
    }

    public void addAppOpPackage(String permName, String packageName) {
        ArraySet<String> pkgs = this.mAppOpPermissionPackages.get(permName);
        if (pkgs == null) {
            pkgs = new ArraySet<>();
            this.mAppOpPermissionPackages.put(permName, pkgs);
        }
        pkgs.add(packageName);
    }

    public void transferPermissions(String origPackageName, String newPackageName) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < 2) {
                for (BasePermission bp : (i == 0 ? this.mPermissionTrees : this.mPermissions).values()) {
                    bp.transfer(origPackageName, newPackageName);
                }
                i++;
            }
        }
    }

    public boolean canPropagatePermissionToInstantApp(String permName) {
        boolean z;
        synchronized (this.mLock) {
            BasePermission bp = this.mPermissions.get(permName);
            z = bp != null && (bp.isRuntime() || bp.isDevelopment()) && bp.isInstant();
        }
        return z;
    }

    public void readPermissions(XmlPullParser parser) throws IOException, XmlPullParserException {
        synchronized (this.mLock) {
            readPermissions(this.mPermissions, parser);
        }
    }

    public void readPermissionTrees(XmlPullParser parser) throws IOException, XmlPullParserException {
        synchronized (this.mLock) {
            readPermissions(this.mPermissionTrees, parser);
        }
    }

    public void writePermissions(XmlSerializer serializer) throws IOException {
        synchronized (this.mLock) {
            for (BasePermission bp : this.mPermissions.values()) {
                bp.writeLPr(serializer);
            }
        }
    }

    public void writePermissionTrees(XmlSerializer serializer) throws IOException {
        synchronized (this.mLock) {
            for (BasePermission bp : this.mPermissionTrees.values()) {
                bp.writeLPr(serializer);
            }
        }
    }

    public static void readPermissions(ArrayMap<String, BasePermission> out, XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (!BasePermission.readLPw(out, parser)) {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element reading permissions: " + parser.getName() + " at " + parser.getPositionDescription());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }

    public void dumpPermissions(PrintWriter pw, String packageName, ArraySet<String> permissionNames, boolean externalStorageEnforced, DumpState dumpState) {
        synchronized (this.mLock) {
            boolean printedSomething = false;
            for (BasePermission bp : this.mPermissions.values()) {
                printedSomething = bp.dumpPermissionsLPr(pw, packageName, permissionNames, externalStorageEnforced, printedSomething, dumpState);
            }
            if (packageName == null && permissionNames == null) {
                for (int iperm = 0; iperm < this.mAppOpPermissionPackages.size(); iperm++) {
                    if (iperm == 0) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        pw.println("AppOp Permissions:");
                    }
                    pw.print("  AppOp Permission ");
                    pw.print(this.mAppOpPermissionPackages.keyAt(iperm));
                    pw.println(":");
                    ArraySet<String> pkgs = this.mAppOpPermissionPackages.valueAt(iperm);
                    for (int ipkg = 0; ipkg < pkgs.size(); ipkg++) {
                        pw.print("    ");
                        pw.println(pkgs.valueAt(ipkg));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public BasePermission getPermissionLocked(String permName) {
        return this.mPermissions.get(permName);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public BasePermission getPermissionTreeLocked(String permName) {
        return this.mPermissionTrees.get(permName);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void putPermissionLocked(String permName, BasePermission permission) {
        this.mPermissions.put(permName, permission);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void putPermissionTreeLocked(String permName, BasePermission permission) {
        this.mPermissionTrees.put(permName, permission);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void removePermissionLocked(String permName) {
        this.mPermissions.remove(permName);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void removePermissionTreeLocked(String permName) {
        this.mPermissionTrees.remove(permName);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public Collection<BasePermission> getAllPermissionsLocked() {
        return this.mPermissions.values();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public Collection<BasePermission> getAllPermissionTreesLocked() {
        return this.mPermissionTrees.values();
    }

    /* access modifiers changed from: package-private */
    public BasePermission enforcePermissionTree(String permName, int callingUid) {
        BasePermission enforcePermissionTree;
        synchronized (this.mLock) {
            enforcePermissionTree = BasePermission.enforcePermissionTree(this.mPermissionTrees.values(), permName, callingUid);
        }
        return enforcePermissionTree;
    }

    public boolean isPermissionInstant(String permName) {
        boolean z;
        synchronized (this.mLock) {
            BasePermission bp = this.mPermissions.get(permName);
            z = bp != null && bp.isInstant();
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isPermissionAppOp(String permName) {
        boolean z;
        synchronized (this.mLock) {
            BasePermission bp = this.mPermissions.get(permName);
            z = bp != null && bp.isAppOp();
        }
        return z;
    }
}
