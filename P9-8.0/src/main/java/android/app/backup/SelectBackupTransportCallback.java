package android.app.backup;

public abstract class SelectBackupTransportCallback {
    public void onSuccess(String transportName) {
    }

    public void onFailure(int reason) {
    }
}
