package com.android.internal.telephony.nano;

import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.protobuf.nano.CodedInputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.ExtendableMessageNano;
import com.android.internal.telephony.protobuf.nano.InternalNano;
import com.android.internal.telephony.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import com.android.internal.telephony.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface CarrierIdProto {

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

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            CarrierId[] carrierIdArr = this.carrierId;
            if (carrierIdArr != null && carrierIdArr.length > 0) {
                int i = 0;
                while (true) {
                    CarrierId[] carrierIdArr2 = this.carrierId;
                    if (i >= carrierIdArr2.length) {
                        break;
                    }
                    CarrierId element = carrierIdArr2[i];
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                    i++;
                }
            }
            int i2 = this.version;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            CarrierId[] carrierIdArr = this.carrierId;
            if (carrierIdArr != null && carrierIdArr.length > 0) {
                int i = 0;
                while (true) {
                    CarrierId[] carrierIdArr2 = this.carrierId;
                    if (i >= carrierIdArr2.length) {
                        break;
                    }
                    CarrierId element = carrierIdArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                    i++;
                }
            }
            int i2 = this.version;
            if (i2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public CarrierList mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    CarrierId[] carrierIdArr = this.carrierId;
                    int i = carrierIdArr == null ? 0 : carrierIdArr.length;
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

    public static final class CarrierId extends ExtendableMessageNano<CarrierId> {
        private static volatile CarrierId[] _emptyArray;
        public int canonicalId;
        public CarrierAttribute[] carrierAttribute;
        public String carrierName;
        public int parentCanonicalId;

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
            this.carrierName = PhoneConfigurationManager.SSSS;
            this.carrierAttribute = CarrierAttribute.emptyArray();
            this.parentCanonicalId = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.canonicalId;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            if (!this.carrierName.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(2, this.carrierName);
            }
            CarrierAttribute[] carrierAttributeArr = this.carrierAttribute;
            if (carrierAttributeArr != null && carrierAttributeArr.length > 0) {
                int i2 = 0;
                while (true) {
                    CarrierAttribute[] carrierAttributeArr2 = this.carrierAttribute;
                    if (i2 >= carrierAttributeArr2.length) {
                        break;
                    }
                    CarrierAttribute element = carrierAttributeArr2[i2];
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                    i2++;
                }
            }
            int i3 = this.parentCanonicalId;
            if (i3 != 0) {
                output.writeInt32(4, i3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.canonicalId;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            if (!this.carrierName.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.carrierName);
            }
            CarrierAttribute[] carrierAttributeArr = this.carrierAttribute;
            if (carrierAttributeArr != null && carrierAttributeArr.length > 0) {
                int i2 = 0;
                while (true) {
                    CarrierAttribute[] carrierAttributeArr2 = this.carrierAttribute;
                    if (i2 >= carrierAttributeArr2.length) {
                        break;
                    }
                    CarrierAttribute element = carrierAttributeArr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                    i2++;
                }
            }
            int i3 = this.parentCanonicalId;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, i3);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
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
                    CarrierAttribute[] carrierAttributeArr = this.carrierAttribute;
                    int i = carrierAttributeArr == null ? 0 : carrierAttributeArr.length;
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
                } else if (tag == 32) {
                    this.parentCanonicalId = input.readInt32();
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

    public static final class CarrierAttribute extends ExtendableMessageNano<CarrierAttribute> {
        private static volatile CarrierAttribute[] _emptyArray;
        public String[] gid1;
        public String[] gid2;
        public String[] iccidPrefix;
        public String[] imsiPrefixXpattern;
        public String[] mccmncTuple;
        public String[] plmn;
        public String[] preferredApn;
        public String[] privilegeAccessRule;
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
            this.privilegeAccessRule = WireFormatNano.EMPTY_STRING_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            String[] strArr = this.mccmncTuple;
            if (strArr != null && strArr.length > 0) {
                int i = 0;
                while (true) {
                    String[] strArr2 = this.mccmncTuple;
                    if (i >= strArr2.length) {
                        break;
                    }
                    String element = strArr2[i];
                    if (element != null) {
                        output.writeString(1, element);
                    }
                    i++;
                }
            }
            String[] strArr3 = this.imsiPrefixXpattern;
            if (strArr3 != null && strArr3.length > 0) {
                int i2 = 0;
                while (true) {
                    String[] strArr4 = this.imsiPrefixXpattern;
                    if (i2 >= strArr4.length) {
                        break;
                    }
                    String element2 = strArr4[i2];
                    if (element2 != null) {
                        output.writeString(2, element2);
                    }
                    i2++;
                }
            }
            String[] strArr5 = this.spn;
            if (strArr5 != null && strArr5.length > 0) {
                int i3 = 0;
                while (true) {
                    String[] strArr6 = this.spn;
                    if (i3 >= strArr6.length) {
                        break;
                    }
                    String element3 = strArr6[i3];
                    if (element3 != null) {
                        output.writeString(3, element3);
                    }
                    i3++;
                }
            }
            String[] strArr7 = this.plmn;
            if (strArr7 != null && strArr7.length > 0) {
                int i4 = 0;
                while (true) {
                    String[] strArr8 = this.plmn;
                    if (i4 >= strArr8.length) {
                        break;
                    }
                    String element4 = strArr8[i4];
                    if (element4 != null) {
                        output.writeString(4, element4);
                    }
                    i4++;
                }
            }
            String[] strArr9 = this.gid1;
            if (strArr9 != null && strArr9.length > 0) {
                int i5 = 0;
                while (true) {
                    String[] strArr10 = this.gid1;
                    if (i5 >= strArr10.length) {
                        break;
                    }
                    String element5 = strArr10[i5];
                    if (element5 != null) {
                        output.writeString(5, element5);
                    }
                    i5++;
                }
            }
            String[] strArr11 = this.gid2;
            if (strArr11 != null && strArr11.length > 0) {
                int i6 = 0;
                while (true) {
                    String[] strArr12 = this.gid2;
                    if (i6 >= strArr12.length) {
                        break;
                    }
                    String element6 = strArr12[i6];
                    if (element6 != null) {
                        output.writeString(6, element6);
                    }
                    i6++;
                }
            }
            String[] strArr13 = this.preferredApn;
            if (strArr13 != null && strArr13.length > 0) {
                int i7 = 0;
                while (true) {
                    String[] strArr14 = this.preferredApn;
                    if (i7 >= strArr14.length) {
                        break;
                    }
                    String element7 = strArr14[i7];
                    if (element7 != null) {
                        output.writeString(7, element7);
                    }
                    i7++;
                }
            }
            String[] strArr15 = this.iccidPrefix;
            if (strArr15 != null && strArr15.length > 0) {
                int i8 = 0;
                while (true) {
                    String[] strArr16 = this.iccidPrefix;
                    if (i8 >= strArr16.length) {
                        break;
                    }
                    String element8 = strArr16[i8];
                    if (element8 != null) {
                        output.writeString(8, element8);
                    }
                    i8++;
                }
            }
            String[] strArr17 = this.privilegeAccessRule;
            if (strArr17 != null && strArr17.length > 0) {
                int i9 = 0;
                while (true) {
                    String[] strArr18 = this.privilegeAccessRule;
                    if (i9 >= strArr18.length) {
                        break;
                    }
                    String element9 = strArr18[i9];
                    if (element9 != null) {
                        output.writeString(9, element9);
                    }
                    i9++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            String[] strArr = this.mccmncTuple;
            if (strArr != null && strArr.length > 0) {
                int dataCount = 0;
                int dataSize = 0;
                int i = 0;
                while (true) {
                    String[] strArr2 = this.mccmncTuple;
                    if (i >= strArr2.length) {
                        break;
                    }
                    String element = strArr2[i];
                    if (element != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                    }
                    i++;
                }
                size = size + dataSize + (dataCount * 1);
            }
            String[] strArr3 = this.imsiPrefixXpattern;
            if (strArr3 != null && strArr3.length > 0) {
                int dataCount2 = 0;
                int dataSize2 = 0;
                int i2 = 0;
                while (true) {
                    String[] strArr4 = this.imsiPrefixXpattern;
                    if (i2 >= strArr4.length) {
                        break;
                    }
                    String element2 = strArr4[i2];
                    if (element2 != null) {
                        dataCount2++;
                        dataSize2 += CodedOutputByteBufferNano.computeStringSizeNoTag(element2);
                    }
                    i2++;
                }
                size = size + dataSize2 + (dataCount2 * 1);
            }
            String[] strArr5 = this.spn;
            if (strArr5 != null && strArr5.length > 0) {
                int dataCount3 = 0;
                int dataSize3 = 0;
                int i3 = 0;
                while (true) {
                    String[] strArr6 = this.spn;
                    if (i3 >= strArr6.length) {
                        break;
                    }
                    String element3 = strArr6[i3];
                    if (element3 != null) {
                        dataCount3++;
                        dataSize3 += CodedOutputByteBufferNano.computeStringSizeNoTag(element3);
                    }
                    i3++;
                }
                size = size + dataSize3 + (dataCount3 * 1);
            }
            String[] strArr7 = this.plmn;
            if (strArr7 != null && strArr7.length > 0) {
                int dataCount4 = 0;
                int dataSize4 = 0;
                int i4 = 0;
                while (true) {
                    String[] strArr8 = this.plmn;
                    if (i4 >= strArr8.length) {
                        break;
                    }
                    String element4 = strArr8[i4];
                    if (element4 != null) {
                        dataCount4++;
                        dataSize4 += CodedOutputByteBufferNano.computeStringSizeNoTag(element4);
                    }
                    i4++;
                }
                size = size + dataSize4 + (dataCount4 * 1);
            }
            String[] strArr9 = this.gid1;
            if (strArr9 != null && strArr9.length > 0) {
                int dataCount5 = 0;
                int dataSize5 = 0;
                int i5 = 0;
                while (true) {
                    String[] strArr10 = this.gid1;
                    if (i5 >= strArr10.length) {
                        break;
                    }
                    String element5 = strArr10[i5];
                    if (element5 != null) {
                        dataCount5++;
                        dataSize5 += CodedOutputByteBufferNano.computeStringSizeNoTag(element5);
                    }
                    i5++;
                }
                size = size + dataSize5 + (dataCount5 * 1);
            }
            String[] strArr11 = this.gid2;
            if (strArr11 != null && strArr11.length > 0) {
                int dataCount6 = 0;
                int dataSize6 = 0;
                int i6 = 0;
                while (true) {
                    String[] strArr12 = this.gid2;
                    if (i6 >= strArr12.length) {
                        break;
                    }
                    String element6 = strArr12[i6];
                    if (element6 != null) {
                        dataCount6++;
                        dataSize6 += CodedOutputByteBufferNano.computeStringSizeNoTag(element6);
                    }
                    i6++;
                }
                size = size + dataSize6 + (dataCount6 * 1);
            }
            String[] strArr13 = this.preferredApn;
            if (strArr13 != null && strArr13.length > 0) {
                int dataCount7 = 0;
                int dataSize7 = 0;
                int i7 = 0;
                while (true) {
                    String[] strArr14 = this.preferredApn;
                    if (i7 >= strArr14.length) {
                        break;
                    }
                    String element7 = strArr14[i7];
                    if (element7 != null) {
                        dataCount7++;
                        dataSize7 += CodedOutputByteBufferNano.computeStringSizeNoTag(element7);
                    }
                    i7++;
                }
                size = size + dataSize7 + (dataCount7 * 1);
            }
            String[] strArr15 = this.iccidPrefix;
            if (strArr15 != null && strArr15.length > 0) {
                int dataCount8 = 0;
                int dataSize8 = 0;
                int i8 = 0;
                while (true) {
                    String[] strArr16 = this.iccidPrefix;
                    if (i8 >= strArr16.length) {
                        break;
                    }
                    String element8 = strArr16[i8];
                    if (element8 != null) {
                        dataCount8++;
                        dataSize8 += CodedOutputByteBufferNano.computeStringSizeNoTag(element8);
                    }
                    i8++;
                }
                size = size + dataSize8 + (dataCount8 * 1);
            }
            String[] strArr17 = this.privilegeAccessRule;
            if (strArr17 == null || strArr17.length <= 0) {
                return size;
            }
            int dataCount9 = 0;
            int dataSize9 = 0;
            int i9 = 0;
            while (true) {
                String[] strArr18 = this.privilegeAccessRule;
                if (i9 >= strArr18.length) {
                    return size + dataSize9 + (dataCount9 * 1);
                }
                String element9 = strArr18[i9];
                if (element9 != null) {
                    dataCount9++;
                    dataSize9 += CodedOutputByteBufferNano.computeStringSizeNoTag(element9);
                }
                i9++;
            }
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public CarrierAttribute mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    String[] strArr = this.mccmncTuple;
                    int i = strArr == null ? 0 : strArr.length;
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
                    String[] strArr2 = this.imsiPrefixXpattern;
                    int i2 = strArr2 == null ? 0 : strArr2.length;
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
                    String[] strArr3 = this.spn;
                    int i3 = strArr3 == null ? 0 : strArr3.length;
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
                    String[] strArr4 = this.plmn;
                    int i4 = strArr4 == null ? 0 : strArr4.length;
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
                    String[] strArr5 = this.gid1;
                    int i5 = strArr5 == null ? 0 : strArr5.length;
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
                    String[] strArr6 = this.gid2;
                    int i6 = strArr6 == null ? 0 : strArr6.length;
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
                    String[] strArr7 = this.preferredApn;
                    int i7 = strArr7 == null ? 0 : strArr7.length;
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
                    String[] strArr8 = this.iccidPrefix;
                    int i8 = strArr8 == null ? 0 : strArr8.length;
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
                } else if (tag == 74) {
                    int arrayLength9 = WireFormatNano.getRepeatedFieldArrayLength(input, 74);
                    String[] strArr9 = this.privilegeAccessRule;
                    int i9 = strArr9 == null ? 0 : strArr9.length;
                    String[] newArray9 = new String[(i9 + arrayLength9)];
                    if (i9 != 0) {
                        System.arraycopy(this.privilegeAccessRule, 0, newArray9, 0, i9);
                    }
                    while (i9 < newArray9.length - 1) {
                        newArray9[i9] = input.readString();
                        input.readTag();
                        i9++;
                    }
                    newArray9[i9] = input.readString();
                    this.privilegeAccessRule = newArray9;
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
}
