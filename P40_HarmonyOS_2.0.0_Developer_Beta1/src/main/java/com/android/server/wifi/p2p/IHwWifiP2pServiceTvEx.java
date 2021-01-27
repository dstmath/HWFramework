package com.android.server.wifi.p2p;

import android.app.AlertDialog;
import android.net.wifi.p2p.WifiP2pConfig;
import android.os.Message;

public interface IHwWifiP2pServiceTvEx {
    void dismissP2pDisallowUntrustInviteDialog();

    void dismissP2pInviteDialog();

    int[] getP2pExtListenTime(boolean z);

    int getP2pGroupCreatingWaitTime();

    boolean handleGroupCreatedStateMessage(Message message);

    boolean handleGroupCreatingStateMessage(Message message);

    boolean handleP2pEnabledStateMessage(Message message);

    void handleP2pUserAuthorizingJoinStateEnter();

    boolean handleP2pUserAuthorizingJoinStateMessage(Message message);

    void notifyP2pInvitationReceived(int i, WifiP2pConfig wifiP2pConfig, String str);

    void setP2pInviteDialog(AlertDialog alertDialog);
}
