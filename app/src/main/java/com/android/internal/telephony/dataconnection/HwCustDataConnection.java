package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import com.android.internal.telephony.Phone;

public class HwCustDataConnection {
    public boolean setMtuIfNeeded(LinkProperties lp, Phone phone) {
        return false;
    }

    public boolean whetherSetApnByCust(Phone phone) {
        return false;
    }
}
