package android.net.booster;

import android.common.HwFrameworkFactory;
import android.os.Handler;

public final class HwDataServiceQoeEx {

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

    public static int registerNetworkQoe(String caller, String packageNameList, Handler handler) {
        return HwFrameworkFactory.getHwDataServiceQoe().registerNetworkQoe(caller, packageNameList, handler);
    }

    public static int unRegisterNetworkQoe(String caller) {
        return HwFrameworkFactory.getHwDataServiceQoe().unRegisterNetworkQoe(caller);
    }
}
