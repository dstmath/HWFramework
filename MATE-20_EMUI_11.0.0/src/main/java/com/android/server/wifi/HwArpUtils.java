package com.android.server.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.arp.HWArpPeer;
import android.net.arp.HWMultiGW;
import android.net.netlink.NetlinkMessage;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.RtNetlinkMessage;
import android.net.netlink.StructNlMsgHdr;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Pair;
import android.util.wifi.HwHiLog;
import com.android.server.HwServiceFactory;
import com.huawei.ncdft.HwNcDftConnManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import libcore.io.IoUtils;

public class HwArpUtils {
    private static final String TAG = "HwArpUtils";
    private ArrayList<ArpItem> mArpItems;
    private ConnectivityManager mConnectivity;
    private Context mContext;
    private INetworkManagementService mNwService;
    private WifiManager mWifiManager;

    private native int nativeReadArpDetail();

    public static class ArpItem {
        private static final int ATF_COM = 2;
        private static final int ATF_PERM = 4;
        public static final int MAX_FAIL_CNT = 10;
        public String device;
        private int failcnt;
        public int flag;
        public String hwaddr;
        public String ipaddr;

        public ArpItem(String ip, String mac, int flag2, String ifname) {
            this.failcnt = 0;
            this.ipaddr = ip;
            this.hwaddr = mac.toLowerCase(Locale.ENGLISH);
            this.device = ifname;
            this.flag = flag2;
        }

        public ArpItem(String mac, int failcnt2) {
            this.failcnt = 0;
            this.ipaddr = "";
            this.hwaddr = mac.toLowerCase(Locale.ENGLISH);
            this.device = "";
            this.flag = 0;
            this.failcnt = failcnt2;
        }

        public boolean matchMaxRetried() {
            return this.failcnt >= 10;
        }

        public void putFail() {
            this.failcnt++;
        }

        public boolean sameIpaddress(String ip) {
            return !TextUtils.isEmpty(ip) && ip.equals(this.ipaddr);
        }

        public boolean isStaticArp() {
            return (this.flag & 4) == 4;
        }

        public boolean sameMacAddress(String mac) {
            return mac != null && mac.toLowerCase(Locale.ENGLISH).equals(this.hwaddr);
        }

        public boolean isValid() {
            return ((this.flag & 2) == 2) && "wlan0".equals(this.device) && (this.hwaddr.length() == 17);
        }

        public String toString() {
            return String.format(Locale.ENGLISH, "%s %d %s %s", this.ipaddr, Integer.valueOf(this.flag), this.hwaddr, this.device);
        }
    }

    public HwArpUtils(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            this.mConnectivity = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            this.mNwService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }
    }

    private HWArpPeer constructArpPeer() throws SocketException {
        DhcpInfo dhcpInfo;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || this.mConnectivity == null) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        LinkProperties linkProperties = this.mConnectivity.getLinkProperties(1);
        String linkIFName = linkProperties != null ? linkProperties.getInterfaceName() : "wlan0";
        if (wifiInfo == null || (dhcpInfo = this.mWifiManager.getDhcpInfo()) == null || dhcpInfo.gateway == 0) {
            return null;
        }
        return new HWArpPeer(linkIFName, NetworkUtils.intToInetAddress(wifiInfo.getIpAddress()), wifiInfo.getMacAddress(), NetworkUtils.intToInetAddress(dhcpInfo.gateway));
    }

    public void doGratuitousArp(int timeout) {
        byte[] rspMac;
        HWArpPeer peer = null;
        try {
            peer = constructArpPeer();
            if (!(peer == null || (rspMac = peer.doGratuitousArp(timeout)) == null || rspMac.length != 6)) {
                HwHiLog.w(TAG, false, "%{private}02x:%{private}02x:%{private}02x:%{private}02x:%{private}02x:%{private}02x also use My IP(IP conflict detected)", new Object[]{Byte.valueOf(rspMac[0]), Byte.valueOf(rspMac[1]), Byte.valueOf(rspMac[2]), Byte.valueOf(rspMac[3]), Byte.valueOf(rspMac[4]), Byte.valueOf(rspMac[5])});
            }
            if (peer == null) {
                return;
            }
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "doGratuitousArp:%{public}s", new Object[]{e.getMessage()});
            if (0 == 0) {
                return;
            }
        } catch (IllegalArgumentException e2) {
            HwHiLog.e(TAG, false, "doGratuitousArp:%{public}s", new Object[]{e2.getMessage()});
            if (0 == 0) {
                return;
            }
        } catch (Exception e3) {
            HwHiLog.e(TAG, false, "doGratuitousArp fail", new Object[0]);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                peer.close();
            }
            throw th;
        }
        peer.close();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
        if (r2 != null) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0062, code lost:
        if (0 == 0) goto L_0x0065;
     */
    public Pair<Boolean, Long> getGateWayArpResult(int times, int timeout) {
        HWArpPeer peer = null;
        boolean isReachable = false;
        long rtt = -1L;
        int i = -1;
        try {
            peer = constructArpPeer();
            if (peer != null) {
                i = 0;
                while (true) {
                    if (i >= times) {
                        break;
                    }
                    long start = SystemClock.elapsedRealtime();
                    if (peer.doArp(timeout) != null) {
                        isReachable = true;
                        rtt = Long.valueOf(SystemClock.elapsedRealtime() - start);
                        break;
                    }
                    i++;
                }
            }
        } catch (SocketException e) {
            isReachable = true;
            HwHiLog.e(TAG, false, "isGateWayReachable:%{public}s", new Object[]{e.getMessage()});
        } catch (IllegalArgumentException e2) {
            isReachable = true;
            HwHiLog.e(TAG, false, "isGateWayReachable:%{public}s", new Object[]{e2.getMessage()});
            if (0 != 0) {
                peer.close();
            }
            Pair<Boolean, Long> pair = new Pair<>(Boolean.valueOf(isReachable), rtt);
            HwHiLog.d(TAG, false, "getGateWayArpResult:%{public}s %{public}d %{public}d", new Object[]{pair.toString(), Integer.valueOf(i), Integer.valueOf(times)});
            return pair;
        } catch (Exception e3) {
            HwHiLog.e(TAG, false, "isGateWayReachable fail", new Object[0]);
            if (0 != 0) {
                peer.close();
            }
            Pair<Boolean, Long> pair2 = new Pair<>(Boolean.valueOf(isReachable), rtt);
            HwHiLog.d(TAG, false, "getGateWayArpResult:%{public}s %{public}d %{public}d", new Object[]{pair2.toString(), Integer.valueOf(i), Integer.valueOf(times)});
            return pair2;
        } catch (Throwable th) {
            if (0 != 0) {
                peer.close();
            }
            throw th;
        }
    }

    public boolean isGateWayReachable(int times, int timeout) {
        return ((Boolean) getGateWayArpResult(times, timeout).first).booleanValue();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        if (r3 != null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0055, code lost:
        if (0 == 0) goto L_0x0058;
     */
    public HWMultiGW getGateWayArpResponses(int times, int timeout) {
        HWMultiGW multiGW = new HWMultiGW();
        HWArpPeer peer = null;
        try {
            peer = constructArpPeer();
            if (peer != null) {
                int i = 0;
                while (true) {
                    if (i < times) {
                        HWMultiGW gw = peer.getGateWayARPResponses(timeout);
                        if (gw != null && gw.getGWNum() > 0) {
                            multiGW = gw;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "getGateWayARPResponses:%{public}s", new Object[]{e.getMessage()});
        } catch (IllegalArgumentException e2) {
            HwHiLog.e(TAG, false, "getGateWayARPResponses:%{public}s", new Object[]{e2.getMessage()});
            if (0 != 0) {
                peer.close();
            }
            HwHiLog.d(TAG, false, "GateWay response num:%{public}d", new Object[]{Integer.valueOf(multiGW.getGWNum())});
            return multiGW;
        } catch (Exception e3) {
            HwHiLog.e(TAG, false, "getGateWayARPResponses fail", new Object[0]);
            if (0 != 0) {
                peer.close();
            }
            HwHiLog.d(TAG, false, "GateWay response num:%{public}d", new Object[]{Integer.valueOf(multiGW.getGWNum())});
            return multiGW;
        } catch (Throwable th) {
            if (0 != 0) {
                peer.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0049, code lost:
        if (0 == 0) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
        if (r2 != null) goto L_0x001d;
     */
    public long getGateWayArpRTT(int timeout) {
        HWMultiGW multiGW;
        HWArpPeer peer = null;
        long arpRTT = -1;
        try {
            peer = constructArpPeer();
            if (!(peer == null || (multiGW = peer.getGateWayARPResponses(timeout)) == null)) {
                arpRTT = multiGW.getArpRTT();
            }
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "getGateWayArpRTT:%{public}s", new Object[]{e.getMessage()});
        } catch (IllegalArgumentException e2) {
            HwHiLog.e(TAG, false, "getGateWayArpRTT:%{public}s", new Object[]{e2.getMessage()});
            if (0 != 0) {
                peer.close();
            }
            HwHiLog.d(TAG, false, "GateWay arp rtt:%{public}s", new Object[]{String.valueOf(arpRTT)});
            return arpRTT;
        } catch (Exception e3) {
            HwHiLog.e(TAG, false, "getGateWayArpRTT fail", new Object[0]);
            if (0 != 0) {
                peer.close();
            }
            HwHiLog.d(TAG, false, "GateWay arp rtt:%{public}s", new Object[]{String.valueOf(arpRTT)});
            return arpRTT;
        } catch (Throwable th) {
            if (0 != 0) {
                peer.close();
            }
            throw th;
        }
    }

    public static String getIpRouteTable() {
        Throwable th;
        boolean z;
        ErrnoException e;
        InterruptedIOException e2;
        SocketException e3;
        int errno = -OsConstants.EPROTO;
        String route = "";
        FileDescriptor fd = null;
        byte[] msg = RtNetlinkMessage.newNewGetRouteMessage();
        try {
            fd = NetlinkSocket.forProto(OsConstants.NETLINK_ROUTE);
            NetlinkSocket.connectToKernel(fd);
            try {
                NetlinkSocket.sendMessage(fd, msg, 0, msg.length, 300);
                int doneMessageCount = 0;
                while (doneMessageCount == 0) {
                    try {
                        ByteBuffer response = NetlinkSocket.recvMessage(fd, 8192, 500);
                        if (response == null) {
                            errno = errno;
                        } else {
                            while (response.remaining() > 0) {
                                NetlinkMessage resmsg = NetlinkMessage.parse(response);
                                if (resmsg != null) {
                                    StructNlMsgHdr hdr = resmsg.getHeader();
                                    if (hdr == null) {
                                        IoUtils.closeQuietly(fd);
                                        return null;
                                    } else if (hdr.nlmsg_type == 3) {
                                        doneMessageCount++;
                                    } else if (hdr.nlmsg_type == 24 || hdr.nlmsg_type == 26) {
                                        route = route + resmsg.toString();
                                    }
                                }
                            }
                            errno = errno;
                        }
                    } catch (ErrnoException e4) {
                        e = e4;
                        z = false;
                        HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e.getMessage()});
                        IoUtils.closeQuietly(fd);
                        Object[] objArr = new Object[1];
                        char c = z ? 1 : 0;
                        char c2 = z ? 1 : 0;
                        char c3 = z ? 1 : 0;
                        objArr[c] = "getIpRouteTable";
                        HwHiLog.e(TAG, z, "fail %{public}s", objArr);
                        return null;
                    } catch (InterruptedIOException e5) {
                        e2 = e5;
                        HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e2.getMessage()});
                        IoUtils.closeQuietly(fd);
                        z = false;
                        Object[] objArr2 = new Object[1];
                        char c4 = z ? 1 : 0;
                        char c22 = z ? 1 : 0;
                        char c32 = z ? 1 : 0;
                        objArr2[c4] = "getIpRouteTable";
                        HwHiLog.e(TAG, z, "fail %{public}s", objArr2);
                        return null;
                    } catch (SocketException e6) {
                        e3 = e6;
                        HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e3.getMessage()});
                        IoUtils.closeQuietly(fd);
                        z = false;
                        Object[] objArr22 = new Object[1];
                        char c42 = z ? 1 : 0;
                        char c222 = z ? 1 : 0;
                        char c322 = z ? 1 : 0;
                        objArr22[c42] = "getIpRouteTable";
                        HwHiLog.e(TAG, z, "fail %{public}s", objArr22);
                        return null;
                    }
                }
                IoUtils.closeQuietly(fd);
                return route;
            } catch (ErrnoException e7) {
                e = e7;
                z = false;
                HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e.getMessage()});
                IoUtils.closeQuietly(fd);
                Object[] objArr222 = new Object[1];
                char c422 = z ? 1 : 0;
                char c2222 = z ? 1 : 0;
                char c3222 = z ? 1 : 0;
                objArr222[c422] = "getIpRouteTable";
                HwHiLog.e(TAG, z, "fail %{public}s", objArr222);
                return null;
            } catch (InterruptedIOException e8) {
                e2 = e8;
                HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e2.getMessage()});
                IoUtils.closeQuietly(fd);
                z = false;
                Object[] objArr2222 = new Object[1];
                char c4222 = z ? 1 : 0;
                char c22222 = z ? 1 : 0;
                char c32222 = z ? 1 : 0;
                objArr2222[c4222] = "getIpRouteTable";
                HwHiLog.e(TAG, z, "fail %{public}s", objArr2222);
                return null;
            } catch (SocketException e9) {
                e3 = e9;
                HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e3.getMessage()});
                IoUtils.closeQuietly(fd);
                z = false;
                Object[] objArr22222 = new Object[1];
                char c42222 = z ? 1 : 0;
                char c222222 = z ? 1 : 0;
                char c322222 = z ? 1 : 0;
                objArr22222[c42222] = "getIpRouteTable";
                HwHiLog.e(TAG, z, "fail %{public}s", objArr22222);
                return null;
            } catch (Throwable th2) {
                th = th2;
                IoUtils.closeQuietly(fd);
                throw th;
            }
        } catch (ErrnoException e10) {
            e = e10;
            z = false;
            HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e.getMessage()});
            IoUtils.closeQuietly(fd);
            Object[] objArr222222 = new Object[1];
            char c422222 = z ? 1 : 0;
            char c2222222 = z ? 1 : 0;
            char c3222222 = z ? 1 : 0;
            objArr222222[c422222] = "getIpRouteTable";
            HwHiLog.e(TAG, z, "fail %{public}s", objArr222222);
            return null;
        } catch (InterruptedIOException e11) {
            e2 = e11;
            HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e2.getMessage()});
            IoUtils.closeQuietly(fd);
            z = false;
            Object[] objArr2222222 = new Object[1];
            char c4222222 = z ? 1 : 0;
            char c22222222 = z ? 1 : 0;
            char c32222222 = z ? 1 : 0;
            objArr2222222[c4222222] = "getIpRouteTable";
            HwHiLog.e(TAG, z, "fail %{public}s", objArr2222222);
            return null;
        } catch (SocketException e12) {
            e3 = e12;
            HwHiLog.e(TAG, false, "Error %{public}s %{public}s", new Object[]{"getIpRouteTable", e3.getMessage()});
            IoUtils.closeQuietly(fd);
            z = false;
            Object[] objArr22222222 = new Object[1];
            char c42222222 = z ? 1 : 0;
            char c222222222 = z ? 1 : 0;
            char c322222222 = z ? 1 : 0;
            objArr22222222[c42222222] = "getIpRouteTable";
            HwHiLog.e(TAG, z, "fail %{public}s", objArr22222222);
            return null;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(fd);
            throw th;
        }
    }

    public static boolean checkWifiDefaultRoute() {
        String wifiRoutes = getIpRouteTable();
        if (wifiRoutes != null) {
            if (!HwNcDftConnManager.isCommercialUser()) {
                HwHiLog.d(TAG, false, "---------default route table -------", new Object[0]);
                HwHiLog.d(TAG, false, "%{private}s", new Object[]{wifiRoutes});
                HwHiLog.d(TAG, false, "------------------------------------", new Object[0]);
            }
            String[] tok = wifiRoutes.toString().split("\n");
            if (tok != null) {
                for (String routeline : tok) {
                    if (routeline.length() > 10 && routeline.startsWith("default") && routeline.indexOf("wlan0") >= 0) {
                        HwHiLog.d(TAG, false, "wifi default route is ok", new Object[0]);
                        return true;
                    }
                }
            }
        }
        HwHiLog.e(TAG, false, "wifi default route not exist!", new Object[0]);
        return false;
    }

    private int getWifiNetworkId() {
        Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
        if (network == null) {
            return -1;
        }
        HwHiLog.e(TAG, false, "getWifiNetworkId, network is null", new Object[0]);
        return network.netId;
    }

    public boolean restoreWifiRoute() {
        ConnectivityManager connectivityManager;
        if (this.mNwService == null || (connectivityManager = this.mConnectivity) == null) {
            return false;
        }
        LinkProperties linkProperties = connectivityManager.getLinkProperties(1);
        if (linkProperties != null) {
            for (RouteInfo r : linkProperties.getRoutes()) {
                if (r.isDefaultRoute()) {
                    try {
                        int networkId = getWifiNetworkId();
                        if (networkId > 0) {
                            this.mNwService.addRoute(networkId, r);
                            this.mNwService.setDefaultNetId(networkId);
                            HwHiLog.d(TAG, false, "addRoute finish", new Object[0]);
                            return true;
                        }
                    } catch (Exception e) {
                        HwHiLog.e(TAG, false, "restoreWifiRoute failed", new Object[0]);
                    }
                }
            }
        }
        HwHiLog.d(TAG, false, "restore Wifi route fail", new Object[0]);
        return false;
    }

    public static String readFileByChars(String fileName) {
        InputStreamReader reader;
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            return "";
        }
        InputStreamReader reader2 = null;
        char[] tempChars = new char[512];
        StringBuilder sb = new StringBuilder();
        try {
            if (Charset.isSupported("UTF-8")) {
                reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            } else if (Charset.isSupported("US-ASCII")) {
                reader = new InputStreamReader(new FileInputStream(fileName), "US-ASCII");
            } else {
                reader = new InputStreamReader(new FileInputStream(fileName), Charset.defaultCharset());
            }
            while (true) {
                int charRead = reader.read(tempChars);
                if (charRead != -1) {
                    sb.append(tempChars, 0, charRead);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                }
            }
            reader.close();
        } catch (IOException e2) {
            HwHiLog.e(TAG, false, "readFileByChars fail", new Object[0]);
            if (0 != 0) {
                reader2.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader2.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return sb.toString();
    }

    public static String writeFile(String fileName, String ctrl) {
        String result = "success";
        File file = new File(fileName);
        if (!file.exists() || !file.canWrite()) {
            HwHiLog.d(TAG, false, "file is exist %{public}s file can write %{public}s", new Object[]{String.valueOf(file.exists()), String.valueOf(file.canWrite())});
            return "";
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(ctrl.getBytes(Charset.defaultCharset()));
            out.flush();
            try {
                out.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            result = "IOException occured";
            HwHiLog.e(TAG, false, "writeFile failed", new Object[0]);
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return result;
    }

    public void pingGateway() {
        DhcpInfo dhcpInfo;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null && (dhcpInfo = wifiManager.getDhcpInfo()) != null && dhcpInfo.gateway != 0) {
            try {
                InetAddress gateway = NetworkUtils.intToInetAddress(dhcpInfo.gateway);
                if (gateway != null) {
                    HwHiLog.d(TAG, false, "pingGateway gateway result: %{public}s", new Object[]{String.valueOf(Inet4Address.getByName(gateway.getHostAddress()).isReachable(1000))});
                }
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "pingGateway fail:%{public}s", new Object[]{e.getMessage()});
            } catch (Exception e2) {
                HwHiLog.e(TAG, false, "pingGateway fail", new Object[0]);
            }
        }
    }

    private void reportArpDetail(String ipaddr, String hwaddr, int flag, String device) {
        ArrayList<ArpItem> arrayList = this.mArpItems;
        if (arrayList != null) {
            arrayList.add(new ArpItem(ipaddr, hwaddr, flag, device));
        }
    }

    public List<ArpItem> readArpFromFile() {
        this.mArpItems = new ArrayList<>();
        nativeReadArpDetail();
        return this.mArpItems;
    }
}
