package android.service.autofill;

import android.os.RemoteException;
import android.util.Log;

public final class FillCallback {
    private static final String TAG = "FillCallback";
    private final IFillCallback mCallback;
    private boolean mCalled;
    private final int mRequestId;

    public FillCallback(IFillCallback callback, int requestId) {
        this.mCallback = callback;
        this.mRequestId = requestId;
    }

    public void onSuccess(FillResponse response) {
        assertNotCalled();
        this.mCalled = true;
        if (response != null) {
            response.setRequestId(this.mRequestId);
        }
        try {
            this.mCallback.onSuccess(response);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public void onFailure(CharSequence message) {
        Log.w(TAG, "onFailure(): " + ((Object) message));
        assertNotCalled();
        this.mCalled = true;
        try {
            this.mCallback.onFailure(this.mRequestId, message);
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
