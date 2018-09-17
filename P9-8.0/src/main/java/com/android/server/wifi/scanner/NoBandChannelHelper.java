package com.android.server.wifi.scanner;

import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.util.ArraySet;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;
import java.util.Set;

public class NoBandChannelHelper extends ChannelHelper {
    private static final int ALL_BAND_CHANNEL_COUNT_ESTIMATE = 36;

    public class NoBandChannelCollection extends ChannelCollection {
        private boolean mAllChannels = false;
        private final ArraySet<Integer> mChannels = new ArraySet();

        public NoBandChannelCollection() {
            super();
        }

        public void addChannel(int frequency) {
            this.mChannels.add(Integer.valueOf(frequency));
        }

        public void addBand(int band) {
            if (band != 0) {
                this.mAllChannels = true;
            }
        }

        public boolean containsChannel(int channel) {
            return !this.mAllChannels ? this.mChannels.contains(Integer.valueOf(channel)) : true;
        }

        public boolean containsBand(int band) {
            if (band != 0) {
                return this.mAllChannels;
            }
            return false;
        }

        public boolean partiallyContainsBand(int band) {
            return false;
        }

        public boolean isEmpty() {
            return !this.mAllChannels ? this.mChannels.isEmpty() : false;
        }

        public boolean isAllChannels() {
            return this.mAllChannels;
        }

        public void clear() {
            this.mAllChannels = false;
            this.mChannels.clear();
        }

        public Set<Integer> getMissingChannelsFromBand(int band) {
            return new ArraySet();
        }

        public Set<Integer> getContainingChannelsFromBand(int band) {
            return new ArraySet();
        }

        public Set<Integer> getChannelSet() {
            if (isEmpty() || (this.mAllChannels ^ 1) == 0) {
                return new ArraySet();
            }
            return this.mChannels;
        }

        public void fillBucketSettings(BucketSettings bucketSettings, int maxChannels) {
            if (this.mAllChannels || this.mChannels.size() > maxChannels) {
                bucketSettings.band = 7;
                bucketSettings.num_channels = 0;
                bucketSettings.channels = null;
                return;
            }
            bucketSettings.band = 0;
            bucketSettings.num_channels = this.mChannels.size();
            bucketSettings.channels = new ChannelSettings[this.mChannels.size()];
            for (int i = 0; i < this.mChannels.size(); i++) {
                ChannelSettings channelSettings = new ChannelSettings();
                channelSettings.frequency = ((Integer) this.mChannels.valueAt(i)).intValue();
                bucketSettings.channels[i] = channelSettings;
            }
        }

        public Set<Integer> getScanFreqs() {
            if (this.mAllChannels) {
                return null;
            }
            return new ArraySet(this.mChannels);
        }
    }

    public boolean settingsContainChannel(ScanSettings settings, int channel) {
        if (settings.band != 0) {
            return true;
        }
        for (ChannelSpec channelSpec : settings.channels) {
            if (channelSpec.frequency == channel) {
                return true;
            }
        }
        return false;
    }

    public ChannelSpec[] getAvailableScanChannels(int band) {
        return NO_CHANNELS;
    }

    public int estimateScanDuration(ScanSettings settings) {
        if (settings.band == 0) {
            return settings.channels.length * 200;
        }
        return 7200;
    }

    public ChannelCollection createChannelCollection() {
        return new NoBandChannelCollection();
    }
}
