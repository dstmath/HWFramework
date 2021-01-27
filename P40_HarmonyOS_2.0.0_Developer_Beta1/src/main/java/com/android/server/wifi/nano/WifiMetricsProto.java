package com.android.server.wifi.nano;

import android.util.JlogConstants;
import com.android.framework.protobuf.nano.CodedInputByteBufferNano;
import com.android.framework.protobuf.nano.CodedOutputByteBufferNano;
import com.android.framework.protobuf.nano.InternalNano;
import com.android.framework.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.framework.protobuf.nano.MessageNano;
import com.android.framework.protobuf.nano.WireFormatNano;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.telephony.RILConstants;
import java.io.IOException;

public interface WifiMetricsProto {

    public static final class WifiLog extends MessageNano {
        public static final int FAILURE_WIFI_DISABLED = 4;
        public static final int SCAN_FAILURE_INTERRUPTED = 2;
        public static final int SCAN_FAILURE_INVALID_CONFIGURATION = 3;
        public static final int SCAN_SUCCESS = 1;
        public static final int SCAN_UNKNOWN = 0;
        public static final int WIFI_ASSOCIATED = 3;
        public static final int WIFI_DISABLED = 1;
        public static final int WIFI_DISCONNECTED = 2;
        public static final int WIFI_UNKNOWN = 0;
        private static volatile WifiLog[] _emptyArray;
        public AlertReasonCount[] alertReasonCount;
        public NumConnectableNetworksBucket[] availableOpenBssidsInScanHistogram;
        public NumConnectableNetworksBucket[] availableOpenOrSavedBssidsInScanHistogram;
        public NumConnectableNetworksBucket[] availableOpenOrSavedSsidsInScanHistogram;
        public NumConnectableNetworksBucket[] availableOpenSsidsInScanHistogram;
        public NumConnectableNetworksBucket[] availableSavedBssidsInScanHistogram;
        public NumConnectableNetworksBucket[] availableSavedPasspointProviderBssidsInScanHistogram;
        public NumConnectableNetworksBucket[] availableSavedPasspointProviderProfilesInScanHistogram;
        public NumConnectableNetworksBucket[] availableSavedSsidsInScanHistogram;
        public WifiSystemStateEntry[] backgroundScanRequestState;
        public ScanReturnEntry[] backgroundScanReturnEntries;
        public ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationActionCount;
        public ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationCount;
        public ConnectionEvent[] connectionEvent;
        public ExperimentValues experimentValues;
        public int fullBandAllSingleScanListenerResults;
        public String hardwareRevision;
        public PasspointProfileTypeCount[] installedPasspointProfileTypeForR1;
        public PasspointProfileTypeCount[] installedPasspointProfileTypeForR2;
        public boolean isLocationEnabled;
        public boolean isMacRandomizationOn;
        public boolean isScanningAlwaysEnabled;
        public boolean isWifiNetworksAvailableNotificationOn;
        public LinkProbeStats linkProbeStats;
        public LinkSpeedCount[] linkSpeedCounts;
        public DeviceMobilityStatePnoScanStats[] mobilityStatePnoStatsList;
        public NetworkSelectionExperimentDecisions[] networkSelectionExperimentDecisionsList;
        public int numAddOrUpdateNetworkCalls;
        public int numBackgroundScans;
        public int numClientInterfaceDown;
        public int numConnectivityOneshotScans;
        public int numConnectivityWatchdogBackgroundBad;
        public int numConnectivityWatchdogBackgroundGood;
        public int numConnectivityWatchdogPnoBad;
        public int numConnectivityWatchdogPnoGood;
        public int numEmptyScanResults;
        public int numEnableNetworkCalls;
        public int numEnhancedOpenNetworkScanResults;
        public int numEnhancedOpenNetworks;
        public int numExternalAppOneshotScanRequests;
        public int numExternalBackgroundAppOneshotScanRequestsThrottled;
        public int numExternalForegroundAppOneshotScanRequestsThrottled;
        public int numHalCrashes;
        public int numHiddenNetworkScanResults;
        public int numHiddenNetworks;
        public int numHostapdCrashes;
        public int numHotspot2R1NetworkScanResults;
        public int numHotspot2R2NetworkScanResults;
        public int numLastResortWatchdogAvailableNetworksTotal;
        public int numLastResortWatchdogBadAssociationNetworksTotal;
        public int numLastResortWatchdogBadAuthenticationNetworksTotal;
        public int numLastResortWatchdogBadDhcpNetworksTotal;
        public int numLastResortWatchdogBadOtherNetworksTotal;
        public int numLastResortWatchdogSuccesses;
        public int numLastResortWatchdogTriggers;
        public int numLastResortWatchdogTriggersWithBadAssociation;
        public int numLastResortWatchdogTriggersWithBadAuthentication;
        public int numLastResortWatchdogTriggersWithBadDhcp;
        public int numLastResortWatchdogTriggersWithBadOther;
        public int numLegacyEnterpriseNetworkScanResults;
        public int numLegacyEnterpriseNetworks;
        public int numLegacyPersonalNetworkScanResults;
        public int numLegacyPersonalNetworks;
        public int numNetworksAddedByApps;
        public int numNetworksAddedByUser;
        public int numNonEmptyScanResults;
        public int numOneshotHasDfsChannelScans;
        public int numOneshotScans;
        public int numOpenNetworkConnectMessageFailedToSend;
        public int numOpenNetworkRecommendationUpdates;
        public int numOpenNetworkScanResults;
        public int numOpenNetworks;
        public int numPasspointNetworks;
        public int numPasspointProviderInstallSuccess;
        public int numPasspointProviderInstallation;
        public int numPasspointProviderUninstallSuccess;
        public int numPasspointProviderUninstallation;
        public int numPasspointProviders;
        public int numPasspointProvidersSuccessfullyConnected;
        public int numRadioModeChangeToDbs;
        public int numRadioModeChangeToMcc;
        public int numRadioModeChangeToSbs;
        public int numRadioModeChangeToScc;
        public int numSarSensorRegistrationFailures;
        public int numSavedNetworks;
        public int numSavedNetworksWithMacRandomization;
        public int numScans;
        public int numSetupClientInterfaceFailureDueToHal;
        public int numSetupClientInterfaceFailureDueToSupplicant;
        public int numSetupClientInterfaceFailureDueToWificond;
        public int numSetupSoftApInterfaceFailureDueToHal;
        public int numSetupSoftApInterfaceFailureDueToHostapd;
        public int numSetupSoftApInterfaceFailureDueToWificond;
        public int numSoftApInterfaceDown;
        public int numSoftApUserBandPreferenceUnsatisfied;
        public int numSupplicantCrashes;
        public int numTotalScanResults;
        public int numWifiToggledViaAirplane;
        public int numWifiToggledViaSettings;
        public int numWificondCrashes;
        public int numWpa3EnterpriseNetworkScanResults;
        public int numWpa3EnterpriseNetworks;
        public int numWpa3PersonalNetworkScanResults;
        public int numWpa3PersonalNetworks;
        public NumConnectableNetworksBucket[] observed80211McSupportingApsInScanHistogram;
        public NumConnectableNetworksBucket[] observedHotspotR1ApsInScanHistogram;
        public NumConnectableNetworksBucket[] observedHotspotR1ApsPerEssInScanHistogram;
        public NumConnectableNetworksBucket[] observedHotspotR1EssInScanHistogram;
        public NumConnectableNetworksBucket[] observedHotspotR2ApsInScanHistogram;
        public NumConnectableNetworksBucket[] observedHotspotR2ApsPerEssInScanHistogram;
        public NumConnectableNetworksBucket[] observedHotspotR2EssInScanHistogram;
        public int openNetworkRecommenderBlacklistSize;
        public int partialAllSingleScanListenerResults;
        public PasspointProvisionStats passpointProvisionStats;
        public PnoScanMetrics pnoScanMetrics;
        public int recordDurationSec;
        public RssiPollCount[] rssiPollDeltaCount;
        public RssiPollCount[] rssiPollRssiCount;
        public ScanReturnEntry[] scanReturnEntries;
        public String scoreExperimentId;
        public SoftApConnectedClientsEvent[] softApConnectedClientsEventsLocalOnly;
        public SoftApConnectedClientsEvent[] softApConnectedClientsEventsTethered;
        public SoftApDurationBucket[] softApDuration;
        public SoftApReturnCodeCount[] softApReturnCode;
        public StaEvent[] staEventList;
        public NumConnectableNetworksBucket[] totalBssidsInScanHistogram;
        public NumConnectableNetworksBucket[] totalSsidsInScanHistogram;
        public long watchdogTotalConnectionFailureCountAfterTrigger;
        public long watchdogTriggerToConnectionSuccessDurationMs;
        public WifiAwareLog wifiAwareLog;
        public WifiConfigStoreIO wifiConfigStoreIo;
        public WifiDppLog wifiDppLog;
        public WifiIsUnusableEvent[] wifiIsUnusableEventList;
        public WifiLinkLayerUsageStats wifiLinkLayerUsageStats;
        public WifiLockStats wifiLockStats;
        public WifiNetworkRequestApiLog wifiNetworkRequestApiLog;
        public WifiNetworkSuggestionApiLog wifiNetworkSuggestionApiLog;
        public WifiP2pStats wifiP2PStats;
        public WifiPowerStats wifiPowerStats;
        public WifiRadioUsage wifiRadioUsage;
        public WifiRttLog wifiRttLog;
        public WifiScoreCount[] wifiScoreCount;
        public WifiSystemStateEntry[] wifiSystemStateEntries;
        public WifiToggleStats wifiToggleStats;
        public WifiUsabilityScoreCount[] wifiUsabilityScoreCount;
        public WifiUsabilityStats[] wifiUsabilityStatsList;
        public WifiWakeStats wifiWakeStats;
        public WpsMetrics wpsMetrics;

        public static final class ScanReturnEntry extends MessageNano {
            private static volatile ScanReturnEntry[] _emptyArray;
            public int scanResultsCount;
            public int scanReturnCode;

            public static ScanReturnEntry[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ScanReturnEntry[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ScanReturnEntry() {
                clear();
            }

            public ScanReturnEntry clear() {
                this.scanReturnCode = 0;
                this.scanResultsCount = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.scanReturnCode;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.scanResultsCount;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.scanReturnCode;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.scanResultsCount;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public ScanReturnEntry mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4) {
                            this.scanReturnCode = value;
                        }
                    } else if (tag == 16) {
                        this.scanResultsCount = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static ScanReturnEntry parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ScanReturnEntry) MessageNano.mergeFrom(new ScanReturnEntry(), data);
            }

            public static ScanReturnEntry parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ScanReturnEntry().mergeFrom(input);
            }
        }

        public static final class WifiSystemStateEntry extends MessageNano {
            private static volatile WifiSystemStateEntry[] _emptyArray;
            public boolean isScreenOn;
            public int wifiState;
            public int wifiStateCount;

            public static WifiSystemStateEntry[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new WifiSystemStateEntry[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public WifiSystemStateEntry() {
                clear();
            }

            public WifiSystemStateEntry clear() {
                this.wifiState = 0;
                this.wifiStateCount = 0;
                this.isScreenOn = false;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.wifiState;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.wifiStateCount;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                boolean z = this.isScreenOn;
                if (z) {
                    output.writeBool(3, z);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.wifiState;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.wifiStateCount;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                boolean z = this.isScreenOn;
                if (z) {
                    return size + CodedOutputByteBufferNano.computeBoolSize(3, z);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public WifiSystemStateEntry mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2 || value == 3) {
                            this.wifiState = value;
                        }
                    } else if (tag == 16) {
                        this.wifiStateCount = input.readInt32();
                    } else if (tag == 24) {
                        this.isScreenOn = input.readBool();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static WifiSystemStateEntry parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (WifiSystemStateEntry) MessageNano.mergeFrom(new WifiSystemStateEntry(), data);
            }

            public static WifiSystemStateEntry parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new WifiSystemStateEntry().mergeFrom(input);
            }
        }

        public static WifiLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiLog() {
            clear();
        }

        public WifiLog clear() {
            this.connectionEvent = ConnectionEvent.emptyArray();
            this.numSavedNetworks = 0;
            this.numOpenNetworks = 0;
            this.numLegacyPersonalNetworks = 0;
            this.numLegacyEnterpriseNetworks = 0;
            this.isLocationEnabled = false;
            this.isScanningAlwaysEnabled = false;
            this.numWifiToggledViaSettings = 0;
            this.numWifiToggledViaAirplane = 0;
            this.numNetworksAddedByUser = 0;
            this.numNetworksAddedByApps = 0;
            this.numEmptyScanResults = 0;
            this.numNonEmptyScanResults = 0;
            this.numOneshotScans = 0;
            this.numBackgroundScans = 0;
            this.scanReturnEntries = ScanReturnEntry.emptyArray();
            this.wifiSystemStateEntries = WifiSystemStateEntry.emptyArray();
            this.backgroundScanReturnEntries = ScanReturnEntry.emptyArray();
            this.backgroundScanRequestState = WifiSystemStateEntry.emptyArray();
            this.numLastResortWatchdogTriggers = 0;
            this.numLastResortWatchdogBadAssociationNetworksTotal = 0;
            this.numLastResortWatchdogBadAuthenticationNetworksTotal = 0;
            this.numLastResortWatchdogBadDhcpNetworksTotal = 0;
            this.numLastResortWatchdogBadOtherNetworksTotal = 0;
            this.numLastResortWatchdogAvailableNetworksTotal = 0;
            this.numLastResortWatchdogTriggersWithBadAssociation = 0;
            this.numLastResortWatchdogTriggersWithBadAuthentication = 0;
            this.numLastResortWatchdogTriggersWithBadDhcp = 0;
            this.numLastResortWatchdogTriggersWithBadOther = 0;
            this.numConnectivityWatchdogPnoGood = 0;
            this.numConnectivityWatchdogPnoBad = 0;
            this.numConnectivityWatchdogBackgroundGood = 0;
            this.numConnectivityWatchdogBackgroundBad = 0;
            this.recordDurationSec = 0;
            this.rssiPollRssiCount = RssiPollCount.emptyArray();
            this.numLastResortWatchdogSuccesses = 0;
            this.numHiddenNetworks = 0;
            this.numPasspointNetworks = 0;
            this.numTotalScanResults = 0;
            this.numOpenNetworkScanResults = 0;
            this.numLegacyPersonalNetworkScanResults = 0;
            this.numLegacyEnterpriseNetworkScanResults = 0;
            this.numHiddenNetworkScanResults = 0;
            this.numHotspot2R1NetworkScanResults = 0;
            this.numHotspot2R2NetworkScanResults = 0;
            this.numScans = 0;
            this.alertReasonCount = AlertReasonCount.emptyArray();
            this.wifiScoreCount = WifiScoreCount.emptyArray();
            this.softApDuration = SoftApDurationBucket.emptyArray();
            this.softApReturnCode = SoftApReturnCodeCount.emptyArray();
            this.rssiPollDeltaCount = RssiPollCount.emptyArray();
            this.staEventList = StaEvent.emptyArray();
            this.numHalCrashes = 0;
            this.numWificondCrashes = 0;
            this.numSetupClientInterfaceFailureDueToHal = 0;
            this.numSetupClientInterfaceFailureDueToWificond = 0;
            this.wifiAwareLog = null;
            this.numPasspointProviders = 0;
            this.numPasspointProviderInstallation = 0;
            this.numPasspointProviderInstallSuccess = 0;
            this.numPasspointProviderUninstallation = 0;
            this.numPasspointProviderUninstallSuccess = 0;
            this.numPasspointProvidersSuccessfullyConnected = 0;
            this.totalSsidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.totalBssidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableOpenSsidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableOpenBssidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableSavedSsidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableSavedBssidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableOpenOrSavedSsidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableOpenOrSavedBssidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableSavedPasspointProviderProfilesInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.availableSavedPasspointProviderBssidsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.fullBandAllSingleScanListenerResults = 0;
            this.partialAllSingleScanListenerResults = 0;
            this.pnoScanMetrics = null;
            this.connectToNetworkNotificationCount = ConnectToNetworkNotificationAndActionCount.emptyArray();
            this.connectToNetworkNotificationActionCount = ConnectToNetworkNotificationAndActionCount.emptyArray();
            this.openNetworkRecommenderBlacklistSize = 0;
            this.isWifiNetworksAvailableNotificationOn = false;
            this.numOpenNetworkRecommendationUpdates = 0;
            this.numOpenNetworkConnectMessageFailedToSend = 0;
            this.observedHotspotR1ApsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.observedHotspotR2ApsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.observedHotspotR1EssInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.observedHotspotR2EssInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.observedHotspotR1ApsPerEssInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.observedHotspotR2ApsPerEssInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.softApConnectedClientsEventsTethered = SoftApConnectedClientsEvent.emptyArray();
            this.softApConnectedClientsEventsLocalOnly = SoftApConnectedClientsEvent.emptyArray();
            this.wpsMetrics = null;
            this.wifiPowerStats = null;
            this.numConnectivityOneshotScans = 0;
            this.wifiWakeStats = null;
            this.observed80211McSupportingApsInScanHistogram = NumConnectableNetworksBucket.emptyArray();
            this.numSupplicantCrashes = 0;
            this.numHostapdCrashes = 0;
            this.numSetupClientInterfaceFailureDueToSupplicant = 0;
            this.numSetupSoftApInterfaceFailureDueToHal = 0;
            this.numSetupSoftApInterfaceFailureDueToWificond = 0;
            this.numSetupSoftApInterfaceFailureDueToHostapd = 0;
            this.numClientInterfaceDown = 0;
            this.numSoftApInterfaceDown = 0;
            this.numExternalAppOneshotScanRequests = 0;
            this.numExternalForegroundAppOneshotScanRequestsThrottled = 0;
            this.numExternalBackgroundAppOneshotScanRequestsThrottled = 0;
            this.watchdogTriggerToConnectionSuccessDurationMs = -1;
            this.watchdogTotalConnectionFailureCountAfterTrigger = 0;
            this.numOneshotHasDfsChannelScans = 0;
            this.wifiRttLog = null;
            this.isMacRandomizationOn = false;
            this.numRadioModeChangeToMcc = 0;
            this.numRadioModeChangeToScc = 0;
            this.numRadioModeChangeToSbs = 0;
            this.numRadioModeChangeToDbs = 0;
            this.numSoftApUserBandPreferenceUnsatisfied = 0;
            this.scoreExperimentId = "";
            this.wifiRadioUsage = null;
            this.experimentValues = null;
            this.wifiIsUnusableEventList = WifiIsUnusableEvent.emptyArray();
            this.linkSpeedCounts = LinkSpeedCount.emptyArray();
            this.numSarSensorRegistrationFailures = 0;
            this.hardwareRevision = "";
            this.wifiLinkLayerUsageStats = null;
            this.wifiUsabilityStatsList = WifiUsabilityStats.emptyArray();
            this.wifiUsabilityScoreCount = WifiUsabilityScoreCount.emptyArray();
            this.mobilityStatePnoStatsList = DeviceMobilityStatePnoScanStats.emptyArray();
            this.wifiP2PStats = null;
            this.wifiDppLog = null;
            this.numEnhancedOpenNetworks = 0;
            this.numWpa3PersonalNetworks = 0;
            this.numWpa3EnterpriseNetworks = 0;
            this.numEnhancedOpenNetworkScanResults = 0;
            this.numWpa3PersonalNetworkScanResults = 0;
            this.numWpa3EnterpriseNetworkScanResults = 0;
            this.wifiConfigStoreIo = null;
            this.numSavedNetworksWithMacRandomization = 0;
            this.linkProbeStats = null;
            this.networkSelectionExperimentDecisionsList = NetworkSelectionExperimentDecisions.emptyArray();
            this.wifiNetworkRequestApiLog = null;
            this.wifiNetworkSuggestionApiLog = null;
            this.wifiLockStats = null;
            this.wifiToggleStats = null;
            this.numAddOrUpdateNetworkCalls = 0;
            this.numEnableNetworkCalls = 0;
            this.passpointProvisionStats = null;
            this.installedPasspointProfileTypeForR1 = PasspointProfileTypeCount.emptyArray();
            this.installedPasspointProfileTypeForR2 = PasspointProfileTypeCount.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            ConnectionEvent[] connectionEventArr = this.connectionEvent;
            if (connectionEventArr != null && connectionEventArr.length > 0) {
                int i = 0;
                while (true) {
                    ConnectionEvent[] connectionEventArr2 = this.connectionEvent;
                    if (i >= connectionEventArr2.length) {
                        break;
                    }
                    ConnectionEvent element = connectionEventArr2[i];
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                    i++;
                }
            }
            int i2 = this.numSavedNetworks;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.numOpenNetworks;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numLegacyPersonalNetworks;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.numLegacyEnterpriseNetworks;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            boolean z = this.isLocationEnabled;
            if (z) {
                output.writeBool(6, z);
            }
            boolean z2 = this.isScanningAlwaysEnabled;
            if (z2) {
                output.writeBool(7, z2);
            }
            int i6 = this.numWifiToggledViaSettings;
            if (i6 != 0) {
                output.writeInt32(8, i6);
            }
            int i7 = this.numWifiToggledViaAirplane;
            if (i7 != 0) {
                output.writeInt32(9, i7);
            }
            int i8 = this.numNetworksAddedByUser;
            if (i8 != 0) {
                output.writeInt32(10, i8);
            }
            int i9 = this.numNetworksAddedByApps;
            if (i9 != 0) {
                output.writeInt32(11, i9);
            }
            int i10 = this.numEmptyScanResults;
            if (i10 != 0) {
                output.writeInt32(12, i10);
            }
            int i11 = this.numNonEmptyScanResults;
            if (i11 != 0) {
                output.writeInt32(13, i11);
            }
            int i12 = this.numOneshotScans;
            if (i12 != 0) {
                output.writeInt32(14, i12);
            }
            int i13 = this.numBackgroundScans;
            if (i13 != 0) {
                output.writeInt32(15, i13);
            }
            ScanReturnEntry[] scanReturnEntryArr = this.scanReturnEntries;
            if (scanReturnEntryArr != null && scanReturnEntryArr.length > 0) {
                int i14 = 0;
                while (true) {
                    ScanReturnEntry[] scanReturnEntryArr2 = this.scanReturnEntries;
                    if (i14 >= scanReturnEntryArr2.length) {
                        break;
                    }
                    ScanReturnEntry element2 = scanReturnEntryArr2[i14];
                    if (element2 != null) {
                        output.writeMessage(16, element2);
                    }
                    i14++;
                }
            }
            WifiSystemStateEntry[] wifiSystemStateEntryArr = this.wifiSystemStateEntries;
            if (wifiSystemStateEntryArr != null && wifiSystemStateEntryArr.length > 0) {
                int i15 = 0;
                while (true) {
                    WifiSystemStateEntry[] wifiSystemStateEntryArr2 = this.wifiSystemStateEntries;
                    if (i15 >= wifiSystemStateEntryArr2.length) {
                        break;
                    }
                    WifiSystemStateEntry element3 = wifiSystemStateEntryArr2[i15];
                    if (element3 != null) {
                        output.writeMessage(17, element3);
                    }
                    i15++;
                }
            }
            ScanReturnEntry[] scanReturnEntryArr3 = this.backgroundScanReturnEntries;
            if (scanReturnEntryArr3 != null && scanReturnEntryArr3.length > 0) {
                int i16 = 0;
                while (true) {
                    ScanReturnEntry[] scanReturnEntryArr4 = this.backgroundScanReturnEntries;
                    if (i16 >= scanReturnEntryArr4.length) {
                        break;
                    }
                    ScanReturnEntry element4 = scanReturnEntryArr4[i16];
                    if (element4 != null) {
                        output.writeMessage(18, element4);
                    }
                    i16++;
                }
            }
            WifiSystemStateEntry[] wifiSystemStateEntryArr3 = this.backgroundScanRequestState;
            if (wifiSystemStateEntryArr3 != null && wifiSystemStateEntryArr3.length > 0) {
                int i17 = 0;
                while (true) {
                    WifiSystemStateEntry[] wifiSystemStateEntryArr4 = this.backgroundScanRequestState;
                    if (i17 >= wifiSystemStateEntryArr4.length) {
                        break;
                    }
                    WifiSystemStateEntry element5 = wifiSystemStateEntryArr4[i17];
                    if (element5 != null) {
                        output.writeMessage(19, element5);
                    }
                    i17++;
                }
            }
            int i18 = this.numLastResortWatchdogTriggers;
            if (i18 != 0) {
                output.writeInt32(20, i18);
            }
            int i19 = this.numLastResortWatchdogBadAssociationNetworksTotal;
            if (i19 != 0) {
                output.writeInt32(21, i19);
            }
            int i20 = this.numLastResortWatchdogBadAuthenticationNetworksTotal;
            if (i20 != 0) {
                output.writeInt32(22, i20);
            }
            int i21 = this.numLastResortWatchdogBadDhcpNetworksTotal;
            if (i21 != 0) {
                output.writeInt32(23, i21);
            }
            int i22 = this.numLastResortWatchdogBadOtherNetworksTotal;
            if (i22 != 0) {
                output.writeInt32(24, i22);
            }
            int i23 = this.numLastResortWatchdogAvailableNetworksTotal;
            if (i23 != 0) {
                output.writeInt32(25, i23);
            }
            int i24 = this.numLastResortWatchdogTriggersWithBadAssociation;
            if (i24 != 0) {
                output.writeInt32(26, i24);
            }
            int i25 = this.numLastResortWatchdogTriggersWithBadAuthentication;
            if (i25 != 0) {
                output.writeInt32(27, i25);
            }
            int i26 = this.numLastResortWatchdogTriggersWithBadDhcp;
            if (i26 != 0) {
                output.writeInt32(28, i26);
            }
            int i27 = this.numLastResortWatchdogTriggersWithBadOther;
            if (i27 != 0) {
                output.writeInt32(29, i27);
            }
            int i28 = this.numConnectivityWatchdogPnoGood;
            if (i28 != 0) {
                output.writeInt32(30, i28);
            }
            int i29 = this.numConnectivityWatchdogPnoBad;
            if (i29 != 0) {
                output.writeInt32(31, i29);
            }
            int i30 = this.numConnectivityWatchdogBackgroundGood;
            if (i30 != 0) {
                output.writeInt32(32, i30);
            }
            int i31 = this.numConnectivityWatchdogBackgroundBad;
            if (i31 != 0) {
                output.writeInt32(33, i31);
            }
            int i32 = this.recordDurationSec;
            if (i32 != 0) {
                output.writeInt32(34, i32);
            }
            RssiPollCount[] rssiPollCountArr = this.rssiPollRssiCount;
            if (rssiPollCountArr != null && rssiPollCountArr.length > 0) {
                int i33 = 0;
                while (true) {
                    RssiPollCount[] rssiPollCountArr2 = this.rssiPollRssiCount;
                    if (i33 >= rssiPollCountArr2.length) {
                        break;
                    }
                    RssiPollCount element6 = rssiPollCountArr2[i33];
                    if (element6 != null) {
                        output.writeMessage(35, element6);
                    }
                    i33++;
                }
            }
            int i34 = this.numLastResortWatchdogSuccesses;
            if (i34 != 0) {
                output.writeInt32(36, i34);
            }
            int i35 = this.numHiddenNetworks;
            if (i35 != 0) {
                output.writeInt32(37, i35);
            }
            int i36 = this.numPasspointNetworks;
            if (i36 != 0) {
                output.writeInt32(38, i36);
            }
            int i37 = this.numTotalScanResults;
            if (i37 != 0) {
                output.writeInt32(39, i37);
            }
            int i38 = this.numOpenNetworkScanResults;
            if (i38 != 0) {
                output.writeInt32(40, i38);
            }
            int i39 = this.numLegacyPersonalNetworkScanResults;
            if (i39 != 0) {
                output.writeInt32(41, i39);
            }
            int i40 = this.numLegacyEnterpriseNetworkScanResults;
            if (i40 != 0) {
                output.writeInt32(42, i40);
            }
            int i41 = this.numHiddenNetworkScanResults;
            if (i41 != 0) {
                output.writeInt32(43, i41);
            }
            int i42 = this.numHotspot2R1NetworkScanResults;
            if (i42 != 0) {
                output.writeInt32(44, i42);
            }
            int i43 = this.numHotspot2R2NetworkScanResults;
            if (i43 != 0) {
                output.writeInt32(45, i43);
            }
            int i44 = this.numScans;
            if (i44 != 0) {
                output.writeInt32(46, i44);
            }
            AlertReasonCount[] alertReasonCountArr = this.alertReasonCount;
            if (alertReasonCountArr != null && alertReasonCountArr.length > 0) {
                int i45 = 0;
                while (true) {
                    AlertReasonCount[] alertReasonCountArr2 = this.alertReasonCount;
                    if (i45 >= alertReasonCountArr2.length) {
                        break;
                    }
                    AlertReasonCount element7 = alertReasonCountArr2[i45];
                    if (element7 != null) {
                        output.writeMessage(47, element7);
                    }
                    i45++;
                }
            }
            WifiScoreCount[] wifiScoreCountArr = this.wifiScoreCount;
            if (wifiScoreCountArr != null && wifiScoreCountArr.length > 0) {
                int i46 = 0;
                while (true) {
                    WifiScoreCount[] wifiScoreCountArr2 = this.wifiScoreCount;
                    if (i46 >= wifiScoreCountArr2.length) {
                        break;
                    }
                    WifiScoreCount element8 = wifiScoreCountArr2[i46];
                    if (element8 != null) {
                        output.writeMessage(48, element8);
                    }
                    i46++;
                }
            }
            SoftApDurationBucket[] softApDurationBucketArr = this.softApDuration;
            if (softApDurationBucketArr != null && softApDurationBucketArr.length > 0) {
                int i47 = 0;
                while (true) {
                    SoftApDurationBucket[] softApDurationBucketArr2 = this.softApDuration;
                    if (i47 >= softApDurationBucketArr2.length) {
                        break;
                    }
                    SoftApDurationBucket element9 = softApDurationBucketArr2[i47];
                    if (element9 != null) {
                        output.writeMessage(49, element9);
                    }
                    i47++;
                }
            }
            SoftApReturnCodeCount[] softApReturnCodeCountArr = this.softApReturnCode;
            if (softApReturnCodeCountArr != null && softApReturnCodeCountArr.length > 0) {
                int i48 = 0;
                while (true) {
                    SoftApReturnCodeCount[] softApReturnCodeCountArr2 = this.softApReturnCode;
                    if (i48 >= softApReturnCodeCountArr2.length) {
                        break;
                    }
                    SoftApReturnCodeCount element10 = softApReturnCodeCountArr2[i48];
                    if (element10 != null) {
                        output.writeMessage(50, element10);
                    }
                    i48++;
                }
            }
            RssiPollCount[] rssiPollCountArr3 = this.rssiPollDeltaCount;
            if (rssiPollCountArr3 != null && rssiPollCountArr3.length > 0) {
                int i49 = 0;
                while (true) {
                    RssiPollCount[] rssiPollCountArr4 = this.rssiPollDeltaCount;
                    if (i49 >= rssiPollCountArr4.length) {
                        break;
                    }
                    RssiPollCount element11 = rssiPollCountArr4[i49];
                    if (element11 != null) {
                        output.writeMessage(51, element11);
                    }
                    i49++;
                }
            }
            StaEvent[] staEventArr = this.staEventList;
            if (staEventArr != null && staEventArr.length > 0) {
                int i50 = 0;
                while (true) {
                    StaEvent[] staEventArr2 = this.staEventList;
                    if (i50 >= staEventArr2.length) {
                        break;
                    }
                    StaEvent element12 = staEventArr2[i50];
                    if (element12 != null) {
                        output.writeMessage(52, element12);
                    }
                    i50++;
                }
            }
            int i51 = this.numHalCrashes;
            if (i51 != 0) {
                output.writeInt32(53, i51);
            }
            int i52 = this.numWificondCrashes;
            if (i52 != 0) {
                output.writeInt32(54, i52);
            }
            int i53 = this.numSetupClientInterfaceFailureDueToHal;
            if (i53 != 0) {
                output.writeInt32(55, i53);
            }
            int i54 = this.numSetupClientInterfaceFailureDueToWificond;
            if (i54 != 0) {
                output.writeInt32(56, i54);
            }
            WifiAwareLog wifiAwareLog2 = this.wifiAwareLog;
            if (wifiAwareLog2 != null) {
                output.writeMessage(57, wifiAwareLog2);
            }
            int i55 = this.numPasspointProviders;
            if (i55 != 0) {
                output.writeInt32(58, i55);
            }
            int i56 = this.numPasspointProviderInstallation;
            if (i56 != 0) {
                output.writeInt32(59, i56);
            }
            int i57 = this.numPasspointProviderInstallSuccess;
            if (i57 != 0) {
                output.writeInt32(60, i57);
            }
            int i58 = this.numPasspointProviderUninstallation;
            if (i58 != 0) {
                output.writeInt32(61, i58);
            }
            int i59 = this.numPasspointProviderUninstallSuccess;
            if (i59 != 0) {
                output.writeInt32(62, i59);
            }
            int i60 = this.numPasspointProvidersSuccessfullyConnected;
            if (i60 != 0) {
                output.writeInt32(63, i60);
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr = this.totalSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr != null && numConnectableNetworksBucketArr.length > 0) {
                int i61 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr2 = this.totalSsidsInScanHistogram;
                    if (i61 >= numConnectableNetworksBucketArr2.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element13 = numConnectableNetworksBucketArr2[i61];
                    if (element13 != null) {
                        output.writeMessage(64, element13);
                    }
                    i61++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr3 = this.totalBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr3 != null && numConnectableNetworksBucketArr3.length > 0) {
                int i62 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr4 = this.totalBssidsInScanHistogram;
                    if (i62 >= numConnectableNetworksBucketArr4.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element14 = numConnectableNetworksBucketArr4[i62];
                    if (element14 != null) {
                        output.writeMessage(65, element14);
                    }
                    i62++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr5 = this.availableOpenSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr5 != null && numConnectableNetworksBucketArr5.length > 0) {
                int i63 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr6 = this.availableOpenSsidsInScanHistogram;
                    if (i63 >= numConnectableNetworksBucketArr6.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element15 = numConnectableNetworksBucketArr6[i63];
                    if (element15 != null) {
                        output.writeMessage(66, element15);
                    }
                    i63++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr7 = this.availableOpenBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr7 != null && numConnectableNetworksBucketArr7.length > 0) {
                int i64 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr8 = this.availableOpenBssidsInScanHistogram;
                    if (i64 >= numConnectableNetworksBucketArr8.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element16 = numConnectableNetworksBucketArr8[i64];
                    if (element16 != null) {
                        output.writeMessage(67, element16);
                    }
                    i64++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr9 = this.availableSavedSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr9 != null && numConnectableNetworksBucketArr9.length > 0) {
                int i65 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr10 = this.availableSavedSsidsInScanHistogram;
                    if (i65 >= numConnectableNetworksBucketArr10.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element17 = numConnectableNetworksBucketArr10[i65];
                    if (element17 != null) {
                        output.writeMessage(68, element17);
                    }
                    i65++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr11 = this.availableSavedBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr11 != null && numConnectableNetworksBucketArr11.length > 0) {
                int i66 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr12 = this.availableSavedBssidsInScanHistogram;
                    if (i66 >= numConnectableNetworksBucketArr12.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element18 = numConnectableNetworksBucketArr12[i66];
                    if (element18 != null) {
                        output.writeMessage(69, element18);
                    }
                    i66++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr13 = this.availableOpenOrSavedSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr13 != null && numConnectableNetworksBucketArr13.length > 0) {
                int i67 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr14 = this.availableOpenOrSavedSsidsInScanHistogram;
                    if (i67 >= numConnectableNetworksBucketArr14.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element19 = numConnectableNetworksBucketArr14[i67];
                    if (element19 != null) {
                        output.writeMessage(70, element19);
                    }
                    i67++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr15 = this.availableOpenOrSavedBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr15 != null && numConnectableNetworksBucketArr15.length > 0) {
                int i68 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr16 = this.availableOpenOrSavedBssidsInScanHistogram;
                    if (i68 >= numConnectableNetworksBucketArr16.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element20 = numConnectableNetworksBucketArr16[i68];
                    if (element20 != null) {
                        output.writeMessage(71, element20);
                    }
                    i68++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr17 = this.availableSavedPasspointProviderProfilesInScanHistogram;
            if (numConnectableNetworksBucketArr17 != null && numConnectableNetworksBucketArr17.length > 0) {
                int i69 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr18 = this.availableSavedPasspointProviderProfilesInScanHistogram;
                    if (i69 >= numConnectableNetworksBucketArr18.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element21 = numConnectableNetworksBucketArr18[i69];
                    if (element21 != null) {
                        output.writeMessage(72, element21);
                    }
                    i69++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr19 = this.availableSavedPasspointProviderBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr19 != null && numConnectableNetworksBucketArr19.length > 0) {
                int i70 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr20 = this.availableSavedPasspointProviderBssidsInScanHistogram;
                    if (i70 >= numConnectableNetworksBucketArr20.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element22 = numConnectableNetworksBucketArr20[i70];
                    if (element22 != null) {
                        output.writeMessage(73, element22);
                    }
                    i70++;
                }
            }
            int i71 = this.fullBandAllSingleScanListenerResults;
            if (i71 != 0) {
                output.writeInt32(74, i71);
            }
            int i72 = this.partialAllSingleScanListenerResults;
            if (i72 != 0) {
                output.writeInt32(75, i72);
            }
            PnoScanMetrics pnoScanMetrics2 = this.pnoScanMetrics;
            if (pnoScanMetrics2 != null) {
                output.writeMessage(76, pnoScanMetrics2);
            }
            ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr = this.connectToNetworkNotificationCount;
            if (connectToNetworkNotificationAndActionCountArr != null && connectToNetworkNotificationAndActionCountArr.length > 0) {
                int i73 = 0;
                while (true) {
                    ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr2 = this.connectToNetworkNotificationCount;
                    if (i73 >= connectToNetworkNotificationAndActionCountArr2.length) {
                        break;
                    }
                    ConnectToNetworkNotificationAndActionCount element23 = connectToNetworkNotificationAndActionCountArr2[i73];
                    if (element23 != null) {
                        output.writeMessage(77, element23);
                    }
                    i73++;
                }
            }
            ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr3 = this.connectToNetworkNotificationActionCount;
            if (connectToNetworkNotificationAndActionCountArr3 != null && connectToNetworkNotificationAndActionCountArr3.length > 0) {
                int i74 = 0;
                while (true) {
                    ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr4 = this.connectToNetworkNotificationActionCount;
                    if (i74 >= connectToNetworkNotificationAndActionCountArr4.length) {
                        break;
                    }
                    ConnectToNetworkNotificationAndActionCount element24 = connectToNetworkNotificationAndActionCountArr4[i74];
                    if (element24 != null) {
                        output.writeMessage(78, element24);
                    }
                    i74++;
                }
            }
            int i75 = this.openNetworkRecommenderBlacklistSize;
            if (i75 != 0) {
                output.writeInt32(79, i75);
            }
            boolean z3 = this.isWifiNetworksAvailableNotificationOn;
            if (z3) {
                output.writeBool(80, z3);
            }
            int i76 = this.numOpenNetworkRecommendationUpdates;
            if (i76 != 0) {
                output.writeInt32(81, i76);
            }
            int i77 = this.numOpenNetworkConnectMessageFailedToSend;
            if (i77 != 0) {
                output.writeInt32(82, i77);
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr21 = this.observedHotspotR1ApsInScanHistogram;
            if (numConnectableNetworksBucketArr21 != null && numConnectableNetworksBucketArr21.length > 0) {
                int i78 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr22 = this.observedHotspotR1ApsInScanHistogram;
                    if (i78 >= numConnectableNetworksBucketArr22.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element25 = numConnectableNetworksBucketArr22[i78];
                    if (element25 != null) {
                        output.writeMessage(83, element25);
                    }
                    i78++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr23 = this.observedHotspotR2ApsInScanHistogram;
            if (numConnectableNetworksBucketArr23 != null && numConnectableNetworksBucketArr23.length > 0) {
                int i79 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr24 = this.observedHotspotR2ApsInScanHistogram;
                    if (i79 >= numConnectableNetworksBucketArr24.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element26 = numConnectableNetworksBucketArr24[i79];
                    if (element26 != null) {
                        output.writeMessage(84, element26);
                    }
                    i79++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr25 = this.observedHotspotR1EssInScanHistogram;
            if (numConnectableNetworksBucketArr25 != null && numConnectableNetworksBucketArr25.length > 0) {
                int i80 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr26 = this.observedHotspotR1EssInScanHistogram;
                    if (i80 >= numConnectableNetworksBucketArr26.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element27 = numConnectableNetworksBucketArr26[i80];
                    if (element27 != null) {
                        output.writeMessage(85, element27);
                    }
                    i80++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr27 = this.observedHotspotR2EssInScanHistogram;
            if (numConnectableNetworksBucketArr27 != null && numConnectableNetworksBucketArr27.length > 0) {
                int i81 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr28 = this.observedHotspotR2EssInScanHistogram;
                    if (i81 >= numConnectableNetworksBucketArr28.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element28 = numConnectableNetworksBucketArr28[i81];
                    if (element28 != null) {
                        output.writeMessage(86, element28);
                    }
                    i81++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr29 = this.observedHotspotR1ApsPerEssInScanHistogram;
            if (numConnectableNetworksBucketArr29 != null && numConnectableNetworksBucketArr29.length > 0) {
                int i82 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr30 = this.observedHotspotR1ApsPerEssInScanHistogram;
                    if (i82 >= numConnectableNetworksBucketArr30.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element29 = numConnectableNetworksBucketArr30[i82];
                    if (element29 != null) {
                        output.writeMessage(87, element29);
                    }
                    i82++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr31 = this.observedHotspotR2ApsPerEssInScanHistogram;
            if (numConnectableNetworksBucketArr31 != null && numConnectableNetworksBucketArr31.length > 0) {
                int i83 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr32 = this.observedHotspotR2ApsPerEssInScanHistogram;
                    if (i83 >= numConnectableNetworksBucketArr32.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element30 = numConnectableNetworksBucketArr32[i83];
                    if (element30 != null) {
                        output.writeMessage(88, element30);
                    }
                    i83++;
                }
            }
            SoftApConnectedClientsEvent[] softApConnectedClientsEventArr = this.softApConnectedClientsEventsTethered;
            if (softApConnectedClientsEventArr != null && softApConnectedClientsEventArr.length > 0) {
                int i84 = 0;
                while (true) {
                    SoftApConnectedClientsEvent[] softApConnectedClientsEventArr2 = this.softApConnectedClientsEventsTethered;
                    if (i84 >= softApConnectedClientsEventArr2.length) {
                        break;
                    }
                    SoftApConnectedClientsEvent element31 = softApConnectedClientsEventArr2[i84];
                    if (element31 != null) {
                        output.writeMessage(89, element31);
                    }
                    i84++;
                }
            }
            SoftApConnectedClientsEvent[] softApConnectedClientsEventArr3 = this.softApConnectedClientsEventsLocalOnly;
            if (softApConnectedClientsEventArr3 != null && softApConnectedClientsEventArr3.length > 0) {
                int i85 = 0;
                while (true) {
                    SoftApConnectedClientsEvent[] softApConnectedClientsEventArr4 = this.softApConnectedClientsEventsLocalOnly;
                    if (i85 >= softApConnectedClientsEventArr4.length) {
                        break;
                    }
                    SoftApConnectedClientsEvent element32 = softApConnectedClientsEventArr4[i85];
                    if (element32 != null) {
                        output.writeMessage(90, element32);
                    }
                    i85++;
                }
            }
            WpsMetrics wpsMetrics2 = this.wpsMetrics;
            if (wpsMetrics2 != null) {
                output.writeMessage(91, wpsMetrics2);
            }
            WifiPowerStats wifiPowerStats2 = this.wifiPowerStats;
            if (wifiPowerStats2 != null) {
                output.writeMessage(92, wifiPowerStats2);
            }
            int i86 = this.numConnectivityOneshotScans;
            if (i86 != 0) {
                output.writeInt32(93, i86);
            }
            WifiWakeStats wifiWakeStats2 = this.wifiWakeStats;
            if (wifiWakeStats2 != null) {
                output.writeMessage(94, wifiWakeStats2);
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr33 = this.observed80211McSupportingApsInScanHistogram;
            if (numConnectableNetworksBucketArr33 != null && numConnectableNetworksBucketArr33.length > 0) {
                int i87 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr34 = this.observed80211McSupportingApsInScanHistogram;
                    if (i87 >= numConnectableNetworksBucketArr34.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element33 = numConnectableNetworksBucketArr34[i87];
                    if (element33 != null) {
                        output.writeMessage(95, element33);
                    }
                    i87++;
                }
            }
            int i88 = this.numSupplicantCrashes;
            if (i88 != 0) {
                output.writeInt32(96, i88);
            }
            int i89 = this.numHostapdCrashes;
            if (i89 != 0) {
                output.writeInt32(97, i89);
            }
            int i90 = this.numSetupClientInterfaceFailureDueToSupplicant;
            if (i90 != 0) {
                output.writeInt32(98, i90);
            }
            int i91 = this.numSetupSoftApInterfaceFailureDueToHal;
            if (i91 != 0) {
                output.writeInt32(99, i91);
            }
            int i92 = this.numSetupSoftApInterfaceFailureDueToWificond;
            if (i92 != 0) {
                output.writeInt32(100, i92);
            }
            int i93 = this.numSetupSoftApInterfaceFailureDueToHostapd;
            if (i93 != 0) {
                output.writeInt32(101, i93);
            }
            int i94 = this.numClientInterfaceDown;
            if (i94 != 0) {
                output.writeInt32(102, i94);
            }
            int i95 = this.numSoftApInterfaceDown;
            if (i95 != 0) {
                output.writeInt32(103, i95);
            }
            int i96 = this.numExternalAppOneshotScanRequests;
            if (i96 != 0) {
                output.writeInt32(104, i96);
            }
            int i97 = this.numExternalForegroundAppOneshotScanRequestsThrottled;
            if (i97 != 0) {
                output.writeInt32(105, i97);
            }
            int i98 = this.numExternalBackgroundAppOneshotScanRequestsThrottled;
            if (i98 != 0) {
                output.writeInt32(106, i98);
            }
            long j = this.watchdogTriggerToConnectionSuccessDurationMs;
            if (j != -1) {
                output.writeInt64(107, j);
            }
            long j2 = this.watchdogTotalConnectionFailureCountAfterTrigger;
            if (j2 != 0) {
                output.writeInt64(108, j2);
            }
            int i99 = this.numOneshotHasDfsChannelScans;
            if (i99 != 0) {
                output.writeInt32(109, i99);
            }
            WifiRttLog wifiRttLog2 = this.wifiRttLog;
            if (wifiRttLog2 != null) {
                output.writeMessage(110, wifiRttLog2);
            }
            boolean z4 = this.isMacRandomizationOn;
            if (z4) {
                output.writeBool(111, z4);
            }
            int i100 = this.numRadioModeChangeToMcc;
            if (i100 != 0) {
                output.writeInt32(112, i100);
            }
            int i101 = this.numRadioModeChangeToScc;
            if (i101 != 0) {
                output.writeInt32(113, i101);
            }
            int i102 = this.numRadioModeChangeToSbs;
            if (i102 != 0) {
                output.writeInt32(114, i102);
            }
            int i103 = this.numRadioModeChangeToDbs;
            if (i103 != 0) {
                output.writeInt32(115, i103);
            }
            int i104 = this.numSoftApUserBandPreferenceUnsatisfied;
            if (i104 != 0) {
                output.writeInt32(116, i104);
            }
            if (!this.scoreExperimentId.equals("")) {
                output.writeString(117, this.scoreExperimentId);
            }
            WifiRadioUsage wifiRadioUsage2 = this.wifiRadioUsage;
            if (wifiRadioUsage2 != null) {
                output.writeMessage(118, wifiRadioUsage2);
            }
            ExperimentValues experimentValues2 = this.experimentValues;
            if (experimentValues2 != null) {
                output.writeMessage(119, experimentValues2);
            }
            WifiIsUnusableEvent[] wifiIsUnusableEventArr = this.wifiIsUnusableEventList;
            if (wifiIsUnusableEventArr != null && wifiIsUnusableEventArr.length > 0) {
                int i105 = 0;
                while (true) {
                    WifiIsUnusableEvent[] wifiIsUnusableEventArr2 = this.wifiIsUnusableEventList;
                    if (i105 >= wifiIsUnusableEventArr2.length) {
                        break;
                    }
                    WifiIsUnusableEvent element34 = wifiIsUnusableEventArr2[i105];
                    if (element34 != null) {
                        output.writeMessage(120, element34);
                    }
                    i105++;
                }
            }
            LinkSpeedCount[] linkSpeedCountArr = this.linkSpeedCounts;
            if (linkSpeedCountArr != null && linkSpeedCountArr.length > 0) {
                int i106 = 0;
                while (true) {
                    LinkSpeedCount[] linkSpeedCountArr2 = this.linkSpeedCounts;
                    if (i106 >= linkSpeedCountArr2.length) {
                        break;
                    }
                    LinkSpeedCount element35 = linkSpeedCountArr2[i106];
                    if (element35 != null) {
                        output.writeMessage(121, element35);
                    }
                    i106++;
                }
            }
            int i107 = this.numSarSensorRegistrationFailures;
            if (i107 != 0) {
                output.writeInt32(122, i107);
            }
            PasspointProfileTypeCount[] passpointProfileTypeCountArr = this.installedPasspointProfileTypeForR1;
            if (passpointProfileTypeCountArr != null && passpointProfileTypeCountArr.length > 0) {
                int i108 = 0;
                while (true) {
                    PasspointProfileTypeCount[] passpointProfileTypeCountArr2 = this.installedPasspointProfileTypeForR1;
                    if (i108 >= passpointProfileTypeCountArr2.length) {
                        break;
                    }
                    PasspointProfileTypeCount element36 = passpointProfileTypeCountArr2[i108];
                    if (element36 != null) {
                        output.writeMessage(123, element36);
                    }
                    i108++;
                }
            }
            if (!this.hardwareRevision.equals("")) {
                output.writeString(124, this.hardwareRevision);
            }
            WifiLinkLayerUsageStats wifiLinkLayerUsageStats2 = this.wifiLinkLayerUsageStats;
            if (wifiLinkLayerUsageStats2 != null) {
                output.writeMessage(125, wifiLinkLayerUsageStats2);
            }
            WifiUsabilityStats[] wifiUsabilityStatsArr = this.wifiUsabilityStatsList;
            if (wifiUsabilityStatsArr != null && wifiUsabilityStatsArr.length > 0) {
                int i109 = 0;
                while (true) {
                    WifiUsabilityStats[] wifiUsabilityStatsArr2 = this.wifiUsabilityStatsList;
                    if (i109 >= wifiUsabilityStatsArr2.length) {
                        break;
                    }
                    WifiUsabilityStats element37 = wifiUsabilityStatsArr2[i109];
                    if (element37 != null) {
                        output.writeMessage(126, element37);
                    }
                    i109++;
                }
            }
            WifiUsabilityScoreCount[] wifiUsabilityScoreCountArr = this.wifiUsabilityScoreCount;
            if (wifiUsabilityScoreCountArr != null && wifiUsabilityScoreCountArr.length > 0) {
                int i110 = 0;
                while (true) {
                    WifiUsabilityScoreCount[] wifiUsabilityScoreCountArr2 = this.wifiUsabilityScoreCount;
                    if (i110 >= wifiUsabilityScoreCountArr2.length) {
                        break;
                    }
                    WifiUsabilityScoreCount element38 = wifiUsabilityScoreCountArr2[i110];
                    if (element38 != null) {
                        output.writeMessage(127, element38);
                    }
                    i110++;
                }
            }
            DeviceMobilityStatePnoScanStats[] deviceMobilityStatePnoScanStatsArr = this.mobilityStatePnoStatsList;
            if (deviceMobilityStatePnoScanStatsArr != null && deviceMobilityStatePnoScanStatsArr.length > 0) {
                int i111 = 0;
                while (true) {
                    DeviceMobilityStatePnoScanStats[] deviceMobilityStatePnoScanStatsArr2 = this.mobilityStatePnoStatsList;
                    if (i111 >= deviceMobilityStatePnoScanStatsArr2.length) {
                        break;
                    }
                    DeviceMobilityStatePnoScanStats element39 = deviceMobilityStatePnoScanStatsArr2[i111];
                    if (element39 != null) {
                        output.writeMessage(128, element39);
                    }
                    i111++;
                }
            }
            WifiP2pStats wifiP2pStats = this.wifiP2PStats;
            if (wifiP2pStats != null) {
                output.writeMessage(129, wifiP2pStats);
            }
            WifiDppLog wifiDppLog2 = this.wifiDppLog;
            if (wifiDppLog2 != null) {
                output.writeMessage(130, wifiDppLog2);
            }
            int i112 = this.numEnhancedOpenNetworks;
            if (i112 != 0) {
                output.writeInt32(131, i112);
            }
            int i113 = this.numWpa3PersonalNetworks;
            if (i113 != 0) {
                output.writeInt32(132, i113);
            }
            int i114 = this.numWpa3EnterpriseNetworks;
            if (i114 != 0) {
                output.writeInt32(133, i114);
            }
            int i115 = this.numEnhancedOpenNetworkScanResults;
            if (i115 != 0) {
                output.writeInt32(134, i115);
            }
            int i116 = this.numWpa3PersonalNetworkScanResults;
            if (i116 != 0) {
                output.writeInt32(135, i116);
            }
            int i117 = this.numWpa3EnterpriseNetworkScanResults;
            if (i117 != 0) {
                output.writeInt32(136, i117);
            }
            WifiConfigStoreIO wifiConfigStoreIO = this.wifiConfigStoreIo;
            if (wifiConfigStoreIO != null) {
                output.writeMessage(137, wifiConfigStoreIO);
            }
            int i118 = this.numSavedNetworksWithMacRandomization;
            if (i118 != 0) {
                output.writeInt32(138, i118);
            }
            LinkProbeStats linkProbeStats2 = this.linkProbeStats;
            if (linkProbeStats2 != null) {
                output.writeMessage(139, linkProbeStats2);
            }
            NetworkSelectionExperimentDecisions[] networkSelectionExperimentDecisionsArr = this.networkSelectionExperimentDecisionsList;
            if (networkSelectionExperimentDecisionsArr != null && networkSelectionExperimentDecisionsArr.length > 0) {
                int i119 = 0;
                while (true) {
                    NetworkSelectionExperimentDecisions[] networkSelectionExperimentDecisionsArr2 = this.networkSelectionExperimentDecisionsList;
                    if (i119 >= networkSelectionExperimentDecisionsArr2.length) {
                        break;
                    }
                    NetworkSelectionExperimentDecisions element40 = networkSelectionExperimentDecisionsArr2[i119];
                    if (element40 != null) {
                        output.writeMessage(140, element40);
                    }
                    i119++;
                }
            }
            WifiNetworkRequestApiLog wifiNetworkRequestApiLog2 = this.wifiNetworkRequestApiLog;
            if (wifiNetworkRequestApiLog2 != null) {
                output.writeMessage(141, wifiNetworkRequestApiLog2);
            }
            WifiNetworkSuggestionApiLog wifiNetworkSuggestionApiLog2 = this.wifiNetworkSuggestionApiLog;
            if (wifiNetworkSuggestionApiLog2 != null) {
                output.writeMessage(142, wifiNetworkSuggestionApiLog2);
            }
            WifiLockStats wifiLockStats2 = this.wifiLockStats;
            if (wifiLockStats2 != null) {
                output.writeMessage(143, wifiLockStats2);
            }
            WifiToggleStats wifiToggleStats2 = this.wifiToggleStats;
            if (wifiToggleStats2 != null) {
                output.writeMessage(144, wifiToggleStats2);
            }
            int i120 = this.numAddOrUpdateNetworkCalls;
            if (i120 != 0) {
                output.writeInt32(145, i120);
            }
            int i121 = this.numEnableNetworkCalls;
            if (i121 != 0) {
                output.writeInt32(146, i121);
            }
            PasspointProvisionStats passpointProvisionStats2 = this.passpointProvisionStats;
            if (passpointProvisionStats2 != null) {
                output.writeMessage(147, passpointProvisionStats2);
            }
            PasspointProfileTypeCount[] passpointProfileTypeCountArr3 = this.installedPasspointProfileTypeForR2;
            if (passpointProfileTypeCountArr3 != null && passpointProfileTypeCountArr3.length > 0) {
                int i122 = 0;
                while (true) {
                    PasspointProfileTypeCount[] passpointProfileTypeCountArr4 = this.installedPasspointProfileTypeForR2;
                    if (i122 >= passpointProfileTypeCountArr4.length) {
                        break;
                    }
                    PasspointProfileTypeCount element41 = passpointProfileTypeCountArr4[i122];
                    if (element41 != null) {
                        output.writeMessage(148, element41);
                    }
                    i122++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            ConnectionEvent[] connectionEventArr = this.connectionEvent;
            if (connectionEventArr != null && connectionEventArr.length > 0) {
                int i = 0;
                while (true) {
                    ConnectionEvent[] connectionEventArr2 = this.connectionEvent;
                    if (i >= connectionEventArr2.length) {
                        break;
                    }
                    ConnectionEvent element = connectionEventArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                    i++;
                }
            }
            int i2 = this.numSavedNetworks;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.numOpenNetworks;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numLegacyPersonalNetworks;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.numLegacyEnterpriseNetworks;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            boolean z = this.isLocationEnabled;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, z);
            }
            boolean z2 = this.isScanningAlwaysEnabled;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, z2);
            }
            int i6 = this.numWifiToggledViaSettings;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i6);
            }
            int i7 = this.numWifiToggledViaAirplane;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, i7);
            }
            int i8 = this.numNetworksAddedByUser;
            if (i8 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(10, i8);
            }
            int i9 = this.numNetworksAddedByApps;
            if (i9 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, i9);
            }
            int i10 = this.numEmptyScanResults;
            if (i10 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(12, i10);
            }
            int i11 = this.numNonEmptyScanResults;
            if (i11 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, i11);
            }
            int i12 = this.numOneshotScans;
            if (i12 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(14, i12);
            }
            int i13 = this.numBackgroundScans;
            if (i13 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(15, i13);
            }
            ScanReturnEntry[] scanReturnEntryArr = this.scanReturnEntries;
            if (scanReturnEntryArr != null && scanReturnEntryArr.length > 0) {
                int i14 = 0;
                while (true) {
                    ScanReturnEntry[] scanReturnEntryArr2 = this.scanReturnEntries;
                    if (i14 >= scanReturnEntryArr2.length) {
                        break;
                    }
                    ScanReturnEntry element2 = scanReturnEntryArr2[i14];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(16, element2);
                    }
                    i14++;
                }
            }
            WifiSystemStateEntry[] wifiSystemStateEntryArr = this.wifiSystemStateEntries;
            if (wifiSystemStateEntryArr != null && wifiSystemStateEntryArr.length > 0) {
                int i15 = 0;
                while (true) {
                    WifiSystemStateEntry[] wifiSystemStateEntryArr2 = this.wifiSystemStateEntries;
                    if (i15 >= wifiSystemStateEntryArr2.length) {
                        break;
                    }
                    WifiSystemStateEntry element3 = wifiSystemStateEntryArr2[i15];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(17, element3);
                    }
                    i15++;
                }
            }
            ScanReturnEntry[] scanReturnEntryArr3 = this.backgroundScanReturnEntries;
            if (scanReturnEntryArr3 != null && scanReturnEntryArr3.length > 0) {
                int i16 = 0;
                while (true) {
                    ScanReturnEntry[] scanReturnEntryArr4 = this.backgroundScanReturnEntries;
                    if (i16 >= scanReturnEntryArr4.length) {
                        break;
                    }
                    ScanReturnEntry element4 = scanReturnEntryArr4[i16];
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(18, element4);
                    }
                    i16++;
                }
            }
            WifiSystemStateEntry[] wifiSystemStateEntryArr3 = this.backgroundScanRequestState;
            if (wifiSystemStateEntryArr3 != null && wifiSystemStateEntryArr3.length > 0) {
                int i17 = 0;
                while (true) {
                    WifiSystemStateEntry[] wifiSystemStateEntryArr4 = this.backgroundScanRequestState;
                    if (i17 >= wifiSystemStateEntryArr4.length) {
                        break;
                    }
                    WifiSystemStateEntry element5 = wifiSystemStateEntryArr4[i17];
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(19, element5);
                    }
                    i17++;
                }
            }
            int i18 = this.numLastResortWatchdogTriggers;
            if (i18 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(20, i18);
            }
            int i19 = this.numLastResortWatchdogBadAssociationNetworksTotal;
            if (i19 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(21, i19);
            }
            int i20 = this.numLastResortWatchdogBadAuthenticationNetworksTotal;
            if (i20 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(22, i20);
            }
            int i21 = this.numLastResortWatchdogBadDhcpNetworksTotal;
            if (i21 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(23, i21);
            }
            int i22 = this.numLastResortWatchdogBadOtherNetworksTotal;
            if (i22 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(24, i22);
            }
            int i23 = this.numLastResortWatchdogAvailableNetworksTotal;
            if (i23 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(25, i23);
            }
            int i24 = this.numLastResortWatchdogTriggersWithBadAssociation;
            if (i24 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(26, i24);
            }
            int i25 = this.numLastResortWatchdogTriggersWithBadAuthentication;
            if (i25 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(27, i25);
            }
            int i26 = this.numLastResortWatchdogTriggersWithBadDhcp;
            if (i26 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(28, i26);
            }
            int i27 = this.numLastResortWatchdogTriggersWithBadOther;
            if (i27 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(29, i27);
            }
            int i28 = this.numConnectivityWatchdogPnoGood;
            if (i28 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(30, i28);
            }
            int i29 = this.numConnectivityWatchdogPnoBad;
            if (i29 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(31, i29);
            }
            int i30 = this.numConnectivityWatchdogBackgroundGood;
            if (i30 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(32, i30);
            }
            int i31 = this.numConnectivityWatchdogBackgroundBad;
            if (i31 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(33, i31);
            }
            int i32 = this.recordDurationSec;
            if (i32 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(34, i32);
            }
            RssiPollCount[] rssiPollCountArr = this.rssiPollRssiCount;
            if (rssiPollCountArr != null && rssiPollCountArr.length > 0) {
                int i33 = 0;
                while (true) {
                    RssiPollCount[] rssiPollCountArr2 = this.rssiPollRssiCount;
                    if (i33 >= rssiPollCountArr2.length) {
                        break;
                    }
                    RssiPollCount element6 = rssiPollCountArr2[i33];
                    if (element6 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(35, element6);
                    }
                    i33++;
                }
            }
            int i34 = this.numLastResortWatchdogSuccesses;
            if (i34 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(36, i34);
            }
            int i35 = this.numHiddenNetworks;
            if (i35 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(37, i35);
            }
            int i36 = this.numPasspointNetworks;
            if (i36 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(38, i36);
            }
            int i37 = this.numTotalScanResults;
            if (i37 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(39, i37);
            }
            int i38 = this.numOpenNetworkScanResults;
            if (i38 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(40, i38);
            }
            int i39 = this.numLegacyPersonalNetworkScanResults;
            if (i39 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(41, i39);
            }
            int i40 = this.numLegacyEnterpriseNetworkScanResults;
            if (i40 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(42, i40);
            }
            int i41 = this.numHiddenNetworkScanResults;
            if (i41 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(43, i41);
            }
            int i42 = this.numHotspot2R1NetworkScanResults;
            if (i42 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(44, i42);
            }
            int i43 = this.numHotspot2R2NetworkScanResults;
            if (i43 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(45, i43);
            }
            int i44 = this.numScans;
            if (i44 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(46, i44);
            }
            AlertReasonCount[] alertReasonCountArr = this.alertReasonCount;
            if (alertReasonCountArr != null && alertReasonCountArr.length > 0) {
                int i45 = 0;
                while (true) {
                    AlertReasonCount[] alertReasonCountArr2 = this.alertReasonCount;
                    if (i45 >= alertReasonCountArr2.length) {
                        break;
                    }
                    AlertReasonCount element7 = alertReasonCountArr2[i45];
                    if (element7 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(47, element7);
                    }
                    i45++;
                }
            }
            WifiScoreCount[] wifiScoreCountArr = this.wifiScoreCount;
            if (wifiScoreCountArr != null && wifiScoreCountArr.length > 0) {
                int i46 = 0;
                while (true) {
                    WifiScoreCount[] wifiScoreCountArr2 = this.wifiScoreCount;
                    if (i46 >= wifiScoreCountArr2.length) {
                        break;
                    }
                    WifiScoreCount element8 = wifiScoreCountArr2[i46];
                    if (element8 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(48, element8);
                    }
                    i46++;
                }
            }
            SoftApDurationBucket[] softApDurationBucketArr = this.softApDuration;
            if (softApDurationBucketArr != null && softApDurationBucketArr.length > 0) {
                int i47 = 0;
                while (true) {
                    SoftApDurationBucket[] softApDurationBucketArr2 = this.softApDuration;
                    if (i47 >= softApDurationBucketArr2.length) {
                        break;
                    }
                    SoftApDurationBucket element9 = softApDurationBucketArr2[i47];
                    if (element9 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(49, element9);
                    }
                    i47++;
                }
            }
            SoftApReturnCodeCount[] softApReturnCodeCountArr = this.softApReturnCode;
            if (softApReturnCodeCountArr != null && softApReturnCodeCountArr.length > 0) {
                int i48 = 0;
                while (true) {
                    SoftApReturnCodeCount[] softApReturnCodeCountArr2 = this.softApReturnCode;
                    if (i48 >= softApReturnCodeCountArr2.length) {
                        break;
                    }
                    SoftApReturnCodeCount element10 = softApReturnCodeCountArr2[i48];
                    if (element10 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(50, element10);
                    }
                    i48++;
                }
            }
            RssiPollCount[] rssiPollCountArr3 = this.rssiPollDeltaCount;
            if (rssiPollCountArr3 != null && rssiPollCountArr3.length > 0) {
                int i49 = 0;
                while (true) {
                    RssiPollCount[] rssiPollCountArr4 = this.rssiPollDeltaCount;
                    if (i49 >= rssiPollCountArr4.length) {
                        break;
                    }
                    RssiPollCount element11 = rssiPollCountArr4[i49];
                    if (element11 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(51, element11);
                    }
                    i49++;
                }
            }
            StaEvent[] staEventArr = this.staEventList;
            if (staEventArr != null && staEventArr.length > 0) {
                int i50 = 0;
                while (true) {
                    StaEvent[] staEventArr2 = this.staEventList;
                    if (i50 >= staEventArr2.length) {
                        break;
                    }
                    StaEvent element12 = staEventArr2[i50];
                    if (element12 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(52, element12);
                    }
                    i50++;
                }
            }
            int i51 = this.numHalCrashes;
            if (i51 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(53, i51);
            }
            int i52 = this.numWificondCrashes;
            if (i52 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(54, i52);
            }
            int i53 = this.numSetupClientInterfaceFailureDueToHal;
            if (i53 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(55, i53);
            }
            int i54 = this.numSetupClientInterfaceFailureDueToWificond;
            if (i54 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(56, i54);
            }
            WifiAwareLog wifiAwareLog2 = this.wifiAwareLog;
            if (wifiAwareLog2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(57, wifiAwareLog2);
            }
            int i55 = this.numPasspointProviders;
            if (i55 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(58, i55);
            }
            int i56 = this.numPasspointProviderInstallation;
            if (i56 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(59, i56);
            }
            int i57 = this.numPasspointProviderInstallSuccess;
            if (i57 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(60, i57);
            }
            int i58 = this.numPasspointProviderUninstallation;
            if (i58 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(61, i58);
            }
            int i59 = this.numPasspointProviderUninstallSuccess;
            if (i59 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(62, i59);
            }
            int i60 = this.numPasspointProvidersSuccessfullyConnected;
            if (i60 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(63, i60);
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr = this.totalSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr != null && numConnectableNetworksBucketArr.length > 0) {
                int i61 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr2 = this.totalSsidsInScanHistogram;
                    if (i61 >= numConnectableNetworksBucketArr2.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element13 = numConnectableNetworksBucketArr2[i61];
                    if (element13 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(64, element13);
                    }
                    i61++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr3 = this.totalBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr3 != null && numConnectableNetworksBucketArr3.length > 0) {
                int i62 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr4 = this.totalBssidsInScanHistogram;
                    if (i62 >= numConnectableNetworksBucketArr4.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element14 = numConnectableNetworksBucketArr4[i62];
                    if (element14 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(65, element14);
                    }
                    i62++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr5 = this.availableOpenSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr5 != null && numConnectableNetworksBucketArr5.length > 0) {
                int i63 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr6 = this.availableOpenSsidsInScanHistogram;
                    if (i63 >= numConnectableNetworksBucketArr6.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element15 = numConnectableNetworksBucketArr6[i63];
                    if (element15 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(66, element15);
                    }
                    i63++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr7 = this.availableOpenBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr7 != null && numConnectableNetworksBucketArr7.length > 0) {
                int i64 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr8 = this.availableOpenBssidsInScanHistogram;
                    if (i64 >= numConnectableNetworksBucketArr8.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element16 = numConnectableNetworksBucketArr8[i64];
                    if (element16 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(67, element16);
                    }
                    i64++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr9 = this.availableSavedSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr9 != null && numConnectableNetworksBucketArr9.length > 0) {
                int i65 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr10 = this.availableSavedSsidsInScanHistogram;
                    if (i65 >= numConnectableNetworksBucketArr10.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element17 = numConnectableNetworksBucketArr10[i65];
                    if (element17 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(68, element17);
                    }
                    i65++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr11 = this.availableSavedBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr11 != null && numConnectableNetworksBucketArr11.length > 0) {
                int i66 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr12 = this.availableSavedBssidsInScanHistogram;
                    if (i66 >= numConnectableNetworksBucketArr12.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element18 = numConnectableNetworksBucketArr12[i66];
                    if (element18 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(69, element18);
                    }
                    i66++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr13 = this.availableOpenOrSavedSsidsInScanHistogram;
            if (numConnectableNetworksBucketArr13 != null && numConnectableNetworksBucketArr13.length > 0) {
                int i67 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr14 = this.availableOpenOrSavedSsidsInScanHistogram;
                    if (i67 >= numConnectableNetworksBucketArr14.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element19 = numConnectableNetworksBucketArr14[i67];
                    if (element19 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(70, element19);
                    }
                    i67++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr15 = this.availableOpenOrSavedBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr15 != null && numConnectableNetworksBucketArr15.length > 0) {
                int i68 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr16 = this.availableOpenOrSavedBssidsInScanHistogram;
                    if (i68 >= numConnectableNetworksBucketArr16.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element20 = numConnectableNetworksBucketArr16[i68];
                    if (element20 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(71, element20);
                    }
                    i68++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr17 = this.availableSavedPasspointProviderProfilesInScanHistogram;
            if (numConnectableNetworksBucketArr17 != null && numConnectableNetworksBucketArr17.length > 0) {
                int i69 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr18 = this.availableSavedPasspointProviderProfilesInScanHistogram;
                    if (i69 >= numConnectableNetworksBucketArr18.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element21 = numConnectableNetworksBucketArr18[i69];
                    if (element21 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(72, element21);
                    }
                    i69++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr19 = this.availableSavedPasspointProviderBssidsInScanHistogram;
            if (numConnectableNetworksBucketArr19 != null && numConnectableNetworksBucketArr19.length > 0) {
                int i70 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr20 = this.availableSavedPasspointProviderBssidsInScanHistogram;
                    if (i70 >= numConnectableNetworksBucketArr20.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element22 = numConnectableNetworksBucketArr20[i70];
                    if (element22 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(73, element22);
                    }
                    i70++;
                }
            }
            int i71 = this.fullBandAllSingleScanListenerResults;
            if (i71 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(74, i71);
            }
            int i72 = this.partialAllSingleScanListenerResults;
            if (i72 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(75, i72);
            }
            PnoScanMetrics pnoScanMetrics2 = this.pnoScanMetrics;
            if (pnoScanMetrics2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(76, pnoScanMetrics2);
            }
            ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr = this.connectToNetworkNotificationCount;
            if (connectToNetworkNotificationAndActionCountArr != null && connectToNetworkNotificationAndActionCountArr.length > 0) {
                int i73 = 0;
                while (true) {
                    ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr2 = this.connectToNetworkNotificationCount;
                    if (i73 >= connectToNetworkNotificationAndActionCountArr2.length) {
                        break;
                    }
                    ConnectToNetworkNotificationAndActionCount element23 = connectToNetworkNotificationAndActionCountArr2[i73];
                    if (element23 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(77, element23);
                    }
                    i73++;
                }
            }
            ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr3 = this.connectToNetworkNotificationActionCount;
            if (connectToNetworkNotificationAndActionCountArr3 != null && connectToNetworkNotificationAndActionCountArr3.length > 0) {
                int i74 = 0;
                while (true) {
                    ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr4 = this.connectToNetworkNotificationActionCount;
                    if (i74 >= connectToNetworkNotificationAndActionCountArr4.length) {
                        break;
                    }
                    ConnectToNetworkNotificationAndActionCount element24 = connectToNetworkNotificationAndActionCountArr4[i74];
                    if (element24 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(78, element24);
                    }
                    i74++;
                }
            }
            int i75 = this.openNetworkRecommenderBlacklistSize;
            if (i75 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(79, i75);
            }
            boolean z3 = this.isWifiNetworksAvailableNotificationOn;
            if (z3) {
                size += CodedOutputByteBufferNano.computeBoolSize(80, z3);
            }
            int i76 = this.numOpenNetworkRecommendationUpdates;
            if (i76 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(81, i76);
            }
            int i77 = this.numOpenNetworkConnectMessageFailedToSend;
            if (i77 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(82, i77);
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr21 = this.observedHotspotR1ApsInScanHistogram;
            if (numConnectableNetworksBucketArr21 != null && numConnectableNetworksBucketArr21.length > 0) {
                int i78 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr22 = this.observedHotspotR1ApsInScanHistogram;
                    if (i78 >= numConnectableNetworksBucketArr22.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element25 = numConnectableNetworksBucketArr22[i78];
                    if (element25 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(83, element25);
                    }
                    i78++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr23 = this.observedHotspotR2ApsInScanHistogram;
            if (numConnectableNetworksBucketArr23 != null && numConnectableNetworksBucketArr23.length > 0) {
                int i79 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr24 = this.observedHotspotR2ApsInScanHistogram;
                    if (i79 >= numConnectableNetworksBucketArr24.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element26 = numConnectableNetworksBucketArr24[i79];
                    if (element26 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(84, element26);
                    }
                    i79++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr25 = this.observedHotspotR1EssInScanHistogram;
            if (numConnectableNetworksBucketArr25 != null && numConnectableNetworksBucketArr25.length > 0) {
                int i80 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr26 = this.observedHotspotR1EssInScanHistogram;
                    if (i80 >= numConnectableNetworksBucketArr26.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element27 = numConnectableNetworksBucketArr26[i80];
                    if (element27 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(85, element27);
                    }
                    i80++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr27 = this.observedHotspotR2EssInScanHistogram;
            if (numConnectableNetworksBucketArr27 != null && numConnectableNetworksBucketArr27.length > 0) {
                int i81 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr28 = this.observedHotspotR2EssInScanHistogram;
                    if (i81 >= numConnectableNetworksBucketArr28.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element28 = numConnectableNetworksBucketArr28[i81];
                    if (element28 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(86, element28);
                    }
                    i81++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr29 = this.observedHotspotR1ApsPerEssInScanHistogram;
            if (numConnectableNetworksBucketArr29 != null && numConnectableNetworksBucketArr29.length > 0) {
                int i82 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr30 = this.observedHotspotR1ApsPerEssInScanHistogram;
                    if (i82 >= numConnectableNetworksBucketArr30.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element29 = numConnectableNetworksBucketArr30[i82];
                    if (element29 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(87, element29);
                    }
                    i82++;
                }
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr31 = this.observedHotspotR2ApsPerEssInScanHistogram;
            if (numConnectableNetworksBucketArr31 != null && numConnectableNetworksBucketArr31.length > 0) {
                int i83 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr32 = this.observedHotspotR2ApsPerEssInScanHistogram;
                    if (i83 >= numConnectableNetworksBucketArr32.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element30 = numConnectableNetworksBucketArr32[i83];
                    if (element30 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(88, element30);
                    }
                    i83++;
                }
            }
            SoftApConnectedClientsEvent[] softApConnectedClientsEventArr = this.softApConnectedClientsEventsTethered;
            if (softApConnectedClientsEventArr != null && softApConnectedClientsEventArr.length > 0) {
                int i84 = 0;
                while (true) {
                    SoftApConnectedClientsEvent[] softApConnectedClientsEventArr2 = this.softApConnectedClientsEventsTethered;
                    if (i84 >= softApConnectedClientsEventArr2.length) {
                        break;
                    }
                    SoftApConnectedClientsEvent element31 = softApConnectedClientsEventArr2[i84];
                    if (element31 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(89, element31);
                    }
                    i84++;
                }
            }
            SoftApConnectedClientsEvent[] softApConnectedClientsEventArr3 = this.softApConnectedClientsEventsLocalOnly;
            if (softApConnectedClientsEventArr3 != null && softApConnectedClientsEventArr3.length > 0) {
                int i85 = 0;
                while (true) {
                    SoftApConnectedClientsEvent[] softApConnectedClientsEventArr4 = this.softApConnectedClientsEventsLocalOnly;
                    if (i85 >= softApConnectedClientsEventArr4.length) {
                        break;
                    }
                    SoftApConnectedClientsEvent element32 = softApConnectedClientsEventArr4[i85];
                    if (element32 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(90, element32);
                    }
                    i85++;
                }
            }
            WpsMetrics wpsMetrics2 = this.wpsMetrics;
            if (wpsMetrics2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(91, wpsMetrics2);
            }
            WifiPowerStats wifiPowerStats2 = this.wifiPowerStats;
            if (wifiPowerStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(92, wifiPowerStats2);
            }
            int i86 = this.numConnectivityOneshotScans;
            if (i86 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(93, i86);
            }
            WifiWakeStats wifiWakeStats2 = this.wifiWakeStats;
            if (wifiWakeStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(94, wifiWakeStats2);
            }
            NumConnectableNetworksBucket[] numConnectableNetworksBucketArr33 = this.observed80211McSupportingApsInScanHistogram;
            if (numConnectableNetworksBucketArr33 != null && numConnectableNetworksBucketArr33.length > 0) {
                int i87 = 0;
                while (true) {
                    NumConnectableNetworksBucket[] numConnectableNetworksBucketArr34 = this.observed80211McSupportingApsInScanHistogram;
                    if (i87 >= numConnectableNetworksBucketArr34.length) {
                        break;
                    }
                    NumConnectableNetworksBucket element33 = numConnectableNetworksBucketArr34[i87];
                    if (element33 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(95, element33);
                    }
                    i87++;
                }
            }
            int i88 = this.numSupplicantCrashes;
            if (i88 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(96, i88);
            }
            int i89 = this.numHostapdCrashes;
            if (i89 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(97, i89);
            }
            int i90 = this.numSetupClientInterfaceFailureDueToSupplicant;
            if (i90 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(98, i90);
            }
            int i91 = this.numSetupSoftApInterfaceFailureDueToHal;
            if (i91 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(99, i91);
            }
            int i92 = this.numSetupSoftApInterfaceFailureDueToWificond;
            if (i92 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(100, i92);
            }
            int i93 = this.numSetupSoftApInterfaceFailureDueToHostapd;
            if (i93 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(101, i93);
            }
            int i94 = this.numClientInterfaceDown;
            if (i94 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(102, i94);
            }
            int i95 = this.numSoftApInterfaceDown;
            if (i95 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(103, i95);
            }
            int i96 = this.numExternalAppOneshotScanRequests;
            if (i96 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(104, i96);
            }
            int i97 = this.numExternalForegroundAppOneshotScanRequestsThrottled;
            if (i97 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(105, i97);
            }
            int i98 = this.numExternalBackgroundAppOneshotScanRequestsThrottled;
            if (i98 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(106, i98);
            }
            long j = this.watchdogTriggerToConnectionSuccessDurationMs;
            if (j != -1) {
                size += CodedOutputByteBufferNano.computeInt64Size(107, j);
            }
            long j2 = this.watchdogTotalConnectionFailureCountAfterTrigger;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(108, j2);
            }
            int i99 = this.numOneshotHasDfsChannelScans;
            if (i99 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(109, i99);
            }
            WifiRttLog wifiRttLog2 = this.wifiRttLog;
            if (wifiRttLog2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(110, wifiRttLog2);
            }
            boolean z4 = this.isMacRandomizationOn;
            if (z4) {
                size += CodedOutputByteBufferNano.computeBoolSize(111, z4);
            }
            int i100 = this.numRadioModeChangeToMcc;
            if (i100 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(112, i100);
            }
            int i101 = this.numRadioModeChangeToScc;
            if (i101 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(113, i101);
            }
            int i102 = this.numRadioModeChangeToSbs;
            if (i102 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(114, i102);
            }
            int i103 = this.numRadioModeChangeToDbs;
            if (i103 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(115, i103);
            }
            int i104 = this.numSoftApUserBandPreferenceUnsatisfied;
            if (i104 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(116, i104);
            }
            if (!this.scoreExperimentId.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(117, this.scoreExperimentId);
            }
            WifiRadioUsage wifiRadioUsage2 = this.wifiRadioUsage;
            if (wifiRadioUsage2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(118, wifiRadioUsage2);
            }
            ExperimentValues experimentValues2 = this.experimentValues;
            if (experimentValues2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(119, experimentValues2);
            }
            WifiIsUnusableEvent[] wifiIsUnusableEventArr = this.wifiIsUnusableEventList;
            if (wifiIsUnusableEventArr != null && wifiIsUnusableEventArr.length > 0) {
                int i105 = 0;
                while (true) {
                    WifiIsUnusableEvent[] wifiIsUnusableEventArr2 = this.wifiIsUnusableEventList;
                    if (i105 >= wifiIsUnusableEventArr2.length) {
                        break;
                    }
                    WifiIsUnusableEvent element34 = wifiIsUnusableEventArr2[i105];
                    if (element34 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(120, element34);
                    }
                    i105++;
                }
            }
            LinkSpeedCount[] linkSpeedCountArr = this.linkSpeedCounts;
            if (linkSpeedCountArr != null && linkSpeedCountArr.length > 0) {
                int i106 = 0;
                while (true) {
                    LinkSpeedCount[] linkSpeedCountArr2 = this.linkSpeedCounts;
                    if (i106 >= linkSpeedCountArr2.length) {
                        break;
                    }
                    LinkSpeedCount element35 = linkSpeedCountArr2[i106];
                    if (element35 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(121, element35);
                    }
                    i106++;
                }
            }
            int i107 = this.numSarSensorRegistrationFailures;
            if (i107 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(122, i107);
            }
            PasspointProfileTypeCount[] passpointProfileTypeCountArr = this.installedPasspointProfileTypeForR1;
            if (passpointProfileTypeCountArr != null && passpointProfileTypeCountArr.length > 0) {
                int i108 = 0;
                while (true) {
                    PasspointProfileTypeCount[] passpointProfileTypeCountArr2 = this.installedPasspointProfileTypeForR1;
                    if (i108 >= passpointProfileTypeCountArr2.length) {
                        break;
                    }
                    PasspointProfileTypeCount element36 = passpointProfileTypeCountArr2[i108];
                    if (element36 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(123, element36);
                    }
                    i108++;
                }
            }
            if (!this.hardwareRevision.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(124, this.hardwareRevision);
            }
            WifiLinkLayerUsageStats wifiLinkLayerUsageStats2 = this.wifiLinkLayerUsageStats;
            if (wifiLinkLayerUsageStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(125, wifiLinkLayerUsageStats2);
            }
            WifiUsabilityStats[] wifiUsabilityStatsArr = this.wifiUsabilityStatsList;
            if (wifiUsabilityStatsArr != null && wifiUsabilityStatsArr.length > 0) {
                int i109 = 0;
                while (true) {
                    WifiUsabilityStats[] wifiUsabilityStatsArr2 = this.wifiUsabilityStatsList;
                    if (i109 >= wifiUsabilityStatsArr2.length) {
                        break;
                    }
                    WifiUsabilityStats element37 = wifiUsabilityStatsArr2[i109];
                    if (element37 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(126, element37);
                    }
                    i109++;
                }
            }
            WifiUsabilityScoreCount[] wifiUsabilityScoreCountArr = this.wifiUsabilityScoreCount;
            if (wifiUsabilityScoreCountArr != null && wifiUsabilityScoreCountArr.length > 0) {
                int i110 = 0;
                while (true) {
                    WifiUsabilityScoreCount[] wifiUsabilityScoreCountArr2 = this.wifiUsabilityScoreCount;
                    if (i110 >= wifiUsabilityScoreCountArr2.length) {
                        break;
                    }
                    WifiUsabilityScoreCount element38 = wifiUsabilityScoreCountArr2[i110];
                    if (element38 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(127, element38);
                    }
                    i110++;
                }
            }
            DeviceMobilityStatePnoScanStats[] deviceMobilityStatePnoScanStatsArr = this.mobilityStatePnoStatsList;
            if (deviceMobilityStatePnoScanStatsArr != null && deviceMobilityStatePnoScanStatsArr.length > 0) {
                int i111 = 0;
                while (true) {
                    DeviceMobilityStatePnoScanStats[] deviceMobilityStatePnoScanStatsArr2 = this.mobilityStatePnoStatsList;
                    if (i111 >= deviceMobilityStatePnoScanStatsArr2.length) {
                        break;
                    }
                    DeviceMobilityStatePnoScanStats element39 = deviceMobilityStatePnoScanStatsArr2[i111];
                    if (element39 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(128, element39);
                    }
                    i111++;
                }
            }
            WifiP2pStats wifiP2pStats = this.wifiP2PStats;
            if (wifiP2pStats != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(129, wifiP2pStats);
            }
            WifiDppLog wifiDppLog2 = this.wifiDppLog;
            if (wifiDppLog2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(130, wifiDppLog2);
            }
            int i112 = this.numEnhancedOpenNetworks;
            if (i112 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(131, i112);
            }
            int i113 = this.numWpa3PersonalNetworks;
            if (i113 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(132, i113);
            }
            int i114 = this.numWpa3EnterpriseNetworks;
            if (i114 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(133, i114);
            }
            int i115 = this.numEnhancedOpenNetworkScanResults;
            if (i115 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(134, i115);
            }
            int i116 = this.numWpa3PersonalNetworkScanResults;
            if (i116 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(135, i116);
            }
            int i117 = this.numWpa3EnterpriseNetworkScanResults;
            if (i117 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(136, i117);
            }
            WifiConfigStoreIO wifiConfigStoreIO = this.wifiConfigStoreIo;
            if (wifiConfigStoreIO != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(137, wifiConfigStoreIO);
            }
            int i118 = this.numSavedNetworksWithMacRandomization;
            if (i118 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(138, i118);
            }
            LinkProbeStats linkProbeStats2 = this.linkProbeStats;
            if (linkProbeStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(139, linkProbeStats2);
            }
            NetworkSelectionExperimentDecisions[] networkSelectionExperimentDecisionsArr = this.networkSelectionExperimentDecisionsList;
            if (networkSelectionExperimentDecisionsArr != null && networkSelectionExperimentDecisionsArr.length > 0) {
                int i119 = 0;
                while (true) {
                    NetworkSelectionExperimentDecisions[] networkSelectionExperimentDecisionsArr2 = this.networkSelectionExperimentDecisionsList;
                    if (i119 >= networkSelectionExperimentDecisionsArr2.length) {
                        break;
                    }
                    NetworkSelectionExperimentDecisions element40 = networkSelectionExperimentDecisionsArr2[i119];
                    if (element40 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(140, element40);
                    }
                    i119++;
                }
            }
            WifiNetworkRequestApiLog wifiNetworkRequestApiLog2 = this.wifiNetworkRequestApiLog;
            if (wifiNetworkRequestApiLog2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(141, wifiNetworkRequestApiLog2);
            }
            WifiNetworkSuggestionApiLog wifiNetworkSuggestionApiLog2 = this.wifiNetworkSuggestionApiLog;
            if (wifiNetworkSuggestionApiLog2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(142, wifiNetworkSuggestionApiLog2);
            }
            WifiLockStats wifiLockStats2 = this.wifiLockStats;
            if (wifiLockStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(143, wifiLockStats2);
            }
            WifiToggleStats wifiToggleStats2 = this.wifiToggleStats;
            if (wifiToggleStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(144, wifiToggleStats2);
            }
            int i120 = this.numAddOrUpdateNetworkCalls;
            if (i120 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(145, i120);
            }
            int i121 = this.numEnableNetworkCalls;
            if (i121 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(146, i121);
            }
            PasspointProvisionStats passpointProvisionStats2 = this.passpointProvisionStats;
            if (passpointProvisionStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(147, passpointProvisionStats2);
            }
            PasspointProfileTypeCount[] passpointProfileTypeCountArr3 = this.installedPasspointProfileTypeForR2;
            if (passpointProfileTypeCountArr3 != null && passpointProfileTypeCountArr3.length > 0) {
                int i122 = 0;
                while (true) {
                    PasspointProfileTypeCount[] passpointProfileTypeCountArr4 = this.installedPasspointProfileTypeForR2;
                    if (i122 >= passpointProfileTypeCountArr4.length) {
                        break;
                    }
                    PasspointProfileTypeCount element41 = passpointProfileTypeCountArr4[i122];
                    if (element41 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(148, element41);
                    }
                    i122++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        ConnectionEvent[] connectionEventArr = this.connectionEvent;
                        int i = connectionEventArr == null ? 0 : connectionEventArr.length;
                        ConnectionEvent[] newArray = new ConnectionEvent[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.connectionEvent, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new ConnectionEvent();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new ConnectionEvent();
                        input.readMessage(newArray[i]);
                        this.connectionEvent = newArray;
                        break;
                    case 16:
                        this.numSavedNetworks = input.readInt32();
                        break;
                    case 24:
                        this.numOpenNetworks = input.readInt32();
                        break;
                    case 32:
                        this.numLegacyPersonalNetworks = input.readInt32();
                        break;
                    case 40:
                        this.numLegacyEnterpriseNetworks = input.readInt32();
                        break;
                    case 48:
                        this.isLocationEnabled = input.readBool();
                        break;
                    case 56:
                        this.isScanningAlwaysEnabled = input.readBool();
                        break;
                    case 64:
                        this.numWifiToggledViaSettings = input.readInt32();
                        break;
                    case 72:
                        this.numWifiToggledViaAirplane = input.readInt32();
                        break;
                    case 80:
                        this.numNetworksAddedByUser = input.readInt32();
                        break;
                    case 88:
                        this.numNetworksAddedByApps = input.readInt32();
                        break;
                    case 96:
                        this.numEmptyScanResults = input.readInt32();
                        break;
                    case 104:
                        this.numNonEmptyScanResults = input.readInt32();
                        break;
                    case 112:
                        this.numOneshotScans = input.readInt32();
                        break;
                    case 120:
                        this.numBackgroundScans = input.readInt32();
                        break;
                    case 130:
                        int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 130);
                        ScanReturnEntry[] scanReturnEntryArr = this.scanReturnEntries;
                        int i2 = scanReturnEntryArr == null ? 0 : scanReturnEntryArr.length;
                        ScanReturnEntry[] newArray2 = new ScanReturnEntry[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.scanReturnEntries, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length - 1) {
                            newArray2[i2] = new ScanReturnEntry();
                            input.readMessage(newArray2[i2]);
                            input.readTag();
                            i2++;
                        }
                        newArray2[i2] = new ScanReturnEntry();
                        input.readMessage(newArray2[i2]);
                        this.scanReturnEntries = newArray2;
                        break;
                    case 138:
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 138);
                        WifiSystemStateEntry[] wifiSystemStateEntryArr = this.wifiSystemStateEntries;
                        int i3 = wifiSystemStateEntryArr == null ? 0 : wifiSystemStateEntryArr.length;
                        WifiSystemStateEntry[] newArray3 = new WifiSystemStateEntry[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.wifiSystemStateEntries, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = new WifiSystemStateEntry();
                            input.readMessage(newArray3[i3]);
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = new WifiSystemStateEntry();
                        input.readMessage(newArray3[i3]);
                        this.wifiSystemStateEntries = newArray3;
                        break;
                    case 146:
                        int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 146);
                        ScanReturnEntry[] scanReturnEntryArr2 = this.backgroundScanReturnEntries;
                        int i4 = scanReturnEntryArr2 == null ? 0 : scanReturnEntryArr2.length;
                        ScanReturnEntry[] newArray4 = new ScanReturnEntry[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.backgroundScanReturnEntries, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length - 1) {
                            newArray4[i4] = new ScanReturnEntry();
                            input.readMessage(newArray4[i4]);
                            input.readTag();
                            i4++;
                        }
                        newArray4[i4] = new ScanReturnEntry();
                        input.readMessage(newArray4[i4]);
                        this.backgroundScanReturnEntries = newArray4;
                        break;
                    case 154:
                        int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 154);
                        WifiSystemStateEntry[] wifiSystemStateEntryArr2 = this.backgroundScanRequestState;
                        int i5 = wifiSystemStateEntryArr2 == null ? 0 : wifiSystemStateEntryArr2.length;
                        WifiSystemStateEntry[] newArray5 = new WifiSystemStateEntry[(i5 + arrayLength5)];
                        if (i5 != 0) {
                            System.arraycopy(this.backgroundScanRequestState, 0, newArray5, 0, i5);
                        }
                        while (i5 < newArray5.length - 1) {
                            newArray5[i5] = new WifiSystemStateEntry();
                            input.readMessage(newArray5[i5]);
                            input.readTag();
                            i5++;
                        }
                        newArray5[i5] = new WifiSystemStateEntry();
                        input.readMessage(newArray5[i5]);
                        this.backgroundScanRequestState = newArray5;
                        break;
                    case 160:
                        this.numLastResortWatchdogTriggers = input.readInt32();
                        break;
                    case 168:
                        this.numLastResortWatchdogBadAssociationNetworksTotal = input.readInt32();
                        break;
                    case 176:
                        this.numLastResortWatchdogBadAuthenticationNetworksTotal = input.readInt32();
                        break;
                    case 184:
                        this.numLastResortWatchdogBadDhcpNetworksTotal = input.readInt32();
                        break;
                    case 192:
                        this.numLastResortWatchdogBadOtherNetworksTotal = input.readInt32();
                        break;
                    case 200:
                        this.numLastResortWatchdogAvailableNetworksTotal = input.readInt32();
                        break;
                    case 208:
                        this.numLastResortWatchdogTriggersWithBadAssociation = input.readInt32();
                        break;
                    case 216:
                        this.numLastResortWatchdogTriggersWithBadAuthentication = input.readInt32();
                        break;
                    case 224:
                        this.numLastResortWatchdogTriggersWithBadDhcp = input.readInt32();
                        break;
                    case 232:
                        this.numLastResortWatchdogTriggersWithBadOther = input.readInt32();
                        break;
                    case 240:
                        this.numConnectivityWatchdogPnoGood = input.readInt32();
                        break;
                    case 248:
                        this.numConnectivityWatchdogPnoBad = input.readInt32();
                        break;
                    case 256:
                        this.numConnectivityWatchdogBackgroundGood = input.readInt32();
                        break;
                    case 264:
                        this.numConnectivityWatchdogBackgroundBad = input.readInt32();
                        break;
                    case 272:
                        this.recordDurationSec = input.readInt32();
                        break;
                    case 282:
                        int arrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(input, 282);
                        RssiPollCount[] rssiPollCountArr = this.rssiPollRssiCount;
                        int i6 = rssiPollCountArr == null ? 0 : rssiPollCountArr.length;
                        RssiPollCount[] newArray6 = new RssiPollCount[(i6 + arrayLength6)];
                        if (i6 != 0) {
                            System.arraycopy(this.rssiPollRssiCount, 0, newArray6, 0, i6);
                        }
                        while (i6 < newArray6.length - 1) {
                            newArray6[i6] = new RssiPollCount();
                            input.readMessage(newArray6[i6]);
                            input.readTag();
                            i6++;
                        }
                        newArray6[i6] = new RssiPollCount();
                        input.readMessage(newArray6[i6]);
                        this.rssiPollRssiCount = newArray6;
                        break;
                    case 288:
                        this.numLastResortWatchdogSuccesses = input.readInt32();
                        break;
                    case 296:
                        this.numHiddenNetworks = input.readInt32();
                        break;
                    case 304:
                        this.numPasspointNetworks = input.readInt32();
                        break;
                    case 312:
                        this.numTotalScanResults = input.readInt32();
                        break;
                    case 320:
                        this.numOpenNetworkScanResults = input.readInt32();
                        break;
                    case 328:
                        this.numLegacyPersonalNetworkScanResults = input.readInt32();
                        break;
                    case 336:
                        this.numLegacyEnterpriseNetworkScanResults = input.readInt32();
                        break;
                    case 344:
                        this.numHiddenNetworkScanResults = input.readInt32();
                        break;
                    case 352:
                        this.numHotspot2R1NetworkScanResults = input.readInt32();
                        break;
                    case 360:
                        this.numHotspot2R2NetworkScanResults = input.readInt32();
                        break;
                    case 368:
                        this.numScans = input.readInt32();
                        break;
                    case 378:
                        int arrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(input, 378);
                        AlertReasonCount[] alertReasonCountArr = this.alertReasonCount;
                        int i7 = alertReasonCountArr == null ? 0 : alertReasonCountArr.length;
                        AlertReasonCount[] newArray7 = new AlertReasonCount[(i7 + arrayLength7)];
                        if (i7 != 0) {
                            System.arraycopy(this.alertReasonCount, 0, newArray7, 0, i7);
                        }
                        while (i7 < newArray7.length - 1) {
                            newArray7[i7] = new AlertReasonCount();
                            input.readMessage(newArray7[i7]);
                            input.readTag();
                            i7++;
                        }
                        newArray7[i7] = new AlertReasonCount();
                        input.readMessage(newArray7[i7]);
                        this.alertReasonCount = newArray7;
                        break;
                    case 386:
                        int arrayLength8 = WireFormatNano.getRepeatedFieldArrayLength(input, 386);
                        WifiScoreCount[] wifiScoreCountArr = this.wifiScoreCount;
                        int i8 = wifiScoreCountArr == null ? 0 : wifiScoreCountArr.length;
                        WifiScoreCount[] newArray8 = new WifiScoreCount[(i8 + arrayLength8)];
                        if (i8 != 0) {
                            System.arraycopy(this.wifiScoreCount, 0, newArray8, 0, i8);
                        }
                        while (i8 < newArray8.length - 1) {
                            newArray8[i8] = new WifiScoreCount();
                            input.readMessage(newArray8[i8]);
                            input.readTag();
                            i8++;
                        }
                        newArray8[i8] = new WifiScoreCount();
                        input.readMessage(newArray8[i8]);
                        this.wifiScoreCount = newArray8;
                        break;
                    case 394:
                        int arrayLength9 = WireFormatNano.getRepeatedFieldArrayLength(input, 394);
                        SoftApDurationBucket[] softApDurationBucketArr = this.softApDuration;
                        int i9 = softApDurationBucketArr == null ? 0 : softApDurationBucketArr.length;
                        SoftApDurationBucket[] newArray9 = new SoftApDurationBucket[(i9 + arrayLength9)];
                        if (i9 != 0) {
                            System.arraycopy(this.softApDuration, 0, newArray9, 0, i9);
                        }
                        while (i9 < newArray9.length - 1) {
                            newArray9[i9] = new SoftApDurationBucket();
                            input.readMessage(newArray9[i9]);
                            input.readTag();
                            i9++;
                        }
                        newArray9[i9] = new SoftApDurationBucket();
                        input.readMessage(newArray9[i9]);
                        this.softApDuration = newArray9;
                        break;
                    case 402:
                        int arrayLength10 = WireFormatNano.getRepeatedFieldArrayLength(input, 402);
                        SoftApReturnCodeCount[] softApReturnCodeCountArr = this.softApReturnCode;
                        int i10 = softApReturnCodeCountArr == null ? 0 : softApReturnCodeCountArr.length;
                        SoftApReturnCodeCount[] newArray10 = new SoftApReturnCodeCount[(i10 + arrayLength10)];
                        if (i10 != 0) {
                            System.arraycopy(this.softApReturnCode, 0, newArray10, 0, i10);
                        }
                        while (i10 < newArray10.length - 1) {
                            newArray10[i10] = new SoftApReturnCodeCount();
                            input.readMessage(newArray10[i10]);
                            input.readTag();
                            i10++;
                        }
                        newArray10[i10] = new SoftApReturnCodeCount();
                        input.readMessage(newArray10[i10]);
                        this.softApReturnCode = newArray10;
                        break;
                    case 410:
                        int arrayLength11 = WireFormatNano.getRepeatedFieldArrayLength(input, 410);
                        RssiPollCount[] rssiPollCountArr2 = this.rssiPollDeltaCount;
                        int i11 = rssiPollCountArr2 == null ? 0 : rssiPollCountArr2.length;
                        RssiPollCount[] newArray11 = new RssiPollCount[(i11 + arrayLength11)];
                        if (i11 != 0) {
                            System.arraycopy(this.rssiPollDeltaCount, 0, newArray11, 0, i11);
                        }
                        while (i11 < newArray11.length - 1) {
                            newArray11[i11] = new RssiPollCount();
                            input.readMessage(newArray11[i11]);
                            input.readTag();
                            i11++;
                        }
                        newArray11[i11] = new RssiPollCount();
                        input.readMessage(newArray11[i11]);
                        this.rssiPollDeltaCount = newArray11;
                        break;
                    case JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_CLCIK_END /* 418 */:
                        int arrayLength12 = WireFormatNano.getRepeatedFieldArrayLength(input, JlogConstants.JLID_INPUTMETHOD_CANDIDTAE_CLCIK_END);
                        StaEvent[] staEventArr = this.staEventList;
                        int i12 = staEventArr == null ? 0 : staEventArr.length;
                        StaEvent[] newArray12 = new StaEvent[(i12 + arrayLength12)];
                        if (i12 != 0) {
                            System.arraycopy(this.staEventList, 0, newArray12, 0, i12);
                        }
                        while (i12 < newArray12.length - 1) {
                            newArray12[i12] = new StaEvent();
                            input.readMessage(newArray12[i12]);
                            input.readTag();
                            i12++;
                        }
                        newArray12[i12] = new StaEvent();
                        input.readMessage(newArray12[i12]);
                        this.staEventList = newArray12;
                        break;
                    case JlogConstants.JLID_INPUTMETHOD_EMOJI_MOVE_END /* 424 */:
                        this.numHalCrashes = input.readInt32();
                        break;
                    case 432:
                        this.numWificondCrashes = input.readInt32();
                        break;
                    case 440:
                        this.numSetupClientInterfaceFailureDueToHal = input.readInt32();
                        break;
                    case 448:
                        this.numSetupClientInterfaceFailureDueToWificond = input.readInt32();
                        break;
                    case 458:
                        if (this.wifiAwareLog == null) {
                            this.wifiAwareLog = new WifiAwareLog();
                        }
                        input.readMessage(this.wifiAwareLog);
                        break;
                    case 464:
                        this.numPasspointProviders = input.readInt32();
                        break;
                    case 472:
                        this.numPasspointProviderInstallation = input.readInt32();
                        break;
                    case 480:
                        this.numPasspointProviderInstallSuccess = input.readInt32();
                        break;
                    case 488:
                        this.numPasspointProviderUninstallation = input.readInt32();
                        break;
                    case 496:
                        this.numPasspointProviderUninstallSuccess = input.readInt32();
                        break;
                    case 504:
                        this.numPasspointProvidersSuccessfullyConnected = input.readInt32();
                        break;
                    case 514:
                        int arrayLength13 = WireFormatNano.getRepeatedFieldArrayLength(input, 514);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr = this.totalSsidsInScanHistogram;
                        int i13 = numConnectableNetworksBucketArr == null ? 0 : numConnectableNetworksBucketArr.length;
                        NumConnectableNetworksBucket[] newArray13 = new NumConnectableNetworksBucket[(i13 + arrayLength13)];
                        if (i13 != 0) {
                            System.arraycopy(this.totalSsidsInScanHistogram, 0, newArray13, 0, i13);
                        }
                        while (i13 < newArray13.length - 1) {
                            newArray13[i13] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray13[i13]);
                            input.readTag();
                            i13++;
                        }
                        newArray13[i13] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray13[i13]);
                        this.totalSsidsInScanHistogram = newArray13;
                        break;
                    case 522:
                        int arrayLength14 = WireFormatNano.getRepeatedFieldArrayLength(input, 522);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr2 = this.totalBssidsInScanHistogram;
                        int i14 = numConnectableNetworksBucketArr2 == null ? 0 : numConnectableNetworksBucketArr2.length;
                        NumConnectableNetworksBucket[] newArray14 = new NumConnectableNetworksBucket[(i14 + arrayLength14)];
                        if (i14 != 0) {
                            System.arraycopy(this.totalBssidsInScanHistogram, 0, newArray14, 0, i14);
                        }
                        while (i14 < newArray14.length - 1) {
                            newArray14[i14] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray14[i14]);
                            input.readTag();
                            i14++;
                        }
                        newArray14[i14] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray14[i14]);
                        this.totalBssidsInScanHistogram = newArray14;
                        break;
                    case 530:
                        int arrayLength15 = WireFormatNano.getRepeatedFieldArrayLength(input, 530);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr3 = this.availableOpenSsidsInScanHistogram;
                        int i15 = numConnectableNetworksBucketArr3 == null ? 0 : numConnectableNetworksBucketArr3.length;
                        NumConnectableNetworksBucket[] newArray15 = new NumConnectableNetworksBucket[(i15 + arrayLength15)];
                        if (i15 != 0) {
                            System.arraycopy(this.availableOpenSsidsInScanHistogram, 0, newArray15, 0, i15);
                        }
                        while (i15 < newArray15.length - 1) {
                            newArray15[i15] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray15[i15]);
                            input.readTag();
                            i15++;
                        }
                        newArray15[i15] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray15[i15]);
                        this.availableOpenSsidsInScanHistogram = newArray15;
                        break;
                    case 538:
                        int arrayLength16 = WireFormatNano.getRepeatedFieldArrayLength(input, 538);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr4 = this.availableOpenBssidsInScanHistogram;
                        int i16 = numConnectableNetworksBucketArr4 == null ? 0 : numConnectableNetworksBucketArr4.length;
                        NumConnectableNetworksBucket[] newArray16 = new NumConnectableNetworksBucket[(i16 + arrayLength16)];
                        if (i16 != 0) {
                            System.arraycopy(this.availableOpenBssidsInScanHistogram, 0, newArray16, 0, i16);
                        }
                        while (i16 < newArray16.length - 1) {
                            newArray16[i16] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray16[i16]);
                            input.readTag();
                            i16++;
                        }
                        newArray16[i16] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray16[i16]);
                        this.availableOpenBssidsInScanHistogram = newArray16;
                        break;
                    case 546:
                        int arrayLength17 = WireFormatNano.getRepeatedFieldArrayLength(input, 546);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr5 = this.availableSavedSsidsInScanHistogram;
                        int i17 = numConnectableNetworksBucketArr5 == null ? 0 : numConnectableNetworksBucketArr5.length;
                        NumConnectableNetworksBucket[] newArray17 = new NumConnectableNetworksBucket[(i17 + arrayLength17)];
                        if (i17 != 0) {
                            System.arraycopy(this.availableSavedSsidsInScanHistogram, 0, newArray17, 0, i17);
                        }
                        while (i17 < newArray17.length - 1) {
                            newArray17[i17] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray17[i17]);
                            input.readTag();
                            i17++;
                        }
                        newArray17[i17] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray17[i17]);
                        this.availableSavedSsidsInScanHistogram = newArray17;
                        break;
                    case 554:
                        int arrayLength18 = WireFormatNano.getRepeatedFieldArrayLength(input, 554);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr6 = this.availableSavedBssidsInScanHistogram;
                        int i18 = numConnectableNetworksBucketArr6 == null ? 0 : numConnectableNetworksBucketArr6.length;
                        NumConnectableNetworksBucket[] newArray18 = new NumConnectableNetworksBucket[(i18 + arrayLength18)];
                        if (i18 != 0) {
                            System.arraycopy(this.availableSavedBssidsInScanHistogram, 0, newArray18, 0, i18);
                        }
                        while (i18 < newArray18.length - 1) {
                            newArray18[i18] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray18[i18]);
                            input.readTag();
                            i18++;
                        }
                        newArray18[i18] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray18[i18]);
                        this.availableSavedBssidsInScanHistogram = newArray18;
                        break;
                    case 562:
                        int arrayLength19 = WireFormatNano.getRepeatedFieldArrayLength(input, 562);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr7 = this.availableOpenOrSavedSsidsInScanHistogram;
                        int i19 = numConnectableNetworksBucketArr7 == null ? 0 : numConnectableNetworksBucketArr7.length;
                        NumConnectableNetworksBucket[] newArray19 = new NumConnectableNetworksBucket[(i19 + arrayLength19)];
                        if (i19 != 0) {
                            System.arraycopy(this.availableOpenOrSavedSsidsInScanHistogram, 0, newArray19, 0, i19);
                        }
                        while (i19 < newArray19.length - 1) {
                            newArray19[i19] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray19[i19]);
                            input.readTag();
                            i19++;
                        }
                        newArray19[i19] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray19[i19]);
                        this.availableOpenOrSavedSsidsInScanHistogram = newArray19;
                        break;
                    case 570:
                        int arrayLength20 = WireFormatNano.getRepeatedFieldArrayLength(input, 570);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr8 = this.availableOpenOrSavedBssidsInScanHistogram;
                        int i20 = numConnectableNetworksBucketArr8 == null ? 0 : numConnectableNetworksBucketArr8.length;
                        NumConnectableNetworksBucket[] newArray20 = new NumConnectableNetworksBucket[(i20 + arrayLength20)];
                        if (i20 != 0) {
                            System.arraycopy(this.availableOpenOrSavedBssidsInScanHistogram, 0, newArray20, 0, i20);
                        }
                        while (i20 < newArray20.length - 1) {
                            newArray20[i20] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray20[i20]);
                            input.readTag();
                            i20++;
                        }
                        newArray20[i20] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray20[i20]);
                        this.availableOpenOrSavedBssidsInScanHistogram = newArray20;
                        break;
                    case 578:
                        int arrayLength21 = WireFormatNano.getRepeatedFieldArrayLength(input, 578);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr9 = this.availableSavedPasspointProviderProfilesInScanHistogram;
                        int i21 = numConnectableNetworksBucketArr9 == null ? 0 : numConnectableNetworksBucketArr9.length;
                        NumConnectableNetworksBucket[] newArray21 = new NumConnectableNetworksBucket[(i21 + arrayLength21)];
                        if (i21 != 0) {
                            System.arraycopy(this.availableSavedPasspointProviderProfilesInScanHistogram, 0, newArray21, 0, i21);
                        }
                        while (i21 < newArray21.length - 1) {
                            newArray21[i21] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray21[i21]);
                            input.readTag();
                            i21++;
                        }
                        newArray21[i21] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray21[i21]);
                        this.availableSavedPasspointProviderProfilesInScanHistogram = newArray21;
                        break;
                    case 586:
                        int arrayLength22 = WireFormatNano.getRepeatedFieldArrayLength(input, 586);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr10 = this.availableSavedPasspointProviderBssidsInScanHistogram;
                        int i22 = numConnectableNetworksBucketArr10 == null ? 0 : numConnectableNetworksBucketArr10.length;
                        NumConnectableNetworksBucket[] newArray22 = new NumConnectableNetworksBucket[(i22 + arrayLength22)];
                        if (i22 != 0) {
                            System.arraycopy(this.availableSavedPasspointProviderBssidsInScanHistogram, 0, newArray22, 0, i22);
                        }
                        while (i22 < newArray22.length - 1) {
                            newArray22[i22] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray22[i22]);
                            input.readTag();
                            i22++;
                        }
                        newArray22[i22] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray22[i22]);
                        this.availableSavedPasspointProviderBssidsInScanHistogram = newArray22;
                        break;
                    case 592:
                        this.fullBandAllSingleScanListenerResults = input.readInt32();
                        break;
                    case 600:
                        this.partialAllSingleScanListenerResults = input.readInt32();
                        break;
                    case 610:
                        if (this.pnoScanMetrics == null) {
                            this.pnoScanMetrics = new PnoScanMetrics();
                        }
                        input.readMessage(this.pnoScanMetrics);
                        break;
                    case 618:
                        int arrayLength23 = WireFormatNano.getRepeatedFieldArrayLength(input, 618);
                        ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr = this.connectToNetworkNotificationCount;
                        int i23 = connectToNetworkNotificationAndActionCountArr == null ? 0 : connectToNetworkNotificationAndActionCountArr.length;
                        ConnectToNetworkNotificationAndActionCount[] newArray23 = new ConnectToNetworkNotificationAndActionCount[(i23 + arrayLength23)];
                        if (i23 != 0) {
                            System.arraycopy(this.connectToNetworkNotificationCount, 0, newArray23, 0, i23);
                        }
                        while (i23 < newArray23.length - 1) {
                            newArray23[i23] = new ConnectToNetworkNotificationAndActionCount();
                            input.readMessage(newArray23[i23]);
                            input.readTag();
                            i23++;
                        }
                        newArray23[i23] = new ConnectToNetworkNotificationAndActionCount();
                        input.readMessage(newArray23[i23]);
                        this.connectToNetworkNotificationCount = newArray23;
                        break;
                    case 626:
                        int arrayLength24 = WireFormatNano.getRepeatedFieldArrayLength(input, 626);
                        ConnectToNetworkNotificationAndActionCount[] connectToNetworkNotificationAndActionCountArr2 = this.connectToNetworkNotificationActionCount;
                        int i24 = connectToNetworkNotificationAndActionCountArr2 == null ? 0 : connectToNetworkNotificationAndActionCountArr2.length;
                        ConnectToNetworkNotificationAndActionCount[] newArray24 = new ConnectToNetworkNotificationAndActionCount[(i24 + arrayLength24)];
                        if (i24 != 0) {
                            System.arraycopy(this.connectToNetworkNotificationActionCount, 0, newArray24, 0, i24);
                        }
                        while (i24 < newArray24.length - 1) {
                            newArray24[i24] = new ConnectToNetworkNotificationAndActionCount();
                            input.readMessage(newArray24[i24]);
                            input.readTag();
                            i24++;
                        }
                        newArray24[i24] = new ConnectToNetworkNotificationAndActionCount();
                        input.readMessage(newArray24[i24]);
                        this.connectToNetworkNotificationActionCount = newArray24;
                        break;
                    case 632:
                        this.openNetworkRecommenderBlacklistSize = input.readInt32();
                        break;
                    case 640:
                        this.isWifiNetworksAvailableNotificationOn = input.readBool();
                        break;
                    case 648:
                        this.numOpenNetworkRecommendationUpdates = input.readInt32();
                        break;
                    case 656:
                        this.numOpenNetworkConnectMessageFailedToSend = input.readInt32();
                        break;
                    case 666:
                        int arrayLength25 = WireFormatNano.getRepeatedFieldArrayLength(input, 666);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr11 = this.observedHotspotR1ApsInScanHistogram;
                        int i25 = numConnectableNetworksBucketArr11 == null ? 0 : numConnectableNetworksBucketArr11.length;
                        NumConnectableNetworksBucket[] newArray25 = new NumConnectableNetworksBucket[(i25 + arrayLength25)];
                        if (i25 != 0) {
                            System.arraycopy(this.observedHotspotR1ApsInScanHistogram, 0, newArray25, 0, i25);
                        }
                        while (i25 < newArray25.length - 1) {
                            newArray25[i25] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray25[i25]);
                            input.readTag();
                            i25++;
                        }
                        newArray25[i25] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray25[i25]);
                        this.observedHotspotR1ApsInScanHistogram = newArray25;
                        break;
                    case 674:
                        int arrayLength26 = WireFormatNano.getRepeatedFieldArrayLength(input, 674);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr12 = this.observedHotspotR2ApsInScanHistogram;
                        int i26 = numConnectableNetworksBucketArr12 == null ? 0 : numConnectableNetworksBucketArr12.length;
                        NumConnectableNetworksBucket[] newArray26 = new NumConnectableNetworksBucket[(i26 + arrayLength26)];
                        if (i26 != 0) {
                            System.arraycopy(this.observedHotspotR2ApsInScanHistogram, 0, newArray26, 0, i26);
                        }
                        while (i26 < newArray26.length - 1) {
                            newArray26[i26] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray26[i26]);
                            input.readTag();
                            i26++;
                        }
                        newArray26[i26] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray26[i26]);
                        this.observedHotspotR2ApsInScanHistogram = newArray26;
                        break;
                    case 682:
                        int arrayLength27 = WireFormatNano.getRepeatedFieldArrayLength(input, 682);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr13 = this.observedHotspotR1EssInScanHistogram;
                        int i27 = numConnectableNetworksBucketArr13 == null ? 0 : numConnectableNetworksBucketArr13.length;
                        NumConnectableNetworksBucket[] newArray27 = new NumConnectableNetworksBucket[(i27 + arrayLength27)];
                        if (i27 != 0) {
                            System.arraycopy(this.observedHotspotR1EssInScanHistogram, 0, newArray27, 0, i27);
                        }
                        while (i27 < newArray27.length - 1) {
                            newArray27[i27] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray27[i27]);
                            input.readTag();
                            i27++;
                        }
                        newArray27[i27] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray27[i27]);
                        this.observedHotspotR1EssInScanHistogram = newArray27;
                        break;
                    case 690:
                        int arrayLength28 = WireFormatNano.getRepeatedFieldArrayLength(input, 690);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr14 = this.observedHotspotR2EssInScanHistogram;
                        int i28 = numConnectableNetworksBucketArr14 == null ? 0 : numConnectableNetworksBucketArr14.length;
                        NumConnectableNetworksBucket[] newArray28 = new NumConnectableNetworksBucket[(i28 + arrayLength28)];
                        if (i28 != 0) {
                            System.arraycopy(this.observedHotspotR2EssInScanHistogram, 0, newArray28, 0, i28);
                        }
                        while (i28 < newArray28.length - 1) {
                            newArray28[i28] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray28[i28]);
                            input.readTag();
                            i28++;
                        }
                        newArray28[i28] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray28[i28]);
                        this.observedHotspotR2EssInScanHistogram = newArray28;
                        break;
                    case 698:
                        int arrayLength29 = WireFormatNano.getRepeatedFieldArrayLength(input, 698);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr15 = this.observedHotspotR1ApsPerEssInScanHistogram;
                        int i29 = numConnectableNetworksBucketArr15 == null ? 0 : numConnectableNetworksBucketArr15.length;
                        NumConnectableNetworksBucket[] newArray29 = new NumConnectableNetworksBucket[(i29 + arrayLength29)];
                        if (i29 != 0) {
                            System.arraycopy(this.observedHotspotR1ApsPerEssInScanHistogram, 0, newArray29, 0, i29);
                        }
                        while (i29 < newArray29.length - 1) {
                            newArray29[i29] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray29[i29]);
                            input.readTag();
                            i29++;
                        }
                        newArray29[i29] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray29[i29]);
                        this.observedHotspotR1ApsPerEssInScanHistogram = newArray29;
                        break;
                    case 706:
                        int arrayLength30 = WireFormatNano.getRepeatedFieldArrayLength(input, 706);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr16 = this.observedHotspotR2ApsPerEssInScanHistogram;
                        int i30 = numConnectableNetworksBucketArr16 == null ? 0 : numConnectableNetworksBucketArr16.length;
                        NumConnectableNetworksBucket[] newArray30 = new NumConnectableNetworksBucket[(i30 + arrayLength30)];
                        if (i30 != 0) {
                            System.arraycopy(this.observedHotspotR2ApsPerEssInScanHistogram, 0, newArray30, 0, i30);
                        }
                        while (i30 < newArray30.length - 1) {
                            newArray30[i30] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray30[i30]);
                            input.readTag();
                            i30++;
                        }
                        newArray30[i30] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray30[i30]);
                        this.observedHotspotR2ApsPerEssInScanHistogram = newArray30;
                        break;
                    case 714:
                        int arrayLength31 = WireFormatNano.getRepeatedFieldArrayLength(input, 714);
                        SoftApConnectedClientsEvent[] softApConnectedClientsEventArr = this.softApConnectedClientsEventsTethered;
                        int i31 = softApConnectedClientsEventArr == null ? 0 : softApConnectedClientsEventArr.length;
                        SoftApConnectedClientsEvent[] newArray31 = new SoftApConnectedClientsEvent[(i31 + arrayLength31)];
                        if (i31 != 0) {
                            System.arraycopy(this.softApConnectedClientsEventsTethered, 0, newArray31, 0, i31);
                        }
                        while (i31 < newArray31.length - 1) {
                            newArray31[i31] = new SoftApConnectedClientsEvent();
                            input.readMessage(newArray31[i31]);
                            input.readTag();
                            i31++;
                        }
                        newArray31[i31] = new SoftApConnectedClientsEvent();
                        input.readMessage(newArray31[i31]);
                        this.softApConnectedClientsEventsTethered = newArray31;
                        break;
                    case 722:
                        int arrayLength32 = WireFormatNano.getRepeatedFieldArrayLength(input, 722);
                        SoftApConnectedClientsEvent[] softApConnectedClientsEventArr2 = this.softApConnectedClientsEventsLocalOnly;
                        int i32 = softApConnectedClientsEventArr2 == null ? 0 : softApConnectedClientsEventArr2.length;
                        SoftApConnectedClientsEvent[] newArray32 = new SoftApConnectedClientsEvent[(i32 + arrayLength32)];
                        if (i32 != 0) {
                            System.arraycopy(this.softApConnectedClientsEventsLocalOnly, 0, newArray32, 0, i32);
                        }
                        while (i32 < newArray32.length - 1) {
                            newArray32[i32] = new SoftApConnectedClientsEvent();
                            input.readMessage(newArray32[i32]);
                            input.readTag();
                            i32++;
                        }
                        newArray32[i32] = new SoftApConnectedClientsEvent();
                        input.readMessage(newArray32[i32]);
                        this.softApConnectedClientsEventsLocalOnly = newArray32;
                        break;
                    case 730:
                        if (this.wpsMetrics == null) {
                            this.wpsMetrics = new WpsMetrics();
                        }
                        input.readMessage(this.wpsMetrics);
                        break;
                    case 738:
                        if (this.wifiPowerStats == null) {
                            this.wifiPowerStats = new WifiPowerStats();
                        }
                        input.readMessage(this.wifiPowerStats);
                        break;
                    case 744:
                        this.numConnectivityOneshotScans = input.readInt32();
                        break;
                    case 754:
                        if (this.wifiWakeStats == null) {
                            this.wifiWakeStats = new WifiWakeStats();
                        }
                        input.readMessage(this.wifiWakeStats);
                        break;
                    case 762:
                        int arrayLength33 = WireFormatNano.getRepeatedFieldArrayLength(input, 762);
                        NumConnectableNetworksBucket[] numConnectableNetworksBucketArr17 = this.observed80211McSupportingApsInScanHistogram;
                        int i33 = numConnectableNetworksBucketArr17 == null ? 0 : numConnectableNetworksBucketArr17.length;
                        NumConnectableNetworksBucket[] newArray33 = new NumConnectableNetworksBucket[(i33 + arrayLength33)];
                        if (i33 != 0) {
                            System.arraycopy(this.observed80211McSupportingApsInScanHistogram, 0, newArray33, 0, i33);
                        }
                        while (i33 < newArray33.length - 1) {
                            newArray33[i33] = new NumConnectableNetworksBucket();
                            input.readMessage(newArray33[i33]);
                            input.readTag();
                            i33++;
                        }
                        newArray33[i33] = new NumConnectableNetworksBucket();
                        input.readMessage(newArray33[i33]);
                        this.observed80211McSupportingApsInScanHistogram = newArray33;
                        break;
                    case 768:
                        this.numSupplicantCrashes = input.readInt32();
                        break;
                    case 776:
                        this.numHostapdCrashes = input.readInt32();
                        break;
                    case 784:
                        this.numSetupClientInterfaceFailureDueToSupplicant = input.readInt32();
                        break;
                    case 792:
                        this.numSetupSoftApInterfaceFailureDueToHal = input.readInt32();
                        break;
                    case 800:
                        this.numSetupSoftApInterfaceFailureDueToWificond = input.readInt32();
                        break;
                    case 808:
                        this.numSetupSoftApInterfaceFailureDueToHostapd = input.readInt32();
                        break;
                    case 816:
                        this.numClientInterfaceDown = input.readInt32();
                        break;
                    case 824:
                        this.numSoftApInterfaceDown = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.NOTIFICATION_SNOOZED_CRITERIA /* 832 */:
                        this.numExternalAppOneshotScanRequests = input.readInt32();
                        break;
                    case 840:
                        this.numExternalForegroundAppOneshotScanRequestsThrottled = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.FIELD_SETTINGS_BUILD_NUMBER_DEVELOPER_MODE_ENABLED /* 848 */:
                        this.numExternalBackgroundAppOneshotScanRequestsThrottled = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.ACTION_NOTIFICATION_CHANNEL /* 856 */:
                        this.watchdogTriggerToConnectionSuccessDurationMs = input.readInt64();
                        break;
                    case MetricsProto.MetricsEvent.ACTION_GET_CONTACT /* 864 */:
                        this.watchdogTotalConnectionFailureCountAfterTrigger = input.readInt64();
                        break;
                    case 872:
                        this.numOneshotHasDfsChannelScans = input.readInt32();
                        break;
                    case 882:
                        if (this.wifiRttLog == null) {
                            this.wifiRttLog = new WifiRttLog();
                        }
                        input.readMessage(this.wifiRttLog);
                        break;
                    case MetricsProto.MetricsEvent.ACTION_APPOP_GRANT_SYSTEM_ALERT_WINDOW /* 888 */:
                        this.isMacRandomizationOn = input.readBool();
                        break;
                    case 896:
                        this.numRadioModeChangeToMcc = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.APP_TRANSITION_CALLING_PACKAGE_NAME /* 904 */:
                        this.numRadioModeChangeToScc = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.AUTOFILL_AUTHENTICATED /* 912 */:
                        this.numRadioModeChangeToSbs = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.METRICS_CHECKPOINT /* 920 */:
                        this.numRadioModeChangeToDbs = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.FIELD_QS_VALUE /* 928 */:
                        this.numSoftApUserBandPreferenceUnsatisfied = input.readInt32();
                        break;
                    case 938:
                        this.scoreExperimentId = input.readString();
                        break;
                    case MetricsProto.MetricsEvent.FIELD_NOTIFICATION_GROUP_ID /* 946 */:
                        if (this.wifiRadioUsage == null) {
                            this.wifiRadioUsage = new WifiRadioUsage();
                        }
                        input.readMessage(this.wifiRadioUsage);
                        break;
                    case 954:
                        if (this.experimentValues == null) {
                            this.experimentValues = new ExperimentValues();
                        }
                        input.readMessage(this.experimentValues);
                        break;
                    case 962:
                        int arrayLength34 = WireFormatNano.getRepeatedFieldArrayLength(input, 962);
                        WifiIsUnusableEvent[] wifiIsUnusableEventArr = this.wifiIsUnusableEventList;
                        int i34 = wifiIsUnusableEventArr == null ? 0 : wifiIsUnusableEventArr.length;
                        WifiIsUnusableEvent[] newArray34 = new WifiIsUnusableEvent[(i34 + arrayLength34)];
                        if (i34 != 0) {
                            System.arraycopy(this.wifiIsUnusableEventList, 0, newArray34, 0, i34);
                        }
                        while (i34 < newArray34.length - 1) {
                            newArray34[i34] = new WifiIsUnusableEvent();
                            input.readMessage(newArray34[i34]);
                            input.readTag();
                            i34++;
                        }
                        newArray34[i34] = new WifiIsUnusableEvent();
                        input.readMessage(newArray34[i34]);
                        this.wifiIsUnusableEventList = newArray34;
                        break;
                    case 970:
                        int arrayLength35 = WireFormatNano.getRepeatedFieldArrayLength(input, 970);
                        LinkSpeedCount[] linkSpeedCountArr = this.linkSpeedCounts;
                        int i35 = linkSpeedCountArr == null ? 0 : linkSpeedCountArr.length;
                        LinkSpeedCount[] newArray35 = new LinkSpeedCount[(i35 + arrayLength35)];
                        if (i35 != 0) {
                            System.arraycopy(this.linkSpeedCounts, 0, newArray35, 0, i35);
                        }
                        while (i35 < newArray35.length - 1) {
                            newArray35[i35] = new LinkSpeedCount();
                            input.readMessage(newArray35[i35]);
                            input.readTag();
                            i35++;
                        }
                        newArray35[i35] = new LinkSpeedCount();
                        input.readMessage(newArray35[i35]);
                        this.linkSpeedCounts = newArray35;
                        break;
                    case 976:
                        this.numSarSensorRegistrationFailures = input.readInt32();
                        break;
                    case MetricsProto.MetricsEvent.SETTINGS_GESTURE_CAMERA_LIFT_TRIGGER /* 986 */:
                        int arrayLength36 = WireFormatNano.getRepeatedFieldArrayLength(input, MetricsProto.MetricsEvent.SETTINGS_GESTURE_CAMERA_LIFT_TRIGGER);
                        PasspointProfileTypeCount[] passpointProfileTypeCountArr = this.installedPasspointProfileTypeForR1;
                        int i36 = passpointProfileTypeCountArr == null ? 0 : passpointProfileTypeCountArr.length;
                        PasspointProfileTypeCount[] newArray36 = new PasspointProfileTypeCount[(i36 + arrayLength36)];
                        if (i36 != 0) {
                            System.arraycopy(this.installedPasspointProfileTypeForR1, 0, newArray36, 0, i36);
                        }
                        while (i36 < newArray36.length - 1) {
                            newArray36[i36] = new PasspointProfileTypeCount();
                            input.readMessage(newArray36[i36]);
                            input.readTag();
                            i36++;
                        }
                        newArray36[i36] = new PasspointProfileTypeCount();
                        input.readMessage(newArray36[i36]);
                        this.installedPasspointProfileTypeForR1 = newArray36;
                        break;
                    case MetricsProto.MetricsEvent.FIELD_SETTINGS_PREFERENCE_CHANGE_LONG_VALUE /* 994 */:
                        this.hardwareRevision = input.readString();
                        break;
                    case 1002:
                        if (this.wifiLinkLayerUsageStats == null) {
                            this.wifiLinkLayerUsageStats = new WifiLinkLayerUsageStats();
                        }
                        input.readMessage(this.wifiLinkLayerUsageStats);
                        break;
                    case 1010:
                        int arrayLength37 = WireFormatNano.getRepeatedFieldArrayLength(input, 1010);
                        WifiUsabilityStats[] wifiUsabilityStatsArr = this.wifiUsabilityStatsList;
                        int i37 = wifiUsabilityStatsArr == null ? 0 : wifiUsabilityStatsArr.length;
                        WifiUsabilityStats[] newArray37 = new WifiUsabilityStats[(i37 + arrayLength37)];
                        if (i37 != 0) {
                            System.arraycopy(this.wifiUsabilityStatsList, 0, newArray37, 0, i37);
                        }
                        while (i37 < newArray37.length - 1) {
                            newArray37[i37] = new WifiUsabilityStats();
                            input.readMessage(newArray37[i37]);
                            input.readTag();
                            i37++;
                        }
                        newArray37[i37] = new WifiUsabilityStats();
                        input.readMessage(newArray37[i37]);
                        this.wifiUsabilityStatsList = newArray37;
                        break;
                    case 1018:
                        int arrayLength38 = WireFormatNano.getRepeatedFieldArrayLength(input, 1018);
                        WifiUsabilityScoreCount[] wifiUsabilityScoreCountArr = this.wifiUsabilityScoreCount;
                        int i38 = wifiUsabilityScoreCountArr == null ? 0 : wifiUsabilityScoreCountArr.length;
                        WifiUsabilityScoreCount[] newArray38 = new WifiUsabilityScoreCount[(i38 + arrayLength38)];
                        if (i38 != 0) {
                            System.arraycopy(this.wifiUsabilityScoreCount, 0, newArray38, 0, i38);
                        }
                        while (i38 < newArray38.length - 1) {
                            newArray38[i38] = new WifiUsabilityScoreCount();
                            input.readMessage(newArray38[i38]);
                            input.readTag();
                            i38++;
                        }
                        newArray38[i38] = new WifiUsabilityScoreCount();
                        input.readMessage(newArray38[i38]);
                        this.wifiUsabilityScoreCount = newArray38;
                        break;
                    case 1026:
                        int arrayLength39 = WireFormatNano.getRepeatedFieldArrayLength(input, 1026);
                        DeviceMobilityStatePnoScanStats[] deviceMobilityStatePnoScanStatsArr = this.mobilityStatePnoStatsList;
                        int i39 = deviceMobilityStatePnoScanStatsArr == null ? 0 : deviceMobilityStatePnoScanStatsArr.length;
                        DeviceMobilityStatePnoScanStats[] newArray39 = new DeviceMobilityStatePnoScanStats[(i39 + arrayLength39)];
                        if (i39 != 0) {
                            System.arraycopy(this.mobilityStatePnoStatsList, 0, newArray39, 0, i39);
                        }
                        while (i39 < newArray39.length - 1) {
                            newArray39[i39] = new DeviceMobilityStatePnoScanStats();
                            input.readMessage(newArray39[i39]);
                            input.readTag();
                            i39++;
                        }
                        newArray39[i39] = new DeviceMobilityStatePnoScanStats();
                        input.readMessage(newArray39[i39]);
                        this.mobilityStatePnoStatsList = newArray39;
                        break;
                    case 1034:
                        if (this.wifiP2PStats == null) {
                            this.wifiP2PStats = new WifiP2pStats();
                        }
                        input.readMessage(this.wifiP2PStats);
                        break;
                    case RILConstants.RIL_UNSOL_RADIO_CAPABILITY /* 1042 */:
                        if (this.wifiDppLog == null) {
                            this.wifiDppLog = new WifiDppLog();
                        }
                        input.readMessage(this.wifiDppLog);
                        break;
                    case 1048:
                        this.numEnhancedOpenNetworks = input.readInt32();
                        break;
                    case 1056:
                        this.numWpa3PersonalNetworks = input.readInt32();
                        break;
                    case 1064:
                        this.numWpa3EnterpriseNetworks = input.readInt32();
                        break;
                    case 1072:
                        this.numEnhancedOpenNetworkScanResults = input.readInt32();
                        break;
                    case 1080:
                        this.numWpa3PersonalNetworkScanResults = input.readInt32();
                        break;
                    case 1088:
                        this.numWpa3EnterpriseNetworkScanResults = input.readInt32();
                        break;
                    case 1098:
                        if (this.wifiConfigStoreIo == null) {
                            this.wifiConfigStoreIo = new WifiConfigStoreIO();
                        }
                        input.readMessage(this.wifiConfigStoreIo);
                        break;
                    case 1104:
                        this.numSavedNetworksWithMacRandomization = input.readInt32();
                        break;
                    case 1114:
                        if (this.linkProbeStats == null) {
                            this.linkProbeStats = new LinkProbeStats();
                        }
                        input.readMessage(this.linkProbeStats);
                        break;
                    case 1122:
                        int arrayLength40 = WireFormatNano.getRepeatedFieldArrayLength(input, 1122);
                        NetworkSelectionExperimentDecisions[] networkSelectionExperimentDecisionsArr = this.networkSelectionExperimentDecisionsList;
                        int i40 = networkSelectionExperimentDecisionsArr == null ? 0 : networkSelectionExperimentDecisionsArr.length;
                        NetworkSelectionExperimentDecisions[] newArray40 = new NetworkSelectionExperimentDecisions[(i40 + arrayLength40)];
                        if (i40 != 0) {
                            System.arraycopy(this.networkSelectionExperimentDecisionsList, 0, newArray40, 0, i40);
                        }
                        while (i40 < newArray40.length - 1) {
                            newArray40[i40] = new NetworkSelectionExperimentDecisions();
                            input.readMessage(newArray40[i40]);
                            input.readTag();
                            i40++;
                        }
                        newArray40[i40] = new NetworkSelectionExperimentDecisions();
                        input.readMessage(newArray40[i40]);
                        this.networkSelectionExperimentDecisionsList = newArray40;
                        break;
                    case MetricsProto.MetricsEvent.FIELD_AUTOFILL_SAVE_TYPE /* 1130 */:
                        if (this.wifiNetworkRequestApiLog == null) {
                            this.wifiNetworkRequestApiLog = new WifiNetworkRequestApiLog();
                        }
                        input.readMessage(this.wifiNetworkRequestApiLog);
                        break;
                    case MetricsProto.MetricsEvent.NOTIFICATION_SELECT_SNOOZE /* 1138 */:
                        if (this.wifiNetworkSuggestionApiLog == null) {
                            this.wifiNetworkSuggestionApiLog = new WifiNetworkSuggestionApiLog();
                        }
                        input.readMessage(this.wifiNetworkSuggestionApiLog);
                        break;
                    case 1146:
                        if (this.wifiLockStats == null) {
                            this.wifiLockStats = new WifiLockStats();
                        }
                        input.readMessage(this.wifiLockStats);
                        break;
                    case 1154:
                        if (this.wifiToggleStats == null) {
                            this.wifiToggleStats = new WifiToggleStats();
                        }
                        input.readMessage(this.wifiToggleStats);
                        break;
                    case 1160:
                        this.numAddOrUpdateNetworkCalls = input.readInt32();
                        break;
                    case 1168:
                        this.numEnableNetworkCalls = input.readInt32();
                        break;
                    case 1178:
                        if (this.passpointProvisionStats == null) {
                            this.passpointProvisionStats = new PasspointProvisionStats();
                        }
                        input.readMessage(this.passpointProvisionStats);
                        break;
                    case 1186:
                        int arrayLength41 = WireFormatNano.getRepeatedFieldArrayLength(input, 1186);
                        PasspointProfileTypeCount[] passpointProfileTypeCountArr2 = this.installedPasspointProfileTypeForR2;
                        int i41 = passpointProfileTypeCountArr2 == null ? 0 : passpointProfileTypeCountArr2.length;
                        PasspointProfileTypeCount[] newArray41 = new PasspointProfileTypeCount[(i41 + arrayLength41)];
                        if (i41 != 0) {
                            System.arraycopy(this.installedPasspointProfileTypeForR2, 0, newArray41, 0, i41);
                        }
                        while (i41 < newArray41.length - 1) {
                            newArray41[i41] = new PasspointProfileTypeCount();
                            input.readMessage(newArray41[i41]);
                            input.readTag();
                            i41++;
                        }
                        newArray41[i41] = new PasspointProfileTypeCount();
                        input.readMessage(newArray41[i41]);
                        this.installedPasspointProfileTypeForR2 = newArray41;
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static WifiLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiLog) MessageNano.mergeFrom(new WifiLog(), data);
        }

        public static WifiLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiLog().mergeFrom(input);
        }
    }

    public static final class RouterFingerPrint extends MessageNano {
        public static final int AUTH_ENTERPRISE = 3;
        public static final int AUTH_OPEN = 1;
        public static final int AUTH_PERSONAL = 2;
        public static final int AUTH_UNKNOWN = 0;
        public static final int ROAM_TYPE_DBDC = 3;
        public static final int ROAM_TYPE_ENTERPRISE = 2;
        public static final int ROAM_TYPE_NONE = 1;
        public static final int ROAM_TYPE_UNKNOWN = 0;
        public static final int ROUTER_TECH_A = 1;
        public static final int ROUTER_TECH_AC = 5;
        public static final int ROUTER_TECH_B = 2;
        public static final int ROUTER_TECH_G = 3;
        public static final int ROUTER_TECH_N = 4;
        public static final int ROUTER_TECH_OTHER = 6;
        public static final int ROUTER_TECH_UNKNOWN = 0;
        private static volatile RouterFingerPrint[] _emptyArray;
        public int authentication;
        public int channelInfo;
        public int dtim;
        public boolean hidden;
        public boolean passpoint;
        public int roamType;
        public int routerTechnology;
        public boolean supportsIpv6;

        public static RouterFingerPrint[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RouterFingerPrint[0];
                    }
                }
            }
            return _emptyArray;
        }

        public RouterFingerPrint() {
            clear();
        }

        public RouterFingerPrint clear() {
            this.roamType = 0;
            this.channelInfo = 0;
            this.dtim = 0;
            this.authentication = 0;
            this.hidden = false;
            this.routerTechnology = 0;
            this.supportsIpv6 = false;
            this.passpoint = false;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.roamType;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.channelInfo;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.dtim;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.authentication;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            boolean z = this.hidden;
            if (z) {
                output.writeBool(5, z);
            }
            int i5 = this.routerTechnology;
            if (i5 != 0) {
                output.writeInt32(6, i5);
            }
            boolean z2 = this.supportsIpv6;
            if (z2) {
                output.writeBool(7, z2);
            }
            boolean z3 = this.passpoint;
            if (z3) {
                output.writeBool(8, z3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.roamType;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.channelInfo;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.dtim;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.authentication;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            boolean z = this.hidden;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, z);
            }
            int i5 = this.routerTechnology;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i5);
            }
            boolean z2 = this.supportsIpv6;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, z2);
            }
            boolean z3 = this.passpoint;
            if (z3) {
                return size + CodedOutputByteBufferNano.computeBoolSize(8, z3);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public RouterFingerPrint mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3) {
                        this.roamType = value;
                    }
                } else if (tag == 16) {
                    this.channelInfo = input.readInt32();
                } else if (tag == 24) {
                    this.dtim = input.readInt32();
                } else if (tag == 32) {
                    int value2 = input.readInt32();
                    if (value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3) {
                        this.authentication = value2;
                    }
                } else if (tag == 40) {
                    this.hidden = input.readBool();
                } else if (tag == 48) {
                    int value3 = input.readInt32();
                    switch (value3) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            this.routerTechnology = value3;
                            continue;
                    }
                } else if (tag == 56) {
                    this.supportsIpv6 = input.readBool();
                } else if (tag == 64) {
                    this.passpoint = input.readBool();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static RouterFingerPrint parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (RouterFingerPrint) MessageNano.mergeFrom(new RouterFingerPrint(), data);
        }

        public static RouterFingerPrint parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new RouterFingerPrint().mergeFrom(input);
        }
    }

    public static final class ConnectionEvent extends MessageNano {
        public static final int AUTH_FAILURE_EAP_FAILURE = 4;
        public static final int AUTH_FAILURE_NONE = 1;
        public static final int AUTH_FAILURE_TIMEOUT = 2;
        public static final int AUTH_FAILURE_WRONG_PSWD = 3;
        public static final int FAILURE_REASON_UNKNOWN = 0;
        public static final int HLF_DHCP = 2;
        public static final int HLF_NONE = 1;
        public static final int HLF_NO_INTERNET = 3;
        public static final int HLF_UNKNOWN = 0;
        public static final int HLF_UNWANTED = 4;
        public static final int NOMINATOR_CARRIER = 5;
        public static final int NOMINATOR_EXTERNAL_SCORED = 6;
        public static final int NOMINATOR_MANUAL = 1;
        public static final int NOMINATOR_OPEN_NETWORK_AVAILABLE = 9;
        public static final int NOMINATOR_PASSPOINT = 4;
        public static final int NOMINATOR_SAVED = 2;
        public static final int NOMINATOR_SAVED_USER_CONNECT_CHOICE = 8;
        public static final int NOMINATOR_SPECIFIER = 7;
        public static final int NOMINATOR_SUGGESTION = 3;
        public static final int NOMINATOR_UNKNOWN = 0;
        public static final int ROAM_DBDC = 2;
        public static final int ROAM_ENTERPRISE = 3;
        public static final int ROAM_NONE = 1;
        public static final int ROAM_UNKNOWN = 0;
        public static final int ROAM_UNRELATED = 5;
        public static final int ROAM_USER_SELECTED = 4;
        private static volatile ConnectionEvent[] _emptyArray;
        public boolean automaticBugReportTaken;
        public int connectionNominator;
        public int connectionResult;
        public int connectivityLevelFailureCode;
        public int durationTakenToConnectMillis;
        public int level2FailureCode;
        public int level2FailureReason;
        public int networkSelectorExperimentId;
        public int roamType;
        public RouterFingerPrint routerFingerprint;
        public int signalStrength;
        public long startTimeMillis;
        public boolean useRandomizedMac;

        public static ConnectionEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ConnectionEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ConnectionEvent() {
            clear();
        }

        public ConnectionEvent clear() {
            this.startTimeMillis = 0;
            this.durationTakenToConnectMillis = 0;
            this.routerFingerprint = null;
            this.signalStrength = 0;
            this.roamType = 0;
            this.connectionResult = 0;
            this.level2FailureCode = 0;
            this.connectivityLevelFailureCode = 0;
            this.automaticBugReportTaken = false;
            this.useRandomizedMac = false;
            this.connectionNominator = 0;
            this.networkSelectorExperimentId = 0;
            this.level2FailureReason = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.startTimeMillis;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            int i = this.durationTakenToConnectMillis;
            if (i != 0) {
                output.writeInt32(2, i);
            }
            RouterFingerPrint routerFingerPrint = this.routerFingerprint;
            if (routerFingerPrint != null) {
                output.writeMessage(3, routerFingerPrint);
            }
            int i2 = this.signalStrength;
            if (i2 != 0) {
                output.writeInt32(4, i2);
            }
            int i3 = this.roamType;
            if (i3 != 0) {
                output.writeInt32(5, i3);
            }
            int i4 = this.connectionResult;
            if (i4 != 0) {
                output.writeInt32(6, i4);
            }
            int i5 = this.level2FailureCode;
            if (i5 != 0) {
                output.writeInt32(7, i5);
            }
            int i6 = this.connectivityLevelFailureCode;
            if (i6 != 0) {
                output.writeInt32(8, i6);
            }
            boolean z = this.automaticBugReportTaken;
            if (z) {
                output.writeBool(9, z);
            }
            boolean z2 = this.useRandomizedMac;
            if (z2) {
                output.writeBool(10, z2);
            }
            int i7 = this.connectionNominator;
            if (i7 != 0) {
                output.writeInt32(11, i7);
            }
            int i8 = this.networkSelectorExperimentId;
            if (i8 != 0) {
                output.writeInt32(12, i8);
            }
            int i9 = this.level2FailureReason;
            if (i9 != 0) {
                output.writeInt32(13, i9);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.startTimeMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            int i = this.durationTakenToConnectMillis;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            RouterFingerPrint routerFingerPrint = this.routerFingerprint;
            if (routerFingerPrint != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, routerFingerPrint);
            }
            int i2 = this.signalStrength;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i2);
            }
            int i3 = this.roamType;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i3);
            }
            int i4 = this.connectionResult;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i4);
            }
            int i5 = this.level2FailureCode;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i5);
            }
            int i6 = this.connectivityLevelFailureCode;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i6);
            }
            boolean z = this.automaticBugReportTaken;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(9, z);
            }
            boolean z2 = this.useRandomizedMac;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(10, z2);
            }
            int i7 = this.connectionNominator;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, i7);
            }
            int i8 = this.networkSelectorExperimentId;
            if (i8 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(12, i8);
            }
            int i9 = this.level2FailureReason;
            if (i9 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(13, i9);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public ConnectionEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.startTimeMillis = input.readInt64();
                        break;
                    case 16:
                        this.durationTakenToConnectMillis = input.readInt32();
                        break;
                    case 26:
                        if (this.routerFingerprint == null) {
                            this.routerFingerprint = new RouterFingerPrint();
                        }
                        input.readMessage(this.routerFingerprint);
                        break;
                    case 32:
                        this.signalStrength = input.readInt32();
                        break;
                    case 40:
                        int value = input.readInt32();
                        if (value != 0 && value != 1 && value != 2 && value != 3 && value != 4 && value != 5) {
                            break;
                        } else {
                            this.roamType = value;
                            break;
                        }
                    case 48:
                        this.connectionResult = input.readInt32();
                        break;
                    case 56:
                        this.level2FailureCode = input.readInt32();
                        break;
                    case 64:
                        int value2 = input.readInt32();
                        if (value2 != 0 && value2 != 1 && value2 != 2 && value2 != 3 && value2 != 4) {
                            break;
                        } else {
                            this.connectivityLevelFailureCode = value2;
                            break;
                        }
                    case 72:
                        this.automaticBugReportTaken = input.readBool();
                        break;
                    case 80:
                        this.useRandomizedMac = input.readBool();
                        break;
                    case 88:
                        int value3 = input.readInt32();
                        switch (value3) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                                this.connectionNominator = value3;
                                continue;
                        }
                    case 96:
                        this.networkSelectorExperimentId = input.readInt32();
                        break;
                    case 104:
                        int value4 = input.readInt32();
                        if (value4 != 0 && value4 != 1 && value4 != 2 && value4 != 3 && value4 != 4) {
                            break;
                        } else {
                            this.level2FailureReason = value4;
                            break;
                        }
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static ConnectionEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ConnectionEvent) MessageNano.mergeFrom(new ConnectionEvent(), data);
        }

        public static ConnectionEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ConnectionEvent().mergeFrom(input);
        }
    }

    public static final class RssiPollCount extends MessageNano {
        private static volatile RssiPollCount[] _emptyArray;
        public int count;
        public int frequency;
        public int rssi;

        public static RssiPollCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RssiPollCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public RssiPollCount() {
            clear();
        }

        public RssiPollCount clear() {
            this.rssi = 0;
            this.count = 0;
            this.frequency = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.rssi;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.frequency;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.rssi;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.frequency;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public RssiPollCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.rssi = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (tag == 24) {
                    this.frequency = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static RssiPollCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (RssiPollCount) MessageNano.mergeFrom(new RssiPollCount(), data);
        }

        public static RssiPollCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new RssiPollCount().mergeFrom(input);
        }
    }

    public static final class AlertReasonCount extends MessageNano {
        private static volatile AlertReasonCount[] _emptyArray;
        public int count;
        public int reason;

        public static AlertReasonCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new AlertReasonCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public AlertReasonCount() {
            clear();
        }

        public AlertReasonCount clear() {
            this.reason = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.reason;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.reason;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public AlertReasonCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.reason = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static AlertReasonCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (AlertReasonCount) MessageNano.mergeFrom(new AlertReasonCount(), data);
        }

        public static AlertReasonCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new AlertReasonCount().mergeFrom(input);
        }
    }

    public static final class WifiScoreCount extends MessageNano {
        private static volatile WifiScoreCount[] _emptyArray;
        public int count;
        public int score;

        public static WifiScoreCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiScoreCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiScoreCount() {
            clear();
        }

        public WifiScoreCount clear() {
            this.score = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.score;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.score;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiScoreCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.score = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiScoreCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiScoreCount) MessageNano.mergeFrom(new WifiScoreCount(), data);
        }

        public static WifiScoreCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiScoreCount().mergeFrom(input);
        }
    }

    public static final class WifiUsabilityScoreCount extends MessageNano {
        private static volatile WifiUsabilityScoreCount[] _emptyArray;
        public int count;
        public int score;

        public static WifiUsabilityScoreCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiUsabilityScoreCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiUsabilityScoreCount() {
            clear();
        }

        public WifiUsabilityScoreCount clear() {
            this.score = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.score;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.score;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiUsabilityScoreCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.score = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiUsabilityScoreCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiUsabilityScoreCount) MessageNano.mergeFrom(new WifiUsabilityScoreCount(), data);
        }

        public static WifiUsabilityScoreCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiUsabilityScoreCount().mergeFrom(input);
        }
    }

    public static final class LinkSpeedCount extends MessageNano {
        private static volatile LinkSpeedCount[] _emptyArray;
        public int count;
        public int linkSpeedMbps;
        public int rssiSumDbm;
        public long rssiSumOfSquaresDbmSq;

        public static LinkSpeedCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new LinkSpeedCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public LinkSpeedCount() {
            clear();
        }

        public LinkSpeedCount clear() {
            this.linkSpeedMbps = 0;
            this.count = 0;
            this.rssiSumDbm = 0;
            this.rssiSumOfSquaresDbmSq = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.linkSpeedMbps;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.rssiSumDbm;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            long j = this.rssiSumOfSquaresDbmSq;
            if (j != 0) {
                output.writeInt64(4, j);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.linkSpeedMbps;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.rssiSumDbm;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            long j = this.rssiSumOfSquaresDbmSq;
            if (j != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(4, j);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public LinkSpeedCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.linkSpeedMbps = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (tag == 24) {
                    this.rssiSumDbm = input.readInt32();
                } else if (tag == 32) {
                    this.rssiSumOfSquaresDbmSq = input.readInt64();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static LinkSpeedCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (LinkSpeedCount) MessageNano.mergeFrom(new LinkSpeedCount(), data);
        }

        public static LinkSpeedCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new LinkSpeedCount().mergeFrom(input);
        }
    }

    public static final class SoftApDurationBucket extends MessageNano {
        private static volatile SoftApDurationBucket[] _emptyArray;
        public int bucketSizeSec;
        public int count;
        public int durationSec;

        public static SoftApDurationBucket[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SoftApDurationBucket[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SoftApDurationBucket() {
            clear();
        }

        public SoftApDurationBucket clear() {
            this.durationSec = 0;
            this.bucketSizeSec = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.durationSec;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.bucketSizeSec;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.count;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.durationSec;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.bucketSizeSec;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.count;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public SoftApDurationBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.durationSec = input.readInt32();
                } else if (tag == 16) {
                    this.bucketSizeSec = input.readInt32();
                } else if (tag == 24) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static SoftApDurationBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (SoftApDurationBucket) MessageNano.mergeFrom(new SoftApDurationBucket(), data);
        }

        public static SoftApDurationBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new SoftApDurationBucket().mergeFrom(input);
        }
    }

    public static final class SoftApReturnCodeCount extends MessageNano {
        public static final int SOFT_AP_FAILED_GENERAL_ERROR = 2;
        public static final int SOFT_AP_FAILED_NO_CHANNEL = 3;
        public static final int SOFT_AP_RETURN_CODE_UNKNOWN = 0;
        public static final int SOFT_AP_STARTED_SUCCESSFULLY = 1;
        private static volatile SoftApReturnCodeCount[] _emptyArray;
        public int count;
        public int returnCode;
        public int startResult;

        public static SoftApReturnCodeCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SoftApReturnCodeCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SoftApReturnCodeCount() {
            clear();
        }

        public SoftApReturnCodeCount clear() {
            this.returnCode = 0;
            this.count = 0;
            this.startResult = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.returnCode;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.startResult;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.returnCode;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.startResult;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public SoftApReturnCodeCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.returnCode = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (tag == 24) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3) {
                        this.startResult = value;
                    }
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static SoftApReturnCodeCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (SoftApReturnCodeCount) MessageNano.mergeFrom(new SoftApReturnCodeCount(), data);
        }

        public static SoftApReturnCodeCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new SoftApReturnCodeCount().mergeFrom(input);
        }
    }

    public static final class StaEvent extends MessageNano {
        public static final int AUTH_FAILURE_EAP_FAILURE = 4;
        public static final int AUTH_FAILURE_NONE = 1;
        public static final int AUTH_FAILURE_TIMEOUT = 2;
        public static final int AUTH_FAILURE_UNKNOWN = 0;
        public static final int AUTH_FAILURE_WRONG_PSWD = 3;
        public static final int DISCONNECT_API = 1;
        public static final int DISCONNECT_GENERIC = 2;
        public static final int DISCONNECT_P2P_DISCONNECT_WIFI_REQUEST = 5;
        public static final int DISCONNECT_RESET_SIM_NETWORKS = 6;
        public static final int DISCONNECT_ROAM_WATCHDOG_TIMER = 4;
        public static final int DISCONNECT_UNKNOWN = 0;
        public static final int DISCONNECT_UNWANTED = 3;
        public static final int STATE_ASSOCIATED = 6;
        public static final int STATE_ASSOCIATING = 5;
        public static final int STATE_AUTHENTICATING = 4;
        public static final int STATE_COMPLETED = 9;
        public static final int STATE_DISCONNECTED = 0;
        public static final int STATE_DORMANT = 10;
        public static final int STATE_FOUR_WAY_HANDSHAKE = 7;
        public static final int STATE_GROUP_HANDSHAKE = 8;
        public static final int STATE_INACTIVE = 2;
        public static final int STATE_INTERFACE_DISABLED = 1;
        public static final int STATE_INVALID = 12;
        public static final int STATE_SCANNING = 3;
        public static final int STATE_UNINITIALIZED = 11;
        public static final int TYPE_ASSOCIATION_REJECTION_EVENT = 1;
        public static final int TYPE_AUTHENTICATION_FAILURE_EVENT = 2;
        public static final int TYPE_CMD_ASSOCIATED_BSSID = 6;
        public static final int TYPE_CMD_IP_CONFIGURATION_LOST = 8;
        public static final int TYPE_CMD_IP_CONFIGURATION_SUCCESSFUL = 7;
        public static final int TYPE_CMD_IP_REACHABILITY_LOST = 9;
        public static final int TYPE_CMD_START_CONNECT = 11;
        public static final int TYPE_CMD_START_ROAM = 12;
        public static final int TYPE_CMD_TARGET_BSSID = 10;
        public static final int TYPE_CONNECT_NETWORK = 13;
        public static final int TYPE_FRAMEWORK_DISCONNECT = 15;
        public static final int TYPE_LINK_PROBE = 21;
        public static final int TYPE_MAC_CHANGE = 17;
        public static final int TYPE_NETWORK_AGENT_VALID_NETWORK = 14;
        public static final int TYPE_NETWORK_CONNECTION_EVENT = 3;
        public static final int TYPE_NETWORK_DISCONNECTION_EVENT = 4;
        public static final int TYPE_SCORE_BREACH = 16;
        public static final int TYPE_SUPPLICANT_STATE_CHANGE_EVENT = 5;
        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_WIFI_DISABLED = 19;
        public static final int TYPE_WIFI_ENABLED = 18;
        public static final int TYPE_WIFI_USABILITY_SCORE_BREACH = 20;
        private static volatile StaEvent[] _emptyArray;
        public boolean associationTimedOut;
        public int authFailureReason;
        public ConfigInfo configInfo;
        public int frameworkDisconnectReason;
        public int lastFreq;
        public int lastLinkSpeed;
        public int lastPredictionHorizonSec;
        public int lastRssi;
        public int lastScore;
        public int lastWifiUsabilityScore;
        public int linkProbeFailureReason;
        public int linkProbeSuccessElapsedTimeMs;
        public boolean linkProbeWasSuccess;
        public boolean localGen;
        public int reason;
        public long startTimeMillis;
        public int status;
        public int supplicantStateChangesBitmask;
        public int type;

        public static final class ConfigInfo extends MessageNano {
            private static volatile ConfigInfo[] _emptyArray;
            public int allowedAuthAlgorithms;
            public int allowedGroupCiphers;
            public int allowedKeyManagement;
            public int allowedPairwiseCiphers;
            public int allowedProtocols;
            public boolean hasEverConnected;
            public boolean hiddenSsid;
            public boolean isEphemeral;
            public boolean isPasspoint;
            public int scanFreq;
            public int scanRssi;

            public static ConfigInfo[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ConfigInfo[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ConfigInfo() {
                clear();
            }

            public ConfigInfo clear() {
                this.allowedKeyManagement = 0;
                this.allowedProtocols = 0;
                this.allowedAuthAlgorithms = 0;
                this.allowedPairwiseCiphers = 0;
                this.allowedGroupCiphers = 0;
                this.hiddenSsid = false;
                this.isPasspoint = false;
                this.isEphemeral = false;
                this.hasEverConnected = false;
                this.scanRssi = -127;
                this.scanFreq = -1;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.allowedKeyManagement;
                if (i != 0) {
                    output.writeUInt32(1, i);
                }
                int i2 = this.allowedProtocols;
                if (i2 != 0) {
                    output.writeUInt32(2, i2);
                }
                int i3 = this.allowedAuthAlgorithms;
                if (i3 != 0) {
                    output.writeUInt32(3, i3);
                }
                int i4 = this.allowedPairwiseCiphers;
                if (i4 != 0) {
                    output.writeUInt32(4, i4);
                }
                int i5 = this.allowedGroupCiphers;
                if (i5 != 0) {
                    output.writeUInt32(5, i5);
                }
                boolean z = this.hiddenSsid;
                if (z) {
                    output.writeBool(6, z);
                }
                boolean z2 = this.isPasspoint;
                if (z2) {
                    output.writeBool(7, z2);
                }
                boolean z3 = this.isEphemeral;
                if (z3) {
                    output.writeBool(8, z3);
                }
                boolean z4 = this.hasEverConnected;
                if (z4) {
                    output.writeBool(9, z4);
                }
                int i6 = this.scanRssi;
                if (i6 != -127) {
                    output.writeInt32(10, i6);
                }
                int i7 = this.scanFreq;
                if (i7 != -1) {
                    output.writeInt32(11, i7);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.allowedKeyManagement;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeUInt32Size(1, i);
                }
                int i2 = this.allowedProtocols;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeUInt32Size(2, i2);
                }
                int i3 = this.allowedAuthAlgorithms;
                if (i3 != 0) {
                    size += CodedOutputByteBufferNano.computeUInt32Size(3, i3);
                }
                int i4 = this.allowedPairwiseCiphers;
                if (i4 != 0) {
                    size += CodedOutputByteBufferNano.computeUInt32Size(4, i4);
                }
                int i5 = this.allowedGroupCiphers;
                if (i5 != 0) {
                    size += CodedOutputByteBufferNano.computeUInt32Size(5, i5);
                }
                boolean z = this.hiddenSsid;
                if (z) {
                    size += CodedOutputByteBufferNano.computeBoolSize(6, z);
                }
                boolean z2 = this.isPasspoint;
                if (z2) {
                    size += CodedOutputByteBufferNano.computeBoolSize(7, z2);
                }
                boolean z3 = this.isEphemeral;
                if (z3) {
                    size += CodedOutputByteBufferNano.computeBoolSize(8, z3);
                }
                boolean z4 = this.hasEverConnected;
                if (z4) {
                    size += CodedOutputByteBufferNano.computeBoolSize(9, z4);
                }
                int i6 = this.scanRssi;
                if (i6 != -127) {
                    size += CodedOutputByteBufferNano.computeInt32Size(10, i6);
                }
                int i7 = this.scanFreq;
                if (i7 != -1) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(11, i7);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public ConfigInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            this.allowedKeyManagement = input.readUInt32();
                            break;
                        case 16:
                            this.allowedProtocols = input.readUInt32();
                            break;
                        case 24:
                            this.allowedAuthAlgorithms = input.readUInt32();
                            break;
                        case 32:
                            this.allowedPairwiseCiphers = input.readUInt32();
                            break;
                        case 40:
                            this.allowedGroupCiphers = input.readUInt32();
                            break;
                        case 48:
                            this.hiddenSsid = input.readBool();
                            break;
                        case 56:
                            this.isPasspoint = input.readBool();
                            break;
                        case 64:
                            this.isEphemeral = input.readBool();
                            break;
                        case 72:
                            this.hasEverConnected = input.readBool();
                            break;
                        case 80:
                            this.scanRssi = input.readInt32();
                            break;
                        case 88:
                            this.scanFreq = input.readInt32();
                            break;
                        default:
                            if (WireFormatNano.parseUnknownField(input, tag)) {
                                break;
                            } else {
                                return this;
                            }
                    }
                }
            }

            public static ConfigInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ConfigInfo) MessageNano.mergeFrom(new ConfigInfo(), data);
            }

            public static ConfigInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ConfigInfo().mergeFrom(input);
            }
        }

        public static StaEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new StaEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public StaEvent() {
            clear();
        }

        public StaEvent clear() {
            this.type = 0;
            this.reason = -1;
            this.status = -1;
            this.localGen = false;
            this.configInfo = null;
            this.lastRssi = -127;
            this.lastLinkSpeed = -1;
            this.lastFreq = -1;
            this.supplicantStateChangesBitmask = 0;
            this.startTimeMillis = 0;
            this.frameworkDisconnectReason = 0;
            this.associationTimedOut = false;
            this.authFailureReason = 0;
            this.lastScore = -1;
            this.lastWifiUsabilityScore = -1;
            this.lastPredictionHorizonSec = -1;
            this.linkProbeWasSuccess = false;
            this.linkProbeSuccessElapsedTimeMs = 0;
            this.linkProbeFailureReason = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.type;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.reason;
            if (i2 != -1) {
                output.writeInt32(2, i2);
            }
            int i3 = this.status;
            if (i3 != -1) {
                output.writeInt32(3, i3);
            }
            boolean z = this.localGen;
            if (z) {
                output.writeBool(4, z);
            }
            ConfigInfo configInfo2 = this.configInfo;
            if (configInfo2 != null) {
                output.writeMessage(5, configInfo2);
            }
            int i4 = this.lastRssi;
            if (i4 != -127) {
                output.writeInt32(6, i4);
            }
            int i5 = this.lastLinkSpeed;
            if (i5 != -1) {
                output.writeInt32(7, i5);
            }
            int i6 = this.lastFreq;
            if (i6 != -1) {
                output.writeInt32(8, i6);
            }
            int i7 = this.supplicantStateChangesBitmask;
            if (i7 != 0) {
                output.writeUInt32(9, i7);
            }
            long j = this.startTimeMillis;
            if (j != 0) {
                output.writeInt64(10, j);
            }
            int i8 = this.frameworkDisconnectReason;
            if (i8 != 0) {
                output.writeInt32(11, i8);
            }
            boolean z2 = this.associationTimedOut;
            if (z2) {
                output.writeBool(12, z2);
            }
            int i9 = this.authFailureReason;
            if (i9 != 0) {
                output.writeInt32(13, i9);
            }
            int i10 = this.lastScore;
            if (i10 != -1) {
                output.writeInt32(14, i10);
            }
            int i11 = this.lastWifiUsabilityScore;
            if (i11 != -1) {
                output.writeInt32(15, i11);
            }
            int i12 = this.lastPredictionHorizonSec;
            if (i12 != -1) {
                output.writeInt32(16, i12);
            }
            boolean z3 = this.linkProbeWasSuccess;
            if (z3) {
                output.writeBool(17, z3);
            }
            int i13 = this.linkProbeSuccessElapsedTimeMs;
            if (i13 != 0) {
                output.writeInt32(18, i13);
            }
            int i14 = this.linkProbeFailureReason;
            if (i14 != 0) {
                output.writeInt32(19, i14);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.type;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.reason;
            if (i2 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.status;
            if (i3 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            boolean z = this.localGen;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(4, z);
            }
            ConfigInfo configInfo2 = this.configInfo;
            if (configInfo2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(5, configInfo2);
            }
            int i4 = this.lastRssi;
            if (i4 != -127) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i4);
            }
            int i5 = this.lastLinkSpeed;
            if (i5 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i5);
            }
            int i6 = this.lastFreq;
            if (i6 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i6);
            }
            int i7 = this.supplicantStateChangesBitmask;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeUInt32Size(9, i7);
            }
            long j = this.startTimeMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(10, j);
            }
            int i8 = this.frameworkDisconnectReason;
            if (i8 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, i8);
            }
            boolean z2 = this.associationTimedOut;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(12, z2);
            }
            int i9 = this.authFailureReason;
            if (i9 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, i9);
            }
            int i10 = this.lastScore;
            if (i10 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(14, i10);
            }
            int i11 = this.lastWifiUsabilityScore;
            if (i11 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(15, i11);
            }
            int i12 = this.lastPredictionHorizonSec;
            if (i12 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(16, i12);
            }
            boolean z3 = this.linkProbeWasSuccess;
            if (z3) {
                size += CodedOutputByteBufferNano.computeBoolSize(17, z3);
            }
            int i13 = this.linkProbeSuccessElapsedTimeMs;
            if (i13 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(18, i13);
            }
            int i14 = this.linkProbeFailureReason;
            if (i14 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(19, i14);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public StaEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                                this.type = value;
                                continue;
                        }
                    case 16:
                        this.reason = input.readInt32();
                        break;
                    case 24:
                        this.status = input.readInt32();
                        break;
                    case 32:
                        this.localGen = input.readBool();
                        break;
                    case 42:
                        if (this.configInfo == null) {
                            this.configInfo = new ConfigInfo();
                        }
                        input.readMessage(this.configInfo);
                        break;
                    case 48:
                        this.lastRssi = input.readInt32();
                        break;
                    case 56:
                        this.lastLinkSpeed = input.readInt32();
                        break;
                    case 64:
                        this.lastFreq = input.readInt32();
                        break;
                    case 72:
                        this.supplicantStateChangesBitmask = input.readUInt32();
                        break;
                    case 80:
                        this.startTimeMillis = input.readInt64();
                        break;
                    case 88:
                        int value2 = input.readInt32();
                        switch (value2) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                                this.frameworkDisconnectReason = value2;
                                continue;
                        }
                    case 96:
                        this.associationTimedOut = input.readBool();
                        break;
                    case 104:
                        int value3 = input.readInt32();
                        if (value3 != 0 && value3 != 1 && value3 != 2 && value3 != 3 && value3 != 4) {
                            break;
                        } else {
                            this.authFailureReason = value3;
                            break;
                        }
                    case 112:
                        this.lastScore = input.readInt32();
                        break;
                    case 120:
                        this.lastWifiUsabilityScore = input.readInt32();
                        break;
                    case 128:
                        this.lastPredictionHorizonSec = input.readInt32();
                        break;
                    case 136:
                        this.linkProbeWasSuccess = input.readBool();
                        break;
                    case 144:
                        this.linkProbeSuccessElapsedTimeMs = input.readInt32();
                        break;
                    case 152:
                        int value4 = input.readInt32();
                        if (value4 != 0 && value4 != 1 && value4 != 2 && value4 != 3 && value4 != 4) {
                            break;
                        } else {
                            this.linkProbeFailureReason = value4;
                            break;
                        }
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static StaEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (StaEvent) MessageNano.mergeFrom(new StaEvent(), data);
        }

        public static StaEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new StaEvent().mergeFrom(input);
        }
    }

    public static final class WifiAwareLog extends MessageNano {
        public static final int ALREADY_ENABLED = 11;
        public static final int FOLLOWUP_TX_QUEUE_FULL = 12;
        public static final int INTERNAL_FAILURE = 2;
        public static final int INVALID_ARGS = 6;
        public static final int INVALID_NDP_ID = 8;
        public static final int INVALID_PEER_ID = 7;
        public static final int INVALID_SESSION_ID = 4;
        public static final int NAN_NOT_ALLOWED = 9;
        public static final int NO_OTA_ACK = 10;
        public static final int NO_RESOURCES_AVAILABLE = 5;
        public static final int PROTOCOL_FAILURE = 3;
        public static final int SUCCESS = 1;
        public static final int UNKNOWN = 0;
        public static final int UNKNOWN_HAL_STATUS = 14;
        public static final int UNSUPPORTED_CONCURRENCY_NAN_DISABLED = 13;
        private static volatile WifiAwareLog[] _emptyArray;
        public long availableTimeMs;
        public long enabledTimeMs;
        public HistogramBucket[] histogramAttachDurationMs;
        public NanStatusHistogramBucket[] histogramAttachSessionStatus;
        public HistogramBucket[] histogramAwareAvailableDurationMs;
        public HistogramBucket[] histogramAwareEnabledDurationMs;
        public HistogramBucket[] histogramNdpCreationTimeMs;
        public HistogramBucket[] histogramNdpSessionDataUsageMb;
        public HistogramBucket[] histogramNdpSessionDurationMs;
        public HistogramBucket[] histogramPublishSessionDurationMs;
        public NanStatusHistogramBucket[] histogramPublishStatus;
        public NanStatusHistogramBucket[] histogramRequestNdpOobStatus;
        public NanStatusHistogramBucket[] histogramRequestNdpStatus;
        public HistogramBucket[] histogramSubscribeGeofenceMax;
        public HistogramBucket[] histogramSubscribeGeofenceMin;
        public HistogramBucket[] histogramSubscribeSessionDurationMs;
        public NanStatusHistogramBucket[] histogramSubscribeStatus;
        public int maxConcurrentAttachSessionsInApp;
        public int maxConcurrentDiscoverySessionsInApp;
        public int maxConcurrentDiscoverySessionsInSystem;
        public int maxConcurrentNdiInApp;
        public int maxConcurrentNdiInSystem;
        public int maxConcurrentNdpInApp;
        public int maxConcurrentNdpInSystem;
        public int maxConcurrentNdpPerNdi;
        public int maxConcurrentPublishInApp;
        public int maxConcurrentPublishInSystem;
        public int maxConcurrentPublishWithRangingInApp;
        public int maxConcurrentPublishWithRangingInSystem;
        public int maxConcurrentSecureNdpInApp;
        public int maxConcurrentSecureNdpInSystem;
        public int maxConcurrentSubscribeInApp;
        public int maxConcurrentSubscribeInSystem;
        public int maxConcurrentSubscribeWithRangingInApp;
        public int maxConcurrentSubscribeWithRangingInSystem;
        public long ndpCreationTimeMsMax;
        public long ndpCreationTimeMsMin;
        public long ndpCreationTimeMsNumSamples;
        public long ndpCreationTimeMsSum;
        public long ndpCreationTimeMsSumOfSq;
        public int numApps;
        public int numAppsUsingIdentityCallback;
        public int numAppsWithDiscoverySessionFailureOutOfResources;
        public int numMatchesWithRanging;
        public int numMatchesWithoutRangingForRangingEnabledSubscribes;
        public int numSubscribesWithRanging;

        public static final class HistogramBucket extends MessageNano {
            private static volatile HistogramBucket[] _emptyArray;
            public int count;
            public long end;
            public long start;

            public static HistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new HistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public HistogramBucket() {
                clear();
            }

            public HistogramBucket clear() {
                this.start = 0;
                this.end = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                long j = this.start;
                if (j != 0) {
                    output.writeInt64(1, j);
                }
                long j2 = this.end;
                if (j2 != 0) {
                    output.writeInt64(2, j2);
                }
                int i = this.count;
                if (i != 0) {
                    output.writeInt32(3, i);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                long j = this.start;
                if (j != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(1, j);
                }
                long j2 = this.end;
                if (j2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(2, j2);
                }
                int i = this.count;
                if (i != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(3, i);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public HistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.start = input.readInt64();
                    } else if (tag == 16) {
                        this.end = input.readInt64();
                    } else if (tag == 24) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static HistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (HistogramBucket) MessageNano.mergeFrom(new HistogramBucket(), data);
            }

            public static HistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new HistogramBucket().mergeFrom(input);
            }
        }

        public static final class NanStatusHistogramBucket extends MessageNano {
            private static volatile NanStatusHistogramBucket[] _emptyArray;
            public int count;
            public int nanStatusType;

            public static NanStatusHistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new NanStatusHistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public NanStatusHistogramBucket() {
                clear();
            }

            public NanStatusHistogramBucket clear() {
                this.nanStatusType = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.nanStatusType;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.nanStatusType;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public NanStatusHistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                                this.nanStatusType = value;
                                continue;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static NanStatusHistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (NanStatusHistogramBucket) MessageNano.mergeFrom(new NanStatusHistogramBucket(), data);
            }

            public static NanStatusHistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new NanStatusHistogramBucket().mergeFrom(input);
            }
        }

        public static WifiAwareLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiAwareLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiAwareLog() {
            clear();
        }

        public WifiAwareLog clear() {
            this.numApps = 0;
            this.numAppsUsingIdentityCallback = 0;
            this.maxConcurrentAttachSessionsInApp = 0;
            this.histogramAttachSessionStatus = NanStatusHistogramBucket.emptyArray();
            this.maxConcurrentPublishInApp = 0;
            this.maxConcurrentSubscribeInApp = 0;
            this.maxConcurrentDiscoverySessionsInApp = 0;
            this.maxConcurrentPublishInSystem = 0;
            this.maxConcurrentSubscribeInSystem = 0;
            this.maxConcurrentDiscoverySessionsInSystem = 0;
            this.histogramPublishStatus = NanStatusHistogramBucket.emptyArray();
            this.histogramSubscribeStatus = NanStatusHistogramBucket.emptyArray();
            this.numAppsWithDiscoverySessionFailureOutOfResources = 0;
            this.histogramRequestNdpStatus = NanStatusHistogramBucket.emptyArray();
            this.histogramRequestNdpOobStatus = NanStatusHistogramBucket.emptyArray();
            this.maxConcurrentNdiInApp = 0;
            this.maxConcurrentNdiInSystem = 0;
            this.maxConcurrentNdpInApp = 0;
            this.maxConcurrentNdpInSystem = 0;
            this.maxConcurrentSecureNdpInApp = 0;
            this.maxConcurrentSecureNdpInSystem = 0;
            this.maxConcurrentNdpPerNdi = 0;
            this.histogramAwareAvailableDurationMs = HistogramBucket.emptyArray();
            this.histogramAwareEnabledDurationMs = HistogramBucket.emptyArray();
            this.histogramAttachDurationMs = HistogramBucket.emptyArray();
            this.histogramPublishSessionDurationMs = HistogramBucket.emptyArray();
            this.histogramSubscribeSessionDurationMs = HistogramBucket.emptyArray();
            this.histogramNdpSessionDurationMs = HistogramBucket.emptyArray();
            this.histogramNdpSessionDataUsageMb = HistogramBucket.emptyArray();
            this.histogramNdpCreationTimeMs = HistogramBucket.emptyArray();
            this.ndpCreationTimeMsMin = 0;
            this.ndpCreationTimeMsMax = 0;
            this.ndpCreationTimeMsSum = 0;
            this.ndpCreationTimeMsSumOfSq = 0;
            this.ndpCreationTimeMsNumSamples = 0;
            this.availableTimeMs = 0;
            this.enabledTimeMs = 0;
            this.maxConcurrentPublishWithRangingInApp = 0;
            this.maxConcurrentSubscribeWithRangingInApp = 0;
            this.maxConcurrentPublishWithRangingInSystem = 0;
            this.maxConcurrentSubscribeWithRangingInSystem = 0;
            this.histogramSubscribeGeofenceMin = HistogramBucket.emptyArray();
            this.histogramSubscribeGeofenceMax = HistogramBucket.emptyArray();
            this.numSubscribesWithRanging = 0;
            this.numMatchesWithRanging = 0;
            this.numMatchesWithoutRangingForRangingEnabledSubscribes = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numApps;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numAppsUsingIdentityCallback;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.maxConcurrentAttachSessionsInApp;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr = this.histogramAttachSessionStatus;
            if (nanStatusHistogramBucketArr != null && nanStatusHistogramBucketArr.length > 0) {
                int i4 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr2 = this.histogramAttachSessionStatus;
                    if (i4 >= nanStatusHistogramBucketArr2.length) {
                        break;
                    }
                    NanStatusHistogramBucket element = nanStatusHistogramBucketArr2[i4];
                    if (element != null) {
                        output.writeMessage(4, element);
                    }
                    i4++;
                }
            }
            int i5 = this.maxConcurrentPublishInApp;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            int i6 = this.maxConcurrentSubscribeInApp;
            if (i6 != 0) {
                output.writeInt32(6, i6);
            }
            int i7 = this.maxConcurrentDiscoverySessionsInApp;
            if (i7 != 0) {
                output.writeInt32(7, i7);
            }
            int i8 = this.maxConcurrentPublishInSystem;
            if (i8 != 0) {
                output.writeInt32(8, i8);
            }
            int i9 = this.maxConcurrentSubscribeInSystem;
            if (i9 != 0) {
                output.writeInt32(9, i9);
            }
            int i10 = this.maxConcurrentDiscoverySessionsInSystem;
            if (i10 != 0) {
                output.writeInt32(10, i10);
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr3 = this.histogramPublishStatus;
            if (nanStatusHistogramBucketArr3 != null && nanStatusHistogramBucketArr3.length > 0) {
                int i11 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr4 = this.histogramPublishStatus;
                    if (i11 >= nanStatusHistogramBucketArr4.length) {
                        break;
                    }
                    NanStatusHistogramBucket element2 = nanStatusHistogramBucketArr4[i11];
                    if (element2 != null) {
                        output.writeMessage(11, element2);
                    }
                    i11++;
                }
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr5 = this.histogramSubscribeStatus;
            if (nanStatusHistogramBucketArr5 != null && nanStatusHistogramBucketArr5.length > 0) {
                int i12 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr6 = this.histogramSubscribeStatus;
                    if (i12 >= nanStatusHistogramBucketArr6.length) {
                        break;
                    }
                    NanStatusHistogramBucket element3 = nanStatusHistogramBucketArr6[i12];
                    if (element3 != null) {
                        output.writeMessage(12, element3);
                    }
                    i12++;
                }
            }
            int i13 = this.numAppsWithDiscoverySessionFailureOutOfResources;
            if (i13 != 0) {
                output.writeInt32(13, i13);
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr7 = this.histogramRequestNdpStatus;
            if (nanStatusHistogramBucketArr7 != null && nanStatusHistogramBucketArr7.length > 0) {
                int i14 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr8 = this.histogramRequestNdpStatus;
                    if (i14 >= nanStatusHistogramBucketArr8.length) {
                        break;
                    }
                    NanStatusHistogramBucket element4 = nanStatusHistogramBucketArr8[i14];
                    if (element4 != null) {
                        output.writeMessage(14, element4);
                    }
                    i14++;
                }
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr9 = this.histogramRequestNdpOobStatus;
            if (nanStatusHistogramBucketArr9 != null && nanStatusHistogramBucketArr9.length > 0) {
                int i15 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr10 = this.histogramRequestNdpOobStatus;
                    if (i15 >= nanStatusHistogramBucketArr10.length) {
                        break;
                    }
                    NanStatusHistogramBucket element5 = nanStatusHistogramBucketArr10[i15];
                    if (element5 != null) {
                        output.writeMessage(15, element5);
                    }
                    i15++;
                }
            }
            int i16 = this.maxConcurrentNdiInApp;
            if (i16 != 0) {
                output.writeInt32(19, i16);
            }
            int i17 = this.maxConcurrentNdiInSystem;
            if (i17 != 0) {
                output.writeInt32(20, i17);
            }
            int i18 = this.maxConcurrentNdpInApp;
            if (i18 != 0) {
                output.writeInt32(21, i18);
            }
            int i19 = this.maxConcurrentNdpInSystem;
            if (i19 != 0) {
                output.writeInt32(22, i19);
            }
            int i20 = this.maxConcurrentSecureNdpInApp;
            if (i20 != 0) {
                output.writeInt32(23, i20);
            }
            int i21 = this.maxConcurrentSecureNdpInSystem;
            if (i21 != 0) {
                output.writeInt32(24, i21);
            }
            int i22 = this.maxConcurrentNdpPerNdi;
            if (i22 != 0) {
                output.writeInt32(25, i22);
            }
            HistogramBucket[] histogramBucketArr = this.histogramAwareAvailableDurationMs;
            if (histogramBucketArr != null && histogramBucketArr.length > 0) {
                int i23 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr2 = this.histogramAwareAvailableDurationMs;
                    if (i23 >= histogramBucketArr2.length) {
                        break;
                    }
                    HistogramBucket element6 = histogramBucketArr2[i23];
                    if (element6 != null) {
                        output.writeMessage(26, element6);
                    }
                    i23++;
                }
            }
            HistogramBucket[] histogramBucketArr3 = this.histogramAwareEnabledDurationMs;
            if (histogramBucketArr3 != null && histogramBucketArr3.length > 0) {
                int i24 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr4 = this.histogramAwareEnabledDurationMs;
                    if (i24 >= histogramBucketArr4.length) {
                        break;
                    }
                    HistogramBucket element7 = histogramBucketArr4[i24];
                    if (element7 != null) {
                        output.writeMessage(27, element7);
                    }
                    i24++;
                }
            }
            HistogramBucket[] histogramBucketArr5 = this.histogramAttachDurationMs;
            if (histogramBucketArr5 != null && histogramBucketArr5.length > 0) {
                int i25 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr6 = this.histogramAttachDurationMs;
                    if (i25 >= histogramBucketArr6.length) {
                        break;
                    }
                    HistogramBucket element8 = histogramBucketArr6[i25];
                    if (element8 != null) {
                        output.writeMessage(28, element8);
                    }
                    i25++;
                }
            }
            HistogramBucket[] histogramBucketArr7 = this.histogramPublishSessionDurationMs;
            if (histogramBucketArr7 != null && histogramBucketArr7.length > 0) {
                int i26 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr8 = this.histogramPublishSessionDurationMs;
                    if (i26 >= histogramBucketArr8.length) {
                        break;
                    }
                    HistogramBucket element9 = histogramBucketArr8[i26];
                    if (element9 != null) {
                        output.writeMessage(29, element9);
                    }
                    i26++;
                }
            }
            HistogramBucket[] histogramBucketArr9 = this.histogramSubscribeSessionDurationMs;
            if (histogramBucketArr9 != null && histogramBucketArr9.length > 0) {
                int i27 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr10 = this.histogramSubscribeSessionDurationMs;
                    if (i27 >= histogramBucketArr10.length) {
                        break;
                    }
                    HistogramBucket element10 = histogramBucketArr10[i27];
                    if (element10 != null) {
                        output.writeMessage(30, element10);
                    }
                    i27++;
                }
            }
            HistogramBucket[] histogramBucketArr11 = this.histogramNdpSessionDurationMs;
            if (histogramBucketArr11 != null && histogramBucketArr11.length > 0) {
                int i28 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr12 = this.histogramNdpSessionDurationMs;
                    if (i28 >= histogramBucketArr12.length) {
                        break;
                    }
                    HistogramBucket element11 = histogramBucketArr12[i28];
                    if (element11 != null) {
                        output.writeMessage(31, element11);
                    }
                    i28++;
                }
            }
            HistogramBucket[] histogramBucketArr13 = this.histogramNdpSessionDataUsageMb;
            if (histogramBucketArr13 != null && histogramBucketArr13.length > 0) {
                int i29 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr14 = this.histogramNdpSessionDataUsageMb;
                    if (i29 >= histogramBucketArr14.length) {
                        break;
                    }
                    HistogramBucket element12 = histogramBucketArr14[i29];
                    if (element12 != null) {
                        output.writeMessage(32, element12);
                    }
                    i29++;
                }
            }
            HistogramBucket[] histogramBucketArr15 = this.histogramNdpCreationTimeMs;
            if (histogramBucketArr15 != null && histogramBucketArr15.length > 0) {
                int i30 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr16 = this.histogramNdpCreationTimeMs;
                    if (i30 >= histogramBucketArr16.length) {
                        break;
                    }
                    HistogramBucket element13 = histogramBucketArr16[i30];
                    if (element13 != null) {
                        output.writeMessage(33, element13);
                    }
                    i30++;
                }
            }
            long j = this.ndpCreationTimeMsMin;
            if (j != 0) {
                output.writeInt64(34, j);
            }
            long j2 = this.ndpCreationTimeMsMax;
            if (j2 != 0) {
                output.writeInt64(35, j2);
            }
            long j3 = this.ndpCreationTimeMsSum;
            if (j3 != 0) {
                output.writeInt64(36, j3);
            }
            long j4 = this.ndpCreationTimeMsSumOfSq;
            if (j4 != 0) {
                output.writeInt64(37, j4);
            }
            long j5 = this.ndpCreationTimeMsNumSamples;
            if (j5 != 0) {
                output.writeInt64(38, j5);
            }
            long j6 = this.availableTimeMs;
            if (j6 != 0) {
                output.writeInt64(39, j6);
            }
            long j7 = this.enabledTimeMs;
            if (j7 != 0) {
                output.writeInt64(40, j7);
            }
            int i31 = this.maxConcurrentPublishWithRangingInApp;
            if (i31 != 0) {
                output.writeInt32(41, i31);
            }
            int i32 = this.maxConcurrentSubscribeWithRangingInApp;
            if (i32 != 0) {
                output.writeInt32(42, i32);
            }
            int i33 = this.maxConcurrentPublishWithRangingInSystem;
            if (i33 != 0) {
                output.writeInt32(43, i33);
            }
            int i34 = this.maxConcurrentSubscribeWithRangingInSystem;
            if (i34 != 0) {
                output.writeInt32(44, i34);
            }
            HistogramBucket[] histogramBucketArr17 = this.histogramSubscribeGeofenceMin;
            if (histogramBucketArr17 != null && histogramBucketArr17.length > 0) {
                int i35 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr18 = this.histogramSubscribeGeofenceMin;
                    if (i35 >= histogramBucketArr18.length) {
                        break;
                    }
                    HistogramBucket element14 = histogramBucketArr18[i35];
                    if (element14 != null) {
                        output.writeMessage(45, element14);
                    }
                    i35++;
                }
            }
            HistogramBucket[] histogramBucketArr19 = this.histogramSubscribeGeofenceMax;
            if (histogramBucketArr19 != null && histogramBucketArr19.length > 0) {
                int i36 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr20 = this.histogramSubscribeGeofenceMax;
                    if (i36 >= histogramBucketArr20.length) {
                        break;
                    }
                    HistogramBucket element15 = histogramBucketArr20[i36];
                    if (element15 != null) {
                        output.writeMessage(46, element15);
                    }
                    i36++;
                }
            }
            int i37 = this.numSubscribesWithRanging;
            if (i37 != 0) {
                output.writeInt32(47, i37);
            }
            int i38 = this.numMatchesWithRanging;
            if (i38 != 0) {
                output.writeInt32(48, i38);
            }
            int i39 = this.numMatchesWithoutRangingForRangingEnabledSubscribes;
            if (i39 != 0) {
                output.writeInt32(49, i39);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numApps;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numAppsUsingIdentityCallback;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.maxConcurrentAttachSessionsInApp;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr = this.histogramAttachSessionStatus;
            if (nanStatusHistogramBucketArr != null && nanStatusHistogramBucketArr.length > 0) {
                int i4 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr2 = this.histogramAttachSessionStatus;
                    if (i4 >= nanStatusHistogramBucketArr2.length) {
                        break;
                    }
                    NanStatusHistogramBucket element = nanStatusHistogramBucketArr2[i4];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element);
                    }
                    i4++;
                }
            }
            int i5 = this.maxConcurrentPublishInApp;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            int i6 = this.maxConcurrentSubscribeInApp;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i6);
            }
            int i7 = this.maxConcurrentDiscoverySessionsInApp;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i7);
            }
            int i8 = this.maxConcurrentPublishInSystem;
            if (i8 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i8);
            }
            int i9 = this.maxConcurrentSubscribeInSystem;
            if (i9 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, i9);
            }
            int i10 = this.maxConcurrentDiscoverySessionsInSystem;
            if (i10 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(10, i10);
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr3 = this.histogramPublishStatus;
            if (nanStatusHistogramBucketArr3 != null && nanStatusHistogramBucketArr3.length > 0) {
                int i11 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr4 = this.histogramPublishStatus;
                    if (i11 >= nanStatusHistogramBucketArr4.length) {
                        break;
                    }
                    NanStatusHistogramBucket element2 = nanStatusHistogramBucketArr4[i11];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(11, element2);
                    }
                    i11++;
                }
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr5 = this.histogramSubscribeStatus;
            if (nanStatusHistogramBucketArr5 != null && nanStatusHistogramBucketArr5.length > 0) {
                int i12 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr6 = this.histogramSubscribeStatus;
                    if (i12 >= nanStatusHistogramBucketArr6.length) {
                        break;
                    }
                    NanStatusHistogramBucket element3 = nanStatusHistogramBucketArr6[i12];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(12, element3);
                    }
                    i12++;
                }
            }
            int i13 = this.numAppsWithDiscoverySessionFailureOutOfResources;
            if (i13 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, i13);
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr7 = this.histogramRequestNdpStatus;
            if (nanStatusHistogramBucketArr7 != null && nanStatusHistogramBucketArr7.length > 0) {
                int i14 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr8 = this.histogramRequestNdpStatus;
                    if (i14 >= nanStatusHistogramBucketArr8.length) {
                        break;
                    }
                    NanStatusHistogramBucket element4 = nanStatusHistogramBucketArr8[i14];
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(14, element4);
                    }
                    i14++;
                }
            }
            NanStatusHistogramBucket[] nanStatusHistogramBucketArr9 = this.histogramRequestNdpOobStatus;
            if (nanStatusHistogramBucketArr9 != null && nanStatusHistogramBucketArr9.length > 0) {
                int i15 = 0;
                while (true) {
                    NanStatusHistogramBucket[] nanStatusHistogramBucketArr10 = this.histogramRequestNdpOobStatus;
                    if (i15 >= nanStatusHistogramBucketArr10.length) {
                        break;
                    }
                    NanStatusHistogramBucket element5 = nanStatusHistogramBucketArr10[i15];
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(15, element5);
                    }
                    i15++;
                }
            }
            int i16 = this.maxConcurrentNdiInApp;
            if (i16 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(19, i16);
            }
            int i17 = this.maxConcurrentNdiInSystem;
            if (i17 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(20, i17);
            }
            int i18 = this.maxConcurrentNdpInApp;
            if (i18 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(21, i18);
            }
            int i19 = this.maxConcurrentNdpInSystem;
            if (i19 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(22, i19);
            }
            int i20 = this.maxConcurrentSecureNdpInApp;
            if (i20 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(23, i20);
            }
            int i21 = this.maxConcurrentSecureNdpInSystem;
            if (i21 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(24, i21);
            }
            int i22 = this.maxConcurrentNdpPerNdi;
            if (i22 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(25, i22);
            }
            HistogramBucket[] histogramBucketArr = this.histogramAwareAvailableDurationMs;
            if (histogramBucketArr != null && histogramBucketArr.length > 0) {
                int i23 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr2 = this.histogramAwareAvailableDurationMs;
                    if (i23 >= histogramBucketArr2.length) {
                        break;
                    }
                    HistogramBucket element6 = histogramBucketArr2[i23];
                    if (element6 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(26, element6);
                    }
                    i23++;
                }
            }
            HistogramBucket[] histogramBucketArr3 = this.histogramAwareEnabledDurationMs;
            if (histogramBucketArr3 != null && histogramBucketArr3.length > 0) {
                int i24 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr4 = this.histogramAwareEnabledDurationMs;
                    if (i24 >= histogramBucketArr4.length) {
                        break;
                    }
                    HistogramBucket element7 = histogramBucketArr4[i24];
                    if (element7 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(27, element7);
                    }
                    i24++;
                }
            }
            HistogramBucket[] histogramBucketArr5 = this.histogramAttachDurationMs;
            if (histogramBucketArr5 != null && histogramBucketArr5.length > 0) {
                int i25 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr6 = this.histogramAttachDurationMs;
                    if (i25 >= histogramBucketArr6.length) {
                        break;
                    }
                    HistogramBucket element8 = histogramBucketArr6[i25];
                    if (element8 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(28, element8);
                    }
                    i25++;
                }
            }
            HistogramBucket[] histogramBucketArr7 = this.histogramPublishSessionDurationMs;
            if (histogramBucketArr7 != null && histogramBucketArr7.length > 0) {
                int i26 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr8 = this.histogramPublishSessionDurationMs;
                    if (i26 >= histogramBucketArr8.length) {
                        break;
                    }
                    HistogramBucket element9 = histogramBucketArr8[i26];
                    if (element9 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(29, element9);
                    }
                    i26++;
                }
            }
            HistogramBucket[] histogramBucketArr9 = this.histogramSubscribeSessionDurationMs;
            if (histogramBucketArr9 != null && histogramBucketArr9.length > 0) {
                int i27 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr10 = this.histogramSubscribeSessionDurationMs;
                    if (i27 >= histogramBucketArr10.length) {
                        break;
                    }
                    HistogramBucket element10 = histogramBucketArr10[i27];
                    if (element10 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(30, element10);
                    }
                    i27++;
                }
            }
            HistogramBucket[] histogramBucketArr11 = this.histogramNdpSessionDurationMs;
            if (histogramBucketArr11 != null && histogramBucketArr11.length > 0) {
                int i28 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr12 = this.histogramNdpSessionDurationMs;
                    if (i28 >= histogramBucketArr12.length) {
                        break;
                    }
                    HistogramBucket element11 = histogramBucketArr12[i28];
                    if (element11 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(31, element11);
                    }
                    i28++;
                }
            }
            HistogramBucket[] histogramBucketArr13 = this.histogramNdpSessionDataUsageMb;
            if (histogramBucketArr13 != null && histogramBucketArr13.length > 0) {
                int i29 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr14 = this.histogramNdpSessionDataUsageMb;
                    if (i29 >= histogramBucketArr14.length) {
                        break;
                    }
                    HistogramBucket element12 = histogramBucketArr14[i29];
                    if (element12 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(32, element12);
                    }
                    i29++;
                }
            }
            HistogramBucket[] histogramBucketArr15 = this.histogramNdpCreationTimeMs;
            if (histogramBucketArr15 != null && histogramBucketArr15.length > 0) {
                int i30 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr16 = this.histogramNdpCreationTimeMs;
                    if (i30 >= histogramBucketArr16.length) {
                        break;
                    }
                    HistogramBucket element13 = histogramBucketArr16[i30];
                    if (element13 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(33, element13);
                    }
                    i30++;
                }
            }
            long j = this.ndpCreationTimeMsMin;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(34, j);
            }
            long j2 = this.ndpCreationTimeMsMax;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(35, j2);
            }
            long j3 = this.ndpCreationTimeMsSum;
            if (j3 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(36, j3);
            }
            long j4 = this.ndpCreationTimeMsSumOfSq;
            if (j4 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(37, j4);
            }
            long j5 = this.ndpCreationTimeMsNumSamples;
            if (j5 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(38, j5);
            }
            long j6 = this.availableTimeMs;
            if (j6 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(39, j6);
            }
            long j7 = this.enabledTimeMs;
            if (j7 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(40, j7);
            }
            int i31 = this.maxConcurrentPublishWithRangingInApp;
            if (i31 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(41, i31);
            }
            int i32 = this.maxConcurrentSubscribeWithRangingInApp;
            if (i32 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(42, i32);
            }
            int i33 = this.maxConcurrentPublishWithRangingInSystem;
            if (i33 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(43, i33);
            }
            int i34 = this.maxConcurrentSubscribeWithRangingInSystem;
            if (i34 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(44, i34);
            }
            HistogramBucket[] histogramBucketArr17 = this.histogramSubscribeGeofenceMin;
            if (histogramBucketArr17 != null && histogramBucketArr17.length > 0) {
                int i35 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr18 = this.histogramSubscribeGeofenceMin;
                    if (i35 >= histogramBucketArr18.length) {
                        break;
                    }
                    HistogramBucket element14 = histogramBucketArr18[i35];
                    if (element14 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(45, element14);
                    }
                    i35++;
                }
            }
            HistogramBucket[] histogramBucketArr19 = this.histogramSubscribeGeofenceMax;
            if (histogramBucketArr19 != null && histogramBucketArr19.length > 0) {
                int i36 = 0;
                while (true) {
                    HistogramBucket[] histogramBucketArr20 = this.histogramSubscribeGeofenceMax;
                    if (i36 >= histogramBucketArr20.length) {
                        break;
                    }
                    HistogramBucket element15 = histogramBucketArr20[i36];
                    if (element15 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(46, element15);
                    }
                    i36++;
                }
            }
            int i37 = this.numSubscribesWithRanging;
            if (i37 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(47, i37);
            }
            int i38 = this.numMatchesWithRanging;
            if (i38 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(48, i38);
            }
            int i39 = this.numMatchesWithoutRangingForRangingEnabledSubscribes;
            if (i39 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(49, i39);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiAwareLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.numApps = input.readInt32();
                        break;
                    case 16:
                        this.numAppsUsingIdentityCallback = input.readInt32();
                        break;
                    case 24:
                        this.maxConcurrentAttachSessionsInApp = input.readInt32();
                        break;
                    case 34:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        NanStatusHistogramBucket[] nanStatusHistogramBucketArr = this.histogramAttachSessionStatus;
                        int i = nanStatusHistogramBucketArr == null ? 0 : nanStatusHistogramBucketArr.length;
                        NanStatusHistogramBucket[] newArray = new NanStatusHistogramBucket[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.histogramAttachSessionStatus, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new NanStatusHistogramBucket();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new NanStatusHistogramBucket();
                        input.readMessage(newArray[i]);
                        this.histogramAttachSessionStatus = newArray;
                        break;
                    case 40:
                        this.maxConcurrentPublishInApp = input.readInt32();
                        break;
                    case 48:
                        this.maxConcurrentSubscribeInApp = input.readInt32();
                        break;
                    case 56:
                        this.maxConcurrentDiscoverySessionsInApp = input.readInt32();
                        break;
                    case 64:
                        this.maxConcurrentPublishInSystem = input.readInt32();
                        break;
                    case 72:
                        this.maxConcurrentSubscribeInSystem = input.readInt32();
                        break;
                    case 80:
                        this.maxConcurrentDiscoverySessionsInSystem = input.readInt32();
                        break;
                    case 90:
                        int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 90);
                        NanStatusHistogramBucket[] nanStatusHistogramBucketArr2 = this.histogramPublishStatus;
                        int i2 = nanStatusHistogramBucketArr2 == null ? 0 : nanStatusHistogramBucketArr2.length;
                        NanStatusHistogramBucket[] newArray2 = new NanStatusHistogramBucket[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.histogramPublishStatus, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length - 1) {
                            newArray2[i2] = new NanStatusHistogramBucket();
                            input.readMessage(newArray2[i2]);
                            input.readTag();
                            i2++;
                        }
                        newArray2[i2] = new NanStatusHistogramBucket();
                        input.readMessage(newArray2[i2]);
                        this.histogramPublishStatus = newArray2;
                        break;
                    case 98:
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 98);
                        NanStatusHistogramBucket[] nanStatusHistogramBucketArr3 = this.histogramSubscribeStatus;
                        int i3 = nanStatusHistogramBucketArr3 == null ? 0 : nanStatusHistogramBucketArr3.length;
                        NanStatusHistogramBucket[] newArray3 = new NanStatusHistogramBucket[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.histogramSubscribeStatus, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = new NanStatusHistogramBucket();
                            input.readMessage(newArray3[i3]);
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = new NanStatusHistogramBucket();
                        input.readMessage(newArray3[i3]);
                        this.histogramSubscribeStatus = newArray3;
                        break;
                    case 104:
                        this.numAppsWithDiscoverySessionFailureOutOfResources = input.readInt32();
                        break;
                    case 114:
                        int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 114);
                        NanStatusHistogramBucket[] nanStatusHistogramBucketArr4 = this.histogramRequestNdpStatus;
                        int i4 = nanStatusHistogramBucketArr4 == null ? 0 : nanStatusHistogramBucketArr4.length;
                        NanStatusHistogramBucket[] newArray4 = new NanStatusHistogramBucket[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.histogramRequestNdpStatus, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length - 1) {
                            newArray4[i4] = new NanStatusHistogramBucket();
                            input.readMessage(newArray4[i4]);
                            input.readTag();
                            i4++;
                        }
                        newArray4[i4] = new NanStatusHistogramBucket();
                        input.readMessage(newArray4[i4]);
                        this.histogramRequestNdpStatus = newArray4;
                        break;
                    case 122:
                        int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 122);
                        NanStatusHistogramBucket[] nanStatusHistogramBucketArr5 = this.histogramRequestNdpOobStatus;
                        int i5 = nanStatusHistogramBucketArr5 == null ? 0 : nanStatusHistogramBucketArr5.length;
                        NanStatusHistogramBucket[] newArray5 = new NanStatusHistogramBucket[(i5 + arrayLength5)];
                        if (i5 != 0) {
                            System.arraycopy(this.histogramRequestNdpOobStatus, 0, newArray5, 0, i5);
                        }
                        while (i5 < newArray5.length - 1) {
                            newArray5[i5] = new NanStatusHistogramBucket();
                            input.readMessage(newArray5[i5]);
                            input.readTag();
                            i5++;
                        }
                        newArray5[i5] = new NanStatusHistogramBucket();
                        input.readMessage(newArray5[i5]);
                        this.histogramRequestNdpOobStatus = newArray5;
                        break;
                    case 152:
                        this.maxConcurrentNdiInApp = input.readInt32();
                        break;
                    case 160:
                        this.maxConcurrentNdiInSystem = input.readInt32();
                        break;
                    case 168:
                        this.maxConcurrentNdpInApp = input.readInt32();
                        break;
                    case 176:
                        this.maxConcurrentNdpInSystem = input.readInt32();
                        break;
                    case 184:
                        this.maxConcurrentSecureNdpInApp = input.readInt32();
                        break;
                    case 192:
                        this.maxConcurrentSecureNdpInSystem = input.readInt32();
                        break;
                    case 200:
                        this.maxConcurrentNdpPerNdi = input.readInt32();
                        break;
                    case 210:
                        int arrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(input, 210);
                        HistogramBucket[] histogramBucketArr = this.histogramAwareAvailableDurationMs;
                        int i6 = histogramBucketArr == null ? 0 : histogramBucketArr.length;
                        HistogramBucket[] newArray6 = new HistogramBucket[(i6 + arrayLength6)];
                        if (i6 != 0) {
                            System.arraycopy(this.histogramAwareAvailableDurationMs, 0, newArray6, 0, i6);
                        }
                        while (i6 < newArray6.length - 1) {
                            newArray6[i6] = new HistogramBucket();
                            input.readMessage(newArray6[i6]);
                            input.readTag();
                            i6++;
                        }
                        newArray6[i6] = new HistogramBucket();
                        input.readMessage(newArray6[i6]);
                        this.histogramAwareAvailableDurationMs = newArray6;
                        break;
                    case 218:
                        int arrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(input, 218);
                        HistogramBucket[] histogramBucketArr2 = this.histogramAwareEnabledDurationMs;
                        int i7 = histogramBucketArr2 == null ? 0 : histogramBucketArr2.length;
                        HistogramBucket[] newArray7 = new HistogramBucket[(i7 + arrayLength7)];
                        if (i7 != 0) {
                            System.arraycopy(this.histogramAwareEnabledDurationMs, 0, newArray7, 0, i7);
                        }
                        while (i7 < newArray7.length - 1) {
                            newArray7[i7] = new HistogramBucket();
                            input.readMessage(newArray7[i7]);
                            input.readTag();
                            i7++;
                        }
                        newArray7[i7] = new HistogramBucket();
                        input.readMessage(newArray7[i7]);
                        this.histogramAwareEnabledDurationMs = newArray7;
                        break;
                    case 226:
                        int arrayLength8 = WireFormatNano.getRepeatedFieldArrayLength(input, 226);
                        HistogramBucket[] histogramBucketArr3 = this.histogramAttachDurationMs;
                        int i8 = histogramBucketArr3 == null ? 0 : histogramBucketArr3.length;
                        HistogramBucket[] newArray8 = new HistogramBucket[(i8 + arrayLength8)];
                        if (i8 != 0) {
                            System.arraycopy(this.histogramAttachDurationMs, 0, newArray8, 0, i8);
                        }
                        while (i8 < newArray8.length - 1) {
                            newArray8[i8] = new HistogramBucket();
                            input.readMessage(newArray8[i8]);
                            input.readTag();
                            i8++;
                        }
                        newArray8[i8] = new HistogramBucket();
                        input.readMessage(newArray8[i8]);
                        this.histogramAttachDurationMs = newArray8;
                        break;
                    case 234:
                        int arrayLength9 = WireFormatNano.getRepeatedFieldArrayLength(input, 234);
                        HistogramBucket[] histogramBucketArr4 = this.histogramPublishSessionDurationMs;
                        int i9 = histogramBucketArr4 == null ? 0 : histogramBucketArr4.length;
                        HistogramBucket[] newArray9 = new HistogramBucket[(i9 + arrayLength9)];
                        if (i9 != 0) {
                            System.arraycopy(this.histogramPublishSessionDurationMs, 0, newArray9, 0, i9);
                        }
                        while (i9 < newArray9.length - 1) {
                            newArray9[i9] = new HistogramBucket();
                            input.readMessage(newArray9[i9]);
                            input.readTag();
                            i9++;
                        }
                        newArray9[i9] = new HistogramBucket();
                        input.readMessage(newArray9[i9]);
                        this.histogramPublishSessionDurationMs = newArray9;
                        break;
                    case 242:
                        int arrayLength10 = WireFormatNano.getRepeatedFieldArrayLength(input, 242);
                        HistogramBucket[] histogramBucketArr5 = this.histogramSubscribeSessionDurationMs;
                        int i10 = histogramBucketArr5 == null ? 0 : histogramBucketArr5.length;
                        HistogramBucket[] newArray10 = new HistogramBucket[(i10 + arrayLength10)];
                        if (i10 != 0) {
                            System.arraycopy(this.histogramSubscribeSessionDurationMs, 0, newArray10, 0, i10);
                        }
                        while (i10 < newArray10.length - 1) {
                            newArray10[i10] = new HistogramBucket();
                            input.readMessage(newArray10[i10]);
                            input.readTag();
                            i10++;
                        }
                        newArray10[i10] = new HistogramBucket();
                        input.readMessage(newArray10[i10]);
                        this.histogramSubscribeSessionDurationMs = newArray10;
                        break;
                    case 250:
                        int arrayLength11 = WireFormatNano.getRepeatedFieldArrayLength(input, 250);
                        HistogramBucket[] histogramBucketArr6 = this.histogramNdpSessionDurationMs;
                        int i11 = histogramBucketArr6 == null ? 0 : histogramBucketArr6.length;
                        HistogramBucket[] newArray11 = new HistogramBucket[(i11 + arrayLength11)];
                        if (i11 != 0) {
                            System.arraycopy(this.histogramNdpSessionDurationMs, 0, newArray11, 0, i11);
                        }
                        while (i11 < newArray11.length - 1) {
                            newArray11[i11] = new HistogramBucket();
                            input.readMessage(newArray11[i11]);
                            input.readTag();
                            i11++;
                        }
                        newArray11[i11] = new HistogramBucket();
                        input.readMessage(newArray11[i11]);
                        this.histogramNdpSessionDurationMs = newArray11;
                        break;
                    case 258:
                        int arrayLength12 = WireFormatNano.getRepeatedFieldArrayLength(input, 258);
                        HistogramBucket[] histogramBucketArr7 = this.histogramNdpSessionDataUsageMb;
                        int i12 = histogramBucketArr7 == null ? 0 : histogramBucketArr7.length;
                        HistogramBucket[] newArray12 = new HistogramBucket[(i12 + arrayLength12)];
                        if (i12 != 0) {
                            System.arraycopy(this.histogramNdpSessionDataUsageMb, 0, newArray12, 0, i12);
                        }
                        while (i12 < newArray12.length - 1) {
                            newArray12[i12] = new HistogramBucket();
                            input.readMessage(newArray12[i12]);
                            input.readTag();
                            i12++;
                        }
                        newArray12[i12] = new HistogramBucket();
                        input.readMessage(newArray12[i12]);
                        this.histogramNdpSessionDataUsageMb = newArray12;
                        break;
                    case 266:
                        int arrayLength13 = WireFormatNano.getRepeatedFieldArrayLength(input, 266);
                        HistogramBucket[] histogramBucketArr8 = this.histogramNdpCreationTimeMs;
                        int i13 = histogramBucketArr8 == null ? 0 : histogramBucketArr8.length;
                        HistogramBucket[] newArray13 = new HistogramBucket[(i13 + arrayLength13)];
                        if (i13 != 0) {
                            System.arraycopy(this.histogramNdpCreationTimeMs, 0, newArray13, 0, i13);
                        }
                        while (i13 < newArray13.length - 1) {
                            newArray13[i13] = new HistogramBucket();
                            input.readMessage(newArray13[i13]);
                            input.readTag();
                            i13++;
                        }
                        newArray13[i13] = new HistogramBucket();
                        input.readMessage(newArray13[i13]);
                        this.histogramNdpCreationTimeMs = newArray13;
                        break;
                    case 272:
                        this.ndpCreationTimeMsMin = input.readInt64();
                        break;
                    case 280:
                        this.ndpCreationTimeMsMax = input.readInt64();
                        break;
                    case 288:
                        this.ndpCreationTimeMsSum = input.readInt64();
                        break;
                    case 296:
                        this.ndpCreationTimeMsSumOfSq = input.readInt64();
                        break;
                    case 304:
                        this.ndpCreationTimeMsNumSamples = input.readInt64();
                        break;
                    case 312:
                        this.availableTimeMs = input.readInt64();
                        break;
                    case 320:
                        this.enabledTimeMs = input.readInt64();
                        break;
                    case 328:
                        this.maxConcurrentPublishWithRangingInApp = input.readInt32();
                        break;
                    case 336:
                        this.maxConcurrentSubscribeWithRangingInApp = input.readInt32();
                        break;
                    case 344:
                        this.maxConcurrentPublishWithRangingInSystem = input.readInt32();
                        break;
                    case 352:
                        this.maxConcurrentSubscribeWithRangingInSystem = input.readInt32();
                        break;
                    case 362:
                        int arrayLength14 = WireFormatNano.getRepeatedFieldArrayLength(input, 362);
                        HistogramBucket[] histogramBucketArr9 = this.histogramSubscribeGeofenceMin;
                        int i14 = histogramBucketArr9 == null ? 0 : histogramBucketArr9.length;
                        HistogramBucket[] newArray14 = new HistogramBucket[(i14 + arrayLength14)];
                        if (i14 != 0) {
                            System.arraycopy(this.histogramSubscribeGeofenceMin, 0, newArray14, 0, i14);
                        }
                        while (i14 < newArray14.length - 1) {
                            newArray14[i14] = new HistogramBucket();
                            input.readMessage(newArray14[i14]);
                            input.readTag();
                            i14++;
                        }
                        newArray14[i14] = new HistogramBucket();
                        input.readMessage(newArray14[i14]);
                        this.histogramSubscribeGeofenceMin = newArray14;
                        break;
                    case 370:
                        int arrayLength15 = WireFormatNano.getRepeatedFieldArrayLength(input, 370);
                        HistogramBucket[] histogramBucketArr10 = this.histogramSubscribeGeofenceMax;
                        int i15 = histogramBucketArr10 == null ? 0 : histogramBucketArr10.length;
                        HistogramBucket[] newArray15 = new HistogramBucket[(i15 + arrayLength15)];
                        if (i15 != 0) {
                            System.arraycopy(this.histogramSubscribeGeofenceMax, 0, newArray15, 0, i15);
                        }
                        while (i15 < newArray15.length - 1) {
                            newArray15[i15] = new HistogramBucket();
                            input.readMessage(newArray15[i15]);
                            input.readTag();
                            i15++;
                        }
                        newArray15[i15] = new HistogramBucket();
                        input.readMessage(newArray15[i15]);
                        this.histogramSubscribeGeofenceMax = newArray15;
                        break;
                    case 376:
                        this.numSubscribesWithRanging = input.readInt32();
                        break;
                    case 384:
                        this.numMatchesWithRanging = input.readInt32();
                        break;
                    case 392:
                        this.numMatchesWithoutRangingForRangingEnabledSubscribes = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static WifiAwareLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiAwareLog) MessageNano.mergeFrom(new WifiAwareLog(), data);
        }

        public static WifiAwareLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiAwareLog().mergeFrom(input);
        }
    }

    public static final class NumConnectableNetworksBucket extends MessageNano {
        private static volatile NumConnectableNetworksBucket[] _emptyArray;
        public int count;
        public int numConnectableNetworks;

        public static NumConnectableNetworksBucket[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NumConnectableNetworksBucket[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NumConnectableNetworksBucket() {
            clear();
        }

        public NumConnectableNetworksBucket clear() {
            this.numConnectableNetworks = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numConnectableNetworks;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numConnectableNetworks;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public NumConnectableNetworksBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numConnectableNetworks = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static NumConnectableNetworksBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (NumConnectableNetworksBucket) MessageNano.mergeFrom(new NumConnectableNetworksBucket(), data);
        }

        public static NumConnectableNetworksBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NumConnectableNetworksBucket().mergeFrom(input);
        }
    }

    public static final class PnoScanMetrics extends MessageNano {
        private static volatile PnoScanMetrics[] _emptyArray;
        public int numPnoFoundNetworkEvents;
        public int numPnoScanAttempts;
        public int numPnoScanFailed;
        public int numPnoScanFailedOverOffload;
        public int numPnoScanStartedOverOffload;

        public static PnoScanMetrics[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PnoScanMetrics[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PnoScanMetrics() {
            clear();
        }

        public PnoScanMetrics clear() {
            this.numPnoScanAttempts = 0;
            this.numPnoScanFailed = 0;
            this.numPnoScanStartedOverOffload = 0;
            this.numPnoScanFailedOverOffload = 0;
            this.numPnoFoundNetworkEvents = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numPnoScanAttempts;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numPnoScanFailed;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.numPnoScanStartedOverOffload;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numPnoScanFailedOverOffload;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.numPnoFoundNetworkEvents;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numPnoScanAttempts;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numPnoScanFailed;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.numPnoScanStartedOverOffload;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numPnoScanFailedOverOffload;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.numPnoFoundNetworkEvents;
            if (i5 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public PnoScanMetrics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numPnoScanAttempts = input.readInt32();
                } else if (tag == 16) {
                    this.numPnoScanFailed = input.readInt32();
                } else if (tag == 24) {
                    this.numPnoScanStartedOverOffload = input.readInt32();
                } else if (tag == 32) {
                    this.numPnoScanFailedOverOffload = input.readInt32();
                } else if (tag == 40) {
                    this.numPnoFoundNetworkEvents = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static PnoScanMetrics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (PnoScanMetrics) MessageNano.mergeFrom(new PnoScanMetrics(), data);
        }

        public static PnoScanMetrics parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new PnoScanMetrics().mergeFrom(input);
        }
    }

    public static final class ConnectToNetworkNotificationAndActionCount extends MessageNano {
        public static final int ACTION_CONNECT_TO_NETWORK = 2;
        public static final int ACTION_PICK_WIFI_NETWORK = 3;
        public static final int ACTION_PICK_WIFI_NETWORK_AFTER_CONNECT_FAILURE = 4;
        public static final int ACTION_UNKNOWN = 0;
        public static final int ACTION_USER_DISMISSED_NOTIFICATION = 1;
        public static final int NOTIFICATION_CONNECTED_TO_NETWORK = 3;
        public static final int NOTIFICATION_CONNECTING_TO_NETWORK = 2;
        public static final int NOTIFICATION_FAILED_TO_CONNECT = 4;
        public static final int NOTIFICATION_RECOMMEND_NETWORK = 1;
        public static final int NOTIFICATION_UNKNOWN = 0;
        public static final int RECOMMENDER_OPEN = 1;
        public static final int RECOMMENDER_UNKNOWN = 0;
        private static volatile ConnectToNetworkNotificationAndActionCount[] _emptyArray;
        public int action;
        public int count;
        public int notification;
        public int recommender;

        public static ConnectToNetworkNotificationAndActionCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ConnectToNetworkNotificationAndActionCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ConnectToNetworkNotificationAndActionCount() {
            clear();
        }

        public ConnectToNetworkNotificationAndActionCount clear() {
            this.notification = 0;
            this.action = 0;
            this.recommender = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.notification;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.action;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.recommender;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.count;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.notification;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.action;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.recommender;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.count;
            if (i4 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public ConnectToNetworkNotificationAndActionCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4) {
                        this.notification = value;
                    }
                } else if (tag == 16) {
                    int value2 = input.readInt32();
                    if (value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3 || value2 == 4) {
                        this.action = value2;
                    }
                } else if (tag == 24) {
                    int value3 = input.readInt32();
                    if (value3 == 0 || value3 == 1) {
                        this.recommender = value3;
                    }
                } else if (tag == 32) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ConnectToNetworkNotificationAndActionCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ConnectToNetworkNotificationAndActionCount) MessageNano.mergeFrom(new ConnectToNetworkNotificationAndActionCount(), data);
        }

        public static ConnectToNetworkNotificationAndActionCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ConnectToNetworkNotificationAndActionCount().mergeFrom(input);
        }
    }

    public static final class SoftApConnectedClientsEvent extends MessageNano {
        public static final int BANDWIDTH_160 = 6;
        public static final int BANDWIDTH_20 = 2;
        public static final int BANDWIDTH_20_NOHT = 1;
        public static final int BANDWIDTH_40 = 3;
        public static final int BANDWIDTH_80 = 4;
        public static final int BANDWIDTH_80P80 = 5;
        public static final int BANDWIDTH_INVALID = 0;
        public static final int NUM_CLIENTS_CHANGED = 2;
        public static final int SOFT_AP_DOWN = 1;
        public static final int SOFT_AP_UP = 0;
        private static volatile SoftApConnectedClientsEvent[] _emptyArray;
        public int channelBandwidth;
        public int channelFrequency;
        public int eventType;
        public int numConnectedClients;
        public long timeStampMillis;

        public static SoftApConnectedClientsEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SoftApConnectedClientsEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SoftApConnectedClientsEvent() {
            clear();
        }

        public SoftApConnectedClientsEvent clear() {
            this.eventType = 0;
            this.timeStampMillis = 0;
            this.numConnectedClients = 0;
            this.channelFrequency = 0;
            this.channelBandwidth = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.eventType;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            long j = this.timeStampMillis;
            if (j != 0) {
                output.writeInt64(2, j);
            }
            int i2 = this.numConnectedClients;
            if (i2 != 0) {
                output.writeInt32(3, i2);
            }
            int i3 = this.channelFrequency;
            if (i3 != 0) {
                output.writeInt32(4, i3);
            }
            int i4 = this.channelBandwidth;
            if (i4 != 0) {
                output.writeInt32(5, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.eventType;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            long j = this.timeStampMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, j);
            }
            int i2 = this.numConnectedClients;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            int i3 = this.channelFrequency;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i3);
            }
            int i4 = this.channelBandwidth;
            if (i4 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(5, i4);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public SoftApConnectedClientsEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2) {
                        this.eventType = value;
                    }
                } else if (tag == 16) {
                    this.timeStampMillis = input.readInt64();
                } else if (tag == 24) {
                    this.numConnectedClients = input.readInt32();
                } else if (tag == 32) {
                    this.channelFrequency = input.readInt32();
                } else if (tag == 40) {
                    int value2 = input.readInt32();
                    switch (value2) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            this.channelBandwidth = value2;
                            continue;
                    }
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static SoftApConnectedClientsEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (SoftApConnectedClientsEvent) MessageNano.mergeFrom(new SoftApConnectedClientsEvent(), data);
        }

        public static SoftApConnectedClientsEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new SoftApConnectedClientsEvent().mergeFrom(input);
        }
    }

    public static final class WpsMetrics extends MessageNano {
        private static volatile WpsMetrics[] _emptyArray;
        public int numWpsAttempts;
        public int numWpsCancellation;
        public int numWpsOtherConnectionFailure;
        public int numWpsOverlapFailure;
        public int numWpsStartFailure;
        public int numWpsSuccess;
        public int numWpsSupplicantFailure;
        public int numWpsTimeoutFailure;

        public static WpsMetrics[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WpsMetrics[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WpsMetrics() {
            clear();
        }

        public WpsMetrics clear() {
            this.numWpsAttempts = 0;
            this.numWpsSuccess = 0;
            this.numWpsStartFailure = 0;
            this.numWpsOverlapFailure = 0;
            this.numWpsTimeoutFailure = 0;
            this.numWpsOtherConnectionFailure = 0;
            this.numWpsSupplicantFailure = 0;
            this.numWpsCancellation = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numWpsAttempts;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numWpsSuccess;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.numWpsStartFailure;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numWpsOverlapFailure;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.numWpsTimeoutFailure;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            int i6 = this.numWpsOtherConnectionFailure;
            if (i6 != 0) {
                output.writeInt32(6, i6);
            }
            int i7 = this.numWpsSupplicantFailure;
            if (i7 != 0) {
                output.writeInt32(7, i7);
            }
            int i8 = this.numWpsCancellation;
            if (i8 != 0) {
                output.writeInt32(8, i8);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numWpsAttempts;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numWpsSuccess;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.numWpsStartFailure;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numWpsOverlapFailure;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.numWpsTimeoutFailure;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            int i6 = this.numWpsOtherConnectionFailure;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i6);
            }
            int i7 = this.numWpsSupplicantFailure;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i7);
            }
            int i8 = this.numWpsCancellation;
            if (i8 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(8, i8);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WpsMetrics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numWpsAttempts = input.readInt32();
                } else if (tag == 16) {
                    this.numWpsSuccess = input.readInt32();
                } else if (tag == 24) {
                    this.numWpsStartFailure = input.readInt32();
                } else if (tag == 32) {
                    this.numWpsOverlapFailure = input.readInt32();
                } else if (tag == 40) {
                    this.numWpsTimeoutFailure = input.readInt32();
                } else if (tag == 48) {
                    this.numWpsOtherConnectionFailure = input.readInt32();
                } else if (tag == 56) {
                    this.numWpsSupplicantFailure = input.readInt32();
                } else if (tag == 64) {
                    this.numWpsCancellation = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WpsMetrics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WpsMetrics) MessageNano.mergeFrom(new WpsMetrics(), data);
        }

        public static WpsMetrics parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WpsMetrics().mergeFrom(input);
        }
    }

    public static final class WifiPowerStats extends MessageNano {
        private static volatile WifiPowerStats[] _emptyArray;
        public double energyConsumedMah;
        public long idleTimeMs;
        public long loggingDurationMs;
        public double monitoredRailEnergyConsumedMah;
        public long numBytesRx;
        public long numBytesTx;
        public long numPacketsRx;
        public long numPacketsTx;
        public long rxTimeMs;
        public long scanTimeMs;
        public long sleepTimeMs;
        public long txTimeMs;
        public long wifiKernelActiveTimeMs;

        public static WifiPowerStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiPowerStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiPowerStats() {
            clear();
        }

        public WifiPowerStats clear() {
            this.loggingDurationMs = 0;
            this.energyConsumedMah = 0.0d;
            this.idleTimeMs = 0;
            this.rxTimeMs = 0;
            this.txTimeMs = 0;
            this.wifiKernelActiveTimeMs = 0;
            this.numPacketsTx = 0;
            this.numBytesTx = 0;
            this.numPacketsRx = 0;
            this.numBytesRx = 0;
            this.sleepTimeMs = 0;
            this.scanTimeMs = 0;
            this.monitoredRailEnergyConsumedMah = 0.0d;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.loggingDurationMs;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            if (Double.doubleToLongBits(this.energyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(2, this.energyConsumedMah);
            }
            long j2 = this.idleTimeMs;
            if (j2 != 0) {
                output.writeInt64(3, j2);
            }
            long j3 = this.rxTimeMs;
            if (j3 != 0) {
                output.writeInt64(4, j3);
            }
            long j4 = this.txTimeMs;
            if (j4 != 0) {
                output.writeInt64(5, j4);
            }
            long j5 = this.wifiKernelActiveTimeMs;
            if (j5 != 0) {
                output.writeInt64(6, j5);
            }
            long j6 = this.numPacketsTx;
            if (j6 != 0) {
                output.writeInt64(7, j6);
            }
            long j7 = this.numBytesTx;
            if (j7 != 0) {
                output.writeInt64(8, j7);
            }
            long j8 = this.numPacketsRx;
            if (j8 != 0) {
                output.writeInt64(9, j8);
            }
            long j9 = this.numBytesRx;
            if (j9 != 0) {
                output.writeInt64(10, j9);
            }
            long j10 = this.sleepTimeMs;
            if (j10 != 0) {
                output.writeInt64(11, j10);
            }
            long j11 = this.scanTimeMs;
            if (j11 != 0) {
                output.writeInt64(12, j11);
            }
            if (Double.doubleToLongBits(this.monitoredRailEnergyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(13, this.monitoredRailEnergyConsumedMah);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.loggingDurationMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            if (Double.doubleToLongBits(this.energyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(2, this.energyConsumedMah);
            }
            long j2 = this.idleTimeMs;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, j2);
            }
            long j3 = this.rxTimeMs;
            if (j3 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, j3);
            }
            long j4 = this.txTimeMs;
            if (j4 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, j4);
            }
            long j5 = this.wifiKernelActiveTimeMs;
            if (j5 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, j5);
            }
            long j6 = this.numPacketsTx;
            if (j6 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, j6);
            }
            long j7 = this.numBytesTx;
            if (j7 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, j7);
            }
            long j8 = this.numPacketsRx;
            if (j8 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(9, j8);
            }
            long j9 = this.numBytesRx;
            if (j9 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(10, j9);
            }
            long j10 = this.sleepTimeMs;
            if (j10 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(11, j10);
            }
            long j11 = this.scanTimeMs;
            if (j11 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(12, j11);
            }
            if (Double.doubleToLongBits(this.monitoredRailEnergyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                return size + CodedOutputByteBufferNano.computeDoubleSize(13, this.monitoredRailEnergyConsumedMah);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiPowerStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.loggingDurationMs = input.readInt64();
                        break;
                    case 17:
                        this.energyConsumedMah = input.readDouble();
                        break;
                    case 24:
                        this.idleTimeMs = input.readInt64();
                        break;
                    case 32:
                        this.rxTimeMs = input.readInt64();
                        break;
                    case 40:
                        this.txTimeMs = input.readInt64();
                        break;
                    case 48:
                        this.wifiKernelActiveTimeMs = input.readInt64();
                        break;
                    case 56:
                        this.numPacketsTx = input.readInt64();
                        break;
                    case 64:
                        this.numBytesTx = input.readInt64();
                        break;
                    case 72:
                        this.numPacketsRx = input.readInt64();
                        break;
                    case 80:
                        this.numBytesRx = input.readInt64();
                        break;
                    case 88:
                        this.sleepTimeMs = input.readInt64();
                        break;
                    case 96:
                        this.scanTimeMs = input.readInt64();
                        break;
                    case 105:
                        this.monitoredRailEnergyConsumedMah = input.readDouble();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static WifiPowerStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiPowerStats) MessageNano.mergeFrom(new WifiPowerStats(), data);
        }

        public static WifiPowerStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiPowerStats().mergeFrom(input);
        }
    }

    public static final class WifiWakeStats extends MessageNano {
        private static volatile WifiWakeStats[] _emptyArray;
        public int numIgnoredStarts;
        public int numSessions;
        public int numWakeups;
        public Session[] sessions;

        public static final class Session extends MessageNano {
            private static volatile Session[] _emptyArray;
            public Event initializeEvent;
            public int lockedNetworksAtInitialize;
            public int lockedNetworksAtStart;
            public Event resetEvent;
            public long startTimeMillis;
            public Event unlockEvent;
            public Event wakeupEvent;

            public static final class Event extends MessageNano {
                private static volatile Event[] _emptyArray;
                public int elapsedScans;
                public long elapsedTimeMillis;

                public static Event[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new Event[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public Event() {
                    clear();
                }

                public Event clear() {
                    this.elapsedTimeMillis = 0;
                    this.elapsedScans = 0;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.framework.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    long j = this.elapsedTimeMillis;
                    if (j != 0) {
                        output.writeInt64(1, j);
                    }
                    int i = this.elapsedScans;
                    if (i != 0) {
                        output.writeInt32(2, i);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.framework.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    long j = this.elapsedTimeMillis;
                    if (j != 0) {
                        size += CodedOutputByteBufferNano.computeInt64Size(1, j);
                    }
                    int i = this.elapsedScans;
                    if (i != 0) {
                        return size + CodedOutputByteBufferNano.computeInt32Size(2, i);
                    }
                    return size;
                }

                @Override // com.android.framework.protobuf.nano.MessageNano
                public Event mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 8) {
                            this.elapsedTimeMillis = input.readInt64();
                        } else if (tag == 16) {
                            this.elapsedScans = input.readInt32();
                        } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }

                public static Event parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (Event) MessageNano.mergeFrom(new Event(), data);
                }

                public static Event parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new Event().mergeFrom(input);
                }
            }

            public static Session[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Session[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Session() {
                clear();
            }

            public Session clear() {
                this.startTimeMillis = 0;
                this.lockedNetworksAtStart = 0;
                this.lockedNetworksAtInitialize = 0;
                this.initializeEvent = null;
                this.unlockEvent = null;
                this.wakeupEvent = null;
                this.resetEvent = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                long j = this.startTimeMillis;
                if (j != 0) {
                    output.writeInt64(1, j);
                }
                int i = this.lockedNetworksAtStart;
                if (i != 0) {
                    output.writeInt32(2, i);
                }
                Event event = this.unlockEvent;
                if (event != null) {
                    output.writeMessage(3, event);
                }
                Event event2 = this.wakeupEvent;
                if (event2 != null) {
                    output.writeMessage(4, event2);
                }
                Event event3 = this.resetEvent;
                if (event3 != null) {
                    output.writeMessage(5, event3);
                }
                int i2 = this.lockedNetworksAtInitialize;
                if (i2 != 0) {
                    output.writeInt32(6, i2);
                }
                Event event4 = this.initializeEvent;
                if (event4 != null) {
                    output.writeMessage(7, event4);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                long j = this.startTimeMillis;
                if (j != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(1, j);
                }
                int i = this.lockedNetworksAtStart;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i);
                }
                Event event = this.unlockEvent;
                if (event != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, event);
                }
                Event event2 = this.wakeupEvent;
                if (event2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, event2);
                }
                Event event3 = this.resetEvent;
                if (event3 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, event3);
                }
                int i2 = this.lockedNetworksAtInitialize;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(6, i2);
                }
                Event event4 = this.initializeEvent;
                if (event4 != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(7, event4);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public Session mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.startTimeMillis = input.readInt64();
                    } else if (tag == 16) {
                        this.lockedNetworksAtStart = input.readInt32();
                    } else if (tag == 26) {
                        if (this.unlockEvent == null) {
                            this.unlockEvent = new Event();
                        }
                        input.readMessage(this.unlockEvent);
                    } else if (tag == 34) {
                        if (this.wakeupEvent == null) {
                            this.wakeupEvent = new Event();
                        }
                        input.readMessage(this.wakeupEvent);
                    } else if (tag == 42) {
                        if (this.resetEvent == null) {
                            this.resetEvent = new Event();
                        }
                        input.readMessage(this.resetEvent);
                    } else if (tag == 48) {
                        this.lockedNetworksAtInitialize = input.readInt32();
                    } else if (tag == 58) {
                        if (this.initializeEvent == null) {
                            this.initializeEvent = new Event();
                        }
                        input.readMessage(this.initializeEvent);
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static Session parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (Session) MessageNano.mergeFrom(new Session(), data);
            }

            public static Session parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new Session().mergeFrom(input);
            }
        }

        public static WifiWakeStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiWakeStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiWakeStats() {
            clear();
        }

        public WifiWakeStats clear() {
            this.numSessions = 0;
            this.sessions = Session.emptyArray();
            this.numIgnoredStarts = 0;
            this.numWakeups = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numSessions;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            Session[] sessionArr = this.sessions;
            if (sessionArr != null && sessionArr.length > 0) {
                int i2 = 0;
                while (true) {
                    Session[] sessionArr2 = this.sessions;
                    if (i2 >= sessionArr2.length) {
                        break;
                    }
                    Session element = sessionArr2[i2];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i2++;
                }
            }
            int i3 = this.numIgnoredStarts;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numWakeups;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numSessions;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            Session[] sessionArr = this.sessions;
            if (sessionArr != null && sessionArr.length > 0) {
                int i2 = 0;
                while (true) {
                    Session[] sessionArr2 = this.sessions;
                    if (i2 >= sessionArr2.length) {
                        break;
                    }
                    Session element = sessionArr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i2++;
                }
            }
            int i3 = this.numIgnoredStarts;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numWakeups;
            if (i4 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiWakeStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numSessions = input.readInt32();
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    Session[] sessionArr = this.sessions;
                    int i = sessionArr == null ? 0 : sessionArr.length;
                    Session[] newArray = new Session[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.sessions, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new Session();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new Session();
                    input.readMessage(newArray[i]);
                    this.sessions = newArray;
                } else if (tag == 24) {
                    this.numIgnoredStarts = input.readInt32();
                } else if (tag == 32) {
                    this.numWakeups = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiWakeStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiWakeStats) MessageNano.mergeFrom(new WifiWakeStats(), data);
        }

        public static WifiWakeStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiWakeStats().mergeFrom(input);
        }
    }

    public static final class WifiRttLog extends MessageNano {
        public static final int ABORTED = 9;
        public static final int FAILURE = 2;
        public static final int FAIL_AP_ON_DIFF_CHANNEL = 7;
        public static final int FAIL_BUSY_TRY_LATER = 13;
        public static final int FAIL_FTM_PARAM_OVERRIDE = 16;
        public static final int FAIL_INVALID_TS = 10;
        public static final int FAIL_NOT_SCHEDULED_YET = 5;
        public static final int FAIL_NO_CAPABILITY = 8;
        public static final int FAIL_NO_RSP = 3;
        public static final int FAIL_PROTOCOL = 11;
        public static final int FAIL_REJECTED = 4;
        public static final int FAIL_SCHEDULE = 12;
        public static final int FAIL_TM_TIMEOUT = 6;
        public static final int INVALID_REQ = 14;
        public static final int MISSING_RESULT = 17;
        public static final int NO_WIFI = 15;
        public static final int OVERALL_AWARE_TRANSLATION_FAILURE = 7;
        public static final int OVERALL_FAIL = 2;
        public static final int OVERALL_HAL_FAILURE = 6;
        public static final int OVERALL_LOCATION_PERMISSION_MISSING = 8;
        public static final int OVERALL_RTT_NOT_AVAILABLE = 3;
        public static final int OVERALL_SUCCESS = 1;
        public static final int OVERALL_THROTTLE = 5;
        public static final int OVERALL_TIMEOUT = 4;
        public static final int OVERALL_UNKNOWN = 0;
        public static final int SUCCESS = 1;
        public static final int UNKNOWN = 0;
        private static volatile WifiRttLog[] _emptyArray;
        public RttOverallStatusHistogramBucket[] histogramOverallStatus;
        public int numRequests;
        public RttToPeerLog rttToAp;
        public RttToPeerLog rttToAware;

        public static final class RttToPeerLog extends MessageNano {
            private static volatile RttToPeerLog[] _emptyArray;
            public HistogramBucket[] histogramDistance;
            public RttIndividualStatusHistogramBucket[] histogramIndividualStatus;
            public HistogramBucket[] histogramNumPeersPerRequest;
            public HistogramBucket[] histogramNumRequestsPerApp;
            public HistogramBucket[] histogramRequestIntervalMs;
            public int numApps;
            public int numIndividualRequests;
            public int numRequests;

            public static RttToPeerLog[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new RttToPeerLog[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public RttToPeerLog() {
                clear();
            }

            public RttToPeerLog clear() {
                this.numRequests = 0;
                this.numIndividualRequests = 0;
                this.numApps = 0;
                this.histogramNumRequestsPerApp = HistogramBucket.emptyArray();
                this.histogramNumPeersPerRequest = HistogramBucket.emptyArray();
                this.histogramIndividualStatus = RttIndividualStatusHistogramBucket.emptyArray();
                this.histogramDistance = HistogramBucket.emptyArray();
                this.histogramRequestIntervalMs = HistogramBucket.emptyArray();
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.numRequests;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.numIndividualRequests;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                int i3 = this.numApps;
                if (i3 != 0) {
                    output.writeInt32(3, i3);
                }
                HistogramBucket[] histogramBucketArr = this.histogramNumRequestsPerApp;
                if (histogramBucketArr != null && histogramBucketArr.length > 0) {
                    int i4 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr2 = this.histogramNumRequestsPerApp;
                        if (i4 >= histogramBucketArr2.length) {
                            break;
                        }
                        HistogramBucket element = histogramBucketArr2[i4];
                        if (element != null) {
                            output.writeMessage(4, element);
                        }
                        i4++;
                    }
                }
                HistogramBucket[] histogramBucketArr3 = this.histogramNumPeersPerRequest;
                if (histogramBucketArr3 != null && histogramBucketArr3.length > 0) {
                    int i5 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr4 = this.histogramNumPeersPerRequest;
                        if (i5 >= histogramBucketArr4.length) {
                            break;
                        }
                        HistogramBucket element2 = histogramBucketArr4[i5];
                        if (element2 != null) {
                            output.writeMessage(5, element2);
                        }
                        i5++;
                    }
                }
                RttIndividualStatusHistogramBucket[] rttIndividualStatusHistogramBucketArr = this.histogramIndividualStatus;
                if (rttIndividualStatusHistogramBucketArr != null && rttIndividualStatusHistogramBucketArr.length > 0) {
                    int i6 = 0;
                    while (true) {
                        RttIndividualStatusHistogramBucket[] rttIndividualStatusHistogramBucketArr2 = this.histogramIndividualStatus;
                        if (i6 >= rttIndividualStatusHistogramBucketArr2.length) {
                            break;
                        }
                        RttIndividualStatusHistogramBucket element3 = rttIndividualStatusHistogramBucketArr2[i6];
                        if (element3 != null) {
                            output.writeMessage(6, element3);
                        }
                        i6++;
                    }
                }
                HistogramBucket[] histogramBucketArr5 = this.histogramDistance;
                if (histogramBucketArr5 != null && histogramBucketArr5.length > 0) {
                    int i7 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr6 = this.histogramDistance;
                        if (i7 >= histogramBucketArr6.length) {
                            break;
                        }
                        HistogramBucket element4 = histogramBucketArr6[i7];
                        if (element4 != null) {
                            output.writeMessage(7, element4);
                        }
                        i7++;
                    }
                }
                HistogramBucket[] histogramBucketArr7 = this.histogramRequestIntervalMs;
                if (histogramBucketArr7 != null && histogramBucketArr7.length > 0) {
                    int i8 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr8 = this.histogramRequestIntervalMs;
                        if (i8 >= histogramBucketArr8.length) {
                            break;
                        }
                        HistogramBucket element5 = histogramBucketArr8[i8];
                        if (element5 != null) {
                            output.writeMessage(8, element5);
                        }
                        i8++;
                    }
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.numRequests;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.numIndividualRequests;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                int i3 = this.numApps;
                if (i3 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
                }
                HistogramBucket[] histogramBucketArr = this.histogramNumRequestsPerApp;
                if (histogramBucketArr != null && histogramBucketArr.length > 0) {
                    int i4 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr2 = this.histogramNumRequestsPerApp;
                        if (i4 >= histogramBucketArr2.length) {
                            break;
                        }
                        HistogramBucket element = histogramBucketArr2[i4];
                        if (element != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(4, element);
                        }
                        i4++;
                    }
                }
                HistogramBucket[] histogramBucketArr3 = this.histogramNumPeersPerRequest;
                if (histogramBucketArr3 != null && histogramBucketArr3.length > 0) {
                    int i5 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr4 = this.histogramNumPeersPerRequest;
                        if (i5 >= histogramBucketArr4.length) {
                            break;
                        }
                        HistogramBucket element2 = histogramBucketArr4[i5];
                        if (element2 != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(5, element2);
                        }
                        i5++;
                    }
                }
                RttIndividualStatusHistogramBucket[] rttIndividualStatusHistogramBucketArr = this.histogramIndividualStatus;
                if (rttIndividualStatusHistogramBucketArr != null && rttIndividualStatusHistogramBucketArr.length > 0) {
                    int i6 = 0;
                    while (true) {
                        RttIndividualStatusHistogramBucket[] rttIndividualStatusHistogramBucketArr2 = this.histogramIndividualStatus;
                        if (i6 >= rttIndividualStatusHistogramBucketArr2.length) {
                            break;
                        }
                        RttIndividualStatusHistogramBucket element3 = rttIndividualStatusHistogramBucketArr2[i6];
                        if (element3 != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(6, element3);
                        }
                        i6++;
                    }
                }
                HistogramBucket[] histogramBucketArr5 = this.histogramDistance;
                if (histogramBucketArr5 != null && histogramBucketArr5.length > 0) {
                    int i7 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr6 = this.histogramDistance;
                        if (i7 >= histogramBucketArr6.length) {
                            break;
                        }
                        HistogramBucket element4 = histogramBucketArr6[i7];
                        if (element4 != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(7, element4);
                        }
                        i7++;
                    }
                }
                HistogramBucket[] histogramBucketArr7 = this.histogramRequestIntervalMs;
                if (histogramBucketArr7 != null && histogramBucketArr7.length > 0) {
                    int i8 = 0;
                    while (true) {
                        HistogramBucket[] histogramBucketArr8 = this.histogramRequestIntervalMs;
                        if (i8 >= histogramBucketArr8.length) {
                            break;
                        }
                        HistogramBucket element5 = histogramBucketArr8[i8];
                        if (element5 != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(8, element5);
                        }
                        i8++;
                    }
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public RttToPeerLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.numRequests = input.readInt32();
                    } else if (tag == 16) {
                        this.numIndividualRequests = input.readInt32();
                    } else if (tag == 24) {
                        this.numApps = input.readInt32();
                    } else if (tag == 34) {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        HistogramBucket[] histogramBucketArr = this.histogramNumRequestsPerApp;
                        int i = histogramBucketArr == null ? 0 : histogramBucketArr.length;
                        HistogramBucket[] newArray = new HistogramBucket[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.histogramNumRequestsPerApp, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new HistogramBucket();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new HistogramBucket();
                        input.readMessage(newArray[i]);
                        this.histogramNumRequestsPerApp = newArray;
                    } else if (tag == 42) {
                        int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        HistogramBucket[] histogramBucketArr2 = this.histogramNumPeersPerRequest;
                        int i2 = histogramBucketArr2 == null ? 0 : histogramBucketArr2.length;
                        HistogramBucket[] newArray2 = new HistogramBucket[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.histogramNumPeersPerRequest, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length - 1) {
                            newArray2[i2] = new HistogramBucket();
                            input.readMessage(newArray2[i2]);
                            input.readTag();
                            i2++;
                        }
                        newArray2[i2] = new HistogramBucket();
                        input.readMessage(newArray2[i2]);
                        this.histogramNumPeersPerRequest = newArray2;
                    } else if (tag == 50) {
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                        RttIndividualStatusHistogramBucket[] rttIndividualStatusHistogramBucketArr = this.histogramIndividualStatus;
                        int i3 = rttIndividualStatusHistogramBucketArr == null ? 0 : rttIndividualStatusHistogramBucketArr.length;
                        RttIndividualStatusHistogramBucket[] newArray3 = new RttIndividualStatusHistogramBucket[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.histogramIndividualStatus, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = new RttIndividualStatusHistogramBucket();
                            input.readMessage(newArray3[i3]);
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = new RttIndividualStatusHistogramBucket();
                        input.readMessage(newArray3[i3]);
                        this.histogramIndividualStatus = newArray3;
                    } else if (tag == 58) {
                        int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                        HistogramBucket[] histogramBucketArr3 = this.histogramDistance;
                        int i4 = histogramBucketArr3 == null ? 0 : histogramBucketArr3.length;
                        HistogramBucket[] newArray4 = new HistogramBucket[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.histogramDistance, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length - 1) {
                            newArray4[i4] = new HistogramBucket();
                            input.readMessage(newArray4[i4]);
                            input.readTag();
                            i4++;
                        }
                        newArray4[i4] = new HistogramBucket();
                        input.readMessage(newArray4[i4]);
                        this.histogramDistance = newArray4;
                    } else if (tag == 66) {
                        int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                        HistogramBucket[] histogramBucketArr4 = this.histogramRequestIntervalMs;
                        int i5 = histogramBucketArr4 == null ? 0 : histogramBucketArr4.length;
                        HistogramBucket[] newArray5 = new HistogramBucket[(i5 + arrayLength5)];
                        if (i5 != 0) {
                            System.arraycopy(this.histogramRequestIntervalMs, 0, newArray5, 0, i5);
                        }
                        while (i5 < newArray5.length - 1) {
                            newArray5[i5] = new HistogramBucket();
                            input.readMessage(newArray5[i5]);
                            input.readTag();
                            i5++;
                        }
                        newArray5[i5] = new HistogramBucket();
                        input.readMessage(newArray5[i5]);
                        this.histogramRequestIntervalMs = newArray5;
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static RttToPeerLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (RttToPeerLog) MessageNano.mergeFrom(new RttToPeerLog(), data);
            }

            public static RttToPeerLog parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new RttToPeerLog().mergeFrom(input);
            }
        }

        public static final class HistogramBucket extends MessageNano {
            private static volatile HistogramBucket[] _emptyArray;
            public int count;
            public long end;
            public long start;

            public static HistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new HistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public HistogramBucket() {
                clear();
            }

            public HistogramBucket clear() {
                this.start = 0;
                this.end = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                long j = this.start;
                if (j != 0) {
                    output.writeInt64(1, j);
                }
                long j2 = this.end;
                if (j2 != 0) {
                    output.writeInt64(2, j2);
                }
                int i = this.count;
                if (i != 0) {
                    output.writeInt32(3, i);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                long j = this.start;
                if (j != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(1, j);
                }
                long j2 = this.end;
                if (j2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(2, j2);
                }
                int i = this.count;
                if (i != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(3, i);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public HistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.start = input.readInt64();
                    } else if (tag == 16) {
                        this.end = input.readInt64();
                    } else if (tag == 24) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static HistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (HistogramBucket) MessageNano.mergeFrom(new HistogramBucket(), data);
            }

            public static HistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new HistogramBucket().mergeFrom(input);
            }
        }

        public static final class RttOverallStatusHistogramBucket extends MessageNano {
            private static volatile RttOverallStatusHistogramBucket[] _emptyArray;
            public int count;
            public int statusType;

            public static RttOverallStatusHistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new RttOverallStatusHistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public RttOverallStatusHistogramBucket() {
                clear();
            }

            public RttOverallStatusHistogramBucket clear() {
                this.statusType = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.statusType;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.statusType;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public RttOverallStatusHistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                                this.statusType = value;
                                continue;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static RttOverallStatusHistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (RttOverallStatusHistogramBucket) MessageNano.mergeFrom(new RttOverallStatusHistogramBucket(), data);
            }

            public static RttOverallStatusHistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new RttOverallStatusHistogramBucket().mergeFrom(input);
            }
        }

        public static final class RttIndividualStatusHistogramBucket extends MessageNano {
            private static volatile RttIndividualStatusHistogramBucket[] _emptyArray;
            public int count;
            public int statusType;

            public static RttIndividualStatusHistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new RttIndividualStatusHistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public RttIndividualStatusHistogramBucket() {
                clear();
            }

            public RttIndividualStatusHistogramBucket clear() {
                this.statusType = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.statusType;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.statusType;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public RttIndividualStatusHistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                                this.statusType = value;
                                continue;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static RttIndividualStatusHistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (RttIndividualStatusHistogramBucket) MessageNano.mergeFrom(new RttIndividualStatusHistogramBucket(), data);
            }

            public static RttIndividualStatusHistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new RttIndividualStatusHistogramBucket().mergeFrom(input);
            }
        }

        public static WifiRttLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiRttLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiRttLog() {
            clear();
        }

        public WifiRttLog clear() {
            this.numRequests = 0;
            this.histogramOverallStatus = RttOverallStatusHistogramBucket.emptyArray();
            this.rttToAp = null;
            this.rttToAware = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numRequests;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            RttOverallStatusHistogramBucket[] rttOverallStatusHistogramBucketArr = this.histogramOverallStatus;
            if (rttOverallStatusHistogramBucketArr != null && rttOverallStatusHistogramBucketArr.length > 0) {
                int i2 = 0;
                while (true) {
                    RttOverallStatusHistogramBucket[] rttOverallStatusHistogramBucketArr2 = this.histogramOverallStatus;
                    if (i2 >= rttOverallStatusHistogramBucketArr2.length) {
                        break;
                    }
                    RttOverallStatusHistogramBucket element = rttOverallStatusHistogramBucketArr2[i2];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i2++;
                }
            }
            RttToPeerLog rttToPeerLog = this.rttToAp;
            if (rttToPeerLog != null) {
                output.writeMessage(3, rttToPeerLog);
            }
            RttToPeerLog rttToPeerLog2 = this.rttToAware;
            if (rttToPeerLog2 != null) {
                output.writeMessage(4, rttToPeerLog2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numRequests;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            RttOverallStatusHistogramBucket[] rttOverallStatusHistogramBucketArr = this.histogramOverallStatus;
            if (rttOverallStatusHistogramBucketArr != null && rttOverallStatusHistogramBucketArr.length > 0) {
                int i2 = 0;
                while (true) {
                    RttOverallStatusHistogramBucket[] rttOverallStatusHistogramBucketArr2 = this.histogramOverallStatus;
                    if (i2 >= rttOverallStatusHistogramBucketArr2.length) {
                        break;
                    }
                    RttOverallStatusHistogramBucket element = rttOverallStatusHistogramBucketArr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i2++;
                }
            }
            RttToPeerLog rttToPeerLog = this.rttToAp;
            if (rttToPeerLog != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, rttToPeerLog);
            }
            RttToPeerLog rttToPeerLog2 = this.rttToAware;
            if (rttToPeerLog2 != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(4, rttToPeerLog2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiRttLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numRequests = input.readInt32();
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    RttOverallStatusHistogramBucket[] rttOverallStatusHistogramBucketArr = this.histogramOverallStatus;
                    int i = rttOverallStatusHistogramBucketArr == null ? 0 : rttOverallStatusHistogramBucketArr.length;
                    RttOverallStatusHistogramBucket[] newArray = new RttOverallStatusHistogramBucket[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.histogramOverallStatus, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new RttOverallStatusHistogramBucket();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new RttOverallStatusHistogramBucket();
                    input.readMessage(newArray[i]);
                    this.histogramOverallStatus = newArray;
                } else if (tag == 26) {
                    if (this.rttToAp == null) {
                        this.rttToAp = new RttToPeerLog();
                    }
                    input.readMessage(this.rttToAp);
                } else if (tag == 34) {
                    if (this.rttToAware == null) {
                        this.rttToAware = new RttToPeerLog();
                    }
                    input.readMessage(this.rttToAware);
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiRttLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiRttLog) MessageNano.mergeFrom(new WifiRttLog(), data);
        }

        public static WifiRttLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiRttLog().mergeFrom(input);
        }
    }

    public static final class WifiRadioUsage extends MessageNano {
        private static volatile WifiRadioUsage[] _emptyArray;
        public long loggingDurationMs;
        public long scanTimeMs;

        public static WifiRadioUsage[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiRadioUsage[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiRadioUsage() {
            clear();
        }

        public WifiRadioUsage clear() {
            this.loggingDurationMs = 0;
            this.scanTimeMs = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.loggingDurationMs;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            long j2 = this.scanTimeMs;
            if (j2 != 0) {
                output.writeInt64(2, j2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.loggingDurationMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            long j2 = this.scanTimeMs;
            if (j2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(2, j2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiRadioUsage mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.loggingDurationMs = input.readInt64();
                } else if (tag == 16) {
                    this.scanTimeMs = input.readInt64();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiRadioUsage parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiRadioUsage) MessageNano.mergeFrom(new WifiRadioUsage(), data);
        }

        public static WifiRadioUsage parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiRadioUsage().mergeFrom(input);
        }
    }

    public static final class ExperimentValues extends MessageNano {
        private static volatile ExperimentValues[] _emptyArray;
        public boolean linkSpeedCountsLoggingEnabled;
        public int wifiDataStallMinTxBad;
        public int wifiDataStallMinTxSuccessWithoutRx;
        public boolean wifiIsUnusableLoggingEnabled;

        public static ExperimentValues[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ExperimentValues[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ExperimentValues() {
            clear();
        }

        public ExperimentValues clear() {
            this.wifiIsUnusableLoggingEnabled = false;
            this.wifiDataStallMinTxBad = 0;
            this.wifiDataStallMinTxSuccessWithoutRx = 0;
            this.linkSpeedCountsLoggingEnabled = false;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            boolean z = this.wifiIsUnusableLoggingEnabled;
            if (z) {
                output.writeBool(1, z);
            }
            int i = this.wifiDataStallMinTxBad;
            if (i != 0) {
                output.writeInt32(2, i);
            }
            int i2 = this.wifiDataStallMinTxSuccessWithoutRx;
            if (i2 != 0) {
                output.writeInt32(3, i2);
            }
            boolean z2 = this.linkSpeedCountsLoggingEnabled;
            if (z2) {
                output.writeBool(4, z2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            boolean z = this.wifiIsUnusableLoggingEnabled;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, z);
            }
            int i = this.wifiDataStallMinTxBad;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            int i2 = this.wifiDataStallMinTxSuccessWithoutRx;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            boolean z2 = this.linkSpeedCountsLoggingEnabled;
            if (z2) {
                return size + CodedOutputByteBufferNano.computeBoolSize(4, z2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public ExperimentValues mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.wifiIsUnusableLoggingEnabled = input.readBool();
                } else if (tag == 16) {
                    this.wifiDataStallMinTxBad = input.readInt32();
                } else if (tag == 24) {
                    this.wifiDataStallMinTxSuccessWithoutRx = input.readInt32();
                } else if (tag == 32) {
                    this.linkSpeedCountsLoggingEnabled = input.readBool();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ExperimentValues parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ExperimentValues) MessageNano.mergeFrom(new ExperimentValues(), data);
        }

        public static ExperimentValues parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ExperimentValues().mergeFrom(input);
        }
    }

    public static final class WifiIsUnusableEvent extends MessageNano {
        public static final int TYPE_DATA_STALL_BAD_TX = 1;
        public static final int TYPE_DATA_STALL_BOTH = 3;
        public static final int TYPE_DATA_STALL_TX_WITHOUT_RX = 2;
        public static final int TYPE_FIRMWARE_ALERT = 4;
        public static final int TYPE_IP_REACHABILITY_LOST = 5;
        public static final int TYPE_UNKNOWN = 0;
        private static volatile WifiIsUnusableEvent[] _emptyArray;
        public int firmwareAlertCode;
        public long lastLinkLayerStatsUpdateTime;
        public int lastPredictionHorizonSec;
        public int lastScore;
        public int lastWifiUsabilityScore;
        public long packetUpdateTimeDelta;
        public long rxSuccessDelta;
        public boolean screenOn;
        public long startTimeMillis;
        public long txBadDelta;
        public long txRetriesDelta;
        public long txSuccessDelta;
        public int type;

        public static WifiIsUnusableEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiIsUnusableEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiIsUnusableEvent() {
            clear();
        }

        public WifiIsUnusableEvent clear() {
            this.type = 0;
            this.startTimeMillis = 0;
            this.lastScore = -1;
            this.txSuccessDelta = 0;
            this.txRetriesDelta = 0;
            this.txBadDelta = 0;
            this.rxSuccessDelta = 0;
            this.packetUpdateTimeDelta = 0;
            this.lastLinkLayerStatsUpdateTime = 0;
            this.firmwareAlertCode = -1;
            this.lastWifiUsabilityScore = -1;
            this.lastPredictionHorizonSec = -1;
            this.screenOn = false;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.type;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            long j = this.startTimeMillis;
            if (j != 0) {
                output.writeInt64(2, j);
            }
            int i2 = this.lastScore;
            if (i2 != -1) {
                output.writeInt32(3, i2);
            }
            long j2 = this.txSuccessDelta;
            if (j2 != 0) {
                output.writeInt64(4, j2);
            }
            long j3 = this.txRetriesDelta;
            if (j3 != 0) {
                output.writeInt64(5, j3);
            }
            long j4 = this.txBadDelta;
            if (j4 != 0) {
                output.writeInt64(6, j4);
            }
            long j5 = this.rxSuccessDelta;
            if (j5 != 0) {
                output.writeInt64(7, j5);
            }
            long j6 = this.packetUpdateTimeDelta;
            if (j6 != 0) {
                output.writeInt64(8, j6);
            }
            long j7 = this.lastLinkLayerStatsUpdateTime;
            if (j7 != 0) {
                output.writeInt64(9, j7);
            }
            int i3 = this.firmwareAlertCode;
            if (i3 != -1) {
                output.writeInt32(10, i3);
            }
            int i4 = this.lastWifiUsabilityScore;
            if (i4 != -1) {
                output.writeInt32(11, i4);
            }
            int i5 = this.lastPredictionHorizonSec;
            if (i5 != -1) {
                output.writeInt32(12, i5);
            }
            boolean z = this.screenOn;
            if (z) {
                output.writeBool(13, z);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.type;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            long j = this.startTimeMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, j);
            }
            int i2 = this.lastScore;
            if (i2 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            long j2 = this.txSuccessDelta;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, j2);
            }
            long j3 = this.txRetriesDelta;
            if (j3 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, j3);
            }
            long j4 = this.txBadDelta;
            if (j4 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, j4);
            }
            long j5 = this.rxSuccessDelta;
            if (j5 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, j5);
            }
            long j6 = this.packetUpdateTimeDelta;
            if (j6 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, j6);
            }
            long j7 = this.lastLinkLayerStatsUpdateTime;
            if (j7 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(9, j7);
            }
            int i3 = this.firmwareAlertCode;
            if (i3 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(10, i3);
            }
            int i4 = this.lastWifiUsabilityScore;
            if (i4 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, i4);
            }
            int i5 = this.lastPredictionHorizonSec;
            if (i5 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(12, i5);
            }
            boolean z = this.screenOn;
            if (z) {
                return size + CodedOutputByteBufferNano.computeBoolSize(13, z);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiIsUnusableEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        int value = input.readInt32();
                        if (value != 0 && value != 1 && value != 2 && value != 3 && value != 4 && value != 5) {
                            break;
                        } else {
                            this.type = value;
                            break;
                        }
                    case 16:
                        this.startTimeMillis = input.readInt64();
                        break;
                    case 24:
                        this.lastScore = input.readInt32();
                        break;
                    case 32:
                        this.txSuccessDelta = input.readInt64();
                        break;
                    case 40:
                        this.txRetriesDelta = input.readInt64();
                        break;
                    case 48:
                        this.txBadDelta = input.readInt64();
                        break;
                    case 56:
                        this.rxSuccessDelta = input.readInt64();
                        break;
                    case 64:
                        this.packetUpdateTimeDelta = input.readInt64();
                        break;
                    case 72:
                        this.lastLinkLayerStatsUpdateTime = input.readInt64();
                        break;
                    case 80:
                        this.firmwareAlertCode = input.readInt32();
                        break;
                    case 88:
                        this.lastWifiUsabilityScore = input.readInt32();
                        break;
                    case 96:
                        this.lastPredictionHorizonSec = input.readInt32();
                        break;
                    case 104:
                        this.screenOn = input.readBool();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static WifiIsUnusableEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiIsUnusableEvent) MessageNano.mergeFrom(new WifiIsUnusableEvent(), data);
        }

        public static WifiIsUnusableEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiIsUnusableEvent().mergeFrom(input);
        }
    }

    public static final class PasspointProfileTypeCount extends MessageNano {
        public static final int TYPE_EAP_AKA = 4;
        public static final int TYPE_EAP_AKA_PRIME = 5;
        public static final int TYPE_EAP_SIM = 3;
        public static final int TYPE_EAP_TLS = 1;
        public static final int TYPE_EAP_TTLS = 2;
        public static final int TYPE_UNKNOWN = 0;
        private static volatile PasspointProfileTypeCount[] _emptyArray;
        public int count;
        public int eapMethodType;

        public static PasspointProfileTypeCount[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PasspointProfileTypeCount[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PasspointProfileTypeCount() {
            clear();
        }

        public PasspointProfileTypeCount clear() {
            this.eapMethodType = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.eapMethodType;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.eapMethodType;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public PasspointProfileTypeCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4 || value == 5) {
                        this.eapMethodType = value;
                    }
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static PasspointProfileTypeCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (PasspointProfileTypeCount) MessageNano.mergeFrom(new PasspointProfileTypeCount(), data);
        }

        public static PasspointProfileTypeCount parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new PasspointProfileTypeCount().mergeFrom(input);
        }
    }

    public static final class WifiLinkLayerUsageStats extends MessageNano {
        private static volatile WifiLinkLayerUsageStats[] _emptyArray;
        public long loggingDurationMs;
        public long radioBackgroundScanTimeMs;
        public long radioHs20ScanTimeMs;
        public long radioNanScanTimeMs;
        public long radioOnTimeMs;
        public long radioPnoScanTimeMs;
        public long radioRoamScanTimeMs;
        public long radioRxTimeMs;
        public long radioScanTimeMs;
        public long radioTxTimeMs;

        public static WifiLinkLayerUsageStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiLinkLayerUsageStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiLinkLayerUsageStats() {
            clear();
        }

        public WifiLinkLayerUsageStats clear() {
            this.loggingDurationMs = 0;
            this.radioOnTimeMs = 0;
            this.radioTxTimeMs = 0;
            this.radioRxTimeMs = 0;
            this.radioScanTimeMs = 0;
            this.radioNanScanTimeMs = 0;
            this.radioBackgroundScanTimeMs = 0;
            this.radioRoamScanTimeMs = 0;
            this.radioPnoScanTimeMs = 0;
            this.radioHs20ScanTimeMs = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.loggingDurationMs;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            long j2 = this.radioOnTimeMs;
            if (j2 != 0) {
                output.writeInt64(2, j2);
            }
            long j3 = this.radioTxTimeMs;
            if (j3 != 0) {
                output.writeInt64(3, j3);
            }
            long j4 = this.radioRxTimeMs;
            if (j4 != 0) {
                output.writeInt64(4, j4);
            }
            long j5 = this.radioScanTimeMs;
            if (j5 != 0) {
                output.writeInt64(5, j5);
            }
            long j6 = this.radioNanScanTimeMs;
            if (j6 != 0) {
                output.writeInt64(6, j6);
            }
            long j7 = this.radioBackgroundScanTimeMs;
            if (j7 != 0) {
                output.writeInt64(7, j7);
            }
            long j8 = this.radioRoamScanTimeMs;
            if (j8 != 0) {
                output.writeInt64(8, j8);
            }
            long j9 = this.radioPnoScanTimeMs;
            if (j9 != 0) {
                output.writeInt64(9, j9);
            }
            long j10 = this.radioHs20ScanTimeMs;
            if (j10 != 0) {
                output.writeInt64(10, j10);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.loggingDurationMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            long j2 = this.radioOnTimeMs;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, j2);
            }
            long j3 = this.radioTxTimeMs;
            if (j3 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, j3);
            }
            long j4 = this.radioRxTimeMs;
            if (j4 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, j4);
            }
            long j5 = this.radioScanTimeMs;
            if (j5 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, j5);
            }
            long j6 = this.radioNanScanTimeMs;
            if (j6 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, j6);
            }
            long j7 = this.radioBackgroundScanTimeMs;
            if (j7 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, j7);
            }
            long j8 = this.radioRoamScanTimeMs;
            if (j8 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, j8);
            }
            long j9 = this.radioPnoScanTimeMs;
            if (j9 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(9, j9);
            }
            long j10 = this.radioHs20ScanTimeMs;
            if (j10 != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(10, j10);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiLinkLayerUsageStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.loggingDurationMs = input.readInt64();
                        break;
                    case 16:
                        this.radioOnTimeMs = input.readInt64();
                        break;
                    case 24:
                        this.radioTxTimeMs = input.readInt64();
                        break;
                    case 32:
                        this.radioRxTimeMs = input.readInt64();
                        break;
                    case 40:
                        this.radioScanTimeMs = input.readInt64();
                        break;
                    case 48:
                        this.radioNanScanTimeMs = input.readInt64();
                        break;
                    case 56:
                        this.radioBackgroundScanTimeMs = input.readInt64();
                        break;
                    case 64:
                        this.radioRoamScanTimeMs = input.readInt64();
                        break;
                    case 72:
                        this.radioPnoScanTimeMs = input.readInt64();
                        break;
                    case 80:
                        this.radioHs20ScanTimeMs = input.readInt64();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static WifiLinkLayerUsageStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiLinkLayerUsageStats) MessageNano.mergeFrom(new WifiLinkLayerUsageStats(), data);
        }

        public static WifiLinkLayerUsageStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiLinkLayerUsageStats().mergeFrom(input);
        }
    }

    public static final class WifiUsabilityStatsEntry extends MessageNano {
        public static final int NETWORK_TYPE_CDMA = 2;
        public static final int NETWORK_TYPE_EVDO_0 = 3;
        public static final int NETWORK_TYPE_GSM = 1;
        public static final int NETWORK_TYPE_LTE = 6;
        public static final int NETWORK_TYPE_NR = 7;
        public static final int NETWORK_TYPE_TD_SCDMA = 5;
        public static final int NETWORK_TYPE_UMTS = 4;
        public static final int NETWORK_TYPE_UNKNOWN = 0;
        public static final int PROBE_STATUS_FAILURE = 3;
        public static final int PROBE_STATUS_NO_PROBE = 1;
        public static final int PROBE_STATUS_SUCCESS = 2;
        public static final int PROBE_STATUS_UNKNOWN = 0;
        private static volatile WifiUsabilityStatsEntry[] _emptyArray;
        public int cellularDataNetworkType;
        public int cellularSignalStrengthDb;
        public int cellularSignalStrengthDbm;
        public int deviceMobilityState;
        public boolean isSameBssidAndFreq;
        public boolean isSameRegisteredCell;
        public int linkSpeedMbps;
        public int predictionHorizonSec;
        public int probeElapsedTimeSinceLastUpdateMs;
        public int probeMcsRateSinceLastUpdate;
        public int probeStatusSinceLastUpdate;
        public int rssi;
        public int rxLinkSpeedMbps;
        public int seqNumInsideFramework;
        public int seqNumToFramework;
        public long timeStampMs;
        public long totalBackgroundScanTimeMs;
        public long totalBeaconRx;
        public long totalCcaBusyFreqTimeMs;
        public long totalHotspot2ScanTimeMs;
        public long totalNanScanTimeMs;
        public long totalPnoScanTimeMs;
        public long totalRadioOnFreqTimeMs;
        public long totalRadioOnTimeMs;
        public long totalRadioRxTimeMs;
        public long totalRadioTxTimeMs;
        public long totalRoamScanTimeMs;
        public long totalRxSuccess;
        public long totalScanTimeMs;
        public long totalTxBad;
        public long totalTxRetries;
        public long totalTxSuccess;
        public int wifiScore;
        public int wifiUsabilityScore;

        public static WifiUsabilityStatsEntry[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiUsabilityStatsEntry[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiUsabilityStatsEntry() {
            clear();
        }

        public WifiUsabilityStatsEntry clear() {
            this.timeStampMs = 0;
            this.rssi = 0;
            this.linkSpeedMbps = 0;
            this.totalTxSuccess = 0;
            this.totalTxRetries = 0;
            this.totalTxBad = 0;
            this.totalRxSuccess = 0;
            this.totalRadioOnTimeMs = 0;
            this.totalRadioTxTimeMs = 0;
            this.totalRadioRxTimeMs = 0;
            this.totalScanTimeMs = 0;
            this.totalNanScanTimeMs = 0;
            this.totalBackgroundScanTimeMs = 0;
            this.totalRoamScanTimeMs = 0;
            this.totalPnoScanTimeMs = 0;
            this.totalHotspot2ScanTimeMs = 0;
            this.wifiScore = 0;
            this.wifiUsabilityScore = 0;
            this.seqNumToFramework = 0;
            this.totalCcaBusyFreqTimeMs = 0;
            this.totalRadioOnFreqTimeMs = 0;
            this.totalBeaconRx = 0;
            this.predictionHorizonSec = 0;
            this.probeStatusSinceLastUpdate = 0;
            this.probeElapsedTimeSinceLastUpdateMs = 0;
            this.probeMcsRateSinceLastUpdate = 0;
            this.rxLinkSpeedMbps = 0;
            this.seqNumInsideFramework = 0;
            this.isSameBssidAndFreq = false;
            this.cellularDataNetworkType = 0;
            this.cellularSignalStrengthDbm = 0;
            this.cellularSignalStrengthDb = 0;
            this.isSameRegisteredCell = false;
            this.deviceMobilityState = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.timeStampMs;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            int i = this.rssi;
            if (i != 0) {
                output.writeInt32(2, i);
            }
            int i2 = this.linkSpeedMbps;
            if (i2 != 0) {
                output.writeInt32(3, i2);
            }
            long j2 = this.totalTxSuccess;
            if (j2 != 0) {
                output.writeInt64(4, j2);
            }
            long j3 = this.totalTxRetries;
            if (j3 != 0) {
                output.writeInt64(5, j3);
            }
            long j4 = this.totalTxBad;
            if (j4 != 0) {
                output.writeInt64(6, j4);
            }
            long j5 = this.totalRxSuccess;
            if (j5 != 0) {
                output.writeInt64(7, j5);
            }
            long j6 = this.totalRadioOnTimeMs;
            if (j6 != 0) {
                output.writeInt64(8, j6);
            }
            long j7 = this.totalRadioTxTimeMs;
            if (j7 != 0) {
                output.writeInt64(9, j7);
            }
            long j8 = this.totalRadioRxTimeMs;
            if (j8 != 0) {
                output.writeInt64(10, j8);
            }
            long j9 = this.totalScanTimeMs;
            if (j9 != 0) {
                output.writeInt64(11, j9);
            }
            long j10 = this.totalNanScanTimeMs;
            if (j10 != 0) {
                output.writeInt64(12, j10);
            }
            long j11 = this.totalBackgroundScanTimeMs;
            if (j11 != 0) {
                output.writeInt64(13, j11);
            }
            long j12 = this.totalRoamScanTimeMs;
            if (j12 != 0) {
                output.writeInt64(14, j12);
            }
            long j13 = this.totalPnoScanTimeMs;
            if (j13 != 0) {
                output.writeInt64(15, j13);
            }
            long j14 = this.totalHotspot2ScanTimeMs;
            if (j14 != 0) {
                output.writeInt64(16, j14);
            }
            int i3 = this.wifiScore;
            if (i3 != 0) {
                output.writeInt32(17, i3);
            }
            int i4 = this.wifiUsabilityScore;
            if (i4 != 0) {
                output.writeInt32(18, i4);
            }
            int i5 = this.seqNumToFramework;
            if (i5 != 0) {
                output.writeInt32(19, i5);
            }
            long j15 = this.totalCcaBusyFreqTimeMs;
            if (j15 != 0) {
                output.writeInt64(20, j15);
            }
            long j16 = this.totalRadioOnFreqTimeMs;
            if (j16 != 0) {
                output.writeInt64(21, j16);
            }
            long j17 = this.totalBeaconRx;
            if (j17 != 0) {
                output.writeInt64(22, j17);
            }
            int i6 = this.predictionHorizonSec;
            if (i6 != 0) {
                output.writeInt32(23, i6);
            }
            int i7 = this.probeStatusSinceLastUpdate;
            if (i7 != 0) {
                output.writeInt32(24, i7);
            }
            int i8 = this.probeElapsedTimeSinceLastUpdateMs;
            if (i8 != 0) {
                output.writeInt32(25, i8);
            }
            int i9 = this.probeMcsRateSinceLastUpdate;
            if (i9 != 0) {
                output.writeInt32(26, i9);
            }
            int i10 = this.rxLinkSpeedMbps;
            if (i10 != 0) {
                output.writeInt32(27, i10);
            }
            int i11 = this.seqNumInsideFramework;
            if (i11 != 0) {
                output.writeInt32(28, i11);
            }
            boolean z = this.isSameBssidAndFreq;
            if (z) {
                output.writeBool(29, z);
            }
            int i12 = this.cellularDataNetworkType;
            if (i12 != 0) {
                output.writeInt32(30, i12);
            }
            int i13 = this.cellularSignalStrengthDbm;
            if (i13 != 0) {
                output.writeInt32(31, i13);
            }
            int i14 = this.cellularSignalStrengthDb;
            if (i14 != 0) {
                output.writeInt32(32, i14);
            }
            boolean z2 = this.isSameRegisteredCell;
            if (z2) {
                output.writeBool(33, z2);
            }
            int i15 = this.deviceMobilityState;
            if (i15 != 0) {
                output.writeInt32(34, i15);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.timeStampMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            int i = this.rssi;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            int i2 = this.linkSpeedMbps;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            long j2 = this.totalTxSuccess;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, j2);
            }
            long j3 = this.totalTxRetries;
            if (j3 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, j3);
            }
            long j4 = this.totalTxBad;
            if (j4 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, j4);
            }
            long j5 = this.totalRxSuccess;
            if (j5 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, j5);
            }
            long j6 = this.totalRadioOnTimeMs;
            if (j6 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, j6);
            }
            long j7 = this.totalRadioTxTimeMs;
            if (j7 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(9, j7);
            }
            long j8 = this.totalRadioRxTimeMs;
            if (j8 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(10, j8);
            }
            long j9 = this.totalScanTimeMs;
            if (j9 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(11, j9);
            }
            long j10 = this.totalNanScanTimeMs;
            if (j10 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(12, j10);
            }
            long j11 = this.totalBackgroundScanTimeMs;
            if (j11 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(13, j11);
            }
            long j12 = this.totalRoamScanTimeMs;
            if (j12 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(14, j12);
            }
            long j13 = this.totalPnoScanTimeMs;
            if (j13 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(15, j13);
            }
            long j14 = this.totalHotspot2ScanTimeMs;
            if (j14 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(16, j14);
            }
            int i3 = this.wifiScore;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(17, i3);
            }
            int i4 = this.wifiUsabilityScore;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(18, i4);
            }
            int i5 = this.seqNumToFramework;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(19, i5);
            }
            long j15 = this.totalCcaBusyFreqTimeMs;
            if (j15 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(20, j15);
            }
            long j16 = this.totalRadioOnFreqTimeMs;
            if (j16 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(21, j16);
            }
            long j17 = this.totalBeaconRx;
            if (j17 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(22, j17);
            }
            int i6 = this.predictionHorizonSec;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(23, i6);
            }
            int i7 = this.probeStatusSinceLastUpdate;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(24, i7);
            }
            int i8 = this.probeElapsedTimeSinceLastUpdateMs;
            if (i8 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(25, i8);
            }
            int i9 = this.probeMcsRateSinceLastUpdate;
            if (i9 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(26, i9);
            }
            int i10 = this.rxLinkSpeedMbps;
            if (i10 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(27, i10);
            }
            int i11 = this.seqNumInsideFramework;
            if (i11 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(28, i11);
            }
            boolean z = this.isSameBssidAndFreq;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(29, z);
            }
            int i12 = this.cellularDataNetworkType;
            if (i12 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(30, i12);
            }
            int i13 = this.cellularSignalStrengthDbm;
            if (i13 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(31, i13);
            }
            int i14 = this.cellularSignalStrengthDb;
            if (i14 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(32, i14);
            }
            boolean z2 = this.isSameRegisteredCell;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(33, z2);
            }
            int i15 = this.deviceMobilityState;
            if (i15 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(34, i15);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiUsabilityStatsEntry mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.timeStampMs = input.readInt64();
                        break;
                    case 16:
                        this.rssi = input.readInt32();
                        break;
                    case 24:
                        this.linkSpeedMbps = input.readInt32();
                        break;
                    case 32:
                        this.totalTxSuccess = input.readInt64();
                        break;
                    case 40:
                        this.totalTxRetries = input.readInt64();
                        break;
                    case 48:
                        this.totalTxBad = input.readInt64();
                        break;
                    case 56:
                        this.totalRxSuccess = input.readInt64();
                        break;
                    case 64:
                        this.totalRadioOnTimeMs = input.readInt64();
                        break;
                    case 72:
                        this.totalRadioTxTimeMs = input.readInt64();
                        break;
                    case 80:
                        this.totalRadioRxTimeMs = input.readInt64();
                        break;
                    case 88:
                        this.totalScanTimeMs = input.readInt64();
                        break;
                    case 96:
                        this.totalNanScanTimeMs = input.readInt64();
                        break;
                    case 104:
                        this.totalBackgroundScanTimeMs = input.readInt64();
                        break;
                    case 112:
                        this.totalRoamScanTimeMs = input.readInt64();
                        break;
                    case 120:
                        this.totalPnoScanTimeMs = input.readInt64();
                        break;
                    case 128:
                        this.totalHotspot2ScanTimeMs = input.readInt64();
                        break;
                    case 136:
                        this.wifiScore = input.readInt32();
                        break;
                    case 144:
                        this.wifiUsabilityScore = input.readInt32();
                        break;
                    case 152:
                        this.seqNumToFramework = input.readInt32();
                        break;
                    case 160:
                        this.totalCcaBusyFreqTimeMs = input.readInt64();
                        break;
                    case 168:
                        this.totalRadioOnFreqTimeMs = input.readInt64();
                        break;
                    case 176:
                        this.totalBeaconRx = input.readInt64();
                        break;
                    case 184:
                        this.predictionHorizonSec = input.readInt32();
                        break;
                    case 192:
                        int value = input.readInt32();
                        if (value != 0 && value != 1 && value != 2 && value != 3) {
                            break;
                        } else {
                            this.probeStatusSinceLastUpdate = value;
                            break;
                        }
                    case 200:
                        this.probeElapsedTimeSinceLastUpdateMs = input.readInt32();
                        break;
                    case 208:
                        this.probeMcsRateSinceLastUpdate = input.readInt32();
                        break;
                    case 216:
                        this.rxLinkSpeedMbps = input.readInt32();
                        break;
                    case 224:
                        this.seqNumInsideFramework = input.readInt32();
                        break;
                    case 232:
                        this.isSameBssidAndFreq = input.readBool();
                        break;
                    case 240:
                        int value2 = input.readInt32();
                        switch (value2) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                                this.cellularDataNetworkType = value2;
                                continue;
                        }
                    case 248:
                        this.cellularSignalStrengthDbm = input.readInt32();
                        break;
                    case 256:
                        this.cellularSignalStrengthDb = input.readInt32();
                        break;
                    case 264:
                        this.isSameRegisteredCell = input.readBool();
                        break;
                    case 272:
                        int value3 = input.readInt32();
                        if (value3 != 0 && value3 != 1 && value3 != 2 && value3 != 3) {
                            break;
                        } else {
                            this.deviceMobilityState = value3;
                            break;
                        }
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static WifiUsabilityStatsEntry parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiUsabilityStatsEntry) MessageNano.mergeFrom(new WifiUsabilityStatsEntry(), data);
        }

        public static WifiUsabilityStatsEntry parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiUsabilityStatsEntry().mergeFrom(input);
        }
    }

    public static final class WifiUsabilityStats extends MessageNano {
        public static final int LABEL_BAD = 2;
        public static final int LABEL_GOOD = 1;
        public static final int LABEL_UNKNOWN = 0;
        public static final int TYPE_DATA_STALL_BAD_TX = 1;
        public static final int TYPE_DATA_STALL_BOTH = 3;
        public static final int TYPE_DATA_STALL_TX_WITHOUT_RX = 2;
        public static final int TYPE_FIRMWARE_ALERT = 4;
        public static final int TYPE_IP_REACHABILITY_LOST = 5;
        public static final int TYPE_UNKNOWN = 0;
        private static volatile WifiUsabilityStats[] _emptyArray;
        public int firmwareAlertCode;
        public int label;
        public WifiUsabilityStatsEntry[] stats;
        public long timeStampMs;
        public int triggerType;

        public static WifiUsabilityStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiUsabilityStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiUsabilityStats() {
            clear();
        }

        public WifiUsabilityStats clear() {
            this.label = 0;
            this.stats = WifiUsabilityStatsEntry.emptyArray();
            this.triggerType = 0;
            this.firmwareAlertCode = -1;
            this.timeStampMs = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.label;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            WifiUsabilityStatsEntry[] wifiUsabilityStatsEntryArr = this.stats;
            if (wifiUsabilityStatsEntryArr != null && wifiUsabilityStatsEntryArr.length > 0) {
                int i2 = 0;
                while (true) {
                    WifiUsabilityStatsEntry[] wifiUsabilityStatsEntryArr2 = this.stats;
                    if (i2 >= wifiUsabilityStatsEntryArr2.length) {
                        break;
                    }
                    WifiUsabilityStatsEntry element = wifiUsabilityStatsEntryArr2[i2];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i2++;
                }
            }
            int i3 = this.triggerType;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.firmwareAlertCode;
            if (i4 != -1) {
                output.writeInt32(4, i4);
            }
            long j = this.timeStampMs;
            if (j != 0) {
                output.writeInt64(5, j);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.label;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            WifiUsabilityStatsEntry[] wifiUsabilityStatsEntryArr = this.stats;
            if (wifiUsabilityStatsEntryArr != null && wifiUsabilityStatsEntryArr.length > 0) {
                int i2 = 0;
                while (true) {
                    WifiUsabilityStatsEntry[] wifiUsabilityStatsEntryArr2 = this.stats;
                    if (i2 >= wifiUsabilityStatsEntryArr2.length) {
                        break;
                    }
                    WifiUsabilityStatsEntry element = wifiUsabilityStatsEntryArr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i2++;
                }
            }
            int i3 = this.triggerType;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.firmwareAlertCode;
            if (i4 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            long j = this.timeStampMs;
            if (j != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(5, j);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiUsabilityStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2) {
                        this.label = value;
                    }
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    WifiUsabilityStatsEntry[] wifiUsabilityStatsEntryArr = this.stats;
                    int i = wifiUsabilityStatsEntryArr == null ? 0 : wifiUsabilityStatsEntryArr.length;
                    WifiUsabilityStatsEntry[] newArray = new WifiUsabilityStatsEntry[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.stats, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new WifiUsabilityStatsEntry();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new WifiUsabilityStatsEntry();
                    input.readMessage(newArray[i]);
                    this.stats = newArray;
                } else if (tag == 24) {
                    int value2 = input.readInt32();
                    if (value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3 || value2 == 4 || value2 == 5) {
                        this.triggerType = value2;
                    }
                } else if (tag == 32) {
                    this.firmwareAlertCode = input.readInt32();
                } else if (tag == 40) {
                    this.timeStampMs = input.readInt64();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiUsabilityStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiUsabilityStats) MessageNano.mergeFrom(new WifiUsabilityStats(), data);
        }

        public static WifiUsabilityStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiUsabilityStats().mergeFrom(input);
        }
    }

    public static final class DeviceMobilityStatePnoScanStats extends MessageNano {
        public static final int HIGH_MVMT = 1;
        public static final int LOW_MVMT = 2;
        public static final int STATIONARY = 3;
        public static final int UNKNOWN = 0;
        private static volatile DeviceMobilityStatePnoScanStats[] _emptyArray;
        public int deviceMobilityState;
        public int numTimesEnteredState;
        public long pnoDurationMs;
        public long totalDurationMs;

        public static DeviceMobilityStatePnoScanStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DeviceMobilityStatePnoScanStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DeviceMobilityStatePnoScanStats() {
            clear();
        }

        public DeviceMobilityStatePnoScanStats clear() {
            this.deviceMobilityState = 0;
            this.numTimesEnteredState = 0;
            this.totalDurationMs = 0;
            this.pnoDurationMs = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.deviceMobilityState;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numTimesEnteredState;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            long j = this.totalDurationMs;
            if (j != 0) {
                output.writeInt64(3, j);
            }
            long j2 = this.pnoDurationMs;
            if (j2 != 0) {
                output.writeInt64(4, j2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.deviceMobilityState;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numTimesEnteredState;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            long j = this.totalDurationMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, j);
            }
            long j2 = this.pnoDurationMs;
            if (j2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(4, j2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public DeviceMobilityStatePnoScanStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3) {
                        this.deviceMobilityState = value;
                    }
                } else if (tag == 16) {
                    this.numTimesEnteredState = input.readInt32();
                } else if (tag == 24) {
                    this.totalDurationMs = input.readInt64();
                } else if (tag == 32) {
                    this.pnoDurationMs = input.readInt64();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static DeviceMobilityStatePnoScanStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DeviceMobilityStatePnoScanStats) MessageNano.mergeFrom(new DeviceMobilityStatePnoScanStats(), data);
        }

        public static DeviceMobilityStatePnoScanStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DeviceMobilityStatePnoScanStats().mergeFrom(input);
        }
    }

    public static final class WifiP2pStats extends MessageNano {
        private static volatile WifiP2pStats[] _emptyArray;
        public P2pConnectionEvent[] connectionEvent;
        public GroupEvent[] groupEvent;
        public int numPersistentGroup;
        public int numTotalPeerScans;
        public int numTotalServiceScans;

        public static WifiP2pStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiP2pStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiP2pStats() {
            clear();
        }

        public WifiP2pStats clear() {
            this.groupEvent = GroupEvent.emptyArray();
            this.connectionEvent = P2pConnectionEvent.emptyArray();
            this.numPersistentGroup = 0;
            this.numTotalPeerScans = 0;
            this.numTotalServiceScans = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            GroupEvent[] groupEventArr = this.groupEvent;
            if (groupEventArr != null && groupEventArr.length > 0) {
                int i = 0;
                while (true) {
                    GroupEvent[] groupEventArr2 = this.groupEvent;
                    if (i >= groupEventArr2.length) {
                        break;
                    }
                    GroupEvent element = groupEventArr2[i];
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                    i++;
                }
            }
            P2pConnectionEvent[] p2pConnectionEventArr = this.connectionEvent;
            if (p2pConnectionEventArr != null && p2pConnectionEventArr.length > 0) {
                int i2 = 0;
                while (true) {
                    P2pConnectionEvent[] p2pConnectionEventArr2 = this.connectionEvent;
                    if (i2 >= p2pConnectionEventArr2.length) {
                        break;
                    }
                    P2pConnectionEvent element2 = p2pConnectionEventArr2[i2];
                    if (element2 != null) {
                        output.writeMessage(2, element2);
                    }
                    i2++;
                }
            }
            int i3 = this.numPersistentGroup;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numTotalPeerScans;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.numTotalServiceScans;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            GroupEvent[] groupEventArr = this.groupEvent;
            if (groupEventArr != null && groupEventArr.length > 0) {
                int i = 0;
                while (true) {
                    GroupEvent[] groupEventArr2 = this.groupEvent;
                    if (i >= groupEventArr2.length) {
                        break;
                    }
                    GroupEvent element = groupEventArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                    i++;
                }
            }
            P2pConnectionEvent[] p2pConnectionEventArr = this.connectionEvent;
            if (p2pConnectionEventArr != null && p2pConnectionEventArr.length > 0) {
                int i2 = 0;
                while (true) {
                    P2pConnectionEvent[] p2pConnectionEventArr2 = this.connectionEvent;
                    if (i2 >= p2pConnectionEventArr2.length) {
                        break;
                    }
                    P2pConnectionEvent element2 = p2pConnectionEventArr2[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                    }
                    i2++;
                }
            }
            int i3 = this.numPersistentGroup;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numTotalPeerScans;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.numTotalServiceScans;
            if (i5 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiP2pStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    GroupEvent[] groupEventArr = this.groupEvent;
                    int i = groupEventArr == null ? 0 : groupEventArr.length;
                    GroupEvent[] newArray = new GroupEvent[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.groupEvent, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new GroupEvent();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new GroupEvent();
                    input.readMessage(newArray[i]);
                    this.groupEvent = newArray;
                } else if (tag == 18) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    P2pConnectionEvent[] p2pConnectionEventArr = this.connectionEvent;
                    int i2 = p2pConnectionEventArr == null ? 0 : p2pConnectionEventArr.length;
                    P2pConnectionEvent[] newArray2 = new P2pConnectionEvent[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.connectionEvent, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = new P2pConnectionEvent();
                        input.readMessage(newArray2[i2]);
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = new P2pConnectionEvent();
                    input.readMessage(newArray2[i2]);
                    this.connectionEvent = newArray2;
                } else if (tag == 24) {
                    this.numPersistentGroup = input.readInt32();
                } else if (tag == 32) {
                    this.numTotalPeerScans = input.readInt32();
                } else if (tag == 40) {
                    this.numTotalServiceScans = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiP2pStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiP2pStats) MessageNano.mergeFrom(new WifiP2pStats(), data);
        }

        public static WifiP2pStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiP2pStats().mergeFrom(input);
        }
    }

    public static final class P2pConnectionEvent extends MessageNano {
        public static final int CLF_CANCEL = 3;
        public static final int CLF_INVITATION_FAIL = 5;
        public static final int CLF_NEW_CONNECTION_ATTEMPT = 7;
        public static final int CLF_NONE = 1;
        public static final int CLF_PROV_DISC_FAIL = 4;
        public static final int CLF_TIMEOUT = 2;
        public static final int CLF_UNKNOWN = 0;
        public static final int CLF_USER_REJECT = 6;
        public static final int CONNECTION_FAST = 3;
        public static final int CONNECTION_FRESH = 0;
        public static final int CONNECTION_LOCAL = 2;
        public static final int CONNECTION_REINVOKE = 1;
        public static final int WPS_DISPLAY = 1;
        public static final int WPS_KEYPAD = 2;
        public static final int WPS_LABEL = 3;
        public static final int WPS_NA = -1;
        public static final int WPS_PBC = 0;
        private static volatile P2pConnectionEvent[] _emptyArray;
        public int connectionType;
        public int connectivityLevelFailureCode;
        public int durationTakenToConnectMillis;
        public long startTimeMillis;
        public int wpsMethod;

        public static P2pConnectionEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new P2pConnectionEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public P2pConnectionEvent() {
            clear();
        }

        public P2pConnectionEvent clear() {
            this.startTimeMillis = 0;
            this.connectionType = 0;
            this.wpsMethod = -1;
            this.durationTakenToConnectMillis = 0;
            this.connectivityLevelFailureCode = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.startTimeMillis;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            int i = this.connectionType;
            if (i != 0) {
                output.writeInt32(2, i);
            }
            int i2 = this.wpsMethod;
            if (i2 != -1) {
                output.writeInt32(3, i2);
            }
            int i3 = this.durationTakenToConnectMillis;
            if (i3 != 0) {
                output.writeInt32(4, i3);
            }
            int i4 = this.connectivityLevelFailureCode;
            if (i4 != 0) {
                output.writeInt32(5, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.startTimeMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            int i = this.connectionType;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            int i2 = this.wpsMethod;
            if (i2 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            int i3 = this.durationTakenToConnectMillis;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i3);
            }
            int i4 = this.connectivityLevelFailureCode;
            if (i4 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(5, i4);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public P2pConnectionEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.startTimeMillis = input.readInt64();
                } else if (tag == 16) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3) {
                        this.connectionType = value;
                    }
                } else if (tag == 24) {
                    int value2 = input.readInt32();
                    if (value2 == -1 || value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3) {
                        this.wpsMethod = value2;
                    }
                } else if (tag == 32) {
                    this.durationTakenToConnectMillis = input.readInt32();
                } else if (tag == 40) {
                    int value3 = input.readInt32();
                    switch (value3) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                            this.connectivityLevelFailureCode = value3;
                            continue;
                    }
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static P2pConnectionEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (P2pConnectionEvent) MessageNano.mergeFrom(new P2pConnectionEvent(), data);
        }

        public static P2pConnectionEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new P2pConnectionEvent().mergeFrom(input);
        }
    }

    public static final class GroupEvent extends MessageNano {
        public static final int GROUP_CLIENT = 1;
        public static final int GROUP_OWNER = 0;
        private static volatile GroupEvent[] _emptyArray;
        public int channelFrequency;
        public int groupRole;
        public int idleDurationMillis;
        public int netId;
        public int numConnectedClients;
        public int numCumulativeClients;
        public int sessionDurationMillis;
        public long startTimeMillis;

        public static GroupEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new GroupEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public GroupEvent() {
            clear();
        }

        public GroupEvent clear() {
            this.netId = 0;
            this.startTimeMillis = 0;
            this.channelFrequency = 0;
            this.groupRole = 0;
            this.numConnectedClients = 0;
            this.numCumulativeClients = 0;
            this.sessionDurationMillis = 0;
            this.idleDurationMillis = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.netId;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            long j = this.startTimeMillis;
            if (j != 0) {
                output.writeInt64(2, j);
            }
            int i2 = this.channelFrequency;
            if (i2 != 0) {
                output.writeInt32(3, i2);
            }
            int i3 = this.groupRole;
            if (i3 != 0) {
                output.writeInt32(5, i3);
            }
            int i4 = this.numConnectedClients;
            if (i4 != 0) {
                output.writeInt32(6, i4);
            }
            int i5 = this.numCumulativeClients;
            if (i5 != 0) {
                output.writeInt32(7, i5);
            }
            int i6 = this.sessionDurationMillis;
            if (i6 != 0) {
                output.writeInt32(8, i6);
            }
            int i7 = this.idleDurationMillis;
            if (i7 != 0) {
                output.writeInt32(9, i7);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.netId;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            long j = this.startTimeMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, j);
            }
            int i2 = this.channelFrequency;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            int i3 = this.groupRole;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i3);
            }
            int i4 = this.numConnectedClients;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i4);
            }
            int i5 = this.numCumulativeClients;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i5);
            }
            int i6 = this.sessionDurationMillis;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i6);
            }
            int i7 = this.idleDurationMillis;
            if (i7 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(9, i7);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public GroupEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.netId = input.readInt32();
                } else if (tag == 16) {
                    this.startTimeMillis = input.readInt64();
                } else if (tag == 24) {
                    this.channelFrequency = input.readInt32();
                } else if (tag == 40) {
                    int value = input.readInt32();
                    if (value == 0 || value == 1) {
                        this.groupRole = value;
                    }
                } else if (tag == 48) {
                    this.numConnectedClients = input.readInt32();
                } else if (tag == 56) {
                    this.numCumulativeClients = input.readInt32();
                } else if (tag == 64) {
                    this.sessionDurationMillis = input.readInt32();
                } else if (tag == 72) {
                    this.idleDurationMillis = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static GroupEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (GroupEvent) MessageNano.mergeFrom(new GroupEvent(), data);
        }

        public static GroupEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new GroupEvent().mergeFrom(input);
        }
    }

    public static final class WifiDppLog extends MessageNano {
        public static final int EASY_CONNECT_EVENT_FAILURE_AUTHENTICATION = 2;
        public static final int EASY_CONNECT_EVENT_FAILURE_BUSY = 5;
        public static final int EASY_CONNECT_EVENT_FAILURE_CONFIGURATION = 4;
        public static final int EASY_CONNECT_EVENT_FAILURE_GENERIC = 7;
        public static final int EASY_CONNECT_EVENT_FAILURE_INVALID_NETWORK = 9;
        public static final int EASY_CONNECT_EVENT_FAILURE_INVALID_URI = 1;
        public static final int EASY_CONNECT_EVENT_FAILURE_NOT_COMPATIBLE = 3;
        public static final int EASY_CONNECT_EVENT_FAILURE_NOT_SUPPORTED = 8;
        public static final int EASY_CONNECT_EVENT_FAILURE_TIMEOUT = 6;
        public static final int EASY_CONNECT_EVENT_FAILURE_UNKNOWN = 0;
        public static final int EASY_CONNECT_EVENT_SUCCESS_CONFIGURATION_SENT = 1;
        public static final int EASY_CONNECT_EVENT_SUCCESS_UNKNOWN = 0;
        private static volatile WifiDppLog[] _emptyArray;
        public DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessCode;
        public DppFailureStatusHistogramBucket[] dppFailureCode;
        public HistogramBucketInt32[] dppOperationTime;
        public int numDppConfiguratorInitiatorRequests;
        public int numDppEnrolleeInitiatorRequests;
        public int numDppEnrolleeSuccess;

        public static final class DppConfiguratorSuccessStatusHistogramBucket extends MessageNano {
            private static volatile DppConfiguratorSuccessStatusHistogramBucket[] _emptyArray;
            public int count;
            public int dppStatusType;

            public static DppConfiguratorSuccessStatusHistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new DppConfiguratorSuccessStatusHistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public DppConfiguratorSuccessStatusHistogramBucket() {
                clear();
            }

            public DppConfiguratorSuccessStatusHistogramBucket clear() {
                this.dppStatusType = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.dppStatusType;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.dppStatusType;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public DppConfiguratorSuccessStatusHistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        if (value == 0 || value == 1) {
                            this.dppStatusType = value;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static DppConfiguratorSuccessStatusHistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (DppConfiguratorSuccessStatusHistogramBucket) MessageNano.mergeFrom(new DppConfiguratorSuccessStatusHistogramBucket(), data);
            }

            public static DppConfiguratorSuccessStatusHistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new DppConfiguratorSuccessStatusHistogramBucket().mergeFrom(input);
            }
        }

        public static final class DppFailureStatusHistogramBucket extends MessageNano {
            private static volatile DppFailureStatusHistogramBucket[] _emptyArray;
            public int count;
            public int dppStatusType;

            public static DppFailureStatusHistogramBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new DppFailureStatusHistogramBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public DppFailureStatusHistogramBucket() {
                clear();
            }

            public DppFailureStatusHistogramBucket clear() {
                this.dppStatusType = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.dppStatusType;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.dppStatusType;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public DppFailureStatusHistogramBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                                this.dppStatusType = value;
                                continue;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static DppFailureStatusHistogramBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (DppFailureStatusHistogramBucket) MessageNano.mergeFrom(new DppFailureStatusHistogramBucket(), data);
            }

            public static DppFailureStatusHistogramBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new DppFailureStatusHistogramBucket().mergeFrom(input);
            }
        }

        public static WifiDppLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiDppLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiDppLog() {
            clear();
        }

        public WifiDppLog clear() {
            this.numDppConfiguratorInitiatorRequests = 0;
            this.numDppEnrolleeInitiatorRequests = 0;
            this.numDppEnrolleeSuccess = 0;
            this.dppConfiguratorSuccessCode = DppConfiguratorSuccessStatusHistogramBucket.emptyArray();
            this.dppFailureCode = DppFailureStatusHistogramBucket.emptyArray();
            this.dppOperationTime = HistogramBucketInt32.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numDppConfiguratorInitiatorRequests;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numDppEnrolleeInitiatorRequests;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.numDppEnrolleeSuccess;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessStatusHistogramBucketArr = this.dppConfiguratorSuccessCode;
            if (dppConfiguratorSuccessStatusHistogramBucketArr != null && dppConfiguratorSuccessStatusHistogramBucketArr.length > 0) {
                int i4 = 0;
                while (true) {
                    DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessStatusHistogramBucketArr2 = this.dppConfiguratorSuccessCode;
                    if (i4 >= dppConfiguratorSuccessStatusHistogramBucketArr2.length) {
                        break;
                    }
                    DppConfiguratorSuccessStatusHistogramBucket element = dppConfiguratorSuccessStatusHistogramBucketArr2[i4];
                    if (element != null) {
                        output.writeMessage(4, element);
                    }
                    i4++;
                }
            }
            DppFailureStatusHistogramBucket[] dppFailureStatusHistogramBucketArr = this.dppFailureCode;
            if (dppFailureStatusHistogramBucketArr != null && dppFailureStatusHistogramBucketArr.length > 0) {
                int i5 = 0;
                while (true) {
                    DppFailureStatusHistogramBucket[] dppFailureStatusHistogramBucketArr2 = this.dppFailureCode;
                    if (i5 >= dppFailureStatusHistogramBucketArr2.length) {
                        break;
                    }
                    DppFailureStatusHistogramBucket element2 = dppFailureStatusHistogramBucketArr2[i5];
                    if (element2 != null) {
                        output.writeMessage(5, element2);
                    }
                    i5++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.dppOperationTime;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i6 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.dppOperationTime;
                    if (i6 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element3 = histogramBucketInt32Arr2[i6];
                    if (element3 != null) {
                        output.writeMessage(7, element3);
                    }
                    i6++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numDppConfiguratorInitiatorRequests;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numDppEnrolleeInitiatorRequests;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.numDppEnrolleeSuccess;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessStatusHistogramBucketArr = this.dppConfiguratorSuccessCode;
            if (dppConfiguratorSuccessStatusHistogramBucketArr != null && dppConfiguratorSuccessStatusHistogramBucketArr.length > 0) {
                int i4 = 0;
                while (true) {
                    DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessStatusHistogramBucketArr2 = this.dppConfiguratorSuccessCode;
                    if (i4 >= dppConfiguratorSuccessStatusHistogramBucketArr2.length) {
                        break;
                    }
                    DppConfiguratorSuccessStatusHistogramBucket element = dppConfiguratorSuccessStatusHistogramBucketArr2[i4];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element);
                    }
                    i4++;
                }
            }
            DppFailureStatusHistogramBucket[] dppFailureStatusHistogramBucketArr = this.dppFailureCode;
            if (dppFailureStatusHistogramBucketArr != null && dppFailureStatusHistogramBucketArr.length > 0) {
                int i5 = 0;
                while (true) {
                    DppFailureStatusHistogramBucket[] dppFailureStatusHistogramBucketArr2 = this.dppFailureCode;
                    if (i5 >= dppFailureStatusHistogramBucketArr2.length) {
                        break;
                    }
                    DppFailureStatusHistogramBucket element2 = dppFailureStatusHistogramBucketArr2[i5];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(5, element2);
                    }
                    i5++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.dppOperationTime;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i6 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.dppOperationTime;
                    if (i6 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element3 = histogramBucketInt32Arr2[i6];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(7, element3);
                    }
                    i6++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiDppLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numDppConfiguratorInitiatorRequests = input.readInt32();
                } else if (tag == 16) {
                    this.numDppEnrolleeInitiatorRequests = input.readInt32();
                } else if (tag == 24) {
                    this.numDppEnrolleeSuccess = input.readInt32();
                } else if (tag == 34) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    DppConfiguratorSuccessStatusHistogramBucket[] dppConfiguratorSuccessStatusHistogramBucketArr = this.dppConfiguratorSuccessCode;
                    int i = dppConfiguratorSuccessStatusHistogramBucketArr == null ? 0 : dppConfiguratorSuccessStatusHistogramBucketArr.length;
                    DppConfiguratorSuccessStatusHistogramBucket[] newArray = new DppConfiguratorSuccessStatusHistogramBucket[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.dppConfiguratorSuccessCode, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new DppConfiguratorSuccessStatusHistogramBucket();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new DppConfiguratorSuccessStatusHistogramBucket();
                    input.readMessage(newArray[i]);
                    this.dppConfiguratorSuccessCode = newArray;
                } else if (tag == 42) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    DppFailureStatusHistogramBucket[] dppFailureStatusHistogramBucketArr = this.dppFailureCode;
                    int i2 = dppFailureStatusHistogramBucketArr == null ? 0 : dppFailureStatusHistogramBucketArr.length;
                    DppFailureStatusHistogramBucket[] newArray2 = new DppFailureStatusHistogramBucket[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.dppFailureCode, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = new DppFailureStatusHistogramBucket();
                        input.readMessage(newArray2[i2]);
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = new DppFailureStatusHistogramBucket();
                    input.readMessage(newArray2[i2]);
                    this.dppFailureCode = newArray2;
                } else if (tag == 58) {
                    int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                    HistogramBucketInt32[] histogramBucketInt32Arr = this.dppOperationTime;
                    int i3 = histogramBucketInt32Arr == null ? 0 : histogramBucketInt32Arr.length;
                    HistogramBucketInt32[] newArray3 = new HistogramBucketInt32[(i3 + arrayLength3)];
                    if (i3 != 0) {
                        System.arraycopy(this.dppOperationTime, 0, newArray3, 0, i3);
                    }
                    while (i3 < newArray3.length - 1) {
                        newArray3[i3] = new HistogramBucketInt32();
                        input.readMessage(newArray3[i3]);
                        input.readTag();
                        i3++;
                    }
                    newArray3[i3] = new HistogramBucketInt32();
                    input.readMessage(newArray3[i3]);
                    this.dppOperationTime = newArray3;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiDppLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiDppLog) MessageNano.mergeFrom(new WifiDppLog(), data);
        }

        public static WifiDppLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiDppLog().mergeFrom(input);
        }
    }

    public static final class WifiConfigStoreIO extends MessageNano {
        private static volatile WifiConfigStoreIO[] _emptyArray;
        public DurationBucket[] readDurations;
        public DurationBucket[] writeDurations;

        public static final class DurationBucket extends MessageNano {
            private static volatile DurationBucket[] _emptyArray;
            public int count;
            public int rangeEndMs;
            public int rangeStartMs;

            public static DurationBucket[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new DurationBucket[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public DurationBucket() {
                clear();
            }

            public DurationBucket clear() {
                this.rangeStartMs = 0;
                this.rangeEndMs = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.rangeStartMs;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.rangeEndMs;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                int i3 = this.count;
                if (i3 != 0) {
                    output.writeInt32(3, i3);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.rangeStartMs;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.rangeEndMs;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                int i3 = this.count;
                if (i3 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(3, i3);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public DurationBucket mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.rangeStartMs = input.readInt32();
                    } else if (tag == 16) {
                        this.rangeEndMs = input.readInt32();
                    } else if (tag == 24) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static DurationBucket parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (DurationBucket) MessageNano.mergeFrom(new DurationBucket(), data);
            }

            public static DurationBucket parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new DurationBucket().mergeFrom(input);
            }
        }

        public static WifiConfigStoreIO[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiConfigStoreIO[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiConfigStoreIO() {
            clear();
        }

        public WifiConfigStoreIO clear() {
            this.readDurations = DurationBucket.emptyArray();
            this.writeDurations = DurationBucket.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            DurationBucket[] durationBucketArr = this.readDurations;
            if (durationBucketArr != null && durationBucketArr.length > 0) {
                int i = 0;
                while (true) {
                    DurationBucket[] durationBucketArr2 = this.readDurations;
                    if (i >= durationBucketArr2.length) {
                        break;
                    }
                    DurationBucket element = durationBucketArr2[i];
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                    i++;
                }
            }
            DurationBucket[] durationBucketArr3 = this.writeDurations;
            if (durationBucketArr3 != null && durationBucketArr3.length > 0) {
                int i2 = 0;
                while (true) {
                    DurationBucket[] durationBucketArr4 = this.writeDurations;
                    if (i2 >= durationBucketArr4.length) {
                        break;
                    }
                    DurationBucket element2 = durationBucketArr4[i2];
                    if (element2 != null) {
                        output.writeMessage(2, element2);
                    }
                    i2++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            DurationBucket[] durationBucketArr = this.readDurations;
            if (durationBucketArr != null && durationBucketArr.length > 0) {
                int i = 0;
                while (true) {
                    DurationBucket[] durationBucketArr2 = this.readDurations;
                    if (i >= durationBucketArr2.length) {
                        break;
                    }
                    DurationBucket element = durationBucketArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                    i++;
                }
            }
            DurationBucket[] durationBucketArr3 = this.writeDurations;
            if (durationBucketArr3 != null && durationBucketArr3.length > 0) {
                int i2 = 0;
                while (true) {
                    DurationBucket[] durationBucketArr4 = this.writeDurations;
                    if (i2 >= durationBucketArr4.length) {
                        break;
                    }
                    DurationBucket element2 = durationBucketArr4[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                    }
                    i2++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiConfigStoreIO mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    DurationBucket[] durationBucketArr = this.readDurations;
                    int i = durationBucketArr == null ? 0 : durationBucketArr.length;
                    DurationBucket[] newArray = new DurationBucket[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.readDurations, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new DurationBucket();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new DurationBucket();
                    input.readMessage(newArray[i]);
                    this.readDurations = newArray;
                } else if (tag == 18) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    DurationBucket[] durationBucketArr2 = this.writeDurations;
                    int i2 = durationBucketArr2 == null ? 0 : durationBucketArr2.length;
                    DurationBucket[] newArray2 = new DurationBucket[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.writeDurations, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = new DurationBucket();
                        input.readMessage(newArray2[i2]);
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = new DurationBucket();
                    input.readMessage(newArray2[i2]);
                    this.writeDurations = newArray2;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiConfigStoreIO parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiConfigStoreIO) MessageNano.mergeFrom(new WifiConfigStoreIO(), data);
        }

        public static WifiConfigStoreIO parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiConfigStoreIO().mergeFrom(input);
        }
    }

    public static final class HistogramBucketInt32 extends MessageNano {
        private static volatile HistogramBucketInt32[] _emptyArray;
        public int count;
        public int end;
        public int start;

        public static HistogramBucketInt32[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new HistogramBucketInt32[0];
                    }
                }
            }
            return _emptyArray;
        }

        public HistogramBucketInt32() {
            clear();
        }

        public HistogramBucketInt32 clear() {
            this.start = 0;
            this.end = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.start;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.end;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.count;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.start;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.end;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.count;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public HistogramBucketInt32 mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.start = input.readInt32();
                } else if (tag == 16) {
                    this.end = input.readInt32();
                } else if (tag == 24) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static HistogramBucketInt32 parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (HistogramBucketInt32) MessageNano.mergeFrom(new HistogramBucketInt32(), data);
        }

        public static HistogramBucketInt32 parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new HistogramBucketInt32().mergeFrom(input);
        }
    }

    public static final class Int32Count extends MessageNano {
        private static volatile Int32Count[] _emptyArray;
        public int count;
        public int key;

        public static Int32Count[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Int32Count[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Int32Count() {
            clear();
        }

        public Int32Count clear() {
            this.key = 0;
            this.count = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.key;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.key;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.count;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public Int32Count mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.key = input.readInt32();
                } else if (tag == 16) {
                    this.count = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static Int32Count parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Int32Count) MessageNano.mergeFrom(new Int32Count(), data);
        }

        public static Int32Count parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Int32Count().mergeFrom(input);
        }
    }

    public static final class LinkProbeStats extends MessageNano {
        public static final int LINK_PROBE_FAILURE_REASON_ALREADY_STARTED = 4;
        public static final int LINK_PROBE_FAILURE_REASON_MCS_UNSUPPORTED = 1;
        public static final int LINK_PROBE_FAILURE_REASON_NO_ACK = 2;
        public static final int LINK_PROBE_FAILURE_REASON_TIMEOUT = 3;
        public static final int LINK_PROBE_FAILURE_REASON_UNKNOWN = 0;
        private static volatile LinkProbeStats[] _emptyArray;
        public ExperimentProbeCounts[] experimentProbeCounts;
        public Int32Count[] failureLinkSpeedCounts;
        public LinkProbeFailureReasonCount[] failureReasonCounts;
        public Int32Count[] failureRssiCounts;
        public HistogramBucketInt32[] failureSecondsSinceLastTxSuccessHistogram;
        public HistogramBucketInt32[] successElapsedTimeMsHistogram;
        public Int32Count[] successLinkSpeedCounts;
        public Int32Count[] successRssiCounts;
        public HistogramBucketInt32[] successSecondsSinceLastTxSuccessHistogram;

        public static final class LinkProbeFailureReasonCount extends MessageNano {
            private static volatile LinkProbeFailureReasonCount[] _emptyArray;
            public int count;
            public int failureReason;

            public static LinkProbeFailureReasonCount[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new LinkProbeFailureReasonCount[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public LinkProbeFailureReasonCount() {
                clear();
            }

            public LinkProbeFailureReasonCount clear() {
                this.failureReason = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.failureReason;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.failureReason;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public LinkProbeFailureReasonCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4) {
                            this.failureReason = value;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static LinkProbeFailureReasonCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (LinkProbeFailureReasonCount) MessageNano.mergeFrom(new LinkProbeFailureReasonCount(), data);
            }

            public static LinkProbeFailureReasonCount parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new LinkProbeFailureReasonCount().mergeFrom(input);
            }
        }

        public static final class ExperimentProbeCounts extends MessageNano {
            private static volatile ExperimentProbeCounts[] _emptyArray;
            public String experimentId;
            public int probeCount;

            public static ExperimentProbeCounts[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ExperimentProbeCounts[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ExperimentProbeCounts() {
                clear();
            }

            public ExperimentProbeCounts clear() {
                this.experimentId = "";
                this.probeCount = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (!this.experimentId.equals("")) {
                    output.writeString(1, this.experimentId);
                }
                int i = this.probeCount;
                if (i != 0) {
                    output.writeInt32(2, i);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (!this.experimentId.equals("")) {
                    size += CodedOutputByteBufferNano.computeStringSize(1, this.experimentId);
                }
                int i = this.probeCount;
                if (i != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public ExperimentProbeCounts mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 10) {
                        this.experimentId = input.readString();
                    } else if (tag == 16) {
                        this.probeCount = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static ExperimentProbeCounts parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ExperimentProbeCounts) MessageNano.mergeFrom(new ExperimentProbeCounts(), data);
            }

            public static ExperimentProbeCounts parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ExperimentProbeCounts().mergeFrom(input);
            }
        }

        public static LinkProbeStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new LinkProbeStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public LinkProbeStats() {
            clear();
        }

        public LinkProbeStats clear() {
            this.successRssiCounts = Int32Count.emptyArray();
            this.failureRssiCounts = Int32Count.emptyArray();
            this.successLinkSpeedCounts = Int32Count.emptyArray();
            this.failureLinkSpeedCounts = Int32Count.emptyArray();
            this.successSecondsSinceLastTxSuccessHistogram = HistogramBucketInt32.emptyArray();
            this.failureSecondsSinceLastTxSuccessHistogram = HistogramBucketInt32.emptyArray();
            this.successElapsedTimeMsHistogram = HistogramBucketInt32.emptyArray();
            this.failureReasonCounts = LinkProbeFailureReasonCount.emptyArray();
            this.experimentProbeCounts = ExperimentProbeCounts.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            Int32Count[] int32CountArr = this.successRssiCounts;
            if (int32CountArr != null && int32CountArr.length > 0) {
                int i = 0;
                while (true) {
                    Int32Count[] int32CountArr2 = this.successRssiCounts;
                    if (i >= int32CountArr2.length) {
                        break;
                    }
                    Int32Count element = int32CountArr2[i];
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                    i++;
                }
            }
            Int32Count[] int32CountArr3 = this.failureRssiCounts;
            if (int32CountArr3 != null && int32CountArr3.length > 0) {
                int i2 = 0;
                while (true) {
                    Int32Count[] int32CountArr4 = this.failureRssiCounts;
                    if (i2 >= int32CountArr4.length) {
                        break;
                    }
                    Int32Count element2 = int32CountArr4[i2];
                    if (element2 != null) {
                        output.writeMessage(2, element2);
                    }
                    i2++;
                }
            }
            Int32Count[] int32CountArr5 = this.successLinkSpeedCounts;
            if (int32CountArr5 != null && int32CountArr5.length > 0) {
                int i3 = 0;
                while (true) {
                    Int32Count[] int32CountArr6 = this.successLinkSpeedCounts;
                    if (i3 >= int32CountArr6.length) {
                        break;
                    }
                    Int32Count element3 = int32CountArr6[i3];
                    if (element3 != null) {
                        output.writeMessage(3, element3);
                    }
                    i3++;
                }
            }
            Int32Count[] int32CountArr7 = this.failureLinkSpeedCounts;
            if (int32CountArr7 != null && int32CountArr7.length > 0) {
                int i4 = 0;
                while (true) {
                    Int32Count[] int32CountArr8 = this.failureLinkSpeedCounts;
                    if (i4 >= int32CountArr8.length) {
                        break;
                    }
                    Int32Count element4 = int32CountArr8[i4];
                    if (element4 != null) {
                        output.writeMessage(4, element4);
                    }
                    i4++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.successSecondsSinceLastTxSuccessHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i5 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.successSecondsSinceLastTxSuccessHistogram;
                    if (i5 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element5 = histogramBucketInt32Arr2[i5];
                    if (element5 != null) {
                        output.writeMessage(5, element5);
                    }
                    i5++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr3 = this.failureSecondsSinceLastTxSuccessHistogram;
            if (histogramBucketInt32Arr3 != null && histogramBucketInt32Arr3.length > 0) {
                int i6 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr4 = this.failureSecondsSinceLastTxSuccessHistogram;
                    if (i6 >= histogramBucketInt32Arr4.length) {
                        break;
                    }
                    HistogramBucketInt32 element6 = histogramBucketInt32Arr4[i6];
                    if (element6 != null) {
                        output.writeMessage(6, element6);
                    }
                    i6++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr5 = this.successElapsedTimeMsHistogram;
            if (histogramBucketInt32Arr5 != null && histogramBucketInt32Arr5.length > 0) {
                int i7 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr6 = this.successElapsedTimeMsHistogram;
                    if (i7 >= histogramBucketInt32Arr6.length) {
                        break;
                    }
                    HistogramBucketInt32 element7 = histogramBucketInt32Arr6[i7];
                    if (element7 != null) {
                        output.writeMessage(7, element7);
                    }
                    i7++;
                }
            }
            LinkProbeFailureReasonCount[] linkProbeFailureReasonCountArr = this.failureReasonCounts;
            if (linkProbeFailureReasonCountArr != null && linkProbeFailureReasonCountArr.length > 0) {
                int i8 = 0;
                while (true) {
                    LinkProbeFailureReasonCount[] linkProbeFailureReasonCountArr2 = this.failureReasonCounts;
                    if (i8 >= linkProbeFailureReasonCountArr2.length) {
                        break;
                    }
                    LinkProbeFailureReasonCount element8 = linkProbeFailureReasonCountArr2[i8];
                    if (element8 != null) {
                        output.writeMessage(8, element8);
                    }
                    i8++;
                }
            }
            ExperimentProbeCounts[] experimentProbeCountsArr = this.experimentProbeCounts;
            if (experimentProbeCountsArr != null && experimentProbeCountsArr.length > 0) {
                int i9 = 0;
                while (true) {
                    ExperimentProbeCounts[] experimentProbeCountsArr2 = this.experimentProbeCounts;
                    if (i9 >= experimentProbeCountsArr2.length) {
                        break;
                    }
                    ExperimentProbeCounts element9 = experimentProbeCountsArr2[i9];
                    if (element9 != null) {
                        output.writeMessage(9, element9);
                    }
                    i9++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            Int32Count[] int32CountArr = this.successRssiCounts;
            if (int32CountArr != null && int32CountArr.length > 0) {
                int i = 0;
                while (true) {
                    Int32Count[] int32CountArr2 = this.successRssiCounts;
                    if (i >= int32CountArr2.length) {
                        break;
                    }
                    Int32Count element = int32CountArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                    i++;
                }
            }
            Int32Count[] int32CountArr3 = this.failureRssiCounts;
            if (int32CountArr3 != null && int32CountArr3.length > 0) {
                int i2 = 0;
                while (true) {
                    Int32Count[] int32CountArr4 = this.failureRssiCounts;
                    if (i2 >= int32CountArr4.length) {
                        break;
                    }
                    Int32Count element2 = int32CountArr4[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                    }
                    i2++;
                }
            }
            Int32Count[] int32CountArr5 = this.successLinkSpeedCounts;
            if (int32CountArr5 != null && int32CountArr5.length > 0) {
                int i3 = 0;
                while (true) {
                    Int32Count[] int32CountArr6 = this.successLinkSpeedCounts;
                    if (i3 >= int32CountArr6.length) {
                        break;
                    }
                    Int32Count element3 = int32CountArr6[i3];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element3);
                    }
                    i3++;
                }
            }
            Int32Count[] int32CountArr7 = this.failureLinkSpeedCounts;
            if (int32CountArr7 != null && int32CountArr7.length > 0) {
                int i4 = 0;
                while (true) {
                    Int32Count[] int32CountArr8 = this.failureLinkSpeedCounts;
                    if (i4 >= int32CountArr8.length) {
                        break;
                    }
                    Int32Count element4 = int32CountArr8[i4];
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element4);
                    }
                    i4++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.successSecondsSinceLastTxSuccessHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i5 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.successSecondsSinceLastTxSuccessHistogram;
                    if (i5 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element5 = histogramBucketInt32Arr2[i5];
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(5, element5);
                    }
                    i5++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr3 = this.failureSecondsSinceLastTxSuccessHistogram;
            if (histogramBucketInt32Arr3 != null && histogramBucketInt32Arr3.length > 0) {
                int i6 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr4 = this.failureSecondsSinceLastTxSuccessHistogram;
                    if (i6 >= histogramBucketInt32Arr4.length) {
                        break;
                    }
                    HistogramBucketInt32 element6 = histogramBucketInt32Arr4[i6];
                    if (element6 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(6, element6);
                    }
                    i6++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr5 = this.successElapsedTimeMsHistogram;
            if (histogramBucketInt32Arr5 != null && histogramBucketInt32Arr5.length > 0) {
                int i7 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr6 = this.successElapsedTimeMsHistogram;
                    if (i7 >= histogramBucketInt32Arr6.length) {
                        break;
                    }
                    HistogramBucketInt32 element7 = histogramBucketInt32Arr6[i7];
                    if (element7 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(7, element7);
                    }
                    i7++;
                }
            }
            LinkProbeFailureReasonCount[] linkProbeFailureReasonCountArr = this.failureReasonCounts;
            if (linkProbeFailureReasonCountArr != null && linkProbeFailureReasonCountArr.length > 0) {
                int i8 = 0;
                while (true) {
                    LinkProbeFailureReasonCount[] linkProbeFailureReasonCountArr2 = this.failureReasonCounts;
                    if (i8 >= linkProbeFailureReasonCountArr2.length) {
                        break;
                    }
                    LinkProbeFailureReasonCount element8 = linkProbeFailureReasonCountArr2[i8];
                    if (element8 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(8, element8);
                    }
                    i8++;
                }
            }
            ExperimentProbeCounts[] experimentProbeCountsArr = this.experimentProbeCounts;
            if (experimentProbeCountsArr != null && experimentProbeCountsArr.length > 0) {
                int i9 = 0;
                while (true) {
                    ExperimentProbeCounts[] experimentProbeCountsArr2 = this.experimentProbeCounts;
                    if (i9 >= experimentProbeCountsArr2.length) {
                        break;
                    }
                    ExperimentProbeCounts element9 = experimentProbeCountsArr2[i9];
                    if (element9 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(9, element9);
                    }
                    i9++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public LinkProbeStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    Int32Count[] int32CountArr = this.successRssiCounts;
                    int i = int32CountArr == null ? 0 : int32CountArr.length;
                    Int32Count[] newArray = new Int32Count[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.successRssiCounts, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new Int32Count();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new Int32Count();
                    input.readMessage(newArray[i]);
                    this.successRssiCounts = newArray;
                } else if (tag == 18) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    Int32Count[] int32CountArr2 = this.failureRssiCounts;
                    int i2 = int32CountArr2 == null ? 0 : int32CountArr2.length;
                    Int32Count[] newArray2 = new Int32Count[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.failureRssiCounts, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = new Int32Count();
                        input.readMessage(newArray2[i2]);
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = new Int32Count();
                    input.readMessage(newArray2[i2]);
                    this.failureRssiCounts = newArray2;
                } else if (tag == 26) {
                    int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    Int32Count[] int32CountArr3 = this.successLinkSpeedCounts;
                    int i3 = int32CountArr3 == null ? 0 : int32CountArr3.length;
                    Int32Count[] newArray3 = new Int32Count[(i3 + arrayLength3)];
                    if (i3 != 0) {
                        System.arraycopy(this.successLinkSpeedCounts, 0, newArray3, 0, i3);
                    }
                    while (i3 < newArray3.length - 1) {
                        newArray3[i3] = new Int32Count();
                        input.readMessage(newArray3[i3]);
                        input.readTag();
                        i3++;
                    }
                    newArray3[i3] = new Int32Count();
                    input.readMessage(newArray3[i3]);
                    this.successLinkSpeedCounts = newArray3;
                } else if (tag == 34) {
                    int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    Int32Count[] int32CountArr4 = this.failureLinkSpeedCounts;
                    int i4 = int32CountArr4 == null ? 0 : int32CountArr4.length;
                    Int32Count[] newArray4 = new Int32Count[(i4 + arrayLength4)];
                    if (i4 != 0) {
                        System.arraycopy(this.failureLinkSpeedCounts, 0, newArray4, 0, i4);
                    }
                    while (i4 < newArray4.length - 1) {
                        newArray4[i4] = new Int32Count();
                        input.readMessage(newArray4[i4]);
                        input.readTag();
                        i4++;
                    }
                    newArray4[i4] = new Int32Count();
                    input.readMessage(newArray4[i4]);
                    this.failureLinkSpeedCounts = newArray4;
                } else if (tag == 42) {
                    int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    HistogramBucketInt32[] histogramBucketInt32Arr = this.successSecondsSinceLastTxSuccessHistogram;
                    int i5 = histogramBucketInt32Arr == null ? 0 : histogramBucketInt32Arr.length;
                    HistogramBucketInt32[] newArray5 = new HistogramBucketInt32[(i5 + arrayLength5)];
                    if (i5 != 0) {
                        System.arraycopy(this.successSecondsSinceLastTxSuccessHistogram, 0, newArray5, 0, i5);
                    }
                    while (i5 < newArray5.length - 1) {
                        newArray5[i5] = new HistogramBucketInt32();
                        input.readMessage(newArray5[i5]);
                        input.readTag();
                        i5++;
                    }
                    newArray5[i5] = new HistogramBucketInt32();
                    input.readMessage(newArray5[i5]);
                    this.successSecondsSinceLastTxSuccessHistogram = newArray5;
                } else if (tag == 50) {
                    int arrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.failureSecondsSinceLastTxSuccessHistogram;
                    int i6 = histogramBucketInt32Arr2 == null ? 0 : histogramBucketInt32Arr2.length;
                    HistogramBucketInt32[] newArray6 = new HistogramBucketInt32[(i6 + arrayLength6)];
                    if (i6 != 0) {
                        System.arraycopy(this.failureSecondsSinceLastTxSuccessHistogram, 0, newArray6, 0, i6);
                    }
                    while (i6 < newArray6.length - 1) {
                        newArray6[i6] = new HistogramBucketInt32();
                        input.readMessage(newArray6[i6]);
                        input.readTag();
                        i6++;
                    }
                    newArray6[i6] = new HistogramBucketInt32();
                    input.readMessage(newArray6[i6]);
                    this.failureSecondsSinceLastTxSuccessHistogram = newArray6;
                } else if (tag == 58) {
                    int arrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                    HistogramBucketInt32[] histogramBucketInt32Arr3 = this.successElapsedTimeMsHistogram;
                    int i7 = histogramBucketInt32Arr3 == null ? 0 : histogramBucketInt32Arr3.length;
                    HistogramBucketInt32[] newArray7 = new HistogramBucketInt32[(i7 + arrayLength7)];
                    if (i7 != 0) {
                        System.arraycopy(this.successElapsedTimeMsHistogram, 0, newArray7, 0, i7);
                    }
                    while (i7 < newArray7.length - 1) {
                        newArray7[i7] = new HistogramBucketInt32();
                        input.readMessage(newArray7[i7]);
                        input.readTag();
                        i7++;
                    }
                    newArray7[i7] = new HistogramBucketInt32();
                    input.readMessage(newArray7[i7]);
                    this.successElapsedTimeMsHistogram = newArray7;
                } else if (tag == 66) {
                    int arrayLength8 = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                    LinkProbeFailureReasonCount[] linkProbeFailureReasonCountArr = this.failureReasonCounts;
                    int i8 = linkProbeFailureReasonCountArr == null ? 0 : linkProbeFailureReasonCountArr.length;
                    LinkProbeFailureReasonCount[] newArray8 = new LinkProbeFailureReasonCount[(i8 + arrayLength8)];
                    if (i8 != 0) {
                        System.arraycopy(this.failureReasonCounts, 0, newArray8, 0, i8);
                    }
                    while (i8 < newArray8.length - 1) {
                        newArray8[i8] = new LinkProbeFailureReasonCount();
                        input.readMessage(newArray8[i8]);
                        input.readTag();
                        i8++;
                    }
                    newArray8[i8] = new LinkProbeFailureReasonCount();
                    input.readMessage(newArray8[i8]);
                    this.failureReasonCounts = newArray8;
                } else if (tag == 74) {
                    int arrayLength9 = WireFormatNano.getRepeatedFieldArrayLength(input, 74);
                    ExperimentProbeCounts[] experimentProbeCountsArr = this.experimentProbeCounts;
                    int i9 = experimentProbeCountsArr == null ? 0 : experimentProbeCountsArr.length;
                    ExperimentProbeCounts[] newArray9 = new ExperimentProbeCounts[(i9 + arrayLength9)];
                    if (i9 != 0) {
                        System.arraycopy(this.experimentProbeCounts, 0, newArray9, 0, i9);
                    }
                    while (i9 < newArray9.length - 1) {
                        newArray9[i9] = new ExperimentProbeCounts();
                        input.readMessage(newArray9[i9]);
                        input.readTag();
                        i9++;
                    }
                    newArray9[i9] = new ExperimentProbeCounts();
                    input.readMessage(newArray9[i9]);
                    this.experimentProbeCounts = newArray9;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static LinkProbeStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (LinkProbeStats) MessageNano.mergeFrom(new LinkProbeStats(), data);
        }

        public static LinkProbeStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new LinkProbeStats().mergeFrom(input);
        }
    }

    public static final class NetworkSelectionExperimentDecisions extends MessageNano {
        private static volatile NetworkSelectionExperimentDecisions[] _emptyArray;
        public Int32Count[] differentSelectionNumChoicesCounter;
        public int experiment1Id;
        public int experiment2Id;
        public Int32Count[] sameSelectionNumChoicesCounter;

        public static NetworkSelectionExperimentDecisions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NetworkSelectionExperimentDecisions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NetworkSelectionExperimentDecisions() {
            clear();
        }

        public NetworkSelectionExperimentDecisions clear() {
            this.experiment1Id = 0;
            this.experiment2Id = 0;
            this.sameSelectionNumChoicesCounter = Int32Count.emptyArray();
            this.differentSelectionNumChoicesCounter = Int32Count.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.experiment1Id;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.experiment2Id;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            Int32Count[] int32CountArr = this.sameSelectionNumChoicesCounter;
            if (int32CountArr != null && int32CountArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Int32Count[] int32CountArr2 = this.sameSelectionNumChoicesCounter;
                    if (i3 >= int32CountArr2.length) {
                        break;
                    }
                    Int32Count element = int32CountArr2[i3];
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                    i3++;
                }
            }
            Int32Count[] int32CountArr3 = this.differentSelectionNumChoicesCounter;
            if (int32CountArr3 != null && int32CountArr3.length > 0) {
                int i4 = 0;
                while (true) {
                    Int32Count[] int32CountArr4 = this.differentSelectionNumChoicesCounter;
                    if (i4 >= int32CountArr4.length) {
                        break;
                    }
                    Int32Count element2 = int32CountArr4[i4];
                    if (element2 != null) {
                        output.writeMessage(4, element2);
                    }
                    i4++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.experiment1Id;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.experiment2Id;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            Int32Count[] int32CountArr = this.sameSelectionNumChoicesCounter;
            if (int32CountArr != null && int32CountArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Int32Count[] int32CountArr2 = this.sameSelectionNumChoicesCounter;
                    if (i3 >= int32CountArr2.length) {
                        break;
                    }
                    Int32Count element = int32CountArr2[i3];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                    i3++;
                }
            }
            Int32Count[] int32CountArr3 = this.differentSelectionNumChoicesCounter;
            if (int32CountArr3 != null && int32CountArr3.length > 0) {
                int i4 = 0;
                while (true) {
                    Int32Count[] int32CountArr4 = this.differentSelectionNumChoicesCounter;
                    if (i4 >= int32CountArr4.length) {
                        break;
                    }
                    Int32Count element2 = int32CountArr4[i4];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                    i4++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public NetworkSelectionExperimentDecisions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.experiment1Id = input.readInt32();
                } else if (tag == 16) {
                    this.experiment2Id = input.readInt32();
                } else if (tag == 26) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    Int32Count[] int32CountArr = this.sameSelectionNumChoicesCounter;
                    int i = int32CountArr == null ? 0 : int32CountArr.length;
                    Int32Count[] newArray = new Int32Count[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.sameSelectionNumChoicesCounter, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new Int32Count();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new Int32Count();
                    input.readMessage(newArray[i]);
                    this.sameSelectionNumChoicesCounter = newArray;
                } else if (tag == 34) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    Int32Count[] int32CountArr2 = this.differentSelectionNumChoicesCounter;
                    int i2 = int32CountArr2 == null ? 0 : int32CountArr2.length;
                    Int32Count[] newArray2 = new Int32Count[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.differentSelectionNumChoicesCounter, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = new Int32Count();
                        input.readMessage(newArray2[i2]);
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = new Int32Count();
                    input.readMessage(newArray2[i2]);
                    this.differentSelectionNumChoicesCounter = newArray2;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static NetworkSelectionExperimentDecisions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (NetworkSelectionExperimentDecisions) MessageNano.mergeFrom(new NetworkSelectionExperimentDecisions(), data);
        }

        public static NetworkSelectionExperimentDecisions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkSelectionExperimentDecisions().mergeFrom(input);
        }
    }

    public static final class WifiNetworkRequestApiLog extends MessageNano {
        private static volatile WifiNetworkRequestApiLog[] _emptyArray;
        public HistogramBucketInt32[] networkMatchSizeHistogram;
        public int numApps;
        public int numConnectSuccess;
        public int numRequest;
        public int numUserApprovalBypass;
        public int numUserReject;

        public static WifiNetworkRequestApiLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiNetworkRequestApiLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiNetworkRequestApiLog() {
            clear();
        }

        public WifiNetworkRequestApiLog clear() {
            this.numRequest = 0;
            this.networkMatchSizeHistogram = HistogramBucketInt32.emptyArray();
            this.numConnectSuccess = 0;
            this.numUserApprovalBypass = 0;
            this.numUserReject = 0;
            this.numApps = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numRequest;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.networkMatchSizeHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i2 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.networkMatchSizeHistogram;
                    if (i2 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element = histogramBucketInt32Arr2[i2];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i2++;
                }
            }
            int i3 = this.numConnectSuccess;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numUserApprovalBypass;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.numUserReject;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            int i6 = this.numApps;
            if (i6 != 0) {
                output.writeInt32(6, i6);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numRequest;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.networkMatchSizeHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i2 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.networkMatchSizeHistogram;
                    if (i2 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element = histogramBucketInt32Arr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i2++;
                }
            }
            int i3 = this.numConnectSuccess;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numUserApprovalBypass;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.numUserReject;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            int i6 = this.numApps;
            if (i6 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(6, i6);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiNetworkRequestApiLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numRequest = input.readInt32();
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    HistogramBucketInt32[] histogramBucketInt32Arr = this.networkMatchSizeHistogram;
                    int i = histogramBucketInt32Arr == null ? 0 : histogramBucketInt32Arr.length;
                    HistogramBucketInt32[] newArray = new HistogramBucketInt32[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.networkMatchSizeHistogram, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new HistogramBucketInt32();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new HistogramBucketInt32();
                    input.readMessage(newArray[i]);
                    this.networkMatchSizeHistogram = newArray;
                } else if (tag == 24) {
                    this.numConnectSuccess = input.readInt32();
                } else if (tag == 32) {
                    this.numUserApprovalBypass = input.readInt32();
                } else if (tag == 40) {
                    this.numUserReject = input.readInt32();
                } else if (tag == 48) {
                    this.numApps = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiNetworkRequestApiLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiNetworkRequestApiLog) MessageNano.mergeFrom(new WifiNetworkRequestApiLog(), data);
        }

        public static WifiNetworkRequestApiLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiNetworkRequestApiLog().mergeFrom(input);
        }
    }

    public static final class WifiNetworkSuggestionApiLog extends MessageNano {
        private static volatile WifiNetworkSuggestionApiLog[] _emptyArray;
        public HistogramBucketInt32[] networkListSizeHistogram;
        public int numConnectFailure;
        public int numConnectSuccess;
        public int numModification;

        public static WifiNetworkSuggestionApiLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiNetworkSuggestionApiLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiNetworkSuggestionApiLog() {
            clear();
        }

        public WifiNetworkSuggestionApiLog clear() {
            this.numModification = 0;
            this.numConnectSuccess = 0;
            this.numConnectFailure = 0;
            this.networkListSizeHistogram = HistogramBucketInt32.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numModification;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numConnectSuccess;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.numConnectFailure;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.networkListSizeHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i4 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.networkListSizeHistogram;
                    if (i4 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element = histogramBucketInt32Arr2[i4];
                    if (element != null) {
                        output.writeMessage(4, element);
                    }
                    i4++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numModification;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numConnectSuccess;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.numConnectFailure;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.networkListSizeHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i4 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.networkListSizeHistogram;
                    if (i4 >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element = histogramBucketInt32Arr2[i4];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element);
                    }
                    i4++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiNetworkSuggestionApiLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numModification = input.readInt32();
                } else if (tag == 16) {
                    this.numConnectSuccess = input.readInt32();
                } else if (tag == 24) {
                    this.numConnectFailure = input.readInt32();
                } else if (tag == 34) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    HistogramBucketInt32[] histogramBucketInt32Arr = this.networkListSizeHistogram;
                    int i = histogramBucketInt32Arr == null ? 0 : histogramBucketInt32Arr.length;
                    HistogramBucketInt32[] newArray = new HistogramBucketInt32[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.networkListSizeHistogram, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new HistogramBucketInt32();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new HistogramBucketInt32();
                    input.readMessage(newArray[i]);
                    this.networkListSizeHistogram = newArray;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiNetworkSuggestionApiLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiNetworkSuggestionApiLog) MessageNano.mergeFrom(new WifiNetworkSuggestionApiLog(), data);
        }

        public static WifiNetworkSuggestionApiLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiNetworkSuggestionApiLog().mergeFrom(input);
        }
    }

    public static final class WifiLockStats extends MessageNano {
        private static volatile WifiLockStats[] _emptyArray;
        public HistogramBucketInt32[] highPerfActiveSessionDurationSecHistogram;
        public long highPerfActiveTimeMs;
        public HistogramBucketInt32[] highPerfLockAcqDurationSecHistogram;
        public HistogramBucketInt32[] lowLatencyActiveSessionDurationSecHistogram;
        public long lowLatencyActiveTimeMs;
        public HistogramBucketInt32[] lowLatencyLockAcqDurationSecHistogram;

        public static WifiLockStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiLockStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiLockStats() {
            clear();
        }

        public WifiLockStats clear() {
            this.highPerfActiveTimeMs = 0;
            this.lowLatencyActiveTimeMs = 0;
            this.highPerfLockAcqDurationSecHistogram = HistogramBucketInt32.emptyArray();
            this.lowLatencyLockAcqDurationSecHistogram = HistogramBucketInt32.emptyArray();
            this.highPerfActiveSessionDurationSecHistogram = HistogramBucketInt32.emptyArray();
            this.lowLatencyActiveSessionDurationSecHistogram = HistogramBucketInt32.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.highPerfActiveTimeMs;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            long j2 = this.lowLatencyActiveTimeMs;
            if (j2 != 0) {
                output.writeInt64(2, j2);
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.highPerfLockAcqDurationSecHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.highPerfLockAcqDurationSecHistogram;
                    if (i >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element = histogramBucketInt32Arr2[i];
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                    i++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr3 = this.lowLatencyLockAcqDurationSecHistogram;
            if (histogramBucketInt32Arr3 != null && histogramBucketInt32Arr3.length > 0) {
                int i2 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr4 = this.lowLatencyLockAcqDurationSecHistogram;
                    if (i2 >= histogramBucketInt32Arr4.length) {
                        break;
                    }
                    HistogramBucketInt32 element2 = histogramBucketInt32Arr4[i2];
                    if (element2 != null) {
                        output.writeMessage(4, element2);
                    }
                    i2++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr5 = this.highPerfActiveSessionDurationSecHistogram;
            if (histogramBucketInt32Arr5 != null && histogramBucketInt32Arr5.length > 0) {
                int i3 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr6 = this.highPerfActiveSessionDurationSecHistogram;
                    if (i3 >= histogramBucketInt32Arr6.length) {
                        break;
                    }
                    HistogramBucketInt32 element3 = histogramBucketInt32Arr6[i3];
                    if (element3 != null) {
                        output.writeMessage(5, element3);
                    }
                    i3++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr7 = this.lowLatencyActiveSessionDurationSecHistogram;
            if (histogramBucketInt32Arr7 != null && histogramBucketInt32Arr7.length > 0) {
                int i4 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr8 = this.lowLatencyActiveSessionDurationSecHistogram;
                    if (i4 >= histogramBucketInt32Arr8.length) {
                        break;
                    }
                    HistogramBucketInt32 element4 = histogramBucketInt32Arr8[i4];
                    if (element4 != null) {
                        output.writeMessage(6, element4);
                    }
                    i4++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.highPerfActiveTimeMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            long j2 = this.lowLatencyActiveTimeMs;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, j2);
            }
            HistogramBucketInt32[] histogramBucketInt32Arr = this.highPerfLockAcqDurationSecHistogram;
            if (histogramBucketInt32Arr != null && histogramBucketInt32Arr.length > 0) {
                int i = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.highPerfLockAcqDurationSecHistogram;
                    if (i >= histogramBucketInt32Arr2.length) {
                        break;
                    }
                    HistogramBucketInt32 element = histogramBucketInt32Arr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                    i++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr3 = this.lowLatencyLockAcqDurationSecHistogram;
            if (histogramBucketInt32Arr3 != null && histogramBucketInt32Arr3.length > 0) {
                int i2 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr4 = this.lowLatencyLockAcqDurationSecHistogram;
                    if (i2 >= histogramBucketInt32Arr4.length) {
                        break;
                    }
                    HistogramBucketInt32 element2 = histogramBucketInt32Arr4[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                    i2++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr5 = this.highPerfActiveSessionDurationSecHistogram;
            if (histogramBucketInt32Arr5 != null && histogramBucketInt32Arr5.length > 0) {
                int i3 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr6 = this.highPerfActiveSessionDurationSecHistogram;
                    if (i3 >= histogramBucketInt32Arr6.length) {
                        break;
                    }
                    HistogramBucketInt32 element3 = histogramBucketInt32Arr6[i3];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(5, element3);
                    }
                    i3++;
                }
            }
            HistogramBucketInt32[] histogramBucketInt32Arr7 = this.lowLatencyActiveSessionDurationSecHistogram;
            if (histogramBucketInt32Arr7 != null && histogramBucketInt32Arr7.length > 0) {
                int i4 = 0;
                while (true) {
                    HistogramBucketInt32[] histogramBucketInt32Arr8 = this.lowLatencyActiveSessionDurationSecHistogram;
                    if (i4 >= histogramBucketInt32Arr8.length) {
                        break;
                    }
                    HistogramBucketInt32 element4 = histogramBucketInt32Arr8[i4];
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(6, element4);
                    }
                    i4++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiLockStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.highPerfActiveTimeMs = input.readInt64();
                } else if (tag == 16) {
                    this.lowLatencyActiveTimeMs = input.readInt64();
                } else if (tag == 26) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    HistogramBucketInt32[] histogramBucketInt32Arr = this.highPerfLockAcqDurationSecHistogram;
                    int i = histogramBucketInt32Arr == null ? 0 : histogramBucketInt32Arr.length;
                    HistogramBucketInt32[] newArray = new HistogramBucketInt32[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.highPerfLockAcqDurationSecHistogram, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new HistogramBucketInt32();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new HistogramBucketInt32();
                    input.readMessage(newArray[i]);
                    this.highPerfLockAcqDurationSecHistogram = newArray;
                } else if (tag == 34) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    HistogramBucketInt32[] histogramBucketInt32Arr2 = this.lowLatencyLockAcqDurationSecHistogram;
                    int i2 = histogramBucketInt32Arr2 == null ? 0 : histogramBucketInt32Arr2.length;
                    HistogramBucketInt32[] newArray2 = new HistogramBucketInt32[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.lowLatencyLockAcqDurationSecHistogram, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = new HistogramBucketInt32();
                        input.readMessage(newArray2[i2]);
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = new HistogramBucketInt32();
                    input.readMessage(newArray2[i2]);
                    this.lowLatencyLockAcqDurationSecHistogram = newArray2;
                } else if (tag == 42) {
                    int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    HistogramBucketInt32[] histogramBucketInt32Arr3 = this.highPerfActiveSessionDurationSecHistogram;
                    int i3 = histogramBucketInt32Arr3 == null ? 0 : histogramBucketInt32Arr3.length;
                    HistogramBucketInt32[] newArray3 = new HistogramBucketInt32[(i3 + arrayLength3)];
                    if (i3 != 0) {
                        System.arraycopy(this.highPerfActiveSessionDurationSecHistogram, 0, newArray3, 0, i3);
                    }
                    while (i3 < newArray3.length - 1) {
                        newArray3[i3] = new HistogramBucketInt32();
                        input.readMessage(newArray3[i3]);
                        input.readTag();
                        i3++;
                    }
                    newArray3[i3] = new HistogramBucketInt32();
                    input.readMessage(newArray3[i3]);
                    this.highPerfActiveSessionDurationSecHistogram = newArray3;
                } else if (tag == 50) {
                    int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                    HistogramBucketInt32[] histogramBucketInt32Arr4 = this.lowLatencyActiveSessionDurationSecHistogram;
                    int i4 = histogramBucketInt32Arr4 == null ? 0 : histogramBucketInt32Arr4.length;
                    HistogramBucketInt32[] newArray4 = new HistogramBucketInt32[(i4 + arrayLength4)];
                    if (i4 != 0) {
                        System.arraycopy(this.lowLatencyActiveSessionDurationSecHistogram, 0, newArray4, 0, i4);
                    }
                    while (i4 < newArray4.length - 1) {
                        newArray4[i4] = new HistogramBucketInt32();
                        input.readMessage(newArray4[i4]);
                        input.readTag();
                        i4++;
                    }
                    newArray4[i4] = new HistogramBucketInt32();
                    input.readMessage(newArray4[i4]);
                    this.lowLatencyActiveSessionDurationSecHistogram = newArray4;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiLockStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiLockStats) MessageNano.mergeFrom(new WifiLockStats(), data);
        }

        public static WifiLockStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiLockStats().mergeFrom(input);
        }
    }

    public static final class WifiToggleStats extends MessageNano {
        private static volatile WifiToggleStats[] _emptyArray;
        public int numToggleOffNormal;
        public int numToggleOffPrivileged;
        public int numToggleOnNormal;
        public int numToggleOnPrivileged;

        public static WifiToggleStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WifiToggleStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WifiToggleStats() {
            clear();
        }

        public WifiToggleStats clear() {
            this.numToggleOnPrivileged = 0;
            this.numToggleOffPrivileged = 0;
            this.numToggleOnNormal = 0;
            this.numToggleOffNormal = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numToggleOnPrivileged;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.numToggleOffPrivileged;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.numToggleOnNormal;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.numToggleOffNormal;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numToggleOnPrivileged;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.numToggleOffPrivileged;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.numToggleOnNormal;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.numToggleOffNormal;
            if (i4 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public WifiToggleStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numToggleOnPrivileged = input.readInt32();
                } else if (tag == 16) {
                    this.numToggleOffPrivileged = input.readInt32();
                } else if (tag == 24) {
                    this.numToggleOnNormal = input.readInt32();
                } else if (tag == 32) {
                    this.numToggleOffNormal = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static WifiToggleStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (WifiToggleStats) MessageNano.mergeFrom(new WifiToggleStats(), data);
        }

        public static WifiToggleStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WifiToggleStats().mergeFrom(input);
        }
    }

    public static final class PasspointProvisionStats extends MessageNano {
        public static final int OSU_FAILURE_ADD_PASSPOINT_CONFIGURATION = 22;
        public static final int OSU_FAILURE_AP_CONNECTION = 1;
        public static final int OSU_FAILURE_INVALID_URL_FORMAT_FOR_OSU = 8;
        public static final int OSU_FAILURE_NO_AAA_SERVER_TRUST_ROOT_NODE = 17;
        public static final int OSU_FAILURE_NO_AAA_TRUST_ROOT_CERTIFICATE = 21;
        public static final int OSU_FAILURE_NO_OSU_ACTIVITY_FOUND = 14;
        public static final int OSU_FAILURE_NO_POLICY_SERVER_TRUST_ROOT_NODE = 19;
        public static final int OSU_FAILURE_NO_PPS_MO = 16;
        public static final int OSU_FAILURE_NO_REMEDIATION_SERVER_TRUST_ROOT_NODE = 18;
        public static final int OSU_FAILURE_OSU_PROVIDER_NOT_FOUND = 23;
        public static final int OSU_FAILURE_PROVISIONING_ABORTED = 6;
        public static final int OSU_FAILURE_PROVISIONING_NOT_AVAILABLE = 7;
        public static final int OSU_FAILURE_RETRIEVE_TRUST_ROOT_CERTIFICATES = 20;
        public static final int OSU_FAILURE_SERVER_CONNECTION = 3;
        public static final int OSU_FAILURE_SERVER_URL_INVALID = 2;
        public static final int OSU_FAILURE_SERVER_VALIDATION = 4;
        public static final int OSU_FAILURE_SERVICE_PROVIDER_VERIFICATION = 5;
        public static final int OSU_FAILURE_SOAP_MESSAGE_EXCHANGE = 11;
        public static final int OSU_FAILURE_START_REDIRECT_LISTENER = 12;
        public static final int OSU_FAILURE_TIMED_OUT_REDIRECT_LISTENER = 13;
        public static final int OSU_FAILURE_UNEXPECTED_COMMAND_TYPE = 9;
        public static final int OSU_FAILURE_UNEXPECTED_SOAP_MESSAGE_STATUS = 15;
        public static final int OSU_FAILURE_UNEXPECTED_SOAP_MESSAGE_TYPE = 10;
        public static final int OSU_FAILURE_UNKNOWN = 0;
        private static volatile PasspointProvisionStats[] _emptyArray;
        public int numProvisionSuccess;
        public ProvisionFailureCount[] provisionFailureCount;

        public static final class ProvisionFailureCount extends MessageNano {
            private static volatile ProvisionFailureCount[] _emptyArray;
            public int count;
            public int failureCode;

            public static ProvisionFailureCount[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ProvisionFailureCount[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ProvisionFailureCount() {
                clear();
            }

            public ProvisionFailureCount clear() {
                this.failureCode = 0;
                this.count = 0;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.failureCode;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.framework.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.failureCode;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.count;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.framework.protobuf.nano.MessageNano
            public ProvisionFailureCount mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                                this.failureCode = value;
                                continue;
                        }
                    } else if (tag == 16) {
                        this.count = input.readInt32();
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static ProvisionFailureCount parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ProvisionFailureCount) MessageNano.mergeFrom(new ProvisionFailureCount(), data);
            }

            public static ProvisionFailureCount parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ProvisionFailureCount().mergeFrom(input);
            }
        }

        public static PasspointProvisionStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PasspointProvisionStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PasspointProvisionStats() {
            clear();
        }

        public PasspointProvisionStats clear() {
            this.numProvisionSuccess = 0;
            this.provisionFailureCount = ProvisionFailureCount.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.numProvisionSuccess;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            ProvisionFailureCount[] provisionFailureCountArr = this.provisionFailureCount;
            if (provisionFailureCountArr != null && provisionFailureCountArr.length > 0) {
                int i2 = 0;
                while (true) {
                    ProvisionFailureCount[] provisionFailureCountArr2 = this.provisionFailureCount;
                    if (i2 >= provisionFailureCountArr2.length) {
                        break;
                    }
                    ProvisionFailureCount element = provisionFailureCountArr2[i2];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i2++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.numProvisionSuccess;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            ProvisionFailureCount[] provisionFailureCountArr = this.provisionFailureCount;
            if (provisionFailureCountArr != null && provisionFailureCountArr.length > 0) {
                int i2 = 0;
                while (true) {
                    ProvisionFailureCount[] provisionFailureCountArr2 = this.provisionFailureCount;
                    if (i2 >= provisionFailureCountArr2.length) {
                        break;
                    }
                    ProvisionFailureCount element = provisionFailureCountArr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i2++;
                }
            }
            return size;
        }

        @Override // com.android.framework.protobuf.nano.MessageNano
        public PasspointProvisionStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.numProvisionSuccess = input.readInt32();
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    ProvisionFailureCount[] provisionFailureCountArr = this.provisionFailureCount;
                    int i = provisionFailureCountArr == null ? 0 : provisionFailureCountArr.length;
                    ProvisionFailureCount[] newArray = new ProvisionFailureCount[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.provisionFailureCount, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new ProvisionFailureCount();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new ProvisionFailureCount();
                    input.readMessage(newArray[i]);
                    this.provisionFailureCount = newArray;
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static PasspointProvisionStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (PasspointProvisionStats) MessageNano.mergeFrom(new PasspointProvisionStats(), data);
        }

        public static PasspointProvisionStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new PasspointProvisionStats().mergeFrom(input);
        }
    }
}
