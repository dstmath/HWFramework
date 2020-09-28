package com.android.internal.inputmethod;

import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.annotations.GuardedBy;

public final class InputMethodPrivilegedOperations {
    private static final String TAG = "InputMethodPrivilegedOperations";
    private final OpsHolder mOps = new OpsHolder();

    /* access modifiers changed from: private */
    public static final class OpsHolder {
        @GuardedBy({"this"})
        private IInputMethodPrivilegedOperations mPrivOps;

        private OpsHolder() {
        }

        public synchronized void set(IInputMethodPrivilegedOperations privOps) {
            if (this.mPrivOps == null) {
                this.mPrivOps = privOps;
            } else {
                throw new IllegalStateException("IInputMethodPrivilegedOperations must be set at most once. privOps=" + privOps);
            }
        }

        private static String getCallerMethodName() {
            StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
            if (callStack.length <= 4) {
                return "<bottom of call stack>";
            }
            return callStack[4].getMethodName();
        }

        public synchronized IInputMethodPrivilegedOperations getAndWarnIfNull() {
            if (this.mPrivOps == null) {
                Log.e(InputMethodPrivilegedOperations.TAG, getCallerMethodName() + " is ignored. Call it within attachToken() and InputMethodService.onDestroy()");
            }
            return this.mPrivOps;
        }
    }

    public void set(IInputMethodPrivilegedOperations privOps) {
        this.mOps.set(privOps);
    }

    public void setImeWindowStatus(int vis, int backDisposition) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.setImeWindowStatus(vis, backDisposition);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportStartInput(IBinder startInputToken) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.reportStartInput(startInputToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public IInputContentUriToken createInputContentUriToken(Uri contentUri, String packageName) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops == null) {
            return null;
        }
        try {
            return ops.createInputContentUriToken(contentUri, packageName);
        } catch (RemoteException e) {
            return null;
        }
    }

    public void reportFullscreenMode(boolean fullscreen) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.reportFullscreenMode(fullscreen);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void updateStatusIcon(String packageName, int iconResId) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.updateStatusIcon(packageName, iconResId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setInputMethod(String id) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.setInputMethod(id);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setInputMethodAndSubtype(String id, InputMethodSubtype subtype) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.setInputMethodAndSubtype(id, subtype);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void hideMySoftInput(int flags) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.hideMySoftInput(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void showMySoftInput(int flags) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.showMySoftInput(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean switchToPreviousInputMethod() {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops == null) {
            return false;
        }
        try {
            return ops.switchToPreviousInputMethod();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean switchToNextInputMethod(boolean onlyCurrentIme) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops == null) {
            return false;
        }
        try {
            return ops.switchToNextInputMethod(onlyCurrentIme);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean shouldOfferSwitchingToNextInputMethod() {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops == null) {
            return false;
        }
        try {
            return ops.shouldOfferSwitchingToNextInputMethod();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void notifyUserAction() {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.notifyUserAction();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void reportPreRendered(EditorInfo info) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.reportPreRendered(info);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void applyImeVisibility(boolean setVisible) {
        IInputMethodPrivilegedOperations ops = this.mOps.getAndWarnIfNull();
        if (ops != null) {
            try {
                ops.applyImeVisibility(setVisible);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
