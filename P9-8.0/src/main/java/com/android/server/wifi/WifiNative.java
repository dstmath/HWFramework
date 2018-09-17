package com.android.server.wifi;

import android.net.apf.ApfCapabilities;
import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.RttManager.ResponderConfig;
import android.net.wifi.RttManager.RttCapabilities;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.RttManager.RttResult;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiWakeReasonAndCounts;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.Immutable;
import com.android.internal.util.HexDump;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.util.FrameParser;
import com.android.server.wifi.util.WifiCommonUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class WifiNative {
    public static final int BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    public static final int BLUETOOTH_COEXISTENCE_MODE_ENABLED = 0;
    public static final int BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    public static final int DISABLE_FIRMWARE_ROAMING = 0;
    public static final int ENABLE_FIRMWARE_ROAMING = 1;
    public static final int RX_FILTER_TYPE_V4_MULTICAST = 0;
    public static final int RX_FILTER_TYPE_V6_MULTICAST = 1;
    public static final String SIM_AUTH_RESP_TYPE_GSM_AUTH = "GSM-AUTH";
    public static final String SIM_AUTH_RESP_TYPE_UMTS_AUTH = "UMTS-AUTH";
    public static final String SIM_AUTH_RESP_TYPE_UMTS_AUTS = "UMTS-AUTS";
    private static final String VOWIFI_DETECT_SET_PREFIX = "SET ";
    public static final int WIFI_SCAN_FAILED = 3;
    public static final int WIFI_SCAN_RESULTS_AVAILABLE = 0;
    public static final int WIFI_SCAN_THRESHOLD_NUM_SCANS = 1;
    public static final int WIFI_SCAN_THRESHOLD_PERCENT = 2;
    private static final int WPA_SUPP_TYPE_CONFIG = 0;
    private static final int WPA_SUPP_TYPE_RAW_PSK = 1;
    private final String mInterfaceName;
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private final String mTAG;
    private final WifiVendorHal mWifiVendorHal;
    private final WificondControl mWificondControl;
    private Set<String> moldSsidList = new HashSet();

    public interface VendorHalDeathEventHandler {
        void onDeath();
    }

    public interface RttEventHandler {
        void onRttResults(RttResult[] rttResultArr);
    }

    public interface WifiLoggerEventHandler {
        void onRingBufferData(RingBufferStatus ringBufferStatus, byte[] bArr);

        void onWifiAlert(int i, byte[] bArr);
    }

    public static class BucketSettings {
        public int band;
        public int bucket;
        public ChannelSettings[] channels;
        public int max_period_ms;
        public int num_channels;
        public int period_ms;
        public int report_events;
        public int step_count;
    }

    public static class ChannelSettings {
        public int dwell_time_ms;
        public int frequency;
        public boolean passive;
    }

    @Immutable
    static abstract class FateReport {
        static final int MAX_DRIVER_TIMESTAMP_MSEC = 4294967;
        static final int USEC_PER_MSEC = 1000;
        static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
        final long mDriverTimestampUSec;
        final long mEstimatedWallclockMSec = convertDriverTimestampUSecToWallclockMSec(this.mDriverTimestampUSec);
        final byte mFate;
        final byte[] mFrameBytes;
        final byte mFrameType;

        protected abstract String directionToString();

        protected abstract String fateToString();

        FateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            this.mFate = fate;
            this.mDriverTimestampUSec = driverTimestampUSec;
            this.mFrameType = frameType;
            this.mFrameBytes = frameBytes;
        }

        public String toTableRowString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            pw.format("%-15s  %12s  %-9s  %-32s  %-12s  %-23s  %s\n", new Object[]{Long.valueOf(this.mDriverTimestampUSec), dateFormatter.format(new Date(this.mEstimatedWallclockMSec)), directionToString(), fateToString(), parser.mMostSpecificProtocolString, parser.mTypeString, parser.mResultString});
            return sw.toString();
        }

        public String toVerboseStringWithPiiAllowed() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            FrameParser parser = new FrameParser(this.mFrameType, this.mFrameBytes);
            pw.format("Frame direction: %s\n", new Object[]{directionToString()});
            pw.format("Frame timestamp: %d\n", new Object[]{Long.valueOf(this.mDriverTimestampUSec)});
            pw.format("Frame fate: %s\n", new Object[]{fateToString()});
            pw.format("Frame type: %s\n", new Object[]{frameTypeToString(this.mFrameType)});
            pw.format("Frame protocol: %s\n", new Object[]{parser.mMostSpecificProtocolString});
            pw.format("Frame protocol type: %s\n", new Object[]{parser.mTypeString});
            pw.format("Frame length: %d\n", new Object[]{Integer.valueOf(this.mFrameBytes.length)});
            pw.append("Frame bytes");
            pw.append(HexDump.dumpHexString(this.mFrameBytes));
            pw.append("\n");
            return sw.toString();
        }

        public static String getTableHeader() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.format("\n%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", new Object[]{"Time usec", "Walltime", "Direction", "Fate", "Protocol", "Type", "Result"});
            pw.format("%-15s  %-12s  %-9s  %-32s  %-12s  %-23s  %s\n", new Object[]{"---------", "--------", "---------", "----", "--------", "----", "------"});
            return sw.toString();
        }

        private static String frameTypeToString(byte frameType) {
            switch (frameType) {
                case (byte) 0:
                    return "unknown";
                case (byte) 1:
                    return "data";
                case (byte) 2:
                    return "802.11 management";
                default:
                    return Byte.toString(frameType);
            }
        }

        private static long convertDriverTimestampUSecToWallclockMSec(long driverTimestampUSec) {
            long wallclockMillisNow = System.currentTimeMillis();
            long driverTimestampMillis = driverTimestampUSec / 1000;
            long boottimeTimestampMillis = SystemClock.elapsedRealtime() % 4294967;
            if (boottimeTimestampMillis < driverTimestampMillis) {
                boottimeTimestampMillis += 4294967;
            }
            return wallclockMillisNow - (boottimeTimestampMillis - driverTimestampMillis);
        }
    }

    public static class HiddenNetwork {
        public String ssid;

        public boolean equals(Object otherObj) {
            if (this == otherObj) {
                return true;
            }
            if (otherObj == null || getClass() != otherObj.getClass()) {
                return false;
            }
            return Objects.equals(this.ssid, ((HiddenNetwork) otherObj).ssid);
        }

        public int hashCode() {
            return this.ssid == null ? 0 : this.ssid.hashCode();
        }
    }

    public interface PnoEventHandler {
        void onPnoNetworkFound(ScanResult[] scanResultArr);

        void onPnoScanFailed();
    }

    public static class PnoNetwork {
        public byte auth_bit_field;
        public byte flags;
        public String ssid;

        public boolean equals(Object otherObj) {
            boolean z = true;
            if (this == otherObj) {
                return true;
            }
            if (otherObj == null || getClass() != otherObj.getClass()) {
                return false;
            }
            PnoNetwork other = (PnoNetwork) otherObj;
            if (!Objects.equals(this.ssid, other.ssid) || this.flags != other.flags) {
                z = false;
            } else if (this.auth_bit_field != other.auth_bit_field) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (this.ssid == null ? 0 : this.ssid.hashCode()) ^ ((this.flags * 31) + (this.auth_bit_field << 8));
        }
    }

    public static class PnoSettings {
        public int band5GHzBonus;
        public int currentConnectionBonus;
        public int initialScoreMax;
        public boolean isConnected;
        public int min24GHzRssi;
        public int min5GHzRssi;
        public PnoNetwork[] networkList;
        public int periodInMs;
        public int sameNetworkBonus;
        public int secureBonus;
    }

    public static class RingBufferStatus {
        public static final int HAS_ASCII_ENTRIES = 2;
        public static final int HAS_BINARY_ENTRIES = 1;
        public static final int HAS_PER_PACKET_ENTRIES = 4;
        int flag;
        String name;
        int readBytes;
        int ringBufferByteSize;
        int ringBufferId;
        int verboseLevel;
        int writtenBytes;
        int writtenRecords;

        public String toString() {
            return "name: " + this.name + " flag: " + this.flag + " ringBufferId: " + this.ringBufferId + " ringBufferByteSize: " + this.ringBufferByteSize + " verboseLevel: " + this.verboseLevel + " writtenBytes: " + this.writtenBytes + " readBytes: " + this.readBytes + " writtenRecords: " + this.writtenRecords;
        }
    }

    public static class RoamingCapabilities {
        public int maxBlacklistSize;
        public int maxWhitelistSize;
    }

    public static class RoamingConfig {
        public ArrayList<String> blacklistBssids;
        public ArrayList<String> whitelistSsids;
    }

    @Immutable
    public static final class RxFateReport extends FateReport {
        RxFateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            super(fate, driverTimestampUSec, frameType, frameBytes);
        }

        protected String directionToString() {
            return "RX";
        }

        protected String fateToString() {
            switch (this.mFate) {
                case (byte) 0:
                    return "success";
                case (byte) 1:
                    return "firmware queued";
                case (byte) 2:
                    return "firmware dropped (filter)";
                case (byte) 3:
                    return "firmware dropped (invalid frame)";
                case (byte) 4:
                    return "firmware dropped (no bufs)";
                case (byte) 5:
                    return "firmware dropped (other)";
                case (byte) 6:
                    return "driver queued";
                case (byte) 7:
                    return "driver dropped (filter)";
                case (byte) 8:
                    return "driver dropped (invalid frame)";
                case (byte) 9:
                    return "driver dropped (no bufs)";
                case (byte) 10:
                    return "driver dropped (other)";
                default:
                    return Byte.toString(this.mFate);
            }
        }
    }

    public static class ScanCapabilities {
        public int max_ap_cache_per_scan;
        public int max_rssi_sample_size;
        public int max_scan_buckets;
        public int max_scan_cache_size;
        public int max_scan_reporting_threshold;
    }

    public interface ScanEventHandler {
        void onFullScanResult(ScanResult scanResult, int i);

        void onScanPaused(ScanData[] scanDataArr);

        void onScanRestarted();

        void onScanStatus(int i);
    }

    public static class ScanSettings {
        public int base_period_ms;
        public BucketSettings[] buckets;
        public String handlerId;
        public HiddenNetwork[] hiddenNetworks;
        public int max_ap_per_scan;
        public int num_buckets;
        public int report_threshold_num_scans;
        public int report_threshold_percent;

        public String toString() {
            return this.handlerId;
        }
    }

    public static class SignalPollResult {
        public int associationFrequency;
        public int currentChload;
        public int currentNoise;
        public int currentRssi;
        public int currentSnr;
        public int txBitrate;
    }

    @Immutable
    public static final class TxFateReport extends FateReport {
        TxFateReport(byte fate, long driverTimestampUSec, byte frameType, byte[] frameBytes) {
            super(fate, driverTimestampUSec, frameType, frameBytes);
        }

        protected String directionToString() {
            return "TX";
        }

        protected String fateToString() {
            switch (this.mFate) {
                case (byte) 0:
                    return "acked";
                case (byte) 1:
                    return "sent";
                case (byte) 2:
                    return "firmware queued";
                case (byte) 3:
                    return "firmware dropped (invalid frame)";
                case (byte) 4:
                    return "firmware dropped (no bufs)";
                case (byte) 5:
                    return "firmware dropped (other)";
                case (byte) 6:
                    return "driver queued";
                case (byte) 7:
                    return "driver dropped (invalid frame)";
                case (byte) 8:
                    return "driver dropped (no bufs)";
                case (byte) 9:
                    return "driver dropped (other)";
                default:
                    return Byte.toString(this.mFate);
            }
        }
    }

    public static class TxPacketCounters {
        public int txFailed;
        public int txSucceeded;
    }

    public interface WifiRssiEventHandler {
        void onRssiThresholdBreached(byte b);
    }

    private native int deauthLastRoamingBssidHwNative(String str, String str2, String str3);

    private native int disassociateSoftapStaHwNative(String str, String str2);

    private native int gameKOGAdjustSpeedNative(int i, int i2);

    private native String getSoftapClientsHwNative(String str);

    private native int hwDelArpItemNative(String str, String str2);

    private native int hwSetArpItemNative(String str, String str2, String str3);

    private native int hwSetPwrBoostNative(int i);

    private static native byte[] readKernelLogNative();

    private native String readSoftapDhcpLeaseFileHwNative(String str);

    private static native int registerNatives();

    private native int setSoftapHwNative(String str, String str2, String str3);

    private native int setSoftapMacFltrHwNative(String str, String str2);

    private native int setWifiTxPowerNative(int i);

    public WifiNative(String interfaceName, WifiVendorHal vendorHal, SupplicantStaIfaceHal staIfaceHal, WificondControl condControl) {
        this.mTAG = "WifiNative-" + interfaceName;
        this.mInterfaceName = interfaceName;
        this.mWifiVendorHal = vendorHal;
        this.mSupplicantStaIfaceHal = staIfaceHal;
        this.mWificondControl = condControl;
    }

    public String getInterfaceName() {
        return this.mInterfaceName;
    }

    public void enableVerboseLogging(int verbose) {
        boolean z;
        boolean z2 = true;
        WificondControl wificondControl = this.mWificondControl;
        if (verbose > 0) {
            z = true;
        } else {
            z = false;
        }
        wificondControl.enableVerboseLogging(z);
        SupplicantStaIfaceHal supplicantStaIfaceHal = this.mSupplicantStaIfaceHal;
        if (verbose > 0) {
            z = true;
        } else {
            z = false;
        }
        supplicantStaIfaceHal.enableVerboseLogging(z);
        WifiVendorHal wifiVendorHal = this.mWifiVendorHal;
        if (verbose <= 0) {
            z2 = false;
        }
        wifiVendorHal.enableVerboseLogging(z2);
    }

    public IClientInterface setupForClientMode() {
        if (startHalIfNecessary(true)) {
            return this.mWificondControl.setupDriverForClientMode();
        }
        Log.e(this.mTAG, "Failed to start HAL for client mode");
        return null;
    }

    public IApInterface setupForSoftApMode() {
        if (startHalIfNecessary(false)) {
            return this.mWificondControl.setupDriverForSoftApMode();
        }
        Log.e(this.mTAG, "Failed to start HAL for AP mode");
        return null;
    }

    public boolean tearDown() {
        if (this.mWificondControl.tearDownInterfaces()) {
            stopHalIfNecessary();
            return true;
        }
        Log.e(this.mTAG, "Failed to teardown interfaces from Wificond");
        return false;
    }

    public boolean disableSupplicant() {
        return this.mWificondControl.disableSupplicant();
    }

    public boolean enableSupplicant() {
        return this.mWificondControl.enableSupplicant();
    }

    public SignalPollResult signalPoll() {
        return this.mWificondControl.signalPoll();
    }

    public TxPacketCounters getTxPacketCounters() {
        return this.mWificondControl.getTxPacketCounters();
    }

    public boolean scan(Set<Integer> freqs, List<String> hiddenNetworkSSIDs) {
        return this.mWificondControl.scan(freqs, hiddenNetworkSSIDs);
    }

    public ArrayList<ScanDetail> getScanResults() {
        return this.mWificondControl.getScanResults();
    }

    public boolean startPnoScan(PnoSettings pnoSettings) {
        return this.mWificondControl.startPnoScan(pnoSettings);
    }

    public boolean stopPnoScan() {
        return this.mWificondControl.stopPnoScan();
    }

    public boolean connectToSupplicant() {
        if (this.mSupplicantStaIfaceHal.isInitializationStarted() || (this.mSupplicantStaIfaceHal.initialize() ^ 1) == 0) {
            return this.mSupplicantStaIfaceHal.isInitializationComplete();
        }
        return false;
    }

    public void closeSupplicantConnection() {
    }

    public void setSupplicantLogLevel(boolean turnOnVerbose) {
        this.mSupplicantStaIfaceHal.setLogLevel(turnOnVerbose);
    }

    public boolean reconnect() {
        boolean result = this.mSupplicantStaIfaceHal.reconnect();
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean reassociate() {
        boolean result = this.mSupplicantStaIfaceHal.reassociate();
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean disconnect() {
        return this.mSupplicantStaIfaceHal.disconnect();
    }

    public String getMacAddress() {
        return this.mSupplicantStaIfaceHal.getMacAddress();
    }

    public boolean startFilteringMulticastV4Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.removeRxFilter(0)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean stopFilteringMulticastV4Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.addRxFilter(0)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean startFilteringMulticastV6Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.removeRxFilter(1)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean stopFilteringMulticastV6Packets() {
        if (this.mSupplicantStaIfaceHal.stopRxFilter() && this.mSupplicantStaIfaceHal.addRxFilter(1)) {
            return this.mSupplicantStaIfaceHal.startRxFilter();
        }
        return false;
    }

    public boolean setBluetoothCoexistenceMode(int mode) {
        return this.mSupplicantStaIfaceHal.setBtCoexistenceMode(mode);
    }

    public boolean setBluetoothCoexistenceScanMode(boolean setCoexScanMode) {
        return this.mSupplicantStaIfaceHal.setBtCoexistenceScanModeEnabled(setCoexScanMode);
    }

    public boolean setSuspendOptimizations(boolean enabled) {
        return this.mSupplicantStaIfaceHal.setSuspendModeEnabled(enabled);
    }

    public boolean setCountryCode(String countryCode) {
        return this.mSupplicantStaIfaceHal.setCountryCode(countryCode);
    }

    public boolean setFilterEnable(boolean enable) {
        return this.mSupplicantStaIfaceHal.setFilterEnable(enable);
    }

    public void startTdls(String macAddr, boolean enable) {
        if (enable) {
            this.mSupplicantStaIfaceHal.initiateTdlsDiscover(macAddr);
            this.mSupplicantStaIfaceHal.initiateTdlsSetup(macAddr);
            return;
        }
        this.mSupplicantStaIfaceHal.initiateTdlsTeardown(macAddr);
    }

    public boolean startWpsPbc(String bssid) {
        return this.mSupplicantStaIfaceHal.startWpsPbc(bssid);
    }

    public boolean startWpsPinKeypad(String pin) {
        return this.mSupplicantStaIfaceHal.startWpsPinKeypad(pin);
    }

    public String startWpsPinDisplay(String bssid) {
        return this.mSupplicantStaIfaceHal.startWpsPinDisplay(bssid);
    }

    public boolean setExternalSim(boolean external) {
        return this.mSupplicantStaIfaceHal.setExternalSim(external);
    }

    public boolean simAuthResponse(int id, String type, String response) {
        if (SIM_AUTH_RESP_TYPE_GSM_AUTH.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimGsmAuthResponse(response);
        }
        if (SIM_AUTH_RESP_TYPE_UMTS_AUTH.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAuthResponse(response);
        }
        if (SIM_AUTH_RESP_TYPE_UMTS_AUTS.equals(type)) {
            return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAutsResponse(response);
        }
        return false;
    }

    public boolean simAuthFailedResponse(int id) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimGsmAuthFailure();
    }

    public boolean umtsAuthFailedResponse(int id) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapSimUmtsAuthFailure();
    }

    public boolean simIdentityResponse(int id, String response) {
        return this.mSupplicantStaIfaceHal.sendCurrentNetworkEapIdentityResponse(response);
    }

    public String getEapAnonymousIdentity() {
        return this.mSupplicantStaIfaceHal.getCurrentNetworkEapAnonymousIdentity();
    }

    public boolean startWpsRegistrar(String bssid, String pin) {
        return this.mSupplicantStaIfaceHal.startWpsRegistrar(bssid, pin);
    }

    public boolean cancelWps() {
        return this.mSupplicantStaIfaceHal.cancelWps();
    }

    public boolean setDeviceName(String name) {
        return this.mSupplicantStaIfaceHal.setWpsDeviceName(name);
    }

    public boolean setDeviceType(String type) {
        return this.mSupplicantStaIfaceHal.setWpsDeviceType(type);
    }

    public boolean setConfigMethods(String cfg) {
        return this.mSupplicantStaIfaceHal.setWpsConfigMethods(cfg);
    }

    public boolean setManufacturer(String value) {
        return this.mSupplicantStaIfaceHal.setWpsManufacturer(value);
    }

    public boolean setModelName(String value) {
        return this.mSupplicantStaIfaceHal.setWpsModelName(value);
    }

    public boolean setModelNumber(String value) {
        return this.mSupplicantStaIfaceHal.setWpsModelNumber(value);
    }

    public boolean setSerialNumber(String value) {
        return this.mSupplicantStaIfaceHal.setWpsSerialNumber(value);
    }

    public void setPowerSave(boolean enabled) {
        this.mSupplicantStaIfaceHal.setPowerSave(enabled);
    }

    public boolean setConcurrencyPriority(boolean isStaHigherPriority) {
        return this.mSupplicantStaIfaceHal.setConcurrencyPriority(isStaHigherPriority);
    }

    public boolean enableStaAutoReconnect(boolean enable) {
        return this.mSupplicantStaIfaceHal.enableAutoReconnect(enable);
    }

    public boolean migrateNetworksFromSupplicant(Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        return this.mSupplicantStaIfaceHal.loadNetworks(configs, networkExtras);
    }

    public boolean connectToNetwork(WifiConfiguration configuration) {
        this.mWificondControl.abortScan();
        boolean result = this.mSupplicantStaIfaceHal.connectToNetwork(configuration);
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public boolean roamToNetwork(WifiConfiguration configuration) {
        this.mWificondControl.abortScan();
        boolean result = this.mSupplicantStaIfaceHal.roamToNetwork(configuration);
        if (result) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_START, "");
        }
        return result;
    }

    public int getFrameworkNetworkId(int supplicantNetworkId) {
        return supplicantNetworkId;
    }

    public boolean removeAllNetworks() {
        return this.mSupplicantStaIfaceHal.removeAllNetworks();
    }

    public boolean setConfiguredNetworkBSSID(String bssid) {
        return this.mSupplicantStaIfaceHal.setCurrentNetworkBssid(bssid);
    }

    public boolean requestAnqp(String bssid, Set<Integer> anqpIds, Set<Integer> hs20Subtypes) {
        if (bssid == null || ((anqpIds == null || anqpIds.isEmpty()) && (hs20Subtypes == null || hs20Subtypes.isEmpty()))) {
            Log.e(this.mTAG, "Invalid arguments for ANQP request.");
            return false;
        }
        ArrayList anqpIdList = new ArrayList();
        if (anqpIds != null) {
            for (Integer anqpId : anqpIds) {
                anqpIdList.add(Short.valueOf(anqpId.shortValue()));
            }
        }
        ArrayList hs20SubtypeList = new ArrayList();
        hs20SubtypeList.addAll(hs20Subtypes);
        return this.mSupplicantStaIfaceHal.initiateAnqpQuery(bssid, anqpIdList, hs20SubtypeList);
    }

    public boolean requestIcon(String bssid, String fileName) {
        if (bssid != null && fileName != null) {
            return this.mSupplicantStaIfaceHal.initiateHs20IconQuery(bssid, fileName);
        }
        Log.e(this.mTAG, "Invalid arguments for Icon request.");
        return false;
    }

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        return this.mSupplicantStaIfaceHal.getCurrentNetworkWpsNfcConfigurationToken();
    }

    public void removeNetworkIfCurrent(int networkId) {
        this.mSupplicantStaIfaceHal.removeNetworkIfCurrent(networkId);
    }

    public boolean initializeVendorHal(VendorHalDeathEventHandler handler) {
        return this.mWifiVendorHal.initialize(handler);
    }

    private boolean startHalIfNecessary(boolean isStaMode) {
        if (this.mWifiVendorHal.isVendorHalSupported()) {
            return this.mWifiVendorHal.startVendorHal(isStaMode);
        }
        Log.i(this.mTAG, "Vendor HAL not supported, Ignore start...");
        return true;
    }

    private void stopHalIfNecessary() {
        if (this.mWifiVendorHal.isVendorHalSupported()) {
            this.mWifiVendorHal.stopVendorHal();
        } else {
            Log.i(this.mTAG, "Vendor HAL not supported, Ignore stop...");
        }
    }

    public boolean isHalStarted() {
        return this.mWifiVendorHal.isHalStarted();
    }

    public boolean getBgScanCapabilities(ScanCapabilities capabilities) {
        return this.mWifiVendorHal.getBgScanCapabilities(capabilities);
    }

    public boolean startBgScan(ScanSettings settings, ScanEventHandler eventHandler) {
        return this.mWifiVendorHal.startBgScan(settings, eventHandler);
    }

    public void stopBgScan() {
        this.mWifiVendorHal.stopBgScan();
    }

    public void pauseBgScan() {
        this.mWifiVendorHal.pauseBgScan();
    }

    public void restartBgScan() {
        this.mWifiVendorHal.restartBgScan();
    }

    public ScanData[] getBgScanResults() {
        return this.mWifiVendorHal.getBgScanResults();
    }

    public WifiLinkLayerStats getWifiLinkLayerStats(String iface) {
        return this.mWifiVendorHal.getWifiLinkLayerStats();
    }

    public int getSupportedFeatureSet() {
        return this.mWifiVendorHal.getSupportedFeatureSet();
    }

    public boolean requestRtt(RttParams[] params, RttEventHandler handler) {
        return this.mWifiVendorHal.requestRtt(params, handler);
    }

    public boolean cancelRtt(RttParams[] params) {
        return this.mWifiVendorHal.cancelRtt(params);
    }

    public ResponderConfig enableRttResponder(int timeoutSeconds) {
        return this.mWifiVendorHal.enableRttResponder(timeoutSeconds);
    }

    public boolean disableRttResponder() {
        return this.mWifiVendorHal.disableRttResponder();
    }

    public boolean setScanningMacOui(byte[] oui) {
        return this.mWifiVendorHal.setScanningMacOui(oui);
    }

    public int[] getChannelsForBand(int band) {
        return this.mWifiVendorHal.getChannelsForBand(band);
    }

    public boolean isGetChannelsForBandSupported() {
        return this.mWifiVendorHal.isGetChannelsForBandSupported();
    }

    public RttCapabilities getRttCapabilities() {
        return this.mWifiVendorHal.getRttCapabilities();
    }

    public ApfCapabilities getApfCapabilities() {
        return this.mWifiVendorHal.getApfCapabilities();
    }

    public boolean installPacketFilter(byte[] filter) {
        return this.mWifiVendorHal.installPacketFilter(filter);
    }

    public boolean setCountryCodeHal(String countryCode) {
        return this.mWifiVendorHal.setCountryCodeHal(countryCode);
    }

    public boolean setLoggingEventHandler(WifiLoggerEventHandler handler) {
        return this.mWifiVendorHal.setLoggingEventHandler(handler);
    }

    public boolean startLoggingRingBuffer(int verboseLevel, int flags, int maxInterval, int minDataSize, String ringName) {
        return this.mWifiVendorHal.startLoggingRingBuffer(verboseLevel, flags, maxInterval, minDataSize, ringName);
    }

    public int getSupportedLoggerFeatureSet() {
        return this.mWifiVendorHal.getSupportedLoggerFeatureSet();
    }

    public boolean resetLogHandler() {
        return this.mWifiVendorHal.resetLogHandler();
    }

    public String getDriverVersion() {
        return this.mWifiVendorHal.getDriverVersion();
    }

    public String getFirmwareVersion() {
        return this.mWifiVendorHal.getFirmwareVersion();
    }

    public RingBufferStatus[] getRingBufferStatus() {
        return this.mWifiVendorHal.getRingBufferStatus();
    }

    public boolean getRingBufferData(String ringName) {
        return this.mWifiVendorHal.getRingBufferData(ringName);
    }

    public byte[] getFwMemoryDump() {
        return this.mWifiVendorHal.getFwMemoryDump();
    }

    public byte[] getDriverStateDump() {
        return this.mWifiVendorHal.getDriverStateDump();
    }

    public boolean startPktFateMonitoring() {
        return this.mWifiVendorHal.startPktFateMonitoring();
    }

    public boolean getTxPktFates(TxFateReport[] reportBufs) {
        return this.mWifiVendorHal.getTxPktFates(reportBufs);
    }

    public boolean getRxPktFates(RxFateReport[] reportBufs) {
        return this.mWifiVendorHal.getRxPktFates(reportBufs);
    }

    public boolean setPnoList(PnoSettings settings, PnoEventHandler eventHandler) {
        Log.e(this.mTAG, "setPnoList not supported");
        return false;
    }

    public boolean resetPnoList() {
        Log.e(this.mTAG, "resetPnoList not supported");
        return false;
    }

    public int startSendingOffloadedPacket(int slot, KeepalivePacketData keepAlivePacket, int period) {
        String[] macAddrStr = getMacAddress().split(":");
        byte[] srcMac = new byte[6];
        for (int i = 0; i < 6; i++) {
            srcMac[i] = Integer.valueOf(Integer.parseInt(macAddrStr[i], 16)).byteValue();
        }
        return this.mWifiVendorHal.startSendingOffloadedPacket(slot, srcMac, keepAlivePacket, period);
    }

    public int stopSendingOffloadedPacket(int slot) {
        return this.mWifiVendorHal.stopSendingOffloadedPacket(slot);
    }

    public int startRssiMonitoring(byte maxRssi, byte minRssi, WifiRssiEventHandler rssiEventHandler) {
        return this.mWifiVendorHal.startRssiMonitoring(maxRssi, minRssi, rssiEventHandler);
    }

    public int stopRssiMonitoring() {
        return this.mWifiVendorHal.stopRssiMonitoring();
    }

    public WifiWakeReasonAndCounts getWlanWakeReasonCount() {
        return this.mWifiVendorHal.getWlanWakeReasonCount();
    }

    public boolean configureNeighborDiscoveryOffload(boolean enabled) {
        return this.mWifiVendorHal.configureNeighborDiscoveryOffload(enabled);
    }

    public boolean getRoamingCapabilities(RoamingCapabilities capabilities) {
        return this.mWifiVendorHal.getRoamingCapabilities(capabilities);
    }

    public int enableFirmwareRoaming(int state) {
        return this.mWifiVendorHal.enableFirmwareRoaming(state);
    }

    public boolean configureRoaming(RoamingConfig config) {
        Log.d(this.mTAG, "configureRoaming ");
        return this.mWifiVendorHal.configureRoaming(config);
    }

    public boolean resetRoamingConfiguration() {
        return this.mWifiVendorHal.configureRoaming(new RoamingConfig());
    }

    static {
        System.loadLibrary("wifi-service");
        registerNatives();
    }

    public synchronized String readKernelLog() {
        byte[] bytes = readKernelLogNative();
        if (bytes != null) {
            try {
                return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException e) {
                return new String(bytes, StandardCharsets.ISO_8859_1);
            }
        }
        return "*** failed to read kernel log ***";
    }

    public String getWpaSuppConfig() {
        return this.mSupplicantStaIfaceHal.getWpasConfig(0);
    }

    public void sendWifiPowerCommand(int level) {
        this.mSupplicantStaIfaceHal.setTxPower(level);
    }

    public void setIsmcoexMode(boolean enable) {
    }

    public boolean isSupportVoWifiDetect() {
        String ret = this.mSupplicantStaIfaceHal.voWifiDetect("VOWIFI_DETECT VOWIFi_IS_SUPPORT");
        Log.e(this.mTAG, "isSupportVoWifiDetect ret :" + ret);
        if (ret != null) {
            return !ret.equals("true") ? ret.equals("OK") : true;
        } else {
            return false;
        }
    }

    public boolean voWifiDetectSet(String cmd) {
        String ret = this.mSupplicantStaIfaceHal.voWifiDetect(VOWIFI_DETECT_SET_PREFIX + cmd);
        Log.d(this.mTAG, "voWifiDetectSet ret :" + ret);
        if (ret != null) {
            return !ret.equals("true") ? ret.equals("OK") : true;
        } else {
            return false;
        }
    }

    public String heartBeat(String param) {
        return this.mSupplicantStaIfaceHal.heartBeat(param);
    }

    public boolean magiclinkConnect(String config) {
        String command = "MAGICLINK_CONNECT " + config;
        return true;
    }

    public boolean magiclinkGroupAdd(boolean persistent, String freq) {
        return true;
    }

    public boolean magiclinkGroupAdd(int netId, String freq) {
        return true;
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        this.mSupplicantStaIfaceHal.enableHiLinkHandshake(uiEnable, bssid);
    }

    public boolean hwABSSetCapability(int capability) {
        Log.d(this.mTAG, "SET_ABS_CAPABILITY ");
        return this.mSupplicantStaIfaceHal.setAbsCapability(capability);
    }

    public boolean hwABSSoftHandover(int type) {
        Log.d(this.mTAG, "hwABSSoftHandover ");
        return this.mSupplicantStaIfaceHal.absPowerCtrl(type);
    }

    public boolean hwABSBlackList(String bssidList) {
        Log.d(this.mTAG, "hwABSBlackList bssidList = " + bssidList);
        return this.mSupplicantStaIfaceHal.setAbsBlacklist(bssidList);
    }

    public void query11vRoamingNetwork(int reason, String preferredBssid) {
    }

    public void query11vRoamingNetwork(int reason) {
        this.mSupplicantStaIfaceHal.query11vRoamingNetwork(reason);
    }

    public boolean isSupportRsdbByDriver() {
        String result = this.mSupplicantStaIfaceHal.getRsdbCapability();
        if (result != null) {
            Log.d(this.mTAG, "isSupportRsdbByDriver: " + result);
            return "RSDB:1".equals(result);
        }
        Log.i(this.mTAG, "isSupportRsdbByDriver: ");
        return false;
    }

    public boolean isDfsChannel(int frequency) {
        int[] channelsDfs = getChannelsForBand(4);
        if (channelsDfs == null) {
            Log.d(this.mTAG, "Failed to get channels for 5GHz DFS only band,get 5GHz band");
            int[] channels5G = getChannelsForBand(2);
            if (channels5G == null) {
                Log.d(this.mTAG, "Failed to get channels for 5GHz band");
                return false;
            }
            for (int channel5G : channels5G) {
                if (frequency == channel5G) {
                    return false;
                }
            }
            Log.d(this.mTAG, "isDfsChannel: true, frequency not in channels5G: " + frequency);
            return true;
        }
        for (int channelDfs : channelsDfs) {
            if (frequency == channelDfs) {
                Log.d(this.mTAG, "isDfsChannel: true, DfsChannel: " + channelDfs);
                return true;
            }
        }
        return false;
    }

    public String getConnectionRawPsk() {
        return this.mSupplicantStaIfaceHal.getWpasConfig(1);
    }

    public boolean setStaticARP(String ipSrc, String mac) {
        boolean z = false;
        if (TextUtils.isEmpty(ipSrc) || TextUtils.isEmpty(mac)) {
            return false;
        }
        Log.d(this.mTAG, "setStaticARP entered");
        if (hwSetArpItemNative(getInterfaceName(), ipSrc, mac) == 0) {
            z = true;
        }
        return z;
    }

    public boolean delStaticARP(String ipSrc) {
        boolean z = false;
        if (TextUtils.isEmpty(ipSrc)) {
            return false;
        }
        Log.d(this.mTAG, "delStaticARP entered");
        if (hwDelArpItemNative(getInterfaceName(), ipSrc) == 0) {
            z = true;
        }
        return z;
    }

    public boolean setSoftapHw(String chan, String mscb) {
        boolean z = false;
        if (TextUtils.isEmpty(chan) || TextUtils.isEmpty(mscb)) {
            return false;
        }
        Log.d(this.mTAG, "setSoftapHw entered");
        if (setSoftapHwNative(getInterfaceName(), chan, mscb) == 0) {
            z = true;
        }
        return z;
    }

    public String getSoftapClientsHw() {
        Log.d(this.mTAG, "getSoftapClientsHw entered");
        return getSoftapClientsHwNative(getInterfaceName());
    }

    public String readSoftapDhcpLeaseFileHw() {
        Log.d(this.mTAG, "readSoftapDhcpLeaseFileHw entered");
        return readSoftapDhcpLeaseFileHwNative(getInterfaceName());
    }

    public boolean setSoftapMacFltrHw(String filter_str) {
        boolean z = false;
        if (TextUtils.isEmpty(filter_str)) {
            return false;
        }
        Log.d(this.mTAG, "setSoftapMacFltrHw entered");
        if (setSoftapMacFltrHwNative(getInterfaceName(), filter_str) == 0) {
            z = true;
        }
        return z;
    }

    public boolean disassociateSoftapStaHw(String dis_mac) {
        boolean z = false;
        if (TextUtils.isEmpty(dis_mac)) {
            return false;
        }
        Log.d(this.mTAG, "disassociateSoftapStaHw entered");
        if (disassociateSoftapStaHwNative(getInterfaceName(), dis_mac) == 0) {
            z = true;
        }
        return z;
    }

    public boolean deauthLastRoamingBssidHw(String mode, String bssid) {
        boolean z = false;
        if (TextUtils.isEmpty(mode)) {
            return false;
        }
        Log.d(this.mTAG, "deauthLastRoamingBssidHw entered");
        if (deauthLastRoamingBssidHwNative(getInterfaceName(), mode, bssid) == 0) {
            z = true;
        }
        return z;
    }

    public int setWifiTxPowerHw(int power) {
        return setWifiTxPowerNative(power);
    }

    public void pwrPercentBoostModeset(int rssi) {
        this.mSupplicantStaIfaceHal.pwrPercentBoostModeset(rssi);
    }

    public String getMssState() {
        return this.mSupplicantStaIfaceHal.getMssState();
    }

    public String getApVendorInfo() {
        return this.mSupplicantStaIfaceHal.getApVendorInfo();
    }

    public void gameKOGAdjustSpeed(int freq, int mode) {
        Log.d(this.mTAG, "gameKOGAdjustSpeed entered: " + freq + " mode: " + mode);
        gameKOGAdjustSpeedNative(freq, mode);
    }

    public boolean setPwrBoost(int enable) {
        Log.d(this.mTAG, "pwr:setPwrBoost entered");
        if (hwSetPwrBoostNative(enable) == 0) {
            return true;
        }
        return false;
    }
}
