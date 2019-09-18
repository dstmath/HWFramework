package com.android.internal.policy;

import android.os.RemoteException;
import com.android.internal.policy.IKeyguardDismissCallback;

public class KeyguardDismissCallback extends IKeyguardDismissCallback.Stub {
    public void onDismissError() throws RemoteException {
    }

    public void onDismissSucceeded() throws RemoteException {
    }

    public void onDismissCancelled() throws RemoteException {
    }
}
