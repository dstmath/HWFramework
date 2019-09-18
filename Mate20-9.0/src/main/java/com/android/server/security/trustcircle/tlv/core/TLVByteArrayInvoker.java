package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;

public class TLVByteArrayInvoker extends TLVInvoker<Byte[]> {
    public TLVByteArrayInvoker(short tag) {
        super(tag);
    }

    public TLVByteArrayInvoker(short tag, Byte[] t) {
        super(tag, t);
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [T, java.lang.Object, java.lang.Byte[]] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public <T> T byteArray2Type(Byte[] r3) {
        if (r3 == 0) {
            LogHelper.e(ICommand.TAG, "error_tlv in TLVByteArrayInvoker.byteArray2Type:input byte array is null");
            return null;
        }
        this.mType = r3;
        return r3;
    }

    public <T> Byte[] type2ByteArray(T t) {
        if (t == null || !(t instanceof Byte[])) {
            return new Byte[0];
        }
        if (t instanceof Byte[]) {
            return (Byte[]) t;
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

    /* access modifiers changed from: protected */
    public short getTagByType(Byte[] type) {
        return this.mTag;
    }

    public String byteArray2ServerHexString() {
        return ByteUtil.byteArray2ServerHexString(this.mOriginalByteArray);
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
}
