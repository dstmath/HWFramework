package com.android.server.wifi.MSS;

import java.util.List;

public interface IHwMssBlacklistMgr {
    boolean addToBlacklist(HwMssDatabaseItem hwMssDatabaseItem);

    boolean addToBlacklist(String str, String str2, int i);

    void closeDb();

    List<HwMssDatabaseItem> getBlacklist(boolean z);

    boolean isInBlacklist(String str);

    boolean isInBlacklistByBssid(String str);
}
