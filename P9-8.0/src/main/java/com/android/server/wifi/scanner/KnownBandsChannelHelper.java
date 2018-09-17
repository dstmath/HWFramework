package com.android.server.wifi.scanner;

import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.util.ArraySet;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import com.android.server.wifi.scanner.ChannelHelper.ChannelCollection;
import java.util.Set;

public class KnownBandsChannelHelper extends ChannelHelper {
    private ChannelSpec[][] mBandsToChannels;

    public class KnownBandsChannelCollection extends ChannelCollection {
        private int mAllBands = 0;
        private final ArraySet<Integer> mChannels = new ArraySet();
        private int mExactBands = 0;

        public KnownBandsChannelCollection() {
            super();
        }

        public void addChannel(int frequency) {
            this.mChannels.add(Integer.valueOf(frequency));
            this.mAllBands |= KnownBandsChannelHelper.this.getBandFromChannel(frequency);
        }

        public void addBand(int band) {
            this.mExactBands |= band;
            this.mAllBands |= band;
            ChannelSpec[] bandChannels = KnownBandsChannelHelper.this.getAvailableScanChannels(band);
            for (ChannelSpec channelSpec : bandChannels) {
                this.mChannels.add(Integer.valueOf(channelSpec.frequency));
            }
        }

        public boolean containsChannel(int channel) {
            return this.mChannels.contains(Integer.valueOf(channel));
        }

        public boolean containsBand(int band) {
            ChannelSpec[] bandChannels = KnownBandsChannelHelper.this.getAvailableScanChannels(band);
            for (ChannelSpec channelSpec : bandChannels) {
                if (!this.mChannels.contains(Integer.valueOf(channelSpec.frequency))) {
                    return false;
                }
            }
            return true;
        }

        public boolean partiallyContainsBand(int band) {
            ChannelSpec[] bandChannels = KnownBandsChannelHelper.this.getAvailableScanChannels(band);
            for (ChannelSpec channelSpec : bandChannels) {
                if (this.mChannels.contains(Integer.valueOf(channelSpec.frequency))) {
                    return true;
                }
            }
            return false;
        }

        public boolean isEmpty() {
            return this.mChannels.isEmpty();
        }

        public boolean isAllChannels() {
            return KnownBandsChannelHelper.this.getAvailableScanChannels(7).length == this.mChannels.size();
        }

        public void clear() {
            this.mAllBands = 0;
            this.mExactBands = 0;
            this.mChannels.clear();
        }

        public Set<Integer> getMissingChannelsFromBand(int band) {
            ArraySet<Integer> missingChannels = new ArraySet();
            ChannelSpec[] bandChannels = KnownBandsChannelHelper.this.getAvailableScanChannels(band);
            for (int i = 0; i < bandChannels.length; i++) {
                if (!this.mChannels.contains(Integer.valueOf(bandChannels[i].frequency))) {
                    missingChannels.add(Integer.valueOf(bandChannels[i].frequency));
                }
            }
            return missingChannels;
        }

        public Set<Integer> getContainingChannelsFromBand(int band) {
            ArraySet<Integer> containingChannels = new ArraySet();
            ChannelSpec[] bandChannels = KnownBandsChannelHelper.this.getAvailableScanChannels(band);
            for (int i = 0; i < bandChannels.length; i++) {
                if (this.mChannels.contains(Integer.valueOf(bandChannels[i].frequency))) {
                    containingChannels.add(Integer.valueOf(bandChannels[i].frequency));
                }
            }
            return containingChannels;
        }

        public Set<Integer> getChannelSet() {
            if (isEmpty() || this.mAllBands == this.mExactBands) {
                return new ArraySet();
            }
            return this.mChannels;
        }

        public void fillBucketSettings(BucketSettings bucketSettings, int maxChannels) {
            if ((this.mChannels.size() > maxChannels || this.mAllBands == this.mExactBands) && this.mAllBands != 0) {
                bucketSettings.band = this.mAllBands;
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
            if (this.mExactBands == 7) {
                return null;
            }
            return new ArraySet(this.mChannels);
        }

        public Set<Integer> getAllChannels() {
            return new ArraySet(this.mChannels);
        }
    }

    protected void setBandChannels(int[] channels2G, int[] channels5G, int[] channelsDfs) {
        this.mBandsToChannels = new ChannelSpec[8][];
        this.mBandsToChannels[0] = NO_CHANNELS;
        this.mBandsToChannels[1] = new ChannelSpec[channels2G.length];
        copyChannels(this.mBandsToChannels[1], 0, channels2G);
        this.mBandsToChannels[2] = new ChannelSpec[channels5G.length];
        copyChannels(this.mBandsToChannels[2], 0, channels5G);
        this.mBandsToChannels[3] = new ChannelSpec[(channels2G.length + channels5G.length)];
        copyChannels(this.mBandsToChannels[3], 0, channels2G);
        copyChannels(this.mBandsToChannels[3], channels2G.length, channels5G);
        this.mBandsToChannels[4] = new ChannelSpec[channelsDfs.length];
        copyChannels(this.mBandsToChannels[4], 0, channelsDfs);
        this.mBandsToChannels[5] = new ChannelSpec[(channels2G.length + channelsDfs.length)];
        copyChannels(this.mBandsToChannels[5], 0, channels2G);
        copyChannels(this.mBandsToChannels[5], channels2G.length, channelsDfs);
        this.mBandsToChannels[6] = new ChannelSpec[(channels5G.length + channelsDfs.length)];
        copyChannels(this.mBandsToChannels[6], 0, channels5G);
        copyChannels(this.mBandsToChannels[6], channels5G.length, channelsDfs);
        this.mBandsToChannels[7] = new ChannelSpec[((channels2G.length + channels5G.length) + channelsDfs.length)];
        copyChannels(this.mBandsToChannels[7], 0, channels2G);
        copyChannels(this.mBandsToChannels[7], channels2G.length, channels5G);
        copyChannels(this.mBandsToChannels[7], channels2G.length + channels5G.length, channelsDfs);
    }

    private static void copyChannels(ChannelSpec[] channelSpec, int offset, int[] channels) {
        for (int i = 0; i < channels.length; i++) {
            channelSpec[offset + i] = new ChannelSpec(channels[i]);
        }
    }

    public ChannelSpec[] getAvailableScanChannels(int band) {
        if (band < 1 || band > 7) {
            return NO_CHANNELS;
        }
        return this.mBandsToChannels[band];
    }

    public int estimateScanDuration(ScanSettings settings) {
        if (settings.band == 0) {
            return settings.channels.length * 200;
        }
        return getAvailableScanChannels(settings.band).length * 200;
    }

    private boolean isDfsChannel(int frequency) {
        for (ChannelSpec dfsChannel : this.mBandsToChannels[4]) {
            if (frequency == dfsChannel.frequency) {
                return true;
            }
        }
        return false;
    }

    private int getBandFromChannel(int frequency) {
        if (2400 <= frequency && frequency < 2500) {
            return 1;
        }
        if (isDfsChannel(frequency)) {
            return 4;
        }
        if (5100 > frequency || frequency >= 6000) {
            return 0;
        }
        return 2;
    }

    public boolean settingsContainChannel(ScanSettings settings, int channel) {
        ChannelSpec[] settingsChannels;
        if (settings.band == 0) {
            settingsChannels = settings.channels;
        } else {
            settingsChannels = getAvailableScanChannels(settings.band);
        }
        for (ChannelSpec channelSpec : settingsChannels) {
            if (channelSpec.frequency == channel) {
                return true;
            }
        }
        return false;
    }

    public KnownBandsChannelCollection createChannelCollection() {
        return new KnownBandsChannelCollection();
    }
}
