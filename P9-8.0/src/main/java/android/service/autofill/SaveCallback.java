package android.service.autofill;

import android.os.RemoteException;

public final class SaveCallback {
    private final ISaveCallback mCallback;
    private boolean mCalled;

    SaveCallback(ISaveCallback callback) {
        this.mCallback = callback;
    }

    public void onSuccess() {
        assertNotCalled();
        this.mCalled = true;
        try {
            this.mCallback.onSuccess();
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public void onFailure(CharSequence message) {
        assertNotCalled();
        this.mCalled = true;
        try {
            this.mCallback.onFailure(message);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    private void assertNotCalled() {
        if (this.mCalled) {
            throw new IllegalStateException("Already called");
        }
    }
}
