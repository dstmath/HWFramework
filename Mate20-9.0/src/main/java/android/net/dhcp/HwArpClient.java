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
    private ConnectivityManager mCM = null;
    private Context mContext = null;
    private WifiManager mWM = null;

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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0041, code lost:
        if (r0 != null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004a, code lost:
        if (r0 == null) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007e, code lost:
        if (r0 == null) goto L_0x0081;
     */
    private boolean doArp(int arpNum, int timeout, Inet4Address requestedAddress, boolean fillSenderIp) {
        HWArpPacket peer = null;
        Log.d(TAG, "doArp() arpnum:" + arpNum + ", timeout:" + timeout + ", fillSenderIp = " + fillSenderIp);
        try {
            peer = constructArpPacket();
            for (int i = 0; i < arpNum; i++) {
                if (peer.doArp(timeout, requestedAddress, fillSenderIp)) {
                    if (peer != null) {
                        peer.close();
                    }
                    return true;
                }
            }
        } catch (SocketException se) {
            Log.e(TAG, "exception in ARP test: " + se);
        } catch (IllegalArgumentException ae) {
            Log.e(TAG, "exception in ARP test:" + ae);
            if (peer != null) {
                peer.close();
            }
            return false;
        } catch (Exception e) {
        } catch (Throwable th) {
            if (peer != null) {
                peer.close();
            }
            throw th;
        }
    }

    private HWArpPacket constructArpPacket() throws SocketException {
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiInfo wifiInfo = this.mWM.getConnectionInfo();
        LinkProperties linkProperties = getCurrentLinkProperties();
        String linkIFName = linkProperties != null ? linkProperties.getInterfaceName() : "wlan0";
        InetAddress linkAddr = null;
        String macAddr = null;
        if (wifiInfo != null) {
            macAddr = wifiInfo.getMacAddress();
            linkAddr = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
        }
        return new HWArpPacket(linkIFName, linkAddr, macAddr);
    }

    private LinkProperties getCurrentLinkProperties() {
        if (this.mCM == null) {
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mCM.getLinkProperties(1);
    }
}
