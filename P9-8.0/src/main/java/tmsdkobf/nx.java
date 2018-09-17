package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.f;

public class nx extends if {
    private static Object FG = new Object();
    private static nx FI = null;
    private boolean FA = false;
    private State FB = State.UNKNOWN;
    private String FC = null;
    private String FD = null;
    private LinkedList<a> FE = new LinkedList();
    private LinkedList<b> FF = new LinkedList();
    private Object FH = new Object();
    private long Fz = 0;
    private Handler mHandler = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    nx.this.fF();
                    return;
                default:
                    return;
            }
        }
    };

    public interface a {
        void onConnected();

        void onDisconnected();
    }

    public interface b {
        void gb();
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0102 A:{SYNTHETIC, Splitter: B:28:0x0102} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0046  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void e(Intent intent) {
        Object obj = 1;
        synchronized (this.FH) {
            Bundle extras;
            if ((this.Fz <= 0 ? 1 : null) == null) {
                if (System.currentTimeMillis() - this.Fz <= 2000) {
                    obj = null;
                }
                if (obj == null) {
                    mb.d("SharkNetworkReceiver", "[conn_monitor]doOnRecv(), ignore for just register: " + (System.currentTimeMillis() - this.Fz));
                    extras = intent.getExtras();
                    if (extras == null) {
                        NetworkInfo networkInfo = (NetworkInfo) extras.getParcelable("networkInfo");
                        if (networkInfo != null) {
                            State state = networkInfo.getState();
                            String typeName = networkInfo.getTypeName();
                            String subtypeName = networkInfo.getSubtypeName();
                            mb.n("SharkNetworkReceiver", "[conn_monitor]doOnRecv(), Sate: " + this.FB + " -> " + state);
                            mb.n("SharkNetworkReceiver", "[conn_monitor]doOnRecv(), type: " + this.FC + " -> " + typeName);
                            mb.n("SharkNetworkReceiver", "[conn_monitor]doOnRecv(), subType: " + this.FD + " -> " + subtypeName);
                            if (state != State.CONNECTED) {
                                if (state == State.DISCONNECTED && this.FB != State.DISCONNECTED) {
                                    gt();
                                }
                            } else if (this.FB != State.CONNECTED) {
                                gu();
                            }
                            this.FB = state;
                            this.FC = typeName;
                            this.FD = subtypeName;
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            np.fS().fT();
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 5000);
            extras = intent.getExtras();
            if (extras == null) {
            }
        }
    }

    private void eZ() {
        try {
            Context applicaionContext = TMSDKContext.getApplicaionContext();
            if (applicaionContext != null) {
                z(applicaionContext);
            }
        } catch (Exception e) {
            mb.o("SharkNetworkReceiver", "[conn_monitor]checkInit(), registerConnectivityIfNeed() failed: " + e);
        }
    }

    private void fF() {
        ((ki) fj.D(4)).a(new Runnable() {
            public void run() {
                LinkedList linkedList;
                mb.d("SharkNetworkReceiver", "[conn_monitor]handleNetworkChange()");
                synchronized (nx.this.FF) {
                    linkedList = (LinkedList) nx.this.FF.clone();
                }
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    b bVar = (b) it.next();
                    if (bVar != null) {
                        bVar.gb();
                    }
                }
            }
        }, "network_change");
    }

    public static nx gs() {
        if (FI == null) {
            synchronized (FG) {
                if (FI == null) {
                    FI = new nx();
                }
            }
        }
        FI.eZ();
        return FI;
    }

    private void gt() {
        ((ki) fj.D(4)).a(new Runnable() {
            public void run() {
                LinkedList linkedList;
                mb.d("SharkNetworkReceiver", "[conn_monitor]handleChange2DisConnected(), 有网络 -> 无网络");
                synchronized (nx.this.FE) {
                    linkedList = (LinkedList) nx.this.FE.clone();
                }
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    if (aVar != null) {
                        aVar.onDisconnected();
                    }
                }
            }
        }, "network_disconnected");
    }

    private void gu() {
        ((ki) fj.D(4)).a(new Runnable() {
            public void run() {
                LinkedList linkedList;
                boolean z = false;
                mb.d("SharkNetworkReceiver", "[conn_monitor]handleChange2Connected(), 无网络 -> 有网络");
                nj fE = nj.fE();
                StringBuilder append = new StringBuilder().append("[conn_monitor][ip_list]handleChange2Connected(), notify hiplist first: ");
                String str = "SharkNetworkReceiver";
                if (fE != null) {
                    z = true;
                }
                mb.d(str, append.append(z).toString());
                if (fE != null) {
                    fE.fF();
                }
                synchronized (nx.this.FE) {
                    linkedList = (LinkedList) nx.this.FE.clone();
                }
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    if (aVar != null) {
                        aVar.onConnected();
                    }
                }
            }
        }, "network_connected");
    }

    private synchronized void z(Context context) {
        NetworkInfo networkInfo;
        Object networkInfo2;
        if (!this.FA) {
            NetworkInfo activeNetworkInfo;
            try {
                activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    this.FB = State.DISCONNECTED;
                    mb.d("SharkNetworkReceiver", "[conn_monitor]registerConnectivRityIfNeed(), not got, set mLastState: " + this.FB);
                } else {
                    this.FB = activeNetworkInfo.getState();
                    this.FC = activeNetworkInfo.getTypeName();
                    this.FD = activeNetworkInfo.getSubtypeName();
                    mb.d("SharkNetworkReceiver", "[conn_monitor]registerConnectivRityIfNeed(), got mLastState: " + this.FB);
                }
                networkInfo2 = activeNetworkInfo;
            } catch (Exception e) {
                mb.o("SharkNetworkReceiver", "[conn_monitor]getActiveNetworkInfo() failed: " + e);
                networkInfo2 = e;
            }
            try {
                IntentFilter intentFilter = new IntentFilter();
                try {
                    intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                    intentFilter.setPriority(Integer.MAX_VALUE);
                    context.registerReceiver(this, intentFilter);
                    this.Fz = System.currentTimeMillis();
                    this.FA = true;
                    mb.n("SharkNetworkReceiver", "[conn_monitor]registerConnectivityIfNeed() succ");
                } catch (Throwable th) {
                    networkInfo2 = th;
                }
            } catch (Throwable th2) {
                activeNetworkInfo = networkInfo2;
                Throwable th3 = th2;
                mb.o("SharkNetworkReceiver", "[conn_monitor]registerConnectivityIfNeed() failed: " + networkInfo2);
            }
        }
    }

    public void a(a aVar) {
        if (aVar != null) {
            synchronized (this.FE) {
                if (!this.FE.contains(aVar)) {
                    this.FE.add(aVar);
                }
            }
        }
    }

    public void a(b bVar) {
        if (bVar != null) {
            synchronized (this.FF) {
                if (!this.FF.contains(bVar)) {
                    this.FF.add(bVar);
                }
            }
        }
    }

    public void doOnRecv(Context context, final Intent -l_4_R) {
        if (-l_4_R != null && -l_4_R.getAction() != null) {
            String action = -l_4_R.getAction();
            f.d("SharkNetworkReceiver", "[conn_monitor]doOnRecv(), action: " + action);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (im.bG() && oo.isScreenOn()) {
                            nx.this.e(-l_4_R);
                        }
                    }
                });
            }
        }
    }
}
