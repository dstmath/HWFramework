package com.android.server.wifi.hwUtil;

public interface IHwTelphonyUtilsEx {
    String getCdmaGsmImsi();

    int getDefault4GSlotId();

    boolean isCDMASimCard(int i);

    boolean notifyDeviceState(String str, String str2, String str3);
}
