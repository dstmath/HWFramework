package ohos.interwork.eventhandler;

import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import ohos.eventhandler.Courier;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public final class CourierExProxyAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108208, "CourierExProxyAdapter");
    private static final int LOG_DOMAIN = 218108208;
    private Messenger messenger = null;

    CourierExProxyAdapter(IRemoteObject iRemoteObject) {
        Object orElse = IPCAdapter.translateToIBinder(iRemoteObject).orElse(null);
        if (orElse instanceof IBinder) {
            this.messenger = new Messenger((IBinder) orElse);
        }
    }

    /* access modifiers changed from: package-private */
    public void send(InnerEvent innerEvent) throws RemoteException {
        if (this.messenger == null) {
            HiLog.error(LABEL, "Proxy::send remote is null", new Object[0]);
            return;
        }
        try {
            this.messenger.send(convertToMessage(innerEvent));
        } catch (android.os.RemoteException unused) {
            throw new RemoteException("CourierEx remote exception");
        }
    }

    private static Message convertToMessage(InnerEvent innerEvent) {
        if (innerEvent == null) {
            return null;
        }
        Message obtain = Message.obtain();
        obtain.what = innerEvent.eventId;
        if (innerEvent.object instanceof InnerEventExInfo) {
            InnerEventExInfo innerEventExInfo = (InnerEventExInfo) innerEvent.object;
            obtain.arg1 = innerEventExInfo.arg1;
            obtain.arg2 = innerEventExInfo.arg2;
        }
        obtain.replyTo = getMessengerFromCourier(innerEvent.replyTo);
        return obtain;
    }

    private static Messenger getMessengerFromCourier(Courier courier) {
        if (courier == null) {
            return null;
        }
        Object orElse = IPCAdapter.translateToIBinder(courier.getRemoteObject()).orElse(null);
        if (orElse instanceof IBinder) {
            return new Messenger((IBinder) orElse);
        }
        return null;
    }
}
