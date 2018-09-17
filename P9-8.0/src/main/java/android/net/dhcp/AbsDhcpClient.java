package android.net.dhcp;

import android.content.Context;
import android.net.DhcpResults;
import com.android.internal.util.StateMachine;

public abstract class AbsDhcpClient extends StateMachine {
    protected AbsDhcpClient(String name) {
        super(name);
    }

    public void updateDhcpResultsInfoCache(DhcpResults result) {
    }

    public void putPendingSSID(String pendingSSID) {
    }

    public void removeDhcpResultsInfoCache() {
    }

    public void saveDhcpResultsInfotoDB() {
    }

    public boolean getReadDBDone() {
        return false;
    }

    public DhcpResultsInfoRecord getDhcpResultsInfoRecord() {
        return null;
    }

    public void sendDhcpOfferPacket(Context context, DhcpPacket dhcpPacket) {
    }

    public boolean isInvalidIpAddr(DhcpResults results) {
        return false;
    }

    public void forceRemoveDhcpCache() {
    }

    public void notifyInvalidDhcpOfferRcvd(Context context, DhcpResults offer) {
    }
}
