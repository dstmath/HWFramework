package com.android.server.wifi;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLiteOrBuilder;
import com.google.protobuf.Parser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public final class WifiScoreCardProto {

    public interface AccessPointOrBuilder extends MessageLiteOrBuilder {
        ByteString getBssid();

        Signal getEventStats(int i);

        int getEventStatsCount();

        List<Signal> getEventStatsList();

        int getId();

        Roam getRoams(int i);

        int getRoamsCount();

        List<Roam> getRoamsList();

        SecurityType getSecurityType();

        Technology getTechnology();

        boolean hasBssid();

        boolean hasId();

        boolean hasSecurityType();

        boolean hasTechnology();
    }

    public interface NetworkListOrBuilder extends MessageLiteOrBuilder {
        long getEndTimeMillis();

        Network getNetworks(int i);

        int getNetworksCount();

        List<Network> getNetworksList();

        long getStartTimeMillis();

        boolean hasEndTimeMillis();

        boolean hasStartTimeMillis();
    }

    public interface NetworkOrBuilder extends MessageLiteOrBuilder {
        AccessPoint getAccessPoints(int i);

        int getAccessPointsCount();

        List<AccessPoint> getAccessPointsList();

        int getNetworkAgentId();

        int getNetworkConfigId();

        SecurityType getSecurityType();

        String getSsid();

        ByteString getSsidBytes();

        boolean hasNetworkAgentId();

        boolean hasNetworkConfigId();

        boolean hasSecurityType();

        boolean hasSsid();
    }

    public interface RoamOrBuilder extends MessageLiteOrBuilder {
        int getBad();

        int getGood();

        int getToId();

        boolean hasBad();

        boolean hasGood();

        boolean hasToId();
    }

    public interface SignalOrBuilder extends MessageLiteOrBuilder {
        UnivariateStatistic getElapsedMs();

        Event getEvent();

        int getFrequency();

        UnivariateStatistic getLinkspeed();

        UnivariateStatistic getRssi();

        boolean hasElapsedMs();

        boolean hasEvent();

        boolean hasFrequency();

        boolean hasLinkspeed();

        boolean hasRssi();
    }

    public interface UnivariateStatisticOrBuilder extends MessageLiteOrBuilder {
        long getCount();

        double getHistoricalMean();

        double getHistoricalVariance();

        double getMaxValue();

        double getMinValue();

        double getSum();

        double getSumOfSquares();

        boolean hasCount();

        boolean hasHistoricalMean();

        boolean hasHistoricalVariance();

        boolean hasMaxValue();

        boolean hasMinValue();

        boolean hasSum();

        boolean hasSumOfSquares();
    }

    private WifiScoreCardProto() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }

    public enum SecurityType implements Internal.EnumLite {
        OPEN(0),
        WEP(1),
        PSK(2),
        EAP(3),
        SAE(4),
        EAP_SUITE_B(5),
        OWE(6);
        
        public static final int EAP_SUITE_B_VALUE = 5;
        public static final int EAP_VALUE = 3;
        public static final int OPEN_VALUE = 0;
        public static final int OWE_VALUE = 6;
        public static final int PSK_VALUE = 2;
        public static final int SAE_VALUE = 4;
        public static final int WEP_VALUE = 1;
        private static final Internal.EnumLiteMap<SecurityType> internalValueMap = new Internal.EnumLiteMap<SecurityType>() {
            /* class com.android.server.wifi.WifiScoreCardProto.SecurityType.AnonymousClass1 */

            @Override // com.google.protobuf.Internal.EnumLiteMap
            public SecurityType findValueByNumber(int number) {
                return SecurityType.forNumber(number);
            }
        };
        private final int value;

        @Override // com.google.protobuf.Internal.EnumLite
        public final int getNumber() {
            return this.value;
        }

        @Deprecated
        public static SecurityType valueOf(int value2) {
            return forNumber(value2);
        }

        public static SecurityType forNumber(int value2) {
            switch (value2) {
                case 0:
                    return OPEN;
                case 1:
                    return WEP;
                case 2:
                    return PSK;
                case 3:
                    return EAP;
                case 4:
                    return SAE;
                case 5:
                    return EAP_SUITE_B;
                case 6:
                    return OWE;
                default:
                    return null;
            }
        }

        public static Internal.EnumLiteMap<SecurityType> internalGetValueMap() {
            return internalValueMap;
        }

        private SecurityType(int value2) {
            this.value = value2;
        }
    }

    public enum Technology implements Internal.EnumLite {
        MODE_UNKNOWN(0),
        MODE_11A(1),
        MODE_11B(2),
        MODE_11G(3),
        MODE_11N(4),
        MODE_11AC(5);
        
        public static final int MODE_11AC_VALUE = 5;
        public static final int MODE_11A_VALUE = 1;
        public static final int MODE_11B_VALUE = 2;
        public static final int MODE_11G_VALUE = 3;
        public static final int MODE_11N_VALUE = 4;
        public static final int MODE_UNKNOWN_VALUE = 0;
        private static final Internal.EnumLiteMap<Technology> internalValueMap = new Internal.EnumLiteMap<Technology>() {
            /* class com.android.server.wifi.WifiScoreCardProto.Technology.AnonymousClass1 */

            @Override // com.google.protobuf.Internal.EnumLiteMap
            public Technology findValueByNumber(int number) {
                return Technology.forNumber(number);
            }
        };
        private final int value;

        @Override // com.google.protobuf.Internal.EnumLite
        public final int getNumber() {
            return this.value;
        }

        @Deprecated
        public static Technology valueOf(int value2) {
            return forNumber(value2);
        }

        public static Technology forNumber(int value2) {
            if (value2 == 0) {
                return MODE_UNKNOWN;
            }
            if (value2 == 1) {
                return MODE_11A;
            }
            if (value2 == 2) {
                return MODE_11B;
            }
            if (value2 == 3) {
                return MODE_11G;
            }
            if (value2 == 4) {
                return MODE_11N;
            }
            if (value2 != 5) {
                return null;
            }
            return MODE_11AC;
        }

        public static Internal.EnumLiteMap<Technology> internalGetValueMap() {
            return internalValueMap;
        }

        private Technology(int value2) {
            this.value = value2;
        }
    }

    public enum Event implements Internal.EnumLite {
        SIGNAL_POLL(1),
        SCAN_BEFORE_SUCCESSFUL_CONNECTION(2),
        FIRST_POLL_AFTER_CONNECTION(3),
        IP_CONFIGURATION_SUCCESS(4),
        SCAN_BEFORE_FAILED_CONNECTION(5),
        CONNECTION_FAILURE(6),
        IP_REACHABILITY_LOST(7),
        LAST_POLL_BEFORE_ROAM(8),
        ROAM_SUCCESS(9),
        WIFI_DISABLED(10),
        ROAM_FAILURE(11),
        LAST_POLL_BEFORE_SWITCH(12),
        VALIDATION_SUCCESS(13);
        
        public static final int CONNECTION_FAILURE_VALUE = 6;
        public static final int FIRST_POLL_AFTER_CONNECTION_VALUE = 3;
        public static final int IP_CONFIGURATION_SUCCESS_VALUE = 4;
        public static final int IP_REACHABILITY_LOST_VALUE = 7;
        public static final int LAST_POLL_BEFORE_ROAM_VALUE = 8;
        public static final int LAST_POLL_BEFORE_SWITCH_VALUE = 12;
        public static final int ROAM_FAILURE_VALUE = 11;
        public static final int ROAM_SUCCESS_VALUE = 9;
        public static final int SCAN_BEFORE_FAILED_CONNECTION_VALUE = 5;
        public static final int SCAN_BEFORE_SUCCESSFUL_CONNECTION_VALUE = 2;
        public static final int SIGNAL_POLL_VALUE = 1;
        public static final int VALIDATION_SUCCESS_VALUE = 13;
        public static final int WIFI_DISABLED_VALUE = 10;
        private static final Internal.EnumLiteMap<Event> internalValueMap = new Internal.EnumLiteMap<Event>() {
            /* class com.android.server.wifi.WifiScoreCardProto.Event.AnonymousClass1 */

            @Override // com.google.protobuf.Internal.EnumLiteMap
            public Event findValueByNumber(int number) {
                return Event.forNumber(number);
            }
        };
        private final int value;

        @Override // com.google.protobuf.Internal.EnumLite
        public final int getNumber() {
            return this.value;
        }

        @Deprecated
        public static Event valueOf(int value2) {
            return forNumber(value2);
        }

        public static Event forNumber(int value2) {
            switch (value2) {
                case 1:
                    return SIGNAL_POLL;
                case 2:
                    return SCAN_BEFORE_SUCCESSFUL_CONNECTION;
                case 3:
                    return FIRST_POLL_AFTER_CONNECTION;
                case 4:
                    return IP_CONFIGURATION_SUCCESS;
                case 5:
                    return SCAN_BEFORE_FAILED_CONNECTION;
                case 6:
                    return CONNECTION_FAILURE;
                case 7:
                    return IP_REACHABILITY_LOST;
                case 8:
                    return LAST_POLL_BEFORE_ROAM;
                case 9:
                    return ROAM_SUCCESS;
                case 10:
                    return WIFI_DISABLED;
                case 11:
                    return ROAM_FAILURE;
                case 12:
                    return LAST_POLL_BEFORE_SWITCH;
                case 13:
                    return VALIDATION_SUCCESS;
                default:
                    return null;
            }
        }

        public static Internal.EnumLiteMap<Event> internalGetValueMap() {
            return internalValueMap;
        }

        private Event(int value2) {
            this.value = value2;
        }
    }

    public static final class NetworkList extends GeneratedMessageLite<NetworkList, Builder> implements NetworkListOrBuilder {
        private static final NetworkList DEFAULT_INSTANCE = new NetworkList();
        public static final int END_TIME_MILLIS_FIELD_NUMBER = 2;
        public static final int NETWORKS_FIELD_NUMBER = 3;
        private static volatile Parser<NetworkList> PARSER = null;
        public static final int START_TIME_MILLIS_FIELD_NUMBER = 1;
        private int bitField0_;
        private long endTimeMillis_ = 0;
        private Internal.ProtobufList<Network> networks_ = emptyProtobufList();
        private long startTimeMillis_ = 0;

        private NetworkList() {
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public boolean hasStartTimeMillis() {
            return (this.bitField0_ & 1) == 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public long getStartTimeMillis() {
            return this.startTimeMillis_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setStartTimeMillis(long value) {
            this.bitField0_ |= 1;
            this.startTimeMillis_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearStartTimeMillis() {
            this.bitField0_ &= -2;
            this.startTimeMillis_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public boolean hasEndTimeMillis() {
            return (this.bitField0_ & 2) == 2;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public long getEndTimeMillis() {
            return this.endTimeMillis_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEndTimeMillis(long value) {
            this.bitField0_ |= 2;
            this.endTimeMillis_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearEndTimeMillis() {
            this.bitField0_ &= -3;
            this.endTimeMillis_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public List<Network> getNetworksList() {
            return this.networks_;
        }

        public List<? extends NetworkOrBuilder> getNetworksOrBuilderList() {
            return this.networks_;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public int getNetworksCount() {
            return this.networks_.size();
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
        public Network getNetworks(int index) {
            return this.networks_.get(index);
        }

        public NetworkOrBuilder getNetworksOrBuilder(int index) {
            return this.networks_.get(index);
        }

        private void ensureNetworksIsMutable() {
            if (!this.networks_.isModifiable()) {
                this.networks_ = GeneratedMessageLite.mutableCopy(this.networks_);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setNetworks(int index, Network value) {
            if (value != null) {
                ensureNetworksIsMutable();
                this.networks_.set(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setNetworks(int index, Network.Builder builderForValue) {
            ensureNetworksIsMutable();
            this.networks_.set(index, (Network) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addNetworks(Network value) {
            if (value != null) {
                ensureNetworksIsMutable();
                this.networks_.add(value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addNetworks(int index, Network value) {
            if (value != null) {
                ensureNetworksIsMutable();
                this.networks_.add(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addNetworks(Network.Builder builderForValue) {
            ensureNetworksIsMutable();
            this.networks_.add((Network) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addNetworks(int index, Network.Builder builderForValue) {
            ensureNetworksIsMutable();
            this.networks_.add(index, (Network) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAllNetworks(Iterable<? extends Network> values) {
            ensureNetworksIsMutable();
            AbstractMessageLite.addAll(values, this.networks_);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearNetworks() {
            this.networks_ = emptyProtobufList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeNetworks(int index) {
            ensureNetworksIsMutable();
            this.networks_.remove(index);
        }

        @Override // com.google.protobuf.MessageLite
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) == 1) {
                output.writeInt64(1, this.startTimeMillis_);
            }
            if ((this.bitField0_ & 2) == 2) {
                output.writeInt64(2, this.endTimeMillis_);
            }
            for (int i = 0; i < this.networks_.size(); i++) {
                output.writeMessage(3, this.networks_.get(i));
            }
            this.unknownFields.writeTo(output);
        }

        @Override // com.google.protobuf.MessageLite
        public int getSerializedSize() {
            int size = this.memoizedSerializedSize;
            if (size != -1) {
                return size;
            }
            int size2 = 0;
            if ((this.bitField0_ & 1) == 1) {
                size2 = 0 + CodedOutputStream.computeInt64Size(1, this.startTimeMillis_);
            }
            if ((this.bitField0_ & 2) == 2) {
                size2 += CodedOutputStream.computeInt64Size(2, this.endTimeMillis_);
            }
            for (int i = 0; i < this.networks_.size(); i++) {
                size2 += CodedOutputStream.computeMessageSize(3, this.networks_.get(i));
            }
            int size3 = size2 + this.unknownFields.getSerializedSize();
            this.memoizedSerializedSize = size3;
            return size3;
        }

        public static NetworkList parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static NetworkList parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static NetworkList parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static NetworkList parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static NetworkList parseFrom(InputStream input) throws IOException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static NetworkList parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static NetworkList parseDelimitedFrom(InputStream input) throws IOException {
            return (NetworkList) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static NetworkList parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (NetworkList) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static NetworkList parseFrom(CodedInputStream input) throws IOException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static NetworkList parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (NetworkList) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(NetworkList prototype) {
            return (Builder) ((Builder) DEFAULT_INSTANCE.toBuilder()).mergeFrom((Builder) prototype);
        }

        public static final class Builder extends GeneratedMessageLite.Builder<NetworkList, Builder> implements NetworkListOrBuilder {
            private Builder() {
                super(NetworkList.DEFAULT_INSTANCE);
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public boolean hasStartTimeMillis() {
                return ((NetworkList) this.instance).hasStartTimeMillis();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public long getStartTimeMillis() {
                return ((NetworkList) this.instance).getStartTimeMillis();
            }

            public Builder setStartTimeMillis(long value) {
                copyOnWrite();
                ((NetworkList) this.instance).setStartTimeMillis(value);
                return this;
            }

            public Builder clearStartTimeMillis() {
                copyOnWrite();
                ((NetworkList) this.instance).clearStartTimeMillis();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public boolean hasEndTimeMillis() {
                return ((NetworkList) this.instance).hasEndTimeMillis();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public long getEndTimeMillis() {
                return ((NetworkList) this.instance).getEndTimeMillis();
            }

            public Builder setEndTimeMillis(long value) {
                copyOnWrite();
                ((NetworkList) this.instance).setEndTimeMillis(value);
                return this;
            }

            public Builder clearEndTimeMillis() {
                copyOnWrite();
                ((NetworkList) this.instance).clearEndTimeMillis();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public List<Network> getNetworksList() {
                return Collections.unmodifiableList(((NetworkList) this.instance).getNetworksList());
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public int getNetworksCount() {
                return ((NetworkList) this.instance).getNetworksCount();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkListOrBuilder
            public Network getNetworks(int index) {
                return ((NetworkList) this.instance).getNetworks(index);
            }

            public Builder setNetworks(int index, Network value) {
                copyOnWrite();
                ((NetworkList) this.instance).setNetworks(index, value);
                return this;
            }

            public Builder setNetworks(int index, Network.Builder builderForValue) {
                copyOnWrite();
                ((NetworkList) this.instance).setNetworks(index, builderForValue);
                return this;
            }

            public Builder addNetworks(Network value) {
                copyOnWrite();
                ((NetworkList) this.instance).addNetworks(value);
                return this;
            }

            public Builder addNetworks(int index, Network value) {
                copyOnWrite();
                ((NetworkList) this.instance).addNetworks(index, value);
                return this;
            }

            public Builder addNetworks(Network.Builder builderForValue) {
                copyOnWrite();
                ((NetworkList) this.instance).addNetworks(builderForValue);
                return this;
            }

            public Builder addNetworks(int index, Network.Builder builderForValue) {
                copyOnWrite();
                ((NetworkList) this.instance).addNetworks(index, builderForValue);
                return this;
            }

            public Builder addAllNetworks(Iterable<? extends Network> values) {
                copyOnWrite();
                ((NetworkList) this.instance).addAllNetworks(values);
                return this;
            }

            public Builder clearNetworks() {
                copyOnWrite();
                ((NetworkList) this.instance).clearNetworks();
                return this;
            }

            public Builder removeNetworks(int index) {
                copyOnWrite();
                ((NetworkList) this.instance).removeNetworks(index);
                return this;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke method, Object arg0, Object arg1) {
            switch (method) {
                case NEW_MUTABLE_INSTANCE:
                    return new NetworkList();
                case IS_INITIALIZED:
                    return DEFAULT_INSTANCE;
                case MAKE_IMMUTABLE:
                    this.networks_.makeImmutable();
                    return null;
                case NEW_BUILDER:
                    return new Builder();
                case VISIT:
                    GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) arg0;
                    NetworkList other = (NetworkList) arg1;
                    this.startTimeMillis_ = visitor.visitLong(hasStartTimeMillis(), this.startTimeMillis_, other.hasStartTimeMillis(), other.startTimeMillis_);
                    this.endTimeMillis_ = visitor.visitLong(hasEndTimeMillis(), this.endTimeMillis_, other.hasEndTimeMillis(), other.endTimeMillis_);
                    this.networks_ = visitor.visitList(this.networks_, other.networks_);
                    if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                        this.bitField0_ |= other.bitField0_;
                    }
                    return this;
                case MERGE_FROM_STREAM:
                    CodedInputStream input = (CodedInputStream) arg0;
                    ExtensionRegistryLite extensionRegistry = (ExtensionRegistryLite) arg1;
                    boolean done = false;
                    while (!done) {
                        try {
                            int tag = input.readTag();
                            if (tag == 0) {
                                done = true;
                            } else if (tag == 8) {
                                this.bitField0_ |= 1;
                                this.startTimeMillis_ = input.readInt64();
                            } else if (tag == 16) {
                                this.bitField0_ |= 2;
                                this.endTimeMillis_ = input.readInt64();
                            } else if (tag == 26) {
                                if (!this.networks_.isModifiable()) {
                                    this.networks_ = GeneratedMessageLite.mutableCopy(this.networks_);
                                }
                                this.networks_.add((Network) input.readMessage(Network.parser(), extensionRegistry));
                            } else if (!parseUnknownField(tag, input)) {
                                done = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.setUnfinishedMessage(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                        }
                    }
                    break;
                case GET_DEFAULT_INSTANCE:
                    break;
                case GET_PARSER:
                    if (PARSER == null) {
                        synchronized (NetworkList.class) {
                            if (PARSER == null) {
                                PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            }
                        }
                    }
                    return PARSER;
                default:
                    throw new UnsupportedOperationException();
            }
            return DEFAULT_INSTANCE;
        }

        static {
            DEFAULT_INSTANCE.makeImmutable();
        }

        public static NetworkList getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<NetworkList> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }

    public static final class Network extends GeneratedMessageLite<Network, Builder> implements NetworkOrBuilder {
        public static final int ACCESS_POINTS_FIELD_NUMBER = 3;
        private static final Network DEFAULT_INSTANCE = new Network();
        public static final int NETWORK_AGENT_ID_FIELD_NUMBER = 5;
        public static final int NETWORK_CONFIG_ID_FIELD_NUMBER = 4;
        private static volatile Parser<Network> PARSER = null;
        public static final int SECURITY_TYPE_FIELD_NUMBER = 2;
        public static final int SSID_FIELD_NUMBER = 1;
        private Internal.ProtobufList<AccessPoint> accessPoints_ = emptyProtobufList();
        private int bitField0_;
        private int networkAgentId_ = 0;
        private int networkConfigId_ = 0;
        private int securityType_ = 0;
        private String ssid_ = "";

        private Network() {
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public boolean hasSsid() {
            return (this.bitField0_ & 1) == 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public String getSsid() {
            return this.ssid_;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public ByteString getSsidBytes() {
            return ByteString.copyFromUtf8(this.ssid_);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSsid(String value) {
            if (value != null) {
                this.bitField0_ |= 1;
                this.ssid_ = value;
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSsid() {
            this.bitField0_ &= -2;
            this.ssid_ = getDefaultInstance().getSsid();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSsidBytes(ByteString value) {
            if (value != null) {
                this.bitField0_ |= 1;
                this.ssid_ = value.toStringUtf8();
                return;
            }
            throw new NullPointerException();
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public boolean hasSecurityType() {
            return (this.bitField0_ & 2) == 2;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public SecurityType getSecurityType() {
            SecurityType result = SecurityType.forNumber(this.securityType_);
            return result == null ? SecurityType.OPEN : result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSecurityType(SecurityType value) {
            if (value != null) {
                this.bitField0_ |= 2;
                this.securityType_ = value.getNumber();
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSecurityType() {
            this.bitField0_ &= -3;
            this.securityType_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public List<AccessPoint> getAccessPointsList() {
            return this.accessPoints_;
        }

        public List<? extends AccessPointOrBuilder> getAccessPointsOrBuilderList() {
            return this.accessPoints_;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public int getAccessPointsCount() {
            return this.accessPoints_.size();
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public AccessPoint getAccessPoints(int index) {
            return this.accessPoints_.get(index);
        }

        public AccessPointOrBuilder getAccessPointsOrBuilder(int index) {
            return this.accessPoints_.get(index);
        }

        private void ensureAccessPointsIsMutable() {
            if (!this.accessPoints_.isModifiable()) {
                this.accessPoints_ = GeneratedMessageLite.mutableCopy(this.accessPoints_);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setAccessPoints(int index, AccessPoint value) {
            if (value != null) {
                ensureAccessPointsIsMutable();
                this.accessPoints_.set(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setAccessPoints(int index, AccessPoint.Builder builderForValue) {
            ensureAccessPointsIsMutable();
            this.accessPoints_.set(index, (AccessPoint) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAccessPoints(AccessPoint value) {
            if (value != null) {
                ensureAccessPointsIsMutable();
                this.accessPoints_.add(value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAccessPoints(int index, AccessPoint value) {
            if (value != null) {
                ensureAccessPointsIsMutable();
                this.accessPoints_.add(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAccessPoints(AccessPoint.Builder builderForValue) {
            ensureAccessPointsIsMutable();
            this.accessPoints_.add((AccessPoint) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAccessPoints(int index, AccessPoint.Builder builderForValue) {
            ensureAccessPointsIsMutable();
            this.accessPoints_.add(index, (AccessPoint) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAllAccessPoints(Iterable<? extends AccessPoint> values) {
            ensureAccessPointsIsMutable();
            AbstractMessageLite.addAll(values, this.accessPoints_);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearAccessPoints() {
            this.accessPoints_ = emptyProtobufList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeAccessPoints(int index) {
            ensureAccessPointsIsMutable();
            this.accessPoints_.remove(index);
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public boolean hasNetworkConfigId() {
            return (this.bitField0_ & 4) == 4;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public int getNetworkConfigId() {
            return this.networkConfigId_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setNetworkConfigId(int value) {
            this.bitField0_ |= 4;
            this.networkConfigId_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearNetworkConfigId() {
            this.bitField0_ &= -5;
            this.networkConfigId_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public boolean hasNetworkAgentId() {
            return (this.bitField0_ & 8) == 8;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
        public int getNetworkAgentId() {
            return this.networkAgentId_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setNetworkAgentId(int value) {
            this.bitField0_ |= 8;
            this.networkAgentId_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearNetworkAgentId() {
            this.bitField0_ &= -9;
            this.networkAgentId_ = 0;
        }

        @Override // com.google.protobuf.MessageLite
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) == 1) {
                output.writeString(1, getSsid());
            }
            if ((this.bitField0_ & 2) == 2) {
                output.writeEnum(2, this.securityType_);
            }
            for (int i = 0; i < this.accessPoints_.size(); i++) {
                output.writeMessage(3, this.accessPoints_.get(i));
            }
            if ((this.bitField0_ & 4) == 4) {
                output.writeInt32(4, this.networkConfigId_);
            }
            if ((this.bitField0_ & 8) == 8) {
                output.writeInt32(5, this.networkAgentId_);
            }
            this.unknownFields.writeTo(output);
        }

        @Override // com.google.protobuf.MessageLite
        public int getSerializedSize() {
            int size = this.memoizedSerializedSize;
            if (size != -1) {
                return size;
            }
            int size2 = 0;
            if ((this.bitField0_ & 1) == 1) {
                size2 = 0 + CodedOutputStream.computeStringSize(1, getSsid());
            }
            if ((this.bitField0_ & 2) == 2) {
                size2 += CodedOutputStream.computeEnumSize(2, this.securityType_);
            }
            for (int i = 0; i < this.accessPoints_.size(); i++) {
                size2 += CodedOutputStream.computeMessageSize(3, this.accessPoints_.get(i));
            }
            if ((this.bitField0_ & 4) == 4) {
                size2 += CodedOutputStream.computeInt32Size(4, this.networkConfigId_);
            }
            if ((this.bitField0_ & 8) == 8) {
                size2 += CodedOutputStream.computeInt32Size(5, this.networkAgentId_);
            }
            int size3 = size2 + this.unknownFields.getSerializedSize();
            this.memoizedSerializedSize = size3;
            return size3;
        }

        public static Network parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static Network parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static Network parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static Network parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static Network parseFrom(InputStream input) throws IOException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static Network parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Network parseDelimitedFrom(InputStream input) throws IOException {
            return (Network) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static Network parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Network) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Network parseFrom(CodedInputStream input) throws IOException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static Network parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Network) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Network prototype) {
            return (Builder) ((Builder) DEFAULT_INSTANCE.toBuilder()).mergeFrom((Builder) prototype);
        }

        public static final class Builder extends GeneratedMessageLite.Builder<Network, Builder> implements NetworkOrBuilder {
            private Builder() {
                super(Network.DEFAULT_INSTANCE);
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public boolean hasSsid() {
                return ((Network) this.instance).hasSsid();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public String getSsid() {
                return ((Network) this.instance).getSsid();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public ByteString getSsidBytes() {
                return ((Network) this.instance).getSsidBytes();
            }

            public Builder setSsid(String value) {
                copyOnWrite();
                ((Network) this.instance).setSsid(value);
                return this;
            }

            public Builder clearSsid() {
                copyOnWrite();
                ((Network) this.instance).clearSsid();
                return this;
            }

            public Builder setSsidBytes(ByteString value) {
                copyOnWrite();
                ((Network) this.instance).setSsidBytes(value);
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public boolean hasSecurityType() {
                return ((Network) this.instance).hasSecurityType();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public SecurityType getSecurityType() {
                return ((Network) this.instance).getSecurityType();
            }

            public Builder setSecurityType(SecurityType value) {
                copyOnWrite();
                ((Network) this.instance).setSecurityType(value);
                return this;
            }

            public Builder clearSecurityType() {
                copyOnWrite();
                ((Network) this.instance).clearSecurityType();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public List<AccessPoint> getAccessPointsList() {
                return Collections.unmodifiableList(((Network) this.instance).getAccessPointsList());
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public int getAccessPointsCount() {
                return ((Network) this.instance).getAccessPointsCount();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public AccessPoint getAccessPoints(int index) {
                return ((Network) this.instance).getAccessPoints(index);
            }

            public Builder setAccessPoints(int index, AccessPoint value) {
                copyOnWrite();
                ((Network) this.instance).setAccessPoints(index, value);
                return this;
            }

            public Builder setAccessPoints(int index, AccessPoint.Builder builderForValue) {
                copyOnWrite();
                ((Network) this.instance).setAccessPoints(index, builderForValue);
                return this;
            }

            public Builder addAccessPoints(AccessPoint value) {
                copyOnWrite();
                ((Network) this.instance).addAccessPoints(value);
                return this;
            }

            public Builder addAccessPoints(int index, AccessPoint value) {
                copyOnWrite();
                ((Network) this.instance).addAccessPoints(index, value);
                return this;
            }

            public Builder addAccessPoints(AccessPoint.Builder builderForValue) {
                copyOnWrite();
                ((Network) this.instance).addAccessPoints(builderForValue);
                return this;
            }

            public Builder addAccessPoints(int index, AccessPoint.Builder builderForValue) {
                copyOnWrite();
                ((Network) this.instance).addAccessPoints(index, builderForValue);
                return this;
            }

            public Builder addAllAccessPoints(Iterable<? extends AccessPoint> values) {
                copyOnWrite();
                ((Network) this.instance).addAllAccessPoints(values);
                return this;
            }

            public Builder clearAccessPoints() {
                copyOnWrite();
                ((Network) this.instance).clearAccessPoints();
                return this;
            }

            public Builder removeAccessPoints(int index) {
                copyOnWrite();
                ((Network) this.instance).removeAccessPoints(index);
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public boolean hasNetworkConfigId() {
                return ((Network) this.instance).hasNetworkConfigId();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public int getNetworkConfigId() {
                return ((Network) this.instance).getNetworkConfigId();
            }

            public Builder setNetworkConfigId(int value) {
                copyOnWrite();
                ((Network) this.instance).setNetworkConfigId(value);
                return this;
            }

            public Builder clearNetworkConfigId() {
                copyOnWrite();
                ((Network) this.instance).clearNetworkConfigId();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public boolean hasNetworkAgentId() {
                return ((Network) this.instance).hasNetworkAgentId();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.NetworkOrBuilder
            public int getNetworkAgentId() {
                return ((Network) this.instance).getNetworkAgentId();
            }

            public Builder setNetworkAgentId(int value) {
                copyOnWrite();
                ((Network) this.instance).setNetworkAgentId(value);
                return this;
            }

            public Builder clearNetworkAgentId() {
                copyOnWrite();
                ((Network) this.instance).clearNetworkAgentId();
                return this;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke method, Object arg0, Object arg1) {
            switch (method) {
                case NEW_MUTABLE_INSTANCE:
                    return new Network();
                case IS_INITIALIZED:
                    return DEFAULT_INSTANCE;
                case MAKE_IMMUTABLE:
                    this.accessPoints_.makeImmutable();
                    return null;
                case NEW_BUILDER:
                    return new Builder();
                case VISIT:
                    GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) arg0;
                    Network other = (Network) arg1;
                    this.ssid_ = visitor.visitString(hasSsid(), this.ssid_, other.hasSsid(), other.ssid_);
                    this.securityType_ = visitor.visitInt(hasSecurityType(), this.securityType_, other.hasSecurityType(), other.securityType_);
                    this.accessPoints_ = visitor.visitList(this.accessPoints_, other.accessPoints_);
                    this.networkConfigId_ = visitor.visitInt(hasNetworkConfigId(), this.networkConfigId_, other.hasNetworkConfigId(), other.networkConfigId_);
                    this.networkAgentId_ = visitor.visitInt(hasNetworkAgentId(), this.networkAgentId_, other.hasNetworkAgentId(), other.networkAgentId_);
                    if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                        this.bitField0_ |= other.bitField0_;
                    }
                    return this;
                case MERGE_FROM_STREAM:
                    CodedInputStream input = (CodedInputStream) arg0;
                    ExtensionRegistryLite extensionRegistry = (ExtensionRegistryLite) arg1;
                    boolean done = false;
                    while (!done) {
                        try {
                            int tag = input.readTag();
                            if (tag == 0) {
                                done = true;
                            } else if (tag == 10) {
                                String s = input.readString();
                                this.bitField0_ |= 1;
                                this.ssid_ = s;
                            } else if (tag == 16) {
                                int rawValue = input.readEnum();
                                if (SecurityType.forNumber(rawValue) == null) {
                                    super.mergeVarintField(2, rawValue);
                                } else {
                                    this.bitField0_ = 2 | this.bitField0_;
                                    this.securityType_ = rawValue;
                                }
                            } else if (tag == 26) {
                                if (!this.accessPoints_.isModifiable()) {
                                    this.accessPoints_ = GeneratedMessageLite.mutableCopy(this.accessPoints_);
                                }
                                this.accessPoints_.add((AccessPoint) input.readMessage(AccessPoint.parser(), extensionRegistry));
                            } else if (tag == 32) {
                                this.bitField0_ |= 4;
                                this.networkConfigId_ = input.readInt32();
                            } else if (tag == 40) {
                                this.bitField0_ |= 8;
                                this.networkAgentId_ = input.readInt32();
                            } else if (!parseUnknownField(tag, input)) {
                                done = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.setUnfinishedMessage(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                        }
                    }
                    break;
                case GET_DEFAULT_INSTANCE:
                    break;
                case GET_PARSER:
                    if (PARSER == null) {
                        synchronized (Network.class) {
                            if (PARSER == null) {
                                PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            }
                        }
                    }
                    return PARSER;
                default:
                    throw new UnsupportedOperationException();
            }
            return DEFAULT_INSTANCE;
        }

        static {
            DEFAULT_INSTANCE.makeImmutable();
        }

        public static Network getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Network> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }

    public static final class AccessPoint extends GeneratedMessageLite<AccessPoint, Builder> implements AccessPointOrBuilder {
        public static final int BSSID_FIELD_NUMBER = 2;
        private static final AccessPoint DEFAULT_INSTANCE = new AccessPoint();
        public static final int EVENT_STATS_FIELD_NUMBER = 4;
        public static final int ID_FIELD_NUMBER = 1;
        private static volatile Parser<AccessPoint> PARSER = null;
        public static final int ROAMS_FIELD_NUMBER = 5;
        public static final int SECURITY_TYPE_FIELD_NUMBER = 6;
        public static final int TECHNOLOGY_FIELD_NUMBER = 3;
        private int bitField0_;
        private ByteString bssid_ = ByteString.EMPTY;
        private Internal.ProtobufList<Signal> eventStats_ = emptyProtobufList();
        private int id_ = 0;
        private Internal.ProtobufList<Roam> roams_ = emptyProtobufList();
        private int securityType_ = 0;
        private int technology_ = 0;

        private AccessPoint() {
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public boolean hasId() {
            return (this.bitField0_ & 1) == 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public int getId() {
            return this.id_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setId(int value) {
            this.bitField0_ |= 1;
            this.id_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearId() {
            this.bitField0_ &= -2;
            this.id_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public boolean hasBssid() {
            return (this.bitField0_ & 2) == 2;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public ByteString getBssid() {
            return this.bssid_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setBssid(ByteString value) {
            if (value != null) {
                this.bitField0_ |= 2;
                this.bssid_ = value;
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearBssid() {
            this.bitField0_ &= -3;
            this.bssid_ = getDefaultInstance().getBssid();
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public boolean hasSecurityType() {
            return (this.bitField0_ & 4) == 4;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public SecurityType getSecurityType() {
            SecurityType result = SecurityType.forNumber(this.securityType_);
            return result == null ? SecurityType.OPEN : result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSecurityType(SecurityType value) {
            if (value != null) {
                this.bitField0_ |= 4;
                this.securityType_ = value.getNumber();
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSecurityType() {
            this.bitField0_ &= -5;
            this.securityType_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public boolean hasTechnology() {
            return (this.bitField0_ & 8) == 8;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public Technology getTechnology() {
            Technology result = Technology.forNumber(this.technology_);
            return result == null ? Technology.MODE_UNKNOWN : result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setTechnology(Technology value) {
            if (value != null) {
                this.bitField0_ |= 8;
                this.technology_ = value.getNumber();
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearTechnology() {
            this.bitField0_ &= -9;
            this.technology_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public List<Signal> getEventStatsList() {
            return this.eventStats_;
        }

        public List<? extends SignalOrBuilder> getEventStatsOrBuilderList() {
            return this.eventStats_;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public int getEventStatsCount() {
            return this.eventStats_.size();
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public Signal getEventStats(int index) {
            return this.eventStats_.get(index);
        }

        public SignalOrBuilder getEventStatsOrBuilder(int index) {
            return this.eventStats_.get(index);
        }

        private void ensureEventStatsIsMutable() {
            if (!this.eventStats_.isModifiable()) {
                this.eventStats_ = GeneratedMessageLite.mutableCopy(this.eventStats_);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEventStats(int index, Signal value) {
            if (value != null) {
                ensureEventStatsIsMutable();
                this.eventStats_.set(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEventStats(int index, Signal.Builder builderForValue) {
            ensureEventStatsIsMutable();
            this.eventStats_.set(index, (Signal) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addEventStats(Signal value) {
            if (value != null) {
                ensureEventStatsIsMutable();
                this.eventStats_.add(value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addEventStats(int index, Signal value) {
            if (value != null) {
                ensureEventStatsIsMutable();
                this.eventStats_.add(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addEventStats(Signal.Builder builderForValue) {
            ensureEventStatsIsMutable();
            this.eventStats_.add((Signal) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addEventStats(int index, Signal.Builder builderForValue) {
            ensureEventStatsIsMutable();
            this.eventStats_.add(index, (Signal) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAllEventStats(Iterable<? extends Signal> values) {
            ensureEventStatsIsMutable();
            AbstractMessageLite.addAll(values, this.eventStats_);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearEventStats() {
            this.eventStats_ = emptyProtobufList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeEventStats(int index) {
            ensureEventStatsIsMutable();
            this.eventStats_.remove(index);
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public List<Roam> getRoamsList() {
            return this.roams_;
        }

        public List<? extends RoamOrBuilder> getRoamsOrBuilderList() {
            return this.roams_;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public int getRoamsCount() {
            return this.roams_.size();
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
        public Roam getRoams(int index) {
            return this.roams_.get(index);
        }

        public RoamOrBuilder getRoamsOrBuilder(int index) {
            return this.roams_.get(index);
        }

        private void ensureRoamsIsMutable() {
            if (!this.roams_.isModifiable()) {
                this.roams_ = GeneratedMessageLite.mutableCopy(this.roams_);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setRoams(int index, Roam value) {
            if (value != null) {
                ensureRoamsIsMutable();
                this.roams_.set(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setRoams(int index, Roam.Builder builderForValue) {
            ensureRoamsIsMutable();
            this.roams_.set(index, (Roam) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addRoams(Roam value) {
            if (value != null) {
                ensureRoamsIsMutable();
                this.roams_.add(value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addRoams(int index, Roam value) {
            if (value != null) {
                ensureRoamsIsMutable();
                this.roams_.add(index, value);
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addRoams(Roam.Builder builderForValue) {
            ensureRoamsIsMutable();
            this.roams_.add((Roam) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addRoams(int index, Roam.Builder builderForValue) {
            ensureRoamsIsMutable();
            this.roams_.add(index, (Roam) builderForValue.build());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addAllRoams(Iterable<? extends Roam> values) {
            ensureRoamsIsMutable();
            AbstractMessageLite.addAll(values, this.roams_);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearRoams() {
            this.roams_ = emptyProtobufList();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeRoams(int index) {
            ensureRoamsIsMutable();
            this.roams_.remove(index);
        }

        @Override // com.google.protobuf.MessageLite
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) == 1) {
                output.writeInt32(1, this.id_);
            }
            if ((this.bitField0_ & 2) == 2) {
                output.writeBytes(2, this.bssid_);
            }
            if ((this.bitField0_ & 8) == 8) {
                output.writeEnum(3, this.technology_);
            }
            for (int i = 0; i < this.eventStats_.size(); i++) {
                output.writeMessage(4, this.eventStats_.get(i));
            }
            for (int i2 = 0; i2 < this.roams_.size(); i2++) {
                output.writeMessage(5, this.roams_.get(i2));
            }
            if ((this.bitField0_ & 4) == 4) {
                output.writeEnum(6, this.securityType_);
            }
            this.unknownFields.writeTo(output);
        }

        @Override // com.google.protobuf.MessageLite
        public int getSerializedSize() {
            int size = this.memoizedSerializedSize;
            if (size != -1) {
                return size;
            }
            int size2 = 0;
            if ((this.bitField0_ & 1) == 1) {
                size2 = 0 + CodedOutputStream.computeInt32Size(1, this.id_);
            }
            if ((this.bitField0_ & 2) == 2) {
                size2 += CodedOutputStream.computeBytesSize(2, this.bssid_);
            }
            if ((this.bitField0_ & 8) == 8) {
                size2 += CodedOutputStream.computeEnumSize(3, this.technology_);
            }
            for (int i = 0; i < this.eventStats_.size(); i++) {
                size2 += CodedOutputStream.computeMessageSize(4, this.eventStats_.get(i));
            }
            for (int i2 = 0; i2 < this.roams_.size(); i2++) {
                size2 += CodedOutputStream.computeMessageSize(5, this.roams_.get(i2));
            }
            if ((this.bitField0_ & 4) == 4) {
                size2 += CodedOutputStream.computeEnumSize(6, this.securityType_);
            }
            int size3 = size2 + this.unknownFields.getSerializedSize();
            this.memoizedSerializedSize = size3;
            return size3;
        }

        public static AccessPoint parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static AccessPoint parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static AccessPoint parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static AccessPoint parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static AccessPoint parseFrom(InputStream input) throws IOException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static AccessPoint parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static AccessPoint parseDelimitedFrom(InputStream input) throws IOException {
            return (AccessPoint) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static AccessPoint parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (AccessPoint) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static AccessPoint parseFrom(CodedInputStream input) throws IOException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static AccessPoint parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (AccessPoint) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(AccessPoint prototype) {
            return (Builder) ((Builder) DEFAULT_INSTANCE.toBuilder()).mergeFrom((Builder) prototype);
        }

        public static final class Builder extends GeneratedMessageLite.Builder<AccessPoint, Builder> implements AccessPointOrBuilder {
            private Builder() {
                super(AccessPoint.DEFAULT_INSTANCE);
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public boolean hasId() {
                return ((AccessPoint) this.instance).hasId();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public int getId() {
                return ((AccessPoint) this.instance).getId();
            }

            public Builder setId(int value) {
                copyOnWrite();
                ((AccessPoint) this.instance).setId(value);
                return this;
            }

            public Builder clearId() {
                copyOnWrite();
                ((AccessPoint) this.instance).clearId();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public boolean hasBssid() {
                return ((AccessPoint) this.instance).hasBssid();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public ByteString getBssid() {
                return ((AccessPoint) this.instance).getBssid();
            }

            public Builder setBssid(ByteString value) {
                copyOnWrite();
                ((AccessPoint) this.instance).setBssid(value);
                return this;
            }

            public Builder clearBssid() {
                copyOnWrite();
                ((AccessPoint) this.instance).clearBssid();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public boolean hasSecurityType() {
                return ((AccessPoint) this.instance).hasSecurityType();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public SecurityType getSecurityType() {
                return ((AccessPoint) this.instance).getSecurityType();
            }

            public Builder setSecurityType(SecurityType value) {
                copyOnWrite();
                ((AccessPoint) this.instance).setSecurityType(value);
                return this;
            }

            public Builder clearSecurityType() {
                copyOnWrite();
                ((AccessPoint) this.instance).clearSecurityType();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public boolean hasTechnology() {
                return ((AccessPoint) this.instance).hasTechnology();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public Technology getTechnology() {
                return ((AccessPoint) this.instance).getTechnology();
            }

            public Builder setTechnology(Technology value) {
                copyOnWrite();
                ((AccessPoint) this.instance).setTechnology(value);
                return this;
            }

            public Builder clearTechnology() {
                copyOnWrite();
                ((AccessPoint) this.instance).clearTechnology();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public List<Signal> getEventStatsList() {
                return Collections.unmodifiableList(((AccessPoint) this.instance).getEventStatsList());
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public int getEventStatsCount() {
                return ((AccessPoint) this.instance).getEventStatsCount();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public Signal getEventStats(int index) {
                return ((AccessPoint) this.instance).getEventStats(index);
            }

            public Builder setEventStats(int index, Signal value) {
                copyOnWrite();
                ((AccessPoint) this.instance).setEventStats(index, value);
                return this;
            }

            public Builder setEventStats(int index, Signal.Builder builderForValue) {
                copyOnWrite();
                ((AccessPoint) this.instance).setEventStats(index, builderForValue);
                return this;
            }

            public Builder addEventStats(Signal value) {
                copyOnWrite();
                ((AccessPoint) this.instance).addEventStats(value);
                return this;
            }

            public Builder addEventStats(int index, Signal value) {
                copyOnWrite();
                ((AccessPoint) this.instance).addEventStats(index, value);
                return this;
            }

            public Builder addEventStats(Signal.Builder builderForValue) {
                copyOnWrite();
                ((AccessPoint) this.instance).addEventStats(builderForValue);
                return this;
            }

            public Builder addEventStats(int index, Signal.Builder builderForValue) {
                copyOnWrite();
                ((AccessPoint) this.instance).addEventStats(index, builderForValue);
                return this;
            }

            public Builder addAllEventStats(Iterable<? extends Signal> values) {
                copyOnWrite();
                ((AccessPoint) this.instance).addAllEventStats(values);
                return this;
            }

            public Builder clearEventStats() {
                copyOnWrite();
                ((AccessPoint) this.instance).clearEventStats();
                return this;
            }

            public Builder removeEventStats(int index) {
                copyOnWrite();
                ((AccessPoint) this.instance).removeEventStats(index);
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public List<Roam> getRoamsList() {
                return Collections.unmodifiableList(((AccessPoint) this.instance).getRoamsList());
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public int getRoamsCount() {
                return ((AccessPoint) this.instance).getRoamsCount();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.AccessPointOrBuilder
            public Roam getRoams(int index) {
                return ((AccessPoint) this.instance).getRoams(index);
            }

            public Builder setRoams(int index, Roam value) {
                copyOnWrite();
                ((AccessPoint) this.instance).setRoams(index, value);
                return this;
            }

            public Builder setRoams(int index, Roam.Builder builderForValue) {
                copyOnWrite();
                ((AccessPoint) this.instance).setRoams(index, builderForValue);
                return this;
            }

            public Builder addRoams(Roam value) {
                copyOnWrite();
                ((AccessPoint) this.instance).addRoams(value);
                return this;
            }

            public Builder addRoams(int index, Roam value) {
                copyOnWrite();
                ((AccessPoint) this.instance).addRoams(index, value);
                return this;
            }

            public Builder addRoams(Roam.Builder builderForValue) {
                copyOnWrite();
                ((AccessPoint) this.instance).addRoams(builderForValue);
                return this;
            }

            public Builder addRoams(int index, Roam.Builder builderForValue) {
                copyOnWrite();
                ((AccessPoint) this.instance).addRoams(index, builderForValue);
                return this;
            }

            public Builder addAllRoams(Iterable<? extends Roam> values) {
                copyOnWrite();
                ((AccessPoint) this.instance).addAllRoams(values);
                return this;
            }

            public Builder clearRoams() {
                copyOnWrite();
                ((AccessPoint) this.instance).clearRoams();
                return this;
            }

            public Builder removeRoams(int index) {
                copyOnWrite();
                ((AccessPoint) this.instance).removeRoams(index);
                return this;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke method, Object arg0, Object arg1) {
            switch (method) {
                case NEW_MUTABLE_INSTANCE:
                    return new AccessPoint();
                case IS_INITIALIZED:
                    return DEFAULT_INSTANCE;
                case MAKE_IMMUTABLE:
                    this.eventStats_.makeImmutable();
                    this.roams_.makeImmutable();
                    return null;
                case NEW_BUILDER:
                    return new Builder();
                case VISIT:
                    GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) arg0;
                    AccessPoint other = (AccessPoint) arg1;
                    this.id_ = visitor.visitInt(hasId(), this.id_, other.hasId(), other.id_);
                    this.bssid_ = visitor.visitByteString(hasBssid(), this.bssid_, other.hasBssid(), other.bssid_);
                    this.securityType_ = visitor.visitInt(hasSecurityType(), this.securityType_, other.hasSecurityType(), other.securityType_);
                    this.technology_ = visitor.visitInt(hasTechnology(), this.technology_, other.hasTechnology(), other.technology_);
                    this.eventStats_ = visitor.visitList(this.eventStats_, other.eventStats_);
                    this.roams_ = visitor.visitList(this.roams_, other.roams_);
                    if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                        this.bitField0_ |= other.bitField0_;
                    }
                    return this;
                case MERGE_FROM_STREAM:
                    CodedInputStream input = (CodedInputStream) arg0;
                    ExtensionRegistryLite extensionRegistry = (ExtensionRegistryLite) arg1;
                    boolean done = false;
                    while (!done) {
                        try {
                            int tag = input.readTag();
                            if (tag == 0) {
                                done = true;
                            } else if (tag == 8) {
                                this.bitField0_ |= 1;
                                this.id_ = input.readInt32();
                            } else if (tag == 18) {
                                this.bitField0_ |= 2;
                                this.bssid_ = input.readBytes();
                            } else if (tag == 24) {
                                int rawValue = input.readEnum();
                                if (Technology.forNumber(rawValue) == null) {
                                    super.mergeVarintField(3, rawValue);
                                } else {
                                    this.bitField0_ = 8 | this.bitField0_;
                                    this.technology_ = rawValue;
                                }
                            } else if (tag == 34) {
                                if (!this.eventStats_.isModifiable()) {
                                    this.eventStats_ = GeneratedMessageLite.mutableCopy(this.eventStats_);
                                }
                                this.eventStats_.add((Signal) input.readMessage(Signal.parser(), extensionRegistry));
                            } else if (tag == 42) {
                                if (!this.roams_.isModifiable()) {
                                    this.roams_ = GeneratedMessageLite.mutableCopy(this.roams_);
                                }
                                this.roams_.add((Roam) input.readMessage(Roam.parser(), extensionRegistry));
                            } else if (tag == 48) {
                                int rawValue2 = input.readEnum();
                                if (SecurityType.forNumber(rawValue2) == null) {
                                    super.mergeVarintField(6, rawValue2);
                                } else {
                                    this.bitField0_ |= 4;
                                    this.securityType_ = rawValue2;
                                }
                            } else if (!parseUnknownField(tag, input)) {
                                done = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.setUnfinishedMessage(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                        }
                    }
                    break;
                case GET_DEFAULT_INSTANCE:
                    break;
                case GET_PARSER:
                    if (PARSER == null) {
                        synchronized (AccessPoint.class) {
                            if (PARSER == null) {
                                PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            }
                        }
                    }
                    return PARSER;
                default:
                    throw new UnsupportedOperationException();
            }
            return DEFAULT_INSTANCE;
        }

        static {
            DEFAULT_INSTANCE.makeImmutable();
        }

        public static AccessPoint getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<AccessPoint> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }

    public static final class Signal extends GeneratedMessageLite<Signal, Builder> implements SignalOrBuilder {
        private static final Signal DEFAULT_INSTANCE = new Signal();
        public static final int ELAPSED_MS_FIELD_NUMBER = 5;
        public static final int EVENT_FIELD_NUMBER = 1;
        public static final int FREQUENCY_FIELD_NUMBER = 2;
        public static final int LINKSPEED_FIELD_NUMBER = 4;
        private static volatile Parser<Signal> PARSER = null;
        public static final int RSSI_FIELD_NUMBER = 3;
        private int bitField0_;
        private UnivariateStatistic elapsedMs_;
        private int event_ = 1;
        private int frequency_ = 0;
        private UnivariateStatistic linkspeed_;
        private UnivariateStatistic rssi_;

        private Signal() {
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public boolean hasEvent() {
            return (this.bitField0_ & 1) == 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public Event getEvent() {
            Event result = Event.forNumber(this.event_);
            return result == null ? Event.SIGNAL_POLL : result;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEvent(Event value) {
            if (value != null) {
                this.bitField0_ |= 1;
                this.event_ = value.getNumber();
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearEvent() {
            this.bitField0_ &= -2;
            this.event_ = 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public boolean hasFrequency() {
            return (this.bitField0_ & 2) == 2;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public int getFrequency() {
            return this.frequency_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setFrequency(int value) {
            this.bitField0_ |= 2;
            this.frequency_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearFrequency() {
            this.bitField0_ &= -3;
            this.frequency_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public boolean hasRssi() {
            return (this.bitField0_ & 4) == 4;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public UnivariateStatistic getRssi() {
            UnivariateStatistic univariateStatistic = this.rssi_;
            return univariateStatistic == null ? UnivariateStatistic.getDefaultInstance() : univariateStatistic;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setRssi(UnivariateStatistic value) {
            if (value != null) {
                this.rssi_ = value;
                this.bitField0_ |= 4;
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setRssi(UnivariateStatistic.Builder builderForValue) {
            this.rssi_ = (UnivariateStatistic) builderForValue.build();
            this.bitField0_ |= 4;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void mergeRssi(UnivariateStatistic value) {
            UnivariateStatistic univariateStatistic = this.rssi_;
            if (univariateStatistic == null || univariateStatistic == UnivariateStatistic.getDefaultInstance()) {
                this.rssi_ = value;
            } else {
                this.rssi_ = (UnivariateStatistic) ((UnivariateStatistic.Builder) UnivariateStatistic.newBuilder(this.rssi_).mergeFrom((UnivariateStatistic.Builder) value)).buildPartial();
            }
            this.bitField0_ |= 4;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearRssi() {
            this.rssi_ = null;
            this.bitField0_ &= -5;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public boolean hasLinkspeed() {
            return (this.bitField0_ & 8) == 8;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public UnivariateStatistic getLinkspeed() {
            UnivariateStatistic univariateStatistic = this.linkspeed_;
            return univariateStatistic == null ? UnivariateStatistic.getDefaultInstance() : univariateStatistic;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setLinkspeed(UnivariateStatistic value) {
            if (value != null) {
                this.linkspeed_ = value;
                this.bitField0_ |= 8;
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setLinkspeed(UnivariateStatistic.Builder builderForValue) {
            this.linkspeed_ = (UnivariateStatistic) builderForValue.build();
            this.bitField0_ |= 8;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void mergeLinkspeed(UnivariateStatistic value) {
            UnivariateStatistic univariateStatistic = this.linkspeed_;
            if (univariateStatistic == null || univariateStatistic == UnivariateStatistic.getDefaultInstance()) {
                this.linkspeed_ = value;
            } else {
                this.linkspeed_ = (UnivariateStatistic) ((UnivariateStatistic.Builder) UnivariateStatistic.newBuilder(this.linkspeed_).mergeFrom((UnivariateStatistic.Builder) value)).buildPartial();
            }
            this.bitField0_ |= 8;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearLinkspeed() {
            this.linkspeed_ = null;
            this.bitField0_ &= -9;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public boolean hasElapsedMs() {
            return (this.bitField0_ & 16) == 16;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
        public UnivariateStatistic getElapsedMs() {
            UnivariateStatistic univariateStatistic = this.elapsedMs_;
            return univariateStatistic == null ? UnivariateStatistic.getDefaultInstance() : univariateStatistic;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setElapsedMs(UnivariateStatistic value) {
            if (value != null) {
                this.elapsedMs_ = value;
                this.bitField0_ |= 16;
                return;
            }
            throw new NullPointerException();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setElapsedMs(UnivariateStatistic.Builder builderForValue) {
            this.elapsedMs_ = (UnivariateStatistic) builderForValue.build();
            this.bitField0_ |= 16;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void mergeElapsedMs(UnivariateStatistic value) {
            UnivariateStatistic univariateStatistic = this.elapsedMs_;
            if (univariateStatistic == null || univariateStatistic == UnivariateStatistic.getDefaultInstance()) {
                this.elapsedMs_ = value;
            } else {
                this.elapsedMs_ = (UnivariateStatistic) ((UnivariateStatistic.Builder) UnivariateStatistic.newBuilder(this.elapsedMs_).mergeFrom((UnivariateStatistic.Builder) value)).buildPartial();
            }
            this.bitField0_ |= 16;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearElapsedMs() {
            this.elapsedMs_ = null;
            this.bitField0_ &= -17;
        }

        @Override // com.google.protobuf.MessageLite
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) == 1) {
                output.writeEnum(1, this.event_);
            }
            if ((this.bitField0_ & 2) == 2) {
                output.writeInt32(2, this.frequency_);
            }
            if ((this.bitField0_ & 4) == 4) {
                output.writeMessage(3, getRssi());
            }
            if ((this.bitField0_ & 8) == 8) {
                output.writeMessage(4, getLinkspeed());
            }
            if ((this.bitField0_ & 16) == 16) {
                output.writeMessage(5, getElapsedMs());
            }
            this.unknownFields.writeTo(output);
        }

        @Override // com.google.protobuf.MessageLite
        public int getSerializedSize() {
            int size = this.memoizedSerializedSize;
            if (size != -1) {
                return size;
            }
            int size2 = 0;
            if ((this.bitField0_ & 1) == 1) {
                size2 = 0 + CodedOutputStream.computeEnumSize(1, this.event_);
            }
            if ((this.bitField0_ & 2) == 2) {
                size2 += CodedOutputStream.computeInt32Size(2, this.frequency_);
            }
            if ((this.bitField0_ & 4) == 4) {
                size2 += CodedOutputStream.computeMessageSize(3, getRssi());
            }
            if ((this.bitField0_ & 8) == 8) {
                size2 += CodedOutputStream.computeMessageSize(4, getLinkspeed());
            }
            if ((this.bitField0_ & 16) == 16) {
                size2 += CodedOutputStream.computeMessageSize(5, getElapsedMs());
            }
            int size3 = size2 + this.unknownFields.getSerializedSize();
            this.memoizedSerializedSize = size3;
            return size3;
        }

        public static Signal parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static Signal parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static Signal parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static Signal parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static Signal parseFrom(InputStream input) throws IOException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static Signal parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Signal parseDelimitedFrom(InputStream input) throws IOException {
            return (Signal) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static Signal parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Signal) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Signal parseFrom(CodedInputStream input) throws IOException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static Signal parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Signal) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Signal prototype) {
            return (Builder) ((Builder) DEFAULT_INSTANCE.toBuilder()).mergeFrom((Builder) prototype);
        }

        public static final class Builder extends GeneratedMessageLite.Builder<Signal, Builder> implements SignalOrBuilder {
            private Builder() {
                super(Signal.DEFAULT_INSTANCE);
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public boolean hasEvent() {
                return ((Signal) this.instance).hasEvent();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public Event getEvent() {
                return ((Signal) this.instance).getEvent();
            }

            public Builder setEvent(Event value) {
                copyOnWrite();
                ((Signal) this.instance).setEvent(value);
                return this;
            }

            public Builder clearEvent() {
                copyOnWrite();
                ((Signal) this.instance).clearEvent();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public boolean hasFrequency() {
                return ((Signal) this.instance).hasFrequency();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public int getFrequency() {
                return ((Signal) this.instance).getFrequency();
            }

            public Builder setFrequency(int value) {
                copyOnWrite();
                ((Signal) this.instance).setFrequency(value);
                return this;
            }

            public Builder clearFrequency() {
                copyOnWrite();
                ((Signal) this.instance).clearFrequency();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public boolean hasRssi() {
                return ((Signal) this.instance).hasRssi();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public UnivariateStatistic getRssi() {
                return ((Signal) this.instance).getRssi();
            }

            public Builder setRssi(UnivariateStatistic value) {
                copyOnWrite();
                ((Signal) this.instance).setRssi(value);
                return this;
            }

            public Builder setRssi(UnivariateStatistic.Builder builderForValue) {
                copyOnWrite();
                ((Signal) this.instance).setRssi(builderForValue);
                return this;
            }

            public Builder mergeRssi(UnivariateStatistic value) {
                copyOnWrite();
                ((Signal) this.instance).mergeRssi(value);
                return this;
            }

            public Builder clearRssi() {
                copyOnWrite();
                ((Signal) this.instance).clearRssi();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public boolean hasLinkspeed() {
                return ((Signal) this.instance).hasLinkspeed();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public UnivariateStatistic getLinkspeed() {
                return ((Signal) this.instance).getLinkspeed();
            }

            public Builder setLinkspeed(UnivariateStatistic value) {
                copyOnWrite();
                ((Signal) this.instance).setLinkspeed(value);
                return this;
            }

            public Builder setLinkspeed(UnivariateStatistic.Builder builderForValue) {
                copyOnWrite();
                ((Signal) this.instance).setLinkspeed(builderForValue);
                return this;
            }

            public Builder mergeLinkspeed(UnivariateStatistic value) {
                copyOnWrite();
                ((Signal) this.instance).mergeLinkspeed(value);
                return this;
            }

            public Builder clearLinkspeed() {
                copyOnWrite();
                ((Signal) this.instance).clearLinkspeed();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public boolean hasElapsedMs() {
                return ((Signal) this.instance).hasElapsedMs();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.SignalOrBuilder
            public UnivariateStatistic getElapsedMs() {
                return ((Signal) this.instance).getElapsedMs();
            }

            public Builder setElapsedMs(UnivariateStatistic value) {
                copyOnWrite();
                ((Signal) this.instance).setElapsedMs(value);
                return this;
            }

            public Builder setElapsedMs(UnivariateStatistic.Builder builderForValue) {
                copyOnWrite();
                ((Signal) this.instance).setElapsedMs(builderForValue);
                return this;
            }

            public Builder mergeElapsedMs(UnivariateStatistic value) {
                copyOnWrite();
                ((Signal) this.instance).mergeElapsedMs(value);
                return this;
            }

            public Builder clearElapsedMs() {
                copyOnWrite();
                ((Signal) this.instance).clearElapsedMs();
                return this;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke method, Object arg0, Object arg1) {
            switch (method) {
                case NEW_MUTABLE_INSTANCE:
                    return new Signal();
                case IS_INITIALIZED:
                    return DEFAULT_INSTANCE;
                case MAKE_IMMUTABLE:
                    return null;
                case NEW_BUILDER:
                    return new Builder();
                case VISIT:
                    GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) arg0;
                    Signal other = (Signal) arg1;
                    this.event_ = visitor.visitInt(hasEvent(), this.event_, other.hasEvent(), other.event_);
                    this.frequency_ = visitor.visitInt(hasFrequency(), this.frequency_, other.hasFrequency(), other.frequency_);
                    this.rssi_ = (UnivariateStatistic) visitor.visitMessage(this.rssi_, other.rssi_);
                    this.linkspeed_ = (UnivariateStatistic) visitor.visitMessage(this.linkspeed_, other.linkspeed_);
                    this.elapsedMs_ = (UnivariateStatistic) visitor.visitMessage(this.elapsedMs_, other.elapsedMs_);
                    if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                        this.bitField0_ |= other.bitField0_;
                    }
                    return this;
                case MERGE_FROM_STREAM:
                    CodedInputStream input = (CodedInputStream) arg0;
                    ExtensionRegistryLite extensionRegistry = (ExtensionRegistryLite) arg1;
                    boolean done = false;
                    while (!done) {
                        try {
                            int tag = input.readTag();
                            if (tag == 0) {
                                done = true;
                            } else if (tag == 8) {
                                int rawValue = input.readEnum();
                                if (Event.forNumber(rawValue) == null) {
                                    super.mergeVarintField(1, rawValue);
                                } else {
                                    this.bitField0_ = 1 | this.bitField0_;
                                    this.event_ = rawValue;
                                }
                            } else if (tag == 16) {
                                this.bitField0_ |= 2;
                                this.frequency_ = input.readInt32();
                            } else if (tag == 26) {
                                UnivariateStatistic.Builder subBuilder = null;
                                if ((this.bitField0_ & 4) == 4) {
                                    subBuilder = (UnivariateStatistic.Builder) this.rssi_.toBuilder();
                                }
                                this.rssi_ = (UnivariateStatistic) input.readMessage(UnivariateStatistic.parser(), extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom((UnivariateStatistic.Builder) this.rssi_);
                                    this.rssi_ = (UnivariateStatistic) subBuilder.buildPartial();
                                }
                                this.bitField0_ |= 4;
                            } else if (tag == 34) {
                                UnivariateStatistic.Builder subBuilder2 = null;
                                if ((this.bitField0_ & 8) == 8) {
                                    subBuilder2 = (UnivariateStatistic.Builder) this.linkspeed_.toBuilder();
                                }
                                this.linkspeed_ = (UnivariateStatistic) input.readMessage(UnivariateStatistic.parser(), extensionRegistry);
                                if (subBuilder2 != null) {
                                    subBuilder2.mergeFrom((UnivariateStatistic.Builder) this.linkspeed_);
                                    this.linkspeed_ = (UnivariateStatistic) subBuilder2.buildPartial();
                                }
                                this.bitField0_ = 8 | this.bitField0_;
                            } else if (tag == 42) {
                                UnivariateStatistic.Builder subBuilder3 = null;
                                if ((this.bitField0_ & 16) == 16) {
                                    subBuilder3 = (UnivariateStatistic.Builder) this.elapsedMs_.toBuilder();
                                }
                                this.elapsedMs_ = (UnivariateStatistic) input.readMessage(UnivariateStatistic.parser(), extensionRegistry);
                                if (subBuilder3 != null) {
                                    subBuilder3.mergeFrom((UnivariateStatistic.Builder) this.elapsedMs_);
                                    this.elapsedMs_ = (UnivariateStatistic) subBuilder3.buildPartial();
                                }
                                this.bitField0_ = 16 | this.bitField0_;
                            } else if (!parseUnknownField(tag, input)) {
                                done = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.setUnfinishedMessage(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                        }
                    }
                    break;
                case GET_DEFAULT_INSTANCE:
                    break;
                case GET_PARSER:
                    if (PARSER == null) {
                        synchronized (Signal.class) {
                            if (PARSER == null) {
                                PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            }
                        }
                    }
                    return PARSER;
                default:
                    throw new UnsupportedOperationException();
            }
            return DEFAULT_INSTANCE;
        }

        static {
            DEFAULT_INSTANCE.makeImmutable();
        }

        public static Signal getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Signal> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }

    public static final class UnivariateStatistic extends GeneratedMessageLite<UnivariateStatistic, Builder> implements UnivariateStatisticOrBuilder {
        public static final int COUNT_FIELD_NUMBER = 1;
        private static final UnivariateStatistic DEFAULT_INSTANCE = new UnivariateStatistic();
        public static final int HISTORICAL_MEAN_FIELD_NUMBER = 6;
        public static final int HISTORICAL_VARIANCE_FIELD_NUMBER = 7;
        public static final int MAX_VALUE_FIELD_NUMBER = 5;
        public static final int MIN_VALUE_FIELD_NUMBER = 4;
        private static volatile Parser<UnivariateStatistic> PARSER = null;
        public static final int SUM_FIELD_NUMBER = 2;
        public static final int SUM_OF_SQUARES_FIELD_NUMBER = 3;
        private int bitField0_;
        private long count_ = 0;
        private double historicalMean_ = 0.0d;
        private double historicalVariance_ = 0.0d;
        private double maxValue_ = 0.0d;
        private double minValue_ = 0.0d;
        private double sumOfSquares_ = 0.0d;
        private double sum_ = 0.0d;

        private UnivariateStatistic() {
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasCount() {
            return (this.bitField0_ & 1) == 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public long getCount() {
            return this.count_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCount(long value) {
            this.bitField0_ |= 1;
            this.count_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearCount() {
            this.bitField0_ &= -2;
            this.count_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasSum() {
            return (this.bitField0_ & 2) == 2;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public double getSum() {
            return this.sum_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSum(double value) {
            this.bitField0_ |= 2;
            this.sum_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSum() {
            this.bitField0_ &= -3;
            this.sum_ = 0.0d;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasSumOfSquares() {
            return (this.bitField0_ & 4) == 4;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public double getSumOfSquares() {
            return this.sumOfSquares_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSumOfSquares(double value) {
            this.bitField0_ |= 4;
            this.sumOfSquares_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearSumOfSquares() {
            this.bitField0_ &= -5;
            this.sumOfSquares_ = 0.0d;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasMinValue() {
            return (this.bitField0_ & 8) == 8;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public double getMinValue() {
            return this.minValue_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setMinValue(double value) {
            this.bitField0_ |= 8;
            this.minValue_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearMinValue() {
            this.bitField0_ &= -9;
            this.minValue_ = 0.0d;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasMaxValue() {
            return (this.bitField0_ & 16) == 16;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public double getMaxValue() {
            return this.maxValue_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setMaxValue(double value) {
            this.bitField0_ |= 16;
            this.maxValue_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearMaxValue() {
            this.bitField0_ &= -17;
            this.maxValue_ = 0.0d;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasHistoricalMean() {
            return (this.bitField0_ & 32) == 32;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public double getHistoricalMean() {
            return this.historicalMean_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setHistoricalMean(double value) {
            this.bitField0_ |= 32;
            this.historicalMean_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearHistoricalMean() {
            this.bitField0_ &= -33;
            this.historicalMean_ = 0.0d;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public boolean hasHistoricalVariance() {
            return (this.bitField0_ & 64) == 64;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
        public double getHistoricalVariance() {
            return this.historicalVariance_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setHistoricalVariance(double value) {
            this.bitField0_ |= 64;
            this.historicalVariance_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearHistoricalVariance() {
            this.bitField0_ &= -65;
            this.historicalVariance_ = 0.0d;
        }

        @Override // com.google.protobuf.MessageLite
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) == 1) {
                output.writeInt64(1, this.count_);
            }
            if ((this.bitField0_ & 2) == 2) {
                output.writeDouble(2, this.sum_);
            }
            if ((this.bitField0_ & 4) == 4) {
                output.writeDouble(3, this.sumOfSquares_);
            }
            if ((this.bitField0_ & 8) == 8) {
                output.writeDouble(4, this.minValue_);
            }
            if ((this.bitField0_ & 16) == 16) {
                output.writeDouble(5, this.maxValue_);
            }
            if ((this.bitField0_ & 32) == 32) {
                output.writeDouble(6, this.historicalMean_);
            }
            if ((this.bitField0_ & 64) == 64) {
                output.writeDouble(7, this.historicalVariance_);
            }
            this.unknownFields.writeTo(output);
        }

        @Override // com.google.protobuf.MessageLite
        public int getSerializedSize() {
            int size = this.memoizedSerializedSize;
            if (size != -1) {
                return size;
            }
            int size2 = 0;
            if ((this.bitField0_ & 1) == 1) {
                size2 = 0 + CodedOutputStream.computeInt64Size(1, this.count_);
            }
            if ((this.bitField0_ & 2) == 2) {
                size2 += CodedOutputStream.computeDoubleSize(2, this.sum_);
            }
            if ((this.bitField0_ & 4) == 4) {
                size2 += CodedOutputStream.computeDoubleSize(3, this.sumOfSquares_);
            }
            if ((this.bitField0_ & 8) == 8) {
                size2 += CodedOutputStream.computeDoubleSize(4, this.minValue_);
            }
            if ((this.bitField0_ & 16) == 16) {
                size2 += CodedOutputStream.computeDoubleSize(5, this.maxValue_);
            }
            if ((this.bitField0_ & 32) == 32) {
                size2 += CodedOutputStream.computeDoubleSize(6, this.historicalMean_);
            }
            if ((this.bitField0_ & 64) == 64) {
                size2 += CodedOutputStream.computeDoubleSize(7, this.historicalVariance_);
            }
            int size3 = size2 + this.unknownFields.getSerializedSize();
            this.memoizedSerializedSize = size3;
            return size3;
        }

        public static UnivariateStatistic parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static UnivariateStatistic parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static UnivariateStatistic parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static UnivariateStatistic parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static UnivariateStatistic parseFrom(InputStream input) throws IOException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static UnivariateStatistic parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static UnivariateStatistic parseDelimitedFrom(InputStream input) throws IOException {
            return (UnivariateStatistic) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static UnivariateStatistic parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (UnivariateStatistic) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static UnivariateStatistic parseFrom(CodedInputStream input) throws IOException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static UnivariateStatistic parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (UnivariateStatistic) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(UnivariateStatistic prototype) {
            return (Builder) ((Builder) DEFAULT_INSTANCE.toBuilder()).mergeFrom((Builder) prototype);
        }

        public static final class Builder extends GeneratedMessageLite.Builder<UnivariateStatistic, Builder> implements UnivariateStatisticOrBuilder {
            private Builder() {
                super(UnivariateStatistic.DEFAULT_INSTANCE);
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasCount() {
                return ((UnivariateStatistic) this.instance).hasCount();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public long getCount() {
                return ((UnivariateStatistic) this.instance).getCount();
            }

            public Builder setCount(long value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setCount(value);
                return this;
            }

            public Builder clearCount() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearCount();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasSum() {
                return ((UnivariateStatistic) this.instance).hasSum();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public double getSum() {
                return ((UnivariateStatistic) this.instance).getSum();
            }

            public Builder setSum(double value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setSum(value);
                return this;
            }

            public Builder clearSum() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearSum();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasSumOfSquares() {
                return ((UnivariateStatistic) this.instance).hasSumOfSquares();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public double getSumOfSquares() {
                return ((UnivariateStatistic) this.instance).getSumOfSquares();
            }

            public Builder setSumOfSquares(double value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setSumOfSquares(value);
                return this;
            }

            public Builder clearSumOfSquares() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearSumOfSquares();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasMinValue() {
                return ((UnivariateStatistic) this.instance).hasMinValue();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public double getMinValue() {
                return ((UnivariateStatistic) this.instance).getMinValue();
            }

            public Builder setMinValue(double value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setMinValue(value);
                return this;
            }

            public Builder clearMinValue() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearMinValue();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasMaxValue() {
                return ((UnivariateStatistic) this.instance).hasMaxValue();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public double getMaxValue() {
                return ((UnivariateStatistic) this.instance).getMaxValue();
            }

            public Builder setMaxValue(double value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setMaxValue(value);
                return this;
            }

            public Builder clearMaxValue() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearMaxValue();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasHistoricalMean() {
                return ((UnivariateStatistic) this.instance).hasHistoricalMean();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public double getHistoricalMean() {
                return ((UnivariateStatistic) this.instance).getHistoricalMean();
            }

            public Builder setHistoricalMean(double value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setHistoricalMean(value);
                return this;
            }

            public Builder clearHistoricalMean() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearHistoricalMean();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public boolean hasHistoricalVariance() {
                return ((UnivariateStatistic) this.instance).hasHistoricalVariance();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.UnivariateStatisticOrBuilder
            public double getHistoricalVariance() {
                return ((UnivariateStatistic) this.instance).getHistoricalVariance();
            }

            public Builder setHistoricalVariance(double value) {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).setHistoricalVariance(value);
                return this;
            }

            public Builder clearHistoricalVariance() {
                copyOnWrite();
                ((UnivariateStatistic) this.instance).clearHistoricalVariance();
                return this;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke method, Object arg0, Object arg1) {
            switch (method) {
                case NEW_MUTABLE_INSTANCE:
                    return new UnivariateStatistic();
                case IS_INITIALIZED:
                    return DEFAULT_INSTANCE;
                case MAKE_IMMUTABLE:
                    return null;
                case NEW_BUILDER:
                    return new Builder();
                case VISIT:
                    GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) arg0;
                    UnivariateStatistic other = (UnivariateStatistic) arg1;
                    this.count_ = visitor.visitLong(hasCount(), this.count_, other.hasCount(), other.count_);
                    this.sum_ = visitor.visitDouble(hasSum(), this.sum_, other.hasSum(), other.sum_);
                    this.sumOfSquares_ = visitor.visitDouble(hasSumOfSquares(), this.sumOfSquares_, other.hasSumOfSquares(), other.sumOfSquares_);
                    this.minValue_ = visitor.visitDouble(hasMinValue(), this.minValue_, other.hasMinValue(), other.minValue_);
                    this.maxValue_ = visitor.visitDouble(hasMaxValue(), this.maxValue_, other.hasMaxValue(), other.maxValue_);
                    this.historicalMean_ = visitor.visitDouble(hasHistoricalMean(), this.historicalMean_, other.hasHistoricalMean(), other.historicalMean_);
                    this.historicalVariance_ = visitor.visitDouble(hasHistoricalVariance(), this.historicalVariance_, other.hasHistoricalVariance(), other.historicalVariance_);
                    if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                        this.bitField0_ |= other.bitField0_;
                    }
                    return this;
                case MERGE_FROM_STREAM:
                    CodedInputStream input = (CodedInputStream) arg0;
                    ExtensionRegistryLite extensionRegistryLite = (ExtensionRegistryLite) arg1;
                    boolean done = false;
                    while (!done) {
                        try {
                            int tag = input.readTag();
                            if (tag == 0) {
                                done = true;
                            } else if (tag == 8) {
                                this.bitField0_ |= 1;
                                this.count_ = input.readInt64();
                            } else if (tag == 17) {
                                this.bitField0_ |= 2;
                                this.sum_ = input.readDouble();
                            } else if (tag == 25) {
                                this.bitField0_ |= 4;
                                this.sumOfSquares_ = input.readDouble();
                            } else if (tag == 33) {
                                this.bitField0_ = 8 | this.bitField0_;
                                this.minValue_ = input.readDouble();
                            } else if (tag == 41) {
                                this.bitField0_ |= 16;
                                this.maxValue_ = input.readDouble();
                            } else if (tag == 49) {
                                this.bitField0_ |= 32;
                                this.historicalMean_ = input.readDouble();
                            } else if (tag == 57) {
                                this.bitField0_ |= 64;
                                this.historicalVariance_ = input.readDouble();
                            } else if (!parseUnknownField(tag, input)) {
                                done = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.setUnfinishedMessage(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                        }
                    }
                    break;
                case GET_DEFAULT_INSTANCE:
                    break;
                case GET_PARSER:
                    if (PARSER == null) {
                        synchronized (UnivariateStatistic.class) {
                            if (PARSER == null) {
                                PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            }
                        }
                    }
                    return PARSER;
                default:
                    throw new UnsupportedOperationException();
            }
            return DEFAULT_INSTANCE;
        }

        static {
            DEFAULT_INSTANCE.makeImmutable();
        }

        public static UnivariateStatistic getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<UnivariateStatistic> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }

    public static final class Roam extends GeneratedMessageLite<Roam, Builder> implements RoamOrBuilder {
        public static final int BAD_FIELD_NUMBER = 3;
        private static final Roam DEFAULT_INSTANCE = new Roam();
        public static final int GOOD_FIELD_NUMBER = 2;
        private static volatile Parser<Roam> PARSER = null;
        public static final int TO_ID_FIELD_NUMBER = 1;
        private int bad_ = 0;
        private int bitField0_;
        private int good_ = 0;
        private int toId_ = 0;

        private Roam() {
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
        public boolean hasToId() {
            return (this.bitField0_ & 1) == 1;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
        public int getToId() {
            return this.toId_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setToId(int value) {
            this.bitField0_ |= 1;
            this.toId_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearToId() {
            this.bitField0_ &= -2;
            this.toId_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
        public boolean hasGood() {
            return (this.bitField0_ & 2) == 2;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
        public int getGood() {
            return this.good_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setGood(int value) {
            this.bitField0_ |= 2;
            this.good_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearGood() {
            this.bitField0_ &= -3;
            this.good_ = 0;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
        public boolean hasBad() {
            return (this.bitField0_ & 4) == 4;
        }

        @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
        public int getBad() {
            return this.bad_;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setBad(int value) {
            this.bitField0_ |= 4;
            this.bad_ = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearBad() {
            this.bitField0_ &= -5;
            this.bad_ = 0;
        }

        @Override // com.google.protobuf.MessageLite
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) == 1) {
                output.writeInt32(1, this.toId_);
            }
            if ((this.bitField0_ & 2) == 2) {
                output.writeInt32(2, this.good_);
            }
            if ((this.bitField0_ & 4) == 4) {
                output.writeInt32(3, this.bad_);
            }
            this.unknownFields.writeTo(output);
        }

        @Override // com.google.protobuf.MessageLite
        public int getSerializedSize() {
            int size = this.memoizedSerializedSize;
            if (size != -1) {
                return size;
            }
            int size2 = 0;
            if ((this.bitField0_ & 1) == 1) {
                size2 = 0 + CodedOutputStream.computeInt32Size(1, this.toId_);
            }
            if ((this.bitField0_ & 2) == 2) {
                size2 += CodedOutputStream.computeInt32Size(2, this.good_);
            }
            if ((this.bitField0_ & 4) == 4) {
                size2 += CodedOutputStream.computeInt32Size(3, this.bad_);
            }
            int size3 = size2 + this.unknownFields.getSerializedSize();
            this.memoizedSerializedSize = size3;
            return size3;
        }

        public static Roam parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static Roam parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static Roam parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static Roam parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static Roam parseFrom(InputStream input) throws IOException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static Roam parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Roam parseDelimitedFrom(InputStream input) throws IOException {
            return (Roam) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static Roam parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Roam) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Roam parseFrom(CodedInputStream input) throws IOException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static Roam parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (Roam) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Roam prototype) {
            return (Builder) ((Builder) DEFAULT_INSTANCE.toBuilder()).mergeFrom((Builder) prototype);
        }

        public static final class Builder extends GeneratedMessageLite.Builder<Roam, Builder> implements RoamOrBuilder {
            private Builder() {
                super(Roam.DEFAULT_INSTANCE);
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
            public boolean hasToId() {
                return ((Roam) this.instance).hasToId();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
            public int getToId() {
                return ((Roam) this.instance).getToId();
            }

            public Builder setToId(int value) {
                copyOnWrite();
                ((Roam) this.instance).setToId(value);
                return this;
            }

            public Builder clearToId() {
                copyOnWrite();
                ((Roam) this.instance).clearToId();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
            public boolean hasGood() {
                return ((Roam) this.instance).hasGood();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
            public int getGood() {
                return ((Roam) this.instance).getGood();
            }

            public Builder setGood(int value) {
                copyOnWrite();
                ((Roam) this.instance).setGood(value);
                return this;
            }

            public Builder clearGood() {
                copyOnWrite();
                ((Roam) this.instance).clearGood();
                return this;
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
            public boolean hasBad() {
                return ((Roam) this.instance).hasBad();
            }

            @Override // com.android.server.wifi.WifiScoreCardProto.RoamOrBuilder
            public int getBad() {
                return ((Roam) this.instance).getBad();
            }

            public Builder setBad(int value) {
                copyOnWrite();
                ((Roam) this.instance).setBad(value);
                return this;
            }

            public Builder clearBad() {
                copyOnWrite();
                ((Roam) this.instance).clearBad();
                return this;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke method, Object arg0, Object arg1) {
            switch (method) {
                case NEW_MUTABLE_INSTANCE:
                    return new Roam();
                case IS_INITIALIZED:
                    return DEFAULT_INSTANCE;
                case MAKE_IMMUTABLE:
                    return null;
                case NEW_BUILDER:
                    return new Builder();
                case VISIT:
                    GeneratedMessageLite.Visitor visitor = (GeneratedMessageLite.Visitor) arg0;
                    Roam other = (Roam) arg1;
                    this.toId_ = visitor.visitInt(hasToId(), this.toId_, other.hasToId(), other.toId_);
                    this.good_ = visitor.visitInt(hasGood(), this.good_, other.hasGood(), other.good_);
                    this.bad_ = visitor.visitInt(hasBad(), this.bad_, other.hasBad(), other.bad_);
                    if (visitor == GeneratedMessageLite.MergeFromVisitor.INSTANCE) {
                        this.bitField0_ |= other.bitField0_;
                    }
                    return this;
                case MERGE_FROM_STREAM:
                    CodedInputStream input = (CodedInputStream) arg0;
                    ExtensionRegistryLite extensionRegistryLite = (ExtensionRegistryLite) arg1;
                    boolean done = false;
                    while (!done) {
                        try {
                            int tag = input.readTag();
                            if (tag == 0) {
                                done = true;
                            } else if (tag == 8) {
                                this.bitField0_ |= 1;
                                this.toId_ = input.readInt32();
                            } else if (tag == 16) {
                                this.bitField0_ |= 2;
                                this.good_ = input.readInt32();
                            } else if (tag == 24) {
                                this.bitField0_ |= 4;
                                this.bad_ = input.readInt32();
                            } else if (!parseUnknownField(tag, input)) {
                                done = true;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.setUnfinishedMessage(this));
                        } catch (IOException e2) {
                            throw new RuntimeException(new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this));
                        }
                    }
                    break;
                case GET_DEFAULT_INSTANCE:
                    break;
                case GET_PARSER:
                    if (PARSER == null) {
                        synchronized (Roam.class) {
                            if (PARSER == null) {
                                PARSER = new GeneratedMessageLite.DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            }
                        }
                    }
                    return PARSER;
                default:
                    throw new UnsupportedOperationException();
            }
            return DEFAULT_INSTANCE;
        }

        static {
            DEFAULT_INSTANCE.makeImmutable();
        }

        public static Roam getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Roam> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }
}
