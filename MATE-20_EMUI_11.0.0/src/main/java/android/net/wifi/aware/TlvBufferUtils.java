package android.net.wifi.aware;

import android.telephony.SmsManager;
import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import libcore.io.Memory;

public class TlvBufferUtils {
    private TlvBufferUtils() {
    }

    public static class TlvConstructor {
        private byte[] mArray;
        private int mArrayLength;
        private ByteOrder mByteOrder = ByteOrder.BIG_ENDIAN;
        private int mLengthSize;
        private int mPosition;
        private int mTypeSize;

        public TlvConstructor(int typeSize, int lengthSize) {
            if (typeSize < 0 || typeSize > 2 || lengthSize <= 0 || lengthSize > 2) {
                throw new IllegalArgumentException("Invalid sizes - typeSize=" + typeSize + ", lengthSize=" + lengthSize);
            }
            this.mTypeSize = typeSize;
            this.mLengthSize = lengthSize;
            this.mPosition = 0;
        }

        public TlvConstructor setByteOrder(ByteOrder byteOrder) {
            this.mByteOrder = byteOrder;
            return this;
        }

        public TlvConstructor wrap(byte[] array) {
            this.mArray = array;
            this.mArrayLength = array == null ? 0 : array.length;
            this.mPosition = 0;
            return this;
        }

        public TlvConstructor allocate(int capacity) {
            this.mArray = new byte[capacity];
            this.mArrayLength = capacity;
            this.mPosition = 0;
            return this;
        }

        public TlvConstructor allocateAndPut(List<byte[]> list) {
            if (list != null) {
                int size = 0;
                for (byte[] field : list) {
                    size += this.mTypeSize + this.mLengthSize;
                    if (field != null) {
                        size += field.length;
                    }
                }
                allocate(size);
                for (byte[] field2 : list) {
                    putByteArray(0, field2);
                }
            }
            return this;
        }

        public TlvConstructor putByte(int type, byte b) {
            checkLength(1);
            addHeader(type, 1);
            byte[] bArr = this.mArray;
            int i = this.mPosition;
            this.mPosition = i + 1;
            bArr[i] = b;
            return this;
        }

        public TlvConstructor putRawByte(byte b) {
            checkRawLength(1);
            byte[] bArr = this.mArray;
            int i = this.mPosition;
            this.mPosition = i + 1;
            bArr[i] = b;
            return this;
        }

        public TlvConstructor putByteArray(int type, byte[] array, int offset, int length) {
            checkLength(length);
            addHeader(type, length);
            if (length != 0) {
                System.arraycopy(array, offset, this.mArray, this.mPosition, length);
            }
            this.mPosition += length;
            return this;
        }

        public TlvConstructor putByteArray(int type, byte[] array) {
            return putByteArray(type, array, 0, array == null ? 0 : array.length);
        }

        public TlvConstructor putRawByteArray(byte[] array) {
            if (array == null) {
                return this;
            }
            checkRawLength(array.length);
            System.arraycopy(array, 0, this.mArray, this.mPosition, array.length);
            this.mPosition += array.length;
            return this;
        }

        public TlvConstructor putZeroLengthElement(int type) {
            checkLength(0);
            addHeader(type, 0);
            return this;
        }

        public TlvConstructor putShort(int type, short data) {
            checkLength(2);
            addHeader(type, 2);
            Memory.pokeShort(this.mArray, this.mPosition, data, this.mByteOrder);
            this.mPosition += 2;
            return this;
        }

        public TlvConstructor putInt(int type, int data) {
            checkLength(4);
            addHeader(type, 4);
            Memory.pokeInt(this.mArray, this.mPosition, data, this.mByteOrder);
            this.mPosition += 4;
            return this;
        }

        public TlvConstructor putString(int type, String data) {
            byte[] bytes = null;
            int length = 0;
            if (data != null) {
                bytes = data.getBytes();
                length = bytes.length;
            }
            return putByteArray(type, bytes, 0, length);
        }

        public byte[] getArray() {
            return Arrays.copyOf(this.mArray, getActualLength());
        }

        private int getActualLength() {
            return this.mPosition;
        }

        private void checkLength(int dataLength) {
            if (this.mPosition + this.mTypeSize + this.mLengthSize + dataLength > this.mArrayLength) {
                throw new BufferOverflowException();
            }
        }

        private void checkRawLength(int dataLength) {
            if (this.mPosition + dataLength > this.mArrayLength) {
                throw new BufferOverflowException();
            }
        }

        private void addHeader(int type, int length) {
            int i = this.mTypeSize;
            if (i == 1) {
                this.mArray[this.mPosition] = (byte) type;
            } else if (i == 2) {
                Memory.pokeShort(this.mArray, this.mPosition, (short) type, this.mByteOrder);
            }
            this.mPosition += this.mTypeSize;
            int i2 = this.mLengthSize;
            if (i2 == 1) {
                this.mArray[this.mPosition] = (byte) length;
            } else if (i2 == 2) {
                Memory.pokeShort(this.mArray, this.mPosition, (short) length, this.mByteOrder);
            }
            this.mPosition += this.mLengthSize;
        }
    }

    public static class TlvElement {
        public ByteOrder byteOrder;
        public int length;
        private byte[] mRefArray;
        public int offset;
        public int type;

        private TlvElement(int type2, int length2, byte[] refArray, int offset2) {
            this.byteOrder = ByteOrder.BIG_ENDIAN;
            this.type = type2;
            this.length = length2;
            this.mRefArray = refArray;
            this.offset = offset2;
            if (offset2 + length2 > refArray.length) {
                throw new BufferOverflowException();
            }
        }

        public byte[] getRawData() {
            byte[] bArr = this.mRefArray;
            int i = this.offset;
            return Arrays.copyOfRange(bArr, i, this.length + i);
        }

        public byte getByte() {
            if (this.length == 1) {
                return this.mRefArray[this.offset];
            }
            throw new IllegalArgumentException("Accesing a byte from a TLV element of length " + this.length);
        }

        public short getShort() {
            if (this.length == 2) {
                return Memory.peekShort(this.mRefArray, this.offset, this.byteOrder);
            }
            throw new IllegalArgumentException("Accesing a short from a TLV element of length " + this.length);
        }

        public int getInt() {
            if (this.length == 4) {
                return Memory.peekInt(this.mRefArray, this.offset, this.byteOrder);
            }
            throw new IllegalArgumentException("Accesing an int from a TLV element of length " + this.length);
        }

        public String getString() {
            return new String(this.mRefArray, this.offset, this.length);
        }
    }

    public static class TlvIterable implements Iterable<TlvElement> {
        private byte[] mArray;
        private int mArrayLength;
        private ByteOrder mByteOrder = ByteOrder.BIG_ENDIAN;
        private int mLengthSize;
        private int mTypeSize;

        public TlvIterable(int typeSize, int lengthSize, byte[] array) {
            if (typeSize < 0 || typeSize > 2 || lengthSize <= 0 || lengthSize > 2) {
                throw new IllegalArgumentException("Invalid sizes - typeSize=" + typeSize + ", lengthSize=" + lengthSize);
            }
            this.mTypeSize = typeSize;
            this.mLengthSize = lengthSize;
            this.mArray = array;
            this.mArrayLength = array == null ? 0 : array.length;
        }

        public void setByteOrder(ByteOrder byteOrder) {
            this.mByteOrder = byteOrder;
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            boolean first = true;
            Iterator<TlvElement> it = iterator();
            while (it.hasNext()) {
                TlvElement tlv = it.next();
                if (!first) {
                    builder.append(SmsManager.REGEX_PREFIX_DELIMITER);
                }
                first = false;
                builder.append(" (");
                if (this.mTypeSize != 0) {
                    builder.append("T=" + tlv.type + SmsManager.REGEX_PREFIX_DELIMITER);
                }
                builder.append("L=" + tlv.length + ") ");
                if (tlv.length == 0) {
                    builder.append("<null>");
                } else if (tlv.length == 1) {
                    builder.append((int) tlv.getByte());
                } else if (tlv.length == 2) {
                    builder.append((int) tlv.getShort());
                } else if (tlv.length == 4) {
                    builder.append(tlv.getInt());
                } else {
                    builder.append("<bytes>");
                }
                if (tlv.length != 0) {
                    builder.append(" (S='" + tlv.getString() + "')");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        public List<byte[]> toList() {
            List<byte[]> list = new ArrayList<>();
            Iterator<TlvElement> it = iterator();
            while (it.hasNext()) {
                TlvElement tlv = it.next();
                list.add(Arrays.copyOfRange(tlv.mRefArray, tlv.offset, tlv.offset + tlv.length));
            }
            return list;
        }

        @Override // java.lang.Iterable
        public Iterator<TlvElement> iterator() {
            return new Iterator<TlvElement>() {
                /* class android.net.wifi.aware.TlvBufferUtils.TlvIterable.AnonymousClass1 */
                private int mOffset = 0;

                @Override // java.util.Iterator
                public boolean hasNext() {
                    return this.mOffset < TlvIterable.this.mArrayLength;
                }

                @Override // java.util.Iterator
                public TlvElement next() {
                    if (hasNext()) {
                        int type = 0;
                        if (TlvIterable.this.mTypeSize == 1) {
                            type = TlvIterable.this.mArray[this.mOffset];
                        } else if (TlvIterable.this.mTypeSize == 2) {
                            type = Memory.peekShort(TlvIterable.this.mArray, this.mOffset, TlvIterable.this.mByteOrder);
                        }
                        this.mOffset += TlvIterable.this.mTypeSize;
                        int length = 0;
                        if (TlvIterable.this.mLengthSize == 1) {
                            length = TlvIterable.this.mArray[this.mOffset];
                        } else if (TlvIterable.this.mLengthSize == 2) {
                            length = Memory.peekShort(TlvIterable.this.mArray, this.mOffset, TlvIterable.this.mByteOrder);
                        }
                        this.mOffset += TlvIterable.this.mLengthSize;
                        TlvElement tlv = new TlvElement(type == 1 ? 1 : 0, length, TlvIterable.this.mArray, this.mOffset);
                        tlv.byteOrder = TlvIterable.this.mByteOrder;
                        this.mOffset += length;
                        return tlv;
                    }
                    throw new NoSuchElementException();
                }

                @Override // java.util.Iterator
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static boolean isValid(byte[] array, int typeSize, int lengthSize) {
        return isValidEndian(array, typeSize, lengthSize, ByteOrder.BIG_ENDIAN);
    }

    public static boolean isValidEndian(byte[] array, int typeSize, int lengthSize, ByteOrder byteOrder) {
        if (typeSize < 0 || typeSize > 2) {
            throw new IllegalArgumentException("Invalid arguments - typeSize must be 0, 1, or 2: typeSize=" + typeSize);
        } else if (lengthSize <= 0 || lengthSize > 2) {
            throw new IllegalArgumentException("Invalid arguments - lengthSize must be 1 or 2: lengthSize=" + lengthSize);
        } else if (array == null) {
            return true;
        } else {
            int nextTlvIndex = 0;
            while (nextTlvIndex + typeSize + lengthSize <= array.length) {
                int nextTlvIndex2 = nextTlvIndex + typeSize;
                if (lengthSize == 1) {
                    nextTlvIndex = nextTlvIndex2 + array[nextTlvIndex2] + lengthSize;
                } else {
                    nextTlvIndex = nextTlvIndex2 + Memory.peekShort(array, nextTlvIndex2, byteOrder) + lengthSize;
                }
            }
            if (nextTlvIndex == array.length) {
                return true;
            }
            return false;
        }
    }
}
