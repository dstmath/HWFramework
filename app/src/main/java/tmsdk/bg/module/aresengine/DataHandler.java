package tmsdk.bg.module.aresengine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
public class DataHandler extends Handler {
    private static final Looper wQ = null;
    private ConcurrentLinkedQueue<DataHandlerCallback> wR;

    /* compiled from: Unknown */
    public interface DataHandlerCallback {
        void onCallback(TelephonyEntity telephonyEntity, int i, int i2, Object... objArr);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.module.aresengine.DataHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.module.aresengine.DataHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.module.aresengine.DataHandler.<clinit>():void");
    }

    public DataHandler() {
        super(wQ);
        this.wR = new ConcurrentLinkedQueue();
    }

    public final void addCallback(DataHandlerCallback dataHandlerCallback) {
        this.wR.add(dataHandlerCallback);
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
            Iterator it2 = this.wR.iterator();
            while (it2.hasNext()) {
                ((DataHandlerCallback) it2.next()).onCallback(telephonyEntity, i, i2, objArr);
            }
        }
    }

    public final void removeCallback(DataHandlerCallback dataHandlerCallback) {
        this.wR.remove(dataHandlerCallback);
    }

    public synchronized void sendMessage(FilterResult filterResult) {
        if (filterResult != null) {
            Message obtainMessage = obtainMessage(3456);
            obtainMessage.obj = filterResult;
            obtainMessage.sendToTarget();
        }
    }
}
