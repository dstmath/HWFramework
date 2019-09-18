package com.huawei.opcollect.collector.pullcollection;

import android.content.Context;
import android.os.RemoteException;
import com.huawei.nb.model.collectencrypt.RawDeviceStatusInfo;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.odmf.OdmfHelper;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.Date;

public class DeviceStatusInfoAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "DeviceStatusInfoAction";
    private static DeviceStatusInfoAction sInstance = null;
    private Collection mCollection = new Collection();

    public static DeviceStatusInfoAction getInstance(Context context) {
        DeviceStatusInfoAction deviceStatusInfoAction;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new DeviceStatusInfoAction(context, OPCollectConstant.DEVICE_STATUS_INFO_ACTION_NAME);
            }
            deviceStatusInfoAction = sInstance;
        }
        return deviceStatusInfoAction;
    }

    private DeviceStatusInfoAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(queryDailyRecordNum(RawDeviceStatusInfo.class));
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static void destroyInstance() {
        synchronized (LOCK) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        OPCollectLog.r(TAG, "execute");
        return collectRawDeviceStatusInfoData();
    }

    private boolean collectRawDeviceStatusInfoData() {
        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, getRawDeviceStatusInfo()).sendToTarget();
        return true;
    }

    private RawDeviceStatusInfo getRawDeviceStatusInfo() {
        RawDeviceStatusInfo rawDeviceStatusInfo = new RawDeviceStatusInfo();
        rawDeviceStatusInfo.setMTimeStamp(new Date());
        if (OPCollectUtils.checkODMFApiVersion(this.mContext, OdmfHelper.ODMF_API_VERSION_2_11_2)) {
            rawDeviceStatusInfo.setMAppInstalled(OPCollectUtils.getThirdPartyAppList(this.mContext));
            rawDeviceStatusInfo.setMAppUsageTime(OPCollectUtils.getAppUsageState(this.mContext));
            rawDeviceStatusInfo.setMWifiDataTotal(String.valueOf(getTodayWifiTotalBytes()));
            rawDeviceStatusInfo.setMMobileDataTotal(String.valueOf(getTodayMobileTotalBytes()));
            rawDeviceStatusInfo.setMMobileDataSurplus(String.valueOf(getMobileLeftBytes()));
        }
        rawDeviceStatusInfo.setMReservedInt(0);
        rawDeviceStatusInfo.setMReservedText(OPCollectUtils.formatCurrentTime());
        return rawDeviceStatusInfo;
    }

    private long getTodayMobileTotalBytes() {
        return NetAssistantManager.getInstance(this.mContext).getTodayMobileTotalBytes(this.mCollection.getDefaultDataSlotIMSI(this.mContext));
    }

    private long getMobileLeftBytes() {
        try {
            return NetAssistantManager.getInstance(this.mContext).getMobileLeftBytes(this.mCollection.getDefaultDataSlotIMSI(this.mContext));
        } catch (RemoteException e) {
            OPCollectLog.e(TAG, "RemoteException:" + e.getMessage());
            return 0;
        }
    }

    private long getTodayWifiTotalBytes() {
        return NetAssistantManager.getInstance(this.mContext).getTodayWifiTotalBytes();
    }
}
