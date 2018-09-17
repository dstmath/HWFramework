package android.net.wifi.p2p.nsd;

public class WifiP2pDnsSdServiceRequest extends WifiP2pServiceRequest {
    private WifiP2pDnsSdServiceRequest(String query) {
        super(1, query);
    }

    private WifiP2pDnsSdServiceRequest() {
        super(1, null);
    }

    private WifiP2pDnsSdServiceRequest(String dnsQuery, int dnsType, int version) {
        super(1, WifiP2pDnsSdServiceInfo.createRequest(dnsQuery, dnsType, version));
    }

    public static WifiP2pDnsSdServiceRequest newInstance() {
        return new WifiP2pDnsSdServiceRequest();
    }

    public static WifiP2pDnsSdServiceRequest newInstance(String serviceType) {
        if (serviceType != null) {
            return new WifiP2pDnsSdServiceRequest(serviceType + ".local.", 12, 1);
        }
        throw new IllegalArgumentException("service type cannot be null");
    }

    public static WifiP2pDnsSdServiceRequest newInstance(String instanceName, String serviceType) {
        if (instanceName != null && serviceType != null) {
            return new WifiP2pDnsSdServiceRequest(instanceName + "." + serviceType + ".local.", 16, 1);
        }
        throw new IllegalArgumentException("instance name or service type cannot be null");
    }
}
