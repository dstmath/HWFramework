package com.android.server.hidata.mplink;

public interface IMpLinkCallback {
    void onBindProcessToNetworkResult(MplinkBindResultInfo mplinkBindResultInfo);

    void onWiFiAndCellCoexistResult(MplinkNetworkResultInfo mplinkNetworkResultInfo);
}
