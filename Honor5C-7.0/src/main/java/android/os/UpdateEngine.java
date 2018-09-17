package android.os;

import android.os.IUpdateEngineCallback.Stub;
import android.util.Slog;

public class UpdateEngine {
    private static final String TAG = "UpdateEngine";
    private static final String UPDATE_ENGINE_SERVICE = "android.os.UpdateEngineService";
    private IUpdateEngine mUpdateEngine;

    /* renamed from: android.os.UpdateEngine.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ UpdateEngineCallback val$callback;
        final /* synthetic */ Handler val$handler;

        /* renamed from: android.os.UpdateEngine.1.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ UpdateEngineCallback val$callback;
            final /* synthetic */ float val$percent;
            final /* synthetic */ int val$status;

            AnonymousClass1(UpdateEngineCallback val$callback, int val$status, float val$percent) {
                this.val$callback = val$callback;
                this.val$status = val$status;
                this.val$percent = val$percent;
            }

            public void run() {
                this.val$callback.onStatusUpdate(this.val$status, this.val$percent);
            }
        }

        /* renamed from: android.os.UpdateEngine.1.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ UpdateEngineCallback val$callback;
            final /* synthetic */ int val$errorCode;

            AnonymousClass2(UpdateEngineCallback val$callback, int val$errorCode) {
                this.val$callback = val$callback;
                this.val$errorCode = val$errorCode;
            }

            public void run() {
                this.val$callback.onPayloadApplicationComplete(this.val$errorCode);
            }
        }

        AnonymousClass1(Handler val$handler, UpdateEngineCallback val$callback) {
            this.val$handler = val$handler;
            this.val$callback = val$callback;
        }

        public void onStatusUpdate(int status, float percent) {
            if (this.val$handler != null) {
                this.val$handler.post(new AnonymousClass1(this.val$callback, status, percent));
            } else {
                this.val$callback.onStatusUpdate(status, percent);
            }
        }

        public void onPayloadApplicationComplete(int errorCode) {
            if (this.val$handler != null) {
                this.val$handler.post(new AnonymousClass2(this.val$callback, errorCode));
            } else {
                this.val$callback.onPayloadApplicationComplete(errorCode);
            }
        }
    }

    public static final class ErrorCodeConstants {
        public static final int DOWNLOAD_PAYLOAD_VERIFICATION_ERROR = 12;
        public static final int DOWNLOAD_TRANSFER_ERROR = 9;
        public static final int ERROR = 1;
        public static final int FILESYSTEM_COPIER_ERROR = 4;
        public static final int INSTALL_DEVICE_OPEN_ERROR = 7;
        public static final int KERNEL_DEVICE_OPEN_ERROR = 8;
        public static final int PAYLOAD_HASH_MISMATCH_ERROR = 10;
        public static final int PAYLOAD_MISMATCHED_TYPE_ERROR = 6;
        public static final int PAYLOAD_SIZE_MISMATCH_ERROR = 11;
        public static final int POST_INSTALL_RUNNER_ERROR = 5;
        public static final int SUCCESS = 0;
    }

    public static final class UpdateStatusConstants {
        public static final int ATTEMPTING_ROLLBACK = 8;
        public static final int CHECKING_FOR_UPDATE = 1;
        public static final int COMPLETED = 11;
        public static final int DISABLED = 9;
        public static final int DOWNLOADING = 3;
        public static final int FAILED = 12;
        public static final int FINALIZING = 5;
        public static final int IDLE = 0;
        public static final int MEMPOOR = 13;
        public static final int ONGOING = 10;
        public static final int REPORTING_ERROR_EVENT = 7;
        public static final int UNSTABLE = 14;
        public static final int UPDATED_NEED_REBOOT = 6;
        public static final int UPDATE_AVAILABLE = 2;
        public static final int VERIFYING = 4;
    }

    public UpdateEngine() {
        this.mUpdateEngine = IUpdateEngine.Stub.asInterface(ServiceManager.getService(UPDATE_ENGINE_SERVICE));
    }

    public boolean bind(UpdateEngineCallback callback, Handler handler) {
        try {
            return this.mUpdateEngine.bind(new AnonymousClass1(handler, callback));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean bind(UpdateEngineCallback callback) {
        return bind(callback, null);
    }

    public int getProgress() {
        try {
            return this.mUpdateEngine.getProgress();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStatus() {
        try {
            return this.mUpdateEngine.getStatus();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void applyPayload(String url, long offset, long size, String[] headerKeyValuePairs) {
        try {
            this.mUpdateEngine.applyPayload(url, offset, size, headerKeyValuePairs);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void applyUpdateZip() {
        Slog.e(TAG, "try to applyUpdateZip");
        try {
            this.mUpdateEngine.applyUpdateZip();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void cancel() {
        try {
            this.mUpdateEngine.cancel();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void suspend() {
        try {
            this.mUpdateEngine.suspend();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resume() {
        try {
            this.mUpdateEngine.resume();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resetStatus() {
        try {
            this.mUpdateEngine.resetStatus();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setSlot() {
        try {
            return this.mUpdateEngine.setSlot();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
