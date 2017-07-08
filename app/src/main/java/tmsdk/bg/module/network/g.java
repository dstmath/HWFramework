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
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.jh;
import tmsdkobf.jj;
import tmsdkobf.jq;
import tmsdkobf.ms;

/* compiled from: Unknown */
final class g extends BaseManagerB {
    private Context mContext;
    private IUpdateObserver vg;
    private f xX;
    private String xY;
    private boolean yg;
    private jh yv;
    private long yw;
    private int yx;
    private final Map<String, d> yy;
    private a yz;

    /* compiled from: Unknown */
    private final class a extends jh {
        private Looper mLooper;
        private HandlerThread or;
        final /* synthetic */ g yA;
        private Handler yB;
        private Runnable yC;
        private b yD;

        /* compiled from: Unknown */
        private final class a implements Runnable {
            final /* synthetic */ a yE;

            private a(a aVar) {
                this.yE = aVar;
            }

            private long o(long j) {
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
                for (d dO : this.yE.yA.yy.values()) {
                    dO.dO();
                }
                if (this.yE.yA.yg) {
                    this.yE.yB.removeCallbacks(this.yE.yC);
                    this.yE.yB.postDelayed(this.yE.yC, o(this.yE.yA.yw));
                }
            }
        }

        private a(g gVar) {
            this.yA = gVar;
            this.yC = new a();
            this.yD = new b(null);
        }

        public void e(Intent intent) {
            super.e(intent);
            this.yB.removeCallbacks(this.yC);
            this.yB.post(this.yC);
        }

        public IBinder onBind() {
            return null;
        }

        public void onCreate(Context context) {
            super.onCreate(context);
            this.or = jq.ct().bF(getClass().getName());
            this.or.start();
            this.mLooper = this.or.getLooper();
            this.yB = new Handler(this.mLooper);
            this.yD.h(this.yA.mContext);
        }

        public void onDestory() {
            super.onDestory();
            this.yB.removeCallbacks(this.yC);
            this.mLooper.quit();
            this.yD.i(this.yA.mContext);
        }
    }

    /* compiled from: Unknown */
    private final class b extends jj {
        final /* synthetic */ g yA;
        private boolean yF;

        private b(g gVar) {
            this.yA = gVar;
        }

        public void doOnRecv(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.TIME_SET")) {
                for (d dP : this.yA.yy.values()) {
                    dP.dP();
                }
            }
        }

        public void h(Context context) {
            if (!this.yF) {
                context.registerReceiver(this, new IntentFilter("android.intent.action.TIME_SET"));
                this.yF = true;
            }
        }

        public void i(Context context) {
            if (this.yF) {
                context.unregisterReceiver(this);
                this.yF = false;
            }
        }
    }

    g() {
        this.xY = null;
        this.yv = new a();
        this.yy = new ConcurrentHashMap();
        this.vg = new IUpdateObserver() {
            final /* synthetic */ g yA;

            {
                this.yA = r1;
            }

            public void onChanged(UpdateInfo updateInfo) {
                this.yA.dV();
            }
        };
        this.yz = new a();
    }

    public static String dU() {
        return new File("/proc/self/net/dev").exists() ? "/proc/self/net/dev" : new File("/proc/net/dev").exists() ? "/proc/net/dev" : "error";
    }

    private int dV() {
        try {
            if (ms.a(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, null) == null) {
                return -1;
            }
            this.xX.dR();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addDefaultMobileMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        if (str == null || iNetworkInfoDao == null || this.yy.containsKey(str)) {
            return false;
        }
        d cVar = new c(iNetworkInfoDao, this.xX);
        cVar.z(this.yg);
        this.yy.put(str, cVar);
        return true;
    }

    public boolean addDefaultWifiMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        if (str == null || iNetworkInfoDao == null || this.yy.containsKey(str)) {
            return false;
        }
        d eVar = new e(iNetworkInfoDao, this.xX);
        eVar.z(this.yg);
        this.yy.put(str, eVar);
        return true;
    }

    public boolean addMonitor(String str, NetDataEntityFactory netDataEntityFactory, INetworkInfoDao iNetworkInfoDao) {
        boolean z = false;
        if (str == null || netDataEntityFactory == null || iNetworkInfoDao == null) {
            return false;
        }
        d dVar = new d(netDataEntityFactory, iNetworkInfoDao);
        dVar.z(this.yg);
        if (this.yy.put(str, dVar) != null) {
            z = true;
        }
        return z;
    }

    public void clearTrafficInfo(String[] strArr) {
        this.yz.clearTrafficInfo(strArr);
    }

    protected void finalize() throws Throwable {
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(32);
        super.finalize();
    }

    public INetworkMonitor findMonitor(String str) {
        return !this.yy.containsKey(str) ? null : (INetworkMonitor) this.yy.get(str);
    }

    public long getInterval() {
        return this.yw;
    }

    public int getIntervalType() {
        return this.yx;
    }

    public long getMobileRxBytes(String str) {
        return this.yz.getMobileRxBytes(str);
    }

    public long getMobileTxBytes(String str) {
        return this.yz.getMobileTxBytes(str);
    }

    public int getSingletonType() {
        return 1;
    }

    public TrafficEntity getTrafficEntity(String str) {
        return this.yz.getTrafficEntity(str);
    }

    public long getWIFIRxBytes(String str) {
        return this.yz.getWIFIRxBytes(str);
    }

    public long getWIFITxBytes(String str) {
        return this.yz.getWIFITxBytes(str);
    }

    public boolean isEnable() {
        return this.yg;
    }

    public boolean isSupportTrafficState() {
        return this.yz.isSupportTrafficState();
    }

    public void networkConnectivityChangeNotify() {
        for (d networkConnectivityChangeNotify : this.yy.values()) {
            networkConnectivityChangeNotify.networkConnectivityChangeNotify();
        }
    }

    public synchronized void notifyConfigChange() {
        if (this.yg) {
            for (d notifyConfigChange : this.yy.values()) {
                notifyConfigChange.notifyConfigChange();
            }
            if (this.yv != null) {
                TMSService.startService(this.yv);
            }
        }
    }

    public void onCreate(Context context) {
        this.mContext = context;
        ms.a(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, null);
        this.xY = dU();
        this.xX = new f(this.xY);
        this.xX.dR();
        setIntervalType(0);
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).addObserver(32, this.vg);
    }

    public ArrayList<TrafficEntity> refreshTrafficInfo(String[] strArr, boolean z) {
        return this.yz.refreshTrafficInfo(strArr, z);
    }

    public void refreshTrafficInfo(ArrayList<TrafficEntity> arrayList) {
        this.yz.refreshTrafficInfo((ArrayList) arrayList);
    }

    public void refreshTrafficInfo(TrafficEntity trafficEntity) {
        this.yz.refreshTrafficInfo(trafficEntity);
    }

    public boolean removeMonitor(String str) {
        boolean z = false;
        if (!this.yy.containsKey(str)) {
            return false;
        }
        if (this.yy.remove(str) != null) {
            z = true;
        }
        return z;
    }

    public void setEnable(boolean z) {
        if (z != this.yg) {
            this.yg = z;
            for (d z2 : this.yy.values()) {
                z2.z(this.yg);
            }
            if (this.yg) {
                TMSService.startService(this.yv);
            } else {
                TMSService.stopService(this.yv);
            }
        }
    }

    public void setInterval(long j) {
        this.yw = j;
        this.yx = 3;
    }

    public void setIntervalType(int i) {
        this.yx = i;
        switch (i) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                this.yw = 300000;
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                this.yw = 30000;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                this.yw = 3000;
            default:
        }
    }
}
