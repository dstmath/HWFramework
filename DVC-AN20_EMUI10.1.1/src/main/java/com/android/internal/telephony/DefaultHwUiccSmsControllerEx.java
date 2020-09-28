package com.android.internal.telephony;

public class DefaultHwUiccSmsControllerEx implements IHwUiccSmsControllerEx {
    private static IHwUiccSmsControllerEx mInstance = new DefaultHwUiccSmsControllerEx();

    public static IHwUiccSmsControllerEx getDefault() {
        return mInstance;
    }
}
