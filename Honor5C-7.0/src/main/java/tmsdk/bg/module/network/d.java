package tmsdk.bg.module.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.module.network.NetworkInfoEntity;
import tmsdkobf.jq;
import tmsdkobf.mp;

/* compiled from: Unknown */
class d implements INetworkChangeCallBack, INetworkMonitor {
    private final String TAG;
    private Date mStartDate;
    private long mTotalForMonth;
    private long mUsedForDay;
    private long mUsedForMonth;
    private long mUsedReceiveForDay;
    private long mUsedReceiveForMonth;
    private long mUsedTranslateForDay;
    private long mUsedTranslateForMonth;
    private NetDataEntityFactory xZ;
    private INetworkInfoDao ya;
    private NetDataEntity yb;
    private int yc;
    private int yd;
    private byte[] ye;
    private HashSet<INetworkChangeCallBack> yf;
    private boolean yg;
    private boolean yh;

    public d(NetDataEntityFactory netDataEntityFactory, INetworkInfoDao iNetworkInfoDao) {
        this.TAG = "DefaultNetworkMonitor";
        this.mTotalForMonth = 0;
        this.mUsedForDay = 0;
        this.mUsedTranslateForDay = 0;
        this.mUsedReceiveForDay = 0;
        this.mUsedForMonth = 0;
        this.mUsedTranslateForMonth = 0;
        this.mUsedReceiveForMonth = 0;
        this.yc = -1;
        this.yd = 0;
        this.ye = new byte[0];
        this.yf = new HashSet();
        this.yg = false;
        this.yh = true;
        this.xZ = netDataEntityFactory;
        this.ya = iNetworkInfoDao;
        NetworkInfoEntity todayNetworkInfoEntity = this.ya.getTodayNetworkInfoEntity();
        this.mStartDate = todayNetworkInfoEntity.mStartDate;
        this.mTotalForMonth = todayNetworkInfoEntity.mTotalForMonth;
        this.mUsedForDay = todayNetworkInfoEntity.mUsedForDay;
        this.mUsedTranslateForDay = todayNetworkInfoEntity.mUsedTranslateForDay;
        this.mUsedReceiveForDay = todayNetworkInfoEntity.mUsedReceiveForDay;
        this.mUsedForMonth = todayNetworkInfoEntity.mUsedForMonth;
        this.mUsedTranslateForMonth = todayNetworkInfoEntity.mUsedTranslateForMonth;
        this.mUsedReceiveForMonth = todayNetworkInfoEntity.mUsedReceiveForMonth;
        this.yc = this.ya.getClosingDayForMonth();
    }

    private synchronized NetworkInfoEntity dK() {
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
        this.ya.setTodayNetworkInfoEntity(networkInfoEntity);
        return networkInfoEntity;
    }

    public int addCallback(INetworkChangeCallBack iNetworkChangeCallBack) {
        synchronized (this.ye) {
            if (iNetworkChangeCallBack != null) {
                this.yf.add(iNetworkChangeCallBack);
            }
        }
        return iNetworkChangeCallBack == null ? -1 : iNetworkChangeCallBack.hashCode();
    }

    public synchronized void clearAllLogs() {
        this.mUsedForDay = 0;
        this.mUsedTranslateForDay = 0;
        this.mUsedReceiveForDay = 0;
        this.mUsedForMonth = 0;
        this.mUsedTranslateForMonth = 0;
        this.mUsedReceiveForMonth = 0;
        this.mStartDate = new Date();
        this.yb = this.xZ.getNetDataEntity();
        this.ya.setLastNetDataEntity(this.yb);
        this.ya.resetToDayNetworkInfoEntity();
        this.ya.clearAll();
    }

    protected synchronized void dL() {
        NetworkInfoEntity dK;
        NetDataEntity netDataEntity = this.xZ.getNetDataEntity();
        if ((netDataEntity.mReceiver <= 0 ? 1 : null) == null) {
            long j = netDataEntity.mReceiver + netDataEntity.mTranslate;
            long j2 = this.yb.mReceiver + this.yb.mTranslate;
            if ((this.yb.mReceiver > 0 ? 1 : null) == null) {
                this.yb.mReceiver = 0;
            }
            if ((this.yb.mTranslate > 0 ? 1 : null) == null) {
                this.yb.mTranslate = 0;
            }
            long j3 = ((j2 > 0 ? 1 : (j2 == 0 ? 0 : -1)) > 0 ? 1 : null) == null ? j : j2;
            j2 = netDataEntity.mTranslate - this.yb.mTranslate;
            if ((j2 >= 0 ? 1 : null) == null) {
                j2 = netDataEntity.mTranslate;
            }
            long j4 = j2;
            j2 = netDataEntity.mReceiver - this.yb.mReceiver;
            if ((j2 >= 0 ? 1 : null) == null) {
                j2 = netDataEntity.mReceiver;
            }
            long j5 = j2;
            j2 = j - j3;
            if ((j2 >= 0 ? 1 : null) != null) {
                j = j2;
            }
            notifyConfigChange();
            this.mUsedForDay += j;
            this.mUsedTranslateForDay += j4;
            this.mUsedReceiveForDay += j5;
            this.mUsedForMonth = j + this.mUsedForMonth;
            this.mUsedTranslateForMonth += j4;
            this.mUsedReceiveForMonth += j5;
            this.yb = netDataEntity;
            this.ya.setLastNetDataEntity(this.yb);
            dK = dK();
        } else {
            notifyConfigChange();
            this.yb = netDataEntity;
            this.ya.setLastNetDataEntity(this.yb);
            dK = dK();
        }
        onNormalChanged(dK);
    }

    protected synchronized void dM() {
        NetworkInfoEntity networkInfoEntity = new NetworkInfoEntity();
        networkInfoEntity.mStartDate = this.mStartDate;
        networkInfoEntity.mUsedForDay = this.mUsedForDay;
        networkInfoEntity.mUsedTranslateForDay = this.mUsedTranslateForDay;
        networkInfoEntity.mUsedReceiveForDay = this.mUsedReceiveForDay;
        this.ya.insert(networkInfoEntity);
        this.mUsedForDay = 0;
        this.mUsedTranslateForDay = 0;
        this.mUsedReceiveForDay = 0;
        this.mStartDate = new Date();
        this.ya.resetToDayNetworkInfoEntity();
        onDayChanged();
    }

    protected synchronized void dN() {
        this.mUsedForMonth = 0;
        this.mUsedTranslateForMonth = 0;
        this.mUsedReceiveForMonth = 0;
        this.ya.resetMonthNetworkinfoEntity();
        onClosingDateReached();
    }

    public synchronized void dO() {
        if (this.yg) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(this.mStartDate);
            GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
            GregorianCalendar a = mp.a(gregorianCalendar2, this.yc);
            GregorianCalendar a2 = mp.a(gregorianCalendar, this.yc);
            if (this.yd <= 0) {
                if (a.get(2) != a2.get(2)) {
                    dN();
                }
                if (gregorianCalendar.get(5) != gregorianCalendar2.get(5)) {
                    tmsdk.common.utils.d.d("DefaultNetworkMonitor", "processForDayChanged");
                    dM();
                }
                dL();
            }
        }
    }

    public synchronized void dP() {
        this.yd++;
        tmsdk.common.utils.d.d("DefaultNetworkMonitor", "onSystemTimeChanged ");
        NetworkInfoEntity systemTimeChange = this.ya.getSystemTimeChange(this.mStartDate);
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
        dK();
        this.yd--;
        jq.ct().a(new Runnable() {
            final /* synthetic */ d yi;

            {
                this.yi = r1;
            }

            public void run() {
                this.yi.dO();
            }
        }, "ACTION_TIME_CHANGED");
    }

    public ArrayList<NetworkInfoEntity> getAllLogs() {
        List all = this.ya.getAll();
        if (all != null && all.size() > 0) {
            Collections.sort(all);
        }
        return all;
    }

    public boolean getRefreshState() {
        return this.yh;
    }

    public void networkConnectivityChangeNotify() {
        this.xZ.networkConnectivityChangeNotify();
    }

    public synchronized void notifyConfigChange() {
        if (this.yg) {
            this.yc = this.ya.getClosingDayForMonth();
            this.mTotalForMonth = this.ya.getTotalForMonth();
            this.mUsedForMonth = this.ya.getUsedForMonth();
            NetworkInfoEntity todayNetworkInfoEntity = this.ya.getTodayNetworkInfoEntity();
            this.mUsedTranslateForMonth = todayNetworkInfoEntity.mUsedTranslateForMonth;
            this.mUsedReceiveForMonth = todayNetworkInfoEntity.mUsedReceiveForMonth;
            this.mUsedForDay = todayNetworkInfoEntity.mUsedForDay;
            this.mUsedTranslateForDay = todayNetworkInfoEntity.mUsedTranslateForDay;
            this.mUsedReceiveForDay = todayNetworkInfoEntity.mUsedReceiveForDay;
        }
    }

    public void onClosingDateReached() {
        if (this.yh) {
            HashSet hashSet;
            synchronized (this.ye) {
                hashSet = (HashSet) this.yf.clone();
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
        if (this.yh) {
            HashSet hashSet;
            synchronized (this.ye) {
                hashSet = (HashSet) this.yf.clone();
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
        if (this.yh) {
            HashSet hashSet;
            synchronized (this.ye) {
                hashSet = (HashSet) this.yf.clone();
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
        boolean z;
        synchronized (this.ye) {
            Iterator it = this.yf.iterator();
            while (it.hasNext()) {
                if (((INetworkChangeCallBack) it.next()).hashCode() == i) {
                    z = true;
                    it.remove();
                    break;
                }
            }
            z = false;
        }
        return z;
    }

    public boolean removeCallback(INetworkChangeCallBack iNetworkChangeCallBack) {
        if (iNetworkChangeCallBack == null) {
            return false;
        }
        boolean remove;
        synchronized (this.ye) {
            remove = this.yf.remove(iNetworkChangeCallBack);
        }
        return remove;
    }

    public void setRefreshState(boolean z) {
        this.yh = z;
    }

    public synchronized void z(boolean z) {
        Object obj = null;
        synchronized (this) {
            this.yg = z;
            if (this.yg) {
                this.yb = this.ya.getLastNetDataEntity();
                if (this.yb != null) {
                    if (this.yb.mReceiver >= 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                    }
                }
                this.yb = this.xZ.getNetDataEntity();
                this.ya.setLastNetDataEntity(this.yb);
            } else {
                dK();
            }
        }
    }
}
