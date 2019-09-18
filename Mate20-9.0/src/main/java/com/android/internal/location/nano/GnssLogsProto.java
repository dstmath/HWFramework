package com.android.internal.location.nano;

import com.android.framework.protobuf.nano.CodedInputByteBufferNano;
import com.android.framework.protobuf.nano.CodedOutputByteBufferNano;
import com.android.framework.protobuf.nano.InternalNano;
import com.android.framework.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.framework.protobuf.nano.MessageNano;
import com.android.framework.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface GnssLogsProto {

    public static final class GnssLog extends MessageNano {
        private static volatile GnssLog[] _emptyArray;
        public int meanPositionAccuracyMeters;
        public int meanTimeToFirstFixSecs;
        public double meanTopFourAverageCn0DbHz;
        public int numLocationReportProcessed;
        public int numPositionAccuracyProcessed;
        public int numTimeToFirstFixProcessed;
        public int numTopFourAverageCn0Processed;
        public int percentageLocationFailure;
        public PowerMetrics powerMetrics;
        public int standardDeviationPositionAccuracyMeters;
        public int standardDeviationTimeToFirstFixSecs;
        public double standardDeviationTopFourAverageCn0DbHz;

        public static GnssLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new GnssLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public GnssLog() {
            clear();
        }

        public GnssLog clear() {
            this.numLocationReportProcessed = 0;
            this.percentageLocationFailure = 0;
            this.numTimeToFirstFixProcessed = 0;
            this.meanTimeToFirstFixSecs = 0;
            this.standardDeviationTimeToFirstFixSecs = 0;
            this.numPositionAccuracyProcessed = 0;
            this.meanPositionAccuracyMeters = 0;
            this.standardDeviationPositionAccuracyMeters = 0;
            this.numTopFourAverageCn0Processed = 0;
            this.meanTopFourAverageCn0DbHz = 0.0d;
            this.standardDeviationTopFourAverageCn0DbHz = 0.0d;
            this.powerMetrics = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.numLocationReportProcessed != 0) {
                output.writeInt32(1, this.numLocationReportProcessed);
            }
            if (this.percentageLocationFailure != 0) {
                output.writeInt32(2, this.percentageLocationFailure);
            }
            if (this.numTimeToFirstFixProcessed != 0) {
                output.writeInt32(3, this.numTimeToFirstFixProcessed);
            }
            if (this.meanTimeToFirstFixSecs != 0) {
                output.writeInt32(4, this.meanTimeToFirstFixSecs);
            }
            if (this.standardDeviationTimeToFirstFixSecs != 0) {
                output.writeInt32(5, this.standardDeviationTimeToFirstFixSecs);
            }
            if (this.numPositionAccuracyProcessed != 0) {
                output.writeInt32(6, this.numPositionAccuracyProcessed);
            }
            if (this.meanPositionAccuracyMeters != 0) {
                output.writeInt32(7, this.meanPositionAccuracyMeters);
            }
            if (this.standardDeviationPositionAccuracyMeters != 0) {
                output.writeInt32(8, this.standardDeviationPositionAccuracyMeters);
            }
            if (this.numTopFourAverageCn0Processed != 0) {
                output.writeInt32(9, this.numTopFourAverageCn0Processed);
            }
            if (Double.doubleToLongBits(this.meanTopFourAverageCn0DbHz) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(10, this.meanTopFourAverageCn0DbHz);
            }
            if (Double.doubleToLongBits(this.standardDeviationTopFourAverageCn0DbHz) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(11, this.standardDeviationTopFourAverageCn0DbHz);
            }
            if (this.powerMetrics != null) {
                output.writeMessage(12, this.powerMetrics);
            }
            GnssLogsProto.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = GnssLogsProto.super.computeSerializedSize();
            if (this.numLocationReportProcessed != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.numLocationReportProcessed);
            }
            if (this.percentageLocationFailure != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.percentageLocationFailure);
            }
            if (this.numTimeToFirstFixProcessed != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.numTimeToFirstFixProcessed);
            }
            if (this.meanTimeToFirstFixSecs != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.meanTimeToFirstFixSecs);
            }
            if (this.standardDeviationTimeToFirstFixSecs != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.standardDeviationTimeToFirstFixSecs);
            }
            if (this.numPositionAccuracyProcessed != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.numPositionAccuracyProcessed);
            }
            if (this.meanPositionAccuracyMeters != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.meanPositionAccuracyMeters);
            }
            if (this.standardDeviationPositionAccuracyMeters != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, this.standardDeviationPositionAccuracyMeters);
            }
            if (this.numTopFourAverageCn0Processed != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.numTopFourAverageCn0Processed);
            }
            if (Double.doubleToLongBits(this.meanTopFourAverageCn0DbHz) != Double.doubleToLongBits(0.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(10, this.meanTopFourAverageCn0DbHz);
            }
            if (Double.doubleToLongBits(this.standardDeviationTopFourAverageCn0DbHz) != Double.doubleToLongBits(0.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(11, this.standardDeviationTopFourAverageCn0DbHz);
            }
            if (this.powerMetrics != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(12, this.powerMetrics);
            }
            return size;
        }

        public GnssLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.numLocationReportProcessed = input.readInt32();
                        break;
                    case 16:
                        this.percentageLocationFailure = input.readInt32();
                        break;
                    case 24:
                        this.numTimeToFirstFixProcessed = input.readInt32();
                        break;
                    case 32:
                        this.meanTimeToFirstFixSecs = input.readInt32();
                        break;
                    case 40:
                        this.standardDeviationTimeToFirstFixSecs = input.readInt32();
                        break;
                    case 48:
                        this.numPositionAccuracyProcessed = input.readInt32();
                        break;
                    case 56:
                        this.meanPositionAccuracyMeters = input.readInt32();
                        break;
                    case 64:
                        this.standardDeviationPositionAccuracyMeters = input.readInt32();
                        break;
                    case 72:
                        this.numTopFourAverageCn0Processed = input.readInt32();
                        break;
                    case 81:
                        this.meanTopFourAverageCn0DbHz = input.readDouble();
                        break;
                    case 89:
                        this.standardDeviationTopFourAverageCn0DbHz = input.readDouble();
                        break;
                    case 98:
                        if (this.powerMetrics == null) {
                            this.powerMetrics = new PowerMetrics();
                        }
                        input.readMessage(this.powerMetrics);
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

        public static GnssLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new GnssLog(), data);
        }

        public static GnssLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new GnssLog().mergeFrom(input);
        }
    }

    public static final class PowerMetrics extends MessageNano {
        private static volatile PowerMetrics[] _emptyArray;
        public double energyConsumedMah;
        public long loggingDurationMs;
        public long[] timeInSignalQualityLevelMs;

        public static PowerMetrics[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PowerMetrics[0];
                    }
                }
            }
            return _emptyArray;
        }

        public PowerMetrics() {
            clear();
        }

        public PowerMetrics clear() {
            this.loggingDurationMs = 0;
            this.energyConsumedMah = 0.0d;
            this.timeInSignalQualityLevelMs = WireFormatNano.EMPTY_LONG_ARRAY;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.loggingDurationMs != 0) {
                output.writeInt64(1, this.loggingDurationMs);
            }
            if (Double.doubleToLongBits(this.energyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(2, this.energyConsumedMah);
            }
            if (this.timeInSignalQualityLevelMs != null && this.timeInSignalQualityLevelMs.length > 0) {
                for (long writeInt64 : this.timeInSignalQualityLevelMs) {
                    output.writeInt64(3, writeInt64);
                }
            }
            GnssLogsProto.super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = GnssLogsProto.super.computeSerializedSize();
            if (this.loggingDurationMs != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.loggingDurationMs);
            }
            if (Double.doubleToLongBits(this.energyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(2, this.energyConsumedMah);
            }
            if (this.timeInSignalQualityLevelMs == null || this.timeInSignalQualityLevelMs.length <= 0) {
                return size;
            }
            int dataSize = 0;
            for (long element : this.timeInSignalQualityLevelMs) {
                dataSize += CodedOutputByteBufferNano.computeInt64SizeNoTag(element);
            }
            return size + dataSize + (1 * this.timeInSignalQualityLevelMs.length);
        }

        public PowerMetrics mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.loggingDurationMs = input.readInt64();
                } else if (tag == 17) {
                    this.energyConsumedMah = input.readDouble();
                } else if (tag == 24) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                    int i = this.timeInSignalQualityLevelMs == null ? 0 : this.timeInSignalQualityLevelMs.length;
                    long[] newArray = new long[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.timeInSignalQualityLevelMs, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = input.readInt64();
                        input.readTag();
                        i++;
                    }
                    newArray[i] = input.readInt64();
                    this.timeInSignalQualityLevelMs = newArray;
                } else if (tag == 26) {
                    int limit = input.pushLimit(input.readRawVarint32());
                    int arrayLength2 = 0;
                    int startPos = input.getPosition();
                    while (input.getBytesUntilLimit() > 0) {
                        input.readInt64();
                        arrayLength2++;
                    }
                    input.rewindToPosition(startPos);
                    int i2 = this.timeInSignalQualityLevelMs == null ? 0 : this.timeInSignalQualityLevelMs.length;
                    long[] newArray2 = new long[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.timeInSignalQualityLevelMs, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length) {
                        newArray2[i2] = input.readInt64();
                        i2++;
                    }
                    this.timeInSignalQualityLevelMs = newArray2;
                    input.popLimit(limit);
                } else if (!WireFormatNano.parseUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static PowerMetrics parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new PowerMetrics(), data);
        }

        public static PowerMetrics parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new PowerMetrics().mergeFrom(input);
        }
    }
}
