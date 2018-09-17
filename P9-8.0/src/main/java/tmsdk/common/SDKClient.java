package tmsdk.common;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdkobf.ih.b;

public final class SDKClient extends b {
    private static ConcurrentLinkedQueue<MessageHandler> rA = new ConcurrentLinkedQueue();
    private static volatile SDKClient xo = null;

    private SDKClient() {
    }

    public static boolean addMessageHandler(MessageHandler messageHandler) {
        return rA.add(messageHandler);
    }

    public static SDKClient getInstance() {
        if (xo == null) {
            Class cls = SDKClient.class;
            synchronized (SDKClient.class) {
                if (xo == null) {
                    xo = new SDKClient();
                }
            }
        }
        return xo;
    }

    public static boolean removeMessageHandler(MessageHandler messageHandler) {
        return rA.remove(messageHandler);
    }

    public IBinder asBinder() {
        return this;
    }

    public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
        int what = dataEntity.what();
        Iterator it = rA.iterator();
        while (it.hasNext()) {
            MessageHandler messageHandler = (MessageHandler) it.next();
            if (messageHandler.isMatch(what)) {
                return messageHandler.onProcessing(dataEntity);
            }
        }
        return null;
    }
}
