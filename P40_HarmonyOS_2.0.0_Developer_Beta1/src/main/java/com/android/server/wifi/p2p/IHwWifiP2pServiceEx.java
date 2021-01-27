package com.android.server.wifi.p2p;

public interface IHwWifiP2pServiceEx {
    boolean isRemoveGroupAllowed(int i, String str);

    void updateGroupCreatedPkgList(int i, String str, boolean z, boolean z2);
}
