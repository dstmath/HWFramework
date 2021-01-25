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

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigured(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$soW0qC12Osypoky6AfL3P2TeDw */
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onConfigured$0$CallbackProxies$SessionStateCallbackProxy(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onConfigured$0$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session) {
            this.mCallback.onConfigured(session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigureFailed(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$gvbTsp9UPpKJAbdycdci_ZW5BeI */
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onConfigureFailed$1$CallbackProxies$SessionStateCallbackProxy(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onConfigureFailed$1$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session) {
            this.mCallback.onConfigureFailed(session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onReady(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$HoziT1tD_pl7sCGu4flyoxB90 */
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onReady$2$CallbackProxies$SessionStateCallbackProxy(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onReady$2$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session) {
            this.mCallback.onReady(session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onActive(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$ISQyEhLUI1khcOCin3OIsRyTUoU */
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onActive$3$CallbackProxies$SessionStateCallbackProxy(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onActive$3$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session) {
            this.mCallback.onActive(session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onCaptureQueueEmpty(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$hoQOYc189Bss2NBtrutabMRw4VU */
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onCaptureQueueEmpty$4$CallbackProxies$SessionStateCallbackProxy(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onCaptureQueueEmpty$4$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session) {
            this.mCallback.onCaptureQueueEmpty(session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onClosed(CameraCaptureSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$9H0ZdANdMrdpoq2bfIL2l3DVsKk */
                    private final /* synthetic */ CameraCaptureSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onClosed$5$CallbackProxies$SessionStateCallbackProxy(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onClosed$5$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session) {
            this.mCallback.onClosed(session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(session, surface) {
                    /* class android.hardware.camera2.impl.$$Lambda$CallbackProxies$SessionStateCallbackProxy$tuajQwbKz3BV5CZZJdjl97HF6Tw */
                    private final /* synthetic */ CameraCaptureSession f$1;
                    private final /* synthetic */ Surface f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        CallbackProxies.SessionStateCallbackProxy.this.lambda$onSurfacePrepared$6$CallbackProxies$SessionStateCallbackProxy(this.f$1, this.f$2);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public /* synthetic */ void lambda$onSurfacePrepared$6$CallbackProxies$SessionStateCallbackProxy(CameraCaptureSession session, Surface surface) {
            this.mCallback.onSurfacePrepared(session, surface);
        }
    }

    private CallbackProxies() {
        throw new AssertionError();
    }
}
