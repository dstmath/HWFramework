package android.net.wifi;

import android.os.Parcel;

public class HwWifiConfigurationUtil {
    public static final int PORTAL_UNKNOWN = 0;

    public static void initWifiproParams(WifiConfiguration config) {
        config.noInternetAccess = false;
        config.internetHistory = "-1/-1/-1/-1/-1/-1/-1/-1/-1/-1";
        config.portalNetwork = false;
        config.lastDhcpResults = "";
        config.internetSelfCureHistory = "";
        config.portalCheckStatus = 0;
        config.poorRssiDectected = false;
        config.lastHasInternetTimestamp = 0;
        config.lastTrySwitchWifiTimestamp = 0;
        config.portalAuthTimestamp = 0;
        config.portalValidityDuration = 0;
        config.internetRecoveryStatus = 3;
        config.internetRecoveryCheckTimestamp = 0;
        config.rssiStatusDisabled = -200;
        config.lastConnFailedType = 0;
        config.lastConnFailedTimestamp = 0;
        config.consecutiveGoodRssiCounter = 0;
        config.rssiDiscNonLocally = 0;
        config.timestampDiscNonLocally = 0;
        config.wifiProNoInternetAccess = false;
        config.wifiProNoHandoverNetwork = false;
        config.wifiProNoInternetReason = 0;
        config.internetAccessType = 0;
        config.networkQosLevel = 0;
        config.networkQosScore = 0;
        config.isTempCreated = false;
    }

    public static void readWifiproParams(WifiConfiguration config, WifiConfiguration source) {
        config.noInternetAccess = source.noInternetAccess;
        config.internetHistory = source.internetHistory;
        config.portalNetwork = source.portalNetwork;
        config.lastDhcpResults = source.lastDhcpResults;
        config.internetSelfCureHistory = source.internetSelfCureHistory;
        config.portalCheckStatus = source.portalCheckStatus;
        config.poorRssiDectected = source.poorRssiDectected;
        config.consecutiveGoodRssiCounter = source.consecutiveGoodRssiCounter;
        config.lastHasInternetTimestamp = source.lastHasInternetTimestamp;
        config.lastTrySwitchWifiTimestamp = source.lastTrySwitchWifiTimestamp;
        config.portalAuthTimestamp = source.portalAuthTimestamp;
        config.portalValidityDuration = source.portalValidityDuration;
        config.internetRecoveryStatus = source.internetRecoveryStatus;
        config.internetRecoveryCheckTimestamp = source.internetRecoveryCheckTimestamp;
        config.rssiStatusDisabled = source.rssiStatusDisabled;
        config.lastConnFailedType = source.lastConnFailedType;
        config.lastConnFailedTimestamp = source.lastConnFailedTimestamp;
        config.rssiDiscNonLocally = source.rssiDiscNonLocally;
        config.timestampDiscNonLocally = source.timestampDiscNonLocally;
        config.wifiProNoHandoverNetwork = source.wifiProNoHandoverNetwork;
        config.wifiProNoInternetAccess = source.wifiProNoInternetAccess;
        config.wifiProNoInternetReason = source.wifiProNoInternetReason;
        config.internetAccessType = source.internetAccessType;
        config.networkQosLevel = source.networkQosLevel;
        config.networkQosScore = source.networkQosScore;
        config.isTempCreated = source.isTempCreated;
    }

    public static void writeToParcelForWifiproParams(WifiConfiguration config, Parcel dest) {
        dest.writeInt(config.noInternetAccess ? 1 : 0);
        dest.writeString(config.internetHistory);
        dest.writeInt(config.portalNetwork ? 1 : 0);
        dest.writeString(config.lastDhcpResults);
        dest.writeString(config.internetSelfCureHistory);
        dest.writeInt(config.portalCheckStatus);
        dest.writeInt(config.poorRssiDectected ? 1 : 0);
        dest.writeInt(config.consecutiveGoodRssiCounter);
        dest.writeLong(config.lastHasInternetTimestamp);
        dest.writeLong(config.lastTrySwitchWifiTimestamp);
        dest.writeLong(config.portalAuthTimestamp);
        dest.writeLong(config.portalValidityDuration);
        dest.writeInt(config.internetRecoveryStatus);
        dest.writeLong(config.internetRecoveryCheckTimestamp);
        dest.writeInt(config.rssiStatusDisabled);
        dest.writeInt(config.lastConnFailedType);
        dest.writeLong(config.lastConnFailedTimestamp);
        dest.writeInt(config.rssiDiscNonLocally);
        dest.writeLong(config.timestampDiscNonLocally);
        dest.writeInt(config.wifiProNoHandoverNetwork ? 1 : 0);
        dest.writeInt(config.wifiProNoInternetAccess ? 1 : 0);
        dest.writeInt(config.wifiProNoInternetReason);
        dest.writeInt(config.internetAccessType);
        dest.writeInt(config.networkQosLevel);
        dest.writeInt(config.networkQosScore);
        dest.writeInt(config.isTempCreated ? 1 : 0);
    }

    public static void createFromParcelForWifiproParams(WifiConfiguration config, Parcel in) {
        boolean z = true;
        config.noInternetAccess = in.readInt() != 0;
        config.internetHistory = in.readString();
        config.portalNetwork = in.readInt() != 0;
        config.lastDhcpResults = in.readString();
        config.internetSelfCureHistory = in.readString();
        config.portalCheckStatus = in.readInt();
        config.poorRssiDectected = in.readInt() != 0;
        config.consecutiveGoodRssiCounter = in.readInt();
        config.lastHasInternetTimestamp = in.readLong();
        config.lastTrySwitchWifiTimestamp = in.readLong();
        config.portalAuthTimestamp = in.readLong();
        config.portalValidityDuration = in.readLong();
        config.internetRecoveryStatus = in.readInt();
        config.internetRecoveryCheckTimestamp = in.readLong();
        config.rssiStatusDisabled = in.readInt();
        config.lastConnFailedType = in.readInt();
        config.lastConnFailedTimestamp = in.readLong();
        config.rssiDiscNonLocally = in.readInt();
        config.timestampDiscNonLocally = in.readLong();
        config.wifiProNoHandoverNetwork = in.readInt() != 0;
        config.wifiProNoInternetAccess = in.readInt() != 0;
        config.wifiProNoInternetReason = in.readInt();
        config.internetAccessType = in.readInt();
        config.networkQosLevel = in.readInt();
        config.networkQosScore = in.readInt();
        if (in.readInt() == 0) {
            z = false;
        }
        config.isTempCreated = z;
    }
}
