package ohos.app;

import ohos.appexecfwk.utils.AppLog;
import ohos.eventhandler.EventHandler;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class GeneralReceiver implements Sequenceable {
    public static final Sequenceable.Producer<GeneralReceiver> PRODUCER = $$Lambda$GeneralReceiver$sa_TCetJeNeZwxiW6Wk5PtaNz28.INSTANCE;
    private final Object LOCK;
    final EventHandler handler;
    boolean local;
    IGeneralReceiver receiver;

    /* access modifiers changed from: protected */
    public void onReceive(int i, PacMap pacMap) {
    }

    class MyRunnable implements Runnable {
        final int code;
        final PacMap data;

        MyRunnable(int i, PacMap pacMap) {
            this.code = i;
            this.data = pacMap;
        }

        @Override // java.lang.Runnable
        public void run() {
            GeneralReceiver.this.onReceive(this.code, this.data);
        }
    }

    class MyReceiver extends GeneralReceiverSkeleton {
        MyReceiver() {
        }

        @Override // ohos.app.GeneralReceiverSkeleton, ohos.app.IGeneralReceiver
        public void sendResult(int i, PacMap pacMap) {
            if (GeneralReceiver.this.handler != null) {
                GeneralReceiver.this.handler.postTask(new MyRunnable(i, pacMap));
            } else {
                GeneralReceiver.this.onReceive(i, pacMap);
            }
        }
    }

    public GeneralReceiver() {
        this(null);
    }

    public GeneralReceiver(EventHandler eventHandler) {
        this.LOCK = new Object();
        this.local = true;
        this.handler = eventHandler;
    }

    static /* synthetic */ GeneralReceiver lambda$static$0(Parcel parcel) {
        GeneralReceiver generalReceiver = new GeneralReceiver();
        generalReceiver.unmarshalling(parcel);
        return generalReceiver;
    }

    public void sendResult(int i, PacMap pacMap) {
        if (this.local) {
            EventHandler eventHandler = this.handler;
            if (eventHandler != null) {
                eventHandler.postTask(new MyRunnable(i, pacMap));
            } else {
                onReceive(i, pacMap);
            }
        } else {
            IGeneralReceiver iGeneralReceiver = this.receiver;
            if (iGeneralReceiver != null) {
                try {
                    iGeneralReceiver.sendResult(i, pacMap);
                } catch (RemoteException unused) {
                    AppLog.e("GeneralReceiver sendResult RemoteException", new Object[0]);
                }
            }
        }
    }

    public boolean marshalling(Parcel parcel) {
        if (!(parcel instanceof MessageParcel)) {
            return false;
        }
        synchronized (this.LOCK) {
            if (this.receiver == null) {
                this.receiver = new MyReceiver();
            }
        }
        return ((MessageParcel) parcel).writeRemoteObject(this.receiver.asObject());
    }

    public boolean unmarshalling(Parcel parcel) {
        this.local = false;
        if (!(parcel instanceof MessageParcel)) {
            return true;
        }
        this.receiver = GeneralReceiverSkeleton.asInterface(((MessageParcel) parcel).readRemoteObject());
        return true;
    }
}
