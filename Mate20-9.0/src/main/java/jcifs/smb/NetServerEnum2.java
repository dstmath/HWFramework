package jcifs.smb;

import java.io.UnsupportedEncodingException;

class NetServerEnum2 extends SmbComTransaction {
    static final String[] DESCR = {"WrLehDO\u0000B16BBDz\u0000", "WrLehDz\u0000B16BBDz\u0000"};
    static final int SV_TYPE_ALL = -1;
    static final int SV_TYPE_DOMAIN_ENUM = Integer.MIN_VALUE;
    String domain;
    String lastName = null;
    int serverTypes;

    NetServerEnum2(String domain2, int serverTypes2) {
        this.domain = domain2;
        this.serverTypes = serverTypes2;
        this.command = 37;
        this.subCommand = 104;
        this.name = "\\PIPE\\LANMAN";
        this.maxParameterCount = 8;
        this.maxDataCount = 16384;
        this.maxSetupCount = 0;
        this.setupCount = 0;
        this.timeout = 5000;
    }

    /* access modifiers changed from: package-private */
    public void reset(int key, String lastName2) {
        super.reset();
        this.lastName = lastName2;
    }

    /* access modifiers changed from: package-private */
    public int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int which;
        int start = dstIndex;
        if (this.subCommand == 104) {
            which = 0;
        } else {
            which = 1;
        }
        try {
            byte[] descr = DESCR[which].getBytes("ASCII");
            writeInt2((long) (this.subCommand & 255), dst, dstIndex);
            int dstIndex2 = dstIndex + 2;
            System.arraycopy(descr, 0, dst, dstIndex2, descr.length);
            int dstIndex3 = dstIndex2 + descr.length;
            writeInt2(1, dst, dstIndex3);
            int dstIndex4 = dstIndex3 + 2;
            writeInt2((long) this.maxDataCount, dst, dstIndex4);
            int dstIndex5 = dstIndex4 + 2;
            writeInt4((long) this.serverTypes, dst, dstIndex5);
            int dstIndex6 = dstIndex5 + 4;
            int dstIndex7 = dstIndex6 + writeString(this.domain.toUpperCase(), dst, dstIndex6, false);
            if (which == 1) {
                dstIndex7 += writeString(this.lastName.toUpperCase(), dst, dstIndex7, false);
            }
            return dstIndex7 - start;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("NetServerEnum2[" + super.toString() + ",name=" + this.name + ",serverTypes=" + (this.serverTypes == -1 ? "SV_TYPE_ALL" : "SV_TYPE_DOMAIN_ENUM") + "]");
    }
}
