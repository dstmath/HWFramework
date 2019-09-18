package android.app;

import android.content.Intent;
import android.content.pm.IPackageDeleteObserver2;

public class PackageDeleteObserver {
    private final IPackageDeleteObserver2.Stub mBinder = new IPackageDeleteObserver2.Stub() {
        public void onUserActionRequired(Intent intent) {
            PackageDeleteObserver.this.onUserActionRequired(intent);
        }

        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            PackageDeleteObserver.this.onPackageDeleted(basePackageName, returnCode, msg);
        }
    };

    public IPackageDeleteObserver2 getBinder() {
        return this.mBinder;
    }

    public void onUserActionRequired(Intent intent) {
    }

    public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
    }
}
