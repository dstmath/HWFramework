package tmsdk.bg.module.aresengine;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdkobf.im;

public class DataHandler extends Handler {
    private static final Looper tU;
    private ConcurrentLinkedQueue<DataHandlerCallback> tV = new ConcurrentLinkedQueue();

    public interface DataHandlerCallback {
        void onCallback(TelephonyEntity telephonyEntity, int i, int i2, Object... objArr);
    }

    static {
        HandlerThread newFreeHandlerThread = im.bJ().newFreeHandlerThread(DataHandler.class.getName());
        newFreeHandlerThread.start();
        tU = newFreeHandlerThread.getLooper();
    }

    public DataHandler() {
        super(tU);
    }

    public final void addCallback(DataHandlerCallback dataHandlerCallback) {
        this.tV.add(dataHandlerCallback);
    }

    public void handleMessage(Message message) {
        if (message.what == 3456) {
            FilterResult filterResult = (FilterResult) message.obj;
            Iterator it = filterResult.mDotos.iterator();
            while (it.hasNext()) {
                Runnable runnable = (Runnable) it.next();
                if (runnable instanceof Thread) {
                    ((Thread) runnable).start();
                } else {
                    runnable.run();
                }
            }
            TelephonyEntity telephonyEntity = filterResult.mData;
            int i = filterResult.mFilterfiled;
            int i2 = filterResult.mState;
            Object[] objArr = filterResult.mParams;
            Iterator it2 = this.tV.iterator();
            while (it2.hasNext()) {
                ((DataHandlerCallback) it2.next()).onCallback(telephonyEntity, i, i2, objArr);
            }
        }
    }

    public final void removeCallback(DataHandlerCallback dataHandlerCallback) {
        this.tV.remove(dataHandlerCallback);
    }

    public synchronized void sendMessage(FilterResult filterResult) {
        if (filterResult != null) {
            Message obtainMessage = obtainMessage(3456);
            obtainMessage.obj = filterResult;
            obtainMessage.sendToTarget();
        }
    }
}
