package com.android.server.wifi;

import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.eap.EAP;
import com.android.server.wifi.scanner.BackgroundScanScheduler;
import com.android.server.wifi.scanner.ChannelHelper;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.Extension;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface WifiMetricsProto {

    public static final class ConnectionEvent extends MessageNano {
        public static final int HLF_DHCP = 2;
        public static final int HLF_NONE = 1;
        public static final int HLF_NO_INTERNET = 3;
        public static final int HLF_UNKNOWN = 0;
        public static final int HLF_UNWANTED = 4;
        public static final int ROAM_DBDC = 2;
        public static final int ROAM_ENTERPRISE = 3;
        public static final int ROAM_NONE = 1;
        public static final int ROAM_UNKNOWN = 0;
        public static final int ROAM_UNRELATED = 5;
        public static final int ROAM_USER_SELECTED = 4;
        private static volatile ConnectionEvent[] _emptyArray;
        public boolean automaticBugReportTaken;
        public int connectionResult;
        public int connectivityLevelFailureCode;
        public int durationTakenToConnectMillis;
        public int level2FailureCode;
        public int roamType;
        public RouterFingerPrint routerFingerprint;
        public int signalStrength;
        public long startTimeMillis;

        public static ConnectionEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ConnectionEvent[ROAM_UNKNOWN];
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
            this.durationTakenToConnectMillis = ROAM_UNKNOWN;
            this.routerFingerprint = null;
            this.signalStrength = ROAM_UNKNOWN;
            this.roamType = ROAM_UNKNOWN;
            this.connectionResult = ROAM_UNKNOWN;
            this.level2FailureCode = ROAM_UNKNOWN;
            this.connectivityLevelFailureCode = ROAM_UNKNOWN;
            this.automaticBugReportTaken = false;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.startTimeMillis != 0) {
                output.writeInt64(ROAM_NONE, this.startTimeMillis);
            }
            if (this.durationTakenToConnectMillis != 0) {
                output.writeInt32(ROAM_DBDC, this.durationTakenToConnectMillis);
            }
            if (this.routerFingerprint != null) {
                output.writeMessage(ROAM_ENTERPRISE, this.routerFingerprint);
            }
            if (this.signalStrength != 0) {
                output.writeInt32(ROAM_USER_SELECTED, this.signalStrength);
            }
            if (this.roamType != 0) {
                output.writeInt32(ROAM_UNRELATED, this.roamType);
            }
            if (this.connectionResult != 0) {
                output.writeInt32(6, this.connectionResult);
            }
            if (this.level2FailureCode != 0) {
                output.writeInt32(7, this.level2FailureCode);
            }
            if (this.connectivityLevelFailureCode != 0) {
                output.writeInt32(8, this.connectivityLevelFailureCode);
            }
            if (this.automaticBugReportTaken) {
                output.writeBool(9, this.automaticBugReportTaken);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.startTimeMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(ROAM_NONE, this.startTimeMillis);
            }
            if (this.durationTakenToConnectMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROAM_DBDC, this.durationTakenToConnectMillis);
            }
            if (this.routerFingerprint != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(ROAM_ENTERPRISE, this.routerFingerprint);
            }
            if (this.signalStrength != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROAM_USER_SELECTED, this.signalStrength);
            }
            if (this.roamType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROAM_UNRELATED, this.roamType);
            }
            if (this.connectionResult != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.connectionResult);
            }
            if (this.level2FailureCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.level2FailureCode);
            }
            if (this.connectivityLevelFailureCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, this.connectivityLevelFailureCode);
            }
            if (this.automaticBugReportTaken) {
                return size + CodedOutputByteBufferNano.computeBoolSize(9, this.automaticBugReportTaken);
            }
            return size;
        }

        public ConnectionEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int value;
                switch (tag) {
                    case ROAM_UNKNOWN /*0*/:
                        return this;
                    case Extension.TYPE_BOOL /*8*/:
                        this.startTimeMillis = input.readInt64();
                        break;
                    case Extension.TYPE_SFIXED64 /*16*/:
                        this.durationTakenToConnectMillis = input.readInt32();
                        break;
                    case EAP.EAP_MSCHAPv2 /*26*/:
                        if (this.routerFingerprint == null) {
                            this.routerFingerprint = new RouterFingerPrint();
                        }
                        input.readMessage(this.routerFingerprint);
                        break;
                    case BackgroundScanScheduler.DEFAULT_MAX_AP_PER_SCAN /*32*/:
                        this.signalStrength = input.readInt32();
                        break;
                    case WifiQualifiedNetworkSelector.PASSPOINT_SECURITY_AWARD /*40*/:
                        value = input.readInt32();
                        switch (value) {
                            case ROAM_UNKNOWN /*0*/:
                            case ROAM_NONE /*1*/:
                            case ROAM_DBDC /*2*/:
                            case ROAM_ENTERPRISE /*3*/:
                            case ROAM_USER_SELECTED /*4*/:
                            case ROAM_UNRELATED /*5*/:
                                this.roamType = value;
                                break;
                            default:
                                break;
                        }
                    case EAP.EAP_SAKE /*48*/:
                        this.connectionResult = input.readInt32();
                        break;
                    case 56:
                        this.level2FailureCode = input.readInt32();
                        break;
                    case 64:
                        value = input.readInt32();
                        switch (value) {
                            case ROAM_UNKNOWN /*0*/:
                            case ROAM_NONE /*1*/:
                            case ROAM_DBDC /*2*/:
                            case ROAM_ENTERPRISE /*3*/:
                            case ROAM_USER_SELECTED /*4*/:
                                this.connectivityLevelFailureCode = value;
                                break;
                            default:
                                break;
                        }
                    case 72:
                        this.automaticBugReportTaken = input.readBool();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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
        public int roamType;
        public int routerTechnology;
        public boolean supportsIpv6;

        public static RouterFingerPrint[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RouterFingerPrint[ROAM_TYPE_UNKNOWN];
                    }
                }
            }
            return _emptyArray;
        }

        public RouterFingerPrint() {
            clear();
        }

        public RouterFingerPrint clear() {
            this.roamType = ROAM_TYPE_UNKNOWN;
            this.channelInfo = ROAM_TYPE_UNKNOWN;
            this.dtim = ROAM_TYPE_UNKNOWN;
            this.authentication = ROAM_TYPE_UNKNOWN;
            this.hidden = false;
            this.routerTechnology = ROAM_TYPE_UNKNOWN;
            this.supportsIpv6 = false;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.roamType != 0) {
                output.writeInt32(ROUTER_TECH_A, this.roamType);
            }
            if (this.channelInfo != 0) {
                output.writeInt32(ROUTER_TECH_B, this.channelInfo);
            }
            if (this.dtim != 0) {
                output.writeInt32(ROUTER_TECH_G, this.dtim);
            }
            if (this.authentication != 0) {
                output.writeInt32(ROUTER_TECH_N, this.authentication);
            }
            if (this.hidden) {
                output.writeBool(ROUTER_TECH_AC, this.hidden);
            }
            if (this.routerTechnology != 0) {
                output.writeInt32(ROUTER_TECH_OTHER, this.routerTechnology);
            }
            if (this.supportsIpv6) {
                output.writeBool(7, this.supportsIpv6);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.roamType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROUTER_TECH_A, this.roamType);
            }
            if (this.channelInfo != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROUTER_TECH_B, this.channelInfo);
            }
            if (this.dtim != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROUTER_TECH_G, this.dtim);
            }
            if (this.authentication != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROUTER_TECH_N, this.authentication);
            }
            if (this.hidden) {
                size += CodedOutputByteBufferNano.computeBoolSize(ROUTER_TECH_AC, this.hidden);
            }
            if (this.routerTechnology != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(ROUTER_TECH_OTHER, this.routerTechnology);
            }
            if (this.supportsIpv6) {
                return size + CodedOutputByteBufferNano.computeBoolSize(7, this.supportsIpv6);
            }
            return size;
        }

        public RouterFingerPrint mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int value;
                switch (tag) {
                    case ROAM_TYPE_UNKNOWN /*0*/:
                        return this;
                    case Extension.TYPE_BOOL /*8*/:
                        value = input.readInt32();
                        switch (value) {
                            case ROAM_TYPE_UNKNOWN /*0*/:
                            case ROUTER_TECH_A /*1*/:
                            case ROUTER_TECH_B /*2*/:
                            case ROUTER_TECH_G /*3*/:
                                this.roamType = value;
                                break;
                            default:
                                break;
                        }
                    case Extension.TYPE_SFIXED64 /*16*/:
                        this.channelInfo = input.readInt32();
                        break;
                    case EAP.EAP_3Com /*24*/:
                        this.dtim = input.readInt32();
                        break;
                    case BackgroundScanScheduler.DEFAULT_MAX_AP_PER_SCAN /*32*/:
                        value = input.readInt32();
                        switch (value) {
                            case ROAM_TYPE_UNKNOWN /*0*/:
                            case ROUTER_TECH_A /*1*/:
                            case ROUTER_TECH_B /*2*/:
                            case ROUTER_TECH_G /*3*/:
                                this.authentication = value;
                                break;
                            default:
                                break;
                        }
                    case WifiQualifiedNetworkSelector.PASSPOINT_SECURITY_AWARD /*40*/:
                        this.hidden = input.readBool();
                        break;
                    case EAP.EAP_SAKE /*48*/:
                        value = input.readInt32();
                        switch (value) {
                            case ROAM_TYPE_UNKNOWN /*0*/:
                            case ROUTER_TECH_A /*1*/:
                            case ROUTER_TECH_B /*2*/:
                            case ROUTER_TECH_G /*3*/:
                            case ROUTER_TECH_N /*4*/:
                            case ROUTER_TECH_AC /*5*/:
                            case ROUTER_TECH_OTHER /*6*/:
                                this.routerTechnology = value;
                                break;
                            default:
                                break;
                        }
                    case 56:
                        this.supportsIpv6 = input.readBool();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
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
        public WifiSystemStateEntry[] backgroundScanRequestState;
        public ScanReturnEntry[] backgroundScanReturnEntries;
        public ConnectionEvent[] connectionEvent;
        public boolean isLocationEnabled;
        public boolean isScanningAlwaysEnabled;
        public int numBackgroundScans;
        public int numConnectivityWatchdogBackgroundBad;
        public int numConnectivityWatchdogBackgroundGood;
        public int numConnectivityWatchdogPnoBad;
        public int numConnectivityWatchdogPnoGood;
        public int numEmptyScanResults;
        public int numEnterpriseNetworks;
        public int numLastResortWatchdogAvailableNetworksTotal;
        public int numLastResortWatchdogBadAssociationNetworksTotal;
        public int numLastResortWatchdogBadAuthenticationNetworksTotal;
        public int numLastResortWatchdogBadDhcpNetworksTotal;
        public int numLastResortWatchdogBadOtherNetworksTotal;
        public int numLastResortWatchdogTriggers;
        public int numLastResortWatchdogTriggersWithBadAssociation;
        public int numLastResortWatchdogTriggersWithBadAuthentication;
        public int numLastResortWatchdogTriggersWithBadDhcp;
        public int numLastResortWatchdogTriggersWithBadOther;
        public int numNetworksAddedByApps;
        public int numNetworksAddedByUser;
        public int numNonEmptyScanResults;
        public int numOneshotScans;
        public int numOpenNetworks;
        public int numPersonalNetworks;
        public int numSavedNetworks;
        public int numWifiToggledViaAirplane;
        public int numWifiToggledViaSettings;
        public int recordDurationSec;
        public ScanReturnEntry[] scanReturnEntries;
        public WifiSystemStateEntry[] wifiSystemStateEntries;

        public static final class ScanReturnEntry extends MessageNano {
            private static volatile ScanReturnEntry[] _emptyArray;
            public int scanResultsCount;
            public int scanReturnCode;

            public static ScanReturnEntry[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ScanReturnEntry[WifiLog.SCAN_UNKNOWN];
                        }
                    }
                }
                return _emptyArray;
            }

            public ScanReturnEntry() {
                clear();
            }

            public ScanReturnEntry clear() {
                this.scanReturnCode = WifiLog.SCAN_UNKNOWN;
                this.scanResultsCount = WifiLog.SCAN_UNKNOWN;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.scanReturnCode != 0) {
                    output.writeInt32(WifiLog.WIFI_DISABLED, this.scanReturnCode);
                }
                if (this.scanResultsCount != 0) {
                    output.writeInt32(WifiLog.WIFI_DISCONNECTED, this.scanResultsCount);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.scanReturnCode != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(WifiLog.WIFI_DISABLED, this.scanReturnCode);
                }
                if (this.scanResultsCount != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(WifiLog.WIFI_DISCONNECTED, this.scanResultsCount);
                }
                return size;
            }

            public ScanReturnEntry mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case WifiLog.SCAN_UNKNOWN /*0*/:
                            return this;
                        case Extension.TYPE_BOOL /*8*/:
                            int value = input.readInt32();
                            switch (value) {
                                case WifiLog.SCAN_UNKNOWN /*0*/:
                                case WifiLog.WIFI_DISABLED /*1*/:
                                case WifiLog.WIFI_DISCONNECTED /*2*/:
                                case WifiLog.WIFI_ASSOCIATED /*3*/:
                                case WifiLog.FAILURE_WIFI_DISABLED /*4*/:
                                    this.scanReturnCode = value;
                                    break;
                                default:
                                    break;
                            }
                        case Extension.TYPE_SFIXED64 /*16*/:
                            this.scanResultsCount = input.readInt32();
                            break;
                        default:
                            if (WireFormatNano.parseUnknownField(input, tag)) {
                                break;
                            }
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
                            _emptyArray = new WifiSystemStateEntry[WifiLog.SCAN_UNKNOWN];
                        }
                    }
                }
                return _emptyArray;
            }

            public WifiSystemStateEntry() {
                clear();
            }

            public WifiSystemStateEntry clear() {
                this.wifiState = WifiLog.SCAN_UNKNOWN;
                this.wifiStateCount = WifiLog.SCAN_UNKNOWN;
                this.isScreenOn = false;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.wifiState != 0) {
                    output.writeInt32(WifiLog.WIFI_DISABLED, this.wifiState);
                }
                if (this.wifiStateCount != 0) {
                    output.writeInt32(WifiLog.WIFI_DISCONNECTED, this.wifiStateCount);
                }
                if (this.isScreenOn) {
                    output.writeBool(WifiLog.WIFI_ASSOCIATED, this.isScreenOn);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.wifiState != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(WifiLog.WIFI_DISABLED, this.wifiState);
                }
                if (this.wifiStateCount != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(WifiLog.WIFI_DISCONNECTED, this.wifiStateCount);
                }
                if (this.isScreenOn) {
                    return size + CodedOutputByteBufferNano.computeBoolSize(WifiLog.WIFI_ASSOCIATED, this.isScreenOn);
                }
                return size;
            }

            public WifiSystemStateEntry mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case WifiLog.SCAN_UNKNOWN /*0*/:
                            return this;
                        case Extension.TYPE_BOOL /*8*/:
                            int value = input.readInt32();
                            switch (value) {
                                case WifiLog.SCAN_UNKNOWN /*0*/:
                                case WifiLog.WIFI_DISABLED /*1*/:
                                case WifiLog.WIFI_DISCONNECTED /*2*/:
                                case WifiLog.WIFI_ASSOCIATED /*3*/:
                                    this.wifiState = value;
                                    break;
                                default:
                                    break;
                            }
                        case Extension.TYPE_SFIXED64 /*16*/:
                            this.wifiStateCount = input.readInt32();
                            break;
                        case EAP.EAP_3Com /*24*/:
                            this.isScreenOn = input.readBool();
                            break;
                        default:
                            if (WireFormatNano.parseUnknownField(input, tag)) {
                                break;
                            }
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
                        _emptyArray = new WifiLog[SCAN_UNKNOWN];
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
            this.numSavedNetworks = SCAN_UNKNOWN;
            this.numOpenNetworks = SCAN_UNKNOWN;
            this.numPersonalNetworks = SCAN_UNKNOWN;
            this.numEnterpriseNetworks = SCAN_UNKNOWN;
            this.isLocationEnabled = false;
            this.isScanningAlwaysEnabled = false;
            this.numWifiToggledViaSettings = SCAN_UNKNOWN;
            this.numWifiToggledViaAirplane = SCAN_UNKNOWN;
            this.numNetworksAddedByUser = SCAN_UNKNOWN;
            this.numNetworksAddedByApps = SCAN_UNKNOWN;
            this.numEmptyScanResults = SCAN_UNKNOWN;
            this.numNonEmptyScanResults = SCAN_UNKNOWN;
            this.numOneshotScans = SCAN_UNKNOWN;
            this.numBackgroundScans = SCAN_UNKNOWN;
            this.scanReturnEntries = ScanReturnEntry.emptyArray();
            this.wifiSystemStateEntries = WifiSystemStateEntry.emptyArray();
            this.backgroundScanReturnEntries = ScanReturnEntry.emptyArray();
            this.backgroundScanRequestState = WifiSystemStateEntry.emptyArray();
            this.numLastResortWatchdogTriggers = SCAN_UNKNOWN;
            this.numLastResortWatchdogBadAssociationNetworksTotal = SCAN_UNKNOWN;
            this.numLastResortWatchdogBadAuthenticationNetworksTotal = SCAN_UNKNOWN;
            this.numLastResortWatchdogBadDhcpNetworksTotal = SCAN_UNKNOWN;
            this.numLastResortWatchdogBadOtherNetworksTotal = SCAN_UNKNOWN;
            this.numLastResortWatchdogAvailableNetworksTotal = SCAN_UNKNOWN;
            this.numLastResortWatchdogTriggersWithBadAssociation = SCAN_UNKNOWN;
            this.numLastResortWatchdogTriggersWithBadAuthentication = SCAN_UNKNOWN;
            this.numLastResortWatchdogTriggersWithBadDhcp = SCAN_UNKNOWN;
            this.numLastResortWatchdogTriggersWithBadOther = SCAN_UNKNOWN;
            this.numConnectivityWatchdogPnoGood = SCAN_UNKNOWN;
            this.numConnectivityWatchdogPnoBad = SCAN_UNKNOWN;
            this.numConnectivityWatchdogBackgroundGood = SCAN_UNKNOWN;
            this.numConnectivityWatchdogBackgroundBad = SCAN_UNKNOWN;
            this.recordDurationSec = SCAN_UNKNOWN;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i;
            ScanReturnEntry element;
            WifiSystemStateEntry element2;
            if (this.connectionEvent != null && this.connectionEvent.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.connectionEvent.length; i += WIFI_DISABLED) {
                    ConnectionEvent element3 = this.connectionEvent[i];
                    if (element3 != null) {
                        output.writeMessage(WIFI_DISABLED, element3);
                    }
                }
            }
            if (this.numSavedNetworks != 0) {
                output.writeInt32(WIFI_DISCONNECTED, this.numSavedNetworks);
            }
            if (this.numOpenNetworks != 0) {
                output.writeInt32(WIFI_ASSOCIATED, this.numOpenNetworks);
            }
            if (this.numPersonalNetworks != 0) {
                output.writeInt32(FAILURE_WIFI_DISABLED, this.numPersonalNetworks);
            }
            if (this.numEnterpriseNetworks != 0) {
                output.writeInt32(5, this.numEnterpriseNetworks);
            }
            if (this.isLocationEnabled) {
                output.writeBool(6, this.isLocationEnabled);
            }
            if (this.isScanningAlwaysEnabled) {
                output.writeBool(7, this.isScanningAlwaysEnabled);
            }
            if (this.numWifiToggledViaSettings != 0) {
                output.writeInt32(8, this.numWifiToggledViaSettings);
            }
            if (this.numWifiToggledViaAirplane != 0) {
                output.writeInt32(9, this.numWifiToggledViaAirplane);
            }
            if (this.numNetworksAddedByUser != 0) {
                output.writeInt32(10, this.numNetworksAddedByUser);
            }
            if (this.numNetworksAddedByApps != 0) {
                output.writeInt32(11, this.numNetworksAddedByApps);
            }
            if (this.numEmptyScanResults != 0) {
                output.writeInt32(12, this.numEmptyScanResults);
            }
            if (this.numNonEmptyScanResults != 0) {
                output.writeInt32(13, this.numNonEmptyScanResults);
            }
            if (this.numOneshotScans != 0) {
                output.writeInt32(14, this.numOneshotScans);
            }
            if (this.numBackgroundScans != 0) {
                output.writeInt32(15, this.numBackgroundScans);
            }
            if (this.scanReturnEntries != null && this.scanReturnEntries.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.scanReturnEntries.length; i += WIFI_DISABLED) {
                    element = this.scanReturnEntries[i];
                    if (element != null) {
                        output.writeMessage(16, element);
                    }
                }
            }
            if (this.wifiSystemStateEntries != null && this.wifiSystemStateEntries.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.wifiSystemStateEntries.length; i += WIFI_DISABLED) {
                    element2 = this.wifiSystemStateEntries[i];
                    if (element2 != null) {
                        output.writeMessage(17, element2);
                    }
                }
            }
            if (this.backgroundScanReturnEntries != null && this.backgroundScanReturnEntries.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.backgroundScanReturnEntries.length; i += WIFI_DISABLED) {
                    element = this.backgroundScanReturnEntries[i];
                    if (element != null) {
                        output.writeMessage(18, element);
                    }
                }
            }
            if (this.backgroundScanRequestState != null && this.backgroundScanRequestState.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.backgroundScanRequestState.length; i += WIFI_DISABLED) {
                    element2 = this.backgroundScanRequestState[i];
                    if (element2 != null) {
                        output.writeMessage(19, element2);
                    }
                }
            }
            if (this.numLastResortWatchdogTriggers != 0) {
                output.writeInt32(20, this.numLastResortWatchdogTriggers);
            }
            if (this.numLastResortWatchdogBadAssociationNetworksTotal != 0) {
                output.writeInt32(21, this.numLastResortWatchdogBadAssociationNetworksTotal);
            }
            if (this.numLastResortWatchdogBadAuthenticationNetworksTotal != 0) {
                output.writeInt32(22, this.numLastResortWatchdogBadAuthenticationNetworksTotal);
            }
            if (this.numLastResortWatchdogBadDhcpNetworksTotal != 0) {
                output.writeInt32(23, this.numLastResortWatchdogBadDhcpNetworksTotal);
            }
            if (this.numLastResortWatchdogBadOtherNetworksTotal != 0) {
                output.writeInt32(24, this.numLastResortWatchdogBadOtherNetworksTotal);
            }
            if (this.numLastResortWatchdogAvailableNetworksTotal != 0) {
                output.writeInt32(25, this.numLastResortWatchdogAvailableNetworksTotal);
            }
            if (this.numLastResortWatchdogTriggersWithBadAssociation != 0) {
                output.writeInt32(26, this.numLastResortWatchdogTriggersWithBadAssociation);
            }
            if (this.numLastResortWatchdogTriggersWithBadAuthentication != 0) {
                output.writeInt32(27, this.numLastResortWatchdogTriggersWithBadAuthentication);
            }
            if (this.numLastResortWatchdogTriggersWithBadDhcp != 0) {
                output.writeInt32(28, this.numLastResortWatchdogTriggersWithBadDhcp);
            }
            if (this.numLastResortWatchdogTriggersWithBadOther != 0) {
                output.writeInt32(29, this.numLastResortWatchdogTriggersWithBadOther);
            }
            if (this.numConnectivityWatchdogPnoGood != 0) {
                output.writeInt32(30, this.numConnectivityWatchdogPnoGood);
            }
            if (this.numConnectivityWatchdogPnoBad != 0) {
                output.writeInt32(31, this.numConnectivityWatchdogPnoBad);
            }
            if (this.numConnectivityWatchdogBackgroundGood != 0) {
                output.writeInt32(32, this.numConnectivityWatchdogBackgroundGood);
            }
            if (this.numConnectivityWatchdogBackgroundBad != 0) {
                output.writeInt32(33, this.numConnectivityWatchdogBackgroundBad);
            }
            if (this.recordDurationSec != 0) {
                output.writeInt32(34, this.recordDurationSec);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int i;
            ScanReturnEntry element;
            WifiSystemStateEntry element2;
            int size = super.computeSerializedSize();
            if (this.connectionEvent != null && this.connectionEvent.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.connectionEvent.length; i += WIFI_DISABLED) {
                    ConnectionEvent element3 = this.connectionEvent[i];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(WIFI_DISABLED, element3);
                    }
                }
            }
            if (this.numSavedNetworks != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(WIFI_DISCONNECTED, this.numSavedNetworks);
            }
            if (this.numOpenNetworks != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(WIFI_ASSOCIATED, this.numOpenNetworks);
            }
            if (this.numPersonalNetworks != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(FAILURE_WIFI_DISABLED, this.numPersonalNetworks);
            }
            if (this.numEnterpriseNetworks != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.numEnterpriseNetworks);
            }
            if (this.isLocationEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, this.isLocationEnabled);
            }
            if (this.isScanningAlwaysEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, this.isScanningAlwaysEnabled);
            }
            if (this.numWifiToggledViaSettings != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, this.numWifiToggledViaSettings);
            }
            if (this.numWifiToggledViaAirplane != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.numWifiToggledViaAirplane);
            }
            if (this.numNetworksAddedByUser != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(10, this.numNetworksAddedByUser);
            }
            if (this.numNetworksAddedByApps != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, this.numNetworksAddedByApps);
            }
            if (this.numEmptyScanResults != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(12, this.numEmptyScanResults);
            }
            if (this.numNonEmptyScanResults != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, this.numNonEmptyScanResults);
            }
            if (this.numOneshotScans != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(14, this.numOneshotScans);
            }
            if (this.numBackgroundScans != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(15, this.numBackgroundScans);
            }
            if (this.scanReturnEntries != null && this.scanReturnEntries.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.scanReturnEntries.length; i += WIFI_DISABLED) {
                    element = this.scanReturnEntries[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(16, element);
                    }
                }
            }
            if (this.wifiSystemStateEntries != null && this.wifiSystemStateEntries.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.wifiSystemStateEntries.length; i += WIFI_DISABLED) {
                    element2 = this.wifiSystemStateEntries[i];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(17, element2);
                    }
                }
            }
            if (this.backgroundScanReturnEntries != null && this.backgroundScanReturnEntries.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.backgroundScanReturnEntries.length; i += WIFI_DISABLED) {
                    element = this.backgroundScanReturnEntries[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(18, element);
                    }
                }
            }
            if (this.backgroundScanRequestState != null && this.backgroundScanRequestState.length > 0) {
                for (i = SCAN_UNKNOWN; i < this.backgroundScanRequestState.length; i += WIFI_DISABLED) {
                    element2 = this.backgroundScanRequestState[i];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(19, element2);
                    }
                }
            }
            if (this.numLastResortWatchdogTriggers != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(20, this.numLastResortWatchdogTriggers);
            }
            if (this.numLastResortWatchdogBadAssociationNetworksTotal != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(21, this.numLastResortWatchdogBadAssociationNetworksTotal);
            }
            if (this.numLastResortWatchdogBadAuthenticationNetworksTotal != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(22, this.numLastResortWatchdogBadAuthenticationNetworksTotal);
            }
            if (this.numLastResortWatchdogBadDhcpNetworksTotal != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(23, this.numLastResortWatchdogBadDhcpNetworksTotal);
            }
            if (this.numLastResortWatchdogBadOtherNetworksTotal != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(24, this.numLastResortWatchdogBadOtherNetworksTotal);
            }
            if (this.numLastResortWatchdogAvailableNetworksTotal != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(25, this.numLastResortWatchdogAvailableNetworksTotal);
            }
            if (this.numLastResortWatchdogTriggersWithBadAssociation != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(26, this.numLastResortWatchdogTriggersWithBadAssociation);
            }
            if (this.numLastResortWatchdogTriggersWithBadAuthentication != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(27, this.numLastResortWatchdogTriggersWithBadAuthentication);
            }
            if (this.numLastResortWatchdogTriggersWithBadDhcp != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(28, this.numLastResortWatchdogTriggersWithBadDhcp);
            }
            if (this.numLastResortWatchdogTriggersWithBadOther != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(29, this.numLastResortWatchdogTriggersWithBadOther);
            }
            if (this.numConnectivityWatchdogPnoGood != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(30, this.numConnectivityWatchdogPnoGood);
            }
            if (this.numConnectivityWatchdogPnoBad != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(31, this.numConnectivityWatchdogPnoBad);
            }
            if (this.numConnectivityWatchdogBackgroundGood != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(32, this.numConnectivityWatchdogBackgroundGood);
            }
            if (this.numConnectivityWatchdogBackgroundBad != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(33, this.numConnectivityWatchdogBackgroundBad);
            }
            if (this.recordDurationSec != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(34, this.recordDurationSec);
            }
            return size;
        }

        public WifiLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                ScanReturnEntry[] newArray;
                WifiSystemStateEntry[] newArray2;
                switch (tag) {
                    case SCAN_UNKNOWN /*0*/:
                        return this;
                    case Extension.TYPE_GROUP /*10*/:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        if (this.connectionEvent == null) {
                            i = SCAN_UNKNOWN;
                        } else {
                            i = this.connectionEvent.length;
                        }
                        ConnectionEvent[] newArray3 = new ConnectionEvent[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.connectionEvent, SCAN_UNKNOWN, newArray3, SCAN_UNKNOWN, i);
                        }
                        while (i < newArray3.length - 1) {
                            newArray3[i] = new ConnectionEvent();
                            input.readMessage(newArray3[i]);
                            input.readTag();
                            i += WIFI_DISABLED;
                        }
                        newArray3[i] = new ConnectionEvent();
                        input.readMessage(newArray3[i]);
                        this.connectionEvent = newArray3;
                        break;
                    case Extension.TYPE_SFIXED64 /*16*/:
                        this.numSavedNetworks = input.readInt32();
                        break;
                    case EAP.EAP_3Com /*24*/:
                        this.numOpenNetworks = input.readInt32();
                        break;
                    case BackgroundScanScheduler.DEFAULT_MAX_AP_PER_SCAN /*32*/:
                        this.numPersonalNetworks = input.readInt32();
                        break;
                    case WifiQualifiedNetworkSelector.PASSPOINT_SECURITY_AWARD /*40*/:
                        this.numEnterpriseNetworks = input.readInt32();
                        break;
                    case EAP.EAP_SAKE /*48*/:
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
                    case WifiQualifiedNetworkSelector.SECURITY_AWARD /*80*/:
                        this.numNetworksAddedByUser = input.readInt32();
                        break;
                    case 88:
                        this.numNetworksAddedByApps = input.readInt32();
                        break;
                    case HwWifiCHRConst.WIFI_SCAN_FAILED_EX /*96*/:
                        this.numEmptyScanResults = input.readInt32();
                        break;
                    case HwWifiCHRConst.WIFI_ACCESS_WEB_SLOWLY_EX /*104*/:
                        this.numNonEmptyScanResults = input.readInt32();
                        break;
                    case 112:
                        this.numOneshotScans = input.readInt32();
                        break;
                    case HwWifiCHRConst.WIFI_PORTAL_SAMPLES_COLLECTE /*120*/:
                        this.numBackgroundScans = input.readInt32();
                        break;
                    case 130:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 130);
                        if (this.scanReturnEntries == null) {
                            i = SCAN_UNKNOWN;
                        } else {
                            i = this.scanReturnEntries.length;
                        }
                        newArray = new ScanReturnEntry[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.scanReturnEntries, SCAN_UNKNOWN, newArray, SCAN_UNKNOWN, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new ScanReturnEntry();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i += WIFI_DISABLED;
                        }
                        newArray[i] = new ScanReturnEntry();
                        input.readMessage(newArray[i]);
                        this.scanReturnEntries = newArray;
                        break;
                    case 138:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 138);
                        if (this.wifiSystemStateEntries == null) {
                            i = SCAN_UNKNOWN;
                        } else {
                            i = this.wifiSystemStateEntries.length;
                        }
                        newArray2 = new WifiSystemStateEntry[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.wifiSystemStateEntries, SCAN_UNKNOWN, newArray2, SCAN_UNKNOWN, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new WifiSystemStateEntry();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i += WIFI_DISABLED;
                        }
                        newArray2[i] = new WifiSystemStateEntry();
                        input.readMessage(newArray2[i]);
                        this.wifiSystemStateEntries = newArray2;
                        break;
                    case 146:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 146);
                        if (this.backgroundScanReturnEntries == null) {
                            i = SCAN_UNKNOWN;
                        } else {
                            i = this.backgroundScanReturnEntries.length;
                        }
                        newArray = new ScanReturnEntry[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.backgroundScanReturnEntries, SCAN_UNKNOWN, newArray, SCAN_UNKNOWN, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new ScanReturnEntry();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i += WIFI_DISABLED;
                        }
                        newArray[i] = new ScanReturnEntry();
                        input.readMessage(newArray[i]);
                        this.backgroundScanReturnEntries = newArray;
                        break;
                    case 154:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 154);
                        if (this.backgroundScanRequestState == null) {
                            i = SCAN_UNKNOWN;
                        } else {
                            i = this.backgroundScanRequestState.length;
                        }
                        newArray2 = new WifiSystemStateEntry[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.backgroundScanRequestState, SCAN_UNKNOWN, newArray2, SCAN_UNKNOWN, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new WifiSystemStateEntry();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i += WIFI_DISABLED;
                        }
                        newArray2[i] = new WifiSystemStateEntry();
                        input.readMessage(newArray2[i]);
                        this.backgroundScanRequestState = newArray2;
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
                    case ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS /*200*/:
                        this.numLastResortWatchdogAvailableNetworksTotal = input.readInt32();
                        break;
                    case HwWifiCHRConst.WIFI_DEVICE_ERROR /*208*/:
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
                    case Constants.ANQP_QUERY_LIST /*256*/:
                        this.numConnectivityWatchdogBackgroundGood = input.readInt32();
                        break;
                    case Constants.ANQP_3GPP_NETWORK /*264*/:
                        this.numConnectivityWatchdogBackgroundBad = input.readInt32();
                        break;
                    case Constants.ANQP_NEIGHBOR_REPORT /*272*/:
                        this.recordDurationSec = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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
}
