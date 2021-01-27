package android.net.dhcp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;

public class HwArpClient {
    private static final int DEFAULT_FAST_NUM_ARP_PINGS = 1;
    private static final int DEFAULT_FAST_PING_TIMEOUT_MS = 50;
    private static final int DEFAULT_SLOW_NUM_ARP_PINGS = 3;
    private static final int DEFAULT_SLOW_PING_TIMEOUT_MS = 800;
    private static final String TAG = "HwArpClient";
    private static final String WIFI_STA_INTERFACE_NAME = "wlan0";
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext = null;
    private WifiManager mWifiManager = null;

    public HwArpClient(Context context) {
        this.mContext = context;
    }

    public boolean doFastArpTest(Inet4Address requestedAddress) {
        return doArp(1, 50, requestedAddress, false);
    }

    public boolean doSlowArpTest(Inet4Address requestedAddress) {
        return doArp(3, DEFAULT_SLOW_PING_TIMEOUT_MS, requestedAddress, false);
    }

    public boolean doGatewayArpTest(Inet4Address requestedAddress) {
        return doArp(3, DEFAULT_SLOW_PING_TIMEOUT_MS, requestedAddress, true);
    }

    private boolean doArp(int arpNum, int timeout, Inet4Address requestedAddress, boolean isFillSenderIp) {
        HwArpPacket peer = null;
        Log.d(TAG, "doArp() arpnum:" + arpNum + ", timeout:" + timeout + ", isFillSenderIp = " + isFillSenderIp);
        try {
            peer = constructArpPacket();
            for (int i = 0; i < arpNum; i++) {
                if (peer.doArp(timeout, requestedAddress, isFillSenderIp)) {
                    peer.close();
                    return true;
                }
            }
            if (peer == null) {
                return false;
            }
        } catch (SocketException e) {
            Log.e(TAG, "SocketException in arp request");
            if (0 == 0) {
                return false;
            }
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "IllegalArgumentException in arp request");
            if (0 == 0) {
                return false;
            }
        } catch (Exception e3) {
            Log.e(TAG, "Exception in arp request");
            if (0 == 0) {
                return false;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                peer.close();
            }
            throw th;
        }
        peer.close();
        return false;
    }

    private HwArpPacket constructArpPacket() throws SocketException {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        LinkProperties linkProperties = getCurrentLinkProperties();
        String linkIfName = linkProperties != null ? linkProperties.getInterfaceName() : WIFI_STA_INTERFACE_NAME;
        InetAddress linkAddr = null;
        String macAddr = null;
        if (wifiInfo != null) {
            macAddr = wifiInfo.getMacAddress();
            linkAddr = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
        }
        return new HwArpPacket(linkIfName, linkAddr, macAddr);
    }

    private LinkProperties getCurrentLinkProperties() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mConnectivityManager.getLinkProperties(1);
    }
}
