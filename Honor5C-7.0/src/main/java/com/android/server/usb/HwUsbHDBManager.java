package com.android.server.usb;

import com.android.internal.util.IndentingPrintWriter;

public interface HwUsbHDBManager {
    void allowUsbHDB(boolean z, String str);

    void clearUsbHDBKeys();

    void denyUsbHDB();

    void dump(IndentingPrintWriter indentingPrintWriter);

    void setHdbEnabled(boolean z);
}
