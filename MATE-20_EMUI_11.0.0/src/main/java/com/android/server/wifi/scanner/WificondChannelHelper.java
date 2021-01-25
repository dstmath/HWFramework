package com.android.server.wifi.scanner;

import android.util.Log;
import com.android.server.wifi.WifiNative;

public class WificondChannelHelper extends KnownBandsChannelHelper {
    private static final String TAG = "WificondChannelHelper";
    private final WifiNative mWifiNative;

    public WificondChannelHelper(WifiNative wifiNative) {
        this.mWifiNative = wifiNative;
        int[] emptyFreqList = new int[0];
        setBandChannels(emptyFreqList, emptyFreqList, emptyFreqList);
        updateChannels();
    }

    @Override // com.android.server.wifi.scanner.ChannelHelper
    public void updateChannels() {
        int[] channels24G = this.mWifiNative.getChannelsForBand(1);
        if (channels24G == null) {
            Log.e(TAG, "Failed to get channels for 2.4GHz band");
        }
        int[] channels5G = this.mWifiNative.getChannelsForBand(2);
        if (channels5G == null) {
            Log.e(TAG, "Failed to get channels for 5GHz band");
        }
        int[] channelsDfs = this.mWifiNative.getChannelsForBand(4);
        if (channelsDfs == null) {
            Log.e(TAG, "Failed to get channels for 5GHz DFS only band");
        }
        if (channels24G == null || channels5G == null || channelsDfs == null) {
            Log.e(TAG, "Failed to get all channels for band, not updating band channel lists");
        } else if (channels24G.length > 0 || channels5G.length > 0 || channelsDfs.length > 0) {
            setBandChannels(channels24G, channels5G, channelsDfs);
        } else {
            Log.e(TAG, "Got zero length for all channel lists");
        }
    }
}
