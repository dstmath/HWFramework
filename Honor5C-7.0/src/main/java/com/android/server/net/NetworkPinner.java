package com.android.server.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;

public class NetworkPinner extends NetworkCallback {
    private static final String TAG = null;
    @GuardedBy("sLock")
    private static ConnectivityManager sCM;
    @GuardedBy("sLock")
    private static Callback sCallback;
    protected static final Object sLock = null;
    @GuardedBy("sLock")
    protected static Network sNetwork;

    private static class Callback extends NetworkCallback {
        private Callback() {
        }

        public void onAvailable(Network network) {
            synchronized (NetworkPinner.sLock) {
                if (this != NetworkPinner.sCallback) {
                    return;
                }
                if (NetworkPinner.sCM.getBoundNetworkForProcess() == null && NetworkPinner.sNetwork == null) {
                    NetworkPinner.sCM.bindProcessToNetwork(network);
                    NetworkPinner.sNetwork = network;
                    Log.d(NetworkPinner.TAG, "Wifi alternate reality enabled on network " + network);
                }
                NetworkPinner.sLock.notify();
            }
        }

        public void onLost(Network network) {
            synchronized (NetworkPinner.sLock) {
                if (this != NetworkPinner.sCallback) {
                    return;
                }
                if (network.equals(NetworkPinner.sNetwork) && network.equals(NetworkPinner.sCM.getBoundNetworkForProcess())) {
                    NetworkPinner.unpin();
                    Log.d(NetworkPinner.TAG, "Wifi alternate reality disabled on network " + network);
                }
                NetworkPinner.sLock.notify();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.net.NetworkPinner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.net.NetworkPinner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkPinner.<clinit>():void");
    }

    private static void maybeInitConnectivityManager(Context context) {
        if (sCM == null) {
            sCM = (ConnectivityManager) context.getSystemService("connectivity");
            if (sCM == null) {
                throw new IllegalStateException("Bad luck, ConnectivityService not started.");
            }
        }
    }

    public static void pin(Context context, NetworkRequest request) {
        synchronized (sLock) {
            if (sCallback == null) {
                maybeInitConnectivityManager(context);
                sCallback = new Callback();
                try {
                    sCM.registerNetworkCallback(request, sCallback);
                } catch (SecurityException e) {
                    Log.d(TAG, "Failed to register network callback", e);
                    sCallback = null;
                }
            }
        }
    }

    public static void unpin() {
        synchronized (sLock) {
            if (sCallback != null) {
                try {
                    sCM.bindProcessToNetwork(null);
                    sCM.unregisterNetworkCallback(sCallback);
                } catch (SecurityException e) {
                    Log.d(TAG, "Failed to unregister network callback", e);
                }
                sCallback = null;
                sNetwork = null;
            }
        }
    }
}
