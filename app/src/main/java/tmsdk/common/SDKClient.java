package tmsdk.common;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdkobf.jl.b;

/* compiled from: Unknown */
public final class SDKClient extends b {
    private static volatile SDKClient Ac;
    private static ConcurrentLinkedQueue<MessageHandler> ud;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.SDKClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.SDKClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.SDKClient.<clinit>():void");
    }

    private SDKClient() {
    }

    public static boolean addMessageHandler(MessageHandler messageHandler) {
        return ud.add(messageHandler);
    }

    public static SDKClient getInstance() {
        if (Ac == null) {
            synchronized (SDKClient.class) {
                if (Ac == null) {
                    Ac = new SDKClient();
                }
            }
        }
        return Ac;
    }

    public static boolean removeMessageHandler(MessageHandler messageHandler) {
        return ud.remove(messageHandler);
    }

    public IBinder asBinder() {
        return this;
    }

    public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
        int what = dataEntity.what();
        Iterator it = ud.iterator();
        while (it.hasNext()) {
            MessageHandler messageHandler = (MessageHandler) it.next();
            if (messageHandler.isMatch(what)) {
                return messageHandler.onProcessing(dataEntity);
            }
        }
        return null;
    }
}
