package android.service.autofill;

import android.app.Service;
import android.content.Intent;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.service.autofill.IAutoFillService.Stub;
import android.util.Log;
import android.view.autofill.AutofillManager;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.internal.os.SomeArgs;

public abstract class AutofillService extends Service {
    private static final int MSG_CONNECT = 1;
    private static final int MSG_DISCONNECT = 2;
    private static final int MSG_ON_FILL_REQUEST = 3;
    private static final int MSG_ON_SAVE_REQUEST = 4;
    public static final String SERVICE_INTERFACE = "android.service.autofill.AutofillService";
    public static final String SERVICE_META_DATA = "android.autofill";
    private static final String TAG = "AutofillService";
    private final Callback mHandlerCallback = new -$Lambda$svbjmB3NFhHnuZrn67G14PFSJlY(this);
    private HandlerCaller mHandlerCaller;
    private final IAutoFillService mInterface = new Stub() {
        public void onConnectedStateChanged(boolean connected) {
            if (connected) {
                AutofillService.this.mHandlerCaller.obtainMessage(1).sendToTarget();
            } else {
                AutofillService.this.mHandlerCaller.obtainMessage(2).sendToTarget();
            }
        }

        public void onFillRequest(FillRequest request, IFillCallback callback) {
            ICancellationSignal transport = CancellationSignal.createTransport();
            try {
                callback.onCancellable(transport);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
            AutofillService.this.mHandlerCaller.obtainMessageOOO(3, request, CancellationSignal.fromTransport(transport), callback).sendToTarget();
        }

        public void onSaveRequest(SaveRequest request, ISaveCallback callback) {
            AutofillService.this.mHandlerCaller.obtainMessageOO(4, request, callback).sendToTarget();
        }
    };

    public abstract void onFillRequest(FillRequest fillRequest, CancellationSignal cancellationSignal, FillCallback fillCallback);

    public abstract void onSaveRequest(SaveRequest saveRequest, SaveCallback saveCallback);

    /* synthetic */ void lambda$-android_service_autofill_AutofillService_15826(Message msg) {
        SomeArgs args;
        switch (msg.what) {
            case 1:
                onConnected();
                return;
            case 2:
                onDisconnected();
                return;
            case 3:
                args = msg.obj;
                FillRequest request = args.arg1;
                CancellationSignal cancellation = args.arg2;
                FillCallback fillCallback = new FillCallback(args.arg3, request.getId());
                args.recycle();
                onFillRequest(request, cancellation, fillCallback);
                return;
            case 4:
                args = (SomeArgs) msg.obj;
                SaveRequest request2 = args.arg1;
                SaveCallback saveCallback = new SaveCallback(args.arg2);
                args.recycle();
                onSaveRequest(request2, saveCallback);
                return;
            default:
                Log.w(TAG, "MyCallbacks received invalid message type in HandlerCaller.Callback");
                return;
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mHandlerCaller = new HandlerCaller(null, Looper.getMainLooper(), this.mHandlerCallback, true);
    }

    public final IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mInterface.asBinder();
        }
        Log.w(TAG, "Tried to bind to wrong intent: " + intent);
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
