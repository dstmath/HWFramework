package com.android.server.wifi.scanner;

public class PresetKnownBandsChannelHelper extends KnownBandsChannelHelper {
    public PresetKnownBandsChannelHelper(int[] channels2G, int[] channels5G, int[] channelsDfs) {
        setBandChannels(channels2G, channels5G, channelsDfs);
    }
}
