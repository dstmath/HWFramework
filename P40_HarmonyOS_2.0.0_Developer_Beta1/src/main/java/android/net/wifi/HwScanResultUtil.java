package android.net.wifi;

import android.os.Parcel;

public class HwScanResultUtil {
    public static void scanResultWifiproParams(ScanResult sr, ScanResult source) {
        sr.internetAccessType = source.internetAccessType;
        sr.networkQosLevel = source.networkQosLevel;
        sr.networkSecurity = source.networkSecurity;
        sr.networkQosScore = source.networkQosScore;
        sr.dot11vNetwork = source.dot11vNetwork;
    }

    public static void writeToParcelForWifiproParams(ScanResult sr, Parcel dest) {
        dest.writeInt(sr.internetAccessType);
        dest.writeInt(sr.networkQosLevel);
        dest.writeInt(sr.networkSecurity);
        dest.writeInt(sr.networkQosScore);
        dest.writeInt(sr.dot11vNetwork ? 1 : 0);
    }

    public static void createFromParcelForWifiproParams(ScanResult sr, Parcel in) {
        sr.internetAccessType = in.readInt();
        sr.networkQosLevel = in.readInt();
        sr.networkSecurity = in.readInt();
        sr.networkQosScore = in.readInt();
        sr.dot11vNetwork = in.readInt() != 0;
    }
}
