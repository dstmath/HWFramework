package android.net.dhcp;

import android.net.DhcpResults;
import com.android.internal.util.StateMachine;

public abstract class AbsDhcpClient extends StateMachine {
    protected AbsDhcpClient(String name) {
        super(name);
    }

    public void updateDhcpResultsInfoCache(DhcpResults result) {
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
}
