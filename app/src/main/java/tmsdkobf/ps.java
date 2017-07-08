package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ps extends jj {
    private static ps IB;
    private static Object lock;
    private boolean Ao;
    private LinkedList<a> IA;
    private State Iz;

    /* compiled from: Unknown */
    public interface a {
        void cn();

        void co();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ps.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ps.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ps.<clinit>():void");
    }

    private ps() {
        this.Iz = State.DISCONNECTED;
        this.IA = new LinkedList();
    }

    private void init(Context context) {
        u(context);
    }

    public static ps t(Context context) {
        if (IB == null) {
            synchronized (lock) {
                if (IB == null) {
                    if (context != null) {
                        IB = new ps();
                        IB.init(context);
                    } else {
                        return null;
                    }
                }
            }
        }
        return IB;
    }

    private synchronized void u(Context context) {
        if (!this.Ao) {
            try {
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    this.Iz = State.DISCONNECTED;
                } else {
                    d.d("NetworkBroadcastReceiver", "network type:" + activeNetworkInfo.getType());
                    this.Iz = activeNetworkInfo.getState();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.setPriority(UrlCheckType.UNKNOWN);
            try {
                context.registerReceiver(this, intentFilter);
                this.Ao = true;
            } catch (Throwable th) {
                d.c("NetworkBroadcastReceiver", th);
            }
        }
    }

    public void b(a aVar) {
        synchronized (this.IA) {
            this.IA.add(aVar);
        }
    }

    public void c(a aVar) {
        synchronized (this.IA) {
            this.IA.remove(aVar);
        }
    }

    public void doOnRecv(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        d.d("NetworkBroadcastReceiver", action);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            State state = ((NetworkInfo) extras.getParcelable("networkInfo")).getState();
            if (state != State.CONNECTED) {
                if (state != State.DISCONNECTED) {
                    return;
                }
                if (this.Iz.compareTo(State.CONNECTED) == 0) {
                    jq.ct().b(new Runnable() {
                        final /* synthetic */ ps IC;

                        {
                            this.IC = r1;
                        }

                        public void run() {
                            synchronized (this.IC.IA) {
                                LinkedList linkedList = (LinkedList) this.IC.IA.clone();
                            }
                            if (linkedList != null) {
                                Iterator it = linkedList.iterator();
                                while (it.hasNext()) {
                                    ((a) it.next()).cn();
                                }
                            }
                        }
                    }, "monitor_toDisconnected");
                }
            } else if (this.Iz.compareTo(State.DISCONNECTED) == 0) {
                jq.ct().b(new Runnable() {
                    final /* synthetic */ ps IC;

                    {
                        this.IC = r1;
                    }

                    public void run() {
                        boolean z = false;
                        synchronized (this.IC.IA) {
                            LinkedList linkedList = (LinkedList) this.IC.IA.clone();
                        }
                        String str = "NetworkBroadcastReceiver";
                        StringBuilder append = new StringBuilder().append("copy != null ? ");
                        if (linkedList != null) {
                            z = true;
                        }
                        d.d(str, append.append(z).toString());
                        if (linkedList != null) {
                            d.d("NetworkBroadcastReceiver", "copy.size() : " + linkedList.size());
                            Iterator it = linkedList.iterator();
                            while (it.hasNext()) {
                                ((a) it.next()).co();
                            }
                        }
                    }
                }, "monitor_toConnected");
            }
            this.Iz = state;
        }
    }
}
