package jcifs.smb;

import jcifs.util.Hexdump;

class Trans2FindFirst2 extends SmbComTransaction {
    private static final int DEFAULT_LIST_COUNT = 200;
    private static final int DEFAULT_LIST_SIZE = 65535;
    private static final int FLAGS_CLOSE_AFTER_THIS_REQUEST = 1;
    private static final int FLAGS_CLOSE_IF_END_REACHED = 2;
    private static final int FLAGS_FIND_WITH_BACKUP_INTENT = 16;
    private static final int FLAGS_RESUME_FROM_PREVIOUS_END = 8;
    private static final int FLAGS_RETURN_RESUME_KEYS = 4;
    static final int LIST_COUNT = 0;
    static final int LIST_SIZE = 0;
    static final int SMB_FILE_BOTH_DIRECTORY_INFO = 260;
    static final int SMB_FILE_NAMES_INFO = 259;
    static final int SMB_FIND_FILE_DIRECTORY_INFO = 257;
    static final int SMB_FIND_FILE_FULL_DIRECTORY_INFO = 258;
    static final int SMB_INFO_QUERY_EAS_FROM_LIST = 3;
    static final int SMB_INFO_QUERY_EA_SIZE = 2;
    static final int SMB_INFO_STANDARD = 1;
    private int flags;
    private int informationLevel;
    private int searchAttributes;
    private int searchStorageType;
    private String wildcard;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.Trans2FindFirst2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.Trans2FindFirst2.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.Trans2FindFirst2.<clinit>():void");
    }

    Trans2FindFirst2(String filename, String wildcard, int searchAttributes) {
        this.searchStorageType = LIST_SIZE;
        if (filename.equals("\\")) {
            this.path = filename;
        } else {
            this.path = filename + "\\";
        }
        this.wildcard = wildcard;
        this.searchAttributes = searchAttributes & 55;
        this.command = (byte) 50;
        this.subCommand = (byte) 1;
        this.flags = LIST_SIZE;
        this.informationLevel = SMB_FILE_BOTH_DIRECTORY_INFO;
        this.totalDataCount = LIST_SIZE;
        this.maxParameterCount = 10;
        this.maxDataCount = LIST_SIZE;
        this.maxSetupCount = (byte) 0;
    }

    int writeSetupWireFormat(byte[] dst, int dstIndex) {
        int i = dstIndex + SMB_INFO_STANDARD;
        dst[dstIndex] = this.subCommand;
        dstIndex = i + SMB_INFO_STANDARD;
        dst[i] = (byte) 0;
        return SMB_INFO_QUERY_EA_SIZE;
    }

    int writeParametersWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.searchAttributes, dst, dstIndex);
        dstIndex += SMB_INFO_QUERY_EA_SIZE;
        ServerMessageBlock.writeInt2((long) LIST_COUNT, dst, dstIndex);
        dstIndex += SMB_INFO_QUERY_EA_SIZE;
        ServerMessageBlock.writeInt2((long) this.flags, dst, dstIndex);
        dstIndex += SMB_INFO_QUERY_EA_SIZE;
        ServerMessageBlock.writeInt2((long) this.informationLevel, dst, dstIndex);
        dstIndex += SMB_INFO_QUERY_EA_SIZE;
        ServerMessageBlock.writeInt4((long) this.searchStorageType, dst, dstIndex);
        dstIndex += FLAGS_RETURN_RESUME_KEYS;
        return (dstIndex + writeString(this.path + this.wildcard, dst, dstIndex)) - start;
    }

    int writeDataWireFormat(byte[] dst, int dstIndex) {
        return LIST_SIZE;
    }

    int readSetupWireFormat(byte[] buffer, int bufferIndex, int len) {
        return LIST_SIZE;
    }

    int readParametersWireFormat(byte[] buffer, int bufferIndex, int len) {
        return LIST_SIZE;
    }

    int readDataWireFormat(byte[] buffer, int bufferIndex, int len) {
        return LIST_SIZE;
    }

    public String toString() {
        return new String("Trans2FindFirst2[" + super.toString() + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, (int) SMB_INFO_QUERY_EA_SIZE) + ",searchCount=" + LIST_COUNT + ",flags=0x" + Hexdump.toHexString(this.flags, (int) SMB_INFO_QUERY_EA_SIZE) + ",informationLevel=0x" + Hexdump.toHexString(this.informationLevel, (int) SMB_INFO_QUERY_EAS_FROM_LIST) + ",searchStorageType=" + this.searchStorageType + ",filename=" + this.path + "]");
    }
}
