package com.android.server.wifi.scanner;

import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.util.ArraySet;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import com.android.server.wifi.util.ApConfigUtil;
import com.google.protobuf.nano.Extension;
import java.util.Set;

public abstract class ChannelHelper {
    protected static final ChannelSpec[] NO_CHANNELS = null;
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

        public abstract Set<Integer> getSupplicantScanFreqs();

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.scanner.ChannelHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.scanner.ChannelHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.ChannelHelper.<clinit>():void");
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
            case ApConfigUtil.SUCCESS /*0*/:
                return "unspecified";
            case Extension.TYPE_DOUBLE /*1*/:
                return "24Ghz";
            case Extension.TYPE_FLOAT /*2*/:
                return "5Ghz (no DFS)";
            case Extension.TYPE_INT64 /*3*/:
                return "24Ghz & 5Ghz (no DFS)";
            case Extension.TYPE_UINT64 /*4*/:
                return "5Ghz (DFS only)";
            case Extension.TYPE_FIXED64 /*6*/:
                return "5Ghz (DFS incl)";
            case Extension.TYPE_FIXED32 /*7*/:
                return "24Ghz & 5Ghz (DFS incl)";
            default:
                return "invalid band";
        }
    }
}
