package android.hardware.camera2.impl;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.impl.CallbackProxies;
import android.os.Binder;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.concurrent.Executor;

public class CallbackProxies {

    public static class SessionStateCallbackProxy extends CameraCaptureSession.StateCallback {
        private final CameraCaptureSession.StateCallback mCallback;
        private final Executor mExecutor;

        public SessionStateCallbackProxy(Executor executor, CameraCaptureSession.StateCallback callback) {
            this.mExecutor = (Executor) Preconditions.checkNotNull(executor, "executor must not be null");
            this.mCallback = (CameraCaptureSession.StateCallback) Preconditions.checkNotNull(callback, "callback must not be null");
        }

        public void onConfigured(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onConfigured(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onConfigureFailed(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onConfigureFailed(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onReady(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onReady(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onActive(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onActive(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onCaptureQueueEmpty(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onClosed(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onClosed(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session, surface) {
                    private final /* synthetic */ CameraCaptureSession f$1;
                    private final /* synthetic */ Surface f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.mCallback.onSurfacePrepared(this.f$1, this.f$2);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private CallbackProxies() {
        throw new AssertionError();
    }
}
