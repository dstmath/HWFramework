package android.security;

public abstract class ConfirmationCallback {
    public void onConfirmed(byte[] dataThatWasConfirmed) {
    }

    public void onDismissed() {
    }

    public void onCanceled() {
    }

    public void onError(Throwable e) {
    }
}
