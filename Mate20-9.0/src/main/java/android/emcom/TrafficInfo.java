package android.emcom;

public class TrafficInfo {
    private static final String TAG = "TrafficInfo";
    public int mpRadioTraffic = 0;
    public int mpWifiTraffic = 0;
    public String pkgName = null;
    public int radioTraffic = 0;
    public int wifiTraffic = 0;

    public void cleanTrafficInfo() {
        this.wifiTraffic = 0;
        this.radioTraffic = 0;
        this.mpWifiTraffic = 0;
        this.mpRadioTraffic = 0;
    }

    public TrafficInfo setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
        return this;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public TrafficInfo setWifiTraffic(int wifiTraffic2) {
        this.wifiTraffic = wifiTraffic2;
        return this;
    }

    public int getWifiTraffic() {
        return this.wifiTraffic;
    }

    public TrafficInfo setRadioTraffic(int radioTraffic2) {
        this.radioTraffic = radioTraffic2;
        return this;
    }

    public int getRadioTraffic() {
        return this.radioTraffic;
    }

    public void setMpWifiTraffic(int mpWifiTraffic2) {
        this.mpWifiTraffic = mpWifiTraffic2;
    }

    public int getMpWifiTraffic() {
        return this.mpWifiTraffic;
    }

    public void setMpRadioTraffic(int mpRadioTraffic2) {
        this.mpRadioTraffic = mpRadioTraffic2;
    }

    public int getMpRadioTraffic() {
        return this.mpRadioTraffic;
    }
}
