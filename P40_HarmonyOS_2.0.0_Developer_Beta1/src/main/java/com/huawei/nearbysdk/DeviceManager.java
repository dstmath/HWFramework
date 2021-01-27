package com.huawei.nearbysdk;

import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.publishinfo.PublishDeviceInfo;
import com.huawei.nearbysdk.publishinfo.PublishDeviceRule;
import java.util.HashMap;
import java.util.Map;

public class DeviceManager {
    private static final String TAG = "DeviceManager";
    private IDeviceManager mDeviceManager;
    private Map<Integer, PublishListenerTransport> mListeners = new HashMap();
    private Looper mLooper;

    DeviceManager(INearbyAdapter nearbyService, Looper looper) throws RemoteException {
        HwLog.d(TAG, "guoh DeviceManager 123");
        this.mDeviceManager = nearbyService.getDeviceManager();
        HwLog.e(TAG, "mDeviceManager is: " + this.mDeviceManager);
        this.mLooper = looper;
    }

    private boolean publishDevice(PublishDeviceInfo deviceInfo, PublishDeviceRule deviceRule, PublishListener publishListener, Looper looper) {
        boolean isPublishSuccess = false;
        HwLog.d(TAG, "deviceInfo:" + deviceInfo + " deviceRule:" + deviceRule + " publishListener:" + publishListener + " mDeviceManager" + this.mDeviceManager);
        if (deviceInfo == null || deviceRule == null || publishListener == null || this.mDeviceManager == null) {
            HwLog.e(TAG, "publishDevice, parmas is null");
            return false;
        }
        if (this.mListeners.get(Integer.valueOf(deviceRule.getTypeChannel())) == null) {
            HwLog.d(TAG, "publishDevice, to new PublishListenerTransport");
            if (looper == null) {
                this.mListeners.put(Integer.valueOf(deviceRule.getTypeChannel()), new PublishListenerTransport(publishListener, this.mLooper));
            } else {
                this.mListeners.put(Integer.valueOf(deviceRule.getTypeChannel()), new PublishListenerTransport(publishListener, looper));
            }
            try {
                isPublishSuccess = this.mDeviceManager.publishDevice(deviceInfo, deviceRule, this.mListeners.get(Integer.valueOf(deviceRule.getTypeChannel())));
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in publishDevice" + e.getLocalizedMessage());
            }
        } else {
            try {
                isPublishSuccess = this.mDeviceManager.publishDevice(deviceInfo, deviceRule, this.mListeners.get(Integer.valueOf(deviceRule.getTypeChannel())));
            } catch (RemoteException e2) {
                HwLog.d(TAG, "error in publishDevice:" + e2.getLocalizedMessage());
            }
        }
        return isPublishSuccess;
    }

    public boolean publishDevice(PublishDeviceInfo deviceInfo, PublishDeviceRule publishDeviceRule, PublishListener publishListener) {
        HwLog.d(TAG, "publishDevice");
        return publishDevice(deviceInfo, publishDeviceRule, publishListener, Looper.myLooper());
    }

    public boolean unPublishDevice(PublishDeviceInfo deviceInfo, PublishDeviceRule deviceRule, PublishListener listener) {
        HwLog.d(TAG, "unPublishDevice");
        boolean isUnpublishSuccess = false;
        if (this.mDeviceManager == null) {
            HwLog.e(TAG, "mNearbyService is null. unPublish return false");
            return false;
        } else if (deviceRule == null) {
            return false;
        } else {
            try {
                HwLog.d(TAG, "NearbyService unPublishDevice start");
                isUnpublishSuccess = this.mDeviceManager.unPublishDevice(this.mListeners.get(Integer.valueOf(deviceRule.getTypeChannel())));
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in unPublishDevice:" + e.getLocalizedMessage());
            }
            return isUnpublishSuccess;
        }
    }

    public boolean publishModelId(String modelId, String subModelId) {
        try {
            return this.mDeviceManager.publishModelId(modelId, subModelId);
        } catch (RemoteException e) {
            HwLog.e(TAG, "publishModelId:" + e.getLocalizedMessage());
            return false;
        }
    }

    public void setWifiConnectMode(int mode) {
        try {
            this.mDeviceManager.setWifiConnectMode(mode);
        } catch (RemoteException e) {
            HwLog.e(TAG, "setWifiConnectMode:" + e.getLocalizedMessage());
        }
    }
}
