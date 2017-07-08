package android.app.backup;

public abstract class RestoreObserver {
    public void restoreSetsAvailable(RestoreSet[] result) {
    }

    public void restoreStarting(int numPackages) {
    }

    public void onUpdate(int nowBeingRestored, String currentPackage) {
    }

    public void restoreFinished(int error) {
    }
}
