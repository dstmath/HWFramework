package com.android.server.wifi.scanner;

import android.net.wifi.WifiScanner;
import android.util.ArraySet;
import com.android.server.wifi.WifiNative;
import java.util.Set;

public abstract class ChannelHelper {
    protected static final WifiScanner.ChannelSpec[] NO_CHANNELS = new WifiScanner.ChannelSpec[0];
    public static final int SCAN_PERIOD_PER_CHANNEL_MS = 200;

    public abstract ChannelCollection createChannelCollection();

    public abstract int estimateScanDuration(WifiScanner.ScanSettings scanSettings);

    public abstract WifiScanner.ChannelSpec[] getAvailableScanChannels(int i);

    public abstract boolean settingsContainChannel(WifiScanner.ScanSettings scanSettings, int i);

    public void updateChannels() {
    }

    public abstract class ChannelCollection {
        public abstract void addBand(int i);

        public abstract void addChannel(int i);

        public abstract void clear();

        public abstract boolean containsBand(int i);

        public abstract boolean containsChannel(int i);

        public abstract void fillBucketSettings(WifiNative.BucketSettings bucketSettings, int i);

        public abstract Set<Integer> getChannelSet();

        public abstract Set<Integer> getContainingChannelsFromBand(int i);

        public abstract Set<Integer> getMissingChannelsFromBand(int i);

        public abstract Set<Integer> getScanFreqs();

        public abstract boolean isAllChannels();

        public abstract boolean isEmpty();

        public abstract boolean partiallyContainsBand(int i);

        public ChannelCollection() {
        }

        public void addChannels(WifiScanner.ScanSettings scanSettings) {
            if (scanSettings.band == 0) {
                for (int j = 0; j < scanSettings.channels.length; j++) {
                    addChannel(scanSettings.channels[j].frequency);
                }
                return;
            }
            addBand(scanSettings.band);
        }

        public void addChannels(WifiNative.BucketSettings bucketSettings) {
            if (bucketSettings.band == 0) {
                for (int j = 0; j < bucketSettings.channels.length; j++) {
                    addChannel(bucketSettings.channels[j].frequency);
                }
                return;
            }
            addBand(bucketSettings.band);
        }

        public boolean containsSettings(WifiScanner.ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return containsBand(scanSettings.band);
            }
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (!containsChannel(scanSettings.channels[j].frequency)) {
                    return false;
                }
            }
            return true;
        }

        public boolean partiallyContainsSettings(WifiScanner.ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return partiallyContainsBand(scanSettings.band);
            }
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (containsChannel(scanSettings.channels[j].frequency)) {
                    return true;
                }
            }
            return false;
        }

        public Set<Integer> getMissingChannelsFromSettings(WifiScanner.ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return getMissingChannelsFromBand(scanSettings.band);
            }
            ArraySet<Integer> missingChannels = new ArraySet<>();
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (!containsChannel(scanSettings.channels[j].frequency)) {
                    missingChannels.add(Integer.valueOf(scanSettings.channels[j].frequency));
                }
            }
            return missingChannels;
        }

        public Set<Integer> getContainingChannelsFromSettings(WifiScanner.ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return getContainingChannelsFromBand(scanSettings.band);
            }
            ArraySet<Integer> containingChannels = new ArraySet<>();
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (containsChannel(scanSettings.channels[j].frequency)) {
                    containingChannels.add(Integer.valueOf(scanSettings.channels[j].frequency));
                }
            }
            return containingChannels;
        }
    }

    public static String toString(WifiScanner.ScanSettings scanSettings) {
        if (scanSettings.band == 0) {
            return toString(scanSettings.channels);
        }
        return bandToString(scanSettings.band);
    }

    public static String toString(WifiNative.BucketSettings bucketSettings) {
        if (bucketSettings.band == 0) {
            return toString(bucketSettings.channels, bucketSettings.num_channels);
        }
        return bandToString(bucketSettings.band);
    }

    private static String toString(WifiScanner.ChannelSpec[] channels) {
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

    private static String toString(WifiNative.ChannelSettings[] channels, int numChannels) {
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

    public static String bandToString(int band) {
        if (band == 0) {
            return "unspecified";
        }
        if (band == 1) {
            return "24Ghz";
        }
        if (band == 2) {
            return "5Ghz (no DFS)";
        }
        if (band == 3) {
            return "24Ghz & 5Ghz (no DFS)";
        }
        if (band == 4) {
            return "5Ghz (DFS only)";
        }
        if (band == 6) {
            return "5Ghz (DFS incl)";
        }
        if (band != 7) {
            return "invalid band";
        }
        return "24Ghz & 5Ghz (DFS incl)";
    }
}
