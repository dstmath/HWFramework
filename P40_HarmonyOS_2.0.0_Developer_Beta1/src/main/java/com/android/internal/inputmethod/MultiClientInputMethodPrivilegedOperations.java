package com.android.internal.inputmethod;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.InputChannel;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.view.IInputMethodSession;

public class MultiClientInputMethodPrivilegedOperations {
    private static final String TAG = "MultiClientInputMethodPrivilegedOperations";
    private final OpsHolder mOps = new OpsHolder();

    /* access modifiers changed from: private */
    public static final class OpsHolder {
        @GuardedBy({"this"})
        private IMultiClientInputMethodPrivilegedOperations mPrivOps;

        private OpsHolder() {
        }

        public synchronized void set(IMultiClientInputMethodPrivilegedOperations privOps) {
            if (this.mPrivOps == null) {
                this.mPrivOps = privOps;
            } else {
                throw new IllegalStateException("IMultiClientInputMethodPrivilegedOperations must be set at most once. privOps=" + privOps);
            }
        }

        private static String getCallerMethodName() {
            StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
            if (callStack.length <= 4) {
                return "<bottom of call stack>";
            }
            return callStack[4].getMethodName();
        }

        public synchronized void dispose() {
            this.mPrivOps = null;
        }

        public synchronized IMultiClientInputMethodPrivilegedOperations getAndWarnIfNull() {
            if (this.mPrivOps == null) {
                Log.e(MultiClientInputMethodPrivilegedOperations.TAG, getCallerMethodName() + " is ignored. Call it within attachToken() and InputMethodService.onDestroy()");
            }
            return this.mPrivOps;
        }
    }

    public void set(IMultiClientInputMethodPrivilegedOperations privOps) {
        this.mOps.set(privOps);
    }

    public void dispose() {
        this.mOps.dispose();
    }

    public IBinder createInputMethodWindowToken(int displayId) {
        IMultiClientInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops == null) {
            return null;
        }
        try {
            return ops.createInputMethodWindowToken(displayId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteInputMethodWindowToken(IBinder token) {
        IMultiClientInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.deleteInputMethodWindowToken(token);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void acceptClient(int clientId, IInputMethodSession session, IMultiClientInputMethodSession multiClientSession, InputChannel writeChannel) {
        IMultiClientInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.acceptClient(clientId, session, multiClientSession, writeChannel);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportImeWindowTarget(int clientId, int targetWindowHandle, IBinder imeWindowToken) {
        IMultiClientInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.reportImeWindowTarget(clientId, targetWindowHandle, imeWindowToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isUidAllowedOnDisplay(int displayId, int uid) {
        IMultiClientInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops == null) {
            return false;
        }
        try {
            return ops.isUidAllowedOnDisplay(displayId, uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setActive(int clientId, boolean active) {
        IMultiClientInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.setActive(clientId, active);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
