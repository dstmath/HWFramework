package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase.DcTrackerBaseReference;
import java.util.HashMap;

public interface HwDataConnectionManager {
    DcTrackerBaseReference createHwDcTrackerBaseReference(AbstractDcTrackerBase abstractDcTrackerBase);

    void createIntelligentDataSwitch(Context context);

    long getThisModemMobileRxPackets(HashMap<String, Integer> hashMap, int i);

    long getThisModemMobileTxPackets(HashMap<String, Integer> hashMap, int i);

    boolean needSetUserDataEnabled(boolean z);
}
