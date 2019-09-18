package com.android.server.wifi.MSS;

import java.util.List;

public interface IHwMSSBlacklistMgr {
    boolean addToBlacklist(HwMSSDatabaseItem hwMSSDatabaseItem);

    boolean addToBlacklist(String str, String str2, int i);

    void closeDB();

    List<HwMSSDatabaseItem> getBlacklist(boolean z);

    boolean isInBlacklist(String str);

    boolean isInBlacklistByBssid(String str);
}
