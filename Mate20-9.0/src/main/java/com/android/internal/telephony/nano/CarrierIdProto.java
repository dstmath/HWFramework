package com.android.internal.telephony.nano;

import com.android.internal.telephony.protobuf.nano.CodedInputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.ExtendableMessageNano;
import com.android.internal.telephony.protobuf.nano.InternalNano;
import com.android.internal.telephony.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import com.android.internal.telephony.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface CarrierIdProto {

    public static final class CarrierAttribute extends ExtendableMessageNano<CarrierAttribute> {
        private static volatile CarrierAttribute[] _emptyArray;
        public String[] gid1;
        public String[] gid2;
        public String[] iccidPrefix;
        public String[] imsiPrefixXpattern;
        public String[] mccmncTuple;
        public String[] plmn;
        public String[] preferredApn;
        public String[] spn;

        public static CarrierAttribute[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new CarrierAttribute[0];
                    }
                }
            }
            return _emptyArray;
        }

        public CarrierAttribute() {
            clear();
        }

        public CarrierAttribute clear() {
            this.mccmncTuple = WireFormatNano.EMPTY_STRING_ARRAY;
            this.imsiPrefixXpattern = WireFormatNano.EMPTY_STRING_ARRAY;
            this.spn = WireFormatNano.EMPTY_STRING_ARRAY;
            this.plmn = WireFormatNano.EMPTY_STRING_ARRAY;
            this.gid1 = WireFormatNano.EMPTY_STRING_ARRAY;
            this.gid2 = WireFormatNano.EMPTY_STRING_ARRAY;
            this.preferredApn = WireFormatNano.EMPTY_STRING_ARRAY;
            this.iccidPrefix = WireFormatNano.EMPTY_STRING_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = 0;
            if (this.mccmncTuple != null && this.mccmncTuple.length > 0) {
                for (String element : this.mccmncTuple) {
                    if (element != null) {
                        output.writeString(1, element);
                    }
                }
            }
            if (this.imsiPrefixXpattern != null && this.imsiPrefixXpattern.length > 0) {
                for (String element2 : this.imsiPrefixXpattern) {
                    if (element2 != null) {
                        output.writeString(2, element2);
                    }
                }
            }
            if (this.spn != null && this.spn.length > 0) {
                for (String element3 : this.spn) {
                    if (element3 != null) {
                        output.writeString(3, element3);
                    }
                }
            }
            if (this.plmn != null && this.plmn.length > 0) {
                for (String element4 : this.plmn) {
                    if (element4 != null) {
                        output.writeString(4, element4);
                    }
                }
            }
            if (this.gid1 != null && this.gid1.length > 0) {
                for (String element5 : this.gid1) {
                    if (element5 != null) {
                        output.writeString(5, element5);
                    }
                }
            }
            if (this.gid2 != null && this.gid2.length > 0) {
                for (String element6 : this.gid2) {
                    if (element6 != null) {
                        output.writeString(6, element6);
                    }
                }
            }
            if (this.preferredApn != null && this.preferredApn.length > 0) {
                for (String element7 : this.preferredApn) {
                    if (element7 != null) {
                        output.writeString(7, element7);
                    }
                }
            }
            if (this.iccidPrefix != null && this.iccidPrefix.length > 0) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.iccidPrefix.length) {
                        break;
                    }
                    String element8 = this.iccidPrefix[i2];
                    if (element8 != null) {
                        output.writeString(8, element8);
                    }
                    i = i2 + 1;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.mccmncTuple != null && this.mccmncTuple.length > 0) {
                int dataSize = 0;
                int dataCount = 0;
                for (String element : this.mccmncTuple) {
                    if (element != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                    }
                }
                size = size + dataSize + (1 * dataCount);
            }
            if (this.imsiPrefixXpattern != null && this.imsiPrefixXpattern.length > 0) {
                int dataSize2 = 0;
                int dataCount2 = 0;
                for (String element2 : this.imsiPrefixXpattern) {
                    if (element2 != null) {
                        dataCount2++;
                        dataSize2 += CodedOutputByteBufferNano.computeStringSizeNoTag(element2);
                    }
                }
                size = size + dataSize2 + (1 * dataCount2);
            }
            if (this.spn != null && this.spn.length > 0) {
                int dataSize3 = 0;
                int dataCount3 = 0;
                for (String element3 : this.spn) {
                    if (element3 != null) {
                        dataCount3++;
                        dataSize3 += CodedOutputByteBufferNano.computeStringSizeNoTag(element3);
                    }
                }
                size = size + dataSize3 + (1 * dataCount3);
            }
            if (this.plmn != null && this.plmn.length > 0) {
                int dataSize4 = 0;
                int dataCount4 = 0;
                for (String element4 : this.plmn) {
                    if (element4 != null) {
                        dataCount4++;
                        dataSize4 += CodedOutputByteBufferNano.computeStringSizeNoTag(element4);
                    }
                }
                size = size + dataSize4 + (1 * dataCount4);
            }
            if (this.gid1 != null && this.gid1.length > 0) {
                int dataSize5 = 0;
                int dataCount5 = 0;
                for (String element5 : this.gid1) {
                    if (element5 != null) {
                        dataCount5++;
                        dataSize5 += CodedOutputByteBufferNano.computeStringSizeNoTag(element5);
                    }
                }
                size = size + dataSize5 + (1 * dataCount5);
            }
            if (this.gid2 != null && this.gid2.length > 0) {
                int dataSize6 = 0;
                int dataCount6 = 0;
                for (String element6 : this.gid2) {
                    if (element6 != null) {
                        dataCount6++;
                        dataSize6 += CodedOutputByteBufferNano.computeStringSizeNoTag(element6);
                    }
                }
                size = size + dataSize6 + (1 * dataCount6);
            }
            if (this.preferredApn != null && this.preferredApn.length > 0) {
                int dataSize7 = 0;
                int dataCount7 = 0;
                for (String element7 : this.preferredApn) {
                    if (element7 != null) {
                        dataCount7++;
                        dataSize7 += CodedOutputByteBufferNano.computeStringSizeNoTag(element7);
                    }
                }
                size = size + dataSize7 + (1 * dataCount7);
            }
            if (this.iccidPrefix == null || this.iccidPrefix.length <= 0) {
                return size;
            }
            int dataCount8 = 0;
            int dataSize8 = 0;
            for (String element8 : this.iccidPrefix) {
                if (element8 != null) {
                    dataCount8++;
                    dataSize8 += CodedOutputByteBufferNano.computeStringSizeNoTag(element8);
                }
            }
            return size + dataSize8 + (1 * dataCount8);
        }

        public CarrierAttribute mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = this.mccmncTuple == null ? 0 : this.mccmncTuple.length;
                    String[] newArray = new String[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.mccmncTuple, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = input.readString();
                        input.readTag();
                        i++;
                    }
                    newArray[i] = input.readString();
                    this.mccmncTuple = newArray;
                } else if (tag == 18) {
                    int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    int i2 = this.imsiPrefixXpattern == null ? 0 : this.imsiPrefixXpattern.length;
                    String[] newArray2 = new String[(i2 + arrayLength2)];
                    if (i2 != 0) {
                        System.arraycopy(this.imsiPrefixXpattern, 0, newArray2, 0, i2);
                    }
                    while (i2 < newArray2.length - 1) {
                        newArray2[i2] = input.readString();
                        input.readTag();
                        i2++;
                    }
                    newArray2[i2] = input.readString();
                    this.imsiPrefixXpattern = newArray2;
                } else if (tag == 26) {
                    int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    int i3 = this.spn == null ? 0 : this.spn.length;
                    String[] newArray3 = new String[(i3 + arrayLength3)];
                    if (i3 != 0) {
                        System.arraycopy(this.spn, 0, newArray3, 0, i3);
                    }
                    while (i3 < newArray3.length - 1) {
                        newArray3[i3] = input.readString();
                        input.readTag();
                        i3++;
                    }
                    newArray3[i3] = input.readString();
                    this.spn = newArray3;
                } else if (tag == 34) {
                    int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    int i4 = this.plmn == null ? 0 : this.plmn.length;
                    String[] newArray4 = new String[(i4 + arrayLength4)];
                    if (i4 != 0) {
                        System.arraycopy(this.plmn, 0, newArray4, 0, i4);
                    }
                    while (i4 < newArray4.length - 1) {
                        newArray4[i4] = input.readString();
                        input.readTag();
                        i4++;
                    }
                    newArray4[i4] = input.readString();
                    this.plmn = newArray4;
                } else if (tag == 42) {
                    int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    int i5 = this.gid1 == null ? 0 : this.gid1.length;
                    String[] newArray5 = new String[(i5 + arrayLength5)];
                    if (i5 != 0) {
                        System.arraycopy(this.gid1, 0, newArray5, 0, i5);
                    }
                    while (i5 < newArray5.length - 1) {
                        newArray5[i5] = input.readString();
                        input.readTag();
                        i5++;
                    }
                    newArray5[i5] = input.readString();
                    this.gid1 = newArray5;
                } else if (tag == 50) {
                    int arrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                    int i6 = this.gid2 == null ? 0 : this.gid2.length;
                    String[] newArray6 = new String[(i6 + arrayLength6)];
                    if (i6 != 0) {
                        System.arraycopy(this.gid2, 0, newArray6, 0, i6);
                    }
                    while (i6 < newArray6.length - 1) {
                        newArray6[i6] = input.readString();
                        input.readTag();
                        i6++;
                    }
                    newArray6[i6] = input.readString();
                    this.gid2 = newArray6;
                } else if (tag == 58) {
                    int arrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                    int i7 = this.preferredApn == null ? 0 : this.preferredApn.length;
                    String[] newArray7 = new String[(i7 + arrayLength7)];
                    if (i7 != 0) {
                        System.arraycopy(this.preferredApn, 0, newArray7, 0, i7);
                    }
                    while (i7 < newArray7.length - 1) {
                        newArray7[i7] = input.readString();
                        input.readTag();
                        i7++;
                    }
                    newArray7[i7] = input.readString();
                    this.preferredApn = newArray7;
                } else if (tag == 66) {
                    int arrayLength8 = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                    int i8 = this.iccidPrefix == null ? 0 : this.iccidPrefix.length;
                    String[] newArray8 = new String[(i8 + arrayLength8)];
                    if (i8 != 0) {
                        System.arraycopy(this.iccidPrefix, 0, newArray8, 0, i8);
                    }
                    while (i8 < newArray8.length - 1) {
                        newArray8[i8] = input.readString();
                        input.readTag();
                        i8++;
                    }
                    newArray8[i8] = input.readString();
                    this.iccidPrefix = newArray8;
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static CarrierAttribute parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (CarrierAttribute) MessageNano.mergeFrom(new CarrierAttribute(), data);
        }

        public static CarrierAttribute parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new CarrierAttribute().mergeFrom(input);
        }
    }

    public static final class CarrierId extends ExtendableMessageNano<CarrierId> {
        private static volatile CarrierId[] _emptyArray;
        public int canonicalId;
        public CarrierAttribute[] carrierAttribute;
        public String carrierName;

        public static CarrierId[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new CarrierId[0];
                    }
                }
            }
            return _emptyArray;
        }

        public CarrierId() {
            clear();
        }

        public CarrierId clear() {
            this.canonicalId = 0;
            this.carrierName = "";
            this.carrierAttribute = CarrierAttribute.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.canonicalId != 0) {
                output.writeInt32(1, this.canonicalId);
            }
            if (!this.carrierName.equals("")) {
                output.writeString(2, this.carrierName);
            }
            if (this.carrierAttribute != null && this.carrierAttribute.length > 0) {
                for (CarrierAttribute element : this.carrierAttribute) {
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.canonicalId != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.canonicalId);
            }
            if (!this.carrierName.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.carrierName);
            }
            if (this.carrierAttribute != null && this.carrierAttribute.length > 0) {
                for (CarrierAttribute element : this.carrierAttribute) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                }
            }
            return size;
        }

        public CarrierId mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.canonicalId = input.readInt32();
                } else if (tag == 18) {
                    this.carrierName = input.readString();
                } else if (tag == 26) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    int i = this.carrierAttribute == null ? 0 : this.carrierAttribute.length;
                    CarrierAttribute[] newArray = new CarrierAttribute[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.carrierAttribute, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new CarrierAttribute();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new CarrierAttribute();
                    input.readMessage(newArray[i]);
                    this.carrierAttribute = newArray;
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static CarrierId parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (CarrierId) MessageNano.mergeFrom(new CarrierId(), data);
        }

        public static CarrierId parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new CarrierId().mergeFrom(input);
        }
    }

    public static final class CarrierList extends ExtendableMessageNano<CarrierList> {
        private static volatile CarrierList[] _emptyArray;
        public CarrierId[] carrierId;
        public int version;

        public static CarrierList[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new CarrierList[0];
                    }
                }
            }
            return _emptyArray;
        }

        public CarrierList() {
            clear();
        }

        public CarrierList clear() {
            this.carrierId = CarrierId.emptyArray();
            this.version = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.carrierId != null && this.carrierId.length > 0) {
                for (CarrierId element : this.carrierId) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            if (this.version != 0) {
                output.writeInt32(2, this.version);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.carrierId != null && this.carrierId.length > 0) {
                for (CarrierId element : this.carrierId) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            if (this.version != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, this.version);
            }
            return size;
        }

        public CarrierList mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = this.carrierId == null ? 0 : this.carrierId.length;
                    CarrierId[] newArray = new CarrierId[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.carrierId, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new CarrierId();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new CarrierId();
                    input.readMessage(newArray[i]);
                    this.carrierId = newArray;
                } else if (tag == 16) {
                    this.version = input.readInt32();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static CarrierList parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (CarrierList) MessageNano.mergeFrom(new CarrierList(), data);
        }

        public static CarrierList parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new CarrierList().mergeFrom(input);
        }
    }
}
