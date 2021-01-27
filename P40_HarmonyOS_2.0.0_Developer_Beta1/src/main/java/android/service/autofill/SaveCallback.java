package android.service.autofill;

import android.content.IntentSender;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;

public final class SaveCallback {
    private static final String TAG = "SaveCallback";
    private final ISaveCallback mCallback;
    private boolean mCalled;

    SaveCallback(ISaveCallback callback) {
        this.mCallback = callback;
    }

    public void onSuccess() {
        onSuccessInternal(null);
    }

    public void onSuccess(IntentSender intentSender) {
        onSuccessInternal((IntentSender) Preconditions.checkNotNull(intentSender));
    }

    private void onSuccessInternal(IntentSender intentSender) {
        assertNotCalled();
        this.mCalled = true;
        try {
            this.mCallback.onSuccess(intentSender);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public void onFailure(CharSequence message) {
        Log.w(TAG, "onFailure(): " + ((Object) message));
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
