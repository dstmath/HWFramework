package com.android.server.connectivity.metrics.nano;

import com.android.framework.protobuf.nano.CodedInputByteBufferNano;
import com.android.framework.protobuf.nano.CodedOutputByteBufferNano;
import com.android.framework.protobuf.nano.InternalNano;
import com.android.framework.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.framework.protobuf.nano.MessageNano;
import com.android.framework.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface IpConnectivityLogClass {
    public static final int BLUETOOTH = 1;
    public static final int CELLULAR = 2;
    public static final int ETHERNET = 3;
    public static final int LOWPAN = 9;
    public static final int MULTIPLE = 6;
    public static final int NONE = 5;
    public static final int UNKNOWN = 0;
    public static final int WIFI = 4;
    public static final int WIFI_NAN = 8;
    public static final int WIFI_P2P = 7;

    public static final class ApfProgramEvent extends MessageNano {
        private static volatile ApfProgramEvent[] _emptyArray;
        public int currentRas;
        public boolean dropMulticast;
        public long effectiveLifetime;
        public int filteredRas;
        public boolean hasIpv4Addr;
        public long lifetime;
        public int programLength;

        public static ApfProgramEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ApfProgramEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ApfProgramEvent() {
            clear();
        }

        public ApfProgramEvent clear() {
            this.lifetime = 0;
            this.effectiveLifetime = 0;
            this.filteredRas = 0;
            this.currentRas = 0;
            this.programLength = 0;
            this.dropMulticast = false;
            this.hasIpv4Addr = false;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.lifetime != 0) {
                output.writeInt64(1, this.lifetime);
            }
            if (this.filteredRas != 0) {
                output.writeInt32(2, this.filteredRas);
            }
            if (this.currentRas != 0) {
                output.writeInt32(3, this.currentRas);
            }
            if (this.programLength != 0) {
                output.writeInt32(4, this.programLength);
            }
            if (this.dropMulticast) {
                output.writeBool(5, this.dropMulticast);
            }
            if (this.hasIpv4Addr) {
                output.writeBool(6, this.hasIpv4Addr);
            }
            if (this.effectiveLifetime != 0) {
                output.writeInt64(7, this.effectiveLifetime);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.lifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.lifetime);
            }
            if (this.filteredRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.filteredRas);
            }
            if (this.currentRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.currentRas);
            }
            if (this.programLength != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.programLength);
            }
            if (this.dropMulticast) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.dropMulticast);
            }
            if (this.hasIpv4Addr) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, this.hasIpv4Addr);
            }
            if (this.effectiveLifetime != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(7, this.effectiveLifetime);
            }
            return size;
        }

        public ApfProgramEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.lifetime = input.readInt64();
                } else if (tag == 16) {
                    this.filteredRas = input.readInt32();
                } else if (tag == 24) {
                    this.currentRas = input.readInt32();
                } else if (tag == 32) {
                    this.programLength = input.readInt32();
                } else if (tag == 40) {
                    this.dropMulticast = input.readBool();
                } else if (tag == 48) {
                    this.hasIpv4Addr = input.readBool();
                } else if (tag == 56) {
                    this.effectiveLifetime = input.readInt64();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ApfProgramEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new ApfProgramEvent(), data);
        }

        public static ApfProgramEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ApfProgramEvent().mergeFrom(input);
        }
    }

    public static final class ApfStatistics extends MessageNano {
        private static volatile ApfStatistics[] _emptyArray;
        public int droppedRas;
        public long durationMs;
        public Pair[] hardwareCounters;
        public int matchingRas;
        public int maxProgramSize;
        public int parseErrors;
        public int programUpdates;
        public int programUpdatesAll;
        public int programUpdatesAllowingMulticast;
        public int receivedRas;
        public int totalPacketDropped;
        public int totalPacketProcessed;
        public int zeroLifetimeRas;

        public static ApfStatistics[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ApfStatistics[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ApfStatistics() {
            clear();
        }

        public ApfStatistics clear() {
            this.durationMs = 0;
            this.receivedRas = 0;
            this.matchingRas = 0;
            this.droppedRas = 0;
            this.zeroLifetimeRas = 0;
            this.parseErrors = 0;
            this.programUpdates = 0;
            this.maxProgramSize = 0;
            this.programUpdatesAll = 0;
            this.programUpdatesAllowingMulticast = 0;
            this.totalPacketProcessed = 0;
            this.totalPacketDropped = 0;
            this.hardwareCounters = Pair.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.durationMs != 0) {
                output.writeInt64(1, this.durationMs);
            }
            if (this.receivedRas != 0) {
                output.writeInt32(2, this.receivedRas);
            }
            if (this.matchingRas != 0) {
                output.writeInt32(3, this.matchingRas);
            }
            if (this.droppedRas != 0) {
                output.writeInt32(5, this.droppedRas);
            }
            if (this.zeroLifetimeRas != 0) {
                output.writeInt32(6, this.zeroLifetimeRas);
            }
            if (this.parseErrors != 0) {
                output.writeInt32(7, this.parseErrors);
            }
            if (this.programUpdates != 0) {
                output.writeInt32(8, this.programUpdates);
            }
            if (this.maxProgramSize != 0) {
                output.writeInt32(9, this.maxProgramSize);
            }
            if (this.programUpdatesAll != 0) {
                output.writeInt32(10, this.programUpdatesAll);
            }
            if (this.programUpdatesAllowingMulticast != 0) {
                output.writeInt32(11, this.programUpdatesAllowingMulticast);
            }
            if (this.totalPacketProcessed != 0) {
                output.writeInt32(12, this.totalPacketProcessed);
            }
            if (this.totalPacketDropped != 0) {
                output.writeInt32(13, this.totalPacketDropped);
            }
            if (this.hardwareCounters != null && this.hardwareCounters.length > 0) {
                for (Pair element : this.hardwareCounters) {
                    if (element != null) {
                        output.writeMessage(14, element);
                    }
                }
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.durationMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.durationMs);
            }
            if (this.receivedRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.receivedRas);
            }
            if (this.matchingRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.matchingRas);
            }
            if (this.droppedRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.droppedRas);
            }
            if (this.zeroLifetimeRas != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.zeroLifetimeRas);
            }
            if (this.parseErrors != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.parseErrors);
            }
            if (this.programUpdates != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, this.programUpdates);
            }
            if (this.maxProgramSize != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.maxProgramSize);
            }
            if (this.programUpdatesAll != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(10, this.programUpdatesAll);
            }
            if (this.programUpdatesAllowingMulticast != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, this.programUpdatesAllowingMulticast);
            }
            if (this.totalPacketProcessed != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(12, this.totalPacketProcessed);
            }
            if (this.totalPacketDropped != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, this.totalPacketDropped);
            }
            if (this.hardwareCounters != null && this.hardwareCounters.length > 0) {
                for (Pair element : this.hardwareCounters) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(14, element);
                    }
                }
            }
            return size;
        }

        public ApfStatistics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.durationMs = input.readInt64();
                        break;
                    case 16:
                        this.receivedRas = input.readInt32();
                        break;
                    case 24:
                        this.matchingRas = input.readInt32();
                        break;
                    case 40:
                        this.droppedRas = input.readInt32();
                        break;
                    case 48:
                        this.zeroLifetimeRas = input.readInt32();
                        break;
                    case 56:
                        this.parseErrors = input.readInt32();
                        break;
                    case 64:
                        this.programUpdates = input.readInt32();
                        break;
                    case 72:
                        this.maxProgramSize = input.readInt32();
                        break;
                    case 80:
                        this.programUpdatesAll = input.readInt32();
                        break;
                    case 88:
                        this.programUpdatesAllowingMulticast = input.readInt32();
                        break;
                    case 96:
                        this.totalPacketProcessed = input.readInt32();
                        break;
                    case 104:
                        this.totalPacketDropped = input.readInt32();
                        break;
                    case 114:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 114);
                        int i = this.hardwareCounters == null ? 0 : this.hardwareCounters.length;
                        Pair[] newArray = new Pair[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.hardwareCounters, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new Pair();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new Pair();
                        input.readMessage(newArray[i]);
                        this.hardwareCounters = newArray;
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

        public static ApfStatistics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new ApfStatistics(), data);
        }

        public static ApfStatistics parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ApfStatistics().mergeFrom(input);
        }
    }

    public static final class ConnectStatistics extends MessageNano {
        private static volatile ConnectStatistics[] _emptyArray;
        public int connectBlockingCount;
        public int connectCount;
        public Pair[] errnosCounters;
        public int ipv6AddrCount;
        public int[] latenciesMs;
        public int[] nonBlockingLatenciesMs;

        public static ConnectStatistics[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ConnectStatistics[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ConnectStatistics() {
            clear();
        }

        public ConnectStatistics clear() {
            this.connectCount = 0;
            this.connectBlockingCount = 0;
            this.ipv6AddrCount = 0;
            this.latenciesMs = WireFormatNano.EMPTY_INT_ARRAY;
            this.nonBlockingLatenciesMs = WireFormatNano.EMPTY_INT_ARRAY;
            this.errnosCounters = Pair.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.connectCount != 0) {
                output.writeInt32(1, this.connectCount);
            }
            if (this.ipv6AddrCount != 0) {
                output.writeInt32(2, this.ipv6AddrCount);
            }
            int i = 0;
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                for (int writeInt32 : this.latenciesMs) {
                    output.writeInt32(3, writeInt32);
                }
            }
            if (this.errnosCounters != null && this.errnosCounters.length > 0) {
                for (Pair element : this.errnosCounters) {
                    if (element != null) {
                        output.writeMessage(4, element);
                    }
                }
            }
            if (this.connectBlockingCount != 0) {
                output.writeInt32(5, this.connectBlockingCount);
            }
            if (this.nonBlockingLatenciesMs != null && this.nonBlockingLatenciesMs.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.nonBlockingLatenciesMs.length) {
                        break;
                    }
                    output.writeInt32(6, this.nonBlockingLatenciesMs[i2]);
                    i = i2 + 1;
                }
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.connectCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.connectCount);
            }
            if (this.ipv6AddrCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.ipv6AddrCount);
            }
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                int dataSize = 0;
                for (int element : this.latenciesMs) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = size + dataSize + (this.latenciesMs.length * 1);
            }
            if (this.errnosCounters != null && this.errnosCounters.length > 0) {
                int size2 = size;
                for (Pair element2 : this.errnosCounters) {
                    if (element2 != null) {
                        size2 += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                }
                size = size2;
            }
            if (this.connectBlockingCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.connectBlockingCount);
            }
            if (this.nonBlockingLatenciesMs == null || this.nonBlockingLatenciesMs.length <= 0) {
                return size;
            }
            int dataSize2 = 0;
            for (int element3 : this.nonBlockingLatenciesMs) {
                dataSize2 += CodedOutputByteBufferNano.computeInt32SizeNoTag(element3);
            }
            return size + dataSize2 + (1 * this.nonBlockingLatenciesMs.length);
        }

        public ConnectStatistics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.connectCount = input.readInt32();
                } else if (tag == 16) {
                    this.ipv6AddrCount = input.readInt32();
                } else if (tag == 24) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                    int i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                    int[] newArray = new int[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    newArray[i] = input.readInt32();
                    this.latenciesMs = newArray;
                } else if (tag == 26) {
                    int limit = input.pushLimit(input.readRawVarint32());
                    int arrayLength2 = 0;
                    int startPos = input.getPosition();
                    while (input.getBytesUntilLimit() > 0) {
                        input.readInt32();
                        arrayLength2++;
                    }
                    input.rewindToPosition(startPos);
                    int i2 = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                    int[] newArray2 = new int[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.latenciesMs, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length) {
                        newArray2[i2] = input.readInt32();
                        i2++;
                    }
                    this.latenciesMs = newArray2;
                    input.popLimit(limit);
                } else if (tag == 34) {
                    int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    int i3 = this.errnosCounters == null ? 0 : this.errnosCounters.length;
                    Pair[] newArray3 = new Pair[(i3 + arrayLength3)];
                    if (i3 != 0) {
                        System.arraycopy(this.errnosCounters, 0, newArray3, 0, i3);
                    }
                    while (i3 < newArray3.length - 1) {
                        newArray3[i3] = new Pair();
                        input.readMessage(newArray3[i3]);
                        input.readTag();
                        i3++;
                    }
                    newArray3[i3] = new Pair();
                    input.readMessage(newArray3[i3]);
                    this.errnosCounters = newArray3;
                } else if (tag == 40) {
                    this.connectBlockingCount = input.readInt32();
                } else if (tag == 48) {
                    int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 48);
                    int i4 = this.nonBlockingLatenciesMs == null ? 0 : this.nonBlockingLatenciesMs.length;
                    int[] newArray4 = new int[(i4 + arrayLength4)];
                    if (i4 != 0) {
                        System.arraycopy(this.nonBlockingLatenciesMs, 0, newArray4, 0, i4);
                    }
                    while (i4 < newArray4.length - 1) {
                        newArray4[i4] = input.readInt32();
                        input.readTag();
                        i4++;
                    }
                    newArray4[i4] = input.readInt32();
                    this.nonBlockingLatenciesMs = newArray4;
                } else if (tag == 50) {
                    int limit2 = input.pushLimit(input.readRawVarint32());
                    int arrayLength5 = 0;
                    int startPos2 = input.getPosition();
                    while (input.getBytesUntilLimit() > 0) {
                        input.readInt32();
                        arrayLength5++;
                    }
                    input.rewindToPosition(startPos2);
                    int i5 = this.nonBlockingLatenciesMs == null ? 0 : this.nonBlockingLatenciesMs.length;
                    int[] newArray5 = new int[(i5 + arrayLength5)];
                    if (i5 != 0) {
                        System.arraycopy(this.nonBlockingLatenciesMs, 0, newArray5, 0, i5);
                    }
                    while (i5 < newArray5.length) {
                        newArray5[i5] = input.readInt32();
                        i5++;
                    }
                    this.nonBlockingLatenciesMs = newArray5;
                    input.popLimit(limit2);
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ConnectStatistics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new ConnectStatistics(), data);
        }

        public static ConnectStatistics parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ConnectStatistics().mergeFrom(input);
        }
    }

    public static final class DHCPEvent extends MessageNano {
        public static final int ERROR_CODE_FIELD_NUMBER = 3;
        public static final int STATE_TRANSITION_FIELD_NUMBER = 2;
        private static volatile DHCPEvent[] _emptyArray;
        public int durationMs;
        public String ifName;
        private int valueCase_ = 0;
        private Object value_;

        public int getValueCase() {
            return this.valueCase_;
        }

        public DHCPEvent clearValue() {
            this.valueCase_ = 0;
            this.value_ = null;
            return this;
        }

        public static DHCPEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DHCPEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public boolean hasStateTransition() {
            return this.valueCase_ == 2;
        }

        public String getStateTransition() {
            if (this.valueCase_ == 2) {
                return (String) this.value_;
            }
            return "";
        }

        public DHCPEvent setStateTransition(String value) {
            this.valueCase_ = 2;
            this.value_ = value;
            return this;
        }

        public boolean hasErrorCode() {
            return this.valueCase_ == 3;
        }

        public int getErrorCode() {
            if (this.valueCase_ == 3) {
                return ((Integer) this.value_).intValue();
            }
            return 0;
        }

        public DHCPEvent setErrorCode(int value) {
            this.valueCase_ = 3;
            this.value_ = Integer.valueOf(value);
            return this;
        }

        public DHCPEvent() {
            clear();
        }

        public DHCPEvent clear() {
            this.ifName = "";
            this.durationMs = 0;
            clearValue();
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals("")) {
                output.writeString(1, this.ifName);
            }
            if (this.valueCase_ == 2) {
                output.writeString(2, (String) this.value_);
            }
            if (this.valueCase_ == 3) {
                output.writeInt32(3, ((Integer) this.value_).intValue());
            }
            if (this.durationMs != 0) {
                output.writeInt32(4, this.durationMs);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (!this.ifName.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.ifName);
            }
            if (this.valueCase_ == 2) {
                size += CodedOutputByteBufferNano.computeStringSize(2, (String) this.value_);
            }
            if (this.valueCase_ == 3) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, ((Integer) this.value_).intValue());
            }
            if (this.durationMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, this.durationMs);
            }
            return size;
        }

        public DHCPEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.ifName = input.readString();
                } else if (tag == 18) {
                    this.value_ = input.readString();
                    this.valueCase_ = 2;
                } else if (tag == 24) {
                    this.value_ = Integer.valueOf(input.readInt32());
                    this.valueCase_ = 3;
                } else if (tag == 32) {
                    this.durationMs = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static DHCPEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new DHCPEvent(), data);
        }

        public static DHCPEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DHCPEvent().mergeFrom(input);
        }
    }

    public static final class DNSLatencies extends MessageNano {
        private static volatile DNSLatencies[] _emptyArray;
        public int aCount;
        public int aaaaCount;
        public int[] latenciesMs;
        public int queryCount;
        public int returnCode;
        public int type;

        public static DNSLatencies[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DNSLatencies[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DNSLatencies() {
            clear();
        }

        public DNSLatencies clear() {
            this.type = 0;
            this.returnCode = 0;
            this.queryCount = 0;
            this.aCount = 0;
            this.aaaaCount = 0;
            this.latenciesMs = WireFormatNano.EMPTY_INT_ARRAY;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.type != 0) {
                output.writeInt32(1, this.type);
            }
            if (this.returnCode != 0) {
                output.writeInt32(2, this.returnCode);
            }
            if (this.queryCount != 0) {
                output.writeInt32(3, this.queryCount);
            }
            if (this.aCount != 0) {
                output.writeInt32(4, this.aCount);
            }
            if (this.aaaaCount != 0) {
                output.writeInt32(5, this.aaaaCount);
            }
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                for (int writeInt32 : this.latenciesMs) {
                    output.writeInt32(6, writeInt32);
                }
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.type != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.type);
            }
            if (this.returnCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.returnCode);
            }
            if (this.queryCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.queryCount);
            }
            if (this.aCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.aCount);
            }
            if (this.aaaaCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.aaaaCount);
            }
            if (this.latenciesMs == null || this.latenciesMs.length <= 0) {
                return size;
            }
            int dataSize = 0;
            for (int element : this.latenciesMs) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
            }
            return size + dataSize + (1 * this.latenciesMs.length);
        }

        public DNSLatencies mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.type = input.readInt32();
                } else if (tag == 16) {
                    this.returnCode = input.readInt32();
                } else if (tag == 24) {
                    this.queryCount = input.readInt32();
                } else if (tag == 32) {
                    this.aCount = input.readInt32();
                } else if (tag == 40) {
                    this.aaaaCount = input.readInt32();
                } else if (tag == 48) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 48);
                    int i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                    int[] newArray = new int[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    newArray[i] = input.readInt32();
                    this.latenciesMs = newArray;
                } else if (tag == 50) {
                    int limit = input.pushLimit(input.readRawVarint32());
                    int arrayLength2 = 0;
                    int startPos = input.getPosition();
                    while (input.getBytesUntilLimit() > 0) {
                        input.readInt32();
                        arrayLength2++;
                    }
                    input.rewindToPosition(startPos);
                    int i2 = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                    int[] newArray2 = new int[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.latenciesMs, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length) {
                        newArray2[i2] = input.readInt32();
                        i2++;
                    }
                    this.latenciesMs = newArray2;
                    input.popLimit(limit);
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static DNSLatencies parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new DNSLatencies(), data);
        }

        public static DNSLatencies parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DNSLatencies().mergeFrom(input);
        }
    }

    public static final class DNSLookupBatch extends MessageNano {
        private static volatile DNSLookupBatch[] _emptyArray;
        public int[] eventTypes;
        public long getaddrinfoErrorCount;
        public Pair[] getaddrinfoErrors;
        public long getaddrinfoQueryCount;
        public long gethostbynameErrorCount;
        public Pair[] gethostbynameErrors;
        public long gethostbynameQueryCount;
        public int[] latenciesMs;
        public NetworkId networkId;
        public int[] returnCodes;

        public static DNSLookupBatch[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DNSLookupBatch[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DNSLookupBatch() {
            clear();
        }

        public DNSLookupBatch clear() {
            this.latenciesMs = WireFormatNano.EMPTY_INT_ARRAY;
            this.getaddrinfoQueryCount = 0;
            this.gethostbynameQueryCount = 0;
            this.getaddrinfoErrorCount = 0;
            this.gethostbynameErrorCount = 0;
            this.getaddrinfoErrors = Pair.emptyArray();
            this.gethostbynameErrors = Pair.emptyArray();
            this.networkId = null;
            this.eventTypes = WireFormatNano.EMPTY_INT_ARRAY;
            this.returnCodes = WireFormatNano.EMPTY_INT_ARRAY;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            int i = 0;
            if (this.eventTypes != null && this.eventTypes.length > 0) {
                for (int writeInt32 : this.eventTypes) {
                    output.writeInt32(2, writeInt32);
                }
            }
            if (this.returnCodes != null && this.returnCodes.length > 0) {
                for (int writeInt322 : this.returnCodes) {
                    output.writeInt32(3, writeInt322);
                }
            }
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                for (int writeInt323 : this.latenciesMs) {
                    output.writeInt32(4, writeInt323);
                }
            }
            if (this.getaddrinfoQueryCount != 0) {
                output.writeInt64(5, this.getaddrinfoQueryCount);
            }
            if (this.gethostbynameQueryCount != 0) {
                output.writeInt64(6, this.gethostbynameQueryCount);
            }
            if (this.getaddrinfoErrorCount != 0) {
                output.writeInt64(7, this.getaddrinfoErrorCount);
            }
            if (this.gethostbynameErrorCount != 0) {
                output.writeInt64(8, this.gethostbynameErrorCount);
            }
            if (this.getaddrinfoErrors != null && this.getaddrinfoErrors.length > 0) {
                for (Pair element : this.getaddrinfoErrors) {
                    if (element != null) {
                        output.writeMessage(9, element);
                    }
                }
            }
            if (this.gethostbynameErrors != null && this.gethostbynameErrors.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.gethostbynameErrors.length) {
                        break;
                    }
                    Pair element2 = this.gethostbynameErrors[i2];
                    if (element2 != null) {
                        output.writeMessage(10, element2);
                    }
                    i = i2 + 1;
                }
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            int i = 0;
            if (this.eventTypes != null && this.eventTypes.length > 0) {
                int dataSize = 0;
                for (int element : this.eventTypes) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = size + dataSize + (this.eventTypes.length * 1);
            }
            if (this.returnCodes != null && this.returnCodes.length > 0) {
                int dataSize2 = 0;
                for (int element2 : this.returnCodes) {
                    dataSize2 += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                }
                size = size + dataSize2 + (this.returnCodes.length * 1);
            }
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                int dataSize3 = 0;
                for (int element3 : this.latenciesMs) {
                    dataSize3 += CodedOutputByteBufferNano.computeInt32SizeNoTag(element3);
                }
                size = size + dataSize3 + (1 * this.latenciesMs.length);
            }
            if (this.getaddrinfoQueryCount != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.getaddrinfoQueryCount);
            }
            if (this.gethostbynameQueryCount != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, this.gethostbynameQueryCount);
            }
            if (this.getaddrinfoErrorCount != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, this.getaddrinfoErrorCount);
            }
            if (this.gethostbynameErrorCount != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, this.gethostbynameErrorCount);
            }
            if (this.getaddrinfoErrors != null && this.getaddrinfoErrors.length > 0) {
                int size2 = size;
                for (Pair element4 : this.getaddrinfoErrors) {
                    if (element4 != null) {
                        size2 += CodedOutputByteBufferNano.computeMessageSize(9, element4);
                    }
                }
                size = size2;
            }
            if (this.gethostbynameErrors != null && this.gethostbynameErrors.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.gethostbynameErrors.length) {
                        break;
                    }
                    Pair element5 = this.gethostbynameErrors[i2];
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(10, element5);
                    }
                    i = i2 + 1;
                }
            }
            return size;
        }

        public DNSLookupBatch mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.networkId == null) {
                            this.networkId = new NetworkId();
                        }
                        input.readMessage(this.networkId);
                        break;
                    case 16:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                        int i = this.eventTypes == null ? 0 : this.eventTypes.length;
                        int[] newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.eventTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.eventTypes = newArray;
                        break;
                    case 18:
                        int limit = input.pushLimit(input.readRawVarint32());
                        int arrayLength2 = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength2++;
                        }
                        input.rewindToPosition(startPos);
                        int i2 = this.eventTypes == null ? 0 : this.eventTypes.length;
                        int[] newArray2 = new int[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.eventTypes, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length) {
                            newArray2[i2] = input.readInt32();
                            i2++;
                        }
                        this.eventTypes = newArray2;
                        input.popLimit(limit);
                        break;
                    case 24:
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                        int i3 = this.returnCodes == null ? 0 : this.returnCodes.length;
                        int[] newArray3 = new int[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.returnCodes, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = input.readInt32();
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = input.readInt32();
                        this.returnCodes = newArray3;
                        break;
                    case 26:
                        int limit2 = input.pushLimit(input.readRawVarint32());
                        int arrayLength4 = 0;
                        int startPos2 = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength4++;
                        }
                        input.rewindToPosition(startPos2);
                        int i4 = this.returnCodes == null ? 0 : this.returnCodes.length;
                        int[] newArray4 = new int[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.returnCodes, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length) {
                            newArray4[i4] = input.readInt32();
                            i4++;
                        }
                        this.returnCodes = newArray4;
                        input.popLimit(limit2);
                        break;
                    case 32:
                        int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        int i5 = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        int[] newArray5 = new int[(i5 + arrayLength5)];
                        if (i5 != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray5, 0, i5);
                        }
                        while (i5 < newArray5.length - 1) {
                            newArray5[i5] = input.readInt32();
                            input.readTag();
                            i5++;
                        }
                        newArray5[i5] = input.readInt32();
                        this.latenciesMs = newArray5;
                        break;
                    case 34:
                        int limit3 = input.pushLimit(input.readRawVarint32());
                        int arrayLength6 = 0;
                        int startPos3 = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength6++;
                        }
                        input.rewindToPosition(startPos3);
                        int i6 = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        int[] newArray6 = new int[(i6 + arrayLength6)];
                        if (i6 != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray6, 0, i6);
                        }
                        while (i6 < newArray6.length) {
                            newArray6[i6] = input.readInt32();
                            i6++;
                        }
                        this.latenciesMs = newArray6;
                        input.popLimit(limit3);
                        break;
                    case 40:
                        this.getaddrinfoQueryCount = input.readInt64();
                        break;
                    case 48:
                        this.gethostbynameQueryCount = input.readInt64();
                        break;
                    case 56:
                        this.getaddrinfoErrorCount = input.readInt64();
                        break;
                    case 64:
                        this.gethostbynameErrorCount = input.readInt64();
                        break;
                    case 74:
                        int arrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(input, 74);
                        int i7 = this.getaddrinfoErrors == null ? 0 : this.getaddrinfoErrors.length;
                        Pair[] newArray7 = new Pair[(i7 + arrayLength7)];
                        if (i7 != 0) {
                            System.arraycopy(this.getaddrinfoErrors, 0, newArray7, 0, i7);
                        }
                        while (i7 < newArray7.length - 1) {
                            newArray7[i7] = new Pair();
                            input.readMessage(newArray7[i7]);
                            input.readTag();
                            i7++;
                        }
                        newArray7[i7] = new Pair();
                        input.readMessage(newArray7[i7]);
                        this.getaddrinfoErrors = newArray7;
                        break;
                    case 82:
                        int arrayLength8 = WireFormatNano.getRepeatedFieldArrayLength(input, 82);
                        int i8 = this.gethostbynameErrors == null ? 0 : this.gethostbynameErrors.length;
                        Pair[] newArray8 = new Pair[(i8 + arrayLength8)];
                        if (i8 != 0) {
                            System.arraycopy(this.gethostbynameErrors, 0, newArray8, 0, i8);
                        }
                        while (i8 < newArray8.length - 1) {
                            newArray8[i8] = new Pair();
                            input.readMessage(newArray8[i8]);
                            input.readTag();
                            i8++;
                        }
                        newArray8[i8] = new Pair();
                        input.readMessage(newArray8[i8]);
                        this.gethostbynameErrors = newArray8;
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

        public static DNSLookupBatch parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new DNSLookupBatch(), data);
        }

        public static DNSLookupBatch parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DNSLookupBatch().mergeFrom(input);
        }
    }

    public static final class DefaultNetworkEvent extends MessageNano {
        public static final int DISCONNECT = 3;
        public static final int DUAL = 3;
        public static final int INVALIDATION = 2;
        public static final int IPV4 = 1;
        public static final int IPV6 = 2;
        public static final int NONE = 0;
        public static final int OUTSCORED = 1;
        public static final int UNKNOWN = 0;
        private static volatile DefaultNetworkEvent[] _emptyArray;
        public long defaultNetworkDurationMs;
        public long finalScore;
        public long initialScore;
        public int ipSupport;
        public NetworkId networkId;
        public long noDefaultNetworkDurationMs;
        public int previousDefaultNetworkLinkLayer;
        public NetworkId previousNetworkId;
        public int previousNetworkIpSupport;
        public int[] transportTypes;
        public long validationDurationMs;

        public static DefaultNetworkEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DefaultNetworkEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DefaultNetworkEvent() {
            clear();
        }

        public DefaultNetworkEvent clear() {
            this.defaultNetworkDurationMs = 0;
            this.validationDurationMs = 0;
            this.initialScore = 0;
            this.finalScore = 0;
            this.ipSupport = 0;
            this.previousDefaultNetworkLinkLayer = 0;
            this.networkId = null;
            this.previousNetworkId = null;
            this.previousNetworkIpSupport = 0;
            this.transportTypes = WireFormatNano.EMPTY_INT_ARRAY;
            this.noDefaultNetworkDurationMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.previousNetworkId != null) {
                output.writeMessage(2, this.previousNetworkId);
            }
            if (this.previousNetworkIpSupport != 0) {
                output.writeInt32(3, this.previousNetworkIpSupport);
            }
            if (this.transportTypes != null && this.transportTypes.length > 0) {
                for (int writeInt32 : this.transportTypes) {
                    output.writeInt32(4, writeInt32);
                }
            }
            if (this.defaultNetworkDurationMs != 0) {
                output.writeInt64(5, this.defaultNetworkDurationMs);
            }
            if (this.noDefaultNetworkDurationMs != 0) {
                output.writeInt64(6, this.noDefaultNetworkDurationMs);
            }
            if (this.initialScore != 0) {
                output.writeInt64(7, this.initialScore);
            }
            if (this.finalScore != 0) {
                output.writeInt64(8, this.finalScore);
            }
            if (this.ipSupport != 0) {
                output.writeInt32(9, this.ipSupport);
            }
            if (this.previousDefaultNetworkLinkLayer != 0) {
                output.writeInt32(10, this.previousDefaultNetworkLinkLayer);
            }
            if (this.validationDurationMs != 0) {
                output.writeInt64(11, this.validationDurationMs);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.previousNetworkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.previousNetworkId);
            }
            if (this.previousNetworkIpSupport != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.previousNetworkIpSupport);
            }
            if (this.transportTypes != null && this.transportTypes.length > 0) {
                int dataSize = 0;
                for (int element : this.transportTypes) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = size + dataSize + (1 * this.transportTypes.length);
            }
            if (this.defaultNetworkDurationMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.defaultNetworkDurationMs);
            }
            if (this.noDefaultNetworkDurationMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, this.noDefaultNetworkDurationMs);
            }
            if (this.initialScore != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, this.initialScore);
            }
            if (this.finalScore != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, this.finalScore);
            }
            if (this.ipSupport != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.ipSupport);
            }
            if (this.previousDefaultNetworkLinkLayer != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(10, this.previousDefaultNetworkLinkLayer);
            }
            if (this.validationDurationMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(11, this.validationDurationMs);
            }
            return size;
        }

        public DefaultNetworkEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.networkId == null) {
                            this.networkId = new NetworkId();
                        }
                        input.readMessage(this.networkId);
                        break;
                    case 18:
                        if (this.previousNetworkId == null) {
                            this.previousNetworkId = new NetworkId();
                        }
                        input.readMessage(this.previousNetworkId);
                        break;
                    case 24:
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.previousNetworkIpSupport = value;
                                break;
                        }
                    case 32:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        int i = this.transportTypes == null ? 0 : this.transportTypes.length;
                        int[] newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.transportTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.transportTypes = newArray;
                        break;
                    case 34:
                        int limit = input.pushLimit(input.readRawVarint32());
                        int arrayLength2 = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength2++;
                        }
                        input.rewindToPosition(startPos);
                        int i2 = this.transportTypes == null ? 0 : this.transportTypes.length;
                        int[] newArray2 = new int[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.transportTypes, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length) {
                            newArray2[i2] = input.readInt32();
                            i2++;
                        }
                        this.transportTypes = newArray2;
                        input.popLimit(limit);
                        break;
                    case 40:
                        this.defaultNetworkDurationMs = input.readInt64();
                        break;
                    case 48:
                        this.noDefaultNetworkDurationMs = input.readInt64();
                        break;
                    case 56:
                        this.initialScore = input.readInt64();
                        break;
                    case 64:
                        this.finalScore = input.readInt64();
                        break;
                    case 72:
                        int value2 = input.readInt32();
                        switch (value2) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.ipSupport = value2;
                                break;
                        }
                    case 80:
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
                                this.previousDefaultNetworkLinkLayer = value3;
                                break;
                        }
                    case 88:
                        this.validationDurationMs = input.readInt64();
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

        public static DefaultNetworkEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new DefaultNetworkEvent(), data);
        }

        public static DefaultNetworkEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DefaultNetworkEvent().mergeFrom(input);
        }
    }

    public static final class IpConnectivityEvent extends MessageNano {
        public static final int APF_PROGRAM_EVENT_FIELD_NUMBER = 9;
        public static final int APF_STATISTICS_FIELD_NUMBER = 10;
        public static final int CONNECT_STATISTICS_FIELD_NUMBER = 14;
        public static final int DEFAULT_NETWORK_EVENT_FIELD_NUMBER = 2;
        public static final int DHCP_EVENT_FIELD_NUMBER = 6;
        public static final int DNS_LATENCIES_FIELD_NUMBER = 13;
        public static final int DNS_LOOKUP_BATCH_FIELD_NUMBER = 5;
        public static final int IP_PROVISIONING_EVENT_FIELD_NUMBER = 7;
        public static final int IP_REACHABILITY_EVENT_FIELD_NUMBER = 3;
        public static final int NETWORK_EVENT_FIELD_NUMBER = 4;
        public static final int NETWORK_STATS_FIELD_NUMBER = 19;
        public static final int RA_EVENT_FIELD_NUMBER = 11;
        public static final int VALIDATION_PROBE_EVENT_FIELD_NUMBER = 8;
        public static final int WAKEUP_STATS_FIELD_NUMBER = 20;
        private static volatile IpConnectivityEvent[] _emptyArray;
        private int eventCase_ = 0;
        private Object event_;
        public String ifName;
        public int linkLayer;
        public int networkId;
        public long timeMs;
        public long transports;

        public int getEventCase() {
            return this.eventCase_;
        }

        public IpConnectivityEvent clearEvent() {
            this.eventCase_ = 0;
            this.event_ = null;
            return this;
        }

        public static IpConnectivityEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpConnectivityEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public boolean hasDefaultNetworkEvent() {
            return this.eventCase_ == 2;
        }

        public DefaultNetworkEvent getDefaultNetworkEvent() {
            if (this.eventCase_ == 2) {
                return (DefaultNetworkEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setDefaultNetworkEvent(DefaultNetworkEvent value) {
            if (value != null) {
                this.eventCase_ = 2;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasIpReachabilityEvent() {
            return this.eventCase_ == 3;
        }

        public IpReachabilityEvent getIpReachabilityEvent() {
            if (this.eventCase_ == 3) {
                return (IpReachabilityEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setIpReachabilityEvent(IpReachabilityEvent value) {
            if (value != null) {
                this.eventCase_ = 3;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasNetworkEvent() {
            return this.eventCase_ == 4;
        }

        public NetworkEvent getNetworkEvent() {
            if (this.eventCase_ == 4) {
                return (NetworkEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setNetworkEvent(NetworkEvent value) {
            if (value != null) {
                this.eventCase_ = 4;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasDnsLookupBatch() {
            return this.eventCase_ == 5;
        }

        public DNSLookupBatch getDnsLookupBatch() {
            if (this.eventCase_ == 5) {
                return (DNSLookupBatch) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setDnsLookupBatch(DNSLookupBatch value) {
            if (value != null) {
                this.eventCase_ = 5;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasDnsLatencies() {
            return this.eventCase_ == 13;
        }

        public DNSLatencies getDnsLatencies() {
            if (this.eventCase_ == 13) {
                return (DNSLatencies) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setDnsLatencies(DNSLatencies value) {
            if (value != null) {
                this.eventCase_ = 13;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasConnectStatistics() {
            return this.eventCase_ == 14;
        }

        public ConnectStatistics getConnectStatistics() {
            if (this.eventCase_ == 14) {
                return (ConnectStatistics) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setConnectStatistics(ConnectStatistics value) {
            if (value != null) {
                this.eventCase_ = 14;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasDhcpEvent() {
            return this.eventCase_ == 6;
        }

        public DHCPEvent getDhcpEvent() {
            if (this.eventCase_ == 6) {
                return (DHCPEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setDhcpEvent(DHCPEvent value) {
            if (value != null) {
                this.eventCase_ = 6;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasIpProvisioningEvent() {
            return this.eventCase_ == 7;
        }

        public IpProvisioningEvent getIpProvisioningEvent() {
            if (this.eventCase_ == 7) {
                return (IpProvisioningEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setIpProvisioningEvent(IpProvisioningEvent value) {
            if (value != null) {
                this.eventCase_ = 7;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasValidationProbeEvent() {
            return this.eventCase_ == 8;
        }

        public ValidationProbeEvent getValidationProbeEvent() {
            if (this.eventCase_ == 8) {
                return (ValidationProbeEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setValidationProbeEvent(ValidationProbeEvent value) {
            if (value != null) {
                this.eventCase_ = 8;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasApfProgramEvent() {
            return this.eventCase_ == 9;
        }

        public ApfProgramEvent getApfProgramEvent() {
            if (this.eventCase_ == 9) {
                return (ApfProgramEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setApfProgramEvent(ApfProgramEvent value) {
            if (value != null) {
                this.eventCase_ = 9;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasApfStatistics() {
            return this.eventCase_ == 10;
        }

        public ApfStatistics getApfStatistics() {
            if (this.eventCase_ == 10) {
                return (ApfStatistics) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setApfStatistics(ApfStatistics value) {
            if (value != null) {
                this.eventCase_ = 10;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasRaEvent() {
            return this.eventCase_ == 11;
        }

        public RaEvent getRaEvent() {
            if (this.eventCase_ == 11) {
                return (RaEvent) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setRaEvent(RaEvent value) {
            if (value != null) {
                this.eventCase_ = 11;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasNetworkStats() {
            return this.eventCase_ == 19;
        }

        public NetworkStats getNetworkStats() {
            if (this.eventCase_ == 19) {
                return (NetworkStats) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setNetworkStats(NetworkStats value) {
            if (value != null) {
                this.eventCase_ = 19;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public boolean hasWakeupStats() {
            return this.eventCase_ == 20;
        }

        public WakeupStats getWakeupStats() {
            if (this.eventCase_ == 20) {
                return (WakeupStats) this.event_;
            }
            return null;
        }

        public IpConnectivityEvent setWakeupStats(WakeupStats value) {
            if (value != null) {
                this.eventCase_ = 20;
                this.event_ = value;
                return this;
            }
            throw new NullPointerException();
        }

        public IpConnectivityEvent() {
            clear();
        }

        public IpConnectivityEvent clear() {
            this.timeMs = 0;
            this.linkLayer = 0;
            this.networkId = 0;
            this.ifName = "";
            this.transports = 0;
            clearEvent();
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.timeMs != 0) {
                output.writeInt64(1, this.timeMs);
            }
            if (this.eventCase_ == 2) {
                output.writeMessage(2, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 3) {
                output.writeMessage(3, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 4) {
                output.writeMessage(4, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 5) {
                output.writeMessage(5, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 6) {
                output.writeMessage(6, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 7) {
                output.writeMessage(7, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 8) {
                output.writeMessage(8, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 9) {
                output.writeMessage(9, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 10) {
                output.writeMessage(10, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 11) {
                output.writeMessage(11, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 13) {
                output.writeMessage(13, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 14) {
                output.writeMessage(14, (MessageNano) this.event_);
            }
            if (this.linkLayer != 0) {
                output.writeInt32(15, this.linkLayer);
            }
            if (this.networkId != 0) {
                output.writeInt32(16, this.networkId);
            }
            if (!this.ifName.equals("")) {
                output.writeString(17, this.ifName);
            }
            if (this.transports != 0) {
                output.writeInt64(18, this.transports);
            }
            if (this.eventCase_ == 19) {
                output.writeMessage(19, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 20) {
                output.writeMessage(20, (MessageNano) this.event_);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.timeMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.timeMs);
            }
            if (this.eventCase_ == 2) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 3) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 4) {
                size += CodedOutputByteBufferNano.computeMessageSize(4, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 5) {
                size += CodedOutputByteBufferNano.computeMessageSize(5, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 6) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 7) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 8) {
                size += CodedOutputByteBufferNano.computeMessageSize(8, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 9) {
                size += CodedOutputByteBufferNano.computeMessageSize(9, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 10) {
                size += CodedOutputByteBufferNano.computeMessageSize(10, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 11) {
                size += CodedOutputByteBufferNano.computeMessageSize(11, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 13) {
                size += CodedOutputByteBufferNano.computeMessageSize(13, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 14) {
                size += CodedOutputByteBufferNano.computeMessageSize(14, (MessageNano) this.event_);
            }
            if (this.linkLayer != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(15, this.linkLayer);
            }
            if (this.networkId != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(16, this.networkId);
            }
            if (!this.ifName.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(17, this.ifName);
            }
            if (this.transports != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(18, this.transports);
            }
            if (this.eventCase_ == 19) {
                size += CodedOutputByteBufferNano.computeMessageSize(19, (MessageNano) this.event_);
            }
            if (this.eventCase_ == 20) {
                return size + CodedOutputByteBufferNano.computeMessageSize(20, (MessageNano) this.event_);
            }
            return size;
        }

        public IpConnectivityEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.timeMs = input.readInt64();
                        break;
                    case 18:
                        if (this.eventCase_ != 2) {
                            this.event_ = new DefaultNetworkEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 2;
                        break;
                    case 26:
                        if (this.eventCase_ != 3) {
                            this.event_ = new IpReachabilityEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 3;
                        break;
                    case 34:
                        if (this.eventCase_ != 4) {
                            this.event_ = new NetworkEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 4;
                        break;
                    case 42:
                        if (this.eventCase_ != 5) {
                            this.event_ = new DNSLookupBatch();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 5;
                        break;
                    case 50:
                        if (this.eventCase_ != 6) {
                            this.event_ = new DHCPEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 6;
                        break;
                    case 58:
                        if (this.eventCase_ != 7) {
                            this.event_ = new IpProvisioningEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 7;
                        break;
                    case 66:
                        if (this.eventCase_ != 8) {
                            this.event_ = new ValidationProbeEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 8;
                        break;
                    case 74:
                        if (this.eventCase_ != 9) {
                            this.event_ = new ApfProgramEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 9;
                        break;
                    case 82:
                        if (this.eventCase_ != 10) {
                            this.event_ = new ApfStatistics();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 10;
                        break;
                    case 90:
                        if (this.eventCase_ != 11) {
                            this.event_ = new RaEvent();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 11;
                        break;
                    case 106:
                        if (this.eventCase_ != 13) {
                            this.event_ = new DNSLatencies();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 13;
                        break;
                    case 114:
                        if (this.eventCase_ != 14) {
                            this.event_ = new ConnectStatistics();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 14;
                        break;
                    case 120:
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
                                this.linkLayer = value;
                                break;
                        }
                    case 128:
                        this.networkId = input.readInt32();
                        break;
                    case 138:
                        this.ifName = input.readString();
                        break;
                    case 144:
                        this.transports = input.readInt64();
                        break;
                    case 154:
                        if (this.eventCase_ != 19) {
                            this.event_ = new NetworkStats();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 19;
                        break;
                    case 162:
                        if (this.eventCase_ != 20) {
                            this.event_ = new WakeupStats();
                        }
                        input.readMessage((MessageNano) this.event_);
                        this.eventCase_ = 20;
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

        public static IpConnectivityEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new IpConnectivityEvent(), data);
        }

        public static IpConnectivityEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpConnectivityEvent().mergeFrom(input);
        }
    }

    public static final class IpConnectivityLog extends MessageNano {
        private static volatile IpConnectivityLog[] _emptyArray;
        public int droppedEvents;
        public IpConnectivityEvent[] events;
        public int version;

        public static IpConnectivityLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpConnectivityLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpConnectivityLog() {
            clear();
        }

        public IpConnectivityLog clear() {
            this.events = IpConnectivityEvent.emptyArray();
            this.droppedEvents = 0;
            this.version = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.events != null && this.events.length > 0) {
                for (IpConnectivityEvent element : this.events) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            if (this.droppedEvents != 0) {
                output.writeInt32(2, this.droppedEvents);
            }
            if (this.version != 0) {
                output.writeInt32(3, this.version);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.events != null && this.events.length > 0) {
                for (IpConnectivityEvent element : this.events) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            if (this.droppedEvents != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.droppedEvents);
            }
            if (this.version != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, this.version);
            }
            return size;
        }

        public IpConnectivityLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = this.events == null ? 0 : this.events.length;
                    IpConnectivityEvent[] newArray = new IpConnectivityEvent[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.events, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new IpConnectivityEvent();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new IpConnectivityEvent();
                    input.readMessage(newArray[i]);
                    this.events = newArray;
                } else if (tag == 16) {
                    this.droppedEvents = input.readInt32();
                } else if (tag == 24) {
                    this.version = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static IpConnectivityLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new IpConnectivityLog(), data);
        }

        public static IpConnectivityLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpConnectivityLog().mergeFrom(input);
        }
    }

    public static final class IpProvisioningEvent extends MessageNano {
        private static volatile IpProvisioningEvent[] _emptyArray;
        public int eventType;
        public String ifName;
        public int latencyMs;

        public static IpProvisioningEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpProvisioningEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpProvisioningEvent() {
            clear();
        }

        public IpProvisioningEvent clear() {
            this.ifName = "";
            this.eventType = 0;
            this.latencyMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals("")) {
                output.writeString(1, this.ifName);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(3, this.latencyMs);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (!this.ifName.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.ifName);
            }
            if (this.eventType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, this.latencyMs);
            }
            return size;
        }

        public IpProvisioningEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.ifName = input.readString();
                } else if (tag == 16) {
                    this.eventType = input.readInt32();
                } else if (tag == 24) {
                    this.latencyMs = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static IpProvisioningEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new IpProvisioningEvent(), data);
        }

        public static IpProvisioningEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpProvisioningEvent().mergeFrom(input);
        }
    }

    public static final class IpReachabilityEvent extends MessageNano {
        private static volatile IpReachabilityEvent[] _emptyArray;
        public int eventType;
        public String ifName;

        public static IpReachabilityEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new IpReachabilityEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public IpReachabilityEvent() {
            clear();
        }

        public IpReachabilityEvent clear() {
            this.ifName = "";
            this.eventType = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals("")) {
                output.writeString(1, this.ifName);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (!this.ifName.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.ifName);
            }
            if (this.eventType != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, this.eventType);
            }
            return size;
        }

        public IpReachabilityEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.ifName = input.readString();
                } else if (tag == 16) {
                    this.eventType = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static IpReachabilityEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new IpReachabilityEvent(), data);
        }

        public static IpReachabilityEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new IpReachabilityEvent().mergeFrom(input);
        }
    }

    public static final class NetworkEvent extends MessageNano {
        private static volatile NetworkEvent[] _emptyArray;
        public int eventType;
        public int latencyMs;
        public NetworkId networkId;

        public static NetworkEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NetworkEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NetworkEvent() {
            clear();
        }

        public NetworkEvent clear() {
            this.networkId = null;
            this.eventType = 0;
            this.latencyMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(3, this.latencyMs);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.eventType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, this.latencyMs);
            }
            return size;
        }

        public NetworkEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    if (this.networkId == null) {
                        this.networkId = new NetworkId();
                    }
                    input.readMessage(this.networkId);
                } else if (tag == 16) {
                    this.eventType = input.readInt32();
                } else if (tag == 24) {
                    this.latencyMs = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static NetworkEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new NetworkEvent(), data);
        }

        public static NetworkEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkEvent().mergeFrom(input);
        }
    }

    public static final class NetworkId extends MessageNano {
        private static volatile NetworkId[] _emptyArray;
        public int networkId;

        public static NetworkId[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NetworkId[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NetworkId() {
            clear();
        }

        public NetworkId clear() {
            this.networkId = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != 0) {
                output.writeInt32(1, this.networkId);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.networkId != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(1, this.networkId);
            }
            return size;
        }

        public NetworkId mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.networkId = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static NetworkId parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new NetworkId(), data);
        }

        public static NetworkId parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkId().mergeFrom(input);
        }
    }

    public static final class NetworkStats extends MessageNano {
        private static volatile NetworkStats[] _emptyArray;
        public long durationMs;
        public boolean everValidated;
        public int ipSupport;
        public int noConnectivityReports;
        public boolean portalFound;
        public int validationAttempts;
        public Pair[] validationEvents;
        public Pair[] validationStates;

        public static NetworkStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new NetworkStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public NetworkStats() {
            clear();
        }

        public NetworkStats clear() {
            this.durationMs = 0;
            this.ipSupport = 0;
            this.everValidated = false;
            this.portalFound = false;
            this.noConnectivityReports = 0;
            this.validationAttempts = 0;
            this.validationEvents = Pair.emptyArray();
            this.validationStates = Pair.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.durationMs != 0) {
                output.writeInt64(1, this.durationMs);
            }
            if (this.ipSupport != 0) {
                output.writeInt32(2, this.ipSupport);
            }
            if (this.everValidated) {
                output.writeBool(3, this.everValidated);
            }
            if (this.portalFound) {
                output.writeBool(4, this.portalFound);
            }
            if (this.noConnectivityReports != 0) {
                output.writeInt32(5, this.noConnectivityReports);
            }
            if (this.validationAttempts != 0) {
                output.writeInt32(6, this.validationAttempts);
            }
            int i = 0;
            if (this.validationEvents != null && this.validationEvents.length > 0) {
                for (Pair element : this.validationEvents) {
                    if (element != null) {
                        output.writeMessage(7, element);
                    }
                }
            }
            if (this.validationStates != null && this.validationStates.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.validationStates.length) {
                        break;
                    }
                    Pair element2 = this.validationStates[i2];
                    if (element2 != null) {
                        output.writeMessage(8, element2);
                    }
                    i = i2 + 1;
                }
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.durationMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.durationMs);
            }
            if (this.ipSupport != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.ipSupport);
            }
            if (this.everValidated) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, this.everValidated);
            }
            if (this.portalFound) {
                size += CodedOutputByteBufferNano.computeBoolSize(4, this.portalFound);
            }
            if (this.noConnectivityReports != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.noConnectivityReports);
            }
            if (this.validationAttempts != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.validationAttempts);
            }
            int i = 0;
            if (this.validationEvents != null && this.validationEvents.length > 0) {
                int size2 = size;
                for (Pair element : this.validationEvents) {
                    if (element != null) {
                        size2 += CodedOutputByteBufferNano.computeMessageSize(7, element);
                    }
                }
                size = size2;
            }
            if (this.validationStates != null && this.validationStates.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.validationStates.length) {
                        break;
                    }
                    Pair element2 = this.validationStates[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(8, element2);
                    }
                    i = i2 + 1;
                }
            }
            return size;
        }

        public NetworkStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag != 8) {
                    if (tag == 16) {
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.ipSupport = value;
                                break;
                        }
                    } else if (tag == 24) {
                        this.everValidated = input.readBool();
                    } else if (tag == 32) {
                        this.portalFound = input.readBool();
                    } else if (tag == 40) {
                        this.noConnectivityReports = input.readInt32();
                    } else if (tag == 48) {
                        this.validationAttempts = input.readInt32();
                    } else if (tag == 58) {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                        int i = this.validationEvents == null ? 0 : this.validationEvents.length;
                        Pair[] newArray = new Pair[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.validationEvents, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new Pair();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new Pair();
                        input.readMessage(newArray[i]);
                        this.validationEvents = newArray;
                    } else if (tag == 66) {
                        int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                        int i2 = this.validationStates == null ? 0 : this.validationStates.length;
                        Pair[] newArray2 = new Pair[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.validationStates, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length - 1) {
                            newArray2[i2] = new Pair();
                            input.readMessage(newArray2[i2]);
                            input.readTag();
                            i2++;
                        }
                        newArray2[i2] = new Pair();
                        input.readMessage(newArray2[i2]);
                        this.validationStates = newArray2;
                    } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                        return this;
                    }
                } else {
                    this.durationMs = input.readInt64();
                }
            }
        }

        public static NetworkStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new NetworkStats(), data);
        }

        public static NetworkStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkStats().mergeFrom(input);
        }
    }

    public static final class Pair extends MessageNano {
        private static volatile Pair[] _emptyArray;
        public int key;
        public int value;

        public static Pair[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Pair[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Pair() {
            clear();
        }

        public Pair clear() {
            this.key = 0;
            this.value = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.key != 0) {
                output.writeInt32(1, this.key);
            }
            if (this.value != 0) {
                output.writeInt32(2, this.value);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.key != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.key);
            }
            if (this.value != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, this.value);
            }
            return size;
        }

        public Pair mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.key = input.readInt32();
                } else if (tag == 16) {
                    this.value = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static Pair parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new Pair(), data);
        }

        public static Pair parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Pair().mergeFrom(input);
        }
    }

    public static final class RaEvent extends MessageNano {
        private static volatile RaEvent[] _emptyArray;
        public long dnsslLifetime;
        public long prefixPreferredLifetime;
        public long prefixValidLifetime;
        public long rdnssLifetime;
        public long routeInfoLifetime;
        public long routerLifetime;

        public static RaEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RaEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public RaEvent() {
            clear();
        }

        public RaEvent clear() {
            this.routerLifetime = 0;
            this.prefixValidLifetime = 0;
            this.prefixPreferredLifetime = 0;
            this.routeInfoLifetime = 0;
            this.rdnssLifetime = 0;
            this.dnsslLifetime = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.routerLifetime != 0) {
                output.writeInt64(1, this.routerLifetime);
            }
            if (this.prefixValidLifetime != 0) {
                output.writeInt64(2, this.prefixValidLifetime);
            }
            if (this.prefixPreferredLifetime != 0) {
                output.writeInt64(3, this.prefixPreferredLifetime);
            }
            if (this.routeInfoLifetime != 0) {
                output.writeInt64(4, this.routeInfoLifetime);
            }
            if (this.rdnssLifetime != 0) {
                output.writeInt64(5, this.rdnssLifetime);
            }
            if (this.dnsslLifetime != 0) {
                output.writeInt64(6, this.dnsslLifetime);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.routerLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.routerLifetime);
            }
            if (this.prefixValidLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, this.prefixValidLifetime);
            }
            if (this.prefixPreferredLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, this.prefixPreferredLifetime);
            }
            if (this.routeInfoLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, this.routeInfoLifetime);
            }
            if (this.rdnssLifetime != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.rdnssLifetime);
            }
            if (this.dnsslLifetime != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(6, this.dnsslLifetime);
            }
            return size;
        }

        public RaEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.routerLifetime = input.readInt64();
                } else if (tag == 16) {
                    this.prefixValidLifetime = input.readInt64();
                } else if (tag == 24) {
                    this.prefixPreferredLifetime = input.readInt64();
                } else if (tag == 32) {
                    this.routeInfoLifetime = input.readInt64();
                } else if (tag == 40) {
                    this.rdnssLifetime = input.readInt64();
                } else if (tag == 48) {
                    this.dnsslLifetime = input.readInt64();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static RaEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new RaEvent(), data);
        }

        public static RaEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new RaEvent().mergeFrom(input);
        }
    }

    public static final class ValidationProbeEvent extends MessageNano {
        private static volatile ValidationProbeEvent[] _emptyArray;
        public int latencyMs;
        public NetworkId networkId;
        public int probeResult;
        public int probeType;

        public static ValidationProbeEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ValidationProbeEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ValidationProbeEvent() {
            clear();
        }

        public ValidationProbeEvent clear() {
            this.networkId = null;
            this.latencyMs = 0;
            this.probeType = 0;
            this.probeResult = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(2, this.latencyMs);
            }
            if (this.probeType != 0) {
                output.writeInt32(3, this.probeType);
            }
            if (this.probeResult != 0) {
                output.writeInt32(4, this.probeResult);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.latencyMs != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.latencyMs);
            }
            if (this.probeType != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.probeType);
            }
            if (this.probeResult != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, this.probeResult);
            }
            return size;
        }

        public ValidationProbeEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    if (this.networkId == null) {
                        this.networkId = new NetworkId();
                    }
                    input.readMessage(this.networkId);
                } else if (tag == 16) {
                    this.latencyMs = input.readInt32();
                } else if (tag == 24) {
                    this.probeType = input.readInt32();
                } else if (tag == 32) {
                    this.probeResult = input.readInt32();
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ValidationProbeEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new ValidationProbeEvent(), data);
        }

        public static ValidationProbeEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ValidationProbeEvent().mergeFrom(input);
        }
    }

    public static final class WakeupStats extends MessageNano {
        private static volatile WakeupStats[] _emptyArray;
        public long applicationWakeups;
        public long durationSec;
        public Pair[] ethertypeCounts;
        public Pair[] ipNextHeaderCounts;
        public long l2BroadcastCount;
        public long l2MulticastCount;
        public long l2UnicastCount;
        public long noUidWakeups;
        public long nonApplicationWakeups;
        public long rootWakeups;
        public long systemWakeups;
        public long totalWakeups;

        public static WakeupStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new WakeupStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public WakeupStats() {
            clear();
        }

        public WakeupStats clear() {
            this.durationSec = 0;
            this.totalWakeups = 0;
            this.rootWakeups = 0;
            this.systemWakeups = 0;
            this.applicationWakeups = 0;
            this.nonApplicationWakeups = 0;
            this.noUidWakeups = 0;
            this.ethertypeCounts = Pair.emptyArray();
            this.ipNextHeaderCounts = Pair.emptyArray();
            this.l2UnicastCount = 0;
            this.l2MulticastCount = 0;
            this.l2BroadcastCount = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.durationSec != 0) {
                output.writeInt64(1, this.durationSec);
            }
            if (this.totalWakeups != 0) {
                output.writeInt64(2, this.totalWakeups);
            }
            if (this.rootWakeups != 0) {
                output.writeInt64(3, this.rootWakeups);
            }
            if (this.systemWakeups != 0) {
                output.writeInt64(4, this.systemWakeups);
            }
            if (this.applicationWakeups != 0) {
                output.writeInt64(5, this.applicationWakeups);
            }
            if (this.nonApplicationWakeups != 0) {
                output.writeInt64(6, this.nonApplicationWakeups);
            }
            if (this.noUidWakeups != 0) {
                output.writeInt64(7, this.noUidWakeups);
            }
            int i = 0;
            if (this.ethertypeCounts != null && this.ethertypeCounts.length > 0) {
                for (Pair element : this.ethertypeCounts) {
                    if (element != null) {
                        output.writeMessage(8, element);
                    }
                }
            }
            if (this.ipNextHeaderCounts != null && this.ipNextHeaderCounts.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.ipNextHeaderCounts.length) {
                        break;
                    }
                    Pair element2 = this.ipNextHeaderCounts[i2];
                    if (element2 != null) {
                        output.writeMessage(9, element2);
                    }
                    i = i2 + 1;
                }
            }
            if (this.l2UnicastCount != 0) {
                output.writeInt64(10, this.l2UnicastCount);
            }
            if (this.l2MulticastCount != 0) {
                output.writeInt64(11, this.l2MulticastCount);
            }
            if (this.l2BroadcastCount != 0) {
                output.writeInt64(12, this.l2BroadcastCount);
            }
            IpConnectivityLogClass.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = IpConnectivityLogClass.super.computeSerializedSize();
            if (this.durationSec != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.durationSec);
            }
            if (this.totalWakeups != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, this.totalWakeups);
            }
            if (this.rootWakeups != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, this.rootWakeups);
            }
            if (this.systemWakeups != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, this.systemWakeups);
            }
            if (this.applicationWakeups != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.applicationWakeups);
            }
            if (this.nonApplicationWakeups != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, this.nonApplicationWakeups);
            }
            if (this.noUidWakeups != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, this.noUidWakeups);
            }
            int i = 0;
            if (this.ethertypeCounts != null && this.ethertypeCounts.length > 0) {
                int size2 = size;
                for (Pair element : this.ethertypeCounts) {
                    if (element != null) {
                        size2 += CodedOutputByteBufferNano.computeMessageSize(8, element);
                    }
                }
                size = size2;
            }
            if (this.ipNextHeaderCounts != null && this.ipNextHeaderCounts.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.ipNextHeaderCounts.length) {
                        break;
                    }
                    Pair element2 = this.ipNextHeaderCounts[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(9, element2);
                    }
                    i = i2 + 1;
                }
            }
            if (this.l2UnicastCount != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(10, this.l2UnicastCount);
            }
            if (this.l2MulticastCount != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(11, this.l2MulticastCount);
            }
            if (this.l2BroadcastCount != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(12, this.l2BroadcastCount);
            }
            return size;
        }

        public WakeupStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.durationSec = input.readInt64();
                        break;
                    case 16:
                        this.totalWakeups = input.readInt64();
                        break;
                    case 24:
                        this.rootWakeups = input.readInt64();
                        break;
                    case 32:
                        this.systemWakeups = input.readInt64();
                        break;
                    case 40:
                        this.applicationWakeups = input.readInt64();
                        break;
                    case 48:
                        this.nonApplicationWakeups = input.readInt64();
                        break;
                    case 56:
                        this.noUidWakeups = input.readInt64();
                        break;
                    case 66:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                        int i = this.ethertypeCounts == null ? 0 : this.ethertypeCounts.length;
                        Pair[] newArray = new Pair[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.ethertypeCounts, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new Pair();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new Pair();
                        input.readMessage(newArray[i]);
                        this.ethertypeCounts = newArray;
                        break;
                    case 74:
                        int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 74);
                        int i2 = this.ipNextHeaderCounts == null ? 0 : this.ipNextHeaderCounts.length;
                        Pair[] newArray2 = new Pair[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.ipNextHeaderCounts, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length - 1) {
                            newArray2[i2] = new Pair();
                            input.readMessage(newArray2[i2]);
                            input.readTag();
                            i2++;
                        }
                        newArray2[i2] = new Pair();
                        input.readMessage(newArray2[i2]);
                        this.ipNextHeaderCounts = newArray2;
                        break;
                    case 80:
                        this.l2UnicastCount = input.readInt64();
                        break;
                    case 88:
                        this.l2MulticastCount = input.readInt64();
                        break;
                    case 96:
                        this.l2BroadcastCount = input.readInt64();
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

        public static WakeupStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new WakeupStats(), data);
        }

        public static WakeupStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new WakeupStats().mergeFrom(input);
        }
    }
}
