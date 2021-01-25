package android.speech;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.PermissionChecker;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.speech.IRecognitionService;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class RecognitionService extends Service {
    private static final boolean DBG = false;
    private static final int MSG_CANCEL = 3;
    private static final int MSG_RESET = 4;
    private static final int MSG_START_LISTENING = 1;
    private static final int MSG_STOP_LISTENING = 2;
    public static final String SERVICE_INTERFACE = "android.speech.RecognitionService";
    public static final String SERVICE_META_DATA = "android.speech";
    private static final String TAG = "RecognitionService";
    private RecognitionServiceBinder mBinder = new RecognitionServiceBinder(this);
    private Callback mCurrentCallback = null;
    private final Handler mHandler = new Handler() {
        /* class android.speech.RecognitionService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                StartListeningArgs args = (StartListeningArgs) msg.obj;
                RecognitionService.this.dispatchStartListening(args.mIntent, args.mListener, args.mCallingUid);
            } else if (i == 2) {
                RecognitionService.this.dispatchStopListening((IRecognitionListener) msg.obj);
            } else if (i == 3) {
                RecognitionService.this.dispatchCancel((IRecognitionListener) msg.obj);
            } else if (i == 4) {
                RecognitionService.this.dispatchClearCallback();
            }
        }
    };

    /* access modifiers changed from: protected */
    public abstract void onCancel(Callback callback);

    /* access modifiers changed from: protected */
    public abstract void onStartListening(Intent intent, Callback callback);

    /* access modifiers changed from: protected */
    public abstract void onStopListening(Callback callback);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchStartListening(Intent intent, final IRecognitionListener listener, int callingUid) {
        if (this.mCurrentCallback == null) {
            try {
                listener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    /* class android.speech.RecognitionService.AnonymousClass2 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        RecognitionService.this.mHandler.sendMessage(RecognitionService.this.mHandler.obtainMessage(3, listener));
                    }
                }, 0);
                this.mCurrentCallback = new Callback(listener, callingUid);
                onStartListening(intent, this.mCurrentCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "dead listener on startListening");
            }
        } else {
            try {
                listener.onError(8);
            } catch (RemoteException e2) {
                Log.d(TAG, "onError call from startListening failed");
            }
            Log.i(TAG, "concurrent startListening received - ignoring this call");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchStopListening(IRecognitionListener listener) {
        try {
            if (this.mCurrentCallback == null) {
                listener.onError(5);
                Log.w(TAG, "stopListening called with no preceding startListening - ignoring");
            } else if (this.mCurrentCallback.mListener.asBinder() != listener.asBinder()) {
                listener.onError(8);
                Log.w(TAG, "stopListening called by other caller than startListening - ignoring");
            } else {
                onStopListening(this.mCurrentCallback);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "onError call from stopListening failed");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchCancel(IRecognitionListener listener) {
        Callback callback = this.mCurrentCallback;
        if (callback != null) {
            if (callback.mListener.asBinder() != listener.asBinder()) {
                Log.w(TAG, "cancel called by client who did not call startListening - ignoring");
                return;
            }
            onCancel(this.mCurrentCallback);
            this.mCurrentCallback = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchClearCallback() {
        this.mCurrentCallback = null;
    }

    private class StartListeningArgs {
        public final int mCallingUid;
        public final Intent mIntent;
        public final IRecognitionListener mListener;

        public StartListeningArgs(Intent intent, IRecognitionListener listener, int callingUid) {
            this.mIntent = intent;
            this.mListener = listener;
            this.mCallingUid = callingUid;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPermissions(IRecognitionListener listener, boolean forDataDelivery) {
        if (forDataDelivery) {
            if (PermissionChecker.checkCallingOrSelfPermissionForDataDelivery(this, Manifest.permission.RECORD_AUDIO) == 0) {
                return true;
            }
        } else if (PermissionChecker.checkCallingOrSelfPermissionForPreflight(this, Manifest.permission.RECORD_AUDIO) == 0) {
            return true;
        }
        try {
            Log.e(TAG, "call for recognition service without RECORD_AUDIO permissions");
            listener.onError(9);
            return false;
        } catch (RemoteException re) {
            Log.e(TAG, "sending ERROR_INSUFFICIENT_PERMISSIONS message failed", re);
            return false;
        }
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mCurrentCallback = null;
        this.mBinder.clearReference();
        super.onDestroy();
    }

    public class Callback {
        private final int mCallingUid;
        private final IRecognitionListener mListener;

        private Callback(IRecognitionListener listener, int callingUid) {
            this.mListener = listener;
            this.mCallingUid = callingUid;
        }

        public void beginningOfSpeech() throws RemoteException {
            this.mListener.onBeginningOfSpeech();
        }

        public void bufferReceived(byte[] buffer) throws RemoteException {
            this.mListener.onBufferReceived(buffer);
        }

        public void endOfSpeech() throws RemoteException {
            this.mListener.onEndOfSpeech();
        }

        public void error(int error) throws RemoteException {
            Message.obtain(RecognitionService.this.mHandler, 4).sendToTarget();
            this.mListener.onError(error);
        }

        public void partialResults(Bundle partialResults) throws RemoteException {
            this.mListener.onPartialResults(partialResults);
        }

        public void readyForSpeech(Bundle params) throws RemoteException {
            this.mListener.onReadyForSpeech(params);
        }

        public void results(Bundle results) throws RemoteException {
            Message.obtain(RecognitionService.this.mHandler, 4).sendToTarget();
            this.mListener.onResults(results);
        }

        public void rmsChanged(float rmsdB) throws RemoteException {
            this.mListener.onRmsChanged(rmsdB);
        }

        public int getCallingUid() {
            return this.mCallingUid;
        }
    }

    private static final class RecognitionServiceBinder extends IRecognitionService.Stub {
        private final WeakReference<RecognitionService> mServiceRef;

        public RecognitionServiceBinder(RecognitionService service) {
            this.mServiceRef = new WeakReference<>(service);
        }

        @Override // android.speech.IRecognitionService
        public void startListening(Intent recognizerIntent, IRecognitionListener listener) {
            RecognitionService service = this.mServiceRef.get();
            if (service != null && service.checkPermissions(listener, true)) {
                Handler handler = service.mHandler;
                Handler handler2 = service.mHandler;
                Objects.requireNonNull(service);
                handler.sendMessage(Message.obtain(handler2, 1, new StartListeningArgs(recognizerIntent, listener, Binder.getCallingUid())));
            }
        }

        @Override // android.speech.IRecognitionService
        public void stopListening(IRecognitionListener listener) {
            RecognitionService service = this.mServiceRef.get();
            if (service != null && service.checkPermissions(listener, false)) {
                service.mHandler.sendMessage(Message.obtain(service.mHandler, 2, listener));
            }
        }

        @Override // android.speech.IRecognitionService
        public void cancel(IRecognitionListener listener) {
            RecognitionService service = this.mServiceRef.get();
            if (service != null && service.checkPermissions(listener, false)) {
                service.mHandler.sendMessage(Message.obtain(service.mHandler, 3, listener));
            }
        }

        public void clearReference() {
            this.mServiceRef.clear();
        }
    }
}
