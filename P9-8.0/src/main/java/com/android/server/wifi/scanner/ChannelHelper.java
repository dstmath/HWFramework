package com.android.server.wifi.scanner;

import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.util.ArraySet;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import java.util.Set;

public abstract class ChannelHelper {
    protected static final ChannelSpec[] NO_CHANNELS = new ChannelSpec[0];
    public static final int SCAN_PERIOD_PER_CHANNEL_MS = 200;

    public abstract class ChannelCollection {
        public abstract void addBand(int i);

        public abstract void addChannel(int i);

        public abstract void clear();

        public abstract boolean containsBand(int i);

        public abstract boolean containsChannel(int i);

        public abstract void fillBucketSettings(BucketSettings bucketSettings, int i);

        public abstract Set<Integer> getChannelSet();

        public abstract Set<Integer> getContainingChannelsFromBand(int i);

        public abstract Set<Integer> getMissingChannelsFromBand(int i);

        public abstract Set<Integer> getScanFreqs();

        public abstract boolean isAllChannels();

        public abstract boolean isEmpty();

        public abstract boolean partiallyContainsBand(int i);

        public void addChannels(ScanSettings scanSettings) {
            if (scanSettings.band == 0) {
                for (ChannelSpec channelSpec : scanSettings.channels) {
                    addChannel(channelSpec.frequency);
                }
                return;
            }
            addBand(scanSettings.band);
        }

        public void addChannels(BucketSettings bucketSettings) {
            if (bucketSettings.band == 0) {
                for (ChannelSettings channelSettings : bucketSettings.channels) {
                    addChannel(channelSettings.frequency);
                }
                return;
            }
            addBand(bucketSettings.band);
        }

        public boolean containsSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return containsBand(scanSettings.band);
            }
            for (ChannelSpec channelSpec : scanSettings.channels) {
                if (!containsChannel(channelSpec.frequency)) {
                    return false;
                }
            }
            return true;
        }

        public boolean partiallyContainsSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return partiallyContainsBand(scanSettings.band);
            }
            for (ChannelSpec channelSpec : scanSettings.channels) {
                if (containsChannel(channelSpec.frequency)) {
                    return true;
                }
            }
            return false;
        }

        public Set<Integer> getMissingChannelsFromSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return getMissingChannelsFromBand(scanSettings.band);
            }
            ArraySet<Integer> missingChannels = new ArraySet();
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (!containsChannel(scanSettings.channels[j].frequency)) {
                    missingChannels.add(Integer.valueOf(scanSettings.channels[j].frequency));
                }
            }
            return missingChannels;
        }

        public Set<Integer> getContainingChannelsFromSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return getContainingChannelsFromBand(scanSettings.band);
            }
            ArraySet<Integer> containingChannels = new ArraySet();
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (containsChannel(scanSettings.channels[j].frequency)) {
                    containingChannels.add(Integer.valueOf(scanSettings.channels[j].frequency));
                }
            }
            return containingChannels;
        }
    }

    public abstract ChannelCollection createChannelCollection();

    public abstract int estimateScanDuration(ScanSettings scanSettings);

    public abstract ChannelSpec[] getAvailableScanChannels(int i);

    public abstract boolean settingsContainChannel(ScanSettings scanSettings, int i);

    public void updateChannels() {
    }

    public static String toString(ScanSettings scanSettings) {
        if (scanSettings.band == 0) {
            return toString(scanSettings.channels);
        }
        return toString(scanSettings.band);
    }

    public static String toString(BucketSettings bucketSettings) {
        if (bucketSettings.band == 0) {
            return toString(bucketSettings.channels, bucketSettings.num_channels);
        }
        return toString(bucketSettings.band);
    }

    private static String toString(ChannelSpec[] channels) {
        if (channels == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int c = 0; c < channels.length; c++) {
            sb.append(channels[c].frequency);
            if (c != channels.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toString(ChannelSettings[] channels, int numChannels) {
        if (channels == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int c = 0; c < numChannels; c++) {
            sb.append(channels[c].frequency);
            if (c != numChannels - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toString(int band) {
        switch (band) {
            case 0:
                return "unspecified";
            case 1:
                return "24Ghz";
            case 2:
                return "5Ghz (no DFS)";
            case 3:
                return "24Ghz & 5Ghz (no DFS)";
            case 4:
                return "5Ghz (DFS only)";
            case 6:
                return "5Ghz (DFS incl)";
            case 7:
                return "24Ghz & 5Ghz (DFS incl)";
            default:
                return "invalid band";
        }
    }
}
