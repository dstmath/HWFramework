package tmsdk.bg.module.network;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.TMSService;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.network.TrafficEntity;
import tmsdk.common.module.update.IUpdateObserver;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import tmsdkobf.id;
import tmsdkobf.if;
import tmsdkobf.im;
import tmsdkobf.lu;

final class g extends BaseManagerB {
    private Context mContext;
    private long vA;
    private int vB;
    private final Map<String, d> vC = new ConcurrentHashMap();
    private IUpdateObserver vD = new IUpdateObserver() {
        public void onChanged(UpdateInfo updateInfo) {
            g.this.dd();
        }
    };
    private a vE = new a();
    private f vb;
    private String vc = null;
    private boolean vk;
    private id vz = new a(this, null);

    private final class a extends id {
        private Looper mLooper;
        private HandlerThread vG;
        private Handler vH;
        private Runnable vI;
        private b vJ;

        private final class a implements Runnable {
            private a() {
            }

            /* synthetic */ a(a aVar, AnonymousClass1 anonymousClass1) {
                this();
            }

            private long j(long j) {
                int i = 1;
                Date date = new Date();
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.add(5, 1);
                gregorianCalendar.set(11, 0);
                gregorianCalendar.set(12, 0);
                gregorianCalendar.set(13, 1);
                gregorianCalendar.set(14, 0);
                long timeInMillis = gregorianCalendar.getTimeInMillis() - date.getTime();
                if (timeInMillis > j) {
                    i = 0;
                }
                return i == 0 ? j : timeInMillis;
            }

            public void run() {
                for (d cW : g.this.vC.values()) {
                    cW.cW();
                }
                if (g.this.vk) {
                    a.this.vH.removeCallbacks(a.this.vI);
                    a.this.vH.postDelayed(a.this.vI, j(g.this.vA));
                }
            }
        }

        private a() {
            this.vI = new a(this, null);
            this.vJ = new b(g.this, null);
        }

        /* synthetic */ a(g gVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void d(Intent intent) {
            super.d(intent);
            this.vH.removeCallbacks(this.vI);
            this.vH.post(this.vI);
        }

        public IBinder onBind() {
            return null;
        }

        public void onCreate(Context context) {
            super.onCreate(context);
            this.vG = im.bJ().newFreeHandlerThread(getClass().getName());
            this.vG.start();
            this.mLooper = this.vG.getLooper();
            this.vH = new Handler(this.mLooper);
            this.vJ.k(g.this.mContext);
        }

        public void onDestory() {
            super.onDestory();
            this.vH.removeCallbacks(this.vI);
            this.mLooper.quit();
            this.vJ.l(g.this.mContext);
        }
    }

    private final class b extends if {
        private boolean vL;

        private b() {
        }

        /* synthetic */ b(g gVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void doOnRecv(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.TIME_SET")) {
                for (d cX : g.this.vC.values()) {
                    cX.cX();
                }
            }
        }

        public void k(Context context) {
            if (!this.vL) {
                context.registerReceiver(this, new IntentFilter("android.intent.action.TIME_SET"));
                this.vL = true;
            }
        }

        public void l(Context context) {
            if (this.vL) {
                context.unregisterReceiver(this);
                this.vL = false;
            }
        }
    }

    g() {
    }

    public static String dc() {
        if (new File("/proc/self/net/dev").exists()) {
            return "/proc/self/net/dev";
        }
        return new File("/proc/net/dev").exists() ? "/proc/net/dev" : "error";
    }

    private int dd() {
        try {
            if (lu.b(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, null) == null) {
                return -1;
            }
            this.vb.cZ();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addDefaultMobileMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        if (str == null || iNetworkInfoDao == null || this.vC.containsKey(str)) {
            return false;
        }
        d cVar = new c(iNetworkInfoDao, this.vb);
        cVar.l(this.vk);
        this.vC.put(str, cVar);
        return true;
    }

    public boolean addDefaultWifiMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        if (str == null || iNetworkInfoDao == null || this.vC.containsKey(str)) {
            return false;
        }
        d eVar = new e(iNetworkInfoDao, this.vb);
        eVar.l(this.vk);
        this.vC.put(str, eVar);
        return true;
    }

    public boolean addMonitor(String str, NetDataEntityFactory netDataEntityFactory, INetworkInfoDao iNetworkInfoDao) {
        boolean z = false;
        if (str == null || netDataEntityFactory == null || iNetworkInfoDao == null) {
            return false;
        }
        d dVar = new d(netDataEntityFactory, iNetworkInfoDao);
        dVar.l(this.vk);
        if (this.vC.put(str, dVar) != null) {
            z = true;
        }
        return z;
    }

    public void clearTrafficInfo(String[] strArr) {
        this.vE.clearTrafficInfo(strArr);
    }

    protected void finalize() throws Throwable {
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(32);
        super.finalize();
    }

    public INetworkMonitor findMonitor(String str) {
        return !this.vC.containsKey(str) ? null : (INetworkMonitor) this.vC.get(str);
    }

    public long getInterval() {
        return this.vA;
    }

    public int getIntervalType() {
        return this.vB;
    }

    public long getMobileRxBytes(String str) {
        return this.vE.getMobileRxBytes(str);
    }

    public long getMobileTxBytes(String str) {
        return this.vE.getMobileTxBytes(str);
    }

    public int getSingletonType() {
        return 1;
    }

    public TrafficEntity getTrafficEntity(String str) {
        return this.vE.getTrafficEntity(str);
    }

    public long getWIFIRxBytes(String str) {
        return this.vE.getWIFIRxBytes(str);
    }

    public long getWIFITxBytes(String str) {
        return this.vE.getWIFITxBytes(str);
    }

    public boolean isEnable() {
        return this.vk;
    }

    public boolean isSupportTrafficState() {
        return this.vE.isSupportTrafficState();
    }

    public void networkConnectivityChangeNotify() {
        for (d networkConnectivityChangeNotify : this.vC.values()) {
            networkConnectivityChangeNotify.networkConnectivityChangeNotify();
        }
    }

    public synchronized void notifyConfigChange() {
        if (this.vk) {
            for (d notifyConfigChange : this.vC.values()) {
                notifyConfigChange.notifyConfigChange();
            }
            if (this.vz != null) {
                TMSService.startService(this.vz);
            }
        }
    }

    public void onCreate(Context context) {
        this.mContext = context;
        lu.b(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, null);
        this.vc = dc();
        this.vb = new f(this.vc);
        this.vb.cZ();
        setIntervalType(0);
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).addObserver(32, this.vD);
    }

    public ArrayList<TrafficEntity> refreshTrafficInfo(String[] strArr, boolean z) {
        return this.vE.refreshTrafficInfo(strArr, z);
    }

    public void refreshTrafficInfo(ArrayList<TrafficEntity> arrayList) {
        this.vE.refreshTrafficInfo((ArrayList) arrayList);
    }

    public void refreshTrafficInfo(TrafficEntity trafficEntity) {
        this.vE.refreshTrafficInfo(trafficEntity);
    }

    public boolean removeMonitor(String str) {
        boolean z = false;
        if (!this.vC.containsKey(str)) {
            return false;
        }
        if (this.vC.remove(str) != null) {
            z = true;
        }
        return z;
    }

    public void setEnable(boolean z) {
        if (z != this.vk) {
            this.vk = z;
            for (d l : this.vC.values()) {
                l.l(this.vk);
            }
            if (this.vk) {
                TMSService.startService(this.vz);
            } else {
                TMSService.stopService(this.vz);
            }
        }
    }

    public void setInterval(long j) {
        this.vA = j;
        this.vB = 3;
    }

    public void setIntervalType(int i) {
        this.vB = i;
        switch (i) {
            case 0:
                this.vA = 300000;
                return;
            case 1:
                this.vA = 30000;
                return;
            case 2:
                this.vA = 3000;
                return;
            default:
                return;
        }
    }
}
