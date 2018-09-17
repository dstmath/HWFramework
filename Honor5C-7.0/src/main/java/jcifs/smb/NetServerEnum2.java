package jcifs.smb;

import java.io.UnsupportedEncodingException;

class NetServerEnum2 extends SmbComTransaction {
    static final String[] DESCR = null;
    static final int SV_TYPE_ALL = -1;
    static final int SV_TYPE_DOMAIN_ENUM = Integer.MIN_VALUE;
    String domain;
    String lastName;
    int serverTypes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.NetServerEnum2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.NetServerEnum2.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.NetServerEnum2.<clinit>():void");
    }

    NetServerEnum2(String domain, int serverTypes) {
        this.lastName = null;
        this.domain = domain;
        this.serverTypes = serverTypes;
        this.command = (byte) 37;
        this.subCommand = (byte) 104;
        this.name = "\\PIPE\\LANMAN";
        this.maxParameterCount = 8;
        this.maxDataCount = SmbConstants.FLAGS2_STATUS32;
        this.maxSetupCount = (byte) 0;
        this.setupCount = 0;
        this.timeout = 5000;
    }

    void reset(int key, String lastName) {
        super.reset();
        this.lastName = lastName;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int which;
        int start = dstIndex;
        if (this.subCommand == 104) {
            which = 0;
        } else {
            which = 1;
        }
        try {
            byte[] descr = DESCR[which].getBytes("ASCII");
            ServerMessageBlock.writeInt2((long) (this.subCommand & 255), dst, dstIndex);
            dstIndex += 2;
            System.arraycopy(descr, 0, dst, dstIndex, descr.length);
            dstIndex += descr.length;
            ServerMessageBlock.writeInt2(1, dst, dstIndex);
            dstIndex += 2;
            ServerMessageBlock.writeInt2((long) this.maxDataCount, dst, dstIndex);
            dstIndex += 2;
            ServerMessageBlock.writeInt4((long) this.serverTypes, dst, dstIndex);
            dstIndex += 4;
            dstIndex += writeString(this.domain.toUpperCase(), dst, dstIndex, false);
            if (which == 1) {
                dstIndex += writeString(this.lastName.toUpperCase(), dst, dstIndex, false);
            }
            return dstIndex - start;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return 0;
    }

    public String toString() {
        return new String("NetServerEnum2[" + super.toString() + ",name=" + this.name + ",serverTypes=" + (this.serverTypes == SV_TYPE_ALL ? "SV_TYPE_ALL" : "SV_TYPE_DOMAIN_ENUM") + "]");
    }
}
