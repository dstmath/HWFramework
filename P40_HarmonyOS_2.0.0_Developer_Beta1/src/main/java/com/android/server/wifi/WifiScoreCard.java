package com.android.server.wifi;

import android.net.MacAddress;
import android.net.wifi.SupplicantState;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiScoreCard;
import com.android.server.wifi.WifiScoreCardProto;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.NativeUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class WifiScoreCard {
    private static final boolean DBG = false;
    public static final String DUMP_ARG = "WifiScoreCard";
    private static final long SUCCESS_MILLIS_SINCE_ROAM = 4000;
    private static final String TAG = "WifiScoreCard";
    private static final long TS_NONE = -1;
    private final Map<MacAddress, PerBssid> mApForBssid = new ArrayMap();
    private boolean mAttemptingSwitch = false;
    private final Clock mClock;
    private final PerBssid mDummyPerBssid;
    private final String mL2KeySeed;
    private MemoryStore mMemoryStore;
    private boolean mPolled = false;
    private long mTsConnectionAttemptStart = -1;
    private long mTsRoam = -1;
    private boolean mValidated = false;

    public interface BlobListener {
        void onBlobRetrieved(byte[] bArr);
    }

    public interface MemoryStore {
        void read(String str, BlobListener blobListener);

        void write(String str, byte[] bArr);
    }

    public void installMemoryStore(MemoryStore memoryStore) {
        Preconditions.checkNotNull(memoryStore);
        if (this.mMemoryStore == null) {
            this.mMemoryStore = memoryStore;
            Log.i("WifiScoreCard", "Installing MemoryStore");
            requestReadForAllChanged();
            return;
        }
        this.mMemoryStore = memoryStore;
        Log.e("WifiScoreCard", "Reinstalling MemoryStore");
    }

    public WifiScoreCard(Clock clock, String l2KeySeed) {
        this.mClock = clock;
        this.mL2KeySeed = l2KeySeed;
        this.mDummyPerBssid = new PerBssid("", MacAddress.fromString("02:00:00:00:00:00"));
    }

    public Pair<String, String> getL2KeyAndGroupHint(ExtendedWifiInfo wifiInfo) {
        PerBssid perBssid = lookupBssid(wifiInfo.getSSID(), wifiInfo.getBSSID());
        if (perBssid == this.mDummyPerBssid) {
            return new Pair<>(null, null);
        }
        return new Pair<>(perBssid.l2Key, groupHintFromLong(computeHashLong(perBssid.ssid, this.mDummyPerBssid.bssid)));
    }

    public void resetConnectionState() {
        resetConnectionStateInternal(true);
    }

    private void resetConnectionStateInternal(boolean calledFromResetConnectionState) {
        if (!calledFromResetConnectionState) {
            this.mAttemptingSwitch = false;
        }
        if (!this.mAttemptingSwitch) {
            this.mTsConnectionAttemptStart = -1;
        }
        this.mTsRoam = -1;
        this.mPolled = false;
        this.mValidated = false;
    }

    private void update(WifiScoreCardProto.Event event, ExtendedWifiInfo wifiInfo) {
        PerBssid perBssid = lookupBssid(wifiInfo.getSSID(), wifiInfo.getBSSID());
        perBssid.updateEventStats(event, wifiInfo.getFrequency(), wifiInfo.getRssi(), wifiInfo.getLinkSpeed());
        perBssid.setNetworkConfigId(wifiInfo.getNetworkId());
    }

    public void noteSignalPoll(ExtendedWifiInfo wifiInfo) {
        if (!this.mPolled && wifiInfo.getRssi() != -127) {
            update(WifiScoreCardProto.Event.FIRST_POLL_AFTER_CONNECTION, wifiInfo);
            this.mPolled = true;
        }
        update(WifiScoreCardProto.Event.SIGNAL_POLL, wifiInfo);
        if (this.mTsRoam > -1 && wifiInfo.getRssi() != -127 && this.mClock.getElapsedSinceBootMillis() - this.mTsRoam >= SUCCESS_MILLIS_SINCE_ROAM) {
            update(WifiScoreCardProto.Event.ROAM_SUCCESS, wifiInfo);
            this.mTsRoam = -1;
            doWrites();
        }
    }

    public void noteIpConfiguration(ExtendedWifiInfo wifiInfo) {
        update(WifiScoreCardProto.Event.IP_CONFIGURATION_SUCCESS, wifiInfo);
        this.mAttemptingSwitch = false;
        doWrites();
    }

    public void noteValidationSuccess(ExtendedWifiInfo wifiInfo) {
        if (!this.mValidated) {
            update(WifiScoreCardProto.Event.VALIDATION_SUCCESS, wifiInfo);
            this.mValidated = true;
        }
    }

    public void noteConnectionAttempt(ExtendedWifiInfo wifiInfo) {
        if (this.mTsConnectionAttemptStart > -1) {
            if (this.mPolled) {
                update(WifiScoreCardProto.Event.LAST_POLL_BEFORE_SWITCH, wifiInfo);
            }
            this.mAttemptingSwitch = true;
        }
        this.mTsConnectionAttemptStart = this.mClock.getElapsedSinceBootMillis();
        this.mPolled = false;
    }

    public void noteNetworkAgentCreated(ExtendedWifiInfo wifiInfo, int networkAgentId) {
        lookupBssid(wifiInfo.getSSID(), wifiInfo.getBSSID()).mNetworkAgentId = networkAgentId;
    }

    public void noteConnectionFailure(ExtendedWifiInfo wifiInfo, int codeMetrics, int codeMetricsProto) {
        update(WifiScoreCardProto.Event.CONNECTION_FAILURE, wifiInfo);
        resetConnectionStateInternal(false);
    }

    public void noteIpReachabilityLost(ExtendedWifiInfo wifiInfo) {
        update(WifiScoreCardProto.Event.IP_REACHABILITY_LOST, wifiInfo);
        long j = this.mTsRoam;
        if (j > -1) {
            this.mTsConnectionAttemptStart = j;
            update(WifiScoreCardProto.Event.ROAM_FAILURE, wifiInfo);
        }
        resetConnectionStateInternal(false);
        doWrites();
    }

    public void noteRoam(ExtendedWifiInfo wifiInfo) {
        update(WifiScoreCardProto.Event.LAST_POLL_BEFORE_ROAM, wifiInfo);
        this.mTsRoam = this.mClock.getElapsedSinceBootMillis();
    }

    public void noteSupplicantStateChanging(ExtendedWifiInfo wifiInfo, SupplicantState state) {
    }

    public void noteSupplicantStateChanged(ExtendedWifiInfo wifiInfo) {
    }

    public void noteWifiDisabled(ExtendedWifiInfo wifiInfo) {
        update(WifiScoreCardProto.Event.WIFI_DISABLED, wifiInfo);
        resetConnectionStateInternal(false);
        doWrites();
    }

    /* access modifiers changed from: package-private */
    public final class PerBssid {
        public final MacAddress bssid;
        public boolean changed;
        public int id;
        public final String l2Key;
        private int mNetworkAgentId = WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK;
        private int mNetworkConfigId = WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK;
        private final AtomicReference<byte[]> mPendingReadFromStore = new AtomicReference<>();
        private WifiScoreCardProto.SecurityType mSecurityType = null;
        private final Map<Pair<WifiScoreCardProto.Event, Integer>, PerSignal> mSignalForEventAndFrequency = new ArrayMap();
        public final String ssid;

        PerBssid(String ssid2, MacAddress bssid2) {
            this.ssid = ssid2;
            this.bssid = bssid2;
            long hash = WifiScoreCard.this.computeHashLong(ssid2, bssid2);
            this.l2Key = WifiScoreCard.l2KeyFromLong(hash);
            this.id = WifiScoreCard.idFromLong(hash);
            this.changed = false;
        }

        /* access modifiers changed from: package-private */
        public void updateEventStats(WifiScoreCardProto.Event event, int frequency, int rssi, int linkspeed) {
            PerSignal perSignal = lookupSignal(event, frequency);
            if (rssi != -127) {
                perSignal.rssi.update((double) rssi);
            }
            if (linkspeed > 0) {
                perSignal.linkspeed.update((double) linkspeed);
            }
            if (perSignal.elapsedMs != null && WifiScoreCard.this.mTsConnectionAttemptStart > -1) {
                long millis = WifiScoreCard.this.mClock.getElapsedSinceBootMillis() - WifiScoreCard.this.mTsConnectionAttemptStart;
                if (millis >= 0) {
                    perSignal.elapsedMs.update((double) millis);
                }
            }
            this.changed = true;
        }

        /* access modifiers changed from: package-private */
        public PerSignal lookupSignal(WifiScoreCardProto.Event event, int frequency) {
            finishPendingRead();
            Pair<WifiScoreCardProto.Event, Integer> key = new Pair<>(event, Integer.valueOf(frequency));
            PerSignal ans = this.mSignalForEventAndFrequency.get(key);
            if (ans != null) {
                return ans;
            }
            PerSignal ans2 = new PerSignal(event, frequency);
            this.mSignalForEventAndFrequency.put(key, ans2);
            return ans2;
        }

        /* access modifiers changed from: package-private */
        public WifiScoreCardProto.SecurityType getSecurityType() {
            finishPendingRead();
            return this.mSecurityType;
        }

        /* access modifiers changed from: package-private */
        public void setSecurityType(WifiScoreCardProto.SecurityType securityType) {
            finishPendingRead();
            if (!Objects.equals(securityType, this.mSecurityType)) {
                this.mSecurityType = securityType;
                this.changed = true;
            }
        }

        /* access modifiers changed from: package-private */
        public void setNetworkConfigId(int networkConfigId) {
            if (networkConfigId >= 0) {
                this.mNetworkConfigId = networkConfigId;
            }
        }

        /* access modifiers changed from: package-private */
        public WifiScoreCardProto.AccessPoint toAccessPoint() {
            return toAccessPoint(false);
        }

        /* access modifiers changed from: package-private */
        public WifiScoreCardProto.AccessPoint toAccessPoint(boolean obfuscate) {
            finishPendingRead();
            WifiScoreCardProto.AccessPoint.Builder builder = WifiScoreCardProto.AccessPoint.newBuilder();
            builder.setId(this.id);
            if (!obfuscate) {
                builder.setBssid(ByteString.copyFrom(this.bssid.toByteArray()));
            }
            WifiScoreCardProto.SecurityType securityType = this.mSecurityType;
            if (securityType != null) {
                builder.setSecurityType(securityType);
            }
            for (PerSignal sig : this.mSignalForEventAndFrequency.values()) {
                builder.addEventStats(sig.toSignal());
            }
            return (WifiScoreCardProto.AccessPoint) builder.build();
        }

        /* access modifiers changed from: package-private */
        public PerBssid merge(WifiScoreCardProto.AccessPoint ap) {
            if (ap.hasId() && this.id != ap.getId()) {
                return this;
            }
            if (ap.hasSecurityType()) {
                WifiScoreCardProto.SecurityType prev = ap.getSecurityType();
                WifiScoreCardProto.SecurityType securityType = this.mSecurityType;
                if (securityType == null) {
                    this.mSecurityType = prev;
                } else if (!securityType.equals(prev)) {
                    this.changed = true;
                }
            }
            for (WifiScoreCardProto.Signal signal : ap.getEventStatsList()) {
                Pair<WifiScoreCardProto.Event, Integer> key = new Pair<>(signal.getEvent(), Integer.valueOf(signal.getFrequency()));
                PerSignal perSignal = this.mSignalForEventAndFrequency.get(key);
                if (perSignal == null) {
                    this.mSignalForEventAndFrequency.put(key, new PerSignal(signal));
                } else {
                    perSignal.merge(signal);
                    this.changed = true;
                }
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public String getL2Key() {
            return this.l2Key.toString();
        }

        /* access modifiers changed from: package-private */
        public void lazyMerge(byte[] serialized) {
            if (serialized != null && this.mPendingReadFromStore.getAndSet(serialized) != null) {
                Log.e("WifiScoreCard", "More answers than we expected!");
            }
        }

        /* access modifiers changed from: package-private */
        public void finishPendingRead() {
            byte[] serialized = this.mPendingReadFromStore.getAndSet(null);
            if (serialized != null) {
                try {
                    merge(WifiScoreCardProto.AccessPoint.parseFrom(serialized));
                } catch (InvalidProtocolBufferException e) {
                    Log.e("WifiScoreCard", "Failed to deserialize", e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public PerBssid lookupBssid(String ssid, String bssid) {
        if (ssid == null || "<unknown ssid>".equals(ssid) || bssid == null) {
            return this.mDummyPerBssid;
        }
        try {
            MacAddress mac = MacAddress.fromString(bssid);
            PerBssid ans = this.mApForBssid.get(mac);
            if (ans == null || !ans.ssid.equals(ssid)) {
                ans = new PerBssid(ssid, mac);
                PerBssid old = this.mApForBssid.put(mac, ans);
                if (old != null) {
                    Log.i("WifiScoreCard", "Discarding stats for score card (ssid changed) ID: " + old.id);
                }
                requestReadForPerBssid(ans);
            }
            return ans;
        } catch (IllegalArgumentException e) {
            return this.mDummyPerBssid;
        }
    }

    private void requestReadForPerBssid(PerBssid perBssid) {
        MemoryStore memoryStore = this.mMemoryStore;
        if (memoryStore != null) {
            memoryStore.read(perBssid.getL2Key(), new BlobListener() {
                /* class com.android.server.wifi.$$Lambda$WifiScoreCard$MvXSGO5JjjyUcB8N6S3TfVBsoqc */

                @Override // com.android.server.wifi.WifiScoreCard.BlobListener
                public final void onBlobRetrieved(byte[] bArr) {
                    WifiScoreCard.PerBssid.this.lazyMerge(bArr);
                }
            });
        }
    }

    private void requestReadForAllChanged() {
        for (PerBssid perBssid : this.mApForBssid.values()) {
            if (perBssid.changed) {
                requestReadForPerBssid(perBssid);
            }
        }
    }

    public int doWrites() {
        if (this.mMemoryStore == null) {
            return 0;
        }
        int count = 0;
        int bytes = 0;
        for (PerBssid perBssid : this.mApForBssid.values()) {
            if (perBssid.changed) {
                perBssid.finishPendingRead();
                byte[] serialized = perBssid.toAccessPoint(true).toByteArray();
                this.mMemoryStore.write(perBssid.getL2Key(), serialized);
                perBssid.changed = false;
                count++;
                bytes += serialized.length;
            }
        }
        return count;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long computeHashLong(String ssid, MacAddress mac) {
        try {
            byte[][] parts = {this.mL2KeySeed.getBytes(), NativeUtil.byteArrayFromArrayList(NativeUtil.decodeSsid(ssid)), mac.toByteArray()};
            int n = 0;
            for (byte[] bArr : parts) {
                n += bArr.length + 1;
            }
            byte[] mashed = new byte[n];
            int p = 0;
            int i = 0;
            while (i < parts.length) {
                byte[] part = parts[i];
                int p2 = p + 1;
                mashed[p] = (byte) part.length;
                int j = 0;
                while (j < part.length) {
                    mashed[p2] = part[j];
                    j++;
                    p2++;
                }
                i++;
                p = p2;
            }
            try {
                return ByteBuffer.wrap(MessageDigest.getInstance("SHA-256").digest(mashed)).getLong();
            } catch (NoSuchAlgorithmException e) {
                Log.e("WifiScoreCard", "SHA-256 not supported.");
                return 0;
            }
        } catch (IllegalArgumentException e2) {
            Log.e("WifiScoreCard", "Could not decode ssid.");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static int idFromLong(long hash) {
        return ((int) hash) & ScoringParams.Values.MAX_EXPID;
    }

    /* access modifiers changed from: private */
    public static String l2KeyFromLong(long hash) {
        return "W" + Long.toHexString(hash);
    }

    private static String groupHintFromLong(long hash) {
        return "G" + Long.toHexString(hash);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public PerBssid fetchByBssid(MacAddress mac) {
        return this.mApForBssid.get(mac);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public PerBssid perBssidFromAccessPoint(String ssid, WifiScoreCardProto.AccessPoint ap) {
        return new PerBssid(ssid, MacAddress.fromBytes(ap.getBssid().toByteArray())).merge(ap);
    }

    /* access modifiers changed from: package-private */
    public final class PerSignal {
        public final PerUnivariateStatistic elapsedMs;
        public final WifiScoreCardProto.Event event;
        public final int frequency;
        public final PerUnivariateStatistic linkspeed;
        public final PerUnivariateStatistic rssi;

        PerSignal(WifiScoreCardProto.Event event2, int frequency2) {
            this.event = event2;
            this.frequency = frequency2;
            this.rssi = new PerUnivariateStatistic();
            this.linkspeed = new PerUnivariateStatistic();
            switch (event2) {
                case FIRST_POLL_AFTER_CONNECTION:
                case IP_CONFIGURATION_SUCCESS:
                case VALIDATION_SUCCESS:
                case CONNECTION_FAILURE:
                case WIFI_DISABLED:
                case ROAM_FAILURE:
                    this.elapsedMs = new PerUnivariateStatistic();
                    return;
                default:
                    this.elapsedMs = null;
                    return;
            }
        }

        PerSignal(WifiScoreCardProto.Signal signal) {
            this.event = signal.getEvent();
            this.frequency = signal.getFrequency();
            this.rssi = new PerUnivariateStatistic(signal.getRssi());
            this.linkspeed = new PerUnivariateStatistic(signal.getLinkspeed());
            if (signal.hasElapsedMs()) {
                this.elapsedMs = new PerUnivariateStatistic(signal.getElapsedMs());
            } else {
                this.elapsedMs = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void merge(WifiScoreCardProto.Signal signal) {
            boolean z = true;
            Preconditions.checkArgument(this.event == signal.getEvent());
            if (this.frequency != signal.getFrequency()) {
                z = false;
            }
            Preconditions.checkArgument(z);
            this.rssi.merge(signal.getRssi());
            this.linkspeed.merge(signal.getLinkspeed());
            if (signal.hasElapsedMs()) {
                this.elapsedMs.merge(signal.getElapsedMs());
            }
        }

        /* access modifiers changed from: package-private */
        public WifiScoreCardProto.Signal toSignal() {
            WifiScoreCardProto.Signal.Builder builder = WifiScoreCardProto.Signal.newBuilder();
            builder.setEvent(this.event).setFrequency(this.frequency).setRssi(this.rssi.toUnivariateStatistic()).setLinkspeed(this.linkspeed.toUnivariateStatistic());
            PerUnivariateStatistic perUnivariateStatistic = this.elapsedMs;
            if (perUnivariateStatistic != null) {
                builder.setElapsedMs(perUnivariateStatistic.toUnivariateStatistic());
            }
            return (WifiScoreCardProto.Signal) builder.build();
        }
    }

    /* access modifiers changed from: package-private */
    public final class PerUnivariateStatistic {
        public long count = 0;
        public double historicalMean = 0.0d;
        public double historicalVariance = Double.POSITIVE_INFINITY;
        public double maxValue = Double.NEGATIVE_INFINITY;
        public double minValue = Double.POSITIVE_INFINITY;
        public double sum = 0.0d;
        public double sumOfSquares = 0.0d;

        PerUnivariateStatistic() {
        }

        PerUnivariateStatistic(WifiScoreCardProto.UnivariateStatistic stats) {
            if (stats.hasCount()) {
                this.count = stats.getCount();
                this.sum = stats.getSum();
                this.sumOfSquares = stats.getSumOfSquares();
            }
            if (stats.hasMinValue()) {
                this.minValue = stats.getMinValue();
            }
            if (stats.hasMaxValue()) {
                this.maxValue = stats.getMaxValue();
            }
            if (stats.hasHistoricalMean()) {
                this.historicalMean = stats.getHistoricalMean();
            }
            if (stats.hasHistoricalVariance()) {
                this.historicalVariance = stats.getHistoricalVariance();
            }
        }

        /* access modifiers changed from: package-private */
        public void update(double value) {
            this.count++;
            this.sum += value;
            this.sumOfSquares += value * value;
            this.minValue = Math.min(this.minValue, value);
            this.maxValue = Math.max(this.maxValue, value);
        }

        /* access modifiers changed from: package-private */
        public void age() {
        }

        /* access modifiers changed from: package-private */
        public void merge(WifiScoreCardProto.UnivariateStatistic stats) {
            if (stats.hasCount()) {
                this.count += stats.getCount();
                this.sum += stats.getSum();
                this.sumOfSquares += stats.getSumOfSquares();
            }
            if (stats.hasMinValue()) {
                this.minValue = Math.min(this.minValue, stats.getMinValue());
            }
            if (stats.hasMaxValue()) {
                this.maxValue = Math.max(this.maxValue, stats.getMaxValue());
            }
            if (!stats.hasHistoricalVariance()) {
                return;
            }
            if (this.historicalVariance < Double.POSITIVE_INFINITY) {
                double numer1 = stats.getHistoricalVariance();
                double numer2 = this.historicalVariance;
                double denom = numer1 + numer2;
                this.historicalMean = ((this.historicalMean * numer1) + (stats.getHistoricalMean() * numer2)) / denom;
                this.historicalVariance = (numer1 * numer2) / denom;
                return;
            }
            this.historicalMean = stats.getHistoricalMean();
            this.historicalVariance = stats.getHistoricalVariance();
        }

        /* access modifiers changed from: package-private */
        public WifiScoreCardProto.UnivariateStatistic toUnivariateStatistic() {
            WifiScoreCardProto.UnivariateStatistic.Builder builder = WifiScoreCardProto.UnivariateStatistic.newBuilder();
            long j = this.count;
            if (j != 0) {
                builder.setCount(j).setSum(this.sum).setSumOfSquares(this.sumOfSquares).setMinValue(this.minValue).setMaxValue(this.maxValue);
            }
            if (this.historicalVariance < Double.POSITIVE_INFINITY) {
                builder.setHistoricalMean(this.historicalMean).setHistoricalVariance(this.historicalVariance);
            }
            return (WifiScoreCardProto.UnivariateStatistic) builder.build();
        }
    }

    public byte[] getNetworkListByteArray(boolean obfuscate) {
        Map<String, WifiScoreCardProto.Network.Builder> networks = new ArrayMap<>();
        for (PerBssid perBssid : this.mApForBssid.values()) {
            String key = perBssid.ssid;
            WifiScoreCardProto.Network.Builder network = networks.get(key);
            if (network == null) {
                network = WifiScoreCardProto.Network.newBuilder();
                networks.put(key, network);
                if (!obfuscate) {
                    network.setSsid(perBssid.ssid);
                }
                if (perBssid.mSecurityType != null) {
                    network.setSecurityType(perBssid.mSecurityType);
                }
                if (perBssid.mNetworkAgentId >= network.getNetworkAgentId()) {
                    network.setNetworkAgentId(perBssid.mNetworkAgentId);
                }
                if (perBssid.mNetworkConfigId >= network.getNetworkConfigId()) {
                    network.setNetworkConfigId(perBssid.mNetworkConfigId);
                }
            }
            network.addAccessPoints(perBssid.toAccessPoint(obfuscate));
        }
        WifiScoreCardProto.NetworkList.Builder builder = WifiScoreCardProto.NetworkList.newBuilder();
        for (WifiScoreCardProto.Network.Builder network2 : networks.values()) {
            builder.addNetworks(network2);
        }
        return ((WifiScoreCardProto.NetworkList) builder.build()).toByteArray();
    }

    public String getNetworkListBase64(boolean obfuscate) {
        return Base64.encodeToString(getNetworkListByteArray(obfuscate), 0);
    }

    public void clear() {
        this.mApForBssid.clear();
        resetConnectionStateInternal(false);
    }
}
