package android.net.wifi.p2p.nsd;

import android.net.nsd.DnsSdTxtRecord;
import android.net.wifi.WifiEnterpriseConfig;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WifiP2pDnsSdServiceInfo extends WifiP2pServiceInfo {
    public static final int DNS_TYPE_PTR = 12;
    public static final int DNS_TYPE_TXT = 16;
    public static final int VERSION_1 = 1;
    private static final Map<String, String> sVmPacket = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo.<clinit>():void");
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
        sb.append(createRequest(serviceType + ".local.", DNS_TYPE_PTR, VERSION_1));
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        byte[] data = instanceName.getBytes();
        Object[] objArr = new Object[VERSION_1];
        objArr[0] = Integer.valueOf(data.length);
        sb.append(String.format(Locale.US, "%02x", objArr));
        sb.append(WifiP2pServiceInfo.bin2HexStr(data));
        sb.append("c027");
        return sb.toString();
    }

    private static String createTxtServiceQuery(String instanceName, String serviceType, DnsSdTxtRecord txtRecord) {
        StringBuffer sb = new StringBuffer();
        sb.append("bonjour ");
        sb.append(createRequest(instanceName + "." + serviceType + ".local.", DNS_TYPE_TXT, VERSION_1));
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
        if (dnsType == DNS_TYPE_TXT) {
            dnsName = dnsName.toLowerCase(Locale.ROOT);
        }
        sb.append(compressDnsName(dnsName));
        Object[] objArr = new Object[VERSION_1];
        objArr[0] = Integer.valueOf(dnsType);
        sb.append(String.format(Locale.US, "%04x", objArr));
        objArr = new Object[VERSION_1];
        objArr[0] = Integer.valueOf(version);
        sb.append(String.format(Locale.US, "%02x", objArr));
        return sb.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String compressDnsName(String dnsName) {
        String data;
        StringBuffer sb = new StringBuffer();
        while (true) {
            data = (String) sVmPacket.get(dnsName);
            if (data != null) {
                break;
            }
            int i = dnsName.indexOf(46);
            if (i == -1) {
                break;
            }
            String name = dnsName.substring(0, i);
            dnsName = dnsName.substring(i + VERSION_1);
            Object[] objArr = new Object[VERSION_1];
            objArr[0] = Integer.valueOf(name.length());
            sb.append(String.format(Locale.US, "%02x", objArr));
            sb.append(WifiP2pServiceInfo.bin2HexStr(name.getBytes()));
            return sb.toString();
        }
        sb.append(data);
        return sb.toString();
    }
}
