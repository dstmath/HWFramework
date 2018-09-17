package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class TLVInvoker<T> implements ICommand {
    protected int mID;
    protected Byte[] mOriginalByteArray;
    protected short mTag;
    protected T mType;
    protected Byte[] zeroLength;

    public abstract <T> T byteArray2Type(Byte[] bArr);

    public abstract <T> T findTypeByTag(short s);

    public abstract int getID();

    protected abstract short getTagByType(T t);

    public abstract boolean isTypeExists(int i);

    public abstract boolean isTypeExists(short s);

    public abstract <T> Byte[] type2ByteArray(T t);

    public TLVInvoker() {
        this.mID = 0;
        this.mTag = (short) 0;
        this.mType = null;
        this.mOriginalByteArray = null;
        this.zeroLength = new Byte[]{Byte.valueOf((byte) 0), Byte.valueOf((byte) 0)};
    }

    public TLVInvoker(int id) {
        this.mID = 0;
        this.mTag = (short) 0;
        this.mType = null;
        this.mOriginalByteArray = null;
        this.zeroLength = new Byte[]{Byte.valueOf((byte) 0), Byte.valueOf((byte) 0)};
        this.mID = id;
    }

    public TLVInvoker(short tag) {
        this.mID = 0;
        this.mTag = (short) 0;
        this.mType = null;
        this.mOriginalByteArray = null;
        this.zeroLength = new Byte[]{Byte.valueOf((byte) 0), Byte.valueOf((byte) 0)};
        this.mTag = tag;
    }

    public TLVInvoker(T t) {
        this.mID = 0;
        this.mTag = (short) 0;
        this.mType = null;
        this.mOriginalByteArray = null;
        this.zeroLength = new Byte[]{Byte.valueOf((byte) 0), Byte.valueOf((byte) 0)};
        this.mType = t;
    }

    public TLVInvoker(short tag, T t) {
        this.mID = 0;
        this.mTag = (short) 0;
        this.mType = null;
        this.mOriginalByteArray = null;
        this.zeroLength = new Byte[]{Byte.valueOf((byte) 0), Byte.valueOf((byte) 0)};
        this.mTag = tag;
        this.mType = t;
    }

    public boolean parse(Byte[] bytes) {
        return parse(ByteUtil.unboxByteArray(bytes));
    }

    public boolean parse(byte[] bytes) {
        LogHelper.d(ICommand.TAG, "************parseTLV************");
        if (bytes == null || bytes.length == 0) {
            LogHelper.d(ICommand.TAG, "parsed tlv with no content");
            return false;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return parse(buffer);
    }

    public boolean parse(ByteBuffer buffer) {
        return parseTLVInternal(buffer);
    }

    private boolean parseTLVInternal(ByteBuffer buffer) {
        if (buffer == null || buffer.remaining() < 4) {
            LogHelper.e(ICommand.TAG, "error_tlv: remaining tlv is too short: " + (buffer == null ? "buffer is null" : Integer.valueOf(buffer.remaining())));
            return false;
        }
        short tag = buffer.getShort();
        if (isTypeExists(tag)) {
            this.mType = findTypeByTag(tag);
            this.mTag = tag;
            this.mID = getID();
            LogHelper.d(ICommand.TAG, "parse tag: 0x" + ByteUtil.short2HexString(tag));
            short claimedValueLength = buffer.getShort();
            if (claimedValueLength < (short) 0 || buffer.remaining() < claimedValueLength) {
                LogHelper.e(ICommand.TAG, "error_tlv: Invalid TLV-- tag: 0x" + ByteUtil.short2HexString(tag) + " length: " + claimedValueLength);
                return false;
            }
            byte[] originalValue = new byte[claimedValueLength];
            buffer.get(originalValue, 0, claimedValueLength);
            this.mOriginalByteArray = ByteUtil.boxbyteArray(originalValue);
            if (byteArray2Type(this.mOriginalByteArray) == null) {
                LogHelper.e(ICommand.TAG, "error_tlv in TLVInvoker.parseTLVInternal");
                return false;
            }
            Class clazz = ByteUtil.getType(this.mType);
            LogHelper.d(ICommand.TAG, "Vlength:" + claimedValueLength + " type:" + (clazz == null ? "null" : clazz.getSimpleName()) + "\nvalue: " + ByteUtil.type2HexString(this.mType));
            return true;
        }
        LogHelper.d("error_tlv: unknown tag: 0x" + ByteUtil.short2HexString(tag), new String[0]);
        return false;
    }

    public Byte[] encapsulate() {
        LogHelper.d(ICommand.TAG, "************encapsulate to TLV************");
        if (this.mType == null) {
            LogHelper.e(ICommand.TAG, "error_tlv in encapsulate: no encapsulated tlv");
            return new Byte[0];
        } else if (this.mTag == (short) 0) {
            LogHelper.d(ICommand.TAG, "encapsulated TLV has no content");
            return new Byte[0];
        } else {
            Byte[] length;
            this.mTag = getTagByType(this.mType);
            Byte[] tag = ByteUtil.short2ByteArray(this.mTag);
            Byte[] value = type2ByteArray(this.mType);
            if (value == null || value.length == 0) {
                length = this.zeroLength;
                value = new Byte[0];
            } else {
                length = ByteUtil.short2ByteArray((short) value.length);
            }
            Byte[] targetTLV = new Byte[((tag.length + length.length) + value.length)];
            System.arraycopy(tag, 0, targetTLV, 0, tag.length);
            System.arraycopy(length, 0, targetTLV, tag.length, length.length);
            System.arraycopy(value, 0, targetTLV, tag.length + length.length, value.length);
            System.arraycopy(value, 0, targetTLV, tag.length + length.length, value.length);
            LogHelper.d(ICommand.TAG, "[T, L]: [0x" + ByteUtil.short2HexString(this.mTag) + ", " + targetTLV.length + " bytes]");
            LogHelper.d(ICommand.TAG, "[T, L, V]: [" + tag.length + ", " + length.length + ", " + value.length + "]");
            LogHelper.d(ICommand.TAG, "Value: " + ByteUtil.type2HexString(value));
            return targetTLV;
        }
    }

    public short getTag() {
        return this.mTag;
    }

    TLVInvoker<T> setTag(short tag) {
        this.mTag = tag;
        return this;
    }

    public T getTLVStruct() {
        return this.mType;
    }

    public TLVInvoker<T> setTLVStruct(T value) {
        this.mType = value;
        return this;
    }

    public int getSize() {
        return ByteUtil.getSizeofType(this.mType);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this.mOriginalByteArray == null) {
            return sb.toString();
        }
        for (Byte byteValue : this.mOriginalByteArray) {
            sb.append(byteValue.byteValue()).append(" ");
        }
        return sb.toString();
    }
}
