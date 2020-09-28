package com.android.internal.telephony;

import android.app.PendingIntent;
import android.net.Uri;
import java.util.List;

public interface IHwIccSmsInterfaceManagerInner {
    void cdmaBroadcastRangeManagerAddRange(int i, int i2, boolean z);

    boolean cdmaBroadcastRangeManagerFinishUpdate();

    void cdmaBroadcastRangeManagerStartUpdate();

    void cellBroadcastRangeManagerAddRange(int i, int i2, boolean z);

    boolean cellBroadcastRangeManagerFinishUpdate();

    void cellBroadcastRangeManagerStartUpdate();

    void sendMultipartTextAfterAuthInner(String str, String str2, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, String str3, boolean z, int i, boolean z2, int i2);

    boolean setCdmaBroadcastActivationHw(boolean z);

    boolean setCellBroadcastActivationHw(boolean z);

    void smsDispatchersControllerSendText(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z, int i, boolean z2, int i2, boolean z3);
}
