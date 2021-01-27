package ohos.batterysipperadapter;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pgmng.plug.DetailBatterySipper;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.ArrayList;
import java.util.List;
import ohos.batterysipper.DetailBatteryStats;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BatterySipperAdapter {
    private static final int LOG_DOMAIN = 218114307;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final String TAG = "BatterySipperAdapter";
    private static volatile BatterySipperAdapter batterySipperAdapter;
    private Context aospContext = null;
    private PowerKit pGSdk = PowerKit.getInstance();

    private BatterySipperAdapter() {
    }

    public static BatterySipperAdapter getInstance() {
        if (batterySipperAdapter == null) {
            synchronized (BatterySipperAdapter.class) {
                if (batterySipperAdapter == null) {
                    batterySipperAdapter = new BatterySipperAdapter();
                }
            }
        }
        return batterySipperAdapter;
    }

    public List<DetailBatteryStats> getBatteryStats(ohos.app.Context context, List<Integer> list) {
        ArrayList arrayList = new ArrayList();
        if (this.pGSdk == null) {
            HiLog.error(LOG_LABEL, "pGSdk is null", new Object[0]);
            return arrayList;
        }
        Object hostContext = context.getHostContext();
        if (hostContext instanceof Context) {
            Context context2 = (Context) hostContext;
            List<UserHandle> userHandleList = getUserHandleList(list);
            if (userHandleList == null) {
                HiLog.error(LOG_LABEL, "userList is null", new Object[0]);
                return arrayList;
            }
            List<DetailBatterySipper> arrayList2 = new ArrayList();
            try {
                arrayList2 = this.pGSdk.getBatteryStats(context2, userHandleList);
            } catch (RemoteException e) {
                HiLog.error(LOG_LABEL, "failed getBatteryStats from pGSdk %{public}s", new Object[]{e});
            }
            if (arrayList2 == null) {
                HiLog.error(LOG_LABEL, "failed getBatteryStats detailList is null", new Object[0]);
                return arrayList;
            }
            for (DetailBatterySipper detailBatterySipper : arrayList2) {
                arrayList.add(syncStatsFromBatterySipper(detailBatterySipper));
            }
        }
        return arrayList;
    }

    public List<DetailBatteryStats> getBatteryStats(List<Integer> list) {
        ArrayList arrayList = new ArrayList();
        if (this.pGSdk == null) {
            HiLog.error(LOG_LABEL, "pGSdk is null", new Object[0]);
            return arrayList;
        }
        Context aospContext2 = getAospContext();
        if (aospContext2 == null) {
            HiLog.error(LOG_LABEL, "get android context and it is null", new Object[0]);
            return arrayList;
        }
        List<UserHandle> userHandleList = getUserHandleList(list);
        List<DetailBatterySipper> arrayList2 = new ArrayList();
        try {
            arrayList2 = this.pGSdk.getBatteryStats(aospContext2, userHandleList);
        } catch (RemoteException e) {
            HiLog.error(LOG_LABEL, "failed getBatteryStats from pGSdk %{public}s", new Object[]{e});
        }
        if (arrayList2 == null) {
            HiLog.error(LOG_LABEL, "failed getBatteryStats detailList is null", new Object[0]);
            return arrayList;
        }
        for (DetailBatterySipper detailBatterySipper : arrayList2) {
            arrayList.add(syncStatsFromBatterySipper(detailBatterySipper));
        }
        return arrayList;
    }

    private Context getAospContext() {
        Application currentApplication;
        if (this.aospContext == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            this.aospContext = currentApplication.getApplicationContext();
        }
        return this.aospContext;
    }

    private List<UserHandle> getUserHandleList(List<Integer> list) {
        ArrayList arrayList = new ArrayList();
        for (Integer num : list) {
            UserHandle userHandle = UserHandleEx.getUserHandle(num.intValue());
            if (userHandle != null) {
                arrayList.add(userHandle);
            }
        }
        return arrayList;
    }

    private DetailBatteryStats syncStatsFromBatterySipper(DetailBatterySipper detailBatterySipper) {
        DetailBatteryStats detailBatteryStats = new DetailBatteryStats();
        if (detailBatterySipper == null) {
            return detailBatteryStats;
        }
        if (detailBatterySipper.mMobileEntry.length >= 2 && detailBatterySipper.mWifiEntry.length >= 2 && detailBatterySipper.mMobileEntry[0].mItem.length >= 4 && detailBatterySipper.mMobileEntry[1].mItem.length >= 4 && detailBatterySipper.mWifiEntry[0].mItem.length >= 4 && detailBatterySipper.mWifiEntry[1].mItem.length >= 4) {
            for (int i = 0; i < 4; i++) {
                detailBatteryStats.mobileEntry[0].mItem[i] = detailBatterySipper.mMobileEntry[0].mItem[i];
                detailBatteryStats.mobileEntry[1].mItem[i] = detailBatterySipper.mMobileEntry[1].mItem[i];
                detailBatteryStats.wifiEntry[0].mItem[i] = detailBatterySipper.mWifiEntry[0].mItem[i];
                detailBatteryStats.wifiEntry[1].mItem[i] = detailBatterySipper.mWifiEntry[1].mItem[i];
            }
        }
        detailBatteryStats.packageName = detailBatterySipper.mName;
        detailBatteryStats.callerUid = detailBatterySipper.mUid;
        detailBatteryStats.fgScreenTime = detailBatterySipper.mFgScreenTime;
        detailBatteryStats.bgGpsTime = detailBatterySipper.mBgGpsTime;
        detailBatteryStats.fgGpsTime = detailBatterySipper.mFgGpsTime;
        detailBatteryStats.bgWlTime = detailBatterySipper.mBgWlTime;
        detailBatteryStats.bgCpuTime = detailBatterySipper.mBgCpuTime;
        detailBatteryStats.fgCpuTime = detailBatterySipper.mFgCpuTime;
        detailBatteryStats.fgGpuTime = detailBatterySipper.mFgGpuTime;
        return detailBatteryStats;
    }
}
