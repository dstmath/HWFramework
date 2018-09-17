package java.security.cert;

import java.io.IOException;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerValue;

public class PolicyQualifierInfo {
    private byte[] mData;
    private byte[] mEncoded;
    private String mId;
    private String pqiString;

    public PolicyQualifierInfo(byte[] encoded) throws IOException {
        this.mEncoded = (byte[]) encoded.clone();
        DerValue val = new DerValue(this.mEncoded);
        if (val.tag != (byte) 48) {
            throw new IOException("Invalid encoding for PolicyQualifierInfo");
        }
        this.mId = val.data.getDerValue().getOID().toString();
        byte[] tmp = val.data.toByteArray();
        if (tmp == null) {
            this.mData = null;
            return;
        }
        this.mData = new byte[tmp.length];
        System.arraycopy(tmp, 0, this.mData, 0, tmp.length);
    }

    public final String getPolicyQualifierId() {
        return this.mId;
    }

    public final byte[] getEncoded() {
        return (byte[]) this.mEncoded.clone();
    }

    public final byte[] getPolicyQualifier() {
        return this.mData == null ? null : (byte[]) this.mData.clone();
    }

    public String toString() {
        if (this.pqiString != null) {
            return this.pqiString;
        }
        HexDumpEncoder enc = new HexDumpEncoder();
        StringBuffer sb = new StringBuffer();
        sb.append("PolicyQualifierInfo: [\n");
        sb.append("  qualifierID: " + this.mId + "\n");
        sb.append("  qualifier: " + (this.mData == null ? "null" : enc.encodeBuffer(this.mData)) + "\n");
        sb.append("]");
        this.pqiString = sb.toString();
        return this.pqiString;
    }
}
