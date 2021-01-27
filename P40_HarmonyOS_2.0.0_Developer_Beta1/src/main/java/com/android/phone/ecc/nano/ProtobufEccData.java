package com.android.phone.ecc.nano;

import com.android.internal.telephony.PhoneConfigurationManager;
import java.io.IOException;

public interface ProtobufEccData {

    public static final class EccInfo extends ExtendableMessageNano<EccInfo> {
        private static volatile EccInfo[] _emptyArray;
        public String phoneNumber;
        public int[] types;

        public interface Type {
            public static final int AMBULANCE = 2;
            public static final int FIRE = 3;
            public static final int POLICE = 1;
            public static final int TYPE_UNSPECIFIED = 0;
        }

        public static EccInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new EccInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public EccInfo() {
            clear();
        }

        public EccInfo clear() {
            this.phoneNumber = PhoneConfigurationManager.SSSS;
            this.types = WireFormatNano.EMPTY_INT_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.phone.ecc.nano.ExtendableMessageNano, com.android.phone.ecc.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.phoneNumber.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(1, this.phoneNumber);
            }
            int[] iArr = this.types;
            if (iArr != null && iArr.length > 0) {
                int dataSize = 0;
                int i = 0;
                while (true) {
                    int[] iArr2 = this.types;
                    if (i >= iArr2.length) {
                        break;
                    }
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(iArr2[i]);
                    i++;
                }
                output.writeRawVarint32(18);
                output.writeRawVarint32(dataSize);
                int i2 = 0;
                while (true) {
                    int[] iArr3 = this.types;
                    if (i2 >= iArr3.length) {
                        break;
                    }
                    output.writeRawVarint32(iArr3[i2]);
                    i2++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.ExtendableMessageNano, com.android.phone.ecc.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.phoneNumber.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.phoneNumber);
            }
            int[] iArr = this.types;
            if (iArr == null || iArr.length <= 0) {
                return size;
            }
            int dataSize = 0;
            int i = 0;
            while (true) {
                int[] iArr2 = this.types;
                if (i >= iArr2.length) {
                    return size + dataSize + 1 + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
                }
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(iArr2[i]);
                i++;
            }
        }

        @Override // com.android.phone.ecc.nano.MessageNano
        public EccInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.phoneNumber = input.readString();
                } else if (tag == 16) {
                    int length = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                    int[] validValues = new int[length];
                    int validCount = 0;
                    for (int i = 0; i < length; i++) {
                        if (i != 0) {
                            input.readTag();
                        }
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2 || value == 3) {
                            validValues[validCount] = value;
                            validCount++;
                        } else {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                        }
                    }
                    if (validCount != 0) {
                        int[] iArr = this.types;
                        int i2 = iArr == null ? 0 : iArr.length;
                        if (i2 == 0 && validCount == validValues.length) {
                            this.types = validValues;
                        } else {
                            int[] newArray = new int[(i2 + validCount)];
                            if (i2 != 0) {
                                System.arraycopy(this.types, 0, newArray, 0, i2);
                            }
                            System.arraycopy(validValues, 0, newArray, i2, validCount);
                            this.types = newArray;
                        }
                    }
                } else if (tag == 18) {
                    int limit = input.pushLimit(input.readRawVarint32());
                    int arrayLength = 0;
                    int startPos = input.getPosition();
                    while (input.getBytesUntilLimit() > 0) {
                        int readInt32 = input.readInt32();
                        if (readInt32 == 0 || readInt32 == 1 || readInt32 == 2 || readInt32 == 3) {
                            arrayLength++;
                        }
                    }
                    if (arrayLength != 0) {
                        input.rewindToPosition(startPos);
                        int[] iArr2 = this.types;
                        int i3 = iArr2 == null ? 0 : iArr2.length;
                        int[] newArray2 = new int[(i3 + arrayLength)];
                        if (i3 != 0) {
                            System.arraycopy(this.types, 0, newArray2, 0, i3);
                        }
                        while (input.getBytesUntilLimit() > 0) {
                            int initialPos2 = input.getPosition();
                            int value2 = input.readInt32();
                            if (value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3) {
                                newArray2[i3] = value2;
                                i3++;
                            } else {
                                input.rewindToPosition(initialPos2);
                                storeUnknownField(input, 16);
                            }
                        }
                        this.types = newArray2;
                    }
                    input.popLimit(limit);
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static EccInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EccInfo) MessageNano.mergeFrom(new EccInfo(), data);
        }

        public static EccInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EccInfo().mergeFrom(input);
        }
    }

    public static final class CountryInfo extends ExtendableMessageNano<CountryInfo> {
        private static volatile CountryInfo[] _emptyArray;
        public String eccFallback;
        public EccInfo[] eccs;
        public String isoCode;

        public static CountryInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new CountryInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public CountryInfo() {
            clear();
        }

        public CountryInfo clear() {
            this.isoCode = PhoneConfigurationManager.SSSS;
            this.eccs = EccInfo.emptyArray();
            this.eccFallback = PhoneConfigurationManager.SSSS;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.phone.ecc.nano.ExtendableMessageNano, com.android.phone.ecc.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.isoCode.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(1, this.isoCode);
            }
            EccInfo[] eccInfoArr = this.eccs;
            if (eccInfoArr != null && eccInfoArr.length > 0) {
                int i = 0;
                while (true) {
                    EccInfo[] eccInfoArr2 = this.eccs;
                    if (i >= eccInfoArr2.length) {
                        break;
                    }
                    EccInfo element = eccInfoArr2[i];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i++;
                }
            }
            if (!this.eccFallback.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(3, this.eccFallback);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.ExtendableMessageNano, com.android.phone.ecc.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.isoCode.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.isoCode);
            }
            EccInfo[] eccInfoArr = this.eccs;
            if (eccInfoArr != null && eccInfoArr.length > 0) {
                int i = 0;
                while (true) {
                    EccInfo[] eccInfoArr2 = this.eccs;
                    if (i >= eccInfoArr2.length) {
                        break;
                    }
                    EccInfo element = eccInfoArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i++;
                }
            }
            if (!this.eccFallback.equals(PhoneConfigurationManager.SSSS)) {
                return size + CodedOutputByteBufferNano.computeStringSize(3, this.eccFallback);
            }
            return size;
        }

        @Override // com.android.phone.ecc.nano.MessageNano
        public CountryInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.isoCode = input.readString();
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    EccInfo[] eccInfoArr = this.eccs;
                    int i = eccInfoArr == null ? 0 : eccInfoArr.length;
                    EccInfo[] newArray = new EccInfo[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.eccs, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new EccInfo();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new EccInfo();
                    input.readMessage(newArray[i]);
                    this.eccs = newArray;
                } else if (tag == 26) {
                    this.eccFallback = input.readString();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static CountryInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (CountryInfo) MessageNano.mergeFrom(new CountryInfo(), data);
        }

        public static CountryInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new CountryInfo().mergeFrom(input);
        }
    }

    public static final class AllInfo extends ExtendableMessageNano<AllInfo> {
        private static volatile AllInfo[] _emptyArray;
        public CountryInfo[] countries;
        public int revision;

        public static AllInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new AllInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public AllInfo() {
            clear();
        }

        public AllInfo clear() {
            this.revision = 0;
            this.countries = CountryInfo.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.phone.ecc.nano.ExtendableMessageNano, com.android.phone.ecc.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.revision;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            CountryInfo[] countryInfoArr = this.countries;
            if (countryInfoArr != null && countryInfoArr.length > 0) {
                int i2 = 0;
                while (true) {
                    CountryInfo[] countryInfoArr2 = this.countries;
                    if (i2 >= countryInfoArr2.length) {
                        break;
                    }
                    CountryInfo element = countryInfoArr2[i2];
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                    i2++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.ExtendableMessageNano, com.android.phone.ecc.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.revision;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            CountryInfo[] countryInfoArr = this.countries;
            if (countryInfoArr != null && countryInfoArr.length > 0) {
                int i2 = 0;
                while (true) {
                    CountryInfo[] countryInfoArr2 = this.countries;
                    if (i2 >= countryInfoArr2.length) {
                        break;
                    }
                    CountryInfo element = countryInfoArr2[i2];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                    i2++;
                }
            }
            return size;
        }

        @Override // com.android.phone.ecc.nano.MessageNano
        public AllInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.revision = input.readInt32();
                } else if (tag == 18) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    CountryInfo[] countryInfoArr = this.countries;
                    int i = countryInfoArr == null ? 0 : countryInfoArr.length;
                    CountryInfo[] newArray = new CountryInfo[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.countries, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new CountryInfo();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new CountryInfo();
                    input.readMessage(newArray[i]);
                    this.countries = newArray;
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static AllInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (AllInfo) MessageNano.mergeFrom(new AllInfo(), data);
        }

        public static AllInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new AllInfo().mergeFrom(input);
        }
    }
}
