package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;

public abstract class TLVNumberInvoker<T extends Number> extends TLVInvoker<Number> {

    public static class TLVIntegerInvoker extends TLVNumberInvoker<Integer> {
        public TLVIntegerInvoker(short tag) {
            super(tag);
        }

        public TLVIntegerInvoker(short tag, Number t) {
            super(tag, t);
        }

        public <T> T byteArray2Type(Byte[] raw) {
            if (raw == null) {
                LogHelper.e(ICommand.TAG, "error_tlv in TLVNumberInvoker.byteArray2Type:input byte array is null");
                return null;
            }
            T byteArray2Int = ByteUtil.byteArray2Int(raw);
            this.mType = byteArray2Int;
            return byteArray2Int;
        }
    }

    public static class TLVLongInvoker extends TLVNumberInvoker<Long> {
        public TLVLongInvoker(short tag) {
            super(tag);
        }

        public TLVLongInvoker(short tag, Number t) {
            super(tag, t);
        }

        public <T> T byteArray2Type(Byte[] raw) {
            if (raw == null) {
                LogHelper.e(ICommand.TAG, "error_tlv in TLVNumberInvoker.byteArray2Type:input byte array is null");
                return null;
            }
            T byteArray2Long = ByteUtil.byteArray2Long(raw);
            this.mType = byteArray2Long;
            return byteArray2Long;
        }
    }

    public static class TLVShortInvoker extends TLVNumberInvoker<Short> {
        public TLVShortInvoker(short tag) {
            super(tag);
        }

        public TLVShortInvoker(short tag, Number t) {
            super(tag, t);
        }

        public <T> T byteArray2Type(Byte[] raw) {
            if (raw == null) {
                LogHelper.e(ICommand.TAG, "error_tlv in TLVNumberInvoker.byteArray2Type:input byte array is null");
                return null;
            }
            T byteArray2Short = ByteUtil.byteArray2Short(raw);
            this.mType = byteArray2Short;
            return byteArray2Short;
        }
    }

    public TLVNumberInvoker(short tag) {
        super(tag);
    }

    public TLVNumberInvoker(short tag, Number t) {
        super(tag, t);
    }

    public <T> Byte[] type2ByteArray(T t) {
        if (t == null || ((t instanceof Number) ^ 1) != 0) {
            return new Byte[0];
        }
        if (t instanceof Short) {
            return ByteUtil.hexString2ByteArray(ByteUtil.type2HexString((Short) t));
        }
        if (t instanceof Integer) {
            return ByteUtil.hexString2ByteArray(ByteUtil.type2HexString((Integer) t));
        }
        if (t instanceof Long) {
            return ByteUtil.hexString2ByteArray(ByteUtil.type2HexString((Long) t));
        }
        LogHelper.e(ICommand.TAG, "type2ByteArray: unsupported type " + t.getClass().getSimpleName());
        return new Byte[0];
    }

    public boolean isTypeExists(int id) {
        return this.mID == id;
    }

    public boolean isTypeExists(short tag) {
        return this.mTag == tag;
    }

    public <T> T findTypeByTag(short tag) {
        if (this.mTag == tag) {
            return this.mType;
        }
        return null;
    }

    public int getID() {
        return 0;
    }

    public short getTagByType(Number type) {
        return this.mTag;
    }

    public String byteArray2ServerHexString() {
        return ByteUtil.byteArray2ServerHexString(this.mOriginalByteArray);
    }
}
