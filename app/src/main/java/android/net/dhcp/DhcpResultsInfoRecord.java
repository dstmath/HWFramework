package android.net.dhcp;

public class DhcpResultsInfoRecord {
    public String apDhcpServer;
    public String apSSID;
    public String staIP;

    public DhcpResultsInfoRecord(String ssid, String IP, String DhcpServer) {
        if (ssid != null) {
            this.apSSID = ssid;
        }
        if (IP != null) {
            this.staIP = IP;
        }
        if (DhcpServer != null) {
            this.apDhcpServer = DhcpServer;
        }
    }
}
