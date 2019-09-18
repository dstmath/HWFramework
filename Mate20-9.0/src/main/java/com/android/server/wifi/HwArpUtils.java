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
import android.util.Log;
import android.util.Pair;
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
            boolean validFlags = (this.flag & 2) == 2;
            boolean validWlanDevice = "wlan0".equals(this.device);
            boolean validMac = this.hwaddr.length() == 17;
            if (!validFlags || !validWlanDevice || !validMac) {
                return false;
            }
            return true;
        }

        public String toString() {
            return String.format(Locale.ENGLISH, "%s %d %s %s", new Object[]{this.ipaddr, Integer.valueOf(this.flag), this.hwaddr, this.device});
        }
    }

    private native int nativeReadArpDetail();

    public HwArpUtils(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            this.mConnectivity = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            this.mNwService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }
    }

    private HWArpPeer constructArpPeer() throws SocketException {
        if (this.mWifiManager == null || this.mConnectivity == null) {
            return null;
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        LinkProperties linkProperties = this.mConnectivity.getLinkProperties(1);
        String linkIFName = linkProperties != null ? linkProperties.getInterfaceName() : "wlan0";
        if (wifiInfo != null) {
            DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
            if (!(dhcpInfo == null || dhcpInfo.gateway == 0)) {
                return new HWArpPeer(linkIFName, NetworkUtils.intToInetAddress(wifiInfo.getIpAddress()), wifiInfo.getMacAddress(), NetworkUtils.intToInetAddress(dhcpInfo.gateway));
            }
        }
        return null;
    }

    public void doGratuitousArp(int timeout) {
        HWArpPeer peer = null;
        try {
            peer = constructArpPeer();
            if (peer != null) {
                byte[] rspMac = peer.doGratuitousArp(timeout);
                if (rspMac != null && rspMac.length == 6) {
                    Log.w(TAG, String.format("%02x:%02x:%02x:%02x:%02x:%02x", new Object[]{Byte.valueOf(rspMac[0]), Byte.valueOf(rspMac[1]), Byte.valueOf(rspMac[2]), Byte.valueOf(rspMac[3]), Byte.valueOf(rspMac[4]), Byte.valueOf(rspMac[5])}) + "alse use My IP(IP conflict detected)");
                }
            }
            if (peer == null) {
                return;
            }
        } catch (SocketException e) {
            Log.e(TAG, "doGratuitousArp:" + e.getMessage());
            if (peer == null) {
                return;
            }
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "doGratuitousArp:" + e2.getMessage());
            if (peer == null) {
                return;
            }
        } catch (Exception e3) {
            Log.e(TAG, "doGratuitousArp:" + e3.getMessage());
            if (peer == null) {
                return;
            }
        } catch (Throwable th) {
            if (peer != null) {
                peer.close();
            }
            throw th;
        }
        peer.close();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
        if (r0 != null) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008d, code lost:
        if (r0 == null) goto L_0x0090;
     */
    public Pair<Boolean, Long> getGateWayArpResult(int times, int timeout) {
        HWArpPeer peer = null;
        boolean isReachable = false;
        long rtt = -1L;
        try {
            peer = constructArpPeer();
            if (peer != null) {
                int i = 0;
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
            Log.e(TAG, "isGateWayReachable:" + e.getMessage());
        } catch (IllegalArgumentException e2) {
            isReachable = true;
            Log.e(TAG, "isGateWayReachable:" + e2.getMessage());
            if (peer != null) {
                peer.close();
            }
            Pair<Boolean, Long> pair = new Pair<>(Boolean.valueOf(isReachable), rtt);
            Log.d(TAG, "getGateWayArpResult:" + pair);
            return pair;
        } catch (Exception e3) {
            Log.e(TAG, "isGateWayReachable:" + e3.getMessage());
            if (peer != null) {
                peer.close();
            }
            Pair<Boolean, Long> pair2 = new Pair<>(Boolean.valueOf(isReachable), rtt);
            Log.d(TAG, "getGateWayArpResult:" + pair2);
            return pair2;
        } catch (Throwable th) {
            if (peer != null) {
                peer.close();
            }
            throw th;
        }
    }

    public boolean isGateWayReachable(int times, int timeout) {
        return ((Boolean) getGateWayArpResult(times, timeout).first).booleanValue();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        if (r1 != null) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0082, code lost:
        if (r1 == null) goto L_0x0085;
     */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0093  */
    public HWMultiGW getGateWayArpResponses(int times, int timeout) {
        HWMultiGW multiGW = new HWMultiGW();
        HWArpPeer peer = null;
        int i = 0;
        try {
            peer = constructArpPeer();
            if (peer != null) {
                int i2 = 0;
                while (true) {
                    if (i2 >= times) {
                        break;
                    }
                    HWMultiGW gw = peer.getGateWayARPResponses(timeout);
                    if (gw != null && gw.getGWNum() > 0) {
                        multiGW = gw;
                        break;
                    }
                    i2++;
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "getGateWayARPResponses:" + e.getMessage());
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "getGateWayARPResponses:" + e2.getMessage());
            if (peer != null) {
                peer.close();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("GateWay response num:");
            if (multiGW != null) {
                i = multiGW.getGWNum();
            }
            sb.append(i);
            Log.d(TAG, sb.toString());
            return multiGW;
        } catch (Exception e3) {
            Log.e(TAG, "getGateWayARPResponses:" + e3.getMessage());
            if (peer != null) {
                peer.close();
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("GateWay response num:");
            if (multiGW != null) {
            }
            sb2.append(i);
            Log.d(TAG, sb2.toString());
            return multiGW;
        } catch (Throwable th) {
            if (peer != null) {
                peer.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0074, code lost:
        if (r0 == null) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0015, code lost:
        if (r0 != null) goto L_0x0017;
     */
    public long getGateWayArpRTT(int timeout) {
        HWArpPeer peer = null;
        long arpRTT = -1;
        try {
            peer = constructArpPeer();
            if (peer != null) {
                HWMultiGW multiGW = peer.getGateWayARPResponses(timeout);
                if (multiGW != null) {
                    arpRTT = multiGW.getArpRTT();
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "getGateWayArpRTT:" + e.getMessage());
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "getGateWayArpRTT:" + e2.getMessage());
            if (peer != null) {
                peer.close();
            }
            Log.d(TAG, "GateWay arp rtt:" + arpRTT);
            return arpRTT;
        } catch (Exception e3) {
            Log.e(TAG, "getGateWayArpRTT:" + e3.getMessage());
            if (peer != null) {
                peer.close();
            }
            Log.d(TAG, "GateWay arp rtt:" + arpRTT);
            return arpRTT;
        } catch (Throwable th) {
            if (peer != null) {
                peer.close();
            }
            throw th;
        }
    }

    public static String getIpRouteTable() {
        int i = -OsConstants.EPROTO;
        String route = "";
        FileDescriptor fd = null;
        byte[] msg = RtNetlinkMessage.newNewGetRouteMessage();
        try {
            fd = NetlinkSocket.forProto(OsConstants.NETLINK_ROUTE);
            NetlinkSocket.connectToKernel(fd);
            byte[] bArr = msg;
            try {
                NetlinkSocket.sendMessage(fd, msg, 0, msg.length, 300);
                int doneMessageCount = 0;
                while (doneMessageCount == 0) {
                    ByteBuffer response = NetlinkSocket.recvMessage(fd, 8192, 500);
                    if (response != null) {
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
                        continue;
                    }
                }
                IoUtils.closeQuietly(fd);
                return route;
            } catch (ErrnoException e) {
                e = e;
                Log.e(TAG, "Error getIpRouteTable", e);
                IoUtils.closeQuietly(fd);
                Log.e(TAG, "fail getIpRouteTable");
                return null;
            } catch (InterruptedIOException e2) {
                e = e2;
                Log.e(TAG, "Error getIpRouteTable", e);
                IoUtils.closeQuietly(fd);
                Log.e(TAG, "fail getIpRouteTable");
                return null;
            } catch (SocketException e3) {
                e = e3;
                Log.e(TAG, "Error getIpRouteTable", e);
                IoUtils.closeQuietly(fd);
                Log.e(TAG, "fail getIpRouteTable");
                return null;
            }
        } catch (ErrnoException e4) {
            e = e4;
            byte[] bArr2 = msg;
            Log.e(TAG, "Error getIpRouteTable", e);
            IoUtils.closeQuietly(fd);
            Log.e(TAG, "fail getIpRouteTable");
            return null;
        } catch (InterruptedIOException e5) {
            e = e5;
            byte[] bArr3 = msg;
            Log.e(TAG, "Error getIpRouteTable", e);
            IoUtils.closeQuietly(fd);
            Log.e(TAG, "fail getIpRouteTable");
            return null;
        } catch (SocketException e6) {
            e = e6;
            byte[] bArr4 = msg;
            Log.e(TAG, "Error getIpRouteTable", e);
            IoUtils.closeQuietly(fd);
            Log.e(TAG, "fail getIpRouteTable");
            return null;
        } catch (Throwable th) {
            th = th;
            IoUtils.closeQuietly(fd);
            throw th;
        }
    }

    public static boolean checkWifiDefaultRoute() {
        String wifiRoutes = getIpRouteTable();
        if (wifiRoutes != null) {
            if (!HwNcDftConnManager.isCommercialUser()) {
                Log.d(TAG, "---------default route table -------");
                Log.d(TAG, wifiRoutes);
                Log.d(TAG, "------------------------------------");
            }
            String[] tok = wifiRoutes.toString().split("\n");
            if (tok != null) {
                int length = tok.length;
                int i = 0;
                while (i < length) {
                    String routeline = tok[i];
                    if (routeline.length() <= 10 || !routeline.startsWith("default") || routeline.indexOf("wlan0") < 0) {
                        i++;
                    } else {
                        Log.d(TAG, "wifi default route is ok");
                        return true;
                    }
                }
            }
        }
        Log.e(TAG, "wifi default route not exist!");
        return false;
    }

    private int getWifiNetworkId() {
        Network network = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
        if (network == null) {
            return -1;
        }
        Log.e(TAG, "getWifiNetworkId, network is null");
        return network.netId;
    }

    public boolean restoreWifiRoute() {
        if (this.mNwService == null || this.mConnectivity == null) {
            return false;
        }
        LinkProperties linkProperties = this.mConnectivity.getLinkProperties(1);
        if (linkProperties != null) {
            for (RouteInfo r : linkProperties.getRoutes()) {
                if (r.isDefaultRoute()) {
                    try {
                        int networkId = getWifiNetworkId();
                        if (networkId > 0) {
                            this.mNwService.addRoute(networkId, r);
                            this.mNwService.setDefaultNetId(networkId);
                            Log.d(TAG, "addRoute finish");
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d(TAG, "restore Wifi route fail");
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
                int read = reader.read(tempChars);
                int charRead = read;
                if (read == -1) {
                    break;
                }
                sb.append(tempChars, 0, charRead);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            if (reader2 != null) {
                reader2.close();
            }
        } catch (Throwable th) {
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e2) {
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
            Log.d(TAG, "file is exists " + file.exists() + " file can write " + file.canWrite());
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
        } catch (IOException ie) {
            result = "IOException occured";
            ie.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        return result;
    }

    public void pingGateway() {
        if (this.mWifiManager != null) {
            DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
            if (dhcpInfo != null && dhcpInfo.gateway != 0) {
                try {
                    InetAddress gateway = NetworkUtils.intToInetAddress(dhcpInfo.gateway);
                    if (gateway != null) {
                        boolean ret = Inet4Address.getByName(gateway.getHostAddress()).isReachable(1000);
                        Log.d(TAG, "pingGateway gateway result: " + ret);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "pingGateway fail:" + e.getMessage());
                } catch (Exception e2) {
                    Log.e(TAG, "pingGateway fail:" + e2.getMessage());
                }
            }
        }
    }

    private void reportArpDetail(String ipaddr, String hwaddr, int flag, String device) {
        if (this.mArpItems != null) {
            this.mArpItems.add(new ArpItem(ipaddr, hwaddr, flag, device));
        }
    }

    public List<ArpItem> readArpFromFile() {
        this.mArpItems = new ArrayList<>();
        nativeReadArpDetail();
        return this.mArpItems;
    }
}
