package libcore.net.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkEventDispatcher {
    private static final NetworkEventDispatcher instance = null;
    private final List<NetworkEventListener> listeners;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.net.event.NetworkEventDispatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.net.event.NetworkEventDispatcher.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.net.event.NetworkEventDispatcher.<clinit>():void");
    }

    public static NetworkEventDispatcher getInstance() {
        return instance;
    }

    protected NetworkEventDispatcher() {
        this.listeners = new CopyOnWriteArrayList();
    }

    public void addListener(NetworkEventListener toAdd) {
        if (toAdd == null) {
            throw new NullPointerException("toAdd == null");
        }
        this.listeners.add(toAdd);
    }

    public void removeListener(NetworkEventListener toRemove) {
        for (NetworkEventListener listener : this.listeners) {
            if (listener == toRemove) {
                this.listeners.remove(listener);
                return;
            }
        }
    }

    public void onNetworkConfigurationChanged() {
        for (NetworkEventListener listener : this.listeners) {
            try {
                listener.onNetworkConfigurationChanged();
            } catch (RuntimeException e) {
                System.logI("Exception thrown during network event propagation", e);
            }
        }
    }
}
