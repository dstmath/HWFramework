package android.net.dhcp;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpResults;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.LruCache;
import com.android.internal.util.StateMachine;
import java.net.InetAddress;

public class HwDhcpClient extends DhcpClient {
    public static final String ACTION_DHCP_OFFER_INFO = "com.hw.wifipro.action.DHCP_OFFER_INFO";
    public static final String ACTION_INVALID_DHCP_OFFER_RCVD = "com.hw.wifipro.action.INVALID_DHCP_OFFER_RCVD";
    public static final String FLAG_DHCP_OFFER_INFO = "com.hw.wifipro.FLAG_DHCP_OFFER_INFO";
    private static final String HW_WIFI_SELF_CURING = "net.wifi.selfcuring";
    public static final int SCE_STATE_IDLE = 0;
    public static final int SCE_STATE_RECONNECT = 103;
    private static final String TAG = "HwDhcpClient";
    private static LruCache<String, DhcpResultsInfoRecord> mDhcpResultsInfoCache = new LruCache(50);
    private static Object mLock = new Object();
    private static boolean mReadDBDone = false;
    private String mPendingSSID = null;
    private String regularMac = ":[\\w]{1,}:[\\w]{1,}:";

    public HwDhcpClient(Context context, StateMachine controller, String iface) {
        super(context, controller, iface);
    }

    public static DhcpClient makeHwDhcpClient(Context context, StateMachine controller, String intf) {
        DhcpClient client = new HwDhcpClient(context, controller, intf);
        client.start();
        return client;
    }

    public void putPendingSSID(String pendingSSID) {
        this.mPendingSSID = pendingSSID;
    }

    public static void getAllDhcpResultsInfofromDB(Context context) {
        DhcpResultsInfoDBManager dbMgr = DhcpResultsInfoDBManager.getInstance(context);
        synchronized (mLock) {
            mDhcpResultsInfoCache = dbMgr.getAllDhcpResultsInfo();
        }
        mReadDBDone = true;
    }

    public boolean getReadDBDone() {
        return mReadDBDone;
    }

    public DhcpResultsInfoRecord getDhcpResultsInfoRecord() {
        if (this.mPendingSSID == null) {
            return null;
        }
        String pendingSSID = this.mPendingSSID;
        synchronized (mLock) {
            if (mDhcpResultsInfoCache != null) {
                DhcpResultsInfoRecord dhcpResultsInfoRecord = (DhcpResultsInfoRecord) mDhcpResultsInfoCache.get(pendingSSID);
                return dhcpResultsInfoRecord;
            }
            return null;
        }
    }

    public void updateDhcpResultsInfoCache(DhcpResults result) {
        StringBuffer ipstr = new StringBuffer();
        StringBuffer dhcpServerstr = new StringBuffer();
        ipstr.append(result.ipAddress);
        dhcpServerstr.append(result.serverAddress);
        DhcpResultsInfoRecord dhcpResultsInfo = new DhcpResultsInfoRecord(this.mPendingSSID, ipstr.toString(), dhcpServerstr.toString());
        if (this.mPendingSSID != null) {
            String pendingSSID = this.mPendingSSID;
            synchronized (mLock) {
                mDhcpResultsInfoCache.put(pendingSSID, dhcpResultsInfo);
                logd("updateDhcpResultsInfoCache add record for " + pendingSSID.replaceAll(this.regularMac, ":**:**:"));
            }
            return;
        }
        logd("updateDhcpResultsInfoCache error PendingSSID is null");
    }

    public void removeDhcpResultsInfoCache() {
        if (this.mPendingSSID != null) {
            String pendingSSID = this.mPendingSSID;
            synchronized (mLock) {
                mDhcpResultsInfoCache.remove(pendingSSID);
                logd("removeDhcpResultsInfoCache remove record for " + pendingSSID.replaceAll(this.regularMac, ":**:**:"));
            }
            return;
        }
        logd("removeDhcpResultsInfoCache error PendingSSID is null");
    }

    public void sendDhcpOfferPacket(Context context, DhcpPacket dhcpPacket) {
        if (dhcpPacket != null && (dhcpPacket instanceof DhcpOfferPacket)) {
            Intent intent = new Intent(ACTION_DHCP_OFFER_INFO);
            String dhcpResultsStr = dhcpResults2String(dhcpPacket.toDhcpResults());
            if (dhcpResultsStr != null) {
                intent.setFlags(67108864);
                intent.putExtra(FLAG_DHCP_OFFER_INFO, dhcpResultsStr);
                context.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    private String dhcpResults2String(DhcpResults dhcpResults) {
        if (dhcpResults == null || dhcpResults.ipAddress == null || dhcpResults.ipAddress.getAddress() == null || dhcpResults.dnsServers == null) {
            return null;
        }
        StringBuilder lastDhcpResults = new StringBuilder();
        lastDhcpResults.append(String.valueOf(-1)).append("|");
        lastDhcpResults.append(dhcpResults.domains == null ? "" : dhcpResults.domains).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getAddress().getHostAddress()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getPrefixLength()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getFlags()).append("|");
        lastDhcpResults.append(dhcpResults.ipAddress.getScope()).append("|");
        lastDhcpResults.append(dhcpResults.gateway != null ? dhcpResults.gateway.getHostAddress() : "").append("|");
        for (InetAddress dnsServer : dhcpResults.dnsServers) {
            lastDhcpResults.append(dnsServer.getHostAddress()).append("|");
        }
        return lastDhcpResults.toString();
    }

    private boolean isWifiSelfCuring() {
        return String.valueOf(0).equals(SystemProperties.get(HW_WIFI_SELF_CURING, String.valueOf(0))) ^ 1;
    }

    private int getSelfCuringState() {
        return Integer.parseInt(SystemProperties.get(HW_WIFI_SELF_CURING, String.valueOf(0)));
    }

    public void forceRemoveDhcpCache() {
        removeDhcpResultsInfoCache();
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInvalidIpAddr(DhcpResults results) {
        if (!(results == null || results.ipAddress == null || results.ipAddress.getAddress() == null || results.gateway == null || !isWifiSelfCuring() || getSelfCuringState() != 103)) {
            byte[] rcvIpAddr = results.ipAddress.getAddress().getAddress();
            int intCurrAddr3 = rcvIpAddr[3] & 255;
            int netmaskLength = results.ipAddress.getPrefixLength();
            boolean ipEqualsGw = results.ipAddress.getAddress().getHostAddress().equals(results.gateway.getHostAddress());
            boolean invalidIp = rcvIpAddr.length == 4 && (intCurrAddr3 == 0 || intCurrAddr3 == 1 || intCurrAddr3 == 255);
            if (ipEqualsGw || (netmaskLength == 24 && invalidIp)) {
                logd("the rcv dhcp offer has ip addr invalid, addr = " + intCurrAddr3);
                return true;
            }
        }
        return false;
    }

    public void notifyInvalidDhcpOfferRcvd(Context context, DhcpResults offer) {
        if (offer != null) {
            Intent intent = new Intent(ACTION_INVALID_DHCP_OFFER_RCVD);
            String dhcpResultsStr = dhcpResults2String(offer);
            if (dhcpResultsStr != null) {
                intent.setFlags(67108864);
                intent.putExtra(FLAG_DHCP_OFFER_INFO, dhcpResultsStr);
                context.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }
}
