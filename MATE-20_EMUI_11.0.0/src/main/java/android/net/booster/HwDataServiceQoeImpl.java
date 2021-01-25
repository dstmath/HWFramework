package android.net.booster;

import android.net.booster.IHwCommBoosterCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

public final class HwDataServiceQoeImpl implements HwDataServiceQoe {
    private static final int APP_SERVICE_QOE_NOTIFY = 12001;
    private static final boolean BOOSTER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_booster_qoe", true);
    private static final int DATA_SEND_TO_AP_MODULE_CARED_APP_QOE = 12;
    private static final int HWSDK_CARED_APPS = 901;
    private static final Object LOCK = new Object();
    private static final String TAG = "HwDataServiceQoeImpl";
    private static HwCommBoosterServiceManager mHwCommBoosterServiceManager = null;
    private static HwDataServiceQoeImpl mHwDataServiceQoeImpl = null;
    private Handler mHandler = null;
    private final IHwCommBoosterCallback mHwBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class android.net.booster.HwDataServiceQoeImpl.AnonymousClass1 */

        public void callBack(int type, Bundle bundle) throws RemoteException {
            if (bundle != null && type == 12 && HwDataServiceQoeImpl.this.mHandler != null) {
                Message msg = HwDataServiceQoeImpl.this.mHandler.obtainMessage(HwDataServiceQoeImpl.APP_SERVICE_QOE_NOTIFY);
                msg.obj = bundle;
                msg.sendToTarget();
            }
        }
    };

    private HwDataServiceQoeImpl() {
        Log.i(TAG, TAG);
    }

    public static HwDataServiceQoeImpl getInstance() {
        HwDataServiceQoeImpl hwDataServiceQoeImpl;
        synchronized (LOCK) {
            if (mHwDataServiceQoeImpl == null) {
                mHwDataServiceQoeImpl = new HwDataServiceQoeImpl();
            }
            hwDataServiceQoeImpl = mHwDataServiceQoeImpl;
        }
        return hwDataServiceQoeImpl;
    }

    public class DataServiceStatus {
        public static final int APP_SERVICE_QOE_BAD = 2;
        public static final int APP_SERVICE_QOE_GOOD = 4;
        public static final int APP_SERVICE_QOE_NOT_BAD = 3;
        public static final int APP_SERVICE_QOE_VERY_BAD = 1;
        public static final int APP_SERVICE_QOE_VERY_GOOD = 5;
        public static final int QOE_BAD_REASON_OTA_DOWNLINK = 2;
        public static final int QOE_BAD_REASON_OTA_UPLINK = 1;
        public static final int QOE_BAD_REASON_SERVICE = 3;
        public static final int QOE_BAD_REASON_UNKNOWN = 0;
        public int mAppServiceQuality = 5;
        public int mDownlinkBandwidth = 0;
        public int mOtaRtt = 0;
        public int mQualityBadReason = 0;
        public int mReasonConfidenceLevel = 0;
        public int mUplinkBandwidth = 0;
        public int mUplinkPacketLossRate = 0;

        public DataServiceStatus() {
        }
    }

    public int registerNetworkQoe(String caller, String packageNameList, Handler handler) {
        if (!BOOSTER_SUPPORT || packageNameList == null || caller == null || caller.isEmpty() || handler == null) {
            Log.i(TAG, "registerNetworkQoe ERROR_INVALID_PARAM");
            return -3;
        }
        mHwCommBoosterServiceManager = HwCommBoosterServiceManager.getInstance();
        HwCommBoosterServiceManager hwCommBoosterServiceManager = mHwCommBoosterServiceManager;
        if (hwCommBoosterServiceManager == null) {
            Log.i(TAG, "registerNetworkQoe ERROR_INVALID_PARAM");
            return -3;
        } else if (hwCommBoosterServiceManager.registerCallBack(caller, this.mHwBoosterCallback) != 0) {
            Log.i(TAG, "registerCallback fail");
            return -3;
        } else {
            this.mHandler = handler;
            Bundle bundle = new Bundle();
            bundle.putString("pkgNameList", packageNameList);
            return mHwCommBoosterServiceManager.reportBoosterPara(caller, HWSDK_CARED_APPS, bundle);
        }
    }

    public int unRegisterNetworkQoe(String caller) {
        HwCommBoosterServiceManager hwCommBoosterServiceManager;
        if (BOOSTER_SUPPORT && caller != null && (hwCommBoosterServiceManager = mHwCommBoosterServiceManager) != null) {
            return hwCommBoosterServiceManager.unRegisterCallBack(caller, this.mHwBoosterCallback);
        }
        Log.i(TAG, "unRegisterNetworkQoe ERROR_INVALID_PARAM");
        return -3;
    }
}
