package jcifs.smb;

class SmbComReadAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = 0;
    private int fid;
    int maxCount;
    int minCount;
    private long offset;
    private int openTimeout;
    int remaining;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SmbComReadAndX.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SmbComReadAndX.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SmbComReadAndX.<clinit>():void");
    }

    SmbComReadAndX() {
        super(null);
        this.command = (byte) 46;
        this.openTimeout = -1;
    }

    SmbComReadAndX(int fid, long offset, int maxCount, ServerMessageBlock andx) {
        super(andx);
        this.fid = fid;
        this.offset = offset;
        this.minCount = maxCount;
        this.maxCount = maxCount;
        this.command = (byte) 46;
        this.openTimeout = -1;
    }

    void setParam(int fid, long offset, int maxCount) {
        this.fid = fid;
        this.offset = offset;
        this.minCount = maxCount;
        this.maxCount = maxCount;
    }

    int getBatchLimit(byte command) {
        return command == 4 ? BATCH_LIMIT : 0;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.fid, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4(this.offset, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.maxCount, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.minCount, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.openTimeout, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.remaining, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4(this.offset >> 32, dst, dstIndex);
        return (dstIndex + 4) - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComReadAndX[" + super.toString() + ",fid=" + this.fid + ",offset=" + this.offset + ",maxCount=" + this.maxCount + ",minCount=" + this.minCount + ",openTimeout=" + this.openTimeout + ",remaining=" + this.remaining + ",offset=" + this.offset + "]");
    }
}
