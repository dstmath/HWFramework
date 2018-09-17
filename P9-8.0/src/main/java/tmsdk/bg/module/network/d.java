package tmsdk.bg.module.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.module.network.NetworkInfoEntity;
import tmsdk.common.utils.f;
import tmsdkobf.im;
import tmsdkobf.lr;

class d implements INetworkChangeCallBack, INetworkMonitor {
    private final String TAG = "DefaultNetworkMonitor";
    private Date mStartDate;
    private long mTotalForMonth = 0;
    private long mUsedForDay = 0;
    private long mUsedForMonth = 0;
    private long mUsedReceiveForDay = 0;
    private long mUsedReceiveForMonth = 0;
    private long mUsedTranslateForDay = 0;
    private long mUsedTranslateForMonth = 0;
    private NetDataEntityFactory vd;
    private INetworkInfoDao ve;
    private NetDataEntity vf;
    private int vg = -1;
    private int vh = 0;
    private byte[] vi = new byte[0];
    private HashSet<INetworkChangeCallBack> vj = new HashSet();
    private boolean vk = false;
    private boolean vl = true;

    public d(NetDataEntityFactory netDataEntityFactory, INetworkInfoDao iNetworkInfoDao) {
        this.vd = netDataEntityFactory;
        this.ve = iNetworkInfoDao;
        NetworkInfoEntity todayNetworkInfoEntity = this.ve.getTodayNetworkInfoEntity();
        this.mStartDate = todayNetworkInfoEntity.mStartDate;
        this.mTotalForMonth = todayNetworkInfoEntity.mTotalForMonth;
        this.mUsedForDay = todayNetworkInfoEntity.mUsedForDay;
        this.mUsedTranslateForDay = todayNetworkInfoEntity.mUsedTranslateForDay;
        this.mUsedReceiveForDay = todayNetworkInfoEntity.mUsedReceiveForDay;
        this.mUsedForMonth = todayNetworkInfoEntity.mUsedForMonth;
        this.mUsedTranslateForMonth = todayNetworkInfoEntity.mUsedTranslateForMonth;
        this.mUsedReceiveForMonth = todayNetworkInfoEntity.mUsedReceiveForMonth;
        this.vg = this.ve.getClosingDayForMonth();
    }

    private synchronized NetworkInfoEntity cS() {
        NetworkInfoEntity networkInfoEntity;
        networkInfoEntity = new NetworkInfoEntity();
        networkInfoEntity.mTotalForMonth = this.mTotalForMonth;
        networkInfoEntity.mUsedForMonth = this.mUsedForMonth;
        networkInfoEntity.mUsedTranslateForMonth = this.mUsedTranslateForMonth;
        networkInfoEntity.mUsedReceiveForMonth = this.mUsedReceiveForMonth;
        networkInfoEntity.mRetialForMonth = this.mTotalForMonth - this.mUsedForMonth;
        networkInfoEntity.mUsedForDay = this.mUsedForDay;
        networkInfoEntity.mUsedTranslateForDay = this.mUsedTranslateForDay;
        networkInfoEntity.mUsedReceiveForDay = this.mUsedReceiveForDay;
        networkInfoEntity.mStartDate = this.mStartDate;
        this.ve.setTodayNetworkInfoEntity(networkInfoEntity);
        return networkInfoEntity;
    }

    public int addCallback(INetworkChangeCallBack iNetworkChangeCallBack) {
        synchronized (this.vi) {
            if (iNetworkChangeCallBack != null) {
                this.vj.add(iNetworkChangeCallBack);
            }
        }
        return iNetworkChangeCallBack == null ? -1 : iNetworkChangeCallBack.hashCode();
    }

    protected synchronized void cT() {
        NetworkInfoEntity cS;
        NetDataEntity netDataEntity = this.vd.getNetDataEntity();
        if ((netDataEntity.mReceiver <= 0 ? 1 : null) == null) {
            long j = netDataEntity.mReceiver + netDataEntity.mTranslate;
            long j2 = this.vf.mReceiver + this.vf.mTranslate;
            if ((this.vf.mReceiver > 0 ? 1 : null) == null) {
                this.vf.mReceiver = 0;
            }
            if ((this.vf.mTranslate > 0 ? 1 : null) == null) {
                this.vf.mTranslate = 0;
            }
            if ((j2 > 0 ? 1 : null) == null) {
                j2 = j;
            }
            long j3 = netDataEntity.mTranslate - this.vf.mTranslate;
            if ((j3 >= 0 ? 1 : null) == null) {
                j3 = netDataEntity.mTranslate;
            }
            long j4 = netDataEntity.mReceiver - this.vf.mReceiver;
            if ((j4 >= 0 ? 1 : null) == null) {
                j4 = netDataEntity.mReceiver;
            }
            long j5 = j - j2;
            if ((j5 >= 0 ? 1 : null) == null) {
                j5 = j;
            }
            notifyConfigChange();
            this.mUsedForDay += j5;
            this.mUsedTranslateForDay += j3;
            this.mUsedReceiveForDay += j4;
            this.mUsedForMonth += j5;
            this.mUsedTranslateForMonth += j3;
            this.mUsedReceiveForMonth += j4;
            this.ve.setUsedForMonth(this.mUsedForMonth);
            this.vf = netDataEntity;
            this.ve.setLastNetDataEntity(this.vf);
            cS = cS();
        } else {
            notifyConfigChange();
            this.vf = netDataEntity;
            this.ve.setLastNetDataEntity(this.vf);
            cS = cS();
        }
        onNormalChanged(cS);
    }

    protected synchronized void cU() {
        NetworkInfoEntity networkInfoEntity = new NetworkInfoEntity();
        networkInfoEntity.mStartDate = this.mStartDate;
        networkInfoEntity.mUsedForDay = this.mUsedForDay;
        networkInfoEntity.mUsedTranslateForDay = this.mUsedTranslateForDay;
        networkInfoEntity.mUsedReceiveForDay = this.mUsedReceiveForDay;
        this.ve.insert(networkInfoEntity);
        this.mUsedForDay = 0;
        this.mUsedTranslateForDay = 0;
        this.mUsedReceiveForDay = 0;
        this.mStartDate = new Date();
        this.ve.resetToDayNetworkInfoEntity();
        onDayChanged();
    }

    protected synchronized void cV() {
        this.mUsedForMonth = 0;
        this.mUsedTranslateForMonth = 0;
        this.mUsedReceiveForMonth = 0;
        this.ve.resetMonthNetworkinfoEntity();
        onClosingDateReached();
    }

    /* JADX WARNING: Missing block: B:5:0x0006, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void cW() {
        if (this.vk) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(this.mStartDate);
            GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
            GregorianCalendar a = lr.a(gregorianCalendar2, this.vg);
            GregorianCalendar a2 = lr.a(gregorianCalendar, this.vg);
            if (this.vh <= 0) {
                if (a.get(2) != a2.get(2)) {
                    cV();
                }
                if (gregorianCalendar.get(5) != gregorianCalendar2.get(5)) {
                    f.f("DefaultNetworkMonitor", "processForDayChanged");
                    cU();
                }
                cT();
            }
        }
    }

    public synchronized void cX() {
        this.vh++;
        f.f("DefaultNetworkMonitor", "onSystemTimeChanged ");
        NetworkInfoEntity systemTimeChange = this.ve.getSystemTimeChange(this.mStartDate);
        if (systemTimeChange != null) {
            this.mTotalForMonth = systemTimeChange.mTotalForMonth;
            this.mUsedForMonth = systemTimeChange.mUsedForMonth;
            this.mUsedTranslateForMonth = systemTimeChange.mUsedTranslateForMonth;
            this.mUsedReceiveForMonth = systemTimeChange.mUsedReceiveForMonth;
            this.mUsedForDay = systemTimeChange.mUsedForDay;
            this.mUsedTranslateForDay = systemTimeChange.mUsedTranslateForDay;
            this.mUsedReceiveForDay = systemTimeChange.mUsedReceiveForDay;
        }
        this.mStartDate = new Date();
        cS();
        this.vh--;
        im.bJ().addTask(new Runnable() {
            public void run() {
                d.this.cW();
            }
        }, "ACTION_TIME_CHANGED");
    }

    public synchronized void clearAllLogs() {
        this.mUsedForDay = 0;
        this.mUsedTranslateForDay = 0;
        this.mUsedReceiveForDay = 0;
        this.mUsedForMonth = 0;
        this.mUsedTranslateForMonth = 0;
        this.mUsedReceiveForMonth = 0;
        this.mStartDate = new Date();
        this.vf = this.vd.getNetDataEntity();
        this.ve.setLastNetDataEntity(this.vf);
        this.ve.resetToDayNetworkInfoEntity();
        this.ve.clearAll();
    }

    public ArrayList<NetworkInfoEntity> getAllLogs() {
        List all = this.ve.getAll();
        if (all != null && all.size() > 0) {
            Collections.sort(all);
        }
        return all;
    }

    public boolean getRefreshState() {
        return this.vl;
    }

    /* JADX WARNING: Missing block: B:20:0x0037, code:
            if (r0 == null) goto L_0x0019;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void l(boolean z) {
        Object obj = null;
        synchronized (this) {
            this.vk = z;
            if (this.vk) {
                this.vf = this.ve.getLastNetDataEntity();
                if (this.vf != null) {
                    if (this.vf.mReceiver >= 0) {
                        obj = 1;
                    }
                }
                this.vf = this.vd.getNetDataEntity();
                this.ve.setLastNetDataEntity(this.vf);
            } else {
                cS();
            }
        }
    }

    public void networkConnectivityChangeNotify() {
        this.vd.networkConnectivityChangeNotify();
    }

    public synchronized void notifyConfigChange() {
        if (this.vk) {
            this.vg = this.ve.getClosingDayForMonth();
            this.mTotalForMonth = this.ve.getTotalForMonth();
            this.mUsedForMonth = this.ve.getUsedForMonth();
            NetworkInfoEntity todayNetworkInfoEntity = this.ve.getTodayNetworkInfoEntity();
            this.mUsedTranslateForMonth = todayNetworkInfoEntity.mUsedTranslateForMonth;
            this.mUsedReceiveForMonth = todayNetworkInfoEntity.mUsedReceiveForMonth;
            this.mUsedForDay = todayNetworkInfoEntity.mUsedForDay;
            this.mUsedTranslateForDay = todayNetworkInfoEntity.mUsedTranslateForDay;
            this.mUsedReceiveForDay = todayNetworkInfoEntity.mUsedReceiveForDay;
        }
    }

    public void onClosingDateReached() {
        if (this.vl) {
            HashSet hashSet;
            synchronized (this.vi) {
                hashSet = (HashSet) this.vj.clone();
            }
            if (hashSet != null) {
                Iterator it = hashSet.iterator();
                while (it.hasNext()) {
                    ((INetworkChangeCallBack) it.next()).onClosingDateReached();
                }
            }
        }
    }

    public void onDayChanged() {
        if (this.vl) {
            HashSet hashSet;
            synchronized (this.vi) {
                hashSet = (HashSet) this.vj.clone();
            }
            if (hashSet != null) {
                Iterator it = hashSet.iterator();
                while (it.hasNext()) {
                    ((INetworkChangeCallBack) it.next()).onDayChanged();
                }
            }
        }
    }

    public void onNormalChanged(NetworkInfoEntity networkInfoEntity) {
        if (this.vl) {
            HashSet hashSet;
            synchronized (this.vi) {
                hashSet = (HashSet) this.vj.clone();
            }
            if (hashSet != null) {
                Iterator it = hashSet.iterator();
                while (it.hasNext()) {
                    ((INetworkChangeCallBack) it.next()).onNormalChanged(networkInfoEntity);
                }
            }
        }
    }

    public boolean removeCallback(int i) {
        boolean z = false;
        synchronized (this.vi) {
            Iterator it = this.vj.iterator();
            while (it.hasNext()) {
                if (((INetworkChangeCallBack) it.next()).hashCode() == i) {
                    z = true;
                    it.remove();
                    break;
                }
            }
        }
        return z;
    }

    public boolean removeCallback(INetworkChangeCallBack iNetworkChangeCallBack) {
        if (iNetworkChangeCallBack == null) {
            return false;
        }
        boolean remove;
        synchronized (this.vi) {
            remove = this.vj.remove(iNetworkChangeCallBack);
        }
        return remove;
    }

    public void setRefreshState(boolean z) {
        this.vl = z;
    }
}
