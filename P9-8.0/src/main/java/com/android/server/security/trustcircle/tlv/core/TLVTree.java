package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.utils.ByteUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public abstract class TLVTree implements ICommand {
    public static final int LENGTH_SIZE = 2;
    public static final int TAG_SIZE = 2;
    protected List<ICommand> mNodeList = null;
    protected Byte[] mOriginalByteArray;

    public static abstract class TLVRootTree extends TLVTree {
    }

    public static abstract class TLVChildTree extends TLVTree {
        public int getCmdID() {
            return 0;
        }
    }

    public abstract int getCmdID();

    public abstract short getTreeTag();

    public boolean parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return parse(buffer);
    }

    public boolean parse(ByteBuffer bytes) {
        for (ICommand cmd : this.mNodeList) {
            if (!cmd.parse(bytes)) {
                return false;
            }
        }
        return true;
    }

    public Byte[] encapsulate() {
        StringBuffer sb = new StringBuffer();
        for (ICommand cmd : this.mNodeList) {
            sb.append(ByteUtil.byteArray2HexString(cmd.encapsulate()));
        }
        return ByteUtil.hexString2ByteArray(sb.toString());
    }

    public Byte[] getOriginalTLVBytes() {
        Byte[] output = new Byte[this.mOriginalByteArray.length];
        System.arraycopy(this.mOriginalByteArray, 0, output, 0, this.mOriginalByteArray.length);
        return output;
    }

    public String byteArray2ServerHexString() {
        return ByteUtil.byteArray2ServerHexString(this.mOriginalByteArray);
    }

    void setOriginalTLVBytes(Byte[] originalTLVBytes) {
        this.mOriginalByteArray = originalTLVBytes;
    }

    public String toString() {
        if (this.mNodeList == null) {
            return "no data in this tree";
        }
        StringBuffer result = new StringBuffer();
        for (ICommand cmd : this.mNodeList) {
            result.append(cmd.toString()).append(" ");
        }
        return result.toString();
    }
}
