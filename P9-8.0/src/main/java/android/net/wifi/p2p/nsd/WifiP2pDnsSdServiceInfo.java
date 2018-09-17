package android.net.wifi.p2p.nsd;

import android.net.nsd.DnsSdTxtRecord;
import android.net.wifi.WifiEnterpriseConfig;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WifiP2pDnsSdServiceInfo extends WifiP2pServiceInfo {
    public static final int DNS_TYPE_PTR = 12;
    public static final int DNS_TYPE_TXT = 16;
    public static final int VERSION_1 = 1;
    private static final Map<String, String> sVmPacket = new HashMap();

    static {
        sVmPacket.put("_tcp.local.", "c00c");
        sVmPacket.put("local.", "c011");
        sVmPacket.put("_udp.local.", "c01c");
    }

    private WifiP2pDnsSdServiceInfo(List<String> queryList) {
        super(queryList);
    }

    public static WifiP2pDnsSdServiceInfo newInstance(String instanceName, String serviceType, Map<String, String> txtMap) {
        if (TextUtils.isEmpty(instanceName) || TextUtils.isEmpty(serviceType)) {
            throw new IllegalArgumentException("instance name or service type cannot be empty");
        }
        DnsSdTxtRecord txtRecord = new DnsSdTxtRecord();
        if (txtMap != null) {
            for (String key : txtMap.keySet()) {
                txtRecord.set(key, (String) txtMap.get(key));
            }
        }
        ArrayList<String> queries = new ArrayList();
        queries.add(createPtrServiceQuery(instanceName, serviceType));
        queries.add(createTxtServiceQuery(instanceName, serviceType, txtRecord));
        return new WifiP2pDnsSdServiceInfo(queries);
    }

    private static String createPtrServiceQuery(String instanceName, String serviceType) {
        StringBuffer sb = new StringBuffer();
        sb.append("bonjour ");
        sb.append(createRequest(serviceType + ".local.", 12, 1));
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(String.format(Locale.US, "%02x", new Object[]{Integer.valueOf(instanceName.getBytes().length)}));
        sb.append(WifiP2pServiceInfo.bin2HexStr(data));
        sb.append("c027");
        return sb.toString();
    }

    private static String createTxtServiceQuery(String instanceName, String serviceType, DnsSdTxtRecord txtRecord) {
        StringBuffer sb = new StringBuffer();
        sb.append("bonjour ");
        sb.append(createRequest(instanceName + "." + serviceType + ".local.", 16, 1));
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        byte[] rawData = txtRecord.getRawData();
        if (rawData.length == 0) {
            sb.append("00");
        } else {
            sb.append(WifiP2pServiceInfo.bin2HexStr(rawData));
        }
        return sb.toString();
    }

    static String createRequest(String dnsName, int dnsType, int version) {
        StringBuffer sb = new StringBuffer();
        if (dnsType == 16) {
            dnsName = dnsName.toLowerCase(Locale.ROOT);
        }
        sb.append(compressDnsName(dnsName));
        sb.append(String.format(Locale.US, "%04x", new Object[]{Integer.valueOf(dnsType)}));
        sb.append(String.format(Locale.US, "%02x", new Object[]{Integer.valueOf(version)}));
        return sb.toString();
    }

    private static String compressDnsName(String dnsName) {
        StringBuffer sb = new StringBuffer();
        while (true) {
            String data = (String) sVmPacket.get(dnsName);
            if (data != null) {
                sb.append(data);
                break;
            }
            int i = dnsName.indexOf(46);
            if (i == -1) {
                if (dnsName.length() > 0) {
                    sb.append(String.format(Locale.US, "%02x", new Object[]{Integer.valueOf(dnsName.length())}));
                    sb.append(WifiP2pServiceInfo.bin2HexStr(dnsName.getBytes()));
                }
                sb.append("00");
            } else {
                String name = dnsName.substring(0, i);
                dnsName = dnsName.substring(i + 1);
                sb.append(String.format(Locale.US, "%02x", new Object[]{Integer.valueOf(name.length())}));
                sb.append(WifiP2pServiceInfo.bin2HexStr(name.getBytes()));
            }
        }
        return sb.toString();
    }
}
