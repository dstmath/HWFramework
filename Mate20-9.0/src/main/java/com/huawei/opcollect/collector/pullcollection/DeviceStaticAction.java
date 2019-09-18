package com.huawei.opcollect.collector.pullcollection;

import android.content.Context;
import com.huawei.nb.model.collectencrypt.RawDeviceInfo;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;

public class DeviceStaticAction extends Action {
    private static final Object LOCK = new Object();
    private static final String TAG = "DeviceStaticAction";
    private static DeviceStaticAction instance = null;
    private Collection mCollection;

    public static DeviceStaticAction getInstance(Context context) {
        DeviceStaticAction deviceStaticAction;
        synchronized (LOCK) {
            if (instance == null) {
                instance = new DeviceStaticAction(context, OPCollectConstant.DEVICE_ACTION_NAME);
            }
            deviceStaticAction = instance;
        }
        return deviceStaticAction;
    }

    private DeviceStaticAction(Context context, String name) {
        super(context, name);
        this.mCollection = null;
        this.mCollection = new Collection();
        setDailyRecordNum(queryDailyRecordNum(RawDeviceInfo.class));
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static void destroyInstance() {
        synchronized (LOCK) {
            instance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        OPCollectLog.r(TAG, "execute");
        return collectRawDeviceStaticData();
    }

    private boolean collectRawDeviceStaticData() {
        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, getRawDeviceInfo()).sendToTarget();
        return true;
    }

    private RawDeviceInfo getRawDeviceInfo() {
        RawDeviceInfo rawDeviceInfo = new RawDeviceInfo();
        rawDeviceInfo.setMDeviceName(this.mCollection.getDeviceName());
        rawDeviceInfo.setMHardwareVer(this.mCollection.getHardwareVersion());
        rawDeviceInfo.setMSoftwareVer(this.mCollection.getBuildNumber());
        rawDeviceInfo.setMLanguageRegion(this.mCollection.getLanguage());
        rawDeviceInfo.setMTimeStamp(OPCollectUtils.getCurrentTime());
        rawDeviceInfo.setMPhoneNum(this.mCollection.getAllPhoneNumber(this.mContext));
        rawDeviceInfo.setMReservedInt(0);
        rawDeviceInfo.setMReservedText(OPCollectUtils.formatCurrentTime());
        return rawDeviceInfo;
    }
}
