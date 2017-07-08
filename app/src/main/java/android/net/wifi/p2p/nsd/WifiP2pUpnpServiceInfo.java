package android.net.wifi.p2p.nsd;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class WifiP2pUpnpServiceInfo extends WifiP2pServiceInfo {
    public static final int VERSION_1_0 = 16;

    private WifiP2pUpnpServiceInfo(List<String> queryList) {
        super(queryList);
    }

    public static WifiP2pUpnpServiceInfo newInstance(String uuid, String device, List<String> services) {
        if (uuid == null || device == null) {
            throw new IllegalArgumentException("uuid or device cannnot be null");
        }
        UUID.fromString(uuid);
        ArrayList<String> info = new ArrayList();
        info.add(createSupplicantQuery(uuid, null));
        info.add(createSupplicantQuery(uuid, "upnp:rootdevice"));
        info.add(createSupplicantQuery(uuid, device));
        if (services != null) {
            for (String service : services) {
                info.add(createSupplicantQuery(uuid, service));
            }
        }
        return new WifiP2pUpnpServiceInfo(info);
    }

    private static String createSupplicantQuery(String uuid, String data) {
        StringBuffer sb = new StringBuffer();
        sb.append("upnp ");
        sb.append(String.format(Locale.US, "%02x ", new Object[]{Integer.valueOf(VERSION_1_0)}));
        sb.append("uuid:");
        sb.append(uuid);
        if (data != null) {
            sb.append("::");
            sb.append(data);
        }
        return sb.toString();
    }
}
