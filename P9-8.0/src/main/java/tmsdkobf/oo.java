package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PowerManager;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;
import tmsdk.common.utils.q;

public class oo extends if {
    private static oo IA;
    private static Object lock = new Object();
    private State Iy = State.DISCONNECTED;
    private LinkedList<a> Iz = new LinkedList();
    private boolean xF;

    public interface a {
        void dC();

        void dD();
    }

    private oo() {
    }

    public static oo A(Context context) {
        if (IA == null) {
            synchronized (lock) {
                if (IA == null) {
                    if (context != null) {
                        IA = new oo();
                        IA.init(context);
                    } else {
                        return null;
                    }
                }
            }
        }
        return IA;
    }

    private synchronized void B(Context context) {
        if (!this.xF) {
            try {
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    this.Iy = State.DISCONNECTED;
                } else {
                    this.Iy = activeNetworkInfo.getState();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.setPriority(Integer.MAX_VALUE);
            try {
                context.registerReceiver(this, intentFilter);
                this.xF = true;
            } catch (Throwable th) {
                f.e("NetworkBroadcastReceiver", th);
            }
        }
        return;
    }

    private void init(Context context) {
        B(context);
    }

    public static boolean isScreenOn() {
        String cI = q.cI(im.bQ());
        if (!"888748".equals(cI) && !"799005".equals(cI)) {
            return true;
        }
        boolean z = true;
        try {
            PowerManager powerManager = (PowerManager) TMSDKContext.getApplicaionContext().getSystemService("power");
            if (VERSION.SDK_INT < 20) {
                z = ((Boolean) PowerManager.class.getMethod("isScreenOn", new Class[0]).invoke(powerManager, new Object[0])).booleanValue();
            } else {
                z = ((Boolean) PowerManager.class.getMethod("isInteractive", new Class[0]).invoke(powerManager, new Object[0])).booleanValue();
            }
        } catch (Exception e) {
        }
        return z;
    }

    public void a(a aVar) {
        synchronized (this.Iz) {
            this.Iz.add(aVar);
        }
    }

    public void b(a aVar) {
        synchronized (this.Iz) {
            this.Iz.remove(aVar);
        }
    }

    public void doOnRecv(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        f.f("NetworkBroadcastReceiver", action);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            State state = ((NetworkInfo) extras.getParcelable("networkInfo")).getState();
            if (state != State.CONNECTED) {
                if (state != State.DISCONNECTED) {
                    return;
                }
                if (this.Iy.compareTo(State.CONNECTED) == 0) {
                    im.bJ().a(new Runnable() {
                        public void run() {
                            LinkedList linkedList;
                            synchronized (oo.this.Iz) {
                                linkedList = (LinkedList) oo.this.Iz.clone();
                            }
                            if (linkedList != null) {
                                Iterator it = linkedList.iterator();
                                while (it.hasNext()) {
                                    ((a) it.next()).dD();
                                }
                            }
                        }
                    }, "monitor_toDisconnected");
                }
            } else if (this.Iy.compareTo(State.DISCONNECTED) == 0 && isScreenOn()) {
                im.bJ().a(new Runnable() {
                    public void run() {
                        LinkedList linkedList;
                        synchronized (oo.this.Iz) {
                            linkedList = (LinkedList) oo.this.Iz.clone();
                        }
                        f.f("NetworkBroadcastReceiver", "copy != null ? " + (linkedList != null));
                        if (linkedList != null) {
                            f.f("NetworkBroadcastReceiver", "copy.size() : " + linkedList.size());
                            Iterator it = linkedList.iterator();
                            while (it.hasNext()) {
                                ((a) it.next()).dC();
                            }
                        }
                    }
                }, "monitor_toConnected");
            }
            this.Iy = state;
        }
    }
}
