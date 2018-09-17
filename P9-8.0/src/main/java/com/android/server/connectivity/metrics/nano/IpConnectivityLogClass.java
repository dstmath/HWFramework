package com.android.server.connectivity.metrics.nano;

import android.util.LogException;
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
    public static final int MULTIPLE = 6;
    public static final int NONE = 5;
    public static final int UNKNOWN = 0;
    public static final int WIFI = 4;

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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.lifetime = input.readInt64();
                        break;
                    case 16:
                        this.filteredRas = input.readInt32();
                        break;
                    case 24:
                        this.currentRas = input.readInt32();
                        break;
                    case 32:
                        this.programLength = input.readInt32();
                        break;
                    case 40:
                        this.dropMulticast = input.readBool();
                        break;
                    case 48:
                        this.hasIpv4Addr = input.readBool();
                        break;
                    case 56:
                        this.effectiveLifetime = input.readInt64();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ApfProgramEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ApfProgramEvent) MessageNano.mergeFrom(new ApfProgramEvent(), data);
        }

        public static ApfProgramEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ApfProgramEvent().mergeFrom(input);
        }
    }

    public static final class ApfStatistics extends MessageNano {
        private static volatile ApfStatistics[] _emptyArray;
        public int droppedRas;
        public long durationMs;
        public int matchingRas;
        public int maxProgramSize;
        public int parseErrors;
        public int programUpdates;
        public int programUpdatesAll;
        public int programUpdatesAllowingMulticast;
        public int receivedRas;
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                return size + CodedOutputByteBufferNano.computeInt32Size(11, this.programUpdatesAllowingMulticast);
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
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ApfStatistics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ApfStatistics) MessageNano.mergeFrom(new ApfStatistics(), data);
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
                for (int writeInt322 : this.nonBlockingLatenciesMs) {
                    output.writeInt32(6, writeInt322);
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int dataSize;
            int size = super.computeSerializedSize();
            if (this.connectCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.connectCount);
            }
            if (this.ipv6AddrCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.ipv6AddrCount);
            }
            if (this.latenciesMs != null && this.latenciesMs.length > 0) {
                dataSize = 0;
                for (int element : this.latenciesMs) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (size + dataSize) + (this.latenciesMs.length * 1);
            }
            if (this.errnosCounters != null && this.errnosCounters.length > 0) {
                for (Pair element2 : this.errnosCounters) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                }
            }
            if (this.connectBlockingCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.connectBlockingCount);
            }
            if (this.nonBlockingLatenciesMs == null || this.nonBlockingLatenciesMs.length <= 0) {
                return size;
            }
            dataSize = 0;
            for (int element3 : this.nonBlockingLatenciesMs) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element3);
            }
            return (size + dataSize) + (this.nonBlockingLatenciesMs.length * 1);
        }

        public ConnectStatistics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                int limit;
                int startPos;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.connectCount = input.readInt32();
                        break;
                    case 16:
                        this.ipv6AddrCount = input.readInt32();
                        break;
                    case 24:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
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
                        break;
                    case 26:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.latenciesMs = newArray;
                        input.popLimit(limit);
                        break;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        i = this.errnosCounters == null ? 0 : this.errnosCounters.length;
                        Pair[] newArray2 = new Pair[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.errnosCounters, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new Pair();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new Pair();
                        input.readMessage(newArray2[i]);
                        this.errnosCounters = newArray2;
                        break;
                    case 40:
                        this.connectBlockingCount = input.readInt32();
                        break;
                    case 48:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 48);
                        i = this.nonBlockingLatenciesMs == null ? 0 : this.nonBlockingLatenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.nonBlockingLatenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.nonBlockingLatenciesMs = newArray;
                        break;
                    case 50:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.nonBlockingLatenciesMs == null ? 0 : this.nonBlockingLatenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.nonBlockingLatenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.nonBlockingLatenciesMs = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ConnectStatistics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ConnectStatistics) MessageNano.mergeFrom(new ConnectStatistics(), data);
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
            return LogException.NO_VALUE;
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
            this.ifName = LogException.NO_VALUE;
            this.durationMs = 0;
            clearValue();
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals(LogException.NO_VALUE)) {
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.ifName.equals(LogException.NO_VALUE)) {
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
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.ifName = input.readString();
                        break;
                    case 18:
                        this.value_ = input.readString();
                        this.valueCase_ = 2;
                        break;
                    case 24:
                        this.value_ = Integer.valueOf(input.readInt32());
                        this.valueCase_ = 3;
                        break;
                    case 32:
                        this.durationMs = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DHCPEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DHCPEvent) MessageNano.mergeFrom(new DHCPEvent(), data);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
            return (size + dataSize) + (this.latenciesMs.length * 1);
        }

        public DNSLatencies mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.type = input.readInt32();
                        break;
                    case 16:
                        this.returnCode = input.readInt32();
                        break;
                    case 24:
                        this.queryCount = input.readInt32();
                        break;
                    case 32:
                        this.aCount = input.readInt32();
                        break;
                    case 40:
                        this.aaaaCount = input.readInt32();
                        break;
                    case 48:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 48);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
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
                        break;
                    case 50:
                        int limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.latenciesMs = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DNSLatencies parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DNSLatencies) MessageNano.mergeFrom(new DNSLatencies(), data);
        }

        public static DNSLatencies parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DNSLatencies().mergeFrom(input);
        }
    }

    public static final class DNSLookupBatch extends MessageNano {
        private static volatile DNSLookupBatch[] _emptyArray;
        public int[] eventTypes;
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
            this.networkId = null;
            this.eventTypes = WireFormatNano.EMPTY_INT_ARRAY;
            this.returnCodes = WireFormatNano.EMPTY_INT_ARRAY;
            this.latenciesMs = WireFormatNano.EMPTY_INT_ARRAY;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.networkId != null) {
                output.writeMessage(1, this.networkId);
            }
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
                for (int writeInt3222 : this.latenciesMs) {
                    output.writeInt32(4, writeInt3222);
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int dataSize;
            int size = super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.eventTypes != null && this.eventTypes.length > 0) {
                dataSize = 0;
                for (int element : this.eventTypes) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (size + dataSize) + (this.eventTypes.length * 1);
            }
            if (this.returnCodes != null && this.returnCodes.length > 0) {
                dataSize = 0;
                for (int element2 : this.returnCodes) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                }
                size = (size + dataSize) + (this.returnCodes.length * 1);
            }
            if (this.latenciesMs == null || this.latenciesMs.length <= 0) {
                return size;
            }
            dataSize = 0;
            for (int element22 : this.latenciesMs) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element22);
            }
            return (size + dataSize) + (this.latenciesMs.length * 1);
        }

        public DNSLookupBatch mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                int limit;
                int startPos;
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
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                        i = this.eventTypes == null ? 0 : this.eventTypes.length;
                        newArray = new int[(i + arrayLength)];
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
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.eventTypes == null ? 0 : this.eventTypes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.eventTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.eventTypes = newArray;
                        input.popLimit(limit);
                        break;
                    case 24:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                        i = this.returnCodes == null ? 0 : this.returnCodes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.returnCodes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.returnCodes = newArray;
                        break;
                    case 26:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.returnCodes == null ? 0 : this.returnCodes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.returnCodes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.returnCodes = newArray;
                        input.popLimit(limit);
                        break;
                    case 32:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
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
                        break;
                    case 34:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.latenciesMs == null ? 0 : this.latenciesMs.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.latenciesMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.latenciesMs = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DNSLookupBatch parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DNSLookupBatch) MessageNano.mergeFrom(new DNSLookupBatch(), data);
        }

        public static DNSLookupBatch parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DNSLookupBatch().mergeFrom(input);
        }
    }

    public static final class DefaultNetworkEvent extends MessageNano {
        public static final int DUAL = 3;
        public static final int IPV4 = 1;
        public static final int IPV6 = 2;
        public static final int NONE = 0;
        private static volatile DefaultNetworkEvent[] _emptyArray;
        public NetworkId networkId;
        public NetworkId previousNetworkId;
        public int previousNetworkIpSupport;
        public int[] transportTypes;

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
            this.networkId = null;
            this.previousNetworkId = null;
            this.previousNetworkIpSupport = 0;
            this.transportTypes = WireFormatNano.EMPTY_INT_ARRAY;
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.networkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.networkId);
            }
            if (this.previousNetworkId != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.previousNetworkId);
            }
            if (this.previousNetworkIpSupport != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.previousNetworkIpSupport);
            }
            if (this.transportTypes == null || this.transportTypes.length <= 0) {
                return size;
            }
            int dataSize = 0;
            for (int element : this.transportTypes) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
            }
            return (size + dataSize) + (this.transportTypes.length * 1);
        }

        public DefaultNetworkEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
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
                            default:
                                break;
                        }
                    case 32:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        i = this.transportTypes == null ? 0 : this.transportTypes.length;
                        newArray = new int[(i + arrayLength)];
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
                        arrayLength = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.transportTypes == null ? 0 : this.transportTypes.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.transportTypes, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.transportTypes = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static DefaultNetworkEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DefaultNetworkEvent) MessageNano.mergeFrom(new DefaultNetworkEvent(), data);
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
        public static final int RA_EVENT_FIELD_NUMBER = 11;
        public static final int VALIDATION_PROBE_EVENT_FIELD_NUMBER = 8;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 2;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 3;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 4;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 5;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 13;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 14;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 6;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 7;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 8;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 9;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 10;
            this.event_ = value;
            return this;
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
            if (value == null) {
                throw new NullPointerException();
            }
            this.eventCase_ = 11;
            this.event_ = value;
            return this;
        }

        public IpConnectivityEvent() {
            clear();
        }

        public IpConnectivityEvent clear() {
            this.timeMs = 0;
            this.linkLayer = 0;
            this.networkId = 0;
            this.ifName = LogException.NO_VALUE;
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
            if (!this.ifName.equals(LogException.NO_VALUE)) {
                output.writeString(17, this.ifName);
            }
            if (this.transports != 0) {
                output.writeInt64(18, this.transports);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
            if (!this.ifName.equals(LogException.NO_VALUE)) {
                size += CodedOutputByteBufferNano.computeStringSize(17, this.ifName);
            }
            if (this.transports != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(18, this.transports);
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
                                this.linkLayer = value;
                                break;
                            default:
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
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpConnectivityEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpConnectivityEvent) MessageNano.mergeFrom(new IpConnectivityEvent(), data);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
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
                        break;
                    case 16:
                        this.droppedEvents = input.readInt32();
                        break;
                    case 24:
                        this.version = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpConnectivityLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpConnectivityLog) MessageNano.mergeFrom(new IpConnectivityLog(), data);
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
            this.ifName = LogException.NO_VALUE;
            this.eventType = 0;
            this.latencyMs = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals(LogException.NO_VALUE)) {
                output.writeString(1, this.ifName);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            if (this.latencyMs != 0) {
                output.writeInt32(3, this.latencyMs);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.ifName.equals(LogException.NO_VALUE)) {
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
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.ifName = input.readString();
                        break;
                    case 16:
                        this.eventType = input.readInt32();
                        break;
                    case 24:
                        this.latencyMs = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpProvisioningEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpProvisioningEvent) MessageNano.mergeFrom(new IpProvisioningEvent(), data);
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
            this.ifName = LogException.NO_VALUE;
            this.eventType = 0;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.ifName.equals(LogException.NO_VALUE)) {
                output.writeString(1, this.ifName);
            }
            if (this.eventType != 0) {
                output.writeInt32(2, this.eventType);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.ifName.equals(LogException.NO_VALUE)) {
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
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        this.ifName = input.readString();
                        break;
                    case 16:
                        this.eventType = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static IpReachabilityEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (IpReachabilityEvent) MessageNano.mergeFrom(new IpReachabilityEvent(), data);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                        this.eventType = input.readInt32();
                        break;
                    case 24:
                        this.latencyMs = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static NetworkEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (NetworkEvent) MessageNano.mergeFrom(new NetworkEvent(), data);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.networkId != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(1, this.networkId);
            }
            return size;
        }

        public NetworkId mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.networkId = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static NetworkId parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (NetworkId) MessageNano.mergeFrom(new NetworkId(), data);
        }

        public static NetworkId parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new NetworkId().mergeFrom(input);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.key = input.readInt32();
                        break;
                    case 16:
                        this.value = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static Pair parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Pair) MessageNano.mergeFrom(new Pair(), data);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.routerLifetime = input.readInt64();
                        break;
                    case 16:
                        this.prefixValidLifetime = input.readInt64();
                        break;
                    case 24:
                        this.prefixPreferredLifetime = input.readInt64();
                        break;
                    case 32:
                        this.routeInfoLifetime = input.readInt64();
                        break;
                    case 40:
                        this.rdnssLifetime = input.readInt64();
                        break;
                    case 48:
                        this.dnsslLifetime = input.readInt64();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static RaEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (RaEvent) MessageNano.mergeFrom(new RaEvent(), data);
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
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
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
                        this.latencyMs = input.readInt32();
                        break;
                    case 24:
                        this.probeType = input.readInt32();
                        break;
                    case 32:
                        this.probeResult = input.readInt32();
                        break;
                    default:
                        if (WireFormatNano.parseUnknownField(input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static ValidationProbeEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ValidationProbeEvent) MessageNano.mergeFrom(new ValidationProbeEvent(), data);
        }

        public static ValidationProbeEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ValidationProbeEvent().mergeFrom(input);
        }
    }
}
