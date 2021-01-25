package android.content.pm;

import android.content.pm.PackageManagerInternal;
import com.android.server.LocalServices;
import java.util.List;

public class PackageList implements PackageManagerInternal.PackageListObserver, AutoCloseable {
    private final List<String> mPackageNames;
    private final PackageManagerInternal.PackageListObserver mWrappedObserver;

    public PackageList(List<String> packageNames, PackageManagerInternal.PackageListObserver observer) {
        this.mPackageNames = packageNames;
        this.mWrappedObserver = observer;
    }

    @Override // android.content.pm.PackageManagerInternal.PackageListObserver
    public void onPackageAdded(String packageName, int uid) {
        PackageManagerInternal.PackageListObserver packageListObserver = this.mWrappedObserver;
        if (packageListObserver != null) {
            packageListObserver.onPackageAdded(packageName, uid);
        }
    }

    @Override // android.content.pm.PackageManagerInternal.PackageListObserver
    public void onPackageRemoved(String packageName, int uid) {
        PackageManagerInternal.PackageListObserver packageListObserver = this.mWrappedObserver;
        if (packageListObserver != null) {
            packageListObserver.onPackageRemoved(packageName, uid);
        }
    }

    @Override // android.content.pm.PackageManagerInternal.PackageListObserver
    public void onPackageChanged(String packageName, int uid) {
        PackageManagerInternal.PackageListObserver packageListObserver = this.mWrappedObserver;
        if (packageListObserver != null) {
            packageListObserver.onPackageChanged(packageName, uid);
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() throws Exception {
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).removePackageListObserver(this);
    }

    public List<String> getPackageNames() {
        return this.mPackageNames;
    }
}
