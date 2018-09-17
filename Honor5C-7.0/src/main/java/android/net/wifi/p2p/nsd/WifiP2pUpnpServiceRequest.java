package android.net.wifi.p2p.nsd;

import java.util.Locale;

public class WifiP2pUpnpServiceRequest extends WifiP2pServiceRequest {
    protected WifiP2pUpnpServiceRequest(String query) {
        super(2, query);
    }

    protected WifiP2pUpnpServiceRequest() {
        super(2, null);
    }

    public static WifiP2pUpnpServiceRequest newInstance() {
        return new WifiP2pUpnpServiceRequest();
    }

    public static WifiP2pUpnpServiceRequest newInstance(String st) {
        if (st == null) {
            throw new IllegalArgumentException("search target cannot be null");
        }
        StringBuffer sb = new StringBuffer();
        sb.append(String.format(Locale.US, "%02x", new Object[]{Integer.valueOf(16)}));
        sb.append(WifiP2pServiceInfo.bin2HexStr(st.getBytes()));
        return new WifiP2pUpnpServiceRequest(sb.toString());
    }
}
