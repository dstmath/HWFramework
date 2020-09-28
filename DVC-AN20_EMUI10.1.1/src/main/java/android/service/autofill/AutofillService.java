package android.service.autofill;

import android.app.Service;
import android.content.Intent;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.Looper;
import android.os.RemoteException;
import android.service.autofill.IAutoFillService;
import android.util.Log;
import android.view.autofill.AutofillManager;
import com.android.internal.util.function.pooled.PooledLambda;

public abstract class AutofillService extends Service {
    public static final String SERVICE_INTERFACE = "android.service.autofill.AutofillService";
    public static final String SERVICE_META_DATA = "android.autofill";
    private static final String TAG = "AutofillService";
    private Handler mHandler;
    private final IAutoFillService mInterface = new IAutoFillService.Stub() {
        /* class android.service.autofill.AutofillService.AnonymousClass1 */

        @Override // android.service.autofill.IAutoFillService
        public void onConnectedStateChanged(boolean connected) {
            AutofillService.this.mHandler.sendMessage(PooledLambda.obtainMessage(connected ? $$Lambda$amIBeR2CTPTUHkT8htLcarZmUYc.INSTANCE : $$Lambda$eWz26esczusoIA84WEwFlxQuDGQ.INSTANCE, AutofillService.this));
        }

        @Override // android.service.autofill.IAutoFillService
        public void onFillRequest(FillRequest request, IFillCallback callback) {
            ICancellationSignal transport = CancellationSignal.createTransport();
            try {
                callback.onCancellable(transport);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
            AutofillService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$I0gCKFrBTO70VZfSZTq2fjwyG8.INSTANCE, AutofillService.this, request, CancellationSignal.fromTransport(transport), new FillCallback(callback, request.getId())));
        }

        @Override // android.service.autofill.IAutoFillService
        public void onSaveRequest(SaveRequest request, ISaveCallback callback) {
            AutofillService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$KrOZIsyY3lh3prHWFldsWopHBw.INSTANCE, AutofillService.this, request, new SaveCallback(callback)));
        }
    };

    public abstract void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, FillCallback fillCallback);

    public abstract void onSaveRequest(SaveRequest saveRequest, SaveCallback saveCallback);

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mHandler = new Handler(Looper.getMainLooper(), null, true);
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mInterface.asBinder();
        }
        Log.w(TAG, "Tried to bind to wrong intent (should be android.service.autofill.AutofillService: " + intent);
        return null;
    }

    public void onConnected() {
    }

    public void onDisconnected() {
    }

    public final FillEventHistory getFillEventHistory() {
        AutofillManager afm = (AutofillManager) getSystemService(AutofillManager.class);
        if (afm == null) {
            return null;
        }
        return afm.getFillEventHistory();
    }
}
