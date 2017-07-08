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
    private ConnectivityManager mCM;
    private Context mContext;
    private WifiManager mWM;

    public HwArpClient(Context context) {
        this.mWM = null;
        this.mCM = null;
        this.mContext = null;
        this.mContext = context;
    }

    public boolean doFastArpTest(Inet4Address requestedAddress) {
        return doArp(DEFAULT_FAST_NUM_ARP_PINGS, DEFAULT_FAST_PING_TIMEOUT_MS, requestedAddress);
    }

    public boolean doSlowArpTest(Inet4Address requestedAddress) {
        return doArp(DEFAULT_SLOW_NUM_ARP_PINGS, DEFAULT_SLOW_PING_TIMEOUT_MS, requestedAddress);
    }

    private boolean doArp(int arpNum, int timeout, Inet4Address requestedAddress) {
        HWArpPacket hWArpPacket = null;
        Log.d(TAG, "doArp() arpnum:" + arpNum + ", timeout:" + timeout);
        try {
            hWArpPacket = constructArpPacket();
            for (int i = 0; i < arpNum; i += DEFAULT_FAST_NUM_ARP_PINGS) {
                if (hWArpPacket.doArp(timeout, requestedAddress)) {
                    if (hWArpPacket != null) {
                        hWArpPacket.close();
                    }
                    return true;
                }
            }
            if (hWArpPacket != null) {
                hWArpPacket.close();
            }
        } catch (SocketException se) {
            Log.e(TAG, "exception in ARP test: " + se);
            if (hWArpPacket != null) {
                hWArpPacket.close();
            }
        } catch (IllegalArgumentException ae) {
            Log.e(TAG, "exception in ARP test:" + ae);
            if (hWArpPacket != null) {
                hWArpPacket.close();
            }
        } catch (Exception e) {
            if (hWArpPacket != null) {
                hWArpPacket.close();
            }
        } catch (Throwable th) {
            if (hWArpPacket != null) {
                hWArpPacket.close();
            }
        }
        return false;
    }

    private HWArpPacket constructArpPacket() throws SocketException {
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiInfo wifiInfo = this.mWM.getConnectionInfo();
        LinkProperties linkProperties = getCurrentLinkProperties();
        String linkIFName = linkProperties != null ? linkProperties.getInterfaceName() : "wlan0";
        String str = null;
        InetAddress linkAddr = null;
        if (wifiInfo != null) {
            str = wifiInfo.getMacAddress();
            linkAddr = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
        }
        return new HWArpPacket(linkIFName, linkAddr, str);
    }

    private LinkProperties getCurrentLinkProperties() {
        if (this.mCM == null) {
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mCM.getLinkProperties(DEFAULT_FAST_NUM_ARP_PINGS);
    }
}
