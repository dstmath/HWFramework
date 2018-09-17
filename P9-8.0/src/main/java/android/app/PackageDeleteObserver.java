package android.app;

import android.content.Intent;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageDeleteObserver2.Stub;

public class PackageDeleteObserver {
    private final Stub mBinder = new Stub() {
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
